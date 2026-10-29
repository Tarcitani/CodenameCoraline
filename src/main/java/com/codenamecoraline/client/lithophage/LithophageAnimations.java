package com.codenamecoraline.client.lithophage;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Complete set of 12 keyframe animations for the Lithophage (Section 10):
 * - animation.lithophage.idle
 * - animation.lithophage.idle_alternate
 * - animation.lithophage.awakening
 * - animation.lithophage.alert
 * - animation.lithophage.walk
 * - animation.lithophage.turn
 * - animation.lithophage.attack_slam
 * - animation.lithophage.attack_sweep
 * - animation.lithophage.attack_shard
 * - animation.lithophage.attack_recovery
 * - animation.lithophage.hurt
 * - animation.lithophage.death
 */
public final class LithophageAnimations {
    public static final String ID_IDLE = "animation.lithophage.idle";
    public static final String ID_IDLE_ALTERNATE = "animation.lithophage.idle_alternate";
    public static final String ID_AWAKENING = "animation.lithophage.awakening";
    public static final String ID_ALERT = "animation.lithophage.alert";
    public static final String ID_WALK = "animation.lithophage.walk";
    public static final String ID_TURN = "animation.lithophage.turn";
    public static final String ID_ATTACK_SLAM = "animation.lithophage.attack_slam";
    public static final String ID_ATTACK_SWEEP = "animation.lithophage.attack_sweep";
    public static final String ID_ATTACK_SHARD = "animation.lithophage.attack_shard";
    public static final String ID_ATTACK_RECOVERY = "animation.lithophage.attack_recovery";
    public static final String ID_HURT = "animation.lithophage.hurt";
    public static final String ID_DEATH = "animation.lithophage.death";
    public static final String ID_FALL_ASLEEP = "animation.lithophage.fall_asleep";

    private static final AnimationChannel.Interpolation LINEAR = AnimationChannel.Interpolations.LINEAR;
    private static final AnimationChannel.Interpolation SMOOTH = AnimationChannel.Interpolations.CATMULLROM;

    /** animation.lithophage.idle: minimal movement, heavy mineral mass settling. */
    public static final AnimationDefinition IDLE = AnimationDefinition.Builder.withLength(2.4F).looping()
            .addAnimation("body", rot(SMOOTH, 0.0F, 0, 0, 0, 1.2F, 1.8F, 0, 0, 2.4F, 0, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, 0, 0, 0, 1.2F, 1.5F, 0, 0, 2.4F, 0, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, 0, 0, 0, 1.2F, 1.5F, 0, 1.2F, 2.4F, 0, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, 0, 0, 0, 1.2F, 1.5F, 0, -1.2F, 2.4F, 0, 0, 0))
            .build();

    /** animation.lithophage.idle_alternate: occasional asymmetric shoulder contraction and mask tilt. */
    public static final AnimationDefinition IDLE_ALTERNATE = AnimationDefinition.Builder.withLength(2.0F)
            .addAnimation("body", rot(SMOOTH, 0.0F, 0, 0, 0, 0.5F, 3, -4, 2, 1.3F, 2, 3, -1, 2.0F, 0, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, 0, 0, 0, 0.5F, 4, -8, 4, 1.3F, 2, 6, -3, 2.0F, 0, 0, 0))
            .addAnimation("right_arm", pos(SMOOTH, 0.0F, 0, 0, 0, 0.5F, 0, 0.8F, 0, 1.3F, 0, 0.3F, 0, 2.0F, 0, 0, 0))
            .addAnimation("dorsal_crystals", rot(SMOOTH, 0.0F, 0, 0, 0, 0.5F, -4, 0, 2, 1.3F, 2, 0, -1, 2.0F, 0, 0, 0))
            .build();

    /** animation.lithophage.awakening: gradual elevation of the torso, arm repositioning and head lift (1.5 s). */
    public static final AnimationDefinition AWAKENING = AnimationDefinition.Builder.withLength(1.5F)
            .addAnimation("body", rot(SMOOTH, 0.0F, 14, 0, 0, 0.5F, 8, -5, 2, 1.0F, -6, 3, -1, 1.5F, 0, 0, 0))
            .addAnimation("body", pos(SMOOTH, 0.0F, 0, -1.5F, 0, 0.5F, 0, -0.8F, 0, 1.0F, 0, 0.4F, 0, 1.5F, 0, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, 18, 0, 0, 0.55F, 10, -10, 3, 1.05F, -8, 6, 0, 1.5F, 0, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, -25, 0, 6, 0.6F, -40, 10, 14, 1.1F, -12, 4, 8, 1.5F, 0, 0, 0))
            .addAnimation("right_forearm", rot(SMOOTH, 0.0F, -20, 0, 0, 0.6F, -35, 0, 0, 1.1F, -10, 0, 0, 1.5F, 0, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, -22, 0, -6, 0.65F, -32, -8, -12, 1.15F, -10, -3, -6, 1.5F, 0, 0, 0))
            .addAnimation("left_forearm", rot(SMOOTH, 0.0F, -18, 0, 0, 0.65F, -28, 0, 0, 1.15F, -8, 0, 0, 1.5F, 0, 0, 0))
            .addAnimation("dorsal_crystals", rot(SMOOTH, 0.0F, -6, 0, 0, 0.8F, 6, 0, 0, 1.5F, 0, 0, 0))
            .build();

    /** animation.lithophage.alert: squares shoulders and locks the clay mask onto the threat (0.8 s). */
    public static final AnimationDefinition ALERT = AnimationDefinition.Builder.withLength(0.8F)
            .addAnimation("body", rot(SMOOTH, 0.0F, 0, 0, 0, 0.25F, -5, 6, 0, 0.55F, 4, -4, 0, 0.8F, 0, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, 0, 0, 0, 0.25F, -6, 12, 0, 0.55F, 3, -8, 0, 0.8F, 0, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, 0, 0, 0, 0.3F, -18, 0, 10, 0.8F, 0, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, 0, 0, 0, 0.3F, -14, 0, -10, 0.8F, 0, 0, 0))
            .build();

    /** animation.lithophage.walk: heavy step cadence and mineral shoulder roll layered with procedural walk. */
    public static final AnimationDefinition WALK = AnimationDefinition.Builder.withLength(1.2F).looping()
            .addAnimation("body", rot(SMOOTH, 0.0F, 3, 0, -2, 0.3F, 5, 2, 0, 0.6F, 3, 0, 2, 0.9F, 5, -2, 0, 1.2F, 3, 0, -2))
            .addAnimation("dorsal_crystals", rot(SMOOTH, 0.0F, 0, 0, -2, 0.6F, 3, 0, 2, 1.2F, 0, 0, -2))
            .build();

    /** animation.lithophage.turn: heavy torso and leg shift when rotating in place (0.6 s). */
    public static final AnimationDefinition TURN = AnimationDefinition.Builder.withLength(0.6F)
            .addAnimation("body", rot(SMOOTH, 0.0F, 0, 0, 0, 0.2F, 3, 10, -3, 0.4F, 2, -6, 2, 0.6F, 0, 0, 0))
            .addAnimation("right_leg", rot(SMOOTH, 0.0F, 0, 0, 0, 0.2F, -12, 0, 0, 0.4F, 4, 0, 0, 0.6F, 0, 0, 0))
            .addAnimation("left_leg", rot(SMOOTH, 0.0F, 0, 0, 0, 0.2F, 6, 0, 0, 0.4F, -12, 0, 0, 0.6F, 0, 0, 0))
            .build();

    /**
     * animation.lithophage.attack_slam (Golpe de Escória):
     * Wind-up 0.0 -> 0.55 s, vertical smash impact at 0.60 s (tick 12), follow-through until 0.80 s.
     */
    public static final AnimationDefinition ATTACK_SLAM = AnimationDefinition.Builder.withLength(0.8F)
            .addAnimation("body", rot(LINEAR, 0.0F, 0, 0, 0, 0.35F, -16, 8, 0, 0.52F, -20, 10, 0, 0.60F, 28, -6, 0, 0.80F, 20, -3, 0))
            .addAnimation("body", pos(LINEAR, 0.0F, 0, 0, 0, 0.52F, 0, 0.6F, 0.8F, 0.60F, 0, -1.5F, -1.6F, 0.80F, 0, -1.0F, -1.0F))
            .addAnimation("head", rot(LINEAR, 0.0F, 0, 0, 0, 0.52F, -14, 0, 0, 0.60F, 18, 0, 0, 0.80F, 12, 0, 0))
            .addAnimation("right_arm", rot(LINEAR, 0.0F, 0, 0, 0, 0.35F, -125, 10, 12, 0.52F, -145, 12, 15, 0.60F, -25, -8, 4, 0.80F, -20, -4, 2))
            .addAnimation("right_forearm", rot(LINEAR, 0.0F, 0, 0, 0, 0.52F, -40, 0, 0, 0.60F, -8, 0, 0, 0.80F, -12, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0.0F, 0, 0, 0, 0.52F, -35, -10, -14, 0.60F, 24, 0, -8, 0.80F, 16, 0, -6))
            .addAnimation("right_leg", rot(LINEAR, 0.0F, 0, 0, 0, 0.52F, 8, 0, 0, 0.60F, -14, 0, 0, 0.80F, -8, 0, 0))
            .addAnimation("left_leg", rot(LINEAR, 0.0F, 0, 0, 0, 0.52F, -8, 0, 0, 0.60F, 12, 0, 0, 0.80F, 6, 0, 0))
            .build();

    /**
     * animation.lithophage.attack_sweep (Varredura Tectônica):
     * Compresses and coils torso 0.0 -> 0.72 s, executes wide lateral sweep at 0.80 s (tick 16), holds until 1.00 s.
     */
    public static final AnimationDefinition ATTACK_SWEEP = AnimationDefinition.Builder.withLength(1.0F)
            .addAnimation("body", rot(LINEAR, 0.0F, 0, 0, 0, 0.45F, 8, 34, 4, 0.72F, 10, 42, 6, 0.80F, 16, -44, -6, 1.00F, 14, -30, -4))
            .addAnimation("body", pos(LINEAR, 0.0F, 0, 0, 0, 0.72F, 0, -1.2F, 0.6F, 0.80F, 0, -1.5F, -1.4F, 1.00F, 0, -1.0F, -0.8F))
            .addAnimation("head", rot(LINEAR, 0.0F, 0, 0, 0, 0.72F, 6, -22, 0, 0.80F, 10, 24, 0, 1.00F, 8, 16, 0))
            .addAnimation("right_arm", rot(LINEAR, 0.0F, 0, 0, 0, 0.45F, -70, 50, 22, 0.72F, -78, 65, 28, 0.80F, -68, -58, -10, 1.00F, -50, -40, -6))
            .addAnimation("right_forearm", rot(LINEAR, 0.0F, 0, 0, 0, 0.72F, -30, 0, 0, 0.80F, -8, -20, 0, 1.00F, -12, -10, 0))
            .addAnimation("left_arm", rot(LINEAR, 0.0F, 0, 0, 0, 0.72F, 20, 20, -18, 0.80F, -30, -25, -12, 1.00F, -18, -15, -8))
            .build();

    /**
     * animation.lithophage.attack_shard (Estilhaço de Âmbar):
     * Retracts right arm and channels mineral shard 0.0 -> 0.68 s, fires at 0.75 s (tick 15), finishes at 0.95 s.
     */
    public static final AnimationDefinition ATTACK_SHARD = AnimationDefinition.Builder.withLength(0.95F)
            .addAnimation("body", rot(LINEAR, 0.0F, 0, 0, 0, 0.40F, -10, 22, 0, 0.68F, -14, 28, 0, 0.75F, 18, -18, 0, 0.95F, 12, -10, 0))
            .addAnimation("head", rot(LINEAR, 0.0F, 0, 0, 0, 0.68F, -8, -18, 0, 0.75F, 10, 12, 0, 0.95F, 6, 6, 0))
            .addAnimation("right_arm", rot(LINEAR, 0.0F, 0, 0, 0, 0.40F, -95, 25, 16, 0.68F, -110, 32, 20, 0.75F, -78, -12, 0, 0.95F, -45, -6, 0))
            .addAnimation("right_forearm", rot(LINEAR, 0.0F, 0, 0, 0, 0.40F, -55, 0, 0, 0.68F, -65, 0, 0, 0.75F, -5, 0, 0, 0.95F, -10, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0.0F, 0, 0, 0, 0.68F, -35, -15, -14, 0.75F, 22, 0, -10, 0.95F, 12, 0, -6))
            .build();

    /**
     * animation.lithophage.attack_recovery:
     * Heavy post-attack vulnerability window where torso and arms sag before rising back to combat posture (0.9 s).
     */
    public static final AnimationDefinition ATTACK_RECOVERY = AnimationDefinition.Builder.withLength(0.9F)
            .addAnimation("body", rot(SMOOTH, 0.0F, 18, 0, 0, 0.35F, 22, 0, 2, 0.70F, 8, 0, 0, 0.90F, 0, 0, 0))
            .addAnimation("body", pos(SMOOTH, 0.0F, 0, -1.0F, -0.8F, 0.35F, 0, -1.2F, -0.6F, 0.90F, 0, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, 14, 0, 0, 0.35F, 18, 0, 0, 0.90F, 0, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, -22, 0, 6, 0.35F, -16, 0, 8, 0.90F, 0, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, -16, 0, -6, 0.35F, -12, 0, -8, 0.90F, 0, 0, 0))
            .build();

    /** animation.lithophage.hurt: brief mineral recoil without interrupting the controller (0.5 s). */
    public static final AnimationDefinition HURT = AnimationDefinition.Builder.withLength(0.5F)
            .addAnimation("body", rot(LINEAR, 0.0F, 0, 0, 0, 0.12F, -12, 0, 6, 0.30F, -5, 0, -3, 0.50F, 0, 0, 0))
            .addAnimation("head", rot(LINEAR, 0.0F, 0, 0, 0, 0.12F, -16, 6, 8, 0.30F, -6, -4, -3, 0.50F, 0, 0, 0))
            .addAnimation("dorsal_crystals", rot(LINEAR, 0.0F, 0, 0, 0, 0.12F, -10, 0, 5, 0.50F, 0, 0, 0))
            .build();

    /** animation.lithophage.death: loss of balance, knees buckling and full mineral collapse (1.0 s). */
    public static final AnimationDefinition DEATH = AnimationDefinition.Builder.withLength(1.0F)
            .addAnimation("body", rot(LINEAR, 0.0F, 0, 0, 0, 0.35F, 28, -8, 6, 0.75F, 74, -12, 10, 1.00F, 74, -12, 10))
            .addAnimation("body", pos(LINEAR, 0.0F, 0, 0, 0, 0.35F, 0, -4.0F, -1.5F, 0.75F, 0, -8.5F, -3.0F, 1.00F, 0, -8.5F, -3.0F))
            .addAnimation("head", rot(LINEAR, 0.0F, 0, 0, 0, 0.35F, 18, 10, 0, 0.75F, -22, 18, 8, 1.00F, -22, 18, 8))
            .addAnimation("right_arm", rot(LINEAR, 0.0F, 0, 0, 0, 0.35F, -40, 0, 18, 0.75F, -75, 15, 28, 1.00F, -75, 15, 28))
            .addAnimation("left_arm", rot(LINEAR, 0.0F, 0, 0, 0, 0.35F, -35, 0, -18, 0.75F, -70, -15, -26, 1.00F, -70, -15, -26))
            .addAnimation("right_leg", rot(LINEAR, 0.0F, 0, 0, 0, 0.35F, -38, 0, 0, 0.75F, -65, 8, 0, 1.00F, -65, 8, 0))
            .addAnimation("right_shin", rot(LINEAR, 0.0F, 0, 0, 0, 0.35F, 55, 0, 0, 0.75F, 80, 0, 0, 1.00F, 80, 0, 0))
            .addAnimation("left_leg", rot(LINEAR, 0.0F, 0, 0, 0, 0.35F, -32, 0, 0, 0.75F, -58, -8, 0, 1.00F, -58, -8, 0))
            .addAnimation("left_shin", rot(LINEAR, 0.0F, 0, 0, 0, 0.35F, 50, 0, 0, 0.75F, 75, 0, 0, 1.00F, 75, 0, 0))
            .build();

    /** animation.lithophage.fall_asleep: heavy sagging of the mineral torso and arms folding back into dormant posture (1.4 s). */
    public static final AnimationDefinition FALL_ASLEEP = AnimationDefinition.Builder.withLength(1.4F)
            .addAnimation("body", rot(SMOOTH, 0.0F, 0, 0, 0, 0.45F, 6, 0, 0, 0.95F, 12, 0, 0, 1.4F, 14, 0, 0))
            .addAnimation("body", pos(SMOOTH, 0.0F, 0, 0, 0, 0.45F, 0, -0.6F, 0, 0.95F, 0, -1.2F, 0, 1.4F, 0, -1.5F, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, 0, 0, 0, 0.50F, 8, 0, 0, 1.4F, 18, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, 0, 0, 0, 0.50F, -10, 0, 4, 1.4F, -25, 0, 6))
            .addAnimation("right_forearm", rot(SMOOTH, 0.0F, 0, 0, 0, 0.50F, -8, 0, 0, 1.4F, -20, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, 0, 0, 0, 0.50F, -8, 0, -4, 1.4F, -22, 0, -6))
            .addAnimation("left_forearm", rot(SMOOTH, 0.0F, 0, 0, 0, 0.50F, -6, 0, 0, 1.4F, -18, 0, 0))
            .addAnimation("dorsal_crystals", rot(SMOOTH, 0.0F, 0, 0, 0, 0.60F, 4, 0, 0, 1.4F, -6, 0, 0))
            .build();

    private LithophageAnimations() {}

    private static AnimationChannel rot(AnimationChannel.Interpolation interpolation, float... frames) {
        Keyframe[] keyframes = new Keyframe[frames.length / 4];
        for (int i = 0; i < keyframes.length; i++) {
            keyframes[i] = new Keyframe(frames[i * 4],
                    KeyframeAnimations.degreeVec(frames[i * 4 + 1], frames[i * 4 + 2], frames[i * 4 + 3]),
                    interpolation);
        }
        return new AnimationChannel(AnimationChannel.Targets.ROTATION, keyframes);
    }

    private static AnimationChannel pos(AnimationChannel.Interpolation interpolation, float... frames) {
        Keyframe[] keyframes = new Keyframe[frames.length / 4];
        for (int i = 0; i < keyframes.length; i++) {
            keyframes[i] = new Keyframe(frames[i * 4],
                    KeyframeAnimations.posVec(frames[i * 4 + 1], frames[i * 4 + 2], frames[i * 4 + 3]),
                    interpolation);
        }
        return new AnimationChannel(AnimationChannel.Targets.POSITION, keyframes);
    }
}

