package com.codenamecoraline.client.ten_threads_witch.matriarch;

import static com.codenamecoraline.client.ten_threads_witch.matriarch.ClipBuilder.clip;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatAnim;
import java.util.function.Consumer;

/**
 * 6.13 Stage transitions (MAT-PHASE-01 to 05), 6.14 cinematics (MAT-CIN-01, 02, 03, 05, 06; MAT-CIN-04 is the server
 * choosing the transition of the stage that begins) and 7, the death. These clips own the whole body at a strong priority,
 * cloth and hair included where the moment needs it (their springs are damped under them). The entrance reveals her with the
 * concealment channel (silhouette first, the hands last); the death dissolves her with the dissolve channel (the hands
 * first, the smile last) while the face goes from disbelief to despair to the smile.
 */
final class CinematicClips {
    private CinematicClips() {}

    static void register(Consumer<ClipBuilder> add) {
        // MAT-PHASE-01, composed to irritated (64): she stops and looks at her wheel; a thread snaps (20) and she flinches; she
        // draws herself up, taller, the shoulders lifting, the cloth stirring; a surge (38) flings her arms open; then her hands
        // come forward to the threads (50) in a new, wider, harder pose
        add.accept(clip(MatAnim.PHASE_01)
                .rot("head", 0, 0, 0, 0, 10, 15, -20, 0, 18, 15, -20, 0, 20, -12, 0, 10, 30, -6, 0, 0, 38, -15, 0, 0, 50, -4, 0, 0, 64, -8, 0, 0)
                .rot("chest", 0, 0, 0, 0, 10, 4, -10, 0, 18, 4, -10, 0, 20, -8, 0, 6, 30, -6, 0, 0, 38, -12, 0, 0, 50, 4, 0, 0, 64, 8, 0, 0)
                .rot("arm_l", 0, 0, 0, 0, 10, -25, 0, -6, 18, -25, 0, -6, 20, -10, 0, -12, 30, 0, 0, -20, 38, -40, 0, -45, 50, -40, 0, -14, 64, -35, 0, -14)
                .rot("forearm_l", 0, 0, 0, 0, 10, -50, 0, 0, 20, -20, 0, 0, 38, -20, 0, 0, 50, -70, 0, 0, 64, -65, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 10, 0, 60, 0, 38, 0, 0, 0, 50, 0, 90, 0, 64, 0, 90, 0)
                .rot("arm_r", 0, 0, 0, 0, 10, -20, 0, 6, 30, -10, 0, 15, 38, -40, 0, 45, 50, -30, 0, 12, 64, -28, 0, 10)
                .rot("forearm_r", 0, 0, 0, 0, 38, -20, 0, 0, 64, -45, 0, 0)
                .fingers('l', Ease.OUT, 0, 0, 0, 0, 0, 0, 0, 18, 20, 25, 25, 30, 15, 2, 20, -15, -15, -15, -15, -10, 10, 38, -20, -20, -20, -20, -10, 12, 50, 35, 35, 35, 35, 25, 0, 64, 25, 30, 30, 35, 20, 2)
                .pos("mantle", 0, 0, 0, 0, 30, 0, 0.4F, 0, 64, 0, 0.3F, 0)
                .pos("body", 0, 0, 0, 0, 30, 0, 0.3F, 0, 38, 0, 0.6F, 0, 64, 0, 0, 0)
                .rot("skirt", 0, 0, 0, 0, 30, 25, 0, 0, 38, 30, 0, 0, 50, 12, 0, 0, 64, 0, 0, 0)
                .rot("skirt_hem", 0, 0, 0, 0, 34, 30, 0, 0, 50, 10, 0, 0, 64, 0, 0, 0)
                .rot("cloak_l", 0, 0, 0, 0, 38, 0, 0, -25, 64, 0, 0, 0)
                .rot("cloak_r", 0, 0, 0, 0, 38, 0, 0, 25, 64, 0, 0, 0)
                .rot("hair_lock_l", 0, 0, 0, 0, 38, 0, 0, -20, 64, 0, 0, 0)
                .rot("hair_lock_r", 0, 0, 0, 0, 38, 0, 0, 20, 64, 0, 0, 0)
                .fx(MatRig.FX_THREAD_L, 0, 1, 0.6F, 0, 18, 1, 0.9F, 0, 20, 1, 1, 1, 28, 0.2F, 0, 0, 50, 0.2F, 0, 0, 58, 1, 0.8F, 0, 64, 1, 0.7F, 0));
        // MAT-PHASE-02, irritated to unravelling (50): every thread snaps (4) and she crumples to her knees (14); kneeling she claws
        // at her face, the head shaking; a surge (30) snaps her head up, arches her back, flings the arms wide, the cloth flaring
        add.accept(clip(MatAnim.PHASE_02)
                .rot("chest", Ease.SNAP, 0, 0, 0, 0, 4, 20, 0, -10, 14, 35, 0, 0, 22, 30, 0, 0, 28, 32, 0, 0, 30, -25, 0, 0, 40, -22, 0, 3, 50, -25, 0, 0)
                .rot("head", Ease.SNAP, 0, 0, 0, 0, 4, 30, 0, -20, 14, 40, 0, 0, 18, 40, 15, 0, 20, 40, -15, 0, 22, 40, 15, 0, 24, 40, -12, 0, 28, 40, 0, 0, 30, -40, 0, 0, 50, -40, 0, 5)
                .pos("body", Ease.IN, 0, 0, 0, 0, 4, 0, -0.5F, 0, 14, 0, -3.5F, 0, 50, 0, -3.5F, 0)
                .rot("leg_l", Ease.IN, 0, 0, 0, 0, 4, 10, 0, 0, 14, 85, 0, 0, 50, 85, 0, 0)
                .rot("leg_r", Ease.IN, 0, 0, 0, 0, 4, 10, 0, 0, 14, 85, 0, 0, 50, 85, 0, 0)
                .rot("foot_l", 0, 0, 0, 0, 14, -10, 0, 0, 50, -10, 0, 0)
                .rot("foot_r", 0, 0, 0, 0, 14, -10, 0, 0, 50, -10, 0, 0)
                .rot("arm_l", Ease.SNAP, 0, 0, 0, 0, 4, -40, 0, -50, 14, 10, 0, -5, 22, -60, 0, -10, 28, -60, 0, -10, 30, -30, 0, -70, 50, -30, 0, -70)
                .rot("forearm_l", 0, 0, 0, 0, 14, 0, 0, 0, 22, -110, 0, 0, 28, -110, 0, 0, 30, -10, 0, 0, 50, -10, 0, 0)
                .rot("arm_r", Ease.SNAP, 0, 0, 0, 0, 4, -40, 0, 50, 14, 10, 0, 5, 22, -50, 0, 10, 28, -50, 0, 10, 30, -30, 0, 70, 50, -30, 0, 70)
                .rot("forearm_r", 0, 0, 0, 0, 22, -100, 0, 0, 28, -100, 0, 0, 30, -10, 0, 0, 50, -10, 0, 0)
                .fingers('l', Ease.STEP, 0, 0, 0, 0, 0, 0, 0, 4, -20, -20, -20, -20, -10, 12, 22, 60, 20, 60, 20, 30, 0, 24, 20, 60, 20, 60, 30, 0, 26, 60, 60, 60, 60, 30, 0, 30, -25, -25, -25, -25, -15, 14, 50, -25, -25, -25, -25, -15, 14)
                .rot("skirt", 0, 0, 0, 0, 30, 0, 0, 0, 36, 40, 0, 0, 50, 35, 0, 0)
                .rot("cloak_l", 0, 0, 0, 0, 30, 0, 0, 0, 36, 0, 0, -35, 50, 0, 0, -30)
                .rot("cloak_r", 0, 0, 0, 0, 30, 0, 0, 0, 36, 0, 0, 35, 50, 0, 0, 30)
                .rot("hair_lock_l", 0, 0, 0, 0, 30, 0, 0, 0, 36, -20, 0, -35, 50, -15, 0, -30)
                .rot("hair_lock_r", 0, 0, 0, 0, 30, 0, 0, 0, 36, -20, 0, 35, 50, -15, 0, 30)
                .rot("hood_tip", 0, 0, 0, 0, 36, -30, 0, 0, 50, -25, 0, 0)
                .fx(MatRig.FX_THREAD_L, Ease.LINEAR, 0, 1, 0.6F, 0, 4, 1, 1, 1, 10, 0, 0, 0, 50, 0, 0, 0)
                .fx(MatRig.FX_THREAD_R, Ease.LINEAR, 0, 1, 0.6F, 0, 4, 1, 1, 1, 10, 0, 0, 0, 50, 0, 0, 0)
                .fx(MatRig.FX_GLOW, 0, 0, 0, 0, 30, 0, 0, 0, 34, 1, 1, 0, 50, 0.8F, 1, 0));
        // MAT-PHASE-03, the supernatural recovery of posture (28): lifted from her knees by the chest as if on strings, limbs and
        // head hanging, turned slowly, set down on her feet; the head snaps upright last
        add.accept(clip(MatAnim.PHASE_03)
                .pos("body", Ease.OUT, 0, 0, -3.5F, 0, 6, 0, 2, 0, 16, 0, 2.2F, 0, 22, 0, 0, 0, 28, 0, 0, 0)
                .rot("body", 0, 0, 0, 0, 6, 0, 0, 0, 16, 0, 20, 0, 22, 0, 10, 0, 28, 0, 0, 0)
                .rot("leg_l", 0, 85, 0, 0, 6, 10, 0, 0, 22, 0, 0, 0)
                .rot("leg_r", 0, 85, 0, 0, 6, 12, 0, 0, 22, 0, 0, 0)
                .rot("foot_l", 0, -10, 0, 0, 6, 40, 0, 0, 20, 40, 0, 0, 24, 0, 0, 0)
                .rot("foot_r", 0, -10, 0, 0, 6, 40, 0, 0, 20, 40, 0, 0, 24, 0, 0, 0)
                .rot("chest", 0, -25, 0, 0, 6, 12, 0, 0, 22, 10, 0, 4, 28, 10, 0, 6)
                .rot("head", Ease.SNAP, 0, -40, 0, 5, 6, 35, 0, 15, 22, 35, 0, 15, 24, -8, 0, 12, 28, -8, 0, 12)
                .rot("arm_l", 0, -30, 0, -70, 6, 15, 0, -10, 22, 15, 0, -10, 28, -15, 0, -10)
                .rot("arm_r", 0, -30, 0, 70, 6, 15, 0, 10, 22, 15, 0, 10, 28, -6, 0, 5)
                .rot("forearm_l", 0, -10, 0, 0, 6, 0, 0, 0, 28, -35, 0, 0)
                .fx(MatRig.FX_THREAD_L, 0, 0, 0, 0, 6, 1, 1, 0, 22, 1, 1, 0, 28, 0, 0, 0)
                .fx(MatRig.FX_THREAD_R, 0, 0, 0, 0, 6, 1, 1, 0, 22, 1, 1, 0, 28, 0, 0, 0));
        // MAT-PHASE-05, the body and the face intensify (26): she stretches crookedly, the fingers curl one by one, the arms float up
        // forward with limp wrists like a marionette's, the shoulders jerk (12, a cackle)
        add.accept(clip(MatAnim.PHASE_05)
                .rot("chest", 0, 10, 0, 6, 10, -10, 0, 15, 20, 6, 0, 12, 26, 12, 0, 8)
                .rot("head", Ease.SNAP, 0, -8, 0, 12, 10, -6, 0, 20, 20, 0, 0, 40, 26, 4, 0, 20)
                .rot("arm_l", 0, -15, 0, -10, 12, -50, 0, -10, 26, -80, 0, -10)
                .rot("arm_r", 0, -6, 0, 5, 12, -40, 0, 8, 26, -70, 0, 10)
                .rot("forearm_l", 0, -35, 0, 0, 26, -10, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 20, 40, 0, 0, 26, 45, 0, 0)
                .pos("mantle", Ease.STEP, 0, 0, 0, 0, 12, 0, 0.5F, 0, 14, 0, 0, 0, 26, 0, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 6, 40, 0, 0, 0, 0, 0, 10, 40, 40, 0, 0, 0, 0, 14, 40, 40, 40, 0, 0, 0, 18, 40, 40, 40, 40, 20, 0, 26, 10, 25, 40, 55, 10, -4));
        // MAT-PHASE-04, the threads manifested wide (30): arms thrown wide and up, fingers splayed, threads bursting everywhere (8);
        // then the hands sweep down (20) into the stance of the last phase
        add.accept(clip(MatAnim.PHASE_04)
                .rot("arm_l", Ease.OUT, 0, -80, 0, -10, 6, -150, 0, -60, 16, -150, 0, -60, 22, -45, 0, -14, 30, -40, 0, -12)
                .rot("arm_r", Ease.OUT, 0, -70, 0, 10, 6, -140, 0, 60, 16, -140, 0, 60, 22, -40, 0, 12, 30, -35, 0, 10)
                .rot("forearm_l", 0, -10, 0, 0, 6, -10, 0, 0, 22, -60, 0, 0, 30, -60, 0, 0)
                .rot("forearm_r", 0, -10, 0, 0, 22, -55, 0, 0, 30, -55, 0, 0)
                .rot("chest", 0, 12, 0, 8, 6, -15, 0, 0, 16, -15, 0, 0, 22, 12, 0, 0, 30, 12, 0, 0)
                .rot("head", 0, 4, 0, 20, 6, -20, 0, 0, 16, -20, 0, 0, 30, -6, 0, 0)
                .rot("leg_l", 0, 0, 0, 0, 22, 0, 0, -6, 30, 0, 0, -6)
                .rot("leg_r", 0, 0, 0, 0, 22, 0, 0, 6, 30, 0, 0, 6)
                .fingers('l', Ease.OUT, 0, 10, 25, 40, 55, 10, -4, 6, -25, -25, -25, -25, -15, 14, 16, -25, -25, -25, -25, -15, 14, 22, 55, 55, 55, 55, 35, -4, 30, 55, 55, 55, 55, 35, -4)
                .fingers('r', Ease.OUT, 0, 0, 0, 0, 0, 0, 0, 6, -45, -45, -35, -35, -15, 12, 16, -45, -45, -35, -35, -15, 12, 22, 0, 0, 0, 0, 0, 0)
                .fx(MatRig.FX_ATTACK, 0, 0, 0, 0, 6, 1, 1, 1, 16, 1, 1, 1, 24, 0, 0, 0)
                .fx(MatRig.FX_THREAD_L, 0, 0, 0, 0, 8, 1, 1, 0, 20, 1, 1, 0, 30, 0.6F, 0.6F, 0)
                .fx(MatRig.FX_THREAD_R, 0, 0, 0, 0, 8, 1, 1, 0, 20, 1, 1, 0, 30, 0.6F, 0.6F, 0)
                .fx(MatRig.FX_GLOW, 0, 0.8F, 1, 0, 30, 0.6F, 0.6F, 0));

        // MAT-CIN-01, the entrance (60): threads rise around the arena (1); she appears out of them curled like a cocoon, arms
        // crossed, floating a little; the silhouette comes first, then the hair and the dress, the hands last; she uncurls
        add.accept(clip(MatAnim.CIN_01)
                .fx(MatRig.FX_DISSOLVE, Ease.SMOOTH, 0, 0, 1, 0, 8, 0, 1, 0, 40, 0, 0.35F, 0, 56, 0, 0, 0, 60, 0, 0, 0)
                .pos("body", 0, 0, 1, 0, 40, 0, 1, 0, 60, 0, 0, 0)
                .rot("chest", 0, 25, 0, 0, 20, 25, 0, 0, 40, 10, 0, 0, 60, 8, 0, 0)
                .rot("head", 0, 40, 0, 0, 20, 40, 0, 0, 40, 25, 0, 0, 60, 20, 0, 0)
                .rot("arm_l", 0, -60, 0, 35, 60, -60, 0, 35)
                .rot("forearm_l", 0, -100, 0, 0, 60, -100, 0, 0)
                .rot("arm_r", 0, -55, 0, -30, 60, -55, 0, -30)
                .rot("forearm_r", 0, -100, 0, 0, 60, -100, 0, 0)
                .rot("leg_l", 0, 6, 0, -2, 40, 6, 0, -2, 60, 0, 0, 0)
                .rot("leg_r", 0, 6, 0, 2, 40, 6, 0, 2, 60, 0, 0, 0)
                .rot("foot_l", 0, 30, 0, 0, 40, 30, 0, 0, 60, 0, 0, 0)
                .rot("foot_r", 0, 30, 0, 0, 40, 30, 0, 0, 60, 0, 0, 0)
                .rot("skirt", Ease.CATMULL, 0, 0, 0, 0, 30, 10, 0, 0, 45, -6, 0, 0, 60, 4, 0, 0)
                .rot("hair_lock_l", Ease.CATMULL, 0, 0, 0, 0, 35, 0, 0, -15, 50, 0, 0, 10, 60, 0, 0, 0)
                .rot("hair_lock_r", Ease.CATMULL, 0, 0, 0, 0, 35, 0, 0, 15, 50, 0, 0, -10, 60, 0, 0, 0)
                .fingers('l', 0, 30, 30, 30, 30, 20, 0, 60, 30, 30, 30, 30, 20, 0));
        // MAT-CIN-02, the presentation (50): the head rises and she looks at the player; the arms uncross and open, palm up, the
        // fingers unfurling one by one (24-34); threads bloom from the hand (30); a small curtsey to finish
        add.accept(clip(MatAnim.CIN_02)
                .rot("head", 0, 20, 0, 0, 10, 20, 0, 0, 22, -6, 0, 0, 40, -6, 0, 4, 44, 10, 0, 0, 50, 0, 0, 0)
                .rot("chest", 0, 8, 0, 0, 22, -2, 0, 0, 44, 6, 0, 0, 50, 0, 0, 0)
                .rot("arm_l", 0, -60, 0, 35, 18, -60, 0, 35, 30, -40, 0, -35, 44, -30, 0, -30, 50, -30, 0, -8)
                .rot("forearm_l", 0, -100, 0, 0, 18, -100, 0, 0, 30, -40, 0, 0, 50, -55, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 30, 0, -90, 0, 44, 0, -90, 0, 50, 0, 90, 0)
                .rot("arm_r", 0, -55, 0, -30, 18, -55, 0, -30, 30, -20, 0, 10, 50, -20, 0, 6)
                .rot("forearm_r", 0, -100, 0, 0, 18, -100, 0, 0, 30, -20, 0, 0, 50, -30, 0, 0)
                .fingers('l', 0, 30, 30, 30, 30, 20, 0, 24, 30, 30, 30, 30, 20, 0, 26, -10, 30, 30, 30, 20, 2, 28, -10, -10, 30, 30, 20, 4, 30, -10, -10, -10, 30, 20, 6,
                        32, -10, -10, -10, -10, 20, 8, 34, -10, -10, -10, -10, -8, 8, 50, 20, 25, 25, 30, 15, 2)
                .rot("body", 0, 0, 0, 0, 40, 0, 0, 0, 44, 12, 0, 0, 50, 0, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 40, 0, 0, 0, 44, 12, 0, 0, 50, 0, 0, 0)
                .fx(MatRig.FX_THREAD_L, 0, 0, 0, 0, 30, 0, 0, 0, 40, 0.6F, 0.3F, 0, 50, 0.6F, 0.4F, 0));
        // MAT-CIN-03, the first show of power (44): the left hand rises high, fingers splayed (10), and snaps shut, throwing threads
        // to the ten hanging puppets (14); a yank (20); the staff lifts and the wheel spins as the power bursts (24); the final
        // pose of the entrance: the hand of the marionettist held high, the staff forward, the chin up
        add.accept(clip(MatAnim.CIN_03)
                .rot("arm_l", Ease.OUT, 0, -30, 0, -8, 10, -170, 0, -15, 14, -170, 0, -15, 20, -60, 0, -20, 36, -100, 0, -20, 44, -100, 0, -20)
                .rot("forearm_l", 0, -55, 0, 0, 10, -10, 0, 0, 20, -70, 0, 0, 36, -50, 0, 0, 44, -50, 0, 0)
                .rot("hand_l", 0, 0, 90, 0, 44, 0, 90, 0)
                .fingers('l', Ease.OUT, 0, 20, 25, 25, 30, 15, 2, 10, -20, -20, -20, -20, -10, 12, 14, 55, 55, 55, 55, 40, 0, 20, 45, 45, 45, 45, 30, 0, 36, 20, 25, 25, 30, 15, 2, 44, 20, 25, 25, 30, 15, 2)
                .rot("arm_r", 0, -20, 0, 6, 20, -20, 0, 6, 24, -90, 0, 10, 30, -60, 0, 8, 36, -40, 0, 6, 44, -40, 0, 6)
                .rot("forearm_r", 0, -30, 0, 0, 24, -10, 0, 0, 36, -30, 0, 0, 44, -30, 0, 0)
                .rot("wheel", Ease.LINEAR, 0, 0, 0, 0, 20, 0, 0, 0, 34, 0, 0, 720, 44, 0, 0, 720)
                .rot("chest", 0, 0, 0, 0, 20, 6, 0, 0, 36, -4, 0, 0, 44, -4, 0, 0)
                .rot("head", 0, 0, 0, 0, 10, -15, 0, 0, 20, 0, 0, 0, 36, -8, 0, 0, 44, -8, 0, 0)
                .rot("leg_l", 0, 0, 0, 0, 36, -6, 0, 0, 44, -6, 0, 0)
                .fx(MatRig.FX_THREAD_L, Ease.STEP, 0, 0.6F, 0.4F, 0, 14, 1, 1, 0, 30, 1, 0.8F, 0, 44, 1, 0.7F, 0)
                .fx(MatRig.FX_ATTACK, 0, 0, 0, 0, 14, 1, 1, 0, 24, 1, 1, 1, 34, 0, 0, 0));

        // MAT-CIN-05, the defeat (116), section 7 phases 1 to 5 (the face is owned by this clip: the face slot is cleared)
        add.accept(clip(MatAnim.CIN_05)
                // phase 1, resistance (0-28): she holds her posture; the arms tremble, the fingers lose their precision, she shakes her head
                .rot("chest", 0, 10, 0, 0, 14, 6, 0, 0, 28, 8, 0, 0, 44, 14, 0, 4, 60, 18, 0, 6, 76, 22, 0, 6, 96, 24, 0, 6, 116, 26, 0, 8)
                .rot("head", 0, 0, 0, 0, 6, -6, 0, 0, 8, -6, 8, 0, 16, -6, -8, 0, 24, -6, 8, 0, 28, -2, 0, 0, 40, 4, 0, -6, 56, 10, 0, 6,
                        70, 18, 0, 10, 96, 20, 0, 14, 116, 22, 0, 16)
                .rot("arm_l", Ease.STEP, 0, -30, 0, -8, 4, -30, 0, -14, 6, -30, 0, -4, 8, -30, 0, -12, 10, -30, 0, -6, 14, -28, 0, -14, 18, -28, 0, -4, 22, -26, 0, -12, 26, -26, 0, -6,
                        28, -10, 0, -10, 40, -12, 0, -8, 48, -6, 0, -8, 64, 0, 0, -6, 76, 5, 0, -6, 116, 5, 0, -6)
                .rot("forearm_l", Ease.OUT, 0, -55, 0, 0, 26, -55, 0, 0, 28, -100, 0, 0, 32, -20, 0, 0, 48, -25, 0, 0, 64, -10, 0, 0, 76, 0, 0, 0, 116, 0, 0, 0)
                .rot("arm_r", Ease.OUT, 0, -20, 0, 6, 34, -20, 0, 6, 36, -5, 0, 14, 40, 0, 0, 8, 64, 4, 0, 5, 116, 4, 0, 5)
                .rot("forearm_r", Ease.OUT, 0, -40, 0, 0, 34, -40, 0, 0, 38, -5, 0, 0, 116, 0, 0, 0)
                .fingers('l', Ease.STEP, 0, 20, 25, 25, 30, 15, 2, 6, 26, 18, 30, 24, 15, 2, 12, 14, 30, 20, 34, 15, 2, 18, 28, 20, 32, 22, 15, 2, 24, 16, 30, 18, 36, 15, 2,
                        28, -20, -20, -20, -20, -10, 10, 40, 30, -10, 35, 5, 10, 4, 52, 5, 25, 0, 30, 5, 3, 64, 0, 10, 20, 30, 0, 2, 76, 10, 10, 30, 40, 0, 0, 116, 10, 10, 30, 40, 0, 0)
                .fingers('r', Ease.STEP, 0, 0, 0, 0, 0, 0, 0, 36, -40, -30, -20, -10, -10, 6, 48, -20, -20, -20, -20, -10, 0, 76, -40, -40, -40, -40, -20, 0, 116, -40, -40, -40, -40, -20, 0)
                // phase 2, loss of control (28-56): legs buckle, the body fails in small ways
                .pos("body", 0, 0, 0, 0, 40, 0, 0, 0, 44, 0, -1, 0, 50, 0, -0.6F, 0, 76, 0, -0.8F, 0, 96, 0, -1.2F, 0, 116, 0, -3, 0)
                .rot("leg_l", 0, 0, 0, 0, 40, 0, 0, 0, 44, -10, 0, 0, 52, -4, 0, 0, 96, -6, 0, 0, 116, 20, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 42, 0, 0, 0, 46, -8, 0, 0, 54, -3, 0, 0, 96, -4, 0, 0, 116, 18, 0, 0)
                // the threads: snapping (28, 36, 56), slackening (48, 64), vanishing (72)
                .fx(MatRig.FX_THREAD_L, Ease.LINEAR, 0, 1, 0.7F, 0, 27, 1, 0.9F, 0, 28, 1, 1, 1, 34, 0.7F, 0.3F, 0.4F, 48, 0.7F, 0.1F, 0, 56, 0.6F, 1, 1, 60, 0.5F, 0, 0.3F, 64, 0.4F, 0, 0, 72, 0, 0, 0, 116, 0, 0, 0)
                .fx(MatRig.FX_THREAD_R, Ease.LINEAR, 0, 0.6F, 0.5F, 0, 35, 0.6F, 0.6F, 0, 36, 0.6F, 1, 1, 42, 0.4F, 0.1F, 0.3F, 64, 0.3F, 0, 0, 72, 0, 0, 0, 116, 0, 0, 0)
                // phase 4, disintegration (76-116): hands and fingers first, then the arms, the staff, the legs, the cloak and the body
                .fx(MatRig.FX_DISSOLVE, Ease.LINEAR, 0, 0, 0, 0, 76, 0, 0, 0, 86, 0.2F, 0, 0, 96, 0.45F, 0, 0, 108, 0.7F, 0, 0, 116, 0.8F, 0, 0)
                // phase 5, the face: disbelief (0-30: eyes wide, brows up, the head back), despair (30-70: brows knotted up in the
                // middle, the corners down, the eyes darting), and the smile that stays (70-116)
                .scale("eye_l", Ease.SMOOTH, 0, 1.4F, 1.4F, 1, 30, 1.4F, 1.4F, 1, 40, 1, 0.8F, 1, 70, 1, 0.8F, 1, 84, 1, 0.5F, 1, 116, 1, 0.5F, 1)
                .scale("eye_r", Ease.SMOOTH, 0, 1.4F, 1.4F, 1, 30, 1.4F, 1.4F, 1, 40, 1, 0.8F, 1, 70, 1, 0.8F, 1, 84, 1, 0.5F, 1, 116, 1, 0.5F, 1)
                .pos("eyes", Ease.STEP, 0, 0, 0, 0, 38, 0, 0, 0, 40, 0.3F, 0, 0, 44, -0.3F, 0.1F, 0, 48, 0.25F, -0.1F, 0, 52, -0.2F, 0, 0, 58, 0, 0, 0, 116, 0, 0, 0)
                .pos("brow_l", 0, 0, 0.4F, 0, 30, 0, 0.4F, 0, 40, 0, 0.3F, 0, 70, 0, 0.3F, 0, 84, 0, 0.1F, 0, 116, 0, 0.15F, 0)
                .pos("brow_r", 0, 0, 0.4F, 0, 30, 0, 0.4F, 0, 40, 0, 0.3F, 0, 70, 0, 0.3F, 0, 84, 0, -0.05F, 0, 116, 0, 0, 0)
                .rot("brow_l", 0, 0, 0, 0, 30, 0, 0, 0, 40, 0, 0, 18, 70, 0, 0, 18, 84, 0, 0, 4, 116, 0, 0, 8)
                .rot("brow_r", 0, 0, 0, 0, 30, 0, 0, 0, 40, 0, 0, -18, 70, 0, 0, -18, 84, 0, 0, 10, 116, 0, 0, 12)
                .scale("mouth", 0, 1, 1.3F, 1, 30, 1, 1.4F, 1, 40, 1.1F, 1.2F, 1, 50, 1.1F, 1.5F, 1, 60, 1.1F, 1.2F, 1, 70, 1.1F, 1.3F, 1, 90, 1.7F, 1.1F, 1, 116, 1.8F, 1.1F, 1)
                .rot("mouth_l", 0, 0, 0, 0, 30, 0, 0, 0, 40, 0, 0, 20, 70, 0, 0, 20, 90, 0, 0, -40, 116, 0, 0, -44)
                .rot("mouth_r", 0, 0, 0, 0, 30, 0, 0, 0, 40, 0, 0, -20, 70, 0, 0, -20, 90, 0, 0, 40, 116, 0, 0, 44)
                .rot("hair_lock_l", 0, 0, 0, 0, 96, 0, 0, -10, 116, 0, 0, -25)
                .rot("hair_lock_r", 0, 0, 0, 0, 96, 0, 0, 10, 116, 0, 0, 25));
        // MAT-CIN-06, the final vanishing (28): the rest of her comes apart, the hair and the hood, the head, the eyes; the smile last;
        // then nothing (the entity is removed only after this ends)
        add.accept(clip(MatAnim.CIN_06)
                .fx(MatRig.FX_DISSOLVE, Ease.LINEAR, 0, 0.8F, 0, 0, 6, 0.88F, 0, 0, 16, 0.95F, 0, 0, 26, 1, 0, 0, 28, 1, 0, 0)
                .rot("head", 0, 22, 0, 16, 28, 28, 0, 20)
                .rot("chest", 0, 26, 0, 8, 28, 30, 0, 8)
                .pos("body", 0, 0, -3, 0, 28, 0, -3.5F, 0)
                .rot("mouth_l", 0, 0, 0, -44, 28, 0, 0, -46)
                .rot("mouth_r", 0, 0, 0, 44, 28, 0, 0, 46)
                .scale("mouth", 0, 1.8F, 1.1F, 1, 28, 1.85F, 1.1F, 1)
                .scale("eye_l", 0, 1, 0.5F, 1, 28, 1, 0.4F, 1)
                .scale("eye_r", 0, 1, 0.5F, 1, 28, 1, 0.4F, 1)
                .rot("hair_lock_l", 0, 0, 0, -25, 28, 0, 0, -40)
                .rot("hair_lock_r", 0, 0, 0, 25, 28, 0, 0, 40)
                .fx(MatRig.FX_THREAD_L, 0, 0, 0, 0, 28, 0, 0, 0)
                .fx(MatRig.FX_THREAD_R, 0, 0, 0, 0, 28, 0, 0, 0));
    }
}
