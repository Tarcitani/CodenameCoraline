package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.client.raven.FeyRavenAnimations;
import com.codenamecoraline.client.raven.FeyRavenModel;
import com.codenamecoraline.entity.raven.FeyRaven;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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

/** Server integration tests for the Fey Raven foundation, visual model baking, and companion AI (Slice 3). */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class FeyRavenGameTests {
    private static final int FLOOR_Y = 2;

    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
            Map.entry("fey_raven_attributes_and_registration", FeyRavenGameTests::attributesAndRegistration),
            Map.entry("fey_raven_spawn_rules_and_immunities", FeyRavenGameTests::spawnRulesAndImmunities),
            Map.entry("fey_raven_taming_and_healing", FeyRavenGameTests::tamingAndHealing),
            Map.entry("fey_raven_commands_follow_sit_sentinel_and_teleport", FeyRavenGameTests::commandsFollowSitSentinelAndTeleport),
            Map.entry("fey_raven_loyalty_defense_and_friendly_fire", FeyRavenGameTests::loyaltyDefenseAndFriendlyFire),
            Map.entry("fey_raven_mystic_sentinel_and_scroll_inventory", FeyRavenGameTests::mysticSentinelAndScrollInventory),
            Map.entry("fey_raven_owner_and_scroll_persistence", FeyRavenGameTests::ownerAndScrollPersistence));

    private FeyRavenGameTests() {}

    private static boolean enabled() {
        return Boolean.getBoolean("coraline.feyRavenTests");
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
        var environment = event.registerEnvironment(id("fey_raven_test_environment"));
        TESTS.forEach((name, fn) -> event.registerTest(id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                        new TestData<>(environment, id("basilisk_test_arena"), 160, 0, true))));
    }

    private static void prepareArena(GameTestHelper helper) {
        for (int x = 1; x <= 11; x++) {
            for (int z = 1; z <= 11; z++) {
                helper.setBlock(new BlockPos(x, FLOOR_Y, z), Blocks.STONE);
                for (int y = FLOOR_Y + 1; y <= FLOOR_Y + 6; y++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }
    }

    private static FeyRaven copyViaNbt(GameTestHelper helper, FeyRaven raven) {
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        raven.saveWithoutId(out);
        FeyRaven loaded = ModEntities.FEY_RAVEN.get().create(helper.getLevel(), EntitySpawnReason.LOAD);
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), out.buildResult()));
        return loaded;
    }

    private static void attributesAndRegistration(GameTestHelper helper) {
        FeyRaven raven = helper.spawnWithNoFreeWill(ModEntities.FEY_RAVEN.get(), new BlockPos(3, 1, 3));
        helper.assertValueEqual(raven.getMaxHealth(), 14.0F, "Fey Raven max health must be 14 HP");
        helper.assertValueEqual(raven.getAttributeValue(Attributes.ARMOR), 0.0D, "Fey Raven armor");
        helper.assertValueEqual(raven.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.28D, "Fey Raven movement speed");
        helper.assertValueEqual(raven.getAttributeValue(Attributes.FLYING_SPEED), 0.40D, "Fey Raven flying speed");
        helper.assertValueEqual(raven.getAttributeValue(Attributes.ATTACK_DAMAGE), 2.0D, "Fey Raven base attack damage");
        helper.assertValueEqual(raven.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.1D, "Fey Raven knockback resistance");
        helper.assertValueEqual(raven.getAttributeValue(Attributes.FOLLOW_RANGE), 24.0D, "Fey Raven follow range");
        helper.assertValueEqual(ModEntities.FEY_RAVEN.get().getCategory(), MobCategory.CREATURE, "Fey Raven category");
        helper.assertValueEqual(raven.getState(), FeyRaven.State.WILD_IDLE, "Fey Raven initial state");
        helper.assertTrue(ModItems.FEY_RAVEN_SPAWN_EGG.get() instanceof SpawnEggItem, "Fey Raven spawn egg registered");
        helper.assertTrue(raven.isFood(new ItemStack(Items.GLOW_BERRIES)), "Glow berries must be valid food");
        helper.assertTrue(raven.isFood(new ItemStack(Items.SWEET_BERRIES)), "Sweet berries must be valid food");
        helper.assertFalse(raven.isFood(new ItemStack(Items.ROTTEN_FLESH)), "Rotten flesh must not be valid food");

        var root = FeyRavenModel.createBodyLayer().bakeRoot();
        var model = new FeyRavenModel(root);
        helper.assertTrue(model != null, "FeyRavenModel bakes all joints and animations cleanly");
        helper.assertTrue(
                FeyRavenAnimations.IDLE_BREATHE != null
                        && FeyRavenAnimations.IDLE_HEAD_TILT != null
                        && FeyRavenAnimations.IDLE_PREEN != null
                        && FeyRavenAnimations.IDLE_CAW != null
                        && FeyRavenAnimations.WALK != null
                        && FeyRavenAnimations.FLY_FLAP != null
                        && FeyRavenAnimations.FLY_GLIDE != null
                        && FeyRavenAnimations.PECK_ATTACK != null
                        && FeyRavenAnimations.HURT != null
                        && FeyRavenAnimations.DEATH != null,
                "All 10 Fey Raven animations must be defined");
        helper.succeed();
    }

    private static void spawnRulesAndImmunities(GameTestHelper helper) {
        FeyRaven raven = helper.spawnWithNoFreeWill(ModEntities.FEY_RAVEN.get(), new BlockPos(3, 1, 3));
        helper.assertFalse(
                raven.causeFallDamage(12.0D, 1.0F, helper.getLevel().damageSources().fall()),
                "Fey Raven must ignore fall damage");
        helper.assertFalse(
                raven.canBeAffected(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0)),
                "Fey Raven must be immune to Blindness");
        helper.assertFalse(
                raven.canBeAffected(new MobEffectInstance(MobEffects.DARKNESS, 100, 0)),
                "Fey Raven must be immune to Darkness");

        BlockPos lowPos = new BlockPos(helper.absolutePos(new BlockPos(5, 1, 5)).getX(), 50,
                helper.absolutePos(new BlockPos(5, 1, 5)).getZ());
        helper.assertFalse(
                FeyRaven.checkSpawnRules(
                        ModEntities.FEY_RAVEN.get(),
                        helper.getLevel(),
                        EntitySpawnReason.NATURAL,
                        lowPos,
                        helper.getLevel().getRandom()),
                "Fey Raven must reject natural spawn below Y=64");

        BlockPos absBase = helper.absolutePos(new BlockPos(6, 2, 6));
        BlockPos highPerch = new BlockPos(absBase.getX(), Math.max(64, absBase.getY()), absBase.getZ());
        helper.getLevel().setBlockAndUpdate(highPerch.below(), Blocks.OAK_LEAVES.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(highPerch, Blocks.AIR.defaultBlockState());

        helper.assertTrue(
                FeyRaven.checkSpawnRules(
                        ModEntities.FEY_RAVEN.get(),
                        helper.getLevel(),
                        EntitySpawnReason.NATURAL,
                        highPerch,
                        helper.getLevel().getRandom()),
                "Fey Raven must allow spawn on leaves at Y >= 64 when fewer than 3 ravens are nearby");
        helper.succeed();
    }

    private static void tamingAndHealing(GameTestHelper helper) {
        prepareArena(helper);
        FeyRaven raven = helper.spawnWithNoFreeWill(ModEntities.FEY_RAVEN.get(), new BlockPos(4, FLOOR_Y + 1, 4));
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLOW_BERRIES, 32));
        raven.getRandom().setSeed(20261105L);

        int attempts = 0;
        while (!raven.isTame() && attempts < 32) {
            raven.mobInteract(owner, InteractionHand.MAIN_HAND);
            attempts++;
        }
        helper.assertTrue(raven.isTame() && raven.isOwnedBy(owner), "Glow berries must tame the Fey Raven to the player's UUID");
        helper.assertValueEqual(owner.getMainHandItem().getCount(), 32 - attempts, "One glow berry consumed per attempt");
        helper.assertValueEqual(raven.getCommandMode(), FeyRaven.CommandMode.FOLLOW, "Tamed raven starts in FOLLOW mode");
        helper.assertValueEqual(raven.getState(), FeyRaven.State.FOLLOWING, "Tamed raven starts in FOLLOWING state");

        // Healing with Sweet Berries (+4 HP)
        raven.setHealth(8.0F);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SWEET_BERRIES, 4));
        raven.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(raven.getHealth(), 12.0F, "Sweet berries must heal 4.0 HP");

        // Stranger cannot command or feed the companion
        Player stranger = helper.makeMockPlayer(GameType.SURVIVAL);
        stranger.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        raven.mobInteract(stranger, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(raven.getCommandMode(), FeyRaven.CommandMode.FOLLOW, "Stranger cannot change command mode");
        helper.succeed();
    }

    private static void commandsFollowSitSentinelAndTeleport(GameTestHelper helper) {
        prepareArena(helper);
        FeyRaven raven = helper.spawnWithNoFreeWill(ModEntities.FEY_RAVEN.get(), new BlockPos(4, FLOOR_Y + 1, 4));
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        owner.setPos(helper.absoluteVec(new Vec3(6.5D, FLOOR_Y + 1, 6.5D)));
        raven.tame(owner);

        // Cycle FOLLOW -> SIT -> SENTINEL -> FOLLOW with empty hand
        owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        raven.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(raven.getCommandMode(), FeyRaven.CommandMode.SIT, "First click switches to SIT");
        helper.assertTrue(raven.isOrderedToSit() && raven.getState() == FeyRaven.State.SITTING, "Raven sits on command");

        raven.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(raven.getCommandMode(), FeyRaven.CommandMode.SENTINEL, "Second click switches to SENTINEL");
        helper.assertValueEqual(raven.getState(), FeyRaven.State.SENTINEL, "Raven enters SENTINEL state");
        helper.assertTrue(raven.isAirborne(), "Sentinel mode is airborne");

        raven.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(raven.getCommandMode(), FeyRaven.CommandMode.FOLLOW, "Third click returns to FOLLOW");

        // Safe teleport from > 20 blocks away
        Vec3 farPos = owner.position().add(28.0D, 0.0D, 0.0D);
        raven.setPos(farPos);
        helper.assertTrue(raven.teleportNearOwner(), "Raven teleports safely near owner when > 20 blocks away");
        helper.assertTrue(raven.distanceToSqr(owner) < 25.0D, "Teleported raven lands within 5 blocks of owner");

        // Non-destructive shoulder perching preserves UUID, name, health, and owner
        var originalUuid = raven.getUUID();
        raven.setCustomName(Component.literal("Hugin"));
        raven.setHealth(11.0F);
        helper.assertTrue(raven.landOnShoulder(owner), "Raven lands on owner's shoulder");
        helper.assertTrue(raven.isOnShoulder(), "State is PERCHED_ON_SHOULDER");
        helper.assertTrue(raven.getUUID().equals(originalUuid) && raven.getHealth() == 11.0F, "Shoulder perch preserves identity and HP");
        raven.leaveShoulder();
        helper.assertFalse(raven.isOnShoulder(), "Raven leaves shoulder cleanly");
        helper.succeed();
    }

    private static void loyaltyDefenseAndFriendlyFire(GameTestHelper helper) {
        prepareArena(helper);
        FeyRaven raven = helper.spawn(ModEntities.FEY_RAVEN.get(), new BlockPos(4, FLOOR_Y + 1, 5));
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        owner.setPos(helper.absoluteVec(new Vec3(5.5D, FLOOR_Y + 1, 5.5D)));
        raven.tame(owner);

        // Friendly fire immunity: owner cannot hurt the raven, and raven never targets owner or same-owner wolf
        helper.assertTrue(raven.isProtectedAlly(owner) && !raven.canAttack(owner), "Owner is protected ally");
        helper.assertFalse(
                raven.hurtServer(helper.getLevel(), raven.damageSources().playerAttack(owner), 6.0F),
                "Owner attacks must not damage the tamed Fey Raven");

        var wolf = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(6, FLOOR_Y + 1, 4));
        wolf.tame(owner);
        helper.assertTrue(raven.isProtectedAlly(wolf) && !raven.canAttack(wolf), "Same-owner wolf is protected ally");

        // Uninvolved peaceful animal is never targeted
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(7, FLOOR_Y + 1, 4));
        helper.assertFalse(raven.validDefensiveTarget(pig), "Uninvolved pig must not be targeted");

        // Hostile attacking the owner becomes a valid target and takes peck damage at tick 8
        var husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(5, FLOOR_Y + 1, 6));
        husk.setNoGravity(true);
        float initialHuskHp = husk.getHealth();
        owner.setLastHurtByMob(husk);
        helper.assertTrue(raven.validDefensiveTarget(husk), "Hostile that attacked owner is a valid defensive target");

        raven.setPos(husk.position().add(0.0D, 0.5D, -1.2D));
        raven.setTarget(husk);
        for (int t = 0; t < 10; t++) {
            raven.tick();
        }
        helper.assertTrue(husk.getHealth() < initialHuskHp, "4-phase peck attack must deal damage at tick 8");
        helper.succeed();
    }

    private static void mysticSentinelAndScrollInventory(GameTestHelper helper) {
        prepareArena(helper);
        FeyRaven raven = helper.spawnWithNoFreeWill(ModEntities.FEY_RAVEN.get(), new BlockPos(5, FLOOR_Y + 1, 5));
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        owner.setPos(helper.absoluteVec(new Vec3(5.5D, FLOOR_Y + 1, 5.5D)));
        raven.tame(owner);

        // 1-slot Messenger Scroll exchange via Sneak + Right-Click
        owner.setShiftKeyDown(true);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WRITABLE_BOOK, 1));
        raven.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertTrue(raven.getScrollItem().is(Items.WRITABLE_BOOK), "Sneak + right-click stores book in scroll case");
        helper.assertTrue(owner.getMainHandItem().isEmpty(), "Item removed from owner's hand");

        // Mystic Sentinel reveals an invisible hostile within 16 blocks with Glowing
        var invisibleZombie = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(8, FLOOR_Y + 1, 5));
        invisibleZombie.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0));
        helper.assertTrue(raven.triggerMysticSentinelScan(), "Mystic Sentinel detects invisible hostile within 16 blocks");
        helper.assertTrue(invisibleZombie.hasEffect(MobEffects.GLOWING), "Invisible hostile receives Glowing effect");
        helper.assertValueEqual(raven.getState(), FeyRaven.State.ALERTING, "Raven enters ALERTING state");
        helper.assertValueEqual(raven.getAlertCooldown(), FeyRaven.MYSTIC_SENTINEL_COOLDOWN, "30-second cooldown started");

        // Death drops the stored scroll item intact
        raven.hurtServer(helper.getLevel(), helper.getLevel().damageSources().genericKill(), 100.0F);
        AABB dropArea = new AABB(helper.absolutePos(new BlockPos(5, FLOOR_Y + 1, 5))).inflate(4.0D);
        boolean droppedBook = helper.getLevel().getEntitiesOfClass(ItemEntity.class, dropArea).stream()
                .anyMatch(item -> item.getItem().is(Items.WRITABLE_BOOK));
        helper.assertTrue(droppedBook, "Stored scroll item must drop intact when the raven dies");
        helper.succeed();
    }

    private static void ownerAndScrollPersistence(GameTestHelper helper) {
        prepareArena(helper);
        FeyRaven raven = helper.spawnWithNoFreeWill(ModEntities.FEY_RAVEN.get(), new BlockPos(5, FLOOR_Y + 1, 5));
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        raven.tame(owner);
        raven.setCommandMode(FeyRaven.CommandMode.SENTINEL);
        raven.setScrollItem(new ItemStack(Items.EMERALD, 1));
        raven.setAlertCooldown(240);
        raven.setHealth(9.0F);

        FeyRaven loaded = copyViaNbt(helper, raven);
        helper.assertTrue(
                loaded.isTame() && loaded.getOwnerReference() != null
                        && loaded.getOwnerReference().getUUID().equals(owner.getUUID()),
                "Owner UUID must survive NBT serialization");
        helper.assertValueEqual(loaded.getCommandMode(), FeyRaven.CommandMode.SENTINEL, "CommandMode persisted");
        helper.assertValueEqual(loaded.getState(), FeyRaven.State.SENTINEL, "RavenState persisted");
        helper.assertTrue(loaded.getScrollItem().is(Items.EMERALD), "ScrollItem persisted");
        helper.assertValueEqual(loaded.getAlertCooldown(), 240, "AlertCooldown persisted");
        helper.assertValueEqual(loaded.getHealth(), 9.0F, "Health persisted");
        helper.succeed();
    }
}
