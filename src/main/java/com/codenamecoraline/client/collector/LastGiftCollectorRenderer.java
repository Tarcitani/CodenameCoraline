package com.codenamecoraline.client.collector;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.collector.LastGiftCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

/** Client renderer for the Collector of the Last Gift (`codenamecoraline:last_gift_collector`). */
public final class LastGiftCollectorRenderer
        extends MobRenderer<LastGiftCollector, LastGiftCollectorRenderState, LastGiftCollectorModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/last_gift_collector.png");

    public LastGiftCollectorRenderer(EntityRendererProvider.Context context) {
        super(context, new LastGiftCollectorModel(context.bakeLayer(LastGiftCollectorModel.LAYER)), 1.15F);
        this.addLayer(new LastGiftCollectorEyesLayer(this));
    }

    @Override
    public LastGiftCollectorRenderState createRenderState() {
        return new LastGiftCollectorRenderState();
    }

    @Override
    public Identifier getTextureLocation(LastGiftCollectorRenderState state) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    public void submit(
            LastGiftCollectorRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState
    ) {
        if (state.bossState == LastGiftCollector.BossState.INTRO_CINEMATIC
                && (state.introStage == LastGiftCollector.IntroStage.INTRO_PRELUDE
                    || state.introStage == LastGiftCollector.IntroStage.INTRO_LIGHTNING)) {
            return;
        }
        super.submit(state, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public void extractRenderState(LastGiftCollector entity, LastGiftCollectorRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.bossState = entity.getBossState();
        state.introStage = entity.getIntroStage();
        state.activeAttack = entity.getActiveAttack();
        state.phaseTwo = entity.isPhaseTwo();
        state.cageOpen = entity.isCageOpen();
        state.eyesGlowing = state.bossState != LastGiftCollector.BossState.DEAD
                && (state.bossState != LastGiftCollector.BossState.DEATH_CINEMATIC || entity.getStateTicks() < 45);
        state.idleSeed = (entity.getId() * 37) % 360;

        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.idleAlternateAnimationState.copyFrom(entity.idleAlternateAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.appearAnimationState.copyFrom(entity.appearAnimationState);
        state.jumpFromSleighAnimationState.copyFrom(entity.jumpFromSleighAnimationState);
        state.fallAnimationState.copyFrom(entity.fallAnimationState);
        state.landAnimationState.copyFrom(entity.landAnimationState);
        state.intimidateAnimationState.copyFrom(entity.intimidateAnimationState);
        state.attackScythePrepAnimationState.copyFrom(entity.attackScythePrepAnimationState);
        state.attackScytheExecAnimationState.copyFrom(entity.attackScytheExecAnimationState);
        state.attackHollowBellPrepAnimationState.copyFrom(entity.attackHollowBellPrepAnimationState);
        state.attackHollowBellImpactAnimationState.copyFrom(entity.attackHollowBellImpactAnimationState);
        state.attackPawnsAnimationState.copyFrom(entity.attackPawnsAnimationState);
        state.attackSolsticeJumpAnimationState.copyFrom(entity.attackSolsticeJumpAnimationState);
        state.attackCastAnimationState.copyFrom(entity.attackCastAnimationState);
        state.attackSpinAnimationState.copyFrom(entity.attackSpinAnimationState);
        state.attackSlamAnimationState.copyFrom(entity.attackSlamAnimationState);
        state.rageAnimationState.copyFrom(entity.rageAnimationState);
        state.recoveryAnimationState.copyFrom(entity.recoveryAnimationState);
        state.phaseTransitionAnimationState.copyFrom(entity.phaseTransitionAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
    }
}

