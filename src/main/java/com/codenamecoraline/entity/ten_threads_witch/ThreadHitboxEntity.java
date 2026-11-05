package com.codenamecoraline.entity.ten_threads_witch;

import java.util.UUID;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * The stretch of a crimson thread a player can cut: a small invisible target (0.7 x 1.6) the puppet places along its thread
 * at the player's height while the thread is cuttable. A blow or an arrow on it cuts the thread (see
 * {@link PuppetEntity#cutThread}). It is never saved and disappears with its puppet.
 */
public class ThreadHitboxEntity extends Entity implements TenThreadsEncounterMember {
    private @Nullable UUID puppetUuid;

    public ThreadHitboxEntity(EntityType<? extends ThreadHitboxEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void bind(PuppetEntity puppet) {
        this.puppetUuid = puppet.getUUID();
    }

    public @Nullable PuppetEntity puppet() {
        if (this.puppetUuid != null && this.level() instanceof ServerLevel level && level.getEntity(this.puppetUuid) instanceof PuppetEntity p) {
            return p;
        }
        return null;
    }

    @Override
    public @Nullable UUID getOwnerWitchUuid() {
        PuppetEntity p = this.puppet();
        return p == null ? null : p.getOwnerWitchUuid();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            PuppetEntity p = this.puppet();
            if (p == null || !p.isAlive() || !p.isThreadCuttable()) {
                this.discard();
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        Player cutter = attacker instanceof Player p ? p : null;
        if (cutter == null && !(source.getDirectEntity() instanceof Projectile)) {
            return false;
        }
        PuppetEntity p = this.puppet();
        return p != null && p.cutThread(level, cutter);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
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
