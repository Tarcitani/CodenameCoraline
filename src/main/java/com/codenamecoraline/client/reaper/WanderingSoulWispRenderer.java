package com.codenamecoraline.client.reaper;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.reaper.WanderingSoulWisp;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public final class WanderingSoulWispRenderer
        extends MobRenderer<WanderingSoulWisp, WanderingSoulWispRenderState, WanderingSoulWispModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/wandering_soul_wisp.png");

    public WanderingSoulWispRenderer(EntityRendererProvider.Context context) {
        super(context, new WanderingSoulWispModel(context.bakeLayer(WanderingSoulWispModel.LAYER_LOCATION)), 0.20F);
    }

    @Override
    public WanderingSoulWispRenderState createRenderState() {
        return new WanderingSoulWispRenderState();
    }

    @Override
    public void extractRenderState(WanderingSoulWisp entity, WanderingSoulWispRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.lifetimeTicks = entity.tickCount;
    }

    @Override
    protected int getBlockLightLevel(WanderingSoulWisp entity, BlockPos pos) {
        return 15;
    }

    @Override
    public Identifier getTextureLocation(WanderingSoulWispRenderState state) {
        return TEXTURE;
    }
}
