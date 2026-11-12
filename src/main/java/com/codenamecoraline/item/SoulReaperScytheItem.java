package com.codenamecoraline.item;

import com.codenamecoraline.entity.PlayerSummon;
import com.codenamecoraline.registry.ModSounds;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Reaper of Wandering Souls' Scythe ({@code soul_reaper_scythe}):
 * <ul>
 *   <li>10 attack damage, 1.2 attack speed.</li>
 *   <li>Charged melee strikes unleash a 360-degree glacial sweep around the wielder.</li>
 *   <li>Active skill <b>Spiritual Reap (Segada Espiritual)</b> (Shift + Right-Click, 300-tick cooldown): lunges 3 blocks
 *       forward, slashing enemies along the path and stealing 2 HP per target hit (up to 6 HP total).</li>
 * </ul>
 */
public final class SoulReaperScytheItem extends Item {
    public static final double SPIRIT_REAP_DISTANCE = 3.0D;
    public static final float SPIRIT_REAP_DAMAGE = 10.0F;
    public static final float LIFE_STEAL_PER_HIT = 2.0F;
    public static final float MAX_LIFE_STEAL = 6.0F;
    public static final int SPIRIT_REAP_COOLDOWN_TICKS = 300;
    public static final int SPIRIT_REAP_DURABILITY_COST = 6;
    public static final double SWEEP_RADIUS = 3.2D;
    public static final float SWEEP_DAMAGE = 5.0F;

    public SoulReaperScytheItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker instanceof Player player && player.level() instanceof ServerLevel serverLevel) {
            performCharged360Sweep(serverLevel, player, target);
        }
    }

    /** Executes the 360-degree glacial sweep around {@code player}, hitting nearby enemies other than {@code primary}. */
    public static int performCharged360Sweep(ServerLevel level, Player player, LivingEntity primary) {
        AABB box = player.getBoundingBox().inflate(SWEEP_RADIUS, 1.0D, SWEEP_RADIUS);
        List<LivingEntity> nearby = level.getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> e != player
                        && e != primary
                        && e.isAlive()
                        && isValidScytheTarget(player, e));
        int hits = 0;
        for (LivingEntity other : nearby) {
            other.invulnerableTime = 0;
            if (other.hurtServer(level, level.damageSources().playerAttack(player), SWEEP_DAMAGE)) {
                hits++;
            }
        }
        level.sendParticles(
                ParticleTypes.SWEEP_ATTACK,
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                4,
                0.8D,
                0.2D,
                0.8D,
                0.0D);
        level.sendParticles(
                ParticleTypes.SOUL,
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                8,
                1.2D,
                0.3D,
                1.2D,
                0.02D);
        return hits;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack) || player.isPassenger()) {
            return InteractionResult.FAIL;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        performSpiritualReap(serverLevel, player, stack, hand);
        return InteractionResult.SUCCESS;
    }

    /**
     * Executes the active skill <b>Spiritual Reap</b>: advances 3 blocks forward, cuts enemies along the path, and heals
     * the wielder by 2 HP per enemy hit (capped at 6 HP). Returns the amount of health restored.
     */
    public static float performSpiritualReap(
            ServerLevel level,
            Player player,
            ItemStack stack,
            InteractionHand hand) {
        Vec3 horizontalLook = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        Vec3 dir = horizontalLook.lengthSqr() > 1.0E-4D
                ? horizontalLook.normalize()
                : Vec3.directionFromRotation(0.0F, player.getYRot());
        Vec3 start = player.position();
        Vec3 step = dir.scale(SPIRIT_REAP_DISTANCE);

        player.setDeltaMovement(dir.x * 1.15D, Math.max(player.getDeltaMovement().y, 0.15D), dir.z * 1.15D);
        player.hurtMarked = true;

        AABB sweepBox = player.getBoundingBox().inflate(1.8D, 0.8D, 1.8D).expandTowards(step);
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                sweepBox,
                e -> e != player && e.isAlive() && isValidScytheTarget(player, e));

        int hitCount = 0;
        for (LivingEntity target : targets) {
            target.invulnerableTime = 0;
            if (target.hurtServer(level, level.damageSources().playerAttack(player), SPIRIT_REAP_DAMAGE)) {
                hitCount++;
            }
        }

        float healAmount = Math.min(MAX_LIFE_STEAL, hitCount * LIFE_STEAL_PER_HIT);
        if (healAmount > 0.0F) {
            player.heal(healAmount);
        }

        player.getCooldowns().addCooldown(stack, SPIRIT_REAP_COOLDOWN_TICKS);
        stack.hurtAndBreak(SPIRIT_REAP_DURABILITY_COST, player, hand);
        player.swing(hand, true);

        level.playSound(
                null,
                player.blockPosition(),
                ModSounds.SOUL_REAPER_SCYTHE_SLASH.get(),
                SoundSource.PLAYERS,
                1.2F,
                0.85F);
        level.sendParticles(
                ParticleTypes.SOUL,
                start.x + step.x * 0.5D,
                start.y + 1.0D,
                start.z + step.z * 0.5D,
                16,
                1.2D,
                0.4D,
                1.2D,
                0.03D);
        level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                start.x + step.x * 0.5D,
                start.y + 1.0D,
                start.z + step.z * 0.5D,
                18,
                1.2D,
                0.4D,
                1.2D,
                0.02D);
        return healAmount;
    }

    private static boolean isValidScytheTarget(Player player, LivingEntity candidate) {
        if (candidate instanceof PlayerSummon summon && summon.isPlayerSummon()) {
            return false;
        }
        if (candidate.isPassengerOfSameVehicle(player)) {
            return false;
        }
        return candidate instanceof Enemy || candidate instanceof IronGolem;
    }
}
