package com.codenamecoraline.entity.basilisk;

import com.codenamecoraline.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** A five-second cloud without potion damage, radius shrinkage, or per-cloud buildup stacking. */
public final class DeathBlightCloud extends AreaEffectCloud {
    public DeathBlightCloud(EntityType<? extends DeathBlightCloud> type, Level level) {
        super(type, level);
        this.setRadius(3.5F);
        this.setDuration(100);
        this.setWaitTime(0);
        this.setRadiusOnUse(0);
        this.setRadiusPerTick(0);
        this.setCustomParticle(ParticleTypes.SMOKE);
    }

    public static DeathBlightCloud create(Level level, Vec3 position, @Nullable LivingEntity owner) {
        DeathBlightCloud cloud = new DeathBlightCloud(ModEntities.DEATH_BLIGHT_CLOUD.get(), level);
        cloud.setPos(position);
        cloud.setOwner(owner);
        return cloud;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved() || this.isWaiting()) return;
        if (this.level() instanceof ServerLevel level) {
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(0, 1.5, 0), DeathBlight::susceptible)) {
                double dx = entity.getX() - this.getX();
                double dz = entity.getZ() - this.getZ();
                if (dx * dx + dz * dz > this.getRadius() * this.getRadius()) continue;
                Vec3 from = this.position().add(0, 0.35, 0);
                Vec3 to = entity.position().add(0, Math.min(0.5, entity.getBbHeight() * 0.5), 0);
                if (level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS) {
                    DeathBlight.expose(entity, this.getOwner());
                }
            }
        } else {
            for (int i = 0; i < 7; i++) {
                double angle = this.random.nextDouble() * Math.PI * 2;
                double radius = Math.sqrt(this.random.nextDouble()) * this.getRadius();
                this.level().addParticle(i % 3 == 0 ? DeathBlight.GOLD : ParticleTypes.SQUID_INK,
                        this.getX() + Math.cos(angle) * radius, this.getY() + this.random.nextDouble() * 0.9,
                        this.getZ() + Math.sin(angle) * radius, 0, 0.015, 0);
            }
        }
    }
}
