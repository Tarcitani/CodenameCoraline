package com.codenamecoraline.client.ten_threads_witch.matriarch;

import static com.codenamecoraline.client.ten_threads_witch.matriarch.ClipBuilder.clip;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatAnim;
import java.util.function.Consumer;

/**
 * 6.1 Idle and independent behaviour: the breathing loop of each mood (MAT-IDLE-01a/b/c), the elegant alternative, the
 * disturbing idle, watching a puppet and watching the threads, the hands' manipulation gestures, the change of posture and
 * the taunts (elegant, the slow clap, the five imitations). The gaze (MAT-IDLE-04) and the fingers (MAT-IDLE-07) are
 * procedural ({@link GazeController}, {@link FingerController}). Times in ticks; see docs/design/matriarch_animation.md.
 */
final class IdleClips {
    private IdleClips() {}

    static void register(Consumer<ClipBuilder> add) {
        // MAT-IDLE-01a, composed: one slow breath every 3.2 s, the old hunch, the hands held a little before her, a sway of the weight
        add.accept(clip(MatAnim.IDLE_01_COMPOSED)
                .rot("body", Ease.CATMULL, 0, 0, 0, 0.8F, 32, 0, 0, -0.8F, 64, 0, 0, 0.8F)
                .pos("body", Ease.CATMULL, 0, 0, 0, 0, 32, 0, -0.1F, 0, 64, 0, 0, 0)
                .rot("chest", Ease.CATMULL, 0, 7, 0, 0, 16, 6, 0, 0, 32, 5.5F, 0, 0, 48, 6.5F, 0, 0, 64, 7, 0, 0)
                .scale("chest", Ease.CATMULL, 0, 1, 1, 1, 32, 1.02F, 1.0F, 1.02F, 64, 1, 1, 1)
                .pos("mantle", Ease.CATMULL, 0, 0, 0, 0, 32, 0, 0.15F, 0, 64, 0, 0, 0)
                .rot("head", Ease.CATMULL, 0, -5, 1, 0, 32, -4, -1, 0, 64, -5, 1, 0)
                .rot("arm_l", Ease.CATMULL, 0, -4, 0, -3, 32, -4, 0, -4, 64, -4, 0, -3)
                .rot("forearm_l", Ease.CATMULL, 0, -18, 0, 0, 32, -20, 0, 0, 64, -18, 0, 0)
                .rot("hand_l", Ease.CATMULL, 0, 8, 0, 0, 64, 8, 0, 0)
                .rot("arm_r", Ease.CATMULL, 0, -8, 0, 3, 32, -8, 0, 4, 64, -8, 0, 3)
                .rot("forearm_r", Ease.CATMULL, 0, -12, 0, 0, 64, -12, 0, 0)
                .rot("hand_r", Ease.CATMULL, 0, 6, 0, 0, 64, 6, 0, 0));
        // MAT-IDLE-01b, irritated: a quicker, shallower breath, the shoulders up, the hands ready, a twitch of the head
        add.accept(clip(MatAnim.IDLE_01_IRRITATED)
                .rot("body", Ease.CATMULL, 0, 0, 0, 1, 20, 0, 0, -1.2F, 30, 0, 0, -0.6F, 48, 0, 0, 1)
                .rot("chest", Ease.CATMULL, 0, 10, 0, 0, 12, 9, 0, 0, 24, 11, 0, 0, 36, 9, 0, 0, 48, 10, 0, 0)
                .scale("chest", Ease.CATMULL, 0, 1, 1, 1, 12, 1.015F, 1, 1.015F, 24, 1, 1, 1, 36, 1.015F, 1, 1.015F, 48, 1, 1, 1)
                .pos("mantle", Ease.CATMULL, 0, 0, 0.3F, 0, 24, 0, 0.4F, 0, 48, 0, 0.3F, 0)
                .rot("head", Ease.SNAP, 0, -8, 0, 0, 22, -8, 0, 0, 24, -8, 4, 2, 30, -8, 4, 2, 32, -8, 0, 0, 48, -8, 0, 0)
                .rot("arm_l", Ease.CATMULL, 0, -8, 0, -6, 24, -9, 0, -7, 48, -8, 0, -6)
                .rot("forearm_l", Ease.CATMULL, 0, -28, 0, 0, 24, -31, 0, 0, 48, -28, 0, 0)
                .rot("hand_l", Ease.CATMULL, 0, 14, 0, 0, 48, 14, 0, 0)
                .rot("arm_r", Ease.CATMULL, 0, -12, 0, 4, 48, -12, 0, 4)
                .rot("forearm_r", Ease.CATMULL, 0, -16, 0, 0, 48, -16, 0, 0));
        // MAT-IDLE-01c, unravelling: lopsided, the head tilted, a ragged breath that catches
        add.accept(clip(MatAnim.IDLE_01_UNRAVELLING)
                .rot("body", Ease.CATMULL, 0, 0, 0, 3, 28, 0, 0, 2, 56, 0, 0, 3)
                .rot("chest", Ease.CATMULL, 0, 14, 0, 6, 10, 17, 0, 6, 14, 13, 0, 6, 30, 16, 0, 7, 34, 12, 0, 6, 56, 14, 0, 6)
                .rot("head", Ease.CATMULL, 0, -10, 0, -10, 20, -9, 3, -12, 40, -11, -2, -8, 56, -10, 0, -10)
                .rot("arm_l", Ease.CATMULL, 0, -15, 0, -10, 28, -13, 0, -12, 56, -15, 0, -10)
                .rot("forearm_l", Ease.CATMULL, 0, -35, 0, 0, 28, -38, 0, 0, 56, -35, 0, 0)
                .rot("hand_l", Ease.CATMULL, 0, 25, 0, 8, 56, 25, 0, 8)
                .rot("arm_r", Ease.CATMULL, 0, -6, 0, 5, 56, -6, 0, 5)
                .rot("forearm_r", Ease.CATMULL, 0, -8, 0, 0, 56, -8, 0, 0));

        // MAT-IDLE-02: the elegant alternative (a tilt of the head, the left hand turned up, a transfer of weight)
        add.accept(clip(MatAnim.IDLE_02)
                .rot("head", 0, 0, 0, 0, 14, 4, 6, 9, 46, 4, 6, 9, 62, 0, 0, 0)
                .rot("arm_l", 0, 0, 0, 0, 12, 0, 0, -3, 20, -2, 0, -6, 52, -2, 0, -6, 66, 0, 0, 0)
                .rot("forearm_l", 0, 0, 0, 0, 18, -30, 0, 0, 50, -30, 0, 0, 66, 0, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 18, 15, -20, 0, 50, 15, -20, 0, 66, 0, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 18, -6, -4, -2, 0, -4, 4, 30, 10, -4, 6, 12, 0, 6, 42, -8, 0, 4, 14, -6, 3, 58, 0, 0, 0, 0, 0, 0)
                .rot("body", 0, 0, 0, 0, 20, 0, 0, -2.5F, 50, 0, 0, -2.5F, 66, 0, 0, 0)
                .pos("body", 0, 0, 0, 0, 20, 0.3F, 0, 0, 50, 0.3F, 0, 0, 66, 0, 0, 0)
                .rot("leg_l", 0, 0, 0, 0, 20, 0, 0, 2.5F, 50, 0, 0, 2.5F, 66, 0, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 20, 0, 0, 2.5F, 50, 0, 0, 2.5F, 66, 0, 0, 0)
                .rot("chest", 0, 0, 0, 0, 20, 0, 0, 3, 50, 0, 0, 3, 66, 0, 0, 0)
                .pos("mantle", 0, 0, 0, 0, 12, 0, 0.3F, 0, 24, 0, 0, 0));

        // MAT-IDLE-03: the disturbing idle (the body still, the head tilting in jerks, single fingers curling, an asymmetric return)
        add.accept(clip(MatAnim.IDLE_03)
                .rot("head", Ease.SNAP, 0, 0, 0, 0, 10, 0, 0, 0, 13, 0, 0, -12, 30, 0, 0, -12, 33, 6, 8, -12, 50, 6, 8, -12, 52, 0, -6, 4, 70, 0, -6, 4, 76, 0, 0, 0, 84, 0, 0, 0)
                .fingers('l', Ease.SNAP, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 40, 0, 0, 24, 0, 0, 0, 40, 0, 0, 26, 0, 0, 0, 0, 0, 0,
                        40, 30, 0, 0, 0, 0, 0, 46, 30, 0, 0, 0, 0, 0, 47, 0, 0, 0, 0, 0, 0, 60, 0, 25, 25, 0, 0, -4, 64, 0, 0, 0, 0, 0, 0, 84, 0, 0, 0, 0, 0, 0)
                .fingers('r', Ease.SNAP, 0, 0, 0, 0, 0, 0, 0, 36, -30, 0, 0, 0, 0, 0, 44, -30, 0, 0, 0, 0, 0, 46, 0, 0, 0, 0, 0, 0, 84, 0, 0, 0, 0, 0, 0)
                .rot("chest", Ease.SNAP, 0, 0, 0, 0, 20, 0, 0, 0, 21, 3, 0, 0, 24, 0, 0, 0, 58, 0, 0, 0, 59, -3, 0, 0, 62, 0, 0, 0, 84, 0, 0, 0)
                .rot("arm_l", Ease.SNAP, 0, 0, 0, 0, 44, 0, 0, 0, 45, 0, 0, -6, 48, 0, 0, -2, 78, 0, 0, -1, 84, 0, 0, 0));

        // MAT-IDLE-05: watching a puppet (the chest leans in, the head nods towards it, the hand lifts a little)
        add.accept(clip(MatAnim.IDLE_05)
                .rot("chest", 0, 0, 0, 0, 10, 6, 0, 0, 34, 6, 0, 0, 44, 0, 0, 0)
                .rot("head", 0, 0, 0, 0, 8, 6, 0, 4, 20, 2, 0, -3, 36, 4, 0, 2, 44, 0, 0, 0)
                .rot("forearm_l", 0, 0, 0, 0, 12, -18, 0, 0, 32, -18, 0, 0, 44, 0, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 12, 10, 0, 0, 32, 10, 0, 0, 44, 0, 0, 0)
                .pos("body", 0, 0, 0, 0, 10, 0, 0, -0.4F, 34, 0, 0, -0.4F, 44, 0, 0, 0));

        // MAT-IDLE-06: watching her own threads (head down to the raised hand, a pluck of the fingers to correct the tension)
        add.accept(clip(MatAnim.IDLE_06)
                .rot("head", 0, 0, 0, 0, 10, 18, -10, 0, 48, 18, -10, 0, 60, 0, 0, 0)
                .rot("arm_l", 0, 0, 0, 0, 10, -20, 0, -6, 48, -20, 0, -6, 60, 0, 0, 0)
                .rot("forearm_l", 0, 0, 0, 0, 10, -45, 0, 0, 48, -45, 0, 0, 60, 0, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 10, 20, -30, 0, 22, 20, -30, 4, 34, 20, -30, -4, 48, 20, -30, 0, 60, 0, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 13, 10, 20, 30, 30, 10, 0, 22, 0, 0, 0, 0, 0, 5, 34, 12, -5, 8, 20, 0, 2, 46, 0, 0, 0, 0, 0, 0, 60, 0, 0, 0, 0, 0, 0)
                .fx(MatRig.FX_THREAD_L, Ease.SMOOTH, 0, 0, 0, 0, 12, 0.6F, 0.4F, 0, 18, 0.6F, 0.9F, 0, 30, 0.6F, 0.5F, 0, 48, 0.6F, 0.5F, 0, 60, 0, 0, 0));

        // MAT-IDLE-08a: one hand's manipulation gesture (reposition, turn of the wrist, the forearm, the fingers one by one)
        add.accept(clip(MatAnim.IDLE_08_ONE)
                .rot("forearm_l", 0, 0, 0, 0, 10, -35, 0, 0, 30, -35, 0, 10, 40, 0, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 10, 0, -40, 0, 18, 0, 40, 0, 26, 15, 20, -10, 34, 0, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 10, -10, -5, 0, 5, 0, 6, 18, 20, 10, 0, -5, 10, 0, 26, 5, 25, 35, 40, -5, 2, 34, 0, 0, 0, 0, 0, 0));
        // MAT-IDLE-08b: two different gestures at once (the left closes and opens; the right lifts two fingers off the staff)
        add.accept(clip(MatAnim.IDLE_08_TWO)
                .rot("forearm_l", 0, 0, 0, 0, 6, -30, 0, 0, 28, -40, 0, -10, 48, 0, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 6, 0, -30, 10, 20, -10, 30, 0, 36, 0, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 6, 30, 30, 30, 30, 20, 0, 18, -15, -15, -15, -15, -10, 8, 30, 0, 30, 0, 30, 0, 4, 44, 0, 0, 0, 0, 0, 0)
                .rot("forearm_r", 0, 0, 0, 0, 14, -20, 0, 0, 36, -20, 0, 0, 48, 0, 0, 0)
                .rot("hand_r", 0, 0, 0, 0, 14, 0, 0, -15, 24, 0, 0, 10, 40, 0, 0, 0)
                .fingers('r', 0, 0, 0, 0, 0, 0, 0, 14, -35, -10, 0, 0, -10, 0, 24, 0, -35, -20, 0, 0, 0, 34, -20, 0, 0, -30, 0, 0, 44, 0, 0, 0, 0, 0, 0));

        // MAT-IDLE-09: a change of posture (the weight moves to the right leg, the left foot steps, the torso and shoulders settle)
        add.accept(clip(MatAnim.IDLE_09)
                .rot("body", 0, 0, 0, 0, 12, 0, 0, 3, 30, 0, 0, -2, 56, 0, 0, 0)
                .pos("body", 0, 0, 0, 0, 12, -0.4F, 0, 0, 30, 0.3F, 0, 0, 56, 0, 0, 0)
                .rot("leg_l", 0, 0, 0, 0, 14, -8, 0, -6, 22, -4, 0, -5, 56, 0, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 12, 0, 0, -3, 30, 0, 0, 2, 56, 0, 0, 0)
                .rot("foot_l", 0, 0, 0, 0, 14, 12, 0, 0, 22, 0, 0, 0)
                .rot("chest", 0, 0, 0, 0, 12, -3, 0, -3, 30, 2, 0, 3, 56, 0, 0, 0)
                .rot("head", 0, 0, 0, 0, 12, -4, 0, 5, 30, 3, 0, -4, 50, 0, 0, 0)
                .rot("arm_l", 0, 0, 0, 0, 14, 0, 0, -6, 30, -5, 0, -3, 56, 0, 0, 0)
                .rot("arm_r", 0, 0, 0, 0, 20, -6, 0, 4, 56, 0, 0, 0));

        // MAT-IDLE-10a, elegant taunt: chin up and turned away, a flick of the left hand, a contemptuous return
        add.accept(clip(MatAnim.IDLE_10_ELEGANT)
                .rot("head", 0, 0, 0, 0, 10, -10, -8, 6, 30, -10, -8, 6, 44, 0, 0, 0)
                .rot("chest", 0, 0, 0, 0, 10, -4, 8, 0, 36, -4, 8, 0, 48, 0, 0, 0)
                .rot("arm_l", Ease.SMOOTH, 0, 0, 0, 0, 8, -15, 0, -10, 16, -25, 0, -25, 26, -15, 0, -15, 44, 0, 0, 0)
                .rot("forearm_l", 0, 0, 0, 0, 8, -50, 0, 0, 16, -20, 0, 0, 26, -35, 0, 0, 44, 0, 0, 0)
                .rot("hand_l", Ease.OUT, 0, 0, 0, 0, 8, 30, 0, 0, 14, -30, 0, -20, 20, -10, 0, -10, 40, 0, 0, 0)
                .fingers('l', Ease.OUT, 0, 0, 0, 0, 0, 0, 0, 8, 20, 20, 20, 20, 10, 0, 14, -15, -15, -12, -10, -10, 10, 24, 0, 5, 10, 15, 0, 4, 44, 0, 0, 0, 0, 0, 0));
        // MAT-IDLE-10b, the slow clap: the left palm pats the back of the hand that holds the staff, three times, slowly
        float[] open = {-12, -12, -12, -12, -8, 2};
        add.accept(clip(MatAnim.IDLE_10_CLAP)
                .rot("arm_l", Ease.SMOOTH, 0, 0, 0, 0, 14, -40, 0, 12, 24, -40, 0, 26, 28, -40, 0, 12, 44, -40, 0, 26, 48, -40, 0, 12, 64, -40, 0, 26, 68, -40, 0, 12, 80, -20, 0, 4, 92, 0, 0, 0)
                .rot("forearm_l", 0, 0, 0, 0, 14, -55, 0, 0, 24, -60, 0, 0, 28, -50, 0, 0, 44, -60, 0, 0, 48, -50, 0, 0, 64, -60, 0, 0, 68, -50, 0, 0, 80, -25, 0, 0, 92, 0, 0, 0)
                .rot("arm_r", 0, 0, 0, 0, 14, -30, 0, -10, 80, -30, 0, -10, 92, 0, 0, 0)
                .rot("forearm_r", 0, 0, 0, 0, 14, -40, 0, 0, 80, -40, 0, 0, 92, 0, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 12, open[0], open[1], open[2], open[3], open[4], open[5], 80, open[0], open[1], open[2], open[3], open[4], open[5], 92, 0, 0, 0, 0, 0, 0)
                .rot("head", 0, 0, 0, 0, 14, 4, 0, 0, 30, -6, 0, 6, 60, -6, 0, -4, 86, 0, 0, 0));
        // MAT-IDLE-10c, imitation: a theatrical curtsey copying a crouching player
        add.accept(clip(MatAnim.IDLE_10_MIMIC_CROUCH)
                .rot("body", 0, 0, 0, 0, 12, 15, 0, 0, 24, 18, 0, 0, 36, 15, 0, 0, 46, 0, 0, 0)
                .pos("body", 0, 0, 0, 0, 12, 0, -2, 0, 24, 0, -2.4F, 0, 36, 0, -2, 0, 46, 0, 0, 0)
                .rot("leg_l", 0, 0, 0, 0, 12, -20, 0, 0, 36, -20, 0, 0, 46, 0, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 12, -14, 0, 0, 36, -14, 0, 0, 46, 0, 0, 0)
                .rot("foot_l", 0, 0, 0, 0, 12, 20, 0, 0, 36, 20, 0, 0, 46, 0, 0, 0)
                .rot("chest", 0, 0, 0, 0, 12, 20, 0, 0, 36, 20, 0, 0, 46, 0, 0, 0)
                .rot("head", 0, 0, 0, 0, 12, -15, 0, 8, 36, -15, 0, 8, 46, 0, 0, 0)
                .rot("arm_l", Ease.BACK, 0, 0, 0, 0, 12, -30, 0, -30, 24, -35, 0, -40, 36, -30, 0, -30, 50, 0, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 12, -10, -8, -6, -4, -6, 8, 46, 0, 0, 0, 0, 0, 0));
        // imitation of a jumping player: a mocking little hop with the arms flung up
        add.accept(clip(MatAnim.IDLE_10_MIMIC_JUMP)
                .pos("body", Ease.OUT, 0, 0, 0, 0, 8, 0, -1, 0, 14, 0, 3, 0, 20, 0, 3.5F, 0, 26, 0, 0, 0, 30, 0, -0.8F, 0, 38, 0, 0, 0)
                .rot("leg_l", 0, 0, 0, 0, 8, -15, 0, 0, 14, 10, 0, 0, 26, -8, 0, 0, 38, 0, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 8, -12, 0, 0, 14, 14, 0, 0, 26, -6, 0, 0, 38, 0, 0, 0)
                .rot("foot_l", 0, 0, 0, 0, 14, 30, 0, 0, 26, 0, 0, 0)
                .rot("foot_r", 0, 0, 0, 0, 14, 30, 0, 0, 26, 0, 0, 0)
                .rot("arm_l", 0, 0, 0, 0, 8, 10, 0, 0, 14, -60, 0, -20, 22, -60, 0, -20, 32, 0, 0, 0)
                .rot("arm_r", 0, 0, 0, 0, 14, -40, 0, 10, 22, -40, 0, 10, 32, 0, 0, 0)
                .rot("head", 0, 0, 0, 0, 14, -10, 0, 10, 30, 0, 0, 0));
        // imitation of a blocking player: cowering behind the hands and the staff, peeking out
        add.accept(clip(MatAnim.IDLE_10_MIMIC_SHIELD)
                .rot("arm_l", 0, 0, 0, 0, 8, -70, 0, 20, 40, -70, 0, 20, 50, 0, 0, 0)
                .rot("forearm_l", 0, 0, 0, 0, 8, -90, 0, 0, 40, -90, 0, 0, 50, 0, 0, 0)
                .rot("arm_r", 0, 0, 0, 0, 8, -60, 0, -15, 40, -60, 0, -15, 50, 0, 0, 0)
                .rot("forearm_r", 0, 0, 0, 0, 8, -60, 0, 0, 40, -60, 0, 0, 50, 0, 0, 0)
                .rot("chest", 0, 0, 0, 0, 8, 18, 0, 0, 40, 18, 0, 0, 50, 0, 0, 0)
                .rot("head", 0, 0, 0, 0, 8, 12, -15, 0, 20, 8, -15, 6, 26, 8, 12, -6, 34, 8, -15, 6, 46, 0, 0, 0)
                .pos("body", 0, 0, 0, 0, 8, 0, -0.8F, 0, 40, 0, -0.8F, 0, 50, 0, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 8, -15, -15, -15, -15, -10, 10, 40, -15, -15, -15, -15, -10, 10, 50, 0, 0, 0, 0, 0, 0));
        // imitation of an eating or drinking player: a dainty sip with the little finger raised, head tipped back
        add.accept(clip(MatAnim.IDLE_10_MIMIC_DRINK)
                .rot("arm_l", 0, 0, 0, 0, 12, -55, 0, 15, 44, -55, 0, 15, 56, 0, 0, 0)
                .rot("forearm_l", 0, 0, 0, 0, 12, -100, 0, 0, 44, -100, 0, 0, 56, 0, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 12, 30, -20, 0, 24, 10, -20, 0, 36, 30, -20, 0, 56, 0, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 12, 35, 35, 35, -20, 20, 0, 44, 35, 35, 35, -20, 20, 0, 56, 0, 0, 0, 0, 0, 0)
                .rot("head", 0, 0, 0, 0, 12, 0, 0, 0, 24, -20, 0, 0, 36, -20, 0, 0, 44, 0, 0, 4, 56, 0, 0, 0)
                .rot("chest", 0, 0, 0, 0, 24, -6, 0, 0, 36, -6, 0, 0, 56, 0, 0, 0));
        // imitation of an attacking player: an exaggerated, clumsy swing of an invisible sword
        add.accept(clip(MatAnim.IDLE_10_MIMIC_SWING)
                .rot("arm_l", Ease.BACK, 0, 0, 0, 0, 10, -150, 0, -10, 16, 20, 0, 10, 24, 10, 0, 5, 40, 0, 0, 0)
                .rot("forearm_l", 0, 0, 0, 0, 10, -30, 0, 0, 16, -5, 0, 0, 40, 0, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 8, 60, 60, 60, 60, 40, 0, 30, 60, 60, 60, 60, 40, 0, 40, 0, 0, 0, 0, 0, 0)
                .rot("chest", 0, 0, 0, 0, 10, -8, 25, 0, 16, 12, -20, 0, 24, 6, -10, 0, 40, 0, 0, 0)
                .rot("head", 0, 0, 0, 0, 10, -10, 0, 0, 18, 10, 0, 8, 40, 0, 0, 0)
                .rot("leg_l", 0, 0, 0, 0, 16, -14, 0, 0, 30, 0, 0, 0));
    }
}
