package com.codenamecoraline.entity.crimson;

import com.codenamecoraline.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Crimson Ash Vanguard: a lesser spectral warrior in grey ash armour with crimson seams, raised by the Crimson Commander
 * when his oath awakens. Bound to its master by UUID: it never attacks him or his other vanguards, his blows never hurt
 * it, at most {@link CrimsonCommander#MAX_VANGUARDS} stand at once, it lasts {@link #LIFETIME} ticks, drops nothing (no
 * farming) and crumbles to ash the moment its master falls. One raised from a spawn egg has no master and no time limit.
 */
public class CrimsonAshVanguard extends Monster {
    public static final float WIDTH = 0.7F;
    public static final float HEIGHT = 1.95F;
    public static final double MAX_HEALTH = 28.0D;
    public static final double ATTACK_DAMAGE = 6.0D;
    public static final double SPEED = 0.36D;
    public static final int LIFETIME = 600;
    public static final int RISE_TICKS = 20;
    public static final int CRUMBLE_TICKS = 20;
    // its cut follows the master's slash timing, so it plays the same animations
    public static final int SWING_PREP = 12;
    public static final int SWING_EXEC = 6;
    public static final int SWING_HIT_TICK = 3;
    public static final int SWING_RECOVER = 14;
    public static final int SWING_COOLDOWN = 16;
    public static final double SWING_REACH = 2.4D;

    public enum Activity { RISE, GROUND, SWING_PREPARE, SWING_EXECUTE, SWING_RECOVER, CRUMBLE }

    private static final EntityDataAccessor<Integer> DATA_ANIM =
            SynchedEntityData.defineId(CrimsonAshVanguard.class, EntityDataSerializers.INT);

    public final AnimationState animState = new AnimationState();

    private @Nullable UUID ownerUuid;
    private Activity activity = Activity.RISE;
    private int activityTimer = 0;
    private int age = 0;
    private int swingCooldown = 0;
    private int pathRecalc = 0;
    private int swingsLanded = 0;

    public CrimsonAshVanguard(EntityType<? extends CrimsonAshVanguard> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, SPEED)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANIM, CrimsonAnim.VANGUARD_RISE.ordinal());
    }

    public CrimsonAnim getAnim() {
        return CrimsonAnim.byOrdinal(this.entityData.get(DATA_ANIM));
    }

    private void setAnim(CrimsonAnim anim) {
        if (this.getAnim() != anim) {
            this.entityData.set(DATA_ANIM, anim.ordinal());
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (this.level().isClientSide() && DATA_ANIM.equals(accessor)) {
            this.animState.start(this.tickCount);
        }
    }

    public Activity getActivity() {
        return this.activity;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.animState.startIfStopped(this.tickCount); // the rise is the synched default: no update ever starts it
        }
    }

    private void setActivity(Activity activity) {
        this.activity = activity;
        this.activityTimer = 0;
    }

    public int getAge() {
        return this.age;
    }

    /** Sets how long it has stood (its lifetime runs out at {@link #LIFETIME}); for tests and commands. */
    public void setAge(int ticks) {
        this.age = ticks;
    }

    public int getSwingsLanded() {
        return this.swingsLanded;
    }

    // ---------------------------------------------------------------- the master
    public void setOwner(CrimsonCommander owner) {
        this.ownerUuid = owner.getUUID();
    }

    public @Nullable UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public boolean isOwnedBy(CrimsonCommander commander) {
        return this.ownerUuid != null && this.ownerUuid.equals(commander.getUUID());
    }

    private @Nullable CrimsonCommander owner(ServerLevel level) {
        return this.ownerUuid != null && level.getEntity(this.ownerUuid) instanceof CrimsonCommander c && c.isAlive() ? c : null;
    }

    /** Falls apart into ash: the master fell, its time ran out, or it was slain. */
    public void crumble() {
        if (this.activity == Activity.CRUMBLE || this.isRemoved()) {
            return;
        }
        this.setActivity(Activity.CRUMBLE);
        this.setAnim(CrimsonAnim.VANGUARD_CRUMBLE);
        this.getNavigation().stop();
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ASH, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 0.3D, 0.6D, 0.3D, 0.02D);
        }
        this.playSound(ModSounds.VANGUARD_DEATH.get(), 0.9F, 0.8F);
    }

    // ---------------------------------------------------------------- alliances and damage
    /** Players and iron golems only: never its master, another vanguard or any other Commander. */
    @Override
    public boolean canAttack(LivingEntity target) {
        return (target instanceof Player || target instanceof IronGolem) && super.canAttack(target);
    }

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        return other instanceof CrimsonCommander || other instanceof CrimsonAshVanguard || super.considersEntityAsAlly(other);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof CrimsonCommander || attacker instanceof CrimsonAshVanguard || this.activity == Activity.CRUMBLE) {
            return false; // the master's sweeps and slams pass through his own
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void die(DamageSource source) {
        this.setAnim(CrimsonAnim.VANGUARD_CRUMBLE);
        super.die(source);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime >= CRUMBLE_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            if (this.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.ASH, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 0.3D, 0.6D, 0.3D, 0.02D);
            }
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.ownerUuid == null && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    // ---------------------------------------------------------------- the brain
    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!this.isAlive()) {
            return;
        }
        this.activityTimer++;
        this.swingCooldown = Math.max(0, this.swingCooldown - 1);
        this.pathRecalc = Math.max(0, this.pathRecalc - 1);
        if (this.ownerUuid != null && this.activity != Activity.CRUMBLE) {
            this.age++;
            // the master's chunk may load a moment after this one: no verdict in the first two seconds
            if (this.age >= LIFETIME || (this.tickCount > 40 && this.age % 10 == 0 && this.owner(level) == null)) {
                this.crumble();
            }
        }
        switch (this.activity) {
            case RISE -> {
                this.getNavigation().stop();
                if (this.activityTimer >= RISE_TICKS) {
                    this.setActivity(Activity.GROUND);
                    this.setAnim(CrimsonAnim.COMBAT_IDLE);
                }
            }
            case GROUND -> this.tickGround(level);
            case SWING_PREPARE -> {
                this.getNavigation().stop();
                LivingEntity t = this.getTarget();
                if (t != null && this.activityTimer < SWING_PREP - 3) {
                    this.faceTowards(t);
                }
                if (this.activityTimer >= SWING_PREP) {
                    if (t == null || !this.canStrike(t)) {
                        this.setAnim(CrimsonAnim.ATTACK_CANCEL); // never swings at the air
                        this.swingCooldown = 10;
                        this.setActivity(Activity.GROUND);
                    } else {
                        this.setActivity(Activity.SWING_EXECUTE);
                        this.setAnim(CrimsonAnim.SLASH_EXECUTE);
                        this.playSound(ModSounds.CRIMSON_SLASH.get(), 0.7F, 1.2F);
                    }
                }
            }
            case SWING_EXECUTE -> {
                if (this.activityTimer == SWING_HIT_TICK) {
                    this.swingImpact(level);
                }
                if (this.activityTimer >= SWING_EXEC) {
                    this.setActivity(Activity.SWING_RECOVER);
                    this.setAnim(CrimsonAnim.SLASH_RECOVERY);
                }
            }
            case SWING_RECOVER -> {
                this.getNavigation().stop();
                if (this.activityTimer >= SWING_RECOVER) {
                    this.swingCooldown = SWING_COOLDOWN;
                    this.setActivity(Activity.GROUND);
                    this.setAnim(CrimsonAnim.COMBAT_IDLE);
                }
            }
            case CRUMBLE -> {
                this.getNavigation().stop();
                if (this.activityTimer >= CRUMBLE_TICKS) {
                    level.sendParticles(ParticleTypes.ASH, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 0.3D, 0.6D, 0.3D, 0.02D);
                    this.discard();
                }
            }
        }
    }

    private void tickGround(ServerLevel level) {
        LivingEntity target = this.chooseTarget(level);
        if (target == null) {
            this.getNavigation().stop();
            this.setAnim(CrimsonAnim.COMBAT_IDLE);
            return;
        }
        double dist = this.distanceTo(target);
        if (dist <= SWING_REACH && this.swingCooldown <= 0 && Math.abs(target.getY() - this.getY()) <= 1.5D
                && this.getSensing().hasLineOfSight(target)) {
            this.faceTowards(target);
            this.setActivity(Activity.SWING_PREPARE);
            this.setAnim(CrimsonAnim.SLASH_PREPARE);
            this.getNavigation().stop();
            return;
        }
        if (dist > SWING_REACH - 0.6D && (this.pathRecalc <= 0 || this.getNavigation().isDone())) {
            this.getNavigation().moveTo(target, dist > 8.0D ? 1.25D : 1.0D);
            this.pathRecalc = 5;
        }
        this.setAnim(this.getDeltaMovement().horizontalDistanceSqr() > 0.012D ? CrimsonAnim.RUN : CrimsonAnim.WALK);
    }

    /** The master's target first; otherwise the nearest player or golem. */
    private @Nullable LivingEntity chooseTarget(ServerLevel level) {
        CrimsonCommander owner = this.owner(level);
        if (owner != null && owner.getTarget() != null && owner.getTarget().isAlive() && this.canAttack(owner.getTarget())) {
            if (this.getTarget() != owner.getTarget()) {
                this.setTarget(owner.getTarget());
            }
            return owner.getTarget();
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && this.canAttack(current) && this.distanceTo(current) <= 32.0D) {
            return current;
        }
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(24.0D),
                en -> en.isAlive() && this.canAttack(en) && !(en instanceof Player p && p.isSpectator()))) {
            double d = this.distanceToSqr(e);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        this.setTarget(best);
        return best;
    }

    private void faceTowards(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        if (dx * dx + dz * dz > 1.0E-5D) {
            float yaw = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
            this.setYRot(yaw);
            this.setYHeadRot(yaw);
            this.yBodyRot = yaw;
        }
    }

    /** The same reach, cone and height the cut uses: a swing is committed only if it can land. */
    private boolean canStrike(LivingEntity e) {
        if (!e.isAlive() || Math.abs(e.getY() - this.getY()) > 1.8D || !this.getSensing().hasLineOfSight(e)) {
            return false;
        }
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 d = e.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        double edge = d.length() - e.getBbWidth() / 2.0D - WIDTH / 2.0D;
        return edge <= SWING_REACH && (d.length() < 0.6D || f.dot(d.normalize()) >= Math.cos(Math.toRadians(60.0D)));
    }

    private void swingImpact(ServerLevel level) {
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        level.sendParticles(CrimsonCommander.CRIMSON_DUST, this.getX() + f.x * 1.4D, this.getY() + 1.1D, this.getZ() + f.z * 1.4D,
                6, 0.4D, 0.1D, 0.4D, 0.0D);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(SWING_REACH, 0.6D, SWING_REACH),
                en -> en != this && en.isAlive() && this.canAttack(en))) {
            if (this.canStrike(e)) {
                e.hurtServer(level, this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
                this.swingsLanded++;
            }
        }
    }

    // ---------------------------------------------------------------- persistence and sounds
    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.ownerUuid != null) {
            output.putString("Owner", this.ownerUuid.toString());
        }
        output.putInt("Age", this.age);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.ownerUuid = CrimsonCommander.parseUuid(input.getStringOr("Owner", ""));
        this.age = input.getIntOr("Age", 0);
        this.setActivity(Activity.GROUND);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.VANGUARD_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.VANGUARD_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.VANGUARD_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.VANGUARD_STEP.get(), 0.5F, 0.8F);
    }
}
