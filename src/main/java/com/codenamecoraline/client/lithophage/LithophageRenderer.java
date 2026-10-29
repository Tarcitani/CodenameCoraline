package com.codenamecoraline.client.lithophage;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.lithophage.Lithophage;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/** Client renderer for the Lithophage. */
public final class LithophageRenderer extends MobRenderer<Lithophage, LithophageRenderState, LithophageModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/lithophage.png");

    public LithophageRenderer(EntityRendererProvider.Context context) {
        super(context, new LithophageModel(context.bakeLayer(LithophageModel.LAYER)), 0.6F);
        this.addLayer(new LithophageEyesLayer(this));
    }

    @Override
    public LithophageRenderState createRenderState() {
        return new LithophageRenderState();
    }

    @Override
    public Identifier getTextureLocation(LithophageRenderState state) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees() {
        // The custom death collapse animation handles the fall instead of vanilla's 90-degree sideways flip.
        return 0.0F;
    }

    @Override
    public void extractRenderState(Lithophage entity, LithophageRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.state = entity.getState();
        state.attackType = entity.getActiveAttack();
        state.awakeningProgress = entity.getAwakeningProgress();
        state.fissureGlowing = state.state != Lithophage.State.DORMANT
                && state.state != Lithophage.State.DYING
                && ((state.state != Lithophage.State.AWAKENING && state.state != Lithophage.State.FALLING_ASLEEP)
                        || state.awakeningProgress > 0.25F);
        state.holdingShard = false;
        state.idleSeed = (entity.getId() * 31) % 300;

        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.idleAlternateAnimationState.copyFrom(entity.idleAlternateAnimationState);
        state.awakeningAnimationState.copyFrom(entity.awakeningAnimationState);
        state.alertAnimationState.copyFrom(entity.alertAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.turnAnimationState.copyFrom(entity.turnAnimationState);
        state.attackSlamAnimationState.copyFrom(entity.attackSlamAnimationState);
        state.attackSweepAnimationState.copyFrom(entity.attackSweepAnimationState);
        state.attackShardAnimationState.copyFrom(entity.attackShardAnimationState);
        state.attackRecoveryAnimationState.copyFrom(entity.attackRecoveryAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.fallAsleepAnimationState.copyFrom(entity.fallAsleepAnimationState);
    }
}

