package com.codenamecoraline.entity.ten_threads_witch.matriarch;

import com.codenamecoraline.entity.ten_threads_witch.PuppetAttack;
import com.codenamecoraline.entity.ten_threads_witch.PuppetAttack.Cut;
import com.codenamecoraline.entity.ten_threads_witch.PuppetAttack.Effect;
import com.codenamecoraline.entity.ten_threads_witch.PuppetAttack.Style;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * What the Matriarch fights with in the last phase, and how each blow looks. Every attack is run by the shared
 * {@code AttackRunner} (the gameplay did not move); this table only says which preparation, execution and recovery clip
 * plays in each phase, and which follow-ups a blow may chain into (the combos of section 6.6). The AI picks the branch from
 * the situation; the animation system only plays what was picked.
 */
public final class MatriarchMoveset {
    private MatriarchMoveset() {}

    // ---------------------------------------------------------------- the attacks (the first three are the original ones)
    public static final PuppetAttack NEEDLE_THRUST = new PuppetAttack("last_needle_thrust", Style.CHARGE, 3.0D, 10.0D, 14, 10, 14, 10.0F).cd(50).knock(1.2D, 0.3D);
    public static final PuppetAttack PHANTOM_ECHO = new PuppetAttack("phantom_puppet_echo", Style.CONE, 2.0D, 8.0D, 16, 12, 14, 11.0F).arc(130.0D).cd(60).knock(0.9D, 0.3D);
    public static final PuppetAttack UNRAVELED_STORM = new PuppetAttack("unraveled_storm", Style.STORM, 0.0D, 12.0D, 20, 60, 20, 9.0F).cd(140).special(80, true, Cut.NEVER);
    public static final PuppetAttack THIMBLE_FLICK = new PuppetAttack("thimble_flick", Style.CONE, 0.0D, 2.6D, 6, 4, 8, 6.0F).arc(100.0D).cd(36);
    public static final PuppetAttack SEAM_RIP = new PuppetAttack("seam_rip", Style.CONE, 0.0D, 3.2D, 9, 5, 10, 8.0F).arc(110.0D).cd(50);
    public static final PuppetAttack STAFF_CRUSH = new PuppetAttack("staff_crush", Style.RING, 0.0D, 3.5D, 22, 6, 18, 13.0F).cd(90).knock(1.0D, 0.5D).breaksShield();
    public static final PuppetAttack HEM_CLEAVE = new PuppetAttack("hem_cleave", Style.MULTI, 0.0D, 3.0D, 16, 12, 14, 8.0F).hits(2, 1.25F).cd(70);
    public static final PuppetAttack NEEDLE_VOLLEY = new PuppetAttack("needle_volley", Style.FAN, 4.0D, 14.0D, 14, 6, 12, 7.0F).arc(40.0D).cd(70);
    public static final PuppetAttack THREAD_LANCE = new PuppetAttack("thread_lance", Style.FAN, 3.0D, 12.0D, 12, 6, 12, 9.0F).arc(16.0D).cd(60);
    public static final PuppetAttack BINDING_COIL = new PuppetAttack("binding_coil", Style.GRAPPLE, 3.0D, 9.0D, 14, 16, 12, 5.0F).cd(100);
    public static final PuppetAttack COCOON_SNARE = new PuppetAttack("cocoon_snare", Style.TRAP, 2.0D, 10.0D, 18, 8, 12, 8.0F).effect(Effect.SLOWNESS, 60, 1).cd(90);
    public static final PuppetAttack TRIPWIRE_CAST = new PuppetAttack("tripwire_cast", Style.WIRE, 4.0D, 12.0D, 14, 10, 12, 7.0F).groundOnly().cd(80);
    public static final PuppetAttack THREAD_BARRAGE = new PuppetAttack("thread_barrage", Style.MULTI, 0.0D, 7.0D, 16, 18, 14, 5.0F).hits(3, 1.0F).arc(150.0D).cd(110);
    public static final PuppetAttack UNMAKING_EMBRACE = new PuppetAttack("unmaking_embrace", Style.CONE, 0.0D, 4.0D, 14, 6, 14, 11.0F).arc(160.0D).cd(80).knock(0.8D, 0.2D);
    // only reached through a combo or a defence (never picked on their own)
    public static final PuppetAttack COMBO_FINALE = new PuppetAttack("combo_finale", Style.RING, 0.0D, 3.0D, 4, 6, 20, 12.0F).knock(1.4D, 0.5D).cd(60).reactive();
    public static final PuppetAttack FEINT_FLICK = new PuppetAttack("feint_flick", Style.CONE, 0.0D, 2.6D, 2, 4, 8, 6.0F).arc(100.0D).cd(30).reactive();
    public static final PuppetAttack PARRY_RIPOSTE = new PuppetAttack("parry_riposte", Style.CONE, 0.0D, 2.8D, 3, 4, 8, 7.0F).arc(90.0D).cd(30).reactive();
    public static final PuppetAttack GUARD_CRUSH = new PuppetAttack("guard_crush", Style.RING, 0.0D, 3.0D, 8, 6, 16, 10.0F).knock(1.0D, 0.4D).cd(40).reactive();
    public static final PuppetAttack BARRIER_LANCE = new PuppetAttack("barrier_lance", Style.FAN, 0.0D, 12.0D, 4, 6, 12, 8.0F).arc(16.0D).cd(40).reactive();
    public static final PuppetAttack DODGE_COUNTER = new PuppetAttack("dodge_counter", Style.CONE, 0.0D, 3.4D, 4, 5, 10, 8.0F).arc(110.0D).cd(40).reactive();

    public static final List<PuppetAttack> FURY_ATTACKS = List.of(NEEDLE_THRUST, PHANTOM_ECHO, UNRAVELED_STORM, THIMBLE_FLICK, SEAM_RIP,
            STAFF_CRUSH, HEM_CLEAVE, NEEDLE_VOLLEY, THREAD_LANCE, BINDING_COIL, COCOON_SNARE, TRIPWIRE_CAST, THREAD_BARRAGE, UNMAKING_EMBRACE,
            COMBO_FINALE, FEINT_FLICK, PARRY_RIPOSTE, GUARD_CRUSH, BARRIER_LANCE, DODGE_COUNTER);

    /**
     * The clips of one attack. {@code prep} plays (and holds) during the wind-up; {@code exec} covers the blow and its
     * recovery tail; {@code recover}, when not null, replaces that tail during the recovery phase. Thread blows play on the
     * hand slot (her legs keep whatever they were doing); body blows on the body slot.
     */
    public record Look(MatAnim prep, MatAnim exec, @Nullable MatAnim recover) {}

    private static final Map<String, Look> LOOKS = Map.ofEntries(
            Map.entry("last_needle_thrust", new Look(MatAnim.FLOAT_05, MatAnim.FLOAT_05, null)),
            Map.entry("phantom_puppet_echo", new Look(MatAnim.THREAD_21, MatAnim.THREAD_23, MatAnim.THREAD_27)),
            Map.entry("unraveled_storm", new Look(MatAnim.ATK_03, MatAnim.ATK_12, null)),
            Map.entry("thimble_flick", new Look(MatAnim.ATK_01, MatAnim.ATK_06, null)),
            Map.entry("seam_rip", new Look(MatAnim.ATK_01, MatAnim.ATK_08, null)),
            Map.entry("staff_crush", new Look(MatAnim.ATK_02, MatAnim.ATK_07, null)),
            Map.entry("hem_cleave", new Look(MatAnim.ATK_04, MatAnim.ATK_09, null)),
            Map.entry("needle_volley", new Look(MatAnim.THREAD_21, MatAnim.ATK_11, null)),
            Map.entry("thread_lance", new Look(MatAnim.THREAD_21, MatAnim.THREAD_24, MatAnim.THREAD_27)),
            Map.entry("binding_coil", new Look(MatAnim.THREAD_21, MatAnim.THREAD_26, MatAnim.THREAD_27)),
            Map.entry("cocoon_snare", new Look(MatAnim.THREAD_06, MatAnim.THREAD_25, MatAnim.THREAD_27)),
            Map.entry("tripwire_cast", new Look(MatAnim.THREAD_21, MatAnim.THREAD_22, MatAnim.THREAD_27)),
            Map.entry("thread_barrage", new Look(MatAnim.THREAD_21, MatAnim.THREAD_28, MatAnim.THREAD_27)),
            Map.entry("unmaking_embrace", new Look(MatAnim.ATK_02, MatAnim.ATK_10, null)),
            Map.entry("combo_finale", new Look(MatAnim.ATK_01, MatAnim.ATK_13, null)),
            Map.entry("feint_flick", new Look(MatAnim.ATK_04, MatAnim.ATK_06, null)),
            Map.entry("parry_riposte", new Look(MatAnim.ATK_01, MatAnim.ATK_06, null)),
            Map.entry("guard_crush", new Look(MatAnim.ATK_02, MatAnim.ATK_07, null)),
            Map.entry("barrier_lance", new Look(MatAnim.THREAD_21, MatAnim.THREAD_24, MatAnim.THREAD_27)),
            Map.entry("dodge_counter", new Look(MatAnim.ATK_01, MatAnim.ATK_08, null)));

    public static Look look(PuppetAttack attack) {
        return LOOKS.getOrDefault(attack.id, new Look(MatAnim.ATK_01, MatAnim.ATK_06, null));
    }

    // ---------------------------------------------------------------- combos (6.6)
    /** What the AI reads of the target when it decides how to continue a combo. */
    public enum Condition {
        CLOSE, MID, FAR, BLOCKING, RETREATING, AIRBORNE;

        public boolean holds(LivingEntity self, LivingEntity target, double distance) {
            return switch (this) {
                case CLOSE -> distance <= 2.8D;
                case MID -> distance > 2.8D && distance <= 7.0D;
                case FAR -> distance > 7.0D;
                case BLOCKING -> target instanceof Player p && p.isBlocking();
                case RETREATING -> target.getDeltaMovement().horizontalDistanceSqr() > 0.004D
                        && target.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).dot(target.position().subtract(self.position()).multiply(1.0D, 0.0D, 1.0D)) > 0.0D;
                case AIRBORNE -> !target.onGround();
            };
        }
    }

    /**
     * One branch of a combo: after {@code from} ends, within {@code window} ticks, if every condition holds, {@code to}
     * starts (its preparation clip is the transition from the recovery of {@code from}). A branch is cancelled when the target
     * is gone, out of sight, or when she is stunned, falling or hit hard (the director forgets the window).
     */
    public record Branch(String from, int window, List<Condition> when, PuppetAttack to) {}

    public static final List<Branch> COMBOS = List.of(
            new Branch("thimble_flick", 8, List.of(Condition.BLOCKING), STAFF_CRUSH),
            new Branch("thimble_flick", 8, List.of(Condition.CLOSE), SEAM_RIP),
            new Branch("thimble_flick", 8, List.of(Condition.MID, Condition.RETREATING), PHANTOM_ECHO),
            new Branch("seam_rip", 8, List.of(Condition.CLOSE), COMBO_FINALE),
            new Branch("seam_rip", 8, List.of(Condition.FAR), THREAD_LANCE),
            new Branch("staff_crush", 10, List.of(Condition.MID), BINDING_COIL),
            new Branch("thread_lance", 8, List.of(Condition.CLOSE), SEAM_RIP),
            new Branch("thread_lance", 8, List.of(Condition.FAR), NEEDLE_VOLLEY),
            new Branch("binding_coil", 10, List.of(Condition.CLOSE), COMBO_FINALE),
            new Branch("tripwire_cast", 8, List.of(Condition.AIRBORNE), THREAD_LANCE),
            new Branch("needle_volley", 8, List.of(Condition.MID), COCOON_SNARE));

    /** The first branch of {@code from} whose conditions all hold, or null (the combo ends and she recovers). */
    public static @Nullable Branch branch(String from, LivingEntity self, LivingEntity target, double distance) {
        for (Branch b : COMBOS) {
            if (b.from().equals(from) && b.when().stream().allMatch(c -> c.holds(self, target, distance))) {
                return b;
            }
        }
        return null;
    }

    public static int comboWindow(String from) {
        int w = 0;
        for (Branch b : COMBOS) {
            if (b.from().equals(from)) {
                w = Math.max(w, b.window());
            }
        }
        return w;
    }
}
