package com.codenamecoraline.entity.ten_threads_witch;

/** Every animation of a puppet (and of the Matriarch), chosen by the server and synched; the keyframes are per kind. */
public enum PuppetAnim {
    SLEEP(true), AWAKEN(false), IDLE(true), WALK(true), WINDUP(false), WINDUP_CRIMSON(true),
    ATTACK_A(false), ATTACK_B(false), ATTACK_C(false), RECOVER(false), TANGLED(true), DEATH(false), TAUNT(false);

    public final boolean loops;

    PuppetAnim(boolean loops) {
        this.loops = loops;
    }

    public static PuppetAnim byOrdinal(int ordinal) {
        PuppetAnim[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, ordinal))];
    }

    /** The execute animation of the {@code index}-th attack of a kind (the third and later share the last one). */
    public static PuppetAnim attack(int index) {
        return index <= 0 ? ATTACK_A : index == 1 ? ATTACK_B : ATTACK_C;
    }
}
