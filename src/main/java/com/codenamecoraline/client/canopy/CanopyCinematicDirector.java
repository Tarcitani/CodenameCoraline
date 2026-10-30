package com.codenamecoraline.client.canopy;

import com.codenamecoraline.client.collector.CollectorCinematicDirector;
import com.codenamecoraline.entity.canopy.CanopyBreaker;
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
 * Client-side intro cinematic of the Canopy Breaker, in the same form as the Collector's: while the boss is in its intro
 * the camera turns to him (first up at the canopy, then to his chest and face), the view narrows, letterbox bars slide in,
 * every blow of the chest-beat and the roar shake the camera, and the title card appears with the roar. The same skip key
 * ({@link CollectorCinematicDirector#SKIP_CINEMATIC_KEY}) or taking damage releases the camera; skipping is local only and
 * never changes the fight on the server.
 */
public final class CanopyCinematicDirector {
    public static final double CINEMATIC_RADIUS = 48.0D;

    private static int skippedBossEntityId = -1;
    private static float smoothedYaw = 0.0F;
    private static float smoothedPitch = 0.0F;
    private static boolean anglesInitialised = false;

    private CanopyCinematicDirector() {}

    private static @Nullable CanopyBreaker findIntroBoss(Level level, Vec3 center) {
        return level.getEntitiesOfClass(CanopyBreaker.class, new AABB(center, center).inflate(CINEMATIC_RADIUS),
                        e -> e.isAlive() && e.getActivity() == CanopyBreaker.Activity.INTRO).stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(center)))
                .orElse(null);
    }

    private static @Nullable CanopyBreaker activeBoss() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || player.isDeadOrDying()) {
            return null;
        }
        CanopyBreaker boss = findIntroBoss(mc.level, player.position());
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
        CanopyBreaker boss = activeBoss();
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

    /** Where the camera looks: the trees above him first, then his chest during the drumming, then his roaring face. */
    static Vec3 focalPoint(CanopyBreaker boss, int t, float partialTick) {
        Vec3 base = boss.getPosition(partialTick);
        if (t < CanopyBreaker.INTRO_BEAT_TICK) {
            double k = t / (double) CanopyBreaker.INTRO_BEAT_TICK;
            return base.add(0.0D, Mth.lerp(k, 6.5D, 2.6D), 0.0D);
        }
        if (t < CanopyBreaker.INTRO_ROAR_TICK) {
            return base.add(0.0D, 2.4D, 0.0D);
        }
        return base.add(0.0D, 3.0D, 0.0D);
    }

    static float fovScale(int t) {
        if (t < CanopyBreaker.INTRO_BEAT_TICK) {
            return 0.95F;
        }
        if (t < CanopyBreaker.INTRO_ROAR_TICK) {
            float k = (t - CanopyBreaker.INTRO_BEAT_TICK) / (float) (CanopyBreaker.INTRO_ROAR_TICK - CanopyBreaker.INTRO_BEAT_TICK);
            return Mth.lerp(k, 0.9F, 0.82F);
        }
        if (t < CanopyBreaker.INTRO_SETTLE_TICK) {
            return 0.78F;
        }
        float k = (t - CanopyBreaker.INTRO_SETTLE_TICK) / (float) (CanopyBreaker.INTRO_TICKS - CanopyBreaker.INTRO_SETTLE_TICK);
        return Mth.lerp(Mth.clamp(k, 0.0F, 1.0F), 0.78F, 1.0F);
    }

    /** A short jolt on every blow of the chest-beat, a long one on the roar. */
    static float shake(int t) {
        int sinceBeat = t - CanopyBreaker.INTRO_BEAT_TICK;
        if (sinceBeat > 0 && sinceBeat <= 40) {
            return 0.9F * (1.0F - (sinceBeat % 4) / 4.0F);
        }
        int sinceRoar = t - (CanopyBreaker.INTRO_ROAR_TICK + 6);
        if (sinceRoar >= 0 && sinceRoar < 22) {
            return 1.6F * (1.0F - sinceRoar / 22.0F);
        }
        return 0.0F;
    }

    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        CanopyBreaker boss = activeBoss();
        if (player == null || boss == null) {
            anglesInitialised = false;
            return;
        }
        float partialTick = (float) event.getPartialTick();
        int t = boss.getIntroTicks();
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
        smoothedYaw = Mth.rotLerp(0.16F, smoothedYaw, targetYaw);
        smoothedPitch = Mth.lerp(0.16F, smoothedPitch, Mth.clamp(targetPitch, -75.0F, 65.0F));
        float s = shake(t);
        float time = boss.tickCount + partialTick;
        event.setYaw(smoothedYaw + Mth.sin(time * 2.0F) * s);
        event.setPitch(smoothedPitch + Mth.cos(time * 2.4F) * s * 0.85F);
        event.setRoll(Mth.sin(time * 1.6F) * s * 0.6F);
    }

    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        CanopyBreaker boss = activeBoss();
        if (boss != null) {
            event.setFOV(event.getFOV() * fovScale(boss.getIntroTicks()));
        }
    }

    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        CanopyBreaker boss = activeBoss();
        if (boss == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        GuiGraphicsExtractor gui = event.getGuiGraphics();
        int width = gui.guiWidth();
        int height = gui.guiHeight();
        int t = boss.getIntroTicks();

        // letterbox bars slide in over 16 ticks and out over the last 16
        float bar = t < 16 ? t / 16.0F : t > CanopyBreaker.INTRO_TICKS - 16 ? Math.max(0.0F, (CanopyBreaker.INTRO_TICKS - t) / 16.0F) : 1.0F;
        int barHeight = Math.round(height * 0.12F * bar);
        if (barHeight > 0) {
            int barColor = ARGB.color(235, 8, 10, 6);
            gui.fill(0, 0, width, barHeight, barColor);
            gui.fill(0, height - barHeight, width, height, barColor);
        }
        Font font = mc.font;

        // title card with the roar
        int start = CanopyBreaker.INTRO_ROAR_TICK + 4;
        int end = CanopyBreaker.INTRO_TICKS - 8;
        if (t >= start && t <= end) {
            float alpha = t < start + 10 ? (t - start) / 10.0F : t > end - 10 ? (end - t) / 10.0F : 1.0F;
            int a = Mth.clamp((int) (alpha * 255.0F), 8, 255);
            Component title = Component.translatable("entity.codenamecoraline.canopy_breaker.cinematic.title").withStyle(ChatFormatting.BOLD);
            Component subtitle = Component.translatable("entity.codenamecoraline.canopy_breaker.cinematic.subtitle").withStyle(ChatFormatting.ITALIC);
            int titleY = height - Math.max(barHeight, 24) - 34;
            gui.centeredText(font, title, width / 2, titleY, ARGB.color(a, 236, 226, 196));
            gui.centeredText(font, subtitle, width / 2, titleY + 13, ARGB.color(a, 156, 192, 118));
        }
        if (barHeight >= 14) {
            Component hint = Component.translatable("entity.codenamecoraline.last_gift_collector.cinematic.skip_hint",
                    CollectorCinematicDirector.SKIP_CINEMATIC_KEY.getTranslatedKeyMessage());
            gui.centeredText(font, hint, width / 2, height - barHeight + (barHeight - 8) / 2, ARGB.color(180, 190, 196, 170));
        }
    }
}
