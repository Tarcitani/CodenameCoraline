package com.codenamecoraline.entity.spirit;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The ghost a boss leaves when it dies: its own model, translucent and blue, rising slowly from the body and fading out.
 * Purely visual: it cannot be hit, pushed or interacted with, is never saved, and removes itself after {@link #LIFETIME}.
 * Each boss has its own entity type of this class so the client can draw it with that boss's model.
 */
public class BossSpirit extends LivingEntity {
    public static final int LIFETIME = 90;
    public static final double RISE_SPEED = 0.045D;

    /** Client-side clock of the calm pose the spirit holds. */
    public final AnimationState poseState = new AnimationState();

    public BossSpirit(EntityType<? extends BossSpirit> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes();
    }

    /** Raises the spirit of {@code boss} from where it lies. */
    public static @Nullable BossSpirit rise(ServerLevel level, EntityType<BossSpirit> type, LivingEntity boss) {
        BossSpirit spirit = type.create(level, EntitySpawnReason.TRIGGERED);
        if (spirit == null) {
            return null;
        }
        spirit.setPos(boss.getX(), boss.getY(), boss.getZ());
        spirit.setYRot(boss.getYRot());
        spirit.setYHeadRot(boss.getYRot());
        spirit.yBodyRot = boss.getYRot();
        level.addFreshEntity(spirit);
        level.playSound(null, boss.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 2.0F, 0.6F);
        level.sendParticles(ParticleTypes.SOUL, boss.getX(), boss.getY() + 1.0D, boss.getZ(), 24, 0.8D, 0.8D, 0.8D, 0.02D);
        return spirit;
    }

    /** 0..1 visibility: a short fade in, a long fade out at the end of the rise. */
    public static float visibility(float age) {
        float in = Math.min(1.0F, age / 12.0F);
        float out = Math.min(1.0F, Math.max(0.0F, (LIFETIME - age) / 36.0F));
        return in * out;
    }

    @Override
    public void tick() {
        if (this.level().isClientSide() && !this.poseState.isStarted()) {
            this.poseState.start(this.tickCount);
        }
        this.noPhysics = true;
        this.setDeltaMovement(Vec3.ZERO); // the rise is a steady drift, not physics
        super.tick();
        this.setPos(this.getX(), this.getY() + RISE_SPEED, this.getZ());
        if (this.level() instanceof ServerLevel level) {
            if (this.tickCount % 4 == 0) {
                level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + this.getBbHeight() * 0.6D, this.getZ(),
                        2, this.getBbWidth() * 0.4D, this.getBbHeight() * 0.3D, this.getBbWidth() * 0.4D, 0.01D);
            }
            if (this.tickCount >= LIFETIME) {
                this.discard();
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
}
