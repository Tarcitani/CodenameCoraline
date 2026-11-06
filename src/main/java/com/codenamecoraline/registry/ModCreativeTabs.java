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
                        output.accept(ModItems.LITHOPHAGE_SPAWN_EGG.get());
                        output.accept(ModItems.LAST_GIFT_COLLECTOR_SPAWN_EGG.get());
                        output.accept(ModItems.CANOPY_BREAKER_SPAWN_EGG.get());
                        output.accept(ModItems.BARK_MACAQUE_SPAWN_EGG.get());
                        output.accept(ModItems.BABY_DRAGON_SPAWN_EGG.get());
                        output.accept(ModItems.CRIMSON_COMMANDER_SPAWN_EGG.get());
                        output.accept(ModItems.CRIMSON_ASH_VANGUARD_SPAWN_EGG.get());
                        output.accept(ModItems.TEN_THREADS_WITCH_SPAWN_EGG.get());
                        output.accept(ModItems.FEY_RAVEN_SPAWN_EGG.get());
                        output.accept(ModItems.BRIMSTONE_SALAMANDER_SPAWN_EGG.get());
                    })
                    .build());

    private ModCreativeTabs() {}

    /** Weapons and the cap go to Combat, the mob parts to Ingredients, and spawn eggs also to vanilla Spawn Eggs. */
    private static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.RENEGADE_GREATAXE.get());
            event.accept(ModItems.DEADROOT_CLUB.get());
            event.accept(ModItems.CRIMSON_OATH_GREATSWORD.get());
            event.accept(ModItems.TEN_THREADS_STAFF.get());
            event.accept(ModItems.REDCAP_DAGGER.get());
            event.accept(ModItems.REDCAP_PIKESTAFF.get());
            event.accept(ModItems.REDCAP_CAP.get());
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.BESTIARY.get());
            event.accept(ModItems.SILENT_BELL.get());
            event.accept(ModItems.HOLLOW_TRUNK_IDOL.get());
            event.accept(ModItems.STAINED_OATH_CREST.get());
            event.accept(ModItems.CURSED_SPINDLE.get());
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.REDCAP_NAIL.get());
            event.accept(ModItems.REDCAP_TOOTH.get());
            event.accept(ModItems.BASILISK_LEG.get());
            event.accept(ModItems.BASILISK_EYE.get());
            event.accept(ModItems.BASILISK_TAIL.get());
            event.accept(ModItems.ANCIENT_CLAY_FRAGMENT.get());
            event.accept(ModItems.MINERAL_AMBER_CORE.get());
            event.accept(ModItems.CRACKED_BELL.get());
            event.accept(ModItems.CANOPY_RESIN.get());
            event.accept(ModItems.HEARTWOOD_SPLINTER.get());
            event.accept(ModItems.FROSTBOUND_THREAD.get());
            event.accept(ModItems.SOLSTICE_SEAL.get());
            event.accept(ModItems.CRIMSON_PLATE_SHARD.get());
            event.accept(ModItems.PUPPET_REMNANT.get());
            event.accept(ModItems.MATRIARCH_THIMBLE.get());
            event.accept(ModItems.FEY_FEATHER.get());
            event.accept(ModItems.BRIMSTONE_SCALE.get());
        } else if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.LITHOPHAGE_SPAWN_EGG.get());
            event.accept(ModItems.LAST_GIFT_COLLECTOR_SPAWN_EGG.get());
            event.accept(ModItems.CANOPY_BREAKER_SPAWN_EGG.get());
            event.accept(ModItems.BARK_MACAQUE_SPAWN_EGG.get());
            event.accept(ModItems.BABY_DRAGON_SPAWN_EGG.get());
            event.accept(ModItems.CRIMSON_COMMANDER_SPAWN_EGG.get());
            event.accept(ModItems.CRIMSON_ASH_VANGUARD_SPAWN_EGG.get());
            event.accept(ModItems.TEN_THREADS_WITCH_SPAWN_EGG.get());
            event.accept(ModItems.FEY_RAVEN_SPAWN_EGG.get());
            event.accept(ModItems.BRIMSTONE_SALAMANDER_SPAWN_EGG.get());
        }
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
        modEventBus.addListener(ModCreativeTabs::addToVanillaTabs);
    }
}
