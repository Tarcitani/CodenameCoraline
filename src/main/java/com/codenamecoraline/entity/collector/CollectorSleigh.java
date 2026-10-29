package com.codenamecoraline.entity.collector;

import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Temporary non-combat visual actor representing the supernatural winter sleigh that crosses the sky
 * during Phases 1-3 of the Collector of the Last Gift's intro cinematic (Sections 8.2-8.4 & 13).
 *
 * <p>Has no combat AI, deals no damage, drops no loot, cannot be saved persistently to disk, and is
 * guaranteed to despawn once its sky pass concludes or if its linked boss is removed.</p>
 */
public class CollectorSleigh extends Entity {
    public static final int MAX_LIFETIME_TICKS = 80;

    private static final EntityDataAccessor<Float> DATA_ORIGIN_X =
            SynchedEntityData.defineId(CollectorSleigh.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ORIGIN_Y =
            SynchedEntityData.defineId(CollectorSleigh.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ORIGIN_Z =
            SynchedEntityData.defineId(CollectorSleigh.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_VEL_X =
            SynchedEntityData.defineId(CollectorSleigh.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_VEL_Z =
            SynchedEntityData.defineId(CollectorSleigh.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_FLIGHT_TICKS =
            SynchedEntityData.defineId(CollectorSleigh.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_RIDER_ABOARD =
            SynchedEntityData.defineId(CollectorSleigh.class, EntityDataSerializers.BOOLEAN);

    private @Nullable UUID ownerBossUuid;
    private int ownerBossNetworkId = -1;

    public CollectorSleigh(EntityType<? extends CollectorSleigh> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    public CollectorSleigh(ServerLevel level, LastGiftCollector boss, Vec3 startPos, Vec3 velocity, float yaw) {
        this(ModEntities.COLLECTOR_SLEIGH.get(), level);
        this.ownerBossUuid = boss.getUUID();
        this.ownerBossNetworkId = boss.getId();
        this.setPos(startPos.x, startPos.y, startPos.z);
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.entityData.set(DATA_ORIGIN_X, (float) startPos.x);
        this.entityData.set(DATA_ORIGIN_Y, (float) startPos.y);
        this.entityData.set(DATA_ORIGIN_Z, (float) startPos.z);
        this.entityData.set(DATA_VEL_X, (float) velocity.x);
        this.entityData.set(DATA_VEL_Z, (float) velocity.z);
        this.entityData.set(DATA_FLIGHT_TICKS, 0);
        this.entityData.set(DATA_RIDER_ABOARD, true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ORIGIN_X, 0.0F);
        builder.define(DATA_ORIGIN_Y, 80.0F);
        builder.define(DATA_ORIGIN_Z, 0.0F);
        builder.define(DATA_VEL_X, 0.55F);
        builder.define(DATA_VEL_Z, 0.0F);
        builder.define(DATA_FLIGHT_TICKS, 0);
        builder.define(DATA_RIDER_ABOARD, true);
    }

    public int getFlightTicks() {
        return this.entityData.get(DATA_FLIGHT_TICKS);
    }

    public float getFlightProgress(float partialTick) {
        return Mth.clamp((this.getFlightTicks() + partialTick) / (float) MAX_LIFETIME_TICKS, 0.0F, 1.0F);
    }

    public boolean isRiderAboard() {
        return this.entityData.get(DATA_RIDER_ABOARD);
    }

    public void setRiderAboard(boolean aboard) {
        this.entityData.set(DATA_RIDER_ABOARD, aboard);
    }

    /** Deterministic sky trajectory position at a given flight tick (including partial ticks). */
    public Vec3 computeSkyPosition(float flightTick) {
        double ox = this.entityData.get(DATA_ORIGIN_X);
        double oy = this.entityData.get(DATA_ORIGIN_Y);
        double oz = this.entityData.get(DATA_ORIGIN_Z);
        double vx = this.entityData.get(DATA_VEL_X);
        double vz = this.entityData.get(DATA_VEL_Z);
        double bob = Mth.sin(flightTick * 0.18F) * 0.22D;
        return new Vec3(ox + vx * flightTick, oy + bob, oz + vz * flightTick);
    }

    @Override
    public void tick() {
        super.tick();
        int ticks = this.getFlightTicks() + 1;
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_FLIGHT_TICKS, ticks);
        }

        Vec3 nextPos = this.computeSkyPosition(ticks);
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.setPos(nextPos.x, nextPos.y, nextPos.z);

        float vx = this.entityData.get(DATA_VEL_X);
        float vz = this.entityData.get(DATA_VEL_Z);
        if (vx * vx + vz * vz > 1.0E-4F) {
            float yaw = (float) (Mth.atan2(-vx, vz) * Mth.RAD_TO_DEG);
            this.setYRot(yaw);
            this.yRotO = yaw;
        }

        if (this.level().isClientSide()) {
            // Frost wake and spectral lantern trail.
            for (int i = 0; i < 3; i++) {
                double ox = (this.random.nextDouble() - 0.5D) * 2.2D;
                double oy = (this.random.nextDouble() - 0.5D) * 0.6D;
                double oz = (this.random.nextDouble() - 0.5D) * 2.2D;
                this.level().addParticle(ParticleTypes.SNOWFLAKE,
                        this.getX() + ox, this.getY() + 0.4D + oy, this.getZ() + oz,
                        -vx * 0.15D, -0.02D, -vz * 0.15D);
            }
            if (ticks % 2 == 0) {
                this.level().addParticle(ParticleTypes.WHITE_ASH,
                        this.getX(), this.getY() + 0.6D, this.getZ(), 0.0D, -0.01D, 0.0D);
            }
        } else if (this.level() instanceof ServerLevel serverLevel) {
            if (ticks % 12 == 0) {
                this.playSound(ModSounds.COLLECTOR_SLEIGH_BELLS.get(), 1.6F, 0.85F + this.random.nextFloat() * 0.2F);
            }
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY() + 0.3D, this.getZ(), 4, 1.1D, 0.25D, 1.1D, 0.01D);

            if (ticks >= MAX_LIFETIME_TICKS || !this.isOwnerValid(serverLevel)) {
                this.discard();
            }
        }
    }

    private boolean isOwnerValid(ServerLevel level) {
        if (this.ownerBossNetworkId != -1) {
            Entity byId = level.getEntity(this.ownerBossNetworkId);
            if (byId instanceof LastGiftCollector collector) {
                return collector.isAlive() && collector.getBossState() == LastGiftCollector.BossState.INTRO_CINEMATIC;
            }
        }
        if (this.ownerBossUuid != null) {
            Entity byUuid = level.getEntity(this.ownerBossUuid);
            if (byUuid instanceof LastGiftCollector collector) {
                return collector.isAlive() && collector.getBossState() == LastGiftCollector.BossState.INTRO_CINEMATIC;
            }
        }
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        // Temporary cinematic actor: never persist to disk.
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
