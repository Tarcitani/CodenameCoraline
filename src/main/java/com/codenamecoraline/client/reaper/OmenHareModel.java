package com.codenamecoraline.client.reaper;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.reaper.OmenHare;
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

/**
 * 12-joint model of the Omen Hare ({@code 64x64} UV map).
 * Spec: {@code docs/design/soul_reaper_animation.md} §3.
 */
public final class OmenHareModel extends EntityModel<OmenHareRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "omen_hare"), "main");

    private final ModelPart head;
    private final ModelPart nose;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final KeyframeAnimation approachAnimation;
    private final KeyframeAnimation standStareAnimation;
    private final KeyframeAnimation tiltTauntAnimation;
    private final KeyframeAnimation ruptureAnimation;

    public OmenHareModel(ModelPart root) {
        super(root);
        Function<String, ModelPart> lookup = root.createPartLookup();
        this.head = lookup.apply("head");
        this.nose = lookup.apply("nose");
        this.leftEar = lookup.apply("left_ear");
        this.rightEar = lookup.apply("right_ear");
        this.approachAnimation = OmenHareAnimations.HARE_APPROACH.bake(root);
        this.standStareAnimation = OmenHareAnimations.HARE_STAND_STARE.bake(root);
        this.tiltTauntAnimation = OmenHareAnimations.HARE_TILT_TAUNT.bake(root);
        this.ruptureAnimation = OmenHareAnimations.HARE_RUPTURE.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition hareRoot = root.addOrReplaceChild(
                "hare_root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = hareRoot.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -3.0F, -6.0F, 5.0F, 5.0F, 8.0F),
                PartPose.offset(0.0F, -5.0F, 2.0F));

        body.addOrReplaceChild(
                "chest_tuft",
                CubeListBuilder.create().texOffs(48, 0).addBox(-2.0F, -1.5F, -1.0F, 4.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -1.0F, -6.0F));

        PartDefinition head = body.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(28, 0).addBox(-2.0F, -4.0F, -4.0F, 4.0F, 4.0F, 5.0F),
                PartPose.offset(0.0F, -3.0F, -6.0F));

        head.addOrReplaceChild(
                "nose",
                CubeListBuilder.create().texOffs(28, 16).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, -1.5F, -4.0F));

        head.addOrReplaceChild(
                "left_ear",
                CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -6.0F, -0.5F, 2.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(-1.2F, -4.0F, -1.0F, -0.10F, 0.0F, -0.08F));

        head.addOrReplaceChild(
                "right_ear",
                CubeListBuilder.create().texOffs(8, 16).addBox(-1.0F, -6.0F, -0.5F, 2.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(1.2F, -4.0F, -1.0F, -0.10F, 0.0F, 0.08F));

        body.addOrReplaceChild(
                "tail",
                CubeListBuilder.create().texOffs(16, 16).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -2.0F, 2.0F));

        body.addOrReplaceChild(
                "left_Front_leg",
                CubeListBuilder.create().texOffs(0, 26).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(-2.0F, 2.0F, -5.0F));

        body.addOrReplaceChild(
                "right_front_leg",
                CubeListBuilder.create().texOffs(10, 26).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(2.0F, 2.0F, -5.0F));

        body.addOrReplaceChild(
                "left_hind_leg",
                CubeListBuilder.create().texOffs(20, 26).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 4.0F, 3.0F),
                PartPose.offset(-2.2F, 1.0F, 1.0F));

        body.addOrReplaceChild(
                "right_hind_leg",
                CubeListBuilder.create().texOffs(32, 26).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 4.0F, 3.0F),
                PartPose.offset(2.2F, 1.0F, 1.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(OmenHareRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;

        switch (state.state) {
            case APPROACHING -> this.approachAnimation.applyWalk(
                    state.walkAnimationPos, state.walkAnimationSpeed, 2.0F, 2.5F);
            case STANDING_STARE -> this.standStareAnimation.apply(state.standStareAnimState, age);
            case TILT_TAUNT -> this.tiltTauntAnimation.apply(state.tiltTauntAnimState, age);
            case RUPTURING -> this.ruptureAnimation.apply(state.ruptureAnimState, age);
        }

        this.nose.xRot += Mth.sin(age * 0.45F) * 0.08F;
        this.leftEar.zRot -= Mth.sin(age * 0.14F) * 0.04F;
        this.rightEar.zRot += Mth.cos(age * 0.14F) * 0.04F;

        if (state.state != OmenHare.State.RUPTURING) {
            this.head.yRot += Mth.clamp(state.yRot, -40.0F, 40.0F) * Mth.DEG_TO_RAD;
            this.head.xRot += Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;
        }
    }
}

