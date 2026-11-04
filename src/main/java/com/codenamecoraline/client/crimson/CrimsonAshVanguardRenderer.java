package com.codenamecoraline.client.crimson;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.crimson.CrimsonAshVanguard;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/**
 * Client renderer for the Crimson Ash Vanguard: the Commander's own armour model in grey ash, without plume, cape or
 * spikes, scaled down to 1.95 blocks.
 */
public final class CrimsonAshVanguardRenderer
        extends MobRenderer<CrimsonAshVanguard, CrimsonCommanderRenderState, CrimsonCommanderModel> {
    /** 1.95 blocks out of the Commander's 3.5. */
    public static final float SCALE = CrimsonAshVanguard.HEIGHT / 3.5F;
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/crimson_ash_vanguard.png");

    public CrimsonAshVanguardRenderer(EntityRendererProvider.Context context) {
        super(context, new CrimsonCommanderModel(context.bakeLayer(CrimsonCommanderModel.LAYER)), 0.5F);
        this.addLayer(new CrimsonEyesLayer(this, CrimsonEyesLayer.VANGUARD));
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
        return 0.0F; // it crumbles to ash instead of toppling over
    }

    @Override
    protected void scale(CrimsonCommanderRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public void extractRenderState(CrimsonAshVanguard entity, CrimsonCommanderRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.anim = entity.getAnim();
        state.animState.copyFrom(entity.animState);
        state.overlay = null;
        state.vanguard = true;
        state.lookAtTarget = true;
    }
}
