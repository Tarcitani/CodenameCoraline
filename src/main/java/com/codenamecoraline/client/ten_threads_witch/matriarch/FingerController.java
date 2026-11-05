package com.codenamecoraline.client.ten_threads_witch.matriarch;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchMood;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * MAT-IDLE-07: each of her ten fingers moves on its own, all the time, on top of whatever the clips make the hand do. Every
 * finger has its own slow drift (two incommensurate waves with its own phases), and now and then a hand plays a small
 * figure: one finger curls and releases, the fingers part and close, a ripple runs from the index to the little finger, the
 * hand tenses. The two hands never play the same figure at once. With her mood the drift widens, the figures come more
 * often and a tremor appears; at the end the curls become twitches. Added after composition, damped where a strong clip
 * owns the hand (a fall, a cinematic, the death), and halved while a precise thread gesture plays.
 */
public final class FingerController {
    private enum Figure { NONE, CURL, PART, RIPPLE, TENSE, TWITCH }

    private static final Figure[] EARLY = {Figure.CURL, Figure.PART, Figure.RIPPLE, Figure.TENSE};
    private static final Figure[] LATE = {Figure.TWITCH, Figure.TWITCH, Figure.RIPPLE, Figure.TENSE};
    private final float[] rot = new float[3];

    private final float[] phaseA = new float[10];
    private final float[] phaseB = new float[10];
    private final float[] freqA = new float[10];
    private final float[] freqB = new float[10];
    private final Figure[] figure = {Figure.NONE, Figure.NONE};
    private final int[] figureFinger = new int[2];
    private final float[] figureStart = new float[2];
    private final float[] nextFigure = new float[2];
    private final RandomSource random;

    public FingerController(long seed) {
        this.random = RandomSource.create(seed);
        for (int i = 0; i < 10; i++) {
            this.phaseA[i] = this.random.nextFloat() * Mth.TWO_PI;
            this.phaseB[i] = this.random.nextFloat() * Mth.TWO_PI;
            this.freqA[i] = 0.05F + this.random.nextFloat() * 0.04F;
            this.freqB[i] = 0.11F + this.random.nextFloat() * 0.07F;
        }
        this.nextFigure[0] = 20.0F + this.random.nextFloat() * 40.0F;
        this.nextFigure[1] = 50.0F + this.random.nextFloat() * 40.0F;
    }

    /** The figure a hand is playing (for tests): 0 none. */
    public int figure(int hand) {
        return this.figure[hand].ordinal();
    }

    /** {@code freedom}: 1 for a free hand, about 0.5 while a precise gesture plays on it. */
    public void update(PoseMixer mixer, float now, float unravel, float freedomL, float freedomR) {
        float strange = MatriarchMood.strangeness(unravel);
        float drift = 3.0F + 4.0F * Math.min(1.0F, unravel / 0.6F) + 3.0F * strange;
        float tremor = strange > 0.3F ? (strange - 0.3F) * 4.0F : 0.0F;
        for (int hand = 0; hand < 2; hand++) {
            if (now >= this.nextFigure[hand] && this.figure[hand] == Figure.NONE) {
                Figure[] options = strange > 0.6F ? LATE : EARLY;
                Figure f = options[this.random.nextInt(options.length)];
                if (f == this.figure[1 - hand]) {
                    f = Figure.CURL; // the hands never mirror each other
                }
                this.figure[hand] = f;
                this.figureFinger[hand] = this.random.nextInt(4);
                this.figureStart[hand] = now;
            }
            float age = now - this.figureStart[hand];
            float len = length(this.figure[hand]);
            if (this.figure[hand] != Figure.NONE && age > len) {
                this.figure[hand] = Figure.NONE;
                float gap = (60.0F + this.random.nextFloat() * 80.0F) * (1.0F - 0.55F * strange);
                this.nextFigure[hand] = now + gap;
            }
            int[] joints = hand == 0 ? MatRig.FINGERS_L : MatRig.FINGERS_R;
            char side = hand == 0 ? 'l' : 'r';
            float freedom = hand == 0 ? freedomL : freedomR;
            for (int f = 0; f < 5; f++) {
                int i = hand * 5 + f;
                float flex = drift * (0.6F * Mth.sin(now * this.freqA[i] + this.phaseA[i]) + 0.4F * Mth.sin(now * this.freqB[i] + this.phaseB[i]));
                flex += tremor * Mth.sin(now * 2.9F + i * 1.3F);
                float spread = 0.0F;
                flex += this.figureFlex(hand, f, age, len);
                if (this.figure[hand] == Figure.PART && f < 4) {
                    spread = 7.0F * bell(age / len);
                }
                float[] r = ClipBuilder.fingerRot(side, f, flex, f < 4 ? spread * (f - 1.5F) : 0.0F, this.rot);
                mixer.addAfter(joints[f] * 3 + MatRig.ROT, r[0] * Mth.DEG_TO_RAD, r[1] * Mth.DEG_TO_RAD, r[2] * Mth.DEG_TO_RAD, freedom);
            }
        }
    }

    private float figureFlex(int hand, int f, float age, float len) {
        float u = len > 0.0F ? Mth.clamp(age / len, 0.0F, 1.0F) : 0.0F;
        return switch (this.figure[hand]) {
            case CURL -> f == this.figureFinger[hand] ? 32.0F * bell(u) : 0.0F;
            case RIPPLE -> f < 4 ? 22.0F * bell(Mth.clamp(u * 1.6F - f * 0.15F, 0.0F, 1.0F)) : 0.0F;
            case TENSE -> -10.0F * bell(u);
            case TWITCH -> f == this.figureFinger[hand] || f == (this.figureFinger[hand] + 2) % 5 ? (u < 0.15F ? 40.0F : u < 0.3F ? -10.0F : 0.0F) : 0.0F;
            default -> 0.0F;
        };
    }

    private static float length(Figure f) {
        return switch (f) {
            case CURL -> 16.0F;
            case PART -> 20.0F;
            case RIPPLE -> 22.0F;
            case TENSE -> 14.0F;
            case TWITCH -> 8.0F;
            case NONE -> 0.0F;
        };
    }

    /** 0 at both ends, 1 in the middle, smooth. */
    private static float bell(float u) {
        float s = Mth.sin(Mth.clamp(u, 0.0F, 1.0F) * Mth.PI);
        return s * s;
    }
}
