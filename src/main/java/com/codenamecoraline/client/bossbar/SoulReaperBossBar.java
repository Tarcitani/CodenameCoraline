package com.codenamecoraline.client.bossbar;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.reaper.SoulReaper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.BossEvent;

/**
 * Health bar of the Reaper of Wandering Souls: the Great Scythe lying flat with its bone collar and lantern pommel on
 * the left and the frost-rimmed crescent blade on the right, whose recessed fuller is the health slot. Over the center
 * sits the pointed abyssal cowl with two glowing crimson eyes above the ancient silver spiral brooch. Frost-silver
 * notches mark where Stage 2 (65%) and Stage 3 (30%) begin.
 */
final class SoulReaperBossBar implements ThemedBossBars.Style {
    private static final Identifier FRAME = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/gui/boss_bar/soul_reaper.png");
    // Keep in sync with tools/generate_boss_bar_soul_reaper.py
    private static final int FRAME_W = 288;
    private static final int FRAME_H = 44;
    private static final int SLOT_X = 46;
    private static final int SLOT_Y = 18;
    private static final int SLOT_W = 196;
    private static final int SLOT_H = 6;
    private static final int NAME_Y = 33;
    private static final int NAME_LEFT_END = 128;
    private static final int NAME_RIGHT_START = 160;
    private static final int NAME_LEFT_MIN = 58;
    private static final int NAME_RIGHT_MAX = 230;

    @Override
    public int height() {
        return FRAME_H + 4;
    }

    @Override
    public void render(GuiGraphicsExtractor g, LerpingBossEvent boss, int centerX, int top) {
        int fx = centerX - FRAME_W / 2;
        long time = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L;
        boolean finalHarvest = boss.getColor() == BossEvent.BossBarColor.PURPLE;
        int[] rows = palette(boss.getColor());
        int sx = fx + SLOT_X;
        int sy = top + SLOT_Y;

        if (finalHarvest) {
            int alpha = 0x2C + (int) (Math.sin(time * 0.32D) * 0x16 + 0x16);
            g.fill(sx - 8, sy - 6, sx + SLOT_W + 8, sy + SLOT_H + 6, (alpha << 24) | 0xE82040);
        }

        g.fill(sx, sy, sx + SLOT_W, sy + SLOT_H, 0xFF0A0E16);
        float progress = Math.max(0.0F, Math.min(1.0F, boss.getProgress()));
        int filled = Math.round(SLOT_W * progress);
        for (int i = 0; i < SLOT_H; i++) {
            g.fill(sx, sy + i, sx + filled, sy + i + 1, rows[i]);
        }
        if (filled > 12) {
            int sheen = (int) ((time * (finalHarvest ? 3 : 1)) % (filled + 40)) - 20;
            int a = Math.max(0, sheen);
            int b = Math.min(filled, sheen + 10);
            if (b > a) {
                g.fill(sx + a, sy, sx + b, sy + 2, 0x66E4F8FF);
            }
        }
        // Stage marks at 65% and 30%
        for (double threshold : new double[] {
                SoulReaper.Stage.STAGE_2.healthCeiling, SoulReaper.Stage.STAGE_3.healthCeiling}) {
            int mx = sx + (int) Math.round(SLOT_W * threshold);
            g.fill(mx, sy - 1, mx + 1, sy + SLOT_H + 1, progress <= threshold ? 0xFF485C74 : 0xFFE4F4FF);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, FRAME, fx, top, 0.0F, 0.0F, FRAME_W, FRAME_H, FRAME_W, FRAME_H);

        Font font = Minecraft.getInstance().font;
        String[] words = boss.getName().getString().split(" ");
        int mid = (words.length + 1) / 2;
        String left = String.join(" ", java.util.Arrays.copyOfRange(words, 0, mid));
        String right = String.join(" ", java.util.Arrays.copyOfRange(words, mid, words.length));
        int ny = top + NAME_Y;
        if (right.isEmpty()) {
            title(g, font, left, centerX - font.width(left) / 2, ny + 3, finalHarvest);
        } else {
            int lx = Math.max(fx + NAME_LEFT_MIN, fx + NAME_LEFT_END - font.width(left));
            int rx = Math.min(fx + NAME_RIGHT_START, fx + NAME_RIGHT_MAX - font.width(right));
            title(g, font, left, lx, ny, finalHarvest);
            title(g, font, right, rx, ny, finalHarvest);
        }
    }

    private static void title(GuiGraphicsExtractor g, Font font, String text, int x, int y, boolean finalHarvest) {
        if (text.isEmpty()) {
            return;
        }
        int outline = finalHarvest ? 0xFF240612 : 0xFF0A101A;
        int edge = finalHarvest ? 0xFF7A122E : 0xFF244864;
        int light = finalHarvest ? 0xFFFFD6E2 : 0xFFEBF8FF;
        int dark = finalHarvest ? 0xFFE83860 : 0xFF8CC8E8;
        for (int[] o : new int[][] {{-1, -1}, {1, -1}, {-1, 1}, {2, 1}, {-1, 0}, {2, 0}, {0, -1}, {0, 2}, {1, 2}}) {
            g.text(font, text, x + o[0], y + o[1], outline, false);
        }
        g.text(font, text, x + 1, y + 1, edge, false);
        g.text(font, text, x, y, dark, false);
        g.enableScissor(x - 2, y - 2, x + font.width(text) + 3, y + 4);
        g.text(font, text, x, y, light, false);
        g.disableScissor();
    }

    private static int[] palette(BossEvent.BossBarColor color) {
        return switch (color) {
            // Stage 2: Harvest of Wandering Souls (spectral cyan-blue)
            case BLUE -> new int[] {0xFFB0F8FF, 0xFF56DCF4, 0xFF28B4D8, 0xFF28B4D8, 0xFF167C9E, 0xFF0C4862};
            // Stage 3: Winter of the Final Harvest (abyssal crimson-violet)
            case PURPLE -> new int[] {0xFFFFB8CE, 0xFFF24872, 0xFFC81E4A, 0xFFC81E4A, 0xFF8A1032, 0xFF4E081E};
            // Stage 1: Vigil of White Silence (glacial cyan-white)
            default -> new int[] {0xFFF2FCFF, 0xFFC8EEFA, 0xFF8CD2EC, 0xFF8CD2EC, 0xFF5298B8, 0xFF2E5C78};
        };
    }
}
