package com.codenamecoraline.client.ten_threads_witch.matriarch;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatAnim;
import java.util.List;
import java.util.Map;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/**
 * One baked clip of the Matriarch: a vanilla {@link AnimationDefinition} (the keyframes, with vanilla or {@link Ease}
 * interpolations) plus, per track, the priority and the blend mode used when it is composed with the other layers. It is
 * sampled with the vanilla algorithm (binary search of the keyframe, the end keyframe's interpolation) but into a pose
 * buffer instead of straight into the model parts, so that layers can be weighed joint by joint.
 */
public final class MatClip {
    public final MatAnim id;
    public final AnimationDefinition definition;
    private final int[] joint;
    private final int[] target;
    private final Keyframe[][] keys;
    private final float[] priority;
    private final boolean[] additive;
    private final Vector3f scratch = new Vector3f();

    MatClip(MatAnim id, AnimationDefinition definition, Map<String, Float> priorities, java.util.Set<String> additiveJoints, float basePriority, boolean allAdditive) {
        this.id = id;
        this.definition = definition;
        int n = 0;
        for (List<AnimationChannel> list : definition.boneAnimations().values()) {
            n += list.size();
        }
        this.joint = new int[n];
        this.target = new int[n];
        this.keys = new Keyframe[n][];
        this.priority = new float[n];
        this.additive = new boolean[n];
        int i = 0;
        for (Map.Entry<String, List<AnimationChannel>> e : definition.boneAnimations().entrySet()) {
            int j = MatRig.index(e.getKey());
            for (AnimationChannel c : e.getValue()) {
                this.joint[i] = j;
                this.target[i] = c.target() == AnimationChannel.Targets.ROTATION ? MatRig.ROT : c.target() == AnimationChannel.Targets.POSITION ? MatRig.POS : MatRig.SCALE;
                this.keys[i] = c.keyframes();
                this.priority[i] = priorities.getOrDefault(e.getKey(), basePriority);
                this.additive[i] = allAdditive || additiveJoints.contains(e.getKey());
                i++;
            }
        }
    }

    public float lengthSeconds() {
        return this.definition.lengthInSeconds();
    }

    public boolean loops() {
        return this.definition.looping();
    }

    public int trackCount() {
        return this.joint.length;
    }

    /** True when some track of this clip moves {@code jointIndex}. */
    public boolean touches(int jointIndex) {
        for (int j : this.joint) {
            if (j == jointIndex) {
                return true;
            }
        }
        return false;
    }

    /**
     * Samples the clip at {@code seconds} into {@code out}: for every channel it moves, the value goes to
     * {@code out.value[channel * 3 ..]} and its priority and mode to the side arrays. {@code amplitude} scales rotations
     * (her mood widening or shrinking a gesture); positions, scales and effect channels are left as authored.
     */
    public void sample(float seconds, float amplitude, PoseSample out) {
        float t = this.loops() ? (seconds % this.lengthSeconds() + this.lengthSeconds()) % this.lengthSeconds() : Mth.clamp(seconds, 0.0F, this.lengthSeconds());
        for (int i = 0; i < this.joint.length; i++) {
            Keyframe[] k = this.keys[i];
            // the vanilla search (first keyframe at or after t, minus one), without allocating a predicate per track
            int lo = 0;
            int hi = k.length;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (t <= k[mid].timestamp()) {
                    hi = mid;
                } else {
                    lo = mid + 1;
                }
            }
            int prev = Math.max(0, lo - 1);
            int next = Math.min(k.length - 1, prev + 1);
            float alpha = next != prev ? Mth.clamp((t - k[prev].timestamp()) / (k[next].timestamp() - k[prev].timestamp()), 0.0F, 1.0F) : 0.0F;
            k[next].interpolation().apply(this.scratch, alpha, k, prev, next, 1.0F);
            int ch = this.joint[i] * 3 + this.target[i];
            float amp = this.target[i] == MatRig.ROT && !MatRig.isVirtual(this.joint[i]) ? amplitude : 1.0F;
            out.put(ch, this.scratch.x() * amp, this.scratch.y() * amp, this.scratch.z() * amp, this.priority[i], this.additive[i]);
        }
    }
}
