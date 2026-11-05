package com.codenamecoraline.client.ten_threads_witch.matriarch;

import static com.codenamecoraline.client.ten_threads_witch.matriarch.ClipBuilder.clip;
import static com.codenamecoraline.client.ten_threads_witch.matriarch.MatClips.MANIP_ARM;
import static com.codenamecoraline.client.ten_threads_witch.matriarch.MatClips.MANIP_FINGERS;
import static com.codenamecoraline.client.ten_threads_witch.matriarch.MatClips.MANIP_FOREARM;
import static com.codenamecoraline.client.ten_threads_witch.matriarch.MatClips.MANIP_HAND;
import static com.codenamecoraline.client.ten_threads_witch.matriarch.MatClips.hold;
import static com.codenamecoraline.client.ten_threads_witch.matriarch.MatClips.holdFingers;
import static com.codenamecoraline.client.ten_threads_witch.matriarch.MatClips.manipArm;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatAnim;
import java.util.function.Consumer;

/**
 * 6.4 Manipulation of the threads, MAT-THREAD-01 to 28. One-hand gestures are authored for the left hand and mirrored when
 * the right hand plays them (the hand slot decides); two-hand gestures (flag BOTH) are authored for both. No finger is bound
 * to a puppet: the same command can come from any finger, from one hand or from both. The effect channels open, tighten,
 * snap and release the threads in step with the fingers ({@link MatRig#FX_THREAD_L}, {@link MatRig#FX_ATTACK}). The chest
 * only gets additive touches here, so a gesture never fights the posture the body is in.
 */
final class ThreadClips {
    private ThreadClips() {}

    static void register(Consumer<ClipBuilder> add) {
        // MAT-THREAD-01: manifestation: both hands rise from the sides, gather, burst open, and the threads appear and settle
        add.accept(clip(MatAnim.THREAD_01)
                .rot("arm_l", 0, 0, 0, 0, 10, -20, 0, -12, 20, -30, 0, -10, 30, -30, 0, -8)
                .rot("forearm_l", 0, 0, 0, 0, 10, -30, 0, 0, 20, -55, 0, 0, 30, -55, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 10, 0, 40, 0, 20, 0, 90, 0, 30, 0, 90, 0)
                .fingers('l', Ease.OUT, 0, 0, 0, 0, 0, 0, 0, 10, 30, 30, 30, 30, 20, 0, 18, -14, -14, -14, -14, -10, 8, 26, 10, 15, 18, 22, 0, 4, 30, 20, 25, 25, 30, 15, 2)
                .rot("arm_r", 0, 0, 0, 0, 10, -18, 0, 10, 20, -26, 0, 8, 30, -26, 0, 8)
                .rot("forearm_r", 0, 0, 0, 0, 20, -40, 0, 0, 30, -40, 0, 0)
                .rot("hand_r", 0, 0, 0, 0, 20, 0, -60, 0, 30, 0, -60, 0)
                .fingers('r', Ease.OUT, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 18, -50, -40, 0, 0, -20, 6, 26, -30, -20, 0, 0, -10, 2, 30, -30, -20, 0, 0, -10, 2)
                .rot("chest", 0, 0, 0, 0, 10, -3, 0, 0, 30, -1, 0, 0).additive("chest")
                .rot("head", 0, 0, 0, 0, 12, 6, 0, 0, 26, 0, 0, 0).additive("head")
                .fx(MatRig.FX_THREAD_L, 0, 0, 0, 0, 10, 0, 0, 0, 26, 1, 0.4F, 0, 30, 1, 0.5F, 0)
                .fx(MatRig.FX_THREAD_R, 0, 0, 0, 0, 14, 0, 0, 0, 28, 1, 0.4F, 0, 30, 1, 0.5F, 0));
        // MAT-THREAD-02: opening the fingers one by one (index, middle, ring, little, then the thumb)
        add.accept(manipArm(clip(MatAnim.THREAD_02), 16)
                .fingers('l', 0, 30, 30, 30, 30, 25, 0, 4, -12, 30, 30, 30, 25, 1, 7, -12, -10, 30, 30, 20, 2, 10, -12, -10, -8, 30, 15, 3,
                        13, -12, -10, -8, -6, -6, 5, 16, -10, -8, -6, -4, -6, 6));
        // MAT-THREAD-03: separating the fingers: alternating configurations, each finger on its own
        add.accept(manipArm(clip(MatAnim.THREAD_03), 20)
                .fingers('l', 0, -10, -8, -6, -4, -6, 6, 6, -15, 20, -10, 25, -6, 10, 12, 20, -15, 25, -10, 0, 12, 20, -5, 10, 0, 15, -4, 8)
                .rot("hand_l", 0, 0, 90, 0, 6, 0, 90, 6, 12, 0, 90, -6, 20, 0, 90, 0));
        // MAT-THREAD-04: extending the arms: both reach slowly forward to their full length to widen the control (held)
        add.accept(clip(MatAnim.THREAD_04)
                .rot("arm_l", 0, -30, 0, -8, 24, -75, 0, -14)
                .rot("forearm_l", 0, -55, 0, 0, 24, -15, 0, 0)
                .rot("hand_l", 0, 0, 90, 0, 24, -10, 90, 0)
                .fingers('l', 0, -10, -8, -6, -4, -6, 6, 24, -5, 0, 5, 10, -4, 5)
                .rot("arm_r", 0, -20, 0, 6, 24, -65, 0, 10)
                .rot("forearm_r", 0, -40, 0, 0, 24, -20, 0, 0)
                .rot("chest", 0, 0, 0, 0, 24, 6, 0, 0).additive("chest")
                .fx(MatRig.FX_THREAD_L, 0, 1, 0.5F, 0, 24, 1, 0.7F, 0));
        // MAT-THREAD-05: raising the hands: both rise to the height of the shoulders, the shoulders lift with them (held)
        add.accept(clip(MatAnim.THREAD_05)
                .rot("arm_l", 0, 0, 0, 0, 12, -60, 0, -25, 20, -70, 0, -20)
                .rot("forearm_l", 0, 0, 0, 0, 20, -50, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 20, 0, 90, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 20, 20, 20, 20, 20, 10, 0)
                .rot("arm_r", 0, 0, 0, 0, 12, -40, 0, 20, 20, -50, 0, 18)
                .rot("forearm_r", 0, 0, 0, 0, 20, -40, 0, 0)
                .pos("mantle", 0, 0, 0, 0, 12, 0, 0.4F, 0, 20, 0, 0.3F, 0)
                .rot("chest", 0, 0, 0, 0, 20, -5, 0, 0).additive("chest"));
        // MAT-THREAD-06: crossing the fingers: a precise sigil before the chest (index and ring out, middle and little crossed under)
        add.accept(clip(MatAnim.THREAD_06)
                .rot("arm_l", 0, 0, 0, 0, 10, -45, 0, 18, 24, -45, 0, 18)
                .rot("forearm_l", 0, 0, 0, 0, 10, -70, 0, 0, 24, -70, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 10, 0, 60, 10, 24, 0, 60, 10)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 8, -20, 40, -20, 40, 30, -6, 16, -25, 45, 50, -20, 35, -10, 24, -25, 45, 50, -20, 35, -10)
                .rot("arm_r", 0, 0, 0, 0, 10, -40, 0, -14, 24, -40, 0, -14)
                .rot("forearm_r", 0, 0, 0, 0, 10, -60, 0, 0, 24, -60, 0, 0)
                .rot("hand_r", 0, 0, 0, 0, 10, 0, -40, -10, 24, 0, -40, -10)
                .fingers('r', 0, 0, 0, 0, 0, 0, 0, 8, -60, -20, 0, 0, -20, 0, 16, -70, 0, -30, 0, -20, -8, 24, -70, 0, -30, 0, -20, -8)
                .rot("head", 0, 0, 0, 0, 8, 12, 0, 0, 24, 12, 0, 0).additive("head")
                .fx(MatRig.FX_GLOW, 0, 0, 0, 0, 16, 0.3F, 0.6F, 0, 24, 0.4F, 1, 0));
        // MAT-THREAD-07: forming the connections: the fingers flick forward one after another, a thread leaping from each
        add.accept(clip(MatAnim.THREAD_07)
                .rot("arm_l", Ease.CATMULL, hold(0, 30, new float[] {-55, 0, -18}))
                .rot("forearm_l", Ease.CATMULL, hold(0, 30, new float[] {-45, 0, 0}))
                .rot("hand_l", Ease.CATMULL, hold(0, 30, MANIP_HAND))
                .fingers('l', Ease.OUT, 0, 30, 30, 30, 30, 25, 0, 6, 30, 30, 30, 30, 25, 0, 9, -15, 30, 30, 30, 25, 0, 12, -15, -15, 30, 30, 25, 0,
                        15, -15, -15, -15, 30, 25, 0, 18, -15, -15, -15, -15, 25, 0, 21, -15, -15, -15, -15, -10, 6, 30, 0, 5, 10, 15, 0, 4)
                .rot("arm_r", Ease.CATMULL, hold(0, 30, new float[] {-45, 0, 14}))
                .rot("forearm_r", Ease.CATMULL, hold(0, 30, new float[] {-40, 0, 0}))
                .fingers('r', Ease.OUT, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 16, -40, 0, 0, 0, 0, 0, 20, -40, -35, 0, 0, -10, 4, 30, -30, -25, 0, 0, -10, 2)
                .fx(MatRig.FX_THREAD_L, Ease.STEP, 0, 0, 0, 0, 6, 0, 0, 0, 9, 0.2F, 0.3F, 0, 12, 0.4F, 0.3F, 0, 15, 0.6F, 0.3F, 0, 18, 0.8F, 0.3F, 0, 21, 1, 0.3F, 0, 30, 1, 0.4F, 0)
                .fx(MatRig.FX_THREAD_R, 0, 0, 0, 0, 10, 0, 0, 0, 26, 1, 0.3F, 0, 30, 1, 0.4F, 0));
        // MAT-THREAD-08: drawing the threads taut: the fingers close, the hand pulls up and back a little, and holds
        add.accept(clip(MatAnim.THREAD_08)
                .rot("arm_l", 0, -30, 0, -8, 8, -22, 0, -8, 20, -24, 0, -8)
                .rot("forearm_l", 0, -55, 0, 0, 8, -70, 0, 0, 14, -66, 0, 0, 20, -66, 0, 0)
                .rot("hand_l", 0, 0, 90, 0, 8, 20, 90, 0, 20, 16, 90, 0)
                .fingers('l', 0, 0, 5, 10, 15, 0, 4, 8, 35, 40, 45, 50, 30, 0, 20, 32, 38, 42, 46, 28, 0)
                .fx(MatRig.FX_THREAD_L, Ease.SMOOTH, 0, 1, 0.4F, 0, 8, 1, 1, 0, 20, 1, 0.9F, 0));
        // MAT-THREAD-09: controlling one puppet: the marionettist's hand, small corrections of tension, single-finger commands
        add.accept(clip(MatAnim.THREAD_09)
                .rot("arm_l", Ease.CATMULL, 0, -30, 0, -8, 12, -31, 0, -9, 24, -29, 0, -8, 36, -31, 0, -7, 48, -30, 0, -8)
                .rot("forearm_l", Ease.CATMULL, 0, -55, 0, 0, 10, -59, 0, 0, 24, -52, 0, 0, 38, -57, 0, 0, 48, -55, 0, 0)
                .rot("hand_l", Ease.CATMULL, 0, 0, 90, 0, 8, -6, 90, 3, 20, 4, 90, -4, 32, 2, 90, 5, 48, 0, 90, 0)
                .fingers('l', Ease.CATMULL, 0, 20, 25, 25, 30, 15, 2, 8, 5, 25, 25, 30, 15, 2, 14, 20, 25, 25, 30, 15, 2, 24, 20, 10, 35, 30, 15, 3,
                        32, 20, 25, 25, 15, 15, 2, 40, 25, 30, 30, 30, 10, 1, 48, 20, 25, 25, 30, 15, 2)
                .fx(MatRig.FX_THREAD_L, Ease.CATMULL, 0, 1, 0.6F, 0, 8, 1, 0.8F, 0, 24, 1, 0.7F, 0, 40, 1, 0.5F, 0, 48, 1, 0.6F, 0));
        // MAT-THREAD-10: controlling several puppets at once (the hanging ones): a slow polyrhythm of the fingers, the wheel turning
        add.accept(clip(MatAnim.THREAD_10)
                .rot("arm_l", Ease.CATMULL, 0, -15, 0, -5, 32, -17, 0, -5, 64, -15, 0, -5)
                .rot("forearm_l", Ease.CATMULL, 0, -35, 0, 0, 32, -38, 0, 0, 64, -35, 0, 0)
                .rot("hand_l", Ease.CATMULL, 0, 0, 30, 0, 20, 0, 30, 4, 44, 0, 30, -4, 64, 0, 30, 0)
                .fingers('l', Ease.CATMULL, 0, 10, 20, 30, 20, 10, 3, 10, -5, 20, 30, 20, 10, 3, 18, -5, 35, 30, 5, 10, 4, 30, 15, 35, 10, 5, 15, 2,
                        42, 15, 15, 10, 30, 5, 5, 54, 5, 25, 30, 25, 10, 3, 64, 10, 20, 30, 20, 10, 3)
                .fx(MatRig.FX_THREAD_L, Ease.CATMULL, 0, 0.6F, 0.5F, 0, 32, 0.6F, 0.6F, 0, 64, 0.6F, 0.5F, 0));
        // MAT-THREAD-11: both hands at once, different commands in opposite directions (one rises as the other falls)
        add.accept(clip(MatAnim.THREAD_11)
                .rot("arm_l", Ease.CATMULL, hold(0, 40, MANIP_ARM))
                .rot("forearm_l", Ease.CATMULL, 0, -50, 0, 0, 10, -70, 0, 0, 20, -50, 0, 0, 30, -40, 0, 0, 40, -50, 0, 0)
                .rot("hand_l", Ease.CATMULL, 0, 0, 90, 0, 10, 0, 90, 10, 20, 0, 90, 0, 30, 0, 90, -10, 40, 0, 90, 0)
                .fingers('l', Ease.CATMULL, 0, 20, 25, 25, 30, 15, 2, 10, 40, 10, 40, 10, 20, 1, 20, 20, 25, 25, 30, 15, 2, 30, 5, 30, 5, 35, 10, 4, 40, 20, 25, 25, 30, 15, 2)
                .rot("arm_r", Ease.CATMULL, hold(0, 40, new float[] {-30, 0, 10}))
                .rot("forearm_r", Ease.CATMULL, 0, -45, 0, 0, 10, -30, 0, 0, 20, -45, 0, 0, 30, -60, 0, 0, 40, -45, 0, 0)
                .rot("hand_r", Ease.CATMULL, 0, 0, -70, 0, 10, 0, -70, -8, 30, 0, -70, 8, 40, 0, -70, 0)
                .fingers('r', Ease.CATMULL, 0, -20, -10, 0, 0, -10, 2, 10, 0, -30, 0, -20, 0, 0, 20, -20, -10, 0, 0, -10, 2, 30, -35, 0, -25, 0, -10, 4, 40, -20, -10, 0, 0, -10, 2)
                .fx(MatRig.FX_THREAD_L, Ease.CATMULL, 0, 1, 0.7F, 0, 20, 1, 0.5F, 0, 40, 1, 0.7F, 0)
                .fx(MatRig.FX_THREAD_R, Ease.CATMULL, 0, 1, 0.5F, 0, 20, 1, 0.7F, 0, 40, 1, 0.5F, 0));
        // MAT-THREAD-12: one hand alone, low by the hip, so the other is free (used while she walks)
        add.accept(clip(MatAnim.THREAD_12)
                .rot("arm_l", Ease.CATMULL, 0, -12, 0, -6, 16, -14, 0, -6, 32, -12, 0, -6)
                .rot("forearm_l", Ease.CATMULL, 0, -45, 0, 0, 16, -48, 0, 0, 32, -45, 0, 0)
                .rot("hand_l", Ease.CATMULL, 0, 0, 70, 0, 32, 0, 70, 0)
                .fingers('l', Ease.CATMULL, 0, 15, 20, 20, 25, 10, 2, 8, 0, 20, 20, 25, 10, 2, 16, 15, 5, 25, 25, 10, 2, 24, 15, 20, 10, 35, 10, 3, 32, 15, 20, 20, 25, 10, 2)
                .fx(MatRig.FX_THREAD_L, Ease.CATMULL, 0, 1, 0.5F, 0, 32, 1, 0.5F, 0));
        // MAT-THREAD-13: a precise command: only the fingers and the wrist (index flick, then middle), the arm does not move
        add.accept(manipArm(clip(MatAnim.THREAD_13), 14)
                .fingers('l', Ease.OUT, 0, 20, 25, 25, 30, 15, 2, 3, -15, 25, 25, 30, 15, 2, 5, 25, 25, 25, 30, 15, 2, 8, 20, -10, 35, 30, 15, 2, 10, 20, 25, 25, 30, 15, 2, 14, 20, 25, 25, 30, 15, 2)
                .rot("hand_l", Ease.OUT, 0, 0, 90, 0, 3, -6, 90, 0, 5, 4, 90, 0, 8, -3, 90, 0, 14, 0, 90, 0)
                .fx(MatRig.FX_THREAD_L, 0, 1, 0.6F, 0, 3, 1, 0.9F, 0, 8, 1, 0.9F, 0, 14, 1, 0.6F, 0));
        // MAT-THREAD-14: pulling the threads: reach, grab, and draw the hand up and back; the chest goes with the pull
        add.accept(clip(MatAnim.THREAD_14)
                .rot("arm_l", 0, -30, 0, -8, 6, -38, 0, -8, 12, -10, 0, -4, 18, -24, 0, -6)
                .rot("forearm_l", Ease.IN, 0, -55, 0, 0, 6, -45, 0, 0, 12, -95, 0, 0, 18, -65, 0, 0)
                .rot("hand_l", 0, 0, 90, 0, 12, 25, 90, 0, 18, 10, 90, 0)
                .fingers('l', 0, 20, 25, 25, 30, 15, 2, 5, -5, 0, 5, 10, 0, 4, 8, 55, 60, 60, 65, 40, 0, 18, 40, 45, 45, 50, 30, 0)
                .rot("chest", 0, 0, 0, 0, 12, -6, 0, 0, 18, -2, 0, 0).additive("chest")
                .fx(MatRig.FX_THREAD_L, 0, 1, 0.6F, 0, 8, 1, 1, 0, 12, 1, 1, 0, 18, 1, 0.8F, 0));
        // MAT-THREAD-15: letting go: the fingers open in turn, the tension drops, the hand relaxes and the threads fade
        add.accept(clip(MatAnim.THREAD_15)
                .rot("arm_l", 0, -30, 0, -8, 20, -10, 0, -4)
                .rot("forearm_l", 0, -55, 0, 0, 20, -20, 0, 0)
                .rot("hand_l", 0, 0, 90, 0, 20, 0, 20, 0)
                .fingers('l', 0, 40, 45, 45, 50, 30, 0, 6, 10, 20, 30, 40, 15, 2, 12, -10, 0, 10, 20, 0, 6, 20, 0, 0, 0, 0, 0, 0)
                .fx(MatRig.FX_THREAD_L, 0, 1, 0.8F, 0, 8, 0.8F, 0.2F, 0, 20, 0, 0, 0));
        // MAT-THREAD-16: changing the tension: a flex and a release of the wrist and fingers, the forearm barely moving
        add.accept(clip(MatAnim.THREAD_16)
                .rot("arm_l", Ease.CATMULL, hold(0, 16, MANIP_ARM))
                .rot("forearm_l", 0, -55, 0, 0, 5, -60, 0, 0, 10, -52, 0, 0, 16, -55, 0, 0)
                .rot("hand_l", 0, 0, 90, 0, 5, 14, 90, 0, 10, -6, 90, 0, 16, 0, 90, 0)
                .fingers('l', 0, 20, 25, 25, 30, 15, 2, 5, 30, 30, 32, 34, 15, 1, 10, 12, 18, 20, 24, 10, 3, 16, 20, 25, 25, 30, 15, 2)
                .fx(MatRig.FX_THREAD_L, 0, 1, 0.6F, 0, 5, 1, 0.9F, 0, 10, 1, 0.5F, 0, 16, 1, 0.6F, 0));
        // MAT-THREAD-17: handing the control over: the hands meet before her, the threads pass from one to the other
        add.accept(clip(MatAnim.THREAD_17)
                .rot("arm_l", 0, -30, 0, -8, 12, -40, 0, 20, 28, -12, 0, -4)
                .rot("forearm_l", 0, -55, 0, 0, 12, -60, 0, 0, 28, -25, 0, 0)
                .rot("hand_l", 0, 0, 90, 0, 12, 0, 60, 0, 28, 0, 20, 0)
                .fingers('l', 0, 20, 25, 25, 30, 15, 2, 12, 30, 30, 30, 30, 20, 0, 18, -10, -10, -10, -10, -5, 4, 28, 5, 5, 8, 10, 0, 2)
                .rot("arm_r", 0, -20, 0, 6, 12, -40, 0, -18, 28, -30, 0, 8)
                .rot("forearm_r", 0, -40, 0, 0, 12, -55, 0, 0, 28, -50, 0, 0)
                .rot("hand_r", 0, 0, -60, 0, 12, 0, -60, 0, 28, 0, -90, 0)
                .fingers('r', 0, 0, 0, 0, 0, 0, 0, 12, -40, -30, -20, -10, -10, 4, 18, -5, 0, 0, 5, 0, 2, 28, 20, 25, 25, 30, 15, 2)
                .rot("head", 0, 0, 0, 0, 10, 10, 0, 0, 22, 0, 0, 0).additive("head")
                .fx(MatRig.FX_THREAD_L, 0, 1, 0.6F, 0, 14, 1, 0.6F, 0, 22, 0, 0, 0, 28, 0, 0, 0)
                .fx(MatRig.FX_THREAD_R, 0, 0, 0, 0, 14, 0.2F, 0.2F, 0, 22, 1, 0.6F, 0, 28, 1, 0.6F, 0));
        // MAT-THREAD-18: ending a manipulation in control: the fingers ease off, the hand lowers a little, the threads slacken
        add.accept(clip(MatAnim.THREAD_18)
                .rot("arm_l", 0, -30, 0, -8, 14, -18, 0, -6)
                .rot("forearm_l", 0, -55, 0, 0, 14, -35, 0, 0)
                .rot("hand_l", 0, 0, 90, 0, 14, 0, 60, 0)
                .fingers('l', 0, 20, 25, 25, 30, 15, 2, 14, 5, 8, 10, 12, 5, 2)
                .fx(MatRig.FX_THREAD_L, 0, 1, 0.6F, 0, 6, 1, 0.1F, 0, 14, 0, 0, 0));
        // MAT-THREAD-19: recovering a connection: a corrective reach, the open hand catches the loose end, the thread re-forms
        add.accept(clip(MatAnim.THREAD_19)
                .rot("arm_l", 0, -18, 0, -6, 8, -45, 0, -12, 16, -30, 0, -8, 24, -30, 0, -8)
                .rot("forearm_l", 0, -35, 0, 0, 8, -30, 0, 0, 16, -55, 0, 0, 24, -55, 0, 0)
                .rot("hand_l", Ease.OUT, 0, 0, 60, 0, 8, -10, 90, 0, 12, 15, 90, 0, 24, 0, 90, 0)
                .fingers('l', Ease.OUT, 0, 5, 8, 10, 12, 5, 2, 8, -15, -15, -15, -15, -10, 8, 12, 35, 35, 35, 35, 25, 0, 24, 20, 25, 25, 30, 15, 2)
                .fx(MatRig.FX_THREAD_L, 0, 0, 0, 0, 12, 0, 0, 0, 16, 1, 1, 0, 24, 1, 0.6F, 0));
        // MAT-THREAD-20: a connection breaks: the hand is jerked back, the fingers fly open, the thread snaps and falls
        add.accept(clip(MatAnim.THREAD_20)
                .rot("arm_l", Ease.OUT, 0, -30, 0, -8, 3, -10, 0, -10, 10, -6, 0, -12, 18, -12, 0, -8)
                .rot("forearm_l", Ease.OUT, 0, -55, 0, 0, 3, -80, 0, 0, 10, -40, 0, 0, 18, -30, 0, 0)
                .rot("hand_l", Ease.OUT, 0, 0, 90, 0, 3, 30, 90, 15, 18, 0, 40, 0)
                .fingers('l', Ease.OUT, 0, 20, 25, 25, 30, 15, 2, 3, -20, -20, -20, -20, -15, 12, 18, 10, 10, 10, 10, 5, 4)
                .rot("chest", Ease.OUT, 0, 0, 0, 0, 3, -4, 0, 3, 12, 0, 0, 0).additive("chest")
                .rot("head", Ease.OUT, 0, 0, 0, 0, 3, -5, 0, 0, 12, 0, 0, 0).additive("head")
                .fx(MatRig.FX_THREAD_L, Ease.LINEAR, 0, 1, 0.6F, 0, 3, 1, 1, 1, 8, 0.5F, 0, 1, 18, 0, 0, 0));

        // ---- attacks of the threads (priority 7)
        // MAT-THREAD-21: preparation: the hand draws back to the shoulder, the fingers gather, the threads go taut (held)
        add.accept(clip(MatAnim.THREAD_21)
                .rot("arm_l", 0, -30, 0, -8, 10, -20, 0, -15, 16, -20, 0, -15)
                .rot("forearm_l", 0, -55, 0, 0, 10, -95, 0, 0, 16, -95, 0, 0)
                .rot("hand_l", 0, 0, 90, 0, 10, -20, 90, 0, 16, -20, 90, 0)
                .fingers('l', 0, 20, 25, 25, 30, 15, 2, 4, 40, 45, 45, 50, 30, 0, 10, 55, 30, 60, 65, 40, 0, 16, 55, 25, 60, 65, 40, 0)
                .rot("chest", 0, 0, 0, 0, 10, 0, 8, 0, 16, 0, 8, 0).additive("chest")
                .fx(MatRig.FX_THREAD_L, 0, 1, 0.6F, 0, 10, 1, 1, 0, 16, 1, 1, 0)
                .fx(MatRig.FX_ATTACK, 0, 0, 0, 0, 16, 0.15F, 0, 0));
        // MAT-THREAD-22: the launch: the arm throws forward and the thread shoots out along the line of the hand
        add.accept(clip(MatAnim.THREAD_22)
                .rot("arm_l", Ease.OUT, 0, -20, 0, -15, 3, -85, 0, -8, 8, -70, 0, -8, 14, -35, 0, -8)
                .rot("forearm_l", Ease.OUT, 0, -95, 0, 0, 3, -5, 0, 0, 8, -15, 0, 0, 14, -50, 0, 0)
                .rot("hand_l", 0, -20, 90, 0, 3, -15, 90, 0, 14, 0, 90, 0)
                .fingers('l', Ease.OUT, 0, 55, 30, 60, 65, 40, 0, 3, -20, -20, -18, -15, -10, 10, 14, 15, 15, 15, 15, 10, 4)
                .rot("chest", Ease.OUT, 0, 0, 8, 0, 3, 8, -10, 0, 14, 0, 0, 0).additive("chest")
                .fx(MatRig.FX_ATTACK, 0, 0.15F, 0, 0, 5, 1, 0, 0, 10, 1, 0, 0, 14, 0.3F, 0, 0));
        // MAT-THREAD-23: the whip: wide raise, a snap down across the body (the wrist leads), the thread lashing out in a wave
        add.accept(clip(MatAnim.THREAD_23)
                .rot("arm_l", Ease.BACK, 0, -20, 0, -15, 4, -60, 0, -40, 7, 10, 0, -10, 11, -10, 0, -10, 16, -30, 0, -8)
                .rot("forearm_l", 0, -95, 0, 0, 4, -60, 0, 0, 7, -20, 0, 0, 16, -50, 0, 0)
                .rot("hand_l", Ease.BACK, 0, -20, 90, 0, 4, -30, 90, 0, 7, 40, 90, 0, 16, 0, 90, 0)
                .fingers('l', 0, 55, 30, 60, 65, 40, 0, 4, 50, 50, 50, 50, 35, 0, 16, 20, 25, 25, 30, 15, 2)
                .rot("chest", Ease.BACK, 0, 0, 8, 0, 4, -6, 12, 0, 7, 10, -14, 0, 16, 0, 0, 0).additive("chest")
                .fx(MatRig.FX_ATTACK, 0, 0.15F, 0, 0, 4, 0.3F, 0, 0.2F, 8, 1, 0, 1, 12, 0.8F, 0, 0.3F, 16, 0.2F, 0, 0));
        // MAT-THREAD-24: the thrust: aim, then a straight drive of the arm with the index pointing, the thread piercing ahead
        add.accept(clip(MatAnim.THREAD_24)
                .rot("arm_l", Ease.OUT, 0, -20, 0, -15, 3, -90, 0, -5, 6, -88, 0, -5, 12, -35, 0, -8)
                .rot("forearm_l", Ease.OUT, 0, -95, 0, 0, 3, 0, 0, 0, 12, -50, 0, 0)
                .rot("hand_l", 0, -20, 90, 0, 3, 0, 90, 0, 12, 0, 90, 0)
                .fingers('l', 0, 55, 30, 60, 65, 40, 0, 3, -15, 50, 55, 60, 35, -4, 8, -15, 50, 55, 60, 35, -4, 12, 20, 25, 25, 30, 15, 2)
                .rot("chest", Ease.OUT, 0, 0, 8, 0, 3, 10, -12, 0, 12, 0, 0, 0).additive("chest")
                .pos("body", Ease.OUT, 0, 0, 0, 0, 3, 0, 0, -0.6F, 12, 0, 0, 0).additive("body")
                .fx(MatRig.FX_ATTACK, 0, 0.15F, 0, 0, 4, 1, 0, 0, 8, 1, 0, 0, 12, 0.2F, 0, 0));
        // MAT-THREAD-25: the coil: both hands circle round each other, closing the threads around the target
        add.accept(clip(MatAnim.THREAD_25)
                .rot("arm_l", 0, -40, 0, -10, 8, -60, 0, 10, 16, -45, 0, 20, 24, -50, 0, 15)
                .rot("forearm_l", 0, -60, 0, 0, 8, -40, 0, 0, 16, -70, 0, 0, 24, -65, 0, 0)
                .rot("hand_l", 0, 0, 90, 0, 8, 0, 60, 30, 16, 0, 120, -20, 24, 0, 100, 0)
                .fingers('l', 0, -10, -10, -10, -10, -5, 8, 16, 50, 50, 50, 50, 40, 0, 24, 45, 45, 45, 45, 35, 0)
                .rot("arm_r", 0, -35, 0, 8, 8, -45, 0, -12, 16, -60, 0, -15, 24, -50, 0, -14)
                .rot("forearm_r", 0, -50, 0, 0, 8, -70, 0, 0, 16, -40, 0, 0, 24, -60, 0, 0)
                .fingers('r', 0, -20, -10, 0, 0, -10, 2, 16, 20, 20, 20, 20, 10, 0, 24, 15, 15, 15, 15, 10, 0)
                .fx(MatRig.FX_ATTACK, 0, 0.2F, 0, 0, 12, 1, 0.5F, 0.4F, 24, 1, 0.5F, 0)
                .fx(MatRig.FX_THREAD_L, 0, 1, 0.6F, 0, 16, 1, 1, 0, 24, 1, 1, 0)
                .fx(MatRig.FX_THREAD_R, 0, 1, 0.6F, 0, 16, 1, 1, 0, 24, 1, 1, 0));
        // MAT-THREAD-26: the hostile pull: reach and grip, then a hard yank of the arm with the whole body behind it
        add.accept(clip(MatAnim.THREAD_26)
                .rot("arm_l", Ease.IN, 0, -60, 0, -10, 6, -70, 0, -10, 10, -5, 0, -6, 20, -25, 0, -8)
                .rot("forearm_l", Ease.IN, 0, -20, 0, 0, 6, -20, 0, 0, 10, -100, 0, 0, 20, -60, 0, 0)
                .rot("hand_l", 0, 0, 90, 0, 10, 30, 90, 0, 20, 10, 90, 0)
                .fingers('l', 0, -10, -10, -10, -10, -5, 6, 6, 60, 60, 60, 60, 45, 0, 20, 40, 40, 40, 40, 30, 0)
                .rot("chest", Ease.IN, 0, 0, 0, 0, 6, 4, -6, 0, 10, -10, 14, 0, 20, 0, 0, 0).additive("chest")
                .rot("body", Ease.IN, 0, 0, 0, 0, 10, -4, 0, 0, 20, 0, 0, 0).additive("body")
                .fx(MatRig.FX_THREAD_L, 0, 1, 0.5F, 0, 10, 1, 1, 0, 20, 1, 0.7F, 0)
                .fx(MatRig.FX_ATTACK, 0, 1, 0, 0, 10, 1, 0, 0, 20, 0.3F, 0, 0));
        // MAT-THREAD-27: the retraction: the threads reel back in and the hand returns to the marionettist's pose
        add.accept(clip(MatAnim.THREAD_27)
                .rot("arm_l", 0, -60, 0, -8, 14, MANIP_ARM[0], MANIP_ARM[1], MANIP_ARM[2])
                .rot("forearm_l", 0, -30, 0, 0, 14, MANIP_FOREARM[0], 0, 0)
                .rot("hand_l", 0, 0, 90, 0, 14, 0, 90, 0)
                .fingers('l', 0, 10, 10, 10, 10, 5, 4, 7, 35, 35, 35, 35, 25, 0, 14, MANIP_FINGERS[0], MANIP_FINGERS[1], MANIP_FINGERS[2], MANIP_FINGERS[3], MANIP_FINGERS[4], MANIP_FINGERS[5])
                .fx(MatRig.FX_ATTACK, 0, 1, 0, 0.2F, 8, 0.2F, 0, 0, 14, 0, 0, 0)
                .fx(MatRig.FX_THREAD_L, 0, 1, 0.6F, 0, 14, 1, 0.6F, 0));
        // MAT-THREAD-28: many threads at once: the hands fling out in turn, three volleys, more threads each time
        add.accept(clip(MatAnim.THREAD_28)
                .rot("arm_l", Ease.OUT, 0, -30, 0, -8, 6, -70, 0, -30, 8, -50, 0, -60, 13, -60, 0, -20, 15, -75, 0, -45, 20, -55, 0, -25, 22, -80, 0, -55, 30, -35, 0, -10)
                .rot("forearm_l", 0, -55, 0, 0, 6, -40, 0, 0, 8, -15, 0, 0, 13, -40, 0, 0, 15, -15, 0, 0, 20, -40, 0, 0, 22, -15, 0, 0, 30, -50, 0, 0)
                .rot("arm_r", Ease.OUT, 0, -20, 0, 6, 2, -40, 0, 20, 8, -60, 0, 10, 11, -70, 0, 45, 18, -55, 0, 25, 22, -75, 0, 50, 30, -25, 0, 8)
                .rot("forearm_r", 0, -40, 0, 0, 11, -15, 0, 0, 18, -35, 0, 0, 22, -15, 0, 0, 30, -40, 0, 0)
                .fingers('l', Ease.OUT, 0, 20, 25, 25, 30, 15, 2, 6, 50, 50, 50, 50, 35, 0, 8, -20, -20, -20, -20, -10, 12, 13, 50, 50, 50, 50, 35, 0,
                        15, -20, -20, -20, -20, -10, 12, 20, 50, 50, 50, 50, 35, 0, 22, -20, -20, -20, -20, -10, 12, 30, 20, 25, 25, 30, 15, 2)
                .fingers('r', Ease.OUT, 0, 0, 0, 0, 0, 0, 0, 11, -50, -40, -30, -20, -15, 10, 18, 0, 0, 0, 0, 0, 0, 22, -50, -40, -30, -20, -15, 10, 30, 0, 0, 0, 0, 0, 0)
                .rot("chest", Ease.OUT, 0, 0, 0, 0, 8, 0, 10, 0, 15, 0, -10, 0, 22, 0, 8, 0, 30, 0, 0, 0).additive("chest")
                .fx(MatRig.FX_ATTACK, 0, 0, 0, 0, 6, 0.2F, 0, 0.5F, 8, 1, 0.4F, 0.5F, 15, 1, 0.7F, 0.5F, 22, 1, 1, 0.5F, 30, 0.2F, 1, 0));
    }
}
