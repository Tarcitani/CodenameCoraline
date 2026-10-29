package com.codenamecoraline.entity.collector;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Gift Mimic (Presente Faminto): a living, wrapped "pledge" the Collector of the Last Gift calls up when a stage
 * demands it. It looks like a gift box with stubby legs and a lid that snaps like a jaw, hunts players only, never
 * turns on the Collector, and bursts into a ring of confetti-shrapnel when its fuse runs out.
 */
public class GiftMimic extends Monster {
    public static final int FUSE_TICKS = 240;
    public static final float BURST_DAMAGE = 7.0F;
    public static final double BURST_RADIUS = 2.7D;

    private static final DustParticleOptions RED_WRAP = new DustParticleOptions(0xC7283A, 1.3F);
    private static final DustParticleOptions GOLD_RIBBON = new DustParticleOptions(0xE6B422, 1.3F);
    private static final DustParticleOptions GREEN_WRAP = new DustParticleOptions(0x2E8B57, 1.3F);

    private static final EntityDataAccessor<Integer> DATA_FUSE =
            SynchedEntityData.defineId(GiftMimic.class, EntityDataSerializers.INT);

    public GiftMimic(EntityType<? extends GiftMimic> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 26.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FUSE, 0);
    }

    /** 0..1 progress towards bursting; the model shakes and flashes as it approaches 1. */
    public float getFuseProgress() {
        return Math.min(1.0F, this.entityData.get(DATA_FUSE) / (float) FUSE_TICKS);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1D, true));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof LastGiftCollector) && !(target instanceof GiftMimic) && super.canAttack(target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // Friendly fire from its master (stomps, comets, presents) never harms it.
        if (source.getEntity() instanceof LastGiftCollector || source.getDirectEntity() instanceof LastGiftCollector) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level) {
            int fuse = this.entityData.get(DATA_FUSE) + 1;
            this.entityData.set(DATA_FUSE, fuse);
            if (fuse % 6 == 0) {
                level.sendParticles(fuse % 12 == 0 ? GOLD_RIBBON : RED_WRAP,
                        this.getX(), this.getY() + 0.9D, this.getZ(), 3, 0.3D, 0.3D, 0.3D, 0.01D);
            }
            if (fuse >= FUSE_TICKS) {
                this.burst(level);
            }
        }
    }

    /** The pledge comes due: confetti and shrapnel hit every player close by, then the mimic is gone. */
    public void burst(ServerLevel level) {
        if (!this.isAlive()) {
            return;
        }
        this.playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 1.6F, 0.7F);
        this.playSound(SoundEvents.FIREWORK_ROCKET_BLAST, 1.2F, 1.0F);
        confetti(level, this.position().add(0.0D, 0.8D, 0.0D), 36);
        AABB box = this.getBoundingBox().inflate(BURST_RADIUS);
        List<Player> victims = level.getEntitiesOfClass(Player.class, box,
                p -> p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) <= BURST_RADIUS * BURST_RADIUS);
        for (Player player : victims) {
            player.hurtServer(level, this.damageSources().mobAttack(this), BURST_DAMAGE);
        }
        this.discard();
    }

    @Override
    public void die(DamageSource source) {
        if (this.level() instanceof ServerLevel level) {
            confetti(level, this.position().add(0.0D, 0.8D, 0.0D), 20);
        }
        super.die(source);
    }

    static void confetti(ServerLevel level, Vec3 center, int count) {
        for (int i = 0; i < count; i++) {
            DustParticleOptions colour = switch (i % 3) {
                case 0 -> RED_WRAP;
                case 1 -> GOLD_RIBBON;
                default -> GREEN_WRAP;
            };
            double a = Math.PI * 2.0D * i / count;
            level.sendParticles(colour, center.x + Math.cos(a) * 0.4D, center.y, center.z + Math.sin(a) * 0.4D,
                    1, Math.cos(a) * 0.5D, 0.35D, Math.sin(a) * 0.5D, 0.25D);
        }
        level.sendParticles(ParticleTypes.FIREWORK, center.x, center.y, center.z, 18, 0.5D, 0.4D, 0.5D, 0.12D);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BARREL_CLOSE;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AMETHYST_BLOCK_BREAK;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return true;
    }
}
