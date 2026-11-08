package com.codenamecoraline.entity.salamander;

import com.codenamecoraline.entity.basilisk.Basilisk;
import com.codenamecoraline.entity.lithophage.Lithophage;
import com.codenamecoraline.entity.raven.FeyRaven;
import com.codenamecoraline.registry.ModSounds;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Brimstone Salamander ({@code codenamecoraline:brimstone_salamander}): a predatory, magma-dwelling
 * amphibian of traditional fey alchemy lore. Native to Nether Basalt Deltas, Nether Wastes, and Crimson Forest shores.
 */
public final class BrimstoneSalamander extends Monster {
    public static final float WIDTH = 1.3F;
    public static final float HEIGHT = 0.85F;
    public static final float EYE_HEIGHT = 0.55F;

    public static final float BABY_WIDTH = WIDTH * 0.5F;
    public static final float BABY_HEIGHT = HEIGHT * 0.5F;
    public static final float BABY_EYE_HEIGHT = EYE_HEIGHT * 0.5F;

    public static final int BABY_START_AGE = -24000;
    public static final int READY_TO_MATURE_AGE = -1;
    public static final int ADULT_AGE = 0;

    public static final int MIN_SPAWN_Y = 24;
    public static final int MAX_SPAWN_Y = 48;

    // Core attributes & ecological distances
    public static final double MAX_HEALTH = 44.0D;
    public static final double ARMOR = 8.0D;
    public static final double ARMOR_TOUGHNESS = 2.0D;
    public static final double MOVEMENT_SPEED = 0.23D;
    public static final double LAVA_SWIM_SPEED = 0.38D;
    public static final double ATTACK_DAMAGE = 6.0D;
    public static final double KNOCKBACK_RESISTANCE = 0.65D;
    public static final double FOLLOW_RANGE = 24.0D;

    // Juvenile scaled attributes (Section 4.2)
    public static final double BABY_MAX_HEALTH = 16.0D;
    public static final double BABY_ARMOR = 2.0D;
    public static final double BABY_ARMOR_TOUGHNESS = 0.0D;
    public static final double BABY_MOVEMENT_SPEED = 0.26D;
    public static final double BABY_LAVA_SWIM_SPEED = 0.42D;
    public static final double BABY_ATTACK_DAMAGE = 2.0D;
    public static final double BABY_KNOCKBACK_RESISTANCE = 0.15D;
    public static final double BABY_FOLLOW_RANGE = 16.0D;
    public static final float BABY_WATER_SHOCK_DAMAGE = 4.0F;
    public static final int BABY_XP_REWARD = 0;
    public static final int ADULT_XP_REWARD = 10;
    public static final double STALKING_MIN_RANGE = 8.0D;
    public static final double SPECIES_SPACING_RANGE = 4.0D;

    // Attack 1: Mordida de Fricção (SNAP_BITE)
    public static final double SNAP_BITE_MAX_REACH = 2.2D;
    public static final double SNAP_BITE_ANTI_WHIFF_REACH = 2.6D;
    public static final int SNAP_BITE_PREP_TICKS = 8;
    public static final int SNAP_BITE_ANTI_WHIFF_TICK = 7;
    public static final int SNAP_BITE_EXEC_TICKS = 4;
    public static final int SNAP_BITE_IMPACT_TICK = 2;
    public static final int SNAP_BITE_RECOVERY_TICKS = 12;
    public static final int SNAP_BITE_SHIELD_STUN_BONUS_TICKS = 10;
    public static final float SNAP_BITE_DAMAGE = 7.0F;
    public static final int SNAP_BITE_FIRE_TICKS = 40;
    public static final int SNAP_BITE_COOLDOWN_TICKS = 35;
    public static final int SNAP_BITE_CANCEL_COOLDOWN_TICKS = 15;

    // Attack Juvenile: Mordiscao Juvenil (BABY_NIP)
    public static final double BABY_NIP_MAX_REACH = 1.4D;
    public static final double BABY_NIP_ANTI_WHIFF_REACH = 1.8D;
    public static final int BABY_NIP_PREP_TICKS = 4;
    public static final int BABY_NIP_ANTI_WHIFF_TICK = 3;
    public static final int BABY_NIP_EXEC_TICKS = 3;
    public static final int BABY_NIP_RECOVERY_TICKS = 8;
    public static final float BABY_NIP_DAMAGE = 2.0F;
    public static final int BABY_NIP_FIRE_TICKS = 15;
    public static final int BABY_NIP_COOLDOWN_TICKS = 25;
    public static final int BABY_NIP_CANCEL_COOLDOWN_TICKS = 10;

    // Attack 2: Cuspe Sulfuroso (SULFUR_SPIT)
    public static final double SULFUR_SPIT_MIN_REACH = 4.5D;
    public static final double SULFUR_SPIT_MAX_REACH = 16.0D;
    public static final double ANTI_PILLARING_MIN_Y = 2.0D;
    public static final double ANTI_PILLARING_MAX_Y = 5.0D;
    public static final int SULFUR_SPIT_PREP_TICKS = 16;
    public static final int SULFUR_SPIT_EXEC_TICKS = 6;
    public static final int SULFUR_SPIT_IMPACT_TICK = 2;
    public static final int SULFUR_SPIT_RECOVERY_TICKS = 18;
    public static final float SULFUR_SPIT_DAMAGE = BrimstoneSpit.DAMAGE;
    public static final int SULFUR_SPIT_COOLDOWN_TICKS = 90;

    // Attack 3: Chicotada de Cauda (TAIL_WHIP)
    public static final double TAIL_WHIP_MAX_REACH = 2.5D;
    public static final double ANTI_SURROUND_RADIUS = 2.0D;
    public static final int TAIL_WHIP_PREP_TICKS = 10;
    public static final int TAIL_WHIP_EXEC_TICKS = 5;
    public static final int TAIL_WHIP_IMPACT_TICK = 3;
    public static final int TAIL_WHIP_RECOVERY_TICKS = 14;
    public static final float TAIL_WHIP_DAMAGE = 6.0F;
    public static final double TAIL_WHIP_KNOCKBACK = 1.2D;
    public static final int TAIL_WHIP_COOLDOWN_TICKS = 60;

    // Reserved entity event range: 150..159
    public static final byte EVENT_THROAT_PUFF = (byte) 150;
    public static final byte EVENT_SULFUR_SPIT = (byte) 151;
    public static final byte EVENT_TAIL_WHIP = (byte) 152;
    public static final byte EVENT_SNAP_BITE = (byte) 153;
    public static final byte EVENT_HURT = (byte) 154;
    public static final byte EVENT_DEATH = (byte) 155;
    public static final byte EVENT_IDLE_TAIL_SWAY = (byte) 156;
    public static final byte EVENT_IDLE_SNIFF = (byte) 157;
    public static final byte EVENT_ATTACK_RECOVERY = (byte) 158;
    public static final byte EVENT_ATTACK_EXEC = (byte) 159;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(BrimstoneSalamander.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(BrimstoneSalamander.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_BABY =
            SynchedEntityData.defineId(BrimstoneSalamander.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleBreatheAnimationState = new AnimationState();
    public final AnimationState idleThroatPuffAnimationState = new AnimationState();
    public final AnimationState idleTailSwayAnimationState = new AnimationState();
    public final AnimationState idleSniffAnimationState = new AnimationState();
    public final AnimationState swimLavaAnimationState = new AnimationState();
    public final AnimationState snapBitePrepAnimationState = new AnimationState();
    public final AnimationState snapBiteExecAnimationState = new AnimationState();
    public final AnimationState snapBiteRecoveryAnimationState = new AnimationState();
    public final AnimationState sulfurSpitPrepAnimationState = new AnimationState();
    public final AnimationState sulfurSpitExecAnimationState = new AnimationState();
    public final AnimationState sulfurSpitRecoveryAnimationState = new AnimationState();
    public final AnimationState tailWhipPrepAnimationState = new AnimationState();
    public final AnimationState tailWhipExecAnimationState = new AnimationState();
    public final AnimationState tailWhipRecoveryAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int idleVariationCooldown = 140;
    private int actionTicks = 0;
    private int recoveryTicks = 0;
    private int snapBiteCooldown = 0;
    private int sulfurSpitCooldown = 0;
    private int tailWhipCooldown = 0;
    private boolean retaliateWithSpit = false;
    private State lastAttackState = State.IDLE;
    private State clientActiveAttack = State.IDLE;
    private @Nullable BlockPos cachedHeatTarget;
    private int growthAge = ADULT_AGE;
    private @Nullable UUID familyId = null;
    private @Nullable UUID leaderUUID = null;
    private int lavaHysteresisTicks = 0;

    public static class SalamanderGroupData implements SpawnGroupData {
        public final UUID familyId;
        public final BrimstoneSalamanderVariant variant;
        public final @Nullable UUID leaderUUID;
        public int spawnedCount = 0;

        public SalamanderGroupData(UUID familyId, BrimstoneSalamanderVariant variant, @Nullable UUID leaderUUID) {
            this.familyId = familyId;
            this.variant = variant;
            this.leaderUUID = leaderUUID;
        }
    }

    public enum State {
        IDLE,
        STALKING,
        PURSUING,
        SNAP_BITE,
        SULFUR_SPIT,
        TAIL_WHIP,
        RECOVERY,
        DYING
    }

    public BrimstoneSalamander(EntityType<? extends BrimstoneSalamander> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.FIRE, 0.0F);
        this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, 0.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, State.IDLE.ordinal());
        builder.define(DATA_VARIANT, BrimstoneSalamanderVariant.BRIMSTONE.getId());
        builder.define(DATA_IS_BABY, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SalamanderBabyFleeGoal(this));
        this.goalSelector.addGoal(2, new SalamanderCombatGoal(this));
        this.goalSelector.addGoal(3, new SalamanderFollowFamilyGoal(this));
        this.goalSelector.addGoal(4, new SeekHeatOutsideNetherGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                LivingEntity attacker = BrimstoneSalamander.this.getLastHurtByMob();
                if (attacker != null && !BrimstoneSalamander.this.canTargetEntity(attacker, true)) {
                    return false;
                }
                return super.canUse();
            }
        });
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this, Player.class, 10, true, false,
                (entity, level) -> this.canTargetEntity(entity, false)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                this, IronGolem.class, 10, true, false,
                (entity, level) -> this.canTargetEntity(entity, false)));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                this, Strider.class, 10, true, false,
                (entity, level) -> entity instanceof Strider strider && strider.isBaby() && this.canTargetEntity(entity, false)));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(
                this, MagmaCube.class, 10, true, false,
                (entity, level) -> entity instanceof MagmaCube cube && cube.getSize() <= 1 && this.canTargetEntity(entity, false)));
    }

    public boolean canTargetEntity(@Nullable LivingEntity entity, boolean directRetaliation) {
        if (entity == null || !entity.isAlive() || entity == this) {
            return false;
        }
        if (entity instanceof BrimstoneSalamander
                || entity instanceof Basilisk
                || entity instanceof Lithophage
                || entity instanceof FeyRaven) {
            return false;
        }
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        if (!directRetaliation && entity.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD)) {
            return false;
        }
        if (this.isBaby() && !directRetaliation) {
            return entity instanceof MagmaCube cube && cube.getSize() <= 1;
        }
        return true;
    }

    public State getState() {
        int idx = this.entityData.get(DATA_STATE);
        State[] values = State.values();
        return idx >= 0 && idx < values.length ? values[idx] : State.IDLE;
    }

    public void setState(State state) {
        this.entityData.set(DATA_STATE, state.ordinal());
    }

    public BrimstoneSalamanderVariant getVariant() {
        return BrimstoneSalamanderVariant.byId(this.entityData.get(DATA_VARIANT));
    }

    public void setVariant(BrimstoneSalamanderVariant variant) {
        this.entityData.set(DATA_VARIANT, variant.getId());
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        EntityDimensions base = super.getDefaultDimensions(pose);
        return this.isBaby() ? base.withEyeHeight(BABY_EYE_HEIGHT) : base;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_IS_BABY.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    public boolean isBaby() {
        if (this.level().isClientSide()) {
            return this.entityData.get(DATA_IS_BABY);
        }
        return this.growthAge < ADULT_AGE;
    }

    public void setBaby(boolean baby) {
        this.setGrowthAge(baby ? BABY_START_AGE : ADULT_AGE);
    }

    public int getGrowthAge() {
        return this.growthAge;
    }

    public void setGrowthAge(int age) {
        int clamped = Math.clamp(age, BABY_START_AGE, ADULT_AGE);
        boolean wasBaby = this.isBaby();
        this.growthAge = clamped;
        boolean nowBaby = clamped < ADULT_AGE;
        if (wasBaby != nowBaby || this.entityData.get(DATA_IS_BABY) != nowBaby) {
            this.entityData.set(DATA_IS_BABY, nowBaby);
            if (!this.level().isClientSide()) {
                if (nowBaby) {
                    this.applyBabyAttributes();
                } else {
                    this.applyAdultAttributes();
                }
            }
            this.refreshDimensions();
        }
    }

    public @Nullable UUID getFamilyId() {
        return this.familyId;
    }

    public void setFamilyId(@Nullable UUID familyId) {
        this.familyId = familyId;
    }

    public @Nullable UUID getLeaderUUID() {
        return this.leaderUUID;
    }

    public void setLeaderUUID(@Nullable UUID leaderUUID) {
        this.leaderUUID = leaderUUID;
    }

    public void applyBabyAttributes() {
        float currentHealth = this.getHealth();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(BABY_MAX_HEALTH);
        this.getAttribute(Attributes.ARMOR).setBaseValue(BABY_ARMOR);
        this.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(BABY_ARMOR_TOUGHNESS);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(BABY_MOVEMENT_SPEED);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(BABY_ATTACK_DAMAGE);
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(BABY_KNOCKBACK_RESISTANCE);
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(BABY_FOLLOW_RANGE);
        this.setHealth(Math.min(currentHealth, (float) BABY_MAX_HEALTH));
        this.xpReward = BABY_XP_REWARD;
    }

    public void applyAdultAttributes() {
        float currentHealth = this.getHealth();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(MAX_HEALTH);
        this.getAttribute(Attributes.ARMOR).setBaseValue(ARMOR);
        this.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(ARMOR_TOUGHNESS);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(ATTACK_DAMAGE);
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(KNOCKBACK_RESISTANCE);
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE);
        this.setHealth(Math.min(currentHealth, (float) MAX_HEALTH));
        this.xpReward = ADULT_XP_REWARD;
    }

    public boolean canFitAdultHitbox() {
        AABB adultAABB = EntityDimensions.scalable(WIDTH, HEIGHT).makeBoundingBox(this.position());
        return this.level().noCollision(this, adultAABB);
    }

    public boolean tryMature(ServerLevel level) {
        if (!this.isBaby() || this.growthAge > READY_TO_MATURE_AGE) {
            return false;
        }
        if (!this.canFitAdultHitbox()) {
            return false;
        }
        this.setGrowthAge(ADULT_AGE);
        for (int i = 0; i < 16; i++) {
            double ox = (this.random.nextDouble() - 0.5D) * WIDTH;
            double oy = this.random.nextDouble() * HEIGHT;
            double oz = (this.random.nextDouble() - 0.5D) * WIDTH;
            level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX() + ox, this.getY() + oy, this.getZ() + oz, 1, 0.0D, 0.02D, 0.0D, 0.02D);
            level.sendParticles(ParticleTypes.FLAME, this.getX() + ox, this.getY() + oy, this.getZ() + oz, 1, 0.0D, 0.03D, 0.0D, 0.03D);
        }
        this.playSound(ModSounds.BRIMSTONE_SALAMANDER_AMBIENT.get(), 1.0F, 0.8F);
        return true;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (this.isBaby()) {
            boolean isMagmaCream = held.is(Items.MAGMA_CREAM);
            boolean isBlazePowder = held.is(Items.BLAZE_POWDER);
            if (isMagmaCream || isBlazePowder) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    float healAmount = isMagmaCream ? 3.0F : 5.0F;
                    this.heal(healAmount);

                    int ageBonus = isMagmaCream ? 2400 : 3600;
                    int newAge = this.growthAge + ageBonus;
                    if (newAge >= READY_TO_MATURE_AGE) {
                        this.setGrowthAge(READY_TO_MATURE_AGE);
                        this.tryMature(serverLevel);
                    } else {
                        this.setGrowthAge(newAge);
                    }

                    for (int i = 0; i < 8; i++) {
                        double ox = (this.random.nextDouble() - 0.5D) * this.getBbWidth();
                        double oy = this.random.nextDouble() * this.getBbHeight();
                        double oz = (this.random.nextDouble() - 0.5D) * this.getBbWidth();
                        serverLevel.sendParticles(ParticleTypes.FLAME, this.getX() + ox, this.getY() + oy, this.getZ() + oz, 1, 0.0D, 0.02D, 0.0D, 0.02D);
                        serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX() + ox, this.getY() + oy, this.getZ() + oz, 1, 0.0D, 0.01D, 0.0D, 0.01D);
                    }
                }
                this.playSound(SoundEvents.GENERIC_EAT.value(), 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    public int getActionTicks() {
        return this.actionTicks;
    }

    public int getRecoveryTicks() {
        return this.recoveryTicks;
    }

    public int getSnapBiteCooldown() {
        return this.snapBiteCooldown;
    }

    public int getSulfurSpitCooldown() {
        return this.sulfurSpitCooldown;
    }

    public int getTailWhipCooldown() {
        return this.tailWhipCooldown;
    }

    public boolean isSwimmingInLava() {
        return this.isInLava()
                || this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)
                || this.lavaHysteresisTicks > 0;
    }

    public boolean isInAttackOrRecovery() {
        State s = this.getState();
        return s == State.SNAP_BITE || s == State.SULFUR_SPIT || s == State.TAIL_WHIP || s == State.RECOVERY;
    }

    @Override
    public void tick() {
        super.tick();
        boolean inLava = this.isInLava() || this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA);
        if (inLava) {
            this.lavaHysteresisTicks = 4;
        } else if (this.lavaHysteresisTicks > 0) {
            this.lavaHysteresisTicks--;
        }

        if (this.level().isClientSide()) {
            this.idleBreatheAnimationState.startIfStopped(this.tickCount);
            if (this.isSwimmingInLava()) {
                this.swimLavaAnimationState.startIfStopped(this.tickCount);
            } else {
                this.swimLavaAnimationState.stop();
            }
            return;
        }

        if (!this.isAlive() || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean exposedToWater = this.isInWater() || this.level().isRainingAt(this.blockPosition());
        if (this.isBaby() && exposedToWater && this.tickCount % 20 == 0) {
            this.hurtServer(serverLevel, this.damageSources().drown(), BABY_WATER_SHOCK_DAMAGE);
        }

        if (this.growthAge < ADULT_AGE) {
            if (this.growthAge < READY_TO_MATURE_AGE) {
                this.growthAge++;
            } else if (this.growthAge == READY_TO_MATURE_AGE) {
                this.tryMature(serverLevel);
            }
        }

        if (this.snapBiteCooldown > 0) {
            this.snapBiteCooldown--;
        }
        if (this.sulfurSpitCooldown > 0) {
            this.sulfurSpitCooldown--;
        }
        if (this.tailWhipCooldown > 0) {
            this.tailWhipCooldown--;
        }

        if (this.isAlive()) {
            this.handleLavaBuoyancy();
        }

        this.tickCombatStateMachine(serverLevel);

        if (this.getState() == State.IDLE && this.getTarget() == null) {
            if (this.tickCount % 10 == 0) {
                this.applySpeciesSpacing();
            }
            if (--this.idleVariationCooldown <= 0) {
                this.idleVariationCooldown = 120 + this.random.nextInt(121);
                int roll = this.random.nextInt(3);
                byte event = switch (roll) {
                    case 0 -> EVENT_THROAT_PUFF;
                    case 1 -> EVENT_IDLE_TAIL_SWAY;
                    default -> EVENT_IDLE_SNIFF;
                };
                this.level().broadcastEntityEvent(this, event);
            }
        }
    }

    private void applySpeciesSpacing() {
        AABB box = this.getBoundingBox().inflate(SPECIES_SPACING_RANGE, 1.5D, SPECIES_SPACING_RANGE);
        List<BrimstoneSalamander> neighbors = this.level().getEntitiesOfClass(
                BrimstoneSalamander.class,
                box,
                other -> {
                    if (other == this || !other.isAlive() || other.getState() != State.IDLE) {
                        return false;
                    }
                    if (this.familyId != null && this.familyId.equals(other.familyId) && (this.isBaby() || other.isBaby())) {
                        return false;
                    }
                    return true;
                });
        if (neighbors.isEmpty()) {
            return;
        }
        BrimstoneSalamander closest = neighbors.get(0);
        double dx = this.getX() - closest.getX();
        double dz = this.getZ() - closest.getZ();
        double dist = Math.hypot(dx, dz);
        if (dist < SPECIES_SPACING_RANGE) {
            if (dist < 1.0E-3D) {
                dx = 1.0D;
                dist = 1.0D;
            }
            double push = 0.12D;
            this.setDeltaMovement(this.getDeltaMovement().add((dx / dist) * push, 0.0D, (dz / dist) * push));
            this.hurtMarked = true;
        }
    }

    private void handleLavaBuoyancy() {
        if (this.isInLava()) {
            if (!this.level().getFluidState(this.blockPosition().above()).is(FluidTags.LAVA)) {
                if (this.getDeltaMovement().y < 0.0D) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.5D, 1.0D));
                }
            } else {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.04D, 0.0D));
            }
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (!this.level().isClientSide() && this.isSwimmingInLava()) {
            float baseSpeed = this.getSpeed() > 0.0F ? this.getSpeed() : (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
            double targetSwimSpeed = this.isBaby() ? BABY_LAVA_SWIM_SPEED : LAVA_SWIM_SPEED;
            double targetMoveSpeed = this.isBaby() ? BABY_MOVEMENT_SPEED : MOVEMENT_SPEED;
            float swimImpulse = (float) (baseSpeed * (targetSwimSpeed / targetMoveSpeed) * 0.474F);
            this.moveRelative(swimImpulse, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.68D, 0.80D, 0.68D));
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new SalamanderPathNavigation(this, level);
    }

    public double horizontalSurfaceDistance(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double centerDist = Math.hypot(dx, dz);
        return Math.max(0.0D, centerDist - (this.getBbWidth() * 0.35D));
    }

    public boolean isTargetFlankingOrBehind(LivingEntity target) {
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot()).normalize();
        Vec3 toTarget = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
        if (toTarget.lengthSqr() < 1.0E-4D) {
            return false;
        }
        double dot = forward.dot(toTarget.normalize());
        return dot < 0.25D; // > ~75 degrees off forward axis
    }

    public boolean isTargetPillaring(LivingEntity target) {
        double dy = target.getY() - this.getY();
        return dy >= ANTI_PILLARING_MIN_Y && dy <= ANTI_PILLARING_MAX_Y;
    }

    public int countNearbyEnemies(double radius) {
        AABB box = this.getBoundingBox().inflate(radius, 1.5D, radius);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                entity -> this.canTargetEntity(entity, true));
        return nearby.size();
    }

    public void startSnapBite() {
        this.setState(State.SNAP_BITE);
        this.lastAttackState = State.SNAP_BITE;
        this.actionTicks = 0;
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, EVENT_SNAP_BITE);
    }

    public void startBabyNip() {
        this.startSnapBite();
    }

    public void startSulfurSpit() {
        if (this.isBaby()) {
            return;
        }
        this.setState(State.SULFUR_SPIT);
        this.lastAttackState = State.SULFUR_SPIT;
        this.actionTicks = 0;
        this.retaliateWithSpit = false;
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, EVENT_SULFUR_SPIT);
    }

    public void startTailWhip() {
        if (this.isBaby()) {
            return;
        }
        this.setState(State.TAIL_WHIP);
        this.lastAttackState = State.TAIL_WHIP;
        this.actionTicks = 0;
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, EVENT_TAIL_WHIP);
    }

    private void enterRecovery(int ticks) {
        this.setState(State.RECOVERY);
        this.recoveryTicks = ticks;
        this.actionTicks = 0;
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, EVENT_ATTACK_RECOVERY);
    }

    private void tickCombatStateMachine(ServerLevel level) {
        State current = this.getState();
        LivingEntity target = this.getTarget();
        if (target != null && !target.isAlive()) {
            this.setTarget(null);
            target = null;
        }

        switch (current) {
            case SNAP_BITE -> this.tickSnapBite(level, target);
            case SULFUR_SPIT -> this.tickSulfurSpit(level, target);
            case TAIL_WHIP -> this.tickTailWhip(level, target);
            case RECOVERY -> {
                if (--this.recoveryTicks <= 0) {
                    this.setState(target != null ? State.PURSUING : State.IDLE);
                }
            }
            case STALKING, PURSUING -> {
                if (target == null) {
                    this.setState(State.IDLE);
                }
            }
            default -> {}
        }
    }

    private void tickSnapBite(ServerLevel level, @Nullable LivingEntity target) {
        if (this.isBaby()) {
            this.tickBabyNip(level, target);
            return;
        }
        this.actionTicks++;
        this.getNavigation().stop();
        if (target != null && this.actionTicks <= SNAP_BITE_PREP_TICKS) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        // Anti-whiff check at prep tick 7: if target backed past 2.6 blocks, cancel cleanly with 15t cooldown
        if (this.actionTicks == SNAP_BITE_ANTI_WHIFF_TICK) {
            if (target == null || this.horizontalSurfaceDistance(target) > SNAP_BITE_ANTI_WHIFF_REACH) {
                this.snapBiteCooldown = SNAP_BITE_CANCEL_COOLDOWN_TICKS;
                this.actionTicks = 0;
                this.setState(target != null ? State.PURSUING : State.IDLE);
                this.level().broadcastEntityEvent(this, EVENT_ATTACK_RECOVERY);
                return;
            }
        }

        if (this.actionTicks == SNAP_BITE_PREP_TICKS) {
            this.level().broadcastEntityEvent(this, EVENT_ATTACK_EXEC);
        }

        if (this.actionTicks == SNAP_BITE_PREP_TICKS + SNAP_BITE_IMPACT_TICK) {
            this.playSound(ModSounds.BRIMSTONE_SALAMANDER_BITE.get(), 1.0F, 0.85F);
            int nextRecovery = SNAP_BITE_RECOVERY_TICKS;
            if (target != null && this.horizontalSurfaceDistance(target) <= SNAP_BITE_ANTI_WHIFF_REACH) {
                boolean shieldBlocked = target instanceof Player player && player.isBlocking();
                if (shieldBlocked) {
                    target.hurtServer(level, this.damageSources().mobAttack(this), SNAP_BITE_DAMAGE);
                    nextRecovery += SNAP_BITE_SHIELD_STUN_BONUS_TICKS;
                } else {
                    boolean hit = target.hurtServer(level, this.damageSources().mobAttack(this), SNAP_BITE_DAMAGE);
                    if (hit) {
                        target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), SNAP_BITE_FIRE_TICKS));
                    }
                }
            }
            this.snapBiteCooldown = SNAP_BITE_COOLDOWN_TICKS;
            this.recoveryTicks = nextRecovery;
        }

        if (this.actionTicks >= SNAP_BITE_PREP_TICKS + SNAP_BITE_EXEC_TICKS) {
            int rec = this.recoveryTicks > 0 ? this.recoveryTicks : SNAP_BITE_RECOVERY_TICKS;
            this.enterRecovery(rec);
        }
    }

    private void tickBabyNip(ServerLevel level, @Nullable LivingEntity target) {
        this.actionTicks++;
        this.getNavigation().stop();
        if (target != null && this.actionTicks <= BABY_NIP_PREP_TICKS) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        // Anti-whiff check at tick 3
        if (this.actionTicks == BABY_NIP_ANTI_WHIFF_TICK) {
            if (target == null || this.horizontalSurfaceDistance(target) > BABY_NIP_ANTI_WHIFF_REACH) {
                this.snapBiteCooldown = BABY_NIP_CANCEL_COOLDOWN_TICKS;
                this.actionTicks = 0;
                this.setState(target != null ? State.PURSUING : State.IDLE);
                this.level().broadcastEntityEvent(this, EVENT_ATTACK_RECOVERY);
                return;
            }
        }

        // Impact at tick 4
        if (this.actionTicks == BABY_NIP_PREP_TICKS) {
            this.level().broadcastEntityEvent(this, EVENT_ATTACK_EXEC);
            this.playSound(ModSounds.BRIMSTONE_SALAMANDER_BITE.get(), 0.8F, 1.3F);
            if (target != null && this.horizontalSurfaceDistance(target) <= BABY_NIP_ANTI_WHIFF_REACH) {
                boolean hit = target.hurtServer(level, this.damageSources().mobAttack(this), BABY_NIP_DAMAGE);
                if (hit) {
                    target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), BABY_NIP_FIRE_TICKS));
                }
            }
            this.snapBiteCooldown = BABY_NIP_COOLDOWN_TICKS;
            this.recoveryTicks = BABY_NIP_RECOVERY_TICKS;
        }

        if (this.actionTicks >= BABY_NIP_PREP_TICKS + BABY_NIP_EXEC_TICKS) {
            this.enterRecovery(BABY_NIP_RECOVERY_TICKS);
        }
    }

    private void tickSulfurSpit(ServerLevel level, @Nullable LivingEntity target) {
        this.actionTicks++;
        this.getNavigation().stop();
        if (target != null) {
            this.getLookControl().setLookAt(target, 35.0F, 35.0F);
        }

        if (this.actionTicks == SULFUR_SPIT_PREP_TICKS) {
            this.level().broadcastEntityEvent(this, EVENT_ATTACK_EXEC);
        }

        if (this.actionTicks == SULFUR_SPIT_PREP_TICKS + SULFUR_SPIT_IMPACT_TICK) {
            this.fireBrimstoneSpit(level, target);
            this.sulfurSpitCooldown = SULFUR_SPIT_COOLDOWN_TICKS;
        }

        if (this.actionTicks >= SULFUR_SPIT_PREP_TICKS + SULFUR_SPIT_EXEC_TICKS) {
            this.enterRecovery(SULFUR_SPIT_RECOVERY_TICKS);
        }
    }

    private void fireBrimstoneSpit(ServerLevel level, @Nullable LivingEntity target) {
        BrimstoneSpit spit = new BrimstoneSpit(level, this);
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot());
        spit.setPos(this.getX() + forward.x * 0.7D, this.getEyeY() - 0.1D, this.getZ() + forward.z * 0.7D);

        if (target != null) {
            double dx = target.getX() - spit.getX();
            double dz = target.getZ() - spit.getZ();
            double horiz = Math.hypot(dx, dz);
            // When target is pillaring, aim at their feet/pillar top with a high ballistic arc
            double aimY = this.isTargetPillaring(target) ? target.getY() + 0.2D : target.getY(0.35D);
            double dy = aimY - spit.getY() + horiz * 0.22D;
            spit.shoot(dx, dy, dz, 0.95F, 1.0F);
        } else {
            spit.shoot(forward.x, 0.2D, forward.z, 0.95F, 1.0F);
        }
        level.addFreshEntity(spit);
        this.playSound(ModSounds.BRIMSTONE_SALAMANDER_SPIT.get(), 1.0F, 0.75F + this.random.nextFloat() * 0.1F);
    }

    private void tickTailWhip(ServerLevel level, @Nullable LivingEntity target) {
        this.actionTicks++;
        this.getNavigation().stop();

        if (this.actionTicks == TAIL_WHIP_PREP_TICKS) {
            this.level().broadcastEntityEvent(this, EVENT_ATTACK_EXEC);
        }

        if (this.actionTicks == TAIL_WHIP_PREP_TICKS + TAIL_WHIP_IMPACT_TICK) {
            this.playSound(ModSounds.BRIMSTONE_SALAMANDER_TAIL_WHIP.get(), 1.0F, 0.6F);
            AABB sweepBox = this.getBoundingBox().inflate(TAIL_WHIP_MAX_REACH, 1.0D, TAIL_WHIP_MAX_REACH);
            List<LivingEntity> victims = level.getEntitiesOfClass(
                    LivingEntity.class,
                    sweepBox,
                    entity -> this.canTargetEntity(entity, true) && this.horizontalSurfaceDistance(entity) <= TAIL_WHIP_MAX_REACH);
            for (int i = 0, n = victims.size(); i < n; i++) {
                LivingEntity victim = victims.get(i);
                victim.hurtServer(level, this.damageSources().mobAttack(this), TAIL_WHIP_DAMAGE);
                double dx = victim.getX() - this.getX();
                double dz = victim.getZ() - this.getZ();
                double len = Math.hypot(dx, dz);
                if (len < 1.0E-3D) {
                    dx = 1.0D;
                    len = 1.0D;
                }
                victim.push((dx / len) * TAIL_WHIP_KNOCKBACK, 0.25D, (dz / len) * TAIL_WHIP_KNOCKBACK);
                victim.hurtMarked = true;
            }
            this.tailWhipCooldown = TAIL_WHIP_COOLDOWN_TICKS;
        }

        if (this.actionTicks >= TAIL_WHIP_PREP_TICKS + TAIL_WHIP_EXEC_TICKS) {
            this.enterRecovery(TAIL_WHIP_RECOVERY_TICKS);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_THROAT_PUFF -> {
                this.idleTailSwayAnimationState.stop();
                this.idleSniffAnimationState.stop();
                this.idleThroatPuffAnimationState.start(this.tickCount);
                for (int i = 0; i < 4; i++) {
                    this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                            this.getX(), this.getEyeY(), this.getZ(),
                            (this.random.nextDouble() - 0.5D) * 0.05D, 0.03D, (this.random.nextDouble() - 0.5D) * 0.05D);
                }
            }
            case EVENT_IDLE_TAIL_SWAY -> {
                this.idleThroatPuffAnimationState.stop();
                this.idleSniffAnimationState.stop();
                this.idleTailSwayAnimationState.start(this.tickCount);
            }
            case EVENT_IDLE_SNIFF -> {
                this.idleThroatPuffAnimationState.stop();
                this.idleTailSwayAnimationState.stop();
                this.idleSniffAnimationState.start(this.tickCount);
            }
            case EVENT_SNAP_BITE -> {
                this.stopAllAttackAnimations();
                this.clientActiveAttack = State.SNAP_BITE;
                this.snapBitePrepAnimationState.start(this.tickCount);
            }
            case EVENT_SULFUR_SPIT -> {
                this.stopAllAttackAnimations();
                this.clientActiveAttack = State.SULFUR_SPIT;
                this.sulfurSpitPrepAnimationState.start(this.tickCount);
            }
            case EVENT_TAIL_WHIP -> {
                this.stopAllAttackAnimations();
                this.clientActiveAttack = State.TAIL_WHIP;
                this.tailWhipPrepAnimationState.start(this.tickCount);
            }
            case EVENT_ATTACK_EXEC -> {
                switch (this.clientActiveAttack) {
                    case SNAP_BITE -> {
                        this.snapBitePrepAnimationState.stop();
                        this.snapBiteExecAnimationState.start(this.tickCount);
                    }
                    case SULFUR_SPIT -> {
                        this.sulfurSpitPrepAnimationState.stop();
                        this.sulfurSpitExecAnimationState.start(this.tickCount);
                    }
                    case TAIL_WHIP -> {
                        this.tailWhipPrepAnimationState.stop();
                        this.tailWhipExecAnimationState.start(this.tickCount);
                    }
                    default -> {}
                }
            }
            case EVENT_ATTACK_RECOVERY -> {
                switch (this.clientActiveAttack) {
                    case SNAP_BITE -> {
                        this.snapBitePrepAnimationState.stop();
                        this.snapBiteExecAnimationState.stop();
                        this.snapBiteRecoveryAnimationState.start(this.tickCount);
                    }
                    case SULFUR_SPIT -> {
                        this.sulfurSpitPrepAnimationState.stop();
                        this.sulfurSpitExecAnimationState.stop();
                        this.sulfurSpitRecoveryAnimationState.start(this.tickCount);
                    }
                    case TAIL_WHIP -> {
                        this.tailWhipPrepAnimationState.stop();
                        this.tailWhipExecAnimationState.stop();
                        this.tailWhipRecoveryAnimationState.start(this.tickCount);
                    }
                    default -> this.stopAllAttackAnimations();
                }
            }
            case EVENT_HURT -> this.hurtAnimationState.start(this.tickCount);
            case EVENT_DEATH -> {
                this.stopAllAttackAnimations();
                this.deathAnimationState.start(this.tickCount);
            }
            default -> super.handleEntityEvent(id);
        }
    }

    public void stopAllAttackAnimations() {
        this.snapBitePrepAnimationState.stop();
        this.snapBiteExecAnimationState.stop();
        this.snapBiteRecoveryAnimationState.stop();
        this.sulfurSpitPrepAnimationState.stop();
        this.sulfurSpitExecAnimationState.stop();
        this.sulfurSpitRecoveryAnimationState.stop();
        this.tailWhipPrepAnimationState.stop();
        this.tailWhipExecAnimationState.stop();
        this.tailWhipRecoveryAnimationState.stop();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.BRIMSTONE_SALAMANDER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.BRIMSTONE_SALAMANDER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BRIMSTONE_SALAMANDER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.BRIMSTONE_SALAMANDER_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return super.causeFallDamage(fallDistance, multiplier * 0.4F, source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.FREEZE)) {
            amount *= 2.0F;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive()) {
            level.broadcastEntityEvent(this, EVENT_HURT);
            if (!this.isBaby() && source.is(DamageTypeTags.IS_PROJECTILE)) {
                this.retaliateWithSpit = true;
                this.sulfurSpitCooldown = 0;
            }
            if (this.isBaby()) {
                this.alertParentalGuardians(source);
            }
            if (source.getEntity() instanceof LivingEntity attacker && this.canTargetEntity(attacker, true)) {
                LivingEntity currentTarget = this.getTarget();
                if (currentTarget == null || !currentTarget.isAlive()) {
                    this.setTarget(attacker);
                } else if (currentTarget != attacker) {
                    // Threat hysteresis: switch target only on heavy blow (>= 6.0), much closer threat (> 3.5 blocks closer), or ranged hit while out of melee
                    double attackerDist = this.distanceTo(attacker);
                    double currentDist = this.distanceTo(currentTarget);
                    boolean heavyHit = amount >= 6.0F;
                    boolean muchCloser = attackerDist + 3.5D < currentDist;
                    boolean rangedRetaliation = source.is(DamageTypeTags.IS_PROJECTILE) && currentDist > SNAP_BITE_MAX_REACH;
                    if (heavyHit || muchCloser || rangedRetaliation) {
                        this.setTarget(attacker);
                    }
                }
            }
        }
        return hurt;
    }

    private void alertParentalGuardians(DamageSource source) {
        net.minecraft.world.entity.Entity attackerEntity = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
        if (!(attackerEntity instanceof LivingEntity attacker) || !attacker.isAlive() || attacker == this) {
            return;
        }
        if (attacker instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }
        AABB defenseBox = this.getBoundingBox().inflate(16.0D, 8.0D, 16.0D);
        List<BrimstoneSalamander> adults = this.level().getEntitiesOfClass(
                BrimstoneSalamander.class,
                defenseBox,
                other -> other != this && !other.isBaby() && other.isAlive()
        );
        for (BrimstoneSalamander adult : adults) {
            boolean familyMatch = this.familyId == null
                    || this.familyId.equals(adult.getFamilyId())
                    || (this.leaderUUID != null && this.leaderUUID.equals(adult.getUUID()));
            if (familyMatch && adult.canTargetEntity(attacker, true)) {
                adult.setTarget(attacker);
                if (adult.getState() == State.IDLE) {
                    adult.setState(State.PURSUING);
                }
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide()) {
            this.setState(State.DYING);
            this.level().broadcastEntityEvent(this, EVENT_DEATH);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("SalamanderState", this.getState().name());
        output.putString("Variant", this.getVariant().getSerializedName());
        output.putInt("GrowthAge", this.growthAge);
        output.putBoolean("IsBaby", this.isBaby());
        output.putInt("SnapBiteCooldown", this.snapBiteCooldown);
        output.putInt("SulfurSpitCooldown", this.sulfurSpitCooldown);
        output.putInt("TailWhipCooldown", this.tailWhipCooldown);
        if (this.familyId != null) {
            output.putString("FamilyId", this.familyId.toString());
        }
        if (this.leaderUUID != null) {
            output.putString("LeaderUUID", this.leaderUUID.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String stateName = input.getStringOr("SalamanderState", State.IDLE.name());
        try {
            this.setState(State.valueOf(stateName));
        } catch (IllegalArgumentException ignored) {
            this.setState(State.IDLE);
        }
        String variantName = input.getStringOr("Variant", BrimstoneSalamanderVariant.BRIMSTONE.getSerializedName());
        this.setVariant(BrimstoneSalamanderVariant.byName(variantName));
        int savedAge = input.getIntOr("GrowthAge", Integer.MIN_VALUE);
        if (savedAge == Integer.MIN_VALUE) {
            int legacyAge = input.getIntOr("Age", Integer.MIN_VALUE);
            if (legacyAge != Integer.MIN_VALUE) {
                savedAge = legacyAge;
            } else {
                boolean legacyBaby = input.getBooleanOr("IsBaby", false);
                savedAge = legacyBaby ? BABY_START_AGE : ADULT_AGE;
            }
        }
        this.setGrowthAge(savedAge);
        this.snapBiteCooldown = input.getIntOr("SnapBiteCooldown", 0);
        this.sulfurSpitCooldown = input.getIntOr("SulfurSpitCooldown", 0);
        this.tailWhipCooldown = input.getIntOr("TailWhipCooldown", 0);

        String famStr = input.getStringOr("FamilyId", "");
        if (!famStr.isEmpty()) {
            try {
                this.familyId = UUID.fromString(famStr);
            } catch (IllegalArgumentException ignored) {
                this.familyId = null;
            }
        } else {
            this.familyId = null;
        }

        String leaderStr = input.getStringOr("LeaderUUID", "");
        if (!leaderStr.isEmpty()) {
            try {
                this.leaderUUID = UUID.fromString(leaderStr);
            } catch (IllegalArgumentException ignored) {
                this.leaderUUID = null;
            }
        } else {
            this.leaderUUID = null;
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        SalamanderGroupData resultData;
        if (groupData instanceof SalamanderGroupData salamanderGroupData) {
            resultData = salamanderGroupData;
            this.setVariant(resultData.variant);
            this.setFamilyId(resultData.familyId);
            if (resultData.leaderUUID != null) {
                this.setLeaderUUID(resultData.leaderUUID);
            }
            if (resultData.spawnedCount > 0 && this.random.nextFloat() < 0.40F) {
                this.setBaby(true);
            }
            resultData.spawnedCount++;
        } else {
            Holder<Biome> biome = level.getBiome(this.blockPosition());
            if (biome.is(Biomes.SOUL_SAND_VALLEY)) {
                this.setVariant(this.random.nextFloat() < 0.70F ? BrimstoneSalamanderVariant.SOULFLAME : BrimstoneSalamanderVariant.BRIMSTONE);
            } else if (biome.is(Biomes.CRIMSON_FOREST)) {
                this.setVariant(this.random.nextFloat() < 0.60F ? BrimstoneSalamanderVariant.CRIMSON : BrimstoneSalamanderVariant.BRIMSTONE);
            } else {
                float roll = this.random.nextFloat();
                if (roll < 0.15F) {
                    this.setVariant(BrimstoneSalamanderVariant.SOULFLAME);
                } else if (roll < 0.30F) {
                    this.setVariant(BrimstoneSalamanderVariant.CRIMSON);
                } else {
                    this.setVariant(BrimstoneSalamanderVariant.BRIMSTONE);
                }
            }
            this.familyId = UUID.randomUUID();
            resultData = new SalamanderGroupData(this.familyId, this.getVariant(), this.getUUID());
            resultData.spawnedCount = 1;
        }
        return resultData;
    }

    public static boolean checkSpawnRules(
            EntityType<BrimstoneSalamander> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        int y = pos.getY();
        if (y < MIN_SPAWN_Y || y > MAX_SPAWN_Y) {
            return false;
        }
        BlockState floor = level.getBlockState(pos.below());
        boolean validFloor = floor.is(Blocks.BASALT)
                || floor.is(Blocks.POLISHED_BASALT)
                || floor.is(Blocks.BLACKSTONE)
                || floor.is(Blocks.NETHERRACK)
                || floor.is(Blocks.MAGMA_BLOCK)
                || floor.is(Blocks.LAVA);
        if (!validFloor) {
            return false;
        }
        if (!level.noCollision(type.getSpawnAABB(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D))) {
            return false;
        }
        AABB area = new AABB(pos).inflate(16.0D);
        return level.getEntitiesOfClass(BrimstoneSalamander.class, area).size() < 3;
    }

    /** Specialized path navigation that treats lava and heated mineral blocks as valid travel paths. */
    private static final class SalamanderPathNavigation extends GroundPathNavigation {
        SalamanderPathNavigation(BrimstoneSalamander mob, Level level) {
            super(mob, level);
            this.setCanFloat(true);
        }

        @Override
        protected boolean hasValidPathType(PathType pathType) {
            return pathType == PathType.LAVA || pathType == PathType.FIRE || pathType == PathType.FIRE_IN_NEIGHBOR || super.hasValidPathType(pathType);
        }

        @Override
        public boolean isStableDestination(BlockPos pos) {
            return this.level.getBlockState(pos).is(Blocks.LAVA)
                    || this.level.getBlockState(pos).is(Blocks.MAGMA_BLOCK)
                    || super.isStableDestination(pos);
        }
    }

    /** Primary combat goal orchestrating STALKING, PURSUING, SNAP_BITE, SULFUR_SPIT, and TAIL_WHIP. */
    private static final class SalamanderCombatGoal extends Goal {
        private final BrimstoneSalamander mob;
        private int pathRecalcTicks = 0;

        SalamanderCombatGoal(BrimstoneSalamander mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            return target != null && target.isAlive() && this.mob.canTargetEntity(target, true);
        }

        @Override
        public boolean canContinueToUse() {
            if (this.mob.isInAttackOrRecovery()) {
                return true;
            }
            LivingEntity target = this.mob.getTarget();
            return target != null && target.isAlive() && this.mob.distanceToSqr(target) <= FOLLOW_RANGE * FOLLOW_RANGE;
        }

        @Override
        public void start() {
            this.pathRecalcTicks = 0;
        }

        @Override
        public void stop() {
            this.mob.getNavigation().stop();
            if (!this.mob.isInAttackOrRecovery() && this.mob.isAlive()) {
                this.mob.setState(State.IDLE);
            }
        }

        @Override
        public void tick() {
            if (this.mob.isInAttackOrRecovery()) {
                return;
            }
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive()) {
                return;
            }

            double surfDist = this.mob.horizontalSurfaceDistance(target);
            boolean pillaring = this.mob.isTargetPillaring(target);

            // 1. Anti-cerco / Flanking Tail Whip priority (adults only)
            if (!this.mob.isBaby() && this.mob.tailWhipCooldown <= 0 && surfDist <= TAIL_WHIP_MAX_REACH && !pillaring) {
                if (this.mob.countNearbyEnemies(ANTI_SURROUND_RADIUS) >= 2 || this.mob.isTargetFlankingOrBehind(target)) {
                    this.mob.startTailWhip();
                    return;
                }
            }

            // 2. Anti-pillaring, ranged retaliation, or mid-range Sulfur Spit (adults only)
            if (!this.mob.isBaby() && this.mob.sulfurSpitCooldown <= 0 && this.mob.hasLineOfSight(target)) {
                boolean inSpitRange = surfDist >= SULFUR_SPIT_MIN_REACH && surfDist <= SULFUR_SPIT_MAX_REACH;
                if (pillaring || this.mob.retaliateWithSpit || inSpitRange) {
                    this.mob.startSulfurSpit();
                    return;
                }
            }

            // 3. Close-range Snap Bite (adult) or Baby Nip (juvenile) (disabled when target is pillaring)
            double biteReach = this.mob.isBaby() ? BABY_NIP_MAX_REACH : SNAP_BITE_MAX_REACH;
            if (this.mob.snapBiteCooldown <= 0 && surfDist <= biteReach && !pillaring) {
                this.mob.startSnapBite();
                return;
            }

            // 4. Stalking (> 8 blocks) vs Pursuing (<= 8 blocks)
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            boolean stalking = surfDist > STALKING_MIN_RANGE;
            this.mob.setState(stalking ? State.STALKING : State.PURSUING);
            if (--this.pathRecalcTicks <= 0) {
                this.pathRecalcTicks = 8;
                double speedMod = stalking ? 0.85D : 1.15D;
                this.mob.getNavigation().moveTo(target, speedMod);
            }
        }
    }

    /** Ecological goal: outside the Nether, actively seeks incandescent heat sources when idle. */
    private static final class SeekHeatOutsideNetherGoal extends Goal {
        private final BrimstoneSalamander mob;
        private int scanCooldown = 20;

        SeekHeatOutsideNetherGoal(BrimstoneSalamander mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.mob.level().dimension().equals(Level.NETHER)
                    || this.mob.getTarget() != null
                    || this.mob.isInAttackOrRecovery()) {
                return false;
            }
            if (--this.scanCooldown > 0) {
                return false;
            }
            this.scanCooldown = 40;
            BlockPos found = this.findNearestHeatBlock();
            if (found != null) {
                this.mob.cachedHeatTarget = found;
                return this.mob.blockPosition().distSqr(found) > 2.25D;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            BlockPos target = this.mob.cachedHeatTarget;
            return target != null
                    && this.mob.getTarget() == null
                    && !this.mob.getNavigation().isDone()
                    && this.mob.blockPosition().distSqr(target) > 2.25D;
        }

        @Override
        public void start() {
            BlockPos t = this.mob.cachedHeatTarget;
            if (t != null) {
                this.mob.getNavigation().moveTo(t.getX() + 0.5D, t.getY(), t.getZ() + 0.5D, 1.0D);
            }
        }

        private @Nullable BlockPos findNearestHeatBlock() {
            BlockPos origin = this.mob.blockPosition();
            Level level = this.mob.level();
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int dy = -2; dy <= 2; dy++) {
                for (int dx = -6; dx <= 6; dx++) {
                    for (int dz = -6; dz <= 6; dz++) {
                        cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                        BlockState state = level.getBlockState(cursor);
                        if (isHeatBlock(state)) {
                            double d = origin.distSqr(cursor);
                            if (d < bestDist) {
                                bestDist = d;
                                best = cursor.immutable();
                            }
                        }
                    }
                }
            }
            return best;
        }

        private static boolean isHeatBlock(BlockState state) {
            return state.is(Blocks.LAVA)
                    || state.is(Blocks.MAGMA_BLOCK)
                    || state.is(Blocks.CAMPFIRE)
                    || state.is(Blocks.SOUL_CAMPFIRE)
                    || state.is(Blocks.FIRE)
                    || state.is(Blocks.SOUL_FIRE)
                    || state.is(Blocks.TORCH);
        }
    }

    /** Baby flee goal: retreats behind an adult family member, into lava, or away from large threats. */
    private static final class SalamanderBabyFleeGoal extends Goal {
        private final BrimstoneSalamander baby;
        private @Nullable LivingEntity threat;
        private @Nullable Vec3 fleeTarget;
        private int fleeTicks = 0;

        SalamanderBabyFleeGoal(BrimstoneSalamander baby) {
            this.baby = baby;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!this.baby.isBaby() || this.baby.isInAttackOrRecovery()) {
                return false;
            }
            AABB search = this.baby.getBoundingBox().inflate(8.0D, 4.0D, 8.0D);
            List<LivingEntity> threats = this.baby.level().getEntitiesOfClass(
                    LivingEntity.class, search,
                    e -> (e instanceof Player p && !p.isCreative() && !p.isSpectator()) || e instanceof IronGolem
            );
            if (threats.isEmpty()) {
                return false;
            }
            this.threat = threats.get(0);
            this.fleeTarget = this.calculateFleeTarget(this.threat);
            return this.fleeTarget != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.threat != null && this.threat.isAlive() && this.fleeTicks > 0
                    && this.baby.distanceToSqr(this.threat) < 144.0D
                    && !this.baby.getNavigation().isDone();
        }

        @Override
        public void start() {
            this.fleeTicks = 60;
            if (this.fleeTarget != null) {
                this.baby.getNavigation().moveTo(this.fleeTarget.x, this.fleeTarget.y, this.fleeTarget.z, 1.25D);
            }
        }

        @Override
        public void tick() {
            this.fleeTicks--;
            if (this.fleeTicks % 15 == 0 && this.threat != null && this.threat.isAlive()) {
                Vec3 newTarget = this.calculateFleeTarget(this.threat);
                if (newTarget != null) {
                    this.fleeTarget = newTarget;
                    this.baby.getNavigation().moveTo(newTarget.x, newTarget.y, newTarget.z, 1.25D);
                }
            }
        }

        @Override
        public void stop() {
            this.threat = null;
            this.fleeTarget = null;
            this.fleeTicks = 0;
            this.baby.getNavigation().stop();
        }

        private @Nullable Vec3 calculateFleeTarget(LivingEntity threat) {
            BrimstoneSalamander adult = this.findNearbyAdult();
            if (adult != null) {
                Vec3 adultPos = adult.position();
                Vec3 threatPos = threat.position();
                Vec3 awayFromThreat = adultPos.subtract(threatPos).horizontal().normalize();
                if (awayFromThreat.lengthSqr() < 1.0E-4D) {
                    awayFromThreat = new Vec3(1, 0, 0);
                }
                return adultPos.add(awayFromThreat.scale(2.5D));
            }

            BlockPos lavaPos = this.findNearbyLava();
            if (lavaPos != null) {
                return Vec3.atBottomCenterOf(lavaPos);
            }

            Vec3 dir = this.baby.position().subtract(threat.position()).horizontal().normalize();
            if (dir.lengthSqr() < 1.0E-4D) {
                dir = new Vec3(this.baby.getRandom().nextGaussian(), 0, this.baby.getRandom().nextGaussian()).normalize();
            }
            return this.baby.position().add(dir.scale(8.0D));
        }

        private @Nullable BrimstoneSalamander findNearbyAdult() {
            AABB box = this.baby.getBoundingBox().inflate(16.0D, 8.0D, 16.0D);
            List<BrimstoneSalamander> adults = this.baby.level().getEntitiesOfClass(
                    BrimstoneSalamander.class, box,
                    other -> other != this.baby && !other.isBaby() && other.isAlive()
            );
            if (adults.isEmpty()) return null;
            UUID familyId = this.baby.getFamilyId();
            for (BrimstoneSalamander a : adults) {
                if (familyId != null && familyId.equals(a.getFamilyId())) {
                    return a;
                }
            }
            return adults.get(0);
        }

        private @Nullable BlockPos findNearbyLava() {
            BlockPos origin = this.baby.blockPosition();
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int r = 1; r <= 8; r++) {
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dx = -r; dx <= r; dx++) {
                        for (int dz = -r; dz <= r; dz++) {
                            cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                            if (this.baby.level().getFluidState(cursor).is(FluidTags.LAVA)) {
                                return cursor.immutable();
                            }
                        }
                    }
                }
            }
            return null;
        }
    }

    /** Follow family goal: keeps juveniles safely escorted near their adult family leader. */
    private static final class SalamanderFollowFamilyGoal extends Goal {
        private final BrimstoneSalamander baby;
        private @Nullable BrimstoneSalamander adult;
        private int checkCooldown = 0;

        SalamanderFollowFamilyGoal(BrimstoneSalamander baby) {
            this.baby = baby;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!this.baby.isBaby() || this.baby.isInAttackOrRecovery()) {
                return false;
            }
            if (--this.checkCooldown > 0) {
                return false;
            }
            this.checkCooldown = 10;
            this.adult = this.findAdult();
            if (this.adult != null) {
                double distSq = this.baby.distanceToSqr(this.adult);
                return distSq > 20.25D; // > 4.5 blocks
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.baby.isBaby() || this.baby.isInAttackOrRecovery() || this.adult == null || !this.adult.isAlive()) {
                return false;
            }
            double distSq = this.baby.distanceToSqr(this.adult);
            return distSq > 4.0D && distSq < 576.0D; // between 2.0 and 24.0 blocks
        }

        @Override
        public void start() {
            if (this.adult != null) {
                this.baby.getNavigation().moveTo(this.adult, 1.15D);
            }
        }

        @Override
        public void tick() {
            if (this.adult == null) return;
            this.baby.getLookControl().setLookAt(this.adult, 20.0F, 20.0F);
            if (this.baby.getNavigation().isDone() || this.baby.tickCount % 20 == 0) {
                double distSq = this.baby.distanceToSqr(this.adult);
                double speed = distSq > 100.0D ? 1.25D : 1.15D;
                this.baby.getNavigation().moveTo(this.adult, speed);
            }
        }

        @Override
        public void stop() {
            this.adult = null;
            this.baby.getNavigation().stop();
        }

        private @Nullable BrimstoneSalamander findAdult() {
            AABB box = this.baby.getBoundingBox().inflate(16.0D, 8.0D, 16.0D);
            List<BrimstoneSalamander> list = this.baby.level().getEntitiesOfClass(
                    BrimstoneSalamander.class, box,
                    other -> other != this.baby && !other.isBaby() && other.isAlive()
            );
            if (list.isEmpty()) {
                return null;
            }
            UUID familyId = this.baby.getFamilyId();
            UUID leaderUUID = this.baby.getLeaderUUID();
            for (BrimstoneSalamander a : list) {
                if (leaderUUID != null && leaderUUID.equals(a.getUUID())) {
                    return a;
                }
                if (familyId != null && familyId.equals(a.getFamilyId())) {
                    return a;
                }
            }
            BrimstoneSalamander closest = null;
            double minDist = Double.MAX_VALUE;
            for (BrimstoneSalamander a : list) {
                double d = this.baby.distanceToSqr(a);
                if (d < minDist) {
                    minDist = d;
                    closest = a;
                }
            }
            return closest;
        }
    }
}
