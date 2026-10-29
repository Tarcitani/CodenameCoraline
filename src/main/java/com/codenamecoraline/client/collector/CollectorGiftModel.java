package com.codenamecoraline.client.collector;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.collector.CollectorGift;
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
 * The big Christmas present that wraps the Collector's loot chest: it trembles, then the lid and bow fly off and the
 * paper falls away, leaving the chest exposed.
 */
public final class CollectorGiftModel extends EntityModel<CollectorGiftRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "collector_gift"), "main");

    private final ModelPart base;
    private final ModelPart lid;

    public CollectorGiftModel(ModelPart root) {
        super(root);
        var lookup = root.createPartLookup();
        this.base = lookup.apply("base");
        this.lid = lookup.apply("lid");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("base",
                CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -14.0F, -10.0F, 20.0F, 14.0F, 20.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition lid = root.addOrReplaceChild("lid",
                CubeListBuilder.create().texOffs(0, 36).addBox(-11.0F, -4.0F, -11.0F, 22.0F, 4.0F, 22.0F),
                PartPose.offset(0.0F, 10.0F, 0.0F));
        lid.addOrReplaceChild("bow",
                CubeListBuilder.create()
                        .texOffs(0, 66).addBox(-6.0F, -8.0F, -3.0F, 12.0F, 4.0F, 6.0F)
                        .texOffs(40, 66).addBox(-3.0F, -7.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(CollectorGiftRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        int pop = CollectorGift.POP_TICK;
        if (age < pop) {
            float shake = Mth.sin(age * 1.9F) * 0.07F * (0.4F + age / pop);
            this.base.zRot = shake;
            this.lid.zRot = shake;
            this.base.xRot = Mth.cos(age * 1.5F) * 0.04F;
            this.lid.y = 10.0F - Math.max(0.0F, Mth.sin(age * 2.6F)) * 0.8F;
        } else {
            float dt = age - pop;
            this.lid.y = 10.0F - dt * 3.0F + dt * dt * 0.08F;
            this.lid.xRot = dt * 0.12F;
            this.lid.zRot = dt * 0.09F;
            this.lid.visible = dt < 26.0F;
            this.base.visible = dt < 6.0F;
            this.base.zRot = dt * 0.05F;
        }
    }
}
