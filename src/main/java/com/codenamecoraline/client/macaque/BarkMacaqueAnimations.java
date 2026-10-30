package com.codenamecoraline.client.macaque;

import com.codenamecoraline.entity.macaque.MacaqueAnim;
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
 * Keyframe animations of the Bark Macaque, written as whole-body poses with ABSOLUTE rotations in degrees per part
 * ({@code "left_arm -28 0 0"}) and position offsets in model pixels ({@code "@body 0 -1 0"}). The model has no built-in
 * stance (every part rests at zero), so the keyframes are the poses themselves; parts left out of a pose stay at rest.
 *
 * <p>Conventions: the model faces -z and y points down. A negative x rotation swings a hanging limb forward and, on the
 * body, tilts the nose up (-90 = climbing straight up); on the head a negative x looks up; on the jaw a positive x opens.</p>
 */
public final class BarkMacaqueAnimations {
    private static final AnimationChannel.Interpolation SMOOTH = AnimationChannel.Interpolations.CATMULLROM;
    private static final Map<MacaqueAnim, AnimationDefinition> DEFINITIONS = new EnumMap<>(MacaqueAnim.class);

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

    private static Object[] f(float t, Pose p) {
        return new Object[] {t, p};
    }

    private static void def(MacaqueAnim anim, float length, Object[]... frames) {
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
        for (String part : rotParts) {
            Keyframe[] kfs = new Keyframe[times.size()];
            for (int i = 0; i < kfs.length; i++) {
                float[] r = poses.get(i).rot().getOrDefault(part, new float[] {0, 0, 0});
                kfs[i] = new Keyframe(times.get(i), KeyframeAnimations.degreeVec(r[0], r[1], r[2]), SMOOTH);
            }
            b.addAnimation(part, new AnimationChannel(AnimationChannel.Targets.ROTATION, kfs));
        }
        for (String part : posParts) {
            Keyframe[] kfs = new Keyframe[times.size()];
            for (int i = 0; i < kfs.length; i++) {
                float[] v = poses.get(i).pos().getOrDefault(part, new float[] {0, 0, 0});
                kfs[i] = new Keyframe(times.get(i), KeyframeAnimations.posVec(v[0], v[1], v[2]), SMOOTH);
            }
            b.addAnimation(part, new AnimationChannel(AnimationChannel.Targets.POSITION, kfs));
        }
        DEFINITIONS.put(anim, b.build());
    }

    public static AnimationDefinition get(MacaqueAnim anim) {
        return DEFINITIONS.get(anim);
    }

    private BarkMacaqueAnimations() {}

    static {
        Pose rest = Pose.of("tail_a -14 0 0", "tail_b -10 0 0");
        // ---- idle: breathing, a look to each side, the tail swaying
        def(MacaqueAnim.IDLE, 3.2f,
                f(0f, rest.with("head 0 0 0", "@body 0 0 0")),
                f(0.8f, rest.with("head -6 24 0", "@body 0 0.3 0", "tail_a -14 18 0", "left_hand -6 0 0")),
                f(1.6f, rest.with("head 2 0 0", "@body 0 0 0", "tail_a -14 0 0")),
                f(2.4f, rest.with("head -4 -26 0", "@body 0 0.3 0", "tail_a -14 -18 0", "right_hand -6 0 0")));
        // ---- walk: diagonal gait, the tail counter-swings
        Pose walkA = rest.with("left_arm -28 0 0", "left_forearm -8 0 0", "right_arm 26 0 0", "right_thigh -26 0 0",
                "right_shin 30 0 0", "left_thigh 26 0 0", "left_shin 6 0 0", "tail_a -14 14 0", "head -4 0 0");
        Pose walkB = rest.with("right_arm -28 0 0", "right_forearm -8 0 0", "left_arm 26 0 0", "left_thigh -26 0 0",
                "left_shin 30 0 0", "right_thigh 26 0 0", "right_shin 6 0 0", "tail_a -14 -14 0", "head -4 0 0");
        def(MacaqueAnim.WALK, 0.8f, f(0f, walkA), f(0.2f, rest.with("@body 0 -0.6 0", "head -4 0 0")), f(0.4f, walkB),
                f(0.6f, rest.with("@body 0 -0.6 0", "head -4 0 0")));
        // ---- run: a bounding gallop, body rocking
        Pose runA = rest.with("left_arm -52 0 0", "right_arm -46 0 0", "left_forearm -14 0 0", "right_forearm -14 0 0",
                "left_thigh 42 0 0", "right_thigh 38 0 0", "left_shin 8 0 0", "right_shin 10 0 0", "body -8 0 0",
                "@body 0 -1.2 0", "head 8 0 0", "tail_a -30 0 0", "tail_b -10 0 0");
        Pose runB = rest.with("left_arm 34 0 0", "right_arm 30 0 0", "left_thigh -58 0 0", "right_thigh -54 0 0",
                "left_shin 76 0 0", "right_shin 72 0 0", "body 8 0 0", "@body 0 0.8 0", "head -2 0 0", "tail_a -4 0 0");
        def(MacaqueAnim.RUN, 0.5f, f(0f, runA), f(0.25f, runB));
        // ---- climb: upright body, hands and feet alternating, the tail hanging
        Pose climbA = Pose.of("body -80 0 0", "head 44 0 0", "left_arm -158 0 -4", "right_arm -104 0 4", "left_forearm -8 0 0",
                "right_forearm -30 0 0", "left_thigh -56 0 -6", "right_thigh -18 0 6", "left_shin 64 0 0", "right_shin 38 0 0",
                "tail_a 8 6 0", "tail_b 10 0 0", "@body 0 -0.4 0");
        Pose climbB = Pose.of("body -80 0 0", "head 44 0 0", "left_arm -104 0 -4", "right_arm -158 0 4", "left_forearm -30 0 0",
                "right_forearm -8 0 0", "left_thigh -18 0 -6", "right_thigh -56 0 6", "left_shin 38 0 0", "right_shin 64 0 0",
                "tail_a 8 -6 0", "tail_b 10 0 0", "@body 0 -0.4 0");
        def(MacaqueAnim.CLIMB, 0.7f, f(0f, climbA), f(0.35f, climbB));
        // ---- leap: coil, fly, land
        Pose coil = rest.with("@body 0 3 0", "left_thigh -70 0 0", "right_thigh -70 0 0", "left_shin 100 0 0",
                "right_shin 100 0 0", "left_foot -26 0 0", "right_foot -26 0 0", "left_arm 24 0 0", "right_arm 24 0 0",
                "head -22 0 0", "body 10 0 0", "tail_a -30 0 0");
        def(MacaqueAnim.LEAP_CROUCH, 0.4f, f(0f, rest), f(0.32f, coil));
        Pose air = rest.with("body -20 0 0", "left_arm -150 0 -8", "right_arm -150 0 8", "left_forearm -10 0 0",
                "right_forearm -10 0 0", "left_thigh 38 0 0", "right_thigh 34 0 0", "left_shin 14 0 0", "right_shin 14 0 0",
                "head 16 0 0", "tail_a 14 0 0", "tail_b 20 0 0", "tail_c 20 0 0");
        def(MacaqueAnim.LEAP_AIR, 0.6f, f(0f, air), f(0.3f, air.with("left_arm -138 0 -8", "right_arm -142 0 8", "tail_b 28 0 0")));
        def(MacaqueAnim.LAND, 0.35f,
                f(0f, rest.with("@body 0 3 0", "left_thigh -64 0 0", "right_thigh -64 0 0", "left_shin 94 0 0", "right_shin 94 0 0",
                        "left_arm -52 0 0", "right_arm -52 0 0", "head -6 0 0")),
                f(0.35f, rest));
        // ---- bite at the end of the leap
        def(MacaqueAnim.BITE, 0.35f,
                f(0f, rest.with("head -26 0 0", "jaw 4 0 0", "left_arm -150 0 -8", "right_arm -150 0 8", "body -14 0 0")),
                f(0.1f, rest.with("head 24 0 0", "jaw 48 0 0", "left_arm -168 0 -10", "right_arm -168 0 10", "body 8 0 0")),
                f(0.35f, rest.with("head 0 0 0", "jaw 0 0 0", "left_arm -110 0 -8", "right_arm -110 0 8", "body -8 0 0")));
        // ---- retreat: spring backward, arms up
        def(MacaqueAnim.RETREAT, 0.5f,
                f(0f, rest.with("body -22 0 0", "left_thigh 44 0 0", "right_thigh 44 0 0", "left_shin 10 0 0", "right_shin 10 0 0",
                        "left_arm -128 0 -10", "right_arm -128 0 10", "head 14 0 0", "tail_a -34 0 0")),
                f(0.28f, rest.with("body -10 0 0", "left_thigh 20 0 0", "right_thigh 20 0 0", "left_arm -90 0 -10", "right_arm -90 0 10")),
                f(0.5f, rest.with("@body 0 2 0", "left_thigh -46 0 0", "right_thigh -46 0 0", "left_shin 80 0 0", "right_shin 80 0 0",
                        "left_arm -40 0 0", "right_arm -40 0 0")));
        // ---- screech: head back, jaw wide, arms raised
        def(MacaqueAnim.SCREECH, 0.9f,
                f(0f, rest),
                f(0.22f, rest.with("head -38 0 0", "jaw 50 0 0", "body -14 0 0", "left_arm -136 0 -12", "right_arm -136 0 12",
                        "@body 0 -0.6 0", "tail_a -40 0 0")),
                f(0.66f, rest.with("head -34 6 0", "jaw 44 0 0", "body -12 0 0", "left_arm -128 0 -12", "right_arm -142 0 12",
                        "@body 0 -0.4 0", "tail_a -40 0 0")),
                f(0.9f, rest));
        def(MacaqueAnim.HURT, 0.3f,
                f(0f, rest),
                f(0.08f, rest.with("body -14 0 4", "head -22 0 0", "jaw 22 0 0", "left_arm -72 0 -10", "right_arm -72 0 10")),
                f(0.3f, rest));
        // ---- death: topples onto its side and lies still (the last pose holds)
        def(MacaqueAnim.DEATH, 1.2f,
                f(0f, rest),
                f(0.25f, rest.with("body -10 0 28", "head -20 0 0", "jaw 24 0 0", "left_arm -80 0 -10", "right_arm -80 0 10")),
                f(0.7f, rest.with("body 0 0 88", "@body 0 8 0", "head 6 0 0", "left_arm -30 0 0", "right_arm -24 0 0",
                        "left_thigh 14 0 0", "right_thigh 10 0 0", "tail_a 0 20 0")),
                f(1.2f, rest.with("body 0 0 90", "@body 0 8.2 0", "head 10 0 0", "left_arm -22 0 0", "right_arm -18 0 0",
                        "left_thigh 18 0 0", "right_thigh 14 0 0", "tail_a 0 24 0")));
    }
}
