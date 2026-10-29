package com.codenamecoraline.entity.lithophage;

import com.codenamecoraline.registry.ModSounds;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Lithophage (Litófago): an ancient, territorial subterranean mineral creature made of hardened clay,
 * dark sediments and amber crystal veins. Rests dormant like a rock formation in Lush Caves and
 * Dripstone Caves until an intruder enters its territory.
 */
public final class Lithophage extends Monster {
    public enum State {
        DORMANT,
        AWAKENING,
        VIGILANT,
        PURSUING,
        COMBAT,
        RECOVERY,
        DYING,
        FALLING_ASLEEP
    }

    public enum AttackType {
        NONE,
        SLAM,
        SWEEP,
        SHARD
    }

    // Entity events broadcast from server to client to drive deterministic animations.
    public static final byte EVENT_AWAKEN = 101;
    public static final byte EVENT_ALERT = 102;
    public static final byte EVENT_TURN = 103;
    public static final byte EVENT_ATTACK_SLAM = 104;
    public static final byte EVENT_ATTACK_SWEEP = 105;
    public static final byte EVENT_ATTACK_SHARD = 106;
    public static final byte EVENT_ATTACK_RECOVERY = 107;
    public static final byte EVENT_HURT = 108;
    public static final byte EVENT_DEATH = 109;
    public static final byte EVENT_FALL_ASLEEP = 110;

    // Balance and habitat constants.
    public static final double MAX_HEALTH = 36.0D;
    public static final double ARMOR = 6.0D;
    public static final double MOVEMENT_SPEED = 0.22D;
    public static final double KNOCKBACK_RESISTANCE = 0.55D;
    public static final double DETECTION_RANGE = 12.0D;
    public static final double MAX_PURSUIT_DISTANCE = 20.0D;
    public static final double RESONANCE_RADIUS = 8.0D;
    public static final int MIN_SPAWN_Y = -20;
    public static final int MAX_SPAWN_Y = 20;

    // Attack 1: Golpe de Escória (vertical heavy slam).
    public static final float SLAM_DAMAGE = 5.0F;
    public static final int SLAM_WINDUP_TICKS = 12; // 0.6 s
    public static final int SLAM_TOTAL_TICKS = 16;
    public static final int SLAM_RECOVERY_TICKS = 16; // 0.8 s
    public static final double SLAM_START_REACH = 2.1D;
    public static final double SLAM_STRIKE_REACH = 2.5D;
    public static final double SLAM_KNOCKBACK = 0.65D;

    // Attack 2: Varredura Tectônica (lateral frontal-arc sweep).
    public static final float SWEEP_DAMAGE = 4.0F;
    public static final int SWEEP_WINDUP_TICKS = 16; // 0.8 s
    public static final int SWEEP_TOTAL_TICKS = 20;
    public static final int SWEEP_RECOVERY_TICKS = 18; // 0.9 s
    public static final double SWEEP_START_REACH = 2.3D;
    public static final double SWEEP_STRIKE_REACH = 2.8D;
    public static final double SWEEP_ARC_MIN_DOT = 0.42D; // ~130 degree frontal arc (±65 deg)
    public static final double SWEEP_KNOCKBACK = 0.80D;

    // Attack 3: Estilhaço de Âmbar (ranged mineral amber shard).
    public static final float SHARD_DAMAGE = AmberShard.DAMAGE;
    public static final int SHARD_WINDUP_TICKS = 15; // 0.75 s
    public static final int SHARD_TOTAL_TICKS = 19;
    public static final int SHARD_RECOVERY_TICKS = 12; // 0.6 s
    public static final int SHARD_MIN_COOLDOWN_TICKS = 60; // 3.0 s
    public static final int SHARD_MAX_COOLDOWN_TICKS = 100; // 5.0 s
    public static final double SHARD_MIN_DISTANCE = 3.6D;
    public static final double SHARD_MAX_DISTANCE = 13.5D;
    public static final float SHARD_SPEED = 1.15F;

    // State timing constants.
    public static final int AWAKENING_TICKS = 30; // 1.5 s
    public static final int FALL_ASLEEP_TICKS = 28; // 1.4 s
    public static final int VIGILANT_ASSESS_TICKS = 5;
    public static final int VIGILANT_TIMEOUT_TICKS = 60; // 3.0 s without threat before initiating return to sleep

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(Lithophage.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ATTACK_TYPE =
            SynchedEntityData.defineId(Lithophage.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_AWAKENING_TICKS =
            SynchedEntityData.defineId(Lithophage.class, EntityDataSerializers.INT);

    // Client animation states (all keyframe animations).
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState idleAlternateAnimationState = new AnimationState();
    public final AnimationState awakeningAnimationState = new AnimationState();
    public final AnimationState alertAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState turnAnimationState = new AnimationState();
    public final AnimationState attackSlamAnimationState = new AnimationState();
    public final AnimationState attackSweepAnimationState = new AnimationState();
    public final AnimationState attackShardAnimationState = new AnimationState();
    public final AnimationState attackRecoveryAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();
    public final AnimationState fallAsleepAnimationState = new AnimationState();

    private @Nullable BlockPos homePos;
    private @Nullable Vec3 lastKnownTargetPos;
    private int awakeningTicks;
    private int vigilantAssessTicks;
    private int vigilantIdleTicks;
    private int attackTicks;
    private int recoveryTicks;
    private int slamCooldown;
    private int sweepCooldown;
    private int shardCooldown;
    private int pathRecalcTicks;
    private boolean lastMeleeWasSlam;
    private float previousYRot;
    private int idleAlternateCooldown = 90;
    private int idleAlternateTicksLeft;

    public Lithophage(EntityType<? extends Lithophage> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, ARMOR)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.ATTACK_DAMAGE, SLAM_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, MAX_PURSUIT_DISTANCE)
                .add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, State.DORMANT.ordinal());
        builder.define(DATA_ATTACK_TYPE, AttackType.NONE.ordinal());
        builder.define(DATA_AWAKENING_TICKS, 0);
    }

    public State getState() {
        int idx = Mth.clamp(this.entityData.get(DATA_STATE), 0, State.values().length - 1);
        return State.values()[idx];
    }

    public void setState(State state) {
        this.entityData.set(DATA_STATE, state.ordinal());
    }

    public AttackType getActiveAttack() {
        int idx = Mth.clamp(this.entityData.get(DATA_ATTACK_TYPE), 0, AttackType.values().length - 1);
        return AttackType.values()[idx];
    }

    private void setActiveAttack(AttackType type) {
        this.entityData.set(DATA_ATTACK_TYPE, type.ordinal());
    }

    public float getAwakeningProgress() {
        State current = this.getState();
        if (current == State.DORMANT) {
            return 0.0F;
        }
        if (current == State.AWAKENING || current == State.FALLING_ASLEEP) {
            return Mth.clamp(this.entityData.get(DATA_AWAKENING_TICKS) / (float) AWAKENING_TICKS, 0.0F, 1.0F);
        }
        return 1.0F;
    }

    public @Nullable BlockPos getHomePos() {
        return this.homePos;
    }

    public void setHomePos(BlockPos pos) {
        this.homePos = pos.immutable();
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.homePos = this.blockPosition();
        this.setState(State.DORMANT);
        return data;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LithophageBrainGoal());
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 5, true, false,
                (target, level) -> this.canAttack(target)
                        && this.distanceToSqr(target) <= DETECTION_RANGE * DETECTION_RANGE
                        && this.isWithinTerritory(target)));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof Lithophage) && super.canAttack(target);
    }

    public boolean isWithinTerritory(LivingEntity target) {
        BlockPos anchor = this.homePos != null ? this.homePos : this.blockPosition();
        Vec3 center = Vec3.atCenterOf(anchor);
        double maxDistSqr = (MAX_PURSUIT_DISTANCE + 2.0D) * (MAX_PURSUIT_DISTANCE + 2.0D);
        return this.position().distanceToSqr(center) <= MAX_PURSUIT_DISTANCE * MAX_PURSUIT_DISTANCE
                && target.position().distanceToSqr(center) <= maxDistSqr;
    }

    private boolean isPosWithinTerritory(Vec3 pos) {
        BlockPos anchor = this.homePos != null ? this.homePos : this.blockPosition();
        return pos.distanceToSqr(Vec3.atCenterOf(anchor)) <= MAX_PURSUIT_DISTANCE * MAX_PURSUIT_DISTANCE;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof Lithophage || !this.isAlive()) {
            return;
        }
        super.setTarget(target);
        if (!this.level().isClientSide() && target != null
                && (this.getState() == State.DORMANT || this.getState() == State.FALLING_ASLEEP)) {
            this.triggerAwakening(target);
        }
    }

    /** Initiates the transition to AWAKENING once, without re-triggering if already awakening. */
    public void triggerAwakening(@Nullable LivingEntity threat) {
        if (!this.isAlive() || (this.getState() != State.DORMANT && this.getState() != State.FALLING_ASLEEP)) {
            return;
        }
        if (this.homePos == null) {
            this.homePos = this.blockPosition();
        }
        if (threat != null) {
            this.lastKnownTargetPos = threat.position();
        }
        this.awakeningTicks = 0;
        this.entityData.set(DATA_AWAKENING_TICKS, 0);
        this.setState(State.AWAKENING);
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, EVENT_AWAKEN);
        this.playSound(ModSounds.LITHOPHAGE_AWAKEN.get(), 1.0F, 0.85F + this.random.nextFloat() * 0.2F);

        // Seismic resonance: the awakening roar sends tremors through the cave rock, awakening nearby dormant allies within 8 blocks.
        if (this.level() instanceof ServerLevel serverLevel) {
            BlockParticleOption tremorDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.DEEPSLATE.defaultBlockState());
            serverLevel.sendParticles(tremorDust, this.getX(), this.getY() + 0.1D, this.getZ(), 14, 0.7D, 0.1D, 0.7D, 0.05D);
            this.alertNearbyPackMembers(serverLevel, threat);
        }
    }

    private void alertNearbyPackMembers(ServerLevel level, @Nullable LivingEntity threat) {
        AABB resonanceBox = this.getBoundingBox().inflate(RESONANCE_RADIUS);
        List<Lithophage> pack = level.getEntitiesOfClass(Lithophage.class, resonanceBox,
                ally -> ally != this && ally.isAlive()
                        && (ally.getState() == State.DORMANT || ally.getState() == State.FALLING_ASLEEP));
        for (Lithophage ally : pack) {
            if (ally.distanceToSqr(this) <= RESONANCE_RADIUS * RESONANCE_RADIUS) {
                if (threat != null && ally.canAttack(threat)) {
                    ally.setTarget(threat);
                }
                ally.triggerAwakening(threat);
            }
        }
    }

    /** Initiates the smooth transition from VIGILANT back to DORMANT, playing the settling animation and sound. */
    public void triggerFallingAsleep() {
        if (!this.isAlive() || this.getState() != State.VIGILANT) {
            return;
        }
        this.awakeningTicks = AWAKENING_TICKS;
        this.entityData.set(DATA_AWAKENING_TICKS, this.awakeningTicks);
        this.setState(State.FALLING_ASLEEP);
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, EVENT_FALL_ASLEEP);
        this.playSound(ModSounds.LITHOPHAGE_AMBIENT.get(), 0.85F, 0.65F + this.random.nextFloat() * 0.1F);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && this.isAlive()) {
            level.broadcastEntityEvent(this, EVENT_HURT);
            LivingEntity attacker = source.getEntity() instanceof LivingEntity living ? living : null;
            if (attacker != null && this.canAttack(attacker)) {
                if (this.homePos == null) {
                    this.homePos = this.blockPosition();
                }
                this.setTarget(attacker);
                this.lastKnownTargetPos = attacker.position();
            }
            if (this.getState() == State.DORMANT || this.getState() == State.FALLING_ASLEEP) {
                this.triggerAwakening(attacker);
            }
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        this.cancelAttack();
        this.recoveryTicks = 0;
        this.setState(State.DYING);
        this.getNavigation().stop();
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.broadcastEntityEvent(this, EVENT_DEATH);
            BlockParticleOption debris = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState());
            serverLevel.sendParticles(debris, this.getX(), this.getY() + 0.8D, this.getZ(),
                    14, 0.35D, 0.45D, 0.35D, 0.08D);
        }
        super.die(source);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!this.isAlive() || this.isDeadOrDying()) {
            if (this.getState() != State.DYING) {
                this.cancelAttack();
                this.setState(State.DYING);
            }
            return;
        }
        if (this. homePos == null) {
            this.homePos = this.blockPosition();
        }
        if (this.slamCooldown > 0) this.slamCooldown--;
        if (this.sweepCooldown > 0) this.sweepCooldown--;
        if (this.shardCooldown > 0) this.shardCooldown--;
        if (this.pathRecalcTicks > 0) this.pathRecalcTicks--;

        // Detect nearby survival/adventure players when dormant or falling asleep even before targetSelector ticks.
        if ((this.getState() == State.DORMANT || this.getState() == State.FALLING_ASLEEP) && this.tickCount % 5 == 0) {
            Player threat = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), DETECTION_RANGE,
                    entity -> entity instanceof Player player
                            && player.isAlive()
                            && !player.isSpectator()
                            && !player.isCreative()
                            && this.getSensing().hasLineOfSight(player)
                            && this.isWithinTerritory(player));
            if (threat != null) {
                this.setTarget(threat);
            }
        }

        // Occasional subtle dust while dormant.
        if (this.getState() == State.DORMANT && this.tickCount % 35 == 0) {
            BlockParticleOption clayDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.CLAY.defaultBlockState());
            level.sendParticles(clayDust, this.getX(), this.getY() + 1.4D, this.getZ(), 2, 0.25D, 0.2D, 0.25D, 0.0D);
        }

        // Broadcast turn animation when rotating significantly in place while awake.
        float yawDelta = Math.abs(Mth.wrapDegrees(this.getYRot() - this.previousYRot));
        this.previousYRot = this.getYRot();
        if (yawDelta > 12.0F && this.getDeltaMovement().horizontalDistanceSqr() < 0.002D
                && (this.getState() == State.VIGILANT || this.getState() == State.COMBAT)) {
            level.broadcastEntityEvent(this, EVENT_TURN);
        }
    }

    /** Starts one of the three attacks if not already attacking or recovering. */
    public boolean startAttack(AttackType type) {
        if (type == AttackType.NONE || !this.isAlive() || this.getState() == State.DYING
                || this.getState() == State.AWAKENING || this.getState() == State.RECOVERY
                || this.getActiveAttack() != AttackType.NONE) {
            return false;
        }
        this.setState(State.COMBAT);
        this.setActiveAttack(type);
        this.attackTicks = 0;
        this.getNavigation().stop();
        byte eventId = switch (type) {
            case SLAM -> EVENT_ATTACK_SLAM;
            case SWEEP -> EVENT_ATTACK_SWEEP;
            case SHARD -> EVENT_ATTACK_SHARD;
            case NONE -> 0;
        };
        if (eventId != 0) {
            this.level().broadcastEntityEvent(this, eventId);
        }
        return true;
    }

    public void cancelAttack() {
        this.setActiveAttack(AttackType.NONE);
        this.attackTicks = 0;
    }

    private void enterRecovery(int durationTicks) {
        this.setActiveAttack(AttackType.NONE);
        this.attackTicks = 0;
        this.recoveryTicks = durationTicks;
        this.setState(State.RECOVERY);
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, EVENT_ATTACK_RECOVERY);
    }

    public boolean isWithinMeleeBox(LivingEntity target, double reach) {
        return this.getBoundingBox().inflate(reach, 0.6D, reach).intersects(target.getBoundingBox());
    }

    /** Checks whether a candidate is inside the configured frontal arc (dot product >= minDot). */
    public boolean isInFrontArc(LivingEntity candidate, double minDot) {
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYHeadRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 toTarget = candidate.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (toTarget.lengthSqr() < 1.0E-4D) {
            return true;
        }
        return forward.dot(toTarget.normalize()) >= minDot;
    }

    private void performSlamStrike(ServerLevel level, @Nullable LivingEntity target) {
        this.playSound(ModSounds.LITHOPHAGE_SLAM.get(), 1.0F, 0.85F + this.random.nextFloat() * 0.15F);
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 impactPos = this.position().add(forward.scale(1.2D));
        BlockParticleOption fragments = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState());
        level.sendParticles(fragments, impactPos.x, this.getY() + 0.2D, impactPos.z, 10, 0.35D, 0.15D, 0.35D, 0.08D);

        if (target != null && target.isAlive() && this.canAttack(target)
                && this.isWithinMeleeBox(target, SLAM_STRIKE_REACH)
                && this.isInFrontArc(target, 0.05D)
                && this.getSensing().hasLineOfSight(target)) {
            target.hurtServer(level, this.damageSources().mobAttack(this), SLAM_DAMAGE);
            target.knockback(SLAM_KNOCKBACK, -forward.x, -forward.z);
        }
    }

    private void performSweepStrike(ServerLevel level) {
        this.playSound(ModSounds.LITHOPHAGE_SWEEP.get(), 1.0F, 0.8F + this.random.nextFloat() * 0.15F);
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 arcCenter = this.position().add(forward.scale(1.4D)).add(0.0D, 0.9D, 0.0D);
        BlockParticleOption mudFragments = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_MUD.defaultBlockState());
        level.sendParticles(mudFragments, arcCenter.x, arcCenter.y, arcCenter.z, 10, 0.6D, 0.2D, 0.6D, 0.06D);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, arcCenter.x, arcCenter.y, arcCenter.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);

        AABB searchBox = this.getBoundingBox().inflate(SWEEP_STRIKE_REACH, 0.6D, SWEEP_STRIKE_REACH);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity != this && entity.isAlive() && this.canAttack(entity));
        for (LivingEntity candidate : candidates) {
            if (this.isWithinMeleeBox(candidate, SWEEP_STRIKE_REACH)
                    && this.isInFrontArc(candidate, SWEEP_ARC_MIN_DOT)
                    && this.getSensing().hasLineOfSight(candidate)) {
                candidate.hurtServer(level, this.damageSources().mobAttack(this), SWEEP_DAMAGE);
                Vec3 push = candidate.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
                if (push.lengthSqr() < 1.0E-4D) {
                    push = forward;
                }
                candidate.knockback(SWEEP_KNOCKBACK, -push.x, -push.z);
            }
        }
    }

    private void performShardShot(ServerLevel level, @Nullable LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return;
        }
        AmberShard shard = new AmberShard(level, this);
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 spawnPos = new Vec3(this.getX() + forward.x * 0.6D, this.getEyeY() - 0.25D, this.getZ() + forward.z * 0.6D);
        shard.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

        Vec3 targetCenter = new Vec3(target.getX(), target.getY(0.5D), target.getZ());
        Vec3 delta = targetCenter.subtract(spawnPos);
        double horizontalDist = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        shard.shoot(delta.x, delta.y + horizontalDist * 0.04D, delta.z, SHARD_SPEED, 0.2F);
        level.addFreshEntity(shard);

        this.playSound(ModSounds.LITHOPHAGE_SHARD_SHOOT.get(), 0.95F, 0.9F + this.random.nextFloat() * 0.2F);
        level.sendParticles(AmberShard.AMBER_DUST, spawnPos.x, spawnPos.y, spawnPos.z, 6, 0.12D, 0.12D, 0.12D, 0.01D);
    }

    /** Primary AI state machine goal driving Estados A through F. */
    private final class LithophageBrainGoal extends Goal {
        LithophageBrainGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return Lithophage.this.isAlive() && Lithophage.this.getState() != State.DYING;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            Lithophage mob = Lithophage.this;
            if (!(mob.level() instanceof ServerLevel level)) {
                return;
            }
            LivingEntity target = mob.getTarget();
            if (target != null && (!target.isAlive() || !mob.canAttack(target) || !mob.isWithinTerritory(target))) {
                mob.setTarget(null);
                target = null;
            }

            switch (mob.getState()) {
                case DORMANT -> this.tickDormant(mob, target);
                case AWAKENING -> this.tickAwakening(mob, target);
                case VIGILANT -> this.tickVigilant(level, mob, target);
                case PURSUING -> this.tickPursuing(level, mob, target);
                case COMBAT -> this.tickCombat(level, mob, target);
                case RECOVERY -> this.tickRecovery(mob, target);
                case FALLING_ASLEEP -> this.tickFallingAsleep(mob, target);
                case DYING -> mob.getNavigation().stop();
            }
        }

        private void tickDormant(Lithophage mob, @Nullable LivingEntity target) {
            mob.getNavigation().stop();
            if (target != null && mob.distanceToSqr(target) <= DETECTION_RANGE * DETECTION_RANGE) {
                mob.triggerAwakening(target);
            }
        }

        private void tickAwakening(Lithophage mob, @Nullable LivingEntity target) {
            mob.getNavigation().stop();
            if (target != null) {
                mob.getLookControl().setLookAt(target, 25.0F, 25.0F);
                mob.lastKnownTargetPos = target.position();
            }
            mob.awakeningTicks++;
            mob.entityData.set(DATA_AWAKENING_TICKS, mob.awakeningTicks);
            if (mob.awakeningTicks >= AWAKENING_TICKS) {
                mob.vigilantAssessTicks = 0;
                mob.vigilantIdleTicks = 0;
                mob.setState(State.VIGILANT);
                mob.level().broadcastEntityEvent(mob, EVENT_ALERT);
            }
        }

        private void tickVigilant(ServerLevel level, Lithophage mob, @Nullable LivingEntity target) {
            if (target != null) {
                mob.vigilantIdleTicks = 0;
                mob.lastKnownTargetPos = target.position();
                mob.getLookControl().setLookAt(target, 35.0F, 35.0F);
                mob.vigilantAssessTicks++;
                if (mob.vigilantAssessTicks >= VIGILANT_ASSESS_TICKS) {
                    if (!this.tryInitiateAttack(mob, target)) {
                        mob.setState(State.PURSUING);
                        mob.pathRecalcTicks = 0;
                    }
                }
                return;
            }

            // No active target: investigate last known position if valid, then return toward home and rest.
            mob.vigilantIdleTicks++;
            if (mob.lastKnownTargetPos != null && mob.isPosWithinTerritory(mob.lastKnownTargetPos)) {
                double distSqr = mob.position().distanceToSqr(mob.lastKnownTargetPos);
                mob.getLookControl().setLookAt(mob.lastKnownTargetPos.x, mob.lastKnownTargetPos.y + 1.0D, mob.lastKnownTargetPos.z, 25.0F, 25.0F);
                if (distSqr > 3.0D && mob.vigilantIdleTicks < VIGILANT_TIMEOUT_TICKS / 2) {
                    if (mob.pathRecalcTicks <= 0 || mob.getNavigation().isDone()) {
                        mob.getNavigation().moveTo(mob.lastKnownTargetPos.x, mob.lastKnownTargetPos.y, mob.lastKnownTargetPos.z, 0.85D);
                        mob.pathRecalcTicks = 15;
                    }
                } else {
                    mob.lastKnownTargetPos = null;
                    mob.getNavigation().stop();
                }
            } else if (mob.homePos != null && mob.blockPosition().distSqr(mob.homePos) > 6.0D) {
                if (mob.pathRecalcTicks <= 0 || mob.getNavigation().isDone()) {
                    mob.getNavigation().moveTo(mob.homePos.getX() + 0.5D, mob.homePos.getY(), mob.homePos.getZ() + 0.5D, 0.85D);
                    mob.pathRecalcTicks = 15;
                }
            } else {
                mob.getNavigation().stop();
            }

            if (mob.vigilantIdleTicks >= VIGILANT_TIMEOUT_TICKS) {
                mob.getNavigation().stop();
                mob.lastKnownTargetPos = null;
                mob.triggerFallingAsleep();
            }
        }

        private void tickFallingAsleep(Lithophage mob, @Nullable LivingEntity target) {
            mob.getNavigation().stop();
            if (target != null && mob.distanceToSqr(target) <= DETECTION_RANGE * DETECTION_RANGE) {
                mob.triggerAwakening(target);
                return;
            }
            mob.awakeningTicks--;
            mob.entityData.set(DATA_AWAKENING_TICKS, Math.max(0, mob.awakeningTicks));
            if (mob.awakeningTicks % 7 == 0 && mob.level() instanceof ServerLevel serverLevel) {
                BlockParticleOption clayDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.CLAY.defaultBlockState());
                serverLevel.sendParticles(clayDust, mob.getX(), mob.getY() + 0.3D, mob.getZ(), 2, 0.25D, 0.1D, 0.25D, 0.0D);
            }
            if (mob.awakeningTicks <= 0) {
                mob.setState(State.DORMANT);
            }
        }

        private void tickPursuing(ServerLevel level, Lithophage mob, @Nullable LivingEntity target) {
            if (target == null) {
                mob.getNavigation().stop();
                mob.vigilantAssessTicks = 0;
                mob.vigilantIdleTicks = 0;
                mob.setState(State.VIGILANT);
                return;
            }
            mob.lastKnownTargetPos = target.position();
            mob.getLookControl().setLookAt(target, 35.0F, 35.0F);

            if (this.tryInitiateAttack(mob, target)) {
                return;
            }

            if (mob.pathRecalcTicks <= 0 || mob.getNavigation().isDone()) {
                mob.getNavigation().moveTo(target, 1.0D);
                mob.pathRecalcTicks = 12;
            }
        }

        private void tickCombat(ServerLevel level, Lithophage mob, @Nullable LivingEntity target) {
            mob.getNavigation().stop();
            AttackType attack = mob.getActiveAttack();
            if (attack == AttackType.NONE || target == null) {
                mob.cancelAttack();
                mob.vigilantAssessTicks = 0;
                mob.vigilantIdleTicks = 0;
                mob.setState(State.VIGILANT);
                return;
            }

            mob.lastKnownTargetPos = target.position();
            mob.getLookControl().setLookAt(target, 45.0F, 45.0F);
            mob.attackTicks++;

            switch (attack) {
                case SLAM -> {
                    if (mob.attackTicks == SLAM_WINDUP_TICKS) {
                        mob.performSlamStrike(level, target);
                    }
                    if (mob.attackTicks >= SLAM_TOTAL_TICKS) {
                        mob.slamCooldown = 22;
                        mob.lastMeleeWasSlam = true;
                        mob.enterRecovery(SLAM_RECOVERY_TICKS);
                    }
                }
                case SWEEP -> {
                    if (mob.attackTicks == SWEEP_WINDUP_TICKS) {
                        mob.performSweepStrike(level);
                    }
                    if (mob.attackTicks >= SWEEP_TOTAL_TICKS) {
                        mob.sweepCooldown = 28;
                        mob.lastMeleeWasSlam = false;
                        mob.enterRecovery(SWEEP_RECOVERY_TICKS);
                    }
                }
                case SHARD -> {
                    if (mob.attackTicks < SHARD_WINDUP_TICKS && mob.attackTicks % 3 == 0) {
                        Vec3 forward = Vec3.directionFromRotation(0.0F, mob.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
                        level.sendParticles(AmberShard.AMBER_DUST,
                                mob.getX() + forward.x * 0.55D,
                                mob.getEyeY() - 0.2D,
                                mob.getZ() + forward.z * 0.55D,
                                3, 0.1D, 0.1D, 0.1D, 0.005D);
                    }
                    if (mob.attackTicks == SHARD_WINDUP_TICKS) {
                        mob.performShardShot(level, target);
                    }
                    if (mob.attackTicks >= SHARD_TOTAL_TICKS) {
                        mob.shardCooldown = SHARD_MIN_COOLDOWN_TICKS
                                + mob.random.nextInt(SHARD_MAX_COOLDOWN_TICKS - SHARD_MIN_COOLDOWN_TICKS + 1);
                        mob.enterRecovery(SHARD_RECOVERY_TICKS);
                    }
                }
                case NONE -> {}
            }
        }

        private void tickRecovery(Lithophage mob, @Nullable LivingEntity target) {
            mob.getNavigation().stop();
            if (target != null) {
                mob.getLookControl().setLookAt(target, 20.0F, 20.0F);
                mob.lastKnownTargetPos = target.position();
            }
            if (--mob.recoveryTicks <= 0) {
                if (target != null) {
                    mob.setState(State.PURSUING);
                    mob.pathRecalcTicks = 0;
                } else {
                    mob.vigilantAssessTicks = 0;
                    mob.vigilantIdleTicks = 0;
                    mob.setState(State.VIGILANT);
                }
            }
        }

        private boolean tryInitiateAttack(Lithophage mob, LivingEntity target) {
            if (!mob.getSensing().hasLineOfSight(target)) {
                return false;
            }
            boolean inSlamReach = mob.isWithinMeleeBox(target, SLAM_START_REACH) && mob.isInFrontArc(target, 0.2D);
            boolean inSweepReach = mob.isWithinMeleeBox(target, SWEEP_START_REACH) && mob.isInFrontArc(target, SWEEP_ARC_MIN_DOT);

            if (inSlamReach || inSweepReach) {
                if (mob.lastMeleeWasSlam && inSweepReach && mob.sweepCooldown <= 0) {
                    return mob.startAttack(AttackType.SWEEP);
                }
                if (inSlamReach && mob.slamCooldown <= 0) {
                    return mob.startAttack(AttackType.SLAM);
                }
                if (inSweepReach && mob.sweepCooldown <= 0) {
                    return mob.startAttack(AttackType.SWEEP);
                }
            }

            double distSqr = mob.distanceToSqr(target);
            if (mob.shardCooldown <= 0
                    && distSqr >= SHARD_MIN_DISTANCE * SHARD_MIN_DISTANCE
                    && distSqr <= SHARD_MAX_DISTANCE * SHARD_MAX_DISTANCE) {
                return mob.startAttack(AttackType.SHARD);
            }
            return false;
        }
    }

    // ---------- client animation controller ----------

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_AWAKEN -> {
                this.stopActionAnimations();
                this.fallAsleepAnimationState.stop();
                this.awakeningAnimationState.start(this.tickCount);
            }
            case EVENT_ALERT -> {
                this.awakeningAnimationState.stop();
                this.fallAsleepAnimationState.stop();
                this.alertAnimationState.start(this.tickCount);
            }
            case EVENT_FALL_ASLEEP -> {
                this.stopActionAnimations();
                this.awakeningAnimationState.stop();
                this.fallAsleepAnimationState.start(this.tickCount);
            }
            case EVENT_TURN -> {
                if (!this.turnAnimationState.isStarted()) {
                    this.turnAnimationState.start(this.tickCount);
                }
            }
            case EVENT_ATTACK_SLAM -> {
                this.stopActionAnimations();
                this.attackSlamAnimationState.start(this.tickCount);
            }
            case EVENT_ATTACK_SWEEP -> {
                this.stopActionAnimations();
                this.attackSweepAnimationState.start(this.tickCount);
            }
            case EVENT_ATTACK_SHARD -> {
                this.stopActionAnimations();
                this.attackShardAnimationState.start(this.tickCount);
            }
            case EVENT_ATTACK_RECOVERY -> {
                this.attackSlamAnimationState.stop();
                this.attackSweepAnimationState.stop();
                this.attackShardAnimationState.stop();
                this.attackRecoveryAnimationState.start(this.tickCount);
            }
            case EVENT_HURT -> this.hurtAnimationState.start(this.tickCount);
            case EVENT_DEATH -> {
                this.stopActionAnimations();
                this.hurtAnimationState.stop();
                this.deathAnimationState.start(this.tickCount);
            }
            default -> super.handleEntityEvent(id);
        }
    }

    private void stopActionAnimations() {
        this.idleAlternateAnimationState.stop();
        this.alertAnimationState.stop();
        this.turnAnimationState.stop();
        this.attackSlamAnimationState.stop();
        this.attackSweepAnimationState.stop();
        this.attackShardAnimationState.stop();
        this.attackRecoveryAnimationState.stop();
        this.fallAsleepAnimationState.stop();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.tickClientAnimations();
        }
    }

    private void tickClientAnimations() {
        State current = this.getState();
        if (current == State.DYING || this.isDeadOrDying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.deathAnimationState.startIfStopped(this.tickCount);
            return;
        }

        this.stopWhenOver(this.awakeningAnimationState, AWAKENING_TICKS);
        this.stopWhenOver(this.fallAsleepAnimationState, FALL_ASLEEP_TICKS);
        this.stopWhenOver(this.alertAnimationState, 16);
        this.stopWhenOver(this.turnAnimationState, 12);
        this.stopWhenOver(this.attackSlamAnimationState, SLAM_TOTAL_TICKS);
        this.stopWhenOver(this.attackSweepAnimationState, SWEEP_TOTAL_TICKS);
        this.stopWhenOver(this.attackShardAnimationState, SHARD_TOTAL_TICKS);
        this.stopWhenOver(this.attackRecoveryAnimationState, 18);
        this.stopWhenOver(this.hurtAnimationState, 10);

        boolean moving = this.walkAnimation.speed() > 0.05F && current == State.PURSUING;
        if (moving) {
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
        }

        this.idleAnimationState.startIfStopped(this.tickCount);

        boolean calm = (current == State.DORMANT || current == State.VIGILANT) && !moving
                && !this.awakeningAnimationState.isStarted() && !this.alertAnimationState.isStarted()
                && !this.fallAsleepAnimationState.isStarted();
        if (this.idleAlternateTicksLeft > 0) {
            if (!calm || --this.idleAlternateTicksLeft == 0) {
                this.idleAlternateAnimationState.stop();
                this.idleAlternateTicksLeft = 0;
            }
        } else if (calm && --this.idleAlternateCooldown <= 0) {
            this.idleAlternateAnimationState.start(this.tickCount);
            this.idleAlternateTicksLeft = 40;
            this.idleAlternateCooldown = 100 + this.random.nextInt(120);
        }
    }

    private void stopWhenOver(AnimationState state, int lengthTicks) {
        if (state.isStarted() && state.getTimeInMillis(this.tickCount) > lengthTicks * 50L) {
            state.stop();
        }
    }

    // ---------- natural spawn rules ----------

    public static boolean checkSpawnRules(EntityType<Lithophage> type, ServerLevelAccessor level,
                                          EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        int y = pos.getY();
        if (y < MIN_SPAWN_Y || y > MAX_SPAWN_Y) {
            return false;
        }
        var biome = level.getBiome(pos);
        if (!biome.is(Biomes.LUSH_CAVES) && !biome.is(Biomes.DRIPSTONE_CAVES)) {
            return false;
        }
        if (level.canSeeSky(pos) || level.getMaxLocalRawBrightness(pos) > 7) {
            return false;
        }
        BlockPos below = pos.below();
        if (!level.getBlockState(below).isValidSpawn(level, below, type)) {
            return false;
        }
        AABB spawnBox = type.getSpawnAABB(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        return level.noCollision(spawnBox) && Mob.checkMobSpawnRules(type, level, reason, pos, random);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 3;
    }

    // ---------- sounds ----------

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        // Silent while dormant; low mineral creak when awake.
        return this.getState() == State.DORMANT ? null : ModSounds.LITHOPHAGE_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.LITHOPHAGE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.LITHOPHAGE_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.LITHOPHAGE_STEP.get(), 0.75F, 0.85F + this.random.nextFloat() * 0.15F);
        if (this.level() instanceof ServerLevel serverLevel && this.tickCount % 4 == 0) {
            BlockParticleOption stepDust = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState());
            serverLevel.sendParticles(stepDust, this.getX(), this.getY() + 0.05D, this.getZ(), 2, 0.2D, 0.02D, 0.2D, 0.02D);
        }
    }

    // ---------- persistence ----------

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("LithophageState", this.getState().name());
        output.putInt("AwakeningTicks", this.awakeningTicks);
        output.putInt("ShardCooldown", this.shardCooldown);
        output.putInt("VigilantIdleTicks", this.vigilantIdleTicks);
        if (this.homePos != null) {
            output.putInt("HomeX", this.homePos.getX());
            output.putInt("HomeY", this.homePos.getY());
            output.putInt("HomeZ", this.homePos.getZ());
            output.putBoolean("HasHome", true);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String savedState = input.getStringOr("LithophageState", State.DORMANT.name());
        try {
            State parsed = State.valueOf(savedState);
            if (parsed == State.COMBAT || parsed == State.RECOVERY) {
                parsed = State.VIGILANT;
            }
            this.setState(parsed);
        } catch (IllegalArgumentException ignored) {
            this.setState(State.DORMANT);
        }
        this.awakeningTicks = input.getIntOr("AwakeningTicks", 0);
        this.entityData.set(DATA_AWAKENING_TICKS, this.awakeningTicks);
        this.shardCooldown = input.getIntOr("ShardCooldown", 0);
        this.vigilantIdleTicks = input.getIntOr("VigilantIdleTicks", 0);
        if (input.getBooleanOr("HasHome", false)) {
            this.homePos = new BlockPos(
                    input.getIntOr("HomeX", this.getBlockX()),
                    input.getIntOr("HomeY", this.getBlockY()),
                    input.getIntOr("HomeZ", this.getBlockZ()));
        }
    }
}

