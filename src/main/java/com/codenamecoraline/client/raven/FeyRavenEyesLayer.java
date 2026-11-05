package com.codenamecoraline.client.raven;

import com.codenamecoraline.CodenameCoraline;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Emissive layer for the Fey Raven's vertical third eye, lateral eyes, and silver-cyan fey runes
 * along the wing coverts and breast ruff.
 */
public final class FeyRavenEyesLayer extends EyesLayer<FeyRavenRenderState, FeyRavenModel> {
    private static final RenderType FEY_RUNE_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/fey_raven_eyes.png"));

    public FeyRavenEyesLayer(RenderLayerParent<FeyRavenRenderState, FeyRavenModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       FeyRavenRenderState state, float yRot, float xRot) {
        if (state.runesGlowing) {
            super.submit(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot);
        }
    }

    @Override
    public RenderType renderType() {
        return FEY_RUNE_GLOW;
    }
}

