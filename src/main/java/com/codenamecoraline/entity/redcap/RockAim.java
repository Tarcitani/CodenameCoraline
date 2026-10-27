package com.codenamecoraline.entity.redcap;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Aims a thrown rock so that its flight meets a moving target. Simulates the projectile motion of
 * ThrowableProjectile (each tick: gravity, then 0.99 inertia, then move) instead of using a fixed
 * "aim a bit high" fudge, and leads the target by the time the rock needs to arrive.
 */
final class RockAim {
    private static final double INERTIA = 0.99D;
    private static final int MAX_FLIGHT_TICKS = 120;

    private RockAim() {}

    /** Result of a simulated flight to a given horizontal distance: height there and ticks taken. */
    private record Flight(double height, double ticks) {}

    /** Direction (not normalised) to shoot from `from` at the given speed so the rock reaches the target. */
    static Vec3 aim(Vec3 from, LivingEntity target, double speed, double gravity) {
        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        // Blocks per tick over the last tick; entity.getDeltaMovement() is not reliable for players on the server.
        Vec3 velocity = new Vec3(target.getX() - target.xo, 0.0D, target.getZ() - target.zo);

        Vec3 aimPoint = center;
        Vec3 direction = center.subtract(from);
        for (int pass = 0; pass < 3; pass++) {
            double dx = aimPoint.x - from.x;
            double dz = aimPoint.z - from.z;
            double dy = aimPoint.y - from.y;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance < 0.01D) {
                return new Vec3(0.0D, 1.0D, 0.0D);
            }
            double pitch = solvePitch(dy, distance, speed, gravity);
            direction = new Vec3(dx / distance * Math.cos(pitch), Math.sin(pitch), dz / distance * Math.cos(pitch));
            aimPoint = center.add(velocity.scale(fly(pitch, distance, speed, gravity).ticks * 0.9D));
        }
        return direction;
    }

    /** Launch pitch (radians) whose flight is at height dy when it has travelled `distance` horizontally. */
    static double solvePitch(double dy, double distance, double speed, double gravity) {
        // The height reached at that distance grows with the pitch (low-arc solution): bisect.
        double low = -0.6D;
        double high = 1.2D;
        for (int i = 0; i < 30; i++) {
            double mid = (low + high) * 0.5D;
            if (fly(mid, distance, speed, gravity).height < dy) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return (low + high) * 0.5D;
    }

    private static Flight fly(double pitch, double distance, double speed, double gravity) {
        double vx = speed * Math.cos(pitch);
        double vy = speed * Math.sin(pitch);
        double x = 0.0D;
        double y = 0.0D;
        for (int tick = 1; tick <= MAX_FLIGHT_TICKS; tick++) {
            vy -= gravity;
            vx *= INERTIA;
            vy *= INERTIA;
            double previousX = x;
            double previousY = y;
            x += vx;
            y += vy;
            if (x >= distance) {
                double fraction = (distance - previousX) / (x - previousX);
                return new Flight(previousY + (y - previousY) * fraction, tick - 1 + fraction);
            }
        }
        return new Flight(-1.0E9D, MAX_FLIGHT_TICKS);
    }
}
