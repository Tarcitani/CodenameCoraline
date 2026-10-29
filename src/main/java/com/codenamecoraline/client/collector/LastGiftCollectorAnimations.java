package com.codenamecoraline.client.collector;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Keyframe animations for the Collector of the Last Gift, the Renegade with the greataxe.
 *
 * <p>The axe ("scythe" part, kept by that name) is a 54-unit haft held at its middle, head towards the top, blade
 * facing forward. Because the hand is at the bottom of the forearm, raising the arm turns the axe upside down; every
 * pose therefore stores the wrist ("scythe" x rotation) that makes the swing read correctly. The reference poses
 * (x rotations in degrees, torso / arm / forearm / wrist):</p>
 * <ul>
 *   <li>CARRY: 3 / -50 / -45 / 60: axe leaning over the right shoulder.</li>
 *   <li>WIND: -18 / -155 / -20 / 143: axe high above and behind the head, about to chop.</li>
 *   <li>IMPACT: 30 / -55 / -10 / 160: axe head driven forward and down into the ground.</li>
 *   <li>RAISED: -10 / -160 / -20 / 180: axe held straight up like a staff (spells, roars).</li>
 *   <li>GROUND: 20 / -20 / -10 / 170: axe head dragging on the ground (recovery).</li>
 * </ul>
 */
public final class LastGiftCollectorAnimations {
    public static final String ID_IDLE = "animation.last_gift_collector.idle";
    public static final String ID_IDLE_ALTERNATE = "animation.last_gift_collector.idle_alternate";
    public static final String ID_WALK = "animation.last_gift_collector.walk";
    public static final String ID_APPEAR = "animation.last_gift_collector.appear";
    public static final String ID_JUMP_FROM_SLEIGH = "animation.last_gift_collector.jump_from_sleigh";
    public static final String ID_FALL = "animation.last_gift_collector.fall";
    public static final String ID_LAND = "animation.last_gift_collector.land";
    public static final String ID_INTIMIDATE = "animation.last_gift_collector.intimidate";
    public static final String ID_ATTACK_SCYTHE_PREP = "animation.last_gift_collector.attack_scythe_prep";
    public static final String ID_ATTACK_SCYTHE_EXEC = "animation.last_gift_collector.attack_scythe_exec";
    public static final String ID_ATTACK_HOLLOW_BELL_PREP = "animation.last_gift_collector.attack_hollow_bell_prep";
    public static final String ID_ATTACK_HOLLOW_BELL_IMPACT = "animation.last_gift_collector.attack_hollow_bell_impact";
    public static final String ID_ATTACK_PAWNS = "animation.last_gift_collector.attack_pawns";
    public static final String ID_ATTACK_SOLSTICE_JUMP = "animation.last_gift_collector.attack_solstice_jump";
    public static final String ID_ATTACK_CAST = "animation.last_gift_collector.attack_cast";
    public static final String ID_ATTACK_SPIN = "animation.last_gift_collector.attack_spin";
    public static final String ID_ATTACK_SLAM = "animation.last_gift_collector.attack_slam";
    public static final String ID_RECOVERY = "animation.last_gift_collector.recovery";
    public static final String ID_PHASE_TRANSITION = "animation.last_gift_collector.phase_transition";
    public static final String ID_RAGE = "animation.last_gift_collector.rage";
    public static final String ID_HURT = "animation.last_gift_collector.hurt";
    public static final String ID_DEATH = "animation.last_gift_collector.death";

    private static final AnimationChannel.Interpolation LINEAR = AnimationChannel.Interpolations.LINEAR;
    private static final AnimationChannel.Interpolation SMOOTH = AnimationChannel.Interpolations.CATMULLROM;

    /** Slow breathing with the axe resting over the right shoulder, bell and beard swaying. */
    public static final AnimationDefinition IDLE = AnimationDefinition.Builder.withLength(3.2F).looping()
            .addAnimation("pelvis", pos(SMOOTH, 0.0F, 0, 0, 0, 1.6F, 0, 0.5F, 0, 3.2F, 0, 0, 0))
            .addAnimation("torso", rot(SMOOTH, 0.0F, 3, 0, 0, 1.6F, 5.5F, 1.5F, 0, 3.2F, 3, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, -3, 0, 0, 1.6F, -1, 4, 0, 3.2F, -3, 0, 0))
            .addAnimation("beard", rot(SMOOTH, 0.0F, 0, 0, 0, 1.6F, 4, 0, -2, 3.2F, 0, 0, 0))
            .addAnimation("mantle", rot(SMOOTH, 0.0F, 0, 0, 0, 1.6F, 2.5F, 0, 1.5F, 3.2F, 0, 0, 0))
            .addAnimation("cage_bell", rot(SMOOTH, 0.0F, 0, 0, -5, 1.6F, 3, 0, 5, 3.2F, 0, 0, -5))
            .addAnimation("chains", rot(SMOOTH, 0.0F, 0, 0, -2, 1.6F, 2, 0, 2, 3.2F, 0, 0, -2))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, -50, 0, 0, 1.6F, -53, 2, 1, 3.2F, -50, 0, 0))
            .addAnimation("right_forearm", rot(SMOOTH, 0.0F, -45, 0, 0, 1.6F, -42, 0, 0, 3.2F, -45, 0, 0))
            .addAnimation("scythe", rot(SMOOTH, 0.0F, 60, 0, 0, 1.6F, 58, 2, 1, 3.2F, 60, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, 8, -5, -6, 1.6F, 11, -5, -8, 3.2F, 8, -5, -6))
            .build();

    /** Occasional shrug: the Renegade shifts the axe on his shoulder and glares sideways. */
    public static final AnimationDefinition IDLE_ALTERNATE = AnimationDefinition.Builder.withLength(2.4F)
            .addAnimation("torso", rot(SMOOTH, 0.0F, 3, 0, 0, 0.8F, 6, -12, 2, 1.6F, 4, 10, -2, 2.4F, 3, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, -3, 0, 0, 0.7F, 4, -26, 6, 1.6F, -2, 24, -4, 2.4F, -3, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, -50, 0, 0, 0.8F, -78, 8, 10, 1.4F, -62, 4, 4, 2.4F, -50, 0, 0))
            .addAnimation("right_forearm", rot(SMOOTH, 0.0F, -45, 0, 0, 0.8F, -30, 0, 0, 2.4F, -45, 0, 0))
            .addAnimation("scythe", rot(SMOOTH, 0.0F, 60, 0, 0, 0.8F, 78, 0, 4, 1.4F, 66, 0, 2, 2.4F, 60, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, 8, -5, -6, 0.8F, -28, 6, -22, 1.6F, 12, -6, -10, 2.4F, 8, -5, -6))
            .addAnimation("cage_bell", rot(SMOOTH, 0.0F, 0, 0, 0, 0.6F, 8, 0, 12, 1.4F, -6, 0, -10, 2.4F, 0, 0, 0))
            .build();

    /** Heavy stride: forward lean, thumping bob, the axe bouncing on the shoulder and the sack swinging. */
    public static final AnimationDefinition WALK = AnimationDefinition.Builder.withLength(1.2F).looping()
            .addAnimation("pelvis", pos(SMOOTH, 0.0F, 0, 0, 0, 0.3F, 0, -1.6F, 0, 0.6F, 0, 0, 0, 0.9F, 0, -1.6F, 0, 1.2F, 0, 0, 0))
            .addAnimation("torso", rot(SMOOTH, 0.0F, 10, -7, -2, 0.3F, 12, 0, 0, 0.6F, 10, 7, 2, 0.9F, 12, 0, 0, 1.2F, 10, -7, -2))
            .addAnimation("head", rot(SMOOTH, 0.0F, -9, 5, 0, 0.6F, -9, -5, 0, 1.2F, -9, 5, 0))
            .addAnimation("reliquary_cage", rot(SMOOTH, 0.0F, 2, 0, -4, 0.6F, 5, 0, 4, 1.2F, 2, 0, -4))
            .addAnimation("cage_bell", rot(SMOOTH, 0.0F, 4, 0, -12, 0.6F, -4, 0, 12, 1.2F, 4, 0, -12))
            .addAnimation("chains", rot(SMOOTH, 0.0F, 3, 0, -5, 0.6F, -2, 0, 5, 1.2F, 3, 0, -5))
            .addAnimation("mantle", rot(SMOOTH, 0.0F, 4, 0, 0, 0.3F, 8, 0, 0, 0.6F, 4, 0, 0, 0.9F, 8, 0, 0, 1.2F, 4, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, -50, 0, 0, 0.3F, -56, 0, 0, 0.6F, -50, 0, 0, 0.9F, -56, 0, 0, 1.2F, -50, 0, 0))
            .addAnimation("scythe", rot(SMOOTH, 0.0F, 60, 0, 0, 0.3F, 52, 0, 4, 0.6F, 60, 0, 0, 0.9F, 52, 0, -4, 1.2F, 60, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, 24, 0, -6, 0.6F, -22, 0, -6, 1.2F, 24, 0, -6))
            .build();

    /** Poised silhouette aboard the flying sleigh: axe raised like a staff, coat streaming in the wind. */
    public static final AnimationDefinition APPEAR = AnimationDefinition.Builder.withLength(2.0F).looping()
            .addAnimation("torso", rot(SMOOTH, 0.0F, -6, -8, 0, 1.0F, -3, -10, 2, 2.0F, -6, -8, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, -10, 10, 0, 1.0F, -6, 12, 0, 2.0F, -10, 10, 0))
            .addAnimation("mantle", rot(SMOOTH, 0.0F, 14, 0, 4, 1.0F, 20, 0, -4, 2.0F, 14, 0, 4))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, -150, 0, 0, 1.0F, -154, 0, 0, 2.0F, -150, 0, 0))
            .addAnimation("right_forearm", rot(SMOOTH, 0.0F, -20, 0, 0, 2.0F, -20, 0, 0))
            .addAnimation("scythe", rot(SMOOTH, 0.0F, 175, 0, 0, 1.0F, 179, 0, 0, 2.0F, 175, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, -20, -15, -12, 1.0F, -25, -18, -15, 2.0F, -20, -15, -12))
            .build();

    /** Deep crouch, then an explosive launch off the sleigh with the axe thrown overhead (1.0s). */
    public static final AnimationDefinition JUMP_FROM_SLEIGH = AnimationDefinition.Builder.withLength(1.0F)
            .addAnimation("pelvis", pos(LINEAR, 0.0F, 0, 0, 0, 0.35F, 0, 5.0F, 0, 0.65F, 0, -4.0F, -2.0F, 1.0F, 0, -2.0F, 0))
            .addAnimation("torso", rot(LINEAR, 0.0F, -6, 0, 0, 0.35F, 28, 0, 0, 0.65F, -14, 0, 0, 1.0F, 12, 0, 0))
            .addAnimation("head", rot(LINEAR, 0.0F, -10, 0, 0, 0.35F, 22, 0, 0, 0.65F, 18, 0, 0, 1.0F, 24, 0, 0))
            .addAnimation("right_arm", rot(LINEAR, 0.0F, -150, 0, 0, 0.35F, -40, 0, 0, 0.65F, -155, 0, 0, 1.0F, -155, 0, 0))
            .addAnimation("right_forearm", rot(LINEAR, 0.0F, -20, 0, 0, 0.35F, -30, 0, 0, 1.0F, -20, 0, 0))
            .addAnimation("scythe", rot(LINEAR, 0.0F, 175, 0, 0, 0.35F, 130, 0, 0, 0.65F, 150, 0, 0, 1.0F, 143, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0.0F, -20, -10, -10, 0.35F, 30, -10, -18, 0.65F, -95, -20, -25, 1.0F, -135, 12, -10))
            .addAnimation("right_leg", rot(LINEAR, 0.0F, 0, 0, 0, 0.35F, -45, 0, 0, 0.65F, 30, 0, 0, 1.0F, -20, 0, 0))
            .addAnimation("left_leg", rot(LINEAR, 0.0F, 0, 0, 0, 0.35F, -45, 0, 0, 0.65F, 20, 0, 0, 1.0F, 15, 0, 0))
            .build();

    /** Skyward fall with the axe cocked high behind the head, ready for the landing blow. */
    public static final AnimationDefinition FALL = AnimationDefinition.Builder.withLength(1.2F).looping()
            .addAnimation("torso", rot(SMOOTH, 0.0F, -16, 0, 0, 0.6F, -20, 0, 0, 1.2F, -16, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, 14, 0, 0, 0.6F, 18, 0, 0, 1.2F, 14, 0, 0))
            .addAnimation("mantle", rot(SMOOTH, 0.0F, 32, 0, 6, 0.6F, 40, 0, -6, 1.2F, 32, 0, 6))
            .addAnimation("beard", rot(SMOOTH, 0.0F, -25, 0, 0, 0.6F, -32, 0, 0, 1.2F, -25, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, -155, 0, 0, 0.6F, -160, 0, 0, 1.2F, -155, 0, 0))
            .addAnimation("right_forearm", rot(SMOOTH, 0.0F, -20, 0, 0, 1.2F, -20, 0, 0))
            .addAnimation("scythe", rot(SMOOTH, 0.0F, 143, 0, 0, 0.6F, 148, 0, 0, 1.2F, 143, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, -140, 12, 0, 0.6F, -146, 12, 0, 1.2F, -140, 12, 0))
            .addAnimation("right_leg", rot(SMOOTH, 0.0F, -28, 0, 4, 0.6F, -32, 0, 4, 1.2F, -28, 0, 4))
            .addAnimation("left_leg", rot(SMOOTH, 0.0F, 18, 0, -4, 0.6F, 22, 0, -4, 1.2F, 18, 0, -4))
            .addAnimation("cage_bell", rot(SMOOTH, 0.0F, -18, 0, -14, 0.6F, -24, 0, 14, 1.2F, -18, 0, -14))
            .build();

    /** Touchdown: the axe comes down into the ground with the whole body behind it (1.1s). */
    public static final AnimationDefinition LAND = AnimationDefinition.Builder.withLength(1.1F)
            .addAnimation("pelvis", pos(LINEAR, 0.0F, 0, 0, 0, 0.18F, 0, 7.5F, 0, 0.55F, 0, 6.0F, 0, 1.1F, 0, 0, 0))
            .addAnimation("torso", rot(LINEAR, 0.0F, -16, 0, 0, 0.18F, 32, 0, 0, 0.55F, 26, 0, 0, 1.1F, 3, 0, 0))
            .addAnimation("head", rot(LINEAR, 0.0F, 14, 0, 0, 0.18F, 18, 0, 0, 0.55F, 12, 0, 0, 1.1F, -3, 0, 0))
            .addAnimation("right_arm", rot(LINEAR, 0.0F, -155, 0, 0, 0.18F, -55, 0, 0, 0.55F, -48, 0, 0, 1.1F, -50, 0, 0))
            .addAnimation("right_forearm", rot(LINEAR, 0.0F, -20, 0, 0, 0.18F, -10, 0, 0, 0.55F, -20, 0, 0, 1.1F, -45, 0, 0))
            .addAnimation("scythe", rot(LINEAR, 0.0F, 143, 0, 0, 0.18F, 160, 0, 0, 0.55F, 150, 0, 0, 1.1F, 60, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0.0F, -140, 12, 0, 0.18F, -60, 12, 0, 0.55F, -40, 8, -10, 1.1F, 8, -5, -6))
            .addAnimation("right_leg", rot(LINEAR, 0.0F, -25, 0, 0, 0.18F, -55, 0, 0, 0.55F, -42, 0, 0, 1.1F, 0, 0, 0))
            .addAnimation("left_leg", rot(LINEAR, 0.0F, 15, 0, 0, 0.18F, 42, 0, 0, 0.55F, 30, 0, 0, 1.1F, 0, 0, 0))
            .addAnimation("cage_bell", rot(LINEAR, 0.0F, -20, 0, 0, 0.2F, 28, 0, 16, 0.6F, -18, 0, -12, 1.1F, 0, 0, 0))
            .build();

    /** Reveal: he straightens, raises the axe, points it at the intruders and lets the bell toll (2.2s). */
    public static final AnimationDefinition INTIMIDATE = AnimationDefinition.Builder.withLength(2.2F)
            .addAnimation("torso", rot(SMOOTH, 0.0F, 14, 0, 0, 0.7F, -12, -8, 0, 1.5F, -2, 8, 0, 2.2F, 3, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, 16, 0, 0, 0.7F, -16, -6, 4, 1.5F, -2, 6, -3, 2.2F, -3, 0, 0))
            .addAnimation("mantle", rot(SMOOTH, 0.0F, 6, 0, 0, 0.8F, -8, 0, 8, 1.5F, -6, 0, -8, 2.2F, 0, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, -20, 0, 0, 0.75F, -158, 0, 0, 1.55F, -100, 0, 0, 2.2F, -50, 0, 0))
            .addAnimation("right_forearm", rot(SMOOTH, 0.0F, -10, 0, 0, 0.75F, -20, 0, 0, 1.55F, -20, 0, 0, 2.2F, -45, 0, 0))
            .addAnimation("scythe", rot(SMOOTH, 0.0F, 160, 0, 0, 0.75F, 178, 0, 0, 1.55F, 190, 0, 0, 2.2F, 60, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, 8, -5, -4, 0.75F, -48, 20, -28, 1.55F, -28, 10, -16, 2.2F, 8, -5, -6))
            .addAnimation("cage_bell", rot(SMOOTH, 0.0F, 0, 0, 0, 0.5F, 22, 0, 20, 1.1F, -22, 0, -20, 1.7F, 14, 0, 12, 2.2F, 0, 0, 0))
            .build();

    /** Wind-up of the axe cleave (1.0s): twist right, axe high and back. */
    public static final AnimationDefinition ATTACK_SCYTHE_PREP = AnimationDefinition.Builder.withLength(1.0F)
            .addAnimation("pelvis", pos(LINEAR, 0.0F, 0, 0, 0, 0.7F, 0, 1.5F, 0, 1.0F, 0, 1.5F, 0))
            .addAnimation("torso", rot(LINEAR, 0.0F, 3, 0, 0, 0.55F, -8, 32, 6, 1.0F, -10, 46, 8))
            .addAnimation("head", rot(LINEAR, 0.0F, -3, 0, 0, 0.6F, -4, -18, 0, 1.0F, -4, -30, 0))
            .addAnimation("right_arm", rot(LINEAR, 0.0F, -50, 0, 0, 0.55F, -120, 0, 18, 1.0F, -135, 0, 24))
            .addAnimation("right_forearm", rot(LINEAR, 0.0F, -45, 0, 0, 0.55F, -25, 0, 0, 1.0F, -20, 0, 0))
            .addAnimation("scythe", rot(LINEAR, 0.0F, 60, 0, 0, 0.55F, 138, 0, 0, 1.0F, 148, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0.0F, 8, -5, -6, 1.0F, -86, 10, -18))
            .addAnimation("cage_bell", rot(LINEAR, 0.0F, 0, 0, 0, 1.0F, 10, 0, 12))
            .build();

    /** Cleave (0.9s): the axe whips across the front arc, the body twisting through the blow. */
    public static final AnimationDefinition ATTACK_SCYTHE_EXEC = AnimationDefinition.Builder.withLength(0.9F)
            .addAnimation("pelvis", pos(LINEAR, 0.0F, 0, 1.5F, 0, 0.2F, 0, 3.5F, -1.5F, 0.9F, 0, 0, 0))
            .addAnimation("torso", rot(LINEAR, 0.0F, -10, 46, 8, 0.2F, 22, -52, -6, 0.5F, 14, -40, -4, 0.9F, 3, 0, 0))
            .addAnimation("head", rot(LINEAR, 0.0F, -4, -30, 0, 0.2F, 12, 30, 0, 0.9F, -3, 0, 0))
            .addAnimation("right_arm", rot(LINEAR, 0.0F, -135, 0, 24, 0.2F, -66, 0, -8, 0.5F, -60, 0, -10, 0.9F, -50, 0, 0))
            .addAnimation("right_forearm", rot(LINEAR, 0.0F, -20, 0, 0, 0.2F, -10, 0, 0, 0.9F, -45, 0, 0))
            .addAnimation("scythe", rot(LINEAR, 0.0F, 148, 0, 0, 0.2F, 168, 0, 0, 0.5F, 160, 0, 0, 0.9F, 60, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0.0F, -86, 10, -18, 0.2F, -50, -20, -10, 0.9F, 8, -5, -6))
            .addAnimation("right_leg", rot(LINEAR, 0.0F, 0, 0, 0, 0.2F, -30, 0, 0, 0.9F, 0, 0, 0))
            .addAnimation("left_leg", rot(LINEAR, 0.0F, 0, 0, 0, 0.2F, 18, 0, 0, 0.9F, 0, 0, 0))
            .build();

    /** Overhead two-handed raise (1.4s): the axe goes high above the head as the bell tolls. */
    public static final AnimationDefinition ATTACK_HOLLOW_BELL_PREP = AnimationDefinition.Builder.withLength(1.4F)
            .addAnimation("pelvis", pos(SMOOTH, 0.0F, 0, 0, 0, 0.5F, 0, 2.5F, 0, 1.4F, 0, 0, 0))
            .addAnimation("torso", rot(SMOOTH, 0.0F, 3, 0, 0, 0.7F, -10, 0, 0, 1.4F, -20, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, -3, 0, 0, 0.7F, -14, 0, 0, 1.4F, -20, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, -50, 0, 0, 0.7F, -120, 0, 0, 1.4F, -158, 0, 0))
            .addAnimation("right_forearm", rot(SMOOTH, 0.0F, -45, 0, 0, 1.4F, -20, 0, 0))
            .addAnimation("scythe", rot(SMOOTH, 0.0F, 60, 0, 0, 0.7F, 120, 0, 0, 1.4F, 143, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, 8, -5, -6, 0.7F, -100, 12, 0, 1.4F, -142, 12, 0))
            .addAnimation("cage_bell", rot(SMOOTH, 0.0F, 0, 0, 0, 0.45F, 24, 0, 22, 0.95F, -24, 0, -22, 1.4F, 18, 0, 16))
            .build();

    /** Chop (0.5s): the axe is driven into the ground, sending frost waves out along it. */
    public static final AnimationDefinition ATTACK_HOLLOW_BELL_IMPACT = AnimationDefinition.Builder.withLength(0.5F)
            .addAnimation("pelvis", pos(LINEAR, 0.0F, 0, 0, 0, 0.15F, 0, 3.5F, 0, 0.5F, 0, 1.0F, 0))
            .addAnimation("torso", rot(LINEAR, 0.0F, -20, 0, 0, 0.15F, 32, 0, 0, 0.5F, 24, 0, 0))
            .addAnimation("head", rot(LINEAR, 0.0F, -20, 0, 0, 0.15F, 16, 0, 0, 0.5F, 10, 0, 0))
            .addAnimation("right_arm", rot(LINEAR, 0.0F, -158, 0, 0, 0.15F, -55, 0, 0, 0.5F, -55, 0, 0))
            .addAnimation("right_forearm", rot(LINEAR, 0.0F, -20, 0, 0, 0.15F, -10, 0, 0, 0.5F, -10, 0, 0))
            .addAnimation("scythe", rot(LINEAR, 0.0F, 143, 0, 0, 0.15F, 160, 0, 0, 0.5F, 160, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0.0F, -142, 12, 0, 0.15F, -60, 12, 0, 0.5F, -62, 12, 0))
            .addAnimation("cage_bell", rot(LINEAR, 0.0F, 18, 0, 16, 0.18F, -28, 0, -24, 0.5F, 12, 0, 8))
            .build();

    /** Procissão dos Penhores (1.6s): the axe is planted, the free hand draws up the roots. */
    public static final AnimationDefinition ATTACK_PAWNS = AnimationDefinition.Builder.withLength(1.6F)
            .addAnimation("torso", rot(SMOOTH, 0.0F, 3, 0, 0, 0.65F, -6, -14, 0, 1.0F, -8, -16, 0, 1.25F, 16, 8, 0, 1.6F, 4, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, -3, 0, 0, 0.65F, -10, 12, 0, 1.0F, -12, 14, 0, 1.25F, 16, -6, 0, 1.6F, 0, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, 8, -5, -6, 0.65F, -110, -18, -22, 1.0F, -125, -20, -28, 1.25F, -22, 10, -12, 1.6F, 8, -5, -6))
            .addAnimation("left_forearm", rot(SMOOTH, 0.0F, 0, 0, 0, 0.65F, -32, 0, 0, 1.0F, -40, 0, 0, 1.25F, -10, 0, 0, 1.6F, 0, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, -50, 0, 0, 0.5F, -34, 0, 0, 1.6F, -34, 0, 0))
            .addAnimation("right_forearm", rot(SMOOTH, 0.0F, -45, 0, 0, 0.5F, -10, 0, 0, 1.6F, -10, 0, 0))
            .addAnimation("scythe", rot(SMOOTH, 0.0F, 60, 0, 0, 0.5F, 44, 0, 0, 1.6F, 44, 0, 0))
            .addAnimation("cage_bell", rot(SMOOTH, 0.0F, 0, 0, 0, 0.5F, 18, 0, 16, 1.0F, -20, 0, -18, 1.6F, 0, 0, 0))
            .build();

    /** Salto do Solstício (2.3s): coil with the axe back, leap with it raised, drive it down on landing. */
    public static final AnimationDefinition ATTACK_SOLSTICE_JUMP = AnimationDefinition.Builder.withLength(2.3F)
            .addAnimation("pelvis", pos(LINEAR, 0.0F, 0, 0, 0, 0.9F, 0, 6.0F, 0, 1.3F, 0, 7.5F, 0, 1.75F, 0, -3.0F, 0, 2.15F, 0, 6.5F, 0, 2.3F, 0, 2.0F, 0))
            .addAnimation("torso", rot(LINEAR, 0.0F, 3, 0, 0, 0.9F, 26, 0, 0, 1.3F, 34, 0, 0, 1.75F, -18, 0, 0, 2.15F, 32, 0, 0, 2.3F, 14, 0, 0))
            .addAnimation("head", rot(LINEAR, 0.0F, -3, 0, 0, 1.3F, 22, 0, 0, 1.75F, -14, 0, 0, 2.15F, 24, 0, 0, 2.3F, 10, 0, 0))
            .addAnimation("right_arm", rot(LINEAR, 0.0F, -50, 0, 0, 1.3F, -150, 0, 0, 1.75F, -158, 0, 0, 2.15F, -55, 0, 0, 2.3F, -55, 0, 0))
            .addAnimation("right_forearm", rot(LINEAR, 0.0F, -45, 0, 0, 1.3F, -25, 0, 0, 1.75F, -20, 0, 0, 2.15F, -10, 0, 0))
            .addAnimation("scythe", rot(LINEAR, 0.0F, 60, 0, 0, 1.3F, 140, 0, 0, 1.75F, 143, 0, 0, 2.15F, 160, 0, 0, 2.3F, 160, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0.0F, 8, -5, -6, 1.3F, -130, 12, 0, 1.75F, -142, 12, 0, 2.15F, -60, 12, 0, 2.3F, -62, 12, 0))
            .addAnimation("right_leg", rot(LINEAR, 0.0F, 0, 0, 0, 1.3F, -48, 0, 0, 1.75F, -25, 0, 0, 2.15F, -42, 0, 0, 2.3F, -12, 0, 0))
            .addAnimation("left_leg", rot(LINEAR, 0.0F, 0, 0, 0, 1.3F, -48, 0, 0, 1.75F, 20, 0, 0, 2.15F, 32, 0, 0, 2.3F, 10, 0, 0))
            .build();

    /** Spellcasting (2.0s loop): the axe raised like a staff, the free hand pushing out the magic, bell ringing. */
    public static final AnimationDefinition ATTACK_CAST = AnimationDefinition.Builder.withLength(2.0F).looping()
            .addAnimation("pelvis", pos(SMOOTH, 0.0F, 0, 0, 0, 1.0F, 0, 0.8F, 0, 2.0F, 0, 0, 0))
            .addAnimation("torso", rot(SMOOTH, 0.0F, -6, 0, 0, 0.5F, -12, 0, 0, 1.0F, -10, 0, 3, 2.0F, -6, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, -6, 0, 0, 0.5F, -18, 0, 0, 1.0F, -16, 4, 0, 2.0F, -6, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, -120, 0, 0, 0.5F, -160, 0, 0, 1.0F, -165, 0, 4, 2.0F, -120, 0, 0))
            .addAnimation("right_forearm", rot(SMOOTH, 0.0F, -20, 0, 0, 2.0F, -20, 0, 0))
            .addAnimation("scythe", rot(SMOOTH, 0.0F, 150, 0, 0, 0.5F, 180, 0, 0, 1.0F, 184, 0, 6, 2.0F, 150, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, -40, 0, -10, 0.5F, -92, 0, -14, 1.0F, -100, 0, -16, 1.5F, -90, 0, -14, 2.0F, -40, 0, -10))
            .addAnimation("left_forearm", rot(SMOOTH, 0.0F, 0, 0, 0, 0.5F, -12, 0, 0, 1.0F, -22, 0, 0, 2.0F, 0, 0, 0))
            .addAnimation("cage_bell", rot(SMOOTH, 0.0F, 0, 0, 0, 0.3F, 22, 0, 20, 0.7F, -22, 0, -20, 1.1F, 22, 0, 20, 1.5F, -22, 0, -20, 2.0F, 0, 0, 0))
            .addAnimation("chains", rot(SMOOTH, 0.0F, 0, 0, 0, 0.5F, -14, 0, 12, 1.0F, 14, 0, -12, 2.0F, 0, 0, 0))
            .build();

    /** Whirlwind (1.0s loop): arms flung wide, the whole body leaning into the spin the entity performs. */
    public static final AnimationDefinition ATTACK_SPIN = AnimationDefinition.Builder.withLength(1.0F).looping()
            .addAnimation("pelvis", pos(SMOOTH, 0.0F, 0, 1.5F, 0, 0.5F, 0, 2.2F, 0, 1.0F, 0, 1.5F, 0))
            .addAnimation("torso", rot(SMOOTH, 0.0F, 20, 0, 6, 0.5F, 24, 0, -6, 1.0F, 20, 0, 6))
            .addAnimation("head", rot(SMOOTH, 0.0F, 6, 0, 0, 1.0F, 6, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, -82, 78, 0, 0.5F, -84, 82, 0, 1.0F, -82, 78, 0))
            .addAnimation("right_forearm", rot(SMOOTH, 0.0F, -6, 0, 0, 1.0F, -6, 0, 0))
            .addAnimation("scythe", rot(SMOOTH, 0.0F, 180, 0, 0, 1.0F, 180, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, -70, -76, -10, 0.5F, -74, -80, -10, 1.0F, -70, -76, -10))
            .addAnimation("right_leg", rot(SMOOTH, 0.0F, -20, 0, 0, 0.5F, 20, 0, 0, 1.0F, -20, 0, 0))
            .addAnimation("left_leg", rot(SMOOTH, 0.0F, 20, 0, 0, 0.5F, -20, 0, 0, 1.0F, 20, 0, 0))
            .addAnimation("mantle", rot(SMOOTH, 0.0F, 14, 0, 0, 0.5F, 22, 0, 0, 1.0F, 14, 0, 0))
            .build();

    /** Stomp and summoning (1.6s): axe up, one boot lifted, then a ground-shaking slam with the planted axe. */
    public static final AnimationDefinition ATTACK_SLAM = AnimationDefinition.Builder.withLength(1.6F)
            .addAnimation("pelvis", pos(LINEAR, 0.0F, 0, 0, 0, 0.7F, 0, 3.0F, 0, 0.95F, 0, 4.5F, 0, 1.15F, 0, -1.5F, 0, 1.6F, 0, 0, 0))
            .addAnimation("torso", rot(LINEAR, 0.0F, 3, 0, 0, 0.7F, -14, 0, 0, 0.95F, -18, 0, 0, 1.15F, 26, 0, 0, 1.6F, 8, 0, 0))
            .addAnimation("head", rot(LINEAR, 0.0F, -3, 0, 0, 0.8F, -14, 0, 0, 1.15F, 20, 0, 0, 1.6F, 0, 0, 0))
            .addAnimation("right_arm", rot(LINEAR, 0.0F, -50, 0, 0, 0.7F, -150, 0, 0, 0.95F, -160, 0, 0, 1.15F, -48, 0, 0, 1.6F, -40, 0, 0))
            .addAnimation("right_forearm", rot(LINEAR, 0.0F, -45, 0, 0, 0.7F, -22, 0, 0, 0.95F, -20, 0, 0, 1.15F, -8, 0, 0, 1.6F, -20, 0, 0))
            .addAnimation("scythe", rot(LINEAR, 0.0F, 60, 0, 0, 0.7F, 150, 0, 0, 0.95F, 176, 0, 0, 1.15F, 172, 0, 0, 1.6F, 120, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0.0F, 8, -5, -6, 0.7F, -120, 20, 0, 0.95F, -150, 14, 0, 1.15F, -40, 10, -8, 1.6F, 0, -5, -6))
            .addAnimation("right_leg", rot(LINEAR, 0.0F, 0, 0, 0, 0.7F, -62, 0, 0, 0.95F, -70, 0, 0, 1.15F, 8, 0, 0, 1.6F, 0, 0, 0))
            .addAnimation("cage_bell", rot(LINEAR, 0.0F, 0, 0, 0, 0.95F, 14, 0, 12, 1.2F, -28, 0, -20, 1.6F, 0, 0, 0))
            .build();

    /** Vulnerable recovery: bent over, panting, the axe head dragging on the ground. */
    public static final AnimationDefinition RECOVERY = AnimationDefinition.Builder.withLength(1.5F)
            .addAnimation("pelvis", pos(SMOOTH, 0.0F, 0, 1.5F, 0, 0.75F, 0, 2.5F, 0, 1.5F, 0, 1.5F, 0))
            .addAnimation("torso", rot(SMOOTH, 0.0F, 20, 0, 0, 0.75F, 25, 0, 2, 1.5F, 20, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, 18, 0, 0, 0.75F, 24, 0, -2, 1.5F, 18, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, -20, 0, 0, 0.75F, -24, 0, 0, 1.5F, -20, 0, 0))
            .addAnimation("right_forearm", rot(SMOOTH, 0.0F, -10, 0, 0, 1.5F, -10, 0, 0))
            .addAnimation("scythe", rot(SMOOTH, 0.0F, 170, 0, 0, 1.5F, 170, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, 18, 0, -10, 0.75F, 28, 0, -14, 1.5F, 18, 0, -10))
            .build();

    /** Stage transition (3.0s): bellowing roar, the axe thrust skyward, sack bursting open. */
    public static final AnimationDefinition PHASE_TRANSITION = AnimationDefinition.Builder.withLength(3.0F)
            .addAnimation("torso", rot(SMOOTH, 0.0F, 3, 0, 0, 0.7F, 22, -8, 4, 1.4F, -22, 10, -4, 2.2F, -16, -8, 3, 3.0F, 3, 0, 0))
            .addAnimation("head", rot(SMOOTH, 0.0F, -3, 0, 0, 0.7F, 24, 10, -5, 1.4F, -28, -12, 5, 2.2F, -20, 8, -3, 3.0F, -3, 0, 0))
            .addAnimation("right_arm", rot(SMOOTH, 0.0F, -50, 0, 0, 0.7F, -60, 0, 0, 1.4F, -160, 0, 0, 2.2F, -150, 0, 0, 3.0F, -50, 0, 0))
            .addAnimation("right_forearm", rot(SMOOTH, 0.0F, -45, 0, 0, 1.4F, -20, 0, 0, 3.0F, -45, 0, 0))
            .addAnimation("scythe", rot(SMOOTH, 0.0F, 60, 0, 0, 0.7F, 100, 0, 0, 1.4F, 180, 0, 0, 2.2F, 178, 0, 0, 3.0F, 60, 0, 0))
            .addAnimation("left_arm", rot(SMOOTH, 0.0F, 8, -5, -6, 0.7F, 24, -18, -24, 1.4F, -98, 25, -42, 2.2F, -70, -18, -26, 3.0F, 8, -5, -6))
            .addAnimation("cage_door", rot(SMOOTH, 0.0F, 0, 0, 0, 1.1F, 0, 0, 0, 1.55F, 0, -115, 0, 3.0F, 0, -105, 0))
            .addAnimation("cage_bell", rot(SMOOTH, 0.0F, 0, 0, 0, 0.5F, 26, 0, 24, 1.0F, -28, 0, -26, 1.5F, 32, 0, 30, 2.1F, -26, 0, -24, 3.0F, 0, 0, 0))
            .addAnimation("chains", rot(SMOOTH, 0.0F, 0, 0, 0, 0.8F, -16, 0, 14, 1.6F, 20, 0, -18, 2.4F, -12, 0, 10, 3.0F, 0, 0, 0))
            .build();

    /** Rage (3.0s): the final roar: trembling with fury, axe raised, hunched then thrown back. */
    public static final AnimationDefinition RAGE = AnimationDefinition.Builder.withLength(3.0F)
            .addAnimation("pelvis", pos(LINEAR, 0.0F, 0, 0, 0, 0.5F, 0, 2.0F, 0, 1.2F, 0, 0, 0, 2.4F, 0, 0, 0, 3.0F, 0, 0, 0))
            .addAnimation("torso", rot(LINEAR, 0.0F, 3, 0, 0, 0.5F, 36, 0, 0, 1.0F, -24, 0, 0, 1.2F, -22, 2, 2, 1.4F, -24, -2, -2, 1.6F, -22, 2, 2,
                    1.8F, -24, -2, -2, 2.0F, -22, 2, 2, 2.4F, -20, 0, 0, 3.0F, 3, 0, 0))
            .addAnimation("head", rot(LINEAR, 0.0F, -3, 0, 0, 0.5F, 30, 0, 0, 1.0F, -34, 0, 0, 2.4F, -30, 0, 0, 3.0F, -3, 0, 0))
            .addAnimation("right_arm", rot(LINEAR, 0.0F, -50, 0, 0, 0.5F, -30, 0, 0, 1.0F, -160, 0, 0, 2.4F, -158, 0, 0, 3.0F, -50, 0, 0))
            .addAnimation("right_forearm", rot(LINEAR, 0.0F, -45, 0, 0, 1.0F, -20, 0, 0, 3.0F, -45, 0, 0))
            .addAnimation("scythe", rot(LINEAR, 0.0F, 60, 0, 0, 0.5F, 150, 0, 0, 1.0F, 180, 0, 0, 2.4F, 180, 0, 0, 3.0F, 60, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0.0F, 8, -5, -6, 0.5F, 30, -10, -20, 1.0F, -110, 30, -40, 2.4F, -108, 30, -40, 3.0F, 8, -5, -6))
            .addAnimation("cage_door", rot(LINEAR, 0.0F, 0, -105, 0, 3.0F, 0, -105, 0))
            .addAnimation("cage_bell", rot(LINEAR, 0.0F, 0, 0, 0, 0.6F, 30, 0, 28, 1.0F, -30, 0, -28, 1.4F, 30, 0, 28, 1.8F, -30, 0, -28, 2.4F, 24, 0, 22, 3.0F, 0, 0, 0))
            .addAnimation("mantle", rot(LINEAR, 0.0F, 0, 0, 0, 1.0F, 18, 0, 6, 1.6F, 10, 0, -6, 3.0F, 0, 0, 0))
            .build();

    /** Brief upper-body recoil when damaged (0.35s). */
    public static final AnimationDefinition HURT = AnimationDefinition.Builder.withLength(0.35F)
            .addAnimation("torso", rot(LINEAR, 0.0F, 3, 0, 0, 0.12F, -9, 6, -4, 0.35F, 3, 0, 0))
            .addAnimation("head", rot(LINEAR, 0.0F, -3, 0, 0, 0.12F, -14, -8, 5, 0.35F, -3, 0, 0))
            .addAnimation("cage_bell", rot(LINEAR, 0.0F, 0, 0, 0, 0.12F, 16, 0, 14, 0.35F, 0, 0, 0))
            .build();

    /** 3.5s collapse: drops to one knee, lets go of the axe, and bows his head before being wrapped as a gift. */
    public static final AnimationDefinition DEATH = AnimationDefinition.Builder.withLength(3.5F)
            .addAnimation("pelvis", pos(LINEAR, 0.0F, 0, 0, 0, 1.1F, 0, 7.0F, 1.0F, 2.3F, 0, 11.5F, 2.0F, 3.5F, 0, 12.5F, 2.5F))
            .addAnimation("torso", rot(LINEAR, 0.0F, 3, 0, 0, 1.1F, 24, -6, 4, 2.3F, 44, -8, 6, 3.5F, 52, -8, 8))
            .addAnimation("head", rot(LINEAR, 0.0F, -3, 0, 0, 1.1F, -14, 8, 0, 2.3F, 28, -4, 6, 3.5F, 38, -6, 8))
            .addAnimation("right_arm", rot(LINEAR, 0.0F, -50, 0, 0, 1.1F, -42, 12, 14, 2.3F, -24, 6, 18, 3.5F, -18, 4, 22))
            .addAnimation("right_forearm", rot(LINEAR, 0.0F, -45, 0, 0, 1.1F, -20, 0, 0, 3.5F, -6, 0, 0))
            .addAnimation("scythe", rot(LINEAR, 0.0F, 60, 0, 0, 1.1F, 150, 0, 0, 2.3F, 175, 0, 0, 3.5F, 178, 0, 0))
            .addAnimation("left_arm", rot(LINEAR, 0.0F, 8, -5, -6, 1.1F, 18, -10, -14, 2.3F, 36, -6, -18, 3.5F, 42, -4, -20))
            .addAnimation("right_leg", rot(LINEAR, 0.0F, 0, 0, 0, 1.1F, -58, 0, 0, 2.3F, -78, 0, 0, 3.5F, -82, 0, 0))
            .addAnimation("right_boot", rot(LINEAR, 0.0F, 0, 0, 0, 1.1F, 52, 0, 0, 2.3F, 72, 0, 0, 3.5F, 76, 0, 0))
            .addAnimation("left_leg", rot(LINEAR, 0.0F, 0, 0, 0, 1.1F, 28, 0, 0, 2.3F, 56, 0, 0, 3.5F, 62, 0, 0))
            .addAnimation("left_boot", rot(LINEAR, 0.0F, 0, 0, 0, 1.1F, 34, 0, 0, 2.3F, 58, 0, 0, 3.5F, 64, 0, 0))
            .addAnimation("cage_bell", rot(LINEAR, 0.0F, 0, 0, 0, 0.8F, 24, 0, 18, 1.8F, -16, 0, -12, 3.5F, 18, 0, 6))
            .build();

    private LastGiftCollectorAnimations() {}

    private static AnimationChannel rot(AnimationChannel.Interpolation interp, float... data) {
        Keyframe[] frames = new Keyframe[data.length / 4];
        for (int i = 0; i < frames.length; i++) {
            int b = i * 4;
            frames[i] = new Keyframe(data[b],
                    KeyframeAnimations.degreeVec(data[b + 1], data[b + 2], data[b + 3]), interp);
        }
        return new AnimationChannel(AnimationChannel.Targets.ROTATION, frames);
    }

    private static AnimationChannel pos(AnimationChannel.Interpolation interp, float... data) {
        Keyframe[] frames = new Keyframe[data.length / 4];
        for (int i = 0; i < frames.length; i++) {
            int b = i * 4;
            frames[i] = new Keyframe(data[b],
                    KeyframeAnimations.posVec(data[b + 1], data[b + 2], data[b + 3]), interp);
        }
        return new AnimationChannel(AnimationChannel.Targets.POSITION, frames);
    }
}
