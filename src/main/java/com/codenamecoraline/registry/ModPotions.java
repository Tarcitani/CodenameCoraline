package com.codenamecoraline.registry;

import com.codenamecoraline.CodenameCoraline;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Native potion types and sequential recipes for the vanilla brewing stand. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class ModPotions {
    private static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, CodenameCoraline.MOD_ID);
    public static final int RESISTANCE_DURATION = 3600;
    public static final int LONG_RESISTANCE_DURATION = 9600;

    public static final DeferredHolder<Potion, Potion> BASILISK_LEG_INFUSION = POTIONS.register("basilisk_leg_infusion",
            () -> new Potion("codenamecoraline.basilisk_leg_infusion"));
    public static final DeferredHolder<Potion, Potion> BASILISK_EYE_INFUSION = POTIONS.register("basilisk_eye_infusion",
            () -> new Potion("codenamecoraline.basilisk_eye_infusion"));
    public static final DeferredHolder<Potion, Potion> WITHER_RESISTANCE = POTIONS.register("wither_resistance",
            () -> new Potion("codenamecoraline.wither_resistance",
                    new MobEffectInstance(ModEffects.WITHER_RESISTANCE, RESISTANCE_DURATION)));
    public static final DeferredHolder<Potion, Potion> LONG_WITHER_RESISTANCE = POTIONS.register("long_wither_resistance",
            () -> new Potion("codenamecoraline.wither_resistance",
                    new MobEffectInstance(ModEffects.WITHER_RESISTANCE, LONG_RESISTANCE_DURATION)));

    private ModPotions() {}

    public static ItemStack resistancePotion() {
        return PotionContents.createItemStack(Items.POTION, WITHER_RESISTANCE);
    }

    @SubscribeEvent
    public static void brewing(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();
        builder.addMix(Potions.AWKWARD, ModItems.BASILISK_LEG.get(), BASILISK_LEG_INFUSION);
        builder.addMix(BASILISK_LEG_INFUSION, ModItems.BASILISK_EYE.get(), BASILISK_EYE_INFUSION);
        builder.addMix(BASILISK_EYE_INFUSION, ModItems.BASILISK_TAIL.get(), WITHER_RESISTANCE);
        builder.addMix(WITHER_RESISTANCE, Items.REDSTONE, LONG_WITHER_RESISTANCE);
    }

    public static void register(IEventBus bus) { POTIONS.register(bus); }
}
