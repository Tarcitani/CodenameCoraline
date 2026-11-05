package com.codenamecoraline.entity.ten_threads_witch;

/** What a puppet is doing, synched to clients. */
public enum PuppetState {
    /** Hanging from the canopy by its silver thread: invulnerable, no AI, waiting for its turn. */
    SLEEPING_SUSPENDED,
    /** Pulled to the ground by the Matriarch: invulnerable for a moment. */
    AWAKENING,
    IDLE_COMBAT,
    /** The wind-up of a normal attack. */
    WINDUP,
    /** The wind-up of a special attack: the thread is crimson and can be cut. */
    WINDUP_CRIMSON,
    EXECUTING,
    RECOVERY,
    /** Entangled in its own threads: motionless and taking 1.75 times the damage. */
    TANGLED_STUNNED,
    DYING
}
