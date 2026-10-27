package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.basilisk.Basilisk;
import com.codenamecoraline.entity.basilisk.DeathBlight;
import com.codenamecoraline.entity.basilisk.DeathBlightCloud;
import com.codenamecoraline.entity.basilisk.DeathBlightMeter;
import com.codenamecoraline.registry.ModAttachments;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Opt-in server integration tests. Never registered during normal client or server runs. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class BasiliskGameTests {
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
            Map.entry("meter_timing", BasiliskGameTests::meterTiming),
            Map.entry("immunities_and_weaknesses", BasiliskGameTests::immunitiesAndWeaknesses),
            Map.entry("soul_light_and_provocation", BasiliskGameTests::soulLight),
            Map.entry("cloud_buildup_no_direct_damage", h -> cloud(h, false, false)),
            Map.entry("overlapping_clouds_do_not_stack", h -> cloud(h, true, false)),
            Map.entry("death_blight_pierces_armor", BasiliskGameTests::lethal),
            Map.entry("totem_survives_blight", h -> cloud(h, false, true)),
            Map.entry("consumables_clear_blight", BasiliskGameTests::consumables),
            Map.entry("ward_halves_blight", BasiliskGameTests::ward),
            Map.entry("ai_hops_inhales_and_exhales", BasiliskGameTests::combatAi),
            Map.entry("lily_strengthens_golem", BasiliskGameTests::lily));

    private BasiliskGameTests() {}
    private static Identifier id(String name) { return Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, name); }

    @SubscribeEvent
    public static void functions(RegisterEvent event) {
        if (!Boolean.getBoolean("coraline.basiliskTests")) return;
        event.register(Registries.TEST_FUNCTION, registry -> TESTS.forEach((name, function) -> registry.register(id(name), function)));
    }

    @SubscribeEvent
    public static void tests(RegisterGameTestsEvent event) {
        if (!Boolean.getBoolean("coraline.basiliskTests")) return;
        var environment = event.registerEnvironment(id("basilisk_test_environment"));
        TESTS.forEach((name, function) -> event.registerTest(id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                        new TestData<>(environment, id("basilisk_test_arena"), 180, 0, true))));
    }

    private static void meterTiming(GameTestHelper helper) {
        DeathBlightMeter meter = new DeathBlightMeter();
        int amount = 0;
        for (int tick = 1; tick <= 69; tick++) amount = meter.tick(amount, true, false);
        helper.assertValueEqual(amount, 90, "buildup before the seventh pulse");
        helper.assertValueEqual(meter.tick(amount, true, false), 100, "threshold at 3.5 seconds");
        meter.reset();
        amount = 60;
        for (int tick = 0; tick < 20; tick++) amount = meter.tick(amount, false, false);
        helper.assertValueEqual(amount, 55, "one second outside the mist");
        meter.reset();
        amount = 0;
        for (int tick = 0; tick < 60; tick++) amount = meter.tick(amount, true, true);
        helper.assertValueEqual(amount, 45, "ward halves buildup");
        helper.succeed();
    }

    private static Basilisk basilisk(GameTestHelper helper, int x) {
        Basilisk mob = helper.spawnWithNoFreeWill(ModEntities.BASILISK.get(), new BlockPos(x, 1, 3));
        mob.setNoGravity(true);
        return mob;
    }

    private static void immunitiesAndWeaknesses(GameTestHelper helper) {
        Basilisk mob = basilisk(helper, 3);
        helper.assertValueEqual(mob.getMaxHealth(), 28.0F, "base health");
        helper.assertValueEqual(mob.getAttributeValue(Attributes.ARMOR), 2.0, "base armor");
        for (var effect : java.util.List.of(MobEffects.POISON, MobEffects.WITHER, MobEffects.DARKNESS)) {
            helper.assertTrue(!mob.addEffect(new MobEffectInstance(effect, 200)), "Basilisk must reject poison, wither and darkness");
        }
        mob.getAttribute(Attributes.ARMOR).setBaseValue(0);
        mob.hurtServer(helper.getLevel(), mob.damageSources().lava(), 4);
        helper.assertValueEqual(mob.getHealth(), 22.0F, "fire multiplier");
        Basilisk swordTarget = basilisk(helper, 7);
        swordTarget.getAttribute(Attributes.ARMOR).setBaseValue(0);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        swordTarget.hurtServer(helper.getLevel(), swordTarget.damageSources().playerAttack(player), 4);
        helper.assertValueEqual(swordTarget.getHealth(), 23.0F, "sword multiplier");
        helper.succeed();
    }

    private static void soulLight(GameTestHelper helper) {
        Basilisk mob = basilisk(helper, 3);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absoluteVec(new Vec3(4, 1, 3)));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SOUL_TORCH));
        helper.assertTrue(mob.isRepelledBy(player) && !mob.canAttack(player), "off-hand torch repels");
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SOUL_LANTERN));
        helper.assertTrue(mob.isRepelledBy(player), "main-hand lantern repels");
        mob.hurtServer(helper.getLevel(), mob.damageSources().playerAttack(player), 1);
        helper.assertTrue(!mob.isRepelledBy(player) && mob.canAttack(player), "direct attacker loses protection");
        helper.succeed();
    }

    private static LivingEntity victim(GameTestHelper helper) {
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(5, 1, 5));
        pig.setNoGravity(true);
        return pig;
    }

    private static DeathBlightCloud mist(GameTestHelper helper, LivingEntity target) {
        DeathBlightCloud cloud = DeathBlightCloud.create(helper.getLevel(), target.position().add(0, 0.1, 0), null);
        helper.getLevel().addFreshEntity(cloud);
        return cloud;
    }

    private static void cloud(GameTestHelper helper, boolean overlap, boolean totem) {
        LivingEntity target = victim(helper);
        if (totem) target.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        DeathBlightCloud cloud = mist(helper, target);
        if (overlap) mist(helper, target);
        float health = target.getHealth();
        helper.runAfterDelay(35, () -> {
            helper.assertValueEqual(DeathBlight.buildup(target), 45, "three half-second buildup pulses");
            helper.assertValueEqual(target.getHealth(), health, "mist has no direct damage");
        });
        helper.runAfterDelay(80, () -> {
            if (totem) {
                helper.assertTrue(target.isAlive(), "totem must protect against death blight");
                helper.assertTrue(target.getOffhandItem().isEmpty(), "totem must be consumed");
            } else helper.assertTrue(!target.isAlive(), "100 buildup must trigger lethal damage");
        });
        helper.runAfterDelay(105, () -> {
            helper.assertTrue(cloud.isRemoved(), "cloud must expire after five seconds");
            helper.succeed();
        });
    }

    private static void lethal(GameTestHelper helper) {
        LivingEntity target = victim(helper);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
        target.getAttribute(Attributes.ARMOR).setBaseValue(30);
        target.setHealth(200);
        mist(helper, target);
        helper.runAfterDelay(80, () -> {
            helper.assertValueEqual(target.getHealth(), 100.0F, "blight deals 100 damage through armor");
            helper.assertTrue(target.getData(ModAttachments.BLIGHT_EXPOSURE).rootedTicks > 0, "root lasts one second");
            helper.succeed();
        });
    }

    private static void consumables(GameTestHelper helper) {
        LivingEntity target = victim(helper);
        for (var item : java.util.List.of(Items.MILK_BUCKET, Items.HONEY_BOTTLE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE)) {
            target.setData(ModAttachments.DEATH_BLIGHT, 90);
            DeathBlight.expose(target, null);
            NeoForge.EVENT_BUS.post(new LivingEntityUseItemEvent.Finish(target, new ItemStack(item), 0, ItemStack.EMPTY));
            helper.assertValueEqual(DeathBlight.buildup(target), 0, "consumed cure clears buildup");
        }
        helper.succeed();
    }

    private static void ward(GameTestHelper helper) {
        LivingEntity target = victim(helper);
        target.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.BASILISK_WARD.get()));
        mist(helper, target);
        helper.runAfterDelay(65, () -> {
            helper.assertValueEqual(DeathBlight.buildup(target), 45, "ward halves exposure in a real cloud");
            helper.assertTrue(target.isAlive(), "ward extends the escape window");
            helper.succeed();
        });
    }

    private static void lily(GameTestHelper helper) {
        var golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(5, 1, 5));
        golem.setHealth(50);
        var stack = new ItemStack(ModItems.CATACOMB_LILY.get());
        stack.interactLivingEntity(helper.makeMockPlayer(GameType.SURVIVAL), golem, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(golem.getHealth(), 62.0F, "lily heals twelve health");
        helper.assertTrue(golem.hasEffect(MobEffects.RESISTANCE), "lily grants resistance");
        helper.assertTrue(stack.isEmpty(), "offered lily is consumed");
        helper.succeed();
    }

    private static void combatAi(GameTestHelper helper) {
        var target = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(7, 1, 5));
        Basilisk mob = helper.spawn(ModEntities.BASILISK.get(), new BlockPos(3, 1, 5));
        mob.setTarget(target);
        boolean[] seen = new boolean[3];
        helper.onEachTick(() -> {
            if (mob.getState() == Basilisk.State.HOPPING) seen[0] = true;
            if (mob.getState() == Basilisk.State.INHALING) seen[1] = true;
            if (mob.getState() == Basilisk.State.EXHALING) seen[2] = true;
        });
        helper.runAfterDelay(100, () -> {
            helper.assertTrue(seen[0], "combat AI must leap");
            helper.assertTrue(seen[1], "combat AI must telegraph its breath");
            helper.assertTrue(seen[2], "combat AI must release its breath");
            helper.assertTrue(!helper.getLevel().getEntitiesOfClass(DeathBlightCloud.class,
                    mob.getBoundingBox().inflate(10)).isEmpty(), "AI breath must leave a cloud");
            helper.succeed();
        });
    }
}
