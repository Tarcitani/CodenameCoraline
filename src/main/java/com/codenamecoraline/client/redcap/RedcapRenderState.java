package com.codenamecoraline.client.redcap;

import com.codenamecoraline.entity.redcap.RedcapAge;
import com.codenamecoraline.entity.redcap.RedcapVariant;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public final class RedcapRenderState extends LivingEntityRenderState {
    public RedcapVariant variant = RedcapVariant.SPEAR;
    public RedcapAge age = RedcapAge.ADULT;
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState clawLeftAnimationState = new AnimationState();
    public final AnimationState rockThrowAnimationState = new AnimationState();
    public final AnimationState sniffAnimationState = new AnimationState();
    public final AnimationState lookAroundAnimationState = new AnimationState();
    public final AnimationState sharpenAnimationState = new AnimationState();
    public final AnimationState capCheckAnimationState = new AnimationState();
    /** Per-entity time offset, so several Redcaps do not breathe and twitch in sync. */
    public float idleSeed;
    public boolean eyesClosed;
}
