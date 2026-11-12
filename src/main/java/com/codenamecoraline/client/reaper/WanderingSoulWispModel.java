package com.codenamecoraline.client.reaper;

import com.codenamecoraline.CodenameCoraline;
import java.util.function.Function;
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

public final class WanderingSoulWispModel extends EntityModel<WanderingSoulWispRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "wandering_soul_wisp"), "main");

    private final ModelPart wispRoot;
    private final ModelPart core;
    private final ModelPart haloRing;
    private final ModelPart tailWisp;

    public WanderingSoulWispModel(ModelPart root) {
        super(root);
        Function<String, ModelPart> lookup = root.createPartLookup();
        this.wispRoot = lookup.apply("wisp_root");
        this.core = lookup.apply("core");
        this.haloRing = lookup.apply("halo_ring");
        this.tailWisp = lookup.apply("tail_wisp");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition wispRoot = root.addOrReplaceChild(
                "wisp_root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        PartDefinition core = wispRoot.addOrReplaceChild(
                "core",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.ZERO);

        core.addOrReplaceChild(
                "halo_ring",
                CubeListBuilder.create().texOffs(0, 12).addBox(-4.0F, -2.0F, -4.0F, 8.0F, 4.0F, 8.0F),
                PartPose.ZERO);

        core.addOrReplaceChild(
                "tail_wisp",
                CubeListBuilder.create().texOffs(0, 24).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(WanderingSoulWispRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        this.wispRoot.y += Mth.sin(age * 0.18F) * 1.5F;
        this.core.yRot = age * 0.08F;
        this.haloRing.yRot = -age * 0.14F;
        this.haloRing.zRot = Mth.sin(age * 0.12F) * 0.22F;
        this.tailWisp.xRot = 0.25F + Mth.sin(age * 0.20F) * 0.18F;
    }
}

