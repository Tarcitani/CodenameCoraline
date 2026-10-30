package com.codenamecoraline.client.collector;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.collector.LastGiftCollector;
import com.codenamecoraline.entity.spirit.BossSpirit;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/** Nicolas Noel's spirit: his own model, translucent and pale blue, rising from where he fell. */
public final class LastGiftCollectorSpiritRenderer
        extends LivingEntityRenderer<BossSpirit, LastGiftCollectorRenderState, LastGiftCollectorModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/last_gift_collector.png");

    public LastGiftCollectorSpiritRenderer(EntityRendererProvider.Context context) {
        super(context, new LastGiftCollectorModel(context.bakeLayer(LastGiftCollectorModel.LAYER)), 0.0F);
    }

    @Override
    public LastGiftCollectorRenderState createRenderState() {
        return new LastGiftCollectorRenderState();
    }

    @Override
    public Identifier getTextureLocation(LastGiftCollectorRenderState state) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    public void extractRenderState(BossSpirit entity, LastGiftCollectorRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.bossState = LastGiftCollector.BossState.PHASE_ONE;
        state.introStage = LastGiftCollector.IntroStage.INTRO_COMPLETE;
        state.activeAttack = LastGiftCollector.AttackType.NONE;
        state.eyesGlowing = false;
        state.idleAnimationState.copyFrom(entity.poseState);
    }

    @Override
    protected RenderType getRenderType(LastGiftCollectorRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucent(TEXTURE);
    }

    @Override
    protected int getModelTint(LastGiftCollectorRenderState state) {
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
