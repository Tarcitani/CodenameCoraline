package com.codenamecoraline.item;

import com.codenamecoraline.entity.collector.GiftMimic;
import com.codenamecoraline.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The Renegade's Greataxe: a real greataxe, and the Collector's gifts answer it. Right-click to call three Gift Mimics
 * to fight for you; every call wears the axe (it will not break from calling, it just refuses when too worn).
 */
public final class RenegadeGreataxeItem extends Item {
    public static final int SUMMON_COUNT = 3;
    public static final int SUMMON_DURABILITY_COST = 250;
    public static final int SUMMON_COOLDOWN_TICKS = 25 * 20;

    public RenegadeGreataxeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        if (stack.getMaxDamage() - stack.getDamageValue() <= SUMMON_DURABILITY_COST) {
            player.sendOverlayMessage(Component.translatable("item.codenamecoraline.renegade_greataxe.too_worn")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResult.FAIL;
        }
        int summoned = summonAllies(serverLevel, player, SUMMON_COUNT);
        if (summoned == 0) {
            player.sendOverlayMessage(Component.translatable("item.codenamecoraline.renegade_greataxe.too_many")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResult.FAIL;
        }
        stack.hurtAndBreak(SUMMON_DURABILITY_COST, player, hand);
        player.getCooldowns().addCooldown(stack, SUMMON_COOLDOWN_TICKS);
        serverLevel.playSound(null, player.blockPosition(), ModSounds.COLLECTOR_BELL_PREP.get(),
                SoundSource.PLAYERS, 1.2F, 1.3F);
        serverLevel.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(),
                20, 1.2D, 0.6D, 1.2D, 0.05D);
        return InteractionResult.SUCCESS;
    }

    /** Spawns up to {@code count} allied Gift Mimics around the player, never exceeding the ally cap. Returns how many. */
    public static int summonAllies(ServerLevel level, Player owner, int count) {
        int room = Math.min(count, GiftMimic.MAX_ALLIES - GiftMimic.countAllies(level, owner));
        int spawned = 0;
        Vec3 look = owner.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        Vec3 around = owner.position().add(look.lengthSqr() > 1.0E-4D ? look.normalize().scale(1.5D) : Vec3.ZERO);
        for (int i = 0; i < room; i++) {
            if (GiftMimic.spawnAllied(level, owner, around, 1.2D, 3.0D)) {
                spawned++;
            }
        }
        return spawned;
    }
}
