package com.codenamecoraline.entity.reaper;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Authoritative catalog of the 11 attacks of the Reaper of Wandering Souls ({@code O1..O5} + {@code F1..F6}).
 * Timings, damage values, ranges, and cooldowns match {@code docs/design/soul_reaper.md} §1.4.
 */
public enum SoulReaperAttack {
    O1_WHIRLWIND(
            SoulReaper.ActionId.O1_WHIRLWIND,
            SoulReaper.Stage.STAGE_1,
            24,
            16,
            20,
            new int[] {6, 12},
            new float[] {18.0F, 18.0F},
            100,
            5.5D),
    O2_BLINK_STRIKE(
            SoulReaper.ActionId.O2_BLINK_STRIKE,
            SoulReaper.Stage.STAGE_1,
            18,
            12,
            16,
            new int[] {5},
            new float[] {16.0F},
            120,
            14.0D),
    O3_SOUL_HARVEST(
            SoulReaper.ActionId.O3_SOUL_HARVEST,
            SoulReaper.Stage.STAGE_2,
            30,
            40,
            25,
            new int[] {10, 20, 30, 40},
            new float[] {3.0F, 3.0F, 3.0F, 3.0F},
            400,
            10.0D),
    O4_FROST_WAVE(
            SoulReaper.ActionId.O4_FROST_WAVE,
            SoulReaper.Stage.STAGE_1,
            20,
            10,
            18,
            new int[] {5},
            new float[] {12.0F},
            90,
            20.0D),
    O5_REAPING_DASH(
            SoulReaper.ActionId.O5_REAPING_DASH,
            SoulReaper.Stage.STAGE_2,
            22,
            18,
            24,
            new int[] {4, 9, 14},
            new float[] {15.0F, 15.0F, 15.0F},
            140,
            12.0D),
    F1_RISING_CRESCENT(
            SoulReaper.ActionId.F1_RISING_CRESCENT,
            SoulReaper.Stage.STAGE_1,
            20,
            10,
            22,
            new int[] {6},
            new float[] {14.0F},
            60,
            6.0D),
    F2_HAFT_THRUST(
            SoulReaper.ActionId.F2_HAFT_THRUST,
            SoulReaper.Stage.STAGE_1,
            14,
            8,
            18,
            new int[] {4},
            new float[] {12.0F},
            50,
            7.0D),
    F3_CROSS_REAP(
            SoulReaper.ActionId.F3_CROSS_REAP,
            SoulReaper.Stage.STAGE_2,
            18,
            24,
            26,
            new int[] {4, 20},
            new float[] {10.0F, 13.0F},
            110,
            5.5D),
    F4_GRAVEDIGGER_SLAM(
            SoulReaper.ActionId.F4_GRAVEDIGGER_SLAM,
            SoulReaper.Stage.STAGE_2,
            32,
            12,
            36,
            new int[] {7},
            new float[] {20.0F},
            160,
            8.0D),
    F5_LOW_SWEEP(
            SoulReaper.ActionId.F5_LOW_SWEEP,
            SoulReaper.Stage.STAGE_1,
            22,
            12,
            20,
            new int[] {6},
            new float[] {11.0F},
            70,
            6.5D),
    F6_THREEFOLD_HARVEST(
            SoulReaper.ActionId.F6_THREEFOLD_HARVEST,
            SoulReaper.Stage.STAGE_3,
            16,
            24,
            30,
            new int[] {3, 10, 20},
            new float[] {7.0F, 7.0F, 11.0F},
            180,
            5.5D);

    public static final int WHIFF_PENALTY_TICKS = 6;
    public static final float F4_CONE_DAMAGE = 8.0F;
    public static final double F4_DIRECT_RADIUS = 5.0D;

    public final SoulReaper.ActionId actionId;
    public final SoulReaper.Stage minStage;
    public final int prepTicks;
    public final int execTicks;
    public final int recoveryTicks;
    public final int[] impactTicks;
    public final float[] pulseDamages;
    public final int cooldownTicks;
    public final double range;

    SoulReaperAttack(
            SoulReaper.ActionId actionId,
            SoulReaper.Stage minStage,
            int prepTicks,
            int execTicks,
            int recoveryTicks,
            int[] impactTicks,
            float[] pulseDamages,
            int cooldownTicks,
            double range) {
        this.actionId = actionId;
        this.minStage = minStage;
        this.prepTicks = prepTicks;
        this.execTicks = execTicks;
        this.recoveryTicks = recoveryTicks;
        this.impactTicks = impactTicks;
        this.pulseDamages = pulseDamages;
        this.cooldownTicks = cooldownTicks;
        this.range = range;
    }

    public boolean isFrontal() {
        return this.ordinal() >= F1_RISING_CRESCENT.ordinal();
    }

    public int pulseIndexForTick(int execTick) {
        for (int i = 0; i < this.impactTicks.length; i++) {
            if (this.impactTicks[i] == execTick) {
                return i;
            }
        }
        return -1;
    }

    /** True if {@code target} lies inside a horizontal cone of {@code maxRadius} and total angle {@code totalAngleDeg}. */
    public static boolean isInFrontArc(
            Vec3 origin,
            Vec3 forwardHorizontal,
            LivingEntity target,
            double maxRadius,
            double totalAngleDeg) {
        Vec3 delta = target.position().subtract(origin);
        if (Math.abs(delta.y) > 4.5D) {
            return false;
        }
        double distXZ = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double effectiveDist = Math.max(0.0D, distXZ - target.getBbWidth() * 0.5D);
        if (effectiveDist > maxRadius) {
            return false;
        }
        if (distXZ < 1.0E-4D) {
            return true;
        }
        Vec3 dir = new Vec3(delta.x / distXZ, 0.0D, delta.z / distXZ);
        double cosHalf = Math.cos(Math.toRadians(totalAngleDeg * 0.5D));
        return forwardHorizontal.dot(dir) >= cosHalf;
    }

    /** True if {@code target} lies inside a linear corridor of {@code length} and {@code halfWidth} along {@code forwardHorizontal}. */
    public static boolean isInLinearCorridor(
            Vec3 origin,
            Vec3 forwardHorizontal,
            LivingEntity target,
            double length,
            double halfWidth) {
        Vec3 delta = target.position().subtract(origin);
        if (Math.abs(delta.y) > 4.0D) {
            return false;
        }
        double along = delta.x * forwardHorizontal.x + delta.z * forwardHorizontal.z;
        if (along < -0.5D || along > length + target.getBbWidth() * 0.5D) {
            return false;
        }
        double perp = Math.abs(-delta.x * forwardHorizontal.z + delta.z * forwardHorizontal.x);
        return perp <= halfWidth + target.getBbWidth() * 0.5D;
    }
}

