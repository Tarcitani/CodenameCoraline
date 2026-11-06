package com.codenamecoraline.client.salamander;

import com.codenamecoraline.CodenameCoraline;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Emissive layer for the Brimstone Salamander's glowing throat bellows, eyes, forked tongue,
 * sulphur spots, and magma fissures along the dorsal ridge and tail.
 */
public final class BrimstoneSalamanderEyesLayer
        extends EyesLayer<BrimstoneSalamanderRenderState, BrimstoneSalamanderModel> {
    private static final RenderType BRIMSTONE_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/brimstone_salamander_eyes.png"));

    public BrimstoneSalamanderEyesLayer(
            RenderLayerParent<BrimstoneSalamanderRenderState, BrimstoneSalamanderModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       BrimstoneSalamanderRenderState state, float yRot, float xRot) {
        if (state.fissuresGlowing) {
            super.submit(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot);
        }
    }

    @Override
    public RenderType renderType() {
        return BRIMSTONE_GLOW;
    }
}

