package com.codenamecoraline.client.firedragon;

import com.codenamecoraline.entity.firedragon.FireDragonMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Adult Fire Dragon inventory screen: saddle slot, 10 cargo slots (5x2), health/satiety bars and Follow/Rest toggle. */
public final class FireDragonScreen extends AbstractContainerScreen<FireDragonMenu> {
    private Button orderButton;

    public FireDragonScreen(FireDragonMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, 176, 168);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 74;
    }

    @Override
    protected void init() {
        super.init();
        this.orderButton = this.addRenderableWidget(Button.builder(orderLabel(), b -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, FireDragonMenu.BUTTON_TOGGLE_ORDER);
            }
        }).pos(this.leftPos + 8, this.topPos + 52).size(54, 18).build());
    }

    private Component orderLabel() {
        return Component.translatable(this.menu.isResting()
                ? "gui.codenamecoraline.fire_dragon.order.rest"
                : "gui.codenamecoraline.fire_dragon.order.follow");
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.orderButton != null) this.orderButton.setMessage(orderLabel());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        int x = this.leftPos;
        int y = this.topPos;
        // Main obsidian-and-ember framed container panel
        gui.fill(x - 1, y - 1, x + this.imageWidth + 1, y + this.imageHeight + 1, 0xFF2B1610);
        gui.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF3E241C);
        gui.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, 0xFF261714);

        // Golden saddle slot frame (slot 0 at +16, +28)
        drawSlotFrame(gui, x + 15, y + 27, 0xFFD99A2B, 0xFF1A0F0D);
        // 5x2 cargo chest slot frames (slots 1..10 at +68, +20)
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 5; col++) {
                drawSlotFrame(gui, x + 67 + col * 18, y + 19 + row * 18, 0xFF7B4A32, 0xFF1B1210);
            }
        }
        // Health & satiety compact bars under cargo grid
        int barX = x + 68;
        int barW = 90;
        float hpRatio = Math.clamp(this.menu.getDragonHealth() / this.menu.getDragonMaxHealth(), 0.0F, 1.0F);
        gui.fill(barX, y + 59, barX + barW, y + 64, 0xFF140B09);
        gui.fill(barX + 1, y + 60, barX + 1 + Math.round((barW - 2) * hpRatio), y + 63, 0xFFD93B2B);

        float satRatio = Math.clamp(this.menu.getDragonSatiety() / 100.0F, 0.0F, 1.0F);
        gui.fill(barX, y + 66, barX + barW, y + 71, 0xFF140B09);
        gui.fill(barX + 1, y + 67, barX + 1 + Math.round((barW - 2) * satRatio), y + 70, 0xFFE69926);

        // Player inventory (3x9) and hotbar (1x9) slot frames
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotFrame(gui, x + 7 + col * 18, y + 85 + row * 18, 0xFF664234, 0xFF191210);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotFrame(gui, x + 7 + col * 18, y + 143, 0xFF664234, 0xFF191210);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
        gui.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFF5D8A0, false);
        gui.text(this.font, Component.translatable("gui.codenamecoraline.fire_dragon.saddle"), 10, 17, 0xFFD9B277, false);
        gui.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFC7A88C, false);
    }

    private static void drawSlotFrame(GuiGraphicsExtractor gui, int x, int y, int border, int fill) {
        gui.fill(x, y, x + 18, y + 18, border);
        gui.fill(x + 1, y + 1, x + 17, y + 17, fill);
    }
}
