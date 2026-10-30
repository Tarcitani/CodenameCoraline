package com.codenamecoraline.client.macaque;

import com.codenamecoraline.entity.macaque.MacaqueAnim;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Render state of the Bark Macaque: the synched current animation. */
public final class BarkMacaqueRenderState extends LivingEntityRenderState {
    public MacaqueAnim anim = MacaqueAnim.IDLE;
    public final AnimationState animState = new AnimationState();
    public boolean lookAtTarget = true;
}
