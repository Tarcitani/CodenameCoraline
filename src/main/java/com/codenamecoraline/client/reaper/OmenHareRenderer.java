package com.codenamecoraline.client.reaper;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.reaper.OmenHare;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public final class OmenHareRenderer extends MobRenderer<OmenHare, OmenHareRenderState, OmenHareModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/omen_hare.png");

    public OmenHareRenderer(EntityRendererProvider.Context context) {
        super(context, new OmenHareModel(context.bakeLayer(OmenHareModel.LAYER_LOCATION)), 0.30F);
        this.addLayer(new OmenHareEyesLayer(this));
    }

    @Override
    public OmenHareRenderState createRenderState() {
        return new OmenHareRenderState();
    }

    @Override
    public void extractRenderState(OmenHare entity, OmenHareRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.state = entity.getState();
        state.ruptureTicks = entity.getRuptureTicks();
        state.standStareAnimState.copyFrom(entity.standStareAnimState);
        state.tiltTauntAnimState.copyFrom(entity.tiltTauntAnimState);
        state.ruptureAnimState.copyFrom(entity.ruptureAnimState);
    }

    @Override
    public Identifier getTextureLocation(OmenHareRenderState state) {
        return TEXTURE;
    }
}

