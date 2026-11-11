package com.codenamecoraline.client.firedragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only registration for the Fire Dragon model and renderer. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID, value = Dist.CLIENT)
public final class FireDragonClient {
    private FireDragonClient() {}

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FireDragonModel.LAYER, FireDragonModel::createBodyLayer);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DRAGON_FIREBALL.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.FIRE_DRAGON.get(), FireDragonRenderer::new);
    }
}
