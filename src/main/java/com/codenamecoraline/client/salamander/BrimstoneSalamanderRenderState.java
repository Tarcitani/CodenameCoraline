package com.codenamecoraline.client.salamander;

import com.codenamecoraline.entity.salamander.BrimstoneSalamander;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Client render state for the Brimstone Salamander, carrying state-machine and keyframe animation states. */
public final class BrimstoneSalamanderRenderState extends LivingEntityRenderState {
    public BrimstoneSalamander.State state = BrimstoneSalamander.State.IDLE;
    public com.codenamecoraline.entity.salamander.BrimstoneSalamanderVariant variant =
            com.codenamecoraline.entity.salamander.BrimstoneSalamanderVariant.BRIMSTONE;
    public boolean swimmingInLava = false;
    public boolean fissuresGlowing = true;

    public final AnimationState idleBreatheAnimationState = new AnimationState();
    public final AnimationState idleThroatPuffAnimationState = new AnimationState();
    public final AnimationState idleTailSwayAnimationState = new AnimationState();
    public final AnimationState idleSniffAnimationState = new AnimationState();
    public final AnimationState swimLavaAnimationState = new AnimationState();
    public final AnimationState snapBitePrepAnimationState = new AnimationState();
    public final AnimationState snapBiteExecAnimationState = new AnimationState();
    public final AnimationState snapBiteRecoveryAnimationState = new AnimationState();
    public final AnimationState sulfurSpitPrepAnimationState = new AnimationState();
    public final AnimationState sulfurSpitExecAnimationState = new AnimationState();
    public final AnimationState sulfurSpitRecoveryAnimationState = new AnimationState();
    public final AnimationState tailWhipPrepAnimationState = new AnimationState();
    public final AnimationState tailWhipExecAnimationState = new AnimationState();
    public final AnimationState tailWhipRecoveryAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();
}

