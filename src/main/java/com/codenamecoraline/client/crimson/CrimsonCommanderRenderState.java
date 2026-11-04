package com.codenamecoraline.client.crimson;

import com.codenamecoraline.entity.crimson.CrimsonAnim;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;
import org.jspecify.annotations.Nullable;

/** Render state shared by the Crimson Commander, his spirit and his vanguards: the synched animation and a hurt overlay. */
public final class CrimsonCommanderRenderState extends LivingEntityRenderState {
    public CrimsonAnim anim = CrimsonAnim.COMBAT_IDLE;
    public final AnimationState animState = new AnimationState();
    public @Nullable CrimsonAnim overlay = null;
    public final AnimationState overlayState = new AnimationState();
    public boolean lookAtTarget = true;
    /** A Crimson Ash Vanguard: the same armour without plume, cape and pauldron spikes. */
    public boolean vanguard = false;
    public int stage = 1;
}
