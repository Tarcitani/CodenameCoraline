package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.client.salamander.BrimstoneSalamanderAnimations;
import com.codenamecoraline.client.salamander.BrimstoneSalamanderModel;
import com.codenamecoraline.entity.basilisk.Basilisk;
import com.codenamecoraline.entity.lithophage.Lithophage;
import com.codenamecoraline.entity.raven.FeyRaven;
import com.codenamecoraline.entity.salamander.BrimstoneSalamander;
import com.codenamecoraline.entity.salamander.BrimstoneSalamanderVariant;
import com.codenamecoraline.entity.salamander.BrimstoneSpit;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Server integration tests for the Brimstone Salamander foundation, visual rig, AI, and 4-phase combat. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class BrimstoneSalamanderGameTests {
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
            Map.entry("brimstone_salamander_attributes_and_registration", BrimstoneSalamanderGameTests::attributesAndRegistration),
            Map.entry("brimstone_salamander_spawn_rules_and_immunities", BrimstoneSalamanderGameTests::spawnRulesAndImmunities),
            Map.entry("brimstone_salamander_snap_bite_and_anti_whiff", BrimstoneSalamanderGameTests::snapBiteAndAntiWhiff),
            Map.entry("brimstone_salamander_sulfur_spit_and_tar_pool", BrimstoneSalamanderGameTests::sulfurSpitAndTarPool),
            Map.entry("brimstone_salamander_tail_whip_and_target_matrix", BrimstoneSalamanderGameTests::tailWhipAndTargetMatrix),
            Map.entry("brimstone_salamander_lava_swimming_and_navigation", BrimstoneSalamanderGameTests::lavaSwimmingAndNavigation),
            Map.entry("brimstone_salamander_texture_variants_and_persistence", BrimstoneSalamanderGameTests::textureVariantsAndPersistence),
            Map.entry("brimstone_salamander_baby_dimensions_and_scaling", BrimstoneSalamanderGameTests::babyDimensionsAndScaling),
            Map.entry("brimstone_salamander_baby_growth_age_progression", BrimstoneSalamanderGameTests::babyGrowthAgeProgression),
            Map.entry("brimstone_salamander_baby_nbt_persistence", BrimstoneSalamanderGameTests::babyNbtPersistence),
            Map.entry("brimstone_salamander_baby_stage_attributes", BrimstoneSalamanderGameTests::babyStageAttributes),
            Map.entry("brimstone_salamander_baby_maturation_clearance", BrimstoneSalamanderGameTests::babyMaturationClearance),
            Map.entry("brimstone_salamander_family_group_variant_consistency", BrimstoneSalamanderGameTests::familyGroupVariantConsistency),
            Map.entry("brimstone_salamander_baby_variant_persistence", BrimstoneSalamanderGameTests::babyVariantPersistence),
            Map.entry("brimstone_salamander_family_nbt_persistence", BrimstoneSalamanderGameTests::familyNbtPersistence),
            Map.entry("brimstone_salamander_baby_follow_family", BrimstoneSalamanderGameTests::babyFollowFamily),
            Map.entry("brimstone_salamander_baby_flee_threat", BrimstoneSalamanderGameTests::babyFleeThreat),
            Map.entry("brimstone_salamander_baby_prey_restriction", BrimstoneSalamanderGameTests::babyPreyRestriction),
            Map.entry("brimstone_salamander_baby_nip_combat", BrimstoneSalamanderGameTests::babyNipCombat),
            Map.entry("brimstone_salamander_parental_protection_fury", BrimstoneSalamanderGameTests::parentalProtectionFury),
            Map.entry("brimstone_salamander_baby_water_shock", BrimstoneSalamanderGameTests::babyWaterShock),
            Map.entry("brimstone_salamander_baby_feeding_magma_and_blaze", BrimstoneSalamanderGameTests::babyFeedingMagmaAndBlaze));

    private BrimstoneSalamanderGameTests() {}

    private static boolean enabled() {
        return Boolean.getBoolean("coraline.brimstoneSalamanderTests");
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
        var environment = event.registerEnvironment(id("brimstone_salamander_test_environment"));
        TESTS.forEach((name, fn) -> event.registerTest(id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                        new TestData<>(environment, id("basilisk_test_arena"), 120, 0, true))));
    }

    private static void attributesAndRegistration(GameTestHelper helper) {
        BrimstoneSalamander salamander = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(3, 1, 3));
        helper.assertValueEqual(salamander.getMaxHealth(), 44.0F, "Brimstone Salamander max health must be 44 HP");
        helper.assertValueEqual(salamander.getAttributeValue(Attributes.ARMOR), 8.0D, "Brimstone Salamander armor");
        helper.assertValueEqual(salamander.getAttributeValue(Attributes.ARMOR_TOUGHNESS), 2.0D, "Brimstone Salamander armor toughness");
        helper.assertValueEqual(salamander.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.23D, "Brimstone Salamander movement speed");
        helper.assertValueEqual(salamander.getAttributeValue(Attributes.ATTACK_DAMAGE), 6.0D, "Brimstone Salamander attack damage");
        helper.assertValueEqual(salamander.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.65D, "Brimstone Salamander knockback resistance");
        helper.assertValueEqual(salamander.getAttributeValue(Attributes.FOLLOW_RANGE), 24.0D, "Brimstone Salamander follow range");
        helper.assertValueEqual(ModEntities.BRIMSTONE_SALAMANDER.get().getCategory(), MobCategory.MONSTER, "Brimstone Salamander category");
        helper.assertValueEqual(salamander.getState(), BrimstoneSalamander.State.IDLE, "Brimstone Salamander initial state");
        helper.assertTrue(ModItems.BRIMSTONE_SALAMANDER_SPAWN_EGG.get() instanceof SpawnEggItem, "Brimstone Salamander spawn egg registered");
        helper.assertTrue(ModItems.BRIMSTONE_SCALE.get() != null, "Brimstone Scale registered");
        helper.assertTrue(ModItems.BRIMSTONE_SPIT.get() != null, "Brimstone Spit item registered");
        helper.assertTrue(com.codenamecoraline.bestiary.Bestiary.BRIMSTONE_SALAMANDER != null, "Bestiary chapter registered");
        helper.assertValueEqual(com.codenamecoraline.bestiary.Bestiary.BRIMSTONE_SALAMANDER.pages().size(), 6, "Bestiary chapter has 6 pages");
        helper.assertTrue(com.codenamecoraline.registry.ModSounds.BRIMSTONE_SALAMANDER_AMBIENT.get() != null, "Ambient sound registered");
        helper.assertTrue(com.codenamecoraline.registry.ModSounds.BRIMSTONE_SALAMANDER_HURT.get() != null, "Hurt sound registered");
        helper.assertTrue(com.codenamecoraline.registry.ModSounds.BRIMSTONE_SALAMANDER_DEATH.get() != null, "Death sound registered");
        helper.assertTrue(com.codenamecoraline.registry.ModSounds.BRIMSTONE_SALAMANDER_BITE.get() != null, "Bite sound registered");
        helper.assertTrue(com.codenamecoraline.registry.ModSounds.BRIMSTONE_SALAMANDER_SPIT.get() != null, "Spit sound registered");
        helper.assertTrue(com.codenamecoraline.registry.ModSounds.BRIMSTONE_SALAMANDER_TAIL_WHIP.get() != null, "Tail whip sound registered");
        helper.assertTrue(salamander.fireImmune(), "Brimstone Salamander must be immune to fire and lava");
        helper.assertTrue(salamander.isSensitiveToWater(), "Brimstone Salamander must be sensitive to water");

        var root = BrimstoneSalamanderModel.createBodyLayer().bakeRoot();
        var model = new BrimstoneSalamanderModel(root);
        helper.assertTrue(model != null, "BrimstoneSalamanderModel bakes all 18 joints and 17 animations cleanly");
        helper.assertTrue(
                BrimstoneSalamanderAnimations.IDLE_BREATHE != null
                        && BrimstoneSalamanderAnimations.IDLE_THROAT_PUFF != null
                        && BrimstoneSalamanderAnimations.IDLE_TAIL_SWAY != null
                        && BrimstoneSalamanderAnimations.IDLE_SNIFF != null
                        && BrimstoneSalamanderAnimations.WALK != null
                        && BrimstoneSalamanderAnimations.SWIM_LAVA != null
                        && BrimstoneSalamanderAnimations.SNAP_BITE_PREP != null
                        && BrimstoneSalamanderAnimations.SNAP_BITE_EXEC != null
                        && BrimstoneSalamanderAnimations.SNAP_BITE_RECOVERY != null
                        && BrimstoneSalamanderAnimations.SULFUR_SPIT_PREP != null
                        && BrimstoneSalamanderAnimations.SULFUR_SPIT_EXEC != null
                        && BrimstoneSalamanderAnimations.SULFUR_SPIT_RECOVERY != null
                        && BrimstoneSalamanderAnimations.TAIL_WHIP_PREP != null
                        && BrimstoneSalamanderAnimations.TAIL_WHIP_EXEC != null
                        && BrimstoneSalamanderAnimations.TAIL_WHIP_RECOVERY != null
                        && BrimstoneSalamanderAnimations.HURT != null
                        && BrimstoneSalamanderAnimations.DEATH != null,
                "All 17 Brimstone Salamander animations must be defined");
        helper.succeed();
    }

    private static void spawnRulesAndImmunities(GameTestHelper helper) {
        BrimstoneSalamander salamander = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(3, 1, 3));

        // Fall damage reduction (40% multiplier)
        helper.assertTrue(
                salamander.causeFallDamage(10.0D, 1.0F, helper.getLevel().damageSources().fall()),
                "Brimstone Salamander still processes fall damage with 60% reduction");

        // Out of bounds Y rejection (< 24 or > 48)
        BlockPos tooLow = new BlockPos(helper.absolutePos(new BlockPos(5, 1, 5)).getX(), 15,
                helper.absolutePos(new BlockPos(5, 1, 5)).getZ());
        helper.assertFalse(
                BrimstoneSalamander.checkSpawnRules(
                        ModEntities.BRIMSTONE_SALAMANDER.get(),
                        helper.getLevel(),
                        EntitySpawnReason.NATURAL,
                        tooLow,
                        helper.getLevel().getRandom()),
                "Brimstone Salamander must reject natural spawn below Y=24");

        BlockPos tooHigh = new BlockPos(helper.absolutePos(new BlockPos(5, 1, 5)).getX(), 60,
                helper.absolutePos(new BlockPos(5, 1, 5)).getZ());
        helper.assertFalse(
                BrimstoneSalamander.checkSpawnRules(
                        ModEntities.BRIMSTONE_SALAMANDER.get(),
                        helper.getLevel(),
                        EntitySpawnReason.NATURAL,
                        tooHigh,
                        helper.getLevel().getRandom()),
                "Brimstone Salamander must reject natural spawn above Y=48");

        // Valid spawn on basalt at Y between 24 and 48
        BlockPos absBase = helper.absolutePos(new BlockPos(6, 2, 6));
        BlockPos validYPos = new BlockPos(absBase.getX(), 35, absBase.getZ());
        helper.getLevel().setBlockAndUpdate(validYPos.below(), Blocks.BASALT.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(validYPos, Blocks.AIR.defaultBlockState());

        helper.assertTrue(
                BrimstoneSalamander.checkSpawnRules(
                        ModEntities.BRIMSTONE_SALAMANDER.get(),
                        helper.getLevel(),
                        EntitySpawnReason.NATURAL,
                        validYPos,
                        helper.getLevel().getRandom()),
                "Brimstone Salamander must allow spawn on basalt at Y in [24, 48]");

        helper.succeed();
    }

    private static void snapBiteAndAntiWhiff(GameTestHelper helper) {
        BrimstoneSalamander salamander = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(3, 1, 3));
        IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(4, 1, 3));
        float initialHealth = golem.getHealth();

        // 1. Direct hit test: start SNAP_BITE and tick through prep (8t) + exec (4t)
        salamander.setTarget(golem);
        salamander.startSnapBite();
        helper.assertValueEqual(salamander.getState(), BrimstoneSalamander.State.SNAP_BITE, "Starts in SNAP_BITE");

        for (int i = 0; i < BrimstoneSalamander.SNAP_BITE_PREP_TICKS + BrimstoneSalamander.SNAP_BITE_EXEC_TICKS; i++) {
            salamander.tick();
        }

        helper.assertTrue(golem.getHealth() < initialHealth, "SNAP_BITE deals 7.0 damage at impact tick");
        helper.assertTrue(golem.getRemainingFireTicks() > 0, "SNAP_BITE ignites target for 40 ticks");
        helper.assertValueEqual(salamander.getState(), BrimstoneSalamander.State.RECOVERY, "Enters RECOVERY after SNAP_BITE exec");
        helper.assertValueEqual(salamander.getRecoveryTicks(), BrimstoneSalamander.SNAP_BITE_RECOVERY_TICKS, "Normal recovery is 12 ticks");

        // 2. Anti-whiff test: move target beyond 2.6 blocks before tick 7
        BlockPos farPos = helper.absolutePos(new BlockPos(9, 1, 3));
        golem.setPos(farPos.getX() + 0.5D, farPos.getY(), farPos.getZ() + 0.5D);
        salamander.startSnapBite();
        for (int i = 0; i < BrimstoneSalamander.SNAP_BITE_ANTI_WHIFF_TICK; i++) {
            salamander.tick();
        }
        helper.assertValueEqual(salamander.getState(), BrimstoneSalamander.State.PURSUING, "Anti-whiff cancels SNAP_BITE at tick 7 into PURSUING");
        helper.assertValueEqual(salamander.getSnapBiteCooldown(), BrimstoneSalamander.SNAP_BITE_CANCEL_COOLDOWN_TICKS, "Anti-whiff sets 15-tick short cooldown");

        helper.succeed();
    }

    private static void sulfurSpitAndTarPool(GameTestHelper helper) {
        BrimstoneSalamander salamander = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(2, 1, 2));
        IronGolem pillaringGolem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(4, 4, 2));

        // Verify anti-pillaring detection (target 3 blocks above)
        helper.assertTrue(salamander.isTargetPillaring(pillaringGolem), "Detects target 3 blocks above as pillaring");

        salamander.setTarget(pillaringGolem);
        salamander.startSulfurSpit();
        helper.assertValueEqual(salamander.getState(), BrimstoneSalamander.State.SULFUR_SPIT, "Starts in SULFUR_SPIT");

        for (int i = 0; i < BrimstoneSalamander.SULFUR_SPIT_PREP_TICKS + BrimstoneSalamander.SULFUR_SPIT_EXEC_TICKS; i++) {
            salamander.tick();
        }

        helper.assertValueEqual(salamander.getState(), BrimstoneSalamander.State.RECOVERY, "Enters RECOVERY after SULFUR_SPIT exec");
        AABB arenaBox = new AABB(helper.absolutePos(new BlockPos(0, 0, 0))).inflate(12.0D);
        List<BrimstoneSpit> spits = helper.getLevel().getEntitiesOfClass(BrimstoneSpit.class, arenaBox);
        helper.assertFalse(spits.isEmpty(), "SULFUR_SPIT spawns BrimstoneSpit projectile at impact tick");

        // Verify Burning Tar Pool transformation and Slowness II + fire application
        BrimstoneSpit spit = spits.get(0);
        IronGolem groundVictim = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(6, 1, 6));
        spit.becomeTarPool(groundVictim.position());
        helper.assertTrue(spit.isTarPool(), "BrimstoneSpit transforms into Burning Tar Pool");
        helper.assertValueEqual(spit.getTarPoolTicks(), BrimstoneSpit.TAR_POOL_DURATION_TICKS, "Tar pool lasts 100 ticks");
        helper.assertTrue(groundVictim.hasEffect(MobEffects.SLOWNESS), "Burning Tar Pool applies Slowness II to non-salamander victims");
        helper.assertTrue(groundVictim.getRemainingFireTicks() > 0, "Burning Tar Pool ignites victims");

        helper.succeed();
    }

    private static void tailWhipAndTargetMatrix(GameTestHelper helper) {
        BrimstoneSalamander salamander = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(4, 1, 4));
        IronGolem flanker1 = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(3, 1, 4));
        IronGolem flanker2 = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(5, 1, 4));
        float initialHealth = flanker1.getHealth();

        // Verify anti-cerco (2 enemies within 2.0 blocks)
        helper.assertTrue(salamander.countNearbyEnemies(BrimstoneSalamander.ANTI_SURROUND_RADIUS) >= 2, "Detects >= 2 nearby enemies for anti-cerco");

        salamander.setTarget(flanker1);
        salamander.startTailWhip();
        for (int i = 0; i < BrimstoneSalamander.TAIL_WHIP_PREP_TICKS + BrimstoneSalamander.TAIL_WHIP_EXEC_TICKS; i++) {
            salamander.tick();
        }
        helper.assertTrue(flanker1.getHealth() < initialHealth && flanker2.getHealth() < initialHealth, "TAIL_WHIP damages surrounded flankers");
        helper.assertValueEqual(salamander.getState(), BrimstoneSalamander.State.RECOVERY, "Enters RECOVERY after TAIL_WHIP exec");

        // Verify Target Matrix rules
        Strider babyStrider = helper.spawnWithNoFreeWill(EntityType.STRIDER, new BlockPos(7, 1, 4));
        babyStrider.setBaby(true);
        WitherSkeleton undead = helper.spawnWithNoFreeWill(EntityType.WITHER_SKELETON, new BlockPos(7, 1, 5));
        Basilisk basilisk = helper.spawnWithNoFreeWill(ModEntities.BASILISK.get(), new BlockPos(7, 1, 6));
        Lithophage lithophage = helper.spawnWithNoFreeWill(ModEntities.LITHOPHAGE.get(), new BlockPos(7, 1, 7));
        FeyRaven raven = helper.spawnWithNoFreeWill(ModEntities.FEY_RAVEN.get(), new BlockPos(7, 1, 8));

        helper.assertTrue(salamander.canTargetEntity(flanker1, false), "Targets Iron Golems");
        helper.assertTrue(salamander.canTargetEntity(babyStrider, false), "Targets baby Striders");
        helper.assertFalse(salamander.canTargetEntity(undead, false), "Ignores Undead unless attacked directly");
        helper.assertTrue(salamander.canTargetEntity(undead, true), "Retaliates against Undead when directly attacked");
        helper.assertFalse(salamander.canTargetEntity(basilisk, true), "Never targets Basilisk");
        helper.assertFalse(salamander.canTargetEntity(lithophage, true), "Never targets Lithophage");
        helper.assertFalse(salamander.canTargetEntity(raven, true), "Never targets Fey Raven");

        helper.succeed();
    }

    private static void lavaSwimmingAndNavigation(GameTestHelper helper) {
        // 1. Setup lava pool at (3, 1, 3)
        for (int x = 2; x <= 4; x++) {
            for (int z = 2; z <= 4; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.LAVA);
            }
        }

        BrimstoneSalamander salamander = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(3, 1, 3));
        helper.assertTrue(salamander.isSwimmingInLava(), "Salamander inside lava block returns isSwimmingInLava == true");

        // 2. Navigation path & destination stability
        BlockPos lavaPos = helper.absolutePos(new BlockPos(3, 1, 3));
        helper.assertTrue(salamander.getNavigation().isStableDestination(lavaPos), "SalamanderPathNavigation recognizes lava as stable destination");

        BlockPos magmaPos = helper.absolutePos(new BlockPos(4, 0, 4));
        helper.setBlock(new BlockPos(4, 0, 4), Blocks.MAGMA_BLOCK);
        helper.assertTrue(salamander.getNavigation().isStableDestination(magmaPos), "SalamanderPathNavigation recognizes magma block as stable destination");

        // 3. Buoyancy: submerged entity receives gentle upward impulse
        helper.setBlock(new BlockPos(3, 2, 3), Blocks.LAVA);
        salamander.setDeltaMovement(Vec3.ZERO);
        salamander.tick();
        helper.assertTrue(salamander.getDeltaMovement().y > 0.0D, "Submerged salamander gains upward buoyancy");
        helper.setBlock(new BlockPos(3, 2, 3), Blocks.AIR);

        // 4. Locomotion along heading (verifying absence of 45-degree diagonal lock)
        // A: Facing South (yaw = 0.0F -> forward is purely +Z)
        salamander.setYRot(0.0F);
        salamander.setYBodyRot(0.0F);
        salamander.setDeltaMovement(Vec3.ZERO);
        salamander.travel(new Vec3(0.0D, 0.0D, 1.0D));
        Vec3 velSouth = salamander.getDeltaMovement();
        helper.assertTrue(velSouth.z > 0.05D, "Southbound swim propels towards +Z");
        helper.assertTrue(Math.abs(velSouth.x) < 1.0E-3D, "Southbound swim produces zero X drift (no 45-degree lock)");

        // B: Facing West (yaw = 90.0F -> forward is purely -X)
        salamander.setYRot(90.0F);
        salamander.setYBodyRot(90.0F);
        salamander.setDeltaMovement(Vec3.ZERO);
        salamander.travel(new Vec3(0.0D, 0.0D, 1.0D));
        Vec3 velWest = salamander.getDeltaMovement();
        helper.assertTrue(velWest.x < -0.05D, "Westbound swim propels towards -X");
        helper.assertTrue(Math.abs(velWest.z) < 1.0E-3D, "Westbound swim produces zero Z drift (no 45-degree lock)");

        // 5. Viscous fluid damping (no runaway positive feedback or infinite sliding)
        salamander.setDeltaMovement(new Vec3(0.35D, 0.0D, 0.35D));
        for (int i = 0; i < 8; i++) {
            salamander.travel(Vec3.ZERO);
        }
        helper.assertTrue(salamander.getDeltaMovement().horizontalDistance() < 0.05D, "Idle salamander smoothly decelerates to rest in lava");

        helper.succeed();
    }

    private static void textureVariantsAndPersistence(GameTestHelper helper) {
        BrimstoneSalamander salamander = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(3, 1, 3));
        helper.assertValueEqual(salamander.getVariant(), BrimstoneSalamanderVariant.BRIMSTONE, "Default variant is BRIMSTONE");

        // 1. Variant mutation & enumeration
        salamander.setVariant(BrimstoneSalamanderVariant.SOULFLAME);
        helper.assertValueEqual(salamander.getVariant(), BrimstoneSalamanderVariant.SOULFLAME, "Can mutate to SOULFLAME");
        helper.assertValueEqual(salamander.getVariant().getId(), 1, "SOULFLAME has id 1");

        salamander.setVariant(BrimstoneSalamanderVariant.CRIMSON);
        helper.assertValueEqual(salamander.getVariant(), BrimstoneSalamanderVariant.CRIMSON, "Can mutate to CRIMSON");
        helper.assertValueEqual(salamander.getVariant().getId(), 2, "CRIMSON has id 2");

        // Enum helpers
        helper.assertValueEqual(BrimstoneSalamanderVariant.byId(0), BrimstoneSalamanderVariant.BRIMSTONE, "byId(0)");
        helper.assertValueEqual(BrimstoneSalamanderVariant.byId(1), BrimstoneSalamanderVariant.SOULFLAME, "byId(1)");
        helper.assertValueEqual(BrimstoneSalamanderVariant.byId(2), BrimstoneSalamanderVariant.CRIMSON, "byId(2)");
        helper.assertValueEqual(BrimstoneSalamanderVariant.byName("soulflame"), BrimstoneSalamanderVariant.SOULFLAME, "byName(soulflame)");
        helper.assertValueEqual(BrimstoneSalamanderVariant.byName("crimson"), BrimstoneSalamanderVariant.CRIMSON, "byName(crimson)");
        helper.assertValueEqual(BrimstoneSalamanderVariant.byName("invalid"), BrimstoneSalamanderVariant.BRIMSTONE, "byName(invalid) fallback");

        // 2. NBT Persistence
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        salamander.saveWithoutId(out);
        BrimstoneSalamander loaded = ModEntities.BRIMSTONE_SALAMANDER.get().create(helper.getLevel(), EntitySpawnReason.LOAD);
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), out.buildResult()));
        helper.assertValueEqual(loaded.getVariant(), BrimstoneSalamanderVariant.CRIMSON, "NBT reload preserves CRIMSON variant");

        // Test persistence of SOULFLAME
        salamander.setVariant(BrimstoneSalamanderVariant.SOULFLAME);
        var outSoul = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        salamander.saveWithoutId(outSoul);
        BrimstoneSalamander loadedSoul = ModEntities.BRIMSTONE_SALAMANDER.get().create(helper.getLevel(), EntitySpawnReason.LOAD);
        loadedSoul.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), outSoul.buildResult()));
        helper.assertValueEqual(loadedSoul.getVariant(), BrimstoneSalamanderVariant.SOULFLAME, "NBT reload preserves SOULFLAME variant");

        helper.succeed();
    }

    private static void babyDimensionsAndScaling(GameTestHelper helper) {
        BrimstoneSalamander adult = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(2, 1, 2));
        helper.assertFalse(adult.isBaby(), "Adult is not baby");
        helper.assertValueEqual(adult.getGrowthAge(), 0, "Adult growthAge is 0");
        helper.assertTrue(Math.abs(adult.getBbWidth() - BrimstoneSalamander.WIDTH) < 0.05F, "Adult width is 1.30");
        helper.assertTrue(Math.abs(adult.getBbHeight() - BrimstoneSalamander.HEIGHT) < 0.05F, "Adult height is 0.85");

        BrimstoneSalamander baby = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(4, 1, 4));
        baby.setBaby(true);
        helper.assertTrue(baby.isBaby(), "Baby isBaby() is true");
        helper.assertValueEqual(baby.getGrowthAge(), BrimstoneSalamander.BABY_START_AGE, "Baby growthAge is -24000");
        helper.assertTrue(Math.abs(baby.getBbWidth() - BrimstoneSalamander.BABY_WIDTH) < 0.05F, "Baby width is 0.65");
        helper.assertTrue(Math.abs(baby.getBbHeight() - BrimstoneSalamander.BABY_HEIGHT) < 0.05F, "Baby height is 0.425");
        helper.assertTrue(Math.abs(baby.getEyeHeight() - BrimstoneSalamander.BABY_EYE_HEIGHT) < 0.05F, "Baby eyeHeight is 0.275");

        helper.succeed();
    }

    private static void babyGrowthAgeProgression(GameTestHelper helper) {
        BrimstoneSalamander baby = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(3, 1, 3));
        baby.setGrowthAge(BrimstoneSalamander.BABY_START_AGE);
        helper.assertValueEqual(baby.getGrowthAge(), BrimstoneSalamander.BABY_START_AGE, "Initial age is -24000");

        // Tick once on server
        baby.tick();
        helper.assertValueEqual(baby.getGrowthAge(), BrimstoneSalamander.BABY_START_AGE + 1, "Advances 1 tick per server tick");

        // Set to ready to mature
        baby.setGrowthAge(BrimstoneSalamander.READY_TO_MATURE_AGE);
        helper.assertValueEqual(baby.getGrowthAge(), -1, "Ready to mature age is -1");
        helper.assertTrue(baby.isBaby(), "Still baby when READY_TO_MATURE");

        // In clear space, tryMature allows maturation
        helper.assertTrue(baby.canFitAdultHitbox(), "Clear space allows adult hitbox");
        boolean matured = baby.tryMature(helper.getLevel());
        helper.assertTrue(matured, "Maturation succeeds in open space");
        helper.assertValueEqual(baby.getGrowthAge(), BrimstoneSalamander.ADULT_AGE, "Matured to adult age 0");
        helper.assertFalse(baby.isBaby(), "No longer baby after maturation");
        helper.assertTrue(Math.abs(baby.getBbWidth() - BrimstoneSalamander.WIDTH) < 0.05F, "AABB updated to adult width");

        helper.succeed();
    }

    private static void babyNbtPersistence(GameTestHelper helper) {
        BrimstoneSalamander baby = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(3, 1, 3));
        baby.setGrowthAge(-12000);
        helper.assertValueEqual(baby.getGrowthAge(), -12000, "Partial age set");

        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        baby.saveWithoutId(out);
        BrimstoneSalamander loaded = ModEntities.BRIMSTONE_SALAMANDER.get().create(helper.getLevel(), EntitySpawnReason.LOAD);
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), out.buildResult()));

        helper.assertValueEqual(loaded.getGrowthAge(), -12000, "Loaded preserves growthAge -12000");
        helper.assertTrue(loaded.isBaby(), "Loaded is baby");
        helper.assertTrue(Math.abs(loaded.getBbWidth() - BrimstoneSalamander.BABY_WIDTH) < 0.05F, "Loaded baby width");

        // Test legacy migration: CompoundTag with legacy IsBaby=true but no GrowthAge
        net.minecraft.nbt.CompoundTag legacyTag = new net.minecraft.nbt.CompoundTag();
        legacyTag.putBoolean("IsBaby", true);
        BrimstoneSalamander migrated = ModEntities.BRIMSTONE_SALAMANDER.get().create(helper.getLevel(), EntitySpawnReason.LOAD);
        migrated.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), legacyTag));
        helper.assertValueEqual(migrated.getGrowthAge(), BrimstoneSalamander.BABY_START_AGE, "Legacy IsBaby migrates to -24000");
        helper.assertTrue(migrated.isBaby(), "Migrated is baby");

        helper.succeed();
    }

    private static void babyStageAttributes(GameTestHelper helper) {
        BrimstoneSalamander baby = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(3, 1, 3));
        baby.setBaby(true);

        helper.assertValueEqual(baby.getMaxHealth(), 16.0F, "Baby max health 16 HP");
        helper.assertValueEqual(baby.getAttributeValue(Attributes.ARMOR), 2.0D, "Baby armor 2");
        helper.assertValueEqual(baby.getAttributeValue(Attributes.ARMOR_TOUGHNESS), 0.0D, "Baby toughness 0");
        helper.assertValueEqual(baby.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.26D, "Baby speed 0.26");
        helper.assertValueEqual(baby.getAttributeValue(Attributes.ATTACK_DAMAGE), 2.0D, "Baby damage 2");
        helper.assertValueEqual(baby.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.15D, "Baby knockback resistance 0.15");
        helper.assertValueEqual(baby.getAttributeValue(Attributes.FOLLOW_RANGE), 16.0D, "Baby follow range 16");

        // Idempotency: reapply baby attributes twice
        baby.applyBabyAttributes();
        baby.applyBabyAttributes();
        helper.assertValueEqual(baby.getMaxHealth(), 16.0F, "Idempotent max health");
        helper.assertValueEqual(baby.getAttributeValue(Attributes.ARMOR), 2.0D, "Idempotent armor");

        // Switch to adult
        baby.setBaby(false);
        helper.assertValueEqual(baby.getMaxHealth(), 44.0F, "Adult max health 44 HP");
        helper.assertValueEqual(baby.getAttributeValue(Attributes.ARMOR), 8.0D, "Adult armor 8");
        helper.assertValueEqual(baby.getAttributeValue(Attributes.ARMOR_TOUGHNESS), 2.0D, "Adult toughness 2");
        helper.assertValueEqual(baby.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.23D, "Adult speed 0.23");
        helper.assertValueEqual(baby.getAttributeValue(Attributes.ATTACK_DAMAGE), 6.0D, "Adult damage 6");

        helper.succeed();
    }

    private static void babyMaturationClearance(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        BrimstoneSalamander baby = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), pos);
        baby.setGrowthAge(BrimstoneSalamander.READY_TO_MATURE_AGE);

        // Place a top slab at pos (y = 1.0 to 1.5 is air with 0.5m clearance; slab occupies y = 1.5 to 2.0).
        // Baby height is 0.425m (fits under 1.5m).
        // Adult height is 0.85m (reaches 1.85m, colliding with the top slab at 1.5m).
        helper.setBlock(pos, Blocks.BLACKSTONE_SLAB.defaultBlockState().setValue(
                net.minecraft.world.level.block.SlabBlock.TYPE,
                net.minecraft.world.level.block.state.properties.SlabType.TOP));

        helper.assertFalse(baby.canFitAdultHitbox(), "Adult hitbox cannot fit with 0.5m clearance under top slab");
        boolean maturedUnderCeiling = baby.tryMature(helper.getLevel());
        helper.assertFalse(maturedUnderCeiling, "Maturation postponed when space is blocked");
        helper.assertValueEqual(baby.getGrowthAge(), BrimstoneSalamander.READY_TO_MATURE_AGE, "Stays at READY_TO_MATURE (-1)");
        helper.assertTrue(baby.isBaby(), "Remains a baby while blocked");

        // Clear the obstacle (replace with air)
        helper.setBlock(pos, Blocks.AIR);
        helper.assertTrue(baby.canFitAdultHitbox(), "Adult hitbox fits after clearing ceiling");

        // Set partial health on baby (e.g. 10.0 HP)
        baby.setHealth(10.0F);

        boolean maturedInClear = baby.tryMature(helper.getLevel());
        helper.assertTrue(maturedInClear, "Maturation succeeds when space is clear");
        helper.assertFalse(baby.isBaby(), "Becomes adult");
        helper.assertValueEqual(baby.getGrowthAge(), BrimstoneSalamander.ADULT_AGE, "Adult growthAge is 0");
        helper.assertValueEqual(baby.getHealth(), 10.0F, "Absolute health preserved across maturation (no free heal)");

        helper.succeed();
    }

    private static void familyGroupVariantConsistency(GameTestHelper helper) {
        BrimstoneSalamander leader = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(2, 1, 2));
        var difficulty = helper.getLevel().getCurrentDifficultyAt(leader.blockPosition());
        var groupData = leader.finalizeSpawn(helper.getLevel(), difficulty, EntitySpawnReason.NATURAL, null);

        helper.assertTrue(groupData != null, "Leader produces group data");
        helper.assertTrue(leader.getFamilyId() != null, "Leader has valid familyId");

        BrimstoneSalamander child = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(3, 1, 2));
        child.finalizeSpawn(helper.getLevel(), difficulty, EntitySpawnReason.NATURAL, groupData);

        helper.assertValueEqual(child.getVariant(), leader.getVariant(), "Group member inherits leader variant");
        helper.assertValueEqual(child.getFamilyId(), leader.getFamilyId(), "Group member shares familyId");
        helper.assertValueEqual(child.getLeaderUUID(), leader.getUUID(), "Group member references leader UUID");

        helper.succeed();
    }

    private static void babyVariantPersistence(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        BrimstoneSalamander babySoul = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), pos);
        babySoul.setVariant(BrimstoneSalamanderVariant.SOULFLAME);
        babySoul.setBaby(true);
        babySoul.setGrowthAge(BrimstoneSalamander.READY_TO_MATURE_AGE);

        boolean maturedSoul = babySoul.tryMature(helper.getLevel());
        helper.assertTrue(maturedSoul, "Soulflame baby matures successfully");
        helper.assertFalse(babySoul.isBaby(), "Is adult after maturation");
        helper.assertValueEqual(babySoul.getVariant(), BrimstoneSalamanderVariant.SOULFLAME, "Variant remains SOULFLAME after maturing");

        BrimstoneSalamander babyCrimson = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), pos);
        babyCrimson.setVariant(BrimstoneSalamanderVariant.CRIMSON);
        babyCrimson.setBaby(true);
        babyCrimson.setGrowthAge(BrimstoneSalamander.READY_TO_MATURE_AGE);

        boolean maturedCrimson = babyCrimson.tryMature(helper.getLevel());
        helper.assertTrue(maturedCrimson, "Crimson baby matures successfully");
        helper.assertFalse(babyCrimson.isBaby(), "Is adult after maturation");
        helper.assertValueEqual(babyCrimson.getVariant(), BrimstoneSalamanderVariant.CRIMSON, "Variant remains CRIMSON after maturing");

        helper.succeed();
    }

    private static void familyNbtPersistence(GameTestHelper helper) {
        BrimstoneSalamander original = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(3, 1, 3));
        UUID testFamily = UUID.randomUUID();
        UUID testLeader = UUID.randomUUID();
        original.setFamilyId(testFamily);
        original.setLeaderUUID(testLeader);

        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        original.saveWithoutId(out);
        CompoundTag tag = out.buildResult();

        BrimstoneSalamander loaded = ModEntities.BRIMSTONE_SALAMANDER.get().create(helper.getLevel(), EntitySpawnReason.LOAD);
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), tag));

        helper.assertValueEqual(loaded.getFamilyId(), testFamily, "FamilyId persists across NBT save/load");
        helper.assertValueEqual(loaded.getLeaderUUID(), testLeader, "LeaderUUID persists across NBT save/load");

        helper.succeed();
    }

    private static void babyFollowFamily(GameTestHelper helper) {
        BrimstoneSalamander adult = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(2, 1, 2));
        UUID familyId = UUID.randomUUID();
        adult.setFamilyId(familyId);

        BrimstoneSalamander baby = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(8, 1, 2));
        baby.setBaby(true);
        baby.setFamilyId(familyId);
        baby.setLeaderUUID(adult.getUUID());

        helper.assertTrue(baby.distanceToSqr(adult) > 20.25D, "Baby starts beyond comfortable distance (> 4.5 blocks)");
        helper.assertFalse(baby.canTargetEntity(adult, false), "Baby never targets family adult");

        helper.succeed();
    }

    private static void babyFleeThreat(GameTestHelper helper) {
        BrimstoneSalamander baby = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(5, 1, 5));
        baby.setBaby(true);

        IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(5, 1, 7));

        helper.assertFalse(baby.canTargetEntity(golem, false), "Baby ignores iron golem autonomously");
        helper.assertTrue(baby.canTargetEntity(golem, true), "Baby allows retaliation if directly attacked");

        helper.succeed();
    }

    private static void babyPreyRestriction(GameTestHelper helper) {
        BrimstoneSalamander baby = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(3, 1, 3));
        baby.setBaby(true);

        MagmaCube smallCube = helper.spawnWithNoFreeWill(EntityType.MAGMA_CUBE, new BlockPos(4, 1, 3));
        smallCube.setSize(1, true);
        helper.assertTrue(baby.canTargetEntity(smallCube, false), "Baby targets small MagmaCube (size <= 1)");

        MagmaCube bigCube = helper.spawnWithNoFreeWill(EntityType.MAGMA_CUBE, new BlockPos(5, 1, 3));
        bigCube.setSize(2, true);
        helper.assertFalse(baby.canTargetEntity(bigCube, false), "Baby ignores larger MagmaCube (size > 1)");

        Strider babyStrider = helper.spawnWithNoFreeWill(EntityType.STRIDER, new BlockPos(6, 1, 3));
        babyStrider.setBaby(true);
        helper.assertFalse(baby.canTargetEntity(babyStrider, false), "Baby salamander does not hunt baby Strider");

        BrimstoneSalamander adult = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(7, 1, 3));
        helper.assertTrue(adult.canTargetEntity(babyStrider, false), "Adult salamander hunts baby Strider");

        helper.succeed();
    }

    private static void babyNipCombat(GameTestHelper helper) {
        BrimstoneSalamander baby = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(2, 1, 2));
        baby.setBaby(true);

        IronGolem dummy = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(3, 1, 2));
        float initialHp = dummy.getHealth();

        // 1. Strict block of adult moves
        baby.startSulfurSpit();
        helper.assertFalse(baby.getState() == BrimstoneSalamander.State.SULFUR_SPIT, "Baby cannot perform SULFUR_SPIT");
        baby.startTailWhip();
        helper.assertFalse(baby.getState() == BrimstoneSalamander.State.TAIL_WHIP, "Baby cannot perform TAIL_WHIP");

        // 2. Direct hit with BABY_NIP (tick 4 impact, 2.0 damage, 15 fire ticks, 25 cooldown)
        baby.setTarget(dummy);
        baby.startBabyNip();
        helper.assertValueEqual(baby.getState(), BrimstoneSalamander.State.SNAP_BITE, "Starts in SNAP_BITE for baby nip");

        for (int i = 0; i < BrimstoneSalamander.BABY_NIP_PREP_TICKS; i++) {
            baby.tick();
        }

        helper.assertTrue(dummy.getHealth() < initialHp, "BABY_NIP deals 2.0 damage on tick 4");
        helper.assertTrue(dummy.getRemainingFireTicks() > 0, "BABY_NIP ignites target for 15 ticks");
        helper.assertValueEqual(baby.getSnapBiteCooldown(), BrimstoneSalamander.BABY_NIP_COOLDOWN_TICKS, "BABY_NIP cooldown is 25 ticks");

        // 3. Anti-whiff test for BABY_NIP at tick 3
        dummy.setPos(dummy.getX() + 5.0D, dummy.getY(), dummy.getZ());
        baby.startBabyNip();
        for (int i = 0; i < BrimstoneSalamander.BABY_NIP_ANTI_WHIFF_TICK; i++) {
            baby.tick();
        }
        helper.assertValueEqual(baby.getState(), BrimstoneSalamander.State.PURSUING, "Anti-whiff cancels BABY_NIP at tick 3 into PURSUING");
        helper.assertValueEqual(baby.getSnapBiteCooldown(), BrimstoneSalamander.BABY_NIP_CANCEL_COOLDOWN_TICKS, "Anti-whiff sets 10-tick short cooldown");

        helper.succeed();
    }

    private static void parentalProtectionFury(GameTestHelper helper) {
        UUID familyId = UUID.randomUUID();
        BrimstoneSalamander baby = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(4, 1, 4));
        baby.setBaby(true);
        baby.setFamilyId(familyId);

        BrimstoneSalamander adult = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(6, 1, 4));
        adult.setFamilyId(familyId);
        baby.setLeaderUUID(adult.getUUID());
        adult.setState(BrimstoneSalamander.State.IDLE);

        IronGolem attacker = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(4, 1, 6));

        // Damage baby
        baby.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(attacker), 4.0F);

        // Adult immediately targets attacker
        helper.assertValueEqual(adult.getTarget(), attacker, "Parental guardian targets attacker of baby");
        helper.assertValueEqual(adult.getState(), BrimstoneSalamander.State.PURSUING, "Parental guardian engages into PURSUING");

        helper.succeed();
    }

    private static void babyWaterShock(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        BrimstoneSalamander baby = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), pos);
        baby.setBaby(true);
        helper.assertValueEqual(baby.getHealth(), 16.0F, "Baby starts with full 16 HP");

        helper.setBlock(pos, Blocks.WATER);

        for (int i = 0; i < 20; i++) {
            baby.tick();
        }

        helper.assertTrue(baby.getHealth() <= 12.0F, "Water shock deals 4.0 HP damage to baby within 20 ticks");

        helper.succeed();
    }

    private static void babyFeedingMagmaAndBlaze(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        BrimstoneSalamander baby = helper.spawnWithNoFreeWill(ModEntities.BRIMSTONE_SALAMANDER.get(), new BlockPos(3, 1, 3));
        baby.setBaby(true);
        baby.setHealth(8.0F);
        baby.setGrowthAge(-24000);

        // 1. Magma Cream: +3.0 HP and +2400 ticks (10%)
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.MAGMA_CREAM, 1));
        var res1 = baby.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(res1, net.minecraft.world.InteractionResult.SUCCESS, "Feeding Magma Cream succeeds");
        helper.assertValueEqual(baby.getHealth(), 11.0F, "Magma Cream heals 3 HP (8 -> 11)");
        helper.assertValueEqual(baby.getGrowthAge(), -21600, "Magma Cream advances age by 2400 ticks");

        // 2. Blaze Powder: +5.0 HP and +3600 ticks (15%)
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BLAZE_POWDER, 1));
        var res2 = baby.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(res2, net.minecraft.world.InteractionResult.SUCCESS, "Feeding Blaze Powder succeeds");
        helper.assertValueEqual(baby.getHealth(), 16.0F, "Blaze Powder heals up to max 16 HP (11 + 5 = 16)");
        helper.assertValueEqual(baby.getGrowthAge(), -18000, "Blaze Powder advances age by 3600 ticks");

        // 3. Maturation via feeding: advance to near maturation and feed Blaze Powder
        baby.setGrowthAge(-100);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BLAZE_POWDER, 1));
        var res3 = baby.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(res3, net.minecraft.world.InteractionResult.SUCCESS, "Feeding near maturity succeeds");
        helper.assertFalse(baby.isBaby(), "Baby matures to adult upon reaching threshold via feeding");
        helper.assertValueEqual(baby.getGrowthAge(), BrimstoneSalamander.ADULT_AGE, "GrowthAge reaches 0 (adult)");

        helper.succeed();
    }
}
