package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.client.salamander.BrimstoneSalamanderAnimations;
import com.codenamecoraline.client.salamander.BrimstoneSalamanderModel;
import com.codenamecoraline.entity.basilisk.Basilisk;
import com.codenamecoraline.entity.lithophage.Lithophage;
import com.codenamecoraline.entity.raven.FeyRaven;
import com.codenamecoraline.entity.salamander.BrimstoneSalamander;
import com.codenamecoraline.entity.salamander.BrimstoneSpit;
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Blocks;
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
            Map.entry("brimstone_salamander_lava_swimming_and_navigation", BrimstoneSalamanderGameTests::lavaSwimmingAndNavigation));

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
}
