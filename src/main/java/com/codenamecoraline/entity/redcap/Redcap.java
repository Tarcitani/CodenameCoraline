package com.codenamecoraline.entity.redcap;

import com.codenamecoraline.registry.ModItems;
import com.codenamecoraline.registry.ModSounds;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Redcap, a hostile fairy from Scottish border folklore. Three variants (spear, dagger, claws) share the
 * same body. Stats, chase, weapon attacks with a wind-up, rock throwing, goblin sounds and client-side idle
 * animations exist so far. The ambush, the blood cap and the banishment are described in
 * docs/design/redcap.md and are not implemented yet.
 */
public final class Redcap extends Monster {
    /** Entity events sent to clients so they play the matching animation (see handleEntityEvent). */
    public static final byte EVENT_MELEE = 101;
    public static final byte EVENT_CLAW_LEFT = 102;
    public static final byte EVENT_ROCK_THROW = 103;

    /** Ticks from the start of the throw animation to the moment the rock leaves the hand. */
    public static final int ROCK_RELEASE_TICKS = 9;
    private static final int ROCK_THROW_TICKS = 22;
    private static final double ROCK_MIN_DISTANCE = 5.0D;
    private static final double ROCK_MAX_DISTANCE = 16.0D;
    private static final float ROCK_SPEED = 1.6F;
    private static final double PACK_RADIUS = 24.0D;

    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(Redcap.class, EntityDataSerializers.INT);

    // Animation states live on the client only; RedcapRenderer copies them into the render state.
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState clawLeftAnimationState = new AnimationState();
    public final AnimationState rockThrowAnimationState = new AnimationState();
    public final AnimationState sniffAnimationState = new AnimationState();
    public final AnimationState lookAroundAnimationState = new AnimationState();
    public final AnimationState sharpenAnimationState = new AnimationState();
    public final AnimationState capCheckAnimationState = new AnimationState();

    private static final EntityDataAccessor<Integer> DATA_AGE =
            SynchedEntityData.defineId(Redcap.class, EntityDataSerializers.INT);

    // Chances of each drop (only when a player kills it). The more useful the item, the rarer it is.
    private static final float DROP_NAIL = 0.55F;
    private static final float DROP_TOOTH = 0.30F;
    private static final float DROP_DAGGER = 0.14F;
    private static final float DROP_PIKESTAFF = 0.10F;
    private static final float DROP_CAP = 0.03F;
    private static final int ENRAGED_TICKS = 600;
    private static final int TRUST_MEMORY_TICKS = 400;

    private boolean variantFixed;
    private boolean ageFixed;
    /** The player wearing the cap that this Redcap last took for one of its own, and for how much longer it remembers. */
    private @Nullable UUID trustedPlayer;
    private int trustTicks;
    /** While above zero the Redcap has been deceived and attacks everyone, cap or not. */
    private int enragedTicks;
    /** Set while a pack mate hands this Redcap its target, so it does not laugh or alert the pack again. */
    private boolean alertedByPack;
    /** True while a flanking Redcap stands in its slot waiting for the pack. */
    private boolean flankReady;
    private int idleVariantTicksLeft;
    private int ticksUntilIdleVariant = 100;

    public Redcap(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D)
                .add(Attributes.MOVEMENT_SPEED, 0.36D)
                .add(Attributes.ATTACK_DAMAGE, RedcapVariant.SPEAR.damage)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    // ---------- variant ----------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_VARIANT, RedcapVariant.SPEAR.ordinal());
        entityData.define(DATA_AGE, RedcapAge.ADULT.ordinal());
    }

    public RedcapVariant getVariant() {
        return RedcapVariant.byId(this.entityData.get(DATA_VARIANT));
    }

    public void setVariant(RedcapVariant variant) {
        this.entityData.set(DATA_VARIANT, variant.ordinal());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(variant.damage);
    }

    public RedcapAge getAge() {
        return RedcapAge.byId(this.entityData.get(DATA_AGE));
    }

    public void setAge(RedcapAge age) {
        this.entityData.set(DATA_AGE, age.ordinal());
    }

    /** A spawn egg or saved data fixes the variant and the age; otherwise they are random. */
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        if (!this.variantFixed) {
            this.setVariant(RedcapVariant.byId(this.random.nextInt(RedcapVariant.values().length)));
        }
        if (!this.ageFixed) {
            this.setAge(RedcapAge.random(this.random));
        }
        return data;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Variant", this.getVariant().getSerializedName());
        output.putString("RedcapAge", this.getAge().getSerializedName());
        output.putInt("Enraged", this.enragedTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String name = input.getStringOr("Variant", "");
        if (!name.isEmpty()) {
            this.setVariant(RedcapVariant.byName(name));
            this.variantFixed = true;
        }
        String age = input.getStringOr("RedcapAge", "");
        if (!age.isEmpty()) {
            this.setAge(RedcapAge.byName(age));
            this.ageFixed = true;
        }
        this.enragedTicks = input.getIntOr("Enraged", 0);
    }

    /** Folklore: the Redcap always carries its weapon in the left hand. */
    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.LEFT;
    }

    // ---------- goals ----------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RockThrowGoal(this));
        this.goalSelector.addGoal(2, new FlankGoal(this));
        this.goalSelector.addGoal(2, new StayBackGoal(this));
        this.goalSelector.addGoal(3, new WeaponAttackGoal(this));
        // Hurting one Redcap turns the whole pack on the attacker; setTarget alerts the pack on a fresh target.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new HelpWearerGoal(this));
        // Players wearing the cap are not prey: the Redcap takes them for one of its own.
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> !(target instanceof Player player && this.isFriendTo(player))));
    }

    // ---------- the cap ----------

    public static boolean wearsCap(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.REDCAP_CAP.get());
    }

    /** A cap wearer is a friend, unless this Redcap has been deceived and is enraged. */
    boolean isFriendTo(LivingEntity entity) {
        return this.enragedTicks <= 0 && wearsCap(entity);
    }

    /**
     * Server tick: remembers the cap wearer it took for kin and, when that player shows up in sight
     * without the cap, realises the trick and attacks. Also drops a hostile target that has put the cap on.
     */
    private void tickTrust() {
        if (this.enragedTicks > 0) {
            this.enragedTicks--;
        }
        if (this.tickCount % 5 != 0) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null && this.isFriendTo(target)) {
            this.setTarget(null);
            this.getNavigation().stop();
        }
        if (this.trustTicks > 0) {
            this.trustTicks -= 5;
        }
        for (Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0D), Player::isAlive)) {
            if (this.isFriendTo(player) && this.getSensing().hasLineOfSight(player)) {
                this.trustedPlayer = player.getUUID();
                this.trustTicks = TRUST_MEMORY_TICKS;
            } else if (this.trustTicks > 0 && player.getUUID().equals(this.trustedPlayer) && !wearsCap(player)
                    && this.getSensing().hasLineOfSight(player) && !player.isCreative() && !player.isSpectator()) {
                this.becomeEnraged(player);
            }
        }
    }

    /** The trick is over: this Redcap and the pack around it attack the player and move faster for a while. */
    private void becomeEnraged(Player player) {
        this.enragedTicks = ENRAGED_TICKS;
        this.trustTicks = 0;
        this.addEffect(new MobEffectInstance(MobEffects.SPEED, ENRAGED_TICKS, 0));
        this.setTarget(player);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getEyeY() + 0.4D, this.getZ(), 5, 0.3D, 0.2D, 0.3D, 0.0D);
        }
        this.playSound(ModSounds.REDCAP_ATTACK.get(), 1.2F, 0.8F);
        for (Redcap mate : this.packMates()) {
            if (mate.enragedTicks <= 0 && mate.getTarget() != player) {
                mate.enragedTicks = ENRAGED_TICKS;
                mate.addEffect(new MobEffectInstance(MobEffects.SPEED, ENRAGED_TICKS, 0));
                mate.setTarget(player);
            }
        }
    }

    /** Hurting a Redcap while wearing the cap is also a betrayal. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && source.getEntity() instanceof Player attacker && this.enragedTicks <= 0 && wearsCap(attacker)) {
            this.becomeEnraged(attacker);
        }
        return hurt;
    }

    /**
     * Like a wolf with its owner: a Redcap near a cap wearer attacks whatever hurt that player, or what that
     * player attacked, within the last five seconds. Never other Redcaps or other cap wearers.
     */
    static final class HelpWearerGoal extends TargetGoal {
        private static final int MEMORY_TICKS = 100;
        private final Redcap redcap;
        private @Nullable LivingEntity wanted;

        HelpWearerGoal(Redcap redcap) {
            super(redcap, false);
            this.redcap = redcap;
        }

        @Override
        public boolean canUse() {
            for (Player friend : this.redcap.level().getEntitiesOfClass(Player.class, this.redcap.getBoundingBox().inflate(20.0D),
                    player -> player.isAlive() && this.redcap.isFriendTo(player))) {
                LivingEntity attacker = friend.getLastHurtByMob();
                if (attacker != null && friend.tickCount - friend.getLastHurtByMobTimestamp() < MEMORY_TICKS && this.isValid(attacker, friend)) {
                    this.wanted = attacker;
                    return true;
                }
                LivingEntity victim = friend.getLastHurtMob();
                if (victim != null && friend.tickCount - friend.getLastHurtMobTimestamp() < MEMORY_TICKS && this.isValid(victim, friend)) {
                    this.wanted = victim;
                    return true;
                }
            }
            return false;
        }

        private boolean isValid(LivingEntity candidate, Player friend) {
            return candidate != friend && candidate.isAlive() && !(candidate instanceof Redcap)
                    && !(candidate instanceof Player other && this.redcap.isFriendTo(other));
        }

        @Override
        public void start() {
            this.redcap.setTarget(this.wanted);
            super.start();
        }
    }

    // ---------- pack ----------

    /** Other living Redcaps near this one. */
    private List<Redcap> packMates() {
        return this.level().getEntitiesOfClass(Redcap.class, this.getBoundingBox().inflate(PACK_RADIUS), mate -> mate != this && mate.isAlive());
    }

    /** Pack members that close in on the target (spear and dagger), not counting this one. */
    private List<Redcap> meleeMatesOn(LivingEntity target) {
        return this.level().getEntitiesOfClass(Redcap.class, this.getBoundingBox().inflate(PACK_RADIUS),
                mate -> mate != this && mate.isAlive() && mate.getTarget() == target && mate.getVariant() != RedcapVariant.CLAWS);
    }

    /**
     * Folklore says it hunts alone, but these ones share a kill: when a Redcap picks a victim it laughs and every
     * Redcap nearby without a target takes the same one, silently.
     */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        boolean freshTarget = target != null && this.getTarget() == null && !this.level().isClientSide();
        super.setTarget(target);
        if (freshTarget && !this.alertedByPack) {
            this.playSound(ModSounds.REDCAP_LAUGH.get(), 1.2F, this.getVoicePitch());
            for (Redcap mate : this.packMates()) {
                if (mate.getTarget() == null) {
                    mate.alertedByPack = true;
                    mate.setTarget(target);
                    mate.alertedByPack = false;
                }
            }
        }
    }

    /**
     * Melee Redcaps (spear, dagger) do not run straight at the target when others of the pack hunt it too:
     * each takes a slot on a circle around it, spread evenly (two make a pincer), and waits there briefly for
     * the others before charging together.
     */
    static final class FlankGoal extends Goal {
        private static final double SLOT_RADIUS = 3.5D;
        private static final double MIN_DISTANCE = 4.5D;
        private static final int MAX_TICKS = 100;
        private static final int MAX_WAIT_TICKS = 25;

        private final Redcap redcap;
        private Vec3 slot = Vec3.ZERO;
        private int ticks;
        private int waited;
        private long nextFlankTime;

        FlankGoal(Redcap redcap) {
            this.redcap = redcap;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.redcap.getTarget();
            if (this.redcap.getVariant() == RedcapVariant.CLAWS || target == null || !target.isAlive()
                    || this.redcap.level().getGameTime() < this.nextFlankTime) {
                return false;
            }
            double distanceSqr = this.redcap.distanceToSqr(target);
            return distanceSqr > MIN_DISTANCE * MIN_DISTANCE && distanceSqr < ROCK_MAX_DISTANCE * ROCK_MAX_DISTANCE
                    && !this.redcap.meleeMatesOn(target).isEmpty();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.redcap.getTarget();
            if (target == null || !target.isAlive() || this.ticks >= MAX_TICKS) {
                return false;
            }
            if (!this.redcap.flankReady) {
                return true;
            }
            // In position: wait for the others (or give up waiting), then charge.
            return this.waited < MAX_WAIT_TICKS && !this.pinchersReady(target);
        }

        @Override
        public void start() {
            this.ticks = 0;
            this.waited = 0;
            this.redcap.flankReady = false;
            this.chooseSlot();
        }

        @Override
        public void tick() {
            LivingEntity target = this.redcap.getTarget();
            if (target == null) {
                return;
            }
            this.ticks++;
            this.redcap.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (this.ticks % 10 == 0) {
                this.chooseSlot();
            }
            if (this.redcap.flankReady) {
                this.waited++;
            } else if (this.redcap.position().distanceToSqr(this.slot) < 1.8D * 1.8D) {
                this.redcap.flankReady = true;
                this.redcap.getNavigation().stop();
            } else if (this.redcap.getNavigation().isDone() || this.ticks % 5 == 0) {
                this.redcap.getNavigation().moveTo(this.slot.x, this.slot.y, this.slot.z, 1.15D);
            }
        }

        @Override
        public void stop() {
            this.redcap.flankReady = false;
            this.nextFlankTime = this.redcap.level().getGameTime() + 80;
        }

        private boolean pinchersReady(LivingEntity target) {
            for (Redcap mate : this.redcap.meleeMatesOn(target)) {
                if (!mate.flankReady && mate.distanceToSqr(target) > MIN_DISTANCE * MIN_DISTANCE) {
                    return false;
                }
            }
            return true;
        }

        /** Slot k of n around the target, centred on the side the pack comes from. */
        private void chooseSlot() {
            LivingEntity target = this.redcap.getTarget();
            if (target == null) {
                return;
            }
            List<Redcap> members = new ArrayList<>(this.redcap.meleeMatesOn(target));
            members.add(this.redcap);
            members.sort(Comparator.comparingInt(Redcap::getId));
            double centreX = 0.0D;
            double centreZ = 0.0D;
            for (Redcap member : members) {
                centreX += member.getX();
                centreZ += member.getZ();
            }
            centreX /= members.size();
            centreZ /= members.size();
            double base = Math.atan2(centreZ - target.getZ(), centreX - target.getX());
            double step = 2.0D * Math.PI / members.size();
            double angle = base + (members.indexOf(this.redcap) - (members.size() - 1) / 2.0D) * step;
            this.slot = new Vec3(target.getX() + Math.cos(angle) * SLOT_RADIUS, target.getY(), target.getZ() + Math.sin(angle) * SLOT_RADIUS);
        }
    }

    /**
     * Claws Redcaps support the melee ones: with spear or dagger Redcaps on the same target they keep a
     * throwing distance (back off when it is close, close in when it is far) instead of rushing in.
     */
    static final class StayBackGoal extends Goal {
        private static final double TOO_CLOSE = 7.0D;
        private static final double TOO_FAR = 13.0D;

        private final Redcap redcap;

        StayBackGoal(Redcap redcap) {
            this.redcap = redcap;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.redcap.getTarget();
            if (this.redcap.getVariant() != RedcapVariant.CLAWS || target == null || !target.isAlive()
                    || this.redcap.meleeMatesOn(target).isEmpty()) {
                return false;
            }
            double distanceSqr = this.redcap.distanceToSqr(target);
            return distanceSqr < TOO_CLOSE * TOO_CLOSE || distanceSqr > TOO_FAR * TOO_FAR;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.redcap.getTarget();
            return target != null && target.isAlive() && !this.redcap.getNavigation().isDone();
        }

        @Override
        public void start() {
            LivingEntity target = this.redcap.getTarget();
            if (target == null) {
                return;
            }
            if (this.redcap.distanceToSqr(target) < TOO_CLOSE * TOO_CLOSE) {
                Vec3 away = LandRandomPos.getPosAway(this.redcap, 8, 4, target.position());
                if (away != null) {
                    this.redcap.getNavigation().moveTo(away.x, away.y, away.z, 1.2D);
                }
            } else {
                this.redcap.getNavigation().moveTo(target, 1.0D);
            }
        }
    }

    private boolean isWithinReach(LivingEntity target, double reach) {
        return this.getBoundingBox().inflate(reach, 0.5D, reach).intersects(target.getBoundingBox());
    }

    /**
     * Melee attack with a wind-up, so the damage lands on the strike frame of the variant's animation.
     * The goal keeps running while it waits (followingTargetEvenIfNotSeen = true): with false, it would end as
     * soon as the path is done and cancel the swing. The swing lands if the target is within strikeReach, which
     * is wider than the reach needed to start it because the target keeps moving during the wind-up.
     */
    static final class WeaponAttackGoal extends MeleeAttackGoal {
        private final Redcap redcap;
        private int windup = -1;
        private int cooldown;
        private boolean nextClawIsLeft;

        WeaponAttackGoal(Redcap redcap) {
            super(redcap, 1.0D, true);
            this.redcap = redcap;
        }

        @Override
        protected boolean canPerformAttack(LivingEntity target) {
            return this.cooldown <= 0 && this.windup < 0
                    && this.redcap.isWithinReach(target, this.redcap.getVariant().startReach)
                    && this.redcap.getSensing().hasLineOfSight(target);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.canPerformAttack(target)) {
                RedcapVariant variant = this.redcap.getVariant();
                this.cooldown = variant.cooldownTicks;
                this.windup = variant.windupTicks;
                boolean left = variant == RedcapVariant.CLAWS && this.nextClawIsLeft;
                this.nextClawIsLeft = !this.nextClawIsLeft;
                this.redcap.level().broadcastEntityEvent(this.redcap, left ? EVENT_CLAW_LEFT : EVENT_MELEE);
                this.redcap.playSound(ModSounds.REDCAP_ATTACK.get(), 1.0F, this.redcap.getVoicePitch());
            }
        }

        @Override
        public void tick() {
            if (this.cooldown > 0) {
                this.cooldown--;
            }
            super.tick();
            if (this.windup >= 0) {
                // Plant the feet and keep aiming during the wind-up, so the swing does not slide past the target.
                this.redcap.getNavigation().stop();
                LivingEntity target = this.redcap.getTarget();
                if (target != null) {
                    this.redcap.getLookControl().setLookAt(target, 60.0F, 60.0F);
                }
                if (this.windup-- == 0 && target != null
                        && this.redcap.isWithinReach(target, this.redcap.getVariant().strikeReach)
                        && this.redcap.level() instanceof ServerLevel level) {
                    this.redcap.doHurtTarget(level, target);
                }
            }
        }

        @Override
        public void stop() {
            super.stop();
            this.windup = -1;
        }
    }

    /** Claws variant only: stops at a distance and hurls a rock, with a wind-up the client animates. */
    static final class RockThrowGoal extends Goal {
        private final Redcap redcap;
        private int ticks;
        private long nextThrowTime;

        RockThrowGoal(Redcap redcap) {
            this.redcap = redcap;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.redcap.getTarget();
            if (this.redcap.getVariant() != RedcapVariant.CLAWS || target == null || !target.isAlive()
                    || this.redcap.level().getGameTime() < this.nextThrowTime) {
                return false;
            }
            double distanceSqr = this.redcap.distanceToSqr(target);
            return distanceSqr >= ROCK_MIN_DISTANCE * ROCK_MIN_DISTANCE
                    && distanceSqr <= ROCK_MAX_DISTANCE * ROCK_MAX_DISTANCE
                    && this.redcap.getSensing().hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.redcap.getTarget();
            return this.ticks < ROCK_THROW_TICKS && target != null && target.isAlive();
        }

        @Override
        public void start() {
            this.ticks = 0;
            this.redcap.getNavigation().stop();
            this.redcap.level().broadcastEntityEvent(this.redcap, EVENT_ROCK_THROW);
            this.redcap.playSound(ModSounds.REDCAP_THROW.get(), 1.0F, this.redcap.getVoicePitch());
        }

        @Override
        public void tick() {
            LivingEntity target = this.redcap.getTarget();
            if (target == null) {
                return;
            }
            this.redcap.getLookControl().setLookAt(target, 60.0F, 60.0F);
            if (++this.ticks == ROCK_RELEASE_TICKS) {
                this.throwRock(target);
            }
        }

        @Override
        public void stop() {
            this.nextThrowTime = this.redcap.level().getGameTime() + 50 + this.redcap.getRandom().nextInt(40);
        }

        private void throwRock(LivingEntity target) {
            RedcapRock rock = new RedcapRock(this.redcap.level(), this.redcap);
            rock.setPos(this.redcap.getX(), this.redcap.getEyeY() - 0.1D, this.redcap.getZ());
            // Simulated ballistic aim at the target's centre, leading it by the flight time (see RockAim).
            Vec3 direction = RockAim.aim(rock.position(), target, ROCK_SPEED, rock.getGravity());
            rock.shoot(direction.x, direction.y, direction.z, ROCK_SPEED, 0.5F);
            this.redcap.level().addFreshEntity(rock);
        }
    }

    // ---------- client animations ----------

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_MELEE -> {
                this.stopIdleVariant();
                this.attackAnimationState.start(this.tickCount);
            }
            case EVENT_CLAW_LEFT -> {
                this.stopIdleVariant();
                this.clawLeftAnimationState.start(this.tickCount);
            }
            case EVENT_ROCK_THROW -> {
                this.stopIdleVariant();
                this.rockThrowAnimationState.start(this.tickCount);
            }
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.tickClientAnimations();
        } else {
            this.tickTrust();
        }
    }

    private void tickClientAnimations() {
        stopWhenOver(this.attackAnimationState, 15);
        stopWhenOver(this.clawLeftAnimationState, 15);
        stopWhenOver(this.rockThrowAnimationState, ROCK_THROW_TICKS);

        boolean busy = this.attackAnimationState.isStarted() || this.clawLeftAnimationState.isStarted()
                || this.rockThrowAnimationState.isStarted();
        boolean calm = !this.isAggressive() && this.walkAnimation.speed() < 0.1F && !busy;
        if (this.idleVariantTicksLeft > 0) {
            if (!calm || --this.idleVariantTicksLeft == 0) {
                this.stopIdleVariant();
            }
        } else if (calm && --this.ticksUntilIdleVariant <= 0) {
            this.startRandomIdleVariant();
        }
    }

    private void stopWhenOver(AnimationState state, int lengthTicks) {
        if (state.isStarted() && state.getTimeInMillis(this.tickCount) > lengthTicks * 50L) {
            state.stop();
        }
    }

    /** Plays one fidget: sniffing the air, looking around, checking the cap, or (spear only) scraping the blade. */
    private void startRandomIdleVariant() {
        int count = this.getVariant() == RedcapVariant.SPEAR ? 4 : 3;
        switch (this.random.nextInt(count)) {
            case 0 -> this.startIdleVariant(this.sniffAnimationState, 48, ModSounds.REDCAP_SNIFF.get());
            case 1 -> this.startIdleVariant(this.lookAroundAnimationState, 64, null);
            case 2 -> this.startIdleVariant(this.capCheckAnimationState, 52, ModSounds.REDCAP_LAUGH.get());
            default -> this.startIdleVariant(this.sharpenAnimationState, 60, ModSounds.REDCAP_SHARPEN.get());
        }
        this.ticksUntilIdleVariant = 120 + this.random.nextInt(200);
    }

    private void startIdleVariant(AnimationState state, int lengthTicks, @Nullable SoundEvent sound) {
        state.start(this.tickCount);
        this.idleVariantTicksLeft = lengthTicks;
        if (sound != null) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), sound, this.getSoundSource(),
                    0.6F, this.getVoicePitch(), false);
        }
    }

    private void stopIdleVariant() {
        this.idleVariantTicksLeft = 0;
        this.sniffAnimationState.stop();
        this.lookAroundAnimationState.stop();
        this.sharpenAnimationState.stop();
        this.capCheckAnimationState.stop();
    }

    // ---------- drops ----------

    /**
     * Parts of the mob and the weapon it carried, only when a player kills it. The more useful an item, the lower
     * its chance (see the DROP_* constants): nail, tooth, weapon, and the cap last. A weapon comes out with a
     * random amount of wear, from nearly new to nearly broken. The claws variant carries no weapon.
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (!killedByPlayer) {
            return;
        }
        if (this.random.nextFloat() < DROP_NAIL) {
            this.spawnAtLocation(level, new ItemStack(ModItems.REDCAP_NAIL.get(), 1 + this.random.nextInt(2)));
        }
        if (this.random.nextFloat() < DROP_TOOTH) {
            this.spawnAtLocation(level, new ItemStack(ModItems.REDCAP_TOOTH.get(), 1 + this.random.nextInt(2)));
        }
        switch (this.getVariant()) {
            case DAGGER -> this.maybeDropWorn(level, ModItems.REDCAP_DAGGER.get(), DROP_DAGGER);
            case SPEAR -> this.maybeDropWorn(level, ModItems.REDCAP_PIKESTAFF.get(), DROP_PIKESTAFF);
            case CLAWS -> { }
        }
        if (this.random.nextFloat() < DROP_CAP) {
            this.spawnAtLocation(level, new ItemStack(ModItems.REDCAP_CAP.get()));
        }
    }

    private void maybeDropWorn(ServerLevel level, Item weapon, float chance) {
        if (this.random.nextFloat() >= chance) {
            return;
        }
        ItemStack stack = new ItemStack(weapon);
        int maxDamage = stack.getMaxDamage();
        // Anywhere from 1 point of wear to one point from breaking, evenly.
        stack.setDamageValue(1 + this.random.nextInt(Math.max(1, maxDamage - 1)));
        this.spawnAtLocation(level, stack);
    }

    // ---------- sounds ----------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.REDCAP_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.REDCAP_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.REDCAP_DEATH.get();
    }

    /** Folklore: heavy iron boots, loud on every step. */
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.REDCAP_STEP.get(), 0.6F, 1.0F);
    }
}
