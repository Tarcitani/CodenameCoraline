package com.codenamecoraline.entity.architect;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * Damage volumes of the Architect's blows (spec docs/design/architect.md §13.0 and §17). Every volume is built in the
 * frame locked when the blow started: origin at his feet, "ahead" along the locked yaw, angles in degrees with positive
 * values on HIS LEFT (the spec's +X), heights in blocks above his feet. Pure geometry, no world access, so the GameTests
 * and the brain share the exact same test.
 */
public final class ArchitectStrikes {
    private ArchitectStrikes() {}

    /** ARC covers sectors and full rings (sector -180..180); CORRIDOR is a box ahead of him. */
    public enum Shape { ARC, CORRIDOR }

    /** Which version of an attack a volume belongs to (the "close", "in the air" or phase-3 variations of §13). */
    public enum Variant { ALWAYS, NORMAL_ONLY, VARIANT_ONLY }

    /**
     * One volume, live from {@code start} to {@code end} (attack ticks, inclusive).
     * ARC: {@code a} inner radius, {@code b} outer radius, {@code c}..{@code d} sector in degrees.
     * CORRIDOR: {@code a} width, {@code b}..{@code c} depth ahead, {@code d} unused.
     * {@code damage} is a multiple of his base damage (DB).
     */
    public record Strike(int start, int end, Shape shape, double a, double b, double c, double d,
                         double minY, double maxY, float damage, Variant variant) {
        public boolean liveAt(int tick, boolean variantActive) {
            if (tick < this.start || tick > this.end) {
                return false;
            }
            return switch (this.variant) {
                case ALWAYS -> true;
                case NORMAL_ONLY -> !variantActive;
                case VARIANT_ONLY -> variantActive;
            };
        }

        /** How far the volume reaches from his feet, horizontally. */
        public double reach() {
            return this.shape == Shape.ARC ? this.b : Math.max(this.c, this.a / 2.0D);
        }
    }

    public static Strike arc(int start, int end, double inner, double outer, double fromDeg, double toDeg,
                             double minY, double maxY, float damage, Variant variant) {
        return new Strike(start, end, Shape.ARC, inner, outer, fromDeg, toDeg, minY, maxY, damage, variant);
    }

    public static Strike arc(int start, int end, double outer, double fromDeg, double toDeg, double minY, double maxY,
                             float damage) {
        return arc(start, end, 0.0D, outer, fromDeg, toDeg, minY, maxY, damage, Variant.ALWAYS);
    }

    public static Strike ring(int start, int end, double inner, double outer, double minY, double maxY, float damage) {
        return arc(start, end, inner, outer, -180.0D, 180.0D, minY, maxY, damage, Variant.ALWAYS);
    }

    public static Strike corridor(int start, int end, double width, double fromDepth, double toDepth, double minY,
                                  double maxY, float damage, Variant variant) {
        return new Strike(start, end, Shape.CORRIDOR, width, fromDepth, toDepth, 0.0D, minY, maxY, damage, variant);
    }

    /**
     * Signed angle (degrees) of an offset seen from a body facing {@code yawDeg}: 0 straight ahead, positive to his
     * left, +-180 behind. Minecraft's forward is (-sin yaw, cos yaw) and his left is (cos yaw, sin yaw).
     */
    public static double angleOf(double dx, double dz, float yawDeg) {
        double yaw = yawDeg * Mth.DEG_TO_RAD;
        double ahead = -Math.sin(yaw) * dx + Math.cos(yaw) * dz;
        double left = Math.cos(yaw) * dx + Math.sin(yaw) * dz;
        return Math.toDegrees(Math.atan2(left, ahead));
    }

    /** Distance ahead and to the left of an offset, for a body facing {@code yawDeg}. */
    public static double ahead(double dx, double dz, float yawDeg) {
        double yaw = yawDeg * Mth.DEG_TO_RAD;
        return -Math.sin(yaw) * dx + Math.cos(yaw) * dz;
    }

    public static double left(double dx, double dz, float yawDeg) {
        double yaw = yawDeg * Mth.DEG_TO_RAD;
        return Math.cos(yaw) * dx + Math.sin(yaw) * dz;
    }

    /**
     * Whether a victim's box overlaps the volume. {@code ox, oy, oz} are his feet, {@code yawDeg} the locked direction.
     * The victim counts with its half width, so standing at the edge still gets hit.
     */
    public static boolean hits(Strike s, double ox, double oy, double oz, float yawDeg, AABB victim) {
        double low = victim.minY - oy;
        double high = victim.maxY - oy;
        if (high < s.minY || low > s.maxY) {
            return false;
        }
        double half = (victim.maxX - victim.minX) / 2.0D;
        double dx = (victim.minX + victim.maxX) / 2.0D - ox;
        double dz = (victim.minZ + victim.maxZ) / 2.0D - oz;
        if (s.shape == Shape.CORRIDOR) {
            double along = ahead(dx, dz, yawDeg);
            double side = Math.abs(left(dx, dz, yawDeg));
            return along + half >= s.b && along - half <= s.c && side - half <= s.a / 2.0D;
        }
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist - half > s.b || dist + half < s.a) {
            return false;
        }
        if (s.c <= -180.0D && s.d >= 180.0D) {
            return true;
        }
        if (dist < 0.5D) {
            return true; // standing inside him: every sector reaches
        }
        double slack = Math.toDegrees(Math.asin(Math.min(1.0D, half / dist)));
        double angle = angleOf(dx, dz, yawDeg);
        return angle >= s.c - slack && angle <= s.d + slack;
    }
}
