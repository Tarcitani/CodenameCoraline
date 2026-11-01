package com.codenamecoraline.registry;

import com.codenamecoraline.CodenameCoraline;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Sound events of this mod. The audio files each event plays are listed in assets/codenamecoraline/sounds.json. */
public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, CodenameCoraline.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> REDCAP_AMBIENT = register("entity.redcap.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> REDCAP_LAUGH = register("entity.redcap.laugh");
    public static final DeferredHolder<SoundEvent, SoundEvent> REDCAP_HURT = register("entity.redcap.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> REDCAP_DEATH = register("entity.redcap.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> REDCAP_ATTACK = register("entity.redcap.attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> REDCAP_STEP = register("entity.redcap.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> REDCAP_SNIFF = register("entity.redcap.sniff");
    public static final DeferredHolder<SoundEvent, SoundEvent> REDCAP_SHARPEN = register("entity.redcap.sharpen");
    public static final DeferredHolder<SoundEvent, SoundEvent> REDCAP_THROW = register("entity.redcap.throw");
    public static final DeferredHolder<SoundEvent, SoundEvent> REDCAP_ROCK_HIT = register("entity.redcap.rock_hit");

    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_AMBIENT = register("entity.basilisk.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_HURT = register("entity.basilisk.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_DEATH = register("entity.basilisk.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_HOP = register("entity.basilisk.hop");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_INHALE = register("entity.basilisk.inhale");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_EXHALE = register("entity.basilisk.exhale");

    public static final DeferredHolder<SoundEvent, SoundEvent> LITHOPHAGE_AMBIENT = register("entity.lithophage.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> LITHOPHAGE_AWAKEN = register("entity.lithophage.awaken");
    public static final DeferredHolder<SoundEvent, SoundEvent> LITHOPHAGE_STEP = register("entity.lithophage.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> LITHOPHAGE_SLAM = register("entity.lithophage.slam");
    public static final DeferredHolder<SoundEvent, SoundEvent> LITHOPHAGE_SWEEP = register("entity.lithophage.sweep");
    public static final DeferredHolder<SoundEvent, SoundEvent> LITHOPHAGE_SHARD_SHOOT = register("entity.lithophage.shard_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> LITHOPHAGE_SHARD_HIT = register("entity.lithophage.shard_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> LITHOPHAGE_HURT = register("entity.lithophage.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> LITHOPHAGE_DEATH = register("entity.lithophage.death");

    public static final DeferredHolder<SoundEvent, SoundEvent> COLLECTOR_AMBIENT = register("entity.last_gift_collector.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLLECTOR_HURT = register("entity.last_gift_collector.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLLECTOR_DEATH = register("entity.last_gift_collector.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLLECTOR_STEP = register("entity.last_gift_collector.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLLECTOR_LIGHTNING = register("entity.last_gift_collector.lightning");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLLECTOR_SLEIGH_BELLS = register("entity.last_gift_collector.sleigh_bells");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLLECTOR_BELL_PREP = register("entity.last_gift_collector.bell_prep");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLLECTOR_SCYTHE_SLASH = register("entity.last_gift_collector.scythe_slash");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLLECTOR_FROST_SHATTER = register("entity.last_gift_collector.frost_shatter");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLLECTOR_PHASE_TRANSITION = register("entity.last_gift_collector.phase_transition");

    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_AMBIENT = register("entity.canopy_breaker.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_HURT = register("entity.canopy_breaker.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_DEATH = register("entity.canopy_breaker.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_STEP = register("entity.canopy_breaker.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_ROAR_ALERT = register("entity.canopy_breaker.roar_alert");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_ROAR_ATTACK = register("entity.canopy_breaker.roar_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_ROAR_FURY = register("entity.canopy_breaker.roar_fury");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_CLUB_DRAW = register("entity.canopy_breaker.club_draw");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_CLUB_SWING = register("entity.canopy_breaker.club_swing");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_CLUB_IMPACT = register("entity.canopy_breaker.club_impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_CLIMB = register("entity.canopy_breaker.climb");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_JUMP = register("entity.canopy_breaker.jump");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_LAND_LIGHT = register("entity.canopy_breaker.land_light");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_LAND_HEAVY = register("entity.canopy_breaker.land_heavy");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_PANT = register("entity.canopy_breaker.pant");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_CHEST_BEAT = register("entity.canopy_breaker.chest_beat");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_GRAB = register("entity.canopy_breaker.grab");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_THROW = register("entity.canopy_breaker.throw");

    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_AMBIENT = register("entity.baby_dragon.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_HAPPY = register("entity.baby_dragon.happy");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_HURT = register("entity.baby_dragon.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_DEATH = register("entity.baby_dragon.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_FLAP = register("entity.baby_dragon.flap");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_LAND = register("entity.baby_dragon.land");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_CHARGE = register("entity.baby_dragon.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_SHOOT = register("entity.baby_dragon.shoot");

    private ModSounds() {}

    public static final DeferredHolder<SoundEvent, SoundEvent> MACAQUE_AMBIENT = register("entity.bark_macaque.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MACAQUE_HURT = register("entity.bark_macaque.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MACAQUE_DEATH = register("entity.bark_macaque.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MACAQUE_SCREECH = register("entity.bark_macaque.screech");
    public static final DeferredHolder<SoundEvent, SoundEvent> MACAQUE_BITE = register("entity.bark_macaque.bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> MACAQUE_LEAP = register("entity.bark_macaque.leap");
    public static final DeferredHolder<SoundEvent, SoundEvent> MACAQUE_STEP = register("entity.bark_macaque.step");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, SoundEvent::createVariableRangeEvent);
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
