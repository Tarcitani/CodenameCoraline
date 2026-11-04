package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.inventory.DragonMenu;
import com.codenamecoraline.entity.babydragon.DragonStage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Vanilla chest panels, inventory slots and XP sprites. No additional GUI texture. */
public final class DragonScreen extends AbstractContainerScreen<DragonMenu> {
    private static final Identifier CHEST = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    public DragonScreen(DragonMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 224); this.inventoryLabelY = 130;
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        int x = this.leftPos, y = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, CHEST, x, y, 0, 0, 176, 17, 256, 256);
        for (int row = 0; row < 6; row++) graphics.blit(RenderPipelines.GUI_TEXTURED, CHEST, x, y + 17 + row * 18, 0, 17, 176, 18, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, CHEST, x, y + 125, 0, 126, 176, 96, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, CHEST, x, y + 128, 0, 126, 176, 96, 256, 256);
        graphics.fill(x + 7, y + 17, x + 169, y + 128, 0xFFC6C6C6);
        for (int slot = 0; slot < this.menu.getStage().slots; slot++) {
            int sx = x + 7 + slot % 9 * 18, sy = y + 83 + slot / 9 * 18;
            graphics.blit(RenderPipelines.GUI_TEXTURED, CHEST, sx, sy, 7, 17, 18, 18, 256, 256);
        }
        int xp = this.menu.value(1), base = this.menu.getStage().totalXp;
        int needed = this.menu.getStage() == DragonStage.ADULT ? 1 : this.menu.getStage().next().totalXp - base;
        int width = this.menu.getStage() == DragonStage.ADULT ? 160 : Math.clamp((xp - base) * 160 / needed, 0, 160);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("hud/experience_bar_background"), x + 8, y + 58, 160, 5);
        if (width > 0) graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("hud/experience_bar_progress"), x + 8, y + 58, width, 5);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.font.plainSubstrByWidth(this.title.getString(), 160), 8, 6, 0xFF404040, false);
        line(graphics, Component.translatable("gui.codenamecoraline.dragon.owner", this.menu.ownerName()), 18);
        line(graphics, Component.translatable("gui.codenamecoraline.dragon.vitals", this.menu.getStage().title(), this.menu.value(2), this.menu.value(3)), 28);
        line(graphics, Component.translatable("gui.codenamecoraline.dragon.stats", this.menu.value(4), this.menu.value(5), String.format(java.util.Locale.ROOT, "%.2f", this.menu.value(6) / 1000D)), 38);
        line(graphics, this.menu.getStage() == DragonStage.ADULT ? Component.translatable("gui.codenamecoraline.dragon.max") :
                Component.translatable("gui.codenamecoraline.dragon.xp", this.menu.value(1) - this.menu.getStage().totalXp,
                        this.menu.getStage().next().totalXp - this.menu.getStage().totalXp), 48);
        String[] names = {"flight", "fireball", "breath", "riding"};
        for (int i = 0; i < names.length; i++) {
            boolean unlocked = i < 2 || this.menu.getStage() == DragonStage.ADULT;
            graphics.text(this.font, Component.translatable("gui.codenamecoraline.dragon.ability." + names[i]), 8 + (i % 2) * 104, 64 + (i / 2) * 10,
                    unlocked ? 0xFF315B24 : 0xFF888888, false);
        }
        graphics.text(this.font, this.playerInventoryTitle, 8, 130, 0xFF404040, false);
    }
    private void line(GuiGraphicsExtractor graphics, Component text, int y) {
        graphics.text(this.font, this.font.plainSubstrByWidth(text.getString(), 160), 8, y, 0xFF404040, false);
    }
    @Override protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (this.isHovering(8, 64, 160, 20, mouseX, mouseY)) {
            int index = ((mouseY - this.topPos - 64) / 10) * 2 + Math.min(1, (mouseX - this.leftPos - 8) / 104);
            String key = index == 0 ? "flight" : index == 1 ? "fireball_" + this.menu.getStage().name().toLowerCase(java.util.Locale.ROOT) : index == 2 ? "breath" : "riding";
            graphics.setTooltipForNextFrame(this.font, Component.translatable("gui.codenamecoraline.dragon.help." + key), mouseX, mouseY);
        }
    }
}
