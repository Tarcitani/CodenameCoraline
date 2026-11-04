package com.codenamecoraline.client.crimson;

import com.codenamecoraline.entity.crimson.CrimsonAnim;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keyframe animations of the Crimson Commander, written as sequences of whole-body poses.
 *
 * <p>A pose lists ABSOLUTE rotations in degrees per part ({@code "chest 20 0 0"}) and position offsets in model pixels
 * ({@code "@pelvis 0 3 0"}); parts left out stay in the resting stance {@link #BASE}. The builder converts each frame to
 * the deltas {@code KeyframeAnimation} adds on top of the model's built-in stance.</p>
 *
 * <p>The greatsword points along -z from the fist. While only x rotations are involved, its angle S is the sum of the x
 * rotations of chest, arm, forearm and hand: S = 0 points the blade forward, -90 straight up, +90 straight down, -180
 * backwards. A hand x rotation of +90 puts the blade in line with the forearm. A ModelPart rotates in Z·Y·X order (x
 * first), so a hand y rotation rolls the blade about the forearm. Reference poses (validated by offline renders, see
 * docs/design/crimson_commander.md):</p>
 * <ul>
 *   <li>SHOULDER (Combat Idle Stance): chest 6, arm -25, forearm -95, hand -31 (S -145): blade resting on the right
 *       pauldron, pointing up and back.</li>
 *   <li>THRUST (Dash Thrust / Lunge): chest 28, arm -80, forearm -8, hand 72 (S 12): blade level with the target.</li>
 *   <li>OVERHEAD (Heavy Overhead Slam, wind-up): both arms up, S -205: blade hanging behind the head.</li>
 *   <li>IMPACT: chest 34, arm -70, forearm -6, hand 112 (S 70): blade driven into the ground in front.</li>
 *   <li>GUARD (Defensive Guard): forearm level in front of the chest (S -90) and the wrist rolled 40 degrees: the blade
 *       crosses the torso diagonally, point up and to his left.</li>
 *   <li>SLASH: arm raised forward (x -90) and swung by its y rotation, hand 85: arm and blade form one long lever that
 *       sweeps horizontally from +95 (behind his right) to -60 (past his left).</li>
 * </ul>
 */
public final class CrimsonCommanderAnimations {
    /** Resting stance (absolute degrees); the model bakes exactly these into its parts. */
    public static final Map<String, float[]> BASE = new HashMap<>();

    static {
        BASE.put("chest", new float[] {4, 0, 0});
        BASE.put("head", new float[] {-4, 0, 0});
        BASE.put("right_arm", new float[] {0, 0, 6});
        BASE.put("left_arm", new float[] {0, 0, -6});
        BASE.put("right_forearm", new float[] {-10, 0, 0});
        BASE.put("left_forearm", new float[] {-10, 0, 0});
        BASE.put("right_thigh", new float[] {0, 0, 3});
        BASE.put("left_thigh", new float[] {0, 0, -3});
        BASE.put("right_foot", new float[] {0, 0, -3});
        BASE.put("left_foot", new float[] {0, 0, 3});
        BASE.put("cape", new float[] {8, 0, 0});
        BASE.put("plume", new float[] {-6, 0, 0});
        BASE.put("plume_tail", new float[] {58, 0, 0});
        BASE.put("horn_l", new float[] {-10, 0, 55});
        BASE.put("horn_r", new float[] {-10, 0, -55});
        BASE.put("r_spike_a", new float[] {15, 0, -30});
        BASE.put("r_spike_b", new float[] {0, 0, -45});
        BASE.put("r_spike_c", new float[] {-20, 0, -30});
        BASE.put("l_spike_a", new float[] {15, 0, 30});
        BASE.put("l_spike_b", new float[] {0, 0, 45});
        BASE.put("l_spike_c", new float[] {-20, 0, 30});
    }

    private static final AnimationChannel.Interpolation SMOOTH = AnimationChannel.Interpolations.CATMULLROM;
    private static final AnimationChannel.Interpolation LINEAR = AnimationChannel.Interpolations.LINEAR;

    private static final Map<CrimsonAnim, AnimationDefinition> DEFINITIONS = new EnumMap<>(CrimsonAnim.class);

    // ------------------------------------------------------------------------------------------------ poses
    private record Pose(Map<String, float[]> rot, Map<String, float[]> pos) {
        static Pose of(String... specs) {
            return new Pose(new HashMap<>(), new HashMap<>()).with(specs);
        }

        Pose with(String... specs) {
            Pose p = new Pose(new HashMap<>(rot), new HashMap<>(pos));
            for (String spec : specs) {
                String[] t = spec.trim().split("\\s+");
                boolean position = t[0].startsWith("@");
                String part = position ? t[0].substring(1) : t[0];
                float[] v = {Float.parseFloat(t[1]), Float.parseFloat(t[2]), Float.parseFloat(t[3])};
                (position ? p.pos : p.rot).put(part, v);
            }
            return p;
        }
    }

    /** Legs planted a little apart: the duelist's base. */
    private static final String[] STANCE = {"right_thigh -6 0 7", "left_thigh 6 0 -7", "right_shin 6 0 0", "left_shin 4 0 0",
            "right_foot 0 0 -7", "left_foot -10 0 7"};
    /** Combat Idle Stance: the greatsword resting on the right shoulder. */
    private static final Pose SHOULDER = Pose.of("chest 6 0 0", "head -6 0 0", "right_arm -25 0 14", "right_forearm -95 0 0",
            "right_hand -31 0 0", "left_arm 8 0 -10", "left_forearm -18 0 0").with(STANCE);
    /** Guard: legs braced wide, the blade across the torso, point up and to his left. */
    private static final Pose GUARD = Pose.of("chest 12 -10 0", "head -10 8 0", "@pelvis 0 2 0",
            "right_arm -50 -20 10", "right_forearm -52 0 0", "right_hand 0 -40 0",
            "left_arm -64 0 -12", "left_forearm -66 0 0", "left_hand 0 0 0",
            "right_thigh -22 0 14", "left_thigh 18 0 -12", "right_shin 24 0 0", "left_shin 12 0 0",
            "right_foot -2 0 -14", "left_foot -30 0 12", "cape 16 0 0");
    /** Dash Thrust lunge: leaning in, the right arm and the blade level with the target. */
    private static final Pose THRUST = Pose.of("chest 28 -10 0", "head -24 10 0", "@pelvis 0 4 -2",
            "right_arm -80 0 4", "right_forearm -8 0 0", "right_hand 72 0 0",
            "left_arm 40 0 -22", "left_forearm -20 0 0",
            "right_thigh -55 0 4", "right_shin 50 0 0", "right_foot 5 0 0", "left_thigh 30 0 -4", "left_shin 18 0 0",
            "left_foot 22 0 0", "cape 30 0 0", "plume 0 0 0");
    /** Overhead wind-up of the Heavy Slam: both hands on the grip above the head, the blade hanging behind it. */
    private static final Pose OVERHEAD = Pose.of("chest -12 0 0", "head -12 0 0", "right_arm -165 0 26", "right_forearm -15 0 0",
            "right_hand -13 0 0", "left_arm -165 0 -26", "left_forearm -20 0 0");
    /** The blade driven into the ground in front, both hands on the grip. */
    private static final Pose IMPACT = Pose.of("chest 34 0 0", "head -22 0 0", "@pelvis 0 7 -1",
            "right_arm -70 -28 0", "right_forearm -6 0 0", "right_hand 112 0 0",
            "left_arm -72 28 0", "left_forearm -8 0 0",
            "right_thigh -72 0 8", "right_shin 82 0 0", "right_foot -10 0 0",
            "left_thigh 22 0 -6", "left_shin 64 0 0", "left_foot 2 0 0", "cape 30 0 0");
    /** Horizontal slash: wind-up (the arm-and-blade lever far behind his right) and the end of the sweep (past his left). */
    private static final Pose SLASH_WIND = Pose.of("chest 4 40 0", "head -4 -34 0", "right_arm -90 55 0", "right_forearm -10 0 0",
            "right_hand 85 0 0", "left_arm -40 -20 -16", "left_forearm -40 0 0",
            "right_thigh 12 0 8", "left_thigh -24 0 -6", "left_shin 22 0 0", "right_shin 4 0 0", "cape 14 0 0");
    private static final Pose SLASH_END = Pose.of("chest 10 -40 0", "head -6 32 0", "right_arm -90 -20 0", "right_forearm -10 0 0",
            "right_hand 85 0 0", "left_arm 30 30 -24", "left_forearm -30 0 0",
            "right_thigh -30 0 8", "right_shin 26 0 0", "left_thigh 14 0 -8", "left_shin 6 0 0", "cape 20 0 0");
    /** Kneeling on the right knee, both hands on the pommel of the planted greatsword. */
    private static final Pose KNEEL = Pose.of("chest 14 0 0", "head 18 0 0", "@pelvis 0 10 0",
            "right_arm -48 0 -8", "right_forearm -38 0 0", "right_hand 176 0 0",
            "left_arm -50 0 14", "left_forearm -40 0 0",
            "left_thigh -84 0 -6", "left_shin 86 0 0", "left_foot -2 0 0",
            "right_thigh 6 0 6", "right_shin 88 0 0", "right_foot -4 0 0", "cape 0 0 0");
    /** Ready stance of the fury: the blade held low and forward, the body coiled. */
    private static final Pose FURY = Pose.of("chest 18 0 0", "head -16 0 0", "@pelvis 0 2 0", "right_arm -40 0 14", "right_forearm -30 0 0",
            "right_hand 98 0 0", "left_arm -20 0 -24", "left_forearm -40 0 0").with(STANCE)
            .with("right_thigh -14 0 8", "left_thigh 12 0 -8", "right_shin 18 0 0", "left_shin 14 0 0");

    private static Object[] f(float t, Pose p) {
        return new Object[] {t, p};
    }

    // ------------------------------------------------------------------------------------------------ builder
    private static void def(CrimsonAnim anim, float length, boolean smooth, Object[]... frames) {
        List<Float> times = new ArrayList<>();
        List<Pose> poses = new ArrayList<>();
        for (Object[] fr : frames) {
            times.add((Float) fr[0]);
            poses.add((Pose) fr[1]);
        }
        if (anim.loops && times.get(times.size() - 1) < length - 1.0E-4F) { // loops close on the first pose
            times.add(length);
            poses.add(poses.get(0));
        }
        AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(length);
        if (anim.loops) {
            b.looping();
        }
        Set<String> rotParts = new LinkedHashSet<>();
        Set<String> posParts = new LinkedHashSet<>();
        for (Pose p : poses) {
            rotParts.addAll(p.rot().keySet());
            posParts.addAll(p.pos().keySet());
        }
        AnimationChannel.Interpolation interp = smooth ? SMOOTH : LINEAR;
        for (String part : rotParts) {
            float[] base = BASE.getOrDefault(part, new float[] {0, 0, 0});
            Keyframe[] kfs = new Keyframe[times.size()];
            for (int i = 0; i < kfs.length; i++) {
                float[] abs = poses.get(i).rot().getOrDefault(part, base);
                kfs[i] = new Keyframe(times.get(i),
                        KeyframeAnimations.degreeVec(abs[0] - base[0], abs[1] - base[1], abs[2] - base[2]), interp);
            }
            b.addAnimation(part, new AnimationChannel(AnimationChannel.Targets.ROTATION, kfs));
        }
        for (String part : posParts) {
            Keyframe[] kfs = new Keyframe[times.size()];
            for (int i = 0; i < kfs.length; i++) {
                float[] v = poses.get(i).pos().getOrDefault(part, new float[] {0, 0, 0});
                kfs[i] = new Keyframe(times.get(i), KeyframeAnimations.posVec(v[0], v[1], v[2]), interp);
            }
            b.addAnimation(part, new AnimationChannel(AnimationChannel.Targets.POSITION, kfs));
        }
        DEFINITIONS.put(anim, b.build());
    }

    public static AnimationDefinition get(CrimsonAnim anim) {
        return DEFINITIONS.get(anim);
    }

    private CrimsonCommanderAnimations() {}

    // ------------------------------------------------------------------------------------------------ the catalogue
    static {
        // ===== A. stance and movement
        Pose in = SHOULDER.with("chest 8 0 0", "head -7 0 0", "left_arm 10 0 -11");
        def(CrimsonAnim.COMBAT_IDLE, 3.0f, true, f(0f, SHOULDER), f(1.5f, in));
        def(CrimsonAnim.IDLE_LOOK, 3.0f, true, f(0f, SHOULDER), f(0.8f, SHOULDER.with("head -6 34 0", "chest 6 8 0")),
                f(1.8f, SHOULDER.with("head -6 -34 0", "chest 6 -8 0")), f(2.6f, SHOULDER));
        Pose wl = SHOULDER.with("right_thigh -26 0 4", "right_shin 14 0 0", "left_thigh 18 0 -4", "left_shin 20 0 0",
                "left_foot 6 0 4", "left_arm -18 0 -10", "chest 7 -4 0");
        Pose wr = SHOULDER.with("left_thigh -26 0 -4", "left_shin 14 0 0", "right_thigh 18 0 4", "right_shin 20 0 0",
                "right_foot 6 0 -4", "left_arm 22 0 -10", "chest 7 4 0");
        def(CrimsonAnim.WALK, 1.2f, true, f(0f, wl), f(0.3f, wl.with("@pelvis 0 -1 0", "right_thigh -8 0 4", "left_thigh 2 0 -4")),
                f(0.6f, wr), f(0.9f, wr.with("@pelvis 0 -1 0", "left_thigh -8 0 -4", "right_thigh 2 0 4")));
        Pose run = Pose.of("chest 16 0 0", "head -16 0 0", "right_arm 20 0 14", "right_forearm -10 0 0", "right_hand 124 0 0",
                "cape 34 0 0", "plume 4 0 0");
        Pose rl = run.with("right_thigh -50 0 4", "right_shin 30 0 0", "left_thigh 30 0 -4", "left_shin 40 0 0", "left_foot 14 0 0",
                "left_arm -40 0 -12", "left_forearm -40 0 0", "chest 16 -6 0");
        Pose rr = run.with("left_thigh -50 0 -4", "left_shin 30 0 0", "right_thigh 30 0 4", "right_shin 40 0 0", "right_foot 14 0 0",
                "left_arm 34 0 -12", "left_forearm -20 0 0", "chest 16 6 0");
        def(CrimsonAnim.RUN, 0.7f, true, f(0f, rl), f(0.175f, rl.with("@pelvis 0 -2 0", "right_thigh -20 0 4", "left_thigh 4 0 -4")),
                f(0.35f, rr), f(0.525f, rr.with("@pelvis 0 -2 0", "left_thigh -20 0 -4", "right_thigh 4 0 4")));
        Pose crouch = SHOULDER.with("@pelvis 0 5 0", "chest 22 0 0", "right_thigh -50 0 7", "left_thigh -50 0 -7", "right_shin 80 0 0",
                "left_shin 80 0 0", "right_foot -30 0 -7", "left_foot -30 0 7", "left_arm 30 0 -14");
        Pose up = SHOULDER.with("@pelvis 0 -1 0", "chest 2 0 0", "right_thigh -40 0 7", "left_thigh -10 0 -7", "right_shin 60 0 0",
                "left_shin 30 0 0", "left_arm -40 0 -24", "cape 30 0 0");
        def(CrimsonAnim.STEP_JUMP, 0.8f, true, f(0f, crouch), f(0.2f, up), f(0.55f, up), f(0.8f, SHOULDER));

        // ===== B. crimson_slash (prep 12 ticks, execute 6, recover 14)
        def(CrimsonAnim.SLASH_PREPARE, 0.6f, true, f(0f, SHOULDER), f(0.25f, SLASH_WIND.with("chest 4 30 0", "right_arm -90 40 0")),
                f(0.6f, SLASH_WIND));
        Pose slashMid = SLASH_WIND.with("chest 8 0 0", "head -6 0 0", "right_arm -90 15 0", "left_arm 0 0 -20", "right_thigh -10 0 8",
                "left_thigh -6 0 -6");
        def(CrimsonAnim.SLASH_EXECUTE, 0.3f, false, f(0f, SLASH_WIND), f(0.15f, slashMid), f(0.3f, SLASH_END));
        def(CrimsonAnim.SLASH_RECOVERY, 0.7f, true, f(0f, SLASH_END), f(0.3f, SLASH_END.with("chest 12 -30 0", "right_arm -70 -10 0")),
                f(0.7f, SHOULDER));

        // ===== C. dash_thrust (prep 16, execute 8, recover 18 or 24)
        Pose aim = THRUST.with("chest 18 -14 0", "@pelvis 0 3 2", "right_arm -60 0 8", "right_forearm -40 0 0", "right_hand 102 0 0",
                "right_thigh -30 0 6", "right_shin 40 0 0", "left_thigh 20 0 -4", "left_shin 30 0 0");
        def(CrimsonAnim.THRUST_PREPARE, 0.8f, true, f(0f, SHOULDER), f(0.35f, aim.with("right_hand 70 0 0")), f(0.8f, aim));
        def(CrimsonAnim.THRUST_EXECUTE, 0.4f, false, f(0f, aim), f(0.1f, THRUST), f(0.4f, THRUST.with("@pelvis 0 4 -3")));
        def(CrimsonAnim.THRUST_RECOVERY, 0.9f, true, f(0f, THRUST), f(0.4f, THRUST.with("chest 14 -6 0", "right_arm -50 0 8",
                "right_hand 60 0 0", "@pelvis 0 2 0")), f(0.9f, SHOULDER));
        Pose skid = THRUST.with("chest 36 -14 0", "@pelvis 0 6 -1", "right_arm -40 0 10", "right_hand 120 0 0", "left_arm -30 0 -40",
                "left_forearm -40 0 0", "right_thigh -70 0 6", "right_shin 70 0 0", "left_thigh 40 0 -6", "left_shin 40 0 0");
        def(CrimsonAnim.THRUST_RECOVERY_MISS, 1.2f, true, f(0f, THRUST), f(0.2f, skid), f(0.7f, skid.with("chest 28 -4 0")),
                f(1.2f, SHOULDER));

        // ===== D. heavy_overhead_slam (prep 22 with the jump at tick 14, execute 10, recover 26)
        Pose load = OVERHEAD.with("@pelvis 0 5 0", "chest 4 0 0", "right_thigh -50 0 7", "left_thigh -50 0 -7", "right_shin 80 0 0",
                "left_shin 80 0 0", "right_foot -30 0 -7", "left_foot -30 0 7");
        Pose apex = OVERHEAD.with("chest -16 0 0", "right_thigh -40 0 7", "left_thigh -14 0 -7", "right_shin 60 0 0", "left_shin 36 0 0",
                "right_foot 10 0 0", "left_foot 14 0 0", "cape 40 0 0", "plume 20 0 0");
        def(CrimsonAnim.SLAM_PREPARE, 1.1f, true, f(0f, SHOULDER), f(0.4f, OVERHEAD), f(0.65f, load), f(0.75f, apex), f(1.1f, apex));
        Pose fall = OVERHEAD.with("chest 4 0 0", "right_arm -120 0 20", "left_arm -120 0 -20", "right_hand 30 0 0", "cape 50 0 0",
                "right_thigh -50 0 7", "left_thigh -30 0 -7", "right_shin 50 0 0", "left_shin 40 0 0");
        def(CrimsonAnim.SLAM_EXECUTE, 0.5f, false, f(0f, apex), f(0.2f, fall), f(0.3f, IMPACT), f(0.5f, IMPACT));
        Pose pull = IMPACT.with("chest 20 0 0", "@pelvis 0 4 0", "right_arm -84 -26 0", "left_arm -86 26 0", "right_hand 120 0 0",
                "right_thigh -50 0 8", "right_shin 60 0 0", "left_shin 40 0 0");
        def(CrimsonAnim.SLAM_RECOVERY, 1.3f, true, f(0f, IMPACT), f(0.5f, IMPACT.with("chest 30 0 0")), f(0.85f, pull),
                f(1.3f, SHOULDER));

        // ===== E. defensive_guard (prep 6, hold 30, recover 10) and the riposte
        def(CrimsonAnim.GUARD_PREPARE, 0.3f, true, f(0f, SHOULDER), f(0.3f, GUARD));
        def(CrimsonAnim.GUARD_HOLD, 1.0f, true, f(0f, GUARD), f(0.5f, GUARD.with("chest 13 -10 0", "@pelvis 0 2.5 0")));
        def(CrimsonAnim.GUARD_BLOCK, 0.25f, false, f(0f, GUARD), f(0.08f, GUARD.with("chest 4 -10 0", "@pelvis 0 1 1.5",
                "right_forearm -60 0 0")), f(0.25f, GUARD));
        def(CrimsonAnim.GUARD_RECOVERY, 0.5f, true, f(0f, GUARD), f(0.5f, SHOULDER));
        Pose ripWind = SLASH_WIND.with("chest 10 30 0", "right_arm -80 50 0");
        def(CrimsonAnim.RIPOSTE, 0.5f, false, f(0f, GUARD), f(0.08f, ripWind), f(0.2f, SLASH_END.with("@pelvis 0 2 -2")),
                f(0.5f, SLASH_END));

        // ===== F. blood_crescent_wave (prep 18, execute 8, recover 16)
        Pose charge = Pose.of("chest 0 0 0", "head -10 0 0", "right_arm -100 0 10", "right_forearm -40 0 0", "right_hand 50 0 0",
                "left_arm -86 0 -20", "left_forearm -54 0 0").with(STANCE);
        Pose low = Pose.of("chest 12 34 0", "head -10 -30 0", "right_arm 24 30 30", "right_forearm -16 0 0", "right_hand 96 0 0",
                "left_arm -50 -20 -20", "left_forearm -40 0 0").with(STANCE).with("right_thigh 10 0 8", "left_thigh -22 0 -6",
                "left_shin 20 0 0");
        Pose rise = Pose.of("chest -6 -30 0", "head -12 26 0", "right_arm -160 -30 -24", "right_forearm -10 0 0", "right_hand 70 0 0",
                "left_arm 20 30 -20", "left_forearm -20 0 0").with(STANCE).with("right_thigh -24 0 8", "right_shin 20 0 0",
                "left_thigh 14 0 -6", "cape 26 0 0");
        def(CrimsonAnim.WAVE_PREPARE, 0.9f, true, f(0f, SHOULDER), f(0.3f, charge), f(0.55f, charge.with("head -14 0 0")),
                f(0.9f, low));
        def(CrimsonAnim.WAVE_EXECUTE, 0.4f, false, f(0f, low), f(0.15f, rise.with("right_arm -80 0 -10", "chest 4 0 0")),
                f(0.25f, rise), f(0.4f, rise));
        def(CrimsonAnim.WAVE_RECOVERY, 0.8f, true, f(0f, rise), f(0.8f, SHOULDER));

        // ===== G. reactions (overlays: hurt; full: cancel)
        def(CrimsonAnim.HURT_FRONT, 0.4f, true, f(0f, Pose.of()), f(0.08f, Pose.of("chest -4 0 0", "head -14 0 0", "@pelvis 0 0 1.5")),
                f(0.4f, Pose.of()));
        def(CrimsonAnim.HURT_BACK, 0.4f, true, f(0f, Pose.of()), f(0.08f, Pose.of("chest 16 0 0", "head 6 0 0", "@pelvis 0 0 -1.5")),
                f(0.4f, Pose.of()));
        def(CrimsonAnim.ATTACK_CANCEL, 0.5f, true, f(0f, SHOULDER.with("chest -2 0 0", "head -12 0 0", "@pelvis 0 0 1")),
                f(0.5f, SHOULDER));

        // ===== H. stages
        Pose raise = Pose.of("chest -6 0 0", "head -24 0 0", "right_arm -175 0 -8", "right_forearm -5 0 0", "right_hand 90 0 0",
                "left_arm 10 0 -24", "left_forearm -30 0 0", "cape 20 0 0").with(STANCE);
        def(CrimsonAnim.AWAKEN_TRANSITION, 1.5f, true, f(0f, SHOULDER), f(0.45f, raise), f(1.1f, raise.with("head -30 0 0")),
                f(1.5f, SHOULDER));
        Pose plant = IMPACT.with("right_hand 132 0 0", "head -40 0 0", "chest 26 0 0");
        def(CrimsonAnim.FURY_TRANSITION, 2.0f, true, f(0f, SHOULDER), f(0.5f, OVERHEAD), f(0.8f, plant), f(1.6f, plant.with("head -46 0 0")),
                f(2.0f, FURY));
        def(CrimsonAnim.FURY_IDLE, 1.6f, true, f(0f, FURY), f(0.8f, FURY.with("chest 21 0 0", "head -14 0 0", "@pelvis 0 2.6 0")));

        Pose heal = IMPACT.with("chest 20 0 0", "head 22 0 0", "left_arm -60 20 -30", "left_forearm -110 0 0", "@pelvis 0 6 0");
        def(CrimsonAnim.HEAL_CHANNEL, 1.0f, true, f(0f, heal), f(0.5f, heal.with("chest 25 0 0", "@pelvis 0 7 0", "head 26 0 0")));

        // ===== I. arrival and death
        def(CrimsonAnim.INTRO_KNEEL, 2.0f, true, f(0f, KNEEL), f(1.0f, KNEEL.with("chest 16 0 0", "head 20 0 0")));
        Pose risen = Pose.of("chest 2 0 0", "head -6 0 0", "right_arm -60 0 8", "right_forearm -60 0 0", "right_hand 30 0 0",
                "left_arm -10 0 -10").with(STANCE);
        def(CrimsonAnim.INTRO_RISE, 2.0f, true, f(0f, KNEEL), f(0.6f, KNEEL.with("@pelvis 0 4 0", "left_thigh -40 0 -6", "left_shin 40 0 0",
                "right_thigh -30 0 6", "right_shin 50 0 0", "chest 6 0 0", "head 0 0 0")), f(1.1f, risen), f(1.6f, risen), f(2.0f, SHOULDER));
        Pose slump = KNEEL.with("chest 30 0 0", "head 34 0 0", "left_arm 10 0 -14", "left_forearm -10 0 0");
        def(CrimsonAnim.DEATH_KNEEL, 1.5f, true, f(0f, SHOULDER), f(0.5f, KNEEL.with("@pelvis 0 6 0")), f(1.0f, KNEEL), f(1.5f, slump));
        def(CrimsonAnim.DEATH_FINAL, 2.0f, true, f(0f, slump), f(1.0f, slump.with("chest 32 0 0", "head 36 0 0")));

        // ===== J. the vanguards
        Pose ash = KNEEL.with("@pelvis 0 14 0", "chest 50 0 0", "head 30 0 0", "right_arm 20 0 6", "right_hand 60 0 0",
                "left_arm 20 0 -6");
        def(CrimsonAnim.VANGUARD_RISE, 1.0f, true, f(0f, ash), f(0.6f, KNEEL), f(1.0f, SHOULDER));
        def(CrimsonAnim.VANGUARD_CRUMBLE, 1.0f, true, f(0f, SHOULDER), f(0.4f, KNEEL), f(1.0f, ash));
    }
}
