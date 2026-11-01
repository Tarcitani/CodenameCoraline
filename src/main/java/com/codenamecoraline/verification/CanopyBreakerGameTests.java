package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.bestiary.Bestiary;
import com.codenamecoraline.client.canopy.CanopyBreakerAnimations;
import com.codenamecoraline.client.canopy.CanopyBreakerModel;
import com.codenamecoraline.client.macaque.BarkMacaqueAnimations;
import com.codenamecoraline.client.macaque.BarkMacaqueModel;
import com.codenamecoraline.entity.macaque.BarkMacaque;
import com.codenamecoraline.entity.macaque.MacaqueAnim;
import com.codenamecoraline.entity.canopy.CanopyAnim;
import com.codenamecoraline.entity.canopy.CanopyBreaker;
import com.codenamecoraline.entity.canopy.CanopyBreaker.Activity;
import com.codenamecoraline.entity.canopy.CanopyBreaker.Attack;
import com.codenamecoraline.entity.canopy.TreeNav;
import com.codenamecoraline.item.HollowTrunkIdolItem;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Integration tests of the Canopy Breaker: registration and assets, animation catalogue, UV layout, idol conditions,
 * ground chase, every attack (damage, zones, cancellation), tree detection, real climbing, ballistic jumps between trees,
 * the Canopy Drop, failure handling (invalid jump, lost grip, no trees), stages, stagger, death and sync of the visual state.
 *
 * <p>Tests build their own plots high above the grid (stone floor + spruce trees) so parallel arenas never interact.</p>
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class CanopyBreakerGameTests {
    /** Relative height of every plot's floor. */
    private static final int Y = 100;

    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
            Map.entry("canopy_registration_and_attributes", CanopyBreakerGameTests::registrationAndAttributes),
            Map.entry("canopy_animation_catalogue_and_uv_layout", CanopyBreakerGameTests::animationCatalogueAndUv),
            Map.entry("canopy_idol_conditions", CanopyBreakerGameTests::idolConditions),
            Map.entry("canopy_chases_and_draws_club", CanopyBreakerGameTests::chasesAndDrawsClub),
            Map.entry("canopy_attack_trunk_slam", CanopyBreakerGameTests::attackTrunkSlam),
            Map.entry("canopy_attack_sweep_and_backhand", CanopyBreakerGameTests::attackSweepAndBackhand),
            Map.entry("canopy_attack_lunge_and_cancel", CanopyBreakerGameTests::attackLungeAndCancel),
            Map.entry("canopy_attack_animation_sync", CanopyBreakerGameTests::attackAnimationSync),
            Map.entry("canopy_tree_detection", CanopyBreakerGameTests::treeDetection),
            Map.entry("canopy_climbs_and_jumps_between_trees", CanopyBreakerGameTests::climbsAndJumps),
            Map.entry("canopy_drop_attack_lands_once", CanopyBreakerGameTests::dropAttack),
            Map.entry("canopy_invalid_jump_is_cancelled", CanopyBreakerGameTests::invalidJumpCancelled),
            Map.entry("canopy_lost_grip_falls_and_recovers", CanopyBreakerGameTests::lostGripRecovers),
            Map.entry("canopy_no_trees_falls_back_to_ground", CanopyBreakerGameTests::noTreesFallsBack),
            Map.entry("canopy_stages_transition_once", CanopyBreakerGameTests::stagesTransitionOnce),
            Map.entry("canopy_stagger_and_hurt_overlay", CanopyBreakerGameTests::staggerAndOverlay),
            Map.entry("canopy_death_loot_and_advancements", CanopyBreakerGameTests::deathLootAndContent),
            Map.entry("macaque_registration_and_animations", CanopyBreakerGameTests::macaqueRegistration),
            Map.entry("macaque_leaps_and_bites_once", CanopyBreakerGameTests::macaqueLeapsAndBitesOnce),
            Map.entry("macaque_climbs_to_elevated_target", CanopyBreakerGameTests::macaqueClimbsToElevatedTarget),
            Map.entry("macaque_bound_to_boss", CanopyBreakerGameTests::macaqueBoundToBoss),
            Map.entry("minions_opening_reinforcements_and_cap", CanopyBreakerGameTests::minionsOpeningReinforcementsAndCap),
            Map.entry("minions_leave_with_dead_boss", CanopyBreakerGameTests::minionsLeaveWithDeadBoss),
            Map.entry("macaque_orphan_is_dismissed", CanopyBreakerGameTests::orphanMinionIsDismissed),
            Map.entry("minions_ground_opening_call", CanopyBreakerGameTests::groundOpeningCall),
            Map.entry("canopy_intro_chest_beat_and_roar", CanopyBreakerGameTests::introChestBeatAndRoar),
            Map.entry("canopy_club_is_held_by_the_handle_end", CanopyBreakerGameTests::clubIsHeldByTheHandleEnd),
            Map.entry("canopy_club_blows_break_the_guard", CanopyBreakerGameTests::clubBlowsBreakTheGuard),
            Map.entry("canopy_grab_holds_and_throws_far", CanopyBreakerGameTests::grabHoldsAndThrowsFar),
            Map.entry("canopy_grab_missing_leaves_him_open", CanopyBreakerGameTests::grabIgnoresShieldsAndMissesOpenHim),
            Map.entry("canopy_pound_throws_everyone_around", CanopyBreakerGameTests::poundThrowsEveryoneAround),
            Map.entry("canopy_ground_leap_crosses_open_ground", CanopyBreakerGameTests::groundLeapCrossesOpenGround),
            Map.entry("canopy_ground_leap_reaches_a_pillar", CanopyBreakerGameTests::groundLeapReachesAPillar),
            Map.entry("canopy_backhand_throws_back_and_up", CanopyBreakerGameTests::backhandThrowsBackAndUp)
    );

    private CanopyBreakerGameTests() {}

    private static boolean enabled() {
        return Boolean.getBoolean("coraline.canopyTests");
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
        var environment = event.registerEnvironment(id("canopy_test_environment"));
        TESTS.forEach((name, fn) -> event.registerTest(id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                        new TestData<>(environment, id("basilisk_test_arena"), 700, 0, true,
                                Rotation.NONE, false, 1, 1, true, 0))));
    }

    // ------------------------------------------------------------------------------------------------ plot helpers
    /**
     * Stone floor with free air above it. The plot may extend well past the 12 x 12 arena, so every chunk it touches is
     * force-loaded: mobs only tick (and so only walk, climb and attack) inside entity-ticking chunks.
     */
    private static void floor(GameTestHelper h, int x0, int z0, int x1, int z1) {
        BlockPos a = h.absolutePos(new BlockPos(x0, Y, z0));
        BlockPos b = h.absolutePos(new BlockPos(x1, Y, z1));
        for (int cx = Math.min(a.getX(), b.getX()) >> 4; cx <= Math.max(a.getX(), b.getX()) >> 4; cx++) {
            for (int cz = Math.min(a.getZ(), b.getZ()) >> 4; cz <= Math.max(a.getZ(), b.getZ()) >> 4; cz++) {
                h.getLevel().setChunkForced(cx, cz, true);
            }
        }
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                // Only the floor is placed, never air above it: arenas are 19 blocks apart, so a plot reaches into its
                // neighbour's, and clearing air would cut the neighbour's trunks.
                h.setBlock(new BlockPos(x, Y, z), Blocks.STONE);
            }
        }
    }

    /** A spruce-like tree: log column of {@code height}, leaves (persistent) in rings and a 3x3 platform on top. */
    private static void tree(GameTestHelper h, int x, int z, int height) {
        for (int i = 1; i <= height; i++) {
            h.setBlock(new BlockPos(x, Y + i, z), Blocks.SPRUCE_LOG);
        }
        var leaves = Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        int[][] rings = {{height - 3, 1}, {height - 2, 2}, {height - 1, 2}, {height, 1}, {height + 1, 1}};
        for (int[] ring : rings) {
            for (int dx = -ring[1]; dx <= ring[1]; dx++) {
                for (int dz = -ring[1]; dz <= ring[1]; dz++) {
                    BlockPos p = new BlockPos(x + dx, Y + ring[0], z + dz);
                    if (!(dx == 0 && dz == 0 && ring[0] <= height)) {
                        h.setBlock(p, leaves);
                    }
                }
            }
        }
    }

    private static CanopyBreaker boss(GameTestHelper h, int x, int z, float yaw) {
        CanopyBreaker b = h.spawn(ModEntities.CANOPY_BREAKER.get(), new BlockPos(x, Y + 1, z));
        b.setYRot(yaw);
        b.setYHeadRot(yaw);
        b.setClubDrawn(true);
        b.setOnGround(true);
        b.setOpeningCalled(true); // the opening call has its own tests
        return b;
    }

    private static LivingEntity dummy(GameTestHelper h, double x, double y, double z) {
        var pig = h.spawnWithNoFreeWill(EntityType.PIG, new BlockPos((int) x, (int) y, (int) z));
        pig.setPos(h.absoluteVec(new Vec3(x + 0.5D, y, z + 0.5D)));
        pig.setNoGravity(true);
        pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0D);
        pig.setHealth(100.0F);
        return pig;
    }

    private static void setBiome(GameTestHelper helper, BlockPos absPos, ResourceKey<Biome> key) {
        ServerLevel level = helper.getLevel();
        var biome = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(key);
        var sampler = level.getChunkSource().randomState().sampler();
        for (int dx = -32; dx <= 32; dx += 16) {
            for (int dz = -32; dz <= 32; dz += 16) {
                level.getChunkAt(absPos.offset(dx, 0, dz)).fillBiomesFromNoise((x, y, z, s) -> biome, sampler);
            }
        }
    }

    /** Samples every tick until {@code stop} holds or {@code max} ticks pass, then runs {@code done} (which asserts the outcome). */
    private static void waitUntil(GameTestHelper h, int max, Consumer<Integer> each, java.util.function.BooleanSupplier stop, Runnable done) {
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

    private static String describe(CanopyBreaker b) {
        return b.getActivity() + "/" + b.getAnim() + " at " + b.position() + ", routes aborted " + b.getRoutesAborted()
                + ", grip: '" + b.getGripLossReason() + "', route: '" + b.getRouteFailure() + "'"
                + (b.getRoute() == null ? "" : ", approach " + b.getRoute().approach + " (horizontal "
                + Math.hypot(b.getX() - b.getRoute().approach.x, b.getZ() - b.getRoute().approach.z) + "), nav done "
                + b.getNavigation().isDone() + ", path " + b.getNavigation().getPath());
    }

    /** Samples a value every tick for {@code ticks} ticks starting at {@code from}, then calls {@code done}. */
    private static void sampleEachTick(GameTestHelper h, int from, int ticks, Consumer<Integer> each, Runnable done) {
        for (int t = 0; t < ticks; t++) {
            final int tick = t;
            h.runAfterDelay(from + t, () -> each.accept(tick));
        }
        h.runAfterDelay(from + ticks, done);
    }

    // ------------------------------------------------------------------------------------------------ tests
    // ------------------------------------------------------------------------------------------------ minions
    /** A damageable survival player (the server-player mock is invulnerable until its client "loads"). */
    private static Player survivalPlayer(GameTestHelper h, double x, double z) {
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(h.absoluteVec(new Vec3(x + 0.5D, Y + 1, z + 0.5D)));
        player.setOnGround(true);
        return player;
    }

    private static BarkMacaque macaque(GameTestHelper h, int x, int z) {
        BarkMacaque m = h.spawn(ModEntities.BARK_MACAQUE.get(), new BlockPos(x, Y + 1, z));
        m.setOnGround(true);
        return m;
    }

    private static void macaqueRegistration(GameTestHelper helper) {
        floor(helper, 0, 0, 12, 12);
        BarkMacaque m = macaque(helper, 6, 6);
        helper.assertValueEqual(m.getMaxHealth(), 14.0F, "14 HP");
        helper.assertValueEqual(m.getAttributeValue(Attributes.ARMOR), 2.0D, "armor 2");
        helper.assertValueEqual(m.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.32D, "ground speed 0.32");
        helper.assertTrue(!m.causeFallDamage(10.0D, 1.0F, helper.getLevel().damageSources().fall()), "no fall damage");
        helper.assertTrue(m.getOwnerUuid() == null, "a spawn-egg macaque has no owner");
        helper.assertTrue(ModItems.BARK_MACAQUE_SPAWN_EGG.get() instanceof SpawnEggItem, "spawn egg registered");
        var root = BarkMacaqueModel.createBodyLayer().bakeRoot();
        Set<String> ids = new HashSet<>();
        for (MacaqueAnim a : MacaqueAnim.values()) {
            helper.assertTrue(BarkMacaqueAnimations.get(a) != null && BarkMacaqueAnimations.get(a).lengthInSeconds() > 0.0F, a + " has keyframes");
            helper.assertTrue(BarkMacaqueAnimations.get(a).bake(root) != null, a + " bakes");
            helper.assertTrue(ids.add(a.name()), a + " is unique");
        }
        helper.assertTrue(MacaqueAnim.values().length >= 12, "twelve animations (" + MacaqueAnim.values().length + ")");
        m.discard();
        helper.succeed();
    }

    private static void macaqueLeapsAndBitesOnce(GameTestHelper helper) {
        floor(helper, 0, 0, 16, 16);
        Player player = survivalPlayer(helper, 8, 4);
        BarkMacaque m = macaque(helper, 8, 10);
        float before = player.getHealth();
        helper.assertTrue(m.canAttack(player), "a survival player is a valid target");
        helper.assertTrue(m.startLeap(player), "the leap starts");
        helper.assertValueEqual(m.getActivity(), BarkMacaque.Activity.LEAP_PREPARE, "it crouches first");
        helper.assertValueEqual(m.getAnim(), MacaqueAnim.LEAP_CROUCH, "with the crouch animation");
        List<MacaqueAnim> seen = new ArrayList<>();
        Vec3[] last = {null};
        double[] maxStep = {0.0D};
        String[] firstNull = {"never"};
        waitUntil(helper, 90, t -> {
            if (m.getTarget() == null && firstNull[0].equals("never")) {
                firstNull[0] = "tick " + t + " in " + m.getActivity() + " alive=" + player.isAlive() + " removed=" + player.isRemoved();
            }
            if (seen.isEmpty() || seen.getLast() != m.getAnim()) {
                seen.add(m.getAnim());
            }
            if (last[0] != null) {
                maxStep[0] = Math.max(maxStep[0], m.position().distanceTo(last[0]));
            }
            last[0] = m.position();
        }, () -> m.getBitesLanded() > 0 && m.getActivity() == BarkMacaque.Activity.GROUND, () -> {
            helper.assertTrue(m.getBitesLanded() == 1, "exactly one bite per leap (" + m.getBitesLanded() + ") " + seen + " target first null: " + firstNull[0] + " activity " + m.getActivity() + " target " + m.getTarget() + " macaque " + m.position() + " player " + player.position());
            helper.assertTrue(Math.abs((before - player.getHealth()) - 3.0F) < 0.01F, "the bite does 3 damage (took " + (before - player.getHealth()) + ")");
            helper.assertTrue(seen.indexOf(MacaqueAnim.LEAP_CROUCH) < seen.indexOf(MacaqueAnim.LEAP_AIR)
                    && seen.indexOf(MacaqueAnim.LEAP_AIR) < seen.indexOf(MacaqueAnim.BITE)
                    && seen.indexOf(MacaqueAnim.BITE) < seen.indexOf(MacaqueAnim.RETREAT), "crouch, flight, bite and retreat play in order: " + seen);
            helper.assertTrue(maxStep[0] < 1.0D, "a real leap, never a teleport (largest step " + maxStep[0] + ")");
            helper.assertTrue(m.getLeapsStarted() == 1, "a single leap");
            m.discard();
            helper.succeed();
        });
    }

    private static void macaqueClimbsToElevatedTarget(GameTestHelper helper) {
        floor(helper, 0, 0, 20, 24);
        tree(helper, 8, 12, 9);
        Player player = survivalPlayer(helper, 8, 12);
        player.setPos(helper.absoluteVec(new Vec3(8.5D, Y + 11, 12.5D)));
        BarkMacaque m = macaque(helper, 6, 16);
        m.setTarget(player);
        Vec3[] last = {null};
        double[] maxStep = {0.0D};
        double[] maxY = {-1000.0D};
        waitUntil(helper, 420, t -> {
            if (last[0] != null) {
                maxStep[0] = Math.max(maxStep[0], m.position().distanceTo(last[0]));
            }
            last[0] = m.position();
            maxY[0] = Math.max(maxY[0], m.getY());
        }, () -> maxY[0] >= helper.absoluteVec(new Vec3(0, Y + 8, 0)).y, () -> {
            helper.assertTrue(maxY[0] >= helper.absoluteVec(new Vec3(0, Y + 8, 0)).y,
                    "the macaque climbs the trunk towards a target high in the tree (best height " + maxY[0] + ", now " + m.position() + ")");
            helper.assertTrue(maxStep[0] < 1.0D, "by climbing, never by teleporting (largest step " + maxStep[0] + ")");
            m.discard();
            helper.succeed();
        });
    }

    private static void macaqueBoundToBoss(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 0, 0, 16, 16);
        CanopyBreaker b = boss(helper, 8, 6, 0.0F);
        BarkMacaque m = macaque(helper, 8, 9);
        m.setOwner(b);
        BarkMacaque other = macaque(helper, 11, 9);
        other.setOwner(b);
        helper.assertTrue(m.isOwnedBy(b), "bound to the boss");
        helper.assertTrue(!m.canAttack(b), "a macaque never targets the boss");
        helper.assertTrue(!m.canAttack(other), "nor another macaque");
        helper.assertTrue(!b.canAttack(m), "the boss never targets a macaque");
        float before = m.getHealth();
        helper.assertTrue(!m.hurtServer(level, level.damageSources().mobAttack(b), 9.0F), "the boss club cannot hurt a macaque");
        helper.assertTrue(!m.hurtServer(level, level.damageSources().mobAttack(other), 9.0F), "nor can its kin");
        helper.assertValueEqual(m.getHealth(), before, "health untouched");
        // a swing right through them hits nobody
        b.setTarget(null);
        helper.assertTrue(b.startAttack(Attack.CANOPY_SWEEP, m), "the sweep starts");
        helper.runAfterDelay(CanopyBreaker.SWEEP_PREP + CanopyBreaker.SWEEP_HIT_TICK + 4, () -> {
            helper.assertValueEqual(m.getHealth(), before, "a sweep through the minions spares them");
            b.discard();
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(m.isRemoved() && other.isRemoved(), "removing the boss dismisses its minions");
                helper.succeed();
            });
        });
    }

    private static void minionsOpeningReinforcementsAndCap(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CanopyBreaker b = twoTrees(helper);
        b.startIntro(level);
        helper.assertTrue(b.isOpeningCalled(), "the intro makes the opening call");
        helper.assertValueEqual(b.getPendingMinions(), CanopyBreaker.OPENING_MINIONS, "two minions on their way, warned by leaves");
        helper.runAfterDelay(CanopyBreaker.INTRO_TICKS + 30, () -> {
            helper.assertValueEqual(b.getMinions(level).size(), 2, "the opening brings two minions");
            helper.assertValueEqual(b.getMinionsSummoned(), 2, "counted once each");
            for (BarkMacaque m : b.getMinions(level)) {
                helper.assertTrue(m.isOwnedBy(b), "owned by the boss");
            }
            b.setHealth(60.0F);
            b.hurtServer(level, level.damageSources().generic(), 5.0F);
            helper.assertValueEqual(b.getActivity(), Activity.TRANSITION, "stage two begins");
        });
        int t2 = CanopyBreaker.INTRO_TICKS + 30;
        helper.runAfterDelay(t2 + 40, () -> helper.assertValueEqual(b.getMinions(level).size(), 3, "stage two adds one reinforcement"));
        helper.runAfterDelay(t2 + CanopyBreaker.TRANSITION_TWO_TICKS + 6, () -> {
            b.setHealth(24.0F);
            b.invulnerableTime = 0;
            b.hurtServer(level, level.damageSources().generic(), 1.0F);
            helper.assertValueEqual(b.getActivity(), Activity.TRANSITION, "stage three begins");
        });
        int t3 = t2 + CanopyBreaker.TRANSITION_TWO_TICKS + 6;
        helper.runAfterDelay(t3 + 46, () -> {
            helper.assertValueEqual(b.getMinions(level).size(), 4, "stage three adds the last one, up to the cap");
            helper.assertValueEqual(b.callMinions(level, 3, 1), 0, "a full arena takes no more calls");
            b.setHealth(b.getMaxHealth());
            b.invulnerableTime = 0;
            b.hurtServer(level, level.damageSources().generic(), 1.0F);
        });
        helper.runAfterDelay(t3 + 90, () -> {
            helper.assertValueEqual(b.getMinions(level).size(), 4, "healing the boss calls nobody again");
            helper.assertValueEqual(b.getMinionsSummoned(), 4, "four in total, never more");
            b.discard();
            helper.succeed();
        });
    }

    private static void minionsLeaveWithDeadBoss(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 0, 0, 24, 24);
        CanopyBreaker b = boss(helper, 10, 10, 0.0F);
        helper.assertValueEqual(b.callMinions(level, 2, 2), 2, "two minions called");
        helper.runAfterDelay(14, () -> {
            List<BarkMacaque> minions = b.getMinions(level);
            helper.assertValueEqual(minions.size(), 2, "they arrived");
            b.hurtServer(level, level.damageSources().generic(), 500.0F);
            helper.assertValueEqual(b.getActivity(), Activity.DYING, "the boss is dying");
            helper.runAfterDelay(2, () -> {
                for (BarkMacaque m : minions) {
                    helper.assertTrue(m.isRemoved(), "no minion outlives the boss");
                }
                helper.succeed();
            });
        });
    }

    private static void orphanMinionIsDismissed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 0, 0, 16, 16);
        CanopyBreaker b = boss(helper, 8, 6, 0.0F);
        BarkMacaque m = macaque(helper, 8, 10);
        m.setOwner(b);
        b.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK); // gone without the death path: only the orphan rule can clean up
        waitUntil(helper, 120, t -> { }, m::isRemoved, () -> {
            helper.assertTrue(m.isRemoved(), "an orphaned minion leaves instead of roaming the arena");
            helper.succeed();
        });
    }

    private static void introChestBeatAndRoar(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 0, 0, 16, 16);
        CanopyBreaker b = boss(helper, 8, 8, 0.0F);
        b.startIntro(level);
        List<CanopyAnim> seen = new ArrayList<>();
        boolean[] vulnerable = {false};
        waitUntil(helper, 260, t -> {
            if (seen.isEmpty() || seen.getLast() != b.getAnim()) {
                seen.add(b.getAnim());
            }
            if (b.getActivity() == Activity.INTRO && b.hurtServer(level, level.damageSources().generic(), 1.0F)) {
                vulnerable[0] = true;
            }
        }, () -> b.getActivity() != Activity.INTRO, () -> {
            int look = seen.indexOf(CanopyAnim.IDLE_WATCH);
            int beat = seen.indexOf(CanopyAnim.INTRO_CHEST_BEAT);
            int roar = seen.indexOf(CanopyAnim.ROAR_AGGRO);
            helper.assertTrue(look >= 0 && beat > look && roar > beat,
                    "he looks around, beats his chest, then roars: " + seen);
            helper.assertTrue(!vulnerable[0], "no damage gets through during the intro");
            helper.assertValueEqual(b.getActivity(), Activity.GROUND, "the intro ends in combat");
            b.discard();
            helper.succeed();
        });
    }

    private static void groundOpeningCall(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 0, 0, 24, 24);
        CanopyBreaker b = helper.spawn(ModEntities.CANOPY_BREAKER.get(), new BlockPos(10, Y + 1, 10));
        b.setOnGround(true);
        helper.assertTrue(!b.isOpeningCalled(), "a boss spawned without the idol has not called yet");
        LivingEntity pig = dummy(helper, 10, Y + 1, 18);
        b.setTarget(pig);
        waitUntil(helper, 60, t -> { }, b::isOpeningCalled, () -> {
            helper.assertTrue(b.isOpeningCalled(), "the first sight of a target opens the fight with the call");
            helper.assertValueEqual(b.getActivity(), Activity.ROAR, "he roars to call them");
            helper.assertValueEqual(b.getAnim(), CanopyAnim.TAUNT, "with the roar animation");
            helper.assertValueEqual(b.getPendingMinions(), CanopyBreaker.OPENING_MINIONS, "two minions announced");
        });
        helper.runAfterDelay(90, () -> {
            helper.assertValueEqual(b.getMinions(level).size(), 2, "and two arrive");
            helper.assertValueEqual(b.getMinionsSummoned(), 2, "exactly the two of the opening");
            b.discard();
            pig.discard();
            helper.succeed();
        });
    }

    // ------------------------------------------------------------------------------------------------ gorilla moves
    /** A free pig with plenty of health, standing on the floor (gravity on, so a throw really flies). */
    private static LivingEntity sturdyPig(GameTestHelper h, double x, double y, double z) {
        var pig = h.spawnWithNoFreeWill(EntityType.PIG, new BlockPos((int) x, (int) y, (int) z));
        pig.setPos(h.absoluteVec(new Vec3(x + 0.5D, y, z + 0.5D)));
        pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0D);
        pig.setHealth(200.0F);
        return pig;
    }

    private static void clubIsHeldByTheHandleEnd(GameTestHelper helper) {
        var root = CanopyBreakerModel.createBodyLayer().bakeRoot();
        float[] range = {Float.MAX_VALUE, -Float.MAX_VALUE};
        root.visit(new PoseStack(), (pose, part, index, cube) -> {
            if (part.contains("club_hand")) {
                range[0] = Math.min(range[0], cube.minY);
                range[1] = Math.max(range[1], cube.maxY);
            }
        });
        // the club points along -y from the fist: the head is far out, the handle ends right at the hand
        helper.assertTrue(range[0] <= -24.0F, "the head is far from the hand (top of the club at " + range[0] + ")");
        helper.assertTrue(range[1] <= 6.0F, "the hand holds the butt end of the handle, nothing trails behind it (butt at " + range[1] + ")");
        helper.succeed();
    }

    private static void clubBlowsBreakTheGuard(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // a mock player is not in the level's entity lists, so a swing cannot find it: the guard break every club blow
        // makes (CanopyBreaker.strike calls breakGuard first) is exercised directly
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack shield = new ItemStack(Items.SHIELD);
        player.setItemInHand(InteractionHand.OFF_HAND, shield);
        helper.assertTrue(!CanopyBreaker.breakGuard(level, player), "a player who is not blocking has no guard to break");
        player.startUsingItem(InteractionHand.OFF_HAND);
        for (int i = 0; i < 8; i++) {
            player.tick(); // the shield needs a few ticks to come up
        }
        helper.assertTrue(player.isBlocking(), "the player is blocking with the shield");
        helper.assertTrue(CanopyBreaker.breakGuard(level, player), "the blow breaks the guard");
        helper.assertTrue(player.getCooldowns().isOnCooldown(shield), "the shield is on cooldown");
        helper.assertTrue(!player.isBlocking() && !player.isUsingItem(), "the player is no longer blocking");
        player.startUsingItem(InteractionHand.OFF_HAND);
        helper.assertTrue(!CanopyBreaker.breakGuard(level, player), "a second blow finds the shield already down");
        helper.succeed();
    }

    private static void grabHoldsAndThrowsFar(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 0, 0, 30, 30);
        CanopyBreaker b = boss(helper, 8, 6, 0.0F);
        LivingEntity pig = sturdyPig(helper, 8, Y + 1, 8);
        b.setTarget(pig);
        helper.assertTrue(b.startAttack(Attack.GRAB_THROW, pig), "the grab starts");
        helper.assertValueEqual(b.getAnim(), CanopyAnim.ATTACK_GRAB_PREPARE, "with its own wind-up animation");
        List<CanopyAnim> seen = new ArrayList<>();
        double[] heldHeight = {-1.0D};
        double[] throwSpeed = {0.0D};
        double[] throwUp = {0.0D};
        waitUntil(helper, 160, t -> {
            if (seen.isEmpty() || seen.getLast() != b.getAnim()) {
                seen.add(b.getAnim());
            }
            if (b.getHeld() == pig && heldHeight[0] < 0.0D) {
                heldHeight[0] = pig.getY() - b.getY();
                helper.assertTrue(pig.getVehicle() == b, "the held target is carried by him");
            }
            if (b.getThrowsDone() > 0 && throwSpeed[0] == 0.0D) {
                Vec3 v = pig.getDeltaMovement();
                throwSpeed[0] = Math.hypot(v.x, v.z);
                throwUp[0] = v.y;
            }
        }, () -> b.getThrowsDone() > 0 && pig.onGround() && b.getActivity() == Activity.GROUND, () -> {
            helper.assertValueEqual(b.getGrabsLanded(), 1, "one grab landed");
            helper.assertValueEqual(b.getThrowsDone(), 1, "and one throw");
            helper.assertTrue(heldHeight[0] > 1.8D, "he lifted the target up (held " + heldHeight[0] + " blocks above his feet)");
            helper.assertTrue(throwSpeed[0] >= 1.4D && throwUp[0] > 0.9D, "the throw is fast and rising (" + throwSpeed[0] + ", " + throwUp[0] + ")");
            helper.assertTrue(pig.getVehicle() == null, "it is no longer carried");
            double flung = Math.hypot(pig.getX() - b.getX(), pig.getZ() - b.getZ());
            helper.assertTrue(flung > 7.0D, "the target was thrown far away from him (" + flung + " blocks)");
            helper.assertTrue(pig.getHealth() <= 200.0F - CanopyBreaker.GRAB_SQUEEZE_DAMAGE - CanopyBreaker.GRAB_THROW_DAMAGE + 0.01F,
                    "squeezed and thrown: both hurt (health " + pig.getHealth() + ")");
            int lift = seen.indexOf(CanopyAnim.ATTACK_GRAB_LIFT);
            int hurl = seen.indexOf(CanopyAnim.ATTACK_GRAB_THROW);
            int rest = seen.indexOf(CanopyAnim.ATTACK_GRAB_RECOVERY);
            helper.assertTrue(lift >= 0 && hurl > lift && rest > hurl, "prepare, lift, throw and recovery play in order: " + seen);
            b.discard();
            pig.discard();
            helper.succeed();
        });
    }

    private static void grabIgnoresShieldsAndMissesOpenHim(GameTestHelper helper) {
        floor(helper, 0, 0, 30, 30);
        CanopyBreaker b = boss(helper, 8, 6, 0.0F);
        LivingEntity pig = sturdyPig(helper, 8, Y + 1, 9);
        b.setTarget(pig);
        helper.assertTrue(b.startAttack(Attack.GRAB_THROW, pig), "the grab starts");
        // it was in reach when the wind-up began; by the time the hand closes it has slipped away
        helper.runAfterDelay(CanopyBreaker.GRAB_THROW_PREP + 3, () -> pig.setPos(helper.absoluteVec(new Vec3(20.5D, Y + 1, 20.5D))));
        waitUntil(helper, 100, t -> { }, () -> b.getAnim() == CanopyAnim.ATTACK_GRAB_RECOVERY, () -> {
            helper.assertTrue(b.getAnim() == CanopyAnim.ATTACK_GRAB_RECOVERY, "with nothing in his hand he goes straight to the open recovery");
            helper.assertValueEqual(b.getGrabsLanded(), 0, "nothing was grabbed");
            helper.assertTrue(b.getHeld() == null, "and nothing is held");
            helper.assertValueEqual(b.getPhase(), CanopyBreaker.Phase.RECOVER, "he is in the recovery window");
            b.discard();
            pig.discard();
            helper.succeed();
        });
    }

    private static void poundThrowsEveryoneAround(GameTestHelper helper) {
        floor(helper, 0, 0, 30, 30);
        CanopyBreaker b = boss(helper, 10, 6, 0.0F);
        LivingEntity near = sturdyPig(helper, 10, Y + 1, 9);
        LivingEntity side = sturdyPig(helper, 13, Y + 1, 8);
        LivingEntity far = sturdyPig(helper, 10, Y + 1, 18);
        b.setTarget(near);
        helper.assertTrue(b.startAttack(Attack.POUND, near), "the pound starts");
        helper.assertValueEqual(b.getAnim(), CanopyAnim.ATTACK_POUND_PREPARE, "with its own wind-up animation");
        double[] popped = {0.0D, 0.0D};
        waitUntil(helper, 120, t -> {
            if (b.getPoundsLanded() > 0 && popped[0] == 0.0D) {
                popped[0] = near.getDeltaMovement().y;
                popped[1] = side.getDeltaMovement().y;
            }
        }, () -> b.getPoundsLanded() > 0, () -> {
            helper.assertValueEqual(b.getPoundsLanded(), 1, "the fists landed once");
            helper.assertTrue(near.getHealth() < 200.0F && side.getHealth() < 200.0F, "everyone around him is hurt");
            helper.assertTrue(far.getHealth() == 200.0F, "the one outside the shockwave is not");
            helper.assertTrue(popped[0] > 0.3D && popped[1] > 0.3D, "and thrown into the air (" + popped[0] + ", " + popped[1] + ")");
            b.discard();
            near.discard();
            side.discard();
            far.discard();
            helper.succeed();
        });
    }

    private static void backhandThrowsBackAndUp(GameTestHelper helper) {
        floor(helper, 0, 0, 30, 30);
        CanopyBreaker b = boss(helper, 8, 6, 0.0F); // facing +z
        LivingEntity pig = sturdyPig(helper, 8, Y + 1, 9);
        double startY = pig.getY();
        double startX = pig.getX();
        double startZ = pig.getZ();
        b.setTarget(pig);
        helper.assertTrue(b.startAttack(Attack.BACKHAND, pig), "the backhand starts");
        double[] peak = {startY};
        waitUntil(helper, 60, t -> peak[0] = Math.max(peak[0], pig.getY()), () -> false, () -> {
            double flownBack = pig.getZ() - startZ;
            helper.assertTrue(peak[0] > startY + 0.8D, "the backhand lifts him off the ground (rose " + (peak[0] - startY) + " blocks)");
            helper.assertTrue(flownBack > 2.0D, "and throws him back, away from the boss (flew " + flownBack + " blocks)");
            helper.assertTrue(Math.abs(pig.getX() - startX) < 1.0D, "straight back, not sideways (drift " + (pig.getX() - startX) + ")");
            b.discard();
            pig.discard();
            helper.succeed();
        });
    }

    private static void groundLeapReachesAPillar(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 0, 0, 30, 30);
        for (int x = 14; x <= 16; x++) { // a 3-block pillar: too high to hit, too low for a tree climb
            for (int z = 11; z <= 13; z++) {
                for (int y = Y + 1; y <= Y + 3; y++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                }
            }
        }
        CanopyBreaker b = boss(helper, 8, 12, -90.0F);
        LivingEntity pig = sturdyPig(helper, 15, Y + 4, 12);
        b.setTarget(pig);
        helper.assertTrue(b.startGroundLeap(level, pig), "a leap up onto the pillar is planned: " + b.getRouteFailure());
        Vec3[] last = {null};
        double[] maxStep = {0.0D};
        waitUntil(helper, 160, t -> {
            if (last[0] != null) {
                maxStep[0] = Math.max(maxStep[0], b.position().distanceTo(last[0]));
            }
            last[0] = b.position();
        }, () -> b.getDropsLanded() > 0 && b.getActivity() == Activity.GROUND, () -> {
            helper.assertTrue(b.getDropsLanded() == 1, "he landed (" + b.getActivity() + " at " + b.position() + ")");
            helper.assertTrue(b.getY() >= helper.absoluteVec(new Vec3(0, Y + 4, 0)).y - 0.3D,
                    "on top of the pillar, level with the target (y=" + b.getY() + ")");
            helper.assertTrue(maxStep[0] < 1.45D, "by leaping, never by teleporting (largest step " + maxStep[0] + ")");
            b.discard();
            pig.discard();
            helper.succeed();
        });
    }

    private static void groundLeapCrossesOpenGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 0, 0, 40, 30);
        CanopyBreaker b = boss(helper, 6, 12, -90.0F); // facing +x
        LivingEntity pig = sturdyPig(helper, 19, Y + 1, 12);
        b.setTarget(pig);
        helper.assertTrue(b.startGroundLeap(level, pig), "a bound across 13 open blocks is planned: " + b.getRouteFailure());
        helper.assertValueEqual(b.getActivity(), Activity.JUMP_PREPARATION, "he crouches first");
        Vec3[] last = {null};
        double[] maxStep = {0.0D};
        boolean[] flew = {false};
        StringBuilder trace = new StringBuilder();
        double[] landedAt = {-1.0D};
        waitUntil(helper, 160, t -> {
            if (landedAt[0] < 0.0D && b.getDropsLanded() > 0) {
                landedAt[0] = b.distanceTo(pig); // measured at the slam: the hurt pig then runs off
            }
            if (t % 4 == 0 && t < 90) {
                trace.append(t).append(':').append(b.getActivity().name().charAt(0)).append(String.format("%.1f", b.getX() - helper.absoluteVec(Vec3.ZERO).x)).append(' ');
            }
            if (b.getActivity() == Activity.AIRBORNE) {
                flew[0] = true;
            }
            if (last[0] != null) {
                maxStep[0] = Math.max(maxStep[0], b.position().distanceTo(last[0]));
            }
            last[0] = b.position();
        }, () -> b.getDropsLanded() > 0 && b.getActivity() == Activity.GROUND, () -> {
            helper.assertTrue(flew[0], "he really flew");
            helper.assertValueEqual(b.getDropsLanded(), 1, "and landed with a slam");
            helper.assertTrue(maxStep[0] < 1.45D, "a leap, never a teleport (largest step " + maxStep[0] + ")");
            helper.assertTrue(landedAt[0] >= 0.0D && landedAt[0] < 3.6D, "he landed on the target (" + landedAt[0] + " blocks away; trace " + trace + ")");
            helper.assertTrue(pig.getHealth() <= 200.0F - CanopyBreaker.DROP_DAMAGE + 0.01F, "the landing slam hurts it (health " + pig.getHealth() + ")");
            helper.assertValueEqual(b.getGroundLeapsStarted(), 1, "one leap");
            b.discard();
            pig.discard();
            helper.succeed();
        });
    }

    private static void registrationAndAttributes(GameTestHelper helper) {
        floor(helper, 0, 0, 20, 20);
        CanopyBreaker b = boss(helper, 8, 8, 0.0F);
        helper.assertValueEqual(b.getMaxHealth(), 200.0F, "200 HP");
        helper.assertValueEqual(b.getAttributeValue(Attributes.ARMOR), 8.0D, "armor 8");
        helper.assertValueEqual(b.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.33D, "ground speed 0.33");
        helper.assertValueEqual(b.getBbWidth(), 1.8F, "width 1.8");
        helper.assertValueEqual(b.getBbHeight(), 3.1F, "height 3.1");
        helper.assertTrue(!b.removeWhenFarAway(512.0D), "never despawns by distance");
        helper.assertValueEqual(b.getActivity(), Activity.GROUND, "starts on the ground");
        helper.assertValueEqual(b.getStage(), 1, "stage 1");
        helper.assertTrue(!b.causeFallDamage(10.0D, 1.0F, helper.getLevel().damageSources().fall()), "no fall damage");
        helper.assertTrue(ModItems.HOLLOW_TRUNK_IDOL.get() != null && ModItems.DEADROOT_CLUB.get() != null
                && ModItems.CANOPY_RESIN.get() != null && ModItems.HEARTWOOD_SPLINTER.get() != null, "items registered");
        ItemStack egg = new ItemStack(ModItems.CANOPY_BREAKER_SPAWN_EGG.get());
        helper.assertTrue(egg.getItem() instanceof SpawnEggItem, "spawn egg item");
        helper.assertValueEqual(((SpawnEggItem) egg.getItem()).getType(egg), ModEntities.CANOPY_BREAKER.get(), "egg spawns the boss");
        b.discard();
        helper.succeed();
    }

    private static void animationCatalogueAndUv(GameTestHelper helper) {
        helper.assertTrue(CanopyAnim.values().length >= 85, "at least 85 animations (" + CanopyAnim.values().length + ")");
        Set<String> ids = new HashSet<>();
        for (CanopyAnim a : CanopyAnim.values()) {
            helper.assertTrue(CanopyBreakerAnimations.get(a) != null, a + " has keyframes");
            helper.assertTrue(CanopyBreakerAnimations.get(a).lengthInSeconds() > 0.0F, a + " has a duration");
            helper.assertTrue(ids.add(a.id()), a + " id is unique");
        }
        var root = CanopyBreakerModel.createBodyLayer().bakeRoot();
        for (CanopyAnim a : CanopyAnim.values()) {
            helper.assertTrue(CanopyBreakerAnimations.get(a).bake(root) != null, a + " bakes");
        }
        // UV layout: no two cubes share atlas pixels
        List<float[]> rects = new ArrayList<>();
        List<String> owner = new ArrayList<>();
        root.visit(new PoseStack(), (pose, part, index, cube) -> {
            for (var polygon : cube.polygons) {
                float minU = 9, minV = 9, maxU = -1, maxV = -1;
                for (var vertex : polygon.vertices()) {
                    minU = Math.min(minU, vertex.u());
                    maxU = Math.max(maxU, vertex.u());
                    minV = Math.min(minV, vertex.v());
                    maxV = Math.max(maxV, vertex.v());
                }
                rects.add(new float[] {minU * 256, minV * 256, maxU * 256, maxV * 256});
                owner.add(part + "#" + index);
            }
        });
        for (int i = 0; i < rects.size(); i++) {
            for (int j = i + 1; j < rects.size(); j++) {
                if (owner.get(i).equals(owner.get(j))) {
                    continue;
                }
                float[] a = rects.get(i);
                float[] b = rects.get(j);
                boolean overlap = a[0] < b[2] - 0.01F && b[0] < a[2] - 0.01F && a[1] < b[3] - 0.01F && b[1] < a[3] - 0.01F;
                helper.assertTrue(!overlap, "UV overlap between " + owner.get(i) + " and " + owner.get(j));
            }
        }
        helper.succeed();
    }

    private static void idolConditions(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        final int ox = 3000; // far from every other test, whose bosses would count as "already active"
        floor(helper, ox, 0, ox + 40, 40);
        tree(helper, ox + 8, 8, 9);
        tree(helper, ox + 14, 8, 9);
        BlockPos log = helper.absolutePos(new BlockPos(ox + 8, Y + 2, 8));
        for (int cx = -1; cx <= 1; cx++) { // the clearing the boss arrives in may lie in a neighbouring chunk
            for (int cz = -1; cz <= 1; cz++) {
                level.setChunkForced((log.getX() >> 4) + cx, (log.getZ() >> 4) + cz, true);
            }
        }
        java.util.function.BooleanSupplier plotTicks = () -> {
            for (int cx = -1; cx <= 1; cx++) { // every forced chunk, since the boss may arrive in a neighbouring one
                for (int cz = -1; cz <= 1; cz++) {
                    if (!level.isPositionEntityTicking(log.offset(cx * 16, 0, cz * 16))) {
                        return false;
                    }
                }
            }
            return true;
        };
        waitUntil(helper, 400, t -> { }, plotTicks, () -> {
            helper.assertTrue(plotTicks.getAsBoolean(), "the far plot ticks entities");
            idolSequence(helper, level, log, ox);
        });
    }

    private static void idolSequence(GameTestHelper helper, ServerLevel level, BlockPos log, int ox) {
        ItemStack idol = new ItemStack(ModItems.HOLLOW_TRUNK_IDOL.get(), 2);

        helper.assertValueEqual(HollowTrunkIdolItem.trySummon(level, log.above(30), null, idol),
                HollowTrunkIdolItem.SummonResult.WRONG_BLOCK, "rejects a non-log block");
        setBiome(helper, log, Biomes.PLAINS);
        helper.setTime(18000L);
        helper.assertValueEqual(HollowTrunkIdolItem.trySummon(level, log, null, idol),
                HollowTrunkIdolItem.SummonResult.WRONG_BIOME, "rejects a plains biome");
        setBiome(helper, log, Biomes.OLD_GROWTH_PINE_TAIGA);
        helper.setTime(6000L);
        helper.assertValueEqual(HollowTrunkIdolItem.trySummon(level, log, null, idol),
                HollowTrunkIdolItem.SummonResult.NOT_NIGHT, "rejects daytime");
        helper.setTime(18000L);
        BlockPos lone = helper.absolutePos(new BlockPos(ox + 36, Y + 1, 36));
        level.setBlock(lone, Blocks.SPRUCE_LOG.defaultBlockState(), 3);
        helper.assertValueEqual(HollowTrunkIdolItem.trySummon(level, lone, null, idol),
                HollowTrunkIdolItem.SummonResult.FEW_TREES, "rejects a lone trunk");
        helper.assertValueEqual(idol.getCount(), 2, "a failed use never consumes the idol");
        helper.assertValueEqual(HollowTrunkIdolItem.evaluateConditions(level, log), HollowTrunkIdolItem.SummonResult.SUCCESS,
                "old-growth taiga at night with trees is accepted");
        setBiome(helper, log, Biomes.DARK_FOREST);
        helper.assertValueEqual(HollowTrunkIdolItem.evaluateConditions(level, log), HollowTrunkIdolItem.SummonResult.SUCCESS,
                "dark forest is accepted");

        helper.assertValueEqual(HollowTrunkIdolItem.trySummon(level, log, null, idol), HollowTrunkIdolItem.SummonResult.SUCCESS, "summons");
        helper.assertValueEqual(idol.getCount(), 1, "success consumes exactly one idol");
        List<CanopyBreaker> found = level.getEntitiesOfClass(CanopyBreaker.class, new AABB(log).inflate(30.0D));
        helper.assertValueEqual(found.size(), 1, "exactly one boss");
        CanopyBreaker b = found.getFirst();
        helper.assertValueEqual(b.getActivity(), Activity.INTRO, "starts with the short intro");
        helper.assertValueEqual(b.getAnim(), CanopyAnim.IDLE_WATCH, "the intro opens with him looking around");
        helper.assertValueEqual(HollowTrunkIdolItem.trySummon(level, log, null, idol), HollowTrunkIdolItem.SummonResult.ALREADY_ACTIVE,
                "no duplicate boss");
        helper.assertValueEqual(idol.getCount(), 1, "duplicate attempt does not consume");
        helper.assertTrue(!b.hurtServer(level, level.damageSources().generic(), 10.0F), "invulnerable during the intro");

        int[] introEnded = {-1};
        sampleEachTick(helper, 1, 260, t -> {
            if (introEnded[0] < 0 && b.getActivity() != Activity.INTRO) {
                introEnded[0] = t;
            }
        }, () -> {
            helper.assertTrue(introEnded[0] > 0, "the intro ends (" + describe(b) + ")");
            helper.assertValueEqual(b.getActivity(), Activity.GROUND, "the intro ends in combat");
            helper.assertTrue(introEnded[0] >= CanopyBreaker.INTRO_TICKS - 4, "and takes its full length (" + introEnded[0] + " ticks)");
            helper.assertTrue(b.getBossEvent().isVisible(), "boss bar visible");
            b.discard();
            helper.succeed();
        });
    }

    private static void chasesAndDrawsClub(GameTestHelper helper) {
        floor(helper, 0, 0, 40, 50);
        CanopyBreaker b = helper.spawn(ModEntities.CANOPY_BREAKER.get(), new BlockPos(6, Y + 1, 6));
        LivingEntity pig = dummy(helper, 6, Y + 1, 30);
        b.setTarget(pig);
        double start = b.distanceTo(pig);
        helper.assertTrue(!b.isClubDrawn(), "the club starts on the back");
        waitUntil(helper, 300, t -> { }, () -> b.isClubDrawn() && (b.distanceTo(pig) < start - 10.0D || pig.getHealth() < 100.0F), () -> {
            helper.assertTrue(b.isClubDrawn(), "he draws the club when the target is near");
            helper.assertTrue(b.distanceTo(pig) < start - 10.0D || pig.getHealth() < 100.0F, "he closes in on a distant target [" + describe(b) + ", distance " + b.distanceTo(pig) + "]");
            b.discard();
            pig.discard();
            helper.succeed();
        });
    }

    private static void attackTrunkSlam(GameTestHelper helper) {
        floor(helper, 0, 0, 30, 30);
        CanopyBreaker b = boss(helper, 10, 10, 0.0F);
        LivingEntity front = dummy(helper, 10, Y + 1, 13);
        LivingEntity behind = dummy(helper, 10, Y + 1, 6);
        b.setTarget(front);
        helper.assertTrue(b.startAttack(Attack.TRUNK_SLAM, front), "slam starts");
        helper.assertValueEqual(b.getActivity(), Activity.ATTACK, "attack state");
        helper.runAfterDelay(CanopyBreaker.SLAM_PREP - 4, () ->
                helper.assertValueEqual(front.getHealth(), 100.0F, "no damage during the prepare phase"));
        helper.runAfterDelay(CanopyBreaker.SLAM_PREP + CanopyBreaker.SLAM_HIT_TICK + 2, () -> {
            helper.assertTrue(front.getHealth() < 100.0F, "the slam hits a target in front");
            helper.assertValueEqual(behind.getHealth(), 100.0F, "the slam spares a target behind");
            helper.assertValueEqual(b.getPhase(), CanopyBreaker.Phase.EXECUTE, "still executing at the hit");
        });
        helper.runAfterDelay(CanopyBreaker.SLAM_PREP + CanopyBreaker.SLAM_EXEC + 4, () -> {
            helper.assertValueEqual(b.getPhase(), CanopyBreaker.Phase.RECOVER, "recovery window after the blow");
            b.discard();
            front.discard();
            behind.discard();
            helper.succeed();
        });
    }

    private static void attackSweepAndBackhand(GameTestHelper helper) {
        floor(helper, 0, 0, 30, 30);
        CanopyBreaker b = boss(helper, 10, 10, 0.0F);
        LivingEntity front = dummy(helper, 10, Y + 1, 13);
        LivingEntity behind = dummy(helper, 10, Y + 1, 7);
        b.setTarget(front);
        helper.assertTrue(b.startAttack(Attack.CANOPY_SWEEP, front), "sweep starts");
        helper.runAfterDelay(CanopyBreaker.SWEEP_PREP + CanopyBreaker.SWEEP_HIT_TICK + 4, () -> {
            helper.assertTrue(front.getHealth() < 100.0F, "the sweep hits the frontal arc");
            helper.assertValueEqual(behind.getHealth(), 100.0F, "the sweep never hits behind him");
            // second part: backhand on a target that is too close
            float h0 = front.getHealth();
            b.abortAttack();
            front.setPos(b.getX(), b.getY(), b.getZ() + 2.4D);
            front.setHealth(100.0F);
            front.setDeltaMovement(Vec3.ZERO);
            helper.assertTrue(b.startAttack(Attack.BACKHAND, front), "backhand starts");
            helper.assertTrue(h0 < 100.0F, "sanity");
        });
        int t2 = CanopyBreaker.SWEEP_PREP + CanopyBreaker.SWEEP_HIT_TICK + 4;
        helper.runAfterDelay(t2 + CanopyBreaker.BACKHAND_PREP + CanopyBreaker.BACKHAND_HIT_TICK + 4, () -> {
            helper.assertTrue(front.getHealth() < 100.0F, "the backhand hits a target close to him");
            helper.assertTrue(CanopyBreaker.BACKHAND_DAMAGE < CanopyBreaker.SLAM_DAMAGE, "backhand is the lightest blow");
            b.discard();
            front.discard();
            behind.discard();
            helper.succeed();
        });
    }

    private static void attackLungeAndCancel(GameTestHelper helper) {
        floor(helper, 0, 0, 30, 40);
        CanopyBreaker b = boss(helper, 10, 8, 0.0F);
        LivingEntity far = dummy(helper, 10, Y + 1, 16);
        b.setTarget(far);
        helper.assertTrue(b.startAttack(Attack.BRUTE_LUNGE, far), "lunge starts");
        waitUntil(helper, 60, tick -> { }, () -> far.getHealth() < 100.0F || b.getPhase() == CanopyBreaker.Phase.RECOVER, () -> {
            helper.assertTrue(far.getHealth() < 100.0F, "the lunge reaches a target 8 blocks away [" + describe(b) + ", distance " + b.distanceTo(far) + ", pig " + far.position() + "]");
            helper.assertTrue(b.getZ() > helper.absoluteVec(new Vec3(0, 0, 10.0D)).z, "he really advanced");
        });
        // cancellation: the target leaves during the wind-up of a slam
        int t = 64;
        helper.runAfterDelay(t, () -> {
            b.abortAttack();
            far.setPos(b.getX(), b.getY(), b.getZ() + 3.0D);
            far.setHealth(100.0F);
            far.invulnerableTime = 0;
            far.setDeltaMovement(Vec3.ZERO);
            helper.assertTrue(b.startAttack(Attack.TRUNK_SLAM, far), "slam starts again");
        });
        helper.runAfterDelay(t + 8, () -> far.setPos(b.getX(), b.getY() + 9.0D, b.getZ() + 18.0D)); // gone, and high above
        helper.runAfterDelay(t + CanopyBreaker.SLAM_PREP + 8, () -> {
            helper.assertTrue(b.getActivity() != Activity.ATTACK || b.getPhase() == CanopyBreaker.Phase.PREPARE,
                    "a wind-up whose target left never reaches the blow");
            helper.assertValueEqual(far.getHealth(), 100.0F, "nothing was hit");
            helper.assertTrue(b.getAnim() == CanopyAnim.ATTACK_INTERRUPTED || b.getAnim().name().contains("IDLE")
                            || b.getAnim().name().contains("WALK") || b.getAnim().name().contains("RUN") || b.getAnim().name().contains("CLUB"),
                    "the cancelled attack falls back to a combat pose, not to the blow (" + b.getAnim() + ")");
            b.discard();
            far.discard();
            helper.succeed();
        });
    }

    private static void attackAnimationSync(GameTestHelper helper) {
        floor(helper, 0, 0, 30, 30);
        CanopyBreaker b = boss(helper, 10, 10, 0.0F);
        LivingEntity front = dummy(helper, 10, Y + 1, 13);
        b.setTarget(front);
        b.startAttack(Attack.TRUNK_SLAM, front);
        List<CanopyAnim> seen = new ArrayList<>();
        sampleEachTick(helper, 1, CanopyBreaker.SLAM_PREP + CanopyBreaker.SLAM_EXEC + CanopyBreaker.SLAM_RECOVER + 4, t -> {
            CanopyAnim a = b.getAnim();
            if (seen.isEmpty() || seen.getLast() != a) {
                seen.add(a);
            }
        }, () -> {
            int prep = Math.max(seen.indexOf(CanopyAnim.ATTACK_TRUNK_SLAM_PREPARE), seen.indexOf(CanopyAnim.ATTACK_TRUNK_SLAM_PREPARE_ALT));
            int exec = seen.indexOf(CanopyAnim.ATTACK_TRUNK_SLAM_EXECUTE);
            int rec = seen.indexOf(CanopyAnim.ATTACK_TRUNK_SLAM_RECOVERY);
            helper.assertTrue(prep >= 0 && exec > prep && rec > exec, "prepare, execute and recovery animations play in order: " + seen);
            b.discard();
            front.discard();
            helper.succeed();
        });
    }

    private static void treeDetection(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 0, 0, 30, 30);
        tree(helper, 12, 12, 9);
        BlockPos center = helper.absolutePos(new BlockPos(12, Y + 1, 12));
        var trunks = TreeNav.findTrunks(level, center, 6, 6);
        helper.assertValueEqual(trunks.size(), 1, "one trunk found");
        TreeNav.Trunk t = trunks.getFirst();
        helper.assertValueEqual(t.height(), 9, "trunk height");
        Vec3 from = helper.absoluteVec(new Vec3(8.5D, Y + 1, 12.5D));
        Vec3 approach = TreeNav.approachSpot(level, t, from, 1.0D);
        helper.assertTrue(approach != null, "an approach spot on the side facing him");
        helper.assertTrue(TreeNav.hasSupportBelow(level, approach), "the approach spot is on the ground");
        Vec3 perch = TreeNav.findPerch(level, t, 3, 1.8D, 3.1D);
        helper.assertTrue(perch != null, "a perch on top of the canopy");
        helper.assertTrue(perch.y >= t.topY() + 1.0D, "the perch is above the trunk top");
        helper.assertTrue(TreeNav.canOccupy(level, TreeNav.boxAt(perch, 1.8D, 3.1D), false), "the perch is free");
        helper.assertTrue(!TreeNav.canOccupy(level, TreeNav.boxAt(Vec3.atBottomCenterOf(center.above(3)).add(0, 0, 0), 1.8D, 3.1D), false),
                "the trunk itself is not a free space");
        helper.assertTrue(TreeNav.planJump(level, perch, perch.add(30.0D, 0.0D, 0.0D), 0.08D, 1.8D, 3.1D) == null,
                "a jump beyond the physical limits is never planned");
        helper.succeed();
    }

    private static final class PathLog {
        final List<Activity> activities = new ArrayList<>();
        double maxStep = 0.0D;
        Vec3 last;
        int climbTicks = 0;
        double maxClimbY = -1000;
        double landedY = -1000;
        double landedDistance = 99;
        final List<String> timeline = new ArrayList<>();

        void note(Activity a, int tick) {
            if (timeline.isEmpty() || !timeline.getLast().startsWith(a.name() + "@")) {
                timeline.add(a.name() + "@" + tick);
            }
        }
    }

    /** Spawns the standard two-tree plot (A at 8,12 and B at 17,12) and returns the boss near A. */
    private static CanopyBreaker twoTrees(GameTestHelper helper) {
        floor(helper, 0, 0, 40, 30);
        tree(helper, 8, 12, 9);
        tree(helper, 17, 12, 9);
        return boss(helper, 6, 15, 0.0F);
    }

    private static void climbsAndJumps(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CanopyBreaker b = twoTrees(helper);
        LivingEntity pig = dummy(helper, 17, Y + 11, 12);
        b.setTarget(pig);
        helper.assertTrue(b.startRoute(level, pig, true), "a route to the other canopy is planned (climb, perch, jump): " + b.getRouteFailure());
        helper.assertTrue(b.getRoute() != null && b.getRoute().plan != null, "the jump is planned before committing");
        double perchY = b.getRoute().perch.y;
        PathLog log = new PathLog();
        waitUntil(helper, 520, t -> {
            Activity a = b.getActivity();
            log.note(a, t);
            if (log.activities.isEmpty() || log.activities.getLast() != a) {
                log.activities.add(a);
            }
            Vec3 p = b.position();
            if (log.last != null && (a == Activity.CLIMB_UP || a == Activity.AIRBORNE)) {
                log.maxStep = Math.max(log.maxStep, p.distanceTo(log.last));
            }
            if (log.landedY < -900 && b.getJumpsCompleted() > 0) {
                log.landedY = p.y;
                log.landedDistance = Math.sqrt(Math.pow(b.getX() - pig.getX(), 2) + Math.pow(b.getZ() - pig.getZ(), 2));
            }
            if (a == Activity.CLIMB_UP) {
                log.climbTicks++;
                log.maxClimbY = Math.max(log.maxClimbY, p.y);
            }
            log.last = p;
        }, () -> b.getJumpsCompleted() > 0 && b.getActivity() == Activity.GROUND, () -> {
            helper.assertTrue(log.activities.contains(Activity.CLIMB_UP), "he climbed the trunk: " + log.activities);
            helper.assertTrue(log.activities.contains(Activity.JUMP_PREPARATION), "he prepared the jump: " + log.activities + " [" + describe(b) + "]");
            helper.assertTrue(log.activities.contains(Activity.AIRBORNE), "he flew between the trees: " + log.activities);
            helper.assertTrue(log.climbTicks >= 30, "the climb took real time (" + log.climbTicks + " ticks)");
            helper.assertTrue(log.maxClimbY >= helper.absoluteVec(new Vec3(0, perchY - 1.5D, 0)).y, "he reached the canopy height");
            helper.assertTrue(log.maxStep < 1.45D, "he never teleports: largest step while climbing or flying was " + log.maxStep);
            helper.assertTrue(b.getJumpsCompleted() == 1, "one completed jump (" + b.getJumpsCompleted() + ") " + log.timeline + " [" + describe(b) + "]");
            helper.assertTrue(b.getRoutesAborted() == 0, "the route was not aborted (" + b.getRoutesAborted() + ") [" + describe(b) + "]");
            helper.assertTrue(log.landedDistance < 3.5D, "he landed on the target canopy (distance " + log.landedDistance + ")");
            helper.assertTrue(log.landedY > helper.absoluteVec(new Vec3(0, Y + 9, 0)).y, "and on top of it, not on the ground (y=" + log.landedY + ")");
            helper.assertTrue(b.getHealth() == b.getMaxHealth(), "no suffocation or fall damage");
            b.discard();
            pig.discard();
            helper.succeed();
        });
    }

    private static void dropAttack(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CanopyBreaker b = twoTrees(helper);
        LivingEntity pig = dummy(helper, 13, Y + 1, 19);
        b.setTarget(pig);
        helper.assertTrue(b.startRoute(level, pig, false), "a drop route is planned: " + b.getRouteFailure());
        helper.assertTrue(b.getRoute().drop, "it is a Canopy Drop");
        int[] landedAt = {-1};
        float[] before = {100.0F};
        String[] geometry = {"no landing"};
        List<Float> after = new ArrayList<>();
        PathLog dropLog = new PathLog();
        waitUntil(helper, 520, t -> {
            dropLog.note(b.getActivity(), t);
            if (landedAt[0] < 0 && b.getDropsLanded() > 0) {
                landedAt[0] = t;
                geometry[0] = "boss " + b.position() + " pig " + pig.position() + " dist " + b.distanceTo(pig);
            }
            if (landedAt[0] < 0) {
                before[0] = pig.getHealth();
            } else {
                after.add(pig.getHealth());
            }
        }, () -> landedAt[0] >= 0 && after.size() >= 8, () -> {
            helper.assertTrue(b.getDropsLanded() == 1, "the drop landed exactly once (" + b.getDropsLanded() + ") " + dropLog.timeline + " [" + describe(b) + "]");
            helper.assertTrue(landedAt[0] > 40, "after a real climb (landed at tick " + landedAt[0] + ")");
            helper.assertTrue(before[0] == 100.0F, "no damage before the impact");
            helper.assertTrue(after.getLast() <= 100.0F - CanopyBreaker.DROP_DAMAGE + 0.01F,
                    "the impact deals the drop damage (" + after + ", " + geometry[0] + ")");
            int hits = 0;
            for (int i = 1; i < after.size(); i++) {
                if (after.get(i) < after.get(i - 1) - 0.01F) {
                    hits++;
                }
            }
            helper.assertTrue(hits <= 1, "the impact damages the target once, not repeatedly (" + after + ")");
            b.discard();
            pig.discard();
            helper.succeed();
        });
    }

    private static void invalidJumpCancelled(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CanopyBreaker b = twoTrees(helper);
        LivingEntity pig = dummy(helper, 17, Y + 11, 12);
        b.setTarget(pig);
        helper.assertTrue(b.startRoute(level, pig, true), "route planned: " + b.getRouteFailure());
        boolean[] walled = {false};
        waitUntil(helper, 560, t -> {
            if (!walled[0] && b.getActivity() == Activity.JUMP_PREPARATION) {
                walled[0] = true;
                for (int x = 10; x <= 15; x++) { // a wall between the canopies, too tall to jump over
                    for (int y = Y + 5; y <= Y + 40; y++) {
                        for (int z = 6; z <= 18; z++) {
                            helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                        }
                    }
                }
            }
        }, () -> walled[0] && b.getRoutesAborted() >= 1 && b.getRoute() == null && b.getActivity() == Activity.GROUND, () -> {
            helper.assertTrue(walled[0], "he reached the jump preparation [" + describe(b) + "]");
            helper.assertTrue(b.getJumpsCompleted() == 0, "he never jumped into the wall (" + b.getJumpsCompleted() + ")");
            helper.assertTrue(b.getRoutesAborted() >= 1, "the route was cancelled [" + describe(b) + "]");
            helper.assertTrue(!b.isClimbing(), "he is no longer clinging to anything [" + describe(b) + "]");
            helper.assertTrue(b.getY() < helper.absoluteVec(new Vec3(0, Y + 3.5D, 0)).y, "he came back down the trunk to the ground (y=" + b.getY() + ")");
            helper.assertTrue(b.getHealth() == b.getMaxHealth(), "no damage from walls");
            b.discard();
            pig.discard();
            helper.succeed();
        });
    }

    private static void lostGripRecovers(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CanopyBreaker b = twoTrees(helper);
        LivingEntity pig = dummy(helper, 17, Y + 11, 12);
        b.setTarget(pig);
        helper.assertTrue(b.startRoute(level, pig, true), "route planned: " + b.getRouteFailure());
        boolean[] cut = {false};
        boolean[] fell = {false};
        waitUntil(helper, 420, t -> {
            if (cut[0] && b.getActivity() == Activity.FALL_RECOVERY) {
                fell[0] = true;
            }
            if (!cut[0] && b.getActivity() == Activity.CLIMB_UP && b.getY() > helper.absoluteVec(new Vec3(0, Y + 4, 0)).y) {
                cut[0] = true;
                for (int y = Y + 2; y <= Y + 10; y++) { // the trunk breaks above his hands
                    helper.setBlock(new BlockPos(8, y, 12), Blocks.AIR);
                }
            }
        }, () -> fell[0] && b.getActivity() == Activity.GROUND, () -> {
            helper.assertTrue(cut[0], "the trunk was cut while he climbed [" + describe(b) + "]");
            helper.assertTrue(fell[0], "he lost his grip and fell [" + describe(b) + "]");
            helper.assertTrue(b.getRoutesAborted() >= 1, "the failed grip is recorded");
            helper.assertTrue(b.getJumpsCompleted() == 0, "no jump after losing the trunk");
            helper.assertTrue(b.getY() < helper.absoluteVec(new Vec3(0, Y + 3.5D, 0)).y, "he is on the ground, not stuck in the air (y=" + b.getY() + ")");
            helper.assertTrue(!b.isClimbing(), "physics and standing size are restored");
            helper.assertTrue(b.getHealth() == b.getMaxHealth(), "he was never embedded in the trunk");
            b.discard();
            pig.discard();
            helper.succeed();
        });
    }

    private static void noTreesFallsBack(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 0, 0, 40, 40);
        CanopyBreaker b = boss(helper, 6, 6, 0.0F);
        LivingEntity pig = dummy(helper, 20, Y + 1, 6);
        b.setTarget(pig);
        helper.assertTrue(!b.startRoute(level, pig, true), "no route without trees");
        helper.assertTrue(!b.startRoute(level, pig, false), "no drop without trees");
        helper.assertValueEqual(b.getActivity(), Activity.GROUND, "he stays on the ground");
        double start = b.distanceTo(pig);
        waitUntil(helper, 240, t -> { }, () -> b.distanceTo(pig) < start - 5.0D || pig.getHealth() < 100.0F, () -> {
            helper.assertTrue(b.distanceTo(pig) < start - 5.0D || pig.getHealth() < 100.0F, "he chases on the ground instead ("
                    + b.getActivity() + ", " + b.getAnim() + ", distance " + b.distanceTo(pig) + ", target " + b.getTarget() + ")");
            b.discard();
            pig.discard();
            helper.succeed();
        });
    }

    private static void stagesTransitionOnce(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 0, 0, 30, 30);
        CanopyBreaker b = boss(helper, 10, 10, 0.0F);
        helper.assertValueEqual(b.getStage(), 1, "stage 1");
        b.setHealth(121.0F);
        b.hurtServer(level, level.damageSources().generic(), 5.0F); // ~117/200 = 58.5% -> stage 2
        helper.assertValueEqual(b.getActivity(), Activity.TRANSITION, "dropping to 60% starts the transition");
        helper.assertValueEqual(b.getAnim(), CanopyAnim.PHASE_TWO_TRANSITION, "with its animation");
        helper.assertTrue(!b.hurtServer(level, level.damageSources().generic(), 5.0F), "invulnerable while transforming");
        helper.runAfterDelay(CanopyBreaker.TRANSITION_TWO_TICKS + 4, () -> {
            helper.assertValueEqual(b.getStage(), 2, "stage 2");
            helper.assertValueEqual(b.getTransitionCount(), 1, "one transition so far");
            b.hurtServer(level, level.damageSources().generic(), 1.0F);
            helper.assertValueEqual(b.getTransitionCount(), 1, "further damage in stage 2 does not retrigger");
            b.setHealth(51.0F);
            b.invulnerableTime = 0;
            b.hurtServer(level, level.damageSources().generic(), 5.0F); // ~47/200 = 23.5% -> stage 3
            helper.assertValueEqual(b.getAnim(), CanopyAnim.FURY_TRANSITION, "the fury transition");
        });
        helper.runAfterDelay(CanopyBreaker.TRANSITION_TWO_TICKS + 4 + CanopyBreaker.TRANSITION_THREE_TICKS + 6, () -> {
            helper.assertValueEqual(b.getStage(), 3, "stage 3");
            helper.assertValueEqual(b.getTransitionCount(), 2, "exactly two transitions");
            helper.assertTrue(b.getAttributeValue(Attributes.MOVEMENT_SPEED) > 0.33D * 1.1D, "the fury is faster");
            b.setHealth(b.getMaxHealth());
            b.hurtServer(level, level.damageSources().generic(), 1.0F);
            helper.assertValueEqual(b.getTransitionCount(), 2, "healing never repeats a transition");
            helper.assertValueEqual(b.getStage(), 3, "the stage never goes back");
            b.discard();
            helper.succeed();
        });
    }

    private static void staggerAndOverlay(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 0, 0, 30, 30);
        CanopyBreaker b = boss(helper, 10, 10, 0.0F);
        LivingEntity pig = dummy(helper, 10, Y + 1, 12);
        b.setTarget(pig);
        helper.assertTrue(b.startAttack(Attack.BACKHAND, pig), "backhand starts");
        helper.runAfterDelay(CanopyBreaker.BACKHAND_PREP + CanopyBreaker.BACKHAND_EXEC + 2, () -> {
            helper.assertValueEqual(b.getPhase(), CanopyBreaker.Phase.RECOVER, "in recovery");
            b.hurtServer(level, level.damageSources().mobAttack(pig), 20.0F);
            helper.assertTrue(b.getOverlay() != null && b.getOverlay().name().startsWith("HURT_"), "a hurt overlay shows");
            helper.assertValueEqual(b.getActivity(), Activity.STAGGER, "a heavy hit during recovery staggers him");
            helper.assertValueEqual(b.getAnim(), CanopyAnim.STAGGER, "stagger animation");
        });
        helper.runAfterDelay(CanopyBreaker.BACKHAND_PREP + CanopyBreaker.BACKHAND_EXEC + 14, () ->
                helper.assertTrue(b.getOverlay() == null, "the overlay clears"));
        helper.runAfterDelay(CanopyBreaker.BACKHAND_PREP + CanopyBreaker.BACKHAND_EXEC + 2 + CanopyBreaker.STAGGER_TICKS + 24, () -> {
            helper.assertTrue(b.getActivity() != Activity.STAGGER, "the stagger does not last forever");
            b.discard();
            pig.discard();
            helper.succeed();
        });
    }

    private static void deathLootAndContent(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 0, 0, 30, 30);
        BlockPos abs = helper.absolutePos(new BlockPos(10, Y + 1, 10));
        CanopyBreaker b = boss(helper, 10, 10, 0.0F);
        b.hurtServer(level, level.damageSources().generic(), 500.0F);
        helper.assertValueEqual(b.getActivity(), Activity.DYING, "dying");
        helper.assertValueEqual(b.getAnim(), CanopyAnim.ROAR_DEATH, "death roar first");
        helper.assertValueEqual(b.getDeathAwardCount(), 1, "rewards once");
        helper.assertTrue(!b.hurtServer(level, level.damageSources().generic(), 5.0F), "no more damage while dying");
        List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(abs).inflate(8.0D));
        int resin = drops.stream().filter(e -> e.getItem().is(ModItems.CANOPY_RESIN.get())).mapToInt(e -> e.getItem().getCount()).sum();
        helper.assertTrue(resin >= 2 && resin <= 4, "2 to 4 Canopy Resin drop (" + resin + ")");
        Set<CanopyAnim> seen = EnumSet.noneOf(CanopyAnim.class);
        sampleEachTick(helper, 1, 98, t -> seen.add(b.getAnim()), () -> {
            helper.assertTrue(seen.contains(CanopyAnim.DEATH_PREPARE) && seen.contains(CanopyAnim.DEATH_FALL)
                    && seen.contains(CanopyAnim.DEATH_IMPACT) && seen.contains(CanopyAnim.DEATH_FINAL), "death plays its four beats: " + seen);
            helper.assertTrue(b.isRemoved(), "removed after the death sequence");
            var spirits = level.getEntitiesOfClass(com.codenamecoraline.entity.spirit.BossSpirit.class, new AABB(abs).inflate(10.0D),
                    s -> s.getType() == ModEntities.CANOPY_BREAKER_SPIRIT.get());
            helper.assertValueEqual(spirits.size(), 1, "his spirit rises from the body, once");
            helper.assertTrue(spirits.getFirst().getY() > abs.getY() + 1.0D, "and it really rises (y=" + spirits.getFirst().getY() + ")");
            helper.assertTrue(!spirits.getFirst().isPickable(), "the spirit is only a vision: it cannot be hit");
            helper.assertValueEqual(b.getDeathAwardCount(), 1, "still rewarded once");
            drops.forEach(e -> e.discard());
            helper.assertTrue(Bestiary.ENTRIES.contains(Bestiary.CANOPY_BREAKER), "Bestiary has the entry");
            helper.assertValueEqual(Bestiary.CANOPY_BREAKER.pages().size(), 6, "six pages");
            for (String adv : new String[] {"canopy_idol", "canopy_summon", "canopy_defeat", "canopy_club"}) {
                helper.assertTrue(level.getServer().getAdvancements().get(id("bosses/" + adv)) != null, "advancement bosses/" + adv);
            }
            for (String art : new String[] {"canopy_breaker_anatomy", "canopy_breaker_combat", "canopy_breaker_spoils"}) {
                helper.assertTrue(CanopyBreakerGameTests.class.getResourceAsStream(
                        "/assets/codenamecoraline/textures/gui/bestiary/" + art + ".png") != null, "engraving " + art);
            }
            for (String res : new String[] {"textures/gui/boss_bar/canopy_breaker.png", "textures/entity/canopy_breaker.png",
                    "textures/entity/canopy_breaker_eyes.png", "textures/item/deadroot_club.png", "textures/item/hollow_trunk_idol.png",
                    "textures/item/canopy_resin.png", "textures/item/heartwood_splinter.png", "textures/item/canopy_breaker_portrait.png"}) {
                helper.assertTrue(CanopyBreakerGameTests.class.getResourceAsStream("/assets/codenamecoraline/" + res) != null, "asset " + res);
            }
            helper.succeed();
        });
    }
}
