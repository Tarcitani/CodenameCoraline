package com.codenamecoraline.client.crimson;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.crimson.CrimsonAnim;
import com.codenamecoraline.entity.spirit.BossSpirit;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/** The Crimson Commander's spirit: his own model, translucent and pale blue, standing up from the kneeling body. */
public final class CrimsonCommanderSpiritRenderer
        extends LivingEntityRenderer<BossSpirit, CrimsonCommanderRenderState, CrimsonCommanderModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/crimson_commander.png");

    public CrimsonCommanderSpiritRenderer(EntityRendererProvider.Context context) {
        super(context, new CrimsonCommanderModel(context.bakeLayer(CrimsonCommanderModel.LAYER)), 0.0F);
    }

    @Override
    public CrimsonCommanderRenderState createRenderState() {
        return new CrimsonCommanderRenderState();
    }

    @Override
    public Identifier getTextureLocation(CrimsonCommanderRenderState state) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    public void extractRenderState(BossSpirit entity, CrimsonCommanderRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.anim = CrimsonAnim.COMBAT_IDLE;
        state.animState.copyFrom(entity.poseState);
        state.overlay = null;
        state.vanguard = false;
        state.lookAtTarget = false;
    }

    @Override
    protected RenderType getRenderType(CrimsonCommanderRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucent(TEXTURE);
    }

    @Override
    protected int getModelTint(CrimsonCommanderRenderState state) {
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
