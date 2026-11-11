package com.codenamecoraline.client.firedragon;

import net.minecraft.client.animation.*;

/** Generated tracks, angles in degrees; Java keyframes convert them to radians. */
public final class FireDragonAnimations {
    private FireDragonAnimations() {}
    public static final AnimationDefinition[] CLIPS = {
        AnimationDefinition.Builder.withLength(0.050F)
            .build(),
        AnimationDefinition.Builder.withLength(2.000F).looping()
            .addAnimation("torso", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.0000F, 1.5000F, 0.0000F, 0.0000F, 2.0000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("neck_mid", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.0000F, -2.0000F, 0.0000F, 0.0000F, 2.0000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("tail_2", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.0000F, 0.0000F, 3.0000F, 0.0000F, 2.0000F, 0.0000F, 0.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(1.500F)
            .addAnimation("neck_base", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.7500F, -12.0000F, 0.0000F, 0.0000F, 1.5000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("head", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.4500F, -8.0000F, 18.0000F, 5.0000F, 1.0000F, -5.0000F, -15.0000F, -5.0000F, 1.5000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("tail_1", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.7500F, 0.0000F, -8.0000F, 0.0000F, 1.5000F, 0.0000F, 0.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(1.800F)
            .addAnimation("left_wing_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.5940F, 0.0000F, 30.0000F, -18.0000F, 1.8000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_wing_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.2060F, 0.0000F, -30.0000F, 18.0000F, 1.8000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.9000F, 5.0000F, 0.0000F, 0.0000F, 1.8000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.9000F, -5.0000F, 0.0000F, 0.0000F, 1.8000F, 0.0000F, 0.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(2.000F)
            .addAnimation("neck_base", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.0000F, 0.0000F, 20.0000F, 0.0000F, 2.0000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("head", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.0000F, 5.0000F, 30.0000F, 12.0000F, 2.0000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("tail_1", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.0000F, 0.0000F, -20.0000F, 0.0000F, 2.0000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("tail_2", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.0000F, 0.0000F, -18.0000F, 0.0000F, 2.0000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("jaw", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.0000F, 12.0000F, 0.0000F, 0.0000F, 2.0000F, 0.0000F, 0.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(1.000F).looping()
            .addAnimation("left_front_upper", rot(0.0000F, 22.0000F, 0.0000F, 0.0000F, 0.5000F, -22.0000F, 0.0000F, 0.0000F, 1.0000F, 22.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_front_lower", rot(0.0000F, -8.8000F, 0.0000F, 0.0000F, 0.5000F, 8.8000F, 0.0000F, 0.0000F, 1.0000F, -8.8000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_upper", rot(0.0000F, -22.0000F, 0.0000F, 0.0000F, 0.5000F, 22.0000F, 0.0000F, 0.0000F, 1.0000F, -22.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_lower", rot(0.0000F, 8.8000F, 0.0000F, 0.0000F, 0.5000F, -8.8000F, 0.0000F, 0.0000F, 1.0000F, 8.8000F, 0.0000F, 0.0000F))
            .addAnimation("right_front_upper", rot(0.0000F, -22.0000F, 0.0000F, 0.0000F, 0.5000F, 22.0000F, 0.0000F, 0.0000F, 1.0000F, -22.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_front_lower", rot(0.0000F, 8.8000F, 0.0000F, 0.0000F, 0.5000F, -8.8000F, 0.0000F, 0.0000F, 1.0000F, 8.8000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_upper", rot(0.0000F, 22.0000F, 0.0000F, 0.0000F, 0.5000F, -22.0000F, 0.0000F, 0.0000F, 1.0000F, 22.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_lower", rot(0.0000F, -8.8000F, 0.0000F, 0.0000F, 0.5000F, 8.8000F, 0.0000F, 0.0000F, 1.0000F, -8.8000F, 0.0000F, 0.0000F))
            .addAnimation("tail_1", rot(0.0000F, 0.0000F, -6.0000F, 0.0000F, 0.5000F, 0.0000F, 6.0000F, 0.0000F, 1.0000F, 0.0000F, -6.0000F, 0.0000F))
            .addAnimation("torso", pos(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.2500F, 0.0000F, 0.3000F, 0.0000F, 0.5000F, 0.0000F, 0.0000F, 0.0000F, 0.7500F, 0.0000F, 0.3000F, 0.0000F, 1.0000F, 0.0000F, 0.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(0.800F).looping()
            .addAnimation("left_wing_upper", rot(0.0000F, 0.0000F, 65.0000F, -35.0000F, 0.2000F, 0.0000F, 65.0000F, 55.0000F, 0.4000F, 0.0000F, 65.0000F, -35.0000F, 0.6000F, 0.0000F, 65.0000F, 55.0000F, 0.8000F, 0.0000F, 65.0000F, -35.0000F))
            .addAnimation("left_wing_forearm", rot(0.0000F, 0.0000F, -18.0000F, 0.0000F, 0.2000F, 0.0000F, -18.0000F, -20.0000F, 0.4000F, 0.0000F, -18.0000F, 0.0000F, 0.6000F, 0.0000F, -18.0000F, -20.0000F, 0.8000F, 0.0000F, -18.0000F, 0.0000F))
            .addAnimation("left_wing_tip", rot(0.0000F, 0.0000F, -18.0000F, 0.0000F, 0.4000F, 0.0000F, -18.0000F, 12.0000F, 0.8000F, 0.0000F, -18.0000F, 0.0000F))
            .addAnimation("left_front_upper", rot(0.0000F, -35.0000F, 0.0000F, 0.0000F, 0.8000F, -35.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_front_lower", rot(0.0000F, 55.0000F, 0.0000F, 0.0000F, 0.8000F, 55.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_upper", rot(0.0000F, -35.0000F, 0.0000F, 0.0000F, 0.8000F, -35.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_lower", rot(0.0000F, 55.0000F, 0.0000F, 0.0000F, 0.8000F, 55.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_wing_upper", rot(0.0000F, 0.0000F, -65.0000F, 35.0000F, 0.2000F, 0.0000F, -65.0000F, -55.0000F, 0.4000F, 0.0000F, -65.0000F, 35.0000F, 0.6000F, 0.0000F, -65.0000F, -55.0000F, 0.8000F, 0.0000F, -65.0000F, 35.0000F))
            .addAnimation("right_wing_forearm", rot(0.0000F, 0.0000F, 18.0000F, 0.0000F, 0.2000F, 0.0000F, 18.0000F, 20.0000F, 0.4000F, 0.0000F, 18.0000F, 0.0000F, 0.6000F, 0.0000F, 18.0000F, 20.0000F, 0.8000F, 0.0000F, 18.0000F, 0.0000F))
            .addAnimation("right_wing_tip", rot(0.0000F, 0.0000F, 18.0000F, 0.0000F, 0.4000F, 0.0000F, 18.0000F, -12.0000F, 0.8000F, 0.0000F, 18.0000F, 0.0000F))
            .addAnimation("right_front_upper", rot(0.0000F, -35.0000F, 0.0000F, 0.0000F, 0.8000F, -35.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_front_lower", rot(0.0000F, 55.0000F, 0.0000F, 0.0000F, 0.8000F, 55.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_upper", rot(0.0000F, -35.0000F, 0.0000F, 0.0000F, 0.8000F, -35.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_lower", rot(0.0000F, 55.0000F, 0.0000F, 0.0000F, 0.8000F, 55.0000F, 0.0000F, 0.0000F))
            .addAnimation("torso", pos(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.4000F, 0.0000F, 1.0000F, 0.0000F, 0.8000F, 0.0000F, 0.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(0.800F)
            .addAnimation("torso", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.4000F, -10.0000F, 0.0000F, 0.0000F, 0.8000F, -10.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_wing_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.4000F, 0.0000F, 65.0000F, -35.0000F, 0.8000F, 0.0000F, 65.0000F, -35.0000F))
            .addAnimation("right_wing_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.4000F, 0.0000F, -65.0000F, 35.0000F, 0.8000F, 0.0000F, -65.0000F, 35.0000F))
            .addAnimation("torso", pos(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.3000F, 0.0000F, -1.0000F, 0.0000F, 0.8000F, 0.0000F, 2.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(0.800F)
            .addAnimation("torso", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.4000F, 8.0000F, 0.0000F, 0.0000F, 0.8000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_wing_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.4000F, 0.0000F, 65.0000F, -25.0000F, 0.8000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_wing_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.4000F, 0.0000F, -65.0000F, 25.0000F, 0.8000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("torso", pos(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.4000F, 0.0000F, -1.2000F, 0.0000F, 0.8000F, 0.0000F, 0.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(0.600F)
            .addAnimation("torso", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, -7.0000F, 0.0000F, 0.0000F))
            .addAnimation("neck_base", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, -12.0000F, 0.0000F, 0.0000F))
            .addAnimation("neck_mid", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, 8.0000F, 0.0000F, 0.0000F))
            .addAnimation("head", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, -8.0000F, 0.0000F, 0.0000F))
            .addAnimation("jaw", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, 24.0000F, 0.0000F, 0.0000F))
            .addAnimation("tail_1", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, 12.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, 10.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, 10.0000F, 0.0000F, 0.0000F))
            .addAnimation("torso", pos(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, 0.0000F, -0.3000F, 0.7000F))
            .build(),
        AnimationDefinition.Builder.withLength(0.050F)
            .addAnimation("torso", rot(0.0000F, -7.0000F, 0.0000F, 0.0000F, 0.0500F, 5.0000F, 0.0000F, 0.0000F))
            .addAnimation("neck_base", rot(0.0000F, -12.0000F, 0.0000F, 0.0000F, 0.0500F, 10.0000F, 0.0000F, 0.0000F))
            .addAnimation("neck_mid", rot(0.0000F, 8.0000F, 0.0000F, 0.0000F, 0.0500F, -10.0000F, 0.0000F, 0.0000F))
            .addAnimation("head", rot(0.0000F, -8.0000F, 0.0000F, 0.0000F, 0.0500F, -5.0000F, 0.0000F, 0.0000F))
            .addAnimation("jaw", rot(0.0000F, 24.0000F, 0.0000F, 0.0000F, 0.0500F, 32.0000F, 0.0000F, 0.0000F))
            .addAnimation("tail_1", rot(0.0000F, 12.0000F, 0.0000F, 0.0000F, 0.0500F, -6.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_upper", rot(0.0000F, 10.0000F, 0.0000F, 0.0000F, 0.0500F, -4.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_upper", rot(0.0000F, 10.0000F, 0.0000F, 0.0000F, 0.0500F, -4.0000F, 0.0000F, 0.0000F))
            .addAnimation("torso", pos(0.0000F, 0.0000F, -0.3000F, 0.7000F, 0.0500F, 0.0000F, 0.0000F, -0.5000F))
            .build(),
        AnimationDefinition.Builder.withLength(0.750F)
            .addAnimation("torso", rot(0.0000F, 5.0000F, 0.0000F, 0.0000F, 0.7500F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("neck_base", rot(0.0000F, 10.0000F, 0.0000F, 0.0000F, 0.7500F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("neck_mid", rot(0.0000F, -10.0000F, 0.0000F, 0.0000F, 0.7500F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("head", rot(0.0000F, -5.0000F, 0.0000F, 0.0000F, 0.7500F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("jaw", rot(0.0000F, 32.0000F, 0.0000F, 0.0000F, 0.7500F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("tail_1", rot(0.0000F, -6.0000F, 0.0000F, 0.0000F, 0.7500F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_upper", rot(0.0000F, -4.0000F, 0.0000F, 0.0000F, 0.7500F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_upper", rot(0.0000F, -4.0000F, 0.0000F, 0.0000F, 0.7500F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("torso", pos(0.0000F, 0.0000F, 0.0000F, -0.5000F, 0.7500F, 0.0000F, 0.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(0.400F)
            .addAnimation("torso", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.1000F, 0.0000F, 0.0000F, -8.0000F, 0.4000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("neck_base", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.1000F, -12.0000F, 0.0000F, 0.0000F, 0.4000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("head", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.1000F, 10.0000F, 0.0000F, 0.0000F, 0.4000F, 0.0000F, 0.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(1.000F)
            .addAnimation("neck_base", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.5000F, 15.0000F, 0.0000F, 0.0000F, 1.0000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("head", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.5000F, 12.0000F, 0.0000F, 8.0000F, 1.0000F, 0.0000F, 0.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(1.000F)
            .addAnimation("torso", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, 0.0000F, 0.0000F, 70.0000F, 1.0000F, 0.0000F, 0.0000F, 85.0000F))
            .addAnimation("neck_base", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.0000F, 20.0000F, 0.0000F, 0.0000F))
            .addAnimation("tail_2", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.0000F, 0.0000F, -15.0000F, 0.0000F))
            .addAnimation("torso", pos(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.0000F, 0.0000F, -2.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(0.600F)
            .addAnimation("torso", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, -12.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, -50.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, -50.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_lower", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, 70.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_lower", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, 70.0000F, 0.0000F, 0.0000F))
            .addAnimation("neck_base", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, 12.0000F, 0.0000F, 0.0000F))
            .addAnimation("tail_1", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, 12.0000F, 0.0000F, 0.0000F))
            .addAnimation("torso", pos(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, 0.0000F, -1.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(0.600F)
            .addAnimation("torso", rot(0.0000F, -12.0000F, 0.0000F, 0.0000F, 0.6000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_upper", rot(0.0000F, -50.0000F, 0.0000F, 0.0000F, 0.6000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_upper", rot(0.0000F, -50.0000F, 0.0000F, 0.0000F, 0.6000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_lower", rot(0.0000F, 70.0000F, 0.0000F, 0.0000F, 0.6000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_lower", rot(0.0000F, 70.0000F, 0.0000F, 0.0000F, 0.6000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("neck_base", rot(0.0000F, 12.0000F, 0.0000F, 0.0000F, 0.6000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("tail_1", rot(0.0000F, 12.0000F, 0.0000F, 0.0000F, 0.6000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("torso", pos(0.0000F, 0.0000F, -1.0000F, 0.0000F, 0.6000F, 0.0000F, 0.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(2.000F).looping()
            .addAnimation("torso", rot(0.0000F, 6.0000F, -10.0000F, -8.0000F, 1.0000F, 7.0000F, -10.0000F, -8.0000F, 2.0000F, 6.0000F, -10.0000F, -8.0000F))
            .addAnimation("neck_base", rot(0.0000F, 12.0000F, -18.0000F, 6.0000F, 2.0000F, 12.0000F, -18.0000F, 6.0000F))
            .addAnimation("neck_mid", rot(0.0000F, 6.0000F, -12.0000F, 0.0000F, 1.0000F, 7.0000F, -12.0000F, 0.0000F, 2.0000F, 6.0000F, -12.0000F, 0.0000F))
            .addAnimation("head", rot(0.0000F, 12.0000F, -12.0000F, 8.0000F, 2.0000F, 12.0000F, -12.0000F, 8.0000F))
            .addAnimation("tail_1", rot(0.0000F, 4.0000F, 42.0000F, -8.0000F, 2.0000F, 4.0000F, 42.0000F, -8.0000F))
            .addAnimation("tail_2", rot(0.0000F, -2.0000F, 48.0000F, 0.0000F, 2.0000F, -2.0000F, 48.0000F, 0.0000F))
            .addAnimation("tail_3", rot(0.0000F, -4.0000F, 45.0000F, 0.0000F, 2.0000F, -4.0000F, 45.0000F, 0.0000F))
            .addAnimation("tail_4", rot(0.0000F, -6.0000F, 38.0000F, 0.0000F, 1.0000F, -6.0000F, 40.0000F, 0.0000F, 2.0000F, -6.0000F, 38.0000F, 0.0000F))
            .addAnimation("left_wing_upper", rot(0.0000F, 5.0000F, -20.0000F, 12.0000F, 2.0000F, 5.0000F, -20.0000F, 12.0000F))
            .addAnimation("right_wing_upper", rot(0.0000F, 5.0000F, 20.0000F, -12.0000F, 2.0000F, 5.0000F, 20.0000F, -12.0000F))
            .addAnimation("left_wing_forearm", rot(0.0000F, 0.0000F, 55.0000F, -28.0000F, 2.0000F, 0.0000F, 55.0000F, -28.0000F))
            .addAnimation("right_wing_forearm", rot(0.0000F, 0.0000F, -55.0000F, 28.0000F, 2.0000F, 0.0000F, -55.0000F, 28.0000F))
            .addAnimation("left_wing_tip", rot(0.0000F, 0.0000F, 25.0000F, 10.0000F, 2.0000F, 0.0000F, 25.0000F, 10.0000F))
            .addAnimation("right_wing_tip", rot(0.0000F, 0.0000F, -25.0000F, -10.0000F, 2.0000F, 0.0000F, -25.0000F, -10.0000F))
            .addAnimation("left_front_upper", rot(0.0000F, -65.0000F, 0.0000F, 0.0000F, 2.0000F, -65.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_front_lower", rot(0.0000F, 95.0000F, 0.0000F, 0.0000F, 2.0000F, 95.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_front_foot", rot(0.0000F, -30.0000F, 0.0000F, 0.0000F, 2.0000F, -30.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_upper", rot(0.0000F, -65.0000F, 0.0000F, 0.0000F, 2.0000F, -65.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_lower", rot(0.0000F, 95.0000F, 0.0000F, 0.0000F, 2.0000F, 95.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_foot", rot(0.0000F, -30.0000F, 0.0000F, 0.0000F, 2.0000F, -30.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_front_upper", rot(0.0000F, -65.0000F, 0.0000F, 0.0000F, 2.0000F, -65.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_front_lower", rot(0.0000F, 95.0000F, 0.0000F, 0.0000F, 2.0000F, 95.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_front_foot", rot(0.0000F, -30.0000F, 0.0000F, 0.0000F, 2.0000F, -30.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_upper", rot(0.0000F, -65.0000F, 0.0000F, 0.0000F, 2.0000F, -65.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_lower", rot(0.0000F, 95.0000F, 0.0000F, 0.0000F, 2.0000F, 95.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_foot", rot(0.0000F, -30.0000F, 0.0000F, 0.0000F, 2.0000F, -30.0000F, 0.0000F, 0.0000F))
            .addAnimation("torso", pos(0.0000F, 0.0000F, 1.0000F, 0.0000F, 1.0000F, 0.0000F, 1.1500F, 0.0000F, 2.0000F, 0.0000F, 1.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(1.200F)
            .addAnimation("neck_base", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, 20.0000F, 0.0000F, 0.0000F, 1.2000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("head", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.6000F, 12.0000F, 0.0000F, 0.0000F, 1.2000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("jaw", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.3000F, 18.0000F, 0.0000F, 0.0000F, 0.5000F, 0.0000F, 0.0000F, 0.0000F, 0.7000F, 18.0000F, 0.0000F, 0.0000F, 0.9000F, 0.0000F, 0.0000F, 0.0000F, 1.2000F, 0.0000F, 0.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(5.000F).looping()
            .addAnimation("torso", rot(0.0000F, -5.0000F, -8.0000F, 0.0000F, 1.2500F, -5.0000F, 8.0000F, 0.0000F, 2.5000F, -5.0000F, -8.0000F, 0.0000F, 3.7500F, -5.0000F, 8.0000F, 0.0000F, 5.0000F, -5.0000F, -8.0000F, 0.0000F))
            .addAnimation("tail_1", rot(0.0000F, 0.0000F, 15.0000F, 0.0000F, 2.5000F, 0.0000F, -15.0000F, 0.0000F, 5.0000F, 0.0000F, 15.0000F, 0.0000F))
            .addAnimation("head", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 2.5000F, -10.0000F, 12.0000F, 0.0000F, 5.0000F, 0.0000F, 0.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(1.500F)
            .addAnimation("neck_base", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.7500F, -5.0000F, 12.0000F, 0.0000F, 1.5000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("head", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.7500F, 0.0000F, 15.0000F, 12.0000F, 1.5000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("tail_2", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.7500F, 0.0000F, 12.0000F, 0.0000F, 1.5000F, 0.0000F, 0.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(2.000F)
            .addAnimation("left_wing_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.7000F, 0.0000F, 45.0000F, -25.0000F, 2.0000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_wing_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.3000F, 0.0000F, -45.0000F, 25.0000F, 2.0000F, 0.0000F, 0.0000F, 0.0000F))
            .addAnimation("head", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.0000F, -5.0000F, 0.0000F, 8.0000F, 2.0000F, 0.0000F, 0.0000F, 0.0000F))
            .build(),
        AnimationDefinition.Builder.withLength(5.000F)
            .addAnimation("torso", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.5000F, -12.0000F, 0.0000F, 0.0000F, 5.0000F, -12.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.5000F, -50.0000F, 0.0000F, 0.0000F, 5.0000F, -50.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_upper", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.5000F, -50.0000F, 0.0000F, 0.0000F, 5.0000F, -50.0000F, 0.0000F, 0.0000F))
            .addAnimation("left_hind_lower", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.5000F, 70.0000F, 0.0000F, 0.0000F, 5.0000F, 70.0000F, 0.0000F, 0.0000F))
            .addAnimation("right_hind_lower", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.5000F, 70.0000F, 0.0000F, 0.0000F, 5.0000F, 70.0000F, 0.0000F, 0.0000F))
            .addAnimation("neck_base", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.5000F, 25.0000F, 0.0000F, 0.0000F, 5.0000F, 25.0000F, 0.0000F, 0.0000F))
            .addAnimation("tail_1", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.5000F, 0.0000F, 25.0000F, 0.0000F, 5.0000F, 0.0000F, 25.0000F, 0.0000F))
            .addAnimation("head", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.5000F, 15.0000F, 0.0000F, -10.0000F, 5.0000F, 15.0000F, 0.0000F, -10.0000F))
            .addAnimation("tail_2", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.5000F, 0.0000F, 25.0000F, 0.0000F, 5.0000F, 0.0000F, 25.0000F, 0.0000F))
            .addAnimation("tail_3", rot(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.5000F, 0.0000F, 20.0000F, 0.0000F, 5.0000F, 0.0000F, 20.0000F, 0.0000F))
            .addAnimation("torso", pos(0.0000F, 0.0000F, 0.0000F, 0.0000F, 1.5000F, 0.0000F, -1.3000F, 0.0000F, 5.0000F, 0.0000F, -1.3000F, 0.0000F))
            .build(),
    };
    private static AnimationChannel rot(float... frames) { return channel(true, frames); }
    private static AnimationChannel pos(float... frames) { return channel(false, frames); }
    private static AnimationChannel channel(boolean rotation, float[] frames) {
        Keyframe[] keys = new Keyframe[frames.length / 4];
        for (int i = 0; i < keys.length; i++) { int j = i * 4;
            keys[i] = new Keyframe(frames[j], rotation
                    ? KeyframeAnimations.degreeVec(frames[j+1], frames[j+2], frames[j+3])
                    : KeyframeAnimations.posVec(frames[j+1], frames[j+2], frames[j+3]),
                    AnimationChannel.Interpolations.LINEAR);
        }
        return new AnimationChannel(rotation ? AnimationChannel.Targets.ROTATION : AnimationChannel.Targets.POSITION, keys);
    }
}
