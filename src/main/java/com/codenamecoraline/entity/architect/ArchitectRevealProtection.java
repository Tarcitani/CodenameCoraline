package com.codenamecoraline.entity.architect;

import com.codenamecoraline.CodenameCoraline;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Server protection during the Architect's reveal (MOB_CONTRACT §5.1): while a statue within
 * {@link Architect#REVEAL_PROTECTION_RADIUS} blocks plays its 110-tick awakening, players take no damage, so the
 * cinematic never costs a life before the fight formally begins. The boss itself is invulnerable in that state.
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class ArchitectRevealProtection {
    private ArchitectRevealProtection() {}

    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        if (isProtected(player)) {
            event.setCanceled(true);
        }
    }

    /** True while some Architect near the player is in its reveal. */
    public static boolean isProtected(Player player) {
        return !player.level().getEntitiesOfClass(Architect.class,
                player.getBoundingBox().inflate(Architect.REVEAL_PROTECTION_RADIUS),
                a -> a.getState() == Architect.State.AWAKENING).isEmpty();
    }
}
