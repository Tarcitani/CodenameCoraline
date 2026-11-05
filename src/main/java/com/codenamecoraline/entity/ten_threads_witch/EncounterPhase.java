package com.codenamecoraline.entity.ten_threads_witch;

/** The phases of the whole encounter, kept by the Matriarch (and synched, for her model and the HUD). */
public enum EncounterPhase {
    DORMANT, INTRO,
    STAGE_01, STAGE_02, STAGE_03, STAGE_04, STAGE_05, STAGE_06, STAGE_07, STAGE_08, STAGE_09, STAGE_10,
    STAGE_11_TRANSITION, STAGE_11_FURY, DEATH_CINEMATIC;

    /** The phase of puppet stage {@code stage} (1 to 10). */
    public static EncounterPhase ofStage(int stage) {
        return values()[2 + Math.max(1, Math.min(10, stage)) - 1];
    }

    /** True while one of the ten puppets is the fight (the Matriarch is untouchable). */
    public boolean isPuppetStage() {
        return this.ordinal() >= STAGE_01.ordinal() && this.ordinal() <= STAGE_10.ordinal();
    }

    public static EncounterPhase byOrdinal(int ordinal) {
        EncounterPhase[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, ordinal))];
    }
}
