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
import net.minecraft.network.syncher.*;
import org.jspecify.annotations.Nullable;

/** One compatible projectile registration, with stage-specific size, range and safe splash. */
public final class DragonEmber extends ThrowableItemProjectile {
    public static final float DAMAGE = 6;
    public static final float SPEED = 1.2F;
    public static final double MAX_RANGE = 12;
    private @Nullable Vec3 origin;
    private int life;
    private static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(DragonEmber.class, EntityDataSerializers.INT);
    public DragonEmber(EntityType<? extends DragonEmber> type, Level level) { super(type, level); }
    public DragonEmber(Level level, BabyDragon owner) {
        super(ModEntities.DRAGON_EMBER.get(), owner, level, new ItemStack(ModItems.DRAGON_EMBER.get()));
        this.entityData.set(STAGE, owner.getDragonStage().ordinal()); this.refreshDimensions();
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) { super.defineSynchedData(data); data.define(STAGE, 0); }
    public DragonStage getDragonStage() { return DragonStage.byId(this.entityData.get(STAGE)); }
    @Override public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose) {
        return super.getDimensions(pose).scale(this.getDragonStage() == DragonStage.ADULT ? 4 : this.getDragonStage() == DragonStage.YOUNG ? 2 : 1);
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) { super.onSyncedDataUpdated(key); if (key == STAGE) this.refreshDimensions(); }
    @Override protected Item getDefaultItem() { return ModItems.DRAGON_EMBER.get(); }
    @Override protected double getDefaultGravity() { return 0; }
    @Override protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof net.minecraft.world.entity.item.ItemEntity)
                && entity != this.getOwner() && (!(this.getOwner() instanceof BabyDragon dragon) || !dragon.isProtected(entity)) && super.canHitEntity(entity);
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        if (this.level() instanceof ServerLevel && this.getOwner() instanceof BabyDragon dragon
                && hit.getEntity() instanceof LivingEntity target && !dragon.isProtected(target))
            dragon.damageEnemyWithFire(target, dragon.getFireballDamage(), 3 + this.getDragonStage().ordinal());
    }
    @Override protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level() instanceof ServerLevel server) {
            double radius = this.getDragonStage().ordinal() * 1.5;
            server.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 8 + 30 * this.getDragonStage().ordinal(), .2 + radius * .4, .2 + radius * .4, .2 + radius * .4, .06);
            if (radius > 0 && this.getOwner() instanceof BabyDragon dragon) {
                server.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
                Entity direct = hit instanceof EntityHitResult e ? e.getEntity() : null;
                for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius))) {
                    if (target == direct || !dragon.validDefensiveTarget(target) || target.position().distanceToSqr(this.position()) > radius * radius) continue;
                    if (server.clip(new net.minecraft.world.level.ClipContext(this.position(), target.getBoundingBox().getCenter(), net.minecraft.world.level.ClipContext.Block.COLLIDER,
                            net.minecraft.world.level.ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS)
                        dragon.damageEnemyWithFire(target, dragon.getFireballDamage() * .5F, 2 + this.getDragonStage().ordinal());
                }
            }
            this.discard();
        }
    }
    @Override public void tick() {
        if (!this.level().isClientSide()) {
            if (this.origin == null) this.origin = this.position();
            if (++this.life > 50 || this.position().add(this.getDeltaMovement()).distanceToSqr(this.origin) > this.getDragonStage().range * this.getDragonStage().range + 0.01
                    || !(this.getOwner() instanceof BabyDragon dragon) || !dragon.isAlive()) { this.discard(); return; }
            if (this.getDragonStage() == DragonStage.ADULT && this.level() instanceof ServerLevel server && this.life % 2 == 0)
                // Lay the trail behind the moving sphere, never inside its next collision sweep.
                DragonMagmaTrail.get(server).sample(server, this.position().subtract(this.getDeltaMovement().scale(2)), dragon);
        }
        super.tick();
        if (this.level().isClientSide()) {
            int count = 3 + this.getDragonStage().ordinal() * 8;
            for (int i = 0; i < count; i++) {
                double t = i / (double)count;
                Vec3 p = this.position().subtract(this.getDeltaMovement().scale(t));
                double size = .05 + this.getDragonStage().ordinal() * .15;
                this.level().addParticle(i % 3 == 0 ? ParticleTypes.SMALL_FLAME : ParticleTypes.FLAME,
                        p.x + (this.random.nextDouble() - .5) * size, p.y + (this.random.nextDouble() - .5) * size,
                        p.z + (this.random.nextDouble() - .5) * size, 0, .005, 0);
            }
        }
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("EmberLife", this.life);
        out.putInt("DragonStage", this.getDragonStage().ordinal());
        if (this.origin != null) { out.putDouble("EmberX", this.origin.x); out.putDouble("EmberY", this.origin.y); out.putDouble("EmberZ", this.origin.z); }
    }
    @Override protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        this.life = Math.max(0, Math.min(50, in.getIntOr("EmberLife", 0)));
        this.entityData.set(STAGE, DragonStage.byId(in.getIntOr("DragonStage", 0)).ordinal()); this.refreshDimensions();
        double x = in.getDoubleOr("EmberX", Double.NaN), y = in.getDoubleOr("EmberY", Double.NaN), z = in.getDoubleOr("EmberZ", Double.NaN);
        this.origin = Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z) ? new Vec3(x, y, z) : null;
    }
}
