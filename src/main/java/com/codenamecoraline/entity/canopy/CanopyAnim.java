package com.codenamecoraline.entity.canopy;

/**
 * Every animation of the Canopy Breaker. The server picks the current one (synched to clients) so the visual state
 * always mirrors the gameplay state; the keyframes live in {@code client/canopy/CanopyBreakerAnimations}.
 *
 * <p>The remaining columns document the Animation Bible: what state activates it, which gameplay event it carries,
 * how it can be interrupted and where it goes next. {@code docs/design/canopy_breaker.md} is generated from them.</p>
 */
public enum CanopyAnim {
    // ---- A. personality / idle
    IDLE_BREATHING(true, "GROUND, standing still", "none", "any action", "idle_watch, walk"),
    IDLE_WATCH(true, "GROUND, standing, player in sight", "none", "any action", "idle_breathing"),
    IDLE_SNIFF(false, "GROUND, standing, random", "none", "any action", "idle_breathing"),
    IDLE_SCRATCH(false, "GROUND, standing, random", "none", "any action", "idle_breathing"),
    IDLE_CLUB_DRAG(false, "GROUND, club drawn, random", "scrape sound", "any action", "idle_breathing"),
    IDLE_ROAR_SHORT(false, "GROUND, standing, random, long idle", "short grunt sound", "any action", "idle_breathing"),
    // ---- B. ground locomotion
    WALK(true, "GROUND, moving slowly", "step sounds", "any action", "run_four_limb, stop_moving"),
    RUN_FOUR_LIMB(true, "GROUND, chasing far target", "step sounds", "any action", "run_bipedal, stop_moving"),
    RUN_BIPEDAL(true, "GROUND, short run before an attack or jump", "step sounds", "any action", "attack, jump_prepare"),
    START_MOVING(false, "GROUND, first ticks of movement", "none", "any action", "walk"),
    STOP_MOVING(false, "GROUND, speed drops to zero", "foot plant", "any action", "idle_breathing"),
    TURN_LEFT(false, "GROUND, large turn to the left", "none", "any action", "walk, idle"),
    TURN_RIGHT(false, "GROUND, large turn to the right", "none", "any action", "walk, idle"),
    STEP_UP(false, "GROUND, climbing a low obstacle", "none", "any action", "walk"),
    FALL(true, "AIRBORNE without a planned jump (lost support)", "none", "landing", "fall_recovery"),
    // ---- C. tree climbing
    CLIMB_APPROACH(true, "CLIMB_APPROACH, running to the trunk", "step sounds", "target lost, hurt stagger", "climb_grab"),
    CLIMB_GRAB(false, "CLIMB_APPROACH reached the trunk", "hands catch the bark (wood hit)", "support lost", "climb_up"),
    CLIMB_UP(true, "CLIMBING_UP", "bark scrapes (wood hit)", "support lost, hurt stagger", "cling_idle, branch_launch"),
    CLIMB_DOWN(true, "CLIMBING_DOWN", "bark scrapes", "support lost, hurt stagger", "cling_idle, walk"),
    CLING_IDLE(true, "CLINGING", "none", "support lost, route change", "cling_reposition, climb_up"),
    CLING_REPOSITION(false, "CLINGING, adjusting grip", "wood hit", "support lost", "cling_idle"),
    BRANCH_TRAVERSE_LEFT(true, "BRANCH_TRAVERSE, moving left along the canopy", "leaf steps", "support lost", "branch_balance"),
    BRANCH_TRAVERSE_RIGHT(true, "BRANCH_TRAVERSE, moving right along the canopy", "leaf steps", "support lost", "branch_balance"),
    BRANCH_BALANCE(true, "BRANCH_TRAVERSE, standing on the canopy", "none", "jump plan", "branch_crouch"),
    BRANCH_CROUCH(false, "JUMP_PREPARATION on the canopy", "wood creak", "destination invalid", "branch_launch, jump_abort"),
    BRANCH_LAUNCH(false, "JUMP_PREPARATION to AIRBORNE", "leaf burst, jump sound", "none", "jump_airborne"),
    BRANCH_GRAB(false, "LANDING on a trunk or branch", "wood hit", "none", "land_recovery"),
    CLIMB_FAILED(false, "support lost while climbing", "scream", "landing", "fall, fall_recovery"),
    // ---- D. jumps and landings
    JUMP_PREPARE(false, "JUMP_PREPARATION on the ground", "grunt", "destination invalid", "jump_takeoff, jump_abort"),
    JUMP_TAKEOFF(false, "start of the ballistic trajectory", "jump sound, leaf burst", "none", "jump_airborne"),
    JUMP_AIRBORNE(true, "AIRBORNE (traversal jump)", "none", "landing", "jump_land_light, jump_land_heavy"),
    JUMP_STEER(true, "AIRBORNE, trajectory corrected", "none", "landing", "jump_land_*"),
    JUMP_LAND_LIGHT(false, "LANDING on a canopy or ledge", "soft landing, leaves", "none", "land_recovery"),
    JUMP_LAND_HEAVY(false, "LANDING on the ground", "thud, dust and leaves", "none", "land_recovery"),
    LAND_RECOVERY(false, "LANDING_RECOVERY", "pant", "stagger", "idle, attack"),
    JUMP_ABORT(false, "JUMP_PREPARATION cancelled", "none", "none", "walk, idle"),
    // ---- E. club
    DRAW_CLUB_PREPARE(false, "DRAW_CLUB step 1", "none", "hurt stagger", "draw_club"),
    DRAW_CLUB(false, "DRAW_CLUB step 2: the club swaps from the back to the hand", "wood hit", "hurt stagger", "draw_club_finish"),
    DRAW_CLUB_FINISH(false, "DRAW_CLUB step 3", "none", "none", "club_guard_idle"),
    CLUB_GUARD_IDLE(true, "GROUND, club drawn, standing", "none", "any action", "club_guard_walk"),
    CLUB_GUARD_WALK(true, "GROUND, club drawn, moving", "step sounds", "any action", "club_guard_idle"),
    CLUB_DRAG(true, "GROUND, club drawn, dragging the club", "scrape", "any action", "club_guard_idle"),
    CLUB_RECOVER_GRIP(false, "after a heavy swing", "none", "any action", "club_guard_idle"),
    CLUB_STOW(false, "optional: club back to the back", "wood hit", "none", "idle_breathing"),
    // ---- F. attacks
    ATTACK_TRUNK_SLAM_PREPARE(false, "ATTACK prepare: Trunk Slam", "roar_attack", "target lost (cancel)", "attack_trunk_slam_execute"),
    ATTACK_TRUNK_SLAM_PREPARE_ALT(false, "ATTACK prepare: Trunk Slam, stepping variant", "roar_attack", "target lost (cancel)", "attack_trunk_slam_execute"),
    ATTACK_TRUNK_SLAM_EXECUTE(false, "ATTACK execute: Trunk Slam", "DAMAGE at impact tick", "hurt does not cancel", "attack_trunk_slam_recovery"),
    ATTACK_TRUNK_SLAM_RECOVERY(false, "ATTACK recover: Trunk Slam", "none", "none", "club_recover_grip"),
    ATTACK_CANOPY_SWEEP_PREPARE_LEFT(false, "ATTACK prepare: Canopy Sweep (arc to the left)", "swing sound", "target lost (cancel)", "attack_canopy_sweep_execute_left"),
    ATTACK_CANOPY_SWEEP_PREPARE_RIGHT(false, "ATTACK prepare: Canopy Sweep (arc to the right)", "swing sound", "target lost (cancel)", "attack_canopy_sweep_execute_right"),
    ATTACK_CANOPY_SWEEP_EXECUTE_LEFT(false, "ATTACK execute: Canopy Sweep left", "DAMAGE at mid-arc tick", "hurt does not cancel", "attack_canopy_sweep_recovery"),
    ATTACK_CANOPY_SWEEP_EXECUTE_RIGHT(false, "ATTACK execute: Canopy Sweep right", "DAMAGE at mid-arc tick", "hurt does not cancel", "attack_canopy_sweep_recovery"),
    ATTACK_CANOPY_SWEEP_RECOVERY(false, "ATTACK recover: Canopy Sweep", "none", "none", "club_recover_grip"),
    ATTACK_BRUTE_LUNGE_PREPARE(false, "ATTACK prepare: Brute Lunge", "roar_attack", "target lost (cancel)", "attack_brute_lunge_execute"),
    ATTACK_BRUTE_LUNGE_EXECUTE(false, "ATTACK execute: Brute Lunge (committed advance)", "DAMAGE at the end of the advance", "hurt does not cancel", "attack_brute_lunge_recovery"),
    ATTACK_BRUTE_LUNGE_RECOVERY(false, "ATTACK recover: Brute Lunge", "pant", "none", "club_recover_grip"),
    CANOPY_DROP_PREPARE(false, "JUMP_PREPARATION for the Canopy Drop (on the canopy)", "roar_attack, ground marker", "destination invalid (cancel)", "canopy_drop_airborne"),
    CANOPY_DROP_AIRBORNE(true, "AIRBORNE, Canopy Drop trajectory", "none", "landing", "canopy_drop_impact"),
    CANOPY_DROP_IMPACT(false, "LANDING from the Canopy Drop", "DAMAGE once, dust and leaves", "none", "canopy_drop_recovery"),
    CANOPY_DROP_RECOVERY(false, "LANDING_RECOVERY after the drop (vulnerable)", "pant", "stagger", "club_guard_idle"),
    ATTACK_BACKHAND_PREPARE(false, "ATTACK prepare: Backhand", "grunt", "target lost (cancel)", "attack_backhand_execute"),
    ATTACK_BACKHAND_EXECUTE(false, "ATTACK execute: Backhand", "DAMAGE at contact tick", "hurt does not cancel", "attack_backhand_recovery"),
    ATTACK_BACKHAND_RECOVERY(false, "ATTACK recover: Backhand", "none", "none", "club_guard_idle"),
    ATTACK_GRAB_PREPARE(false, "ATTACK prepare: Grab and Throw (crouches, free hand reaching)", "none (telegraphed, cannot be blocked)", "target leaves reach, hurt stagger", "attack_grab_lift"),
    ATTACK_GRAB_LIFT(false, "ATTACK execute: Grab and Throw (the hand closes and lifts the target overhead)", "grab roar, squeeze damage, the target is carried", "heavy hit staggers him and he lets go", "attack_grab_throw, attack_grab_recovery"),
    ATTACK_GRAB_THROW(false, "ATTACK execute: Grab and Throw (the target is hurled)", "throw sound, thrown damage and a long launch", "none", "attack_grab_recovery"),
    ATTACK_GRAB_RECOVERY(false, "ATTACK recover: Grab and Throw (panting, open)", "none", "heavy hit staggers him", "club_guard_idle"),
    ATTACK_POUND_PREPARE(false, "ATTACK prepare: Fist Pound (rears up, both fists and the club high)", "none (telegraphed)", "target leaves reach, hurt stagger", "attack_pound_execute"),
    ATTACK_POUND_EXECUTE(false, "ATTACK execute: Fist Pound (both fists drive into the ground)", "heavy thud, shockwave, everyone around is thrown up", "none", "attack_pound_recovery"),
    ATTACK_POUND_RECOVERY(false, "ATTACK recover: Fist Pound (leaning on his fists, open)", "none", "heavy hit staggers him", "club_guard_idle"),
    // ---- G. roars
    ROAR_IDLE(false, "GROUND, distant short roar", "roar sound", "any action", "idle_breathing"),
    ROAR_AGGRO(false, "target sighted / intro", "roar sound (cooldown)", "death", "club_guard_idle"),
    INTRO_CHEST_BEAT(false, "INTRO: upright, beating his chest like a gorilla", "a chest-beat drum on every blow", "none (invulnerable)", "roar_aggro"),
    ROAR_ATTACK(false, "start of a heavy attack", "roar sound (cooldown)", "death", "attack prepare"),
    ROAR_PHASE_TWO(false, "stage 2 transition", "roar sound", "death", "club_guard_idle"),
    ROAR_HURT(false, "heavy damage taken", "roar sound (cooldown)", "death", "club_guard_idle"),
    ROAR_STAGGER(false, "STAGGER", "roar sound", "death", "stumble"),
    ROAR_DEATH(false, "DYING, first beat", "roar sound", "none", "death_fall"),
    PANT_RECOVERY(true, "LAND_RECOVERY or ATTACK recovery, tired", "pant", "any action", "idle"),
    INTIMIDATE_ROAR(false, "ATTACK: Intimidating Roar", "roar sound, knock-back of nothing", "death", "club_guard_idle"),
    // ---- H. reactions
    HURT_FRONT(false, "damage from the front (overlay)", "hurt sound", "none", "previous animation"),
    HURT_SIDE(false, "damage from the side (overlay)", "hurt sound", "none", "previous animation"),
    HURT_BACK(false, "damage from behind (overlay)", "hurt sound", "none", "previous animation"),
    STAGGER(false, "STAGGER: heavy hit during recovery or after a failed jump", "grunt", "death", "stumble, knockback_recover"),
    STUMBLE(false, "after stagger", "none", "death", "knockback_recover"),
    KNOCKBACK_RECOVER(false, "after strong knock-back", "none", "death", "club_guard_idle"),
    ATTACK_INTERRUPTED(false, "an attack was cancelled (target left)", "none", "death", "club_guard_idle"),
    TARGET_LOST(false, "no target for a while", "sniff", "any action", "idle_breathing"),
    TAUNT(false, "pressure beat between attacks, stage 2+", "roar sound (cooldown)", "any action", "club_guard_idle"),
    // ---- I. stages and death
    PHASE_TWO_TRANSITION(false, "TRANSITION to stage 2 (60% health)", "roar, invulnerable", "death", "club_guard_idle"),
    FURY_TRANSITION(false, "TRANSITION to stage 3 (25% health)", "roar, club slam, invulnerable", "death", "fury_idle"),
    FURY_IDLE(true, "stage 3 standing", "none", "any action", "fury movement"),
    DEATH_PREPARE(false, "DYING 1", "none", "none", "death_fall"),
    DEATH_FALL(false, "DYING 2", "none", "none", "death_impact"),
    DEATH_IMPACT(false, "DYING 3", "heavy thud, leaves", "none", "death_final"),
    DEATH_FINAL(false, "DYING 4 / DEAD", "none", "none", "removed");

    public final boolean loops;
    public final String state;
    public final String event;
    public final String interrupt;
    public final String next;

    CanopyAnim(boolean loops, String state, String event, String interrupt, String next) {
        this.loops = loops;
        this.state = state;
        this.event = event;
        this.interrupt = interrupt;
        this.next = next;
    }

    public String id() {
        return "animation.canopy_breaker." + this.name().toLowerCase();
    }

    public static CanopyAnim byOrdinal(int ordinal) {
        CanopyAnim[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, ordinal))];
    }
}
