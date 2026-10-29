package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.bestiary.Bestiary;
import com.codenamecoraline.client.lithophage.LithophageAnimations;
import com.codenamecoraline.client.lithophage.LithophageModel;
import com.codenamecoraline.entity.lithophage.AmberShard;
import com.codenamecoraline.entity.lithophage.Lithophage;
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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Server integration tests for the Lithophage, its combat system, spawn rules, loot and Bestiary integration. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class LithophageGameTests {
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
            Map.entry("lithophage_attributes_and_animations", LithophageGameTests::attributesAndAnimations),
            Map.entry("lithophage_dormant_awakening_and_vigilance", LithophageGameTests::dormantAwakeningAndVigilance),
            Map.entry("lithophage_pursuit_limit_and_territory", LithophageGameTests::pursuitLimitAndTerritory),
            Map.entry("lithophage_slam_attack_and_recovery", LithophageGameTests::slamAttackAndRecovery),
            Map.entry("lithophage_sweep_frontal_arc", LithophageGameTests::sweepFrontalArc),
            Map.entry("lithophage_amber_shard_and_wall_collision", LithophageGameTests::amberShardAndWallCollision),
            Map.entry("lithophage_death_cancels_combat_and_drops_loot", LithophageGameTests::deathAndLoot),
            Map.entry("lithophage_spawn_tables_and_cave_rules", LithophageGameTests::spawnTablesAndCaveRules),
            Map.entry("lithophage_spawn_egg_and_bestiary", LithophageGameTests::spawnEggAndBestiary),
            Map.entry("lithophage_seismic_resonance_pack_awakening", LithophageGameTests::seismicResonancePackAwakening));

    private LithophageGameTests() {}

    private static boolean enabled() {
        return Boolean.getBoolean("coraline.lithophageTests");
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
        var environment = event.registerEnvironment(id("lithophage_test_environment"));
        TESTS.forEach((name, fn) -> event.registerTest(id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                        new TestData<>(environment, id("basilisk_test_arena"), 220, 0, true))));
    }

    private static Lithophage spawnLithophage(GameTestHelper helper, BlockPos relativePos) {
        Lithophage mob = helper.spawn(ModEntities.LITHOPHAGE.get(), relativePos);
        mob.setHomePos(helper.absolutePos(relativePos));
        return mob;
    }

    private static LivingEntity dummyTarget(GameTestHelper helper, BlockPos relativePos) {
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, relativePos);
        pig.setNoGravity(true);
        pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(60.0D);
        pig.setHealth(60.0F);
        return pig;
    }

    private static void attributesAndAnimations(GameTestHelper helper) {
        Lithophage mob = helper.spawnWithNoFreeWill(ModEntities.LITHOPHAGE.get(), new BlockPos(3, 1, 3));
        helper.assertValueEqual(mob.getMaxHealth(), 36.0F, "Lithophage max health must be 36 HP");
        helper.assertValueEqual(mob.getAttributeValue(Attributes.ARMOR), 6.0D, "Lithophage armor");
        helper.assertValueEqual(mob.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.22D, "Lithophage movement speed");
        helper.assertValueEqual(mob.getAttributeValue(Attributes.ATTACK_DAMAGE), 5.0D, "Lithophage base attack damage");
        helper.assertValueEqual(mob.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.55D, "Lithophage knockback resistance");
        helper.assertValueEqual(mob.getAttributeValue(Attributes.FOLLOW_RANGE), 20.0D, "Lithophage max pursuit range");
        helper.assertValueEqual(ModEntities.LITHOPHAGE.get().getCategory(), MobCategory.MONSTER, "Lithophage category");

        // Verify that the model bakes cleanly and all animations bind without error.
        var root = LithophageModel.createBodyLayer().bakeRoot();
        var model = new LithophageModel(root);
        helper.assertTrue(model != null, "LithophageModel bakes all animations");
        helper.assertTrue(LithophageAnimations.IDLE != null && LithophageAnimations.DEATH != null
                && LithophageAnimations.FALL_ASLEEP != null, "All animations defined including fall_asleep");
        helper.succeed();
    }

    private static void dormantAwakeningAndVigilance(GameTestHelper helper) {
        Lithophage mob = spawnLithophage(helper, new BlockPos(3, 1, 5));
        helper.assertValueEqual(mob.getState(), Lithophage.State.DORMANT, "starts in DORMANT state");

        LivingEntity target = dummyTarget(helper, new BlockPos(8, 1, 5));
        mob.setTarget(target);
        helper.assertValueEqual(mob.getState(), Lithophage.State.AWAKENING, "detecting a threat initiates AWAKENING");

        boolean[] sawVigilant = new boolean[1];
        boolean[] sawPursuing = new boolean[1];
        boolean[] sawFallingAsleep = new boolean[1];
        helper.onEachTick(() -> {
            if (mob.getState() == Lithophage.State.VIGILANT) sawVigilant[0] = true;
            if (mob.getState() == Lithophage.State.PURSUING) sawPursuing[0] = true;
            if (mob.getState() == Lithophage.State.FALLING_ASLEEP) sawFallingAsleep[0] = true;
        });

        helper.runAfterDelay(42, () -> {
            helper.assertTrue(sawVigilant[0], "transitions through VIGILANT after AWAKENING completes");
            helper.assertTrue(sawPursuing[0] || mob.getState() == Lithophage.State.COMBAT,
                    "proceeds to PURSUING or COMBAT after vigilance assessment");
            target.discard();
        });

        helper.runAfterDelay(140, () -> {
            helper.assertTrue(sawFallingAsleep[0], "transitions through FALLING_ASLEEP before dormancy");
            helper.assertValueEqual(mob.getState(), Lithophage.State.DORMANT,
                    "returns to DORMANT after threat disappears and vigilance times out");
            helper.succeed();
        });
    }

    private static void pursuitLimitAndTerritory(GameTestHelper helper) {
        Lithophage mob = spawnLithophage(helper, new BlockPos(3, 1, 5));
        LivingEntity target = dummyTarget(helper, new BlockPos(7, 1, 5));
        mob.setState(Lithophage.State.PURSUING);
        mob.setTarget(target);

        helper.runAfterDelay(5, () -> {
            helper.assertTrue(mob.getTarget() == target, "tracks target within 20-block territory");
            // Move target 25 blocks away from homePos (exceeding MAX_PURSUIT_DISTANCE = 20).
            target.setPos(helper.absoluteVec(new Vec3(29, 1, 5)));
        });

        helper.runAfterDelay(15, () -> {
            helper.assertTrue(mob.getTarget() == null, "drops target that exceeds 20-block pursuit limit");
            helper.assertValueEqual(mob.getState(), Lithophage.State.VIGILANT, "switches to VIGILANT after dropping pursuit");
            target.discard();
            helper.succeed();
        });
    }

    private static void slamAttackAndRecovery(GameTestHelper helper) {
        Lithophage mob = spawnLithophage(helper, new BlockPos(4, 1, 5));
        LivingEntity target = dummyTarget(helper, new BlockPos(5, 1, 5));
        float initialHealth = target.getHealth();

        // Face the target along +X (yaw = -90).
        mob.setYRot(-90.0F);
        mob.setYHeadRot(-90.0F);
        mob.setState(Lithophage.State.VIGILANT);
        mob.setTarget(target);
        helper.assertTrue(mob.startAttack(Lithophage.AttackType.SLAM), "starts SLAM attack");
        helper.assertValueEqual(mob.getState(), Lithophage.State.COMBAT, "in COMBAT state");

        helper.runAfterDelay(8, () -> {
            helper.assertValueEqual(target.getHealth(), initialHealth, "no damage before SLAM_WINDUP_TICKS (12)");
        });

        helper.runAfterDelay(14, () -> {
            helper.assertValueEqual(target.getHealth(), initialHealth - Lithophage.SLAM_DAMAGE,
                    "SLAM deals 5 HP at impact frame");
        });

        helper.runAfterDelay(18, () -> {
            helper.assertValueEqual(mob.getState(), Lithophage.State.RECOVERY,
                    "enters RECOVERY vulnerability window after SLAM");
            helper.assertTrue(!mob.startAttack(Lithophage.AttackType.SLAM),
                    "cannot start a new attack during RECOVERY");
            helper.succeed();
        });
    }

    private static void sweepFrontalArc(GameTestHelper helper) {
        Lithophage mob = spawnLithophage(helper, new BlockPos(5, 1, 5));
        // Face East (+X, yaw = -90).
        mob.setYRot(-90.0F);
        mob.setYHeadRot(-90.0F);

        LivingEntity frontVictim = dummyTarget(helper, new BlockPos(7, 1, 5)); // In front (+X)
        LivingEntity rearVictim = dummyTarget(helper, new BlockPos(3, 1, 5));  // Behind (-X)
        float initialFront = frontVictim.getHealth();
        float initialRear = rearVictim.getHealth();

        mob.setState(Lithophage.State.VIGILANT);
        mob.setTarget(frontVictim);
        helper.assertTrue(mob.startAttack(Lithophage.AttackType.SWEEP), "starts SWEEP attack");

        helper.runAfterDelay(12, () -> {
            helper.assertValueEqual(frontVictim.getHealth(), initialFront, "no damage before SWEEP_WINDUP_TICKS (16)");
        });

        helper.runAfterDelay(18, () -> {
            helper.assertValueEqual(frontVictim.getHealth(), initialFront - Lithophage.SWEEP_DAMAGE,
                    "front victim in arc takes 4 HP from SWEEP");
            helper.assertValueEqual(rearVictim.getHealth(), initialRear,
                    "rear victim outside frontal arc takes 0 damage");
            helper.succeed();
        });
    }

    private static void amberShardAndWallCollision(GameTestHelper helper) {
        Lithophage shooter = spawnLithophage(helper, new BlockPos(2, 1, 3));
        shooter.setYRot(-90.0F);
        shooter.setYHeadRot(-90.0F);

        // Place another Lithophage in the line of fire to verify no friendly fire.
        Lithophage ally = helper.spawnWithNoFreeWill(ModEntities.LITHOPHAGE.get(), new BlockPos(4, 1, 3));
        ally.setNoGravity(true);
        float allyInitial = ally.getHealth();

        LivingEntity target = dummyTarget(helper, new BlockPos(7, 1, 3));
        float targetInitial = target.getHealth();

        shooter.setState(Lithophage.State.VIGILANT);
        shooter.setTarget(target);
        helper.assertTrue(shooter.startAttack(Lithophage.AttackType.SHARD), "starts SHARD attack");

        helper.runAfterDelay(28, () -> {
            helper.assertValueEqual(ally.getHealth(), allyInitial, "AmberShard never harms Lithophages");
            helper.assertValueEqual(target.getHealth(), targetInitial - AmberShard.DAMAGE,
                    "AmberShard hits target for 4 HP");

            // Now verify solid walls block AmberShard.
            helper.setBlock(new BlockPos(5, 1, 7), Blocks.STONE);
            helper.setBlock(new BlockPos(5, 2, 7), Blocks.STONE);
            LivingEntity walledTarget = dummyTarget(helper, new BlockPos(7, 1, 7));
            AmberShard walledShard = new AmberShard(helper.getLevel(), shooter);
            Vec3 start = helper.absoluteVec(new Vec3(3.0D, 2.0D, 7.5D));
            walledShard.setPos(start.x, start.y, start.z);
            walledShard.shoot(1.0D, 0.0D, 0.0D, 1.15F, 0.0F);
            helper.getLevel().addFreshEntity(walledShard);

            helper.runAfterDelay(15, () -> {
                helper.assertTrue(walledShard.isRemoved(), "AmberShard shatters on solid wall");
                helper.assertValueEqual(walledTarget.getHealth(), walledTarget.getMaxHealth(),
                        "walled target takes no shard damage");
                helper.succeed();
            });
        });
    }

    private static void deathAndLoot(GameTestHelper helper) {
        Lithophage dyingMob = helper.spawnWithNoFreeWill(ModEntities.LITHOPHAGE.get(), new BlockPos(4, 1, 5));
        dyingMob.setNoGravity(true);
        dyingMob.setHomePos(helper.absolutePos(new BlockPos(4, 1, 5)));
        dyingMob.setYRot(-90.0F);
        dyingMob.setYHeadRot(-90.0F);
        LivingEntity target = dummyTarget(helper, new BlockPos(5, 1, 5));
        float initialHealth = target.getHealth();

        dyingMob.setState(Lithophage.State.VIGILANT);
        dyingMob.setTarget(target);
        dyingMob.startAttack(Lithophage.AttackType.SLAM);

        // Kill the Lithophage mid-windup (at tick 5, before tick 12 impact).
        helper.runAfterDelay(5, () -> {
            dyingMob.hurtServer(helper.getLevel(), dyingMob.damageSources().genericKill(), 1000.0F);
            helper.assertValueEqual(dyingMob.getState(), Lithophage.State.DYING, "enters DYING state immediately");
            helper.assertValueEqual(dyingMob.getActiveAttack(), Lithophage.AttackType.NONE, "cancels active attack on death");
        });

        // Also kill several seeded Lithophages to verify loot table drops both items without invalid items.
        for (int i = 0; i < 18; i++) {
            Lithophage lootMob = helper.spawnWithNoFreeWill(ModEntities.LITHOPHAGE.get(), new BlockPos(6, 1, 3));
            lootMob.getRandom().setSeed(4200L + i * 997L);
            lootMob.hurtServer(helper.getLevel(), lootMob.damageSources().genericKill(), 1000.0F);
        }

        helper.runAfterDelay(20, () -> {
            helper.assertValueEqual(target.getHealth(), initialHealth, "dead Lithophage never deals delayed strike damage");
            var items = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                    helper.absoluteAABB(new AABB(0, 0, 0, 10, 5, 10)).inflate(1.5D));
            helper.assertTrue(items.stream().anyMatch(item -> item.getItem().is(ModItems.ANCIENT_CLAY_FRAGMENT.get())),
                    "drops Ancient Clay Fragment from loot table");
            helper.assertTrue(items.stream().anyMatch(item -> item.getItem().is(ModItems.MINERAL_AMBER_CORE.get())),
                    "drops Mineral Amber Core from loot table");
            helper.succeed();
        });
    }

    private static void setBiome(GameTestHelper helper, BlockPos absPos, ResourceKey<Biome> key) {
        var level = helper.getLevel();
        var biome = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(key);
        for (int dx = -16; dx <= 16; dx += 16) {
            for (int dz = -16; dz <= 16; dz += 16) {
                level.getChunkAt(absPos.offset(dx, 0, dz)).fillBiomesFromNoise((x, y, z, sampler) -> biome,
                        level.getChunkSource().randomState().sampler());
            }
        }
    }

    private static void spawnTablesAndCaveRules(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);

        var lushEntries = biomes.getOrThrow(Biomes.LUSH_CAVES).value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap()
                .stream().filter(e -> e.value().type() == ModEntities.LITHOPHAGE.get()).toList();
        helper.assertValueEqual(lushEntries.size(), 1, "Lush Caves has Lithophage spawn entry");
        helper.assertValueEqual(lushEntries.getFirst().weight(), 28, "Lush Caves primary weight");
        helper.assertValueEqual(lushEntries.getFirst().value().minCount(), 1, "Lush Caves minCount 1");
        helper.assertValueEqual(lushEntries.getFirst().value().maxCount(), 3, "Lush Caves maxCount 3");

        var dripEntries = biomes.getOrThrow(Biomes.DRIPSTONE_CAVES).value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap()
                .stream().filter(e -> e.value().type() == ModEntities.LITHOPHAGE.get()).toList();
        helper.assertValueEqual(dripEntries.size(), 1, "Dripstone Caves has Lithophage spawn entry");
        helper.assertValueEqual(dripEntries.getFirst().weight(), 16, "Dripstone Caves secondary weight");
        helper.assertValueEqual(dripEntries.getFirst().value().minCount(), 1, "Dripstone Caves minCount 1");
        helper.assertValueEqual(dripEntries.getFirst().value().maxCount(), 2, "Dripstone Caves maxCount 2");

        for (var excluded : List.of(Biomes.PLAINS, Biomes.SWAMP, Biomes.DEEP_DARK)) {
            var list = biomes.getOrThrow(excluded).value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap()
                    .stream().filter(e -> e.value().type() == ModEntities.LITHOPHAGE.get()).toList();
            helper.assertTrue(list.isEmpty(), "Lithophage does not spawn in " + excluded.identifier());
        }

        // Build a closed dark cave chamber at Y = 0 (within [-20, 20]) in the same chunk.
        var level = helper.getLevel();
        BlockPos baseAbs = helper.absolutePos(new BlockPos(5, 1, 5));
        BlockPos cavePos = new BlockPos(baseAbs.getX(), 0, baseAbs.getZ());
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -1; dy <= 4; dy++) {
                    BlockPos p = cavePos.offset(dx, dy, dz);
                    boolean shell = dy == -1 || dy == 4 || Math.abs(dx) == 2 || Math.abs(dz) == 2;
                    level.setBlock(p, shell ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        helper.runAfterDelay(15, () -> {
            setBiome(helper, cavePos, Biomes.LUSH_CAVES);
            helper.assertTrue(Lithophage.checkSpawnRules(ModEntities.LITHOPHAGE.get(), level,
                    EntitySpawnReason.NATURAL, cavePos, level.getRandom()),
                    "allows spawn in dark Lush Caves at Y = 0");

            setBiome(helper, cavePos, Biomes.DRIPSTONE_CAVES);
            helper.assertTrue(Lithophage.checkSpawnRules(ModEntities.LITHOPHAGE.get(), level,
                    EntitySpawnReason.NATURAL, cavePos, level.getRandom()),
                    "allows spawn in dark Dripstone Caves at Y = 0");

            // Reject outside Y = [-20, 20] (baseAbs is at Y = -58).
            helper.assertTrue(!Lithophage.checkSpawnRules(ModEntities.LITHOPHAGE.get(), level,
                    EntitySpawnReason.NATURAL, baseAbs, level.getRandom()),
                    "rejects spawn outside Y = [-20, 20]");

            // Reject non-habitat biome.
            setBiome(helper, cavePos, Biomes.PLAINS);
            helper.assertTrue(!Lithophage.checkSpawnRules(ModEntities.LITHOPHAGE.get(), level,
                    EntitySpawnReason.NATURAL, cavePos, level.getRandom()),
                    "rejects spawn in Plains biome");

            // Reject insufficient vertical space (block at head height dy = 1).
            setBiome(helper, cavePos, Biomes.LUSH_CAVES);
            level.setBlock(cavePos.above(), Blocks.DEEPSLATE.defaultBlockState(), 3);
            helper.assertTrue(!Lithophage.checkSpawnRules(ModEntities.LITHOPHAGE.get(), level,
                    EntitySpawnReason.NATURAL, cavePos, level.getRandom()),
                    "rejects spawn when 2.1-block clearance is obstructed");

            // Clean up temporary chamber blocks at Y = 0.
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = -1; dy <= 4; dy++) {
                        level.setBlock(cavePos.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
            helper.succeed();
        });
    }

    private static void spawnEggAndBestiary(GameTestHelper helper) {
        ItemStack eggStack = new ItemStack(ModItems.LITHOPHAGE_SPAWN_EGG.get());
        helper.assertTrue(eggStack.getItem() instanceof SpawnEggItem, "lithophage_spawn_egg is a SpawnEggItem");
        SpawnEggItem egg = (SpawnEggItem) eggStack.getItem();
        helper.assertValueEqual(egg.getType(eggStack), ModEntities.LITHOPHAGE.get(),
                "spawn egg spawns codenamecoraline:lithophage");

        helper.assertTrue(Bestiary.ENTRIES.contains(Bestiary.LITHOPHAGE), "Bestiary includes LITHOPHAGE entry");
        helper.assertValueEqual(Bestiary.LITHOPHAGE.id(), "lithophage", "Bestiary entry ID is lithophage");
        helper.assertValueEqual(Bestiary.ENTRIES.stream().filter(e -> e.id().equals("lithophage")).count(), 1L,
                "Bestiary entry is not duplicated");
        helper.assertTrue(Bestiary.ENTRIES.contains(Bestiary.REDCAP) && Bestiary.ENTRIES.contains(Bestiary.BASILISK),
                "Existing Bestiary entries are preserved");
        helper.succeed();
    }

    private static void seismicResonancePackAwakening(GameTestHelper helper) {
        Lithophage mobA = spawnLithophage(helper, new BlockPos(3, 1, 3));
        Lithophage mobB = spawnLithophage(helper, new BlockPos(3, 1, 8)); // 5 blocks away (<= 8)
        Lithophage mobC = spawnLithophage(helper, new BlockPos(17, 1, 3)); // 14 blocks away (> 8)

        helper.assertValueEqual(mobA.getState(), Lithophage.State.DORMANT, "mobA starts DORMANT");
        helper.assertValueEqual(mobB.getState(), Lithophage.State.DORMANT, "mobB starts DORMANT");
        helper.assertValueEqual(mobC.getState(), Lithophage.State.DORMANT, "mobC starts DORMANT");

        LivingEntity dummyThreat = dummyTarget(helper, new BlockPos(1, 1, 3));

        // Awakening mobA sends seismic tremors through the ground.
        mobA.triggerAwakening(dummyThreat);

        helper.assertValueEqual(mobA.getState(), Lithophage.State.AWAKENING, "mobA transitions to AWAKENING");
        helper.assertValueEqual(mobB.getState(), Lithophage.State.AWAKENING,
                "mobB awakened by seismic resonance within 8-block radius");
        helper.assertValueEqual(mobC.getState(), Lithophage.State.DORMANT,
                "mobC remains DORMANT beyond 8-block resonance radius");

        dummyThreat.discard();
        helper.succeed();
    }
}
