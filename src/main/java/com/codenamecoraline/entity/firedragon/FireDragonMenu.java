package com.codenamecoraline.entity.firedragon;

import com.codenamecoraline.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/** Adult Fire Dragon inventory: 1 saddle slot (0) + 10 cargo slots (1..10) + Follow/Rest command toggle. */
public final class FireDragonMenu extends AbstractContainerMenu {
    public static final int SADDLE_SLOT = 0;
    public static final int CARGO_SLOTS = 10;
    public static final int DRAGON_SLOTS = 1 + CARGO_SLOTS;
    public static final int BUTTON_TOGGLE_ORDER = 0;

    private final @Nullable FireDragon dragon;
    private final Container container;
    private final ContainerData data;

    public static FireDragonMenu fromNetwork(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        Entity entity = playerInv.player.level().getEntity(entityId);
        FireDragon found = entity instanceof FireDragon fd ? fd : null;
        return new FireDragonMenu(containerId, playerInv, found,
                found != null ? found.getDragonInventory() : new SimpleContainer(DRAGON_SLOTS),
                new SimpleContainerData(4));
    }

    public FireDragonMenu(int containerId, Inventory playerInv, @Nullable FireDragon dragon,
                          Container container, ContainerData data) {
        super(ModMenus.FIRE_DRAGON.get(), containerId);
        this.dragon = dragon;
        this.container = container;
        this.data = data;
        checkContainerSize(container, DRAGON_SLOTS);
        checkContainerDataCount(data, 4);
        this.addDataSlots(data);

        // Slot 0: Saddle slot on the left panel
        this.addSlot(new Slot(container, SADDLE_SLOT, 16, 28) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.SADDLE);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Slots 1..10: 5x2 cargo chest grid on the right panel
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 5; col++) {
                this.addSlot(new Slot(container, 1 + row * 5 + col, 68 + col * 18, 20 + row * 18));
            }
        }

        // Player inventory (3 rows x 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 86 + row * 18));
            }
        }
        // Player hotbar (9 slots)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 144));
        }
    }

    public boolean isResting() { return data.get(0) != 0; }
    public float getDragonHealth() { return data.get(1) / 10.0F; }
    public float getDragonMaxHealth() { return Math.max(1.0F, data.get(2) / 10.0F); }
    public int getDragonSatiety() { return data.get(3); }
    public boolean hasSaddle() { return !container.getItem(SADDLE_SLOT).isEmpty(); }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_TOGGLE_ORDER && dragon != null && dragon.isAdult() && dragon.isOwnedBy(player)) {
            dragon.toggleSitOrder();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < DRAGON_SLOTS) {
            if (!this.moveItemStackTo(stack, DRAGON_SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (stack.is(Items.SADDLE) && !this.slots.get(SADDLE_SLOT).hasItem()) {
                if (!this.moveItemStackTo(stack, SADDLE_SLOT, SADDLE_SLOT + 1, false)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(stack, 1, DRAGON_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return dragon != null && dragon.isAlive() && dragon.isAdult()
                && dragon.isOwnedBy(player) && dragon.distanceToSqr(player) <= 64.0D;
    }
}
