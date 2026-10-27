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

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, SoundEvent::createVariableRangeEvent);
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
