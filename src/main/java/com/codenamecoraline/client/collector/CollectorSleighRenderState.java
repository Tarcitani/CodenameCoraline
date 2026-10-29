package com.codenamecoraline.client.collector;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Render state for the temporary sky sleigh (`codenamecoraline:collector_sleigh`). */
public final class CollectorSleighRenderState extends EntityRenderState {
    public float yRot = 0.0F;
    public float xRot = 0.0F;
    public float flightProgress = 0.0F;
    public boolean riderAboard = true;
}

