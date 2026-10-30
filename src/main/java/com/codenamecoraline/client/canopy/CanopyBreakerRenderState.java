package com.codenamecoraline.client.canopy;

import com.codenamecoraline.entity.canopy.CanopyAnim;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;
import org.jspecify.annotations.Nullable;

/** Render state of the Canopy Breaker: the synched current animation, an optional hurt overlay and club visibility. */
public final class CanopyBreakerRenderState extends LivingEntityRenderState {
    public CanopyAnim anim = CanopyAnim.IDLE_BREATHING;
    public final AnimationState animState = new AnimationState();
    public @Nullable CanopyAnim overlay = null;
    public final AnimationState overlayState = new AnimationState();
    public boolean clubDrawn = false;
    public boolean lookAtTarget = true;
    public int stage = 1;
}
