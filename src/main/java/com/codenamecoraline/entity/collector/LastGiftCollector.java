package com.codenamecoraline.entity.collector;

import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModSounds;
import com.codenamecoraline.CodenameCoraline;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The Collector of the Last Gift (O Cobrador da Última Dádiva) — `codenamecoraline:last_gift_collector`.
 *
 * <p>Colossal optional winter-folklore boss (~4.5 blocks tall, ~2.5 blocks visual shoulder/mantle width)
 * featuring a 9.5-second sky-sleigh arrival and leap intro cinematic, a two-phase choreographed combat
 * state machine with four distinct attacks, a 3-second Phase II transition, and a 3.5-second death collapse.</p>
 */
public class LastGiftCollector extends Monster {

    /** High-level boss state machine (Section 10). */
    public enum BossState {
        INTRO_CINEMATIC,
        PHASE_ONE,
        PHASE_TRANSITION,
        PHASE_TWO,
        ATTACK_PREPARATION,
        ATTACK_EXECUTION,
        RECOVERY,
        DEATH_CINEMATIC,
        DEAD
    }

    /** Sub-states of the 9.5-second opening cinematic (Section 8). */
    public enum IntroStage {
        INTRO_PRELUDE,
        INTRO_LIGHTNING,
        INTRO_SLEIGH_PASS,
        INTRO_JUMP,
        INTRO_FALL,
        INTRO_LANDING,
        INTRO_REVEAL,
        INTRO_COMPLETE
    }

    /** Four choreographed combat attacks (Section 11). */
    public enum AttackType {
        NONE,
        WINTER_SCYTHE,
        HOLLOW_BELL,
        PAWN_PROCESSION,
        SOLSTICE_JUMP,
        WHIRLWIND,
        STOMP,
        FROST_CONE,
        HYPNOTIC_GAZE,
        FEY_SUMMON,
        CRESCENT_WAVE,
        COMET_BARRAGE,
        GIFT_RAIN
    }

    // Deterministic entity events broadcast from server to client. Kept at 100+ so none collides with vanilla
    // LivingEntity events (e.g. 60 = death poof, which tickDeath() broadcasts and super.handleEntityEvent handles).
    private static final byte VANILLA_EVENT_DEATH_POOF = 60;
    public static final byte EVENT_INTRO_START = 100;
    public static final byte EVENT_INTRO_LIGHTNING = 101;
    public static final byte EVENT_INTRO_JUMP = 102;
    public static final byte EVENT_INTRO_FALL = 103;
    public static final byte EVENT_INTRO_LAND = 104;
    public static final byte EVENT_INTRO_INTIMIDATE = 105;
    public static final byte EVENT_INTRO_COMPLETE = 106;
    public static final byte EVENT_ATTACK_SCYTHE_PREP = 107;
    public static final byte EVENT_ATTACK_SCYTHE_EXEC = 108;
    public static final byte EVENT_ATTACK_BELL_PREP = 109;
    public static final byte EVENT_ATTACK_BELL_IMPACT = 110;
    public static final byte EVENT_ATTACK_PAWNS = 111;
    public static final byte EVENT_ATTACK_SOLSTICE_JUMP = 112;
    public static final byte EVENT_RECOVERY = 113;
    public static final byte EVENT_PHASE_TRANSITION = 114;
    public static final byte EVENT_HURT = 115;
    public static final byte EVENT_DEATH_CINEMATIC = 116;
    public static final byte EVENT_ATTACK_CAST = 117;
    public static final byte EVENT_ATTACK_SPIN = 118;
    public static final byte EVENT_ATTACK_SLAM = 119;
    public static final byte EVENT_RAGE = 120;

    // Scale and balance constants (Sections 5 & 6).
    public static final float COLLISION_WIDTH = 1.85F;
    public static final float COLLISION_HEIGHT = 4.5F;
    public static final float EYE_HEIGHT = 3.95F;
    public static final double MAX_HEALTH = 420.0D;
    public static final double ARMOR = 10.0D;
    public static final double ARMOR_TOUGHNESS = 4.0D;
    public static final double PHASE_ONE_SPEED = 0.23D;
    public static final double PHASE_TWO_SPEED = 0.27D;
    public static final double STAGE_FIVE_SPEED = 0.31D;
    /** Health fractions at which stages 2..5 begin (Nicholas-style five-stage fight). */
    public static final double[] STAGE_THRESHOLDS = {0.8D, 0.6D, 0.4D, 0.2D, 0.1D};
    public static final int MAX_STAGE = 6;
    public static final float FRENZY_BONUS_DAMAGE = 4.0F;
    /** Stage 6, Rage: every attribute is amplified. */
    public static final float RAGE_DAMAGE_MULTIPLIER = 1.5F;
    public static final double RAGE_SPEED = 0.34D;
    public static final double RAGE_ARMOR_BONUS = 4.0D;
    public static final double KNOCKBACK_RESISTANCE = 0.9D;
    public static final double FOLLOW_RANGE = 40.0D;
    public static final int XP_REWARD = 100;

    // Intro cinematic timing constants (9.5 seconds = 190 ticks total).
    public static final int INTRO_LIGHTNING_TICK = 24;   // 1.2 s
    public static final int INTRO_SLEIGH_TICK = 40;      // 2.0 s
    public static final int INTRO_JUMP_TICK = 70;        // 3.5 s
    public static final int INTRO_FALL_TICK = 90;        // 4.5 s
    public static final int INTRO_LANDING_TICK = 124;    // 6.2 s
    public static final int INTRO_REVEAL_TICK = 146;     // 7.3 s
    public static final int INTRO_TOTAL_TICKS = 190;     // 9.5 s
    public static final double INTRO_SKY_HEIGHT = 22.0D;

    // Phase transition and death timing constants (Sections 10.2 & 14.3).
    public static final int PHASE_TRANSITION_TICKS = 60; // 3.0 s
    public static final int DEATH_CINEMATIC_TICKS = 70;  // 3.5 s

    // Attack 1: Foice do Inverno (Winter Scythe, Section 11.1).
    public static final float SCYTHE_DAMAGE = 11.0F;
    public static final int SCYTHE_PREP_TICKS = 20;      // 1.0 s
    public static final int SCYTHE_EXEC_TICKS_P1 = 10;   // 0.5 s
    public static final int SCYTHE_EXEC_TICKS_P2 = 22;   // includes second sweep in Phase II
    public static final int SCYTHE_RECOVERY_TICKS_P1 = 22; // 1.1 s
    public static final int SCYTHE_RECOVERY_TICKS_P2 = 16; // 0.8 s
    public static final double SCYTHE_START_REACH = 3.8D;
    public static final double SCYTHE_STRIKE_REACH = 4.2D;
    public static final double SCYTHE_ARC_MIN_DOT = 0.15D; // frontal arc only, never 360 deg

    // Attack 2: Badalada Oca (Hollow Toll frost waves, Section 11.2).
    public static final float HOLLOW_BELL_DAMAGE = 7.0F;
    public static final int HOLLOW_BELL_PREP_TICKS = 28; // 1.4 s
    public static final int HOLLOW_BELL_EXEC_TICKS = 20; // 1.0 s wave travel
    public static final int HOLLOW_BELL_RECOVERY_TICKS = 24; // 1.2 s
    public static final int HOLLOW_BELL_COOLDOWN_TICKS = 100; // 5.0 s
    public static final double HOLLOW_BELL_MAX_RANGE = 12.0D;
    public static final double HOLLOW_BELL_WAVE_SPEED = 0.60D;
    public static final double HOLLOW_BELL_RAY_RADIUS = 1.15D;

    // Attack 3: Procissão dos Penhores (Pledge Procession root zones, Section 11.3).
    public static final float PAWN_DAMAGE = 6.0F;
    public static final int PAWN_PREP_TICKS = 20;        // 1.0 s ground warning marks
    public static final int PAWN_EXEC_TICKS = 10;        // root eruption
    public static final int PAWN_RECOVERY_TICKS = 20;    // 1.0 s
    public static final int PAWN_COOLDOWN_TICKS = 110;   // 5.5 s
    public static final double PAWN_ZONE_RADIUS = 1.65D;

    // Attack 4: Salto do Solstício (Solstice Leap, Phase II exclusive, Section 11.4).
    public static final float SOLSTICE_DIRECT_DAMAGE = 12.0F;
    public static final float SOLSTICE_WAVE_DAMAGE = 6.0F;
    public static final int SOLSTICE_PREP_TICKS = 26;    // 1.3 s
    public static final int SOLSTICE_EXEC_TICKS = 20;    // 1.0 s controlled leap
    public static final int SOLSTICE_RECOVERY_TICKS = 30;// 1.5 s vulnerable recovery
    public static final int SOLSTICE_COOLDOWN_TICKS = 140; // 7.0 s
    public static final double SOLSTICE_MAX_LEAP_DIST = 14.0D;
    public static final double SOLSTICE_IMPACT_RADIUS = 3.2D;
    public static final double SOLSTICE_WAVE_RADIUS = 5.4D;

    // Attack 5: Giro do Machado (Axe Whirlwind, stage 2+).
    public static final float WHIRL_DAMAGE = 6.0F;
    public static final int WHIRL_PREP_TICKS = 24;
    public static final int WHIRL_EXEC_TICKS = 50;
    public static final int WHIRL_RECOVERY_TICKS = 34;
    public static final int WHIRL_COOLDOWN_TICKS = 150;
    public static final double WHIRL_RADIUS = 5.6D;

    // Attack 6: Pisada do Renegado (Stomp shockwave, every stage).
    public static final float STOMP_DAMAGE = 9.0F;
    public static final int STOMP_PREP_TICKS = 18;
    public static final int STOMP_EXEC_TICKS = 14;
    public static final int STOMP_RECOVERY_TICKS = 18;
    public static final int STOMP_COOLDOWN_TICKS = 70;
    public static final double STOMP_RADIUS = 5.5D;

    // Attack 7: Sopro do Inverno (Chilly frost cone, stage 4+).
    public static final float CONE_DAMAGE = 8.0F;
    public static final int CONE_PREP_TICKS = 30;
    public static final int CONE_EXEC_TICKS = 22;
    public static final int CONE_RECOVERY_TICKS = 26;
    public static final int CONE_COOLDOWN_TICKS = 130;
    public static final double CONE_LENGTH = 9.5D;
    public static final double CONE_MIN_DOT = 0.80D;

    // Attack 8: Olhar Hipnótico (Watchful gaze, stage 2+): only hits players who look at the boss.
    public static final float GAZE_DAMAGE = 5.0F;
    public static final int GAZE_PREP_TICKS = 36;
    public static final int GAZE_EXEC_TICKS = 6;
    public static final int GAZE_RECOVERY_TICKS = 30;
    public static final int GAZE_COOLDOWN_TICKS = 200;
    public static final double GAZE_RANGE = 22.0D;
    public static final double GAZE_FACING_DOT = 0.35D;

    // Attack 9: Convocação Feérica (Fey Summons, stage 3+): calls redcaps to his side.
    public static final int SUMMON_PREP_TICKS = 30;
    public static final int SUMMON_EXEC_TICKS = 10;
    public static final int SUMMON_RECOVERY_TICKS = 40;
    public static final int SUMMON_COOLDOWN_TICKS = 600;
    public static final int MAX_MINIONS = 4;

    // Attack 10: Corte Crescente (axe swing that throws a crescent of frost along the ground).
    public static final float CRESCENT_DAMAGE = 9.0F;
    public static final int CRESCENT_PREP_TICKS = 22;
    public static final int CRESCENT_EXEC_TICKS = 12;
    public static final int CRESCENT_RECOVERY_TICKS = 20;
    public static final int CRESCENT_COOLDOWN_TICKS = 90;
    public static final double CRESCENT_SPEED = 0.95D;
    public static final int CRESCENT_MAX_AGE = 28;
    public static final double CRESCENT_RADIUS = 1.9D;

    // Attack 11: Cometas do Solstício (homing frost comets with long trails).
    public static final float COMET_DAMAGE = 8.0F;
    public static final int COMET_PREP_TICKS = 30;
    public static final int COMET_EXEC_TICKS = 30;
    public static final int COMET_RECOVERY_TICKS = 28;
    public static final int COMET_COOLDOWN_TICKS = 130;
    public static final double COMET_SPEED = 0.85D;
    public static final int COMET_HOMING_TICKS = 45;
    public static final double COMET_RADIUS = 1.2D;

    // Attack 12: Chuva de Presentes (marked ground, then wrapped presents fall and burst).
    public static final float RAIN_DAMAGE = 10.0F;
    public static final int RAIN_PREP_TICKS = 34;
    public static final int RAIN_EXEC_TICKS = 44;
    public static final int RAIN_RECOVERY_TICKS = 30;
    public static final int RAIN_COOLDOWN_TICKS = 220;
    public static final double RAIN_RADIUS = 2.8D;
    public static final int RAIN_WARNING_TICKS = 22;

    public static final DustParticleOptions COMET_DUST = new DustParticleOptions(0x9FE3FF, 1.7F);
    public static final DustParticleOptions CRESCENT_DUST = new DustParticleOptions(0xD8F4FF, 1.5F);
    public static final DustParticleOptions PRESENT_RED = new DustParticleOptions(0xC7283A, 1.5F);
    public static final DustParticleOptions PRESENT_GOLD = new DustParticleOptions(0xE6B422, 1.5F);
    public static final DustParticleOptions PRESENT_GREEN = new DustParticleOptions(0x2E8B57, 1.5F);
    public static final DustParticleOptions RAGE_DUST = new DustParticleOptions(0xFF2A2A, 1.6F);

    // Reactive "legendary" stomp when a player hugs him during recovery, and the restless dash.
    public static final float LEGEND_DAMAGE = 6.0F;
    public static final int LEGEND_COOLDOWN_TICKS = 80;
    public static final double LEGEND_RADIUS = 4.2D;
    public static final int DASH_COOLDOWN_TICKS = 70;

    public static final DustParticleOptions SPECTRAL_FROST_DUST =
            new DustParticleOptions(0xA9DCEB, 1.35F);
    public static final DustParticleOptions WINE_PLEDGE_DUST =
            new DustParticleOptions(0x722D3B, 1.45F);

    // Synched entity data for deterministic client rendering & camera choreography.
    private static final EntityDataAccessor<Integer> DATA_BOSS_STATE =
            SynchedEntityData.defineId(LastGiftCollector.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_INTRO_STAGE =
            SynchedEntityData.defineId(LastGiftCollector.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_INTRO_TICKS =
            SynchedEntityData.defineId(LastGiftCollector.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ATTACK_TYPE =
            SynchedEntityData.defineId(LastGiftCollector.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STAGE =
            SynchedEntityData.defineId(LastGiftCollector.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_PHASE_TWO =
            SynchedEntityData.defineId(LastGiftCollector.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CAGE_OPEN =
            SynchedEntityData.defineId(LastGiftCollector.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_EYES_GLOWING =
            SynchedEntityData.defineId(LastGiftCollector.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<BlockPos> DATA_ARENA_CENTER =
            SynchedEntityData.defineId(LastGiftCollector.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Integer> DATA_SLEIGH_ID =
            SynchedEntityData.defineId(LastGiftCollector.class, EntityDataSerializers.INT);

    // Client AnimationStates for all required animations (Section 12).
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState idleAlternateAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState appearAnimationState = new AnimationState();
    public final AnimationState jumpFromSleighAnimationState = new AnimationState();
    public final AnimationState fallAnimationState = new AnimationState();
    public final AnimationState landAnimationState = new AnimationState();
    public final AnimationState intimidateAnimationState = new AnimationState();
    public final AnimationState attackScythePrepAnimationState = new AnimationState();
    public final AnimationState attackScytheExecAnimationState = new AnimationState();
    public final AnimationState attackHollowBellPrepAnimationState = new AnimationState();
    public final AnimationState attackHollowBellImpactAnimationState = new AnimationState();
    public final AnimationState attackPawnsAnimationState = new AnimationState();
    public final AnimationState attackSolsticeJumpAnimationState = new AnimationState();
    public final AnimationState attackCastAnimationState = new AnimationState();
    public final AnimationState attackSpinAnimationState = new AnimationState();
    public final AnimationState attackSlamAnimationState = new AnimationState();
    public final AnimationState rageAnimationState = new AnimationState();
    public final AnimationState recoveryAnimationState = new AnimationState();
    public final AnimationState phaseTransitionAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            UUID.randomUUID(),
            Component.translatable("entity.codenamecoraline.last_gift_collector"),
            BossEvent.BossBarColor.WHITE,
            BossEvent.BossBarOverlay.NOTCHED_10);

    private BlockPos arenaCenter = BlockPos.ZERO;
    private int introTicks = 0;
    private boolean landedOnce = false;
    private int landingCount = 0;
    private final Set<UUID> minionIds = new HashSet<>();
    private final List<Bolt> bolts = new ArrayList<>();
    private final List<RainSpot> rainSpots = new ArrayList<>();
    private Vec3 targetVelocity = Vec3.ZERO;
    private Vec3 targetLastPos = Vec3.ZERO;
    private int farTicks = 0;
    private int strafeTicks = 0;
    private int strafeDir = 1;
    private int stuckTicks = 0;
    private Vec3 stuckAnchor = Vec3.ZERO;
    private int blinkCooldown = 0;
    private int unreachableTicks = 0;
    private int hopCooldown = 0;
    private @Nullable DamageSource deathSource;
    private boolean giftSpawned = false;
    private int pendingStage = 1;
    private AttackType lastAttack = AttackType.NONE;
    private boolean chainNext = false;
    private int prepJitter = 0;
    private int dashCooldown = 0;
    private int legendCooldown = 0;
    private final int[] cooldowns = new int[AttackType.values().length];
    private int phaseTransitionCount = 0;
    private int phaseTransitionTicks = 0;
    private int actionTimer = 0;
    private int recoveryTicks = 0;
    private int deathCinematicTicks = 0;
    private boolean rewardsGranted = false;
    private int deathAwardCount = 0;

    private int scytheCooldown = 0;
    private int hollowBellCooldown = 0;
    private int pawnCooldown = 0;
    private int solsticeJumpCooldown = 0;
    private int pathRecalcTicks = 0;
    private int idleAlternateCooldown = 100;

    private Vec3 introJumpStartPos = Vec3.ZERO;
    private Vec3 solsticeStartPos = Vec3.ZERO;
    private Vec3 solsticeTargetPos = Vec3.ZERO;
    private final List<Vec3> activeFrostWaveDirs = new ArrayList<>();
    private Vec3 frostWaveOrigin = Vec3.ZERO;
    private final Set<UUID> frostWaveHitEntities = new HashSet<>();
    private final List<Vec3> activePawnZones = new ArrayList<>();

    public LastGiftCollector(EntityType<? extends LastGiftCollector> type, Level level) {
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
                .add(Attributes.MOVEMENT_SPEED, PHASE_ONE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, SCYTHE_DAMAGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
                .add(Attributes.STEP_HEIGHT, 1.2D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BOSS_STATE, BossState.PHASE_ONE.ordinal());
        builder.define(DATA_INTRO_STAGE, IntroStage.INTRO_COMPLETE.ordinal());
        builder.define(DATA_INTRO_TICKS, INTRO_TOTAL_TICKS);
        builder.define(DATA_ATTACK_TYPE, AttackType.NONE.ordinal());
        builder.define(DATA_STAGE, 1);
        builder.define(DATA_PHASE_TWO, false);
        builder.define(DATA_CAGE_OPEN, false);
        builder.define(DATA_EYES_GLOWING, true);
        builder.define(DATA_ARENA_CENTER, BlockPos.ZERO);
        builder.define(DATA_SLEIGH_ID, -1);
    }

    public BossState getBossState() {
        int idx = Mth.clamp(this.entityData.get(DATA_BOSS_STATE), 0, BossState.values().length - 1);
        return BossState.values()[idx];
    }

    public void setBossState(BossState state) {
        this.entityData.set(DATA_BOSS_STATE, state.ordinal());
    }

    public IntroStage getIntroStage() {
        int idx = Mth.clamp(this.entityData.get(DATA_INTRO_STAGE), 0, IntroStage.values().length - 1);
        return IntroStage.values()[idx];
    }

    private void setIntroStage(IntroStage stage) {
        this.entityData.set(DATA_INTRO_STAGE, stage.ordinal());
    }

    public int getIntroTicks() {
        return this.entityData.get(DATA_INTRO_TICKS);
    }

    public AttackType getActiveAttack() {
        int idx = Mth.clamp(this.entityData.get(DATA_ATTACK_TYPE), 0, AttackType.values().length - 1);
        return AttackType.values()[idx];
    }

    private void setActiveAttack(AttackType type) {
        this.entityData.set(DATA_ATTACK_TYPE, type.ordinal());
    }

    /** Current fight stage, 1 (full health) to 5 (frenzy, below 20% health). */
    public int getStage() {
        return Mth.clamp(this.entityData.get(DATA_STAGE), 1, MAX_STAGE);
    }

    private void setStage(int stage) {
        this.entityData.set(DATA_STAGE, Mth.clamp(stage, 1, MAX_STAGE));
    }

    private int stageForHealth() {
        double ratio = this.getHealth() / this.getMaxHealth();
        int stage = 1;
        for (double threshold : STAGE_THRESHOLDS) {
            if (ratio <= threshold) {
                stage++;
            }
        }
        return stage;
    }

    public boolean isPhaseTwo() {
        return this.entityData.get(DATA_PHASE_TWO);
    }

    private void setPhaseTwo(boolean phaseTwo) {
        this.entityData.set(DATA_PHASE_TWO, phaseTwo);
    }

    public boolean isCageOpen() {
        return this.entityData.get(DATA_CAGE_OPEN);
    }

    public void setCageOpen(boolean open) {
        this.entityData.set(DATA_CAGE_OPEN, open);
    }

    public boolean areEyesGlowing() {
        return this.entityData.get(DATA_EYES_GLOWING);
    }

    public void setEyesGlowing(boolean glowing) {
        this.entityData.set(DATA_EYES_GLOWING, glowing);
    }

    public BlockPos getArenaCenter() {
        return this.entityData.get(DATA_ARENA_CENTER);
    }

    public Vec3 getAnchorPosition() {
        BlockPos c = this.getArenaCenter();
        return new Vec3(c.getX() + 0.5D, c.getY(), c.getZ() + 0.5D);
    }

    public int getStateTicks() {
        return switch (this.getBossState()) {
            case INTRO_CINEMATIC -> this.getIntroTicks();
            case PHASE_TRANSITION -> this.phaseTransitionTicks;
            case DEATH_CINEMATIC -> this.deathCinematicTicks;
            case RECOVERY -> this.recoveryTicks;
            default -> this.actionTimer;
        };
    }

    public void setArenaCenter(BlockPos pos) {
        this.arenaCenter = pos.immutable();
        this.entityData.set(DATA_ARENA_CENTER, this.arenaCenter);
    }

    public int getSleighEntityId() {
        return this.entityData.get(DATA_SLEIGH_ID);
    }

    public int getLandingCount() {
        return this.landingCount;
    }

    public int getPhaseTransitionCount() {
        return this.phaseTransitionCount;
    }

    public int getDeathAwardCount() {
        return this.deathAwardCount;
    }

    public List<Vec3> getActivePawnZones() {
        return List.copyOf(this.activePawnZones);
    }

    public List<Vec3> getActiveFrostWaveDirs() {
        return List.copyOf(this.activeFrostWaveDirs);
    }

    public ServerBossEvent getBossEvent() {
        return this.bossEvent;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        // Optional boss never despawns due to player distance (Section 10).
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CollectorCombatBrainGoal());
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (this.getBossState() == BossState.INTRO_CINEMATIC
                || this.getBossState() == BossState.PHASE_TRANSITION
                || this.getBossState() == BossState.DEATH_CINEMATIC
                || this.getBossState() == BossState.DEAD) {
            return false;
        }
        return !(target instanceof LastGiftCollector) && !(target instanceof GiftMimic mimic && !mimic.isAllied())
                && super.canAttack(target);
    }

    /** The Renegade is a creature of the winter: powder snow neither freezes nor swallows him. */
    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
                                                  EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setArenaCenter(this.blockPosition());
        // Spawn Egg or /summon starts ready in PHASE_ONE without duplicating cinematic instances;
        // Silent Bell ritual explicitly invokes startIntroCinematic(...).
        if (this.getBossState() != BossState.INTRO_CINEMATIC) {
            this.setBossState(BossState.PHASE_ONE);
            this.setIntroStage(IntroStage.INTRO_COMPLETE);
            this.entityData.set(DATA_INTRO_TICKS, INTRO_TOTAL_TICKS);
            this.setEyesGlowing(true);
            this.bossEvent.setVisible(true);
        }
        return data;
    }

    /**
     * Starts the full 9.5-second sky-sleigh arrival and leap intro cinematic (Section 8).
     */
    public void startIntroCinematic(ServerLevel level, BlockPos landingBlockPos, @Nullable Player summoner) {
        this.setArenaCenter(landingBlockPos);
        this.introTicks = 0;
        this.landedOnce = false;
        this.landingCount = 0;
        this.setBossState(BossState.INTRO_CINEMATIC);
        this.setIntroStage(IntroStage.INTRO_PRELUDE);
        this.entityData.set(DATA_INTRO_TICKS, 0);
        this.setActiveAttack(AttackType.NONE);
        this.setEyesGlowing(false);
        this.setCageOpen(false);
        this.bossEvent.setVisible(false);
        this.setNoGravity(true);
        this.getNavigation().stop();

        Vec3 skyStart = new Vec3(
                landingBlockPos.getX() + 0.5D - 18.0D,
                landingBlockPos.getY() + INTRO_SKY_HEIGHT,
                landingBlockPos.getZ() + 0.5D - 8.0D);
        this.setPos(skyStart.x, skyStart.y, skyStart.z);
        if (summoner != null) {
            this.setTarget(summoner);
        }

        level.broadcastEntityEvent(this, EVENT_INTRO_START);
        this.playSound(ModSounds.COLLECTOR_SLEIGH_BELLS.get(), 1.8F, 0.72F);
    }

    /**
     * Safely interrupts or finishes the intro cinematic, placing the boss at the arena landing spot,
     * cleaning up any temporary sleigh actor, and unlocking PHASE_ONE without duplicating or removing the boss.
     */
    public void interruptIntroToCombat() {
        if (this.getBossState() != BossState.INTRO_CINEMATIC) {
            return;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            this.cleanupSleigh(serverLevel);
        }
        Vec3 landPos = new Vec3(this.arenaCenter.getX() + 0.5D, this.arenaCenter.getY(), this.arenaCenter.getZ() + 0.5D);
        this.setPos(landPos.x, landPos.y, landPos.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(false);
        if (!this.landedOnce) {
            this.landedOnce = true;
            this.landingCount++;
        }
        this.introTicks = INTRO_TOTAL_TICKS;
        this.entityData.set(DATA_INTRO_TICKS, INTRO_TOTAL_TICKS);
        this.setIntroStage(IntroStage.INTRO_COMPLETE);
        this.setBossState(BossState.PHASE_ONE);
        this.setEyesGlowing(true);
        this.bossEvent.setVisible(true);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.broadcastEntityEvent(this, EVENT_INTRO_COMPLETE);
        }
    }

    private void cleanupSleigh(ServerLevel level) {
        int sleighId = this.getSleighEntityId();
        if (sleighId != -1) {
            Entity entity = level.getEntity(sleighId);
            if (entity instanceof CollectorSleigh sleigh) {
                sleigh.discard();
            }
            this.entityData.set(DATA_SLEIGH_ID, -1);
        }
        AABB search = new AABB(this.arenaCenter).inflate(64.0D);
        for (CollectorSleigh stray : level.getEntitiesOfClass(CollectorSleigh.class, search)) {
            stray.discard();
        }
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
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        BossState current = this.getBossState();
        if (current == BossState.INTRO_CINEMATIC || current == BossState.PHASE_TRANSITION) {
            if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                return false;
            }
        }
        if (current == BossState.DEATH_CINEMATIC || current == BossState.DEAD) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive()) {
            level.broadcastEntityEvent(this, EVENT_HURT);
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            // Each stage (80/60/40/20% health) is entered once, in order, and never repeats.
            if (this.stageForHealth() > this.getStage()) {
                this.triggerPhaseTransition(level);
            }
        }
        return hurt;
    }

    /** Initiates the 3-second transition ("Noite sem Estrelas") into the next stage. */
    public void triggerPhaseTransition(ServerLevel level) {
        if (this.getStage() >= MAX_STAGE || !this.isAlive()
                || this.getBossState() == BossState.DEATH_CINEMATIC
                || this.getBossState() == BossState.DEAD) {
            return;
        }
        this.pendingStage = Math.min(MAX_STAGE, Math.max(this.getStage() + 1, this.stageForHealth()));
        this.phaseTransitionCount++;
        this.phaseTransitionTicks = 0;
        this.cancelCombatAction();
        this.setBossState(BossState.PHASE_TRANSITION);
        this.setCageOpen(true);
        this.setEyesGlowing(true);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(false);

        level.broadcastEntityEvent(this, this.pendingStage >= MAX_STAGE ? EVENT_RAGE : EVENT_PHASE_TRANSITION);
        this.playSound(ModSounds.COLLECTOR_PHASE_TRANSITION.get(), 1.6F, 0.72F);
        level.sendParticles(SPECTRAL_FROST_DUST,
                this.getX(), this.getY() + 2.2D, this.getZ(), 28, 1.4D, 1.2D, 1.4D, 0.04D);
    }

    @Override
    public void die(DamageSource source) {
        if (this.rewardsGranted) {
            return;
        }
        this.rewardsGranted = true;
        this.deathSource = source;
        this.bolts.clear();
        this.rainSpots.clear();
        this.deathAwardCount++;
        this.cancelCombatAction();
        if (this.level() instanceof ServerLevel serverLevel) {
            this.cleanupSleigh(serverLevel);
            this.discardMinions(serverLevel);
            serverLevel.broadcastEntityEvent(this, EVENT_DEATH_CINEMATIC);
            BlockParticleOption iceDebris = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState());
            serverLevel.sendParticles(iceDebris, this.getX(), this.getY() + 1.8D, this.getZ(),
                    24, 0.8D, 1.1D, 0.8D, 0.08D);
        }
        this.setBossState(BossState.DEATH_CINEMATIC);
        this.setEyesGlowing(false);
        this.setCageOpen(true);
        this.deathCinematicTicks = 0;
        this.bossEvent.setProgress(0.0F);
        this.bossEvent.setVisible(false);
        this.getNavigation().stop();
        this.setNoGravity(false);
        super.die(source);
    }

    @Override
    protected void tickDeath() {
        this.deathCinematicTicks++;
        if (this.level() instanceof ServerLevel serverLevel) {
            if (this.deathCinematicTicks % 8 == 0) {
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                        this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.9D, 0.5D, 0.9D, 0.02D);
            }
            if (this.deathCinematicTicks > 20) {
                // Ribbons of red, gold and green spiral up around the fallen Renegade as he is wrapped as a present.
                double t = this.deathCinematicTicks * 0.45D;
                for (int i = 0; i < 3; i++) {
                    double a = t + i * Math.PI * 2.0D / 3.0D;
                    double rise = (this.deathCinematicTicks % 24) / 24.0D * 3.6D;
                    serverLevel.sendParticles(i == 0 ? PRESENT_RED : i == 1 ? PRESENT_GOLD : PRESENT_GREEN,
                            this.getX() + Math.cos(a) * 1.3D, this.getY() + rise, this.getZ() + Math.sin(a) * 1.3D,
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }
        if (this.deathCinematicTicks >= DEATH_CINEMATIC_TICKS && !this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                this.spawnLastGift(serverLevel);
            }
            this.setBossState(BossState.DEAD);
            this.bossEvent.removeAllPlayers();
            this.level().broadcastEntityEvent(this, VANILLA_EVENT_DEATH_POOF);
            this.remove(RemovalReason.KILLED);
        }
    }

    private static final ResourceKey<LootTable> LOOT_TABLE_KEY = ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "entities/last_gift_collector"));

    /** His loot is no longer scattered: it waits in a chest inside a big Christmas gift that pops open where he fell. */
    @Override
    protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled) {
        // Intentionally empty: spawnLastGift() rolls the same table into the chest.
    }

    public int getGiftChestCount() {
        return this.giftSpawned ? 1 : 0;
    }

    /** Places the loot chest (with the table's drops, always including the greataxe) and the wrapping gift on top. */
    public void spawnLastGift(ServerLevel level) {
        if (this.giftSpawned) {
            return;
        }
        this.giftSpawned = true;
        LootTable table = level.getServer().reloadableRegistries().getLootTable(LOOT_TABLE_KEY);
        DamageSource source = this.deathSource != null ? this.deathSource : this.damageSources().generic();
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .withParameter(LootContextParams.ORIGIN, this.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .create(LootContextParamSets.ENTITY);
        List<ItemStack> loot = table.getRandomItems(params);

        BlockPos chestPos = this.findChestSpot(level);
        if (chestPos == null) {
            for (ItemStack stack : loot) {
                level.addFreshEntity(new ItemEntity(level, this.getX(), this.getY() + 0.5D, this.getZ(), stack));
            }
            return;
        }
        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(this.random);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 3);
        BlockEntity entity = level.getBlockEntity(chestPos);
        if (entity instanceof ChestBlockEntity chest) {
            for (int i = 0; i < loot.size() && i < chest.getContainerSize(); i++) {
                chest.setItem(i, loot.get(i));
            }
            chest.setChanged();
        }
        net.minecraft.world.entity.Entity gift = ModEntities.COLLECTOR_GIFT.get().create(level, EntitySpawnReason.TRIGGERED);
        if (gift != null) {
            gift.setPos(chestPos.getX() + 0.5D, chestPos.getY(), chestPos.getZ() + 0.5D);
            level.addFreshEntity(gift);
        }
    }

    private @Nullable BlockPos findChestSpot(ServerLevel level) {
        BlockPos origin = this.blockPosition();
        for (int radius = 0; radius <= 3; radius++) {
            for (int dy = 1; dy >= -2; dy--) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                            continue;
                        }
                        BlockPos pos = origin.offset(dx, dy, dz);
                        if (level.getBlockState(pos).canBeReplaced()
                                && level.getFluidState(pos).isEmpty()
                                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
                            return pos;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level() instanceof ServerLevel serverLevel) {
            this.cleanupSleigh(serverLevel);
        }
        this.bossEvent.removeAllPlayers();
        super.remove(reason);
    }

    public void cancelCombatAction() {
        this.setActiveAttack(AttackType.NONE);
        this.actionTimer = 0;
        this.recoveryTicks = 0;
        this.activeFrostWaveDirs.clear();
        this.frostWaveHitEntities.clear();
        this.activePawnZones.clear();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!this.isAlive() || this.isDeadOrDying()) {
            return;
        }
        if (this.arenaCenter.equals(BlockPos.ZERO)) {
            this.setArenaCenter(this.blockPosition());
        }
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (this.scytheCooldown > 0) this.scytheCooldown--;
        if (this.hollowBellCooldown > 0) this.hollowBellCooldown--;
        if (this.pawnCooldown > 0) this.pawnCooldown--;
        if (this.solsticeJumpCooldown > 0) this.solsticeJumpCooldown--;
        if (this.pathRecalcTicks > 0) this.pathRecalcTicks--;
        int rate = this.getStage() >= MAX_STAGE ? 2 : 1; // Rage: everything comes back twice as fast
        this.dashCooldown = Math.max(0, this.dashCooldown - rate);
        this.legendCooldown = Math.max(0, this.legendCooldown - rate);
        this.blinkCooldown = Math.max(0, this.blinkCooldown - 1);
        this.hopCooldown = Math.max(0, this.hopCooldown - 1);
        this.scytheCooldown = Math.max(0, this.scytheCooldown - (rate - 1));
        this.hollowBellCooldown = Math.max(0, this.hollowBellCooldown - (rate - 1));
        this.pawnCooldown = Math.max(0, this.pawnCooldown - (rate - 1));
        this.solsticeJumpCooldown = Math.max(0, this.solsticeJumpCooldown - (rate - 1));
        for (int i = 0; i < this.cooldowns.length; i++) {
            this.cooldowns[i] = Math.max(0, this.cooldowns[i] - rate);
        }
        this.tickBolts(level);
        if (this.getStage() >= MAX_STAGE && this.tickCount % 3 == 0 && this.getBossState() != BossState.DEATH_CINEMATIC) {
            level.sendParticles(RAGE_DUST, this.getX(), this.getY() + 2.2D, this.getZ(), 6, 0.9D, 1.6D, 0.9D, 0.02D);
            level.sendParticles(ParticleTypes.SMALL_FLAME, this.getX(), this.getY() + 0.3D, this.getZ(), 2, 0.7D, 0.1D, 0.7D, 0.01D);
        }
        // Never stay buried: leaps, landings or snow-covered ground must not leave him stuck inside blocks.
        if (this.tickCount % 5 == 0 && this.getBossState() != BossState.INTRO_CINEMATIC && !level.noCollision(this)) {
            for (int i = 0; i < 12 && !level.noCollision(this); i++) {
                this.setPos(this.getX(), this.getY() + 0.25D, this.getZ());
            }
        }

        BossState current = this.getBossState();
        if (current == BossState.INTRO_CINEMATIC) {
            this.tickIntroCinematic(level);
        } else if (current == BossState.PHASE_TRANSITION) {
            this.tickPhaseTransition(level);
        }
    }

    /**
     * Drives the 6-phase opening cinematic on the server (Section 8).
     * Never deals damage, never modifies terrain/weather/time, and lands deterministically once.
     */
    private void tickIntroCinematic(ServerLevel level) {
        this.getNavigation().stop();
        this.introTicks++;
        this.entityData.set(DATA_INTRO_TICKS, this.introTicks);

        Vec3 landingPos = new Vec3(this.arenaCenter.getX() + 0.5D, this.arenaCenter.getY(), this.arenaCenter.getZ() + 0.5D);
        Vec3 sleighOrigin = new Vec3(landingPos.x - 18.0D, landingPos.y + INTRO_SKY_HEIGHT, landingPos.z - 8.0D);
        Vec3 sleighVel = new Vec3(0.40D, 0.0D, 0.17D);

        if (this.introTicks < INTRO_LIGHTNING_TICK) {
            // Phase 0: Prelude (0.0 - 1.2s)
            this.setIntroStage(IntroStage.INTRO_PRELUDE);
            this.setPos(sleighOrigin.x, sleighOrigin.y, sleighOrigin.z);
            if (this.introTicks == 12) {
                this.playSound(ModSounds.COLLECTOR_SLEIGH_BELLS.get(), 1.5F, 0.80F);
            }
        } else if (this.introTicks == INTRO_LIGHTNING_TICK) {
            // Phase 1: The Lightning Flash (1.2s) — cosmetic flash & sleigh spawn
            this.setIntroStage(IntroStage.INTRO_LIGHTNING);
            level.broadcastEntityEvent(this, EVENT_INTRO_LIGHTNING);
            this.playSound(ModSounds.COLLECTOR_LIGHTNING.get(), 2.2F, 0.85F);
            level.sendParticles(SPECTRAL_FROST_DUST, landingPos.x, landingPos.y + INTRO_SKY_HEIGHT + 2.0D, landingPos.z,
                    40, 3.5D, 1.5D, 3.5D, 0.05D);
            level.sendParticles(ParticleTypes.END_ROD, sleighOrigin.x, sleighOrigin.y + 1.0D, sleighOrigin.z,
                    25, 2.5D, 1.0D, 2.5D, 0.08D);

            CollectorSleigh sleigh = new CollectorSleigh(level, this, sleighOrigin, sleighVel, -67.0F);
            level.addFreshEntity(sleigh);
            this.entityData.set(DATA_SLEIGH_ID, sleigh.getId());
            this.setPos(sleighOrigin.x, sleighOrigin.y + 0.6D, sleighOrigin.z);
        } else if (this.introTicks < INTRO_SLEIGH_TICK) {
            // Phase 1 continued (1.2 - 2.0s)
            this.setIntroStage(IntroStage.INTRO_LIGHTNING);
            int flightTick = this.introTicks - INTRO_LIGHTNING_TICK;
            Vec3 ridePos = sleighOrigin.add(sleighVel.scale(flightTick)).add(0.0D, 0.6D, 0.0D);
            this.setPos(ridePos.x, ridePos.y, ridePos.z);
        } else if (this.introTicks < INTRO_JUMP_TICK) {
            // Phase 2: Sleigh sky pass (2.0 - 3.5s)
            if (this.getIntroStage() != IntroStage.INTRO_SLEIGH_PASS) {
                this.setIntroStage(IntroStage.INTRO_SLEIGH_PASS);
            }
            int flightTick = this.introTicks - INTRO_LIGHTNING_TICK;
            Vec3 ridePos = sleighOrigin.add(sleighVel.scale(flightTick)).add(0.0D, 0.6D, 0.0D);
            this.setPos(ridePos.x, ridePos.y, ridePos.z);
        } else if (this.introTicks == INTRO_JUMP_TICK) {
            // Phase 3: The Leap from the Sleigh (3.5s)
            this.setIntroStage(IntroStage.INTRO_JUMP);
            int flightTick = this.introTicks - INTRO_LIGHTNING_TICK;
            this.introJumpStartPos = sleighOrigin.add(sleighVel.scale(flightTick)).add(0.0D, 0.6D, 0.0D);
            this.setPos(this.introJumpStartPos.x, this.introJumpStartPos.y, this.introJumpStartPos.z);
            level.broadcastEntityEvent(this, EVENT_INTRO_JUMP);
            this.playSound(ModSounds.COLLECTOR_SLEIGH_BELLS.get(), 1.8F, 0.65F);

            Entity sleighEntity = level.getEntity(this.getSleighEntityId());
            if (sleighEntity instanceof CollectorSleigh sleigh) {
                sleigh.setRiderAboard(false);
            }
        } else if (this.introTicks < INTRO_FALL_TICK) {
            // Phase 3 continued: upward/forward separation arc from the sleigh (3.5 - 4.5s)
            float t = (this.introTicks - INTRO_JUMP_TICK) / (float) (INTRO_FALL_TICK - INTRO_JUMP_TICK);
            double x = Mth.lerp(t * 0.35F, this.introJumpStartPos.x, landingPos.x);
            double z = Mth.lerp(t * 0.35F, this.introJumpStartPos.z, landingPos.z);
            double arcBoost = Math.sin(t * Math.PI) * 2.2D;
            double y = this.introJumpStartPos.y + arcBoost;
            this.setPos(x, y, z);
            level.sendParticles(ParticleTypes.SNOWFLAKE, x, y + 1.5D, z, 3, 0.5D, 0.5D, 0.5D, 0.02D);
        } else if (this.introTicks < INTRO_LANDING_TICK) {
            // Phase 4a: Continuous accelerated descent (4.5 - 6.2s)
            if (this.getIntroStage() != IntroStage.INTRO_FALL) {
                this.setIntroStage(IntroStage.INTRO_FALL);
                level.broadcastEntityEvent(this, EVENT_INTRO_FALL);
            }
            float t = (this.introTicks - INTRO_FALL_TICK) / (float) (INTRO_LANDING_TICK - INTRO_FALL_TICK);
            float accelT = t * t; // quadratic gravity acceleration
            Vec3 fallStart = new Vec3(
                    Mth.lerp(0.35D, this.introJumpStartPos.x, landingPos.x),
                    this.introJumpStartPos.y,
                    Mth.lerp(0.35D, this.introJumpStartPos.z, landingPos.z));
            double x = Mth.lerp(t, fallStart.x, landingPos.x);
            double z = Mth.lerp(t, fallStart.z, landingPos.z);
            double y = Mth.lerp(accelT, fallStart.y, landingPos.y);
            this.setPos(x, y, z);
            level.sendParticles(ParticleTypes.SNOWFLAKE, x, y + 2.0D, z, 4, 0.6D, 0.6D, 0.6D, 0.03D);
        } else if (this.introTicks == INTRO_LANDING_TICK) {
            // Phase 4b: Touchdown & Impact (6.2s) — guaranteed to occur once, purely visual shockwave
            this.setIntroStage(IntroStage.INTRO_LANDING);
            this.setNoGravity(false);
            this.setPos(landingPos.x, landingPos.y, landingPos.z);
            this.setDeltaMovement(Vec3.ZERO);
            if (!this.landedOnce) {
                this.landedOnce = true;
                this.landingCount++;
                level.broadcastEntityEvent(this, EVENT_INTRO_LAND);
                this.playSound(ModSounds.COLLECTOR_SCYTHE_SLASH.get(), 1.8F, 0.60F);
                this.playSound(ModSounds.COLLECTOR_FROST_SHATTER.get(), 1.5F, 0.65F);

                BlockParticleOption snowBurst = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.defaultBlockState());
                BlockParticleOption iceBurst = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState());
                level.sendParticles(snowBurst, landingPos.x, landingPos.y + 0.2D, landingPos.z,
                        36, 2.2D, 0.35D, 2.2D, 0.12D);
                level.sendParticles(iceBurst, landingPos.x, landingPos.y + 0.2D, landingPos.z,
                        24, 1.8D, 0.25D, 1.8D, 0.10D);
                level.sendParticles(ParticleTypes.CLOUD, landingPos.x, landingPos.y + 0.3D, landingPos.z,
                        20, 2.4D, 0.2D, 2.4D, 0.05D);
            }
        } else if (this.introTicks < INTRO_REVEAL_TICK) {
            // Phase 4b continued: crouched impact absorption (6.2 - 7.3s)
            this.setPos(landingPos.x, landingPos.y, landingPos.z);
        } else if (this.introTicks == INTRO_REVEAL_TICK) {
            // Phase 5: Presentation & Reveal (7.3 - 9.5s) — raises head, bell tolls, eyes ignite
            this.setIntroStage(IntroStage.INTRO_REVEAL);
            this.setEyesGlowing(true);
            level.broadcastEntityEvent(this, EVENT_INTRO_INTIMIDATE);
            this.playSound(ModSounds.COLLECTOR_BELL_PREP.get(), 1.7F, 0.70F);
            level.sendParticles(SPECTRAL_FROST_DUST, landingPos.x, landingPos.y + 3.6D, landingPos.z,
                    16, 0.6D, 0.4D, 0.6D, 0.02D);
        } else if (this.introTicks >= INTRO_TOTAL_TICKS) {
            // Phase 6: Combat Unlock (9.5s)
            this.cleanupSleigh(level);
            this.setIntroStage(IntroStage.INTRO_COMPLETE);
            this.setBossState(BossState.PHASE_ONE);
            this.setEyesGlowing(true);
            this.bossEvent.setVisible(true);
            level.broadcastEntityEvent(this, EVENT_INTRO_COMPLETE);
        }
    }

    private void tickPhaseTransition(ServerLevel level) {
        this.getNavigation().stop();
        this.phaseTransitionTicks++;
        if (this.phaseTransitionTicks == 20 || this.phaseTransitionTicks == 40) {
            this.playSound(ModSounds.COLLECTOR_PHASE_TRANSITION.get(), 1.5F, 0.65F + this.phaseTransitionTicks * 0.005F);
            // Expanding frost ring (visual only during phase transition)
            double radius = this.phaseTransitionTicks * 0.14D;
            for (int i = 0; i < 16; i++) {
                double angle = (Math.PI * 2.0D * i) / 16.0D;
                level.sendParticles(ParticleTypes.SNOWFLAKE,
                        this.getX() + Math.cos(angle) * radius,
                        this.getY() + 0.3D,
                        this.getZ() + Math.sin(angle) * radius,
                        2, 0.15D, 0.1D, 0.15D, 0.01D);
            }
        }
        if (this.phaseTransitionTicks >= PHASE_TRANSITION_TICKS) {
            this.applyStage(this.pendingStage);
            this.frightfulPresence(level);
            if (this.getStage() == 3) {
                this.summonFey(level, 3);
            }
            this.setBossState(this.isPhaseTwo() ? BossState.PHASE_TWO : BossState.PHASE_ONE);
        }
    }

    /** Applies the stats and presentation of a stage (speed, cage, boss bar colour). */
    private void applyStage(int stage) {
        this.setStage(stage);
        this.setPhaseTwo(stage >= 3);
        this.setCageOpen(stage >= 3);
        this.setEyesGlowing(true);
        this.bossEvent.setColor(switch (this.getStage()) {
            case 1, 2 -> BossEvent.BossBarColor.WHITE;
            case 3 -> BossEvent.BossBarColor.PURPLE;
            case 4 -> BossEvent.BossBarColor.BLUE;
            case 5 -> BossEvent.BossBarColor.YELLOW;
            default -> BossEvent.BossBarColor.RED;
        });
        this.bossEvent.setCreateWorldFog(stage >= MAX_STAGE);
        boolean rage = stage >= MAX_STAGE;
        AttributeInstance speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(rage ? RAGE_SPEED : stage >= 5 ? STAGE_FIVE_SPEED : stage >= 3 ? PHASE_TWO_SPEED : PHASE_ONE_SPEED);
        }
        AttributeInstance armorAttr = this.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.setBaseValue(ARMOR + (rage ? RAGE_ARMOR_BONUS : 0.0D));
        }
        AttributeInstance damageAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.setBaseValue(SCYTHE_DAMAGE * (rage ? RAGE_DAMAGE_MULTIPLIER : 1.0F));
        }
    }

    /** Frightful Presence: everyone nearby is shaken (weakness and a pulse of darkness) whenever a stage begins. */
    private void frightfulPresence(ServerLevel level) {
        this.playSound(ModSounds.COLLECTOR_PHASE_TRANSITION.get(), 2.0F, 0.55F);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.0D, this.getZ(),
                40, 3.0D, 0.6D, 3.0D, 0.04D);
        for (Player player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(24.0D),
                p -> p.isAlive() && !p.isSpectator() && !p.isCreative())) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 50, 0));
        }
    }

    private List<GiftMimic> activeMinions(ServerLevel level) {
        return level.getEntitiesOfClass(GiftMimic.class, this.getBoundingBox().inflate(48.0D),
                r -> r.isAlive() && this.minionIds.contains(r.getUUID()));
    }

    /** Fey Summons: up to {@code count} Gift Mimics appear around him, never exceeding {@link #MAX_MINIONS} alive. */
    public int summonFey(ServerLevel level, int count) {
        int room = Math.min(count, MAX_MINIONS - this.activeMinions(level).size());
        int spawned = 0;
        for (int i = 0; i < room; i++) {
            if (this.spawnMimic(level, this.position(), 3.5D, 6.0D)) {
                spawned++;
            }
        }
        if (spawned > 0) {
            this.playSound(ModSounds.COLLECTOR_SLEIGH_BELLS.get(), 1.6F, 0.6F);
        }
        return spawned;
    }

    /** Spawns one Gift Mimic on a free spot between {@code minDist} and {@code maxDist} blocks from {@code around}. */
    private boolean spawnMimic(ServerLevel level, Vec3 around, double minDist, double maxDist) {
        GiftMimic mimic = ModEntities.GIFT_MIMIC.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mimic == null) {
            return false;
        }
        boolean placed = false;
        for (int attempt = 0; attempt < 8 && !placed; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double dist = minDist + this.random.nextDouble() * (maxDist - minDist);
            mimic.setPos(around.x + Math.cos(angle) * dist, around.y, around.z + Math.sin(angle) * dist);
            placed = level.noCollision(mimic);
        }
        if (!placed) {
            return false; // never spawn a minion inside a wall
        }
        this.minionIds.add(mimic.getUUID());
        mimic.finalizeSpawn(level, level.getCurrentDifficultyAt(mimic.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
        if (this.getTarget() != null) {
            mimic.setTarget(this.getTarget());
        }
        level.addFreshEntity(mimic);
        level.sendParticles(ParticleTypes.POOF, mimic.getX(), mimic.getY() + 0.6D, mimic.getZ(), 10, 0.3D, 0.4D, 0.3D, 0.02D);
        level.sendParticles(PRESENT_GOLD, mimic.getX(), mimic.getY() + 0.6D, mimic.getZ(), 8, 0.4D, 0.5D, 0.4D, 0.04D);
        return true;
    }

    /** Removes every summoned minion (also used on death and by tests). */
    public void discardMinions(ServerLevel level) {
        for (GiftMimic minion : this.activeMinions(level)) {
            level.sendParticles(ParticleTypes.POOF, minion.getX(), minion.getY() + 0.6D, minion.getZ(), 8, 0.3D, 0.4D, 0.3D, 0.02D);
            minion.discard();
        }
    }

    /** Cosmetic lightning (no fire, no damage) used to dress up the larger attacks. */
    private void visualLightning(ServerLevel level, Vec3 pos) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setPos(pos.x, pos.y, pos.z);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
    }

    /** Applies attack damage: stage 5 (Frenzied) adds a flat bonus to every hit, stage 6 (Rage) multiplies it too. */
    private void strike(ServerLevel level, LivingEntity victim, float damage) {
        float total = damage * (this.getStage() >= MAX_STAGE ? RAGE_DAMAGE_MULTIPLIER : 1.0F)
                + (this.getStage() >= 5 ? FRENZY_BONUS_DAMAGE : 0.0F);
        victim.hurtServer(level, this.damageSources().mobAttack(this), total);
    }

    // ---------- Choreographed Combat Attacks (Section 11) ----------

    /** Starts one of the four choreographed attacks if the boss is in a valid combat state. */
    public boolean startAttack(AttackType type, @Nullable LivingEntity target) {
        return this.startAttack(type, target, 0);
    }

    /** AI entry point: {@code jitter} randomly lengthens or shortens the wind-up so timings cannot be memorised. */
    private boolean startAttack(AttackType type, @Nullable LivingEntity target, int jitter) {
        if (type == AttackType.NONE || !this.isAlive()) {
            return false;
        }
        BossState current = this.getBossState();
        if (current != BossState.PHASE_ONE && current != BossState.PHASE_TWO) {
            return false;
        }
        if ((type == AttackType.SOLSTICE_JUMP && !this.isPhaseTwo())
                || ((type == AttackType.WHIRLWIND || type == AttackType.HYPNOTIC_GAZE || type == AttackType.CRESCENT_WAVE)
                    && this.getStage() < 2)
                || ((type == AttackType.COMET_BARRAGE || type == AttackType.GIFT_RAIN) && this.getStage() < 3)
                || (type == AttackType.FEY_SUMMON && this.getStage() < 3)
                || (type == AttackType.FROST_CONE && this.getStage() < 4)) {
            return false;
        }
        this.lastAttack = type;
        this.prepJitter = jitter;
        this.setActiveAttack(type);
        this.setBossState(BossState.ATTACK_PREPARATION);
        this.actionTimer = 0;
        this.getNavigation().stop();

        if (this.level() instanceof ServerLevel serverLevel) {
            switch (type) {
                case WINTER_SCYTHE -> {
                    serverLevel.broadcastEntityEvent(this, EVENT_ATTACK_SCYTHE_PREP);
                    this.playSound(ModSounds.COLLECTOR_SLEIGH_BELLS.get(), 1.1F, 0.70F);
                }
                case HOLLOW_BELL -> {
                    serverLevel.broadcastEntityEvent(this, EVENT_ATTACK_BELL_PREP);
                    this.playSound(ModSounds.COLLECTOR_BELL_PREP.get(), 1.5F, 0.68F);
                    this.prepareHollowBellRays(target);
                }
                case PAWN_PROCESSION -> {
                    this.setCageOpen(true);
                    serverLevel.broadcastEntityEvent(this, EVENT_ATTACK_PAWNS);
                    this.playSound(ModSounds.COLLECTOR_BELL_PREP.get(), 1.3F, 0.85F);
                    this.preparePawnZones(target);
                }
                case SOLSTICE_JUMP -> {
                    serverLevel.broadcastEntityEvent(this, EVENT_ATTACK_SOLSTICE_JUMP);
                    this.playSound(ModSounds.COLLECTOR_BELL_PREP.get(), 1.6F, 0.58F);
                    this.prepareSolsticeJumpTarget(target);
                }
                case WHIRLWIND -> {
                    serverLevel.broadcastEntityEvent(this, EVENT_ATTACK_SCYTHE_PREP);
                    this.playSound(ModSounds.COLLECTOR_SLEIGH_BELLS.get(), 1.4F, 0.55F);
                }
                case STOMP -> {
                    serverLevel.broadcastEntityEvent(this, EVENT_ATTACK_SLAM);
                    this.faceTarget(target);
                }
                case FROST_CONE -> {
                    this.setCageOpen(true);
                    serverLevel.broadcastEntityEvent(this, EVENT_ATTACK_CAST);
                    this.playSound(ModSounds.COLLECTOR_BELL_PREP.get(), 1.4F, 0.5F);
                    this.faceTarget(target);
                }
                case HYPNOTIC_GAZE -> {
                    serverLevel.broadcastEntityEvent(this, EVENT_INTRO_INTIMIDATE);
                    this.playSound(ModSounds.COLLECTOR_PHASE_TRANSITION.get(), 1.4F, 1.25F);
                    for (Player player : serverLevel.getEntitiesOfClass(Player.class,
                            this.getBoundingBox().inflate(GAZE_RANGE), p -> p.isAlive() && !p.isSpectator())) {
                        player.sendOverlayMessage(Component.translatable("entity.codenamecoraline.last_gift_collector.gaze_warning"));
                    }
                }
                case FEY_SUMMON -> {
                    serverLevel.broadcastEntityEvent(this, EVENT_ATTACK_SLAM);
                    this.playSound(ModSounds.COLLECTOR_BELL_PREP.get(), 1.6F, 0.9F);
                }
                case CRESCENT_WAVE -> {
                    serverLevel.broadcastEntityEvent(this, EVENT_ATTACK_SCYTHE_PREP);
                    this.playSound(ModSounds.COLLECTOR_SLEIGH_BELLS.get(), 1.2F, 0.8F);
                    this.faceTarget(target);
                }
                case COMET_BARRAGE -> {
                    serverLevel.broadcastEntityEvent(this, EVENT_ATTACK_CAST);
                    this.playSound(ModSounds.COLLECTOR_BELL_PREP.get(), 1.5F, 1.1F);
                    this.faceTarget(target);
                }
                case GIFT_RAIN -> {
                    serverLevel.broadcastEntityEvent(this, EVENT_ATTACK_CAST);
                    this.playSound(ModSounds.COLLECTOR_SLEIGH_BELLS.get(), 1.8F, 0.9F);
                    this.faceTarget(target);
                }
                case NONE -> {}
            }
        }
        return true;
    }

    private void prepareHollowBellRays(@Nullable LivingEntity target) {
        this.activeFrostWaveDirs.clear();
        this.frostWaveHitEntities.clear();
        this.frostWaveOrigin = this.position();
        float baseYaw = this.getYRot();
        if (target != null) {
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            baseYaw = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
            this.setYRot(baseYaw);
            this.setYHeadRot(baseYaw);
        }
        int[] angles = this.isPhaseTwo() ? new int[]{-44, -22, 0, 22, 44} : new int[]{-28, 0, 28};
        for (int offset : angles) {
            Vec3 dir = Vec3.directionFromRotation(0.0F, baseYaw + offset).multiply(1.0D, 0.0D, 1.0D).normalize();
            this.activeFrostWaveDirs.add(dir);
        }
    }

    private void preparePawnZones(@Nullable LivingEntity target) {
        this.activePawnZones.clear();
        Vec3 center;
        Vec3 forward;
        if (target != null) {
            center = this.predict(target, PAWN_PREP_TICKS);
            Vec3 toTarget = center.subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            forward = toTarget.lengthSqr() > 1.0E-4D ? toTarget.normalize() : Vec3.directionFromRotation(0.0F, this.getYRot());
        } else {
            forward = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
            center = this.position().add(forward.scale(4.5D));
        }
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        this.activePawnZones.add(center);
        this.activePawnZones.add(center.add(right.scale(3.0D)).add(forward.scale(1.0D)));
        this.activePawnZones.add(center.subtract(right.scale(3.0D)).add(forward.scale(1.0D)));
    }

    private void prepareSolsticeJumpTarget(@Nullable LivingEntity target) {
        this.solsticeStartPos = this.position();
        if (target != null) {
            Vec3 delta = target.position().subtract(this.solsticeStartPos);
            double horizontalDist = Math.min(SOLSTICE_MAX_LEAP_DIST, Math.sqrt(delta.x * delta.x + delta.z * delta.z));
            Vec3 horizDir = delta.multiply(1.0D, 0.0D, 1.0D);
            if (horizDir.lengthSqr() > 1.0E-4D) {
                horizDir = horizDir.normalize();
            } else {
                horizDir = Vec3.directionFromRotation(0.0F, this.getYRot());
            }
            this.solsticeTargetPos = new Vec3(
                    this.solsticeStartPos.x + horizDir.x * horizontalDist,
                    target.getY(),
                    this.solsticeStartPos.z + horizDir.z * horizontalDist);
        } else {
            Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
            this.solsticeTargetPos = this.solsticeStartPos.add(forward.scale(6.0D));
        }
    }

    /** Checks whether a candidate entity is inside the configured frontal arc (dot product >= minDot). */
    public boolean isInFrontArc(LivingEntity candidate, double minDot) {
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 toTarget = candidate.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (toTarget.lengthSqr() < 1.0E-4D) {
            return true;
        }
        return forward.dot(toTarget.normalize()) >= minDot;
    }

    public boolean isWithinScytheBox(LivingEntity candidate, double reach) {
        return this.getBoundingBox().inflate(reach, 0.8D, reach).intersects(candidate.getBoundingBox());
    }

    private void executeWinterScytheSweep(ServerLevel level) {
        this.playSound(ModSounds.COLLECTOR_SCYTHE_SLASH.get(), 1.4F, 0.78F + this.random.nextFloat() * 0.12F);
        this.axeArc(level, 3.4D);
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 sweepCenter = this.position().add(forward.scale(2.4D)).add(0.0D, 1.4D, 0.0D);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, sweepCenter.x, sweepCenter.y, sweepCenter.z, 2, 0.6D, 0.1D, 0.6D, 0.0D);
        level.sendParticles(ParticleTypes.SNOWFLAKE, sweepCenter.x, sweepCenter.y, sweepCenter.z, 14, 1.4D, 0.3D, 1.4D, 0.04D);

        AABB hitBox = this.getBoundingBox().inflate(SCYTHE_STRIKE_REACH, 0.8D, SCYTHE_STRIKE_REACH);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, hitBox,
                entity -> entity != this && entity.isAlive() && this.canAttack(entity));
        for (LivingEntity victim : candidates) {
            if (this.isWithinScytheBox(victim, SCYTHE_STRIKE_REACH)
                    && this.isInFrontArc(victim, SCYTHE_ARC_MIN_DOT)
                    && this.getSensing().hasLineOfSight(victim)) {
                this.strike(level, victim, SCYTHE_DAMAGE);
                victim.knockback(0.75D, -forward.x, -forward.z);
            }
        }
    }

    private void stepHollowBellWaves(ServerLevel level, int execTick) {
        double dist = Math.min(HOLLOW_BELL_MAX_RANGE, execTick * HOLLOW_BELL_WAVE_SPEED);
        for (Vec3 dir : this.activeFrostWaveDirs) {
            Vec3 wavePos = this.frostWaveOrigin.add(dir.scale(dist));
            for (int tail = 1; tail <= 6; tail++) {
                Vec3 t = this.frostWaveOrigin.add(dir.scale(Math.max(0.0D, dist - tail * 0.55D)));
                level.sendParticles(tail % 2 == 0 ? COMET_DUST : CRESCENT_DUST, t.x, t.y + 0.3D + tail * 0.04D, t.z, 1, 0.12D, 0.1D, 0.12D, 0.0D);
            }
            level.sendParticles(ParticleTypes.END_ROD, wavePos.x, wavePos.y + 0.5D, wavePos.z, 1, 0.1D, 0.2D, 0.1D, 0.01D);
            level.sendParticles(ParticleTypes.SNOWFLAKE, wavePos.x, wavePos.y + 0.35D, wavePos.z,
                    4, 0.35D, 0.2D, 0.35D, 0.01D);
            level.sendParticles(SPECTRAL_FROST_DUST, wavePos.x, wavePos.y + 0.25D, wavePos.z,
                    2, 0.25D, 0.15D, 0.25D, 0.0D);

            AABB waveBox = new AABB(
                    wavePos.x - HOLLOW_BELL_RAY_RADIUS, wavePos.y - 0.5D, wavePos.z - HOLLOW_BELL_RAY_RADIUS,
                    wavePos.x + HOLLOW_BELL_RAY_RADIUS, wavePos.y + 2.2D, wavePos.z + HOLLOW_BELL_RAY_RADIUS);
            List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, waveBox,
                    entity -> entity != this && entity.isAlive() && this.canAttack(entity));
            for (LivingEntity victim : victims) {
                if (this.frostWaveHitEntities.add(victim.getUUID())) {
                    this.strike(level, victim, HOLLOW_BELL_DAMAGE);
                    victim.knockback(0.45D, -dir.x, -dir.z);
                }
            }
        }
    }

    private void eruptPawnProcessionRoots(ServerLevel level) {
        this.playSound(ModSounds.COLLECTOR_FROST_SHATTER.get(), 1.4F, 0.75F);
        BlockParticleOption rootParticles = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_WOOD.defaultBlockState());
        BlockParticleOption iceParticles = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState());
        Set<UUID> hitThisEruption = new HashSet<>();

        for (Vec3 zone : this.activePawnZones) {
            level.sendParticles(rootParticles, zone.x, zone.y + 0.6D, zone.z, 14, 0.6D, 0.5D, 0.6D, 0.06D);
            level.sendParticles(iceParticles, zone.x, zone.y + 0.6D, zone.z, 10, 0.6D, 0.5D, 0.6D, 0.06D);
            AABB zoneBox = new AABB(
                    zone.x - PAWN_ZONE_RADIUS, zone.y - 0.5D, zone.z - PAWN_ZONE_RADIUS,
                    zone.x + PAWN_ZONE_RADIUS, zone.y + 2.5D, zone.z + PAWN_ZONE_RADIUS);
            List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, zoneBox,
                    entity -> entity != this && entity.isAlive() && this.canAttack(entity));
            for (LivingEntity victim : victims) {
                double dx = victim.getX() - zone.x;
                double dz = victim.getZ() - zone.z;
                if (dx * dx + dz * dz <= PAWN_ZONE_RADIUS * PAWN_ZONE_RADIUS && hitThisEruption.add(victim.getUUID())) {
                    this.strike(level, victim, PAWN_DAMAGE);
                }
            }
        }
    }

    private void executeSolsticeLandingImpact(ServerLevel level) {
        this.playSound(ModSounds.COLLECTOR_SCYTHE_SLASH.get(), 1.7F, 0.58F);
        this.playSound(ModSounds.COLLECTOR_FROST_SHATTER.get(), 1.6F, 0.65F);

        BlockParticleOption iceBurst = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState());
        level.sendParticles(iceBurst, this.getX(), this.getY() + 0.2D, this.getZ(), 28, 1.8D, 0.3D, 1.8D, 0.10D);
        level.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 0.4D, this.getZ(), 32, 2.5D, 0.3D, 2.5D, 0.06D);

        AABB search = this.getBoundingBox().inflate(SOLSTICE_WAVE_RADIUS, 1.2D, SOLSTICE_WAVE_RADIUS);
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, search,
                entity -> entity != this && entity.isAlive() && this.canAttack(entity));
        for (LivingEntity victim : victims) {
            double dist = Math.sqrt(this.distanceToSqr(victim));
            if (dist <= SOLSTICE_IMPACT_RADIUS) {
                this.strike(level, victim, SOLSTICE_DIRECT_DAMAGE);
                Vec3 push = victim.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
                victim.knockback(0.95D, -push.x, -push.z);
            } else if (dist <= SOLSTICE_WAVE_RADIUS) {
                this.strike(level, victim, SOLSTICE_WAVE_DAMAGE);
            }
        }
    }

    private void faceTarget(@Nullable LivingEntity target) {
        if (target == null) {
            return;
        }
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        float yaw = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.yBodyRot = yaw;
    }

    private List<LivingEntity> victimsWithin(ServerLevel level, Vec3 center, double radius) {
        AABB box = new AABB(center.x - radius, center.y - 1.5D, center.z - radius, center.x + radius, center.y + 3.0D, center.z + radius);
        return level.getEntitiesOfClass(LivingEntity.class, box, entity -> entity != this && entity.isAlive()
                && this.canAttack(entity) && !this.minionIds.contains(entity.getUUID())
                && entity.position().subtract(center).multiply(1.0D, 0.0D, 1.0D).lengthSqr() <= radius * radius);
    }

    private void shove(LivingEntity victim, Vec3 origin, double strength) {
        Vec3 away = victim.position().subtract(origin).multiply(1.0D, 0.0D, 1.0D);
        away = away.lengthSqr() > 1.0E-4D ? away.normalize() : Vec3.directionFromRotation(0.0F, this.getYRot());
        victim.knockback(strength, -away.x, -away.z);
    }

    /** Pisada do Renegado: a shockwave ring around the boss; {@code radius} lets the legendary reaction reuse it. */
    private void stompBlast(ServerLevel level, double radius, float damage, double knock) {
        this.playSound(ModSounds.COLLECTOR_FROST_SHATTER.get(), 1.8F, 0.55F);
        this.playSound(ModSounds.COLLECTOR_STEP.get(), 2.0F, 0.5F);
        BlockParticleOption debris = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState());
        for (int ring = 1; ring <= 3; ring++) {
            double r = radius * ring / 3.0D;
            int points = 14 + ring * 8;
            for (int i = 0; i < points; i++) {
                double a = Math.PI * 2.0D * i / points;
                level.sendParticles(debris, this.getX() + Math.cos(a) * r, this.getY() + 0.2D, this.getZ() + Math.sin(a) * r,
                        2, 0.1D, 0.3D, 0.1D, 0.12D);
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.3D, this.getZ(), 2, 0.8D, 0.1D, 0.8D, 0.0D);
        for (LivingEntity victim : this.victimsWithin(level, this.position(), radius)) {
            this.strike(level, victim, damage);
            this.shove(victim, this.position(), knock);
        }
    }

    private void executeStomp(ServerLevel level, int timer) {
        if (timer == 2) {
            this.stompBlast(level, STOMP_RADIUS, STOMP_DAMAGE, 0.95D);
            if (this.getStage() >= 3) {
                // Cosmetic lightning around the ring from stage 3 on, so the later stages look the part.
                for (int i = 0; i < 3; i++) {
                    double a = Math.PI * 2.0D * i / 3.0D + this.random.nextDouble();
                    this.visualLightning(level, this.position().add(Math.cos(a) * STOMP_RADIUS * 0.8D, 0.0D, Math.sin(a) * STOMP_RADIUS * 0.8D));
                }
            }
        }
    }

    /** Giro do Machado: spin with the greataxe, hitting everything in reach every few ticks and dragging the nearest target. */
    private void executeWhirlwind(ServerLevel level, int timer, @Nullable LivingEntity target) {
        float spin = this.getYRot() + 31.0F;
        this.setYRot(spin);
        this.yBodyRot = spin;
        this.setYHeadRot(spin);
        if (target != null) {
            Vec3 toward = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            if (toward.lengthSqr() > 4.0D) {
                Vec3 step = toward.normalize().scale(this.getStage() >= 5 ? 0.30D : 0.22D);
                this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
            }
        }
        for (int i = 0; i < 8; i++) {
            double a = Math.toRadians(spin * 3.0D + i * 45.0D);
            double r = WHIRL_RADIUS * (0.55D + 0.45D * ((timer + i) % 4) / 3.0D);
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + Math.cos(a) * r, this.getY() + 1.2D, this.getZ() + Math.sin(a) * r,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 1.0D, this.getZ(), 10, WHIRL_RADIUS * 0.5D, 0.5D, WHIRL_RADIUS * 0.5D, 0.05D);
        if (timer % 8 == 2) {
            this.playSound(ModSounds.COLLECTOR_SCYTHE_SLASH.get(), 1.5F, 0.6F + this.random.nextFloat() * 0.2F);
            for (LivingEntity victim : this.victimsWithin(level, this.position(), WHIRL_RADIUS)) {
                this.strike(level, victim, WHIRL_DAMAGE);
                this.shove(victim, this.position(), 0.55D);
            }
        }
    }

    /** Sopro do Inverno: a wide frost cone that damages, slows and freezes. */
    private void executeFrostCone(ServerLevel level, int timer) {
        Vec3 dir = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 origin = this.position().add(0.0D, 2.6D, 0.0D).add(dir.scale(1.0D));
        if (timer >= 2 && timer % 2 == 0) {
            double reach = Math.min(CONE_LENGTH, timer * 0.6D);
            for (int i = 0; i < 6; i++) {
                double spread = (this.random.nextDouble() - 0.5D) * 1.2D;
                Vec3 side = new Vec3(-dir.z, 0.0D, dir.x);
                Vec3 p = origin.add(dir.scale(reach)).add(side.scale(spread * reach * 0.45D)).add(0.0D, -1.2D - reach * 0.08D, 0.0D);
                level.sendParticles(ParticleTypes.SNOWFLAKE, p.x, p.y, p.z, 5, 0.3D, 0.25D, 0.3D, 0.05D);
                level.sendParticles(SPECTRAL_FROST_DUST, p.x, p.y, p.z, 2, 0.3D, 0.2D, 0.3D, 0.0D);
            }
        }
        if (timer == 4 || timer == 12) {
            this.playSound(ModSounds.COLLECTOR_FROST_SHATTER.get(), 1.5F, 0.9F);
            for (LivingEntity victim : this.victimsWithin(level, this.position(), CONE_LENGTH)) {
                Vec3 to = victim.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
                if (to.lengthSqr() < 1.0E-4D || dir.dot(to.normalize()) >= CONE_MIN_DOT) {
                    this.strike(level, victim, CONE_DAMAGE);
                    victim.setTicksFrozen(victim.getTicksRequiredToFreeze() + 60);
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                }
            }
        }
    }

    /** Olhar Hipnótico: punishes only those who keep staring at him — look away (or break line of sight) to dodge. */
    private void executeHypnoticGaze(ServerLevel level) {
        this.playSound(ModSounds.COLLECTOR_LIGHTNING.get(), 1.2F, 1.6F);
        Vec3 eyes = this.getEyePosition();
        for (Player player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(GAZE_RANGE),
                p -> p.isAlive() && !p.isSpectator() && this.canAttack(p))) {
            Vec3 toBoss = eyes.subtract(player.getEyePosition());
            if (toBoss.lengthSqr() < 1.0E-4D || !this.getSensing().hasLineOfSight(player)) {
                continue;
            }
            if (player.getViewVector(1.0F).dot(toBoss.normalize()) >= GAZE_FACING_DOT) {
                this.strike(level, player, GAZE_DAMAGE);
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 3));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0));
                level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getEyeY(), player.getZ(), 12, 0.3D, 0.3D, 0.3D, 0.05D);
            }
        }
    }

    /** Restless: closes long distances with a short, loud charge. */
    private void dashToward(ServerLevel level, LivingEntity target) {
        Vec3 dir = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (dir.lengthSqr() < 1.0E-4D) {
            return;
        }
        dir = dir.normalize();
        this.setDeltaMovement(dir.x * 1.05D, 0.28D, dir.z * 1.05D);
        this.hurtMarked = true;
        this.dashCooldown = DASH_COOLDOWN_TICKS;
        this.playSound(ModSounds.COLLECTOR_STEP.get(), 1.6F, 0.5F);
        level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.2D, this.getZ(), 12, 0.8D, 0.1D, 0.8D, 0.04D);
    }

    /** Weighted, non-repeating attack choice from what the stage, range, height, line of sight and kiting allow. */
    private AttackType pickAttack(LivingEntity target) {
        double distSqr = this.distanceToSqr(target);
        int stage = this.getStage();
        double dy = target.getY() - this.getY();
        boolean elevated = dy > 2.0D && distSqr < 169.0D;   // pillar / ledge above: the axe cannot reach
        boolean lowered = dy < -3.0D;                         // pit below: do not chop the air above it
        boolean sight = this.getSensing().hasLineOfSight(target);
        boolean cannotMelee = elevated || lowered || !sight || this.unreachableTicks > 24;
        boolean kiting = this.farTicks > 50 || this.unreachableTicks > 24;
        boolean melee = !cannotMelee && this.isWithinScytheBox(target, SCYTHE_START_REACH);
        List<AttackType> options = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        if (melee) {
            options.add(AttackType.WINTER_SCYTHE);
            weights.add(this.scytheCooldown <= 0 ? 6 : 2);
        }
        if (!cannotMelee && this.cooldowns[AttackType.STOMP.ordinal()] <= 0 && distSqr <= 30.0D) {
            options.add(AttackType.STOMP);
            weights.add(5);
        }
        if (!cannotMelee && stage >= 2 && this.cooldowns[AttackType.WHIRLWIND.ordinal()] <= 0 && distSqr <= 49.0D) {
            options.add(AttackType.WHIRLWIND);
            weights.add(stage >= 4 ? 6 : 4);
        }
        if (this.hollowBellCooldown <= 0 && distSqr >= 9.0D && distSqr <= 144.0D && sight && Math.abs(dy) <= 2.5D) {
            options.add(AttackType.HOLLOW_BELL);
            weights.add(4);
        }
        if (this.pawnCooldown <= 0 && distSqr >= 6.0D && distSqr <= 169.0D) {
            options.add(AttackType.PAWN_PROCESSION);
            weights.add(3);
        }
        if (this.isPhaseTwo() && this.solsticeJumpCooldown <= 0 && distSqr >= 20.0D && distSqr <= 196.0D) {
            options.add(AttackType.SOLSTICE_JUMP);
            weights.add(elevated ? 8 : 5);
        }
        if (stage >= 2 && this.cooldowns[AttackType.CRESCENT_WAVE.ordinal()] <= 0 && distSqr >= 9.0D && distSqr <= 400.0D
                && sight && Math.abs(dy) <= 2.5D) {
            options.add(AttackType.CRESCENT_WAVE);
            weights.add(kiting ? 14 : 4);
        }
        if (stage >= 3 && this.cooldowns[AttackType.COMET_BARRAGE.ordinal()] <= 0 && distSqr >= 16.0D && distSqr <= 900.0D
                && this.getSensing().hasLineOfSight(target)) {
            options.add(AttackType.COMET_BARRAGE);
            weights.add(kiting || elevated || lowered ? 16 : 5);
        }
        if (stage >= 3 && this.cooldowns[AttackType.GIFT_RAIN.ordinal()] <= 0 && distSqr >= 9.0D && distSqr <= 900.0D) {
            options.add(AttackType.GIFT_RAIN);
            weights.add(kiting || elevated || lowered ? 14 : 4);
        }
        if (stage >= 2 && this.cooldowns[AttackType.HYPNOTIC_GAZE.ordinal()] <= 0
                && distSqr <= GAZE_RANGE * GAZE_RANGE && this.getSensing().hasLineOfSight(target)) {
            options.add(AttackType.HYPNOTIC_GAZE);
            weights.add(3);
        }
        if (!cannotMelee && stage >= 4 && this.cooldowns[AttackType.FROST_CONE.ordinal()] <= 0 && distSqr >= 9.0D && distSqr <= 72.0D) {
            options.add(AttackType.FROST_CONE);
            weights.add(5);
        }
        if (stage >= 3 && this.cooldowns[AttackType.FEY_SUMMON.ordinal()] <= 0
                && this.level() instanceof ServerLevel sl && this.activeMinions(sl).size() < 2) {
            options.add(AttackType.FEY_SUMMON);
            weights.add(4);
        }
        if (options.size() > 1 && !kiting) {
            int idx = options.indexOf(this.lastAttack);
            if (idx >= 0) {
                options.remove(idx);
                weights.remove(idx);
            }
        }
        if (options.isEmpty()) {
            return AttackType.NONE;
        }
        int total = weights.stream().mapToInt(Integer::intValue).sum();
        int roll = this.random.nextInt(total);
        for (int i = 0; i < options.size(); i++) {
            roll -= weights.get(i);
            if (roll < 0) {
                return options.get(i);
            }
        }
        return options.get(0);
    }

    private int rollJitter() {
        int stage = this.getStage();
        return this.random.nextInt(11) - 3 - (stage >= 4 ? 2 : 0);
    }

    // ---------- Intelligence: targeting, prediction, kiting, tactical movement, blinking ----------

    /** Picks who to fight: whoever last hurt him, otherwise the nearest player; switches only with a real reason. */
    private @Nullable LivingEntity chooseTarget(ServerLevel level, @Nullable LivingEntity current) {
        boolean currentOk = current != null && current.isAlive() && this.canAttack(current)
                && this.distanceToSqr(current) <= FOLLOW_RANGE * FOLLOW_RANGE;
        if (currentOk && this.tickCount % 20 != 0) {
            return current;
        }
        LivingEntity attacker = this.getLastHurtByMob();
        if (attacker instanceof Player p && this.tickCount - this.getLastHurtByMobTimestamp() < 100
                && p.isAlive() && !p.isSpectator() && this.canAttack(p) && this.distanceToSqr(p) <= FOLLOW_RANGE * FOLLOW_RANGE) {
            if (p != current) {
                this.setTarget(p);
            }
            return p;
        }
        Player best = null;
        double bestDist = Double.MAX_VALUE;
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(FOLLOW_RANGE),
                pl -> pl.isAlive() && !pl.isSpectator() && this.canAttack(pl))) {
            double d = this.distanceToSqr(p);
            if (d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        if (currentOk && (best == null || bestDist > this.distanceToSqr(current) - 16.0D)) {
            return current; // hysteresis: do not flip-flop between targets
        }
        if (best != null && best != current) {
            this.setTarget(best);
        }
        return best != null ? best : (currentOk ? current : null);
    }

    private void trackTarget(LivingEntity target) {
        Vec3 now = target.position();
        this.targetVelocity = this.targetLastPos == Vec3.ZERO ? Vec3.ZERO : now.subtract(this.targetLastPos);
        if (this.targetVelocity.lengthSqr() > 1.0D) {
            this.targetVelocity = Vec3.ZERO; // teleport or respawn: ignore the jump
        }
        this.targetLastPos = now;
    }

    /** Where {@code target} will probably stand in {@code ticks}: attacks are aimed with this lead, not at the old spot. */
    private Vec3 predict(LivingEntity target, int ticks) {
        Vec3 velocity = target == this.getTarget() ? this.targetVelocity : Vec3.ZERO;
        Vec3 lead = new Vec3(velocity.x, 0.0D, velocity.z).scale(Math.min(ticks, 24));
        return target.position().add(lead);
    }

    /** Counts how long the target stays out of melee reach, so a kiting player is answered with ranged magic. */
    private void watchForKiting(ServerLevel level, LivingEntity target, double distSqr) {
        if (distSqr > 169.0D) {
            this.farTicks++;
        } else if (this.farTicks > 0) {
            this.farTicks--;
        }
        // Stuck detection: not making progress towards a distant target means a wall, a ledge or bad pathing.
        if (this.tickCount % 20 == 0) {
            if (distSqr > 36.0D && this.position().distanceToSqr(this.stuckAnchor) < 0.5D) {
                this.stuckTicks += 20;
            } else {
                this.stuckTicks = 0;
            }
            this.stuckAnchor = this.position();
        }
        if (this.stuckTicks >= 80 && this.blinkCooldown <= 0) {
            this.blinkNear(level, target);
        }
    }

    /** Frost blink: when walking is hopeless he steps through the snow to a free spot near the target. */
    private void blinkNear(ServerLevel level, LivingEntity target) {
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double dist = 5.0D + this.random.nextDouble() * 2.5D;
            double x = target.getX() + Math.cos(angle) * dist;
            double z = target.getZ() + Math.sin(angle) * dist;
            Vec3 old = this.position();
            this.setPos(x, target.getY(), z);
            if (level.noCollision(this)) {
                this.blinkCooldown = 160;
                this.stuckTicks = 0;
                level.sendParticles(ParticleTypes.SNOWFLAKE, old.x, old.y + 1.5D, old.z, 30, 0.8D, 1.4D, 0.8D, 0.05D);
                level.sendParticles(COMET_DUST, x, this.getY() + 1.5D, z, 24, 0.8D, 1.4D, 0.8D, 0.04D);
                this.playSound(ModSounds.COLLECTOR_SLEIGH_BELLS.get(), 1.4F, 1.3F);
                this.getNavigation().stop();
                return;
            }
            this.setPos(old.x, old.y, old.z);
        }
    }

    /**
     * Chasing and positioning between attacks. He plans a real path to where the target is going, closes in when an
     * axe attack is ready (instead of hovering out of reach), circles when everything is on cooldown, hops up ledges
     * and steps, and never walks into the open air above a pit.
     */
    private void moveTactically(ServerLevel level, LivingEntity target, double distSqr) {
        double dist = Math.sqrt(distSqr);
        double dy = target.getY() - this.getY();
        double speed = 1.05D + (this.getStage() >= 5 ? 0.2D : 0.0D);
        if (this.strafeTicks-- <= 0) {
            this.strafeTicks = 30 + this.random.nextInt(40);
            this.strafeDir = this.random.nextBoolean() ? 1 : -1;
        }

        // Terrain: step up when something blocks the way, hop onto a ledge the player stands on.
        if (this.onGround() && this.horizontalCollision && this.getNavigation().isInProgress()) {
            this.getJumpControl().jump();
        }
        if (this.hopCooldown <= 0 && this.onGround() && dy > 1.1D && dy <= 3.4D && dist < 5.5D) {
            Vec3 toward = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            if (toward.lengthSqr() > 1.0E-4D) {
                toward = toward.normalize();
                this.setDeltaMovement(toward.x * 0.42D, 0.66D, toward.z * 0.42D);
                this.hopCooldown = 30;
                this.playSound(ModSounds.COLLECTOR_STEP.get(), 1.4F, 0.6F);
            }
        }

        boolean axeReady = this.scytheCooldown <= 0 || this.cooldowns[AttackType.STOMP.ordinal()] <= 0;
        boolean pit = dy < -3.0D;
        Vec3 goal = this.predict(target, Math.min(12, (int) (dist * 1.4D)));
        double standOff = pit ? 5.5D : axeReady ? 2.3D : 4.4D;

        if (dist > standOff + 0.8D && !(pit && dist < 6.5D)) {
            // Chase: a full path to the predicted spot, refreshed often; judge reachability from it.
            if (this.pathRecalcTicks <= 0 || this.getNavigation().isDone()) {
                var path = this.getNavigation().createPath(goal.x, goal.y, goal.z, 1);
                if (path != null && path.canReach()) {
                    this.unreachableTicks = 0;
                    this.getNavigation().moveTo(path, speed);
                } else {
                    this.unreachableTicks += 6;
                    this.getNavigation().moveTo(target, speed); // get as close as the terrain allows
                }
                this.pathRecalcTicks = 6;
            }
            return;
        }
        this.unreachableTicks = Math.max(0, this.unreachableTicks - 2);
        if (axeReady && !pit) {
            return; // in position: pickAttack decides within a tick, no wandering off
        }
        // Everything on cooldown: circle around the target at a fighting distance, never backing into ledges.
        Vec3 away = this.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
        if (away.lengthSqr() < 1.0E-4D) {
            away = Vec3.directionFromRotation(0.0F, this.getYRot());
        }
        away = away.normalize();
        double wanted = Math.max(dist < 3.2D ? 4.8D : dist, 4.2D);
        double angle = Math.toRadians(26.0D) * this.strafeDir;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        Vec3 rotated = new Vec3(away.x * cos - away.z * sin, 0.0D, away.x * sin + away.z * cos);
        Vec3 spot = target.position().add(rotated.scale(wanted));
        if ((this.pathRecalcTicks <= 0 || this.getNavigation().isDone()) && this.isSafeToStand(level, spot)) {
            this.getNavigation().moveTo(spot.x, this.getY(), spot.z, speed);
            this.pathRecalcTicks = 8;
        }
    }

    /** True when {@code spot} has ground within a short step: no walking off cliffs while strafing. */
    private boolean isSafeToStand(ServerLevel level, Vec3 spot) {
        BlockPos base = BlockPos.containing(spot.x, this.getY(), spot.z);
        for (int dy = 0; dy >= -3; dy--) {
            BlockPos below = base.offset(0, dy - 1, 0);
            if (!level.getBlockState(below).getCollisionShape(level, below).isEmpty() || !level.getFluidState(below).isEmpty()) {
                return level.getFluidState(below).isEmpty();
            }
        }
        return false;
    }

    private void lungeToward(LivingEntity target, double speed) {
        Vec3 dir = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (dir.lengthSqr() > 1.0E-4D) {
            dir = dir.normalize();
            this.setDeltaMovement(dir.x * speed, 0.12D, dir.z * speed);
            this.playSound(ModSounds.COLLECTOR_STEP.get(), 1.3F, 0.7F);
        }
    }

    /** Whether the planned attack can still land on the target as it stands now (movement during wind-up, height, walls). */
    private boolean attackStillUseful(AttackType type, @Nullable LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return type == AttackType.GIFT_RAIN || type == AttackType.FEY_SUMMON;
        }
        Vec3 d = target.position().subtract(this.position());
        double horizontal = Math.sqrt(d.x * d.x + d.z * d.z);
        boolean sameLevel = d.y <= 2.6D && d.y >= -3.2D;
        return switch (type) {
            case WINTER_SCYTHE -> sameLevel && this.isWithinScytheBox(target, SCYTHE_STRIKE_REACH + 2.6D);
            case STOMP -> sameLevel && horizontal <= STOMP_RADIUS + 1.4D;
            case WHIRLWIND -> sameLevel && horizontal <= WHIRL_RADIUS + 3.5D;
            case FROST_CONE -> sameLevel && horizontal <= CONE_LENGTH + 1.0D;
            case CRESCENT_WAVE -> Math.abs(d.y) <= 3.5D && horizontal >= 2.5D;
            case COMET_BARRAGE -> horizontal <= 32.0D; // they arc down from above, no line of sight needed
            default -> true;
        };
    }

    /** Cancels a wind-up whose target slipped away: no swinging at the air; he re-plans and chases instead. */
    private void abortAttack(ServerLevel level, AttackType type) {
        switch (type) {
            case WINTER_SCYTHE -> this.scytheCooldown = Math.max(this.scytheCooldown, 10);
            case HOLLOW_BELL -> this.hollowBellCooldown = Math.max(this.hollowBellCooldown, 20);
            case PAWN_PROCESSION -> this.pawnCooldown = Math.max(this.pawnCooldown, 20);
            default -> this.cooldowns[type.ordinal()] = Math.max(this.cooldowns[type.ordinal()], 25);
        }
        this.setActiveAttack(AttackType.NONE);
        this.actionTimer = 0;
        this.recoveryTicks = 4;
        this.chainNext = false;
        this.activeFrostWaveDirs.clear();
        this.activePawnZones.clear();
        this.setBossState(this.isPhaseTwo() ? BossState.PHASE_TWO : BossState.PHASE_ONE);
        level.broadcastEntityEvent(this, EVENT_INTRO_COMPLETE); // cuts the wind-up animation
    }

    // ---------- Axe and spell effects: arcs, bolts with trails, falling presents ----------

    private enum BoltKind { CRESCENT, COMET, PRESENT }

    private static final class Bolt {
        final BoltKind kind;
        Vec3 pos;
        Vec3 vel;
        int age;
        final int maxAge;
        final float damage;
        final double radius;
        final int homingDelay;
        final @Nullable UUID homingTarget;
        final Set<UUID> hit = new HashSet<>();

        Bolt(BoltKind kind, Vec3 pos, Vec3 vel, int maxAge, float damage, double radius, int homingDelay, @Nullable UUID homingTarget) {
            this.kind = kind;
            this.pos = pos;
            this.vel = vel;
            this.maxAge = maxAge;
            this.damage = damage;
            this.radius = radius;
            this.homingDelay = homingDelay;
            this.homingTarget = homingTarget;
        }
    }

    private static final class RainSpot {
        final Vec3 pos;
        int delay;

        RainSpot(Vec3 pos, int delay) {
            this.pos = pos;
            this.delay = delay;
        }
    }

    public int getActiveBoltCount() {
        return this.bolts.size();
    }

    public int getPendingRainCount() {
        return this.rainSpots.size();
    }

    /** A bright arc of frost drawn along the cleave of the axe, in front of the boss. */
    private void axeArc(ServerLevel level, double radius) {
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 side = new Vec3(-f.z, 0.0D, f.x);
        for (int i = -7; i <= 7; i++) {
            double s = i / 7.0D;
            double ang = s * 1.15D;
            Vec3 p = this.position().add(f.scale(Math.cos(ang) * radius)).add(side.scale(Math.sin(ang) * radius));
            level.sendParticles(CRESCENT_DUST, p.x, p.y + 1.3D + (1.0D - Math.abs(s)) * 0.5D, p.z, 1, 0.05D, 0.05D, 0.05D, 0.0D);
            if (i % 2 == 0) {
                level.sendParticles(ParticleTypes.SNOWFLAKE, p.x, p.y + 1.4D, p.z, 1, 0.1D, 0.1D, 0.1D, 0.02D);
            }
        }
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + f.x * radius * 0.6D, this.getY() + 1.4D, this.getZ() + f.z * radius * 0.6D,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    /** The Renegade's Greataxe calls the gifts: its frost runes flare at the head of the axe, in his right hand. */
    private void axeSummonFlash(ServerLevel level) {
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 right = new Vec3(f.z, 0.0D, -f.x);
        Vec3 head = this.position().add(right.scale(1.6D)).add(f.scale(0.4D)).add(0.0D, 3.6D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, head.x, head.y, head.z, 26, 0.5D, 0.6D, 0.5D, 0.12D);
        level.sendParticles(COMET_DUST, head.x, head.y, head.z, 18, 0.4D, 0.5D, 0.4D, 0.05D);
        level.sendParticles(PRESENT_GOLD, head.x, head.y, head.z, 14, 0.6D, 0.6D, 0.6D, 0.1D);
        this.playSound(ModSounds.COLLECTOR_BELL_PREP.get(), 1.6F, 1.3F);
    }

    private void launchCrescents(ServerLevel level) {
        this.playSound(ModSounds.COLLECTOR_SCYTHE_SLASH.get(), 1.7F, 0.55F);
        this.axeArc(level, 3.6D);
        int[] offsets = this.getStage() >= 4 ? new int[]{-24, 0, 24} : new int[]{0};
        for (int off : offsets) {
            Vec3 dir = Vec3.directionFromRotation(0.0F, this.getYRot() + off).multiply(1.0D, 0.0D, 1.0D).normalize();
            this.bolts.add(new Bolt(BoltKind.CRESCENT, this.position().add(0.0D, 0.9D, 0.0D).add(dir.scale(1.6D)),
                    dir.scale(CRESCENT_SPEED), CRESCENT_MAX_AGE, CRESCENT_DAMAGE, CRESCENT_RADIUS, 0, null));
        }
    }

    private void executeCometBarrage(ServerLevel level, int timer, @Nullable LivingEntity target) {
        int total = Math.min(6, this.getStage() + 1);
        if (timer >= 2 && (timer - 2) % 4 == 0 && (timer - 2) / 4 < total) {
            int index = (timer - 2) / 4;
            Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
            Vec3 side = new Vec3(-f.z, 0.0D, f.x);
            double lateral = (index % 2 == 0 ? 1.0D : -1.0D) * (1.2D + 0.3D * (index / 2));
            Vec3 origin = this.position().add(0.0D, 4.8D, 0.0D).add(side.scale(lateral));
            Vec3 vel = new Vec3(side.x * lateral * 0.12D, 0.42D, side.z * lateral * 0.12D);
            this.bolts.add(new Bolt(BoltKind.COMET, origin, vel, 70, COMET_DAMAGE, COMET_RADIUS, 8,
                    target != null ? target.getUUID() : null));
            this.playSound(ModSounds.COLLECTOR_LIGHTNING.get(), 1.0F, 1.7F);
            level.sendParticles(ParticleTypes.END_ROD, origin.x, origin.y, origin.z, 10, 0.2D, 0.2D, 0.2D, 0.08D);
        }
    }

    private void planGiftRain(ServerLevel level, @Nullable LivingEntity target) {
        Vec3 center = target != null ? this.predict(target, RAIN_WARNING_TICKS) : this.position().add(
                Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize().scale(6.0D));
        double y = target != null ? target.getY() : this.getY();
        int count = 3 + this.getStage();
        for (int i = 0; i < count; i++) {
            Vec3 pos;
            if (i == 0) {
                pos = new Vec3(center.x, y, center.z);
            } else {
                double a = this.random.nextDouble() * Math.PI * 2.0D;
                double r = 2.0D + this.random.nextDouble() * 5.5D;
                pos = new Vec3(center.x + Math.cos(a) * r, y, center.z + Math.sin(a) * r);
            }
            this.rainSpots.add(new RainSpot(pos, RAIN_WARNING_TICKS + i * 3 + this.random.nextInt(6)));
        }
        this.playSound(ModSounds.COLLECTOR_BELL_PREP.get(), 1.6F, 1.0F);
    }

    private void tickBolts(ServerLevel level) {
        for (int i = this.rainSpots.size() - 1; i >= 0; i--) {
            RainSpot spot = this.rainSpots.get(i);
            if (spot.delay % 3 == 0) {
                for (int k = 0; k < 14; k++) {
                    double a = Math.PI * 2.0D * k / 14.0D + spot.delay * 0.15D;
                    level.sendParticles(k % 2 == 0 ? PRESENT_RED : PRESENT_GOLD,
                            spot.pos.x + Math.cos(a) * RAIN_RADIUS, spot.pos.y + 0.12D, spot.pos.z + Math.sin(a) * RAIN_RADIUS,
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
            if (spot.delay-- <= 0) {
                this.bolts.add(new Bolt(BoltKind.PRESENT, spot.pos.add(0.0D, 15.0D, 0.0D), new Vec3(0.0D, -1.25D, 0.0D),
                        40, RAIN_DAMAGE, RAIN_RADIUS, 0, null));
                this.rainSpots.remove(i);
            }
        }
        for (int i = this.bolts.size() - 1; i >= 0; i--) {
            if (this.stepBolt(level, this.bolts.get(i))) {
                this.bolts.remove(i);
            }
        }
    }

    /** Advances one bolt: steering, trail, collisions. Returns true when it is spent. */
    private boolean stepBolt(ServerLevel level, Bolt b) {
        Vec3 prev = b.pos;
        b.age++;
        if (b.kind == BoltKind.COMET) {
            if (b.age <= b.homingDelay) {
                b.vel = b.vel.scale(0.9D);
            } else if (b.homingTarget != null && b.age < b.homingDelay + COMET_HOMING_TICKS) {
                Entity t = level.getEntity(b.homingTarget);
                if (t instanceof LivingEntity living && living.isAlive()) {
                    Vec3 aim = (living == this.getTarget() ? this.predict(living, 6) : living.position())
                            .add(0.0D, living.getBbHeight() * 0.5D, 0.0D);
                    Vec3 want = aim.subtract(b.pos);
                    if (want.lengthSqr() > 1.0E-4D) {
                        Vec3 cur = b.vel.lengthSqr() > 1.0E-4D ? b.vel.normalize() : want.normalize();
                        b.vel = cur.scale(0.8D).add(want.normalize().scale(0.2D)).normalize().scale(COMET_SPEED);
                    }
                }
            }
        }
        b.pos = b.pos.add(b.vel);
        this.drawTrail(level, b, prev);

        BlockPos bp = BlockPos.containing(b.pos);
        boolean solid = !level.getBlockState(bp).getCollisionShape(level, bp).isEmpty();
        switch (b.kind) {
            case CRESCENT -> {
                for (LivingEntity victim : this.victimsWithin(level, b.pos, b.radius)) {
                    if (b.hit.add(victim.getUUID())) {
                        this.strike(level, victim, b.damage);
                        this.shove(victim, b.pos.subtract(b.vel.scale(2.0D)), 0.7D);
                    }
                }
                if (solid || b.age >= b.maxAge) {
                    level.sendParticles(ParticleTypes.SNOWFLAKE, b.pos.x, b.pos.y, b.pos.z, 16, 0.6D, 0.5D, 0.6D, 0.05D);
                    return true;
                }
            }
            case COMET -> {
                List<LivingEntity> hits = this.victimsWithin(level, b.pos, b.radius);
                if (!hits.isEmpty() || solid || b.age >= b.maxAge) {
                    this.boltImpact(level, b, 2.4D, false);
                    return true;
                }
            }
            case PRESENT -> {
                BlockPos below = BlockPos.containing(b.pos.add(0.0D, -0.8D, 0.0D));
                boolean ground = !level.getBlockState(below).getCollisionShape(level, below).isEmpty();
                if (solid || ground || b.age >= b.maxAge) {
                    this.boltImpact(level, b, b.radius, true);
                    return true;
                }
            }
        }
        return false;
    }

    private void boltImpact(ServerLevel level, Bolt b, double radius, boolean present) {
        Vec3 p = b.pos;
        this.playSound(present ? ModSounds.COLLECTOR_FROST_SHATTER.get() : ModSounds.COLLECTOR_LIGHTNING.get(), 1.6F, present ? 0.8F : 1.4F);
        if (present) {
            GiftMimic.confetti(level, p.add(0.0D, 0.5D, 0.0D), 40);
            level.sendParticles(ParticleTypes.EXPLOSION, p.x, p.y + 0.5D, p.z, 2, 0.6D, 0.2D, 0.6D, 0.0D);
            for (int k = 0; k < 18; k++) {
                double a = Math.PI * 2.0D * k / 18.0D;
                level.sendParticles(ParticleTypes.END_ROD, p.x + Math.cos(a) * radius * 0.8D, p.y + 0.2D, p.z + Math.sin(a) * radius * 0.8D,
                        1, 0.0D, 0.15D, 0.0D, 0.03D);
            }
            if (this.getStage() >= 4 && this.random.nextFloat() < 0.18F && this.activeMinions(level).size() < MAX_MINIONS) {
                this.spawnMimic(level, p, 0.2D, 1.0D); // some presents hatch a hungry mimic
            }
        } else {
            level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 24, 0.4D, 0.4D, 0.4D, 0.14D);
            level.sendParticles(COMET_DUST, p.x, p.y, p.z, 20, 0.6D, 0.6D, 0.6D, 0.1D);
            level.sendParticles(ParticleTypes.SNOWFLAKE, p.x, p.y, p.z, 20, 0.8D, 0.6D, 0.8D, 0.08D);
        }
        for (LivingEntity victim : this.victimsWithin(level, p, radius)) {
            this.strike(level, victim, b.damage);
            this.shove(victim, p, present ? 0.9D : 0.6D);
        }
    }

    private void drawTrail(ServerLevel level, Bolt b, Vec3 prev) {
        Vec3 d = b.pos.subtract(prev);
        double len = d.length();
        if (len < 1.0E-4D) {
            return;
        }
        Vec3 dir = d.scale(1.0D / len);
        int steps = Math.max(1, (int) Math.ceil(len / 0.3D));
        Vec3 side = new Vec3(-dir.z, 0.0D, dir.x);
        if (side.lengthSqr() < 1.0E-4D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        }
        side = side.normalize();
        Vec3 up = dir.cross(side).normalize();
        for (int s = 0; s < steps; s++) {
            Vec3 p = prev.add(d.scale((s + 1.0D) / steps));
            switch (b.kind) {
                case COMET -> {
                    level.sendParticles(COMET_DUST, p.x, p.y, p.z, 1, 0.04D, 0.04D, 0.04D, 0.0D);
                    double ang = b.age * 0.9D + s;
                    Vec3 o = side.scale(Math.cos(ang) * 0.4D).add(up.scale(Math.sin(ang) * 0.4D));
                    level.sendParticles(ParticleTypes.SNOWFLAKE, p.x + o.x, p.y + o.y, p.z + o.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                    if (s == 0) {
                        level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.05D, 0.05D, 0.05D, 0.01D);
                    }
                }
                case CRESCENT -> {
                    for (int j = -4; j <= 4; j++) {
                        double t = j / 4.0D;
                        Vec3 o = side.scale(t * 1.8D).subtract(dir.scale(t * t * 0.9D));
                        level.sendParticles(CRESCENT_DUST, p.x + o.x, p.y + 0.35D + (1.0D - Math.abs(t)) * 0.45D, p.z + o.z,
                                1, 0.0D, 0.0D, 0.0D, 0.0D);
                        if (Math.abs(j) == 4 && s == 0) {
                            level.sendParticles(ParticleTypes.END_ROD, p.x + o.x, p.y + 0.4D, p.z + o.z, 1, 0.0D, 0.0D, 0.0D, 0.01D);
                        }
                    }
                }
                case PRESENT -> {
                    level.sendParticles((b.age + s) % 3 == 0 ? PRESENT_RED : (b.age + s) % 3 == 1 ? PRESENT_GOLD : PRESENT_GREEN,
                            p.x, p.y, p.z, 2, 0.25D, 0.1D, 0.25D, 0.0D);
                    if (s == 0) {
                        level.sendParticles(ParticleTypes.FIREWORK, p.x, p.y, p.z, 1, 0.15D, 0.1D, 0.15D, 0.01D);
                    }
                }
            }
        }
    }

    private void enterRecovery(ServerLevel level, int durationTicks) {
        this.setActiveAttack(AttackType.NONE);
        if (this.getStage() >= MAX_STAGE) {
            durationTicks = durationTicks / 2;
        } else if (this.getStage() >= 5) {
            durationTicks = durationTicks * 3 / 5;
        }
        // Combo: the harder the stage, the likelier he skips most of his recovery and chains another attack.
        if (this.getTarget() != null && this.random.nextFloat() < Math.min(0.85F, 0.15F + 0.12F * (this.getStage() - 1))) {
            this.chainNext = true;
            durationTicks = Math.min(durationTicks, 8);
        }
        this.actionTimer = 0;
        this.recoveryTicks = durationTicks;
        this.setBossState(BossState.RECOVERY);
        if (!this.isPhaseTwo()) {
            this.setCageOpen(false);
        }
        this.getNavigation().stop();
        level.broadcastEntityEvent(this, EVENT_RECOVERY);
    }

    /** Primary combat AI goal driving Phase I, Phase II, Attack Preparation, Execution, and Recovery. */
    private final class CollectorCombatBrainGoal extends Goal {
        CollectorCombatBrainGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            BossState state = LastGiftCollector.this.getBossState();
            return LastGiftCollector.this.isAlive()
                    && state != BossState.INTRO_CINEMATIC
                    && state != BossState.PHASE_TRANSITION
                    && state != BossState.DEATH_CINEMATIC
                    && state != BossState.DEAD;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LastGiftCollector boss = LastGiftCollector.this;
            if (!(boss.level() instanceof ServerLevel level)) {
                return;
            }
            LivingEntity target = boss.getTarget();
            if (target != null && (!target.isAlive() || !boss.canAttack(target))) {
                boss.setTarget(null);
                target = null;
            }

            switch (boss.getBossState()) {
                case PHASE_ONE, PHASE_TWO -> this.tickActivePhase(level, boss, target);
                case ATTACK_PREPARATION -> this.tickAttackPreparation(level, boss, target);
                case ATTACK_EXECUTION -> this.tickAttackExecution(level, boss, target);
                case RECOVERY -> this.tickRecovery(level, boss, target);
                default -> {}
            }
        }

        private void tickActivePhase(ServerLevel level, LastGiftCollector boss, @Nullable LivingEntity target) {
            target = boss.chooseTarget(level, target);
            if (target == null) {
                boss.getNavigation().stop();
                return;
            }
            boss.trackTarget(target);
            boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSqr = boss.distanceToSqr(target);
            boss.watchForKiting(level, target, distSqr);

            // Weighted, non-repeating choice (see pickAttack), aware of range, height and kiting.
            boss.chainNext = false;
            AttackType pick = boss.pickAttack(target);
            if (pick != AttackType.NONE && boss.startAttack(pick, target, boss.rollJitter())) {
                boss.farTicks = 0;
                return;
            }
            if (boss.dashCooldown <= 0 && distSqr >= 100.0D && distSqr <= 900.0D && boss.onGround()) {
                boss.dashToward(level, target);
            }
            boss.moveTactically(level, target, distSqr);
        }

        private void tickAttackPreparation(ServerLevel level, LastGiftCollector boss, @Nullable LivingEntity target) {
            boss.getNavigation().stop();
            boss.actionTimer++;
            AttackType attack = boss.getActiveAttack();

            if (attack == AttackType.PAWN_PROCESSION && boss.actionTimer % 4 == 0) {
                // Draw visible ground warning rings at the 3 deterministic root zones before eruption.
                for (Vec3 zone : boss.activePawnZones) {
                    for (int i = 0; i < 10; i++) {
                        double angle = (Math.PI * 2.0D * i) / 10.0D;
                        level.sendParticles(WINE_PLEDGE_DUST,
                                zone.x + Math.cos(angle) * PAWN_ZONE_RADIUS,
                                zone.y + 0.12D,
                                zone.z + Math.sin(angle) * PAWN_ZONE_RADIUS,
                                1, 0.04D, 0.02D, 0.04D, 0.0D);
                    }
                }
            } else if (attack == AttackType.STOMP && boss.actionTimer % 3 == 0) {
                for (int i = 0; i < 18; i++) {
                    double angle = (Math.PI * 2.0D * i) / 18.0D;
                    level.sendParticles(WINE_PLEDGE_DUST, boss.getX() + Math.cos(angle) * STOMP_RADIUS, boss.getY() + 0.12D,
                            boss.getZ() + Math.sin(angle) * STOMP_RADIUS, 1, 0.03D, 0.02D, 0.03D, 0.0D);
                }
            } else if (attack == AttackType.WHIRLWIND && boss.actionTimer % 3 == 0) {
                level.sendParticles(ParticleTypes.SNOWFLAKE, boss.getX(), boss.getY() + 1.0D, boss.getZ(), 10, 1.6D, 0.8D, 1.6D, 0.08D);
            } else if (attack == AttackType.FROST_CONE && boss.actionTimer % 3 == 0) {
                Vec3 d = Vec3.directionFromRotation(0.0F, boss.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
                Vec3 side = new Vec3(-d.z, 0.0D, d.x);
                for (int k = 1; k <= 5; k++) {
                    double reach = CONE_LENGTH * k / 5.0D;
                    for (int sgn = -1; sgn <= 1; sgn += 2) {
                        Vec3 p = boss.position().add(d.scale(reach)).add(side.scale(sgn * reach * 0.6D * 0.75D));
                        level.sendParticles(SPECTRAL_FROST_DUST, p.x, p.y + 0.15D, p.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                    }
                }
            } else if (attack == AttackType.HYPNOTIC_GAZE && boss.actionTimer % 2 == 0) {
                double a = boss.actionTimer * 0.45D;
                for (int k = 0; k < 3; k++) {
                    double ang = a + k * Math.PI * 2.0D / 3.0D;
                    level.sendParticles(ParticleTypes.END_ROD, boss.getX() + Math.cos(ang) * 0.9D, boss.getEyeY() + 0.1D,
                            boss.getZ() + Math.sin(ang) * 0.9D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            } else if (attack == AttackType.FEY_SUMMON && boss.actionTimer % 4 == 0) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, boss.getX(), boss.getY() + 0.2D, boss.getZ(), 14, 3.0D, 0.1D, 3.0D, 0.02D);
            } else if (attack == AttackType.CRESCENT_WAVE && boss.actionTimer % 2 == 0) {
                Vec3 d = Vec3.directionFromRotation(0.0F, boss.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
                for (int k = 1; k <= 7; k++) {
                    Vec3 p = boss.position().add(d.scale(k * 2.2D));
                    level.sendParticles(CRESCENT_DUST, p.x, p.y + 0.15D, p.z, 1, 0.5D, 0.0D, 0.5D, 0.0D);
                }
            } else if ((attack == AttackType.COMET_BARRAGE || attack == AttackType.GIFT_RAIN) && boss.actionTimer % 2 == 0) {
                double a = boss.actionTimer * 0.5D;
                for (int k = 0; k < 4; k++) {
                    double ang = a + k * Math.PI / 2.0D;
                    double rise = 4.2D + Math.sin(boss.actionTimer * 0.3D + k) * 0.5D;
                    level.sendParticles(attack == AttackType.GIFT_RAIN ? (k % 2 == 0 ? PRESENT_RED : PRESENT_GOLD) : COMET_DUST,
                            boss.getX() + Math.cos(ang) * 1.4D, boss.getY() + rise, boss.getZ() + Math.sin(ang) * 1.4D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                    level.sendParticles(ParticleTypes.END_ROD, boss.getX() + Math.cos(ang) * 0.7D, boss.getY() + rise + 0.3D,
                            boss.getZ() + Math.sin(ang) * 0.7D, 1, 0.0D, 0.02D, 0.0D, 0.0D);
                }
            } else if (attack == AttackType.SOLSTICE_JUMP && boss.actionTimer % 4 == 0) {
                level.sendParticles(SPECTRAL_FROST_DUST,
                        boss.solsticeTargetPos.x, boss.solsticeTargetPos.y + 0.15D, boss.solsticeTargetPos.z,
                        8, 1.1D, 0.05D, 1.1D, 0.01D);
            }

            int prepDuration = switch (attack) {
                case WINTER_SCYTHE -> SCYTHE_PREP_TICKS;
                case HOLLOW_BELL -> HOLLOW_BELL_PREP_TICKS;
                case PAWN_PROCESSION -> PAWN_PREP_TICKS;
                case SOLSTICE_JUMP -> SOLSTICE_PREP_TICKS;
                case WHIRLWIND -> WHIRL_PREP_TICKS;
                case STOMP -> STOMP_PREP_TICKS;
                case FROST_CONE -> CONE_PREP_TICKS;
                case HYPNOTIC_GAZE -> GAZE_PREP_TICKS;
                case FEY_SUMMON -> SUMMON_PREP_TICKS;
                case CRESCENT_WAVE -> CRESCENT_PREP_TICKS;
                case COMET_BARRAGE -> COMET_PREP_TICKS;
                case GIFT_RAIN -> RAIN_PREP_TICKS;
                case NONE -> 1;
            };
            if (boss.getStage() >= MAX_STAGE) {
                prepDuration = prepDuration * 3 / 4; // Rage: wind-ups come faster
            }
            prepDuration = Math.max(6, prepDuration + boss.prepJitter);

            // Wind-up of a melee/ground attack: keep facing the target and keep closing in, until the last
            // few ticks, when the blow is committed.
            boolean trackedAttack = attack == AttackType.WINTER_SCYTHE || attack == AttackType.STOMP
                    || attack == AttackType.WHIRLWIND || attack == AttackType.FROST_CONE
                    || attack == AttackType.CRESCENT_WAVE || attack == AttackType.COMET_BARRAGE;
            if (target != null && trackedAttack && boss.actionTimer < prepDuration - 4) {
                boss.faceTarget(target);
                boolean closeIn = attack == AttackType.WINTER_SCYTHE || attack == AttackType.STOMP || attack == AttackType.WHIRLWIND;
                if (closeIn && boss.onGround() && !boss.isWithinScytheBox(target, 1.6D)) {
                    Vec3 step = target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
                    if (step.lengthSqr() > 1.0E-4D) {
                        step = step.normalize().scale(boss.getStage() >= 5 ? 0.2D : 0.15D);
                        boss.setDeltaMovement(step.x, boss.getDeltaMovement().y, step.z);
                    }
                }
            }

            if (boss.actionTimer >= prepDuration && !boss.attackStillUseful(attack, target)) {
                boss.abortAttack(level, attack);
                return;
            }
            if (boss.actionTimer >= prepDuration) {
                boss.actionTimer = 0;
                boss.setBossState(BossState.ATTACK_EXECUTION);
                if (attack == AttackType.WINTER_SCYTHE) {
                    level.broadcastEntityEvent(boss, EVENT_ATTACK_SCYTHE_EXEC);
                } else if (attack == AttackType.HOLLOW_BELL) {
                    level.broadcastEntityEvent(boss, EVENT_ATTACK_BELL_IMPACT);
                    boss.playSound(ModSounds.COLLECTOR_FROST_SHATTER.get(), 1.4F, 0.70F);
                } else if (attack == AttackType.WHIRLWIND) {
                    level.broadcastEntityEvent(boss, EVENT_ATTACK_SPIN);
                } else if (attack == AttackType.CRESCENT_WAVE) {
                    level.broadcastEntityEvent(boss, EVENT_ATTACK_SCYTHE_EXEC);
                } else if (attack == AttackType.STOMP) {
                    boss.setDeltaMovement(0.0D, 0.42D, 0.0D);
                }
            }
        }

        private void tickAttackExecution(ServerLevel level, LastGiftCollector boss, @Nullable LivingEntity target) {
            boss.getNavigation().stop();
            boss.actionTimer++;
            AttackType attack = boss.getActiveAttack();

            switch (attack) {
                case WINTER_SCYTHE -> {
                    if (boss.actionTimer == 1 && target != null && !boss.isWithinScytheBox(target, SCYTHE_STRIKE_REACH - 0.8D)) {
                        boss.lungeToward(target, 0.85D); // the target backed off during the wind-up: lunge after it
                    }
                    if (boss.actionTimer == 3) {
                        boss.executeWinterScytheSweep(level);
                    }
                    if (boss.isPhaseTwo() && boss.actionTimer == 15) {
                        if (target != null) {
                            boss.getLookControl().setLookAt(target, 35.0F, 35.0F);
                        }
                        boss.executeWinterScytheSweep(level);
                    }
                    int execDuration = boss.isPhaseTwo() ? SCYTHE_EXEC_TICKS_P2 : SCYTHE_EXEC_TICKS_P1;
                    if (boss.actionTimer >= execDuration) {
                        boss.scytheCooldown = boss.isPhaseTwo() ? 18 : 26;
                        boss.enterRecovery(level, boss.isPhaseTwo() ? SCYTHE_RECOVERY_TICKS_P2 : SCYTHE_RECOVERY_TICKS_P1);
                    }
                }
                case HOLLOW_BELL -> {
                    boss.stepHollowBellWaves(level, boss.actionTimer);
                    if (boss.actionTimer >= HOLLOW_BELL_EXEC_TICKS) {
                        boss.hollowBellCooldown = HOLLOW_BELL_COOLDOWN_TICKS;
                        boss.activeFrostWaveDirs.clear();
                        boss.frostWaveHitEntities.clear();
                        boss.enterRecovery(level, HOLLOW_BELL_RECOVERY_TICKS);
                    }
                }
                case PAWN_PROCESSION -> {
                    if (boss.actionTimer == 2) {
                        boss.eruptPawnProcessionRoots(level);
                    }
                    if (boss.actionTimer >= PAWN_EXEC_TICKS) {
                        boss.pawnCooldown = PAWN_COOLDOWN_TICKS;
                        boss.activePawnZones.clear();
                        boss.enterRecovery(level, PAWN_RECOVERY_TICKS);
                    }
                }
                case SOLSTICE_JUMP -> {
                    // Controlled parabolic leap — zero damage during flight, damage only at touchdown (tick 20).
                    float t = Mth.clamp(boss.actionTimer / (float) SOLSTICE_EXEC_TICKS, 0.0F, 1.0F);
                    double x = Mth.lerp(t, boss.solsticeStartPos.x, boss.solsticeTargetPos.x);
                    double z = Mth.lerp(t, boss.solsticeStartPos.z, boss.solsticeTargetPos.z);
                    double arc = Math.sin(t * Math.PI) * 4.2D;
                    double y = Mth.lerp(t, boss.solsticeStartPos.y, boss.solsticeTargetPos.y) + arc;
                    boss.setPos(x, y, z);
                    level.sendParticles(ParticleTypes.SNOWFLAKE, x, y + 1.0D, z, 4, 0.5D, 0.4D, 0.5D, 0.02D);

                    if (boss.actionTimer >= SOLSTICE_EXEC_TICKS) {
                        boss.setPos(boss.solsticeTargetPos.x, boss.solsticeTargetPos.y, boss.solsticeTargetPos.z);
                        boss.setDeltaMovement(Vec3.ZERO);
                        boss.executeSolsticeLandingImpact(level);
                        boss.solsticeJumpCooldown = SOLSTICE_COOLDOWN_TICKS;
                        boss.enterRecovery(level, SOLSTICE_RECOVERY_TICKS);
                    }
                }
                case WHIRLWIND -> {
                    boss.executeWhirlwind(level, boss.actionTimer, target);
                    if (boss.actionTimer >= WHIRL_EXEC_TICKS) {
                        boss.cooldowns[AttackType.WHIRLWIND.ordinal()] = WHIRL_COOLDOWN_TICKS;
                        boss.enterRecovery(level, WHIRL_RECOVERY_TICKS);
                    }
                }
                case STOMP -> {
                    boss.executeStomp(level, boss.actionTimer);
                    if (boss.actionTimer >= STOMP_EXEC_TICKS) {
                        boss.cooldowns[AttackType.STOMP.ordinal()] = STOMP_COOLDOWN_TICKS;
                        boss.enterRecovery(level, STOMP_RECOVERY_TICKS);
                    }
                }
                case FROST_CONE -> {
                    boss.executeFrostCone(level, boss.actionTimer);
                    if (boss.actionTimer >= CONE_EXEC_TICKS) {
                        boss.cooldowns[AttackType.FROST_CONE.ordinal()] = CONE_COOLDOWN_TICKS;
                        boss.enterRecovery(level, CONE_RECOVERY_TICKS);
                    }
                }
                case HYPNOTIC_GAZE -> {
                    if (boss.actionTimer == 2) {
                        boss.executeHypnoticGaze(level);
                    }
                    if (boss.actionTimer >= GAZE_EXEC_TICKS) {
                        boss.cooldowns[AttackType.HYPNOTIC_GAZE.ordinal()] = GAZE_COOLDOWN_TICKS;
                        boss.enterRecovery(level, GAZE_RECOVERY_TICKS);
                    }
                }
                case FEY_SUMMON -> {
                    if (boss.actionTimer == 2) {
                        boss.axeSummonFlash(level);
                        boss.summonFey(level, boss.getStage() >= 5 ? 3 : 2);
                    }
                    if (boss.actionTimer >= SUMMON_EXEC_TICKS) {
                        boss.cooldowns[AttackType.FEY_SUMMON.ordinal()] = SUMMON_COOLDOWN_TICKS;
                        boss.enterRecovery(level, SUMMON_RECOVERY_TICKS);
                    }
                }
                case CRESCENT_WAVE -> {
                    if (boss.actionTimer == 3) {
                        boss.launchCrescents(level);
                    }
                    if (boss.actionTimer >= CRESCENT_EXEC_TICKS) {
                        boss.cooldowns[AttackType.CRESCENT_WAVE.ordinal()] = CRESCENT_COOLDOWN_TICKS;
                        boss.enterRecovery(level, CRESCENT_RECOVERY_TICKS);
                    }
                }
                case COMET_BARRAGE -> {
                    boss.executeCometBarrage(level, boss.actionTimer, target);
                    if (boss.actionTimer >= COMET_EXEC_TICKS) {
                        boss.cooldowns[AttackType.COMET_BARRAGE.ordinal()] = COMET_COOLDOWN_TICKS;
                        boss.enterRecovery(level, COMET_RECOVERY_TICKS);
                    }
                }
                case GIFT_RAIN -> {
                    if (boss.actionTimer == 1) {
                        boss.planGiftRain(level, target);
                    }
                    if (boss.actionTimer >= RAIN_EXEC_TICKS) {
                        boss.cooldowns[AttackType.GIFT_RAIN.ordinal()] = RAIN_COOLDOWN_TICKS;
                        boss.enterRecovery(level, RAIN_RECOVERY_TICKS);
                    }
                }
                case NONE -> boss.enterRecovery(level, 10);
            }
        }

        private void tickRecovery(ServerLevel level, LastGiftCollector boss, @Nullable LivingEntity target) {
            boss.getNavigation().stop();
            // Legendary reaction: standing next to him while he recovers is punished with a quick stomp.
            if (target != null && boss.legendCooldown <= 0 && boss.getStage() >= 2
                    && boss.distanceToSqr(target) <= (LEGEND_RADIUS - 0.4D) * (LEGEND_RADIUS - 0.4D)) {
                boss.legendCooldown = boss.getStage() >= MAX_STAGE ? LEGEND_COOLDOWN_TICKS / 2 : LEGEND_COOLDOWN_TICKS;
                level.broadcastEntityEvent(boss, EVENT_ATTACK_BELL_IMPACT);
                boss.stompBlast(level, LEGEND_RADIUS, LEGEND_DAMAGE, 0.8D);
            }
            if (--boss.recoveryTicks <= 0) {
                boss.setBossState(boss.isPhaseTwo() ? BossState.PHASE_TWO : BossState.PHASE_ONE);
            }
        }
    }

    // ---------- Client Animation Controller (Section 12) ----------

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_INTRO_START, EVENT_INTRO_LIGHTNING -> {
                this.stopAllActionAnimations();
                this.appearAnimationState.start(this.tickCount);
            }
            case EVENT_INTRO_JUMP -> {
                this.stopAllActionAnimations();
                this.jumpFromSleighAnimationState.start(this.tickCount);
            }
            case EVENT_INTRO_FALL -> {
                this.stopAllActionAnimations();
                this.fallAnimationState.start(this.tickCount);
            }
            case EVENT_INTRO_LAND -> {
                this.stopAllActionAnimations();
                this.landAnimationState.start(this.tickCount);
            }
            case EVENT_INTRO_INTIMIDATE -> {
                this.stopAllActionAnimations();
                this.intimidateAnimationState.start(this.tickCount);
            }
            case EVENT_INTRO_COMPLETE -> this.stopAllActionAnimations();
            case EVENT_ATTACK_SCYTHE_PREP -> {
                this.stopAllActionAnimations();
                this.attackScythePrepAnimationState.start(this.tickCount);
            }
            case EVENT_ATTACK_SCYTHE_EXEC -> {
                this.attackScythePrepAnimationState.stop();
                this.attackScytheExecAnimationState.start(this.tickCount);
            }
            case EVENT_ATTACK_BELL_PREP -> {
                this.stopAllActionAnimations();
                this.attackHollowBellPrepAnimationState.start(this.tickCount);
            }
            case EVENT_ATTACK_BELL_IMPACT -> {
                this.attackHollowBellPrepAnimationState.stop();
                this.attackHollowBellImpactAnimationState.start(this.tickCount);
            }
            case EVENT_ATTACK_PAWNS -> {
                this.stopAllActionAnimations();
                this.attackPawnsAnimationState.start(this.tickCount);
            }
            case EVENT_ATTACK_SOLSTICE_JUMP -> {
                this.stopAllActionAnimations();
                this.attackSolsticeJumpAnimationState.start(this.tickCount);
            }
            case EVENT_RECOVERY -> {
                this.stopAllActionAnimations();
                this.recoveryAnimationState.start(this.tickCount);
            }
            case EVENT_PHASE_TRANSITION -> {
                this.stopAllActionAnimations();
                this.phaseTransitionAnimationState.start(this.tickCount);
            }
            case EVENT_ATTACK_CAST -> {
                this.stopAllActionAnimations();
                this.attackCastAnimationState.start(this.tickCount);
            }
            case EVENT_ATTACK_SPIN -> {
                this.attackScythePrepAnimationState.stop();
                this.attackSpinAnimationState.start(this.tickCount);
            }
            case EVENT_ATTACK_SLAM -> {
                this.stopAllActionAnimations();
                this.attackSlamAnimationState.start(this.tickCount);
            }
            case EVENT_RAGE -> {
                this.stopAllActionAnimations();
                this.rageAnimationState.start(this.tickCount);
            }
            case EVENT_HURT -> this.hurtAnimationState.start(this.tickCount);
            case EVENT_DEATH_CINEMATIC -> {
                this.stopAllActionAnimations();
                this.hurtAnimationState.stop();
                this.deathAnimationState.start(this.tickCount);
            }
            default -> super.handleEntityEvent(id);
        }
    }

    private void stopAllActionAnimations() {
        this.idleAlternateAnimationState.stop();
        this.appearAnimationState.stop();
        this.jumpFromSleighAnimationState.stop();
        this.fallAnimationState.stop();
        this.landAnimationState.stop();
        this.intimidateAnimationState.stop();
        this.attackScythePrepAnimationState.stop();
        this.attackScytheExecAnimationState.stop();
        this.attackHollowBellPrepAnimationState.stop();
        this.attackHollowBellImpactAnimationState.stop();
        this.attackPawnsAnimationState.stop();
        this.attackSolsticeJumpAnimationState.stop();
        this.attackCastAnimationState.stop();
        this.attackSpinAnimationState.stop();
        this.attackSlamAnimationState.stop();
        this.rageAnimationState.stop();
        this.recoveryAnimationState.stop();
        this.phaseTransitionAnimationState.stop();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.tickClientAnimations();
        }
    }

    private void tickClientAnimations() {
        BossState state = this.getBossState();
        if (state == BossState.DEATH_CINEMATIC || state == BossState.DEAD || this.isDeadOrDying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.deathAnimationState.startIfStopped(this.tickCount);
            return;
        }

        this.stopWhenOver(this.hurtAnimationState, 10);
        this.stopWhenOver(this.recoveryAnimationState, 30);
        this.stopWhenOver(this.phaseTransitionAnimationState, PHASE_TRANSITION_TICKS);
        this.stopWhenOver(this.rageAnimationState, PHASE_TRANSITION_TICKS);

        boolean moving = this.walkAnimation.speed() > 0.04F
                && (state == BossState.PHASE_ONE || state == BossState.PHASE_TWO);
        if (moving) {
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
        }

        this.idleAnimationState.startIfStopped(this.tickCount);

        if ((state == BossState.PHASE_ONE || state == BossState.PHASE_TWO) && !moving && --this.idleAlternateCooldown <= 0) {
            this.idleAlternateAnimationState.start(this.tickCount);
            this.idleAlternateCooldown = 120 + this.random.nextInt(100);
        }
        this.stopWhenOver(this.idleAlternateAnimationState, 40);
    }

    private void stopWhenOver(AnimationState state, int maxTicks) {
        if (state.isStarted() && state.getTimeInMillis(this.tickCount) > maxTicks * 50L) {
            state.stop();
        }
    }

    // ---------- Sounds (Section 20) ----------

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (this.getBossState() == BossState.INTRO_CINEMATIC
                || this.getBossState() == BossState.DEATH_CINEMATIC
                || this.getBossState() == BossState.DEAD) {
            return null;
        }
        return ModSounds.COLLECTOR_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.COLLECTOR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.COLLECTOR_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState blockState) {
        this.playSound(ModSounds.COLLECTOR_STEP.get(), 0.9F, this.isPhaseTwo() ? 0.85F : 0.72F);
    }

    // ---------- Persistence (Section 10) ----------

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("ArenaX", this.arenaCenter.getX());
        output.putInt("ArenaY", this.arenaCenter.getY());
        output.putInt("ArenaZ", this.arenaCenter.getZ());
        output.putBoolean("PhaseTwo", this.isPhaseTwo());
        output.putInt("Stage", this.getStage());
        output.putBoolean("LandedOnce", this.landedOnce);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        int ax = input.getIntOr("ArenaX", this.blockPosition().getX());
        int ay = input.getIntOr("ArenaY", this.blockPosition().getY());
        int az = input.getIntOr("ArenaZ", this.blockPosition().getZ());
        this.setArenaCenter(new BlockPos(ax, ay, az));
        boolean p2 = input.getBooleanOr("PhaseTwo", false);
        this.landedOnce = input.getBooleanOr("LandedOnce", true);
        int savedStage = Mth.clamp(input.getIntOr("Stage", p2 ? 3 : 1), 1, MAX_STAGE);
        this.applyStage(savedStage);
        p2 = savedStage >= 3;
        this.setEyesGlowing(true);
        this.setIntroStage(IntroStage.INTRO_COMPLETE);
        this.entityData.set(DATA_INTRO_TICKS, INTRO_TOTAL_TICKS);
        this.setBossState(p2 ? BossState.PHASE_TWO : BossState.PHASE_ONE);
        this.bossEvent.setVisible(true);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }
}
