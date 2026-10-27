package com.codenamecoraline.client.redcap;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.redcap.Redcap;
import com.codenamecoraline.entity.redcap.RedcapAge;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public final class RedcapRenderer extends MobRenderer<Redcap, RedcapRenderState, RedcapModel> {
    private static final Identifier[] TEXTURES = new Identifier[RedcapAge.values().length];
    private static final int BLINK_PERIOD_TICKS = 110;
    private static final int BLINK_TICKS = 3;

    static {
        for (RedcapAge age : RedcapAge.values()) {
            TEXTURES[age.ordinal()] = Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, age.texture);
        }
    }

    public RedcapRenderer(EntityRendererProvider.Context context) {
        super(context, new RedcapModel(context.bakeLayer(RedcapModel.LAYER)), 0.5F);
        this.addLayer(new RedcapEyesLayer(this));
    }

    @Override
    public RedcapRenderState createRenderState() {
        return new RedcapRenderState();
    }

    @Override
    public void extractRenderState(Redcap entity, RedcapRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
        state.age = entity.getAge();
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.clawLeftAnimationState.copyFrom(entity.clawLeftAnimationState);
        state.rockThrowAnimationState.copyFrom(entity.rockThrowAnimationState);
        state.sniffAnimationState.copyFrom(entity.sniffAnimationState);
        state.lookAroundAnimationState.copyFrom(entity.lookAroundAnimationState);
        state.sharpenAnimationState.copyFrom(entity.sharpenAnimationState);
        state.capCheckAnimationState.copyFrom(entity.capCheckAnimationState);
        state.idleSeed = (entity.getId() * 37) % 400;
        state.eyesClosed = (entity.tickCount + entity.getId() * 13) % BLINK_PERIOD_TICKS < BLINK_TICKS;
    }

    @Override
    public Identifier getTextureLocation(RedcapRenderState state) {
        return TEXTURES[state.age.ordinal()];
    }
}
