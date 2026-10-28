package com.codenamecoraline.registry;

import com.codenamecoraline.CodenameCoraline;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Creative tabs of this mod, and the vanilla tabs its items are added to. */
public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CodenameCoraline.MOD_ID);

    /** Only the spawn eggs of this mod's mobs. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SPAWN_EGGS = TABS.register("spawn_eggs",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + CodenameCoraline.MOD_ID + ".spawn_eggs"))
                    .icon(() -> new ItemStack(ModItems.REDCAP_SPAWN_EGG.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.REDCAP_SPAWN_EGG.get());
                        output.accept(ModItems.BASILISK_SPAWN_EGG.get());
                    })
                    .build());

    private ModCreativeTabs() {}

    /** Weapons and the cap go to Combat, the mob parts to Ingredients. */
    private static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.REDCAP_DAGGER.get());
            event.accept(ModItems.REDCAP_PIKESTAFF.get());
            event.accept(ModItems.REDCAP_CAP.get());
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.BESTIARY.get());
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.REDCAP_NAIL.get());
            event.accept(ModItems.REDCAP_TOOTH.get());
            event.accept(ModItems.BASILISK_LEG.get());
            event.accept(ModItems.BASILISK_EYE.get());
            event.accept(ModItems.BASILISK_TAIL.get());
        }
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
        modEventBus.addListener(ModCreativeTabs::addToVanillaTabs);
    }
}
