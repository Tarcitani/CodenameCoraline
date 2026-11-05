package com.codenamecoraline.client.ten_threads_witch.matriarch;

import static com.codenamecoraline.client.ten_threads_witch.matriarch.ClipBuilder.clip;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatAnim;
import java.util.function.Consumer;

/**
 * 6.2 Locomotion and 6.3 floating. The cycles are sampled by distance ({@link GaitController}); the arm swing of a cycle
 * has a weaker priority (9.5) than thread work (9), so walking never takes a hand off its puppet. The head only gets an
 * additive bob, so the gaze keeps it on the target while she walks.
 */
final class LocomotionClips {
    private LocomotionClips() {}

    private static final float ARMS = 9.5F;

    static void register(Consumer<ClipBuilder> add) {
        // MAT-MOVE-01: the old woman's walk: short steps, a bob at mid-stance, the hips and the shoulders counter-turning
        add.accept(clip(MatAnim.MOVE_01)
                .rot("leg_l", Ease.CATMULL, 0, -18, 0, 0, 10, 16, 0, 0, 20, -18, 0, 0)
                .rot("leg_r", Ease.CATMULL, 0, 16, 0, 0, 10, -18, 0, 0, 20, 16, 0, 0)
                .rot("foot_l", Ease.CATMULL, 0, -10, 0, 0, 5, 0, 0, 0, 10, 15, 0, 0, 15, 5, 0, 0, 20, -10, 0, 0)
                .rot("foot_r", Ease.CATMULL, 0, 15, 0, 0, 5, 5, 0, 0, 10, -10, 0, 0, 15, 0, 0, 0, 20, 15, 0, 0)
                .pos("body", Ease.CATMULL, 0, 0, 0, 0, 5, 0, 0.35F, 0, 10, 0, 0, 0, 15, 0, 0.35F, 0, 20, 0, 0, 0)
                .rot("body", Ease.CATMULL, 0, 0, 3, -1.5F, 10, 0, -3, 1.5F, 20, 0, 3, -1.5F)
                .rot("chest", Ease.CATMULL, 0, 2, -3, 0, 10, 2, 3, 0, 20, 2, -3, 0)
                .rot("arm_l", Ease.CATMULL, 0, 12, 0, -3, 10, -14, 0, -3, 20, 12, 0, -3)
                .rot("arm_r", Ease.CATMULL, 0, -8, 0, 3, 10, 8, 0, 3, 20, -8, 0, 3)
                .rot("forearm_l", Ease.CATMULL, 0, -8, 0, 0, 10, -14, 0, 0, 20, -8, 0, 0)
                .rot("head", Ease.CATMULL, 0, 1, 0, 0, 5, -1, 0, 0, 10, 1, 0, 0, 15, -1, 0, 0, 20, 1, 0, 0)
                .prio(ARMS, "arm_l", "arm_r", "forearm_l").additive("head"));
        // MAT-MOVE-02: slow and threatening: long deliberate steps that pause at each contact, the torso still and leaning in
        add.accept(clip(MatAnim.MOVE_02)
                .rot("leg_l", Ease.CATMULL, 0, -22, 0, 0, 6, -20, 0, 0, 16, 20, 0, 0, 22, 18, 0, 0, 32, -22, 0, 0)
                .rot("leg_r", Ease.CATMULL, 0, 20, 0, 0, 6, 18, 0, 0, 16, -22, 0, 0, 22, -20, 0, 0, 32, 20, 0, 0)
                .rot("foot_l", Ease.CATMULL, 0, -8, 0, 0, 8, 0, 0, 0, 16, 18, 0, 0, 24, 4, 0, 0, 32, -8, 0, 0)
                .rot("foot_r", Ease.CATMULL, 0, 18, 0, 0, 8, 4, 0, 0, 16, -8, 0, 0, 24, 0, 0, 0, 32, 18, 0, 0)
                .pos("body", Ease.CATMULL, 0, 0, 0, 0, 8, 0, 0.25F, 0, 16, 0, 0, 0, 24, 0, 0.25F, 0, 32, 0, 0, 0)
                .rot("body", Ease.CATMULL, 0, 0, 1, -1, 16, 0, -1, 1, 32, 0, 1, -1)
                .rot("chest", Ease.CATMULL, 0, 4, -1, 0, 16, 4, 1, 0, 32, 4, -1, 0)
                .rot("arm_l", Ease.CATMULL, 0, 5, 0, -2, 16, -5, 0, -2, 32, 5, 0, -2)
                .rot("arm_r", Ease.CATMULL, 0, -3, 0, 2, 16, 3, 0, 2, 32, -3, 0, 2)
                .prio(ARMS, "arm_l", "arm_r"));
        // MAT-MOVE-03: aggressive: hard steps, the centre of gravity thrown forward, the shoulders driving, elbows bent
        add.accept(clip(MatAnim.MOVE_03)
                .rot("leg_l", Ease.CATMULL, 0, -24, 0, 0, 8, 22, 0, 0, 16, -24, 0, 0)
                .rot("leg_r", Ease.CATMULL, 0, 22, 0, 0, 8, -24, 0, 0, 16, 22, 0, 0)
                .rot("foot_l", Ease.CATMULL, 0, -12, 0, 0, 4, 0, 0, 0, 8, 22, 0, 0, 12, 6, 0, 0, 16, -12, 0, 0)
                .rot("foot_r", Ease.CATMULL, 0, 22, 0, 0, 4, 6, 0, 0, 8, -12, 0, 0, 12, 0, 0, 0, 16, 22, 0, 0)
                .rot("body", Ease.CATMULL, 0, 6, 5, -2, 8, 6, -5, 2, 16, 6, 5, -2)
                .pos("body", Ease.CATMULL, 0, 0, 0, -0.3F, 4, 0, 0.5F, -0.3F, 8, 0, 0, -0.3F, 12, 0, 0.5F, -0.3F, 16, 0, 0, -0.3F)
                .rot("chest", Ease.CATMULL, 0, 8, -6, 0, 8, 8, 6, 0, 16, 8, -6, 0)
                .rot("arm_l", Ease.CATMULL, 0, 22, 0, -6, 8, -22, 0, -6, 16, 22, 0, -6)
                .rot("arm_r", Ease.CATMULL, 0, -16, 0, 6, 8, 16, 0, 6, 16, -16, 0, 6)
                .rot("forearm_l", Ease.CATMULL, 0, -35, 0, 0, 16, -35, 0, 0)
                .prio(ARMS, "arm_l", "arm_r", "forearm_l"));
        // MAT-MOVE-04: setting off: she leans into the first steps and the arms pump once (the cadence itself rises with speed)
        add.accept(clip(MatAnim.MOVE_04)
                .rot("body", 0, 6, 0, 0, 7, 9, 0, 0, 14, 3, 0, 0)
                .rot("chest", 0, 4, 0, 0, 14, 2, 0, 0)
                .rot("arm_l", 0, -20, 0, -4, 7, 10, 0, -4, 14, 0, 0, 0)
                .rot("forearm_l", 0, -30, 0, 0, 14, -10, 0, 0)
                .prio(ARMS, "arm_l", "forearm_l"));
        // MAT-MOVE-05: stopping: a last, shorter step, the torso corrects backwards, the feet come together
        add.accept(clip(MatAnim.MOVE_05)
                .rot("leg_l", 0, -15, 0, 0, 6, -4, 0, 0, 14, 0, 0, 0)
                .rot("leg_r", 0, 12, 0, 0, 6, 2, 0, 0, 10, -2, 0, 0, 14, 0, 0, 0)
                .rot("foot_r", 0, 10, 0, 0, 8, 0, 0, 0)
                .rot("body", 0, 4, 0, 0, 6, -3, 0, 0, 14, 0, 0, 0)
                .pos("body", 0, 0, 0.3F, 0, 8, 0, -0.2F, 0, 14, 0, 0, 0)
                .rot("chest", 0, 0, 0, 0, 6, -3, 0, 0, 14, 0, 0, 0)
                .rot("arm_l", 0, -10, 0, 0, 8, 4, 0, 0, 14, 0, 0, 0)
                .prio(ARMS, "arm_l"));
        // MAT-MOVE-06: running: its own cycle (long reach, airborne bob), forward lean, elbows at right angles, the hem flies
        add.accept(clip(MatAnim.MOVE_06)
                .rot("leg_l", Ease.CATMULL, 0, -38, 0, 0, 6, 34, 0, 0, 12, -38, 0, 0)
                .rot("leg_r", Ease.CATMULL, 0, 34, 0, 0, 6, -38, 0, 0, 12, 34, 0, 0)
                .rot("foot_l", Ease.CATMULL, 0, -14, 0, 0, 3, 5, 0, 0, 6, 35, 0, 0, 9, 15, 0, 0, 12, -14, 0, 0)
                .rot("foot_r", Ease.CATMULL, 0, 35, 0, 0, 3, 15, 0, 0, 6, -14, 0, 0, 9, 5, 0, 0, 12, 35, 0, 0)
                .rot("body", Ease.CATMULL, 0, 12, 4, 0, 6, 12, -4, 0, 12, 12, 4, 0)
                .pos("body", Ease.CATMULL, 0, 0, 0, 0, 3, 0, 0.8F, 0, 6, 0, 0, 0, 9, 0, 0.8F, 0, 12, 0, 0, 0)
                .rot("chest", Ease.CATMULL, 0, 6, -7, 0, 6, 6, 7, 0, 12, 6, -7, 0)
                .rot("arm_l", Ease.CATMULL, 0, 35, 0, -8, 6, -35, 0, -8, 12, 35, 0, -8)
                .rot("arm_r", Ease.CATMULL, 0, -30, 0, 8, 6, 30, 0, 8, 12, -30, 0, 8)
                .rot("forearm_l", Ease.CATMULL, 0, -60, 0, 0, 12, -60, 0, 0)
                .rot("forearm_r", Ease.CATMULL, 0, -45, 0, 0, 12, -45, 0, 0)
                .rot("head", Ease.CATMULL, 0, -8, 0, 0, 12, -8, 0, 0)
                .prio(ARMS, "arm_l", "arm_r", "forearm_l", "forearm_r").additive("head"));
        // MAT-MOVE-07: stepping back: the foot reaches behind and lands toe first, the torso stays upright, the hands guard
        add.accept(clip(MatAnim.MOVE_07)
                .rot("leg_l", Ease.CATMULL, 0, 12, 0, -2, 12, -10, 0, -2, 24, 12, 0, -2)
                .rot("leg_r", Ease.CATMULL, 0, -10, 0, 2, 12, 12, 0, 2, 24, -10, 0, 2)
                .rot("foot_l", Ease.CATMULL, 0, 18, 0, 0, 6, 4, 0, 0, 12, -6, 0, 0, 18, 0, 0, 0, 24, 18, 0, 0)
                .rot("foot_r", Ease.CATMULL, 0, -6, 0, 0, 6, 0, 0, 0, 12, 18, 0, 0, 18, 4, 0, 0, 24, -6, 0, 0)
                .rot("body", Ease.CATMULL, 0, -3, 0, 1, 12, -3, 0, -1, 24, -3, 0, 1)
                .pos("body", Ease.CATMULL, 0, 0, 0, 0, 6, 0, 0.2F, 0, 12, 0, 0, 0, 18, 0, 0.2F, 0, 24, 0, 0, 0)
                .rot("chest", Ease.CATMULL, 0, -2, 0, 0, 24, -2, 0, 0)
                .rot("arm_l", Ease.CATMULL, 0, -10, 0, -4, 24, -10, 0, -4)
                .rot("forearm_l", Ease.CATMULL, 0, -30, 0, 0, 24, -30, 0, 0)
                .prio(ARMS, "arm_l", "forearm_l"));
        // MAT-MOVE-08: approaching the target: steps that shorten as she closes in, the body turned to it, the left hand half raised
        add.accept(clip(MatAnim.MOVE_08)
                .rot("leg_l", Ease.CATMULL, 0, -17, 0, 0, 10, 15, 0, 0, 20, -17, 0, 0)
                .rot("leg_r", Ease.CATMULL, 0, 15, 0, 0, 10, -17, 0, 0, 20, 15, 0, 0)
                .rot("foot_l", Ease.CATMULL, 0, -9, 0, 0, 5, 0, 0, 0, 10, 14, 0, 0, 15, 4, 0, 0, 20, -9, 0, 0)
                .rot("foot_r", Ease.CATMULL, 0, 14, 0, 0, 5, 4, 0, 0, 10, -9, 0, 0, 15, 0, 0, 0, 20, 14, 0, 0)
                .pos("body", Ease.CATMULL, 0, 0, 0, 0, 5, 0, 0.3F, 0, 10, 0, 0, 0, 15, 0, 0.3F, 0, 20, 0, 0, 0)
                .rot("body", Ease.CATMULL, 0, 3, 2, -1, 10, 3, -2, 1, 20, 3, 2, -1)
                .rot("chest", Ease.CATMULL, 0, 5, 0, 0, 20, 5, 0, 0)
                .rot("arm_l", Ease.CATMULL, 0, -25, 0, -6, 10, -28, 0, -6, 20, -25, 0, -6)
                .rot("forearm_l", Ease.CATMULL, 0, -30, 0, 0, 20, -30, 0, 0)
                .rot("hand_l", Ease.CATMULL, 0, 10, 40, 0, 20, 10, 40, 0)
                .rot("arm_r", Ease.CATMULL, 0, -6, 0, 3, 10, 6, 0, 3, 20, -6, 0, 3)
                .prio(ARMS, "arm_l", "forearm_l", "hand_l", "arm_r"));
        // MAT-MOVE-09: a change of direction: the head anticipates, the hips pivot, a short repositioning step, then stable
        add.accept(clip(MatAnim.MOVE_09)
                .rot("head", 0, 0, 25, 0, 6, 0, 10, 0, 12, 0, 0, 0)
                .rot("chest", 0, 0, 10, 0, 6, 0, 4, 0, 12, 0, 0, 0)
                .rot("body", 0, 0, -20, 0, 6, 0, 6, 0, 12, 0, 0, 0)
                .rot("leg_l", 0, 0, 0, 0, 4, 0, 0, -6, 10, 0, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 4, -10, 0, 0, 10, 0, 0, 0)
                .pos("body", 0, 0, 0, 0, 4, 0, -0.3F, 0, 12, 0, 0, 0)
                .additive("head"));
        // MAT-MOVE-10: turning on the spot (mirrored for the other side): head, chest, hips and feet follow one another
        add.accept(clip(MatAnim.MOVE_10)
                .rot("body", Ease.OUT, 0, 0, -30, 0, 8, 0, -10, 0, 16, 0, 0, 0)
                .rot("chest", Ease.OUT, 0, 0, -15, 0, 4, 0, -12, 0, 8, 0, -2, 0, 16, 0, 0, 0)
                .rot("leg_l", 0, 0, 0, 0, 3, -10, 0, 0, 6, 0, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 7, -10, 0, 0, 10, 0, 0, 0)
                .pos("body", 0, 0, 0, 0, 3, 0, 0.2F, 0, 5, 0, 0, 0, 7, 0, 0.2F, 0, 10, 0, 0, 0, 16, 0, 0, 0));

        // MAT-FLOAT-01: lifting off: the step stops, the torso gathers, the legs draw together and trail, the feet point, the arms open
        add.accept(clip(MatAnim.FLOAT_01)
                .rot("leg_l", 0, 0, 0, 0, 10, 8, 0, 1, 24, 12, 0, 1)
                .rot("leg_r", 0, 0, 0, 0, 10, 8, 0, -1, 24, 12, 0, -1)
                .rot("foot_l", 0, 0, 0, 0, 10, 20, 0, 0, 24, 35, 0, 0)
                .rot("foot_r", 0, 0, 0, 0, 10, 20, 0, 0, 24, 35, 0, 0)
                .rot("chest", 0, 0, 0, 0, 8, 6, 0, 0, 16, -4, 0, 0, 24, -2, 0, 0)
                .rot("arm_l", 0, 0, 0, 0, 8, 0, 0, -8, 16, 0, 0, -20, 24, 0, 0, -15)
                .rot("arm_r", 0, 0, 0, 0, 16, 0, 0, 12, 24, 0, 0, 10)
                .rot("forearm_l", 0, 0, 0, 0, 16, -25, 0, 0, 24, -22, 0, 0)
                .rot("head", 0, 0, 0, 0, 16, -6, 0, 0, 24, 0, 0, 0)
                .additive("head").prio(ARMS, "arm_l", "arm_r", "forearm_l"));
        // MAT-FLOAT-02: stable floating: a slow vertical swell, small corrections of balance
        add.accept(clip(MatAnim.FLOAT_02)
                .pos("body", Ease.CATMULL, 0, 0, 0, 0, 15, 0, 0.6F, 0, 30, 0, 0, 0, 45, 0, -0.6F, 0, 60, 0, 0, 0)
                .rot("body", Ease.CATMULL, 0, 0, 0, 1.5F, 20, 0, 0, -1, 40, 0, 0, 1, 60, 0, 0, 1.5F)
                .rot("chest", Ease.CATMULL, 0, -2, 0, 0, 30, -3, 0, 0, 60, -2, 0, 0)
                .rot("leg_l", Ease.CATMULL, 0, 12, 0, 1, 30, 14, 0, 1, 60, 12, 0, 1)
                .rot("leg_r", Ease.CATMULL, 0, 13, 0, -1, 30, 11, 0, -1, 60, 13, 0, -1)
                .rot("foot_l", Ease.CATMULL, 0, 35, 0, 0, 30, 38, 0, 0, 60, 35, 0, 0)
                .rot("foot_r", Ease.CATMULL, 0, 37, 0, 0, 30, 34, 0, 0, 60, 37, 0, 0)
                .rot("arm_l", Ease.CATMULL, 0, 0, 0, -14, 30, 0, 0, -17, 60, 0, 0, -14)
                .rot("arm_r", Ease.CATMULL, 0, 0, 0, 10, 30, 0, 0, 12, 60, 0, 0, 10)
                .rot("forearm_l", Ease.CATMULL, 0, -22, 0, 0, 60, -22, 0, 0)
                .prio(ARMS, "arm_l", "arm_r", "forearm_l"));
        // MAT-FLOAT-03: the elegant glide: leaning into the motion, the legs trailing, the arms swept slightly back
        add.accept(clip(MatAnim.FLOAT_03)
                .rot("body", Ease.CATMULL, 0, 8, 0, 0, 20, 9, 0, 0, 40, 8, 0, 0)
                .pos("body", Ease.CATMULL, 0, 0, 0, 0, 20, 0, 0.3F, 0, 40, 0, 0, 0)
                .rot("chest", Ease.CATMULL, 0, 4, 0, 0, 40, 4, 0, 0)
                .rot("leg_l", Ease.CATMULL, 0, 22, 0, 0, 20, 25, 0, 0, 40, 22, 0, 0)
                .rot("leg_r", Ease.CATMULL, 0, 24, 0, 0, 20, 21, 0, 0, 40, 24, 0, 0)
                .rot("foot_l", Ease.CATMULL, 0, 40, 0, 0, 40, 40, 0, 0)
                .rot("foot_r", Ease.CATMULL, 0, 40, 0, 0, 40, 40, 0, 0)
                .rot("arm_l", Ease.CATMULL, 0, 15, 0, -12, 20, 18, 0, -13, 40, 15, 0, -12)
                .rot("arm_r", Ease.CATMULL, 0, 10, 0, 8, 40, 10, 0, 8)
                .prio(ARMS, "arm_l", "arm_r"));
        // MAT-FLOAT-04: the disturbing glide: sudden reorientations, halts, the legs dangling unevenly, the arms jerking
        add.accept(clip(MatAnim.FLOAT_04)
                .rot("body", Ease.SNAP, 0, 10, 0, 0, 10, 10, 0, 0, 11, 10, 15, 0, 18, 10, 15, 0, 19, -4, 15, 0, 22, -4, 15, 0, 23, -4, -10, 0,
                        30, -4, -10, 0, 31, 12, -10, 0, 36, 12, -10, 0, 37, 12, 0, 0, 48, 10, 0, 0)
                .rot("leg_l", Ease.CATMULL, 0, 30, 0, 0, 24, 10, 0, 0, 48, 30, 0, 0)
                .rot("leg_r", Ease.CATMULL, 0, 10, 0, 0, 24, 32, 0, 0, 48, 10, 0, 0)
                .rot("foot_l", Ease.CATMULL, 0, 45, 0, 0, 48, 45, 0, 0)
                .rot("foot_r", Ease.CATMULL, 0, 30, 0, 0, 24, 50, 0, 0, 48, 30, 0, 0)
                .rot("arm_l", Ease.SNAP, 0, 0, 0, -20, 10, 0, 0, -20, 11, 0, 0, -40, 22, 0, 0, -40, 23, 0, 0, -10, 36, 0, 0, -10, 37, 0, 0, -25, 48, 0, 0, -20)
                .rot("head", Ease.SNAP, 0, 0, 0, 0, 10, 0, 0, 0, 11, 0, 0, 15, 22, 0, 0, 15, 23, 0, 0, -10, 36, 0, 0, -10, 37, 0, 0, 0, 48, 0, 0, 0)
                .prio(ARMS, "arm_l").additive("head"));
        // MAT-FLOAT-05: the aggressive dash (the needle thrust): she crouches and draws back (0-14), lifts off and shoots forward
        // leaning into it, legs trailing and arms swept back (14-24), then rights herself and lands (24-38)
        add.accept(clip(MatAnim.FLOAT_05)
                .rot("chest", 0, 0, 0, 0, 14, 14, 0, 0, 18, 6, 0, 0, 24, 4, 0, 0, 38, 0, 0, 0)
                .rot("body", Ease.IN, 0, 0, 0, 0, 12, -4, 0, 0, 16, 25, 0, 0, 24, 25, 0, 0, 32, 6, 0, 0, 38, 0, 0, 0)
                .pos("body", Ease.OUT, 0, 0, 0, 0, 12, 0, -1, 0, 16, 0, 1.5F, 0, 24, 0, 1.5F, 0, 32, 0, 0.2F, 0, 38, 0, 0, 0)
                .rot("leg_l", 0, 0, 0, 0, 12, -10, 0, 0, 16, 35, 0, 0, 24, 35, 0, 0, 32, 4, 0, 0, 38, 0, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 12, -6, 0, 0, 16, 32, 0, 0, 24, 32, 0, 0, 32, 2, 0, 0, 38, 0, 0, 0)
                .rot("foot_l", 0, 0, 0, 0, 16, 45, 0, 0, 24, 45, 0, 0, 34, 0, 0, 0)
                .rot("foot_r", 0, 0, 0, 0, 16, 45, 0, 0, 24, 45, 0, 0, 34, 0, 0, 0)
                .rot("arm_l", 0, 0, 0, 0, 12, 40, 0, -6, 16, 50, 0, -10, 24, 50, 0, -10, 30, -20, 0, -8, 38, 0, 0, 0)
                .rot("arm_r", 0, 0, 0, 0, 12, 20, 0, 4, 16, 30, 0, 6, 24, 30, 0, 6, 38, 0, 0, 0)
                .rot("head", 0, 0, 0, 0, 12, -10, 0, 0, 16, -18, 0, 0, 24, -14, 0, 0, 38, 0, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 12, 55, 55, 55, 55, 40, 0, 16, -15, 50, 55, 60, 35, -4, 30, -15, 50, 55, 60, 35, -4, 38, 0, 0, 0, 0, 0, 0));
        // MAT-FLOAT-06: rising: legs hanging straight, feet pointed, arms lifting, the face turned up
        add.accept(clip(MatAnim.FLOAT_06)
                .rot("leg_l", Ease.CATMULL, 0, 2, 0, 1, 30, 2, 0, 1)
                .rot("leg_r", Ease.CATMULL, 0, 3, 0, -1, 30, 3, 0, -1)
                .rot("foot_l", Ease.CATMULL, 0, 45, 0, 0, 30, 45, 0, 0)
                .rot("foot_r", Ease.CATMULL, 0, 45, 0, 0, 30, 45, 0, 0)
                .rot("arm_l", Ease.CATMULL, 0, 0, 0, -30, 15, 0, 0, -34, 30, 0, 0, -30)
                .rot("arm_r", Ease.CATMULL, 0, 0, 0, 20, 15, 0, 0, 24, 30, 0, 0, 20)
                .rot("forearm_l", Ease.CATMULL, 0, -15, 0, 0, 30, -15, 0, 0)
                .rot("chest", Ease.CATMULL, 0, -6, 0, 0, 30, -6, 0, 0)
                .rot("head", Ease.CATMULL, 0, -10, 0, 0, 30, -10, 0, 0)
                .pos("body", Ease.CATMULL, 0, 0, 0, 0, 15, 0, 0.3F, 0, 30, 0, 0, 0)
                .prio(ARMS, "arm_l", "arm_r", "forearm_l").additive("head"));
        // MAT-FLOAT-07: sinking: the torso leans, the legs come forward to meet the ground, the arms out for balance
        add.accept(clip(MatAnim.FLOAT_07)
                .rot("chest", Ease.CATMULL, 0, 8, 0, 0, 30, 8, 0, 0)
                .rot("leg_l", Ease.CATMULL, 0, -8, 0, 1, 15, -6, 0, 1, 30, -8, 0, 1)
                .rot("leg_r", Ease.CATMULL, 0, -6, 0, -1, 15, -8, 0, -1, 30, -6, 0, -1)
                .rot("foot_l", Ease.CATMULL, 0, 10, 0, 0, 30, 10, 0, 0)
                .rot("foot_r", Ease.CATMULL, 0, 10, 0, 0, 30, 10, 0, 0)
                .rot("arm_l", Ease.CATMULL, 0, 0, 0, -20, 15, 0, 0, -22, 30, 0, 0, -20)
                .rot("arm_r", Ease.CATMULL, 0, 0, 0, 15, 15, 0, 0, 17, 30, 0, 0, 15)
                .prio(ARMS, "arm_l", "arm_r"));
        // MAT-FLOAT-08: still suspension: almost nothing, but never frozen
        add.accept(clip(MatAnim.FLOAT_08)
                .pos("body", Ease.CATMULL, 0, 0, 0, 0, 40, 0, 0.15F, 0, 80, 0, 0, 0)
                .rot("chest", Ease.CATMULL, 0, -1, 0, 0, 40, -1.5F, 0, 0, 80, -1, 0, 0)
                .rot("arm_l", Ease.CATMULL, 0, 0, 0, -12, 40, 0, 0, -13, 80, 0, 0, -12)
                .rot("arm_r", Ease.CATMULL, 0, 0, 0, 8, 40, 0, 0, 9, 80, 0, 0, 8)
                .rot("leg_l", Ease.CATMULL, 0, 14, 0, 1, 80, 14, 0, 1)
                .rot("leg_r", Ease.CATMULL, 0, 14, 0, -1, 80, 14, 0, -1)
                .rot("foot_l", Ease.CATMULL, 0, 38, 0, 0, 80, 38, 0, 0)
                .rot("foot_r", Ease.CATMULL, 0, 38, 0, 0, 80, 38, 0, 0)
                .prio(ARMS, "arm_l", "arm_r"));
        // MAT-FLOAT-09: landing: the feet reach, touch, the body absorbs the contact, then the posture recovers
        add.accept(clip(MatAnim.FLOAT_09)
                .rot("leg_l", 0, 12, 0, 1, 8, -4, 0, 0, 12, -10, 0, 0, 20, 0, 0, 0)
                .rot("leg_r", 0, 12, 0, -1, 8, -4, 0, 0, 12, -8, 0, 0, 20, 0, 0, 0)
                .rot("foot_l", 0, 30, 0, 0, 8, 0, 0, 0, 20, 0, 0, 0)
                .rot("foot_r", 0, 30, 0, 0, 8, 0, 0, 0, 20, 0, 0, 0)
                .pos("body", Ease.OUT, 0, 0, 0, 0, 8, 0, 0, 0, 10, 0, -1, 0, 14, 0, -0.6F, 0, 20, 0, 0, 0)
                .rot("chest", 0, 0, 0, 0, 10, 8, 0, 0, 20, 0, 0, 0)
                .rot("arm_l", 0, 0, 0, -12, 8, 0, 0, -20, 20, 0, 0, 0)
                .rot("arm_r", 0, 0, 0, 8, 8, 0, 0, 15, 20, 0, 0, 0)
                .prio(ARMS, "arm_l", "arm_r"));
    }
}
