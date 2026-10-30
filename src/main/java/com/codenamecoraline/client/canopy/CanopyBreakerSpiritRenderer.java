package com.codenamecoraline.client.canopy;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.canopy.CanopyAnim;
import com.codenamecoraline.entity.spirit.BossSpirit;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/** The Canopy Breaker's spirit: his own model, translucent and pale blue, rising from where he fell. */
public final class CanopyBreakerSpiritRenderer
        extends LivingEntityRenderer<BossSpirit, CanopyBreakerRenderState, CanopyBreakerModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/canopy_breaker.png");

    public CanopyBreakerSpiritRenderer(EntityRendererProvider.Context context) {
        super(context, new CanopyBreakerModel(context.bakeLayer(CanopyBreakerModel.LAYER)), 0.0F);
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
    public void extractRenderState(BossSpirit entity, CanopyBreakerRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.anim = CanopyAnim.IDLE_BREATHING;
        state.animState.copyFrom(entity.poseState);
        state.overlay = null;
        state.clubDrawn = false;
        state.lookAtTarget = false;
    }

    @Override
    protected RenderType getRenderType(CanopyBreakerRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucent(TEXTURE);
    }

    @Override
    protected int getModelTint(CanopyBreakerRenderState state) {
        return ARGB.color((int) (165.0F * BossSpirit.visibility(state.ageInTicks)), 150, 205, 255);
    }

    @Override
    protected int getBlockLightLevel(BossSpirit entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected boolean shouldShowName(BossSpirit entity, double distanceToCameraSq) {
        return false;
    }
}
