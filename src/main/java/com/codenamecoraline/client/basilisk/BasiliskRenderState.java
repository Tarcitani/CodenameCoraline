package com.codenamecoraline.client.basilisk;

import com.codenamecoraline.entity.basilisk.Basilisk;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public final class BasiliskRenderState extends LivingEntityRenderState {
    public Basilisk.State state = Basilisk.State.LURKING;
    public float inflation;
    public boolean airborne;
    public boolean concealed;
    public float verticalSpeed;
    public float idleSeed;
}
