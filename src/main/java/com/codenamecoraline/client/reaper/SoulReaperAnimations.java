package com.codenamecoraline.client.reaper;

import com.codenamecoraline.entity.reaper.SoulReaper;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Keyframe animations for {@link SoulReaperModel}.
 * Spec: {@code docs/design/soul_reaper_animation.md} §§4–9.
 */
public final class SoulReaperAnimations {
    private SoulReaperAnimations() {}

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

    /** Awakening part 1 (8.0 s): rises from the ruptured Omen Hare shadow and materializes the Great Scythe. */
    public static final AnimationDefinition AWAKEN_ERUPTION = AnimationDefinition.Builder.withLength(8.0F)
            .addAnimation("reaper_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    pos(0.0F, 0.0F, -28.0F, 0.0F),
                    pos(2.5F, 0.0F, -6.0F, 0.0F),
                    pos(5.5F, 0.0F, 4.0F, 0.0F),
                    pos(8.0F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 48.0F, 0.0F, 0.0F),
                    rot(3.0F, 22.0F, 0.0F, 0.0F),
                    rot(5.8F, -24.0F, 0.0F, 0.0F),
                    rot(8.0F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 42.0F, 0.0F, 38.0F),
                    rot(3.2F, 18.0F, 0.0F, -18.0F),
                    rot(5.8F, -28.0F, 0.0F, 0.0F),
                    rot(8.0F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("jaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(4.5F, 8.0F, 0.0F, 0.0F),
                    rot(6.0F, 34.0F, 0.0F, 0.0F),
                    rot(8.0F, 4.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 65.0F, 0.0F, -30.0F),
                    rot(4.0F, 15.0F, 0.0F, -10.0F),
                    rot(6.2F, -55.0F, 0.0F, 24.0F),
                    rot(8.0F, -35.0F, 0.0F, 18.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 35.0F, 0.0F, 15.0F),
                    rot(5.8F, -75.0F, -25.0F, -35.0F),
                    rot(8.0F, -45.0F, 10.0F, -15.0F)))
            .addAnimation("right_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 35.0F, 0.0F, -15.0F),
                    rot(5.8F, -75.0F, 25.0F, 35.0F),
                    rot(8.0F, -40.0F, -10.0F, 15.0F)))
            .build();

    /** Awakening part 2 (2.0 s): slams the scythe shaft into the ground, unleashing the blizzard wave. */
    public static final AnimationDefinition AWAKEN_SLAM = AnimationDefinition.Builder.withLength(2.0F)
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, -24.0F, 0.0F, 0.0F),
                    rot(0.45F, 26.0F, 0.0F, 0.0F),
                    rot(1.1F, 14.0F, 0.0F, 0.0F),
                    rot(2.0F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, -45.0F, 0.0F, -18.0F),
                    rot(0.45F, 18.0F, 0.0F, -20.0F),
                    rot(1.2F, 12.0F, 0.0F, -12.0F),
                    rot(2.0F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    pos(0.0F, 0.0F, 6.0F, 0.0F),
                    pos(0.45F, 0.0F, -6.0F, -2.0F),
                    pos(1.2F, 0.0F, -4.0F, -1.0F),
                    pos(2.0F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, -55.0F, 0.0F, -18.0F),
                    rot(0.45F, 18.0F, 0.0F, -8.0F),
                    rot(2.0F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("right_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, -50.0F, 0.0F, 18.0F),
                    rot(0.45F, 22.0F, 0.0F, 8.0F),
                    rot(2.0F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** Idle 1 (4.0 s): Vigil breathing and subtle scythe hover. */
    public static final AnimationDefinition IDLE_1 = AnimationDefinition.Builder.withLength(4.0F).looping()
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(2.0F, -3.5F, 0.0F, 0.0F),
                    rot(4.0F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(2.0F, -3.0F, 2.0F, 2.5F),
                    rot(4.0F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** Idle 2 (3.0 s): Inspects spectral claw while holding scythe upright. */
    public static final AnimationDefinition IDLE_2 = AnimationDefinition.Builder.withLength(3.0F)
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(1.2F, 16.0F, 22.0F, -8.0F),
                    rot(2.2F, 12.0F, 18.0F, -6.0F),
                    rot(3.0F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(1.1F, -38.0F, -18.0F, -12.0F),
                    rot(2.1F, -34.0F, -12.0F, -8.0F),
                    rot(3.0F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(1.2F, -32.0F, 28.0F, 0.0F),
                    rot(3.0F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** Idle 3 (3.5 s): Shakes frost from cowl and mantle. */
    public static final AnimationDefinition IDLE_3 = AnimationDefinition.Builder.withLength(3.5F)
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.6F, 8.0F, -14.0F, -6.0F),
                    rot(1.1F, 6.0F, 16.0F, 6.0F),
                    rot(1.6F, 4.0F, -12.0F, -4.0F),
                    rot(2.4F, -4.0F, 0.0F, 0.0F),
                    rot(3.5F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.7F, 10.0F, 18.0F, 12.0F),
                    rot(1.2F, 8.0F, -20.0F, -12.0F),
                    rot(1.8F, 6.0F, 14.0F, 8.0F),
                    rot(3.5F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** O1: Whirlwind of the Scythe (3.5 s / 70 ticks) — 0.9 s counter-windup, 2.0 s 1080 deg spin, 0.6 s brake. */
    public static final AnimationDefinition O1_WHIRLWIND = AnimationDefinition.Builder.withLength(3.5F)
            .addAnimation("reaper_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.9F, 6.0F, -55.0F, -8.0F),
                    rot(1.55F, -8.0F, 360.0F, 6.0F),
                    rot(2.20F, -8.0F, 720.0F, 6.0F),
                    rot(2.90F, -4.0F, 1080.0F, 0.0F),
                    rot(3.20F, 6.0F, 1098.0F, 4.0F),
                    rot(3.50F, 0.0F, 1080.0F, 0.0F)))
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.9F, 18.0F, -32.0F, -12.0F),
                    rot(1.2F, -12.0F, 18.0F, 8.0F),
                    rot(2.9F, -10.0F, 12.0F, 6.0F),
                    rot(3.5F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.9F, 18.0F, -32.0F, -26.0F),
                    rot(1.15F, 52.0F, 18.0F, 34.0F),
                    rot(2.85F, 52.0F, 18.0F, 34.0F),
                    rot(3.25F, 14.0F, 16.0F, 8.0F),
                    rot(3.5F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.9F, 18.0F, -35.0F, -22.0F),
                    rot(1.15F, -38.0F, 24.0F, -42.0F),
                    rot(2.85F, -38.0F, 24.0F, -42.0F),
                    rot(3.5F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("right_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.9F, -24.0F, -35.0F, 18.0F),
                    rot(1.15F, -42.0F, -24.0F, 48.0F),
                    rot(2.85F, -42.0F, -24.0F, 48.0F),
                    rot(3.5F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("front_left_strip_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.9F, 15.0F, 0.0F, -12.0F),
                    rot(1.3F, -62.0F, 0.0F, 28.0F),
                    rot(2.85F, -62.0F, 0.0F, 28.0F),
                    rot(3.5F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("front_right_strip_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.9F, 15.0F, 0.0F, -12.0F),
                    rot(1.3F, -62.0F, 0.0F, -28.0F),
                    rot(2.85F, -62.0F, 0.0F, -28.0F),
                    rot(3.5F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("back_center_strip_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(1.3F, 68.0F, 0.0F, 0.0F),
                    rot(2.85F, 68.0F, 0.0F, 0.0F),
                    rot(3.5F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** O2: Blink Strike (1.75 s / 35 ticks) — vanish crouch, overhead materialization behind target, diagonal cleave. */
    public static final AnimationDefinition O2_BLINK_STRIKE = AnimationDefinition.Builder.withLength(1.75F)
            .addAnimation("reaper_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    pos(0.0F, 0.0F, 0.0F, 0.0F),
                    pos(0.35F, 0.0F, -8.0F, 4.0F),
                    pos(0.75F, 0.0F, 6.0F, -2.0F),
                    pos(1.10F, 0.0F, -3.0F, -5.0F),
                    pos(1.75F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.35F, 28.0F, -24.0F, 0.0F),
                    rot(0.75F, -26.0F, -38.0F, -14.0F),
                    rot(1.10F, 34.0F, 42.0F, 16.0F),
                    rot(1.45F, 18.0F, 24.0F, 8.0F),
                    rot(1.75F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.35F, 24.0F, -24.0F, -18.0F),
                    rot(0.75F, -48.0F, -28.0F, -28.0F),
                    rot(1.10F, 44.0F, 32.0F, 16.0F),
                    rot(1.45F, 22.0F, 18.0F, 8.0F),
                    rot(1.75F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.75F, -64.0F, -22.0F, -28.0F),
                    rot(1.10F, 38.0F, 28.0F, 18.0F),
                    rot(1.75F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("right_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.75F, -58.0F, -18.0F, 24.0F),
                    rot(1.10F, 42.0F, 24.0F, -16.0F),
                    rot(1.75F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** O3: Harvest of Wandering Souls (4.5 s / 90 ticks) — plants scythe totem, opens ribcage & jaw to siphon life. */
    public static final AnimationDefinition O3_SOUL_HARVEST = AnimationDefinition.Builder.withLength(4.5F)
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.9F, 14.0F, 0.0F, 0.0F),
                    rot(1.6F, -22.0F, 0.0F, 0.0F),
                    rot(3.5F, -26.0F, 0.0F, 0.0F),
                    rot(4.5F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("rib_flaps_left", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(1.4F, 0.0F, -48.0F, -18.0F),
                    rot(3.5F, 0.0F, -52.0F, -22.0F),
                    rot(4.5F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("rib_flaps_right", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(1.4F, 0.0F, 48.0F, 18.0F),
                    rot(3.5F, 0.0F, 52.0F, 22.0F),
                    rot(4.5F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("jaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(1.4F, 36.0F, 0.0F, 0.0F),
                    rot(3.5F, 38.0F, 0.0F, 0.0F),
                    rot(4.5F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.9F, -8.0F, 0.0F, -18.0F),
                    rot(3.6F, -8.0F, 0.0F, -18.0F),
                    rot(4.5F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    pos(0.0F, 0.0F, 0.0F, 0.0F),
                    pos(0.9F, 5.0F, -3.0F, -1.0F),
                    pos(3.6F, 5.0F, -3.0F, -1.0F),
                    pos(4.5F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(1.4F, -68.0F, -28.0F, -32.0F),
                    rot(2.5F, -78.0F, -12.0F, -24.0F),
                    rot(3.5F, -64.0F, -24.0F, -28.0F),
                    rot(4.5F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** O4: Frost Wave (2.25 s / 45 ticks) — two-handed overhead lift, ground-splitting downward slam, ice extraction. */
    public static final AnimationDefinition O4_FROST_WAVE = AnimationDefinition.Builder.withLength(2.25F)
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.70F, -24.0F, 0.0F, 0.0F),
                    rot(1.05F, 34.0F, 0.0F, 0.0F),
                    rot(1.55F, 24.0F, 0.0F, 0.0F),
                    rot(2.25F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    pos(0.0F, 0.0F, 0.0F, 0.0F),
                    pos(0.70F, 0.0F, 8.0F, 2.0F),
                    pos(1.05F, 0.0F, -4.0F, -3.0F),
                    pos(1.55F, 0.0F, -3.0F, -2.0F),
                    pos(2.25F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.70F, -42.0F, 32.0F, -18.0F),
                    rot(1.05F, 48.0F, 42.0F, -16.0F),
                    rot(1.55F, 32.0F, 28.0F, -12.0F),
                    rot(2.25F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.70F, -72.0F, 14.0F, -12.0F),
                    rot(1.05F, 36.0F, 10.0F, -6.0F),
                    rot(2.25F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("right_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.70F, -68.0F, -14.0F, 12.0F),
                    rot(1.05F, 40.0F, -10.0F, 6.0F),
                    rot(2.25F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** O5: Reaping Dash (2.0 s / 40 ticks) — predator crouch, 14-block dash with 180 deg wide cleave, drifting brake. */
    public static final AnimationDefinition O5_REAPING_DASH = AnimationDefinition.Builder.withLength(2.0F)
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.55F, 34.0F, -48.0F, -14.0F),
                    rot(1.10F, 28.0F, 62.0F, 18.0F),
                    rot(1.55F, 16.0F, 38.0F, 10.0F),
                    rot(2.0F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.55F, 42.0F, -68.0F, -48.0F),
                    rot(1.10F, 52.0F, 84.0F, 62.0F),
                    rot(1.55F, 24.0F, 42.0F, 28.0F),
                    rot(2.0F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.55F, 22.0F, -42.0F, -24.0F),
                    rot(1.10F, -32.0F, 48.0F, 18.0F),
                    rot(2.0F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("right_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.55F, -18.0F, -42.0F, 22.0F),
                    rot(1.10F, 28.0F, 48.0F, -24.0F),
                    rot(2.0F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** F1: Rising Crescent (1.6 s / 32 ticks) — low ground drag into diagonal rising uppercut slash. */
    public static final AnimationDefinition F1_RISING_CRESCENT = AnimationDefinition.Builder.withLength(1.6F)
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.50F, 28.0F, -36.0F, -16.0F),
                    rot(0.80F, -24.0F, 42.0F, 18.0F),
                    rot(1.20F, -14.0F, 26.0F, 10.0F),
                    rot(1.60F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.50F, 54.0F, -48.0F, -44.0F),
                    rot(0.80F, -68.0F, 52.0F, 48.0F),
                    rot(1.20F, -38.0F, 28.0F, 24.0F),
                    rot(1.60F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.50F, 32.0F, -28.0F, -18.0F),
                    rot(0.80F, -58.0F, 32.0F, 16.0F),
                    rot(1.60F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("right_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.50F, 28.0F, -24.0F, 14.0F),
                    rot(0.80F, -52.0F, 28.0F, -18.0F),
                    rot(1.60F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** F2: Haft Thrust (1.2 s / 24 ticks) — quick horizontal shaft/pommel check to push back huggers. */
    public static final AnimationDefinition F2_HAFT_THRUST = AnimationDefinition.Builder.withLength(1.2F)
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.35F, -12.0F, -18.0F, 0.0F),
                    rot(0.55F, 22.0F, 14.0F, 0.0F),
                    rot(1.20F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    pos(0.0F, 0.0F, 0.0F, 0.0F),
                    pos(0.35F, 0.0F, 2.0F, 5.0F),
                    pos(0.55F, 0.0F, 0.0F, -9.0F),
                    pos(1.20F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.35F, -18.0F, 0.0F, -28.0F),
                    rot(0.55F, 26.0F, 0.0F, -34.0F),
                    rot(1.20F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.35F, 24.0F, 0.0F, -12.0F),
                    rot(0.55F, -48.0F, 0.0F, -8.0F),
                    rot(1.20F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("right_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.35F, 24.0F, 0.0F, 12.0F),
                    rot(0.55F, -48.0F, 0.0F, 8.0F),
                    rot(1.20F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** F3: Cross Reap (2.1 s / 42 ticks) — X-pattern double diagonal cleave across the front cone. */
    public static final AnimationDefinition F3_CROSS_REAP = AnimationDefinition.Builder.withLength(2.1F)
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.55F, -16.0F, -42.0F, -12.0F),
                    rot(0.90F, 24.0F, 46.0F, 16.0F),
                    rot(1.30F, 22.0F, -48.0F, -16.0F),
                    rot(2.10F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.55F, -48.0F, -54.0F, -38.0F),
                    rot(0.90F, 52.0F, 64.0F, 54.0F),
                    rot(1.30F, 48.0F, -68.0F, -52.0F),
                    rot(2.10F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.55F, -42.0F, -34.0F, -22.0F),
                    rot(0.90F, 34.0F, 38.0F, 18.0F),
                    rot(1.30F, 28.0F, -42.0F, -24.0F),
                    rot(2.10F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("right_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.55F, -38.0F, -32.0F, 18.0F),
                    rot(0.90F, 38.0F, 36.0F, -22.0F),
                    rot(1.30F, 32.0F, -38.0F, 24.0F),
                    rot(2.10F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** F4: Gravedigger Slam (2.2 s / 44 ticks) — rises vertically then buries the scythe tip into the earth. */
    public static final AnimationDefinition F4_GRAVEDIGGER_SLAM = AnimationDefinition.Builder.withLength(2.2F)
            .addAnimation("reaper_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    pos(0.0F, 0.0F, 0.0F, 0.0F),
                    pos(0.75F, 0.0F, 8.0F, 0.0F),
                    pos(1.10F, 0.0F, -5.0F, -3.0F),
                    pos(1.65F, 0.0F, -4.0F, -2.0F),
                    pos(2.20F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.75F, -28.0F, 0.0F, 0.0F),
                    rot(1.10F, 38.0F, 0.0F, 0.0F),
                    rot(1.65F, 28.0F, 0.0F, 0.0F),
                    rot(2.20F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    pos(0.0F, 0.0F, 0.0F, 0.0F),
                    pos(0.75F, 0.0F, 9.0F, 2.0F),
                    pos(1.10F, 0.0F, -4.0F, -3.0F),
                    pos(1.65F, 0.0F, -3.0F, -2.0F),
                    pos(2.20F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.75F, -46.0F, 34.0F, -18.0F),
                    rot(1.10F, 52.0F, 44.0F, -16.0F),
                    rot(1.65F, 36.0F, 30.0F, -12.0F),
                    rot(2.20F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.75F, -76.0F, 16.0F, -14.0F),
                    rot(1.10F, 44.0F, 12.0F, -8.0F),
                    rot(2.20F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("right_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.75F, -72.0F, -16.0F, 14.0F),
                    rot(1.10F, 48.0F, -12.0F, 8.0F),
                    rot(2.20F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** F5: Low Reaping Sweep (1.5 s / 30 ticks) — drops pelvis low for a 240 deg ankle-height harvest sweep. */
    public static final AnimationDefinition F5_LOW_SWEEP = AnimationDefinition.Builder.withLength(1.5F)
            .addAnimation("reaper_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    pos(0.0F, 0.0F, 0.0F, 0.0F),
                    pos(0.45F, 0.0F, -7.0F, 0.0F),
                    pos(0.80F, 0.0F, -8.0F, -2.0F),
                    pos(1.50F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.45F, 28.0F, 52.0F, 12.0F),
                    rot(0.80F, 32.0F, -68.0F, -16.0F),
                    rot(1.50F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.45F, 64.0F, 72.0F, 58.0F),
                    rot(0.80F, 68.0F, -88.0F, -62.0F),
                    rot(1.50F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.45F, 34.0F, 44.0F, 18.0F),
                    rot(0.80F, 38.0F, -52.0F, -24.0F),
                    rot(1.50F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("right_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.45F, 38.0F, 42.0F, -14.0F),
                    rot(0.80F, 34.0F, -48.0F, 22.0F),
                    rot(1.50F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** F6: Threefold Harvest (2.9 s / 58 ticks) — 3-step combo: horizontal cleave -> rising hook -> spinning slam. */
    public static final AnimationDefinition F6_THREEFOLD_HARVEST = AnimationDefinition.Builder.withLength(2.9F)
            .addAnimation("reaper_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(1.40F, 0.0F, 0.0F, 0.0F),
                    rot(2.00F, 0.0F, 360.0F, 0.0F),
                    rot(2.90F, 0.0F, 360.0F, 0.0F)))
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.65F, 14.0F, -48.0F, -10.0F),
                    rot(1.00F, 18.0F, 52.0F, 14.0F),
                    rot(1.45F, -26.0F, -38.0F, -14.0F),
                    rot(2.00F, 42.0F, 0.0F, 0.0F),
                    rot(2.90F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.65F, 38.0F, -64.0F, -42.0F),
                    rot(1.00F, 44.0F, 68.0F, 54.0F),
                    rot(1.45F, -64.0F, -48.0F, -38.0F),
                    rot(2.00F, 72.0F, 0.0F, 24.0F),
                    rot(2.90F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.65F, 24.0F, -38.0F, -20.0F),
                    rot(1.00F, 28.0F, 42.0F, 18.0F),
                    rot(1.45F, -58.0F, -32.0F, -18.0F),
                    rot(2.00F, 48.0F, 12.0F, -8.0F),
                    rot(2.90F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("right_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.65F, 22.0F, -36.0F, 18.0F),
                    rot(1.00F, 32.0F, 40.0F, -20.0F),
                    rot(1.45F, -54.0F, -28.0F, 18.0F),
                    rot(2.00F, 52.0F, -12.0F, 8.0F),
                    rot(2.90F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** E1: Sidestep Glide (0.6 s / 12 ticks) — sharp lateral banking dodge with opposing shroud flare. */
    public static final AnimationDefinition E1_SIDESTEP_GLIDE = AnimationDefinition.Builder.withLength(0.6F)
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.25F, 10.0F, -18.0F, 24.0F),
                    rot(0.60F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.25F, 14.0F, -22.0F, 28.0F),
                    rot(0.60F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("side_left_strip_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.30F, 0.0F, 0.0F, 46.0F),
                    rot(0.60F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** E2: Phantom Retreat (0.75 s / 15 ticks) — backward dash with scythe held across chest as guard bar. */
    public static final AnimationDefinition E2_PHANTOM_RETREAT = AnimationDefinition.Builder.withLength(0.75F)
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.30F, 24.0F, 0.0F, 0.0F),
                    rot(0.75F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.30F, -18.0F, 0.0F, -42.0F),
                    rot(0.75F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("front_left_strip_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.35F, -48.0F, 0.0F, 0.0F),
                    rot(0.75F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("front_right_strip_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.35F, -48.0F, 0.0F, 0.0F),
                    rot(0.75F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** E3: Snow Dissolve (0.9 s / 18 ticks) — sinks into snow vortex and rises reformed. */
    public static final AnimationDefinition E3_SNOW_DISSOLVE = AnimationDefinition.Builder.withLength(0.9F)
            .addAnimation("reaper_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    pos(0.0F, 0.0F, 0.0F, 0.0F),
                    pos(0.35F, 0.0F, -24.0F, 0.0F),
                    pos(0.55F, 0.0F, -24.0F, 0.0F),
                    pos(0.90F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("reaper_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.45F, 0.0F, 180.0F, 0.0F),
                    rot(0.90F, 0.0F, 360.0F, 0.0F)))
            .build();

    /** E4: Scythe Vault (0.9 s / 18 ticks) — plants scythe pole and vaults backward over melee attackers. */
    public static final AnimationDefinition E4_SCYTHE_VAULT = AnimationDefinition.Builder.withLength(0.9F)
            .addAnimation("reaper_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    pos(0.0F, 0.0F, 0.0F, 0.0F),
                    pos(0.40F, 0.0F, 10.0F, 4.0F),
                    pos(0.90F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.40F, -28.0F, 0.0F, 0.0F),
                    rot(0.90F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.40F, 38.0F, 0.0F, 24.0F),
                    rot(0.90F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** P1: Beckon of the End (2.0 s / 40 ticks) — plants scythe with right hand and curls left index claw twice. */
    public static final AnimationDefinition P1_BECKON = AnimationDefinition.Builder.withLength(2.0F)
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.45F, 0.0F, 0.0F, 24.0F),
                    rot(1.60F, 0.0F, 0.0F, 24.0F),
                    rot(2.00F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    pos(0.0F, 0.0F, 0.0F, 0.0F),
                    pos(0.45F, 7.0F, -4.0F, 0.0F),
                    pos(1.60F, 7.0F, -4.0F, 0.0F),
                    pos(2.00F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.45F, -42.0F, -24.0F, -12.0F),
                    rot(1.60F, -42.0F, -24.0F, -12.0F),
                    rot(2.00F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.45F, 28.0F, 0.0F, 160.0F),
                    rot(1.60F, 28.0F, 0.0F, 160.0F),
                    rot(2.00F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_index_group", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.75F, -64.0F, 0.0F, 0.0F),
                    rot(1.05F, -12.0F, 0.0F, 0.0F),
                    rot(1.35F, -68.0F, 0.0F, 0.0F),
                    rot(2.00F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** P2: Funeral Bow (2.5 s / 50 ticks) — mocking aristocratic bow with scythe swept out and left hand on brooch. */
    public static final AnimationDefinition P2_FUNERAL_BOW = AnimationDefinition.Builder.withLength(2.5F)
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.85F, 38.0F, 0.0F, 0.0F),
                    rot(1.75F, 38.0F, 0.0F, 0.0F),
                    rot(2.50F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.85F, -24.0F, 0.0F, 8.0F),
                    rot(1.75F, -24.0F, 0.0F, 8.0F),
                    rot(2.50F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.85F, -16.0F, 24.0F, 28.0F),
                    rot(1.75F, -16.0F, 24.0F, 28.0F),
                    rot(2.50F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.85F, -38.0F, -24.0F, -36.0F),
                    rot(1.75F, -38.0F, -24.0F, -36.0F),
                    rot(2.50F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** P3: Hourglass Count (2.25 s / 45 ticks) — ticks off skeletal fingers one by one. */
    public static final AnimationDefinition P3_HOURGLASS_COUNT = AnimationDefinition.Builder.withLength(2.25F)
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.45F, -52.0F, -18.0F, -14.0F),
                    rot(1.80F, -52.0F, -18.0F, -14.0F),
                    rot(2.25F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_index_group", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.70F, 58.0F, 0.0F, 0.0F),
                    rot(1.80F, 58.0F, 0.0F, 0.0F),
                    rot(2.25F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_middle_ring_group", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(1.10F, 58.0F, 0.0F, 0.0F),
                    rot(1.80F, 58.0F, 0.0F, 0.0F),
                    rot(2.25F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_pinky_claw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(1.50F, 58.0F, 0.0F, 0.0F),
                    rot(1.80F, 58.0F, 0.0F, 0.0F),
                    rot(2.25F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** P4: Blade Honing (2.5 s / 50 ticks) — scrapes left skeletal claws down the frost crescent blade. */
    public static final AnimationDefinition P4_BLADE_HONING = AnimationDefinition.Builder.withLength(2.5F)
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.55F, 28.0F, -18.0F, -24.0F),
                    rot(1.90F, 28.0F, -18.0F, -24.0F),
                    rot(2.50F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.60F, -64.0F, -12.0F, -18.0F),
                    rot(1.80F, -48.0F, 34.0F, 14.0F),
                    rot(2.50F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** P5: Hare Tilt Echo (2.0 s / 40 ticks) — eerie 42 deg head tilt recalling the Omen Hare's stare. */
    public static final AnimationDefinition P5_HARE_TILT_ECHO = AnimationDefinition.Builder.withLength(2.0F)
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.30F, 8.0F, 0.0F, 42.0F),
                    rot(1.45F, 10.0F, 0.0F, 44.0F),
                    rot(2.00F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("jaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.40F, 18.0F, 0.0F, 0.0F),
                    rot(1.45F, 20.0F, 0.0F, 0.0F),
                    rot(2.00F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** A1: Vial Silence (1.6 s / 32 ticks) — extends left claw and crushes fist to nullify potion buff. */
    public static final AnimationDefinition A1_VIAL_SILENCE = AnimationDefinition.Builder.withLength(1.6F)
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.45F, -54.0F, -24.0F, -14.0F),
                    rot(0.80F, -48.0F, -18.0F, -10.0F),
                    rot(1.60F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_index_group", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.45F, -24.0F, 0.0F, 0.0F),
                    rot(0.75F, 72.0F, 0.0F, 0.0F),
                    rot(1.60F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_middle_ring_group", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.45F, -24.0F, 0.0F, 0.0F),
                    rot(0.75F, 72.0F, 0.0F, 0.0F),
                    rot(1.60F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** A2: Golden Apple Toll (1.8 s / 36 ticks) — points scythe blade and yanks soul toll inward. */
    public static final AnimationDefinition A2_APPLE_TOLL = AnimationDefinition.Builder.withLength(1.8F)
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.50F, 14.0F, 24.0F, 0.0F),
                    rot(0.95F, -18.0F, -28.0F, 0.0F),
                    rot(1.80F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.50F, 48.0F, 22.0F, 18.0F),
                    rot(0.95F, -34.0F, -38.0F, -24.0F),
                    rot(1.80F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** A3: Greater Toll (2.2 s / 44 ticks) — two-handed ritual inversion stripping Enchanted Golden Apple buffs. */
    public static final AnimationDefinition A3_GREATER_TOLL = AnimationDefinition.Builder.withLength(2.2F)
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.65F, -28.0F, 0.0F, 0.0F),
                    rot(1.25F, 32.0F, 0.0F, 0.0F),
                    rot(2.20F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("rib_flaps_left", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.95F, 0.0F, -45.0F, -16.0F),
                    rot(2.20F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("rib_flaps_right", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.95F, 0.0F, 45.0F, 16.0F),
                    rot(2.20F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.65F, -64.0F, 0.0F, 24.0F),
                    rot(1.25F, 42.0F, 0.0F, -32.0F),
                    rot(2.20F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** A4: Flick Snuff (0.5 s / 10 ticks) — L3 additive left-hand flick while moving or fighting. */
    public static final AnimationDefinition A4_FLICK_SNUFF = AnimationDefinition.Builder.withLength(0.5F)
            .addAnimation("left_upper_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.18F, -44.0F, -22.0F, -16.0F),
                    rot(0.50F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("left_hand", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.18F, -38.0F, 0.0F, -24.0F),
                    rot(0.50F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** A5: Shield Tithe (1.4 s / 28 ticks) — hooks scythe crescent behind a raised shield and wrenches it aside. */
    public static final AnimationDefinition A5_SHIELD_TITHE = AnimationDefinition.Builder.withLength(1.4F)
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.40F, 18.0F, 34.0F, 0.0F),
                    rot(0.75F, -14.0F, -48.0F, -14.0F),
                    rot(1.40F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.40F, 42.0F, 44.0F, 28.0F),
                    rot(0.75F, -28.0F, -64.0F, -42.0F),
                    rot(1.40F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** A6: Arrow Reversal (1.2 s / 24 ticks) — spins the scythe in a frontward frost wheel to deflect projectiles. */
    public static final AnimationDefinition A6_ARROW_REVERSAL = AnimationDefinition.Builder.withLength(1.2F)
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.35F, 0.0F, 0.0F, 180.0F),
                    rot(0.75F, 0.0F, 0.0F, 360.0F),
                    rot(1.20F, 0.0F, 0.0F, 360.0F)))
            .build();

    /** HURT (0.35 s / 7 ticks) — L3 additive torso recoil and rib-flap shudder. */
    public static final AnimationDefinition HURT = AnimationDefinition.Builder.withLength(0.35F)
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.10F, -12.0F, 8.0F, -6.0F),
                    rot(0.35F, 0.0F, 0.0F, 0.0F)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(0.10F, -16.0F, -10.0F, 8.0F),
                    rot(0.35F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** DEATH (4.5 s / 90 ticks) — drops to knees, plants scythe, soul core ruptures, collapses into empty shroud. */
    public static final AnimationDefinition DEATH = AnimationDefinition.Builder.withLength(4.5F)
            .addAnimation("reaper_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    pos(0.0F, 0.0F, 0.0F, 0.0F),
                    pos(1.5F, 0.0F, -10.0F, 0.0F),
                    pos(3.2F, 0.0F, -14.0F, 0.0F),
                    pos(4.5F, 0.0F, -24.0F, 0.0F)))
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(1.5F, 24.0F, 0.0F, 0.0F),
                    rot(2.8F, -38.0F, 0.0F, 0.0F),
                    rot(4.5F, 64.0F, 0.0F, 0.0F)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(1.5F, 28.0F, 0.0F, 12.0F),
                    rot(2.8F, -44.0F, 0.0F, -8.0F),
                    rot(4.5F, 48.0F, 0.0F, 18.0F)))
            .addAnimation("rib_flaps_left", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(2.8F, 0.0F, -58.0F, -24.0F),
                    rot(4.5F, 0.0F, -12.0F, 0.0F)))
            .addAnimation("rib_flaps_right", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(2.8F, 0.0F, 58.0F, 24.0F),
                    rot(4.5F, 0.0F, 12.0F, 0.0F)))
            .addAnimation("scythe_root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    rot(0.0F, 0.0F, 0.0F, 0.0F),
                    rot(1.5F, -14.0F, 0.0F, 24.0F),
                    rot(4.5F, 78.0F, 18.0F, 64.0F)))
            .build();

    public static AnimationDefinition forAction(SoulReaper.ActionId action) {
        return switch (action) {
            case AWAKEN_ERUPTION -> AWAKEN_ERUPTION;
            case AWAKEN_SLAM -> AWAKEN_SLAM;
            case IDLE_1 -> IDLE_1;
            case IDLE_2 -> IDLE_2;
            case IDLE_3 -> IDLE_3;
            case O1_WHIRLWIND -> O1_WHIRLWIND;
            case O2_BLINK_STRIKE -> O2_BLINK_STRIKE;
            case O3_SOUL_HARVEST -> O3_SOUL_HARVEST;
            case O4_FROST_WAVE -> O4_FROST_WAVE;
            case O5_REAPING_DASH -> O5_REAPING_DASH;
            case F1_RISING_CRESCENT -> F1_RISING_CRESCENT;
            case F2_HAFT_THRUST -> F2_HAFT_THRUST;
            case F3_CROSS_REAP -> F3_CROSS_REAP;
            case F4_GRAVEDIGGER_SLAM -> F4_GRAVEDIGGER_SLAM;
            case F5_LOW_SWEEP -> F5_LOW_SWEEP;
            case F6_THREEFOLD_HARVEST -> F6_THREEFOLD_HARVEST;
            case E1_SIDESTEP_GLIDE -> E1_SIDESTEP_GLIDE;
            case E2_PHANTOM_RETREAT -> E2_PHANTOM_RETREAT;
            case E3_SNOW_DISSOLVE -> E3_SNOW_DISSOLVE;
            case E4_SCYTHE_VAULT -> E4_SCYTHE_VAULT;
            case P1_BECKON -> P1_BECKON;
            case P2_FUNERAL_BOW -> P2_FUNERAL_BOW;
            case P3_HOURGLASS_COUNT -> P3_HOURGLASS_COUNT;
            case P4_BLADE_HONING -> P4_BLADE_HONING;
            case P5_HARE_TILT_ECHO -> P5_HARE_TILT_ECHO;
            case A1_VIAL_SILENCE -> A1_VIAL_SILENCE;
            case A2_APPLE_TOLL -> A2_APPLE_TOLL;
            case A3_GREATER_TOLL -> A3_GREATER_TOLL;
            case A5_SHIELD_TITHE -> A5_SHIELD_TITHE;
            case A6_ARROW_REVERSAL -> A6_ARROW_REVERSAL;
            case DEATH -> DEATH;
            case NONE -> IDLE_1;
        };
    }
}
