package com.codenamecoraline.client.crimson;

import com.codenamecoraline.client.collector.CollectorCinematicDirector;
import com.codenamecoraline.entity.crimson.CrimsonCommander;
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
 * Client-side intro cinematic of the Crimson Commander, in the same form as the other bosses: while he is in his intro the
 * camera turns to him (first the planted sword and his bowed head, then up as he rises), the view narrows, letterbox bars
 * slide in, the blade torn from the floor and the salute shake the camera, and the title card appears. The same skip key
 * ({@link CollectorCinematicDirector#SKIP_CINEMATIC_KEY}) or taking damage releases the camera; skipping is local only and
 * never changes the fight on the server.
 */
public final class CrimsonCinematicDirector {
    public static final double CINEMATIC_RADIUS = 48.0D;

    private static int skippedBossEntityId = -1;
    private static float smoothedYaw = 0.0F;
    private static float smoothedPitch = 0.0F;
    private static boolean anglesInitialised = false;

    private CrimsonCinematicDirector() {}

    private static @Nullable CrimsonCommander findIntroBoss(Level level, Vec3 center) {
        return level.getEntitiesOfClass(CrimsonCommander.class, new AABB(center, center).inflate(CINEMATIC_RADIUS),
                        e -> e.isAlive() && e.getActivity() == CrimsonCommander.Activity.INTRO).stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(center)))
                .orElse(null);
    }

    private static @Nullable CrimsonCommander activeBoss() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || player.isDeadOrDying()) {
            return null;
        }
        CrimsonCommander boss = findIntroBoss(mc.level, player.position());
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
        CrimsonCommander boss = activeBoss();
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

    /** Where the camera looks: the planted sword and his bowed head, then up along his body as he rises. */
    static Vec3 focalPoint(CrimsonCommander boss, int t, float partialTick) {
        Vec3 base = boss.getPosition(partialTick);
        if (t < CrimsonCommander.INTRO_RISE_TICK) {
            double k = t / (double) CrimsonCommander.INTRO_RISE_TICK;
            return base.add(0.0D, Mth.lerp(k, 0.6D, 1.6D), 0.0D);
        }
        double k = Mth.clamp((t - CrimsonCommander.INTRO_RISE_TICK) / 24.0D, 0.0D, 1.0D);
        return base.add(0.0D, Mth.lerp(k, 1.6D, 3.0D), 0.0D);
    }

    static float fovScale(int t) {
        if (t < CrimsonCommander.INTRO_RISE_TICK) {
            return 0.9F;
        }
        if (t < CrimsonCommander.INTRO_TITLE_TICK) {
            return Mth.lerp((t - CrimsonCommander.INTRO_RISE_TICK) / (float) (CrimsonCommander.INTRO_TITLE_TICK - CrimsonCommander.INTRO_RISE_TICK), 0.9F, 0.78F);
        }
        float k = (t - CrimsonCommander.INTRO_TITLE_TICK) / (float) (CrimsonCommander.INTRO_TICKS - CrimsonCommander.INTRO_TITLE_TICK);
        return Mth.lerp(Mth.clamp(k, 0.0F, 1.0F), 0.78F, 1.0F);
    }

    /** A jolt when the blade is torn from the floor and a ringing one at the salute. */
    static float shake(int t) {
        int sinceRise = t - CrimsonCommander.INTRO_RISE_TICK;
        if (sinceRise >= 0 && sinceRise < 14) {
            return 1.1F * (1.0F - sinceRise / 14.0F);
        }
        int sinceSalute = t - (CrimsonCommander.INTRO_RISE_TICK + 22);
        if (sinceSalute >= 0 && sinceSalute < 12) {
            return 0.7F * (1.0F - sinceSalute / 12.0F);
        }
        return 0.0F;
    }

    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        CrimsonCommander boss = activeBoss();
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
        CrimsonCommander boss = activeBoss();
        if (boss != null) {
            event.setFOV(event.getFOV() * fovScale(boss.getIntroTicks()));
        }
    }

    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        CrimsonCommander boss = activeBoss();
        if (boss == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        GuiGraphicsExtractor gui = event.getGuiGraphics();
        int width = gui.guiWidth();
        int height = gui.guiHeight();
        int t = boss.getIntroTicks();

        // letterbox bars slide in over 16 ticks and out over the last 16
        float bar = t < 16 ? t / 16.0F : t > CrimsonCommander.INTRO_TICKS - 16 ? Math.max(0.0F, (CrimsonCommander.INTRO_TICKS - t) / 16.0F) : 1.0F;
        int barHeight = Math.round(height * 0.12F * bar);
        if (barHeight > 0) {
            int barColor = ARGB.color(235, 10, 4, 6);
            gui.fill(0, 0, width, barHeight, barColor);
            gui.fill(0, height - barHeight, width, height, barColor);
        }
        Font font = mc.font;

        // title card with the roar
        int start = CrimsonCommander.INTRO_TITLE_TICK;
        int end = CrimsonCommander.INTRO_TICKS - 8;
        if (t >= start && t <= end) {
            float alpha = t < start + 10 ? (t - start) / 10.0F : t > end - 10 ? (end - t) / 10.0F : 1.0F;
            int a = Mth.clamp((int) (alpha * 255.0F), 8, 255);
            Component title = Component.translatable("entity.codenamecoraline.crimson_commander.cinematic.title").withStyle(ChatFormatting.BOLD);
            Component subtitle = Component.translatable("entity.codenamecoraline.crimson_commander.cinematic.subtitle").withStyle(ChatFormatting.ITALIC);
            int titleY = height - Math.max(barHeight, 24) - 34;
            gui.centeredText(font, title, width / 2, titleY, ARGB.color(a, 244, 226, 190));
            gui.centeredText(font, subtitle, width / 2, titleY + 13, ARGB.color(a, 214, 70, 80));
        }
        if (barHeight >= 14) {
            Component hint = Component.translatable("entity.codenamecoraline.last_gift_collector.cinematic.skip_hint",
                    CollectorCinematicDirector.SKIP_CINEMATIC_KEY.getTranslatedKeyMessage());
            gui.centeredText(font, hint, width / 2, height - barHeight + (barHeight - 8) / 2, ARGB.color(180, 190, 196, 170));
        }
    }
}
