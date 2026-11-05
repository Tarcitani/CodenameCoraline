package com.codenamecoraline.entity.ten_threads_witch.matriarch;

/**
 * The animation layers of the Matriarch. The server drives the synched ones (one clip each, replaced by priority); the
 * client derives the others from what it sees (her motion, her mood). A layer is not a body region: which joints a clip
 * moves comes from its own tracks, and conflicts are settled per joint by priority (docs/design/matriarch_animation.md).
 */
public enum MatSlot {
    /** Whole-body actions: idle variants, taunts, attacks, defences, stuns, falls, stage transitions, cinematics, death. */
    BODY(true),
    /** Hit reactions, added on top of whatever else plays (a blow never erases the rest of the pose). */
    REACT(true),
    /** Thread work of the left hand (or of both hands, for two-handed gestures). */
    HAND_L(true),
    /** Thread work of the right hand, the one that holds the staff. */
    HAND_R(true),
    /** The face: brows, mouth, eyes. */
    FACE(true),
    /** Client only: the breathing loop of her mood. */
    BASE(false),
    /** Client only: walking, running, floating and their transitions, chosen from her actual motion. */
    GAIT(false),
    /** Client only: procedural layers (gaze, fingers, cloth and hair); listed for the catalogue. */
    PROCEDURAL(false);

    public final boolean synched;

    MatSlot(boolean synched) {
        this.synched = synched;
    }

    /** The synched slots, in the order of their data accessors. */
    public static final MatSlot[] SYNCHED = {BODY, REACT, HAND_L, HAND_R, FACE};
}
