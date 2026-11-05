package com.codenamecoraline.entity.ten_threads_witch.matriarch;

import com.codenamecoraline.entity.ten_threads_witch.EncounterPhase;
import net.minecraft.util.Mth;

/**
 * The three behavioural stages of the Matriarch. They follow one continuous value, {@code unravel} (0 composed, 1 lost), so
 * that intensities can grow smoothly inside a stage while the clips chosen change at the thresholds: puppets 1 to 5 keep her
 * composed, puppets 6 to 10 irritate her, the last phase unravels her further as she loses health. Server and client compute
 * it from the same synched facts (phase, stage, health), so both sides agree without another field.
 */
public enum MatriarchMood {
    /** Elegance and arrogance. */
    COMPOSED,
    /** Curiosity and irritation. */
    IRRITATED,
    /** Disturbance and loss of control. */
    UNRAVELLING;

    public static final float IRRITATED_FROM = 0.30F;
    public static final float UNRAVELLING_FROM = 0.60F;

    public static float unravel(EncounterPhase phase, int stage, float healthFraction) {
        return switch (phase) {
            case DORMANT, INTRO -> 0.0F;
            case STAGE_11_TRANSITION -> UNRAVELLING_FROM;
            case STAGE_11_FURY -> UNRAVELLING_FROM + (1.0F - UNRAVELLING_FROM) * Mth.clamp(1.0F - healthFraction, 0.0F, 1.0F);
            case DEATH_CINEMATIC -> 1.0F;
            default -> Mth.clamp((stage - 1) * 0.065F, 0.0F, 0.59F);
        };
    }

    public static MatriarchMood of(float unravel) {
        return unravel >= UNRAVELLING_FROM ? UNRAVELLING : unravel >= IRRITATED_FROM ? IRRITATED : COMPOSED;
    }

    /** 0 inside the composed stage, rising to 1 at full loss of control: the strength of unnatural motion. */
    public static float strangeness(float unravel) {
        return Mth.clamp((unravel - IRRITATED_FROM) / (1.0F - IRRITATED_FROM), 0.0F, 1.0F);
    }
}
