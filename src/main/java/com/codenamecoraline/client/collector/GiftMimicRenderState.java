package com.codenamecoraline.client.collector;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Render state for the Gift Mimic: how close its fuse is to bursting (0..1). */
public final class GiftMimicRenderState extends LivingEntityRenderState {
    public float fuse = 0.0F;
    public float attackProgress = 0.0F;
}
