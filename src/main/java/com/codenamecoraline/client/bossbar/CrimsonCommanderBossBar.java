package com.codenamecoraline.client.bossbar;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.crimson.CrimsonCommander;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.BossEvent;

/**
 * Health bar of the Crimson Commander: his greatsword lying flat, a blade of obsidian steel with gold edges whose fuller
 * is the health slot, the hilt with its ruby closing the left end and the point the right end; his closed helm with the
 * glowing T visor, horns and the red plume sits over the middle. The frame texture has a transparent health slot: the
 * dark back and the fill are drawn first and the frame on top, so the helm overlaps the fill. The fill colour follows his
 * stage (the server sets the boss bar colour per stage): deep crimson in the Iron Vigil, incandescent ruby in the Awakened
 * Oath and blood gold in the Fury of the Last Commander, which also pulses. Gold notches mark where stages two (60%) and
 * three (25%) begin. The name is split in two and set under the rail, either side of the helm.
 */
final class CrimsonCommanderBossBar implements ThemedBossBars.Style {
    private static final Identifier FRAME = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/gui/boss_bar/crimson_commander.png");
    // Keep in sync with tools/generate_boss_bar_crimson_commander.py
    private static final int FRAME_W = 288;
    private static final int FRAME_H = 44;
    private static final int SLOT_X = 46;
    private static final int SLOT_Y = 18;
    private static final int SLOT_W = 196;
    private static final int SLOT_H = 6;
    private static final int NAME_Y = 33;
    private static final int NAME_LEFT_END = 130;   // the left half of the name ends here (frame x), left of the helm
    private static final int NAME_RIGHT_START = 158; // the right half starts here (frame x)
    private static final int NAME_LEFT_MIN = 60;
    private static final int NAME_RIGHT_MAX = 228;

    @Override
    public int height() {
        return FRAME_H + 4;
    }

    @Override
    public void render(GuiGraphicsExtractor g, LerpingBossEvent boss, int centerX, int top) {
        int fx = centerX - FRAME_W / 2;
        long time = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L;
        boolean fury = boss.getColor() == BossEvent.BossBarColor.YELLOW;
        int[] rows = palette(boss.getColor());
        int sx = fx + SLOT_X;
        int sy = top + SLOT_Y;

        // The fury burns behind the blade.
        if (fury) {
            int alpha = 0x30 + (int) (Math.sin(time * 0.35D) * 0x18 + 0x18);
            g.fill(sx - 8, sy - 6, sx + SLOT_W + 8, sy + SLOT_H + 6, (alpha << 24) | 0xFF2A3C);
        }

        // Dark back of the slot, then the fill, then the frame over both.
        g.fill(sx, sy, sx + SLOT_W, sy + SLOT_H, 0xFF120A0E);
        float progress = Math.max(0.0F, Math.min(1.0F, boss.getProgress()));
        int filled = Math.round(SLOT_W * progress);
        for (int i = 0; i < SLOT_H; i++) {
            g.fill(sx, sy + i, sx + filled, sy + i + 1, rows[i]);
        }
        if (filled > 12) { // a glint running along the blade
            int sheen = (int) ((time * (fury ? 3 : 1)) % (filled + 40)) - 20;
            int a = Math.max(0, sheen);
            int b = Math.min(filled, sheen + 10);
            if (b > a) {
                g.fill(sx + a, sy, sx + b, sy + 2, 0x55FFFFFF);
            }
        }
        // Stage marks (60% and 25%): bright while still ahead, dim once passed.
        for (double threshold : new double[] {CrimsonCommander.STAGE_TWO_HEALTH, CrimsonCommander.STAGE_THREE_HEALTH}) {
            int mx = sx + (int) Math.round(SLOT_W * threshold);
            g.fill(mx, sy - 1, mx + 1, sy + SLOT_H + 1, progress <= threshold ? 0xFF6A5420 : 0xFFF3CF55);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, FRAME, fx, top, 0.0F, 0.0F, FRAME_W, FRAME_H, FRAME_W, FRAME_H);

        // Name: split in two, set under the rail either side of the helm.
        Font font = Minecraft.getInstance().font;
        String[] words = boss.getName().getString().split(" ");
        int mid = (words.length + 1) / 2;
        String left = String.join(" ", java.util.Arrays.copyOfRange(words, 0, mid));
        String right = String.join(" ", java.util.Arrays.copyOfRange(words, mid, words.length));
        int ny = top + NAME_Y;
        if (right.isEmpty()) {
            title(g, font, left, centerX - font.width(left) / 2, ny + 3, fury);
        } else {
            int lx = Math.max(fx + NAME_LEFT_MIN, fx + NAME_LEFT_END - font.width(left));
            int rx = Math.min(fx + NAME_RIGHT_START, fx + NAME_RIGHT_MAX - font.width(right));
            title(g, font, left, lx, ny, fury);
            title(g, font, right, rx, ny, fury);
        }
    }

    /** Lettering with an outline, a drop edge and a light-to-dark gradient (bone and gold, or ruby fire in the fury). */
    private static void title(GuiGraphicsExtractor g, Font font, String text, int x, int y, boolean fury) {
        if (text.isEmpty()) {
            return;
        }
        int outline = fury ? 0xFF3A0A06 : 0xFF1A0608;
        int edge = fury ? 0xFF8A2010 : 0xFF6A0E18;
        int light = fury ? 0xFFFFE6A0 : 0xFFF6EAD2;
        int dark = fury ? 0xFFFF5A3C : 0xFFD4AF37;
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
            // stage 2, the Awakened Oath: incandescent ruby
            case PINK -> new int[] {0xFFFFB0B8, 0xFFFF5A6E, 0xFFFF2A3C, 0xFFFF2A3C, 0xFFC0142A, 0xFF7A0A18};
            // stage 3, the Fury of the Last Commander: blood gold
            case YELLOW -> new int[] {0xFFFFF0A0, 0xFFF3CF55, 0xFFD4AF37, 0xFFD4AF37, 0xFFA0461E, 0xFF6A1410};
            // stage 1, the Iron Vigil: deep crimson
            default -> new int[] {0xFFE0505A, 0xFFB81D2A, 0xFF8B111E, 0xFF8B111E, 0xFF5E0A14, 0xFF3A050C};
        };
    }
}
