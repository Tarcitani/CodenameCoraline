package com.codenamecoraline.entity.redcap;

import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import com.codenamecoraline.registry.ModSounds;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** A small spinning pebble hurled by a claws Redcap: heavy hit, strong knockback, Slowness II. Never hits other Redcaps. */
public final class RedcapRock extends ThrowableItemProjectile {
    private static final float DAMAGE = 8.0F;
    private static final double KNOCKBACK = 0.9D;
    private static final int SLOWNESS_TICKS = 40;
    private static final byte EVENT_BREAK = 3;

    public RedcapRock(EntityType<? extends RedcapRock> type, Level level) {
        super(type, level);
    }

    public RedcapRock(Level level, LivingEntity owner) {
        super(ModEntities.REDCAP_ROCK.get(), owner, level, new ItemStack(ModItems.REDCAP_PEBBLE.get()));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.REDCAP_PEBBLE.get();
    }

    /** Pebbles fly through the pack: friendly fire would punish the Redcaps for working together. */
    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof Redcap) && super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        Entity target = hitResult.getEntity();
        if (target == this.getOwner() || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        target.hurtServer(level, this.damageSources().thrown(this, this.getOwner()), DAMAGE);
        if (target instanceof LivingEntity living) {
            Vec3 direction = this.getDeltaMovement().normalize();
            // knockback() pushes away from the given point of view: pass the opposite of the travel direction.
            living.knockback(KNOCKBACK, -direction.x, -direction.z);
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_TICKS, 1));
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            this.playSound(ModSounds.REDCAP_ROCK_HIT.get(), 0.9F, 1.2F + this.random.nextFloat() * 0.2F);
            this.level().broadcastEntityEvent(this, EVENT_BREAK);
            this.discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_BREAK) {
            ParticleOptions particle = new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(this.getItem()));
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(particle, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }
}
