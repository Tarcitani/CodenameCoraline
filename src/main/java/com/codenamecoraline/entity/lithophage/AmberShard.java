package com.codenamecoraline.entity.lithophage;

import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import com.codenamecoraline.registry.ModSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Mineral amber projectile hurled by the Lithophage (Estilhaço de Âmbar).
 * Flies at moderate speed with limited range, shatters on blocks or entities,
 * never passes through walls, and never harms Lithophages.
 */
public final class AmberShard extends ThrowableItemProjectile {
    public static final float DAMAGE = 4.0F;
    public static final double KNOCKBACK = 0.35D;
    public static final double MAX_RANGE = 15.0D;
    public static final int MAX_LIFETIME_TICKS = 40;
    public static final DustParticleOptions AMBER_DUST = new DustParticleOptions(0xD4A64B, 0.85F);
    private static final byte EVENT_SHATTER = 3;

    private @Nullable Vec3 originPos;

    public AmberShard(EntityType<? extends AmberShard> type, Level level) {
        super(type, level);
    }

    public AmberShard(Level level, LivingEntity owner) {
        super(ModEntities.AMBER_SHARD.get(), owner, level, new ItemStack(ModItems.AMBER_SHARD.get()));
        this.originPos = owner.position();
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.AMBER_SHARD.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.015D;
    }

    @Override
    public void tick() {
        if (this.originPos == null) {
            this.originPos = this.position();
        }
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        if (this.level().isClientSide()) {
            if (this.tickCount % 2 == 0) {
                this.level().addParticle(AMBER_DUST, this.getX(), this.getY() + 0.1D, this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        } else if (this.tickCount >= MAX_LIFETIME_TICKS
                || this.position().distanceToSqr(this.originPos) > MAX_RANGE * MAX_RANGE) {
            this.shatter();
        }
    }

    /** Never hits the shooter or other Lithophages. */
    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof Lithophage) && entity != this.getOwner() && super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        Entity target = hitResult.getEntity();
        if (target == this.getOwner() || target instanceof Lithophage || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        target.hurtServer(level, this.damageSources().thrown(this, this.getOwner()), DAMAGE);
        if (target instanceof LivingEntity living) {
            Vec3 direction = this.getDeltaMovement().normalize();
            if (direction.lengthSqr() > 1.0E-4D) {
                living.knockback(KNOCKBACK, -direction.x, -direction.z);
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            this.shatter();
        }
    }

    private void shatter() {
        if (this.isRemoved()) {
            return;
        }
        this.playSound(ModSounds.LITHOPHAGE_SHARD_HIT.get(), 0.85F, 0.9F + this.random.nextFloat() * 0.2F);
        this.level().broadcastEntityEvent(this, EVENT_SHATTER);
        this.discard();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_SHATTER) {
            ParticleOptions itemParticle = new ItemParticleOption(
                    ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(this.getItem()));
            for (int i = 0; i < 6; i++) {
                this.level().addParticle(itemParticle, this.getX(), this.getY(), this.getZ(),
                        (this.random.nextDouble() - 0.5D) * 0.15D,
                        this.random.nextDouble() * 0.12D,
                        (this.random.nextDouble() - 0.5D) * 0.15D);
            }
            for (int i = 0; i < 4; i++) {
                this.level().addParticle(AMBER_DUST, this.getX(), this.getY(), this.getZ(), 0.0D, 0.01D, 0.0D);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.originPos != null) {
            output.putDouble("OriginX", this.originPos.x);
            output.putDouble("OriginY", this.originPos.y);
            output.putDouble("OriginZ", this.originPos.z);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        double ox = input.getDoubleOr("OriginX", Double.NaN);
        double oy = input.getDoubleOr("OriginY", Double.NaN);
        double oz = input.getDoubleOr("OriginZ", Double.NaN);
        if (!Double.isNaN(ox) && !Double.isNaN(oy) && !Double.isNaN(oz)) {
            this.originPos = new Vec3(ox, oy, oz);
        }
    }
}

