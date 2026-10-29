package com.codenamecoraline.client.lithophage;

import com.codenamecoraline.CodenameCoraline;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Renders the discreet amber mineral glow in the Lithophage's mask fissures and crystal veins
 * when the creature is awakening or awake. Remains unlit while dormant or after death.
 */
public final class LithophageEyesLayer extends EyesLayer<LithophageRenderState, LithophageModel> {
    private static final RenderType AMBER_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/lithophage_eyes.png"));

    public LithophageEyesLayer(RenderLayerParent<LithophageRenderState, LithophageModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       LithophageRenderState state, float yRot, float xRot) {
        if (state.fissureGlowing) {
            super.submit(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot);
        }
    }

    @Override
    public RenderType renderType() {
        return AMBER_GLOW;
    }
}

