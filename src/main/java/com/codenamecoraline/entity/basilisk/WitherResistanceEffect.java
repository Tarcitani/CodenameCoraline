package com.codenamecoraline.entity.basilisk;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.registry.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/** Clears existing Wither and Blindness and prevents reapplication for the duration of the potion. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class WitherResistanceEffect extends MobEffect {
    public WitherResistanceEffect() { super(MobEffectCategory.BENEFICIAL, 0xA5B66D); }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        super.onEffectStarted(entity, amplifier);
        if (!entity.level().isClientSide()) {
            entity.removeEffect(MobEffects.WITHER);
            entity.removeEffect(MobEffects.BLINDNESS);
        }
    }

    @SubscribeEvent
    public static void preventWither(MobEffectEvent.Applicable event) {
        if ((event.getEffectInstance().is(MobEffects.WITHER)
                || event.getEffectInstance().is(MobEffects.BLINDNESS))
                && event.getEntity().hasEffect(ModEffects.WITHER_RESISTANCE)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }
}
