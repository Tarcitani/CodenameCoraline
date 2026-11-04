package com.codenamecoraline.item;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.PlayerSummon;
import com.codenamecoraline.entity.crimson.CrimsonCommander;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Crimson Oath Greatsword: the Commander's colossal blade. A slow, heavy sword; right-click performs the <b>Crimson
 * Dash</b>: a fast advance of {@link #DASH_DISTANCE} blocks along the direction the player faces that cuts every hostile
 * creature ({@code Enemy}) in its line for {@link #DASH_DAMAGE}, leaving a trail of red particles. It costs
 * {@link #DASH_DURABILITY_COST} durability and goes on a {@link #DASH_COOLDOWN_TICKS}-tick cooldown. Players, pets,
 * mounts, villagers and the player's own summons are never cut; it cannot be used while riding.
 *
 * <p>The dash state lives on the server, keyed by player, and advances in the player tick.</p>
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class CrimsonOathGreatswordItem extends Item {
    public static final double DASH_DISTANCE = 6.0D;
    public static final int DASH_TICKS = 6;
    public static final float DASH_DAMAGE = 8.0F;
    public static final int DASH_DURABILITY_COST = 8;
    public static final int DASH_COOLDOWN_TICKS = 120;

    private static final Map<UUID, Dash> DASHES = new HashMap<>();

    private static final class Dash {
        final Vec3 from;
        final Vec3 dir;
        int ticks;
        final Set<Integer> hit = new HashSet<>();

        Dash(Vec3 from, Vec3 dir) {
            this.from = from;
            this.dir = dir;
        }
    }

    public CrimsonOathGreatswordItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (player.isPassenger()) {
            return InteractionResult.FAIL; // a mount carries the rider: there is nothing to dash with
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!startDash(player)) {
            return InteractionResult.FAIL;
        }
        player.getCooldowns().addCooldown(stack, DASH_COOLDOWN_TICKS); // before the wear: a sword that breaks has no item left
        stack.hurtAndBreak(DASH_DURABILITY_COST, player, hand);
        player.swing(hand, true);
        return InteractionResult.SUCCESS;
    }

    public static boolean isDashing(Player player) {
        return DASHES.containsKey(player.getUUID());
    }

    /** Starts the dash along the player's horizontal facing. False if one is already under way. */
    public static boolean startDash(Player player) {
        if (isDashing(player)) {
            return false;
        }
        Vec3 look = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        Vec3 dir = look.lengthSqr() > 1.0E-4D ? look.normalize() : Vec3.directionFromRotation(0.0F, player.getYRot());
        DASHES.put(player.getUUID(), new Dash(player.position(), dir));
        player.level().playSound(null, player.blockPosition(), com.codenamecoraline.registry.ModSounds.CRIMSON_DASH.get(),
                SoundSource.PLAYERS, 1.0F, 1.1F);
        return true;
    }

    /** One tick of the dash (called from the player tick; tests call it directly for mock players). */
    public static void tickDash(Player player) {
        Dash dash = DASHES.get(player.getUUID());
        if (dash == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (!player.isAlive() || player.isRemoved() || player.isPassenger()) {
            DASHES.remove(player.getUUID());
            return;
        }
        dash.ticks++;
        // a fixed schedule (one velocity per tick for DASH_TICKS ticks), not the distance the server has seen so far:
        // the client moves the player, and the server's view of the position lags behind it
        Vec3 step = dash.dir.scale(DASH_DISTANCE / DASH_TICKS);
        player.setDeltaMovement(step.x, Math.min(player.getDeltaMovement().y, 0.1D), step.z);
        player.hurtMarked = true;
        for (int i = 0; i < 4; i++) {
            Vec3 p = player.position().add(step.scale(i / 4.0D));
            level.sendParticles(i % 2 == 0 ? CrimsonCommander.CRIMSON_DUST : CrimsonCommander.RUBY_DUST, p.x, p.y + 1.0D, p.z,
                    1, 0.1D, 0.2D, 0.1D, 0.0D);
        }
        AABB sweep = player.getBoundingBox().inflate(0.8D, 0.2D, 0.8D).expandTowards(step);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, sweep, e -> e != player && e.isAlive()
                && e instanceof Enemy && !(e instanceof PlayerSummon summon && summon.isPlayerSummon()) && !e.isPassengerOfSameVehicle(player))) {
            if (dash.hit.add(e.getId())) {
                e.hurtServer(level, level.damageSources().playerAttack(player), DASH_DAMAGE);
            }
        }
        if (dash.ticks >= DASH_TICKS || (dash.ticks > 1 && player.horizontalCollision)) {
            DASHES.remove(player.getUUID());
            player.setDeltaMovement(player.getDeltaMovement().multiply(0.2D, 1.0D, 0.2D));
            player.hurtMarked = true;
        }
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() && isDashing(player)) {
            tickDash(player);
        }
    }

    @SubscribeEvent
    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        DASHES.remove(event.getEntity().getUUID());
    }
}
