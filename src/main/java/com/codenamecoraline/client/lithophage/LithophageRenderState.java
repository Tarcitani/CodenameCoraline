package com.codenamecoraline.client.lithophage;

import com.codenamecoraline.entity.lithophage.Lithophage;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Render state for the Lithophage, carrying state-machine and keyframe animation states to the client model. */
public final class LithophageRenderState extends LivingEntityRenderState {
    public Lithophage.State state = Lithophage.State.DORMANT;
    public Lithophage.AttackType attackType = Lithophage.AttackType.NONE;
    public float awakeningProgress = 0.0F;
    public boolean fissureGlowing = false;
    public boolean holdingShard = false;
    public float idleSeed = 0.0F;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState idleAlternateAnimationState = new AnimationState();
    public final AnimationState awakeningAnimationState = new AnimationState();
    public final AnimationState alertAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState turnAnimationState = new AnimationState();
    public final AnimationState attackSlamAnimationState = new AnimationState();
    public final AnimationState attackSweepAnimationState = new AnimationState();
    public final AnimationState attackShardAnimationState = new AnimationState();
    public final AnimationState attackRecoveryAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();
    public final AnimationState fallAsleepAnimationState = new AnimationState();
}

