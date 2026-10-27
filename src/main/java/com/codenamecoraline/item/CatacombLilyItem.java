package com.codenamecoraline.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Offer a lily to an iron golem: repairs it and grants thirty seconds of Resistance I. */
public final class CatacombLilyItem extends Item {
    public CatacombLilyItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof IronGolem)) return InteractionResult.PASS;
        if (!target.level().isClientSide()) {
            target.heal(12);
            target.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 600, 0));
            stack.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }
}
