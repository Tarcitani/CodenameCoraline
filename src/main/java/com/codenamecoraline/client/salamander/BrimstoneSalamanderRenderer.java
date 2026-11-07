package com.codenamecoraline.client.salamander;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.salamander.BrimstoneSalamander;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/** Client renderer for the Brimstone Salamander. */
public final class BrimstoneSalamanderRenderer
        extends MobRenderer<BrimstoneSalamander, BrimstoneSalamanderRenderState, BrimstoneSalamanderModel> {
    private static final Identifier TEXTURE_BRIMSTONE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/brimstone_salamander.png");
    private static final Identifier TEXTURE_SOULFLAME = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/brimstone_salamander_soulflame.png");
    private static final Identifier TEXTURE_CRIMSON = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/brimstone_salamander_crimson.png");

    public BrimstoneSalamanderRenderer(EntityRendererProvider.Context context) {
        super(context, new BrimstoneSalamanderModel(context.bakeLayer(BrimstoneSalamanderModel.LAYER)), 0.65F);
        this.addLayer(new BrimstoneSalamanderEyesLayer(this));
    }

    @Override
    public BrimstoneSalamanderRenderState createRenderState() {
        return new BrimstoneSalamanderRenderState();
    }

    @Override
    public Identifier getTextureLocation(BrimstoneSalamanderRenderState state) {
        return switch (state.variant) {
            case BRIMSTONE -> TEXTURE_BRIMSTONE;
            case SOULFLAME -> TEXTURE_SOULFLAME;
            case CRIMSON -> TEXTURE_CRIMSON;
        };
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    public void extractRenderState(BrimstoneSalamander entity, BrimstoneSalamanderRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.state = entity.getState();
        state.variant = entity.getVariant();
        state.swimmingInLava = entity.isSwimmingInLava();
        state.fissuresGlowing = !entity.isDeadOrDying();

        state.idleBreatheAnimationState.copyFrom(entity.idleBreatheAnimationState);
        state.idleThroatPuffAnimationState.copyFrom(entity.idleThroatPuffAnimationState);
        state.idleTailSwayAnimationState.copyFrom(entity.idleTailSwayAnimationState);
        state.idleSniffAnimationState.copyFrom(entity.idleSniffAnimationState);
        state.swimLavaAnimationState.copyFrom(entity.swimLavaAnimationState);
        state.snapBitePrepAnimationState.copyFrom(entity.snapBitePrepAnimationState);
        state.snapBiteExecAnimationState.copyFrom(entity.snapBiteExecAnimationState);
        state.snapBiteRecoveryAnimationState.copyFrom(entity.snapBiteRecoveryAnimationState);
        state.sulfurSpitPrepAnimationState.copyFrom(entity.sulfurSpitPrepAnimationState);
        state.sulfurSpitExecAnimationState.copyFrom(entity.sulfurSpitExecAnimationState);
        state.sulfurSpitRecoveryAnimationState.copyFrom(entity.sulfurSpitRecoveryAnimationState);
        state.tailWhipPrepAnimationState.copyFrom(entity.tailWhipPrepAnimationState);
        state.tailWhipExecAnimationState.copyFrom(entity.tailWhipExecAnimationState);
        state.tailWhipRecoveryAnimationState.copyFrom(entity.tailWhipRecoveryAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
    }
}

