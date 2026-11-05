package com.codenamecoraline.client.ten_threads_witch.matriarch;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatAnim;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatClipRef;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatSlot;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchAnimSync;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchMood;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The client controller of one Matriarch: every frame it composes, joint by joint, the layers of her animation into one
 * pose. Order of submission does not matter (the {@link PoseMixer} sorts by priority); the layers are the breathing loop of
 * her mood, the locomotion, the five synched slots (face, both hands, body, reactions) with their cross-fades, the gaze,
 * and, after composition, the fingers' own motion and the cloth and hair. It reads only plain inputs ({@link Input}), so the
 * same code runs in the renderer and in the GameTests that check it.
 */
public final class MatriarchAnimator {
    /** What the animator needs to know about the entity this frame. */
    public static final class Input {
        public float now;
        public MatriarchAnimSync sync = new MatriarchAnimSync();
        public double vx;
        public double vy;
        public double vz;
        public float bodyYaw;
        public float yawRate;
        public boolean floating;
        public int style;
        public float unravel;
        public @Nullable Vec3 look;
        public boolean concealed;
    }

    private static final int[] SLOT_RANK = {40, 60, 31, 30, 20};

    private final MatClips clips = MatClips.get();
    private final PoseMixer mixer = new PoseMixer();
    private final PoseSample a = new PoseSample();
    private final PoseSample b = new PoseSample();
    private final GaitController gait = new GaitController();
    private final GazeController gaze = new GazeController();
    private final FingerController fingers;
    private final SecondaryMotion cloth = new SecondaryMotion();
    private MatAnim base;
    private MatAnim prevBase;
    private float baseSwitch = -100.0F;
    private float lookWeight = 1.0F;

    public MatriarchAnimator(long seed) {
        this.fingers = new FingerController(seed);
    }

    public PoseMixer mixer() {
        return this.mixer;
    }

    public GaitController gait() {
        return this.gait;
    }

    public GazeController gaze() {
        return this.gaze;
    }

    public FingerController fingers() {
        return this.fingers;
    }

    public SecondaryMotion cloth() {
        return this.cloth;
    }

    /** The share of freedom the body clip leaves to the gaze this frame. */
    public float lookWeight() {
        return this.lookWeight;
    }

    /** Composes the pose of this frame; the result is in {@code mixer().out}. */
    public float[] animate(Input in) {
        this.mixer.begin();
        this.baseLayer(in);
        this.gait.update(this.clips, this.mixer, this.a, this.b, in.vx, in.vy, in.vz, in.bodyYaw, in.floating, in.style, in.unravel, in.now);
        float bodyLook = 1.0F;
        for (int slot = MatSlot.SYNCHED.length - 1; slot >= 0; slot--) {
            float lw = this.slotLayer(slot, in);
            if (slot == 0) {
                bodyLook = lw;
            }
        }
        this.lookWeight = bodyLook;
        this.gaze.update(this.mixer, in.look, in.bodyYaw, bodyLook, in.unravel, in.now);
        this.mixer.resolve();
        float freeL = this.freedom(in, 2);
        float freeR = this.freedom(in, 3);
        this.fingers.update(this.mixer, in.now, in.unravel, freeL, freeR);
        float yaw = in.bodyYaw * Mth.DEG_TO_RAD;
        float fwd = (float) (in.vx * -Mth.sin(yaw) + in.vz * Mth.cos(yaw));
        float side = (float) (in.vx * Mth.cos(yaw) + in.vz * Mth.sin(yaw));
        float[] o = this.mixer.out;
        this.cloth.update(this.mixer, in.now, fwd, side, (float) in.vy, in.yawRate, in.floating, in.unravel,
                o[(MatRig.CHEST * 3 + MatRig.ROT) * 3], o[(MatRig.CHEST * 3 + MatRig.ROT) * 3 + 1], o[(MatRig.BODY * 3 + MatRig.ROT) * 3]);
        if (in.concealed) {
            o[(MatRig.DISSOLVE * 3 + MatRig.POS) * 3 + 1] = 1.0F; // not yet manifested: the entrance has not begun
        }
        return o;
    }

    // ---------------------------------------------------------------- MAT-IDLE-01: the breathing of her mood
    private void baseLayer(Input in) {
        MatAnim want = switch (MatriarchMood.of(in.unravel)) {
            case COMPOSED -> MatAnim.IDLE_01_COMPOSED;
            case IRRITATED -> MatAnim.IDLE_01_IRRITATED;
            case UNRAVELLING -> MatAnim.IDLE_01_UNRAVELLING;
        };
        if (want != this.base) {
            this.prevBase = this.base;
            this.base = want;
            this.baseSwitch = in.now;
        }
        float f = smooth((in.now - this.baseSwitch) / 20.0F);
        if (this.prevBase != null && f < 1.0F) {
            this.b.clear();
            this.clips.get(this.prevBase, false, false).sample(in.now / 20.0F, 1.0F, this.b);
            this.mixer.submit(this.b, 1.0F - f, 0);
        }
        this.a.clear();
        this.clips.get(this.base, false, false).sample(in.now / 20.0F, amplitude(this.base, in.unravel), this.a);
        this.mixer.submit(this.a, f, 1);
    }

    // ---------------------------------------------------------------- the synched slots, with cross-fades
    private float slotLayer(int slot, Input in) {
        MatriarchAnimSync s = in.sync;
        MatAnim cur = MatClipRef.anim(s.value[slot]);
        MatAnim prev = MatClipRef.anim(s.prevValue[slot]);
        int curVar = MatClipRef.variant(s.value[slot]);
        int prevVar = MatClipRef.variant(s.prevValue[slot]);
        float sinceSwitch = in.now - s.switched[slot];
        float fadeIn = cur != null ? blendIn(cur) : blendOut(prev);
        float k = smooth(sinceSwitch / fadeIn);
        boolean right = slot == 3;
        float curW = 0.0F;
        float lookCur = 1.0F;
        this.a.clear();
        this.b.clear();
        if (cur != null) {
            float age = in.now - s.start[slot];
            float clipTick = MatAnim.clipTick(age, curVar);
            float endFade = 1.0F;
            if (!cur.loops() && clipTick > cur.ticks && !cur.has(MatAnim.Flags.HOLD)) {
                endFade = 1.0F - smooth((clipTick - cur.ticks) / blendOut(cur));
            }
            curW = endFade;
            lookCur = endFade * cur.lookWeight + (1.0F - endFade);
            if (endFade > 0.0F) {
                this.clips.get(cur, mirrored(cur, curVar, right), (curVar & MatAnim.Variant.PRONE) != 0)
                        .sample(this.warp(cur, clipTick, in.unravel) / 20.0F, amplitude(cur, in.unravel), this.a);
            }
        }
        float lookPrev = 1.0F;
        if (prev != null && k < 1.0F) {
            // the previous clip keeps playing on its own clock while it fades (a one-shot holds its last frame)
            float clipTick = MatAnim.clipTick(in.now - s.prevStart[slot], prevVar);
            this.clips.get(prev, mirrored(prev, prevVar, right), (prevVar & MatAnim.Variant.PRONE) != 0)
                    .sample(this.warp(prev, clipTick, in.unravel) / 20.0F, amplitude(prev, in.unravel), this.b);
            lookPrev = prev.lookWeight;
        }
        int rank = SLOT_RANK[slot];
        // cross-fade inside the slot: where both clips move a channel, blend between them; elsewhere fade each one alone
        for (int i = 0; i < this.a.count; i++) {
            int ch = this.a.used[i];
            if (this.b.set[ch] && k < 1.0F) {
                float x = lerp(this.b.value[ch * 3], this.a.value[ch * 3], k);
                float y = lerp(this.b.value[ch * 3 + 1], this.a.value[ch * 3 + 1], k);
                float z = lerp(this.b.value[ch * 3 + 2], this.a.value[ch * 3 + 2], k);
                this.mixer.submit(ch, x, y, z, curW, this.a.priority[ch], this.a.additive[ch], rank);
            } else {
                this.mixer.submit(ch, this.a.value[ch * 3], this.a.value[ch * 3 + 1], this.a.value[ch * 3 + 2], curW * k, this.a.priority[ch], this.a.additive[ch], rank);
            }
        }
        if (k < 1.0F) {
            for (int i = 0; i < this.b.count; i++) {
                int ch = this.b.used[i];
                if (!this.a.set[ch]) {
                    this.mixer.submit(ch, this.b.value[ch * 3], this.b.value[ch * 3 + 1], this.b.value[ch * 3 + 2], 1.0F - k, this.b.priority[ch], this.b.additive[ch], rank);
                }
            }
        }
        if (cur == null && prev == null) {
            return 1.0F;
        }
        float lc = cur != null ? lookCur : 1.0F;
        return prev != null && k < 1.0F ? lerp(lookPrev, lc, k) : lc;
    }

    private static boolean mirrored(MatAnim a, int variant, boolean rightSlot) {
        return (variant & MatAnim.Variant.MIRROR) != 0 || (rightSlot && !a.has(MatAnim.Flags.BOTH));
    }

    private float freedom(Input in, int slot) {
        MatAnim cur = MatClipRef.anim(in.sync.value[slot]);
        if (cur == null) {
            return 1.0F;
        }
        return cur == MatAnim.THREAD_13 || cur == MatAnim.THREAD_06 ? 0.4F : 0.75F;
    }

    // ---------------------------------------------------------------- mood: amplitude and the hitched rhythm
    /** Gestures widen with irritation and then a little more as she loses control (only clips flagged MOOD). */
    public static float amplitude(MatAnim a, float unravel) {
        if (!a.has(MatAnim.Flags.MOOD)) {
            return 1.0F;
        }
        return 1.0F + 0.28F * Math.min(1.0F, unravel / MatriarchMood.UNRAVELLING_FROM) + 0.17F * MatriarchMood.strangeness(unravel);
    }

    /**
     * The unnatural rhythm of her later stages: time is cut into cells of three ticks, and inside each cell the motion
     * stalls and then catches up, so the pose holds and jumps instead of flowing. Monotonic, and exact at every cell edge,
     * so a clip still ends on time.
     */
    public float warp(MatAnim a, float tick, float unravel) {
        float strange = MatriarchMood.strangeness(unravel);
        if (!a.has(MatAnim.Flags.MOOD) || strange <= 0.05F) {
            return tick;
        }
        float cell = 3.0F;
        float start = (float) Math.floor(tick / cell) * cell;
        float u = (tick - start) / cell;
        float power = 1.0F + 2.5F * strange;
        return start + cell * (float) Math.pow(u, power);
    }

    static float blendIn(MatAnim a) {
        if (a == null) {
            return 6.0F;
        }
        return switch (a.slot) {
            case REACT -> 1.5F;
            case FACE -> 6.0F;
            case HAND_L, HAND_R -> a.priority <= 7 ? 2.0F : 4.0F;
            default -> a.priority <= 2 ? 3.0F : a.priority <= 5 ? 2.0F : a.priority == 7 ? 2.5F : a.priority == 3 ? 4.0F : 7.0F;
        };
    }

    static float blendOut(MatAnim a) {
        if (a == null) {
            return 6.0F;
        }
        return a.slot == MatSlot.REACT ? 5.0F : a.priority <= 7 ? 5.0F : 8.0F;
    }

    private static float smooth(float t) {
        float c = Mth.clamp(t, 0.0F, 1.0F);
        return c * c * (3.0F - 2.0F * c);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
