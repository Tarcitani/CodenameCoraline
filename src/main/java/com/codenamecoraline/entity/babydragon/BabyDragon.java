package com.codenamecoraline.entity.babydragon;

import com.codenamecoraline.entity.PlayerSummon;
import com.codenamecoraline.registry.ModSounds;
import java.util.EnumMap;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** A persistent tameable companion. Perching never serializes, discards or replaces the entity. */
public final class BabyDragon extends TamableAnimal implements PlayerSummon {
    // Entity-local events: avoid vanilla packet-handler IDs (notably 63, which casts to Sniffer).
    public static final byte EVENT_TAKEOFF = 80;
    public static final byte EVENT_LANDING = 81;
    public static final byte EVENT_CHARGE = 82;
    public static final byte EVENT_SHOOT = 83;
    public static final byte EVENT_HURT = 84;
    public static final byte EVENT_HAPPY = 85;
    public static final byte EVENT_CANCEL_CHARGE = 86;
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> AIRBORNE = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DEFENSIVE = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.BOOLEAN);
    public enum Pose { IDLE, WALK, TAKEOFF, FLYING, HOVER, LANDING, SHOULDER_IDLE, FIREBALL_CHARGE, FIREBALL_SHOOT, HURT, SLEEP, HAPPY }
    public final EnumMap<Pose, AnimationState> animations = new EnumMap<>(Pose.class);
    private PathNavigation groundNavigation;
    private PathNavigation flightNavigation;
    private final DragonBrain brain;
    private int takeoffTicks, landingTicks, chargeTicks, shootTicks, happyTicks, shoulderGrace, crouchTicks;
    public float flightBlend;
    private boolean shoulderRequested;
    private float ownerHealth;
    private @Nullable Vec3 releasePosition;

    public BabyDragon(EntityType<? extends BabyDragon> type, Level level) {
        super(type, level);
        this.groundNavigation = this.navigation;
        this.flightNavigation = new FlyingPathNavigation(this, level);
        this.flightNavigation.setCanFloat(true);
        this.brain = new DragonBrain(this);
        this.xpReward = 2;
        this.setPathfindingMalus(PathType.LAVA, -1);
        this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, -1);
        this.setPathfindingMalus(PathType.FIRE, -1);
        for (Pose pose : Pose.values()) this.animations.put(pose, new AnimationState());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.ARMOR, 2)
                .add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.FLYING_SPEED, 0.34).add(Attributes.FOLLOW_RANGE, 16);
    }

    @Override protected PathNavigation createNavigation(Level level) { return new GroundPathNavigation(this, level); }
    @Override protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 7));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) {
        super.defineSynchedData(data);
        data.define(STATE, DragonState.WILD_IDLE.ordinal()); data.define(AIRBORNE, false);
        data.define(DEFENSIVE, true); data.define(SLEEPING, false);
    }
    public DragonState getDragonState() { return DragonState.values()[this.entityData.get(STATE)]; }
    public void setDragonState(DragonState state) { this.entityData.set(STATE, state.ordinal()); }
    public boolean isFlying() { return this.entityData.get(AIRBORNE); }
    public boolean isDefensive() { return this.entityData.get(DEFENSIVE); }
    public boolean isDragonSleeping() { return this.entityData.get(SLEEPING); }
    public boolean isOnShoulder() { return this.getDragonState() == DragonState.SHOULDER; }
    public boolean wantsShoulder() { return this.shoulderRequested; }
    public int getChargeTicks() { return this.chargeTicks; }
    public DragonBrain getDragonBrain() { return this.brain; }
    @Override public boolean isPlayerSummon() { return this.isTame(); }

    public void setFlying(boolean flying) {
        if (flying == this.isFlying()) return;
        this.navigation.stop();
        this.entityData.set(AIRBORNE, flying);
        this.setNoGravity(flying);
        this.navigation = flying ? this.flightNavigation : this.groundNavigation;
        this.moveControl = flying ? new DragonFlightControl(this) : new MoveControl(this);
        this.level().broadcastEntityEvent(this, flying ? EVENT_TAKEOFF : EVENT_LANDING);
        this.playSound((flying ? ModSounds.DRAGON_FLAP : ModSounds.DRAGON_LAND).get(), 0.28F, 1.5F);
    }

    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.isAlive()) this.brain.tick();
    }

    @Override public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.flightBlend += ((this.isFlying() ? 1 : 0) - this.flightBlend) * 0.15F;
            // Predict only the visual anchor; commands and all state changes remain server-authoritative.
            LivingEntity owner = this.getOwner();
            if (this.isOnShoulder() && owner != null) {
                this.setPos(shoulderPosition(owner)); this.setYRot(owner.yBodyRot); this.yBodyRot = owner.yBodyRot;
            }
            this.updateAnimations(); return;
        }
        if (this.happyTicks > 0) this.happyTicks--;
        if (this.isOnShoulder()) this.tickShoulder();
        this.entityData.set(SLEEPING, this.isOrderedToSit() && this.onGround() && this.level() instanceof ServerLevel server && Math.floorMod(server.getDefaultClockTime(), 24000L) >= 13000);
        if (this.isFlying() && this.tickCount % 24 == 0 && !this.isOnShoulder()) this.playSound(ModSounds.DRAGON_FLAP.get(), 0.14F, 1.6F);
    }

    @Override public void travel(Vec3 input) {
        if (this.isOnShoulder()) { this.setDeltaMovement(Vec3.ZERO); return; }
        if (this.isFlying()) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.96));
        } else super.travel(input);
    }

    public boolean isProtected(Entity entity) {
        if (entity == this || this.isOwnedByEntity(entity) || this.isAlliedTo(entity)) return true;
        if (entity instanceof PlayerSummon summon && summon.isPlayerSummon() && this.isTame()) return true;
        if (entity instanceof TamableAnimal pet && pet.isTame() && this.getOwnerReference() != null
                && Objects.equals(this.getOwnerReference(), pet.getOwnerReference())) return true;
        LivingEntity owner = this.getOwner();
        if (owner != null && owner.isAlliedTo(entity)) return true;
        return entity instanceof Player target && owner instanceof Player player && !player.canHarmPlayer(target);
    }

    private boolean isOwnedByEntity(Entity entity) {
        return this.getOwnerReference() != null && this.getOwnerReference().getUUID().equals(entity.getUUID());
    }

    /** Reactive targets only; the owner may explicitly engage any unprotected mob. */
    public boolean validDefensiveTarget(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive() || this.isProtected(target) || target.isSpectator()) return false;
        if (target instanceof Player p) return !p.isCreative() && this.getOwner() instanceof Player owner && owner.canHarmPlayer(p);
        if (target instanceof Enemy) return true;
        LivingEntity owner = this.getOwner();
        boolean attacking = this.getLastHurtByMob() == target && recent(this, this.getLastHurtByMobTimestamp())
                || owner != null && owner.getLastHurtByMob() == target && recent(owner, owner.getLastHurtByMobTimestamp());
        boolean directed = owner != null && owner.getLastHurtMob() == target && recent(owner, owner.getLastHurtMobTimestamp());
        return target instanceof Mob && (attacking || directed);
    }

    static boolean recent(LivingEntity entity, int stamp) { return entity.tickCount >= stamp && entity.tickCount - stamp <= 100; }

    @Override public boolean canAttack(LivingEntity target) { return this.validDefensiveTarget(target) && super.canAttack(target); }
    // isPushable alone only prevents receiving an impulse; doPush can still push the owner.
    @Override protected void pushEntities() { if (!this.isOnShoulder()) super.pushEntities(); }
    @Override public void push(Entity entity) { if (!this.isOnShoulder()) super.push(entity); }
    @Override public boolean isPushable() { return !this.isOnShoulder() && super.isPushable(); }
    @Override public boolean causeFallDamage(double distance, float multiplier, DamageSource source) { return false; }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FALL)) return false;
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) { if (this.isOnShoulder()) this.leaveShoulder(); level.broadcastEntityEvent(this, EVENT_HURT); }
        return hurt;
    }

    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.SALMON)) {
            if (this.isTame() && !this.isOwnedByEntity(player)) return InteractionResult.PASS;
            if (!this.level().isClientSide()) {
                stack.consume(1, player);
                this.getLookControl().setLookAt(player);
                this.happy();
                if (!this.isTame()) {
                    boolean success = this.random.nextInt(3) == 0 && !net.neoforged.neoforge.event.EventHooks.onAnimalTame(this, player);
                    if (success) { this.tame(player); this.setPersistenceRequired(); }
                    this.level().broadcastEntityEvent(this, (byte)(success ? 7 : 6));
                } else { this.heal(4); this.level().broadcastEntityEvent(this, (byte)7); }
            }
            return InteractionResult.SUCCESS;
        }
        if (this.isTame() && this.isOwnedByEntity(player)) {
            if (!this.level().isClientSide()) {
                if (player.isShiftKeyDown() && stack.is(Items.STICK)) {
                    this.entityData.set(DEFENSIVE, !this.isDefensive()); this.setTarget(null);
                    player.sendOverlayMessage(Component.translatable(this.isDefensive() ? "message.codenamecoraline.dragon.defense_on" : "message.codenamecoraline.dragon.defense_off"));
                } else if (stack.isEmpty()) {
                    if (player.isShiftKeyDown()) {
                        if (this.isOnShoulder()) this.leaveShoulder(); else this.requestShoulder(player);
                    } else {
                        if (this.isOnShoulder()) this.leaveShoulder();
                        this.shoulderRequested = false; this.setOrderedToSit(!this.isOrderedToSit());
                        this.setTarget(null); this.navigation.stop();
                        player.sendOverlayMessage(Component.translatable(this.isOrderedToSit() ? "message.codenamecoraline.dragon.stay" : "message.codenamecoraline.dragon.follow"));
                    }
                } else return super.mobInteract(player, hand);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public void happy() { this.happyTicks = 30; this.level().broadcastEntityEvent(this, EVENT_HAPPY); this.playSound(ModSounds.DRAGON_HAPPY.get(), 0.5F, 1.6F); }
    public boolean requestShoulder(Player player) {
        if (!this.isOwnedByEntity(player) || !player.isAlive() || player.isInLiquid() || player.isPassenger()) return false;
        this.setOrderedToSit(false); this.setTarget(null); this.shoulderRequested = true;
        this.setDragonState(DragonState.RETURNING); this.setFlying(true); return true;
    }

    public static Vec3 shoulderPosition(LivingEntity owner) {
        return owner.position().add(new Vec3(0.39, owner.isShiftKeyDown() ? 1.15 : 1.43, 0.04).yRot(-owner.yBodyRot * (float)Math.PI / 180));
    }

    public boolean landOnShoulder(Player owner) {
        if (!this.isOwnedByEntity(owner) || this.distanceToSqr(owner) > 9 || owner.isInLiquid() || owner.isPassenger()) return false;
        Vec3 anchor = shoulderPosition(owner);
        if (!this.clearBlocks(anchor)) return false;
        for (BabyDragon other : this.level().getEntitiesOfClass(BabyDragon.class, owner.getBoundingBox().inflate(2))) {
            if (other != this && other.isOnShoulder() && Objects.equals(other.getOwnerReference(), this.getOwnerReference())) return false;
        }
        this.releasePosition = this.position(); this.setFlying(false); this.setNoGravity(true);
        this.setDragonState(DragonState.SHOULDER); this.setInSittingPose(false);
        this.shoulderRequested = false; this.navigation.stop(); this.setDeltaMovement(Vec3.ZERO);
        this.setPos(anchor); this.shoulderGrace = 40; this.ownerHealth = owner.getHealth();
        this.level().broadcastEntityEvent(this, EVENT_LANDING); return true;
    }

    private void tickShoulder() {
        LivingEntity owner = this.getOwner();
        if (owner == null || !owner.isAlive() || owner.isInLiquid() || owner.isPassenger() || owner.isFallFlying()
                || owner.isSpectator() || owner.getHealth() < this.ownerHealth - 3.9F || !this.clearBlocks(shoulderPosition(owner))) {
            this.leaveShoulder(); return;
        }
        this.ownerHealth = owner.getHealth();
        this.setPos(shoulderPosition(owner)); this.setYRot(owner.yBodyRot); this.yBodyRot = owner.yBodyRot;
        this.setDeltaMovement(Vec3.ZERO); this.fallDistance = 0; this.setNoGravity(true);
        if (this.shoulderGrace > 0) this.shoulderGrace--;
        else { this.crouchTicks = owner.isShiftKeyDown() ? this.crouchTicks + 1 : 0; if (this.crouchTicks >= 20) this.leaveShoulder(); }
    }

    public void leaveShoulder() {
        this.shoulderRequested = false; this.crouchTicks = 0;
        LivingEntity owner = this.getOwner();
        Vec3 safe = owner == null ? null : this.findSafePosition(owner.blockPosition(), true);
        if (safe == null && this.releasePosition != null && this.safeGround(this.releasePosition)) safe = this.releasePosition;
        if (safe == null) {
            // A swimmer or an elytra user may have no nearby ground. Release into clear air instead.
            search: for (int dy = 0; dy <= 4; dy++) for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
                Vec3 candidate = this.position().add(dx, dy, dz);
                if (this.level().hasChunkAt(BlockPos.containing(candidate)) && this.clearBlocks(candidate)) { safe = candidate; break search; }
            }
        }
        this.setDragonState(DragonState.RETURNING); this.setFlying(true);
        if (safe != null) this.setPos(safe);
        this.setDeltaMovement(Vec3.ZERO); this.brain.delayAmbient(100);
    }

    public boolean clearBlocks(Vec3 pos) {
        AABB box = this.getBoundingBox().move(pos.subtract(this.position()));
        return this.level().getWorldBorder().isWithinBounds(box) && !this.level().getBlockCollisions(this, box).iterator().hasNext()
                && !this.level().containsAnyLiquid(box);
    }
    public boolean safeGround(Vec3 pos) {
        BlockPos below = BlockPos.containing(pos).below();
        return this.clearBlocks(pos) && this.level().getBlockState(below).isCollisionShapeFullBlock(this.level(), below)
                && !this.level().getBlockState(below).is(Blocks.MAGMA_BLOCK) && !this.level().getBlockState(below).is(Blocks.CAMPFIRE)
                && !this.level().getBlockState(below).is(Blocks.SOUL_CAMPFIRE);
    }
    public @Nullable Vec3 findSafePosition(BlockPos center, boolean allowAir) {
        for (int dy = 0; dy <= 3; dy++) for (int radius = 2; radius <= 4; radius++)
            for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dx) != radius && Math.abs(dz) != radius) continue;
                BlockPos pos = center.offset(dx, dy, dz);
                if (!this.level().hasChunkAt(pos)) continue;
                Vec3 candidate = Vec3.atBottomCenterOf(pos);
                if ((allowAir ? this.clearBlocks(candidate) && this.safeGround(candidate.add(0, -dy, 0)) : this.safeGround(candidate))
                        && this.level().noCollision(this, this.getBoundingBox().move(candidate.subtract(this.position())))) return candidate;
            }
        return null;
    }
    public boolean teleportNearOwner() {
        LivingEntity owner = this.getOwner();
        if (owner == null || this.isOrderedToSit() || this.isLeashed() || this.isPassenger() || this.isOnShoulder()
                || this.distanceToSqr(owner) <= 24 * 24) return false;
        Vec3 safe = this.findSafePosition(owner.blockPosition(), false);
        if (safe == null) return false;
        this.navigation.stop(); this.setPos(safe); this.setDeltaMovement(Vec3.ZERO); this.setFlying(false); return true;
    }

    public void beginCharge() { this.chargeTicks = 12; this.level().broadcastEntityEvent(this, EVENT_CHARGE); this.playSound(ModSounds.DRAGON_CHARGE.get(), 0.35F, 1.7F); }
    public void cancelCharge() { if (this.chargeTicks > 0) this.level().broadcastEntityEvent(this, EVENT_CANCEL_CHARGE); this.chargeTicks = 0; }
    public boolean chargeReady() { return this.chargeTicks > 0 && --this.chargeTicks == 0; }
    public void shoot(LivingEntity target) {
        DragonEmber ember = new DragonEmber(this.level(), this);
        Vec3 mouth = this.position().add(this.getLookAngle().scale(0.36)).add(0, 0.42, 0);
        ember.setPos(mouth);
        Vec3 center = target.getBoundingBox().getCenter();
        // Lead a moving target at release time, accounting for the projectile's vanilla air drag.
        Vec3 velocity = target.getDeltaMovement();
        if (target.onGround()) velocity = new Vec3(velocity.x, 0, velocity.z);
        if (velocity.lengthSqr() > 1) velocity = velocity.normalize();
        Vec3 aim = center.subtract(mouth);
        for (int i = 0; i < 5; i++) {
            double distance = Math.min(DragonEmber.MAX_RANGE, aim.length());
            double travelTicks = Math.log(1 - distance * 0.01 / DragonEmber.SPEED) / Math.log(0.99);
            aim = center.add(velocity.scale(travelTicks)).subtract(mouth);
        }
        ember.shoot(aim.x, aim.y, aim.z, DragonEmber.SPEED, 0);
        this.level().addFreshEntity(ember); this.level().broadcastEntityEvent(this, EVENT_SHOOT);
        this.playSound(ModSounds.DRAGON_SHOOT.get(), 0.45F, 1.8F);
    }

    private void updateAnimations() {
        if (this.takeoffTicks > 0) this.takeoffTicks--;
        if (this.landingTicks > 0) this.landingTicks--;
        if (this.chargeTicks > 0) {
            this.chargeTicks--;
            Vec3 mouth = this.position().add(this.getLookAngle().scale(0.4)).add(0, 0.4, 0);
            this.level().addParticle(ParticleTypes.SMALL_FLAME, mouth.x, mouth.y, mouth.z, 0, 0.015, 0);
        }
        if (this.shootTicks > 0) this.shootTicks--;
        if (this.happyTicks > 0) this.happyTicks--;
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 0.004;
        for (Pose pose : Pose.values()) {
            boolean active = switch (pose) {
                case IDLE -> !this.isFlying() && !this.isOnShoulder() && !this.isDragonSleeping();
                case WALK -> !this.isFlying() && moving && !this.isOnShoulder();
                case TAKEOFF -> this.takeoffTicks > 0;
                case FLYING -> this.isFlying() && moving;
                case HOVER -> this.isFlying() && !moving;
                case LANDING -> this.landingTicks > 0;
                case SHOULDER_IDLE -> this.isOnShoulder();
                case FIREBALL_CHARGE -> this.chargeTicks > 0;
                case FIREBALL_SHOOT -> this.shootTicks > 0;
                case HURT -> this.hurtTime > 0;
                case SLEEP -> this.isDragonSleeping();
                case HAPPY -> this.happyTicks > 0;
            };
            this.animations.get(pose).animateWhen(active, this.tickCount);
        }
    }
    @Override public void handleEntityEvent(byte event) {
        switch (event) {
            case EVENT_TAKEOFF -> this.takeoffTicks = 14;
            case EVENT_LANDING -> this.landingTicks = 12;
            case EVENT_CHARGE -> this.chargeTicks = 12;
            case EVENT_SHOOT -> { this.chargeTicks = 0; this.shootTicks = 8; }
            case EVENT_HURT -> this.animations.get(Pose.HURT).start(this.tickCount);
            case EVENT_HAPPY -> this.happyTicks = 30;
            case EVENT_CANCEL_CHARGE -> this.chargeTicks = 0;
            default -> super.handleEntityEvent(event);
        }
    }
    @Override public boolean isFood(ItemStack stack) { return false; }
    @Override public boolean canMate(Animal other) { return false; }
    @Override public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) { return null; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected @Nullable SoundEvent getAmbientSound() { return ModSounds.DRAGON_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModSounds.DRAGON_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModSounds.DRAGON_DEATH.get(); }
    @Override public float getVoicePitch() { return 1.5F + this.random.nextFloat() * 0.25F; }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("DragonDefensive", this.isDefensive()); output.putBoolean("DragonShoulder", this.isOnShoulder());
        output.putInt("DragonFireCooldown", this.brain.getAttackCooldown());
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DEFENSIVE, input.getBooleanOr("DragonDefensive", true));
        this.setDragonState(input.getBooleanOr("DragonShoulder", false) && this.isTame() ? DragonState.SHOULDER : this.isOrderedToSit() ? DragonState.SITTING : DragonState.WILD_IDLE);
        this.brain.setAttackCooldown(input.getIntOr("DragonFireCooldown", 0));
        this.shoulderGrace = 40;
        LivingEntity owner = this.getOwner(); this.ownerHealth = owner == null ? 0 : owner.getHealth();
        this.setNoGravity(this.isOnShoulder());
    }
    public static boolean checkSpawnRules(EntityType<BabyDragon> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        BlockPos floor = pos.below();
        return level.canSeeSky(pos) && level.getRawBrightness(pos, 0) >= 9 && level.getFluidState(pos).isEmpty()
                && level.getBlockState(floor).isCollisionShapeFullBlock(level, floor)
                && !level.getBlockState(floor).is(Blocks.MAGMA_BLOCK) && level.noCollision(type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
    }
}
