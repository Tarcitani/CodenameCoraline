package com.codenamecoraline.client.ten_threads_witch.matriarch;

/**
 * Composes the layers of the Matriarch joint by joint. Every layer submits, for each channel it moves, a value, a weight, a
 * priority and a mode; {@link #resolve} then walks each channel's contributions from the weakest priority to the strongest
 * (ties broken by a fixed layer rank, never by the order the code ran in): an override blends the result towards its value
 * by its weight, an additive layer adds its value times its weight. So a stronger layer owns the joints it touches and only
 * those; the others keep moving underneath. Channels locked by a strong override (priority 5 or stronger: stun, fall,
 * transition, cinematic, death) also damp the procedural motion added afterwards ({@link #lock}).
 */
public final class PoseMixer {
    public static final int MAX = 12;
    public static final float LOCK_PRIORITY = 5.0F;

    private final float[] cx = new float[MatRig.CHANNELS * MAX];
    private final float[] cy = new float[MatRig.CHANNELS * MAX];
    private final float[] cz = new float[MatRig.CHANNELS * MAX];
    private final float[] cw = new float[MatRig.CHANNELS * MAX];
    private final float[] ckey = new float[MatRig.CHANNELS * MAX];
    private final float[] cprio = new float[MatRig.CHANNELS * MAX];
    private final boolean[] cadd = new boolean[MatRig.CHANNELS * MAX];
    private final int[] count = new int[MatRig.CHANNELS];
    private final int[] order = new int[MAX];
    /** The composed pose: 3 values per channel (radians, model units, scale offsets, or raw effect values). */
    public final float[] out = new float[MatRig.CHANNELS * 3];
    /** Per channel: the strongest weight of an override of priority {@link #LOCK_PRIORITY} or stronger. */
    public final float[] lock = new float[MatRig.CHANNELS];

    public void begin() {
        java.util.Arrays.fill(this.count, 0);
    }

    /** Adds one contribution. {@code rank} orders layers of equal priority (higher rank is applied later, so it wins). */
    public void submit(int channel, float x, float y, float z, float weight, float priority, boolean additive, int rank) {
        if (weight <= 1.0E-4F) {
            return;
        }
        int n = this.count[channel];
        if (n >= MAX) {
            return;
        }
        int i = channel * MAX + n;
        this.cx[i] = x;
        this.cy[i] = y;
        this.cz[i] = z;
        this.cw[i] = Math.min(1.0F, weight);
        // weakest first: a larger priority number is weaker; within one priority, the lower rank goes first
        this.ckey[i] = -priority * 100.0F + rank;
        this.cprio[i] = priority;
        this.cadd[i] = additive;
        this.count[channel] = n + 1;
    }

    /** Submits every channel of a sample with one weight. */
    public void submit(PoseSample s, float weight, int rank) {
        for (int k = 0; k < s.count; k++) {
            int ch = s.used[k];
            this.submit(ch, s.value[ch * 3], s.value[ch * 3 + 1], s.value[ch * 3 + 2], weight, s.priority[ch], s.additive[ch], rank);
        }
    }

    public void resolve() {
        for (int ch = 0; ch < MatRig.CHANNELS; ch++) {
            int n = this.count[ch];
            float x = 0.0F;
            float y = 0.0F;
            float z = 0.0F;
            float lockW = 0.0F;
            if (n > 0) {
                for (int k = 0; k < n; k++) {
                    this.order[k] = ch * MAX + k;
                }
                for (int a = 1; a < n; a++) { // insertion sort: a handful of entries
                    int v = this.order[a];
                    int b = a - 1;
                    while (b >= 0 && this.ckey[this.order[b]] > this.ckey[v]) {
                        this.order[b + 1] = this.order[b];
                        b--;
                    }
                    this.order[b + 1] = v;
                }
                for (int k = 0; k < n; k++) {
                    int i = this.order[k];
                    float w = this.cw[i];
                    if (this.cadd[i]) {
                        x += this.cx[i] * w;
                        y += this.cy[i] * w;
                        z += this.cz[i] * w;
                    } else {
                        x += (this.cx[i] - x) * w;
                        y += (this.cy[i] - y) * w;
                        z += (this.cz[i] - z) * w;
                        if (this.cprio[i] <= LOCK_PRIORITY) {
                            lockW = Math.max(lockW, w);
                        }
                    }
                }
            }
            this.out[ch * 3] = x;
            this.out[ch * 3 + 1] = y;
            this.out[ch * 3 + 2] = z;
            this.lock[ch] = lockW;
        }
    }

    /** Adds procedural motion to a channel after {@link #resolve}, damped where a strong override owns the joint. */
    public void addAfter(int channel, float x, float y, float z, float weight) {
        float w = weight * (1.0F - this.lock[channel]);
        this.out[channel * 3] += x * w;
        this.out[channel * 3 + 1] += y * w;
        this.out[channel * 3 + 2] += z * w;
    }

    /** For tests: how many layers contributed to a channel in the last composition. */
    public int contributions(int channel) {
        return this.count[channel];
    }
}
