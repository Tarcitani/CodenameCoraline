package com.codenamecoraline.client.reaper;

import com.codenamecoraline.entity.reaper.OmenHare;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public final class OmenHareRenderState extends LivingEntityRenderState {
    public OmenHare.State state = OmenHare.State.APPROACHING;
    public int ruptureTicks;
    public final AnimationState standStareAnimState = new AnimationState();
    public final AnimationState tiltTauntAnimState = new AnimationState();
    public final AnimationState ruptureAnimState = new AnimationState();
}

