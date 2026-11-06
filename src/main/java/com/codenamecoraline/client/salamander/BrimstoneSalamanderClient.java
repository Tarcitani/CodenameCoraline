package com.codenamecoraline.client.salamander;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only registration for the Brimstone Salamander model, renderer, and BrimstoneSpit projectile renderer. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID, value = Dist.CLIENT)
public final class BrimstoneSalamanderClient {
    private BrimstoneSalamanderClient() {}

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BrimstoneSalamanderModel.LAYER, BrimstoneSalamanderModel::createBodyLayer);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BRIMSTONE_SALAMANDER.get(), BrimstoneSalamanderRenderer::new);
        event.registerEntityRenderer(ModEntities.BRIMSTONE_SPIT.get(), context -> new ThrownItemRenderer<>(context, 0.85F, true));
    }
}

