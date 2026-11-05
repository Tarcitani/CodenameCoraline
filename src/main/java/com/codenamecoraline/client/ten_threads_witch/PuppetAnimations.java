package com.codenamecoraline.client.ten_threads_witch;

import com.codenamecoraline.entity.ten_threads_witch.PuppetAnim;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Keyframe animations of the Matriarch and her ten puppets. Every model is a tree of named parts, and what a part does in
 * an animation follows from its role (the prefix of its name): bodies bob and lunge, heads jerk like a marionette's, arms
 * and legs swing in opposite phase by side or index, wings flap, centipede segments ripple, the bell's clapper tolls, the
 * wheel of the staff spins. Each animation is sampled from a function of time into keyframes (deltas on the resting pose),
 * so all eleven models share one description and still move differently. The reference poses are documented in
 * {@code docs/design/ten_threads_witch.md} and checked offline with the pose renderer.
 */
public final class PuppetAnimations {
    private enum Role { BODY, HEAD, NECK, ARM, LEG, WING, WING_TIP, SEG, CLAPPER, WHEEL, CLOTH, JAW, ANTLER, TAIL, ABDOMEN, CAGE, CRADLE, NONE }

    private static final int SAMPLES = 8;
    private static final Map<String, Map<PuppetAnim, AnimationDefinition>> CACHE = new HashMap<>();

    private PuppetAnimations() {}

    public static float length(PuppetAnim a) {
        return switch (a) {
            case SLEEP -> 3.0F;
            case AWAKEN -> 0.8F;
            case IDLE -> 2.4F;
            case WALK -> 0.9F;
            case WINDUP -> 0.6F;
            case WINDUP_CRIMSON -> 0.5F;
            case ATTACK_A, ATTACK_B, ATTACK_C -> 0.5F;
            case RECOVER -> 0.8F;
            case TANGLED -> 1.2F;
            case DEATH -> 1.2F;
            case TAUNT -> 0.8F;
        };
    }

    public static synchronized AnimationDefinition get(String id, PuppetAnim anim) {
        return CACHE.computeIfAbsent(id, PuppetAnimations::buildAll).get(anim);
    }

    private static Map<PuppetAnim, AnimationDefinition> buildAll(String id) {
        List<String> parts = TenThreadsParts.partNames(id);
        float pivot = TenThreadsParts.pivotHeight(id);
        boolean flier = id.equals("hollow_bell") || id.equals("stitched_rook");
        Map<PuppetAnim, AnimationDefinition> map = new EnumMap<>(PuppetAnim.class);
        for (PuppetAnim anim : PuppetAnim.values()) {
            map.put(anim, build(id, anim, parts, pivot, flier));
        }
        return map;
    }

    // ---------------------------------------------------------------- roles
    private static Role role(String name) {
        if (name.equals("body")) return Role.BODY;
        if (name.equals("head")) return Role.HEAD;
        if (name.equals("neck")) return Role.NECK;
        if (name.startsWith("arm") || name.startsWith("forearm") || name.startsWith("root_")) return Role.ARM;
        if (name.startsWith("leg")) return Role.LEG;
        if (name.startsWith("wing_tip")) return Role.WING_TIP;
        if (name.startsWith("wing")) return Role.WING;
        if (name.startsWith("seg_")) return Role.SEG;
        if (name.equals("clapper")) return Role.CLAPPER;
        if (name.equals("wheel")) return Role.WHEEL;
        if (name.equals("veil") || name.equals("skirt")) return Role.CLOTH;
        if (name.startsWith("jaw") || name.startsWith("beak")) return Role.JAW;
        if (name.startsWith("antler")) return Role.ANTLER;
        if (name.equals("tail")) return Role.TAIL;
        if (name.equals("abdomen")) return Role.ABDOMEN;
        if (name.equals("cage")) return Role.CAGE;
        if (name.equals("cradle")) return Role.CRADLE;
        return Role.NONE;
    }

    /** +1 for a left (or even-numbered) part, -1 for a right (or odd-numbered) one. */
    private static float sign(String name) {
        char c = name.charAt(name.length() - 1);
        if (Character.isDigit(c)) {
            return (c - '0') % 2 == 0 ? 1.0F : -1.0F;
        }
        return c == 'r' ? -1.0F : 1.0F;
    }

    private static int index(String name) {
        for (int i = name.length() - 1; i >= 0; i--) {
            if (Character.isDigit(name.charAt(i))) {
                return name.charAt(i) - '0';
            }
        }
        return name.length() % 4;
    }

    // ---------------------------------------------------------------- the sampled motion
    private static float sin(double turns) {
        return (float) Math.sin(turns * Math.PI * 2.0D);
    }

    /** {rx, ry, rz, px, py, pz}: degrees and model pixels, at normalised time {@code u} in [0, 1]. */
    private static float[] pose(String id, PuppetAnim anim, String name, float u, float pivot, boolean flier) {
        Role role = role(name);
        float s = sign(name);
        int idx = index(name);
        float[] v = new float[6];
        boolean giant = id.equals("cradle_giant");
        boolean centipede = id.equals("skirted_centipede");
        switch (anim) {
            case IDLE -> {
                switch (role) {
                    case BODY -> v[4] = flier ? -1.6F * sin(u) : -0.5F * sin(u);
                    case HEAD, NECK -> {
                        v[1] = 7.0F * sin(u * 2.0F) * Math.max(0.0F, sin(u)) * (role == Role.NECK ? 0.5F : 1.0F); // the marionette's twitch
                        v[0] = 2.0F * sin(u + 0.25F);
                    }
                    case ARM -> v[0] = 5.0F * sin(u + 0.2F * idx) * s;
                    case LEG -> v[0] = 2.0F * sin(u + 0.5F * idx);
                    case WING -> v[2] = (flier ? 16.0F : 6.0F) * sin(u) * s;
                    case WING_TIP -> v[2] = 10.0F * sin(u - 0.1F) * s;
                    case SEG -> v[0] = 4.0F * sin(u - 0.12F * idx);
                    case CLAPPER -> v[2] = 9.0F * sin(u);
                    case WHEEL -> v[2] = 360.0F * u;
                    case CLOTH -> v[0] = 3.0F * sin(u - 0.1F);
                    case TAIL -> v[1] = 8.0F * sin(u);
                    case CRADLE -> v[4] = -0.5F * sin(u);
                    default -> {
                    }
                }
            }
            case WALK -> {
                switch (role) {
                    case BODY -> {
                        v[4] = -0.9F * Math.abs(sin(u));
                        v[2] = 2.5F * sin(u);
                    }
                    case HEAD, NECK -> v[0] = -4.0F * sin(u * 2.0F);
                    case ARM -> v[0] = -26.0F * sin(u + (idx % 2) * 0.5F) * s;
                    case LEG -> v[0] = 34.0F * sin(u + (idx % 2) * 0.5F + (name.contains("b") && name.length() > 5 ? 0.25F : 0.0F)) * (name.endsWith("l") || idx % 2 == 0 ? 1.0F : -1.0F);
                    case WING -> v[2] = (flier ? 30.0F : 10.0F) * sin(u * 2.0F) * s;
                    case WING_TIP -> v[2] = 20.0F * sin(u * 2.0F - 0.15F) * s;
                    case SEG -> {
                        v[0] = 7.0F * sin(u - 0.15F * idx);
                        v[1] = 9.0F * sin(u - 0.2F * idx);
                    }
                    case CLAPPER -> v[2] = 14.0F * sin(u * 2.0F);
                    case CLOTH -> v[0] = 8.0F;
                    case TAIL -> v[1] = 12.0F * sin(u * 2.0F);
                    case WHEEL -> v[2] = 360.0F * u;
                    default -> {
                    }
                }
            }
            case SLEEP -> { // hanging by the thread from the canopy, swaying
                switch (role) {
                    case BODY -> {
                        v[2] = 5.0F * sin(u);
                        v[0] = 4.0F * sin(u + 0.25F);
                    }
                    case HEAD, NECK -> v[0] = 22.0F;
                    case ARM -> {
                        v[0] = 14.0F + 5.0F * sin(u + 0.1F * idx);
                        v[2] = 6.0F * s;
                    }
                    case LEG -> v[0] = 10.0F + 4.0F * sin(u + 0.2F * idx);
                    case WING -> v[2] = -60.0F * s;
                    case WING_TIP -> v[2] = -40.0F * s;
                    case SEG -> v[0] = 12.0F + 3.0F * sin(u - 0.1F * idx);
                    case CLAPPER -> v[2] = 10.0F * sin(u);
                    case JAW -> v[0] = 18.0F;
                    case WHEEL -> v[2] = 360.0F * u;
                    default -> {
                    }
                }
            }
            case AWAKEN -> {
                float r = 1.0F - u;
                switch (role) {
                    case BODY -> v[0] = -38.0F * r;
                    case HEAD, NECK -> v[0] = 24.0F * r;
                    case ARM -> v[0] = -70.0F * r * s;
                    case LEG -> v[0] = 30.0F * r;
                    case WING -> v[2] = -60.0F * r * s;
                    case SEG -> v[0] = 14.0F * r;
                    default -> {
                    }
                }
            }
            case WINDUP, WINDUP_CRIMSON -> {
                float r = Math.min(1.0F, u * 1.6F);
                float j = anim == PuppetAnim.WINDUP_CRIMSON ? 2.5F * sin(u * 8.0F) : 0.0F; // the taut thread makes it tremble
                switch (role) {
                    case BODY -> {
                        v[0] = -15.0F * r + j;
                        v[4] = 0.8F * r;
                    }
                    case HEAD, NECK -> v[0] = -20.0F * r + j;
                    case ARM -> {
                        v[0] = -88.0F * r + j;
                        v[2] = 22.0F * r * s;
                    }
                    case LEG -> v[0] = (idx % 2 == 0 ? 12.0F : -8.0F) * r;
                    case WING -> v[2] = -(flier ? 55.0F : 25.0F) * r * s;
                    case WING_TIP -> v[2] = -25.0F * r * s;
                    case JAW -> v[0] = 28.0F * r;
                    case SEG -> v[0] = (centipede ? -9.0F : 6.0F) * r * (idx % 2 == 0 ? 1.0F : -1.0F) + j;
                    case ANTLER -> v[0] = -22.0F * r;
                    case CLAPPER -> v[2] = 38.0F * r + j * 3.0F;
                    case CAGE -> v[1] = 40.0F * r * s;
                    case CLOTH -> v[0] = -14.0F * r;
                    case TAIL -> v[0] = -20.0F * r;
                    case WHEEL -> v[2] = 360.0F * u * 2.0F;
                    default -> {
                    }
                }
            }
            case ATTACK_A, ATTACK_B, ATTACK_C -> {
                float k = (float) Math.sin(Math.PI * Math.min(1.0F, u * 1.15F));
                float amp = anim == PuppetAnim.ATTACK_B ? 1.5F : 1.0F;
                boolean spin = anim == PuppetAnim.ATTACK_C;
                switch (role) {
                    case BODY -> {
                        v[0] = (anim == PuppetAnim.ATTACK_B ? 34.0F : 24.0F) * k;
                        v[1] = spin ? 80.0F * k : 0.0F;
                        v[5] = -3.0F * k;
                    }
                    case HEAD, NECK -> v[0] = 22.0F * k;
                    case ARM -> {
                        v[0] = (spin ? -40.0F : 80.0F * amp) * k;
                        v[1] = spin ? 85.0F * k * s : 12.0F * k * s;
                        v[2] = spin ? 0.0F : 10.0F * k * s;
                    }
                    case LEG -> v[0] = 30.0F * k * (idx % 2 == 0 ? 1.0F : -1.0F);
                    case WING -> v[2] = 45.0F * k * s;
                    case WING_TIP -> v[2] = 25.0F * k * s;
                    case JAW -> v[0] = -10.0F * k;
                    case SEG -> v[0] = 14.0F * k * (idx % 2 == 0 ? 1.0F : -1.0F);
                    case ANTLER -> v[0] = 30.0F * k;
                    case CLAPPER -> v[2] = -45.0F * k;
                    case CAGE -> v[1] = 40.0F * s * (1.0F - k);
                    case CLOTH -> v[0] = 18.0F * k;
                    case WHEEL -> v[2] = 720.0F * u;
                    case TAIL -> v[0] = 20.0F * k;
                    default -> {
                    }
                }
            }
            case RECOVER -> {
                float r = 1.0F - u;
                switch (role) {
                    case BODY -> {
                        v[0] = 15.0F * r;
                        v[4] = 0.8F * r;
                    }
                    case HEAD, NECK -> v[0] = 16.0F * r;
                    case ARM -> v[0] = 12.0F * r * s;
                    case LEG -> v[0] = -6.0F * r * (idx % 2 == 0 ? 1.0F : -1.0F);
                    case WING -> v[2] = 30.0F * r * s;
                    case SEG -> v[0] = 8.0F * r;
                    case CLAPPER -> v[2] = 12.0F * r;
                    default -> {
                    }
                }
            }
            case TANGLED -> { // entangled in its own threads: down on its side, flailing; the giant kneels, the cradle lowered
                switch (role) {
                    case BODY -> {
                        if (giant) {
                            v[0] = 42.0F;
                            v[4] = pivot * 0.35F;
                        } else if (flier) {
                            v[2] = 70.0F;
                            v[4] = pivot * 0.5F;
                        } else if (centipede) {
                            v[2] = 12.0F;
                        } else {
                            v[2] = 74.0F;
                            v[4] = pivot * 0.62F;
                        }
                        v[1] = 5.0F * sin(u * 2.0F);
                    }
                    case HEAD, NECK -> v[0] = 28.0F;
                    case ARM -> {
                        v[0] = 24.0F * sin(u * 3.0F + 0.15F * idx);
                        v[2] = 10.0F * s;
                    }
                    case LEG -> v[0] = giant ? -78.0F : 22.0F * sin(u * 3.0F + 0.2F * idx);
                    case WING -> v[2] = 30.0F * sin(u * 3.0F) * s;
                    case SEG -> {
                        v[0] = 38.0F;
                        v[2] = 20.0F * sin(u * 2.0F - 0.2F * idx);
                    }
                    case CLAPPER -> v[2] = 28.0F * sin(u * 3.0F);
                    case CAGE -> v[1] = 55.0F * s;
                    case CLOTH -> v[0] = 20.0F;
                    case WHEEL -> v[2] = 360.0F * u;
                    default -> {
                    }
                }
            }
            case DEATH -> { // the strings are cut: the body folds and every limb goes slack
                float c = u * u;
                switch (role) {
                    case BODY -> {
                        v[0] = 82.0F * c;
                        v[4] = pivot * 0.8F * c;
                    }
                    case HEAD, NECK -> v[0] = 42.0F * c;
                    case ARM -> {
                        v[0] = 55.0F * c * s;
                        v[2] = 30.0F * c * s;
                    }
                    case LEG -> v[0] = -34.0F * c * (idx % 2 == 0 ? 1.0F : -1.0F);
                    case WING -> v[2] = -85.0F * c * s;
                    case WING_TIP -> v[2] = -50.0F * c * s;
                    case SEG -> v[0] = 32.0F * c * (1.0F + 0.2F * idx);
                    case CLAPPER -> v[2] = 55.0F * c;
                    case ANTLER -> v[0] = 40.0F * c;
                    case JAW -> v[0] = 30.0F * c;
                    case CLOTH -> v[0] = 30.0F * c;
                    default -> {
                    }
                }
            }
            case TAUNT -> {
                float k = (float) Math.sin(Math.PI * u);
                switch (role) {
                    case BODY -> v[0] = -9.0F * k;
                    case HEAD, NECK -> v[0] = -26.0F * k;
                    case ARM -> v[0] = -105.0F * k * s;
                    case WING -> v[2] = -(flier ? 50.0F : 25.0F) * k * s;
                    case JAW -> v[0] = 22.0F * k;
                    case WHEEL -> v[2] = 720.0F * u;
                    default -> {
                    }
                }
            }
        }
        return v;
    }

    private static AnimationDefinition build(String id, PuppetAnim anim, List<String> parts, float pivot, boolean flier) {
        float len = length(anim);
        AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(len);
        if (anim.loops) {
            b.looping();
        }
        for (String part : parts) {
            Keyframe[] rot = new Keyframe[SAMPLES + 1];
            Keyframe[] pos = new Keyframe[SAMPLES + 1];
            boolean anyRot = false;
            boolean anyPos = false;
            boolean linear = role(part) == Role.WHEEL;
            AnimationChannel.Interpolation interp = linear ? AnimationChannel.Interpolations.LINEAR : AnimationChannel.Interpolations.CATMULLROM;
            for (int i = 0; i <= SAMPLES; i++) {
                float u = i / (float) SAMPLES;
                float[] v = pose(id, anim, part, u, pivot, flier);
                float t = len * u;
                rot[i] = new Keyframe(t, KeyframeAnimations.degreeVec(v[0], v[1], v[2]), interp);
                pos[i] = new Keyframe(t, KeyframeAnimations.posVec(v[3], v[4], v[5]), interp);
                anyRot |= Math.abs(v[0]) + Math.abs(v[1]) + Math.abs(v[2]) > 1.0E-3F;
                anyPos |= Math.abs(v[3]) + Math.abs(v[4]) + Math.abs(v[5]) > 1.0E-3F;
            }
            if (anyRot) {
                b.addAnimation(part, new AnimationChannel(AnimationChannel.Targets.ROTATION, rot));
            }
            if (anyPos) {
                b.addAnimation(part, new AnimationChannel(AnimationChannel.Targets.POSITION, pos));
            }
        }
        return b.build();
    }
}
