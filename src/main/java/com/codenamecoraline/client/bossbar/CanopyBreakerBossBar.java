package com.codenamecoraline.client.bossbar;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.canopy.CanopyBreaker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.BossEvent;

/**
 * Health bar of The Canopy Breaker: a rail of dark cracked bark with claw gouges, gnarled roots curling up from both ends
 * and the brute's face (heavy brow, glowing amber eyes, tusks) over the middle. The frame texture has a transparent
 * health slot: the dark back and the fill are drawn first and the frame on top, so the roots and the face overlap the
 * fill. The fill colour follows his stage (the server sets the boss bar colour per stage): moss green, amber and, in the
 * Fury of the Timber, a pulsing ember. Notches on the rail mark where stages two (60%) and three (25%) begin. The name is
 * set under the rail.
 */
final class CanopyBreakerBossBar implements ThemedBossBars.Style {
    private static final Identifier FRAME = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/gui/boss_bar/canopy_breaker.png");
    // Keep in sync with tools/generate_boss_bar_canopy_breaker.py
    private static final int FRAME_W = 288;
    private static final int FRAME_H = 44;
    private static final int SLOT_X = 46;
    private static final int SLOT_Y = 18;
    private static final int SLOT_W = 196;
    private static final int SLOT_H = 6;
    private static final int NAME_Y = 33;

    @Override
    public int height() {
        return FRAME_H + 4;
    }

    @Override
    public void render(GuiGraphicsExtractor g, LerpingBossEvent boss, int centerX, int top) {
        int fx = centerX - FRAME_W / 2;
        long time = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L;
        boolean fury = boss.getColor() == BossEvent.BossBarColor.RED;
        int[] rows = palette(boss.getColor());
        int sx = fx + SLOT_X;
        int sy = top + SLOT_Y;

        // Fury glow behind the rail.
        if (fury) {
            int alpha = 0x30 + (int) (Math.sin(time * 0.35D) * 0x18 + 0x18);
            g.fill(sx - 8, sy - 6, sx + SLOT_W + 8, sy + SLOT_H + 6, (alpha << 24) | 0xFF3010);
        }

        // Dark back of the slot, then the fill, then the frame over both.
        g.fill(sx, sy, sx + SLOT_W, sy + SLOT_H, 0xFF150E0A);
        float progress = Math.max(0.0F, Math.min(1.0F, boss.getProgress()));
        int filled = Math.round(SLOT_W * progress);
        for (int i = 0; i < SLOT_H; i++) {
            g.fill(sx, sy + i, sx + filled, sy + i + 1, rows[i]);
        }
        if (filled > 12) { // a sheen sliding along the filled part
            int sheen = (int) ((time * (fury ? 3 : 1)) % (filled + 40)) - 20;
            int a = Math.max(0, sheen);
            int b = Math.min(filled, sheen + 10);
            if (b > a) {
                g.fill(sx + a, sy, sx + b, sy + 2, 0x44FFFFFF);
            }
        }
        // Stage marks (60% and 25%): bright while still ahead, dim once passed.
        for (double threshold : new double[] {CanopyBreaker.STAGE_TWO_HEALTH, CanopyBreaker.STAGE_THREE_HEALTH}) {
            int mx = sx + (int) Math.round(SLOT_W * threshold);
            g.fill(mx, sy - 1, mx + 1, sy + SLOT_H + 1, progress <= threshold ? 0xFF4A3A22 : 0xFFE8D8A8);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, FRAME, fx, top, 0.0F, 0.0F, FRAME_W, FRAME_H, FRAME_W, FRAME_H);

        Font font = Minecraft.getInstance().font;
        String name = boss.getName().getString();
        title(g, font, name, centerX - font.width(name) / 2, top + NAME_Y, fury);
    }

    /** Lettering with an outline, a drop edge and a light-to-dark gradient (pale bone, or ember in the Fury). */
    private static void title(GuiGraphicsExtractor g, Font font, String text, int x, int y, boolean fury) {
        if (text.isEmpty()) {
            return;
        }
        int outline = fury ? 0xFF2A0A06 : 0xFF140C08;
        int edge = fury ? 0xFF7A2410 : 0xFF4A3420;
        int light = fury ? 0xFFFFB080 : 0xFFF4EAD0;
        int dark = fury ? 0xFFFF5A30 : 0xFFC8B488;
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
            case YELLOW -> new int[] {0xFFFFE9A0, 0xFFF0C846, 0xFFD2A22A, 0xFFD2A22A, 0xFF9A6A18, 0xFF5E3E0C};
            case RED -> new int[] {0xFFFFDC78, 0xFFFF9628, 0xFFE64614, 0xFFE64614, 0xFFA01E0A, 0xFF5A0A05};
            default -> new int[] {0xFFC6E88A, 0xFF8CC050, 0xFF5E963A, 0xFF5E963A, 0xFF3E6A28, 0xFF244418};
        };
    }
}
