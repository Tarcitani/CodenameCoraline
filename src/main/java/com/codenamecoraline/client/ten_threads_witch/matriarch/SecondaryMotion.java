package com.codenamecoraline.client.ten_threads_witch.matriarch;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchMood;
import net.minecraft.util.Mth;

/**
 * Her cloak, dress and hair (MAT-CLOTH-01..05, MAT-HAIR-01..06). Each panel and lock is a damped spring on two axes, pushed
 * by what the body does: the speed and direction she moves in (walking and running drag the hem back, a side step swings
 * it), her turning, her rising and sinking, and the jolts of the chest during a blow, which reach the hem and the tips of the
 * hair a moment later (a lock's tip follows the lock, the hem follows the back panel). Floating, the cloth lifts and waves
 * slowly; as she unravels, the cloth and the hair start moving on their own, against the motion of the body, until they
 * writhe. Simulated in fixed steps of a sixtieth of a second, so it looks the same at any frame rate.
 */
public final class SecondaryMotion {
    private static final int SKIRT = 0;
    private static final int HEM = 1;
    private static final int CLOAK_L = 2;
    private static final int CLOAK_R = 3;
    private static final int HOOD = 4;
    private static final int LOCK_L = 5;
    private static final int LOCK_L_TIP = 6;
    private static final int LOCK_R = 7;
    private static final int LOCK_R_TIP = 8;
    private static final int HAIR_BACK = 9;
    private static final int N = 10;
    private static final int[] JOINT = {MatRig.SKIRT, MatRig.SKIRT_HEM, MatRig.CLOAK_L, MatRig.CLOAK_R, MatRig.HOOD_TIP,
            MatRig.LOCK_L, MatRig.LOCK_L_TIP, MatRig.LOCK_R, MatRig.LOCK_R_TIP, MatRig.HAIR_BACK};
    private static final float[] STIFF = {55.0F, 32.0F, 50.0F, 50.0F, 60.0F, 75.0F, 45.0F, 75.0F, 45.0F, 70.0F};
    private static final float[] DAMP = {8.5F, 5.5F, 8.0F, 8.0F, 7.0F, 8.0F, 5.0F, 8.0F, 5.0F, 9.0F};

    private final float[] x = new float[N];
    private final float[] z = new float[N];
    private final float[] vx = new float[N];
    private final float[] vz = new float[N];
    private final float[] tx = new float[N];
    private final float[] tz = new float[N];
    private float lastTime = Float.NaN;
    private float lastChestX;
    private float lastChestY;
    private float lastBodyX;
    private float acc = 0.0F;
    private float floatBlend = 0.0F;

    /** For tests: the current angle (degrees, x axis) of the back panel of the cloak. */
    public float skirtAngle() {
        return this.x[SKIRT];
    }

    /**
     * {@code fwd, side, up}: her velocity in blocks per tick along her facing, to her left and upwards; {@code yawRate}
     * degrees per tick; {@code chestX, chestY, bodyX}: the composed rotations (radians) of the chest and hips, whose jolts
     * the cloth follows.
     */
    public void update(PoseMixer mixer, float now, float fwd, float side, float up, float yawRate, boolean floating, float unravel,
                       float chestX, float chestY, float bodyX) {
        float dt = Float.isNaN(this.lastTime) ? 0.0F : Mth.clamp(now - this.lastTime, 0.0F, 6.0F) / 20.0F;
        boolean first = Float.isNaN(this.lastTime);
        this.lastTime = now;
        float dChestX = first ? 0.0F : chestX - this.lastChestX;
        float dChestY = first ? 0.0F : chestY - this.lastChestY;
        float dBodyX = first ? 0.0F : bodyX - this.lastBodyX;
        this.lastChestX = chestX;
        this.lastChestY = chestY;
        this.lastBodyX = bodyX;
        this.floatBlend += ((floating ? 1.0F : 0.0F) - this.floatBlend) * Math.min(1.0F, dt * 2.0F);
        float strange = MatriarchMood.strangeness(unravel);
        float irritated = Mth.clamp((unravel - 0.2F) / 0.4F, 0.0F, 1.0F);
        float t = now / 20.0F;

        // targets (degrees): the wind of her motion, the float, and the cloth's own will
        float drag = Mth.clamp(fwd * 240.0F, -35.0F, 45.0F);
        float sway = Mth.clamp(side * 200.0F + yawRate * 1.6F, -35.0F, 35.0F);
        float lift = Mth.clamp(-up * 160.0F, -25.0F, 25.0F);
        float[] tx = this.tx;
        float[] tz = this.tz;
        tx[SKIRT] = drag + lift * 0.6F;
        tz[SKIRT] = -sway * 0.4F;
        tx[HEM] = drag * 0.8F + lift * 0.8F;
        tz[HEM] = -sway * 0.5F;
        tx[CLOAK_L] = drag * 0.4F;
        tz[CLOAK_L] = -Math.abs(drag) * 0.35F - Math.max(0.0F, -sway) * 0.6F - lift * 0.3F;
        tx[CLOAK_R] = drag * 0.4F;
        tz[CLOAK_R] = Math.abs(drag) * 0.35F + Math.max(0.0F, sway) * 0.6F + lift * 0.3F;
        tx[HOOD] = drag * 0.7F;
        tz[HOOD] = -sway * 0.6F;
        tx[LOCK_L] = tx[LOCK_R] = tx[HAIR_BACK] = drag * 0.8F + lift * 0.5F;
        tz[LOCK_L] = tz[LOCK_R] = tz[HAIR_BACK] = -sway * 0.7F;
        tx[LOCK_L_TIP] = tx[LOCK_R_TIP] = 0.0F;
        tz[LOCK_L_TIP] = tz[LOCK_R_TIP] = 0.0F;
        // MAT-CLOTH-03 / MAT-HAIR-03 and 05: floating, the cloth billows up and waves; the locks drift up and apart
        float fb = this.floatBlend;
        tx[SKIRT] += fb * (14.0F + 7.0F * Mth.sin(t * 1.3F));
        tx[HEM] += fb * (20.0F + 10.0F * Mth.sin(t * 1.3F - 0.8F));
        tz[CLOAK_L] += fb * (-12.0F - 6.0F * Mth.sin(t * 1.1F));
        tz[CLOAK_R] += fb * (12.0F + 6.0F * Mth.sin(t * 1.1F + 0.5F));
        tz[LOCK_L] += fb * irritated * (-18.0F - 8.0F * Mth.sin(t * 0.9F));
        tz[LOCK_R] += fb * irritated * (18.0F + 8.0F * Mth.sin(t * 0.9F + 1.4F));
        tx[HAIR_BACK] += fb * irritated * -10.0F;
        // MAT-CLOTH-05 / MAT-HAIR-06: unravelling, each piece moves by itself, out of step with the body
        if (strange > 0.0F) {
            for (int i = 0; i < N; i++) {
                float a = strange * (i >= LOCK_L ? 22.0F : 16.0F);
                tx[i] += a * (0.6F * Mth.sin(t * (2.3F + i * 0.37F) + i) + 0.4F * Mth.sin(t * (5.1F + i * 0.91F) + i * 2.1F));
                tz[i] += a * 0.7F * Mth.sin(t * (1.7F + i * 0.53F) + i * 3.3F);
            }
        }
        // MAT-CLOTH-04 / MAT-HAIR-04: the jolts of a blow, felt as an impulse
        float kick = 900.0F;
        for (int i = 0; i < N; i++) {
            this.vx[i] -= (dChestX + dBodyX) * Mth.RAD_TO_DEG * (i == HEM || i == LOCK_L_TIP || i == LOCK_R_TIP ? 1.4F : 1.0F) * kick * Math.min(dt, 0.05F);
            this.vz[i] += dChestY * Mth.RAD_TO_DEG * kick * 0.5F * Math.min(dt, 0.05F);
        }
        // fixed steps
        this.acc += dt;
        int steps = 0;
        while (this.acc >= 1.0F / 60.0F && steps < 30) {
            this.acc -= 1.0F / 60.0F;
            steps++;
            float h = 1.0F / 60.0F;
            for (int i = 0; i < N; i++) {
                float gx = tx[i];
                float gz = tz[i];
                if (i == HEM) {
                    gx += this.x[SKIRT] * 0.45F; // the hem hangs from the back panel
                } else if (i == LOCK_L_TIP) {
                    gx += this.x[LOCK_L] * 0.6F;
                    gz += this.z[LOCK_L] * 0.6F;
                } else if (i == LOCK_R_TIP) {
                    gx += this.x[LOCK_R] * 0.6F;
                    gz += this.z[LOCK_R] * 0.6F;
                }
                this.vx[i] += ((gx - this.x[i]) * STIFF[i] - this.vx[i] * DAMP[i]) * h;
                this.vz[i] += ((gz - this.z[i]) * STIFF[i] - this.vz[i] * DAMP[i]) * h;
                this.x[i] = Mth.clamp(this.x[i] + this.vx[i] * h, -70.0F, 75.0F);
                this.z[i] = Mth.clamp(this.z[i] + this.vz[i] * h, -70.0F, 70.0F);
            }
        }
        for (int i = 0; i < N; i++) {
            mixer.addAfter(JOINT[i] * 3 + MatRig.ROT, this.x[i] * Mth.DEG_TO_RAD, 0.0F, this.z[i] * Mth.DEG_TO_RAD, 1.0F);
        }
    }
}
