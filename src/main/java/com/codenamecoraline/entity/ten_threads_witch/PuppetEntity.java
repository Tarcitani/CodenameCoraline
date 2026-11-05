package com.codenamecoraline.entity.ten_threads_witch;

import com.codenamecoraline.entity.ten_threads_witch.AttackRunner.Phase;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import com.codenamecoraline.registry.ModSounds;
import java.util.UUID;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * One of the ten puppets of the Witch of Ten Threads. Every puppet is this class: its {@link PuppetKind} says what it is
 * (size, attributes, attacks, weak points). A puppet hangs asleep from the canopy until the Matriarch wakes it for its
 * stage; its special attack turns its thread crimson (cuttable, see {@link #cutThread}) and a dodged special attack, or a
 * cut thread, leaves it TANGLED: motionless and taking 1.75 times the damage. The server owns everything; the client only
 * plays the synched state and animation.
 */
public class PuppetEntity extends Monster implements TenThreadsEncounterMember, AttackRunner.Host {
    public static final int AWAKEN_TICKS = 20;
    public static final int DEATH_TICKS = 30;
    public static final float TANGLED_DAMAGE_FACTOR = 1.75F;
    public static final float THREAD_CUT_DAMAGE_SHARE = 0.15F;
    public static final int THREAD_CUT_TANGLE_TICKS = 70;
    public static final int XP_REWARD = 50;
    public static final int STUCK_LIMIT = 50;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(PuppetEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ANIM =
            SynchedEntityData.defineId(PuppetEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CRIMSON =
            SynchedEntityData.defineId(PuppetEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID =
            SynchedEntityData.defineId(PuppetEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CRACKED =
            SynchedEntityData.defineId(PuppetEntity.class, EntityDataSerializers.BOOLEAN);

    /** Client-side animation clock. */
    public final AnimationState animState = new AnimationState();

    private final PuppetKind kind;
    private final AttackRunner runner;
    private @Nullable UUID ownerWitchUuid;
    private int stateTimer = 0;
    private int tangledTicks = 0;
    private int deathTicks = 0;
    private int hitsRecently = 0;
    private int lastHitTick = -1000;
    private int pathRecalc = 0;
    private int reachCheck = 0;
    private boolean targetReachable = true;
    private int stuckTicks = 0;
    private Vec3 lastPos = Vec3.ZERO;
    private double lastSpeed = 0.0D;
    private Vec3 awakenFrom = Vec3.ZERO;
    private Vec3 awakenTo = Vec3.ZERO;
    private Vec3 liftDest = Vec3.ZERO;
    private int liftTicks = 0;
    private @Nullable ThreadHitboxEntity hitbox;
    private Vec3 targetLastPos = Vec3.ZERO;
    private Vec3 targetVelocity = Vec3.ZERO;
    private boolean orbsDropped = false;

    // counters for tests and the design document
    private int threadsCut = 0;
    private int tangles = 0;
    private int liftsDone = 0;

    public PuppetEntity(EntityType<? extends PuppetEntity> type, Level level) {
        super(type, level);
        this.kind = kindOf(type);
        this.xpReward = XP_REWARD;
        this.runner = new AttackRunner(this, this.kind.attacks, this);
        this.setNoGravity(this.kind.flies);
    }

    public static PuppetKind kindOf(EntityType<?> type) {
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        PuppetKind kind = key == null ? null : PuppetKind.byId(key.getPath());
        return kind == null ? PuppetKind.SEWING_ARACHNID : kind;
    }

    public static AttributeSupplier.Builder createAttributes(PuppetKind kind) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, kind.health)
                .add(Attributes.ARMOR, kind.armor)
                .add(Attributes.MOVEMENT_SPEED, kind.speed)
                .add(Attributes.ATTACK_DAMAGE, kind.damage)
                .add(Attributes.KNOCKBACK_RESISTANCE, Math.min(1.0D, kind.knockbackResistance))
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.STEP_HEIGHT, Math.max(1.2D, kind.stepHeight));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, PuppetState.IDLE_COMBAT.ordinal());
        builder.define(DATA_ANIM, PuppetAnim.IDLE.ordinal());
        builder.define(DATA_CRIMSON, false);
        builder.define(DATA_OWNER_ID, -1);
        builder.define(DATA_CRACKED, false);
    }

    // ---------------------------------------------------------------- accessors
    public PuppetKind getKind() {
        return this.kind;
    }

    public PuppetState getPuppetState() {
        PuppetState[] all = PuppetState.values();
        return all[Mth.clamp(this.entityData.get(DATA_STATE), 0, all.length - 1)];
    }

    public PuppetAnim getAnim() {
        return PuppetAnim.byOrdinal(this.entityData.get(DATA_ANIM));
    }

    public boolean isCrimsonThreadActive() {
        return this.entityData.get(DATA_CRIMSON);
    }

    public boolean isTangled() {
        return this.getPuppetState() == PuppetState.TANGLED_STUNNED;
    }

    public boolean isCracked() {
        return this.entityData.get(DATA_CRACKED);
    }

    public int getOwnerEntityId() {
        return this.entityData.get(DATA_OWNER_ID);
    }

    @Override
    public @Nullable UUID getOwnerWitchUuid() {
        return this.ownerWitchUuid;
    }

    public AttackRunner getRunner() {
        return this.runner;
    }

    /** The cuttable stretch of the crimson thread while it is taut, else null; for tests. */
    public @Nullable ThreadHitboxEntity getThreadHitbox() {
        return this.hitbox;
    }

    public int getThreadsCut() {
        return this.threadsCut;
    }

    public int getTangles() {
        return this.tangles;
    }

    public int getLiftsDone() {
        return this.liftsDone;
    }

    public int getStuckTicks() {
        return this.stuckTicks;
    }

    public boolean isTargetReachable() {
        return this.targetReachable;
    }

    public void bindToWitch(MatriarchWitchEntity witch) {
        this.ownerWitchUuid = witch.getUUID();
        this.entityData.set(DATA_OWNER_ID, witch.getId());
    }

    private void setState(PuppetState state) {
        this.entityData.set(DATA_STATE, state.ordinal());
        this.stateTimer = 0;
    }

    public void setAnim(PuppetAnim anim) {
        if (this.getAnim() != anim) {
            this.entityData.set(DATA_ANIM, anim.ordinal());
        }
    }

    /** Tells the Matriarch's animation director what this puppet did (her hands follow her puppets). */
    private void notifyDirector(java.util.function.Consumer<com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchAnimDirector> what) {
        if (this.level() instanceof ServerLevel level) {
            MatriarchWitchEntity w = this.witch(level);
            if (w != null && w.getActivePuppet(level) == this) {
                what.accept(w.getDirector());
            }
        }
    }

    private @Nullable MatriarchWitchEntity witch(ServerLevel level) {
        return this.ownerWitchUuid != null && level.getEntity(this.ownerWitchUuid) instanceof MatriarchWitchEntity w && w.isAlive() ? w : null;
    }

    // ---------------------------------------------------------------- client clocks
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (this.level().isClientSide() && DATA_ANIM.equals(accessor)) {
            this.animState.start(this.tickCount);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.animState.startIfStopped(this.tickCount); // the first animation is the synched default: no update starts it
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 103 || id == 105) {
            for (int i = 0; i < 12; i++) {
                this.level().addParticle(AttackRunner.CRIMSON_DUST, this.getRandomX(0.6D), this.getRandomY(), this.getRandomZ(0.6D), 0.0D, 0.05D, 0.0D);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    // ---------------------------------------------------------------- awakening
    /** Starts a suspended puppet hanging at its current spot. */
    public void suspend() {
        this.setState(PuppetState.SLEEPING_SUSPENDED);
        this.setAnim(PuppetAnim.SLEEP);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    /** The Matriarch pulls this puppet down to {@code spot}: invulnerable while it comes, then the fight begins. */
    public void awaken(ServerLevel level, Vec3 spot) {
        this.awakenFrom = this.position();
        this.awakenTo = spot;
        this.setState(PuppetState.AWAKENING);
        this.setAnim(PuppetAnim.AWAKEN);
        this.playSound(ModSounds.PUPPET_AWAKEN.get(), 1.6F, 0.9F);
        level.sendParticles(AttackRunner.SPOOL_DUST, spot.x, spot.y + 0.5D, spot.z, 20, 0.6D, 0.6D, 0.6D, 0.02D);
    }

    // ---------------------------------------------------------------- damage
    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypes.IN_WALL) && (this.kind == PuppetKind.SKIRTED_CENTIPEDE || this.noPhysics)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return AttackRunner.isFoe(this, target) && super.canAttack(target);
    }

    /** Damage multiplier of where a blow lands (weak points and shells); 0 means the blow bounces off. */
    public float damageMultiplier(DamageSource source) {
        Vec3 from = source.getSourcePosition();
        double hy = from == null ? 0.0D : from.y - this.getY();
        boolean tangled = this.isTangled();
        float mult = 1.0F;
        switch (this.kind) {
            case HOLLOW_BELL -> {
                if (from != null && hy >= 1.4D) {
                    if (source.getDirectEntity() instanceof Projectile) {
                        return 0.0F; // arrows only hurt the clapper hanging below
                    }
                } else {
                    mult = 2.0F; // the clapper
                }
            }
            case PATCHWORK_EXECUTIONER -> {
                boolean armStuck = this.runner.current() != null && this.runner.current().id.equals("guillotine_smash")
                        && this.runner.phase() == Phase.RECOVER;
                if (hy >= 2.6D || armStuck) {
                    mult = 2.5F; // the seam of the neck, reached from above or along the fallen arm
                }
            }
            case CRADLE_GIANT -> {
                if (tangled) {
                    return 2.5F; // kneeling: the cradle of the head is at sword height
                }
                if (hy < 1.5D) {
                    mult = 0.5F; // the legs
                }
            }
            default -> {
            }
        }
        return tangled ? mult * TANGLED_DAMAGE_FACTOR : mult;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        PuppetState state = this.getPuppetState();
        boolean bypass = source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        if ((state == PuppetState.SLEEPING_SUSPENDED || state == PuppetState.AWAKENING || state == PuppetState.DYING) && !bypass) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof TenThreadsEncounterMember) {
            return false; // zero friendly fire
        }
        float mult = this.damageMultiplier(source);
        if (mult <= 0.0F) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, amount * mult);
        if (hurt && this.isAlive()) {
            this.notifyDirector(d -> d.onPuppetHurt(this));
            if (this.tickCount - this.lastHitTick > 40) {
                this.hitsRecently = 0;
            }
            this.hitsRecently++;
            this.lastHitTick = this.tickCount;
        }
        return hurt;
    }

    /** The hound's porcelain cracks against a shield or a tree: its armour drops from 10 to 0. */
    public void crack() {
        if (this.kind == PuppetKind.PORCELAIN_HOUND && !this.isCracked()) {
            this.entityData.set(DATA_CRACKED, true);
            var armor = this.getAttribute(Attributes.ARMOR);
            if (armor != null) {
                armor.setBaseValue(0.0D);
            }
            this.playSound(ModSounds.PUPPET_CRACK.get(), 1.4F, 1.2F);
            if (this.level() instanceof ServerLevel level) {
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, this.getX(), this.getY() + 0.8D, this.getZ(), 14, 0.4D, 0.4D, 0.4D, 0.05D);
            }
        }
    }

    // ---------------------------------------------------------------- the thread
    public boolean isThreadCuttable() {
        PuppetAttack a = this.runner.current();
        if (a == null || !a.special) {
            return false;
        }
        PuppetState s = this.getPuppetState();
        return (a.cut == PuppetAttack.Cut.WINDUP && s == PuppetState.WINDUP_CRIMSON)
                || (a.cut == PuppetAttack.Cut.RECOVERY && s == PuppetState.RECOVERY);
    }

    /**
     * A player (or an arrow) cut the crimson thread: the attack is cancelled, the puppet takes 15% of its maximum health
     * and falls TANGLED for 70 ticks. Returns false when the thread was not cuttable.
     */
    public boolean cutThread(ServerLevel level, @Nullable Player cutter) {
        if (!this.isThreadCuttable() || !this.isAlive()) {
            return false;
        }
        this.runner.cancel(true);
        this.threadsCut++;
        DamageSource source = cutter != null ? level.damageSources().indirectMagic(cutter, cutter) : level.damageSources().magic();
        this.invulnerableTime = 0;
        float before = this.getHealth();
        this.hurtServer(level, source, THREAD_CUT_DAMAGE_SHARE * this.getMaxHealth());
        this.playSound(ModSounds.WITCH_THREAD_SNAP.get(), 1.5F, 1.3F);
        level.sendParticles(AttackRunner.CRIMSON_DUST, this.getX(), this.getY() + this.getBbHeight() * 0.6D, this.getZ(), 30, 0.6D, 0.6D, 0.6D, 0.2D);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, this.getX(), this.getY() + this.getBbHeight() * 0.6D, this.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.3D);
        level.broadcastEntityEvent(this, (byte) 105);
        if (this.isAlive()) {
            this.enterTangled(THREAD_CUT_TANGLE_TICKS);
        }
        if (cutter instanceof ServerPlayer player && this.getHealth() < before) {
            awardAdvancement(level, player, "sever_crimson_thread");
            MatriarchWitchEntity w = this.witch(level);
            if (w != null) {
                WitchDialogueManager.say(level, w, WitchDialogueManager.Line.THREAD_CUT);
            }
        }
        return true;
    }

    public static void awardAdvancement(ServerLevel level, ServerPlayer player, String name) {
        AdvancementHolder holder = level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("codenamecoraline", "bosses/" + name));
        if (holder != null) {
            player.getAdvancements().award(holder, "trigger");
        }
    }

    private void updateHitbox(ServerLevel level, @Nullable LivingEntity target) {
        boolean active = this.isThreadCuttable();
        if (!active) {
            if (this.hitbox != null) {
                this.hitbox.discard();
                this.hitbox = null;
            }
            return;
        }
        MatriarchWitchEntity w = this.witch(level);
        Vec3 anchor = w != null ? w.getThreadAnchor() : this.position().add(0.0D, 2.0D, 0.0D);
        Vec3 back = this.position().add(0.0D, this.getBbHeight() * 0.6D, 0.0D);
        Vec3 mid = anchor.add(back).scale(0.5D);
        double y = target != null ? Math.max(target.getY(), Math.min(this.getY(), anchor.y)) : mid.y;
        if (this.hitbox == null || this.hitbox.isRemoved()) {
            ThreadHitboxEntity h = ModEntities.THREAD_HITBOX.get().create(level, EntitySpawnReason.TRIGGERED);
            if (h == null) {
                return;
            }
            h.bind(this);
            h.setPos(mid.x, y, mid.z); // placed before it joins the level: never added at the world origin, which may not be loaded
            if (!level.addFreshEntity(h)) {
                return;
            }
            this.hitbox = h;
            return;
        }
        this.hitbox.setPos(mid.x, y, mid.z);
    }

    // ---------------------------------------------------------------- host callbacks
    @Override
    public void phaseChanged(PuppetAttack attack, Phase phase) {
        int index = Math.max(0, this.kind.attacks.indexOf(attack));
        switch (phase) {
            case PREPARE -> {
                this.setState(attack.special ? PuppetState.WINDUP_CRIMSON : PuppetState.WINDUP);
                this.setAnim(attack.special ? PuppetAnim.WINDUP_CRIMSON : PuppetAnim.WINDUP);
                this.entityData.set(DATA_CRIMSON, attack.special);
                if (attack.special) {
                    this.playSound(ModSounds.WITCH_THREAD_TENSION.get(), 1.6F, 1.0F);
                    this.level().broadcastEntityEvent(this, (byte) 103);
                }
            }
            case EXECUTE -> {
                this.setState(PuppetState.EXECUTING);
                this.setAnim(PuppetAnim.attack(index));
                this.entityData.set(DATA_CRIMSON, false);
                this.playSound(ModSounds.puppetAction(this.kind), 1.3F, 1.0F);
            }
            case RECOVER -> {
                this.setState(PuppetState.RECOVERY);
                this.setAnim(PuppetAnim.RECOVER);
                this.entityData.set(DATA_CRIMSON, attack.special && attack.cut == PuppetAttack.Cut.RECOVERY);
            }
            default -> {
            }
        }
        this.notifyDirector(d -> d.onPuppetPhase(this, attack, phase));
    }

    @Override
    public void attackEnded(PuppetAttack attack, boolean hit) {
        this.entityData.set(DATA_CRIMSON, false);
        this.notifyDirector(d -> d.onPuppetAttackEnded(this, attack, hit));
        if (this.getPuppetState() != PuppetState.TANGLED_STUNNED && this.getPuppetState() != PuppetState.DYING) {
            this.setState(PuppetState.IDLE_COMBAT);
            this.setAnim(PuppetAnim.IDLE);
        }
    }

    @Override
    public void tangle(PuppetAttack attack, int ticks) {
        this.enterTangled(ticks);
    }

    @Override
    public void crashed(PuppetAttack attack) {
        if (this.kind == PuppetKind.PORCELAIN_HOUND) {
            this.crack();
        }
        this.playSound(ModSounds.PUPPET_CRASH.get(), 1.4F, 0.8F);
    }

    /** Entangles the puppet for {@code ticks} (public for tests): motionless, no attacks, 1.75 times the damage. */
    public void enterTangled(int ticks) {
        if (this.getPuppetState() == PuppetState.DYING) {
            return;
        }
        this.runner.cancel(false);
        this.entityData.set(DATA_CRIMSON, false);
        this.setState(PuppetState.TANGLED_STUNNED);
        this.setAnim(PuppetAnim.TANGLED);
        this.tangledTicks = ticks;
        this.tangles++;
        this.notifyDirector(d -> d.onPuppetTangled(this));
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        this.playSound(ModSounds.PUPPET_TANGLED.get(), 1.4F, 0.9F);
        if (this.level() instanceof ServerLevel level) {
            this.level().broadcastEntityEvent(this, (byte) 105);
            level.sendParticles(AttackRunner.THREAD_DUST, this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(), 24, 0.6D, 0.6D, 0.6D, 0.0D);
        }
    }

    // ---------------------------------------------------------------- death
    @Override
    public void die(DamageSource source) {
        if (this.getPuppetState() == PuppetState.DYING) {
            return;
        }
        this.runner.cancel(false);
        this.entityData.set(DATA_CRIMSON, false);
        this.setState(PuppetState.DYING);
        this.setAnim(PuppetAnim.DEATH);
        this.getNavigation().stop();
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            this.playSound(ModSounds.PUPPET_DEATH.get(), 1.6F, 0.9F);
            level.sendParticles(AttackRunner.THREAD_DUST, this.getX(), this.getY() + this.getBbHeight() * 0.6D, this.getZ(), 40, 0.8D, 0.8D, 0.8D, 0.1D);
            this.dropOrbs(level);
            MatriarchWitchEntity w = this.witch(level);
            if (w != null) {
                w.onPuppetDefeated(this);
            }
        }
    }

    /** Two Orbs of Vital Spool for whoever fought it. */
    private void dropOrbs(ServerLevel level) {
        if (this.orbsDropped) {
            return;
        }
        this.orbsDropped = true;
        for (int i = 0; i < 2; i++) {
            SpoolVitalOrbEntity orb = ModEntities.SPOOL_VITAL_ORB.get().create(level, EntitySpawnReason.TRIGGERED);
            if (orb != null) {
                orb.setPos(this.getX() + (i == 0 ? 0.4D : -0.4D), this.getY() + this.getBbHeight() * 0.6D, this.getZ());
                level.addFreshEntity(orb);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.spawnAtLocation(level, new ItemStack(ModItems.PUPPET_REMNANT.get())); // its unique sewing ingredient
    }

    @Override
    protected void tickDeath() {
        this.deathTicks++;
        if (this.deathTicks >= DEATH_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.hitbox != null) {
            this.hitbox.discard();
            this.hitbox = null;
        }
        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    // ---------------------------------------------------------------- the brain
    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!this.isAlive() || this.isDeadOrDying()) {
            return;
        }
        this.stateTimer++;
        this.runner.tickCooldowns();
        this.pathRecalc = Math.max(0, this.pathRecalc - 1);
        this.reachCheck = Math.max(0, this.reachCheck - 1);
        Vec3 now = this.position();
        this.lastSpeed = Math.sqrt((now.x - this.lastPos.x) * (now.x - this.lastPos.x) + (now.z - this.lastPos.z) * (now.z - this.lastPos.z));
        this.lastPos = now;
        if (this.kind == PuppetKind.ROOT_MOTHER && this.isInWater() && this.tickCount % 20 == 0) {
            this.heal(2.0F); // standing in water, the Mother regrows
        }
        PuppetState state = this.getPuppetState();
        switch (state) {
            case SLEEPING_SUSPENDED -> {
                this.noPhysics = true;
                this.setDeltaMovement(Vec3.ZERO);
            }
            case AWAKENING -> this.tickAwakening();
            case TANGLED_STUNNED -> this.tickTangled();
            case DYING -> {
            }
            default -> this.tickCombat(level);
        }
        if (this.liftTicks > 0) {
            this.tickLift();
        }
        this.updateHitbox(level, this.getTarget());
        if (this.kind == PuppetKind.PATCHWORK_STAG && state == PuppetState.EXECUTING) {
            this.breakLeaves(level);
        }
    }

    private void tickAwakening() {
        double k = Mth.clamp(this.stateTimer / (double) AWAKEN_TICKS, 0.0D, 1.0D);
        Vec3 p = this.awakenFrom.lerp(this.awakenTo, k);
        this.setPos(p.x, p.y, p.z);
        this.noPhysics = true;
        this.setDeltaMovement(Vec3.ZERO);
        if (this.stateTimer >= AWAKEN_TICKS) {
            this.noPhysics = false;
            this.setInvulnerable(false);
            this.setNoGravity(this.kind.flies);
            this.setPos(this.awakenTo.x, this.awakenTo.y, this.awakenTo.z);
            this.setState(PuppetState.IDLE_COMBAT);
            this.setAnim(PuppetAnim.IDLE);
        }
    }

    private void tickTangled() {
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.kind.flies ? -0.04D : this.getDeltaMovement().y, 0.0D);
        if (this.tickCount % 6 == 0 && this.level() instanceof ServerLevel level) {
            level.sendParticles(AttackRunner.THREAD_DUST, this.getX(), this.getY() + 0.5D, this.getZ(), 4, 0.5D, 0.3D, 0.5D, 0.0D);
        }
        if (--this.tangledTicks <= 0) {
            this.setState(PuppetState.IDLE_COMBAT);
            this.setAnim(PuppetAnim.IDLE);
            this.notifyDirector(d -> d.onPuppetRecovered(this));
        }
    }

    private void tickCombat(ServerLevel level) {
        LivingEntity target = this.chooseTarget(level);
        if (this.runner.isBusy()) {
            this.runner.tick(level, target);
            return;
        }
        if (target == null) {
            this.getNavigation().stop();
            this.setAnim(PuppetAnim.IDLE);
            return;
        }
        this.trackTarget(target);
        this.getLookControl().setLookAt(target, 40.0F, 40.0F);
        double dist = Math.sqrt(this.distanceToSqr(target));
        double dy = target.getY() - this.getY();
        if (this.reachCheck <= 0) {
            this.targetReachable = this.kind.flies;
            if (!this.kind.flies) {
                Path path = this.getNavigation().createPath(target, 1);
                this.targetReachable = path != null && path.canReach();
            }
            this.reachCheck = 10;
        }
        // a puppet that cannot get to its target for a while is lifted by the Matriarch's threads (see the Matriarch)
        if (this.tickCount % 10 == 0) {
            boolean noProgress = dist > 4.0D && this.position().distanceToSqr(this.lastCheckAnchor) < 0.3D;
            this.stuckTicks = (noProgress || !this.targetReachable) && dist > 4.0D ? this.stuckTicks + 10 : 0;
            this.lastCheckAnchor = this.position();
        }
        // after three hits in two seconds the bride's thread pulls her aside
        for (PuppetAttack a : this.kind.attacks) {
            if (a.reactive && this.hitsRecently >= 3 && this.runner.cooldown(a) <= 0) {
                this.hitsRecently = 0;
                this.runner.start(a, target);
                return;
            }
        }
        boolean sight = this.getSensing().hasLineOfSight(target);
        PuppetAttack pick = this.runner.pick(target, dist, dy, sight);
        if (pick != null) {
            this.runner.start(pick, target);
            return;
        }
        this.chase(target, dist, dy);
    }

    private Vec3 lastCheckAnchor = Vec3.ZERO;

    private @Nullable LivingEntity chooseTarget(ServerLevel level) {
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && this.canAttack(current) && this.distanceToSqr(current) <= 48.0D * 48.0D
                && this.tickCount % 20 != 0) {
            return current;
        }
        LivingEntity attacker = this.getLastHurtByMob();
        if (attacker instanceof Player p && this.tickCount - this.getLastHurtByMobTimestamp() < 100 && p.isAlive() && this.canAttack(p)) {
            this.setTarget(p);
            return p;
        }
        Player best = level.getNearestPlayer(this, 48.0D);
        if (best != null && best.isAlive() && !best.isSpectator() && this.canAttack(best)) {
            if (best != current) {
                this.setTarget(best);
            }
            return best;
        }
        return current != null && current.isAlive() && this.canAttack(current) ? current : null;
    }

    private void trackTarget(LivingEntity target) {
        Vec3 now = target.position();
        Vec3 v = this.targetLastPos == Vec3.ZERO ? Vec3.ZERO : now.subtract(this.targetLastPos);
        this.targetVelocity = v.lengthSqr() > 4.0D ? Vec3.ZERO : this.targetVelocity.scale(0.5D).add(v.scale(0.5D));
        this.targetLastPos = now;
    }

    /** Where the target will be in {@code ticks} ticks (the velocity is the one the server tracked). */
    public Vec3 predict(LivingEntity target, int ticks) {
        return target.position().add(this.targetVelocity.x * Math.min(ticks, 20), 0.0D, this.targetVelocity.z * Math.min(ticks, 20));
    }

    private void chase(LivingEntity target, double dist, double dy) {
        double speed = dist > 9.0D ? 1.3D : 1.0D;
        if (this.kind.flies) {
            double hover = this.kind == PuppetKind.STITCHED_ROOK ? 3.0D : 0.6D;
            Vec3 goal = this.predict(target, 8).add(0.0D, hover, 0.0D);
            Vec3 d = goal.subtract(this.position());
            double want = this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (dist > 6.0D ? 2.6D : 1.6D);
            if (d.length() > 1.5D) {
                Vec3 v = d.normalize().scale(Math.min(want, d.length()));
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5D).add(v.scale(0.5D)));
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.6D));
            }
            this.faceHorizontal(target.getX() - this.getX(), target.getZ() - this.getZ());
            this.setAnim(PuppetAnim.WALK);
            return;
        }
        if (dist > 1.8D && (this.pathRecalc <= 0 || this.getNavigation().isDone())) {
            Vec3 goal = this.predict(target, Math.min(10, (int) (dist * 1.2D)));
            Path path = this.getNavigation().createPath(goal.x, goal.y, goal.z, 1);
            if (path != null && path.canReach()) {
                this.getNavigation().moveTo(path, speed);
            } else {
                this.getNavigation().moveTo(target, speed);
            }
            this.pathRecalc = 4;
        }
        if (this.onGround() && this.horizontalCollision && this.getNavigation().isInProgress()) {
            this.getJumpControl().jump();
        }
        this.setAnim(this.lastSpeed > 0.03D ? PuppetAnim.WALK : PuppetAnim.IDLE);
    }

    private void faceHorizontal(double dx, double dz) {
        if (dx * dx + dz * dz < 1.0E-5D) {
            return;
        }
        float yaw = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.yBodyRot = yaw;
    }

    // ---------------------------------------------------------------- lift and stag
    /** The Matriarch hoists this puppet by its threads to {@code dest}, over 10 ticks (the hole it was stuck in no longer matters). */
    public void lift(Vec3 dest) {
        this.liftDest = dest;
        this.liftTicks = 10;
        this.stuckTicks = 0;
        this.liftsDone++;
        this.getNavigation().stop();
    }

    private void tickLift() {
        this.liftTicks--;
        Vec3 d = this.liftDest.subtract(this.position());
        this.setPos(this.getX() + d.x / (this.liftTicks + 1.0D), this.getY() + d.y / (this.liftTicks + 1.0D) + (this.liftTicks > 4 ? 0.35D : 0.0D),
                this.getZ() + d.z / (this.liftTicks + 1.0D));
        this.setDeltaMovement(Vec3.ZERO);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(AttackRunner.THREAD_DUST, this.getX(), this.getY() + this.getBbHeight() + 1.0D, this.getZ(), 4, 0.2D, 1.0D, 0.2D, 0.0D);
        }
    }

    private void breakLeaves(ServerLevel level) {
        for (BlockPos pos : BlockPos.betweenClosed(this.blockPosition().offset(-1, 0, -1), this.blockPosition().offset(1, 2, 1))) {
            if (level.getBlockState(pos).is(BlockTags.LEAVES)) {
                level.destroyBlock(pos, false);
            }
        }
    }

    // ---------------------------------------------------------------- persistence
    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("State", this.getPuppetState().ordinal());
        output.putInt("Tangled", this.tangledTicks);
        output.putBoolean("Cracked", this.isCracked());
        if (this.ownerWitchUuid != null) {
            output.putString("Witch", this.ownerWitchUuid.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.ownerWitchUuid = MatriarchWitchEntity.parseUuid(input.getStringOr("Witch", ""));
        this.tangledTicks = input.getIntOr("Tangled", 0);
        if (input.getBooleanOr("Cracked", false)) {
            this.crack();
        }
        PuppetState[] all = PuppetState.values();
        PuppetState saved = all[Mth.clamp(input.getIntOr("State", PuppetState.IDLE_COMBAT.ordinal()), 0, all.length - 1)];
        switch (saved) {
            case SLEEPING_SUSPENDED -> this.suspend();
            case TANGLED_STUNNED -> {
                this.setState(PuppetState.TANGLED_STUNNED);
                this.setAnim(PuppetAnim.TANGLED);
            }
            default -> {
                this.setState(PuppetState.IDLE_COMBAT);
                this.setAnim(PuppetAnim.IDLE);
            }
        }
    }

    // ---------------------------------------------------------------- sounds
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.PUPPET_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.PUPPET_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        if (!this.kind.flies) {
            this.playSound(ModSounds.puppetAction(this.kind), 0.4F, 1.3F);
        }
    }
}
