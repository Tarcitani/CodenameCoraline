package com.codenamecoraline.registry;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.firedragon.FireDragonMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Container menus registered by this mod. */
public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, CodenameCoraline.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<FireDragonMenu>> FIRE_DRAGON =
            MENUS.register("fire_dragon", () -> IMenuTypeExtension.create(FireDragonMenu::fromNetwork));

    private ModMenus() {}

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
