package com.codenamecoraline.entity.architect;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * The Architect, Kandiaru ({@code codenamecoraline:architect}): a false stone deity that waits as the Statue of the
 * Commandments and wakes into a six-armed duelist. Spec: {@code docs/design/architect.md}.
 *
 * <p>Slice 1 (foundation): attributes, state machine skeleton, phase thresholds, immunities and the dormant statue.
 * Slice 2 (visual): the synchronized animation clip ({@link ArchitectAnim}) and a minimal awakening (a player's hit wakes
 * the statue, the 110-tick reveal plays, then the combat idle) so the visual can be reviewed in the client. AI and
 * attacks arrive in slice 3.</p>
 */
public final class Architect extends Monster {
    public static final float WIDTH = 1.55F;
    public static final float HEIGHT = 3.35F;
    public static final float EYE_HEIGHT = 2.95F;

    public static final double MAX_HEALTH = 480.0D;
    public static final double ARMOR = 16.0D;
    public static final double ARMOR_TOUGHNESS = 8.0D;
    public static final double BASE_DAMAGE = 12.0D;
    public static final double BASE_SPEED = 0.30D;
    public static final double KNOCKBACK_RESISTANCE = 0.95D;
    public static final double FOLLOW_RANGE = 48.0D;
    public static final int XP_REWARD = 500;
    public static final double STEP_HEIGHT = 1.1D;

    /** Falls shorter than this are absorbed by the wings (spec §11.1). */
    public static final double SAFE_FALL_BLOCKS = 6.0D;

    /** Health fractions where the fight changes phase (spec §12.2). */
    public static final float PHASE_ADMINISTRATOR_BELOW = 0.70F;
    public static final float PHASE_COLLAPSE_BELOW = 0.35F;

    // Reserved entity event range: 160..179 (spec §19). Fired from slice 3 on.
    public static final byte EVENT_AWAKEN_START = (byte) 160;
    public static final byte EVENT_MASK_RIGHT = (byte) 161;
    public static final byte EVENT_MASK_LEFT = (byte) 162;
    public static final byte EVENT_TABLET_FALL = (byte) 163;
    public static final byte EVENT_PARRY = (byte) 170;
    public static final byte EVENT_STAGGER = (byte) 171;
    public static final byte EVENT_GARGOYLE_IMPACT = (byte) 172;
    public static final byte EVENT_ENRAGE = (byte) 177;
    public static final byte EVENT_DEATH = (byte) 178;

    private static final String TAG_STATE = "ArchitectState";

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(Architect.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ANIM =
            SynchedEntityData.defineId(Architect.class, EntityDataSerializers.INT);

    /** Client-side clock of the current clip, restarted whenever the server changes the clip. */
    public final AnimationState animState = new AnimationState();
    private int stateTicks;
    /** Themed bar (client/bossbar/ArchitectBossBar): hidden while he is a statue, its colour carries the phase. */
    private final ServerBossEvent bossEvent = new ServerBossEvent(java.util.UUID.randomUUID(),
            net.minecraft.network.chat.Component.translatable("entity.codenamecoraline.architect"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

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
        DYING
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
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    // ---------------------------------------------------------------- state and phase
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
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_ANIM.equals(accessor) && this.level().isClientSide()) {
            this.animState.start(this.tickCount);
        }
    }

    /** Wakes the statue: the 110-tick reveal (spec §7.4), invulnerable while it plays. */
    public void awaken() {
        if (this.isDormant()) {
            this.setState(State.AWAKENING);
        }
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

    // ---------------------------------------------------------------- boss bar
    private void updateBossBar() {
        this.bossEvent.setVisible(!this.isDormant());
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.bossEvent.setColor(switch (this.getPhase()) {
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

    // ---------------------------------------------------------------- the dormant statue
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.animState.startIfStopped(this.tickCount);
        } else {
            this.stateTicks++;
            if (this.getState() == State.AWAKENING && this.stateTicks >= ArchitectAnim.AWAKEN.ticks) {
                this.setState(State.COMBAT_IDLE);
            }
            this.updateBossBar();
            if (this.getState() == State.COMBAT_IDLE) {
                boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
                this.setAnim(moving ? ArchitectAnim.WALK : ArchitectAnim.BATTLE_IDLE);
            }
        }
        if (this.isDormant() || this.getState() == State.AWAKENING) {
            this.setDeltaMovement(0.0D, Math.min(0.0D, this.getDeltaMovement().y), 0.0D);
            this.setYHeadRot(this.getYRot());
            this.yBodyRot = this.getYRot();
        }
    }

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

    // ---------------------------------------------------------------- stone constitution (spec §11.1)
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.GENERIC_KILL)) {
            return super.hurtServer(level, source, amount);
        }
        if (this.isDormant()) {
            if (source.getEntity() instanceof Player) {
                this.awaken();
            }
            return false;
        }
        if (this.getState() == State.AWAKENING) {
            return false;
        }
        return super.hurtServer(level, source, amount);
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

    // ---------------------------------------------------------------- persistence
    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString(TAG_STATE, this.getState().name());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String name = input.getStringOr(TAG_STATE, State.DORMANT.name());
        State state;
        try {
            state = State.valueOf(name);
        } catch (IllegalArgumentException unknown) {
            state = State.DORMANT;
        }
        // A fight interrupted by a save resumes in combat; slice 3 owns the transitions.
        this.setState(state == State.DORMANT || state == State.DYING ? state : State.COMBAT_IDLE);
    }
}
