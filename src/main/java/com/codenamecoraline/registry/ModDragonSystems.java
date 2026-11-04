package com.codenamecoraline.registry;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.inventory.DragonMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Only the inventory and a non-obtainable temporary terrain block are new registries. */
public final class ModDragonSystems {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, CodenameCoraline.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, CodenameCoraline.MOD_ID);
    public static final DeferredHolder<Block, Block> TEMPORARY_MAGMA = BLOCKS.register("dragon_temporary_magma", id -> new Block(
            BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id)).strength(.5F)
                    .lightLevel(state -> 5).noLootTable().pushReaction(PushReaction.BLOCK)));
    public static final DeferredHolder<MenuType<?>, MenuType<DragonMenu>> DRAGON_MENU = MENUS.register("dragon", () ->
            IMenuTypeExtension.create((id, inv, buffer) -> DragonMenu.client(id, inv, buffer.readInt())));
    public static void register(IEventBus bus) { BLOCKS.register(bus); MENUS.register(bus); }
    private ModDragonSystems() {}
}
