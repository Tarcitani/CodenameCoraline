package com.codenamecoraline.entity.macaque;

import com.codenamecoraline.entity.PlayerSummon;
import com.codenamecoraline.entity.canopy.CanopyBreaker;
import com.codenamecoraline.entity.canopy.TreeNav;
import com.codenamecoraline.entity.canopy.TreeNav.Trunk;
import com.codenamecoraline.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import com.codenamecoraline.entity.canopy.CanopyBreaker;
import com.codenamecoraline.registry.ModEntities;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Bark Macaque: a small, fast creature of the canopy, the Canopy Breaker's minion. It flanks, circles until the target
 * looks away, then crouches and leaps (a real ballistic jump) to bite, springs back and circles again. It climbs trunks
 * and walls with physical wall-climbing navigation, so it never slides or teleports.
 *
 * <p>A macaque summoned by the boss carries the boss's UUID: it never targets the boss or another macaque, and it is
 * dismissed as soon as the boss dies or is gone (no orphaned minions). Without an owner (spawn egg, command) it is a
 * plain hostile mob.</p>
 */
public class BarkMacaque extends Monster implements PlayerSummon {
    public static final float WIDTH = 0.7F;
    public static final float HEIGHT = 0.85F;
    public static final double MAX_HEALTH = 14.0D;
    public static final double ARMOR = 2.0D;
    public static final double GROUND_SPEED = 0.32D;
    public static final double BITE_DAMAGE = 3.0D;

    public static final int ARRIVE_TICKS = 14;
    public static final int LEAP_PREPARE_TICKS = 8;
    public static final int LAND_TICKS = 10;
    public static final int RETREAT_TICKS = 24;
    public static final int LEAP_COOLDOWN = 70;
    /** How long it circles waiting for the target to look away before it attacks anyway. */
    public static final int MAX_WAIT_TICKS = 60;
    public static final double LEAP_MIN_RANGE = 2.2D;
    public static final double LEAP_MAX_RANGE = 6.5D;
    /** Allies a single player may keep out at once. */
    public static final int ALLY_CAP = 4;
    /** Allies leave on their own after this long (30 s), so the fight does not fill up with them. */
    public static final int ALLY_LIFETIME = 600;
    /** How far an ally will chase an enemy from its owner before turning back. */
    public static final double ALLY_LEASH = 18.0D;

    /** What the body is doing. */
    public enum Activity { ARRIVE, GROUND, TRUNK_APPROACH, TRUNK_CLIMB, LEAP_PREPARE, AIRBORNE, LAND, RETREAT, DYING }

    private static final EntityDataAccessor<Integer> DATA_ANIM =
            SynchedEntityData.defineId(BarkMacaque.class, EntityDataSerializers.INT);

    /** Client-side animation clock. */
    public final AnimationState animState = new AnimationState();

    private Activity activity = Activity.GROUND;
    private int activityTimer = 0;
    private int animHold = 0;
    private int hurtTicks = 0;
    private int attackCooldown = 40;
    private int waitTicks = 0;
    private int airTicks = 0;
    private boolean hitDone = false;
    private Vec3 leapDir = Vec3.ZERO;
    /** The target of the current leap, held here so the leap does not depend on the mob target being kept. */
    private @Nullable LivingEntity leapTarget;
    private boolean climbing = false;
    private int pathRecalc = 0;
    private Vec3 lastPos = Vec3.ZERO;
    private double lastSpeed = 0.0D;
    private double lastClimb = 0.0D;
    private @Nullable Trunk climbTrunk;
    private Vec3 approachSpot = Vec3.ZERO;
    private Vec3 perchSpot = Vec3.ZERO;
    private int trunkCooldown = 0;
    private int trunkClimbs = 0;
    private int gripLosses = 0;
    private int bitesLanded = 0;
    private int leapsStarted = 0;
    private @Nullable UUID ownerUuid;
    /** True when a player summoned this macaque (it fights hostile mobs for them); false for the boss's minions. */
    private boolean allied = false;
    private int alliedTicks = 0;

    public BarkMacaque(EntityType<? extends BarkMacaque> type, Level level) {
        super(type, level);
        this.xpReward = 6;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, ARMOR)
                .add(Attributes.MOVEMENT_SPEED, GROUND_SPEED)
                .add(Attributes.ATTACK_DAMAGE, BITE_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANIM, MacaqueAnim.IDLE.ordinal());
    }

    // ---------------------------------------------------------------- accessors
    public MacaqueAnim getAnim() {
        return MacaqueAnim.byOrdinal(this.entityData.get(DATA_ANIM));
    }

    public void setAnim(MacaqueAnim anim) {
        if (this.getAnim() != anim) {
            this.entityData.set(DATA_ANIM, anim.ordinal());
            this.animHold = 0;
        }
    }

    public Activity getActivity() {
        return this.activity;
    }

    private void setActivity(Activity next) {
        this.activity = next;
        this.activityTimer = 0;
    }

    public int getBitesLanded() {
        return this.bitesLanded;
    }

    public int getTrunkClimbs() {
        return this.trunkClimbs;
    }

    public int getGripLosses() {
        return this.gripLosses;
    }

    public int getLeapsStarted() {
        return this.leapsStarted;
    }

    public boolean isAllied() {
        return this.allied;
    }

    @Override
    public boolean isPlayerSummon() {
        return this.allied;
    }

    /** True for a macaque this player summoned. */
    public boolean isAlliedTo(Player player) {
        return this.allied && this.ownerUuid != null && this.ownerUuid.equals(player.getUUID());
    }

    /** Living allies of {@code owner} near it: the summon cap counts these. */
    public static int countAllies(ServerLevel level, Player owner) {
        return level.getEntitiesOfClass(BarkMacaque.class, owner.getBoundingBox().inflate(64.0D),
                m -> m.isAlive() && m.isAlliedTo(owner)).size();
    }

    /**
     * Summons up to {@code count} allied macaques around {@code owner}, never beyond {@link #ALLY_CAP}. Returns how many
     * appeared (zero when the cap is already reached).
     */
    public static int summonAllies(ServerLevel level, ServerPlayer owner, int count) {
        int room = Math.min(count, ALLY_CAP - countAllies(level, owner));
        int spawned = 0;
        Vec3 look = owner.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        Vec3 front = look.lengthSqr() > 1.0E-4D ? look.normalize().scale(1.6D) : Vec3.ZERO;
        for (int i = 0; i < room; i++) {
            double a = Math.PI * 2.0D * i / Math.max(1, count) + owner.getYRot() * Math.PI / 180.0D;
            Vec3 spot = owner.position().add(front).add(Math.cos(a) * 1.8D, 0.0D, Math.sin(a) * 1.8D);
            BarkMacaque macaque = ModEntities.BARK_MACAQUE.get().create(level, EntitySpawnReason.TRIGGERED);
            if (macaque == null) {
                continue;
            }
            macaque.setPos(spot.x, spot.y, spot.z);
            macaque.setAlliedTo(owner);
            level.addFreshEntity(macaque);
            spawned++;
        }
        if (spawned > 0) {
            level.playSound(null, owner.blockPosition(), ModSounds.MACAQUE_SCREECH.get(), SoundSource.PLAYERS, 1.1F, 1.0F);
        }
        return spawned;
    }

    /** Makes this macaque an ally of {@code owner}: it fights hostile mobs, never the owner, the boss or its kin. */
    public void setAlliedTo(ServerPlayer owner) {
        this.allied = true;
        this.ownerUuid = owner.getUUID();
        this.setActivity(Activity.ARRIVE);
        this.setAnim(MacaqueAnim.SCREECH);
    }

    public @Nullable UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public boolean isOwnedBy(CanopyBreaker boss) {
        return this.ownerUuid != null && this.ownerUuid.equals(boss.getUUID());
    }

    /** True for the animations in which the head should not follow the target. */
    public boolean isClimbingAnimation() {
        MacaqueAnim a = this.getAnim();
        return a == MacaqueAnim.CLIMB || a == MacaqueAnim.DEATH;
    }

    /** Binds this macaque to its boss and starts its arrival cry. */
    public void setOwner(CanopyBreaker boss) {
        this.ownerUuid = boss.getUUID();
        this.setActivity(Activity.ARRIVE);
        this.setAnim(MacaqueAnim.SCREECH);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (this.level().isClientSide() && DATA_ANIM.equals(accessor)) {
            this.animState.start(this.tickCount);
        }
    }

    @Override
    public boolean onClimbable() {
        return this.climbing;
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false; // it lives in the treetops
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.ownerUuid == null && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    /** Only players are ever targets: never the boss, another macaque or any other mob. */
    @Override
    public boolean canAttack(LivingEntity target) {
        if (this.allied) {
            return target instanceof Enemy && !(target instanceof CanopyBreaker) && !(target instanceof BarkMacaque)
                    && !(target instanceof Player) && !(target instanceof PlayerSummon summon && summon.isPlayerSummon())
                    && super.canAttack(target);
        }
        return target instanceof Player && super.canAttack(target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof CanopyBreaker || attacker instanceof BarkMacaque || this.activity == Activity.DYING) {
            return false; // the boss's club and its own kin never hurt a macaque
        }
        if (this.allied && attacker instanceof Player p && this.isAlliedTo(p)) {
            return false; // an ally never fights its owner
        }
        if (this.allied && attacker instanceof PlayerSummon summon && summon.isPlayerSummon()) {
            return false; // nor another player summon (a Gift Mimic's burst, say)
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive()) {
            this.hurtTicks = 7;
            if (this.activity == Activity.GROUND || this.activity == Activity.LAND) {
                this.setAnim(MacaqueAnim.HURT);
            }
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        this.activity = Activity.DYING;
        this.climbing = false;
        this.setAnim(MacaqueAnim.DEATH);
        super.die(source);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime >= 30 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    /** Removes the macaque with a puff of leaves (the boss fell or left). */
    public void dismiss() {
        if (this.level() instanceof ServerLevel level && !this.isRemoved()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_LEAVES.defaultBlockState()),
                    this.getX(), this.getY() + 0.4D, this.getZ(), 14, 0.3D, 0.3D, 0.3D, 0.05D);
        }
        this.discard();
    }

    // ---------------------------------------------------------------- the brain
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.climbing = this.horizontalCollision && this.activity == Activity.GROUND;
        }
        this.animHold++;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.activity == Activity.DYING) {
            return;
        }
        this.activityTimer++;
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        if (this.trunkCooldown > 0) {
            this.trunkCooldown--;
        }
        if (this.pathRecalc > 0) {
            this.pathRecalc--;
        }
        if (this.hurtTicks > 0) {
            this.hurtTicks--;
        }
        double moved = Math.hypot(this.getX() - this.lastPos.x, this.getZ() - this.lastPos.z);
        this.lastSpeed = Mth.lerp(0.4D, this.lastSpeed, moved);
        this.lastClimb = this.getY() - this.lastPos.y;
        this.lastPos = this.position();

        if (this.allied) {
            this.alliedTicks++;
            if (this.alliedTicks > ALLY_LIFETIME || (this.tickCount % 10 == 0 && !this.allyOwnerPresent(level))) {
                this.dismiss(); // the time is up, or the summoner left or died
                return;
            }
        } else if (this.ownerUuid != null && this.tickCount > 40 && this.tickCount % 10 == 0 && !this.ownerAlive(level)) {
            this.dismiss(); // no orphans: the boss is gone
            return;
        }
        switch (this.activity) {
            case ARRIVE -> this.tickArrive(level);
            case GROUND -> this.tickGround(level);
            case TRUNK_APPROACH -> this.tickTrunkApproach(level);
            case TRUNK_CLIMB -> this.tickTrunkClimb(level);
            case LEAP_PREPARE -> this.tickLeapPrepare();
            case AIRBORNE -> this.tickAirborne(level);
            case LAND -> this.tickLand();
            case RETREAT -> this.tickRetreat(level);
            default -> { }
        }
    }

    private boolean allyOwnerPresent(ServerLevel level) {
        return this.ownerUuid != null && level.getPlayerByUUID(this.ownerUuid) instanceof Player p && p.isAlive();
    }

    private boolean ownerAlive(ServerLevel level) {
        return level.getEntity(this.ownerUuid) instanceof CanopyBreaker boss && boss.isAlive()
                && boss.getActivity() != CanopyBreaker.Activity.DYING && boss.getActivity() != CanopyBreaker.Activity.DEAD;
    }

    private @Nullable LivingEntity chooseTarget(ServerLevel level) {
        if (this.allied) {
            return this.chooseAllyTarget(level);
        }
        if (this.ownerUuid != null && level.getEntity(this.ownerUuid) instanceof CanopyBreaker boss
                && boss.getTarget() instanceof Player p && p.isAlive() && this.canAttack(p)
                && this.distanceToSqr(p) <= 32.0D * 32.0D) {
            this.setTarget(p);
            return p;
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && this.canAttack(current) && this.distanceToSqr(current) <= 32.0D * 32.0D
                && this.tickCount % 20 != 0) {
            return current;
        }
        Player best = null;
        double bestDist = Double.MAX_VALUE;
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(32.0D),
                pl -> pl.isAlive() && this.canAttack(pl))) {
            double d = this.distanceToSqr(p);
            if (d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        this.setTarget(best);
        return best;
    }

    /** An ally fights the nearest hostile mob within its leash of the owner; it never picks the owner or the boss. */
    private @Nullable LivingEntity chooseAllyTarget(ServerLevel level) {
        Player owner = this.ownerUuid == null ? null : level.getPlayerByUUID(this.ownerUuid);
        if (owner == null) {
            this.setTarget(null);
            return null;
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && this.canAttack(current) && this.tickCount % 20 != 0
                && current.distanceToSqr(owner) <= ALLY_LEASH * ALLY_LEASH) {
            return current;
        }
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0D),
                e -> this.canAttack(e) && e.distanceToSqr(owner) <= ALLY_LEASH * ALLY_LEASH)) {
            double d = this.distanceToSqr(e);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        this.setTarget(best);
        return best;
    }

    private void faceTarget(LivingEntity target) {
        this.faceDirection(target.getX() - this.getX(), target.getZ() - this.getZ());
    }

    private void faceDirection(double dx, double dz) {
        if (dx * dx + dz * dz < 1.0E-5D) {
            return;
        }
        float yaw = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.yBodyRot = yaw;
    }

    private void tickArrive(ServerLevel level) {
        this.getNavigation().stop();
        if (this.activityTimer == 1) {
            this.playSound(ModSounds.MACAQUE_SCREECH.get(), 1.2F, 1.0F);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_LEAVES.defaultBlockState()),
                    this.getX(), this.getY() + 0.5D, this.getZ(), 16, 0.4D, 0.4D, 0.4D, 0.06D);
        }
        LivingEntity t = this.chooseTarget(level);
        if (t != null) {
            this.faceTarget(t);
        }
        if (this.activityTimer >= ARRIVE_TICKS) {
            this.setActivity(Activity.GROUND);
        }
    }

    private void tickGround(ServerLevel level) {
        LivingEntity target = this.chooseTarget(level);
        if (target == null) {
            Player owner = this.allied && this.ownerUuid != null ? level.getPlayerByUUID(this.ownerUuid) : null;
            if (owner != null && this.distanceToSqr(owner) > 25.0D) {
                if (this.pathRecalc <= 0) {
                    this.getNavigation().moveTo(owner, 1.3D);
                    this.pathRecalc = 10;
                }
            } else {
                this.getNavigation().stop();
            }
            this.updateLocomotionAnim();
            return;
        }
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        double dy = target.getY() - this.getY();
        boolean sight = this.getSensing().hasLineOfSight(target);
        boolean inRange = dist >= LEAP_MIN_RANGE && dist <= LEAP_MAX_RANGE && dy <= 3.2D && dy >= -3.5D;

        if (this.attackCooldown <= 0 && this.onGround() && inRange && sight) {
            this.waitTicks++;
            if (this.targetLooksAway(target) || this.waitTicks > MAX_WAIT_TICKS) {
                this.startLeap(target);
                return;
            }
        }
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.pathRecalc <= 0 || this.getNavigation().isDone()) {
            if (dy >= 3.0D && dist < 14.0D && this.trunkCooldown <= 0 && this.onGround() && this.startTrunkRoute(level, target)) {
                return;
            }
            if (dy >= 2.5D && dist < 10.0D) {
                this.getNavigation().moveTo(target, 1.25D); // up a wall towards it
            } else if (dist > LEAP_MAX_RANGE + 0.5D) {
                this.moveToFlank(target, dx, dz, dist);
            } else {
                this.circle(target, dx, dz, dist);
            }
            this.pathRecalc = 8;
        }
        this.updateLocomotionAnim();
    }

    /** True when the target is not facing this macaque: the moment to strike from the flank. */
    private boolean targetLooksAway(LivingEntity target) {
        Vec3 look = target.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        Vec3 toMe = this.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
        if (look.lengthSqr() < 1.0E-4D || toMe.lengthSqr() < 1.0E-4D) {
            return true;
        }
        return look.normalize().dot(toMe.normalize()) < 0.3D;
    }

    /** Heads for a point beside the target instead of straight at it (the flank). */
    private void moveToFlank(LivingEntity target, double dx, double dz, double dist) {
        double side = (this.getUUID().getLeastSignificantBits() & 1L) == 0L ? 1.0D : -1.0D;
        double px = -dz / dist * 4.0D * side;
        double pz = dx / dist * 4.0D * side;
        this.getNavigation().moveTo(target.getX() + px, target.getY(), target.getZ() + pz, 1.3D);
    }

    /** Circles at a biting distance while it waits for an opening. */
    private void circle(LivingEntity target, double dx, double dz, double dist) {
        double angle = Math.atan2(-dz, -dx) + Math.toRadians(32.0D) * ((this.tickCount / 80) % 2 == 0 ? 1 : -1);
        double radius = Math.max(4.2D, Math.min(dist, 5.5D));
        this.getNavigation().moveTo(target.getX() + Math.cos(angle) * radius, target.getY(),
                target.getZ() + Math.sin(angle) * radius, 1.1D);
    }

    private void updateLocomotionAnim() {
        if (this.hurtTicks > 0) {
            return;
        }
        MacaqueAnim want;
        if (this.climbing && this.lastClimb > 0.01D) {
            want = MacaqueAnim.CLIMB;
        } else if (this.lastSpeed > 0.2D) {
            want = MacaqueAnim.RUN;
        } else if (this.lastSpeed > 0.04D) {
            want = MacaqueAnim.WALK;
        } else {
            want = MacaqueAnim.IDLE;
        }
        if (want != this.getAnim() && this.animHold >= 5) {
            this.setAnim(want);
        }
    }

    // ---------------------------------------------------------------- trunks
    /**
     * Plans a climb up the trunk nearest to an elevated target: an approach spot reachable on foot and a perch on the
     * canopy. Returns false (and rests the idea for a while) when there is no such tree.
     */
    public boolean startTrunkRoute(ServerLevel level, LivingEntity target) {
        Trunk trunk = TreeNav.bestTrunk(level, this.position(), target.position(), 12, 4, WIDTH);
        Vec3 approach = trunk == null ? null : TreeNav.approachSpot(level, trunk, this.position(), WIDTH);
        Vec3 perch = trunk == null ? null : TreeNav.findPerch(level, trunk, 3, WIDTH, HEIGHT);
        if (trunk == null || approach == null || perch == null || perch.y - approach.y < 3.0D) {
            this.trunkCooldown = 100;
            return false;
        }
        var path = this.getNavigation().createPath(approach.x, approach.y, approach.z, 1);
        if (path == null || !path.canReach()) {
            this.trunkCooldown = 100;
            return false;
        }
        this.climbTrunk = trunk;
        this.approachSpot = approach;
        this.perchSpot = perch;
        this.setActivity(Activity.TRUNK_APPROACH);
        this.getNavigation().moveTo(approach.x, approach.y, approach.z, 1.3D);
        return true;
    }

    private void tickTrunkApproach(ServerLevel level) {
        Trunk trunk = this.climbTrunk;
        if (trunk == null || this.activityTimer > 120) {
            this.endTrunkRoute(100);
            return;
        }
        double dx = this.approachSpot.x - this.getX();
        double dz = this.approachSpot.z - this.getZ();
        if (dx * dx + dz * dz < 0.8D * 0.8D && Math.abs(this.getY() - this.approachSpot.y) < 1.2D) {
            // hands catch the bark: from here every step is validated against solid blocks
            this.getNavigation().stop();
            this.noPhysics = true;
            this.setNoGravity(true);
            this.setDeltaMovement(Vec3.ZERO);
            this.setPos(this.approachSpot.x, this.getY(), this.approachSpot.z);
            Direction face = TreeNav.faceOf(trunk, this.approachSpot);
            this.faceDirection(-face.getStepX(), -face.getStepZ());
            this.setActivity(Activity.TRUNK_CLIMB);
            this.setAnim(MacaqueAnim.CLIMB);
            return;
        }
        if (this.pathRecalc <= 0 || this.getNavigation().isDone()) {
            this.getNavigation().moveTo(this.approachSpot.x, this.approachSpot.y, this.approachSpot.z, 1.3D);
            this.pathRecalc = 8;
        }
        this.updateLocomotionAnim();
    }

    private boolean hasGrip(ServerLevel level, Trunk trunk) {
        int y = (int) Math.floor(this.getY() + 0.5D);
        if (y > trunk.topY()) {
            return true; // above the trunk: only the last steps over the top
        }
        return TreeNav.isLog(level.getBlockState(new BlockPos(trunk.base().getX(), Math.max(y, trunk.base().getY()), trunk.base().getZ())));
    }

    private void tickTrunkClimb(ServerLevel level) {
        Trunk trunk = this.climbTrunk;
        if (trunk == null) {
            this.endTrunkRoute(100);
            return;
        }
        this.setAnim(MacaqueAnim.CLIMB);
        if (!this.hasGrip(level, trunk) || this.activityTimer > 400) {
            this.loseGrip();
            return;
        }
        double ny = Math.min(this.perchSpot.y, this.getY() + 0.2D);
        double nx = this.getX();
        double nz = this.getZ();
        if (ny >= this.perchSpot.y - 1.0E-3D) { // the top: slide over it onto the perch, through leaves but never logs
            double dx = this.perchSpot.x - nx;
            double dz = this.perchSpot.z - nz;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len < 0.25D) {
                this.setPos(this.perchSpot.x, this.perchSpot.y, this.perchSpot.z);
                this.releaseClimb();
                if (!TreeNav.canOccupy(level, this.getBoundingBox(), false)) {
                    this.loseGrip();
                    return;
                }
                this.trunkClimbs++;
                this.endTrunkRoute(60);
                return;
            }
            nx += dx / len * 0.2D;
            nz += dz / len * 0.2D;
        }
        AABB box = TreeNav.boxAt(new Vec3(nx, ny, nz), WIDTH, HEIGHT);
        if (!TreeNav.canOccupy(level, box, true)) {
            this.loseGrip();
            return;
        }
        this.setPos(nx, ny, nz);
    }

    private void releaseClimb() {
        this.noPhysics = false;
        this.setNoGravity(false);
    }

    /** The trunk was lost or the way is blocked: let go and fall with normal physics (it takes no fall damage). */
    private void loseGrip() {
        this.gripLosses++;
        this.releaseClimb();
        this.endTrunkRoute(160);
    }

    private void endTrunkRoute(int cooldown) {
        this.climbTrunk = null;
        this.trunkCooldown = cooldown;
        this.setActivity(Activity.GROUND);
    }

    // ---------------------------------------------------------------- the leap
    /** Starts the crouch of a leap at {@code target} (public for tests). Returns false when the state forbids it. */
    public boolean startLeap(LivingEntity target) {
        if (this.activity != Activity.GROUND || !this.isAlive()) {
            return false;
        }
        this.getNavigation().stop();
        this.setActivity(Activity.LEAP_PREPARE);
        this.setAnim(MacaqueAnim.LEAP_CROUCH);
        this.setTarget(target);
        this.leapTarget = target;
        this.waitTicks = 0;
        this.leapsStarted++;
        return true;
    }

    private void tickLeapPrepare() {
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        LivingEntity target = this.leapTarget != null ? this.leapTarget : this.getTarget();
        if (target != null) {
            this.faceTarget(target);
        }
        if (this.activityTimer >= LEAP_PREPARE_TICKS) {
            if (target == null || !target.isAlive()) {
                this.setActivity(Activity.GROUND);
                return;
            }
            this.launch(target);
        }
    }

    /** A real ballistic jump: the velocity is set once and gravity and drag do the rest. */
    private void launch(LivingEntity target) {
        Vec3 vel = target.getDeltaMovement();
        Vec3 to = target.position().add(vel.x * 5.0D, 0.0D, vel.z * 5.0D).subtract(this.position());
        double h = Math.hypot(to.x, to.z);
        Vec3 dir = h > 1.0E-4D ? new Vec3(to.x / h, 0.0D, to.z / h) : Vec3.ZERO;
        double speed = Mth.clamp(h * 0.14D, 0.3D, 0.7D);
        double vy = 0.42D + Mth.clamp(to.y, -1.0D, 4.0D) * 0.07D;
        this.leapDir = dir;
        this.setDeltaMovement(dir.x * speed, vy, dir.z * speed);
        this.setOnGround(false);
        this.faceDirection(dir.x, dir.z);
        this.hitDone = false;
        this.airTicks = 0;
        this.setActivity(Activity.AIRBORNE);
        this.setAnim(MacaqueAnim.LEAP_AIR);
        this.playSound(ModSounds.MACAQUE_LEAP.get(), 0.9F, 1.1F);
    }

    private void tickAirborne(ServerLevel level) {
        this.airTicks++;
        this.getNavigation().stop();
        LivingEntity target = this.leapTarget != null ? this.leapTarget : this.getTarget();
        if (!this.hitDone && target != null && target.isAlive() && this.getBoundingBox().inflate(0.3D).intersects(target.getBoundingBox())) {
            this.bite(level, target);
            return;
        }
        if (this.airTicks >= 4 && this.onGround()) {
            this.setActivity(Activity.LAND);
            this.setAnim(MacaqueAnim.LAND);
        } else if (this.airTicks > 40) {
            this.setActivity(Activity.LAND);
        }
    }

    private void bite(ServerLevel level, LivingEntity target) {
        this.hitDone = true;
        this.bitesLanded++;
        target.hurtServer(level, this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (this.leapDir.lengthSqr() > 1.0E-4D) {
            target.knockback(0.3D, -this.leapDir.x, -this.leapDir.z);
        }
        this.playSound(ModSounds.MACAQUE_BITE.get(), 1.0F, 1.2F);
        // spring backwards off the target
        this.setDeltaMovement(-this.leapDir.x * 0.4D, 0.34D, -this.leapDir.z * 0.4D);
        this.setOnGround(false);
        this.setActivity(Activity.RETREAT);
        this.setAnim(MacaqueAnim.BITE);
    }

    private void tickLand() {
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        if (this.activityTimer >= LAND_TICKS) {
            this.attackCooldown = LEAP_COOLDOWN - 30;
            this.setActivity(Activity.GROUND);
        }
    }

    private void tickRetreat(ServerLevel level) {
        if (this.activityTimer == 7) {
            this.setAnim(MacaqueAnim.RETREAT);
        }
        LivingEntity target = this.leapTarget != null ? this.leapTarget : this.getTarget();
        if (this.onGround() && this.activityTimer > 6 && target != null && this.pathRecalc <= 0) {
            Vec3 away = this.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
            if (away.lengthSqr() > 1.0E-4D) {
                Vec3 spot = this.position().add(away.normalize().scale(4.0D));
                this.getNavigation().moveTo(spot.x, spot.y, spot.z, 1.3D);
            }
            this.pathRecalc = 8;
        }
        if (this.activityTimer >= RETREAT_TICKS && this.onGround()) {
            this.attackCooldown = LEAP_COOLDOWN;
            this.setActivity(Activity.GROUND);
        }
    }

    // ---------------------------------------------------------------- persistence and sounds
    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.ownerUuid != null) {
            output.putString("Owner", this.ownerUuid.toString());
        }
        output.putBoolean("Allied", this.allied);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String owner = input.getStringOr("Owner", "");
        this.ownerUuid = owner.isEmpty() ? null : UUID.fromString(owner);
        this.allied = input.getBooleanOr("Allied", false);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.activity == Activity.GROUND ? ModSounds.MACAQUE_AMBIENT.get() : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MACAQUE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MACAQUE_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(ModSounds.MACAQUE_STEP.get(), 0.5F, 1.3F + this.random.nextFloat() * 0.2F);
    }
}
