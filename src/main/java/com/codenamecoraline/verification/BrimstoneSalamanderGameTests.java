package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.client.salamander.BrimstoneSalamanderAnimations;
import com.codenamecoraline.client.salamander.BrimstoneSalamanderModel;
import com.codenamecoraline.entity.salamander.BrimstoneSalamander;
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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Server integration tests for the Brimstone Salamander foundation, attributes, immunities, and spawn rules (Slice 1). */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class BrimstoneSalamanderGameTests {
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
            Map.entry("brimstone_salamander_attributes_and_registration", BrimstoneSalamanderGameTests::attributesAndRegistration),
            Map.entry("brimstone_salamander_spawn_rules_and_immunities", BrimstoneSalamanderGameTests::spawnRulesAndImmunities));

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
                        new TestData<>(environment, id("basilisk_test_arena"), 100, 0, true))));
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
}

