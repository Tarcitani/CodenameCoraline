package com.codenamecoraline.entity.salamander;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * Brimstone Salamander ({@code codenamecoraline:brimstone_salamander}): a predatory, magma-dwelling
 * amphibian of traditional fey alchemy lore. Native to Nether Basalt Deltas, Nether Wastes, and Crimson Forest shores.
 * Slice 1 & 2 provide the foundation entity, attributes, state enum, spawn rules, and client animation states.
 */
public final class BrimstoneSalamander extends Monster {
    public static final float WIDTH = 1.3F;
    public static final float HEIGHT = 0.85F;
    public static final float EYE_HEIGHT = 0.55F;

    public static final int MIN_SPAWN_Y = 24;
    public static final int MAX_SPAWN_Y = 48;

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
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 44.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.65D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, State.IDLE.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public State getState() {
        int idx = this.entityData.get(DATA_STATE);
        State[] values = State.values();
        return idx >= 0 && idx < values.length ? values[idx] : State.IDLE;
    }

    public void setState(State state) {
        this.entityData.set(DATA_STATE, state.ordinal());
    }

    public boolean isSwimmingInLava() {
        return this.isInLava() || this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA);
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
        } else if (this.isAlive() && this.getState() == State.IDLE && this.getTarget() == null) {
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

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_THROAT_PUFF -> {
                this.idleTailSwayAnimationState.stop();
                this.idleSniffAnimationState.stop();
                this.idleThroatPuffAnimationState.start(this.tickCount);
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
                this.snapBitePrepAnimationState.start(this.tickCount);
            }
            case EVENT_SULFUR_SPIT -> {
                this.stopAllAttackAnimations();
                this.sulfurSpitPrepAnimationState.start(this.tickCount);
            }
            case EVENT_TAIL_WHIP -> {
                this.stopAllAttackAnimations();
                this.tailWhipPrepAnimationState.start(this.tickCount);
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
}
