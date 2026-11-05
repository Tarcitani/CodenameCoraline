package com.codenamecoraline.entity.babydragon;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** One movement/combat arbiter prevents competing follow, orbit and combat goals. */
public final class DragonBrain {
    public static final int ATTACK_COOLDOWN = 30;
    private final BabyDragon dragon;
    private int attackCooldown, ambientCooldown = 100, flightTicks, repath, idleTicks, combatTicks, noSightTicks;
    private int ownerHurtStamp = -1, ownerAttackStamp = -1, hurtStamp = -1;
    private int orbitDirection = 1;
    private double orbitAngle;
    private @Nullable Vec3 destination;
    private @Nullable LivingEntity huntTarget;
    private @Nullable UUID lastHuntedPreyUuid;
    private @Nullable Vec3 lastHuntedPreyPos;
    private int huntCooldown = 200, huntTicks, postKillCollectTicks, carryTicks;
    private boolean waterHunting, resurfacingFromHunt;
    private @Nullable BabyDragon socialPartner;
    private int socialTicks, socialCooldown;

    DragonBrain(BabyDragon dragon) { this.dragon = dragon; this.orbitAngle = dragon.getId() * 2.39; }
    public int getAttackCooldown() { return this.attackCooldown; }
    public void setAttackCooldown(int ticks) { this.attackCooldown = Math.max(0, ticks); }
    public void delayAmbient(int ticks) { this.ambientCooldown = ticks; this.flightTicks = 0; }
    public int getCombatTicks() { return this.combatTicks; }
    public int getNoSightTicks() { return this.noSightTicks; }
    public int getOrbitDirection() { return this.orbitDirection; }
    public int getHuntCooldown() { return this.huntCooldown; }
    public void setHuntCooldown(int ticks) { this.huntCooldown = Math.max(0, ticks); }
    public @Nullable LivingEntity getActiveHuntPrey() { return this.huntTarget; }
    public boolean isHuntingPrey(@Nullable LivingEntity entity) { return entity != null && entity == this.huntTarget; }
    public boolean isSocialActive() { return this.socialTicks > 0 && this.socialPartner != null; }
    public @Nullable BabyDragon getSocialPartner() { return this.socialPartner; }
    public int getSocialTicks() { return this.socialTicks; }
    public void setSocialCooldown(int ticks) { this.socialCooldown = Math.max(0, ticks); }

    public void tick() {
        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.ambientCooldown > 0) this.ambientCooldown--;
        if (this.repath > 0) this.repath--;
        if (this.huntCooldown > 0) this.huntCooldown--;
        if (this.socialCooldown > 0) this.socialCooldown--;
        if (this.dragon.getRider() != null || this.dragon.getEvolutionTicks() > 0) {
            this.abortSecondaryActivities();
            this.dragon.getNavigation().stop();
            return;
        }
        if (this.dragon.hurtTime > 0) this.clearSocialState();
        LivingEntity owner = this.dragon.getOwner();

        // Priority 1: Entity emergencies (lava, real drowning, or unintended water immersion).
        if (this.isLiquidEmergency()) {
            this.abortSecondaryActivities();
            if (this.dragon.isOnShoulder()) this.dragon.leaveShoulder();
            this.dragon.cancelCharge(); this.dragon.cancelBreath();
            this.dragon.setFlying(true); this.dragon.setDragonState(DragonState.RETURNING);
            this.steerDirect(this.dragon.position().add(0, 2.5, 0), 1.25); return;
        }

        // Priority 2: Owner fall rescue (adult companion catches falling owner mid-air).
        if (this.tryRescueFallingOwner(owner)) {
            this.abortSecondaryActivities();
            return;
        }

        // Target acquisition and non-arbitrary retention for Priorities 3 & 4.
        this.selectReactiveTarget(owner);
        LivingEntity target = this.dragon.getTarget();
        if (target != null) {
            this.abortSecondaryActivities();
            if (this.dragon.getSensing().hasLineOfSight(target)) this.noSightTicks = 0;
            else this.noSightTicks++;
        } else {
            this.noSightTicks = 0;
        }
        boolean invalid = !this.dragon.isDefensive() || !this.dragon.validDefensiveTarget(target);
        boolean outOfLeash = false;
        if (target != null) {
            if (this.dragon.isOrderedToSit()) {
                Vec3 anchor = this.dragon.getStayAnchor() != null ? this.dragon.getStayAnchor() : this.dragon.position();
                outOfLeash = target.distanceToSqr(anchor) > 100.0;
            } else if (owner != null) {
                outOfLeash = target.distanceToSqr(owner) > 1600.0;
            }
        }
        int maxNoSight = Math.max(100, Math.round(160 * this.dragon.getIntelligence()));
        if (invalid || outOfLeash || this.noSightTicks > maxNoSight) {
            this.dragon.setTarget(null); this.dragon.cancelCharge(); this.dragon.cancelBreath();
            target = null; this.combatTicks = 0; this.noSightTicks = 0;
        }

        if (this.dragon.isOnShoulder()) {
            this.abortSecondaryActivities();
            if (target == null || this.dragon.isReturnRequested()) return;
            this.dragon.leaveShoulder();
        }

        // Priority 3: Explicit commands (RETURN and STAY).
        if (this.dragon.isReturnRequested()) {
            this.abortSecondaryActivities();
            if (owner == null || !owner.isAlive() || owner.isSpectator()) {
                this.dragon.clearReturnRequest();
            } else {
                this.dragon.setInSittingPose(false);
                this.dragon.setDragonState(DragonState.RETURNING); this.dragon.setFlying(true);
                this.moveTo(owner.position().add(0, 1.5, 0));
                if (target != null) this.engageInTransit(target);
                if (this.dragon.distanceToSqr(owner) <= 9.0) {
                    this.dragon.clearReturnRequest();
                    this.dragon.setTarget(null); this.dragon.cancelCharge();
                }
                return;
            }
        }
        if (this.dragon.isOrderedToSit()) {
            this.abortSecondaryActivities();
            Vec3 anchor = this.dragon.getStayAnchor() != null ? this.dragon.getStayAnchor() : this.dragon.position();
            if (this.dragon.getStayAnchor() == null) this.dragon.setStayAnchor(anchor);
            if (target != null) {
                this.dragon.setInSittingPose(false);
                this.defend(target);
                return;
            }
            if (this.dragon.position().distanceToSqr(anchor) > 1.25) {
                this.dragon.setInSittingPose(false);
                this.dragon.setDragonState(DragonState.RETURNING);
                if (this.dragon.position().distanceToSqr(anchor) > 4.0) this.dragon.setFlying(true);
                this.moveTo(anchor);
                return;
            }
            this.dragon.setDragonState(DragonState.SITTING);
            this.land(); this.dragon.setInSittingPose(this.dragon.onGround()); return;
        }
        this.dragon.setInSittingPose(false);

        // Priority 4: Combat & tactical survival retreat.
        if (this.dragon.getHealth() <= 5.0F * this.dragon.getIntelligence() && owner != null && target != null) {
            this.abortSecondaryActivities();
            this.dragon.cancelCharge(); this.dragon.cancelBreath(); this.dragon.setFlying(true); this.dragon.setDragonState(DragonState.RETURNING);
            this.moveTo(owner.position().add(0, 2, 0)); return;
        }
        if (target != null) { this.abortSecondaryActivities(); this.defend(target); return; }

        if (owner == null || !owner.isAlive() || owner.isSpectator()) {
            this.abortSecondaryActivities();
            if (this.dragon.wantsShoulder()) this.dragon.leaveShoulder();
            this.wild(); return;
        }
        if (this.dragon.wantsShoulder() && owner instanceof Player player) {
            this.abortSecondaryActivities();
            this.dragon.setDragonState(DragonState.RETURNING); this.dragon.setFlying(true);
            this.moveTo(BabyDragon.shoulderPosition(owner));
            if (this.dragon.distanceToSqr(owner) < 2.5) this.dragon.landOnShoulder(player);
            return;
        }

        // Priority 5: Autonomous hunting (land/aquatic prey when safe and near owner).
        if (this.tickHunting(owner)) {
            this.clearSocialState();
            return;
        }

        // Priority 6: Follow owner.
        if (this.dragon.tickCount % 20 == 0 && this.dragon.teleportNearOwner()) {
            this.clearSocialState();
            return;
        }
        double distance = this.dragon.distanceToSqr(owner);
        boolean running = owner.getDeltaMovement().horizontalDistanceSqr() > 0.04 || distance > 64;
        if (running) {
            this.clearSocialState();
            this.idleTicks = 0; this.flightTicks = Math.max(this.flightTicks, 60);
            this.dragon.setFlying(true); this.dragon.setDragonState(DragonState.FLYING);
            this.moveTo(owner.position().add(owner.getDeltaMovement().scale(4)).add(0, 1.5, 0)); return;
        }
        if (this.flightTicks > 0) {
            this.clearSocialState();
            this.flightTicks--; this.orbit(owner); return;
        }
        if (this.dragon.isFlying()) { this.land(); return; }
        this.dragon.setDragonState(DragonState.FOLLOWING);
        if (distance > 36) { this.clearSocialState(); this.moveTo(owner.position()); this.idleTicks = 0; return; }

        // Priority 7: Social interactions between dragons of the same owner.
        if (this.tickSocial(owner)) return;

        if (distance > 16) { this.moveTo(owner.position()); this.idleTicks = 0; return; }
        else if (distance < 6.25) this.dragon.getNavigation().stop();

        // Priority 8: Idle / ambient orbital flight.
        if (distance <= 25 && ++this.idleTicks > 80 && this.ambientCooldown == 0 && !this.dragon.isLeashed()) {
            this.flightTicks = 140 + this.dragon.getRandom().nextInt(100); this.ambientCooldown = 300;
            this.idleTicks = 0; this.dragon.setFlying(true); this.orbit(owner);
        }
    }

    private boolean isLiquidEmergency() {
        if (this.dragon.isInLava() || this.dragon.getAirSupply() < 60) return true;
        return this.dragon.isInLiquid() && !this.isIntentionallyInWater();
    }

    public boolean isIntentionallyInWater() {
        return (this.waterHunting || this.resurfacingFromHunt) && !this.dragon.isInLava() && this.dragon.getAirSupply() >= 60;
    }

    public void abortSecondaryActivities() {
        this.clearHuntState();
        this.clearSocialState();
    }

    public void clearHuntState() {
        this.huntTarget = null;
        this.waterHunting = false;
        this.resurfacingFromHunt = false;
        this.postKillCollectTicks = 0;
        this.huntTicks = 0;
    }

    public void clearSocialState() {
        BabyDragon partner = this.socialPartner;
        this.socialPartner = null;
        this.socialTicks = 0;
        if (partner != null && partner.getDragonBrain().socialPartner == this.dragon) {
            partner.getDragonBrain().socialPartner = null;
            partner.getDragonBrain().socialTicks = 0;
        }
    }

    private void steerDirect(Vec3 targetPos, double speedModifier) {
        this.dragon.getNavigation().stop();
        this.dragon.getMoveControl().setWantedPosition(targetPos.x, targetPos.y, targetPos.z, speedModifier);
    }

    private Vec3 findWaterSurfaceAbove(Vec3 from) {
        BlockPos base = BlockPos.containing(from);
        for (int dy = 1; dy <= 12; dy++) {
            BlockPos check = base.above(dy);
            if (this.dragon.level().getFluidState(check).isEmpty()) {
                return new Vec3(from.x, check.getY() + 0.6, from.z);
            }
        }
        return from.add(0, 3.0, 0);
    }

    public static boolean isPreyFoodItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.has(DataComponents.FOOD)) return true;
        return stack.is(Items.CHICKEN) || stack.is(Items.COOKED_CHICKEN)
                || stack.is(Items.PORKCHOP) || stack.is(Items.COOKED_PORKCHOP)
                || stack.is(Items.BEEF) || stack.is(Items.COOKED_BEEF)
                || stack.is(Items.MUTTON) || stack.is(Items.COOKED_MUTTON)
                || stack.is(Items.RABBIT) || stack.is(Items.COOKED_RABBIT)
                || stack.is(Items.COD) || stack.is(Items.COOKED_COD)
                || stack.is(Items.SALMON) || stack.is(Items.COOKED_SALMON)
                || stack.is(Items.TROPICAL_FISH);
    }

    private static float getFoodHealAmount(ItemStack stack) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        return food != null ? Math.max(4.0F, food.nutrition() * 1.25F) : 6.0F;
    }

    private static boolean isInWaterOrFluid(LivingEntity entity) {
        return entity.isInWater() || entity.level().getFluidState(entity.blockPosition()).is(net.minecraft.tags.FluidTags.WATER);
    }

    public boolean isValidLandPrey(@Nullable LivingEntity entity, LivingEntity owner) {
        if (entity == null || !entity.isAlive() || isInWaterOrFluid(entity) || entity.hasCustomName()
                || this.dragon.isProtected(entity) || entity.isPassenger() || entity.getMaxHealth() > 20.0F
                || !this.dragon.hasLineOfSight(entity)) return false;
        if (entity instanceof Mob mob && mob.isLeashed()) return false;
        if (entity instanceof TamableAnimal tamable && tamable.isTame()) return false;
        if (entity instanceof Animal animal && animal.isBaby()) return false;
        EntityType<?> type = entity.getType();
        if (type != EntityType.CHICKEN && type != EntityType.RABBIT && type != EntityType.PIG
                && type != EntityType.SHEEP && type != EntityType.COW) return false;
        double maxRange = 10.0 * this.dragon.getIntelligence();
        return this.dragon.distanceToSqr(entity) <= maxRange * maxRange && entity.distanceToSqr(owner) <= 100.0;
    }

    public boolean isValidAquaticPrey(@Nullable LivingEntity entity, LivingEntity owner) {
        if (entity == null || !entity.isAlive() || !isInWaterOrFluid(entity) || entity.hasCustomName()
                || this.dragon.isProtected(entity) || entity.isPassenger() || this.dragon.getAirSupply() < 120
                || !this.dragon.hasLineOfSight(entity)) return false;
        if (entity instanceof Mob mob && mob.isLeashed()) return false;
        EntityType<?> type = entity.getType();
        if (type != EntityType.COD && type != EntityType.SALMON && type != EntityType.TROPICAL_FISH) return false;
        double maxRange = 10.0 * this.dragon.getIntelligence();
        return this.dragon.distanceToSqr(entity) <= maxRange * maxRange && entity.distanceToSqr(owner) <= 100.0;
    }

    private boolean tryCollectPreyDrop() {
        if (!this.dragon.getCarriedPreyItem().isEmpty() || this.lastHuntedPreyPos == null) return false;
        if (this.dragon.position().distanceToSqr(this.lastHuntedPreyPos) > 16.0) return false;
        AABB searchBox = new AABB(this.lastHuntedPreyPos, this.lastHuntedPreyPos).inflate(3.5);
        ItemEntity bestDrop = this.dragon.level().getEntitiesOfClass(ItemEntity.class, searchBox,
                item -> item.isAlive() && item.getAge() <= 60 && isPreyFoodItem(item.getItem()))
                .stream()
                .min(Comparator.comparingDouble(item -> item.position().distanceToSqr(this.lastHuntedPreyPos)))
                .orElse(null);
        if (bestDrop == null) return false;
        ItemStack captured = bestDrop.getItem().copy();
        bestDrop.discard();
        this.dragon.setCarriedPreyItem(captured);
        this.carryTicks = 0;
        this.postKillCollectTicks = 0;
        this.lastHuntedPreyUuid = null;
        this.lastHuntedPreyPos = null;
        this.waterHunting = false;
        this.resurfacingFromHunt = isInWaterOrFluid(this.dragon);
        return true;
    }

    protected boolean tickHunting(LivingEntity owner) {
        if (this.dragon.isLeashed()) {
            this.clearHuntState();
            return false;
        }
        // Phase A: Resurfacing or eating/delivering an already captured prey item.
        if (!this.dragon.getCarriedPreyItem().isEmpty()) {
            this.carryTicks++;
            this.waterHunting = false;
            if (isInWaterOrFluid(this.dragon)) {
                this.resurfacingFromHunt = true;
                this.dragon.setFlying(true);
                this.dragon.setDragonState(DragonState.RETURNING);
                Vec3 surface = this.findWaterSurfaceAbove(this.dragon.position());
                this.steerDirect(surface, 1.3);
                return true;
            }
            this.resurfacingFromHunt = false;
            if (this.dragon.getHealth() < this.dragon.getMaxHealth()) {
                this.dragon.setDragonState(DragonState.FOLLOWING);
                if (this.carryTicks >= 8) {
                    ItemStack carried = this.dragon.getCarriedPreyItem().copy();
                    float healAmount = getFoodHealAmount(carried);
                    carried.shrink(1);
                    this.dragon.setCarriedPreyItem(carried);
                    this.dragon.heal(healAmount);
                    this.dragon.happy();
                    this.carryTicks = 0;
                    this.huntCooldown = Math.max(120, Math.round(200 / this.dragon.getIntelligence()));
                }
                return true;
            }
            this.dragon.setDragonState(DragonState.RETURNING);
            this.dragon.setFlying(true);
            this.steerDirect(owner.position().add(0, 1.2, 0), 1.2);
            this.dragon.getLookControl().setLookAt(owner, 30, 30);
            if (this.dragon.distanceToSqr(owner) <= 9.0 && this.carryTicks >= 8) {
                ItemStack toDeliver = this.dragon.getCarriedPreyItem().copy();
                this.dragon.setCarriedPreyItem(ItemStack.EMPTY);
                this.carryTicks = 0;
                if (owner instanceof Player player) {
                    if (!player.getInventory().add(toDeliver) && this.dragon.level() instanceof ServerLevel server) {
                        ItemEntity dropped = owner.spawnAtLocation(server, toDeliver);
                        if (dropped != null) dropped.setNoPickUpDelay();
                    }
                } else if (this.dragon.level() instanceof ServerLevel server) {
                    owner.spawnAtLocation(server, toDeliver);
                }
                this.dragon.happy();
                this.huntCooldown = Math.max(160, Math.round(260 / this.dragon.getIntelligence()));
            }
            return true;
        }
        if (this.resurfacingFromHunt) {
            if (isInWaterOrFluid(this.dragon)) {
                this.dragon.setFlying(true);
                this.dragon.setDragonState(DragonState.RETURNING);
                this.steerDirect(this.findWaterSurfaceAbove(this.dragon.position()), 1.3);
                return true;
            }
            this.resurfacingFromHunt = false;
        }

        // Phase B: Collecting the food drop from a just-killed hunted prey.
        if (this.postKillCollectTicks > 0) {
            this.postKillCollectTicks--;
            if (this.tryCollectPreyDrop()) return true;
            if (this.lastHuntedPreyPos != null && this.postKillCollectTicks > 0) {
                this.dragon.setFlying(true);
                this.dragon.setDragonState(DragonState.FLYING);
                this.steerDirect(this.lastHuntedPreyPos, 1.25);
                return true;
            }
            this.clearHuntState();
            return false;
        }

        // Phase C: Pursuing active prey or scanning for new prey when safe.
        if (this.huntTarget != null) {
            if (!this.huntTarget.isAlive()) {
                this.huntTarget = null;
                if (this.tryCollectPreyDrop()) return true;
                this.postKillCollectTicks = 40;
                return true;
            }
            boolean aquatic = this.isValidAquaticPrey(this.huntTarget, owner) || isInWaterOrFluid(this.huntTarget);
            if (++this.huntTicks > 260 || this.huntTarget.distanceToSqr(owner) > 32.0 * 32.0
                    || this.dragon.isProtected(this.huntTarget) || (aquatic && this.dragon.getAirSupply() < 80)) {
                boolean wasInWater = isInWaterOrFluid(this.dragon);
                this.clearHuntState();
                this.resurfacingFromHunt = wasInWater;
                this.huntCooldown = 80;
                return wasInWater;
            }
            this.lastHuntedPreyUuid = this.huntTarget.getUUID();
            this.lastHuntedPreyPos = this.huntTarget.position();
            this.waterHunting = aquatic;
            this.dragon.setFlying(true);
            this.dragon.setDragonState(DragonState.FLYING);
            this.dragon.aimAt(this.huntTarget.getBoundingBox().getCenter());
            Vec3 approach = aquatic ? this.huntTarget.position() : this.huntTarget.position().add(0, 0.5, 0);
            this.steerDirect(approach, aquatic ? 1.3 : 1.2);
            double dist = this.dragon.distanceTo(this.huntTarget);
            if (dist <= this.dragon.getBbWidth() + 1.8) {
                if (this.attackCooldown == 0) {
                    this.dragon.beginMelee(this.huntTarget);
                    float biteDamage = Math.max(4.0F, (float) this.dragon.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    if (aquatic) {
                        this.dragon.damageEnemy(this.huntTarget, biteDamage);
                    } else {
                        this.dragon.damageEnemyWithFire(this.huntTarget, biteDamage, 4);
                    }
                    this.attackCooldown = 16;
                }
            } else if (!aquatic && dist <= this.dragon.getDragonStage().range && this.dragon.getSensing().hasLineOfSight(this.huntTarget)) {
                if (this.dragon.getChargeTicks() > 0) {
                    if (this.dragon.chargeReady()) {
                        this.dragon.shoot(this.huntTarget);
                        this.attackCooldown = this.dragon.getDragonStage().fireCooldown;
                    }
                } else if (this.attackCooldown == 0 && this.dragon.getRecoveryTicks() == 0) {
                    this.dragon.beginCharge();
                }
            }
            if (!this.huntTarget.isAlive()) {
                this.lastHuntedPreyPos = this.huntTarget.position();
                this.huntTarget = null;
                if (!this.tryCollectPreyDrop()) this.postKillCollectTicks = 40;
            }
            return true;
        }

        if (this.huntCooldown > 0 || this.dragon.getHealth() <= 6.0F * this.dragon.getIntelligence()
                || this.dragon.distanceToSqr(owner) > 24.0 * 24.0) {
            return false;
        }
        double scanRadius = 10.0 * this.dragon.getIntelligence();
        Mob candidate = this.dragon.level().getEntitiesOfClass(Mob.class, this.dragon.getBoundingBox().inflate(scanRadius),
                e -> this.isValidLandPrey(e, owner) || this.isValidAquaticPrey(e, owner))
                .stream()
                .min(Comparator.comparingDouble(this.dragon::distanceToSqr))
                .orElse(null);
        if (candidate == null) return false;
        this.huntTarget = candidate;
        this.huntTicks = 0;
        this.lastHuntedPreyUuid = candidate.getUUID();
        this.lastHuntedPreyPos = candidate.position();
        this.waterHunting = this.isValidAquaticPrey(candidate, owner);
        return true;
    }

    public boolean isSocialEligible() {
        return this.dragon.isAlive() && this.dragon.isTame() && !this.dragon.isOrderedToSit()
                && !this.dragon.isReturnRequested() && !this.dragon.isOnShoulder() && !this.dragon.wantsShoulder()
                && this.dragon.getRider() == null && this.dragon.getTarget() == null
                && this.dragon.hurtTime == 0 && !this.dragon.isDragonSleeping()
                && this.huntTarget == null && this.postKillCollectTicks == 0
                && this.dragon.getCarriedPreyItem().isEmpty() && !this.dragon.isInLiquid();
    }

    private void startSocialWith(BabyDragon partner, int duration) {
        this.socialPartner = partner;
        this.socialTicks = duration;
        this.idleTicks = 0;
        this.dragon.happy();
    }

    protected boolean tickSocial(LivingEntity owner) {
        if (!this.isSocialEligible() || this.dragon.isFlying()) {
            this.clearSocialState();
            return false;
        }
        if (this.socialTicks > 0 && this.socialPartner != null) {
            if (!this.socialPartner.isAlive() || !this.socialPartner.getDragonBrain().isSocialEligible()
                    || !Objects.equals(this.dragon.getOwnerReference(), this.socialPartner.getOwnerReference())
                    || this.dragon.distanceToSqr(this.socialPartner) > 64.0) {
                this.clearSocialState();
                return false;
            }
            this.dragon.setDragonState(DragonState.FOLLOWING);
            this.dragon.getLookControl().setLookAt(this.socialPartner, 30, 30);
            double distSqr = this.dragon.distanceToSqr(this.socialPartner);
            if (distSqr > 5.0) {
                this.dragon.getNavigation().moveTo(this.socialPartner, 1.0);
            } else {
                this.dragon.getNavigation().stop();
                if (this.socialTicks % 24 == 0) this.dragon.happy();
                if (this.socialTicks % 20 == 10 && this.dragon.onGround()) {
                    Vec3 tangent = this.socialPartner.position().subtract(this.dragon.position()).cross(new Vec3(0, 1, 0)).normalize().scale(0.10);
                    this.dragon.setDeltaMovement(tangent.x, 0.16, tangent.z);
                }
            }
            if (--this.socialTicks == 0) {
                this.socialPartner = null;
                this.socialCooldown = 200;
            }
            return true;
        }
        if (this.socialCooldown > 0) return false;
        BabyDragon partner = this.dragon.level().getEntitiesOfClass(BabyDragon.class, this.dragon.getBoundingBox().inflate(8.0),
                other -> other != this.dragon
                        && Objects.equals(this.dragon.getOwnerReference(), other.getOwnerReference())
                        && !other.isFlying()
                        && other.getDragonBrain().isSocialEligible()
                        && other.getDragonBrain().socialTicks == 0
                        && other.getDragonBrain().socialCooldown == 0)
                .stream()
                .min(Comparator.comparingDouble(this.dragon::distanceToSqr))
                .orElse(null);
        if (partner == null) return false;
        int duration = 60;
        this.startSocialWith(partner, duration);
        partner.getDragonBrain().startSocialWith(this.dragon, duration);
        return true;
    }

    private boolean tryRescueFallingOwner(@Nullable LivingEntity owner) {
        if (!(owner instanceof Player player) || !owner.isAlive() || owner.isSpectator() || this.dragon.isOrderedToSit()
                || this.dragon.getDragonStage() != DragonStage.ADULT || !this.dragon.canAddPassenger(player)) return false;
        if (owner.fallDistance < 5.0F || owner.onGround() || owner.isFallFlying() || owner.isInLiquid() || owner.isPassenger()
                || this.dragon.distanceToSqr(owner) > 48 * 48) return false;
        this.dragon.setTarget(null); this.dragon.cancelCharge(); this.dragon.cancelBreath();
        this.dragon.setFlying(true); this.dragon.setDragonState(DragonState.RETURNING);
        Vec3 intercept = owner.position().add(owner.getDeltaMovement().scale(2.0));
        this.repath = 0;
        this.moveTo(intercept);
        if (this.dragon.distanceToSqr(owner) <= 16.0 && this.dragon.mountOwner(player)) {
            owner.fallDistance = 0.0F;
            this.dragon.setFlying(true);
        }
        return true;
    }

    private void selectReactiveTarget(@Nullable LivingEntity owner) {
        if (!this.dragon.isDefensive()) return;
        if (this.dragon.isOrderedToSit()) {
            Vec3 anchor = this.dragon.getStayAnchor() != null ? this.dragon.getStayAnchor() : this.dragon.position();
            LivingEntity stayThreat = null;
            if (this.dragon.getLastHurtByMob() != null && this.dragon.getLastHurtByMobTimestamp() != this.hurtStamp) {
                this.hurtStamp = this.dragon.getLastHurtByMobTimestamp();
                if (BabyDragon.recent(this.dragon, this.hurtStamp)) stayThreat = this.dragon.getLastHurtByMob();
            }
            if (stayThreat == null && this.dragon.getTarget() == null && this.dragon.tickCount % 10 == 0) {
                stayThreat = this.dragon.level().getEntitiesOfClass(Mob.class, new AABB(anchor, anchor).inflate(5.0),
                        e -> e instanceof Enemy && this.dragon.validDefensiveTarget(e) && this.dragon.hasLineOfSight(e))
                        .stream().min(Comparator.comparingDouble(this.dragon::distanceToSqr)).orElse(null);
            }
            if (this.dragon.validDefensiveTarget(stayThreat) && stayThreat.distanceToSqr(anchor) <= 100.0) {
                this.dragon.setTarget(stayThreat); this.combatTicks = 0; this.noSightTicks = 0;
            }
            return;
        }
        LivingEntity candidate = null;
        if (owner != null) {
            if (owner.getLastHurtByMob() != null && owner.getLastHurtByMobTimestamp() != this.ownerHurtStamp) {
                this.ownerHurtStamp = owner.getLastHurtByMobTimestamp();
                if (BabyDragon.recent(owner, this.ownerHurtStamp)) candidate = owner.getLastHurtByMob();
            }
            if (candidate == null && owner.getLastHurtMob() != null && owner.getLastHurtMobTimestamp() != this.ownerAttackStamp) {
                this.ownerAttackStamp = owner.getLastHurtMobTimestamp();
                if (BabyDragon.recent(owner, this.ownerAttackStamp)) candidate = owner.getLastHurtMob();
            }
        }
        if (candidate == null && this.dragon.getLastHurtByMob() != null && this.dragon.getLastHurtByMobTimestamp() != this.hurtStamp) {
            this.hurtStamp = this.dragon.getLastHurtByMobTimestamp();
            if (BabyDragon.recent(this.dragon, this.hurtStamp)) candidate = this.dragon.getLastHurtByMob();
        }
        // Hatchlings retain reactive-only defence; grown companions also intercept nearby hostiles using tactical threat scoring.
        if (candidate == null && this.dragon.getDragonStage() != DragonStage.BABY && owner != null && this.dragon.getTarget() == null && this.dragon.tickCount % 20 == 0) {
            double scanRadius = 12.0 * this.dragon.getIntelligence();
            candidate = this.dragon.level().getEntitiesOfClass(Mob.class, owner.getBoundingBox().inflate(scanRadius),
                    e -> e instanceof Enemy && this.dragon.validDefensiveTarget(e) && this.dragon.hasLineOfSight(e))
                    .stream().max(Comparator.comparingDouble(e -> this.evaluateThreatPriority(owner, e))).orElse(null);
        }
        if (this.dragon.validDefensiveTarget(candidate) && this.dragon.distanceToSqr(candidate) <= 576) {
            this.dragon.setTarget(candidate); this.combatTicks = 0; this.noSightTicks = 0;
        }
    }

    public double evaluateThreatPriority(@Nullable LivingEntity owner, LivingEntity candidate) {
        double score = 0.0;
        if (owner != null) {
            if (owner.getLastHurtByMob() == candidate && BabyDragon.recent(owner, owner.getLastHurtByMobTimestamp())) score += 120.0;
            if (candidate instanceof Mob mob && mob.getTarget() == owner) score += 80.0;
            score -= candidate.distanceToSqr(owner) * 0.30;
        }
        score -= this.dragon.distanceToSqr(candidate) * 0.15;
        float maxHp = Math.max(1.0F, candidate.getMaxHealth());
        double missingRatio = 1.0 - Math.clamp(candidate.getHealth() / maxHp, 0.0F, 1.0F);
        score += missingRatio * 28.0 * this.dragon.getIntelligence();
        return score;
    }

    private void engageInTransit(LivingEntity target) {
        double range = this.dragon.getDragonStage().range;
        if (this.dragon.distanceToSqr(target) > range * range || !this.dragon.getSensing().hasLineOfSight(target)) {
            this.dragon.cancelCharge();
            return;
        }
        this.dragon.aimAt(target.getBoundingBox().getCenter());
        if (this.dragon.getChargeTicks() > 0) {
            if (this.dragon.chargeReady()) {
                this.dragon.shoot(target);
                this.attackCooldown = this.dragon.getDragonStage().fireCooldown;
            }
        } else if (this.attackCooldown == 0 && this.dragon.getRecoveryTicks() == 0) {
            this.dragon.beginCharge();
        }
    }

    private void defend(LivingEntity target) {
        this.combatTicks++;
        this.dragon.aimAt(target.getBoundingBox().getCenter());
        this.dragon.setDragonState(DragonState.DEFENDING); this.dragon.setFlying(true);
        Vec3 point = this.selectCombatPosition(target);
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

    public Vec3 selectCombatPosition(LivingEntity target) {
        DragonStage stage = this.dragon.getDragonStage();
        double distSqr = this.dragon.distanceToSqr(target);
        double baseRadius = switch (stage) {
            case BABY -> distSqr < 12.25 ? 5.8 * this.dragon.getIntelligence() : 5.0;
            case YOUNG -> 6.0;
            case ADULT -> 7.5;
        };
        double baseHeight = switch (stage) {
            case BABY -> 2.0;
            case YOUNG -> 2.4;
            case ADULT -> 3.0;
        };
        this.orbitAngle += 0.025 * this.orbitDirection * this.dragon.getAgility();
        Vec3 anchor = this.dragon.isOrderedToSit() ? this.dragon.getStayAnchor() : null;
        Vec3 fallbackClear = null;
        for (int attempt = 0; attempt < 8; attempt++) {
            double testAngle = this.orbitAngle + (attempt == 0 ? 0 : (attempt % 2 == 1 ? 1 : -1) * ((attempt + 1) / 2) * 0.65 * this.orbitDirection);
            Vec3 candidate = target.position().add(
                    Math.cos(testAngle) * baseRadius,
                    baseHeight + 0.3 * Math.sin(testAngle * 1.7),
                    Math.sin(testAngle) * baseRadius);
            if (anchor != null && candidate.distanceToSqr(anchor) > 64.0) {
                Vec3 fromAnchor = candidate.subtract(anchor);
                candidate = anchor.add(fromAnchor.normalize().scale(8.0));
            }
            if (this.dragon.clearBlocks(candidate) && !this.isHazardousBelow(candidate)) {
                if (fallbackClear == null) fallbackClear = candidate;
                if (this.hasClearSightFrom(candidate, target)) {
                    if (attempt > 0) {
                        this.orbitDirection = -this.orbitDirection;
                        this.orbitAngle = testAngle;
                    }
                    return candidate;
                }
            }
        }
        return fallbackClear != null ? fallbackClear : target.position().add(0, baseHeight, 0);
    }

    private boolean isHazardousBelow(Vec3 pos) {
        BlockPos check = BlockPos.containing(pos).below();
        var state = this.dragon.level().getBlockState(check);
        return state.is(net.minecraft.world.level.block.Blocks.LAVA)
                || state.is(net.minecraft.world.level.block.Blocks.FIRE)
                || state.is(net.minecraft.world.level.block.Blocks.SOUL_FIRE)
                || state.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK);
    }

    private boolean hasClearSightFrom(Vec3 candidateBottom, LivingEntity target) {
        Vec3 eye = candidateBottom.add(0, this.dragon.getEyeHeight(), 0);
        return this.dragon.level().clip(new net.minecraft.world.level.ClipContext(
                eye, target.getBoundingBox().getCenter(),
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this.dragon)).getType() == net.minecraft.world.phys.HitResult.Type.MISS;
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
