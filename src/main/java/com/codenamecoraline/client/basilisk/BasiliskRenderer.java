package com.codenamecoraline.client.basilisk;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.basilisk.Basilisk;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

public final class BasiliskRenderer extends MobRenderer<Basilisk, BasiliskRenderState, BasiliskModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/basilisk.png");

    public BasiliskRenderer(EntityRendererProvider.Context context) {
        super(context, new BasiliskModel(context.bakeLayer(BasiliskModel.LAYER)), 0.65F);
        this.addLayer(new BasiliskEyesLayer(this));
    }
    @Override public BasiliskRenderState createRenderState() { return new BasiliskRenderState(); }
    @Override public Identifier getTextureLocation(BasiliskRenderState state) { return TEXTURE; }
    @Override public void extractRenderState(Basilisk entity, BasiliskRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.state = entity.getState();
        state.inflation = entity.getInflation();
        state.airborne = !entity.onGround() && !entity.isInWater();
        state.verticalSpeed = (float) entity.getDeltaMovement().y;
        state.idleSeed = entity.getId() * 17 % 200;
        state.concealed = entity.getTarget() == null && (entity.isInWater()
                || entity.getBlockStateOn().is(Blocks.MUD) || entity.getBlockStateOn().is(Blocks.SCULK));
    }
}
