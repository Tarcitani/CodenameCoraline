package com.codenamecoraline.client.ten_threads_witch.matriarch;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.Keyframe;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Interpolation curves for the Matriarch's keyframes, written against the vanilla {@link AnimationChannel.Interpolation}
 * interface (the curve of a segment belongs to its end keyframe, as in vanilla). LINEAR and CATMULLROM are the vanilla
 * ones; the others ease the segment between its two keys: SMOOTH in and out (deliberate, elegant gestures), IN (a slow
 * start, a heavy blow), OUT (a quick start that settles, recoveries), BACK (overshoots a little and returns, a whip or a
 * snap), SNAP (holds, then jumps at the end: the unnatural, puppet-like motion of her later stages), STEP (no blend at all).
 */
public final class Ease {
    private Ease() {}

    public static final AnimationChannel.Interpolation LINEAR = AnimationChannel.Interpolations.LINEAR;
    public static final AnimationChannel.Interpolation CATMULL = AnimationChannel.Interpolations.CATMULLROM;
    public static final AnimationChannel.Interpolation SMOOTH = of(t -> t * t * (3.0F - 2.0F * t));
    public static final AnimationChannel.Interpolation IN = of(t -> t * t * t);
    public static final AnimationChannel.Interpolation OUT = of(t -> 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t));
    public static final AnimationChannel.Interpolation BACK = of(t -> {
        float c = 1.70158F;
        float u = t - 1.0F;
        return 1.0F + (c + 1.0F) * u * u * u + c * u * u;
    });
    public static final AnimationChannel.Interpolation SNAP = of(t -> t < 0.7F ? t * 0.15F : 0.105F + (t - 0.7F) / 0.3F * 0.895F);
    public static final AnimationChannel.Interpolation STEP = of(t -> t < 1.0F ? 0.0F : 1.0F);

    /** A curve as an interpolation between the previous and the next keyframe. */
    public static AnimationChannel.Interpolation of(Curve curve) {
        return (Vector3f vector, float alpha, Keyframe[] keyframes, int prev, int next, float targetScale) -> {
            Vector3fc a = keyframes[prev].postTarget();
            Vector3fc b = keyframes[next].preTarget();
            float k = curve.apply(alpha);
            return vector.set(a.x() + (b.x() - a.x()) * k, a.y() + (b.y() - a.y()) * k, a.z() + (b.z() - a.z()) * k).mul(targetScale);
        };
    }

    @FunctionalInterface
    public interface Curve {
        float apply(float t);
    }
}
