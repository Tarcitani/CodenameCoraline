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

    public static final DeferredHolder<SoundEvent, SoundEvent> CRIMSON_AMBIENT = register("entity.crimson_commander.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRIMSON_STEP = register("entity.crimson_commander.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRIMSON_HURT = register("entity.crimson_commander.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRIMSON_DEATH = register("entity.crimson_commander.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRIMSON_SLASH = register("entity.crimson_commander.crimson_slash");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRIMSON_DASH = register("entity.crimson_commander.dash_thrust");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRIMSON_SLAM = register("entity.crimson_commander.heavy_overhead_slam");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRIMSON_GUARD = register("entity.crimson_commander.defensive_guard");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRIMSON_WAVE = register("entity.crimson_commander.blood_crescent_wave");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRIMSON_AWAKEN = register("entity.crimson_commander.awaken");
    public static final DeferredHolder<SoundEvent, SoundEvent> VANGUARD_AMBIENT = register("entity.crimson_ash_vanguard.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> VANGUARD_HURT = register("entity.crimson_ash_vanguard.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> VANGUARD_DEATH = register("entity.crimson_ash_vanguard.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> VANGUARD_STEP = register("entity.crimson_ash_vanguard.step");

    public static final DeferredHolder<SoundEvent, SoundEvent> WITCH_AMBIENT = register("entity.ten_threads_witch.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> WITCH_HURT = register("entity.ten_threads_witch.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> WITCH_DEATH = register("entity.ten_threads_witch.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> WITCH_THREAD_TENSION = register("entity.ten_threads_witch.thread_tension");
    public static final DeferredHolder<SoundEvent, SoundEvent> WITCH_THREAD_SNAP = register("entity.ten_threads_witch.thread_snap");
    /** The Matriarch's voice lines (the text is shown on the action bar; see WitchDialogueManager). */
    public static final DeferredHolder<SoundEvent, SoundEvent> WITCH_VOICE = register("entity.ten_threads_witch.voice");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUPPET_HURT = register("entity.ten_threads_puppet.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUPPET_DEATH = register("entity.ten_threads_puppet.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUPPET_AWAKEN = register("entity.ten_threads_puppet.awaken");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUPPET_TANGLED = register("entity.ten_threads_puppet.tangled");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUPPET_CRASH = register("entity.ten_threads_puppet.crash");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUPPET_CRACK = register("entity.ten_threads_puppet.crack");

    public static final DeferredHolder<SoundEvent, SoundEvent> FEY_RAVEN_AMBIENT = register("entity.fey_raven.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> FEY_RAVEN_ALERT = register("entity.fey_raven.alert");
    public static final DeferredHolder<SoundEvent, SoundEvent> FEY_RAVEN_FLAP = register("entity.fey_raven.flap");
    public static final DeferredHolder<SoundEvent, SoundEvent> FEY_RAVEN_HURT = register("entity.fey_raven.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> FEY_RAVEN_DEATH = register("entity.fey_raven.death");

    public static final DeferredHolder<SoundEvent, SoundEvent> BRIMSTONE_SALAMANDER_AMBIENT = register("entity.brimstone_salamander.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> BRIMSTONE_SALAMANDER_HURT = register("entity.brimstone_salamander.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BRIMSTONE_SALAMANDER_DEATH = register("entity.brimstone_salamander.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BRIMSTONE_SALAMANDER_STEP = register("entity.brimstone_salamander.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> BRIMSTONE_SALAMANDER_BITE = register("entity.brimstone_salamander.bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> BRIMSTONE_SALAMANDER_SPIT = register("entity.brimstone_salamander.spit");
    public static final DeferredHolder<SoundEvent, SoundEvent> BRIMSTONE_SALAMANDER_TAIL_WHIP = register("entity.brimstone_salamander.tail_whip");

    /** The signature sound of each puppet (steps and attacks), one event per kind. */
    private static final java.util.EnumMap<com.codenamecoraline.entity.ten_threads_witch.PuppetKind, DeferredHolder<SoundEvent, SoundEvent>> PUPPET_ACTIONS =
            new java.util.EnumMap<>(com.codenamecoraline.entity.ten_threads_witch.PuppetKind.class);

    static {
        for (var kind : com.codenamecoraline.entity.ten_threads_witch.PuppetKind.values()) {
            PUPPET_ACTIONS.put(kind, register("entity." + kind.id + ".action"));
        }
    }

    public static SoundEvent puppetAction(com.codenamecoraline.entity.ten_threads_witch.PuppetKind kind) {
        return PUPPET_ACTIONS.get(kind).get();
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, SoundEvent::createVariableRangeEvent);
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
