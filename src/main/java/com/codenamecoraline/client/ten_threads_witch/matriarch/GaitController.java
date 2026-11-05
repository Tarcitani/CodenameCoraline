package com.codenamecoraline.client.ten_threads_witch.matriarch;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatAnim;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchMood;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchPresence;
import net.minecraft.util.Mth;

/**
 * The locomotion layer (MAT-MOVE and MAT-FLOAT, except the dash, which is an action). It is chosen on the client from how
 * the Matriarch actually moves: her speed, the direction of travel against the way she faces, her vertical speed, the gait
 * style and the floating flag the server synchs. Walking cycles advance with the distance covered, not with time, so a
 * footfall always matches the ground covered and the cadence rises and falls with her speed (MAT-MOVE-04/05) at any frame
 * rate. One-shot transitions (start, stop, change of direction, turning on the spot, lifting off, landing) play over the
 * cycles and fade out by themselves.
 */
public final class GaitController {
    private static final float MOVING = 0.012F;

    private MatAnim cycle = null;
    private MatAnim prevCycle = null;
    private float cycleSwitch = -100.0F;
    private float phase = 0.0F;
    private float prevPhase = 0.0F;
    private float weight = 0.0F;
    private MatAnim oneShot = null;
    private boolean oneShotMirror = false;
    private float oneShotStart = -100.0F;
    private float movingSince = -100.0F;
    private float stillSince = 0.0F;
    private boolean wasMoving = false;
    private float lastHeading = 0.0F;
    private float headingAt = 0.0F;
    private float lastYaw = Float.NaN;
    private float yawAt = 0.0F;
    private boolean wasFloating = false;
    private float lastTime = Float.NaN;

    /** The clip whose cycle plays now (for tests and the thread layer), or null when standing. */
    public MatAnim cycle() {
        return this.cycle;
    }

    public MatAnim oneShot() {
        return this.oneShot;
    }

    public float weight() {
        return this.weight;
    }

    /**
     * Updates the layer and submits it. {@code vx, vz, vy} are blocks per tick, {@code yawDeg} the body yaw, {@code style}
     * the synched gait style, {@code now} the client time in ticks.
     */
    public void update(MatClips clips, PoseMixer mixer, PoseSample a, PoseSample b, double vx, double vy, double vz, float yawDeg,
                       boolean floating, int style, float unravel, float now) {
        float dt = Float.isNaN(this.lastTime) ? 0.0F : Mth.clamp(now - this.lastTime, 0.0F, 4.0F);
        this.lastTime = now;
        float speed = (float) Math.sqrt(vx * vx + vz * vz);
        float yaw = yawDeg * Mth.DEG_TO_RAD;
        float fx = -Mth.sin(yaw);
        float fz = Mth.cos(yaw);
        float forward = speed > 1.0E-4F ? (float) (vx * fx + vz * fz) / speed : 1.0F;
        boolean moving = speed > MOVING;

        // ---- transitions that play once
        if (floating != this.wasFloating) {
            this.fire(floating ? MatAnim.FLOAT_01 : MatAnim.FLOAT_09, false, now);
            this.wasFloating = floating;
        }
        if (!floating) {
            if (moving && !this.wasMoving) {
                this.movingSince = now;
                this.fire(MatAnim.MOVE_04, false, now);
            } else if (!moving && this.wasMoving && now - this.movingSince > 10.0F) {
                this.fire(MatAnim.MOVE_05, false, now);
            }
            float heading = (float) Mth.atan2(vz, vx);
            if (moving && this.wasMoving) {
                float turn = Math.abs(Mth.wrapDegrees((heading - this.lastHeading) * Mth.RAD_TO_DEG));
                if (turn > 60.0F && now - this.headingAt < 6.0F && this.oneShot == null) {
                    this.fire(MatAnim.MOVE_09, false, now);
                }
            }
            if (moving) {
                this.lastHeading = heading;
                this.headingAt = now;
            }
            if (!Float.isNaN(this.lastYaw) && !moving) {
                float dy = Mth.wrapDegrees(yawDeg - this.lastYaw);
                if (Math.abs(dy) > 35.0F && this.oneShot == null) {
                    this.fire(MatAnim.MOVE_10, dy < 0.0F, now);
                }
            }
            if (Float.isNaN(this.lastYaw) || now - this.yawAt >= 6.0F) {
                this.lastYaw = yawDeg;
                this.yawAt = now;
            }
        }
        if (!moving) {
            if (this.wasMoving) {
                this.stillSince = now;
            }
        }
        this.wasMoving = moving;

        // ---- the cycle
        MatAnim want;
        if (floating) {
            if (vy > 0.03D) {
                want = MatAnim.FLOAT_06;
            } else if (vy < -0.03D) {
                want = MatAnim.FLOAT_07;
            } else if (moving) {
                want = unravel >= MatriarchMood.IRRITATED_FROM ? MatAnim.FLOAT_04 : MatAnim.FLOAT_03;
            } else {
                want = now - this.stillSince > 60.0F ? MatAnim.FLOAT_08 : MatAnim.FLOAT_02;
            }
        } else if (!moving) {
            want = null;
        } else if (forward < -0.3F) {
            want = MatAnim.MOVE_07;
        } else if (speed > 0.11F) {
            want = MatAnim.MOVE_06;
        } else {
            want = switch (style) {
                case MatriarchPresence.STYLE_MENACE -> MatAnim.MOVE_02;
                case MatriarchPresence.STYLE_AGGRESSIVE -> MatAnim.MOVE_03;
                case MatriarchPresence.STYLE_APPROACH -> MatAnim.MOVE_08;
                default -> MatAnim.MOVE_01;
            };
        }
        if (want != this.cycle) {
            this.prevCycle = this.cycle;
            this.prevPhase = this.phase;
            this.cycle = want;
            this.cycleSwitch = now;
        }
        if (this.cycle != null) {
            if (floating) {
                this.phase += dt / this.cycle.ticks;
            } else {
                this.phase += speed * dt / stride(this.cycle);
            }
            this.phase -= (float) Math.floor(this.phase);
        }
        float targetWeight = floating ? 1.0F : Mth.clamp(speed / 0.03F, 0.0F, 1.0F);
        this.weight += (targetWeight - this.weight) * Math.min(1.0F, dt * 0.25F);
        float fade = Mth.clamp((now - this.cycleSwitch) / 8.0F, 0.0F, 1.0F);
        fade = fade * fade * (3.0F - 2.0F * fade);
        if (this.prevCycle != null && fade < 1.0F) {
            b.clear();
            clips.get(this.prevCycle, false, false).sample(this.prevPhase * this.prevCycle.ticks / 20.0F, 1.0F, b);
            mixer.submit(b, (1.0F - fade) * (floating ? 1.0F : this.weight), 10);
        }
        if (this.cycle != null) {
            a.clear();
            clips.get(this.cycle, false, false).sample(this.phase * this.cycle.ticks / 20.0F, 1.0F, a);
            mixer.submit(a, fade * (floating ? 1.0F : this.weight), 11);
        }

        // ---- the one-shot transition on top of the cycle, fading out at its end
        if (this.oneShot != null) {
            float age = now - this.oneShotStart;
            float len = this.oneShot.ticks;
            if (age >= len) {
                this.oneShot = null;
            } else {
                float in = Mth.clamp(age / 3.0F, 0.0F, 1.0F);
                float out = Mth.clamp((len - age) / 5.0F, 0.0F, 1.0F);
                a.clear();
                clips.get(this.oneShot, this.oneShotMirror, false).sample(age / 20.0F, 1.0F, a);
                mixer.submit(a, in * out, 12);
            }
        }
    }

    private void fire(MatAnim clip, boolean mirror, float now) {
        this.oneShot = clip;
        this.oneShotMirror = mirror;
        this.oneShotStart = now;
    }

    /** Blocks covered by one full cycle of each gait. */
    static float stride(MatAnim cycle) {
        return switch (cycle) {
            case MOVE_02 -> 1.0F;
            case MOVE_03 -> 1.05F;
            case MOVE_06 -> 1.7F;
            case MOVE_07 -> 0.7F;
            case MOVE_08 -> 0.95F;
            default -> 0.9F;
        };
    }
}
