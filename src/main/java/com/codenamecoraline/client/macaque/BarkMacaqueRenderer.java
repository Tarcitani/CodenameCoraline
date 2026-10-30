package com.codenamecoraline.client.macaque;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.macaque.BarkMacaque;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/** Client renderer for the Bark Macaque (`codenamecoraline:bark_macaque`). */
public final class BarkMacaqueRenderer extends MobRenderer<BarkMacaque, BarkMacaqueRenderState, BarkMacaqueModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/bark_macaque.png");

    public BarkMacaqueRenderer(EntityRendererProvider.Context context) {
        super(context, new BarkMacaqueModel(context.bakeLayer(BarkMacaqueModel.LAYER)), 0.35F);
        this.addLayer(new BarkMacaqueEyesLayer(this));
    }

    @Override
    public BarkMacaqueRenderState createRenderState() {
        return new BarkMacaqueRenderState();
    }

    @Override
    public Identifier getTextureLocation(BarkMacaqueRenderState state) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    public void extractRenderState(BarkMacaque entity, BarkMacaqueRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.anim = entity.getAnim();
        state.animState.copyFrom(entity.animState);
        state.lookAtTarget = !entity.isClimbingAnimation();
    }
}
