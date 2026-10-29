package com.codenamecoraline.client.collector;

import com.codenamecoraline.CodenameCoraline;
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
 * Gift Mimic: a wrapped box on stubby legs whose hinged lid snaps like a jaw. Body, lid (with bow) and two legs;
 * everything is animated procedurally from the walk cycle, its attack swing and how close its fuse is to bursting.
 */
public final class GiftMimicModel extends EntityModel<GiftMimicRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "gift_mimic"), "main");

    private final ModelPart body;
    private final ModelPart lid;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public GiftMimicModel(ModelPart root) {
        super(root);
        var lookup = root.createPartLookup();
        this.body = lookup.apply("body");
        this.lid = lookup.apply("lid");
        this.leftLeg = lookup.apply("left_leg");
        this.rightLeg = lookup.apply("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -9.0F, -6.0F, 12.0F, 9.0F, 12.0F),
                PartPose.offset(0.0F, 19.0F, 0.0F));
        PartDefinition lid = root.addOrReplaceChild("lid",
                CubeListBuilder.create().texOffs(0, 22).addBox(-7.0F, -4.0F, -14.0F, 14.0F, 4.0F, 14.0F),
                PartPose.offset(0.0F, 10.0F, 7.0F));
        lid.addOrReplaceChild("bow",
                CubeListBuilder.create().texOffs(0, 42).addBox(-3.0F, -7.0F, -9.0F, 6.0F, 3.0F, 6.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(40, 40).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offset(3.5F, 19.0F, 0.0F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(40, 40).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offset(-3.5F, 19.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(GiftMimicRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed * 2.0F);
        float walk = state.walkAnimationPos;
        this.leftLeg.xRot = Mth.cos(walk * 0.9F) * 0.9F * speed;
        this.rightLeg.xRot = -Mth.cos(walk * 0.9F) * 0.9F * speed;
        float hop = Math.abs(Mth.sin(walk * 0.9F)) * 1.8F * speed;
        this.body.y = 19.0F - hop;
        this.lid.y = 10.0F - hop;

        // The lid flaps idly and snaps wide open on every bite.
        float chomp = state.attackProgress > 0.0F ? Mth.sin(state.attackProgress * Mth.PI) : 0.0F;
        this.lid.xRot = -(0.16F + 0.1F * Mth.sin(t * 0.3F)) - chomp * 1.0F;
        this.body.xRot = chomp * 0.18F;

        // Close to bursting: it shakes more and more.
        float f = Math.max(0.0F, (state.fuse - 0.6F) / 0.4F);
        float shake = Mth.sin(t * 2.9F) * 0.12F * f;
        this.body.zRot = shake;
        this.lid.zRot = shake;
        this.lid.xRot -= f * Math.abs(Mth.sin(t * 1.7F)) * 0.5F;
    }
}
