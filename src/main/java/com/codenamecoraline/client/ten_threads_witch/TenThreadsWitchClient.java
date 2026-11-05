package com.codenamecoraline.client.ten_threads_witch;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.ten_threads_witch.PuppetKind;
import com.codenamecoraline.registry.ModEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-only registration of the eleven model layers (the Matriarch and her ten puppets) and their renderers. The thread
 * hitbox and the Vital Spool orb have no model: they are drawn as particles by the server, so they get a no-op renderer.
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID, value = Dist.CLIENT)
public final class TenThreadsWitchClient {
    private TenThreadsWitchClient() {}

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PuppetModel.layer(MatriarchRenderer.ID), () -> TenThreadsParts.layer(MatriarchRenderer.ID));
        for (PuppetKind kind : PuppetKind.values()) {
            event.registerLayerDefinition(PuppetModel.layer(kind.id), () -> TenThreadsParts.layer(kind.id));
        }
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TEN_THREADS_WITCH.get(), MatriarchRenderer::new);
        for (PuppetKind kind : PuppetKind.values()) {
            event.registerEntityRenderer(ModEntities.puppet(kind), context -> new PuppetRenderer(context, kind));
        }
        event.registerEntityRenderer(ModEntities.THREAD_HITBOX.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.SPOOL_VITAL_ORB.get(), NoopRenderer::new);
    }
}
