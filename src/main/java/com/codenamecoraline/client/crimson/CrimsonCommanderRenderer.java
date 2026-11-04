package com.codenamecoraline.client.crimson;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.crimson.CrimsonCommander;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/** Client renderer for the Crimson Commander (`codenamecoraline:crimson_commander`), drawn at real size. */
public final class CrimsonCommanderRenderer
        extends MobRenderer<CrimsonCommander, CrimsonCommanderRenderState, CrimsonCommanderModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/crimson_commander.png");

    public CrimsonCommanderRenderer(EntityRendererProvider.Context context) {
        super(context, new CrimsonCommanderModel(context.bakeLayer(CrimsonCommanderModel.LAYER)), 0.9F);
        this.addLayer(new CrimsonEyesLayer(this, CrimsonEyesLayer.COMMANDER));
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
        return 0.0F; // he kneels instead of toppling over (DEATH_KNEEL)
    }

    @Override
    public void extractRenderState(CrimsonCommander entity, CrimsonCommanderRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.anim = entity.getAnim();
        state.animState.copyFrom(entity.animState);
        state.overlay = entity.getOverlay();
        state.overlayState.copyFrom(entity.overlayState);
        state.stage = entity.getStage();
        state.vanguard = false;
        state.lookAtTarget = entity.getActivity() == CrimsonCommander.Activity.GROUND;
    }
}
