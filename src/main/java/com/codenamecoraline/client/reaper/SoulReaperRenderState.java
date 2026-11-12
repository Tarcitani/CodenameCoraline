package com.codenamecoraline.client.reaper;

import com.codenamecoraline.entity.reaper.SoulReaper;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public final class SoulReaperRenderState extends LivingEntityRenderState {
    public SoulReaper.State state = SoulReaper.State.COMBAT_IDLE;
    public SoulReaper.Stage stage = SoulReaper.Stage.STAGE_1;
    public SoulReaper.LocomotionMode locomotionMode = SoulReaper.LocomotionMode.GLIDE;
    public SoulReaper.ActionId actionId = SoulReaper.ActionId.NONE;
    public SoulReaper.ReactId reactId = SoulReaper.ReactId.NONE;
    public int actionTick;
    public final AnimationState primaryAnimState = new AnimationState();
    public final AnimationState reactAnimState = new AnimationState();
}

