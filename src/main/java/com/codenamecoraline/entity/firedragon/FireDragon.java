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

/** Baby-only entity with persistent appearance and server-selected visual idles; companion AI comes later. */
public final class FireDragon extends TamableAnimal {
    public static final float WIDTH = 0.55F;
    public static final float HEIGHT = 0.65F;
    public static final float EYE_HEIGHT = 0.48F;

    public enum State { WILD_IDLE }
    private static final FireDragonAnimation[] VISUAL_CLIPS = FireDragonAnimation.values();
    private static final EntityDataAccessor<Integer> COLORS = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CLIP = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> CLIP_START = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Boolean> PREVIEW = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.BOOLEAN);
    private int nextIdleTicks = 120;

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
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
    private void playVisual(FireDragonAnimation animation) {
        entityData.set(CLIP_START, level().getGameTime());
        entityData.set(CLIP, animation.ordinal());
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() || isVisualPreview() || !isAlive()) return;
        if (getVisualAnimation() != FireDragonAnimation.NONE
                && getVisualAnimationTicks(0) >= getVisualAnimation().ticks) playVisual(FireDragonAnimation.NONE);
        if (--nextIdleTicks > 0) return;
        nextIdleTicks = 100 + random.nextInt(101);
        if (getTarget() == null && onGround() && !isInSittingPose() && getDeltaMovement().horizontalDistanceSqr() < 0.001D) {
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
        output.putInt("PrimaryColor", getPrimaryColor());
        output.putInt("SecondaryColor", getSecondaryColor());
        output.putInt("AccentColor", getAccentColor());
        output.putString("VisualPreview", isVisualPreview() ? getVisualAnimation().name() : "none");
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
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
        return State.WILD_IDLE;
    }

    @Override
    protected void registerGoals() {
        // Deliberately inert until the companion AI slice.
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
        if (source.is(DamageTypeTags.IS_FALL)) return false;
        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.SALMON);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Prevent vanilla feeding/breeding from substituting the future growth contract.
        return InteractionResult.PASS;
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
