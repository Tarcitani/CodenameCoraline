package com.codenamecoraline.client.reaper;

import com.codenamecoraline.CodenameCoraline;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public final class OmenHareEyesLayer extends EyesLayer<OmenHareRenderState, OmenHareModel> {
    private static final RenderType EYES = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/omen_hare_eyes.png"));

    public OmenHareEyesLayer(RenderLayerParent<OmenHareRenderState, OmenHareModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            OmenHareRenderState state,
            float yRot,
            float xRot) {
        super.submit(poseStack, collector, packedLight, state, yRot, xRot);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }
}

