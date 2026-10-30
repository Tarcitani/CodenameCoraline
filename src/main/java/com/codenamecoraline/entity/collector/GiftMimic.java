package com.codenamecoraline.entity.collector;

import com.codenamecoraline.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Gift Mimic (Presente Faminto): a living, wrapped "pledge" shaped like a gift box with stubby legs and a lid that
 * snaps like a jaw. Called by the Collector it hunts players, never turns on its master and bursts into
 * confetti-shrapnel when its fuse runs out. Called by a player with the Renegade's Greataxe it is an <em>ally</em>:
 * it follows its owner, attacks hostile mobs (and the Collector), never touches players, and bursts on enemies.
 */
public class GiftMimic extends Monster implements com.codenamecoraline.entity.PlayerSummon {
    public static final int FUSE_TICKS = 240;
    public static final int ALLY_FUSE_TICKS = 600;
    public static final float BURST_DAMAGE = 7.0F;
    public static final float ALLY_BURST_DAMAGE = 9.0F;
    public static final double BURST_RADIUS = 2.7D;
    public static final int MAX_ALLIES = 4;

    private static final DustParticleOptions RED_WRAP = new DustParticleOptions(0xC7283A, 1.3F);
    private static final DustParticleOptions GOLD_RIBBON = new DustParticleOptions(0xE6B422, 1.3F);
    private static final DustParticleOptions GREEN_WRAP = new DustParticleOptions(0x2E8B57, 1.3F);

    private static final EntityDataAccessor<Integer> DATA_FUSE =
            SynchedEntityData.defineId(GiftMimic.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ALLIED =
            SynchedEntityData.defineId(GiftMimic.class, EntityDataSerializers.BOOLEAN);

    private @Nullable UUID ownerId;

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
        builder.define(DATA_ALLIED, false);
    }

    public boolean isAllied() {
        return this.entityData.get(DATA_ALLIED);
    }

    public @Nullable UUID getOwnerId() {
        return this.ownerId;
    }

    /** Makes this mimic an ally of the given owner (a player's UUID). */
    public void setOwner(@Nullable UUID owner) {
        this.ownerId = owner;
        this.entityData.set(DATA_ALLIED, owner != null);
    }

    private int fuseLimit() {
        return this.isAllied() ? ALLY_FUSE_TICKS : FUSE_TICKS;
    }

    /** 0..1 progress towards bursting; the model shakes and flashes as it approaches 1. */
    public float getFuseProgress() {
        return Math.min(1.0F, this.entityData.get(DATA_FUSE) / (float) this.fuseLimit());
    }

    @Override
    public boolean isPlayerSummon() {
        return this.isAllied();
    }

    /**
     * Never a target for an allied mimic: its owner, any player, its own side, every other player summon (such as the
     * Deadroot Club's macaques) and the owner's tamed pets.
     */
    private boolean isProtected(LivingEntity target) {
        if (target instanceof Player || target.getUUID().equals(this.ownerId)) {
            return true;
        }
        if (target instanceof com.codenamecoraline.entity.PlayerSummon summon && summon.isPlayerSummon()) {
            return true;
        }
        if (target instanceof GiftMimic other) {
            return other.isAllied();
        }
        return target instanceof net.minecraft.world.entity.TamableAnimal pet && pet.getOwnerReference() != null
                && this.ownerId != null && this.ownerId.equals(pet.getOwnerReference().getUUID());
    }

    /** Hostile things an ally attacks on its own initiative. */
    private boolean isEnemyOfAlly(LivingEntity target) {
        return target instanceof Enemy && !this.isProtected(target);
    }

    /** What the owner is fighting right now: whoever hurt them, otherwise whatever they last hit (animals included). */
    private @Nullable LivingEntity ownerFocus(Player owner) {
        LivingEntity attacker = owner.getLastHurtByMob();
        if (attacker != null && attacker.isAlive() && owner.tickCount - owner.getLastHurtByMobTimestamp() < 160
                && !this.isProtected(attacker)) {
            return attacker;
        }
        LivingEntity hit = owner.getLastHurtMob();
        if (hit != null && hit.isAlive() && owner.tickCount - owner.getLastHurtMobTimestamp() < 160
                && !this.isProtected(hit)) {
            return hit;
        }
        return null;
    }

    private void assistOwner(ServerLevel level) {
        Player owner = this.ownerId != null ? level.getPlayerByUUID(this.ownerId) : null;
        if (owner == null) {
            return;
        }
        LivingEntity focus = this.ownerFocus(owner);
        if (focus != null && focus != this.getTarget() && this.distanceToSqr(focus) < 48.0D * 48.0D) {
            this.setTarget(focus);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, true));
        this.goalSelector.addGoal(2, new FollowOwnerGoal());
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        // Hostile mimics hunt players; allied ones hunt anything hostile near them or their owner.
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> !this.isAllied()));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                (target, level) -> this.isAllied() && this.isEnemyOfAlly(target)));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof GiftMimic other) {
            return this.isAllied() != other.isAllied() && super.canAttack(target);
        }
        if (target instanceof LastGiftCollector) {
            return this.isAllied() && super.canAttack(target);
        }
        if (this.isAllied()) {
            return !this.isProtected(target) && super.canAttack(target);
        }
        return super.canAttack(target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // Friendly fire from its master (stomps, comets, presents) never harms a hostile mimic.
        if (!this.isAllied()
                && (source.getEntity() instanceof LastGiftCollector || source.getDirectEntity() instanceof LastGiftCollector)) {
            return false;
        }
        // An ally is never hurt by its owner, nor by another player summon.
        if (this.isAllied() && source.getEntity() != null && source.getEntity().getUUID().equals(this.ownerId)) {
            return false;
        }
        if (this.isAllied() && source.getEntity() instanceof com.codenamecoraline.entity.PlayerSummon summon
                && summon.isPlayerSummon()) {
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
            if (this.isAllied() && fuse % 8 == 0) {
                this.assistOwner(level);
            }
            if (fuse % 6 == 0) {
                level.sendParticles(fuse % 12 == 0 ? GOLD_RIBBON : RED_WRAP,
                        this.getX(), this.getY() + 0.9D, this.getZ(), 3, 0.3D, 0.3D, 0.3D, 0.01D);
            }
            if (fuse >= this.fuseLimit()) {
                this.burst(level);
            }
        }
    }

    /** The pledge comes due: confetti and shrapnel hit everyone it is against, then the mimic is gone. */
    public void burst(ServerLevel level) {
        if (!this.isAlive()) {
            return;
        }
        this.playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 1.6F, 0.7F);
        this.playSound(SoundEvents.FIREWORK_ROCKET_BLAST, 1.2F, 1.0F);
        confetti(level, this.position().add(0.0D, 0.8D, 0.0D), 36);
        AABB box = this.getBoundingBox().inflate(BURST_RADIUS);
        if (this.isAllied()) {
            Player owner = this.ownerId != null ? level.getPlayerByUUID(this.ownerId) : null;
            DamageSource source = owner != null ? this.damageSources().playerAttack(owner) : this.damageSources().mobAttack(this);
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != this && e.isAlive() && !this.isProtected(e)
                            && (this.isEnemyOfAlly(e) || e == this.getTarget() || (owner != null && e == this.ownerFocus(owner)))
                            && e.distanceToSqr(this) <= BURST_RADIUS * BURST_RADIUS)) {
                victim.hurtServer(level, source, ALLY_BURST_DAMAGE);
            }
        } else {
            for (Player player : level.getEntitiesOfClass(Player.class, box,
                    p -> p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) <= BURST_RADIUS * BURST_RADIUS)) {
                player.hurtServer(level, this.damageSources().mobAttack(this), BURST_DAMAGE);
            }
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

    /** Allied mimics currently alive for {@code owner}. */
    public static int countAllies(ServerLevel level, Player owner) {
        return level.getEntitiesOfClass(GiftMimic.class, owner.getBoundingBox().inflate(96.0D),
                m -> m.isAlive() && m.isAllied() && owner.getUUID().equals(m.getOwnerId())).size();
    }

    /** Spawns one allied mimic on a free spot between {@code minDist} and {@code maxDist} blocks from {@code around}. */
    public static boolean spawnAllied(ServerLevel level, Player owner, Vec3 around, double minDist, double maxDist) {
        GiftMimic mimic = ModEntities.GIFT_MIMIC.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mimic == null) {
            return false;
        }
        boolean placed = false;
        for (int attempt = 0; attempt < 10 && !placed; attempt++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0D;
            double dist = minDist + level.getRandom().nextDouble() * (maxDist - minDist);
            mimic.setPos(around.x + Math.cos(angle) * dist, around.y, around.z + Math.sin(angle) * dist);
            placed = level.noCollision(mimic);
        }
        if (!placed) {
            return false;
        }
        mimic.setOwner(owner.getUUID());
        mimic.finalizeSpawn(level, level.getCurrentDifficultyAt(mimic.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
        level.addFreshEntity(mimic);
        confetti(level, mimic.position().add(0.0D, 0.6D, 0.0D), 14);
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.ownerId != null) {
            output.putString("Owner", this.ownerId.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String owner = input.getStringOr("Owner", "");
        if (!owner.isEmpty()) {
            try {
                this.setOwner(UUID.fromString(owner));
            } catch (IllegalArgumentException ignored) {
                this.setOwner(null);
            }
        }
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
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isAllied();
    }

    /** Allied mimics stay near their owner and hurry back when left behind. */
    private final class FollowOwnerGoal extends Goal {
        FollowOwnerGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        private @Nullable Player owner() {
            UUID id = GiftMimic.this.ownerId;
            return id == null ? null : GiftMimic.this.level().getPlayerByUUID(id);
        }

        @Override
        public boolean canUse() {
            Player owner = this.owner();
            return GiftMimic.this.isAllied() && owner != null && GiftMimic.this.getTarget() == null
                    && GiftMimic.this.distanceToSqr(owner) > 64.0D;
        }

        @Override
        public boolean canContinueToUse() {
            Player owner = this.owner();
            return owner != null && GiftMimic.this.getTarget() == null && GiftMimic.this.distanceToSqr(owner) > 9.0D;
        }

        @Override
        public void tick() {
            Player owner = this.owner();
            if (owner == null) {
                return;
            }
            if (GiftMimic.this.distanceToSqr(owner) > 900.0D) {
                GiftMimic.this.setPos(owner.getX(), owner.getY(), owner.getZ()); // lost: pop back next to the owner
                GiftMimic.this.getNavigation().stop();
                return;
            }
            if (GiftMimic.this.getNavigation().isDone()) {
                GiftMimic.this.getNavigation().moveTo(owner, 1.25D);
            }
        }
    }

}
