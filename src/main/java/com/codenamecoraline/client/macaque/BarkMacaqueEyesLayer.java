package com.codenamecoraline.client.macaque;

import com.codenamecoraline.CodenameCoraline;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Glowing amber eyes of the Bark Macaque. */
public final class BarkMacaqueEyesLayer extends EyesLayer<BarkMacaqueRenderState, BarkMacaqueModel> {
    private static final RenderType EYES = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/bark_macaque_eyes.png"));

    public BarkMacaqueEyesLayer(RenderLayerParent<BarkMacaqueRenderState, BarkMacaqueModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }
}
