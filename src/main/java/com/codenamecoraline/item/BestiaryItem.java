package com.codenamecoraline.item;

import com.codenamecoraline.client.bestiary.BestiaryClient;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** The bestiary book. Using it opens the book screen on the client. */
public final class BestiaryItem extends Item {
    public BestiaryItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            // Only reached on the client, so the dedicated server never loads the screen class.
            BestiaryClient.open();
        }
        return InteractionResult.SUCCESS;
    }
}
