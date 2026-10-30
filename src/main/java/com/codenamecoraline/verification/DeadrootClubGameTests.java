package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.macaque.BarkMacaque;
import com.codenamecoraline.item.DeadrootClubItem;
import com.codenamecoraline.item.DeadrootLeapSlam;
import com.codenamecoraline.item.DeadrootLeapSlam.LeapResult;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Game tests of the Deadroot Club: the charged leap (hold, release, cooldown, ground rule), the slam's damage growing
 * with the height of the fall, the Shift summon of Bark Macaques with its cap, and the allies' loyalty (they fight hostile
 * mobs, never their owner). Run with {@code runDeadrootClubTest}.
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class DeadrootClubGameTests {
    /** Relative height of the plot floor; the plot is built in its own corner, away from the other suites. */
    private static final int Y = 100;
    private static final float SECONDARY_SHARE = DeadrootLeapSlam.SECONDARY_SHARE;

    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.of(
            "deadroot_charged_leap_needs_ground_and_has_cooldown", DeadrootClubGameTests::chargedLeapNeedsGroundAndHasCooldown,
            "deadroot_charge_scales_leap_height_and_distance", DeadrootClubGameTests::chargeScalesLeap,
            "deadroot_slam_damage_grows_with_height", DeadrootClubGameTests::slamDamageGrowsWithHeight,
            "deadroot_slam_damages_enemies_with_falloff", DeadrootClubGameTests::slamDamagesWithFalloff,
            "deadroot_leap_lands_once_without_fall_damage", DeadrootClubGameTests::leapLandsOnceWithoutFallDamage,
            "deadroot_shift_summons_macaques_with_cap", DeadrootClubGameTests::shiftSummonsMacaquesWithCap,
            "deadroot_macaque_allies_fight_enemies_not_owner", DeadrootClubGameTests::alliesFightEnemiesNotOwner,
            "deadroot_player_summons_never_fight_each_other", DeadrootClubGameTests::summonsNeverFightEachOther);

    private static boolean enabled() {
        return Boolean.getBoolean("coraline.deadrootTests");
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, name);
    }

    @SubscribeEvent
    public static void functions(RegisterEvent event) {
        if (!enabled()) return;
        event.register(Registries.TEST_FUNCTION, registry -> TESTS.forEach((name, fn) -> registry.register(id(name), fn)));
    }

    @SubscribeEvent
    public static void tests(RegisterGameTestsEvent event) {
        if (!enabled()) return;
        var environment = event.registerEnvironment(id("deadroot_test_environment"));
        TESTS.forEach((name, fn) -> event.registerTest(id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                        new TestData<>(environment, id("basilisk_test_arena"), 300, 0, true,
                                Rotation.NONE, false, 1, 1, true, 0))));
    }

    private static void floor(GameTestHelper h, int x0, int z0, int x1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                // only the floor: air above would cut neighbouring arenas' trunks
                h.setBlock(new BlockPos(x, Y, z), Blocks.STONE);
            }
        }
    }

    private static ServerPlayer player(GameTestHelper h, double x, double z) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        player.setPos(h.absoluteVec(new Vec3(x + 0.5D, Y + 1, z + 0.5D)));
        player.setOnGround(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.DEADROOT_CLUB.get()));
        return player;
    }

    /** Runs {@code each} every tick until {@code stop} holds or {@code max} ticks pass, then {@code done}. */
    private static void waitUntil(GameTestHelper h, int max, Consumer<Integer> each, BooleanSupplier stop, Runnable done) {
        boolean[] finished = {false};
        for (int t = 1; t <= max; t++) {
            final int tick = t;
            h.runAfterDelay(t, () -> {
                if (finished[0]) {
                    return;
                }
                each.accept(tick);
                if (stop.getAsBoolean() || tick == max) {
                    finished[0] = true;
                    done.run();
                }
            });
        }
    }

    private static void chargedLeapNeedsGroundAndHasCooldown(GameTestHelper h) {
        floor(h, 0, 0, 12, 12);
        ServerPlayer player = player(h, 4, 4);
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        DeadrootClubItem item = (DeadrootClubItem) stack.getItem();

        h.assertValueEqual(item.use(h.getLevel(), player, InteractionHand.MAIN_HAND), InteractionResult.CONSUME,
                "holding right-click on the ground starts a charge");
        h.assertTrue(!DeadrootLeapSlam.isLeaping(player), "a charge is not a leap until it is released");
        h.assertTrue(item.releaseUsing(stack, h.getLevel(), player, DeadrootClubItem.USE_DURATION - DeadrootClubItem.MAX_CHARGE_TICKS),
                "releasing a full charge leaps");
        h.assertTrue(DeadrootLeapSlam.isLeaping(player), "the player is now leaping");
        h.assertTrue(player.getCooldowns().isOnCooldown(stack), "the leap puts the club on cooldown");
        h.assertValueEqual(item.use(h.getLevel(), player, InteractionHand.MAIN_HAND), InteractionResult.FAIL,
                "no new charge during the cooldown");

        ServerPlayer airborne = h.makeMockServerPlayerInLevel();
        airborne.setOnGround(false);
        airborne.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.DEADROOT_CLUB.get()));
        h.assertValueEqual(item.use(h.getLevel(), airborne, InteractionHand.MAIN_HAND), InteractionResult.FAIL,
                "no charge while airborne");
        h.succeed();
    }

    private static void chargeScalesLeap(GameTestHelper h) {
        floor(h, 0, 0, 12, 12);
        ServerPlayer tap = player(h, 4, 4);
        ServerPlayer full = player(h, 4, 4);
        ServerPlayer upward = player(h, 4, 4);
        upward.setXRot(-45.0F); // looking 45 degrees up
        h.assertValueEqual(DeadrootLeapSlam.launch(tap, 0.0D), LeapResult.STARTED, "a tap flies");
        h.assertValueEqual(DeadrootLeapSlam.launch(full, 1.0D), LeapResult.STARTED, "a full charge flies");
        h.assertValueEqual(DeadrootLeapSlam.launch(upward, 1.0D), LeapResult.STARTED, "a flight upwards");
        Vec3 lo = tap.getDeltaMovement();
        Vec3 hi = full.getDeltaMovement();
        Vec3 look = full.getLookAngle();
        double align = hi.subtract(0.0D, DeadrootLeapSlam.LIFT, 0.0D).normalize().dot(look.normalize());
        h.assertTrue(align > 0.999D, "the club pulls along the exact look direction (alignment " + align + ")");
        h.assertTrue(hi.length() > lo.length() * 1.8D, "a full charge pulls far harder than a tap (" + hi.length() + " vs " + lo.length() + ")");
        h.assertTrue(Math.hypot(hi.x, hi.z) > 1.8D, "looking level, the flight goes forward, not up (horizontal " + Math.hypot(hi.x, hi.z) + ")");
        h.assertTrue(upward.getDeltaMovement().y > 1.4D, "looking up, the flight climbs (" + upward.getDeltaMovement().y + ")");
        h.assertTrue(DeadrootLeapSlam.thrustTicksLeft(full) > DeadrootLeapSlam.thrustTicksLeft(tap),
                "a full charge pulls for longer (" + DeadrootLeapSlam.thrustTicksLeft(full) + " vs " + DeadrootLeapSlam.thrustTicksLeft(tap) + " ticks)");
        // the pull is sustained: even if something slows the player, the next tick pulls again
        full.setDeltaMovement(Vec3.ZERO);
        DeadrootLeapSlam.tick(full);
        h.assertTrue(full.getDeltaMovement().distanceTo(hi) < 1.0E-6D, "the thrust is re-applied every tick while it lasts");
        h.succeed();
    }

    private static void slamDamageGrowsWithHeight(GameTestHelper h) {
        floor(h, 0, 0, 20, 20);
        // two separate impacts, each with its own player and zombie under the club
        ServerPlayer lowFall = player(h, 4, 4);
        Zombie low = h.spawn(EntityType.ZOMBIE, new BlockPos(4, Y + 1, 5));
        low.setNoAi(true);
        float lowBefore = low.getHealth();
        DeadrootLeapSlam.slam(h.getLevel(), lowFall, 2.0D);
        float lowTaken = lowBefore - low.getHealth();

        ServerPlayer highFall = player(h, 12, 12);
        Zombie high = h.spawn(EntityType.ZOMBIE, new BlockPos(12, Y + 1, 13));
        high.setNoAi(true);
        float highBefore = high.getHealth();
        DeadrootLeapSlam.slam(h.getLevel(), highFall, 12.0D);
        float highTaken = highBefore - high.getHealth();
        h.assertTrue(lowTaken > 0.0F, "the low fall still hurts (" + lowTaken + ")");
        h.assertTrue(highTaken > lowTaken + 10.0F,
                "a fall of 12 blocks hits far harder than one of 2 (" + highTaken + " vs " + lowTaken + ")");
        h.succeed();
    }

    private static void slamDamagesWithFalloff(GameTestHelper h) {
        floor(h, 0, 0, 16, 16);
        ServerPlayer player = player(h, 8, 8);
        Zombie near = h.spawn(EntityType.ZOMBIE, new BlockPos(8, Y + 1, 9));
        Zombie mid = h.spawn(EntityType.ZOMBIE, new BlockPos(10, Y + 1, 8));
        Zombie far = h.spawn(EntityType.ZOMBIE, new BlockPos(14, Y + 1, 8));
        Pig pig = h.spawn(EntityType.PIG, new BlockPos(8, Y + 1, 7));
        for (Zombie z : new Zombie[] {near, mid, far}) {
            z.setNoAi(true);
        }
        float nearBefore = near.getHealth();
        float midBefore = mid.getHealth();
        float farBefore = far.getHealth();
        float pigBefore = pig.getHealth();

        // a fall of 8 blocks: base 6 + 8 * 1.5 = 18 at full strength, 60% of it at the outer ring
        DeadrootLeapSlam.slam(h.getLevel(), player, 8.0D);

        // zombies carry a base armour attribute (2), which trims the hit by about 1.6%: compare the rings, not raw numbers
        float nearTaken = nearBefore - near.getHealth();
        float midTaken = midBefore - mid.getHealth();
        h.assertTrue(nearTaken > 17.0F && nearTaken <= 18.0F,
                "the zombie under the impact takes about the full 18 (took " + nearTaken + ")");
        h.assertTrue(Math.abs(midTaken / nearTaken - SECONDARY_SHARE) < 0.01F,
                "the outer ring takes 60% of the centre (took " + midTaken + " of " + nearTaken + ")");
        h.assertTrue(far.getHealth() == farBefore, "a zombie beyond the radius is untouched");
        h.assertTrue(pig.getHealth() == pigBefore, "animals are never hit by the slam");
        h.succeed();
    }

    private static void leapLandsOnceWithoutFallDamage(GameTestHelper h) {
        floor(h, 0, 0, 12, 12);
        ServerPlayer player = player(h, 4, 4);
        float healthBefore = player.getHealth();
        h.assertValueEqual(DeadrootLeapSlam.launch(player, 0.5D), LeapResult.STARTED, "the leap starts");
        // the mock player does not move by physics, so the test lands it by hand after the minimum air time
        for (int t = 1; t <= DeadrootLeapSlam.MIN_AIR_TICKS + 1; t++) {
            h.runAfterDelay(t, () -> DeadrootLeapSlam.tick(player));
        }
        h.runAfterDelay(DeadrootLeapSlam.MIN_AIR_TICKS + 2, () -> {
            player.setOnGround(true);
            DeadrootLeapSlam.tick(player);
            h.assertTrue(!DeadrootLeapSlam.isLeaping(player), "the leap ends at the landing");
            h.assertTrue(player.getHealth() == healthBefore, "the leap causes no fall damage");
            h.succeed();
        });
    }

    private static void shiftSummonsMacaquesWithCap(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        floor(h, 0, 0, 16, 16);
        ServerPlayer player = player(h, 8, 8);
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        h.assertValueEqual(DeadrootClubItem.summon(level, player, stack, InteractionHand.MAIN_HAND), InteractionResult.SUCCESS,
                "Shift + right-click summons");
        h.assertValueEqual(BarkMacaque.countAllies(level, player), DeadrootClubItem.SUMMON_COUNT, "three allied macaques");
        h.assertTrue(player.getCooldowns().isOnCooldown(stack), "the summon puts the club on cooldown");
        h.assertValueEqual(DeadrootClubItem.summon(level, player, stack, InteractionHand.MAIN_HAND), InteractionResult.FAIL,
                "no second summon during the cooldown");
        h.assertValueEqual(BarkMacaque.summonAllies(level, player, 3), 1, "the cap of four allows only one more");
        h.assertValueEqual(BarkMacaque.summonAllies(level, player, 3), 0, "a full cap summons nobody");
        h.assertValueEqual(BarkMacaque.countAllies(level, player), BarkMacaque.ALLY_CAP, "exactly the cap is alive");
        h.succeed();
    }

    private static void summonsNeverFightEachOther(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        floor(h, 0, 0, 16, 16);
        ServerPlayer player = player(h, 8, 8);
        h.assertValueEqual(BarkMacaque.summonAllies(level, player, 1), 1, "one allied macaque");
        h.assertValueEqual(com.codenamecoraline.item.RenegadeGreataxeItem.summonAllies(level, player, 1), 1, "one allied gift mimic");
        BarkMacaque macaque = level.getEntitiesOfClass(BarkMacaque.class, player.getBoundingBox().inflate(8.0D), m -> m.isAlliedTo(player)).getFirst();
        var mimic = level.getEntitiesOfClass(com.codenamecoraline.entity.collector.GiftMimic.class, player.getBoundingBox().inflate(8.0D),
                m -> m.isAllied()).getFirst();
        h.assertTrue(macaque.isPlayerSummon() && mimic.isPlayerSummon(), "both are player summons");
        h.assertTrue(!macaque.canAttack(mimic), "the macaque never targets the gift mimic");
        h.assertTrue(!mimic.canAttack(macaque), "the gift mimic never targets the macaque");
        h.assertTrue(!macaque.hurtServer(level, mimic.damageSources().mobAttack(mimic), 5.0F), "the mimic cannot hurt the macaque");
        h.assertTrue(!mimic.hurtServer(level, macaque.damageSources().mobAttack(macaque), 5.0F), "the macaque cannot hurt the mimic");
        // the club's own slam spares both
        macaque.setPos(player.getX() + 1.0D, player.getY(), player.getZ());
        mimic.setPos(player.getX() - 1.0D, player.getY(), player.getZ());
        float macaqueBefore = macaque.getHealth();
        float mimicBefore = mimic.getHealth();
        DeadrootLeapSlam.slam(level, player, 10.0D);
        h.assertTrue(macaque.getHealth() == macaqueBefore && mimic.getHealth() == mimicBefore, "the slam spares the player summons");
        h.succeed();
    }

    private static void alliesFightEnemiesNotOwner(GameTestHelper h) {
        floor(h, 0, 0, 16, 16);
        ServerPlayer player = player(h, 4, 4);
        BarkMacaque ally = h.spawn(ModEntities.BARK_MACAQUE.get(), new BlockPos(6, Y + 1, 4));
        ally.setOnGround(true);
        ally.setAlliedTo(player);
        Zombie zombie = h.spawn(EntityType.ZOMBIE, new BlockPos(9, Y + 1, 4));
        zombie.setNoAi(true);
        h.assertTrue(ally.isAlliedTo(player), "bound to its summoner");
        h.assertTrue(ally.canAttack(zombie), "an ally may fight a hostile mob");
        h.assertTrue(!ally.canAttack(player), "an ally never targets its owner");
        h.assertTrue(!ally.hurtServer(h.getLevel(), player.damageSources().playerAttack(player), 5.0F),
                "an ally never takes damage from its owner");
        float zombieBefore = zombie.getHealth();
        waitUntil(h, 60, t -> { }, () -> ally.getActivity() == BarkMacaque.Activity.GROUND, () -> {
            h.assertTrue(ally.startLeap(zombie), "the ally leaps at the zombie once it is ready");
            waitUntil(h, 80, t -> { }, () -> zombie.getHealth() < zombieBefore, () -> {
                h.assertTrue(zombie.getHealth() < zombieBefore, "the ally bites the hostile mob");
                h.succeed();
            });
        });
    }
}
