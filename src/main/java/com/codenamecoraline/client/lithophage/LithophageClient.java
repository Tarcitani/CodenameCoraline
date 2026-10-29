package com.codenamecoraline.client.lithophage;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only registration for the Lithophage model, renderer and AmberShard projectile renderer. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID, value = Dist.CLIENT)
public final class LithophageClient {
    private LithophageClient() {}

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LithophageModel.LAYER, LithophageModel::createBodyLayer);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.LITHOPHAGE.get(), LithophageRenderer::new);
        event.registerEntityRenderer(ModEntities.AMBER_SHARD.get(), context -> new ThrownItemRenderer<>(context, 0.75F, true));
    }
}

