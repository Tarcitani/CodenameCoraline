package com.codenamecoraline.client.salamander;

import com.codenamecoraline.CodenameCoraline;
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
 * Articulated sprawled amphibian model for the Brimstone Salamander (Salamandra de Enxofre):
 * - Low-slung trunk (body) with layered basalt carapace (CubeDeformation 0.25F)
 * - Asymmetric volcanic dorsal ridge (5 jagged plates of distinct heights and offsets)
 * - Inflatable emissive bellows throat sac (throat_sac, CubeDeformation 0.30F)
 * - Articulated neck, broad triangular cranium with asymmetric ocular horns, upper jaw and hinged lower jaw with forked tongue
 * - Four two-segment sprawled legs (left/right_front_leg -> paw; left/right_hind_leg -> paw)
 * - Three-segment vertically compressed paddle tail (tail_base -> tail_mid -> tail_tip)
 *
 * Rotation convention in Minecraft 26.1.2: ModelPart.translateAndRotate delegates to
 * PoseStack.rotateZYX(zRot, yRot, xRot), using radians in PartPose and degrees in KeyframeAnimations.
 */
public final class BrimstoneSalamanderModel extends EntityModel<BrimstoneSalamanderRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "brimstone_salamander"), "main");

    private final ModelPart body;
    private final ModelPart dorsalRidge;
    private final ModelPart throatSac;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart upperJaw;
    private final ModelPart lowerJaw;
    private final ModelPart leftFrontLeg;
    private final ModelPart leftFrontPaw;
    private final ModelPart rightFrontLeg;
    private final ModelPart rightFrontPaw;
    private final ModelPart leftHindLeg;
    private final ModelPart leftHindPaw;
    private final ModelPart rightHindLeg;
    private final ModelPart rightHindPaw;
    private final ModelPart tailBase;
    private final ModelPart tailMid;
    private final ModelPart tailTip;

    private final KeyframeAnimation idleBreatheAnim;
    private final KeyframeAnimation idleThroatPuffAnim;
    private final KeyframeAnimation idleTailSwayAnim;
    private final KeyframeAnimation idleSniffAnim;
    private final KeyframeAnimation walkAnim;
    private final KeyframeAnimation swimLavaAnim;
    private final KeyframeAnimation snapBitePrepAnim;
    private final KeyframeAnimation snapBiteExecAnim;
    private final KeyframeAnimation snapBiteRecoveryAnim;
    private final KeyframeAnimation sulfurSpitPrepAnim;
    private final KeyframeAnimation sulfurSpitExecAnim;
    private final KeyframeAnimation sulfurSpitRecoveryAnim;
    private final KeyframeAnimation tailWhipPrepAnim;
    private final KeyframeAnimation tailWhipExecAnim;
    private final KeyframeAnimation tailWhipRecoveryAnim;
    private final KeyframeAnimation hurtAnim;
    private final KeyframeAnimation deathAnim;

    public BrimstoneSalamanderModel(ModelPart root) {
        super(root);
        var lookup = root.createPartLookup();
        this.body = lookup.apply("body");
        this.dorsalRidge = lookup.apply("dorsal_ridge");
        this.throatSac = lookup.apply("throat_sac");
        this.neck = lookup.apply("neck");
        this.head = lookup.apply("head");
        this.upperJaw = lookup.apply("upper_jaw");
        this.lowerJaw = lookup.apply("lower_jaw");
        this.leftFrontLeg = lookup.apply("left_front_leg");
        this.leftFrontPaw = lookup.apply("left_front_paw");
        this.rightFrontLeg = lookup.apply("right_front_leg");
        this.rightFrontPaw = lookup.apply("right_front_paw");
        this.leftHindLeg = lookup.apply("left_hind_leg");
        this.leftHindPaw = lookup.apply("left_hind_paw");
        this.rightHindLeg = lookup.apply("right_hind_leg");
        this.rightHindPaw = lookup.apply("right_hind_paw");
        this.tailBase = lookup.apply("tail_base");
        this.tailMid = lookup.apply("tail_mid");
        this.tailTip = lookup.apply("tail_tip");

        this.idleBreatheAnim = BrimstoneSalamanderAnimations.IDLE_BREATHE.bake(root);
        this.idleThroatPuffAnim = BrimstoneSalamanderAnimations.IDLE_THROAT_PUFF.bake(root);
        this.idleTailSwayAnim = BrimstoneSalamanderAnimations.IDLE_TAIL_SWAY.bake(root);
        this.idleSniffAnim = BrimstoneSalamanderAnimations.IDLE_SNIFF.bake(root);
        this.walkAnim = BrimstoneSalamanderAnimations.WALK.bake(root);
        this.swimLavaAnim = BrimstoneSalamanderAnimations.SWIM_LAVA.bake(root);
        this.snapBitePrepAnim = BrimstoneSalamanderAnimations.SNAP_BITE_PREP.bake(root);
        this.snapBiteExecAnim = BrimstoneSalamanderAnimations.SNAP_BITE_EXEC.bake(root);
        this.snapBiteRecoveryAnim = BrimstoneSalamanderAnimations.SNAP_BITE_RECOVERY.bake(root);
        this.sulfurSpitPrepAnim = BrimstoneSalamanderAnimations.SULFUR_SPIT_PREP.bake(root);
        this.sulfurSpitExecAnim = BrimstoneSalamanderAnimations.SULFUR_SPIT_EXEC.bake(root);
        this.sulfurSpitRecoveryAnim = BrimstoneSalamanderAnimations.SULFUR_SPIT_RECOVERY.bake(root);
        this.tailWhipPrepAnim = BrimstoneSalamanderAnimations.TAIL_WHIP_PREP.bake(root);
        this.tailWhipExecAnim = BrimstoneSalamanderAnimations.TAIL_WHIP_EXEC.bake(root);
        this.tailWhipRecoveryAnim = BrimstoneSalamanderAnimations.TAIL_WHIP_RECOVERY.bake(root);
        this.hurtAnim = BrimstoneSalamanderAnimations.HURT.bake(root);
        this.deathAnim = BrimstoneSalamanderAnimations.DEATH.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Low-slung sprawled amphibian trunk (pivot at y = 19.0, 5 px above ground y = 24.0) + layered basalt carapace.
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, -3.0F, -7.0F, 10.0F, 6.0F, 14.0F)
                        .texOffs(50, 0).addBox(-5.0F, -3.2F, -6.0F, 10.0F, 4.0F, 12.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 19.0F, 0.0F));

        // Five asymmetric volcanic basalt plates along the spine, breaking lateral and vertical symmetry.
        body.addOrReplaceChild("dorsal_ridge",
                CubeListBuilder.create()
                        .texOffs(0, 22).addBox(-1.8F, -2.6F, -6.0F, 2.0F, 3.0F, 3.0F)
                        .texOffs(12, 22).addBox(0.2F, -4.6F, -2.5F, 2.0F, 5.0F, 4.0F)
                        .texOffs(26, 22).addBox(-2.2F, -3.6F, 1.5F, 3.0F, 4.0F, 4.0F)
                        .texOffs(42, 22).addBox(0.4F, -3.4F, 5.2F, 2.0F, 4.0F, 3.0F)
                        .texOffs(54, 22).addBox(-1.2F, -2.4F, 8.2F, 2.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));

        // Inflatable glowing bellows sac beneath the throat and anterior chest (CubeDeformation 0.30F).
        body.addOrReplaceChild("throat_sac",
                CubeListBuilder.create()
                        .texOffs(96, 0).addBox(-3.0F, -0.8F, -4.5F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.30F)),
                PartPose.offset(0.0F, 1.5F, -6.5F));

        // Muscular cervical neck joint.
        PartDefinition neck = body.addOrReplaceChild("neck",
                CubeListBuilder.create()
                        .texOffs(66, 22).addBox(-3.5F, -2.5F, -4.5F, 7.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -0.5F, -7.0F, -0.06F, 0.0F, 0.0F));

        // Broad flattened triangular cranium with asymmetric basalt brow horns.
        PartDefinition head = neck.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 34).addBox(-4.5F, -2.2F, -5.5F, 9.0F, 4.0F, 6.0F)
                        .texOffs(32, 34).addBox(2.2F, -3.6F, -4.5F, 2.0F, 2.0F, 4.0F)
                        .texOffs(46, 34).addBox(-4.0F, -3.0F, -4.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -0.4F, -4.2F, 0.06F, 0.0F, 0.0F));

        // Upper snout and nostrils.
        head.addOrReplaceChild("upper_jaw",
                CubeListBuilder.create()
                        .texOffs(58, 34).addBox(-3.5F, -1.2F, -4.8F, 7.0F, 2.0F, 5.0F),
                PartPose.offset(0.0F, -0.4F, -5.2F));

        // Hinged lower mandible + incandescent forked tongue inside the mouth.
        head.addOrReplaceChild("lower_jaw",
                CubeListBuilder.create()
                        .texOffs(84, 34).addBox(-3.5F, -0.2F, -5.0F, 7.0F, 2.0F, 5.0F)
                        .texOffs(110, 34).addBox(-1.5F, -0.6F, -4.6F, 3.0F, 1.0F, 4.0F),
                PartPose.offset(0.0F, 1.0F, -4.8F));

        // Sprawled left front leg (shoulder -> elbow/paw planted at y = 24.0).
        PartDefinition leftFrontLeg = body.addOrReplaceChild("left_front_leg",
                CubeListBuilder.create()
                        .texOffs(0, 46).addBox(-0.5F, -1.5F, -1.5F, 5.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(4.8F, 0.8F, -4.5F, 0.0F, 0.22F, 0.22F));

        leftFrontLeg.addOrReplaceChild("left_front_paw",
                CubeListBuilder.create()
                        .texOffs(18, 46).addBox(-1.5F, 0.0F, -2.2F, 4.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(3.8F, 0.4F, 0.0F, 0.0F, -0.10F, -0.22F));

        // Sprawled right front leg (shoulder -> elbow/paw planted at y = 24.0).
        PartDefinition rightFrontLeg = body.addOrReplaceChild("right_front_leg",
                CubeListBuilder.create()
                        .texOffs(36, 46).addBox(-4.5F, -1.5F, -1.5F, 5.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(-4.8F, 0.8F, -4.5F, 0.0F, -0.22F, -0.22F));

        rightFrontLeg.addOrReplaceChild("right_front_paw",
                CubeListBuilder.create()
                        .texOffs(54, 46).addBox(-2.5F, 0.0F, -2.2F, 4.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(-3.8F, 0.4F, 0.0F, 0.0F, 0.10F, 0.22F));

        // Sprawled left hind leg (hip -> knee/paw planted at y = 24.0).
        PartDefinition leftHindLeg = body.addOrReplaceChild("left_hind_leg",
                CubeListBuilder.create()
                        .texOffs(72, 46).addBox(-0.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F),
                PartPose.offsetAndRotation(4.8F, 0.8F, 4.5F, 0.0F, -0.24F, 0.22F));

        leftHindLeg.addOrReplaceChild("left_hind_paw",
                CubeListBuilder.create()
                        .texOffs(92, 46).addBox(-1.5F, 0.0F, -2.8F, 4.0F, 4.0F, 5.0F),
                PartPose.offsetAndRotation(3.8F, 0.4F, 0.0F, 0.0F, 0.12F, -0.22F));

        // Sprawled right hind leg (hip -> knee/paw planted at y = 24.0).
        PartDefinition rightHindLeg = body.addOrReplaceChild("right_hind_leg",
                CubeListBuilder.create()
                        .texOffs(0, 58).addBox(-4.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F),
                PartPose.offsetAndRotation(-4.8F, 0.8F, 4.5F, 0.0F, 0.24F, -0.22F));

        rightHindLeg.addOrReplaceChild("right_hind_paw",
                CubeListBuilder.create()
                        .texOffs(20, 58).addBox(-2.5F, 0.0F, -2.8F, 4.0F, 4.0F, 5.0F),
                PartPose.offsetAndRotation(-3.8F, 0.4F, 0.0F, 0.0F, -0.12F, 0.22F));

        // Three-segment vertically compressed paddle tail (tail_base -> tail_mid -> tail_tip).
        PartDefinition tailBase = body.addOrReplaceChild("tail_base",
                CubeListBuilder.create()
                        .texOffs(40, 58).addBox(-3.0F, -2.5F, 0.0F, 6.0F, 5.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, -0.2F, 6.8F, -0.05F, 0.0F, 0.0F));

        PartDefinition tailMid = tailBase.addOrReplaceChild("tail_mid",
                CubeListBuilder.create()
                        .texOffs(68, 58).addBox(-2.0F, -3.2F, 0.0F, 4.0F, 6.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 0.1F, 6.5F, 0.03F, 0.0F, 0.0F));

        tailMid.addOrReplaceChild("tail_tip",
                CubeListBuilder.create()
                        .texOffs(94, 58).addBox(-1.0F, -3.6F, 0.0F, 2.0F, 7.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 7.5F, 0.02F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(BrimstoneSalamanderRenderState state) {
        super.setupAnim(state);
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (state.deathAnimationState.isStarted()) {
            this.deathAnim.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        // Head tracking
        this.neck.yRot += state.yRot * Mth.DEG_TO_RAD * 0.35F;
        this.head.yRot += state.yRot * Mth.DEG_TO_RAD * 0.65F;
        this.head.xRot += state.xRot * Mth.DEG_TO_RAD * 0.45F;

        // Ground crawl when moving and not swimming in lava
        if (!state.swimmingInLava && state.walkAnimationSpeed > 0.01F) {
            float walkSpeed = state.isBaby ? 2.5F : 2.0F;
            this.walkAnim.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, walkSpeed, 2.5F);
        }

        // Keyframe animations
        this.idleBreatheAnim.apply(state.idleBreatheAnimationState, state.ageInTicks);
        this.idleThroatPuffAnim.apply(state.idleThroatPuffAnimationState, state.ageInTicks);
        this.idleTailSwayAnim.apply(state.idleTailSwayAnimationState, state.ageInTicks);
        this.idleSniffAnim.apply(state.idleSniffAnimationState, state.ageInTicks);
        this.swimLavaAnim.apply(state.swimLavaAnimationState, state.ageInTicks);
        this.snapBitePrepAnim.apply(state.snapBitePrepAnimationState, state.ageInTicks);
        this.snapBiteExecAnim.apply(state.snapBiteExecAnimationState, state.ageInTicks);
        this.snapBiteRecoveryAnim.apply(state.snapBiteRecoveryAnimationState, state.ageInTicks);
        this.sulfurSpitPrepAnim.apply(state.sulfurSpitPrepAnimationState, state.ageInTicks);
        this.sulfurSpitExecAnim.apply(state.sulfurSpitExecAnimationState, state.ageInTicks);
        this.sulfurSpitRecoveryAnim.apply(state.sulfurSpitRecoveryAnimationState, state.ageInTicks);
        this.tailWhipPrepAnim.apply(state.tailWhipPrepAnimationState, state.ageInTicks);
        this.tailWhipExecAnim.apply(state.tailWhipExecAnimationState, state.ageInTicks);
        this.tailWhipRecoveryAnim.apply(state.tailWhipRecoveryAnimationState, state.ageInTicks);
        this.hurtAnim.apply(state.hurtAnimationState, state.ageInTicks);

        // Juvenile neotenic proportions
        if (state.isBaby) {
            this.head.xScale = 1.15F;
            this.head.yScale = 1.15F;
            this.head.zScale = 1.15F;
            this.dorsalRidge.xScale = 0.4F;
            this.dorsalRidge.yScale = 0.4F;
            this.dorsalRidge.zScale = 0.4F;
            this.throatSac.xScale = 0.6F;
            this.throatSac.yScale = 0.6F;
            this.throatSac.zScale = 0.6F;
        }
    }
}

