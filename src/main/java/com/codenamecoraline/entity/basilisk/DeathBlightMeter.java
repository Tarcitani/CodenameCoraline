package com.codenamecoraline.entity.basilisk;

/** Exposure time is per victim, so overlapping clouds cannot multiply the buildup rate. */
public final class DeathBlightMeter {
    public static final int MAX = 100;
    private int exposureTicks;
    private int decayTicks;

    public int tick(int amount, boolean exposed, boolean warded) {
        if (exposed) {
            this.decayTicks = 0;
            if (++this.exposureTicks >= (warded ? 20 : 10)) {
                this.exposureTicks = 0;
                return Math.min(MAX, amount + 15);
            }
        } else {
            this.exposureTicks = 0;
            if (++this.decayTicks >= 20) {
                this.decayTicks = 0;
                return Math.max(0, amount - 5);
            }
        }
        return amount;
    }

    public void reset() {
        this.exposureTicks = 0;
        this.decayTicks = 0;
    }
}
