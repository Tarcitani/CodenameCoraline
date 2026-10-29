package com.codenamecoraline.client.collector;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.collector.GiftMimic;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/** Client renderer for the Gift Mimic (`codenamecoraline:gift_mimic`). */
public final class GiftMimicRenderer extends MobRenderer<GiftMimic, GiftMimicRenderState, GiftMimicModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/gift_mimic.png");

    public GiftMimicRenderer(EntityRendererProvider.Context context) {
        super(context, new GiftMimicModel(context.bakeLayer(GiftMimicModel.LAYER)), 0.5F);
    }

    @Override
    public GiftMimicRenderState createRenderState() {
        return new GiftMimicRenderState();
    }

    @Override
    public Identifier getTextureLocation(GiftMimicRenderState state) {
        return TEXTURE;
    }

    @Override
    public void extractRenderState(GiftMimic entity, GiftMimicRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.fuse = entity.getFuseProgress();
        state.attackProgress = entity.getAttackAnim(partialTick);
    }
}
