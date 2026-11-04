package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.entity.babydragon.DragonEmber;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public final class DragonEmberRenderer extends ThrownItemRenderer<DragonEmber> {
    private static final class State extends ThrownItemRenderState { float size = 1; }
    public DragonEmberRenderer(EntityRendererProvider.Context context) { super(context, .35F, true); }
    @Override public ThrownItemRenderState createRenderState() { return new State(); }
    @Override public void extractRenderState(DragonEmber entity, ThrownItemRenderState state, float partial) {
        super.extractRenderState(entity, state, partial);
        ((State)state).size = switch (entity.getDragonStage()) { case BABY -> 1; case YOUNG -> 2.5F; case ADULT -> 7; };
    }
    @Override public void submit(ThrownItemRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        float size = ((State)state).size;
        stack.pushPose(); stack.scale(size, size, size); super.submit(state, stack, collector, camera); stack.popPose();
    }
}
