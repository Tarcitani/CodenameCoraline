package com.codenamecoraline.item;

import com.codenamecoraline.entity.macaque.BarkMacaque;
import com.codenamecoraline.item.DeadrootLeapSlam.LeapResult;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

/**
 * Deadroot Club. Two abilities, like the Renegade's Greataxe has one summon:
 * <ul>
 *   <li><b>Leap (hold right-click on the ground).</b> The longer the button is held (up to {@link #MAX_CHARGE_TICKS}),
 *       the higher and farther the leap. Releasing launches it; see {@link DeadrootLeapSlam}.</li>
 *   <li><b>Summon (Shift + right-click).</b> Calls Bark Macaques to fight beside you, like the greataxe's Gift Mimics.</li>
 * </ul>
 * Both cost durability and share the club's cooldown.
 */
public final class DeadrootClubItem extends Item {
    public static final int LEAP_COOLDOWN_TICKS = 12 * 20;
    public static final int SUMMON_COOLDOWN_TICKS = 20 * 20;
    public static final int LEAP_DURABILITY_COST = 4;
    public static final int SUMMON_DURABILITY_COST = 120;
    public static final int SUMMON_COUNT = 3;
    /** Ticks of holding that reach the full charge (1.5 s). */
    public static final int MAX_CHARGE_TICKS = 30;
    /** Vanilla bow-style "held" duration: the item is never finished by the timer, only released. */
    public static final int USE_DURATION = 72000;

    public DeadrootClubItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            return summon(level, player, stack, hand);
        }
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (!player.onGround()) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("item.codenamecoraline.deadroot_club.airborne")
                        .withStyle(ChatFormatting.GRAY));
            }
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand); // charge the leap; released in releaseUsing
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return true;
        }
        if (!player.onGround()) {
            serverPlayer.sendOverlayMessage(Component.translatable("item.codenamecoraline.deadroot_club.airborne")
                    .withStyle(ChatFormatting.GRAY));
            return false;
        }
        double charge = Math.min(1.0D, (USE_DURATION - timeLeft) / (double) MAX_CHARGE_TICKS);
        LeapResult result = DeadrootLeapSlam.launch(serverPlayer, charge);
        if (result != LeapResult.STARTED) {
            serverPlayer.sendOverlayMessage(Component.translatable("item.codenamecoraline.deadroot_club.busy")
                    .withStyle(ChatFormatting.GRAY));
            return false;
        }
        InteractionHand hand = serverPlayer.getUsedItemHand();
        stack.hurtAndBreak(LEAP_DURABILITY_COST, serverPlayer, hand);
        serverPlayer.getCooldowns().addCooldown(stack, LEAP_COOLDOWN_TICKS);
        serverPlayer.swing(hand);
        return true;
    }

    /** Calls Bark Macaques to fight beside the player, with the greataxe's rules: durability cost, cooldown, a cap. */
    public static InteractionResult summon(Level level, Player player, ItemStack stack, InteractionHand hand) {
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        if (stack.getMaxDamage() - stack.getDamageValue() <= SUMMON_DURABILITY_COST) {
            serverPlayer.sendOverlayMessage(Component.translatable("item.codenamecoraline.deadroot_club.too_worn")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResult.FAIL;
        }
        int summoned = BarkMacaque.summonAllies(serverLevel, serverPlayer, SUMMON_COUNT);
        if (summoned == 0) {
            serverPlayer.sendOverlayMessage(Component.translatable("item.codenamecoraline.deadroot_club.summon_full")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResult.FAIL;
        }
        stack.hurtAndBreak(SUMMON_DURABILITY_COST, serverPlayer, hand);
        serverPlayer.getCooldowns().addCooldown(stack, SUMMON_COOLDOWN_TICKS);
        serverPlayer.swing(hand);
        serverLevel.sendParticles(ParticleTypes.END_ROD, serverPlayer.getX(), serverPlayer.getY() + 1.0D, serverPlayer.getZ(),
                20, 1.2D, 0.6D, 1.2D, 0.05D);
        return InteractionResult.SUCCESS;
    }
}
