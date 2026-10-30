package com.codenamecoraline;

import com.codenamecoraline.registry.ModCreativeTabs;
import com.codenamecoraline.registry.ModEffects;
import com.codenamecoraline.registry.ModPotions;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import com.codenamecoraline.registry.ModSounds;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Mod entry point. Only coordinates registration: registry classes live in
 * com.codenamecoraline.registry and gameplay code in its own packages (see docs/ARCHITECTURE.md).
 */
@Mod(CodenameCoraline.MOD_ID)
public final class CodenameCoraline {
    // Must match mod_id in gradle.properties.
    public static final String MOD_ID = "codenamecoraline";

    private static final Logger LOGGER = LogUtils.getLogger();

    public CodenameCoraline(IEventBus modEventBus) {
        ModSounds.register(modEventBus);
        ModEffects.register(modEventBus);
        ModPotions.register(modEventBus);
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        LOGGER.info("{} loaded (entities, companions, effects and Bestiary registered)", MOD_ID);
    }
}
