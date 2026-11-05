package com.codenamecoraline.client.ten_threads_witch.matriarch;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatAnim;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatSlot;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Every baked clip of the Matriarch, by catalogue entry: the authored clip, its mirror (a one-hand gesture played by the
 * right hand, a dodge to the other side), and for falls and recoveries a face-down version. The keyframes live in one class
 * per category of the specification ({@link IdleClips}, {@link LocomotionClips}, {@link ThreadClips}, {@link CombatClips},
 * {@link ReactionClips}, {@link FaceClips}, {@link CinematicClips}); procedural entries (gaze, fingers, cloth, hair) have no
 * keyframes and get an empty clip.
 */
public final class MatClips {
    private static MatClips instance;

    private final Map<MatAnim, MatClip[]> clips = new EnumMap<>(MatAnim.class);

    private MatClips() {
        Consumer<ClipBuilder> add = this::add;
        Consumer<ClipBuilder> prone = this::addProne;
        IdleClips.register(add);
        LocomotionClips.register(add);
        ThreadClips.register(add);
        CombatClips.register(add);
        ReactionClips.register(add, prone);
        FaceClips.register(add);
        CinematicClips.register(add);
    }

    public static synchronized MatClips get() {
        if (instance == null) {
            instance = new MatClips();
        }
        return instance;
    }

    private void add(ClipBuilder b) {
        MatClip normal = b.build();
        MatClip[] v = this.clips.computeIfAbsent(normal.id, k -> new MatClip[4]);
        v[0] = normal;
        v[1] = b.buildMirrored();
    }

    private void addProne(ClipBuilder b) {
        MatClip normal = b.build();
        MatClip[] v = this.clips.computeIfAbsent(normal.id, k -> new MatClip[4]);
        v[2] = normal;
        v[3] = b.buildMirrored();
    }

    /** True when the entry has keyframes of its own (every entry except the procedural layers). */
    public boolean has(MatAnim a) {
        return this.clips.containsKey(a);
    }

    public boolean hasProne(MatAnim a) {
        MatClip[] v = this.clips.get(a);
        return v != null && v[2] != null;
    }

    public MatClip get(MatAnim a, boolean mirror, boolean prone) {
        MatClip[] v = this.clips.get(a);
        if (v == null) {
            return empty(a);
        }
        int i = (mirror ? 1 : 0) + (prone && v[2] != null ? 2 : 0);
        return v[i] != null ? v[i] : v[0];
    }

    private final Map<MatAnim, MatClip> empty = new EnumMap<>(MatAnim.class);

    private MatClip empty(MatAnim a) {
        return this.empty.computeIfAbsent(a, k -> ClipBuilder.clip(k).build());
    }

    /** Entries that must have keyframes: everything that is not a procedural layer. */
    public static boolean authored(MatAnim a) {
        return a.slot != MatSlot.PROCEDURAL;
    }

    // ---------------------------------------------------------------- shared poses of the hands
    /** The marionettist's hand: raised before her at chest height, palm down, fingers forward and loosely curled. */
    public static final float[] MANIP_ARM = {-30, 0, -8};
    public static final float[] MANIP_FOREARM = {-55, 0, 0};
    public static final float[] MANIP_HAND = {0, 90, 0};
    public static final float[] MANIP_FINGERS = {20, 25, 25, 30, 15, 2};

    /** Keys holding a three-value pose from {@code t0} to {@code t1}. */
    static float[] hold(float t0, float t1, float[] v) {
        return new float[] {t0, v[0], v[1], v[2], t1, v[0], v[1], v[2]};
    }

    /** Keys holding the finger pose {@code f} (5 flexions and a spread) from {@code t0} to {@code t1}. */
    static float[] holdFingers(float t0, float t1, float[] f) {
        return new float[] {t0, f[0], f[1], f[2], f[3], f[4], f[5], t1, f[0], f[1], f[2], f[3], f[4], f[5]};
    }

    /** Holds the left arm in the marionettist's pose for the whole clip (a gesture that only moves the fingers). */
    static ClipBuilder manipArm(ClipBuilder c, float length) {
        return c.rot("arm_l", Ease.CATMULL, hold(0, length, MANIP_ARM))
                .rot("forearm_l", Ease.CATMULL, hold(0, length, MANIP_FOREARM))
                .rot("hand_l", Ease.CATMULL, hold(0, length, MANIP_HAND));
    }
}
