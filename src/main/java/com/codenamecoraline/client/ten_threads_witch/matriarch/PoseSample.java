package com.codenamecoraline.client.ten_threads_witch.matriarch;

import java.util.Arrays;

/**
 * Scratch buffer of one sampled clip (or one procedural layer): a value, a priority and a mode per channel, and which
 * channels are set. Reused every frame, never reallocated.
 */
public final class PoseSample {
    public final float[] value = new float[MatRig.CHANNELS * 3];
    public final float[] priority = new float[MatRig.CHANNELS];
    public final boolean[] additive = new boolean[MatRig.CHANNELS];
    public final boolean[] set = new boolean[MatRig.CHANNELS];
    /** Channels touched in this frame, in order (so clearing and walking cost only what was used). */
    public final int[] used = new int[MatRig.CHANNELS];
    public int count = 0;

    public void clear() {
        for (int i = 0; i < this.count; i++) {
            this.set[this.used[i]] = false;
        }
        this.count = 0;
    }

    public void put(int channel, float x, float y, float z, float prio, boolean add) {
        if (!this.set[channel]) {
            this.set[channel] = true;
            this.used[this.count++] = channel;
        }
        this.value[channel * 3] = x;
        this.value[channel * 3 + 1] = y;
        this.value[channel * 3 + 2] = z;
        this.priority[channel] = prio;
        this.additive[channel] = add;
    }

    /** For tests: whether a channel is set at all. */
    public boolean isSet(int channel) {
        return this.set[channel];
    }

    public void fillZero() {
        Arrays.fill(this.value, 0.0F);
    }
}
