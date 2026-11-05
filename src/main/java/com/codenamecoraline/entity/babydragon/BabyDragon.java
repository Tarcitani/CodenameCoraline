package com.codenamecoraline.entity.babydragon;

import com.codenamecoraline.entity.PlayerSummon;
import com.codenamecoraline.registry.ModSounds;
import java.util.EnumMap;
import java.util.Objects;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.server.level.ServerPlayer;
import com.codenamecoraline.inventory.DragonMenu;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import org.jspecify.annotations.Nullable;

/** A persistent tameable companion. Perching never serializes, discards or replaces the entity. */
public final class BabyDragon extends TamableAnimal implements PlayerSummon {
    // Entity-local events: avoid vanilla packet-handler IDs (notably 63, which casts to Sniffer).
    public static final byte EVENT_TAKEOFF = 100;
    public static final byte EVENT_LANDING = 101;
    public static final byte EVENT_CHARGE = 102;
    public static final byte EVENT_SHOOT = 103;
    public static final byte EVENT_HURT = 104;
    public static final byte EVENT_HAPPY = 105;
    public static final byte EVENT_CANCEL_CHARGE = 106;
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> AIRBORNE = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DEFENSIVE = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DRAGON_XP = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EVOLUTION = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BREATH = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHARGING = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHOT = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> INTELLIGENCE = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> VITALITY = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STRENGTH = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AGILITY = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SADDLED = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_TOTEM = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> CARRIED_ITEM = SynchedEntityData.defineId(BabyDragon.class, EntityDataSerializers.ITEM_STACK);
    public static final float MIN_TRAIT = 0.85F, MAX_TRAIT = 1.25F, DEFAULT_TRAIT = 1.0F;
    public static final int MAX_CUSTOM_NAME_LENGTH = 32, VARIANT_COUNT = 6;
    public static final int EQUIP_SLOT_SADDLE = 0, EQUIP_SLOT_TOTEM = 1;
    public enum Command { FOLLOW, STAY, PROTECT, RETURN }
    public enum Pose { IDLE, WALK, TAKEOFF, FLYING, HOVER, LANDING, SHOULDER_IDLE, FIREBALL_CHARGE, FIREBALL_SHOOT, HURT, SLEEP, HAPPY, BREATH_PREPARE, BREATH_LOOP, EVOLVE, DEATH, MELEE }
    public final EnumMap<Pose, AnimationState> animations = new EnumMap<>(Pose.class);
    private PathNavigation groundNavigation;
    private PathNavigation flightNavigation;
    private final DragonBrain brain;
    private int takeoffTicks, landingTicks, chargeTicks, shootTicks, happyTicks, shoulderGrace, crouchTicks;
    public float flightBlend, flightRoll, flightPitch, glideFactor;
    private float flightMomentum;
    private boolean shoulderRequested;
    private boolean returnRequested;
    private @Nullable Vec3 stayAnchor;
    private boolean traitsInitialized;
    private boolean namePromptRequested;
    private float ownerHealth;
    private @Nullable Vec3 releasePosition;
    public static final int EVOLUTION_DURATION = 60, BREATH_PREPARE = 20, BREATH_DURATION = 80;
    private int breathCooldown, recoveryTicks, meleeCooldown, meleeTicks;
    private int riderButtons, riderTimeout;
    private float riderYaw, riderPitch;
    private boolean inventoryDropped;
    private int previousVisualBreath, breathClosingTicks;
    private final SimpleContainer inventory = new SimpleContainer(18);
    private final SimpleContainer equipment = new SimpleContainer(2) {
        @Override public void setChanged() {
            super.setChanged();
            BabyDragon.this.syncEquipmentFlags();
        }
    };
    // A bounded, persisted ledger caps paid damage per real victim; healing/invulnerability cannot farm XP.
    private final HashMap<UUID, CombatCredit> combatCredits = new HashMap<>();
    private record CombatCredit(float damage, long lastHit, boolean killed) {}

    public BabyDragon(EntityType<? extends BabyDragon> type, Level level) {
        super(type, level);
        this.groundNavigation = this.navigation;
        this.flightNavigation = new FlyingPathNavigation(this, level);
        this.flightNavigation.setCanFloat(true);
        this.brain = new DragonBrain(this);
        this.xpReward = 2;
        this.setPathfindingMalus(PathType.LAVA, -1);
        this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, -1);
        this.setPathfindingMalus(PathType.FIRE, -1);
        for (Pose pose : Pose.values()) this.animations.put(pose, new AnimationState());
        if (!level.isClientSide()) this.initializeTraitsOnce();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 30).add(Attributes.ARMOR, 2)
                .add(Attributes.ATTACK_DAMAGE, 4).add(Attributes.ARMOR_TOUGHNESS, 0).add(Attributes.KNOCKBACK_RESISTANCE, 0)
                .add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.FLYING_SPEED, 0.25).add(Attributes.FOLLOW_RANGE, 40)
                .add(Attributes.CAMERA_DISTANCE, 4);
    }

    @Override protected PathNavigation createNavigation(Level level) { return new GroundPathNavigation(this, level); }
    @Override protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this) {
            @Override public boolean canUse() { return !BabyDragon.this.brain.isIntentionallyInWater() && super.canUse(); }
            @Override public boolean canContinueToUse() { return !BabyDragon.this.brain.isIntentionallyInWater() && super.canContinueToUse(); }
        });
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 7));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) {
        super.defineSynchedData(data);
        data.define(STATE, DragonState.WILD_IDLE.ordinal()); data.define(AIRBORNE, false);
        data.define(DEFENSIVE, true); data.define(SLEEPING, false);
        data.define(STAGE, 0); data.define(DRAGON_XP, 0); data.define(EVOLUTION, 0); data.define(BREATH, 0); data.define(CHARGING, 0);
        data.define(SHOT, 0);
        data.define(INTELLIGENCE, DEFAULT_TRAIT); data.define(VITALITY, DEFAULT_TRAIT);
        data.define(STRENGTH, DEFAULT_TRAIT); data.define(AGILITY, DEFAULT_TRAIT);
        data.define(VARIANT, 0);
        data.define(SADDLED, false);
        data.define(HAS_TOTEM, false);
        data.define(CARRIED_ITEM, ItemStack.EMPTY);
    }
    public float getIntelligence() { return this.entityData.get(INTELLIGENCE); }
    public float getVitality() { return this.entityData.get(VITALITY); }
    public float getStrength() { return this.entityData.get(STRENGTH); }
    public float getAgility() { return this.entityData.get(AGILITY); }
    public int getTextureVariant() { return Math.clamp(this.entityData.get(VARIANT), 0, VARIANT_COUNT - 1); }
    public void setTextureVariant(int variant) { this.entityData.set(VARIANT, Math.clamp(variant, 0, VARIANT_COUNT - 1)); }
    public boolean areTraitsInitialized() { return this.traitsInitialized; }
    public float getFireballDamage() { return this.getDragonStage().fireball * this.getStrength(); }
    public float getBreathDamage() { return 5.0F * this.getStrength(); }
    private static float clampTrait(float value) {
        return Float.isFinite(value) ? net.minecraft.util.Mth.clamp(Math.round(value * 1000.0F) / 1000.0F, MIN_TRAIT, MAX_TRAIT) : DEFAULT_TRAIT;
    }
    private static float rollTrait(RandomSource random) {
        float u1 = MIN_TRAIT + random.nextFloat() * (MAX_TRAIT - MIN_TRAIT);
        float u2 = MIN_TRAIT + random.nextFloat() * (MAX_TRAIT - MIN_TRAIT);
        return clampTrait((u1 + u2) * 0.5F);
    }
    private void initializeTraitsOnce() {
        if (this.traitsInitialized) return;
        this.traitsInitialized = true;
        RandomSource seeded = RandomSource.create(this.getUUID().getMostSignificantBits() ^ this.getUUID().getLeastSignificantBits());
        this.entityData.set(INTELLIGENCE, rollTrait(seeded));
        this.entityData.set(VITALITY, rollTrait(seeded));
        this.entityData.set(STRENGTH, rollTrait(seeded));
        this.entityData.set(AGILITY, rollTrait(seeded));
        this.entityData.set(VARIANT, seeded.nextInt(VARIANT_COUNT));
        this.applyStageAttributes();
        this.setHealth(this.getMaxHealth());
    }
    public void setIndividualTraits(float intelligence, float vitality, float strength, float agility) {
        if (this.level().isClientSide()) return;
        float ratio = this.getMaxHealth() > 0 ? this.getHealth() / this.getMaxHealth() : 1.0F;
        this.traitsInitialized = true;
        this.entityData.set(INTELLIGENCE, clampTrait(intelligence));
        this.entityData.set(VITALITY, clampTrait(vitality));
        this.entityData.set(STRENGTH, clampTrait(strength));
        this.entityData.set(AGILITY, clampTrait(agility));
        this.applyStageAttributes();
        this.setHealth(Math.max(1.0F, ratio * this.getMaxHealth()));
    }
    public DragonState getDragonState() { return DragonState.values()[this.entityData.get(STATE)]; }
    public void setDragonState(DragonState state) { this.entityData.set(STATE, state.ordinal()); }
    public boolean isFlying() { return this.entityData.get(AIRBORNE); }
    public boolean isDefensive() { return this.entityData.get(DEFENSIVE); }
    public boolean isDragonSleeping() { return this.entityData.get(SLEEPING); }
    public boolean isOnShoulder() { return this.getDragonState() == DragonState.SHOULDER; }
    public boolean wantsShoulder() { return this.shoulderRequested; }
    public boolean isReturnRequested() { return this.returnRequested; }
    public void clearReturnRequest() { this.returnRequested = false; }
    public @Nullable Vec3 getStayAnchor() { return this.stayAnchor; }
    public void setStayAnchor(@Nullable Vec3 anchor) { this.stayAnchor = anchor; }
    @Override public void setOrderedToSit(boolean sit) {
        super.setOrderedToSit(sit);
        if (sit) {
            this.stayAnchor = this.position();
            this.returnRequested = false;
            this.shoulderRequested = false;
        } else {
            this.stayAnchor = null;
        }
    }
    public Command getActiveCommand() {
        return this.returnRequested ? Command.RETURN : this.isOrderedToSit() ? Command.STAY : this.isDefensive() ? Command.PROTECT : Command.FOLLOW;
    }
    public boolean issueCommand(Command command, @Nullable Player sender) {
        if (this.level().isClientSide() || !this.isAlive() || !this.isTame() || (sender != null && !this.isOwnedByEntity(sender))) return false;
        if (this.isOnShoulder() && command != Command.RETURN) this.leaveShoulder();
        switch (command) {
            case STAY -> {
                this.shoulderRequested = false; this.returnRequested = false; this.setOrderedToSit(true);
                this.setTarget(null); this.cancelCharge(); this.cancelBreath(); this.navigation.stop();
                if (sender != null) sender.sendOverlayMessage(Component.translatable("message.codenamecoraline.dragon.stay"));
            }
            case FOLLOW -> {
                this.shoulderRequested = false; this.returnRequested = false; this.setOrderedToSit(false);
                this.entityData.set(DEFENSIVE, false); this.setTarget(null); this.cancelCharge(); this.cancelBreath();
                if (sender != null) sender.sendOverlayMessage(Component.translatable("message.codenamecoraline.dragon.follow"));
            }
            case PROTECT -> {
                this.shoulderRequested = false; this.returnRequested = false; this.setOrderedToSit(false);
                this.entityData.set(DEFENSIVE, true);
                if (sender != null) sender.sendOverlayMessage(Component.translatable("message.codenamecoraline.dragon.protect"));
            }
            case RETURN -> {
                this.setOrderedToSit(false); this.returnRequested = true;
                this.setTarget(null); this.cancelCharge(); this.cancelBreath();
                if (!this.isOnShoulder()) { this.setDragonState(DragonState.RETURNING); this.setFlying(true); }
                if (sender != null) sender.sendOverlayMessage(Component.translatable("message.codenamecoraline.dragon.return"));
            }
        }
        return true;
    }
    public int getChargeTicks() { return this.chargeTicks; }
    public DragonBrain getDragonBrain() { return this.brain; }
    @Override public boolean isPlayerSummon() { return this.isTame(); }

    public DragonStage getDragonStage() { return DragonStage.byId(this.entityData.get(STAGE)); }
    @Override protected Component getTypeName() {
        return switch (this.getDragonStage()) {
            case BABY -> super.getTypeName();
            case YOUNG -> Component.translatable("entity.codenamecoraline.dragon_young");
            case ADULT -> Component.translatable("entity.codenamecoraline.dragon_adult");
        };
    }
    public int getDragonXp() { return this.entityData.get(DRAGON_XP); }
    public int getEvolutionTicks() { return this.entityData.get(EVOLUTION); }
    public int getBreathTicks() { return this.entityData.get(BREATH); }
    public int getShotTicks() { return this.entityData.get(SHOT); }
    public int getBreathClosingTicks() { return this.breathClosingTicks; }
    public int getBreathCooldown() { return this.breathCooldown; }
    public int getRecoveryTicks() { return this.recoveryTicks; }
    public SimpleContainer getDragonInventory() { return this.inventory; }
    public SimpleContainer getDragonEquipment() { return this.equipment; }
    public static boolean isValidSaddleItem(ItemStack stack) { return stack.is(Items.SADDLE); }
    public static boolean isValidTotemItem(ItemStack stack) { return !stack.isEmpty() && stack.has(DataComponents.DEATH_PROTECTION); }
    private void syncEquipmentFlags() {
        if (this.level() != null && !this.level().isClientSide()) {
            boolean saddled = isValidSaddleItem(this.equipment.getItem(EQUIP_SLOT_SADDLE));
            this.entityData.set(SADDLED, saddled);
            this.entityData.set(HAS_TOTEM, saddled && isValidTotemItem(this.equipment.getItem(EQUIP_SLOT_TOTEM)));
        }
    }
    public boolean isSaddled() { return this.entityData.get(SADDLED); }
    public boolean hasEquippedTotem() { return this.entityData.get(HAS_TOTEM); }
    public ItemStack getSaddleStack() { return this.equipment.getItem(EQUIP_SLOT_SADDLE); }
    public ItemStack getTotemStack() { return this.equipment.getItem(EQUIP_SLOT_TOTEM); }
    public int getActiveCargoSlots() {
        return Math.min(18, this.getDragonStage().slots + (this.isSaddled() ? 9 : 0));
    }
    public boolean canUnequipSaddle() {
        if (!this.equipment.getItem(EQUIP_SLOT_TOTEM).isEmpty()) return false;
        for (int i = this.getDragonStage().slots; i < 18; i++) {
            if (!this.inventory.getItem(i).isEmpty()) return false;
        }
        return true;
    }
    public boolean equipSaddle(ItemStack stack) {
        if (this.level().isClientSide() || !this.isAlive() || !this.isTame() || this.isSaddled() || !isValidSaddleItem(stack)) return false;
        this.equipment.setItem(EQUIP_SLOT_SADDLE, stack.split(1));
        this.syncEquipmentFlags();
        this.playSound(ModSounds.DRAGON_LAND.get(), 0.5F, 1.1F);
        return true;
    }
    public boolean equipTotem(ItemStack stack) {
        if (this.level().isClientSide() || !this.isAlive() || !this.isTame() || !this.isSaddled()
                || !this.equipment.getItem(EQUIP_SLOT_TOTEM).isEmpty() || !isValidTotemItem(stack)) return false;
        this.equipment.setItem(EQUIP_SLOT_TOTEM, stack.split(1));
        this.syncEquipmentFlags();
        this.playSound(ModSounds.DRAGON_HAPPY.get(), 0.6F, 1.2F);
        return true;
    }
    public ItemStack getCarriedPreyItem() { return this.entityData.get(CARRIED_ITEM); }
    public void setCarriedPreyItem(ItemStack stack) {
        this.entityData.set(CARRIED_ITEM, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
    }
    public boolean canProtectOwnerWithTotem(LivingEntity victim) {
        return this.isAlive() && this.isTame() && this.isOwnedByEntity(victim)
                && this.level() == victim.level() && !victim.isSpectator()
                && this.distanceToSqr(victim) <= 32.0 * 32.0
                && this.isSaddled() && isValidTotemItem(this.equipment.getItem(EQUIP_SLOT_TOTEM));
    }
    public boolean tryActivateTotemForOwner(LivingEntity victim, DamageSource source) {
        if (this.level().isClientSide() || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || !this.canProtectOwnerWithTotem(victim)) {
            return false;
        }
        ItemStack totemStack = this.equipment.getItem(EQUIP_SLOT_TOTEM);
        DeathProtection deathProtection = totemStack.get(DataComponents.DEATH_PROTECTION);
        if (deathProtection == null || !CommonHooks.onLivingUseTotem(victim, source, totemStack, InteractionHand.MAIN_HAND)) {
            return false;
        }
        ItemStack usedCopy = totemStack.copyWithCount(1);
        totemStack.shrink(1);
        if (totemStack.isEmpty()) this.equipment.setItem(EQUIP_SLOT_TOTEM, ItemStack.EMPTY);
        this.equipment.setChanged();
        this.syncEquipmentFlags();
        if (victim instanceof ServerPlayer serverPlayer) {
            serverPlayer.awardStat(Stats.ITEM_USED.get(usedCopy.getItem()), 1);
            CriteriaTriggers.USED_TOTEM.trigger(serverPlayer, usedCopy);
            usedCopy.causeUseVibration(victim, GameEvent.ITEM_INTERACT_FINISH);
        }
        victim.setAbsorptionAmount(0.0F);
        victim.setHealth(1.0F);
        deathProtection.applyEffects(usedCopy, victim);
        victim.level().broadcastEntityEvent(victim, (byte) 35);
        return true;
    }
    public float getGrowthScale() {
        DragonStage stage = this.getDragonStage();
        float previous = DragonStage.byId(stage.ordinal() - 1).scale;
        return stage.scale + (previous - stage.scale) * this.getEvolutionTicks() / EVOLUTION_DURATION;
    }
    @Override protected EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose pose) {
        return DragonAnatomy.dimensions(this.getDragonStage(), this.getGrowthScale());
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key == STAGE || key == EVOLUTION) this.refreshDimensions();
        if (key == CHARGING && this.level().isClientSide()) this.chargeTicks = this.entityData.get(CHARGING);
    }
    /** Administrative override only; normal progression is monotonic and never discards stored items. */
    public boolean setDragonStage(DragonStage stage, boolean animate) {
        if (this.level().isClientSide() || !this.isAlive() || this.isVehicle()) return false;
        int newActiveSlots = Math.min(18, stage.slots + (this.isSaddled() ? 9 : 0));
        for (int i = newActiveSlots; i < 18; i++) if (!this.inventory.getItem(i).isEmpty()) return false;
        AABB grown = DragonAnatomy.dimensions(stage, stage.scale).makeBoundingBox(this.position());
        if (!this.level().noCollision(this, grown)) return false;
        if (this.isOnShoulder()) this.leaveShoulder();
        this.cancelCharge(); this.entityData.set(BREATH, 0);
        float ratio = this.getHealth() / this.getMaxHealth();
        boolean forward = stage.ordinal() > this.getDragonStage().ordinal();
        this.entityData.set(EVOLUTION, animate && forward ? EVOLUTION_DURATION : 0);
        this.entityData.set(STAGE, stage.ordinal());
        this.applyStageAttributes(); this.setHealth(Math.max(1, ratio * this.getMaxHealth()));
        this.entityData.set(DRAGON_XP, animate ? Math.max(this.getDragonXp(), stage.totalXp) : stage.totalXp);
        this.shoulderRequested = false; this.navigation.stop(); this.refreshDimensions();
        if (forward && this.getOwner() instanceof Player player)
            player.sendSystemMessage(Component.translatable("message.codenamecoraline.dragon.evolved", this.getName(), stage.title()));
        return true;
    }
    private void applyStageAttributes() {
        DragonStage s = this.getDragonStage();
        float vitality = this.getVitality(), strength = this.getStrength(), agility = this.getAgility(), intelligence = this.getIntelligence();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(s.health * vitality);
        this.getAttribute(Attributes.ARMOR).setBaseValue(s.armor * vitality);
        this.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(s.ordinal() * 2 * vitality);
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(Math.min(1.0, s.ordinal() * .2 * vitality));
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(s.melee * strength);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.22 * agility);
        this.getAttribute(Attributes.FLYING_SPEED).setBaseValue(s.flightSpeed * agility);
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(40.0 * intelligence);
        // Vanilla clips the detached mount camera against blocks after applying this distance.
        this.getAttribute(Attributes.CAMERA_DISTANCE).setBaseValue(Math.max(4, DragonRig.of(s).width() * 4));
    }
    public void addDragonXp(int amount) {
        if (!this.level().isClientSide() && amount > 0) this.entityData.set(DRAGON_XP, (int)Math.min(2000L, (long)this.getDragonXp() + amount));
    }
    public boolean damageEnemy(LivingEntity target, float damage) {
        if (!(this.level() instanceof ServerLevel server) || this.isProtected(target) || !target.isAlive()) return false;
        float before = target.getHealth();
        boolean hurt = target.hurtServer(server, this.damageSources().mobAttack(this), damage);
        float actual = Math.max(0, before - target.getHealth());
        if (hurt && actual > 0 && target instanceof Enemy && this.isTame()) {
            CombatCredit prior = this.combatCredits.get(target.getUUID());
            if (prior != null || this.combatCredits.size() < 1024) {
                float paid = prior == null ? 0 : prior.damage;
                float total = Math.min(target.getMaxHealth(), paid + actual);
                this.combatCredits.put(target.getUUID(), new CombatCredit(total, server.getGameTime(), prior != null && prior.killed));
                this.addDragonXp((int)(total * 2) - (int)(paid * 2));
                if (!target.isAlive()) this.awardKill(target);
            }
        }
        return hurt;
    }
    public boolean damageEnemyWithFire(LivingEntity target, float damage, int igniteSeconds) {
        if (!(this.level() instanceof ServerLevel) || this.isProtected(target) || !target.isAlive()) return false;
        int previousFire = target.getRemainingFireTicks();
        if (!target.fireImmune() && igniteSeconds > 0) target.igniteForSeconds(igniteSeconds);
        boolean hurt = this.damageEnemy(target, damage);
        if (!hurt) target.setRemainingFireTicks(previousFire);
        return hurt;
    }
    public void awardKill(LivingEntity target) {
        CombatCredit credit = this.combatCredits.get(target.getUUID());
        if (credit != null && !credit.killed && this.level().getGameTime() - credit.lastHit <= 200 && !target.isAlive()) {
            this.combatCredits.put(target.getUUID(), new CombatCredit(credit.damage, credit.lastHit, true));
            this.addDragonXp(20);
        }
    }

    public void setFlying(boolean flying) {
        if (flying == this.isFlying()) return;
        this.navigation.stop();
        this.entityData.set(AIRBORNE, flying);
        this.setNoGravity(flying);
        if (!flying) {
            this.flightMomentum = 0.0F;
            this.flightRoll = 0.0F;
            this.flightPitch = 0.0F;
            this.glideFactor = 0.0F;
        }
        this.navigation = flying ? this.flightNavigation : this.groundNavigation;
        this.moveControl = flying ? new DragonFlightControl(this) : new MoveControl(this);
        this.level().broadcastEntityEvent(this, flying ? EVENT_TAKEOFF : EVENT_LANDING);
        this.playSound((flying ? ModSounds.DRAGON_FLAP : ModSounds.DRAGON_LAND).get(), 0.28F, 1.5F);
    }

    public float getFlightMomentum() { return this.flightMomentum; }
    public float getFlightRoll() { return this.flightRoll; }
    public float getFlightPitch() { return this.flightPitch; }
    public float getGlideFactor() { return this.glideFactor; }
    public void addFlightMomentum(float amount) {
        this.flightMomentum = net.minecraft.util.Mth.clamp(this.flightMomentum + Math.max(0.0F, amount), 0.0F, 1.0F);
    }
    public void decayFlightMomentum(float amount) {
        this.flightMomentum = Math.max(0.0F, this.flightMomentum - Math.max(0.0F, amount));
    }
    public void updateFlightAttitude(float yawDelta, float targetPitch) {
        if (!this.isFlying()) {
            this.flightRoll *= 0.65F;
            this.flightPitch *= 0.65F;
            this.glideFactor *= 0.65F;
            return;
        }
        double horizSpeed = this.getDeltaMovement().horizontalDistance();
        float speedFactor = (float)net.minecraft.util.Mth.clamp(horizSpeed / 0.25, 0.35, 1.45);
        float desiredRoll = net.minecraft.util.Mth.clamp(-yawDelta * 2.4F * speedFactor, -28.0F, 28.0F);
        this.flightRoll = net.minecraft.util.Mth.lerp(0.24F, this.flightRoll, desiredRoll);
        float clampedPitch = net.minecraft.util.Mth.clamp(targetPitch, -35.0F, 35.0F);
        this.flightPitch = net.minecraft.util.Mth.lerp(0.22F, this.flightPitch, clampedPitch);
        float desiredGlide = (this.getDeltaMovement().y < -0.05 && horizSpeed > 0.14) || this.flightMomentum > 0.20F
                ? net.minecraft.util.Mth.clamp(this.flightMomentum + (float)Math.max(0.0, -this.getDeltaMovement().y * 1.4), 0.0F, 1.0F)
                : 0.0F;
        this.glideFactor = net.minecraft.util.Mth.lerp(0.18F, this.glideFactor, desiredGlide);
    }

    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.isAlive()) this.brain.tick();
    }

    @Override public void tick() {
        if (!this.level().isClientSide() && this.getShotTicks() > 0) this.entityData.set(SHOT, this.getShotTicks()-1);
        super.tick();
        if (this.level().isClientSide()) {
            this.flightBlend += ((this.isFlying() ? 1 : 0) - this.flightBlend) * 0.15F;
            float clientYawDelta = net.minecraft.util.Mth.wrapDegrees(this.getYRot() - this.yRotO);
            float clientPitch = this.isFlying() ? (float)net.minecraft.util.Mth.clamp(-this.getDeltaMovement().y * 55.0, -32.0, 32.0) : 0.0F;
            this.updateFlightAttitude(clientYawDelta, clientPitch);
            if (this.breathClosingTicks > 0) this.breathClosingTicks--;
            if (this.previousVisualBreath > 0 && this.getBreathTicks() == 0) this.breathClosingTicks = 10;
            if (this.getBreathTicks() > 0) this.breathClosingTicks = 0;
            this.previousVisualBreath = this.getBreathTicks();
            // Predict only the visual anchor; commands and all state changes remain server-authoritative.
            LivingEntity owner = this.getOwner();
            if (this.isOnShoulder() && owner != null) {
                this.setPos(shoulderPosition(owner)); this.setYRot(owner.yBodyRot); this.yBodyRot = owner.yBodyRot;
            }
            this.updateAnimations();
            this.clientFireEffects(); return;
        }
        this.tickAbilities();
        if (this.happyTicks > 0) this.happyTicks--;
        if (this.isOnShoulder()) this.tickShoulder();
        this.entityData.set(SLEEPING, this.isOrderedToSit() && this.onGround() && this.level() instanceof ServerLevel server && Math.floorMod(server.getDefaultClockTime(), 24000L) >= 13000);
        if (this.isFlying() && this.tickCount % 24 == 0 && !this.isOnShoulder()) this.playSound(ModSounds.DRAGON_FLAP.get(), 0.14F, 1.6F);
    }

    @Override public void travel(Vec3 input) {
        if (this.isOnShoulder()) { this.setDeltaMovement(Vec3.ZERO); return; }
        if (this.getRider() != null) {
            if (!this.level().isClientSide()) this.travelMounted();
            return;
        }
        if (this.isFlying()) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(this.isInWater() ? 0.88 : 0.96));
        } else super.travel(input);
    }

    public boolean isProtected(Entity entity) {
        if (entity == this || this.isOwnedByEntity(entity) || this.isAlliedTo(entity)) return true;
        if (entity instanceof PlayerSummon summon && summon.isPlayerSummon() && this.isTame()) return true;
        if (entity instanceof TamableAnimal pet && pet.isTame() && this.getOwnerReference() != null
                && Objects.equals(this.getOwnerReference(), pet.getOwnerReference())) return true;
        LivingEntity owner = this.getOwner();
        if (owner != null && owner.isAlliedTo(entity)) return true;
        return entity instanceof Player target && owner instanceof Player player && !player.canHarmPlayer(target);
    }

    private boolean isOwnedByEntity(Entity entity) {
        return this.getOwnerReference() != null && this.getOwnerReference().getUUID().equals(entity.getUUID());
    }

    /** Reactive targets only; the owner may explicitly engage any unprotected mob. */
    public boolean validDefensiveTarget(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive() || this.isProtected(target) || target.isSpectator()) return false;
        if (target instanceof Player p) return !p.isCreative() && this.getOwner() instanceof Player owner && owner.canHarmPlayer(p);
        if (target instanceof Enemy) return true;
        LivingEntity owner = this.getOwner();
        boolean attacking = this.getLastHurtByMob() == target && recent(this, this.getLastHurtByMobTimestamp())
                || owner != null && owner.getLastHurtByMob() == target && recent(owner, owner.getLastHurtByMobTimestamp());
        boolean directed = owner != null && owner.getLastHurtMob() == target && recent(owner, owner.getLastHurtMobTimestamp());
        return target instanceof Mob && (attacking || directed);
    }

    static boolean recent(LivingEntity entity, int stamp) { return entity.tickCount >= stamp && entity.tickCount - stamp <= 100; }

    @Override public boolean canAttack(LivingEntity target) { return this.validDefensiveTarget(target) && super.canAttack(target); }
    // isPushable alone only prevents receiving an impulse; doPush can still push the owner.
    @Override protected void pushEntities() { if (!this.isOnShoulder()) super.pushEntities(); }
    @Override public void push(Entity entity) { if (!this.isOnShoulder()) super.push(entity); }
    @Override public boolean isPushable() { return !this.isOnShoulder() && super.isPushable(); }
    @Override public boolean causeFallDamage(double distance, float multiplier, DamageSource source) { return false; }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FALL)) return false;
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) { if (this.isOnShoulder()) this.leaveShoulder(); level.broadcastEntityEvent(this, EVENT_HURT); }
        return hurt;
    }

    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.SALMON)) {
            if (this.isTame() && !this.isOwnedByEntity(player)) return InteractionResult.PASS;
            if (!this.level().isClientSide()) {
                stack.consume(1, player);
                this.getLookControl().setLookAt(player);
                this.happy();
                if (!this.isTame()) {
                    boolean success = this.random.nextInt(3) == 0 && !net.neoforged.neoforge.event.EventHooks.onAnimalTame(this, player);
                    if (success) {
                        this.tame(player);
                        this.setPersistenceRequired();
                        if (!this.hasCustomName()) {
                            this.namePromptRequested = true;
                            if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection != null
                                    && serverPlayer.connection.hasChannel(DragonNamingPayloads.Prompt.TYPE)) {
                                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, new DragonNamingPayloads.Prompt(this.getId()));
                            }
                        }
                    }
                    this.level().broadcastEntityEvent(this, (byte)(success ? 7 : 6));
                } else { this.heal(4); this.level().broadcastEntityEvent(this, (byte)7); }
            }
            return InteractionResult.SUCCESS;
        }
        if (this.isTame() && this.isOwnedByEntity(player)) {
            if (!this.level().isClientSide()) {
                if (player.isShiftKeyDown() && stack.is(Items.STICK)) {
                    this.entityData.set(DEFENSIVE, !this.isDefensive()); this.setTarget(null);
                    player.sendOverlayMessage(Component.translatable(this.isDefensive() ? "message.codenamecoraline.dragon.defense_on" : "message.codenamecoraline.dragon.defense_off"));
                } else if (player.isShiftKeyDown() && stack.is(Items.BOOK)) {
                    this.openDragonMenu(player);
                } else if (!player.isShiftKeyDown() && isValidSaddleItem(stack) && !this.isSaddled()) {
                    this.equipSaddle(player.getAbilities().instabuild ? stack.copy() : stack);
                } else if (!player.isShiftKeyDown() && isValidTotemItem(stack) && this.isSaddled() && !this.hasEquippedTotem()) {
                    this.equipTotem(player.getAbilities().instabuild ? stack.copy() : stack);
                } else if (player.isShiftKeyDown() && (isValidSaddleItem(stack) || isValidTotemItem(stack))) {
                    this.openDragonMenu(player);
                } else if (stack.is(Items.STICK)) {
                    this.toggleRest(player);
                } else if (stack.isEmpty()) {
                    if (player.isShiftKeyDown()) {
                        if (this.getDragonStage() == DragonStage.BABY) {
                            if (this.isOnShoulder()) this.leaveShoulder(); else this.requestShoulder(player);
                        } else this.openDragonMenu(player);
                    } else if (this.getDragonStage() == DragonStage.ADULT && !this.isOnShoulder()) {
                        this.mountOwner(player);
                    } else {
                        this.toggleRest(player);
                    }
                } else return super.mobInteract(player, hand);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public void happy() { this.happyTicks = 30; this.level().broadcastEntityEvent(this, EVENT_HAPPY); this.playSound(ModSounds.DRAGON_HAPPY.get(), 0.5F, 1.6F); }
    private void toggleRest(Player player) {
        if (this.isOnShoulder()) this.leaveShoulder();
        this.shoulderRequested = false; this.setOrderedToSit(!this.isOrderedToSit());
        this.setTarget(null); this.cancelCharge(); this.cancelBreath(); this.navigation.stop();
        player.sendOverlayMessage(Component.translatable(this.isOrderedToSit() ? "message.codenamecoraline.dragon.stay" : "message.codenamecoraline.dragon.follow"));
    }
    public boolean requestShoulder(Player player) {
        if (this.getDragonStage() != DragonStage.BABY || !this.isOwnedByEntity(player) || !player.isAlive() || player.isInLiquid() || player.isPassenger()) return false;
        this.setOrderedToSit(false); this.setTarget(null); this.shoulderRequested = true;
        this.setDragonState(DragonState.RETURNING); this.setFlying(true); return true;
    }

    public static Vec3 shoulderPosition(LivingEntity owner) {
        return owner.position().add(new Vec3(0.39, owner.isShiftKeyDown() ? 1.15 : 1.43, 0.04).yRot(-owner.yBodyRot * (float)Math.PI / 180));
    }

    public boolean landOnShoulder(Player owner) {
        if (this.getDragonStage() != DragonStage.BABY || !this.isOwnedByEntity(owner) || this.distanceToSqr(owner) > 9 || owner.isInLiquid() || owner.isPassenger()) return false;
        Vec3 anchor = shoulderPosition(owner);
        if (!this.clearBlocks(anchor)) return false;
        for (BabyDragon other : this.level().getEntitiesOfClass(BabyDragon.class, owner.getBoundingBox().inflate(2))) {
            if (other != this && other.isOnShoulder() && Objects.equals(other.getOwnerReference(), this.getOwnerReference())) return false;
        }
        this.releasePosition = this.position(); this.setFlying(false); this.setNoGravity(true);
        this.setDragonState(DragonState.SHOULDER); this.setInSittingPose(false);
        this.shoulderRequested = false; this.navigation.stop(); this.setDeltaMovement(Vec3.ZERO);
        this.setPos(anchor); this.shoulderGrace = 40; this.ownerHealth = owner.getHealth();
        this.level().broadcastEntityEvent(this, EVENT_LANDING); return true;
    }

    private void tickShoulder() {
        LivingEntity owner = this.getOwner();
        if (owner == null || !owner.isAlive() || owner.isInLiquid() || owner.isPassenger() || owner.isFallFlying()
                || owner.isSpectator() || owner.getHealth() < this.ownerHealth - 3.9F || !this.clearBlocks(shoulderPosition(owner))) {
            this.leaveShoulder(); return;
        }
        this.ownerHealth = owner.getHealth();
        this.setPos(shoulderPosition(owner)); this.setYRot(owner.yBodyRot); this.yBodyRot = owner.yBodyRot;
        this.setDeltaMovement(Vec3.ZERO); this.fallDistance = 0; this.setNoGravity(true);
        if (this.shoulderGrace > 0) this.shoulderGrace--;
        else { this.crouchTicks = owner.isShiftKeyDown() ? this.crouchTicks + 1 : 0; if (this.crouchTicks >= 20) this.leaveShoulder(); }
    }

    public void leaveShoulder() {
        this.shoulderRequested = false; this.crouchTicks = 0;
        LivingEntity owner = this.getOwner();
        Vec3 safe = owner == null ? null : this.findSafePosition(owner.blockPosition(), true);
        if (safe == null && this.releasePosition != null && this.safeGround(this.releasePosition)) safe = this.releasePosition;
        if (safe == null) {
            // A swimmer or an elytra user may have no nearby ground. Release into clear air instead.
            search: for (int dy = 0; dy <= 4; dy++) for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
                Vec3 candidate = this.position().add(dx, dy, dz);
                if (this.level().hasChunkAt(BlockPos.containing(candidate)) && this.clearBlocks(candidate)) { safe = candidate; break search; }
            }
        }
        this.setDragonState(DragonState.RETURNING); this.setFlying(true);
        if (safe != null) this.setPos(safe);
        this.setDeltaMovement(Vec3.ZERO); this.brain.delayAmbient(100);
    }

    public boolean clearBlocks(Vec3 pos) {
        AABB box = this.getBoundingBox().move(pos.subtract(this.position()));
        return this.level().getWorldBorder().isWithinBounds(box) && !this.level().getBlockCollisions(this, box).iterator().hasNext()
                && !this.level().containsAnyLiquid(box);
    }
    public boolean safeGround(Vec3 pos) {
        BlockPos below = BlockPos.containing(pos).below();
        return this.clearBlocks(pos) && this.level().getBlockState(below).isCollisionShapeFullBlock(this.level(), below)
                && !this.level().getBlockState(below).is(Blocks.MAGMA_BLOCK) && !this.level().getBlockState(below).is(Blocks.CAMPFIRE)
                && !this.level().getBlockState(below).is(Blocks.SOUL_CAMPFIRE);
    }
    public @Nullable Vec3 findSafePosition(BlockPos center, boolean allowAir) {
        for (int dy = 0; dy <= 3; dy++) for (int radius = 2; radius <= 4; radius++)
            for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dx) != radius && Math.abs(dz) != radius) continue;
                BlockPos pos = center.offset(dx, dy, dz);
                if (!this.level().hasChunkAt(pos)) continue;
                Vec3 candidate = Vec3.atBottomCenterOf(pos);
                if ((allowAir ? this.clearBlocks(candidate) && this.safeGround(candidate.add(0, -dy, 0)) : this.safeGround(candidate))
                        && this.level().noCollision(this, this.getBoundingBox().move(candidate.subtract(this.position())))) return candidate;
            }
        return null;
    }
    public boolean teleportNearOwner() {
        LivingEntity owner = this.getOwner();
        if (owner == null || this.isOrderedToSit() || this.isLeashed() || this.isPassenger() || this.isVehicle() || this.isOnShoulder()
                || this.distanceToSqr(owner) <= 24 * 24) return false;
        Vec3 safe = this.findSafePosition(owner.blockPosition(), false);
        if (safe == null) return false;
        this.navigation.stop(); this.setPos(safe); this.setDeltaMovement(Vec3.ZERO); this.setFlying(false); return true;
    }

    public int chargeDuration() { return this.getDragonStage() == DragonStage.ADULT ? 32 : this.getDragonStage() == DragonStage.YOUNG ? 16 : 12; }
    public void beginCharge() { this.chargeTicks = this.chargeDuration(); this.entityData.set(CHARGING, this.chargeTicks); this.level().broadcastEntityEvent(this, EVENT_CHARGE); this.playSound(ModSounds.DRAGON_CHARGE.get(), 0.35F, 1.7F); }
    public void cancelCharge() { if (this.chargeTicks > 0) this.level().broadcastEntityEvent(this, EVENT_CANCEL_CHARGE); this.chargeTicks = 0; this.entityData.set(CHARGING, 0); }
    public boolean chargeReady() {
        if (this.chargeTicks <= 0) return false;
        this.chargeTicks--; this.entityData.set(CHARGING, this.chargeTicks); return this.chargeTicks == 0;
    }
    public void shoot(LivingEntity target) {
        this.entityData.set(SHOT, 8);
        if (!this.hasClearMouth()) return;
        DragonEmber ember = new DragonEmber(this.level(), this);
        Vec3 mouth = this.getMouthPosition();
        ember.setPos(mouth);
        Vec3 center = target.getBoundingBox().getCenter();
        // Lead a moving target at release time, accounting for the projectile's vanilla air drag.
        Vec3 velocity = target.getDeltaMovement();
        if (target.onGround()) velocity = new Vec3(velocity.x, 0, velocity.z);
        if (velocity.lengthSqr() > 1) velocity = velocity.normalize();
        Vec3 aim = center.subtract(mouth);
        int leadIterations = Math.max(3, Math.round(5 * this.getIntelligence()));
        double leadFactor = Math.min(1.15, 0.75 + 0.25 * this.getIntelligence());
        for (int i = 0; i < leadIterations; i++) {
            double distance = Math.min(this.getDragonStage().range, aim.length());
            double travelTicks = Math.log(1 - distance * 0.01 / DragonEmber.SPEED) / Math.log(0.99);
            aim = center.add(velocity.scale(travelTicks * leadFactor)).subtract(mouth);
        }
        ember.shoot(aim.x, aim.y, aim.z, DragonEmber.SPEED, 0);
        this.level().addFreshEntity(ember); this.level().broadcastEntityEvent(this, EVENT_SHOOT);
        this.playSound(ModSounds.DRAGON_SHOOT.get(), 0.45F, 1.8F);
        this.recoveryTicks = this.getDragonStage() == DragonStage.BABY ? 0 : 8;
    }

    public DragonAnatomy.HeadPose getHeadPose() {
        return DragonAnatomy.head(this.getDragonStage(), net.minecraft.util.Mth.wrapDegrees(this.getYHeadRot()-this.yBodyRot),
                this.getXRot(), this.getChargeTicks(), this.getShotTicks(), this.getBreathTicks());
    }
    public Vec3 getFireDirection() { return DragonAnatomy.directionToWorld(this.getHeadPose().directionModel(), this.yBodyRot); }
    public Vec3 getMouthPosition() {
        return this.position().add(DragonAnatomy.modelToWorld(this.getHeadPose().mouthModel(), this.getGrowthScale(), this.yBodyRot));
    }
    public boolean hasClearMouth() {
        return this.level().clip(new net.minecraft.world.level.ClipContext(this.getEyePosition(), this.getMouthPosition(),
                net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, this)).getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }
    public boolean beginBreath() {
        if (this.level().isClientSide() || this.getDragonStage() != DragonStage.ADULT || this.breathCooldown > 0
                || this.getBreathTicks() > 0 || this.getChargeTicks() > 0 || this.recoveryTicks > 0 || this.getEvolutionTicks() > 0) return false;
        this.entityData.set(BREATH, BREATH_PREPARE + BREATH_DURATION);
        this.breathCooldown = 240;
        this.playSound(ModSounds.DRAGON_CHARGE.get(), .7F, .65F); return true;
    }
    public void cancelBreath() {
        if (this.getBreathTicks() > 0) this.recoveryTicks = 16;
        this.entityData.set(BREATH, 0);
    }
    private void tickAbilities() {
        if (!this.isAlive()) return;
        if (this.breathCooldown > 0) this.breathCooldown--;
        if (this.recoveryTicks > 0) this.recoveryTicks--;
        if (this.meleeCooldown > 0) this.meleeCooldown--;
        if (this.meleeTicks > 0 && --this.meleeTicks == 6) {
            LivingEntity target = this.getTarget() != null ? this.getTarget() : this.brain.getActiveHuntPrey();
            if (target != null && (this.validDefensiveTarget(target) || this.brain.isHuntingPrey(target))
                    && this.distanceTo(target) <= this.getBbWidth() + 1.5 && this.hasLineOfSight(target))
                this.damageEnemy(target, (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        }
        int evolution = this.getEvolutionTicks();
        if (evolution > 0) {
            this.entityData.set(EVOLUTION, evolution - 1); this.navigation.stop();
            this.setDeltaMovement(this.getDeltaMovement().scale(.7));
        } else if (this.tickCount % 20 == 0 && this.getDragonStage() != DragonStage.ADULT
                && this.getDragonXp() >= this.getDragonStage().next().totalXp && !this.isOnShoulder()) this.setDragonStage(this.getDragonStage().next(), true);
        int breath = this.getBreathTicks();
        if (breath > 0) {
            LivingEntity target = this.getTarget();
            if (this.getRider() == null) {
                if (!this.validDefensiveTarget(target) || !this.hasLineOfSight(target) || this.distanceTo(target) > this.getDragonStage().range) {
                    this.cancelBreath(); return;
                }
                this.aimAt(target.getBoundingBox().getCenter());
            }
            if (breath <= BREATH_DURATION && breath % 10 == 0) this.applyBreathDamage();
            this.entityData.set(BREATH, breath - 1);
            if (breath == 1) this.recoveryTicks = 20;
        }
        if (this.getRider() != null && this.getEvolutionTicks() == 0) {
            if (this.riderTimeout > 0) this.riderTimeout--; else this.riderButtons = 0;
            if ((this.riderButtons & 64) != 0) { this.getRider().stopRiding(); this.riderButtons = 0; return; }
            if (this.chargeTicks > 0) {
                if (this.chargeReady()) {
                    this.shootDirection(this.getFireDirection()); this.brain.setAttackCooldown(this.getDragonStage().fireCooldown);
                }
            } else if ((this.riderButtons & 16) != 0 && this.brain.getAttackCooldown() == 0 && this.getBreathTicks() == 0 && this.recoveryTicks == 0) this.beginCharge();
            if ((this.riderButtons & 32) != 0) this.beginBreath();
        }
    }
    public void aimAt(Vec3 point) {
        // Aim from the articulated head pivot. Aiming from the muzzle end oscillates as a long head pitches.
        Vec3 pivot = this.getMouthPosition().subtract(this.getFireDirection().scale(-DragonRig.of(this.getDragonStage()).mouthZ()*.85*this.getGrowthScale()/16));
        Vec3 direction = point.subtract(pivot);
        float yaw = (float)(net.minecraft.util.Mth.atan2(direction.z, direction.x) * 180 / Math.PI) - 90;
        float pitch = (float)(-net.minecraft.util.Mth.atan2(direction.y, direction.horizontalDistance()) * 180 / Math.PI);
        this.setYHeadRot(yaw); this.setYRot(yaw); this.yBodyRot = yaw; this.setXRot(pitch);
        this.getLookControl().setLookAt(point.x, point.y, point.z, 30, 30);
    }
    public void applyBreathDamage() {
        if (!(this.level() instanceof ServerLevel server) || this.getDragonStage() != DragonStage.ADULT || !this.hasClearMouth()) return;
        Vec3 mouth = this.getMouthPosition(), direction = this.getFireDirection();
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(35))) {
            if (this.isProtected(target) || !this.validDefensiveTarget(target)) continue;
            Vec3 delta = target.getBoundingBox().getCenter().subtract(mouth);
            double along = delta.dot(direction);
            double radius = .45 + along * .19 + target.getBbWidth() * .4;
            if (along < 0 || along > 35 || delta.lengthSqr() - along * along > radius * radius) continue;
            if (server.clip(new net.minecraft.world.level.ClipContext(mouth, target.getBoundingBox().getCenter(),
                    net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, this)).getType() != net.minecraft.world.phys.HitResult.Type.MISS) continue;
            this.damageEnemyWithFire(target, this.getBreathDamage(), 3);
        }
    }
    public boolean beginMelee(LivingEntity target) {
        if (this.meleeCooldown > 0 || this.recoveryTicks > 0 || this.getChargeTicks() > 0 || this.getBreathTicks() > 0 || !this.hasLineOfSight(target)) return false;
        this.meleeTicks = 14; this.meleeCooldown = 32;
        this.level().broadcastEntityEvent(this, (byte)107); return true;
    }
    public void shootDirection(Vec3 direction) {
        this.entityData.set(SHOT, 8);
        if (!this.hasClearMouth()) return;
        DragonEmber ember = new DragonEmber(this.level(), this); ember.setPos(this.getMouthPosition());
        ember.shoot(direction.x, direction.y, direction.z, DragonEmber.SPEED, 0);
        this.level().addFreshEntity(ember); this.level().broadcastEntityEvent(this, EVENT_SHOOT);
        this.playSound(ModSounds.DRAGON_SHOOT.get(), .7F, .65F); this.recoveryTicks = 12;
    }
    private void clientFireEffects() {
        if (!this.isAlive() || !this.hasClearMouth()) return;
        int breath = this.getBreathTicks();
        Vec3 mouth = this.getMouthPosition(), forward = this.getFireDirection();
        if (this.getEvolutionTicks() > 0) for (int i = 0; i < 5; i++)
            this.level().addParticle(ParticleTypes.FLAME, this.getX() + (this.random.nextDouble() - .5) * this.getBbWidth() * 2,
                    this.getY() + this.random.nextDouble() * this.getBbHeight(), this.getZ() + (this.random.nextDouble() - .5) * this.getBbWidth() * 2, 0, .05, 0);
        if (breath == 0) {
            if(this.getDragonStage()==DragonStage.ADULT && this.onGround() && !this.isDragonSleeping()
                    && this.getDeltaMovement().lengthSqr()<.001 && this.tickCount%18==0) {
                Vec3 ember=this.position().add(DragonAnatomy.modelToWorld(DragonAnatomy.tailEmber(this.getDragonStage(),this.tickCount),this.getGrowthScale(),this.yBodyRot));
                this.level().addParticle(ParticleTypes.SMALL_FLAME,ember.x,ember.y,ember.z,0,.015,0);
            }
            return;
        }
        boolean streaming = breath <= BREATH_DURATION;
        if (streaming && this.tickCount % 10 == 0) this.playSound(ModSounds.DRAGON_SHOOT.get(), .8F, .5F);
        Vec3 side = forward.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < .01) side = new Vec3(1, 0, 0); else side = side.normalize();
        Vec3 up = side.cross(forward).normalize();
        Vec3 end = mouth.add(forward.scale(streaming ? 35 : .8));
        var hit = this.level().clip(new net.minecraft.world.level.ClipContext(mouth, end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, this));
        double length = mouth.distanceTo(hit.getLocation());
        for (int i = 0; i < (streaming ? 110 : 12); i++) {
            double distance = this.random.nextDouble() * length;
            double angle = this.random.nextDouble() * Math.PI * 2, radius = Math.sqrt(this.random.nextDouble()) * (.18 + distance * .18);
            Vec3 pos = mouth.add(forward.scale(distance)).add(side.scale(Math.cos(angle) * radius)).add(up.scale(Math.sin(angle) * radius));
            if (this.level().clip(new net.minecraft.world.level.ClipContext(mouth, pos, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE, this)).getType() != net.minecraft.world.phys.HitResult.Type.MISS) continue;
            // Lava particles spawn their own black smoke and obscure the flame cone at this density.
            this.level().addParticle(i % 5 == 0 ? new net.minecraft.core.particles.DustParticleOptions(0xFF4A16, 1.4F) : i % 3 == 0 ? ParticleTypes.SMALL_FLAME : ParticleTypes.FLAME,
                    pos.x, pos.y, pos.z, forward.x * .18, forward.y * .18, forward.z * .18);
        }
    }
    public @Nullable Player getRider() {
        return this.getFirstPassenger() instanceof Player p && this.isOwnedByEntity(p) && this.getDragonStage() == DragonStage.ADULT ? p : null;
    }
    @Override protected boolean canAddPassenger(Entity passenger) {
        return passenger instanceof Player && this.isOwnedByEntity(passenger) && this.getDragonStage() == DragonStage.ADULT
                && this.getEvolutionTicks() == 0 && this.getPassengers().isEmpty() && !this.isLeashed();
    }
    public boolean mountOwner(Player owner) {
        if (!this.canAddPassenger(owner) || !this.isAlive() || !owner.isAlive() || owner.isSpectator() || this.distanceToSqr(owner) > 64) return false;
        this.setOrderedToSit(false); this.setTarget(null); this.cancelCharge(); this.cancelBreath(); this.navigation.stop();
        this.riderYaw = owner.getYRot(); this.riderPitch = owner.getXRot();
        return owner.startRiding(this);
    }
    @Override protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        return DragonAnatomy.saddle(this.getDragonStage(), this.getGrowthScale(), this.getYRot());
    }
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 safe = this.findSafePosition(this.blockPosition(), false);
        return safe != null ? safe : super.getDismountLocationForPassenger(passenger);
    }
    public boolean acceptRiderInput(Player sender, int buttons, float yaw, float pitch) {
        if (this.level().isClientSide() || this.getRider() != sender || !this.isAlive() || !Float.isFinite(yaw) || !Float.isFinite(pitch) || (buttons & ~255) != 0) return false;
        this.riderButtons = buttons; this.riderTimeout = 10;
        this.riderYaw = net.minecraft.util.Mth.wrapDegrees(yaw); this.riderPitch = net.minecraft.util.Mth.clamp(pitch, -65, 65); return true;
    }
    private void travelMounted() {
        float agility = this.getAgility();
        float previousYaw = this.getYRot();
        float yaw = net.minecraft.util.Mth.approachDegrees(previousYaw, this.riderYaw, 6.0F * agility);
        float yawDelta = net.minecraft.util.Mth.wrapDegrees(yaw - previousYaw);
        this.setYRot(yaw); this.yBodyRot = yaw; this.setYHeadRot(yaw); this.setXRot(this.riderPitch);
        if ((this.riderButtons & 4) != 0 && !this.isFlying() && this.clearBlocks(this.position().add(0, .5, 0))) this.setFlying(true);
        double forward = (this.riderButtons & 1) != 0 ? 1 : (this.riderButtons & 2) != 0 ? -.4 : 0;
        double verticalInput = (this.riderButtons & 4) != 0 ? .65 : (this.riderButtons & 8) != 0 ? -.55 : 0;
        double baseSpeed = this.getAttributeValue(Attributes.FLYING_SPEED);
        Vec3 motion;
        if (this.isFlying()) {
            float pitchRad = this.riderPitch * net.minecraft.util.Mth.DEG_TO_RAD;
            double sinPitch = Math.sin(pitchRad); // > 0 when pitching down (diving), < 0 when pitching up (climbing)
            if (forward > 0 && (sinPitch > 0.08 || verticalInput < 0)) {
                float diveGain = (float)((Math.max(0.0, sinPitch) * 0.065 + (verticalInput < 0 ? 0.03 : 0.0)) * agility);
                this.addFlightMomentum(diveGain);
            } else if (sinPitch < -0.08 || verticalInput > 0) {
                this.decayFlightMomentum(0.03F);
            } else {
                this.decayFlightMomentum(0.015F);
            }
            double diveSpeedBoost = 1.0 + this.flightMomentum * (0.65 * agility) + (forward > 0 && sinPitch > 0 ? sinPitch * 0.35 : 0.0);
            double climbCost = (sinPitch < -0.15 && this.flightMomentum < 0.08F) ? Math.max(0.78, 1.0 + sinPitch * 0.22) : 1.0;
            double effectiveSpeed = baseSpeed * diveSpeedBoost * climbCost;
            // Pitch-coupled glide/dive vertical component plus direct Space/X vertical thrust and pull-up lift conversion.
            double pitchLift = forward > 0 ? (-sinPitch * 0.50 + (sinPitch < -0.05 ? this.flightMomentum * 0.28 : 0.0)) : 0.0;
            double vertical = net.minecraft.util.Mth.clamp(verticalInput + pitchLift, -0.95, 0.90);
            Vec3 desired = new Vec3(0, vertical, forward).yRot(-yaw * (float)Math.PI / 180).scale(effectiveSpeed);
            double response = net.minecraft.util.Mth.clamp((0.14 + 0.03 * agility) * (1.0 - 0.22 * this.flightMomentum), 0.10, 0.24);
            motion = this.getDeltaMovement().lerp(desired, response);
            this.updateFlightAttitude(yawDelta, this.riderPitch * 0.55F + (float)(-vertical * 18.0));
        } else {
            this.decayFlightMomentum(0.05F);
            Vec3 desired = new Vec3(0, 0, forward).yRot(-yaw * (float)Math.PI / 180).scale(baseSpeed);
            motion = this.getDeltaMovement().lerp(desired, 0.16);
            motion = new Vec3(motion.x, Math.max(-.4, this.getDeltaMovement().y - .08), motion.z);
            this.updateFlightAttitude(0.0F, 0.0F);
        }
        this.setDeltaMovement(motion); this.move(MoverType.SELF, motion); this.fallDistance = 0;
        if (this.isFlying() && this.onGround() && verticalInput <= 0) this.setFlying(false);
    }
    public void openDragonMenu(Player player) {
        if (player instanceof ServerPlayer server && this.isOwnedByEntity(player) && this.isAlive() && this.distanceToSqr(player) <= 64)
            server.openMenu(new SimpleMenuProvider((id, inv, p) -> new DragonMenu(id, inv, this), this.getName()), buf -> buf.writeInt(this.getId()));
    }
    public boolean wasNamePromptRequested() { return this.namePromptRequested; }
    public boolean acceptOwnerName(Player sender, @Nullable String rawName) {
        if (this.level().isClientSide() || rawName == null || !this.isAlive() || !this.isTame() || !this.isOwnedByEntity(sender) || this.distanceToSqr(sender) > 64) return false;
        for (int i = 0; i < rawName.length(); i++) {
            if (!net.minecraft.util.StringUtil.isAllowedChatCharacter(rawName.charAt(i))) return false;
        }
        String trimmed = rawName.trim();
        if (trimmed.length() > MAX_CUSTOM_NAME_LENGTH) return false;
        this.namePromptRequested = false;
        if (trimmed.isEmpty()) return true;
        this.setCustomName(Component.literal(trimmed));
        sender.sendOverlayMessage(Component.translatable("message.codenamecoraline.dragon.named", trimmed));
        return true;
    }

    private void updateAnimations() {
        if (this.meleeTicks > 0) this.meleeTicks--;
        if (this.takeoffTicks > 0) this.takeoffTicks--;
        if (this.landingTicks > 0) this.landingTicks--;
        if (this.chargeTicks > 0) {
            this.chargeTicks--;
            Vec3 mouth = this.getMouthPosition();
            for (int i = 0; i <= this.getDragonStage().ordinal() * 6; i++) {
                double size = this.getDragonStage().ordinal() * .35 * (1 - this.chargeTicks / (double)this.chargeDuration());
                this.level().addParticle(ParticleTypes.FLAME, mouth.x + (this.random.nextDouble() - .5) * size,
                        mouth.y + (this.random.nextDouble() - .5) * size, mouth.z + (this.random.nextDouble() - .5) * size, 0, .015, 0);
            }
        }
        if (this.shootTicks > 0) this.shootTicks--;
        if (this.happyTicks > 0) this.happyTicks--;
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 0.004;
        for (Pose pose : Pose.values()) {
            boolean active = switch (pose) {
                case IDLE -> !this.isFlying() && !this.isOnShoulder() && !this.isDragonSleeping();
                case WALK -> !this.isFlying() && moving && !this.isOnShoulder();
                case TAKEOFF -> this.takeoffTicks > 0;
                case FLYING -> this.isFlying() && moving;
                case HOVER -> this.isFlying() && !moving;
                case LANDING -> this.landingTicks > 0;
                case SHOULDER_IDLE -> this.isOnShoulder();
                case FIREBALL_CHARGE -> this.chargeTicks > 0;
                case FIREBALL_SHOOT -> this.shootTicks > 0;
                case HURT -> this.hurtTime > 0;
                case SLEEP -> this.isDragonSleeping();
                case HAPPY -> this.happyTicks > 0;
                case BREATH_PREPARE -> this.getBreathTicks() > BREATH_DURATION;
                case BREATH_LOOP -> this.getBreathTicks() > 0 && this.getBreathTicks() <= BREATH_DURATION;
                case EVOLVE -> this.getEvolutionTicks() > 0;
                case DEATH -> this.isDeadOrDying();
                case MELEE -> this.meleeTicks > 0;
            };
            this.animations.get(pose).animateWhen(active, this.tickCount);
        }
    }
    @Override public void handleEntityEvent(byte event) {
        switch (event) {
            case EVENT_TAKEOFF -> this.takeoffTicks = 14+3*this.getDragonStage().ordinal();
            case EVENT_LANDING -> this.landingTicks = 12+3*this.getDragonStage().ordinal();
            case EVENT_CHARGE -> this.chargeTicks = this.chargeDuration();
            case EVENT_SHOOT -> { this.chargeTicks = 0; this.shootTicks = 8; }
            case EVENT_HURT -> this.animations.get(Pose.HURT).start(this.tickCount);
            case EVENT_HAPPY -> this.happyTicks = 30;
            case EVENT_CANCEL_CHARGE -> this.chargeTicks = 0;
            case 107 -> this.meleeTicks = 14;
            default -> super.handleEntityEvent(event);
        }
    }
    @Override public boolean isFood(ItemStack stack) { return false; }
    @Override public boolean canMate(Animal other) { return false; }
    @Override public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) { return null; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected @Nullable SoundEvent getAmbientSound() { return ModSounds.DRAGON_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModSounds.DRAGON_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModSounds.DRAGON_DEATH.get(); }
    @Override public float getVoicePitch() { return 1.5F + this.random.nextFloat() * 0.25F; }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("DragonDefensive", this.isDefensive()); output.putBoolean("DragonShoulder", this.isOnShoulder());
        output.putInt("DragonFireCooldown", this.brain.getAttackCooldown());
        output.putInt("DragonStage", this.getDragonStage().ordinal()); output.putInt("DragonXp", this.getDragonXp());
        output.putInt("DragonEvolution", this.getEvolutionTicks()); output.putInt("DragonBreathCooldown", this.breathCooldown);
        output.putInt("DragonRecovery", this.recoveryTicks); output.putBoolean("DragonInventoryDropped", this.inventoryDropped);
        output.putInt("DragonMeleeCooldown", this.meleeCooldown);
        output.putBoolean("DragonTraitsInitialized", this.traitsInitialized);
        output.putFloat("DragonIntelligence", this.getIntelligence());
        output.putFloat("DragonVitality", this.getVitality());
        output.putFloat("DragonStrength", this.getStrength());
        output.putFloat("DragonAgility", this.getAgility());
        output.putInt("DragonVariant", this.getTextureVariant());
        output.putBoolean("DragonReturnRequested", this.returnRequested);
        output.putBoolean("DragonStayAnchorSet", this.stayAnchor != null);
        if (this.stayAnchor != null) {
            output.putDouble("DragonStayX", this.stayAnchor.x);
            output.putDouble("DragonStayY", this.stayAnchor.y);
            output.putDouble("DragonStayZ", this.stayAnchor.z);
        }
        net.minecraft.world.ContainerHelper.saveAllItems(output.child("DragonInventory"), this.inventory.getItems());
        net.minecraft.world.ContainerHelper.saveAllItems(output.child("DragonEquipment"), this.equipment.getItems());
        if (!this.getCarriedPreyItem().isEmpty()) {
            NonNullList<ItemStack> carried = NonNullList.withSize(1, this.getCarriedPreyItem());
            net.minecraft.world.ContainerHelper.saveAllItems(output.child("DragonCarriedItem"), carried);
        }
        var credits = output.childrenList("DragonCombatCredits");
        this.combatCredits.forEach((uuid, credit) -> {
            var out = credits.addChild(); out.putString("Victim", uuid.toString()); out.putFloat("Damage", credit.damage);
            out.putLong("LastHit", credit.lastHit); out.putBoolean("Killed", credit.killed);
        });
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DEFENSIVE, input.getBooleanOr("DragonDefensive", true));
        this.setDragonState(input.getBooleanOr("DragonShoulder", false) && this.isTame() ? DragonState.SHOULDER : this.isOrderedToSit() ? DragonState.SITTING : DragonState.WILD_IDLE);
        this.brain.setAttackCooldown(input.getIntOr("DragonFireCooldown", 0));
        this.entityData.set(STAGE, DragonStage.byId(input.getIntOr("DragonStage", 0)).ordinal());
        this.entityData.set(DRAGON_XP, Math.max(this.getDragonStage().totalXp, Math.min(2000, input.getIntOr("DragonXp", 0))));
        this.entityData.set(EVOLUTION, Math.max(0, Math.min(EVOLUTION_DURATION, input.getIntOr("DragonEvolution", 0))));
        this.breathCooldown = Math.max(0, Math.min(240, input.getIntOr("DragonBreathCooldown", 0)));
        this.recoveryTicks = Math.max(0, Math.min(20, input.getIntOr("DragonRecovery", 0)));
        this.meleeCooldown = Math.max(0, Math.min(32, input.getIntOr("DragonMeleeCooldown", 0)));
        this.entityData.set(BREATH, 0); this.chargeTicks = 0;
        this.inventoryDropped = input.getBooleanOr("DragonInventoryDropped", false);
        this.inventory.clearContent();
        net.minecraft.world.ContainerHelper.loadAllItems(input.childOrEmpty("DragonInventory"), this.inventory.getItems());
        this.equipment.clearContent();
        net.minecraft.world.ContainerHelper.loadAllItems(input.childOrEmpty("DragonEquipment"), this.equipment.getItems());
        this.syncEquipmentFlags();
        NonNullList<ItemStack> carried = NonNullList.withSize(1, ItemStack.EMPTY);
        net.minecraft.world.ContainerHelper.loadAllItems(input.childOrEmpty("DragonCarriedItem"), carried);
        this.setCarriedPreyItem(carried.get(0));
        this.combatCredits.clear();
        for (var in : input.childrenListOrEmpty("DragonCombatCredits")) {
            if (this.combatCredits.size() >= 1024) break;
            try { this.combatCredits.put(UUID.fromString(in.getStringOr("Victim", "")), new CombatCredit(
                    Math.max(0, in.getFloatOr("Damage", 0)), in.getLongOr("LastHit", 0), in.getBooleanOr("Killed", false))); }
            catch (IllegalArgumentException ignored) { /* Invalid legacy entry grants no credit. */ }
        }
        this.traitsInitialized = true;
        if (input.getBooleanOr("DragonTraitsInitialized", false)) {
            this.entityData.set(INTELLIGENCE, clampTrait(input.getFloatOr("DragonIntelligence", DEFAULT_TRAIT)));
            this.entityData.set(VITALITY, clampTrait(input.getFloatOr("DragonVitality", DEFAULT_TRAIT)));
            this.entityData.set(STRENGTH, clampTrait(input.getFloatOr("DragonStrength", DEFAULT_TRAIT)));
            this.entityData.set(AGILITY, clampTrait(input.getFloatOr("DragonAgility", DEFAULT_TRAIT)));
        } else {
            // Legacy saves predating individual traits keep neutral 1.0 multipliers so existing companions do not change stats unexpectedly.
            this.entityData.set(INTELLIGENCE, DEFAULT_TRAIT);
            this.entityData.set(VITALITY, DEFAULT_TRAIT);
            this.entityData.set(STRENGTH, DEFAULT_TRAIT);
            this.entityData.set(AGILITY, DEFAULT_TRAIT);
        }
        this.entityData.set(VARIANT, Math.clamp(input.getIntOr("DragonVariant", 0), 0, VARIANT_COUNT - 1));
        this.returnRequested = input.getBooleanOr("DragonReturnRequested", false) && this.isTame() && !this.isOrderedToSit();
        if (input.getBooleanOr("DragonStayAnchorSet", false)) {
            this.stayAnchor = new Vec3(
                    input.getDoubleOr("DragonStayX", this.getX()),
                    input.getDoubleOr("DragonStayY", this.getY()),
                    input.getDoubleOr("DragonStayZ", this.getZ()));
        } else {
            this.stayAnchor = this.isOrderedToSit() ? this.position() : null;
        }
        float health = input.getFloatOr("Health", this.getHealth());
        this.applyStageAttributes();
        this.setHealth(Math.min(health, this.getMaxHealth()));
        if (this.getDragonStage() != DragonStage.BABY && this.isOnShoulder()) this.setDragonState(DragonState.WILD_IDLE);
        this.refreshDimensions();
        this.shoulderGrace = 40;
        LivingEntity owner = this.getOwner(); this.ownerHealth = owner == null ? 0 : owner.getHealth();
        this.setNoGravity(this.isOnShoulder());
    }
    @Override public void die(DamageSource source) {
        super.die(source);
        if (this.dead && this.level() instanceof ServerLevel server && !this.inventoryDropped) {
            this.inventoryDropped = true;
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                ItemStack item = this.inventory.removeItemNoUpdate(i);
                if (!item.isEmpty()) this.spawnAtLocation(server, item);
            }
            for (int i = 0; i < this.equipment.getContainerSize(); i++) {
                ItemStack item = this.equipment.removeItemNoUpdate(i);
                if (!item.isEmpty()) this.spawnAtLocation(server, item);
            }
            this.syncEquipmentFlags();
            ItemStack carried = this.getCarriedPreyItem();
            if (!carried.isEmpty()) {
                this.setCarriedPreyItem(ItemStack.EMPTY);
                this.spawnAtLocation(server, carried);
            }
        }
    }
    public static boolean checkSpawnRules(EntityType<BabyDragon> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        BlockPos floor = pos.below();
        return level.canSeeSky(pos) && level.getRawBrightness(pos, 0) >= 9 && level.getFluidState(pos).isEmpty()
                && level.getBlockState(floor).isCollisionShapeFullBlock(level, floor)
                && !level.getBlockState(floor).is(Blocks.MAGMA_BLOCK) && level.noCollision(type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
    }
}
