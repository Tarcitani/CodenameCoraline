package com.codenamecoraline.client.ten_threads_witch.matriarch;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatAnim;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3f;

/**
 * The authoring language of the Matriarch's clips. Keys are written in ticks with authored units (degrees, model pixels with
 * up positive, scale factors) and become a vanilla {@link AnimationDefinition}. A track is
 * {@code rot(joint, ease, t, x, y, z, t, x, y, z, ...)}; {@code fingers(side, ease, t, index, middle, ring, little, thumb,
 * spread, ...)} writes the five fingers of a hand from flexion angles (positive closes the hand), so a clip reads as the
 * gesture it is. Values are deltas on the rest pose of the rig.
 *
 * <p>Axis conventions of the rig (left side; the right mirrors y and z): arms and legs swing forward with negative x and
 * open outwards with negative z; the chest and the head lean forward (nod) with positive x and turn with y; a brow tilts
 * its outer end up (anger) with negative z; a mouth corner rises (smile) with negative z.
 */
public final class ClipBuilder {
    private record Track(String joint, int target, float[] times, float[][] values, AnimationChannel.Interpolation[] interps) {}

    private final MatAnim id;
    private final float seconds;
    private final boolean loop;
    private final List<Track> tracks = new ArrayList<>();
    private final Map<String, Float> priority = new HashMap<>();
    private final Set<String> additive = new HashSet<>();

    private ClipBuilder(MatAnim id) {
        this.id = id;
        this.seconds = id.ticks / 20.0F;
        this.loop = id.loops();
    }

    public static ClipBuilder clip(MatAnim id) {
        return new ClipBuilder(id);
    }

    private ClipBuilder track(String joint, int target, AnimationChannel.Interpolation ease, int stride, float[] k) {
        if (!MatRig.has(joint)) {
            throw new IllegalArgumentException(this.id.code + ": unknown joint " + joint);
        }
        if (k.length % stride != 0) {
            throw new IllegalArgumentException(this.id.code + "/" + joint + ": keys must come in groups of " + stride);
        }
        int n = k.length / stride;
        float[] times = new float[n];
        float[][] values = new float[n][3];
        AnimationChannel.Interpolation[] interps = new AnimationChannel.Interpolation[n];
        for (int i = 0; i < n; i++) {
            times[i] = k[i * stride] / 20.0F;
            for (int c = 0; c < 3; c++) {
                values[i][c] = stride > c + 1 ? k[i * stride + 1 + c] : 0.0F;
            }
            interps[i] = ease;
        }
        this.tracks.removeIf(t -> t.joint().equals(joint) && t.target() == target);
        this.tracks.add(new Track(joint, target, times, values, interps));
        return this;
    }

    /** Rotation keys in degrees: t, x, y, z, ... */
    public ClipBuilder rot(String joint, AnimationChannel.Interpolation ease, float... k) {
        return this.track(joint, MatRig.ROT, ease, 4, k);
    }

    public ClipBuilder rot(String joint, float... k) {
        return this.rot(joint, Ease.SMOOTH, k);
    }

    /** Position keys in model pixels, up positive: t, x, y, z, ... */
    public ClipBuilder pos(String joint, AnimationChannel.Interpolation ease, float... k) {
        return this.track(joint, MatRig.POS, ease, 4, k);
    }

    public ClipBuilder pos(String joint, float... k) {
        return this.pos(joint, Ease.SMOOTH, k);
    }

    /** Scale keys as factors (1 = unchanged): t, x, y, z, ... */
    public ClipBuilder scale(String joint, AnimationChannel.Interpolation ease, float... k) {
        return this.track(joint, MatRig.SCALE, ease, 4, k);
    }

    public ClipBuilder scale(String joint, float... k) {
        return this.scale(joint, Ease.SMOOTH, k);
    }

    /** Keys of an effect channel (see {@link MatRig}): t, x, y, z, ... taken as they are. */
    public ClipBuilder fx(String fxJoint, AnimationChannel.Interpolation ease, float... k) {
        return this.track(fxJoint, MatRig.POS, ease, 4, k);
    }

    public ClipBuilder fx(String fxJoint, float... k) {
        return this.fx(fxJoint, Ease.LINEAR, k);
    }

    /**
     * The fingers of one hand ('l' or 'r'): t, index, middle, ring, little, thumb, spread, ... Flexion in degrees closes
     * the finger towards the palm (the right hand rests closed round the staff, so negative values open it); spread fans
     * the four fingers apart (degrees between neighbours).
     */
    public ClipBuilder fingers(char side, AnimationChannel.Interpolation ease, float... k) {
        if (k.length % 7 != 0) {
            throw new IllegalArgumentException(this.id.code + ": finger keys come in groups of 7");
        }
        int n = k.length / 7;
        for (int f = 0; f < 5; f++) {
            float[] r = new float[n * 4];
            for (int i = 0; i < n; i++) {
                float flex = k[i * 7 + 1 + f];
                float spread = f < 4 ? k[i * 7 + 6] * (f - 1.5F) : 0.0F;
                r[i * 4] = k[i * 7];
                float[] v = fingerRot(side, f, flex, spread, new float[3]);
                r[i * 4 + 1] = v[0];
                r[i * 4 + 2] = v[1];
                r[i * 4 + 3] = v[2];
            }
            String joint = f < 4 ? "finger_" + side + f : "thumb_" + side;
            this.rot(joint, ease, r);
        }
        return this;
    }

    public ClipBuilder fingers(char side, float... k) {
        return this.fingers(side, Ease.SMOOTH, k);
    }

    /** Rotation (degrees) of finger {@code f} (4 = thumb) of a hand for a flexion and a spread. */
    static float[] fingerRot(char side, int f, float flex, float spread, float[] out) {
        if (side == 'l') {
            out[0] = f < 4 ? spread : flex * 0.5F;
            out[1] = 0.0F;
            out[2] = f < 4 ? flex : flex * 0.7F;
        } else {
            out[0] = f < 4 ? -flex : -flex * 0.6F;
            out[1] = 0.0F;
            out[2] = f < 4 ? spread : flex * 0.5F;
        }
        return out;
    }

    /** Overrides the composition priority of these joints' tracks (default: the clip's priority). */
    public ClipBuilder prio(float p, String... joints) {
        for (String j : joints) {
            this.priority.put(j, p);
        }
        return this;
    }

    /** These joints' tracks are added to the pose instead of replacing it. */
    public ClipBuilder additive(String... joints) {
        this.additive.addAll(List.of(joints));
        return this;
    }

    public MatClip build() {
        return this.make(false);
    }

    /** The same clip on the other side of the body: joints swapped, y and z rotations and x positions negated. */
    public MatClip buildMirrored() {
        return this.make(true);
    }

    private MatClip make(boolean mirror) {
        AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(this.seconds);
        if (this.loop) {
            b.looping();
        }
        Map<String, Float> prio = new HashMap<>();
        Set<String> add = new HashSet<>();
        for (Track t : this.tracks) {
            String joint = mirror ? MatRig.mirror(t.joint()) : t.joint();
            Keyframe[] keys = new Keyframe[t.times().length];
            for (int i = 0; i < keys.length; i++) {
                float[] v = t.values()[i];
                float x = v[0];
                float y = v[1];
                float z = v[2];
                if (mirror) {
                    if (t.target() == MatRig.ROT && MatRig.isFinger(t.joint())) {
                        boolean fromLeft = t.joint().endsWith("_l") || t.joint().startsWith("finger_l");
                        float nx = fromLeft ? -z : z;
                        float nz = fromLeft ? x : -x;
                        x = nx;
                        z = nz;
                    } else if (t.target() == MatRig.ROT) {
                        y = -y;
                        z = -z;
                    } else if (t.target() == MatRig.POS && !MatRig.isVirtual(MatRig.index(t.joint()))) {
                        x = -x;
                    }
                }
                Vector3f vec = switch (t.target()) {
                    case MatRig.ROT -> KeyframeAnimations.degreeVec(x, y, z);
                    case MatRig.SCALE -> KeyframeAnimations.scaleVec(x, y, z);
                    default -> MatRig.isVirtual(MatRig.index(joint)) ? new Vector3f(x, y, z) : KeyframeAnimations.posVec(x, y, z);
                };
                keys[i] = new Keyframe(t.times()[i], vec, t.interps()[i]);
            }
            AnimationChannel.Target target = t.target() == MatRig.ROT ? AnimationChannel.Targets.ROTATION
                    : t.target() == MatRig.SCALE ? AnimationChannel.Targets.SCALE : AnimationChannel.Targets.POSITION;
            b.addAnimation(joint, new AnimationChannel(target, keys));
            if (this.priority.containsKey(t.joint())) {
                prio.put(joint, this.priority.get(t.joint()));
            }
            if (this.additive.contains(t.joint())) {
                add.add(joint);
            }
        }
        return new MatClip(this.id, b.build(), prio, add, this.id.priority, this.id.has(MatAnim.Flags.ADD));
    }
}
