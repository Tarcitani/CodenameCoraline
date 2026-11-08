package com.codenamecoraline.client.architect;

import com.codenamecoraline.entity.architect.Architect;
import com.codenamecoraline.entity.architect.ArchitectAnim;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Render state of the Architect: the synchronized clip and the state that picks the glow texture. */
public final class ArchitectRenderState extends LivingEntityRenderState {
    public ArchitectAnim anim = ArchitectAnim.DORMANT_STATUE;
    public final AnimationState animState = new AnimationState();
    public Architect.State state = Architect.State.DORMANT;
    /** Whether the head may follow the target on top of the clip (idle, walking, guarding). */
    public boolean lookAtTarget = false;
}
