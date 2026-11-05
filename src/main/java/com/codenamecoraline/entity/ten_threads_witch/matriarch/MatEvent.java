package com.codenamecoraline.entity.ten_threads_witch.matriarch;

/**
 * A synchronisation point of an animation: something that must be seen or heard at one exact moment of a clip (a thread
 * appearing, a blow landing, a fall touching the ground). The server fires each one once, at the tick written in the clip's
 * entry of {@link MatAnim}, and the particles and sounds it sends reach every client; the client never fires them itself,
 * so nothing is doubled in multiplayer and nothing repeats per frame. Effects live in {@link MatriarchEffects}.
 */
public enum MatEvent {
    THREAD_MANIFEST(Anchor.HANDS),
    THREAD_FORM(Anchor.FOCUS),
    THREAD_TENSION(Anchor.HAND_L),
    THREAD_RELEASE(Anchor.HAND_L),
    THREAD_SNAP(Anchor.HAND_L),
    THREAD_VANISH(Anchor.HANDS),
    ATTACK_WHOOSH(Anchor.FRONT),
    ATTACK_RELEASE(Anchor.FOCUS),
    IMPACT(Anchor.FRONT),
    BLOCK(Anchor.FRONT),
    BARRIER(Anchor.FRONT),
    DODGE(Anchor.FEET),
    FALL_IMPACT(Anchor.FEET),
    CLAP(Anchor.HANDS),
    CACKLE(Anchor.HEAD),
    PHASE_SURGE(Anchor.CHEST),
    INTRO_THREADS(Anchor.ARENA),
    INTRO_REVEAL(Anchor.CHEST),
    POWER_BURST(Anchor.ARENA),
    DISINTEGRATE(Anchor.CHEST),
    VANISH(Anchor.CHEST),
    PUPPET_GUARD(Anchor.FOCUS);

    /** Where on (or around) the Matriarch the effect appears. */
    public enum Anchor { HAND_L, HAND_R, HANDS, CHEST, HEAD, FEET, FRONT, FOCUS, ARENA }

    public final Anchor anchor;

    MatEvent(Anchor anchor) {
        this.anchor = anchor;
    }
}
