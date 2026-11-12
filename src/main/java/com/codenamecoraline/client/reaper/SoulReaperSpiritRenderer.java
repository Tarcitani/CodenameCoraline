package com.codenamecoraline.client.reaper;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.reaper.SoulReaper;
import com.codenamecoraline.entity.spirit.BossSpirit;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/**
 * Post-death spirit of the Reaper of Wandering Souls: his own 5.4m model rendered in translucent cyan-blue rising and
 * fading out after defeat.
 */
public final class SoulReaperSpiritRenderer
        extends LivingEntityRenderer<BossSpirit, SoulReaperRenderState, SoulReaperModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/soul_reaper.png");

    public SoulReaperSpiritRenderer(EntityRendererProvider.Context context) {
        super(context, new SoulReaperModel(context.bakeLayer(SoulReaperModel.LAYER_LOCATION)), 0.0F);
    }

    @Override
    public SoulReaperRenderState createRenderState() {
        return new SoulReaperRenderState();
    }

    @Override
    public Identifier getTextureLocation(SoulReaperRenderState state) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    public void extractRenderState(BossSpirit entity, SoulReaperRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.state = SoulReaper.State.COMBAT_IDLE;
        state.stage = SoulReaper.Stage.STAGE_1;
        state.locomotionMode = SoulReaper.LocomotionMode.GLIDE;
        state.actionId = SoulReaper.ActionId.IDLE_1;
        state.reactId = SoulReaper.ReactId.NONE;
        state.primaryAnimState.copyFrom(entity.poseState);
    }

    @Override
    protected RenderType getRenderType(
            SoulReaperRenderState state,
            boolean bodyVisible,
            boolean translucent,
            boolean glowing) {
        return RenderTypes.entityTranslucent(TEXTURE);
    }

    @Override
    protected int getModelTint(SoulReaperRenderState state) {
        return ARGB.color((int) (170.0F * BossSpirit.visibility(state.ageInTicks)), 136, 224, 255);
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
