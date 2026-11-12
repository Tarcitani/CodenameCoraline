package com.codenamecoraline.entity.reaper;

import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Omen Hare ({@code codenamecoraline:omen_hare}): albino ruby-eyed herald that appears naturally during heavy
 * snowstorms, approaches the player fearlessly, stares in bipedal posture, and triggers the Soul Reaper's awakening
 * metamorphosis when slain. Spec: {@code docs/design/soul_reaper.md} §1.2, {@code soul_reaper_animation.md} §3.
 */
public final class OmenHare extends PathfinderMob {
    public static final float WIDTH = 0.5F;
    public static final float HEIGHT = 0.6F;
    public static final float EYE_HEIGHT = 0.45F;

    public static final double MAX_HEALTH = 10.0D;
    public static final double MOVEMENT_SPEED = 0.28D;
    public static final double FOLLOW_RANGE = 32.0D;
    public static final double EXCLUSION_RADIUS = 128.0D;
    public static final double STARE_DISTANCE = 3.8D;
    public static final int RUPTURE_DURATION_TICKS = 40;

    private static final String TAG_STATE = "HareState";
    private static final String TAG_RUPTURE_TICKS = "RuptureTicks";
    private static final String TAG_NATURAL_STORM = "NaturalStormSpawn";

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(OmenHare.class, EntityDataSerializers.INT);

    public enum State {
        APPROACHING,
        STANDING_STARE,
        TILT_TAUNT,
        RUPTURING
    }

    public final AnimationState standStareAnimState = new AnimationState();
    public final AnimationState tiltTauntAnimState = new AnimationState();
    public final AnimationState ruptureAnimState = new AnimationState();

    private int stateTicks;
    private int ruptureTicks;
    private boolean naturalStormSpawn;

    public OmenHare(EntityType<? extends OmenHare> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.standStareAnimState.start(0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, State.APPROACHING.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, (float) FOLLOW_RANGE, 1.0F));
    }

    public State getState() {
        int idx = this.entityData.get(DATA_STATE);
        State[] values = State.values();
        return idx >= 0 && idx < values.length ? values[idx] : State.APPROACHING;
    }

    public void setState(State state) {
        if (this.getState() != state) {
            this.stateTicks = 0;
        }
        this.entityData.set(DATA_STATE, state.ordinal());
    }

    public int getRuptureTicks() {
        return this.ruptureTicks;
    }

    public void setNaturalStormSpawn(boolean naturalStormSpawn) {
        this.naturalStormSpawn = naturalStormSpawn;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (!this.level().isClientSide() || !DATA_STATE.equals(accessor)) {
            return;
        }
        this.standStareAnimState.stop();
        this.tiltTauntAnimState.stop();
        this.ruptureAnimState.stop();
        switch (this.getState()) {
            case STANDING_STARE -> this.standStareAnimState.start(this.tickCount);
            case TILT_TAUNT -> this.tiltTauntAnimState.start(this.tickCount);
            case RUPTURING -> this.ruptureAnimState.start(this.tickCount);
            default -> {}
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
            return;
        }
        ServerLevel serverLevel = (ServerLevel) this.level();
        this.stateTicks++;

        if (this.getState() == State.RUPTURING) {
            this.getNavigation().stop();
            this.setNoGravity(true);
            this.setDeltaMovement(0.0D, 0.03D, 0.0D);
            serverLevel.sendParticles(
                    ParticleTypes.LARGE_SMOKE,
                    this.getX(),
                    this.getY() + 0.3D,
                    this.getZ(),
                    3,
                    0.18D,
                    0.18D,
                    0.18D,
                    0.01D);
            this.ruptureTicks++;
            if (this.ruptureTicks >= RUPTURE_DURATION_TICKS) {
                this.completeMetamorphosis(serverLevel);
            }
            return;
        }

        if (this.naturalStormSpawn && (!serverLevel.isRaining() || !serverLevel.isThundering())) {
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    this.getX(),
                    this.getY() + 0.3D,
                    this.getZ(),
                    20,
                    0.25D,
                    0.25D,
                    0.25D,
                    0.02D);
            this.discard();
            return;
        }

        if (this.isNoAi()) {
            return;
        }

        Player nearest = serverLevel.getNearestPlayer(this, FOLLOW_RANGE);
        if (nearest == null || !nearest.isAlive() || nearest.isSpectator()) {
            if (this.getState() != State.APPROACHING) {
                this.setState(State.APPROACHING);
            }
            return;
        }

        this.getLookControl().setLookAt(nearest, 35.0F, 35.0F);
        double distSqr = this.distanceToSqr(nearest);
        if (distSqr > (STARE_DISTANCE + 1.0D) * (STARE_DISTANCE + 1.0D)) {
            if (this.getState() != State.APPROACHING) {
                this.setState(State.APPROACHING);
            }
            if (this.tickCount % 10 == 0) {
                this.getNavigation().moveTo(nearest, 1.0D);
            }
        } else {
            this.getNavigation().stop();
            if (this.getState() == State.APPROACHING) {
                this.setState(State.STANDING_STARE);
            } else if (this.stateTicks >= 60) {
                this.setState(this.getState() == State.STANDING_STARE ? State.TILT_TAUNT : State.STANDING_STARE);
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.FREEZE) || source.is(DamageTypes.FALL) || source.is(DamageTypes.DROWN)) {
            return false;
        }
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.GENERIC_KILL)) {
            return super.hurtServer(level, source, amount);
        }
        if (this.getState() == State.RUPTURING) {
            return false;
        }
        if (amount >= this.getHealth()) {
            this.triggerMetamorphosis(level);
            return true;
        }
        return super.hurtServer(level, source, amount);
    }

    /** Starts the 40-tick rupture metamorphosis that culminates in spawning the Soul Reaper. */
    public void triggerMetamorphosis(ServerLevel level) {
        if (this.getState() == State.RUPTURING) {
            return;
        }
        this.setHealth(1.0F);
        this.ruptureTicks = 0;
        this.setState(State.RUPTURING);
        this.setNoGravity(true);
        level.playSound(
                null,
                this.blockPosition(),
                ModSounds.OMEN_HARE_RUPTURE.get(),
                SoundSource.HOSTILE,
                1.1F,
                0.85F);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.OMEN_HARE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.OMEN_HARE_HURT.get();
    }

    /** Spawns the Soul Reaper in AWAKENING state at the hare's position and removes the hare. */
    public @Nullable SoulReaper completeMetamorphosis(ServerLevel level) {
        AABB search = AABB.ofSize(this.position(), 16.0D, 16.0D, 16.0D);
        if (!level.getEntitiesOfClass(SoulReaper.class, search, SoulReaper::isAlive).isEmpty()) {
            this.discard();
            return null;
        }
        SoulReaper reaper = ModEntities.SOUL_REAPER.get().create(level, EntitySpawnReason.TRIGGERED);
        if (reaper != null) {
            Vec3 pos = this.position();
            reaper.snapTo(pos.x, Math.max(level.getMinY() + 1, pos.y - 1.0D), pos.z, this.getYRot(), 0.0F);
            reaper.setAnchor(BlockPos.containing(pos));
            reaper.beginAwakening();
            level.addFreshEntity(reaper);
            level.broadcastEntityEvent(reaper, SoulReaper.EVENT_AWAKEN_START);
        }
        this.discard();
        return reaper;
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    /**
     * Natural spawn rule: requires non-peaceful difficulty, valid snowy/ground block below, active snowstorm
     * (raining + thundering) when spawned naturally, and at most 1 Omen Hare and 0 Soul Reapers within 128 blocks.
     */
    public static boolean checkSpawnRules(
            EntityType<OmenHare> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        boolean validGround = below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || below.is(BlockTags.SNOW)
                || below.is(Blocks.SNOW_BLOCK)
                || below.is(Blocks.POWDER_SNOW)
                || below.is(Blocks.ICE)
                || below.is(Blocks.PACKED_ICE);
        if (!validGround) {
            return false;
        }
        ServerLevel serverLevel = level.getLevel();
        if (reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION) {
            if (!serverLevel.isRaining() || !serverLevel.isThundering()) {
                return false;
            }
        }
        AABB searchBox = AABB.ofSize(pos.getCenter(), EXCLUSION_RADIUS * 2.0D, 64.0D, EXCLUSION_RADIUS * 2.0D);
        if (!serverLevel.getEntitiesOfClass(OmenHare.class, searchBox).isEmpty()) {
            return false;
        }
        return serverLevel.getEntitiesOfClass(SoulReaper.class, searchBox).isEmpty();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString(TAG_STATE, this.getState().name());
        output.putInt(TAG_RUPTURE_TICKS, this.ruptureTicks);
        output.putBoolean(TAG_NATURAL_STORM, this.naturalStormSpawn);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String name = input.getStringOr(TAG_STATE, State.APPROACHING.name());
        try {
            this.setState(State.valueOf(name));
        } catch (IllegalArgumentException ex) {
            this.setState(State.APPROACHING);
        }
        this.ruptureTicks = input.getIntOr(TAG_RUPTURE_TICKS, 0);
        this.naturalStormSpawn = input.getBooleanOr(TAG_NATURAL_STORM, false);
    }
}
