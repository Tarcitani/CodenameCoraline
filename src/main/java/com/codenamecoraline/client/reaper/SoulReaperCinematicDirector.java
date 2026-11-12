package com.codenamecoraline.client.reaper;

import com.codenamecoraline.client.collector.CollectorCinematicDirector;
import com.codenamecoraline.entity.reaper.SoulReaper;
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
 * Client-side metamorphosis cinematic director for the Reaper of Wandering Souls: tracks the boss during its 200-tick
 * {@link SoulReaper.State#AWAKENING} sequence, slides in glacial letterbox bars, shakes the camera at the scythe slam
 * (tick 175), displays the title card, and supports skipping via {@link CollectorCinematicDirector#SKIP_CINEMATIC_KEY}.
 */
public final class SoulReaperCinematicDirector {
    public static final double CINEMATIC_RADIUS = 48.0D;

    private static int skippedBossEntityId = -1;
    private static float smoothedYaw = 0.0F;
    private static float smoothedPitch = 0.0F;
    private static boolean anglesInitialised = false;

    private SoulReaperCinematicDirector() {}

    private static @Nullable SoulReaper findAwakeningBoss(Level level, Vec3 center) {
        return level.getEntitiesOfClass(
                        SoulReaper.class,
                        new AABB(center, center).inflate(CINEMATIC_RADIUS),
                        e -> e.isAlive() && e.getState() == SoulReaper.State.AWAKENING)
                .stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(center)))
                .orElse(null);
    }

    private static @Nullable SoulReaper activeBoss() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || player.isDeadOrDying()) {
            return null;
        }
        SoulReaper boss = findAwakeningBoss(mc.level, player.position());
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
        SoulReaper boss = activeBoss();
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

    static Vec3 focalPoint(SoulReaper boss, int t, float partialTick) {
        Vec3 base = boss.getPosition(partialTick);
        double k = Mth.clamp(t / 140.0D, 0.0D, 1.0D);
        return base.add(0.0D, Mth.lerp(k, 1.0D, 4.4D), 0.0D);
    }

    static float fovScale(int t) {
        if (t < SoulReaper.AWAKEN_SLAM_START_TICK) {
            return Mth.lerp(t / (float) SoulReaper.AWAKEN_SLAM_START_TICK, 0.92F, 0.78F);
        }
        float k = (t - SoulReaper.AWAKEN_SLAM_START_TICK)
                / (float) (SoulReaper.AWAKEN_DURATION_TICKS - SoulReaper.AWAKEN_SLAM_START_TICK);
        return Mth.lerp(Mth.clamp(k, 0.0F, 1.0F), 0.78F, 1.0F);
    }

    static float shake(int t) {
        int sinceSlam = t - SoulReaper.AWAKEN_SLAM_IMPACT_TICK;
        if (sinceSlam >= 0 && sinceSlam < 18) {
            return 1.35F * (1.0F - sinceSlam / 18.0F);
        }
        return 0.0F;
    }

    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        SoulReaper boss = activeBoss();
        if (player == null || boss == null) {
            anglesInitialised = false;
            return;
        }
        float partialTick = (float) event.getPartialTick();
        int t = boss.getActionTick();
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
        event.setYaw(smoothedYaw + Mth.sin(time * 2.1F) * s);
        event.setPitch(smoothedPitch + Mth.cos(time * 2.5F) * s * 0.85F);
        event.setRoll(Mth.sin(time * 1.7F) * s * 0.6F);
    }

    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        SoulReaper boss = activeBoss();
        if (boss != null) {
            event.setFOV(event.getFOV() * fovScale(boss.getActionTick()));
        }
    }

    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        SoulReaper boss = activeBoss();
        if (boss == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        GuiGraphicsExtractor gui = event.getGuiGraphics();
        int width = gui.guiWidth();
        int height = gui.guiHeight();
        int t = boss.getActionTick();

        float bar = t < 20
                ? t / 20.0F
                : t > SoulReaper.AWAKEN_DURATION_TICKS - 20
                        ? Math.max(0.0F, (SoulReaper.AWAKEN_DURATION_TICKS - t) / 20.0F)
                        : 1.0F;
        int barHeight = Math.round(height * 0.12F * bar);
        if (barHeight > 0) {
            int barColor = ARGB.color(235, 6, 10, 18);
            gui.fill(0, 0, width, barHeight, barColor);
            gui.fill(0, height - barHeight, width, height, barColor);
        }
        Font font = mc.font;

        int start = SoulReaper.AWAKEN_SLAM_START_TICK;
        int end = SoulReaper.AWAKEN_DURATION_TICKS - 8;
        if (t >= start && t <= end) {
            float alpha = t < start + 10 ? (t - start) / 10.0F : t > end - 10 ? (end - t) / 10.0F : 1.0F;
            int a = Mth.clamp((int) (alpha * 255.0F), 8, 255);
            Component title = Component.translatable("entity.codenamecoraline.soul_reaper.cinematic.title")
                    .withStyle(ChatFormatting.BOLD);
            Component subtitle = Component.translatable("entity.codenamecoraline.soul_reaper.cinematic.subtitle")
                    .withStyle(ChatFormatting.ITALIC);
            int titleY = height - Math.max(barHeight, 24) - 34;
            gui.centeredText(font, title, width / 2, titleY, ARGB.color(a, 224, 246, 255));
            gui.centeredText(font, subtitle, width / 2, titleY + 13, ARGB.color(a, 110, 210, 238));
        }
        if (barHeight >= 14) {
            Component hint = Component.translatable(
                    "entity.codenamecoraline.last_gift_collector.cinematic.skip_hint",
                    CollectorCinematicDirector.SKIP_CINEMATIC_KEY.getTranslatedKeyMessage());
            gui.centeredText(font, hint, width / 2, height - barHeight + (barHeight - 8) / 2, ARGB.color(180, 180, 198, 214));
        }
    }
}

