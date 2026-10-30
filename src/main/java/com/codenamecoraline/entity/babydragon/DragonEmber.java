package com.codenamecoraline.entity.babydragon;

import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Cosmetic fire, direct damage only: no ignition, explosion or block mutation paths. */
public final class DragonEmber extends ThrowableItemProjectile {
    public static final float DAMAGE = 3;
    public static final float SPEED = 1.2F;
    public static final double MAX_RANGE = 12;
    private @Nullable Vec3 origin;
    private int life;
    public DragonEmber(EntityType<? extends DragonEmber> type, Level level) { super(type, level); }
    public DragonEmber(Level level, BabyDragon owner) { super(ModEntities.DRAGON_EMBER.get(), owner, level, new ItemStack(ModItems.DRAGON_EMBER.get())); }
    @Override protected Item getDefaultItem() { return ModItems.DRAGON_EMBER.get(); }
    @Override protected double getDefaultGravity() { return 0; }
    @Override protected boolean canHitEntity(Entity entity) {
        return entity != this.getOwner() && (!(this.getOwner() instanceof BabyDragon dragon) || !dragon.isProtected(entity)) && super.canHitEntity(entity);
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        if (this.level() instanceof ServerLevel server && this.getOwner() instanceof BabyDragon dragon
                && !dragon.isProtected(hit.getEntity())) hit.getEntity().hurtServer(server, this.damageSources().thrown(this, dragon), DAMAGE);
    }
    @Override protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SMALL_FLAME, this.getX(), this.getY(), this.getZ(), 8, 0.12, 0.12, 0.12, 0.02);
            this.discard();
        }
    }
    @Override public void tick() {
        if (!this.level().isClientSide()) {
            if (this.origin == null) this.origin = this.position();
            if (++this.life > 20 || this.position().add(this.getDeltaMovement()).distanceToSqr(this.origin) > MAX_RANGE * MAX_RANGE + 0.01
                    || !(this.getOwner() instanceof BabyDragon dragon) || !dragon.isAlive()) { this.discard(); return; }
        }
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.SMALL_FLAME, this.getX(), this.getY(), this.getZ(), 0, 0.005, 0);
            this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("EmberLife", this.life);
        if (this.origin != null) { out.putDouble("EmberX", this.origin.x); out.putDouble("EmberY", this.origin.y); out.putDouble("EmberZ", this.origin.z); }
    }
    @Override protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        this.life = Math.max(0, Math.min(20, in.getIntOr("EmberLife", 0)));
        double x = in.getDoubleOr("EmberX", Double.NaN), y = in.getDoubleOr("EmberY", Double.NaN), z = in.getDoubleOr("EmberZ", Double.NaN);
        this.origin = Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z) ? new Vec3(x, y, z) : null;
    }
}
