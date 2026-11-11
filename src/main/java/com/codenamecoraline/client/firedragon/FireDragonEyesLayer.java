package com.codenamecoraline.client.firedragon;

import com.codenamecoraline.CodenameCoraline;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Emissive eye highlights, sharing the exact base UV coordinates. */
public final class FireDragonEyesLayer extends EyesLayer<FireDragonRenderState, FireDragonModel> {
    private static final RenderType EYE_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/fire_dragon/eyes.png"));

    public FireDragonEyesLayer(RenderLayerParent<FireDragonRenderState, FireDragonModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       FireDragonRenderState state, float yRot, float xRot) {
        if (state.glowing) {
            super.submit(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot);
        }
    }

    @Override
    public RenderType renderType() {
        return EYE_GLOW;
    }
}
