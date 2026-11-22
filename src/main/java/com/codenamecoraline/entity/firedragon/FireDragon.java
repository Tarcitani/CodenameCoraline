package com.codenamecoraline.entity.firedragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;
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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
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

/** Baby and Young Fire Dragon entity sharing persistent UUID, owner, colors and server-authoritative companion AI. */
public final class FireDragon extends TamableAnimal {
    public static final float WIDTH = 0.55F;
    public static final float HEIGHT = 0.65F;
    public static final float EYE_HEIGHT = 0.48F;
    public static final float YOUNG_WIDTH = 1.0F;
    public static final float YOUNG_HEIGHT = 1.1F;
    public static final float YOUNG_EYE_HEIGHT = 0.95F;
    public static final int BABY_GROWTH_TICKS = 72000;
    public static final int YOUNG_GROWTH_TICKS = 144000;
    private static final EntityDimensions BABY_DIMENSIONS = EntityDimensions.scalable(WIDTH, HEIGHT).withEyeHeight(EYE_HEIGHT);
    private static final EntityDimensions YOUNG_DIMENSIONS = EntityDimensions.scalable(YOUNG_WIDTH, YOUNG_HEIGHT).withEyeHeight(YOUNG_EYE_HEIGHT);

    public enum Stage { BABY, YOUNG }
    private static final Stage[] STAGES = Stage.values();

    public enum State { WILD_IDLE, FOLLOWING, SITTING, SHOULDER, HUNTING, DEFENDING, SOCIAL }
    private static final State[] STATES = State.values();
    private static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHOULDER = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private final FireDragonCompanion companion = new FireDragonCompanion(this);
    private int satiety = 100;
    private int hungerTicks;
    private int growthRemaining = BABY_GROWTH_TICKS;
    private boolean hatched;
    public Stage getStage() { return STAGES[entityData.get(STAGE)]; }
    public boolean isBabyStage() { return getStage() == Stage.BABY; }
    public boolean isYoung() { return getStage() == Stage.YOUNG; }
    public void setStage(Stage stage) {
        if (getStage() == stage) return;
        entityData.set(STAGE, stage.ordinal());
        applyStageAttributes(stage);
        refreshDimensions();
    }
    public int getSatiety() { return satiety; }
    public int getGrowthRemaining() { return growthRemaining; }
    public boolean isHatched() { return hatched; }
    public int getShoulderSide() { return entityData.get(SHOULDER); }
    public boolean isOnShoulder() { return getShoulderSide() != 0; }
    void setShoulderSide(int side) { entityData.set(SHOULDER, side); }
    void setState(State state) { entityData.set(STATE, state.ordinal()); }
    public void feedSalmon() {
        heal(isYoung() ? 6.0F : 4.0F);
        satiety = Math.min(100, satiety + 25);
        if (growthRemaining > 0) {
            growthRemaining = Math.max(0, growthRemaining - Math.max(1, (growthRemaining + 9) / 10));
        }
        playVisual(FireDragonAnimation.EAT);
        playSound(ModSounds.FIRE_DRAGON_PURR.get(), 0.55F, isYoung() ? 0.85F : 1.05F);
        companion.onFed();
        if (!level().isClientSide() && isBabyStage() && growthRemaining <= 0) evolveToYoung();
    }

    public boolean evolveToYoung() {
        if (!isBabyStage()) return false;
        if (isOnShoulder() && !companion.dismountForEvolution()) return false;
        float ratio = getMaxHealth() > 0.0F ? getHealth() / getMaxHealth() : 1.0F;
        setStage(Stage.YOUNG);
        setHealth(Math.min(getMaxHealth(), Math.max(1.0F, getMaxHealth() * ratio)));
        growthRemaining = YOUNG_GROWTH_TICKS;
        playSound(ModSounds.FIRE_DRAGON_EVOLVE.get(), 0.9F, 1.0F);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.55D, getZ(), 24, 0.45D, 0.4D, 0.45D, 0.09D);
            sl.sendParticles(ParticleTypes.SMALL_FLAME, getX(), getY() + 0.55D, getZ(), 18, 0.5D, 0.45D, 0.5D, 0.12D);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.6D, getZ(), 10, 0.4D, 0.35D, 0.4D, 0.04D);
        }
        return true;
    }

    private void applyStageAttributes(Stage stage) {
        boolean young = stage == Stage.YOUNG;
        setBaseAttr(Attributes.MAX_HEALTH, young ? 48.0D : 20.0D);
        setBaseAttr(Attributes.ARMOR, young ? 5.0D : 2.0D);
        setBaseAttr(Attributes.MOVEMENT_SPEED, young ? 0.28D : 0.25D);
        setBaseAttr(Attributes.FLYING_SPEED, young ? 0.30D : 0.20D);
        setBaseAttr(Attributes.ATTACK_DAMAGE, young ? 6.0D : 3.0D);
        setBaseAttr(Attributes.FOLLOW_RANGE, young ? 24.0D : 16.0D);
    }

    private void setBaseAttr(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, double val) {
        var inst = getAttribute(attr);
        if (inst != null) inst.setBaseValue(val);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return isYoung() ? YOUNG_DIMENSIONS : BABY_DIMENSIONS;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (STAGE.equals(key)) refreshDimensions();
    }

    static void awardAdvancement(Player player, String name, String criterion) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer sp) || sp.level().getServer() == null) return;
        var holder = sp.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "bosses/" + name));
        if (holder != null) sp.getAdvancements().award(holder, criterion);
    }
    void eatMeat() { satiety = Math.min(100, satiety + 25); playVisual(FireDragonAnimation.EAT); companion.onFed(); }
    public boolean canDamage(net.minecraft.world.entity.Entity target) {
        if (!(target instanceof net.minecraft.world.entity.LivingEntity living) || target == this || !living.isAlive()
                || living.isInvulnerable() || isAlliedTo(target)) return false;
        var owner = getOwner();
        if (target == owner) return false;
        if (target instanceof TamableAnimal pet && getOwnerReference() != null
                && getOwnerReference().equals(pet.getOwnerReference())) return false;
        if (target instanceof Player player) {
            if (!isTame() && isBabyStage()) return false;
            if (player.isCreative() || player.isSpectator()) return false;
            if (owner instanceof Player own && (!own.canHarmPlayer(player)
                    || !((ServerLevel) level()).isPvpAllowed())) return false;
        }
        return true;
    }
    boolean canSocialize() { return companion.isAvailableForSocial(); }
    void startSocial(FireDragon other, FireDragonAnimation clip, boolean initiator) { companion.acceptSocial(other, clip, initiator); }
    void stopSocial() { companion.cancelSocial(); }
    public boolean landOnShoulder(Player player) { return companion.perch(player); }
    private void tickNutrition() {
        if (++hungerTicks >= 600) { hungerTicks = 0; satiety = Math.max(0, satiety - 1); }
        if (satiety > 0 && growthRemaining > 0) --growthRemaining;
        if (isBabyStage() && growthRemaining <= 0) evolveToYoung();
        // Young remains Young at zero until the Adult stage is implemented.
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
        builder.define(STAGE, Stage.BABY.ordinal());
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
        if (level().isClientSide()) {
            if (isAlive() && !isOnShoulder()) spawnClientParticles();
            return;
        }
        if (isVisualPreview() || !isAlive()) return;
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

    private void spawnClientParticles() {
        FireDragonAnimation clip = getVisualAnimation();
        if (clip == FireDragonAnimation.NONE) return;
        double yaw = Math.toRadians(yHeadRot);
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double forward = isYoung() ? 0.95D : 0.45D;
        double mx = getX() + fx * forward;
        double my = getY() + getEyeHeight() - 0.08D;
        double mz = getZ() + fz * forward;
        if (clip == FireDragonAnimation.FIRE_WINDUP || clip == FireDragonAnimation.BREATH_WINDUP) {
            float clipTicks = getVisualAnimationTicks(0);
            double angle = (tickCount * 1.15D) + random.nextDouble() * 0.6D;
            double radius = Math.max(0.14D, (isYoung() ? 0.52D : 0.36D) - clipTicks * 0.025D);
            double px = mx + Math.cos(angle) * radius;
            double py = my + (random.nextDouble() - 0.5D) * 0.22D;
            double pz = mz + Math.sin(angle) * radius;
            level().addParticle(ParticleTypes.SMALL_FLAME, px, py, pz,
                    (mx - px) * 0.18D, (my - py) * 0.18D, (mz - pz) * 0.18D);
            if (tickCount % 2 == 0) {
                level().addParticle(ParticleTypes.SMOKE, px, py, pz,
                        (mx - px) * 0.12D, 0.015D, (mz - pz) * 0.12D);
            }
            if (clipTicks >= 6.0F) {
                level().addParticle(ParticleTypes.FLAME, mx, my, mz,
                        fx * 0.05D + (random.nextDouble() - 0.5D) * 0.02D, 0.02D,
                        fz * 0.05D + (random.nextDouble() - 0.5D) * 0.02D);
            }
        } else if (clip == FireDragonAnimation.BREATH_ACTIVE) {
            for (int i = 0; i < 4; i++) {
                double spread = (random.nextDouble() - 0.5D) * 0.55D;
                double syaw = yaw + spread;
                double sx = -Math.sin(syaw);
                double sz = Math.cos(syaw);
                double speed = 0.28D + random.nextDouble() * 0.22D;
                level().addParticle(i % 2 == 0 ? ParticleTypes.FLAME : ParticleTypes.SMALL_FLAME,
                        mx, my, mz, sx * speed, (random.nextDouble() - 0.45D) * 0.08D, sz * speed);
            }
            if (tickCount % 2 == 0) {
                level().addParticle(ParticleTypes.SMOKE, mx, my + 0.05D, mz, fx * 0.16D, 0.04D, fz * 0.16D);
            }
        } else if (clip == FireDragonAnimation.GROWL && tickCount % 2 == 0) {
            level().addParticle(ParticleTypes.SMOKE, mx, my, mz, fx * 0.05D, 0.03D, fz * 0.05D);
            if (tickCount % 4 == 0) {
                level().addParticle(ParticleTypes.SMALL_FLAME, mx, my, mz, fx * 0.04D, 0.02D, fz * 0.04D);
            }
        } else if ((clip == FireDragonAnimation.FIRE_RECOVERY || clip == FireDragonAnimation.BREATH_RECOVERY) && tickCount % 2 == 0) {
            level().addParticle(ParticleTypes.SMOKE, mx, my + 0.02D, mz,
                    fx * 0.015D + (random.nextDouble() - 0.5D) * 0.015D, 0.035D,
                    fz * 0.015D + (random.nextDouble() - 0.5D) * 0.015D);
            if (tickCount % 4 == 0) {
                level().addParticle(ParticleTypes.SMALL_FLAME, mx, my, mz,
                        fx * 0.02D, 0.02D, fz * 0.02D);
            }
        } else if (clip == FireDragonAnimation.IDLE_TAIL && tickCount % 3 == 0) {
            float clipTicks = getVisualAnimationTicks(0);
            if (clipTicks >= 16.0F && clipTicks <= 26.0F) {
                level().addParticle(ParticleTypes.SMOKE, mx, my, mz,
                        fx * 0.02D, 0.03D, fz * 0.02D);
            }
        } else if (clip == FireDragonAnimation.EAT && tickCount % 4 == 0) {
            level().addParticle(ParticleTypes.SMOKE, mx, my - 0.05D, mz,
                    (random.nextDouble() - 0.5D) * 0.02D, 0.02D, (random.nextDouble() - 0.5D) * 0.02D);
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
        output.putString("Stage", isYoung() ? "young" : "baby");
        output.putInt("Satiety", satiety);
        output.putInt("HungerTicks", hungerTicks);
        output.putInt("GrowthRemaining", growthRemaining);
        output.putBoolean("Hatched", hatched);
        output.putInt("DragonShoulder", isBabyStage() ? getShoulderSide() : 0);
        output.putInt("PrimaryColor", getPrimaryColor());
        output.putInt("SecondaryColor", getSecondaryColor());
        output.putInt("AccentColor", getAccentColor());
        output.putString("VisualPreview", isVisualPreview() ? getVisualAnimation().name() : "none");
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        float loadedHealth = getHealth();
        Stage loadedStage = "young".equalsIgnoreCase(input.getStringOr("Stage", "baby")) ? Stage.YOUNG : Stage.BABY;
        setStage(loadedStage);
        if (loadedHealth > 0.0F) setHealth(Math.min(getMaxHealth(), loadedHealth));
        int maxGrowth = isYoung() ? YOUNG_GROWTH_TICKS : BABY_GROWTH_TICKS;
        satiety = Math.clamp(input.getIntOr("Satiety", 100), 0, 100);
        hungerTicks = Math.clamp(input.getIntOr("HungerTicks", 0), 0, 599);
        growthRemaining = Math.clamp(input.getIntOr("GrowthRemaining", maxGrowth), 0, maxGrowth);
        hatched = input.getBooleanOr("Hatched", false);
        setShoulderSide(isTame() && isBabyStage() ? Math.clamp(input.getIntOr("DragonShoulder", 0), -1, 1) : 0);
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
            if (isBabyStage()) landOnShoulder(player);
            return InteractionResult.SUCCESS;
        }
        if (held.is(Items.SALMON)) {
            if (!isTame() && isYoung() && !companion.canTameWildYoung(player)) {
                playSound(ModSounds.FIRE_DRAGON_GROWL.get(), 0.7F, 1.0F);
                return InteractionResult.SUCCESS;
            }
            if (!player.getAbilities().instabuild) held.shrink(1);
            if (!isTame()) {
                float chance = isYoung() ? 0.10F : (hatched ? 0.5F : 0.33F);
                if (random.nextFloat() < chance) {
                    tame(player);
                    setOrderedToSit(false);
                    awardAdvancement(player, "fire_dragon_ember", "interact_fire_dragon");
                    playSound(ModSounds.FIRE_DRAGON_PURR.get(), 0.6F, isYoung() ? 0.85F : 1.0F);
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
    protected SoundEvent getAmbientSound() {
        return ModSounds.FIRE_DRAGON_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.FIRE_DRAGON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.FIRE_DRAGON_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(ModSounds.FIRE_DRAGON_STEP.get(), 0.4F, 1.0F);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }
}
