package com.codenamecoraline.client.collector;

import com.codenamecoraline.entity.collector.CollectorSleigh;
import com.codenamecoraline.entity.collector.LastGiftCollector;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
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
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.jspecify.annotations.Nullable;

/**
 * Client-side cinematic camera, FOV, HUD letterbox/title overlay, and input director for
 * the Collector of the Last Gift (`codenamecoraline:last_gift_collector`).
 *
 * <p>Guarantees full restoration of local camera, FOV, HUD, and movement input if the player
 * presses the skip key ({@link #SKIP_CINEMATIC_KEY}), takes damage, dies, changes dimension,
 * or disconnects. Skipping is strictly local to the client and never alters server combat state.
 */
public final class CollectorCinematicDirector {
    public static final double CINEMATIC_RADIUS = 48.0D;
    public static final double SHAKE_RADIUS = 26.0D;

    public static final KeyMapping SKIP_CINEMATIC_KEY = new KeyMapping(
            "key.codenamecoraline.skip_cinematic",
            InputConstants.KEY_V,
            KeyMapping.Category.GAMEPLAY
    );

    private static int activeBossEntityId = -1;
    private static int skippedBossEntityId = -1;
    private static float smoothedYaw = 0.0F;
    private static float smoothedPitch = 0.0F;
    private static boolean anglesInitialised = false;

    private CollectorCinematicDirector() {}

    public static void resetState() {
        activeBossEntityId = -1;
        anglesInitialised = false;
    }

    public static boolean isIntroActiveForLocalPlayer() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;
        if (player == null || level == null || player.isDeadOrDying()) {
            return false;
        }
        LastGiftCollector boss = findActiveIntroBoss(level, player.position());
        if (boss == null) {
            return false;
        }
        return boss.getId() != skippedBossEntityId;
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;

        if (player == null || level == null || player.isDeadOrDying()) {
            resetState();
            skippedBossEntityId = -1;
            return;
        }

        LastGiftCollector introBoss = findActiveIntroBoss(level, player.position());
        if (introBoss == null) {
            resetState();
            return;
        }

        activeBossEntityId = introBoss.getId();

        // Allow skipping via configurable keybind or immediately release lock if the player takes damage
        while (SKIP_CINEMATIC_KEY.consumeClick()) {
            if (activeBossEntityId != -1 && skippedBossEntityId != activeBossEntityId) {
                skippedBossEntityId = activeBossEntityId;
                resetState();
                player.sendOverlayMessage(
                        Component.translatable("entity.codenamecoraline.last_gift_collector.cinematic.skipped")
                                .withStyle(ChatFormatting.GRAY)
                );
                return;
            }
        }

        if (player.hurtTime > 0 && skippedBossEntityId != activeBossEntityId) {
            skippedBossEntityId = activeBossEntityId;
            resetState();
        }
    }

    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        if (!isIntroActiveForLocalPlayer()) {
            return;
        }
        var input = event.getInput();
        input.keyPresses = Input.EMPTY;
    }

    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;
        if (player == null || level == null || player.isDeadOrDying()) {
            resetState();
            return;
        }

        float partialTick = (float) event.getPartialTick();
        LastGiftCollector introBoss = findActiveIntroBoss(level, player.position());

        if (introBoss != null && introBoss.getId() != skippedBossEntityId) {
            Vec3 eyePos = player.getEyePosition(partialTick);
            Vec3 focalPoint = computeFocalPoint(level, introBoss, partialTick);
            Vec3 delta = focalPoint.subtract(eyePos);
            double horiz = Math.sqrt(delta.x * delta.x + delta.z * delta.z);

            float targetYaw = (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F;
            float targetPitch = (float) (-(Mth.atan2(delta.y, horiz) * Mth.RAD_TO_DEG));

            if (!anglesInitialised) {
                smoothedYaw = event.getYaw();
                smoothedPitch = event.getPitch();
                anglesInitialised = true;
            }

            smoothedYaw = Mth.rotLerp(0.18F, smoothedYaw, targetYaw);
            smoothedPitch = Mth.lerp(0.18F, smoothedPitch, Mth.clamp(targetPitch, -75.0F, 65.0F));

            float shake = computeShakeIntensity(introBoss);
            float time = introBoss.tickCount + partialTick;
            float shakeYaw = Mth.sin(time * 1.9F) * shake;
            float shakePitch = Mth.cos(time * 2.3F) * shake * 0.85F;
            float shakeRoll = Mth.sin(time * 1.5F) * shake * 0.65F;

            event.setYaw(smoothedYaw + shakeYaw);
            event.setPitch(smoothedPitch + shakePitch);
            event.setRoll(shakeRoll);
            return;
        }

        anglesInitialised = false;

        // Subtle combat/transition/death camera shake for nearby players without locking camera
        LastGiftCollector combatBoss = findNearbyCollector(level, player.position(), SHAKE_RADIUS);
        if (combatBoss != null) {
            float shake = computeShakeIntensity(combatBoss);
            if (shake > 0.01F) {
                float distFactor = 1.0F - (float) Math.min(1.0D, player.distanceTo(combatBoss) / SHAKE_RADIUS);
                float scaled = shake * distFactor;
                float time = combatBoss.tickCount + partialTick;
                event.setYaw(event.getYaw() + Mth.sin(time * 2.1F) * scaled);
                event.setPitch(event.getPitch() + Mth.cos(time * 2.5F) * scaled * 0.8F);
                event.setRoll(event.getRoll() + Mth.sin(time * 1.7F) * scaled * 0.5F);
            }
        }
    }

    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;
        if (player == null || level == null || player.isDeadOrDying()) {
            return;
        }

        LastGiftCollector introBoss = findActiveIntroBoss(level, player.position());
        if (introBoss != null && introBoss.getId() != skippedBossEntityId) {
            float scale = computeIntroFovScale(introBoss.getIntroStage(), introBoss.getIntroTicks());
            event.setFOV(event.getFOV() * scale);
        }
    }

    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;
        if (player == null || level == null || player.isDeadOrDying()) {
            return;
        }

        LastGiftCollector introBoss = findActiveIntroBoss(level, player.position());
        if (introBoss == null || introBoss.getId() == skippedBossEntityId) {
            return;
        }

        GuiGraphicsExtractor gui = event.getGuiGraphics();
        int width = gui.guiWidth();
        int height = gui.guiHeight();
        int introTicks = introBoss.getIntroTicks();
        LastGiftCollector.IntroStage stage = introBoss.getIntroStage();

        // Cold-blue lightning flash overlay during INTRO_LIGHTNING (ticks 24..32)
        if (stage == LastGiftCollector.IntroStage.INTRO_LIGHTNING && introTicks >= 24 && introTicks <= 32) {
            float flashAlpha = 1.0F - ((introTicks - 24) / 8.0F);
            int flashColor = ARGB.color((int) (flashAlpha * 110.0F), 185, 225, 255);
            gui.fill(0, 0, width, height, flashColor);
        }

        // Smooth letterbox bars (slide in 0..16 ticks, slide out 174..190 ticks)
        float barFactor = 1.0F;
        if (introTicks < 16) {
            barFactor = introTicks / 16.0F;
        } else if (introTicks > 174) {
            barFactor = Math.max(0.0F, (LastGiftCollector.INTRO_TOTAL_TICKS - introTicks) / 16.0F);
        }
        int barHeight = Math.round(height * 0.12F * barFactor);
        if (barHeight > 0) {
            int barColor = ARGB.color(235, 6, 8, 14);
            gui.fill(0, 0, width, barHeight, barColor);
            gui.fill(0, height - barHeight, width, height, barColor);
        }

        Font font = mc.font;

        // Title and subtitle card during INTRO_REVEAL (ticks 146..188)
        if (stage == LastGiftCollector.IntroStage.INTRO_REVEAL && introTicks >= 146 && introTicks <= 188) {
            float alpha = 1.0F;
            if (introTicks < 156) {
                alpha = (introTicks - 146) / 10.0F;
            } else if (introTicks > 178) {
                alpha = (188 - introTicks) / 10.0F;
            }
            int alphaInt = Mth.clamp((int) (alpha * 255.0F), 8, 255);
            int titleColor = ARGB.color(alphaInt, 232, 244, 255);
            int subColor = ARGB.color(alphaInt, 168, 202, 232);

            Component title = Component.translatable("entity.codenamecoraline.last_gift_collector.cinematic.title")
                    .withStyle(ChatFormatting.BOLD);
            Component subtitle = Component.translatable("entity.codenamecoraline.last_gift_collector.cinematic.subtitle")
                    .withStyle(ChatFormatting.ITALIC);

            int titleY = height - Math.max(barHeight, 24) - 34;
            gui.centeredText(font, title, width / 2, titleY, titleColor);
            gui.centeredText(font, subtitle, width / 2, titleY + 13, subColor);
        }

        // Skip prompt in the bottom letterbox bar
        if (barHeight >= 14) {
            Component skipHint = Component.translatable(
                    "entity.codenamecoraline.last_gift_collector.cinematic.skip_hint",
                    SKIP_CINEMATIC_KEY.getTranslatedKeyMessage()
            );
            int hintColor = ARGB.color(180, 175, 190, 208);
            gui.centeredText(font, skipHint, width / 2, height - barHeight + (barHeight - 8) / 2, hintColor);
        }
    }

    public static Vec3 computeFocalPoint(Level level, LastGiftCollector boss, float partialTick) {
        Vec3 anchor = boss.getAnchorPosition();
        LastGiftCollector.IntroStage stage = boss.getIntroStage();

        return switch (stage) {
            case INTRO_PRELUDE -> anchor.add(0.0D, 20.0D, -12.0D);
            case INTRO_LIGHTNING -> anchor.add(-24.0D, 27.0D, -20.0D);
            case INTRO_SLEIGH_PASS -> {
                CollectorSleigh sleigh = findNearestSleigh(level, anchor);
                if (sleigh != null) {
                    yield sleigh.getPosition(partialTick).add(0.0D, 1.5D, 0.0D);
                }
                yield boss.getPosition(partialTick).add(0.0D, 2.5D, 0.0D);
            }
            case INTRO_JUMP, INTRO_FALL -> boss.getPosition(partialTick).add(0.0D, 2.6D, 0.0D);
            case INTRO_LANDING -> anchor.add(0.0D, 2.0D, 0.0D);
            case INTRO_REVEAL, INTRO_COMPLETE -> boss.getPosition(partialTick).add(0.0D, 3.2D, 0.0D);
        };
    }

    public static float computeIntroFovScale(LastGiftCollector.IntroStage stage, int introTicks) {
        return switch (stage) {
            case INTRO_PRELUDE -> 0.96F;
            case INTRO_LIGHTNING -> 0.92F;
            case INTRO_SLEIGH_PASS -> 0.90F;
            case INTRO_JUMP -> 0.84F;
            case INTRO_FALL -> 0.88F;
            case INTRO_LANDING -> 0.92F;
            case INTRO_REVEAL -> {
                float revealProgress = Mth.clamp((introTicks - 146) / 44.0F, 0.0F, 1.0F);
                yield Mth.lerp(revealProgress, 0.86F, 0.78F);
            }
            case INTRO_COMPLETE -> 1.0F;
        };
    }

    public static float computeShakeIntensity(LastGiftCollector boss) {
        LastGiftCollector.BossState state = boss.getBossState();
        int stateTicks = boss.getStateTicks();
        int introTicks = boss.getIntroTicks();

        if (state == LastGiftCollector.BossState.INTRO_CINEMATIC) {
            LastGiftCollector.IntroStage stage = boss.getIntroStage();
            if (stage == LastGiftCollector.IntroStage.INTRO_LIGHTNING && introTicks <= 32) {
                return 0.95F;
            }
            if (stage == LastGiftCollector.IntroStage.INTRO_LANDING) {
                int sinceLand = Math.max(0, introTicks - LastGiftCollector.INTRO_LANDING_TICK);
                return Math.max(0.0F, 2.4F * (1.0F - (sinceLand / 22.0F)));
            }
            if (stage == LastGiftCollector.IntroStage.INTRO_REVEAL && introTicks >= 158 && introTicks <= 172) {
                return 0.75F;
            }
            return 0.0F;
        }

        if (state == LastGiftCollector.BossState.PHASE_TRANSITION) {
            if (stateTicks >= 22 && stateTicks <= 46) {
                return 1.35F;
            }
            return 0.45F;
        }

        if (state == LastGiftCollector.BossState.ATTACK_EXECUTION) {
            LastGiftCollector.AttackType attack = boss.getActiveAttack();
            if (attack == LastGiftCollector.AttackType.HOLLOW_BELL && stateTicks <= 6) {
                return 1.25F;
            }
            if (attack == LastGiftCollector.AttackType.SOLSTICE_JUMP && stateTicks >= 18) {
                return 1.85F;
            }
        }

        if (state == LastGiftCollector.BossState.DEATH_CINEMATIC && stateTicks >= 55 && stateTicks <= 66) {
            return 1.15F;
        }

        return 0.0F;
    }

    private static @Nullable LastGiftCollector findActiveIntroBoss(Level level, Vec3 center) {
        AABB box = new AABB(center, center).inflate(CINEMATIC_RADIUS);
        List<LastGiftCollector> list = level.getEntitiesOfClass(
                LastGiftCollector.class,
                box,
                e -> e.isAlive() && e.getBossState() == LastGiftCollector.BossState.INTRO_CINEMATIC
        );
        return list.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(center)))
                .orElse(null);
    }

    private static @Nullable LastGiftCollector findNearbyCollector(Level level, Vec3 center, double radius) {
        AABB box = new AABB(center, center).inflate(radius);
        List<LastGiftCollector> list = level.getEntitiesOfClass(
                LastGiftCollector.class,
                box,
                LastGiftCollector::isAlive
        );
        return list.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(center)))
                .orElse(null);
    }

    private static @Nullable CollectorSleigh findNearestSleigh(Level level, Vec3 center) {
        AABB box = new AABB(center, center).inflate(CINEMATIC_RADIUS * 1.5D);
        List<CollectorSleigh> list = level.getEntitiesOfClass(
                CollectorSleigh.class,
                box,
                CollectorSleigh::isAlive
        );
        return list.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(center)))
                .orElse(null);
    }
}
