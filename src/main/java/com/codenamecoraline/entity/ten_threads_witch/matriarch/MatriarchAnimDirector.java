package com.codenamecoraline.entity.ten_threads_witch.matriarch;

import com.codenamecoraline.entity.ten_threads_witch.AttackRunner.Phase;
import com.codenamecoraline.entity.ten_threads_witch.EncounterPhase;
import com.codenamecoraline.entity.ten_threads_witch.MatriarchWitchEntity;
import com.codenamecoraline.entity.ten_threads_witch.PuppetAttack;
import com.codenamecoraline.entity.ten_threads_witch.PuppetEntity;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Server half of the Matriarch's animation system: it turns what her AI and her puppets do into clips on the synched slots,
 * enforces the priority and interruption rules (section 8 of the specification), chains transitions, schedules her idle
 * behaviour, points her gaze, and fires each synchronisation event once. It never chooses a target or an attack; the AI
 * tells it what happened and it decides how that looks.
 *
 * <p>Rules, per slot: a request replaces the current clip when the current one has ended, is an idle (flag INT), or has a
 * weaker or equal priority; a LOCK clip (fall, transition, cinematic, death) only yields to a strictly stronger priority. A
 * thread request for a hand that the body clip claims (flags HL, HR) waits and starts when the body releases the arm. A
 * one-shot that ends goes to its queue, then to its fixed exit ({@link MatAnim#next()}), then to the hand's control loop, and
 * only then to nothing.
 */
public final class MatriarchAnimDirector {
    private static final int SLOTS = MatSlot.SYNCHED.length;
    private static final int BODY = 0;
    private static final int REACT = 1;
    private static final int HAND_L = 2;
    private static final int HAND_R = 3;
    private static final int FACE = 4;

    /** One blow of at least this much knocks her down (in the last phase); the threshold rises by {@link #KNOCKDOWN_RISE} as she unravels. */
    public static final float KNOCKDOWN_DAMAGE = 12.0F;
    public static final float KNOCKDOWN_RISE = 4.0F;
    /** After a knockdown she cannot be knocked down again for this long (heavy blows stagger her instead). */
    public static final int KNOCKDOWN_COOLDOWN = 240;
    /** A wind-up is interrupted by a blow of at least this much; a blow of at least {@link #STAGGER_DAMAGE} staggers her. */
    public static final float INTERRUPT_DAMAGE = 6.0F;
    public static final float STAGGER_DAMAGE = 9.0F;
    /** Interruptions and staggers share this cooldown, so she can never be stun-locked. */
    public static final int STUN_COOLDOWN = 80;
    /** Chance of a defence: base plus a share of her strangeness; one defence at most every {@link #DEFENSE_COOLDOWN} ticks. */
    public static final float DEFENSE_CHANCE = 0.18F;
    public static final float DEFENSE_CHANCE_RISE = 0.14F;
    public static final int DEFENSE_COOLDOWN = 100;
    public static final int DOWN_TICKS = 26;

    /** What she looks at. */
    public enum LookMode { NONE, TARGET, PUPPET, THREADS }

    private final MatriarchWitchEntity w;
    private final MatAnim[] anim = new MatAnim[SLOTS];
    private final int[] variant = new int[SLOTS];
    private final int[] age = new int[SLOTS];
    private final int[] sequence = new int[SLOTS];
    @SuppressWarnings("unchecked")
    private final ArrayDeque<int[]>[] queue = new ArrayDeque[SLOTS];
    private final MatAnim[] handLoop = new MatAnim[2];
    private final MatAnim[] pending = new MatAnim[2];
    private final Map<MatEvent, Integer> fired = new EnumMap<>(MatEvent.class);
    private final Map<MatAnim, Integer> started = new EnumMap<>(MatAnim.class);

    private int idleCooldown = 140;
    private int tauntCooldown = 200;
    private int mimicCooldown = 260;
    private int flourishCooldown = 90;
    private int transferCooldown = 560;
    private int defenseCooldown = 0;
    private int knockdownCooldown = 0;
    private int stunCooldown = 0;
    private int faceHold = 0;
    private boolean primaryRight = false;
    private @Nullable Entity lookOverride;
    private LookMode lookOverrideMode = LookMode.NONE;
    private int lookOverrideTicks = 0;
    private int downTicks = 0;
    private int stunTicks = 0;
    private boolean recoveryInterrupted = false;
    private int recentHits = 0;
    private int lastHitAge = -1000;
    private int clock = 0;
    private boolean prone = false;

    public MatriarchAnimDirector(MatriarchWitchEntity witch) {
        this.w = witch;
        for (int i = 0; i < SLOTS; i++) {
            this.queue[i] = new ArrayDeque<>();
        }
    }

    // ---------------------------------------------------------------- state for the AI and for tests
    public @Nullable MatAnim current(MatSlot slot) {
        return this.anim[slot.ordinal()];
    }

    public int age(MatSlot slot) {
        return this.age[slot.ordinal()];
    }

    public int firedCount(MatEvent e) {
        return this.fired.getOrDefault(e, 0);
    }

    public int startedCount(MatAnim a) {
        return this.started.getOrDefault(a, 0);
    }

    /** Down after a fall or stunned: the AI neither moves nor attacks. */
    public boolean incapacitated() {
        MatAnim body = this.anim[BODY];
        return this.downTicks > 0 || this.stunTicks > 0 || (body != null && (body.priority <= 5 && body.priority >= 4));
    }

    /** A cinematic or a stage transition is playing: the combat AI waits. */
    public boolean inCinematic() {
        MatAnim body = this.anim[BODY];
        return body != null && body.priority <= 3;
    }

    public boolean bodyFree() {
        MatAnim body = this.anim[BODY];
        return body == null || body.has(MatAnim.Flags.INT);
    }

    public boolean primaryIsRight() {
        return this.primaryRight;
    }

    // ---------------------------------------------------------------- playing
    private int index(MatAnim a, boolean right) {
        return switch (a.slot) {
            case BODY -> BODY;
            case REACT -> REACT;
            case FACE -> FACE;
            case HAND_L, HAND_R -> a.has(MatAnim.Flags.BOTH) ? HAND_L : (right ? HAND_R : HAND_L);
            default -> -1;
        };
    }

    private boolean canReplace(int slot, MatAnim next) {
        MatAnim cur = this.anim[slot];
        if (cur == null || this.finished(slot)) {
            return true;
        }
        if (cur.has(MatAnim.Flags.LOCK)) {
            return next.priority < cur.priority;
        }
        return cur.has(MatAnim.Flags.INT) || next.priority <= cur.priority;
    }

    private boolean finished(int slot) {
        MatAnim cur = this.anim[slot];
        return cur != null && !cur.loops() && this.age[slot] >= cur.lengthTicks(this.variant[slot]);
    }

    private boolean claimed(int hand) {
        MatAnim body = this.anim[BODY];
        if (body == null || this.finished(BODY) && !body.has(MatAnim.Flags.HOLD)) {
            return false;
        }
        return body.has(hand == HAND_L ? MatAnim.Flags.HL : MatAnim.Flags.HR);
    }

    /** Plays {@code a} on its slot (a one-hand clip on the left hand). Returns false when a stronger clip holds the slot. */
    public boolean play(MatAnim a) {
        return this.play(a, 0, false);
    }

    public boolean play(MatAnim a, int variantBits, boolean rightHand) {
        int slot = this.index(a, rightHand);
        if (slot < 0) {
            return false;
        }
        if ((slot == HAND_L || slot == HAND_R) && (this.claimed(slot) || (a.has(MatAnim.Flags.BOTH) && this.claimed(HAND_R)))) {
            this.pending[slot - HAND_L] = a; // waits for the body to release the arm
            return false;
        }
        if (slot == HAND_R && this.anim[HAND_L] != null && this.anim[HAND_L].has(MatAnim.Flags.BOTH) && !this.finished(HAND_L)
                && this.anim[HAND_L].priority <= a.priority) {
            this.pending[1] = a; // both hands are busy with a two-handed gesture
            return false;
        }
        if (!this.canReplace(slot, a)) {
            return false;
        }
        this.queue[slot].clear();
        this.start(slot, a, variantBits);
        return true;
    }

    /** Plays the first clip now and the others one after another on the same slot. */
    public boolean sequence(boolean rightHand, MatAnim... clips) {
        if (clips.length == 0 || !this.play(clips[0], 0, rightHand)) {
            return false;
        }
        int slot = this.index(clips[0], rightHand);
        for (int i = 1; i < clips.length; i++) {
            this.queue[slot].add(new int[] {clips[i].ordinal(), 0});
        }
        return true;
    }

    public void stop(MatSlot s) {
        int slot = s.ordinal();
        this.queue[slot].clear();
        if (this.anim[slot] != null) {
            this.anim[slot] = null;
            this.sequence[slot]++;
            this.w.writeAnimSlot(slot, MatClipRef.pack(null, 0, this.sequence[slot]));
        }
    }

    private void start(int slot, MatAnim a, int variantBits) {
        if (slot == HAND_L && a.has(MatAnim.Flags.BOTH) && this.anim[HAND_R] != null) {
            this.stop(MatSlot.HAND_R); // a two-handed gesture takes the right hand too
        }
        this.anim[slot] = a;
        this.variant[slot] = variantBits;
        this.age[slot] = 0;
        this.sequence[slot]++;
        this.started.merge(a, 1, Integer::sum);
        this.w.writeAnimSlot(slot, MatClipRef.pack(a, variantBits, this.sequence[slot]));
    }

    /** The loops each hand returns to when its one-shots are over (null: the hand is free). */
    public void setHandLoops(@Nullable MatAnim left, @Nullable MatAnim right) {
        this.handLoop[0] = left;
        this.handLoop[1] = right;
        for (int h = 0; h < 2; h++) {
            int slot = HAND_L + h;
            MatAnim cur = this.anim[slot];
            boolean idle = cur == null || this.finished(slot) || (cur.loops() && cur != this.handLoop[h]);
            if (idle && this.queue[slot].isEmpty()) {
                if (this.handLoop[h] != null) {
                    this.play(this.handLoop[h], 0, h == 1);
                } else if (cur != null && cur.loops()) {
                    this.stop(slot == HAND_L ? MatSlot.HAND_L : MatSlot.HAND_R);
                }
            }
        }
    }

    // ---------------------------------------------------------------- the tick
    public void tick(ServerLevel level) {
        this.clock++;
        for (int slot = 0; slot < SLOTS; slot++) {
            MatAnim a = this.anim[slot];
            if (a == null) {
                continue;
            }
            this.age[slot]++;
            float before = MatAnim.clipTick(this.age[slot] - 1, this.variant[slot]);
            float now = MatAnim.clipTick(this.age[slot], this.variant[slot]);
            for (MatAnim.Ev ev : a.events) {
                if (ev.tick() > before && ev.tick() <= now) {
                    this.fired.merge(ev.event(), 1, Integer::sum);
                    MatriarchEffects.fire(this.w, level, ev.event());
                }
            }
            if (!a.loops() && this.age[slot] == a.lengthTicks(this.variant[slot])) {
                this.onFinished(slot);
            }
        }
        for (int h = 0; h < 2; h++) {
            int slot = HAND_L + h;
            if (this.pending[h] != null && !this.claimed(slot)) {
                MatAnim p = this.pending[h];
                this.pending[h] = null;
                this.play(p, 0, h == 1);
            }
        }
        this.downTicks = Math.max(0, this.downTicks - (this.anim[BODY] == MatAnim.FALL_04 ? 1 : 0));
        if (this.anim[BODY] == MatAnim.FALL_04 && this.downTicks == 0 && this.age[BODY] >= MatAnim.FALL_04.ticks) {
            this.beginRecovery();
        }
        this.stunTicks = Math.max(0, this.stunTicks - 1);
        if (this.stunTicks == 1 && this.anim[BODY] == MatAnim.STUN_04) {
            this.play(MatAnim.STUN_05);
        }
        this.defenseCooldown = Math.max(0, this.defenseCooldown - 1);
        this.knockdownCooldown = Math.max(0, this.knockdownCooldown - 1);
        this.stunCooldown = Math.max(0, this.stunCooldown - 1);
        this.idleCooldown--;
        this.tauntCooldown--;
        this.mimicCooldown--;
        this.flourishCooldown--;
        this.transferCooldown--;
        this.faceHold = Math.max(0, this.faceHold - 1);
        this.lookOverrideTicks = Math.max(0, this.lookOverrideTicks - 1);
        this.tickAttention(level);
        this.tickFace();
        this.tickIdle(level);
    }

    private void onFinished(int slot) {
        MatAnim a = this.anim[slot];
        int[] q = this.queue[slot].poll();
        if (q != null) {
            this.start(slot, MatAnim.byOrdinal(q[0]), q[1]);
            return;
        }
        if (a.next() != null) {
            int v = a.next() == MatAnim.FALL_04 ? this.variant[slot] : 0;
            this.start(slot, a.next(), v);
            if (a.next() == MatAnim.FALL_04) {
                this.downTicks = DOWN_TICKS;
            }
            return;
        }
        if (slot == HAND_L || slot == HAND_R) {
            MatAnim loop = this.handLoop[slot - HAND_L];
            if (a.has(MatAnim.Flags.BOTH) && this.handLoop[1] != null) {
                this.pending[1] = this.handLoop[1];
            }
            if (loop != null && !a.has(MatAnim.Flags.HOLD)) {
                this.start(slot, loop, 0);
                return;
            }
        }
        if (!a.has(MatAnim.Flags.HOLD)) {
            this.stop(MatSlot.SYNCHED[slot]);
        }
    }

    // ---------------------------------------------------------------- gaze (MAT-IDLE-04, 05, 06)
    public void lookAt(@Nullable Entity e, LookMode mode, int ticks) {
        this.lookOverride = e;
        this.lookOverrideMode = mode;
        this.lookOverrideTicks = ticks;
    }

    private void tickAttention(ServerLevel level) {
        Entity look = null;
        LookMode mode = LookMode.NONE;
        if (this.lookOverrideTicks > 0 && (this.lookOverrideMode == LookMode.THREADS || (this.lookOverride != null && this.lookOverride.isAlive()))) {
            look = this.lookOverride;
            mode = this.lookOverrideMode;
        } else {
            LivingEntity t = this.w.getTarget();
            if (t == null || !t.isAlive()) {
                t = level.getNearestPlayer(this.w, 24.0D);
            }
            if (t != null && !t.isSpectator()) {
                look = t;
                mode = LookMode.TARGET;
            }
        }
        this.w.setAnimLook(look == null ? -1 : look.getId(), mode.ordinal());
    }

    // ---------------------------------------------------------------- expressions (MAT-EXP)
    /** Shows {@code exp} for at least {@code ticks} before the face returns to the default of her mood. */
    public void express(MatAnim exp, int ticks) {
        if (this.anim[FACE] != exp) {
            this.play(exp);
        }
        this.faceHold = Math.max(this.faceHold, ticks);
    }

    private MatAnim defaultFace() {
        EncounterPhase phase = this.w.getPhase();
        float u = this.w.getUnravel();
        if (phase == EncounterPhase.STAGE_11_FURY) {
            return this.w.getTarget() != null && u < 0.85F ? MatAnim.EXP_07 : MatAnim.EXP_08;
        }
        return switch (MatriarchMood.of(u)) {
            case COMPOSED -> MatAnim.EXP_02;
            case IRRITATED -> this.w.getCurrentStage() >= 8 ? MatAnim.EXP_06 : MatAnim.EXP_05;
            case UNRAVELLING -> MatAnim.EXP_08;
        };
    }

    private void tickFace() {
        if (this.faceHold > 0 || this.w.getPhase() == EncounterPhase.DEATH_CINEMATIC || this.w.getPhase() == EncounterPhase.DORMANT) {
            return;
        }
        MatAnim want = this.defaultFace();
        if (this.anim[FACE] != want) {
            this.play(want);
        }
    }

    // ---------------------------------------------------------------- idle behaviour (MAT-IDLE-02, 03, 06, 08, 09, 10)
    private void tickIdle(ServerLevel level) {
        EncounterPhase phase = this.w.getPhase();
        if (phase == EncounterPhase.DORMANT || phase == EncounterPhase.INTRO || phase == EncounterPhase.DEATH_CINEMATIC
                || phase == EncounterPhase.STAGE_11_TRANSITION || !this.bodyFree() || this.incapacitated()) {
            return;
        }
        RandomSource r = this.w.getRandom();
        boolean fury = phase == EncounterPhase.STAGE_11_FURY;
        boolean busyFighting = fury && (this.w.getRunner().isBusy() || this.w.getTarget() != null);
        Player near = level.getNearestPlayer(this.w, 16.0D);
        // a taunt that imitates what the player is doing
        if (!busyFighting && this.mimicCooldown <= 0 && this.anim[BODY] == null && near != null && !near.isSpectator()) {
            MatAnim mimic = mimicOf(near);
            if (mimic != null) {
                this.play(mimic);
                this.express(MatAnim.EXP_03, mimic.ticks);
                this.lookAt(near, LookMode.TARGET, mimic.ticks);
                this.mimicCooldown = 420 + r.nextInt(200);
                this.idleCooldown = Math.max(this.idleCooldown, 80);
                return;
            }
        }
        // the hands' small flourishes on top of their control loops
        if (this.flourishCooldown <= 0) {
            this.flourishCooldown = 110 + r.nextInt(120);
            int h = this.primaryRight ? 1 : 0;
            MatAnim cur = this.anim[HAND_L + h];
            if (cur != null && cur.loops()) {
                this.play(r.nextBoolean() ? MatAnim.THREAD_13 : MatAnim.THREAD_16, 0, this.primaryRight);
            } else if (cur == null && this.anim[HAND_L] == null && !busyFighting) {
                this.play(r.nextInt(3) == 0 ? MatAnim.IDLE_08_TWO : MatAnim.IDLE_08_ONE, 0, r.nextBoolean());
            }
        }
        // the control passes from one hand to the other now and then (MAT-THREAD-17)
        if (this.transferCooldown <= 0 && phase.isPuppetStage() && this.handLoop[0] != null && this.handLoop[1] != null) {
            this.transferCooldown = 600 + r.nextInt(300);
            // authored from the left hand to the right; mirrored when the right hand holds the control
            if (this.play(MatAnim.THREAD_17, this.primaryRight ? MatAnim.Variant.MIRROR : 0, false)) {
                this.primaryRight = !this.primaryRight;
                MatAnim l = this.handLoop[0];
                this.handLoop[0] = this.handLoop[1] == MatAnim.THREAD_10 ? MatAnim.THREAD_12 : this.handLoop[1];
                this.handLoop[1] = l;
                this.pending[1] = this.handLoop[1];
            }
        }
        if (busyFighting || this.idleCooldown > 0 || this.anim[BODY] != null) {
            return;
        }
        MatriarchMood mood = MatriarchMood.of(this.w.getUnravel());
        int roll = r.nextInt(100);
        MatAnim pick = switch (mood) {
            case COMPOSED -> roll < 40 ? MatAnim.IDLE_02 : roll < 68 ? MatAnim.IDLE_09 : roll < 94 ? MatAnim.IDLE_06 : MatAnim.IDLE_03;
            case IRRITATED -> roll < 35 ? MatAnim.IDLE_03 : roll < 55 ? MatAnim.IDLE_09 : roll < 80 ? MatAnim.IDLE_06 : MatAnim.IDLE_02;
            case UNRAVELLING -> roll < 60 ? MatAnim.IDLE_03 : roll < 80 ? MatAnim.IDLE_06 : MatAnim.IDLE_09;
        };
        this.play(pick);
        if (pick == MatAnim.IDLE_06) {
            this.lookAt(null, LookMode.THREADS, pick.ticks);
        }
        this.idleCooldown = pick.ticks + (mood == MatriarchMood.UNRAVELLING ? 70 : 120) + r.nextInt(140);
    }

    /** The taunt that copies what {@code p} is doing (MAT-IDLE-10, imitation), or null. */
    public static @Nullable MatAnim mimicOf(Player p) {
        if (p.isBlocking()) {
            return MatAnim.IDLE_10_MIMIC_SHIELD;
        }
        if (p.isUsingItem()) {
            return MatAnim.IDLE_10_MIMIC_DRINK;
        }
        if (p.isCrouching()) {
            return MatAnim.IDLE_10_MIMIC_CROUCH;
        }
        if (!p.onGround() && p.getDeltaMovement().y > 0.15D) {
            return MatAnim.IDLE_10_MIMIC_JUMP;
        }
        if (p.swinging) {
            return MatAnim.IDLE_10_MIMIC_SWING;
        }
        return null;
    }

    // ---------------------------------------------------------------- encounter hooks
    /** MAT-CIN-01 to 03: the entrance (threads, appearing, silhouette, hands, first manipulation, final pose). */
    public void onIntro() {
        this.stop(MatSlot.HAND_L);
        this.stop(MatSlot.HAND_R);
        this.play(MatAnim.CIN_01);
        this.express(MatAnim.EXP_01, MatriarchWitchEntity.INTRO_TICKS);
    }

    public void onStageStart(int stage, @Nullable PuppetEntity puppet) {
        this.primaryRight = false;
        boolean more = stage < 10;
        // the last puppet is held with both hands, each giving its own commands (MAT-THREAD-11)
        this.setHandLoops(more ? MatAnim.THREAD_09 : MatAnim.THREAD_11, more ? MatAnim.THREAD_10 : null);
        // the summoning gesture: raise, open, separate, connect, reach and draw the new thread taut
        this.sequence(false, MatAnim.THREAD_05, MatAnim.THREAD_02, MatAnim.THREAD_03, MatAnim.THREAD_07, MatAnim.THREAD_04, MatAnim.THREAD_08);
        if (puppet != null) {
            this.lookAt(puppet, LookMode.PUPPET, 40);
        }
        if (MatriarchMood.of(this.w.getUnravel()) == MatriarchMood.COMPOSED && this.tauntCooldown <= 0) {
            this.play(MatAnim.IDLE_10_ELEGANT);
            this.express(MatAnim.EXP_01, 50);
            this.tauntCooldown = 300;
        } else {
            this.express(stage >= 6 ? MatAnim.EXP_04 : MatAnim.EXP_02, 40);
        }
    }

    /** MAT-CIN-04 / MAT-PHASE-01: the first change of stage (composed to irritated). */
    public void onPhaseOne() {
        this.play(MatAnim.PHASE_01);
        this.express(MatAnim.EXP_06, MatAnim.PHASE_01.ticks);
    }

    /** MAT-CIN-04 / MAT-PHASE-02 to 05: the collapse into the last phase. */
    public void onCollapse() {
        this.stop(MatSlot.REACT);
        this.setHandLoops(null, null);
        this.stop(MatSlot.HAND_L);
        this.stop(MatSlot.HAND_R);
        this.play(MatAnim.PHASE_02);
        int slot = BODY;
        this.queue[slot].add(new int[] {MatAnim.PHASE_03.ordinal(), 0});
        this.queue[slot].add(new int[] {MatAnim.PHASE_05.ordinal(), 0});
        this.queue[slot].add(new int[] {MatAnim.PHASE_04.ordinal(), 0});
        this.express(MatAnim.EXP_09, MatAnim.PHASE_02.ticks + MatAnim.PHASE_03.ticks);
    }

    public void onFuryStart() {
        this.setHandLoops(null, null);
        this.play(MatAnim.THREAD_01); // her own threads appear in her hands: from now on she fights with them
        this.express(MatAnim.EXP_10, 40);
    }

    /** MAT-CIN-05 and 06, section 7: the death. Everything else stops; the clip owns the whole body. */
    public void onDeath() {
        this.setHandLoops(null, null);
        for (MatSlot s : new MatSlot[] {MatSlot.REACT, MatSlot.HAND_L, MatSlot.HAND_R, MatSlot.FACE}) {
            this.stop(s);
        }
        this.downTicks = 0;
        this.stunTicks = 0;
        this.queue[BODY].clear();
        this.start(BODY, MatAnim.CIN_05, 0);
    }

    // ---------------------------------------------------------------- puppet hooks (the threads follow her puppets)
    public void onPuppetPhase(PuppetEntity p, PuppetAttack attack, Phase phase) {
        boolean right = this.primaryRight;
        switch (phase) {
            case PREPARE -> {
                if (attack.special) {
                    this.sequence(false, MatAnim.THREAD_06, MatAnim.THREAD_21);
                    this.express(MatriarchMood.of(this.w.getUnravel()) == MatriarchMood.COMPOSED ? MatAnim.EXP_01 : MatAnim.EXP_10, attack.prep + 10);
                } else {
                    this.play(MatAnim.THREAD_21, 0, right);
                }
                this.lookAt(p, LookMode.PUPPET, 12);
            }
            case EXECUTE -> this.play(threadBlow(attack.style), 0, right);
            case RECOVER -> this.play(MatAnim.THREAD_27, 0, right);
            default -> {
            }
        }
    }

    /** Which thread gesture drives a puppet's blow of this style. */
    public static MatAnim threadBlow(PuppetAttack.Style style) {
        return switch (style) {
            case CHARGE, LEAP, BURROW -> MatAnim.THREAD_24;
            case CONE, MULTI -> MatAnim.THREAD_23;
            case FAN, WIRE -> MatAnim.THREAD_22;
            case RING, WAVES, STORM -> MatAnim.THREAD_28;
            case GRAPPLE -> MatAnim.THREAD_26;
            case TRAP -> MatAnim.THREAD_25;
            case SIDESTEP -> MatAnim.THREAD_13;
        };
    }

    public void onPuppetAttackEnded(PuppetEntity p, PuppetAttack attack, boolean hit) {
        if (hit && this.tauntCooldown <= 0 && this.bodyFree() && this.anim[BODY] == null) {
            this.play(MatAnim.IDLE_10_CLAP);
            this.express(MatAnim.EXP_02, MatAnim.IDLE_10_CLAP.ticks);
            this.tauntCooldown = 360 + this.w.getRandom().nextInt(200);
        } else if (!hit) {
            this.play(MatAnim.THREAD_16, 0, this.primaryRight);
            if (MatriarchMood.of(this.w.getUnravel()) != MatriarchMood.COMPOSED) {
                this.express(MatAnim.EXP_05, 30);
            }
        }
        this.setHandLoops(this.handLoop[0], this.handLoop[1]);
    }

    public void onPuppetTangled(PuppetEntity p) {
        this.play(MatAnim.THREAD_20, 0, this.primaryRight);
        this.lookAt(p, LookMode.PUPPET, 40);
        this.play(MatAnim.IDLE_05);
        this.express(MatriarchMood.of(this.w.getUnravel()) == MatriarchMood.COMPOSED ? MatAnim.EXP_04 : MatAnim.EXP_06, 50);
    }

    public void onPuppetRecovered(PuppetEntity p) {
        this.play(MatAnim.THREAD_19, 0, this.primaryRight);
    }

    public void onPuppetHurt(PuppetEntity p) {
        if (this.lookOverrideTicks <= 0 && this.w.getRandom().nextInt(3) == 0) {
            this.lookAt(p, LookMode.PUPPET, 20);
        }
    }

    public void onPuppetDefeated(PuppetEntity p) {
        boolean right = this.primaryRight;
        this.setHandLoops(null, this.w.getCurrentStage() < 10 ? MatAnim.THREAD_10 : null);
        this.sequence(right, MatAnim.THREAD_20, MatAnim.THREAD_15);
        this.lookAt(p, LookMode.PUPPET, 40);
        this.play(MatAnim.IDLE_05);
        this.express(MatriarchMood.of(this.w.getUnravel()) == MatriarchMood.COMPOSED ? MatAnim.EXP_03 : MatAnim.EXP_06, 45);
    }

    public void onLiftPuppet(PuppetEntity p) {
        this.play(MatAnim.THREAD_14, 0, this.primaryRight);
        this.lookAt(p, LookMode.PUPPET, 20);
    }

    public void onPullDown(LivingEntity target) {
        this.play(MatAnim.THREAD_26, 0, !this.primaryRight);
        this.lookAt(target, LookMode.TARGET, 24);
        this.express(MatAnim.EXP_03, 30);
    }

    public void onReset() {
        this.play(MatAnim.THREAD_18, 0, false);
        this.play(MatAnim.THREAD_18, 0, true);
        this.express(MatAnim.EXP_04, 60);
    }

    /** The player is walking about while she directs: the free hand swings, the other keeps the puppet (MAT-THREAD-12). */
    public void onMoving(boolean moving) {
        if (!this.w.getPhase().isPuppetStage() || this.handLoop[0] == null && this.handLoop[1] == null) {
            return;
        }
        int h = this.primaryRight ? 1 : 0;
        MatAnim primary = moving ? MatAnim.THREAD_12 : this.w.getCurrentStage() < 10 ? MatAnim.THREAD_09 : MatAnim.THREAD_11;
        MatAnim secondary = moving ? null : (this.w.getCurrentStage() < 10 ? MatAnim.THREAD_10 : null);
        if (h == 0) {
            this.setHandLoops(primary, secondary);
        } else {
            this.setHandLoops(secondary == MatAnim.THREAD_10 ? MatAnim.THREAD_12 : null, primary);
        }
    }

    // ---------------------------------------------------------------- the last phase: attacks, defence, reactions
    public void onAttackPhase(PuppetAttack attack, Phase phase) {
        MatriarchMoveset.Look look = MatriarchMoveset.look(attack);
        switch (phase) {
            case PREPARE -> {
                this.playAction(look.prep());
                this.express(attack.special ? MatAnim.EXP_10 : MatAnim.EXP_07, attack.prep + attack.exec);
            }
            case EXECUTE -> {
                if (look.exec() != look.prep()) {
                    this.playAction(look.exec());
                }
            }
            case RECOVER -> {
                if (look.recover() != null) {
                    this.playAction(look.recover());
                }
            }
            default -> {
            }
        }
    }

    private void playAction(MatAnim a) {
        if (a.slot == MatSlot.HAND_L) {
            this.play(a, 0, false);
        } else {
            this.play(a);
        }
    }

    /** The preparation was called off (MAT-ATK-05): the target left before the commit. */
    public void onAttackCancelled() {
        this.play(MatAnim.ATK_05);
        this.stop(MatSlot.HAND_L);
    }

    /** The storm missed and left her exhausted (MAT-STUN-04, then MAT-STUN-05). */
    public void onTangled(int ticks) {
        this.stunTicks = ticks;
        this.start(BODY, MatAnim.STUN_04, 0);
        this.express(MatAnim.EXP_09, ticks);
    }

    /**
     * Tries a defence against an incoming blow in the last phase. Returns the share of the damage that still lands (1 = no
     * defence). Melee from the front: a hand parry (light blows) or the crossed arms (heavier ones); a projectile: a barrier
     * of threads or a dodge. The counter-attack that each defence opens is returned through {@link #counterFor}.
     */
    public float defend(DamageSource source, float amount) {
        if (this.defenseCooldown > 0 || !this.bodyFree() || this.incapacitated() || this.w.getRunner().isBusy()) {
            return 1.0F;
        }
        Entity attacker = source.getEntity();
        Entity direct = source.getDirectEntity();
        if (!(attacker instanceof Player) || this.w.getRandom().nextFloat() > DEFENSE_CHANCE + DEFENSE_CHANCE_RISE * MatriarchMood.strangeness(this.w.getUnravel())) {
            return 1.0F;
        }
        Vec3 from = source.getSourcePosition();
        boolean front = from == null || frontal(this.w, from);
        this.defenseCooldown = DEFENSE_COOLDOWN;
        this.express(MatAnim.EXP_03, 30);
        if (direct instanceof Projectile) {
            float s = MatriarchMood.strangeness(this.w.getUnravel());
            MatAnim dodge = this.w.getRandom().nextBoolean() ? MatAnim.DEF_03 : s > 0.75F ? MatAnim.DEF_07 : MatAnim.DEF_05;
            boolean toRight = this.w.getRandom().nextBoolean();
            // the dodges are authored to her left; a dodge to the right plays mirrored
            this.play(dodge, dodge != MatAnim.DEF_03 && toRight ? MatAnim.Variant.MIRROR : 0, false);
            if (dodge != MatAnim.DEF_03) {
                Vec3 side = Vec3.directionFromRotation(0.0F, this.w.getYRot() + (toRight ? 90.0F : -90.0F));
                this.w.setDeltaMovement(side.x * 0.55D, 0.15D, side.z * 0.55D);
                this.w.hurtMarked = true;
            }
            return 0.0F;
        }
        if (!front) {
            return 1.0F;
        }
        if (amount < 7.0F) {
            this.play(MatAnim.DEF_02);
            this.queue[BODY].add(new int[] {MatAnim.DEF_09.ordinal(), 0});
            return 0.0F;
        }
        this.play(MatAnim.DEF_01);
        this.queue[BODY].add(new int[] {MatAnim.DEF_09.ordinal(), 0});
        return 0.5F;
    }

    /** A dodge backwards when a player crowds her (MAT-DEF-06), turning aggressive as she unravels (MAT-DEF-08). */
    public boolean dodgeBack(LivingEntity crowding) {
        if (this.defenseCooldown > 0 || !this.bodyFree() || this.incapacitated() || this.w.getRunner().isBusy()) {
            return false;
        }
        boolean aggressive = MatriarchMood.strangeness(this.w.getUnravel()) > 0.5F && this.w.getRandom().nextBoolean();
        this.play(aggressive ? MatAnim.DEF_08 : MatAnim.DEF_06);
        Vec3 away = this.w.position().subtract(crowding.position()).multiply(1.0D, 0.0D, 1.0D);
        away = away.lengthSqr() > 1.0E-4D ? away.normalize() : Vec3.directionFromRotation(0.0F, this.w.getYRot() + 180.0F);
        this.w.setDeltaMovement(away.x * 0.6D, 0.18D, away.z * 0.6D);
        this.w.hurtMarked = true;
        this.defenseCooldown = 70;
        return aggressive;
    }

    /** The counter-attack opened by the defence playing now, or null. */
    public @Nullable PuppetAttack counterFor() {
        MatAnim body = this.anim[BODY];
        if (body == MatAnim.DEF_02) return MatriarchMoveset.PARRY_RIPOSTE;
        if (body == MatAnim.DEF_01) return MatriarchMoveset.GUARD_CRUSH;
        if (body == MatAnim.DEF_03) return MatriarchMoveset.BARRIER_LANCE;
        if (body == MatAnim.DEF_08) return MatriarchMoveset.DODGE_COUNTER;
        return null;
    }

    /**
     * A blow landed in the last phase. Strong enough, it knocks her down (MAT-FALL); a hit in the wind-up interrupts it
     * (MAT-STUN-01); a heavy one staggers her (MAT-STUN-02/03); anything else is a reaction added on top of what plays
     * (MAT-HIT), so the threads in her hands are not lost. Returns true when her current attack must be cancelled.
     */
    public boolean onHurt(DamageSource source, float amount) {
        Vec3 from = source.getSourcePosition();
        boolean behind = from != null && !frontal(this.w, from) && behindOf(this.w, from);
        float s = MatriarchMood.strangeness(this.w.getUnravel());
        MatAnim body = this.anim[BODY];
        boolean recovering = body == MatAnim.FALL_05 || body == MatAnim.FALL_06 || body == MatAnim.FALL_07 || body == MatAnim.FALL_09;
        if (recovering && !this.recoveryInterrupted && this.age[BODY] < body.ticks * 0.6F) {
            this.recoveryInterrupted = true; // MAT-FALL-08: knocked back down mid-recovery, once
            this.start(BODY, MatAnim.FALL_08, this.prone ? MatAnim.Variant.PRONE : 0);
            return true;
        }
        if (body != null && body.priority <= 4) {
            this.play(MatAnim.HIT_01);
            return false;
        }
        if (amount >= KNOCKDOWN_DAMAGE + KNOCKDOWN_RISE * s && this.knockdownCooldown <= 0) {
            this.knockdownCooldown = KNOCKDOWN_COOLDOWN;
            this.prone = behind;
            int v = (behind ? MatAnim.Variant.PRONE : 0) | (amount >= 20.0F ? MatAnim.Variant.FAST : amount < 14.0F ? MatAnim.Variant.SLOW : 0);
            MatAnim fall = s > 0.8F ? MatAnim.FALL_03 : behind ? MatAnim.FALL_01 : MatAnim.FALL_02;
            this.queue[BODY].clear();
            this.start(BODY, fall, v);
            this.recoveryInterrupted = false;
            this.express(MatAnim.EXP_09, 60);
            return true;
        }
        Phase phase = this.w.getRunner().phase();
        if (phase == Phase.PREPARE && amount >= INTERRUPT_DAMAGE && this.stunCooldown <= 0) {
            this.stunCooldown = STUN_COOLDOWN;
            this.start(BODY, MatAnim.STUN_01, 0);
            this.stunTicks = MatAnim.STUN_01.ticks;
            this.express(MatAnim.EXP_05, 20);
            return true;
        }
        if (amount >= STAGGER_DAMAGE && phase != Phase.EXECUTE && this.stunCooldown <= 0) {
            this.stunCooldown = STUN_COOLDOWN;
            MatAnim stun = s > 0.7F ? MatAnim.STUN_03 : MatAnim.STUN_02;
            this.start(BODY, stun, 0);
            this.stunTicks = stun.ticks;
            this.express(MatAnim.EXP_09, stun.ticks);
            return true;
        }
        // reactions on top of the rest
        if (this.clock - this.lastHitAge > 40) {
            this.recentHits = 0;
        }
        this.recentHits++;
        this.lastHitAge = this.clock;
        MatAnim hit;
        if (this.recentHits >= 3) {
            hit = MatAnim.HIT_08;
            this.recentHits = 0;
        } else if (behind) {
            hit = MatAnim.HIT_04;
        } else if (phase == Phase.EXECUTE || phase == Phase.RECOVER) {
            hit = MatAnim.HIT_06;
        } else if (this.anim[HAND_L] != null || this.anim[HAND_R] != null) {
            hit = MatAnim.HIT_05;
        } else if (this.w.isNoGravity() || this.w.getDeltaMovement().horizontalDistanceSqr() > 0.02D) {
            hit = MatAnim.HIT_07;
        } else {
            hit = amount < 4.0F ? MatAnim.HIT_01 : amount < 8.0F ? MatAnim.HIT_02 : MatAnim.HIT_03;
        }
        this.play(hit);
        this.express(s > 0.6F ? MatAnim.EXP_08 : MatAnim.EXP_05, 24);
        return false;
    }

    private void beginRecovery() {
        float s = MatriarchMood.strangeness(this.w.getUnravel());
        RandomSource r = this.w.getRandom();
        MatAnim rec = r.nextInt(10) < 3 && s < 0.9F ? MatAnim.FALL_09 : s < 0.35F ? MatAnim.FALL_05 : s < 0.7F ? MatAnim.FALL_06 : MatAnim.FALL_07;
        this.start(BODY, rec, this.prone ? MatAnim.Variant.PRONE : 0);
    }

    public static boolean frontal(Entity self, Vec3 from) {
        Vec3 to = from.subtract(self.position()).multiply(1.0D, 0.0D, 1.0D);
        if (to.lengthSqr() < 1.0E-4D) {
            return true;
        }
        Vec3 f = Vec3.directionFromRotation(0.0F, self instanceof LivingEntity l ? l.yBodyRot : self.getYRot());
        return to.normalize().dot(f) > 0.2D;
    }

    private static boolean behindOf(Entity self, Vec3 from) {
        Vec3 to = from.subtract(self.position()).multiply(1.0D, 0.0D, 1.0D);
        Vec3 f = Vec3.directionFromRotation(0.0F, self instanceof LivingEntity l ? l.yBodyRot : self.getYRot());
        return to.lengthSqr() > 1.0E-4D && to.normalize().dot(f) < -0.5D;
    }

    /** Hits in the cocoon (puppet stages): she cannot be hurt, but she shows how she wards the blow off (MAT-DEF-01..04). */
    public void onCocoonHit(DamageSource source, @Nullable PuppetEntity active) {
        if (this.defenseCooldown > 0 || !this.bodyFree() || !(source.getEntity() instanceof Player)) {
            return;
        }
        this.defenseCooldown = 40;
        MatAnim def;
        if (source.getDirectEntity() instanceof Projectile) {
            def = MatAnim.DEF_03;
        } else if (active != null && active.distanceTo(this.w) < 7.0F && this.w.getRandom().nextInt(4) == 0) {
            def = MatAnim.DEF_04;
            this.lookAt(active, LookMode.PUPPET, 20);
        } else {
            def = MatriarchMood.of(this.w.getUnravel()) == MatriarchMood.COMPOSED ? MatAnim.DEF_02 : MatAnim.DEF_01;
        }
        if (this.play(def)) {
            this.queue[BODY].add(new int[] {MatAnim.DEF_09.ordinal(), 0});
        }
        this.express(MatAnim.EXP_03, 30);
    }

    /** Debug and test access: lists the clips playing now, slot by slot. */
    public List<String> describe() {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        for (int i = 0; i < SLOTS; i++) {
            out.add(MatSlot.SYNCHED[i] + "=" + (this.anim[i] == null ? "-" : this.anim[i].code + "@" + this.age[i]));
        }
        return out;
    }

    public float stunRemaining() {
        return Mth.clamp(this.stunTicks, 0, 1000);
    }
}
