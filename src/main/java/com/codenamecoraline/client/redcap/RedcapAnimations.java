package com.codenamecoraline.client.redcap;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Keyframe animations of the Redcap, applied on top of the procedural walk and breathing in RedcapModel.
 * Rotations are in degrees and positions in model pixels (Blockbench convention: +y is up).
 * Bone names are the part names in RedcapModel.createBodyLayer.
 */
final class RedcapAnimations {
    private static final AnimationChannel.Interpolation LINEAR = AnimationChannel.Interpolations.LINEAR;
    private static final AnimationChannel.Interpolation SMOOTH = AnimationChannel.Interpolations.CATMULLROM;

    /**
     * Two-handed pike thrust. Wind-up until 0.18 s, strike at 0.25 s (Redcap.THRUST_WINDUP_TICKS, when the
     * damage lands), hold until 0.42 s, recover by 0.75 s. The pike rotations were solved from the posed hand
     * matrices as absolute angles, then reduced by the resting pose of the pike in RedcapModel (64.3, 0, 44.7
     * degrees; the channel adds to it), so the blade points at the target: level at 0.18 s, chest height at the strike.
     */
    static final AnimationDefinition PIKE_THRUST = AnimationDefinition.Builder.withLength(0.75F)
            .addAnimation("body", rot(LINEAR, 0, 0, 0, 0, 0.18F, -10, 0, 0, 0.25F, 22, -8, 0, 0.42F, 18, -6, 0, 0.75F, 0, 0, 0))
            .addAnimation("body", pos(LINEAR, 0, 0, 0, 0, 0.18F, 0, 0, 1, 0.25F, 0, 0, -2, 0.42F, 0, 0, -1.8F, 0.75F, 0, 0, 0))
            .addAnimation("head", rot(LINEAR, 0, 0, 0, 0, 0.18F, -6, 0, 0, 0.25F, 12, -6, 0, 0.42F, 10, -5, 0, 0.75F, 0, 0, 0))
            .addAnimation("head", pos(LINEAR, 0, 0, 0, 0, 0.18F, 0, 0.9F, 1.8F, 0.25F, 0, -2, -3.8F, 0.42F, 0, -1.8F, -3.5F, 0.75F, 0, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0, 0, 0, 0, 0.18F, -35, 10, 5, 0.25F, -80, 15, 0, 0.42F, -75, 12, 0, 0.75F, 0, 0, 0))
            .addAnimation("left_arm", pos(LINEAR, 0, 0, 0, 0, 0.18F, 0, 0.75F, 1.9F, 0.25F, 0, -1.7F, -4.1F, 0.42F, 0, -1.5F, -3.8F, 0.75F, 0, 0, 0))
            .addAnimation("left_forearm", rot(LINEAR, 0, 0, 0, 0, 0.18F, -20, 0, 0, 0.25F, 20, 0, 0, 0.42F, 15, 0, 0, 0.75F, 0, 0, 0))
            .addAnimation("pike", rot(LINEAR, 0, 0, 0, 0, 0.12F, -9.4F, 0, 135.0F, 0.18F, -58.1F, 0, 135.3F, 0.25F, -53.3F, 0, 133.2F, 0.42F, -55.2F, 0, 134.0F, 0.58F, 12.3F, 0, -22.2F, 0.75F, 0, 0, 0))
            // Grips the shaft nearer its middle while thrusting, so the blade stops at the target instead of far behind it.
            .addAnimation("pike", pos(LINEAR, 0, 0, 0, 0, 0.18F, 0, 12, 0, 0.42F, 0, 12, 0, 0.75F, 0, 0, 0))
            // The free right arm only counterbalances: it swings back and out and stays out of the thrust.
            .addAnimation("right_arm", rot(LINEAR, 0, 0, 0, 0, 0.18F, 18, 0, 14, 0.25F, 30, 0, 18, 0.42F, 26, 0, 16, 0.75F, 0, 0, 0))
            .addAnimation("right_arm", pos(LINEAR, 0, 0, 0, 0, 0.18F, 0, 0, 1, 0.25F, 0, 0, -2, 0.42F, 0, 0, -1.8F, 0.75F, 0, 0, 0))
            .addAnimation("right_forearm", rot(LINEAR, 0, 0, 0, 0, 0.25F, 10, 0, 0, 0.42F, 8, 0, 0, 0.75F, 0, 0, 0))
            .addAnimation("right_ear", rot(LINEAR, 0, 0, 0, 0, 0.25F, 0, 25, 0, 0.42F, 0, 20, 0, 0.75F, 0, 0, 0))
            .addAnimation("left_ear", rot(LINEAR, 0, 0, 0, 0, 0.25F, 0, -25, 0, 0.42F, 0, -20, 0, 0.75F, 0, 0, 0))
            .addAnimation("tip_4", rot(SMOOTH, 0, 0, 0, 0, 0.25F, -25, 0, 0, 0.42F, 15, 0, 0, 0.75F, 0, 0, 0))
            .build();

    /**
     * One-handed dagger slash, left arm. Raises the dagger behind the head until 0.1 s, strikes down and
     * forward at 0.15 s (3 ticks, when the damage lands), holds, recovers by 0.6 s.
     */
    static final AnimationDefinition DAGGER_SLASH = AnimationDefinition.Builder.withLength(0.6F)
            .addAnimation("body", rot(LINEAR, 0, 0, 0, 0, 0.1F, -12, 0, 0, 0.15F, 18, 0, 0, 0.3F, 14, 0, 0, 0.6F, 0, 0, 0))
            .addAnimation("body", pos(LINEAR, 0, 0, 0, 0, 0.1F, 0, 0, 0.8F, 0.15F, 0, 0, -1.6F, 0.3F, 0, 0, -1.2F, 0.6F, 0, 0, 0))
            .addAnimation("head", rot(LINEAR, 0, 0, 0, 0, 0.1F, -8, 0, 0, 0.15F, 8, 0, 0, 0.3F, 6, 0, 0, 0.6F, 0, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0, 0, 0, 0, 0.1F, -140, 0, 20, 0.15F, -60, 0, 0, 0.3F, -52, 0, 0, 0.6F, 0, 0, 0))
            .addAnimation("left_forearm", rot(LINEAR, 0, 0, 0, 0, 0.1F, -50, 0, 0, 0.15F, -10, 0, 0, 0.3F, -10, 0, 0, 0.6F, 0, 0, 0))
            // Solved against the posed hand (dagger rest 73.3, 0, 79.8 degrees; the channel adds to it): raised behind
            // the head at 0.1 s, forward and slightly down at the strike.
            .addAnimation("dagger", rot(LINEAR, 0, 0, 0, 0, 0.1F, -94.8F, 0, 114.5F, 0.15F, -91.7F, 0, 125.3F, 0.3F, -93.8F, 0, 126.2F, 0.6F, 0, 0, 0))
            .addAnimation("right_arm", rot(LINEAR, 0, 0, 0, 0, 0.1F, 22, 0, 16, 0.15F, 28, 0, 18, 0.3F, 24, 0, 16, 0.6F, 0, 0, 0))
            .addAnimation("right_arm", pos(LINEAR, 0, 0, 0, 0, 0.1F, 0, 0, 0.8F, 0.15F, 0, 0, -1.6F, 0.3F, 0, 0, -1.2F, 0.6F, 0, 0, 0))
            .build();

    /** Claw swipe with the free right hand: sweeps from out on the right, across the body, to the left. */
    static final AnimationDefinition CLAW_SWIPE_RIGHT = AnimationDefinition.Builder.withLength(0.6F)
            .addAnimation("body", rot(LINEAR, 0, 0, 0, 0, 0.1F, 0, 14, 0, 0.15F, 8, -20, 0, 0.3F, 6, -14, 0, 0.6F, 0, 0, 0))
            .addAnimation("body", pos(LINEAR, 0, 0, 0, 0, 0.1F, 0, 0, 0.6F, 0.15F, 0, 0, -1.4F, 0.3F, 0, 0, -1F, 0.6F, 0, 0, 0))
            .addAnimation("head", rot(LINEAR, 0, 0, 0, 0, 0.1F, 0, 10, 0, 0.15F, 8, -10, 0, 0.6F, 0, 0, 0))
            .addAnimation("right_arm", rot(LINEAR, 0, 0, 0, 0, 0.1F, -75, 70, 0, 0.15F, -75, -45, 0, 0.3F, -65, -40, 0, 0.6F, 0, 0, 0))
            .addAnimation("right_arm", pos(LINEAR, 0, 0, 0, 0, 0.1F, 0, 0, 0.6F, 0.15F, 0, 0, -1.4F, 0.3F, 0, 0, -1F, 0.6F, 0, 0, 0))
            .addAnimation("right_forearm", rot(LINEAR, 0, 0, 0, 0, 0.1F, -20, 0, 0, 0.15F, -5, 0, 0, 0.3F, -5, 0, 0, 0.6F, 0, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0, 0, 0, 0, 0.1F, 12, 0, 12, 0.15F, 18, 0, 14, 0.6F, 0, 0, 0))
            .build();

    /** Mirror of CLAW_SWIPE_RIGHT with the left hand. */
    static final AnimationDefinition CLAW_SWIPE_LEFT = AnimationDefinition.Builder.withLength(0.6F)
            .addAnimation("body", rot(LINEAR, 0, 0, 0, 0, 0.1F, 0, -14, 0, 0.15F, 8, 20, 0, 0.3F, 6, 14, 0, 0.6F, 0, 0, 0))
            .addAnimation("body", pos(LINEAR, 0, 0, 0, 0, 0.1F, 0, 0, 0.6F, 0.15F, 0, 0, -1.4F, 0.3F, 0, 0, -1F, 0.6F, 0, 0, 0))
            .addAnimation("head", rot(LINEAR, 0, 0, 0, 0, 0.1F, 0, -10, 0, 0.15F, 8, 10, 0, 0.6F, 0, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0, 0, 0, 0, 0.1F, -75, -70, 0, 0.15F, -75, 45, 0, 0.3F, -65, 40, 0, 0.6F, 0, 0, 0))
            .addAnimation("left_arm", pos(LINEAR, 0, 0, 0, 0, 0.1F, 0, 0, 0.6F, 0.15F, 0, 0, -1.4F, 0.3F, 0, 0, -1F, 0.6F, 0, 0, 0))
            .addAnimation("left_forearm", rot(LINEAR, 0, 0, 0, 0, 0.1F, -20, 0, 0, 0.15F, -5, 0, 0, 0.3F, -5, 0, 0, 0.6F, 0, 0, 0))
            .addAnimation("right_arm", rot(LINEAR, 0, 0, 0, 0, 0.1F, 12, 0, -12, 0.15F, 18, 0, -14, 0.6F, 0, 0, 0))
            .build();

    /**
     * Rock throw with the right hand: lifts the rock over the head (0.2 to 0.35 s, leaning back), then snaps
     * forward and lets go at 0.45 s (Redcap.ROCK_RELEASE_TICKS); RedcapModel hides the rock from that moment.
     */
    static final AnimationDefinition ROCK_THROW = AnimationDefinition.Builder.withLength(1.1F)
            .addAnimation("body", rot(LINEAR, 0, 0, 0, 0, 0.2F, -16, 0, 0, 0.35F, -18, 0, 0, 0.45F, 24, 0, 0, 0.7F, 18, 0, 0, 1.1F, 0, 0, 0))
            .addAnimation("body", pos(LINEAR, 0, 0, 0, 0, 0.2F, 0, 0, 1F, 0.35F, 0, 0, 1.2F, 0.45F, 0, 0, -2F, 0.7F, 0, 0, -1.5F, 1.1F, 0, 0, 0))
            .addAnimation("head", rot(LINEAR, 0, 0, 0, 0, 0.2F, -14, 0, 0, 0.35F, -14, 0, 0, 0.45F, 14, 0, 0, 0.7F, 10, 0, 0, 1.1F, 0, 0, 0))
            .addAnimation("right_arm", rot(LINEAR, 0, 0, 0, 0, 0.2F, -165, 0, -12, 0.35F, -175, 0, -12, 0.45F, -80, 0, -5, 0.7F, -55, 0, 0, 1.1F, 0, 0, 0))
            .addAnimation("right_arm", pos(LINEAR, 0, 0, 0, 0, 0.2F, 0, 0, 1F, 0.35F, 0, 0, 1.2F, 0.45F, 0, 0, -2F, 0.7F, 0, 0, -1.5F, 1.1F, 0, 0, 0))
            .addAnimation("right_forearm", rot(LINEAR, 0, 0, 0, 0, 0.2F, -55, 0, 0, 0.35F, -80, 0, 0, 0.45F, -5, 0, 0, 0.7F, -5, 0, 0, 1.1F, 0, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0, 0, 0, 0, 0.2F, -50, 0, -20, 0.35F, -55, 0, -22, 0.45F, 10, 0, 6, 1.1F, 0, 0, 0))
            .addAnimation("left_arm", pos(LINEAR, 0, 0, 0, 0, 0.2F, 0, 0, 1F, 0.35F, 0, 0, 1.2F, 0.45F, 0, 0, -2F, 0.7F, 0, 0, -1.5F, 1.1F, 0, 0, 0))
            .build();

    /** Straightens up and sniffs the air in short bursts, ears perked, then scans left and right. */
    static final AnimationDefinition SNIFF = AnimationDefinition.Builder.withLength(2.4F)
            .addAnimation("head", rot(SMOOTH, 0, 0, 0, 0, 0.3F, -25, 0, 0, 0.45F, -20, 0, 0, 0.55F, -27, 0, 0, 0.65F, -21, 0, 0,
                    0.75F, -27, 0, 0, 0.85F, -22, 0, 0, 1.3F, -18, 25, 0, 1.7F, -18, -20, 0, 2.1F, -8, 0, 0, 2.4F, 0, 0, 0))
            .addAnimation("head", pos(SMOOTH, 0, 0, 0, 0, 0.3F, 0, 0.75F, 0.7F, 2.1F, 0, 0.75F, 0.7F, 2.4F, 0, 0, 0))
            .addAnimation("body", rot(SMOOTH, 0, 0, 0, 0, 0.3F, -8, 0, 0, 2.1F, -8, 0, 0, 2.4F, 0, 0, 0))
            .addAnimation("right_arm", pos(SMOOTH, 0, 0, 0, 0, 0.3F, 0, 0.6F, 0.6F, 2.1F, 0, 0.6F, 0.6F, 2.4F, 0, 0, 0))
            .addAnimation("left_arm", pos(SMOOTH, 0, 0, 0, 0, 0.3F, 0, 0.6F, 0.6F, 2.1F, 0, 0.6F, 0.6F, 2.4F, 0, 0, 0))
            .addAnimation("right_ear", rot(SMOOTH, 0, 0, 0, 0, 0.3F, 0, -12, 15, 2.1F, 0, -12, 15, 2.4F, 0, 0, 0))
            .addAnimation("left_ear", rot(SMOOTH, 0, 0, 0, 0, 0.3F, 0, 12, -15, 2.1F, 0, 12, -15, 2.4F, 0, 0, 0))
            .build();

    /** Slowly turns head and shoulders to one side, then the other. */
    static final AnimationDefinition LOOK_AROUND = AnimationDefinition.Builder.withLength(3.2F)
            .addAnimation("head", rot(SMOOTH, 0, 0, 0, 0, 0.5F, 5, 55, 0, 1.2F, 5, 55, 0, 1.8F, 5, -50, 0, 2.5F, 5, -50, 0, 3.2F, 0, 0, 0))
            .addAnimation("body", rot(SMOOTH, 0, 0, 0, 0, 0.5F, 0, 12, 0, 1.2F, 0, 12, 0, 1.8F, 0, -10, 0, 2.5F, 0, -10, 0, 3.2F, 0, 0, 0))
            .addAnimation("right_ear", rot(SMOOTH, 0, 0, 0, 0, 0.6F, 0, 0, 12, 1.2F, 0, 0, 0, 1.9F, 0, 0, 12, 2.5F, 0, 0, 0, 3.2F, 0, 0, 0))
            .addAnimation("left_ear", rot(SMOOTH, 0, 0, 0, 0, 0.6F, 0, 0, -12, 1.2F, 0, 0, 0, 1.9F, 0, 0, -12, 2.5F, 0, 0, 0, 3.2F, 0, 0, 0))
            .build();

    /**
     * Lays the pike across the hips, blade towards the free hand, and scrapes the blade with the claws.
     * The pike rotation and slide along the shaft were solved from the posed hand matrices so the blade
     * centre sits at the right hand's claws while the left hand keeps gripping the shaft; the strokes swing
     * the right arm along the blade.
     */
    static final AnimationDefinition SHARPEN = AnimationDefinition.Builder.withLength(3.0F)
            // Linear holds: Catmull-Rom would sag between equal keyframes and pull the blade off the claws.
            .addAnimation("body", rot(LINEAR, 0, 0, 0, 0, 0.4F, 8, 0, 0, 2.6F, 8, 0, 0, 3.0F, 0, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0, 0, 0, 0, 0.4F, -25, 0, 0, 2.6F, -25, 0, 0, 3.0F, 0, 0, 0))
            .addAnimation("pike", rot(LINEAR, 0, 0, 0, 0, 0.4F, -35.3F, 0, -123.2F, 2.6F, -35.3F, 0, -123.2F, 3.0F, 0, 0, 0))
            .addAnimation("pike", pos(LINEAR, 0, 0, 0, 0, 0.4F, 6.06F, -1.23F, 3.43F, 2.6F, 6.06F, -1.23F, 3.43F, 3.0F, 0, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0, 0, 0, 0, 0.45F, -20, 0, 5, 0.7F, -20, 0, -18, 1.0F, -20, 0, 12, 1.3F, -20, 0, -18,
                    1.6F, -20, 0, 12, 1.9F, -20, 0, -18, 2.2F, -20, 0, 12, 2.6F, -20, 0, 0, 3.0F, 0, 0, 0))
            .addAnimation("right_forearm", rot(LINEAR, 0, 0, 0, 0, 0.45F, -25, 0, 0, 2.6F, -25, 0, 0, 3.0F, 0, 0, 0))
            .addAnimation("head", rot(LINEAR, 0, 0, 0, 0, 0.4F, 25, 20, 0, 2.6F, 25, 20, 0, 3.0F, 0, 0, 0))
            .build();

    /** Reaches up to feel the cap (is the blood still wet?), then studies the claws with a grin. */
    static final AnimationDefinition CAP_CHECK = AnimationDefinition.Builder.withLength(2.6F)
            .addAnimation("right_arm", rot(SMOOTH, 0, 0, 0, 0, 0.45F, -150, 0, -20, 0.9F, -150, 0, -20, 1.3F, -95, 0, -15, 2.0F, -95, 0, -15, 2.6F, 0, 0, 0))
            .addAnimation("right_forearm", rot(SMOOTH, 0, 0, 0, 0, 0.45F, -40, 0, 0, 0.55F, -30, 0, 0, 0.7F, -45, 0, 0, 0.85F, -30, 0, 0,
                    1.3F, -60, 0, 0, 2.0F, -60, 0, 0, 2.6F, 0, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0, 0, 0, 0, 0.45F, -10, 10, 0, 0.9F, -10, 10, 0, 1.3F, 20, 20, 0, 2.0F, 15, 20, 0, 2.6F, 0, 0, 0))
            .addAnimation("right_ear", rot(SMOOTH, 0, 0, 0, 0, 0.45F, 0, 0, -20, 0.9F, 0, 0, -20, 1.3F, 0, 0, 0, 2.6F, 0, 0, 0))
            .build();

    private RedcapAnimations() {}

    /** Rotation channel from (time, x, y, z) quadruples, angles in degrees. */
    private static AnimationChannel rot(AnimationChannel.Interpolation interpolation, float... frames) {
        Keyframe[] keyframes = new Keyframe[frames.length / 4];
        for (int i = 0; i < keyframes.length; i++) {
            keyframes[i] = new Keyframe(frames[i * 4], KeyframeAnimations.degreeVec(frames[i * 4 + 1], frames[i * 4 + 2], frames[i * 4 + 3]), interpolation);
        }
        return new AnimationChannel(AnimationChannel.Targets.ROTATION, keyframes);
    }

    /** Position channel from (time, x, y, z) quadruples, offsets in model pixels with +y up. */
    private static AnimationChannel pos(AnimationChannel.Interpolation interpolation, float... frames) {
        Keyframe[] keyframes = new Keyframe[frames.length / 4];
        for (int i = 0; i < keyframes.length; i++) {
            keyframes[i] = new Keyframe(frames[i * 4], KeyframeAnimations.posVec(frames[i * 4 + 1], frames[i * 4 + 2], frames[i * 4 + 3]), interpolation);
        }
        return new AnimationChannel(AnimationChannel.Targets.POSITION, keyframes);
    }
}
