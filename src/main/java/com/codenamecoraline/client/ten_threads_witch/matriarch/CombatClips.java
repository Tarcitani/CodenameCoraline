package com.codenamecoraline.client.ten_threads_witch.matriarch;

import static com.codenamecoraline.client.ten_threads_witch.matriarch.ClipBuilder.clip;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatAnim;
import java.util.function.Consumer;

/**
 * 6.5 Preparations and body attacks (MAT-ATK-01 to 13) and 6.7 defences and dodges (MAT-DEF-01 to 09). Preparations hold
 * their last frame until the blow begins (the wind-up of the attack sets their length); each execution carries its own
 * recovery tail. One-hand attacks only move the left arm, so the right hand may keep a thread. Dodges are authored to her
 * left and mirrored for the other side.
 */
final class CombatClips {
    private CombatClips() {}

    static void register(Consumer<ClipBuilder> add) {
        // MAT-ATK-01: the discreet preparation: a small coil of the chest, the left hand drawn to the breast, the fingers clawing
        add.accept(clip(MatAnim.ATK_01)
                .rot("chest", 0, 0, 0, 0, 6, 2, 8, 0, 8, 2, 8, 0)
                .rot("arm_l", 0, 0, 0, 0, 6, -40, 0, -5, 8, -40, 0, -5)
                .rot("forearm_l", 0, 0, 0, 0, 6, -70, 0, 0, 8, -70, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 6, 20, 0, 0, 8, 20, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 6, 50, 50, 50, 50, 30, 0, 8, 50, 50, 50, 50, 30, 0)
                .rot("head", 0, 0, 0, 0, 6, 4, 0, 0, 8, 4, 0, 0)
                .pos("body", 0, 0, 0, 0, 6, 0, -0.3F, 0, 8, 0, -0.3F, 0));
        // MAT-ATK-02: the heavy preparation: the staff raised over the head with both hands, back arched, stance widened
        add.accept(clip(MatAnim.ATK_02)
                .rot("arm_r", 0, 0, 0, 0, 14, -150, 0, 10, 22, -165, 0, 12)
                .rot("forearm_r", 0, -10, 0, 0, 22, -20, 0, 0)
                .rot("arm_l", 0, 0, 0, 0, 14, -130, 0, -30, 22, -140, 0, -25)
                .rot("forearm_l", 0, 0, 0, 0, 14, -40, 0, 0, 22, -40, 0, 0)
                .rot("chest", 0, 0, 0, 0, 14, -14, 0, 0, 22, -18, 0, 0)
                .pos("body", 0, 0, 0, 0, 14, 0, -0.4F, 0, 22, 0, -0.8F, 0)
                .rot("leg_l", 0, 0, 0, 0, 22, -10, 0, -4)
                .rot("leg_r", 0, 0, 0, 0, 22, 8, 0, 4)
                .rot("head", 0, 0, 0, 0, 22, -10, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 14, 45, 45, 45, 45, 30, 0, 22, 45, 45, 45, 45, 30, 0));
        // MAT-ATK-03: the special preparation: she rises onto her toes, arms flung wide and up, palms up, the threads gathering
        add.accept(clip(MatAnim.ATK_03)
                .rot("arm_l", 0, 0, 0, 0, 10, -40, 0, -60, 20, -90, 0, -80, 30, -100, 0, -85)
                .rot("arm_r", 0, 0, 0, 0, 10, -30, 0, 50, 20, -80, 0, 75, 30, -90, 0, 80)
                .rot("forearm_l", 0, 0, 0, 0, 20, -20, 0, 0, 30, -20, 0, 0)
                .rot("forearm_r", 0, 0, 0, 0, 20, -20, 0, 0, 30, -20, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 20, 0, -90, 0, 30, 0, -90, 0)
                .rot("chest", 0, 0, 0, 0, 20, -16, 0, 0, 30, -16, 0, 0)
                .rot("head", 0, 0, 0, 0, 20, -18, 0, 0, 30, -25, 0, 0)
                .pos("body", 0, 0, 0, 0, 30, 0, 1.2F, 0)
                .rot("foot_l", 0, 0, 0, 0, 30, 40, 0, 0)
                .rot("foot_r", 0, 0, 0, 0, 30, 40, 0, 0)
                .fingers('l', Ease.SNAP, 0, 0, 0, 0, 0, 0, 0, 20, -20, -20, -20, -20, -10, 12, 22, 10, -20, 10, -20, -10, 12, 24, -20, 10, -20, 10, -10, 12, 26, -20, -20, -20, -20, -10, 12, 30, -20, -20, -20, -20, -10, 12)
                .fx(MatRig.FX_GLOW, 0, 0, 0, 0, 30, 1, 1, 0)
                .fx(MatRig.FX_ATTACK, 0, 0, 0, 0, 20, 0.2F, 0.6F, 0.4F, 30, 0.3F, 1, 0.8F));
        // MAT-ATK-04: the deceptive preparation: both arms rise halfway and the chest twists one way then the other, revealing nothing
        add.accept(clip(MatAnim.ATK_04)
                .rot("arm_l", 0, 0, 0, 0, 8, -60, 0, -30, 16, -55, 0, -20)
                .rot("arm_r", 0, 0, 0, 0, 8, -30, 0, 20, 16, -45, 0, 15)
                .rot("forearm_l", 0, 0, 0, 0, 8, -60, 0, 0, 16, -80, 0, 0)
                .rot("chest", 0, 0, 0, 0, 6, 0, 15, 0, 12, 0, -10, 0, 16, 0, 0, 0)
                .rot("head", 0, 0, 0, 0, 6, 0, 0, -6, 12, 0, 0, 6, 16, 0, 0, 0)
                .pos("body", 0, 0, 0, 0, 16, 0, -0.5F, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 8, 35, 35, 35, 35, 20, 0, 16, 35, 35, 35, 35, 20, 0));
        // MAT-ATK-05: calling off a preparation: the arms come down, a dismissive tilt of the head, back to stance
        add.accept(clip(MatAnim.ATK_05)
                .rot("arm_l", 0, -55, 0, -20, 12, 0, 0, 0)
                .rot("arm_r", 0, -40, 0, 15, 12, 0, 0, 0)
                .rot("forearm_l", 0, -60, 0, 0, 12, -10, 0, 0)
                .rot("forearm_r", 0, -30, 0, 0, 12, -10, 0, 0)
                .rot("chest", 0, 0, 8, 0, 6, -4, 0, 0, 12, 0, 0, 0)
                .rot("head", 0, 0, 0, 0, 4, -6, 0, 8, 12, 0, 0, 0)
                .fingers('l', 0, 35, 35, 35, 35, 20, 0, 12, 0, 0, 0, 0, 0, 0));
        // MAT-ATK-06: the quick attack: a short raking swipe of the clawed hand across her front, and back
        add.accept(clip(MatAnim.ATK_06)
                .rot("arm_l", Ease.OUT, 0, -50, 0, -10, 2, -80, 0, 25, 5, -60, 0, 35, 14, -10, 0, 0)
                .rot("forearm_l", Ease.OUT, 0, -70, 0, 0, 2, -30, 0, 0, 14, -15, 0, 0)
                .rot("hand_l", 0, 20, 0, 0, 2, 0, 40, -20, 14, 0, 0, 0)
                .fingers('l', 0, 60, 60, 60, 60, 40, -4, 5, 30, 30, 30, 30, 20, 0, 14, 0, 0, 0, 0, 0, 0)
                .rot("chest", Ease.OUT, 0, 2, 8, 0, 2, 2, -14, 0, 14, 0, 0, 0)
                .pos("body", Ease.OUT, 0, 0, -0.3F, 0, 2, 0, -0.3F, -0.5F, 14, 0, 0, 0));
        // MAT-ATK-07: the heavy attack: the staff brought down overhead with the whole body folding behind it, a slam, a slow rise
        add.accept(clip(MatAnim.ATK_07)
                .rot("arm_r", Ease.IN, 0, -165, 0, 12, 3, -40, 0, 4, 6, -25, 0, 4, 16, -30, 0, 6, 26, 0, 0, 0)
                .rot("forearm_r", Ease.IN, 0, -20, 0, 0, 3, -5, 0, 0, 26, -10, 0, 0)
                .rot("arm_l", Ease.IN, 0, -140, 0, -25, 3, -45, 0, 10, 16, -30, 0, 0, 26, 0, 0, 0)
                .rot("forearm_l", 0, -40, 0, 0, 3, -20, 0, 0, 26, 0, 0, 0)
                .rot("chest", Ease.IN, 0, -18, 0, 0, 3, 24, 0, 0, 6, 26, 0, 0, 18, 10, 0, 0, 26, 0, 0, 0)
                .rot("body", Ease.IN, 0, 0, 0, 0, 3, 10, 0, 0, 18, 4, 0, 0, 26, 0, 0, 0)
                .pos("body", Ease.IN, 0, 0, -0.8F, 0, 3, 0, -1.2F, 0, 10, 0, -1, 0, 26, 0, 0, 0)
                .rot("head", 0, -10, 0, 0, 3, 14, 0, 0, 26, 0, 0, 0)
                .rot("leg_l", 0, -10, 0, -4, 3, -16, 0, -4, 26, 0, 0, 0)
                .rot("leg_r", 0, 8, 0, 4, 3, 10, 0, 4, 26, 0, 0, 0)
                .fingers('l', 0, 45, 45, 45, 45, 30, 0, 26, 0, 0, 0, 0, 0, 0)
                .fx(MatRig.FX_GLOW, 0, 0, 0, 0, 3, 0, 0, 0.85F, 16, 0, 0, 0.5F, 26, 0, 0, 0)); // the staff swings with the blow
        // MAT-ATK-08: the one-hand attack: the left hand rises and rips down across her body; the right keeps the staff (and a thread)
        add.accept(clip(MatAnim.ATK_08)
                .rot("arm_l", Ease.BACK, 0, -40, 0, -5, 3, -130, 0, -40, 6, -20, 0, 30, 10, 0, 0, 25, 18, 0, 0, 0)
                .rot("forearm_l", 0, -70, 0, 0, 3, -30, 0, 0, 6, -10, 0, 0, 18, -15, 0, 0)
                .rot("hand_l", 0, 20, 0, 0, 6, 30, 0, -20, 18, 0, 0, 0)
                .fingers('l', Ease.OUT, 0, 50, 50, 50, 50, 30, 0, 3, -10, -10, -10, -10, -5, 10, 6, 60, 60, 60, 60, 40, 0, 18, 0, 0, 0, 0, 0, 0)
                .rot("chest", Ease.BACK, 0, 2, 8, 0, 3, -6, -14, 0, 6, 12, 16, 0, 18, 0, 0, 0)
                .pos("body", 0, 0, -0.3F, 0, 6, 0, 0, -0.4F, 18, 0, 0, 0));
        // MAT-ATK-09: the two-hand attack: both arms cross and sweep outward, twice (the hem cleave)
        add.accept(clip(MatAnim.ATK_09)
                .rot("arm_l", Ease.OUT, 0, -55, 0, -20, 2, -80, 0, 20, 6, -70, 0, -70, 10, -75, 0, 15, 14, -80, 0, -75, 26, 0, 0, 0)
                .rot("arm_r", Ease.OUT, 0, -45, 0, 15, 2, -75, 0, -20, 6, -65, 0, 65, 10, -70, 0, -15, 14, -80, 0, 70, 26, 0, 0, 0)
                .rot("forearm_l", 0, -80, 0, 0, 2, -40, 0, 0, 14, -30, 0, 0, 26, 0, 0, 0)
                .rot("forearm_r", 0, -30, 0, 0, 2, -40, 0, 0, 14, -30, 0, 0, 26, 0, 0, 0)
                .rot("chest", 0, 0, 0, 0, 6, 10, 0, 0, 10, 6, 0, 0, 14, 12, 0, 0, 26, 0, 0, 0)
                .pos("body", 0, 0, -0.5F, 0, 6, 0, -0.6F, 0, 14, 0, -0.8F, 0, 26, 0, 0, 0)
                .rot("head", 0, 0, 0, 0, 6, 6, 0, 0, 26, 0, 0, 0)
                .fingers('l', 0, 35, 35, 35, 35, 20, 0, 6, -15, -15, -15, -15, -10, 10, 10, 40, 40, 40, 40, 25, 0, 14, -15, -15, -15, -15, -10, 10, 26, 0, 0, 0, 0, 0, 0));
        // MAT-ATK-10: the supernatural attack: the spine bends the wrong way, the arms swing backwards, then she folds onto the target
        add.accept(clip(MatAnim.ATK_10)
                .rot("chest", Ease.SNAP, 0, -18, 0, 0, 4, -50, 30, 20, 8, 40, -20, -10, 14, 35, 0, 0, 24, 0, 0, 0)
                .rot("head", Ease.SNAP, 0, -10, 0, 0, 4, 20, 0, 70, 8, -20, 0, -40, 14, -10, 0, -10, 24, 0, 0, 0)
                .rot("arm_l", Ease.SNAP, 0, -140, 0, -25, 4, 60, 0, -80, 8, -110, 0, 40, 14, -90, 0, 30, 24, 0, 0, 0)
                .rot("arm_r", Ease.SNAP, 0, -165, 0, 12, 4, 50, 0, 80, 8, -100, 0, -40, 14, -80, 0, -30, 24, 0, 0, 0)
                .rot("forearm_l", 0, -40, 0, 0, 8, -20, 0, 0, 24, 0, 0, 0)
                .rot("body", Ease.SNAP, 0, 0, 0, 0, 4, -10, 0, 0, 8, 14, 0, 0, 24, 0, 0, 0)
                .pos("body", Ease.OUT, 0, 0, -0.8F, 0, 4, 0, 1, 0, 8, 0, -0.5F, 0, 24, 0, 0, 0)
                .fingers('l', Ease.SNAP, 0, 45, 45, 45, 45, 30, 0, 4, -25, -25, -25, -25, -15, 14, 8, 70, 70, 70, 70, 50, 0, 24, 0, 0, 0, 0, 0, 0));
        // MAT-ATK-11: the long-range attack: a wide side-arm throw with a step, needles sent along the line of the arm
        add.accept(clip(MatAnim.ATK_11)
                .rot("arm_l", Ease.OUT, 0, -20, 0, -15, 2, -100, 0, -60, 4, -80, 0, 20, 8, -60, 0, 30, 20, 0, 0, 0)
                .rot("forearm_l", Ease.OUT, 0, -95, 0, 0, 2, -60, 0, 0, 4, -10, 0, 0, 20, -15, 0, 0)
                .rot("hand_l", 0, -20, 0, 0, 4, 0, 0, -30, 20, 0, 0, 0)
                .fingers('l', Ease.OUT, 0, 55, 30, 60, 65, 40, 0, 2, 55, 55, 55, 55, 40, 0, 4, -20, -20, -20, -20, -10, 12, 20, 0, 0, 0, 0, 0, 0)
                .rot("chest", Ease.OUT, 0, 0, 8, 0, 2, 0, 25, 0, 4, 4, -25, 0, 20, 0, 0, 0)
                .rot("leg_l", 0, 0, 0, 0, 4, -12, 0, 0, 20, 0, 0, 0)
                .pos("body", 0, 0, 0, 0, 4, 0, 0, -0.5F, 20, 0, 0, 0)
                .fx(MatRig.FX_ATTACK, 0, 0.15F, 0, 0, 2, 0.2F, 0.6F, 0, 4, 1, 0.6F, 0, 10, 1, 0.6F, 0, 20, 0, 0, 0));
        // MAT-ATK-12: the special attack (the unravelled storm): she rises, arms wide, spins twice above the floor while the spools
        // burst around her, twitching; then drops, folded and spent
        add.accept(clip(MatAnim.ATK_12)
                .rot("body", Ease.LINEAR, 0, 0, 0, 0, 30, 0, 360, 0, 60, 0, 720, 0, 66, 0, 720, 0, 80, 0, 720, 0)
                .pos("body", 0, 0, 1.2F, 0, 10, 0, 2.5F, 0, 50, 0, 2.5F, 0, 62, 0, 0.4F, 0, 80, 0, 0, 0)
                .rot("arm_l", 0, -100, 0, -85, 4, -95, 0, -85, 58, -95, 0, -85, 66, -20, 0, -10, 80, 0, 0, 0)
                .rot("arm_r", 0, -90, 0, 80, 4, -95, 0, 85, 58, -95, 0, 85, 66, -20, 0, 10, 80, 0, 0, 0)
                .rot("forearm_l", 0, -20, 0, 0, 4, -10, 0, 0, 58, -10, 0, 0, 80, 0, 0, 0)
                .rot("forearm_r", 0, -20, 0, 0, 4, -10, 0, 0, 58, -10, 0, 0, 80, 0, 0, 0)
                .rot("chest", 0, -16, 0, 0, 10, -10, 0, 0, 58, -10, 0, 0, 66, 20, 0, 0, 80, 0, 0, 0)
                .rot("head", Ease.SNAP, 0, -25, 0, 0, 18, -20, 0, 0, 20, -20, 0, 25, 30, -20, 0, 25, 40, -20, 0, -25, 50, -20, 0, -25, 58, -20, 0, 0, 66, 25, 0, 0, 80, 0, 0, 0)
                .rot("leg_l", 0, 0, 0, 0, 10, 15, 0, 0, 58, 15, 0, 0, 66, 0, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 10, -5, 0, 0, 58, -5, 0, 0, 66, 0, 0, 0)
                .rot("foot_l", 0, 40, 0, 0, 58, 45, 0, 0, 66, 0, 0, 0)
                .rot("foot_r", 0, 40, 0, 0, 58, 45, 0, 0, 66, 0, 0, 0)
                .fingers('l', Ease.SNAP, 0, -20, -20, -20, -20, -10, 12, 30, -20, -20, -20, -20, -10, 12, 31, 40, -20, 40, -20, 0, 12, 34, -20, -20, -20, -20, -10, 12, 66, 30, 30, 30, 30, 20, 0, 80, 0, 0, 0, 0, 0, 0)
                .fx(MatRig.FX_GLOW, 0, 1, 1, 0, 60, 1, 1, 0, 80, 0, 0, 0)
                .fx(MatRig.FX_ATTACK, 0, 0.3F, 1, 0.8F, 4, 1, 1, 1, 58, 1, 1, 1, 70, 0, 0, 0));
        // MAT-ATK-13: the combo finisher: a spinning backhand sweep of the staff and a stomp, then a recovery of its own
        add.accept(clip(MatAnim.ATK_13)
                .rot("chest", Ease.OUT, 0, 2, 20, 0, 4, 0, -60, 0, 8, 6, -70, 0, 20, 0, -10, 0, 28, 0, 0, 0)
                .rot("body", Ease.OUT, 0, 0, 10, 0, 4, 0, -30, 0, 28, 0, 0, 0)
                .rot("arm_r", Ease.OUT, 0, -60, 0, 40, 4, -80, 0, -30, 6, -70, 0, -40, 28, 0, 0, 0)
                .rot("forearm_r", 0, -20, 0, 0, 28, 0, 0, 0)
                .rot("arm_l", 0, -40, 0, -5, 4, -30, 0, -60, 28, 0, 0, 0)
                .rot("leg_l", Ease.IN, 0, 0, 0, 0, 4, -25, 0, 0, 6, -2, 0, 0, 28, 0, 0, 0)
                .pos("body", Ease.IN, 0, 0, -0.3F, 0, 6, 0, -1, 0, 14, 0, -0.8F, 0, 28, 0, 0, 0)
                .rot("head", 0, 0, 0, 0, 4, 0, 30, 0, 28, 0, 0, 0)
                .fx(MatRig.FX_GLOW, 0, 0, 0, 0, 4, 0, 0, 0.8F, 10, 0, 0, 0.8F, 28, 0, 0, 0));

        // MAT-DEF-01: defence with the arms: forearms crossed before the face, a hunch, pushed back a step
        add.accept(clip(MatAnim.DEF_01)
                .rot("arm_l", Ease.OUT, 0, 0, 0, 0, 3, -80, 0, 25, 12, -80, 0, 25, 18, -40, 0, 10)
                .rot("forearm_l", Ease.OUT, 0, 0, 0, 0, 3, -80, 0, 0, 12, -80, 0, 0, 18, -40, 0, 0)
                .rot("arm_r", Ease.OUT, 0, 0, 0, 0, 3, -70, 0, -20, 12, -70, 0, -20, 18, -30, 0, -8)
                .rot("forearm_r", Ease.OUT, 0, 0, 0, 0, 3, -70, 0, 0, 12, -70, 0, 0, 18, -30, 0, 0)
                .rot("chest", 0, 0, 0, 0, 3, 10, 0, 0, 18, 4, 0, 0)
                .rot("head", 0, 0, 0, 0, 3, 14, 0, 0, 18, 4, 0, 0)
                .pos("body", Ease.OUT, 0, 0, 0, 0, 3, 0, -0.5F, 0, 4, 0, -0.5F, 0.6F, 18, 0, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 4, 8, 0, 0, 18, 0, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 3, 60, 60, 60, 60, 45, 0, 18, 30, 30, 30, 30, 20, 0));
        // MAT-DEF-02: a block with the hand: the open palm snaps up to meet the blow, chin raised in disdain
        add.accept(clip(MatAnim.DEF_02)
                .rot("arm_l", Ease.OUT, 0, 0, 0, 0, 2, -95, 0, -10, 6, -90, 0, -12, 12, -50, 0, -8)
                .rot("forearm_l", Ease.OUT, 0, 0, 0, 0, 2, -40, 0, 0, 12, -40, 0, 0)
                .rot("hand_l", Ease.OUT, 0, 0, 0, 0, 2, -60, 0, 0, 6, -60, 0, 0, 12, -20, 0, 0)
                .fingers('l', Ease.OUT, 0, 0, 0, 0, 0, 0, 0, 2, -20, -20, -20, -20, -10, 10, 12, 0, 0, 0, 0, 0, 2)
                .rot("chest", 0, 0, 0, 0, 2, 0, -10, 0, 12, 0, -4, 0)
                .rot("head", 0, 0, 0, 0, 2, -4, 0, 0, 12, 0, 0, 0));
        // MAT-DEF-03: the barrier of threads: both hands up and apart, fingers splayed, a web stretched before her
        add.accept(clip(MatAnim.DEF_03)
                .rot("arm_l", Ease.OUT, 0, 0, 0, 0, 4, -90, 0, -40, 16, -90, 0, -40, 22, -40, 0, -10)
                .rot("arm_r", Ease.OUT, 0, 0, 0, 0, 4, -85, 0, 40, 16, -85, 0, 40, 22, -35, 0, 10)
                .rot("forearm_l", 0, 0, 0, 0, 4, -30, 0, 0, 22, -20, 0, 0)
                .rot("forearm_r", 0, 0, 0, 0, 4, -30, 0, 0, 22, -20, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 4, -30, 90, 0, 22, 0, 30, 0)
                .fingers('l', Ease.OUT, 0, 0, 0, 0, 0, 0, 0, 6, -25, -25, -25, -25, -15, 14, 16, -25, -25, -25, -25, -15, 14, 22, 0, 0, 0, 0, 0, 0)
                .fingers('r', Ease.OUT, 0, 0, 0, 0, 0, 0, 0, 6, -40, -40, -30, -30, -15, 10, 16, -40, -40, -30, -30, -15, 10, 22, 0, 0, 0, 0, 0, 0)
                .rot("chest", 0, 0, 0, 0, 4, -4, 0, 0, 22, 0, 0, 0)
                .fx(MatRig.FX_ATTACK, 0, 0, 0, 0, 3, 0, 0, 0, 6, 0.35F, 1, 0, 16, 0.35F, 1, 0, 22, 0, 0, 0));
        // MAT-DEF-04: defence with a puppet: she reaches for it and drags it across, between herself and the blow
        add.accept(clip(MatAnim.DEF_04)
                .rot("arm_l", 0, 0, 0, 0, 6, -70, 0, -30, 10, -60, 0, 20, 18, -50, 0, 20, 26, -30, 0, -8)
                .rot("forearm_l", Ease.IN, 0, 0, 0, 0, 6, -20, 0, 0, 10, -90, 0, 0, 26, -55, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 10, 20, 90, 0, 26, 0, 90, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 6, -15, -15, -15, -15, -10, 8, 10, 55, 55, 55, 55, 40, 0, 26, 20, 25, 25, 30, 15, 2)
                .rot("chest", 0, 0, 0, 0, 6, 0, -20, 0, 10, 0, 15, 0, 26, 0, 0, 0)
                .pos("body", 0, 0, 0, 0, 10, 0, 0, 0.3F, 26, 0, 0, 0)
                .fx(MatRig.FX_THREAD_L, 0, 1, 0.6F, 0, 6, 1, 1, 0, 10, 1, 1, 0, 26, 1, 0.6F, 0));
        // MAT-DEF-05: the side dodge (to her left; mirrored to the right): a lean and a step out, the far arm swinging for balance
        add.accept(clip(MatAnim.DEF_05)
                .rot("body", Ease.OUT, 0, 0, 0, 0, 3, 0, 0, 16, 8, 0, 0, 10, 14, 0, 0, 0)
                .rot("leg_l", Ease.OUT, 0, 0, 0, 0, 3, 0, 0, -20, 8, 0, 0, -8, 14, 0, 0, 0)
                .rot("leg_r", Ease.OUT, 0, 0, 0, 0, 3, 0, 0, -10, 14, 0, 0, 0)
                .pos("body", Ease.OUT, 0, 0, 0, 0, 3, 0, -0.6F, 0, 14, 0, 0, 0)
                .rot("chest", 0, 0, 0, 0, 3, 0, 0, -6, 14, 0, 0, 0)
                .rot("arm_r", Ease.OUT, 0, 0, 0, 0, 3, 0, 0, 25, 14, 0, 0, 0)
                .rot("arm_l", Ease.OUT, 0, 0, 0, 0, 3, 0, 0, -10, 14, 0, 0, 0));
        // MAT-DEF-06: the dodge backwards: a little hop back with the torso leaning away, eyes kept on the attacker
        add.accept(clip(MatAnim.DEF_06)
                .rot("body", Ease.OUT, 0, 0, 0, 0, 3, -14, 0, 0, 10, -6, 0, 0, 16, 0, 0, 0)
                .pos("body", Ease.OUT, 0, 0, 0, 0, 3, 0, 0.8F, 0, 7, 0, 0, 0, 10, 0, -0.5F, 0, 16, 0, 0, 0)
                .rot("leg_l", 0, 0, 0, 0, 3, 20, 0, 0, 8, -6, 0, 0, 16, 0, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 3, 10, 0, 0, 8, -4, 0, 0, 16, 0, 0, 0)
                .rot("arm_l", 0, 0, 0, 0, 3, -40, 0, -30, 16, 0, 0, 0)
                .rot("arm_r", 0, 0, 0, 0, 3, -30, 0, 25, 16, 0, 0, 0)
                .rot("head", 0, 0, 0, 0, 3, 8, 0, 0, 16, 0, 0, 0));
        // MAT-DEF-07: the supernatural dodge: she snaps over sideways further than a body can, the head staying level, and snaps back
        add.accept(clip(MatAnim.DEF_07)
                .rot("body", Ease.SNAP, 0, 0, 0, 0, 2, 0, 0, 60, 10, 0, 0, 60, 13, 0, 0, -10, 18, 0, 0, 0)
                .rot("chest", Ease.SNAP, 0, 0, 0, 0, 2, 0, 0, 30, 10, 0, 0, 30, 13, 0, 0, 0)
                .rot("head", Ease.SNAP, 0, 0, 0, 0, 2, 0, 0, -70, 10, 0, 0, -70, 13, 0, 0, 10, 18, 0, 0, 0)
                .pos("body", Ease.SNAP, 0, 0, 0, 0, 2, 0, -1.5F, 0, 10, 0, -1.5F, 0, 13, 0, 0, 0)
                .rot("leg_l", Ease.SNAP, 0, 0, 0, 0, 2, 0, 0, -40, 10, 0, 0, -40, 13, 0, 0, 0)
                .rot("arm_l", Ease.SNAP, 0, 0, 0, 0, 2, 0, 0, -90, 10, 0, 0, -90, 13, 0, 0, 0)
                .rot("arm_r", Ease.SNAP, 0, 0, 0, 0, 2, 0, 0, 70, 10, 0, 0, 70, 13, 0, 0, 0));
        // MAT-DEF-08: the aggressive dodge: back, then a lurch forward into a low lunge with the claw drawn back to strike
        add.accept(clip(MatAnim.DEF_08)
                .rot("body", Ease.OUT, 0, 0, 0, 0, 3, -16, 0, 0, 8, 18, 0, 0, 18, 12, 0, 0)
                .pos("body", Ease.OUT, 0, 0, 0, 0, 3, 0, 0.8F, 0, 8, 0, -1, 0, 18, 0, -0.8F, 0)
                .rot("leg_l", 0, 0, 0, 0, 3, 20, 0, 0, 8, -18, 0, 0, 18, -16, 0, 0)
                .rot("leg_r", 0, 0, 0, 0, 3, 10, 0, 0, 8, 14, 0, 0, 18, 12, 0, 0)
                .rot("arm_l", 0, 0, 0, 0, 3, -30, 0, -20, 8, 40, 0, -20, 18, 40, 0, -20)
                .rot("forearm_l", 0, 0, 0, 0, 8, -70, 0, 0, 18, -70, 0, 0)
                .fingers('l', 0, 0, 0, 0, 0, 0, 0, 8, 60, 60, 60, 60, 40, -4, 18, 60, 60, 60, 60, 40, -4)
                .rot("head", 0, 0, 0, 0, 8, -12, 0, 0, 18, -12, 0, 0));
        // MAT-DEF-09: defensive recovery: from any guard or dodge back to her stance, composing herself
        add.accept(clip(MatAnim.DEF_09)
                .rot("arm_l", 0, -50, 0, 0, 14, 0, 0, 0)
                .rot("arm_r", 0, -30, 0, 0, 14, 0, 0, 0)
                .rot("forearm_l", 0, -40, 0, 0, 14, 0, 0, 0)
                .rot("chest", 0, 6, 0, 0, 6, -3, 0, 0, 14, 0, 0, 0)
                .rot("head", 0, 0, 0, 0, 4, -4, 0, 4, 14, 0, 0, 0)
                .rot("hand_l", 0, 0, 0, 0, 6, 0, 0, -10, 14, 0, 0, 0)
                .pos("body", 0, 0, -0.4F, 0, 14, 0, 0, 0)
                .fingers('l', 0, 30, 30, 30, 30, 20, 0, 14, 0, 0, 0, 0, 0, 0));
    }
}
