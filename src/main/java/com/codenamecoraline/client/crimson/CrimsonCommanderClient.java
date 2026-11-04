package com.codenamecoraline.client.crimson;

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

/** Client-only registration of the Crimson Commander model layer and the renderers of the boss, his spirit and vanguards. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID, value = Dist.CLIENT)
public final class CrimsonCommanderClient {
    private CrimsonCommanderClient() {}

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CrimsonCommanderModel.LAYER, CrimsonCommanderModel::createBodyLayer);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CRIMSON_COMMANDER.get(), CrimsonCommanderRenderer::new);
        event.registerEntityRenderer(ModEntities.CRIMSON_ASH_VANGUARD.get(), CrimsonAshVanguardRenderer::new);
        event.registerEntityRenderer(ModEntities.CRIMSON_COMMANDER_SPIRIT.get(), CrimsonCommanderSpiritRenderer::new);
    }

    @SubscribeEvent
    static void onClientTickPost(ClientTickEvent.Post event) {
        CrimsonCinematicDirector.onClientTick(event);
    }

    @SubscribeEvent
    static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        CrimsonCinematicDirector.onMovementInputUpdate(event);
    }

    @SubscribeEvent
    static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        CrimsonCinematicDirector.onComputeCameraAngles(event);
    }

    @SubscribeEvent
    static void onComputeFov(ViewportEvent.ComputeFov event) {
        CrimsonCinematicDirector.onComputeFov(event);
    }

    @SubscribeEvent
    static void onRenderGuiPost(RenderGuiEvent.Post event) {
        CrimsonCinematicDirector.onRenderGuiPost(event);
    }
}
