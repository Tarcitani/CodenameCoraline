package com.codenamecoraline.client.canopy;

import com.codenamecoraline.CodenameCoraline;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Glowing amber eyes of the Canopy Breaker. */
public final class CanopyBreakerEyesLayer extends EyesLayer<CanopyBreakerRenderState, CanopyBreakerModel> {
    private static final RenderType EYES = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/canopy_breaker_eyes.png"));

    public CanopyBreakerEyesLayer(RenderLayerParent<CanopyBreakerRenderState, CanopyBreakerModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }
}
