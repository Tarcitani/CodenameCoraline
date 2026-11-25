package com.codenamecoraline.client.firedragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only registration for the Fire Dragon models, renderers, inventory screen and mount keys. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID, value = Dist.CLIENT)
public final class FireDragonClient {
    private FireDragonClient() {}

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FireDragonModel.LAYER, FireDragonModel::createBodyLayer);
        event.registerLayerDefinition(FireDragonModel.YOUNG_LAYER, FireDragonModel::createYoungBodyLayer);
        event.registerLayerDefinition(FireDragonModel.ADULT_LAYER, FireDragonModel::createAdultBodyLayer);
        event.registerLayerDefinition(DragonFireballModel.LAYER, DragonFireballModel::createBodyLayer);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DRAGON_FIREBALL.get(), DragonFireballRenderer::new);
        event.registerEntityRenderer(ModEntities.FIRE_DRAGON.get(), FireDragonRenderer::new);
    }

    @SubscribeEvent
    static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.FIRE_DRAGON.get(), FireDragonScreen::new);
    }

    @SubscribeEvent
    static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(FireDragonMountClient.KEY_METEOR);
        event.register(FireDragonMountClient.KEY_BREATH);
        event.register(FireDragonMountClient.KEY_REND);
        event.register(FireDragonMountClient.KEY_ROAR);
    }
}
