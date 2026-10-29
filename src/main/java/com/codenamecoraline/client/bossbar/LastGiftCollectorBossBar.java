package com.codenamecoraline.client.bossbar;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.collector.LastGiftCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.BossEvent;

/**
 * Health bar of Nicolas Noel, the Renegade: antler ends, a gold rail with fur and icicles, sleigh bells and a medallion
 * with his face. The fill's colour follows his stage (the server sets the boss bar colour per stage): red, wine, ice,
 * gold and, in Rage, a pulsing fire. Gold marks on the rail show where each stage begins.
 */
final class LastGiftCollectorBossBar implements ThemedBossBars.Style {
    private static final Identifier FRAME = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/gui/boss_bar/last_gift_collector.png");
    // Keep in sync with tools/generate_boss_bar_last_gift_collector.py
    private static final int FRAME_W = 256;
    private static final int FRAME_H = 48;
    private static final int SLOT_X = 38;
    private static final int SLOT_Y = 19;
    private static final int SLOT_W = 180;
    private static final int SLOT_H = 6;
    private static final int MEDALLION_X = 104;
    private static final int MEDALLION_W = 48;
    private static final int NAME_GAP = 2;

    @Override
    public int height() {
        return FRAME_H + 10 + NAME_GAP + 8;
    }

    @Override
    public void render(GuiGraphicsExtractor g, LerpingBossEvent boss, int centerX, int top) {
        int fx = centerX - FRAME_W / 2;
        long time = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L;
        boolean rage = boss.getColor() == BossEvent.BossBarColor.RED;
        int[] rows = palette(boss.getColor());

        // Rage glow behind the rail.
        if (rage) {
            int alpha = 0x30 + (int) (Math.sin(time * 0.35D) * 0x18 + 0x18);
            g.fill(fx + SLOT_X - 6, top + SLOT_Y - 5, fx + SLOT_X + SLOT_W + 6, top + SLOT_Y + SLOT_H + 5, (alpha << 24) | 0xFF3010);
        }

        g.blit(RenderPipelines.GUI_TEXTURED, FRAME, fx, top, 0.0F, 0.0F, FRAME_W, FRAME_H, FRAME_W, FRAME_H);

        // Health fill, row by row for a rounded, lit look.
        float progress = Math.max(0.0F, Math.min(1.0F, boss.getProgress()));
        int filled = Math.round(SLOT_W * progress);
        for (int i = 0; i < SLOT_H; i++) {
            g.fill(fx + SLOT_X, top + SLOT_Y + i, fx + SLOT_X + filled, top + SLOT_Y + i + 1, rows[i]);
        }
        // A bright sheen sliding along the filled part.
        if (filled > 12) {
            int sheen = (int) ((time * (rage ? 3 : 1)) % (filled + 40)) - 20;
            int sx0 = Math.max(0, sheen);
            int sx1 = Math.min(filled, sheen + 10);
            if (sx1 > sx0) {
                g.fill(fx + SLOT_X + sx0, top + SLOT_Y, fx + SLOT_X + sx1, top + SLOT_Y + 2, 0x55FFFFFF);
            }
        }
        // Stage marks (80/60/40/20/10%): bright while still ahead, dim once passed.
        for (double threshold : LastGiftCollector.STAGE_THRESHOLDS) {
            int mx = fx + SLOT_X + (int) Math.round(SLOT_W * threshold);
            boolean passed = progress <= threshold;
            g.fill(mx, top + SLOT_Y - 1, mx + 1, top + SLOT_Y + SLOT_H + 1, passed ? 0xFF6A5420 : 0xFFFFE27A);
        }
        // The medallion sits over the rail: draw it again on top of the fill.
        g.blit(RenderPipelines.GUI_TEXTURED, FRAME, fx + MEDALLION_X, top, (float) MEDALLION_X, 0.0F,
                MEDALLION_W, FRAME_H, FRAME_W, FRAME_H);

        // Name, outlined, centred under the medallion.
        Font font = Minecraft.getInstance().font;
        Component name = boss.getName();
        int nx = centerX - font.width(name) / 2;
        int ny = top + FRAME_H + NAME_GAP;
        int outline = rage ? 0xFF3A0A06 : 0xFF2A0A12;
        for (int[] o : new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
            g.text(font, name, nx + o[0], ny + o[1], outline, false);
        }
        g.text(font, name, nx, ny, rage ? 0xFFFF8060 : 0xFFFFF1D0, false);
    }

    /** Six rows, top to bottom, of the fill colour for the stage the server signals through the bar colour. */
    private static int[] palette(BossEvent.BossBarColor color) {
        return switch (color) {
            case PURPLE -> new int[] {0xFFE68AC8, 0xFFAA3282, 0xFF821E64, 0xFF821E64, 0xFF5A1046, 0xFF38082C};
            case BLUE -> new int[] {0xFFD2F5FF, 0xFF82CDF0, 0xFF50A0D7, 0xFF50A0D7, 0xFF326EAA, 0xFF1E4678};
            case YELLOW -> new int[] {0xFFFFF0A0, 0xFFF0C846, 0xFFCD961E, 0xFFCD961E, 0xFF966414, 0xFF5A3C0A};
            case RED -> new int[] {0xFFFFDC78, 0xFFFF9628, 0xFFE64614, 0xFFE64614, 0xFFA01E0A, 0xFF5A0A05};
            default -> new int[] {0xFFFF786E, 0xFFD6303C, 0xFFB0182A, 0xFFB0182A, 0xFF78101E, 0xFF460814};
        };
    }
}
