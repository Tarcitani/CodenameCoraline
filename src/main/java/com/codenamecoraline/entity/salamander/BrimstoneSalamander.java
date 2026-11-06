package com.codenamecoraline.entity.salamander;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * Brimstone Salamander ({@code codenamecoraline:brimstone_salamander}): a predatory, magma-dwelling
 * amphibian of traditional fey alchemy lore. Native to Nether Basalt Deltas, Nether Wastes, and Crimson Forest shores.
 * Slice 1 provides the foundation entity, attributes, state enum, and spawn rules.
 */
public final class BrimstoneSalamander extends Monster {
    public static final float WIDTH = 1.3F;
    public static final float HEIGHT = 0.85F;
    public static final float EYE_HEIGHT = 0.55F;

    public static final int MIN_SPAWN_Y = 24;
    public static final int MAX_SPAWN_Y = 48;

    // Reserved entity event range: 150..159
    public static final byte EVENT_THROAT_PUFF = (byte) 150;
    public static final byte EVENT_SULFUR_SPIT = (byte) 151;
    public static final byte EVENT_TAIL_WHIP = (byte) 152;
    public static final byte EVENT_SNAP_BITE = (byte) 153;
    public static final byte EVENT_HURT = (byte) 154;
    public static final byte EVENT_DEATH = (byte) 155;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(BrimstoneSalamander.class, EntityDataSerializers.INT);

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
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 44.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.65D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, State.IDLE.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public State getState() {
        int idx = this.entityData.get(DATA_STATE);
        State[] values = State.values();
        return idx >= 0 && idx < values.length ? values[idx] : State.IDLE;
    }

    public void setState(State state) {
        this.entityData.set(DATA_STATE, state.ordinal());
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
        return super.hurtServer(level, source, amount);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("SalamanderState", this.getState().name());
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
}
