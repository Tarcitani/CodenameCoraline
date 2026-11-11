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
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.of(
            "fire_dragon_registration", FireDragonGameTests::registration,
            "fire_dragon_spawn", FireDragonGameTests::spawn,
            "fire_dragon_damage", FireDragonGameTests::damage,
            "fire_dragon_appearance_persistence", FireDragonGameTests::appearancePersistence,
            "fire_dragon_visual_idles", FireDragonGameTests::visualIdles);

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
                        new TestData<>(environment, id("basilisk_test_arena"), 200, 0, true))));
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
