package com.codenamecoraline.client.redcap;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-only registration of the Redcap model and renderer. The renderer events are
 * mod-bus events, so no explicit bus is needed. Kept out of the dedicated server.
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID, value = Dist.CLIENT)
public final class RedcapClient {
    private RedcapClient() {}

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RedcapModel.LAYER, RedcapModel::createBodyLayer);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.REDCAP.get(), RedcapRenderer::new);
        // The pebble sprite is animated (spinning); drawn at half size.
        event.registerEntityRenderer(ModEntities.REDCAP_ROCK.get(), context -> new ThrownItemRenderer<>(context, 0.5F, false));
    }
}
