package com.codenamecoraline.client.bossbar;

import com.codenamecoraline.CodenameCoraline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.BossEvent;

/**
 * Health bar of the Witch of Ten Threads: the beam of her loom with a spool of crimson thread on the left, a bone needle on
 * the right and a patched-cloth emblem over the middle. The fill is the health of the puppet being fought (silver thread
 * through the ten stages, ruby in stage eleven, when the Matriarch fights herself). Ten spool marks sit above the rail,
 * one per puppet: lit for the stages already reached, dim for those still to come, and all burnt out in the fury.
 * The stage number is read from the first argument of the server's boss-bar name ("[Thread N/10] ...").
 */
final class TenThreadsWitchBossBar implements ThemedBossBars.Style {
    private static final Identifier FRAME = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/gui/boss_bar/ten_threads_witch.png");
    // Keep in sync with tools/generate_boss_bar_ten_threads_witch.py
    private static final int FRAME_W = 288;
    private static final int FRAME_H = 44;
    private static final int SLOT_X = 46;
    private static final int SLOT_Y = 18;
    private static final int SLOT_W = 196;
    private static final int SLOT_H = 6;
    private static final int NAME_Y = 33;
    private static final int STAGES = 10;

    @Override
    public int height() {
        return FRAME_H + 4;
    }

    @Override
    public void render(GuiGraphicsExtractor g, LerpingBossEvent boss, int centerX, int top) {
        int fx = centerX - FRAME_W / 2;
        long time = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L;
        boolean fury = boss.getColor() == BossEvent.BossBarColor.RED;
        int stage = fury ? STAGES + 1 : stageOf(boss.getName());
        int[] rows = fury ? FURY_ROWS : thread(stage);
        int sx = fx + SLOT_X;
        int sy = top + SLOT_Y;

        if (fury) { // the loom burns behind the beam
            int alpha = 0x30 + (int) (Math.sin(time * 0.35D) * 0x18 + 0x18);
            g.fill(sx - 8, sy - 6, sx + SLOT_W + 8, sy + SLOT_H + 6, (alpha << 24) | 0xFF2A3C);
        }
        g.fill(sx, sy, sx + SLOT_W, sy + SLOT_H, 0xFF120A10);
        float progress = Math.max(0.0F, Math.min(1.0F, boss.getProgress()));
        int filled = Math.round(SLOT_W * progress);
        for (int i = 0; i < SLOT_H; i++) {
            g.fill(sx, sy + i, sx + filled, sy + i + 1, rows[i]);
        }
        if (filled > 12) { // a glint running along the thread
            int sheen = (int) ((time * (fury ? 3 : 1)) % (filled + 40)) - 20;
            int a = Math.max(0, sheen);
            int b = Math.min(filled, sheen + 10);
            if (b > a) {
                g.fill(sx + a, sy, sx + b, sy + 2, 0x55FFFFFF);
            }
        }
        g.blit(RenderPipelines.GUI_TEXTURED, FRAME, fx, top, 0.0F, 0.0F, FRAME_W, FRAME_H, FRAME_W, FRAME_H);

        // Ten spool marks above the slot, one per puppet.
        for (int i = 0; i < STAGES; i++) {
            int mx = sx + (int) Math.round(SLOT_W * (i + 0.5D) / STAGES) - 2;
            int my = top + 8;
            int color;
            if (fury) {
                color = 0xFF3A1A1E;
            } else if (i + 1 < stage) {
                color = 0xFFCC2034; // a spool already wound back: the puppet fell
            } else if (i + 1 == stage) {
                color = 0xFFE8EEF8; // the one being fought
            } else {
                color = 0xFF5A4A52;
            }
            g.fill(mx, my + 1, mx + 5, my + 4, 0xFF160C10);
            g.fill(mx + 1, my, mx + 4, my + 5, 0xFF160C10);
            g.fill(mx + 1, my + 1, mx + 4, my + 4, color);
            g.fill(mx + 1, my + 1, mx + 4, my + 2, 0x55FFFFFF);
        }

        Font font = Minecraft.getInstance().font;
        String text = boss.getName().getString();
        title(g, font, text, centerX - font.width(text) / 2, top + NAME_Y + 3, fury);
    }

    /** The stage the server announces in the name's first argument, or 1 when the name carries none. */
    private static int stageOf(Component name) {
        if (name.getContents() instanceof TranslatableContents tc && tc.getArgs().length > 0) {
            Object first = tc.getArgs()[0];
            if (first instanceof Integer n) {
                return Math.max(1, Math.min(STAGES, n));
            }
            try {
                return Math.max(1, Math.min(STAGES, Integer.parseInt(String.valueOf(first))));
            } catch (NumberFormatException ignored) {
                return 1;
            }
        }
        return 1;
    }

    /** Lettering with an outline and a light-to-dark gradient (bone and thread, or ruby fire in the fury). */
    private static void title(GuiGraphicsExtractor g, Font font, String text, int x, int y, boolean fury) {
        if (text.isEmpty()) {
            return;
        }
        int outline = fury ? 0xFF3A0A06 : 0xFF160C10;
        int edge = fury ? 0xFF8A2010 : 0xFF5A1626;
        int light = fury ? 0xFFFFE6A0 : 0xFFF4EAD6;
        int dark = fury ? 0xFFFF5A3C : 0xFFC4B08C;
        for (int[] o : new int[][] {{-1, -1}, {1, -1}, {-1, 1}, {2, 1}, {-1, 0}, {2, 0}, {0, -1}, {0, 2}, {1, 2}}) {
            g.text(font, text, x + o[0], y + o[1], outline, false);
        }
        g.text(font, text, x + 1, y + 1, edge, false);
        g.text(font, text, x, y, dark, false);
        g.enableScissor(x - 2, y - 2, x + font.width(text) + 3, y + 4);
        g.text(font, text, x, y, light, false);
        g.disableScissor();
    }

    private static final int[] FURY_ROWS = {0xFFFFB0B8, 0xFFFF5A6E, 0xFFFF2A3C, 0xFFFF2A3C, 0xFFC0142A, 0xFF7A0A18};

    /** Six rows of silver thread, tinted a little more crimson the deeper the stage. */
    private static int[] thread(int stage) {
        int t = Math.max(0, Math.min(STAGES - 1, stage - 1));
        int r = 0xA8 + t * 6;
        int gr = 0xBA - t * 8;
        int b = 0xE0 - t * 9;
        return new int[] {shade(r, gr, b, 1.25F), shade(r, gr, b, 1.1F), shade(r, gr, b, 1.0F), shade(r, gr, b, 1.0F),
                shade(r, gr, b, 0.78F), shade(r, gr, b, 0.55F)};
    }

    private static int shade(int r, int g, int b, float f) {
        return 0xFF000000 | (clamp(r * f) << 16) | (clamp(g * f) << 8) | clamp(b * f);
    }

    private static int clamp(float v) {
        return Math.max(0, Math.min(255, Math.round(v)));
    }
}
