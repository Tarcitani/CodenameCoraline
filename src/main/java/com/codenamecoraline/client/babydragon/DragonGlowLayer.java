package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.babydragon.DragonStage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Sparse eyes, heat seams, wing veins and tail embers; the main model remains visible. */
public final class DragonGlowLayer extends EyesLayer<BabyDragonRenderState, DragonModel> {
    private static final RenderType[] TYPES = java.util.Arrays.stream(DragonStage.values()).map(stage -> RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID,"textures/entity/"+stage.name().toLowerCase(java.util.Locale.ROOT)+"_dragon_glow.png"))).toArray(RenderType[]::new);
    public DragonGlowLayer(RenderLayerParent<BabyDragonRenderState, DragonModel> parent) { super(parent); }
    @Override public RenderType renderType() { return TYPES[0]; }
    @Override public void submit(PoseStack stack, SubmitNodeCollector collector, int light, BabyDragonRenderState state, float yaw, float pitch) {
        if (!state.sleeping) collector.order(1).submitModel(this.getParentModel(), state, stack, TYPES[Math.clamp(state.stage,0,2)], light, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
    }
}
