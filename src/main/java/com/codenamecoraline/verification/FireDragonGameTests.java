package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.firedragon.FireDragon;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import com.codenamecoraline.entity.firedragon.FireDragonAnimation;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Server-only integration coverage for the baby foundation, with no client model dependency. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class FireDragonGameTests {
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
            Map.entry("fire_dragon_registration", FireDragonGameTests::registration),
            Map.entry("fire_dragon_spawn", FireDragonGameTests::spawn),
            Map.entry("fire_dragon_damage", FireDragonGameTests::damage),
            Map.entry("fire_dragon_appearance_persistence", FireDragonGameTests::appearancePersistence),
            Map.entry("fire_dragon_visual_idles", FireDragonGameTests::visualIdles),
            Map.entry("fire_dragon_taming_food_and_commands", FireDragonGameTests::tamingFoodAndCommands),
            Map.entry("fire_dragon_owner_persistence_and_protection", FireDragonGameTests::ownerPersistenceAndProtection),
            Map.entry("fire_dragon_shoulder_controls", FireDragonGameTests::shoulderControls),
            Map.entry("fire_dragon_fireball_cooked_loot", FireDragonGameTests::fireballCookedLoot),
            Map.entry("fire_dragon_shoulder_no_push", FireDragonGameTests::shoulderNoPush),
            Map.entry("fire_dragon_repeated_hits_retaliation", FireDragonGameTests::repeatedHitsRetaliation),
            Map.entry("fire_dragon_predictive_aim", FireDragonGameTests::predictiveAim));

    private FireDragonGameTests() {}

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, name);
    }

    @SubscribeEvent
    public static void functions(RegisterEvent event) {
        if (!Boolean.getBoolean("coraline.fireDragonTests")) return;
        event.register(Registries.TEST_FUNCTION,
                registry -> TESTS.forEach((name, fn) -> registry.register(id(name), fn)));
    }

    @SubscribeEvent
    public static void tests(RegisterGameTestsEvent event) {
        if (!Boolean.getBoolean("coraline.fireDragonTests")) return;
        var environment = event.registerEnvironment(id("fire_dragon_test_environment"));
        TESTS.forEach((name, fn) -> event.registerTest(id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                        new TestData<>(environment, id("basilisk_test_arena"), 240, 0, true))));
    }

    private static void registration(GameTestHelper helper) {
        FireDragon dragon = helper.spawnWithNoFreeWill(ModEntities.FIRE_DRAGON.get(), new BlockPos(3, 2, 3));
        helper.assertValueEqual(dragon.getMaxHealth(), 20.0F, "Health");
        helper.assertValueEqual(dragon.getAttributeValue(Attributes.ARMOR), 2.0D, "Armor");
        helper.assertValueEqual(dragon.getAttributeValue(Attributes.ARMOR_TOUGHNESS), 0.0D, "Toughness");
        helper.assertValueEqual(dragon.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.25D, "Ground speed");
        helper.assertValueEqual(dragon.getAttributeValue(Attributes.FLYING_SPEED), 0.2D, "Flight attribute only");
        helper.assertValueEqual(dragon.getAttributeValue(Attributes.ATTACK_DAMAGE), 3.0D, "Damage attribute");
        helper.assertValueEqual(dragon.getAttributeValue(Attributes.FOLLOW_RANGE), 16.0D, "Follow range");
        helper.assertValueEqual(dragon.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0D, "Knockback resistance");
        helper.assertValueEqual(dragon.getBbWidth(), FireDragon.WIDTH, "Width");
        helper.assertValueEqual(dragon.getBbHeight(), FireDragon.HEIGHT, "Height");
        helper.assertTrue(Math.abs(dragon.getEyeHeight() - FireDragon.EYE_HEIGHT) < 0.001F, "Eye height");
        helper.assertValueEqual(dragon.getType().getCategory(), MobCategory.CREATURE, "Creature category");
        helper.assertValueEqual(dragon.getState(), FireDragon.State.WILD_IDLE, "Initial state");
        helper.assertFalse(dragon.isTame(), "Spawn must not assign an owner");
        helper.assertTrue(dragon.isFood(new ItemStack(Items.SALMON)), "Raw salmon accepted");
        helper.assertFalse(dragon.isFood(new ItemStack(Items.COOKED_SALMON)), "Cooked salmon is not growth food");
        helper.assertFalse(dragon.canMate(dragon), "No breeding in foundation");
        helper.assertTrue(ModItems.FIRE_DRAGON_SPAWN_EGG.get() instanceof SpawnEggItem, "Spawn egg registered");
        for (String path : new String[] { "items/fire_dragon_spawn_egg.json", "models/item/fire_dragon_spawn_egg.json",
                "textures/item/fire_dragon_spawn_egg.png" }) {
            helper.assertTrue(FireDragonGameTests.class.getResource("/assets/codenamecoraline/" + path) != null,
                    "Missing spawn egg resource: " + path);
        }
        helper.succeed();
    }

    private static void spawn(GameTestHelper helper) {
        var type = ModEntities.FIRE_DRAGON.get();
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 3, 5));
        helper.assertFalse(SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.NATURAL, pos, level.getRandom()),
                "Natural spawning must remain disabled until nest design");
        helper.assertFalse(FireDragon.checkSpawnRules(type, level, EntitySpawnReason.CHUNK_GENERATION, pos, level.getRandom()),
                "Chunk generation must not create unguarded babies");
        FireDragon dragon = (FireDragon) ((SpawnEggItem) ModItems.FIRE_DRAGON_SPAWN_EGG.get())
                .getType(new ItemStack(ModItems.FIRE_DRAGON_SPAWN_EGG.get())).spawn(level, pos, EntitySpawnReason.SPAWN_ITEM_USE);
        helper.assertTrue(dragon != null && dragon.isAlive(), "Spawn egg type creates a living dragon");
        helper.assertFalse(dragon.isTame(), "Spawn egg dragon is wild");
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(dragon.isAlive() && dragon.getTarget() == null, "Inert spawn remains alive without combat");
            helper.succeed();
        });
    }

    private static void appearancePersistence(GameTestHelper helper) {
        FireDragon original = helper.spawnWithNoFreeWill(ModEntities.FIRE_DRAGON.get(), new BlockPos(3, 2, 3));
        original.setAppearance(1, 3, 4);
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        original.saveWithoutId(out);
        var saved = out.buildResult();
        FireDragon loaded = ModEntities.FIRE_DRAGON.get().create(helper.getLevel(), EntitySpawnReason.LOAD);
        helper.assertTrue(loaded != null, "Load factory");
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
        helper.assertValueEqual(loaded.getColorCode(), original.getColorCode(), "All three colors survive save/load");
        saved.putInt("PrimaryColor", -100);
        saved.putInt("SecondaryColor", 999);
        saved.putString("VisualPreview", "not_a_clip");
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
        helper.assertValueEqual(loaded.getPrimaryColor(), 0, "Negative color clamped");
        helper.assertValueEqual(loaded.getSecondaryColor(), 4, "Oversized color clamped");
        helper.assertFalse(loaded.isVisualPreview(), "Unknown preview rejected");
        saved.putString("VisualPreview", "fire_windup");
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
        helper.assertTrue(loaded.isVisualPreview(), "Preview clip loads independently of AI");
        helper.assertValueEqual(loaded.getVisualAnimation(), FireDragonAnimation.FIRE_WINDUP, "Known preview clip");
        helper.succeed();
    }

    private static void visualIdles(GameTestHelper helper) {
        FireDragon dragon = helper.spawnWithNoFreeWill(ModEntities.FIRE_DRAGON.get(), new BlockPos(5, 2, 5));
        helper.runAfterDelay(125, () -> {
            var clip = dragon.getVisualAnimation();
            helper.assertTrue(clip == FireDragonAnimation.IDLE_SNIFF || clip == FireDragonAnimation.IDLE_WINGS
                    || clip == FireDragonAnimation.IDLE_TAIL, "Server selects one of the three idle variations");
            helper.assertTrue(dragon.getVisualAnimationTicks(0) >= 0, "Non-negative synchronized clip clock");
        });
        helper.runAfterDelay(170, () -> {
            helper.assertValueEqual(dragon.getVisualAnimation(), FireDragonAnimation.NONE, "Idle returns to base breathing");
            helper.succeed();
        });
    }

    private static void tamingFoodAndCommands(GameTestHelper helper) {
        var owner = helper.makeMockServerPlayerInLevel();
        FireDragon dragon = helper.spawnWithNoFreeWill(ModEntities.FIRE_DRAGON.get(), new BlockPos(3, 2, 3));
        owner.getAbilities().instabuild = false;
        owner.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.SALMON, 64));
        dragon.getRandom().setSeed(4096);
        for (int i = 0; i < 64 && !dragon.isTame(); i++) dragon.mobInteract(owner, net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertTrue(dragon.isOwnedBy(owner), "Salmon creates owner bond");
        helper.assertTrue(owner.getMainHandItem().getCount() < 64, "Taming consumes food");
        dragon.setHealth(10);
        int before = dragon.getGrowthRemaining();
        dragon.mobInteract(owner, net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertValueEqual(dragon.getHealth(), 14F, "Salmon heals four HP");
        helper.assertValueEqual(dragon.getGrowthRemaining(), before - (before + 9) / 10, "Salmon accelerates remaining baby timer");
        helper.assertFalse(dragon.isOrderedToSit(), "Feeding does not toggle command");
        owner.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        dragon.mobInteract(owner, net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertTrue(dragon.isOrderedToSit(), "Right click sits");
        dragon.mobInteract(owner, net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertFalse(dragon.isOrderedToSit(), "Second right click follows");
        helper.succeed();
    }

    private static void ownerPersistenceAndProtection(GameTestHelper helper) {
        var owner = helper.makeMockServerPlayerInLevel();
        FireDragon dragon = helper.spawnWithNoFreeWill(ModEntities.FIRE_DRAGON.get(), new BlockPos(3, 2, 3));
        dragon.tame(owner); dragon.setOrderedToSit(true); dragon.setAppearance(0, 4, 2); dragon.feedSalmon();
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        dragon.saveWithoutId(out);
        FireDragon loaded = ModEntities.FIRE_DRAGON.get().create(helper.getLevel(), EntitySpawnReason.LOAD);
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), out.buildResult()));
        helper.assertTrue(loaded.getOwnerReference().equals(dragon.getOwnerReference()), "Owner UUID persists");
        helper.assertTrue(loaded.isOrderedToSit(), "Wait order persists");
        helper.assertValueEqual(loaded.getGrowthRemaining(), dragon.getGrowthRemaining(), "Growth persists");
        helper.assertValueEqual(loaded.getSatiety(), dragon.getSatiety(), "Satiety persists");
        helper.assertFalse(dragon.canDamage(owner), "Never attack owner");
        FireDragon friend = helper.spawnWithNoFreeWill(ModEntities.FIRE_DRAGON.get(), new BlockPos(5, 2, 3));
        friend.tame(owner);
        helper.assertFalse(dragon.canDamage(friend), "Never attack another pet of owner");
        float health = dragon.getHealth();
        dragon.hurtServer(helper.getLevel(), dragon.damageSources().playerAttack(owner), 8);
        helper.assertValueEqual(dragon.getHealth(), health, "Owner cannot accidentally injure companion");
        helper.succeed();
    }

    private static void shoulderControls(GameTestHelper helper) {
        for (int x = 2; x <= 9; x++) for (int z = 2; z <= 9; z++)
            helper.setBlock(new BlockPos(x, 1, z), net.minecraft.world.level.block.Blocks.STONE);
        var owner = helper.makeMockServerPlayerInLevel();
        owner.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(5, 2, 5)));
        FireDragon dragon = helper.spawnWithNoFreeWill(ModEntities.FIRE_DRAGON.get(), new BlockPos(6, 2, 5));
        dragon.tame(owner); dragon.setNoAi(false);
        var uuid = dragon.getUUID();
        owner.setShiftKeyDown(true);
        owner.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.SALMON));
        dragon.mobInteract(owner, net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertTrue(dragon.isOnShoulder(), "Shift-right mounts shoulder");
        helper.assertValueEqual(owner.getMainHandItem().getCount(), 1, "Shoulder input takes priority over food");
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(dragon.isOnShoulder(), "Held mounting Shift must not immediately unmount");
            helper.assertTrue(dragon.getTarget() == null, "Shoulder disables targets");
            owner.setShiftKeyDown(false);
        });
        helper.runAfterDelay(6, () -> owner.setShiftKeyDown(true));
        helper.runAfterDelay(9, () -> {
            helper.assertFalse(dragon.isOnShoulder(), "New Shift press safely unmounts");
            helper.assertValueEqual(dragon.getUUID(), uuid, "Shoulder never replaces the entity");
            helper.assertTrue(dragon.isOwnedBy(owner), "Owner survives shoulder trip");
            helper.succeed();
        });
    }

    private static void fireballCookedLoot(GameTestHelper helper) {
        FireDragon dragon = helper.spawnWithNoFreeWill(ModEntities.FIRE_DRAGON.get(), new BlockPos(3, 2, 5));
        var pig = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.PIG, new BlockPos(6, 2, 5));
        pig.setHealth(3);
        var shot = new com.codenamecoraline.entity.firedragon.DragonFireball(helper.getLevel(), dragon);
        var aim = pig.getEyePosition().subtract(shot.position());
        shot.shoot(aim.x, aim.y, aim.z, .8F, 0);
        helper.getLevel().addFreshEntity(shot);
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(pig.isDeadOrDying() || pig.isRemoved(), "Fireball kills three-HP prey");
            var items = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    pig.getBoundingBox().inflate(4));
            helper.assertTrue(items.stream().anyMatch(e -> e.getItem().is(Items.COOKED_PORKCHOP)), "Lethal fire yields cooked meat");
            helper.assertFalse(items.stream().anyMatch(e -> e.getItem().is(Items.PORKCHOP)), "No duplicate raw meat");
            helper.succeed();
        });
    }

    private static void shoulderNoPush(GameTestHelper helper) {
        var owner = helper.makeMockServerPlayerInLevel();
        owner.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(5, 3, 5)));
        FireDragon dragon = helper.spawnWithNoFreeWill(ModEntities.FIRE_DRAGON.get(), new BlockPos(6, 3, 5));
        dragon.tame(owner);
        helper.assertTrue(dragon.landOnShoulder(owner), "Shoulder attachment succeeds");
        owner.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        helper.assertFalse(dragon.isPushable(), "Perched dragon is excluded from push queries");
        helper.assertFalse(dragon.canCollideWith(owner), "Perched dragon does not block player movement");
        dragon.push(owner);
        helper.assertValueEqual(owner.getDeltaMovement(), net.minecraft.world.phys.Vec3.ZERO,
                "Dragon cannot impart a collision impulse to its owner");
        helper.succeed();
    }

    private static void repeatedHitsRetaliation(GameTestHelper helper) {
        for (int x = 2; x <= 10; x++) for (int z = 2; z <= 10; z++)
            helper.setBlock(new BlockPos(x, 1, z), net.minecraft.world.level.block.Blocks.STONE);
        var owner = helper.makeMockServerPlayerInLevel();
        owner.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(3, 2, 4)));
        FireDragon dragon = helper.spawnWithNoFreeWill(ModEntities.FIRE_DRAGON.get(), new BlockPos(4, 2, 5));
        dragon.tame(owner); dragon.setNoAi(false);
        var attacker = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.ZOMBIE, new BlockPos(9, 2, 5));
        attacker.setInvulnerable(false);
        dragon.hurtServer(helper.getLevel(), dragon.damageSources().mobAttack(attacker), 1);
        helper.assertValueEqual(dragon.getTarget(), attacker, "Accepted damage acquires aggressor immediately");
        helper.assertValueEqual(dragon.getState(), FireDragon.State.DEFENDING, "Defense interrupts idle");
        for (int tick : new int[] {3, 6, 9}) helper.runAfterDelay(tick, () -> {
            dragon.invulnerableTime = 0;
            dragon.hurtServer(helper.getLevel(), dragon.damageSources().mobAttack(attacker), 1);
            helper.assertValueEqual(dragon.getTarget(), attacker, "Repeated hit retains target");
        });
        helper.runAfterDelay(13, () -> {
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(
                    com.codenamecoraline.entity.firedragon.DragonFireball.class,
                    dragon.getBoundingBox().inflate(12)).stream().anyMatch(e -> e.getOwner() == dragon),
                    "Repeated attacks must not endlessly reset the shot windup");
            helper.succeed();
        });
    }

    private static void predictiveAim(GameTestHelper helper) {
        var origin = net.minecraft.world.phys.Vec3.ZERO;
        var center = new net.minecraft.world.phys.Vec3(0, 1, 8);
        var stationary = com.codenamecoraline.entity.firedragon.DragonFireball.aimPoint(origin, center, origin);
        helper.assertValueEqual(stationary, center, "Stationary target stays centered");
        var moving = com.codenamecoraline.entity.firedragon.DragonFireball.aimPoint(origin, center,
                new net.minecraft.world.phys.Vec3(.2, 0, 0));
        helper.assertTrue(moving.x > 1 && moving.x < 5, "Lateral target receives bounded leading");
        helper.assertValueEqual(moving.y, center.y, "Horizontal leading does not aim above torso");
        helper.succeed();
    }

    private static void damage(GameTestHelper helper) {
        FireDragon dragon = helper.spawnWithNoFreeWill(ModEntities.FIRE_DRAGON.get(), new BlockPos(7, 3, 7));
        helper.assertTrue(dragon.fireImmune(), "Entity type is fire immune");
        float initial = dragon.getHealth();
        dragon.hurtServer(helper.getLevel(), dragon.damageSources().inFire(), 6.0F);
        dragon.hurtServer(helper.getLevel(), dragon.damageSources().lava(), 6.0F);
        dragon.hurtServer(helper.getLevel(), dragon.damageSources().fall(), 6.0F);
        dragon.causeFallDamage(30.0D, 1.0F, dragon.damageSources().fall());
        helper.assertValueEqual(dragon.getHealth(), initial, "Fire, lava and fall must not hurt");
        dragon.hurtServer(helper.getLevel(), dragon.damageSources().generic(), 4.0F);
        helper.assertTrue(dragon.getHealth() < initial, "Ordinary damage must still work");
        helper.succeed();
    }
}
