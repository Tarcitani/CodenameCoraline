package com.codenamecoraline.entity.ten_threads_witch;

import com.codenamecoraline.entity.ten_threads_witch.PuppetAttack.Cut;
import com.codenamecoraline.entity.ten_threads_witch.PuppetAttack.Effect;
import com.codenamecoraline.entity.ten_threads_witch.PuppetAttack.Style;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * The ten puppets of the Witch of Ten Threads, in the order they fight (the stage is {@code ordinal() + 1}), with the
 * attributes, dimensions and attack catalogue of the design sheet. All of them share {@link PuppetEntity}: what makes a
 * puppet different is this table, its model and its sounds.
 */
public enum PuppetKind {
    SEWING_ARACHNID("sewing_arachnid", 1.3F, 1.1F, 0.65F, 80.0D, 2.0D, 0.2D, 0.34D, 6.0D, false, 1.0F, List.of(
            new PuppetAttack("ground_stitch", Style.FAN, 4.0D, 12.0D, 16, 10, 14, 4.0F).arc(30.0D).cd(40)
                    .effect(Effect.SLOWNESS, 40, 2),
            new PuppetAttack("needle_jab", Style.MULTI, 0.0D, 3.2D, 12, 10, 12, 6.0F).hits(2, 1.0F).cd(30),
            new PuppetAttack("canopy_plunge", Style.LEAP, 3.0D, 10.0D, 24, 14, 20, 11.0F).cd(140)
                    .special(70, true, Cut.WINDUP))),
    PORCELAIN_HOUND("porcelain_hound", 0.9F, 1.4F, 1.1F, 95.0D, 10.0D, 0.3D, 0.41D, 7.0D, false, 1.0F, List.of(
            new PuppetAttack("hinge_bite", Style.CONE, 0.0D, 3.0D, 10, 8, 12, 7.0F).cd(30).effect(Effect.WITHER, 40, 0),
            new PuppetAttack("shard_shake", Style.RING, 0.0D, 4.0D, 16, 12, 16, 5.0F).cd(80),
            new PuppetAttack("porcelain_lunge", Style.CHARGE, 4.0D, 14.0D, 22, 16, 18, 12.0F).cd(120).knock(1.4D, 0.3D)
                    .special(80, true, Cut.WINDUP))),
    HOLLOW_BELL("hollow_bell", 1.4F, 2.5F, 2.1F, 110.0D, 8.0D, 0.8D, 0.26D, 8.0D, true, 1.0F, List.of(
            new PuppetAttack("repulse_toll", Style.RING, 0.0D, 9.0D, 20, 12, 18, 8.0F).cd(60).knock(1.0D, 0.2D).groundOnly(),
            new PuppetAttack("bronze_slam", Style.RING, 0.0D, 4.0D, 18, 10, 20, 10.0F).cd(60).knock(0.6D, 0.3D),
            new PuppetAttack("funeral_resonance", Style.WAVES, 0.0D, 10.0D, 40, 24, 24, 13.0F).cd(160)
                    .effect(Effect.NAUSEA, 60, 0).special(80, false, Cut.WINDUP))),
    PATCHWORK_STAG("patchwork_stag", 1.4F, 3.1F, 2.6F, 125.0D, 4.0D, 0.5D, 0.38D, 9.0D, false, 1.5F, List.of(
            new PuppetAttack("antler_sweep", Style.CONE, 0.0D, 4.0D, 16, 10, 16, 9.0F).arc(180.0D).cd(40).knock(0.8D, 0.6D),
            new PuppetAttack("stilt_stomp", Style.CONE, 0.0D, 3.5D, 18, 10, 16, 11.0F).arc(130.0D).cd(50),
            new PuppetAttack("impaling_stampede", Style.CHARGE, 5.0D, 16.0D, 26, 20, 20, 14.0F).cd(150).knock(1.2D, 0.4D)
                    .special(80, true, Cut.WINDUP))),
    STITCHED_ROOK("stitched_rook", 1.5F, 2.2F, 1.8F, 115.0D, 3.0D, 0.2D, 0.42D, 8.0D, true, 1.0F, List.of(
            new PuppetAttack("tattered_gust", Style.CONE, 0.0D, 6.0D, 14, 12, 14, 4.0F).arc(100.0D).cd(40).knock(1.6D, 0.2D),
            new PuppetAttack("quill_barrage", Style.FAN, 5.0D, 15.0D, 18, 10, 16, 6.0F).arc(50.0D).cd(70),
            new PuppetAttack("shear_dive", Style.CHARGE, 6.0D, 16.0D, 28, 16, 18, 13.0F).cd(150)
                    .special(80, true, Cut.WINDUP))),
    HOLLOW_BRIDE("hollow_bride", 0.8F, 2.7F, 2.3F, 135.0D, 5.0D, 0.4D, 0.33D, 7.0D, false, 1.2F, List.of(
            new PuppetAttack("eight_arm_flurry", Style.MULTI, 0.0D, 3.2D, 14, 16, 16, 3.5F).hits(4, 1.0F).cd(50)
                    .effect(Effect.POISON, 40, 0),
            new PuppetAttack("thread_sidestep", Style.SIDESTEP, 0.0D, 0.0D, 4, 8, 6, 0.0F).cd(60).reactive(),
            new PuppetAttack("bridal_veil_grapple", Style.GRAPPLE, 4.0D, 9.0D, 22, 14, 18, 13.0F).cd(140)
                    .special(80, true, Cut.WINDUP))),
    SKIRTED_CENTIPEDE("skirted_centipede", 1.0F, 0.9F, 0.7F, 145.0D, 6.0D, 0.7D, 0.39D, 8.0D, false, 1.0F, List.of(
            new PuppetAttack("procession_coil", Style.RING, 3.0D, 7.0D, 14, 30, 14, 7.0F).cd(60).groundOnly(),
            new PuppetAttack("pin_spit", Style.FAN, 4.0D, 11.0D, 16, 10, 14, 8.0F).arc(70.0D).cd(50),
            new PuppetAttack("burrow_ambush", Style.BURROW, 3.0D, 16.0D, 30, 14, 36, 14.0F).cd(160)
                    .special(80, false, Cut.RECOVERY))),
    ROOT_MOTHER("root_mother", 2.0F, 3.0F, 2.4F, 160.0D, 10.0D, 1.0D, 0.22D, 12.0D, false, 1.0F, List.of(
            new PuppetAttack("maternal_sweep", Style.CONE, 0.0D, 4.5D, 20, 12, 18, 12.0F).arc(220.0D).cd(50).knock(1.6D, 0.3D),
            new PuppetAttack("bramble_snare", Style.TRAP, 4.0D, 12.0D, 20, 10, 16, 5.0F).cd(90)
                    .effect(Effect.ROOT, 50, 6),
            new PuppetAttack("deep_rooting", Style.WAVES, 0.0D, 9.0D, 60, 60, 20, 10.0F).cd(180)
                    .special(90, false, Cut.WINDUP))),
    PATCHWORK_EXECUTIONER("patchwork_executioner", 1.8F, 3.8F, 3.2F, 185.0D, 12.0D, 0.9D, 0.25D, 13.0D, false, 1.2F, List.of(
            new PuppetAttack("dragging_cleave", Style.RING, 0.0D, 5.0D, 22, 14, 20, 13.0F).cd(60).knock(1.0D, 0.3D).groundOnly(),
            new PuppetAttack("tectonic_hop", Style.LEAP, 5.0D, 12.0D, 20, 14, 18, 11.0F).cd(90),
            new PuppetAttack("guillotine_smash", Style.CONE, 0.0D, 6.0D, 28, 10, 90, 18.0F).arc(100.0D).cd(150).breaksShield()
                    .special(90, true, Cut.WINDUP))),
    CRADLE_GIANT("cradle_giant", 1.6F, 4.2F, 3.8F, 220.0D, 8.0D, 0.9D, 0.30D, 12.0D, false, 1.4F, List.of(
            new PuppetAttack("cross_loom_slash", Style.CONE, 0.0D, 4.8D, 16, 10, 14, 12.0F).arc(140.0D).cd(50),
            new PuppetAttack("forest_wire_trap", Style.WIRE, 5.0D, 14.0D, 20, 16, 16, 10.0F).cd(90),
            new PuppetAttack("six_limb_barrage", Style.MULTI, 0.0D, 6.0D, 18, 36, 40, 7.0F).hits(6, 2.0F).arc(150.0D).cd(160)
                    .special(100, false, Cut.RECOVERY)));

    public final String id;
    public final float width;
    public final float height;
    public final float eyeHeight;
    public final double health;
    public final double armor;
    public final double knockbackResistance;
    public final double speed;
    public final double damage;
    public final boolean flies;
    public final float stepHeight;
    public final List<PuppetAttack> attacks;

    PuppetKind(String id, float width, float height, float eyeHeight, double health, double armor, double knockbackResistance,
               double speed, double damage, boolean flies, float stepHeight, List<PuppetAttack> attacks) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.eyeHeight = eyeHeight;
        this.health = health;
        this.armor = armor;
        this.knockbackResistance = knockbackResistance;
        this.speed = speed;
        this.damage = damage;
        this.flies = flies;
        this.stepHeight = stepHeight;
        this.attacks = attacks;
    }

    /** The stage this puppet fights in (1 to 10). */
    public int stage() {
        return this.ordinal() + 1;
    }

    /** Bit of this puppet's spool in the Matriarch's staff wheel. */
    public int spoolBit() {
        return 1 << this.ordinal();
    }

    public String nameKey() {
        return "entity.codenamecoraline." + this.id;
    }

    public static @Nullable PuppetKind byId(String id) {
        for (PuppetKind kind : values()) {
            if (kind.id.equals(id)) {
                return kind;
            }
        }
        return null;
    }

    public static PuppetKind forStage(int stage) {
        return values()[Math.max(1, Math.min(10, stage)) - 1];
    }

    /** The attack that ends a stage's pattern: the special, with its crimson thread. */
    public PuppetAttack special() {
        return this.attacks.getLast();
    }
}
