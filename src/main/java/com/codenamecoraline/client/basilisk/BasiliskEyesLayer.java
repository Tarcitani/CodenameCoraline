package com.codenamecoraline.client.basilisk;

import com.codenamecoraline.CodenameCoraline;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public final class BasiliskEyesLayer extends EyesLayer<BasiliskRenderState, BasiliskModel> {
    private static final RenderType EYES = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/basilisk_eyes.png"));
    public BasiliskEyesLayer(RenderLayerParent<BasiliskRenderState, BasiliskModel> parent) { super(parent); }
    @Override public RenderType renderType() { return EYES; }
}
