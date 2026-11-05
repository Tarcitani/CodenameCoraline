package com.codenamecoraline.client.raven;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.raven.FeyRaven;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

/** Client renderer for the Fey Raven. */
public final class FeyRavenRenderer extends MobRenderer<FeyRaven, FeyRavenRenderState, FeyRavenModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/fey_raven.png");

    public FeyRavenRenderer(EntityRendererProvider.Context context) {
        super(context, new FeyRavenModel(context.bakeLayer(FeyRavenModel.LAYER)), 0.30F);
        this.addLayer(new FeyRavenEyesLayer(this));
    }

    @Override
    public FeyRavenRenderState createRenderState() {
        return new FeyRavenRenderState();
    }

    @Override
    public Identifier getTextureLocation(FeyRavenRenderState state) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    public void extractRenderState(FeyRaven entity, FeyRavenRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.state = entity.getState();
        state.airborne = entity.isAirborne() || !entity.onGround();
        state.tamed = entity.isTame();
        state.sitting = entity.isInSittingPose();
        state.hasScroll = !entity.getScrollItem().isEmpty() || entity.isTame();
        state.holdingBerries = entity.getScrollItem().is(Items.GLOW_BERRIES)
                || entity.getScrollItem().is(Items.SWEET_BERRIES);
        state.runesGlowing = !entity.isDeadOrDying();
        state.idleSeed = (entity.getId() * 31) % 240;

        state.idleBreatheAnimationState.copyFrom(entity.idleBreatheAnimationState);
        state.idleHeadTiltAnimationState.copyFrom(entity.idleHeadTiltAnimationState);
        state.idlePreenAnimationState.copyFrom(entity.idlePreenAnimationState);
        state.idleCawAnimationState.copyFrom(entity.idleCawAnimationState);
        state.flyFlapAnimationState.copyFrom(entity.flyFlapAnimationState);
        state.flyGlideAnimationState.copyFrom(entity.flyGlideAnimationState);
        state.peckAttackAnimationState.copyFrom(entity.peckAttackAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
    }
}

