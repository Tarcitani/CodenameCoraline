package com.codenamecoraline.entity.canopy;

import com.codenamecoraline.entity.canopy.TreeNav.JumpPlan;
import com.codenamecoraline.entity.canopy.TreeNav.Trunk;
import com.codenamecoraline.entity.macaque.BarkMacaque;
import com.codenamecoraline.entity.spirit.BossSpirit;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Canopy Breaker (O Quebra-Copas): an ancient ape that owns the old forests' canopy. It chases on the ground, hugs
 * trunks and climbs them with real physics, crosses the treetops and jumps between them on ballistic trajectories, and
 * fights with the Deadroot Club. The server owns everything (movement, damage, stages, drops); the client only plays the
 * animation the server selects (synched), so visuals always mirror gameplay.
 *
 * <p>Movement is a state machine ({@link Activity}); combat is a second, nested one ({@link Attack} with prepare,
 * execute and recover phases). Stage 1 is the Guardian of the Woods, stage 2 (60% health) the Canopy Hunter, stage 3
 * (25% health) the Fury of the Timber; each transition happens once.</p>
 */
public class CanopyBreaker extends Monster {
    // ---------------------------------------------------------------- balance
    public static final double MAX_HEALTH = 200.0D;
    public static final double ARMOR = 8.0D;
    public static final double GROUND_SPEED = 0.33D;
    public static final double BASE_DAMAGE = 7.0D;
    public static final int XP_REWARD = 60;
    public static final float WIDTH = 1.8F;
    public static final float HEIGHT = 3.1F;
    public static final float EYE_HEIGHT = 2.7F;
    public static final float CLIMB_WIDTH = 1.0F;
    public static final float CLIMB_HEIGHT = 2.2F;
    /** Minions alive at once (counting every species); a new call never goes beyond it. */
    public static final int MAX_MINIONS = 4;
    public static final int OPENING_MINIONS = 2;
    public static final int REINFORCEMENT_MINIONS = 1;
    /** Ticks between the warning (leaves and a cry at the spot) and the minion arriving. */
    public static final int MINION_WARNING_TICKS = 20;
    public static final double STAGE_TWO_HEALTH = 0.60D;
    public static final double STAGE_THREE_HEALTH = 0.25D;

    // ---------------------------------------------------------------- timings (ticks; 20 per second)
    /** The intro (6 s): he rises and looks around, drums his chest like a gorilla, then roars as the title appears. */
    public static final int INTRO_TICKS = 120;
    public static final int INTRO_BEAT_TICK = 24;
    public static final int INTRO_ROAR_TICK = 72;
    public static final int INTRO_SETTLE_TICK = 104;
    /** Ticks into the intro at which the opening minions land (they arrive during the roar). */
    public static final int INTRO_MINION_DELAY = 84;
    public static final int DRAW_PREPARE_TICKS = 8;
    public static final int DRAW_TICKS = 12;
    public static final int DRAW_FINISH_TICKS = 12;
    public static final int TRANSITION_TWO_TICKS = 50;
    public static final int TRANSITION_THREE_TICKS = 60;

    public static final int SLAM_PREP = 20;
    public static final int SLAM_EXEC = 10;
    public static final int SLAM_HIT_TICK = 6;
    public static final int SLAM_RECOVER = 18;
    public static final float SLAM_DAMAGE = 7.0F;
    public static final double SLAM_RADIUS = 2.6D;

    public static final int SWEEP_PREP = 16;
    public static final int SWEEP_EXEC = 10;
    public static final int SWEEP_HIT_TICK = 7;
    public static final int SWEEP_RECOVER = 18;
    public static final float SWEEP_DAMAGE = 5.0F;
    public static final double SWEEP_REACH = 4.6D;

    public static final int LUNGE_PREP = 10;
    public static final int LUNGE_EXEC = 12;
    public static final int LUNGE_RECOVER = 18;
    public static final float LUNGE_DAMAGE = 6.0F;
    public static final double LUNGE_SPEED = 0.85D;

    public static final int BACKHAND_PREP = 10;
    public static final int BACKHAND_EXEC = 6;
    public static final int BACKHAND_HIT_TICK = 3;
    public static final int BACKHAND_RECOVER = 10;
    public static final float BACKHAND_DAMAGE = 4.0F;
    public static final double BACKHAND_REACH = 3.2D;

    public static final int SHIELD_BREAK_PREP = 16;
    public static final int SHIELD_BREAK_EXEC = 14;
    public static final int SHIELD_BREAK_HIT_TICK = 8;
    public static final int SHIELD_BREAK_RECOVER = 20;
    public static final float SHIELD_BREAK_DAMAGE = 12.0F;
    public static final double SHIELD_BREAK_REACH = 3.8D;
    public static final double SHIELD_BREAK_KNOCKBACK = 1.2D;

    public static final int DROP_PREP = 14;
    public static final int DROP_RECOVER = 26;
    public static final float DROP_DAMAGE = 8.0F;
    public static final double DROP_RADIUS = 3.4D;

    public static final int INTIMIDATE_TICKS = 30;
    public static final int STAGGER_TICKS = 16;
    public static final int LAND_RECOVERY_TICKS = 20;

    public static final double CLIMB_SPEED = 0.2D;
    public static final int MAX_ROUTE_TICKS = 600;
    public static final int MAX_TRUNK_SEARCH = 12;

    private static final DustParticleOptions BARK_DUST = new DustParticleOptions(0x7A5638, 1.4F);
    private static final DustParticleOptions MARK_DUST = new DustParticleOptions(0xC44020, 1.6F);

    // ---------------------------------------------------------------- types
    /** What the body is doing: the movement state machine. */
    public enum Activity {
        INTRO, GROUND, DRAW_CLUB, ATTACK, ROAR, CLIMB_APPROACH, CLIMB_UP, CLIMB_DOWN, CLING, BRANCH,
        JUMP_PREPARATION, AIRBORNE, LANDING, LAND_RECOVERY, FALL_RECOVERY, STAGGER, TRANSITION, DYING, DEAD
    }

    public enum Attack { NONE, TRUNK_SLAM, CANOPY_SWEEP, BRUTE_LUNGE, CANOPY_DROP, BACKHAND, SHIELD_BREAK, INTIMIDATE }

    public enum Phase { PREPARE, EXECUTE, RECOVER }

    /** A planned tree route. */
    public static final class Route {
        public final boolean drop;
        public final Trunk trunk;
        public final Vec3 approach;
        public final Vec3 perch;
        public Vec3 landing;
        public Direction face;
        public @Nullable JumpPlan plan;
        /** Leaps that never left the perch (blocked by foliage or a wall): two of them end the route. */
        public int failedJumps;

        Route(boolean drop, Trunk trunk, Vec3 approach, Vec3 perch, Vec3 landing, Direction face) {
            this.drop = drop;
            this.trunk = trunk;
            this.approach = approach;
            this.perch = perch;
            this.landing = landing;
            this.face = face;
        }
    }

    // ---------------------------------------------------------------- synched state
    private static final EntityDataAccessor<Integer> DATA_ANIM =
            SynchedEntityData.defineId(CanopyBreaker.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_OVERLAY =
            SynchedEntityData.defineId(CanopyBreaker.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(CanopyBreaker.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STAGE =
            SynchedEntityData.defineId(CanopyBreaker.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CLUB =
            SynchedEntityData.defineId(CanopyBreaker.class, EntityDataSerializers.BOOLEAN);

    /** Client-side animation clocks. */
    public final AnimationState animState = new AnimationState();
    public final AnimationState overlayState = new AnimationState();

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            UUID.randomUUID(),
            Component.translatable("entity.codenamecoraline.canopy_breaker"),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.PROGRESS);

    // ---------------------------------------------------------------- server state
    private int activityTimer = 0;
    private Attack attack = Attack.NONE;
    private Phase phase = Phase.PREPARE;
    private int phaseTimer = 0;
    private boolean attackHitDone = false;
    private boolean sweepLeft = true;
    private Vec3 lungeDir = Vec3.ZERO;
    private int transitionCount = 0;
    private int pendingStage = 1;
    private int stageTransitions = 0;
    private int deathTicks = 0;
    private boolean rewardsGranted = false;
    private int deathAwardCount = 0;
    private int animHold = 0;
    private int overlayTicks = 0;
    private int idleTicks = 0;
    private int noTargetTicks = 0;
    private boolean hadTarget = false;
    private boolean introDone = true;
    /** Client side: the local tick at which the synched activity turned to INTRO (drives the cinematic). */
    private int introClientStart = -1;

    /** A minion that has been called and is still on its way: the spot shows leaves, then it arrives. */
    private static final class PendingMinion {
        final Vec3 pos;
        int delay;

        PendingMinion(Vec3 pos, int delay) {
            this.pos = pos;
            this.delay = delay;
        }
    }

    private final List<PendingMinion> pendingMinions = new ArrayList<>();
    private boolean openingCalled = false;
    private int minionsSummoned = 0;

    private final int[] cooldowns = new int[Attack.values().length];
    private Attack lastAttack = Attack.NONE;
    private int routeCooldown = 80;
    private int tauntCooldown = 200;
    private int roarCooldown = 0;
    private int staggerCooldown = 0;
    private int pathRecalc = 0;
    private int stuckTicks = 0;
    private Vec3 stuckAnchor = Vec3.ZERO;

    private @Nullable Route route;
    private int routeTicks = 0;
    private int branchTicks = 0;
    private int jumpTicks = 0;
    private int airTicks = 0;
    private double jumpStartY = 0.0D;
    private boolean steered = false;
    private Vec3 targetVelocity = Vec3.ZERO;
    private Vec3 targetLastPos = Vec3.ZERO;
    private Vec3 lastPos = Vec3.ZERO;
    private double lastSpeed = 0.0D;
    private float lastYaw = 0.0F;
    private @Nullable LivingEntity lastLookedAt;
    private int jumpsCompleted = 0;
    private int dropsLanded = 0;
    private int routesStarted = 0;
    private int routesAborted = 0;

    public CanopyBreaker(EntityType<? extends CanopyBreaker> type, Level level) {
        super(type, level);
        this.xpReward = XP_REWARD;
        this.bossEvent.setVisible(false);
        this.bossEvent.setDarkenScreen(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, ARMOR)
                .add(Attributes.MOVEMENT_SPEED, GROUND_SPEED)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.STEP_HEIGHT, 1.1D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANIM, CanopyAnim.IDLE_BREATHING.ordinal());
        builder.define(DATA_OVERLAY, -1);
        builder.define(DATA_ACTIVITY, Activity.GROUND.ordinal());
        builder.define(DATA_STAGE, 1);
        builder.define(DATA_CLUB, false);
    }

    // ---------------------------------------------------------------- accessors
    public CanopyAnim getAnim() {
        return CanopyAnim.byOrdinal(this.entityData.get(DATA_ANIM));
    }

    public @Nullable CanopyAnim getOverlay() {
        int o = this.entityData.get(DATA_OVERLAY);
        return o < 0 ? null : CanopyAnim.byOrdinal(o);
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

    public boolean isClubDrawn() {
        return this.entityData.get(DATA_CLUB);
    }

    public Attack getAttack() {
        return this.attack;
    }

    public Phase getPhase() {
        return this.phase;
    }

    public @Nullable Route getRoute() {
        return this.route;
    }

    public int getTransitionCount() {
        return this.stageTransitions;
    }

    public int getDeathAwardCount() {
        return this.deathAwardCount;
    }

    public int getJumpsCompleted() {
        return this.jumpsCompleted;
    }

    public int getDropsLanded() {
        return this.dropsLanded;
    }

    public int getRoutesAborted() {
        return this.routesAborted;
    }

    /** Ticks since the intro began: the server's timer, or on the client the time since INTRO was synched. */
    public int getIntroTicks() {
        if (this.getActivity() != Activity.INTRO) {
            return 0;
        }
        if (this.level().isClientSide()) {
            return this.introClientStart < 0 ? 0 : this.tickCount - this.introClientStart;
        }
        return this.activityTimer;
    }

    public ServerBossEvent getBossEvent() {
        return this.bossEvent;
    }

    /** True for the climbing family of animations: the head should not track the target then. */
    public boolean isClimbingAnimation() {
        CanopyAnim a = this.getAnim();
        return a.name().startsWith("CLIMB_") || a.name().startsWith("CLING") || a.name().startsWith("BRANCH_")
                || a.name().startsWith("DEATH_");
    }

    public boolean isUnderRoute() {
        return this.route != null;
    }

    public void setClubDrawn(boolean drawn) {
        this.entityData.set(DATA_CLUB, drawn);
    }

    /** Selects the animation the clients play; honours a short hold so locomotion does not flicker. */
    public void setAnim(CanopyAnim anim) {
        if (this.getAnim() != anim) {
            this.entityData.set(DATA_ANIM, anim.ordinal());
            this.animHold = 0;
        }
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
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        if (pose == Pose.CROUCHING) {
            return EntityDimensions.scalable(CLIMB_WIDTH, CLIMB_HEIGHT).withEyeHeight(1.9F);
        }
        return super.getDefaultDimensions(pose);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false; // his landings are controlled; falling is handled by FALL_RECOVERY
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
    public boolean canAttack(LivingEntity target) {
        Activity a = this.getActivity();
        if (a == Activity.INTRO || a == Activity.TRANSITION || a == Activity.DYING || a == Activity.DEAD) {
            return false;
        }
        return !(target instanceof CanopyBreaker) && !(target instanceof BarkMacaque) && super.canAttack(target);
    }

    // ---------------------------------------------------------------- minions
    public int getMinionsSummoned() {
        return this.minionsSummoned;
    }

    public boolean isOpeningCalled() {
        return this.openingCalled;
    }

    /** Marks the opening call as already made (tests and scripted fights that do not want the call). */
    public void setOpeningCalled(boolean called) {
        this.openingCalled = called;
    }

    public int getPendingMinions() {
        return this.pendingMinions.size();
    }

    /** Living minions bound to this boss. */
    public List<BarkMacaque> getMinions(ServerLevel level) {
        return level.getEntitiesOfClass(BarkMacaque.class, this.getBoundingBox().inflate(96.0D),
                m -> m.isAlive() && m.isOwnedBy(this));
    }

    /**
     * Calls up to {@code count} Bark Macaques, never beyond {@link #MAX_MINIONS} counting those alive and those already
     * on their way. Each arrives {@code delay} ticks later at a canopy or ground spot, announced by leaves and a cry.
     * Returns how many were scheduled (zero when the arena is already full).
     */
    public int callMinions(ServerLevel level, int count, int delay) {
        int room = MAX_MINIONS - this.getMinions(level).size() - this.pendingMinions.size();
        int n = Math.min(count, Math.max(0, room));
        if (n <= 0) {
            return 0;
        }
        List<Vec3> spots = this.minionSpots(level, n);
        for (Vec3 spot : spots) {
            this.pendingMinions.add(new PendingMinion(spot, delay));
        }
        return spots.size();
    }

    /** Canopy tops of nearby trees first (the minions come down from there), then free ground around the boss. */
    private List<Vec3> minionSpots(ServerLevel level, int n) {
        List<Vec3> spots = new ArrayList<>();
        List<Trunk> trunks = TreeNav.findTrunks(level, this.blockPosition(), 14, 6);
        for (Trunk trunk : trunks) {
            if (spots.size() >= n) {
                break;
            }
            Vec3 perch = TreeNav.findPerch(level, trunk, 3, BarkMacaque.WIDTH, BarkMacaque.HEIGHT);
            if (perch != null && perch.distanceTo(this.position()) > 3.0D) {
                spots.add(perch);
            }
        }
        double base = this.random.nextDouble() * Math.PI * 2.0D;
        for (int i = 0; spots.size() < n && i < 8; i++) {
            double a = base + Math.PI * 2.0D * i / 8.0D;
            Vec3 around = this.position().add(Math.cos(a) * 5.5D, 0.0D, Math.sin(a) * 5.5D);
            Vec3 land = TreeNav.findLanding(level, around, 2.5D, BarkMacaque.WIDTH, BarkMacaque.HEIGHT, false);
            if (land != null && spots.stream().noneMatch(v -> v.distanceTo(land) < 1.5D)) {
                spots.add(land);
            }
        }
        return spots;
    }

    private void tickMinions(ServerLevel level) {
        if (this.pendingMinions.isEmpty()) {
            return;
        }
        var it = this.pendingMinions.iterator();
        while (it.hasNext()) {
            PendingMinion m = it.next();
            m.delay--;
            if (m.delay > 0 && m.delay % 4 == 0) { // warning: leaves rain at the spot
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_LEAVES.defaultBlockState()),
                        m.pos.x, m.pos.y + 0.6D, m.pos.z, 5, 0.3D, 0.3D, 0.3D, 0.03D);
            }
            if (m.delay > 0) {
                continue;
            }
            it.remove();
            if (this.getMinions(level).size() >= MAX_MINIONS) {
                continue; // the cap holds even if something else filled the arena meanwhile
            }
            BarkMacaque macaque = ModEntities.BARK_MACAQUE.get().create(level, EntitySpawnReason.TRIGGERED);
            if (macaque == null) {
                continue;
            }
            macaque.setPos(m.pos.x, m.pos.y, m.pos.z);
            macaque.setOwner(this);
            LivingEntity t = this.getTarget();
            if (t != null) {
                macaque.setTarget(t);
            }
            level.addFreshEntity(macaque);
            this.minionsSummoned++;
        }
    }

    /** The boss fell or was removed: its minions leave with it, none stays behind. */
    private void dismissMinions() {
        this.pendingMinions.clear();
        if (this.level() instanceof ServerLevel level) {
            for (BarkMacaque m : this.getMinions(level)) {
                m.dismiss();
            }
        }
    }

    // ---------------------------------------------------------------- spawn and intro
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
                                                  EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        this.bossEvent.setVisible(true);
        return result;
    }

    /** The short arrival: the branches tremble, he roars into the clearing, then the fight begins. */
    public void startIntro(ServerLevel level) {
        this.introDone = false;
        this.setActivity(Activity.INTRO);
        this.setAnim(CanopyAnim.IDLE_WATCH);
        this.bossEvent.setVisible(true);
        this.getNavigation().stop();
        this.playSound(ModSounds.CANOPY_ROAR_ALERT.get(), 2.4F, 0.8F);
        this.openingCalled = true;
        this.callMinions(level, OPENING_MINIONS, INTRO_MINION_DELAY); // two macaques come down during the roar
        BlockParticleOption leaves = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_LEAVES.defaultBlockState());
        level.sendParticles(leaves, this.getX(), this.getY() + 3.0D, this.getZ(), 40, 3.0D, 1.5D, 3.0D, 0.1D);
    }

    // ---------------------------------------------------------------- damage, stagger, stages, death
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        Activity a = this.getActivity();
        if ((a == Activity.INTRO || a == Activity.TRANSITION) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        if (a == Activity.DYING || a == Activity.DEAD) {
            return false;
        }
        float before = this.getHealth();
        boolean hurt = super.hurtServer(level, source, amount);
        if (!hurt || !this.isAlive()) {
            return hurt;
        }
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        float taken = before - this.getHealth();
        this.showHurtOverlay(source);
        if (taken >= this.getMaxHealth() * 0.12F && this.roarCooldown <= 0 && !this.isAnimLocked()) {
            this.roarCooldown = 120;
            this.playSound(ModSounds.CANOPY_ROAR_ATTACK.get(), 1.8F, 0.75F);
        }
        int wanted = this.stageForHealth();
        if (wanted > this.getStage() && a != Activity.TRANSITION) {
            this.startTransition(wanted);
        } else if (this.canStagger(a, taken)) {
            this.startStagger();
        }
        return hurt;
    }

    private boolean isAnimLocked() {
        Activity a = this.getActivity();
        return a == Activity.AIRBORNE || a == Activity.TRANSITION || a == Activity.DYING;
    }

    private boolean canStagger(Activity a, float taken) {
        boolean vulnerable = a == Activity.LAND_RECOVERY || (a == Activity.ATTACK && this.phase == Phase.RECOVER);
        return vulnerable && this.staggerCooldown <= 0 && taken >= this.getMaxHealth() * 0.07F;
    }

    private void showHurtOverlay(DamageSource source) {
        CanopyAnim overlay = CanopyAnim.HURT_FRONT;
        if (source.getSourcePosition() != null) {
            Vec3 to = source.getSourcePosition().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            Vec3 fwd = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D);
            if (to.lengthSqr() > 1.0E-4D && fwd.lengthSqr() > 1.0E-4D) {
                double dot = fwd.normalize().dot(to.normalize());
                double side = fwd.x * to.z - fwd.z * to.x;
                overlay = dot > 0.5D ? CanopyAnim.HURT_FRONT : dot < -0.5D ? CanopyAnim.HURT_BACK : CanopyAnim.HURT_SIDE;
                if (overlay == CanopyAnim.HURT_SIDE && side < 0) {
                    overlay = CanopyAnim.HURT_SIDE;
                }
            }
        }
        this.entityData.set(DATA_OVERLAY, overlay.ordinal());
        this.overlayTicks = 8;
    }

    private int stageForHealth() {
        double r = this.getHealth() / this.getMaxHealth();
        return r <= STAGE_THREE_HEALTH ? 3 : r <= STAGE_TWO_HEALTH ? 2 : 1;
    }

    private void startTransition(int stage) {
        this.releaseClimb();
        this.cancelAttack(false);
        this.route = null;
        this.pendingStage = stage;
        this.stageTransitions++;
        this.setActivity(Activity.TRANSITION);
        this.setAnim(stage >= 3 ? CanopyAnim.FURY_TRANSITION : CanopyAnim.PHASE_TWO_TRANSITION);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.setClubDrawn(true);
        this.playSound(ModSounds.CANOPY_ROAR_FURY.get(), 2.6F, stage >= 3 ? 0.62F : 0.8F);
        if (this.level() instanceof ServerLevel level) {
            // one reinforcement per stage, only if the arena has room (stage 2 will call the Root Boar once it exists)
            this.callMinions(level, REINFORCEMENT_MINIONS, stage >= 3 ? 36 : 30);
            BlockParticleOption leaves = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_LEAVES.defaultBlockState());
            level.sendParticles(leaves, this.getX(), this.getY() + 2.5D, this.getZ(), 50, 3.0D, 1.6D, 3.0D, 0.12D);
        }
    }

    private void applyStage(int stage) {
        this.entityData.set(DATA_STAGE, Mth.clamp(stage, 1, 3));
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(GROUND_SPEED * (stage >= 3 ? 1.12D : stage >= 2 ? 1.04D : 1.0D));
        }
        this.bossEvent.setColor(stage >= 3 ? BossEvent.BossBarColor.RED : stage >= 2 ? BossEvent.BossBarColor.YELLOW
                : BossEvent.BossBarColor.GREEN);
    }

    private void startStagger() {
        this.staggerCooldown = 140;
        this.cancelAttack(false);
        this.setActivity(Activity.STAGGER);
        this.setAnim(CanopyAnim.STAGGER);
        this.getNavigation().stop();
        this.playSound(ModSounds.CANOPY_HURT.get(), 1.8F, 0.7F);
    }

    @Override
    public void die(DamageSource source) {
        if (this.rewardsGranted) {
            return;
        }
        this.rewardsGranted = true;
        this.deathAwardCount++;
        this.dismissMinions();
        this.releaseClimb();
        this.cancelAttack(false);
        this.route = null;
        this.setActivity(Activity.DYING);
        this.setClubDrawn(true);
        this.setAnim(CanopyAnim.ROAR_DEATH);
        this.bossEvent.setProgress(0.0F);
        this.getNavigation().stop();
        this.playSound(ModSounds.CANOPY_DEATH.get(), 2.4F, 0.8F);
        super.die(source);
    }

    @Override
    protected void tickDeath() {
        this.deathTicks++;
        if (this.deathTicks == 14) {
            this.setAnim(CanopyAnim.DEATH_PREPARE);
        } else if (this.deathTicks == 32) {
            this.setAnim(CanopyAnim.DEATH_FALL);
        } else if (this.deathTicks == 48) {
            this.setAnim(CanopyAnim.DEATH_IMPACT);
            if (this.level() instanceof ServerLevel level) {
                BossSpirit.rise(level, ModEntities.CANOPY_BREAKER_SPIRIT.get(), this); // his spirit leaves the fallen body
                this.playSound(ModSounds.CANOPY_LAND_HEAVY.get(), 2.0F, 0.6F);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_LEAVES.defaultBlockState()),
                        this.getX(), this.getY() + 0.5D, this.getZ(), 40, 1.8D, 0.4D, 1.8D, 0.1D);
            }
        } else if (this.deathTicks == 58) {
            this.setAnim(CanopyAnim.DEATH_FINAL);
        }
        if (this.deathTicks >= 90 && !this.level().isClientSide()) {
            this.setActivity(Activity.DEAD);
            this.bossEvent.removeAllPlayers();
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        this.bossEvent.removeAllPlayers();
        if (reason.shouldDestroy()) {
            this.dismissMinions();
        }
        super.remove(reason);
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
        this.tickMinions(level);
        Vec3 now = this.position();
        this.lastSpeed = Math.sqrt((now.x - this.lastPos.x) * (now.x - this.lastPos.x) + (now.z - this.lastPos.z) * (now.z - this.lastPos.z));
        this.lastPos = now;

        switch (this.getActivity()) {
            case INTRO -> this.tickIntro(level);
            case GROUND -> this.tickGround(level);
            case DRAW_CLUB -> this.tickDrawClub();
            case ATTACK -> this.tickAttack(level);
            case ROAR -> this.tickRoar();
            case CLIMB_APPROACH -> this.tickClimbApproach(level);
            case CLIMB_UP -> this.tickClimbUp(level);
            case CLIMB_DOWN -> this.tickClimbDown(level);
            case CLING -> this.tickCling(level);
            case BRANCH -> this.tickBranch(level);
            case JUMP_PREPARATION -> this.tickJumpPreparation(level);
            case AIRBORNE -> this.tickAirborne(level);
            case LANDING -> this.tickLanding(level);
            case LAND_RECOVERY -> this.tickLandRecovery(level);
            case FALL_RECOVERY -> this.tickFallRecovery(level);
            case STAGGER -> this.tickStagger();
            case TRANSITION -> this.tickTransition(level);
            default -> {
            }
        }
        this.lastYaw = this.getYRot();
    }

    private void tickCounters() {
        this.activityTimer++;
        this.animHold++;
        for (int i = 0; i < this.cooldowns.length; i++) {
            if (this.cooldowns[i] > 0) {
                this.cooldowns[i] -= this.getStage() >= 3 ? 2 : 1 + (this.tickCount % 4 == 0 && this.getStage() >= 2 ? 1 : 0);
            }
        }
        this.routeCooldown = Math.max(0, this.routeCooldown - 1);
        this.tauntCooldown = Math.max(0, this.tauntCooldown - 1);
        this.roarCooldown = Math.max(0, this.roarCooldown - 1);
        this.staggerCooldown = Math.max(0, this.staggerCooldown - 1);
        this.pathRecalc = Math.max(0, this.pathRecalc - 1);
        if (this.overlayTicks > 0 && --this.overlayTicks == 0) {
            this.entityData.set(DATA_OVERLAY, -1);
        }
    }

    /** Short beats (taunt, lost-target scan, stowing the club, recovering from a stumble) with their own length. */
    private void tickRoar() {
        this.getNavigation().stop();
        CanopyAnim a = this.getAnim();
        int total = switch (a) {
            case TAUNT -> 28;
            case TARGET_LOST -> 32;
            case CLUB_STOW, KNOCKBACK_RECOVER -> 16;
            default -> 18;
        };
        if (a == CanopyAnim.CLUB_STOW && this.activityTimer == 10) {
            this.setClubDrawn(false); // the club goes back to the back mid-animation
            this.playSound(ModSounds.CANOPY_CLUB_DRAW.get(), 1.2F, 0.8F);
        }
        if (this.activityTimer >= total) {
            this.setActivity(Activity.GROUND);
        }
    }

    private void tickTimedActivity(int ticks, Activity next) {
        this.getNavigation().stop();
        if (this.activityTimer >= ticks) {
            this.setActivity(next);
        }
    }

    private void tickIntro(ServerLevel level) {
        this.getNavigation().stop();
        int t = this.activityTimer;
        BlockParticleOption leaves = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_LEAVES.defaultBlockState());
        if (t == 2) {
            this.playSound(ModSounds.CANOPY_ROAR_ALERT.get(), 1.6F, 0.7F); // a low growl from the trees
        }
        if (t < INTRO_BEAT_TICK && t % 4 == 0) {
            level.sendParticles(leaves, this.getX(), this.getY() + 4.0D, this.getZ(), 10, 2.4D, 0.8D, 2.4D, 0.04D);
        }
        if (t == INTRO_BEAT_TICK) {
            this.setAnim(CanopyAnim.INTRO_CHEST_BEAT);
        }
        // a drum on every blow of the chest-beat: the fists land every 0.2 s from 0.2 s to 2.0 s of the animation
        if (t > INTRO_BEAT_TICK && t <= INTRO_BEAT_TICK + 40 && (t - INTRO_BEAT_TICK) % 4 == 0) {
            int blow = (t - INTRO_BEAT_TICK) / 4;
            this.playSound(ModSounds.CANOPY_CHEST_BEAT.get(), 2.0F, blow % 2 == 0 ? 0.62F : 0.7F);
            level.sendParticles(leaves, this.getX(), this.getY() + 3.0D, this.getZ(), 6, 1.6D, 0.6D, 1.6D, 0.05D);
        }
        if (t == INTRO_ROAR_TICK) {
            this.setAnim(CanopyAnim.ROAR_AGGRO);
        }
        if (t == INTRO_ROAR_TICK + 6) {
            this.playSound(ModSounds.CANOPY_ROAR_ALERT.get(), 2.6F, 0.8F);
            level.sendParticles(leaves, this.getX(), this.getY() + 3.2D, this.getZ(), 40, 3.0D, 1.2D, 3.0D, 0.1D);
        }
        if (t == INTRO_SETTLE_TICK) {
            this.setAnim(CanopyAnim.IDLE_WATCH);
        }
        LivingEntity target = this.nearestPlayer(level);
        if (target != null) {
            this.faceTarget(target);
        }
        if (t >= INTRO_TICKS) {
            this.introDone = true;
            this.setActivity(Activity.GROUND);
            this.setAnim(CanopyAnim.CLUB_GUARD_IDLE);
        }
    }

    private void tickTransition(ServerLevel level) {
        this.getNavigation().stop();
        int total = this.pendingStage >= 3 ? TRANSITION_THREE_TICKS : TRANSITION_TWO_TICKS;
        if (this.pendingStage >= 3 && this.activityTimer == 39) { // the club slams the ground at 1.95 s
            this.playSound(ModSounds.CANOPY_CLUB_IMPACT.get(), 2.4F, 0.6F);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PODZOL.defaultBlockState()),
                    this.getX(), this.getY() + 0.3D, this.getZ(), 60, 2.6D, 0.3D, 2.6D, 0.14D);
        }
        if (this.activityTimer >= total) {
            this.applyStage(this.pendingStage);
            this.setActivity(Activity.GROUND);
            this.setAnim(this.pendingStage >= 3 ? CanopyAnim.FURY_IDLE : CanopyAnim.CLUB_GUARD_IDLE);
        }
    }

    private void tickStagger() {
        this.getNavigation().stop();
        if (this.activityTimer == STAGGER_TICKS - 4) {
            this.setAnim(CanopyAnim.STUMBLE);
        }
        if (this.activityTimer >= STAGGER_TICKS + 4) {
            this.setAnim(CanopyAnim.KNOCKBACK_RECOVER);
            this.setActivity(Activity.ROAR); // short recovery beat, then back to the fight
            this.activityTimer = 4;
        }
    }

    // ================================================================= GROUND
    private @Nullable LivingEntity nearestPlayer(ServerLevel level) {
        Player best = null;
        double bestDist = Double.MAX_VALUE;
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(40.0D),
                pl -> pl.isAlive() && !pl.isSpectator())) {
            double d = this.distanceToSqr(p);
            if (d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }

    /** Whoever hurt him last, otherwise the nearest valid player; keeps the current one unless a real reason appears. */
    private @Nullable LivingEntity chooseTarget(ServerLevel level) {
        LivingEntity current = this.getTarget();
        boolean ok = current != null && current.isAlive() && this.canAttack(current)
                && this.distanceToSqr(current) <= 40.0D * 40.0D;
        if (ok && this.tickCount % 20 != 0) {
            return current;
        }
        LivingEntity attacker = this.getLastHurtByMob();
        if (attacker instanceof Player p && this.tickCount - this.getLastHurtByMobTimestamp() < 100 && p.isAlive()
                && !p.isSpectator() && this.canAttack(p) && this.distanceToSqr(p) <= 40.0D * 40.0D) {
            if (p != current) {
                this.setTarget(p);
            }
            return p;
        }
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(40.0D),
                pl -> pl.isAlive() && !pl.isSpectator() && this.canAttack(pl))) {
            double d = this.distanceToSqr(p);
            if (d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        if (ok && (best == null || bestDist > this.distanceToSqr(current) - 16.0D)) {
            return current;
        }
        if (best != null && best != current) {
            this.setTarget(best);
        }
        return best != null ? best : (ok ? current : null);
    }

    private void trackTarget(LivingEntity target) {
        Vec3 now = target.position();
        this.targetVelocity = this.targetLastPos == Vec3.ZERO ? Vec3.ZERO : now.subtract(this.targetLastPos);
        if (this.targetVelocity.lengthSqr() > 1.0D) {
            this.targetVelocity = Vec3.ZERO;
        }
        this.targetLastPos = now;
    }

    public Vec3 predict(LivingEntity target, int ticks) {
        Vec3 lead = new Vec3(this.targetVelocity.x, 0.0D, this.targetVelocity.z).scale(Math.min(ticks, 20));
        return target.position().add(lead);
    }

    private void faceTarget(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        this.faceDirection(dx, dz);
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

    private void tickGround(ServerLevel level) {
        this.setNoGravity(false);
        LivingEntity target = this.chooseTarget(level);
        if (target == null) {
            this.tickNoTarget(level);
            return;
        }
        this.noTargetTicks = 0;
        this.hadTarget = true;
        if (!this.openingCalled && this.introDone) { // spawned without the idol: the call opens the fight instead
            this.openingCalled = true;
            this.callMinions(level, OPENING_MINIONS, 24);
            this.setActivity(Activity.ROAR);
            this.setAnim(CanopyAnim.TAUNT);
            this.playSound(ModSounds.CANOPY_ROAR_ALERT.get(), 2.0F, 0.85F);
            return;
        }
        this.trackTarget(target);
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double dist = Math.sqrt(this.distanceToSqr(target));
        double dy = target.getY() - this.getY();

        if (!this.isClubDrawn() && (dist < 24.0D || this.getHealth() < this.getMaxHealth())) {
            this.startDrawClub();
            return;
        }
        if (this.tauntCooldown <= 0 && this.getStage() >= 2 && dist > 6.0D && dist < 18.0D && this.random.nextInt(60) == 0) {
            this.tauntCooldown = 360;
            this.setActivity(Activity.ROAR);
            this.setAnim(CanopyAnim.TAUNT);
            this.playSound(ModSounds.CANOPY_ROAR_ALERT.get(), 1.6F, 0.9F);
            return;
        }
        Attack pick = this.pickAttack(target, dist, dy);
        if (pick != Attack.NONE) {
            this.startAttack(pick, target);
            return;
        }
        if (this.routeCooldown <= 0 && this.considerRoute(level, target, dist, dy)) {
            return;
        }
        this.chaseOnGround(level, target, dist, dy);
        this.updateLocomotionAnim(dist);
    }

    private void tickNoTarget(ServerLevel level) {
        this.noTargetTicks++;
        this.getNavigation().stop();
        if (this.hadTarget && this.noTargetTicks == 8) {
            this.hadTarget = false;
            this.setActivity(Activity.ROAR);
            this.setAnim(CanopyAnim.TARGET_LOST);
            return;
        }
        if (this.isClubDrawn() && this.noTargetTicks == 240) {
            this.setActivity(Activity.ROAR);
            this.setAnim(CanopyAnim.CLUB_STOW);
            this.activityTimer = 0;
            return;
        }
        this.idleAnimation();
    }

    private void idleAnimation() {
        this.idleTicks++;
        CanopyAnim cur = this.getAnim();
        boolean idleFamily = cur.name().startsWith("IDLE_") || cur == CanopyAnim.CLUB_GUARD_IDLE || cur == CanopyAnim.FURY_IDLE
                || cur == CanopyAnim.STOP_MOVING || cur == CanopyAnim.WALK || cur == CanopyAnim.CLUB_GUARD_WALK
                || cur == CanopyAnim.RUN_FOUR_LIMB || cur == CanopyAnim.RUN_BIPEDAL || cur == CanopyAnim.TARGET_LOST
                || cur == CanopyAnim.CLUB_STOW || cur == CanopyAnim.LAND_RECOVERY || cur == CanopyAnim.KNOCKBACK_RECOVER;
        if (!idleFamily && this.animHold < 12) {
            return;
        }
        if (this.isClubDrawn()) {
            this.setAnim(this.getStage() >= 3 ? CanopyAnim.FURY_IDLE : this.idleTicks % 400 > 300 ? CanopyAnim.CLUB_DRAG : CanopyAnim.CLUB_GUARD_IDLE);
            return;
        }
        if (cur.name().startsWith("IDLE_") && cur.loops == false && this.animHold < 36) {
            return; // let the one-shot variation finish
        }
        if (this.idleTicks % 160 == 0 && this.random.nextBoolean()) {
            CanopyAnim[] variants = {CanopyAnim.IDLE_SNIFF, CanopyAnim.IDLE_SCRATCH, CanopyAnim.IDLE_ROAR_SHORT};
            CanopyAnim v = variants[this.random.nextInt(variants.length)];
            this.setAnim(v);
            if (v == CanopyAnim.IDLE_ROAR_SHORT && this.roarCooldown <= 0) {
                this.roarCooldown = 240;
                this.playSound(ModSounds.CANOPY_AMBIENT.get(), 1.4F, 0.7F);
            }
        } else if (this.getTarget() != null || this.level().getNearestPlayer(this, 24.0D) != null) {
            this.setAnim(CanopyAnim.IDLE_WATCH);
        } else if (!cur.name().startsWith("IDLE_")) {
            this.setAnim(CanopyAnim.IDLE_BREATHING);
        } else if (cur == CanopyAnim.IDLE_WATCH) {
            this.setAnim(CanopyAnim.IDLE_BREATHING);
        } else if (!cur.loops && this.animHold >= 36) {
            this.setAnim(CanopyAnim.IDLE_BREATHING);
        }
    }

    private void updateLocomotionAnim(double distToTarget) {
        boolean moving = this.lastSpeed > 0.035D;
        CanopyAnim cur = this.getAnim();
        boolean club = this.isClubDrawn();
        CanopyAnim want;
        if (!moving) {
            if (cur.name().contains("WALK") || cur.name().contains("RUN")) {
                want = CanopyAnim.STOP_MOVING;
            } else if (cur == CanopyAnim.STOP_MOVING && this.animHold < 10) {
                return;
            } else {
                this.idleAnimation();
                return;
            }
        } else if (this.lastSpeed > 0.2D && distToTarget > 9.0D) {
            want = club ? CanopyAnim.RUN_BIPEDAL : CanopyAnim.RUN_FOUR_LIMB;
        } else if (this.lastSpeed > 0.2D) {
            want = CanopyAnim.RUN_BIPEDAL;
        } else if (cur == CanopyAnim.STOP_MOVING || cur.name().startsWith("IDLE_") || cur == CanopyAnim.CLUB_GUARD_IDLE
                || cur == CanopyAnim.FURY_IDLE) {
            want = CanopyAnim.START_MOVING;
        } else {
            want = club ? CanopyAnim.CLUB_GUARD_WALK : CanopyAnim.WALK;
        }
        if (want == CanopyAnim.START_MOVING && this.animHold < 8 && cur == want) {
            return;
        }
        if (cur == CanopyAnim.START_MOVING && this.animHold < 8) {
            return;
        }
        if (want != cur && (this.animHold >= 6 || want == CanopyAnim.START_MOVING)) {
            this.setAnim(want);
        }
        this.detectTurnAndStep();
    }

    private void detectTurnAndStep() {
        float turn = Mth.wrapDegrees(this.getYRot() - this.lastYaw);
        CanopyAnim cur = this.getAnim();
        if (Math.abs(turn) > 14.0F && this.lastSpeed < 0.12D && this.animHold >= 8 && !cur.name().startsWith("TURN")) {
            this.setAnim(turn < 0 ? CanopyAnim.TURN_LEFT : CanopyAnim.TURN_RIGHT);
        } else if (this.onGround() && this.lastSpeed > 0.04D && this.getY() - this.lastPos.y > 0.3D) {
            this.setAnim(CanopyAnim.STEP_UP);
        }
    }

    private void chaseOnGround(ServerLevel level, LivingEntity target, double dist, double dy) {
        double boost = this.getStage() >= 3 ? 0.12D : 0.0D;
        // a gorilla gallops across open ground and never plants its feet while an attack is cooling down
        double speed = 1.0D + boost + (dist > 8.0D ? 0.2D : 0.0D);
        boolean meleeReady = this.cooldowns[Attack.TRUNK_SLAM.ordinal()] <= 0 || this.cooldowns[Attack.CANOPY_SWEEP.ordinal()] <= 0;
        double standOff = meleeReady ? 2.6D : 4.6D;
        if (this.onGround() && this.horizontalCollision && this.getNavigation().isInProgress()) {
            this.getJumpControl().jump();
        }
        if (dist > standOff + 0.8D) {
            if (this.pathRecalc <= 0 || this.getNavigation().isDone()) {
                Vec3 goal = this.predict(target, Math.min(10, (int) (dist * 1.2D)));
                var path = this.getNavigation().createPath(goal.x, goal.y, goal.z, 1);
                if (path != null && path.canReach()) {
                    this.getNavigation().moveTo(path, speed);
                } else {
                    this.getNavigation().moveTo(target, speed);
                }
                this.pathRecalc = 4;
            }
        } else if (!meleeReady && this.pathRecalc <= 0) {
            // prowl around the target along a wide arc (the side flips every ~2.5 s) instead of standing still
            Vec3 away = this.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
            if (away.lengthSqr() > 1.0E-4D) {
                away = away.normalize();
                boolean left = ((this.tickCount / 50) + this.getId()) % 2 == 0;
                double a = Math.toRadians(55.0D) * (left ? 1 : -1);
                Vec3 rot = new Vec3(away.x * Math.cos(a) - away.z * Math.sin(a), 0.0D, away.x * Math.sin(a) + away.z * Math.cos(a));
                Vec3 spot = target.position().add(rot.scale(Mth.clamp(dist, 3.6D, 5.6D)));
                this.getNavigation().moveTo(spot.x, this.getY(), spot.z, 1.1D + boost);
            }
            this.pathRecalc = 5;
        } else if (meleeReady && dist > 2.2D && this.pathRecalc <= 0) {
            // an attack is ready but the target is out of its window (no line of sight, other height): keep pressing in
            this.getNavigation().moveTo(target, speed);
            this.pathRecalc = 5;
        }
        // stuck detection: not progressing towards a far target
        if (this.tickCount % 20 == 0) {
            if (dist > 8.0D && this.position().distanceToSqr(this.stuckAnchor) < 0.4D) {
                this.stuckTicks += 20;
            } else {
                this.stuckTicks = 0;
            }
            this.stuckAnchor = this.position();
        }
    }

    // ================================================================= club
    private void startDrawClub() {
        this.setActivity(Activity.DRAW_CLUB);
        this.setAnim(CanopyAnim.DRAW_CLUB_PREPARE);
        this.getNavigation().stop();
    }

    private void tickDrawClub() {
        this.getNavigation().stop();
        int t = this.activityTimer;
        if (t == DRAW_PREPARE_TICKS) {
            this.setAnim(CanopyAnim.DRAW_CLUB);
            this.playSound(ModSounds.CANOPY_CLUB_DRAW.get(), 1.6F, 0.7F);
        } else if (t == DRAW_PREPARE_TICKS + 6) {
            this.setClubDrawn(true); // the club swaps from the back to the hand mid-draw
        } else if (t == DRAW_PREPARE_TICKS + DRAW_TICKS) {
            this.setAnim(CanopyAnim.DRAW_CLUB_FINISH);
        } else if (t >= DRAW_PREPARE_TICKS + DRAW_TICKS + DRAW_FINISH_TICKS) {
            this.setActivity(Activity.GROUND);
            this.setAnim(CanopyAnim.CLUB_GUARD_IDLE);
        }
    }

    // ================================================================= attacks
    private int cooldownFor(Attack a) {
        return switch (a) {
            case TRUNK_SLAM -> 36;
            case CANOPY_SWEEP -> 32;
            case BRUTE_LUNGE -> 64;
            case BACKHAND -> 42;
            case SHIELD_BREAK -> 48;
            case INTIMIDATE -> 480;
            case CANOPY_DROP -> 200;
            case NONE -> 0;
        };
    }

    /** Weighted choice that respects range, height, cooldowns and the previous attack. */
    private Attack pickAttack(LivingEntity target, double dist, double dy) {
        if (!this.isClubDrawn() || !this.onGround()) {
            return Attack.NONE;
        }
        boolean sameLevel = dy <= 2.4D && dy >= -3.0D;
        boolean sight = this.getSensing().hasLineOfSight(target);
        List<Attack> options = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        if (sameLevel && sight) {
            if (dist <= 3.0D && this.cooldowns[Attack.BACKHAND.ordinal()] <= 0) {
                options.add(Attack.BACKHAND);
                weights.add(this.getStage() >= 3 ? 7 : 5);
            }
            if (dist <= 4.2D && this.cooldowns[Attack.TRUNK_SLAM.ordinal()] <= 0) {
                options.add(Attack.TRUNK_SLAM);
                weights.add(6);
            }
            if (dist <= 4.4D && this.cooldowns[Attack.CANOPY_SWEEP.ordinal()] <= 0) {
                options.add(Attack.CANOPY_SWEEP);
                weights.add(5);
            }
            if (dist >= 4.5D && dist <= 10.0D && this.cooldowns[Attack.BRUTE_LUNGE.ordinal()] <= 0) {
                options.add(Attack.BRUTE_LUNGE);
                weights.add(5);
            }
            if (dist >= 5.0D && dist <= 14.0D && this.cooldowns[Attack.INTIMIDATE.ordinal()] <= 0) {
                options.add(Attack.INTIMIDATE);
                weights.add(1);
            }
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
        return options.get(0);
    }

    /** Starts an attack (public for tests and for routes). Returns false if the state forbids it. */
    public boolean startAttack(Attack type, @Nullable LivingEntity target) {
        Activity a = this.getActivity();
        if (type == Attack.NONE || (a != Activity.GROUND && a != Activity.ATTACK) || !this.isAlive()) {
            return false;
        }
        if (type == Attack.CANOPY_DROP) {
            return false; // only reachable through a tree route
        }
        this.attack = type;
        this.lastAttack = type;
        this.phase = Phase.PREPARE;
        this.phaseTimer = 0;
        this.attackHitDone = false;
        this.setActivity(Activity.ATTACK);
        this.getNavigation().stop();
        if (target != null) {
            this.faceTarget(target);
            this.sweepLeft = this.targetIsLeft(target);
        }
        switch (type) {
            case TRUNK_SLAM -> this.setAnim(this.random.nextBoolean() ? CanopyAnim.ATTACK_TRUNK_SLAM_PREPARE : CanopyAnim.ATTACK_TRUNK_SLAM_PREPARE_ALT);
            case CANOPY_SWEEP -> this.setAnim(this.sweepLeft ? CanopyAnim.ATTACK_CANOPY_SWEEP_PREPARE_LEFT : CanopyAnim.ATTACK_CANOPY_SWEEP_PREPARE_RIGHT);
            case BRUTE_LUNGE -> this.setAnim(CanopyAnim.ATTACK_BRUTE_LUNGE_PREPARE);
            case BACKHAND -> this.setAnim(CanopyAnim.ATTACK_BACKHAND_PREPARE);
            case SHIELD_BREAK -> this.setAnim(CanopyAnim.ATTACK_BACKHAND_PREPARE);  // Use same prepare as backhand
            case INTIMIDATE -> {
                this.setAnim(CanopyAnim.INTIMIDATE_ROAR);
                this.playSound(ModSounds.CANOPY_ROAR_ALERT.get(), 2.4F, 0.7F);
            }
            default -> {
            }
        }
        if (type != Attack.INTIMIDATE && type != Attack.BACKHAND && type != Attack.SHIELD_BREAK && this.roarCooldown <= 0) {
            this.roarCooldown = 100;
            this.playSound(ModSounds.CANOPY_ROAR_ATTACK.get(), 1.7F, 0.9F);
        }
        return true;
    }

    private boolean targetIsLeft(LivingEntity target) {
        Vec3 to = target.position().subtract(this.position());
        Vec3 fwd = Vec3.directionFromRotation(0.0F, this.getYRot());
        return fwd.x * to.z - fwd.z * to.x > 0.0D; // cross product sign: which side of the facing the target is on
    }

    private int prepTicks() {
        return switch (this.attack) {
            case TRUNK_SLAM -> SLAM_PREP;
            case CANOPY_SWEEP -> SWEEP_PREP;
            case BRUTE_LUNGE -> LUNGE_PREP;
            case BACKHAND -> BACKHAND_PREP;
            case SHIELD_BREAK -> SHIELD_BREAK_PREP;
            case INTIMIDATE -> INTIMIDATE_TICKS;
            default -> 1;
        };
    }

    private int execTicks() {
        return switch (this.attack) {
            case TRUNK_SLAM -> SLAM_EXEC;
            case CANOPY_SWEEP -> SWEEP_EXEC;
            case BRUTE_LUNGE -> LUNGE_EXEC;
            case BACKHAND -> BACKHAND_EXEC;
            case SHIELD_BREAK -> SHIELD_BREAK_EXEC;
            default -> 1;
        };
    }

    private int recoverTicks() {
        return switch (this.attack) {
            case TRUNK_SLAM -> SLAM_RECOVER;
            case CANOPY_SWEEP -> SWEEP_RECOVER;
            case BRUTE_LUNGE -> LUNGE_RECOVER;
            case BACKHAND -> BACKHAND_RECOVER;
            case SHIELD_BREAK -> SHIELD_BREAK_RECOVER;
            default -> 1;
        };
    }

    /** Whether the attack can still land on the target as it stands now: the "never swing at the air" rule. */
    private boolean attackStillUseful(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        Vec3 d = target.position().subtract(this.position());
        double horizontal = Math.sqrt(d.x * d.x + d.z * d.z);
        boolean sameLevel = d.y <= 2.8D && d.y >= -3.2D;
        return switch (this.attack) {
            case TRUNK_SLAM -> sameLevel && horizontal <= SLAM_RADIUS + 2.4D;
            case CANOPY_SWEEP -> sameLevel && horizontal <= SWEEP_REACH + 2.2D;
            case BRUTE_LUNGE -> sameLevel && horizontal <= 12.5D && this.getSensing().hasLineOfSight(target);
            case BACKHAND -> sameLevel && horizontal <= BACKHAND_REACH + 1.6D;
            case SHIELD_BREAK -> sameLevel && horizontal <= SHIELD_BREAK_REACH + 1.8D;
            case INTIMIDATE -> horizontal <= 20.0D;
            default -> true;
        };
    }

    /** Drops the current attack; {@code interrupted} plays the recoil animation. */
    private void cancelAttack(boolean interrupted) {
        if (this.attack != Attack.NONE && this.getActivity() == Activity.ATTACK) {
            this.cooldowns[this.attack.ordinal()] = Math.max(this.cooldowns[this.attack.ordinal()], 24);
            if (interrupted) {
                this.setAnim(CanopyAnim.ATTACK_INTERRUPTED);
            }
        }
        this.attack = Attack.NONE;
        this.phaseTimer = 0;
    }

    /** Drops the current attack at once and returns to the ground state (used when a fight is reset). */
    public void abortAttack() {
        this.cancelAttack(false);
        if (this.getActivity() == Activity.ATTACK) {
            this.setActivity(Activity.GROUND);
        }
    }

    private void tickAttack(ServerLevel level) {
        LivingEntity target = this.getTarget();
        if (target != null) {
            this.trackTarget(target);
        }
        this.phaseTimer++;
        switch (this.phase) {
            case PREPARE -> {
                this.getNavigation().stop();
                int prep = this.prepTicks();
                if (target != null && this.attack != Attack.INTIMIDATE && this.phaseTimer < prep - 3) {
                    this.faceTarget(target);
                    if (this.attack == Attack.TRUNK_SLAM || this.attack == Attack.CANOPY_SWEEP) {
                        this.stepTowards(target, 0.07D, 2.4D);
                    }
                }
                if (this.attack == Attack.INTIMIDATE) {
                    this.tickIntimidate(level);
                    if (this.phaseTimer >= prep) {
                        this.finishAttack();
                    }
                    return;
                }
                this.telegraph(level);
                if (this.phaseTimer >= prep) {
                    if (!this.attackStillUseful(target)) {
                        this.cancelAttack(true);
                        this.setActivity(Activity.GROUND);
                        this.cooldowns[this.lastAttack.ordinal()] = 24;
                        return;
                    }
                    this.beginExecute(target);
                }
            }
            case EXECUTE -> this.tickExecute(level, target);
            case RECOVER -> {
                this.getNavigation().stop();
                if (this.phaseTimer == 2 && this.attack == Attack.BRUTE_LUNGE) {
                    this.playSound(ModSounds.CANOPY_PANT.get(), 1.2F, 0.8F);
                }
                if (this.phaseTimer >= this.recoverTicks()) {
                    this.finishAttack();
                }
            }
        }
    }

    private void finishAttack() {
        this.cooldowns[this.attack.ordinal()] = this.cooldownFor(this.attack);
        this.attack = Attack.NONE;
        this.setActivity(Activity.GROUND);
        this.setAnim(this.getStage() >= 3 ? CanopyAnim.FURY_IDLE : CanopyAnim.CLUB_RECOVER_GRIP);
    }

    private void stepTowards(LivingEntity target, double speed, double stopDist) {
        Vec3 d = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (this.onGround() && d.lengthSqr() > stopDist * stopDist) {
            Vec3 s = d.normalize().scale(speed);
            this.setDeltaMovement(s.x, this.getDeltaMovement().y, s.z);
        }
    }

    private void telegraph(ServerLevel level) {
        if (this.phaseTimer % 3 != 0) {
            return;
        }
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        if (this.attack == Attack.TRUNK_SLAM) {
            Vec3 c = this.position().add(f.scale(2.6D));
            for (int i = 0; i < 10; i++) {
                double a = Math.PI * 2.0D * i / 10.0D;
                level.sendParticles(MARK_DUST, c.x + Math.cos(a) * SLAM_RADIUS, c.y + 0.1D, c.z + Math.sin(a) * SLAM_RADIUS, 1, 0, 0, 0, 0);
            }
        } else if (this.attack == Attack.BRUTE_LUNGE) {
            for (int k = 1; k <= 6; k++) {
                Vec3 p = this.position().add(f.scale(k * 1.8D));
                level.sendParticles(MARK_DUST, p.x, p.y + 0.1D, p.z, 1, 0.3D, 0, 0.3D, 0);
            }
        }
    }

    private void beginExecute(@Nullable LivingEntity target) {
        this.phase = Phase.EXECUTE;
        this.phaseTimer = 0;
        this.attackHitDone = false;
        switch (this.attack) {
            case TRUNK_SLAM -> this.setAnim(CanopyAnim.ATTACK_TRUNK_SLAM_EXECUTE);
            case CANOPY_SWEEP -> {
                this.setAnim(this.sweepLeft ? CanopyAnim.ATTACK_CANOPY_SWEEP_EXECUTE_LEFT : CanopyAnim.ATTACK_CANOPY_SWEEP_EXECUTE_RIGHT);
                this.playSound(ModSounds.CANOPY_CLUB_SWING.get(), 1.6F, 0.8F);
            }
            case BRUTE_LUNGE -> {
                this.setAnim(CanopyAnim.ATTACK_BRUTE_LUNGE_EXECUTE);
                Vec3 dir = target != null ? target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D) : Vec3.directionFromRotation(0.0F, this.getYRot());
                this.lungeDir = dir.lengthSqr() > 1.0E-4D ? dir.normalize() : Vec3.directionFromRotation(0.0F, this.getYRot());
                this.faceDirection(this.lungeDir.x, this.lungeDir.z);
                this.playSound(ModSounds.CANOPY_JUMP.get(), 1.4F, 0.8F);
            }
            case BACKHAND -> {
                this.setAnim(CanopyAnim.ATTACK_BACKHAND_EXECUTE);
                this.playSound(ModSounds.CANOPY_CLUB_SWING.get(), 1.3F, 1.0F);
            }
            default -> {
            }
        }
    }

    private void tickExecute(ServerLevel level, @Nullable LivingEntity target) {
        switch (this.attack) {
            case TRUNK_SLAM -> {
                if (this.phaseTimer == SLAM_HIT_TICK) {
                    this.slamImpact(level);
                }
            }
            case CANOPY_SWEEP -> {
                if (this.phaseTimer == SWEEP_HIT_TICK) {
                    this.sweepImpact(level);
                }
            }
            case BRUTE_LUNGE -> {
                if (this.phaseTimer <= 8 && this.onGround()) {
                    this.setDeltaMovement(this.lungeDir.x * LUNGE_SPEED, this.getDeltaMovement().y, this.lungeDir.z * LUNGE_SPEED);
                }
                if (!this.attackHitDone) {
                    for (LivingEntity v : this.victimsInFront(level, 3.0D, 100.0D)) {
                        this.attackHitDone = true;
                        this.strike(level, v, LUNGE_DAMAGE, 0.9D);
                    }
                    if (this.attackHitDone) {
                        this.playSound(ModSounds.CANOPY_CLUB_IMPACT.get(), 1.6F, 0.9F);
                    }
                }
            }
            case BACKHAND -> {
                if (this.phaseTimer == BACKHAND_HIT_TICK) {
                    for (LivingEntity v : this.victimsInFront(level, BACKHAND_REACH, 200.0D)) {
                        this.strike(level, v, BACKHAND_DAMAGE, 0.7D);
                    }
                    this.playSound(ModSounds.CANOPY_CLUB_IMPACT.get(), 1.2F, 1.1F);
                }
            }
            default -> {
            }
        }
        if (this.phaseTimer >= this.execTicks()) {
            this.phase = Phase.RECOVER;
            this.phaseTimer = 0;
            this.setAnim(switch (this.attack) {
                case TRUNK_SLAM -> CanopyAnim.ATTACK_TRUNK_SLAM_RECOVERY;
                case CANOPY_SWEEP -> CanopyAnim.ATTACK_CANOPY_SWEEP_RECOVERY;
                case BRUTE_LUNGE -> CanopyAnim.ATTACK_BRUTE_LUNGE_RECOVERY;
                default -> CanopyAnim.ATTACK_BACKHAND_RECOVERY;
            });
        }
    }

    private void tickIntimidate(ServerLevel level) {
        if (this.phaseTimer == 10) {
            level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 2.0D, 0.5D, 2.0D, 0.08D);
            for (LivingEntity v : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(9.0D, 2.0D, 9.0D),
                    e -> e != this && e.isAlive() && this.canAttack(e))) {
                Vec3 away = v.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
                if (away.lengthSqr() > 1.0E-4D) {
                    away = away.normalize();
                    v.knockback(0.45D, -away.x, -away.z); // a shove of air, no damage and no loss of input
                }
            }
        }
    }

    private void slamImpact(ServerLevel level) {
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 c = this.position().add(f.scale(2.6D));
        this.playSound(ModSounds.CANOPY_CLUB_IMPACT.get(), 2.0F, 0.75F);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PODZOL.defaultBlockState()), c.x, c.y + 0.3D, c.z, 36, 1.6D, 0.2D, 1.6D, 0.12D);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_LEAVES.defaultBlockState()), c.x, c.y + 1.2D, c.z, 18, 1.4D, 0.6D, 1.4D, 0.06D);
        for (LivingEntity v : this.victimsAround(level, c, SLAM_RADIUS)) {
            this.strike(level, v, SLAM_DAMAGE, 0.8D);
        }
    }

    private void sweepImpact(ServerLevel level) {
        this.playSound(ModSounds.CANOPY_CLUB_IMPACT.get(), 1.5F, 0.95F);
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        for (int i = -6; i <= 6; i++) {
            double ang = i / 6.0D * 1.2D;
            Vec3 p = this.position().add(f.scale(Math.cos(ang) * 3.4D)).add(new Vec3(-f.z, 0, f.x).scale(Math.sin(ang) * 3.4D));
            level.sendParticles(BARK_DUST, p.x, p.y + 1.3D, p.z, 1, 0.05D, 0.05D, 0.05D, 0);
        }
        for (LivingEntity v : this.victimsInFront(level, SWEEP_REACH, 140.0D)) {
            this.strike(level, v, SWEEP_DAMAGE, 0.7D);
        }
    }

    /** Targets in a frontal cone of {@code arcDegrees} and within {@code reach} blocks, in line of sight. */
    private List<LivingEntity> victimsInFront(ServerLevel level, double reach, double arcDegrees) {
        Vec3 f = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        double minDot = Math.cos(Math.toRadians(arcDegrees / 2.0D));
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(reach, 1.5D, reach),
                en -> en != this && en.isAlive() && this.canAttack(en))) {
            Vec3 d = e.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            double h = d.length();
            double edge = h - e.getBbWidth() / 2.0D - this.getBbWidth() / 2.0D;
            boolean sameLevel = e.getY() - this.getY() <= 2.6D && e.getY() - this.getY() >= -2.6D;
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

    private void strike(ServerLevel level, LivingEntity victim, float damage, double knock) {
        victim.hurtServer(level, this.damageSources().mobAttack(this), damage);
        Vec3 away = victim.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (away.lengthSqr() > 1.0E-4D) {
            away = away.normalize();
            victim.knockback(knock, -away.x, -away.z);
        }
    }

    // ================================================================= tree routes
    /** Decides whether a tree route is worth it now; if so plans it and starts the approach. */
    private boolean considerRoute(ServerLevel level, LivingEntity target, double dist, double dy) {
        boolean elevated = dy >= 3.5D;
        boolean flank = this.getStage() >= 2 && this.cooldowns[Attack.CANOPY_DROP.ordinal()] <= 0 && dist >= 6.0D && dist <= 20.0D && dy < 3.5D && this.random.nextInt(this.getStage() >= 3 ? 40 : 70) == 0;
        boolean stuck = this.stuckTicks >= 60;
        if (!elevated && !flank && !stuck) {
            return false;
        }
        Route planned = this.planRoute(level, target, elevated || stuck && dy > 1.5D);
        this.routeCooldown = planned == null ? 120 : 260;
        if (planned == null) {
            return false;
        }
        this.beginRoute(planned);
        return true;
    }

    /** Plans climb, perch and jump; null when any link is impossible (no teleporting to make it work). */
    public @Nullable Route planRoute(ServerLevel level, LivingEntity target, boolean reposition) {
        Vec3 from = this.position();
        Vec3 targetPos = target.position();
        Trunk trunk = TreeNav.bestTrunk(level, from, targetPos, MAX_TRUNK_SEARCH, 6, CLIMB_WIDTH);
        if (trunk == null) {
            return this.routeFailed("no usable trunk");
        }
        Vec3 approach = TreeNav.approachSpot(level, trunk, from, CLIMB_WIDTH);
        Vec3 perch = TreeNav.findPerch(level, trunk, 3, WIDTH, HEIGHT);
        if (approach == null || perch == null || perch.y - approach.y < 3.0D) {
            return this.routeFailed("no approach or perch (approach=" + approach + ", perch=" + perch + ")");
        }
        var path = this.getNavigation().createPath(approach.x, approach.y, approach.z, 1);
        if (path == null || !path.canReach()) {
            return this.routeFailed("approach spot unreachable on foot");
        }
        boolean drop = !reposition;
        Vec3 landing = TreeNav.findLanding(level, this.predict(target, 20), drop ? 2.5D : 3.0D, WIDTH, HEIGHT, reposition);
        if (landing == null) {
            return this.routeFailed("no landing near the target");
        }
        double gravity = this.getAttributeValue(Attributes.GRAVITY);
        JumpPlan plan = TreeNav.planJump(level, perch, landing, gravity, WIDTH, HEIGHT);
        if (plan == null) {
            return this.routeFailed("no valid jump from " + perch + " to " + landing);
        }
        Route r = new Route(drop, trunk, approach, perch, landing, TreeNav.faceOf(trunk, approach));
        r.plan = plan;
        this.routeFailure = "";
        return r;
    }

    private @Nullable Route routeFailed(String reason) {
        this.routeFailure = reason;
        return null;
    }

    /** Why the last route plan was rejected (empty when it succeeded); for tests and diagnostics. */
    public String getRouteFailure() {
        return this.routeFailure;
    }

    public boolean startRoute(ServerLevel level, LivingEntity target, boolean reposition) {
        Route r = this.planRoute(level, target, reposition);
        if (r == null) {
            return false;
        }
        this.beginRoute(r);
        return true;
    }

    private String routeFailure = "";
    private String gripLossReason = "";

    /** Why the last climb was abandoned (empty if never); for tests and diagnostics. */
    public String getGripLossReason() {
        return this.gripLossReason;
    }

    private void beginRoute(Route r) {
        this.route = r;
        this.routeTicks = 0;
        this.branchTicks = 0;
        this.routesStarted++;
        this.setActivity(Activity.CLIMB_APPROACH);
        this.setAnim(CanopyAnim.CLIMB_APPROACH);
        this.getNavigation().moveTo(r.approach.x, r.approach.y, r.approach.z, 1.2D);
    }

    /**
     * Abandons the route. With {@code descend} (or when standing on the canopy) he grabs the trunk again and climbs down it:
     * the fall back to the ground is never a teleport.
     */
    private void abortRoute(boolean descend) {
        this.routesAborted++;
        this.routeCooldown = 160;
        Activity a = this.getActivity();
        boolean onCanopy = a == Activity.BRANCH || a == Activity.JUMP_PREPARATION;
        if (this.route != null && (descend || onCanopy) && (this.isClimbing() || onCanopy)) {
            if (!this.isClimbing()) {
                this.beginClimb();
            }
            this.branchTicks = -1; // descending
            this.setActivity(Activity.CLING);
            this.setAnim(CanopyAnim.JUMP_ABORT);
            return;
        }
        if (a == Activity.JUMP_PREPARATION) {
            this.setAnim(CanopyAnim.JUMP_ABORT);
        }
        this.releaseClimb();
        this.route = null;
        this.setActivity(Activity.GROUND);
    }

    public boolean isClimbing() {
        return this.noPhysics || this.getPose() == Pose.CROUCHING;
    }

    private void beginClimb() {
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setPose(Pose.CROUCHING);
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();
    }

    /** Hands the body back to normal physics (gravity, collisions, standing size). */
    public void releaseClimb() {
        boolean wasClimbing = this.isClimbing();
        this.noPhysics = false;
        this.setNoGravity(false);
        if (this.getPose() == Pose.CROUCHING) {
            this.setPose(Pose.STANDING);
        }
        if (wasClimbing && this.level() instanceof ServerLevel level) {
            this.pushOffTrunk(level);
        }
    }

    /** Back at full size next to the trunk the body would overlap it: step away from the bark, never into blocks. */
    private void pushOffTrunk(ServerLevel level) {
        if (TreeNav.canOccupy(level, this.getBoundingBox(), false)) {
            return;
        }
        List<Direction> dirs = new ArrayList<>();
        if (this.route != null) {
            dirs.add(this.route.face);
        }
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (!dirs.contains(d)) {
                dirs.add(d);
            }
        }
        for (double step = 0.1D; step <= 1.6D; step += 0.1D) {
            for (Direction d : dirs) {
                Vec3 p = this.position().add(d.getStepX() * step, 0.0D, d.getStepZ() * step);
                if (TreeNav.canOccupy(level, TreeNav.boxAt(p, WIDTH, HEIGHT), false)) {
                    this.setPos(p.x, p.y, p.z);
                    return;
                }
            }
        }
    }

    private void tickClimbApproach(ServerLevel level) {
        Route r = this.route;
        if (r == null) {
            this.abortRoute(false);
            return;
        }
        this.routeTicks++;
        Vec3 d = r.approach.subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (this.activityTimer > 100 || this.routeTicks > MAX_ROUTE_TICKS) {
            this.abortRoute(false);
            return;
        }
        if (d.lengthSqr() < 1.2D * 1.2D && Math.abs(this.getY() - r.approach.y) < 1.2D) {
            // arrived: hands catch the bark, then the climb starts
            this.getNavigation().stop();
            this.setAnim(CanopyAnim.CLIMB_GRAB);
            this.faceDirection(-r.face.getStepX(), -r.face.getStepZ());
            this.setActivity(Activity.CLING);
            this.activityTimer = 0;
            this.playSound(ModSounds.CANOPY_CLIMB.get(), 1.4F, 0.8F);
            this.beginClimb();
            this.setPos(r.approach.x, this.getY(), r.approach.z);
            return;
        }
        if (this.pathRecalc <= 0 || this.getNavigation().isDone()) {
            this.getNavigation().moveTo(r.approach.x, r.approach.y, r.approach.z, 1.25D);
            this.pathRecalc = 8;
        }
    }

    /** Pause on the trunk (after the grab, or to re-grip mid-climb): then continue up or down. */
    private void tickCling(ServerLevel level) {
        Route r = this.route;
        if (r == null) {
            this.releaseClimb();
            this.setActivity(Activity.GROUND);
            return;
        }
        this.faceDirection(-r.face.getStepX(), -r.face.getStepZ());
        if (!this.hasGrip(level, r, this.getY())) {
            this.loseGrip(level, "the trunk was gone while he clung to it at y=" + this.getY());
            return;
        }
        if (this.activityTimer == 10) {
            this.setAnim(CanopyAnim.CLING_IDLE);
        }
        boolean descending = this.branchTicks < 0;
        if (this.activityTimer >= 14) {
            this.setActivity(descending ? Activity.CLIMB_DOWN : Activity.CLIMB_UP);
            this.setAnim(descending ? CanopyAnim.CLIMB_DOWN : CanopyAnim.CLIMB_UP);
        }
    }

    private boolean climbStep(ServerLevel level, Vec3 newFeet) {
        AABB box = TreeNav.boxAt(newFeet, CLIMB_WIDTH, CLIMB_HEIGHT);
        if (!TreeNav.canOccupy(level, box, true)) {
            return false;
        }
        this.setPos(newFeet.x, newFeet.y, newFeet.z);
        return true;
    }

    /** The trunk must still be there beside him, otherwise he loses his grip. */
    private boolean hasGrip(ServerLevel level, Route r, double feetY) {
        int y = (int) Math.floor(feetY + 1.0D);
        if (y > r.trunk.topY()) {
            return true; // hands above the trunk's top: only the last steps onto the perch
        }
        return TreeNav.isLog(level.getBlockState(new BlockPos(r.trunk.base().getX(), Math.max(y, r.trunk.base().getY()), r.trunk.base().getZ())));
    }

    private void tickClimbUp(ServerLevel level) {
        Route r = this.route;
        if (r == null) {
            this.releaseClimb();
            this.setActivity(Activity.GROUND);
            return;
        }
        this.routeTicks++;
        this.faceDirection(-r.face.getStepX(), -r.face.getStepZ());
        if (this.routeTicks > MAX_ROUTE_TICKS) {
            this.loseGrip(level, "route took too long");
            return;
        }
        if (this.activityTimer > 0 && this.activityTimer % 34 == 0 && this.getY() < r.perch.y - 2.5D) {
            this.setActivity(Activity.CLING); // re-grip pause
            this.branchTicks = 0;
            this.setAnim(CanopyAnim.CLING_REPOSITION);
            return;
        }
        if (!this.hasGrip(level, r, this.getY())) {
            this.loseGrip(level, "no log beside him at y=" + this.getY());
            return;
        }
        if (this.tickCount % 6 == 0) {
            this.playSound(ModSounds.CANOPY_CLIMB.get(), 0.7F, 0.9F);
        }
        double ny = Math.min(r.perch.y, this.getY() + CLIMB_SPEED);
        boolean topReached = ny >= r.perch.y - 1.0E-3D;
        Vec3 target;
        if (topReached) {
            // last stretch: slide sideways over the top onto the perch, through foliage but never through logs
            Vec3 toPerch = new Vec3(r.perch.x - this.getX(), 0.0D, r.perch.z - this.getZ());
            double len = toPerch.length();
            if (len < 0.25D) {
                this.finishClimbUp(level);
                return;
            }
            Vec3 step = toPerch.scale(Math.min(len, CLIMB_SPEED) / len);
            target = new Vec3(this.getX() + step.x, ny, this.getZ() + step.z);
        } else {
            target = new Vec3(this.getX(), ny, this.getZ());
        }
        if (!this.climbStep(level, target)) {
            this.loseGrip(level, "blocked going up at " + this.position() + " towards " + target);
        }
    }

    private void finishClimbUp(ServerLevel level) {
        Route r = this.route;
        if (r == null) {
            return;
        }
        Vec3 perch = r.perch;
        this.setPos(perch.x, perch.y, perch.z);
        this.noPhysics = false;
        this.setNoGravity(false);
        this.setPose(Pose.STANDING);
        if (!TreeNav.canOccupy(level, this.getBoundingBox(), false) || !TreeNav.hasSupportBelow(level, this.position())) {
            this.loseGrip(level, "the perch is not free or has no support");
            return;
        }
        this.setDeltaMovement(Vec3.ZERO);
        this.setActivity(Activity.BRANCH);
        this.setAnim(CanopyAnim.BRANCH_BALANCE);
        this.branchTicks = 0;
    }

    /** The grip failed (log gone, blocked, too long): fall with a scream instead of reappearing anywhere. */
    private void loseGrip(ServerLevel level, String reason) {
        this.gripLossReason = reason;
        this.releaseClimb();
        this.routesAborted++;
        this.routeCooldown = 200;
        this.route = null;
        this.setActivity(Activity.FALL_RECOVERY);
        this.activityTimer = 0;
        this.setAnim(CanopyAnim.CLIMB_FAILED);
        this.playSound(ModSounds.CANOPY_ROAR_ATTACK.get(), 1.8F, 1.1F);
    }

    private void tickFallRecovery(ServerLevel level) {
        this.getNavigation().stop();
        if (!this.onGround()) {
            if (this.activityTimer == 16) {
                this.setAnim(CanopyAnim.FALL);
            }
            this.activityTimer = Math.min(this.activityTimer, 40);
            return;
        }
        if (this.getAnim() != CanopyAnim.LAND_RECOVERY && this.getAnim() != CanopyAnim.STAGGER) {
            this.setAnim(CanopyAnim.LAND_RECOVERY);
            this.playSound(ModSounds.CANOPY_LAND_HEAVY.get(), 1.8F, 0.8F);
            this.activityTimer = 20;
        }
        if (this.activityTimer >= 20 + LAND_RECOVERY_TICKS) {
            this.setActivity(Activity.GROUND);
        }
    }

    private void tickClimbDown(ServerLevel level) {
        Route r = this.route;
        if (r == null) {
            this.releaseClimb();
            this.setActivity(Activity.GROUND);
            return;
        }
        this.faceDirection(-r.face.getStepX(), -r.face.getStepZ());
        Vec3 attach = TreeNav.attachPosition(r.trunk, r.face, CLIMB_WIDTH, this.getY());
        // first slide back to the trunk's face, then go down it
        double dx = attach.x - this.getX();
        double dz = attach.z - this.getZ();
        double lateral = Math.sqrt(dx * dx + dz * dz);
        Vec3 next;
        if (lateral > 0.12D) {
            double s = Math.min(lateral, CLIMB_SPEED);
            next = new Vec3(this.getX() + dx / lateral * s, this.getY(), this.getZ() + dz / lateral * s);
        } else {
            next = new Vec3(this.getX(), Math.max(r.trunk.base().getY(), this.getY() - CLIMB_SPEED), this.getZ());
        }
        if (!this.climbStep(level, next)) {
            this.loseGrip(level, "blocked going down at " + this.position());
            return;
        }
        if (this.tickCount % 6 == 0) {
            this.playSound(ModSounds.CANOPY_CLIMB.get(), 0.7F, 0.8F);
        }
        if (this.getY() <= r.trunk.base().getY() + 0.02D && lateral <= 0.12D) {
            this.releaseClimb();
            this.route = null;
            this.setActivity(Activity.LAND_RECOVERY);
            this.setAnim(CanopyAnim.JUMP_LAND_LIGHT);
        }
    }

    /** Fighting and waiting on top of the canopy: steer along it, line up the jump. */
    private void tickBranch(ServerLevel level) {
        Route r = this.route;
        if (r == null) {
            this.setActivity(Activity.GROUND);
            return;
        }
        this.branchTicks++;
        this.getNavigation().stop();
        LivingEntity target = this.getTarget();
        if (target != null) {
            this.trackTarget(target);
            this.faceTarget(target);
            // keep the landing fresh: the target moves while he climbs
            Vec3 landing = TreeNav.findLanding(level, this.predict(target, 24), r.drop ? 2.5D : 3.0D, WIDTH, HEIGHT, !r.drop);
            if (landing != null) {
                r.landing = landing;
            }
        }
        if (this.branchTicks == 4 || this.branchTicks % 40 == 0) {
            this.setAnim(CanopyAnim.BRANCH_BALANCE);
        }
        if (this.branchTicks >= 14) {
            this.setActivity(Activity.JUMP_PREPARATION);
            this.setAnim(r.drop ? CanopyAnim.CANOPY_DROP_PREPARE : CanopyAnim.BRANCH_CROUCH);
            this.jumpTicks = 0;
            this.playSound(ModSounds.CANOPY_ROAR_ATTACK.get(), 1.8F, 0.85F);
        }
        if (this.branchTicks > 220) {
            this.abortRoute(true);
        }
    }

    private void tickJumpPreparation(ServerLevel level) {
        Route r = this.route;
        if (r == null) {
            this.setActivity(Activity.GROUND);
            return;
        }
        this.jumpTicks++;
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        double gravity = this.getAttributeValue(Attributes.GRAVITY);
        JumpPlan plan = TreeNav.planJump(level, this.position(), r.landing, gravity, WIDTH, HEIGHT);
        if (plan == null) { // the destination became invalid: cancel instead of leaping into walls
            r.plan = null;
            this.abortRoute(true);
            return;
        }
        r.plan = plan;
        if (r.drop && this.jumpTicks % 3 == 0) { // ground marker for the Canopy Drop
            for (int i = 0; i < 10; i++) {
                double a = Math.PI * 2.0D * i / 10.0D;
                level.sendParticles(MARK_DUST, r.landing.x + Math.cos(a) * DROP_RADIUS, r.landing.y + 0.1D, r.landing.z + Math.sin(a) * DROP_RADIUS, 1, 0, 0, 0, 0);
            }
        }
        int prep = r.drop ? DROP_PREP : 10;
        if (this.jumpTicks >= prep) {
            this.launch(level, r, plan);
        }
    }

    private void launch(ServerLevel level, Route r, JumpPlan plan) {
        this.setActivity(Activity.AIRBORNE);
        this.airTicks = 0;
        this.jumpStartY = this.getY();
        this.steered = false;
        this.setOnGround(false);
        this.setDeltaMovement(plan.velocity());
        this.setAnim(r.drop ? CanopyAnim.CANOPY_DROP_AIRBORNE : CanopyAnim.BRANCH_LAUNCH);
        this.playSound(ModSounds.CANOPY_JUMP.get(), 1.8F, 0.8F);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_LEAVES.defaultBlockState()),
                this.getX(), this.getY() + 0.3D, this.getZ(), 30, 1.2D, 0.3D, 1.2D, 0.1D);
        this.faceDirection(plan.velocity().x, plan.velocity().z);
    }

    private void tickAirborne(ServerLevel level) {
        this.airTicks++;
        Route r = this.route;
        this.getNavigation().stop();
        if (this.airTicks == 5 && r != null && !r.drop) {
            this.setAnim(CanopyAnim.JUMP_AIRBORNE);
        }
        if (r != null && r.plan != null && this.airTicks < r.plan.ticks() - 2) {
            // steer: aim the remaining flight at the landing, so drag and tiny collisions cannot make him miss
            int remaining = Math.max(1, r.plan.ticks() - this.airTicks);
            Vec3 d = r.landing.subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            double needed = TreeNav.requiredHorizontalSpeed(d.length(), remaining);
            Vec3 v = this.getDeltaMovement();
            Vec3 want = d.lengthSqr() > 1.0E-4D ? d.normalize().scale(Math.min(needed, TreeNav.MAX_HORIZONTAL_SPEED)) : Vec3.ZERO;
            Vec3 cur = new Vec3(v.x, 0.0D, v.z);
            Vec3 diff = want.subtract(cur);
            if (diff.length() > 0.06D) {
                this.steered = true;
                if (!r.drop) {
                    this.setAnim(CanopyAnim.JUMP_STEER);
                }
            }
            Vec3 adj = cur.add(diff.scale(0.35D));
            this.setDeltaMovement(adj.x, v.y, adj.z);
            this.faceDirection(adj.x, adj.z);
        }
        if (this.airTicks >= 4 && this.onGround()) {
            this.setActivity(Activity.LANDING);
            return;
        }
        if (this.airTicks > 90) { // never trapped in the air
            this.setActivity(Activity.LANDING);
        }
    }

    private void tickLanding(ServerLevel level) {
        Route r = this.route;
        boolean heavy = this.jumpStartY - this.getY() > 2.0D || (r != null && r.drop);
        if (r != null && this.position().distanceTo(r.perch) < 2.5D && Math.abs(this.getY() - r.perch.y) < 1.0D) {
            // the leap was stopped by foliage or a wall and he is back on the perch: try once more, then climb down
            r.failedJumps++;
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
            if (r.failedJumps >= 2) {
                this.abortRoute(true);
            } else {
                this.setActivity(Activity.BRANCH);
                this.branchTicks = 0;
                this.setAnim(CanopyAnim.BRANCH_BALANCE);
            }
            return;
        }
        BlockPos below = BlockPos.containing(this.getX(), this.getY() - 0.2D, this.getZ());
        boolean onLog = TreeNav.isLog(level.getBlockState(below));
        this.jumpsCompleted++;
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        if (r != null && r.drop) {
            this.dropsLanded++;
            this.setAnim(CanopyAnim.CANOPY_DROP_IMPACT);
            this.dropImpact(level);
            this.route = null;
            this.setActivity(Activity.LAND_RECOVERY);
            this.setAnim(CanopyAnim.CANOPY_DROP_IMPACT);
            return;
        }
        this.setAnim(onLog ? CanopyAnim.BRANCH_GRAB : heavy ? CanopyAnim.JUMP_LAND_HEAVY : CanopyAnim.JUMP_LAND_LIGHT);
        this.playSound(heavy ? ModSounds.CANOPY_LAND_HEAVY.get() : ModSounds.CANOPY_LAND_LIGHT.get(), heavy ? 1.8F : 1.2F, 0.85F);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_LEAVES.defaultBlockState()),
                this.getX(), this.getY() + 0.3D, this.getZ(), heavy ? 36 : 16, 1.2D, 0.3D, 1.2D, 0.08D);
        this.route = null;
        this.setActivity(Activity.LAND_RECOVERY);
    }

    private void dropImpact(ServerLevel level) {
        this.playSound(ModSounds.CANOPY_CLUB_IMPACT.get(), 2.4F, 0.65F);
        this.playSound(ModSounds.CANOPY_LAND_HEAVY.get(), 2.0F, 0.7F);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PODZOL.defaultBlockState()),
                this.getX(), this.getY() + 0.3D, this.getZ(), 60, DROP_RADIUS * 0.5D, 0.3D, DROP_RADIUS * 0.5D, 0.16D);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_LEAVES.defaultBlockState()),
                this.getX(), this.getY() + 1.4D, this.getZ(), 40, 1.6D, 0.8D, 1.6D, 0.07D);
        for (LivingEntity v : this.victimsAround(level, this.position(), DROP_RADIUS)) {
            this.strike(level, v, DROP_DAMAGE, 1.0D); // once per target: this method runs exactly once per landing
        }
    }

    private void tickLandRecovery(ServerLevel level) {
        this.getNavigation().stop();
        if (this.activityTimer == 12 && this.getAnim() != CanopyAnim.CANOPY_DROP_RECOVERY && this.getAnim() != CanopyAnim.BRANCH_GRAB) {
            this.setAnim(CanopyAnim.LAND_RECOVERY);
        }
        boolean wasDrop = this.getAnim() == CanopyAnim.CANOPY_DROP_IMPACT || this.getAnim() == CanopyAnim.CANOPY_DROP_RECOVERY;
        if (wasDrop && this.activityTimer == 8) {
            this.setAnim(CanopyAnim.CANOPY_DROP_RECOVERY);
            this.playSound(ModSounds.CANOPY_PANT.get(), 1.4F, 0.8F);
        }
        int total = wasDrop ? DROP_RECOVER : LAND_RECOVERY_TICKS;
        if (this.activityTimer >= total) {
            this.setActivity(Activity.GROUND);
            this.setAnim(this.getStage() >= 3 ? CanopyAnim.FURY_IDLE : CanopyAnim.CLUB_GUARD_IDLE);
            this.cooldowns[Attack.CANOPY_DROP.ordinal()] = this.cooldownFor(Attack.CANOPY_DROP);
        }
    }

    // ================================================================= persistence
    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Stage", this.getStage());
        output.putBoolean("ClubDrawn", this.isClubDrawn());
        output.putBoolean("IntroDone", this.introDone);
        output.putBoolean("OpeningCalled", this.openingCalled);
        output.putInt("Transitions", this.stageTransitions);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.applyStage(input.getIntOr("Stage", 1));
        this.setClubDrawn(input.getBooleanOr("ClubDrawn", false));
        this.introDone = input.getBooleanOr("IntroDone", true);
        this.openingCalled = input.getBooleanOr("OpeningCalled", true);
        this.stageTransitions = input.getIntOr("Transitions", 0);
        this.route = null;
        this.releaseClimb();
        this.setActivity(Activity.GROUND);
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

    // ================================================================= sounds
    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        Activity a = this.getActivity();
        return a == Activity.GROUND ? ModSounds.CANOPY_AMBIENT.get() : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CANOPY_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CANOPY_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(ModSounds.CANOPY_STEP.get(), 1.0F, 0.8F + this.random.nextFloat() * 0.1F);
    }
}
