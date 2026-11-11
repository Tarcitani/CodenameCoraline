package com.codenamecoraline.entity.firedragon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.codenamecoraline.entity.raven.FeyRaven;

/** Server coordinator: shoulder > defense > command > food > social > follow. */
final class FireDragonCompanion {
    private static final double LEASH_SQR = 16 * 16;
    private static final int SHOT_COOLDOWN = 60;
    private final FireDragon d;
    private boolean shiftHeld;
    private boolean exitRequested;
    private int ownerHurt, ownerAttack, selfHurt;
    private int shotCooldown, attackTick = -1, unseenTicks, mealCooldown, socialTicks;
    private FireDragon socialPartner;
    private ItemEntity mealTarget;
    private boolean hunting;
    private boolean personalThreat;
    private boolean seekingFood;
    private int socialCooldown;
    private LivingEntity trackedTarget;
    private Vec3 previousTargetPosition;
    private Vec3 targetMotion = Vec3.ZERO;
    private Vec3 defenseOrigin;
    private long targetSampleTime;

    FireDragonCompanion(FireDragon dragon) { d = dragon; }
    void resetAfterLoad() {
        shiftHeld = true; exitRequested = false; attackTick = -1; socialTicks = 0;
        socialPartner = null; mealTarget = null; hunting = false;
        trackedTarget = null; previousTargetPosition = null; targetMotion = Vec3.ZERO;
    }

    void tick() {
        if (shotCooldown > 0) --shotCooldown;
        if (mealCooldown > 0) --mealCooldown;
        if (socialCooldown > 0) --socialCooldown;
        if (d.getSatiety() <= 30) seekingFood = true;
        if (d.getSatiety() >= 55) seekingFood = false;
        LivingEntity owner = d.getOwner();
        if (d.isOnShoulder()) { maintainShoulder(owner); return; }
        if (d.isTame() && (owner == null || !owner.isAlive())) {
            if (owner != null && !owner.isAlive()) d.setOrderedToSit(true);
            if (d.getTarget() != null && d.canDamage(d.getTarget()) && d.distanceToSqr(d.getTarget()) <= LEASH_SQR) {
                fight(d.getTarget()); return;
            }
            d.setTarget(null); stop(); d.setState(FireDragon.State.SITTING); return;
        }
        if (!d.isTame()) {
            LivingEntity threat = d.getTarget();
            if (threat != null && !(threat instanceof Player) && d.canDamage(threat)
                    && defenseOrigin != null && threat.position().distanceToSqr(defenseOrigin) <= LEASH_SQR) {
                fight(threat); return;
            }
            d.setTarget(null);
            d.setState(FireDragon.State.WILD_IDLE);
            LivingEntity attacker = d.getLastHurtByMob();
            if (attacker != null && d.distanceToSqr(attacker) < 64 && d.tickCount % 10 == 0) {
                Vec3 away = d.position().subtract(attacker.position()).normalize().scale(5);
                move(d.position().add(away).add(0, 1, 0));
            } else if (d.tickCount % 200 == 0) move(d.position().add(d.getRandom().nextInt(5)-2, 1, d.getRandom().nextInt(5)-2));
            return;
        }
        chooseDefense(owner);
        if (seekingFood && d.tickCount % 40 == 20 && (d.getTarget() == null || hunting)) {
            if (scavenge(owner) && hunting) { d.setTarget(null); hunting = false; }
        }
        LivingEntity target = d.getTarget();
        if (target != null && (!d.canDamage(target) || (target.distanceToSqr(owner) > LEASH_SQR && !(personalThreat && d.distanceToSqr(target) <= 64))
                || (d.isOrderedToSit() && d.distanceToSqr(target) > 64))) {
            d.setTarget(null); attackTick = -1; target = null;
        }
        if (target != null) { socialTicks = 0; fight(target); return; }
        attackTick = -1;
        if (d.isOrderedToSit()) {
            stop(); d.setInSittingPose(true); d.setState(FireDragon.State.SITTING); return;
        }
        d.setInSittingPose(false);
        if (d.distanceToSqr(owner) > 24 * 24) { relocate(owner.position()); return; }
        if (d.distanceToSqr(owner) > 64) { socialTicks = 0; follow(owner); return; }
        if (d.tickCount % 40 == 0) scan(owner);
        if (d.getTarget() != null) return;
        if (seekingFood && tickMeal(owner)) return;
        if (socialTicks > 0 && socialPartner != null && socialPartner.isAlive() && socialPartner.getTarget() == null
                && socialPartner.distanceToSqr(owner) <= LEASH_SQR) {
            d.setState(FireDragon.State.SOCIAL);
            --socialTicks;
            if (d.tickCount % 10 == 0 && d.distanceToSqr(socialPartner) > 4) move(socialPartner.position());
            return;
        }
        follow(owner);
    }

    /** Damage callback avoids missing a first-tick hit and interrupts non-combat tasks immediately. */
    void onDamaged(LivingEntity attacker) {
        if (d.isOnShoulder() || (!d.isTame() && attacker instanceof Player)) return;
        if (d.canDamage(attacker)) defend(attacker, true);
    }

    private void chooseDefense(LivingEntity owner) {
        if (owner.getLastHurtByMobTimestamp() != ownerHurt) {
            ownerHurt = owner.getLastHurtByMobTimestamp(); defend(owner.getLastHurtByMob(), false);
        }
        if (d.getTarget() == null || hunting) {
            if (d.getLastHurtByMobTimestamp() != selfHurt) {
                selfHurt = d.getLastHurtByMobTimestamp(); defend(d.getLastHurtByMob(), true);
            }
            if ((d.getTarget() == null || hunting) && owner.getLastHurtMobTimestamp() != ownerAttack) {
                ownerAttack = owner.getLastHurtMobTimestamp(); defend(owner.getLastHurtMob(), false);
            }
        }
    }
    private void defend(LivingEntity target, boolean personal) {
        if (target != null && d.canDamage(target)) {
            personalThreat = personal;
            if (d.getTarget() == target && !hunting) return;
            d.setTarget(target); hunting = false; attackTick = -1; unseenTicks = 0;
            socialTicks = 0; mealTarget = null; defenseOrigin = d.position();
            trackedTarget = null; d.setInSittingPose(false);
            d.setState(FireDragon.State.DEFENDING);
        }
    }
    private void fight(LivingEntity target) {
        d.setInSittingPose(false);
        d.setState(hunting ? FireDragon.State.HUNTING : FireDragon.State.DEFENDING);
        sampleTarget(target);
        d.getLookControl().setLookAt(target.getX(), target.getY() + target.getBbHeight() * .5, target.getZ(), 60, 60);
        Vec3 facing = target.position().subtract(d.position());
        d.setYRot((float) Math.toDegrees(Math.atan2(-facing.x, facing.z)));
        d.yBodyRot = d.getYRot();
        boolean visible = d.hasLineOfSight(target);
        if (!visible) ++unseenTicks; else unseenTicks = 0;
        if (unseenTicks > 100 || (hunting && d.getSatiety() >= 55)) { d.setTarget(null); return; }
        if (attackTick < 0 && (d.distanceToSqr(target) > 100 || !visible)) {
            if (d.tickCount % 10 == 0) move(target.position().add(0, target.isInWater() ? 2 : 1, 0));
            return;
        }
        if (attackTick < 0 && d.distanceToSqr(target) < 16 && shotCooldown > 0) {
            if (d.tickCount % 5 == 0) retreat(target);
            return;
        }
        stop();
        if (attackTick < 0) {
            if (shotCooldown > 0) return;
            attackTick = 0; d.playVisual(FireDragonAnimation.FIRE_WINDUP);
        }
        if (++attackTick == 12) {
            var shot = new DragonFireball(d.level(), d);
            Vec3 center = target.position().add(0, target.getBbHeight() * .5, 0);
            Vec3 aimPoint = DragonFireball.aimPoint(shot.position(), center, targetMotion);
            if (!visible || !d.canDamage(target) || d.distanceToSqr(target) > 144 || !clearShot(shot.position(), aimPoint, target)) {
                attackTick = -1; shotCooldown = 20; d.playVisual(FireDragonAnimation.NONE); return;
            }
            Vec3 aim = aimPoint.subtract(shot.position());
            shot.shoot(aim.x, aim.y, aim.z, DragonFireball.SPEED, 0);
            d.level().addFreshEntity(shot);
            shotCooldown = SHOT_COOLDOWN;
            d.playVisual(FireDragonAnimation.FIRE_RELEASE);
        } else if (attackTick == 13) d.playVisual(FireDragonAnimation.FIRE_RECOVERY);
        if (attackTick >= 28) attackTick = -1;
    }
    private boolean clearShot(Vec3 start, Vec3 end, LivingEntity target) {
        if (d.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, d)).getType() != HitResult.Type.MISS) return false;
        for (LivingEntity e : d.level().getEntitiesOfClass(LivingEntity.class, d.getBoundingBox().expandTowards(end.subtract(start)).inflate(1))) {
            if (e != d && e != target && !d.canDamage(e) && e.getBoundingBox().inflate(.2).clip(start, end).isPresent()) return false;
        }
        return true;
    }

    private void sampleTarget(LivingEntity target) {
        long now = d.level().getGameTime();
        Vec3 velocity = target.getDeltaMovement();
        if (trackedTarget == target && previousTargetPosition != null && now > targetSampleTime) {
            velocity = target.position().subtract(previousTargetPosition).scale(1.0 / (now - targetSampleTime));
        }
        targetMotion = velocity.lengthSqr() > .36 ? Vec3.ZERO : velocity;
        trackedTarget = target; previousTargetPosition = target.position(); targetSampleTime = now;
    }

    private void retreat(LivingEntity target) {
        Vec3 away = d.position().subtract(target.position()).multiply(1, 0, 1);
        if (away.lengthSqr() < .01) away = new Vec3(1, 0, 0);
        Vec3 goal = d.position().add(away.normalize().scale(3)).add(0, .8, 0);
        LivingEntity owner = d.getOwner();
        Vec3 anchor = owner == null ? defenseOrigin : owner.position();
        if (anchor == null || goal.distanceToSqr(anchor) <= LEASH_SQR) move(goal);
    }

    static boolean prey(LivingEntity e) {
        if (!e.isAlive() || e.hasCustomName() || (e instanceof Animal a && a.isBaby())
                || (e instanceof TamableAnimal a && a.isTame())) return false;
        var t = e.getType();
        return t == net.minecraft.world.entity.EntityType.PIG || t == net.minecraft.world.entity.EntityType.COW
                || t == net.minecraft.world.entity.EntityType.SHEEP || t == net.minecraft.world.entity.EntityType.CHICKEN
                || t == net.minecraft.world.entity.EntityType.RABBIT || t == net.minecraft.world.entity.EntityType.COD
                || t == net.minecraft.world.entity.EntityType.SALMON;
    }
    private void scan(LivingEntity owner) {
        var nearby = d.level().getEntitiesOfClass(LivingEntity.class, d.getBoundingBox().inflate(16));
        LivingEntity closest = null;
        FireDragon friend = null;
        for (LivingEntity e : nearby) {
            if (e.distanceToSqr(owner) > LEASH_SQR) continue;
            if (prey(e) && d.canDamage(e) && (closest == null || d.distanceToSqr(e) < d.distanceToSqr(closest))) closest = e;
            if (e instanceof FireDragon f && f != d && f.isTame() && !f.isOnShoulder()
                    && !f.isOrderedToSit() && f.getTarget() == null && f.getOwnerReference() != null
                    && f.getOwnerReference().equals(d.getOwnerReference()) && d.distanceToSqr(f) < 36) friend = f;
        }
        if (seekingFood && mealTarget == null && closest != null) {
            hunting = true; unseenTicks = 0; d.setTarget(closest);
        } else if (friend != null && socialCooldown == 0 && d.getId() < friend.getId()) {
            socialPartner = friend; socialTicks = 100; socialCooldown = 400;
            FireDragonAnimation clip = switch (d.getRandom().nextInt(4)) {
                case 0 -> FireDragonAnimation.GREET;
                case 1 -> FireDragonAnimation.IMITATE_WINGS;
                case 2 -> FireDragonAnimation.REST_TOGETHER;
                default -> FireDragonAnimation.PLAY_CHASE;
            };
            d.playVisual(clip); friend.companionSocial(d, clip);
        }
    }
    void acceptSocial(FireDragon other, FireDragonAnimation clip) {
        socialPartner = other; socialTicks = 100; socialCooldown = 400; d.playVisual(clip);
    }
    private static boolean meat(ItemStack s) {
        return s.is(Items.PORKCHOP) || s.is(Items.COOKED_PORKCHOP) || s.is(Items.BEEF) || s.is(Items.COOKED_BEEF)
                || s.is(Items.MUTTON) || s.is(Items.COOKED_MUTTON) || s.is(Items.CHICKEN) || s.is(Items.COOKED_CHICKEN)
                || s.is(Items.RABBIT) || s.is(Items.COOKED_RABBIT) || s.is(Items.COD) || s.is(Items.COOKED_COD)
                || s.is(Items.SALMON) || s.is(Items.COOKED_SALMON);
    }
    private boolean scavenge(LivingEntity owner) {
        if (mealCooldown > 0) return false;
        ItemEntity item = null;
        for (ItemEntity candidate : d.level().getEntitiesOfClass(ItemEntity.class, d.getBoundingBox().inflate(8))) {
            if (meat(candidate.getItem()) && candidate.distanceToSqr(owner) <= LEASH_SQR
                    && (item == null || d.distanceToSqr(candidate) < d.distanceToSqr(item))) item = candidate;
        }
        mealTarget = item;
        return item != null;
    }
    private boolean tickMeal(LivingEntity owner) {
        if (mealTarget == null || !mealTarget.isAlive() || !meat(mealTarget.getItem())
                || mealTarget.distanceToSqr(owner) > LEASH_SQR) { mealTarget = null; return false; }
        if (d.distanceToSqr(mealTarget) > 2) {
            if (d.tickCount % 10 == 0) move(mealTarget.position().add(0,.2,0));
        } else if (mealCooldown == 0) {
            mealTarget.getItem().shrink(1); if (mealTarget.getItem().isEmpty()) mealTarget.discard();
            d.eatMeat(); mealCooldown = 40;
        }
        return true;
    }
    private void follow(LivingEntity owner) {
        d.setState(FireDragon.State.FOLLOWING);
        if (d.distanceToSqr(owner) > 9 && d.tickCount % 10 == 0) move(owner.position().add(0, 1, 0));
        else if (d.distanceToSqr(owner) <= 9) stop();
    }
    private void stop() {
        d.getNavigation().stop(); d.setNoGravity(false);
        d.setDeltaMovement(d.getDeltaMovement().multiply(.4, 1, .4));
    }
    private void move(Vec3 goal) {
        if (d.level().clip(new ClipContext(d.getEyePosition(), goal.add(0,.3,0), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, d)).getType() != HitResult.Type.MISS) {
            d.setNoGravity(false); d.getNavigation().moveTo(goal.x, goal.y, goal.z, 1); return;
        }
        d.getNavigation().stop(); d.setNoGravity(true);
        Vec3 delta = goal.subtract(d.position());
        d.setDeltaMovement(delta.lengthSqr() < .04 ? Vec3.ZERO : delta.normalize().scale(.2));
        d.setYRot((float) Math.toDegrees(Math.atan2(-delta.x, delta.z)));
        d.yBodyRot = d.getYRot();
    }

    boolean perch(Player owner) {
        if (!d.isTame() || !d.isOwnedBy(owner) || !owner.isAlive() || d.isOnShoulder()) return false;
        boolean left = owner.getShoulderParrotLeft().isEmpty(), right = owner.getShoulderParrotRight().isEmpty();
        for (Entity e : d.level().getEntities(d, owner.getBoundingBox().inflate(3))) {
            if (e instanceof FireDragon f && f.isOnShoulder() && f.getOwnerReference() != null
                    && f.getOwnerReference().equals(d.getOwnerReference())) {
                if (f.getShoulderSide() < 0) left = false; else right = false;
            }
            if (e instanceof FeyRaven r && r.isOnShoulder() && r.getOwnerReference() != null
                    && r.getOwnerReference().equals(d.getOwnerReference())) left = false;
        }
        if (!left && !right) return false;
        int side = left ? -1 : 1;
        if (!clearShoulder(shoulderPosition(owner, side))) return false;
        d.setShoulderSide(left ? -1 : 1); shiftHeld = owner.isShiftKeyDown(); exitRequested = false;
        d.setTarget(null); attackTick = -1; socialTicks = 0; d.getNavigation().stop();
        d.setOrderedToSit(false); d.setInSittingPose(false); d.setNoGravity(true);
        d.setState(FireDragon.State.SHOULDER); d.playVisual(FireDragonAnimation.SHOULDER);
        maintainShoulder(owner); return true;
    }
    private void maintainShoulder(LivingEntity owner) {
        if (!(owner instanceof Player player) || !owner.isAlive()) {
            if (relocate(d.position())) { d.setShoulderSide(0); d.setOrderedToSit(true); }
            return;
        }
        if ((d.getShoulderSide() < 0 && player.getShoulderParrotLeft().isPresent())
                || (d.getShoulderSide() > 0 && player.getShoulderParrotRight().isPresent())) exitRequested = true;
        boolean shift = player.isShiftKeyDown();
        if (shift && !shiftHeld) exitRequested = true;
        shiftHeld = shift;
        if (exitRequested && relocate(player.position())) {
            d.setShoulderSide(0); d.playVisual(FireDragonAnimation.NONE); d.setState(FireDragon.State.FOLLOWING); return;
        }
        Vec3 perch = shoulderPosition(player, d.getShoulderSide());
        if (!clearShoulder(perch)) { exitRequested = true; return; }
        d.setPos(perch);
        d.setDeltaMovement(Vec3.ZERO); d.setNoGravity(true); d.setTarget(null);
        d.setYRot(player.yBodyRot); d.yBodyRot = player.yBodyRot;
        d.setState(FireDragon.State.SHOULDER);
    }
    private static Vec3 shoulderPosition(Player player, int side) {
        double yaw = Math.toRadians(player.yBodyRot);
        return new Vec3(player.getX() + Math.cos(yaw)*side*.32,
                player.getY() + player.getBbHeight() - .75, player.getZ() + Math.sin(yaw)*side*.32);
    }
    private boolean clearShoulder(Vec3 pos) {
        var box = d.getType().getDimensions().makeBoundingBox(pos);
        return !d.level().getBlockCollisions(d, box).iterator().hasNext();
    }
    private boolean relocate(Vec3 origin) {
        for (int dy = 0; dy <= 2; dy++) for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            if (dx == 0 && dz == 0) continue;
            BlockPos p = BlockPos.containing(origin).offset(dx, dy, dz);
            if (!d.level().hasChunkAt(p)) continue;
            Vec3 v = Vec3.atBottomCenterOf(p);
            var box = d.getType().getDimensions().makeBoundingBox(v);
            if (d.level().getBlockState(p.below()).isFaceSturdy(d.level(), p.below(), Direction.UP)
                    && d.level().noCollision(d, box) && !d.level().containsAnyLiquid(box)) {
                d.setPos(v); stop(); return true;
            }
        }
        return false;
    }
}
