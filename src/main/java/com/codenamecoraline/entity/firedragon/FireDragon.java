package com.codenamecoraline.entity.firedragon;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

/** Baby-only entity with persistent appearance and server-selected visual idles; companion logic remains server-authoritative. */
public final class FireDragon extends TamableAnimal {
    public static final float WIDTH = 0.55F;
    public static final float HEIGHT = 0.65F;
    public static final float EYE_HEIGHT = 0.48F;

    public enum State { WILD_IDLE, FOLLOWING, SITTING, SHOULDER, HUNTING, DEFENDING, SOCIAL }
    private static final State[] STATES = State.values();
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHOULDER = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private final FireDragonCompanion companion = new FireDragonCompanion(this);
    private int satiety = 100;
    private int hungerTicks;
    private int growthRemaining = 72000;
    private boolean hatched;
    public int getSatiety() { return satiety; }
    public int getGrowthRemaining() { return growthRemaining; }
    public boolean isHatched() { return hatched; }
    public int getShoulderSide() { return entityData.get(SHOULDER); }
    public boolean isOnShoulder() { return getShoulderSide() != 0; }
    void setShoulderSide(int side) { entityData.set(SHOULDER, side); }
    void setState(State state) { entityData.set(STATE, state.ordinal()); }
    public void feedSalmon() {
        heal(4);
        satiety = Math.min(100, satiety + 25);
        growthRemaining = Math.max(0, growthRemaining - Math.max(1, (growthRemaining + 9) / 10));
        playVisual(FireDragonAnimation.EAT);
    }
    void eatMeat() { satiety = Math.min(100, satiety + 25); playVisual(FireDragonAnimation.EAT); }
    public boolean canDamage(net.minecraft.world.entity.Entity target) {
        if (!(target instanceof net.minecraft.world.entity.LivingEntity living) || target == this || !living.isAlive()
                || living.isInvulnerable() || isAlliedTo(target)) return false;
        var owner = getOwner();
        if (target == owner) return false;
        if (target instanceof TamableAnimal pet && getOwnerReference() != null
                && getOwnerReference().equals(pet.getOwnerReference())) return false;
        if (target instanceof Player player) {
            if (!isTame() || player.isCreative() || player.isSpectator()) return false;
            if (owner instanceof Player own && (!own.canHarmPlayer(player)
                    || !((ServerLevel) level()).isPvpAllowed())) return false;
        }
        return true;
    }
    void companionSocial(FireDragon other, FireDragonAnimation clip) { companion.acceptSocial(other, clip); }
    public boolean landOnShoulder(Player player) { return companion.perch(player); }
    private void tickNutrition() {
        if (++hungerTicks >= 600) { hungerTicks = 0; satiety = Math.max(0, satiety - 1); }
        if (satiety > 0 && growthRemaining > 0) --growthRemaining;
        // Stay a baby at zero until the young stage is implemented.
    }
    private static final FireDragonAnimation[] VISUAL_CLIPS = FireDragonAnimation.values();
    private static final EntityDataAccessor<Integer> COLORS = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CLIP = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> CLIP_START = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Boolean> PREVIEW = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.BOOLEAN);
    private int nextIdleTicks = 120;

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, State.WILD_IDLE.ordinal());
        builder.define(SHOULDER, 0);
        builder.define(COLORS, 70);
        builder.define(CLIP, 0);
        builder.define(CLIP_START, 0L);
        builder.define(PREVIEW, false);
    }

    public int getColorCode() { return entityData.get(COLORS); }
    public int getPrimaryColor() { return getColorCode() / 25; }
    public int getSecondaryColor() { return getColorCode() / 5 % 5; }
    public int getAccentColor() { return getColorCode() % 5; }
    public void setAppearance(int primary, int secondary, int accent) {
        entityData.set(COLORS, clampColor(primary) * 25 + clampColor(secondary) * 5 + clampColor(accent));
    }
    private static int clampColor(int color) { return Math.clamp(color, 0, 4); }
    public boolean isVisualPreview() { return entityData.get(PREVIEW); }
    public FireDragonAnimation getVisualAnimation() { return VISUAL_CLIPS[entityData.get(CLIP)]; }
    public float getVisualAnimationTicks(float partialTick) {
        float ticks = Math.max(0, level().getGameTime() - entityData.get(CLIP_START) + partialTick);
        return isVisualPreview() ? ticks % (getVisualAnimation().ticks + 10) : ticks;
    }
    void playVisual(FireDragonAnimation animation) {
        entityData.set(CLIP_START, level().getGameTime());
        entityData.set(CLIP, animation.ordinal());
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() || isVisualPreview() || !isAlive()) return;
        tickNutrition();
        if (!isNoAi()) companion.tick();
        if (getVisualAnimation() != FireDragonAnimation.NONE
                && getVisualAnimationTicks(0) >= getVisualAnimation().ticks) playVisual(FireDragonAnimation.NONE);
        if (--nextIdleTicks > 0) return;
        nextIdleTicks = 100 + random.nextInt(101);
        if ((getState() == State.WILD_IDLE || getState() == State.FOLLOWING) && getVisualAnimation() == FireDragonAnimation.NONE && getTarget() == null && onGround() && !isInSittingPose() && getDeltaMovement().horizontalDistanceSqr() < 0.001D) {
            playVisual(switch (random.nextInt(3)) {
                case 0 -> FireDragonAnimation.IDLE_SNIFF;
                case 1 -> FireDragonAnimation.IDLE_WINGS;
                default -> FireDragonAnimation.IDLE_TAIL;
            });
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        var result = super.finalizeSpawn(level, difficulty, reason, data);
        int primary = random.nextInt(5);
        int secondary = (primary + 1 + random.nextInt(4)) % 5;
        setAppearance(primary, secondary, random.nextInt(5));
        return result;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Satiety", satiety);
        output.putInt("HungerTicks", hungerTicks);
        output.putInt("GrowthRemaining", growthRemaining);
        output.putBoolean("Hatched", hatched);
        output.putInt("DragonShoulder", getShoulderSide());
        output.putInt("PrimaryColor", getPrimaryColor());
        output.putInt("SecondaryColor", getSecondaryColor());
        output.putInt("AccentColor", getAccentColor());
        output.putString("VisualPreview", isVisualPreview() ? getVisualAnimation().name() : "none");
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        satiety = Math.clamp(input.getIntOr("Satiety", 100), 0, 100);
        hungerTicks = Math.clamp(input.getIntOr("HungerTicks", 0), 0, 599);
        growthRemaining = Math.clamp(input.getIntOr("GrowthRemaining", 72000), 0, 72000);
        hatched = input.getBooleanOr("Hatched", false);
        setShoulderSide(isTame() ? Math.clamp(input.getIntOr("DragonShoulder", 0), -1, 1) : 0);
        companion.resetAfterLoad();
        setAppearance(input.getIntOr("PrimaryColor", 2), input.getIntOr("SecondaryColor", 4), input.getIntOr("AccentColor", 0));
        FireDragonAnimation preview = FireDragonAnimation.fromName(input.getStringOr("VisualPreview", "none"));
        entityData.set(PREVIEW, preview != FireDragonAnimation.NONE);
        playVisual(preview);
    }


    public FireDragon(EntityType<? extends FireDragon> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FLYING_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public State getState() {
        return STATES[entityData.get(STATE)];
    }

    @Override
    protected void registerGoals() {
        // Companion priorities are coordinated by FireDragonCompanion on the server.
    }

    public static boolean checkSpawnRules(EntityType<FireDragon> type, ServerLevelAccessor level,
            EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION;
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FALL) || (source.getEntity() != null && source.getEntity() == getOwner())
                || (source.getEntity() instanceof TamableAnimal pet && getOwnerReference() != null
                && getOwnerReference().equals(pet.getOwnerReference()))) return false;
        boolean accepted = super.hurtServer(level, source, amount);
        if (accepted && source.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker) {
            companion.onDamaged(attacker);
        }
        return accepted;
    }

    @Override
    public boolean isPushable() { return !isOnShoulder() && super.isPushable(); }

    @Override
    public void push(net.minecraft.world.entity.Entity other) {
        if (!isOnShoulder()) super.push(other);
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity other) {
        if (!isOnShoulder()) super.doPush(other);
    }

    @Override
    public boolean canCollideWith(net.minecraft.world.entity.Entity other) {
        return !isOnShoulder() && super.canCollideWith(other);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.SALMON);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (isTame() && !isOwnedBy(player)) return InteractionResult.PASS;
        if (!isTame() && !held.is(Items.SALMON)) return InteractionResult.PASS;
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        if (isTame() && player.isShiftKeyDown()) {
            landOnShoulder(player);
            return InteractionResult.SUCCESS;
        }
        if (held.is(Items.SALMON)) {
            if (!player.getAbilities().instabuild) held.shrink(1);
            if (!isTame()) {
                if (random.nextFloat() < (hatched ? 0.5F : 0.33F)) {
                    tame(player);
                    setOrderedToSit(false);
                    level().broadcastEntityEvent(this, (byte) 7);
                } else level().broadcastEntityEvent(this, (byte) 6);
            }
            feedSalmon();
            return InteractionResult.SUCCESS;
        }
        if (!isOnShoulder()) {
            setOrderedToSit(!isOrderedToSit());
            setInSittingPose(isOrderedToSit());
            getNavigation().stop();
            setTarget(null);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canMate(Animal other) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }
}
