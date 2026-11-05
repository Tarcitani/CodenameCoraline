package com.codenamecoraline.entity.ten_threads_witch;

import com.codenamecoraline.entity.crimson.CrimsonCommander;
import com.codenamecoraline.entity.ten_threads_witch.PuppetAttack.Effect;
import com.codenamecoraline.entity.ten_threads_witch.PuppetAttack.Style;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Runs the attacks of one mob (a puppet, or the Matriarch in her last phase) from their {@link PuppetAttack} data: the
 * weighted choice, the prepare / execute / recover phases, the "never swing at the air" rule, the aim that locks a few
 * ticks before a special attack so it can be dodged, and the delivery of every {@link Style}. The host decides what a
 * phase looks like (state, animation, thread colour) and what a missed or crashed special attack leads to.
 */
public final class AttackRunner {
    public enum Phase { IDLE, PREPARE, EXECUTE, RECOVER }

    public interface Host {
        /** The runner entered {@code phase} of {@code attack}. */
        void phaseChanged(PuppetAttack attack, Phase phase);

        /** The attack is over ({@code hit}: it hurt someone) or was called off at the commit. */
        void attackEnded(PuppetAttack attack, boolean hit);

        /** A special attack missed (or crashed): the mob is entangled for {@code ticks}. */
        void tangle(PuppetAttack attack, int ticks);

        /** A charge ended against a wall, a tree or a raised shield. */
        void crashed(PuppetAttack attack);
    }

    /** Ticks before the end of the wind-up at which the aim is frozen. */
    public static final int AIM_LOCK_TICKS = 6;
    public static final DustParticleOptions THREAD_DUST = new DustParticleOptions(0xD8E2E8, 1.0F);
    public static final DustParticleOptions CRIMSON_DUST = new DustParticleOptions(0xFF1E42, 1.3F);
    public static final DustParticleOptions SPOOL_DUST = new DustParticleOptions(0xE0115F, 1.6F);

    private final PathfinderMob mob;
    private final List<PuppetAttack> attacks;
    private final Host host;
    private final int[] cooldowns;
    private @Nullable PuppetAttack current;
    private @Nullable PuppetAttack last;
    private Phase phase = Phase.IDLE;
    private int timer = 0;
    private boolean hitAny = false;
    private boolean crashed = false;
    private Vec3 lockedDir = Vec3.ZERO;
    private Vec3 lockedPos = Vec3.ZERO;
    private final Set<Integer> struck = new HashSet<>();
    private float damageScale = 1.0F;
    private int swings = 0;
    private int cancelled = 0;

    public AttackRunner(PathfinderMob mob, List<PuppetAttack> attacks, Host host) {
        this.mob = mob;
        this.attacks = attacks;
        this.host = host;
        this.cooldowns = new int[attacks.size()];
    }

    // ---------------------------------------------------------------- state
    public @Nullable PuppetAttack current() {
        return this.current;
    }

    public Phase phase() {
        return this.phase;
    }

    public int timer() {
        return this.timer;
    }

    public boolean isBusy() {
        return this.phase != Phase.IDLE;
    }

    public Vec3 lockedDir() {
        return this.lockedDir;
    }

    public Vec3 lockedPos() {
        return this.lockedPos;
    }

    /** Attacks that actually delivered a blow (or a charge) so far; for tests. */
    public int swings() {
        return this.swings;
    }

    /** Attacks called off at the commit because the target had left; for tests. */
    public int cancelled() {
        return this.cancelled;
    }

    public void setDamageScale(float scale) {
        this.damageScale = scale;
    }

    public int cooldown(PuppetAttack a) {
        return this.cooldowns[this.attacks.indexOf(a)];
    }

    public void setCooldown(PuppetAttack a, int ticks) {
        this.cooldowns[this.attacks.indexOf(a)] = ticks;
    }

    public void tickCooldowns() {
        for (int i = 0; i < this.cooldowns.length; i++) {
            if (this.cooldowns[i] > 0) {
                this.cooldowns[i]--;
            }
        }
    }

    // ---------------------------------------------------------------- choice
    /** Weighted choice that respects range, height, line of sight, cooldowns and the previous attack. */
    public @Nullable PuppetAttack pick(LivingEntity target, double dist, double dy, boolean sight) {
        boolean sameLevel = this.mob.isNoGravity() || (dy <= 2.8D && dy >= -3.2D);
        if (!sight || !sameLevel) {
            return null;
        }
        List<PuppetAttack> options = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        for (int i = 0; i < this.attacks.size(); i++) {
            PuppetAttack a = this.attacks.get(i);
            if (a.reactive || this.cooldowns[i] > 0 || dist < a.minRange || dist > a.maxRange) {
                continue;
            }
            options.add(a);
            weights.add(a.special ? 4 : 5);
        }
        if (options.size() > 1 && this.last != null) {
            int idx = options.indexOf(this.last);
            if (idx >= 0) {
                options.remove(idx);
                weights.remove(idx);
            }
        }
        if (options.isEmpty()) {
            return null;
        }
        int total = weights.stream().mapToInt(Integer::intValue).sum();
        int roll = this.mob.getRandom().nextInt(total);
        for (int i = 0; i < options.size(); i++) {
            roll -= weights.get(i);
            if (roll < 0) {
                return options.get(i);
            }
        }
        return options.getFirst();
    }

    // ---------------------------------------------------------------- lifecycle
    public boolean start(PuppetAttack attack, @Nullable LivingEntity target) {
        if (this.isBusy()) {
            return false;
        }
        this.current = attack;
        this.last = attack;
        this.phase = Phase.PREPARE;
        this.timer = 0;
        this.hitAny = false;
        this.crashed = false;
        this.struck.clear();
        this.lockedPos = target != null ? target.position() : this.mob.position();
        this.lockedDir = this.aimAt(target);
        this.host.phaseChanged(attack, Phase.PREPARE);
        return true;
    }

    /** Drops the current attack at once ({@code cooldown}: it also cools down a little). */
    public void cancel(boolean cooldown) {
        PuppetAttack a = this.current;
        if (a == null) {
            return;
        }
        this.restoreBody(a);
        if (cooldown) {
            int i = this.attacks.indexOf(a);
            this.cooldowns[i] = Math.max(this.cooldowns[i], 24);
        }
        this.current = null;
        this.phase = Phase.IDLE;
        this.timer = 0;
    }

    private void restoreBody(PuppetAttack a) {
        if (a.style == Style.BURROW) {
            this.mob.noPhysics = false;
            this.mob.setNoGravity(false);
        }
    }

    private Vec3 aimAt(@Nullable LivingEntity target) {
        Vec3 f = Vec3.directionFromRotation(0.0F, this.mob.getYRot());
        if (target == null) {
            return f.multiply(1.0D, 0.0D, 1.0D).normalize();
        }
        Vec3 d = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D).subtract(this.mob.position().add(0.0D, this.mob.getBbHeight() * 0.5D, 0.0D));
        if (!this.mob.isNoGravity()) {
            d = d.multiply(1.0D, 0.0D, 1.0D);
        }
        return d.lengthSqr() > 1.0E-4D ? d.normalize() : f.multiply(1.0D, 0.0D, 1.0D).normalize();
    }

    private void face(Vec3 dir) {
        if (dir.x * dir.x + dir.z * dir.z < 1.0E-5D) {
            return;
        }
        float yaw = (float) (Mth.atan2(-dir.x, dir.z) * Mth.RAD_TO_DEG);
        this.mob.setYRot(yaw);
        this.mob.setYHeadRot(yaw);
        this.mob.yBodyRot = yaw;
    }

    // ---------------------------------------------------------------- the tick
    public void tick(ServerLevel level, @Nullable LivingEntity target) {
        PuppetAttack a = this.current;
        if (a == null || this.phase == Phase.IDLE) {
            return;
        }
        this.timer++;
        switch (this.phase) {
            case PREPARE -> this.tickPrepare(level, a, target);
            case EXECUTE -> this.tickExecute(level, a, target);
            case RECOVER -> {
                this.mob.getNavigation().stop();
                if (this.timer >= a.recover) {
                    this.finish(a);
                }
            }
            default -> {
            }
        }
    }

    private void tickPrepare(ServerLevel level, PuppetAttack a, @Nullable LivingEntity target) {
        this.mob.getNavigation().stop();
        boolean aiming = target != null && target.isAlive() && this.timer < a.prep - AIM_LOCK_TICKS;
        if (aiming) {
            this.lockedPos = target.position();
            this.lockedDir = this.aimAt(target);
        }
        this.face(this.lockedDir);
        if (a.style != Style.CHARGE && a.style != Style.LEAP && a.style != Style.BURROW) {
            this.mob.setDeltaMovement(0.0D, this.mob.getDeltaMovement().y, 0.0D);
        }
        this.telegraph(level, a);
        if (this.timer >= a.prep) {
            // never a blow at the air; a special attack, whose direction was locked on purpose, fires at the locked line
            if (!a.special && !a.reactive && !this.stillUseful(a, target)) {
                this.cancelled++;
                this.cancel(true);
                this.host.attackEnded(a, false);
                return;
            }
            this.phase = Phase.EXECUTE;
            this.timer = 0;
            this.swings++;
            this.host.phaseChanged(a, Phase.EXECUTE);
        }
    }

    private boolean stillUseful(PuppetAttack a, @Nullable LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        Vec3 d = target.position().subtract(this.mob.position());
        double h = Math.sqrt(d.x * d.x + d.z * d.z);
        boolean sameLevel = this.mob.isNoGravity() || (d.y <= 3.0D && d.y >= -3.5D);
        return sameLevel && h <= a.maxRange + 1.6D;
    }

    private void telegraph(ServerLevel level, PuppetAttack a) {
        if (this.timer % 3 != 0) {
            return;
        }
        Vec3 p = this.mob.position();
        switch (a.style) {
            case CHARGE, FAN, BURROW -> {
                double len = Math.min(a.maxRange, 10.0D);
                for (double k = 1.0D; k <= len; k += 1.2D) {
                    Vec3 q = p.add(this.lockedDir.multiply(k, a.style == Style.CHARGE && this.mob.isNoGravity() ? k / Math.max(k, 1.0D) : 0.0D, k));
                    level.sendParticles(a.special ? CRIMSON_DUST : THREAD_DUST, q.x, q.y + 0.1D, q.z, 1, 0.05D, 0.0D, 0.05D, 0.0D);
                }
            }
            case RING, WAVES, STORM -> {
                double r = Math.min(a.maxRange, 6.0D);
                for (int i = 0; i < 14; i++) {
                    double ang = Math.PI * 2.0D * i / 14.0D;
                    level.sendParticles(a.special ? CRIMSON_DUST : THREAD_DUST, p.x + Math.cos(ang) * r, p.y + 0.1D, p.z + Math.sin(ang) * r,
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
            case TRAP -> {
                for (int i = 0; i < 12; i++) {
                    double ang = Math.PI * 2.0D * i / 12.0D;
                    level.sendParticles(THREAD_DUST, this.lockedPos.x + Math.cos(ang) * 2.0D, this.lockedPos.y + 0.1D,
                            this.lockedPos.z + Math.sin(ang) * 2.0D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
            case WIRE -> this.wireParticles(level, a);
            default -> {
            }
        }
    }

    private void wireParticles(ServerLevel level, PuppetAttack a) {
        Vec3 from = this.mob.position();
        Vec3 d = this.lockedPos.subtract(from).multiply(1.0D, 0.0D, 1.0D);
        double len = d.length();
        if (len < 0.5D) {
            return;
        }
        Vec3 dir = d.normalize();
        for (double k = 1.0D; k < len; k += 0.8D) {
            level.sendParticles(CRIMSON_DUST, from.x + dir.x * k, from.y + 0.3D, from.z + dir.z * k, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void tickExecute(ServerLevel level, PuppetAttack a, @Nullable LivingEntity target) {
        int t = this.timer;
        switch (a.style) {
            case CONE -> {
                if (t == Math.max(1, a.exec / 2)) {
                    this.coneStrike(level, a, a.maxRange, a.arc, a.damage);
                }
            }
            case FAN -> {
                if (t == Math.max(1, a.exec / 2)) {
                    this.coneStrike(level, a, a.maxRange, a.arc, a.damage);
                    this.fanParticles(level, a);
                }
            }
            case MULTI -> {
                for (int i = 0; i < a.hits; i++) {
                    int at = Math.max(1, (int) Math.round((i + 0.5D) * a.exec / a.hits));
                    if (t == at) {
                        this.coneStrike(level, a, a.maxRange, a.arc, a.damage * (i == a.hits - 1 ? a.lastHitFactor : 1.0F));
                    }
                }
            }
            case RING -> {
                if (t == Math.max(1, a.exec / 2)) {
                    this.ringStrike(level, a, a.maxRange, 0.0D, a.damage, 0);
                    this.ringParticles(level, this.mob.position(), a.maxRange, a);
                }
            }
            case CHARGE -> this.tickCharge(level, a);
            case LEAP -> this.tickLeap(level, a);
            case WAVES -> {
                for (int k = 0; k < 3; k++) {
                    if (t == k * a.exec / 3 + 1) {
                        double r = a.maxRange * (k + 1) / 3.0D;
                        this.ringStrike(level, a, r + 1.9D, Math.max(0.0D, r - 1.9D), a.damage, k + 1);
                        this.ringParticles(level, this.mob.position(), r, a);
                    }
                }
            }
            case STORM -> {
                if (t % 5 == 0) {
                    for (int k = 0; k < 3; k++) {
                        double ang = t * 0.55D + k * Math.PI * 2.0D / 3.0D;
                        double r = 2.0D + (t / (double) a.exec) * 7.0D;
                        Vec3 p = this.mob.position().add(Math.cos(ang) * r, 0.6D, Math.sin(ang) * r);
                        level.sendParticles(SPOOL_DUST, p.x, p.y, p.z, 8, 0.25D, 0.25D, 0.25D, 0.02D);
                        level.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 4, 0.3D, 0.3D, 0.3D, 0.1D);
                        for (LivingEntity v : this.foesNear(level, p, 1.6D)) {
                            if (this.struck.add(v.getId() * 1000 + t * 3 + k)) {
                                this.strike(level, a, v, a.damage * this.damageScale, 0.5D, 0.3D);
                            }
                        }
                    }
                }
            }
            case GRAPPLE -> {
                if (t == Math.max(1, a.exec / 2)) {
                    this.grapple(level, a);
                }
            }
            case SIDESTEP -> {
                if (t <= 3) {
                    Vec3 side = new Vec3(-this.lockedDir.z, 0.0D, this.lockedDir.x).scale(this.mob.getRandom().nextBoolean() ? 1.0D : -1.0D);
                    this.mob.setDeltaMovement(side.x * 0.8D, 0.15D, side.z * 0.8D);
                }
                this.hitAny = true;
            }
            case BURROW -> this.tickBurrow(level, a);
            case TRAP -> {
                if (t == 2) {
                    for (int i = 0; i < 16; i++) {
                        double ang = Math.PI * 2.0D * i / 16.0D;
                        level.sendParticles(ParticleTypes.COMPOSTER, this.lockedPos.x + Math.cos(ang) * 2.0D, this.lockedPos.y + 0.2D,
                                this.lockedPos.z + Math.sin(ang) * 2.0D, 2, 0.1D, 0.4D, 0.1D, 0.02D);
                    }
                    for (LivingEntity v : this.foesNear(level, this.lockedPos, 2.2D)) {
                        this.strike(level, a, v, a.damage * this.damageScale, 0.0D, 0.0D);
                    }
                }
            }
            case WIRE -> {
                if (t == Math.max(1, a.exec / 2)) {
                    this.wireStrike(level, a);
                }
            }
        }
        if (this.crashed || t >= a.exec) {
            this.endExecute(a);
        }
    }

    private void endExecute(PuppetAttack a) {
        this.restoreBody(a);
        if (a.special && a.missTangles && !this.hitAny) {
            int i = this.attacks.indexOf(a);
            this.cooldowns[i] = a.cooldown;
            this.current = null;
            this.phase = Phase.IDLE;
            this.timer = 0;
            this.host.tangle(a, a.tangleTicks);
            this.host.attackEnded(a, false);
            return;
        }
        this.phase = Phase.RECOVER;
        this.timer = 0;
        this.host.phaseChanged(a, Phase.RECOVER);
    }

    private void finish(PuppetAttack a) {
        int i = this.attacks.indexOf(a);
        this.cooldowns[i] = a.cooldown;
        this.current = null;
        this.phase = Phase.IDLE;
        this.timer = 0;
        this.host.attackEnded(a, this.hitAny);
    }

    // ---------------------------------------------------------------- styles
    private void tickCharge(ServerLevel level, PuppetAttack a) {
        double speed = Math.min(1.3D, a.maxRange / Math.max(1, a.exec));
        Vec3 dir = this.lockedDir;
        double vy = this.mob.isNoGravity() ? dir.y * speed : this.mob.getDeltaMovement().y;
        this.mob.setDeltaMovement(dir.x * speed, vy, dir.z * speed);
        this.face(dir);
        if (this.timer % 2 == 0) {
            level.sendParticles(ParticleTypes.CLOUD, this.mob.getX(), this.mob.getY() + 0.3D, this.mob.getZ(), 3, 0.3D, 0.1D, 0.3D, 0.02D);
        }
        AABB box = this.mob.getBoundingBox().inflate(0.7D, 0.3D, 0.7D);
        for (LivingEntity v : level.getEntitiesOfClass(LivingEntity.class, box, e -> this.isFoe(e))) {
            if (this.struck.add(v.getId())) {
                boolean blocking = v instanceof Player p && p.isBlocking();
                this.strike(level, a, v, a.damage * this.damageScale, a.push, a.lift);
                if (blocking) {
                    this.crashed = true;
                    this.host.crashed(a);
                }
                this.crashed = true; // the charge spends itself on its mark
            }
        }
        if (!this.crashed && this.timer > 2 && this.mob.horizontalCollision && !this.mob.isNoGravity()) {
            this.crashed = true;
            this.host.crashed(a); // against a tree or a wall: the special attack will leave it entangled
        }
        if (!this.crashed && this.timer > 2 && this.mob.isNoGravity() && this.mob.verticalCollision) {
            this.crashed = true;
            this.host.crashed(a);
        }
        if (this.crashed) {
            this.mob.setDeltaMovement(0.0D, this.mob.getDeltaMovement().y, 0.0D);
        }
    }

    /** Upward speed that brings a body back to its starting height after {@code ticks} ticks of mob gravity. */
    static double leapLift(int ticks) {
        double lo = 0.2D;
        double hi = 1.2D;
        for (int i = 0; i < 24; i++) {
            double mid = (lo + hi) / 2.0D;
            double v = mid;
            double y = 0.0D;
            for (int n = 0; n < ticks; n++) {
                y += v;
                v = (v - 0.08D) * 0.98D;
            }
            if (y > 0.0D) { // still up after the wanted flight: too strong
                hi = mid;
            } else {
                lo = mid;
            }
        }
        return (lo + hi) / 2.0D;
    }

    private void tickLeap(ServerLevel level, PuppetAttack a) {
        int flight = Math.max(8, a.exec - 2); // ticks in the air: the leap lands before the execute phase ends
        if (this.timer == 1) {
            Vec3 d = this.lockedPos.subtract(this.mob.position()).multiply(1.0D, 0.0D, 1.0D);
            this.mob.setDeltaMovement(0.0D, leapLift(flight), 0.0D);
            this.face(d.lengthSqr() > 1.0E-4D ? d.normalize() : this.lockedDir);
            level.sendParticles(ParticleTypes.CLOUD, this.mob.getX(), this.mob.getY() + 0.2D, this.mob.getZ(), 10, 0.5D, 0.1D, 0.5D, 0.03D);
        }
        if (this.timer <= flight && !this.crashed) { // steer so that the flight ends on the locked spot
            Vec3 d = this.lockedPos.subtract(this.mob.position()).multiply(1.0D, 0.0D, 1.0D);
            Vec3 step = d.scale(1.0D / (flight - this.timer + 1.0D));
            if (step.length() > 1.2D) {
                step = step.normalize().scale(1.2D);
            }
            this.mob.setDeltaMovement(step.x, this.mob.getDeltaMovement().y, step.z);
        }
        boolean landed = this.timer >= 4 && this.mob.onGround();
        if ((landed || this.timer >= a.exec) && !this.crashed) {
            this.crashed = true; // the slam ends the execute phase
            this.mob.setDeltaMovement(0.0D, this.mob.getDeltaMovement().y, 0.0D);
            this.ringStrike(level, a, 3.5D, 0.0D, a.damage, 0);
            this.ringParticles(level, this.mob.position(), 3.0D, a);
        }
    }

    private void tickBurrow(ServerLevel level, PuppetAttack a) {
        Vec3 d = this.lockedPos.subtract(this.mob.position()).multiply(1.0D, 0.0D, 1.0D);
        if (this.timer == 1) {
            this.mob.noPhysics = true;
            this.mob.setNoGravity(true);
        }
        double speed = Math.min(1.4D, Math.max(0.2D, d.length() / Math.max(1, a.exec - this.timer + 1)));
        Vec3 step = d.lengthSqr() > 1.0E-4D ? d.normalize().scale(speed) : Vec3.ZERO;
        this.mob.setDeltaMovement(step.x, 0.0D, step.z);
        level.sendParticles(CRIMSON_DUST, this.mob.getX(), this.mob.getY() + 0.1D, this.mob.getZ(), 3, 0.3D, 0.05D, 0.3D, 0.0D);
        if (this.timer >= a.exec) {
            this.mob.noPhysics = false;
            this.mob.setNoGravity(false);
            this.mob.setPos(this.mob.getX(), this.lockedPos.y, this.mob.getZ());
            this.mob.setDeltaMovement(0.0D, 0.0D, 0.0D);
            this.ringStrike(level, a, 2.6D, 0.0D, a.damage, 0);
            this.ringParticles(level, this.mob.position(), 2.4D, a);
            this.crashed = true;
        }
    }

    private void grapple(ServerLevel level, PuppetAttack a) {
        for (LivingEntity v : this.cone(level, a.maxRange, 70.0D)) {
            Vec3 pull = this.mob.position().subtract(v.position()).multiply(1.0D, 0.0D, 1.0D);
            if (pull.lengthSqr() > 1.0E-4D) {
                pull = pull.normalize().scale(1.5D);
            }
            v.setDeltaMovement(pull.x, 0.3D, pull.z);
            v.hurtMarked = true;
            this.strike(level, a, v, a.damage * this.damageScale, 0.0D, 0.0D);
            level.sendParticles(THREAD_DUST, v.getX(), v.getY() + 1.0D, v.getZ(), 12, 0.3D, 0.5D, 0.3D, 0.0D);
        }
    }

    private void wireStrike(ServerLevel level, PuppetAttack a) {
        Vec3 from = this.mob.position();
        Vec3 d = this.lockedPos.subtract(from).multiply(1.0D, 0.0D, 1.0D);
        double len = d.length();
        if (len < 0.5D) {
            return;
        }
        Vec3 dir = d.normalize();
        this.wireParticles(level, a);
        AABB box = new AABB(from, this.lockedPos).inflate(1.5D, 3.0D, 1.5D);
        for (LivingEntity v : level.getEntitiesOfClass(LivingEntity.class, box, e -> this.isFoe(e))) {
            Vec3 rel = v.position().subtract(from).multiply(1.0D, 0.0D, 1.0D);
            double along = rel.dot(dir);
            double off = rel.subtract(dir.scale(along)).length();
            boolean low = v.onGround() && v.getY() <= from.y + 0.6D; // a jump clears the wire
            if (along > 0.0D && along < len + 1.0D && off <= 1.1D && low) {
                this.strike(level, a, v, a.damage * this.damageScale, 0.6D, 0.3D);
            }
        }
    }

    private void fanParticles(ServerLevel level, PuppetAttack a) {
        Vec3 p = this.mob.position().add(0.0D, this.mob.getBbHeight() * 0.5D, 0.0D);
        for (double k = 1.0D; k <= a.maxRange; k += 1.0D) {
            level.sendParticles(ParticleTypes.CRIT, p.x + this.lockedDir.x * k, p.y, p.z + this.lockedDir.z * k, 2, 0.15D, 0.15D, 0.15D, 0.05D);
        }
    }

    private void ringParticles(ServerLevel level, Vec3 c, double r, PuppetAttack a) {
        for (int i = 0; i < 24; i++) {
            double ang = Math.PI * 2.0D * i / 24.0D;
            level.sendParticles(a.special ? CRIMSON_DUST : THREAD_DUST, c.x + Math.cos(ang) * r, c.y + 0.2D, c.z + Math.sin(ang) * r,
                    1, 0.05D, 0.05D, 0.05D, 0.0D);
        }
    }

    // ---------------------------------------------------------------- hits
    private boolean isFoe(LivingEntity e) {
        return isFoe(this.mob, e);
    }

    public static boolean isFoe(LivingEntity self, LivingEntity e) {
        if (e == self || !e.isAlive() || e instanceof TenThreadsEncounterMember) {
            return false;
        }
        return (e instanceof Player p && !p.isSpectator()) || e instanceof IronGolem;
    }

    private List<LivingEntity> foesNear(ServerLevel level, Vec3 c, double radius) {
        AABB box = new AABB(c.x - radius, c.y - 1.5D, c.z - radius, c.x + radius, c.y + 3.0D, c.z + radius);
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, this::isFoe)) {
            Vec3 d = e.position().subtract(c).multiply(1.0D, 0.0D, 1.0D);
            if (d.length() <= radius) {
                out.add(e);
            }
        }
        return out;
    }

    private List<LivingEntity> cone(ServerLevel level, double reach, double arcDegrees) {
        Vec3 f = this.lockedDir.multiply(1.0D, 0.0D, 1.0D);
        f = f.lengthSqr() > 1.0E-4D ? f.normalize() : Vec3.directionFromRotation(0.0F, this.mob.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        double minDot = Math.cos(Math.toRadians(arcDegrees / 2.0D));
        List<LivingEntity> out = new ArrayList<>();
        AABB box = this.mob.getBoundingBox().inflate(reach, 2.5D, reach);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, this::isFoe)) {
            Vec3 d = e.position().subtract(this.mob.position()).multiply(1.0D, 0.0D, 1.0D);
            double h = d.length();
            double edge = h - e.getBbWidth() / 2.0D - this.mob.getBbWidth() / 2.0D;
            double dy = e.getY() - this.mob.getY();
            boolean sameLevel = this.mob.isNoGravity() || (dy <= 3.0D && dy >= -3.2D);
            if (edge <= reach && sameLevel && (h < 0.8D || f.dot(d.normalize()) >= minDot)) {
                out.add(e);
            }
        }
        return out;
    }

    private void coneStrike(ServerLevel level, PuppetAttack a, double reach, double arc, float damage) {
        Vec3 p = this.mob.position().add(0.0D, this.mob.getBbHeight() * 0.5D, 0.0D);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, p.x + this.lockedDir.x * 1.2D, p.y, p.z + this.lockedDir.z * 1.2D, 1, 0, 0, 0, 0);
        for (LivingEntity v : this.cone(level, reach, arc)) {
            this.strike(level, a, v, damage * this.damageScale, a.push, a.lift);
        }
    }

    private void ringStrike(ServerLevel level, PuppetAttack a, double outer, double inner, float damage, int wave) {
        for (LivingEntity v : this.foesNear(level, this.mob.position(), outer)) {
            Vec3 d = v.position().subtract(this.mob.position()).multiply(1.0D, 0.0D, 1.0D);
            if (d.length() < inner) {
                continue;
            }
            if (a.groundOnly && (!v.onGround() || v.getY() > this.mob.getY() + 0.9D)) {
                continue; // a jump clears the wave
            }
            if (!this.struck.add(v.getId() * 10 + wave)) {
                continue;
            }
            this.strike(level, a, v, damage * this.damageScale, a.push > 0.0D ? a.push : 0.5D, a.lift > 0.0D ? a.lift : 0.35D);
        }
    }

    /** Hurts first (the hit's own knock would otherwise override the launch), then throws the victim away from the mob. */
    private void strike(ServerLevel level, PuppetAttack a, LivingEntity victim, float damage, double push, double lift) {
        if (a.breaksShield && victim instanceof Player p && p.isBlocking()) {
            CrimsonCommander.disableShield(level, p);
        }
        boolean hurt = victim.hurtServer(level, this.mob.damageSources().mobAttack(this.mob), damage);
        this.hitAny = true;
        if (push > 0.0D || lift > 0.0D) {
            Vec3 away = victim.position().subtract(this.mob.position()).multiply(1.0D, 0.0D, 1.0D);
            if (away.lengthSqr() > 1.0E-4D) {
                away = away.normalize();
                victim.setDeltaMovement(away.x * push, Math.max(victim.getDeltaMovement().y, lift), away.z * push);
                victim.hurtMarked = true;
            }
        }
        if (hurt || damage <= 0.0F) {
            this.applyEffect(a, victim);
        }
    }

    private void applyEffect(PuppetAttack a, LivingEntity victim) {
        if (a.effect == Effect.NONE) {
            return;
        }
        var holder = switch (a.effect) {
            case SLOWNESS, ROOT -> MobEffects.SLOWNESS;
            case WITHER -> MobEffects.WITHER;
            case NAUSEA -> MobEffects.NAUSEA;
            case POISON -> MobEffects.POISON;
            default -> null;
        };
        if (holder != null) {
            victim.addEffect(new MobEffectInstance(holder, a.effectTicks, a.effectAmplifier), this.mob);
        }
    }
}
