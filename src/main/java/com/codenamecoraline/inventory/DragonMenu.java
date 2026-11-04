package com.codenamecoraline.inventory;

import com.codenamecoraline.entity.babydragon.BabyDragon;
import com.codenamecoraline.entity.babydragon.DragonStage;
import com.codenamecoraline.registry.ModDragonSystems;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Native transaction/state-ID synchronization; the server owns every item operation. */
public final class DragonMenu extends AbstractContainerMenu {
    private final @Nullable BabyDragon dragon;
    private final Container inventory;
    private final ContainerData data;
    public DragonMenu(int id, Inventory player, BabyDragon dragon) {
        this(id, player, dragon, dragon.getDragonInventory(), new ContainerData() {
            @Override public int get(int key) {
                return switch (key) {
                    case 0 -> dragon.getDragonStage().ordinal(); case 1 -> dragon.getDragonXp();
                    case 2 -> (int)Math.ceil(dragon.getHealth()); case 3 -> (int)dragon.getMaxHealth();
                    case 4 -> dragon.getArmorValue(); case 5 -> (int)dragon.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                    case 6 -> (int)(dragon.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED) * 1000);
                    default -> dragon.getEvolutionTicks();
                };
            }
            @Override public void set(int key, int value) {}
            @Override public int getCount() { return 8; }
        });
    }
    public static DragonMenu client(int id, Inventory player, int entityId) {
        BabyDragon dragon = player.player.level().getEntity(entityId) instanceof BabyDragon d ? d : null;
        return new DragonMenu(id, player, dragon, new SimpleContainer(18), new SimpleContainerData(8));
    }
    private DragonMenu(int id, Inventory player, @Nullable BabyDragon dragon, Container inventory, ContainerData data) {
        super(ModDragonSystems.DRAGON_MENU.get(), id); this.dragon = dragon; this.inventory = inventory; this.data = data;
        for (int slot = 0; slot < 18; slot++) {
            final int index = slot;
            this.addSlot(new Slot(inventory, slot, 8 + slot % 9 * 18, 84 + slot / 9 * 18) {
                @Override public boolean isActive() { return index < getStage().slots; }
                @Override public boolean mayPlace(ItemStack stack) { return this.isActive(); }
                @Override public boolean mayPickup(Player p) { return this.isActive() && DragonMenu.this.stillValid(p); }
            });
        }
        this.addStandardInventorySlots(player, 8, 142); this.addDataSlots(data);
    }
    public DragonStage getStage() { return DragonStage.byId(this.data.get(0)); }
    public int value(int index) { return this.data.get(index); }
    public String ownerName() { return this.dragon != null && this.dragon.getOwner() != null ? this.dragon.getOwner().getName().getString() : "?"; }
    @Override public boolean stillValid(Player player) {
        return this.dragon != null && this.dragon.isAlive() && this.dragon.isOwnedBy(player) && this.dragon.distanceToSqr(player) <= 64;
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (!this.stillValid(player) || index < 0 || index >= this.slots.size()) return ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.isActive() || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index < 18) {
            if (!this.moveItemStackTo(stack, 18, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (!this.moveItemStackTo(stack, 0, this.getStage().slots, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }
}
