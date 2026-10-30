package com.codenamecoraline.item;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.PlayerSummon;
import com.codenamecoraline.registry.ModSounds;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The flight-and-slam of the Deadroot Club, in the manner of a thrown war hammer: the club pulls the player along the exact
 * direction they look at (up, level or down) for a few ticks of sustained thrust, then the player glides on momentum. Hold
 * Shift in the air to dive straight down. Whenever the flight ends on the ground the club hits it: every hostile mob within
 * {@link #RADIUS} takes damage, the one right under the impact at full strength and the rest at a reduced share. The damage
 * grows with the height the player fell from, so a dive from high up hits hardest. The player takes no fall damage.
 *
 * <p>The state lives on the server, keyed by player, and advances in the player tick. Allies, player summons and other
 * players are never hit. A flight that never lands (the player dies, changes dimension or logs out) is dropped without a
 * slam.</p>
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class DeadrootLeapSlam {
    /** Speed of the pull along the look direction: a tap gets the MIN, a full charge the MAX. */
    public static final double THRUST_SPEED_MIN = 0.9D;
    public static final double THRUST_SPEED_MAX = 2.0D;
    /** How many ticks the club keeps pulling before the player glides on momentum. */
    public static final int THRUST_TICKS_MIN = 3;
    public static final int THRUST_TICKS_MAX = 10;
    /** A little lift so a level flight clears the ground instead of scraping it. */
    public static final double LIFT = 0.22D;
    public static final double DIVE_SPEED = 1.6D;
    /** Ticks in the air before the landing check starts, so the launch itself is not taken as a landing. */
    public static final int MIN_AIR_TICKS = 4;
    /** Hard limit: a flight that has not landed by now is dropped. */
    public static final int MAX_AIR_TICKS = 140;

    public static final float BASE_DAMAGE = 6.0F;
    public static final float DAMAGE_PER_FALL_BLOCK = 1.5F;
    public static final double MAX_FALL_BONUS_BLOCKS = 16.0D;
    public static final double RADIUS = 3.5D;
    public static final double PRIMARY_RADIUS = 1.5D;
    public static final float SECONDARY_SHARE = 0.6F;
    public static final double VERTICAL_REACH = 2.0D;

    private static final Map<UUID, Leap> LEAPS = new HashMap<>();

    public enum LeapResult { STARTED, ALREADY_LEAPING }

    private static final class Leap {
        int airTicks;
        double peakY;
        boolean diving;
        int thrustTicks;
        Vec3 thrust;

        Leap(double startY, Vec3 thrust, int thrustTicks) {
            this.peakY = startY;
            this.thrust = thrust;
            this.thrustTicks = thrustTicks;
        }
    }

    private DeadrootLeapSlam() {}

    public static boolean isLeaping(ServerPlayer player) {
        return LEAPS.containsKey(player.getUUID());
    }

    /** Thrust ticks still to come for this player's flight (0 when gliding or not flying); for tests. */
    public static int thrustTicksLeft(ServerPlayer player) {
        Leap leap = LEAPS.get(player.getUUID());
        return leap == null ? 0 : leap.thrustTicks;
    }

    /** The velocity the club pulls with for a given charge in [0, 1], along {@code look}. */
    public static Vec3 thrustFor(Vec3 look, double charge) {
        double f = Math.max(0.0D, Math.min(1.0D, charge));
        double speed = THRUST_SPEED_MIN + (THRUST_SPEED_MAX - THRUST_SPEED_MIN) * f;
        Vec3 dir = look.lengthSqr() > 1.0E-4D ? look.normalize() : new Vec3(0.0D, 1.0D, 0.0D);
        return dir.scale(speed).add(0.0D, LIFT, 0.0D);
    }

    /**
     * Starts a flight from the ground. {@code charge} in [0, 1] is how long the button was held: it scales the speed of
     * the pull and how long it lasts. The caller handles durability, cooldown and the swing.
     */
    public static LeapResult launch(ServerPlayer player, double charge) {
        if (isLeaping(player)) {
            return LeapResult.ALREADY_LEAPING;
        }
        double f = Math.max(0.0D, Math.min(1.0D, charge));
        Vec3 thrust = thrustFor(player.getLookAngle(), f);
        int ticks = (int) Math.round(THRUST_TICKS_MIN + (THRUST_TICKS_MAX - THRUST_TICKS_MIN) * f);
        player.setDeltaMovement(thrust);
        player.setOnGround(false);
        player.hurtMarked = true;
        LEAPS.put(player.getUUID(), new Leap(player.getY(), thrust, ticks));
        if (player.level() instanceof ServerLevel level) {
            level.playSound(null, player.blockPosition(), ModSounds.CANOPY_JUMP.get(), SoundSource.PLAYERS, 1.2F, 0.9F);
            level.playSound(null, player.blockPosition(), SoundEvents.WIND_CHARGE_THROW, SoundSource.PLAYERS, 1.0F, 0.7F);
        }
        return LeapResult.STARTED;
    }

    /** Advances one flight by one server tick. Public so the tests can drive it without a ticking player. */
    public static void tick(ServerPlayer player) {
        Leap leap = LEAPS.get(player.getUUID());
        if (leap == null) {
            return;
        }
        if (!player.isAlive() || player.isRemoved() || !(player.level() instanceof ServerLevel level)) {
            LEAPS.remove(player.getUUID());
            return;
        }
        leap.airTicks++;
        player.resetFallDistance();
        leap.peakY = Math.max(leap.peakY, player.getY());
        if (leap.thrustTicks > 0) {
            // the club keeps pulling along the launch direction; a wall ends the pull at once
            if (player.horizontalCollision && leap.airTicks > 1) {
                leap.thrustTicks = 0;
            } else {
                leap.thrustTicks--;
                player.setDeltaMovement(leap.thrust);
                player.hurtMarked = true;
                if (leap.airTicks % 2 == 0) {
                    level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.6D, player.getZ(), 2,
                            0.15D, 0.15D, 0.15D, 0.01D);
                }
            }
        } else if (!leap.diving && player.isShiftKeyDown()) {
            leap.diving = true; // the downward blow, on demand
            Vec3 v = player.getDeltaMovement();
            player.setDeltaMovement(v.x * 0.5D, -DIVE_SPEED, v.z * 0.5D);
            player.hurtMarked = true;
        }
        if (leap.airTicks >= MIN_AIR_TICKS && player.onGround()) {
            LEAPS.remove(player.getUUID());
            slam(level, player, Math.max(0.0D, leap.peakY - player.getY()));
        } else if (leap.airTicks > MAX_AIR_TICKS) {
            LEAPS.remove(player.getUUID());
        }
    }

    /** The club hits the ground: area damage around the player, strongest right under the impact. */
    public static void slam(ServerLevel level, ServerPlayer player, double fallBlocks) {
        Vec3 center = player.position();
        float damage = BASE_DAMAGE + (float) (Math.min(fallBlocks, MAX_FALL_BONUS_BLOCKS) * DAMAGE_PER_FALL_BLOCK);
        AABB box = new AABB(center.x - RADIUS, center.y - VERTICAL_REACH, center.z - RADIUS,
                center.x + RADIUS, center.y + VERTICAL_REACH + 1.0D, center.z + RADIUS);
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e instanceof Enemy && e != player && e.isAlive()
                        && !(e instanceof PlayerSummon summon && summon.isPlayerSummon()));
        for (LivingEntity victim : victims) {
            double dx = victim.getX() - center.x;
            double dz = victim.getZ() - center.z;
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            if (horizontal > RADIUS) {
                continue;
            }
            float share = horizontal <= PRIMARY_RADIUS ? 1.0F : SECONDARY_SHARE;
            victim.hurtServer(level, player.damageSources().playerAttack(player), damage * share);
            if (horizontal > 0.01D) {
                double push = 0.45D + 0.45D * share;
                victim.knockback(push, -dx / horizontal, -dz / horizontal);
            }
        }
        player.resetFallDistance();
        impactEffects(level, center);
    }

    /** The weight of the landing: a heavy smash, a burst of dust and debris, and two shockwave rings racing outwards. */
    private static void impactEffects(ServerLevel level, Vec3 center) {
        BlockPos at = BlockPos.containing(center);
        level.playSound(null, at, SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.PLAYERS, 2.4F, 0.85F);
        level.playSound(null, at, ModSounds.CANOPY_CLUB_IMPACT.get(), SoundSource.PLAYERS, 2.2F, 0.7F);
        level.playSound(null, at, ModSounds.CANOPY_LAND_HEAVY.get(), SoundSource.PLAYERS, 1.6F, 0.8F);
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.3D, center.z, 2, 0.4D, 0.1D, 0.4D, 0.0D);
        BlockParticleOption dust = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PODZOL.defaultBlockState());
        BlockParticleOption stone = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState());
        level.sendParticles(dust, center.x, center.y + 0.2D, center.z, 50, RADIUS * 0.5D, 0.2D, RADIUS * 0.5D, 0.12D);
        level.sendParticles(stone, center.x, center.y + 0.1D, center.z, 24, RADIUS * 0.4D, 0.1D, RADIUS * 0.4D, 0.2D);
        // two rings: a tight one at the centre, then the wide one at the edge of the damage area
        for (double ring : new double[] {PRIMARY_RADIUS * 1.2D, RADIUS}) {
            int points = ring > PRIMARY_RADIUS * 2.0D ? 28 : 16;
            for (int i = 0; i < points; i++) {
                double a = Math.PI * 2.0D * i / points;
                double dx = Math.cos(a);
                double dz = Math.sin(a);
                level.sendParticles(ParticleTypes.CLOUD, center.x + dx * ring * 0.5D, center.y + 0.15D, center.z + dz * ring * 0.5D,
                        0, dx, 0.04D, dz, 0.35D);
            }
        }
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            tick(player);
        }
    }

    @SubscribeEvent
    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Entity entity = event.getEntity();
        LEAPS.remove(entity.getUUID());
    }
}
