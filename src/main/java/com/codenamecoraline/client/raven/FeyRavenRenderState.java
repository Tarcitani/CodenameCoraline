package com.codenamecoraline.client.raven;

import com.codenamecoraline.entity.raven.FeyRaven;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Client render state for the Fey Raven, carrying state-machine and keyframe animation states. */
public final class FeyRavenRenderState extends LivingEntityRenderState {
    public FeyRaven.State state = FeyRaven.State.WILD_IDLE;
    public boolean airborne = false;
    public boolean tamed = false;
    public boolean sitting = false;
    public boolean hasScroll = false;
    public boolean holdingBerries = false;
    public boolean runesGlowing = true;
    public float idleSeed = 0.0F;

    public final AnimationState idleBreatheAnimationState = new AnimationState();
    public final AnimationState idleHeadTiltAnimationState = new AnimationState();
    public final AnimationState idlePreenAnimationState = new AnimationState();
    public final AnimationState idleCawAnimationState = new AnimationState();
    public final AnimationState flyFlapAnimationState = new AnimationState();
    public final AnimationState flyGlideAnimationState = new AnimationState();
    public final AnimationState peckAttackAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();
}

