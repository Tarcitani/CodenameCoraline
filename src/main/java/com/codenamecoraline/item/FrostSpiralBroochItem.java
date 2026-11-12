package com.codenamecoraline.item;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Brooch of the Frigid Spiral ({@code frost_spiral_brooch}):
 * <ul>
 *   <li>While carried anywhere in the inventory or offhand, grants complete immunity to powder-snow freezing.</li>
 *   <li>Grants {@code +20%} movement speed while standing on snow or ice blocks.</li>
 * </ul>
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class FrostSpiralBroochItem extends Item {
    public static final Identifier FROST_STRIDE_MODIFIER_ID =
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "frost_spiral_brooch_speed");
    public static final double SNOW_ICE_SPEED_BONUS = 0.20D;

    private static final AttributeModifier FROST_STRIDE_MODIFIER = new AttributeModifier(
            FROST_STRIDE_MODIFIER_ID,
            SNOW_ICE_SPEED_BONUS,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    public FrostSpiralBroochItem(Properties properties) {
        super(properties);
    }

    /** Evaluates and applies/removes the brooch's freeze immunity and +20% snow/ice speed bonus for {@code player}. */
    public static void tickBroochEffects(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        boolean carriesBrooch = hasBroochInInventory(player);
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (!carriesBrooch) {
            if (speedAttr != null && speedAttr.hasModifier(FROST_STRIDE_MODIFIER_ID)) {
                speedAttr.removeModifier(FROST_STRIDE_MODIFIER_ID);
            }
            return;
        }

        if (player.getTicksFrozen() > 0) {
            player.setTicksFrozen(0);
        }

        if (speedAttr != null) {
            boolean onSnowOrIce = isStandingOnSnowOrIce(player.level(), player.blockPosition());
            if (onSnowOrIce) {
                if (!speedAttr.hasModifier(FROST_STRIDE_MODIFIER_ID)) {
                    speedAttr.addTransientModifier(FROST_STRIDE_MODIFIER);
                }
            } else if (speedAttr.hasModifier(FROST_STRIDE_MODIFIER_ID)) {
                speedAttr.removeModifier(FROST_STRIDE_MODIFIER_ID);
            }
        }
    }

    public static boolean hasBroochInInventory(Player player) {
        if (player.getOffhandItem().is(ModItems.FROST_SPIRAL_BROOCH.get())
                || player.getMainHandItem().is(ModItems.FROST_SPIRAL_BROOCH.get())) {
            return true;
        }
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(ModItems.FROST_SPIRAL_BROOCH.get())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isStandingOnSnowOrIce(Level level, BlockPos pos) {
        return isSnowOrIceBlock(level.getBlockState(pos))
                || isSnowOrIceBlock(level.getBlockState(pos.below()));
    }

    public static boolean isSnowOrIceBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        return state.is(BlockTags.SNOW)
                || state.is(BlockTags.ICE)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.SNOW)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.FROSTED_ICE);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        tickBroochEffects(event.getEntity());
    }
}

