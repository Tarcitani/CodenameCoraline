package com.codenamecoraline.entity.canopy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * World queries for the Canopy Breaker's tree movement: finding climbable trunks, the spot where he starts climbing,
 * a safe perch on top of the canopy, and ballistic jump plans that account for Minecraft's air drag (0.91 horizontally,
 * 0.98 vertically) and gravity. Everything works on real blocks; nothing here moves an entity.
 */
public final class TreeNav {
    public static final double AIR_DRAG_H = 0.91D;
    public static final double AIR_DRAG_V = 0.98D;
    public static final double MAX_HORIZONTAL_SPEED = 1.15D;
    public static final double MAX_LAUNCH_SPEED = 1.1D;
    public static final double MIN_LAUNCH_SPEED = 0.3D;

    /** A vertical run of log blocks standing on the ground. */
    public record Trunk(BlockPos base, int topY) {
        public Vec3 center() {
            return new Vec3(this.base.getX() + 0.5D, this.base.getY(), this.base.getZ() + 0.5D);
        }

        public int height() {
            return this.topY - this.base.getY() + 1;
        }
    }

    /** A ballistic jump: launch velocity, flight time in ticks, and the apex it reaches. */
    public record JumpPlan(Vec3 velocity, int ticks, Vec3 apex, Vec3 landing) {}

    private TreeNav() {}

    public static boolean isLog(BlockState state) {
        return state.is(BlockTags.LOGS);
    }

    public static boolean isFoliage(BlockState state) {
        return state.is(BlockTags.LEAVES);
    }

    public static AABB boxAt(Vec3 feet, double width, double height) {
        double h = width / 2.0D;
        return new AABB(feet.x - h, feet.y, feet.z - h, feet.x + h, feet.y + height, feet.z + h);
    }

    /** Free of solid blocks; with {@code passFoliage} leaves do not count as solid (he pushes through foliage). */
    public static boolean canOccupy(ServerLevel level, AABB box, boolean passFoliage) {
        BlockPos min = BlockPos.containing(box.minX + 1.0E-3D, box.minY + 1.0E-3D, box.minZ + 1.0E-3D);
        BlockPos max = BlockPos.containing(box.maxX - 1.0E-3D, box.maxY - 1.0E-3D, box.maxZ - 1.0E-3D);
        for (BlockPos p : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(p);
            if (state.getCollisionShape(level, p).isEmpty()) {
                continue;
            }
            if (passFoliage && isFoliage(state)) {
                continue;
            }
            return false;
        }
        return level.getWorldBorder().isWithinBounds(box);
    }

    /** Solid ground (or leaves/logs) directly under a point: something to stand on. */
    public static boolean hasSupportBelow(ServerLevel level, Vec3 feet) {
        BlockPos below = BlockPos.containing(feet.x, feet.y - 0.15D, feet.z);
        BlockState state = level.getBlockState(below);
        return !state.getCollisionShape(level, below).isEmpty();
    }

    /** All trunks (at least {@code minHeight} logs tall, resting on solid ground) within {@code radius} of {@code center}. */
    public static List<Trunk> findTrunks(ServerLevel level, BlockPos center, int radius, int minHeight) {
        List<Trunk> out = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                int yMin = Math.max(level.getMinY(), center.getY() - 12);
                int y = Math.min(top, center.getY() + 40);
                while (y >= yMin) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (isLog(level.getBlockState(p))) {
                        int topLog = y;
                        while (isLog(level.getBlockState(new BlockPos(x, topLog + 1, z)))) {
                            topLog++;
                        }
                        int bottom = y;
                        while (isLog(level.getBlockState(new BlockPos(x, bottom - 1, z)))) {
                            bottom--;
                        }
                        BlockPos base = new BlockPos(x, bottom, z);
                        BlockPos under = base.below();
                        boolean grounded = !level.getBlockState(under).getCollisionShape(level, under).isEmpty()
                                && !isLog(level.getBlockState(under));
                        if (grounded && topLog - bottom + 1 >= minHeight) {
                            out.add(new Trunk(base, topLog));
                        }
                        y = bottom - 1;
                    } else {
                        y--;
                    }
                }
            }
        }
        return out;
    }

    /** Best trunk to climb: close to {@code from}, and (if given) close to {@code toward}. */
    public static @Nullable Trunk bestTrunk(ServerLevel level, Vec3 from, @Nullable Vec3 toward, int radius, int minHeight,
                                            double bodyWidth) {
        List<Trunk> trunks = findTrunks(level, BlockPos.containing(from), radius, minHeight);
        return trunks.stream()
                .filter(t -> approachSpot(level, t, from, bodyWidth) != null)
                .min(Comparator.comparingDouble(t -> {
                    Vec3 c = t.center();
                    double cost = c.distanceTo(from);
                    if (toward != null) {
                        cost += 0.8D * Math.sqrt((c.x - toward.x) * (c.x - toward.x) + (c.z - toward.z) * (c.z - toward.z));
                    }
                    return cost;
                }))
                .orElse(null);
    }

    /** Where he stands on the ground to start climbing {@code trunk}: a free column on the side facing {@code from}. */
    public static @Nullable Vec3 approachSpot(ServerLevel level, Trunk trunk, Vec3 from, double bodyWidth) {
        Vec3 c = trunk.center();
        Direction best = null;
        double bestDist = Double.MAX_VALUE;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            Vec3 spot = attachPosition(trunk, d, bodyWidth, trunk.base.getY());
            AABB box = boxAt(spot, bodyWidth, 2.2D);
            if (!canOccupy(level, box, false) || !hasSupportBelow(level, spot)) {
                continue;
            }
            double dist = spot.distanceToSqr(from);
            if (dist < bestDist) {
                bestDist = dist;
                best = d;
            }
        }
        return best == null ? null : attachPosition(trunk, best, bodyWidth, trunk.base.getY());
    }

    /** Feet position hugging {@code trunk} on side {@code face} at height {@code y}. */
    public static Vec3 attachPosition(Trunk trunk, Direction face, double bodyWidth, double y) {
        Vec3 c = trunk.center();
        double off = 0.5D + bodyWidth / 2.0D + 0.02D;
        return new Vec3(c.x + face.getStepX() * off, y, c.z + face.getStepZ() * off);
    }

    /** The face of the trunk an attach position is on. */
    public static Direction faceOf(Trunk trunk, Vec3 attach) {
        Vec3 c = trunk.center();
        double dx = attach.x - c.x;
        double dz = attach.z - c.z;
        return Math.abs(dx) > Math.abs(dz) ? (dx > 0 ? Direction.EAST : Direction.WEST) : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
    }

    /** Highest standable spot on top of the canopy within {@code range} blocks of the trunk (feet position), or null. */
    public static @Nullable Vec3 findPerch(ServerLevel level, Trunk trunk, int range, double bodyWidth, double bodyHeight) {
        Vec3 best = null;
        double bestScore = -Double.MAX_VALUE;
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                int x = trunk.base.getX() + dx;
                int z = trunk.base.getZ() + dz;
                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                BlockPos surface = new BlockPos(x, top - 1, z);
                BlockState state = level.getBlockState(surface);
                if (!(isFoliage(state) || isLog(state)) || top < trunk.topY() - 2) {
                    continue;
                }
                Vec3 feet = new Vec3(x + 0.5D, top, z + 0.5D);
                if (!canOccupy(level, boxAt(feet, bodyWidth, bodyHeight), false)) {
                    continue;
                }
                double score = top * 3.0D - Math.sqrt(dx * dx + dz * dz);
                if (score > bestScore) {
                    bestScore = score;
                    best = feet;
                }
            }
        }
        return best;
    }

    /** A solid-supported, collision-free standing spot near {@code around} (ground or canopy), or null. */
    public static @Nullable Vec3 findLanding(ServerLevel level, Vec3 around, double radius, double bodyWidth, double bodyHeight,
                                             boolean preferCanopy) {
        Vec3 best = null;
        double bestScore = Double.MAX_VALUE;
        int r = (int) Math.ceil(radius);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double hd = Math.sqrt(dx * dx + dz * dz);
                if (hd > radius) {
                    continue;
                }
                int x = (int) Math.floor(around.x) + dx;
                int z = (int) Math.floor(around.z) + dz;
                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                Vec3 feet = new Vec3(x + 0.5D, top, z + 0.5D);
                if (Math.abs(top - around.y) > (preferCanopy ? 9 : 6) || !hasSupportBelow(level, feet)
                        || !canOccupy(level, boxAt(feet, bodyWidth, bodyHeight), false)) {
                    continue;
                }
                double score = hd + Math.abs(top - around.y) * 0.4D;
                if (score < bestScore) {
                    bestScore = score;
                    best = feet;
                }
            }
        }
        return best;
    }

    /** Position after {@code t} ticks of free flight from {@code from} with initial velocity {@code v} (drag and gravity). */
    public static Vec3 flightPosition(Vec3 from, Vec3 v, double gravity, double t) {
        double fh = (1.0D - Math.pow(AIR_DRAG_H, t)) / (1.0D - AIR_DRAG_H);
        double c = gravity * AIR_DRAG_V / (1.0D - AIR_DRAG_V);
        double fv = (1.0D - Math.pow(AIR_DRAG_V, t)) / (1.0D - AIR_DRAG_V);
        return new Vec3(from.x + v.x * fh, from.y + (v.y + c) * fv - c * t, from.z + v.z * fh);
    }

    /** Horizontal velocity needed to cover {@code horizontal} displacement in {@code ticks} ticks. */
    public static double requiredHorizontalSpeed(double distance, int ticks) {
        return distance * (1.0D - AIR_DRAG_H) / (1.0D - Math.pow(AIR_DRAG_H, Math.max(1, ticks)));
    }

    /**
     * Plans a jump from {@code from} to {@code to}: the shortest flight time whose launch speed is physically allowed and
     * whose whole trajectory (except the last two ticks) is clear of solid blocks for a body of the given size.
     */
    public static @Nullable JumpPlan planJump(ServerLevel level, Vec3 from, Vec3 to, double gravity, double bodyWidth, double bodyHeight) {
        double hx = to.x - from.x;
        double hz = to.z - from.z;
        double dist = Math.sqrt(hx * hx + hz * hz);
        double dirX = dist < 1.0E-4D ? 0.0D : hx / dist;
        double dirZ = dist < 1.0E-4D ? 0.0D : hz / dist;
        double c = gravity * AIR_DRAG_V / (1.0D - AIR_DRAG_V);
        for (int n = 8; n <= 44; n++) {
            double vh = requiredHorizontalSpeed(dist, n);
            if (vh > MAX_HORIZONTAL_SPEED) {
                continue;
            }
            double fv = (1.0D - Math.pow(AIR_DRAG_V, n)) / (1.0D - AIR_DRAG_V);
            double vy0 = ((to.y - from.y) + c * n) / fv - c;
            if (vy0 > MAX_LAUNCH_SPEED || vy0 < MIN_LAUNCH_SPEED) {
                continue; // a leap always springs upward off the surface it starts from
            }
            Vec3 v = new Vec3(dirX * vh, vy0, dirZ * vh);
            boolean clear = true;
            Vec3 apex = from;
            for (int half = 1; half <= (n - 2) * 2 && clear; half++) { // half-tick steps: nothing thin slips through
                Vec3 p = flightPosition(from, v, gravity, half / 2.0D);
                if (p.y > apex.y) {
                    apex = p;
                }
                clear = canOccupy(level, boxAt(p.add(0.0D, 0.05D, 0.0D), bodyWidth, bodyHeight - 0.05D), false);
            }
            if (clear) {
                return new JumpPlan(v, n, apex, to);
            }
        }
        return null;
    }
}
