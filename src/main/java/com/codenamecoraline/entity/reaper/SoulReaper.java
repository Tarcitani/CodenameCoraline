package com.codenamecoraline.entity.reaper;

import com.codenamecoraline.entity.spirit.BossSpirit;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModSounds;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Reaper of Wandering Souls ({@code codenamecoraline:soul_reaper}): colossal psychopomp boss of the winter fey folklore.
 * Spec: {@code docs/design/soul_reaper.md}, {@code soul_reaper_animation.md}, {@code soul_reaper_systems.md}.
 */
public final class SoulReaper extends Monster {
    public static final float WIDTH = 2.2F;
    public static final float HEIGHT = 5.4F;
    public static final float EYE_HEIGHT = 4.8F;

    public static final double MAX_HEALTH = 350.0D;
    public static final double ARMOR = 12.0D;
    public static final double ARMOR_TOUGHNESS = 4.0D;
    public static final double BASE_DAMAGE = 14.0D;
    public static final double BASE_SPEED = 0.26D;
    public static final double STAGE_THREE_SPEED = 0.32D;
    public static final double KNOCKBACK_RESISTANCE = 0.90D;
    public static final double FOLLOW_RANGE = 48.0D;
    public static final double STEP_HEIGHT = 1.5D;
    public static final int XP_REWARD = 150;

    public static final int DESOLATION_RADIUS = 6;
    public static final int DESOLATION_INTERVAL_TICKS = 10;
    public static final int AWAKEN_DURATION_TICKS = 200;
    public static final int AWAKEN_SLAM_START_TICK = 150;
    public static final int AWAKEN_SLAM_IMPACT_TICK = 175;
    public static final int PHASE_TRANSITION_TICKS = 30;
    public static final int DEATH_DURATION_TICKS = 90;
    public static final double LEASH_RADIUS = 48.0D;
    public static final int DESPAWN_IDLE_TICKS = 600;

    // Anti-instakill & nullification constants
    public static final float POST_ARMOR_CAP_RATIO = 0.65F;
    public static final int MERCY_WINDOW_TICKS = 6;
    public static final float BREATH_RULE_THRESHOLD = 0.80F;
    public static final float HARVEST_MAX_DRAIN_RATIO = 0.60F;
    public static final float TOLL_MIN_HEALTH_FLOOR = 2.0F;
    public static final int NULLIFY_DELAY_NORMAL = 20;
    public static final int NULLIFY_DELAY_STAGE_3 = 8;

    // Reserved entity event range: 180..199 (ephemeral particle/sound/camera triggers).
    public static final byte EVENT_AWAKEN_START = (byte) 180;
    public static final byte EVENT_BLINK_SMOKE = (byte) 181;
    public static final byte EVENT_AWAKEN_SLAM = (byte) 182;
    public static final byte EVENT_HARVEST_PULSE = (byte) 183;
    public static final byte EVENT_FROST_WAVE = (byte) 184;
    public static final byte EVENT_SNOW_DISSOLVE = (byte) 185;
    public static final byte EVENT_NULLIFY_BEAM = (byte) 186;
    public static final byte EVENT_FLICK_SNUFF = (byte) 187;
    public static final byte EVENT_SHIELD_TITHE = (byte) 188;
    public static final byte EVENT_ARROW_REVERSAL = (byte) 189;
    public static final byte EVENT_PHASE_TWO = (byte) 190;
    public static final byte EVENT_PHASE_THREE = (byte) 191;
    public static final byte EVENT_HEAVY_SLAM = (byte) 192;
    public static final byte EVENT_DEATH_SHATTER = (byte) 193;

    private static final String TAG_STATE = "ReaperState";
    private static final String TAG_STAGE = "ReaperStage";
    private static final String TAG_PEAK_PLAYERS = "ReaperPeakPlayers";
    private static final String TAG_ANCHOR = "ReaperAnchor";

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(SoulReaper.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STAGE =
            SynchedEntityData.defineId(SoulReaper.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LOCOMOTION =
            SynchedEntityData.defineId(SoulReaper.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION_ID =
            SynchedEntityData.defineId(SoulReaper.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION_TICK =
            SynchedEntityData.defineId(SoulReaper.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REACT_ID =
            SynchedEntityData.defineId(SoulReaper.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LOCKED_TARGET_ID =
            SynchedEntityData.defineId(SoulReaper.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PEAK_PLAYERS =
            SynchedEntityData.defineId(SoulReaper.class, EntityDataSerializers.INT);

    /** High-level authoritative state machine. */
    public enum State {
        AWAKENING,
        COMBAT_IDLE,
        ATTACK_PREP,
        ATTACK_EXEC,
        ATTACK_RECOVERY,
        DODGING,
        TAUNTING,
        NULLIFYING,
        PHASE_TRANSITION,
        DYING,
        DESPAWNING
    }

    /** Monotonic combat stages (never regress once reached). */
    public enum Stage {
        STAGE_1(1.00F, BossEvent.BossBarColor.WHITE),
        STAGE_2(0.65F, BossEvent.BossBarColor.BLUE),
        STAGE_3(0.30F, BossEvent.BossBarColor.PURPLE);

        public final float healthCeiling;
        public final BossEvent.BossBarColor barColor;

        Stage(float healthCeiling, BossEvent.BossBarColor barColor) {
            this.healthCeiling = healthCeiling;
            this.barColor = barColor;
        }
    }

    /** Layer L0 locomotion modes (spec section 4.2 of soul_reaper_animation.md). */
    public enum LocomotionMode {
        GLIDE,
        ORBIT,
        STAFF_STRIDE,
        PREDATOR_RUN,
        MIST_ASCENT
    }

    /** Layer L2/L4 primary actions synchronized to clients. */
    public enum ActionId {
        NONE,
        AWAKEN_ERUPTION,
        AWAKEN_SLAM,
        IDLE_1,
        IDLE_2,
        IDLE_3,
        O1_WHIRLWIND,
        O2_BLINK_STRIKE,
        O3_SOUL_HARVEST,
        O4_FROST_WAVE,
        O5_REAPING_DASH,
        F1_RISING_CRESCENT,
        F2_HAFT_THRUST,
        F3_CROSS_REAP,
        F4_GRAVEDIGGER_SLAM,
        F5_LOW_SWEEP,
        F6_THREEFOLD_HARVEST,
        E1_SIDESTEP_GLIDE,
        E2_PHANTOM_RETREAT,
        E3_SNOW_DISSOLVE,
        E4_SCYTHE_VAULT,
        P1_BECKON,
        P2_FUNERAL_BOW,
        P3_HOURGLASS_COUNT,
        P4_BLADE_HONING,
        P5_HARE_TILT_ECHO,
        A1_VIAL_SILENCE,
        A2_APPLE_TOLL,
        A3_GREATER_TOLL,
        A5_SHIELD_TITHE,
        A6_ARROW_REVERSAL,
        DEATH
    }

    /** Layer L3 quick additive reaction overlays. */
    public enum ReactId {
        NONE,
        HURT,
        A4_FLICK_SNUFF
    }

    /** Snapshot of a single effect before consuming an item (for provenance-based nullification). */
    public record EffectSnapshot(Holder<MobEffect> effect, int amplifier, int duration) {}

    /** Pre-consumption snapshot of a living entity's active effects and health. */
    public record PreConsumeSnapshot(int tick, float health, Map<Holder<MobEffect>, EffectSnapshot> effects) {}

    /** Scheduled nullification (A1/A2/A3/A4) waiting for T0 + 20t (or T0 + 8t in Stage 3) resolution. */
    public record PendingNullification(
            UUID targetUuid,
            int resolveTick,
            ActionId categoryAction,
            List<Holder<MobEffect>> newlyAddedEffects,
            List<EffectSnapshot> upgradedPreExistingEffects,
            float instantHealToll) {}

    private record MercyRecord(int tick, int sequenceId, int pulseIndex) {}

    public final AnimationState primaryAnimState = new AnimationState();
    public final AnimationState reactAnimState = new AnimationState();

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            UUID.randomUUID(),
            Component.translatable("entity.codenamecoraline.soul_reaper"),
            BossEvent.BossBarColor.WHITE,
            BossEvent.BossBarOverlay.PROGRESS);

    private final Set<UUID> activeWisps = new HashSet<>();
    private final int[] attackCooldowns = new int[SoulReaperAttack.values().length];
    private final Deque<SoulReaperAttack> recentAttacks = new ArrayDeque<>(3);

    // Anti-instakill tracking state
    private final Map<UUID, MercyRecord> mercyRecords = new HashMap<>();
    private final Set<UUID> breathEligibleForSequence = new HashSet<>();
    private final Set<UUID> breathSpentUntilReheal = new HashSet<>();
    private final Map<UUID, Float> harvestDrainThisCast = new HashMap<>();
    private final Set<UUID> harvestStartedAboveHalf = new HashSet<>();

    // Habit & provenance nullification tracking
    private final Map<UUID, PreConsumeSnapshot> preConsumeSnapshots = new HashMap<>();
    private final List<PendingNullification> pendingNullifications = new ArrayList<>();
    private final Map<UUID, List<Integer>> shieldBlockTicks = new HashMap<>();
    private final Map<UUID, List<Integer>> projectileShotTicks = new HashMap<>();

    // Dialogue anti-spam tracking
    private int lastVoiceGlobalTick = -200;
    private final Map<String, Integer> lastVoiceCategoryTick = new HashMap<>();

    private boolean hasAnchor;
    private double anchorX;
    private double anchorY;
    private double anchorZ;
    private int stateTicks;
    private int serverTicks;
    private int deathTicks;
    private int combatTicks;
    private int noPlayerTicks;
    private int idleCooldownTicks = 120;
    private int elevatedTargetTicks;
    private int siegeTier2Ticks;

    // Active attack execution state
    private @Nullable SoulReaperAttack currentAttack;
    private int currentSequenceId;
    private int currentPulseIndex;
    private boolean currentAttackLandedHit;
    private Vec3 lockedForward = new Vec3(0.0D, 0.0D, 1.0D);
    private boolean activeStrikeContext;
    private boolean activeInstantTollContext;
    private boolean tauntBaitEnabled;
    private @Nullable UUID nullifyTargetUuid;
    private boolean arrowReflectedThisCast;

    public SoulReaper(EntityType<? extends SoulReaper> type, Level level) {
        super(type, level);
        this.xpReward = XP_REWARD;
        this.setPersistenceRequired();
        this.primaryAnimState.start(0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, BASE_SPEED)
                .add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
                .add(Attributes.STEP_HEIGHT, STEP_HEIGHT);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, State.COMBAT_IDLE.ordinal());
        builder.define(DATA_STAGE, Stage.STAGE_1.ordinal());
        builder.define(DATA_LOCOMOTION, LocomotionMode.GLIDE.ordinal());
        builder.define(DATA_ACTION_ID, ActionId.NONE.ordinal());
        builder.define(DATA_ACTION_TICK, 0);
        builder.define(DATA_REACT_ID, ReactId.NONE.ordinal());
        builder.define(DATA_LOCKED_TARGET_ID, -1);
        builder.define(DATA_PEAK_PLAYERS, 1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    public State getState() {
        int idx = this.entityData.get(DATA_STATE);
        State[] values = State.values();
        return idx >= 0 && idx < values.length ? values[idx] : State.COMBAT_IDLE;
    }

    public void setState(State state) {
        if (this.getState() != state) {
            this.stateTicks = 0;
        }
        this.entityData.set(DATA_STATE, state.ordinal());
    }

    public int getStateTicks() {
        return this.stateTicks;
    }

    public Stage getStage() {
        int idx = this.entityData.get(DATA_STAGE);
        Stage[] values = Stage.values();
        return idx >= 0 && idx < values.length ? values[idx] : Stage.STAGE_1;
    }

    /** Advances stage monotonically: never regresses below the highest reached stage. */
    public void setStage(Stage stage) {
        if (stage.ordinal() > this.getStage().ordinal()) {
            this.entityData.set(DATA_STAGE, stage.ordinal());
            this.bossEvent.setColor(stage.barColor);
            if (stage == Stage.STAGE_3) {
                AttributeInstance speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speedAttr != null) {
                    speedAttr.setBaseValue(STAGE_THREE_SPEED);
                }
            }
        }
    }

    public LocomotionMode getLocomotionMode() {
        int idx = this.entityData.get(DATA_LOCOMOTION);
        LocomotionMode[] values = LocomotionMode.values();
        return idx >= 0 && idx < values.length ? values[idx] : LocomotionMode.GLIDE;
    }

    public void setLocomotionMode(LocomotionMode mode) {
        this.entityData.set(DATA_LOCOMOTION, mode.ordinal());
    }

    public ActionId getActionId() {
        int idx = this.entityData.get(DATA_ACTION_ID);
        ActionId[] values = ActionId.values();
        return idx >= 0 && idx < values.length ? values[idx] : ActionId.NONE;
    }

    public void setActionId(ActionId action) {
        this.entityData.set(DATA_ACTION_ID, action.ordinal());
        this.entityData.set(DATA_ACTION_TICK, 0);
    }

    public int getActionTick() {
        return this.entityData.get(DATA_ACTION_TICK);
    }

    public ReactId getReactId() {
        int idx = this.entityData.get(DATA_REACT_ID);
        ReactId[] values = ReactId.values();
        return idx >= 0 && idx < values.length ? values[idx] : ReactId.NONE;
    }

    public void setReactId(ReactId react) {
        this.entityData.set(DATA_REACT_ID, react.ordinal());
    }

    public int getLockedTargetId() {
        return this.entityData.get(DATA_LOCKED_TARGET_ID);
    }

    public void setLockedTargetId(int entityId) {
        this.entityData.set(DATA_LOCKED_TARGET_ID, entityId);
    }

    public int getPeakPlayers() {
        return this.entityData.get(DATA_PEAK_PLAYERS);
    }

    /**
     * Monotonically updates {@code N_peak} (1..10) and rescales max health using
     * {@code 350 * (1 + 0.6 * (N_peak - 1))} while preserving the exact current health ratio.
     */
    public void updatePeakPlayers(int activePlayers) {
        int clamped = Mth.clamp(activePlayers, 1, 10);
        if (clamped > this.getPeakPlayers()) {
            float ratio = this.getMaxHealth() > 0.0F ? this.getHealth() / this.getMaxHealth() : 1.0F;
            this.entityData.set(DATA_PEAK_PLAYERS, clamped);
            double newMax = MAX_HEALTH * (1.0D + 0.6D * (clamped - 1));
            AttributeInstance maxHealthAttr = this.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttr != null) {
                maxHealthAttr.setBaseValue(newMax);
            }
            super.setHealth(Math.min((float) newMax, (float) newMax * ratio));
        }
    }

    @Override
    public void heal(float healAmount) {
        if (healAmount <= 0.0F) {
            return;
        }
        float ceiling = this.getMaxHealth() * this.getStage().healthCeiling;
        float target = Math.min(ceiling, this.getHealth() + healAmount);
        if (target > this.getHealth()) {
            super.setHealth(target);
        }
    }

    public void setAnchor(BlockPos pos) {
        this.hasAnchor = true;
        this.anchorX = pos.getX() + 0.5D;
        this.anchorY = pos.getY();
        this.anchorZ = pos.getZ() + 0.5D;
    }

    public Set<UUID> getActiveWisps() {
        return this.activeWisps;
    }

    public @Nullable SoulReaperAttack getCurrentAttack() {
        return this.currentAttack;
    }

    public int getAttackCooldown(SoulReaperAttack attack) {
        return this.attackCooldowns[attack.ordinal()];
    }

    public void setAttackCooldown(SoulReaperAttack attack, int ticks) {
        this.attackCooldowns[attack.ordinal()] = Math.max(0, ticks);
    }

    public Deque<SoulReaperAttack> getRecentAttacks() {
        return this.recentAttacks;
    }

    public boolean isActiveStrikeContext() {
        return this.activeStrikeContext;
    }

    public boolean isActiveInstantTollContext() {
        return this.activeInstantTollContext;
    }

    /** Starts the 200-tick metamorphosis awakening sequence. */
    public void beginAwakening() {
        this.setState(State.AWAKENING);
        this.setActionId(ActionId.AWAKEN_ERUPTION);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (!this.level().isClientSide()) {
            return;
        }
        if (DATA_ACTION_ID.equals(accessor)) {
            this.primaryAnimState.start(this.tickCount);
        } else if (DATA_REACT_ID.equals(accessor) && this.getReactId() != ReactId.NONE) {
            this.reactAnimState.start(this.tickCount);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
            return;
        }
        ServerLevel serverLevel = (ServerLevel) this.level();
        if (!this.hasAnchor) {
            this.setAnchor(this.blockPosition());
        }
        if (this.getRemainingFireTicks() > 40) {
            this.setRemainingFireTicks(40);
        }

        this.serverTicks++;
        this.stateTicks++;
        this.entityData.set(DATA_ACTION_TICK, this.getActionTick() + 1);
        for (int i = 0; i < this.attackCooldowns.length; i++) {
            if (this.attackCooldowns[i] > 0) {
                this.attackCooldowns[i]--;
            }
        }
        if (this.getReactId() != ReactId.NONE && this.stateTicks % 12 == 0) {
            this.setReactId(ReactId.NONE);
        }

        // Passive ecological effects: Glacial Desolation Aura & surface water freezing
        if (this.serverTicks % DESOLATION_INTERVAL_TICKS == 0) {
            this.tickDesolationAura(serverLevel, DESOLATION_RADIUS);
        }
        this.freezeSurfaceWaterUnderfoot(serverLevel);

        // Process scheduled potion/apple nullifications (A1..A4)
        this.tickPendingNullifications(serverLevel);

        // Refresh Breath Rule eligibility for players who healed back to >= 80% HP
        if (this.serverTicks % 10 == 0) {
            this.refreshMultiplayerAndBreathState(serverLevel);
        }

        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (this.isNoAi()) {
            return;
        }

        // Leash check (48 blocks from awakening anchor)
        if (this.hasAnchor && this.distanceToSqr(this.anchorX, this.anchorY, this.anchorZ) > LEASH_RADIUS * LEASH_RADIUS) {
            this.blinkToPosition(serverLevel, new Vec3(this.anchorX, this.anchorY, this.anchorZ));
        }

        switch (this.getState()) {
            case AWAKENING -> this.tickAwakening(serverLevel);
            case PHASE_TRANSITION -> this.tickPhaseTransition(serverLevel);
            case ATTACK_PREP -> this.tickAttackPrep(serverLevel);
            case ATTACK_EXEC -> this.tickAttackExec(serverLevel);
            case ATTACK_RECOVERY -> this.tickAttackRecovery(serverLevel);
            case DODGING -> this.tickDodging(serverLevel);
            case TAUNTING -> this.tickTaunting(serverLevel);
            case NULLIFYING -> this.tickNullifying(serverLevel);
            case COMBAT_IDLE -> this.tickCombatIdle(serverLevel);
            default -> {}
        }
    }

    // ================================================================ Passive Ecological Systems

    /**
     * Breaks natural vegetation (#leaves, #flowers, #saplings, short/tall grass, ferns, vines) within {@code radius},
     * respecting {@code mobGriefing} and skipping non-vegetation blocks. Zero stream allocations.
     */
    public int tickDesolationAura(ServerLevel level, int radius) {
        if (!EventHooks.canEntityGrief(level, this)) {
            return 0;
        }
        int broken = 0;
        BlockPos center = this.blockPosition();
        int rSqr = radius * radius;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = -2; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > rSqr) {
                        continue;
                    }
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (isNaturalVegetation(state)) {
                        level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 3);
                        level.sendParticles(
                                ParticleTypes.ASH,
                                cursor.getX() + 0.5D,
                                cursor.getY() + 0.5D,
                                cursor.getZ() + 0.5D,
                                4,
                                0.25D,
                                0.25D,
                                0.25D,
                                0.01D);
                        broken++;
                    }
                }
            }
        }
        return broken;
    }

    public static boolean isNaturalVegetation(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        return state.is(BlockTags.LEAVES)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.VINE)
                || state.is(Blocks.DEAD_BUSH)
                || state.is(Blocks.SWEET_BERRY_BUSH);
    }

    private void freezeSurfaceWaterUnderfoot(ServerLevel level) {
        BlockPos below = this.blockPosition().below();
        BlockState state = level.getBlockState(below);
        if (state.is(Blocks.WATER) && level.getBlockState(below.above()).isAir()) {
            level.setBlock(below, Blocks.FROSTED_ICE.defaultBlockState(), 3);
        }
    }

    private void refreshMultiplayerAndBreathState(ServerLevel level) {
        AABB box = this.getBoundingBox().inflate(FOLLOW_RANGE);
        List<ServerPlayer> players = level.getEntitiesOfClass(
                ServerPlayer.class,
                box,
                p -> p.isAlive() && !p.isSpectator() && !p.isCreative());
        if (!players.isEmpty()) {
            this.updatePeakPlayers(players.size());
            this.noPlayerTicks = 0;
        } else {
            this.noPlayerTicks += 10;
            if (this.noPlayerTicks >= DESPAWN_IDLE_TICKS && this.getState() == State.COMBAT_IDLE) {
                this.setState(State.DESPAWNING);
                this.discard();
                return;
            }
        }
        for (ServerPlayer player : players) {
            if (player.getHealth() >= player.getMaxHealth() * BREATH_RULE_THRESHOLD) {
                this.breathSpentUntilReheal.remove(player.getUUID());
            }
        }
    }

    // ================================================================ Awakening & Phase Transitions

    private void tickAwakening(ServerLevel level) {
        this.getNavigation().stop();
        if (this.stateTicks == 40) {
            this.tickDesolationAura(level, 12);
        } else if (this.stateTicks == AWAKEN_SLAM_START_TICK) {
            this.setActionId(ActionId.AWAKEN_SLAM);
        } else if (this.stateTicks == AWAKEN_SLAM_IMPACT_TICK) {
            level.broadcastEntityEvent(this, EVENT_AWAKEN_SLAM);
            level.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.4F, 0.7F);
            AABB waveBox = this.getBoundingBox().inflate(6.0D);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, waveBox, e -> e != this && e.isAlive())) {
                Vec3 diff = entity.position().subtract(this.position());
                double dist = Math.max(0.1D, Math.sqrt(diff.x * diff.x + diff.z * diff.z));
                Vec3 push = new Vec3((diff.x / dist) * 1.1D, 0.35D, (diff.z / dist) * 1.1D);
                this.applySafeKnockback(level, entity, push);
            }
        } else if (this.stateTicks >= AWAKEN_DURATION_TICKS) {
            this.setActionId(ActionId.IDLE_1);
            this.setState(State.COMBAT_IDLE);
            this.speakCategory(level, "awaken", 3);
        }
    }

    private void checkStageProgression(ServerLevel level) {
        float ratio = this.getHealth() / this.getMaxHealth();
        if (ratio <= Stage.STAGE_3.healthCeiling && this.getStage().ordinal() < Stage.STAGE_3.ordinal()) {
            this.setStage(Stage.STAGE_3);
            this.setState(State.PHASE_TRANSITION);
            this.setActionId(ActionId.AWAKEN_SLAM);
            level.broadcastEntityEvent(this, EVENT_PHASE_THREE);
            this.speakSpecificLine(level, "phase", "entity.codenamecoraline.soul_reaper.line.phase.2");
            this.summonWanderingSoulWisps(level);
        } else if (ratio <= Stage.STAGE_2.healthCeiling && this.getStage().ordinal() < Stage.STAGE_2.ordinal()) {
            this.setStage(Stage.STAGE_2);
            this.setState(State.PHASE_TRANSITION);
            this.setActionId(ActionId.AWAKEN_SLAM);
            level.broadcastEntityEvent(this, EVENT_PHASE_TWO);
            this.speakSpecificLine(level, "phase", "entity.codenamecoraline.soul_reaper.line.phase.1");
            this.summonWanderingSoulWisps(level);
        }
    }

    private void tickPhaseTransition(ServerLevel level) {
        this.getNavigation().stop();
        if (this.stateTicks >= PHASE_TRANSITION_TICKS) {
            this.setActionId(ActionId.IDLE_1);
            this.setState(State.COMBAT_IDLE);
        }
    }

    /** Summons {@code min(12, 4 + N_peak)} Wandering Soul Wisps bound to this Reaper. */
    public List<WanderingSoulWisp> summonWanderingSoulWisps(ServerLevel level) {
        this.activeWisps.removeIf(uuid -> {
            Entity e = level.getEntity(uuid);
            return e == null || !e.isAlive();
        });
        int desired = Math.min(12, 4 + this.getPeakPlayers());
        int toSpawn = Math.max(0, desired - this.activeWisps.size());
        List<WanderingSoulWisp> spawned = new ArrayList<>(toSpawn);
        for (int i = 0; i < toSpawn; i++) {
            WanderingSoulWisp wisp = ModEntities.WANDERING_SOUL_WISP.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (wisp != null) {
                double angle = (Math.PI * 2.0D * i) / Math.max(1, toSpawn);
                double wx = this.getX() + Math.cos(angle) * 4.5D;
                double wy = this.getY() + 2.2D;
                double wz = this.getZ() + Math.sin(angle) * 4.5D;
                wisp.snapTo(wx, wy, wz, (float) Math.toDegrees(angle), 0.0F);
                wisp.bindToReaper(this);
                level.addFreshEntity(wisp);
                spawned.add(wisp);
            }
        }
        return spawned;
    }

    // ================================================================ Combat Idle, Locomotion & AI Selection

    private void tickCombatIdle(ServerLevel level) {
        LivingEntity target = this.selectCombatTarget(level);
        if (target == null) {
            this.setLocomotionMode(LocomotionMode.GLIDE);
            if (level.isRaining() && level.isThundering() && this.tickCount % 40 == 0) {
                this.heal(1.0F);
            }
            if (--this.idleCooldownTicks <= 0) {
                this.idleCooldownTicks = 100 + this.random.nextInt(101);
                ActionId[] idles = {ActionId.IDLE_1, ActionId.IDLE_2, ActionId.IDLE_3};
                this.setActionId(idles[this.random.nextInt(idles.length)]);
            }
            return;
        }

        this.combatTicks++;
        this.setTarget(target);
        this.getLookControl().setLookAt(target, 35.0F, 35.0F);
        double dist = this.distanceTo(target);

        // Ordered Anti-Pillaring (M5 mist_ascent -> O4 frost_wave -> O2 blink_strike)
        double heightDiff = target.getY() - this.getY();
        if (heightDiff >= 3.0D) {
            this.elevatedTargetTicks++;
        } else {
            this.elevatedTargetTicks = 0;
        }
        if (this.elevatedTargetTicks >= 30) {
            this.setLocomotionMode(LocomotionMode.MIST_ASCENT);
            double maxAscentY = this.hasAnchor ? this.anchorY + 6.0D : this.getY() + 6.0D;
            if (this.getY() < Math.min(target.getY(), maxAscentY)) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.16D, this.getDeltaMovement().z);
            }
            if (this.getAttackCooldown(SoulReaperAttack.O4_FROST_WAVE) == 0) {
                this.startAttack(SoulReaperAttack.O4_FROST_WAVE, target);
                return;
            } else if (this.getAttackCooldown(SoulReaperAttack.O2_BLINK_STRIKE) == 0 && this.elevatedTargetTicks >= 60) {
                this.startAttack(SoulReaperAttack.O2_BLINK_STRIKE, target);
                return;
            }
        } else {
            // Select Layer L0 locomotion mode based on distance
            if (dist > 12.0D) {
                this.setLocomotionMode(LocomotionMode.PREDATOR_RUN);
            } else if (dist > 6.5D) {
                this.setLocomotionMode(LocomotionMode.GLIDE);
            } else if (dist > 3.8D) {
                this.setLocomotionMode(LocomotionMode.ORBIT);
            } else {
                this.setLocomotionMode(LocomotionMode.STAFF_STRIDE);
            }
            this.getNavigation().moveTo(target, this.getLocomotionMode() == LocomotionMode.PREDATOR_RUN ? 1.35D : 1.0D);
        }

        // Check tactical Stage 2+ nullifications (A5 Shield Tithe / A6 Arrow Reversal)
        if (this.getStage().ordinal() >= Stage.STAGE_2.ordinal() && target instanceof Player playerTarget) {
            if (this.countRecentShieldBlocks(playerTarget.getUUID()) >= 3 && dist <= 5.0D) {
                this.shieldBlockTicks.remove(playerTarget.getUUID());
                this.startNullification(ActionId.A5_SHIELD_TITHE, playerTarget);
                return;
            }
            if (this.countRecentProjectiles(playerTarget.getUUID()) >= 3) {
                this.projectileShotTicks.remove(playerTarget.getUUID());
                this.startNullification(ActionId.A6_ARROW_REVERSAL, playerTarget);
                return;
            }
        }

        // Choose and start next attack
        SoulReaperAttack chosen = this.chooseNextAttack(level, target, dist);
        if (chosen != null) {
            this.startAttack(chosen, target);
        }
    }

    private @Nullable LivingEntity selectCombatTarget(ServerLevel level) {
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && this.distanceToSqr(current) <= FOLLOW_RANGE * FOLLOW_RANGE) {
            if (!(current instanceof Player p && (p.isCreative() || p.isSpectator()))) {
                return current;
            }
        }
        AABB search = this.getBoundingBox().inflate(FOLLOW_RANGE);
        Player nearestPlayer = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), FOLLOW_RANGE, true);
        if (nearestPlayer != null) {
            return nearestPlayer;
        }
        List<LivingEntity> golems = level.getEntitiesOfClass(
                LivingEntity.class,
                search,
                e -> e.isAlive() && (e instanceof IronGolem || e instanceof SnowGolem));
        return golems.isEmpty() ? null : golems.get(0);
    }

    /**
     * Selects the next available attack respecting stage unlocks, cooldowns, 2-tier anti-siege, and the 3-attack
     * anti-repetition history.
     */
    public @Nullable SoulReaperAttack chooseNextAttack(ServerLevel level, LivingEntity target, double distance) {
        AABB close4 = this.getBoundingBox().inflate(4.0D);
        AABB close6 = this.getBoundingBox().inflate(6.0D);
        int playersWithin4 = level.getEntitiesOfClass(Player.class, close4, p -> p.isAlive() && !p.isSpectator()).size();
        int playersWithin6 = level.getEntitiesOfClass(Player.class, close6, p -> p.isAlive() && !p.isSpectator()).size();

        if (playersWithin6 >= 3) {
            this.siegeTier2Ticks++;
        } else {
            this.siegeTier2Ticks = 0;
        }

        // Anti-siege Tier 2 (3+ players within 6b for >= 10 ticks)
        if (this.siegeTier2Ticks >= 10) {
            if (this.canUseAttack(SoulReaperAttack.F4_GRAVEDIGGER_SLAM)) {
                return SoulReaperAttack.F4_GRAVEDIGGER_SLAM;
            }
            if (this.canUseAttack(SoulReaperAttack.O1_WHIRLWIND)) {
                return SoulReaperAttack.O1_WHIRLWIND;
            }
        }
        // Anti-siege Tier 1 (2+ players within 4b)
        if (playersWithin4 >= 2 && this.canUseAttack(SoulReaperAttack.O1_WHIRLWIND)) {
            return SoulReaperAttack.O1_WHIRLWIND;
        }

        SoulReaperAttack mostRecent = this.recentAttacks.peekLast();
        List<SoulReaperAttack> candidates = new ArrayList<>();
        for (SoulReaperAttack attack : SoulReaperAttack.values()) {
            if (!this.canUseAttack(attack)) {
                continue;
            }
            if (distance > attack.range + 1.5D) {
                continue;
            }
            candidates.add(attack);
        }
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() > 1 && mostRecent != null) {
            candidates.remove(mostRecent);
        }

        // Weighted selection (previous 2 attacks in recentAttacks get 0.25x weight -> weight 1 vs 4)
        int totalWeight = 0;
        int[] weights = new int[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            SoulReaperAttack candidate = candidates.get(i);
            int w = this.recentAttacks.contains(candidate) ? 1 : 4;
            weights[i] = w;
            totalWeight += w;
        }
        int roll = this.random.nextInt(Math.max(1, totalWeight));
        int acc = 0;
        for (int i = 0; i < candidates.size(); i++) {
            acc += weights[i];
            if (roll < acc) {
                return candidates.get(i);
            }
        }
        return candidates.get(0);
    }

    public boolean canUseAttack(SoulReaperAttack attack) {
        return this.getStage().ordinal() >= attack.minStage.ordinal()
                && this.attackCooldowns[attack.ordinal()] <= 0;
    }

    // ================================================================ 4-Phase Attack State Machine

    /** Starts the 4-phase execution of {@code attack} locked onto {@code target}. */
    public void startAttack(SoulReaperAttack attack, @Nullable LivingEntity target) {
        this.currentAttack = attack;
        this.currentSequenceId++;
        this.currentPulseIndex = 0;
        this.currentAttackLandedHit = false;
        this.mercyRecords.clear();
        this.harvestDrainThisCast.clear();
        this.harvestStartedAboveHalf.clear();
        this.breathEligibleForSequence.clear();

        if (this.level() instanceof ServerLevel serverLevel) {
            AABB arena = this.getBoundingBox().inflate(FOLLOW_RANGE);
            for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, arena, LivingEntity::isAlive)) {
                if (entity.getHealth() >= entity.getMaxHealth() * BREATH_RULE_THRESHOLD
                        && !this.breathSpentUntilReheal.contains(entity.getUUID())) {
                    this.breathEligibleForSequence.add(entity.getUUID());
                }
                if (entity.getHealth() >= entity.getMaxHealth() * 0.50F) {
                    this.harvestStartedAboveHalf.add(entity.getUUID());
                }
            }
        }

        if (target != null) {
            this.setTarget(target);
            this.setLockedTargetId(target.getId());
            this.faceTarget(target);
        } else {
            this.setLockedTargetId(-1);
        }

        this.recentAttacks.addLast(attack);
        while (this.recentAttacks.size() > 3) {
            this.recentAttacks.removeFirst();
        }

        this.setActionId(attack.actionId);
        this.setState(State.ATTACK_PREP);
    }

    public int getEffectivePrepTicks(SoulReaperAttack attack) {
        float mult = 1.0F;
        if (this.getStage() == Stage.STAGE_3 && attack.isFrontal()) {
            mult *= 0.85F;
        }
        if (this.combatTicks >= 12000) { // Enrage Stage 3 (10 min+)
            mult = Math.max(0.75F, mult - 0.10F);
        }
        return Math.max(4, Math.round(attack.prepTicks * mult));
    }

    public int getEffectiveRecoveryTicks(SoulReaperAttack attack, boolean whiffed) {
        float scale = Math.max(0.70F, 1.0F - 0.03F * (this.getPeakPlayers() - 1));
        int base = Math.max(6, Math.round(attack.recoveryTicks * scale));
        return whiffed ? base + SoulReaperAttack.WHIFF_PENALTY_TICKS : base;
    }

    private void tickAttackPrep(ServerLevel level) {
        this.getNavigation().stop();
        SoulReaperAttack attack = this.currentAttack;
        if (attack == null) {
            this.setState(State.COMBAT_IDLE);
            return;
        }
        LivingEntity locked = this.resolveLockedTarget(level);
        if (locked != null) {
            this.faceTarget(locked);
        }
        if (this.stateTicks >= this.getEffectivePrepTicks(attack)) {
            if (attack == SoulReaperAttack.O2_BLINK_STRIKE && locked != null) {
                Vec3 look = locked.getLookAngle();
                Vec3 behind = locked.position().subtract(look.x * 2.2D, 0.0D, look.z * 2.2D);
                this.blinkToPosition(level, behind);
                this.faceTarget(locked);
            }
            this.setState(State.ATTACK_EXEC);
        }
    }

    private void tickAttackExec(ServerLevel level) {
        this.getNavigation().stop();
        SoulReaperAttack attack = this.currentAttack;
        if (attack == null) {
            this.setState(State.COMBAT_IDLE);
            return;
        }
        int pulseIdx = attack.pulseIndexForTick(this.stateTicks);
        if (pulseIdx >= 0) {
            this.currentPulseIndex = pulseIdx;
            if (attack == SoulReaperAttack.F6_THREEFOLD_HARVEST) {
                this.setPos(
                        this.getX() + this.lockedForward.x * 1.0D,
                        this.getY(),
                        this.getZ() + this.lockedForward.z * 1.0D);
            } else if (attack == SoulReaperAttack.O5_REAPING_DASH) {
                this.setPos(
                        this.getX() + this.lockedForward.x * 3.5D,
                        this.getY(),
                        this.getZ() + this.lockedForward.z * 3.5D);
            }
            int hits = this.executeAttackImpact(level, attack, pulseIdx);
            if (hits > 0) {
                this.currentAttackLandedHit = true;
            }
        }
        if (this.stateTicks >= attack.execTicks) {
            this.setState(State.ATTACK_RECOVERY);
        }
    }

    private void tickAttackRecovery(ServerLevel level) {
        this.getNavigation().stop();
        SoulReaperAttack attack = this.currentAttack;
        if (attack == null) {
            this.setState(State.COMBAT_IDLE);
            return;
        }
        int needed = this.getEffectiveRecoveryTicks(attack, !this.currentAttackLandedHit);
        if (this.stateTicks >= needed) {
            this.attackCooldowns[attack.ordinal()] = attack.cooldownTicks;
            this.currentAttack = null;
            this.setActionId(ActionId.IDLE_1);
            this.setState(State.COMBAT_IDLE);
        }
    }

    /**
     * Executes a single impact pulse of {@code attack} on the server and returns the number of targets hit.
     */
    public int executeAttackImpact(ServerLevel level, SoulReaperAttack attack, int pulseIdx) {
        float baseDamage = attack.pulseDamages[Math.min(pulseIdx, attack.pulseDamages.length - 1)];
        float scaledDamage = baseDamage * this.getDamageMultiplier();
        AABB search = this.getBoundingBox().inflate(attack.range + 2.0D);
        List<LivingEntity> candidates = level.getEntitiesOfClass(
                LivingEntity.class,
                search,
                e -> e != this && e.isAlive() && !(e instanceof WanderingSoulWisp) && !(e instanceof OmenHare));

        int hits = 0;
        switch (attack) {
            case O1_WHIRLWIND -> {
                for (LivingEntity target : candidates) {
                    if (this.distanceTo(target) <= attack.range + target.getBbWidth() * 0.5D) {
                        if (target instanceof Player player) {
                            disablePlayerShield(level, player, 60);
                        }
                        if (this.applyStrikeToTarget(level, target, scaledDamage, this.currentSequenceId, pulseIdx)) {
                            hits++;
                        }
                    }
                }
            }
            case O2_BLINK_STRIKE -> {
                for (LivingEntity target : candidates) {
                    if (SoulReaperAttack.isInFrontArc(this.position(), this.lockedForward, target, 3.5D, 160.0D)) {
                        if (this.applyStrikeToTarget(level, target, scaledDamage, this.currentSequenceId, pulseIdx)) {
                            hits++;
                        }
                    }
                }
            }
            case O3_SOUL_HARVEST -> {
                level.broadcastEntityEvent(this, EVENT_HARVEST_PULSE);
                float maxHealBudget = 24.0F + 6.0F * (this.getPeakPlayers() - 1);
                float healPerPulse = maxHealBudget / 4.0F;
                int maxTargets = Math.min(Math.max(1, this.getPeakPlayers()), 4);
                int drainedCount = 0;
                for (LivingEntity target : candidates) {
                    if (drainedCount >= maxTargets) {
                        break;
                    }
                    if (this.distanceTo(target) <= attack.range && this.hasLineOfSight(target)) {
                        float drained = this.applyHarvestDrainPulse(level, target, scaledDamage, pulseIdx);
                        if (drained > 0.0F) {
                            drainedCount++;
                            hits++;
                        }
                    }
                }
                // Also drain surviving Wandering Soul Wisps on the final pulse
                this.activeWisps.removeIf(uuid -> {
                    Entity e = level.getEntity(uuid);
                    return e == null || !e.isAlive();
                });
                if (hits > 0 || !this.activeWisps.isEmpty()) {
                    float wispFactor = this.getStage().ordinal() >= Stage.STAGE_2.ordinal()
                            ? Math.min(1.0F, (drainedCount + this.activeWisps.size()) / 4.0F)
                            : 1.0F;
                    this.heal(healPerPulse * Math.max(0.25F, wispFactor));
                }
            }
            case O4_FROST_WAVE -> {
                level.broadcastEntityEvent(this, EVENT_FROST_WAVE);
                for (LivingEntity target : candidates) {
                    if (SoulReaperAttack.isInLinearCorridor(this.position(), this.lockedForward, target, 20.0D, 0.9D)) {
                        if (this.applyStrikeToTarget(level, target, scaledDamage, this.currentSequenceId, pulseIdx)) {
                            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 0));
                            hits++;
                        }
                    }
                }
            }
            case O5_REAPING_DASH -> {
                for (LivingEntity target : candidates) {
                    if (SoulReaperAttack.isInLinearCorridor(this.position(), this.lockedForward, target, 4.5D, 1.25D)) {
                        if (this.applyStrikeToTarget(level, target, scaledDamage, this.currentSequenceId, pulseIdx)) {
                            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 0));
                            hits++;
                        }
                    }
                }
            }
            case F1_RISING_CRESCENT -> {
                for (LivingEntity target : candidates) {
                    if (SoulReaperAttack.isInFrontArc(this.position(), this.lockedForward, target, 6.0D, 140.0D)) {
                        if (target instanceof Player player) {
                            disablePlayerShield(level, player, 40);
                        }
                        if (this.applyStrikeToTarget(level, target, scaledDamage, this.currentSequenceId, pulseIdx)) {
                            this.applySafeKnockback(
                                    level,
                                    target,
                                    new Vec3(this.lockedForward.x * 0.4D, 0.55D, this.lockedForward.z * 0.4D));
                            hits++;
                        }
                    }
                }
            }
            case F2_HAFT_THRUST -> {
                for (LivingEntity target : candidates) {
                    if (SoulReaperAttack.isInLinearCorridor(this.position(), this.lockedForward, target, 7.0D, 0.75D)) {
                        if (target instanceof Player player) {
                            disablePlayerShield(level, player, 40);
                        }
                        if (this.applyStrikeToTarget(level, target, scaledDamage, this.currentSequenceId, pulseIdx)) {
                            this.applySafeKnockback(
                                    level,
                                    target,
                                    new Vec3(this.lockedForward.x * 1.35D, 0.25D, this.lockedForward.z * 1.35D));
                            hits++;
                        }
                    }
                }
            }
            case F3_CROSS_REAP, F6_THREEFOLD_HARVEST -> {
                double arc = attack == SoulReaperAttack.F3_CROSS_REAP ? 160.0D : 150.0D;
                for (LivingEntity target : candidates) {
                    if (SoulReaperAttack.isInFrontArc(this.position(), this.lockedForward, target, 5.5D, arc)) {
                        if (this.applyStrikeToTarget(level, target, scaledDamage, this.currentSequenceId, pulseIdx)) {
                            hits++;
                        }
                    }
                }
            }
            case F4_GRAVEDIGGER_SLAM -> {
                level.broadcastEntityEvent(this, EVENT_HEAVY_SLAM);
                Set<UUID> directHitTargets = new HashSet<>();
                // Zone 1: Direct impact (5.0b, 20 base damage)
                for (LivingEntity target : candidates) {
                    if (SoulReaperAttack.isInFrontArc(
                            this.position(),
                            this.lockedForward,
                            target,
                            SoulReaperAttack.F4_DIRECT_RADIUS,
                            120.0D)) {
                        directHitTargets.add(target.getUUID());
                        if (this.applyStrikeToTarget(level, target, scaledDamage, this.currentSequenceId, pulseIdx)) {
                            hits++;
                        }
                    }
                }
                // Zone 2: Outer cone (8.0b, 8 base damage) — strictly mutually exclusive with direct hit (Rule 5)!
                float coneDamage = SoulReaperAttack.F4_CONE_DAMAGE * this.getDamageMultiplier();
                for (LivingEntity target : candidates) {
                    if (directHitTargets.contains(target.getUUID())) {
                        continue;
                    }
                    if (SoulReaperAttack.isInFrontArc(this.position(), this.lockedForward, target, 8.0D, 140.0D)) {
                        if (this.applyStrikeToTarget(level, target, coneDamage, this.currentSequenceId, pulseIdx)) {
                            hits++;
                        }
                    }
                }
            }
            case F5_LOW_SWEEP -> {
                for (LivingEntity target : candidates) {
                    if (Math.abs(target.getY() - this.getY()) <= 0.8D
                            && SoulReaperAttack.isInFrontArc(this.position(), this.lockedForward, target, 6.5D, 200.0D)) {
                        if (this.applyStrikeToTarget(level, target, scaledDamage, this.currentSequenceId, pulseIdx)) {
                            if (target.onGround()) {
                                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0));
                            }
                            hits++;
                        }
                    }
                }
            }
        }
        return hits;
    }

    public float getDamageMultiplier() {
        float mult = 1.0F + 0.03F * (this.getPeakPlayers() - 1);
        if (this.combatTicks >= 9600) { // Enrage Stage 2 (8 min+)
            mult *= 1.10F;
        }
        return mult;
    }

    // ================================================================ 7 Anti-Instakill Rules

    /**
     * Applies a boss strike to {@code target} enforcing Rules 1, 2, and 3 (6-tick mercy window aware of
     * {@code (sequenceId, pulseIndex)}, Breath Rule at >= 80% HP, and 65% post-armor cap).
     */
    public boolean applyStrikeToTarget(
            ServerLevel level,
            LivingEntity target,
            float nominalDamage,
            int sequenceId,
            int pulseIndex) {
        UUID uuid = target.getUUID();
        MercyRecord prev = this.mercyRecords.get(uuid);
        if (prev != null) {
            boolean samePulse = prev.sequenceId() == sequenceId && prev.pulseIndex() == pulseIndex;
            boolean withinWindow = (this.serverTicks - prev.tick()) < MERCY_WINDOW_TICKS;
            if (samePulse || (withinWindow && prev.sequenceId() != sequenceId)) {
                return false;
            }
        }
        this.mercyRecords.put(uuid, new MercyRecord(this.serverTicks, sequenceId, pulseIndex));

        this.activeStrikeContext = true;
        try {
            target.invulnerableTime = 0;
            return target.hurtServer(level, this.damageSources().mobAttack(this), nominalDamage);
        } finally {
            this.activeStrikeContext = false;
        }
    }

    /**
     * Enforces Rule 1 (post-armor 65% maxHealth cap), Rule 3 (Breath Rule >= 80% HP -> floor of 1.0 HP), and Rule 7
     * (Instant-heal toll -> floor of 2.0 HP) inside {@code LivingDamageEvent.Pre}.
     */
    public float clampOutgoingDamagePostArmor(LivingEntity victim, float postArmorDamage) {
        if (this.activeInstantTollContext) {
            float maxToll = Math.max(0.0F, victim.getHealth() - TOLL_MIN_HEALTH_FLOOR);
            if (postArmorDamage > maxToll && victim.level() instanceof ServerLevel serverLevel) {
                this.speakCategory(serverLevel, "toll_spared", 1);
            }
            return Math.min(postArmorDamage, maxToll);
        }

        // Rule 1: Post-armor 65% maxHealth ceiling
        float capped = Math.min(postArmorDamage, victim.getMaxHealth() * POST_ARMOR_CAP_RATIO);

        // Rule 3: Breath Rule (if victim started sequence at >= 80% HP, cannot drop below 1.0 HP)
        UUID uuid = victim.getUUID();
        if (this.breathEligibleForSequence.contains(uuid) && !this.breathSpentUntilReheal.contains(uuid)) {
            float maxBeforeOneHp = Math.max(0.0F, victim.getHealth() - 1.0F);
            if (capped >= maxBeforeOneHp) {
                capped = maxBeforeOneHp;
                this.breathSpentUntilReheal.add(uuid);
            }
        }
        return capped;
    }

    /**
     * Enforces Rule 4: Soul Harvest (O3) drains at most 60% of maxHealth across all 4 pulses and never drops a target
     * below 1.0 HP if they started the cast above 50% HP.
     */
    private float applyHarvestDrainPulse(ServerLevel level, LivingEntity target, float pulseDamage, int pulseIdx) {
        UUID uuid = target.getUUID();
        float alreadyDrained = this.harvestDrainThisCast.getOrDefault(uuid, 0.0F);
        float maxTotalDrain = target.getMaxHealth() * HARVEST_MAX_DRAIN_RATIO;
        float allowed = Math.max(0.0F, maxTotalDrain - alreadyDrained);
        float dmg = Math.min(pulseDamage, allowed);
        if (this.harvestStartedAboveHalf.contains(uuid)) {
            dmg = Math.min(dmg, Math.max(0.0F, target.getHealth() - 1.0F));
        }
        if (dmg <= 0.0F) {
            return 0.0F;
        }
        this.harvestDrainThisCast.put(uuid, alreadyDrained + dmg);
        this.applyStrikeToTarget(level, target, dmg, this.currentSequenceId, pulseIdx);
        return dmg;
    }

    /**
     * Enforces Rule 6: vertical knockback capped at 1.2 blocks (0.6 velocity) and horizontal knockback cancelled if it
     * would push the target into lava or a >8 block drop within 2 blocks.
     */
    public void applySafeKnockback(ServerLevel level, LivingEntity target, Vec3 push) {
        double clampedY = Math.min(0.6D, push.y);
        Vec3 horizontal = new Vec3(push.x, 0.0D, push.z);
        if (horizontal.lengthSqr() > 1.0E-4D && !isSafeKnockbackDestination(level, target.blockPosition(), horizontal)) {
            horizontal = Vec3.ZERO;
        }
        target.setDeltaMovement(horizontal.x, clampedY, horizontal.z);
        target.hurtMarked = true;
    }

    public static boolean isSafeKnockbackDestination(ServerLevel level, BlockPos origin, Vec3 horizontalPush) {
        Vec3 dir = horizontalPush.normalize();
        for (int step = 1; step <= 2; step++) {
            int checkX = origin.getX() + (int) Math.round(dir.x * step);
            int checkZ = origin.getZ() + (int) Math.round(dir.z * step);
            BlockPos checkPos = new BlockPos(checkX, origin.getY(), checkZ);
            if (level.getBlockState(checkPos).is(Blocks.LAVA)
                    || level.getBlockState(checkPos.below()).is(Blocks.LAVA)) {
                return false;
            }
            boolean foundGround = false;
            for (int dy = 0; dy <= 8; dy++) {
                BlockPos below = checkPos.below(dy);
                BlockState state = level.getBlockState(below);
                if (state.is(Blocks.LAVA)) {
                    return false;
                }
                if (!state.isAir() && state.blocksMotion()) {
                    foundGround = true;
                    break;
                }
            }
            if (!foundGround) {
                return false;
            }
        }
        return true;
    }

    // ================================================================ Dodges (E1-E4), Taunts (P1-P5), Nullifications (A1-A6)

    public void startDodge(ActionId dodgeAction) {
        this.setActionId(dodgeAction);
        this.setState(State.DODGING);
        if (this.level() instanceof ServerLevel serverLevel) {
            if (dodgeAction == ActionId.E3_SNOW_DISSOLVE) {
                serverLevel.broadcastEntityEvent(this, EVENT_SNOW_DISSOLVE);
            }
            this.speakCategory(serverLevel, "dodge", 3);
        }
    }

    private void tickDodging(ServerLevel level) {
        int duration = switch (this.getActionId()) {
            case E1_SIDESTEP_GLIDE -> 12;
            case E2_PHANTOM_RETREAT -> 15;
            default -> 18;
        };
        if (this.stateTicks >= duration) {
            this.setActionId(ActionId.IDLE_1);
            this.setState(State.COMBAT_IDLE);
        }
    }

    public void startTaunt(ActionId tauntAction, boolean enableBait) {
        this.tauntBaitEnabled = enableBait;
        this.setActionId(tauntAction);
        this.setState(State.TAUNTING);
    }

    private void tickTaunting(ServerLevel level) {
        ActionId action = this.getActionId();
        if (this.tauntBaitEnabled) {
            LivingEntity target = this.selectCombatTarget(level);
            if (action == ActionId.P1_BECKON && this.stateTicks == 28) {
                this.startAttack(SoulReaperAttack.F2_HAFT_THRUST, target);
                return;
            } else if (action == ActionId.P2_FUNERAL_BOW && this.stateTicks == 38) {
                this.startAttack(SoulReaperAttack.F3_CROSS_REAP, target);
                return;
            } else if (action == ActionId.P3_HOURGLASS_COUNT && this.stateTicks == 54) {
                this.startAttack(SoulReaperAttack.O4_FROST_WAVE, target);
                return;
            } else if (action == ActionId.P4_BLADE_HONING && this.stateTicks == 36) {
                this.startAttack(SoulReaperAttack.F4_GRAVEDIGGER_SLAM, target);
                return;
            }
        }
        int totalTicks = switch (action) {
            case P1_BECKON -> 36;
            case P2_FUNERAL_BOW -> 50;
            case P3_HOURGLASS_COUNT -> 64;
            case P4_BLADE_HONING -> 44;
            default -> 40;
        };
        if (this.stateTicks >= totalTicks) {
            this.setActionId(ActionId.IDLE_1);
            this.setState(State.COMBAT_IDLE);
        }
    }

    public void startNullification(ActionId nullifyAction, LivingEntity target) {
        this.nullifyTargetUuid = target.getUUID();
        this.arrowReflectedThisCast = false;
        this.setLockedTargetId(target.getId());
        this.faceTarget(target);
        this.setActionId(nullifyAction);
        this.setState(State.NULLIFYING);
        if (this.level() instanceof ServerLevel serverLevel) {
            if (nullifyAction == ActionId.A5_SHIELD_TITHE) {
                this.speakCategory(serverLevel, "shield", 3);
            } else if (nullifyAction == ActionId.A6_ARROW_REVERSAL) {
                this.speakCategory(serverLevel, "arrow", 3);
            }
        }
    }

    private void tickNullifying(ServerLevel level) {
        ActionId action = this.getActionId();
        if (action == ActionId.A5_SHIELD_TITHE && this.stateTicks == 16 && this.nullifyTargetUuid != null) {
            Entity e = level.getEntity(this.nullifyTargetUuid);
            if (e instanceof Player player && player.isAlive() && this.distanceTo(player) <= 4.5D && player.isBlocking()) {
                disablePlayerShield(level, player, 30);
                level.broadcastEntityEvent(this, EVENT_SHIELD_TITHE);
            }
        } else if (action == ActionId.A6_ARROW_REVERSAL && this.stateTicks >= 20 && this.stateTicks <= 30 && !this.arrowReflectedThisCast) {
            AABB frontBox = this.getBoundingBox().inflate(3.5D);
            List<Projectile> projectiles = level.getEntitiesOfClass(Projectile.class, frontBox, Projectile::isAlive);
            if (!projectiles.isEmpty()) {
                Projectile proj = projectiles.get(0);
                Vec3 rev = proj.getDeltaMovement().scale(-1.2D);
                proj.setDeltaMovement(rev);
                proj.setOwner(this);
                this.arrowReflectedThisCast = true;
                level.broadcastEntityEvent(this, EVENT_ARROW_REVERSAL);
            }
        }

        int duration = switch (action) {
            case A5_SHIELD_TITHE -> 24;
            case A6_ARROW_REVERSAL -> 30;
            case A1_VIAL_SILENCE -> 36;
            case A2_APPLE_TOLL -> 44;
            case A3_GREATER_TOLL -> 60;
            default -> 30;
        };
        if (this.stateTicks >= duration) {
            this.setActionId(ActionId.IDLE_1);
            this.setState(State.COMBAT_IDLE);
        }
    }

    /**
     * Records a pre-consumption snapshot of {@code entity}'s active effects and health (for provenance-based
     * nullification A1-A3).
     */
    public void recordPreConsumeSnapshot(LivingEntity entity) {
        Map<Holder<MobEffect>, EffectSnapshot> map = new HashMap<>();
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            map.put(inst.getEffect(), new EffectSnapshot(inst.getEffect(), inst.getAmplifier(), inst.getDuration()));
        }
        this.preConsumeSnapshots.put(entity.getUUID(), new PreConsumeSnapshot(this.serverTicks, entity.getHealth(), map));
    }

    /**
     * Called when {@code entity} within 48 blocks finishes drinking a potion or eating a Golden/Enchanted Golden Apple.
     * Computes the exact delta against {@code preConsumeSnapshots}, emits the immediate {@code T0} visual warning
     * (L2 animation if free, or L3 {@code A4_FLICK_SNUFF} overlay if busy), and schedules resolution at {@code T0 + 20t}
     * ({@code T0 + 8t} in Stage 3).
     */
    public void onConsumableFinished(ServerLevel level, LivingEntity entity, ItemStack item) {
        ActionId categoryAction;
        String voiceCat;
        if (item.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            categoryAction = ActionId.A3_GREATER_TOLL;
            voiceCat = "enchanted_apple";
        } else if (item.is(Items.GOLDEN_APPLE)) {
            categoryAction = ActionId.A2_APPLE_TOLL;
            voiceCat = "apple";
        } else if (item.is(Items.POTION)) {
            categoryAction = ActionId.A1_VIAL_SILENCE;
            voiceCat = "potion";
        } else {
            return;
        }

        PreConsumeSnapshot pre = this.preConsumeSnapshots.remove(entity.getUUID());
        Map<Holder<MobEffect>, EffectSnapshot> preMap = pre != null ? pre.effects() : Map.of();
        float preHealth = pre != null ? pre.health() : entity.getHealth();
        float instantHeal = Math.max(0.0F, entity.getHealth() - preHealth);

        List<Holder<MobEffect>> newlyAdded = new ArrayList<>();
        List<EffectSnapshot> upgradedPreExisting = new ArrayList<>();

        for (MobEffectInstance current : entity.getActiveEffects()) {
            EffectSnapshot prevSnap = preMap.get(current.getEffect());
            if (prevSnap == null) {
                newlyAdded.add(current.getEffect());
            } else if (current.getAmplifier() > prevSnap.amplifier()
                    || current.getDuration() > prevSnap.duration() + 5) {
                upgradedPreExisting.add(prevSnap);
            }
        }

        int delay = this.getStage() == Stage.STAGE_3 ? NULLIFY_DELAY_STAGE_3 : NULLIFY_DELAY_NORMAL;
        this.pendingNullifications.add(new PendingNullification(
                entity.getUUID(),
                this.serverTicks + delay,
                categoryAction,
                newlyAdded,
                upgradedPreExisting,
                instantHeal));

        // T0 mandatory visual warning: full L2 animation if in COMBAT_IDLE/TAUNTING, or L3 A4_FLICK_SNUFF if busy!
        if (this.getState() == State.COMBAT_IDLE || this.getState() == State.TAUNTING) {
            this.startNullification(categoryAction, entity);
            level.broadcastEntityEvent(this, EVENT_NULLIFY_BEAM);
        } else {
            this.setReactId(ReactId.A4_FLICK_SNUFF);
            level.broadcastEntityEvent(this, EVENT_FLICK_SNUFF);
        }

        if (this.pendingNullifications.size() >= 2) {
            this.speakCategory(level, "multi_consume", 1);
        } else {
            this.speakCategory(level, voiceCat, 3);
        }
    }

    private void tickPendingNullifications(ServerLevel level) {
        if (this.pendingNullifications.isEmpty()) {
            return;
        }
        Iterator<PendingNullification> it = this.pendingNullifications.iterator();
        while (it.hasNext()) {
            PendingNullification pending = it.next();
            if (this.serverTicks < pending.resolveTick()) {
                continue;
            }
            it.remove();
            Entity targetEntity = level.getEntity(pending.targetUuid());
            if (!(targetEntity instanceof LivingEntity target) || !target.isAlive()) {
                continue;
            }
            for (Holder<MobEffect> added : pending.newlyAddedEffects()) {
                target.removeEffect(added);
            }
            for (EffectSnapshot prev : pending.upgradedPreExistingEffects()) {
                target.removeEffect(prev.effect());
                target.addEffect(new MobEffectInstance(prev.effect(), prev.duration(), prev.amplifier()));
            }
            if (pending.instantHealToll() > 0.0F) {
                this.activeInstantTollContext = true;
                try {
                    target.invulnerableTime = 0;
                    float safeToll = Math.min(
                            pending.instantHealToll(),
                            Math.max(0.0F, target.getHealth() - TOLL_MIN_HEALTH_FLOOR));
                    if (safeToll > 0.0F) {
                        target.hurtServer(level, this.damageSources().indirectMagic(this, this), safeToll);
                    }
                    if (pending.instantHealToll() > safeToll) {
                        this.speakCategory(level, "toll_spared", 1);
                    }
                } finally {
                    this.activeInstantTollContext = false;
                }
            }
        }
    }

    public void recordShieldBlock(UUID playerUuid) {
        List<Integer> list = this.shieldBlockTicks.computeIfAbsent(playerUuid, k -> new ArrayList<>());
        list.removeIf(t -> this.serverTicks - t > 80);
        list.add(this.serverTicks);
    }

    public int countRecentShieldBlocks(UUID playerUuid) {
        List<Integer> list = this.shieldBlockTicks.get(playerUuid);
        if (list == null) {
            return 0;
        }
        list.removeIf(t -> this.serverTicks - t > 80);
        return list.size();
    }

    public void recordProjectileShot(UUID playerUuid) {
        List<Integer> list = this.projectileShotTicks.computeIfAbsent(playerUuid, k -> new ArrayList<>());
        list.removeIf(t -> this.serverTicks - t > 80);
        list.add(this.serverTicks);
    }

    public int countRecentProjectiles(UUID playerUuid) {
        List<Integer> list = this.projectileShotTicks.get(playerUuid);
        if (list == null) {
            return 0;
        }
        list.removeIf(t -> this.serverTicks - t > 80);
        return list.size();
    }

    public static boolean disablePlayerShield(ServerLevel level, Player player, int cooldownTicks) {
        ItemStack shield = player.getUseItem().is(Items.SHIELD) ? player.getUseItem()
                : player.getOffhandItem().is(Items.SHIELD) ? player.getOffhandItem()
                : player.getMainHandItem().is(Items.SHIELD) ? player.getMainHandItem() : ItemStack.EMPTY;
        if (shield.isEmpty()) {
            return false;
        }
        player.getCooldowns().addCooldown(shield, cooldownTicks);
        if (player.isUsingItem() && player.getUseItem() == shield) {
            player.stopUsingItem();
        }
        level.broadcastEntityEvent(player, (byte) 30);
        level.playSound(null, player.blockPosition(), SoundEvents.SHIELD_BREAK.value(), SoundSource.PLAYERS, 1.1F, 0.85F);
        return true;
    }

    // ================================================================ Helpers & Dialogue

    private @Nullable LivingEntity resolveLockedTarget(ServerLevel level) {
        int id = this.getLockedTargetId();
        if (id >= 0) {
            Entity e = level.getEntity(id);
            if (e instanceof LivingEntity living && living.isAlive()) {
                return living;
            }
        }
        return this.getTarget();
    }

    private void faceTarget(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len > 1.0E-4D) {
            this.lockedForward = new Vec3(dx / len, 0.0D, dz / len);
            float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            this.yHeadRot = yaw;
        }
    }

    public void blinkToPosition(ServerLevel level, Vec3 destination) {
        level.broadcastEntityEvent(this, EVENT_BLINK_SMOKE);
        this.teleportTo(destination.x, destination.y, destination.z);
        level.broadcastEntityEvent(this, EVENT_BLINK_SMOKE);
        level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.2F, 0.6F);
    }

    public boolean speakCategory(ServerLevel level, String category, int count) {
        int index = 1 + this.random.nextInt(Math.max(1, count));
        String key = "entity.codenamecoraline.soul_reaper.line." + category + "." + index;
        return this.speakSpecificLine(level, category, key);
    }

    public boolean speakSpecificLine(ServerLevel level, String category, String translationKey) {
        if (this.serverTicks - this.lastVoiceGlobalTick < 100) {
            return false;
        }
        int lastCat = this.lastVoiceCategoryTick.getOrDefault(category, -1000);
        if (this.serverTicks - lastCat < 600) {
            return false;
        }
        this.lastVoiceGlobalTick = this.serverTicks;
        this.lastVoiceCategoryTick.put(category, this.serverTicks);
        Component msg = Component.translatable(translationKey);
        for (ServerPlayer player : level.getEntitiesOfClass(
                ServerPlayer.class, this.getBoundingBox().inflate(FOLLOW_RANGE), ServerPlayer::isAlive)) {
            player.sendSystemMessage(msg);
        }
        return true;
    }

    // ================================================================ Immunities, Damage & Persistence

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        var holder = effect.getEffect();
        if (holder.is(MobEffects.POISON)
                || holder.is(MobEffects.WITHER)
                || holder.is(MobEffects.REGENERATION)
                || holder.is(MobEffects.SLOWNESS)) {
            return false;
        }
        return super.canBeAffected(effect);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.FREEZE) || source.is(DamageTypes.FALL) || source.is(DamageTypes.DROWN)) {
            return false;
        }
        if (this.getState() == State.AWAKENING || this.getState() == State.PHASE_TRANSITION) {
            return false;
        }
        if (source.getDirectEntity() instanceof Projectile && source.getEntity() instanceof Player shooter) {
            this.recordProjectileShot(shooter.getUUID());
        }
        float capped = Math.min(amount, this.getMaxHealth() * 0.04F);
        boolean hurt = super.hurtServer(level, source, capped);
        if (hurt && this.isAlive()) {
            this.setReactId(ReactId.HURT);
            this.checkStageProgression(level);
        }
        return hurt;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.SOUL_REAPER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.SOUL_REAPER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SOUL_REAPER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.SOUL_REAPER_STEP.get(), 0.7F, 0.9F);
    }

    @Override
    protected void tickDeath() {
        if (this.deathTicks == 0) {
            this.setState(State.DYING);
            this.setActionId(ActionId.DEATH);
            if (this.level() instanceof ServerLevel serverLevel) {
                this.speakCategory(serverLevel, "death", 3);
            }
        }
        this.deathTicks++;
        if (this.deathTicks >= DEATH_DURATION_TICKS && this.level() instanceof ServerLevel serverLevel) {
            this.bossEvent.removeAllPlayers();
            BossSpirit.rise(serverLevel, ModEntities.SOUL_REAPER_SPIRIT.get(), this);
            serverLevel.broadcastEntityEvent(this, EVENT_DEATH_SHATTER);
            serverLevel.broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString(TAG_STATE, this.getState().name());
        output.putString(TAG_STAGE, this.getStage().name());
        output.putInt(TAG_PEAK_PLAYERS, this.getPeakPlayers());
        if (this.hasAnchor) {
            output.putDouble(TAG_ANCHOR + "X", this.anchorX);
            output.putDouble(TAG_ANCHOR + "Y", this.anchorY);
            output.putDouble(TAG_ANCHOR + "Z", this.anchorZ);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        Stage loadedStage = parseEnum(Stage.class, input.getStringOr(TAG_STAGE, Stage.STAGE_1.name()), Stage.STAGE_1);
        this.entityData.set(DATA_STAGE, loadedStage.ordinal());
        this.bossEvent.setColor(loadedStage.barColor);
        this.updatePeakPlayers(input.getIntOr(TAG_PEAK_PLAYERS, 1));
        double ax = input.getDoubleOr(TAG_ANCHOR + "X", Double.NaN);
        if (!Double.isNaN(ax)) {
            this.hasAnchor = true;
            this.anchorX = ax;
            this.anchorY = input.getDoubleOr(TAG_ANCHOR + "Y", this.getY());
            this.anchorZ = input.getDoubleOr(TAG_ANCHOR + "Z", this.getZ());
        }
        State loadedState = parseEnum(State.class, input.getStringOr(TAG_STATE, State.COMBAT_IDLE.name()), State.COMBAT_IDLE);
        this.setState(loadedState == State.DYING ? State.DYING : State.COMBAT_IDLE);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String name, E fallback) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}
