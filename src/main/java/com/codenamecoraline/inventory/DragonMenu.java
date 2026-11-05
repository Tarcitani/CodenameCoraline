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
    public static final int MENU_SLOT_SADDLE = 54, MENU_SLOT_TOTEM = 55;
    private final @Nullable BabyDragon dragon;
    private final Container inventory;
    private final Container equipment;
    private final ContainerData data;
    public DragonMenu(int id, Inventory player, BabyDragon dragon) {
        this(id, player, dragon, dragon.getDragonInventory(), dragon.getDragonEquipment(), new ContainerData() {
            @Override public int get(int key) {
                return switch (key) {
                    case 0 -> dragon.getDragonStage().ordinal(); case 1 -> dragon.getDragonXp();
                    case 2 -> (int)Math.ceil(dragon.getHealth()); case 3 -> (int)dragon.getMaxHealth();
                    case 4 -> dragon.getArmorValue(); case 5 -> (int)dragon.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                    case 6 -> (int)(dragon.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED) * 1000);
                    case 7 -> dragon.getEvolutionTicks();
                    case 8 -> dragon.isSaddled() ? 1 : 0;
                    default -> dragon.hasEquippedTotem() ? 1 : 0;
                };
            }
            @Override public void set(int key, int value) {}
            @Override public int getCount() { return 10; }
        });
    }
    public static DragonMenu client(int id, Inventory player, int entityId) {
        BabyDragon dragon = player.player.level().getEntity(entityId) instanceof BabyDragon d ? d : null;
        return new DragonMenu(id, player, dragon, new SimpleContainer(18), new SimpleContainer(2), new SimpleContainerData(10));
    }
    private DragonMenu(int id, Inventory player, @Nullable BabyDragon dragon, Container inventory, Container equipment, ContainerData data) {
        super(ModDragonSystems.DRAGON_MENU.get(), id);
        this.dragon = dragon; this.inventory = inventory; this.equipment = equipment; this.data = data;
        for (int slot = 0; slot < 18; slot++) {
            final int index = slot;
            this.addSlot(new Slot(inventory, slot, 8 + slot % 9 * 18, 84 + slot / 9 * 18) {
                @Override public boolean isActive() { return index < DragonMenu.this.getActiveCargoSlots(); }
                @Override public boolean mayPlace(ItemStack stack) { return this.isActive(); }
                @Override public boolean mayPickup(Player p) { return this.isActive() && DragonMenu.this.stillValid(p); }
            });
        }
        this.addStandardInventorySlots(player, 8, 142);
        this.addSlot(new Slot(equipment, BabyDragon.EQUIP_SLOT_SADDLE, 134, 18) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean mayPlace(ItemStack stack) { return BabyDragon.isValidSaddleItem(stack); }
            @Override public boolean mayPickup(Player p) {
                return DragonMenu.this.stillValid(p) && DragonMenu.this.canUnequipSaddle();
            }
        });
        this.addSlot(new Slot(equipment, BabyDragon.EQUIP_SLOT_TOTEM, 152, 18) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean isActive() { return DragonMenu.this.isSaddled(); }
            @Override public boolean mayPlace(ItemStack stack) { return this.isActive() && BabyDragon.isValidTotemItem(stack); }
            @Override public boolean mayPickup(Player p) { return this.isActive() && DragonMenu.this.stillValid(p); }
        });
        this.addDataSlots(data);
    }
    public DragonStage getStage() { return DragonStage.byId(this.data.get(0)); }
    public boolean isSaddled() {
        return this.data.get(8) != 0 || BabyDragon.isValidSaddleItem(this.equipment.getItem(BabyDragon.EQUIP_SLOT_SADDLE));
    }
    public boolean hasEquippedTotem() {
        return this.data.get(9) != 0 || (this.isSaddled() && BabyDragon.isValidTotemItem(this.equipment.getItem(BabyDragon.EQUIP_SLOT_TOTEM)));
    }
    public int getActiveCargoSlots() {
        return Math.min(18, this.getStage().slots + (this.isSaddled() ? 9 : 0));
    }
    public boolean canUnequipSaddle() {
        if (this.dragon != null && !this.dragon.level().isClientSide()) return this.dragon.canUnequipSaddle();
        if (!this.equipment.getItem(BabyDragon.EQUIP_SLOT_TOTEM).isEmpty()) return false;
        for (int i = this.getStage().slots; i < 18; i++) {
            if (!this.inventory.getItem(i).isEmpty()) return false;
        }
        return true;
    }
    public int value(int index) { return this.data.get(index); }
    public String ownerName() { return this.dragon != null && this.dragon.getOwner() != null ? this.dragon.getOwner().getName().getString() : "?"; }
    @Override public boolean stillValid(Player player) {
        return this.dragon != null && this.dragon.isAlive() && this.dragon.isOwnedBy(player) && this.dragon.distanceToSqr(player) <= 64;
    }
    @Override public boolean clickMenuButton(Player player, int id) {
        if (!this.stillValid(player) || this.dragon == null || id < 0 || id >= BabyDragon.Command.values().length) return false;
        return this.dragon.issueCommand(BabyDragon.Command.values()[id], player);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (!this.stillValid(player) || index < 0 || index >= this.slots.size()) return ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.isActive() || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index < 18 || index >= MENU_SLOT_SADDLE) {
            if (index == MENU_SLOT_SADDLE && !this.canUnequipSaddle()) return ItemStack.EMPTY;
            if (!this.moveItemStackTo(stack, 18, MENU_SLOT_SADDLE, true)) return ItemStack.EMPTY;
        } else {
            boolean moved = false;
            if (BabyDragon.isValidSaddleItem(stack) && !this.slots.get(MENU_SLOT_SADDLE).hasItem()) {
                moved = this.moveItemStackTo(stack, MENU_SLOT_SADDLE, MENU_SLOT_SADDLE + 1, false);
            } else if (this.isSaddled() && BabyDragon.isValidTotemItem(stack) && !this.slots.get(MENU_SLOT_TOTEM).hasItem()) {
                moved = this.moveItemStackTo(stack, MENU_SLOT_TOTEM, MENU_SLOT_TOTEM + 1, false);
            }
            if (!moved) {
                int activeCargo = this.getActiveCargoSlots();
                if (activeCargo == 0 || !this.moveItemStackTo(stack, 0, activeCargo, false)) return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }
}
