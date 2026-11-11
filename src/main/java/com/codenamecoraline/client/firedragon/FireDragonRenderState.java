package com.codenamecoraline.client.firedragon;

import com.codenamecoraline.entity.firedragon.FireDragonAnimation;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Visual snapshot only; never drives combat or movement. */
public final class FireDragonRenderState extends LivingEntityRenderState {
    public int colorCode = 70;
    public boolean airborne;
    public boolean sitting;
    public boolean perched;
    public net.minecraft.world.phys.Vec3 shoulderOffset = net.minecraft.world.phys.Vec3.ZERO;
    public int shoulderSide;
    public boolean glowing = true;
    public boolean preview;
    public FireDragonAnimation clip = FireDragonAnimation.NONE;
    public float clipTicks;
    public float hurtTicks;
}
