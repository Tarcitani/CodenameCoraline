package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.bestiary.Bestiary;
import com.codenamecoraline.bestiary.BestiaryPage;
import com.codenamecoraline.client.crimson.CrimsonCommanderAnimations;
import com.codenamecoraline.client.crimson.CrimsonCommanderModel;
import com.codenamecoraline.client.crimson.CrimsonCommanderRenderState;
import com.codenamecoraline.entity.crimson.CrimsonAnim;
import com.codenamecoraline.entity.crimson.CrimsonAshVanguard;
import com.codenamecoraline.entity.crimson.CrimsonCommander;
import com.codenamecoraline.entity.crimson.CrimsonCommander.Activity;
import com.codenamecoraline.entity.crimson.CrimsonCommander.Attack;
import com.codenamecoraline.entity.crimson.CrimsonCommander.Phase;
import com.codenamecoraline.item.CrimsonOathGreatswordItem;
import com.codenamecoraline.item.StainedOathCrestItem;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Integration tests of the Crimson Commander: registration, attributes and immunities, the animation catalogue and its
 * reference poses, UV layout, the crest ritual, the arrival, the chase, every attack (damage to whom it should, none to
 * whom it should not, recovery), "never swing at the air", the guard (reduction, deflection, riposte, trigger), the wave
 * against walls and unreachable targets, the stages (once each), the fury combo, the vanguards (cap, alliance, expiry,
 * fall with their master), death and its single reward, content (Bestiary, advancements, recipe, lang), targeting,
 * getting unstuck, the natural spawn rules and the greatsword's dash.
 *
 * <p>Tests build their own plots high above the grid (stone floor) and only ever touch their own entities, since
 * arenas run side by side 19 blocks apart: every plot, mob and blow stays inside relative x and z 0 to 18 (or on a far
 * plot of its own), and every Commander gets a target of his own close by, so that neither he nor his vanguards wander
 * off after a neighbour's golem.</p>
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class CrimsonCommanderGameTests {
    /** Relative height of every plot's floor. */
    private static final int Y = 100;

    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
            Map.entry("crimson_registration_and_immunities", CrimsonCommanderGameTests::registrationAndImmunities),
            Map.entry("crimson_animation_catalogue_and_uv", CrimsonCommanderGameTests::animationCatalogueAndUv),
            Map.entry("crimson_reference_poses_hold_the_sword_right", CrimsonCommanderGameTests::referencePoses),
            Map.entry("crimson_crest_conditions", CrimsonCommanderGameTests::crestConditions),
            Map.entry("crimson_intro_then_duel", CrimsonCommanderGameTests::introThenDuel),
            Map.entry("crimson_chases_far_target", CrimsonCommanderGameTests::chasesFarTarget),
            Map.entry("crimson_slash_hits_front_spares_back", CrimsonCommanderGameTests::slashFrontAndBack),
            Map.entry("crimson_never_swings_at_air", CrimsonCommanderGameTests::neverSwingsAtAir),
            Map.entry("crimson_dash_thrust_hits_and_slows", CrimsonCommanderGameTests::dashThrustHitsAndSlows),
            Map.entry("crimson_dash_thrust_miss_recovers_longer", CrimsonCommanderGameTests::dashThrustMiss),
            Map.entry("crimson_slam_two_rings_and_launch", CrimsonCommanderGameTests::slamTwoRings),
            Map.entry("crimson_slam_disables_shields", CrimsonCommanderGameTests::slamDisablesShields),
            Map.entry("crimson_slam_reaches_a_pillar", CrimsonCommanderGameTests::slamReachesPillar),
            Map.entry("crimson_guard_reduces_frontal_damage", CrimsonCommanderGameTests::guardReducesFrontal),
            Map.entry("crimson_guard_deflects_arrows", CrimsonCommanderGameTests::guardDeflectsArrows),
            Map.entry("crimson_guard_riposte", CrimsonCommanderGameTests::guardRiposte),
            Map.entry("crimson_guard_after_three_hits", CrimsonCommanderGameTests::guardAfterThreeHits),
            Map.entry("crimson_guard_turns_slowly", CrimsonCommanderGameTests::guardTurnsSlowly),
            Map.entry("crimson_wave_travels_hits_and_stops_at_walls", CrimsonCommanderGameTests::waveTravels),
            Map.entry("crimson_wave_against_unreachable_target", CrimsonCommanderGameTests::waveAgainstUnreachable),
            Map.entry("crimson_stages_transition_once", CrimsonCommanderGameTests::stagesTransitionOnce),
            Map.entry("crimson_fury_combo_dash_into_slam", CrimsonCommanderGameTests::furyCombo),
            Map.entry("crimson_vanguards_cap_and_alliance", CrimsonCommanderGameTests::vanguardsCapAndAlliance),
            Map.entry("crimson_vanguards_expire_and_fall_with_master", CrimsonCommanderGameTests::vanguardsExpire),
            Map.entry("crimson_death_rewards_once", CrimsonCommanderGameTests::deathRewardsOnce),
            Map.entry("crimson_content_bestiary_advancements_lang", CrimsonCommanderGameTests::contentLoaded),
            Map.entry("crimson_target_priority_and_hysteresis", CrimsonCommanderGameTests::targetPriority),
            Map.entry("crimson_frees_himself_from_blocks", CrimsonCommanderGameTests::freesHimself),
            Map.entry("crimson_leaps_out_of_a_dead_end", CrimsonCommanderGameTests::leapsOutOfDeadEnd),
            Map.entry("crimson_natural_spawn_rules", CrimsonCommanderGameTests::naturalSpawnRules),
            Map.entry("crimson_greatsword_dash", CrimsonCommanderGameTests::greatswordDash),
            Map.entry("crimson_heals_while_vanguards_fight", CrimsonCommanderGameTests::healsWhileVanguardsFight),
            Map.entry("crimson_alliances_and_passive_animals", CrimsonCommanderGameTests::alliances)
    );

    private CrimsonCommanderGameTests() {}

    private static boolean enabled() {
        return Boolean.getBoolean("coraline.crimsonTests");
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, name);
    }

    @SubscribeEvent
    public static void functions(RegisterEvent event) {
        if (!enabled()) return;
        event.register(Registries.TEST_FUNCTION, registry -> TESTS.forEach((name, fn) -> registry.register(id(name),
                (Consumer<GameTestHelper>) helper -> {
                    pump(helper);
                    fn.accept(helper);
                })));
    }

    @SubscribeEvent
    public static void tests(RegisterGameTestsEvent event) {
        if (!enabled()) return;
        var environment = event.registerEnvironment(id("crimson_test_environment"));
        TESTS.forEach((name, fn) -> event.registerTest(id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                        new TestData<>(environment, id("basilisk_test_arena"), 700, 0, true,
                                Rotation.NONE, false, 1, 1, true, 0))));
    }

    // ------------------------------------------------------------------------------------------------ plot helpers
    /** Stone floor at relative height {@link #Y}; every chunk it touches is force-loaded so mobs tick there. */
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
                h.setBlock(new BlockPos(x, Y, z), Blocks.STONE);
            }
        }
    }

    /**
     * Builds the plot's floor and runs {@code body} once every chunk of the plot ticks entities: before that, entities
     * added there neither tick nor show up in the level's entity searches (the cause of intermittent failures).
     */
    private static void plot(GameTestHelper h, int x0, int z0, int x1, int z1, Runnable body) {
        floor(h, x0, z0, x1, z1);
        ServerLevel level = h.getLevel();
        BlockPos a = h.absolutePos(new BlockPos(x0, Y, z0));
        BlockPos b = h.absolutePos(new BlockPos(x1, Y, z1));
        java.util.function.BooleanSupplier ticking = () -> {
            for (int cx = Math.min(a.getX(), b.getX()) >> 4; cx <= Math.max(a.getX(), b.getX()) >> 4; cx++) {
                for (int cz = Math.min(a.getZ(), b.getZ()) >> 4; cz <= Math.max(a.getZ(), b.getZ()) >> 4; cz++) {
                    if (!level.isPositionEntityTicking(new BlockPos(cx << 4, a.getY(), cz << 4))) {
                        return false;
                    }
                }
            }
            return true;
        };
        if (ticking.getAsBoolean()) {
            body.run();
            return;
        }
        waitUntil(h, 300, t -> { }, ticking, () -> {
            h.assertTrue(ticking.getAsBoolean(), "the plot ticks entities");
            body.run();
        });
    }

    private static CrimsonCommander commander(GameTestHelper h, int x, int z, float yaw) {
        CrimsonCommander c = h.spawn(ModEntities.CRIMSON_COMMANDER.get(), new BlockPos(x, Y + 1, z));
        c.setPos(h.absoluteVec(new Vec3(x + 0.5D, Y + 1, z + 0.5D)));
        c.setYRot(yaw);
        c.setYHeadRot(yaw);
        c.yBodyRot = yaw;
        c.setOnGround(true);
        return c;
    }

    /** A still iron golem (a valid target for him) with plenty of health, so damage is read exactly (golems have no armour). */
    private static IronGolem golem(GameTestHelper h, double x, double y, double z) {
        IronGolem g = h.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos((int) x, (int) y, (int) z));
        g.setPos(h.absoluteVec(new Vec3(x + 0.5D, y, z + 0.5D)));
        g.getAttribute(Attributes.MAX_HEALTH).setBaseValue(300.0D);
        g.setHealth(300.0F);
        return g;
    }

    /** A wait registered on a test's pump: sampled every tick until it stops, then {@code done} runs (and asserts). */
    private static final class Waiter {
        final int max;
        final Consumer<Integer> each;
        final java.util.function.BooleanSupplier stop;
        final Runnable done;
        int tick;

        Waiter(int max, Consumer<Integer> each, java.util.function.BooleanSupplier stop, Runnable done) {
            this.max = max;
            this.each = each;
            this.stop = stop;
            this.done = done;
        }
    }

    private static final Map<GameTestHelper, List<Waiter>> WAITERS = new java.util.IdentityHashMap<>();
    /** Ticks the pump keeps running: the test's own timeout. */
    private static final int PUMP_TICKS = 700;

    /**
     * Arms the test's pump before anything else: one scheduled runnable per tick, registered up front, that drives every
     * wait of the test. Waits created later (inside other waits) only join the pump's list, so the helper's own schedule
     * is never modified while the game iterates it (doing that crashes the GameTest server).
     */
    private static void pump(GameTestHelper h) {
        List<Waiter> list = new ArrayList<>();
        WAITERS.put(h, list);
        for (int t = 1; t <= PUMP_TICKS; t++) {
            h.runAfterDelay(t, () -> {
                for (Waiter w : new ArrayList<>(list)) {
                    if (!list.contains(w)) {
                        continue;
                    }
                    w.tick++;
                    w.each.accept(w.tick);
                    if (w.stop.getAsBoolean() || w.tick >= w.max) {
                        list.remove(w);
                        w.done.run();
                    }
                }
            });
        }
    }

    /** Samples every tick until {@code stop} holds or {@code max} ticks pass, then runs {@code done} (which asserts). */
    private static void waitUntil(GameTestHelper h, int max, Consumer<Integer> each, java.util.function.BooleanSupplier stop, Runnable done) {
        WAITERS.get(h).add(new Waiter(max, each, stop, done));
    }

    /** Runs {@code task} {@code ticks} ticks from now. */
    private static void later(GameTestHelper h, int ticks, Runnable task) {
        waitUntil(h, ticks, t -> { }, () -> false, task);
    }

    private static float lost(LivingEntity e) {
        return e.getMaxHealth() - e.getHealth();
    }

    private static void lockOffense(CrimsonCommander c) {
        for (Attack a : Attack.values()) {
            c.setCooldown(a, 100000);
        }
    }

    // ------------------------------------------------------------------------------------------------ registration
    private static void registrationAndImmunities(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 16, 16, () -> {
            CrimsonCommander c = commander(helper, 8, 8, 0.0F);
            helper.assertValueEqual(c.getMaxHealth(), 220.0F, "220 HP");
            helper.assertValueEqual(c.getAttributeValue(Attributes.ARMOR), 14.0D, "armor 14");
            helper.assertValueEqual(c.getAttributeValue(Attributes.ARMOR_TOUGHNESS), 6.0D, "armor toughness 6");
            helper.assertValueEqual(c.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.34D, "speed 0.34");
            helper.assertValueEqual(c.getAttributeValue(Attributes.ATTACK_DAMAGE), 12.0D, "base damage 12");
            helper.assertValueEqual(c.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.75D, "knockback resistance 0.75");
            helper.assertValueEqual(c.getAttributeValue(Attributes.FOLLOW_RANGE), 40.0D, "sight 40");
            helper.assertValueEqual(c.getBbWidth(), 1.1F, "1.1 wide");
            helper.assertValueEqual(c.getBbHeight(), 3.5F, "3.5 tall");
            helper.assertTrue(Math.abs(c.getEyeHeight() - 3.05F) < 1.0E-4F, "eyes at 3.05");
            helper.assertValueEqual(CrimsonCommander.XP_REWARD, 250, "250 XP");
            helper.assertTrue(c.getType().fireImmune(), "immune to fire and lava");
            helper.assertTrue(!c.canFreeze(), "never freezes");
            helper.assertTrue(c.getType().builtInRegistryHolder().is(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES), "in freeze_immune_entity_types");
            helper.assertTrue(c.getType().builtInRegistryHolder().is(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS), "walks on powder snow");
            helper.assertTrue(c.canBreatheUnderwater(), "cannot drown");
            helper.assertTrue(!c.removeWhenFarAway(1000.0D), "never despawns by distance");
            helper.assertValueEqual(c.getActivity(), Activity.GROUND, "starts on the ground");
            helper.assertValueEqual(c.getStage(), 1, "stage 1");
            c.causeFallDamage(20.0D, 1.0F, level.damageSources().fall());
            float fall = lost(c);
            helper.assertTrue(fall > 0.0F && fall <= 5.0F, "a 20-block fall costs only a fifth (" + fall + " instead of 17)");
            // items, eggs and the greatsword's 10 attack damage
            helper.assertTrue(ModItems.CRIMSON_PLATE_SHARD.get() != null && ModItems.STAINED_OATH_CREST.get() != null
                    && ModItems.CRIMSON_OATH_GREATSWORD.get() != null && ModItems.CRIMSON_COMMANDER_PORTRAIT.get() != null, "items registered");
            ItemStack egg = new ItemStack(ModItems.CRIMSON_COMMANDER_SPAWN_EGG.get());
            helper.assertValueEqual(((SpawnEggItem) egg.getItem()).getType(egg), ModEntities.CRIMSON_COMMANDER.get(), "egg spawns the Commander");
            ItemStack vegg = new ItemStack(ModItems.CRIMSON_ASH_VANGUARD_SPAWN_EGG.get());
            helper.assertValueEqual(((SpawnEggItem) vegg.getItem()).getType(vegg), ModEntities.CRIMSON_ASH_VANGUARD.get(), "egg spawns a Vanguard");
            ItemStack sword = new ItemStack(ModItems.CRIMSON_OATH_GREATSWORD.get());
            double damage = sword.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                    net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).compute(Attributes.ATTACK_DAMAGE, 1.0D, EquipmentSlot.MAINHAND);
            helper.assertValueEqual(damage, 10.0D, "the greatsword deals 10");
            c.discard();
            helper.succeed();
        });
    }

    // ------------------------------------------------------------------------------------------------ animations
    private static void animationCatalogueAndUv(GameTestHelper helper) {
        Set<String> ids = new HashSet<>();
        var root = CrimsonCommanderModel.createBodyLayer().bakeRoot();
        for (CrimsonAnim a : CrimsonAnim.values()) {
            helper.assertTrue(CrimsonCommanderAnimations.get(a) != null, a + " has keyframes");
            helper.assertTrue(CrimsonCommanderAnimations.get(a).lengthInSeconds() > 0.0F, a + " has a duration");
            helper.assertTrue(ids.add(a.id()), a + " id is unique");
            helper.assertTrue(CrimsonCommanderAnimations.get(a).bake(root) != null, a + " bakes");
        }
        // the attack animations last as long as their gameplay phases
        helper.assertTrue(Math.abs(CrimsonCommanderAnimations.get(CrimsonAnim.SLASH_PREPARE).lengthInSeconds() - CrimsonCommander.SLASH_PREP / 20.0F) < 1.0E-3F, "slash wind-up length");
        helper.assertTrue(Math.abs(CrimsonCommanderAnimations.get(CrimsonAnim.THRUST_PREPARE).lengthInSeconds() - CrimsonCommander.THRUST_PREP / 20.0F) < 1.0E-3F, "thrust wind-up length");
        helper.assertTrue(Math.abs(CrimsonCommanderAnimations.get(CrimsonAnim.SLAM_PREPARE).lengthInSeconds() - CrimsonCommander.SLAM_PREP / 20.0F) < 1.0E-3F, "slam wind-up length");
        helper.assertTrue(Math.abs(CrimsonCommanderAnimations.get(CrimsonAnim.WAVE_PREPARE).lengthInSeconds() - CrimsonCommander.WAVE_PREP / 20.0F) < 1.0E-3F, "wave wind-up length");
        helper.assertTrue(Math.abs(CrimsonCommanderAnimations.get(CrimsonAnim.AWAKEN_TRANSITION).lengthInSeconds() - CrimsonCommander.AWAKEN_TICKS / 20.0F) < 1.0E-3F, "awakening length");
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

    /** Model-space bounds (in blocks; y grows downwards, ground at 1.5, front is -z) of the greatsword in a pose. */
    private static float[] swordBounds(CrimsonAnim anim, float seconds) {
        var model = new CrimsonCommanderModel(CrimsonCommanderModel.createBodyLayer().bakeRoot());
        var state = new CrimsonCommanderRenderState();
        state.anim = anim;
        state.lookAtTarget = false;
        state.ageInTicks = 100.0F + seconds * 20.0F;
        state.animState.start(100);
        model.setupAnim(state);
        float[] b = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        model.root().visit(new PoseStack(), (pose, part, index, cube) -> {
            if (!part.endsWith("sword")) {
                return;
            }
            for (var polygon : cube.polygons) {
                for (var vertex : polygon.vertices()) {
                    var v = pose.pose().transformPosition(vertex.worldX(), vertex.worldY(), vertex.worldZ(), new org.joml.Vector3f());
                    b[0] = Math.min(b[0], v.x());
                    b[1] = Math.min(b[1], v.y());
                    b[2] = Math.min(b[2], v.z());
                    b[3] = Math.max(b[3], v.x());
                    b[4] = Math.max(b[4], v.y());
                    b[5] = Math.max(b[5], v.z());
                }
            }
        });
        return b;
    }

    private static void referencePoses(GameTestHelper helper) {
        // Combat Idle Stance: the blade rests on the right shoulder, rising above the pauldron and reaching behind him
        float[] idle = swordBounds(CrimsonAnim.COMBAT_IDLE, 0.0F);
        helper.assertTrue(idle[1] < -1.6F, "the blade rises above the shoulders (top y " + idle[1] + ")");
        helper.assertTrue(idle[5] > 0.2F, "and reaches behind his back (z " + idle[5] + ")");
        helper.assertTrue(idle[3] < 0.0F, "on his right side (max x " + idle[3] + ")");
        // Dash Thrust: the blade points straight ahead, level, far in front of him
        float[] thrust = swordBounds(CrimsonAnim.THRUST_EXECUTE, 0.4F);
        helper.assertTrue(thrust[2] < -2.6F, "the thrust reaches far in front (z " + thrust[2] + ")");
        helper.assertTrue(thrust[4] - thrust[1] < 1.5F, "and the blade is close to level (a broad blade; vertical span " + (thrust[4] - thrust[1]) + ")");
        // Heavy Overhead Slam: the wind-up hangs the blade behind the head, the impact drives it into the floor in front
        float[] wind = swordBounds(CrimsonAnim.SLAM_PREPARE, 1.1F);
        helper.assertTrue(wind[1] < -2.0F && wind[5] > 0.5F, "the wind-up holds the blade high and behind (top " + wind[1] + ", back " + wind[5] + ")");
        float[] impact = swordBounds(CrimsonAnim.SLAM_EXECUTE, 0.5F);
        helper.assertTrue(impact[4] > 1.3F && impact[2] < -0.8F, "the impact drives the point to the floor in front (low " + impact[4] + ", front " + impact[2] + ")");
        // Defensive Guard: the blade crosses the torso diagonally, point up
        float[] guard = swordBounds(CrimsonAnim.GUARD_HOLD, 0.0F);
        helper.assertTrue(guard[0] < -0.3F && guard[3] > 0.3F, "the guard crosses the torso from his right to his left (x " + guard[0] + ".." + guard[3] + ")");
        helper.assertTrue(guard[1] < -1.5F && guard[2] < -0.3F, "point up, in front of him (top " + guard[1] + ", front " + guard[2] + ")");
        helper.succeed();
    }

    // ------------------------------------------------------------------------------------------------ ritual and arrival
    private static void crestConditions(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        final int ox = 3200; // far from every other test: their Commanders would count as "already active"
        floor(helper, ox, 0, ox + 30, 20);
        BlockPos stone = helper.absolutePos(new BlockPos(ox + 4, Y, 10));
        BlockPos altar = helper.absolutePos(new BlockPos(ox + 10, Y, 10));
        BlockPos second = helper.absolutePos(new BlockPos(ox + 22, Y, 10));
        level.setBlock(altar, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
        level.setBlock(second, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3);
        java.util.function.BooleanSupplier plotTicks = () -> {
            for (int cx = -1; cx <= 1; cx++) {
                for (int cz = -1; cz <= 1; cz++) {
                    if (!level.isPositionEntityTicking(altar.offset(cx * 16, 0, cz * 16))) {
                        return false;
                    }
                }
            }
            return true;
        };
        for (int cx = -1; cx <= 1; cx++) {
            for (int cz = -1; cz <= 1; cz++) {
                level.setChunkForced((altar.getX() >> 4) + cx, (altar.getZ() >> 4) + cz, true);
            }
        }
        waitUntil(helper, 400, t -> { }, plotTicks, () -> {
            helper.assertTrue(plotTicks.getAsBoolean(), "the far plot ticks entities");
            crestSequence(helper, level, stone, altar, second);
        });
    }

    private static void crestSequence(GameTestHelper helper, ServerLevel level, BlockPos stone, BlockPos altar, BlockPos second) {
        ItemStack crest = new ItemStack(ModItems.STAINED_OATH_CREST.get(), 3);
        helper.assertValueEqual(StainedOathCrestItem.trySummon(level, stone, null, crest), StainedOathCrestItem.SummonResult.WRONG_BLOCK,
                "rejects plain stone");
        BlockPos beam = altar.offset(1, 3, 1);
        level.setBlock(beam, Blocks.STONE.defaultBlockState(), 3);
        helper.assertValueEqual(StainedOathCrestItem.trySummon(level, altar, null, crest), StainedOathCrestItem.SummonResult.NO_SPACE,
                "rejects a cramped hall (a block inside the 5x5x5 above)");
        helper.assertValueEqual(crest.getCount(), 3, "a failed use never consumes the crest");
        level.setBlock(beam, Blocks.AIR.defaultBlockState(), 3);
        helper.assertValueEqual(StainedOathCrestItem.evaluateConditions(level, second), StainedOathCrestItem.SummonResult.SUCCESS,
                "polished deepslate is accepted too");
        helper.assertValueEqual(StainedOathCrestItem.trySummon(level, altar, null, crest), StainedOathCrestItem.SummonResult.SUCCESS, "summons");
        helper.assertValueEqual(crest.getCount(), 2, "success consumes exactly one crest");
        List<CrimsonCommander> found = level.getEntitiesOfClass(CrimsonCommander.class, new AABB(altar).inflate(10.0D));
        helper.assertValueEqual(found.size(), 1, "exactly one Commander");
        CrimsonCommander c = found.getFirst();
        helper.assertValueEqual(c.getActivity(), Activity.INTRO, "he arrives kneeling (intro)");
        helper.assertTrue(c.getY() >= altar.getY() + 1.0D - 1.0E-3D, "standing on the altar");
        helper.assertValueEqual(StainedOathCrestItem.trySummon(level, second, null, crest), StainedOathCrestItem.SummonResult.ALREADY_ACTIVE,
                "a second one within 48 blocks is refused");
        helper.assertValueEqual(crest.getCount(), 2, "and the refusal keeps the crest");
        c.discard();
        helper.succeed();
    }

    private static void introThenDuel(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 16, 16, () -> {
            CrimsonCommander c = commander(helper, 8, 8, 0.0F);
            c.startIntro(level);
            helper.assertValueEqual(c.getActivity(), Activity.INTRO, "intro");
            helper.assertValueEqual(c.getAnim(), CrimsonAnim.INTRO_KNEEL, "kneeling over the planted sword");
            helper.assertTrue(!c.hurtServer(level, level.damageSources().generic(), 10.0F), "untouchable during the audience");
            Set<CrimsonAnim> seen = EnumSet.noneOf(CrimsonAnim.class);
            waitUntil(helper, CrimsonCommander.INTRO_TICKS + 10, t -> seen.add(c.getAnim()), () -> c.getActivity() == Activity.GROUND, () -> {
                helper.assertTrue(seen.contains(CrimsonAnim.INTRO_RISE), "he rises and salutes: " + seen);
                helper.assertValueEqual(c.getActivity(), Activity.GROUND, "then the duel begins");
                helper.assertValueEqual(c.getHealth(), c.getMaxHealth(), "unhurt by the blow during the intro");
                c.discard();
                helper.succeed();
            });
        });
    }

    private static void chasesFarTarget(GameTestHelper helper) {
        plot(helper, 0, 0, 10, 18, () -> {
            CrimsonCommander c = commander(helper, 5, 1, 0.0F);
            IronGolem g = golem(helper, 5, Y + 1, 16);
            c.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(g), 1.0F); // it struck first
            c.setTarget(g);
            double start = c.distanceTo(g);
            double[] closest = {start};
            waitUntil(helper, 160, t -> closest[0] = Math.min(closest[0], c.distanceTo(g)),
                    () -> closest[0] < start - 4.0D || c.getAttack() == Attack.DASH_THRUST, () -> {
                        helper.assertTrue(closest[0] < start - 4.0D || c.getAttack() == Attack.DASH_THRUST,
                                "he closes in on a far target (from " + start + " to " + closest[0] + ")");
                        c.discard();
                        g.discard();
                        helper.succeed();
                    });
        });
    }

    // ------------------------------------------------------------------------------------------------ slash
    private static void slashFrontAndBack(GameTestHelper helper) {
        plot(helper, 0, 0, 16, 16, () -> {
            CrimsonCommander c = commander(helper, 8, 8, 0.0F); // facing +z
            IronGolem front = golem(helper, 8, Y + 1, 10.5D);
            IronGolem back = golem(helper, 8, Y + 1, 4.5D);
            c.setTarget(front);
            helper.assertTrue(c.startAttack(Attack.CRIMSON_SLASH, front), "the slash starts");
            helper.assertValueEqual(c.getAnim(), CrimsonAnim.SLASH_PREPARE, "with its wind-up");
            waitUntil(helper, 40, t -> { }, () -> c.getSlashesLanded() > 0, () -> {
                helper.assertValueEqual(lost(front), CrimsonCommander.SLASH_DAMAGE, "12 to the one in front");
                helper.assertValueEqual(lost(back), 0.0F, "nothing to the one behind him");
                helper.assertValueEqual(c.getAnim(), CrimsonAnim.SLASH_EXECUTE, "during the sweep");
                later(helper, CrimsonCommander.SLASH_EXEC, () -> {
                    helper.assertValueEqual(c.getPhase(), Phase.RECOVER, "then an open recovery");
                    helper.assertValueEqual(c.getAnim(), CrimsonAnim.SLASH_RECOVERY, "with its own animation");
                    c.discard();
                    front.discard();
                    back.discard();
                    helper.succeed();
                });
            });
        });
    }

    private static void neverSwingsAtAir(GameTestHelper helper) {
        plot(helper, 0, 0, 18, 18, () -> {
            CrimsonCommander c = commander(helper, 8, 4, 0.0F);
            IronGolem g = golem(helper, 8, Y + 1, 6.5D);
            c.setTarget(g);
            helper.assertTrue(c.startAttack(Attack.CRIMSON_SLASH, g), "the slash starts");
            later(helper, 4, () -> g.setPos(helper.absoluteVec(new Vec3(16.5D, Y + 1, 16.5D)))); // the target steps away
            waitUntil(helper, 40, t -> { }, () -> c.getAttacksCancelled() > 0, () -> {
                helper.assertValueEqual(c.getAttacksCancelled(), 1, "the blow is called off");
                helper.assertValueEqual(c.getSlashesLanded(), 0, "never swung at the air");
                helper.assertValueEqual(c.getAnim(), CrimsonAnim.ATTACK_CANCEL, "he recoils from the wind-up");
                helper.assertValueEqual(lost(g), 0.0F, "the target is untouched");
                helper.assertTrue(c.getCooldown(Attack.CRIMSON_SLASH) > 0, "with a short cooldown");
                c.discard();
                g.discard();
                helper.succeed();
            });
        });
    }

    // ------------------------------------------------------------------------------------------------ dash thrust
    private static void dashThrustHitsAndSlows(GameTestHelper helper) {
        plot(helper, 0, 0, 16, 18, () -> {
            CrimsonCommander c = commander(helper, 8, 3, 0.0F);
            IronGolem g = golem(helper, 8, Y + 1, 11);
            c.setTarget(g);
            Vec3 start = c.position();
            helper.assertTrue(c.startAttack(Attack.DASH_THRUST, g), "the dash starts");
            helper.assertValueEqual(c.getAnim(), CrimsonAnim.THRUST_PREPARE, "it leans in first");
            waitUntil(helper, 50, t -> { }, () -> c.getThrustHits() > 0, () -> {
                helper.assertValueEqual(lost(g), CrimsonCommander.THRUST_DAMAGE, "15 piercing damage");
                helper.assertTrue(g.hasEffect(MobEffects.SLOWNESS), "and Slowness I");
                helper.assertTrue(c.position().distanceTo(start) > 4.5D, "after a real advance (" + c.position().distanceTo(start) + " blocks)");
                int[] recover = {0};
                waitUntil(helper, 40, t -> {
                    if (c.getAttack() == Attack.DASH_THRUST && c.getPhase() == Phase.RECOVER) {
                        recover[0]++;
                    }
                }, () -> c.getAttack() != Attack.DASH_THRUST, () -> {
                    helper.assertTrue(Math.abs(recover[0] - CrimsonCommander.THRUST_RECOVER) <= 2,
                            "a hit recovers in 18 ticks (" + recover[0] + ")");
                    c.discard();
                    g.discard();
                    helper.succeed();
                });
            });
        });
    }

    private static void dashThrustMiss(GameTestHelper helper) {
        plot(helper, 0, 0, 18, 18, () -> {
            CrimsonCommander c = commander(helper, 8, 3, 0.0F);
            IronGolem g = golem(helper, 8, Y + 1, 11);
            c.setTarget(g);
            helper.assertTrue(c.startAttack(Attack.DASH_THRUST, g), "the dash starts");
            boolean[] moved = {false};
            int[] recover = {0};
            waitUntil(helper, 90, t -> {
                if (!moved[0] && c.getPhase() == Phase.EXECUTE) {
                    moved[0] = true; // the target sidesteps as the dash commits
                    g.setPos(helper.absoluteVec(new Vec3(16.5D, Y + 1, 11.5D)));
                }
                if (c.getAttack() == Attack.DASH_THRUST && c.getPhase() == Phase.RECOVER) {
                    recover[0]++;
                    helper.assertValueEqual(c.getAnim(), CrimsonAnim.THRUST_RECOVERY_MISS, "a missed dash brakes with its own animation");
                }
            }, () -> recover[0] > 0 && c.getAttack() != Attack.DASH_THRUST, () -> {
                helper.assertTrue(moved[0], "the target sidestepped");
                helper.assertValueEqual(c.getThrustHits(), 0, "the dash missed");
                helper.assertValueEqual(lost(g), 0.0F, "the target is untouched");
                helper.assertTrue(c.isDashMissed(), "recorded as a miss");
                helper.assertTrue(Math.abs(recover[0] - CrimsonCommander.THRUST_RECOVER_MISS) <= 2,
                        "a miss recovers longer, 24 ticks (" + recover[0] + ")");
                c.discard();
                g.discard();
                helper.succeed();
            });
        });
    }

    // ------------------------------------------------------------------------------------------------ heavy slam
    private static void slamTwoRings(GameTestHelper helper) {
        plot(helper, 0, 0, 18, 18, () -> {
            CrimsonCommander c = commander(helper, 8, 3, 0.0F);
            IronGolem target = golem(helper, 8, Y + 1, 9);
            IronGolem edge = golem(helper, 11, Y + 1, 9);
            IronGolem outside = golem(helper, 15, Y + 1, 9);
            c.setTarget(target);
            Map<IronGolem, Vec3> home = Map.of(target, target.position(), edge, edge.position(), outside, outside.position()); // before the knock moves them
            helper.assertTrue(c.startAttack(Attack.HEAVY_SLAM, target), "the slam starts");
            helper.assertValueEqual(c.getAnim(), CrimsonAnim.SLAM_PREPARE, "two hands over the head");
            double[] peak = {c.getY()};
            waitUntil(helper, 70, t -> peak[0] = Math.max(peak[0], c.getY()), () -> c.getSlamsLanded() > 0, () -> {
                helper.assertTrue(peak[0] > c.getY() + 1.2D, "he leapt (rose " + (peak[0] - c.getY()) + " blocks)");
                Vec3 center = c.getLastSlamCenter();
                helper.assertTrue(c.getSlamRingCenter().distanceTo(center) < 1.0D, "the warning rings were drawn where the blade came down ("
                        + c.getSlamRingCenter() + " vs " + center + ")");
                for (IronGolem g : List.of(target, edge, outside)) {
                    double d = home.get(g).subtract(center).multiply(1.0D, 0.0D, 1.0D).length();
                    float expected = d <= CrimsonCommander.SLAM_EPICENTER_RADIUS ? CrimsonCommander.SLAM_EPICENTER_DAMAGE
                            : d <= CrimsonCommander.SLAM_RADIUS ? CrimsonCommander.SLAM_EDGE_DAMAGE : 0.0F;
                    helper.assertValueEqual(lost(g), expected, "damage at " + String.format("%.2f", d) + " blocks from the impact");
                    if (expected > 0.0F) {
                        helper.assertTrue(g.getDeltaMovement().y > 0.3D, "thrown up (vy " + g.getDeltaMovement().y + ")");
                    }
                }
                helper.assertTrue(lost(target) == CrimsonCommander.SLAM_EPICENTER_DAMAGE, "the target took the epicentre");
                helper.assertTrue(lost(edge) > 0.0F && lost(edge) < lost(target), "the edge ring hurts less");
                helper.assertValueEqual(lost(outside), 0.0F, "outside the 4-block ring nothing");
                later(helper, CrimsonCommander.SLAM_EXEC, () -> {
                    helper.assertValueEqual(c.getAnim(), CrimsonAnim.SLAM_RECOVERY, "then the long recovery, pulling the blade free");
                    c.discard();
                    target.discard();
                    edge.discard();
                    outside.discard();
                    helper.succeed();
                });
            });
        });
    }

    private static void slamDisablesShields(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // a mock player is not in the level's entity lists, so the shockwave cannot find it: the shield rule is exercised
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(!CrimsonCommander.disableShield(level, player), "nothing to disable without a shield");
        ItemStack shield = new ItemStack(Items.SHIELD);
        player.setItemInHand(InteractionHand.OFF_HAND, shield);
        helper.assertTrue(CrimsonCommander.disableShield(level, player), "a shield in hand is disabled, raised or not");
        helper.assertTrue(player.getCooldowns().isOnCooldown(shield), "for 100 ticks");
        helper.assertValueEqual(CrimsonCommander.SHIELD_DISABLE_TICKS, 100, "the cooldown is 100 ticks");
        helper.succeed();
    }

    private static void slamReachesPillar(GameTestHelper helper) {
        plot(helper, 0, 0, 18, 18, () -> {
            for (int x = 13; x <= 15; x++) { // a 3-block pillar: out of reach of a blade, not of a leap
                for (int z = 11; z <= 13; z++) {
                    for (int y = Y + 1; y <= Y + 3; y++) {
                        helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                    }
                }
            }
            CrimsonCommander c = commander(helper, 7, 12, -90.0F); // facing +x
            IronGolem g = golem(helper, 14, Y + 4, 12);
            c.setTarget(g);
            helper.assertTrue(c.startAttack(Attack.HEAVY_SLAM, g), "the slam starts against the pillar");
            Vec3[] last = {null};
            double[] maxStep = {0.0D};
            waitUntil(helper, 80, t -> {
                if (last[0] != null) {
                    maxStep[0] = Math.max(maxStep[0], c.position().distanceTo(last[0]));
                }
                last[0] = c.position();
            }, () -> c.getSlamsLanded() > 0, () -> {
                helper.assertValueEqual(c.getSlamsLanded(), 1, "the blade came down");
                helper.assertTrue(lost(g) >= CrimsonCommander.SLAM_EDGE_DAMAGE, "on the one hiding on the pillar (" + lost(g) + ")");
                helper.assertTrue(c.getY() >= helper.absoluteVec(new Vec3(0, Y + 4, 0)).y - 0.3D, "he landed on top (y " + c.getY() + ")");
                helper.assertTrue(maxStep[0] < 2.0D, "by leaping, never by teleporting (largest step " + maxStep[0] + ")");
                c.discard();
                g.discard();
                helper.succeed();
            });
        });
    }

    // ------------------------------------------------------------------------------------------------ guard
    private static void startGuard(GameTestHelper helper, CrimsonCommander c, LivingEntity facing, Runnable inGuard) {
        c.setTarget(facing);
        helper.assertTrue(c.startAttack(Attack.DEFENSIVE_GUARD, facing), "the guard starts");
        helper.assertValueEqual(c.getAnim(), CrimsonAnim.GUARD_PREPARE, "legs planted, sword crossing");
        later(helper, CrimsonCommander.GUARD_PREP + 2, () -> {
            helper.assertValueEqual(c.getPhase(), Phase.EXECUTE, "the guard is up");
            helper.assertValueEqual(c.getAnim(), CrimsonAnim.GUARD_HOLD, "holding the guard");
            helper.assertTrue(c.isGuarding(), "guarding");
            inGuard.run();
        });
    }

    private static void guardReducesFrontal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            CrimsonCommander guard = commander(helper, 6, 6, 0.0F);
            CrimsonCommander open = commander(helper, 16, 6, 0.0F);
            lockOffense(open);
            IronGolem frontA = golem(helper, 6, Y + 1, 13);   // 7 blocks in front: a blow from there is not melee
            IronGolem frontB = golem(helper, 16, Y + 1, 13);
            IronGolem behind = golem(helper, 6, Y + 1, 0);
            startGuard(helper, guard, frontA, () -> {
                guard.hurtServer(level, level.damageSources().mobAttack(frontA), 20.0F);
                open.hurtServer(level, level.damageSources().mobAttack(frontB), 20.0F);
                float guarded = lost(guard);
                float plain = lost(open);
                helper.assertTrue(guarded > 0.0F && guarded < plain * 0.5F, "the guard stops most frontal damage (" + guarded + " vs " + plain + ")");
                helper.assertValueEqual(guard.getAttack(), Attack.DEFENSIVE_GUARD, "a blow from afar is not answered with a riposte");
                helper.assertTrue(guard.getGuardBlocks() > 0, "counted as blocked");
                guard.invulnerableTime = 0;
                float before = guard.getHealth();
                guard.hurtServer(level, level.damageSources().mobAttack(behind), 20.0F);
                helper.assertTrue(Math.abs((before - guard.getHealth()) - plain) < 0.01F, "from behind the guard is useless ("
                        + (before - guard.getHealth()) + " vs " + plain + ")");
                for (LivingEntity e : List.of(guard, open, frontA, frontB, behind)) {
                    e.discard();
                }
                helper.succeed();
            });
        });
    }

    private static void guardDeflectsArrows(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 16, 18, () -> {
            CrimsonCommander c = commander(helper, 8, 6, 0.0F);
            IronGolem g = golem(helper, 8, Y + 1, 16);
            startGuard(helper, c, g, () -> {
                Arrow arrow = new Arrow(EntityType.ARROW, level);
                Vec3 from = helper.absoluteVec(new Vec3(8.5D, Y + 3.0D, 11.5D));
                arrow.setPos(from);
                arrow.setDeltaMovement(new Vec3(0.0D, 0.0D, -1.6D)); // straight at his chest
                level.addFreshEntity(arrow);
                waitUntil(helper, 15, t -> { }, () -> c.getDeflections() > 0, () -> {
                    helper.assertTrue(c.getDeflections() > 0, "the arrow is turned back");
                    helper.assertValueEqual(c.getHealth(), c.getMaxHealth(), "no damage");
                    later(helper, 2, () -> {
                        helper.assertTrue(arrow.getDeltaMovement().z > 0.0D || arrow.isRemoved(), "flying away from him (vz "
                                + arrow.getDeltaMovement().z + ")");
                        arrow.discard();
                        c.discard();
                        g.discard();
                        helper.succeed();
                    });
                });
            });
        });
    }

    private static void guardRiposte(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 16, 18, () -> {
            CrimsonCommander c = commander(helper, 8, 6, 0.0F);
            IronGolem g = golem(helper, 8, Y + 1, 8.5D); // in sword's reach, in front
            startGuard(helper, c, g, () -> {
                c.hurtServer(level, level.damageSources().mobAttack(g), 10.0F);
                helper.assertValueEqual(c.getAttack(), Attack.RIPOSTE, "a frontal melee blow breaks the guard into a riposte");
                helper.assertValueEqual(c.getAnim(), CrimsonAnim.RIPOSTE, "with its counter-cut");
                waitUntil(helper, 12, t -> { }, () -> c.getRipostes() > 0, () -> {
                    helper.assertValueEqual(lost(g), CrimsonCommander.RIPOSTE_DAMAGE, "14 to the attacker");
                    double z0 = g.getZ();
                    later(helper, 20, () -> {
                        helper.assertTrue(g.getZ() - z0 > 1.5D, "thrown far back (" + (g.getZ() - z0) + " blocks)");
                        c.discard();
                        g.discard();
                        helper.succeed();
                    });
                });
            });
        });
    }

    private static void guardTurnsSlowly(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 16, 16, () -> {
            CrimsonCommander c = commander(helper, 8, 8, 0.0F);
            IronGolem g = golem(helper, 8, Y + 1, 10.5D); // in front, in reach
            startGuard(helper, c, g, () -> {
                float yaw0 = c.getYRot();
                g.setPos(helper.absoluteVec(new Vec3(8.5D, Y + 1, 5.5D))); // steps around behind him
                later(helper, 2, () -> {
                    float turned = Math.abs(net.minecraft.util.Mth.wrapDegrees(c.getYRot() - yaw0));
                    helper.assertTrue(turned <= 2.0F * CrimsonCommander.GUARD_TURN_RATE + 0.5F, "the guard turns slowly (" + turned + " degrees in 2 ticks)");
                    helper.assertTrue(!c.isInFront(g.position()), "so the one behind him is not in front of the blade");
                    c.invulnerableTime = 0;
                    c.hurtServer(level, level.damageSources().mobAttack(g), 10.0F);
                    helper.assertValueEqual(c.getAttack(), Attack.DEFENSIVE_GUARD, "a blow from behind is not answered with a riposte");
                    helper.assertValueEqual(c.getGuardBlocks(), 0, "nor blocked");
                    c.discard();
                    g.discard();
                    helper.succeed();
                });
            });
        });
    }

    private static void guardAfterThreeHits(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 16, 18, () -> {
            CrimsonCommander c = commander(helper, 8, 4, 0.0F);
            IronGolem g = golem(helper, 8, Y + 1, 17);
            c.setTarget(g);
            for (Attack a : Attack.values()) {
                if (a != Attack.DEFENSIVE_GUARD) {
                    c.setCooldown(a, 100000);
                }
            }
            for (int i = 0; i < 3; i++) {
                final int n = i;
                later(helper, 1 + i * 8, () -> {
                    c.invulnerableTime = 0;
                    c.hurtServer(level, level.damageSources().mobAttack(g), 2.0F); // a blow from afar: no riposte
                    if (n < 2) {
                        helper.assertTrue(c.getAttack() != Attack.DEFENSIVE_GUARD, "not yet after " + (n + 1) + " blows");
                    }
                });
            }
            later(helper, 25, () -> c.hurtServer(level, level.damageSources().magic(), 1.0F)); // poison-like damage is not a blow
            waitUntil(helper, 30, t -> { }, () -> c.getAttack() == Attack.DEFENSIVE_GUARD, () -> {
                helper.assertValueEqual(c.getAttack(), Attack.DEFENSIVE_GUARD, "three blows inside two seconds raise the guard");
                c.discard();
                g.discard();
                helper.succeed();
            });
        });
    }

    // ------------------------------------------------------------------------------------------------ wave
    private static void wall(GameTestHelper h, int x0, int x1, int z) {
        for (int x = x0; x <= x1; x++) {
            for (int y = Y + 1; y <= Y + 5; y++) {
                h.setBlock(new BlockPos(x, y, z), Blocks.STONE);
            }
        }
    }

    private static void waveTravels(GameTestHelper helper) {
        plot(helper, 0, 0, 16, 18, () -> {
            wall(helper, 4, 12, 14); // the wave must stop here, before the golem behind it
            CrimsonCommander c = commander(helper, 8, 1, 0.0F);
            IronGolem g = golem(helper, 8, Y + 1, 11);
            IronGolem shielded = golem(helper, 8, Y + 1, 16);
            c.setTarget(g);
            helper.assertTrue(c.startAttack(Attack.BLOOD_WAVE, g), "the wave starts");
            helper.assertValueEqual(c.getAnim(), CrimsonAnim.WAVE_PREPARE, "the blade is charged first");
            waitUntil(helper, 70, t -> { }, () -> c.getWavesReleased() > 0 && c.getWaves().isEmpty(), () -> {
                helper.assertValueEqual(c.getWavesReleased(), 1, "one crescent released");
                helper.assertValueEqual(lost(g), CrimsonCommander.WAVE_DAMAGE, "13 to the target 10 blocks away");
                helper.assertValueEqual(lost(shielded), 0.0F, "the wall stops it: nothing behind");
                c.discard();
                g.discard();
                shielded.discard();
                helper.succeed();
            });
        });
    }

    private static void waveAgainstUnreachable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        final int ox = 3400; // alone: a neighbouring arena's floor would offer a way around the pit
        floor(helper, ox, 0, ox + 16, 26);
        BlockPos probe = helper.absolutePos(new BlockPos(ox + 8, Y + 1, 13));
        waitUntil(helper, 400, t -> { }, () -> level.isPositionEntityTicking(probe) && level.isPositionEntityTicking(probe.north(16))
                && level.isPositionEntityTicking(probe.south(16)), () -> waveAcrossPit(helper, ox));
    }

    private static void waveAcrossPit(GameTestHelper helper, int ox) {
        for (int x = ox; x <= ox + 16; x++) { // a pit across the hall: no walking over it
            for (int z = 9; z <= 13; z++) {
                helper.setBlock(new BlockPos(x, Y, z), Blocks.AIR);
            }
        }
        wall(helper, ox + 4, ox + 12, 22);
        CrimsonCommander c = commander(helper, ox + 8, 4, 0.0F);
        IronGolem g = golem(helper, ox + 8, Y + 1, 17);
        c.setTarget(g);
        Set<Attack> tried = EnumSet.noneOf(Attack.class);
        String[] seenAt = {""};
        waitUntil(helper, 60, t -> {
            if (tried.add(c.getAttack())) {
                seenAt[0] += c.getAttack() + "@" + t + " dist " + String.format("%.1f", c.distanceTo(g)) + " reach " + c.isTargetReachable() + "; ";
            }
        }, () -> c.getWavesReleased() > 0, () -> {
            helper.assertTrue(c.getWavesReleased() > 0, "across a pit he answers with the wave: " + seenAt[0]);
            helper.assertTrue(!tried.contains(Attack.CRIMSON_SLASH) && !tried.contains(Attack.DASH_THRUST),
                    "never a blade swung over the pit: " + tried);
            c.discard();
            g.discard();
            helper.succeed();
        });
    }

    // ------------------------------------------------------------------------------------------------ stages
    private static void stagesTransitionOnce(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            CrimsonCommander c = commander(helper, 9, 9, 0.0F);
            lockOffense(c);
            IronGolem anchor = golem(helper, 9, Y + 1, 12);
            c.setTarget(anchor);
            helper.assertValueEqual(c.getBossEvent().getColor(), BossEvent.BossBarColor.RED, "the bar starts deep crimson");
            c.setHealth(133.0F);
            c.hurtServer(level, level.damageSources().generic(), 5.0F); // below 60%
            helper.assertValueEqual(c.getActivity(), Activity.TRANSITION, "dropping below 60% awakens the oath");
            helper.assertValueEqual(c.getAnim(), CrimsonAnim.AWAKEN_TRANSITION, "raising the sword");
            helper.assertTrue(!c.hurtServer(level, level.damageSources().generic(), 5.0F), "invulnerable while transforming");
            later(helper, CrimsonCommander.AWAKEN_TICKS + 4, () -> {
                helper.assertValueEqual(c.getStage(), 2, "stage 2");
                helper.assertValueEqual(c.getTransitionCount(), 1, "one transition");
                helper.assertTrue(Math.abs(c.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.38D) < 1.0E-6D, "speed 0.38");
                helper.assertValueEqual(c.getBossEvent().getColor(), BossEvent.BossBarColor.PINK, "the bar turns incandescent ruby");
                helper.assertValueEqual(c.getVanguards(level).size(), 2, "two vanguards answered");
                helper.assertValueEqual(c.cooldownTicks(Attack.HEAVY_SLAM), 96, "cooldowns 20% shorter (120 -> 96)");
                c.invulnerableTime = 0;
                c.hurtServer(level, level.damageSources().generic(), 1.0F);
                helper.assertValueEqual(c.getTransitionCount(), 1, "more damage in stage 2 does not repeat it");
                c.setHealth(57.0F);
                c.invulnerableTime = 0;
                c.hurtServer(level, level.damageSources().generic(), 5.0F); // below 25%
                helper.assertValueEqual(c.getAnim(), CrimsonAnim.FURY_TRANSITION, "the fury transition");
            });
            later(helper, CrimsonCommander.AWAKEN_TICKS + 4 + CrimsonCommander.FURY_TRANSITION_TICKS + 6, () -> {
                helper.assertValueEqual(c.getStage(), 3, "stage 3");
                helper.assertValueEqual(c.getTransitionCount(), 2, "exactly two transitions");
                helper.assertTrue(Math.abs(c.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.42D) < 1.0E-6D, "speed 0.42");
                helper.assertTrue(Math.abs(c.damageFor(12.0F) - 15.0F) < 1.0E-4F, "25% more damage");
                helper.assertValueEqual(c.getBossEvent().getColor(), BossEvent.BossBarColor.YELLOW, "the bar turns blood gold");
                helper.assertValueEqual(c.getVanguards(level).size(), 2, "the oath is not called twice");
                c.setHealth(c.getMaxHealth());
                c.invulnerableTime = 0;
                c.hurtServer(level, level.damageSources().generic(), 1.0F);
                helper.assertValueEqual(c.getTransitionCount(), 2, "healing never repeats a transition");
                helper.assertValueEqual(c.getStage(), 3, "the stage never goes back");
                c.discard();
                anchor.discard();
                helper.succeed();
            });
        });
    }

    private static void furyCombo(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 16, 18, () -> {
            CrimsonCommander c = commander(helper, 8, 3, 0.0F);
            lockOffense(c);
            IronGolem g = golem(helper, 8, Y + 1, 11);
            c.setTarget(g);
            c.setHealth(50.0F);
            c.hurtServer(level, level.damageSources().generic(), 2.0F); // straight to the fury
            later(helper, CrimsonCommander.FURY_TRANSITION_TICKS + 4, () -> {
                helper.assertValueEqual(c.getStage(), 3, "in the fury");
                for (Attack a : Attack.values()) {
                    c.setCooldown(a, 0);
                }
                g.setPos(helper.absoluteVec(new Vec3(8.5D, Y + 1, 11.5D)));
                c.setPos(helper.absoluteVec(new Vec3(8.5D, Y + 1, 3.5D)));
                c.setTarget(g);
                helper.assertTrue(c.startAttack(Attack.DASH_THRUST, g), "the dash starts");
                boolean[] dashRecovered = {false};
                waitUntil(helper, 60, t -> {
                    if (c.getAttack() == Attack.DASH_THRUST && c.getPhase() == Phase.RECOVER) {
                        dashRecovered[0] = true;
                    }
                }, () -> c.getAttack() == Attack.HEAVY_SLAM, () -> {
                    helper.assertTrue(c.getThrustHits() > 0, "the dash found its mark");
                    helper.assertValueEqual(c.getAttack(), Attack.HEAVY_SLAM, "and goes straight into the slam");
                    helper.assertTrue(!dashRecovered[0], "skipping the dash's recovery");
                    helper.assertValueEqual(c.getCombos(), 1, "one combo");
                    helper.assertTrue(c.isComboSlam(), "marked as a combo");
                    c.discard();
                    g.discard();
                    helper.succeed();
                });
            });
        });
    }

    // ------------------------------------------------------------------------------------------------ vanguards
    private static void vanguardsCapAndAlliance(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            CrimsonCommander c = commander(helper, 9, 9, 0.0F);
            lockOffense(c);
            IronGolem g = golem(helper, 9, Y + 1, 14);
            c.setTarget(g);
            helper.assertValueEqual(c.summonVanguards(level, 5), 2, "at most two answer");
            helper.assertValueEqual(c.summonVanguards(level, 1), 0, "never a third");
            List<CrimsonAshVanguard> vs = c.getVanguards(level);
            helper.assertValueEqual(vs.size(), 2, "two bound to him");
            CrimsonAshVanguard a = vs.get(0);
            CrimsonAshVanguard b = vs.get(1);
            helper.assertValueEqual(a.getMaxHealth(), 28.0F, "28 HP");
            helper.assertValueEqual(a.getAttributeValue(Attributes.ATTACK_DAMAGE), 6.0D, "6 damage");
            helper.assertValueEqual(a.getBbHeight(), 1.95F, "1.95 tall");
            helper.assertTrue(a.isOwnedBy(c) && c.getUUID().equals(a.getOwnerUuid()), "bound by UUID");
            helper.assertTrue(!a.canAttack(c) && !a.canAttack(b) && !c.canAttack(a), "never at their master, nor he at them");
            helper.assertTrue(a.isAlliedTo(c) && c.isAlliedTo(a), "allies");
            helper.assertTrue(a.canAttack(g), "they fight what he fights");
            helper.assertTrue(!a.hurtServer(level, level.damageSources().mobAttack(c), 20.0F), "his blows pass through them");
            helper.assertTrue(!a.hurtServer(level, level.damageSources().mobAttack(b), 6.0F), "nor do they hurt each other");
            helper.assertValueEqual(CrimsonAshVanguard.LIFETIME, 600, "30 seconds of life");
            BlockPos at = a.blockPosition();
            a.hurtServer(level, level.damageSources().generic(), 100.0F);
            later(helper, CrimsonAshVanguard.CRUMBLE_TICKS + 4, () -> {
                helper.assertTrue(a.isRemoved(), "a slain vanguard crumbles away");
                int drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(4.0D)).size();
                helper.assertValueEqual(drops, 0, "and leaves nothing to farm");
                c.discard();
                g.discard();
                helper.succeed();
            });
        });
    }

    private static void vanguardsExpire(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            CrimsonCommander c = commander(helper, 9, 9, 0.0F);
            lockOffense(c);
            IronGolem anchor = golem(helper, 9, Y + 1, 14);
            c.setTarget(anchor);
            helper.assertValueEqual(c.summonVanguards(level, 2), 2, "two vanguards");
            List<CrimsonAshVanguard> vs = c.getVanguards(level);
            vs.get(0).setAge(CrimsonAshVanguard.LIFETIME - 5);
            later(helper, 5 + CrimsonAshVanguard.CRUMBLE_TICKS + 4, () -> {
                helper.assertTrue(vs.get(0).isRemoved(), "after 600 ticks it turns to ash");
                helper.assertTrue(!vs.get(1).isRemoved(), "the other still stands");
                c.hurtServer(level, level.damageSources().generic(), 1000.0F); // the master falls
            });
            later(helper, 5 + CrimsonAshVanguard.CRUMBLE_TICKS + 4 + CrimsonAshVanguard.CRUMBLE_TICKS + 6, () -> {
                helper.assertTrue(vs.get(1).isRemoved(), "the moment he falls, his vanguards crumble");
                anchor.discard();
                helper.succeed();
            });
        });
    }

    // ------------------------------------------------------------------------------------------------ death
    private static void deathRewardsOnce(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            BlockPos abs = helper.absolutePos(new BlockPos(9, Y + 1, 9));
            CrimsonCommander c = commander(helper, 9, 9, 0.0F);
            c.hurtServer(level, level.damageSources().generic(), 1000.0F);
            helper.assertValueEqual(c.getActivity(), Activity.DYING, "dying");
            helper.assertValueEqual(c.getAnim(), CrimsonAnim.DEATH_KNEEL, "he falls to one knee");
            helper.assertValueEqual(c.getDeathAwardCount(), 1, "rewards once");
            helper.assertTrue(!c.hurtServer(level, level.damageSources().generic(), 5.0F), "no more damage while dying");
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(abs).inflate(8.0D));
            int shards = drops.stream().filter(e -> e.getItem().is(ModItems.CRIMSON_PLATE_SHARD.get())).mapToInt(e -> e.getItem().getCount()).sum();
            helper.assertTrue(shards >= 3 && shards <= 6, "3 to 6 plate shards (" + shards + ")");
            helper.assertTrue(drops.stream().noneMatch(e -> e.getItem().is(ModItems.CRIMSON_OATH_GREATSWORD.get())), "the greatsword waits for the cinematic");
            Set<CrimsonAnim> seen = EnumSet.noneOf(CrimsonAnim.class);
            waitUntil(helper, CrimsonCommander.DEATH_TICKS + 10, t -> seen.add(c.getAnim()), c::isRemoved, () -> {
                helper.assertTrue(c.isRemoved(), "removed after the cinematic");
                helper.assertTrue(seen.contains(CrimsonAnim.DEATH_FINAL), "head bowed over the planted sword: " + seen);
                c.deliverGreatsword(level);
                List<ItemEntity> swords = level.getEntitiesOfClass(ItemEntity.class, new AABB(abs).inflate(8.0D),
                        e -> e.getItem().is(ModItems.CRIMSON_OATH_GREATSWORD.get()));
                helper.assertValueEqual(swords.size(), 1, "the greatsword is delivered exactly once");
                helper.assertValueEqual(c.getSwordsDelivered(), 1, "once, even if asked again");
                var spirits = level.getEntitiesOfClass(com.codenamecoraline.entity.spirit.BossSpirit.class, new AABB(abs).inflate(12.0D),
                        s -> s.getType() == ModEntities.CRIMSON_COMMANDER_SPIRIT.get());
                helper.assertValueEqual(spirits.size(), 1, "his spirit rises, once");
                helper.assertTrue(spirits.getFirst().getY() > abs.getY() + 0.5D, "and really rises");
                level.getEntitiesOfClass(ItemEntity.class, new AABB(abs).inflate(8.0D)).forEach(ItemEntity::discard);
                helper.succeed();
            });
        });
    }

    // ------------------------------------------------------------------------------------------------ content
    private static JsonObject lang(String name) {
        try (var in = CrimsonCommanderGameTests.class.getResourceAsStream("/assets/codenamecoraline/lang/" + name + ".json")) {
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            throw new IllegalStateException("lang " + name, e);
        }
    }

    private static void contentLoaded(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.assertTrue(Bestiary.ENTRIES.contains(Bestiary.CRIMSON_COMMANDER), "the Bestiary has his chapter");
        List<BestiaryPage> pages = Bestiary.CRIMSON_COMMANDER.pages();
        helper.assertValueEqual(pages.size(), 6, "six pages");
        helper.assertValueEqual(pages.get(1).drawingWidth() * 1000 + pages.get(1).drawingHeight(), 112 * 1000 + 80, "anatomy 112x80");
        helper.assertValueEqual(pages.get(2).drawingWidth() * 1000 + pages.get(2).drawingHeight(), 112 * 1000 + 72, "combat 112x72");
        helper.assertValueEqual(pages.get(5).drawingWidth() * 1000 + pages.get(5).drawingHeight(), 112 * 1000 + 64, "spoils 112x64");
        for (String art : new String[] {"crimson_commander_anatomy", "crimson_commander_combat", "crimson_commander_spoils"}) {
            helper.assertTrue(CrimsonCommanderGameTests.class.getResourceAsStream(
                    "/assets/codenamecoraline/textures/gui/bestiary/" + art + ".png") != null, "engraving " + art);
        }
        for (String adv : new String[] {"oath_discovered", "commander_summoned", "commander_defeated", "crimson_greatsword_obtained"}) {
            helper.assertTrue(level.getServer().getAdvancements().get(id("bosses/" + adv)) != null, "advancement bosses/" + adv);
        }
        var recipe = level.getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, id("crimson_oath_greatsword")));
        helper.assertTrue(recipe.isPresent(), "the greatsword recipe is loaded");
        for (String res : new String[] {"textures/gui/boss_bar/crimson_commander.png", "textures/entity/crimson_commander.png",
                "textures/entity/crimson_commander_eyes.png", "textures/entity/crimson_ash_vanguard.png",
                "textures/entity/crimson_ash_vanguard_eyes.png", "textures/item/crimson_oath_greatsword.png",
                "textures/item/crimson_plate_shard.png", "textures/item/stained_oath_crest.png",
                "textures/item/crimson_commander_portrait.png", "models/item/crimson_oath_greatsword_3d.json"}) {
            helper.assertTrue(CrimsonCommanderGameTests.class.getResourceAsStream("/assets/codenamecoraline/" + res) != null, "asset " + res);
        }
        helper.assertTrue(CrimsonCommanderGameTests.class.getResourceAsStream(
                "/data/codenamecoraline/loot_table/entities/crimson_commander.json") != null, "loot table");
        List<String> keys = new ArrayList<>(List.of("entity.codenamecoraline.crimson_commander", "entity.codenamecoraline.crimson_ash_vanguard",
                "item.codenamecoraline.crimson_oath_greatsword", "item.codenamecoraline.stained_oath_crest",
                "item.codenamecoraline.crimson_plate_shard", "item.codenamecoraline.crimson_commander_spawn_egg",
                "entity.codenamecoraline.crimson_commander.summon.wrong_block", "entity.codenamecoraline.crimson_commander.summon.no_space",
                "entity.codenamecoraline.crimson_commander.summon.already_active", "bestiary.codenamecoraline.crimson_commander.name"));
        for (BestiaryPage page : pages) {
            keys.add(Bestiary.CRIMSON_COMMANDER.titleKey(page));
            keys.add(Bestiary.CRIMSON_COMMANDER.bodyKey(page));
            if (page.drawing() != null) {
                keys.add(Bestiary.CRIMSON_COMMANDER.captionKey(page));
            }
        }
        for (String adv : new String[] {"oath_discovered", "commander_summoned", "commander_defeated", "crimson_greatsword_obtained"}) {
            keys.add("advancements.codenamecoraline.bosses." + adv + ".title");
            keys.add("advancements.codenamecoraline.bosses." + adv + ".description");
        }
        for (String langName : new String[] {"en_us", "pt_br"}) {
            JsonObject json = lang(langName);
            for (String key : keys) {
                helper.assertTrue(json.has(key), langName + " has " + key);
            }
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------------------------------------ targeting
    private static void targetPriority(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            CrimsonCommander c = commander(helper, 8, 4, 0.0F);
            lockOffense(c);
            IronGolem near = golem(helper, 8, Y + 1, 10);  // 6 blocks
            IronGolem far = golem(helper, 8, Y + 1, 13);   // 9 blocks
            later(helper, 1, () -> {
                c.setTarget(null);
                helper.assertTrue(c.reconsiderTarget(level) == near, "the nearest by default");
                c.hurtServer(level, level.damageSources().mobAttack(far), 2.0F);
                helper.assertTrue(c.reconsiderTarget(level) == far, "but whoever hurt him first comes first");
                far.discard();
                helper.assertTrue(c.reconsiderTarget(level) == near, "until that one is gone");
                IronGolem closer = golem(helper, 8, Y + 1, 7); // 3 blocks: closer, but not by 4
                helper.assertTrue(c.reconsiderTarget(level) == near, "a slightly closer foe does not make him waver");
                closer.setPos(helper.absoluteVec(new Vec3(8.5D, Y + 1, 5.5D)));
                helper.assertTrue(c.reconsiderTarget(level) == closer, "a clearly closer one does");
                c.discard();
                near.discard();
                closer.discard();
                helper.succeed();
            });
        });
    }

    private static void freesHimself(GameTestHelper helper) {
        plot(helper, 0, 0, 18, 18, () -> {
            for (int x = 7; x <= 9; x++) { // entombed in stone over his head
                for (int z = 7; z <= 9; z++) {
                    for (int y = Y + 1; y <= Y + 5; y++) {
                        helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                    }
                }
            }
            CrimsonCommander c = commander(helper, 8, 8, 0.0F);
            lockOffense(c);
            IronGolem anchor = golem(helper, 15, Y + 1, 8);
            c.setTarget(anchor);
            waitUntil(helper, 60, t -> { }, () -> c.getUnstuckMoves() > 0 && !c.isInWall(), () -> {
                helper.assertTrue(c.getUnstuckMoves() > 0, "he forces his way out");
                helper.assertTrue(!c.isInWall(), "and stands free");
                helper.assertTrue(c.position().distanceTo(helper.absoluteVec(new Vec3(8.5D, Y + 1, 8.5D))) <= 4.5D, "close by: no long teleport");
                c.discard();
                anchor.discard();
                helper.succeed();
            });
        });
    }

    private static void leapsOutOfDeadEnd(GameTestHelper helper) {
        plot(helper, 0, 0, 18, 18, () -> {
            for (int x = 0; x <= 18; x++) { // a ledge two blocks up, no stairs: no path, but a leap
                for (int z = 12; z <= 18; z++) {
                    helper.setBlock(new BlockPos(x, Y + 1, z), Blocks.STONE);
                    helper.setBlock(new BlockPos(x, Y + 2, z), Blocks.STONE);
                }
            }
            CrimsonCommander c = commander(helper, 10, 6, 0.0F);
            lockOffense(c); // every blow cooling down: only his legs can get him there
            IronGolem g = golem(helper, 10, Y + 3, 15);
            c.setTarget(g);
            Vec3[] last = {null};
            double[] maxStep = {0.0D};
            waitUntil(helper, 200, t -> {
                if (last[0] != null) {
                    maxStep[0] = Math.max(maxStep[0], c.position().distanceTo(last[0]));
                }
                last[0] = c.position();
            }, () -> c.getLeapsTaken() > 0 && !c.isLeaping() && c.onGround(), () -> {
                helper.assertTrue(c.getLeapsTaken() > 0, "stuck below the ledge, he leaps (" + c.getActivity() + " at " + c.position() + ")");
                helper.assertTrue(c.getY() >= helper.absoluteVec(new Vec3(0, Y + 3, 0)).y - 0.3D, "and lands on it (y " + c.getY() + ")");
                helper.assertTrue(maxStep[0] < 2.0D, "a real leap, not a teleport (largest step " + maxStep[0] + ")");
                c.discard();
                g.discard();
                helper.succeed();
            });
        });
    }

    private static void naturalSpawnRules(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var type = ModEntities.CRIMSON_COMMANDER.get();
        var random = level.getRandom();
        // the biome modifier: deep caves and mountains, never the plains
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        for (var key : List.of(net.minecraft.world.level.biome.Biomes.DEEP_DARK, net.minecraft.world.level.biome.Biomes.DRIPSTONE_CAVES,
                net.minecraft.world.level.biome.Biomes.JAGGED_PEAKS, net.minecraft.world.level.biome.Biomes.PLAINS)) {
            var entries = biomes.getOrThrow(key).value().getMobSettings().getMobs(net.minecraft.world.entity.MobCategory.MONSTER).unwrap()
                    .stream().filter(e -> e.value().type() == type).toList();
            boolean plains = key == net.minecraft.world.level.biome.Biomes.PLAINS;
            helper.assertTrue(plains ? entries.isEmpty() : entries.size() == 1 && entries.getFirst().weight() == 2,
                    (plains ? "never in " : "rare (weight 2) in ") + key.identifier());
        }
        BlockPos base = helper.absolutePos(new BlockPos(3500, Y, 10));
        BlockPos deep = new BlockPos(base.getX(), -20, base.getZ());
        for (int cx = -1; cx <= 1; cx++) {
            for (int cz = -1; cz <= 1; cz++) {
                level.setChunkForced((deep.getX() >> 4) + cx, (deep.getZ() >> 4) + cz, true);
            }
        }
        // a closed deepslate chamber below layer zero: dark, a sturdy floor, room for 3.5 blocks
        for (BlockPos p : BlockPos.betweenClosed(deep.offset(-3, -1, -3), deep.offset(3, 5, 3))) {
            boolean shell = Math.abs(p.getX() - deep.getX()) == 3 || Math.abs(p.getZ() - deep.getZ()) == 3
                    || p.getY() == deep.getY() - 1 || p.getY() == deep.getY() + 5;
            level.setBlock(p, shell ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
        }
        BlockPos high = new BlockPos(base.getX() + 20, 64, base.getZ());
        level.setBlock(high.below(), Blocks.STONE.defaultBlockState(), 3);
        waitUntil(helper, 200, t -> { }, () -> level.getMaxLocalRawBrightness(deep) <= 7, () -> {
            helper.assertTrue(level.getMaxLocalRawBrightness(deep) <= 7, "the chamber is dark (" + level.getMaxLocalRawBrightness(deep) + ")");
            helper.assertTrue(CrimsonCommander.checkSpawnRules(type, level, EntitySpawnReason.SPAWNER, deep, random),
                    "a dark deepslate chamber below 0 is his");
            helper.assertTrue(!CrimsonCommander.checkSpawnRules(type, level, EntitySpawnReason.SPAWNER, high, random), "never above layer 0");
            level.setBlock(deep.above(2).east(), Blocks.GLOWSTONE.defaultBlockState(), 3);
            waitUntil(helper, 200, t -> { }, () -> level.getMaxLocalRawBrightness(deep) > 7, () -> {
                helper.assertTrue(!CrimsonCommander.checkSpawnRules(type, level, EntitySpawnReason.SPAWNER, deep, random),
                        "never in the light (" + level.getMaxLocalRawBrightness(deep) + ")");
                level.setBlock(deep.above(2).east(), Blocks.AIR.defaultBlockState(), 3);
                waitUntil(helper, 400, t -> { }, () -> level.getMaxLocalRawBrightness(deep) <= 7 && level.isPositionEntityTicking(deep), () -> {
                    CrimsonCommander other = type.create(level, EntitySpawnReason.TRIGGERED);
                    other.setPos(deep.getX() + 0.5D, deep.getY(), deep.getZ() + 0.5D);
                    level.addFreshEntity(other);
                    helper.assertTrue(!CrimsonCommander.checkSpawnRules(type, level, EntitySpawnReason.SPAWNER, deep, random),
                            "never within 48 blocks of another Commander");
                    other.discard();
                    helper.succeed();
                });
            });
        });
    }

    // ------------------------------------------------------------------------------------------------ the greatsword
    private static void greatswordDash(GameTestHelper helper) {
        plot(helper, 0, 0, 16, 18, () -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setPos(helper.absoluteVec(new Vec3(8.5D, Y + 1, 3.5D)));
            player.setYRot(0.0F); // looking along +z
            player.setXRot(0.0F);
            player.setYHeadRot(0.0F);
            ItemStack sword = new ItemStack(ModItems.CRIMSON_OATH_GREATSWORD.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, sword);
            LivingEntity g = helper.spawnWithNoFreeWill(EntityType.VINDICATOR, new BlockPos(8, Y + 1, 6));
            g.setPos(helper.absoluteVec(new Vec3(8.5D, Y + 1, 6.5D)));
            LivingEntity cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(8, Y + 1, 8));
            cow.setPos(helper.absoluteVec(new Vec3(8.5D, Y + 1, 8.5D)));
            Vec3 start = player.position();
            InteractionResult result = sword.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            helper.assertTrue(result.consumesAction(), "the Crimson Dash starts");
            helper.assertTrue(CrimsonOathGreatswordItem.isDashing(player), "dashing");
            helper.assertValueEqual(sword.getDamageValue(), CrimsonOathGreatswordItem.DASH_DURABILITY_COST, "8 durability");
            helper.assertTrue(player.getCooldowns().isOnCooldown(sword), "and a cooldown");
            helper.assertTrue(!sword.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND).consumesAction(), "not again while cooling down");
            for (int i = 0; i < 12 && CrimsonOathGreatswordItem.isDashing(player); i++) {
                // the mock player is not ticked by the server: drive the dash and the motion by hand
                CrimsonOathGreatswordItem.tickDash(player);
                player.move(MoverType.SELF, player.getDeltaMovement());
            }
            helper.assertTrue(!CrimsonOathGreatswordItem.isDashing(player), "the dash ends");
            double travelled = player.position().subtract(start).multiply(1.0D, 0.0D, 1.0D).length();
            helper.assertTrue(travelled > 5.0D && travelled < 7.0D, "about 6 blocks forward (" + travelled + ")");
            helper.assertValueEqual(lost(g), CrimsonOathGreatswordItem.DASH_DAMAGE, "8 to the hostile one in the line, once");
            helper.assertValueEqual(lost(cow), 0.0F, "never to a passive animal (nor to pets, mounts or villagers)");
            g.discard();
            cow.discard();
            helper.succeed();
        });
    }

    private static void healsWhileVanguardsFight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            CrimsonCommander c = commander(helper, 9, 10, 180.0F);
            IronGolem g = golem(helper, 9, Y + 1, 5);
            c.setTarget(g);
            c.setHealth(100.0F);
            helper.assertTrue(c.startHealing(level, g), "the heal starts");
            helper.assertValueEqual(c.getActivity(), Activity.HEALING, "healing");
            double start = c.distanceTo(g);
            Set<CrimsonAnim> seen = EnumSet.noneOf(CrimsonAnim.class);
            double[] far = {start};
            waitUntil(helper, 200, t -> {
                seen.add(c.getAnim());
                far[0] = Math.max(far[0], c.distanceTo(g));
            }, () -> c.getHealsDone() > 0, () -> {
                helper.assertTrue(far[0] > start + 3.0D, "he backs away first (from " + start + " to " + far[0] + ")");
                helper.assertTrue(seen.contains(CrimsonAnim.HEAL_CHANNEL), "then channels the heal: " + seen);
                helper.assertTrue(c.getHealedTotal() >= c.getMaxHealth() * 0.12F, "he recovers about 15% (" + c.getHealedTotal() + ")");
                helper.assertValueEqual(c.getVanguards(level).size(), 2, "his vanguards fight for him meanwhile");
                helper.assertValueEqual(c.getActivity(), Activity.GROUND, "then back to the duel");
                c.discard();
                g.discard();
                helper.succeed();
            });
        });
    }

    private static void alliances(GameTestHelper helper) {
        plot(helper, 0, 0, 16, 16, () -> {
            CrimsonCommander c = commander(helper, 4, 4, 0.0F);
            CrimsonCommander other = commander(helper, 12, 12, 0.0F);
            LivingEntity cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(8, Y + 1, 4));
            IronGolem g = golem(helper, 4, Y + 1, 12);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            helper.assertTrue(c.canAttack(g), "iron golems are foes");
            helper.assertTrue(c.canAttack(player), "players are foes");
            helper.assertTrue(!c.canAttack(cow), "passive animals are ignored");
            helper.assertTrue(!c.canAttack(other), "never another Commander");
            c.discard();
            other.discard();
            cow.discard();
            g.discard();
            helper.succeed();
        });
    }
}
