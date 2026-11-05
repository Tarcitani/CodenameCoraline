package com.codenamecoraline.client.ten_threads_witch.matriarch;

import static com.codenamecoraline.client.ten_threads_witch.matriarch.ClipBuilder.clip;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatAnim;
import java.util.function.Consumer;

/**
 * 6.11 Expressions (MAT-EXP-01 to 10), on the face parts the rig has: the two brows (height and tilt: the outer end up is
 * anger, the inner end up is worry), the mouth (width, opening, and its two corners: up is a smile) and the eyes (their size
 * and, through the eye effect channel, their glow). The eyes are behind the lace blindfold until the last phase; while it is
 * on, the brows and the mouth carry the expression. Each expression is a loop with its own small life (a twitch, a creeping
 * smile) so the face never freezes.
 */
final class FaceClips {
    private FaceClips() {}

    static void register(Consumer<ClipBuilder> add) {
        // MAT-EXP-01: the elegant smile: both corners gently up, the mouth a little wider, soft eyes
        add.accept(clip(MatAnim.EXP_01)
                .rot("mouth_l", Ease.CATMULL, 0, 0, 0, -18, 30, 0, 0, -21, 60, 0, 0, -18)
                .rot("mouth_r", Ease.CATMULL, 0, 0, 0, 18, 30, 0, 0, 21, 60, 0, 0, 18)
                .scale("mouth", Ease.CATMULL, 0, 1.1F, 0.9F, 1, 60, 1.1F, 0.9F, 1)
                .pos("brow_l", Ease.CATMULL, 0, 0, 0.1F, 0, 60, 0, 0.1F, 0)
                .pos("brow_r", Ease.CATMULL, 0, 0, 0.1F, 0, 60, 0, 0.1F, 0)
                .scale("eye_l", Ease.CATMULL, 0, 1, 0.85F, 1, 60, 1, 0.85F, 1)
                .scale("eye_r", Ease.CATMULL, 0, 1, 0.85F, 1, 60, 1, 0.85F, 1));
        // MAT-EXP-02: the arrogant smile: a lopsided smirk, one brow raised, heavy lids
        add.accept(clip(MatAnim.EXP_02)
                .rot("mouth_l", Ease.CATMULL, 0, 0, 0, -28, 60, 0, 0, -28)
                .rot("mouth_r", Ease.CATMULL, 0, 0, 0, 6, 60, 0, 0, 6)
                .pos("mouth", Ease.CATMULL, 0, 0.15F, 0, 0, 60, 0.15F, 0, 0)
                .pos("brow_l", Ease.CATMULL, 0, 0, 0.25F, 0, 36, 0, 0.25F, 0, 40, 0, 0.4F, 0, 46, 0, 0.25F, 0, 60, 0, 0.25F, 0)
                .rot("brow_r", Ease.CATMULL, 0, 0, 0, 6, 60, 0, 0, 6)
                .scale("eye_l", Ease.CATMULL, 0, 1, 0.8F, 1, 60, 1, 0.8F, 1)
                .scale("eye_r", Ease.CATMULL, 0, 1, 0.8F, 1, 60, 1, 0.8F, 1));
        // MAT-EXP-03: contempt: both brows raised (looking down her nose), the corners down, the mouth narrowed, narrow eyes
        add.accept(clip(MatAnim.EXP_03)
                .rot("mouth_l", Ease.CATMULL, 0, 0, 0, 18, 48, 0, 0, 18)
                .rot("mouth_r", Ease.CATMULL, 0, 0, 0, -18, 48, 0, 0, -18)
                .scale("mouth", Ease.CATMULL, 0, 0.8F, 1, 1, 48, 0.8F, 1, 1)
                .pos("brow_l", Ease.CATMULL, 0, 0, 0.2F, 0, 48, 0, 0.2F, 0)
                .pos("brow_r", Ease.CATMULL, 0, 0, 0.2F, 0, 48, 0, 0.2F, 0)
                .rot("brow_l", Ease.CATMULL, 0, 0, 0, -6, 48, 0, 0, -6)
                .rot("brow_r", Ease.CATMULL, 0, 0, 0, 6, 48, 0, 0, 6)
                .scale("eye_l", Ease.CATMULL, 0, 1, 0.6F, 1, 48, 1, 0.6F, 1)
                .scale("eye_r", Ease.CATMULL, 0, 1, 0.6F, 1, 48, 1, 0.6F, 1));
        // MAT-EXP-04: curiosity: brows up and uneven (the inner ends lifted), a small round mouth, wide eyes
        add.accept(clip(MatAnim.EXP_04)
                .pos("brow_l", Ease.CATMULL, 0, 0, 0.4F, 0, 56, 0, 0.4F, 0)
                .rot("brow_l", Ease.CATMULL, 0, 0, 0, 8, 56, 0, 0, 8)
                .pos("brow_r", Ease.CATMULL, 0, 0, 0.2F, 0, 24, 0, 0.2F, 0, 28, 0, 0.45F, 0, 36, 0, 0.2F, 0, 56, 0, 0.2F, 0)
                .scale("mouth", Ease.CATMULL, 0, 0.6F, 1.6F, 1, 56, 0.6F, 1.6F, 1)
                .scale("eye_l", Ease.CATMULL, 0, 1.25F, 1.25F, 1, 56, 1.25F, 1.25F, 1)
                .scale("eye_r", Ease.CATMULL, 0, 1.25F, 1.25F, 1, 56, 1.25F, 1.25F, 1));
        // MAT-EXP-05: contained irritation: brows drawn down at the inner ends, lips pressed, a twitch of one brow
        add.accept(clip(MatAnim.EXP_05)
                .rot("brow_l", Ease.STEP, 0, 0, 0, -12, 24, 0, 0, -12, 25, 0, 0, -18, 27, 0, 0, -12, 50, 0, 0, -12)
                .rot("brow_r", Ease.CATMULL, 0, 0, 0, 12, 50, 0, 0, 12)
                .pos("brow_l", Ease.CATMULL, 0, 0, -0.15F, 0, 50, 0, -0.15F, 0)
                .pos("brow_r", Ease.CATMULL, 0, 0, -0.15F, 0, 50, 0, -0.15F, 0)
                .scale("mouth", Ease.CATMULL, 0, 0.9F, 0.6F, 1, 50, 0.9F, 0.6F, 1)
                .rot("mouth_l", Ease.CATMULL, 0, 0, 0, 8, 50, 0, 0, 8)
                .rot("mouth_r", Ease.CATMULL, 0, 0, 0, -8, 50, 0, 0, -8)
                .scale("eye_l", Ease.CATMULL, 0, 1, 0.75F, 1, 50, 1, 0.75F, 1)
                .scale("eye_r", Ease.CATMULL, 0, 1, 0.75F, 1, 50, 1, 0.75F, 1));
        // MAT-EXP-06: growing irritation: harder brows, the corners pulled down, teeth set behind a half-open mouth, flickers
        add.accept(clip(MatAnim.EXP_06)
                .rot("brow_l", Ease.STEP, 0, 0, 0, -20, 12, 0, 0, -20, 13, 0, 0, -26, 15, 0, 0, -20, 30, 0, 0, -20, 31, 0, 0, -26, 33, 0, 0, -20, 40, 0, 0, -20)
                .rot("brow_r", Ease.STEP, 0, 0, 0, 20, 20, 0, 0, 20, 21, 0, 0, 26, 23, 0, 0, 20, 40, 0, 0, 20)
                .pos("brow_l", Ease.CATMULL, 0, 0, -0.25F, 0, 40, 0, -0.25F, 0)
                .pos("brow_r", Ease.CATMULL, 0, 0, -0.25F, 0, 40, 0, -0.25F, 0)
                .rot("mouth_l", Ease.CATMULL, 0, 0, 0, 16, 40, 0, 0, 16)
                .rot("mouth_r", Ease.CATMULL, 0, 0, 0, -16, 40, 0, 0, -16)
                .scale("mouth", Ease.CATMULL, 0, 1.1F, 1.3F, 1, 20, 1.1F, 1.4F, 1, 40, 1.1F, 1.3F, 1)
                .scale("eye_l", Ease.CATMULL, 0, 1, 0.65F, 1, 40, 1, 0.65F, 1)
                .scale("eye_r", Ease.CATMULL, 0, 1, 0.65F, 1, 40, 1, 0.65F, 1));
        // MAT-EXP-07: fury: brows slammed down, a wide open snarl, eyes wide and burning, everything trembling
        add.accept(clip(MatAnim.EXP_07)
                .rot("brow_l", Ease.LINEAR, 0, 0, 0, -28, 2, 0, 0, -30, 4, 0, 0, -27, 6, 0, 0, -29, 30, 0, 0, -28)
                .rot("brow_r", Ease.LINEAR, 0, 0, 0, 28, 3, 0, 0, 30, 5, 0, 0, 27, 30, 0, 0, 28)
                .pos("brow_l", Ease.CATMULL, 0, 0, -0.3F, 0, 30, 0, -0.3F, 0)
                .pos("brow_r", Ease.CATMULL, 0, 0, -0.3F, 0, 30, 0, -0.3F, 0)
                .scale("mouth", Ease.LINEAR, 0, 1.4F, 2.2F, 1, 2, 1.45F, 2.1F, 1, 4, 1.4F, 2.3F, 1, 30, 1.4F, 2.2F, 1)
                .pos("mouth", Ease.CATMULL, 0, 0, -0.15F, 0, 30, 0, -0.15F, 0)
                .rot("mouth_l", Ease.CATMULL, 0, 0, 0, 25, 30, 0, 0, 25)
                .rot("mouth_r", Ease.CATMULL, 0, 0, 0, -25, 30, 0, 0, -25)
                .scale("eye_l", Ease.CATMULL, 0, 1.3F, 1.3F, 1, 30, 1.3F, 1.3F, 1)
                .scale("eye_r", Ease.CATMULL, 0, 1.3F, 1.3F, 1, 30, 1.3F, 1.3F, 1)
                .fx(MatRig.FX_GLOW, 0, 0.5F, 0, 0, 30, 0.5F, 0, 0));
        // MAT-EXP-08: the disturbing face: a smile far too wide that keeps creeping wider, brows at odds, one eye larger
        add.accept(clip(MatAnim.EXP_08)
                .rot("mouth_l", Ease.SNAP, 0, 0, 0, -35, 30, 0, 0, -45, 32, 0, 0, -50, 62, 0, 0, -38, 64, 0, 0, -35)
                .rot("mouth_r", Ease.SNAP, 0, 0, 0, 35, 30, 0, 0, 45, 32, 0, 0, 50, 62, 0, 0, 38, 64, 0, 0, 35)
                .scale("mouth", Ease.CATMULL, 0, 1.9F, 1.1F, 1, 32, 2.0F, 1.1F, 1, 64, 1.9F, 1.1F, 1)
                .pos("brow_l", Ease.CATMULL, 0, 0, 0.45F, 0, 64, 0, 0.45F, 0)
                .rot("brow_l", Ease.CATMULL, 0, 0, 0, 15, 64, 0, 0, 15)
                .pos("brow_r", Ease.CATMULL, 0, 0, -0.2F, 0, 64, 0, -0.2F, 0)
                .rot("brow_r", Ease.CATMULL, 0, 0, 0, 18, 64, 0, 0, 18)
                .scale("eye_l", Ease.CATMULL, 0, 1.4F, 1.4F, 1, 64, 1.4F, 1.4F, 1)
                .scale("eye_r", Ease.CATMULL, 0, 0.7F, 0.7F, 1, 64, 0.7F, 0.7F, 1));
        // MAT-EXP-09: losing control: the face cannot hold one expression (worry, a clench, a smile, worry again), each one snapped in
        add.accept(clip(MatAnim.EXP_09)
                .rot("brow_l", Ease.STEP, 0, 0, 0, 15, 9, 0, 0, -25, 18, 0, 0, 5, 27, 0, 0, 18, 36, 0, 0, 15)
                .rot("brow_r", Ease.STEP, 0, 0, 0, -15, 9, 0, 0, 25, 18, 0, 0, -5, 27, 0, 0, -18, 36, 0, 0, -15)
                .pos("brow_l", Ease.STEP, 0, 0, 0.3F, 0, 9, 0, -0.2F, 0, 18, 0, 0.1F, 0, 27, 0, 0.35F, 0, 36, 0, 0.3F, 0)
                .pos("brow_r", Ease.STEP, 0, 0, 0.3F, 0, 9, 0, -0.2F, 0, 18, 0, 0.1F, 0, 27, 0, 0.35F, 0, 36, 0, 0.3F, 0)
                .scale("mouth", Ease.STEP, 0, 1, 1.8F, 1, 9, 1, 0.5F, 1, 18, 1.6F, 1, 1, 27, 0.9F, 1.4F, 1, 36, 1, 1.8F, 1)
                .rot("mouth_l", Ease.STEP, 0, 0, 0, 10, 9, 0, 0, 5, 18, 0, 0, -40, 27, 0, 0, 20, 36, 0, 0, 10)
                .rot("mouth_r", Ease.STEP, 0, 0, 0, -10, 9, 0, 0, -5, 18, 0, 0, 40, 27, 0, 0, -20, 36, 0, 0, -10)
                .scale("eye_l", Ease.STEP, 0, 1.3F, 1.3F, 1, 9, 1, 0.5F, 1, 18, 1.2F, 0.9F, 1, 27, 1.4F, 1.4F, 1, 36, 1.3F, 1.3F, 1)
                .scale("eye_r", Ease.STEP, 0, 1.3F, 1.3F, 1, 9, 1, 0.5F, 1, 18, 1.2F, 0.9F, 1, 27, 1.4F, 1.4F, 1, 36, 1.3F, 1.3F, 1));
        // MAT-EXP-10: the supernatural face: brows high and still, the mouth stretched open too long, eyes wide and glowing
        add.accept(clip(MatAnim.EXP_10)
                .pos("brow_l", Ease.CATMULL, 0, 0, 0.5F, 0, 44, 0, 0.5F, 0)
                .pos("brow_r", Ease.CATMULL, 0, 0, 0.5F, 0, 44, 0, 0.5F, 0)
                .scale("mouth", Ease.LINEAR, 0, 0.9F, 2.6F, 1, 1, 0.9F, 2.5F, 1, 2, 0.9F, 2.7F, 1, 3, 0.9F, 2.6F, 1, 44, 0.9F, 2.6F, 1)
                .pos("mouth", Ease.CATMULL, 0, 0, -0.2F, 0, 44, 0, -0.2F, 0)
                .scale("eye_l", Ease.CATMULL, 0, 1.3F, 1.3F, 1, 22, 1.5F, 1.5F, 1, 44, 1.3F, 1.3F, 1)
                .scale("eye_r", Ease.CATMULL, 0, 1.3F, 1.3F, 1, 22, 1.5F, 1.5F, 1, 44, 1.3F, 1.3F, 1)
                .fx(MatRig.FX_GLOW, Ease.CATMULL, 0, 1, 0, 0, 22, 0.8F, 0, 0, 44, 1, 0, 0));
    }
}
