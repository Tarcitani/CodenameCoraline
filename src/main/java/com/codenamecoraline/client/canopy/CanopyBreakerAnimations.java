package com.codenamecoraline.client.canopy;

import com.codenamecoraline.entity.canopy.CanopyAnim;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * Keyframe animations of the Canopy Breaker, written as sequences of whole-body poses.
 *
 * <p>A pose lists ABSOLUTE rotations in degrees per part ({@code "chest 20 0 0"}) and position offsets in model pixels
 * ({@code "@pelvis 0 3 0"}); parts left out stay in the resting stance {@link #BASE}. The builder converts each frame
 * to the deltas {@code KeyframeAnimation} adds on top of the model's built-in stance. Reference club poses (x rotations:
 * chest + shoulder + forearm + wrist = alpha, the angle of the club haft, 0 = pointing up, +90 = pointing forward):</p>
 * <ul>
 *   <li>CARRY: chest 14, shoulder -62, forearm -45, wrist 58 (alpha -35): club over the right shoulder.</li>
 *   <li>WIND: chest -4, shoulder -150, forearm -22, wrist 121 (alpha -55): club high behind the head.</li>
 *   <li>MID: chest 8, shoulder -104, forearm -16, wrist 153 (alpha 41): club in front, passing the horizontal.</li>
 *   <li>IMPACT: chest 36, shoulder -56, forearm -10, wrist 158 (alpha 128): club head driven down into the ground.</li>
 *   <li>GROUND: chest 24, shoulder -22, forearm -10, wrist 173 (alpha 165): club head dragging on the floor.</li>
 * </ul>
 */
public final class CanopyBreakerAnimations {
    /** Resting stance (absolute degrees); the model bakes exactly these into its parts. */
    public static final Map<String, float[]> BASE = new HashMap<>();

    static {
        BASE.put("chest", new float[] {14, 0, 0});
        BASE.put("head", new float[] {-12, 0, 0});
        BASE.put("left_upper_arm", new float[] {14, 0, -5});
        BASE.put("right_upper_arm", new float[] {14, 0, 5});
        BASE.put("left_forearm", new float[] {-16, 0, 0});
        BASE.put("right_forearm", new float[] {-16, 0, 0});
        BASE.put("left_thigh", new float[] {-16, 0, 0});
        BASE.put("right_thigh", new float[] {-16, 0, 0});
        BASE.put("left_shin", new float[] {26, 0, 0});
        BASE.put("right_shin", new float[] {26, 0, 0});
        BASE.put("left_foot", new float[] {-10, 0, 0});
        BASE.put("right_foot", new float[] {-10, 0, 0});
        BASE.put("club_back", new float[] {0, 0, 28});
    }

    private static final AnimationChannel.Interpolation SMOOTH = AnimationChannel.Interpolations.CATMULLROM;
    private static final AnimationChannel.Interpolation LINEAR = AnimationChannel.Interpolations.LINEAR;

    private static final Map<CanopyAnim, AnimationDefinition> DEFINITIONS = new EnumMap<>(CanopyAnim.class);

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

    private static final Pose STAND = Pose.of();
    /** Club over the shoulder, free hand relaxed: the combat resting pose. */
    private static final Pose CARRY = Pose.of("right_upper_arm -62 0 0", "right_forearm -45 0 0", "right_hand 58 0 0",
            "left_upper_arm 20 0 -6", "left_forearm -30 0 0", "chest 14 0 0", "head -12 0 0");
    private static final Pose WIND = Pose.of("chest -4 0 0", "head -22 0 0", "right_upper_arm -150 0 0", "right_forearm -22 0 0",
            "right_hand 121 0 0", "left_upper_arm -140 12 0", "left_forearm -16 0 0", "jaw 14 0 0",
            "left_thigh -24 0 0", "right_thigh -24 0 0", "@pelvis 0 1 0");
    private static final Pose MID = Pose.of("chest 8 0 0", "head -16 0 0", "right_upper_arm -104 0 0", "right_forearm -16 0 0",
            "right_hand 153 0 0", "left_upper_arm -100 12 0", "left_forearm -16 0 0", "@pelvis 0 2 0");
    private static final Pose IMPACT = Pose.of("chest 36 0 0", "head -4 0 0", "right_upper_arm -56 0 0", "right_forearm -10 0 0",
            "right_hand 158 0 0", "left_upper_arm -60 12 0", "left_forearm -12 0 0", "@pelvis 0 4 0",
            "left_thigh -40 0 0", "right_thigh -22 0 0", "left_shin 56 0 0", "right_shin 40 0 0");
    private static final Pose GROUND = Pose.of("chest 24 0 0", "head -8 0 0", "right_upper_arm -22 0 0", "right_forearm -10 0 0",
            "right_hand 173 0 0", "left_upper_arm 22 0 -8", "left_forearm -34 0 0");
    private static final Pose CROUCH = Pose.of("chest 40 0 0", "head -26 0 0", "@pelvis 0 5 0", "left_thigh -72 0 0",
            "right_thigh -72 0 0", "left_shin 100 0 0", "right_shin 100 0 0", "left_foot -28 0 0", "right_foot -28 0 0",
            "left_upper_arm 28 0 -8", "right_upper_arm 28 0 8", "left_forearm -30 0 0", "right_forearm -30 0 0");
    private static final Pose LEAP = Pose.of("chest 6 0 0", "head -16 0 0", "@pelvis 0 -2 0", "left_thigh -6 0 0",
            "right_thigh -6 0 0", "left_shin 8 0 0", "right_shin 8 0 0", "left_foot 14 0 0", "right_foot 14 0 0",
            "left_upper_arm -140 0 -8", "right_upper_arm -140 0 8", "left_forearm -12 0 0", "right_forearm -12 0 0");
    private static final Pose AIR = Pose.of("chest 12 0 0", "head -14 0 0", "left_thigh -52 0 0", "right_thigh -44 0 0",
            "left_shin 64 0 0", "right_shin 58 0 0", "left_foot 0 0 0", "right_foot 0 0 0",
            "left_upper_arm -112 0 -14", "right_upper_arm -112 0 14", "left_forearm -16 0 0", "right_forearm -16 0 0");
    /** Body hugging a trunk: upright chest, knees drawn up, arms reaching high. */
    private static final Pose CLING = Pose.of("chest 4 0 0", "head 12 0 0", "left_upper_arm -150 0 -10",
            "right_upper_arm -150 0 10", "left_forearm -18 0 0", "right_forearm -18 0 0", "left_thigh -66 0 -8",
            "right_thigh -66 0 8", "left_shin 82 0 0", "right_shin 82 0 0", "left_foot -14 0 0", "right_foot -14 0 0");

    private static Object[] f(float t, Pose p) {
        return new Object[] {t, p};
    }

    // ------------------------------------------------------------------------------------------------ builder
    private static void def(CanopyAnim anim, float length, boolean smooth, Object[]... frames) {
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

    public static AnimationDefinition get(CanopyAnim anim) {
        return DEFINITIONS.get(anim);
    }

    private CanopyBreakerAnimations() {}

    // ------------------------------------------------------------------------------------------------ the catalog
    static {
        // ===== A. personality / idle
        Pose in = Pose.of("chest 18 0 0", "head -15 0 0", "left_upper_arm 16 0 -6", "right_upper_arm 16 0 6", "jaw 2 0 0");
        Pose out = Pose.of("chest 13 0 0", "head -11 0 0", "left_upper_arm 12 0 -4", "right_upper_arm 12 0 4");
        def(CanopyAnim.IDLE_BREATHING, 3.2f, true, f(0f, out), f(1.5f, in));
        def(CanopyAnim.IDLE_WATCH, 4.0f, true, f(0f, out.with("head -12 -4 0")), f(1.0f, in.with("head -14 30 3", "chest 14 10 0")),
                f(2.0f, out.with("head -10 -6 0")), f(3.0f, in.with("head -13 -24 -3", "chest 14 -8 0")));
        def(CanopyAnim.IDLE_SNIFF, 2.0f, true, f(0f, out), f(0.35f, Pose.of("chest 24 0 0", "head -4 0 0", "jaw 10 0 0")),
                f(0.6f, Pose.of("chest 22 0 0", "head -8 4 0", "jaw 2 0 0")), f(0.85f, Pose.of("chest 25 0 0", "head -2 -4 0", "jaw 10 0 0")),
                f(1.3f, Pose.of("chest 22 0 0", "head -10 0 0", "jaw 0 0 0")));
        def(CanopyAnim.IDLE_SCRATCH, 2.4f, true, f(0f, out), f(0.5f, Pose.of("chest 12 -6 0", "head -8 -14 8", "left_upper_arm -118 0 -20", "left_forearm -100 0 0", "left_hand 10 0 0")),
                f(0.8f, Pose.of("chest 12 -6 0", "head -8 -14 8", "left_upper_arm -120 0 -22", "left_forearm -112 0 0")),
                f(1.1f, Pose.of("chest 12 -6 0", "head -8 -14 8", "left_upper_arm -118 0 -20", "left_forearm -98 0 0")),
                f(1.4f, Pose.of("chest 12 -6 0", "head -8 -14 8", "left_upper_arm -120 0 -22", "left_forearm -112 0 0")),
                f(1.9f, out));
        def(CanopyAnim.IDLE_CLUB_DRAG, 2.0f, true, f(0f, GROUND), f(0.6f, GROUND.with("chest 26 6 0", "right_upper_arm -26 0 0", "head -10 8 0")),
                f(1.2f, GROUND.with("chest 24 -4 0", "right_upper_arm -20 0 0", "head -6 -6 0")));
        def(CanopyAnim.IDLE_ROAR_SHORT, 1.4f, true, f(0f, out), f(0.25f, Pose.of("chest 6 0 0", "head -38 0 0", "jaw 26 0 0", "left_upper_arm -10 0 -24", "right_upper_arm -10 0 24")),
                f(0.8f, Pose.of("chest 8 0 0", "head -34 0 0", "jaw 22 0 0", "left_upper_arm -6 0 -26", "right_upper_arm -6 0 26")),
                f(1.15f, out));

        // ===== B. ground locomotion
        Pose wl = Pose.of("left_thigh -38 0 0", "left_shin 30 0 0", "left_foot -2 0 0", "right_thigh 6 0 0", "right_shin 22 0 0", "right_foot -14 0 0",
                "left_upper_arm 38 0 -6", "right_upper_arm -8 0 6", "chest 16 -7 0", "head -13 5 0");
        Pose wr = Pose.of("right_thigh -38 0 0", "right_shin 30 0 0", "right_foot -2 0 0", "left_thigh 6 0 0", "left_shin 22 0 0", "left_foot -14 0 0",
                "right_upper_arm 38 0 6", "left_upper_arm -8 0 -6", "chest 16 7 0", "head -13 -5 0");
        def(CanopyAnim.WALK, 1.2f, true, f(0f, wl.with("@pelvis 0 0 0")), f(0.3f, wl.with("@pelvis 0 1.4 0", "left_thigh -14 0 0", "right_thigh -12 0 0", "chest 18 0 0")),
                f(0.6f, wr.with("@pelvis 0 0 0")), f(0.9f, wr.with("@pelvis 0 1.4 0", "left_thigh -12 0 0", "right_thigh -14 0 0", "chest 18 0 0")));
        Pose rl = Pose.of("left_thigh -62 0 0", "left_shin 52 0 0", "right_thigh 26 0 0", "right_shin 40 0 0", "right_foot 8 0 0",
                "left_upper_arm -78 0 -10", "right_upper_arm -12 0 10", "left_forearm -8 0 0", "right_forearm -10 0 0",
                "chest 44 -6 0", "head -34 4 0", "@pelvis 0 2 0", "jaw 6 0 0");
        Pose rr = Pose.of("right_thigh -62 0 0", "right_shin 52 0 0", "left_thigh 26 0 0", "left_shin 40 0 0", "left_foot 8 0 0",
                "right_upper_arm -78 0 10", "left_upper_arm -12 0 -10", "right_forearm -8 0 0", "left_forearm -10 0 0",
                "chest 44 6 0", "head -34 -4 0", "@pelvis 0 2 0", "jaw 6 0 0");
        def(CanopyAnim.RUN_FOUR_LIMB, 0.7f, true, f(0f, rl), f(0.175f, rl.with("@pelvis 0 -3 0", "left_thigh -20 0 0", "right_thigh -20 0 0", "left_upper_arm -30 0 -10", "right_upper_arm -30 0 10")),
                f(0.35f, rr), f(0.525f, rr.with("@pelvis 0 -3 0", "left_thigh -20 0 0", "right_thigh -20 0 0", "left_upper_arm -30 0 -10", "right_upper_arm -30 0 10")));
        Pose bl = Pose.of("left_thigh -54 0 0", "left_shin 56 0 0", "right_thigh 22 0 0", "right_shin 34 0 0", "right_foot 6 0 0",
                "left_upper_arm 50 0 -8", "right_upper_arm -50 0 8", "chest 26 -8 0", "head -20 6 0");
        Pose br = Pose.of("right_thigh -54 0 0", "right_shin 56 0 0", "left_thigh 22 0 0", "left_shin 34 0 0", "left_foot 6 0 0",
                "right_upper_arm 50 0 8", "left_upper_arm -50 0 -8", "chest 26 8 0", "head -20 -6 0");
        def(CanopyAnim.RUN_BIPEDAL, 0.6f, true, f(0f, bl), f(0.15f, bl.with("@pelvis 0 -2.5 0")), f(0.3f, br), f(0.45f, br.with("@pelvis 0 -2.5 0")));
        def(CanopyAnim.START_MOVING, 0.4f, true, f(0f, out), f(0.2f, Pose.of("chest 24 0 0", "head -16 0 0", "left_thigh -30 0 0", "right_thigh -6 0 0", "left_shin 34 0 0", "@pelvis 0 1 0")), f(0.4f, wl));
        def(CanopyAnim.STOP_MOVING, 0.5f, true, f(0f, wl), f(0.2f, Pose.of("chest 8 0 0", "head -8 0 0", "left_thigh -26 0 0", "right_thigh -26 0 0", "left_shin 34 0 0", "right_shin 34 0 0", "@pelvis 0 1.5 0")), f(0.5f, STAND));
        def(CanopyAnim.TURN_LEFT, 0.6f, true, f(0f, STAND), f(0.25f, Pose.of("chest 16 -26 0", "head -12 -18 0", "left_thigh -26 -10 0", "right_thigh -8 14 0", "left_upper_arm 30 0 -10")), f(0.6f, STAND));
        def(CanopyAnim.TURN_RIGHT, 0.6f, true, f(0f, STAND), f(0.25f, Pose.of("chest 16 26 0", "head -12 18 0", "right_thigh -26 10 0", "left_thigh -8 -14 0", "right_upper_arm 30 0 10")), f(0.6f, STAND));
        def(CanopyAnim.STEP_UP, 0.5f, true, f(0f, STAND), f(0.2f, Pose.of("chest 24 0 0", "left_thigh -62 0 0", "left_shin 82 0 0", "left_foot -20 0 0", "left_upper_arm -40 0 -8", "@pelvis 0 -1 0")),
                f(0.35f, Pose.of("chest 20 0 0", "left_thigh -34 0 0", "left_shin 40 0 0", "@pelvis 0 -3 0")), f(0.5f, STAND));
        def(CanopyAnim.FALL, 0.6f, true, f(0f, Pose.of("chest 10 0 0", "head -20 0 0", "left_upper_arm -150 0 -34", "right_upper_arm -150 0 34", "left_thigh -30 0 -12", "right_thigh -20 0 12", "left_shin 20 0 0", "right_shin 30 0 0", "jaw 18 0 0")),
                f(0.3f, Pose.of("chest 12 0 0", "head -22 0 0", "left_upper_arm -136 0 -42", "right_upper_arm -146 0 40", "left_thigh -20 0 -14", "right_thigh -34 0 14", "left_shin 30 0 0", "right_shin 16 0 0", "jaw 24 0 0")));

        // ===== C. climbing
        def(CanopyAnim.CLIMB_APPROACH, 0.6f, true, f(0f, bl.with("left_upper_arm -90 0 -10", "right_upper_arm -70 0 10", "chest 22 0 0")), f(0.3f, br.with("left_upper_arm -70 0 -10", "right_upper_arm -90 0 10", "chest 22 0 0")));
        def(CanopyAnim.CLIMB_GRAB, 0.5f, true, f(0f, Pose.of("chest 22 0 0", "left_upper_arm -90 0 -10", "right_upper_arm -90 0 10")),
                f(0.25f, Pose.of("chest 8 0 0", "head 4 0 0", "left_upper_arm -140 0 -12", "right_upper_arm -140 0 12", "left_thigh -40 0 0", "right_thigh -40 0 0", "left_shin 50 0 0", "right_shin 50 0 0", "@pelvis 0 -2 0")),
                f(0.5f, CLING));
        Pose cuA = CLING.with("left_upper_arm -165 0 -10", "right_upper_arm -118 0 10", "left_thigh -34 0 -8", "right_thigh -86 0 8", "left_shin 44 0 0", "right_shin 100 0 0", "chest 2 4 0");
        Pose cuB = CLING.with("right_upper_arm -165 0 10", "left_upper_arm -118 0 -10", "right_thigh -34 0 8", "left_thigh -86 0 -8", "right_shin 44 0 0", "left_shin 100 0 0", "chest 2 -4 0");
        def(CanopyAnim.CLIMB_UP, 0.9f, true, f(0f, cuA), f(0.45f, cuB));
        Pose cdA = CLING.with("left_upper_arm -138 0 -10", "right_upper_arm -100 0 10", "left_thigh -50 0 -8", "right_thigh -30 0 8", "left_shin 60 0 0", "right_shin 34 0 0", "head 18 0 0", "chest -2 0 0");
        Pose cdB = CLING.with("right_upper_arm -138 0 10", "left_upper_arm -100 0 -10", "right_thigh -50 0 8", "left_thigh -30 0 -8", "right_shin 60 0 0", "left_shin 34 0 0", "head 18 0 0", "chest -2 0 0");
        def(CanopyAnim.CLIMB_DOWN, 1.0f, true, f(0f, cdA), f(0.5f, cdB));
        def(CanopyAnim.CLING_IDLE, 3.0f, true, f(0f, CLING), f(1.5f, CLING.with("chest 6 0 0", "head 15 6 0", "@pelvis 0 -0.6 0")));
        def(CanopyAnim.CLING_REPOSITION, 1.0f, true, f(0f, CLING), f(0.3f, CLING.with("right_upper_arm -174 0 14", "right_forearm -34 0 0", "chest 4 -8 0")), f(0.6f, CLING.with("right_upper_arm -156 0 10", "left_thigh -80 0 -8", "left_shin 96 0 0")), f(1.0f, CLING));
        Pose btl = Pose.of("chest 38 14 0", "head -22 -8 0", "@pelvis 0 4 0", "left_thigh -60 0 -34", "left_shin 84 0 0", "right_thigh -52 0 14", "right_shin 78 0 0",
                "left_upper_arm -50 0 -70", "right_upper_arm 4 0 20", "left_forearm -20 0 0", "left_foot -26 0 0", "right_foot -26 0 0");
        Pose btr = Pose.of("chest 38 -14 0", "head -22 8 0", "@pelvis 0 4 0", "right_thigh -60 0 34", "right_shin 84 0 0", "left_thigh -52 0 -14", "left_shin 78 0 0",
                "right_upper_arm -50 0 70", "left_upper_arm 4 0 -20", "right_forearm -20 0 0", "left_foot -26 0 0", "right_foot -26 0 0");
        def(CanopyAnim.BRANCH_TRAVERSE_LEFT, 0.9f, true, f(0f, btl), f(0.45f, btl.with("left_thigh -40 0 -10", "right_thigh -72 0 24", "left_upper_arm -30 0 -50", "right_upper_arm -40 0 40", "@pelvis 0 5.5 0")));
        def(CanopyAnim.BRANCH_TRAVERSE_RIGHT, 0.9f, true, f(0f, btr), f(0.45f, btr.with("right_thigh -40 0 10", "left_thigh -72 0 -24", "right_upper_arm -30 0 50", "left_upper_arm -40 0 -40", "@pelvis 0 5.5 0")));
        Pose bal = Pose.of("chest 30 0 0", "head -20 0 0", "@pelvis 0 3 0", "left_thigh -44 0 -12", "right_thigh -44 0 12", "left_shin 62 0 0", "right_shin 62 0 0",
                "left_upper_arm -10 0 -74", "right_upper_arm -10 0 74", "left_forearm -12 0 0", "right_forearm -12 0 0");
        def(CanopyAnim.BRANCH_BALANCE, 2.0f, true, f(0f, bal), f(0.5f, bal.with("left_upper_arm -14 0 -80", "right_upper_arm -6 0 66", "chest 30 0 4", "@pelvis 0 3.4 0")),
                f(1.0f, bal), f(1.5f, bal.with("left_upper_arm -6 0 -66", "right_upper_arm -14 0 80", "chest 30 0 -4", "@pelvis 0 3.4 0")));
        def(CanopyAnim.BRANCH_CROUCH, 0.5f, false, f(0f, STAND), f(0.35f, CROUCH.with("left_upper_arm 52 0 -8", "right_upper_arm 52 0 8")), f(0.5f, CROUCH.with("@pelvis 0 5.5 0", "left_upper_arm 60 0 -8", "right_upper_arm 60 0 8")));
        def(CanopyAnim.BRANCH_LAUNCH, 0.4f, false, f(0f, CROUCH.with("@pelvis 0 5.5 0")), f(0.15f, LEAP.with("@pelvis 0 -3 0", "chest 4 0 0")), f(0.4f, AIR));
        def(CanopyAnim.BRANCH_GRAB, 0.5f, false, f(0f, AIR.with("left_upper_arm -140 0 -14", "right_upper_arm -140 0 14")), f(0.2f, CLING.with("chest 14 0 0", "@pelvis 0 3 0")), f(0.5f, CLING));
        def(CanopyAnim.CLIMB_FAILED, 0.8f, false, f(0f, CLING), f(0.2f, Pose.of("chest 6 0 0", "head -24 0 0", "left_upper_arm -170 0 -40", "right_upper_arm -140 0 38", "left_thigh -50 0 -10", "right_thigh -30 0 10", "left_shin 50 0 0", "right_shin 28 0 0", "jaw 26 0 0")),
                f(0.5f, Pose.of("chest 14 0 0", "head -28 0 0", "left_upper_arm -120 0 -60", "right_upper_arm -164 0 56", "left_thigh -20 0 -16", "right_thigh -44 0 16", "left_shin 30 0 0", "right_shin 50 0 0", "jaw 24 0 0")),
                f(0.8f, AIR));

        // ===== D. jumps and landings
        def(CanopyAnim.JUMP_PREPARE, 0.6f, false, f(0f, STAND), f(0.4f, CROUCH.with("left_upper_arm 54 0 -8", "right_upper_arm 54 0 8", "jaw 6 0 0")), f(0.6f, CROUCH.with("@pelvis 0 5.5 0", "left_upper_arm 60 0 -8", "right_upper_arm 60 0 8")));
        def(CanopyAnim.JUMP_TAKEOFF, 0.35f, false, f(0f, CROUCH.with("@pelvis 0 5.5 0")), f(0.12f, LEAP.with("@pelvis 0 -3 0")), f(0.35f, AIR));
        def(CanopyAnim.JUMP_AIRBORNE, 0.9f, true, f(0f, AIR), f(0.45f, AIR.with("left_thigh -46 0 -10", "right_thigh -52 0 10", "left_upper_arm -120 0 -18", "right_upper_arm -104 0 18", "chest 14 0 0")));
        def(CanopyAnim.JUMP_STEER, 0.8f, true, f(0f, AIR.with("chest 12 -12 6")), f(0.4f, AIR.with("chest 12 12 -6", "left_upper_arm -100 0 -30", "right_upper_arm -120 0 10")));
        def(CanopyAnim.JUMP_LAND_LIGHT, 0.6f, false, f(0f, AIR), f(0.15f, CROUCH.with("chest 34 0 0", "@pelvis 0 4 0", "left_upper_arm -40 0 -24", "right_upper_arm -40 0 24")), f(0.6f, STAND));
        def(CanopyAnim.JUMP_LAND_HEAVY, 0.8f, false, f(0f, AIR), f(0.14f, CROUCH.with("chest 52 0 0", "@pelvis 0 7.5 0", "left_upper_arm -50 0 -22", "right_upper_arm -50 0 22", "head -34 0 0", "jaw 14 0 0")),
                f(0.35f, CROUCH.with("chest 50 0 0", "@pelvis 0 7 0", "left_upper_arm -46 0 -22", "right_upper_arm -46 0 22")), f(0.8f, STAND));
        def(CanopyAnim.LAND_RECOVERY, 1.1f, false, f(0f, CROUCH.with("chest 46 0 0", "@pelvis 0 6 0")), f(0.5f, Pose.of("chest 32 0 0", "head -16 0 0", "@pelvis 0 2 0", "left_thigh -34 0 0", "right_thigh -34 0 0", "left_shin 44 0 0", "right_shin 44 0 0", "jaw 10 0 0")), f(1.1f, STAND));
        def(CanopyAnim.JUMP_ABORT, 0.4f, false, f(0f, CROUCH.with("@pelvis 0 5 0")), f(0.4f, STAND));

        // ===== E. club
        def(CanopyAnim.DRAW_CLUB_PREPARE, 0.4f, false, f(0f, STAND), f(0.25f, Pose.of("chest 12 -18 0", "head -12 -36 0", "right_upper_arm -120 30 20", "right_forearm -50 0 0", "right_hand 20 0 0")), f(0.4f, Pose.of("chest 12 -22 0", "head -12 -40 0", "right_upper_arm -150 36 24", "right_forearm -60 0 0", "right_hand 30 0 0")));
        def(CanopyAnim.DRAW_CLUB, 0.6f, false, f(0f, Pose.of("chest 12 -22 0", "head -12 -40 0", "right_upper_arm -150 36 24", "right_forearm -60 0 0", "right_hand 30 0 0")),
                f(0.25f, Pose.of("chest 4 -8 0", "head -16 -14 0", "right_upper_arm -156 12 12", "right_forearm -30 0 0", "right_hand 100 0 0")), f(0.6f, WIND));
        def(CanopyAnim.DRAW_CLUB_FINISH, 0.6f, false, f(0f, WIND), f(0.25f, MID), f(0.6f, CARRY.with("chest 20 0 0", "right_upper_arm -52 0 0", "right_hand 46 0 0")));
        Pose gi = CARRY.with("chest 20 0 0", "right_upper_arm -52 0 0", "right_forearm -42 0 0", "right_hand 46 0 0", "head -16 0 0");
        def(CanopyAnim.CLUB_GUARD_IDLE, 2.4f, true, f(0f, gi), f(1.2f, gi.with("chest 23 0 0", "head -18 3 0", "right_upper_arm -55 0 0", "right_hand 42 0 0", "left_upper_arm 24 0 -8")));
        Pose gwl = gi.with("left_thigh -38 0 0", "left_shin 30 0 0", "right_thigh 6 0 0", "right_shin 22 0 0", "chest 22 -5 0", "left_upper_arm 36 0 -6", "@pelvis 0 0 0");
        Pose gwr = gi.with("right_thigh -38 0 0", "right_shin 30 0 0", "left_thigh 6 0 0", "left_shin 22 0 0", "chest 22 5 0", "left_upper_arm -6 0 -6", "@pelvis 0 0 0");
        def(CanopyAnim.CLUB_GUARD_WALK, 1.2f, true, f(0f, gwl), f(0.3f, gwl.with("@pelvis 0 1.4 0", "right_hand 52 0 0", "left_thigh -14 0 0", "right_thigh -12 0 0")), f(0.6f, gwr), f(0.9f, gwr.with("@pelvis 0 1.4 0", "right_hand 52 0 0", "left_thigh -12 0 0", "right_thigh -14 0 0")));
        def(CanopyAnim.CLUB_DRAG, 1.6f, true, f(0f, GROUND.with("left_thigh -34 0 0", "left_shin 30 0 0", "right_thigh 6 0 0", "right_shin 22 0 0", "chest 28 -6 0")),
                f(0.4f, GROUND.with("@pelvis 0 1.5 0", "chest 28 0 0")), f(0.8f, GROUND.with("right_thigh -34 0 0", "right_shin 30 0 0", "left_thigh 6 0 0", "left_shin 22 0 0", "chest 28 6 0")), f(1.2f, GROUND.with("@pelvis 0 1.5 0", "chest 28 0 0")));
        def(CanopyAnim.CLUB_RECOVER_GRIP, 0.7f, false, f(0f, IMPACT), f(0.3f, Pose.of("chest 28 0 0", "right_upper_arm -60 0 0", "right_forearm -40 0 0", "right_hand 120 0 0", "left_upper_arm -50 14 0", "left_forearm -40 0 0", "head -10 0 0")), f(0.7f, gi));
        def(CanopyAnim.CLUB_STOW, 0.8f, false, f(0f, gi), f(0.3f, Pose.of("chest 8 -8 0", "head -14 -26 0", "right_upper_arm -150 20 14", "right_forearm -34 0 0", "right_hand 100 0 0")), f(0.55f, Pose.of("chest 12 -20 0", "head -12 -38 0", "right_upper_arm -150 36 24", "right_forearm -60 0 0", "right_hand 30 0 0")), f(0.8f, STAND));

        // ===== F. attacks
        Pose guard = gi;
        def(CanopyAnim.ATTACK_TRUNK_SLAM_PREPARE, 1.0f, true, f(0f, guard), f(0.35f, MID.with("chest 6 0 0", "right_upper_arm -110 0 0", "right_hand 130 0 0")), f(0.75f, WIND), f(1.0f, WIND.with("chest -6 0 0", "head -26 0 0", "jaw 18 0 0")));
        def(CanopyAnim.ATTACK_TRUNK_SLAM_PREPARE_ALT, 1.1f, true, f(0f, guard), f(0.3f, guard.with("right_thigh -52 0 0", "right_shin 52 0 0", "left_thigh 8 0 0", "chest 18 -14 0", "@pelvis 0 2 0")), f(0.8f, WIND.with("right_thigh -40 0 0", "right_shin 44 0 0", "left_thigh 4 0 0", "chest -2 -8 0")), f(1.1f, WIND.with("chest -6 -8 0", "head -26 0 0", "jaw 18 0 0")));
        def(CanopyAnim.ATTACK_TRUNK_SLAM_EXECUTE, 0.5f, false, f(0f, WIND.with("chest -6 0 0")), f(0.15f, MID), f(0.28f, IMPACT), f(0.5f, IMPACT.with("@pelvis 0 4.5 0")));
        def(CanopyAnim.ATTACK_TRUNK_SLAM_RECOVERY, 1.0f, false, f(0f, IMPACT.with("@pelvis 0 4.5 0")), f(0.4f, IMPACT.with("chest 40 0 0", "jaw 12 0 0", "head 0 0 0", "@pelvis 0 4 0")), f(1.0f, gi));
        Pose swL = Pose.of("chest 12 52 0", "head -12 -22 0", "right_upper_arm -86 20 20", "right_forearm -30 0 0", "right_hand 120 0 0", "left_upper_arm -50 30 -20", "left_forearm -40 0 0",
                "left_thigh -40 0 -10", "right_thigh -8 24 0", "left_shin 44 0 0", "@pelvis 0 2 0");
        Pose swLx = Pose.of("chest 22 -64 0", "head -12 20 0", "right_upper_arm -80 -34 -16", "right_forearm -14 0 0", "right_hand 150 0 0", "left_upper_arm -70 -30 -20", "left_forearm -30 0 0",
                "left_thigh -12 0 0", "right_thigh -44 -20 0", "right_shin 52 0 0", "@pelvis 0 3 0");
        def(CanopyAnim.ATTACK_CANOPY_SWEEP_PREPARE_LEFT, 0.8f, false, f(0f, guard), f(0.5f, swL.with("chest 16 36 0")), f(0.8f, swL));
        def(CanopyAnim.ATTACK_CANOPY_SWEEP_EXECUTE_LEFT, 0.5f, false, f(0f, swL), f(0.2f, swL.with("chest 18 -10 0", "right_upper_arm -92 0 6")), f(0.34f, swLx), f(0.5f, swLx.with("chest 20 -52 0")));
        Pose swR = Pose.of("chest 14 -50 0", "head -12 22 0", "right_upper_arm -70 -40 -22", "right_forearm -20 0 0", "right_hand 130 0 0", "left_upper_arm -70 -20 -20", "left_forearm -30 0 0",
                "right_thigh -40 0 10", "left_thigh -8 -24 0", "right_shin 44 0 0", "@pelvis 0 2 0");
        Pose swRx = Pose.of("chest 22 62 0", "head -12 -20 0", "right_upper_arm -90 40 20", "right_forearm -12 0 0", "right_hand 150 0 0", "left_upper_arm -84 30 20", "left_forearm -30 0 0",
                "left_thigh -44 20 0", "left_shin 52 0 0", "@pelvis 0 3 0");
        def(CanopyAnim.ATTACK_CANOPY_SWEEP_PREPARE_RIGHT, 0.8f, false, f(0f, guard), f(0.5f, swR.with("chest 16 -30 0")), f(0.8f, swR));
        def(CanopyAnim.ATTACK_CANOPY_SWEEP_EXECUTE_RIGHT, 0.5f, false, f(0f, swR), f(0.2f, swR.with("chest 18 8 0", "right_upper_arm -80 0 -6")), f(0.34f, swRx), f(0.5f, swRx.with("chest 20 50 0")));
        def(CanopyAnim.ATTACK_CANOPY_SWEEP_RECOVERY, 0.9f, false, f(0f, swLx.with("chest 20 -40 0")), f(0.4f, gi.with("chest 26 -14 0", "jaw 10 0 0")), f(0.9f, gi));
        Pose lungeP = Pose.of("chest 40 0 0", "head -30 0 0", "@pelvis 0 4 0", "left_thigh -64 0 0", "right_thigh -30 0 0", "left_shin 88 0 0", "right_shin 56 0 0",
                "right_upper_arm 20 0 10", "right_forearm -76 0 0", "right_hand 130 0 0", "left_upper_arm -70 10 -10", "left_forearm -20 0 0", "jaw 16 0 0");
        def(CanopyAnim.ATTACK_BRUTE_LUNGE_PREPARE, 0.5f, false, f(0f, guard), f(0.3f, lungeP.with("chest 34 0 0")), f(0.5f, lungeP));
        Pose lungeE = Pose.of("chest 48 0 0", "head -32 0 0", "@pelvis 0 0 0", "left_thigh -78 0 0", "right_thigh 24 0 0", "left_shin 64 0 0", "right_shin 24 0 0",
                "right_upper_arm -92 0 0", "right_forearm -12 0 0", "right_hand 110 0 0", "left_upper_arm -86 10 -10", "left_forearm -16 0 0", "jaw 24 0 0");
        def(CanopyAnim.ATTACK_BRUTE_LUNGE_EXECUTE, 0.6f, false, f(0f, lungeP), f(0.18f, lungeE), f(0.4f, lungeE.with("right_thigh -70 0 0", "left_thigh 18 0 0", "@pelvis 0 1.5 0")), f(0.6f, lungeE.with("chest 52 0 0", "right_upper_arm -70 0 0", "@pelvis 0 3 0")));
        def(CanopyAnim.ATTACK_BRUTE_LUNGE_RECOVERY, 0.9f, false, f(0f, lungeE.with("chest 52 0 0")), f(0.45f, Pose.of("chest 38 0 0", "head -14 0 0", "left_thigh -40 0 0", "right_thigh -40 0 0", "left_shin 54 0 0", "right_shin 54 0 0", "right_upper_arm -50 0 0", "right_forearm -30 0 0", "right_hand 110 0 0", "jaw 14 0 0", "@pelvis 0 3 0")), f(0.9f, gi));
        def(CanopyAnim.CANOPY_DROP_PREPARE, 0.7f, false, f(0f, STAND), f(0.4f, CROUCH.with("right_upper_arm -150 0 0", "right_forearm -22 0 0", "right_hand 121 0 0", "left_upper_arm -140 12 0")), f(0.7f, CROUCH.with("@pelvis 0 6 0", "right_upper_arm -154 0 0", "right_forearm -22 0 0", "right_hand 121 0 0", "left_upper_arm -144 12 0")));
        def(CanopyAnim.CANOPY_DROP_AIRBORNE, 0.8f, true, f(0f, AIR.with("chest 4 0 0", "right_upper_arm -154 0 0", "right_forearm -22 0 0", "right_hand 121 0 0", "left_upper_arm -144 12 0", "jaw 20 0 0")),
                f(0.4f, AIR.with("chest 8 0 0", "right_upper_arm -150 0 0", "right_forearm -22 0 0", "right_hand 126 0 0", "left_upper_arm -140 12 0", "jaw 24 0 0", "left_thigh -36 0 0", "right_thigh -40 0 0")));
        def(CanopyAnim.CANOPY_DROP_IMPACT, 0.6f, false, f(0f, AIR.with("right_upper_arm -150 0 0", "right_hand 121 0 0", "left_upper_arm -144 12 0")), f(0.12f, IMPACT.with("@pelvis 0 7 0", "chest 52 0 0", "left_thigh -70 0 0", "right_thigh -70 0 0", "left_shin 100 0 0", "right_shin 100 0 0")), f(0.6f, IMPACT.with("@pelvis 0 6 0", "chest 50 0 0", "left_thigh -66 0 0", "right_thigh -66 0 0", "left_shin 96 0 0", "right_shin 96 0 0")));
        def(CanopyAnim.CANOPY_DROP_RECOVERY, 1.3f, false, f(0f, IMPACT.with("@pelvis 0 6 0", "chest 50 0 0", "left_thigh -66 0 0", "right_thigh -66 0 0", "left_shin 96 0 0", "right_shin 96 0 0")), f(0.6f, GROUND.with("chest 36 0 0", "jaw 14 0 0", "@pelvis 0 3 0", "left_thigh -40 0 0", "right_thigh -40 0 0", "left_shin 56 0 0", "right_shin 56 0 0")), f(1.3f, gi));
        def(CanopyAnim.ATTACK_BACKHAND_PREPARE, 0.5f, false, f(0f, guard), f(0.35f, Pose.of("chest 16 -44 0", "head -12 20 0", "right_upper_arm -50 -50 -30", "right_forearm -90 0 0", "right_hand 110 0 0", "left_upper_arm 20 -10 -10", "@pelvis 0 1 0")), f(0.5f, Pose.of("chest 16 -50 0", "head -12 24 0", "right_upper_arm -50 -56 -34", "right_forearm -98 0 0", "right_hand 110 0 0", "@pelvis 0 1.5 0")));
        def(CanopyAnim.ATTACK_BACKHAND_EXECUTE, 0.3f, false, f(0f, Pose.of("chest 16 -50 0", "right_upper_arm -50 -56 -34", "right_forearm -98 0 0", "right_hand 110 0 0")), f(0.14f, Pose.of("chest 18 20 0", "head -12 -12 0", "right_upper_arm -80 20 16", "right_forearm -20 0 0", "right_hand 120 0 0", "left_upper_arm 10 0 -20")), f(0.3f, Pose.of("chest 20 54 0", "head -12 -28 0", "right_upper_arm -88 40 24", "right_forearm -10 0 0", "right_hand 130 0 0", "left_upper_arm 20 0 -26")));
        def(CanopyAnim.ATTACK_BACKHAND_RECOVERY, 0.5f, false, f(0f, Pose.of("chest 20 54 0", "right_upper_arm -88 40 24", "right_hand 130 0 0")), f(0.5f, gi));

        // ===== G. roars
        Pose roarUp = Pose.of("chest 4 0 0", "head -46 0 0", "jaw 30 0 0", "left_upper_arm -60 0 -66", "right_upper_arm -60 0 66", "left_forearm -30 0 0", "right_forearm -30 0 0", "@pelvis 0 1 0");
        def(CanopyAnim.ROAR_IDLE, 1.2f, false, f(0f, STAND), f(0.3f, Pose.of("chest 8 0 0", "head -36 0 0", "jaw 24 0 0", "left_upper_arm -12 0 -26", "right_upper_arm -12 0 26")), f(0.8f, Pose.of("chest 9 0 0", "head -34 0 0", "jaw 20 0 0")), f(1.2f, STAND));
        def(CanopyAnim.ROAR_AGGRO, 1.6f, false, f(0f, gi.with("right_upper_arm -62 0 0", "right_hand 58 0 0")), f(0.3f, Pose.of("chest 30 0 0", "head -4 0 0", "jaw 4 0 0")), f(0.6f, roarUp), f(1.2f, roarUp.with("head -42 0 0", "jaw 26 0 0", "left_upper_arm -50 0 -70", "right_upper_arm -50 0 70")), f(1.6f, gi));
        // Intro: he rears up and drums his chest with alternating fists, a gorilla's display, before the roar.
        Pose upright = Pose.of("chest -4 0 0", "head -22 0 0", "jaw 16 0 0", "@pelvis 0 -1 0",
                "left_thigh -10 0 -6", "right_thigh -10 0 6", "left_shin 14 0 0", "right_shin 14 0 0");
        Pose beatLeft = upright.with("left_upper_arm -28 0 40", "left_forearm -112 0 0", "left_hand 10 0 0",
                "right_upper_arm -104 0 58", "right_forearm -58 0 0", "chest -6 6 0", "jaw 22 0 0");
        Pose beatRight = upright.with("right_upper_arm -28 0 -40", "right_forearm -112 0 0", "right_hand 10 0 0",
                "left_upper_arm -104 0 -58", "left_forearm -58 0 0", "chest -6 -6 0", "jaw 22 0 0");
        def(CanopyAnim.INTRO_CHEST_BEAT, 2.4f, true, f(0f, gi), f(0.2f, beatLeft), f(0.4f, beatRight), f(0.6f, beatLeft),
                f(0.8f, beatRight), f(1.0f, beatLeft), f(1.2f, beatRight), f(1.4f, beatLeft), f(1.6f, beatRight),
                f(1.8f, beatLeft), f(2.0f, beatRight), f(2.4f, upright.with("left_upper_arm -120 0 -40", "right_upper_arm -120 0 40",
                        "left_forearm -30 0 0", "right_forearm -30 0 0", "head -36 0 0", "jaw 28 0 0")));
        def(CanopyAnim.ROAR_ATTACK, 0.5f, false, f(0f, gi), f(0.15f, gi.with("head -34 0 0", "jaw 26 0 0", "chest 12 0 0")), f(0.5f, gi));
        def(CanopyAnim.ROAR_PHASE_TWO, 2.0f, false, f(0f, gi), f(0.5f, CROUCH.with("chest 44 0 0", "right_upper_arm -50 0 0", "right_hand 120 0 0", "jaw 10 0 0")), f(1.0f, roarUp.with("right_upper_arm -150 0 0", "right_forearm -22 0 0", "right_hand 160 0 0")), f(1.6f, roarUp.with("right_upper_arm -154 0 0", "right_forearm -22 0 0", "right_hand 160 0 0", "head -48 0 0", "jaw 30 0 0")), f(2.0f, gi));
        def(CanopyAnim.ROAR_HURT, 0.5f, false, f(0f, gi), f(0.15f, gi.with("head -30 0 0", "jaw 22 0 0", "chest 8 0 0")), f(0.5f, gi));
        def(CanopyAnim.ROAR_STAGGER, 0.6f, false, f(0f, gi), f(0.2f, Pose.of("chest 2 0 0", "head -36 0 0", "jaw 26 0 0", "left_upper_arm -30 0 -50", "right_upper_arm -30 0 50", "@pelvis 0 0 0")), f(0.6f, gi));
        def(CanopyAnim.ROAR_DEATH, 1.0f, false, f(0f, gi), f(0.3f, roarUp.with("right_upper_arm -40 0 40", "right_hand 120 0 0")), f(1.0f, roarUp.with("head -30 0 0", "chest 20 0 0", "right_upper_arm -20 0 20", "right_hand 160 0 0")));
        def(CanopyAnim.PANT_RECOVERY, 1.2f, true, f(0f, Pose.of("chest 34 0 0", "head -12 0 0", "jaw 10 0 0", "left_thigh -36 0 0", "right_thigh -36 0 0", "left_shin 46 0 0", "right_shin 46 0 0", "@pelvis 0 2 0", "right_upper_arm -30 0 0", "right_forearm -30 0 0", "right_hand 150 0 0")),
                f(0.6f, Pose.of("chest 40 0 0", "head -14 0 0", "jaw 16 0 0", "left_thigh -36 0 0", "right_thigh -36 0 0", "left_shin 46 0 0", "right_shin 46 0 0", "@pelvis 0 2.6 0", "right_upper_arm -30 0 0", "right_forearm -30 0 0", "right_hand 150 0 0")));
        def(CanopyAnim.INTIMIDATE_ROAR, 1.5f, false, f(0f, gi), f(0.3f, Pose.of("chest 22 0 0", "head -10 0 0", "left_upper_arm -70 0 -10", "right_upper_arm -70 0 10")), f(0.5f, Pose.of("chest 6 0 0", "head -46 0 0", "jaw 30 0 0", "left_upper_arm -90 0 -60", "right_upper_arm -90 0 60", "left_forearm -80 0 0", "right_forearm -80 0 0")),
                f(0.8f, Pose.of("chest 6 0 0", "head -42 0 0", "jaw 28 0 0", "left_upper_arm -80 -30 -20", "left_forearm -110 0 0", "right_upper_arm -70 0 60", "right_forearm -60 0 0")),
                f(1.1f, Pose.of("chest 6 0 0", "head -42 0 0", "jaw 28 0 0", "right_upper_arm -80 30 20", "right_forearm -110 0 0", "left_upper_arm -70 0 -60", "left_forearm -60 0 0")), f(1.5f, gi));

        // ===== H. reactions (overlay poses are small recoils of chest and head)
        def(CanopyAnim.HURT_FRONT, 0.35f, false, f(0f, STAND), f(0.1f, Pose.of("chest 6 0 0", "head -22 0 0", "jaw 10 0 0")), f(0.35f, STAND));
        def(CanopyAnim.HURT_SIDE, 0.35f, false, f(0f, STAND), f(0.1f, Pose.of("chest 14 -14 6", "head -12 -22 6", "jaw 8 0 0")), f(0.35f, STAND));
        def(CanopyAnim.HURT_BACK, 0.35f, false, f(0f, STAND), f(0.1f, Pose.of("chest 28 0 0", "head -4 0 0", "jaw 8 0 0")), f(0.35f, STAND));
        def(CanopyAnim.STAGGER, 0.8f, false, f(0f, gi), f(0.2f, Pose.of("chest 2 0 0", "head -24 0 0", "left_upper_arm -40 0 -60", "right_upper_arm -30 0 60", "left_thigh -30 0 0", "right_thigh -4 0 0", "left_shin 40 0 0", "@pelvis 0 1 0", "jaw 24 0 0")), f(0.5f, Pose.of("chest 22 0 0", "head -8 0 0", "left_thigh -50 0 0", "right_thigh -50 0 0", "left_shin 70 0 0", "right_shin 70 0 0", "@pelvis 0 3 0")), f(0.8f, Pose.of("chest 28 0 0", "head -10 0 0", "left_thigh -36 0 0", "right_thigh -36 0 0", "left_shin 46 0 0", "right_shin 46 0 0", "@pelvis 0 2 0")));
        def(CanopyAnim.STUMBLE, 0.7f, false, f(0f, Pose.of("chest 28 0 0", "left_thigh -36 0 0", "right_thigh -36 0 0", "left_shin 46 0 0", "right_shin 46 0 0", "@pelvis 0 2 0")), f(0.25f, Pose.of("chest 20 14 0", "left_thigh -52 0 0", "right_thigh -4 0 0", "left_shin 60 0 0", "left_upper_arm -34 0 -30", "right_upper_arm -34 0 30")), f(0.5f, Pose.of("chest 22 -10 0", "right_thigh -50 0 0", "left_thigh -4 0 0", "right_shin 60 0 0")), f(0.7f, STAND));
        def(CanopyAnim.KNOCKBACK_RECOVER, 0.8f, false, f(0f, Pose.of("chest 4 0 0", "head -20 0 0", "left_upper_arm -30 0 -40", "right_upper_arm -30 0 40", "left_thigh -34 0 0", "right_thigh -24 0 0", "left_shin 40 0 0")), f(0.4f, Pose.of("chest 28 0 0", "left_thigh -60 0 0", "right_thigh -60 0 0", "left_shin 82 0 0", "right_shin 82 0 0", "@pelvis 0 4 0", "left_upper_arm -30 0 -20", "right_upper_arm -30 0 20")), f(0.8f, gi));
        def(CanopyAnim.ATTACK_INTERRUPTED, 0.6f, false, f(0f, WIND), f(0.25f, Pose.of("chest 22 0 0", "head -8 0 0", "right_upper_arm -70 0 0", "right_forearm -50 0 0", "right_hand 120 0 0", "left_upper_arm 20 0 -6", "jaw 14 0 0")), f(0.6f, gi));
        def(CanopyAnim.TARGET_LOST, 1.6f, false, f(0f, gi), f(0.4f, gi.with("head -10 -40 0", "chest 20 -18 0")), f(0.8f, gi.with("head -12 40 0", "chest 20 18 0")), f(1.2f, gi.with("head -4 0 0", "chest 26 0 0", "jaw 8 0 0")), f(1.6f, gi));
        def(CanopyAnim.TAUNT, 1.4f, false, f(0f, gi), f(0.3f, Pose.of("chest 4 0 0", "head -40 0 0", "jaw 28 0 0", "left_upper_arm -70 0 -50", "right_upper_arm -70 0 50", "left_forearm -90 0 0", "right_forearm -90 0 0")),
                f(0.5f, Pose.of("chest 4 0 0", "head -40 0 0", "jaw 28 0 0", "left_upper_arm -20 -40 -20", "right_upper_arm -70 0 50", "left_forearm -110 0 0", "right_forearm -90 0 0")),
                f(0.75f, Pose.of("chest 4 0 0", "head -40 0 0", "jaw 28 0 0", "right_upper_arm -20 40 20", "left_upper_arm -70 0 -50", "right_forearm -110 0 0", "left_forearm -90 0 0")), f(1.4f, gi));

        // ===== I. stages and death
        def(CanopyAnim.PHASE_TWO_TRANSITION, 2.5f, false, f(0f, gi), f(0.5f, CROUCH.with("chest 46 0 0", "head -10 0 0", "right_upper_arm -60 0 0", "right_hand 150 0 0", "jaw 8 4 0")), f(0.9f, CROUCH.with("chest 48 0 3", "jaw 12 0 0")),
                f(1.4f, roarUp.with("right_upper_arm -130 0 0", "right_forearm -30 0 0", "right_hand 150 0 0", "head -46 0 0")), f(2.0f, roarUp.with("right_upper_arm -150 0 0", "right_forearm -22 0 0", "right_hand 160 0 0", "head -48 0 0")), f(2.5f, gi.with("chest 26 0 0")));
        def(CanopyAnim.FURY_TRANSITION, 3.0f, false, f(0f, gi), f(0.6f, CROUCH.with("chest 50 0 0", "right_upper_arm -70 0 0", "right_hand 150 0 0", "jaw 10 0 0")), f(1.2f, WIND.with("chest -8 0 0", "head -40 0 0", "jaw 30 0 0")),
                f(1.7f, WIND.with("chest -10 0 0", "head -44 0 0", "jaw 32 0 0")), f(1.95f, IMPACT.with("@pelvis 0 5 0")), f(2.3f, IMPACT.with("chest 42 0 0", "head -30 0 0", "jaw 28 0 0", "@pelvis 0 4 0")), f(3.0f, Pose.of("chest 30 0 0", "head -8 0 0", "right_upper_arm -40 0 0", "right_forearm -30 0 0", "right_hand 150 0 0", "left_upper_arm 20 0 -10", "@pelvis 0 2 0", "left_thigh -30 0 0", "right_thigh -30 0 0", "left_shin 42 0 0", "right_shin 42 0 0")));
        Pose fi = Pose.of("chest 30 0 0", "head -8 0 0", "right_upper_arm -40 0 0", "right_forearm -30 0 0", "right_hand 150 0 0", "left_upper_arm 22 0 -12", "left_forearm -30 0 0", "@pelvis 0 2 0", "left_thigh -30 0 0", "right_thigh -30 0 0", "left_shin 42 0 0", "right_shin 42 0 0", "jaw 8 0 0");
        def(CanopyAnim.FURY_IDLE, 2.0f, true, f(0f, fi), f(0.5f, fi.with("chest 34 0 0", "jaw 14 0 0", "@pelvis 0 2.6 0")), f(1.0f, fi.with("chest 30 4 0", "head -8 8 0")), f(1.5f, fi.with("chest 34 -4 0", "head -8 -8 0", "jaw 12 0 0")));
        def(CanopyAnim.DEATH_PREPARE, 0.9f, false, f(0f, gi), f(0.45f, Pose.of("chest 28 0 0", "head 4 0 0", "right_upper_arm -30 0 0", "right_forearm -30 0 0", "right_hand 150 0 0", "left_thigh -40 0 0", "right_thigh -40 0 0", "left_shin 60 0 0", "right_shin 60 0 0", "@pelvis 0 3 0", "jaw 12 0 0")),
                f(0.9f, Pose.of("chest 36 0 0", "head 12 0 0", "right_upper_arm -10 0 0", "right_forearm -20 0 0", "right_hand 170 0 0", "left_thigh -60 0 0", "right_thigh -52 0 0", "left_shin 86 0 0", "right_shin 80 0 0", "@pelvis 0 5 0", "jaw 18 0 0")));
        def(CanopyAnim.DEATH_FALL, 0.8f, false, f(0f, Pose.of("chest 36 0 0", "head 12 0 0", "right_upper_arm -10 0 0", "right_hand 170 0 0", "left_thigh -60 0 0", "right_thigh -52 0 0", "left_shin 86 0 0", "right_shin 80 0 0", "@pelvis 0 5 0")),
                f(0.5f, Pose.of("chest 58 0 0", "head 22 0 0", "right_upper_arm 10 0 20", "right_forearm -10 0 0", "right_hand 180 0 0", "left_upper_arm -30 0 -30", "left_thigh -80 0 0", "right_thigh -76 0 0", "left_shin 104 0 0", "right_shin 100 0 0", "@pelvis 0 8 0")),
                f(0.8f, Pose.of("chest 70 0 0", "head 26 0 0", "right_upper_arm 20 0 30", "left_upper_arm -40 0 -40", "left_thigh -88 0 0", "right_thigh -84 0 0", "left_shin 110 0 0", "right_shin 106 0 0", "@pelvis 0 9.5 0")));
        Pose dead = Pose.of("chest 78 0 4", "head 28 8 0", "right_upper_arm 28 0 38", "right_forearm -8 0 0", "right_hand 180 0 14", "left_upper_arm -46 0 -44", "left_forearm -12 0 0", "left_thigh -92 0 -6", "right_thigh -88 0 6", "left_shin 112 0 0", "right_shin 108 0 0", "@pelvis 0 10 0", "jaw 22 0 0");
        def(CanopyAnim.DEATH_IMPACT, 0.5f, false, f(0f, Pose.of("chest 70 0 0", "head 26 0 0", "right_upper_arm 20 0 30", "left_upper_arm -40 0 -40", "left_thigh -88 0 0", "right_thigh -84 0 0", "left_shin 110 0 0", "right_shin 106 0 0", "@pelvis 0 9.5 0")), f(0.14f, dead.with("chest 84 0 0", "@pelvis 0 11 0")), f(0.5f, dead));
        def(CanopyAnim.DEATH_FINAL, 1.0f, false, f(0f, dead), f(1.0f, dead.with("chest 80 0 4", "jaw 18 0 0", "@pelvis 0 10.4 0")));
    }
}
