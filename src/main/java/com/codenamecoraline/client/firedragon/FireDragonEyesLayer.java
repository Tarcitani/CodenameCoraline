package com.codenamecoraline.client.firedragon;

import com.codenamecoraline.CodenameCoraline;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Emissive eye highlights, sharing the exact base UV coordinates for baby and young stages. */
public final class FireDragonEyesLayer extends EyesLayer<FireDragonRenderState, FireDragonModel> {
    private static final RenderType BABY_EYE_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/fire_dragon/eyes.png"));
    private static final RenderType YOUNG_EYE_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/fire_dragon/young/eyes.png"));

    private final boolean forYoung;

    public FireDragonEyesLayer(RenderLayerParent<FireDragonRenderState, FireDragonModel> parent, boolean forYoung) {
        super(parent);
        this.forYoung = forYoung;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       FireDragonRenderState state, float yRot, float xRot) {
        if (state.glowing && state.young == forYoung) {
            super.submit(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot);
        }
    }

    @Override
    public RenderType renderType() {
        return forYoung ? YOUNG_EYE_GLOW : BABY_EYE_GLOW;
    }
}
