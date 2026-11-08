package com.codenamecoraline.entity.architect;

import static com.codenamecoraline.entity.architect.ArchitectStrikes.Variant.ALWAYS;
import static com.codenamecoraline.entity.architect.ArchitectStrikes.Variant.NORMAL_ONLY;
import static com.codenamecoraline.entity.architect.ArchitectStrikes.Variant.VARIANT_ONLY;
import static com.codenamecoraline.entity.architect.ArchitectStrikes.arc;
import static com.codenamecoraline.entity.architect.ArchitectStrikes.corridor;
import static com.codenamecoraline.entity.architect.ArchitectStrikes.ring;

import com.codenamecoraline.entity.architect.ArchitectStrikes.Strike;
import java.util.List;

/**
 * The Architect's physical attacks with the spec's tick tables (docs/design/architect.md §13 and §17). Each attack has
 * the four phases of MOB_CONTRACT §3.2: windup (0 .. validation), validation (anti-whiff, the tick before the first
 * volume), active (the volumes) and recovery (from {@link #recoveryStart} to the clip's end). The clip and the volumes
 * share the same ticks. H and I depend on the arena and J is off in v1 (spec §12.2), so they are not here.
 */
public enum ArchitectAttack {
    /** A: six cuts in three heights; the variation is "the player is glued to him" (§13.A item 11). */
    SIXFOLD_EDICT(ArchitectAnim.SIXFOLD_EDICT, Architect.Phase.DUELIST, 40, 31, 4, 99.0F, List.of(
            arc(8, 9, 3.0D, -20.0D, 50.0D, 1.2D, 3.4D, 0.5F),
            arc(12, 13, 3.0D, -60.0D, 30.0D, 1.0D, 2.2D, 0.5F),
            arc(16, 17, 0.0D, 2.6D, -30.0D, 60.0D, 0.0D, 1.1D, 0.5F, NORMAL_ONLY),
            arc(17, 18, 0.0D, 2.6D, -70.0D, 70.0D, 0.0D, 1.1D, 0.5F, VARIANT_ONLY),
            arc(20, 21, 3.0D, -50.0D, 20.0D, 1.2D, 3.4D, 0.5F),
            arc(24, 25, 3.0D, -30.0D, 60.0D, 1.0D, 2.2D, 0.5F),
            arc(28, 29, 2.6D, -40.0D, 40.0D, 0.0D, 1.1D, 0.5F))),
    /** B: a high X then a low X; in phase 3 the low X arrives two ticks early (§13.B item 11). */
    CROSSED_MANDATE(ArchitectAnim.CROSSED_MANDATE, Architect.Phase.DUELIST, 50, 23, 2, 1.4F, List.of(
            corridor(11, 13, 2.3D, 0.4D, 2.8D, 1.4D, 3.2D, 0.9F, ALWAYS),
            corridor(16, 18, 2.3D, 0.4D, 2.8D, 0.6D, 2.0D, 0.9F, NORMAL_ONLY),
            corridor(14, 16, 2.3D, 0.4D, 2.8D, 0.6D, 2.0D, 0.9F, VARIANT_ONLY))),
    /** C: three rings around him, high, middle and low, with safe gaps between them (§13.C). */
    CARTENON_WHEEL(ArchitectAnim.CARTENON_WHEEL, Architect.Phase.DUELIST, 120, 35, 2, 99.0F, List.of(
            ring(17, 19, 0.8D, 3.2D, 1.8D, 3.4D, 0.6F),
            ring(22, 24, 0.8D, 3.4D, 1.0D, 2.2D, 0.6F),
            ring(27, 28, 0.8D, 3.0D, 0.0D, 1.1D, 0.6F))),
    /** D: low sweep, two rising crescents that catch jumpers, a short chop; the variation skips the sweep (target in the air). */
    SPLITTER_CRESCENT(ArchitectAnim.SPLITTER_CRESCENT, Architect.Phase.DUELIST, 60, 25, 2, 99.0F, List.of(
            arc(10, 12, 0.0D, 2.6D, -70.0D, 70.0D, 0.0D, 1.0D, 0.5F, NORMAL_ONLY),
            arc(16, 19, 2.8D, -70.0D, -10.0D, 0.5D, 3.6D, 0.6F),
            arc(16, 19, 2.8D, 10.0D, 70.0D, 0.5D, 3.6D, 0.6F),
            corridor(22, 24, 1.2D, 0.0D, 2.4D, 0.0D, 3.2D, 0.7F, ALWAYS))),
    /** E: the upper blades thrust down a narrow corridor after a real 1.5-block lunge (§13.E). */
    PIERCING_SENTENCE(ArchitectAnim.PIERCING_SENTENCE, Architect.Phase.DUELIST, 80, 20, 2, 1.3F, List.of(
            corridor(13, 14, 1.0D, 0.0D, 4.5D, 0.8D, 2.8D, 1.0F, ALWAYS),
            corridor(15, 16, 0.8D, 0.0D, 3.5D, 0.8D, 2.8D, 0.5F, ALWAYS))),
    /** F: leap, apex, dive onto the marked point; a central blow and a low ring wave (§13.F). */
    GARGOYLE_FALL(ArchitectAnim.GARGOYLE_FALL, Architect.Phase.ADMINISTRATOR, 200, 36, 2, 99.0F, List.of(
            ring(32, 33, 0.0D, 1.5D, 0.0D, 2.0D, 1.2F),
            ring(34, 35, 1.5D, 3.5D, 0.0D, 0.8D, 0.5F))),
    /** G: the reprise after a successful parry; only the parried attacker can be hit (§11.3, §13.G). */
    PARRY_REPRISE(ArchitectAnim.PARRY_LEFT, Architect.Phase.DUELIST, 100, 17, 1, 99.0F, List.of(
            arc(13, 14, 2.5D, -35.0D, 35.0D, 1.0D, 3.0D, 0.6F)));

    public final ArchitectAnim anim;
    /** The first phase in which the brain may pick it (F also answers pillaring in any phase). */
    public final Architect.Phase minPhase;
    /** Ticks before the same attack can be picked again, counted from its start ([ADP]). */
    public final int cooldown;
    public final int recoveryStart;
    /** Most volumes that may hit the same victim in one use. */
    public final int maxHitsPerTarget;
    /** Most damage (in DB) one victim can take from one use. */
    public final float maxDamagePerTarget;
    public final List<Strike> strikes;
    /** The anti-whiff check runs on this tick: one before the first volume. */
    public final int validationTick;
    public final int firstActiveTick;
    public final double reach;

    ArchitectAttack(ArchitectAnim anim, Architect.Phase minPhase, int cooldown, int recoveryStart, int maxHitsPerTarget,
                    float maxDamagePerTarget, List<Strike> strikes) {
        this.anim = anim;
        this.minPhase = minPhase;
        this.cooldown = cooldown;
        this.recoveryStart = recoveryStart;
        this.maxHitsPerTarget = maxHitsPerTarget;
        this.maxDamagePerTarget = maxDamagePerTarget;
        this.strikes = strikes;
        int first = Integer.MAX_VALUE;
        double far = 0.0D;
        for (Strike s : strikes) {
            first = Math.min(first, s.start());
            far = Math.max(far, s.reach());
        }
        this.firstActiveTick = first;
        this.validationTick = first - 1;
        this.reach = far;
    }

    public int ticks() {
        return this.anim.ticks;
    }

    /** True while at least one volume of this attack exists on {@code tick}. */
    public boolean activeAt(int tick, boolean variant) {
        for (Strike s : this.strikes) {
            if (s.liveAt(tick, variant)) {
                return true;
            }
        }
        return false;
    }
}
