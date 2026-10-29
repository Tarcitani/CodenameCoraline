package com.codenamecoraline.client.collector;

import com.codenamecoraline.entity.collector.LastGiftCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Render state for the Collector of the Last Gift, carrying boss states and keyframe animations to the model. */
public final class LastGiftCollectorRenderState extends LivingEntityRenderState {
    public LastGiftCollector.BossState bossState = LastGiftCollector.BossState.PHASE_ONE;
    public LastGiftCollector.IntroStage introStage = LastGiftCollector.IntroStage.INTRO_COMPLETE;
    public LastGiftCollector.AttackType activeAttack = LastGiftCollector.AttackType.NONE;
    public boolean phaseTwo = false;
    public boolean cageOpen = false;
    public boolean eyesGlowing = true;
    public float idleSeed = 0.0F;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState idleAlternateAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState appearAnimationState = new AnimationState();
    public final AnimationState jumpFromSleighAnimationState = new AnimationState();
    public final AnimationState fallAnimationState = new AnimationState();
    public final AnimationState landAnimationState = new AnimationState();
    public final AnimationState intimidateAnimationState = new AnimationState();
    public final AnimationState attackScythePrepAnimationState = new AnimationState();
    public final AnimationState attackScytheExecAnimationState = new AnimationState();
    public final AnimationState attackHollowBellPrepAnimationState = new AnimationState();
    public final AnimationState attackHollowBellImpactAnimationState = new AnimationState();
    public final AnimationState attackPawnsAnimationState = new AnimationState();
    public final AnimationState attackSolsticeJumpAnimationState = new AnimationState();
    public final AnimationState attackCastAnimationState = new AnimationState();
    public final AnimationState attackSpinAnimationState = new AnimationState();
    public final AnimationState attackSlamAnimationState = new AnimationState();
    public final AnimationState rageAnimationState = new AnimationState();
    public final AnimationState recoveryAnimationState = new AnimationState();
    public final AnimationState phaseTransitionAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();
}

