package com.codenamecoraline.client.raven;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Complete set of 10 keyframe animations for the Fey Raven (Section 1.6 of docs/design/fey_raven.md):
 * - animation.fey_raven.idle_breathe (40 ticks / 2.00 s, looping)
 * - animation.fey_raven.idle_head_tilt (25 ticks / 1.25 s)
 * - animation.fey_raven.idle_preen (35 ticks / 1.75 s)
 * - animation.fey_raven.idle_caw (25 ticks / 1.25 s)
 * - animation.fey_raven.walk (12 ticks / 0.60 s, looping)
 * - animation.fey_raven.fly_flap (8 ticks / 0.40 s, looping)
 * - animation.fey_raven.fly_glide (20 ticks / 1.00 s, looping)
 * - animation.fey_raven.peck_attack (15 ticks / 0.75 s, impact at tick 8 / 0.40 s)
 * - animation.fey_raven.hurt (6 ticks / 0.30 s)
 * - animation.fey_raven.death (20 ticks / 1.00 s)
 */
public final class FeyRavenAnimations {
    public static final String ID_IDLE_BREATHE = "animation.fey_raven.idle_breathe";
    public static final String ID_IDLE_HEAD_TILT = "animation.fey_raven.idle_head_tilt";
    public static final String ID_IDLE_PREEN = "animation.fey_raven.idle_preen";
    public static final String ID_IDLE_CAW = "animation.fey_raven.idle_caw";
    public static final String ID_WALK = "animation.fey_raven.walk";
    public static final String ID_FLY_FLAP = "animation.fey_raven.fly_flap";
    public static final String ID_FLY_GLIDE = "animation.fey_raven.fly_glide";
    public static final String ID_PECK_ATTACK = "animation.fey_raven.peck_attack";
    public static final String ID_HURT = "animation.fey_raven.hurt";
    public static final String ID_DEATH = "animation.fey_raven.death";

    private static final AnimationChannel.Interpolation LINEAR = AnimationChannel.Interpolations.LINEAR;
    private static final AnimationChannel.Interpolation SMOOTH = AnimationChannel.Interpolations.CATMULLROM;

    /**
     * animation.fey_raven.idle_breathe (40 ticks / 2.00 s):
     * Gentle chest ruff expansion, slight folded-wing breath and subtle tail balance.
     */
    public static final AnimationDefinition IDLE_BREATHE = AnimationDefinition.Builder.withLength(2.00F).looping()
            .addAnimation("body", rot(SMOOTH, 0.0F, 0, 0, 0, 1.00F, 2.5F, 0, 0, 2.00F, 0, 0, 0))
            .addAnimation("breast_ruff", rot(SMOOTH, 0.0F, 0, 0, 0, 1.00F, -4.0F, 0, 0, 2.00F, 0, 0, 0))
            .addAnimation("neck", rot(SMOOTH, 0.0F, 0, 0, 0, 1.00F, -2.0F, 0, 0, 2.00F, 0, 0, 0))
            .addAnimation("left_wing_inner", rot(SMOOTH, 0.0F, 0, 0, 0, 1.00F, 1.5F, 2.0F, -2.0F, 2.00F, 0, 0, 0))
            .addAnimation("right_wing_inner", rot(SMOOTH, 0.0F, 0, 0, 0, 1.00F, 1.5F, -2.0F, 2.0F, 2.00F, 0, 0, 0))
            .addAnimation("tail_base", rot(SMOOTH, 0.0F, 0, 0, 0, 1.00F, -3.5F, 0, 0, 2.00F, 0, 0, 0))
            .addAnimation("tail_mid", rot(SMOOTH, 0.0F, 0, 0, 0, 1.00F, -2.5F, 0, 0, 2.00F, 0, 0, 0))
            .build();

    /**
     * animation.fey_raven.idle_head_tilt (25 ticks / 1.25 s):
     * Sharp corvid head cock to 35 deg Z and 25 deg Y, followed by an opposite 20 deg glance.
     */
    public static final AnimationDefinition IDLE_HEAD_TILT = AnimationDefinition.Builder.withLength(1.25F)
            .addAnimation("neck", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.20F, -6, 12, 10,
                    0.55F, -6, 14, 12,
                    0.80F, -4, -10, -8,
                    1.05F, -4, -8, -6,
                    1.25F, 0, 0, 0))
            .addAnimation("head", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.20F, 4, 25, 35,
                    0.55F, 5, 28, 35,
                    0.80F, 3, -20, -24,
                    1.05F, 3, -18, -22,
                    1.25F, 0, 0, 0))
            .addAnimation("tail_base", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.40F, 4, -6, 0,
                    0.90F, 4, 5, 0,
                    1.25F, 0, 0, 0))
            .build();

    /**
     * animation.fey_raven.idle_preen (35 ticks / 1.75 s):
     * Turns neck and beak toward the partially raised left wing and preens the rune-etched feathers twice.
     */
    public static final AnimationDefinition IDLE_PREEN = AnimationDefinition.Builder.withLength(1.75F)
            .addAnimation("body", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.35F, 6, -12, 4,
                    1.35F, 6, -10, 4,
                    1.75F, 0, 0, 0))
            .addAnimation("neck", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.35F, 18, -38, -10,
                    0.70F, 24, -44, -14,
                    1.00F, 16, -36, -8,
                    1.30F, 24, -42, -12,
                    1.75F, 0, 0, 0))
            .addAnimation("head", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.35F, 22, -42, -16,
                    0.70F, 32, -48, -22,
                    1.00F, 20, -40, -14,
                    1.30F, 30, -46, -20,
                    1.75F, 0, 0, 0))
            .addAnimation("lower_beak", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.50F, 14, 0, 0,
                    0.70F, 2, 0, 0,
                    1.10F, 14, 0, 0,
                    1.35F, 2, 0, 0,
                    1.75F, 0, 0, 0))
            .addAnimation("left_wing_inner", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.35F, 10, 18, -28,
                    0.85F, 14, 22, -34,
                    1.35F, 10, 18, -26,
                    1.75F, 0, 0, 0))
            .addAnimation("left_wing_outer", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.35F, 0, 14, -18,
                    0.85F, 0, 20, -24,
                    1.75F, 0, 0, 0))
            .build();

    /**
     * animation.fey_raven.idle_caw (25 ticks / 1.25 s):
     * Pitches torso 30 deg forward, stretches neck, opens beak wide and flares the wedge tail.
     */
    public static final AnimationDefinition IDLE_CAW = AnimationDefinition.Builder.withLength(1.25F)
            .addAnimation("body", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.25F, 30, 0, 0,
                    0.85F, 26, 0, 0,
                    1.25F, 0, 0, 0))
            .addAnimation("breast_ruff", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.25F, -12, 0, 0,
                    0.85F, -10, 0, 0,
                    1.25F, 0, 0, 0))
            .addAnimation("neck", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.25F, -18, 0, 0,
                    0.85F, -14, 0, 0,
                    1.25F, 0, 0, 0))
            .addAnimation("head", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.25F, -16, 0, 0,
                    0.55F, -20, 0, 0,
                    0.85F, -14, 0, 0,
                    1.25F, 0, 0, 0))
            .addAnimation("upper_beak", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.22F, -14, 0, 0,
                    0.85F, -12, 0, 0,
                    1.25F, 0, 0, 0))
            .addAnimation("lower_beak", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.22F, 28, 0, 0,
                    0.55F, 32, 0, 0,
                    0.85F, 24, 0, 0,
                    1.25F, 0, 0, 0))
            .addAnimation("left_wing_inner", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.25F, 8, 10, -16,
                    0.85F, 6, 8, -12,
                    1.25F, 0, 0, 0))
            .addAnimation("right_wing_inner", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.25F, 8, -10, 16,
                    0.85F, 6, -8, 12,
                    1.25F, 0, 0, 0))
            .addAnimation("tail_base", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.25F, -16, 0, 0,
                    0.85F, -14, 0, 0,
                    1.25F, 0, 0, 0))
            .addAnimation("tail_tip", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.25F, -10, 0, 0,
                    0.85F, -8, 0, 0,
                    1.25F, 0, 0, 0))
            .build();

    /**
     * animation.fey_raven.walk (12 ticks / 0.60 s, looping):
     * Characteristic corvid ground hop/stride with rhythmic head bob and tail counterbalance.
     */
    public static final AnimationDefinition WALK = AnimationDefinition.Builder.withLength(0.60F).looping()
            .addAnimation("body", pos(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.15F, 0, 0.9F, 0,
                    0.30F, 0, 0, 0,
                    0.45F, 0, 0.9F, 0,
                    0.60F, 0, 0, 0))
            .addAnimation("body", rot(SMOOTH,
                    0.00F, 0, 0, -3,
                    0.15F, 5, 3, 0,
                    0.30F, 0, 0, 3,
                    0.45F, 5, -3, 0,
                    0.60F, 0, 0, -3))
            .addAnimation("neck", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.15F, 10, 0, 0,
                    0.30F, -4, 0, 0,
                    0.45F, 10, 0, 0,
                    0.60F, 0, 0, 0))
            .addAnimation("head", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.15F, -8, 0, 0,
                    0.30F, 4, 0, 0,
                    0.45F, -8, 0, 0,
                    0.60F, 0, 0, 0))
            .addAnimation("left_thigh", rot(LINEAR,
                    0.00F, -26, 0, 0,
                    0.30F, 26, 0, 0,
                    0.60F, -26, 0, 0))
            .addAnimation("left_tarsus", rot(LINEAR,
                    0.00F, 18, 0, 0,
                    0.30F, -12, 0, 0,
                    0.60F, 18, 0, 0))
            .addAnimation("right_thigh", rot(LINEAR,
                    0.00F, 26, 0, 0,
                    0.30F, -26, 0, 0,
                    0.60F, 26, 0, 0))
            .addAnimation("right_tarsus", rot(LINEAR,
                    0.00F, -12, 0, 0,
                    0.30F, 18, 0, 0,
                    0.60F, -12, 0, 0))
            .addAnimation("tail_base", rot(SMOOTH,
                    0.00F, -6, 0, 0,
                    0.15F, 6, 0, 0,
                    0.30F, -6, 0, 0,
                    0.45F, 6, 0, 0,
                    0.60F, -6, 0, 0))
            .build();

    /**
     * animation.fey_raven.fly_flap (8 ticks / 0.40 s, looping):
     * Full wing extension out to the sides (yRot +/-68 deg) and powerful vertical stroke with tucked legs.
     */
    public static final AnimationDefinition FLY_FLAP = AnimationDefinition.Builder.withLength(0.40F).looping()
            .addAnimation("body", rot(SMOOTH,
                    0.00F, 18, 0, 0,
                    0.20F, 12, 0, 0,
                    0.40F, 18, 0, 0))
            .addAnimation("body", pos(SMOOTH,
                    0.00F, 0, -0.8F, 0,
                    0.20F, 0, 0.8F, 0,
                    0.40F, 0, -0.8F, 0))
            .addAnimation("neck", rot(SMOOTH,
                    0.00F, -10, 0, 0,
                    0.20F, -6, 0, 0,
                    0.40F, -10, 0, 0))
            .addAnimation("head", rot(SMOOTH,
                    0.00F, -8, 0, 0,
                    0.20F, -4, 0, 0,
                    0.40F, -8, 0, 0))
            .addAnimation("left_wing_inner", rot(SMOOTH,
                    0.00F, 42, 68, -48,
                    0.20F, 18, 58, 34,
                    0.40F, 42, 68, -48))
            .addAnimation("left_wing_outer", rot(SMOOTH,
                    0.00F, 12, 22, -26,
                    0.20F, -8, -12, 22,
                    0.40F, 12, 22, -26))
            .addAnimation("right_wing_inner", rot(SMOOTH,
                    0.00F, 42, -68, 48,
                    0.20F, 18, -58, -34,
                    0.40F, 42, -68, 48))
            .addAnimation("right_wing_outer", rot(SMOOTH,
                    0.00F, 12, -22, 26,
                    0.20F, -8, 12, -22,
                    0.40F, 12, -22, 26))
            .addAnimation("left_thigh", rot(SMOOTH,
                    0.00F, -38, 0, 0,
                    0.20F, -44, 0, 0,
                    0.40F, -38, 0, 0))
            .addAnimation("right_thigh", rot(SMOOTH,
                    0.00F, -38, 0, 0,
                    0.20F, -44, 0, 0,
                    0.40F, -38, 0, 0))
            .addAnimation("tail_base", rot(SMOOTH,
                    0.00F, 14, 0, 0,
                    0.20F, 4, 0, 0,
                    0.40F, 14, 0, 0))
            .build();

    /**
     * animation.fey_raven.fly_glide (20 ticks / 1.00 s, looping):
     * Soaring posture with horizontally extended wings (yRot +/-66 deg) and fanned wedge tail.
     */
    public static final AnimationDefinition FLY_GLIDE = AnimationDefinition.Builder.withLength(1.00F).looping()
            .addAnimation("body", rot(SMOOTH,
                    0.00F, 16, 0, -2,
                    0.50F, 14, 0, 2,
                    1.00F, 16, 0, -2))
            .addAnimation("neck", rot(SMOOTH,
                    0.00F, -10, 0, 0,
                    0.50F, -8, 0, 0,
                    1.00F, -10, 0, 0))
            .addAnimation("head", rot(SMOOTH,
                    0.00F, -6, 0, 0,
                    0.50F, -4, 0, 0,
                    1.00F, -6, 0, 0))
            .addAnimation("left_wing_inner", rot(SMOOTH,
                    0.00F, 34, 66, -24,
                    0.50F, 32, 64, -18,
                    1.00F, 34, 66, -24))
            .addAnimation("left_wing_outer", rot(SMOOTH,
                    0.00F, 6, 18, -12,
                    0.50F, 6, 16, -8,
                    1.00F, 6, 18, -12))
            .addAnimation("right_wing_inner", rot(SMOOTH,
                    0.00F, 34, -66, 24,
                    0.50F, 32, -64, 18,
                    1.00F, 34, -66, 24))
            .addAnimation("right_wing_outer", rot(SMOOTH,
                    0.00F, 6, -18, 12,
                    0.50F, 6, -16, 8,
                    1.00F, 6, -18, 12))
            .addAnimation("left_thigh", rot(SMOOTH,
                    0.00F, -40, 0, 0,
                    0.50F, -40, 0, 0,
                    1.00F, -40, 0, 0))
            .addAnimation("right_thigh", rot(SMOOTH,
                    0.00F, -40, 0, 0,
                    0.50F, -40, 0, 0,
                    1.00F, -40, 0, 0))
            .addAnimation("tail_base", rot(SMOOTH,
                    0.00F, 12, 0, 0,
                    0.50F, 8, 0, 0,
                    1.00F, 12, 0, 0))
            .build();

    /**
     * animation.fey_raven.peck_attack (15 ticks / 0.75 s):
     * 4-phase aerial/perched strike:
     * - Windup (0.00 -> 0.25 s / ticks 0..5): draws torso and neck back, wings flare wide and high.
     * - Trigger & Impact (0.40 s / tick 8): forward lunge with open beak and forward-swept wings.
     * - Recovery (0.40 -> 0.75 s / ticks 8..15): settles back to neutral perch/flight pose.
     */
    public static final AnimationDefinition PECK_ATTACK = AnimationDefinition.Builder.withLength(0.75F)
            .addAnimation("body", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.25F, -18, 0, 0,
                    0.40F, 14, 0, 0,
                    0.55F, 6, 0, 0,
                    0.75F, 0, 0, 0))
            .addAnimation("body", pos(LINEAR,
                    0.00F, 0, 0, 0,
                    0.25F, 0, 0.6F, 0.4F,
                    0.40F, 0, -0.4F, -1.4F,
                    0.55F, 0, -0.1F, -0.5F,
                    0.75F, 0, 0, 0))
            .addAnimation("neck", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.25F, -16, 0, 0,
                    0.40F, 24, 0, 0,
                    0.55F, 10, 0, 0,
                    0.75F, 0, 0, 0))
            .addAnimation("head", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.25F, -12, 0, 0,
                    0.40F, 18, 0, 0,
                    0.55F, 8, 0, 0,
                    0.75F, 0, 0, 0))
            .addAnimation("lower_beak", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.25F, 22, 0, 0,
                    0.40F, 8, 0, 0,
                    0.75F, 0, 0, 0))
            .addAnimation("left_wing_inner", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.25F, 36, 58, -42,
                    0.40F, 22, 42, 18,
                    0.55F, 10, 20, -10,
                    0.75F, 0, 0, 0))
            .addAnimation("left_wing_outer", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.25F, 10, 18, -24,
                    0.40F, -6, -10, 14,
                    0.75F, 0, 0, 0))
            .addAnimation("right_wing_inner", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.25F, 36, -58, 42,
                    0.40F, 22, -42, -18,
                    0.55F, 10, -20, 10,
                    0.75F, 0, 0, 0))
            .addAnimation("right_wing_outer", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.25F, 10, -18, 24,
                    0.40F, -6, 10, -14,
                    0.75F, 0, 0, 0))
            .addAnimation("tail_base", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.25F, -14, 0, 0,
                    0.40F, 18, 0, 0,
                    0.75F, 0, 0, 0))
            .build();

    /**
     * animation.fey_raven.hurt (6 ticks / 0.30 s):
     * Sharp backward jolt with half-flared wings and ruffled chest feathers.
     */
    public static final AnimationDefinition HURT = AnimationDefinition.Builder.withLength(0.30F)
            .addAnimation("body", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.10F, -18, 0, 8,
                    0.20F, -8, 0, -4,
                    0.30F, 0, 0, 0))
            .addAnimation("head", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.10F, -22, 10, 10,
                    0.30F, 0, 0, 0))
            .addAnimation("left_wing_inner", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.10F, 12, 18, -42,
                    0.30F, 0, 0, 0))
            .addAnimation("right_wing_inner", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.10F, 12, -18, 42,
                    0.30F, 0, 0, 0))
            .build();

    /**
     * animation.fey_raven.death (20 ticks / 1.00 s):
     * Wings lose lift, legs fold and the body tips onto its side with head resting down.
     */
    public static final AnimationDefinition DEATH = AnimationDefinition.Builder.withLength(1.00F)
            .addAnimation("body", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.35F, 18, -8, 32,
                    0.75F, 28, -14, 78,
                    1.00F, 28, -14, 78))
            .addAnimation("body", pos(LINEAR,
                    0.00F, 0, 0, 0,
                    0.35F, 0, -2.0F, 0,
                    0.75F, 0, -4.5F, 0,
                    1.00F, 0, -4.5F, 0))
            .addAnimation("neck", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.35F, 14, 10, 0,
                    0.75F, 26, 18, 12,
                    1.00F, 26, 18, 12))
            .addAnimation("head", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.35F, 16, 12, 8,
                    0.75F, 24, 22, 16,
                    1.00F, 24, 22, 16))
            .addAnimation("left_wing_inner", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.35F, 8, 16, -34,
                    0.75F, 14, 22, -24,
                    1.00F, 14, 22, -24))
            .addAnimation("right_wing_inner", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.35F, 6, -12, 18,
                    0.75F, 10, -8, 10,
                    1.00F, 10, -8, 10))
            .addAnimation("left_thigh", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.75F, 36, 0, 0,
                    1.00F, 36, 0, 0))
            .addAnimation("right_thigh", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.75F, 36, 0, 0,
                    1.00F, 36, 0, 0))
            .build();

    private FeyRavenAnimations() {}

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
