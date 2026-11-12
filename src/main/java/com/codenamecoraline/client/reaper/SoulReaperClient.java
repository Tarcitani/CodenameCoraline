package com.codenamecoraline.client.reaper;

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

@EventBusSubscriber(modid = CodenameCoraline.MOD_ID, value = Dist.CLIENT)
public final class SoulReaperClient {
    private SoulReaperClient() {}

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SoulReaperModel.LAYER_LOCATION, SoulReaperModel::createBodyLayer);
        event.registerLayerDefinition(OmenHareModel.LAYER_LOCATION, OmenHareModel::createBodyLayer);
        event.registerLayerDefinition(WanderingSoulWispModel.LAYER_LOCATION, WanderingSoulWispModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SOUL_REAPER.get(), SoulReaperRenderer::new);
        event.registerEntityRenderer(ModEntities.OMEN_HARE.get(), OmenHareRenderer::new);
        event.registerEntityRenderer(ModEntities.WANDERING_SOUL_WISP.get(), WanderingSoulWispRenderer::new);
        event.registerEntityRenderer(ModEntities.SOUL_REAPER_SPIRIT.get(), SoulReaperSpiritRenderer::new);
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        SoulReaperCinematicDirector.onClientTick(event);
    }

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        SoulReaperCinematicDirector.onMovementInputUpdate(event);
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        SoulReaperCinematicDirector.onComputeCameraAngles(event);
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        SoulReaperCinematicDirector.onComputeFov(event);
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        SoulReaperCinematicDirector.onRenderGuiPost(event);
    }
}
