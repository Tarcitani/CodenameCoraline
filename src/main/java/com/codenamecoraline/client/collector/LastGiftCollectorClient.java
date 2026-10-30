package com.codenamecoraline.client.collector;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Client-only registration for the Collector of the Last Gift model, sleigh model,
 * renderers, skip keymapping, and cinematic camera/HUD director events.
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID, value = Dist.CLIENT)
public final class LastGiftCollectorClient {
    private LastGiftCollectorClient() {}

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LastGiftCollectorModel.LAYER, LastGiftCollectorModel::createBodyLayer);
        event.registerLayerDefinition(CollectorSleighModel.LAYER, CollectorSleighModel::createBodyLayer);
        event.registerLayerDefinition(GiftMimicModel.LAYER, GiftMimicModel::createBodyLayer);
        event.registerLayerDefinition(CollectorGiftModel.LAYER, CollectorGiftModel::createBodyLayer);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.LAST_GIFT_COLLECTOR.get(), LastGiftCollectorRenderer::new);
        event.registerEntityRenderer(ModEntities.LAST_GIFT_COLLECTOR_SPIRIT.get(), LastGiftCollectorSpiritRenderer::new);
        event.registerEntityRenderer(ModEntities.COLLECTOR_SLEIGH.get(), CollectorSleighRenderer::new);
        event.registerEntityRenderer(ModEntities.GIFT_MIMIC.get(), GiftMimicRenderer::new);
        event.registerEntityRenderer(ModEntities.COLLECTOR_GIFT.get(), CollectorGiftRenderer::new);
    }

    @SubscribeEvent
    static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CollectorCinematicDirector.SKIP_CINEMATIC_KEY);
    }

    @SubscribeEvent
    static void onClientTickPost(ClientTickEvent.Post event) {
        CollectorCinematicDirector.onClientTick(event);
    }

    @SubscribeEvent
    static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        CollectorCinematicDirector.onMovementInputUpdate(event);
    }

    @SubscribeEvent
    static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        CollectorCinematicDirector.onComputeCameraAngles(event);
    }

    @SubscribeEvent
    static void onComputeFov(ViewportEvent.ComputeFov event) {
        CollectorCinematicDirector.onComputeFov(event);
    }

    @SubscribeEvent
    static void onRenderGuiPost(RenderGuiEvent.Post event) {
        CollectorCinematicDirector.onRenderGuiPost(event);
    }
}

