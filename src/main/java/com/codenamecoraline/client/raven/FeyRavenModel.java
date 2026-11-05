package com.codenamecoraline.client.raven;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.raven.FeyRaven;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Articulated corvid model for the Fey Raven (Corvo Feerico):
 * - Diagonal perched body with layered breast ruff (CubeDeformation 0.25F)
 * - Articulated neck, skull, third-eye crest (CubeDeformation 0.25F), hooked upper beak and hinged lower beak
 * - Two-segment articulated wings (inner coverts + outer primaries)
 * - Three-segment wedge tail fan (tail_base -> tail_mid -> tail_tip)
 * - Two-segment legs (thigh -> tarsus -> talons) with a messenger scroll tied to the left tarsus
 *
 * Rotation convention in Minecraft 26.1.2: ModelPart.translateAndRotate delegates to
 * PoseStack.rotateZYX(zRot, yRot, xRot), using radians in PartPose and degrees in KeyframeAnimations.
 */
public final class FeyRavenModel extends EntityModel<FeyRavenRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "fey_raven"), "main");

    private final ModelPart body;
    private final ModelPart breastRuff;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart thirdEyeCrest;
    private final ModelPart upperBeak;
    private final ModelPart lowerBeak;
    private final ModelPart beakBerries;
    private final ModelPart leftWingInner;
    private final ModelPart leftWingOuter;
    private final ModelPart rightWingInner;
    private final ModelPart rightWingOuter;
    private final ModelPart tailBase;
    private final ModelPart tailMid;
    private final ModelPart tailTip;
    private final ModelPart leftThigh;
    private final ModelPart leftTarsus;
    private final ModelPart messengerScroll;
    private final ModelPart leftTalons;
    private final ModelPart rightThigh;
    private final ModelPart rightTarsus;
    private final ModelPart rightTalons;

    private final KeyframeAnimation idleBreatheAnim;
    private final KeyframeAnimation idleHeadTiltAnim;
    private final KeyframeAnimation idlePreenAnim;
    private final KeyframeAnimation idleCawAnim;
    private final KeyframeAnimation walkAnim;
    private final KeyframeAnimation flyFlapAnim;
    private final KeyframeAnimation flyGlideAnim;
    private final KeyframeAnimation peckAttackAnim;
    private final KeyframeAnimation hurtAnim;
    private final KeyframeAnimation deathAnim;

    public FeyRavenModel(ModelPart root) {
        super(root);
        var lookup = root.createPartLookup();
        this.body = lookup.apply("body");
        this.breastRuff = lookup.apply("breast_ruff");
        this.neck = lookup.apply("neck");
        this.head = lookup.apply("head");
        this.thirdEyeCrest = lookup.apply("third_eye_crest");
        this.upperBeak = lookup.apply("upper_beak");
        this.lowerBeak = lookup.apply("lower_beak");
        this.beakBerries = lookup.apply("beak_berries");
        this.leftWingInner = lookup.apply("left_wing_inner");
        this.leftWingOuter = lookup.apply("left_wing_outer");
        this.rightWingInner = lookup.apply("right_wing_inner");
        this.rightWingOuter = lookup.apply("right_wing_outer");
        this.tailBase = lookup.apply("tail_base");
        this.tailMid = lookup.apply("tail_mid");
        this.tailTip = lookup.apply("tail_tip");
        this.leftThigh = lookup.apply("left_thigh");
        this.leftTarsus = lookup.apply("left_tarsus");
        this.messengerScroll = lookup.apply("messenger_scroll");
        this.leftTalons = lookup.apply("left_talons");
        this.rightThigh = lookup.apply("right_thigh");
        this.rightTarsus = lookup.apply("right_tarsus");
        this.rightTalons = lookup.apply("right_talons");

        this.idleBreatheAnim = FeyRavenAnimations.IDLE_BREATHE.bake(root);
        this.idleHeadTiltAnim = FeyRavenAnimations.IDLE_HEAD_TILT.bake(root);
        this.idlePreenAnim = FeyRavenAnimations.IDLE_PREEN.bake(root);
        this.idleCawAnim = FeyRavenAnimations.IDLE_CAW.bake(root);
        this.walkAnim = FeyRavenAnimations.WALK.bake(root);
        this.flyFlapAnim = FeyRavenAnimations.FLY_FLAP.bake(root);
        this.flyGlideAnim = FeyRavenAnimations.FLY_GLIDE.bake(root);
        this.peckAttackAnim = FeyRavenAnimations.PECK_ATTACK.bake(root);
        this.hurtAnim = FeyRavenAnimations.HURT.bake(root);
        this.deathAnim = FeyRavenAnimations.DEATH.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Diagonal perched corvid torso (pivot at hips y = 18.0, tilted +22 deg forward).
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5F, -5.5F, -2.5F, 5.0F, 7.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.38F, 0.0F, 0.0F));

        // Layered ruffled chest plumage with silver-cyan runes (CubeDeformation 0.25F prevents z-fighting).
        body.addOrReplaceChild("breast_ruff",
                CubeListBuilder.create()
                        .texOffs(20, 0).addBox(-2.5F, -1.5F, -2.2F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offsetAndRotation(0.0F, -4.2F, -0.8F, -0.08F, 0.0F, 0.0F));

        // Cervical neck joint rising from the upper chest.
        PartDefinition neck = body.addOrReplaceChild("neck",
                CubeListBuilder.create()
                        .texOffs(38, 0).addBox(-2.0F, -2.6F, -2.0F, 4.0F, 3.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -5.0F, -1.0F, -0.24F, 0.0F, 0.0F));

        // Corvid skull with lateral eyes and raised third-eye crest on the center of the brow.
        PartDefinition head = neck.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-2.0F, -3.5F, -3.2F, 4.0F, 4.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -2.2F, -0.3F, -0.14F, 0.0F, 0.0F));

        head.addOrReplaceChild("third_eye_crest",
                CubeListBuilder.create()
                        .texOffs(18, 12).addBox(-1.0F, -1.5F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.15F)),
                PartPose.offset(0.0F, -2.4F, -3.0F));

        // Heavy slightly hooked upper raven mandible.
        head.addOrReplaceChild("upper_beak",
                CubeListBuilder.create()
                        .texOffs(32, 12).addBox(-1.0F, -0.6F, -3.8F, 2.0F, 1.0F, 4.0F)
                        .texOffs(44, 12).addBox(-0.5F, 0.1F, -4.4F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -1.8F, -3.0F, 0.06F, 0.0F, 0.0F));

        // Hinged lower mandible with optional glow-berry twig held in the beak tip.
        PartDefinition lowerBeak = head.addOrReplaceChild("lower_beak",
                CubeListBuilder.create()
                        .texOffs(48, 12).addBox(-1.0F, -0.2F, -3.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, -0.9F, -3.0F));

        lowerBeak.addOrReplaceChild("beak_berries",
                CubeListBuilder.create()
                        .texOffs(48, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.6F, -2.4F));

        // Two-segment left wing: inner rune-etched coverts + outer pointed primary remiges sloping along the back.
        PartDefinition leftWingInner = body.addOrReplaceChild("left_wing_inner",
                CubeListBuilder.create()
                        .texOffs(0, 21).addBox(-0.2F, -0.5F, -0.5F, 1.0F, 4.0F, 6.0F),
                PartPose.offsetAndRotation(2.5F, -4.6F, -1.2F, -0.72F, 0.05F, -0.06F));

        leftWingInner.addOrReplaceChild("left_wing_outer",
                CubeListBuilder.create()
                        .texOffs(14, 21).addBox(-0.1F, -0.5F, 0.0F, 1.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.2F, 5.0F, -0.08F, -0.06F, 0.0F));

        // Two-segment right wing: inner rune-etched coverts + outer pointed primary remiges sloping along the back.
        PartDefinition rightWingInner = body.addOrReplaceChild("right_wing_inner",
                CubeListBuilder.create()
                        .texOffs(28, 21).addBox(-0.8F, -0.5F, -0.5F, 1.0F, 4.0F, 6.0F),
                PartPose.offsetAndRotation(-2.5F, -4.6F, -1.2F, -0.72F, -0.05F, 0.06F));

        rightWingInner.addOrReplaceChild("right_wing_outer",
                CubeListBuilder.create()
                        .texOffs(42, 21).addBox(-0.9F, -0.5F, 0.0F, 1.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.2F, 5.0F, -0.08F, 0.06F, 0.0F));

        // Three-segment wedge tail fan (tail_base -> tail_mid -> tail_tip) sloping smoothly behind the rump.
        PartDefinition tailBase = body.addOrReplaceChild("tail_base",
                CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.8F, 1.8F, -0.68F, 0.0F, 0.0F));

        PartDefinition tailMid = tailBase.addOrReplaceChild("tail_mid",
                CubeListBuilder.create()
                        .texOffs(16, 32).addBox(-2.5F, -0.5F, 0.0F, 5.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 3.4F, 0.04F, 0.0F, 0.0F));

        tailMid.addOrReplaceChild("tail_tip",
                CubeListBuilder.create()
                        .texOffs(34, 32).addBox(-3.0F, -0.5F, 0.0F, 6.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 3.4F, 0.04F, 0.0F, 0.0F));

        // Two-segment left leg + talons + leather-bound messenger scroll tied to the left tarsus.
        PartDefinition leftThigh = body.addOrReplaceChild("left_thigh",
                CubeListBuilder.create()
                        .texOffs(0, 38).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(1.4F, 0.6F, -0.2F, -0.52F, -0.08F, 0.0F));

        PartDefinition leftTarsus = leftThigh.addOrReplaceChild("left_tarsus",
                CubeListBuilder.create()
                        .texOffs(6, 38).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 2.7F, 0.2F, 0.28F, 0.0F, 0.0F));

        leftTarsus.addOrReplaceChild("messenger_scroll",
                CubeListBuilder.create()
                        .texOffs(40, 38).addBox(-0.8F, -1.0F, -2.0F, 2.0F, 2.0F, 4.0F),
                PartPose.offsetAndRotation(0.7F, 0.6F, 0.0F, 0.22F, 0.12F, -0.20F));

        leftTarsus.addOrReplaceChild("left_talons",
                CubeListBuilder.create()
                        .texOffs(10, 38).addBox(-1.0F, 0.0F, -2.2F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 2.7F, -0.2F, -0.14F, 0.0F, 0.0F));

        // Two-segment right leg + talons.
        PartDefinition rightThigh = body.addOrReplaceChild("right_thigh",
                CubeListBuilder.create()
                        .texOffs(20, 38).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(-1.4F, 0.6F, -0.2F, -0.52F, 0.08F, 0.0F));

        PartDefinition rightTarsus = rightThigh.addOrReplaceChild("right_tarsus",
                CubeListBuilder.create()
                        .texOffs(26, 38).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 2.7F, 0.2F, 0.28F, 0.0F, 0.0F));

        rightTarsus.addOrReplaceChild("right_talons",
                CubeListBuilder.create()
                        .texOffs(30, 38).addBox(-1.0F, 0.0F, -2.2F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 2.7F, -0.2F, -0.14F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(FeyRavenRenderState state) {
        super.setupAnim(state);
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.messengerScroll.visible = state.hasScroll || state.tamed;
        this.beakBerries.visible = state.holdingBerries;

        if (state.deathAnimationState.isStarted()) {
            this.deathAnim.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        // Head look tracking
        this.neck.yRot += state.yRot * Mth.DEG_TO_RAD * 0.35F;
        this.head.yRot += state.yRot * Mth.DEG_TO_RAD * 0.65F;
        this.head.xRot += state.xRot * Mth.DEG_TO_RAD * 0.55F;

        // Sitting / perched crouch adjustment
        if (state.sitting || state.state == FeyRaven.State.SITTING
                || state.state == FeyRaven.State.SENTINEL
                || state.state == FeyRaven.State.PERCHED_ON_SHOULDER) {
            this.body.y += 1.4F;
            this.body.xRot -= 0.08F;
            this.leftThigh.xRot -= 0.35F;
            this.leftTarsus.xRot += 0.42F;
            this.rightThigh.xRot -= 0.35F;
            this.rightTarsus.xRot += 0.42F;
        }

        // Ground hop/walk cycle when moving on the ground
        if (!state.airborne && state.walkAnimationSpeed > 0.01F) {
            this.walkAnim.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 1.8F, 2.5F);
        }

        // Keyframe animations
        this.idleBreatheAnim.apply(state.idleBreatheAnimationState, state.ageInTicks);
        this.idleHeadTiltAnim.apply(state.idleHeadTiltAnimationState, state.ageInTicks);
        this.idlePreenAnim.apply(state.idlePreenAnimationState, state.ageInTicks);
        this.idleCawAnim.apply(state.idleCawAnimationState, state.ageInTicks);
        this.flyFlapAnim.apply(state.flyFlapAnimationState, state.ageInTicks);
        this.flyGlideAnim.apply(state.flyGlideAnimationState, state.ageInTicks);
        this.peckAttackAnim.apply(state.peckAttackAnimationState, state.ageInTicks);
        this.hurtAnim.apply(state.hurtAnimationState, state.ageInTicks);
    }
}
