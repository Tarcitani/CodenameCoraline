package com.codenamecoraline.entity.ten_threads_witch;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Finds the points the Matriarch's threads hold on to. At the start of the fight it scans a radius of 20 blocks, between
 * 4 and 14 blocks above her, for logs, and takes up to 8 anchors, one per octant around her. An octant with no natural
 * tree gets a Stitching Post instead: a visual column of thread 6 blocks high (particles only, no block is ever placed or
 * destroyed) that holds the threads of the puppets waiting their turn.
 */
public final class ArenaAnchorManager {
    public static final int RADIUS = 20;
    public static final int MIN_HEIGHT = 4;
    public static final int MAX_HEIGHT = 14;
    public static final int SECTORS = 8;
    public static final double POST_DISTANCE = 14.0D;
    public static final int POST_HEIGHT = 6;

    /** One anchor: where the thread is tied, and whether a real tree holds it (false: a Stitching Post). */
    public record Anchor(BlockPos pos, boolean natural) {
        /** Base of a Stitching Post (the ground under it). */
        public BlockPos postBase() {
            return this.pos.below(POST_HEIGHT);
        }
    }

    private ArenaAnchorManager() {}

    public static List<Anchor> scan(ServerLevel level, BlockPos center) {
        BlockPos[] best = new BlockPos[SECTORS];
        double[] bestScore = new double[SECTORS];
        java.util.Arrays.fill(bestScore, Double.MAX_VALUE);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-RADIUS, MIN_HEIGHT, -RADIUS), center.offset(RADIUS, MAX_HEIGHT, RADIUS))) {
            double dx = p.getX() - center.getX();
            double dz = p.getZ() - center.getZ();
            double h = Math.sqrt(dx * dx + dz * dz);
            if (h < 4.0D || h > RADIUS || !level.getBlockState(p).is(BlockTags.LOGS)) {
                continue;
            }
            int sector = sectorOf(dx, dz);
            double score = Math.abs(h - 12.0D) + Math.abs(p.getY() - center.getY() - 8) * 0.3D;
            if (score < bestScore[sector]) {
                bestScore[sector] = score;
                best[sector] = p.immutable();
            }
        }
        List<Anchor> anchors = new ArrayList<>();
        for (int s = 0; s < SECTORS; s++) {
            if (best[s] != null) {
                anchors.add(new Anchor(best[s], true));
            } else {
                double ang = (s + 0.5D) * Math.PI * 2.0D / SECTORS;
                BlockPos post = BlockPos.containing(center.getX() + Math.cos(ang) * POST_DISTANCE, center.getY() + POST_HEIGHT, center.getZ() + Math.sin(ang) * POST_DISTANCE);
                anchors.add(new Anchor(post, false));
            }
        }
        return anchors;
    }

    /** The octant (0 to 7) of an offset from the centre. */
    public static int sectorOf(double dx, double dz) {
        double ang = Math.atan2(dz, dx);
        if (ang < 0.0D) {
            ang += Math.PI * 2.0D;
        }
        return Mth.clamp((int) (ang / (Math.PI * 2.0D) * SECTORS), 0, SECTORS - 1);
    }

    /** Where the {@code index}-th puppet hangs while it waits: on the perimeter, 4 blocks over the ground. */
    public static Vec3 hangingSpot(Vec3 center, int index) {
        double ang = index * Math.PI * 2.0D / 10.0D;
        return new Vec3(center.x + Math.cos(ang) * 11.0D, center.y + 4.0D, center.z + Math.sin(ang) * 11.0D);
    }
}
