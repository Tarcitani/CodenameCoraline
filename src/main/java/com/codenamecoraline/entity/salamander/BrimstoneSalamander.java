package com.codenamecoraline.entity.salamander;

import com.codenamecoraline.entity.basilisk.Basilisk;
import com.codenamecoraline.entity.lithophage.Lithophage;
import com.codenamecoraline.entity.raven.FeyRaven;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Brimstone Salamander ({@code codenamecoraline:brimstone_salamander}): a predatory, magma-dwelling
 * amphibian of traditional fey alchemy lore. Native to Nether Basalt Deltas, Nether Wastes, and Crimson Forest shores.
 */
public final class BrimstoneSalamander extends Monster {
    public static final float WIDTH = 1.3F;
    public static final float HEIGHT = 0.85F;
    public static final float EYE_HEIGHT = 0.55F;

    public static final int MIN_SPAWN_Y = 24;
    public static final int MAX_SPAWN_Y = 48;

    // Core attributes & ecological distances
    public static final double MAX_HEALTH = 44.0D;
    public static final double ARMOR = 8.0D;
    public static final double ARMOR_TOUGHNESS = 2.0D;
    public static final double MOVEMENT_SPEED = 0.23D;
    public static final double LAVA_SWIM_SPEED = 0.38D;
    public static final double ATTACK_DAMAGE = 6.0D;
    public static final double KNOCKBACK_RESISTANCE = 0.65D;
    public static final double FOLLOW_RANGE = 24.0D;
    public static final double STALKING_MIN_RANGE = 8.0D;
    public static final double SPECIES_SPACING_RANGE = 4.0D;

    // Attack 1: Mordida de Fricção (SNAP_BITE)
    public static final double SNAP_BITE_MAX_REACH = 2.2D;
    public static final double SNAP_BITE_ANTI_WHIFF_REACH = 2.6D;
    public static final int SNAP_BITE_PREP_TICKS = 8;
    public static final int SNAP_BITE_ANTI_WHIFF_TICK = 7;
    public static final int SNAP_BITE_EXEC_TICKS = 4;
    public static final int SNAP_BITE_IMPACT_TICK = 2;
    public static final int SNAP_BITE_RECOVERY_TICKS = 12;
    public static final int SNAP_BITE_SHIELD_STUN_BONUS_TICKS = 10;
    public static final float SNAP_BITE_DAMAGE = 7.0F;
    public static final int SNAP_BITE_FIRE_TICKS = 40;
    public static final int SNAP_BITE_COOLDOWN_TICKS = 35;
    public static final int SNAP_BITE_CANCEL_COOLDOWN_TICKS = 15;

    // Attack 2: Cuspe Sulfuroso (SULFUR_SPIT)
    public static final double SULFUR_SPIT_MIN_REACH = 4.5D;
    public static final double SULFUR_SPIT_MAX_REACH = 16.0D;
    public static final double ANTI_PILLARING_MIN_Y = 2.0D;
    public static final double ANTI_PILLARING_MAX_Y = 5.0D;
    public static final int SULFUR_SPIT_PREP_TICKS = 16;
    public static final int SULFUR_SPIT_EXEC_TICKS = 6;
    public static final int SULFUR_SPIT_IMPACT_TICK = 2;
    public static final int SULFUR_SPIT_RECOVERY_TICKS = 18;
    public static final float SULFUR_SPIT_DAMAGE = BrimstoneSpit.DAMAGE;
    public static final int SULFUR_SPIT_COOLDOWN_TICKS = 90;

    // Attack 3: Chicotada de Cauda (TAIL_WHIP)
    public static final double TAIL_WHIP_MAX_REACH = 2.5D;
    public static final double ANTI_SURROUND_RADIUS = 2.0D;
    public static final int TAIL_WHIP_PREP_TICKS = 10;
    public static final int TAIL_WHIP_EXEC_TICKS = 5;
    public static final int TAIL_WHIP_IMPACT_TICK = 3;
    public static final int TAIL_WHIP_RECOVERY_TICKS = 14;
    public static final float TAIL_WHIP_DAMAGE = 6.0F;
    public static final double TAIL_WHIP_KNOCKBACK = 1.2D;
    public static final int TAIL_WHIP_COOLDOWN_TICKS = 60;

    // Reserved entity event range: 150..159
    public static final byte EVENT_THROAT_PUFF = (byte) 150;
    public static final byte EVENT_SULFUR_SPIT = (byte) 151;
    public static final byte EVENT_TAIL_WHIP = (byte) 152;
    public static final byte EVENT_SNAP_BITE = (byte) 153;
    public static final byte EVENT_HURT = (byte) 154;
    public static final byte EVENT_DEATH = (byte) 155;
    public static final byte EVENT_IDLE_TAIL_SWAY = (byte) 156;
    public static final byte EVENT_IDLE_SNIFF = (byte) 157;
    public static final byte EVENT_ATTACK_RECOVERY = (byte) 158;
    public static final byte EVENT_ATTACK_EXEC = (byte) 159;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(BrimstoneSalamander.class, EntityDataSerializers.INT);

    public final AnimationState idleBreatheAnimationState = new AnimationState();
    public final AnimationState idleThroatPuffAnimationState = new AnimationState();
    public final AnimationState idleTailSwayAnimationState = new AnimationState();
    public final AnimationState idleSniffAnimationState = new AnimationState();
    public final AnimationState swimLavaAnimationState = new AnimationState();
    public final AnimationState snapBitePrepAnimationState = new AnimationState();
    public final AnimationState snapBiteExecAnimationState = new AnimationState();
    public final AnimationState snapBiteRecoveryAnimationState = new AnimationState();
    public final AnimationState sulfurSpitPrepAnimationState = new AnimationState();
    public final AnimationState sulfurSpitExecAnimationState = new AnimationState();
    public final AnimationState sulfurSpitRecoveryAnimationState = new AnimationState();
    public final AnimationState tailWhipPrepAnimationState = new AnimationState();
    public final AnimationState tailWhipExecAnimationState = new AnimationState();
    public final AnimationState tailWhipRecoveryAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int idleVariationCooldown = 140;
    private int actionTicks = 0;
    private int recoveryTicks = 0;
    private int snapBiteCooldown = 0;
    private int sulfurSpitCooldown = 0;
    private int tailWhipCooldown = 0;
    private boolean retaliateWithSpit = false;
    private State lastAttackState = State.IDLE;
    private State clientActiveAttack = State.IDLE;
    private @Nullable BlockPos cachedHeatTarget;

    public enum State {
        IDLE,
        STALKING,
        PURSUING,
        SNAP_BITE,
        SULFUR_SPIT,
        TAIL_WHIP,
        RECOVERY,
        DYING
    }

    public BrimstoneSalamander(EntityType<? extends BrimstoneSalamander> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.FIRE, 0.0F);
        this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, 0.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, State.IDLE.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SalamanderCombatGoal(this));
        this.goalSelector.addGoal(2, new SeekHeatOutsideNetherGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                LivingEntity attacker = BrimstoneSalamander.this.getLastHurtByMob();
                if (attacker != null && !BrimstoneSalamander.this.canTargetEntity(attacker, true)) {
                    return false;
                }
                return super.canUse();
            }
        });
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this, Player.class, 10, true, false,
                (entity, level) -> this.canTargetEntity(entity, false)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                this, IronGolem.class, 10, true, false,
                (entity, level) -> this.canTargetEntity(entity, false)));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                this, Strider.class, 10, true, false,
                (entity, level) -> entity instanceof Strider strider && strider.isBaby() && this.canTargetEntity(entity, false)));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(
                this, MagmaCube.class, 10, true, false,
                (entity, level) -> entity instanceof MagmaCube cube && cube.getSize() <= 1 && this.canTargetEntity(entity, false)));
    }

    public boolean canTargetEntity(@Nullable LivingEntity entity, boolean directRetaliation) {
        if (entity == null || !entity.isAlive() || entity == this) {
            return false;
        }
        if (entity instanceof BrimstoneSalamander
                || entity instanceof Basilisk
                || entity instanceof Lithophage
                || entity instanceof FeyRaven) {
            return false;
        }
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        if (!directRetaliation && entity.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD)) {
            return false;
        }
        return true;
    }

    public State getState() {
        int idx = this.entityData.get(DATA_STATE);
        State[] values = State.values();
        return idx >= 0 && idx < values.length ? values[idx] : State.IDLE;
    }

    public void setState(State state) {
        this.entityData.set(DATA_STATE, state.ordinal());
    }

    public int getActionTicks() {
        return this.actionTicks;
    }

    public int getRecoveryTicks() {
        return this.recoveryTicks;
    }

    public int getSnapBiteCooldown() {
        return this.snapBiteCooldown;
    }

    public int getSulfurSpitCooldown() {
        return this.sulfurSpitCooldown;
    }

    public int getTailWhipCooldown() {
        return this.tailWhipCooldown;
    }

    public boolean isSwimmingInLava() {
        return this.isInLava() || this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA);
    }

    public boolean isInAttackOrRecovery() {
        State s = this.getState();
        return s == State.SNAP_BITE || s == State.SULFUR_SPIT || s == State.TAIL_WHIP || s == State.RECOVERY;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleBreatheAnimationState.startIfStopped(this.tickCount);
            if (this.isSwimmingInLava()) {
                this.swimLavaAnimationState.startIfStopped(this.tickCount);
            } else {
                this.swimLavaAnimationState.stop();
            }
            return;
        }

        if (!this.isAlive() || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.snapBiteCooldown > 0) {
            this.snapBiteCooldown--;
        }
        if (this.sulfurSpitCooldown > 0) {
            this.sulfurSpitCooldown--;
        }
        if (this.tailWhipCooldown > 0) {
            this.tailWhipCooldown--;
        }

        // Lava hydrodynamic speed boost (0.38 effective speed in lava)
        if (this.isSwimmingInLava() && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D) {
            Vec3 vel = this.getDeltaMovement();
            double boost = LAVA_SWIM_SPEED / MOVEMENT_SPEED;
            double maxHoriz = 0.28D;
            double nx = Mth.clamp(vel.x * boost, -maxHoriz, maxHoriz);
            double nz = Mth.clamp(vel.z * boost, -maxHoriz, maxHoriz);
            this.setDeltaMovement(nx, Math.max(vel.y, 0.02D), nz);
        }

        this.tickCombatStateMachine(serverLevel);

        if (this.getState() == State.IDLE && this.getTarget() == null) {
            if (this.tickCount % 10 == 0) {
                this.applySpeciesSpacing();
            }
            if (--this.idleVariationCooldown <= 0) {
                this.idleVariationCooldown = 120 + this.random.nextInt(121);
                int roll = this.random.nextInt(3);
                byte event = switch (roll) {
                    case 0 -> EVENT_THROAT_PUFF;
                    case 1 -> EVENT_IDLE_TAIL_SWAY;
                    default -> EVENT_IDLE_SNIFF;
                };
                this.level().broadcastEntityEvent(this, event);
            }
        }
    }

    private void applySpeciesSpacing() {
        AABB box = this.getBoundingBox().inflate(SPECIES_SPACING_RANGE, 1.5D, SPECIES_SPACING_RANGE);
        List<BrimstoneSalamander> neighbors = this.level().getEntitiesOfClass(
                BrimstoneSalamander.class,
                box,
                other -> other != this && other.isAlive() && other.getState() == State.IDLE);
        if (neighbors.isEmpty()) {
            return;
        }
        BrimstoneSalamander closest = neighbors.get(0);
        double dx = this.getX() - closest.getX();
        double dz = this.getZ() - closest.getZ();
        double dist = Math.hypot(dx, dz);
        if (dist < SPECIES_SPACING_RANGE) {
            if (dist < 1.0E-3D) {
                dx = 1.0D;
                dist = 1.0D;
            }
            double push = 0.12D;
            this.setDeltaMovement(this.getDeltaMovement().add((dx / dist) * push, 0.0D, (dz / dist) * push));
            this.hurtMarked = true;
        }
    }

    public double horizontalSurfaceDistance(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double centerDist = Math.hypot(dx, dz);
        return Math.max(0.0D, centerDist - (this.getBbWidth() * 0.35D));
    }

    public boolean isTargetFlankingOrBehind(LivingEntity target) {
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot()).normalize();
        Vec3 toTarget = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
        if (toTarget.lengthSqr() < 1.0E-4D) {
            return false;
        }
        double dot = forward.dot(toTarget.normalize());
        return dot < 0.25D; // > ~75 degrees off forward axis
    }

    public boolean isTargetPillaring(LivingEntity target) {
        double dy = target.getY() - this.getY();
        return dy >= ANTI_PILLARING_MIN_Y && dy <= ANTI_PILLARING_MAX_Y;
    }

    public int countNearbyEnemies(double radius) {
        AABB box = this.getBoundingBox().inflate(radius, 1.5D, radius);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                entity -> this.canTargetEntity(entity, true));
        return nearby.size();
    }

    public void startSnapBite() {
        this.setState(State.SNAP_BITE);
        this.lastAttackState = State.SNAP_BITE;
        this.actionTicks = 0;
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, EVENT_SNAP_BITE);
    }

    public void startSulfurSpit() {
        this.setState(State.SULFUR_SPIT);
        this.lastAttackState = State.SULFUR_SPIT;
        this.actionTicks = 0;
        this.retaliateWithSpit = false;
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, EVENT_SULFUR_SPIT);
    }

    public void startTailWhip() {
        this.setState(State.TAIL_WHIP);
        this.lastAttackState = State.TAIL_WHIP;
        this.actionTicks = 0;
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, EVENT_TAIL_WHIP);
    }

    private void enterRecovery(int ticks) {
        this.setState(State.RECOVERY);
        this.recoveryTicks = ticks;
        this.actionTicks = 0;
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, EVENT_ATTACK_RECOVERY);
    }

    private void tickCombatStateMachine(ServerLevel level) {
        State current = this.getState();
        LivingEntity target = this.getTarget();
        if (target != null && !target.isAlive()) {
            this.setTarget(null);
            target = null;
        }

        switch (current) {
            case SNAP_BITE -> this.tickSnapBite(level, target);
            case SULFUR_SPIT -> this.tickSulfurSpit(level, target);
            case TAIL_WHIP -> this.tickTailWhip(level, target);
            case RECOVERY -> {
                if (--this.recoveryTicks <= 0) {
                    this.setState(target != null ? State.PURSUING : State.IDLE);
                }
            }
            case STALKING, PURSUING -> {
                if (target == null) {
                    this.setState(State.IDLE);
                }
            }
            default -> {}
        }
    }

    private void tickSnapBite(ServerLevel level, @Nullable LivingEntity target) {
        this.actionTicks++;
        this.getNavigation().stop();
        if (target != null && this.actionTicks <= SNAP_BITE_PREP_TICKS) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        // Anti-whiff check at prep tick 7: if target backed past 2.6 blocks, cancel cleanly with 15t cooldown
        if (this.actionTicks == SNAP_BITE_ANTI_WHIFF_TICK) {
            if (target == null || this.horizontalSurfaceDistance(target) > SNAP_BITE_ANTI_WHIFF_REACH) {
                this.snapBiteCooldown = SNAP_BITE_CANCEL_COOLDOWN_TICKS;
                this.actionTicks = 0;
                this.setState(target != null ? State.PURSUING : State.IDLE);
                this.level().broadcastEntityEvent(this, EVENT_ATTACK_RECOVERY);
                return;
            }
        }

        if (this.actionTicks == SNAP_BITE_PREP_TICKS) {
            this.level().broadcastEntityEvent(this, EVENT_ATTACK_EXEC);
        }

        if (this.actionTicks == SNAP_BITE_PREP_TICKS + SNAP_BITE_IMPACT_TICK) {
            this.playSound(SoundEvents.EVOKER_FANGS_ATTACK, 1.0F, 0.75F);
            int nextRecovery = SNAP_BITE_RECOVERY_TICKS;
            if (target != null && this.horizontalSurfaceDistance(target) <= SNAP_BITE_ANTI_WHIFF_REACH) {
                boolean shieldBlocked = target instanceof Player player && player.isBlocking();
                if (shieldBlocked) {
                    target.hurtServer(level, this.damageSources().mobAttack(this), SNAP_BITE_DAMAGE);
                    nextRecovery += SNAP_BITE_SHIELD_STUN_BONUS_TICKS;
                } else {
                    boolean hit = target.hurtServer(level, this.damageSources().mobAttack(this), SNAP_BITE_DAMAGE);
                    if (hit) {
                        target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), SNAP_BITE_FIRE_TICKS));
                    }
                }
            }
            this.snapBiteCooldown = SNAP_BITE_COOLDOWN_TICKS;
            this.recoveryTicks = nextRecovery;
        }

        if (this.actionTicks >= SNAP_BITE_PREP_TICKS + SNAP_BITE_EXEC_TICKS) {
            int rec = this.recoveryTicks > 0 ? this.recoveryTicks : SNAP_BITE_RECOVERY_TICKS;
            this.enterRecovery(rec);
        }
    }

    private void tickSulfurSpit(ServerLevel level, @Nullable LivingEntity target) {
        this.actionTicks++;
        this.getNavigation().stop();
        if (target != null) {
            this.getLookControl().setLookAt(target, 35.0F, 35.0F);
        }

        if (this.actionTicks == SULFUR_SPIT_PREP_TICKS) {
            this.level().broadcastEntityEvent(this, EVENT_ATTACK_EXEC);
        }

        if (this.actionTicks == SULFUR_SPIT_PREP_TICKS + SULFUR_SPIT_IMPACT_TICK) {
            this.fireBrimstoneSpit(level, target);
            this.sulfurSpitCooldown = SULFUR_SPIT_COOLDOWN_TICKS;
        }

        if (this.actionTicks >= SULFUR_SPIT_PREP_TICKS + SULFUR_SPIT_EXEC_TICKS) {
            this.enterRecovery(SULFUR_SPIT_RECOVERY_TICKS);
        }
    }

    private void fireBrimstoneSpit(ServerLevel level, @Nullable LivingEntity target) {
        BrimstoneSpit spit = new BrimstoneSpit(level, this);
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot());
        spit.setPos(this.getX() + forward.x * 0.7D, this.getEyeY() - 0.1D, this.getZ() + forward.z * 0.7D);

        if (target != null) {
            double dx = target.getX() - spit.getX();
            double dz = target.getZ() - spit.getZ();
            double horiz = Math.hypot(dx, dz);
            // When target is pillaring, aim at their feet/pillar top with a high ballistic arc
            double aimY = this.isTargetPillaring(target) ? target.getY() + 0.2D : target.getY(0.35D);
            double dy = aimY - spit.getY() + horiz * 0.22D;
            spit.shoot(dx, dy, dz, 0.95F, 1.0F);
        } else {
            spit.shoot(forward.x, 0.2D, forward.z, 0.95F, 1.0F);
        }
        level.addFreshEntity(spit);
        this.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 0.65F + this.random.nextFloat() * 0.15F);
    }

    private void tickTailWhip(ServerLevel level, @Nullable LivingEntity target) {
        this.actionTicks++;
        this.getNavigation().stop();

        if (this.actionTicks == TAIL_WHIP_PREP_TICKS) {
            this.level().broadcastEntityEvent(this, EVENT_ATTACK_EXEC);
        }

        if (this.actionTicks == TAIL_WHIP_PREP_TICKS + TAIL_WHIP_IMPACT_TICK) {
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.6F);
            AABB sweepBox = this.getBoundingBox().inflate(TAIL_WHIP_MAX_REACH, 1.0D, TAIL_WHIP_MAX_REACH);
            List<LivingEntity> victims = level.getEntitiesOfClass(
                    LivingEntity.class,
                    sweepBox,
                    entity -> this.canTargetEntity(entity, true) && this.horizontalSurfaceDistance(entity) <= TAIL_WHIP_MAX_REACH);
            for (int i = 0, n = victims.size(); i < n; i++) {
                LivingEntity victim = victims.get(i);
                victim.hurtServer(level, this.damageSources().mobAttack(this), TAIL_WHIP_DAMAGE);
                double dx = victim.getX() - this.getX();
                double dz = victim.getZ() - this.getZ();
                double len = Math.hypot(dx, dz);
                if (len < 1.0E-3D) {
                    dx = 1.0D;
                    len = 1.0D;
                }
                victim.push((dx / len) * TAIL_WHIP_KNOCKBACK, 0.25D, (dz / len) * TAIL_WHIP_KNOCKBACK);
                victim.hurtMarked = true;
            }
            this.tailWhipCooldown = TAIL_WHIP_COOLDOWN_TICKS;
        }

        if (this.actionTicks >= TAIL_WHIP_PREP_TICKS + TAIL_WHIP_EXEC_TICKS) {
            this.enterRecovery(TAIL_WHIP_RECOVERY_TICKS);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_THROAT_PUFF -> {
                this.idleTailSwayAnimationState.stop();
                this.idleSniffAnimationState.stop();
                this.idleThroatPuffAnimationState.start(this.tickCount);
                for (int i = 0; i < 4; i++) {
                    this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                            this.getX(), this.getEyeY(), this.getZ(),
                            (this.random.nextDouble() - 0.5D) * 0.05D, 0.03D, (this.random.nextDouble() - 0.5D) * 0.05D);
                }
            }
            case EVENT_IDLE_TAIL_SWAY -> {
                this.idleThroatPuffAnimationState.stop();
                this.idleSniffAnimationState.stop();
                this.idleTailSwayAnimationState.start(this.tickCount);
            }
            case EVENT_IDLE_SNIFF -> {
                this.idleThroatPuffAnimationState.stop();
                this.idleTailSwayAnimationState.stop();
                this.idleSniffAnimationState.start(this.tickCount);
            }
            case EVENT_SNAP_BITE -> {
                this.stopAllAttackAnimations();
                this.clientActiveAttack = State.SNAP_BITE;
                this.snapBitePrepAnimationState.start(this.tickCount);
            }
            case EVENT_SULFUR_SPIT -> {
                this.stopAllAttackAnimations();
                this.clientActiveAttack = State.SULFUR_SPIT;
                this.sulfurSpitPrepAnimationState.start(this.tickCount);
            }
            case EVENT_TAIL_WHIP -> {
                this.stopAllAttackAnimations();
                this.clientActiveAttack = State.TAIL_WHIP;
                this.tailWhipPrepAnimationState.start(this.tickCount);
            }
            case EVENT_ATTACK_EXEC -> {
                switch (this.clientActiveAttack) {
                    case SNAP_BITE -> {
                        this.snapBitePrepAnimationState.stop();
                        this.snapBiteExecAnimationState.start(this.tickCount);
                    }
                    case SULFUR_SPIT -> {
                        this.sulfurSpitPrepAnimationState.stop();
                        this.sulfurSpitExecAnimationState.start(this.tickCount);
                    }
                    case TAIL_WHIP -> {
                        this.tailWhipPrepAnimationState.stop();
                        this.tailWhipExecAnimationState.start(this.tickCount);
                    }
                    default -> {}
                }
            }
            case EVENT_ATTACK_RECOVERY -> {
                switch (this.clientActiveAttack) {
                    case SNAP_BITE -> {
                        this.snapBitePrepAnimationState.stop();
                        this.snapBiteExecAnimationState.stop();
                        this.snapBiteRecoveryAnimationState.start(this.tickCount);
                    }
                    case SULFUR_SPIT -> {
                        this.sulfurSpitPrepAnimationState.stop();
                        this.sulfurSpitExecAnimationState.stop();
                        this.sulfurSpitRecoveryAnimationState.start(this.tickCount);
                    }
                    case TAIL_WHIP -> {
                        this.tailWhipPrepAnimationState.stop();
                        this.tailWhipExecAnimationState.stop();
                        this.tailWhipRecoveryAnimationState.start(this.tickCount);
                    }
                    default -> this.stopAllAttackAnimations();
                }
            }
            case EVENT_HURT -> this.hurtAnimationState.start(this.tickCount);
            case EVENT_DEATH -> {
                this.stopAllAttackAnimations();
                this.deathAnimationState.start(this.tickCount);
            }
            default -> super.handleEntityEvent(id);
        }
    }

    public void stopAllAttackAnimations() {
        this.snapBitePrepAnimationState.stop();
        this.snapBiteExecAnimationState.stop();
        this.snapBiteRecoveryAnimationState.stop();
        this.sulfurSpitPrepAnimationState.stop();
        this.sulfurSpitExecAnimationState.stop();
        this.sulfurSpitRecoveryAnimationState.stop();
        this.tailWhipPrepAnimationState.stop();
        this.tailWhipExecAnimationState.stop();
        this.tailWhipRecoveryAnimationState.stop();
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return super.causeFallDamage(fallDistance, multiplier * 0.4F, source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.FREEZE)) {
            amount *= 2.0F;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive()) {
            level.broadcastEntityEvent(this, EVENT_HURT);
            if (source.is(DamageTypeTags.IS_PROJECTILE)) {
                this.retaliateWithSpit = true;
                this.sulfurSpitCooldown = 0;
            }
            if (source.getEntity() instanceof LivingEntity attacker && this.canTargetEntity(attacker, true)) {
                LivingEntity currentTarget = this.getTarget();
                if (currentTarget == null || !currentTarget.isAlive()) {
                    this.setTarget(attacker);
                } else if (currentTarget != attacker) {
                    // Threat hysteresis: switch target only on heavy blow (>= 6.0), much closer threat (> 3.5 blocks closer), or ranged hit while out of melee
                    double attackerDist = this.distanceTo(attacker);
                    double currentDist = this.distanceTo(currentTarget);
                    boolean heavyHit = amount >= 6.0F;
                    boolean muchCloser = attackerDist + 3.5D < currentDist;
                    boolean rangedRetaliation = source.is(DamageTypeTags.IS_PROJECTILE) && currentDist > SNAP_BITE_MAX_REACH;
                    if (heavyHit || muchCloser || rangedRetaliation) {
                        this.setTarget(attacker);
                    }
                }
            }
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide()) {
            this.setState(State.DYING);
            this.level().broadcastEntityEvent(this, EVENT_DEATH);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("SalamanderState", this.getState().name());
        output.putInt("SnapBiteCooldown", this.snapBiteCooldown);
        output.putInt("SulfurSpitCooldown", this.sulfurSpitCooldown);
        output.putInt("TailWhipCooldown", this.tailWhipCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String stateName = input.getStringOr("SalamanderState", State.IDLE.name());
        try {
            this.setState(State.valueOf(stateName));
        } catch (IllegalArgumentException ignored) {
            this.setState(State.IDLE);
        }
        this.snapBiteCooldown = input.getIntOr("SnapBiteCooldown", 0);
        this.sulfurSpitCooldown = input.getIntOr("SulfurSpitCooldown", 0);
        this.tailWhipCooldown = input.getIntOr("TailWhipCooldown", 0);
    }

    public static boolean checkSpawnRules(
            EntityType<BrimstoneSalamander> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        int y = pos.getY();
        if (y < MIN_SPAWN_Y || y > MAX_SPAWN_Y) {
            return false;
        }
        BlockState floor = level.getBlockState(pos.below());
        boolean validFloor = floor.is(Blocks.BASALT)
                || floor.is(Blocks.POLISHED_BASALT)
                || floor.is(Blocks.BLACKSTONE)
                || floor.is(Blocks.NETHERRACK)
                || floor.is(Blocks.MAGMA_BLOCK)
                || floor.is(Blocks.LAVA);
        if (!validFloor) {
            return false;
        }
        if (!level.noCollision(type.getSpawnAABB(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D))) {
            return false;
        }
        AABB area = new AABB(pos).inflate(16.0D);
        return level.getEntitiesOfClass(BrimstoneSalamander.class, area).size() < 3;
    }

    /** Primary combat goal orchestrating STALKING, PURSUING, SNAP_BITE, SULFUR_SPIT, and TAIL_WHIP. */
    private static final class SalamanderCombatGoal extends Goal {
        private final BrimstoneSalamander mob;
        private int pathRecalcTicks = 0;

        SalamanderCombatGoal(BrimstoneSalamander mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            return target != null && target.isAlive() && this.mob.canTargetEntity(target, true);
        }

        @Override
        public boolean canContinueToUse() {
            if (this.mob.isInAttackOrRecovery()) {
                return true;
            }
            LivingEntity target = this.mob.getTarget();
            return target != null && target.isAlive() && this.mob.distanceToSqr(target) <= FOLLOW_RANGE * FOLLOW_RANGE;
        }

        @Override
        public void start() {
            this.pathRecalcTicks = 0;
        }

        @Override
        public void stop() {
            this.mob.getNavigation().stop();
            if (!this.mob.isInAttackOrRecovery() && this.mob.isAlive()) {
                this.mob.setState(State.IDLE);
            }
        }

        @Override
        public void tick() {
            if (this.mob.isInAttackOrRecovery()) {
                return;
            }
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive()) {
                return;
            }

            double surfDist = this.mob.horizontalSurfaceDistance(target);
            boolean pillaring = this.mob.isTargetPillaring(target);

            // 1. Anti-cerco / Flanking Tail Whip priority
            if (this.mob.tailWhipCooldown <= 0 && surfDist <= TAIL_WHIP_MAX_REACH && !pillaring) {
                if (this.mob.countNearbyEnemies(ANTI_SURROUND_RADIUS) >= 2 || this.mob.isTargetFlankingOrBehind(target)) {
                    this.mob.startTailWhip();
                    return;
                }
            }

            // 2. Anti-pillaring, ranged retaliation, or mid-range Sulfur Spit
            if (this.mob.sulfurSpitCooldown <= 0 && this.mob.hasLineOfSight(target)) {
                boolean inSpitRange = surfDist >= SULFUR_SPIT_MIN_REACH && surfDist <= SULFUR_SPIT_MAX_REACH;
                if (pillaring || this.mob.retaliateWithSpit || inSpitRange) {
                    this.mob.startSulfurSpit();
                    return;
                }
            }

            // 3. Close-range Snap Bite (disabled when target is pillaring)
            if (this.mob.snapBiteCooldown <= 0 && surfDist <= SNAP_BITE_MAX_REACH && !pillaring) {
                this.mob.startSnapBite();
                return;
            }

            // 4. Stalking (> 8 blocks) vs Pursuing (<= 8 blocks)
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            boolean stalking = surfDist > STALKING_MIN_RANGE;
            this.mob.setState(stalking ? State.STALKING : State.PURSUING);
            if (--this.pathRecalcTicks <= 0) {
                this.pathRecalcTicks = 8;
                double speedMod = stalking ? 0.85D : 1.15D;
                this.mob.getNavigation().moveTo(target, speedMod);
            }
        }
    }

    /** Ecological goal: outside the Nether, actively seeks incandescent heat sources when idle. */
    private static final class SeekHeatOutsideNetherGoal extends Goal {
        private final BrimstoneSalamander mob;
        private int scanCooldown = 20;

        SeekHeatOutsideNetherGoal(BrimstoneSalamander mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.mob.level().dimension().equals(Level.NETHER)
                    || this.mob.getTarget() != null
                    || this.mob.isInAttackOrRecovery()) {
                return false;
            }
            if (--this.scanCooldown > 0) {
                return false;
            }
            this.scanCooldown = 40;
            BlockPos found = this.findNearestHeatBlock();
            if (found != null) {
                this.mob.cachedHeatTarget = found;
                return this.mob.blockPosition().distSqr(found) > 2.25D;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            BlockPos target = this.mob.cachedHeatTarget;
            return target != null
                    && this.mob.getTarget() == null
                    && !this.mob.getNavigation().isDone()
                    && this.mob.blockPosition().distSqr(target) > 2.25D;
        }

        @Override
        public void start() {
            BlockPos t = this.mob.cachedHeatTarget;
            if (t != null) {
                this.mob.getNavigation().moveTo(t.getX() + 0.5D, t.getY(), t.getZ() + 0.5D, 1.0D);
            }
        }

        private @Nullable BlockPos findNearestHeatBlock() {
            BlockPos origin = this.mob.blockPosition();
            Level level = this.mob.level();
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int dy = -2; dy <= 2; dy++) {
                for (int dx = -6; dx <= 6; dx++) {
                    for (int dz = -6; dz <= 6; dz++) {
                        cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                        BlockState state = level.getBlockState(cursor);
                        if (isHeatBlock(state)) {
                            double d = origin.distSqr(cursor);
                            if (d < bestDist) {
                                bestDist = d;
                                best = cursor.immutable();
                            }
                        }
                    }
                }
            }
            return best;
        }

        private static boolean isHeatBlock(BlockState state) {
            return state.is(Blocks.LAVA)
                    || state.is(Blocks.MAGMA_BLOCK)
                    || state.is(Blocks.CAMPFIRE)
                    || state.is(Blocks.SOUL_CAMPFIRE)
                    || state.is(Blocks.FIRE)
                    || state.is(Blocks.SOUL_FIRE)
                    || state.is(Blocks.TORCH);
        }
    }
}
