package com.codenamecoraline.registry;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.basilisk.WitherResistanceEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    private static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, CodenameCoraline.MOD_ID);
    public static final DeferredHolder<MobEffect, WitherResistanceEffect> WITHER_RESISTANCE =
            EFFECTS.register("wither_resistance", WitherResistanceEffect::new);

    private ModEffects() {}

    public static void register(IEventBus bus) { EFFECTS.register(bus); }
}
