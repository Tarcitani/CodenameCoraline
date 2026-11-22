package com.codenamecoraline.entity.firedragon;

import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
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
    private static final double AIR_DRAG = .99;

    public DragonFireball(EntityType<? extends DragonFireball> type, Level level) { super(type, level); }
    public DragonFireball(Level level, FireDragon owner) {
        super(ModEntities.DRAGON_FIREBALL.get(), owner, level, new ItemStack(Items.FIRE_CHARGE));
        setPos(owner.getX(), owner.getEyeY(), owner.getZ());
    }
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
        } else if (tickCount > 30 || !(getOwner() instanceof FireDragon d) || !d.isAlive() || d.isOnShoulder()) discard();
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        if (!(level() instanceof ServerLevel level) || !(getOwner() instanceof FireDragon owner)
                || owner.isOnShoulder() || !owner.canDamage(hit.getEntity())) return;
        if (hit.getEntity() instanceof LivingEntity target) {
            int previousFire = target.getRemainingFireTicks();
            target.igniteForSeconds(3);
            if (!target.hurtServer(level, damageSources().thrown(this, owner), 3)) target.setRemainingFireTicks(previousFire);
        }
    }
    @Override protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel serverLevel) {
            var loc = hit.getLocation();
            serverLevel.sendParticles(ParticleTypes.FLAME, loc.x, loc.y, loc.z, 8, 0.18, 0.18, 0.18, 0.08);
            serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, loc.x, loc.y, loc.z, 6, 0.22, 0.22, 0.22, 0.1);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, loc.x, loc.y + 0.05, loc.z, 3, 0.15, 0.15, 0.15, 0.03);
            playSound(ModSounds.FIRE_DRAGON_IMPACT.get(), 0.55F, 1.0F);
            discard();
        }
    }
}
