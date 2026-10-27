package com.codenamecoraline.client.basilisk;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.basilisk.Basilisk;
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

/** Cuboid amphibian, in model pixels: huge false eyes, tiny real eyes, throat sac and a ragged finned tail. */
public final class BasiliskModel extends EntityModel<BasiliskRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "basilisk"), "main");
    private final ModelPart body, head, throat, jaw, tail, tailTip, leftEye, rightEye;
    private final ModelPart[] legs;

    public BasiliskModel(ModelPart root) {
        super(root);
        var lookup = root.createPartLookup();
        this.body = lookup.apply("body");
        this.head = lookup.apply("head");
        this.throat = lookup.apply("throat");
        this.jaw = lookup.apply("jaw");
        this.tail = lookup.apply("tail");
        this.tailTip = lookup.apply("tail_tip");
        this.leftEye = lookup.apply("left_false_eye");
        this.rightEye = lookup.apply("right_false_eye");
        this.legs = new ModelPart[]{lookup.apply("left_front"), lookup.apply("right_front"),
                lookup.apply("left_hind"), lookup.apply("right_hind")};
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4, -3, -5, 8, 5, 11)
                .texOffs(40, 24).addBox(-3, 1, -4, 6, 2, 8), PartPose.offset(0, 19, 0));
        // A low, broad skull. The actual eyes are 1-pixel golden spots beside the muzzle.
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 24).addBox(-4, -2, -6, 8, 4, 6)
                .texOffs(0, 36).addBox(-3, -1, -8, 6, 2, 3)
                .texOffs(110, 16).addBox(-3.6F, -0.4F, -6.2F, 1, 1, 1)
                .texOffs(110, 16).addBox(2.6F, -0.4F, -6.2F, 1, 1, 1), PartPose.offset(0, -1, -4));
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(40, 36)
                .addBox(-3.5F, 0, -7.7F, 7, 1, 6), PartPose.offset(0, 1, 0));
        head.addOrReplaceChild("throat", CubeListBuilder.create().texOffs(40, 24)
                .addBox(-3, -1, -2, 6, 3, 4), PartPose.offset(0, 2, -3));
        for (int side : new int[]{-1, 1}) {
            head.addOrReplaceChild(side < 0 ? "right_false_eye" : "left_false_eye", CubeListBuilder.create()
                    // Slightly inset cross-sections avoid coplanar faces on the stepped eyeballs.
                    .texOffs(64, 0).addBox(-3.5F, -2.45F, -2.45F, 7, 4.9F, 4.9F)
                    .texOffs(64, 0).addBox(-2.45F, -3.5F, -2.45F, 4.9F, 7, 4.9F)
                    .texOffs(64, 0).addBox(-2.5F, -2.5F, -3.5F, 5, 5, 7)
                    .texOffs(96, 0).addBox(-2.5F, -2.5F, -3.7F, 5, 5, 0.1F),
                    PartPose.offsetAndRotation(side * 3.5F, -3.8F, -2.5F, -0.12F, side * 0.16F, side * 0.10F));
        }
        for (int index = 0; index < 4; index++) {
            boolean front = index < 2;
            int side = index % 2 == 0 ? 1 : -1;
            String name = (side > 0 ? "left_" : "right_") + (front ? "front" : "hind");
            PartDefinition leg = body.addOrReplaceChild(name, CubeListBuilder.create().texOffs(0, 48)
                    .addBox(side > 0 ? 0 : -4, -1, -1.5F, 4, 3, front ? 3 : 5),
                    PartPose.offsetAndRotation(side * 3, 0, front ? -3 : 4, front ? -0.3F : 0.3F, 0, side * 0.3F));
            leg.addOrReplaceChild(name + "_foot", CubeListBuilder.create().texOffs(22, 48)
                    .addBox(-1.5F, 0, -4, 3, 2, 5)
                    .texOffs(0, 62).addBox(-1.4F, 1, -6, 1, 1, 3)
                    .texOffs(0, 62).addBox(0.4F, 1, -6, 1, 1, 3)
                    .texOffs(40, 48).addBox(side > 0 ? 1.5F : -3.5F, -1, -2, 2, 0, 5),
                    PartPose.offsetAndRotation(side * 3, 2, 0, front ? 0.25F : -0.25F, side * -0.2F, side * -0.3F));
        }
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 72)
                .addBox(-2, -1.5F, 0, 4, 3, 7)
                .texOffs(48, 48).addBox(0, -4, 0, 0, 4, 7), PartPose.offsetAndRotation(0, -1, 5, 0.12F, 0, 0));
        tail.addOrReplaceChild("tail_tip", CubeListBuilder.create().texOffs(24, 72)
                .addBox(-1, -1, 0, 2, 2, 7)
                .texOffs(48, 60).addBox(0, -3, 0, 0, 4, 8), PartPose.offsetAndRotation(0, 0, 6, 0.1F, 0, 0));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(BasiliskRenderState state) {
        super.setupAnim(state);
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float time = state.ageInTicks + state.idleSeed;
        float breath = Mth.sin(time * 0.09F);
        float inflation = state.inflation;
        this.body.y += breath * 0.15F + (state.concealed ? 2 : 0);
        this.body.xRot += state.airborne ? Mth.clamp(-state.verticalSpeed * 0.6F, -0.25F, 0.3F) : 0;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD * 0.6F;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD * 0.4F - inflation * 0.25F;
        this.throat.xScale = 1 + 0.6F * inflation;
        this.throat.yScale = 1 + 0.6F * inflation + breath * 0.025F;
        this.throat.zScale = 1 + 0.6F * inflation;
        this.jaw.xRot = state.state == Basilisk.State.EXHALING ? 0.4F : inflation * 0.12F;
        this.tail.yRot += Mth.sin(time * 0.08F) * 0.18F;
        this.tailTip.yRot += Mth.sin(time * 0.08F - 0.7F) * 0.25F;
        // False eyes never blink. They wobble on their stalks after each leap.
        float wobble = Mth.sin(time * 0.6F) * (state.airborne ? 0.11F : 0.015F);
        this.leftEye.zRot += wobble;
        this.rightEye.zRot -= wobble;
        for (int i = 0; i < this.legs.length; i++) {
            float sign = i % 2 == 0 ? 1 : -1;
            this.legs[i].xRot += state.airborne ? (i < 2 ? -0.7F : 0.8F) : Mth.sin(time * 0.06F + i) * 0.025F;
            this.legs[i].zRot += sign * inflation * 0.08F;
        }
    }
}
