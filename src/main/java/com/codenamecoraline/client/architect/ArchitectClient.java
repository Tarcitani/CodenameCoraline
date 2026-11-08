package com.codenamecoraline.client.architect;

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

/** Client-only registration of the model layer, the renderer and the reveal cinematic of the Architect. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID, value = Dist.CLIENT)
public final class ArchitectClient {
    private ArchitectClient() {}

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ArchitectModel.LAYER, ArchitectModel::createBodyLayer);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ARCHITECT.get(), ArchitectRenderer::new);
    }

    @SubscribeEvent
    static void onClientTickPost(ClientTickEvent.Post event) {
        ArchitectCinematicDirector.onClientTick(event);
    }

    @SubscribeEvent
    static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        ArchitectCinematicDirector.onMovementInputUpdate(event);
    }

    @SubscribeEvent
    static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        ArchitectCinematicDirector.onComputeCameraAngles(event);
    }

    @SubscribeEvent
    static void onComputeFov(ViewportEvent.ComputeFov event) {
        ArchitectCinematicDirector.onComputeFov(event);
    }

    @SubscribeEvent
    static void onRenderGuiPost(RenderGuiEvent.Post event) {
        ArchitectCinematicDirector.onRenderGuiPost(event);
    }
}
