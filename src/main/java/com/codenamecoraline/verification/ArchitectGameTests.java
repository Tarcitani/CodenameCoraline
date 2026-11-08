package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.architect.Architect;
import com.codenamecoraline.entity.architect.ArchitectAnim;
import com.codenamecoraline.entity.architect.ArchitectAttack;
import com.codenamecoraline.bestiary.Bestiary;
import com.codenamecoraline.bestiary.BestiaryPage;
import com.codenamecoraline.item.ArchitectScimitarItem;
import com.codenamecoraline.item.CommandmentTabletItem;
import com.codenamecoraline.registry.ModSounds;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Server integration tests for the Architect: foundation (slice 1), the reveal (slice 2) and the AI and combat of slice 3
 * (every attack lands inside its volume and aborts when the target escaped, parry and reprise, posture and stagger,
 * phase transitions with the bar colour, projectile deflection, protection during the reveal and anti-pillaring), and the
 * content of slice 4 (bestiary, advancements, recipe, drop, sounds, both languages) with the Architect's Scimitar.
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class ArchitectGameTests {
    private static final int TIMEOUT = 260;
    private static final Map<String, Consumer<GameTestHelper>> TESTS = new LinkedHashMap<>();

    static {
        TESTS.put("architect_attributes_and_registration", ArchitectGameTests::attributesAndRegistration);
        TESTS.put("architect_phase_thresholds", ArchitectGameTests::phaseThresholds);
        TESTS.put("architect_dormant_statue_constitution", ArchitectGameTests::dormantStatueConstitution);
        TESTS.put("architect_tablet_summon_rules", ArchitectGameTests::tabletSummonRules);
        TESTS.put("architect_player_hit_awakens_statue", ArchitectGameTests::playerHitAwakensStatue);
        TESTS.put("architect_reveal_protects_players", ArchitectGameTests::revealProtectsPlayers);
        for (ArchitectAttack a : ArchitectAttack.values()) {
            if (a == ArchitectAttack.PARRY_REPRISE) {
                continue;
            }
            String name = a.name().toLowerCase(Locale.ROOT);
            TESTS.put("architect_" + name + "_lands", h -> attackLands(h, a));
            if (a != ArchitectAttack.GARGOYLE_FALL) {
                TESTS.put("architect_" + name + "_aborts_on_whiff", h -> attackAbortsOnWhiff(h, a));
            }
        }
        TESTS.put("architect_parry_and_reprise", ArchitectGameTests::parryAndReprise);
        TESTS.put("architect_posture_stagger_and_immunity", ArchitectGameTests::postureStaggerAndImmunity);
        TESTS.put("architect_weak_hits_never_stagger", ArchitectGameTests::weakHitsNeverStagger);
        TESTS.put("architect_stagger_cancels_windup", ArchitectGameTests::staggerCancelsWindup);
        TESTS.put("architect_stagger_waits_for_active_blow", ArchitectGameTests::staggerWaitsForActiveBlow);
        TESTS.put("architect_phase_transitions_and_bar", ArchitectGameTests::phaseTransitionsAndBar);
        TESTS.put("architect_deflects_light_projectiles", ArchitectGameTests::deflectsLightProjectiles);
        TESTS.put("architect_answers_pillaring_with_gargoyle_fall", ArchitectGameTests::answersPillaring);
        // slice 4: content, drop and the Architect's Scimitar (spec §26)
        TESTS.put("architect_content_loaded", ArchitectGameTests::contentLoaded);
        TESTS.put("architect_drops_scimitar", ArchitectGameTests::dropsScimitar);
        TESTS.put("architect_scimitar_parry_in_window", ArchitectGameTests::scimitarParryInWindow);
        TESTS.put("architect_scimitar_parry_misses_outside_window", ArchitectGameTests::scimitarParryMissesOutsideWindow);
        TESTS.put("architect_scimitar_no_parry_from_behind", ArchitectGameTests::scimitarNoParryFromBehind);
        TESTS.put("architect_scimitar_reprise_bonus_once", ArchitectGameTests::scimitarRepriseBonusOnce);
        TESTS.put("architect_scimitar_reprise_expires", ArchitectGameTests::scimitarRepriseExpires);
        TESTS.put("architect_scimitar_deflects_projectiles", ArchitectGameTests::scimitarDeflectsProjectiles);
        TESTS.put("architect_scimitar_parry_breaks_his_posture", ArchitectGameTests::scimitarParryBreaksHisPosture);
    }

    private ArchitectGameTests() {}

    private static boolean enabled() {
        return Boolean.getBoolean("coraline.architectTests");
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, name);
    }

    @SubscribeEvent
    public static void functions(RegisterEvent event) {
        if (!enabled()) return;
        event.register(Registries.TEST_FUNCTION,
                registry -> TESTS.forEach((name, fn) -> registry.register(id(name), fn)));
    }

    @SubscribeEvent
    public static void tests(RegisterGameTestsEvent event) {
        if (!enabled()) return;
        var environment = event.registerEnvironment(id("architect_test_environment"));
        TESTS.forEach((name, fn) -> event.registerTest(id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                        new TestData<>(environment, id("basilisk_test_arena"), TIMEOUT, 0, true, Rotation.NONE, false, 1, 1,
                                true, 0)))); // sky access: the Gargoyle Fall leaps above the arena walls
    }

    private static void attributesAndRegistration(GameTestHelper helper) {
        Architect architect = helper.spawnWithNoFreeWill(ModEntities.ARCHITECT.get(), new BlockPos(3, 1, 3));
        helper.assertValueEqual(architect.getMaxHealth(), 480.0F, "Architect max health");
        helper.assertValueEqual(architect.getAttributeValue(Attributes.ARMOR), 16.0D, "Architect armor");
        helper.assertValueEqual(architect.getAttributeValue(Attributes.ARMOR_TOUGHNESS), 8.0D, "Architect armor toughness");
        helper.assertValueEqual(architect.getAttributeValue(Attributes.ATTACK_DAMAGE), 12.0D, "Architect base damage");
        helper.assertValueEqual(architect.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.30D, "Architect movement speed");
        helper.assertValueEqual(architect.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.95D, "Architect knockback resistance");
        helper.assertValueEqual(architect.getAttributeValue(Attributes.FOLLOW_RANGE), 48.0D, "Architect follow range");
        helper.assertValueEqual(ModEntities.ARCHITECT.get().getCategory(), MobCategory.MONSTER, "Architect category");
        helper.assertValueEqual(ModEntities.ARCHITECT.get().getWidth(), Architect.WIDTH, "Architect collision width");
        helper.assertValueEqual(ModEntities.ARCHITECT.get().getHeight(), Architect.HEIGHT, "Architect collision height");
        helper.assertValueEqual(architect.getState(), Architect.State.DORMANT, "Architect starts as the statue");
        helper.assertValueEqual(architect.getPhase(), Architect.Phase.MONUMENT, "Dormant Architect is in the monument phase");
        helper.assertTrue(architect.isPersistenceRequired(), "Architect never despawns");
        helper.assertTrue(ModItems.ARCHITECT_SPAWN_EGG.get() instanceof SpawnEggItem, "Architect spawn egg registered");
        helper.assertTrue(ModItems.COMMANDMENT_TABLET.get() instanceof CommandmentTabletItem, "Tablet of the Commandments registered");
        helper.succeed();
    }

    private static void phaseThresholds(GameTestHelper helper) {
        helper.assertValueEqual(Architect.phaseFor(1.0F), Architect.Phase.DUELIST, "Full health is the Duelist");
        helper.assertValueEqual(Architect.phaseFor(0.70F), Architect.Phase.DUELIST, "70 % is still the Duelist");
        helper.assertValueEqual(Architect.phaseFor(0.69F), Architect.Phase.ADMINISTRATOR, "Below 70 % is the Administrator");
        helper.assertValueEqual(Architect.phaseFor(0.35F), Architect.Phase.ADMINISTRATOR, "35 % is still the Administrator");
        helper.assertValueEqual(Architect.phaseFor(0.34F), Architect.Phase.COLLAPSE, "Below 35 % is the Collapse");

        Architect architect = helper.spawnWithNoFreeWill(ModEntities.ARCHITECT.get(), new BlockPos(3, 1, 3));
        architect.setState(Architect.State.COMBAT_IDLE);
        architect.setHealth(architect.getMaxHealth() * 0.5F);
        helper.assertValueEqual(architect.getPhase(), Architect.Phase.ADMINISTRATOR, "Awake Architect at half health");
        helper.succeed();
    }

    private static void dormantStatueConstitution(GameTestHelper helper) {
        Architect architect = helper.spawnWithNoFreeWill(ModEntities.ARCHITECT.get(), new BlockPos(3, 1, 3));
        float health = architect.getHealth();
        architect.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 20.0F);
        helper.assertValueEqual(architect.getHealth(), health, "The dormant statue ignores damage that is not from a player");
        helper.assertFalse(architect.isPushable(), "The dormant statue cannot be pushed");
        helper.assertFalse(architect.causeFallDamage(5.0D, 1.0F, helper.getLevel().damageSources().fall()),
                "Falls under 6 blocks are absorbed by the wings");
        helper.assertFalse(architect.canBeAffected(new MobEffectInstance(MobEffects.POISON, 100)), "Stone ignores poison");
        helper.assertFalse(architect.canBeAffected(new MobEffectInstance(MobEffects.WITHER, 100)), "Stone ignores wither");
        helper.assertTrue(architect.canBreatheUnderwater(), "Stone does not drown");
        helper.assertFalse(architect.fireImmune(), "The Architect is not immune to fire");

        architect.setState(Architect.State.COMBAT_IDLE);
        architect.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 20.0F);
        helper.assertTrue(architect.getHealth() < health, "The awake Architect takes damage");
        helper.succeed();
    }

    /** Slice 2: a player's hit wakes the statue; the reveal plays its 110 ticks (invulnerable), then the combat idle. */
    private static void playerHitAwakensStatue(GameTestHelper helper) {
        Architect architect = helper.spawn(ModEntities.ARCHITECT.get(), new BlockPos(3, 1, 3));
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        float health = architect.getHealth();
        architect.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 10.0F);
        helper.assertValueEqual(architect.getState(), Architect.State.AWAKENING, "A player hit wakes the statue");
        helper.assertValueEqual(architect.getAnim(), com.codenamecoraline.entity.architect.ArchitectAnim.AWAKEN, "The reveal clip plays");
        helper.assertValueEqual(architect.getHealth(), health, "The waking hit itself does no damage");
        architect.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 10.0F);
        helper.assertValueEqual(architect.getHealth(), health, "Invulnerable while the reveal plays");
        helper.runAfterDelay(112, () -> {
            helper.assertValueEqual(architect.getState(), Architect.State.COMBAT_IDLE, "After 110 ticks he stands in guard");
            helper.assertTrue(architect.getBossEvent().isVisible(), "The themed bar shows once he is awake");
            helper.succeed();
        });
    }

    private static void tabletSummonRules(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos floor = helper.absolutePos(new BlockPos(2, 0, 2));
        level.setBlockAndUpdate(floor, Blocks.STONE_BRICKS.defaultBlockState());

        helper.assertValueEqual(CommandmentTabletItem.evaluateConditions(level, floor, Direction.NORTH),
                CommandmentTabletItem.SummonResult.WRONG_FACE, "The tablet only works on a top face");

        BlockPos ceiling = floor.above(3);
        level.setBlockAndUpdate(ceiling, Blocks.STONE.defaultBlockState());
        helper.assertValueEqual(CommandmentTabletItem.evaluateConditions(level, floor, Direction.UP),
                CommandmentTabletItem.SummonResult.NO_ROOM, "The statue needs four free blocks of height");
        level.setBlockAndUpdate(ceiling, Blocks.AIR.defaultBlockState());

        ItemStack stack = new ItemStack(ModItems.COMMANDMENT_TABLET.get());
        CommandmentTabletItem.SummonResult result =
                CommandmentTabletItem.trySummon(level, floor, Direction.UP, 0.0F, null, stack);
        helper.assertValueEqual(result, CommandmentTabletItem.SummonResult.SUCCESS, "Summon on a free stone floor");
        helper.assertTrue(stack.isEmpty(), "A successful summon consumes the tablet");
        var summoned = level.getEntitiesOfClass(Architect.class, new net.minecraft.world.phys.AABB(floor.above()).inflate(1.0D));
        helper.assertValueEqual(summoned.size(), 1, "Exactly one statue rises on the tablet");
        helper.assertValueEqual(summoned.get(0).getState(), Architect.State.DORMANT, "The summoned Architect is a dormant statue");

        BlockPos other = helper.absolutePos(new BlockPos(6, 0, 6));
        level.setBlockAndUpdate(other, Blocks.STONE_BRICKS.defaultBlockState());
        ItemStack second = new ItemStack(ModItems.COMMANDMENT_TABLET.get());
        helper.assertValueEqual(CommandmentTabletItem.trySummon(level, other, Direction.UP, 0.0F, null, second),
                CommandmentTabletItem.SummonResult.ALREADY_ACTIVE, "Only one Architect within 96 blocks");
        helper.assertFalse(second.isEmpty(), "A failed summon keeps the tablet");
        helper.succeed();
    }

    // ================================================================= slice 3: AI and combat
    /**
     * An awake Architect with NoAI: the timelines in tick() run, the brain in customServerAiStep does not, so the test
     * drives every attack itself (removing the goals alone would leave his brain running).
     */
    private static Architect awakeBoss(GameTestHelper helper, BlockPos pos) {
        Architect boss = helper.spawnWithNoFreeWill(ModEntities.ARCHITECT.get(), pos);
        boss.setNoAi(true);
        boss.setState(Architect.State.COMBAT_IDLE);
        return boss;
    }

    /** A foe that never moves: iron golems are always his enemies (spec §17 "Alvos"). */
    private static IronGolem dummy(GameTestHelper helper, BlockPos pos) {
        return helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, pos);
    }

    private static void heavyHits(GameTestHelper helper, Architect boss, IronGolem from, int count, float amount) {
        for (int i = 0; i < count; i++) {
            boss.invulnerableTime = 0;
            boss.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(from), amount);
        }
    }

    /** Every attack hits a foe two blocks ahead, and never before its first active tick (spec §17). */
    private static void attackLands(GameTestHelper helper, ArchitectAttack attack) {
        Architect boss = awakeBoss(helper, new BlockPos(5, 1, 2));
        IronGolem golem = dummy(helper, new BlockPos(5, 1, 4));
        float full = golem.getHealth();
        boss.startAttack(attack, golem);
        helper.assertValueEqual(boss.getAnim(), attack.anim, "The attack clip plays");
        helper.runAfterDelay(attack.firstActiveTick - 3, () ->
                helper.assertValueEqual(golem.getHealth(), full, attack + ": no damage volume before the active ticks"));
        helper.runAfterDelay(attack.ticks() + 4, () -> {
            helper.assertTrue(golem.getHealth() < full, attack + " hits a foe inside its volume (aborted " + boss.getAttacksAborted() + ", landed " + boss.getStrikesLanded() + ", boss " + boss.position() + ", golem " + golem.position() + ")");
            helper.assertTrue(boss.getStrikesLanded() > 0, attack + " counts its landed strikes");
            helper.assertValueEqual(boss.getAttacksAborted(), 0, attack + " is not aborted while the foe is in reach");
            helper.assertValueEqual(boss.getState(), Architect.State.COMBAT_IDLE, attack + " returns to the combat idle");
            helper.succeed();
        });
    }

    /** Anti-whiff (MOB_CONTRACT §3.2): with the foe out of reach at the validation tick the blow is called off. */
    private static void attackAbortsOnWhiff(GameTestHelper helper, ArchitectAttack attack) {
        Architect boss = awakeBoss(helper, new BlockPos(5, 1, 1));
        IronGolem golem = dummy(helper, new BlockPos(5, 1, 10));
        float full = golem.getHealth();
        boss.startAttack(attack, golem);
        helper.runAfterDelay(attack.validationTick + 3, () -> {
            helper.assertValueEqual(boss.getAttacksAborted(), 1, attack + " aborts when the foe escaped");
            helper.assertValueEqual(boss.getState(), Architect.State.COMBAT_IDLE, attack + " goes back to the idle");
            helper.assertValueEqual(golem.getHealth(), full, attack + " never hits from out of reach");
            helper.succeed();
        });
    }

    /** Spec §11.3: a melee blow inside the 5..9 window is cancelled and answered by the reprise; then the cooldown. */
    private static void parryAndReprise(GameTestHelper helper) {
        Architect boss = awakeBoss(helper, new BlockPos(5, 1, 2));
        IronGolem golem = dummy(helper, new BlockPos(5, 1, 4));
        boss.setYRot(Architect.yawTowards(boss, golem));
        float full = golem.getHealth();
        helper.assertTrue(boss.startParry(golem), "The parry rises against a frontal attacker");
        helper.runAfterDelay(7, () -> {
            helper.assertTrue(boss.isParryWindow(), "Tick 7 is inside the parry window");
            float health = boss.getHealth();
            boolean hurt = boss.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(golem), 5.0F);
            helper.assertFalse(hurt, "A blow in the window is parried");
            helper.assertValueEqual(boss.getHealth(), health, "The parried blow does no damage");
            helper.assertValueEqual(boss.getParries(), 1, "The parry is counted");
        });
        helper.runAfterDelay(22, () ->
                helper.assertTrue(golem.getHealth() < full, "The reprise answers the parried attacker"));
        helper.runAfterDelay(30, () -> {
            helper.assertValueEqual(boss.getState(), Architect.State.COMBAT_IDLE, "Back to the idle after the reprise");
            helper.assertFalse(boss.startParry(golem), "The parry has a cooldown");
            helper.succeed();
        });
    }

    /** Spec §11.5: heavy blows fill the posture, a full posture staggers, then 200 ticks of immunity. */
    private static void postureStaggerAndImmunity(GameTestHelper helper) {
        Architect boss = awakeBoss(helper, new BlockPos(5, 1, 2));
        IronGolem golem = dummy(helper, new BlockPos(5, 1, 7));
        heavyHits(helper, boss, golem, 6, 10.0F);
        helper.assertValueEqual(boss.getState(), Architect.State.STAGGERED, "Six heavy blows break his posture");
        helper.assertValueEqual(boss.getAnim(), ArchitectAnim.STAGGER, "The stagger clip plays");
        helper.assertValueEqual(boss.getStaggers(), 1, "One stagger");
        helper.assertTrue(boss.getStaggerImmunity() > 0, "Immune to a new stagger right after");
        heavyHits(helper, boss, golem, 8, 10.0F);
        helper.assertValueEqual(boss.getStaggers(), 1, "No stagger while immune");
        helper.runAfterDelay(ArchitectAnim.STAGGER.ticks + 3, () -> {
            helper.assertValueEqual(boss.getState(), Architect.State.COMBAT_IDLE, "The stagger lasts 30 ticks");
            heavyHits(helper, boss, golem, 8, 10.0F);
            helper.assertValueEqual(boss.getStaggers(), 1, "Still immune inside the 200 ticks");
            helper.succeed();
        });
    }

    private static void weakHitsNeverStagger(GameTestHelper helper) {
        Architect boss = awakeBoss(helper, new BlockPos(5, 1, 2));
        IronGolem golem = dummy(helper, new BlockPos(5, 1, 7));
        heavyHits(helper, boss, golem, 120, 2.0F);
        helper.assertValueEqual(boss.getStaggers(), 0, "Weak damage alone never staggers");
        helper.assertTrue(boss.getPosture() < Architect.POSTURE_MAX, "Weak damage never fills the posture");
        helper.succeed();
    }

    /** Spec §17: a stagger before the first active tick cancels the blow entirely. */
    private static void staggerCancelsWindup(GameTestHelper helper) {
        Architect boss = awakeBoss(helper, new BlockPos(5, 1, 2));
        IronGolem golem = dummy(helper, new BlockPos(5, 1, 4));
        float full = golem.getHealth();
        boss.startAttack(ArchitectAttack.SIXFOLD_EDICT, golem);
        helper.runAfterDelay(3, () -> {
            heavyHits(helper, boss, golem, 6, 10.0F);
            helper.assertValueEqual(boss.getState(), Architect.State.STAGGERED, "Staggered in the windup");
            helper.assertTrue(boss.getAttack() == null, "The attack is cancelled");
        });
        helper.runAfterDelay(45, () -> {
            helper.assertValueEqual(golem.getHealth(), full, "A cancelled blow never lands");
            helper.succeed();
        });
    }

    /** A full posture during an active blow waits for it to end (stagger priority 3 < attack 2, spec §15.2). */
    private static void staggerWaitsForActiveBlow(GameTestHelper helper) {
        Architect boss = awakeBoss(helper, new BlockPos(5, 1, 2));
        IronGolem golem = dummy(helper, new BlockPos(5, 1, 4));
        ArchitectAttack wheel = ArchitectAttack.CARTENON_WHEEL;
        boss.startAttack(wheel, golem);
        helper.runAfterDelay(wheel.firstActiveTick + 2, () -> {
            heavyHits(helper, boss, golem, 6, 10.0F);
            helper.assertValueEqual(boss.getState(), Architect.State.ATTACKING, "The swing in progress is not cut");
        });
        helper.runAfterDelay(wheel.ticks() + 4, () -> {
            helper.assertValueEqual(boss.getState(), Architect.State.STAGGERED, "The stagger comes when the blow ends");
            helper.assertValueEqual(boss.getStaggers(), 1, "One stagger");
            helper.succeed();
        });
    }

    /** Spec §12.2 and §19: the enrage at 70 %, the laugh at 35 %, invulnerable while they play, bar colour per phase. */
    private static void phaseTransitionsAndBar(GameTestHelper helper) {
        Architect boss = awakeBoss(helper, new BlockPos(5, 1, 5));
        IronGolem golem = dummy(helper, new BlockPos(5, 1, 9));
        boss.setHealth(boss.getMaxHealth() * 0.6F);
        helper.runAfterDelay(2, () -> {
            helper.assertValueEqual(boss.getState(), Architect.State.PHASE_TRANSITION, "Below 70 % the transition plays");
            helper.assertValueEqual(boss.getAnim(), ArchitectAnim.PHASE_ENRAGE, "The enrage clip");
            helper.assertValueEqual(boss.getReachedPhase(), Architect.Phase.ADMINISTRATOR, "Now the Administrator");
            helper.assertValueEqual(boss.getBossEvent().getColor(), BossEvent.BossBarColor.BLUE, "Cyan bar");
            heavyHits(helper, boss, golem, 1, 20.0F);
            helper.assertValueEqual(boss.getHealth(), boss.getMaxHealth() * 0.6F, "Invulnerable during the transition");
        });
        helper.runAfterDelay(ArchitectAnim.PHASE_ENRAGE.ticks + 5, () -> {
            helper.assertValueEqual(boss.getState(), Architect.State.COMBAT_IDLE, "The fight resumes");
            boss.setHealth(boss.getMaxHealth() * 0.3F);
        });
        helper.runAfterDelay(ArchitectAnim.PHASE_ENRAGE.ticks + 8, () -> {
            helper.assertValueEqual(boss.getState(), Architect.State.PHASE_TRANSITION, "Below 35 % the collapse begins");
            helper.assertValueEqual(boss.getAnim(), ArchitectAnim.MANIACAL_LAUGH, "With his laugh");
            helper.assertValueEqual(boss.getBossEvent().getColor(), BossEvent.BossBarColor.RED, "Coral bar");
        });
        helper.runAfterDelay(ArchitectAnim.PHASE_ENRAGE.ticks + ArchitectAnim.MANIACAL_LAUGH.ticks + 12, () -> {
            helper.assertValueEqual(boss.getState(), Architect.State.COMBAT_IDLE, "The collapse fight resumes");
            helper.succeed();
        });
    }

    /** Spec §11.4: light projectiles from the front are deflected (cooldown 40); tridents are strong. */
    private static void deflectsLightProjectiles(GameTestHelper helper) {
        Architect boss = awakeBoss(helper, new BlockPos(5, 1, 5));
        var level = helper.getLevel();
        Vec3 ahead = Vec3.directionFromRotation(0.0F, boss.getYRot()).scale(2.0D).add(boss.position()).add(0.0D, 2.0D, 0.0D);
        var arrow = EntityType.ARROW.create(level, EntitySpawnReason.TRIGGERED);
        helper.assertTrue(arrow != null, "An arrow can be created");
        arrow.setPos(ahead);
        helper.assertTrue(boss.deflection(arrow) == ProjectileDeflection.REVERSE, "A frontal arrow is deflected");
        helper.assertValueEqual(boss.getDeflections(), 1, "The deflection is counted");
        helper.assertValueEqual(boss.getState(), Architect.State.GUARDING, "He raises the blades");
        helper.assertValueEqual(boss.getAnim(), ArchitectAnim.PROJECTILE_DEFLECT, "The deflection clip plays");
        var second = EntityType.ARROW.create(level, EntitySpawnReason.TRIGGERED);
        second.setPos(ahead);
        helper.assertFalse(boss.deflection(second) == ProjectileDeflection.REVERSE, "Deflection has a cooldown");
        var trident = EntityType.TRIDENT.create(level, EntitySpawnReason.TRIGGERED);
        helper.assertFalse(Architect.isLightProjectile(trident), "A trident is a strong projectile");
        helper.succeed();
    }

    /** MOB_CONTRACT §5.1: nobody near the statue takes damage while the reveal plays. */
    private static void revealProtectsPlayers(GameTestHelper helper) {
        Architect boss = helper.spawnWithNoFreeWill(ModEntities.ARCHITECT.get(), new BlockPos(5, 1, 5));
        boss.setNoAi(true);
        var player = helper.makeMockServerPlayerInLevel();
        player.setPos(boss.position().add(3.0D, 0.0D, 0.0D));
        boss.awaken();
        float health = player.getHealth();
        player.hurtServer(helper.getLevel(), helper.getLevel().damageSources().fellOutOfWorld(), 4.0F);
        helper.assertValueEqual(player.getHealth(), health, "Players near the reveal take no damage");
        helper.runAfterDelay(ArchitectAnim.AWAKEN.ticks + 3, () -> {
            helper.assertValueEqual(boss.getState(), Architect.State.COMBAT_IDLE, "The reveal is over");
            player.hurtServer(helper.getLevel(), helper.getLevel().damageSources().fellOutOfWorld(), 4.0F);
            helper.assertTrue(player.getHealth() < health, "The protection ends with the reveal");
            helper.succeed();
        });
    }

    /** MOB_CONTRACT §4.4: a foe perched 3 blocks up is answered by the Gargoyle Fall (AI on). */
    private static void answersPillaring(GameTestHelper helper) {
        for (int x = 6; x <= 8; x++) {
            for (int z = 5; z <= 7; z++) {
                for (int y = 1; y <= 3; y++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                }
            }
        }
        Architect boss = helper.spawn(ModEntities.ARCHITECT.get(), new BlockPos(2, 1, 6));
        boss.setState(Architect.State.COMBAT_IDLE);
        dummy(helper, new BlockPos(7, 4, 6));
        helper.succeedWhen(() -> helper.assertTrue(boss.getLastAttack() == ArchitectAttack.GARGOYLE_FALL,
                "The Gargoyle Fall answers the pillar"));
    }

    // ================================================================= slice 4: content and the Architect's Scimitar
    private static JsonObject lang(String name) {
        try (var in = ArchitectGameTests.class.getResourceAsStream("/assets/codenamecoraline/lang/" + name + ".json")) {
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            throw new IllegalStateException("lang " + name, e);
        }
    }

    /** Bestiary chapter, engravings, advancements, recipe, loot table, assets, sounds and both languages. */
    private static void contentLoaded(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        helper.assertTrue(Bestiary.ENTRIES.contains(Bestiary.ARCHITECT), "the Bestiary has his chapter");
        List<BestiaryPage> pages = Bestiary.ARCHITECT.pages();
        helper.assertTrue(pages.size() >= 6, "a chapter of six or more pages");
        for (String art : new String[] {"architect_anatomy", "architect_combat", "architect_spoils"}) {
            helper.assertTrue(ArchitectGameTests.class.getResourceAsStream("/assets/codenamecoraline/textures/gui/bestiary/" + art + ".png") != null,
                    "engraving " + art);
        }
        for (String adv : new String[] {"architect_tablet", "architect_statue", "architect_defeated", "architect_scimitar"}) {
            helper.assertTrue(server.getAdvancements().get(id("bosses/" + adv)) != null, "advancement bosses/" + adv);
        }
        helper.assertTrue(server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, id("commandment_tablet"))).isPresent(),
                "the tablet recipe is loaded");
        helper.assertTrue(ArchitectGameTests.class.getResourceAsStream("/data/codenamecoraline/loot_table/entities/architect.json") != null,
                "loot table");
        for (String res : new String[] {"textures/item/architect_scimitar.png", "textures/item/architect_scimitar_charged.png",
                "textures/item/architect_scimitar_3d.png", "textures/item/architect_portrait.png", "items/architect_scimitar.json",
                "models/item/architect_scimitar_3d.json", "models/item/architect_scimitar_3d_raised.json",
                "models/item/architect_scimitar_3d_charged.json", "models/item/architect_scimitar_3d_dim.json"}) {
            helper.assertTrue(ArchitectGameTests.class.getResourceAsStream("/assets/codenamecoraline/" + res) != null, "asset " + res);
        }
        helper.assertTrue(ModSounds.ARCHITECT_PARRY.isBound() && ModSounds.SCIMITAR_PARRY_SUCCESS.isBound(), "sound events registered");
        JsonObject en = lang("en_us");
        JsonObject pt = lang("pt_br");
        java.util.List<String> keys = new java.util.ArrayList<>(List.of("item.codenamecoraline.architect_scimitar",
                "item.codenamecoraline.architect_scimitar.lore1", "item.codenamecoraline.architect_portrait",
                "bestiary.codenamecoraline.architect.name", "subtitles.codenamecoraline.item.architect_scimitar.parry_success",
                "subtitles.codenamecoraline.entity.architect.laugh"));
        for (String adv : new String[] {"architect_tablet", "architect_statue", "architect_defeated", "architect_scimitar"}) {
            keys.add("advancements.codenamecoraline.bosses." + adv + ".title");
            keys.add("advancements.codenamecoraline.bosses." + adv + ".description");
        }
        for (BestiaryPage page : pages) {
            keys.add(Bestiary.ARCHITECT.titleKey(page));
            keys.add(Bestiary.ARCHITECT.bodyKey(page));
        }
        for (String key : keys) {
            helper.assertTrue(en.has(key), "en_us has " + key);
            helper.assertTrue(pt.has(key), "pt_br has " + key);
        }
        helper.succeed();
    }

    /** The loot table always gives the scimitar. */
    private static void dropsScimitar(GameTestHelper helper) {
        Architect boss = awakeBoss(helper, new BlockPos(5, 1, 5));
        Player killer = fighter(helper, new Vec3(5.5D, 1.0D, 3.0D), 0.0F);
        boss.invulnerableTime = 0;
        boss.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(killer), 100000.0F);
        helper.assertTrue(boss.isDeadOrDying(), "the boss falls");
        helper.runAfterDelay(3, () -> {
            helper.assertItemEntityPresent(ModItems.ARCHITECT_SCIMITAR.get(), new BlockPos(5, 1, 5), 4.0D);
            helper.succeed();
        });
    }

    /** A survival player in the test arena, facing yaw (0 = +Z), with the scimitar in the main hand. */
    private static Player fighter(GameTestHelper helper, Vec3 local, float yaw) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL); // a plain survival player (the server mock is creative)
        Vec3 world = helper.absoluteVec(local);
        player.snapTo(world.x, world.y, world.z, yaw, 0.0F);
        player.setYHeadRot(yaw);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ARCHITECT_SCIMITAR.get()));
        return player;
    }

    private static void hit(GameTestHelper helper, Player victim, IronGolem from, float amount) {
        victim.invulnerableTime = 0;
        victim.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(from), amount);
    }

    /** Spec §26.2: inside the window a frontal blow is cancelled, the attacker is shaken and the Reprise charges. */
    private static void scimitarParryInWindow(GameTestHelper helper) {
        Player player = fighter(helper, new Vec3(5.5D, 1.0D, 3.5D), 0.0F);
        IronGolem golem = dummy(helper, new BlockPos(5, 1, 5));
        ItemStack blade = player.getMainHandItem();
        int durability = blade.getDamageValue();
        helper.assertTrue(ArchitectScimitarItem.raiseGuard(player), "the guard rises");
        helper.assertValueEqual(blade.getDamageValue(), durability + ArchitectScimitarItem.DURABILITY_COST, "raising costs 2 durability");
        helper.runAfterDelay(4, () -> {
            helper.assertTrue(ArchitectScimitarItem.isInWindow(player), "the window is open at tick 4");
            float health = player.getHealth();
            hit(helper, player, golem, 6.0F);
            helper.assertValueEqual(player.getHealth(), health, "the parried blow does no damage");
            helper.assertTrue(ArchitectScimitarItem.isRepriseCharged(player), "the Reprise is charged");
            helper.assertTrue(ArchitectScimitarItem.isChargedStack(player.getMainHandItem()), "the blade carries the charged flag");
            helper.assertTrue(golem.hasEffect(MobEffects.SLOWNESS) && golem.hasEffect(MobEffects.WEAKNESS), "the attacker is shaken");
            helper.assertFalse(ArchitectScimitarItem.isGuarding(player), "the guard ends with the parry");
            helper.assertTrue(player.getCooldowns().isOnCooldown(player.getMainHandItem()), "the scimitar recharges");
            helper.succeed();
        });
    }

    /** Before tick 2 there is no defence; when the window passes unused the guard drops on the long cooldown. */
    private static void scimitarParryMissesOutsideWindow(GameTestHelper helper) {
        Player player = fighter(helper, new Vec3(5.5D, 1.0D, 3.5D), 0.0F);
        IronGolem golem = dummy(helper, new BlockPos(5, 1, 5));
        ArchitectScimitarItem.raiseGuard(player);
        float health = player.getHealth();
        hit(helper, player, golem, 4.0F);
        helper.assertTrue(player.getHealth() < health, "a blow before the window lands");
        helper.assertFalse(ArchitectScimitarItem.isRepriseCharged(player), "no Reprise without a parry");
        helper.runAfterDelay(ArchitectScimitarItem.WINDOW_END + 2, () -> {
            helper.assertFalse(ArchitectScimitarItem.isGuarding(player), "the guard drops after the window");
            helper.assertFalse(ArchitectScimitarItem.isRepriseCharged(player), "a missed parry charges nothing");
            float before = player.getHealth();
            hit(helper, player, golem, 4.0F);
            helper.assertTrue(player.getHealth() < before, "after the window blows land again");
            helper.assertTrue(player.getCooldowns().isOnCooldown(player.getMainHandItem()), "a missed parry is on cooldown");
            helper.succeed();
        });
    }

    /** The cone covers the front only: a blow from behind lands even inside the window. */
    private static void scimitarNoParryFromBehind(GameTestHelper helper) {
        Player player = fighter(helper, new Vec3(5.5D, 1.0D, 5.5D), 0.0F);
        IronGolem golem = dummy(helper, new BlockPos(5, 1, 3)); // behind: the player faces +Z
        ArchitectScimitarItem.raiseGuard(player);
        helper.runAfterDelay(4, () -> {
            float health = player.getHealth();
            hit(helper, player, golem, 4.0F);
            helper.assertTrue(player.getHealth() < health, "a blow from behind lands");
            helper.assertFalse(ArchitectScimitarItem.isRepriseCharged(player), "no Reprise from a blow from behind");
            helper.succeed();
        });
    }

    /** The charged Reprise multiplies one melee hit by 1.6, once. */
    private static void scimitarRepriseBonusOnce(GameTestHelper helper) {
        Player player = fighter(helper, new Vec3(5.5D, 1.0D, 3.5D), 0.0F);
        IronGolem golem = dummy(helper, new BlockPos(5, 1, 5));
        ArchitectScimitarItem.raiseGuard(player);
        helper.runAfterDelay(4, () -> {
            hit(helper, player, golem, 6.0F);
            helper.assertTrue(ArchitectScimitarItem.isRepriseCharged(player), "the Reprise is charged");
            var source = helper.getLevel().damageSources().playerAttack(player);
            golem.invulnerableTime = 0;
            float h0 = golem.getHealth();
            golem.hurtServer(helper.getLevel(), source, 10.0F);
            float first = h0 - golem.getHealth();
            golem.invulnerableTime = 0;
            float h1 = golem.getHealth();
            golem.hurtServer(helper.getLevel(), source, 10.0F);
            float second = h1 - golem.getHealth();
            helper.assertTrue(Math.abs(first - second * ArchitectScimitarItem.REPRISE_MULTIPLIER) < 0.01F,
                    "the Reprise hit deals 1.6x (" + first + " vs " + second + ")");
            helper.assertFalse(ArchitectScimitarItem.isRepriseCharged(player), "the Reprise is spent");
            helper.assertFalse(ArchitectScimitarItem.isChargedStack(player.getMainHandItem()), "the blade goes dark");
            helper.succeed();
        });
    }

    /** Unused, the Reprise fades after 40 ticks and the flag leaves the blade. */
    private static void scimitarRepriseExpires(GameTestHelper helper) {
        Player player = fighter(helper, new Vec3(5.5D, 1.0D, 3.5D), 0.0F);
        IronGolem golem = dummy(helper, new BlockPos(5, 1, 5));
        ArchitectScimitarItem.raiseGuard(player);
        helper.runAfterDelay(4, () -> {
            hit(helper, player, golem, 6.0F);
            helper.assertTrue(ArchitectScimitarItem.isRepriseCharged(player), "the Reprise is charged");
            helper.runAfterDelay(ArchitectScimitarItem.REPRISE_TICKS + 2, () -> {
                helper.assertFalse(ArchitectScimitarItem.isRepriseCharged(player), "the Reprise fades");
                helper.assertFalse(ArchitectScimitarItem.isChargedStack(player.getMainHandItem()), "the flag leaves the blade");
                helper.succeed();
            });
        });
    }

    /** A light projectile from the front flies back; a strong one is stopped and costs more durability. */
    private static void scimitarDeflectsProjectiles(GameTestHelper helper) {
        var level = helper.getLevel();
        Player player = fighter(helper, new Vec3(5.5D, 1.0D, 3.5D), 0.0F);
        ArchitectScimitarItem.raiseGuard(player);
        helper.runAfterDelay(4, () -> {
            var arrow = EntityType.ARROW.create(level, EntitySpawnReason.TRIGGERED);
            helper.assertTrue(arrow != null, "an arrow can be created");
            arrow.setPos(player.position().add(0.0D, 1.5D, 0.6D));
            arrow.setDeltaMovement(0.0D, 0.0D, -1.5D); // flying at the player's face
            level.addFreshEntity(arrow);
            helper.assertTrue(ArchitectScimitarItem.parryProjectile(player, arrow), "the frontal arrow is parried");
            helper.assertTrue(arrow.getDeltaMovement().z > 0.0D, "the arrow flies back");
            helper.assertTrue(ArchitectScimitarItem.isRepriseCharged(player), "a parried projectile charges the Reprise");
            var behind = EntityType.ARROW.create(level, EntitySpawnReason.TRIGGERED);
            behind.setDeltaMovement(0.0D, 0.0D, 1.5D);
            helper.assertFalse(ArchitectScimitarItem.parryProjectile(player, behind), "no second parry once the guard is down");
            var trident = EntityType.TRIDENT.create(level, EntitySpawnReason.TRIGGERED);
            helper.assertFalse(Architect.isLightProjectile(trident), "a trident is stopped, not sent back");
            helper.succeed();
        });
    }

    /** Parrying one of his blows weighs on his posture like a heavy hit (spec §26.2). */
    private static void scimitarParryBreaksHisPosture(GameTestHelper helper) {
        Architect boss = awakeBoss(helper, new BlockPos(5, 1, 6));
        Player player = fighter(helper, new Vec3(5.5D, 1.0D, 3.5D), 0.0F);
        ArchitectScimitarItem.raiseGuard(player);
        helper.runAfterDelay(4, () -> {
            float health = player.getHealth();
            player.invulnerableTime = 0;
            player.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(boss), 12.0F);
            helper.assertValueEqual(player.getHealth(), health, "his blow is parried");
            helper.assertTrue(boss.getPosture() >= Architect.POSTURE_HEAVY_HIT - 0.01F,
                    "the parry fills his posture like a heavy hit (" + boss.getPosture() + ")");
            helper.succeed();
        });
    }
}
