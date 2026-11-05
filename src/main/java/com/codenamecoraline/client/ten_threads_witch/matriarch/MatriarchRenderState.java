package com.codenamecoraline.client.ten_threads_witch.matriarch;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Render state of the Matriarch: the composed pose of this frame (one value triple per channel of {@link MatRig}), the
 * spools already burst, whether the blindfold is gone, and where her threads lead (relative to her, in world axes): the
 * focus (the active puppet or her target) and the puppets still hanging over the arena.
 */
public final class MatriarchRenderState extends LivingEntityRenderState {
    public final float[] pose = new float[MatRig.CHANNELS * 3];
    public int brokenSpools = 0;
    public boolean blindfoldOff = false;
    public float unravel = 0.0F;
    public float bodyYawDegrees = 0.0F;
    public boolean hasFocus = false;
    public float focusX;
    public float focusY;
    public float focusZ;
    public final float[] hanging = new float[30];
    public int hangingCount = 0;

    /** One value of the pose: {@code target} is {@link MatRig#ROT}, {@link MatRig#POS} or {@link MatRig#SCALE}, {@code axis} 0..2. */
    public float get(int joint, int target, int axis) {
        return this.pose[(joint * 3 + target) * 3 + axis];
    }
}
