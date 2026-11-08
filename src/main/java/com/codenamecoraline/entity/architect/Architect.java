package com.codenamecoraline.entity.architect;

import com.codenamecoraline.entity.architect.ArchitectStrikes.Strike;
import com.codenamecoraline.registry.ModSounds;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Architect, Kandiaru ({@code codenamecoraline:architect}): a false stone deity that waits as the Statue of the
 * Commandments and wakes into a six-armed duelist. Spec: {@code docs/design/architect.md}.
 *
 * <p>The server is authoritative. {@link #tick()} runs the timelines (reveal, attacks, parry, stagger, phase change,
 * backstep), so they also run for a statue spawned without AI in the GameTests; {@link #customServerAiStep} only makes
 * decisions (target, attack choice, chase, guard, rest). The client only reads the synchronized state and clip.</p>
 */
public final class Architect extends Monster {
    /**
     * Reform scale: the body grew from 3.35 to 4.7 blocks (x1.4; the Last Gift Collector, the tallest boss, is 4.5). The
     * volumes, ranges and heights of the spec are written for the old body and pass through this factor
     * ({@link ArchitectStrikes}, the range constants below, the rig in tools/generate_architect_assets.py).
     */
    public static final float SCALE = 1.4F;
    public static final float WIDTH = 1.9F;
    public static final float HEIGHT = 4.7F;
    public static final float EYE_HEIGHT = 4.1F;
    /** Height of the sternum seal above his feet, where the mana pulses come from. */
    public static final double STERNUM_Y = 2.3D * SCALE;

    public static final double MAX_HEALTH = 480.0D;
    public static final double ARMOR = 16.0D;
    public static final double ARMOR_TOUGHNESS = 8.0D;
    /**
     * Reform (spec §23, balance in design/architect.md §27): 12 -> 20. Against full Netherite with Protection IV (armor 20,
     * toughness 12, 64 % from the enchantment) a 12-point blow did about 1 HP per cut and the fight had no teeth; at 20 plus
     * the mana rend below a cut costs that player about 2 HP and a whole Sixfold Edict about 8.
     */
    public static final double BASE_DAMAGE = 20.0D;
    /** Share of every blow that arrives as mana (indirect magic): it ignores armor but not the Protection enchantments. */
    public static final float MANA_REND = 0.25F;
    /** One blow never takes more than half of a victim's maximum health (no deleting a lightly armored player in a combo). */
    public static final float BLOW_HEALTH_CAP = 0.5F;
    /** Damage multiplier by Phase ordinal: the Administrator hits 10 % harder and the Collapse 20 %. */
    public static final float[] PHASE_DAMAGE = {1.0F, 1.0F, 1.1F, 1.2F};
    public static final double BASE_SPEED = 0.30D;
    public static final double KNOCKBACK_RESISTANCE = 0.95D;
    public static final double FOLLOW_RANGE = 48.0D;
    public static final int XP_REWARD = 500;
    public static final double STEP_HEIGHT = 1.5D;

    /** Falls shorter than this are absorbed by the wings (spec §11.1). */
    public static final double SAFE_FALL_BLOCKS = 8.0D;

    /** Health fractions where the fight changes phase (spec §12.2). */
    public static final float PHASE_ADMINISTRATOR_BELOW = 0.70F;
    public static final float PHASE_COLLAPSE_BELOW = 0.35F;
    /** In the Duelist phase the Piercing Sentence only opens once health drops to 85 % (spec §13.E). */
    public static final float PIERCING_UNLOCK_HEALTH = 0.85F;

    // ---- defence (spec §11) [ADP values]
    public static final float GUARD_REDUCTION = 0.5F;
    public static final double GUARD_HALF_ARC = 60.0D;
    public static final int GUARD_TICKS = 30;
    public static final float POSTURE_MAX = 60.0F;
    public static final float POSTURE_HEAVY_HIT = 8.0F;
    public static final float POSTURE_WEAK_FACTOR = 0.25F;
    public static final float POSTURE_DECAY_PER_TICK = POSTURE_MAX * 0.10F / 20.0F;
    public static final int STAGGER_IMMUNITY_TICKS = 200;
    public static final int PARRY_WINDOW_START = 5;
    public static final int PARRY_WINDOW_END = 9;
    public static final int PARRY_FAIL_TICK = 14;
    public static final int PARRY_REACTION_TICKS = 40;
    public static final int PARRY_CHAIN_WINDOW = 60;
    public static final int PARRY_MAX_IN_ROW = 2;
    public static final int PARRY_VULNERABLE_TICKS = 8;
    public static final int DEFLECT_COOLDOWN = 40;
    public static final float WINGS_OPEN_BACK_BONUS = 1.25F;
    public static final double BACK_ANGLE = 120.0D;
    public static final double BACKSTEP_DISTANCE = 1.5D * SCALE;
    public static final int BACKSTEP_MOVE_TICKS = 12;

    // ---- attack flow (spec §12.1, §13)
    private static final int[] GLOBAL_COOLDOWN = {8, 8, 6, 4}; // by Phase ordinal
    private static final int[] PARRY_COOLDOWN = {100, 100, 80, 120};
    public static final float COLLAPSE_RECOVERY_FACTOR = 1.3F;
    public static final float COLLAPSE_COMBO_CHANCE = 0.5F;
    public static final int COMBO_PAUSE = 8;
    public static final float[] CHAIN_CHANCE = {0.0F, 0.50F, 0.60F, 0.75F}; // by Phase ordinal
    public static final int CHAIN_WINDOW = 14;
    public static final double ATTACK_FACING_DEGREES = 20.0D;
    public static final float TURN_DEGREES_PER_TICK = 20.0F;
    /** The head leads the body (spec §10): it turns faster and may sit this far off the body axis. */
    public static final float HEAD_TURN_DEGREES_PER_TICK = 28.0F;
    public static final float HEAD_MAX_OFFSET = 80.0F;
    /** A blow tracks its target through the wind-up and locks its direction this many ticks before the first volume. */
    public static final int AIM_LOCK_LEAD = 6;
    public static final float AIM_DEGREES_PER_TICK = 10.0F;
    public static final double ANTI_WHIFF_MARGIN = 1.0D * SCALE;
    public static final double CLOSE_RANGE = 1.5D * SCALE;
    public static final double SHORT_RANGE = 3.2D * SCALE;
    public static final double MEDIUM_RANGE = 7.0D * SCALE;
    public static final double SIEGE_RADIUS = 4.0D * SCALE;
    public static final double WHEEL_CLEARANCE = 3.0D * SCALE;
    public static final double LUNGE_DISTANCE = 1.5D * SCALE;
    /** The Rushing Rebuke's charge: covered over ticks RUSH_FROM..RUSH_TO without any damage volume. */
    public static final double RUSH_DISTANCE = 4.2D;
    public static final int RUSH_FROM = 8;
    public static final int RUSH_TO = 16;
    public static final int LUNGE_FROM = 9;
    public static final int LUNGE_TO = 12;

    // ---- Gargoyle Fall (spec §13.F)
    public static final int LEAP_LOCK_TICK = 9;
    public static final int LEAP_APEX_TICK = 17;
    public static final int LEAP_DIVE_TICK = 24;
    public static final int LEAP_LAND_TICK = 32;
    public static final double LEAP_MAX_HORIZONTAL = 5.0D * SCALE;
    public static final double LEAP_MIN_APEX = 4.0D * SCALE;
    public static final double LEAP_RUNE_RADIUS = 1.5D * SCALE;

    // ---- targeting, leash and rest (spec §12.3; leash [ADP] until the arena exists)
    public static final double LEASH_RADIUS = 36.0D;
    public static final int TARGET_SCAN_INTERVAL = 10;
    /** A target switch needs a clearly better candidate and then freezes the choice for a while (no spinning on players). */
    public static final float TARGET_SWITCH_MARGIN = 1.25F;
    public static final int TARGET_SWITCH_LOCK = 60;
    public static final int REST_AFTER_TICKS = 200;
    public static final int IDLE_VARIANT_MIN = 100;
    public static final int IDLE_VARIANT_SPREAD = 100;
    public static final double PILLAR_MIN_HEIGHT = 3.0D * SCALE;
    public static final int PILLAR_TICKS = 60;
    public static final double REVEAL_PROTECTION_RADIUS = 24.0D;
    /** Speed modifiers on BASE_SPEED (0.30), top speed is about 1.04 x BASE_SPEED x modifier blocks/tick: walking 0.2 (4 b/s), running 0.34 (6.8 b/s, above a sprinting player at 5.6; 1.5 gave 0.44 and was too fast to kite). */
    public static final double WALK_SPEED = 0.65D;
    public static final double RUN_SPEED = 1.1D;

    // ---- footwork and evasion (reform): blocks per tick and tick counts of the by-hand movements
    public static final double STRAFE_SPEED = 0.17D;
    public static final double BACKPEDAL_SPEED = 0.15D;
    public static final int DODGE_STARTUP = 3;
    public static final int DODGE_MOVE_TICKS = 8;
    public static final int COUNTER_WINDOW = 26;
    public static final int[] DODGE_COOLDOWN = {55, 55, 45, 35}; // by Phase ordinal
    public static final float[] DODGE_CHANCE = {0.0F, 0.28F, 0.36F, 0.44F}; // per read (every 5 ticks) when threatened
    public static final int MAX_DODGES_IN_ROW = 2;
    /** He never stays in the pose reading the fight for longer than this when nothing else is chosen. */
    public static final int READ_MAX_TICKS = 12;
    /** Shield pressure: the points a player blocking accumulates before the Verdict Hammer is wanted. */
    public static final int SHIELD_PRESSURE_THRESHOLD = 36;
    public static final float DISABLE_SHIELD_SECONDS = 5.0F;
    public static final double ENRAGE_PUSH_RADIUS = 4.0D * SCALE;
    /** The three rest idles drawn out of combat (MOB_CONTRACT §3.2). */
    private static final ArchitectAnim[] REST_IDLES = {
            ArchitectAnim.MANIACAL_LAUGH, ArchitectAnim.IDLE_BLADE_INSPECT, ArchitectAnim.IDLE_WING_ADJUST};

    // ---- reveal beats (spec §7.4) and death
    public static final int REVEAL_MASK_RIGHT_TICK = 58;
    public static final int REVEAL_MASK_LEFT_TICK = 64;
    public static final int REVEAL_TABLET_TICK = 72;
    /** The jaw opens and he laughs while the title card rises (§7.4 rows 10-11). */
    public static final int REVEAL_LAUGH_TICK = 86;
    public static final int DEATH_TICKS = 80;

    // Reserved entity event range: 160..179 (spec §19).
    public static final byte EVENT_AWAKEN_START = (byte) 160;
    public static final byte EVENT_MASK_RIGHT = (byte) 161;
    public static final byte EVENT_MASK_LEFT = (byte) 162;
    public static final byte EVENT_TABLET_FALL = (byte) 163;
    public static final byte EVENT_SIXFOLD_CUT_FIRST = (byte) 164;
    public static final byte EVENT_PARRY = (byte) 170;
    public static final byte EVENT_STAGGER = (byte) 171;
    public static final byte EVENT_GARGOYLE_IMPACT = (byte) 172;
    public static final byte EVENT_DODGE = (byte) 173;
    public static final byte EVENT_SHIELD_BREAK = (byte) 174;
    public static final byte EVENT_ENRAGE = (byte) 177;
    public static final byte EVENT_DEATH = (byte) 178;

    public static final DustParticleOptions MANA_DUST = new DustParticleOptions(0x38BDBA, 0.9F);
    public static final DustParticleOptions RUNE_DUST = new DustParticleOptions(0xD8C8FF, 1.0F);
    public static final DustParticleOptions ERROR_DUST = new DustParticleOptions(0xFF604B, 0.8F);
    public static final DustParticleOptions STONE_DUST = new DustParticleOptions(0x6E7582, 1.2F);

    private static final String TAG_STATE = "ArchitectState";
    private static final String TAG_ANCHOR = "ArchitectAnchor";
    private static final String TAG_PHASE = "ArchitectReachedPhase";

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(Architect.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ANIM =
            SynchedEntityData.defineId(Architect.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_LOCKED_YAW =
            SynchedEntityData.defineId(Architect.class, EntityDataSerializers.FLOAT);

    /** Client-side clock of the current clip, restarted whenever the server changes the clip. */
    public final AnimationState animState = new AnimationState();
    /** Themed bar (client/bossbar/ArchitectBossBar): hidden while he is a statue, its colour carries the phase. */
    private final ServerBossEvent bossEvent = new ServerBossEvent(UUID.randomUUID(),
            Component.translatable("entity.codenamecoraline.architect"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    // ---- timelines
    private int stateTicks;
    private int animStartTick;
    private int revealClientStart = -1;
    private float lockedYaw;

    // ---- the current attack
    private @Nullable ArchitectAttack attack;
    private boolean attackVariant;
    private int extraRecovery;
    private @Nullable LivingEntity attackTarget;
    private final Map<Integer, float[]> hitsByVictim = new HashMap<>(); // id -> {hits, damage in DB, strike bitmask}
    private final int[] cooldowns = new int[ArchitectAttack.values().length];
    private final float[] weights = new float[ArchitectAttack.values().length];
    private final ArchitectAttack[] recentAttacks = new ArchitectAttack[4];
    private final int[] attackUses = new int[ArchitectAttack.values().length];
    private int recentIndex;
    private @Nullable ArchitectAttack lastAttack;
    private int globalCooldown;
    private boolean comboArmed;
    private boolean staggerPending;

    // ---- tactics (reform): footwork, dodges, chains and the reading of the shield
    private Tactic tactic = Tactic.READ;
    private int tacticTicks;
    private int tacticLimit;
    private int strafeSide = 1; // +1 = his left
    private int sideSwitchTick;
    private @Nullable Dodge dodge;
    private int dodgeCooldown;
    private int dodgesInRow;
    private int counterTicks;
    private double dodgeStepX;
    private double dodgeStepZ;
    private int shieldScore;
    private int idleTicks;
    private int unreachableTicks;
    private final java.util.EnumSet<ArchitectAttack> chain = java.util.EnumSet.noneOf(ArchitectAttack.class);
    private int chainTicks;
    private boolean lastBlow;
    private float attackYaw;
    private @Nullable Dodge lastDodge;
    private @Nullable LivingEntity dodgeTarget;
    private int footworkTick = -1000;
    private int recentHits;
    private int dodges;
    private int shieldsBroken;
    private int blowsPunished;

    // ---- Gargoyle Fall
    private Vec3 leapFrom = Vec3.ZERO;
    private Vec3 leapTo = Vec3.ZERO;
    private double leapApex;
    private boolean airborne;

    // ---- defence
    private float posture;
    private int lastHurtTick = -1000;
    private int staggerImmunity;
    private int deflectCooldown;
    private int parryCooldown;
    private int parriesInRow;
    private int lastParryTick = -1000;
    private boolean parryLeft;
    private boolean parried;
    private @Nullable LivingEntity parryAttacker;
    private int guardTicks;

    // ---- targeting, leash and rest
    private final Predicate<LivingEntity> foeInLeash = this::isFoeInLeash;
    private boolean hasAnchor;
    private double anchorX;
    private double anchorY;
    private double anchorZ;
    private int noTargetTicks;
    private int nextIdleVariantTick;
    private int pillarTicks;
    /** Threat table of the targeting (spec reform §12.4): damage dealt, decayed, and when each foe was last the target. */
    private final Map<UUID, float[]> threats = new HashMap<>(); // uuid -> {damage, last targeted tick}
    private int retargetLockUntil;
    private int lastAggressionTick = -1000;
    private int aggressorId = -1;
    private Phase reachedPhase = Phase.DUELIST;
    private int deathTicks;

    // ---- counters read by the GameTests
    private int strikesLanded;
    private int attacksAborted;
    private int parries;
    private int staggers;
    private int deflections;

    /** What the Architect is doing; synchronized so the client can pick the animation. */
    public enum State {
        DORMANT,
        AWAKENING,
        COMBAT_IDLE,
        REPOSITION,
        ATTACKING,
        CHANNELING,
        GUARDING,
        PARRYING,
        STAGGERED,
        PHASE_TRANSITION,
        DYING,
        /** A physical evasive move (side step, back step, diagonal, lean) with its own clip, run by tickDodge. */
        DODGING
    }

    /** What he does with his feet while the state is COMBAT_IDLE (the brain's sub-state; never synchronized). */
    private enum Tactic {
        /** Poised and reading the opponent, for at most READ_MAX_TICKS. */
        READ,
        /** Closing the distance on foot (walk or run by distance). */
        ADVANCE,
        /** Sliding sideways around the target, facing it. */
        CIRCLE,
        /** Backing away to open the distance, facing the target. */
        RETREAT
    }

    /** The evasive moves: direction as seen from his body (0 ahead, positive to his left), distance in blocks. */
    public enum Dodge {
        LEFT(90.0D, 4.0D, ArchitectAnim.DODGE_LEFT),
        RIGHT(-90.0D, 4.0D, ArchitectAnim.DODGE_RIGHT),
        BACK(180.0D, 3.8D, ArchitectAnim.DODGE_BACK),
        DIAG_LEFT(135.0D, 4.2D, ArchitectAnim.DODGE_DIAG_LEFT),
        DIAG_RIGHT(-135.0D, 4.2D, ArchitectAnim.DODGE_DIAG_RIGHT),
        LEAN_LEFT(90.0D, 1.1D, ArchitectAnim.LEAN_LEFT),
        LEAN_RIGHT(-90.0D, 1.1D, ArchitectAnim.LEAN_RIGHT);

        public final double angle;
        public final double distance;
        public final ArchitectAnim anim;

        Dodge(double angle, double distance, ArchitectAnim anim) {
            this.angle = angle;
            this.distance = distance;
            this.anim = anim;
        }

        public boolean isLean() {
            return this == LEAN_LEFT || this == LEAN_RIGHT;
        }
    }

    /** Fight phases; MONUMENT is the disguise, the rest follow health (spec §12.2). */
    public enum Phase {
        MONUMENT,
        DUELIST,
        ADMINISTRATOR,
        COLLAPSE
    }

    public Architect(EntityType<? extends Architect> type, Level level) {
        super(type, level);
        this.xpReward = XP_REWARD;
        this.setPersistenceRequired();
        this.bossEvent.setVisible(false);
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
        builder.define(DATA_STATE, State.DORMANT.ordinal());
        builder.define(DATA_ANIM, ArchitectAnim.DORMANT_STATUE.ordinal());
        builder.define(DATA_LOCKED_YAW, 0.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    // ================================================================= state, clip and phase
    public State getState() {
        int index = this.entityData.get(DATA_STATE);
        State[] values = State.values();
        return index >= 0 && index < values.length ? values[index] : State.DORMANT;
    }

    public void setState(State state) {
        if (this.getState() != state) {
            this.stateTicks = 0;
        }
        this.entityData.set(DATA_STATE, state.ordinal());
        if (state == State.DORMANT) {
            this.setAnim(ArchitectAnim.DORMANT_STATUE);
        } else if (state == State.AWAKENING) {
            this.setAnim(ArchitectAnim.AWAKEN);
        } else if (state == State.COMBAT_IDLE) {
            this.setAnim(ArchitectAnim.BATTLE_IDLE);
        }
    }

    public ArchitectAnim getAnim() {
        return ArchitectAnim.byOrdinal(this.entityData.get(DATA_ANIM));
    }

    public void setAnim(ArchitectAnim anim) {
        if (this.getAnim() != anim) {
            this.entityData.set(DATA_ANIM, anim.ordinal());
            this.animStartTick = this.tickCount;
        }
    }

    public float getLockedYaw() {
        return this.entityData.get(DATA_LOCKED_YAW);
    }

    public void setLockedYaw(float yaw) {
        this.lockedYaw = yaw;
        this.entityData.set(DATA_LOCKED_YAW, yaw);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (!this.level().isClientSide()) {
            return;
        }
        if (DATA_ANIM.equals(accessor)) {
            this.animState.start(this.tickCount);
        }
        if (DATA_STATE.equals(accessor)) {
            this.revealClientStart = this.getState() == State.AWAKENING ? this.tickCount : -1;
        }
    }

    /** Ticks into the reveal, on either side (the client counts from when it saw the state change). */
    public int getRevealTicks() {
        if (this.getState() != State.AWAKENING) {
            return 0;
        }
        if (this.level().isClientSide()) {
            return this.revealClientStart < 0 ? 0 : this.tickCount - this.revealClientStart;
        }
        return this.stateTicks;
    }

    /** Wakes the statue: the 110-tick reveal (spec §7.4), invulnerable while it plays. */
    public void awaken() {
        if (!this.isDormant()) {
            return;
        }
        this.setAnchorHere();
        this.setState(State.AWAKENING);
        this.level().broadcastEntityEvent(this, EVENT_AWAKEN_START);
        this.playSound(ModSounds.ARCHITECT_AWAKEN.get(), 1.0F, 1.0F);
    }

    public boolean isDormant() {
        return this.getState() == State.DORMANT;
    }

    public Phase getPhase() {
        return this.isDormant() ? Phase.MONUMENT : phaseFor(this.getHealth() / this.getMaxHealth());
    }

    /** Combat phase for a health fraction in [0, 1]; thresholds are exclusive (70 % is still the Duelist). */
    public static Phase phaseFor(float healthFraction) {
        if (healthFraction < PHASE_COLLAPSE_BELOW) {
            return Phase.COLLAPSE;
        }
        return healthFraction < PHASE_ADMINISTRATOR_BELOW ? Phase.ADMINISTRATOR : Phase.DUELIST;
    }

    /** The phase whose transition has already played (the fight never goes back). */
    public Phase getReachedPhase() {
        return this.reachedPhase;
    }

    // ================================================================= boss bar
    private void updateBossBar() {
        this.bossEvent.setVisible(!this.isDormant() && this.getState() != State.DYING);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.bossEvent.setColor(switch (this.reachedPhase) {
            case ADMINISTRATOR -> BossEvent.BossBarColor.BLUE;
            case COLLAPSE -> BossEvent.BossBarColor.RED;
            default -> BossEvent.BossBarColor.PURPLE;
        });
    }

    public ServerBossEvent getBossEvent() {
        return this.bossEvent;
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

    // ================================================================= timelines (run every tick, AI or not)
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.animState.startIfStopped(this.tickCount);
        } else if (this.level() instanceof ServerLevel level && this.isAlive()) {
            this.stateTicks++;
            this.tickCounters();
            switch (this.getState()) {
                case AWAKENING -> this.tickAwakening(level);
                case ATTACKING -> this.tickAttack(level);
                case PARRYING -> this.tickParry(level);
                case STAGGERED -> this.tickStagger();
                case PHASE_TRANSITION -> this.tickTransition();
                case GUARDING -> this.tickGuard();
                case REPOSITION -> this.tickBackstep();
                case DODGING -> this.tickDodge(level);
                case COMBAT_IDLE -> this.tickIdleClip();
                default -> {
                }
            }
            this.checkPhase(level);
            this.updateBossBar();
        }
        if (this.isRooted()) {
            this.setDeltaMovement(0.0D, this.airborne ? 0.0D : Math.min(0.0D, this.getDeltaMovement().y), 0.0D);
        }
        if (this.locksFacing()) {
            this.face(this.getLockedYaw());
        } else if (this.isDormant() || this.getState() == State.AWAKENING) {
            this.face(this.getYRot());
        }
        if (!this.level().isClientSide() && this.isAlive()) {
            this.updateHead();
        }
    }

    /** States in which he does not walk (the lunge, the backstep and the leap move him by hand). */
    private boolean isRooted() {
        return switch (this.getState()) {
            case DORMANT, AWAKENING, ATTACKING, PARRYING, STAGGERED, PHASE_TRANSITION, REPOSITION, DYING, DODGING -> true;
            default -> false;
        };
    }

    private boolean locksFacing() {
        State s = this.getState();
        return s == State.ATTACKING || s == State.PARRYING || s == State.REPOSITION || s == State.DODGING;
    }

    /** Snaps body, head and the interpolation to {@code yaw}: only for locked poses (a blow in flight, a dodge). */
    private void face(float yaw) {
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.yBodyRot = yaw;
        this.yBodyRotO = yaw;
        this.setYHeadRot(yaw);
        this.yHeadRotO = yaw;
    }

    /** Turns the body toward {@code yaw} by at most {@code step} degrees, keeping the interpolation smooth. */
    private void turnBody(float yaw, float step) {
        float next = Mth.approachDegrees(this.getYRot(), yaw, step);
        this.setYRot(next);
        this.yBodyRot = next;
    }

    /**
     * The head leads the body (spec §10: head first, then shoulders, torso and hips): it follows the target faster than
     * the body and may sit up to HEAD_MAX_OFFSET degrees off the body axis. The renderer turns the difference into the
     * neck, the head and a little of the torso. The head is server state, so every client sees the same gaze.
     */
    private void updateHead() {
        LivingEntity target = this.getTarget();
        if (this.isNoAi() || target == null || !this.isFoe(target) || this.isDormant()
                || this.getState() == State.AWAKENING || this.getState() == State.DYING
                || this.getState() == State.STAGGERED) {
            if (this.getState() == State.STAGGERED || this.getState() == State.DYING) {
                this.setYHeadRot(this.yBodyRot);
            }
            return;
        }
        float wanted = yawTowards(this, target);
        float offset = Mth.clamp(Mth.wrapDegrees(wanted - this.yBodyRot), -HEAD_MAX_OFFSET, HEAD_MAX_OFFSET);
        float goal = this.yBodyRot + offset;
        this.setYHeadRot(Mth.approachDegrees(this.getYHeadRot(), goal, HEAD_TURN_DEGREES_PER_TICK));
        double dy = target.getEyeY() - this.getEyeY();
        double flat = Math.max(1.0E-3D, horizontalDistance(this, target));
        this.setXRot(Mth.clamp((float) -(Mth.atan2(dy, flat) * Mth.RAD_TO_DEG), -25.0F, 25.0F));
    }

    private void tickCounters() {
        for (int i = 0; i < this.cooldowns.length; i++) {
            if (this.cooldowns[i] > 0) {
                this.cooldowns[i]--;
            }
        }
        if (this.globalCooldown > 0) {
            this.globalCooldown--;
        }
        if (this.staggerImmunity > 0) {
            this.staggerImmunity--;
        }
        if (this.deflectCooldown > 0) {
            this.deflectCooldown--;
        }
        if (this.parryCooldown > 0) {
            this.parryCooldown--;
        }
        if (this.tickCount - this.lastHurtTick > 20 && this.posture > 0.0F) {
            this.posture = Math.max(0.0F, this.posture - POSTURE_DECAY_PER_TICK);
        }
        if (this.dodgeCooldown > 0) {
            this.dodgeCooldown--;
        }
        if (this.counterTicks > 0) {
            this.counterTicks--;
        }
        if (this.tickCount - this.lastHurtTick > 20) {
            this.recentHits = 0;
        }
        if (this.tickCount % 20 == 0) {
            this.decayThreats();
        }
        LivingEntity watched = this.getTarget();
        if (watched != null) {
            this.watchShield(watched);
        }
    }

    private void tickAwakening(ServerLevel level) {
        int t = this.stateTicks;
        if (t == REVEAL_MASK_RIGHT_TICK) {
            level.broadcastEntityEvent(this, EVENT_MASK_RIGHT);
            this.playSound(ModSounds.ARCHITECT_MASK_FALL.get(), 1.0F, 1.0F);
        } else if (t == REVEAL_MASK_LEFT_TICK) {
            level.broadcastEntityEvent(this, EVENT_MASK_LEFT);
            this.playSound(ModSounds.ARCHITECT_MASK_FALL.get(), 1.0F, 0.9F);
        } else if (t == REVEAL_TABLET_TICK) {
            level.broadcastEntityEvent(this, EVENT_TABLET_FALL);
            this.playSound(ModSounds.ARCHITECT_TABLET_FALL.get(), 1.0F, 1.0F);
            level.sendParticles(STONE_DUST, this.getX(), this.getY() + 0.3D, this.getZ(), 24, 0.8D, 0.1D, 0.8D, 0.02D);
        }
        if (t == REVEAL_LAUGH_TICK || t == REVEAL_LAUGH_TICK + 8 || t == REVEAL_LAUGH_TICK + 16) {
            this.playSound(ModSounds.ARCHITECT_LAUGH.get(), 1.0F, 1.0F); // stone grinding in three pulses (§16.4)
        }
        if (t >= ArchitectAnim.AWAKEN.ticks) {
            this.setState(State.COMBAT_IDLE);
            this.nextIdleVariantTick = this.tickCount + IDLE_VARIANT_MIN;
        }
    }

    /** In combat idle the clip follows his movement, except while one of the rest idles plays out. */
    private void tickIdleClip() {
        ArchitectAnim anim = this.getAnim();
        boolean variantPlaying = (anim == ArchitectAnim.MANIACAL_LAUGH || anim == ArchitectAnim.IDLE_BLADE_INSPECT
                || anim == ArchitectAnim.IDLE_WING_ADJUST) && this.tickCount - this.animStartTick < anim.ticks;
        if (variantPlaying) {
            return;
        }
        if (this.isStepping()) { // by-hand footwork: the clip names the direction of the feet
            this.setAnim(this.tactic == Tactic.RETREAT ? ArchitectAnim.BACKPEDAL
                    : this.strafeSide > 0 ? ArchitectAnim.STRAFE_LEFT : ArchitectAnim.STRAFE_RIGHT);
            return;
        }
        double speed = this.getDeltaMovement().horizontalDistanceSqr();
        this.setAnim(speed > 0.06D ? ArchitectAnim.RUN : speed > 1.0E-4D ? ArchitectAnim.WALK : ArchitectAnim.BATTLE_IDLE);
    }

    private void tickGuard() {
        if (this.getAnim() == ArchitectAnim.PROJECTILE_DEFLECT
                && this.tickCount - this.animStartTick >= ArchitectAnim.PROJECTILE_DEFLECT.ticks) {
            this.setAnim(ArchitectAnim.GUARD_FULL);
        }
        if (this.stateTicks >= this.guardTicks) {
            this.setState(State.COMBAT_IDLE);
        }
    }

    private void tickStagger() {
        if (this.stateTicks >= ArchitectAnim.STAGGER.ticks) {
            this.setState(State.COMBAT_IDLE);
            this.pause(0, 3);
        }
    }

    // ================================================================= attacks: the four phases
    /**
     * Starts an attack against {@code target} now. The blow aims at the target through its wind-up (AIM_DEGREES_PER_TICK)
     * and locks the direction AIM_LOCK_LEAD ticks before the first volume (spec §13.0, reform): he never swings at where
     * the target was a second ago, and the last 6 ticks are the fair window to sidestep. The brain calls it after its
     * checks; the GameTests call it directly.
     */
    public void startAttack(ArchitectAttack type, LivingEntity target) {
        this.attack = type;
        this.attackTarget = target;
        this.setLockedYaw(yawTowards(this, target));
        this.face(this.lockedYaw);
        this.lastBlow = false;
        this.dodge = null;
        this.attackVariant = this.variantFor(type, target);
        this.extraRecovery = this.reachedPhase == Phase.COLLAPSE
                ? Math.round((type.ticks() - type.recoveryStart) * (COLLAPSE_RECOVERY_FACTOR - 1.0F)) : 0;
        this.hitsByVictim.clear();
        this.staggerPending = false;
        this.cooldowns[type.ordinal()] = type.cooldown;
        this.attackUses[type.ordinal()]++;
        this.recentAttacks[this.recentIndex] = type;
        this.recentIndex = (this.recentIndex + 1) % this.recentAttacks.length;
        this.lastAttack = type;
        this.getNavigation().stop();
        this.setState(State.ATTACKING);
        this.setAnim(type.anim);
        SoundEvent windup = windupSound(type);
        if (windup != null) {
            this.playSound(windup, 1.0F, 1.0F);
        }
    }

    /** Each blow has its own wind-up sound (spec §16.4); the Gargoyle Fall announces itself with the wing beat. */
    private static @Nullable SoundEvent windupSound(ArchitectAttack type) {
        return switch (type) {
            case CARTENON_WHEEL -> ModSounds.ARCHITECT_WHEEL_WINDUP.get();
            case PIERCING_SENTENCE -> ModSounds.ARCHITECT_SENTENCE_WINDUP.get();
            case GARGOYLE_FALL -> null;
            default -> ModSounds.ARCHITECT_WINDUP.get();
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ARCHITECT_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ARCHITECT_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.ARCHITECT_STEP.get(), 0.9F, 1.0F);
    }

    /** The spec's variations: A glued to him, B in the Collapse, D against a target in the air. */
    private boolean variantFor(ArchitectAttack type, LivingEntity target) {
        return switch (type) {
            case SIXFOLD_EDICT -> horizontalDistance(this, target) <= CLOSE_RANGE;
            case CROSSED_MANDATE -> this.reachedPhase == Phase.COLLAPSE;
            case SPLITTER_CRESCENT -> isInAir(target);
            default -> false;
        };
    }

    private void tickAttack(ServerLevel level) {
        ArchitectAttack a = this.attack;
        if (a == null) {
            this.setState(State.COMBAT_IDLE);
            return;
        }
        int t = this.stateTicks;
        this.trackAim(a, t);
        if (a == ArchitectAttack.GARGOYLE_FALL) {
            if (!this.tickLeap(level, t)) {
                return;
            }
        } else if (t == a.validationTick && !this.anyFoeReachable(level, a)) {
            this.abortAttack();
            return;
        }
        if (a == ArchitectAttack.PIERCING_SENTENCE && t >= LUNGE_FROM && t <= LUNGE_TO) {
            this.moveAlong(this.lockedYaw, LUNGE_DISTANCE / (LUNGE_TO - LUNGE_FROM + 1));
        }
        if (a == ArchitectAttack.RUSHING_REBUKE && t >= RUSH_FROM && t <= RUSH_TO) {
            this.rush(level);
        }
        if (a == ArchitectAttack.SIXFOLD_EDICT && this.attackVariant && t >= 14 && t <= 16) {
            this.moveAlong(this.lockedYaw, -1.0D / 3.0D); // the pelvis steps back before the bilateral sweep
        }
        List<Strike> strikes = a.strikes;
        for (int i = 0; i < strikes.size(); i++) {
            Strike s = strikes.get(i);
            if (s.liveAt(t, this.attackVariant)) {
                if (t == s.start()) {
                    this.strikeTrail(level, s);
                    if (a == ArchitectAttack.SIXFOLD_EDICT) {
                        level.broadcastEntityEvent(this, (byte) (EVENT_SIXFOLD_CUT_FIRST + Math.min(5, this.sixfoldCutIndex(i))));
                    }
                }
                this.applyStrike(level, a, i, s, null);
            }
        }
        if (t == a.ticks()) {
            this.setAnim(ArchitectAnim.BATTLE_IDLE); // the clip is over; the extra recovery of the Collapse stays open
        }
        if (t >= a.ticks() + this.extraRecovery) {
            this.finishAttack();
        }
    }

    /** Until the lock tick the blow turns toward its target (limited, so a sidestep during the wind-up still works). */
    private void trackAim(ArchitectAttack a, int t) {
        LivingEntity target = this.attackTarget;
        if (target == null || !target.isAlive() || a == ArchitectAttack.GARGOYLE_FALL || this.isNoAi()
                || t >= Math.max(1, a.firstActiveTick - AIM_LOCK_LEAD)) {
            return;
        }
        this.setLockedYaw(Mth.approachDegrees(this.lockedYaw, yawTowards(this, target), AIM_DEGREES_PER_TICK));
        this.face(this.lockedYaw);
    }

    /** The Rushing Rebuke's charge: a real run along the locked direction, stopping short of the target and of walls. */
    private void rush(ServerLevel level) {
        double step = RUSH_DISTANCE / (RUSH_TO - RUSH_FROM + 1);
        LivingEntity target = this.attackTarget;
        if (target != null) {
            double gap = horizontalDistance(this, target) - (target.getBbWidth() + this.getBbWidth()) / 2.0D - 0.3D;
            step = Math.min(step, Math.max(0.0D, gap));
        }
        this.moveSafely(level, this.lockedYaw, step, 0.0D);
    }

    /** Index of a Sixfold cut among the six (the close variation replaces the third one). */
    private int sixfoldCutIndex(int strikeIndex) {
        return strikeIndex <= 2 ? strikeIndex : strikeIndex - 1;
    }

    /** Anti-whiff (MOB_CONTRACT §3.2): the blow only lands if some foe is still within its reach and arc. */
    private boolean anyFoeReachable(ServerLevel level, ArchitectAttack a) {
        double extra = a == ArchitectAttack.PIERCING_SENTENCE ? LUNGE_DISTANCE : 0.0D; // the charge of M is over by tick 16
        double reach = a.reach + extra + ANTI_WHIFF_MARGIN;
        boolean anyAngle = a == ArchitectAttack.CARTENON_WHEEL;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(reach, 2.0D, reach), this.foeInLeash)) {
            double d = horizontalDistance(this, e) - e.getBbWidth() / 2.0D;
            double dy = e.getY() - this.getY();
            double angle = ArchitectStrikes.angleOf(e.getX() - this.getX(), e.getZ() - this.getZ(), this.lockedYaw);
            if (d <= reach && dy > -2.0D * SCALE && dy < 4.0D * SCALE && (anyAngle || Math.abs(angle) <= 100.0D)) {
                return true;
            }
        }
        return false;
    }

    private void abortAttack() {
        this.attacksAborted++;
        this.endAirborne();
        this.attack = null;
        this.setState(State.COMBAT_IDLE);
        this.globalCooldown = GLOBAL_COOLDOWN[this.reachedPhase.ordinal()];
    }

    private void finishAttack() {
        ArchitectAttack done = this.attack;
        LivingEntity victim = this.attackTarget;
        this.attack = null;
        this.attackTarget = null;
        if (this.staggerPending) {
            this.stagger();
            return;
        }
        this.setState(State.COMBAT_IDLE);
        this.globalCooldown = GLOBAL_COOLDOWN[this.reachedPhase.ordinal()];
        this.comboArmed = false;
        this.dodgesInRow = 0; // a blow thrown: the evasion budget is paid back
        this.pause(1, 3);
        this.armChain(done, victim);
        if (this.chain.isEmpty() && this.reachedPhase == Phase.COLLAPSE && this.random.nextFloat() < COLLAPSE_COMBO_CHANCE) {
            this.globalCooldown = COMBO_PAUSE; // spec §12.2: two physical blows chained with an 8-tick pause
            this.comboArmed = true;
        }
    }

    /**
     * Combat transitions (reform §12.5): after a blow he may follow with a specific one, chosen from what the target did,
     * not at random. The follow-up ignores its own cooldown, is re-validated against distance and facing when it is
     * picked, and is dropped silently when it no longer makes sense. Telegraphs are never skipped: every blow of the
     * chain has its own wind-up.
     */
    private void armChain(@Nullable ArchitectAttack done, @Nullable LivingEntity victim) {
        this.chain.clear();
        this.chainTicks = 0;
        boolean punish = done == ArchitectAttack.SHIELD_BREAKER && victim != null && !victim.isBlocking(); // the shield is down: always
        if (done == null || victim == null || !victim.isAlive()
                || !punish && this.random.nextFloat() >= CHAIN_CHANCE[this.reachedPhase.ordinal()]) {
            return;
        }
        boolean jumped = isInAir(victim);
        boolean shielding = victim.isBlocking();
        double dist = horizontalDistance(this, victim);
        switch (done) {
            case SIXFOLD_EDICT -> this.chainFor(shielding ? ArchitectAttack.SHIELD_BREAKER : ArchitectAttack.CROSSED_MANDATE,
                    ArchitectAttack.SPLITTER_CRESCENT);
            case CROSSED_MANDATE -> {
                if (jumped || dist <= CLOSE_RANGE) {
                    this.chainFor(ArchitectAttack.SPLITTER_CRESCENT);
                } else {
                    this.chainFor(ArchitectAttack.PIERCING_SENTENCE, ArchitectAttack.RUSHING_REBUKE);
                }
            }
            case SPLITTER_CRESCENT -> this.chainFor(dist > SHORT_RANGE ? ArchitectAttack.PIERCING_SENTENCE : ArchitectAttack.SIXFOLD_EDICT);
            case PIERCING_SENTENCE -> this.chainFor(ArchitectAttack.SIXFOLD_EDICT, ArchitectAttack.CROSSED_MANDATE);
            case RUSHING_REBUKE -> this.chainFor(ArchitectAttack.SIXFOLD_EDICT, ArchitectAttack.SPLITTER_CRESCENT);
            case FALSE_VERDICT -> this.chainFor(shielding ? ArchitectAttack.SHIELD_BREAKER : ArchitectAttack.SIXFOLD_EDICT);
            case SHIELD_BREAKER -> {
                if (!victim.isBlocking()) { // the shield is down: punish it before it comes back
                    this.blowsPunished++;
                    this.chainFor(ArchitectAttack.SIXFOLD_EDICT, ArchitectAttack.CROSSED_MANDATE);
                }
            }
            default -> {
            }
        }
        if (!this.chain.isEmpty()) {
            this.chainTicks = CHAIN_WINDOW;
            this.globalCooldown = COMBO_PAUSE;
        }
    }

    private void chainFor(ArchitectAttack... options) {
        for (ArchitectAttack a : options) {
            if (a.minPhase.ordinal() <= this.reachedPhase.ordinal()) {
                this.chain.add(a);
            }
        }
    }

    /** The vanilla hook that makes a blocked blow disable the shield: only the Verdict Hammer carries it (spec reform §13.K). */
    @Override
    public float getSecondsToDisableBlocking() {
        ArchitectAttack a = this.attack;
        return this.getState() == State.ATTACKING && a != null && a.breaksShield() ? DISABLE_SHIELD_SECONDS : 0.0F;
    }

    /**
     * Damages every foe inside the volume this tick, once per volume and within the attack's per-victim limits.
     * {@code only}, when set, is the single entity the volume may hit (the parry reprise).
     */
    private void applyStrike(ServerLevel level, ArchitectAttack a, int index, Strike s, @Nullable LivingEntity only) {
        double r = s.reach() + 1.0D;
        float base = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * PHASE_DAMAGE[this.reachedPhase.ordinal()];
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(r, 1.0D, r), this.foeInLeash)) {
            if (only != null && e != only) {
                continue;
            }
            if (!ArchitectStrikes.hits(s, this.getX(), this.getY(), this.getZ(), this.lockedYaw, e.getBoundingBox())) {
                continue;
            }
            float[] record = this.hitsByVictim.computeIfAbsent(e.getId(), id -> new float[4]);
            int mask = (int) record[2];
            if ((mask & (1 << index)) != 0 || record[0] >= a.maxHitsPerTarget || record[1] >= a.maxDamagePerTarget
                    || record[3] >= e.getMaxHealth() * BLOW_HEALTH_CAP) {
                continue;
            }
            float db = Math.min(s.damage(), a.maxDamagePerTarget - record[1]);
            record[0]++;
            record[1] += db;
            record[2] = mask | (1 << index);
            e.invulnerableTime = 0; // each cut of a combo is its own blow (the spec's per-victim limits apply instead)
            boolean wasBlocking = e.isBlocking();
            float before = e.getHealth() + e.getAbsorptionAmount();
            float raw = db * base;
            if (e.hurtServer(level, this.damageSources().mobAttack(this), raw * (1.0F - MANA_REND))) {
                this.strikesLanded++;
                level.sendParticles(ParticleTypes.CRIT, e.getX(), e.getY() + e.getBbHeight() * 0.6D, e.getZ(),
                        6, 0.2D, 0.2D, 0.2D, 0.15D);
                if (e.isAlive() && !wasBlocking) { // the mana part, only when the steel got through
                    e.invulnerableTime = 0;
                    e.hurtServer(level, this.damageSources().indirectMagic(this, this), raw * MANA_REND);
                }
            }
            record[3] += Math.max(0.0F, before - (e.getHealth() + e.getAbsorptionAmount()));
            if (a.breaksShield() && wasBlocking && !e.isBlocking()) { // the vanilla hook disabled the shield (getSecondsToDisableBlocking)
                this.shieldsBroken++;
                this.shieldScore = 0;
                level.broadcastEntityEvent(this, EVENT_SHIELD_BREAK);
            }
            if (a == ArchitectAttack.GARGOYLE_FALL && index == 0) {
                this.throwAway(e, 1.1D, 0.45D);
            }
        }
    }

    /** A short trail of mana dust along the volume as it appears (the visible half of §16). */
    private void strikeTrail(ServerLevel level, Strike s) {
        double y = this.getY() + (s.minY() + s.maxY()) / 2.0D;
        double yaw = this.lockedYaw * Mth.DEG_TO_RAD;
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double lx = Math.cos(yaw);
        double lz = Math.sin(yaw);
        for (int k = 0; k <= 6; k++) {
            double px;
            double pz;
            if (s.shape() == ArchitectStrikes.Shape.ARC) {
                double ang = Math.toRadians(Mth.lerp(k / 6.0D, s.c(), s.d()));
                double rad = s.b() * 0.85D;
                px = (fx * Math.cos(ang) + lx * Math.sin(ang)) * rad;
                pz = (fz * Math.cos(ang) + lz * Math.sin(ang)) * rad;
            } else {
                double along = Mth.lerp(k / 6.0D, s.b(), s.c());
                px = fx * along;
                pz = fz * along;
            }
            level.sendParticles(MANA_DUST, this.getX() + px, y, this.getZ() + pz, 1, 0.05D, 0.05D, 0.05D, 0.0D);
        }
        this.playSound(ModSounds.ARCHITECT_BLADE_CUT.get(), 1.0F, 1.0F);
    }

    // ================================================================= F: the Gargoyle Fall
    /** Runs the leap; false when the attack ended this tick (aborted). */
    private boolean tickLeap(ServerLevel level, int t) {
        if (t == LEAP_LOCK_TICK) {
            LivingEntity target = this.attackTarget;
            Vec3 landing = target == null ? null : this.landingFor(level, target);
            if (landing == null) {
                this.abortAttack();
                return false;
            }
            this.leapFrom = this.position();
            this.leapTo = landing;
            this.leapApex = Math.max(this.leapFrom.y, this.leapTo.y) + LEAP_MIN_APEX;
            this.airborne = true;
            this.setNoGravity(true);
            this.noPhysics = true;
            this.playSound(ModSounds.ARCHITECT_WING_FLAP.get(), 1.0F, 1.0F);
        }
        if (this.airborne && t > LEAP_LOCK_TICK && t < LEAP_LAND_TICK) {
            this.setPos(this.leapPoint(t));
            if (t % 4 == 0) {
                runeCircle(level, this.leapTo, LEAP_RUNE_RADIUS);
            }
        }
        if (this.airborne && t == LEAP_LAND_TICK) {
            this.setPos(this.leapTo);
            this.endAirborne();
            level.broadcastEntityEvent(this, EVENT_GARGOYLE_IMPACT);
            level.sendParticles(STONE_DUST, this.getX(), this.getY() + 0.2D, this.getZ(), 40, 1.8D, 0.1D, 1.8D, 0.05D);
            this.playSound(ModSounds.ARCHITECT_GARGOYLE_IMPACT.get(), 1.0F, 1.0F);
        }
        return true;
    }

    /** Where the dive lands: the target's position at tick 9, at most 5 blocks away, on solid ground with room for him. */
    private @Nullable Vec3 landingFor(ServerLevel level, LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double h = Math.sqrt(dx * dx + dz * dz);
        double k = h > LEAP_MAX_HORIZONTAL ? LEAP_MAX_HORIZONTAL / h : 1.0D;
        double x = this.getX() + dx * k;
        double z = this.getZ() + dz * k;
        double y = groundBelow(level, x, Math.max(target.getY(), this.getY()) + 0.5D, z);
        if (Double.isNaN(y)) {
            return null;
        }
        AABB box = this.getDimensions(this.getPose()).makeBoundingBox(x, y, z);
        AABB ceiling = this.getBoundingBox().expandTowards(0.0D, LEAP_MIN_APEX, 0.0D);
        if (!level.noBlockCollision(this, box) || !level.noBlockCollision(this, ceiling)) {
            return null;
        }
        return new Vec3(x, y, z);
    }

    private Vec3 leapPoint(int t) {
        Vec3 apex = this.leapFrom.lerp(this.leapTo, 0.3D);
        if (t < LEAP_APEX_TICK) {
            double k = (t - LEAP_LOCK_TICK) / (double) (LEAP_APEX_TICK - LEAP_LOCK_TICK);
            Vec3 p = this.leapFrom.lerp(apex, k);
            return new Vec3(p.x, Mth.lerp(Math.sin(k * Math.PI / 2.0D), this.leapFrom.y, this.leapApex), p.z);
        }
        if (t < LEAP_DIVE_TICK) {
            return new Vec3(apex.x, this.leapApex, apex.z);
        }
        double k = (t - LEAP_DIVE_TICK + 1) / (double) (LEAP_LAND_TICK - LEAP_DIVE_TICK);
        Vec3 p = apex.lerp(this.leapTo, k);
        return new Vec3(p.x, Mth.lerp(k * k, this.leapApex, this.leapTo.y), p.z);
    }

    private void endAirborne() {
        if (this.airborne) {
            this.airborne = false;
            this.setNoGravity(false);
            this.noPhysics = false;
            this.fallDistance = 0.0D;
        }
    }

    public boolean isAirborne() {
        return this.airborne;
    }

    /** The topmost block top at or below {@code fromY} (within 8 blocks), or NaN when there is none. */
    private static double groundBelow(ServerLevel level, double x, double fromY, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(x), Mth.floor(fromY), Mth.floor(z));
        for (int i = 0; i < 8; i++) {
            if (level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP)) {
                return pos.getY() + 1.0D;
            }
            pos.move(Direction.DOWN);
        }
        return Double.NaN;
    }

    private static void runeCircle(ServerLevel level, Vec3 c, double radius) {
        for (int k = 0; k < 12; k++) {
            double a = k * Math.PI / 6.0D;
            level.sendParticles(RUNE_DUST, c.x + Math.cos(a) * radius, c.y + 0.1D, c.z + Math.sin(a) * radius,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    // ================================================================= G: parry and reprise (spec §11.3)
    /**
     * Raises the parry against {@code attacker} if the rules allow it: from idle or guard only, the attacker in front
     * (60 degrees each side), the cooldown of the phase spent and no more than two parries in a row.
     */
    public boolean startParry(LivingEntity attacker) {
        State s = this.getState();
        if (s != State.COMBAT_IDLE && s != State.GUARDING) {
            return false;
        }
        if (this.parryCooldown > 0 || !this.parryChainAllows()) {
            return false;
        }
        double angle = ArchitectStrikes.angleOf(attacker.getX() - this.getX(), attacker.getZ() - this.getZ(), this.getYRot());
        if (Math.abs(angle) > GUARD_HALF_ARC) {
            return false;
        }
        this.parryLeft = angle > 0.0D;
        this.parried = false;
        this.parryAttacker = null;
        this.setLockedYaw(yawTowards(this, attacker));
        this.face(this.lockedYaw);
        this.parryCooldown = PARRY_COOLDOWN[this.reachedPhase.ordinal()];
        this.hitsByVictim.clear();
        this.getNavigation().stop();
        this.setState(State.PARRYING);
        this.setAnim(this.parryLeft ? ArchitectAnim.PARRY_LEFT : ArchitectAnim.PARRY_RIGHT);
        return true;
    }

    /** Spec §11.3: at most two parries in a row; the third frontal blow within 60 ticks goes through. */
    private boolean parryChainAllows() {
        if (this.tickCount - this.lastParryTick > PARRY_CHAIN_WINDOW) {
            this.parriesInRow = 0;
        }
        return this.parriesInRow < PARRY_MAX_IN_ROW;
    }

    public boolean isParryWindow() {
        return this.getState() == State.PARRYING && this.stateTicks >= PARRY_WINDOW_START && this.stateTicks <= PARRY_WINDOW_END;
    }

    private void tickParry(ServerLevel level) {
        int t = this.stateTicks;
        if (!this.parried) {
            if (t >= PARRY_FAIL_TICK) {
                this.setState(State.COMBAT_IDLE); // nobody struck in the window: back to guard, never waits
            }
            return;
        }
        Strike reprise = ArchitectAttack.PARRY_REPRISE.strikes.getFirst();
        if (reprise.liveAt(t, false) && this.parryAttacker != null) {
            if (t == reprise.start()) {
                this.strikeTrail(level, reprise);
            }
            this.applyStrike(level, ArchitectAttack.PARRY_REPRISE, 0, reprise, this.parryAttacker);
        }
        if (t >= ArchitectAnim.PARRY_LEFT.ticks) {
            this.setState(State.COMBAT_IDLE);
            this.globalCooldown = PARRY_VULNERABLE_TICKS; // no guard right after the reprise: a real opening
        }
    }

    // ================================================================= damage, guard, posture, stagger
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.GENERIC_KILL)) {
            return super.hurtServer(level, source, amount);
        }
        State state = this.getState();
        if (state == State.DORMANT) {
            if (source.getEntity() instanceof Player) {
                this.awaken();
            }
            return false;
        }
        if (state == State.AWAKENING || state == State.PHASE_TRANSITION || state == State.DYING) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof Architect) {
            return false;
        }
        Vec3 from = source.getSourcePosition();
        double angle = from == null ? 0.0D
                : ArchitectStrikes.angleOf(from.x - this.getX(), from.z - this.getZ(), this.getYRot());
        boolean frontal = from != null && Math.abs(angle) <= GUARD_HALF_ARC;
        boolean melee = attacker instanceof LivingEntity && source.getDirectEntity() == attacker;
        if (state == State.PARRYING && this.isParryWindow() && melee && frontal && !this.parried) {
            if (amount >= POSTURE_HEAVY_HIT * 2.0F) {
                this.posture = POSTURE_MAX; // a crushing blow breaks the parry (spec §11.3)
                this.stagger();
            } else {
                this.landParry(level, (LivingEntity) attacker);
                return false;
            }
        }
        float dealt = amount;
        if (state == State.GUARDING && frontal) {
            dealt *= 1.0F - GUARD_REDUCTION;
        }
        if (this.wingsOpenInRecovery() && from != null && Math.abs(angle) >= BACK_ANGLE) {
            dealt *= WINGS_OPEN_BACK_BONUS;
        }
        boolean strong = source.getDirectEntity() instanceof Projectile p && !isLightProjectile(p);
        boolean hurt = super.hurtServer(level, source, dealt);
        if (!hurt || !this.isAlive()) {
            return hurt;
        }
        this.recentHits = this.tickCount - this.lastHurtTick <= 20 ? this.recentHits + 1 : 1;
        this.lastHurtTick = this.tickCount;
        this.addPosture(dealt, strong);
        this.noteAggression(attacker, dealt, melee && frontal);
        return true;
    }

    private void landParry(ServerLevel level, LivingEntity attacker) {
        this.parried = true;
        this.parryAttacker = attacker;
        this.parries++;
        this.parriesInRow++;
        this.lastParryTick = this.tickCount;
        level.broadcastEntityEvent(this, EVENT_PARRY);
        Vec3 f = Vec3.directionFromRotation(0.0F, this.lockedYaw);
        level.sendParticles(MANA_DUST, this.getX() + f.x * 1.2D, this.getY() + 2.0D, this.getZ() + f.z * 1.2D,
                8, 0.15D, 0.15D, 0.15D, 0.1D);
        this.playSound(ModSounds.ARCHITECT_PARRY.get(), 1.0F, 1.0F);
    }

    /** C and F leave his back open with the wings spread during their recovery (spec §11.6). */
    private boolean wingsOpenInRecovery() {
        ArchitectAttack a = this.attack;
        return this.getState() == State.ATTACKING && a != null && this.stateTicks >= a.recoveryStart
                && (a == ArchitectAttack.CARTENON_WHEEL || a == ArchitectAttack.GARGOYLE_FALL);
    }

    /**
     * Spec §11.5: heavy blows fill posture with their damage, light ones with a quarter (and never finish it alone),
     * strong projectiles count double. A full posture staggers him, but not inside the 200-tick immunity, and not in the
     * middle of a blow already swinging (it waits for the attack to end).
     */
    private void addPosture(float damage, boolean strong) {
        boolean heavy = damage >= POSTURE_HEAVY_HIT;
        float gain = damage * (heavy ? 1.0F : POSTURE_WEAK_FACTOR) * (strong ? 2.0F : 1.0F);
        float cap = heavy || strong ? POSTURE_MAX : POSTURE_MAX * 0.99F;
        this.posture = Math.min(cap, Math.max(this.posture, Math.min(cap, this.posture + gain)));
        if (this.posture < POSTURE_MAX || this.staggerImmunity > 0) {
            return;
        }
        ArchitectAttack a = this.attack;
        if (this.getState() == State.ATTACKING && a != null && this.stateTicks >= a.firstActiveTick) {
            this.staggerPending = true;
            return;
        }
        this.stagger();
    }

    /** A player's Architect's Scimitar parried one of his blows: a heavy hit on his posture (spec §26.2). */
    public void receiveParry() {
        this.addPosture(POSTURE_HEAVY_HIT, false);
    }

    /** Breaks his guard: the current attack is cancelled, the sternum is exposed for 30 ticks. */
    public void stagger() {
        this.endAirborne();
        this.attack = null;
        this.staggerPending = false;
        this.posture = 0.0F;
        this.staggerImmunity = STAGGER_IMMUNITY_TICKS;
        this.staggers++;
        this.getNavigation().stop();
        this.setState(State.STAGGERED);
        this.setAnim(ArchitectAnim.STAGGER);
        this.level().broadcastEntityEvent(this, EVENT_STAGGER);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ERROR_DUST, this.getX(), this.getY() + STERNUM_Y, this.getZ(), 14, 0.3D, 0.3D, 0.3D, 0.05D);
        }
        this.playSound(ModSounds.ARCHITECT_STAGGER.get(), 1.0F, 1.0F);
    }

    /** Raises the full guard for {@code ticks} (spec §11.2): frontal damage halved, nothing behind covered. */
    public void raiseGuard(int ticks) {
        this.guardTicks = ticks;
        this.getNavigation().stop();
        this.setState(State.GUARDING);
        this.setAnim(ArchitectAnim.GUARD_FULL);
    }

    @Override
    public ProjectileDeflection deflection(Projectile projectile) {
        if (this.level().isClientSide() || !isLightProjectile(projectile) || this.deflectCooldown > 0) {
            return super.deflection(projectile);
        }
        State s = this.getState();
        if (s != State.COMBAT_IDLE && s != State.GUARDING) {
            return super.deflection(projectile);
        }
        double angle = ArchitectStrikes.angleOf(projectile.getX() - this.getX(), projectile.getZ() - this.getZ(), this.getYRot());
        if (Math.abs(angle) > GUARD_HALF_ARC) {
            return super.deflection(projectile);
        }
        this.deflectCooldown = DEFLECT_COOLDOWN;
        this.deflections++;
        this.raiseGuard(ArchitectAnim.PROJECTILE_DEFLECT.ticks);
        this.setAnim(ArchitectAnim.PROJECTILE_DEFLECT);
        this.playSound(ModSounds.ARCHITECT_DEFLECT.get(), 1.0F, 1.0F);
        return ProjectileDeflection.REVERSE;
    }

    /** Common arrows, snowballs and eggs; tridents, piercing bolts and fireballs are strong (spec §11.4). */
    public static boolean isLightProjectile(Projectile p) {
        if (p instanceof ThrownTrident) {
            return false;
        }
        if (p instanceof AbstractArrow arrow) {
            return arrow.getPierceLevel() == 0;
        }
        return p instanceof Snowball || p instanceof ThrownEgg;
    }

    // ================================================================= phase transitions (spec §12.2, §15.3)
    private void checkPhase(ServerLevel level) {
        if (this.isDormant()) {
            return;
        }
        Phase now = this.getPhase();
        if (now.ordinal() <= this.reachedPhase.ordinal()) {
            return;
        }
        State s = this.getState();
        if (s != State.COMBAT_IDLE && s != State.GUARDING && s != State.REPOSITION) {
            return; // the transition waits for the current attack, parry or stagger to end
        }
        this.reachedPhase = now;
        this.posture = 0.0F;
        this.getNavigation().stop();
        this.setState(State.PHASE_TRANSITION);
        this.setAnim(now == Phase.ADMINISTRATOR ? ArchitectAnim.PHASE_ENRAGE : ArchitectAnim.MANIACAL_LAUGH);
        level.broadcastEntityEvent(this, EVENT_ENRAGE);
        this.playSound(ModSounds.ARCHITECT_ENRAGE.get(), 1.0F, 1.0F);
        level.sendParticles(MANA_DUST, this.getX(), this.getY() + STERNUM_Y, this.getZ(), 24, 0.6D, 0.6D, 0.6D, 0.12D);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(ENRAGE_PUSH_RADIUS), this.foeInLeash)) {
            this.throwAway(e, 0.8D, 0.3D); // a light push, no damage
        }
    }

    private void tickTransition() {
        int t = this.stateTicks;
        if (this.getAnim() == ArchitectAnim.MANIACAL_LAUGH && (t == 8 || t == 16 || t == 24)) {
            this.playSound(ModSounds.ARCHITECT_LAUGH.get(), 1.0F, 1.0F);
        }
        if (this.stateTicks >= this.getAnim().ticks) {
            this.setState(State.COMBAT_IDLE);
        }
    }

    // ================================================================= the brain (decisions only)
    /**
     * One decision per tick while he is free (COMBAT_IDLE or GUARDING). The order is the priority:
     * parry, anti-pillaring, evasion, chain or counter blow, a normal blow, then footwork. Each layer either takes the
     * tick (and changes the state, so the next tick is the timeline of that state) or hands it down.
     */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        State s = this.getState();
        if (s != State.COMBAT_IDLE && s != State.GUARDING) {
            if (this.isRooted()) {
                this.getNavigation().stop();
            }
            return;
        }
        LivingEntity target = this.selectTarget(level);
        if (target == null) {
            this.rest(level);
            return;
        }
        this.noTargetTicks = 0;
        if (s == State.COMBAT_IDLE) {
            this.decide(level, target);
        }
        this.turnBodyToward(target);
    }

    /** Body follows the target at TURN_DEGREES_PER_TICK, unless he is walking a path that bends away from it. */
    private void turnBodyToward(LivingEntity target) {
        State s = this.getState();
        if (s != State.COMBAT_IDLE && s != State.GUARDING) {
            return;
        }
        float wanted = yawTowards(this, target);
        boolean pathing = !this.getNavigation().isDone();
        if (pathing && Math.abs(Mth.wrapDegrees(wanted - this.getYRot())) > 70.0F) {
            return; // going around an obstacle: the move control aims the body along the path
        }
        this.turnBody(wanted, TURN_DEGREES_PER_TICK);
    }

    private void decide(ServerLevel level, LivingEntity target) {
        double dist = horizontalDistance(this, target);
        double dy = target.getY() - this.getY();
        double angle = ArchitectStrikes.angleOf(target.getX() - this.getX(), target.getZ() - this.getZ(), this.getYRot());
        if (this.tactic != Tactic.READ) {
            this.tacticTicks++;
        } else if (this.tacticTicks++ >= this.tacticLimit) {
            this.tactic = Tactic.ADVANCE;
            this.tacticTicks = 0;
        }
        if (this.chainTicks > 0 && --this.chainTicks == 0) {
            this.chain.clear();
        }
        // 1. the parry answers a foe that just struck him from the front
        if (this.tickCount - this.lastAggressionTick <= PARRY_REACTION_TICKS && target.getId() == this.aggressorId
                && dist <= SHORT_RANGE + 0.3D && this.startParry(target)) {
            return;
        }
        // 2. a target perched out of reach
        if (this.tickPillaring(level, target, dist, dy)) {
            return;
        }
        // 3. evasion: reads the foe's charged blow, a dangerous projectile or a beating
        if (this.tryReactiveDodge(level, target, dist, angle)) {
            return;
        }
        // 4. never a frontal blow without facing the target: the body turns first (the head already follows)
        boolean facing = Math.abs(angle) <= ATTACK_FACING_DEGREES;
        if (this.globalCooldown <= 0 && facing) {
            ArchitectAttack pick = null;
            if (this.isPressuringShield() && dist <= SHORT_RANGE && this.cooldowns[ArchitectAttack.SHIELD_BREAKER.ordinal()] <= 0
                    && this.random.nextFloat() < 0.7F) {
                pick = ArchitectAttack.SHIELD_BREAKER; // a player who lives behind the shield gets the blow built for it
            }
            if (pick == null && !this.chain.isEmpty()) {
                pick = this.pickChained(level, target, dist, dy);
            }
            if (pick == null) {
                if (dist <= CLOSE_RANGE && this.globalCooldown <= 0 && this.random.nextFloat() < 0.15F && this.dodgeCooldown <= 0
                        && this.startBackstepIfRoom(level)) {
                    return;
                }
                pick = this.pickAttack(level, target, dist, dy);
            }
            if (pick != null) {
                this.chain.clear();
                this.chainTicks = 0;
                this.startAttack(pick, target);
                return;
            }
        }
        if (dist <= SHORT_RANGE && this.globalCooldown <= 0 && this.counterTicks <= 0 && this.random.nextFloat() < 0.03F) {
            this.raiseGuard(GUARD_TICKS);
            return;
        }
        // 5. footwork: never stands still without a reason
        this.position(level, target, dist);
    }

    // ---------------------------------------------------------------- reading the opponent
    /** Shield pressure: grows while the target blocks, shrinks when it does not. */
    private void watchShield(LivingEntity target) {
        if (target.isBlocking()) {
            this.shieldScore = Math.min(120, this.shieldScore + 2);
        } else if (this.shieldScore > 0) {
            this.shieldScore--;
        }
    }

    public boolean isPressuringShield() {
        return this.shieldScore >= SHIELD_PRESSURE_THRESHOLD;
    }

    /** A foe that could land a blow on him in a moment: a charged melee attacker close in front of him. */
    private boolean foeAboutToStrike(ServerLevel level) {
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(SHORT_RANGE + 1.5D, 2.0D, SHORT_RANGE + 1.5D), this.foeInLeash)) {
            if (!(e instanceof Player p) || horizontalDistance(this, p) > SHORT_RANGE + 0.5D) {
                continue;
            }
            double toMe = ArchitectStrikes.angleOf(this.getX() - p.getX(), this.getZ() - p.getZ(), p.getYRot());
            if (p.getAttackStrengthScale(0.0F) >= 0.9F && Math.abs(toMe) <= 50.0D) {
                return true;
            }
        }
        return false;
    }

    /** The nearest dangerous projectile (not the light ones he deflects) flying at him, or null. */
    private @Nullable Projectile incomingProjectile(ServerLevel level) {
        for (Projectile p : level.getEntitiesOfClass(Projectile.class, this.getBoundingBox().inflate(14.0D, 6.0D, 14.0D))) {
            if (isLightProjectile(p) || p.getOwner() == this) {
                continue;
            }
            Vec3 v = p.getDeltaMovement();
            Vec3 toMe = this.position().add(0.0D, this.getBbHeight() / 2.0D, 0.0D).subtract(p.position());
            double speed = v.length();
            if (speed < 0.3D || toMe.length() > 14.0D || v.normalize().dot(toMe.normalize()) < 0.92D) {
                continue;
            }
            if (toMe.length() / speed <= 12.0D) {
                return p;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- evasion
    /**
     * Evasion (reform §2.1.D). Triggers, read every 5 ticks: a charged foe about to strike; a dangerous projectile on
     * its way; two hits taken inside a second. A draw against the phase's chance, a cooldown and a budget of two dodges
     * in a row (paid back by throwing a blow) keep him hittable: he dodges the reflex, not the whole fight.
     */
    private boolean tryReactiveDodge(ServerLevel level, LivingEntity target, double dist, double angle) {
        if (this.dodgeCooldown > 0 || this.dodgesInRow >= MAX_DODGES_IN_ROW || this.reachedPhase == Phase.MONUMENT) {
            return false;
        }
        Projectile incoming = this.tickCount % 2 == 0 ? this.incomingProjectile(level) : null;
        boolean slow = this.tickCount % 5 == 0;
        boolean battered = slow && this.recentHits >= 2;
        boolean about = slow && incoming == null && this.foeAboutToStrike(level);
        if (incoming == null && !battered && !about) {
            return false;
        }
        float chance = DODGE_CHANCE[this.reachedPhase.ordinal()];
        if (incoming != null) {
            chance = 0.75F;
        } else if (battered) {
            chance = Math.min(0.9F, chance * 2.0F);
        } else if (this.globalCooldown <= 0 && Math.abs(angle) <= ATTACK_FACING_DEGREES) {
            chance *= 0.5F; // he would rather strike first
        }
        if (this.random.nextFloat() >= chance) {
            return false;
        }
        Dodge pick = this.chooseDodge(level, target, dist, incoming != null);
        if (pick == null) {
            return false;
        }
        this.startDodge(pick, target);
        return true;
    }

    private @Nullable Dodge chooseDodge(ServerLevel level, LivingEntity target, double dist, boolean projectile) {
        float[] w = new float[Dodge.values().length];
        for (Dodge d : Dodge.values()) {
            float weight;
            if (projectile) {
                weight = d == Dodge.LEFT || d == Dodge.RIGHT ? 3.0F : d.isLean() ? 1.5F : d == Dodge.BACK ? 0.0F : 1.0F;
            } else if (dist <= CLOSE_RANGE) {
                weight = d == Dodge.BACK ? 3.0F : d == Dodge.DIAG_LEFT || d == Dodge.DIAG_RIGHT ? 2.5F : d.isLean() ? 0.5F : 1.5F;
            } else {
                weight = d == Dodge.LEFT || d == Dodge.RIGHT ? 3.0F : d == Dodge.DIAG_LEFT || d == Dodge.DIAG_RIGHT ? 2.0F
                        : d.isLean() ? 1.0F : 1.5F;
            }
            if (this.lastDodge != null && d == this.lastDodge) {
                weight *= 0.4F; // never the same step twice: no pattern to learn
            }
            if (weight > 0.0F && !this.dodgeFeasible(level, d)) {
                weight = 0.0F;
            }
            w[d.ordinal()] = weight;
        }
        float total = 0.0F;
        for (float v : w) {
            total += v;
        }
        if (total <= 0.0F) {
            return null;
        }
        float roll = this.random.nextFloat() * total;
        for (Dodge d : Dodge.values()) {
            roll -= w[d.ordinal()];
            if (w[d.ordinal()] > 0.0F && roll <= 0.0F) {
                return d;
            }
        }
        return null;
    }

    /** The step vector (blocks) of a dodge in the world, for a body facing {@code yaw}. */
    private static Vec3 dodgeVector(Dodge d, float yaw) {
        double a = Math.toRadians(d.angle);
        double fwd = Math.cos(a) * d.distance;
        double left = Math.sin(a) * d.distance;
        double rad = yaw * Mth.DEG_TO_RAD;
        return new Vec3(-Math.sin(rad) * fwd + Math.cos(rad) * left, 0.0D, Math.cos(rad) * fwd + Math.sin(rad) * left);
    }

    /** Room to dodge: a clear body-sized path, solid ground at the end (no cliff, no lava) and inside the leash. */
    private boolean dodgeFeasible(ServerLevel level, Dodge d) {
        Vec3 v = dodgeVector(d, this.getYRot());
        for (int i = 1; i <= 4; i++) {
            Vec3 at = this.position().add(v.scale(i / 4.0D));
            if (!this.canStandAt(level, at)) {
                return false;
            }
        }
        Vec3 end = this.position().add(v);
        return this.inLeash(end);
    }

    private boolean inLeash(Vec3 p) {
        return !this.hasAnchor || p.distanceToSqr(this.anchorX, this.anchorY, this.anchorZ) <= LEASH_RADIUS * LEASH_RADIUS;
    }

    /** Whether the body fits at {@code at} (feet position) with ground within one step and no lava or fire below. */
    private boolean canStandAt(ServerLevel level, Vec3 at) {
        AABB box = this.getDimensions(this.getPose()).makeBoundingBox(at.x, at.y + 0.05D, at.z);
        if (!level.noBlockCollision(this, box)) {
            return false;
        }
        double ground = groundBelow(level, at.x, at.y + 0.6D, at.z);
        if (Double.isNaN(ground) || at.y - ground > 1.6D || ground - at.y > STEP_HEIGHT) {
            return false;
        }
        BlockPos below = BlockPos.containing(at.x, ground - 0.5D, at.z);
        return !level.getFluidState(below).is(net.minecraft.tags.FluidTags.LAVA)
                && !level.getFluidState(below.above()).is(net.minecraft.tags.FluidTags.LAVA)
                && !level.getBlockState(below.above()).is(net.minecraft.tags.BlockTags.FIRE);
    }

    /** Starts the evasive move: public so the GameTests can drive it with the AI off. */
    public void startDodge(Dodge d, LivingEntity target) {
        this.dodge = d;
        this.setLockedYaw(this.isNoAi() || target == null ? this.getYRot() : yawTowards(this, target));
        this.face(this.lockedYaw);
        Vec3 v = dodgeVector(d, this.lockedYaw);
        this.dodgeStepX = v.x;
        this.dodgeStepZ = v.z;
        this.dodgeTarget = target;
        this.getNavigation().stop();
        this.setState(State.DODGING);
        this.setAnim(d.anim);
        this.dodges++;
        this.lastDodge = d;
        this.dodgeCooldown = DODGE_COOLDOWN[this.reachedPhase.ordinal()];
        if (this.level() instanceof ServerLevel level) {
            level.broadcastEntityEvent(this, EVENT_DODGE);
        }
        this.playSound(ModSounds.ARCHITECT_WING_FLAP.get(), 0.8F, 1.2F);
    }

    /**
     * The dodge: DODGE_STARTUP ticks of anticipation (the clip's lean), then the body slides along the vector with an
     * ease-out over DODGE_MOVE_TICKS (a lean: 4), then the clip settles. No teleport, no invulnerability: the escape is
     * the distance. Afterwards the counter window opens (COUNTER_WINDOW ticks, global cooldown nearly spent).
     */
    private void tickDodge(ServerLevel level) {
        Dodge d = this.dodge;
        if (d == null) {
            this.setState(State.COMBAT_IDLE);
            return;
        }
        int t = this.stateTicks;
        int moveTicks = d.isLean() ? 4 : DODGE_MOVE_TICKS;
        int start = d.isLean() ? 2 : DODGE_STARTUP;
        if (t >= start && t < start + moveTicks) {
            int i = t - start;
            double weight = (moveTicks - i) / (moveTicks * (moveTicks + 1) / 2.0D); // 8,7,6,...,1 over the total
            double sx = this.dodgeStepX * weight;
            double sz = this.dodgeStepZ * weight;
            Vec3 next = this.position().add(sx, 0.0D, sz);
            if (this.canStandAt(level, next)) {
                this.move(MoverType.SELF, new Vec3(sx, 0.0D, sz));
            }
            if (t % 2 == 0) {
                level.sendParticles(STONE_DUST, this.getX(), this.getY() + 0.1D, this.getZ(), 3, 0.5D, 0.05D, 0.5D, 0.01D);
            }
        }
        if (t >= d.anim.ticks) {
            this.dodge = null;
            this.dodgesInRow++;
            this.counterTicks = COUNTER_WINDOW;
            this.globalCooldown = Math.min(this.globalCooldown, 3);
            this.pause(2, 6);
            this.setState(State.COMBAT_IDLE);
        }
    }

    // ---------------------------------------------------------------- choosing the blow
    /** Weighted choice by position and by what the target is doing (spec §12.3 and reform §12.5), with anti-repetition. */
    private @Nullable ArchitectAttack pickAttack(ServerLevel level, LivingEntity target, double dist, double dy) {
        java.util.Arrays.fill(this.weights, 0.0F);
        boolean inAir = isInAir(target) && dy < PILLAR_MIN_HEIGHT;
        boolean shield = this.isPressuringShield();
        int crowd = this.foesWithin(level, SIEGE_RADIUS);
        double lateral = lateralSpeed(this, target);
        if (inAir && dist <= SHORT_RANGE) {
            this.weigh(ArchitectAttack.SPLITTER_CRESCENT, 3.5F);
            this.weigh(ArchitectAttack.GARGOYLE_FALL, 2.0F);
        } else if (dist <= CLOSE_RANGE) {
            this.weigh(ArchitectAttack.SPLITTER_CRESCENT, 3.0F);
            this.weigh(ArchitectAttack.SIXFOLD_EDICT, 2.5F);
            this.weigh(ArchitectAttack.FALSE_VERDICT, 1.5F);
        } else if (dist <= SHORT_RANGE) {
            this.weigh(ArchitectAttack.SIXFOLD_EDICT, 3.0F);
            this.weigh(ArchitectAttack.CROSSED_MANDATE, 2.5F);
            this.weigh(ArchitectAttack.SPLITTER_CRESCENT, 2.0F);
            this.weigh(ArchitectAttack.FALSE_VERDICT, 2.0F);
            this.weigh(ArchitectAttack.RUSHING_REBUKE, dist >= SHORT_RANGE * 0.75D ? 1.5F : 0.0F);
        } else if (dist <= MEDIUM_RANGE) {
            this.weigh(ArchitectAttack.PIERCING_SENTENCE, isRetreating(this, target) ? 5.0F : 3.0F);
            this.weigh(ArchitectAttack.RUSHING_REBUKE, 3.5F);
            this.weigh(ArchitectAttack.GARGOYLE_FALL, dist >= 3.0D * SCALE && dist <= LEAP_MAX_HORIZONTAL + 1.0D ? 2.0F : 0.0F);
        }
        if (shield && dist <= SHORT_RANGE) {
            // a player living behind the shield: the heavy blow built to open it, and the feint that baits the raise
            this.weigh(ArchitectAttack.SHIELD_BREAKER, 9.0F);
            this.weigh(ArchitectAttack.FALSE_VERDICT, 3.0F);
            this.weights[ArchitectAttack.SIXFOLD_EDICT.ordinal()] *= 0.5F;
        }
        if (lateral > 0.12D && dist <= SHORT_RANGE) { // circling him: the wide, sweeping blows
            this.weigh(ArchitectAttack.CROSSED_MANDATE, 2.0F);
            this.weigh(ArchitectAttack.SPLITTER_CRESCENT, 1.0F);
        }
        if (crowd >= 2 && dist <= 3.4D * SCALE) {
            this.weigh(ArchitectAttack.CARTENON_WHEEL, 4.0F); // anti-siege
        }
        if (this.counterTicks > 0) { // right after a dodge: the fast blows and the gap closers
            for (ArchitectAttack a : new ArchitectAttack[] {ArchitectAttack.SIXFOLD_EDICT, ArchitectAttack.CROSSED_MANDATE,
                    ArchitectAttack.PIERCING_SENTENCE, ArchitectAttack.RUSHING_REBUKE}) {
                this.weights[a.ordinal()] *= 1.6F;
            }
        }
        float total = 0.0F;
        for (ArchitectAttack a : ArchitectAttack.values()) {
            int i = a.ordinal();
            if (this.weights[i] <= 0.0F || !this.allowed(level, a, target, false)) {
                this.weights[i] = 0.0F;
                continue;
            }
            if (a == this.lastAttack) {
                this.weights[i] = 0.0F; // never the same attack twice in a row
                continue;
            }
            for (ArchitectAttack recent : this.recentAttacks) {
                if (recent == a) {
                    this.weights[i] *= 0.5F; // each use in the last four halves the weight again
                }
            }
            total += this.weights[i];
        }
        if (total <= 0.0F) {
            return null;
        }
        float roll = this.random.nextFloat() * total;
        for (ArchitectAttack a : ArchitectAttack.values()) {
            roll -= this.weights[a.ordinal()];
            if (this.weights[a.ordinal()] > 0.0F && roll <= 0.0F) {
                return a;
            }
        }
        return null;
    }

    /** The follow-up armed by the blow that just ended: the first valid one, re-checked against where the target is now. */
    private @Nullable ArchitectAttack pickChained(ServerLevel level, LivingEntity target, double dist, double dy) {
        java.util.List<ArchitectAttack> options = new java.util.ArrayList<>(this.chain);
        java.util.Collections.shuffle(options, new java.util.Random(this.random.nextLong()));
        for (ArchitectAttack a : options) {
            if (this.fitsRange(a, dist) && this.allowed(level, a, target, true)) {
                return a;
            }
        }
        return null;
    }

    private boolean fitsRange(ArchitectAttack a, double dist) {
        return switch (a) {
            case SIXFOLD_EDICT, CROSSED_MANDATE, SPLITTER_CRESCENT, FALSE_VERDICT -> dist <= SHORT_RANGE + 0.5D;
            case SHIELD_BREAKER -> dist <= SHORT_RANGE;
            case PIERCING_SENTENCE -> dist <= MEDIUM_RANGE;
            case RUSHING_REBUKE -> dist >= SHORT_RANGE * 0.7D && dist <= MEDIUM_RANGE;
            default -> false;
        };
    }

    private void weigh(ArchitectAttack a, float w) {
        this.weights[a.ordinal()] += w;
    }

    private boolean allowed(ServerLevel level, ArchitectAttack a, LivingEntity target, boolean chained) {
        if (a == ArchitectAttack.PARRY_REPRISE) {
            return false;
        }
        if (this.cooldowns[a.ordinal()] > 0 && !this.comboArmed && !chained) {
            return false;
        }
        if (this.reachedPhase.ordinal() < a.minPhase.ordinal()) {
            return false;
        }
        if (a == ArchitectAttack.PIERCING_SENTENCE && this.reachedPhase == Phase.DUELIST
                && this.getHealth() / this.getMaxHealth() > PIERCING_UNLOCK_HEALTH) {
            return false;
        }
        if (a == ArchitectAttack.CARTENON_WHEEL) {
            return level.noBlockCollision(this, this.getBoundingBox().inflate(WHEEL_CLEARANCE, 0.0D, WHEEL_CLEARANCE).move(0.0D, 0.1D, 0.0D));
        }
        if (a == ArchitectAttack.GARGOYLE_FALL) {
            return this.landingFor(level, target) != null;
        }
        if (a == ArchitectAttack.RUSHING_REBUKE) {
            return this.runwayClear(level, RUSH_DISTANCE);
        }
        return true;
    }

    /** The straight line ahead (the body's facing) is walkable for {@code distance} blocks: the charge never runs off an edge. */
    private boolean runwayClear(ServerLevel level, double distance) {
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot());
        for (int i = 1; i <= 4; i++) {
            if (!this.canStandAt(level, this.position().add(f.scale(distance * i / 4.0D)))) {
                return false;
            }
        }
        return true;
    }

    /** Anti-pillaring (MOB_CONTRACT §4.4): a target perched 3+ blocks up for 3 s gets the Gargoyle Fall, or he backs off. */
    private boolean tickPillaring(ServerLevel level, LivingEntity target, double dist, double dy) {
        if (dy < PILLAR_MIN_HEIGHT || dist > LEAP_MAX_HORIZONTAL + 3.0D) {
            this.pillarTicks = 0;
            return false;
        }
        this.getNavigation().stop(); // never grind against the foot of the pillar
        if (++this.pillarTicks < PILLAR_TICKS || this.globalCooldown > 0) {
            return true;
        }
        this.pillarTicks = 0;
        if (this.cooldowns[ArchitectAttack.GARGOYLE_FALL.ordinal()] == 0 && this.landingFor(level, target) != null) {
            this.startAttack(ArchitectAttack.GARGOYLE_FALL, target);
        } else {
            this.raiseGuard(GUARD_TICKS);
        }
        return true;
    }

    // ---------------------------------------------------------------- footwork
    /**
     * Footwork between blows (reform §2.1): close the distance when far, slide around the target inside the fighting
     * band, back away when crowded. After a blow or a dodge he first reads the fight for a few ticks (a poise, never
     * longer than READ_MAX_TICKS); afterwards he is always doing one of the three.
     */
    private void position(ServerLevel level, LivingEntity target, double dist) {
        if (this.tactic == Tactic.READ) {
            this.getNavigation().stop();
            this.idleTicks++;
            if (this.idleTicks > READ_MAX_TICKS) { // the guard that nothing keeps him still
                this.pause(0, 0);
            }
            return;
        }
        this.idleTicks = 0;
        boolean wantsToEngage = this.globalCooldown <= 0;
        Tactic wanted;
        if (dist < CLOSE_RANGE - 0.3D) {
            wanted = Tactic.RETREAT;
        } else if (dist > CLOSE_RANGE + 0.8D || wantsToEngage) {
            wanted = Tactic.ADVANCE;
        } else {
            wanted = Tactic.CIRCLE;
        }
        if (wanted != this.tactic) {
            this.tactic = wanted;
            this.tacticTicks = 0;
            this.getNavigation().stop();
        }
        float toward = yawTowards(this, target);
        switch (this.tactic) {
            case ADVANCE -> this.advance(target, dist);
            case CIRCLE -> {
                if (this.tickCount >= this.sideSwitchTick) {
                    this.strafeSide = this.random.nextInt(5) == 0 ? this.strafeSide : -this.strafeSide;
                    this.sideSwitchTick = this.tickCount + 18 + this.random.nextInt(34);
                }
                double radial = dist > SHORT_RANGE * 0.9D ? 0.05D : dist < CLOSE_RANGE + 0.5D ? -0.05D : 0.0D;
                if (!this.moveSafely(level, toward, radial, this.strafeSide * STRAFE_SPEED)) {
                    this.strafeSide = -this.strafeSide; // wall or edge on that side: the other way
                    this.sideSwitchTick = this.tickCount + 18;
                }
            }
            case RETREAT -> {
                if (!this.moveSafely(level, toward, -BACKPEDAL_SPEED, this.strafeSide * 0.04D)) {
                    this.tactic = Tactic.CIRCLE; // cornered: stop backing and fight from here
                }
            }
            default -> {
            }
        }
    }

    private void advance(LivingEntity target, double dist) {
        if (this.tickCount % 10 == 0 || this.getNavigation().isDone()) {
            boolean ok = this.getNavigation().moveTo(target, dist > CLOSE_RANGE + 0.8D ? RUN_SPEED : WALK_SPEED);
            this.unreachableTicks = ok ? 0 : this.unreachableTicks + 10;
        }
        if (this.unreachableTicks >= 40) { // no path (a wall, a gap): hold the guard instead of grinding on it
            this.unreachableTicks = 0;
            this.raiseGuard(GUARD_TICKS);
        }
    }

    /** Enters the reading poise for {@code min + random(spread)} ticks. */
    private void pause(int min, int spread) {
        this.tactic = Tactic.READ;
        this.tacticTicks = 0;
        this.idleTicks = 0;
        this.tacticLimit = min + (spread > 0 ? this.random.nextInt(spread + 1) : 0);
    }

    /**
     * One by-hand step relative to the body facing {@code yaw}: {@code forward} and {@code left} in blocks. False when the
     * spot is not standable (wall, ledge, lava): the move is refused, never half done.
     */
    private boolean moveSafely(ServerLevel level, float yaw, double forward, double left) {
        double rad = yaw * Mth.DEG_TO_RAD;
        double dx = -Math.sin(rad) * forward + Math.cos(rad) * left;
        double dz = Math.cos(rad) * forward + Math.sin(rad) * left;
        if (dx == 0.0D && dz == 0.0D) {
            return true;
        }
        Vec3 next = this.position().add(dx, 0.0D, dz);
        if (!this.canStandAt(level, next) || !this.inLeash(next)) {
            return false;
        }
        this.move(MoverType.SELF, new Vec3(dx, 0.0D, dz));
        this.footworkTick = this.tickCount;
        return true;
    }

    /** True when the last by-hand step happened within the last 2 ticks (the clip follows what the feet do). */
    private boolean isStepping() {
        return this.tickCount - this.footworkTick <= 2;
    }

    private boolean startBackstepIfRoom(ServerLevel level) {
        Vec3 back = dodgeVector(Dodge.BACK, this.getYRot()).normalize().scale(BACKSTEP_DISTANCE);
        if (!this.canStandAt(level, this.position().add(back))) {
            return false;
        }
        this.startBackstep();
        return true;
    }

    private void startBackstep() {
        this.setLockedYaw(this.getYRot());
        this.face(this.lockedYaw);
        this.getNavigation().stop();
        this.setState(State.REPOSITION);
        this.setAnim(ArchitectAnim.BACKSTEP);
    }

    private void tickBackstep() {
        if (this.stateTicks <= BACKSTEP_MOVE_TICKS) {
            this.moveAlong(this.lockedYaw, -BACKSTEP_DISTANCE / BACKSTEP_MOVE_TICKS);
        }
        if (this.stateTicks >= ArchitectAnim.BACKSTEP.ticks) {
            this.setState(State.COMBAT_IDLE);
            this.cooldowns[ArchitectAttack.SIXFOLD_EDICT.ordinal()] = 0; // spec §12.3: backstep, then the Decree
            this.pause(0, 4);
        }
    }

    /** Out of combat: walk back to the anchor and, once there, play one of the three rest idles every 100-200 ticks. */
    private void rest(ServerLevel level) {
        this.noTargetTicks++;
        if (this.hasAnchor && this.distanceToSqr(this.anchorX, this.anchorY, this.anchorZ) > 9.0D) {
            if (this.tickCount % 20 == 0) {
                this.getNavigation().moveTo(this.anchorX, this.anchorY, this.anchorZ, WALK_SPEED);
            }
            return;
        }
        if (this.noTargetTicks < REST_AFTER_TICKS || this.tickCount < this.nextIdleVariantTick) {
            return;
        }
        this.setAnim(REST_IDLES[this.random.nextInt(REST_IDLES.length)]);
        this.nextIdleVariantTick = this.tickCount + IDLE_VARIANT_MIN + this.random.nextInt(IDLE_VARIANT_SPREAD + 1);
    }

    // ================================================================= targets
    /** Players outside creative and spectator, iron golems and any mob that targets him (spec §17 "Alvos"). */
    public boolean isFoe(LivingEntity e) {
        if (e == this || !e.isAlive() || e instanceof Architect) {
            return false;
        }
        if (e instanceof Player p) {
            return !p.isCreative() && !p.isSpectator();
        }
        return e instanceof IronGolem || e instanceof Mob m && m.getTarget() == this;
    }

    private boolean isFoeInLeash(LivingEntity e) {
        return this.isFoe(e) && this.inLeash(e);
    }

    private boolean inLeash(Entity e) {
        return !this.hasAnchor || e.distanceToSqr(this.anchorX, this.anchorY, this.anchorZ) <= LEASH_RADIUS * LEASH_RADIUS;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return this.isFoe(target) && super.canAttack(target);
    }

    /**
     * Target selection (reform §12.4), server side only; the clients just receive the head and body angles. Each foe in
     * range gets a score: proximity (up to 40), threat from the damage it dealt (up to 50, decaying), a recent melee
     * aggressor (15), how long it has gone without being the target (1 per second, up to 25: nobody is ignored for good),
     * a wounded player (6) and a stickiness bonus for the current target (12). The choice is reviewed every
     * TARGET_SWITCH scan, but the target only changes when the best foe beats the current one by TARGET_SWITCH_MARGIN
     * (x1.25 plus 4), then it is frozen for TARGET_SWITCH_LOCK ticks. A blow keeps its own target until it ends.
     */
    private @Nullable LivingEntity selectTarget(ServerLevel level) {
        LivingEntity current = this.getTarget();
        boolean valid = current != null && this.isFoeInLeash(current);
        if (valid) {
            this.markTargeted(current);
            if (this.tickCount % TARGET_SCAN_INTERVAL != 0 || this.tickCount < this.retargetLockUntil) {
                return current;
            }
        }
        LivingEntity best = null;
        double bestScore = -1.0D;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(FOLLOW_RANGE, 16.0D, FOLLOW_RANGE), this.foeInLeash)) {
            double score = this.scoreFoe(e, current);
            if (score > bestScore) {
                bestScore = score;
                best = e;
            }
        }
        if (valid && (best == null || best == current || bestScore < this.scoreFoe(current, current) * TARGET_SWITCH_MARGIN + 4.0D)) {
            return current;
        }
        if (best != current) {
            this.setTarget(best);
            this.retargetLockUntil = this.tickCount + TARGET_SWITCH_LOCK;
        }
        if (best != null) {
            this.markTargeted(best);
        }
        return best;
    }

    private void markTargeted(LivingEntity e) {
        this.threats.computeIfAbsent(e.getUUID(), k -> new float[] {0.0F, this.tickCount})[1] = this.tickCount;
    }

    private double scoreFoe(LivingEntity e, @Nullable LivingEntity current) {
        double proximity = 40.0D * (1.0D - Mth.clamp(Math.sqrt(this.distanceToSqr(e)) / FOLLOW_RANGE, 0.0D, 1.0D));
        float[] t = this.threats.get(e.getUUID());
        double threat = t == null ? 0.0D : Math.min(50.0D, 50.0D * t[0] / (this.getMaxHealth() * 0.15D));
        double aggressor = this.tickCount - this.lastAggressionTick <= 100 && e.getId() == this.aggressorId ? 15.0D : 0.0D;
        double neglect = t == null ? 10.0D : Math.min(25.0D, (this.tickCount - t[1]) / 20.0D);
        double wounded = e instanceof Player && e.getHealth() < e.getMaxHealth() * 0.4F ? 6.0D : 0.0D;
        double sticky = e == current ? 12.0D : 0.0D;
        return proximity + threat + aggressor + neglect + wounded + sticky;
    }

    /** Threat decays 4 % a second; entries of foes long gone are dropped. */
    private void decayThreats() {
        this.threats.values().forEach(t -> t[0] *= 0.96F);
        this.threats.values().removeIf(t -> t[0] < 0.5F && this.tickCount - t[1] > 1200);
    }

    private void noteAggression(@Nullable Entity attacker, float damage, boolean meleeFront) {
        if (!(attacker instanceof LivingEntity living) || !this.isFoe(living)) {
            return;
        }
        if (meleeFront) {
            this.lastAggressionTick = this.tickCount;
            this.aggressorId = living.getId();
        }
        this.threats.computeIfAbsent(living.getUUID(), k -> new float[] {0.0F, -2000.0F})[0] += damage;
        LivingEntity current = this.getTarget();
        if (current == null || !this.isFoeInLeash(current)) {
            this.setTarget(living);
            this.retargetLockUntil = this.tickCount + TARGET_SWITCH_LOCK;
        }
    }

    /** Public for the GameTests: the damage a foe has dealt (decayed), the base of its threat score. */
    public float getThreat(LivingEntity e) {
        float[] t = this.threats.get(e.getUUID());
        return t == null ? 0.0F : t[0];
    }

    private int foesWithin(ServerLevel level, double radius) {
        int n = 0;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius), this.foeInLeash)) {
            if (horizontalDistance(this, e) <= radius) {
                n++;
            }
        }
        return n;
    }

    /** How fast the target moves sideways across his line of sight, blocks per tick (circling him). */
    private static double lateralSpeed(Entity self, LivingEntity target) {
        Vec3 away = target.position().subtract(self.position()).multiply(1.0D, 0.0D, 1.0D);
        if (away.lengthSqr() < 1.0E-4D) {
            return 0.0D;
        }
        Vec3 dir = away.normalize();
        Vec3 v = target.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D);
        return Math.abs(v.x * -dir.z + v.z * dir.x);
    }

    // ================================================================= anchor and geometry helpers
    private void setAnchorHere() {
        this.hasAnchor = true;
        this.anchorX = this.getX();
        this.anchorY = this.getY();
        this.anchorZ = this.getZ();
    }

    public @Nullable Vec3 getAnchor() {
        return this.hasAnchor ? new Vec3(this.anchorX, this.anchorY, this.anchorZ) : null;
    }

    private void moveAlong(float yawDeg, double step) {
        Vec3 f = Vec3.directionFromRotation(0.0F, yawDeg);
        this.move(MoverType.SELF, new Vec3(f.x * step, 0.0D, f.z * step));
    }

    private void throwAway(LivingEntity victim, double push, double lift) {
        double dx = victim.getX() - this.getX();
        double dz = victim.getZ() - this.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 1.0E-3D) {
            return;
        }
        victim.setDeltaMovement(dx / len * push, Math.max(victim.getDeltaMovement().y, lift), dz / len * push);
        victim.hurtMarked = true;
    }

    public static float yawTowards(Entity from, Entity to) {
        return (float) (Mth.atan2(-(to.getX() - from.getX()), to.getZ() - from.getZ()) * Mth.RAD_TO_DEG);
    }

    public static double horizontalDistance(Entity a, Entity b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Off the ground and moving: a jumper or a flier (spec §12.3 "No ar"). */
    private static boolean isInAir(LivingEntity e) {
        return !e.onGround() && !e.isInWater();
    }

    private static boolean isRetreating(Entity self, LivingEntity target) {
        Vec3 away = target.position().subtract(self.position()).multiply(1.0D, 0.0D, 1.0D);
        Vec3 v = target.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D);
        return away.lengthSqr() > 1.0E-4D && v.lengthSqr() > 0.004D && v.normalize().dot(away.normalize()) > 0.6D;
    }

    // ================================================================= read by GameTests
    public @Nullable ArchitectAttack getAttack() {
        return this.attack;
    }

    public @Nullable ArchitectAttack getLastAttack() {
        return this.lastAttack;
    }

    public int getStateTicks() {
        return this.stateTicks;
    }

    public float getPosture() {
        return this.posture;
    }

    public int getStaggerImmunity() {
        return this.staggerImmunity;
    }

    public int getStrikesLanded() {
        return this.strikesLanded;
    }

    public int getAttacksAborted() {
        return this.attacksAborted;
    }

    public int getParries() {
        return this.parries;
    }

    public int getStaggers() {
        return this.staggers;
    }

    public int getDeflections() {
        return this.deflections;
    }

    public int getCooldown(ArchitectAttack a) {
        return this.cooldowns[a.ordinal()];
    }

    public int getDodges() {
        return this.dodges;
    }

    public @Nullable Dodge getDodge() {
        return this.dodge;
    }

    public boolean isCounterWindow() {
        return this.counterTicks > 0;
    }

    public int getShieldsBroken() {
        return this.shieldsBroken;
    }

    public int getBlowsPunished() {
        return this.blowsPunished;
    }

    public int getShieldScore() {
        return this.shieldScore;
    }

    /** How many times each blow was started (read by the GameTests and the balance runs). */
    public int getAttackUses(ArchitectAttack a) {
        return this.attackUses[a.ordinal()];
    }

    // ================================================================= dormant statue and stone constitution (§11.1)
    @Override
    public boolean isPushable() {
        return !this.isDormant() && super.isPushable();
    }

    @Override
    public void knockback(double strength, double x, double z) {
        if (!this.isDormant()) {
            super.knockback(strength, x, z);
        }
    }

    @Override
    public void push(Vec3 impulse) {
        if (!this.isDormant()) {
            super.push(impulse);
        }
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (effect.is(MobEffects.POISON) || effect.is(MobEffects.WITHER)) {
            return false;
        }
        return super.canBeAffected(effect);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        if (fallDistance < SAFE_FALL_BLOCKS) {
            return false;
        }
        return super.causeFallDamage(fallDistance, multiplier, source);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    // ================================================================= visual events (client side, spec §16 and §19)
    @Override
    public void handleEntityEvent(byte id) {
        if (id < EVENT_AWAKEN_START || id > EVENT_DEATH) {
            super.handleEntityEvent(id);
            return;
        }
        Level level = this.level();
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        if (id == EVENT_AWAKEN_START || id == EVENT_ENRAGE) {
            for (int k = 0; k < 16; k++) { // the mana pulse from the sternum
                double a = k * Math.PI / 8.0D;
                level.addParticle(MANA_DUST, x, y + STERNUM_Y, z, Math.cos(a) * 0.15D, 0.0D, Math.sin(a) * 0.15D);
            }
        } else if (id == EVENT_MASK_RIGHT || id == EVENT_MASK_LEFT || id == EVENT_TABLET_FALL) {
            double side = id == EVENT_MASK_RIGHT ? -0.4D : id == EVENT_MASK_LEFT ? 0.4D : 0.0D;
            for (int k = 0; k < 10; k++) { // stone chips where the piece breaks on the ground
                level.addParticle(STONE_DUST, x + side + (this.random.nextDouble() - 0.5D) * 0.6D, y + 0.2D,
                        z + (this.random.nextDouble() - 0.5D) * 0.6D, 0.0D, 0.05D, 0.0D);
            }
        } else if (id == EVENT_PARRY) {
            for (int k = 0; k < 3; k++) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y + 2.0D, z, 0.0D, 0.1D, 0.0D);
            }
        } else if (id == EVENT_STAGGER || id == EVENT_DEATH) {
            for (int k = 0; k < 10; k++) { // mana errors flicker on the sternum
                level.addParticle(ERROR_DUST, x + (this.random.nextDouble() - 0.5D) * 0.5D, y + STERNUM_Y,
                        z + (this.random.nextDouble() - 0.5D) * 0.5D, 0.0D, 0.02D, 0.0D);
            }
        } else if (id == EVENT_DODGE) {
            for (int k = 0; k < 8; k++) { // a puff of stone dust where the feet pushed off
                level.addParticle(ParticleTypes.POOF, x + (this.random.nextDouble() - 0.5D) * 1.2D, y + 0.1D,
                        z + (this.random.nextDouble() - 0.5D) * 1.2D, 0.0D, 0.02D, 0.0D);
            }
        } else if (id == EVENT_SHIELD_BREAK) {
            for (int k = 0; k < 12; k++) {
                level.addParticle(ParticleTypes.CRIT, x + (this.random.nextDouble() - 0.5D) * 2.0D,
                        y + 1.4D + this.random.nextDouble(), z + (this.random.nextDouble() - 0.5D) * 2.0D, 0.0D, 0.1D, 0.0D);
            }
        } else if (id == EVENT_GARGOYLE_IMPACT) {
            for (int k = 0; k < 24; k++) { // a ring of dust hugging the ground
                double a = k * Math.PI / 12.0D;
                level.addParticle(ParticleTypes.POOF, x + Math.cos(a) * 1.5D, y + 0.1D, z + Math.sin(a) * 1.5D,
                        Math.cos(a) * 0.2D, 0.0D, Math.sin(a) * 0.2D);
            }
        } else if (id >= EVENT_SIXFOLD_CUT_FIRST && id < EVENT_SIXFOLD_CUT_FIRST + 6) {
            level.addParticle(ParticleTypes.SWEEP_ATTACK, x, y + 1.6D, z, 0.0D, 0.0D, 0.0D);
        }
    }

    // ================================================================= death (spec §15, DEATH 80 ticks)
    @Override
    public void die(DamageSource source) {
        this.endAirborne();
        this.attack = null;
        super.die(source);
        if (!this.level().isClientSide()) {
            this.setState(State.DYING);
            this.setAnim(ArchitectAnim.DEATH);
            this.level().broadcastEntityEvent(this, EVENT_DEATH);
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTicks++;
        if (this.level() instanceof ServerLevel level && this.deathTicks % 8 == 0) {
            level.sendParticles(STONE_DUST, this.getX(), this.getY() + 1.5D, this.getZ(), 6, 0.6D, 0.8D, 0.6D, 0.02D);
        }
        if (this.deathTicks >= DEATH_TICKS && !this.level().isClientSide()) {
            this.bossEvent.removeAllPlayers();
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    // ================================================================= persistence
    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString(TAG_STATE, this.getState().name());
        output.putString(TAG_PHASE, this.reachedPhase.name());
        if (this.hasAnchor) {
            output.putDouble(TAG_ANCHOR + "X", this.anchorX);
            output.putDouble(TAG_ANCHOR + "Y", this.anchorY);
            output.putDouble(TAG_ANCHOR + "Z", this.anchorZ);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        State state = parseEnum(State.class, input.getStringOr(TAG_STATE, State.DORMANT.name()), State.DORMANT);
        this.reachedPhase = parseEnum(Phase.class, input.getStringOr(TAG_PHASE, Phase.DUELIST.name()), Phase.DUELIST);
        if (this.reachedPhase == Phase.MONUMENT) {
            this.reachedPhase = Phase.DUELIST;
        }
        double ax = input.getDoubleOr(TAG_ANCHOR + "X", Double.NaN);
        if (!Double.isNaN(ax)) {
            this.hasAnchor = true;
            this.anchorX = ax;
            this.anchorY = input.getDoubleOr(TAG_ANCHOR + "Y", this.getY());
            this.anchorZ = input.getDoubleOr(TAG_ANCHOR + "Z", this.getZ());
        }
        // A fight interrupted by a save resumes in combat idle: attacks, leaps and transitions are never replayed.
        this.setState(state == State.DORMANT || state == State.DYING ? state : State.COMBAT_IDLE);
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String name, E fallback) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException unknown) {
            return fallback;
        }
    }
}
