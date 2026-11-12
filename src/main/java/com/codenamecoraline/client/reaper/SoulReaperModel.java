package com.codenamecoraline.client.reaper;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.reaper.SoulReaper;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
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
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 63-joint hierarchical model of the Reaper of Wandering Souls ({@code 256x256} UV map).
 * Spec: {@code docs/design/soul_reaper_animation.md} §§1–2.
 */
public final class SoulReaperModel extends EntityModel<SoulReaperRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "soul_reaper"), "main");

    private final ModelPart reaperRoot;
    private final ModelPart pelvis;
    private final ModelPart lowerSpine;
    private final ModelPart chest;
    private final ModelPart ribFlapsLeft;
    private final ModelPart ribFlapsRight;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart cowlRim;
    private final ModelPart cowlLeftFold;
    private final ModelPart cowlRightFold;
    private final ModelPart cowlTail1;
    private final ModelPart cowlTail2;
    private final ModelPart leftClavicle;
    private final ModelPart leftUpperArm;
    private final ModelPart leftSleeveDrape1;
    private final ModelPart leftSleeveDrape2;
    private final ModelPart leftForearm;
    private final ModelPart leftHand;
    private final ModelPart rightClavicle;
    private final ModelPart rightUpperArm;
    private final ModelPart rightSleeveDrape1;
    private final ModelPart rightSleeveDrape2;
    private final ModelPart rightForearm;
    private final ModelPart rightHand;
    private final ModelPart upperShroud;
    private final ModelPart lowerShroudRing;
    private final ModelPart frontLeftStrip1;
    private final ModelPart frontLeftStrip2;
    private final ModelPart frontLeftStrip3;
    private final ModelPart frontRightStrip1;
    private final ModelPart frontRightStrip2;
    private final ModelPart frontRightStrip3;
    private final ModelPart sideLeftStrip1;
    private final ModelPart sideLeftStrip2;
    private final ModelPart sideLeftStrip3;
    private final ModelPart sideRightStrip1;
    private final ModelPart sideRightStrip2;
    private final ModelPart sideRightStrip3;
    private final ModelPart backCenterStrip1;
    private final ModelPart backCenterStrip2;
    private final ModelPart backCenterStrip3;
    private final ModelPart spectralTail1;
    private final ModelPart spectralTail2;
    private final ModelPart waistChains;
    private final ModelPart scytheRoot;

    private static final float UPPER_ARM_LEN = 12.0F;
    private static final float FOREARM_GRIP_LEN = 13.0F;
    private static final float UPPER_GRIP_LOCAL_Y = -6.5F;
    private static final float LOWER_GRIP_LOCAL_Y = 6.5F;

    private final Map<SoulReaper.ActionId, KeyframeAnimation> actionAnimations =
            new EnumMap<>(SoulReaper.ActionId.class);
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation flickSnuffAnimation;

    public SoulReaperModel(ModelPart root) {
        super(root);
        Function<String, ModelPart> lookup = root.createPartLookup();
        this.reaperRoot = lookup.apply("reaper_root");
        this.pelvis = lookup.apply("pelvis");
        this.lowerSpine = lookup.apply("lower_spine");
        this.chest = lookup.apply("chest");
        this.ribFlapsLeft = lookup.apply("rib_flaps_left");
        this.ribFlapsRight = lookup.apply("rib_flaps_right");
        this.neck = lookup.apply("neck");
        this.head = lookup.apply("head");
        this.jaw = lookup.apply("jaw");
        this.cowlRim = lookup.apply("cowl_rim");
        this.cowlLeftFold = lookup.apply("cowl_left_fold");
        this.cowlRightFold = lookup.apply("cowl_right_fold");
        this.cowlTail1 = lookup.apply("cowl_tail_1");
        this.cowlTail2 = lookup.apply("cowl_tail_2");
        this.leftClavicle = lookup.apply("left_clavicle");
        this.leftUpperArm = lookup.apply("left_upper_arm");
        this.leftSleeveDrape1 = lookup.apply("left_Sleeve_drape_1");
        this.leftSleeveDrape2 = lookup.apply("left_sleeve_drape_2");
        this.leftForearm = lookup.apply("left_forearm");
        this.leftHand = lookup.apply("left_hand");
        this.rightClavicle = lookup.apply("right_clavicle");
        this.rightUpperArm = lookup.apply("right_upper_arm");
        this.rightSleeveDrape1 = lookup.apply("right_sleeve_drape_1");
        this.rightSleeveDrape2 = lookup.apply("right_sleeve_drape_2");
        this.rightForearm = lookup.apply("right_forearm");
        this.rightHand = lookup.apply("right_hand");
        this.upperShroud = lookup.apply("upper_shroud");
        this.lowerShroudRing = lookup.apply("lower_shroud_ring");
        this.frontLeftStrip1 = lookup.apply("front_left_strip_1");
        this.frontLeftStrip2 = lookup.apply("front_left_strip_2");
        this.frontLeftStrip3 = lookup.apply("front_left_strip_3");
        this.frontRightStrip1 = lookup.apply("front_right_strip_1");
        this.frontRightStrip2 = lookup.apply("front_right_strip_2");
        this.frontRightStrip3 = lookup.apply("front_right_strip_3");
        this.sideLeftStrip1 = lookup.apply("side_left_strip_1");
        this.sideLeftStrip2 = lookup.apply("side_left_strip_2");
        this.sideLeftStrip3 = lookup.apply("side_left_strip_3");
        this.sideRightStrip1 = lookup.apply("side_right_strip_1");
        this.sideRightStrip2 = lookup.apply("side_right_strip_2");
        this.sideRightStrip3 = lookup.apply("side_right_strip_3");
        this.backCenterStrip1 = lookup.apply("back_center_strip_1");
        this.backCenterStrip2 = lookup.apply("back_center_strip_2");
        this.backCenterStrip3 = lookup.apply("back_center_strip_3");
        this.spectralTail1 = lookup.apply("spectral_tail_1");
        this.spectralTail2 = lookup.apply("spectral_tail_2");
        this.waistChains = lookup.apply("waist_chains");
        this.scytheRoot = lookup.apply("scythe_root");

        for (SoulReaper.ActionId action : SoulReaper.ActionId.values()) {
            this.actionAnimations.put(action, SoulReaperAnimations.forAction(action).bake(root));
        }
        this.hurtAnimation = SoulReaperAnimations.HURT.bake(root);
        this.flickSnuffAnimation = SoulReaperAnimations.A4_FLICK_SNUFF.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Master container at ground level (Y=24)
        PartDefinition reaperRoot = root.addOrReplaceChild(
                "reaper_root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // 2-6. Pelvis, Spine, Chest, and Rib Flaps
        PartDefinition pelvis = reaperRoot.addOrReplaceChild(
                "pelvis",
                CubeListBuilder.create().texOffs(140, 0).addBox(-5.0F, -4.0F, -3.0F, 10.0F, 5.0F, 6.0F),
                PartPose.offset(0.0F, -24.0F, 0.0F));

        PartDefinition lowerSpine = pelvis.addOrReplaceChild(
                "lower_spine",
                CubeListBuilder.create().texOffs(220, 0).addBox(-3.0F, -7.0F, -2.0F, 6.0F, 7.0F, 4.0F),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition chest = lowerSpine.addOrReplaceChild(
                "chest",
                CubeListBuilder.create().texOffs(56, 0).addBox(-7.0F, -11.0F, -4.0F, 14.0F, 11.0F, 8.0F),
                PartPose.offset(0.0F, -7.0F, 0.0F));

        chest.addOrReplaceChild(
                "rib_flaps_left",
                CubeListBuilder.create().texOffs(80, 54).addBox(-3.0F, -4.0F, -1.0F, 6.0F, 8.0F, 2.0F),
                PartPose.offset(-4.0F, -5.0F, -4.0F));

        chest.addOrReplaceChild(
                "rib_flaps_right",
                CubeListBuilder.create().texOffs(100, 54).addBox(-3.0F, -4.0F, -1.0F, 6.0F, 8.0F, 2.0F),
                PartPose.offset(4.0F, -5.0F, -4.0F));

        // 7-14. Neck, Head, Jaw, and Cowl
        PartDefinition neck = chest.addOrReplaceChild(
                "neck",
                CubeListBuilder.create().texOffs(174, 24).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, -11.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(104, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        head.addOrReplaceChild(
                "jaw",
                CubeListBuilder.create().texOffs(194, 24).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, -1.0F, -2.0F));

        head.addOrReplaceChild(
                "cowl_rim",
                CubeListBuilder.create().texOffs(218, 24).addBox(-5.0F, 0.0F, -2.0F, 10.0F, 3.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -8.0F, -4.0F, 0.14F, 0.0F, 0.0F));

        head.addOrReplaceChild(
                "cowl_left_fold",
                CubeListBuilder.create().texOffs(0, 54).addBox(-2.0F, 0.0F, -3.0F, 2.0F, 10.0F, 7.0F),
                PartPose.offsetAndRotation(-4.0F, -6.0F, -1.0F, 0.0F, 0.0F, 0.08F));

        head.addOrReplaceChild(
                "cowl_right_fold",
                CubeListBuilder.create().texOffs(22, 54).addBox(0.0F, 0.0F, -3.0F, 2.0F, 10.0F, 7.0F),
                PartPose.offsetAndRotation(4.0F, -6.0F, -1.0F, 0.0F, 0.0F, -0.08F));

        PartDefinition cowlTail1 = head.addOrReplaceChild(
                "cowl_tail_1",
                CubeListBuilder.create().texOffs(44, 54).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 4.0F, 0.16F, 0.0F, 0.0F));

        cowlTail1.addOrReplaceChild(
                "cowl_tail_2",
                CubeListBuilder.create().texOffs(64, 54).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, 0.12F, 0.0F, 0.0F));

        // 15-24. Left Arm & Hand (grasping grip_upper in default Vigil pose)
        PartDefinition leftClavicle = chest.addOrReplaceChild(
                "left_clavicle",
                CubeListBuilder.create().texOffs(120, 54).addBox(-4.0F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F),
                PartPose.offset(-7.0F, -9.0F, 0.0F));

        PartDefinition leftUpperArm = leftClavicle.addOrReplaceChild(
                "left_upper_arm",
                CubeListBuilder.create().texOffs(172, 54).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(-2.0F, 1.0F, 0.0F, -0.28F, -0.34F, 0.36F));

        PartDefinition leftSleeve1 = leftUpperArm.addOrReplaceChild(
                "left_Sleeve_drape_1",
                CubeListBuilder.create().texOffs(0, 76).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 9.0F, 5.0F),
                PartPose.offsetAndRotation(-1.0F, 4.0F, 1.0F, 0.18F, 0.0F, 0.08F));

        leftSleeve1.addOrReplaceChild(
                "left_sleeve_drape_2",
                CubeListBuilder.create().texOffs(18, 76).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 8.0F, 4.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));

        PartDefinition leftForearm = leftUpperArm.addOrReplaceChild(
                "left_forearm",
                CubeListBuilder.create().texOffs(208, 54).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, -1.22F, 0.0F, 0.0F));

        PartDefinition leftHand = leftForearm.addOrReplaceChild(
                "left_hand",
                CubeListBuilder.create().texOffs(68, 76).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, 0.15F, 0.0F, 0.0F));

        leftHand.addOrReplaceChild(
                "left_thumb",
                CubeListBuilder.create().texOffs(82, 76).addBox(0.0F, 0.0F, -1.0F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(1.5F, 1.0F, -1.0F, 0.35F, 0.0F, -0.35F));

        leftHand.addOrReplaceChild(
                "left_index_group",
                CubeListBuilder.create().texOffs(90, 76).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.5F, 4.0F, -1.0F, 0.65F, 0.0F, 0.0F));

        leftHand.addOrReplaceChild(
                "left_middle_ring_group",
                CubeListBuilder.create().texOffs(100, 76).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-0.5F, 4.0F, 0.0F, 0.72F, 0.0F, 0.0F));

        leftHand.addOrReplaceChild(
                "left_pinky_claw",
                CubeListBuilder.create().texOffs(110, 76).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-1.5F, 3.5F, 0.5F, 0.78F, 0.0F, 0.0F));

        // 25-34. Right Arm & Hand (grasping grip_lower in default Vigil pose)
        PartDefinition rightClavicle = chest.addOrReplaceChild(
                "right_clavicle",
                CubeListBuilder.create().texOffs(146, 54).addBox(-1.0F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F),
                PartPose.offset(7.0F, -9.0F, 0.0F));

        PartDefinition rightUpperArm = rightClavicle.addOrReplaceChild(
                "right_upper_arm",
                CubeListBuilder.create().texOffs(190, 54).addBox(-1.0F, 0.0F, -2.0F, 3.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(2.0F, 1.0F, 0.0F, -0.32F, 0.52F, -0.18F));

        PartDefinition rightSleeve1 = rightUpperArm.addOrReplaceChild(
                "right_sleeve_drape_1",
                CubeListBuilder.create().texOffs(34, 76).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 9.0F, 5.0F),
                PartPose.offsetAndRotation(1.0F, 4.0F, 1.0F, 0.18F, 0.0F, -0.08F));

        rightSleeve1.addOrReplaceChild(
                "right_sleeve_drape_2",
                CubeListBuilder.create().texOffs(52, 76).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 8.0F, 4.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));

        PartDefinition rightForearm = rightUpperArm.addOrReplaceChild(
                "right_forearm",
                CubeListBuilder.create().texOffs(224, 54).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, -1.96F, 0.0F, 0.0F));

        PartDefinition rightHand = rightForearm.addOrReplaceChild(
                "right_hand",
                CubeListBuilder.create().texOffs(118, 76).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, 0.12F, 0.0F, 0.0F));

        rightHand.addOrReplaceChild(
                "right_thumb",
                CubeListBuilder.create().texOffs(132, 76).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-1.5F, 1.0F, -1.0F, 0.35F, 0.0F, 0.35F));

        rightHand.addOrReplaceChild(
                "right_index_group",
                CubeListBuilder.create().texOffs(140, 76).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-0.5F, 4.0F, -1.0F, 0.65F, 0.0F, 0.0F));

        rightHand.addOrReplaceChild(
                "right_middle_ring_group",
                CubeListBuilder.create().texOffs(150, 76).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.5F, 4.0F, 0.0F, 0.72F, 0.0F, 0.0F));

        rightHand.addOrReplaceChild(
                "right_pinky_claw",
                CubeListBuilder.create().texOffs(160, 76).addBox(0.0F, 0.0F, -1.0F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(1.5F, 3.5F, 0.5F, 0.78F, 0.0F, 0.0F));

        // 35-54. Shroud, Strips, Spectral Tail, and Waist Chains
        chest.addOrReplaceChild(
                "upper_shroud",
                CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.0F, -5.0F, 16.0F, 10.0F, 10.0F),
                PartPose.offset(0.0F, -9.0F, 0.0F));

        PartDefinition lowerShroudRing = pelvis.addOrReplaceChild(
                "lower_shroud_ring",
                CubeListBuilder.create().texOffs(176, 0).addBox(-6.0F, 0.0F, -4.0F, 12.0F, 6.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition fl1 = lowerShroudRing.addOrReplaceChild(
                "front_left_strip_1",
                CubeListBuilder.create().texOffs(0, 96).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 8.0F, 1.0F),
                PartPose.offset(-3.5F, 5.0F, -3.5F));
        PartDefinition fl2 = fl1.addOrReplaceChild(
                "front_left_strip_2",
                CubeListBuilder.create().texOffs(14, 96).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 8.0F, 1.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        fl2.addOrReplaceChild(
                "front_left_strip_3",
                CubeListBuilder.create().texOffs(28, 96).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 7.0F, 1.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition fr1 = lowerShroudRing.addOrReplaceChild(
                "front_right_strip_1",
                CubeListBuilder.create().texOffs(40, 96).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 8.0F, 1.0F),
                PartPose.offset(3.5F, 5.0F, -3.5F));
        PartDefinition fr2 = fr1.addOrReplaceChild(
                "front_right_strip_2",
                CubeListBuilder.create().texOffs(54, 96).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 8.0F, 1.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        fr2.addOrReplaceChild(
                "front_right_strip_3",
                CubeListBuilder.create().texOffs(68, 96).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 7.0F, 1.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition sl1 = lowerShroudRing.addOrReplaceChild(
                "side_left_strip_1",
                CubeListBuilder.create().texOffs(80, 96).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 8.0F, 6.0F),
                PartPose.offset(-5.5F, 5.0F, 0.0F));
        PartDefinition sl2 = sl1.addOrReplaceChild(
                "side_left_strip_2",
                CubeListBuilder.create().texOffs(98, 96).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 8.0F, 6.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        sl2.addOrReplaceChild(
                "side_left_strip_3",
                CubeListBuilder.create().texOffs(116, 96).addBox(-1.0F, 0.0F, -2.5F, 1.0F, 7.0F, 5.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition sr1 = lowerShroudRing.addOrReplaceChild(
                "side_right_strip_1",
                CubeListBuilder.create().texOffs(132, 96).addBox(0.0F, 0.0F, -3.0F, 1.0F, 8.0F, 6.0F),
                PartPose.offset(5.5F, 5.0F, 0.0F));
        PartDefinition sr2 = sr1.addOrReplaceChild(
                "side_right_strip_2",
                CubeListBuilder.create().texOffs(150, 96).addBox(0.0F, 0.0F, -3.0F, 1.0F, 8.0F, 6.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        sr2.addOrReplaceChild(
                "side_right_strip_3",
                CubeListBuilder.create().texOffs(168, 96).addBox(0.0F, 0.0F, -2.5F, 1.0F, 7.0F, 5.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition bc1 = lowerShroudRing.addOrReplaceChild(
                "back_center_strip_1",
                CubeListBuilder.create().texOffs(0, 116).addBox(-4.0F, 0.0F, 0.0F, 8.0F, 9.0F, 1.0F),
                PartPose.offset(0.0F, 5.0F, 3.5F));
        PartDefinition bc2 = bc1.addOrReplaceChild(
                "back_center_strip_2",
                CubeListBuilder.create().texOffs(22, 116).addBox(-3.5F, 0.0F, 0.0F, 7.0F, 9.0F, 1.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));
        bc2.addOrReplaceChild(
                "back_center_strip_3",
                CubeListBuilder.create().texOffs(42, 116).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 8.0F, 1.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));

        PartDefinition spectralTail1 = pelvis.addOrReplaceChild(
                "spectral_tail_1",
                CubeListBuilder.create().texOffs(60, 116).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 11.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 2.0F, 1.0F, 0.14F, 0.0F, 0.0F));
        spectralTail1.addOrReplaceChild(
                "spectral_tail_2",
                CubeListBuilder.create().texOffs(82, 116).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, 0.18F, 0.0F, 0.0F));

        pelvis.addOrReplaceChild(
                "waist_chains",
                CubeListBuilder.create().texOffs(168, 76).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 6.0F, 2.0F),
                PartPose.offset(0.0F, 1.0F, -3.5F));

        // 55-63. Great Scythe (child of chest so hands and weapon share the same torso frame)
        PartDefinition scytheRoot = chest.addOrReplaceChild(
                "scythe_root",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -2.0F, -9.0F, 0.10F, 0.16F, 0.38F));

        PartDefinition shaftLower = scytheRoot.addOrReplaceChild(
                "scythe_shaft_lower",
                CubeListBuilder.create().texOffs(130, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 18.0F, 2.0F),
                PartPose.ZERO);

        PartDefinition shaftUpper = shaftLower.addOrReplaceChild(
                "scythe_shaft_upper",
                CubeListBuilder.create().texOffs(118, 24).addBox(-1.0F, -24.0F, -1.0F, 2.0F, 24.0F, 2.0F),
                PartPose.ZERO);

        shaftUpper.addOrReplaceChild(
                "grip_upper",
                CubeListBuilder.create().texOffs(142, 24).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offset(0.0F, UPPER_GRIP_LOCAL_Y, 0.0F));

        shaftLower.addOrReplaceChild(
                "grip_lower",
                CubeListBuilder.create().texOffs(158, 24).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offset(0.0F, LOWER_GRIP_LOCAL_Y, 0.0F));

        shaftLower.addOrReplaceChild(
                "scythe_pommel",
                CubeListBuilder.create().texOffs(98, 24).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        PartDefinition scytheCollar = shaftUpper.addOrReplaceChild(
                "scythe_collar",
                CubeListBuilder.create().texOffs(74, 24).addBox(-2.5F, -4.0F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, -24.0F, 0.0F));

        PartDefinition bladeBase = scytheCollar.addOrReplaceChild(
                "scythe_blade_base",
                CubeListBuilder.create().texOffs(0, 24).addBox(0.0F, -3.0F, -1.0F, 16.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, 0.14F, 0.22F));

        bladeBase.addOrReplaceChild(
                "scythe_blade_tip",
                CubeListBuilder.create().texOffs(40, 24).addBox(0.0F, -2.0F, -0.5F, 14.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(16.0F, -0.5F, 0.0F, 0.0F, 0.10F, 0.42F));

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(SoulReaperRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Mth.clamp(state.walkAnimationSpeed, 0.0F, 1.0F);
        float walkPos = state.walkAnimationPos;

        // Layer L0: Locomotion & Hover Breathing
        float hoverBob = Mth.sin(age * 0.09F) * 1.4F;
        this.reaperRoot.y += hoverBob;
        this.ribFlapsLeft.yRot -= (Mth.sin(age * 0.09F) * 0.06F + 0.04F);
        this.ribFlapsRight.yRot += (Mth.sin(age * 0.09F) * 0.06F + 0.04F);

        switch (state.locomotionMode) {
            case GLIDE -> {
                this.chest.xRot += speed * 0.22F;
                this.pelvis.xRot += speed * 0.16F;
            }
            case ORBIT -> {
                this.chest.zRot += Mth.sin(walkPos * 0.5F) * 0.18F * speed;
                this.pelvis.zRot += Mth.sin(walkPos * 0.5F) * 0.12F * speed;
            }
            case STAFF_STRIDE -> {
                float step = Mth.sin(walkPos * 0.65F) * speed;
                this.reaperRoot.y += Math.abs(step) * 1.6F;
                this.scytheRoot.xRot += step * 0.20F;
            }
            case PREDATOR_RUN -> {
                this.chest.xRot += 0.42F * speed;
                this.scytheRoot.zRot -= 0.28F * speed;
                this.scytheRoot.yRot -= 0.32F * speed;
            }
            case MIST_ASCENT -> {
                this.reaperRoot.y -= 2.5F;
                this.chest.xRot -= 0.18F;
            }
        }

        // Layer L2 / L4: Primary Action Keyframe Animation
        KeyframeAnimation primary = this.actionAnimations.get(state.actionId);
        if (primary != null && state.actionId != SoulReaper.ActionId.NONE) {
            primary.apply(state.primaryAnimState, age);
        } else {
            this.actionAnimations.get(SoulReaper.ActionId.IDLE_1).apply(state.primaryAnimState, age);
        }

        // Layer L3: Additive Reaction Overlay
        if (state.reactId == SoulReaper.ReactId.HURT) {
            this.hurtAnimation.apply(state.reactAnimState, age);
        } else if (state.reactId == SoulReaper.ReactId.A4_FLICK_SNUFF) {
            this.flickSnuffAnimation.apply(state.reactAnimState, age);
        }

        // Analytical Two-Handed Scythe IK Solver (locks hands onto grip_upper and grip_lower in chest space)
        applyTwoHandedScytheIK(state);

        // Layer L1: Head & Neck Look-At (suppressed during DYING / AWAKENING)
        if (state.state != SoulReaper.State.DYING && state.state != SoulReaper.State.AWAKENING) {
            float yawRad = Mth.clamp(state.yRot, -45.0F, 45.0F) * Mth.DEG_TO_RAD;
            float pitchRad = Mth.clamp(state.xRot, -30.0F, 30.0F) * Mth.DEG_TO_RAD;
            this.neck.yRot += yawRad * 0.35F;
            this.head.yRot += yawRad * 0.65F;
            this.neck.xRot += pitchRad * 0.30F;
            this.head.xRot += pitchRad * 0.70F;
        }

        // Layer L5: Secondary Cloth, Cowl, Sleeve, Chain & Spectral Tail Inertia
        float windWave = Mth.sin(age * 0.12F) * 0.08F + speed * 0.28F;
        float windLag = Mth.sin(age * 0.12F - 0.85F) * 0.10F + speed * 0.22F;
        float windTip = Mth.sin(age * 0.12F - 1.70F) * 0.12F + speed * 0.18F;

        this.cowlTail1.xRot += windWave;
        this.cowlTail2.xRot += windLag;
        this.leftSleeveDrape1.xRot += windWave * 0.7F;
        this.leftSleeveDrape2.xRot += windLag * 0.8F;
        this.rightSleeveDrape1.xRot += windWave * 0.7F;
        this.rightSleeveDrape2.xRot += windLag * 0.8F;

        this.frontLeftStrip1.xRot += windWave * 0.6F;
        this.frontLeftStrip2.xRot += windLag * 0.7F;
        this.frontLeftStrip3.xRot += windTip * 0.8F;
        this.frontRightStrip1.xRot += windWave * 0.6F;
        this.frontRightStrip2.xRot += windLag * 0.7F;
        this.frontRightStrip3.xRot += windTip * 0.8F;

        this.sideLeftStrip1.zRot += Math.abs(windWave) * 0.5F;
        this.sideLeftStrip2.zRot += Math.abs(windLag) * 0.6F;
        this.sideLeftStrip3.zRot += Math.abs(windTip) * 0.7F;
        this.sideRightStrip1.zRot -= Math.abs(windWave) * 0.5F;
        this.sideRightStrip2.zRot -= Math.abs(windLag) * 0.6F;
        this.sideRightStrip3.zRot -= Math.abs(windTip) * 0.7F;

        this.backCenterStrip1.xRot += windWave;
        this.backCenterStrip2.xRot += windLag;
        this.backCenterStrip3.xRot += windTip;

        this.spectralTail1.xRot += windWave * 0.8F;
        this.spectralTail1.zRot += Mth.cos(age * 0.08F) * 0.06F;
        this.spectralTail2.xRot += windLag * 0.9F;
        this.spectralTail2.zRot += Mth.cos(age * 0.08F - 0.9F) * 0.09F;
        this.waistChains.xRot += Mth.sin(age * 0.18F) * 0.06F + speed * 0.20F;
    }

    private void applyTwoHandedScytheIK(SoulReaperRenderState state) {
        Vector3f upperGrip = computeGripInChestSpace(UPPER_GRIP_LOCAL_Y);
        Vector3f lowerGrip = computeGripInChestSpace(LOWER_GRIP_LOCAL_Y);

        Vector3f leftShoulder = new Vector3f(
                this.leftClavicle.x + this.leftUpperArm.x,
                this.leftClavicle.y + this.leftUpperArm.y,
                this.leftClavicle.z + this.leftUpperArm.z);
        Vector3f rightShoulder = new Vector3f(
                this.rightClavicle.x + this.rightUpperArm.x,
                this.rightClavicle.y + this.rightUpperArm.y,
                this.rightClavicle.z + this.rightUpperArm.z);

        Vector3f leftPole = new Vector3f(-0.45F, 1.0F, 0.55F).normalize();
        Vector3f rightPole = new Vector3f(0.45F, 1.0F, 0.55F).normalize();

        boolean swapGrips = state.actionId == SoulReaper.ActionId.F6_THREEFOLD_HARVEST
                && state.actionTick >= 19
                && state.actionTick <= 26;
        Vector3f rightTarget = swapGrips ? lowerGrip : upperGrip;
        Vector3f leftTarget = swapGrips ? upperGrip : lowerGrip;

        solveArmIK(
                this.rightUpperArm,
                this.rightForearm,
                this.rightHand,
                this.rightSleeveDrape1,
                rightShoulder,
                rightTarget,
                rightPole);
        if (!isLeftHandFreeGesture(state)) {
            solveArmIK(
                    this.leftUpperArm,
                    this.leftForearm,
                    this.leftHand,
                    this.leftSleeveDrape1,
                    leftShoulder,
                    leftTarget,
                    leftPole);
        }
    }

    private static boolean isLeftHandFreeGesture(SoulReaperRenderState state) {
        if (state.reactId == SoulReaper.ReactId.A4_FLICK_SNUFF) {
            return true;
        }
        return switch (state.actionId) {
            case IDLE_2,
                    O3_SOUL_HARVEST,
                    P1_BECKON,
                    P2_FUNERAL_BOW,
                    P3_HOURGLASS_COUNT,
                    P4_BLADE_HONING,
                    A1_VIAL_SILENCE -> true;
            default -> false;
        };
    }

    private Vector3f computeGripInChestSpace(float localGripY) {
        Quaternionf scytheRot = new Quaternionf()
                .rotationZYX(this.scytheRoot.zRot, this.scytheRoot.yRot, this.scytheRoot.xRot);
        return new Vector3f(0.0F, localGripY, 0.0F)
                .rotate(scytheRot)
                .add(this.scytheRoot.x, this.scytheRoot.y, this.scytheRoot.z);
    }

    private static void solveArmIK(
            ModelPart upperArm,
            ModelPart forearm,
            ModelPart hand,
            ModelPart sleeveDrape1,
            Vector3f shoulder,
            Vector3f target,
            Vector3f poleDir) {
        float dx = target.x - shoulder.x;
        float dy = target.y - shoulder.y;
        float dz = target.z - shoulder.z;
        float rawDist = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (rawDist < 1.0E-4F) {
            return;
        }

        float maxReach = UPPER_ARM_LEN + FOREARM_GRIP_LEN - 0.15F;
        float dist = Mth.clamp(rawDist, 2.5F, maxReach);
        Vector3f dHat = new Vector3f(dx / rawDist, dy / rawDist, dz / rawDist);

        float l1Sq = UPPER_ARM_LEN * UPPER_ARM_LEN;
        float l2Sq = FOREARM_GRIP_LEN * FOREARM_GRIP_LEN;
        float a = (l1Sq - l2Sq + dist * dist) / (2.0F * dist);
        float h = Mth.sqrt(Math.max(0.0F, l1Sq - a * a));
        float cosElbow = Mth.clamp(
                (dist * dist - l1Sq - l2Sq) / (2.0F * UPPER_ARM_LEN * FOREARM_GRIP_LEN), -0.99F, 0.99F);
        float thetaElbow = (float) Math.acos(cosElbow);

        float dot = poleDir.dot(dHat);
        Vector3f pPerp = new Vector3f(
                poleDir.x - dot * dHat.x,
                poleDir.y - dot * dHat.y,
                poleDir.z - dot * dHat.z);
        if (pPerp.lengthSquared() < 1.0E-5F) {
            pPerp.set(0.0F, 1.0F, 0.0F);
        } else {
            pPerp.normalize();
        }

        Vector3f sToE = new Vector3f(
                a * dHat.x + h * pPerp.x,
                a * dHat.y + h * pPerp.y,
                a * dHat.z + h * pPerp.z);
        Vector3f uy = new Vector3f(sToE).normalize();

        Vector3f eToG = new Vector3f(
                dist * dHat.x - sToE.x,
                dist * dHat.y - sToE.y,
                dist * dHat.z - sToE.z);
        Vector3f fy = new Vector3f(eToG).normalize();

        Vector3f ux = new Vector3f(fy).cross(uy);
        if (ux.lengthSquared() < 1.0E-6F) {
            ux.set(1.0F, 0.0F, 0.0F);
        } else {
            ux.normalize();
        }
        Vector3f uz = new Vector3f(ux).cross(uy).normalize();

        upperArm.xRot = (float) Math.atan2(uy.z, uz.z);
        upperArm.yRot = (float) Math.asin(Mth.clamp(-ux.z, -1.0F, 1.0F));
        upperArm.zRot = (float) Math.atan2(ux.y, ux.x);

        // Counter-orient sleeve drape using transpose M^T so tattered sleeves hang downward under gravity
        sleeveDrape1.xRot = (float) Math.atan2(uz.y, uz.z) + 0.16F;
        sleeveDrape1.yRot = (float) Math.asin(Mth.clamp(-uz.x, -1.0F, 1.0F));
        sleeveDrape1.zRot = (float) Math.atan2(uy.x, ux.x);

        forearm.xRot = -thetaElbow;
        forearm.yRot = 0.0F;
        forearm.zRot = 0.0F;

        hand.xRot = 0.14F;
        hand.yRot = 0.0F;
        hand.zRot = 0.0F;
    }
}
