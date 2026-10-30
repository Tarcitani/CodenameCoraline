package com.codenamecoraline.entity.babydragon;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

/** Steering accelerates toward navigation waypoints; Entity.move still resolves solid collisions. */
final class DragonFlightControl extends MoveControl {
    DragonFlightControl(BabyDragon dragon) { super(dragon); }

    @Override public void tick() {
        Vec3 desired = Vec3.ZERO;
        if (this.operation == Operation.MOVE_TO) {
            Vec3 offset = new Vec3(this.wantedX, this.wantedY, this.wantedZ).subtract(this.mob.position());
            double speed = this.mob.getAttributeValue(Attributes.FLYING_SPEED) * this.speedModifier;
            desired = offset.normalize().scale(Math.min(speed, offset.length() * 0.35));
            if (offset.lengthSqr() < 0.15) this.operation = Operation.WAIT;
            if (offset.horizontalDistanceSqr() > 0.02) {
                float yaw = (float)(Mth.atan2(offset.z, offset.x) * Mth.RAD_TO_DEG) - 90;
                this.mob.setYRot(this.rotlerp(this.mob.getYRot(), yaw, 9));
                this.mob.yBodyRot = this.mob.getYRot();
            }
        }
        this.mob.setDeltaMovement(this.mob.getDeltaMovement().lerp(desired, 0.14));
    }
}
