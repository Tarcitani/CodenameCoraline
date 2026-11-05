package com.codenamecoraline.client.ten_threads_witch.matriarch;

import static com.codenamecoraline.client.ten_threads_witch.matriarch.ClipBuilder.clip;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatAnim;
import java.util.function.Consumer;

/**
 * 6.8 Hit reactions (MAT-HIT-01 to 08, added on top of the pose: a blow never erases the gesture of her hands), 6.9 stun
 * (MAT-STUN-01 to 05) and 6.10 falls and recoveries (MAT-FALL-01 to 09). A fall ends in one of two ground poses, on her back
 * or face down; the impact and every recovery exist in both versions (the variant chooses), so a recovery always starts
 * from the pose the fall left her in. Quick and slow versions are the same keys played faster or slower.
 */
final class ReactionClips {
    private ReactionClips() {}

    // the two ground poses: {body rot, body pos, chest, head, arm_l, arm_r, legs, feet}
    private static final float[][] SUPINE = {{-90, 0, 0}, {0, -5, 2}, {-4, 0, 0}, {25, 0, 0}, {-30, 0, -60}, {-30, 0, 60}, {-8, 0, 0}, {-20, 0, 0}};
    private static final float[][] PRONE = {{88, 0, 0}, {0, -5.5F, -1}, {0, 0, 0}, {-35, 0, 8}, {-160, 0, -25}, {-160, 0, 25}, {5, 0, 0}, {40, 0, 0}};

    /** Keys that start at the ground pose {@code part} of {@code prone} and go on with {@code rest}. */
    private static float[] from(boolean prone, int part, float... rest) {
        float[] g = (prone ? PRONE : SUPINE)[part];
        float[] k = new float[4 + rest.length];
        k[1] = g[0];
        k[2] = g[1];
        k[3] = g[2];
        System.arraycopy(rest, 0, k, 4, rest.length);
        return k;
    }

    /** Keys holding the ground pose {@code part} at {@code t0} and {@code t1}, with {@code mid} in between. */
    private static float[] ground(boolean prone, int part, float t0, float t1) {
        float[] g = (prone ? PRONE : SUPINE)[part];
        return new float[] {t0, g[0], g[1], g[2], t1, g[0], g[1], g[2]};
    }

    static void register(Consumer<ClipBuilder> add, Consumer<ClipBuilder> addProne) {
        // ---- hit reactions (additive)
        // MAT-HIT-01: a light blow: the chest and the head give a little
        add.accept(clip(MatAnim.HIT_01)
                .rot("chest", Ease.OUT, 0, 0, 0, 0, 2, -5, 0, 0, 8, 0, 0, 0)
                .rot("head", Ease.OUT, 0, 0, 0, 0, 2, -6, 0, 0, 8, 0, 0, 0));
        // MAT-HIT-02: a moderate blow: the chest, the shoulders and the head turn away from it
        add.accept(clip(MatAnim.HIT_02)
                .rot("chest", Ease.OUT, 0, 0, 0, 0, 2, -10, 6, 0, 12, 0, 0, 0)
                .rot("head", Ease.OUT, 0, 0, 0, 0, 2, -12, -8, 5, 12, 0, 0, 0)
                .rot("arm_l", Ease.OUT, 0, 0, 0, 0, 2, 0, 0, -8, 12, 0, 0, 0)
                .rot("arm_r", Ease.OUT, 0, 0, 0, 0, 2, 0, 0, 6, 12, 0, 0, 0)
                .pos("mantle", Ease.OUT, 0, 0, 0, 0, 2, 0, 0.3F, 0, 12, 0, 0, 0));
        // MAT-HIT-03: a heavy blow: pushed back off balance, the arms thrown out, a step to catch herself
        add.accept(clip(MatAnim.HIT_03)
                .rot("body", Ease.OUT, 0, 0, 0, 0, 2, -14, 0, 0, 8, -8, 0, 4, 20, 0, 0, 0)
                .pos("body", Ease.OUT, 0, 0, 0, 0, 2, 0, 0, 1, 20, 0, 0, 0)
                .rot("chest", Ease.OUT, 0, 0, 0, 0, 2, -16, 10, 0, 12, 4, 0, 0, 20, 0, 0, 0)
                .rot("head", Ease.OUT, 0, 0, 0, 0, 2, -20, 0, 10, 20, 0, 0, 0)
                .rot("arm_l", Ease.OUT, 0, 0, 0, 0, 2, -30, 0, -30, 20, 0, 0, 0)
                .rot("arm_r", Ease.OUT, 0, 0, 0, 0, 2, -20, 0, 25, 20, 0, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 4, 12, 0, 0, 14, 0, 0, 0));
        // MAT-HIT-04: a blow from behind: she lurches forward, the head whips back, the arms fly behind her
        add.accept(clip(MatAnim.HIT_04)
                .rot("body", Ease.OUT, 0, 0, 0, 0, 2, 12, 0, 0, 14, 0, 0, 0)
                .pos("body", Ease.OUT, 0, 0, 0, 0, 2, 0, 0, -0.8F, 14, 0, 0, 0)
                .rot("chest", Ease.OUT, 0, 0, 0, 0, 2, 14, 0, 0, 14, 0, 0, 0)
                .rot("head", Ease.OUT, 0, 0, 0, 0, 2, -10, 0, 0, 6, 8, 0, 0, 14, 0, 0, 0)
                .rot("arm_l", Ease.OUT, 0, 0, 0, 0, 2, 20, 0, -15, 14, 0, 0, 0)
                .rot("arm_r", Ease.OUT, 0, 0, 0, 0, 2, 15, 0, 12, 14, 0, 0, 0));
        // MAT-HIT-05: hit while manipulating: only the chest and the head react, the arms and the threads stay where they are
        add.accept(clip(MatAnim.HIT_05)
                .rot("chest", Ease.OUT, 0, 0, 0, 0, 2, -8, -6, 3, 10, 0, 0, 0)
                .rot("head", Ease.OUT, 0, 0, 0, 0, 2, -10, 6, -4, 10, 0, 0, 0)
                .pos("body", Ease.OUT, 0, 0, 0, 0, 2, 0, 0, 0.4F, 10, 0, 0, 0));
        // MAT-HIT-06: hit during an attack: a small shudder of the torso; the blow under way goes on
        add.accept(clip(MatAnim.HIT_06)
                .rot("chest", Ease.OUT, 0, 0, 0, 0, 2, -4, 3, 0, 8, 0, 0, 0)
                .rot("head", Ease.OUT, 0, 0, 0, 0, 2, -5, 0, 3, 8, 0, 0, 0));
        // MAT-HIT-07: knocked off balance: the body rolls, the legs and arms correct, one way then the other
        add.accept(clip(MatAnim.HIT_07)
                .rot("body", 0, 0, 0, 0, 2, 0, 0, 10, 8, 0, 0, -6, 14, 0, 0, 2, 18, 0, 0, 0)
                .pos("body", 0, 0, 0, 0, 4, 0.6F, 0, 0, 18, 0, 0, 0)
                .rot("chest", 0, 0, 0, 0, 2, 0, 0, -8, 10, 0, 0, 4, 18, 0, 0, 0)
                .rot("leg_l", 0, 0, 0, 0, 4, 0, 0, -14, 12, 0, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 8, 0, 0, 10, 16, 0, 0, 0)
                .rot("arm_l", 0, 0, 0, 0, 3, 0, 0, -40, 10, 0, 0, -15, 18, 0, 0, 0)
                .rot("arm_r", 0, 0, 0, 0, 3, 0, 0, 30, 18, 0, 0, 0));
        // MAT-HIT-08: a moment of lost coordination: head, arms and fingers jerk out of step with one another
        add.accept(clip(MatAnim.HIT_08)
                .rot("head", Ease.STEP, 0, 0, 0, 0, 2, 8, 0, -12, 5, 8, 0, -12, 6, -4, 10, 6, 12, -4, 10, 6, 13, 3, -6, 0, 20, 3, -6, 0, 26, 0, 0, 0)
                .rot("arm_l", Ease.SNAP, 0, 0, 0, 0, 3, 10, 0, -10, 9, -12, 0, 6, 15, 8, 0, -8, 26, 0, 0, 0)
                .rot("arm_r", Ease.SNAP, 0, 0, 0, 0, 4, -8, 0, 8, 11, 12, 0, -6, 26, 0, 0, 0)
                .rot("forearm_l", Ease.SNAP, 0, 0, 0, 0, 5, 18, 0, 0, 12, -15, 0, 0, 26, 0, 0, 0)
                .fingers('l', Ease.STEP, 0, 0, 0, 0, 0, 0, 0, 3, 30, -20, 25, -10, 15, 0, 9, -20, 30, -15, 30, -10, 0, 16, 25, 0, 30, -20, 10, 0, 26, 0, 0, 0, 0, 0, 0)
                .fingers('r', Ease.STEP, 0, 0, 0, 0, 0, 0, 0, 4, -25, 15, -20, 10, 0, 0, 12, 20, -25, 15, -20, 0, 0, 26, 0, 0, 0, 0, 0, 0));

        // ---- stun
        // MAT-STUN-01: a brief interruption: a freeze and a small stagger, the fingers twitching open
        add.accept(clip(MatAnim.STUN_01)
                .rot("chest", Ease.OUT, 0, 0, 0, 0, 2, -6, 0, 4, 6, -4, 0, 3, 10, 0, 0, 0)
                .rot("head", Ease.OUT, 0, 0, 0, 0, 2, -10, 0, -8, 10, 0, 0, 0)
                .rot("arm_l", 0, 0, 0, 0, 2, -20, 0, -14, 10, -10, 0, -6)
                .rot("arm_r", 0, 0, 0, 0, 2, -15, 0, 10, 10, 0, 0, 0)
                .fingers('l', Ease.OUT, 0, 0, 0, 0, 0, 0, 0, 2, -15, -15, -15, -15, -10, 8, 10, 0, 0, 0, 0, 0, 0)
                .pos("body", 0, 0, 0, 0, 2, 0, -0.3F, 0, 10, 0, 0, 0));
        // MAT-STUN-02: losing her balance: the body sways one way and the other, the arms wheel, the feet search for ground
        add.accept(clip(MatAnim.STUN_02)
                .rot("body", 0, 0, 0, 0, 3, -10, 0, 12, 8, 6, 0, -14, 13, -4, 0, 6, 18, 0, 0, 0)
                .rot("leg_l", 0, 0, 0, 0, 3, 0, 0, -18, 8, -12, 0, 6, 13, 6, 0, -4, 18, 0, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 5, 10, 0, 14, 10, 0, 0, -8, 18, 0, 0, 0)
                .rot("arm_l", 0, 0, 0, 0, 3, -60, 0, -50, 8, -20, 0, -70, 13, -50, 0, -30, 18, -20, 0, -10)
                .rot("arm_r", 0, 0, 0, 0, 3, -40, 0, 60, 8, -70, 0, 30, 18, -15, 0, 8)
                .rot("head", 0, 0, 0, 0, 3, -10, 0, 15, 8, 8, 0, -12, 18, 0, 0, 0)
                .pos("body", 0, 0, 0, 0, 3, 0, -0.5F, 0, 8, 0, -0.8F, 0, 18, 0, -0.3F, 0));
        // MAT-STUN-03: spasms: involuntary jerks, held for a beat, in directions that make no sense
        add.accept(clip(MatAnim.STUN_03)
                .rot("chest", Ease.STEP, 0, 0, 0, 0, 2, 14, -10, 8, 4, 14, -10, 8, 5, -12, 12, -10, 9, -12, 12, -10, 10, 20, 0, 14, 15, 20, 0, 14, 16, -6, -15, -6, 21, -6, -15, -6, 24, 0, 0, 0)
                .rot("head", Ease.STEP, 0, 0, 0, 0, 2, 20, 0, -30, 5, -25, 15, 25, 10, 10, -20, -40, 16, -15, 25, 20, 24, 0, 0, 0)
                .rot("arm_l", Ease.STEP, 0, 0, 0, 0, 2, -40, 0, -60, 5, 30, 0, -10, 10, -80, 0, -20, 16, 10, 0, -50, 24, 0, 0, 0)
                .rot("arm_r", Ease.STEP, 0, 0, 0, 0, 2, 20, 0, 40, 5, -70, 0, 20, 10, 10, 0, 60, 16, -50, 0, 10, 24, 0, 0, 0)
                .fingers('l', Ease.STEP, 0, 0, 0, 0, 0, 0, 0, 2, 60, -20, 60, -20, 30, 0, 5, -20, 60, -20, 60, -10, 10, 10, 60, 60, 60, 60, 40, 0, 16, -20, -20, -20, -20, -10, 12, 24, 0, 0, 0, 0, 0, 0)
                .pos("body", Ease.STEP, 0, 0, 0, 0, 5, 0, -0.6F, 0, 10, 0, 0.4F, 0, 16, 0, -0.3F, 0, 24, 0, 0, 0));
        // MAT-STUN-04: the long stun (exhausted, tangled in her own threads): on her knees, slumped, swaying, twitching now and then
        add.accept(clip(MatAnim.STUN_04)
                .pos("body", Ease.CATMULL, 0, 0, -3.5F, 0, 20, 0, -3.6F, 0, 40, 0, -3.5F, 0)
                .rot("leg_l", Ease.CATMULL, 0, 85, 0, 0, 40, 85, 0, 0)
                .rot("leg_r", Ease.CATMULL, 0, 85, 0, 0, 40, 85, 0, 0)
                .rot("foot_l", Ease.CATMULL, 0, -10, 0, 0, 40, -10, 0, 0)
                .rot("foot_r", Ease.CATMULL, 0, -10, 0, 0, 40, -10, 0, 0)
                .rot("chest", Ease.SNAP, 0, 30, 0, 0, 18, 28, 0, 3, 20, 24, 0, 3, 22, 34, 0, -2, 30, 32, 0, 0, 40, 30, 0, 0)
                .rot("head", Ease.CATMULL, 0, 25, 6, 0, 10, 28, 0, 4, 20, 25, -6, 0, 30, 28, 0, -4, 40, 25, 6, 0)
                .rot("arm_l", Ease.SNAP, 0, -15, 0, -8, 26, -15, 0, -8, 28, -15, 0, -30, 30, -15, 0, -8, 40, -15, 0, -8)
                .rot("arm_r", Ease.CATMULL, 0, -10, 0, 6, 40, -10, 0, 6)
                .rot("forearm_l", Ease.CATMULL, 0, -10, 0, 0, 40, -10, 0, 0)
                .fingers('l', Ease.SNAP, 0, 10, 10, 10, 10, 5, 0, 12, 10, 10, 10, 10, 5, 0, 13, 40, 10, 10, 10, 5, 0, 16, 10, 10, 10, 10, 5, 0, 40, 10, 10, 10, 10, 5, 0));
        // MAT-STUN-05: recovering from a stun: control comes back joint by joint, a little overcorrection, threads to steady her
        add.accept(clip(MatAnim.STUN_05)
                .pos("body", 0, 0, -0.8F, 0, 10, 0, -0.4F, 0, 22, 0, 0, 0)
                .rot("chest", 0, 14, 0, 0, 10, 8, 0, 0, 16, -4, 0, 0, 22, 0, 0, 0)
                .rot("head", 0, 12, 0, 0, 12, -6, 0, 0, 22, 0, 0, 0)
                .rot("arm_l", 0, -15, 0, -8, 8, -40, 0, -30, 22, 0, 0, 0)
                .rot("arm_r", 0, -10, 0, 6, 10, -20, 0, 10, 22, 0, 0, 0)
                .rot("leg_l", 0, 10, 0, 0, 12, 0, 0, 0)
                .rot("leg_r", 0, 10, 0, 0, 12, 0, 0, 0)
                .fingers('l', 0, 10, 10, 10, 10, 5, 0, 8, -10, -10, -10, -10, -5, 6, 22, 0, 0, 0, 0, 0, 0)
                .fx(MatRig.FX_THREAD_L, 0, 0, 0, 0, 6, 0, 0, 0, 10, 1, 1, 0, 18, 0.5F, 0.4F, 0, 22, 0, 0, 0));

        // ---- falls
        // MAT-FALL-01: falling forward: a trip, the arms reach to catch her, and she goes down onto her front
        add.accept(clip(MatAnim.FALL_01)
                .rot("body", Ease.IN, 0, 0, 0, 0, 4, 25, 0, 0, 10, 70, 0, 0, 13, 85, 0, 0, 16, PRONE[0][0], 0, 0)
                .pos("body", Ease.IN, 0, 0, 0, 0, 4, 0, -0.5F, 0, 10, 0, -3, -0.5F, 13, 0, -5.2F, -1, 16, PRONE[1][0], PRONE[1][1], PRONE[1][2])
                .rot("arm_l", 0, 0, 0, 0, 4, -80, 0, -15, 10, -150, 0, -20, 13, -160, 0, -25, 16, -160, 0, -25)
                .rot("arm_r", 0, 0, 0, 0, 4, -70, 0, 15, 10, -150, 0, 20, 13, -160, 0, 25, 16, -160, 0, 25)
                .rot("head", 0, 0, 0, 0, 10, -30, 0, 0, 16, -35, 0, 8)
                .rot("leg_l", 0, 0, 0, 0, 4, -14, 0, 0, 10, 10, 0, 0, 16, 5, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 10, 8, 0, 0, 16, 5, 0, 0)
                .rot("foot_l", 0, 0, 0, 0, 13, 40, 0, 0, 16, 40, 0, 0)
                .rot("foot_r", 0, 0, 0, 0, 13, 40, 0, 0, 16, 40, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 10, -20, -20, -20, -20, -10, 10, 16, 10, 10, 10, 10, 5, 4));
        // MAT-FALL-02: falling backwards: the arms fly up, the feet kick out, she lands on her back
        add.accept(clip(MatAnim.FALL_02)
                .rot("body", Ease.IN, 0, 0, 0, 0, 4, -25, 0, 0, 10, -70, 0, 0, 13, -88, 0, 0, 16, SUPINE[0][0], 0, 0)
                .pos("body", Ease.IN, 0, 0, 0, 0, 10, 0, -2.5F, 1.5F, 13, 0, -4.8F, 2, 16, SUPINE[1][0], SUPINE[1][1], SUPINE[1][2])
                .rot("arm_l", 0, 0, 0, 0, 4, -120, 0, -40, 13, -60, 0, -50, 16, -30, 0, -60)
                .rot("arm_r", 0, 0, 0, 0, 4, -110, 0, 45, 13, -60, 0, 55, 16, -30, 0, 60)
                .rot("leg_l", 0, 0, 0, 0, 6, -30, 0, 0, 13, -20, 0, 0, 16, -8, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 6, -24, 0, 0, 13, -16, 0, 0, 16, -8, 0, 0)
                .rot("head", 0, 0, 0, 0, 10, 20, 0, 0, 16, 25, 0, 0)
                .rot("foot_l", 0, 0, 0, 0, 16, -20, 0, 0)
                .rot("foot_r", 0, 0, 0, 0, 16, -20, 0, 0));
        // MAT-FALL-03: the supernatural fall (on her back; the face-down version below): she topples stiff as a cut puppet,
        // spins the wrong way in the air and slams flat
        for (boolean prone : new boolean[] {false, true}) {
            float s = prone ? -1.0F : 1.0F;
            float[][] g = prone ? PRONE : SUPINE;
            ClipBuilder c = clip(MatAnim.FALL_03)
                    .rot("body", Ease.SNAP, 0, 0, 0, 0, 3, -10 * s, 0, 20, 8, -45 * s, 40, 30, 14, g[0][0], 0, 0, 22, g[0][0], 0, 0)
                    .pos("body", Ease.IN, 0, 0, 0, 0, 8, 0, -1.5F, 0, 14, g[1][0], g[1][1], g[1][2], 22, g[1][0], g[1][1], g[1][2])
                    .rot("head", Ease.SNAP, 0, 0, 0, 0, 3, 0, 0, -50, 8, 30, 60, 0, 14, g[3][0], g[3][1], g[3][2], 22, g[3][0], g[3][1], g[3][2])
                    .rot("arm_l", Ease.SNAP, 0, 0, 0, 0, 3, 0, 0, -90, 8, -160, 0, -30, 14, g[4][0], g[4][1], g[4][2], 22, g[4][0], g[4][1], g[4][2])
                    .rot("arm_r", Ease.SNAP, 0, 0, 0, 0, 3, 0, 0, 90, 8, 40, 0, 60, 14, g[5][0], g[5][1], g[5][2], 22, g[5][0], g[5][1], g[5][2])
                    .rot("leg_l", Ease.SNAP, 0, 0, 0, 0, 8, -40, 0, -20, 14, g[6][0], 0, 0, 22, g[6][0], 0, 0)
                    .rot("leg_r", Ease.SNAP, 0, 0, 0, 0, 8, 30, 0, 20, 14, g[6][0], 0, 0, 22, g[6][0], 0, 0);
            (prone ? addProne : add).accept(c);
        }
        // MAT-FALL-04: hitting the ground: a squash, a small bounce, the head lifting from the impact; held while she is down
        for (boolean prone : new boolean[] {false, true}) {
            float[][] g = prone ? PRONE : SUPINE;
            ClipBuilder c = clip(MatAnim.FALL_04)
                    .rot("body", Ease.CATMULL, ground(prone, 0, 0, 10))
                    .pos("body", Ease.OUT, 0, g[1][0], g[1][1], g[1][2], 2, g[1][0], g[1][1] - 0.6F, g[1][2], 5, g[1][0], g[1][1] + 0.2F, g[1][2], 10, g[1][0], g[1][1], g[1][2])
                    .rot("chest", Ease.OUT, 0, g[2][0], 0, 0, 2, g[2][0] - 8, 0, 0, 10, g[2][0], 0, 0)
                    .rot("head", Ease.OUT, 0, g[3][0], g[3][1], g[3][2], 2, g[3][0] - 12 * (prone ? -1 : 1), g[3][1], g[3][2], 4, g[3][0] + 10 * (prone ? -1 : 1), g[3][1], g[3][2], 10, g[3][0], g[3][1], g[3][2])
                    .rot("arm_l", Ease.CATMULL, ground(prone, 4, 0, 10))
                    .rot("arm_r", Ease.CATMULL, ground(prone, 5, 0, 10))
                    .rot("leg_l", Ease.OUT, 0, g[6][0], 0, 0, 2, g[6][0] - 6, 0, 0, 10, g[6][0], 0, 0)
                    .rot("leg_r", Ease.OUT, 0, g[6][0], 0, 0, 2, g[6][0] - 6, 0, 0, 10, g[6][0], 0, 0)
                    .rot("foot_l", Ease.CATMULL, ground(prone, 7, 0, 10))
                    .rot("foot_r", Ease.CATMULL, ground(prone, 7, 0, 10))
                    .fingers('l', 0, 0, 0, 0, 0, 0, 0, 3, -20, -20, -20, -20, -10, 10, 10, 15, 15, 15, 15, 10, 2);
            (prone ? addProne : add).accept(c);
        }
        for (boolean prone : new boolean[] {false, true}) {
            Consumer<ClipBuilder> to = prone ? addProne : add;
            // MAT-FALL-05: the elegant recovery: she sits (or pushes) up, kneels, rises, and smooths her hood with one hand
            to.accept(clip(MatAnim.FALL_05)
                    .rot("body", from(prone, 0, 8, prone ? 50 : -40, 0, 0, 16, prone ? 20 : 10, 0, 0, 22, 5, 0, 0, 30, 0, 0, 0))
                    .pos("body", from(prone, 1, 8, 0, -4, prone ? -0.6F : 1.5F, 16, 0, -3, 0, 22, 0, -1, 0, 30, 0, 0, 0))
                    .rot("leg_l", from(prone, 6, 8, 30, 0, 0, 16, 70, 0, 0, 22, 20, 0, 0, 30, 0, 0, 0))
                    .rot("leg_r", from(prone, 6, 8, 20, 0, 0, 16, 60, 0, 0, 22, 10, 0, 0, 30, 0, 0, 0))
                    .rot("arm_l", from(prone, 4, 8, prone ? -80 : 20, 0, -30, 16, -40, 0, -10, 22, -140, 0, -20, 28, -20, 0, -5, 30, 0, 0, 0))
                    .rot("forearm_l", 0, 0, 0, 0, 16, -20, 0, 0, 22, -110, 0, 0, 28, -30, 0, 0, 30, 0, 0, 0)
                    .rot("arm_r", from(prone, 5, 8, prone ? -80 : 20, 0, 30, 16, -20, 0, 10, 30, 0, 0, 0))
                    .rot("head", from(prone, 3, 8, 0, 0, 0, 22, -6, 0, 6, 30, 0, 0, 0))
                    .rot("chest", from(prone, 2, 16, 10, 0, 0, 30, 0, 0, 0))
                    .rot("foot_l", from(prone, 7, 16, 0, 0, 0)).rot("foot_r", from(prone, 7, 16, 0, 0, 0)));
            // MAT-FALL-06: the partly supernatural recovery: it begins like a person's, then she is hoisted upright by the chest,
            // limbs dangling, head lolling, and set down on her feet
            to.accept(clip(MatAnim.FALL_06)
                    .rot("body", Ease.SNAP, from(prone, 0, 10, prone ? 60 : -60, 0, 0, 12, prone ? 60 : -60, 0, 0, 14, -5, 0, 0, 24, -5, 0, 0, 32, 0, 0, 0))
                    .pos("body", Ease.SNAP, from(prone, 1, 10, 0, -4.5F, 0, 12, 0, -4.5F, 0, 14, 0, -0.2F, 0, 24, 0, 0.3F, 0, 28, 0, 0, 0, 32, 0, 0, 0))
                    .rot("leg_l", from(prone, 6, 12, 10, 0, 0, 14, 15, 0, 0, 26, 15, 0, 0, 30, 0, 0, 0))
                    .rot("leg_r", from(prone, 6, 12, 10, 0, 0, 14, 12, 0, 0, 26, 12, 0, 0, 30, 0, 0, 0))
                    .rot("foot_l", from(prone, 7, 14, 40, 0, 0, 26, 40, 0, 0, 30, 0, 0, 0))
                    .rot("foot_r", from(prone, 7, 14, 40, 0, 0, 26, 40, 0, 0, 30, 0, 0, 0))
                    .rot("arm_l", from(prone, 4, 10, prone ? -80 : 15, 0, -30, 14, 10, 0, -10, 26, 10, 0, -10, 32, 0, 0, 0))
                    .rot("arm_r", from(prone, 5, 10, prone ? -80 : 15, 0, 30, 14, 10, 0, 10, 26, 10, 0, 10, 32, 0, 0, 0))
                    .rot("head", from(prone, 3, 14, 30, 0, 20, 20, 30, 0, -15, 26, 25, 0, 10, 32, 0, 0, 0))
                    .rot("chest", from(prone, 2, 14, 10, 0, 0, 32, 0, 0, 0)));
            // MAT-FALL-07: the unnatural recovery: she arches up onto her head and heels, folds the wrong way, and her joints
            // snap back into place one after another
            to.accept(clip(MatAnim.FALL_07)
                    .rot("body", Ease.SNAP, from(prone, 0, 4, prone ? 88 : -90, 0, 0, 8, prone ? 40 : -80, 0, 0, 12, 60, 0, 0, 16, 20, 0, 0, 24, 0, 0, 0))
                    .pos("body", Ease.SNAP, from(prone, 1, 4, 0, -5, 1, 8, 0, -2, 0, 12, 0, -2, 0, 16, 0, -1, 0, 24, 0, 0, 0))
                    .rot("chest", Ease.SNAP, from(prone, 2, 4, prone ? 40 : -60, 0, 0, 8, -80, 0, 0, 12, 40, 0, 0, 20, 10, 0, 0, 28, 0, 0, 0))
                    .rot("head", Ease.SNAP, from(prone, 3, 4, 60, 0, 0, 12, -60, 0, 0, 16, 0, 0, 70, 20, 0, 0, -60, 22, 0, 0, 0, 28, 0, 0, 0))
                    .rot("arm_l", Ease.SNAP, from(prone, 4, 8, 60, 0, -40, 12, -160, 0, 20, 18, 20, 0, -60, 28, 0, 0, 0))
                    .rot("arm_r", Ease.SNAP, from(prone, 5, 8, 50, 0, 40, 12, -150, 0, -20, 18, 25, 0, 50, 28, 0, 0, 0))
                    .rot("leg_l", Ease.SNAP, from(prone, 6, 8, -30, 0, 0, 12, 40, 0, 0, 18, -10, 0, 0, 24, 0, 0, 0))
                    .rot("leg_r", Ease.SNAP, from(prone, 6, 8, -30, 0, 0, 12, 30, 0, 0, 20, -5, 0, 0, 24, 0, 0, 0))
                    .fingers('l', Ease.STEP, 0, 10, 10, 10, 10, 5, 0, 8, 60, -20, 60, -20, 30, 0, 14, -20, 60, -20, 60, 10, 0, 20, 30, 30, 30, 30, 10, 0, 28, 0, 0, 0, 0, 0, 0));
            // MAT-FALL-08: an interrupted recovery: half up, she is struck back down and lands again (the impact follows)
            to.accept(clip(MatAnim.FALL_08)
                    .rot("body", Ease.IN, 0, prone ? 20 : 10, 0, 0, 4, prone ? 40 : -30, 0, 0, 10, (prone ? PRONE : SUPINE)[0][0], 0, 0, 16, (prone ? PRONE : SUPINE)[0][0], 0, 0)
                    .pos("body", Ease.IN, 0, 0, -2.5F, 0, 4, 0, -2.8F, 0, 10, (prone ? PRONE : SUPINE)[1][0], (prone ? PRONE : SUPINE)[1][1], (prone ? PRONE : SUPINE)[1][2],
                            16, (prone ? PRONE : SUPINE)[1][0], (prone ? PRONE : SUPINE)[1][1], (prone ? PRONE : SUPINE)[1][2])
                    .rot("leg_l", 0, 60, 0, 0, 10, (prone ? PRONE : SUPINE)[6][0], 0, 0, 16, (prone ? PRONE : SUPINE)[6][0], 0, 0)
                    .rot("leg_r", 0, 50, 0, 0, 10, (prone ? PRONE : SUPINE)[6][0], 0, 0, 16, (prone ? PRONE : SUPINE)[6][0], 0, 0)
                    .rot("arm_l", 0, -40, 0, -10, 4, -90, 0, -40, 10, (prone ? PRONE : SUPINE)[4][0], 0, (prone ? PRONE : SUPINE)[4][2], 16, (prone ? PRONE : SUPINE)[4][0], 0, (prone ? PRONE : SUPINE)[4][2])
                    .rot("arm_r", 0, -20, 0, 10, 4, -80, 0, 40, 10, (prone ? PRONE : SUPINE)[5][0], 0, (prone ? PRONE : SUPINE)[5][2], 16, (prone ? PRONE : SUPINE)[5][0], 0, (prone ? PRONE : SUPINE)[5][2])
                    .rot("head", 0, 0, 0, 0, 4, -20, 0, 10, 10, (prone ? PRONE : SUPINE)[3][0], 0, 0, 16, (prone ? PRONE : SUPINE)[3][0], 0, 0));
            // MAT-FALL-09: recovering with the threads: the hands throw threads up, grip, and she is drawn upright by them, stiff
            to.accept(clip(MatAnim.FALL_09)
                    .rot("arm_l", from(prone, 4, 6, -170, 0, -20, 10, -170, 0, -20, 24, -150, 0, -20, 32, 0, 0, 0))
                    .rot("arm_r", from(prone, 5, 6, -170, 0, 20, 10, -170, 0, 20, 24, -150, 0, 20, 32, 0, 0, 0))
                    .fingers('l', 0, 0, 0, 0, 0, 0, 0, 6, -20, -20, -20, -20, -10, 12, 10, 55, 55, 55, 55, 40, 0, 24, 55, 55, 55, 55, 40, 0, 32, 0, 0, 0, 0, 0, 0)
                    .fingers('r', 0, 0, 0, 0, 0, 0, 0, 6, -40, -40, -30, -30, -15, 10, 10, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 0)
                    .rot("body", from(prone, 0, 14, prone ? 88 : -90, 0, 0, 24, -5, 0, 0, 32, 0, 0, 0))
                    .pos("body", from(prone, 1, 14, 0, prone ? -5.5F : -5, 0, 24, 0, -0.5F, 0, 32, 0, 0, 0))
                    .rot("leg_l", from(prone, 6, 14, -10, 0, 0, 24, 10, 0, 0, 32, 0, 0, 0))
                    .rot("leg_r", from(prone, 6, 14, -10, 0, 0, 24, 10, 0, 0, 32, 0, 0, 0))
                    .rot("head", from(prone, 3, 14, 0, 0, 0, 24, -15, 0, 0, 32, 0, 0, 0))
                    .fx(MatRig.FX_THREAD_L, 0, 0, 0, 0, 6, 0.5F, 0.2F, 0, 10, 1, 1, 0, 24, 1, 1, 0, 32, 0, 0, 0)
                    .fx(MatRig.FX_THREAD_R, 0, 0, 0, 0, 6, 0.5F, 0.2F, 0, 10, 1, 1, 0, 24, 1, 1, 0, 32, 0, 0, 0));
        }
    }
}
