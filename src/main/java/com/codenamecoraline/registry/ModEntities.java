package com.codenamecoraline.registry;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.redcap.Redcap;
import com.codenamecoraline.entity.redcap.RedcapRock;
import com.codenamecoraline.entity.basilisk.Basilisk;
import com.codenamecoraline.entity.basilisk.WitherMistCloud;
import com.codenamecoraline.entity.basilisk.MistFlask;
import com.codenamecoraline.entity.lithophage.AmberShard;
import com.codenamecoraline.entity.lithophage.Lithophage;
import com.codenamecoraline.entity.canopy.CanopyBreaker;
import com.codenamecoraline.entity.macaque.BarkMacaque;
import com.codenamecoraline.entity.spirit.BossSpirit;
import com.codenamecoraline.entity.collector.CollectorGift;
import com.codenamecoraline.entity.collector.CollectorSleigh;
import com.codenamecoraline.entity.collector.GiftMimic;
import com.codenamecoraline.entity.collector.LastGiftCollector;
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

    public static final DeferredHolder<EntityType<?>, EntityType<LastGiftCollector>> LAST_GIFT_COLLECTOR = ENTITY_TYPES.register("last_gift_collector",
            id -> EntityType.Builder.of(LastGiftCollector::new, MobCategory.MONSTER)
                    .sized(LastGiftCollector.COLLISION_WIDTH, LastGiftCollector.COLLISION_HEIGHT)
                    .eyeHeight(LastGiftCollector.EYE_HEIGHT)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<CollectorSleigh>> COLLECTOR_SLEIGH = ENTITY_TYPES.register("collector_sleigh",
            id -> EntityType.Builder.<CollectorSleigh>of(CollectorSleigh::new, MobCategory.MISC)
                    .sized(3.5F, 2.2F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .noSave()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<GiftMimic>> GIFT_MIMIC = ENTITY_TYPES.register("gift_mimic",
            id -> EntityType.Builder.of(GiftMimic::new, MobCategory.MONSTER)
                    .sized(0.9F, 1.1F).eyeHeight(0.8F).clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<CollectorGift>> COLLECTOR_GIFT = ENTITY_TYPES.register("collector_gift",
            id -> EntityType.Builder.<CollectorGift>of(CollectorGift::new, MobCategory.MISC)
                    .sized(1.4F, 1.3F)
                    .clientTrackingRange(10)
                    .noSave()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<CanopyBreaker>> CANOPY_BREAKER = ENTITY_TYPES.register("canopy_breaker",
            id -> EntityType.Builder.of(CanopyBreaker::new, MobCategory.MONSTER)
                    .sized(CanopyBreaker.WIDTH, CanopyBreaker.HEIGHT).eyeHeight(CanopyBreaker.EYE_HEIGHT)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<BarkMacaque>> BARK_MACAQUE = ENTITY_TYPES.register("bark_macaque",
            id -> EntityType.Builder.of(BarkMacaque::new, MobCategory.MONSTER)
                    .sized(BarkMacaque.WIDTH, BarkMacaque.HEIGHT).eyeHeight(0.6F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    /** The ghosts the bosses leave when they die (visual only, never saved). */
    public static final DeferredHolder<EntityType<?>, EntityType<BossSpirit>> CANOPY_BREAKER_SPIRIT = ENTITY_TYPES.register("canopy_breaker_spirit",
            id -> EntityType.Builder.<BossSpirit>of(BossSpirit::new, MobCategory.MISC)
                    .sized(CanopyBreaker.WIDTH, CanopyBreaker.HEIGHT).noSave().fireImmune()
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<BossSpirit>> LAST_GIFT_COLLECTOR_SPIRIT = ENTITY_TYPES.register("last_gift_collector_spirit",
            id -> EntityType.Builder.<BossSpirit>of(BossSpirit::new, MobCategory.MISC)
                    .sized(LastGiftCollector.COLLISION_WIDTH, LastGiftCollector.COLLISION_HEIGHT).noSave().fireImmune()
                    .clientTrackingRange(10)
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
        event.put(LAST_GIFT_COLLECTOR.get(), LastGiftCollector.createAttributes().build());
        event.put(GIFT_MIMIC.get(), GiftMimic.createAttributes().build());
        event.put(CANOPY_BREAKER.get(), CanopyBreaker.createAttributes().build());
        event.put(BARK_MACAQUE.get(), BarkMacaque.createAttributes().build());
        event.put(CANOPY_BREAKER_SPIRIT.get(), BossSpirit.createAttributes().build());
        event.put(LAST_GIFT_COLLECTOR_SPIRIT.get(), BossSpirit.createAttributes().build());
    }

    private static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(BASILISK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Basilisk::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(LITHOPHAGE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Lithophage::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
