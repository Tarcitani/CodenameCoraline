package com.codenamecoraline.entity.crimson;

/**
 * Every animation of the Crimson Commander (and of his vanguards, who wear the same model). The server picks the current
 * one (synched to clients) so the visual state always mirrors the gameplay state; the keyframes live in
 * {@code client/crimson/CrimsonCommanderAnimations}.
 *
 * <p>The remaining columns document the animation table in {@code docs/design/crimson_commander.md}: the state that
 * activates it, the gameplay event it carries, how it can be interrupted and where it goes next.</p>
 */
public enum CrimsonAnim {
    // ---- A. stance and movement
    COMBAT_IDLE(true, "GROUND, standing: the greatsword rests on the right shoulder", "none", "any action", "walk, attacks"),
    IDLE_LOOK(false, "GROUND, no target: scans the hall", "none", "any action", "combat_idle"),
    WALK(true, "GROUND, marching with the sword on the shoulder", "step sounds", "any action", "run, combat_idle"),
    RUN(true, "GROUND, chasing a far target, sword trailing low", "step sounds", "any action", "walk, attacks"),
    STEP_JUMP(false, "GROUND, jumping onto a ledge or out of a pit", "none", "landing", "walk"),
    // ---- B. crimson_slash
    SLASH_PREPARE(false, "ATTACK prepare: Crimson Slash (torso twists, the sword is drawn back)", "ruby glow at the visor", "target escapes (cancel)", "slash_execute"),
    SLASH_EXECUTE(false, "ATTACK execute: Crimson Slash (140 degree horizontal sweep)", "DAMAGE at the sweep tick, crimson arc", "none", "slash_recovery"),
    SLASH_RECOVERY(false, "ATTACK recover: Crimson Slash (open)", "none", "none", "combat_idle"),
    // ---- C. dash_thrust
    THRUST_PREPARE(false, "ATTACK prepare: Dash Thrust (leans in, points the sword)", "crimson trail on the ground along the vector", "target escapes (cancel)", "thrust_execute"),
    THRUST_EXECUTE(false, "ATTACK execute: Dash Thrust (fast advance up to 9 blocks)", "DAMAGE on contact, Slowness I", "wall stops the advance", "thrust_recovery, thrust_recovery_miss, slam_prepare (fury combo)"),
    THRUST_RECOVERY(false, "ATTACK recover: Dash Thrust after a hit", "none", "none", "combat_idle"),
    THRUST_RECOVERY_MISS(false, "ATTACK recover: Dash Thrust that missed (braking, long)", "none", "none", "combat_idle"),
    // ---- D. heavy_overhead_slam
    SLAM_PREPARE(false, "ATTACK prepare: Heavy Overhead Slam (two hands over the head, jump of 2 blocks)", "gold and crimson ring on the ground", "target escapes before the jump (cancel)", "slam_execute"),
    SLAM_EXECUTE(false, "ATTACK execute: Heavy Overhead Slam (the blade is driven into the ground)", "DAMAGE in two rings, shockwave, shields disabled", "none", "slam_recovery"),
    SLAM_RECOVERY(false, "ATTACK recover: Heavy Overhead Slam (pulls the sword out of the ground)", "none", "none", "combat_idle"),
    // ---- E. defensive_guard and riposte
    GUARD_PREPARE(false, "GUARD prepare: plants the legs, crosses the sword in front of the torso", "locked-guard sound", "none", "guard_hold"),
    GUARD_HOLD(true, "GUARD active (75% less frontal damage, projectiles deflected)", "gold sparks on every block", "a frontal melee hit (riposte)", "guard_recovery, riposte"),
    GUARD_BLOCK(false, "GUARD, a hit is blocked (overlay)", "gold sparks, shield sound", "none", "guard_hold"),
    GUARD_RECOVERY(false, "GUARD recover", "none", "none", "combat_idle"),
    RIPOSTE(false, "RIPOSTE: the guard breaks into an instant counter-cut", "DAMAGE in a 3.2 block cone, strong knock-back", "none", "combat_idle"),
    // ---- F. blood_crescent_wave
    WAVE_PREPARE(false, "ATTACK prepare: Blood Crescent Wave (the blade is charged with ruby and gold)", "spell sound, glowing blade", "target escapes (cancel)", "wave_execute"),
    WAVE_EXECUTE(false, "ATTACK execute: Blood Crescent Wave (rising cut that releases the wave)", "the wave travels 22 blocks", "none", "wave_recovery"),
    WAVE_RECOVERY(false, "ATTACK recover: Blood Crescent Wave", "none", "none", "combat_idle"),
    // ---- G. reactions
    HURT_FRONT(false, "damage from the front (overlay)", "hurt sound", "none", "previous animation"),
    HURT_BACK(false, "damage from behind (overlay)", "hurt sound", "none", "previous animation"),
    ATTACK_CANCEL(false, "an attack was cancelled because the target escaped", "none", "none", "combat_idle"),
    // ---- H. stages
    AWAKEN_TRANSITION(false, "TRANSITION to stage 2 (60% health): raises the sword, crimson pulse", "invulnerable, calls the vanguards", "death", "combat_idle"),
    FURY_TRANSITION(false, "TRANSITION to stage 3 (25% health): drives the sword down, the visor flares", "invulnerable", "death", "fury_idle"),
    HEAL_CHANNEL(true, "HEALING: crouched over the planted sword, a red aura rising around him", "he recovers 15% of his health over 3 s", "damage of 8% of his health breaks it", "combat_idle"),
    FURY_IDLE(true, "stage 3 standing, the sword held low and ready", "constant particles at the visor and blade", "any action", "attacks"),
    // ---- I. arrival and death
    INTRO_KNEEL(true, "INTRO: kneeling, both hands on the planted sword", "crimson motes", "none", "intro_rise"),
    INTRO_RISE(false, "INTRO: rises, frees the sword and salutes with it", "steel ring", "none", "combat_idle"),
    DEATH_KNEEL(false, "DYING: falls to one knee and plants the greatsword", "heavy armour fall", "none", "death_final"),
    DEATH_FINAL(true, "DYING / DEAD: head bowed over the planted sword", "his spirit rises", "none", "removed"),
    // ---- J. the vanguards
    VANGUARD_RISE(false, "a Crimson Ash Vanguard rises from the ash", "ash burst", "none", "combat_idle"),
    VANGUARD_CRUMBLE(false, "a vanguard falls apart into ash (death, expiry, master fallen)", "ash burst", "none", "removed");

    public final boolean loops;
    public final String state;
    public final String event;
    public final String interrupt;
    public final String next;

    CrimsonAnim(boolean loops, String state, String event, String interrupt, String next) {
        this.loops = loops;
        this.state = state;
        this.event = event;
        this.interrupt = interrupt;
        this.next = next;
    }

    public String id() {
        return "animation.crimson_commander." + this.name().toLowerCase();
    }

    public static CrimsonAnim byOrdinal(int ordinal) {
        CrimsonAnim[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, ordinal))];
    }
}
