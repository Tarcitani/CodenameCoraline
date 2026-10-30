package com.codenamecoraline.client.canopy;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Client-only registration of the Canopy Breaker model layer, renderers (boss and spirit) and intro cinematic. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID, value = Dist.CLIENT)
public final class CanopyBreakerClient {
    private CanopyBreakerClient() {}

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CanopyBreakerModel.LAYER, CanopyBreakerModel::createBodyLayer);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CANOPY_BREAKER.get(), CanopyBreakerRenderer::new);
        event.registerEntityRenderer(ModEntities.CANOPY_BREAKER_SPIRIT.get(), CanopyBreakerSpiritRenderer::new);
    }

    @SubscribeEvent
    static void onClientTickPost(ClientTickEvent.Post event) {
        CanopyCinematicDirector.onClientTick(event);
    }

    @SubscribeEvent
    static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        CanopyCinematicDirector.onMovementInputUpdate(event);
    }

    @SubscribeEvent
    static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        CanopyCinematicDirector.onComputeCameraAngles(event);
    }

    @SubscribeEvent
    static void onComputeFov(ViewportEvent.ComputeFov event) {
        CanopyCinematicDirector.onComputeFov(event);
    }

    @SubscribeEvent
    static void onRenderGuiPost(RenderGuiEvent.Post event) {
        CanopyCinematicDirector.onRenderGuiPost(event);
    }
}
