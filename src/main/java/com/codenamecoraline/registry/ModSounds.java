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

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, SoundEvent::createVariableRangeEvent);
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
