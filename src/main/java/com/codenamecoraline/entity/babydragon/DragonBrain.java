package com.codenamecoraline.entity.babydragon;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** One movement/combat arbiter prevents competing follow, orbit and combat goals. */
public final class DragonBrain {
    public static final int ATTACK_COOLDOWN = 30;
    private final BabyDragon dragon;
    private int attackCooldown, ambientCooldown = 100, flightTicks, repath, idleTicks, combatTicks;
    private int ownerHurtStamp = -1, ownerAttackStamp = -1, hurtStamp = -1;
    private double orbitAngle;
    private @Nullable Vec3 destination;

    DragonBrain(BabyDragon dragon) { this.dragon = dragon; this.orbitAngle = dragon.getId() * 2.39; }
    public int getAttackCooldown() { return this.attackCooldown; }
    public void setAttackCooldown(int ticks) { this.attackCooldown = Math.max(0, ticks); }
    public void delayAmbient(int ticks) { this.ambientCooldown = ticks; this.flightTicks = 0; }

    public void tick() {
        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.ambientCooldown > 0) this.ambientCooldown--;
        if (this.repath > 0) this.repath--;
        if (this.dragon.getRider() != null || this.dragon.getEvolutionTicks() > 0) { this.dragon.getNavigation().stop(); return; }
        LivingEntity owner = this.dragon.getOwner();
        this.selectReactiveTarget(owner);
        LivingEntity target = this.dragon.getTarget();
        if (!this.dragon.isDefensive() || this.dragon.isOrderedToSit() || !this.dragon.validDefensiveTarget(target)
                || (owner != null && target != null && target.distanceToSqr(owner) > 1600) || this.combatTicks > 240) {
            this.dragon.setTarget(null); this.dragon.cancelCharge(); target = null; this.combatTicks = 0;
        }
        if (this.dragon.isOnShoulder()) {
            if (target == null) return;
            this.dragon.leaveShoulder();
        }
        // Escape liquids before optional behaviour, even when ordered to rest.
        if (this.dragon.isInLiquid()) {
            this.dragon.setFlying(true); this.dragon.setDragonState(DragonState.RETURNING);
            this.moveTo(this.dragon.position().add(0, 2, 0)); return;
        }
        if (this.dragon.isOrderedToSit()) {
            this.dragon.setDragonState(DragonState.SITTING);
            this.land(); this.dragon.setInSittingPose(this.dragon.onGround()); return;
        }
        this.dragon.setInSittingPose(false);
        if (this.dragon.getHealth() <= 5 && owner != null && target != null) {
            this.dragon.cancelCharge(); this.dragon.setFlying(true); this.dragon.setDragonState(DragonState.RETURNING);
            this.moveTo(owner.position().add(0, 2, 0)); return;
        }
        if (target != null) { this.defend(target); return; }
        if (owner == null || !owner.isAlive() || owner.isSpectator()) {
            if (this.dragon.wantsShoulder()) this.dragon.leaveShoulder();
            this.wild(); return;
        }
        if (this.dragon.wantsShoulder() && owner instanceof Player player) {
            this.dragon.setDragonState(DragonState.RETURNING); this.dragon.setFlying(true);
            this.moveTo(BabyDragon.shoulderPosition(owner));
            if (this.dragon.distanceToSqr(owner) < 2.5) this.dragon.landOnShoulder(player);
            return;
        }
        if (this.dragon.tickCount % 20 == 0 && this.dragon.teleportNearOwner()) return;
        double distance = this.dragon.distanceToSqr(owner);
        boolean running = owner.getDeltaMovement().horizontalDistanceSqr() > 0.04 || distance > 64;
        if (running) {
            this.idleTicks = 0; this.flightTicks = Math.max(this.flightTicks, 60);
            this.dragon.setFlying(true); this.dragon.setDragonState(DragonState.FLYING);
            this.moveTo(owner.position().add(owner.getDeltaMovement().scale(4)).add(0, 1.5, 0)); return;
        }
        if (this.flightTicks > 0) {
            this.flightTicks--; this.orbit(owner); return;
        }
        if (this.dragon.isFlying()) { this.land(); return; }
        this.dragon.setDragonState(DragonState.FOLLOWING);
        if (distance > 16) { this.moveTo(owner.position()); this.idleTicks = 0; }
        else if (distance < 6.25) this.dragon.getNavigation().stop();
        if (distance <= 25 && ++this.idleTicks > 80 && this.ambientCooldown == 0 && !this.dragon.isLeashed()) {
            this.flightTicks = 140 + this.dragon.getRandom().nextInt(100); this.ambientCooldown = 300;
            this.idleTicks = 0; this.dragon.setFlying(true); this.orbit(owner);
        }
    }

    private void selectReactiveTarget(@Nullable LivingEntity owner) {
        if (!this.dragon.isDefensive() || this.dragon.isOrderedToSit()) return;
        LivingEntity candidate = null;
        if (owner != null) {
            if (owner.getLastHurtByMobTimestamp() != this.ownerHurtStamp) {
                this.ownerHurtStamp = owner.getLastHurtByMobTimestamp();
                if (BabyDragon.recent(owner, this.ownerHurtStamp)) candidate = owner.getLastHurtByMob();
            }
            if (candidate == null && owner.getLastHurtMobTimestamp() != this.ownerAttackStamp) {
                this.ownerAttackStamp = owner.getLastHurtMobTimestamp();
                if (BabyDragon.recent(owner, this.ownerAttackStamp)) candidate = owner.getLastHurtMob();
            }
        }
        if (candidate == null && this.dragon.getLastHurtByMobTimestamp() != this.hurtStamp) {
            this.hurtStamp = this.dragon.getLastHurtByMobTimestamp();
            if (BabyDragon.recent(this.dragon, this.hurtStamp)) candidate = this.dragon.getLastHurtByMob();
        }
        // Hatchlings retain reactive-only defence; grown companions also intercept nearby hostiles.
        if (candidate == null && this.dragon.getDragonStage() != DragonStage.BABY && owner != null && this.dragon.getTarget() == null && this.dragon.tickCount % 20 == 0) {
            candidate = this.dragon.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, owner.getBoundingBox().inflate(12),
                    e -> e instanceof net.minecraft.world.entity.monster.Enemy && this.dragon.validDefensiveTarget(e) && this.dragon.hasLineOfSight(e))
                    .stream().min(java.util.Comparator.comparingDouble(this.dragon::distanceToSqr)).orElse(null);
        }
        if (this.dragon.validDefensiveTarget(candidate) && this.dragon.distanceToSqr(candidate) <= 576) {
            this.dragon.setTarget(candidate); this.combatTicks = 0;
        }
    }

    private void defend(LivingEntity target) {
        this.combatTicks++;
        this.dragon.aimAt(target.getBoundingBox().getCenter());
        this.dragon.setDragonState(DragonState.DEFENDING); this.dragon.setFlying(true);
        this.orbitAngle += 0.025;
        Vec3 point = target.position().add(Math.cos(this.orbitAngle) * 5, 2 + 0.3 * Math.sin(this.orbitAngle * 1.7), Math.sin(this.orbitAngle) * 5);
        this.moveTo(point); this.dragon.getLookControl().setLookAt(target, 30, 30);
        double range = this.dragon.getDragonStage().range;
        if (this.dragon.distanceToSqr(target) > range * range || !this.dragon.getSensing().hasLineOfSight(target)) { this.dragon.cancelCharge(); this.dragon.cancelBreath(); return; }
        if (this.dragon.getBreathTicks() > 0 || this.dragon.getRecoveryTicks() > 0) return;
        if (this.dragon.distanceTo(target) < this.dragon.getBbWidth() + 1.2 && this.dragon.beginMelee(target)) return;
        if (this.dragon.getDragonStage() == DragonStage.ADULT && this.dragon.distanceToSqr(target) < 225 && this.dragon.beginBreath()) return;
        if (this.dragon.getChargeTicks() > 0) {
            if (this.dragon.chargeReady()) { this.dragon.shoot(target); this.attackCooldown = this.dragon.getDragonStage().fireCooldown; }
        } else if (this.attackCooldown == 0) this.dragon.beginCharge();
    }

    private void orbit(LivingEntity owner) {
        this.dragon.setDragonState(DragonState.ORBITING); this.dragon.setFlying(true);
        this.orbitAngle += 0.032;
        double radius = 2.8 + Math.sin(this.orbitAngle * 1.7) * 0.65;
        Vec3 point = owner.position().add(Math.cos(this.orbitAngle) * radius,
                1.9 + Math.sin(this.orbitAngle * 1.3) * 0.5, Math.sin(this.orbitAngle) * radius);
        this.moveTo(point); this.dragon.getLookControl().setLookAt(owner, 20, 20);
    }
    private void wild() {
        this.dragon.setDragonState(this.dragon.isFlying() ? DragonState.FLYING : DragonState.WILD_IDLE);
        if (this.dragon.isFlying()) {
            if (--this.flightTicks <= 0) this.land(); else if (this.destination != null) this.moveTo(this.destination);
        } else if (this.ambientCooldown == 0) {
            this.ambientCooldown = 140 + this.dragon.getRandom().nextInt(160);
            Vec3 pos = DefaultRandomPos.getPos(this.dragon, 5, 2);
            if (pos != null) {
                if (this.dragon.getRandom().nextInt(3) == 0 && this.dragon.clearBlocks(pos.add(0, 2, 0))) {
                    this.destination = pos.add(0, 2, 0); this.flightTicks = 70; this.dragon.setFlying(true);
                } else this.dragon.getNavigation().moveTo(pos.x, pos.y, pos.z, 1);
            }
        }
    }
    private void land() {
        if (!this.dragon.isFlying()) { this.dragon.getNavigation().stop(); return; }
        BlockPos floor = this.dragon.blockPosition();
        for (int dy = 0; dy < 10; dy++) {
            Vec3 ground = Vec3.atBottomCenterOf(floor.below(dy));
            if (this.dragon.safeGround(ground)) {
                this.moveTo(ground);
                if (this.dragon.position().distanceToSqr(ground) < 0.4 || this.dragon.onGround()) {
                    this.dragon.setFlying(false); this.dragon.getNavigation().stop();
                }
                return;
            }
        }
        this.flightTicks = 40;
        LivingEntity owner = this.dragon.getOwner();
        if (owner != null) this.moveTo(owner.position().add(0, 1, 0));
    }
    private void moveTo(Vec3 pos) {
        if (this.repath > 0) return;
        this.repath = 8;
        if (!this.dragon.clearBlocks(pos)) {
            Vec3 raised = pos.add(0, 1, 0);
            if (!this.dragon.clearBlocks(raised)) return;
            pos = raised;
        }
        this.dragon.getNavigation().moveTo(pos.x, pos.y, pos.z, 1.0);
    }
}
