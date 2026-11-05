package com.codenamecoraline.entity.ten_threads_witch;

import com.codenamecoraline.entity.crimson.CrimsonCommander;
import com.codenamecoraline.entity.ten_threads_witch.AttackRunner.Phase;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatAnim;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatSlot;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchAnimDirector;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchAnimSync;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchMood;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchMoveset;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchPresence;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Witch of Ten Threads (the Matriarch): a tiny, frail old seamstress whose mind lives in ten puppets. She runs the
 * whole encounter: the intro, ten stages in which exactly one puppet fights (she cannot be hurt while any thread holds),
 * the anti-cheese threads (pulling down whoever climbs out of reach, hoisting a puppet that cannot reach its target), the
 * 28-block arena, the reset when everyone leaves, the collapse into the last phase where she fights herself, and the death
 * that leaves the Seamstress Casket. The server owns everything; clients play the synched phase, stage and animation.
 */
public class MatriarchWitchEntity extends Monster implements TenThreadsEncounterMember, AttackRunner.Host {
    public static final double MAX_HEALTH = 150.0D;
    public static final double BASE_SPEED = 0.28D;
    public static final double FURY_SPEED = 0.40D;
    public static final double BASE_DAMAGE = 10.0D;
    public static final int XP_REWARD = 700;
    public static final float WIDTH = 0.6F;
    public static final float HEIGHT = 1.15F;
    public static final float EYE_HEIGHT = 0.95F;

    /** The entrance (MAT-CIN-01, 02 and 03 in a row); the fight starts only when it is over. */
    public static final int INTRO_TICKS = MatAnim.CIN_01.ticks + MatAnim.CIN_02.ticks + MatAnim.CIN_03.ticks;
    public static final int REACT_TICKS = 20;
    public static final int SUMMON_TICKS = 25;
    /** The first change of stage (MAT-PHASE-01), between the fifth and the sixth puppet. */
    public static final int PHASE_ONE_TICKS = MatAnim.PHASE_01.ticks;
    /** The collapse into the last phase (MAT-PHASE-02, 03, 05 and 04 in a row). */
    public static final int COLLAPSE_TICKS = MatAnim.PHASE_02.ticks + MatAnim.PHASE_03.ticks + MatAnim.PHASE_05.ticks + MatAnim.PHASE_04.ticks;
    /** The death (MAT-CIN-05 then MAT-CIN-06): she is removed only when the last fragment has vanished. */
    public static final int DEATH_TICKS = MatAnim.CIN_05.ticks + MatAnim.CIN_06.ticks;
    /** The breath between sequences of blows in the last phase: long while she still holds herself, short once she unravels. */
    public static final int FURY_REST_CALM = 46;
    public static final int FURY_REST_WILD = 32;
    public static final double ARENA_RADIUS = 28.0D;
    public static final float ARENA_DAMAGE = 2.0F;
    public static final int RESET_TICKS = 200;
    public static final double RESET_RANGE = 48.0D;
    public static final double PILLAR_HEIGHT = 4.5D;
    public static final int PILLAR_TICKS = 60;
    public static final int PULL_SLOW_FALLING_TICKS = 25;
    public static final double OTHER_WITCH_RADIUS = 64.0D;

    /** The attacks she fights with in the last phase (and the clips each one plays: {@link MatriarchMoveset}). */
    public static final List<PuppetAttack> FURY_ATTACKS = MatriarchMoveset.FURY_ATTACKS;

    private static final BossEvent.BossBarColor[] STAGE_COLORS = {
            BossEvent.BossBarColor.GREEN, BossEvent.BossBarColor.BLUE, BossEvent.BossBarColor.WHITE, BossEvent.BossBarColor.PINK,
            BossEvent.BossBarColor.YELLOW, BossEvent.BossBarColor.PURPLE, BossEvent.BossBarColor.GREEN, BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarColor.WHITE, BossEvent.BossBarColor.PINK};

    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(MatriarchWitchEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STAGE =
            SynchedEntityData.defineId(MatriarchWitchEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BROKEN =
            SynchedEntityData.defineId(MatriarchWitchEntity.class, EntityDataSerializers.INT);
    /** The five synched animation slots (body, reaction, left hand, right hand, face): see {@link MatSlot}. */
    private static final List<EntityDataAccessor<Integer>> DATA_SLOTS = List.of(
            SynchedEntityData.defineId(MatriarchWitchEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(MatriarchWitchEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(MatriarchWitchEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(MatriarchWitchEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(MatriarchWitchEntity.class, EntityDataSerializers.INT));
    /** Gait style (bits 0-3) and the floating flag (bit 4). */
    private static final EntityDataAccessor<Integer> DATA_GAIT =
            SynchedEntityData.defineId(MatriarchWitchEntity.class, EntityDataSerializers.INT);
    /** Entity she looks at (id, -1 none) and why (ordinal of {@link MatriarchAnimDirector.LookMode}). */
    private static final EntityDataAccessor<Integer> DATA_LOOK =
            SynchedEntityData.defineId(MatriarchWitchEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LOOK_MODE =
            SynchedEntityData.defineId(MatriarchWitchEntity.class, EntityDataSerializers.INT);
    /** The entity her threads lead to: the active puppet, or her target in the last phase (-1 none). */
    private static final EntityDataAccessor<Integer> DATA_FOCUS =
            SynchedEntityData.defineId(MatriarchWitchEntity.class, EntityDataSerializers.INT);

    /** Client clocks of the animation slots (filled from the synched data; read by the client animator). */
    public final MatriarchAnimSync animSync = new MatriarchAnimSync();
    private final MatriarchAnimDirector director = new MatriarchAnimDirector(this);
    private final MatriarchPresence presence = new MatriarchPresence(this);
    private @Nullable PuppetAttack pendingCounter;
    private int counterDelay = 0;
    private @Nullable String comboFrom;
    private int comboWindow = 0;
    private int seenCancelled = 0;
    /** Ticks of breath she takes after a sequence of blows (a single attack or a whole combo) before she picks the next. */
    private int furyRest = 0;
    private boolean restPending = false;

    private final ServerBossEvent bossEvent = new ServerBossEvent(UUID.randomUUID(),
            Component.translatable("entity.codenamecoraline.ten_threads_witch"), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
    private final AttackRunner runner = new AttackRunner(this, FURY_ATTACKS, this);
    private final UUID[] puppetIds = new UUID[10];
    private final boolean[] puppetKnown = new boolean[10];
    private final java.util.Set<UUID> engaged = new java.util.HashSet<>();
    private List<ArenaAnchorManager.Anchor> anchors = new ArrayList<>();
    private Vec3 center = Vec3.ZERO;
    private int phaseTimer = 0;
    private int waitNext = 0;
    private int pillarTicks = 0;
    private int pullCooldown = 0;
    private int noPlayerTicks = 0;
    private int tangledTicks = 0;
    private int ticksSinceLine = 1000;
    private int deathTicks = 0;
    private int resetTicks = 0;
    private int arenaHitCooldown = 0;
    private boolean rewardsGranted = false;
    private boolean casketSpawned = false;
    private int casketCount = 0;
    private int deathAwardCount = 0;
    private int pulls = 0;
    private int resets = 0;
    private int arenaPushes = 0;
    private int arenaHits = 0;
    private WitchDialogueManager.@Nullable Line lastLine;
    private boolean needsRebind = false;
    private boolean restoreAnimation = false;

    public MatriarchWitchEntity(EntityType<? extends MatriarchWitchEntity> type, Level level) {
        super(type, level);
        this.xpReward = XP_REWARD;
        this.bossEvent.setVisible(false);
        this.bossEvent.setDarkenScreen(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, BASE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE, EncounterPhase.DORMANT.ordinal());
        builder.define(DATA_STAGE, 0);
        builder.define(DATA_BROKEN, 0);
        for (EntityDataAccessor<Integer> slot : DATA_SLOTS) {
            builder.define(slot, 0);
        }
        builder.define(DATA_GAIT, 0);
        builder.define(DATA_LOOK, -1);
        builder.define(DATA_LOOK_MODE, 0);
        builder.define(DATA_FOCUS, -1);
    }

    // ---------------------------------------------------------------- accessors
    public EncounterPhase getPhase() {
        return EncounterPhase.byOrdinal(this.entityData.get(DATA_PHASE));
    }

    private void setPhase(EncounterPhase phase) {
        this.entityData.set(DATA_PHASE, phase.ordinal());
        this.phaseTimer = 0;
    }

    /** The current stage: 1 to 10 for a puppet, 11 for the last phase, 0 before the fight. */
    public int getCurrentStage() {
        return this.entityData.get(DATA_STAGE);
    }

    /** Bitmask of the spools of the staff that have already burst (bit {@code n-1} = puppet {@code n} is defeated). */
    public int getBrokenSpoolsBitmask() {
        return this.entityData.get(DATA_BROKEN);
    }

    // ---------------------------------------------------------------- animation (docs/design/matriarch_animation.md)
    public MatriarchAnimDirector getDirector() {
        return this.director;
    }

    public MatriarchPresence getPresence() {
        return this.presence;
    }

    /** How far she has lost control, 0 to 1 (drives the three behavioural stages of her animations). */
    public float getUnravel() {
        return MatriarchMood.unravel(this.getPhase(), this.getCurrentStage(), this.getMaxHealth() > 0.0F ? this.getHealth() / this.getMaxHealth() : 0.0F);
    }

    public void writeAnimSlot(int slot, int packed) {
        this.entityData.set(DATA_SLOTS.get(slot), packed);
    }

    public int readAnimSlot(int slot) {
        return this.entityData.get(DATA_SLOTS.get(slot));
    }

    public void setAnimGait(int gait) {
        this.entityData.set(DATA_GAIT, gait);
    }

    public int getAnimGait() {
        return this.entityData.get(DATA_GAIT);
    }

    public boolean isAnimFloating() {
        return (this.getAnimGait() & 16) != 0;
    }

    public void setAnimLook(int entityId, int mode) {
        this.entityData.set(DATA_LOOK, entityId);
        this.entityData.set(DATA_LOOK_MODE, mode);
    }

    public int getAnimLookId() {
        return this.entityData.get(DATA_LOOK);
    }

    public int getAnimLookMode() {
        return this.entityData.get(DATA_LOOK_MODE);
    }

    public int getAnimFocusId() {
        return this.entityData.get(DATA_FOCUS);
    }

    /** The entity her threads lead to (server side): the active puppet, or her target in the last phase. */
    public @Nullable Entity getAnimFocus(ServerLevel level) {
        PuppetEntity p = this.getActivePuppet(level);
        return p != null ? p : this.getTarget();
    }

    public ServerBossEvent getBossEvent() {
        return this.bossEvent;
    }

    public Vec3 getCenter() {
        return this.center;
    }

    public List<ArenaAnchorManager.Anchor> getAnchors() {
        return this.anchors;
    }

    public AttackRunner getRunner() {
        return this.runner;
    }

    public int getTicksSinceLastLine() {
        return this.ticksSinceLine;
    }

    public WitchDialogueManager.@Nullable Line getLastLine() {
        return this.lastLine;
    }

    void noteLine(WitchDialogueManager.Line line) {
        this.ticksSinceLine = 0;
        this.lastLine = line;
    }

    public int getCasketCount() {
        return this.casketCount;
    }

    public int getDeathAwardCount() {
        return this.deathAwardCount;
    }

    public int getPulls() {
        return this.pulls;
    }

    public int getResets() {
        return this.resets;
    }

    public int getArenaPushes() {
        return this.arenaPushes;
    }

    /** Times the edge of the arena hurt someone leaving it; for tests. */
    public int getArenaHits() {
        return this.arenaHits;
    }

    public boolean isTangled() {
        return this.tangledTicks > 0;
    }

    @Override
    public @Nullable UUID getOwnerWitchUuid() {
        return null;
    }

    /** Where the threads leave her staff wheel: over her raised hand, in front of her. */
    public Vec3 getThreadAnchor() {
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        return this.position().add(f.x * 0.35D, 1.75D, f.z * 0.35D);
    }

    // ---------------------------------------------------------------- client
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (!this.level().isClientSide()) {
            return;
        }
        int slot = DATA_SLOTS.indexOf(accessor);
        if (slot >= 0) {
            this.animSync.receive(slot, this.entityData.get(DATA_SLOTS.get(slot)), this.tickCount);
        } else if (DATA_GAIT.equals(accessor)) {
            this.animSync.receiveGait(this.animSync.prevGait, this.tickCount);
        }
    }

    // ---------------------------------------------------------------- immunities
    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL)) {
            return true; // leaves and tangled branches never smother her; falls never hurt her
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return AttackRunner.isFoe(this, target) && super.canAttack(target);
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

    // ---------------------------------------------------------------- the encounter
    /** Starts the whole encounter here: scans the arena, hangs the ten puppets and plays the intro. */
    public void startEncounter(ServerLevel level) {
        this.prepareArena(level);
        this.setPhase(EncounterPhase.INTRO);
        this.entityData.set(DATA_STAGE, 0);
        this.director.onIntro();
        this.bossEvent.setVisible(true);
        this.bossEvent.setProgress(1.0F);
        this.playSound(ModSounds.WITCH_THREAD_TENSION.get(), 2.0F, 0.7F);
        WitchDialogueManager.say(level, this, WitchDialogueManager.Line.INTRO);
    }

    /** Starts the fight straight at {@code stage}, with no intro (tests, and the Matriarch summoned mid-fight). */
    public void beginDirect(ServerLevel level, int stage) {
        this.prepareArena(level);
        this.bossEvent.setVisible(true);
        if (stage >= 11) {
            this.entityData.set(DATA_BROKEN, 0x3FF);
            this.beginFury(level);
        } else {
            this.entityData.set(DATA_BROKEN, (1 << (stage - 1)) - 1);
            this.startStage(level, stage);
        }
    }

    private void prepareArena(ServerLevel level) {
        this.center = this.position();
        this.anchors = ArenaAnchorManager.scan(level, this.blockPosition());
        for (int i = 0; i < 10; i++) {
            this.ensurePuppet(level, i + 1);
        }
    }

    /** The puppet of {@code stage}: the living one, or (after a reload that lost it) a new one hanging in its place. */
    public PuppetEntity ensurePuppet(ServerLevel level, int stage) {
        int i = stage - 1;
        if (this.puppetKnown[i] && level.getEntity(this.puppetIds[i]) instanceof PuppetEntity p && p.isAlive()) {
            return p;
        }
        PuppetKind kind = PuppetKind.forStage(stage);
        PuppetEntity p = ModEntities.puppet(kind).create(level, EntitySpawnReason.TRIGGERED);
        Vec3 spot = ArenaAnchorManager.hangingSpot(this.center, i);
        p.setPos(spot.x, spot.y, spot.z);
        p.bindToWitch(this);
        p.suspend();
        level.addFreshEntity(p);
        this.puppetIds[i] = p.getUUID();
        this.puppetKnown[i] = true;
        return p;
    }

    public @Nullable PuppetEntity getActivePuppet(ServerLevel level) {
        int stage = this.getCurrentStage();
        if (stage < 1 || stage > 10 || !this.puppetKnown[stage - 1]) {
            return null;
        }
        return level.getEntity(this.puppetIds[stage - 1]) instanceof PuppetEntity p && p.isAlive() ? p : null;
    }

    public List<PuppetEntity> getPuppets(ServerLevel level) {
        List<PuppetEntity> out = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (this.puppetKnown[i] && level.getEntity(this.puppetIds[i]) instanceof PuppetEntity p && p.isAlive()) {
                out.add(p);
            }
        }
        return out;
    }

    private void startStage(ServerLevel level, int stage) {
        this.entityData.set(DATA_STAGE, stage);
        this.setPhase(EncounterPhase.ofStage(stage));
        this.waitNext = 0;
        this.pillarTicks = 0;
        PuppetEntity p = this.ensurePuppet(level, stage);
        p.awaken(level, this.awakenSpot(level, p.getKind()));
        if (stage >= 6 && !this.presence.isFloating()) {
            this.presence.setFloating(true); // irritated, she no longer touches the ground (MAT-FLOAT)
        }
        this.director.onStageStart(stage, p);
        this.updateBar(level);
        WitchDialogueManager.say(level, this, WitchDialogueManager.Line.forStage(stage));
    }

    private Vec3 awakenSpot(ServerLevel level, PuppetKind kind) {
        Player target = level.getNearestPlayer(this, 40.0D);
        Vec3 dir = target != null ? target.position().subtract(this.center).multiply(1.0D, 0.0D, 1.0D) : new Vec3(1.0D, 0.0D, 0.0D);
        dir = dir.lengthSqr() > 1.0E-4D ? dir.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 around = this.center.add(dir.scale(7.0D));
        Vec3 spot = CrimsonCommander.findStandSpot(level, around, 5.0D, kind.width, kind.height);
        if (spot == null) {
            spot = this.center.add(2.5D, 0.0D, 0.0D);
        }
        return kind.flies ? spot.add(0.0D, 1.5D, 0.0D) : spot;
    }

    /** A puppet was defeated: its spool bursts, and after a beat of reaction the next one is pulled down. */
    public void onPuppetDefeated(PuppetEntity puppet) {
        int stage = puppet.getKind().stage();
        this.entityData.set(DATA_BROKEN, this.getBrokenSpoolsBitmask() | puppet.getKind().spoolBit());
        if (stage == this.getCurrentStage() && this.waitNext <= 0) {
            this.waitNext = REACT_TICKS + SUMMON_TICKS + (stage == 5 ? PHASE_ONE_TICKS : 0);
            this.director.onPuppetDefeated(puppet); // WITCH_PUPPET_BROKEN_REACT
            this.playSound(ModSounds.WITCH_THREAD_SNAP.get(), 1.8F, 0.8F);
            if (this.level() instanceof ServerLevel level) {
                level.sendParticles(AttackRunner.THREAD_DUST, puppet.getX(), puppet.getY() + 1.0D, puppet.getZ(), 30, 0.6D, 1.0D, 0.6D, 0.05D);
            }
        }
    }

    private void updateBar(ServerLevel level) {
        int stage = this.getCurrentStage();
        if (stage >= 11) {
            this.bossEvent.setName(Component.translatable("bossbar.codenamecoraline.ten_threads_witch.fury"));
            this.bossEvent.setColor(BossEvent.BossBarColor.RED);
            this.bossEvent.setProgress(Mth.clamp(this.getHealth() / this.getMaxHealth(), 0.0F, 1.0F));
        } else if (stage >= 1) {
            PuppetKind kind = PuppetKind.forStage(stage);
            this.bossEvent.setName(Component.translatable("bossbar.codenamecoraline.ten_threads_witch.stage", stage, Component.translatable(kind.nameKey())));
            this.bossEvent.setColor(STAGE_COLORS[stage - 1]);
            PuppetEntity p = this.getActivePuppet(level);
            this.bossEvent.setProgress(p == null ? (this.waitNext > 0 ? 0.0F : 1.0F) : Mth.clamp(p.getHealth() / p.getMaxHealth(), 0.0F, 1.0F));
        }
    }

    // ---------------------------------------------------------------- damage
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean bypass = source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        if (this.getPhase() != EncounterPhase.STAGE_11_FURY && !bypass) {
            if (this.getPhase().isPuppetStage()) {
                this.director.onCocoonHit(source, this.getActivePuppet(level)); // MAT-DEF-01..04: she wards it off
            }
            return false; // the cocoon of threads: untouchable while any puppet is active or suspended
        }
        if (source.getEntity() instanceof TenThreadsEncounterMember) {
            return false;
        }
        float mult = this.isTangled() ? PuppetEntity.TANGLED_DAMAGE_FACTOR : 1.0F;
        if (!bypass && this.isAlive() && this.getPhase() == EncounterPhase.STAGE_11_FURY) {
            float share = this.director.defend(source, amount * mult); // a parry, crossed arms, a barrier or a dodge
            if (share < 1.0F) {
                this.queueCounter();
            }
            if (share <= 0.0F) {
                return false;
            }
            mult *= share;
        }
        boolean hurt = super.hurtServer(level, source, amount * mult);
        if (hurt && this.isAlive() && this.getPhase() == EncounterPhase.STAGE_11_FURY) {
            this.bossEvent.setProgress(Mth.clamp(this.getHealth() / this.getMaxHealth(), 0.0F, 1.0F));
            if (this.director.onHurt(source, amount * mult)) {
                this.runner.cancel(true);
                this.comboWindow = 0;
            }
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        if (this.rewardsGranted) {
            return;
        }
        this.rewardsGranted = true;
        this.deathAwardCount++;
        this.runner.cancel(false);
        this.tangledTicks = 0;
        this.setPhase(EncounterPhase.DEATH_CINEMATIC);
        this.director.onDeath();
        this.presence.setFloating(false);
        this.bossEvent.setProgress(0.0F);
        this.getNavigation().stop();
        if (this.level() instanceof ServerLevel level) {
            WitchDialogueManager.say(level, this, WitchDialogueManager.Line.DEATH);
            this.discardPuppets(level);
        }
        super.die(source);
    }

    @Override
    protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled) {
        // Intentionally empty: spawnCasket() rolls the loot table into the Seamstress Casket, once.
    }

    @Override
    protected void tickDeath() {
        this.deathTicks++;
        if (this.level() instanceof ServerLevel level) {
            // the threads snapping, the dust and the final vanishing are the death clip's own events (MAT-CIN-05/06)
            this.director.tick(level);
            if (this.deathTicks >= DEATH_TICKS && !this.isRemoved()) {
                this.spawnCasket(level);
                this.bossEvent.removeAllPlayers();
                this.level().broadcastEntityEvent(this, (byte) 60);
                this.remove(RemovalReason.KILLED);
            }
        }
    }

    /** The Seamstress Casket: a chest holding the loot table's drops (always the Staff of the Ten Threads), made once. */
    public void spawnCasket(ServerLevel level) {
        if (this.casketSpawned) {
            return;
        }
        this.casketSpawned = true;
        this.casketCount++;
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("codenamecoraline", "entities/ten_threads_witch"));
        LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .withParameter(LootContextParams.ORIGIN, this.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, this.damageSources().generic())
                .create(LootContextParamSets.ENTITY);
        List<ItemStack> loot = table.getRandomItems(params);
        BlockPos spot = this.findChestSpot(level);
        if (spot == null) {
            for (ItemStack stack : loot) {
                level.addFreshEntity(new ItemEntity(level, this.getX(), this.getY() + 0.5D, this.getZ(), stack));
            }
            return;
        }
        level.setBlock(spot, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(this.random)), 3);
        BlockEntity entity = level.getBlockEntity(spot);
        if (entity instanceof ChestBlockEntity chest) {
            for (int i = 0; i < loot.size() && i < chest.getContainerSize(); i++) {
                chest.setItem(i, loot.get(i));
            }
            chest.setChanged();
        }
        level.sendParticles(AttackRunner.THREAD_DUST, spot.getX() + 0.5D, spot.getY() + 1.0D, spot.getZ() + 0.5D, 40, 0.6D, 0.8D, 0.6D, 0.05D);
        this.playSound(ModSounds.WITCH_THREAD_SNAP.get(), 1.6F, 0.7F);
    }

    private @Nullable BlockPos findChestSpot(ServerLevel level) {
        BlockPos base = this.blockPosition();
        for (int r = 0; r <= 3; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    for (int dy = 2; dy >= -2; dy--) {
                        BlockPos p = base.offset(dx, dy, dz);
                        if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP)) {
                            return p;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void discardPuppets(ServerLevel level) {
        for (PuppetEntity p : this.getPuppets(level)) {
            p.discard();
        }
        for (var h : level.getEntitiesOfClass(ThreadHitboxEntity.class, this.getBoundingBox().inflate(80.0D), h -> this.getUUID().equals(h.getOwnerWitchUuid()))) {
            h.discard();
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        this.bossEvent.removeAllPlayers();
        if (reason.shouldDestroy() && this.level() instanceof ServerLevel level) {
            this.discardPuppets(level); // /kill or discard: every puppet goes with her
        }
        super.remove(reason);
    }

    // ---------------------------------------------------------------- the brain
    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!this.isAlive() || this.isDeadOrDying()) {
            return;
        }
        this.ticksSinceLine++;
        if (this.restoreAnimation) { // a reloaded fight: the hands take up their threads again
            this.restoreAnimation = false;
            if (phase0().isPuppetStage()) {
                this.director.onStageStart(this.getCurrentStage(), this.getActivePuppet(level));
            } else if (phase0() == EncounterPhase.STAGE_11_FURY) {
                this.director.onFuryStart();
            }
        }
        this.director.tick(level);
        this.setFocus(level);
        this.pullCooldown = Math.max(0, this.pullCooldown - 1);
        this.arenaHitCooldown = Math.max(0, this.arenaHitCooldown - 1);
        EncounterPhase phase = this.getPhase();
        this.phaseTimer++;
        if (phase == EncounterPhase.DORMANT) {
            if (level.getNearestPlayer(this, 24.0D) != null) {
                this.startEncounter(level);
            }
            return;
        }
        this.tickPosts(level);
        this.tickArena(level);
        switch (phase) {
            case INTRO -> this.tickIntro(level);
            case STAGE_11_TRANSITION -> this.tickCollapse(level);
            case STAGE_11_FURY -> this.tickFury(level);
            default -> {
                if (phase.isPuppetStage()) {
                    this.tickPuppetStage(level);
                }
            }
        }
        if (phase != EncounterPhase.STAGE_11_FURY) {
            Player p = level.getNearestPlayer(this, 40.0D);
            if (phase.isPuppetStage() && this.waitNext <= 0 && !this.director.inCinematic()) {
                boolean was = this.presence.isMoving();
                this.presence.tickStage(level);
                if (was != this.presence.isMoving()) {
                    this.director.onMoving(this.presence.isMoving());
                }
            } else {
                this.presence.hold(p);
            }
        }
        this.updateBar(level);
    }

    private EncounterPhase phase0() {
        return this.getPhase();
    }

    private void setFocus(ServerLevel level) {
        Entity focus = this.getAnimFocus(level);
        int id = focus != null && focus.isAlive() ? focus.getId() : -1;
        if (this.entityData.get(DATA_FOCUS) != id) {
            this.entityData.set(DATA_FOCUS, id);
        }
    }

    public void faceTowards(LivingEntity t) {
        double dx = t.getX() - this.getX();
        double dz = t.getZ() - this.getZ();
        if (dx * dx + dz * dz > 1.0E-5D) {
            float yaw = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
            this.setYRot(yaw);
            this.setYHeadRot(yaw);
            this.yBodyRot = yaw;
        }
    }

    /** Stitching Posts: a column of thread where no tree holds an anchor (particles only). */
    private void tickPosts(ServerLevel level) {
        if (this.tickCount % 10 != 0) {
            return;
        }
        for (ArenaAnchorManager.Anchor a : this.anchors) {
            if (!a.natural()) {
                BlockPos base = a.postBase();
                for (int y = 0; y < ArenaAnchorManager.POST_HEIGHT; y += 2) {
                    level.sendParticles(AttackRunner.THREAD_DUST, base.getX() + 0.5D, base.getY() + y, base.getZ() + 0.5D, 1, 0.05D, 0.2D, 0.05D, 0.0D);
                }
            }
        }
    }

    private void tickIntro(ServerLevel level) {
        if (this.phaseTimer % 6 == 0) {
            for (PuppetEntity p : this.getPuppets(level)) {
                level.sendParticles(AttackRunner.THREAD_DUST, p.getX(), p.getY() + p.getBbHeight() + 0.5D, p.getZ(), 2, 0.1D, 0.6D, 0.1D, 0.0D);
            }
        }
        if (this.phaseTimer >= INTRO_TICKS) {
            this.startStage(level, 1);
        }
    }

    private void tickPuppetStage(ServerLevel level) {
        int stage = this.getCurrentStage();
        if (this.waitNext > 0) { // between stages: the reaction, then the next puppet is pulled down
            this.waitNext--;
            if (stage == 5 && this.waitNext == SUMMON_TICKS + PHASE_ONE_TICKS) {
                this.director.onPhaseOne(); // MAT-PHASE-01: composed to irritated
            }
            if (stage == 5 && this.waitNext == SUMMON_TICKS + 20) {
                this.presence.setFloating(true); // the transition ends with her lifting off the ground
            }
            if (this.waitNext == 0) {
                if (stage >= 10) {
                    this.beginCollapse(level);
                } else {
                    this.startStage(level, stage + 1);
                }
            }
            return;
        }
        PuppetEntity p = this.getActivePuppet(level);
        if (p == null) {
            if (((this.getBrokenSpoolsBitmask() >> (stage - 1)) & 1) == 0) {
                // the puppet of this stage is gone (a reload, a command): the same one is invoked again, no stage is skipped
                p = this.ensurePuppet(level, stage);
                p.awaken(level, this.awakenSpot(level, p.getKind()));
            }
            return;
        }
        if (p.getPuppetState() == PuppetState.SLEEPING_SUSPENDED) { // loaded still hanging: pull it down
            p.awaken(level, this.awakenSpot(level, p.getKind()));
        }
        this.tickAntiCheese(level, p);
        this.tickReset(level, p);
    }

    /** Anti-pillar (pull the target down) and anti-hole (hoist the puppet). */
    private void tickAntiCheese(ServerLevel level, PuppetEntity p) {
        LivingEntity target = p.getTarget();
        if (target == null || p.getPuppetState() != PuppetState.IDLE_COMBAT) {
            this.pillarTicks = Math.max(0, this.pillarTicks - 2);
            return;
        }
        boolean above = target.getY() - p.getY() > PILLAR_HEIGHT && !p.isTargetReachable();
        this.pillarTicks = above ? this.pillarTicks + 1 : Math.max(0, this.pillarTicks - 2);
        if (this.pillarTicks > PILLAR_TICKS && this.pullCooldown <= 0) {
            this.pullDown(level, target);
        }
        if (p.getStuckTicks() >= PuppetEntity.STUCK_LIMIT && !above) { // a target up on a pillar is pulled down instead
            Vec3 spot = CrimsonCommander.findStandSpot(level, target.position(), 3.0D, p.getKind().width, p.getKind().height);
            p.lift(spot != null ? spot : target.position()); // WITCH_LIFT_PUPPET
            this.director.onLiftPuppet(p);
        }
    }

    /** WITCH_PULL_DOWN: a purple thread drags the target to the centre, with slow falling so the pull does not kill. */
    public void pullDown(ServerLevel level, LivingEntity target) {
        Vec3 to = this.position().subtract(target.position());
        Vec3 dir = to.lengthSqr() > 1.0E-4D ? to.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        target.setDeltaMovement(dir.x * 1.1D, 0.45D, dir.z * 1.1D);
        target.hurtMarked = true;
        target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, PULL_SLOW_FALLING_TICKS, 0), this);
        for (double k = 0.0D; k < 1.0D; k += 0.05D) {
            Vec3 p = target.position().add(0.0D, 1.0D, 0.0D).lerp(this.getThreadAnchor(), k);
            level.sendParticles(AttackRunner.SPOOL_DUST, p.x, p.y, p.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        this.playSound(ModSounds.WITCH_THREAD_TENSION.get(), 1.6F, 0.8F);
        this.pillarTicks = 0;
        this.pullCooldown = 100;
        this.pulls++;
        this.director.onPullDown(target);
    }

    /** The arena edge: a circle of threads 28 blocks from the centre pushes whoever leaves it back in, for 2 damage. */
    private void tickArena(ServerLevel level) {
        EncounterPhase phase = this.getPhase();
        if (phase == EncounterPhase.DEATH_CINEMATIC) {
            return;
        }
        if (this.tickCount % 20 == 0) {
            for (int i = 0; i < 36; i++) {
                double ang = Math.PI * 2.0D * i / 36.0D;
                level.sendParticles(AttackRunner.THREAD_DUST, this.center.x + Math.cos(ang) * ARENA_RADIUS, this.center.y + 0.5D,
                        this.center.z + Math.sin(ang) * ARENA_RADIUS, 1, 0.0D, 0.6D, 0.0D, 0.0D);
            }
        }
        for (ServerPlayer player : level.players()) {
            Vec3 d = player.position().subtract(this.center).multiply(1.0D, 0.0D, 1.0D);
            if (d.length() <= ARENA_RADIUS) {
                this.engaged.add(player.getUUID()); // only whoever entered the arena is held in it
            }
            if (this.engaged.contains(player.getUUID()) && player.isAlive() && !player.isSpectator()
                    && d.length() > ARENA_RADIUS && d.length() < ARENA_RADIUS + 40.0D) {
                Vec3 back = d.normalize().scale(-1.0D);
                player.setDeltaMovement(back.x * 1.0D, 0.3D, back.z * 1.0D);
                player.hurtMarked = true;
                this.arenaPushes++;
                if (this.arenaHitCooldown <= 0) {
                    this.arenaHitCooldown = 20;
                    this.arenaHits++;
                    player.hurtServer(level, level.damageSources().magic(), ARENA_DAMAGE);
                }
            }
        }
    }

    /** No valid player within 48 blocks for 10 seconds: the active puppet heals back and every attack and trap is dropped. */
    private void tickReset(ServerLevel level, PuppetEntity p) {
        boolean anyone = false;
        for (Player pl : level.players()) {
            if (pl.isAlive() && !pl.isSpectator() && pl.distanceToSqr(this) <= RESET_RANGE * RESET_RANGE) {
                anyone = true;
                break;
            }
        }
        this.noPlayerTicks = anyone ? 0 : this.noPlayerTicks + 1;
        if (this.noPlayerTicks >= RESET_TICKS) {
            this.noPlayerTicks = 0;
            this.resetEncounter(level, p);
        }
    }

    public void resetEncounter(ServerLevel level, @Nullable PuppetEntity p) {
        this.resets++;
        this.director.onReset();
        if (p != null) {
            p.getRunner().cancel(false);
            p.setHealth(p.getMaxHealth());
            if (p.isTangled()) {
                p.enterTangled(1);
            }
            p.setTarget(null);
        }
        for (var h : level.getEntitiesOfClass(ThreadHitboxEntity.class, this.getBoundingBox().inflate(80.0D), h -> this.getUUID().equals(h.getOwnerWitchUuid()))) { // only her own threads
            h.discard();
        }
        this.pillarTicks = 0;
    }

    // ---------------------------------------------------------------- the last phase
    private void beginCollapse(ServerLevel level) {
        this.setPhase(EncounterPhase.STAGE_11_TRANSITION);
        this.entityData.set(DATA_STAGE, 11);
        this.presence.setFloating(false); // the threads give way and she drops to the floor
        this.director.onCollapse();
        this.playSound(ModSounds.WITCH_THREAD_SNAP.get(), 2.0F, 0.6F);
        WitchDialogueManager.say(level, this, WitchDialogueManager.Line.FURY);
        this.updateBar(level);
    }

    private void tickCollapse(ServerLevel level) {
        if (this.phaseTimer % 5 == 0) {
            level.sendParticles(AttackRunner.CRIMSON_DUST, this.getX(), this.getY() + 0.8D, this.getZ(), 10, 0.6D, 0.5D, 0.6D, 0.08D);
        }
        if (this.phaseTimer >= COLLAPSE_TICKS) {
            this.beginFury(level);
        }
    }

    private void beginFury(ServerLevel level) {
        this.entityData.set(DATA_STAGE, 11);
        this.entityData.set(DATA_BROKEN, 0x3FF);
        this.setPhase(EncounterPhase.STAGE_11_FURY);
        this.setHealth(this.getMaxHealth());
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(FURY_SPEED);
        }
        this.director.onFuryStart();
        this.updateBar(level);
    }

    private void tickFury(ServerLevel level) {
        this.runner.tickCooldowns();
        if (this.tangledTicks > 0) {
            this.getNavigation().stop();
            --this.tangledTicks;
            return;
        }
        if (this.director.incapacitated() || this.director.inCinematic()) {
            this.getNavigation().stop(); // down, stunned or in a transition: no step, no blow
            this.comboWindow = 0;
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || !this.canAttack(target)) {
            target = level.getNearestPlayer(this, 48.0D);
            this.setTarget(target != null && !target.isSpectator() ? target : null);
        }
        if (this.runner.isBusy()) {
            this.tickFeint(target);
            if (this.runner.isBusy()) {
                this.runner.tick(level, target);
            }
            this.presence.tickFury(level, target, false);
            return;
        }
        if (target == null) {
            this.getNavigation().stop();
            this.comboWindow = 0;
            this.presence.tickFury(level, null, false);
            return;
        }
        double dist = Math.sqrt(this.distanceToSqr(target));
        boolean sight = this.getSensing().hasLineOfSight(target);
        // a defence opened a counter-attack (MAT-DEF-* to its transition)
        if (this.pendingCounter != null && --this.counterDelay <= 0) {
            PuppetAttack counter = this.pendingCounter;
            this.pendingCounter = null;
            if (sight && dist <= counter.maxRange + 1.0D) {
                this.runner.setCooldown(counter, 0);
                this.runner.start(counter, target);
                return;
            }
        }
        // a combo continues within its window, along the branch the situation calls for (6.6)
        if (this.comboWindow > 0 && this.comboFrom != null) {
            this.comboWindow--;
            MatriarchMoveset.Branch branch = sight ? MatriarchMoveset.branch(this.comboFrom, this, target, dist) : null;
            if (branch != null && dist <= branch.to().maxRange + 0.5D) {
                this.comboWindow = 0;
                this.comboFrom = null;
                this.runner.setCooldown(branch.to(), 0);
                this.runner.start(branch.to(), target);
                return;
            }
        }
        if (this.comboWindow <= 0 && this.restPending) {
            this.restPending = false;
            this.furyRest = this.furyRestTicks(); // the sequence is over: she takes a breath before the next one
        }
        // a player crowding her: a step back, sometimes a vicious one that turns into a counter
        if (dist < 1.6D && this.random.nextInt(24) == 0) {
            if (this.director.dodgeBack(target)) {
                this.pendingCounter = MatriarchMoveset.DODGE_COUNTER;
                this.counterDelay = MatAnim.DEF_08.ticks - 4;
            }
            return;
        }
        if (this.furyRest > 0) {
            this.furyRest--;
            this.moveInFury(level, target);
            return;
        }
        PuppetAttack pick = this.runner.pick(target, dist, target.getY() - this.getY(), sight);
        if (pick != null) {
            this.runner.start(pick, target);
            return;
        }
        this.moveInFury(level, target);
    }

    private void moveInFury(ServerLevel level, LivingEntity target) {
        if (this.presence.isFloating()) {
            this.getNavigation().stop();
            this.presence.tickFury(level, target, false);
        } else {
            this.getNavigation().moveTo(target, 1.2D);
            this.presence.tickFury(level, target, !this.getNavigation().isDone());
        }
    }

    /** The breath after a sequence of blows: {@link #FURY_REST_CALM} at full health, down to {@link #FURY_REST_WILD} near death. */
    public int furyRestTicks() {
        return Math.round(Mth.lerp(MatriarchMood.strangeness(this.getUnravel()), FURY_REST_CALM, FURY_REST_WILD));
    }

    public int getFuryRest() {
        return this.furyRest;
    }

    /** MAT-ATK-04: the double-edged wind-up of the hem cleave turns into a quick flick when the target stepped in close. */
    private void tickFeint(@Nullable LivingEntity target) {
        PuppetAttack a = this.runner.current();
        if (a == MatriarchMoveset.HEM_CLEAVE && this.runner.phase() == Phase.PREPARE && this.runner.timer() == a.prep - 3
                && target != null && this.distanceTo(target) <= 2.2D) {
            this.runner.cancel(false);
            this.runner.setCooldown(MatriarchMoveset.FEINT_FLICK, 0);
            this.runner.start(MatriarchMoveset.FEINT_FLICK, target);
        }
    }

    private void queueCounter() {
        PuppetAttack counter = this.director.counterFor();
        if (counter != null) {
            this.pendingCounter = counter;
            this.counterDelay = 8;
        }
    }

    // ---------------------------------------------------------------- host callbacks (the last phase)
    @Override
    public void phaseChanged(PuppetAttack attack, Phase phase) {
        this.director.onAttackPhase(attack, phase);
        if (phase == Phase.EXECUTE) {
            this.playSound(ModSounds.WITCH_THREAD_TENSION.get(), 1.4F, 1.4F);
        }
    }

    @Override
    public void attackEnded(PuppetAttack attack, boolean hit) {
        if (this.runner.cancelled() > this.seenCancelled) {
            this.seenCancelled = this.runner.cancelled();
            this.director.onAttackCancelled(); // MAT-ATK-05
            this.comboWindow = 0;
            this.restPending = true;
            return;
        }
        this.comboFrom = attack.id;
        this.comboWindow = MatriarchMoveset.comboWindow(attack.id);
        this.restPending = true;
    }

    @Override
    public void tangle(PuppetAttack attack, int ticks) {
        this.tangledTicks = ticks; // the unravelled storm leaves her exhausted on the floor
        this.director.onTangled(ticks);
        this.getNavigation().stop();
        this.comboWindow = 0;
        this.playSound(ModSounds.PUPPET_TANGLED.get(), 1.4F, 0.8F);
    }

    @Override
    public void crashed(PuppetAttack attack) {
    }

    // ---------------------------------------------------------------- persistence
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
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Phase", this.getPhase().ordinal());
        output.putInt("CurrentStage", this.getCurrentStage());
        output.putInt("BrokenSpools", this.getBrokenSpoolsBitmask());
        output.putDouble("CenterX", this.center.x);
        output.putDouble("CenterY", this.center.y);
        output.putDouble("CenterZ", this.center.z);
        output.putBoolean("CasketSpawned", this.casketSpawned);
        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            ids.append(this.puppetKnown[i] ? this.puppetIds[i].toString() : "-").append(i < 9 ? "," : "");
        }
        output.putString("Puppets", ids.toString());
        if (this.getPhase().isPuppetStage()) {
            PuppetEntity active = this.level() instanceof ServerLevel level ? this.getActivePuppet(level) : null;
            output.putString("ActivePuppet", active != null ? active.getUUID().toString() : "");
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        EncounterPhase phase = EncounterPhase.byOrdinal(input.getIntOr("Phase", 0));
        this.entityData.set(DATA_STAGE, input.getIntOr("CurrentStage", 0));
        this.entityData.set(DATA_BROKEN, input.getIntOr("BrokenSpools", 0));
        this.center = new Vec3(input.getDoubleOr("CenterX", this.getX()), input.getDoubleOr("CenterY", this.getY()), input.getDoubleOr("CenterZ", this.getZ()));
        this.casketSpawned = input.getBooleanOr("CasketSpawned", false);
        String[] ids = input.getStringOr("Puppets", "").split(",");
        for (int i = 0; i < 10 && i < ids.length; i++) {
            UUID u = parseUuid(ids[i]);
            this.puppetKnown[i] = u != null;
            if (u != null) {
                this.puppetIds[i] = u;
            }
        }
        // a fight reloaded in the middle of an intro or a collapse resumes at the stage it was on
        if (phase == EncounterPhase.INTRO) {
            phase = EncounterPhase.ofStage(1);
            this.entityData.set(DATA_STAGE, 1);
        } else if (phase == EncounterPhase.STAGE_11_TRANSITION) {
            phase = EncounterPhase.STAGE_11_FURY;
        } else if (phase == EncounterPhase.DEATH_CINEMATIC) {
            this.rewardsGranted = true;
        }
        this.setPhase(phase);
        this.bossEvent.setVisible(phase != EncounterPhase.DORMANT);
        this.needsRebind = phase.isPuppetStage();
        this.restoreAnimation = true;
        if (phase.isPuppetStage() && this.getCurrentStage() >= 6) {
            this.presence.setFloating(true);
        }
        if (phase == EncounterPhase.STAGE_11_FURY) {
            AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.setBaseValue(FURY_SPEED);
            }
        }
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    // ---------------------------------------------------------------- sounds
    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.getPhase() == EncounterPhase.DEATH_CINEMATIC ? null : ModSounds.WITCH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WITCH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WITCH_DEATH.get();
    }

    public static boolean otherWitchNear(ServerLevel level, BlockPos pos) {
        return !level.getEntitiesOfClass(MatriarchWitchEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(OTHER_WITCH_RADIUS),
                w -> w.isAlive() && w.getPhase() != EncounterPhase.DEATH_CINEMATIC).isEmpty();
    }
}
