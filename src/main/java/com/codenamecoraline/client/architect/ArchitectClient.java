package com.codenamecoraline.client.architect;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Client-only registration of the model layer, the renderer, the reveal cinematic and the scimitar's poses. */
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
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(ArchitectScimitarClient.INSTANCE, ModItems.ARCHITECT_SCIMITAR.get());
    }

    @SubscribeEvent
    static void onClientTickPost(ClientTickEvent.Post event) {
        ArchitectCinematicDirector.onClientTick(event);
        ArchitectScimitarClient.onClientTick(event);
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
