package com.codenamecoraline.client.salamander;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.salamander.BrimstoneSalamander;
import com.mojang.blaze3d.vertex.PoseStack;
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
    private static final Identifier TEXTURE_BRIMSTONE_BABY = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/brimstone_salamander_baby.png");
    private static final Identifier TEXTURE_SOULFLAME_BABY = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/brimstone_salamander_soulflame_baby.png");
    private static final Identifier TEXTURE_CRIMSON_BABY = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/brimstone_salamander_crimson_baby.png");

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
        if (state.isBaby) {
            return switch (state.variant) {
                case BRIMSTONE -> TEXTURE_BRIMSTONE_BABY;
                case SOULFLAME -> TEXTURE_SOULFLAME_BABY;
                case CRIMSON -> TEXTURE_CRIMSON_BABY;
            };
        }
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
    protected void scale(BrimstoneSalamanderRenderState state, PoseStack stack) {
        if (state.isBaby) {
            stack.scale(0.5F, 0.5F, 0.5F);
        }
    }

    @Override
    protected float getShadowRadius(BrimstoneSalamanderRenderState state) {
        return state.isBaby ? 0.325F : 0.65F;
    }

    @Override
    public void extractRenderState(BrimstoneSalamander entity, BrimstoneSalamanderRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.state = entity.getState();
        state.variant = entity.getVariant();
        state.swimmingInLava = entity.isSwimmingInLava();
        state.fissuresGlowing = !entity.isDeadOrDying();
        state.isBaby = entity.isBaby();

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

