package com.codenamecoraline.registry;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.redcap.Redcap;
import com.codenamecoraline.entity.redcap.RedcapRock;
import com.codenamecoraline.entity.basilisk.Basilisk;
import com.codenamecoraline.entity.basilisk.WitherMistCloud;
import com.codenamecoraline.entity.basilisk.MistFlask;
import com.codenamecoraline.entity.lithophage.AmberShard;
import com.codenamecoraline.entity.lithophage.Lithophage;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
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

    public static final DeferredHolder<EntityType<?>, EntityType<Basilisk>> BASILISK = ENTITY_TYPES.register("basilisk",
            id -> EntityType.Builder.of(Basilisk::new, MobCategory.MONSTER)
                    .sized(1.3F, 0.85F).eyeHeight(0.35F).clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    // Legacy entity ID preserves clouds already saved in worlds.
    public static final DeferredHolder<EntityType<?>, EntityType<WitherMistCloud>> WITHER_MIST_CLOUD = ENTITY_TYPES.register("death_blight_cloud",
            id -> EntityType.Builder.<WitherMistCloud>of(WitherMistCloud::new, MobCategory.MISC)
                    .sized(7, 0.5F).clientTrackingRange(8).updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<MistFlask>> MIST_FLASK = ENTITY_TYPES.register("mist_flask",
            id -> EntityType.Builder.<MistFlask>of(MistFlask::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<Lithophage>> LITHOPHAGE = ENTITY_TYPES.register("lithophage",
            id -> EntityType.Builder.of(Lithophage::new, MobCategory.MONSTER)
                    .sized(0.85F, 2.1F).eyeHeight(1.8F).clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<AmberShard>> AMBER_SHARD = ENTITY_TYPES.register("amber_shard",
            id -> EntityType.Builder.<AmberShard>of(AmberShard::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F).clientTrackingRange(4).updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    private ModEntities() {}

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(ModEntities::onEntityAttributeCreation);
        modEventBus.addListener(ModEntities::onSpawnPlacements);
    }

    private static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(REDCAP.get(), Redcap.createAttributes().build());
        event.put(BASILISK.get(), Basilisk.createAttributes().build());
        event.put(LITHOPHAGE.get(), Lithophage.createAttributes().build());
    }

    private static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(BASILISK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Basilisk::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(LITHOPHAGE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Lithophage::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
