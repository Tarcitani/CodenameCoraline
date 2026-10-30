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
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) { event.registerLayerDefinition(BabyDragonModel.LAYER,BabyDragonModel::createBodyLayer); }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BABY_DRAGON.get(),BabyDragonRenderer::new);
        event.registerEntityRenderer(ModEntities.DRAGON_EMBER.get(),context->new ThrownItemRenderer<>(context,.35F,true));
    }
    private BabyDragonClient() {}
}
