package com.codenamecoraline.client.collector;

import com.codenamecoraline.CodenameCoraline;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Emissive layer for the Collector of the Last Gift: illuminates the porcelain mask fissures,
 * frost-iron runes on the Winter Scythe, and the cracked bell core inside the dorsal reliquary cage.
 * Extinguishes during the death collapse.
 */
public final class LastGiftCollectorEyesLayer
        extends EyesLayer<LastGiftCollectorRenderState, LastGiftCollectorModel> {
    private static final RenderType EMISSIVE_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/last_gift_collector_eyes.png"));

    public LastGiftCollectorEyesLayer(
            RenderLayerParent<LastGiftCollectorRenderState, LastGiftCollectorModel> parent
    ) {
        super(parent);
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            LastGiftCollectorRenderState state,
            float yRot,
            float xRot
    ) {
        if (state.eyesGlowing) {
            super.submit(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot);
        }
    }

    @Override
    public RenderType renderType() {
        return EMISSIVE_GLOW;
    }
}

