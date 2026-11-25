package com.codenamecoraline.entity.firedragon;

import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Non-explosive fire bolt. Ignites before lethal damage so vanilla loot can cook meat. */
public final class DragonFireball extends ThrowableItemProjectile {
    public static final float SPEED = .8F;
    public static final float YOUNG_SPEED = .9F;
    public static final float ADULT_SPEED = 1.05F;
    private static final double AIR_DRAG = .99;
    private static final EntityDataAccessor<Float> SCALE =
            SynchedEntityData.defineId(DragonFireball.class, EntityDataSerializers.FLOAT);

    private float damage = 3.0F;
    private int maxLife = 30;
    private int igniteSeconds = 3;
    private float splashRadius = 0.0F;
    private float splashDamage = 0.0F;
    private net.minecraft.world.entity.Entity directHitEntity;

    public DragonFireball(EntityType<? extends DragonFireball> type, Level level) { super(type, level); }
    public DragonFireball(Level level, FireDragon owner) {
        this(level, owner, 3.0F, 1.0F, 30, 3, 0.0F, 0.0F);
    }
    public DragonFireball(Level level, FireDragon owner, float damage, float scale, int maxLife, int igniteSeconds) {
        this(level, owner, damage, scale, maxLife, igniteSeconds, 0.0F, 0.0F);
    }
    public DragonFireball(Level level, FireDragon owner, float damage, float scale, int maxLife,
                          int igniteSeconds, float splashRadius, float splashDamage) {
        super(ModEntities.DRAGON_FIREBALL.get(), owner, level, new ItemStack(Items.FIRE_CHARGE));
        setPos(owner.getX(), owner.getEyeY(), owner.getZ());
        this.damage = damage;
        this.maxLife = maxLife;
        this.igniteSeconds = igniteSeconds;
        this.splashRadius = splashRadius;
        this.splashDamage = splashDamage;
        this.entityData.set(SCALE, scale);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SCALE, 1.0F);
    }

    public float getVisualScale() { return entityData.get(SCALE); }
    public float getFireballDamage() { return damage; }
    /** Bounded leading of the torso, including vanilla air drag; no homing after release. */
    public static net.minecraft.world.phys.Vec3 aimPoint(net.minecraft.world.phys.Vec3 origin,
            net.minecraft.world.phys.Vec3 center, net.minecraft.world.phys.Vec3 velocity) {
        double flightTicks = origin.distanceTo(center) / SPEED;
        for (int i = 0; i < 4; i++) {
            var predicted = center.add(velocity.x * flightTicks,
                    Math.clamp(velocity.y, -.2, .2) * Math.min(6, flightTicks), velocity.z * flightTicks);
            double dragTerm = Math.max(.01, 1 - origin.distanceTo(predicted) * (1 - AIR_DRAG) / (SPEED * AIR_DRAG));
            flightTicks = Math.clamp(Math.log(dragTerm) / Math.log(AIR_DRAG), 0, 24);
        }
        return center.add(velocity.x * flightTicks,
                Math.clamp(velocity.y, -.2, .2) * Math.min(6, flightTicks), velocity.z * flightTicks);
    }

    @Override protected Item getDefaultItem() { return Items.FIRE_CHARGE; }
    @Override protected double getDefaultGravity() { return 0; }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide()) {
            var vel = getDeltaMovement();
            double angle = tickCount * 1.25D;
            double ox = Math.cos(angle) * 0.11D;
            double oz = Math.sin(angle) * 0.11D;
            double bx = getX() - vel.x * 0.35D;
            double by = getY() - vel.y * 0.35D;
            double bz = getZ() - vel.z * 0.35D;
            level().addParticle(ParticleTypes.FLAME, bx + ox, by + 0.04D, bz + oz,
                    -vel.x * 0.12D, 0.015D, -vel.z * 0.12D);
            level().addParticle(ParticleTypes.SMALL_FLAME, bx - ox, by + 0.08D, bz - oz,
                    -vel.x * 0.18D + (random.nextDouble() - 0.5D) * 0.04D, 0.02D,
                    -vel.z * 0.18D + (random.nextDouble() - 0.5D) * 0.04D);
            level().addParticle(ParticleTypes.SMOKE, bx - vel.x * 0.45D, by + 0.06D, bz - vel.z * 0.45D,
                    (random.nextDouble() - 0.5D) * 0.02D, 0.025D, (random.nextDouble() - 0.5D) * 0.02D);
        } else if (tickCount > maxLife || !(getOwner() instanceof FireDragon d) || !d.isAlive() || d.isOnShoulder()) discard();
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        if (!(level() instanceof ServerLevel level) || !(getOwner() instanceof FireDragon owner)
                || owner.isOnShoulder() || !owner.canDamage(hit.getEntity())) return;
        if (hit.getEntity() instanceof LivingEntity target) {
            directHitEntity = target;
            int previousFire = target.getRemainingFireTicks();
            target.igniteForSeconds(igniteSeconds);
            if (!target.hurtServer(level, damageSources().thrown(this, owner), damage)) target.setRemainingFireTicks(previousFire);
        }
    }
    @Override protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel serverLevel) {
            var loc = hit.getLocation();
            boolean meteor = splashRadius > 0.0F;
            serverLevel.sendParticles(ParticleTypes.FLAME, loc.x, loc.y, loc.z, meteor ? 20 : 8,
                    meteor ? 0.45 : 0.18, meteor ? 0.35 : 0.18, meteor ? 0.45 : 0.18, meteor ? 0.14 : 0.08);
            serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, loc.x, loc.y, loc.z, meteor ? 16 : 6,
                    meteor ? 0.5 : 0.22, meteor ? 0.4 : 0.22, meteor ? 0.5 : 0.22, meteor ? 0.16 : 0.1);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, loc.x, loc.y + 0.05, loc.z, meteor ? 8 : 3,
                    meteor ? 0.4 : 0.15, meteor ? 0.3 : 0.15, meteor ? 0.4 : 0.15, 0.04);
            if (meteor && getOwner() instanceof FireDragon owner) {
                var area = new net.minecraft.world.phys.AABB(loc, loc).inflate(splashRadius);
                for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
                    if (victim == directHitEntity || !owner.canDamage(victim)) continue;
                    if (victim.position().distanceToSqr(loc) > (double) splashRadius * splashRadius) continue;
                    int prevFire = victim.getRemainingFireTicks();
                    victim.igniteForSeconds(igniteSeconds);
                    if (!victim.hurtServer(serverLevel, damageSources().thrown(this, owner), splashDamage)) {
                        victim.setRemainingFireTicks(prevFire);
                    }
                }
            }
            playSound(ModSounds.FIRE_DRAGON_IMPACT.get(), meteor ? 0.9F : 0.55F, meteor ? 0.72F : 1.0F);
            discard();
        }
    }
}
