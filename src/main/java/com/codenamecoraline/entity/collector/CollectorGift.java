package com.codenamecoraline.entity.collector;

import com.codenamecoraline.registry.ModSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The Collector's last gift: a big Christmas-wrapped present that drops where he fell, shakes, and pops open to
 * reveal the loot chest hidden inside. Purely visual (the chest is a real block placed by the boss), never saved.
 */
public class CollectorGift extends Entity {
    /** Shakes until this tick, then the lid flies off. */
    public static final int POP_TICK = 36;
    public static final int LIFETIME_TICKS = 78;

    private static final DustParticleOptions RED_WRAP = new DustParticleOptions(0xC7283A, 1.4F);
    private static final DustParticleOptions GOLD_RIBBON = new DustParticleOptions(0xE6B422, 1.4F);
    private static final DustParticleOptions GREEN_WRAP = new DustParticleOptions(0x2E8B57, 1.4F);

    public CollectorGift(EntityType<? extends CollectorGift> type, Level level) {
        super(type, level);
        this.setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        int age = this.tickCount;
        if (age < POP_TICK) {
            if (age % 4 == 0) {
                level.sendParticles(GOLD_RIBBON, this.getX(), this.getY() + 1.4D, this.getZ(), 2, 0.5D, 0.2D, 0.5D, 0.02D);
            }
            if (age % 9 == 0) {
                this.playSound(ModSounds.COLLECTOR_SLEIGH_BELLS.get(), 1.0F, 1.2F + age * 0.01F);
            }
        } else if (age == POP_TICK) {
            this.playSound(SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, 1.8F, 0.9F);
            this.playSound(SoundEvents.CHEST_OPEN, 1.4F, 0.8F);
            GiftMimic.confetti(level, this.position().add(0.0D, 1.3D, 0.0D), 60);
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.4D, this.getZ(), 30, 0.6D, 0.8D, 0.6D, 0.12D);
        } else if (age % 3 == 0 && age < POP_TICK + 24) {
            level.sendParticles(age % 2 == 0 ? RED_WRAP : GREEN_WRAP,
                    this.getX(), this.getY() + 1.6D, this.getZ(), 4, 0.5D, 0.5D, 0.5D, 0.03D);
        }
        if (age >= LIFETIME_TICKS) {
            this.discard();
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
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
