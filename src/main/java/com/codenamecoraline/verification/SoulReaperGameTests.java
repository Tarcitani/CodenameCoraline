package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.bestiary.Bestiary;
import com.codenamecoraline.client.reaper.OmenHareModel;
import com.codenamecoraline.client.reaper.OmenHareRenderState;
import com.codenamecoraline.client.reaper.SoulReaperModel;
import com.codenamecoraline.client.reaper.SoulReaperRenderState;
import com.codenamecoraline.client.reaper.WanderingSoulWispModel;
import com.codenamecoraline.client.reaper.WanderingSoulWispRenderState;
import com.codenamecoraline.entity.reaper.OmenHare;
import com.codenamecoraline.entity.reaper.SoulReaper;
import com.codenamecoraline.entity.reaper.SoulReaperAttack;
import com.codenamecoraline.entity.reaper.WanderingSoulWisp;
import com.codenamecoraline.entity.spirit.BossSpirit;
import com.codenamecoraline.item.FrostSpiralBroochItem;
import com.codenamecoraline.item.SoulReaperScytheItem;
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
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Server integration tests for the Soul Reaper, Omen Hare, and Wandering Soul Wisp (Slices 1–3). */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class SoulReaperGameTests {
    private static final int FLOOR_Y = 2;

    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
            Map.entry("soul_reaper_registration_and_attributes", SoulReaperGameTests::registrationAndAttributes),
            Map.entry("soul_reaper_immunities_and_anti_burst", SoulReaperGameTests::immunitiesAndAntiBurst),
            Map.entry("omen_hare_spawn_rules_and_wisp_binding", SoulReaperGameTests::omenHareSpawnRulesAndWispBinding),
            Map.entry("soul_reaper_models_and_animations_baking", SoulReaperGameTests::modelsAndAnimationsBaking),
            Map.entry("omen_hare_metamorphosis_and_awakening", SoulReaperGameTests::omenHareMetamorphosisAndAwakening),
            Map.entry("soul_reaper_desolation_aura_and_frost", SoulReaperGameTests::desolationAuraAndFrost),
            Map.entry("soul_reaper_stages_healing_and_scaling", SoulReaperGameTests::stagesHealingAndScaling),
            Map.entry("soul_reaper_original_attacks_and_anti_whiff", SoulReaperGameTests::originalAttacksAndAntiWhiff),
            Map.entry("soul_reaper_frontal_attacks_and_exclusive_f4", SoulReaperGameTests::frontalAttacksAndExclusiveF4),
            Map.entry("soul_reaper_seven_anti_instakill_rules", SoulReaperGameTests::sevenAntiInstakillRules),
            Map.entry("soul_reaper_provenance_nullification_and_taunt", SoulReaperGameTests::provenanceNullificationAndTaunt),
            Map.entry("soul_reaper_items_scythe_brooch_and_spirit", SoulReaperGameTests::itemsScytheBroochAndSpirit));

    private SoulReaperGameTests() {}

    private static boolean enabled() {
        return Boolean.getBoolean("coraline.soulReaperTests");
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
        var environment = event.registerEnvironment(id("soul_reaper_test_environment"));
        TESTS.forEach((name, fn) -> event.registerTest(id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                        new TestData<>(environment, id("basilisk_test_arena"), 200, 0, true))));
    }

    private static void prepareSnowArena(GameTestHelper helper) {
        for (int x = 1; x <= 11; x++) {
            for (int z = 1; z <= 11; z++) {
                helper.setBlock(new BlockPos(x, FLOOR_Y, z), Blocks.SNOW_BLOCK);
                for (int y = FLOOR_Y + 1; y <= FLOOR_Y + 8; y++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }
    }

    private static void registrationAndAttributes(GameTestHelper helper) {
        prepareSnowArena(helper);
        SoulReaper reaper = helper.spawnWithNoFreeWill(ModEntities.SOUL_REAPER.get(), new BlockPos(5, FLOOR_Y + 1, 5));
        helper.assertValueEqual(reaper.getMaxHealth(), 350.0F, "Soul Reaper max health must be 350 HP");
        helper.assertValueEqual(reaper.getAttributeValue(Attributes.ARMOR), 12.0D, "Soul Reaper armor");
        helper.assertValueEqual(reaper.getAttributeValue(Attributes.ARMOR_TOUGHNESS), 4.0D, "Soul Reaper armor toughness");
        helper.assertValueEqual(reaper.getAttributeValue(Attributes.ATTACK_DAMAGE), 14.0D, "Soul Reaper base attack damage");
        helper.assertValueEqual(reaper.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.26D, "Soul Reaper base movement speed");
        helper.assertValueEqual(reaper.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.90D, "Soul Reaper knockback resistance");
        helper.assertValueEqual(reaper.getAttributeValue(Attributes.FOLLOW_RANGE), 48.0D, "Soul Reaper follow range");
        helper.assertValueEqual(ModEntities.SOUL_REAPER.get().getCategory(), MobCategory.MONSTER, "Soul Reaper category");
        helper.assertValueEqual(reaper.getBbWidth(), 2.2F, "Soul Reaper width");
        helper.assertValueEqual(reaper.getBbHeight(), 5.4F, "Soul Reaper height");

        OmenHare hare = helper.spawnWithNoFreeWill(ModEntities.OMEN_HARE.get(), new BlockPos(2, FLOOR_Y + 1, 2));
        helper.assertValueEqual(hare.getMaxHealth(), 10.0F, "Omen Hare max health must be 10 HP");
        helper.assertValueEqual(hare.getBbWidth(), 0.5F, "Omen Hare width");
        helper.assertValueEqual(hare.getBbHeight(), 0.6F, "Omen Hare height");

        WanderingSoulWisp wisp = helper.spawnWithNoFreeWill(ModEntities.WANDERING_SOUL_WISP.get(), new BlockPos(8, FLOOR_Y + 2, 8));
        helper.assertValueEqual(wisp.getMaxHealth(), 6.0F, "Wandering Soul Wisp max health must be 6 HP");
        helper.assertTrue(wisp.isNoGravity(), "Wandering Soul Wisp must float with no gravity");
        helper.assertFalse(wisp.isPlayerSummon(), "Wandering Soul Wisp is a boss minion, not a player summon");

        helper.assertTrue(ModItems.SOUL_REAPER_SPAWN_EGG.get() instanceof SpawnEggItem, "Soul Reaper spawn egg registered");
        helper.assertTrue(ModItems.OMEN_HARE_SPAWN_EGG.get() instanceof SpawnEggItem, "Omen Hare spawn egg registered");
        wisp.discard();
        hare.discard();
        reaper.discard();
        helper.succeed();
    }

    private static void immunitiesAndAntiBurst(GameTestHelper helper) {
        prepareSnowArena(helper);
        SoulReaper reaper = helper.spawnWithNoFreeWill(ModEntities.SOUL_REAPER.get(), new BlockPos(5, FLOOR_Y + 1, 5));
        helper.assertFalse(reaper.canFreeze(), "Soul Reaper must be immune to freezing");
        helper.assertTrue(reaper.canBreatheUnderwater(), "Soul Reaper must not drown");
        helper.assertFalse(
                reaper.causeFallDamage(20.0D, 1.0F, helper.getLevel().damageSources().fall()),
                "Soul Reaper must ignore fall damage");
        helper.assertFalse(
                reaper.canBeAffected(new MobEffectInstance(MobEffects.WITHER, 100, 1)),
                "Soul Reaper must be immune to Wither");
        helper.assertFalse(
                reaper.canBeAffected(new MobEffectInstance(MobEffects.POISON, 100, 1)),
                "Soul Reaper must be immune to Poison");
        helper.assertFalse(
                reaper.canBeAffected(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1)),
                "Soul Reaper must be immune to Slowness");

        // Monotonic stage progression check (never regresses)
        reaper.setStage(SoulReaper.Stage.STAGE_2);
        helper.assertValueEqual(reaper.getStage(), SoulReaper.Stage.STAGE_2, "Stage advances to STAGE_2");
        reaper.setStage(SoulReaper.Stage.STAGE_1);
        helper.assertValueEqual(reaper.getStage(), SoulReaper.Stage.STAGE_2, "Stage must never regress to STAGE_1");
        reaper.setStage(SoulReaper.Stage.STAGE_3);
        helper.assertValueEqual(reaper.getStage(), SoulReaper.Stage.STAGE_3, "Stage advances to STAGE_3");

        // Peak player count monotonic check
        reaper.updatePeakPlayers(4);
        reaper.updatePeakPlayers(2);
        helper.assertValueEqual(reaper.getPeakPlayers(), 4, "Peak player count must not decrease");

        // Anti-burst cap: a massive 500-damage hit removes at most 4% of current maxHealth
        float maxHp = reaper.getMaxHealth();
        float before = reaper.getHealth();
        reaper.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 500.0F);
        float lost = before - reaper.getHealth();
        helper.assertTrue(
                lost > 0.0F && lost <= maxHp * 0.04F + 0.05F,
                "Single hit loss must be capped at 4% max HP, was " + lost);

        // NBT round-trip
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        reaper.saveWithoutId(out);
        SoulReaper loaded = ModEntities.SOUL_REAPER.get().create(helper.getLevel(), EntitySpawnReason.LOAD);
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), out.buildResult()));
        helper.assertValueEqual(loaded.getStage(), SoulReaper.Stage.STAGE_3, "Loaded Soul Reaper preserves STAGE_3");
        helper.assertValueEqual(loaded.getPeakPlayers(), 4, "Loaded Soul Reaper preserves peak players");
        reaper.discard();
        helper.succeed();
    }

    private static void omenHareSpawnRulesAndWispBinding(GameTestHelper helper) {
        prepareSnowArena(helper);
        BlockPos spawnPos = helper.absolutePos(new BlockPos(5, FLOOR_Y + 1, 5));
        var clearBox = net.minecraft.world.phys.AABB.ofSize(
                spawnPos.getCenter(), OmenHare.EXCLUSION_RADIUS * 2.0D, 128.0D, OmenHare.EXCLUSION_RADIUS * 2.0D);
        helper.getLevel().getEntitiesOfClass(OmenHare.class, clearBox).forEach(OmenHare::discard);
        helper.getLevel().getEntitiesOfClass(SoulReaper.class, clearBox).forEach(SoulReaper::discard);

        helper.assertTrue(
                OmenHare.checkSpawnRules(
                        ModEntities.OMEN_HARE.get(),
                        helper.getLevel(),
                        EntitySpawnReason.SPAWN_ITEM_USE,
                        spawnPos,
                        helper.getLevel().getRandom()),
                "Omen Hare should pass spawn rules on snow when no other hare/reaper is nearby");

        OmenHare hare = helper.spawnWithNoFreeWill(ModEntities.OMEN_HARE.get(), new BlockPos(5, FLOOR_Y + 1, 5));
        helper.assertFalse(
                OmenHare.checkSpawnRules(
                        ModEntities.OMEN_HARE.get(),
                        helper.getLevel(),
                        EntitySpawnReason.SPAWN_ITEM_USE,
                        spawnPos.offset(2, 0, 2),
                        helper.getLevel().getRandom()),
                "Second Omen Hare within 128 blocks must be rejected");
        hare.discard();

        SoulReaper reaper = helper.spawnWithNoFreeWill(ModEntities.SOUL_REAPER.get(), new BlockPos(6, FLOOR_Y + 1, 6));
        WanderingSoulWisp wisp = helper.spawnWithNoFreeWill(ModEntities.WANDERING_SOUL_WISP.get(), new BlockPos(4, FLOOR_Y + 2, 4));
        wisp.bindToReaper(reaper);
        helper.assertValueEqual(wisp.getReaperUuid(), reaper.getUUID(), "Wisp must store Reaper UUID");
        helper.assertValueEqual(wisp.getReaperEntityId(), reaper.getId(), "Wisp must sync Reaper entity ID");
        helper.assertTrue(reaper.getActiveWisps().contains(wisp.getUUID()), "Reaper must track bound wisp UUID");
        wisp.discard();
        reaper.discard();
        helper.succeed();
    }

    private static void modelsAndAnimationsBaking(GameTestHelper helper) {
        var reaperRoot = SoulReaperModel.createBodyLayer().bakeRoot();
        var lookup = reaperRoot.createPartLookup();
        helper.assertTrue(lookup.apply("reaper_root") != null, "reaper_root joint must exist");
        helper.assertTrue(lookup.apply("chest") != null, "chest joint must exist");
        helper.assertTrue(lookup.apply("scythe_root") != null, "scythe_root joint must exist");
        helper.assertTrue(lookup.apply("grip_upper") != null, "grip_upper joint must exist");
        helper.assertTrue(lookup.apply("grip_lower") != null, "grip_lower joint must exist");
        helper.assertTrue(lookup.apply("scythe_blade_tip") != null, "scythe_blade_tip joint must exist");

        SoulReaperModel reaperModel = new SoulReaperModel(reaperRoot);
        SoulReaperRenderState reaperState = new SoulReaperRenderState();
        reaperState.primaryAnimState.start(0);
        reaperState.reactAnimState.start(0);
        reaperState.ageInTicks = 15.0F;
        for (SoulReaper.ActionId action : SoulReaper.ActionId.values()) {
            reaperState.actionId = action;
            reaperModel.setupAnim(reaperState);
        }
        reaperState.reactId = SoulReaper.ReactId.HURT;
        reaperModel.setupAnim(reaperState);
        reaperState.reactId = SoulReaper.ReactId.A4_FLICK_SNUFF;
        reaperModel.setupAnim(reaperState);

        OmenHareModel hareModel = new OmenHareModel(OmenHareModel.createBodyLayer().bakeRoot());
        OmenHareRenderState hareState = new OmenHareRenderState();
        hareState.standStareAnimState.start(0);
        hareState.tiltTauntAnimState.start(0);
        hareState.ruptureAnimState.start(0);
        hareState.ageInTicks = 10.0F;
        for (OmenHare.State state : OmenHare.State.values()) {
            hareState.state = state;
            hareModel.setupAnim(hareState);
        }

        WanderingSoulWispModel wispModel =
                new WanderingSoulWispModel(WanderingSoulWispModel.createBodyLayer().bakeRoot());
        WanderingSoulWispRenderState wispState = new WanderingSoulWispRenderState();
        wispState.ageInTicks = 10.0F;
        wispModel.setupAnim(wispState);

        helper.succeed();
    }

    private static void omenHareMetamorphosisAndAwakening(GameTestHelper helper) {
        prepareSnowArena(helper);
        OmenHare hare = helper.spawn(ModEntities.OMEN_HARE.get(), new BlockPos(5, FLOOR_Y + 1, 5));

        // Lethal damage intercepts normal death and enters RUPTURING state
        hare.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 25.0F);
        helper.assertTrue(hare.isAlive(), "Omen Hare must intercept lethal damage and remain alive during rupture");
        helper.assertValueEqual(hare.getState(), OmenHare.State.RUPTURING, "Omen Hare must enter RUPTURING state");

        SoulReaper reaper = hare.completeMetamorphosis(helper.getLevel());
        helper.assertTrue(reaper != null && reaper.isAlive(), "Metamorphosis must spawn the Soul Reaper");
        helper.assertValueEqual(reaper.getState(), SoulReaper.State.AWAKENING, "Spawned Reaper must start in AWAKENING");
        helper.assertValueEqual(reaper.getActionId(), SoulReaper.ActionId.AWAKEN_ERUPTION, "Initial awakening action");

        // Reaper is invulnerable while AWAKENING
        boolean hurtWhileAwakening = reaper.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 50.0F);
        helper.assertFalse(hurtWhileAwakening, "Soul Reaper must be invulnerable during AWAKENING");
        reaper.discard();
        helper.succeed();
    }

    private static void desolationAuraAndFrost(GameTestHelper helper) {
        prepareSnowArena(helper);
        BlockPos veg1 = new BlockPos(4, FLOOR_Y + 1, 5);
        BlockPos veg2 = new BlockPos(6, FLOOR_Y + 1, 5);
        BlockPos stoneBlock = new BlockPos(5, FLOOR_Y + 1, 4);
        BlockPos waterUnder = new BlockPos(5, FLOOR_Y, 5);

        helper.setBlock(veg1, Blocks.OAK_LEAVES);
        helper.setBlock(veg2, Blocks.DANDELION);
        helper.setBlock(stoneBlock, Blocks.STONE);
        helper.setBlock(waterUnder, Blocks.WATER);

        SoulReaper reaper = helper.spawnWithNoFreeWill(ModEntities.SOUL_REAPER.get(), new BlockPos(5, FLOOR_Y + 1, 5));
        int broken = reaper.tickDesolationAura(helper.getLevel(), SoulReaper.DESOLATION_RADIUS);
        helper.assertTrue(broken >= 2, "Desolation aura must clear natural vegetation within 6 blocks");
        helper.assertBlockPresent(Blocks.AIR, veg1);
        helper.assertBlockPresent(Blocks.AIR, veg2);
        helper.assertBlockPresent(Blocks.STONE, stoneBlock);

        reaper.tick();
        helper.assertBlockPresent(Blocks.FROSTED_ICE, waterUnder);
        reaper.discard();
        helper.succeed();
    }

    private static void stagesHealingAndScaling(GameTestHelper helper) {
        prepareSnowArena(helper);
        SoulReaper reaper = helper.spawnWithNoFreeWill(ModEntities.SOUL_REAPER.get(), new BlockPos(5, FLOOR_Y + 1, 5));

        // Reduce to 50% HP (175 / 350), then scale to N_peak = 3 -> max HP = 350 * (1 + 1.2) = 770, current HP = 385 (50%)
        reaper.setHealth(175.0F);
        reaper.updatePeakPlayers(3);
        helper.assertValueEqual(reaper.getMaxHealth(), 770.0F, "Max health for N_peak=3 must be 770 HP");
        helper.assertTrue(Math.abs(reaper.getHealth() - 385.0F) < 1.0F, "Scaling N_peak must preserve exact 50% health ratio");

        // Trigger Stage 2 transition and verify healing ceiling (65% of 770 = 500.5 HP)
        reaper.setStage(SoulReaper.Stage.STAGE_2);
        reaper.heal(400.0F);
        helper.assertTrue(
                reaper.getHealth() <= 770.0F * 0.65F + 0.5F,
                "Healing in Stage 2 must never exceed 65% max HP ceiling");

        // Summon Wandering Soul Wisps (4 + N_peak = 7 for N_peak=3)
        List<WanderingSoulWisp> wisps = reaper.summonWanderingSoulWisps(helper.getLevel());
        helper.assertValueEqual(wisps.size(), 7, "Stage 2 wisp count must equal 4 + N_peak (7)");
        for (WanderingSoulWisp w : wisps) {
            w.discard();
        }

        // Advance to Stage 3 and verify 30% healing ceiling (30% of 770 = 231 HP)
        reaper.setHealth(150.0F);
        reaper.setStage(SoulReaper.Stage.STAGE_3);
        reaper.heal(400.0F);
        helper.assertTrue(
                reaper.getHealth() <= 770.0F * 0.30F + 0.5F,
                "Healing in Stage 3 must never exceed 30% max HP ceiling");
        reaper.discard();
        helper.succeed();
    }

    private static void originalAttacksAndAntiWhiff(GameTestHelper helper) {
        prepareSnowArena(helper);
        SoulReaper reaper = helper.spawnWithNoFreeWill(ModEntities.SOUL_REAPER.get(), new BlockPos(5, FLOOR_Y + 1, 5));
        reaper.setStage(SoulReaper.Stage.STAGE_2);
        IronGolem target = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(5, FLOOR_Y + 1, 8));

        for (SoulReaperAttack attack : List.of(
                SoulReaperAttack.O1_WHIRLWIND,
                SoulReaperAttack.O2_BLINK_STRIKE,
                SoulReaperAttack.O3_SOUL_HARVEST,
                SoulReaperAttack.O4_FROST_WAVE,
                SoulReaperAttack.O5_REAPING_DASH)) {
            target.setHealth(target.getMaxHealth());
            reaper.startAttack(attack, target);
            int hits = reaper.executeAttackImpact(helper.getLevel(), attack, 0);
            helper.assertTrue(hits >= 1, "Original attack " + attack.name() + " must hit target in range");
        }

        // Verify +6 tick anti-whiff recovery penalty when an attack misses all targets
        int normalRec = reaper.getEffectiveRecoveryTicks(SoulReaperAttack.O1_WHIRLWIND, false);
        int whiffRec = reaper.getEffectiveRecoveryTicks(SoulReaperAttack.O1_WHIRLWIND, true);
        helper.assertValueEqual(
                whiffRec - normalRec,
                SoulReaperAttack.WHIFF_PENALTY_TICKS,
                "Whiffed attack must add +6 ticks to recovery");

        target.discard();
        reaper.discard();
        helper.succeed();
    }

    private static void frontalAttacksAndExclusiveF4(GameTestHelper helper) {
        prepareSnowArena(helper);
        SoulReaper reaper = helper.spawnWithNoFreeWill(ModEntities.SOUL_REAPER.get(), new BlockPos(5, FLOOR_Y + 1, 5));
        reaper.setStage(SoulReaper.Stage.STAGE_3);
        IronGolem closeTarget = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(5, FLOOR_Y + 1, 8));
        IronGolem coneTarget = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(5, FLOOR_Y + 1, 11));

        for (SoulReaperAttack attack : List.of(
                SoulReaperAttack.F1_RISING_CRESCENT,
                SoulReaperAttack.F2_HAFT_THRUST,
                SoulReaperAttack.F3_CROSS_REAP,
                SoulReaperAttack.F5_LOW_SWEEP,
                SoulReaperAttack.F6_THREEFOLD_HARVEST)) {
            closeTarget.setHealth(closeTarget.getMaxHealth());
            reaper.startAttack(attack, closeTarget);
            int hits = reaper.executeAttackImpact(helper.getLevel(), attack, 0);
            helper.assertTrue(hits >= 1, "Frontal attack " + attack.name() + " must hit front target");
        }

        // F4 Gravedigger Slam: closeTarget (3b) gets direct 20 dmg only (not 20+8=28), coneTarget (6b) gets 8 cone dmg
        closeTarget.setHealth(closeTarget.getMaxHealth());
        coneTarget.setHealth(coneTarget.getMaxHealth());
        reaper.startAttack(SoulReaperAttack.F4_GRAVEDIGGER_SLAM, closeTarget);
        reaper.executeAttackImpact(helper.getLevel(), SoulReaperAttack.F4_GRAVEDIGGER_SLAM, 0);
        float closeLost = closeTarget.getMaxHealth() - closeTarget.getHealth();
        float coneLost = coneTarget.getMaxHealth() - coneTarget.getHealth();
        helper.assertTrue(
                closeLost > 12.0F && closeLost <= 21.0F,
                "F4 direct target must take direct damage (~20) and NOT cone damage on top, took " + closeLost);
        helper.assertTrue(
                coneLost > 4.0F && coneLost <= 9.0F,
                "F4 outer cone target must take cone damage (~8), took " + coneLost);

        closeTarget.discard();
        coneTarget.discard();
        reaper.discard();
        helper.succeed();
    }

    private static void sevenAntiInstakillRules(GameTestHelper helper) {
        prepareSnowArena(helper);
        SoulReaper reaper = helper.spawnWithNoFreeWill(ModEntities.SOUL_REAPER.get(), new BlockPos(5, FLOOR_Y + 1, 5));
        OmenHare victim = helper.spawnWithNoFreeWill(ModEntities.OMEN_HARE.get(), new BlockPos(5, FLOOR_Y + 1, 7));
        victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
        victim.setHealth(20.0F);

        // Rule 1 & Rule 3: Victim starts sequence at 100% HP (20/20 >= 80%).
        // First hit is clamped to 65% of 20 = 13.0 HP (leaving 7.0 HP).
        reaper.startAttack(SoulReaperAttack.F4_GRAVEDIGGER_SLAM, victim);
        float firstClamped = reaper.clampOutgoingDamagePostArmor(victim, 50.0F);
        helper.assertValueEqual(firstClamped, 13.0F, "Rule 1: Post-armor hit on 20-HP target must cap at 13.0 HP (65%)");
        victim.setHealth(7.0F);

        // Second hit in the same sequence would deal 13.0 HP on a 7.0 HP victim, but Breath Rule (Rule 3) holds at 1.0 HP!
        float secondClamped = reaper.clampOutgoingDamagePostArmor(victim, 13.0F);
        helper.assertValueEqual(secondClamped, 6.0F, "Rule 3 (Breath Rule): Must clamp damage to leave victim at 1.0 HP");

        // Rule 2: 6-tick mercy window blocks duplicate pulse 0, but allows distinct pulse 1
        IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(6, FLOOR_Y + 1, 7));
        boolean hitPulse0First = reaper.applyStrikeToTarget(helper.getLevel(), golem, 10.0F, 99, 0);
        boolean hitPulse0Dup = reaper.applyStrikeToTarget(helper.getLevel(), golem, 10.0F, 99, 0);
        boolean hitPulse1Next = reaper.applyStrikeToTarget(helper.getLevel(), golem, 10.0F, 99, 1);
        helper.assertTrue(hitPulse0First, "First hit of pulse 0 must land");
        helper.assertFalse(hitPulse0Dup, "Duplicate hit of same (sequenceId, pulseIndex) must be blocked by mercy window");
        helper.assertTrue(hitPulse1Next, "Distinct pulseIndex of multi-hit sequence must NOT be blocked by mercy window");

        // Rule 6: Safe knockback cancels horizontal push when lava is ahead
        BlockPos lavaPos = new BlockPos(6, FLOOR_Y, 9);
        helper.setBlock(lavaPos, Blocks.LAVA);
        reaper.applySafeKnockback(helper.getLevel(), golem, new Vec3(0.0D, 0.4D, 1.0D));
        helper.assertValueEqual((float) golem.getDeltaMovement().z, 0.0F, "Rule 6: Horizontal knockback toward lava must be zeroed");

        golem.discard();
        victim.discard();
        reaper.discard();
        helper.succeed();
    }

    private static void provenanceNullificationAndTaunt(GameTestHelper helper) {
        prepareSnowArena(helper);
        SoulReaper reaper = helper.spawnWithNoFreeWill(ModEntities.SOUL_REAPER.get(), new BlockPos(5, FLOOR_Y + 1, 5));
        IronGolem consumer = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(5, FLOOR_Y + 1, 8));

        // Pre-existing ambient/beacon effect: SPEED I (amplifier 0, duration 400) and low health (5.0 HP)
        consumer.setHealth(5.0F);
        consumer.addEffect(new MobEffectInstance(MobEffects.SPEED, 400, 0));
        reaper.recordPreConsumeSnapshot(consumer);

        // Simulate consuming an Enchanted Golden Apple / Potion that adds REGENERATION II, upgrades SPEED to II, and heals +4 HP (to 9.0 HP)
        consumer.setHealth(9.0F);
        consumer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        consumer.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 1));
        reaper.onConsumableFinished(helper.getLevel(), consumer, new ItemStack(Items.GOLDEN_APPLE));

        // Verify immediate T0 visual warning (Reaper enters NULLIFYING with A2_APPLE_TOLL)
        helper.assertValueEqual(reaper.getState(), SoulReaper.State.NULLIFYING, "Reaper must enter NULLIFYING at T0");
        helper.assertValueEqual(reaper.getActionId(), SoulReaper.ActionId.A2_APPLE_TOLL, "ActionId must be A2_APPLE_TOLL");

        // Advance 21 ticks so T0 + 20 resolution triggers
        for (int i = 0; i < 21; i++) {
            reaper.tick();
        }
        helper.assertFalse(consumer.hasEffect(MobEffects.REGENERATION), "Newly added REGENERATION must be nullified");
        MobEffectInstance speedAfter = consumer.getEffect(MobEffects.SPEED);
        helper.assertTrue(
                speedAfter != null && speedAfter.getAmplifier() == 0,
                "Pre-existing SPEED I must be restored to amplifier 0, not wiped");
        helper.assertTrue(consumer.getHealth() >= 2.0F, "Rule 7: Instant-heal toll must never reduce target below 2.0 HP");

        // Verify P1_BECKON taunt bait into F2_HAFT_THRUST at tick 28
        reaper.setNoAi(false);
        reaper.setTarget(consumer);
        reaper.startTaunt(SoulReaper.ActionId.P1_BECKON, true);
        for (int i = 0; i < 28; i++) {
            reaper.tick();
        }
        helper.assertValueEqual(reaper.getState(), SoulReaper.State.ATTACK_PREP, "Bait taunt P1 must transition to ATTACK_PREP at tick 28");
        helper.assertValueEqual(reaper.getCurrentAttack(), SoulReaperAttack.F2_HAFT_THRUST, "P1 bait must chain into F2_HAFT_THRUST");

        consumer.discard();
        reaper.discard();
        helper.succeed();
    }

    private static void itemsScytheBroochAndSpirit(GameTestHelper helper) {
        prepareSnowArena(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos playerPos = helper.absolutePos(new BlockPos(5, FLOOR_Y + 1, 5));
        player.snapTo(playerPos.getX() + 0.5D, playerPos.getY(), playerPos.getZ() + 0.5D, 0.0F, 0.0F);

        // 1. Frost Spiral Brooch: clears freeze ticks and grants +20% speed on snow/ice
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.FROST_SPIRAL_BROOCH.get()));
        player.setTicksFrozen(100);
        FrostSpiralBroochItem.tickBroochEffects(player);
        helper.assertValueEqual(player.getTicksFrozen(), 0, "Frost Spiral Brooch must clear powder-snow freeze ticks");
        helper.assertTrue(
                player.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(FrostSpiralBroochItem.FROST_STRIDE_MODIFIER_ID),
                "Frost Spiral Brooch must grant +20% speed modifier on snow/ice");

        // 2. Scythe of Wandering Souls: Spiritual Reap lunges 3b, damages targets, steals 2 HP/hit (capped at 6 HP), sets 300t cooldown
        ItemStack scythe = new ItemStack(ModItems.SOUL_REAPER_SCYTHE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, scythe);
        player.setHealth(10.0F);
        IronGolem target1 = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(5, FLOOR_Y + 1, 7));
        IronGolem target2 = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(6, FLOOR_Y + 1, 7));

        float healed = SoulReaperScytheItem.performSpiritualReap(helper.getLevel(), player, scythe, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(healed, 4.0F, "Spiritual Reap hitting 2 targets must heal 4.0 HP (2 HP per target)");
        helper.assertTrue(player.getCooldowns().isOnCooldown(scythe), "Scythe must enter 300-tick cooldown after Spiritual Reap");

        // Charged 360-degree sweep hits secondary nearby target
        int sweepHits = SoulReaperScytheItem.performCharged360Sweep(helper.getLevel(), player, target1);
        helper.assertTrue(sweepHits >= 1, "Charged 360-degree sweep must hit nearby secondary enemies");

        // 3. BossSpirit rise & Bestiary entry
        SoulReaper reaper = helper.spawnWithNoFreeWill(ModEntities.SOUL_REAPER.get(), new BlockPos(5, FLOOR_Y + 1, 5));
        BossSpirit spirit = BossSpirit.rise(helper.getLevel(), ModEntities.SOUL_REAPER_SPIRIT.get(), reaper);
        helper.assertTrue(spirit != null && spirit.isAlive(), "BossSpirit.rise must spawn SOUL_REAPER_SPIRIT");
        helper.assertTrue(Bestiary.ENTRIES.contains(Bestiary.SOUL_REAPER), "Bestiary must contain SOUL_REAPER chapter");
        helper.assertTrue(Bestiary.SOUL_REAPER.pages().size() >= 6, "Bestiary chapter must have at least 6 pages");

        spirit.discard();
        reaper.discard();
        target1.discard();
        target2.discard();
        helper.succeed();
    }
}
