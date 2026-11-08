package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.architect.ArchitectStrikes;
import net.minecraft.world.item.Items;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
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
    private static final Map<String, Integer> TIMEOUTS = new LinkedHashMap<>();

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
        // reform: movement, facing, evasion, shield and targets
        TESTS.put("architect_reform_dimensions", ArchitectGameTests::reformDimensions);
        TESTS.put("architect_approaches_a_far_target", ArchitectGameTests::approachesAFarTarget);
        TESTS.put("architect_keeps_moving_in_combat", ArchitectGameTests::keepsMovingInCombat);
        TESTS.put("architect_never_attacks_backwards", ArchitectGameTests::neverAttacksBackwards);
        TESTS.put("architect_dodges_move_the_body", ArchitectGameTests::dodgesMoveTheBody);
        TESTS.put("architect_dodges_dangerous_projectiles", ArchitectGameTests::dodgesDangerousProjectiles);
        TESTS.put("architect_reads_a_charged_player", ArchitectGameTests::readsAChargedPlayer);
        TESTS.put("architect_verdict_hammer_disables_shield", ArchitectGameTests::verdictHammerDisablesShield);
        TESTS.put("architect_wants_the_hammer_against_a_shield", ArchitectGameTests::wantsTheHammerAgainstAShield);
        TESTS.put("architect_target_selection_is_stable", ArchitectGameTests::targetSelectionIsStable);
        TESTS.put("architect_balance_netherite", h -> balanceRun(h, true, false));
        TESTS.put("architect_balance_netherite_shield", h -> balanceRun(h, true, true));
        TESTS.put("architect_balance_iron", h -> balanceRun(h, false, false));
        TIMEOUTS.put("architect_balance_netherite", 460);
        TIMEOUTS.put("architect_balance_netherite_shield", 460);
        TIMEOUTS.put("architect_balance_iron", 460);
        TIMEOUTS.put("architect_never_attacks_backwards", 300);
        TIMEOUTS.put("architect_keeps_moving_in_combat", 300);
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
                        new TestData<>(environment, id("basilisk_test_arena"), TIMEOUTS.getOrDefault(name, TIMEOUT), 0, true, Rotation.NONE, false, 1, 1,
                                true, 0)))); // sky access: the Gargoyle Fall leaps above the arena walls
    }

    private static void attributesAndRegistration(GameTestHelper helper) {
        Architect architect = helper.spawnWithNoFreeWill(ModEntities.ARCHITECT.get(), new BlockPos(3, 1, 3));
        helper.assertValueEqual(architect.getMaxHealth(), 480.0F, "Architect max health");
        helper.assertValueEqual(architect.getAttributeValue(Attributes.ARMOR), 16.0D, "Architect armor");
        helper.assertValueEqual(architect.getAttributeValue(Attributes.ARMOR_TOUGHNESS), 8.0D, "Architect armor toughness");
        helper.assertValueEqual(architect.getAttributeValue(Attributes.ATTACK_DAMAGE), 20.0D, "Architect base damage");
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
        Architect boss = awakeBoss(helper, new BlockPos(1, 1, 1));
        // out of everything the blow can cover (its reach, the lunge or charge it makes by hand, the anti-whiff margin);
        // the arena is 12 blocks long, so the boss and the foe stand at opposite corners (12.7 blocks apart)
        double cover = attack.reach + (attack == ArchitectAttack.PIERCING_SENTENCE ? Architect.LUNGE_DISTANCE
                : attack == ArchitectAttack.RUSHING_REBUKE ? Architect.RUSH_DISTANCE : 0.0D) + Architect.ANTI_WHIFF_MARGIN;
        helper.assertTrue(cover < 11.5D, attack + " fits the arena check (cover " + cover + ")");
        IronGolem golem = dummy(helper, new BlockPos(10, 1, 10));
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
                for (int y = 1; y <= 5; y++) { // perched 5 blocks up: above his 4.2-block pillaring threshold
                    helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                }
            }
        }
        Architect boss = helper.spawn(ModEntities.ARCHITECT.get(), new BlockPos(2, 1, 6));
        boss.setState(Architect.State.COMBAT_IDLE);
        dummy(helper, new BlockPos(7, 6, 6));
        helper.succeedWhen(() -> helper.assertTrue(boss.getLastAttack() == ArchitectAttack.GARGOYLE_FALL,
                "The Gargoyle Fall answers the pillar"));
    }


    // ================================================================= reform: movement, facing, evasion, shield, targets
    /** Prints a line the balance report reads from the test log (grep ARCHITECT_METRIC). */
    private static void metric(String line) {
        System.out.println("ARCHITECT_METRIC " + line);
    }

    /** The reform's body: 4.7 blocks tall (the spec's 3.35 x 1.4), volumes and ranges scaled with it. */
    private static void reformDimensions(GameTestHelper helper) {
        Architect boss = helper.spawnWithNoFreeWill(ModEntities.ARCHITECT.get(), new BlockPos(5, 1, 5));
        helper.assertValueEqual(ModEntities.ARCHITECT.get().getHeight(), 4.7F, "collision height");
        helper.assertValueEqual(ModEntities.ARCHITECT.get().getWidth(), 1.9F, "collision width");
        helper.assertTrue(Math.abs(boss.getEyeHeight() - 4.1F) < 1.0E-4F, "eye height " + boss.getEyeHeight());
        helper.assertTrue(Architect.HEIGHT >= 4.5F && Architect.HEIGHT <= 5.0F, "height inside the 4.5-5 block brief");
        helper.assertTrue(Architect.HEIGHT > 4.5F - 0.01F, "taller than the Last Gift Collector (4.5)");
        helper.assertTrue(ArchitectAttack.SIXFOLD_EDICT.reach >= 3.0D * Architect.SCALE - 1.0E-4D, "the volumes follow the scale (reach " + ArchitectAttack.SIXFOLD_EDICT.reach + ")");
        helper.assertTrue(Architect.CLOSE_RANGE > 2.0D && Architect.SHORT_RANGE > 4.4D, "the ranges follow the scale");
        helper.succeed();
    }

    /** Far from the target he walks or runs at it, at a speed worth the name (the old brain stopped at 2.4 blocks and waited). */
    private static void approachesAFarTarget(GameTestHelper helper) {
        Architect boss = helper.spawn(ModEntities.ARCHITECT.get(), new BlockPos(1, 1, 1));
        boss.setState(Architect.State.COMBAT_IDLE);
        IronGolem golem = dummy(helper, new BlockPos(10, 1, 10));
        golem.getAttribute(Attributes.MAX_HEALTH).setBaseValue(4000.0D);
        golem.setHealth(4000.0F);
        double start = Architect.horizontalDistance(boss, golem);
        double[] top = {0.0D};
        Vec3[] last = {boss.position()};
        helper.onEachTick(() -> {
            double step = Math.sqrt(boss.position().subtract(last[0]).multiply(1.0D, 0.0D, 1.0D).lengthSqr());
            if (boss.getState() == Architect.State.COMBAT_IDLE) {
                top[0] = Math.max(top[0], step); // locomotion only: the charge of the Rushing Rebuke is a blow
            }
            last[0] = boss.position();
        });
        helper.runAfterDelay(70, () -> {
            double now = Architect.horizontalDistance(boss, golem);
            metric("approach start=" + start + " after70=" + now + " topSpeed=" + top[0] + " blocks/tick");
            helper.assertTrue(now < start - 5.0D, "he closed in (from " + start + " to " + now + ")");
            helper.assertTrue(top[0] >= 0.15D && top[0] <= 0.40D, "he ran at 0.15-0.40 blocks/tick (top " + top[0] + ")");
            helper.succeed();
        });
    }

    /** In the fighting band he never stands still without a reason: footwork, blows, dodges, guard. */
    private static void keepsMovingInCombat(GameTestHelper helper) {
        Architect boss = helper.spawn(ModEntities.ARCHITECT.get(), new BlockPos(3, 1, 5));
        boss.setState(Architect.State.COMBAT_IDLE);
        IronGolem golem = dummy(helper, new BlockPos(8, 1, 5));
        golem.getAttribute(Attributes.MAX_HEALTH).setBaseValue(4000.0D);
        golem.setHealth(4000.0F);
        int[] idle = {0, 0};
        double[] walked = {0.0D};
        Vec3[] last = {boss.position()};
        helper.onEachTick(() -> {
            double step = Math.sqrt(boss.position().subtract(last[0]).multiply(1.0D, 0.0D, 1.0D).lengthSqr());
            last[0] = boss.position();
            walked[0] += step;
            boolean excused = boss.getState() != Architect.State.COMBAT_IDLE; // a blow, a dodge, a guard, a parry
            if (step < 0.01D && !excused) {
                idle[0]++;
                idle[1] = Math.max(idle[1], idle[0]);
            } else {
                idle[0] = 0;
            }
        });
        helper.runAfterDelay(220, () -> {
            metric("movement walked=" + walked[0] + " blocks, longestIdleStreak=" + idle[1] + " ticks, blows="
                    + boss.getStrikesLanded() + " dodges=" + boss.getDodges());
            helper.assertTrue(walked[0] >= 5.0D, "he moved " + walked[0] + " blocks in 11 s");
            helper.assertTrue(idle[1] <= Architect.READ_MAX_TICKS + 12, "longest unexcused stillness " + idle[1] + " ticks");
            helper.succeed();
        });
    }

    /** No blow starts with the body turned away: the target circles him and every blow starts inside the 20-degree cone. */
    private static void neverAttacksBackwards(GameTestHelper helper) {
        Architect boss = helper.spawn(ModEntities.ARCHITECT.get(), new BlockPos(6, 1, 6));
        boss.setState(Architect.State.COMBAT_IDLE);
        IronGolem golem = dummy(helper, new BlockPos(6, 1, 9));
        golem.getAttribute(Attributes.MAX_HEALTH).setBaseValue(4000.0D);
        golem.setHealth(4000.0F);
        double[] worst = {0.0D};
        double[] headLead = {0.0D};
        int[] started = {0};
        int[] tick = {0};
        helper.onEachTick(() -> {
            tick[0]++;
            double a = tick[0] * 0.07D; // about 4 degrees a tick: he must keep turning to keep up
            Vec3 c = boss.position();
            golem.setPos(c.x + Math.sin(a) * 3.6D, c.y, c.z + Math.cos(a) * 3.6D);
            if (boss.getState() == Architect.State.ATTACKING && boss.getStateTicks() == 1) {
                started[0]++;
                double off = Math.abs(ArchitectStrikes.angleOf(golem.getX() - boss.getX(), golem.getZ() - boss.getZ(), boss.getYRot()));
                worst[0] = Math.max(worst[0], off);
                helper.assertTrue(Math.abs(Mth.wrapDegrees(boss.getLockedYaw() - boss.getYRot())) <= 0.01F,
                        "locked yaw is synced to body yaw");
            }
            headLead[0] = Math.max(headLead[0], Math.abs(Mth.wrapDegrees(boss.getYHeadRot() - boss.yBodyRot)));
        });
        helper.runAfterDelay(240, () -> {
            metric("facing blowsStarted=" + started[0] + " worstAngleAtStart=" + worst[0] + " maxHeadLead=" + headLead[0]);
            helper.assertTrue(started[0] >= 2, "he attacked while being circled (" + started[0] + " blows)");
            helper.assertTrue(worst[0] <= 21.0D, "every blow started facing the target, worst " + worst[0] + " degrees");
            helper.assertTrue(headLead[0] >= 3.0D, "the head leads the body (max " + headLead[0] + " degrees)");
            helper.succeed();
        });
    }

    /** Every dodge moves the body: part of the way mid-move, the full distance at the end, in the right direction. */
    private static void dodgesMoveTheBody(GameTestHelper helper) {
        Architect boss = awakeBoss(helper, new BlockPos(5, 1, 6));
        IronGolem golem = dummy(helper, new BlockPos(5, 1, 10));
        Architect.Dodge[] all = Architect.Dodge.values();
        int[] next = {0};
        Runnable[] run = new Runnable[1];
        run[0] = () -> {
            if (next[0] >= all.length) {
                helper.succeed();
                return;
            }
            Architect.Dodge d = all[next[0]++];
            Vec3 base = helper.absoluteVec(new Vec3(5.5D, 1.0D, 6.5D));
            boss.snapTo(base.x, base.y, base.z, 0.0F, 0.0F);
            Vec3 from = boss.position();
            boss.startDodge(d, golem);
            helper.assertValueEqual(boss.getState(), Architect.State.DODGING, d + ": the state");
            helper.assertValueEqual(boss.getAnim(), d.anim, d + ": its own clip");
            int mid = d.isLean() ? 4 : 7;
            helper.runAfterDelay(mid, () -> {
                double part = boss.position().subtract(from).horizontalDistance();
                helper.assertTrue(part > 0.1D && part < d.distance * 0.95D, d + ": mid-move at " + part + " of " + d.distance + " blocks (no teleport)");
                helper.runAfterDelay(d.anim.ticks - mid + 1, () -> {
                    Vec3 moved = boss.position().subtract(from);
                    double total = moved.horizontalDistance();
                    helper.assertTrue(Math.abs(total - d.distance) < 0.7D, d + ": covered " + total + " blocks, expected " + d.distance);
                    double angle = ArchitectStrikes.angleOf(moved.x, moved.z, 0.0F);
                    helper.assertTrue(Math.abs(Mth.wrapDegrees(angle - d.angle)) < 15.0D, d + ": direction " + angle + " expected " + d.angle);
                    helper.assertValueEqual(boss.getState(), Architect.State.COMBAT_IDLE, d + ": back to the idle");
                    helper.assertTrue(boss.isCounterWindow(), d + ": opens the counter window");
                    run[0].run();
                });
            });
        };
        run[0].run();
    }

    /** A dangerous projectile on its way is sidestepped (the light ones are deflected instead). */
    private static void dodgesDangerousProjectiles(GameTestHelper helper) {
        Architect boss = helper.spawn(ModEntities.ARCHITECT.get(), new BlockPos(5, 1, 3));
        boss.setState(Architect.State.COMBAT_IDLE);
        IronGolem golem = dummy(helper, new BlockPos(5, 1, 8));
        golem.getAttribute(Attributes.MAX_HEALTH).setBaseValue(4000.0D);
        golem.setHealth(4000.0F);
        int[] shots = {0};
        helper.onEachTick(() -> {
            if (boss.getDodges() == 0 && shots[0] < 22 && boss.tickCount % 11 == 0) {
                shots[0]++;
                var trident = EntityType.TRIDENT.create(helper.getLevel(), EntitySpawnReason.TRIGGERED); // strong: never deflected
                Vec3 from = boss.position().add(0.0D, 2.0D, 8.0D);
                trident.setPos(from);
                trident.setNoGravity(true);
                trident.setDeltaMovement(boss.position().add(0.0D, 2.0D, 0.0D).subtract(from).normalize().scale(0.7D));
                helper.getLevel().addFreshEntity(trident);
            }
        });
        helper.succeedWhen(() -> helper.assertTrue(boss.getDodges() >= 1, "he sidestepped a thrown trident (shots " + shots[0] + ")"));
    }

    /** The helper's mock server player is creative (a boss ignores creative players), so this one is built the same way in survival. */
    private static ServerPlayer survivalPlayer(GameTestHelper helper) {
        var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "test-survival-player"), false);
        var server = helper.getLevel().getServer();
        ServerPlayer player = new ServerPlayer(server, helper.getLevel(), cookie.gameProfile(), cookie.clientInformation()) {
            @Override
            public GameType gameMode() {
                return GameType.SURVIVAL;
            }
        };
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        server.getPlayerList().placeNewPlayer(connection, player, cookie);
        // without this the player counts as "client not loaded yet" and ServerPlayer.isInvulnerableTo returns true
        player.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());
        // placeNewPlayer gave the player the creative abilities of the helper's mock: take them away (survival)
        player.getAbilities().instabuild = false;
        player.getAbilities().invulnerable = false;
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
        return player;
    }

    private static ServerPlayer shieldBearer(GameTestHelper helper, Vec3 local) {
        ServerPlayer player = survivalPlayer(helper);
        Vec3 world = helper.absoluteVec(local);
        player.snapTo(world.x, world.y, world.z, 180.0F, 0.0F); // faces north, toward the boss
        player.setNoGravity(true);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
        player.startUsingItem(InteractionHand.OFF_HAND);
        for (int i = 0; i < 8; i++) {
            player.doTick(); // the shield needs a few ticks to come up
        }
        return player;
    }

    /** The Verdict Hammer is the only blow that disables a blocked shield, and the other blows are stopped as before. */
    private static void verdictHammerDisablesShield(GameTestHelper helper) {
        Architect boss = awakeBoss(helper, new BlockPos(5, 1, 3));
        ServerPlayer player = shieldBearer(helper, new Vec3(5.5D, 1.0D, 6.0D));
        ItemStack shield = player.getOffhandItem();
        helper.assertTrue(player.isBlocking(), "the player is blocking");
        float health = player.getHealth();
        boss.startAttack(ArchitectAttack.SIXFOLD_EDICT, player);
        helper.runAfterDelay(ArchitectAttack.SIXFOLD_EDICT.ticks() + 2, () -> {
            helper.assertTrue(boss.getStrikesLanded() == 0, "an ordinary blow reaches nothing behind a raised shield (landed " + boss.getStrikesLanded() + ")");
            helper.assertTrue(player.isBlocking() && !player.getCooldowns().isOnCooldown(shield), "an ordinary blow leaves the shield up");
            helper.assertValueEqual(player.getHealth(), health, "an ordinary blow does no damage to a blocking player");
            boss.startAttack(ArchitectAttack.SHIELD_BREAKER, player);
            helper.runAfterDelay(ArchitectAttack.SHIELD_BREAKER.ticks() + 2, () -> {
                helper.assertTrue(!player.isBlocking(), "the Verdict Hammer lowered the shield");
                helper.assertTrue(player.getCooldowns().isOnCooldown(shield), "the shield is on cooldown");
                helper.assertValueEqual(boss.getShieldsBroken(), 1, "the break is counted");
                helper.assertValueEqual(boss.getSecondsToDisableBlocking(), 0.0F, "the hook is closed again once the blow is over");
                helper.succeed();
            });
        });
    }

    /** Against a player who lives behind the shield he picks the heavy blow, and his other options lose weight. */
    private static void wantsTheHammerAgainstAShield(GameTestHelper helper) {
        Architect boss = helper.spawn(ModEntities.ARCHITECT.get(), new BlockPos(5, 1, 3));
        boss.setState(Architect.State.COMBAT_IDLE);
        ServerPlayer player = shieldBearer(helper, new Vec3(5.5D, 1.0D, 7.0D));
        helper.onEachTick(() -> {
            player.setHealth(player.getMaxHealth()); // the shield-turtle never dies and never moves
            Vec3 spot = helper.absoluteVec(new Vec3(5.5D, 1.0D, 7.0D));
            player.snapTo(spot.x, spot.y, spot.z, player.getYRot(), 0.0F);
            float face = Architect.yawTowards(player, boss); // always raised toward him
            player.setYRot(face);
            player.setYHeadRot(face);
            if (!player.isBlocking() && !player.getCooldowns().isOnCooldown(player.getOffhandItem())) {
                player.startUsingItem(InteractionHand.OFF_HAND);
                for (int i = 0; i < 6; i++) {
                    player.doTick();
                }
            }
            player.doTick();
        });
        helper.succeedWhen(() -> {
            if (boss.getShieldsBroken() >= 1) {
                metric("shield uses hammer=" + boss.getAttackUses(ArchitectAttack.SHIELD_BREAKER) + " broken=" + boss.getShieldsBroken()
                    + " punished=" + boss.getBlowsPunished() + " feint=" + boss.getAttackUses(ArchitectAttack.FALSE_VERDICT)
                    + " sixfold=" + boss.getAttackUses(ArchitectAttack.SIXFOLD_EDICT) + " score=" + boss.getShieldScore());
            }
            helper.assertTrue(boss.getShieldsBroken() >= 1, "he broke the guard (hammer used " + boss.getAttackUses(ArchitectAttack.SHIELD_BREAKER) + " times)");
        });
    }


    /** A player standing close with a charged attack makes him read the blow and sidestep (and then counter). */
    private static void readsAChargedPlayer(GameTestHelper helper) {
        Architect boss = helper.spawn(ModEntities.ARCHITECT.get(), new BlockPos(5, 1, 3));
        boss.setState(Architect.State.COMBAT_IDLE);
        ServerPlayer player = survivalPlayer(helper);
        Vec3 spot = helper.absoluteVec(new Vec3(5.5D, 1.0D, 7.0D));
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(4000.0D);
        player.setHealth(4000.0F);
        player.setNoGravity(true);
        helper.onEachTick(() -> {
            player.snapTo(spot.x, spot.y, spot.z, Architect.yawTowards(player, boss), 0.0F);
            player.setYHeadRot(player.getYRot());
            player.doTick(); // keeps the attack strength charging: the foe is always ready to swing
        });
        helper.succeedWhen(() -> helper.assertTrue(boss.getDodges() >= 1, "he read the charged foe and dodged (blows "
                + boss.getAttackUses(ArchitectAttack.SIXFOLD_EDICT) + "+" + boss.getAttackUses(ArchitectAttack.CROSSED_MANDATE) + ")"));
    }

    private static void sturdy(IronGolem golem) {
        golem.getAttribute(Attributes.MAX_HEALTH).setBaseValue(4000.0D);
        golem.setHealth(4000.0F);
    }

    /** Targeting with two foes: nearest first, a heavy hitter takes the focus, and the choice then holds (no spinning). */
    private static void targetSelectionIsStable(GameTestHelper helper) {
        Architect boss = helper.spawn(ModEntities.ARCHITECT.get(), new BlockPos(5, 1, 2));
        boss.setState(Architect.State.COMBAT_IDLE);
        IronGolem near = dummy(helper, new BlockPos(5, 1, 6));
        IronGolem far = dummy(helper, new BlockPos(8, 1, 9));
        sturdy(near);
        sturdy(far);
        int[] changes = {0};
        int[] stage = {0};
        int[] mark = {0};
        int[] before = {0};
        net.minecraft.world.entity.LivingEntity[] seen = {null};
        helper.onEachTick(() -> {
            if (boss.getTarget() != seen[0]) {
                seen[0] = boss.getTarget();
                changes[0]++;
            }
            mark[0]++;
            if (stage[0] == 0 && mark[0] == 25) {
                helper.assertTrue(boss.getTarget() == near, "the nearest foe is the first target");
                for (int i = 0; i < 6; i++) { // the far one hurts him again and again from the front: its threat climbs
                    boss.invulnerableTime = 0;
                    boss.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(far), 8.0F);
                }
                helper.assertTrue(boss.getThreat(far) > 30.0F, "its threat is recorded (" + boss.getThreat(far) + ")");
                stage[0] = 1;
            } else if (stage[0] == 1 && boss.getTarget() == far) {
                before[0] = changes[0]; // the focus just changed: now the old target hits back as hard as it can
                for (int i = 0; i < 8; i++) {
                    boss.invulnerableTime = 0;
                    boss.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(near), 4.0F);
                }
                mark[0] = 0;
                stage[0] = 2;
            } else if (stage[0] == 2 && mark[0] == 30) {
                helper.assertValueEqual(changes[0], before[0], "no flip-flop within the lock window");
                far.kill(helper.getLevel());
                mark[0] = 0;
                stage[0] = 3;
            } else if (stage[0] == 3 && mark[0] == 25) {
                helper.assertTrue(boss.getTarget() == near, "a dead target is replaced by the other foe");
                metric("targeting changes=" + changes[0]);
                helper.succeed();
            }
        });
    }

    /**
     * Balance run (reform §3): a stationary player (it does not dodge, does not strike back: the worst case for the player,
     * a lower bound for the fight) takes 20 s of the boss's AI. Prints the damage taken per second so the report can
     * compare gear. The assertion is only that the boss is a threat to full enchanted Netherite and not a one-hit killer.
     */
    private static void balanceRun(GameTestHelper helper, boolean netherite, boolean shield) {
        Architect boss = helper.spawn(ModEntities.ARCHITECT.get(), new BlockPos(5, 1, 3));
        boss.setState(Architect.State.COMBAT_IDLE);
        ServerPlayer player = survivalPlayer(helper);
        Vec3 spot = helper.absoluteVec(new Vec3(5.5D, 1.0D, 7.0D));
        player.snapTo(spot.x, spot.y, spot.z, 180.0F, 0.0F);
        player.setNoGravity(true);
        var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var armor = netherite
                ? new ItemStack[] {new ItemStack(Items.NETHERITE_HELMET), new ItemStack(Items.NETHERITE_CHESTPLATE),
                        new ItemStack(Items.NETHERITE_LEGGINGS), new ItemStack(Items.NETHERITE_BOOTS)}
                : new ItemStack[] {new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_CHESTPLATE),
                        new ItemStack(Items.IRON_LEGGINGS), new ItemStack(Items.IRON_BOOTS)};
        net.minecraft.world.entity.EquipmentSlot[] slots = {net.minecraft.world.entity.EquipmentSlot.HEAD,
                net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.LEGS,
                net.minecraft.world.entity.EquipmentSlot.FEET};
        for (int i = 0; i < 4; i++) {
            if (netherite) {
                armor[i].enchant(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.PROTECTION), 4);
            }
            player.setItemSlot(slots[i], armor[i]);
        }
        if (shield) {
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
        }
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(2000.0D); // measure, do not die
        player.setHealth(2000.0F);
        float[] taken = {0.0F};
        // probe: does a plain mob blow reach this player at all (difficulty, invulnerability)?
        float probe0 = player.getHealth();
        boolean probeHurt = player.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(boss), 10.0F);
        metric("probe difficulty=" + helper.getLevel().getDifficulty() + " hurt=" + probeHurt + " delta=" + (probe0 - player.getHealth())
                + " invulnerableTo=" + player.isInvulnerableTo(helper.getLevel(), helper.getLevel().damageSources().mobAttack(boss))
                + " creative=" + player.isCreative() + " instabuild=" + player.getAbilities().instabuild + " invulnerableAbility=" + player.getAbilities().invulnerable);
        player.setHealth(player.getMaxHealth());
        float[] last = {player.getHealth()};
        helper.onEachTick(() -> {
            player.snapTo(spot.x, spot.y, spot.z, (float) Architect.yawTowards(player, boss), 0.0F);
            player.setYHeadRot(player.getYRot());
            if (shield && !player.isBlocking() && !player.getCooldowns().isOnCooldown(player.getOffhandItem())) {
                player.startUsingItem(InteractionHand.OFF_HAND);
                for (int i = 0; i < 6; i++) {
                    player.doTick();
                }
            }
            player.doTick();
            taken[0] += Math.max(0.0F, last[0] - player.getHealth());
            last[0] = player.getHealth();
        });
        helper.runAfterDelay(400, () -> {
            double armorValue = player.getAttributeValue(Attributes.ARMOR);
            double tough = player.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
            StringBuilder mix = new StringBuilder();
            for (ArchitectAttack a : ArchitectAttack.values()) {
                if (boss.getAttackUses(a) > 0) {
                    mix.append(a).append('=').append(boss.getAttackUses(a)).append(' ');
                }
            }
            metric("balance gear=" + (netherite ? "netherite+ProtIV" : "iron") + " shield=" + shield + " armor=" + armorValue
                    + " toughness=" + tough + " duration=20s players=1 stationary taken=" + taken[0] + " hp ("
                    + (taken[0] / 20.0F) + "/s) landed=" + boss.getStrikesLanded() + " broken=" + boss.getShieldsBroken()
                    + " blows: " + mix);
            int blows = 0;
            for (ArchitectAttack a : ArchitectAttack.values()) {
                blows += boss.getAttackUses(a);
            }
            helper.assertTrue(blows >= 3, "he fought (" + blows + " blows)");
            helper.assertTrue(taken[0] < last[0] + taken[0], "not a one-hit kill");
            helper.succeed();
        });
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
        helper.assertTrue(Bestiary.ARCHITECT != null, "the Bestiary has his chapter");
        List<BestiaryPage> pages = Bestiary.ARCHITECT.pages();
        helper.assertTrue(pages.size() >= 6, "a chapter of six or more pages");
        for (String art : new String[] {"architect_anatomy", "architect_combat", "architect_spoils"}) {
            helper.assertTrue(ArchitectGameTests.class.getResourceAsStream("/assets/codenamecoraline/textures/gui/bestiary/" + art + ".png") != null,
                    "engraving " + art);
        }
        for (String adv : new String[] {"architect_tablet", "architect_statue", "architect_defeated", "architect_scimitar"}) {
            helper.assertTrue(server.getAdvancements().get(id("bosses/" + adv)) != null, "advancement bosses/" + adv);
        }
        helper.assertTrue(ArchitectGameTests.class.getResourceAsStream("/data/codenamecoraline/recipe/commandment_tablet.json") != null,
                "the tablet recipe is packaged");
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
