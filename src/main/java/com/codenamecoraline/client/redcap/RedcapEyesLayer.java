package com.codenamecoraline.client.redcap;

import com.codenamecoraline.CodenameCoraline;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Draws the fire-red eyes fullbright, on top of the lighting, from a texture with only the eye pixels.
 * Skipped while blinking, which leaves the dark eye pixels of the base texture visible.
 */
public final class RedcapEyesLayer extends EyesLayer<RedcapRenderState, RedcapModel> {
    private static final RenderType EYES = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/redcap_eyes.png"));

    public RedcapEyesLayer(RenderLayerParent<RedcapRenderState, RedcapModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       RedcapRenderState state, float yRot, float xRot) {
        if (!state.eyesClosed) {
            super.submit(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot);
        }
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }
}
