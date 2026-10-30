package com.codenamecoraline.entity.macaque;

/**
 * Every animation of the Bark Macaque. The server picks the current one (synched to clients) so the visual state always
 * mirrors the gameplay state; the keyframes live in {@code client/macaque/BarkMacaqueAnimations}.
 */
public enum MacaqueAnim {
    /** Standing: head turns, finger and tail movement. */
    IDLE(true),
    WALK(true),
    /** Fast flanking run on all fours. */
    RUN(true),
    /** Up a trunk or wall: body upright, hands and feet alternating. */
    CLIMB(true),
    /** Coiling before a leap. */
    LEAP_CROUCH(false),
    /** In flight: arms reaching, legs trailing, tail streaming. */
    LEAP_AIR(true),
    /** Absorbing the landing. */
    LAND(false),
    /** The bite at the end of a leap. */
    BITE(false),
    /** Springing away after the bite. */
    RETREAT(false),
    /** Short call to the boss, also the arrival cry. */
    SCREECH(false),
    HURT(false),
    DEATH(false);

    public final boolean loops;

    MacaqueAnim(boolean loops) {
        this.loops = loops;
    }

    private static final MacaqueAnim[] ALL = values();

    public static MacaqueAnim byOrdinal(int ordinal) {
        return ALL[Math.max(0, Math.min(ALL.length - 1, ordinal))];
    }
}
