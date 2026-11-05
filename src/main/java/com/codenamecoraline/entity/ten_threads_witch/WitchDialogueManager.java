package com.codenamecoraline.entity.ten_threads_witch;

import com.codenamecoraline.registry.ModSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

/**
 * The Matriarch's lines. Every line is shown on the action bar of the players nearby (themed text, translated in
 * {@code lang/}) with a voice sound mapped to vanilla witch and illusioner events in {@code sounds.json} (no audio files).
 * A {@link VoiceHook} is the clean, optional place to plug a voice API in later: it is called after the line is shown and
 * does nothing by default.
 */
public final class WitchDialogueManager {
    /** Every line she speaks; the key is {@code dialogue.codenamecoraline.ten_threads_witch.<id>}. */
    public enum Line {
        INTRO("intro"), STAGE_1("stage_1"), STAGE_2("stage_2"), STAGE_3("stage_3"), STAGE_4("stage_4"), STAGE_5("stage_5"),
        STAGE_6("stage_6"), STAGE_7("stage_7"), STAGE_8("stage_8"), STAGE_9("stage_9"), STAGE_10("stage_10"),
        THREAD_CUT("thread_cut"), FURY("fury"), DEATH("death");

        public final String id;

        Line(String id) {
            this.id = id;
        }

        public String translationKey() {
            return "dialogue.codenamecoraline.ten_threads_witch." + this.id;
        }

        public static Line forStage(int stage) {
            return List.of(STAGE_1, STAGE_2, STAGE_3, STAGE_4, STAGE_5, STAGE_6, STAGE_7, STAGE_8, STAGE_9, STAGE_10)
                    .get(Math.max(1, Math.min(10, stage)) - 1);
        }
    }

    /** Optional voice backend: receives every line after it is shown. */
    @FunctionalInterface
    public interface VoiceHook {
        void speak(ServerLevel level, MatriarchWitchEntity witch, Line line, Component text);
    }

    public static final double HEARING_RADIUS = 48.0D;
    /** The cut-thread complaint is not repeated more often than this. */
    public static final int THREAD_CUT_GAP_TICKS = 60;

    private static VoiceHook voiceHook = (level, witch, line, text) -> { };

    private WitchDialogueManager() {}

    public static void setVoiceHook(VoiceHook hook) {
        voiceHook = hook == null ? (level, witch, line, text) -> { } : hook;
    }

    /** Says a line: action bar for the players in earshot, the voice sound, then the hook. Returns false if it was held back. */
    public static boolean say(ServerLevel level, MatriarchWitchEntity witch, Line line) {
        if (line == Line.THREAD_CUT && witch.getTicksSinceLastLine() < THREAD_CUT_GAP_TICKS) {
            return false;
        }
        Component text = Component.translatable(line.translationKey()).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(witch) <= HEARING_RADIUS * HEARING_RADIUS) {
                player.sendOverlayMessage(text);
            }
        }
        level.playSound(null, witch.blockPosition(), ModSounds.WITCH_VOICE.get(), SoundSource.HOSTILE, 1.8F,
                0.9F + level.getRandom().nextFloat() * 0.2F);
        witch.noteLine(line);
        voiceHook.speak(level, witch, line, text);
        return true;
    }
}
