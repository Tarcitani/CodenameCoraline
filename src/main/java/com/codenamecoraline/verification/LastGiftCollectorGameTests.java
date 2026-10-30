package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.bestiary.Bestiary;
import com.codenamecoraline.client.collector.CollectorCinematicDirector;
import com.codenamecoraline.client.collector.CollectorSleighModel;
import com.codenamecoraline.client.collector.LastGiftCollectorAnimations;
import com.codenamecoraline.client.collector.LastGiftCollectorModel;
import com.codenamecoraline.entity.collector.CollectorSleigh;
import com.codenamecoraline.entity.collector.LastGiftCollector;
import com.codenamecoraline.item.SilentBellItem;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import com.codenamecoraline.entity.collector.CollectorGift;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Comprehensive server integration tests for the Collector of the Last Gift (`last_gift_collector`),
 * its sky-sleigh opening cinematic, Silent Bell ritual conditions, four choreographed attacks,
 * Phase II transformation, death collapse, loot table, advancement, and Bestiary entry.
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class LastGiftCollectorGameTests {
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
            Map.entry("collector_registration_attributes_and_animations",
                    LastGiftCollectorGameTests::registrationAttributesAndAnimations),
            Map.entry("collector_silent_bell_ritual_conditions",
                    LastGiftCollectorGameTests::silentBellRitualConditions),
            Map.entry("collector_intro_cinematic_and_sleigh",
                    LastGiftCollectorGameTests::introCinematicAndSleigh),
            Map.entry("collector_attack_winter_scythe",
                    LastGiftCollectorGameTests::attackWinterScythe),
            Map.entry("collector_attack_hollow_bell",
                    LastGiftCollectorGameTests::attackHollowBell),
            Map.entry("collector_attack_pawn_procession",
                    LastGiftCollectorGameTests::attackPawnProcession),
            Map.entry("collector_phase_transition_and_solstice_jump",
                    LastGiftCollectorGameTests::phaseTransitionAndSolsticeJump),
            Map.entry("collector_stomp_and_stage_gating",
                    LastGiftCollectorGameTests::stompAndStageGating),
            Map.entry("collector_late_stage_attacks_deal_damage",
                    LastGiftCollectorGameTests::lateStageAttacksDealDamage),
            Map.entry("collector_chases_and_never_swings_at_air",
                    LastGiftCollectorGameTests::chasesAndNeverSwingsAtAir),
            Map.entry("collector_greataxe_allied_mimics",
                    LastGiftCollectorGameTests::greataxeAlliedMimics),
            Map.entry("collector_allies_help_owner_against_anything",
                    LastGiftCollectorGameTests::alliesHelpOwner),
            Map.entry("collector_spell_bolts_with_trails",
                    LastGiftCollectorGameTests::spellBolts),
            Map.entry("collector_gift_rain_and_crescent",
                    LastGiftCollectorGameTests::giftRainAndCrescent),
            Map.entry("collector_snow_immunity_and_mimics",
                    LastGiftCollectorGameTests::snowImmunityAndMimics),
            Map.entry("collector_five_stages_and_fey_summons",
                    LastGiftCollectorGameTests::fiveStagesAndFeySummons),
            Map.entry("collector_death_loot_advancement_and_bestiary",
                    LastGiftCollectorGameTests::deathLootAdvancementAndBestiary)
    );

    private LastGiftCollectorGameTests() {}

    private static boolean enabled() {
        return Boolean.getBoolean("coraline.collectorTests");
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
        var environment = event.registerEnvironment(id("collector_test_environment"));
        TESTS.forEach((name, fn) -> event.registerTest(id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                        new TestData<>(environment, id("basilisk_test_arena"), 320, 0, true,
                                Rotation.NONE, false, 1, 1, true, 0))));
    }

    private static LivingEntity dummyTarget(GameTestHelper helper, BlockPos relativePos) {
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, relativePos);
        pig.setNoGravity(true);
        pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0D);
        pig.setHealth(100.0F);
        return pig;
    }

    private static void setBiome(GameTestHelper helper, BlockPos absPos, ResourceKey<Biome> key) {
        ServerLevel level = helper.getLevel();
        var biome = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(key);
        var sampler = level.getChunkSource().randomState().sampler();
        for (int dx = -16; dx <= 16; dx += 16) {
            for (int dz = -16; dz <= 16; dz += 16) {
                level.getChunkAt(absPos.offset(dx, 0, dz)).fillBiomesFromNoise((x, y, z, s) -> biome, sampler);
            }
        }
    }

    /** Test 1: Entity & item registries, attributes, dimensions, and model/animation baking. */
    private static void registrationAttributesAndAnimations(GameTestHelper helper) {
        LastGiftCollector boss = helper.spawnWithNoFreeWill(ModEntities.LAST_GIFT_COLLECTOR.get(), new BlockPos(4, 1, 4));
        helper.assertValueEqual(boss.getMaxHealth(), 420.0F, "Collector max health must be 420 HP");
        helper.assertValueEqual(boss.getAttributeValue(Attributes.ARMOR), 10.0D, "Collector armor must be 10");
        helper.assertValueEqual(boss.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.23D, "Phase I movement speed");
        helper.assertValueEqual(boss.getAttributeValue(Attributes.ATTACK_DAMAGE), 11.0D, "Base greataxe attack damage");
        helper.assertValueEqual(boss.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.9D, "Knockback resistance");
        helper.assertValueEqual(boss.getAttributeValue(Attributes.FOLLOW_RANGE), 40.0D, "Follow range");
        helper.assertValueEqual(boss.getBbHeight(), 4.5F, "Collision height 4.5 blocks");
        helper.assertValueEqual(boss.getBbWidth(), 1.85F, "Collision width 1.85 blocks");
        helper.assertTrue(!boss.removeWhenFarAway(256.0D), "Optional boss must never despawn by distance");

        // Bake both 3D models and all 18 animations
        var bossRoot = LastGiftCollectorModel.createBodyLayer().bakeRoot();
        new LastGiftCollectorModel(bossRoot);
        var sleighRoot = CollectorSleighModel.createBodyLayer().bakeRoot();
        new CollectorSleighModel(sleighRoot);

        // Regression: custom events once reused vanilla id 60 (death poof), restarting the intro animation on death.
        helper.assertTrue(LastGiftCollector.EVENT_INTRO_START > 60 && LastGiftCollector.EVENT_INTRO_START >= 100,
                "Custom events must stay clear of vanilla LivingEntity event ids");
        helper.assertTrue(LastGiftCollectorAnimations.IDLE.bake(bossRoot) != null, "IDLE bakes");
        helper.assertTrue(LastGiftCollectorAnimations.IDLE_ALTERNATE.bake(bossRoot) != null, "IDLE_ALTERNATE bakes");
        helper.assertTrue(LastGiftCollectorAnimations.WALK.bake(bossRoot) != null, "WALK bakes");
        helper.assertTrue(LastGiftCollectorAnimations.APPEAR.bake(bossRoot) != null, "APPEAR bakes");
        helper.assertTrue(LastGiftCollectorAnimations.JUMP_FROM_SLEIGH.bake(bossRoot) != null, "JUMP_FROM_SLEIGH bakes");
        helper.assertTrue(LastGiftCollectorAnimations.FALL.bake(bossRoot) != null, "FALL bakes");
        helper.assertTrue(LastGiftCollectorAnimations.LAND.bake(bossRoot) != null, "LAND bakes");
        helper.assertTrue(LastGiftCollectorAnimations.INTIMIDATE.bake(bossRoot) != null, "INTIMIDATE bakes");
        helper.assertTrue(LastGiftCollectorAnimations.ATTACK_SCYTHE_PREP.bake(bossRoot) != null, "ATTACK_SCYTHE_PREP bakes");
        helper.assertTrue(LastGiftCollectorAnimations.ATTACK_SCYTHE_EXEC.bake(bossRoot) != null, "ATTACK_SCYTHE_EXEC bakes");
        helper.assertTrue(LastGiftCollectorAnimations.ATTACK_HOLLOW_BELL_PREP.bake(bossRoot) != null, "ATTACK_HOLLOW_BELL_PREP bakes");
        helper.assertTrue(LastGiftCollectorAnimations.ATTACK_HOLLOW_BELL_IMPACT.bake(bossRoot) != null, "ATTACK_HOLLOW_BELL_IMPACT bakes");
        helper.assertTrue(LastGiftCollectorAnimations.ATTACK_PAWNS.bake(bossRoot) != null, "ATTACK_PAWNS bakes");
        helper.assertTrue(LastGiftCollectorAnimations.ATTACK_SOLSTICE_JUMP.bake(bossRoot) != null, "ATTACK_SOLSTICE_JUMP bakes");
        helper.assertTrue(LastGiftCollectorAnimations.RECOVERY.bake(bossRoot) != null, "RECOVERY bakes");
        helper.assertTrue(LastGiftCollectorAnimations.PHASE_TRANSITION.bake(bossRoot) != null, "PHASE_TRANSITION bakes");
        helper.assertTrue(LastGiftCollectorAnimations.HURT.bake(bossRoot) != null, "HURT bakes");
        helper.assertTrue(LastGiftCollectorAnimations.DEATH.bake(bossRoot) != null, "DEATH bakes");

        boss.discard();
        helper.succeed();
    }

    /** Test 2: Silent Bell ritual conditions (block, biome, night time, sky clearance, active duplicate). */
    private static void silentBellRitualConditions(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos relAltar = new BlockPos(4, 1, 4);
        // Elevate the ritual altar 100 blocks above the test grid (Y = +42 vs Y = -58)
        // so its 64-block active-Collector search radius never overlaps neighboring parallel test plots.
        BlockPos absAltar = helper.absolutePos(relAltar).above(100);
        ItemStack bellStack = new ItemStack(ModItems.SILENT_BELL.get(), 2);

        // 1. Wrong block (stone instead of packed_ice)
        level.setBlock(absAltar, Blocks.STONE.defaultBlockState(), 3);
        helper.assertValueEqual(SilentBellItem.trySummon(level, absAltar, null, bellStack),
                SilentBellItem.SummonResult.WRONG_BLOCK, "Rejects non-packed_ice block");
        helper.assertValueEqual(bellStack.getCount(), 2, "Does not consume Silent Bell on wrong block");

        // Place packed_ice
        level.setBlock(absAltar, Blocks.PACKED_ICE.defaultBlockState(), 3);

        // 2. Wrong biome (Plains)
        setBiome(helper, absAltar, Biomes.PLAINS);
        helper.setTime(18000L);
        helper.assertValueEqual(SilentBellItem.trySummon(level, absAltar, null, bellStack),
                SilentBellItem.SummonResult.WRONG_BIOME, "Rejects non-winter biome");
        helper.assertValueEqual(bellStack.getCount(), 2, "Does not consume Silent Bell on wrong biome");

        // 3. Daytime in Snowy Taiga
        setBiome(helper, absAltar, Biomes.SNOWY_TAIGA);
        helper.setTime(6000L);
        helper.assertValueEqual(SilentBellItem.trySummon(level, absAltar, null, bellStack),
                SilentBellItem.SummonResult.NOT_NIGHT, "Rejects daytime summon");
        helper.assertValueEqual(bellStack.getCount(), 2, "Does not consume Silent Bell during day");

        // 4. Obstructed sky above altar at night
        helper.setTime(18000L);
        level.setBlock(absAltar.above(6), Blocks.STONE.defaultBlockState(), 3);
        helper.assertValueEqual(SilentBellItem.trySummon(level, absAltar, null, bellStack),
                SilentBellItem.SummonResult.OBSTRUCTED_SKY, "Rejects obstructed sky above altar");
        helper.assertValueEqual(bellStack.getCount(), 2, "Does not consume Silent Bell when sky is obstructed");
        level.setBlock(absAltar.above(6), Blocks.AIR.defaultBlockState(), 3);

        // Verify Grove biome also passes condition evaluation
        setBiome(helper, absAltar, Biomes.GROVE);
        helper.assertValueEqual(SilentBellItem.evaluateConditions(level, absAltar),
                SilentBellItem.SummonResult.SUCCESS, "Grove biome is accepted");

        // 5. Valid ritual in Snowy Taiga at night with clear sky
        setBiome(helper, absAltar, Biomes.SNOWY_TAIGA);
        helper.assertValueEqual(SilentBellItem.trySummon(level, absAltar, null, bellStack),
                SilentBellItem.SummonResult.SUCCESS, "Summons Collector when all 5 ritual conditions are met");
        helper.assertValueEqual(bellStack.getCount(), 1, "Consumes exactly 1 Silent Bell on success");

        List<LastGiftCollector> summoned = level.getEntitiesOfClass(LastGiftCollector.class, new AABB(absAltar).inflate(32.0D));
        helper.assertValueEqual(summoned.size(), 1, "Exactly 1 Collector spawned");
        LastGiftCollector collector = summoned.getFirst();
        helper.assertValueEqual(collector.getBossState(), LastGiftCollector.BossState.INTRO_CINEMATIC,
                "Summoned Collector starts in INTRO_CINEMATIC");

        // 6. Rejects second summon within 64 blocks while first Collector is active
        helper.assertValueEqual(SilentBellItem.trySummon(level, absAltar, null, bellStack),
                SilentBellItem.SummonResult.ALREADY_ACTIVE, "Rejects duplicate summon within 64 blocks");
        helper.assertValueEqual(bellStack.getCount(), 1, "Does not consume Silent Bell when Collector is already active");

        collector.discard();
        level.setBlock(absAltar, Blocks.AIR.defaultBlockState(), 3);
        helper.succeed();
    }

    /** Test 3: 9.5-second sky-sleigh opening cinematic, single landing, and PHASE_ONE unlock. */
    private static void introCinematicAndSleigh(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos relCenter = new BlockPos(10, 1, 10);
        BlockPos absCenter = helper.absolutePos(relCenter);

        LastGiftCollector boss = helper.spawn(ModEntities.LAST_GIFT_COLLECTOR.get(), relCenter);
        boss.startIntroCinematic(level, absCenter, null);

        helper.assertValueEqual(boss.getBossState(), LastGiftCollector.BossState.INTRO_CINEMATIC, "Starts in INTRO_CINEMATIC");
        helper.assertValueEqual(boss.getIntroStage(), LastGiftCollector.IntroStage.INTRO_PRELUDE, "Starts in INTRO_PRELUDE");
        helper.assertTrue(!boss.getBossEvent().isVisible(), "Boss bar is hidden during intro cinematic");

        // Invulnerable to normal combat damage during intro cinematic
        boolean hurtDuringIntro = boss.hurtServer(level, level.damageSources().generic(), 20.0F);
        helper.assertTrue(!hurtDuringIntro, "Collector ignores normal damage during INTRO_CINEMATIC");
        helper.assertValueEqual(boss.getHealth(), 420.0F, "Health remains 420 HP during intro");

        // Advance through the 190-tick intro cinematic and verify each stage transition
        boolean sawLightning = false;
        boolean sawSleighPass = false;
        boolean sawJump = false;
        boolean sawFall = false;
        boolean sawLanding = false;
        boolean sawReveal = false;
        boolean sawSleighEntity = false;

        for (int i = 0; i < LastGiftCollector.INTRO_TOTAL_TICKS; i++) {
            boss.tick();
            switch (boss.getIntroStage()) {
                case INTRO_LIGHTNING -> {
                    sawLightning = true;
                    Entity sleighEntity = level.getEntity(boss.getSleighEntityId());
                    if (sleighEntity instanceof CollectorSleigh sleigh) {
                        sawSleighEntity = true;
                        sleigh.tick();
                        helper.assertTrue(!sleigh.shouldBeSaved(), "Sleigh actor is never persisted to disk");
                    }
                }
                case INTRO_SLEIGH_PASS -> sawSleighPass = true;
                case INTRO_JUMP -> sawJump = true;
                case INTRO_FALL -> sawFall = true;
                case INTRO_LANDING -> sawLanding = true;
                case INTRO_REVEAL -> sawReveal = true;
                default -> {}
            }
            // Verify pure camera director helpers across stages
            Vec3 focal = CollectorCinematicDirector.computeFocalPoint(level, boss, 0.5F);
            helper.assertTrue(focal != null, "Focal point is always non-null");
            float fovScale = CollectorCinematicDirector.computeIntroFovScale(boss.getIntroStage(), boss.getIntroTicks());
            helper.assertTrue(fovScale > 0.5F && fovScale <= 1.0F, "FOV scale stays within valid bounds");
        }

        helper.assertTrue(sawLightning, "Passed through INTRO_LIGHTNING");
        helper.assertTrue(sawSleighEntity, "Spawned temporary CollectorSleigh in the sky");
        helper.assertTrue(sawSleighPass, "Passed through INTRO_SLEIGH_PASS");
        helper.assertTrue(sawJump, "Passed through INTRO_JUMP");
        helper.assertTrue(sawFall, "Passed through INTRO_FALL");
        helper.assertTrue(sawLanding, "Passed through INTRO_LANDING");
        helper.assertTrue(sawReveal, "Passed through INTRO_REVEAL");

        helper.assertValueEqual(boss.getLandingCount(), 1, "Collector lands from the sky exactly once");
        helper.assertValueEqual(boss.getBossState(), LastGiftCollector.BossState.PHASE_ONE, "Unlocks PHASE_ONE at tick 190");
        helper.assertValueEqual(boss.getIntroStage(), LastGiftCollector.IntroStage.INTRO_COMPLETE, "Stage is INTRO_COMPLETE");
        helper.assertTrue(boss.getBossEvent().isVisible(), "Boss bar becomes visible when PHASE_ONE starts");
        helper.assertTrue(level.getEntity(boss.getSleighEntityId()) == null, "Temporary sleigh is cleaned up after intro");

        boss.discard();
        helper.succeed();
    }

    /** Test 4: Attack 1 — Foice do Inverno (frontal arc sweep, zero rear damage, recovery window). */
    private static void attackWinterScythe(GameTestHelper helper) {
        LastGiftCollector boss = helper.spawn(ModEntities.LAST_GIFT_COLLECTOR.get(), new BlockPos(8, 1, 8));
        boss.setYRot(0.0F);
        boss.setYHeadRot(0.0F);

        LivingEntity frontTarget = dummyTarget(helper, new BlockPos(8, 1, 10)); // +Z in front
        LivingEntity rearTarget = dummyTarget(helper, new BlockPos(8, 1, 6));   // -Z behind
        boss.setTarget(frontTarget);

        helper.assertTrue(boss.startAttack(LastGiftCollector.AttackType.WINTER_SCYTHE, frontTarget),
                "Starts WINTER_SCYTHE attack");
        helper.assertValueEqual(boss.getBossState(), LastGiftCollector.BossState.ATTACK_PREPARATION,
                "Enters ATTACK_PREPARATION");

        helper.runAfterDelay(LastGiftCollector.SCYTHE_PREP_TICKS + 4, () -> {
            helper.assertTrue(frontTarget.getHealth() < 100.0F,
                    "Front target takes damage from Foice do Inverno frontal sweep");
            helper.assertValueEqual(rearTarget.getHealth(), 100.0F,
                    "Rear target takes zero damage (frontal arc only, not 360 degrees)");
        });

        helper.runAfterDelay(LastGiftCollector.SCYTHE_PREP_TICKS + LastGiftCollector.SCYTHE_EXEC_TICKS_P1 + 3, () -> {
            helper.assertValueEqual(boss.getBossState(), LastGiftCollector.BossState.RECOVERY,
                    "Enters vulnerable RECOVERY state after Foice do Inverno");
            frontTarget.discard();
            rearTarget.discard();
            boss.discard();
            helper.succeed();
        });
    }

    /** Test 5: Attack 2 — Badalada Oca (3 linear frost waves in Phase I with safe gaps, zero block destruction). */
    private static void attackHollowBell(GameTestHelper helper) {
        LastGiftCollector boss = helper.spawn(ModEntities.LAST_GIFT_COLLECTOR.get(), new BlockPos(8, 1, 4));
        boss.setYRot(0.0F);
        boss.setYHeadRot(0.0F);

        LivingEntity centerTarget = dummyTarget(helper, new BlockPos(8, 1, 9)); // Directly on central ray (0 deg)
        LivingEntity gapTarget = dummyTarget(helper, new BlockPos(14, 1, 5));   // Outside the 3 ray corridors
        boss.setTarget(centerTarget);

        helper.assertTrue(boss.startAttack(LastGiftCollector.AttackType.HOLLOW_BELL, centerTarget),
                "Starts HOLLOW_BELL attack");
        helper.assertValueEqual(boss.getActiveFrostWaveDirs().size(), 3,
                "Phase I Badalada Oca prepares 3 linear frost rays");

        helper.runAfterDelay(LastGiftCollector.HOLLOW_BELL_PREP_TICKS + 12, () -> {
            helper.assertTrue(centerTarget.getHealth() < 100.0F,
                    "Target in central frost wave path takes 5 HP damage");
            helper.assertValueEqual(gapTarget.getHealth(), 100.0F,
                    "Target in safe gap outside frost rays takes zero damage");
            centerTarget.discard();
            gapTarget.discard();
            boss.discard();
            helper.succeed();
        });
    }

    /** Test 6: Attack 3 — Procissão dos Penhores (1.0s warning marks before root eruption). */
    private static void attackPawnProcession(GameTestHelper helper) {
        LastGiftCollector boss = helper.spawn(ModEntities.LAST_GIFT_COLLECTOR.get(), new BlockPos(8, 1, 4));
        LivingEntity zoneTarget = dummyTarget(helper, new BlockPos(8, 1, 9));
        boss.setTarget(zoneTarget);

        helper.assertTrue(boss.startAttack(LastGiftCollector.AttackType.PAWN_PROCESSION, zoneTarget),
                "Starts PAWN_PROCESSION attack");
        helper.assertValueEqual(boss.getActivePawnZones().size(), 3,
                "Procissão dos Penhores marks 3 deterministic ground zones");

        // During the 20-tick (1.0s) warning phase, no damage has occurred yet
        helper.runAfterDelay(10, () -> {
            helper.assertValueEqual(zoneTarget.getHealth(), 100.0F,
                    "Zero damage during the 1.0s ground warning telegraph");
        });

        // After 20 ticks, roots erupt and damage entities still standing inside the marked zones
        helper.runAfterDelay(LastGiftCollector.PAWN_PREP_TICKS + 3, () -> {
            helper.assertTrue(zoneTarget.getHealth() < 100.0F,
                    "Erupting frozen roots deal damage to targets remaining in the marked zone");
            zoneTarget.discard();
            boss.discard();
            helper.succeed();
        });
    }

    /** Test 7: Phase II transition at <= 50% HP and Phase II exclusive Salto do Solstício. */
    private static void phaseTransitionAndSolsticeJump(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LastGiftCollector boss = helper.spawn(ModEntities.LAST_GIFT_COLLECTOR.get(), new BlockPos(4, 1, 2));
        LivingEntity target = dummyTarget(helper, new BlockPos(4, 1, 6));
        boss.setTarget(target);

        // SOLSTICE_JUMP is locked in Phase I
        helper.assertTrue(!boss.startAttack(LastGiftCollector.AttackType.SOLSTICE_JUMP, target),
                "Salto do Solstício cannot be used in Phase I");

        // Deal damage to bring boss to <= 50% HP (210 HP of 420; 238 after the hit is stage 3, 60% or lower)
        boss.setHealth(250.0F);
        boss.hurtServer(level, level.damageSources().generic(), 12.0F);

        helper.assertValueEqual(boss.getBossState(), LastGiftCollector.BossState.PHASE_TRANSITION,
                "Dropping to <= 50% HP triggers PHASE_TRANSITION");
        helper.assertValueEqual(boss.getPhaseTransitionCount(), 1,
                "Phase transition triggers once");

        // Advance through the 60-tick (3.0s) phase transition
        for (int i = 0; i < LastGiftCollector.PHASE_TRANSITION_TICKS; i++) {
            boss.tick();
        }

        helper.assertValueEqual(boss.getBossState(), LastGiftCollector.BossState.PHASE_TWO,
                "Transitions to PHASE_TWO after 60 ticks");
        helper.assertTrue(boss.isPhaseTwo(), "isPhaseTwo() is true");
        helper.assertTrue(boss.isCageOpen(), "Reliquary cage remains open in Phase II");
        helper.assertValueEqual(boss.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.27D,
                "Movement speed increases to 0.27 in Phase II");

        // Further damage in Phase II never re-triggers PHASE_TRANSITION
        boss.hurtServer(level, level.damageSources().generic(), 5.0F);
        helper.assertValueEqual(boss.getPhaseTransitionCount(), 1,
                "Phase transition never triggers a second time");

        // Now start Phase II exclusive Salto do Solstício
        helper.assertTrue(boss.startAttack(LastGiftCollector.AttackType.SOLSTICE_JUMP, target),
                "Salto do Solstício starts in Phase II");

        // Mid-leap (after 26 prep ticks + 10 midair ticks): zero midair collision damage
        helper.runAfterDelay(LastGiftCollector.SOLSTICE_PREP_TICKS + 10, () -> {
            helper.assertValueEqual(target.getHealth(), 100.0F,
                    "Salto do Solstício deals zero damage while airborne");
        });

        // After landing (26 prep + 20 leap + GoalSelector tick margin): target takes impact damage and boss enters recovery
        helper.runAfterDelay(LastGiftCollector.SOLSTICE_PREP_TICKS + LastGiftCollector.SOLSTICE_EXEC_TICKS + 6, () -> {
            helper.assertTrue(target.getHealth() < 100.0F,
                    "Salto do Solstício landing deals impact damage at destination");
            helper.assertValueEqual(boss.getBossState(), LastGiftCollector.BossState.RECOVERY,
                    "Boss enters 1.5s vulnerable RECOVERY after Salto do Solstício");
            target.discard();
            boss.discard();
            helper.succeed();
        });
    }

    /** Test 7b: the Stomp hits everyone close, spares the far, and later-stage attacks stay locked in stage 1. */
    private static void stompAndStageGating(GameTestHelper helper) {
        LastGiftCollector boss = helper.spawn(ModEntities.LAST_GIFT_COLLECTOR.get(), new BlockPos(8, 1, 8));
        LivingEntity near = dummyTarget(helper, new BlockPos(8, 1, 11));
        LivingEntity far = dummyTarget(helper, new BlockPos(8, 1, 17));
        boss.setTarget(near);

        helper.assertValueEqual(boss.getStage(), 1, "Collector starts at stage 1");
        for (var locked : new LastGiftCollector.AttackType[] {
                LastGiftCollector.AttackType.WHIRLWIND, LastGiftCollector.AttackType.HYPNOTIC_GAZE,
                LastGiftCollector.AttackType.FEY_SUMMON, LastGiftCollector.AttackType.FROST_CONE,
                LastGiftCollector.AttackType.SOLSTICE_JUMP}) {
            helper.assertTrue(!boss.startAttack(locked, near), locked + " is locked at stage 1");
        }
        helper.assertTrue(boss.startAttack(LastGiftCollector.AttackType.STOMP, near), "Stomp is available at stage 1");

        helper.runAfterDelay(LastGiftCollector.STOMP_PREP_TICKS + 6, () -> {
            helper.assertTrue(near.getHealth() < 100.0F, "Stomp damages a target inside its radius");
            helper.assertValueEqual(far.getHealth(), 100.0F, "Stomp spares a target outside its radius");
            near.discard();
            far.discard();
            boss.discard();
            helper.succeed();
        });
    }

    /** Test 7d: Whirlwind and Frost Cone reach their victims and spare the rest; Hypnotic Gaze hurts a player who looks. */
    private static void lateStageAttacksDealDamage(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LastGiftCollector boss = helper.spawn(ModEntities.LAST_GIFT_COLLECTOR.get(), new BlockPos(8, 1, 8));
        boss.setYRot(0.0F);
        boss.setYHeadRot(0.0F);
        // Two thresholds crossed in one hit => a single transition straight to stage 4.
        boss.setHealth(150.0F);
        boss.hurtServer(level, level.damageSources().generic(), 5.0F);
        for (int i = 0; i < LastGiftCollector.PHASE_TRANSITION_TICKS; i++) {
            boss.tick();
        }
        helper.assertValueEqual(boss.getStage(), 4, "Boss is at stage 4");
        boss.cancelCombatAction();
        boss.setBossState(LastGiftCollector.BossState.PHASE_TWO);

        LivingEntity inFront = dummyTarget(helper, new BlockPos(8, 1, 13)); // +Z: inside the cone and the spin radius
        LivingEntity behind = dummyTarget(helper, new BlockPos(8, 1, 1));   // -Z: outside the cone, 7 blocks away
        boss.setTarget(inFront);
        helper.assertTrue(boss.startAttack(LastGiftCollector.AttackType.FROST_CONE, inFront), "Frost cone starts");

        int coneDone = LastGiftCollector.CONE_PREP_TICKS + LastGiftCollector.CONE_EXEC_TICKS;
        helper.runAfterDelay(LastGiftCollector.CONE_PREP_TICKS + 10, () -> {
            helper.assertTrue(inFront.getHealth() < 100.0F, "Frost cone damages a target inside the cone");
            helper.assertValueEqual(behind.getHealth(), 100.0F, "Frost cone spares a target behind the boss");
            boss.setTarget(null); // keep the AI from starting attacks of its own while the test drives the next one
        });
        helper.runAfterDelay(coneDone + 40, () -> {
            boss.cancelCombatAction();
            boss.setBossState(LastGiftCollector.BossState.PHASE_TWO);
            inFront.setHealth(100.0F);
            inFront.setPos(boss.getX(), boss.getY(), boss.getZ() + 4.0D); // the frozen pig may have drifted
            inFront.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            behind.setPos(boss.getX(), boss.getY(), boss.getZ() - 8.0D);
            behind.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            helper.assertTrue(boss.startAttack(LastGiftCollector.AttackType.WHIRLWIND, inFront), "Whirlwind starts");
        });
        helper.runAfterDelay(coneDone + 40 + LastGiftCollector.WHIRL_PREP_TICKS + 16, () -> {
            helper.assertTrue(inFront.getHealth() < 100.0F, "Whirlwind damages a target within its radius");
            helper.assertValueEqual(behind.getHealth(), 100.0F, "Whirlwind spares a target outside its radius");
            inFront.discard();
            behind.discard();
            boss.discard();
            helper.succeed();
        });
    }

    /** Puts the boss straight into stage 3 (comets, gift rain and the leap unlock there). */
    private static void toStageThree(ServerLevel level, LastGiftCollector boss) {
        boss.setHealth(235.0F);
        boss.hurtServer(level, level.damageSources().generic(), 5.0F); // 230/420 = 54.8% -> stage 3
        for (int i = 0; i < LastGiftCollector.PHASE_TRANSITION_TICKS; i++) {
            boss.tick();
        }
        boss.discardMinions(level); // the stage-3 summon would otherwise shove the dummy targets around
        boss.cancelCombatAction();
        boss.setBossState(LastGiftCollector.BossState.PHASE_TWO);
    }

    /** Test 7j: summoned mimics fight whatever their owner fights (an animal included) and never the owner. */
    private static void alliesHelpOwner(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        net.minecraft.server.level.ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        owner.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(8.5D, 1.0D, 8.5D)));
        int made = com.codenamecoraline.item.RenegadeGreataxeItem.summonAllies(level, owner, 3);
        helper.assertValueEqual(made, 3, "The greataxe call summons 3 allied mimics");
        var allies = level.getEntitiesOfClass(com.codenamecoraline.entity.collector.GiftMimic.class,
                owner.getBoundingBox().inflate(12.0D), m -> m.isAllied());
        helper.assertTrue(allies.size() >= 3, "Allied mimics exist");

        LivingEntity cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(12, 1, 8));
        helper.assertTrue(allies.get(0).canAttack(cow), "Allies may fight an animal");
        helper.assertTrue(!allies.get(0).canAttack(owner), "Allies never fight their owner");
        owner.attack(cow);

        helper.runAfterDelay(40, () -> {
            boolean anyTargets = allies.stream().anyMatch(m -> m.getTarget() == cow) || cow.getHealth() < cow.getMaxHealth();
            helper.assertTrue(anyTargets, "Allied mimics join the owner's fight against the animal");
            helper.assertTrue(allies.stream().noneMatch(m -> m.getTarget() == owner), "No ally ever targets the owner");
            allies.forEach(m -> m.discard());
            cow.discard();
            helper.succeed();
        });
    }

    /** Test 7i: the Renegade's Greataxe wears out when it calls gifts; its mimics fight for the owner, not against. */
    private static void greataxeAlliedMimics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack axe = new ItemStack(ModItems.RENEGADE_GREATAXE.get());
        helper.assertValueEqual(axe.getMaxDamage(), 2200, "The greataxe has 2200 durability");
        helper.assertTrue(axe.has(net.minecraft.core.component.DataComponents.LORE), "The greataxe explains its power in its tooltip");
        helper.assertTrue(com.codenamecoraline.item.RenegadeGreataxeItem.SUMMON_DURABILITY_COST > 0
                        && com.codenamecoraline.item.RenegadeGreataxeItem.SUMMON_COUNT == 3,
                "Each call summons 3 mimics and costs durability");

        LastGiftCollector boss = helper.spawnWithNoFreeWill(ModEntities.LAST_GIFT_COLLECTOR.get(), new BlockPos(2, 1, 2));
        var ally = helper.spawnWithNoFreeWill(ModEntities.GIFT_MIMIC.get(), new BlockPos(8, 1, 8));
        var hostile = helper.spawnWithNoFreeWill(ModEntities.GIFT_MIMIC.get(), new BlockPos(14, 1, 14));
        ally.setOwner(java.util.UUID.randomUUID());
        helper.assertTrue(ally.isAllied() && !hostile.isAllied(), "Owner marks a mimic as allied");

        LivingEntity zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(9, 1, 8));
        LivingEntity pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(7, 1, 8));
        helper.assertTrue(ally.canAttack(zombie), "An allied mimic fights hostile mobs");
        helper.assertTrue(ally.canAttack(pig), "An allied mimic may fight any animal its owner is fighting");
        helper.assertTrue(ally.canAttack(boss), "An allied mimic fights the Collector");
        helper.assertTrue(ally.canAttack(hostile) && hostile.canAttack(ally), "Allied and hostile mimics fight each other");
        helper.assertTrue(boss.canAttack(ally) && !boss.canAttack(hostile), "The Collector fights allied mimics but not his own");
        helper.assertTrue(!hostile.canAttack(boss), "A hostile mimic never turns on the Collector");

        float zombieBefore = zombie.getHealth();
        float pigBefore = pig.getHealth();
        ally.burst(level);
        helper.assertTrue(ally.isRemoved(), "The ally bursts when called");
        helper.assertTrue(zombie.getHealth() < zombieBefore || !zombie.isAlive(), "The burst hurts hostile mobs");
        helper.assertValueEqual(pig.getHealth(), pigBefore, "The burst spares animals nobody is fighting");

        zombie.discard();
        pig.discard();
        hostile.discard();
        boss.discard();
        helper.succeed();
    }

    /** Test 7h: he walks after a distant target, and a wind-up whose target slipped away is cancelled, not swung. */
    private static void chasesAndNeverSwingsAtAir(GameTestHelper helper) {
        LastGiftCollector boss = helper.spawn(ModEntities.LAST_GIFT_COLLECTOR.get(), new BlockPos(8, 1, 1));
        LivingEntity runner = dummyTarget(helper, new BlockPos(8, 1, 14));
        boss.setTarget(runner);
        double startDist = boss.distanceTo(runner);

        helper.runAfterDelay(60, () -> {
            helper.assertTrue(boss.distanceTo(runner) < startDist - 5.0D || runner.getHealth() < 100.0F,
                    "The Collector closes in on a distant target instead of standing still");

            // Now a wind-up whose target leaves: it must be cancelled before any blow lands.
            runner.setHealth(100.0F);
            boss.setTarget(null);
            boss.cancelCombatAction();
            boss.setBossState(LastGiftCollector.BossState.PHASE_ONE);
            runner.setPos(boss.getX(), boss.getY(), boss.getZ() + 2.5D);
            runner.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            helper.assertTrue(boss.startAttack(LastGiftCollector.AttackType.WINTER_SCYTHE, runner), "Cleave starts at close range");
            helper.runAfterDelay(8, () -> runner.setPos(boss.getX(), boss.getY() + 6.0D, boss.getZ() + 14.0D)); // gone, and above
            helper.runAfterDelay(LastGiftCollector.SCYTHE_PREP_TICKS + 6, () -> {
                helper.assertTrue(boss.getBossState() != LastGiftCollector.BossState.ATTACK_EXECUTION
                                && boss.getActiveAttack() != LastGiftCollector.AttackType.WINTER_SCYTHE,
                        "A wind-up with no target left is cancelled instead of swinging at the air");
                helper.assertValueEqual(runner.getHealth(), 100.0F, "Nothing was hit");
                runner.discard();
                boss.discard();
                helper.succeed();
            });
        });
    }

    /** Test 7e: the comet barrage launches homing bolts that leave the boss, fly with trails and hit the target. */
    private static void spellBolts(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LastGiftCollector boss = helper.spawn(ModEntities.LAST_GIFT_COLLECTOR.get(), new BlockPos(8, 1, 5));
        boss.setYRot(0.0F);
        boss.setYHeadRot(0.0F);
        toStageThree(level, boss);
        LivingEntity target = dummyTarget(helper, new BlockPos(8, 1, 13));
        boss.setTarget(target);
        helper.assertValueEqual(boss.getStage(), 3, "Boss is at stage 3");
        helper.assertTrue(boss.startAttack(LastGiftCollector.AttackType.COMET_BARRAGE, target), "Comet barrage starts at stage 3");

        helper.runAfterDelay(LastGiftCollector.COMET_PREP_TICKS + 5, () ->
                helper.assertTrue(boss.getActiveBoltCount() > 0, "Comets are in flight after the cast"));
        helper.runAfterDelay(LastGiftCollector.COMET_PREP_TICKS + LastGiftCollector.COMET_EXEC_TICKS + 30, () -> {
            helper.assertTrue(target.getHealth() < 100.0F, "A homing comet reaches and damages the target");
            target.discard();
            boss.discard();
            helper.succeed();
        });
    }

    /** Test 7f: gift rain marks the ground first, then presents fall and burst; the crescent travels and hits. */
    private static void giftRainAndCrescent(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LastGiftCollector boss = helper.spawn(ModEntities.LAST_GIFT_COLLECTOR.get(), new BlockPos(8, 1, 5));
        boss.setYRot(0.0F);
        boss.setYHeadRot(0.0F);
        toStageThree(level, boss);
        LivingEntity target = dummyTarget(helper, new BlockPos(8, 1, 13));
        boss.setTarget(target);
        helper.assertTrue(boss.startAttack(LastGiftCollector.AttackType.GIFT_RAIN, target), "Gift rain starts at stage 3");

        helper.runAfterDelay(LastGiftCollector.RAIN_PREP_TICKS + 4, () -> {
            helper.assertTrue(boss.getPendingRainCount() > 0, "The ground is marked before any present falls");
            helper.assertValueEqual(target.getHealth(), 100.0F, "No damage during the warning");
        });
        int landed = LastGiftCollector.RAIN_PREP_TICKS + 1 + LastGiftCollector.RAIN_WARNING_TICKS + 60; // the last staggered present has landed by then (they would grant the dummy hurt immunity)
        helper.runAfterDelay(landed, () -> {
            helper.assertTrue(target.getHealth() < 100.0F, "A falling present bursts on the marked spot");
            // Next: the axe's crescent wave
            boss.cancelCombatAction();
            boss.setBossState(LastGiftCollector.BossState.PHASE_TWO);
            target.setHealth(100.0F);
            target.setPos(boss.getX(), boss.getY(), boss.getZ() + 8.0D);
            target.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            boss.setYRot(0.0F);
            helper.assertTrue(boss.startAttack(LastGiftCollector.AttackType.CRESCENT_WAVE, target), "Crescent wave starts");
        });
        helper.runAfterDelay(landed + LastGiftCollector.CRESCENT_PREP_TICKS + 3 + 18, () -> {
            helper.assertTrue(target.getHealth() < 100.0F, "The crescent of frost travels and hits the target");
            target.discard();
            boss.discard();
            helper.succeed();
        });
    }

    /** Test 7g: no freezing or powder snow trouble; Gift Mimics never fight their master and burst when due. */
    private static void snowImmunityAndMimics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LastGiftCollector boss = helper.spawn(ModEntities.LAST_GIFT_COLLECTOR.get(), new BlockPos(8, 1, 3));
        helper.assertTrue(!boss.canFreeze(), "The Collector cannot freeze");
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(ModEntities.LAST_GIFT_COLLECTOR.get()).is(net.minecraft.tags.EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS),
                "The Collector walks on powder snow");
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(ModEntities.LAST_GIFT_COLLECTOR.get()).is(net.minecraft.tags.EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES),
                "The Collector is freeze immune");
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(ModEntities.GIFT_MIMIC.get()).is(net.minecraft.tags.EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS),
                "Gift Mimics walk on powder snow");

        helper.assertTrue(boss.summonFey(level, 2) > 0, "The Collector can call Gift Mimics");
        var mimics = level.getEntitiesOfClass(com.codenamecoraline.entity.collector.GiftMimic.class,
                boss.getBoundingBox().inflate(20.0D));
        helper.assertTrue(!mimics.isEmpty(), "Mimics exist after the summon");
        var mimic = mimics.get(0);
        helper.assertTrue(!mimic.canAttack(boss), "A mimic never targets the Collector");
        helper.assertTrue(!boss.canAttack(mimic), "The Collector never targets his own mimics");
        float before = mimic.getHealth();
        helper.assertTrue(!mimic.hurtServer(level, level.damageSources().mobAttack(boss), 20.0F),
                "The Collector's attacks never hurt his mimics");
        helper.assertValueEqual(mimic.getHealth(), before, "Mimic health is untouched by its master");

        LivingEntity victim = dummyTarget(helper, new BlockPos(8, 1, 3));
        victim.setPos(mimic.getX(), mimic.getY(), mimic.getZ());
        mimic.burst(level);
        helper.assertTrue(mimic.isRemoved(), "A bursting mimic is gone");
        boss.discardMinions(level);
        victim.discard();
        boss.discard();
        helper.succeed();
    }

    /** Test 7c: stages begin at 80/60/40/20% HP, each entered once; fey summons are capped; frenzy is stage 5. */
    private static void fiveStagesAndFeySummons(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LastGiftCollector boss = helper.spawn(ModEntities.LAST_GIFT_COLLECTOR.get(), new BlockPos(8, 1, 8));
        LivingEntity target = dummyTarget(helper, new BlockPos(8, 1, 14));
        boss.setTarget(target);

        boss.setHealth(330.0F);
        boss.hurtServer(level, level.damageSources().generic(), 10.0F); // 320/420 = 76% -> stage 2
        helper.assertValueEqual(boss.getBossState(), LastGiftCollector.BossState.PHASE_TRANSITION, "Below 80% begins a transition");
        for (int i = 0; i < LastGiftCollector.PHASE_TRANSITION_TICKS; i++) {
            boss.tick();
        }
        helper.assertValueEqual(boss.getStage(), 2, "Stage 2 after the first transition");
        helper.assertTrue(!boss.isPhaseTwo(), "Stage 2 is not yet the late phase");
        helper.assertTrue(boss.startAttack(LastGiftCollector.AttackType.WHIRLWIND, target), "Whirlwind unlocks at stage 2");
        boss.cancelCombatAction();
        boss.setBossState(LastGiftCollector.BossState.PHASE_ONE);

        boss.setHealth(160.0F);
        boss.hurtServer(level, level.damageSources().generic(), 10.0F); // 150/420 = 36% -> stage 4 directly
        for (int i = 0; i < LastGiftCollector.PHASE_TRANSITION_TICKS; i++) {
            boss.tick();
        }
        helper.assertValueEqual(boss.getStage(), 4, "Dropping past two thresholds lands on stage 4 in one transition");
        helper.assertValueEqual(boss.getPhaseTransitionCount(), 2, "Exactly one transition per threshold crossing");
        helper.assertTrue(boss.isPhaseTwo(), "Stage 4 keeps the late-phase kit");
        boss.cancelCombatAction();
        boss.setBossState(LastGiftCollector.BossState.PHASE_TWO);
        helper.assertTrue(boss.startAttack(LastGiftCollector.AttackType.FROST_CONE, target), "Frost cone unlocks at stage 4");
        boss.cancelCombatAction();
        boss.setBossState(LastGiftCollector.BossState.PHASE_TWO);

        int first = boss.summonFey(level, 3);
        helper.assertTrue(first >= 1 && first <= 3, "First fey summon brings redcaps");
        helper.assertTrue(first + boss.summonFey(level, 3) <= LastGiftCollector.MAX_MINIONS, "Gift Mimics never exceed four alive");

        boss.setHealth(70.0F);
        boss.hurtServer(level, level.damageSources().generic(), 5.0F); // 65/420 = 15% -> stage 5
        for (int i = 0; i < LastGiftCollector.PHASE_TRANSITION_TICKS; i++) {
            boss.tick();
        }
        helper.assertValueEqual(boss.getStage(), 5, "Stage 5 (frenzy) below 20% HP");
        helper.assertValueEqual(boss.getAttributeValue(Attributes.MOVEMENT_SPEED), LastGiftCollector.STAGE_FIVE_SPEED,
                "Frenzied stage moves faster");

        boss.setHealth(45.0F);
        boss.hurtServer(level, level.damageSources().generic(), 5.0F); // 40/420 = 9.5% -> stage 6, Rage
        for (int i = 0; i < LastGiftCollector.PHASE_TRANSITION_TICKS; i++) {
            boss.tick();
        }
        helper.assertValueEqual(boss.getStage(), 6, "Stage 6 (Rage) below 10% HP");
        helper.assertValueEqual(boss.getAttributeValue(Attributes.MOVEMENT_SPEED), LastGiftCollector.RAGE_SPEED,
                "Rage amplifies movement speed");
        helper.assertValueEqual(boss.getAttributeValue(Attributes.ARMOR),
                LastGiftCollector.ARMOR + LastGiftCollector.RAGE_ARMOR_BONUS, "Rage amplifies armor");
        helper.assertValueEqual(boss.getAttributeValue(Attributes.ATTACK_DAMAGE),
                (double) (LastGiftCollector.SCYTHE_DAMAGE * LastGiftCollector.RAGE_DAMAGE_MULTIPLIER),
                "Rage amplifies attack damage");

        boss.discardMinions(level);
        target.discard();
        boss.discard();
        helper.succeed();
    }

    /** Test 8: Death cinematic cancels active attacks, drops loot once, and verifies Advancement & Bestiary. */
    private static void deathLootAdvancementAndBestiary(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos relPos = new BlockPos(8, 1, 8);
        BlockPos absPos = helper.absolutePos(relPos);

        LastGiftCollector boss = helper.spawn(ModEntities.LAST_GIFT_COLLECTOR.get(), relPos);
        LivingEntity target = dummyTarget(helper, new BlockPos(8, 1, 10));
        boss.setTarget(target);

        // Start an attack, then deal lethal damage before it executes
        boss.startAttack(LastGiftCollector.AttackType.WINTER_SCYTHE, target);
        boss.hurtServer(level, level.damageSources().generic(), 500.0F);

        helper.assertValueEqual(boss.getBossState(), LastGiftCollector.BossState.DEATH_CINEMATIC,
                "Enters DEATH_CINEMATIC upon lethal damage");
        helper.assertValueEqual(boss.getActiveAttack(), LastGiftCollector.AttackType.NONE,
                "Cancels active combat attack immediately on death");
        helper.assertTrue(!boss.areEyesGlowing(), "Eye glow extinguishes during death collapse");
        helper.assertTrue(!boss.getBossEvent().isVisible(), "Boss bar hides on death");
        helper.assertValueEqual(boss.getDeathAwardCount(), 1, "Death rewards granted exactly once");

        // The loot no longer scatters on the ground: it waits in a chest inside the Collector's gift.
        AABB dropBox = new AABB(absPos).inflate(6.0D);
        helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, dropBox).isEmpty(),
                "No loose drops while the death cinematic runs");

        // Advance through the 70-tick (3.5s) death collapse and verify clean removal & zero residual damage
        for (int i = 0; i < LastGiftCollector.DEATH_CINEMATIC_TICKS + 2; i++) {
            boss.tick();
        }
        helper.assertValueEqual(target.getHealth(), 100.0F,
                "Target takes zero residual damage after boss dies");
        helper.assertValueEqual(boss.getBossState(), LastGiftCollector.BossState.DEAD,
                "Reaches DEAD state at end of 70-tick death cinematic");
        helper.assertTrue(boss.isRemoved(), "Boss entity is removed cleanly after death cinematic");
        var spirits = level.getEntitiesOfClass(com.codenamecoraline.entity.spirit.BossSpirit.class, dropBox.inflate(4.0D),
                s -> s.getType() == ModEntities.LAST_GIFT_COLLECTOR_SPIRIT.get());
        helper.assertValueEqual(spirits.size(), 1, "His spirit rises from the body, once");
        helper.assertTrue(!spirits.getFirst().isPickable(), "The spirit is only a vision: it cannot be hit");

        // The gift: a chest with the whole haul (always the greataxe) and a wrapping present entity on top.
        ChestBlockEntity chest = null;
        for (BlockPos p : BlockPos.betweenClosed(absPos.offset(-4, -3, -4), absPos.offset(4, 3, 4))) {
            if (level.getBlockEntity(p) instanceof ChestBlockEntity found) {
                chest = found;
                break;
            }
        }
        helper.assertTrue(chest != null, "A loot chest appears where the Collector fell");
        int bells = 0;
        int threads = 0;
        int axes = 0;
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (stack.is(ModItems.CRACKED_BELL.get())) bells += stack.getCount();
            if (stack.is(ModItems.FROSTBOUND_THREAD.get())) threads += stack.getCount();
            if (stack.is(ModItems.RENEGADE_GREATAXE.get())) axes += stack.getCount();
        }
        helper.assertValueEqual(bells, 1, "The chest holds 1 guaranteed Cracked Bell");
        helper.assertTrue(threads >= 2 && threads <= 4, "The chest holds 2 to 4 Frostbound Threads (got " + threads + ")");
        helper.assertValueEqual(axes, 1, "The chest always holds the Renegade's Greataxe");
        helper.assertTrue(!level.getEntitiesOfClass(CollectorGift.class, dropBox).isEmpty(),
                "A Christmas gift wraps the chest");
        helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, dropBox).isEmpty(),
                "Nothing is dropped loose when the gift is placed");

        // Verify spawn egg, advancement, and Bestiary entry
        ItemStack eggStack = new ItemStack(ModItems.LAST_GIFT_COLLECTOR_SPAWN_EGG.get());
        helper.assertTrue(eggStack.getItem() instanceof SpawnEggItem, "Spawn egg is a SpawnEggItem");
        SpawnEggItem egg = (SpawnEggItem) eggStack.getItem();
        helper.assertValueEqual(egg.getType(eggStack), ModEntities.LAST_GIFT_COLLECTOR.get(),
                "Spawn egg spawns codenamecoraline:last_gift_collector");

        for (String story : new String[] {"silent_bell", "the_summoning", "the_final_delivery", "the_cracked_bell",
                "the_lost_axe", "the_solstice_seal"}) {
            helper.assertTrue(level.getServer().getAdvancements().get(id("bosses/" + story)) != null,
                    "Story advancement codenamecoraline:bosses/" + story + " is loaded");
        }

        helper.assertTrue(Bestiary.ENTRIES.contains(Bestiary.LAST_GIFT_COLLECTOR),
                "Bestiary contains LAST_GIFT_COLLECTOR");
        helper.assertValueEqual(Bestiary.LAST_GIFT_COLLECTOR.pages().size(), 6,
                "Bestiary entry has 6 pages");
        helper.assertTrue(Bestiary.ENTRIES.contains(Bestiary.REDCAP)
                        && Bestiary.ENTRIES.contains(Bestiary.BASILISK)
                        && Bestiary.ENTRIES.contains(Bestiary.LITHOPHAGE),
                "All existing Bestiary entries are preserved");

        target.discard();
        helper.runAfterDelay(CollectorGift.LIFETIME_TICKS + 4, () -> {
            helper.assertTrue(level.getEntitiesOfClass(CollectorGift.class, dropBox).isEmpty(),
                    "The gift pops open and disappears, leaving the chest");
            helper.assertTrue(level.getBlockEntity(finalChestPos(level, absPos)) instanceof ChestBlockEntity,
                    "The chest remains after the gift is gone");
            helper.succeed();
        });
    }

    private static BlockPos finalChestPos(ServerLevel level, BlockPos around) {
        for (BlockPos p : BlockPos.betweenClosed(around.offset(-4, -3, -4), around.offset(4, 3, 4))) {
            if (level.getBlockEntity(p) instanceof ChestBlockEntity) {
                return p.immutable();
            }
        }
        return around;
    }
}
