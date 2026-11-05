package com.codenamecoraline.entity.ten_threads_witch;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Orb of Vital Spool: a mote of silver thread a defeated puppet leaves. It drifts to the nearest player and, on touch,
 * heals 4 HP (2 hearts), restores 3 hunger and repairs 15 durability of the main-hand and off-hand items. Drawn as
 * particles (the entity itself has no model), never saved, gone after 20 seconds.
 */
public class SpoolVitalOrbEntity extends Entity {
    public static final float HEAL = 4.0F;
    public static final int FOOD = 3;
    public static final int REPAIR = 15;
    public static final int LIFETIME = 400;

    public SpoolVitalOrbEntity(EntityType<? extends SpoolVitalOrbEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        ServerLevel level = (ServerLevel) this.level();
        Player p = level.getNearestPlayer(this, 40.0D);
        if (p != null && p.isAlive() && !p.isSpectator()) {
            Vec3 to = p.position().add(0.0D, p.getBbHeight() * 0.5D, 0.0D).subtract(this.position());
            double speed = Math.min(0.9D, 0.2D + this.tickCount * 0.03D);
            Vec3 step = to.lengthSqr() > 1.0E-4D ? to.normalize().scale(Math.min(speed, to.length())) : Vec3.ZERO;
            this.setPos(this.position().add(step));
            if (to.length() < 1.0D) {
                reward(p);
                this.discard();
                return;
            }
        }
        level.sendParticles(AttackRunner.SPOOL_DUST, this.getX(), this.getY(), this.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.0D);
        level.sendParticles(AttackRunner.THREAD_DUST, this.getX(), this.getY(), this.getZ(), 1, 0.15D, 0.15D, 0.15D, 0.0D);
        if (this.tickCount > LIFETIME) {
            this.discard();
        }
    }

    /** The reward of one orb (public for tests): heal, food and repair of both hands. */
    public static void reward(Player player) {
        player.heal(HEAL);
        player.getFoodData().eat(FOOD, 0.3F);
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.isDamageableItem() && stack.getDamageValue() > 0) {
                stack.setDamageValue(Math.max(0, stack.getDamageValue() - REPAIR));
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
