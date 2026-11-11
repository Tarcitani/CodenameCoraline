package com.codenamecoraline.client.firedragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.firedragon.FireDragon;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public final class FireDragonRenderer extends MobRenderer<FireDragon, FireDragonRenderState, FireDragonModel> {
    private static final Identifier[] SKINS = new Identifier[125];
    static {
        for (int p = 0; p < 5; p++) for (int s = 0; s < 5; s++) for (int a = 0; a < 5; a++)
            SKINS[p * 25 + s * 5 + a] = Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID,
                    "textures/entity/fire_dragon/" + p + "_" + s + "_" + a + ".png");
    }
    public FireDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new FireDragonModel(context.bakeLayer(FireDragonModel.LAYER)), 0.3F);
        addLayer(new FireDragonEyesLayer(this));
    }
    @Override public FireDragonRenderState createRenderState() { return new FireDragonRenderState(); }
    @Override public Identifier getTextureLocation(FireDragonRenderState state) { return SKINS[state.colorCode]; }
    @Override protected float getFlipDegrees() { return 0.0F; }
    @Override public void extractRenderState(FireDragon entity, FireDragonRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.hurtTicks = Math.max(0, entity.hurtTime - partialTick);
        state.colorCode = entity.getColorCode();
        state.airborne = !entity.onGround() && !entity.isInWater();
        state.sitting = entity.isInSittingPose();
        state.glowing = !entity.isDeadOrDying();
        state.preview = entity.isVisualPreview();
        state.clip = entity.getVisualAnimation();
        state.clipTicks = entity.getVisualAnimationTicks(partialTick);
    }
}
