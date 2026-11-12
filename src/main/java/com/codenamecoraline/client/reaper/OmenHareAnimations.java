package com.codenamecoraline.client.reaper;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Keyframe animations for {@link OmenHareModel}.
 * Spec: {@code docs/design/soul_reaper_animation.md} §3.
 */
public final class OmenHareAnimations {
    private OmenHareAnimations() {}

    private static Keyframe rot(float time, float xDeg, float yDeg, float zDeg) {
        return new Keyframe(
                time,
                KeyframeAnimations.degreeVec(xDeg, yDeg, zDeg),
                AnimationChannel.Interpolations.CATMULLROM);
    }

    private static Keyframe pos(float time, float dx, float dy, float dz) {
        return new Keyframe(
                time,
                KeyframeAnimations.posVec(dx, dy, dz),
                AnimationChannel.Interpolations.CATMULLROM);
    }

    /** HARE_APPROACH (0.8 s loop): deliberate fearless snow hop. */
    public static final AnimationDefinition HARE_APPROACH = AnimationDefinition.Builder.withLength(0.8F).looping()
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    pos(0.0F, 0.0F, 0.0F, 0.0F),
                    pos(0.35F, 0.0F, 2.2F, 0.0F),
                    pos(0.80F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.25F, -14.0F, 0.0F, 0.0F),
                    rot(0.55F, 12.0F, 0.0F, 0.0F),
                    rot(0.80F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_Front_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.30F, -38.0F, 0.0F, 0.0F),
                    rot(0.60F, 24.0F, 0.0F, 0.0F),
                    rot(0.80F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("right_front_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.30F, -38.0F, 0.0F, 0.0F),
                    rot(0.60F, 24.0F, 0.0F, 0.0F),
                    rot(0.80F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_hind_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.35F, 44.0F, 0.0F, 0.0F),
                    rot(0.80F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("right_hind_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.35F, 44.0F, 0.0F, 0.0F),
                    rot(0.80F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** HARE_STAND_STARE (2.0 s loop): rises onto hind legs in eerie bipedal sentinel posture, staring at the player. */
    public static final AnimationDefinition HARE_STAND_STARE = AnimationDefinition.Builder.withLength(2.0F).looping()
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, -62.0F, 0.0F, 0.0F),
                    rot(1.0F, -65.0F, 0.0F, 0.0F),
                    rot(2.0F, -62.0F, 0.0F, 0.0F)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 58.0F, 0.0F, 0.0F),
                    rot(1.0F, 62.0F, 0.0F, 0.0F),
                    rot(2.0F, 58.0F, 0.0F, 0.0F)))
            .addAnimation("left_Front_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 48.0F, 0.0F, -8.0F),
                    rot(1.0F, 54.0F, 0.0F, -12.0F),
                    rot(2.0F, 48.0F, 0.0F, -8.0F)))
            .addAnimation("right_front_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 48.0F, 0.0F, 8.0F),
                    rot(1.0F, 54.0F, 0.0F, 12.0F),
                    rot(2.0F, 48.0F, 0.0F, 8.0F)))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, -8.0F, 0.0F, -6.0F),
                    rot(0.4F, -16.0F, 0.0F, -14.0F),
                    rot(0.8F, -8.0F, 0.0F, -6.0F),
                    rot(2.0F, -8.0F, 0.0F, -6.0F)))
            .build();

    /** HARE_TILT_TAUNT (2.0 s loop): bipedal posture with an unnatural 42 deg head tilt and asymmetric ear twitch. */
    public static final AnimationDefinition HARE_TILT_TAUNT = AnimationDefinition.Builder.withLength(2.0F).looping()
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, -64.0F, 0.0F, 0.0F),
                    rot(1.0F, -66.0F, 0.0F, 0.0F),
                    rot(2.0F, -64.0F, 0.0F, 0.0F)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 58.0F, 0.0F, 0.0F),
                    rot(0.35F, 62.0F, 0.0F, 42.0F),
                    rot(1.45F, 62.0F, 0.0F, 44.0F),
                    rot(2.0F, 58.0F, 0.0F, 0.0F)))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, -10.0F, 0.0F, -8.0F),
                    rot(0.35F, -22.0F, 0.0F, -28.0F),
                    rot(1.45F, -24.0F, 0.0F, -32.0F),
                    rot(2.0F, -10.0F, 0.0F, -8.0F)))
            .addAnimation("right_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, -10.0F, 0.0F, 8.0F),
                    rot(0.35F, 12.0F, 0.0F, 18.0F),
                    rot(1.45F, 14.0F, 0.0F, 22.0F),
                    rot(2.0F, -10.0F, 0.0F, 8.0F)))
            .addAnimation("left_Front_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 52.0F, 0.0F, -10.0F),
                    rot(2.0F, 52.0F, 0.0F, -10.0F)))
            .addAnimation("right_front_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 52.0F, 0.0F, 10.0F),
                    rot(2.0F, 52.0F, 0.0F, 10.0F)))
            .build();

    /** HARE_RUPTURE (2.0 s / 40 ticks): arches spine, convulses, and ruptures upward as the Reaper's shadow erupts. */
    public static final AnimationDefinition HARE_RUPTURE = AnimationDefinition.Builder.withLength(2.0F)
            .addAnimation("hare_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    pos(0.0F, 0.0F, 0.0F, 0.0F),
                    pos(0.8F, 0.0F, 3.5F, 0.0F),
                    pos(1.6F, 0.0F, 8.0F, 0.0F),
                    pos(2.0F, 0.0F, 12.0F, 0.0F)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, -45.0F, 0.0F, 0.0F),
                    rot(0.8F, -78.0F, 12.0F, -14.0F),
                    rot(1.4F, -86.0F, -14.0F, 16.0F),
                    rot(2.0F, -90.0F, 0.0F, 0.0F)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 35.0F, 0.0F, 0.0F),
                    rot(0.8F, -32.0F, 0.0F, 28.0F),
                    rot(2.0F, -48.0F, 0.0F, -24.0F)))
            .build();
}

