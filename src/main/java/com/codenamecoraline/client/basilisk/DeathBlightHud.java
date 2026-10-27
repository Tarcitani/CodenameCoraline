package com.codenamecoraline.client.basilisk;

import com.codenamecoraline.entity.basilisk.DeathBlight;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class DeathBlightHud {
    private DeathBlightHud() {}
    public static void render(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || !DeathBlight.susceptible(minecraft.player)) return;
        int amount = DeathBlight.buildup(minecraft.player);
        if (amount <= 0) return;
        int x = graphics.guiWidth() / 2 - 61;
        int y = graphics.guiHeight() - 78;
        graphics.fill(x - 1, y - 1, x + 123, y + 9, 0xFF806527);
        graphics.fill(x, y, x + 122, y + 8, 0xEF0D100E);
        graphics.fill(x + 1, y + 1, x + 1 + amount * 120 / 100, y + 7,
                amount >= 75 && minecraft.player.tickCount % 16 < 8 ? 0xFFF0D272 : 0xFFBD952F);
        var label = Component.translatable("gui.codenamecoraline.death_blight", amount);
        graphics.text(minecraft.font, label, graphics.guiWidth() / 2 - minecraft.font.width(label) / 2,
                y - 11, 0xFFE1CA7C);
    }
}
