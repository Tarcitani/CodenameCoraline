package com.codenamecoraline.entity.basilisk;

import com.codenamecoraline.registry.ModItems;
import com.codenamecoraline.registry.ModSounds;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Low amphibian predator: hop, telegraphed breath, then circle the contaminated ground. */
public final class Basilisk extends Monster {
    public enum State { LURKING, HOPPING, INHALING, EXHALING, REPELLED }
    public static final int INHALE_TICKS = 24;
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(Basilisk.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INHALE = SynchedEntityData.defineId(Basilisk.class, EntityDataSerializers.INT);
    private @Nullable UUID provokedBy;
    private boolean dampOrigin;
    private int hopCooldown;
    private int breathCooldown = 30;
    private int clawCooldown;
    private int exhaleTicks;

    public Basilisk(EntityType<? extends Basilisk> type, Level level) {
        super(type, level);
        this.moveControl = new HopMoveControl();
        this.setPathfindingMalus(PathType.WATER, 0);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 28)
                .add(Attributes.ARMOR, 2).add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 4).add(Attributes.FOLLOW_RANGE, 20)
                .add(Attributes.STEP_HEIGHT, 1);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, State.LURKING.ordinal());
        builder.define(INHALE, 0);
    }

    public State getState() { return State.values()[this.entityData.get(STATE)]; }
    private void setState(State state) { this.entityData.set(STATE, state.ordinal()); }
    public float getInflation() { return this.entityData.get(INHALE) / (float) INHALE_TICKS; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RepelGoal());
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new CombatGoal());
        this.goalSelector.addGoal(5, new LurkGoal());
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.7, 100));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 5, true, false,
                (target, level) -> this.canAttack(target) && this.distanceToSqr(target) < 100));
    }

    public static boolean carriesSoulLight(LivingEntity entity) {
        return entity.getMainHandItem().is(Items.SOUL_TORCH) || entity.getOffhandItem().is(Items.SOUL_TORCH)
                || entity.getMainHandItem().is(Items.SOUL_LANTERN) || entity.getOffhandItem().is(Items.SOUL_LANTERN);
    }

    public boolean isRepelledBy(LivingEntity entity) {
        return carriesSoulLight(entity) && !entity.getUUID().equals(this.provokedBy);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof Basilisk) && !this.isRepelledBy(target) && super.canAttack(target);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.POISON) && !effect.is(MobEffects.WITHER)
                && !effect.is(MobEffects.DARKNESS) && super.canBeAffected(effect);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypeTags.IS_FIRE)) damage *= 1.5F;
        if (source.getDirectEntity() instanceof LivingEntity attacker && attacker.getMainHandItem().is(ItemTags.SWORDS)) {
            damage *= 1.25F;
        }
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && source.getEntity() instanceof LivingEntity attacker && !(attacker instanceof Basilisk)) {
            this.provokedBy = attacker.getUUID();
            if (this.canAttack(attacker)) this.setTarget(attacker);
        }
        return hurt;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.hopCooldown > 0) this.hopCooldown--;
        if (this.breathCooldown > 0) this.breathCooldown--;
        if (this.clawCooldown > 0) this.clawCooldown--;
        if (this.exhaleTicks > 0 && --this.exhaleTicks == 0) this.setState(State.LURKING);
        if (this.onGround() && this.getState() == State.HOPPING) this.setState(State.LURKING);
        if (this.tickCount % 20 == 0) {
            boolean wet = this.isInWater() || this.getBlockStateOn().is(Blocks.MUD);
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(wet ? 0.38 : 0.32);
        }
        LivingEntity target = this.getTarget();
        if (target != null && !this.canAttack(target)) this.setTarget(null);
    }

    private void cancelInhale() {
        this.entityData.set(INHALE, 0);
        if (this.getState() == State.INHALING) this.setState(State.LURKING);
    }

    private void hopTowards(Vec3 destination, boolean pounce) {
        if (this.hopCooldown > 0 || this.getState() == State.INHALING || this.getState() == State.EXHALING) return;
        Vec3 direction = destination.subtract(this.position()).multiply(1, 0, 1).normalize();
        if (direction.lengthSqr() < 0.01) return;
        if (this.isInWater()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.6).add(direction.scale(0.22)).add(0, 0.08, 0));
            this.hopCooldown = 10;
        } else if (this.onGround()) {
            double speed = pounce ? 0.43 : 0.29;
            this.setDeltaMovement(direction.scale(speed).add(0, pounce ? 0.52 : 0.40, 0));
            this.hopCooldown = pounce ? 24 : 16;
            this.setState(State.HOPPING);
            this.playSound(ModSounds.BASILISK_HOP.get(), 0.5F, 0.8F + this.random.nextFloat() * 0.3F);
        }
    }

    private void exhale(ServerLevel level, LivingEntity target) {
        Vec3 forward = target.position().subtract(this.position()).multiply(1, 0, 1).normalize();
        Vec3 mouth = this.position().add(0, 0.45, 0);
        Vec3 desired = mouth.add(forward.scale(2.0));
        var hit = level.clip(new ClipContext(mouth, desired, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 center = hit.getType() == HitResult.Type.MISS ? desired : hit.getLocation().subtract(forward.scale(0.2));
        level.addFreshEntity(DeathBlightCloud.create(level, new Vec3(center.x, this.getY() + 0.1, center.z), this));
        // The release reads as a cone; its lingering area is the 3.5-block-radius cloud.
        for (int step = 1; step <= 6; step++) {
            Vec3 point = mouth.add(forward.scale(step * 0.45));
            level.sendParticles(ParticleTypes.SQUID_INK, point.x, point.y, point.z, 5,
                    step * 0.10, step * 0.05, step * 0.10, 0.015);
            level.sendParticles(DeathBlight.GOLD, point.x, point.y, point.z, 3,
                    step * 0.08, step * 0.05, step * 0.08, 0);
        }
        this.playSound(ModSounds.BASILISK_EXHALE.get(), 0.9F, 0.65F);
        this.entityData.set(INHALE, 0);
        this.setState(State.EXHALING);
        this.exhaleTicks = 12;
        // A second breath can overlap the first cloud while victims try to escape.
        this.breathCooldown = 85 + this.random.nextInt(20);
    }

    private final class LurkGoal extends Goal {
        LurkGoal() { this.setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() {
            return Basilisk.this.getTarget() == null && (Basilisk.this.isInWater()
                    || Basilisk.this.getBlockStateOn().is(Blocks.MUD) || Basilisk.this.getBlockStateOn().is(Blocks.SCULK));
        }
        @Override public void start() {
            Basilisk.this.getNavigation().stop();
            Basilisk.this.setState(State.LURKING);
        }
    }

    private final class CombatGoal extends Goal {
        private boolean opened;

        CombatGoal() { this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() {
            LivingEntity target = Basilisk.this.getTarget();
            return target != null && target.isAlive() && Basilisk.this.canAttack(target);
        }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void start() { this.opened = false; }
        @Override public void stop() {
            Basilisk.this.cancelInhale();
            Basilisk.this.getNavigation().stop();
        }
        @Override public void tick() {
            Basilisk mob = Basilisk.this;
            LivingEntity target = mob.getTarget();
            if (target == null) return;
            mob.getLookControl().setLookAt(target, 40, 40);
            double distance = mob.distanceToSqr(target);
            if (mob.getState() == State.INHALING) {
                mob.getNavigation().stop();
                int elapsed = mob.entityData.get(INHALE) + 1;
                mob.entityData.set(INHALE, elapsed);
                if (elapsed >= INHALE_TICKS) {
                    if (distance < 100 && mob.getSensing().hasLineOfSight(target)) mob.exhale((ServerLevel) mob.level(), target);
                    else { mob.cancelInhale(); mob.breathCooldown = 20; }
                }
                return;
            }
            if (mob.getState() == State.EXHALING) return;
            if (!this.opened && distance < 36 && mob.onGround()) {
                mob.getNavigation().stop();
                mob.hopTowards(target.position().add(target.position().subtract(mob.position()).normalize()), true);
                this.opened = true;
            } else if (mob.breathCooldown == 0 && distance < 49
                    && (mob.onGround() || mob.isInWater()) && mob.getSensing().hasLineOfSight(target)) {
                mob.getNavigation().stop();
                mob.setState(State.INHALING);
                mob.entityData.set(INHALE, 0);
                mob.playSound(ModSounds.BASILISK_INHALE.get(), 0.85F, 0.6F);
            } else if (mob.breathCooldown > 35 && distance < 49) {
                // Circle the victim while the cloud is active instead of standing in melee range.
                Vec3 away = mob.position().subtract(target.position()).multiply(1, 0, 1).normalize();
                double side = mob.getId() % 2 == 0 ? 1 : -1;
                Vec3 tangent = new Vec3(-away.z * side, 0, away.x * side);
                mob.getNavigation().stop();
                mob.hopTowards(mob.position().add(tangent.scale(3)).add(away.scale(distance < 9 ? 2 : -0.5)), false);
            } else {
                mob.getNavigation().moveTo(target, 1.0);
            }
            if (mob.clawCooldown == 0 && distance < 3.2 && !mob.onGround()
                    && mob.getSensing().hasLineOfSight(target)) {
                mob.doHurtTarget((ServerLevel) mob.level(), target);
                mob.clawCooldown = 24;
            }
        }
    }

    private final class RepelGoal extends Goal {
        private @Nullable Player light;

        RepelGoal() { this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() {
            this.light = Basilisk.this.level().getNearestPlayer(Basilisk.this.getX(), Basilisk.this.getY(), Basilisk.this.getZ(),
                    8, entity -> entity instanceof Player player && player.isAlive() && !player.isSpectator()
                            && !player.isCreative() && Basilisk.this.isRepelledBy(player));
            return this.light != null;
        }
        @Override public boolean canContinueToUse() {
            return this.light != null && this.light.isAlive() && !this.light.isSpectator() && !this.light.isCreative()
                    && Basilisk.this.isRepelledBy(this.light) && Basilisk.this.distanceToSqr(this.light) < 100;
        }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void start() {
            Basilisk.this.cancelInhale();
            Basilisk.this.exhaleTicks = 0;
            Basilisk.this.setState(State.REPELLED);
            Basilisk.this.setTarget(null);
        }
        @Override public void tick() {
            if (this.light == null) return;
            Basilisk mob = Basilisk.this;
            mob.cancelInhale();
            mob.getLookControl().setLookAt(this.light, 35, 35);
            mob.getNavigation().stop();
            if (mob.distanceToSqr(this.light) < 36) {
                Vec3 away = mob.position().subtract(this.light.position()).multiply(1, 0, 1).normalize();
                mob.hopTowards(mob.position().add(away.scale(4)), false);
            }
            mob.setState(State.REPELLED);
        }
        @Override public void stop() { Basilisk.this.setState(State.LURKING); }
    }

    /** Navigation chooses safe routes; the movement controller follows them with short frog-like hops. */
    private final class HopMoveControl extends MoveControl {
        HopMoveControl() { super(Basilisk.this); }
        @Override public void tick() {
            Basilisk mob = Basilisk.this;
            if (this.hasWanted()) {
                Vec3 destination = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
                double dx = this.wantedX - mob.getX(), dz = this.wantedZ - mob.getZ();
                mob.setYRot(this.rotlerp(mob.getYRot(), (float) (Math.atan2(dz, dx) * 180 / Math.PI) - 90, 45));
                mob.hopTowards(destination, false);
                this.operation = Operation.WAIT;
            }
            mob.setZza(0);
            mob.setXxa(0);
        }
    }

    public static boolean checkSpawnRules(EntityType<Basilisk> type, ServerLevelAccessor level,
                                         EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL || pos.getY() >= 0
                || level.getMaxLocalRawBrightness(pos) > 5 || level.canSeeSky(pos)) return false;
        var biome = level.getBiome(pos);
        return (biome.is(Biomes.DEEP_DARK) || biome.is(Biomes.LUSH_CAVES) || biome.is(Biomes.MANGROVE_SWAMP))
                && Mob.checkMobSpawnRules(type, level, reason, pos, random);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        var biome = level.getBiome(this.blockPosition());
        this.dampOrigin = biome.is(Biomes.LUSH_CAVES) || biome.is(Biomes.MANGROVE_SWAMP);
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (this.random.nextFloat() < 0.25F) this.spawnAtLocation(level, new ItemStack(ModItems.CATACOMB_LILY.get(), 1 + this.random.nextInt(2)));
        if (this.dampOrigin && this.random.nextFloat() < 0.08F) this.spawnAtLocation(level, ModItems.ROTTEN_BUTTERFLY.get());
        if (this.random.nextFloat() < 0.05F) this.spawnAtLocation(level, ModItems.BASILISK_EYE_CYST.get());
    }

    @Override protected SoundEvent getAmbientSound() { return ModSounds.BASILISK_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModSounds.BASILISK_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModSounds.BASILISK_DEATH.get(); }
    @Override public int getAmbientSoundInterval() { return 140; }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("DampOrigin", this.dampOrigin);
        output.putInt("BreathCooldown", this.breathCooldown);
        if (this.provokedBy != null) output.putString("ProvokedBy", this.provokedBy.toString());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.dampOrigin = input.getBooleanOr("DampOrigin", false);
        this.breathCooldown = Math.max(20, input.getIntOr("BreathCooldown", 30));
        String id = input.getStringOr("ProvokedBy", "");
        try { this.provokedBy = id.isEmpty() ? null : UUID.fromString(id); }
        catch (IllegalArgumentException ignored) { this.provokedBy = null; }
    }
}
