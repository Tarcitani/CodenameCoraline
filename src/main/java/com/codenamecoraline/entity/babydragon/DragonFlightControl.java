package com.codenamecoraline.entity.babydragon;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

/** Steering accelerates toward navigation waypoints with dive boost, climb energy conversion, inertia and banking. */
final class DragonFlightControl extends MoveControl {
    private final BabyDragon dragon;
    DragonFlightControl(BabyDragon dragon) { super(dragon); this.dragon = dragon; }

    @Override public void tick() {
        Vec3 desired = Vec3.ZERO;
        float agility = this.dragon.getAgility();
        float yawDelta = 0.0F;
        float targetPitch = 0.0F;
        if (this.operation == Operation.MOVE_TO) {
            Vec3 offset = new Vec3(this.wantedX, this.wantedY, this.wantedZ).subtract(this.mob.position());
            double dist = offset.length();
            double baseSpeed = this.mob.getAttributeValue(Attributes.FLYING_SPEED) * this.speedModifier;
            if (dist > 1.0E-4) {
                Vec3 dir = offset.scale(1.0 / dist);
                // Dive acceleration and climb energy conversion when cruising beyond immediate landing proximity.
                double verticalFactor = 1.0;
                if (dist > 1.5) {
                    if (dir.y < -0.12) {
                        this.dragon.addFlightMomentum((float)(-dir.y * 0.05 * agility));
                        verticalFactor = 1.0 + (-dir.y) * 0.30 * agility + this.dragon.getFlightMomentum() * 0.35;
                    } else if (dir.y > 0.18) {
                        float momentum = this.dragon.getFlightMomentum();
                        verticalFactor = momentum > 0.05F ? 1.0 + momentum * 0.25 : Math.max(0.82, 1.0 - dir.y * 0.16);
                        this.dragon.decayFlightMomentum(0.03F);
                    } else {
                        verticalFactor = 1.0 + this.dragon.getFlightMomentum() * 0.20;
                        this.dragon.decayFlightMomentum(0.015F);
                    }
                }
                double speed = baseSpeed * verticalFactor;
                desired = dir.scale(Math.min(speed, dist * 0.38));
                targetPitch = (float)Mth.clamp(-dir.y * 32.0, -28.0, 32.0);
            }
            if (offset.lengthSqr() < 0.15) this.operation = Operation.WAIT;
            if (offset.horizontalDistanceSqr() > 0.02) {
                float previousYaw = this.mob.getYRot();
                float wantedYaw = (float)(Mth.atan2(offset.z, offset.x) * Mth.RAD_TO_DEG) - 90;
                float newYaw = this.rotlerp(previousYaw, wantedYaw, 9.5F * agility);
                yawDelta = Mth.wrapDegrees(newYaw - previousYaw);
                this.mob.setYRot(newYaw);
                this.mob.yBodyRot = newYaw;
            }
        } else {
            this.dragon.decayFlightMomentum(0.025F);
        }
        double response = Mth.clamp((0.14 * agility) * (1.0 - 0.18 * this.dragon.getFlightMomentum()), 0.10, 0.24);
        this.mob.setDeltaMovement(this.mob.getDeltaMovement().lerp(desired, response));
        this.dragon.updateFlightAttitude(yawDelta, targetPitch);
    }
}
