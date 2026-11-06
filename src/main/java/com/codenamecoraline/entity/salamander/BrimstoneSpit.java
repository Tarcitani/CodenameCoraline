package com.codenamecoraline.entity.salamander;

import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import java.util.List;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Sulfurous tar globule hurled in a ballistic arc by the Brimstone Salamander ({@code SULFUR_SPIT}).
 * Deals 5.0 damage on impact and transforms into a 2x2 Burning Tar Pool for 100 ticks that applies
 * Slowness II and fire to non-salamander intruders.
 */
public final class BrimstoneSpit extends ThrowableItemProjectile {
    public static final float DAMAGE = 5.0F;
    public static final int TAR_POOL_DURATION_TICKS = 100;
    public static final double TAR_POOL_HALF_EXTENT = 1.0D;
    public static final int MAX_FLIGHT_TICKS = 60;
    public static final DustParticleOptions SULPHUR_DUST = new DustParticleOptions(0xE3C434, 1.0F);
    public static final DustParticleOptions TAR_DUST = new DustParticleOptions(0x221E1D, 1.2F);
    private static final byte EVENT_TAR_SPLASH = 3;

    private static final EntityDataAccessor<Boolean> DATA_TAR_POOL =
            SynchedEntityData.defineId(BrimstoneSpit.class, EntityDataSerializers.BOOLEAN);

    private int tarPoolTicks = 0;
    private @Nullable Vec3 originPos;

    public BrimstoneSpit(EntityType<? extends BrimstoneSpit> type, Level level) {
        super(type, level);
    }

    public BrimstoneSpit(Level level, LivingEntity owner) {
        super(ModEntities.BRIMSTONE_SPIT.get(), owner, level, new ItemStack(ModItems.BRIMSTONE_SPIT.get()));
        this.originPos = owner.position();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TAR_POOL, false);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BRIMSTONE_SPIT.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04D;
    }

    public boolean isTarPool() {
        return this.entityData.get(DATA_TAR_POOL);
    }

    public int getTarPoolTicks() {
        return this.tarPoolTicks;
    }

    @Override
    public void tick() {
        if (this.originPos == null) {
            this.originPos = this.position();
        }
        if (this.isTarPool()) {
            this.tickTarPool();
            return;
        }
        super.tick();
        if (this.isRemoved() || this.isTarPool()) {
            return;
        }
        if (this.level().isClientSide()) {
            this.level().addParticle(SULPHUR_DUST, this.getX(), this.getY() + 0.1D, this.getZ(), 0.0D, 0.0D, 0.0D);
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.1D, this.getZ(), 0.0D, 0.01D, 0.0D);
        } else if (this.tickCount >= MAX_FLIGHT_TICKS) {
            this.becomeTarPool(this.position());
        }
    }

    private void tickTarPool() {
        if (this.level().isClientSide()) {
            if (this.tickCount % 3 == 0) {
                for (int i = 0; i < 3; i++) {
                    double ox = (this.random.nextDouble() - 0.5D) * 2.0D * TAR_POOL_HALF_EXTENT;
                    double oz = (this.random.nextDouble() - 0.5D) * 2.0D * TAR_POOL_HALF_EXTENT;
                    this.level().addParticle(TAR_DUST, this.getX() + ox, this.getY() + 0.08D, this.getZ() + oz, 0.0D, 0.01D, 0.0D);
                    this.level().addParticle(ParticleTypes.FLAME, this.getX() + ox, this.getY() + 0.1D, this.getZ() + oz, 0.0D, 0.02D, 0.0D);
                }
            }
            return;
        }
        if (this.tickCount % 5 == 0) {
            this.applyTarPoolEffects();
        }
        if (--this.tarPoolTicks <= 0) {
            this.discard();
        }
    }

    public void applyTarPoolEffects() {
        AABB box = new AABB(
                this.getX() - TAR_POOL_HALF_EXTENT, this.getY() - 0.5D, this.getZ() - TAR_POOL_HALF_EXTENT,
                this.getX() + TAR_POOL_HALF_EXTENT, this.getY() + 1.2D, this.getZ() + TAR_POOL_HALF_EXTENT);
        List<LivingEntity> victims = this.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                entity -> entity.isAlive() && !(entity instanceof BrimstoneSalamander));
        for (int i = 0, n = victims.size(); i < n; i++) {
            LivingEntity victim = victims.get(i);
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
            victim.igniteForSeconds(2.0F);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (this.isTarPool() || entity instanceof BrimstoneSalamander || entity == this.getOwner()) {
            return false;
        }
        return super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        Entity target = hitResult.getEntity();
        if (target == this.getOwner() || target instanceof BrimstoneSalamander || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        target.hurtServer(level, this.damageSources().thrown(this, this.getOwner()), DAMAGE);
        target.igniteForSeconds(2.0F);
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide() && !this.isTarPool()) {
            this.becomeTarPool(hitResult.getLocation());
        }
    }

    public void becomeTarPool(Vec3 pos) {
        if (this.isRemoved() || this.isTarPool()) {
            return;
        }
        this.setPos(pos.x, pos.y, pos.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.entityData.set(DATA_TAR_POOL, true);
        this.tarPoolTicks = TAR_POOL_DURATION_TICKS;
        this.playSound(SoundEvents.LAVA_POP, 0.9F, 0.75F + this.random.nextFloat() * 0.2F);
        this.level().broadcastEntityEvent(this, EVENT_TAR_SPLASH);
        this.applyTarPoolEffects();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_TAR_SPLASH) {
            ParticleOptions itemParticle = new ItemParticleOption(
                    ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(this.getItem()));
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(itemParticle, this.getX(), this.getY() + 0.1D, this.getZ(),
                        (this.random.nextDouble() - 0.5D) * 0.2D,
                        this.random.nextDouble() * 0.15D,
                        (this.random.nextDouble() - 0.5D) * 0.2D);
            }
            for (int i = 0; i < 6; i++) {
                this.level().addParticle(SULPHUR_DUST, this.getX(), this.getY() + 0.1D, this.getZ(), 0.0D, 0.02D, 0.0D);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("TarPool", this.isTarPool());
        output.putInt("TarPoolTicks", this.tarPoolTicks);
        if (this.originPos != null) {
            output.putDouble("OriginX", this.originPos.x);
            output.putDouble("OriginY", this.originPos.y);
            output.putDouble("OriginZ", this.originPos.z);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        boolean pool = input.getBooleanOr("TarPool", false);
        this.entityData.set(DATA_TAR_POOL, pool);
        this.tarPoolTicks = input.getIntOr("TarPoolTicks", 0);
        if (pool) {
            this.setNoGravity(true);
            this.setDeltaMovement(Vec3.ZERO);
        }
        double ox = input.getDoubleOr("OriginX", Double.NaN);
        double oy = input.getDoubleOr("OriginY", Double.NaN);
        double oz = input.getDoubleOr("OriginZ", Double.NaN);
        if (!Double.isNaN(ox) && !Double.isNaN(oy) && !Double.isNaN(oz)) {
            this.originPos = new Vec3(ox, oy, oz);
        }
    }
}

