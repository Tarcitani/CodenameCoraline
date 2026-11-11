package com.codenamecoraline.entity.firedragon;

import com.codenamecoraline.registry.ModEntities;
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
        if (level().isClientSide()) level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0, 0, 0);
        else if (tickCount > 30 || !(getOwner() instanceof FireDragon d) || !d.isAlive() || d.isOnShoulder()) discard();
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
        if (!level().isClientSide()) discard();
    }
}
