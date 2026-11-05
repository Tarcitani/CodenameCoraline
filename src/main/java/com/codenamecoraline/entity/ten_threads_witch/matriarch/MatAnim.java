package com.codenamecoraline.entity.ten_threads_witch.matriarch;

import static com.codenamecoraline.entity.ten_threads_witch.matriarch.MatEvent.*;
import static com.codenamecoraline.entity.ten_threads_witch.matriarch.MatSlot.*;

import java.util.List;

/**
 * The catalogue of the Matriarch's animations: one entry per planning ID of docs/design/matriarch_animation.md, with the
 * facts both sides need. The server uses the slot, priority, length, flags and events to decide what plays and to fire the
 * synchronisation points; the client uses the same entry to find the keyframes ({@code client.ten_threads_witch.matriarch})
 * and the blend times. Lengths are in ticks.
 *
 * <p>Priority follows the order of the specification (1 = death, the strongest; 10 = secondary motion and expressions).
 * It is applied per joint when poses are composed, never as "the newest wins".
 */
public enum MatAnim {
    // ---------------------------------------------------------------- 6.1 idle and independent behaviour
    IDLE_01_COMPOSED("MAT-IDLE-01a", BASE, 10, 64, Flags.LOOP, 1.0F),
    IDLE_01_IRRITATED("MAT-IDLE-01b", BASE, 10, 48, Flags.LOOP, 1.0F),
    IDLE_01_UNRAVELLING("MAT-IDLE-01c", BASE, 10, 56, Flags.LOOP | Flags.MOOD, 1.0F),
    IDLE_02("MAT-IDLE-02", BODY, 10, 70, Flags.INT, 0.6F),
    IDLE_03("MAT-IDLE-03", BODY, 10, 84, Flags.INT | Flags.MOOD, 0.5F),
    IDLE_04("MAT-IDLE-04", PROCEDURAL, 10, 0, 0, 1.0F),
    IDLE_05("MAT-IDLE-05", BODY, 10, 44, Flags.INT, 1.0F),
    IDLE_06("MAT-IDLE-06", BODY, 10, 60, Flags.INT, 0.2F, at(18, THREAD_TENSION)),
    IDLE_07("MAT-IDLE-07", PROCEDURAL, 10, 0, 0, 1.0F),
    IDLE_08_ONE("MAT-IDLE-08a", HAND_L, 9, 40, Flags.MOOD, 1.0F),
    IDLE_08_TWO("MAT-IDLE-08b", HAND_L, 9, 48, Flags.MOOD | Flags.BOTH, 1.0F),
    IDLE_09("MAT-IDLE-09", BODY, 10, 56, Flags.INT, 0.7F),
    IDLE_10_ELEGANT("MAT-IDLE-10a", BODY, 10, 50, Flags.INT, 0.8F),
    IDLE_10_CLAP("MAT-IDLE-10b", BODY, 10, 92, Flags.INT | Flags.HL | Flags.HR, 1.0F, at(26, CLAP), at(46, CLAP), at(66, CLAP)),
    IDLE_10_MIMIC_CROUCH("MAT-IDLE-10c1", BODY, 10, 56, Flags.INT, 0.6F, at(30, CACKLE)),
    IDLE_10_MIMIC_JUMP("MAT-IDLE-10c2", BODY, 10, 44, Flags.INT, 0.6F, at(26, CACKLE)),
    IDLE_10_MIMIC_SHIELD("MAT-IDLE-10c3", BODY, 10, 50, Flags.INT | Flags.HL | Flags.HR, 0.5F, at(30, CACKLE)),
    IDLE_10_MIMIC_DRINK("MAT-IDLE-10c4", BODY, 10, 56, Flags.INT | Flags.HL, 0.4F, at(36, CACKLE)),
    IDLE_10_MIMIC_SWING("MAT-IDLE-10c5", BODY, 10, 40, Flags.INT | Flags.HL, 0.7F, at(14, ATTACK_WHOOSH), at(26, CACKLE)),

    // ---------------------------------------------------------------- 6.2 locomotion (client: chosen from her motion)
    MOVE_01("MAT-MOVE-01", GAIT, 8, 20, Flags.LOOP, 1.0F),
    MOVE_02("MAT-MOVE-02", GAIT, 8, 32, Flags.LOOP, 1.0F),
    MOVE_03("MAT-MOVE-03", GAIT, 8, 16, Flags.LOOP, 1.0F),
    MOVE_04("MAT-MOVE-04", GAIT, 8, 14, 0, 1.0F),
    MOVE_05("MAT-MOVE-05", GAIT, 8, 14, 0, 1.0F),
    MOVE_06("MAT-MOVE-06", GAIT, 8, 12, Flags.LOOP, 1.0F),
    MOVE_07("MAT-MOVE-07", GAIT, 8, 24, Flags.LOOP, 1.0F),
    MOVE_08("MAT-MOVE-08", GAIT, 8, 20, Flags.LOOP, 1.0F),
    MOVE_09("MAT-MOVE-09", GAIT, 8, 12, 0, 1.0F),
    MOVE_10("MAT-MOVE-10", GAIT, 8, 16, 0, 1.0F),

    // ---------------------------------------------------------------- 6.3 floating (client, except the dash)
    FLOAT_01("MAT-FLOAT-01", GAIT, 8, 24, 0, 1.0F),
    FLOAT_02("MAT-FLOAT-02", GAIT, 8, 60, Flags.LOOP, 1.0F),
    FLOAT_03("MAT-FLOAT-03", GAIT, 8, 40, Flags.LOOP, 1.0F),
    FLOAT_04("MAT-FLOAT-04", GAIT, 8, 48, Flags.LOOP | Flags.MOOD, 1.0F),
    FLOAT_05("MAT-FLOAT-05", BODY, 7, 38, Flags.HL, 0.4F, at(12, DODGE), at(22, ATTACK_WHOOSH)),
    FLOAT_06("MAT-FLOAT-06", GAIT, 8, 30, Flags.LOOP, 1.0F),
    FLOAT_07("MAT-FLOAT-07", GAIT, 8, 30, Flags.LOOP, 1.0F),
    FLOAT_08("MAT-FLOAT-08", GAIT, 8, 80, Flags.LOOP, 1.0F),
    FLOAT_09("MAT-FLOAT-09", GAIT, 8, 20, 0, 1.0F),

    // ---------------------------------------------------------------- 6.4 thread manipulation (hand slots)
    THREAD_01("MAT-THREAD-01", HAND_L, 9, 30, Flags.BOTH, 1.0F, at(12, THREAD_MANIFEST)),
    THREAD_02("MAT-THREAD-02", HAND_L, 9, 16, 0, 1.0F),
    THREAD_03("MAT-THREAD-03", HAND_L, 9, 20, 0, 1.0F),
    THREAD_04("MAT-THREAD-04", HAND_L, 9, 24, Flags.BOTH | Flags.HOLD, 1.0F),
    THREAD_05("MAT-THREAD-05", HAND_L, 9, 20, Flags.BOTH | Flags.HOLD, 1.0F),
    THREAD_06("MAT-THREAD-06", HAND_L, 9, 24, Flags.BOTH | Flags.HOLD, 0.8F, at(16, THREAD_TENSION)),
    THREAD_07("MAT-THREAD-07", HAND_L, 9, 30, Flags.BOTH, 1.0F, at(10, THREAD_FORM), at(20, THREAD_FORM)),
    THREAD_08("MAT-THREAD-08", HAND_L, 9, 20, Flags.HOLD, 1.0F, at(10, THREAD_TENSION)),
    THREAD_09("MAT-THREAD-09", HAND_L, 9, 48, Flags.LOOP | Flags.MOOD, 1.0F),
    THREAD_10("MAT-THREAD-10", HAND_R, 9, 64, Flags.LOOP | Flags.MOOD, 1.0F),
    THREAD_11("MAT-THREAD-11", HAND_L, 9, 40, Flags.LOOP | Flags.BOTH | Flags.MOOD, 1.0F),
    THREAD_12("MAT-THREAD-12", HAND_L, 9, 32, Flags.LOOP | Flags.MOOD, 1.0F),
    THREAD_13("MAT-THREAD-13", HAND_L, 9, 14, 0, 1.0F),
    THREAD_14("MAT-THREAD-14", HAND_L, 9, 18, 0, 1.0F, at(8, THREAD_TENSION)),
    THREAD_15("MAT-THREAD-15", HAND_L, 9, 20, 0, 1.0F, at(8, THREAD_RELEASE)),
    THREAD_16("MAT-THREAD-16", HAND_L, 9, 16, 0, 1.0F),
    THREAD_17("MAT-THREAD-17", HAND_L, 9, 28, Flags.BOTH, 1.0F, at(14, THREAD_FORM)),
    THREAD_18("MAT-THREAD-18", HAND_L, 9, 14, 0, 1.0F, at(6, THREAD_RELEASE)),
    THREAD_19("MAT-THREAD-19", HAND_L, 9, 24, 0, 1.0F, at(16, THREAD_FORM)),
    THREAD_20("MAT-THREAD-20", HAND_L, 9, 18, 0, 0.6F, at(4, THREAD_SNAP)),
    THREAD_21("MAT-THREAD-21", HAND_L, 7, 16, Flags.HOLD | Flags.MOOD, 1.0F, at(8, THREAD_TENSION)),
    THREAD_22("MAT-THREAD-22", HAND_L, 7, 14, Flags.MOOD, 1.0F, at(5, ATTACK_RELEASE)),
    THREAD_23("MAT-THREAD-23", HAND_L, 7, 16, Flags.MOOD, 1.0F, at(6, ATTACK_WHOOSH), at(8, ATTACK_RELEASE)),
    THREAD_24("MAT-THREAD-24", HAND_L, 7, 12, Flags.MOOD, 1.0F, at(4, ATTACK_RELEASE)),
    THREAD_25("MAT-THREAD-25", HAND_L, 7, 24, Flags.BOTH | Flags.MOOD, 1.0F, at(6, THREAD_FORM), at(16, THREAD_TENSION)),
    THREAD_26("MAT-THREAD-26", HAND_L, 7, 20, Flags.MOOD, 0.8F, at(9, THREAD_TENSION)),
    THREAD_27("MAT-THREAD-27", HAND_L, 7, 14, 0, 1.0F, at(4, THREAD_RELEASE)),
    THREAD_28("MAT-THREAD-28", HAND_L, 7, 30, Flags.BOTH | Flags.MOOD, 1.0F, at(8, ATTACK_RELEASE), at(15, ATTACK_RELEASE), at(22, ATTACK_RELEASE)),

    // ---------------------------------------------------------------- 6.5 preparations and body attacks
    ATK_01("MAT-ATK-01", BODY, 7, 8, Flags.HOLD | Flags.HL, 0.9F),
    ATK_02("MAT-ATK-02", BODY, 7, 22, Flags.HOLD | Flags.HL | Flags.HR, 0.7F, at(12, THREAD_TENSION)),
    ATK_03("MAT-ATK-03", BODY, 7, 30, Flags.HOLD | Flags.HL | Flags.HR | Flags.MOOD, 0.5F, at(6, THREAD_MANIFEST), at(22, PHASE_SURGE)),
    ATK_04("MAT-ATK-04", BODY, 7, 16, Flags.HOLD | Flags.HL | Flags.HR, 0.9F),
    ATK_05("MAT-ATK-05", BODY, 7, 12, 0, 1.0F, at(3, THREAD_RELEASE)),
    ATK_06("MAT-ATK-06", BODY, 7, 14, Flags.HL | Flags.MOOD, 0.8F, at(2, ATTACK_WHOOSH)),
    ATK_07("MAT-ATK-07", BODY, 7, 26, Flags.HL | Flags.HR | Flags.MOOD, 0.4F, at(3, ATTACK_WHOOSH), at(6, IMPACT)),
    ATK_08("MAT-ATK-08", BODY, 7, 18, Flags.HL | Flags.MOOD, 0.8F, at(3, ATTACK_WHOOSH)),
    ATK_09("MAT-ATK-09", BODY, 7, 26, Flags.HL | Flags.HR | Flags.MOOD, 0.6F, at(2, ATTACK_WHOOSH), at(8, ATTACK_WHOOSH)),
    ATK_10("MAT-ATK-10", BODY, 7, 24, Flags.HL | Flags.HR | Flags.MOOD, 0.3F, at(4, ATTACK_WHOOSH), at(6, CACKLE)),
    ATK_11("MAT-ATK-11", BODY, 7, 20, Flags.HL | Flags.MOOD, 0.9F, at(4, ATTACK_RELEASE)),
    ATK_12("MAT-ATK-12", BODY, 7, 80, Flags.HL | Flags.HR | Flags.MOOD, 0.2F, at(2, POWER_BURST), at(30, POWER_BURST), at(58, POWER_BURST)),
    ATK_13("MAT-ATK-13", BODY, 7, 28, Flags.HL | Flags.HR | Flags.MOOD, 0.5F, at(4, ATTACK_WHOOSH), at(6, IMPACT)),

    // ---------------------------------------------------------------- 6.7 defence and dodges
    DEF_01("MAT-DEF-01", BODY, 7, 18, Flags.HL | Flags.HR, 0.6F, at(4, BLOCK)),
    DEF_02("MAT-DEF-02", BODY, 7, 12, Flags.HL, 0.9F, at(3, BLOCK)),
    DEF_03("MAT-DEF-03", BODY, 7, 22, Flags.HL | Flags.HR, 0.8F, at(5, BARRIER)),
    DEF_04("MAT-DEF-04", BODY, 7, 26, Flags.HL, 0.9F, at(8, PUPPET_GUARD)),
    DEF_05("MAT-DEF-05", BODY, 7, 14, 0, 0.9F, at(2, DODGE)),
    DEF_06("MAT-DEF-06", BODY, 7, 16, 0, 0.9F, at(2, DODGE)),
    DEF_07("MAT-DEF-07", BODY, 7, 18, Flags.MOOD, 0.6F, at(3, DODGE)),
    DEF_08("MAT-DEF-08", BODY, 7, 18, 0, 1.0F, at(2, DODGE)),
    DEF_09("MAT-DEF-09", BODY, 7, 14, 0, 1.0F),

    // ---------------------------------------------------------------- 6.8 hit reactions (added on top)
    HIT_01("MAT-HIT-01", REACT, 6, 8, Flags.ADD, 1.0F),
    HIT_02("MAT-HIT-02", REACT, 6, 12, Flags.ADD, 0.8F),
    HIT_03("MAT-HIT-03", REACT, 6, 20, Flags.ADD, 0.4F),
    HIT_04("MAT-HIT-04", REACT, 6, 14, Flags.ADD, 0.6F),
    HIT_05("MAT-HIT-05", REACT, 6, 10, Flags.ADD, 0.9F),
    HIT_06("MAT-HIT-06", REACT, 6, 8, Flags.ADD, 0.9F),
    HIT_07("MAT-HIT-07", REACT, 6, 18, Flags.ADD, 0.6F),
    HIT_08("MAT-HIT-08", REACT, 6, 26, Flags.ADD | Flags.MOOD, 0.7F),

    // ---------------------------------------------------------------- 6.9 stun
    STUN_01("MAT-STUN-01", BODY, 5, 10, 0, 0.5F),
    STUN_02("MAT-STUN-02", BODY, 5, 18, 0, 0.3F),
    STUN_03("MAT-STUN-03", BODY, 5, 24, Flags.MOOD, 0.2F),
    STUN_04("MAT-STUN-04", BODY, 5, 40, Flags.LOOP | Flags.MOOD, 0.1F),
    STUN_05("MAT-STUN-05", BODY, 5, 22, 0, 0.6F, at(10, THREAD_FORM)),

    // ---------------------------------------------------------------- 6.10 falls and recoveries
    FALL_01("MAT-FALL-01", BODY, 4, 16, Flags.LOCK, 0.0F, at(13, FALL_IMPACT)),
    FALL_02("MAT-FALL-02", BODY, 4, 16, Flags.LOCK, 0.0F, at(13, FALL_IMPACT)),
    FALL_03("MAT-FALL-03", BODY, 4, 22, Flags.LOCK | Flags.MOOD, 0.0F, at(18, FALL_IMPACT)),
    FALL_04("MAT-FALL-04", BODY, 4, 10, Flags.HOLD | Flags.LOCK, 0.0F),
    FALL_05("MAT-FALL-05", BODY, 4, 30, Flags.LOCK, 0.5F),
    FALL_06("MAT-FALL-06", BODY, 4, 32, Flags.LOCK | Flags.MOOD, 0.4F, at(16, CACKLE)),
    FALL_07("MAT-FALL-07", BODY, 4, 28, Flags.LOCK | Flags.MOOD, 0.2F),
    FALL_08("MAT-FALL-08", BODY, 4, 16, Flags.LOCK, 0.0F, at(10, FALL_IMPACT)),
    FALL_09("MAT-FALL-09", BODY, 4, 32, Flags.LOCK, 0.4F, at(6, THREAD_MANIFEST), at(14, THREAD_TENSION)),

    // ---------------------------------------------------------------- 6.11 expressions (face slot)
    EXP_01("MAT-EXP-01", FACE, 10, 60, Flags.LOOP, 1.0F),
    EXP_02("MAT-EXP-02", FACE, 10, 60, Flags.LOOP, 1.0F),
    EXP_03("MAT-EXP-03", FACE, 10, 48, Flags.LOOP, 1.0F),
    EXP_04("MAT-EXP-04", FACE, 10, 56, Flags.LOOP, 1.0F),
    EXP_05("MAT-EXP-05", FACE, 10, 50, Flags.LOOP, 1.0F),
    EXP_06("MAT-EXP-06", FACE, 10, 40, Flags.LOOP, 1.0F),
    EXP_07("MAT-EXP-07", FACE, 10, 30, Flags.LOOP, 1.0F),
    EXP_08("MAT-EXP-08", FACE, 10, 64, Flags.LOOP | Flags.MOOD, 1.0F),
    EXP_09("MAT-EXP-09", FACE, 10, 36, Flags.LOOP | Flags.MOOD, 1.0F),
    EXP_10("MAT-EXP-10", FACE, 10, 44, Flags.LOOP | Flags.MOOD, 1.0F),

    // ---------------------------------------------------------------- 6.12 cloth and hair (procedural modes)
    CLOTH_01("MAT-CLOTH-01", PROCEDURAL, 10, 0, 0, 1.0F),
    CLOTH_02("MAT-CLOTH-02", PROCEDURAL, 10, 0, 0, 1.0F),
    CLOTH_03("MAT-CLOTH-03", PROCEDURAL, 10, 0, 0, 1.0F),
    CLOTH_04("MAT-CLOTH-04", PROCEDURAL, 10, 0, 0, 1.0F),
    CLOTH_05("MAT-CLOTH-05", PROCEDURAL, 10, 0, 0, 1.0F),
    HAIR_01("MAT-HAIR-01", PROCEDURAL, 10, 0, 0, 1.0F),
    HAIR_02("MAT-HAIR-02", PROCEDURAL, 10, 0, 0, 1.0F),
    HAIR_03("MAT-HAIR-03", PROCEDURAL, 10, 0, 0, 1.0F),
    HAIR_04("MAT-HAIR-04", PROCEDURAL, 10, 0, 0, 1.0F),
    HAIR_05("MAT-HAIR-05", PROCEDURAL, 10, 0, 0, 1.0F),
    HAIR_06("MAT-HAIR-06", PROCEDURAL, 10, 0, 0, 1.0F),

    // ---------------------------------------------------------------- 6.13 stage transitions
    PHASE_01("MAT-PHASE-01", BODY, 3, 64, Flags.LOCK | Flags.HL | Flags.HR, 0.5F, at(20, THREAD_SNAP), at(38, PHASE_SURGE), at(50, THREAD_MANIFEST)),
    PHASE_02("MAT-PHASE-02", BODY, 3, 50, Flags.LOCK | Flags.HL | Flags.HR, 0.0F, at(4, THREAD_SNAP), at(14, FALL_IMPACT), at(30, PHASE_SURGE)),
    PHASE_03("MAT-PHASE-03", BODY, 3, 28, Flags.LOCK | Flags.HL | Flags.HR, 0.2F, at(6, THREAD_FORM)),
    PHASE_04("MAT-PHASE-04", BODY, 3, 30, Flags.LOCK | Flags.HL | Flags.HR, 0.5F, at(8, POWER_BURST), at(20, THREAD_MANIFEST)),
    PHASE_05("MAT-PHASE-05", BODY, 3, 26, Flags.LOCK | Flags.HL | Flags.HR, 0.6F, at(12, CACKLE)),

    // ---------------------------------------------------------------- 6.14 cinematics and 7 death
    CIN_01("MAT-CIN-01", BODY, 2, 60, Flags.LOCK | Flags.HL | Flags.HR, 0.0F, at(1, INTRO_THREADS), at(16, INTRO_REVEAL), at(36, INTRO_REVEAL)),
    CIN_02("MAT-CIN-02", BODY, 2, 50, Flags.LOCK | Flags.HL | Flags.HR, 0.7F, at(30, THREAD_MANIFEST)),
    CIN_03("MAT-CIN-03", BODY, 2, 44, Flags.LOCK | Flags.HL | Flags.HR, 0.6F, at(14, THREAD_FORM), at(24, POWER_BURST), at(30, THREAD_TENSION)),
    CIN_04("MAT-CIN-04", PROCEDURAL, 3, 0, 0, 0.0F),
    CIN_05("MAT-CIN-05", BODY, 1, 116, Flags.LOCK | Flags.HL | Flags.HR | Flags.HOLD, 0.0F,
            at(28, THREAD_SNAP), at(36, THREAD_SNAP), at(48, THREAD_RELEASE), at(56, THREAD_SNAP), at(64, THREAD_RELEASE),
            at(72, THREAD_VANISH), at(78, DISINTEGRATE), at(88, DISINTEGRATE), at(98, DISINTEGRATE), at(108, DISINTEGRATE)),
    CIN_06("MAT-CIN-06", BODY, 1, 28, Flags.LOCK | Flags.HL | Flags.HR | Flags.HOLD, 0.0F, at(6, DISINTEGRATE), at(16, DISINTEGRATE), at(26, VANISH));

    /** Behaviour flags of an entry. */
    public static final class Flags {
        /** Repeats until replaced. */
        public static final int LOOP = 1;
        /** Keeps its last frame until something replaces it (a held preparation, the ground pose of a fall). */
        public static final int HOLD = 2;
        /** Amplitude and rhythm change with her mood (wider, then hitched and unnatural). */
        public static final int MOOD = 4;
        /** Any other body clip may replace it (idles and taunts never block an action). */
        public static final int INT = 8;
        /** Claims the left arm: a hand-slot request for that arm waits until it ends. */
        public static final int HL = 16;
        /** Claims the right arm. */
        public static final int HR = 32;
        /** A hand-slot clip that moves both hands (it occupies both hand slots). */
        public static final int BOTH = 64;
        /** Only a strictly stronger priority replaces it (falls, transitions, cinematics, death). */
        public static final int LOCK = 128;
        /** Its tracks are added to the pose instead of replacing it (hit reactions). */
        public static final int ADD = 256;

        private Flags() {}
    }

    /** A synchronisation point: {@code event} fires at {@code tick} of the clip. */
    public record Ev(int tick, MatEvent event) {}

    private static Ev at(int tick, MatEvent event) {
        return new Ev(tick, event);
    }

    public final String code;
    public final MatSlot slot;
    public final int priority;
    public final int ticks;
    public final int flags;
    /** How much the gaze (MAT-IDLE-04/05/06) may still turn the head while this plays: 0 never, 1 freely. */
    public final float lookWeight;
    public final List<Ev> events;
    private MatAnim next;

    MatAnim(String code, MatSlot slot, int priority, int ticks, int flags, float lookWeight, Ev... events) {
        this.code = code;
        this.slot = slot;
        this.priority = priority;
        this.ticks = ticks;
        this.flags = flags;
        this.lookWeight = lookWeight;
        this.events = List.of(events);
    }

    static {
        // fixed exits: a fall always lands in its impact pose, the death's defeat always ends in the final vanishing
        FALL_01.next = FALL_04;
        FALL_02.next = FALL_04;
        FALL_03.next = FALL_04;
        FALL_08.next = FALL_04;
        CIN_05.next = CIN_06;
        CIN_01.next = CIN_02;
        CIN_02.next = CIN_03;
        STUN_02.next = STUN_05;
        STUN_03.next = STUN_05;
        DEF_05.next = DEF_09;
        DEF_06.next = DEF_09;
        DEF_07.next = DEF_09;
    }

    public boolean has(int flag) {
        return (this.flags & flag) != 0;
    }

    public boolean loops() {
        return this.has(Flags.LOOP);
    }

    /** The clip that always follows this one when it ends on its own, or null. */
    public MatAnim next() {
        return this.next;
    }

    public boolean synched() {
        return this.slot.synched;
    }

    /** Playback speed of a variant: bit 1 a quick version, bit 2 a slow one (falls and recoveries). */
    public static float speed(int variant) {
        return (variant & Variant.FAST) != 0 ? 1.35F : (variant & Variant.SLOW) != 0 ? 0.75F : 1.0F;
    }

    /** Length in ticks of this clip played as {@code variant}. */
    public int lengthTicks(int variant) {
        return Math.max(1, Math.round(this.ticks / speed(variant)));
    }

    /** The tick of the clip (as authored) reached after {@code age} ticks of playback of {@code variant}. */
    public static float clipTick(float age, int variant) {
        return age * speed(variant);
    }

    /** Variant bits shared by the server and the client. */
    public static final class Variant {
        /** The fall or recovery is face down (forward) instead of on the back. */
        public static final int PRONE = 1;
        public static final int FAST = 2;
        public static final int SLOW = 4;
        /** A one-hand clip played mirrored, by the right hand. */
        public static final int MIRROR = 8;

        private Variant() {}
    }

    public static MatAnim byOrdinal(int ordinal) {
        MatAnim[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : null;
    }
}
