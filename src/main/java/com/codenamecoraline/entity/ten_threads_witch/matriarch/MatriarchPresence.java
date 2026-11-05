package com.codenamecoraline.entity.ten_threads_witch.matriarch;

import com.codenamecoraline.entity.ten_threads_witch.MatriarchWitchEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * How the Matriarch carries herself while her puppets fight, and when she floats. It gives the locomotion and floating
 * animations something real to show without touching the combat: in the puppet stages she paces slowly around the centre of
 * the arena (MAT-MOVE-02), steps back from a player who comes too close (MAT-MOVE-07) and, once irritated (stage 6 on), floats
 * a block above the ground with uneven glides (MAT-FLOAT-02/03/04/08). In the last phase she walks or runs to her target as
 * before and rises into the air only to follow a target that climbed out of reach (MAT-FLOAT-06/07). The style of gait is
 * synched with the floating flag; the client derives everything else from her actual motion.
 */
public final class MatriarchPresence {
    public static final int STYLE_NORMAL = 0;
    public static final int STYLE_MENACE = 1;
    public static final int STYLE_AGGRESSIVE = 2;
    public static final int STYLE_APPROACH = 3;
    public static final double HOVER = 1.0D;
    public static final double WANDER_RADIUS = 4.0D;
    public static final double PERSONAL_SPACE = 3.5D;

    private final MatriarchWitchEntity w;
    private boolean floating = false;
    private @Nullable Vec3 goal;
    private int goalTimer = 60;
    private int pause = 0;
    private int style = STYLE_NORMAL;
    private boolean moving = false;
    private int aboveTicks = 0;

    public MatriarchPresence(MatriarchWitchEntity witch) {
        this.w = witch;
    }

    public boolean isFloating() {
        return this.floating;
    }

    public boolean isMoving() {
        return this.moving;
    }

    public void setFloating(boolean on) {
        this.floating = on;
        this.w.setNoGravity(on);
        this.publish();
    }

    private void publish() {
        this.w.setAnimGait(this.style | (this.floating ? 16 : 0));
    }

    private void setStyle(int s) {
        if (this.style != s) {
            this.style = s;
            this.publish();
        }
    }

    /** Stands still facing {@code p} (cinematics, the beats between stages). */
    public void hold(@Nullable LivingEntity p) {
        this.w.getNavigation().stop();
        this.moving = false;
        if (this.floating) {
            this.hover(this.w.position(), 0.0D);
        }
        if (p != null) {
            this.w.faceTowards(p);
        }
    }

    /** The puppet stages: pacing, keeping her distance, floating once irritated. */
    public void tickStage(ServerLevel level) {
        Player p = level.getNearestPlayer(this.w, 40.0D);
        Vec3 center = this.w.getCenter();
        Vec3 pos = this.w.position();
        boolean retreat = p != null && !p.isSpectator() && p.distanceTo(this.w) < PERSONAL_SPACE;
        if (retreat) {
            Vec3 away = pos.subtract(p.position()).multiply(1.0D, 0.0D, 1.0D);
            away = away.lengthSqr() > 1.0E-4D ? away.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 g = pos.add(away.scale(3.0D));
            Vec3 off = g.subtract(center).multiply(1.0D, 0.0D, 1.0D);
            if (off.length() > 7.0D) {
                g = center.add(off.normalize().scale(7.0D));
            }
            this.goal = g;
            this.goalTimer = 30;
            this.pause = 0;
            this.setStyle(STYLE_NORMAL);
        } else if (--this.goalTimer <= 0 || this.goal == null && this.pause <= 0) {
            var r = this.w.getRandom();
            if (r.nextInt(3) == 0) {
                this.goal = null; // she simply stands and watches for a while
                this.pause = 100 + r.nextInt(120);
            } else {
                double a = r.nextDouble() * Math.PI * 2.0D;
                double d = 1.5D + r.nextDouble() * (WANDER_RADIUS - 1.5D);
                this.goal = center.add(Math.cos(a) * d, 0.0D, Math.sin(a) * d);
            }
            this.goalTimer = 180 + r.nextInt(140);
            this.setStyle(STYLE_MENACE);
        }
        this.pause = Math.max(0, this.pause - 1);
        boolean arrived = this.goal == null || horizontal(pos, this.goal) < 0.6D;
        if (arrived) {
            this.goal = null;
        }
        if (this.floating) {
            double speed = 0.0D;
            if (this.goal != null) {
                speed = retreat ? 0.11D : 0.05D;
                if (MatriarchMood.of(this.w.getUnravel()) != MatriarchMood.COMPOSED) {
                    // the disturbing glide: sudden halts and spurts (MAT-FLOAT-04)
                    float n = (float) Math.sin(this.w.tickCount * 0.21D) + (float) Math.sin(this.w.tickCount * 0.53D + 1.7D);
                    speed *= n > 1.1F ? 2.2D : n < -0.9F ? 0.0D : 1.0D;
                }
            }
            this.hover(this.goal != null ? this.goal : pos, speed);
            this.moving = this.goal != null && speed > 0.0D;
        } else if (this.goal != null) {
            this.w.getNavigation().moveTo(this.goal.x, this.goal.y, this.goal.z, retreat ? 0.85D : 0.5D);
            this.moving = true;
        } else {
            this.w.getNavigation().stop();
            this.moving = false;
        }
        if (p != null && (retreat || !this.moving)) {
            this.w.faceTowards(p); // stepping back she keeps her eyes on whoever crowds her
        }
    }

    /** The last phase: the gait style follows the distance; she floats only to reach a target high above her. */
    public void tickFury(ServerLevel level, @Nullable LivingEntity target, boolean pathing) {
        if (target == null) {
            this.setStyle(STYLE_NORMAL);
            this.aboveTicks = 0;
            if (this.floating) {
                this.descend();
            }
            return;
        }
        double dist = this.w.distanceTo(target);
        this.setStyle(dist < 3.0D ? STYLE_AGGRESSIVE : dist < 9.0D ? STYLE_APPROACH : STYLE_NORMAL);
        double dy = target.getY() - this.w.getY();
        this.aboveTicks = dy > 3.5D ? this.aboveTicks + 1 : Math.max(0, this.aboveTicks - 2);
        if (!this.floating && this.aboveTicks > 40) {
            this.setFloating(true);
        }
        if (this.floating) {
            if (dy < 1.0D && this.aboveTicks == 0) {
                this.descend();
            } else {
                Vec3 t = target.position();
                Vec3 g = new Vec3(t.x, target.getY() + 0.2D, t.z);
                this.flyTowards(g, dist > 3.0D ? 0.12D : 0.0D);
            }
            this.moving = this.w.getDeltaMovement().horizontalDistanceSqr() > 0.002D;
        } else {
            this.moving = pathing;
        }
    }

    private void descend() {
        Vec3 pos = this.w.position();
        double ground = groundY(this.w, pos);
        if (pos.y - ground < 0.15D) {
            this.setFloating(false);
            return;
        }
        this.w.setDeltaMovement(this.w.getDeltaMovement().multiply(0.7D, 0.0D, 0.7D).add(0.0D, -0.08D, 0.0D));
    }

    private void hover(Vec3 g, double speed) {
        Vec3 pos = this.w.position();
        double ground = groundY(this.w, pos);
        this.flyTowards(new Vec3(g.x, ground + HOVER, g.z), speed);
    }

    private void flyTowards(Vec3 g, double speed) {
        Vec3 pos = this.w.position();
        Vec3 d = g.subtract(pos);
        Vec3 h = new Vec3(d.x, 0.0D, d.z);
        Vec3 v = h.lengthSqr() > 1.0E-4D && speed > 0.0D ? h.normalize().scale(Math.min(speed, h.length())) : Vec3.ZERO;
        double vy = Mth.clamp(d.y * 0.12D, -0.12D, 0.12D);
        Vec3 now = this.w.getDeltaMovement();
        this.w.setDeltaMovement(now.x * 0.5D + v.x * 0.5D, now.y * 0.5D + vy, now.z * 0.5D + v.z * 0.5D);
        if (v.lengthSqr() > 1.0E-5D) {
            float yaw = (float) (Mth.atan2(-v.x, v.z) * Mth.RAD_TO_DEG);
            this.w.setYRot(Mth.approachDegrees(this.w.getYRot(), yaw, 8.0F));
            this.w.yBodyRot = this.w.getYRot();
        }
    }

    private static double horizontal(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** The top of the first solid block under {@code pos} (up to eight blocks down). */
    public static double groundY(MatriarchWitchEntity w, Vec3 pos) {
        BlockPos.MutableBlockPos m = BlockPos.containing(pos).mutable();
        for (int i = 0; i < 8; i++) {
            if (!w.level().getBlockState(m).getCollisionShape(w.level(), m).isEmpty()) {
                return m.getY() + w.level().getBlockState(m).getCollisionShape(w.level(), m).max(net.minecraft.core.Direction.Axis.Y);
            }
            m.move(0, -1, 0);
        }
        return pos.y - 8.0D;
    }
}
