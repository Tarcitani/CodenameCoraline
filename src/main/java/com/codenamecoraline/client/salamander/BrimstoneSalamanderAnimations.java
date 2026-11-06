package com.codenamecoraline.client.salamander;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Complete set of 17 keyframe animations for the Brimstone Salamander (Section 1.6 of docs/design/brimstone_salamander.md):
 * - animation.brimstone_salamander.idle_breathe (40 ticks / 2.00 s, looping)
 * - animation.brimstone_salamander.idle_throat_puff (30 ticks / 1.50 s)
 * - animation.brimstone_salamander.idle_tail_sway (35 ticks / 1.75 s)
 * - animation.brimstone_salamander.idle_sniff (25 ticks / 1.25 s)
 * - animation.brimstone_salamander.walk (16 ticks / 0.80 s, looping)
 * - animation.brimstone_salamander.swim_lava (14 ticks / 0.70 s, looping)
 * - animation.brimstone_salamander.snap_bite_prep (8 ticks / 0.40 s)
 * - animation.brimstone_salamander.snap_bite_exec (4 ticks / 0.20 s, impact tick 2)
 * - animation.brimstone_salamander.snap_bite_recovery (12 ticks / 0.60 s)
 * - animation.brimstone_salamander.sulfur_spit_prep (16 ticks / 0.80 s)
 * - animation.brimstone_salamander.sulfur_spit_exec (6 ticks / 0.30 s, impact tick 2)
 * - animation.brimstone_salamander.sulfur_spit_recovery (18 ticks / 0.90 s)
 * - animation.brimstone_salamander.tail_whip_prep (10 ticks / 0.50 s)
 * - animation.brimstone_salamander.tail_whip_exec (5 ticks / 0.25 s, impact tick 3)
 * - animation.brimstone_salamander.tail_whip_recovery (14 ticks / 0.70 s)
 * - animation.brimstone_salamander.hurt (6 ticks / 0.30 s)
 * - animation.brimstone_salamander.death (24 ticks / 1.20 s)
 */
public final class BrimstoneSalamanderAnimations {
    private static final AnimationChannel.Interpolation LINEAR = AnimationChannel.Interpolations.LINEAR;
    private static final AnimationChannel.Interpolation SMOOTH = AnimationChannel.Interpolations.CATMULLROM;

    /** 1. idle_breathe (40 ticks / 2.00 s, looping): slow thermal throat pulse and subtle tail tip sway. */
    public static final AnimationDefinition IDLE_BREATHE = AnimationDefinition.Builder.withLength(2.00F).looping()
            .addAnimation("body", pos(SMOOTH, 0.00F, 0, 0, 0, 1.00F, 0, -0.3F, 0, 2.00F, 0, 0, 0))
            .addAnimation("throat_sac", scale(SMOOTH, 0.00F, 1.0F, 1.0F, 1.0F, 1.00F, 1.18F, 1.28F, 1.15F, 2.00F, 1.0F, 1.0F, 1.0F))
            .addAnimation("throat_sac", rot(SMOOTH, 0.00F, 0, 0, 0, 1.00F, 8.0F, 0, 0, 2.00F, 0, 0, 0))
            .addAnimation("tail_base", rot(SMOOTH, 0.00F, 0, 0, 0, 0.50F, 0, 2.5F, 0, 1.50F, 0, -2.5F, 0, 2.00F, 0, 0, 0))
            .addAnimation("tail_tip", rot(SMOOTH, 0.00F, 0, 0, 0, 0.50F, 0, -3.5F, 0, 1.50F, 0, 3.5F, 0, 2.00F, 0, 0, 0))
            .build();

    /** 2. idle_throat_puff (30 ticks / 1.50 s): raises head 15 deg, inflates bellows throat to double volume, exhales. */
    public static final AnimationDefinition IDLE_THROAT_PUFF = AnimationDefinition.Builder.withLength(1.50F)
            .addAnimation("neck", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.50F, -15, 0, 0,
                    0.90F, -12, 0, 0,
                    1.50F, 0, 0, 0))
            .addAnimation("head", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.50F, -10, 0, 0,
                    0.90F, -6, 0, 0,
                    1.50F, 0, 0, 0))
            .addAnimation("throat_sac", scale(SMOOTH,
                    0.00F, 1.0F, 1.0F, 1.0F,
                    0.55F, 1.45F, 1.85F, 1.40F,
                    0.85F, 1.52F, 1.95F, 1.45F,
                    1.15F, 0.92F, 0.85F, 0.92F,
                    1.50F, 1.0F, 1.0F, 1.0F))
            .addAnimation("throat_sac", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.70F, 18, 0, 0,
                    1.15F, -4, 0, 0,
                    1.50F, 0, 0, 0))
            .addAnimation("lower_jaw", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.75F, 6, 0, 0,
                    1.00F, 18, 0, 0,
                    1.35F, 0, 0, 0,
                    1.50F, 0, 0, 0))
            .build();

    /** 3. idle_tail_sway (35 ticks / 1.75 s): braces front paws and sweeps tail in a full S-wave along the ground. */
    public static final AnimationDefinition IDLE_TAIL_SWAY = AnimationDefinition.Builder.withLength(1.75F)
            .addAnimation("body", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.45F, 0, 6, 0,
                    1.20F, 0, -6, 0,
                    1.75F, 0, 0, 0))
            .addAnimation("tail_base", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.45F, 0, 24, 0,
                    1.20F, 0, -24, 0,
                    1.75F, 0, 0, 0))
            .addAnimation("tail_mid", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.30F, 0, -16, 0,
                    0.65F, 0, 28, 0,
                    1.35F, 0, -28, 0,
                    1.75F, 0, 0, 0))
            .addAnimation("tail_tip", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.40F, 0, -24, 0,
                    0.80F, 0, 32, 0,
                    1.45F, 0, -32, 0,
                    1.75F, 0, 0, 0))
            .build();

    /** 4. idle_sniff (25 ticks / 1.25 s): turns head 35 deg left, opens jaw to flick glowing forked tongue for 8 ticks. */
    public static final AnimationDefinition IDLE_SNIFF = AnimationDefinition.Builder.withLength(1.25F)
            .addAnimation("neck", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.25F, -6, 16, 0,
                    0.65F, -6, 18, 0,
                    1.00F, -2, -8, 0,
                    1.25F, 0, 0, 0))
            .addAnimation("head", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.25F, -4, 19, -8,
                    0.65F, -4, 21, -6,
                    1.00F, -2, -10, 4,
                    1.25F, 0, 0, 0))
            .addAnimation("lower_jaw", rot(LINEAR,
                    0.00F, 0, 0, 0,
                    0.20F, 0, 0, 0,
                    0.30F, 24, 0, 0,
                    0.70F, 24, 0, 0,
                    0.85F, 0, 0, 0,
                    1.25F, 0, 0, 0))
            .build();

    /** 5. walk (16 ticks / 0.80 s, looping): diagonal sprawled reptile crawl with lateral body/tail undulation. */
    public static final AnimationDefinition WALK = AnimationDefinition.Builder.withLength(0.80F).looping()
            .addAnimation("body", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.20F, 0, 8, -2,
                    0.40F, 0, 0, 0,
                    0.60F, 0, -8, 2,
                    0.80F, 0, 0, 0))
            .addAnimation("neck", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.20F, 0, -6, 0,
                    0.40F, 0, 0, 0,
                    0.60F, 0, 6, 0,
                    0.80F, 0, 0, 0))
            .addAnimation("left_front_leg", rot(LINEAR,
                    0.00F, 0, -18, 0,
                    0.20F, 0, 24, -14,
                    0.40F, 0, 24, 0,
                    0.60F, 0, -18, 0,
                    0.80F, 0, -18, 0))
            .addAnimation("right_front_leg", rot(LINEAR,
                    0.00F, 0, -18, 0,
                    0.20F, 0, 18, 0,
                    0.40F, 0, 18, 0,
                    0.60F, 0, -24, 14,
                    0.80F, 0, -18, 0))
            .addAnimation("left_hind_leg", rot(LINEAR,
                    0.00F, 0, 18, 0,
                    0.20F, 0, -18, 0,
                    0.40F, 0, -18, 0,
                    0.60F, 0, 24, -14,
                    0.80F, 0, 18, 0))
            .addAnimation("right_hind_leg", rot(LINEAR,
                    0.00F, 0, 18, 0,
                    0.20F, 0, -24, 14,
                    0.40F, 0, -24, 0,
                    0.60F, 0, 18, 0,
                    0.80F, 0, 18, 0))
            .addAnimation("tail_base", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.20F, 0, -16, 0,
                    0.40F, 0, 0, 0,
                    0.60F, 0, 16, 0,
                    0.80F, 0, 0, 0))
            .addAnimation("tail_mid", rot(SMOOTH,
                    0.00F, 0, 12, 0,
                    0.20F, 0, -22, 0,
                    0.40F, 0, -12, 0,
                    0.60F, 0, 22, 0,
                    0.80F, 0, 12, 0))
            .addAnimation("tail_tip", rot(SMOOTH,
                    0.00F, 0, 18, 0,
                    0.20F, 0, -26, 0,
                    0.40F, 0, -18, 0,
                    0.60F, 0, 26, 0,
                    0.80F, 0, 18, 0))
            .build();

    /** 6. swim_lava (14 ticks / 0.70 s, looping): limbs tucked along flanks, vigorous S-wave propulsion in lava. */
    public static final AnimationDefinition SWIM_LAVA = AnimationDefinition.Builder.withLength(0.70F).looping()
            .addAnimation("body", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.175F, 0, 12, 0,
                    0.35F, 0, 0, 0,
                    0.525F, 0, -12, 0,
                    0.70F, 0, 0, 0))
            .addAnimation("neck", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.175F, 0, -10, 0,
                    0.35F, 0, 0, 0,
                    0.525F, 0, 10, 0,
                    0.70F, 0, 0, 0))
            .addAnimation("left_front_leg", rot(SMOOTH, 0.00F, 0, -48, 12, 0.70F, 0, -48, 12))
            .addAnimation("right_front_leg", rot(SMOOTH, 0.00F, 0, 48, -12, 0.70F, 0, 48, -12))
            .addAnimation("left_hind_leg", rot(SMOOTH, 0.00F, 0, -52, 12, 0.70F, 0, -52, 12))
            .addAnimation("right_hind_leg", rot(SMOOTH, 0.00F, 0, 52, -12, 0.70F, 0, 52, -12))
            .addAnimation("tail_base", rot(SMOOTH,
                    0.00F, 0, 0, 0,
                    0.175F, 0, -24, 0,
                    0.35F, 0, 0, 0,
                    0.525F, 0, 24, 0,
                    0.70F, 0, 0, 0))
            .addAnimation("tail_mid", rot(SMOOTH,
                    0.00F, 0, 18, 0,
                    0.175F, 0, -32, 0,
                    0.35F, 0, -18, 0,
                    0.525F, 0, 32, 0,
                    0.70F, 0, 18, 0))
            .addAnimation("tail_tip", rot(SMOOTH,
                    0.00F, 0, 24, 0,
                    0.175F, 0, -38, 0,
                    0.35F, 0, -24, 0,
                    0.525F, 0, 38, 0,
                    0.70F, 0, 24, 0))
            .build();

    /** 7. snap_bite_prep (8 ticks / 0.40 s): coils neck back in S-curve, braces paws, opens lower jaw 40 deg. */
    public static final AnimationDefinition SNAP_BITE_PREP = AnimationDefinition.Builder.withLength(0.40F)
            .addAnimation("body", pos(LINEAR, 0.00F, 0, 0, 0, 0.40F, 0, 0, 1.8F))
            .addAnimation("body", rot(LINEAR, 0.00F, 0, 0, 0, 0.40F, -8, 0, 0))
            .addAnimation("neck", rot(LINEAR, 0.00F, 0, 0, 0, 0.40F, -22, 0, 0))
            .addAnimation("head", rot(LINEAR, 0.00F, 0, 0, 0, 0.40F, 14, 0, 0))
            .addAnimation("lower_jaw", rot(LINEAR, 0.00F, 0, 0, 0, 0.40F, 40, 0, 0))
            .addAnimation("left_front_leg", rot(LINEAR, 0.00F, 0, 0, 0, 0.40F, 0, 18, 0))
            .addAnimation("right_front_leg", rot(LINEAR, 0.00F, 0, 0, 0, 0.40F, 0, -18, 0))
            .build();

    /** 8. snap_bite_exec (4 ticks / 0.20 s, impact at tick 2 / 0.10 s): ballistic forward lunge and jaw snap. */
    public static final AnimationDefinition SNAP_BITE_EXEC = AnimationDefinition.Builder.withLength(0.20F)
            .addAnimation("body", pos(LINEAR, 0.00F, 0, 0, 1.8F, 0.10F, 0, 0, -2.8F, 0.20F, 0, 0, -2.4F))
            .addAnimation("body", rot(LINEAR, 0.00F, -8, 0, 0, 0.10F, 10, 0, 0, 0.20F, 8, 0, 0))
            .addAnimation("neck", rot(LINEAR, 0.00F, -22, 0, 0, 0.10F, 18, 0, 0, 0.20F, 14, 0, 0))
            .addAnimation("head", rot(LINEAR, 0.00F, 14, 0, 0, 0.10F, -8, 0, 0, 0.20F, -4, 0, 0))
            .addAnimation("lower_jaw", rot(LINEAR, 0.00F, 40, 0, 0, 0.10F, 0, 0, 0, 0.20F, 0, 0, 0))
            .addAnimation("left_front_leg", rot(LINEAR, 0.00F, 0, 18, 0, 0.10F, 0, -24, 0, 0.20F, 0, -20, 0))
            .addAnimation("right_front_leg", rot(LINEAR, 0.00F, 0, -18, 0, 0.10F, 0, 24, 0, 0.20F, 0, 20, 0))
            .build();

    /** 9. snap_bite_recovery (12 ticks / 0.60 s): settles back from forward bite lunge to neutral stance. */
    public static final AnimationDefinition SNAP_BITE_RECOVERY = AnimationDefinition.Builder.withLength(0.60F)
            .addAnimation("body", pos(SMOOTH, 0.00F, 0, 0, -2.4F, 0.60F, 0, 0, 0))
            .addAnimation("body", rot(SMOOTH, 0.00F, 8, 0, 0, 0.60F, 0, 0, 0))
            .addAnimation("neck", rot(SMOOTH, 0.00F, 14, 0, 0, 0.60F, 0, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.00F, -4, 0, 0, 0.60F, 0, 0, 0))
            .addAnimation("left_front_leg", rot(SMOOTH, 0.00F, 0, -20, 0, 0.60F, 0, 0, 0))
            .addAnimation("right_front_leg", rot(SMOOTH, 0.00F, 0, 20, 0, 0.60F, 0, 0, 0))
            .build();

    /** 10. sulfur_spit_prep (16 ticks / 0.80 s): elevates head 25 deg and inflates throat bellows to limit. */
    public static final AnimationDefinition SULFUR_SPIT_PREP = AnimationDefinition.Builder.withLength(0.80F)
            .addAnimation("body", rot(SMOOTH, 0.00F, 0, 0, 0, 0.80F, -10, 0, 0))
            .addAnimation("neck", rot(SMOOTH, 0.00F, 0, 0, 0, 0.80F, -18, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.00F, 0, 0, 0, 0.80F, -12, 0, 0))
            .addAnimation("throat_sac", scale(SMOOTH, 0.00F, 1.0F, 1.0F, 1.0F, 0.80F, 1.65F, 2.10F, 1.55F))
            .addAnimation("throat_sac", rot(SMOOTH, 0.00F, 0, 0, 0, 0.80F, 22, 0, 0))
            .addAnimation("lower_jaw", rot(LINEAR, 0.00F, 0, 0, 0, 0.80F, 10, 0, 0))
            .build();

    /** 11. sulfur_spit_exec (6 ticks / 0.30 s, release at tick 2 / 0.10 s): violent neck heave and throat compression. */
    public static final AnimationDefinition SULFUR_SPIT_EXEC = AnimationDefinition.Builder.withLength(0.30F)
            .addAnimation("body", rot(LINEAR, 0.00F, -10, 0, 0, 0.10F, 8, 0, 0, 0.30F, 4, 0, 0))
            .addAnimation("body", pos(LINEAR, 0.00F, 0, 0, 0, 0.10F, 0, 0, 1.4F, 0.30F, 0, 0, 0.8F))
            .addAnimation("neck", rot(LINEAR, 0.00F, -18, 0, 0, 0.10F, 20, 0, 0, 0.30F, 10, 0, 0))
            .addAnimation("head", rot(LINEAR, 0.00F, -12, 0, 0, 0.10F, 14, 0, 0, 0.30F, 6, 0, 0))
            .addAnimation("throat_sac", scale(LINEAR, 0.00F, 1.65F, 2.10F, 1.55F, 0.10F, 0.85F, 0.75F, 0.85F, 0.30F, 0.90F, 0.80F, 0.90F))
            .addAnimation("throat_sac", rot(LINEAR, 0.00F, 22, 0, 0, 0.10F, -6, 0, 0, 0.30F, -4, 0, 0))
            .addAnimation("lower_jaw", rot(LINEAR, 0.00F, 10, 0, 0, 0.10F, 42, 0, 0, 0.30F, 26, 0, 0))
            .build();

    /** 12. sulfur_spit_recovery (18 ticks / 0.90 s): deflated throat sac and lowered head recover to rest. */
    public static final AnimationDefinition SULFUR_SPIT_RECOVERY = AnimationDefinition.Builder.withLength(0.90F)
            .addAnimation("body", rot(SMOOTH, 0.00F, 4, 0, 0, 0.90F, 0, 0, 0))
            .addAnimation("body", pos(SMOOTH, 0.00F, 0, 0, 0.8F, 0.90F, 0, 0, 0))
            .addAnimation("neck", rot(SMOOTH, 0.00F, 10, 0, 0, 0.40F, 14, 0, 0, 0.90F, 0, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.00F, 6, 0, 0, 0.40F, 10, 0, 0, 0.90F, 0, 0, 0))
            .addAnimation("throat_sac", scale(SMOOTH, 0.00F, 0.90F, 0.80F, 0.90F, 0.90F, 1.0F, 1.0F, 1.0F))
            .addAnimation("throat_sac", rot(SMOOTH, 0.00F, -4, 0, 0, 0.90F, 0, 0, 0))
            .addAnimation("lower_jaw", rot(SMOOTH, 0.00F, 26, 0, 0, 0.50F, 8, 0, 0, 0.90F, 0, 0, 0))
            .build();

    /** 13. tail_whip_prep (10 ticks / 0.50 s): twists torso 30 deg right and coils tail up like a loaded spring. */
    public static final AnimationDefinition TAIL_WHIP_PREP = AnimationDefinition.Builder.withLength(0.50F)
            .addAnimation("body", rot(LINEAR, 0.00F, 0, 0, 0, 0.50F, 0, -28, 0))
            .addAnimation("neck", rot(LINEAR, 0.00F, 0, 0, 0, 0.50F, 0, 18, 0))
            .addAnimation("tail_base", rot(LINEAR, 0.00F, 0, 0, 0, 0.50F, 12, -38, 0))
            .addAnimation("tail_mid", rot(LINEAR, 0.00F, 0, 0, 0, 0.50F, 10, -42, 0))
            .addAnimation("tail_tip", rot(LINEAR, 0.00F, 0, 0, 0, 0.50F, 8, -36, 0))
            .build();

    /** 14. tail_whip_exec (5 ticks / 0.25 s, impact at tick 3 / 0.15 s): 160 deg horizontal tail whip sweep. */
    public static final AnimationDefinition TAIL_WHIP_EXEC = AnimationDefinition.Builder.withLength(0.25F)
            .addAnimation("body", rot(LINEAR, 0.00F, 0, -28, 0, 0.15F, 0, 32, 0, 0.25F, 0, 28, 0))
            .addAnimation("neck", rot(LINEAR, 0.00F, 0, 18, 0, 0.15F, 0, -20, 0, 0.25F, 0, -16, 0))
            .addAnimation("tail_base", rot(LINEAR, 0.00F, 12, -38, 0, 0.15F, -4, 58, 0, 0.25F, -2, 52, 0))
            .addAnimation("tail_mid", rot(LINEAR, 0.00F, 10, -42, 0, 0.15F, -4, 64, 0, 0.25F, -2, 56, 0))
            .addAnimation("tail_tip", rot(LINEAR, 0.00F, 8, -36, 0, 0.15F, -2, 56, 0, 0.25F, 0, 48, 0))
            .build();

    /** 15. tail_whip_recovery (14 ticks / 0.70 s): drags tail tip along the ground back to center alignment. */
    public static final AnimationDefinition TAIL_WHIP_RECOVERY = AnimationDefinition.Builder.withLength(0.70F)
            .addAnimation("body", rot(SMOOTH, 0.00F, 0, 28, 0, 0.70F, 0, 0, 0))
            .addAnimation("neck", rot(SMOOTH, 0.00F, 0, -16, 0, 0.70F, 0, 0, 0))
            .addAnimation("tail_base", rot(SMOOTH, 0.00F, -2, 52, 0, 0.70F, 0, 0, 0))
            .addAnimation("tail_mid", rot(SMOOTH, 0.00F, -2, 56, 0, 0.70F, 0, 0, 0))
            .addAnimation("tail_tip", rot(SMOOTH, 0.00F, 0, 48, 0, 0.70F, 0, 0, 0))
            .build();

    /** 16. hurt (6 ticks / 0.30 s): backward flinch, head spasm and defensive leg contraction. */
    public static final AnimationDefinition HURT = AnimationDefinition.Builder.withLength(0.30F)
            .addAnimation("body", pos(LINEAR, 0.00F, 0, 0, 0, 0.10F, 0, 0, 1.2F, 0.30F, 0, 0, 0))
            .addAnimation("body", rot(LINEAR, 0.00F, 0, 0, 0, 0.10F, -14, 0, 6, 0.30F, 0, 0, 0))
            .addAnimation("head", rot(LINEAR, 0.00F, 0, 0, 0, 0.10F, -18, 8, 10, 0.30F, 0, 0, 0))
            .addAnimation("lower_jaw", rot(LINEAR, 0.00F, 0, 0, 0, 0.10F, 24, 0, 0, 0.30F, 0, 0, 0))
            .build();

    /** 17. death (24 ticks / 1.20 s): legs lose support, belly drops to ground, tail goes limp and jaw opens slack. */
    public static final AnimationDefinition DEATH = AnimationDefinition.Builder.withLength(1.20F)
            .addAnimation("body", pos(LINEAR, 0.00F, 0, 0, 0, 0.50F, 0, -1.8F, 0, 1.20F, 0, -2.2F, 0))
            .addAnimation("body", rot(LINEAR, 0.00F, 0, 0, 0, 0.60F, 4, -8, 18, 1.20F, 6, -10, 22))
            .addAnimation("neck", rot(LINEAR, 0.00F, 0, 0, 0, 0.70F, 14, 12, 0, 1.20F, 18, 16, 0))
            .addAnimation("head", rot(LINEAR, 0.00F, 0, 0, 0, 0.70F, 12, 10, -12, 1.20F, 16, 14, -16))
            .addAnimation("lower_jaw", rot(LINEAR, 0.00F, 0, 0, 0, 0.80F, 28, 0, 0, 1.20F, 32, 0, 0))
            .addAnimation("throat_sac", scale(LINEAR, 0.00F, 1.0F, 1.0F, 1.0F, 0.80F, 0.80F, 0.65F, 0.80F, 1.20F, 0.75F, 0.60F, 0.75F))
            .addAnimation("left_front_leg", rot(LINEAR, 0.00F, 0, 0, 0, 0.60F, 0, -24, -28, 1.20F, 0, -28, -32))
            .addAnimation("right_front_leg", rot(LINEAR, 0.00F, 0, 0, 0, 0.60F, 0, 24, 28, 1.20F, 0, 28, 32))
            .addAnimation("left_hind_leg", rot(LINEAR, 0.00F, 0, 0, 0, 0.60F, 0, -28, -28, 1.20F, 0, -32, -32))
            .addAnimation("right_hind_leg", rot(LINEAR, 0.00F, 0, 0, 0, 0.60F, 0, 28, 28, 1.20F, 0, 32, 32))
            .addAnimation("tail_base", rot(LINEAR, 0.00F, 0, 0, 0, 0.80F, -6, 16, 0, 1.20F, -8, 20, 0))
            .addAnimation("tail_mid", rot(LINEAR, 0.00F, 0, 0, 0, 0.80F, -4, 18, 0, 1.20F, -6, 22, 0))
            .addAnimation("tail_tip", rot(LINEAR, 0.00F, 0, 0, 0, 0.80F, -2, 14, 0, 1.20F, -4, 18, 0))
            .build();

    private BrimstoneSalamanderAnimations() {}

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

    private static AnimationChannel scale(AnimationChannel.Interpolation interpolation, float... frames) {
        Keyframe[] keyframes = new Keyframe[frames.length / 4];
        for (int i = 0; i < keyframes.length; i++) {
            keyframes[i] = new Keyframe(frames[i * 4],
                    KeyframeAnimations.scaleVec(frames[i * 4 + 1], frames[i * 4 + 2], frames[i * 4 + 3]),
                    interpolation);
        }
        return new AnimationChannel(AnimationChannel.Targets.SCALE, keyframes);
    }
}

