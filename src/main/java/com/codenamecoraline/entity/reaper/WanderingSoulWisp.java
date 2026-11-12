package com.codenamecoraline.entity.reaper;

import com.codenamecoraline.entity.PlayerSummon;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Wandering Soul Wisp ({@code codenamecoraline:wandering_soul_wisp}): ethereal 6-HP soul orb summoned in Stage 2+ by
 * the Soul Reaper. Tethered visually to the Reaper's spiral brooch and drained during Soul Harvest if not destroyed
 * by players first. Spec: {@code docs/design/soul_reaper.md} §1.4, {@code soul_reaper_systems.md} §4.
 */
public final class WanderingSoulWisp extends PathfinderMob implements PlayerSummon {
    public static final float WIDTH = 0.6F;
    public static final float HEIGHT = 0.8F;
    public static final float EYE_HEIGHT = 0.4F;

    public static final double MAX_HEALTH = 6.0D;
    public static final double MOVEMENT_SPEED = 0.18D;
    public static final double KNOCKBACK_RESISTANCE = 1.0D;
    public static final double FOLLOW_RANGE = 24.0D;
    public static final int HIT_COOLDOWN_TICKS = 10;

    private static final String TAG_REAPER_UUID = "ReaperOwnerUuid";

    private static final EntityDataAccessor<Integer> DATA_REAPER_ID =
            SynchedEntityData.defineId(WanderingSoulWisp.class, EntityDataSerializers.INT);

    private @Nullable UUID reaperUuid;
    private int lastHitTick = -100;
    private float orbitOffsetRad;

    public WanderingSoulWisp(EntityType<? extends WanderingSoulWisp> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.xpReward = 0;
        this.orbitOffsetRad = (float) (this.random.nextDouble() * Math.PI * 2.0D);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    @Override
    public boolean isPlayerSummon() {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_REAPER_ID, -1);
    }

    public void bindToReaper(SoulReaper reaper) {
        this.reaperUuid = reaper.getUUID();
        this.entityData.set(DATA_REAPER_ID, reaper.getId());
        reaper.getActiveWisps().add(this.getUUID());
    }

    public @Nullable UUID getReaperUuid() {
        return this.reaperUuid;
    }

    public int getReaperEntityId() {
        return this.entityData.get(DATA_REAPER_ID);
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
        if (this.level() instanceof ServerLevel serverLevel && !this.isNoAi()) {
            SoulReaper owner = this.resolveOwner(serverLevel);
            if (owner != null && owner.isAlive()) {
                float angle = this.orbitOffsetRad + this.tickCount * 0.04F;
                double radius = owner.getActionId() == SoulReaper.ActionId.O3_SOUL_HARVEST ? 2.2D : 5.5D;
                double targetX = owner.getX() + Math.cos(angle) * radius;
                double targetY = owner.getY() + 2.4D + Math.sin(this.tickCount * 0.08F + this.orbitOffsetRad) * 0.6D;
                double targetZ = owner.getZ() + Math.sin(angle) * radius;
                Vec3 delta = new Vec3(targetX - this.getX(), targetY - this.getY(), targetZ - this.getZ());
                if (delta.lengthSqr() > 0.04D) {
                    Vec3 step = delta.normalize().scale(0.12D);
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.7D).add(step.scale(0.3D)));
                    this.setYRot((float) (Mth.atan2(step.z, step.x) * Mth.RAD_TO_DEG) - 90.0F);
                }
            }
        }
    }

    private @Nullable SoulReaper resolveOwner(ServerLevel level) {
        int id = this.getReaperEntityId();
        if (id >= 0) {
            Entity entity = level.getEntity(id);
            if (entity instanceof SoulReaper reaper) {
                return reaper;
            }
        }
        if (this.reaperUuid != null) {
            Entity entity = level.getEntity(this.reaperUuid);
            if (entity instanceof SoulReaper reaper) {
                this.entityData.set(DATA_REAPER_ID, reaper.getId());
                return reaper;
            }
        }
        return null;
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
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.FREEZE) || source.is(DamageTypes.FALL) || source.is(DamageTypes.DROWN)) {
            return false;
        }
        if (source.getEntity() instanceof SoulReaper) {
            return false;
        }
        if (this.tickCount - this.lastHitTick < HIT_COOLDOWN_TICKS) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            this.lastHitTick = this.tickCount;
            if (!this.isAlive()) {
                SoulReaper owner = this.resolveOwner(level);
                if (owner != null) {
                    owner.getActiveWisps().remove(this.getUUID());
                }
            }
        }
        return hurt;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.reaperUuid != null) {
            output.putString(TAG_REAPER_UUID, this.reaperUuid.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String rawUuid = input.getStringOr(TAG_REAPER_UUID, "");
        if (!rawUuid.isEmpty()) {
            try {
                this.reaperUuid = UUID.fromString(rawUuid);
            } catch (IllegalArgumentException ignored) {
                this.reaperUuid = null;
            }
        }
    }
}
