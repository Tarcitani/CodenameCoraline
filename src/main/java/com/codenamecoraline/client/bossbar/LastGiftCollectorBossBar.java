package com.codenamecoraline.client.bossbar;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.collector.LastGiftCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.BossEvent;

/**
 * Health bar of Nicolas Noel, the Renegade: palmate antler racks, a frost-fringed rail, crossed greataxes and his face
 * (hat, glowing eyes, long nose, huge beard) over the middle. The frame texture has a transparent health slot: the dark
 * back and the fill are drawn first and the frame on top, so ornaments overlap the fill. The fill colour follows his
 * stage (the server sets the boss bar colour per stage): red, wine, ice, gold and, in Rage, a pulsing fire. Gold marks
 * on the rail show where each stage begins. The name is split in two and set under the rail, either side of the beard.
 */
final class LastGiftCollectorBossBar implements ThemedBossBars.Style {
    private static final Identifier FRAME = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/gui/boss_bar/last_gift_collector.png");
    // Keep in sync with tools/generate_boss_bar_last_gift_collector.py
    private static final int FRAME_W = 288;
    private static final int FRAME_H = 44;
    private static final int SLOT_X = 46;
    private static final int SLOT_Y = 18;
    private static final int SLOT_W = 196;
    private static final int SLOT_H = 6;
    private static final int NAME_Y = 34;
    private static final int NAME_LEFT_END = 120;   // the left half of the name ends here (frame x)
    private static final int NAME_RIGHT_START = 168; // the right half starts here (frame x)
    private static final int NAME_LEFT_MIN = 50;
    private static final int NAME_RIGHT_MAX = 238;

    @Override
    public int height() {
        return FRAME_H + 4;
    }

    @Override
    public void render(GuiGraphicsExtractor g, LerpingBossEvent boss, int centerX, int top) {
        int fx = centerX - FRAME_W / 2;
        long time = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L;
        boolean rage = boss.getColor() == BossEvent.BossBarColor.RED;
        int[] rows = palette(boss.getColor());
        int sx = fx + SLOT_X;
        int sy = top + SLOT_Y;

        // Rage glow behind the rail.
        if (rage) {
            int alpha = 0x30 + (int) (Math.sin(time * 0.35D) * 0x18 + 0x18);
            g.fill(sx - 8, sy - 6, sx + SLOT_W + 8, sy + SLOT_H + 6, (alpha << 24) | 0xFF3010);
        }

        // Dark back of the slot, then the fill, then the frame over both.
        g.fill(sx, sy, sx + SLOT_W, sy + SLOT_H, 0xFF220C12);
        float progress = Math.max(0.0F, Math.min(1.0F, boss.getProgress()));
        int filled = Math.round(SLOT_W * progress);
        for (int i = 0; i < SLOT_H; i++) {
            g.fill(sx, sy + i, sx + filled, sy + i + 1, rows[i]);
        }
        if (filled > 12) { // a bright sheen sliding along the filled part
            int sheen = (int) ((time * (rage ? 3 : 1)) % (filled + 40)) - 20;
            int a = Math.max(0, sheen);
            int b = Math.min(filled, sheen + 10);
            if (b > a) {
                g.fill(sx + a, sy, sx + b, sy + 2, 0x55FFFFFF);
            }
        }
        // Stage marks (80/60/40/20/10%): bright while still ahead, dim once passed.
        for (double threshold : LastGiftCollector.STAGE_THRESHOLDS) {
            int mx = sx + (int) Math.round(SLOT_W * threshold);
            g.fill(mx, sy - 1, mx + 1, sy + SLOT_H + 1, progress <= threshold ? 0xFF6A5420 : 0xFFFFE27A);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, FRAME, fx, top, 0.0F, 0.0F, FRAME_W, FRAME_H, FRAME_W, FRAME_H);

        // Name: split in two, set under the rail either side of the beard.
        Font font = Minecraft.getInstance().font;
        String[] words = boss.getName().getString().split(" ");
        int mid = (words.length + 1) / 2;
        String left = String.join(" ", java.util.Arrays.copyOfRange(words, 0, mid));
        String right = String.join(" ", java.util.Arrays.copyOfRange(words, mid, words.length));
        int ny = top + NAME_Y;
        if (right.isEmpty()) {
            title(g, font, left, centerX - font.width(left) / 2, ny, rage);
        } else {
            int lx = Math.max(fx + NAME_LEFT_MIN, fx + NAME_LEFT_END - font.width(left));
            int rx = Math.min(fx + NAME_RIGHT_START, fx + NAME_RIGHT_MAX - font.width(right));
            title(g, font, left, lx, ny, rage);
            title(g, font, right, rx, ny, rage);
        }
    }

    /** Lettering with an outline, a drop edge and a light-to-dark gradient (cream/gold, or ember in Rage). */
    private static void title(GuiGraphicsExtractor g, Font font, String text, int x, int y, boolean rage) {
        if (text.isEmpty()) {
            return;
        }
        int outline = rage ? 0xFF3A0A06 : 0xFF2A0A12;
        int edge = rage ? 0xFF8A2010 : 0xFF7A4A1E;
        int light = rage ? 0xFFFFB080 : 0xFFFFF4D6;
        int dark = rage ? 0xFFFF5A30 : 0xFFE6B05E;
        for (int[] o : new int[][] {{-1, -1}, {1, -1}, {-1, 1}, {2, 1}, {-1, 0}, {2, 0}, {0, -1}, {0, 2}, {1, 2}}) {
            g.text(font, text, x + o[0], y + o[1], outline, false);
        }
        g.text(font, text, x + 1, y + 1, edge, false);
        g.text(font, text, x, y, dark, false);
        g.enableScissor(x - 2, y - 2, x + font.width(text) + 3, y + 4);
        g.text(font, text, x, y, light, false);
        g.disableScissor();
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
