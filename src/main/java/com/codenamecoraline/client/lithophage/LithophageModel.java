package com.codenamecoraline.client.lithophage;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.lithophage.Lithophage;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Articulated humanoid mineral model for the Lithophage (~2.1 blocks tall):
 * broad robust torso, asymmetric shoulders, long heavy arms, short sturdy legs,
 * weathered clay mask head with narrow amber fissures, and dorsal/shoulder mineral crystals.
 */
public final class LithophageModel extends EntityModel<LithophageRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "lithophage"), "main");

    private static final long SHARD_RELEASE_MS = Lithophage.SHARD_WINDUP_TICKS * 50L;

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart rightForearm;
    private final ModelPart heldShard;
    private final ModelPart leftArm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart rightShin;
    private final ModelPart leftLeg;
    private final ModelPart leftShin;
    private final ModelPart dorsalCrystals;
    private final ModelPart rightShoulderCrystals;
    private final ModelPart leftShoulderCrystals;

    private final KeyframeAnimation idleAnim;
    private final KeyframeAnimation idleAlternateAnim;
    private final KeyframeAnimation awakeningAnim;
    private final KeyframeAnimation alertAnim;
    private final KeyframeAnimation walkAnim;
    private final KeyframeAnimation turnAnim;
    private final KeyframeAnimation attackSlamAnim;
    private final KeyframeAnimation attackSweepAnim;
    private final KeyframeAnimation attackShardAnim;
    private final KeyframeAnimation attackRecoveryAnim;
    private final KeyframeAnimation hurtAnim;
    private final KeyframeAnimation deathAnim;
    private final KeyframeAnimation fallAsleepAnim;

    public LithophageModel(ModelPart root) {
        super(root);
        var lookup = root.createPartLookup();
        this.body = lookup.apply("body");
        this.head = lookup.apply("head");
        this.rightArm = lookup.apply("right_arm");
        this.rightForearm = lookup.apply("right_forearm");
        this.heldShard = lookup.apply("held_shard");
        this.leftArm = lookup.apply("left_arm");
        this.leftForearm = lookup.apply("left_forearm");
        this.rightLeg = lookup.apply("right_leg");
        this.rightShin = lookup.apply("right_shin");
        this.leftLeg = lookup.apply("left_leg");
        this.leftShin = lookup.apply("left_shin");
        this.dorsalCrystals = lookup.apply("dorsal_crystals");
        this.rightShoulderCrystals = lookup.apply("right_shoulder_crystals");
        this.leftShoulderCrystals = lookup.apply("left_shoulder_crystals");

        this.idleAnim = LithophageAnimations.IDLE.bake(root);
        this.idleAlternateAnim = LithophageAnimations.IDLE_ALTERNATE.bake(root);
        this.awakeningAnim = LithophageAnimations.AWAKENING.bake(root);
        this.alertAnim = LithophageAnimations.ALERT.bake(root);
        this.walkAnim = LithophageAnimations.WALK.bake(root);
        this.turnAnim = LithophageAnimations.TURN.bake(root);
        this.attackSlamAnim = LithophageAnimations.ATTACK_SLAM.bake(root);
        this.attackSweepAnim = LithophageAnimations.ATTACK_SWEEP.bake(root);
        this.attackShardAnim = LithophageAnimations.ATTACK_SHARD.bake(root);
        this.attackRecoveryAnim = LithophageAnimations.ATTACK_RECOVERY.bake(root);
        this.hurtAnim = LithophageAnimations.HURT.bake(root);
        this.deathAnim = LithophageAnimations.DEATH.bake(root);
        this.fallAsleepAnim = LithophageAnimations.FALL_ASLEEP.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Hips and broad mineral torso (pivot at y = 14.0, growing upward to y = 1.0).
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        // Upper chest block (12 x 8 x 7)
                        .texOffs(0, 0).addBox(-6.0F, -13.0F, -3.5F, 12.0F, 8.0F, 7.0F)
                        // Lower waist/pelvis block (9 x 5 x 5)
                        .texOffs(0, 16).addBox(-4.5F, -5.0F, -2.5F, 9.0F, 5.0F, 5.0F)
                        // Recessed chest amber fissure plate (6 x 4 x 1)
                        .texOffs(40, 0).addBox(-3.0F, -11.0F, -3.8F, 6.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));

        // Weathered clay mask head without conventional human features; narrow amber fissures in front.
        body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        // Core clay skull (7 x 8 x 6)
                        .texOffs(0, 28).addBox(-3.5F, -8.0F, -3.2F, 7.0F, 8.0F, 6.0F)
                        // Carved weathered clay face-mask plate (8 x 7 x 1)
                        .texOffs(28, 28).addBox(-4.0F, -7.5F, -4.0F, 8.0F, 7.0F, 1.0F)
                        // Asymmetric mineral crown ridges on top of the mask
                        .texOffs(48, 28).addBox(-3.2F, -10.0F, -2.5F, 3.0F, 2.0F, 4.0F)
                        .texOffs(64, 28).addBox(0.4F, -9.0F, -2.0F, 2.5F, 1.0F, 3.0F),
                PartPose.offset(0.0F, -13.0F, -0.5F));

        // Dorsal mineral crystals jutting from the upper back.
        body.addOrReplaceChild("dorsal_crystals",
                CubeListBuilder.create()
                        .texOffs(80, 20).addBox(-3.5F, -3.5F, 0.0F, 2.0F, 5.0F, 3.0F)
                        .texOffs(92, 20).addBox(1.2F, -2.5F, 0.0F, 2.0F, 4.0F, 2.0F)
                        .texOffs(102, 20).addBox(-0.8F, 0.5F, 0.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -9.5F, 3.3F, 0.22F, 0.0F, 0.0F));

        // Right arm: higher, heavier asymmetric shoulder shelf, long upper arm, heavy slag forearm/fist.
        PartDefinition rightArm = body.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        // Heavy asymmetric right shoulder shelf (5 x 4 x 6)
                        .texOffs(0, 44).addBox(-3.5F, -2.5F, -3.0F, 5.0F, 4.0F, 6.0F)
                        // Upper right arm (4 x 7 x 4)
                        .texOffs(24, 44).addBox(-2.5F, 1.5F, -2.0F, 4.0F, 7.0F, 4.0F),
                PartPose.offsetAndRotation(-6.5F, -11.5F, 0.0F, 0.08F, 0.0F, 0.08F));

        rightArm.addOrReplaceChild("right_shoulder_crystals",
                CubeListBuilder.create()
                        .texOffs(80, 0).addBox(-1.5F, -3.5F, -1.0F, 2.0F, 4.0F, 2.0F)
                        .texOffs(90, 0).addBox(0.2F, -2.5F, 0.5F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, -2.5F, 0.0F, -0.15F, 0.0F, -0.25F));

        PartDefinition rightForearm = rightArm.addOrReplaceChild("right_forearm",
                CubeListBuilder.create()
                        // Heavy club forearm & slag fist (4 x 9 x 5)
                        .texOffs(42, 44).addBox(-2.2F, 0.0F, -2.5F, 4.0F, 9.0F, 5.0F)
                        // Mineral crust ridge along outer forearm (1 x 5 x 3)
                        .texOffs(62, 44).addBox(-3.0F, 2.0F, -1.5F, 1.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(-0.5F, 8.5F, 0.0F, -0.18F, 0.0F, 0.0F));

        // Amber shard held in the right hand while preparing Estilhaço de Âmbar.
        rightForearm.addOrReplaceChild("held_shard",
                CubeListBuilder.create()
                        .texOffs(100, 0).addBox(-1.5F, -1.5F, -2.5F, 3.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, 8.5F, -1.5F));

        // Left arm: lower, sloped sediment shoulder, long heavy counter-arm.
        PartDefinition leftArm = body.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        // Sloped left shoulder crust (4 x 4 x 5)
                        .texOffs(0, 60).addBox(-1.2F, -1.5F, -2.5F, 4.0F, 4.0F, 5.0F)
                        // Upper left arm (4 x 6 x 4)
                        .texOffs(20, 60).addBox(-1.5F, 2.5F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(6.5F, -10.5F, 0.0F, 0.06F, 0.0F, -0.06F));

        leftArm.addOrReplaceChild("left_shoulder_crystals",
                CubeListBuilder.create()
                        .texOffs(80, 10).addBox(-0.5F, -2.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(1.5F, -1.5F, 0.5F, -0.10F, 0.0F, 0.22F));

        leftArm.addOrReplaceChild("left_forearm",
                CubeListBuilder.create()
                        .texOffs(38, 60).addBox(-2.0F, 0.0F, -2.2F, 4.0F, 8.0F, 4.0F),
                PartPose.offsetAndRotation(0.5F, 8.5F, 0.0F, -0.15F, 0.0F, 0.0F));

        // Short, sturdy pillar legs (pivot at y = 14.0, reaching ground at y = 24.0).
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 76).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offset(-2.8F, 14.0F, 0.0F));
        rightLeg.addOrReplaceChild("right_shin",
                CubeListBuilder.create()
                        .texOffs(20, 76).addBox(-2.2F, 0.0F, -2.6F, 4.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 5.0F, 0.0F));

        PartDefinition leftLeg = root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 90).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offset(2.8F, 14.0F, 0.0F));
        leftLeg.addOrReplaceChild("left_shin",
                CubeListBuilder.create()
                        .texOffs(20, 90).addBox(-2.2F, 0.0F, -2.6F, 4.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 5.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(LithophageRenderState state) {
        super.setupAnim(state);
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float age = state.ageInTicks + state.idleSeed;
        float walkPos = state.walkAnimationPos * 0.75F;
        float walkSpeed = Math.min(1.0F, state.walkAnimationSpeed);

        this.heldShard.visible = state.holdingShard
                || (state.attackShardAnimationState.isStarted()
                        && state.attackShardAnimationState.getTimeInMillis(state.ageInTicks) < SHARD_RELEASE_MS);

        if (state.state == Lithophage.State.DYING || state.deathAnimationState.isStarted()) {
            this.deathAnim.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        // Dormant / awakening posture interpolation: folded like a jagged stalagmite when dormant (progress = 0).
        float dormantWeight = 1.0F - Mth.clamp(state.awakeningProgress, 0.0F, 1.0F);
        if (dormantWeight > 0.001F) {
            float microShift = Mth.sin(age * 0.04F) * 0.015F * dormantWeight;
            this.body.xRot += 0.58F * dormantWeight + microShift;
            this.body.y += 2.2F * dormantWeight;
            this.head.xRot += 0.38F * dormantWeight - microShift * 0.8F;
            this.head.yRot += Mth.cos(age * 0.03F) * 0.02F * dormantWeight;
            this.rightArm.xRot -= 0.48F * dormantWeight;
            this.rightArm.zRot += 0.10F * dormantWeight + microShift;
            this.rightForearm.xRot -= 0.35F * dormantWeight;
            this.leftArm.xRot -= 0.45F * dormantWeight;
            this.leftArm.zRot -= 0.10F * dormantWeight - microShift;
            this.leftForearm.xRot -= 0.32F * dormantWeight;
            this.rightLeg.xRot -= 0.22F * dormantWeight;
            this.rightShin.xRot += 0.22F * dormantWeight;
            this.leftLeg.xRot -= 0.22F * dormantWeight;
            this.leftShin.xRot += 0.22F * dormantWeight;
        }

        float awakeWeight = Mth.clamp(state.awakeningProgress, 0.0F, 1.0F);
        this.head.yRot += state.yRot * Mth.DEG_TO_RAD * 0.75F * awakeWeight;
        this.head.xRot += state.xRot * Mth.DEG_TO_RAD * 0.65F * awakeWeight;

        boolean attackingOrRecovering = state.attackSlamAnimationState.isStarted()
                || state.attackSweepAnimationState.isStarted()
                || state.attackShardAnimationState.isStarted()
                || state.attackRecoveryAnimationState.isStarted();
        float armFreedom = attackingOrRecovering ? 0.0F : awakeWeight;

        this.animateHeavyWalk(walkPos, walkSpeed * awakeWeight, armFreedom);
        this.animateMineralBreathing(age, awakeWeight * (1.0F - walkSpeed * 0.6F));

        // Apply keyframe animations (Section 10).
        this.idleAnim.apply(state.idleAnimationState, state.ageInTicks);
        this.idleAlternateAnim.apply(state.idleAlternateAnimationState, state.ageInTicks);
        this.awakeningAnim.apply(state.awakeningAnimationState, state.ageInTicks);
        this.alertAnim.apply(state.alertAnimationState, state.ageInTicks);
        this.walkAnim.apply(state.walkAnimationState, state.ageInTicks);
        this.turnAnim.apply(state.turnAnimationState, state.ageInTicks);
        this.attackSlamAnim.apply(state.attackSlamAnimationState, state.ageInTicks);
        this.attackSweepAnim.apply(state.attackSweepAnimationState, state.ageInTicks);
        this.attackShardAnim.apply(state.attackShardAnimationState, state.ageInTicks);
        this.attackRecoveryAnim.apply(state.attackRecoveryAnimationState, state.ageInTicks);
        this.hurtAnim.apply(state.hurtAnimationState, state.ageInTicks);
        this.fallAsleepAnim.apply(state.fallAsleepAnimationState, state.ageInTicks);
    }

    private void animateHeavyWalk(float walk, float speed, float armFreedom) {
        if (speed <= 0.001F) {
            return;
        }
        float swing = Mth.cos(walk);
        float lift = Mth.sin(walk);

        this.rightLeg.xRot += swing * 0.55F * speed;
        this.leftLeg.xRot -= swing * 0.55F * speed;
        this.rightShin.xRot += Math.max(0.0F, lift) * 0.45F * speed;
        this.leftShin.xRot += Math.max(0.0F, -lift) * 0.45F * speed;

        float bob = Math.abs(lift) * 0.75F * speed;
        float roll = lift * 0.06F * speed;
        this.body.y += bob;
        this.body.zRot += roll;
        this.body.xRot += 0.08F * speed;

        this.rightArm.xRot -= swing * 0.42F * speed * armFreedom;
        this.leftArm.xRot += swing * 0.42F * speed * armFreedom;
        this.rightForearm.xRot += Mth.sin(walk + 0.7F) * 0.20F * speed * armFreedom;
        this.leftForearm.xRot += Mth.sin(walk + Mth.PI + 0.7F) * 0.20F * speed * armFreedom;
    }

    private void animateMineralBreathing(float age, float weight) {
        if (weight <= 0.001F) {
            return;
        }
        float breath = Mth.sin(age * 0.06F) * weight;
        this.body.xRot += breath * 0.02F;
        this.rightArm.zRot += (0.04F + breath * 0.02F) * weight;
        this.leftArm.zRot -= (0.04F + breath * 0.02F) * weight;
        this.dorsalCrystals.xRot += breath * 0.03F;
        this.rightShoulderCrystals.zRot -= breath * 0.025F;
        this.leftShoulderCrystals.zRot += breath * 0.025F;
    }
}

