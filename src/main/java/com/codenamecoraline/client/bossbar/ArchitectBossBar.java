package com.codenamecoraline.client.bossbar;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.architect.Architect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.BossEvent;

/**
 * Health bar of the Architect (spec docs/design/architect.md §19): a carved stone lintel shaped like the Tablet of the
 * Commandments, a gargoyle wing at each end and his hooded head (coral pupils, lilac seal) over the middle. The frame has
 * a transparent health slot: the dark back and the fill are drawn first and the frame on top. The fill follows the phase
 * the server signals through the bar colour: lilac for the Duelist, cyan mana for the Administrator and coral for the
 * Collapse, which flickers like failing mana. Lilac notches mark where phases two (70%) and three (35%) begin.
 */
final class ArchitectBossBar implements ThemedBossBars.Style {
    private static final Identifier FRAME = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/gui/boss_bar/architect.png");
    // Keep in sync with tools/generate_boss_bar_architect.py
    private static final int FRAME_W = 288;
    private static final int FRAME_H = 44;
    private static final int SLOT_X = 46;
    private static final int SLOT_Y = 18;
    private static final int SLOT_W = 196;
    private static final int SLOT_H = 6;
    private static final int NAME_Y = 33;

    @Override
    public int height() {
        return FRAME_H + 6;
    }

    @Override
    public void render(GuiGraphicsExtractor g, LerpingBossEvent boss, int centerX, int top) {
        int fx = centerX - FRAME_W / 2;
        long time = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L;
        boolean collapse = boss.getColor() == BossEvent.BossBarColor.RED;
        int[] rows = palette(boss.getColor());
        int sx = fx + SLOT_X;
        int sy = top + SLOT_Y;

        g.fill(sx, sy, sx + SLOT_W, sy + SLOT_H, 0xFF0B0B10);
        float progress = Math.max(0.0F, Math.min(1.0F, boss.getProgress()));
        int filled = Math.round(SLOT_W * progress);
        for (int i = 0; i < SLOT_H; i++) {
            g.fill(sx, sy + i, sx + filled, sy + i + 1, rows[i]);
        }
        // runes of mana running along the fill: square pulses, irregular in the collapse
        if (filled > 8) {
            long step = collapse ? (time * 7) % 23 : time;
            for (int x = (int) (step % 12); x < filled - 2; x += 12) {
                g.fill(sx + x, sy + 2, sx + x + 2, sy + 4, collapse ? 0x88FFD09A : 0x66F7F2FF);
            }
        }
        for (float threshold : new float[] {Architect.PHASE_ADMINISTRATOR_BELOW, Architect.PHASE_COLLAPSE_BELOW}) {
            int mx = sx + Math.round(SLOT_W * threshold);
            g.fill(mx, sy - 1, mx + 1, sy + SLOT_H + 1, progress <= threshold ? 0xFF4A3A70 : 0xFFD8C8FF);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, FRAME, fx, top, 0.0F, 0.0F, FRAME_W, FRAME_H, FRAME_W, FRAME_H);

        Font font = Minecraft.getInstance().font;
        String name = boss.getName().getString();
        title(g, font, name, centerX - font.width(name) / 2, top + NAME_Y + 2, collapse);
    }

    /** Lettering in pale stone with a lilac edge (coral in the collapse) and a dark outline. */
    private static void title(GuiGraphicsExtractor g, Font font, String text, int x, int y, boolean collapse) {
        int outline = 0xFF0B0B10;
        int edge = collapse ? 0xFFA91F32 : 0xFF7E64C4;
        for (int[] o : new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {1, 1}}) {
            g.text(font, text, x + o[0], y + o[1], outline, false);
        }
        g.text(font, text, x + 1, y + 1, edge, false);
        g.text(font, text, x, y, collapse ? 0xFFFFD09A : 0xFFC7BFB0, false);
    }

    /** Six rows, top to bottom, of the fill colour for the phase the server signals through the bar colour. */
    private static int[] palette(BossEvent.BossBarColor color) {
        return switch (color) {
            // phase 2, the Administrator of the Temple: cyan mana
            case BLUE -> new int[] {0xFFB6FFF0, 0xFF6FE0D6, 0xFF38BDBA, 0xFF38BDBA, 0xFF22908F, 0xFF176A70};
            // phase 3, the Collapse: coral, the colour of his eyes
            case RED -> new int[] {0xFFFFD09A, 0xFFFF8A6E, 0xFFFF604B, 0xFFFF604B, 0xFFD23A44, 0xFFA91F32};
            // phase 1, the Duelist of Six Blades: lilac authority
            default -> new int[] {0xFFF7F2FF, 0xFFE6DCFF, 0xFFD8C8FF, 0xFFD8C8FF, 0xFFA690E0, 0xFF7E64C4};
        };
    }
}
