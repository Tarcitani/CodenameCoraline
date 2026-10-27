package com.codenamecoraline;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Mod entry point. Only loads the mod for now: registries and gameplay code
 * belong in their own packages once requirements define them (see docs/ARCHITECTURE.md).
 */
@Mod(CodenameCoraline.MOD_ID)
public final class CodenameCoraline {
    // Must match mod_id in gradle.properties.
    public static final String MOD_ID = "codenamecoraline";

    private static final Logger LOGGER = LogUtils.getLogger();

    public CodenameCoraline() {
        LOGGER.info("{} loaded (technical foundation, no gameplay yet)", MOD_ID);
    }
}
