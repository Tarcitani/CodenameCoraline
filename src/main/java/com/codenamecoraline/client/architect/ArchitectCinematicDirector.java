package com.codenamecoraline.client.architect;

import com.codenamecoraline.client.collector.CollectorCinematicDirector;
import com.codenamecoraline.entity.architect.Architect;
import java.util.Comparator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.jspecify.annotations.Nullable;

/**
 * Client-side cinematic of the Architect's reveal (spec docs/design/architect.md §7.4), in the same form as the other
 * bosses: while the statue wakes (110 ticks) the camera turns to the hooded face, letterbox bars slide in, the falls of
 * the two mask halves and of the tablet shake the camera, and the title card rises with his laugh. The shared skip key
 * ({@link CollectorCinematicDirector#SKIP_CINEMATIC_KEY}) or taking damage releases the camera; skipping is local and
 * never changes the fight on the server (players are protected there by ArchitectRevealProtection).
 */
public final class ArchitectCinematicDirector {
    public static final double CINEMATIC_RADIUS = 48.0D;
    /** The title card: from the jaw opening to the end of the laugh (§7.4 rows 10-11). */
    static final int TITLE_FROM = 86;
    static final int TITLE_TO = 108;
    static final int SHAKE_TICKS = 10;
    private static final int[] SHAKE_BEATS = {
            Architect.REVEAL_MASK_RIGHT_TICK, Architect.REVEAL_MASK_LEFT_TICK, Architect.REVEAL_TABLET_TICK};
    private static final float[] SHAKE_STRENGTH = {0.6F, 0.6F, 1.2F};

    private static int skippedBossEntityId = -1;
    private static float smoothedYaw;
    private static float smoothedPitch;
    private static boolean anglesInitialised;

    private ArchitectCinematicDirector() {}

    private static @Nullable Architect findRevealingBoss(Level level, Vec3 center) {
        return level.getEntitiesOfClass(Architect.class, new AABB(center, center).inflate(CINEMATIC_RADIUS),
                        e -> e.isAlive() && e.getState() == Architect.State.AWAKENING).stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(center)))
                .orElse(null);
    }

    private static @Nullable Architect activeBoss() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || player.isDeadOrDying()) {
            return null;
        }
        Architect boss = findRevealingBoss(mc.level, player.position());
        return boss == null || boss.getId() == skippedBossEntityId ? null : boss;
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || player.isDeadOrDying()) {
            anglesInitialised = false;
            skippedBossEntityId = -1;
            return;
        }
        Architect boss = activeBoss();
        if (boss == null) {
            anglesInitialised = false;
            return;
        }
        while (CollectorCinematicDirector.SKIP_CINEMATIC_KEY.consumeClick()) {
            skippedBossEntityId = boss.getId();
            anglesInitialised = false;
            player.sendOverlayMessage(Component.translatable("entity.codenamecoraline.last_gift_collector.cinematic.skipped")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        if (player.hurtTime > 0) {
            skippedBossEntityId = boss.getId();
            anglesInitialised = false;
        }
    }

    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        if (activeBoss() != null) {
            event.getInput().keyPresses = Input.EMPTY;
        }
    }

    /** The camera looks at the hooded face, then down to the tablet as it falls, then back up to the open jaw. */
    static Vec3 focalPoint(Architect boss, int t, float partialTick) {
        Vec3 base = boss.getPosition(partialTick);
        double y;
        if (t < Architect.REVEAL_MASK_RIGHT_TICK) {
            y = 3.0D;
        } else if (t < Architect.REVEAL_TABLET_TICK + 6) {
            y = Mth.lerp((t - Architect.REVEAL_MASK_RIGHT_TICK) / 20.0D, 3.0D, 1.6D);
        } else {
            y = Mth.lerp(Mth.clamp((t - Architect.REVEAL_TABLET_TICK - 6) / 14.0D, 0.0D, 1.0D), 1.6D, 3.0D);
        }
        return base.add(0.0D, y, 0.0D);
    }

    static float fovScale(int t) {
        if (t < TITLE_FROM) {
            return Mth.lerp(t / (float) TITLE_FROM, 0.92F, 0.8F);
        }
        return Mth.lerp(Mth.clamp((t - TITLE_FROM) / 24.0F, 0.0F, 1.0F), 0.8F, 1.0F);
    }

    /** A jolt as each mask half breaks on the ground and a heavier one as the tablet lands. */
    static float shake(int t) {
        float s = 0.0F;
        for (int i = 0; i < SHAKE_BEATS.length; i++) {
            int since = t - SHAKE_BEATS[i];
            if (since >= 0 && since < SHAKE_TICKS) {
                s = Math.max(s, SHAKE_STRENGTH[i] * (1.0F - since / (float) SHAKE_TICKS));
            }
        }
        return s;
    }

    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Architect boss = activeBoss();
        if (player == null || boss == null) {
            anglesInitialised = false;
            return;
        }
        float partialTick = (float) event.getPartialTick();
        int t = boss.getRevealTicks();
        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 delta = focalPoint(boss, t, partialTick).subtract(eye);
        double horiz = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float targetYaw = (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F;
        float targetPitch = (float) (-(Mth.atan2(delta.y, horiz) * Mth.RAD_TO_DEG));
        if (!anglesInitialised) {
            smoothedYaw = event.getYaw();
            smoothedPitch = event.getPitch();
            anglesInitialised = true;
        }
        smoothedYaw = Mth.rotLerp(0.14F, smoothedYaw, targetYaw);
        smoothedPitch = Mth.lerp(0.14F, smoothedPitch, Mth.clamp(targetPitch, -75.0F, 65.0F));
        float s = shake(t);
        float time = boss.tickCount + partialTick;
        event.setYaw(smoothedYaw + Mth.sin(time * 2.0F) * s);
        event.setPitch(smoothedPitch + Mth.cos(time * 2.4F) * s * 0.85F);
        event.setRoll(Mth.sin(time * 1.6F) * s * 0.6F);
    }

    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        Architect boss = activeBoss();
        if (boss != null) {
            event.setFOV(event.getFOV() * fovScale(boss.getRevealTicks()));
        }
    }

    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Architect boss = activeBoss();
        if (boss == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        GuiGraphicsExtractor gui = event.getGuiGraphics();
        int width = gui.guiWidth();
        int height = gui.guiHeight();
        int t = boss.getRevealTicks();
        int total = com.codenamecoraline.entity.architect.ArchitectAnim.AWAKEN.ticks;

        float bar = t < 16 ? t / 16.0F : t > total - 16 ? Math.max(0.0F, (total - t) / 16.0F) : 1.0F;
        int barHeight = Math.round(height * 0.12F * bar);
        if (barHeight > 0) {
            int barColor = ARGB.color(235, 11, 11, 16);
            gui.fill(0, 0, width, barHeight, barColor);
            gui.fill(0, height - barHeight, width, height, barColor);
        }
        Font font = mc.font;
        if (t >= TITLE_FROM && t <= TITLE_TO) {
            float alpha = t < TITLE_FROM + 8 ? (t - TITLE_FROM) / 8.0F : t > TITLE_TO - 8 ? (TITLE_TO - t) / 8.0F : 1.0F;
            int a = Mth.clamp((int) (alpha * 255.0F), 8, 255);
            Component title = Component.translatable("entity.codenamecoraline.architect.cinematic.title").withStyle(ChatFormatting.BOLD);
            Component subtitle = Component.translatable("entity.codenamecoraline.architect.cinematic.subtitle").withStyle(ChatFormatting.ITALIC);
            int titleY = height - Math.max(barHeight, 24) - 34;
            gui.centeredText(font, title, width / 2, titleY, ARGB.color(a, 216, 200, 255));
            gui.centeredText(font, subtitle, width / 2, titleY + 13, ARGB.color(a, 255, 96, 75));
        }
        if (barHeight >= 14) {
            Component hint = Component.translatable("entity.codenamecoraline.last_gift_collector.cinematic.skip_hint",
                    CollectorCinematicDirector.SKIP_CINEMATIC_KEY.getTranslatedKeyMessage());
            gui.centeredText(font, hint, width / 2, height - barHeight + (barHeight - 8) / 2, ARGB.color(180, 199, 191, 176));
        }
    }
}
