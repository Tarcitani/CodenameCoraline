package com.codenamecoraline.entity.ten_threads_witch;

/**
 * One attack of a puppet (or of the Matriarch in her last phase), as data: the {@link AttackRunner} turns it into
 * behaviour through its {@link Style}. Timings are in ticks (20 per second), ranges in blocks. Built fluently while the
 * {@link PuppetKind} table is initialised and never changed afterwards.
 */
public final class PuppetAttack {
    /** How the blow is delivered. */
    public enum Style {
        /** One strike in a frontal cone (reach = max range). */
        CONE,
        /** Several evenly spaced strikes in the frontal cone. */
        MULTI,
        /** One strike around the attacker (radius = max range). */
        RING,
        /** A narrow, long cone (needles, pins, feathers). */
        FAN,
        /** A straight advance along a direction locked before the end of the wind-up. */
        CHARGE,
        /** A jump towards the target that lands with a slam. */
        LEAP,
        /** Expanding rings (three waves). */
        WAVES,
        /** A spiral of exploding spools around the attacker. */
        STORM,
        /** A veil that drags the target in. */
        GRAPPLE,
        /** A reposition of a few blocks (no damage). */
        SIDESTEP,
        /** Underground run to the target's spot and an eruption. */
        BURROW,
        /** A circle marked under the target, locked a moment before the blow. */
        TRAP,
        /** A wire stretched between the attacker and the target's spot, low over the ground. */
        WIRE
    }

    /** Status effect applied to whoever the blow hits. */
    public enum Effect { NONE, SLOWNESS, WITHER, NAUSEA, POISON, ROOT }

    /** When the crimson thread of a special attack can be cut. */
    public enum Cut { NEVER, WINDUP, RECOVERY }

    public final String id;
    public final Style style;
    public final double minRange;
    public final double maxRange;
    public final int prep;
    public final int exec;
    public final int recover;
    public final float damage;
    public boolean special = false;
    public int cooldown = 40;
    /** Ticks of the TANGLED punish window this attack can lead to. */
    public int tangleTicks = 0;
    /** The attack ends in TANGLED when it did not hit anyone (the dodge is the counter-play). */
    public boolean missTangles = false;
    public Cut cut = Cut.NEVER;
    public Effect effect = Effect.NONE;
    public int effectTicks = 0;
    public int effectAmplifier = 0;
    public double push = 0.0D;
    public double lift = 0.0D;
    public int hits = 1;
    public double arc = 120.0D;
    /** Only whoever stands on the ground is hit: jumping avoids it. */
    public boolean groundOnly = false;
    /** Triggered by the brain, never picked by the weighted choice. */
    public boolean reactive = false;
    /** A raised shield is disabled by the blow. */
    public boolean breaksShield = false;
    /** The last strike of a multi-hit attack deals this multiple of the damage. */
    public float lastHitFactor = 1.0F;

    public PuppetAttack(String id, Style style, double minRange, double maxRange, int prep, int exec, int recover, float damage) {
        this.id = id;
        this.style = style;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.prep = prep;
        this.exec = exec;
        this.recover = recover;
        this.damage = damage;
    }

    public PuppetAttack cd(int ticks) {
        this.cooldown = ticks;
        return this;
    }

    /** A special attack: its wind-up turns the thread crimson and it can end in TANGLED. */
    public PuppetAttack special(int tangleTicks, boolean missTangles, Cut cut) {
        this.special = true;
        this.tangleTicks = tangleTicks;
        this.missTangles = missTangles;
        this.cut = cut;
        return this;
    }

    public PuppetAttack effect(Effect effect, int ticks, int amplifier) {
        this.effect = effect;
        this.effectTicks = ticks;
        this.effectAmplifier = amplifier;
        return this;
    }

    public PuppetAttack knock(double push, double lift) {
        this.push = push;
        this.lift = lift;
        return this;
    }

    public PuppetAttack hits(int hits, float lastHitFactor) {
        this.hits = hits;
        this.lastHitFactor = lastHitFactor;
        return this;
    }

    public PuppetAttack arc(double degrees) {
        this.arc = degrees;
        return this;
    }

    public PuppetAttack groundOnly() {
        this.groundOnly = true;
        return this;
    }

    public PuppetAttack reactive() {
        this.reactive = true;
        return this;
    }

    public PuppetAttack breaksShield() {
        this.breaksShield = true;
        return this;
    }

    @Override
    public String toString() {
        return this.id;
    }
}
