package com.codenamecoraline.entity.architect;

/**
 * Every animation clip of the Architect (spec docs/design/architect.md §15). The server picks the current clip and
 * synchronizes it, so the picture always mirrors the gameplay state; the keyframes live in
 * {@code client/architect/ArchitectAnimations}. Tick lengths are the spec's and must match the attack timelines.
 */
public enum ArchitectAnim {
    // ---- presentation
    DORMANT_STATUE(true, 200),
    AWAKEN(false, 110),
    BATTLE_IDLE(true, 60),
    MANIACAL_LAUGH(false, 36),
    // ---- locomotion
    WALK(true, 24),
    RUN(true, 18),
    // ---- defence and damage
    GUARD_FULL(true, 20),
    STAGGER(false, 30),
    DEATH(false, 80),
    // ---- attacks (ticks = spec §13)
    SIXFOLD_EDICT(false, 38),
    CROSSED_MANDATE(false, 30),
    CARTENON_WHEEL(false, 44),
    SPLITTER_CRESCENT(false, 32),
    PIERCING_SENTENCE(false, 26),
    GARGOYLE_FALL(false, 56),
    PARRY_LEFT(false, 24),
    // ---- slice 3: the right parry, deflection, backstep, the out-of-combat idles and the phase enrage
    PARRY_RIGHT(false, 24),
    PROJECTILE_DEFLECT(false, 14),
    BACKSTEP(false, 16),
    IDLE_BLADE_INSPECT(false, 30),
    IDLE_WING_ADJUST(false, 24),
    PHASE_ENRAGE(false, 40),
    // ---- reform: footwork in combat (loops), the dodges (physical displacement, each with its own clip) and three new blows
    STRAFE_LEFT(true, 16),
    STRAFE_RIGHT(true, 16),
    BACKPEDAL(true, 16),
    DODGE_LEFT(false, 18),
    DODGE_RIGHT(false, 18),
    DODGE_BACK(false, 16),
    DODGE_DIAG_LEFT(false, 18),
    DODGE_DIAG_RIGHT(false, 18),
    LEAN_LEFT(false, 10),
    LEAN_RIGHT(false, 10),
    SHIELD_BREAKER(false, 44),
    FALSE_VERDICT(false, 40),
    RUSHING_REBUKE(false, 42);

    public final boolean loops;
    public final int ticks;

    ArchitectAnim(boolean loops, int ticks) {
        this.loops = loops;
        this.ticks = ticks;
    }

    public static ArchitectAnim byOrdinal(int ordinal) {
        ArchitectAnim[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : DORMANT_STATUE;
    }
}
