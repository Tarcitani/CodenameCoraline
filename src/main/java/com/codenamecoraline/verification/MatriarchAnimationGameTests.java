package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.client.ten_threads_witch.TenThreadsParts;
import com.codenamecoraline.client.ten_threads_witch.matriarch.MatClip;
import com.codenamecoraline.client.ten_threads_witch.matriarch.MatClips;
import com.codenamecoraline.client.ten_threads_witch.matriarch.MatRig;
import com.codenamecoraline.client.ten_threads_witch.matriarch.MatriarchAnimator;
import com.codenamecoraline.client.ten_threads_witch.matriarch.MatriarchModel;
import com.codenamecoraline.client.ten_threads_witch.matriarch.MatriarchRenderState;
import com.codenamecoraline.client.ten_threads_witch.matriarch.PoseMixer;
import com.codenamecoraline.entity.ten_threads_witch.EncounterPhase;
import com.codenamecoraline.entity.ten_threads_witch.MatriarchWitchEntity;
import com.codenamecoraline.entity.ten_threads_witch.PuppetAttack;
import com.codenamecoraline.entity.ten_threads_witch.PuppetEntity;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatAnim;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatClipRef;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatEvent;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatSlot;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchMood;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchMoveset;
import com.codenamecoraline.registry.ModEntities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Tests of the Matriarch's animation system (docs/design/matriarch_animation.md): the catalogue is complete and consistent,
 * the rig has its joints, the composition is independent of submission order, and the twelve acceptance scenarios of the
 * specification as far as code can check them (poses composed by the real client animator; flows run by the real server
 * director on a real entity). What a test cannot show is how it looks: that remains for {@code runClient}.
 */
public final class MatriarchAnimationGameTests {
    private static final int Y = 100;
    private static final int PUMP_TICKS = 700;
    private static final Map<String, Consumer<GameTestHelper>> TESTS = new LinkedHashMap<>();

    static {
        TESTS.put("ma_catalogue_complete", MatriarchAnimationGameTests::catalogueComplete);
        TESTS.put("ma_rig_has_the_joints", MatriarchAnimationGameTests::rigJoints);
        TESTS.put("ma_composition_order_independent", MatriarchAnimationGameTests::compositionOrder);
        TESTS.put("ma_t01_idle_is_alive", MatriarchAnimationGameTests::idleAlive);
        TESTS.put("ma_t02_walk_head_and_fingers_independent", MatriarchAnimationGameTests::walkHeadFingers);
        TESTS.put("ma_t03_walks_while_controlling", MatriarchAnimationGameTests::walkWhileControlling);
        TESTS.put("ma_t04_two_hands_two_commands", MatriarchAnimationGameTests::twoHands);
        TESTS.put("ma_t05_float_without_jumps", MatriarchAnimationGameTests::floatWithoutJumps);
        TESTS.put("ma_t06_thread_attack_while_walking", MatriarchAnimationGameTests::threadAttackWhileWalking);
        TESTS.put("ma_t07_hit_spares_the_hands", MatriarchAnimationGameTests::hitSparesHands);
        TESTS.put("ma_t08_stun_takes_over_and_lets_go", MatriarchAnimationGameTests::stunTakesOver);
        TESTS.put("ma_t09_fall_and_recovery", MatriarchAnimationGameTests::fallAndRecovery);
        TESTS.put("ma_t10_stage_changes_the_body", MatriarchAnimationGameTests::stageChanges);
        TESTS.put("ma_t11_intro_before_combat", MatriarchAnimationGameTests::introBeforeCombat);
        TESTS.put("ma_t12_death_sequence", MatriarchAnimationGameTests::deathSequence);
        TESTS.put("ma_events_fire_once", MatriarchAnimationGameTests::eventsOnce);
        TESTS.put("ma_priority_and_waiting", MatriarchAnimationGameTests::priorityRules);
        TESTS.put("ma_combo_branches", MatriarchAnimationGameTests::comboBranches);
        TESTS.put("ma_fury_breathes_between_sequences", MatriarchAnimationGameTests::furyRest);
        TESTS.put("ma_no_knockdown_or_stun_lock", MatriarchAnimationGameTests::noLock);
    }

    private MatriarchAnimationGameTests() {}

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, name);
    }

    public static void functions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, registry -> TESTS.forEach((name, fn) -> registry.register(id(name),
                (Consumer<GameTestHelper>) helper -> {
                    pump(helper);
                    fn.accept(helper);
                })));
    }

    public static void tests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("matriarch_animation_environment"));
        TESTS.forEach((name, fn) -> event.registerTest(id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                        new TestData<>(environment, id("basilisk_test_arena"), PUMP_TICKS, 0, true, Rotation.NONE, false, 1, 1, true, 0))));
    }

    // ------------------------------------------------------------------------------------------------ helpers
    private record Waiter(int max, java.util.function.BooleanSupplier stop, Runnable done, int[] tick) {}

    private static final Map<GameTestHelper, List<Waiter>> WAITERS = new java.util.IdentityHashMap<>();

    private static void pump(GameTestHelper h) {
        List<Waiter> list = new ArrayList<>();
        WAITERS.put(h, list);
        for (int t = 1; t <= PUMP_TICKS - 5; t++) {
            h.runAfterDelay(t, () -> {
                for (Waiter w : new ArrayList<>(list)) {
                    if (!list.contains(w)) {
                        continue;
                    }
                    w.tick()[0]++;
                    if (w.stop().getAsBoolean() || w.tick()[0] >= w.max()) {
                        list.remove(w);
                        w.done().run();
                    }
                }
            });
        }
    }

    private static void waitUntil(GameTestHelper h, int max, java.util.function.BooleanSupplier stop, Runnable done) {
        WAITERS.get(h).add(new Waiter(max, stop, done, new int[1]));
    }

    private static void later(GameTestHelper h, int ticks, Runnable task) {
        waitUntil(h, ticks, () -> false, task);
    }

    /** A stone floor and a Matriarch on it (far from the other tests' plots), once the chunks tick. */
    private static void withWitch(GameTestHelper h, int ox, Consumer<MatriarchWitchEntity> body) {
        ServerLevel level = h.getLevel();
        for (int x = ox; x <= ox + 18; x++) {
            for (int z = 0; z <= 18; z++) {
                h.setBlock(new BlockPos(x, Y, z), Blocks.STONE);
            }
        }
        BlockPos a = h.absolutePos(new BlockPos(ox, Y, 0));
        BlockPos b = h.absolutePos(new BlockPos(ox + 18, Y, 18));
        for (int cx = (Math.min(a.getX(), b.getX()) >> 4) - 1; cx <= (Math.max(a.getX(), b.getX()) >> 4) + 1; cx++) {
            for (int cz = (Math.min(a.getZ(), b.getZ()) >> 4) - 1; cz <= (Math.max(a.getZ(), b.getZ()) >> 4) + 1; cz++) {
                level.setChunkForced(cx, cz, true);
            }
        }
        java.util.function.BooleanSupplier ticking = () -> level.isPositionEntityTicking(a) && level.isPositionEntityTicking(b);
        waitUntil(h, 300, ticking, () -> {
            h.assertTrue(ticking.getAsBoolean(), "the plot ticks entities");
            MatriarchWitchEntity w = h.spawn(ModEntities.TEN_THREADS_WITCH.get(), new BlockPos(ox + 9, Y + 1, 9));
            w.setPos(h.absoluteVec(new Vec3(ox + 9.5D, Y + 1, 9.5D)));
            body.accept(w);
        });
    }

    private static void cleanup(ServerLevel level, MatriarchWitchEntity w) {
        for (PuppetEntity p : w.getPuppets(level)) {
            p.discard();
        }
        w.discard();
    }

    /** A client animator driven by hand: clips are put on its synched slots as the server would. */
    private static final class Rig {
        final MatriarchAnimator animator = new MatriarchAnimator(7L);
        final MatriarchAnimator.Input in = new MatriarchAnimator.Input();
        int seq = 0;

        void play(MatSlot slot, MatAnim a, int variant, float at) {
            this.in.sync.receive(slot.ordinal(), MatClipRef.pack(a, variant, ++this.seq), (int) at);
        }

        void stop(MatSlot slot, float at) {
            this.in.sync.receive(slot.ordinal(), MatClipRef.pack(null, 0, ++this.seq), (int) at);
        }

        float[] at(float t) {
            this.in.now = t;
            return this.animator.animate(this.in).clone();
        }
    }

    private static float rot(float[] pose, int joint, int axis) {
        return pose[(joint * 3 + MatRig.ROT) * 3 + axis] * Mth.RAD_TO_DEG;
    }

    private static float fx(float[] pose, int joint, int axis) {
        return pose[(joint * 3 + MatRig.POS) * 3 + axis];
    }

    private static boolean finite(float[] pose) {
        for (float v : pose) {
            if (!Float.isFinite(v)) {
                return false;
            }
        }
        return true;
    }

    private static float range(List<Float> values) {
        float lo = Float.MAX_VALUE;
        float hi = -Float.MAX_VALUE;
        for (float v : values) {
            lo = Math.min(lo, v);
            hi = Math.max(hi, v);
        }
        return hi - lo;
    }

    // ------------------------------------------------------------------------------------------------ catalogue and rig
    private static void catalogueComplete(GameTestHelper helper) {
        MatClips clips = MatClips.get();
        Set<String> codes = new HashSet<>();
        int authored = 0;
        for (MatAnim a : MatAnim.values()) {
            helper.assertTrue(codes.add(a.code), "unique code " + a.code);
            if (!MatClips.authored(a)) {
                continue;
            }
            authored++;
            helper.assertTrue(clips.has(a), a.code + " has keyframes of its own");
            MatClip c = clips.get(a, false, false);
            helper.assertTrue(c.trackCount() > 0, a.code + " moves something");
            helper.assertTrue(Math.abs(c.lengthSeconds() - a.ticks / 20.0F) < 1.0E-4F, a.code + " lasts " + a.ticks + " ticks");
            helper.assertValueEqual(c.loops(), a.loops(), a.code + " loop flag");
            helper.assertTrue(clips.get(a, true, false).trackCount() == c.trackCount(), a.code + " has a mirror");
            for (MatAnim.Ev ev : a.events) {
                helper.assertTrue(ev.tick() > 0 && ev.tick() <= a.ticks, a.code + " event " + ev.event() + " inside the clip");
            }
        }
        for (MatAnim a : List.of(MatAnim.FALL_03, MatAnim.FALL_04, MatAnim.FALL_05, MatAnim.FALL_06, MatAnim.FALL_07, MatAnim.FALL_08, MatAnim.FALL_09)) {
            helper.assertTrue(clips.hasProne(a), a.code + " exists face down too");
        }
        // every ID of the specification is there (6.1 to 6.14 and 7)
        String[] families = {"MAT-IDLE-", "MAT-MOVE-", "MAT-FLOAT-", "MAT-THREAD-", "MAT-ATK-", "MAT-DEF-", "MAT-HIT-", "MAT-STUN-", "MAT-FALL-",
                "MAT-EXP-", "MAT-CLOTH-", "MAT-HAIR-", "MAT-PHASE-", "MAT-CIN-"};
        int[] expected = {10, 10, 9, 28, 13, 9, 8, 5, 9, 10, 5, 6, 5, 6};
        for (int i = 0; i < families.length; i++) {
            Set<String> ids = new HashSet<>();
            for (MatAnim a : MatAnim.values()) {
                if (a.code.startsWith(families[i])) {
                    ids.add(a.code.substring(0, families[i].length() + 2));
                }
            }
            helper.assertValueEqual(ids.size(), expected[i], families[i] + " IDs");
        }
        // every attack of the last phase has its clips; combos only lead to her own attacks
        for (PuppetAttack atk : MatriarchMoveset.FURY_ATTACKS) {
            MatriarchMoveset.Look look = MatriarchMoveset.look(atk);
            helper.assertTrue(look.prep() != null && look.exec() != null, atk.id + " has its clips");
        }
        for (MatriarchMoveset.Branch b : MatriarchMoveset.COMBOS) {
            helper.assertTrue(MatriarchMoveset.FURY_ATTACKS.contains(b.to()), "combo " + b.from() + " leads to one of her attacks");
            helper.assertTrue(MatriarchMoveset.FURY_ATTACKS.stream().anyMatch(x -> x.id.equals(b.from())), "combo starts from one of her attacks");
        }
        helper.assertTrue(MatriarchWitchEntity.INTRO_TICKS == MatAnim.CIN_01.ticks + MatAnim.CIN_02.ticks + MatAnim.CIN_03.ticks, "the intro lasts its three clips");
        helper.assertTrue(MatriarchWitchEntity.DEATH_TICKS == MatAnim.CIN_05.ticks + MatAnim.CIN_06.ticks, "the death lasts its two clips");
        helper.assertTrue(authored > 120, "more than 120 clips authored (" + authored + ")");
        helper.succeed();
    }

    private static void rigJoints(GameTestHelper helper) {
        List<String> parts = TenThreadsParts.partNames(MatRig.ID);
        for (String j : List.of("body", "chest", "head", "arm_l", "forearm_l", "hand_l", "thumb_l", "finger_l0", "finger_l1", "finger_l2", "finger_l3",
                "arm_r", "forearm_r", "hand_r", "thumb_r", "finger_r0", "finger_r3", "brow_l", "brow_r", "mouth", "mouth_l", "mouth_r", "eye_l", "eye_r",
                "hair_lock_l", "hair_lock_l_tip", "hair_lock_r", "hair_lock_r_tip", "skirt", "skirt_hem", "cloak_l", "cloak_r", "hood_tip", "foot_l", "foot_r",
                "staff", "wheel", "spool_9", "blindfold")) {
            helper.assertTrue(parts.contains(j), "the rig has " + j);
        }
        var model = new MatriarchModel(TenThreadsParts.layer(MatRig.ID).bakeRoot());
        var state = new MatriarchRenderState();
        model.setupAnim(state);
        helper.assertTrue(!model.part(MatRig.HEAD).skipDraw && !model.part(MatRig.FINGERS_L[0]).skipDraw, "at rest everything is drawn");
        state.brokenSpools = 0x3FF;
        state.blindfoldOff = true;
        model.setupAnim(state);
        helper.assertTrue(!model.root().getChild("body").getChild("chest").getChild("head").getChild("blindfold").visible, "the blindfold comes off");
        helper.succeed();
    }

    private static void compositionOrder(GameTestHelper helper) {
        PoseMixer a = new PoseMixer();
        PoseMixer b = new PoseMixer();
        int ch = MatRig.ARM_L * 3 + MatRig.ROT;
        a.begin();
        b.begin();
        a.submit(ch, 0.3F, 0, 0, 0.7F, 9.0F, false, 31);
        a.submit(ch, -0.5F, 0.1F, 0, 1.0F, 7.0F, false, 40);
        a.submit(ch, 0.05F, 0, 0.02F, 1.0F, 6.0F, true, 60);
        b.submit(ch, 0.05F, 0, 0.02F, 1.0F, 6.0F, true, 60);
        b.submit(ch, -0.5F, 0.1F, 0, 1.0F, 7.0F, false, 40);
        b.submit(ch, 0.3F, 0, 0, 0.7F, 9.0F, false, 31);
        a.resolve();
        b.resolve();
        for (int i = 0; i < 3; i++) {
            helper.assertTrue(Math.abs(a.out[ch * 3 + i] - b.out[ch * 3 + i]) < 1.0E-6F, "the result does not depend on the order of submission");
        }
        helper.assertTrue(Math.abs(a.out[ch * 3] - (-0.45F)) < 1.0E-5F, "the stronger override wins, the reaction is added on top (" + a.out[ch * 3] + ")");
        helper.succeed();
    }

    // ------------------------------------------------------------------------------------------------ acceptance scenarios (client)
    private static void idleAlive(GameTestHelper helper) {
        Rig r = new Rig();
        List<Float> chest = new ArrayList<>();
        List<Float> finger = new ArrayList<>();
        List<Float> hem = new ArrayList<>();
        for (int t = 0; t <= 120; t++) {
            float[] p = r.at(t);
            helper.assertTrue(finite(p), "finite pose at " + t);
            chest.add(rot(p, MatRig.CHEST, 0));
            finger.add(rot(p, MatRig.FINGERS_L[1], 2));
            hem.add(rot(p, MatRig.SKIRT_HEM, 0));
        }
        helper.assertTrue(range(chest) > 0.8F, "she breathes (chest " + range(chest) + ")");
        helper.assertTrue(range(finger) > 2.0F, "her fingers move (" + range(finger) + ")");
        helper.succeed();
    }

    private static void walkHeadFingers(GameTestHelper helper) {
        Rig r = new Rig();
        r.in.vx = 0.05D;
        r.in.bodyYaw = -90.0F; // facing +x, the way she walks
        r.in.look = new Vec3(0.0D, 0.0D, 5.0D); // the player stands to her side
        List<Float> leg = new ArrayList<>();
        List<Float> finger = new ArrayList<>();
        float head = 0.0F;
        for (int t = 0; t <= 80; t++) {
            float[] p = r.at(t);
            leg.add(rot(p, MatRig.LEG_L, 0));
            finger.add(rot(p, MatRig.FINGERS_L[2], 2));
            head = rot(p, MatRig.HEAD, 1);
        }
        helper.assertTrue(r.animator.gait().cycle() == MatAnim.MOVE_01, "she walks (" + r.animator.gait().cycle() + ")");
        helper.assertTrue(range(leg) > 20.0F, "the legs step (" + range(leg) + ")");
        helper.assertTrue(Math.abs(head) > 20.0F, "the head turns to the player while she walks (" + head + ")");
        helper.assertTrue(range(finger) > 2.0F, "the fingers keep their own motion (" + range(finger) + ")");
        helper.succeed();
    }

    private static void walkWhileControlling(GameTestHelper helper) {
        Rig r = new Rig();
        r.in.vx = 0.05D;
        r.in.bodyYaw = -90.0F;
        r.play(MatSlot.HAND_L, MatAnim.THREAD_12, 0, 0);
        List<Float> left = new ArrayList<>();
        List<Float> right = new ArrayList<>();
        List<Float> leg = new ArrayList<>();
        for (int t = 0; t <= 80; t++) {
            float[] p = r.at(t);
            if (t > 10) {
                left.add(rot(p, MatRig.ARM_L, 0));
                right.add(rot(p, MatRig.ARM_R, 0));
                leg.add(rot(p, MatRig.LEG_L, 0));
            }
        }
        helper.assertTrue(range(leg) > 20.0F, "she keeps walking (" + range(leg) + ")");
        helper.assertTrue(range(left) < 5.0F, "the controlling hand holds its puppet instead of swinging (" + range(left) + ")");
        helper.assertTrue(range(right) > 8.0F, "the free arm swings with the walk (" + range(right) + ")");
        helper.succeed();
    }

    private static void twoHands(GameTestHelper helper) {
        Rig r = new Rig();
        r.play(MatSlot.HAND_L, MatAnim.THREAD_11, 0, 0);
        int differing = 0;
        List<Float> left = new ArrayList<>();
        for (int t = 0; t <= 60; t++) {
            float[] p = r.at(t);
            if (t > 6) {
                float l = rot(p, MatRig.FOREARM_L, 0);
                float rr = rot(p, MatRig.FOREARM_R, 0);
                left.add(l);
                if (Math.abs(l - rr) > 8.0F) {
                    differing++;
                }
            }
        }
        helper.assertTrue(range(left) > 15.0F, "the left hand works (" + range(left) + ")");
        helper.assertTrue(differing > 20, "the two hands give different commands (" + differing + " frames apart)");
        // and two separate slots: control on the left, the hanging puppets on the right (mirrored for that hand)
        Rig s = new Rig();
        s.play(MatSlot.HAND_L, MatAnim.THREAD_09, 0, 0);
        s.play(MatSlot.HAND_R, MatAnim.THREAD_10, 0, 0);
        float[] p = s.at(30);
        helper.assertTrue(Math.abs(rot(p, MatRig.ARM_L, 0) - rot(p, MatRig.ARM_R, 0)) > 8.0F, "each hand its own gesture");
        helper.succeed();
    }

    private static void floatWithoutJumps(GameTestHelper helper) {
        Rig r = new Rig();
        int[] joints = {MatRig.BODY, MatRig.CHEST, MatRig.LEG_L, MatRig.LEG_R, MatRig.FOOT_L, MatRig.ARM_L, MatRig.ARM_R};
        float[] prev = null;
        float worst = 0.0F;
        for (int t = 0; t <= 120; t++) {
            r.in.floating = t >= 20 && t < 80; // lift off, hover, land
            float[] p = r.at(t);
            if (prev != null) {
                for (int j : joints) {
                    for (int axis = 0; axis < 3; axis++) {
                        worst = Math.max(worst, Math.abs(rot(p, j, axis) - rot(prev, j, axis)));
                    }
                }
            }
            prev = p;
        }
        helper.assertTrue(worst < 12.0F, "no joint jumps more than 12 degrees in a tick through lift-off and landing (" + worst + ")");
        helper.succeed();
    }

    private static void threadAttackWhileWalking(GameTestHelper helper) {
        Rig r = new Rig();
        r.in.vx = 0.05D;
        r.in.bodyYaw = -90.0F;
        r.play(MatSlot.HAND_L, MatAnim.THREAD_23, 0, 20);
        List<Float> leg = new ArrayList<>();
        float reach = 0.0F;
        for (int t = 0; t <= 40; t++) {
            float[] p = r.at(t);
            leg.add(rot(p, MatRig.LEG_L, 0));
            if (t == 28) {
                reach = fx(p, MatRig.ATTACK, 0);
            }
        }
        helper.assertTrue(reach > 0.8F, "the whip reaches out at its peak (" + reach + ")");
        helper.assertTrue(range(leg) > 20.0F, "while her legs keep walking (" + range(leg) + ")");
        helper.succeed();
    }

    private static void hitSparesHands(GameTestHelper helper) {
        Rig a = new Rig();
        Rig b = new Rig();
        a.play(MatSlot.HAND_L, MatAnim.THREAD_09, 0, 0);
        b.play(MatSlot.HAND_L, MatAnim.THREAD_09, 0, 0);
        b.play(MatSlot.REACT, MatAnim.HIT_05, 0, 38);
        float[] pa = null;
        float[] pb = null;
        for (int t = 0; t <= 41; t++) {
            pa = a.at(t);
            pb = b.at(t);
        }
        for (int j : new int[] {MatRig.ARM_L, MatRig.FOREARM_L, MatRig.HAND_L}) {
            for (int axis = 0; axis < 3; axis++) {
                helper.assertTrue(Math.abs(rot(pa, j, axis) - rot(pb, j, axis)) < 1.0E-3F, "the hit leaves the hand that holds the thread alone");
            }
        }
        helper.assertTrue(Math.abs(rot(pa, MatRig.CHEST, 0) - rot(pb, MatRig.CHEST, 0)) > 2.0F, "the chest reacts");
        // and on the server: a light blow is a reaction on top, the thread work goes on
        withWitch(helper, 0, w -> {
            ServerLevel level = helper.getLevel();
            w.beginDirect(level, 11);
            w.getDirector().play(MatAnim.THREAD_09);
            boolean cancel = w.getDirector().onHurt(level.damageSources().generic(), 3.0F);
            helper.assertTrue(!cancel, "a light blow cancels nothing");
            helper.assertValueEqual(w.getDirector().current(MatSlot.HAND_L), MatAnim.THREAD_09, "the left hand keeps its thread");
            helper.assertValueEqual(w.getDirector().current(MatSlot.REACT), MatAnim.HIT_05, "a reaction for a hit while manipulating");
            cleanup(level, w);
            helper.succeed();
        });
    }

    private static void stunTakesOver(GameTestHelper helper) {
        Rig a = new Rig();
        Rig ref = new Rig();
        a.play(MatSlot.HAND_L, MatAnim.THREAD_09, 0, 0);
        ref.play(MatSlot.HAND_L, MatAnim.THREAD_09, 0, 0);
        a.play(MatSlot.BODY, MatAnim.STUN_02, 0, 20);
        float[] during = null;
        float[] after = null;
        float[] reference = null;
        for (int t = 0; t <= 60; t++) {
            if (t == 38) {
                a.stop(MatSlot.BODY, 38);
            }
            float[] p = a.at(t);
            float[] q = ref.at(t);
            if (t == 30) {
                during = p;
            }
            if (t == 60) {
                after = p;
                reference = q;
            }
        }
        helper.assertTrue(Math.abs(rot(during, MatRig.ARM_L, 2) - rot(reference, MatRig.ARM_L, 2)) > 15.0F, "the stun takes the arms over");
        for (int axis = 0; axis < 3; axis++) {
            helper.assertTrue(Math.abs(rot(after, MatRig.ARM_L, axis) - rot(reference, MatRig.ARM_L, axis)) < 1.0E-2F,
                    "after the stun nothing is left stuck: the arm is back on its thread (" + rot(after, MatRig.ARM_L, axis) + " vs " + rot(reference, MatRig.ARM_L, axis) + ")");
        }
        helper.succeed();
    }

    private static void fallAndRecovery(GameTestHelper helper) {
        // the client: the fall, the ground, the recovery, with no jump between them
        Rig r = new Rig();
        r.play(MatSlot.BODY, MatAnim.FALL_02, 0, 10);
        r.play(MatSlot.BODY, MatAnim.FALL_04, 0, 26);
        r.play(MatSlot.BODY, MatAnim.FALL_05, 0, 62);
        float[] prev = null;
        float worst = 0.0F;
        for (int t = 0; t <= 100; t++) {
            float[] p = r.at(t);
            if (prev != null) {
                for (int j : new int[] {MatRig.CHEST, MatRig.LEG_L, MatRig.ARM_L}) {
                    worst = Math.max(worst, Math.abs(rot(p, j, 0) - rot(prev, j, 0)));
                }
            }
            prev = p;
        }
        helper.assertTrue(worst < 40.0F, "fall, impact and recovery chain without a jump (" + worst + ")");
        // the server: a heavy blow knocks her down; she stays down, then gets up by one of the recoveries
        withWitch(helper, 20, w -> {
            ServerLevel level = helper.getLevel();
            w.beginDirect(level, 11);
            boolean cancel = w.getDirector().onHurt(level.damageSources().generic(), 14.0F);
            helper.assertTrue(cancel, "a knockdown cancels what she was doing");
            helper.assertValueEqual(w.getDirector().current(MatSlot.BODY), MatAnim.FALL_02, "a blow from the front throws her on her back");
            helper.assertTrue(w.getDirector().incapacitated(), "and she is down");
            Set<MatAnim> recoveries = Set.of(MatAnim.FALL_05, MatAnim.FALL_06, MatAnim.FALL_07, MatAnim.FALL_09);
            waitUntil(helper, 120, () -> recoveries.contains(w.getDirector().current(MatSlot.BODY)), () -> {
                helper.assertTrue(w.getDirector().startedCount(MatAnim.FALL_04) == 1, "she hit the ground once");
                helper.assertTrue(recoveries.contains(w.getDirector().current(MatSlot.BODY)), "then she gets up (" + w.getDirector().describe() + ")");
                waitUntil(helper, 60, () -> w.getDirector().current(MatSlot.BODY) == null, () -> {
                    helper.assertTrue(!w.getDirector().incapacitated(), "back on her feet, she fights again");
                    cleanup(level, w);
                    helper.succeed();
                });
            });
        });
    }

    private static void stageChanges(GameTestHelper helper) {
        // the client: her breathing and the reach of her gestures change with her mood
        helper.assertTrue(MatriarchAnimator.amplitude(MatAnim.THREAD_09, 0.0F) == 1.0F, "composed gestures as authored");
        helper.assertTrue(MatriarchAnimator.amplitude(MatAnim.THREAD_09, 0.9F) > 1.3F, "wider once unravelled");
        Rig calm = new Rig();
        Rig lost = new Rig();
        lost.in.unravel = 0.9F;
        helper.assertTrue(lost.animator.warp(MatAnim.THREAD_09, 4.5F, 0.9F) != 4.5F && calm.animator.warp(MatAnim.THREAD_09, 4.5F, 0.0F) == 4.5F,
                "the hitched rhythm only once she unravels");
        float[] a = null;
        float[] b = null;
        for (int t = 0; t <= 40; t++) {
            a = calm.at(t);
            b = lost.at(t);
        }
        helper.assertTrue(Math.abs(rot(a, MatRig.CHEST, 0) - rot(b, MatRig.CHEST, 0)) > 3.0F, "a different posture");
        helper.assertTrue(Math.abs(rot(a, MatRig.HEAD, 2) - rot(b, MatRig.HEAD, 2)) > 4.0F, "a different tilt of the head");
        helper.assertTrue(MatriarchMood.of(MatriarchMood.unravel(EncounterPhase.STAGE_05, 5, 1.0F)) == MatriarchMood.COMPOSED
                && MatriarchMood.of(MatriarchMood.unravel(EncounterPhase.STAGE_06, 6, 1.0F)) == MatriarchMood.IRRITATED
                && MatriarchMood.of(MatriarchMood.unravel(EncounterPhase.STAGE_11_FURY, 11, 1.0F)) == MatriarchMood.UNRAVELLING, "three stages of behaviour");
        // the server: the fifth puppet's fall plays the first transition, and she rises into the air before the sixth
        withWitch(helper, 40, w -> {
            ServerLevel level = helper.getLevel();
            w.beginDirect(level, 5);
            PuppetEntity p = w.getActivePuppet(level);
            p.hurtServer(level, level.damageSources().genericKill(), 100000.0F);
            waitUntil(helper, MatriarchWitchEntity.REACT_TICKS + 10, () -> w.getDirector().current(MatSlot.BODY) == MatAnim.PHASE_01, () -> {
                helper.assertValueEqual(w.getDirector().current(MatSlot.BODY), MatAnim.PHASE_01, "MAT-PHASE-01 plays");
                helper.assertValueEqual(w.getCurrentStage(), 5, "before the sixth puppet comes");
                waitUntil(helper, MatriarchWitchEntity.PHASE_ONE_TICKS + MatriarchWitchEntity.SUMMON_TICKS + 10, () -> w.getCurrentStage() == 6, () -> {
                    helper.assertValueEqual(w.getCurrentStage(), 6, "the sixth stage begins after the transition");
                    helper.assertTrue(w.isAnimFloating() && w.isNoGravity(), "and she floats");
                    helper.assertValueEqual(w.getDirector().firedCount(MatEvent.PHASE_SURGE), 1, "the surge of the transition fired once");
                    cleanup(level, w);
                    helper.succeed();
                });
            });
        });
    }

    private static void introBeforeCombat(GameTestHelper helper) {
        withWitch(helper, 60, w -> {
            ServerLevel level = helper.getLevel();
            w.startEncounter(level);
            helper.assertValueEqual(w.getDirector().current(MatSlot.BODY), MatAnim.CIN_01, "the entrance starts");
            later(helper, MatriarchWitchEntity.INTRO_TICKS - 6, () -> {
                helper.assertValueEqual(w.getPhase(), EncounterPhase.INTRO, "nothing interrupts it: still the intro");
                helper.assertValueEqual(w.getCurrentStage(), 0, "no puppet fights yet");
                helper.assertValueEqual(w.getDirector().current(MatSlot.BODY), MatAnim.CIN_03, "it went through the presentation to the first show of power");
                helper.assertTrue(!w.hurtServer(level, level.damageSources().generic(), 50.0F), "and she cannot be hurt during it");
                waitUntil(helper, 20, () -> w.getPhase() == EncounterPhase.STAGE_01, () -> {
                    helper.assertValueEqual(w.getPhase(), EncounterPhase.STAGE_01, "the fight starts only once it is over");
                    helper.assertValueEqual(w.getDirector().firedCount(MatEvent.INTRO_THREADS), 1, "the threads rose once");
                    helper.assertValueEqual(w.getDirector().firedCount(MatEvent.POWER_BURST), 1, "the power burst once");
                    cleanup(level, w);
                    helper.succeed();
                });
            });
        });
    }

    private static void deathSequence(GameTestHelper helper) {
        // the client: the dissolve takes the hands first and the smile last
        Rig r = new Rig();
        r.in.unravel = 1.0F;
        r.play(MatSlot.BODY, MatAnim.CIN_05, 0, 0);
        r.play(MatSlot.BODY, MatAnim.CIN_06, 0, MatAnim.CIN_05.ticks);
        var model = new MatriarchModel(TenThreadsParts.layer(MatRig.ID).bakeRoot());
        var state = new MatriarchRenderState();
        state.blindfoldOff = true;
        boolean handsBeforeFace = false;
        boolean smileLast = false;
        float smile = 0.0F;
        for (int t = 0; t <= MatriarchWitchEntity.DEATH_TICKS; t++) {
            float[] p = r.at(t);
            System.arraycopy(p, 0, state.pose, 0, p.length);
            model.setupAnim(state);
            if (model.part(MatRig.HAND_L).skipDraw && !model.part(MatRig.HEAD).skipDraw && !model.part(MatRig.MOUTH).skipDraw) {
                handsBeforeFace = true;
            }
            if (model.part(MatRig.HEAD).skipDraw && !model.part(MatRig.MOUTH).skipDraw) {
                smileLast = true;
                smile = rot(p, MatRig.index("mouth_l"), 2);
            }
        }
        helper.assertTrue(handsBeforeFace, "the hands vanish while the face is still there");
        helper.assertTrue(smileLast && smile < -30.0F, "the smile is the last thing left (" + smile + ")");
        helper.assertTrue(model.part(MatRig.MOUTH).skipDraw && model.part(MatRig.BODY).skipDraw, "in the end nothing remains");
        // the server: the cinematic plays to its end before she is removed; the threads snap, the body dissolves, she vanishes once
        withWitch(helper, 80, w -> {
            ServerLevel level = helper.getLevel();
            w.beginDirect(level, 11);
            w.hurtServer(level, level.damageSources().genericKill(), 100000.0F);
            helper.assertValueEqual(w.getDirector().current(MatSlot.BODY), MatAnim.CIN_05, "the defeat begins");
            later(helper, MatriarchWitchEntity.DEATH_TICKS - 4, () -> {
                helper.assertTrue(!w.isRemoved(), "not removed before the cinematic ends");
                helper.assertValueEqual(w.getDirector().current(MatSlot.BODY), MatAnim.CIN_06, "the final vanishing is playing");
                waitUntil(helper, 20, w::isRemoved, () -> {
                    helper.assertTrue(w.isRemoved(), "removed once it is over");
                    helper.assertTrue(w.getDirector().firedCount(MatEvent.THREAD_SNAP) >= 3, "the threads snapped (" + w.getDirector().firedCount(MatEvent.THREAD_SNAP) + ")");
                    helper.assertTrue(w.getDirector().firedCount(MatEvent.DISINTEGRATE) >= 6, "the body came apart in steps");
                    helper.assertValueEqual(w.getDirector().firedCount(MatEvent.VANISH), 1, "she vanished once");
                    cleanup(level, w);
                    helper.succeed();
                });
            });
        });
    }

    // ------------------------------------------------------------------------------------------------ rules of the director
    private static void eventsOnce(GameTestHelper helper) {
        withWitch(helper, 100, w -> {
            ServerLevel level = helper.getLevel();
            helper.assertTrue(w.getDirector().play(MatAnim.IDLE_10_CLAP), "the clap plays");
            later(helper, MatAnim.IDLE_10_CLAP.ticks + 10, () -> {
                helper.assertValueEqual(w.getDirector().firedCount(MatEvent.CLAP), 3, "three claps, each heard once");
                helper.assertTrue(w.getDirector().current(MatSlot.BODY) == null, "the taunt ends by itself");
                cleanup(level, w);
                helper.succeed();
            });
        });
    }

    private static void priorityRules(GameTestHelper helper) {
        withWitch(helper, 120, w -> {
            ServerLevel level = helper.getLevel();
            var d = w.getDirector();
            helper.assertTrue(d.play(MatAnim.IDLE_02), "an idle plays");
            helper.assertTrue(d.play(MatAnim.IDLE_09), "an idle yields to anything");
            helper.assertTrue(d.play(MatAnim.FALL_04), "a fall takes the body");
            helper.assertTrue(!d.play(MatAnim.DEF_01), "a weaker action cannot interrupt a fall");
            helper.assertTrue(d.play(MatAnim.PHASE_02), "a stronger one can");
            d.stop(MatSlot.BODY);
            helper.assertTrue(d.play(MatAnim.ATK_09), "a two-hand attack");
            helper.assertTrue(!d.play(MatAnim.THREAD_09), "a thread for a hand the attack holds has to wait");
            helper.assertTrue(d.current(MatSlot.HAND_L) == null, "so it does not start yet");
            later(helper, MatAnim.ATK_09.ticks + 3, () -> {
                helper.assertValueEqual(d.current(MatSlot.HAND_L), MatAnim.THREAD_09, "and starts once the attack lets go of the arm");
                cleanup(level, w);
                helper.succeed();
            });
        });
    }

    private static void comboBranches(GameTestHelper helper) {
        withWitch(helper, 140, w -> {
            ServerLevel level = helper.getLevel();
            IronGolem g = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(140 + 11, Y + 1, 9));
            g.setPos(w.position().add(2.0D, 0.0D, 0.0D));
            var b = MatriarchMoveset.branch("thimble_flick", w, g, 2.0D);
            helper.assertTrue(b != null && b.to() == MatriarchMoveset.SEAM_RIP, "close: the flick goes on into the seam rip");
            g.setPos(w.position().add(5.0D, 0.0D, 0.0D));
            g.setDeltaMovement(0.3D, 0.0D, 0.0D);
            b = MatriarchMoveset.branch("thimble_flick", w, g, 5.0D);
            helper.assertTrue(b != null && b.to() == MatriarchMoveset.PHANTOM_ECHO, "retreating: the whip reaches after it");
            g.setDeltaMovement(Vec3.ZERO);
            helper.assertTrue(MatriarchMoveset.branch("thimble_flick", w, g, 10.0D) == null, "far and still: the combo ends");
            b = MatriarchMoveset.branch("thread_lance", w, g, 10.0D);
            helper.assertTrue(b != null && b.to() == MatriarchMoveset.NEEDLE_VOLLEY, "the lance at range chains into the volley");
            g.discard();
            cleanup(level, w);
            helper.succeed();
        });
    }

    private static void furyRest(GameTestHelper helper) {
        withWitch(helper, 160, w -> {
            ServerLevel level = helper.getLevel();
            w.beginDirect(level, 11);
            int full = w.furyRestTicks();
            helper.assertTrue(full <= MatriarchWitchEntity.FURY_REST_CALM && full > MatriarchWitchEntity.FURY_REST_WILD, "a long breath at full health (" + full + ")");
            IronGolem g = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(160 + 11, Y + 1, 9));
            g.setPos(w.position().add(2.0D, 0.0D, 0.0D));
            g.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000.0D);
            g.setHealth(1000.0F);
            w.setTarget(g);
            waitUntil(helper, 300, () -> w.getFuryRest() > 0, () -> {
                helper.assertTrue(w.getRunner().swings() >= 1 && w.getFuryRest() > 0, "after a sequence of blows she takes a breath (swings " + w.getRunner().swings() + ")");
                helper.assertTrue(!w.getRunner().isBusy(), "and does not attack during it");
                w.setHealth(5.0F);
                helper.assertTrue(w.furyRestTicks() < full && w.furyRestTicks() >= MatriarchWitchEntity.FURY_REST_WILD, "near death the breath is shorter (" + w.furyRestTicks() + ")");
                g.discard();
                cleanup(level, w);
                helper.succeed();
            });
        });
    }

    private static void noLock(GameTestHelper helper) {
        withWitch(helper, 180, w -> {
            ServerLevel level = helper.getLevel();
            w.beginDirect(level, 11);
            var d = w.getDirector();
            helper.assertTrue(d.onHurt(level.damageSources().generic(), 14.0F), "the first heavy blow knocks her down");
            Set<MatAnim> recoveries = Set.of(MatAnim.FALL_05, MatAnim.FALL_06, MatAnim.FALL_07, MatAnim.FALL_09);
            waitUntil(helper, 200, () -> d.startedCount(MatAnim.FALL_04) == 1 && d.current(MatSlot.BODY) == null, () -> {
                helper.assertTrue(recoveries.stream().anyMatch(r -> d.startedCount(r) == 1), "she got up");
                helper.assertTrue(d.onHurt(level.damageSources().generic(), 14.0F), "a second heavy blow soon after still breaks her action");
                MatAnim body = d.current(MatSlot.BODY);
                helper.assertTrue(body == MatAnim.STUN_02 || body == MatAnim.STUN_03, "but only staggers her: no second knockdown so soon (" + body + ")");
                helper.assertTrue(!d.onHurt(level.damageSources().generic(), 14.0F), "a third one right away cannot stun her again");
                helper.assertTrue(d.current(MatSlot.REACT) != null, "it is only a reaction on top");
                helper.assertValueEqual(d.startedCount(MatAnim.FALL_04), 1, "still only one fall");
                cleanup(level, w);
                helper.succeed();
            });
        });
    }
}
