package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Server integration tests for the Fey Raven foundation, attributes, immunities, and spawn rules. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class FeyRavenGameTests {
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
            Map.entry("fey_raven_attributes_and_registration", FeyRavenGameTests::attributesAndRegistration),
            Map.entry("fey_raven_spawn_rules_and_immunities", FeyRavenGameTests::spawnRulesAndImmunities));

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
                        new TestData<>(environment, id("basilisk_test_arena"), 100, 0, true))));
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

        // Verify Y < 64 rejection
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

        // Place oak leaves at Y >= 64 and test valid natural spawn + local cap of 3
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
}
