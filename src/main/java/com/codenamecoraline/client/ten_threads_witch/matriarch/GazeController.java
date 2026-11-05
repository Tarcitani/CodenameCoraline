package com.codenamecoraline.client.ten_threads_witch.matriarch;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchMood;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Her gaze (MAT-IDLE-04 the player, MAT-IDLE-05 a puppet, MAT-IDLE-06 her own threads). The direction to what she watches is
 * split over the joints within their limits: the eyes first (only seen once the blindfold is gone), then the head (up to 50
 * degrees of turn and 30 of pitch), and the chest takes what the head cannot (up to 22 degrees). It follows the target with
 * a smooth lag; as she unravels it overshoots and snaps, with a tilt of the head that a person would not make. The arms are
 * never touched, so her hands keep working while she watches.
 */
public final class GazeController {
    private float yaw;
    private float pitch;
    private float yawVel;
    private float pitchVel;
    private float tilt;
    private float lastTime = Float.NaN;

    /** Yaw and pitch (degrees, relative to her body) the head and chest have now; for tests. */
    public float yaw() {
        return this.yaw;
    }

    public float pitch() {
        return this.pitch;
    }

    /**
     * {@code toTarget}: the vector from her eyes to the point watched, in world axes (null when she watches nothing);
     * {@code weight}: how free the gaze is under the body clip playing (its look weight).
     */
    public void update(PoseMixer mixer, @Nullable Vec3 toTarget, float bodyYawDeg, float weight, float unravel, float now) {
        float dt = Float.isNaN(this.lastTime) ? 1.0F : Mth.clamp(now - this.lastTime, 0.0F, 4.0F);
        this.lastTime = now;
        float wantYaw = 0.0F;
        float wantPitch = 0.0F;
        if (toTarget != null && toTarget.lengthSqr() > 1.0E-4D) {
            float world = (float) (Mth.atan2(toTarget.z, toTarget.x) * Mth.RAD_TO_DEG) - 90.0F;
            wantYaw = Mth.clamp(Mth.wrapDegrees(world - bodyYawDeg), -110.0F, 110.0F);
            double h = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
            wantPitch = Mth.clamp((float) (-Mth.atan2(toTarget.y, h) * Mth.RAD_TO_DEG), -40.0F, 50.0F);
        } else {
            weight = 0.0F;
        }
        float strange = MatriarchMood.strangeness(unravel);
        // a damped spring: composed she settles softly, unravelled she overshoots and jerks
        float stiffness = 0.06F + 0.10F * strange;
        float damping = 0.42F - 0.22F * strange;
        for (float t = 0.0F; t < dt; t += 0.5F) {
            float step = Math.min(0.5F, dt - t);
            this.yawVel += ((wantYaw - this.yaw) * stiffness - this.yawVel * damping) * step * 2.0F;
            this.pitchVel += ((wantPitch - this.pitch) * stiffness - this.pitchVel * damping) * step * 2.0F;
            this.yaw += this.yawVel * step * 2.0F;
            this.pitch += this.pitchVel * step * 2.0F;
        }
        // the inhuman tilt of the head, sudden, held, then dropped
        float tiltWant = strange > 0.2F ? strange * 24.0F * Mth.sin(now * 0.037F) * (Mth.sin(now * 0.011F + 2.0F) > 0.3F ? 1.0F : 0.0F) : 0.0F;
        this.tilt += (tiltWant - this.tilt) * Math.min(1.0F, dt * (0.08F + 0.4F * strange));
        if (weight <= 1.0E-3F) {
            return;
        }
        float headYaw = Mth.clamp(this.yaw, -50.0F, 50.0F);
        float chestYaw = Mth.clamp(this.yaw - headYaw, -22.0F, 22.0F);
        float headPitch = Mth.clamp(this.pitch, -30.0F, 30.0F);
        float chestPitch = Mth.clamp((this.pitch - headPitch) * 0.5F, -10.0F, 14.0F);
        float d = Mth.DEG_TO_RAD;
        mixer.submit(MatRig.HEAD * 3 + MatRig.ROT, headPitch * d, headYaw * d, this.tilt * d, weight, 6.5F, true, 50);
        mixer.submit(MatRig.CHEST * 3 + MatRig.ROT, chestPitch * d, chestYaw * d, 0.0F, weight, 6.5F, true, 50);
        // the eyes lead the head by a fraction of a pixel (only visible without the blindfold)
        float ex = Mth.clamp((this.yaw - headYaw) / 40.0F + this.yaw / 160.0F, -0.35F, 0.35F);
        float ey = Mth.clamp(-headPitch / 120.0F, -0.2F, 0.2F);
        mixer.submit(MatRig.EYES * 3 + MatRig.POS, -ex, -ey, 0.0F, weight, 6.5F, true, 50);
    }
}
