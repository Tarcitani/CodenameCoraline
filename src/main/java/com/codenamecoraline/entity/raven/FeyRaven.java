package com.codenamecoraline.entity.raven;

import com.codenamecoraline.entity.PlayerSummon;
import com.codenamecoraline.registry.ModSounds;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Fey Raven ({@code codenamecoraline:fey_raven}): a three-eyed mystic messenger and companion
 * of the twilight courts. Implements taming with Glow Berries, Follow/Sit/Sentinel command modes,
 * non-destructive shoulder perching, 1-slot messenger scroll case, Mystic Sentinel invisible-threat
 * reveal, 4-phase aerial peck defense, and strict friendly-fire immunity.
 */
public final class FeyRaven extends TamableAnimal implements PlayerSummon {
    public static final float WIDTH = 0.5F;
    public static final float HEIGHT = 0.65F;
    public static final float EYE_HEIGHT = 0.55F;

    public static final float HEAL_AMOUNT = 4.0F;
    public static final double FOLLOW_MIN_DIST_SQR = 9.0D;     // 3 blocks
    public static final double FOLLOW_MAX_DIST_SQR = 64.0D;    // 8 blocks
    public static final double TELEPORT_DIST_SQR = 400.0D;     // 20 blocks
    public static final double SENTINEL_RADIUS = 3.5D;
    public static final double MYSTIC_SENTINEL_RANGE = 16.0D;
    public static final int MYSTIC_SENTINEL_SCAN_INTERVAL = 20;
    public static final int MYSTIC_SENTINEL_GLOW_TICKS = 100;  // 5 seconds
    public static final int MYSTIC_SENTINEL_COOLDOWN = 600;    // 30 seconds

    public static final int PECK_DURATION_TICKS = 15;
    public static final int PECK_HIT_TICK = 8;
    public static final int PECK_COOLDOWN_TICKS = 20;
    public static final double PECK_REACH_SQR = 6.25D;         // 2.5 blocks

    // Reserved entity event range: 140..149
    public static final byte EVENT_BLINK_THIRD_EYE = (byte) 140;
    public static final byte EVENT_MYSTIC_ALERT = (byte) 141;
    public static final byte EVENT_OPEN_SCROLL = (byte) 142;
    public static final byte EVENT_IDLE_CLEAN = (byte) 143;
    public static final byte EVENT_IDLE_CAW = (byte) 144;
    public static final byte EVENT_PECK = (byte) 145;
    public static final byte EVENT_HURT_REACTION = (byte) 146;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(FeyRaven.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COMMAND =
            SynchedEntityData.defineId(FeyRaven.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_AIRBORNE =
            SynchedEntityData.defineId(FeyRaven.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> DATA_SCROLL_ITEM =
            SynchedEntityData.defineId(FeyRaven.class, EntityDataSerializers.ITEM_STACK);

    public enum State {
        WILD_IDLE,
        FOLLOWING,
        SITTING,
        SENTINEL,
        PERCHED_ON_SHOULDER,
        ALERTING,
        PECKING
    }

    public enum CommandMode {
        FOLLOW,
        SIT,
        SENTINEL
    }

    public final AnimationState idleBreatheAnimationState = new AnimationState();
    public final AnimationState idleHeadTiltAnimationState = new AnimationState();
    public final AnimationState idlePreenAnimationState = new AnimationState();
    public final AnimationState idleCawAnimationState = new AnimationState();
    public final AnimationState flyFlapAnimationState = new AnimationState();
    public final AnimationState flyGlideAnimationState = new AnimationState();
    public final AnimationState peckAttackAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private @Nullable Vec3 sentinelAnchor = null;
    private int alertCooldown = 0;
    private int alertPoseTicks = 0;
    private int peckTick = 0;
    private int peckCooldown = 0;
    private int idleVariantCooldown = 120;
    private boolean scrollDroppedOnDeath = false;

    public FeyRaven(EntityType<? extends FeyRaven> type, Level level) {
        super(type, level);
        this.xpReward = 3;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 14.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FLYING_SPEED, 0.40D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, State.WILD_IDLE.ordinal());
        builder.define(DATA_COMMAND, CommandMode.FOLLOW.ordinal());
        builder.define(DATA_AIRBORNE, false);
        builder.define(DATA_SCROLL_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                this, Silverfish.class, 10, true, false,
                (entity, level) -> !this.isOrderedToSit() && this.getState() != State.PERCHED_ON_SHOULDER));
    }

    @Override
    public boolean isPlayerSummon() {
        return this.isTame();
    }

    public State getState() {
        int idx = this.entityData.get(DATA_STATE);
        State[] values = State.values();
        return idx >= 0 && idx < values.length ? values[idx] : State.WILD_IDLE;
    }

    public void setState(State state) {
        this.entityData.set(DATA_STATE, state.ordinal());
    }

    public CommandMode getCommandMode() {
        int idx = this.entityData.get(DATA_COMMAND);
        CommandMode[] values = CommandMode.values();
        return idx >= 0 && idx < values.length ? values[idx] : CommandMode.FOLLOW;
    }

    public void setCommandMode(CommandMode mode) {
        this.entityData.set(DATA_COMMAND, mode.ordinal());
        if (mode == CommandMode.SIT) {
            this.setOrderedToSit(true);
            this.setAirborne(false);
            this.setState(State.SITTING);
            this.getNavigation().stop();
        } else if (mode == CommandMode.SENTINEL) {
            this.setOrderedToSit(false);
            this.sentinelAnchor = this.position();
            this.setAirborne(true);
            this.setState(State.SENTINEL);
        } else {
            this.setOrderedToSit(false);
            this.sentinelAnchor = null;
            this.setState(this.isTame() ? State.FOLLOWING : State.WILD_IDLE);
        }
    }

    public boolean isAirborne() {
        return this.entityData.get(DATA_AIRBORNE);
    }

    public void setAirborne(boolean airborne) {
        this.entityData.set(DATA_AIRBORNE, airborne);
    }

    public ItemStack getScrollItem() {
        return this.entityData.get(DATA_SCROLL_ITEM);
    }

    public void setScrollItem(ItemStack stack) {
        this.entityData.set(DATA_SCROLL_ITEM, stack.copy());
    }

    public boolean isOnShoulder() {
        return this.getState() == State.PERCHED_ON_SHOULDER;
    }

    public int getAlertCooldown() {
        return this.alertCooldown;
    }

    public void setAlertCooldown(int cooldown) {
        this.alertCooldown = Math.max(0, cooldown);
    }

    @Override
    public void tame(Player player) {
        super.tame(player);
        this.setCommandMode(CommandMode.FOLLOW);
        this.setState(State.FOLLOWING);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        // 1. Wild taming with Glow Berries (33% chance per berry)
        if (!this.isTame()) {
            if (!held.is(Items.GLOW_BERRIES)) {
                return super.mobInteract(player, hand);
            }
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            if (!this.level().isClientSide()) {
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Reject interactions from non-owners
        if (!this.isOwnedBy(player)) {
            return InteractionResult.PASS;
        }

        // 2. Healing with Glow Berries or Sweet Berries (+4.0 HP)
        if (this.isFood(held) && this.getHealth() < this.getMaxHealth()) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            this.heal(HEAL_AMOUNT);
            return InteractionResult.SUCCESS;
        }

        // 3. Sneak + Right-Click: Messenger Scroll 1-slot inventory exchange
        if (player.isShiftKeyDown()) {
            if (!this.level().isClientSide()) {
                ItemStack currentScroll = this.getScrollItem();
                if (!currentScroll.isEmpty()) {
                    if (!player.getInventory().add(currentScroll.copy())) {
                        this.spawnAtLocation((ServerLevel) this.level(), currentScroll.copy());
                    }
                    this.setScrollItem(ItemStack.EMPTY);
                    this.level().broadcastEntityEvent(this, EVENT_OPEN_SCROLL);
                } else if (!held.isEmpty()) {
                    ItemStack single = held.copyWithCount(1);
                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                    this.setScrollItem(single);
                    this.level().broadcastEntityEvent(this, EVENT_OPEN_SCROLL);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // 4. Empty-hand right-click (or holding non-food item while not sneaking): cycle CommandMode
        if (held.isEmpty() || !this.isFood(held)) {
            if (!this.level().isClientSide()) {
                if (this.isOnShoulder()) {
                    this.leaveShoulder();
                } else {
                    CommandMode next = switch (this.getCommandMode()) {
                        case FOLLOW -> CommandMode.SIT;
                        case SIT -> CommandMode.SENTINEL;
                        case SENTINEL -> CommandMode.FOLLOW;
                    };
                    this.setCommandMode(next);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    public static Vec3 shoulderPosition(Player owner) {
        float yawRad = owner.yBodyRot * Mth.DEG_TO_RAD;
        double leftX = Mth.cos(yawRad) * 0.42D;
        double leftZ = Mth.sin(yawRad) * 0.42D;
        return new Vec3(owner.getX() + leftX, owner.getY() + owner.getBbHeight() - 0.25D, owner.getZ() + leftZ);
    }

    public boolean clearBlocks(Vec3 pos) {
        AABB box = this.getType().getDimensions().makeBoundingBox(pos);
        return this.level().noCollision(this, box) && !this.level().containsAnyLiquid(box);
    }

    public boolean landOnShoulder(Player owner) {
        if (!this.isTame() || !this.isOwnedBy(owner) || !owner.isAlive() || owner.isShiftKeyDown()) {
            return false;
        }
        Vec3 targetPos = shoulderPosition(owner);
        if (!this.clearBlocks(targetPos)) {
            return false;
        }
        this.setOrderedToSit(false);
        this.setTarget(null);
        this.getNavigation().stop();
        this.setNoGravity(true);
        this.setAirborne(false);
        this.setState(State.PERCHED_ON_SHOULDER);
        this.setPos(targetPos);
        this.setDeltaMovement(Vec3.ZERO);
        return true;
    }

    public void leaveShoulder() {
        if (!this.isOnShoulder()) {
            return;
        }
        this.setNoGravity(false);
        LivingEntity owner = this.getOwner();
        if (owner instanceof Player player) {
            Vec3 safe = this.findSafeExitNear(player.position());
            this.setPos(safe);
        }
        this.setState(this.getCommandMode() == CommandMode.SIT ? State.SITTING : State.FOLLOWING);
    }

    private Vec3 findSafeExitNear(Vec3 origin) {
        for (double dy : new double[]{0.5D, 1.0D, 0.0D}) {
            for (double dx : new double[]{0.0D, 0.8D, -0.8D}) {
                for (double dz : new double[]{0.0D, 0.8D, -0.8D}) {
                    Vec3 candidate = origin.add(dx, dy, dz);
                    if (this.clearBlocks(candidate)) {
                        return candidate;
                    }
                }
            }
        }
        return origin.add(0.0D, 0.6D, 0.0D);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.updateClientAnimations();
            return;
        }
        this.tickServerCompanionAi();
    }

    private void tickServerCompanionAi() {
        if (this.alertCooldown > 0) {
            this.alertCooldown--;
        }
        if (this.peckCooldown > 0) {
            this.peckCooldown--;
        }
        if (this.alertPoseTicks > 0) {
            this.alertPoseTicks--;
            if (this.alertPoseTicks == 0 && this.getState() == State.ALERTING) {
                this.restorePostAlertState();
            }
        }

        // Sanitize target: never keep a protected ally or uninvolved peaceful target
        LivingEntity currentTarget = this.getTarget();
        if (currentTarget != null && !this.validDefensiveTarget(currentTarget)) {
            this.setTarget(null);
            currentTarget = null;
        }

        // Shoulder perch maintenance
        if (this.isOnShoulder()) {
            this.tickShoulderPerch();
            return;
        }

        // Mystic Sentinel periodic scan (every 20 ticks when tamed)
        if (this.isTame() && this.alertCooldown == 0 && this.tickCount % MYSTIC_SENTINEL_SCAN_INTERVAL == 0) {
            this.tickMysticSentinel();
        }

        // Active combat: 4-phase Peck Attack
        if (currentTarget != null && !this.isOrderedToSit()) {
            this.tickCombatPeck(currentTarget);
            return;
        }
        if (this.peckTick > 0) {
            this.peckTick = 0;
        }

        if (this.isTame()) {
            this.tickOwnerCommands();
        } else {
            this.setState(State.WILD_IDLE);
            this.setAirborne(!this.onGround());
        }

        this.tickIdleVariations();
    }

    private void tickShoulderPerch() {
        LivingEntity owner = this.getOwner();
        if (!(owner instanceof Player player)
                || !player.isAlive()
                || player.isShiftKeyDown()
                || player.isInWater()
                || this.getTarget() != null) {
            this.leaveShoulder();
            return;
        }
        Vec3 perch = shoulderPosition(player);
        if (!this.clearBlocks(perch)) {
            this.leaveShoulder();
            return;
        }
        this.setPos(perch);
        this.setDeltaMovement(Vec3.ZERO);
        this.setYRot(player.yBodyRot);
        this.setYHeadRot(player.getYHeadRot());
    }

    private void tickOwnerCommands() {
        LivingEntity owner = this.getOwner();
        // If owner died, land and hold vigil in SITTING state until owner returns alive
        if (owner != null && !owner.isAlive()) {
            this.setOrderedToSit(true);
            this.setAirborne(false);
            this.setState(State.SITTING);
            this.getNavigation().stop();
            return;
        }

        CommandMode mode = this.getCommandMode();
        if (mode == CommandMode.SIT || this.isOrderedToSit()) {
            this.setAirborne(false);
            this.setState(State.SITTING);
            this.getNavigation().stop();
            return;
        }

        if (mode == CommandMode.SENTINEL) {
            if (this.sentinelAnchor == null) {
                this.sentinelAnchor = this.position();
            }
            this.setState(State.SENTINEL);
            this.setAirborne(true);
            double angle = this.tickCount * 0.08D;
            double targetX = this.sentinelAnchor.x + Math.cos(angle) * SENTINEL_RADIUS;
            double targetZ = this.sentinelAnchor.z + Math.sin(angle) * SENTINEL_RADIUS;
            double targetY = this.sentinelAnchor.y + 1.4D;
            Vec3 delta = new Vec3(targetX - this.getX(), targetY - this.getY(), targetZ - this.getZ());
            if (delta.lengthSqr() > 0.01D) {
                Vec3 step = delta.normalize().scale(0.18D);
                this.setDeltaMovement(step);
                this.setYRot((float) (Mth.atan2(-step.x, step.z) * Mth.RAD_TO_DEG));
            }
            return;
        }

        // CommandMode.FOLLOW
        if (this.getState() != State.ALERTING) {
            this.setState(State.FOLLOWING);
        }
        if (owner == null) {
            return;
        }

        double distSqr = this.distanceToSqr(owner);
        if (distSqr > TELEPORT_DIST_SQR) {
            this.teleportNearOwner();
            return;
        }

        // Jump-to-shoulder check when close to a jumping owner
        if (owner instanceof Player player
                && distSqr < 4.0D
                && !player.onGround()
                && player.getDeltaMovement().y > 0.05D
                && !player.isShiftKeyDown()) {
            if (this.landOnShoulder(player)) {
                return;
            }
        }

        if (distSqr > FOLLOW_MAX_DIST_SQR) {
            this.setAirborne(true);
            Vec3 toward = owner.position().add(0.0D, 1.0D, 0.0D).subtract(this.position());
            if (toward.lengthSqr() > 0.01D) {
                Vec3 vel = toward.normalize().scale(0.28D);
                this.setDeltaMovement(vel);
            }
            this.getNavigation().moveTo(owner, 1.25D);
        } else if (distSqr > FOLLOW_MIN_DIST_SQR) {
            this.setAirborne(!this.onGround());
            this.getNavigation().moveTo(owner, 1.0D);
        } else {
            this.setAirborne(!this.onGround());
            this.getNavigation().stop();
        }
    }

    public boolean teleportNearOwner() {
        LivingEntity owner = this.getOwner();
        if (owner == null || !owner.isAlive()) {
            return false;
        }
        BlockPos ownerPos = owner.blockPosition();
        for (int dy = 2; dy >= 0; dy--) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1 && dy == 0) {
                        continue;
                    }
                    Vec3 candidate = new Vec3(
                            ownerPos.getX() + dx + 0.5D,
                            ownerPos.getY() + dy,
                            ownerPos.getZ() + dz + 0.5D);
                    if (this.isSafeTeleportDestination(candidate)) {
                        this.setPos(candidate);
                        this.setDeltaMovement(Vec3.ZERO);
                        this.getNavigation().stop();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isSafeTeleportDestination(Vec3 pos) {
        if (!this.clearBlocks(pos)) {
            return false;
        }
        BlockPos blockPos = BlockPos.containing(pos);
        BlockPos belowPos = blockPos.below();
        BlockState below = this.level().getBlockState(belowPos);
        if (below.is( net.minecraft.world.level.block.Blocks.LAVA )
                || below.is( net.minecraft.world.level.block.Blocks.FIRE )
                || below.is( net.minecraft.world.level.block.Blocks.MAGMA_BLOCK )
                || this.level().getFluidState(belowPos).is(FluidTags.LAVA)
                || this.level().getFluidState(blockPos).is(FluidTags.LAVA)) {
            return false;
        }
        return below.isSolidRender() || below.is(BlockTags.LEAVES);
    }

    public boolean triggerMysticSentinelScan() {
        return this.tickMysticSentinel();
    }

    private boolean tickMysticSentinel() {
        LivingEntity owner = this.getOwner();
        Vec3 center = (owner != null && owner.distanceToSqr(this) <= 1024.0D) ? owner.position() : this.position();
        AABB scanBox = new AABB(center, center).inflate(MYSTIC_SENTINEL_RANGE);
        List<LivingEntity> threats = this.level().getEntitiesOfClass(
                LivingEntity.class,
                scanBox,
                e -> e.isAlive() && e instanceof Enemy && !this.isProtectedAlly(e));

        for (LivingEntity threat : threats) {
            boolean ambushingBehindOwner = false;
            if (owner != null) {
                Vec3 ownerLook = owner.getViewVector(1.0F).normalize();
                Vec3 toThreat = threat.position().subtract(owner.position()).normalize();
                ambushingBehindOwner = ownerLook.dot(toThreat) < -0.15D;
            }
            if (threat.isInvisible() || threat.hasEffect(MobEffects.INVISIBILITY) || ambushingBehindOwner) {
                threat.addEffect(new MobEffectInstance(MobEffects.GLOWING, MYSTIC_SENTINEL_GLOW_TICKS, 0));
                this.alertCooldown = MYSTIC_SENTINEL_COOLDOWN;
                this.alertPoseTicks = 25;
                this.setState(State.ALERTING);
                this.level().broadcastEntityEvent(this, EVENT_MYSTIC_ALERT);
                this.playSound(ModSounds.FEY_RAVEN_ALERT.get(), 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }

    private void tickCombatPeck(LivingEntity target) {
        this.setAirborne(true);
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double distSqr = this.distanceToSqr(target);

        if (this.peckTick == 0) {
            if (distSqr <= PECK_REACH_SQR && this.peckCooldown == 0 && this.hasLineOfSight(target)) {
                this.peckTick = 1;
                this.setState(State.PECKING);
                this.level().broadcastEntityEvent(this, EVENT_PECK);
            } else {
                this.setState(this.isTame() ? State.FOLLOWING : State.WILD_IDLE);
                Vec3 approach = target.getEyePosition().subtract(this.position());
                if (approach.lengthSqr() > 0.01D) {
                    this.setDeltaMovement(approach.normalize().scale(0.32D));
                }
                this.getNavigation().moveTo(target, 1.35D);
            }
            return;
        }

        this.peckTick++;
        // Phase 2 & 3: Validation (Anti-Whiff) + Impact at tick 8
        if (this.peckTick == PECK_HIT_TICK && this.level() instanceof ServerLevel serverLevel) {
            if (target.isAlive()
                    && !this.isProtectedAlly(target)
                    && this.distanceToSqr(target) <= PECK_REACH_SQR
                    && this.hasLineOfSight(target)) {
                this.doHurtTarget(serverLevel, target);
                // Evasive aerial rebound away from target after strike
                Vec3 away = this.position().subtract(target.position());
                Vec3 horizontal = new Vec3(away.x, 0.0D, away.z);
                Vec3 rebound = (horizontal.lengthSqr() > 1e-4D ? horizontal.normalize() : new Vec3(0.0D, 0.0D, 1.0D))
                        .scale(0.35D)
                        .add(0.0D, 0.22D, 0.0D);
                this.setDeltaMovement(rebound);
            }
        }

        // Phase 4: Recovery completes at tick 15
        if (this.peckTick >= PECK_DURATION_TICKS) {
            this.peckTick = 0;
            this.peckCooldown = PECK_COOLDOWN_TICKS;
            this.restorePostAlertState();
        }
    }

    private void restorePostAlertState() {
        if (!this.isTame()) {
            this.setState(State.WILD_IDLE);
            return;
        }
        this.setState(switch (this.getCommandMode()) {
            case SIT -> State.SITTING;
            case SENTINEL -> State.SENTINEL;
            case FOLLOW -> State.FOLLOWING;
        });
    }

    private void tickIdleVariations() {
        if (this.getState() == State.PECKING || this.getState() == State.ALERTING || this.isAirborne()) {
            return;
        }
        if (this.idleVariantCooldown > 0) {
            this.idleVariantCooldown--;
            return;
        }
        this.idleVariantCooldown = 100 + this.random.nextInt(101);
        byte eventId = switch (this.random.nextInt(3)) {
            case 0 -> EVENT_IDLE_CLEAN;
            case 1 -> EVENT_BLINK_THIRD_EYE;
            default -> EVENT_IDLE_CAW;
        };
        this.level().broadcastEntityEvent(this, eventId);
    }

    public boolean isProtectedAlly(@Nullable Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity == this) {
            return true;
        }
        if (!this.isTame()) {
            return false;
        }
        var myOwnerRef = this.getOwnerReference();
        if (myOwnerRef == null) {
            return false;
        }
        if (entity.getUUID().equals(myOwnerRef.getUUID())) {
            return true;
        }
        if (entity instanceof TamableAnimal otherTamable && otherTamable.isTame()) {
            var otherOwnerRef = otherTamable.getOwnerReference();
            if (otherOwnerRef != null && Objects.equals(myOwnerRef.getUUID(), otherOwnerRef.getUUID())) {
                return true;
            }
        }
        if (entity instanceof PlayerSummon summon && summon.isPlayerSummon()) {
            return true;
        }
        return false;
    }

    public boolean validDefensiveTarget(@Nullable LivingEntity candidate) {
        if (candidate == null || !candidate.isAlive() || this.isProtectedAlly(candidate)) {
            return false;
        }
        if (candidate instanceof Silverfish) {
            return true;
        }
        if (this.getLastHurtByMob() == candidate) {
            return true;
        }
        LivingEntity owner = this.getOwner();
        if (owner != null) {
            if (owner.getLastHurtByMob() == candidate || owner.getLastHurtMob() == candidate) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (this.isProtectedAlly(target)) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (this.isProtectedAlly(target)) {
            return false;
        }
        return true;
    }

    private void updateClientAnimations() {
        if (this.isDeadOrDying()) {
            this.deathAnimationState.startIfStopped(this.tickCount);
            return;
        }
        this.idleBreatheAnimationState.startIfStopped(this.tickCount);
        if (this.isAirborne() || (!this.onGround() && !this.isOnShoulder())) {
            if (this.getDeltaMovement().y < -0.02D && this.getDeltaMovement().horizontalDistanceSqr() > 0.01D) {
                this.flyFlapAnimationState.stop();
                this.flyGlideAnimationState.startIfStopped(this.tickCount);
            } else {
                this.flyGlideAnimationState.stop();
                this.flyFlapAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            this.flyFlapAnimationState.stop();
            this.flyGlideAnimationState.stop();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_BLINK_THIRD_EYE) {
            this.idleHeadTiltAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_IDLE_CLEAN) {
            this.idlePreenAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_IDLE_CAW || id == EVENT_MYSTIC_ALERT) {
            this.idleCawAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_PECK) {
            this.peckAttackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_HURT_REACTION) {
            this.hurtAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FALL)) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker != null && this.isProtectedAlly(attacker) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        if (this.isOnShoulder()) {
            this.leaveShoulder();
        }
        if (this.isOrderedToSit()) {
            this.setOrderedToSit(false);
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive()) {
            level.broadcastEntityEvent(this, EVENT_HURT_REACTION);
        }
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        if (!this.scrollDroppedOnDeath) {
            ItemStack scroll = this.getScrollItem();
            if (!scroll.isEmpty()) {
                this.spawnAtLocation(level, scroll.copy());
                this.setScrollItem(ItemStack.EMPTY);
                this.scrollDroppedOnDeath = true;
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.FEY_RAVEN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.FEY_RAVEN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.FEY_RAVEN_DEATH.get();
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (effect.is(MobEffects.BLINDNESS) || effect.is(MobEffects.DARKNESS)) {
            return false;
        }
        return super.canBeAffected(effect);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.GLOW_BERRIES) || stack.is(Items.SWEET_BERRIES);
    }

    @Override
    public boolean canMate(Animal other) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("RavenState", this.getState().name());
        output.putString("CommandMode", this.getCommandMode().name());
        output.putBoolean("Airborne", this.isAirborne());
        output.putInt("AlertCooldown", this.alertCooldown);
        if (this.sentinelAnchor != null) {
            output.putDouble("SentinelX", this.sentinelAnchor.x);
            output.putDouble("SentinelY", this.sentinelAnchor.y);
            output.putDouble("SentinelZ", this.sentinelAnchor.z);
        }
        ItemStack scroll = this.getScrollItem();
        if (!scroll.isEmpty()) {
            output.store("ScrollItem", ItemStack.CODEC, scroll);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String cmdName = input.getStringOr("CommandMode", CommandMode.FOLLOW.name());
        try {
            this.entityData.set(DATA_COMMAND, CommandMode.valueOf(cmdName).ordinal());
        } catch (IllegalArgumentException ignored) {
            this.entityData.set(DATA_COMMAND, CommandMode.FOLLOW.ordinal());
        }
        String stateName = input.getStringOr("RavenState", State.WILD_IDLE.name());
        try {
            this.setState(State.valueOf(stateName));
        } catch (IllegalArgumentException ignored) {
            this.setState(State.WILD_IDLE);
        }
        this.setAirborne(input.getBooleanOr("Airborne", false));
        this.alertCooldown = input.getIntOr("AlertCooldown", 0);
        double sy = input.getDoubleOr("SentinelY", Double.NaN);
        if (!Double.isNaN(sy)) {
            this.sentinelAnchor = new Vec3(
                    input.getDoubleOr("SentinelX", this.getX()),
                    sy,
                    input.getDoubleOr("SentinelZ", this.getZ()));
        }
        this.setScrollItem(input.read("ScrollItem", ItemStack.CODEC).orElse(ItemStack.EMPTY));
    }

    public static boolean checkSpawnRules(
            EntityType<FeyRaven> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        if (pos.getY() < 64) {
            return false;
        }
        if (!level.getFluidState(pos).isEmpty()) {
            return false;
        }
        BlockState floor = level.getBlockState(pos.below());
        boolean validPerch = floor.is(BlockTags.LEAVES)
                || floor.is(BlockTags.LOGS)
                || floor.is(BlockTags.ANIMALS_SPAWNABLE_ON);
        if (!validPerch) {
            return false;
        }
        if (!level.noCollision(type.getSpawnAABB(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D))) {
            return false;
        }
        AABB area = new AABB(pos).inflate(16.0D);
        return level.getEntitiesOfClass(FeyRaven.class, area).size() < 3;
    }
}
