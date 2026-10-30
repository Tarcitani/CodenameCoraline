package com.codenamecoraline.client.canopy;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.canopy.CanopyBreaker;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/** Client renderer for the Canopy Breaker (`codenamecoraline:canopy_breaker`). */
public final class CanopyBreakerRenderer
        extends MobRenderer<CanopyBreaker, CanopyBreakerRenderState, CanopyBreakerModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/canopy_breaker.png");

    public CanopyBreakerRenderer(EntityRendererProvider.Context context) {
        super(context, new CanopyBreakerModel(context.bakeLayer(CanopyBreakerModel.LAYER)), 1.1F);
        this.addLayer(new CanopyBreakerEyesLayer(this));
    }

    @Override
    public CanopyBreakerRenderState createRenderState() {
        return new CanopyBreakerRenderState();
    }

    @Override
    public Identifier getTextureLocation(CanopyBreakerRenderState state) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected void scale(CanopyBreakerRenderState state, PoseStack poseStack) {
        poseStack.scale(CanopyBreakerModel.SCALE, CanopyBreakerModel.SCALE, CanopyBreakerModel.SCALE);
    }

    @Override
    public void extractRenderState(CanopyBreaker entity, CanopyBreakerRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.anim = entity.getAnim();
        state.animState.copyFrom(entity.animState);
        state.overlay = entity.getOverlay();
        state.overlayState.copyFrom(entity.overlayState);
        state.clubDrawn = entity.isClubDrawn();
        state.stage = entity.getStage();
        state.lookAtTarget = !entity.isClimbingAnimation();
    }
}
