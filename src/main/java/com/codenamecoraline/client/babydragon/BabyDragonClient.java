package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid=CodenameCoraline.MOD_ID,value=Dist.CLIENT)
public final class BabyDragonClient {
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BabyDragonModel.LAYER,BabyDragonModel::createBodyLayer);
        event.registerLayerDefinition(YoungDragonModel.LAYER,YoungDragonModel::createBodyLayer);
        event.registerLayerDefinition(AdultDragonModel.LAYER,AdultDragonModel::createBodyLayer);
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BABY_DRAGON.get(),BabyDragonRenderer::new);
        event.registerEntityRenderer(ModEntities.DRAGON_EMBER.get(),DragonEmberRenderer::new);
    }
    @SubscribeEvent public static void screens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(com.codenamecoraline.registry.ModDragonSystems.DRAGON_MENU.get(), DragonScreen::new);
    }
    private BabyDragonClient() {}
}
