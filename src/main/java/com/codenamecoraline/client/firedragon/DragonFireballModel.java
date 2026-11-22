package com.codenamecoraline.client.firedragon;

import com.codenamecoraline.CodenameCoraline;
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

/** 3D comet fireball with incandescent core, counter-rotating flame shells, swirling crown and tapered tail. */
public final class DragonFireballModel extends EntityModel<DragonFireballRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "dragon_fireball"), "main");

    private final ModelPart core;
    private final ModelPart innerShell;
    private final ModelPart outerShell;
    private final ModelPart crown;
    private final ModelPart tailBase;
    private final ModelPart tailMid;
    private final ModelPart tailTip;
    private final ModelPart finH;
    private final ModelPart finV;

    public DragonFireballModel(ModelPart root) {
        super(root);
        var lookup = root.createPartLookup();
        this.core = lookup.apply("core");
        this.innerShell = lookup.apply("inner_shell");
        this.outerShell = lookup.apply("outer_shell");
        this.crown = lookup.apply("crown");
        this.tailBase = lookup.apply("tail_base");
        this.tailMid = lookup.apply("tail_mid");
        this.tailTip = lookup.apply("tail_tip");
        this.finH = lookup.apply("fin_h");
        this.finV = lookup.apply("fin_v");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("core",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.ZERO);

        root.addOrReplaceChild("inner_shell",
                CubeListBuilder.create().texOffs(0, 10).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.25F)),
                PartPose.rotation(0.45F, 0.35F, 0.0F));

        root.addOrReplaceChild("outer_shell",
                CubeListBuilder.create().texOffs(24, 0).addBox(-3.0F, -3.0F, -3.4F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.35F)),
                PartPose.rotation(-0.3F, 0.5F, 0.0F));

        PartDefinition crown = root.addOrReplaceChild("crown", CubeListBuilder.create(), PartPose.ZERO);
        crown.addOrReplaceChild("petal_top",
                CubeListBuilder.create().texOffs(0, 22).addBox(-1.0F, -3.2F, -4.6F, 2.0F, 2.0F, 5.0F),
                PartPose.rotation(0.28F, 0.0F, 0.0F));
        crown.addOrReplaceChild("petal_bottom",
                CubeListBuilder.create().texOffs(0, 22).addBox(-1.0F, 1.2F, -4.6F, 2.0F, 2.0F, 5.0F),
                PartPose.rotation(-0.28F, 0.0F, 0.0F));
        crown.addOrReplaceChild("petal_left",
                CubeListBuilder.create().texOffs(0, 22).addBox(1.2F, -1.0F, -4.6F, 2.0F, 2.0F, 5.0F),
                PartPose.rotation(0.0F, -0.28F, 0.0F));
        crown.addOrReplaceChild("petal_right",
                CubeListBuilder.create().texOffs(0, 22).addBox(-3.2F, -1.0F, -4.6F, 2.0F, 2.0F, 5.0F),
                PartPose.rotation(0.0F, 0.28F, 0.0F));

        PartDefinition tailBase = root.addOrReplaceChild("tail_base",
                CubeListBuilder.create().texOffs(24, 14).addBox(-2.0F, -2.0F, -6.0F, 4.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, -2.2F));

        PartDefinition tailMid = tailBase.addOrReplaceChild("tail_mid",
                CubeListBuilder.create().texOffs(24, 26).addBox(-1.5F, -1.5F, -6.0F, 3.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, -5.2F));

        tailMid.addOrReplaceChild("tail_tip",
                CubeListBuilder.create().texOffs(24, 37).addBox(-1.0F, -1.0F, -5.0F, 2.0F, 2.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, -5.2F));

        root.addOrReplaceChild("fin_h",
                CubeListBuilder.create().texOffs(0, 46).addBox(-5.0F, 0.0F, -15.5F, 10.0F, 0.0F, 16.0F),
                PartPose.ZERO);

        root.addOrReplaceChild("fin_v",
                CubeListBuilder.create().texOffs(0, 46).addBox(-5.0F, 0.0F, -15.5F, 10.0F, 0.0F, 16.0F),
                PartPose.rotation(0.0F, 0.0F, Mth.HALF_PI));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(DragonFireballRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        core.zRot = t * 0.45F;
        core.xRot = t * 0.28F;
        innerShell.zRot = -t * 0.35F;
        innerShell.yRot = 0.35F + t * 0.22F;
        outerShell.zRot = t * 0.26F;
        outerShell.xRot = -0.3F - t * 0.18F;
        crown.zRot = -t * 0.42F;

        tailBase.yRot = Mth.sin(t * 0.65F) * 0.10F;
        tailBase.xRot = Mth.cos(t * 0.55F) * 0.08F;
        tailMid.yRot = Mth.sin(t * 0.65F - 0.9F) * 0.16F;
        tailMid.xRot = Mth.cos(t * 0.55F - 0.9F) * 0.12F;
        tailTip.yRot = Mth.sin(t * 0.65F - 1.8F) * 0.24F;
        tailTip.xRot = Mth.cos(t * 0.55F - 1.8F) * 0.18F;

        finH.zRot = t * 0.18F;
        finV.zRot = Mth.HALF_PI + t * 0.18F;
    }
}
