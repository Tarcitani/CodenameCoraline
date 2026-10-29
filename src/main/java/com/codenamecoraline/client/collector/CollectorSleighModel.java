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
 * Articulated 3D model for the sky sleigh (`codenamecoraline:collector_sleigh`), spanning ~8 blocks
 * from the curved iron runners and high timber stern to the twin spectral winter stags pulling it.
 */
public final class CollectorSleighModel extends EntityModel<CollectorSleighRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "collector_sleigh"), "main");

    private final ModelPart carriage;
    private final ModelPart leftLantern;
    private final ModelPart rightLantern;
    private final ModelPart leftStag;
    private final ModelPart rightStag;

    public CollectorSleighModel(ModelPart root) {
        super(root);
        var lookup = root.createPartLookup();
        this.carriage = lookup.apply("carriage");
        this.leftLantern = lookup.apply("left_lantern");
        this.rightLantern = lookup.apply("right_lantern");
        this.leftStag = lookup.apply("left_stag");
        this.rightStag = lookup.apply("right_stag");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Main timber sleigh carriage and long curved iron runners.
        PartDefinition carriage = root.addOrReplaceChild("carriage",
                CubeListBuilder.create()
                        // Left and right iron runners (3 x 3 x 56) + upturned front prows
                        .texOffs(0, 0).addBox(-16.0F, 18.0F, -28.0F, 3.0F, 3.0F, 56.0F)
                        .texOffs(0, 0).addBox(13.0F, 18.0F, -28.0F, 3.0F, 3.0F, 56.0F)
                        .texOffs(0, 60).addBox(-16.0F, 10.0F, -34.0F, 3.0F, 10.0F, 6.0F)
                        .texOffs(0, 60).addBox(13.0F, 10.0F, -34.0F, 3.0F, 10.0F, 6.0F)
                        // Heavy weathered timber deck (28 x 4 x 46)
                        .texOffs(0, 60).addBox(-14.0F, 12.0F, -22.0F, 28.0F, 4.0F, 46.0F)
                        // Left and right carved side walls (3 x 12 x 42)
                        .texOffs(0, 0).addBox(-15.0F, 0.0F, -20.0F, 3.0F, 12.0F, 42.0F)
                        .texOffs(0, 0).addBox(12.0F, 0.0F, -20.0F, 3.0F, 12.0F, 42.0F)
                        // High ritual stern back-wall and cargo reliquary (28 x 22 x 6)
                        .texOffs(60, 0).addBox(-14.0F, -10.0F, 18.0F, 28.0F, 22.0F, 6.0F)
                        // Curved front prow shield (26 x 12 x 4)
                        .texOffs(60, 28).addBox(-13.0F, 0.0F, -24.0F, 26.0F, 12.0F, 4.0F)
                        // Harness drawbars extending forward to the spectral stags
                        .texOffs(0, 0).addBox(-9.0F, 10.0F, -54.0F, 2.0F, 2.0F, 30.0F)
                        .texOffs(0, 0).addBox(7.0F, 10.0F, -54.0F, 2.0F, 2.0F, 30.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Swinging cold-blue lanterns on both sides of the prow.
        carriage.addOrReplaceChild("left_lantern",
                CubeListBuilder.create()
                        .texOffs(64, 48).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 8.0F, 5.0F),
                PartPose.offset(-16.5F, -2.0F, -18.0F));

        carriage.addOrReplaceChild("right_lantern",
                CubeListBuilder.create()
                        .texOffs(64, 48).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 8.0F, 5.0F),
                PartPose.offset(16.5F, -2.0F, -18.0F));

        // Twin spectral winter stags galloping ahead of the sleigh.
        carriage.addOrReplaceChild("left_stag",
                CubeListBuilder.create()
                        .texOffs(64, 64).addBox(-4.0F, -4.0F, -12.0F, 8.0F, 9.0F, 20.0F)
                        .texOffs(64, 94).addBox(-3.0F, -14.0F, -16.0F, 6.0F, 12.0F, 7.0F)
                        .texOffs(92, 94).addBox(-6.0F, -24.0F, -15.0F, 12.0F, 10.0F, 4.0F),
                PartPose.offset(-10.0F, 6.0F, -58.0F));

        carriage.addOrReplaceChild("right_stag",
                CubeListBuilder.create()
                        .texOffs(64, 64).addBox(-4.0F, -4.0F, -12.0F, 8.0F, 9.0F, 20.0F)
                        .texOffs(64, 94).addBox(-3.0F, -14.0F, -16.0F, 6.0F, 12.0F, 7.0F)
                        .texOffs(92, 94).addBox(-6.0F, -24.0F, -15.0F, 12.0F, 10.0F, 4.0F),
                PartPose.offset(10.0F, 6.0F, -58.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(CollectorSleighRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        // Subtle airborne roll and pitch undulation
        this.carriage.zRot = Mth.sin(t * 0.18F) * 0.04F;
        this.carriage.xRot = Mth.cos(t * 0.14F) * 0.03F;

        // Swinging lanterns
        this.leftLantern.zRot = Mth.sin(t * 0.25F) * 0.18F;
        this.rightLantern.zRot = -Mth.sin(t * 0.25F) * 0.18F;

        // Galloping motion of the spectral winter stags
        this.leftStag.xRot = Mth.sin(t * 0.45F) * 0.16F;
        this.leftStag.y = 6.0F + Mth.cos(t * 0.45F) * 1.8F;
        this.rightStag.xRot = Mth.sin(t * 0.45F + 1.5F) * 0.16F;
        this.rightStag.y = 6.0F + Mth.cos(t * 0.45F + 1.5F) * 1.8F;
    }
}

