package com.codenamecoraline.registry;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.redcap.Redcap;
import com.codenamecoraline.entity.redcap.RedcapRock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Entity types registered by this mod. */
public final class ModEntities {
    private static final DeferredRegister.Entities ENTITY_TYPES =
            DeferredRegister.createEntities(CodenameCoraline.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<Redcap>> REDCAP = ENTITY_TYPES.register("redcap",
            id -> EntityType.Builder.of(Redcap::new, MobCategory.MONSTER)
                    .sized(0.7F, 1.35F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<RedcapRock>> REDCAP_ROCK = ENTITY_TYPES.register("redcap_rock",
            id -> EntityType.Builder.<RedcapRock>of(RedcapRock::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    private ModEntities() {}

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(ModEntities::onEntityAttributeCreation);
    }

    private static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(REDCAP.get(), Redcap.createAttributes().build());
    }
}
