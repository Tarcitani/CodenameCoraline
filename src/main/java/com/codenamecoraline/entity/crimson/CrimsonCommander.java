package com.codenamecoraline.entity.crimson;

import com.codenamecoraline.entity.spirit.BossSpirit;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import com.codenamecoraline.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The Crimson Commander (Comandante Carmesim): an ancient general who still guards his ruined hall under the deepslate.
 * A disciplined duelist with a colossal greatsword: he punishes rash attacks with a guard and a riposte, runs down whoever
 * flees with a piercing dash, crushes the ground with leaping overhead slams and cuts down the unreachable with a crescent
 * wave of blood. The server owns everything (movement, damage, stages, rewards); the client only plays the animation the
 * server selects (synched), so visuals always mirror gameplay.
 *
 * <p>The body is a state machine ({@link Activity}); combat is nested inside it ({@link Attack} with prepare, execute and
 * recover phases). Stage 1 is the Iron Vigil, stage 2 (below 60% health) the Awakened Oath, stage 3 (below 25%) the Fury
 * of the Last Commander; each transition happens once.</p>
 */
public class CrimsonCommander extends Monster {
    // ---------------------------------------------------------------- balance
    public static final double MAX_HEALTH = 220.0D;
    public static final double ARMOR = 14.0D;
    public static final double ARMOR_TOUGHNESS = 6.0D;
    public static final double BASE_SPEED = 0.34D;
    public static final double STAGE_TWO_SPEED = 0.38D;
    public static final double STAGE_THREE_SPEED = 0.42D;
    public static final double BASE_DAMAGE = 12.0D;
    public static final double KNOCKBACK_RESISTANCE = 0.75D;
    public static final double FOLLOW_RANGE = 40.0D;
    public static final int XP_REWARD = 250;
    public static final float WIDTH = 1.1F;
    public static final float HEIGHT = 3.5F;
    public static final float EYE_HEIGHT = 3.05F;
    /** Fall damage he takes, as a share of the normal amount (80% less). */
    public static final float FALL_DAMAGE_FACTOR = 0.2F;
    public static final double STAGE_TWO_HEALTH = 0.60D;
    public static final double STAGE_THREE_HEALTH = 0.25D;
    /** Damage of every attack in the Fury of the Last Commander. */
    public static final float FURY_DAMAGE_MULTIPLIER = 1.25F;
    /** From the Awakened Oath on, every cooldown is 20% shorter. */
    public static final double STAGE_TWO_COOLDOWN_FACTOR = 0.8D;
    public static final int MAX_VANGUARDS = 2;
    /** Another Commander this close forbids a summoning and a natural spawn. */
    public static final double OTHER_COMMANDER_RADIUS = 48.0D;

    // ---------------------------------------------------------------- timings (ticks; 20 per second)
    public static final int INTRO_TICKS = 100;
    public static final int INTRO_RISE_TICK = 40;
    /** The title card appears as the salute ends. */
    public static final int INTRO_TITLE_TICK = 66;
    public static final int AWAKEN_TICKS = 30;
    public static final int AWAKEN_SUMMON_TICK = 18;
    public static final int FURY_TRANSITION_TICKS = 40;
    public static final int DEATH_SPIRIT_TICK = 30;
    public static final int DEATH_SWORD_TICK = 50;
    public static final int DEATH_TICKS = 90;

    // crimson_slash: a 140 degree horizontal sweep in front of him
    public static final int SLASH_PREP = 12;
    public static final int SLASH_EXEC = 6;
    public static final int SLASH_HIT_TICK = 3;
    public static final int SLASH_RECOVER = 14;
    public static final float SLASH_DAMAGE = 12.0F;
    public static final double SLASH_REACH = 3.8D;
    public static final double SLASH_ARC = 140.0D;
    public static final double SLASH_KNOCKBACK = 1.4D;

    // dash_thrust: a fast piercing advance at where the target is going
    public static final int THRUST_PREP = 16;
    public static final int THRUST_EXEC = 8;
    public static final int THRUST_RECOVER = 18;
    public static final int THRUST_RECOVER_MISS = 24;
    public static final float THRUST_DAMAGE = 15.0F;
    public static final double THRUST_MIN_RANGE = 4.0D;
    public static final double THRUST_MAX_RANGE = 11.0D;
    public static final double THRUST_MAX_DISTANCE = 9.0D;
    public static final int THRUST_SLOW_TICKS = 40;

    // heavy_overhead_slam: two hands over the head, a jump of two blocks, the blade driven into the ground
    public static final int SLAM_PREP = 22;
    public static final int SLAM_JUMP_TICK = 14;
    public static final int SLAM_EXEC = 10;
    public static final int SLAM_IMPACT_TICK = 6;
    public static final int SLAM_RECOVER = 26;
    public static final float SLAM_EPICENTER_DAMAGE = 20.0F;
    public static final float SLAM_EDGE_DAMAGE = 12.0F;
    public static final double SLAM_EPICENTER_RADIUS = 2.0D;
    public static final double SLAM_RADIUS = 4.0D;
    public static final double SLAM_MIN_RANGE = 2.5D;
    public static final double SLAM_MAX_RANGE = 9.0D;
    public static final double SLAM_LIFT = 0.75D;
    public static final double SLAM_JUMP_HEIGHT = 2.6D;
    /** Ticks of the leap, from the take-off to the impact (8 in the preparation, 6 in the execution). */
    public static final int SLAM_FLIGHT_TICKS = SLAM_PREP - SLAM_JUMP_TICK + SLAM_IMPACT_TICK;
    public static final int SHIELD_DISABLE_TICKS = 100;

    // defensive_guard and the riposte
    public static final int GUARD_PREP = 6;
    public static final int GUARD_HOLD = 30;
    public static final int GUARD_RECOVER = 10;
    /** Share of frontal damage the guard stops. */
    public static final float GUARD_REDUCTION = 0.75F;
    public static final int GUARD_TRIGGER_HITS = 3;
    public static final int GUARD_TRIGGER_WINDOW = 40;
    /** While guarding he turns at most this many degrees per tick: walking around him is a real answer to the guard. */
    public static final float GUARD_TURN_RATE = 12.0F;
    public static final int RIPOSTE_EXEC = 6;
    public static final int RIPOSTE_HIT_TICK = 2;
    public static final int RIPOSTE_RECOVER = 8;
    public static final float RIPOSTE_DAMAGE = 14.0F;
    public static final double RIPOSTE_REACH = 3.2D;
    public static final double RIPOSTE_PUSH = 2.3D;
    public static final double RIPOSTE_LIFT = 0.6D;

    // blood_crescent_wave: a crescent of blood that flies 22 blocks
    public static final int WAVE_PREP = 18;
    public static final int WAVE_EXEC = 8;
    public static final int WAVE_RELEASE_TICK = 3;
    public static final int WAVE_RECOVER = 16;
    public static final float WAVE_DAMAGE = 13.0F;
    public static final double WAVE_MIN_RANGE = 8.0D;
    public static final double WAVE_MAX_RANGE = 20.0D;
    public static final double WAVE_DISTANCE = 22.0D;
    public static final double WAVE_SPEED = 1.5D;
    public static final double WAVE_HIT_RADIUS = 1.1D;

    // self-healing: he backs away from the fight, then drinks the oath's power in a red aura while his vanguards fight for him
    public static final double HEAL_BELOW = 0.70D;
    public static final double HEAL_PERCENT = 0.15D;
    public static final int HEAL_RETREAT_MAX = 40;
    public static final double HEAL_SAFE_DISTANCE = 10.0D;
    public static final int HEAL_CHANNEL_TICKS = 60;
    public static final int HEAL_COOLDOWN = 600;
    /** Losing this share of his health while channelling breaks the heal. */
    public static final double HEAL_INTERRUPT = 0.08D;
    public static final DustParticleOptions AURA_DUST = new DustParticleOptions(0xFF1030, 1.9F);

    public static final DustParticleOptions CRIMSON_DUST = new DustParticleOptions(0xB81D2A, 1.3F);
    public static final DustParticleOptions RUBY_DUST = new DustParticleOptions(0xFF2A3C, 0.9F);
    public static final DustParticleOptions OBSIDIAN_DUST = new DustParticleOptions(0x18161C, 1.4F);
    public static final DustParticleOptions GOLD_DUST = new DustParticleOptions(0xF3CF55, 1.0F);

    // ---------------------------------------------------------------- types
    /** What the body is doing. */
    public enum Activity { INTRO, GROUND, ATTACK, TRANSITION, DYING, DEAD, HEALING }

    public enum Attack { NONE, CRIMSON_SLASH, DASH_THRUST, HEAVY_SLAM, DEFENSIVE_GUARD, RIPOSTE, BLOOD_WAVE }

    public enum Phase { PREPARE, EXECUTE, RECOVER }

    /** A crescent of blood in flight: it moves on its own after the cut that released it. */
    public static final class Wave {
        Vec3 pos;
        final Vec3 dir;
        double travelled;
        final Set<Integer> hit = new HashSet<>();
        boolean done;

        Wave(Vec3 pos, Vec3 dir) {
            this.pos = pos;
            this.dir = dir;
        }

        public Vec3 position() {
            return this.pos;
        }

        public double travelled() {
            return this.travelled;
        }
    }

    // ---------------------------------------------------------------- synched state
    private static final EntityDataAccessor<Integer> DATA_ANIM =
            SynchedEntityData.defineId(CrimsonCommander.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_OVERLAY =
            SynchedEntityData.defineId(CrimsonCommander.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(CrimsonCommander.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STAGE =
            SynchedEntityData.defineId(CrimsonCommander.class, EntityDataSerializers.INT);

    /** Client-side animation clocks. */
    public final AnimationState animState = new AnimationState();
    public final AnimationState overlayState = new AnimationState();

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            UUID.randomUUID(),
            Component.translatable("entity.codenamecoraline.crimson_commander"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS);

    // ---------------------------------------------------------------- server state
    private int activityTimer = 0;
    private Attack attack = Attack.NONE;
    private Phase phase = Phase.PREPARE;
    private int phaseTimer = 0;
    private int prepJitter = 0;
    private boolean attackHitDone = false;
    private final Set<Integer> struck = new HashSet<>();
    private Attack lastAttack = Attack.NONE;
    private final int[] cooldowns = new int[Attack.values().length];
    private boolean comboSlam = false;

    // dash
    private Vec3 dashFrom = Vec3.ZERO;
    private Vec3 dashDir = Vec3.ZERO;
    private boolean dashPlanned = false;
    private double dashLength = 0.0D;
    private boolean dashHit = false;
    private boolean dashMissed = false;

    // slam leap
    private Vec3 leapFrom = Vec3.ZERO;
    private Vec3 leapTo = Vec3.ZERO;
    private int leapTick = -1;
    private int leapTicks = 0;
    private double leapHeight = SLAM_JUMP_HEIGHT;
    private boolean leapIsSlam = false;
    private Vec3 slamCenter = Vec3.ZERO;
    private Vec3 lastImpact = Vec3.ZERO;
    private Vec3 ringCenter = Vec3.ZERO;

    // riposte and guard
    private @Nullable LivingEntity riposteTarget;
    private final Deque<Integer> recentHits = new ArrayDeque<>();
    private int lastDeflectedId = -1;
    /** Client side: the local tick at which the synched activity turned to INTRO (drives the cinematic). */
    private int introClientStart = -1;

    // waves
    private final List<Wave> waves = new ArrayList<>();

    // stages, intro, death
    private int pendingStage = 1;
    private int stageTransitions = 0;
    private boolean vanguardsCalled = false;
    private int vanguardsSummoned = 0;
    private int deathTicks = 0;
    private boolean rewardsGranted = false;
    private int deathAwardCount = 0;
    private boolean swordDelivered = false;
    private int swordsDelivered = 0;
    private int overlayTicks = 0;
    private int animHold = 0;
    private int idleTicks = 0;

    // targeting and movement
    private @Nullable UUID firstAttacker;
    private Vec3 targetVelocity = Vec3.ZERO;
    private Vec3 targetLastPos = Vec3.ZERO;
    private int pathRecalc = 0;
    private int reachCheck = 0;
    private boolean targetReachable = true;
    private Vec3 lastPos = Vec3.ZERO;
    private double lastSpeed = 0.0D;
    private int stuckTicks = 0;
    private Vec3 stuckAnchor = Vec3.ZERO;
    private int inWallTicks = 0;
    private int leapCooldown = 0;
    private int healCooldown = 300;
    private int healPhase = 0;
    private float healLostWhileChannelling = 0.0F;
    private float healLastHealth = 0.0F;
    private int healsDone = 0;
    private float healedTotal = 0.0F;
    /** Tick at which the last recovery ended: whoever stays glued to him right after it gets an answer. */
    private int recoveredAt = -1000;
    private int leapsTaken = 0;

    // counters (for tests and the design document)
    private int slashesLanded = 0;
    private int thrustHits = 0;
    private int slamsLanded = 0;
    private int ripostes = 0;
    private int wavesReleased = 0;
    private int waveHits = 0;
    private int guardBlocks = 0;
    private int deflections = 0;
    private int attacksCancelled = 0;
    private int unstuckMoves = 0;
    private int combos = 0;

    public CrimsonCommander(EntityType<? extends CrimsonCommander> type, Level level) {
        super(type, level);
        this.xpReward = XP_REWARD;
        this.bossEvent.setVisible(false);
        this.bossEvent.setDarkenScreen(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS)
                .add(Attributes.MOVEMENT_SPEED, BASE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
                .add(Attributes.STEP_HEIGHT, 1.1D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANIM, CrimsonAnim.COMBAT_IDLE.ordinal());
        builder.define(DATA_OVERLAY, -1);
        builder.define(DATA_ACTIVITY, Activity.GROUND.ordinal());
        builder.define(DATA_STAGE, 1);
    }

    // ---------------------------------------------------------------- accessors
    public CrimsonAnim getAnim() {
        return CrimsonAnim.byOrdinal(this.entityData.get(DATA_ANIM));
    }

    public @Nullable CrimsonAnim getOverlay() {
        int o = this.entityData.get(DATA_OVERLAY);
        return o < 0 ? null : CrimsonAnim.byOrdinal(o);
    }

    public Activity getActivity() {
        Activity[] all = Activity.values();
        return all[Mth.clamp(this.entityData.get(DATA_ACTIVITY), 0, all.length - 1)];
    }

    private void setActivity(Activity activity) {
        this.entityData.set(DATA_ACTIVITY, activity.ordinal());
        this.activityTimer = 0;
    }

    public int getStage() {
        return Mth.clamp(this.entityData.get(DATA_STAGE), 1, 3);
    }

    public Attack getAttack() {
        return this.attack;
    }

    public Phase getPhase() {
        return this.phase;
    }

    public int getCooldown(Attack a) {
        return this.cooldowns[a.ordinal()];
    }

    /** Sets a cooldown (tests use it to lock or free an attack). */
    public void setCooldown(Attack a, int ticks) {
        this.cooldowns[a.ordinal()] = ticks;
    }

    public ServerBossEvent getBossEvent() {
        return this.bossEvent;
    }

    public int getTransitionCount() {
        return this.stageTransitions;
    }

    public int getDeathAwardCount() {
        return this.deathAwardCount;
    }

    public int getSwordsDelivered() {
        return this.swordsDelivered;
    }

    public int getVanguardsSummoned() {
        return this.vanguardsSummoned;
    }

    public int getSlashesLanded() {
        return this.slashesLanded;
    }

    public int getThrustHits() {
        return this.thrustHits;
    }

    public int getSlamsLanded() {
        return this.slamsLanded;
    }

    public int getRipostes() {
        return this.ripostes;
    }

    public int getWavesReleased() {
        return this.wavesReleased;
    }

    public int getWaveHits() {
        return this.waveHits;
    }

    public List<Wave> getWaves() {
        return this.waves;
    }

    public int getGuardBlocks() {
        return this.guardBlocks;
    }

    public int getDeflections() {
        return this.deflections;
    }

    public int getAttacksCancelled() {
        return this.attacksCancelled;
    }

    public int getUnstuckMoves() {
        return this.unstuckMoves;
    }

    public int getCombos() {
        return this.combos;
    }

    public @Nullable UUID getFirstAttacker() {
        return this.firstAttacker;
    }

    /** Damage of one of his blows in the current stage (the fury adds 25%). */
    public float damageFor(float base) {
        return this.getStage() >= 3 ? base * FURY_DAMAGE_MULTIPLIER : base;
    }

    /** Ticks since the intro began: the server timer, or on the client the time since INTRO was synched. */
    public int getIntroTicks() {
        if (this.getActivity() != Activity.INTRO) {
            return 0;
        }
        if (this.level().isClientSide()) {
            return this.introClientStart < 0 ? 0 : this.tickCount - this.introClientStart;
        }
        return this.activityTimer;
    }

    /** The active guard (its execute phase): frontal blows are reduced, projectiles turned back, melee answered. */
    public boolean isGuarding() {
        return this.attack == Attack.DEFENSIVE_GUARD && this.phase == Phase.EXECUTE && this.getActivity() == Activity.ATTACK;
    }

    /** Selects the animation the clients play. */
    public void setAnim(CrimsonAnim anim) {
        if (this.getAnim() != anim) {
            this.entityData.set(DATA_ANIM, anim.ordinal());
            this.animHold = 0;
        }
    }

    private void showOverlay(CrimsonAnim overlay, int ticks) {
        this.entityData.set(DATA_OVERLAY, overlay.ordinal());
        this.overlayTicks = ticks;
    }

    // ---------------------------------------------------------------- client animation clocks
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (this.level().isClientSide()) {
            if (DATA_ACTIVITY.equals(accessor) && this.getActivity() == Activity.INTRO) {
                this.introClientStart = this.tickCount;
            }
            if (DATA_ANIM.equals(accessor)) {
                this.animState.start(this.tickCount);
            } else if (DATA_OVERLAY.equals(accessor) && this.entityData.get(DATA_OVERLAY) >= 0) {
                this.overlayState.start(this.tickCount);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.animState.startIfStopped(this.tickCount);
        }
    }

    // ---------------------------------------------------------------- immunities and bookkeeping
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return super.causeFallDamage(distance, multiplier * FALL_DAMAGE_FACTOR, source);
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isSensitiveToWater() {
        return false;
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

    /** Hostile to players and iron golems only: never to his vanguards, another Commander or passive animals. */
    @Override
    public boolean canAttack(LivingEntity target) {
        Activity a = this.getActivity();
        if (a == Activity.INTRO || a == Activity.TRANSITION || a == Activity.DYING || a == Activity.DEAD) {
            return false;
        }
        return this.isFoe(target);
    }

    /** Players and iron golems, whatever he is doing right now (a wave already in flight still cuts them). */
    private boolean isFoe(LivingEntity target) {
        return (target instanceof Player || target instanceof IronGolem) && super.canAttack(target);
    }

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        if (other instanceof CrimsonAshVanguard vanguard) {
            return vanguard.isOwnedBy(this);
        }
        return other instanceof CrimsonCommander || super.considersEntityAsAlly(other);
    }

    // ---------------------------------------------------------------- spawn and intro
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
                                                  EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        this.bossEvent.setVisible(true);
        return result;
    }

    /**
     * Natural spawn: rare, alone, deep underground (Y 0 and below) in the dark (light 7 or less), on solid ground with room
     * for his 3.5 blocks, and never within 48 blocks of another Commander.
     */
    public static boolean checkSpawnRules(EntityType<CrimsonCommander> type, ServerLevelAccessor level, EntitySpawnReason reason,
                                          BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL || pos.getY() > 0) {
            return false;
        }
        if (level.getMaxLocalRawBrightness(pos) > 7) {
            return false;
        }
        if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP)) {
            return false;
        }
        for (int dy = 0; dy < 4; dy++) {
            if (!level.getBlockState(pos.above(dy)).getCollisionShape(level, pos.above(dy)).isEmpty()) {
                return false;
            }
        }
        if (!level.getEntitiesOfClass(CrimsonCommander.class, new AABB(pos).inflate(OTHER_COMMANDER_RADIUS)).isEmpty()) {
            return false;
        }
        return reason != EntitySpawnReason.NATURAL || random.nextInt(4) == 0;
    }

    /** The audience: kneeling over his planted sword, he rises, frees the blade and salutes before the duel begins. */
    public void startIntro(ServerLevel level) {
        this.setActivity(Activity.INTRO);
        this.setAnim(CrimsonAnim.INTRO_KNEEL);
        this.bossEvent.setVisible(true);
        this.getNavigation().stop();
        level.sendParticles(CRIMSON_DUST, this.getX(), this.getY() + 1.5D, this.getZ(), 40, 1.2D, 1.2D, 1.2D, 0.02D);
        level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 0.4D, this.getZ(), 16, 1.0D, 0.2D, 1.0D, 0.02D);
        this.playSound(SoundEvents.BEACON_ACTIVATE, 2.0F, 0.6F);
    }

    // ---------------------------------------------------------------- damage, guard, stages, death
    /** Whether a source position lies in front of him (within the 140 degrees his guard covers). */
    public boolean isInFront(Vec3 sourcePos) {
        Vec3 to = sourcePos.subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (to.lengthSqr() < 1.0E-4D) {
            return true;
        }
        Vec3 fwd = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        return fwd.dot(to.normalize()) >= Math.cos(Math.toRadians(70.0D));
    }

    @Override
    public ProjectileDeflection deflection(Projectile projectile) {
        if (this.isGuarding() && this.isInFront(projectile.position())) {
            if (projectile.getId() != this.lastDeflectedId) { // the game may ask more than once for the same arrow
                this.lastDeflectedId = projectile.getId();
                this.deflections++;
                this.guardBlocks++;
                this.guardSpark();
            }
            return ProjectileDeflection.REVERSE;
        }
        return super.deflection(projectile);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        Activity a = this.getActivity();
        if ((a == Activity.INTRO || a == Activity.TRANSITION) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        if (a == Activity.DYING || a == Activity.DEAD) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof CrimsonAshVanguard || attacker instanceof CrimsonCommander) {
            return false; // his own never hurt him
        }
        boolean blocked = false;
        LivingEntity riposteAt = null;
        Vec3 from = source.getSourcePosition();
        if (this.isGuarding() && from != null && this.isInFront(from) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (source.getDirectEntity() instanceof Projectile) {
                this.deflections++;
                this.guardBlocks++;
                this.guardSpark();
                return false; // a projectile that slipped past the deflection still glances off the blade
            }
            amount *= 1.0F - GUARD_REDUCTION;
            blocked = true;
            boolean melee = attacker instanceof LivingEntity && source.getDirectEntity() == attacker
                    && attacker.distanceTo(this) <= 4.5D;
            if (melee && this.phase == Phase.EXECUTE) {
                riposteAt = (LivingEntity) attacker;
            }
        }
        float before = this.getHealth();
        boolean hurt = super.hurtServer(level, source, amount);
        if (blocked) {
            this.guardBlocks++;
            this.guardSpark();
            this.showOverlay(CrimsonAnim.GUARD_BLOCK, 5);
        }
        if (!hurt || !this.isAlive()) {
            if (riposteAt != null && this.isAlive()) {
                this.startRiposte(riposteAt); // even a blow fully stopped by the guard is answered
            }
            return hurt;
        }
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.firstAttacker == null && attacker instanceof LivingEntity living && this.canAttack(living)) {
            this.firstAttacker = attacker.getUUID();
        }
        if (attacker != null || from != null) {
            this.recentHits.addLast(this.tickCount);
        }
        if (!blocked && before - this.getHealth() > 0.0F) {
            this.showOverlay(from != null && !this.isInFront(from) ? CrimsonAnim.HURT_BACK : CrimsonAnim.HURT_FRONT, 8);
        }
        int wanted = this.stageForHealth();
        if (wanted > this.getStage() && a != Activity.TRANSITION) {
            this.startTransition(level, wanted);
        } else if (riposteAt != null) {
            this.startRiposte(riposteAt);
        }
        return hurt;
    }

    private void guardSpark() {
        if (this.level() instanceof ServerLevel level) {
            Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
            Vec3 p = this.position().add(f.scale(0.9D)).add(0.0D, 2.0D, 0.0D);
            level.sendParticles(GOLD_DUST, p.x, p.y, p.z, 10, 0.3D, 0.4D, 0.3D, 0.05D);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 6, 0.3D, 0.4D, 0.3D, 0.2D);
        }
        this.playSound(ModSounds.CRIMSON_GUARD.get(), 1.1F, 1.2F);
    }

    private int stageForHealth() {
        double r = this.getHealth() / this.getMaxHealth();
        return r < STAGE_THREE_HEALTH ? 3 : r < STAGE_TWO_HEALTH ? 2 : 1;
    }

    private void startTransition(ServerLevel level, int stage) {
        this.cancelAttack(false);
        this.endLeap();
        this.pendingStage = stage;
        this.stageTransitions++;
        this.setActivity(Activity.TRANSITION);
        this.setAnim(stage >= 3 ? CrimsonAnim.FURY_TRANSITION : CrimsonAnim.AWAKEN_TRANSITION);
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        this.playSound(ModSounds.CRIMSON_AWAKEN.get(), 2.4F, stage >= 3 ? 0.6F : 0.8F);
        level.sendParticles(CRIMSON_DUST, this.getX(), this.getY() + 1.8D, this.getZ(), 60, 1.6D, 1.4D, 1.6D, 0.08D);
    }

    private void applyStage(int stage) {
        this.entityData.set(DATA_STAGE, Mth.clamp(stage, 1, 3));
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(stage >= 3 ? STAGE_THREE_SPEED : stage >= 2 ? STAGE_TWO_SPEED : BASE_SPEED);
        }
        // the bar's fill follows the stage: crimson, incandescent ruby, blood gold (see CrimsonCommanderBossBar)
        this.bossEvent.setColor(stage >= 3 ? BossEvent.BossBarColor.YELLOW : stage >= 2 ? BossEvent.BossBarColor.PINK
                : BossEvent.BossBarColor.RED);
    }

    private void tickTransition(ServerLevel level) {
        this.getNavigation().stop();
        int total = this.pendingStage >= 3 ? FURY_TRANSITION_TICKS : AWAKEN_TICKS;
        int t = this.activityTimer;
        if (t % 4 == 0) { // a pulse of crimson motes rising around him
            double r = 1.2D + (t % 12) * 0.12D;
            for (int i = 0; i < 12; i++) {
                double ang = Math.PI * 2.0D * i / 12.0D;
                level.sendParticles(CRIMSON_DUST, this.getX() + Math.cos(ang) * r, this.getY() + 0.2D + t * 0.05D,
                        this.getZ() + Math.sin(ang) * r, 1, 0.0D, 0.05D, 0.0D, 0.0D);
            }
        }
        if (!this.vanguardsCalled && t >= AWAKEN_SUMMON_TICK) {
            this.vanguardsCalled = true; // the oath awakens once: two vanguards answer (also if stage 2 was skipped)
            this.summonVanguards(level, MAX_VANGUARDS);
        }
        if (this.pendingStage >= 3 && t == 16) {
            this.playSound(ModSounds.CRIMSON_SLAM.get(), 2.0F, 0.6F);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE_BRICKS.defaultBlockState()),
                    this.getX(), this.getY() + 0.2D, this.getZ(), 40, 1.6D, 0.2D, 1.6D, 0.12D);
        }
        if (t >= total) {
            this.applyStage(this.pendingStage);
            this.setActivity(Activity.GROUND);
            this.setAnim(this.pendingStage >= 3 ? CrimsonAnim.FURY_IDLE : CrimsonAnim.COMBAT_IDLE);
        }
    }

    @Override
    public void die(DamageSource source) {
        if (this.rewardsGranted) {
            return;
        }
        super.die(source); // drops the loot table (once) and the experience
        if (!this.dead) {
            return; // the death was cancelled: he fights on
        }
        this.rewardsGranted = true;
        this.deathAwardCount++;
        this.cancelAttack(false);
        this.endLeap();
        this.waves.clear();
        this.dismissVanguards();
        this.setActivity(Activity.DYING);
        this.setAnim(CrimsonAnim.DEATH_KNEEL);
        this.entityData.set(DATA_OVERLAY, -1);
        this.bossEvent.setProgress(0.0F);
        this.getNavigation().stop();
        this.playSound(ModSounds.CRIMSON_DEATH.get(), 2.0F, 0.6F);
    }

    @Override
    protected void tickDeath() {
        this.deathTicks++;
        if (this.level() instanceof ServerLevel level) {
            if (this.deathTicks == 20) {
                this.playSound(SoundEvents.ANVIL_LAND, 1.4F, 0.5F); // the blade bites into the floor
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE_BRICKS.defaultBlockState()),
                        this.swordTip().x, this.getY() + 0.1D, this.swordTip().z, 24, 0.4D, 0.1D, 0.4D, 0.08D);
            }
            if (this.deathTicks == DEATH_SPIRIT_TICK) {
                this.setAnim(CrimsonAnim.DEATH_FINAL);
                BossSpirit.rise(level, ModEntities.CRIMSON_COMMANDER_SPIRIT.get(), this); // his oath is released
            }
            if (this.deathTicks == DEATH_SWORD_TICK) {
                this.deliverGreatsword(level);
            }
            if (this.deathTicks % 6 == 0) {
                level.sendParticles(RUBY_DUST, this.getX(), this.getY() + 2.6D, this.getZ(), 3, 0.3D, 0.2D, 0.3D, 0.01D);
            }
        }
        if (this.deathTicks >= DEATH_TICKS && !this.level().isClientSide()) {
            this.setActivity(Activity.DEAD);
            this.bossEvent.removeAllPlayers();
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    private Vec3 swordTip() {
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        return this.position().add(f.scale(1.3D));
    }

    /** The Crimson Oath Greatsword is left planted where he knelt: delivered once, whatever happens afterwards. */
    public void deliverGreatsword(ServerLevel level) {
        if (this.swordDelivered) {
            return;
        }
        this.swordDelivered = true;
        this.swordsDelivered++;
        Vec3 tip = this.swordTip();
        ItemEntity sword = new ItemEntity(level, tip.x, this.getY() + 0.6D, tip.z, new ItemStack(ModItems.CRIMSON_OATH_GREATSWORD.get()));
        sword.setDeltaMovement(Vec3.ZERO);
        sword.setDefaultPickUpDelay();
        level.addFreshEntity(sword);
        level.sendParticles(GOLD_DUST, tip.x, this.getY() + 1.0D, tip.z, 30, 0.3D, 0.6D, 0.3D, 0.02D);
        this.playSound(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), 1.6F, 0.7F);
    }

    @Override
    public void remove(RemovalReason reason) {
        this.bossEvent.removeAllPlayers();
        if (reason.shouldDestroy()) {
            this.dismissVanguards();
        }
        super.remove(reason);
    }

    // ---------------------------------------------------------------- vanguards
    public List<CrimsonAshVanguard> getVanguards(ServerLevel level) {
        return level.getEntitiesOfClass(CrimsonAshVanguard.class, this.getBoundingBox().inflate(96.0D),
                v -> v.isAlive() && v.isOwnedBy(this));
    }

    /** Raises up to {@code count} vanguards from the ash around him, never beyond {@link #MAX_VANGUARDS} alive. */
    public int summonVanguards(ServerLevel level, int count) {
        int room = MAX_VANGUARDS - this.getVanguards(level).size();
        int made = 0;
        double base = this.random.nextDouble() * Math.PI * 2.0D;
        for (int i = 0; i < 8 && made < Math.min(count, room); i++) {
            double ang = base + Math.PI * 2.0D * i / 8.0D;
            Vec3 around = this.position().add(Math.cos(ang) * 3.0D, 0.0D, Math.sin(ang) * 3.0D);
            Vec3 spot = findStandSpot(level, around, 1.5D, CrimsonAshVanguard.WIDTH, CrimsonAshVanguard.HEIGHT);
            if (spot == null) {
                continue;
            }
            CrimsonAshVanguard vanguard = ModEntities.CRIMSON_ASH_VANGUARD.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (vanguard == null) {
                continue;
            }
            vanguard.setPos(spot.x, spot.y, spot.z);
            vanguard.setYRot(this.getYRot());
            vanguard.setOwner(this);
            LivingEntity t = this.getTarget();
            if (t != null) {
                vanguard.setTarget(t);
            }
            level.addFreshEntity(vanguard);
            level.sendParticles(ParticleTypes.ASH, spot.x, spot.y + 1.0D, spot.z, 30, 0.4D, 0.8D, 0.4D, 0.02D);
            level.sendParticles(CRIMSON_DUST, spot.x, spot.y + 0.2D, spot.z, 12, 0.5D, 0.1D, 0.5D, 0.02D);
            made++;
            this.vanguardsSummoned++;
        }
        return made;
    }

    /** The master fell or left: his vanguards crumble with him. */
    private void dismissVanguards() {
        if (this.level() instanceof ServerLevel level) {
            for (CrimsonAshVanguard v : this.getVanguards(level)) {
                v.crumble();
            }
        }
    }

    // ---------------------------------------------------------------- the server brain
    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!this.isAlive() || this.isDeadOrDying()) {
            return;
        }
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.tickCounters();
        this.tickWaves(level);
        Vec3 now = this.position();
        this.lastSpeed = Math.sqrt((now.x - this.lastPos.x) * (now.x - this.lastPos.x) + (now.z - this.lastPos.z) * (now.z - this.lastPos.z));
        this.lastPos = now;
        if (this.getStage() >= 3 && this.tickCount % 3 == 0) { // the fury burns at the visor and along the blade
            Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
            level.sendParticles(RUBY_DUST, this.getX() + f.x * 0.35D, this.getY() + 3.15D, this.getZ() + f.z * 0.35D, 2, 0.12D, 0.05D, 0.12D, 0.01D);
            Vec3 side = new Vec3(-f.z, 0.0D, f.x).scale(-0.75D);
            level.sendParticles(CRIMSON_DUST, this.getX() + side.x + f.x * 0.9D, this.getY() + 1.4D, this.getZ() + side.z + f.z * 0.9D,
                    2, 0.15D, 0.5D, 0.15D, 0.01D);
        }
        if (this.tickUnstuck(level)) {
            return;
        }
        switch (this.getActivity()) {
            case INTRO -> this.tickIntro(level);
            case GROUND -> this.tickGround(level);
            case ATTACK -> this.tickAttack(level);
            case TRANSITION -> this.tickTransition(level);
            case HEALING -> this.tickHealing(level);
            default -> {
            }
        }
    }

    private void tickCounters() {
        this.activityTimer++;
        this.animHold++;
        for (int i = 0; i < this.cooldowns.length; i++) {
            if (this.cooldowns[i] > 0) {
                this.cooldowns[i]--;
            }
        }
        this.pathRecalc = Math.max(0, this.pathRecalc - 1);
        this.reachCheck = Math.max(0, this.reachCheck - 1);
        this.leapCooldown = Math.max(0, this.leapCooldown - 1);
        this.healCooldown = Math.max(0, this.healCooldown - 1);
        while (!this.recentHits.isEmpty() && this.tickCount - this.recentHits.peekFirst() > GUARD_TRIGGER_WINDOW) {
            this.recentHits.pollFirst();
        }
        if (this.overlayTicks > 0 && --this.overlayTicks == 0) {
            this.entityData.set(DATA_OVERLAY, -1);
        }
    }

    private void tickIntro(ServerLevel level) {
        this.getNavigation().stop();
        int t = this.activityTimer;
        if (t % 5 == 0 && t < INTRO_RISE_TICK) {
            level.sendParticles(CRIMSON_DUST, this.getX(), this.getY() + 0.6D + t * 0.05D, this.getZ(), 6, 0.8D, 0.4D, 0.8D, 0.01D);
        }
        if (t >= INTRO_RISE_TICK && this.getAnim() == CrimsonAnim.INTRO_KNEEL) {
            this.setAnim(CrimsonAnim.INTRO_RISE);
            this.playSound(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), 2.0F, 0.6F);
        }
        if (t == INTRO_RISE_TICK + 22) { // the salute: the blade rings
            this.playSound(SoundEvents.ANVIL_USE, 1.2F, 1.6F);
            level.sendParticles(GOLD_DUST, this.getX(), this.getY() + 3.4D, this.getZ(), 16, 0.2D, 0.6D, 0.2D, 0.02D);
        }
        Player nearest = level.getNearestPlayer(this, 32.0D);
        if (nearest != null) {
            this.faceTarget(nearest);
        }
        if (t >= INTRO_TICKS) {
            this.setActivity(Activity.GROUND);
            this.setAnim(CrimsonAnim.COMBAT_IDLE);
        }
    }

    // ================================================================= targeting
    /** Who hurt him first while still in the fight, otherwise the nearest player or golem, with hysteresis. */
    private @Nullable LivingEntity chooseTarget(ServerLevel level) {
        return this.chooseTarget(level, false);
    }

    /** Runs the target choice now, as the brain does once a second (tests use it to check priority and hysteresis). */
    public @Nullable LivingEntity reconsiderTarget(ServerLevel level) {
        return this.chooseTarget(level, true);
    }

    private @Nullable LivingEntity chooseTarget(ServerLevel level, boolean now) {
        LivingEntity current = this.getTarget();
        boolean ok = current != null && current.isAlive() && this.canAttack(current)
                && this.distanceToSqr(current) <= FOLLOW_RANGE * FOLLOW_RANGE;
        if (ok && !now && this.tickCount % 20 != 0) {
            return current;
        }
        if (this.firstAttacker != null) {
            Entity first = level.getEntity(this.firstAttacker);
            if (first instanceof LivingEntity living && living.isAlive() && this.canAttack(living)
                    && this.distanceToSqr(living) <= FOLLOW_RANGE * FOLLOW_RANGE) {
                if (living != current) {
                    this.setTarget(living);
                }
                return living;
            }
            this.firstAttacker = null; // gone, dead or out of reach: the next blow decides
        }
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(FOLLOW_RANGE),
                en -> en.isAlive() && this.canAttack(en) && !(en instanceof Player p && p.isSpectator()))) {
            double d = this.distanceToSqr(e);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        // hysteresis: keep the current target unless the new one is clearly (4+ blocks) closer
        if (ok && (best == null || Math.sqrt(bestDist) > Math.sqrt(this.distanceToSqr(current)) - 4.0D)) {
            return current;
        }
        if (best != null && best != current) {
            this.setTarget(best);
        }
        return best != null ? best : (ok ? current : null);
    }

    private void trackTarget(LivingEntity target) {
        Vec3 now = target.position();
        Vec3 v = this.targetLastPos == Vec3.ZERO ? Vec3.ZERO : now.subtract(this.targetLastPos);
        this.targetVelocity = v.lengthSqr() > 4.0D ? Vec3.ZERO : this.targetVelocity.scale(0.5D).add(v.scale(0.5D));
        this.targetLastPos = now;
    }

    /** Where the target will be in {@code ticks} ticks, from the velocity the server tracked (not the client's). */
    public Vec3 predict(LivingEntity target, int ticks) {
        Vec3 lead = new Vec3(this.targetVelocity.x, 0.0D, this.targetVelocity.z).scale(Math.min(ticks, 20));
        return target.position().add(lead);
    }

    /** True when the target is moving away from him. */
    private boolean isRetreating(LivingEntity target) {
        Vec3 away = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        Vec3 v = this.targetVelocity.multiply(1.0D, 0.0D, 1.0D);
        return away.lengthSqr() > 1.0E-4D && v.length() > 0.08D && v.normalize().dot(away.normalize()) > 0.6D;
    }

    private void faceTarget(LivingEntity target) {
        this.faceDirection(target.getX() - this.getX(), target.getZ() - this.getZ());
    }

    private void faceDirection(double dx, double dz) {
        if (dx * dx + dz * dz < 1.0E-5D) {
            return;
        }
        float yaw = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.yBodyRot = yaw;
    }

    // ================================================================= GROUND
    private void tickGround(ServerLevel level) {
        this.setNoGravity(false);
        if (this.leapTick >= 0 && !this.leapIsSlam) {
            this.tickLeap(level); // a leap out of a dead end finishes before anything else, even if the target is gone
            return;
        }
        LivingEntity target = this.chooseTarget(level);
        if (target == null) {
            this.getNavigation().stop();
            this.idleTicks++;
            if (this.idleTicks % 160 == 0) {
                this.setAnim(CrimsonAnim.IDLE_LOOK);
            } else if (this.getAnim() != CrimsonAnim.IDLE_LOOK || this.animHold > 60) {
                this.setAnim(this.getStage() >= 3 ? CrimsonAnim.FURY_IDLE : CrimsonAnim.COMBAT_IDLE);
            }
            return;
        }
        this.idleTicks = 0;
        this.trackTarget(target);
        if (!this.vanguardsCalled && this.getStage() >= 2) { // the world was saved mid-awakening
            this.vanguardsCalled = true;
            this.summonVanguards(level, MAX_VANGUARDS);
        }
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double dist = Math.sqrt(this.distanceToSqr(target));
        double dy = target.getY() - this.getY();
        if (this.reachCheck <= 0) {
            Path path = this.getNavigation().createPath(target, 1);
            this.targetReachable = path != null && path.canReach();
            this.reachCheck = 10;
        }
        if (this.getStage() >= 2 && this.healCooldown <= 0 && this.onGround()
                && this.getHealth() < this.getMaxHealth() * HEAL_BELOW) {
            this.startHealing(level, target);
            return;
        }
        // three blows inside two seconds: he raises his guard
        if (this.recentHits.size() >= GUARD_TRIGGER_HITS && this.cooldowns[Attack.DEFENSIVE_GUARD.ordinal()] <= 0) {
            this.recentHits.clear();
            this.startAttack(Attack.DEFENSIVE_GUARD, target);
            return;
        }
        Attack pick = this.pickAttack(target, dist, dy);
        if (pick != Attack.NONE) {
            this.startAttack(pick, target);
            return;
        }
        this.chase(level, target, dist, dy);
        this.updateLocomotionAnim();
    }

    /** Weighted choice that respects range, height, line of sight, cooldowns, stage and the previous attack. */
    private Attack pickAttack(LivingEntity target, double dist, double dy) {
        if (!this.onGround()) {
            return Attack.NONE;
        }
        boolean sight = this.getSensing().hasLineOfSight(target);
        boolean sameLevel = dy <= 1.5D && dy >= -2.0D;
        boolean elevated = dy >= 1.0D && dy <= 4.0D;
        boolean retreating = this.isRetreating(target);
        int stage = this.getStage();
        List<Attack> options = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        if (sight && sameLevel && dist <= SLASH_REACH && this.ready(Attack.CRIMSON_SLASH)) {
            options.add(Attack.CRIMSON_SLASH);
            weights.add(6);
        }
        if (sight && sameLevel && this.ready(Attack.DASH_THRUST) && this.targetReachable
                && ((dist >= THRUST_MIN_RANGE && dist <= THRUST_MAX_RANGE) || (retreating && dist >= 3.0D && dist <= THRUST_MAX_RANGE))
                && this.level() instanceof ServerLevel level && groundAlong(level, this.position(), target.position())) {
            options.add(Attack.DASH_THRUST);
            weights.add(retreating ? 9 : 5);
        }
        if (sight && this.ready(Attack.HEAVY_SLAM)) {
            boolean pillar = elevated && dist <= SLAM_MAX_RANGE + 1.0D; // anti-pillar, at every stage
            boolean inRange = sameLevel && dist >= SLAM_MIN_RANGE && dist <= SLAM_MAX_RANGE;
            // from the Awakened Oath on, whoever stays glued to him right after a recovery is crushed from above
            boolean glued = stage >= 2 && sameLevel && dist < SLAM_MIN_RANGE && this.tickCount - this.recoveredAt <= 10;
            if (pillar) {
                options.add(Attack.HEAVY_SLAM);
                weights.add(12);
            } else if (glued) {
                options.add(Attack.HEAVY_SLAM);
                weights.add(8);
            } else if (stage >= 2 && inRange) {
                options.add(Attack.HEAVY_SLAM);
                weights.add(4);
            }
        }
        if (sight && this.ready(Attack.BLOOD_WAVE)) {
            boolean unreachable = !this.targetReachable && dist >= 3.0D && dist <= WAVE_MAX_RANGE;
            boolean high = dy > 4.0D && dist <= WAVE_MAX_RANGE;
            boolean fleeing = retreating && dist >= WAVE_MIN_RANGE && dist <= WAVE_MAX_RANGE;
            boolean inRange = dist >= WAVE_MIN_RANGE && dist <= WAVE_MAX_RANGE;
            if (unreachable || high || fleeing) { // anti-pillar and anti-flight, at every stage
                options.add(Attack.BLOOD_WAVE);
                weights.add(10);
            } else if (stage >= 2 && inRange) {
                options.add(Attack.BLOOD_WAVE);
                weights.add(4);
            }
        }
        if (options.isEmpty() && !this.anyOffenseReady() && this.ready(Attack.DEFENSIVE_GUARD) && dist <= 6.0D && sight
                && this.random.nextInt(8) == 0) {
            return Attack.DEFENSIVE_GUARD; // every blow is cooling down and the enemy is close: he waits behind the blade
        }
        if (options.size() > 1) {
            int idx = options.indexOf(this.lastAttack);
            if (idx >= 0) {
                options.remove(idx);
                weights.remove(idx);
            }
        }
        if (options.isEmpty()) {
            return Attack.NONE;
        }
        int total = weights.stream().mapToInt(Integer::intValue).sum();
        int roll = this.random.nextInt(total);
        for (int i = 0; i < options.size(); i++) {
            roll -= weights.get(i);
            if (roll < 0) {
                return options.get(i);
            }
        }
        return options.getFirst();
    }

    private boolean ready(Attack a) {
        return this.cooldowns[a.ordinal()] <= 0;
    }

    private boolean anyOffenseReady() {
        return this.ready(Attack.CRIMSON_SLASH) || this.ready(Attack.DASH_THRUST)
                || (this.getStage() >= 2 && (this.ready(Attack.HEAVY_SLAM) || this.ready(Attack.BLOOD_WAVE)));
    }

    private void chase(ServerLevel level, LivingEntity target, double dist, double dy) {
        double speed = dist > 9.0D ? 1.45D : 1.2D;
        boolean offense = this.anyOffenseReady();
        double standOff = offense ? 2.4D : 4.5D;
        if (dist > standOff + 0.6D || !offense && dist > 6.5D) {
            if (this.pathRecalc <= 0 || this.getNavigation().isDone()) {
                Vec3 goal = this.predict(target, Math.min(10, (int) (dist * 1.2D)));
                Path path = this.getNavigation().createPath(goal.x, goal.y, goal.z, 1);
                if (path != null && path.canReach()) {
                    this.getNavigation().moveTo(path, speed);
                } else {
                    this.getNavigation().moveTo(target, speed);
                }
                this.pathRecalc = 4;
            }
        } else if (!offense && this.pathRecalc <= 0) {
            // everything is cooling down: circle the target at a duelist's distance, only over safe ground
            Vec3 away = this.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
            if (away.lengthSqr() > 1.0E-4D) {
                away = away.normalize();
                boolean left = ((this.tickCount / 50) + this.getId()) % 2 == 0;
                double a = Math.toRadians(50.0D) * (left ? 1 : -1);
                Vec3 rot = new Vec3(away.x * Math.cos(a) - away.z * Math.sin(a), 0.0D, away.x * Math.sin(a) + away.z * Math.cos(a));
                Vec3 spot = target.position().add(rot.scale(4.5D));
                if (isSafeGround(level, BlockPos.containing(spot.x, this.getY(), spot.z))) {
                    this.getNavigation().moveTo(spot.x, this.getY(), spot.z, 1.3D);
                } else {
                    this.getNavigation().stop();
                }
            }
            this.pathRecalc = 6;
        } else if (offense && this.pathRecalc <= 0) {
            this.getNavigation().moveTo(target, speed);
            this.pathRecalc = 5;
        }
        // stuck: not getting any closer to a far target, or no path at all: he leaps towards it
        if (this.tickCount % 10 == 0) {
            boolean noProgress = dist > (offense ? 4.0D : 7.0D) && this.position().distanceToSqr(this.stuckAnchor) < 0.3D;
            this.stuckTicks = noProgress || (!this.targetReachable && dist > 4.0D) ? this.stuckTicks + 10 : 0;
            this.stuckAnchor = this.position();
        }
        if (this.stuckTicks >= 40 && this.leapCooldown <= 0 && this.onGround() && dist <= 14.0D && dy <= 5.0D) {
            Vec3 spot = this.leapSpot(level, target, Math.min(7.0D, dist - 1.5D));
            if (spot != null) {
                this.stuckTicks = 0;
                this.leapCooldown = 100;
                this.leapsTaken++;
                this.beginLeap(spot, false);
                this.setAnim(CrimsonAnim.STEP_JUMP);
            }
        }
    }

    /**
     * True when a straight dash from {@code from} to {@code to} runs over floor all the way: no pit, no lava and no wall
     * in between (he never lunges into a hole after someone standing on the far side).
     */
    public static boolean groundAlong(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 d = to.subtract(from).multiply(1.0D, 0.0D, 1.0D);
        double len = d.length();
        if (len < 1.0E-3D) {
            return true;
        }
        Vec3 step = d.normalize().scale(0.5D);
        for (double k = 0.5D; k < len; k += 0.5D) {
            Vec3 p = from.add(step.scale(k / 0.5D));
            BlockPos feet = BlockPos.containing(p.x, from.y + 0.01D, p.z);
            if (!level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                    && !level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()) {
                return false; // a wall
            }
            BlockState one = level.getBlockState(feet.below());
            BlockState two = level.getBlockState(feet.below(2));
            if (one.is(Blocks.LAVA) || (one.getCollisionShape(level, feet.below()).isEmpty()
                    && two.getCollisionShape(level, feet.below(2)).isEmpty())) {
                return false; // a hole (deeper than a step) or lava
            }
        }
        return true;
    }

    private static boolean isSafeGround(ServerLevel level, BlockPos feet) {
        for (int d = 1; d <= 3; d++) {
            BlockState below = level.getBlockState(feet.below(d));
            if (below.is(Blocks.LAVA) || below.is(Blocks.FIRE) || below.is(Blocks.MAGMA_BLOCK)) {
                return false;
            }
            if (!below.getCollisionShape(level, feet.below(d)).isEmpty()) {
                return true;
            }
        }
        return false; // a drop of more than three blocks
    }

    private void updateLocomotionAnim() {
        if (this.leapTick >= 0) {
            return; // the leap keeps its own animation
        }
        boolean moving = this.lastSpeed > 0.03D;
        CrimsonAnim want = !moving ? (this.getStage() >= 3 ? CrimsonAnim.FURY_IDLE : CrimsonAnim.COMBAT_IDLE)
                : this.lastSpeed > 0.19D ? CrimsonAnim.RUN : CrimsonAnim.WALK;
        CrimsonAnim cur = this.getAnim();
        boolean locomotion = cur == CrimsonAnim.WALK || cur == CrimsonAnim.RUN || cur == CrimsonAnim.COMBAT_IDLE
                || cur == CrimsonAnim.FURY_IDLE || cur == CrimsonAnim.IDLE_LOOK;
        if (want != cur && (locomotion ? this.animHold >= 6 : this.animHold >= 10)) {
            this.setAnim(want);
        }
    }

    // ================================================================= stuck and leaps
    /** Entombed in blocks for half a second: he forces his way to the nearest free spot nearby (no long teleport). */
    private boolean tickUnstuck(ServerLevel level) {
        if (this.isInWall() && this.getActivity() != Activity.DYING) {
            if (++this.inWallTicks >= 10) {
                this.inWallTicks = 0;
                Vec3 free = findStandSpot(level, this.position(), 3.0D, WIDTH, HEIGHT);
                if (free != null) {
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()),
                            this.getX(), this.getY() + 1.5D, this.getZ(), 30, 0.6D, 1.0D, 0.6D, 0.1D);
                    this.setPos(free.x, free.y, free.z);
                    this.unstuckMoves++;
                    this.playSound(SoundEvents.DEEPSLATE_BREAK, 1.5F, 0.6F);
                    return true;
                }
            }
        } else {
            this.inWallTicks = 0;
        }
        return false;
    }

    /**
     * A supported spot near {@code around} where a body of the given size fits, scanning each column from a little above
     * to a little below (works underground, unlike a heightmap).
     */
    public static @Nullable Vec3 findStandSpot(ServerLevel level, Vec3 around, double radius, float width, float height) {
        Vec3 best = null;
        double bestScore = Double.MAX_VALUE;
        int r = (int) Math.ceil(radius);
        int baseY = Mth.floor(around.y);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double hd = Math.sqrt(dx * dx + dz * dz);
                if (hd > radius) {
                    continue;
                }
                for (int dy = 3; dy >= -4; dy--) {
                    BlockPos feet = new BlockPos(Mth.floor(around.x) + dx, baseY + dy, Mth.floor(around.z) + dz);
                    BlockState below = level.getBlockState(feet.below());
                    if (below.getCollisionShape(level, feet.below()).isEmpty() || below.is(Blocks.LAVA)) {
                        continue;
                    }
                    Vec3 pos = new Vec3(feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D);
                    AABB box = new AABB(pos.x - width / 2.0D, pos.y + 0.01D, pos.z - width / 2.0D,
                            pos.x + width / 2.0D, pos.y + height, pos.z + width / 2.0D);
                    if (!level.noCollision(box)) {
                        continue;
                    }
                    double score = hd + Math.abs(dy) * 0.6D;
                    if (score < bestScore) {
                        bestScore = score;
                        best = pos;
                    }
                }
            }
        }
        return best;
    }

    /** A landing spot towards (or onto) the target, at most {@code maxDistance} blocks away horizontally. */
    private @Nullable Vec3 leapSpot(ServerLevel level, LivingEntity target, double maxDistance) {
        Vec3 to = target.position().subtract(this.position());
        Vec3 flat = new Vec3(to.x, 0.0D, to.z);
        double h = flat.length();
        Vec3 aim = h <= maxDistance ? target.position() : this.position().add(flat.normalize().scale(maxDistance)).add(0.0D, to.y, 0.0D);
        return findStandSpot(level, aim, 2.0D, WIDTH, HEIGHT);
    }

    private void beginLeap(Vec3 to, boolean slam) {
        this.leapFrom = this.position();
        this.leapTo = to;
        this.leapTick = 0;
        this.leapIsSlam = slam;
        this.leapTicks = slam ? SLAM_FLIGHT_TICKS : 12;
        this.leapHeight = SLAM_JUMP_HEIGHT + Math.max(0.0D, to.y - this.leapFrom.y);
        this.getNavigation().stop();
        this.playSound(ModSounds.CRIMSON_DASH.get(), 1.0F, 1.2F);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.1D, this.getZ(), 10, 0.6D, 0.05D, 0.6D, 0.02D);
        }
    }

    /** Height of the leap's arc at progress {@code s} in [0, 1]: a parabola that rises {@link #leapHeight} over the start. */
    private Vec3 leapPoint(double s) {
        double y = this.leapFrom.y + (this.leapTo.y - this.leapFrom.y) * s + 4.0D * this.leapHeight * s * (1.0D - s);
        return new Vec3(Mth.lerp(s, this.leapFrom.x, this.leapTo.x), y, Mth.lerp(s, this.leapFrom.z, this.leapTo.z));
    }

    /** Follows the planned arc with a velocity per tick (collisions still apply); returns true once he is down. */
    private boolean tickLeap(ServerLevel level) {
        if (this.leapTick < 0) {
            return true;
        }
        this.leapTick++;
        double s = Math.min(1.0D, this.leapTick / (double) this.leapTicks);
        Vec3 next = this.leapPoint(s);
        Vec3 step = next.subtract(this.position());
        this.setDeltaMovement(step);
        this.faceDirection(this.leapTo.x - this.leapFrom.x, this.leapTo.z - this.leapFrom.z);
        boolean down = this.leapTick >= this.leapTicks || (this.leapTick > 3 && this.onGround() && s > 0.5D)
                || this.leapTick > this.leapTicks + 6;
        if (down) {
            this.leapTick = -1;
            this.setDeltaMovement(0.0D, Math.min(0.0D, this.getDeltaMovement().y), 0.0D);
            if (!this.leapIsSlam) {
                this.setAnim(CrimsonAnim.COMBAT_IDLE);
                level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.1D, this.getZ(), 8, 0.6D, 0.05D, 0.6D, 0.02D);
                this.playSound(SoundEvents.NETHERITE_BLOCK_FALL, 1.4F, 0.6F);
            }
        }
        return down;
    }

    private void endLeap() {
        this.leapTick = -1;
    }

    public int getHealsDone() {
        return this.healsDone;
    }

    public float getHealedTotal() {
        return this.healedTotal;
    }

    /** Starts the self-heal: back away first, then channel (public for tests). */
    public boolean startHealing(ServerLevel level, @Nullable LivingEntity from) {
        Activity a = this.getActivity();
        if ((a != Activity.GROUND && a != Activity.ATTACK) || !this.isAlive()) {
            return false;
        }
        this.cancelAttack(false);
        this.endLeap();
        this.healPhase = 0;
        this.healLostWhileChannelling = 0.0F;
        this.healCooldown = HEAL_COOLDOWN;
        this.setActivity(Activity.HEALING);
        this.setAnim(CrimsonAnim.RUN);
        this.getNavigation().stop();
        this.playSound(ModSounds.CRIMSON_WAVE.get(), 1.2F, 0.5F);
        this.summonVanguards(level, MAX_VANGUARDS); // his own fight for him while he is away
        return true;
    }

    private void tickHealing(ServerLevel level) {
        LivingEntity target = this.getTarget();
        int t = this.activityTimer;
        if (this.healPhase == 0) { // back away, quickly
            double dist = target != null ? this.distanceTo(target) : HEAL_SAFE_DISTANCE;
            if (target != null && this.pathRecalc <= 0) {
                Vec3 away = this.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
                away = away.lengthSqr() > 1.0E-4D ? away.normalize() : Vec3.directionFromRotation(0.0F, this.getYRot() + 180.0F);
                Vec3 spot = findStandSpot(level, this.position().add(away.scale(12.0D)), 4.0D, WIDTH, HEIGHT);
                if (spot != null) {
                    this.getNavigation().moveTo(spot.x, spot.y, spot.z, 1.7D);
                }
                this.pathRecalc = 5;
            }
            this.setAnim(CrimsonAnim.RUN);
            boolean cornered = t > 8 && this.lastSpeed < 0.03D;
            if (dist >= HEAL_SAFE_DISTANCE || t >= HEAL_RETREAT_MAX || cornered) {
                this.healPhase = 1;
                this.activityTimer = 0;
                this.healLastHealth = this.getHealth();
                this.getNavigation().stop();
                this.setAnim(CrimsonAnim.HEAL_CHANNEL);
                this.playSound(ModSounds.CRIMSON_AWAKEN.get(), 1.4F, 1.3F);
            }
            return;
        }
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        if (target != null) {
            this.faceTarget(target);
        }
        // the red aura: rings rising around him
        double rise = (t % 20) / 20.0D * 3.2D;
        for (int i = 0; i < 10; i++) {
            double ang = Math.PI * 2.0D * i / 10.0D + t * 0.3D;
            level.sendParticles(AURA_DUST, this.getX() + Math.cos(ang) * 0.9D, this.getY() + rise, this.getZ() + Math.sin(ang) * 0.9D,
                    1, 0.0D, 0.02D, 0.0D, 0.0D);
        }
        if (t % 3 == 0) {
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.6D, this.getZ(), 2, 0.5D, 0.9D, 0.5D, 0.01D);
            level.sendParticles(RUBY_DUST, this.getX(), this.getY() + 3.1D, this.getZ(), 2, 0.15D, 0.05D, 0.15D, 0.0D);
        }
        // damage taken while channelling is tracked: too much and the heal breaks
        if (this.getHealth() < this.healLastHealth) {
            this.healLostWhileChannelling += this.healLastHealth - this.getHealth();
        }
        if (t % 4 == 0) {
            float step = (float) (this.getMaxHealth() * HEAL_PERCENT / (HEAL_CHANNEL_TICKS / 4.0D));
            float before = this.getHealth();
            this.heal(step);
            this.healedTotal += this.getHealth() - before;
        }
        this.healLastHealth = this.getHealth();
        boolean broken = this.healLostWhileChannelling >= this.getMaxHealth() * HEAL_INTERRUPT;
        if (t >= HEAL_CHANNEL_TICKS || broken) {
            if (!broken) {
                this.healsDone++;
            } else {
                this.playSound(ModSounds.CRIMSON_HURT.get(), 1.4F, 0.7F);
            }
            this.healCooldown = broken ? HEAL_COOLDOWN / 2 : HEAL_COOLDOWN;
            this.setActivity(Activity.GROUND);
            this.setAnim(this.getStage() >= 3 ? CrimsonAnim.FURY_IDLE : CrimsonAnim.COMBAT_IDLE);
        }
    }

    public boolean isLeaping() {
        return this.leapTick >= 0;
    }

    /** Leaps taken to get out of a dead end (no path, no progress). */
    public int getLeapsTaken() {
        return this.leapsTaken;
    }

    // ================================================================= attacks
    private int cooldownFor(Attack a) {
        int base = switch (a) {
            case CRIMSON_SLASH -> 30;
            case DASH_THRUST -> 80;
            case HEAVY_SLAM -> 120;
            case DEFENSIVE_GUARD -> 160;
            case BLOOD_WAVE -> 140;
            case RIPOSTE, NONE -> 0;
        };
        return this.getStage() >= 2 ? (int) Math.round(base * STAGE_TWO_COOLDOWN_FACTOR) : base;
    }

    /** Starts an attack (public for tests). Returns false if the state forbids it. */
    public boolean startAttack(Attack type, @Nullable LivingEntity target) {
        Activity a = this.getActivity();
        if (type == Attack.NONE || (a != Activity.GROUND && a != Activity.ATTACK) || !this.isAlive()) {
            return false;
        }
        this.attack = type;
        this.lastAttack = type;
        this.phase = Phase.PREPARE;
        this.phaseTimer = 0;
        this.attackHitDone = false;
        this.struck.clear();
        this.comboSlam = false;
        this.dashPlanned = false;
        // a little variation in every wind-up, so the rhythm cannot be counted (the last frame holds a moment longer)
        this.prepJitter = type == Attack.DEFENSIVE_GUARD || type == Attack.RIPOSTE ? 0 : this.random.nextInt(4);
        this.setActivity(Activity.ATTACK);
        this.getNavigation().stop();
        this.endLeap();
        if (target != null) {
            this.faceTarget(target);
        }
        switch (type) {
            case CRIMSON_SLASH -> this.setAnim(CrimsonAnim.SLASH_PREPARE);
            case DASH_THRUST -> this.setAnim(CrimsonAnim.THRUST_PREPARE);
            case HEAVY_SLAM -> {
                this.setAnim(CrimsonAnim.SLAM_PREPARE);
                this.slamCenter = target != null ? this.slamLanding(target) : this.position();
            }
            case DEFENSIVE_GUARD -> {
                this.setAnim(CrimsonAnim.GUARD_PREPARE);
                this.playSound(ModSounds.CRIMSON_GUARD.get(), 1.1F, 0.8F); // the guard locks with a ring of steel
            }
            case BLOOD_WAVE -> {
                this.setAnim(CrimsonAnim.WAVE_PREPARE);
                this.playSound(ModSounds.CRIMSON_WAVE.get(), 1.1F, 0.55F);
            }
            case RIPOSTE -> {
                this.phase = Phase.EXECUTE;
                this.setAnim(CrimsonAnim.RIPOSTE);
            }
            default -> {
            }
        }
        return true;
    }

    private void startRiposte(LivingEntity attacker) {
        if (this.getActivity() != Activity.ATTACK && this.getActivity() != Activity.GROUND) {
            return;
        }
        this.cooldowns[Attack.DEFENSIVE_GUARD.ordinal()] = this.cooldownFor(Attack.DEFENSIVE_GUARD);
        this.riposteTarget = attacker;
        this.startAttack(Attack.RIPOSTE, attacker);
        this.playSound(ModSounds.CRIMSON_SLASH.get(), 1.2F, 1.1F);
    }

    private int prepTicks() {
        int base = switch (this.attack) {
            case CRIMSON_SLASH -> SLASH_PREP;
            case DASH_THRUST -> THRUST_PREP;
            case HEAVY_SLAM -> SLAM_PREP;
            case DEFENSIVE_GUARD -> GUARD_PREP;
            case BLOOD_WAVE -> WAVE_PREP;
            default -> 0;
        };
        return base + (this.attack == Attack.HEAVY_SLAM ? 0 : this.prepJitter); // the slam's leap has a fixed rhythm
    }

    private int execTicks() {
        return switch (this.attack) {
            case CRIMSON_SLASH -> SLASH_EXEC;
            case DASH_THRUST -> THRUST_EXEC;
            case HEAVY_SLAM -> SLAM_EXEC;
            case DEFENSIVE_GUARD -> GUARD_HOLD;
            case RIPOSTE -> RIPOSTE_EXEC;
            case BLOOD_WAVE -> WAVE_EXEC;
            default -> 1;
        };
    }

    private int recoverTicks() {
        return switch (this.attack) {
            case CRIMSON_SLASH -> SLASH_RECOVER;
            case DASH_THRUST -> this.dashHit ? THRUST_RECOVER : THRUST_RECOVER_MISS;
            case HEAVY_SLAM -> SLAM_RECOVER;
            case DEFENSIVE_GUARD -> GUARD_RECOVER;
            case RIPOSTE -> RIPOSTE_RECOVER;
            case BLOOD_WAVE -> WAVE_RECOVER;
            default -> 1;
        };
    }

    /** Whether the attack can still land on the target as it stands now: the "never swing at the air" rule. */
    private boolean attackStillUseful(@Nullable LivingEntity target) {
        if (this.attack == Attack.DEFENSIVE_GUARD) {
            return true;
        }
        if (target == null || !target.isAlive()) {
            return false;
        }
        Vec3 d = target.position().subtract(this.position());
        double horizontal = Math.sqrt(d.x * d.x + d.z * d.z);
        boolean sameLevel = d.y <= 1.8D && d.y >= -2.2D;
        return switch (this.attack) {
            case CRIMSON_SLASH -> sameLevel && horizontal <= SLASH_REACH + 0.8D;
            case DASH_THRUST -> sameLevel && horizontal <= THRUST_MAX_RANGE + 1.5D && this.getSensing().hasLineOfSight(target);
            case HEAVY_SLAM -> d.y >= -2.2D && d.y <= 4.5D && horizontal <= SLAM_MAX_RANGE + 2.0D;
            case BLOOD_WAVE -> horizontal <= WAVE_DISTANCE && this.getSensing().hasLineOfSight(target);
            default -> true;
        };
    }

    /** Drops the current attack; {@code interrupted} plays the recoil animation. */
    private void cancelAttack(boolean interrupted) {
        if (this.attack != Attack.NONE && this.getActivity() == Activity.ATTACK) {
            this.cooldowns[this.attack.ordinal()] = Math.max(this.cooldowns[this.attack.ordinal()], 20);
            if (interrupted) {
                this.attacksCancelled++;
                this.setAnim(CrimsonAnim.ATTACK_CANCEL);
            }
        }
        this.attack = Attack.NONE;
        this.phaseTimer = 0;
    }

    /** Drops the current attack at once and returns to the ground state. */
    public void abortAttack() {
        this.cancelAttack(false);
        this.endLeap();
        if (this.getActivity() == Activity.ATTACK) {
            this.setActivity(Activity.GROUND);
        }
    }

    private void tickAttack(ServerLevel level) {
        LivingEntity target = this.attack == Attack.RIPOSTE && this.riposteTarget != null ? this.riposteTarget : this.getTarget();
        if (target != null) {
            this.trackTarget(target);
        }
        this.phaseTimer++;
        switch (this.phase) {
            case PREPARE -> this.tickPrepare(level, target);
            case EXECUTE -> this.tickExecute(level, target);
            case RECOVER -> {
                this.getNavigation().stop();
                if (this.attack != Attack.DASH_THRUST && this.attack != Attack.HEAVY_SLAM && this.attack != Attack.CRIMSON_SLASH) {
                    this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
                }
                if (this.attack == Attack.CRIMSON_SLASH && this.phaseTimer == 2 && this.onGround()) { // a backflip out of reach
                    Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
                    this.setDeltaMovement(-f.x * 0.45D, 0.42D, -f.z * 0.45D);
                    level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.1D, this.getZ(), 6, 0.4D, 0.05D, 0.4D, 0.02D);
                }
                if (this.phaseTimer >= this.recoverTicks()) {
                    this.finishAttack();
                }
            }
        }
    }

    private void tickPrepare(ServerLevel level, @Nullable LivingEntity target) {
        this.getNavigation().stop();
        int prep = this.prepTicks();
        boolean aiming = target != null && this.phaseTimer < prep - 3;
        if (this.attack == Attack.HEAVY_SLAM) {
            this.tickSlamPrepare(level, target);
            return;
        }
        if (aiming && this.attack == Attack.DEFENSIVE_GUARD) {
            this.turnTowards(target, GUARD_TURN_RATE);
        } else if (aiming) {
            this.faceTarget(target); // keeps tracking until a few ticks before the blow
            if (this.attack == Attack.DASH_THRUST) {
                this.planDash(target); // the line drawn on the floor is the line he will run: frozen when the tracking stops
            }
            if (this.attack == Attack.CRIMSON_SLASH && this.onGround()) {
                Vec3 d = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
                if (d.lengthSqr() > 2.6D * 2.6D) {
                    Vec3 s = d.normalize().scale(0.09D);
                    this.setDeltaMovement(s.x, this.getDeltaMovement().y, s.z); // presses in during the wind-up
                }
            }
        }
        this.telegraph(level, target);
        if (this.phaseTimer >= prep) {
            if (this.attack == Attack.DASH_THRUST && !this.dashPlanned && target != null) {
                this.planDash(target);
            }
            boolean safeLine = this.attack != Attack.DASH_THRUST
                    || groundAlong(level, this.position(), this.position().add(this.dashDir.scale(this.dashLength)));
            if (!this.attackStillUseful(target) || !safeLine) {
                this.cancelAttack(true); // never a blow at the air, never a dash into a hole
                this.setActivity(Activity.GROUND);
                return;
            }
            this.beginExecute(level, target);
        }
    }

    /** Fixes the dash's line (direction and length) towards where the target is going. */
    private void planDash(LivingEntity target) {
        Vec3 d = this.dashGoal(target).subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        this.dashDir = d.lengthSqr() > 1.0E-4D ? d.normalize()
                : Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        this.dashLength = Mth.clamp(d.length() + 1.5D, 3.0D, THRUST_MAX_DISTANCE);
        this.dashPlanned = true;
    }

    /** Turns towards the target by at most {@code maxDegrees} this tick. */
    private void turnTowards(LivingEntity target, float maxDegrees) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        if (dx * dx + dz * dz < 1.0E-5D) {
            return;
        }
        float want = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
        float yaw = Mth.approachDegrees(this.getYRot(), want, maxDegrees);
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.yBodyRot = yaw;
    }

    /** Readable warnings: a ruby glow at the visor, a crimson line on the ground, a charged blade, a locked guard. */
    private void telegraph(ServerLevel level, @Nullable LivingEntity target) {
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        switch (this.attack) {
            case CRIMSON_SLASH -> {
                if (this.phaseTimer % 2 == 0) {
                    level.sendParticles(RUBY_DUST, this.getX() + f.x * 0.35D, this.getY() + 3.15D, this.getZ() + f.z * 0.35D,
                            3, 0.1D, 0.03D, 0.1D, 0.0D);
                }
            }
            case DASH_THRUST -> {
                if (this.phaseTimer % 3 == 0 && this.dashPlanned) {
                    double len = this.dashLength;
                    Vec3 dir = this.dashDir;
                    for (double k = 1.0D; k <= len; k += 0.8D) {
                        Vec3 p = this.position().add(dir.scale(k));
                        level.sendParticles(CRIMSON_DUST, p.x, p.y + 0.1D, p.z, 1, 0.05D, 0.0D, 0.05D, 0.0D);
                    }
                    level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.1D, this.getZ(), 3, 0.4D, 0.05D, 0.4D, 0.01D);
                }
            }
            case DEFENSIVE_GUARD -> {
                if (this.phaseTimer % 2 == 0) {
                    Vec3 p = this.position().add(f.scale(0.8D)).add(0.0D, 1.9D, 0.0D);
                    level.sendParticles(GOLD_DUST, p.x, p.y, p.z, 2, 0.15D, 0.15D, 0.15D, 0.0D);
                }
            }
            case BLOOD_WAVE -> {
                if (this.phaseTimer % 2 == 0) {
                    Vec3 side = new Vec3(-f.z, 0.0D, f.x).scale(-0.7D);
                    for (int i = 0; i < 4; i++) {
                        double h = 1.6D + i * 0.45D;
                        level.sendParticles(i % 2 == 0 ? RUBY_DUST : GOLD_DUST, this.getX() + side.x + f.x * 0.6D, this.getY() + h,
                                this.getZ() + side.z + f.z * 0.6D, 1, 0.08D, 0.1D, 0.08D, 0.0D);
                    }
                }
            }
            default -> {
            }
        }
    }

    private void beginExecute(ServerLevel level, @Nullable LivingEntity target) {
        this.phase = Phase.EXECUTE;
        this.phaseTimer = 0;
        this.attackHitDone = false;
        this.struck.clear();
        switch (this.attack) {
            case CRIMSON_SLASH -> {
                this.setAnim(CrimsonAnim.SLASH_EXECUTE);
                this.playSound(ModSounds.CRIMSON_SLASH.get(), 1.1F, 0.7F);
                Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
                this.setDeltaMovement(f.x * 0.55D, 0.3D, f.z * 0.55D); // a leaping cut: he flies into the blow
            }
            case DASH_THRUST -> {
                this.setAnim(CrimsonAnim.THRUST_EXECUTE);
                if (!this.dashPlanned) { // no target to aim at: straight ahead
                    this.dashDir = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
                    this.dashLength = THRUST_MAX_DISTANCE;
                }
                this.dashFrom = this.position();
                this.dashHit = false;
                this.dashMissed = false;
                this.faceDirection(this.dashDir.x, this.dashDir.z);
                this.playSound(ModSounds.CRIMSON_DASH.get(), 1.2F, 0.8F);
            }
            case DEFENSIVE_GUARD -> this.setAnim(CrimsonAnim.GUARD_HOLD);
            case BLOOD_WAVE -> {
                this.setAnim(CrimsonAnim.WAVE_EXECUTE);
                if (target != null) {
                    this.faceTarget(target);
                }
            }
            case HEAVY_SLAM -> this.setAnim(CrimsonAnim.SLAM_EXECUTE);
            default -> {
            }
        }
    }

    private void tickExecute(ServerLevel level, @Nullable LivingEntity target) {
        switch (this.attack) {
            case CRIMSON_SLASH -> {
                if (this.phaseTimer == SLASH_HIT_TICK) {
                    this.slashImpact(level);
                }
            }
            case DASH_THRUST -> {
                if (this.tickDash(level)) {
                    this.endExecute(level, target);
                }
                return;
            }
            case HEAVY_SLAM -> {
                boolean down = this.leapTick < 0 || this.tickLeap(level);
                if (down && !this.attackHitDone) {
                    this.attackHitDone = true;
                    this.slamImpact(level);
                }
            }
            case DEFENSIVE_GUARD -> {
                this.getNavigation().stop();
                this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
                if (target != null) {
                    this.turnTowards(target, GUARD_TURN_RATE); // slowly: circling him is the answer to the guard
                }
                this.telegraph(level, target);
            }
            case RIPOSTE -> {
                if (target != null && this.phaseTimer <= RIPOSTE_HIT_TICK) {
                    this.faceTarget(target);
                }
                if (this.phaseTimer == RIPOSTE_HIT_TICK) {
                    this.riposteImpact(level);
                }
            }
            case BLOOD_WAVE -> {
                if (this.phaseTimer == WAVE_RELEASE_TICK) {
                    this.releaseWave(level, target);
                }
            }
            default -> {
            }
        }
        if (this.phaseTimer >= this.execTicks()) {
            this.endExecute(level, target);
        }
    }

    private void endExecute(ServerLevel level, @Nullable LivingEntity target) {
        if (this.attack == Attack.HEAVY_SLAM && !this.attackHitDone) {
            this.attackHitDone = true;
            this.endLeap();
            this.slamImpact(level); // the blade always comes down
        }
        // the fury's combo: a dash that found its mark goes straight into the leaping slam, skipping its recovery
        if (this.attack == Attack.DASH_THRUST && this.getStage() >= 3 && this.dashHit && target != null && target.isAlive()
                && this.distanceTo(target) <= SLAM_MAX_RANGE + 1.0D) {
            this.cooldowns[Attack.DASH_THRUST.ordinal()] = this.cooldownFor(Attack.DASH_THRUST);
            this.combos++;
            this.startAttack(Attack.HEAVY_SLAM, target);
            this.comboSlam = true;
            return;
        }
        this.phase = Phase.RECOVER;
        this.phaseTimer = 0;
        switch (this.attack) {
            case CRIMSON_SLASH -> this.setAnim(CrimsonAnim.SLASH_RECOVERY);
            case DASH_THRUST -> this.setAnim(this.dashHit ? CrimsonAnim.THRUST_RECOVERY : CrimsonAnim.THRUST_RECOVERY_MISS);
            case HEAVY_SLAM -> this.setAnim(CrimsonAnim.SLAM_RECOVERY);
            case DEFENSIVE_GUARD -> this.setAnim(CrimsonAnim.GUARD_RECOVERY);
            case BLOOD_WAVE -> this.setAnim(CrimsonAnim.WAVE_RECOVERY);
            case RIPOSTE -> {
                // the counter-cut's own animation (10 ticks) covers its short recovery
            }
            default -> {
            }
        }
    }

    private void finishAttack() {
        this.recoveredAt = this.tickCount;
        this.cooldowns[this.attack.ordinal()] = Math.max(this.cooldowns[this.attack.ordinal()], this.cooldownFor(this.attack));
        this.attack = Attack.NONE;
        this.riposteTarget = null;
        this.setActivity(Activity.GROUND);
        this.setAnim(this.getStage() >= 3 ? CrimsonAnim.FURY_IDLE : CrimsonAnim.COMBAT_IDLE);
    }

    public boolean isComboSlam() {
        return this.comboSlam;
    }

    /** Where the last Heavy Overhead Slam bit the ground (the centre of its two damage rings). */
    public Vec3 getLastSlamCenter() {
        return this.lastImpact;
    }

    /** Where the last slam's warning rings were drawn on the floor. */
    public Vec3 getSlamRingCenter() {
        return this.ringCenter;
    }

    /** The point the blade bites when he lands at {@code feet}, facing the way he faces now. */
    private Vec3 bladePoint(Vec3 feet) {
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        return feet.add(f.scale(1.3D));
    }

    /** The cooldown an attack gets when it ends, in the current stage. */
    public int cooldownTicks(Attack a) {
        return this.cooldownFor(a);
    }

    // ---------------------------------------------------------------- crimson_slash
    private void slashImpact(ServerLevel level) {
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 left = new Vec3(-f.z, 0.0D, f.x);
        for (int i = -9; i <= 9; i++) { // the crimson arc of the sweep
            double ang = Math.toRadians(SLASH_ARC / 2.0D) * i / 9.0D;
            Vec3 p = this.position().add(f.scale(Math.cos(ang) * 3.2D)).add(left.scale(Math.sin(ang) * 3.2D));
            level.sendParticles(i % 3 == 0 ? RUBY_DUST : CRIMSON_DUST, p.x, p.y + 1.8D, p.z, 1, 0.05D, 0.05D, 0.05D, 0.0D);
        }
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + f.x * 2.0D, this.getY() + 1.8D, this.getZ() + f.z * 2.0D, 1, 0, 0, 0, 0);
        for (LivingEntity v : this.victimsInFront(level, SLASH_REACH, SLASH_ARC)) {
            this.launchAway(level, v, this.damageFor(SLASH_DAMAGE), SLASH_KNOCKBACK, 0.35D);
            this.slashesLanded++;
        }
    }

    // ---------------------------------------------------------------- dash_thrust
    /** The point the dash aims at: where the target will be when the blade gets there, at most 9 blocks away. */
    private Vec3 dashGoal(LivingEntity target) {
        double dist = this.distanceTo(target);
        return this.predict(target, (int) Math.ceil(dist / 1.1D) + THRUST_EXEC / 2);
    }

    /** One tick of the advance; true when it is over (distance covered, wall, or the blade found its mark). */
    private boolean tickDash(ServerLevel level) {
        double done = this.position().subtract(this.dashFrom).multiply(1.0D, 0.0D, 1.0D).length();
        double speed = THRUST_MAX_DISTANCE / (double) THRUST_EXEC + 0.05D;
        Vec3 step = this.dashDir.scale(Math.min(speed, Math.max(0.0D, this.dashLength - done)));
        this.setDeltaMovement(step.x, this.phaseTimer == 1 ? 0.22D : this.getDeltaMovement().y, step.z);
        if (this.phaseTimer == 1) { // the take-off: a sonic ring and a burst of dust
            level.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 1.4D, this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.1D, this.getZ(), 16, 0.7D, 0.05D, 0.7D, 0.06D);
            this.playSound(ModSounds.CRIMSON_DASH.get(), 1.4F, 1.4F);
        }
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.0D, this.getZ(), 3, 0.3D, 0.6D, 0.3D, 0.02D);
        level.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.6D, this.getZ(), 4, 0.4D, 0.7D, 0.4D, 0.3D);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + this.dashDir.x, this.getY() + 1.2D, this.getZ() + this.dashDir.z, 1, 0, 0, 0, 0);
        BlockState under = level.getBlockState(this.blockPosition().below());
        if (!under.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, under), this.getX(), this.getY() + 0.1D, this.getZ(), 8, 0.4D, 0.05D, 0.4D, 0.1D);
        }
        // a trail of crimson behind him, interpolated between his last and his next position
        for (int i = 0; i < 4; i++) {
            Vec3 p = this.position().add(step.scale(i / 4.0D));
            level.sendParticles(i % 2 == 0 ? CRIMSON_DUST : RUBY_DUST, p.x, p.y + 1.2D, p.z, 1, 0.1D, 0.3D, 0.1D, 0.0D);
        }
        AABB sweep = this.getBoundingBox().inflate(0.6D, 0.0D, 0.6D).expandTowards(step);
        for (LivingEntity v : level.getEntitiesOfClass(LivingEntity.class, sweep, e -> e != this && e.isAlive() && this.canAttack(e))) {
            if (this.struck.add(v.getId())) {
                this.launchAway(level, v, this.damageFor(THRUST_DAMAGE), 1.7D, 0.4D);
                Vec3 hit = v.position().add(0.0D, v.getBbHeight() * 0.5D, 0.0D);
                level.sendParticles(ParticleTypes.EXPLOSION, hit.x, hit.y, hit.z, 2, 0.2D, 0.2D, 0.2D, 0.0D);
                level.sendParticles(ParticleTypes.END_ROD, hit.x, hit.y, hit.z, 10, 0.3D, 0.3D, 0.3D, 0.2D);
                level.sendParticles(CRIMSON_DUST, hit.x, hit.y, hit.z, 24, 0.5D, 0.5D, 0.5D, 0.15D);
                this.playSound(ModSounds.CRIMSON_SLAM.get(), 1.0F, 1.4F);
                v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, THRUST_SLOW_TICKS, 0), this);
                this.dashHit = true;
                this.thrustHits++;
            }
        }
        boolean wall = this.phaseTimer > 1 && this.horizontalCollision && this.lastSpeed < 0.2D;
        boolean far = done >= this.dashLength - 0.05D;
        if (this.dashHit || wall || far || this.phaseTimer >= THRUST_EXEC + 4) {
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
            this.dashMissed = !this.dashHit;
            return true;
        }
        return false;
    }

    public boolean isDashMissed() {
        return this.dashMissed;
    }

    /** Whether the last path check found a way to walk up to the target. */
    public boolean isTargetReachable() {
        return this.targetReachable;
    }

    // ---------------------------------------------------------------- heavy_overhead_slam
    /** Where the slam comes down: on the target, or on top of the pillar it stands on, or as close as the floor allows. */
    private Vec3 slamLanding(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel level)) {
            return target.position();
        }
        Vec3 goal = this.predict(target, SLAM_FLIGHT_TICKS / 2);
        Vec3 to = goal.subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        double h = to.length();
        Vec3 aim = h > SLAM_MAX_RANGE ? this.position().add(to.normalize().scale(SLAM_MAX_RANGE)).add(0.0D, goal.y - this.getY(), 0.0D)
                : goal;
        // land just short of the target so the blade, not his feet, comes down on it
        if (h > 1.5D) {
            aim = aim.subtract(to.normalize().scale(1.0D));
        }
        Vec3 spot = findStandSpot(level, new Vec3(aim.x, Math.max(aim.y, target.getY()), aim.z), 2.0D, WIDTH, HEIGHT);
        return spot != null ? spot : this.position();
    }

    private void tickSlamPrepare(ServerLevel level, @Nullable LivingEntity target) {
        if (this.phaseTimer < SLAM_JUMP_TICK - 3 && target != null) {
            this.faceTarget(target);
            this.slamCenter = this.slamLanding(target); // the ring follows the target until he commits
        }
        if (this.phaseTimer < SLAM_JUMP_TICK) {
            this.ringCenter = this.bladePoint(this.slamCenter); // the rings mark where the blade, not his feet, comes down
        }
        if (this.phaseTimer % 3 == 0 && this.phaseTimer < SLAM_JUMP_TICK + 6) { // the gold and crimson ring on the floor
            Vec3 r = this.ringCenter;
            for (int i = 0; i < 16; i++) {
                double a = Math.PI * 2.0D * i / 16.0D;
                level.sendParticles(GOLD_DUST, r.x + Math.cos(a) * SLAM_RADIUS, r.y + 0.15D, r.z + Math.sin(a) * SLAM_RADIUS, 1, 0, 0, 0, 0);
                level.sendParticles(CRIMSON_DUST, r.x + Math.cos(a) * SLAM_EPICENTER_RADIUS, r.y + 0.15D,
                        r.z + Math.sin(a) * SLAM_EPICENTER_RADIUS, 1, 0, 0, 0, 0);
            }
        }
        if (this.phaseTimer == SLAM_JUMP_TICK) {
            if (!this.attackStillUseful(target)) {
                this.cancelAttack(true);
                this.setActivity(Activity.GROUND);
                return;
            }
            this.beginLeap(this.slamCenter, true);
        }
        if (this.phaseTimer > SLAM_JUMP_TICK) {
            this.tickLeap(level);
        }
        if (this.phaseTimer >= SLAM_PREP) {
            this.beginExecute(level, target);
        }
    }

    private void slamImpact(ServerLevel level) {
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 c = this.bladePoint(this.position()); // where the blade bites
        this.lastImpact = c;
        this.playSound(ModSounds.CRIMSON_SLAM.get(), 2.0F, 0.65F);
        BlockState floor = level.getBlockState(BlockPos.containing(c.x, c.y - 0.5D, c.z));
        if (!floor.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, floor), c.x, c.y + 0.2D, c.z, 50, 1.8D, 0.2D, 1.8D, 0.15D);
        }
        for (int ring = 1; ring <= 4; ring++) { // the shockwave rolls outwards in gold and crimson
            double r = ring;
            for (int i = 0; i < 20; i++) {
                double a = Math.PI * 2.0D * i / 20.0D;
                level.sendParticles(ring % 2 == 0 ? GOLD_DUST : CRIMSON_DUST, c.x + Math.cos(a) * r, c.y + 0.2D, c.z + Math.sin(a) * r,
                        1, 0.05D, 0.05D, 0.05D, 0.0D);
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y + 0.5D, c.z, 2, 0.4D, 0.1D, 0.4D, 0.0D);
        this.slamsLanded++;
        for (LivingEntity v : this.victimsAround(level, c, SLAM_RADIUS)) {
            double d = v.position().subtract(c).multiply(1.0D, 0.0D, 1.0D).length();
            if (d > SLAM_EPICENTER_RADIUS && !this.getSensing().hasLineOfSight(v)) {
                continue; // a wall shelters from the outer ring
            }
            float damage = this.damageFor(d <= SLAM_EPICENTER_RADIUS ? SLAM_EPICENTER_DAMAGE : SLAM_EDGE_DAMAGE);
            if (v instanceof Player player) {
                disableShield(level, player);
            }
            v.hurtServer(level, this.damageSources().mobAttack(this), damage);
            Vec3 away = v.position().subtract(c).multiply(1.0D, 0.0D, 1.0D);
            Vec3 push = away.lengthSqr() > 1.0E-4D ? away.normalize().scale(1.2D) : Vec3.ZERO;
            v.setDeltaMovement(push.x, SLAM_LIFT, push.z);
            v.hurtMarked = true;
        }
    }

    /** A shield in the hands of whoever stands in the shockwave is useless for {@link #SHIELD_DISABLE_TICKS} ticks. */
    public static boolean disableShield(ServerLevel level, Player player) {
        ItemStack shield = player.getUseItem().is(Items.SHIELD) ? player.getUseItem()
                : player.getOffhandItem().is(Items.SHIELD) ? player.getOffhandItem()
                : player.getMainHandItem().is(Items.SHIELD) ? player.getMainHandItem() : ItemStack.EMPTY;
        if (shield.isEmpty()) {
            return false;
        }
        player.getCooldowns().addCooldown(shield, SHIELD_DISABLE_TICKS);
        if (player.isUsingItem() && player.getUseItem() == shield) {
            player.stopUsingItem();
        }
        level.broadcastEntityEvent(player, (byte) 30); // the vanilla shield-break animation and sound
        level.playSound(null, player.blockPosition(), SoundEvents.SHIELD_BREAK.value(), SoundSource.PLAYERS, 1.2F, 0.8F);
        return true;
    }

    // ---------------------------------------------------------------- riposte
    private void riposteImpact(ServerLevel level) {
        this.ripostes++;
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + f.x * 1.8D, this.getY() + 1.8D, this.getZ() + f.z * 1.8D, 1, 0, 0, 0, 0);
        level.sendParticles(GOLD_DUST, this.getX() + f.x * 1.5D, this.getY() + 1.8D, this.getZ() + f.z * 1.5D, 14, 0.6D, 0.3D, 0.6D, 0.05D);
        for (LivingEntity v : this.victimsInFront(level, RIPOSTE_REACH, 120.0D)) {
            this.launchAway(level, v, this.damageFor(RIPOSTE_DAMAGE), RIPOSTE_PUSH, RIPOSTE_LIFT);
        }
    }

    // ---------------------------------------------------------------- blood_crescent_wave
    private void releaseWave(ServerLevel level, @Nullable LivingEntity target) {
        Vec3 start = this.position().add(0.0D, 2.2D, 0.0D);
        Vec3 dir;
        if (target != null) {
            double dist = this.distanceTo(target);
            Vec3 aim = this.predict(target, (int) Math.ceil(dist / WAVE_SPEED)).add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
            dir = aim.subtract(start);
        } else {
            dir = Vec3.directionFromRotation(0.0F, this.getYRot());
        }
        dir = dir.lengthSqr() > 1.0E-4D ? dir.normalize() : Vec3.directionFromRotation(0.0F, this.getYRot());
        Vec3 f = new Vec3(dir.x, 0.0D, dir.z);
        Vec3 origin = start.add(f.lengthSqr() > 1.0E-4D ? f.normalize().scale(1.0D) : Vec3.ZERO);
        this.waves.add(new Wave(origin, dir));
        this.wavesReleased++;
        this.playSound(ModSounds.CRIMSON_WAVE.get(), 1.4F, 0.7F);
    }

    /** Moves every crescent in flight: an interpolated trail, hits once per target, stops at walls with a burst. */
    private void tickWaves(ServerLevel level) {
        if (this.waves.isEmpty()) {
            return;
        }
        var it = this.waves.iterator();
        while (it.hasNext()) {
            Wave w = it.next();
            Vec3 side = new Vec3(-w.dir.z, 0.0D, w.dir.x);
            side = side.lengthSqr() > 1.0E-4D ? side.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
            int sub = 6;
            for (int k = 1; k <= sub && !w.done; k++) {
                Vec3 p = w.pos.add(w.dir.scale(WAVE_SPEED * k / sub));
                // the crescent: an arc of points across the direction of flight, bowed forwards
                for (int i = -3; i <= 3; i++) {
                    double bow = (9 - i * i) * 0.06D;
                    Vec3 q = p.add(side.scale(i * 0.35D)).add(w.dir.scale(bow - 0.5D));
                    level.sendParticles(i == 0 ? RUBY_DUST : (i + k) % 3 == 0 ? OBSIDIAN_DUST : CRIMSON_DUST, q.x, q.y, q.z, 1, 0.02D, 0.04D, 0.02D, 0.0D);
                }
                BlockPos bp = BlockPos.containing(p);
                if (!level.getBlockState(bp).getCollisionShape(level, bp).isEmpty()) {
                    this.waveBurst(level, p);
                    w.done = true;
                    break;
                }
                for (LivingEntity v : level.getEntitiesOfClass(LivingEntity.class, new AABB(p, p).inflate(WAVE_HIT_RADIUS),
                        e -> e != this && e.isAlive() && this.isFoe(e))) {
                    if (w.hit.add(v.getId())) {
                        v.hurtServer(level, this.damageSources().mobAttack(this), this.damageFor(WAVE_DAMAGE));
                        v.setDeltaMovement(w.dir.x * 1.0D, 0.35D, w.dir.z * 1.0D);
                        v.hurtMarked = true;
                        this.waveHits++;
                        this.waveBurst(level, p);
                    }
                }
            }
            w.pos = w.pos.add(w.dir.scale(WAVE_SPEED));
            w.travelled += WAVE_SPEED;
            if (w.travelled >= WAVE_DISTANCE && !w.done) {
                this.waveBurst(level, w.pos);
                w.done = true;
            }
            if (w.done) {
                it.remove();
            }
        }
    }

    private void waveBurst(ServerLevel level, Vec3 p) {
        level.sendParticles(CRIMSON_DUST, p.x, p.y, p.z, 20, 0.4D, 0.4D, 0.4D, 0.08D);
        level.sendParticles(OBSIDIAN_DUST, p.x, p.y, p.z, 10, 0.3D, 0.3D, 0.3D, 0.05D);
        level.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 8, 0.3D, 0.3D, 0.3D, 0.2D);
    }

    // ---------------------------------------------------------------- shared hit helpers
    /** Targets in a frontal cone of {@code arcDegrees} and within {@code reach} blocks, in line of sight. */
    private List<LivingEntity> victimsInFront(ServerLevel level, double reach, double arcDegrees) {
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        double minDot = Math.cos(Math.toRadians(arcDegrees / 2.0D));
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(reach, 1.0D, reach),
                en -> en != this && en.isAlive() && this.canAttack(en))) {
            Vec3 d = e.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            double h = d.length();
            double edge = h - e.getBbWidth() / 2.0D - this.getBbWidth() / 2.0D;
            boolean sameLevel = e.getY() - this.getY() <= 2.6D && e.getY() - this.getY() >= -2.0D;
            if (edge <= reach && sameLevel && (h < 0.8D || f.dot(d.normalize()) >= minDot) && this.getSensing().hasLineOfSight(e)) {
                out.add(e);
            }
        }
        return out;
    }

    private List<LivingEntity> victimsAround(ServerLevel level, Vec3 center, double radius) {
        List<LivingEntity> out = new ArrayList<>();
        AABB box = new AABB(center.x - radius, center.y - 1.5D, center.z - radius, center.x + radius, center.y + 3.0D, center.z + radius);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, en -> en != this && en.isAlive() && this.canAttack(en))) {
            Vec3 d = e.position().subtract(center).multiply(1.0D, 0.0D, 1.0D);
            if (d.lengthSqr() <= radius * radius) {
                out.add(e);
            }
        }
        return out;
    }

    /** Hurts first (the hit's own knock would otherwise override the launch), then throws the victim away from him. */
    private void launchAway(ServerLevel level, LivingEntity victim, float damage, double push, double lift) {
        victim.hurtServer(level, this.damageSources().mobAttack(this), damage);
        Vec3 away = victim.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (away.lengthSqr() > 1.0E-4D) {
            away = away.normalize();
            victim.setDeltaMovement(away.x * push, Math.max(victim.getDeltaMovement().y, lift), away.z * push);
            victim.hurtMarked = true;
        }
    }

    // ================================================================= persistence
    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        // saved mid-transition: the stage it was becoming (the transition is never replayed)
        output.putInt("Stage", this.getActivity() == Activity.TRANSITION ? Math.max(this.getStage(), this.pendingStage) : this.getStage());
        output.putInt("DeathTicks", this.deathTicks);
        output.putInt("Transitions", this.stageTransitions);
        output.putBoolean("VanguardsCalled", this.vanguardsCalled);
        output.putBoolean("SwordDelivered", this.swordDelivered);
        if (this.firstAttacker != null) {
            output.putString("FirstAttacker", this.firstAttacker.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.applyStage(input.getIntOr("Stage", 1));
        this.stageTransitions = input.getIntOr("Transitions", 0);
        this.vanguardsCalled = input.getBooleanOr("VanguardsCalled", false);
        this.swordDelivered = input.getBooleanOr("SwordDelivered", false);
        this.firstAttacker = parseUuid(input.getStringOr("FirstAttacker", ""));
        this.deathTicks = input.getIntOr("DeathTicks", 0);
        this.setActivity(Activity.GROUND);
        this.bossEvent.setVisible(true);
        if (this.getHealth() <= 0.0F) { // saved during the death cinematic: it carries on from where it was
            this.rewardsGranted = true;
            this.setActivity(Activity.DYING);
            this.setAnim(this.deathTicks >= DEATH_SPIRIT_TICK ? CrimsonAnim.DEATH_FINAL : CrimsonAnim.DEATH_KNEEL);
            this.bossEvent.setProgress(0.0F);
        }
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    static @Nullable UUID parseUuid(String text) {
        if (text.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException e) {
            return null; // a damaged or hand-edited save: forget it rather than fail to load
        }
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    // ================================================================= sounds
    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.getActivity() == Activity.GROUND ? ModSounds.CRIMSON_AMBIENT.get() : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CRIMSON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRIMSON_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.CRIMSON_STEP.get(), 0.8F, 0.75F + this.random.nextFloat() * 0.1F);
    }
}
