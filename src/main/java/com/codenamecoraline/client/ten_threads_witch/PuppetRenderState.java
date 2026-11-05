package com.codenamecoraline.client.ten_threads_witch;

import com.codenamecoraline.entity.ten_threads_witch.PuppetAnim;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Render state shared by the Matriarch and her puppets: the synched animation and the facts the thread layer needs. */
public final class PuppetRenderState extends LivingEntityRenderState {
    public PuppetAnim anim = PuppetAnim.IDLE;
    public final AnimationState animState = new AnimationState();
    public boolean lookAtTarget = true;
    /** The thread of a special attack is crimson (cuttable). */
    public boolean crimson = false;
    public boolean tangled = false;
    public boolean cracked = false;
    /** Where the thread is tied (the Matriarch's staff wheel), or null when the puppet has no Matriarch. */
    public @Nullable Vec3 threadAnchor = null;
    /** Matriarch: bits of the spools that have burst. */
    public int brokenSpools = 0;
    /** Matriarch: in the last phase, with the blindfold off. */
    public boolean fury = false;
    public float bodyYawDegrees = 0.0F;
}
