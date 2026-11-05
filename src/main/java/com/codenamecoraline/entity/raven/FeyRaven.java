package com.codenamecoraline.entity.raven;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * The Fey Raven ({@code codenamecoraline:fey_raven}): a three-eyed mystic messenger and companion
 * of the twilight courts. Slice 1 provides the foundation entity, attributes, state machine enum,
 * immunities and natural spawn rules.
 */
public final class FeyRaven extends TamableAnimal {
    public static final float WIDTH = 0.5F;
    public static final float HEIGHT = 0.65F;
    public static final float EYE_HEIGHT = 0.55F;

    // Reserved entity event range: 140..149
    public static final byte EVENT_BLINK_THIRD_EYE = (byte) 140;
    public static final byte EVENT_MYSTIC_ALERT = (byte) 141;
    public static final byte EVENT_OPEN_SCROLL = (byte) 142;
    public static final byte EVENT_IDLE_CLEAN = (byte) 143;
    public static final byte EVENT_IDLE_CAW = (byte) 144;
    public static final byte EVENT_PECK = (byte) 145;

    private static final EntityDataAccessor<Integer> DATA_STATE =
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

    public final AnimationState idleBreatheAnimationState = new AnimationState();
    public final AnimationState idleHeadTiltAnimationState = new AnimationState();
    public final AnimationState idlePreenAnimationState = new AnimationState();
    public final AnimationState idleCawAnimationState = new AnimationState();
    public final AnimationState flyFlapAnimationState = new AnimationState();
    public final AnimationState flyGlideAnimationState = new AnimationState();
    public final AnimationState peckAttackAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

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
        builder.define(DATA_AIRBORNE, false);
        builder.define(DATA_SCROLL_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public State getState() {
        int idx = this.entityData.get(DATA_STATE);
        State[] values = State.values();
        return idx >= 0 && idx < values.length ? values[idx] : State.WILD_IDLE;
    }

    public void setState(State state) {
        this.entityData.set(DATA_STATE, state.ordinal());
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

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.updateClientAnimations();
        }
    }

    private void updateClientAnimations() {
        if (this.isDeadOrDying()) {
            this.deathAnimationState.startIfStopped(this.tickCount);
            return;
        }
        this.idleBreatheAnimationState.startIfStopped(this.tickCount);
        if (this.isAirborne() || !this.onGround()) {
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
        return super.hurtServer(level, source, amount);
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
        output.putBoolean("Airborne", this.isAirborne());
        ItemStack scroll = this.getScrollItem();
        if (!scroll.isEmpty()) {
            output.store("ScrollItem", ItemStack.CODEC, scroll);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String stateName = input.getStringOr("RavenState", State.WILD_IDLE.name());
        try {
            this.setState(State.valueOf(stateName));
        } catch (IllegalArgumentException ignored) {
            this.setState(State.WILD_IDLE);
        }
        this.setAirborne(input.getBooleanOr("Airborne", false));
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

