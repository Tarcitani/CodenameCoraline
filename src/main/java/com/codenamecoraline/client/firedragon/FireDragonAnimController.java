package com.codenamecoraline.client.firedragon;

import com.codenamecoraline.entity.firedragon.FireDragon;
import com.codenamecoraline.entity.firedragon.FireDragonAnimation;
import java.util.WeakHashMap;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Client-only procedural & blended animation controller for FireDragon (Baby & Young).
 * Keeps server mechanics untouched while providing smooth state cross-fading, asymmetric
 * articulated wingbeats, gliding, flight banking, climb/dive pitch, quadruped walk/run
 * blending, idle breathing/curiosity and delayed neck/tail secondary motion.
 */
final class FireDragonAnimController {
    private static final float DEG = Mth.DEG_TO_RAD;
    private static final WeakHashMap<FireDragon, ClientState> STATES = new WeakHashMap<>();

    private FireDragonAnimController() {}

    /** Stage-tuned animation parameters (Baby = agile/curious, Young = heavier/stronger; structured for future Adult). */
    record StageParams(
            float wingRateRadPerTick,
            float wingAmpScale,
            float bodyHeaveScale,
            float walkAmpScale,
            float tailWaveAmp,
            float tailLagRate,
            float neckLagRate,
            float curiosityRate
    ) {
        static final StageParams BABY = new StageParams(0.36F, 0.92F, 1.0F, 1.05F, 1.10F, 0.24F, 0.28F, 1.15F);
        static final StageParams YOUNG = new StageParams(0.26F, 1.08F, 1.25F, 0.95F, 1.00F, 0.16F, 0.20F, 0.78F);
        // Adult params reserved for when Stage.ADULT is added to FireDragon.
        static final StageParams ADULT = new StageParams(0.20F, 1.16F, 1.45F, 0.85F, 0.90F, 0.12F, 0.14F, 0.55F);

        static StageParams of(boolean young) {
            return young ? YOUNG : BABY;
        }
    }

    /** Per-entity client-side smoothing state (GC'd automatically via WeakHashMap). */
    static final class ClientState {
        float lastAge = -1.0F;
        float wingPhase;
        float walkWeight;
        float runWeight;
        float airWeight;
        float glideWeight;
        float sitWeight;
        float smoothHorizSpeed;
        float smoothVertVel;
        float smoothTurnRate;
        final float[] tailYawLag = new float[4];
        final float[] tailPitchLag = new float[4];
        final float[] neckYawLag = new float[3];
        float[] prevX, prevY, prevZ, prevXR, prevYR, prevZR;
        boolean hasPrevPose;
        boolean prevYoung;
    }

    static ClientState stateFor(FireDragon entity) {
        return STATES.computeIfAbsent(entity, ignored -> new ClientState());
    }

    /** Updates smoothed kinematics & blend weights on the client during render-state extraction. */
    static void update(FireDragon entity, FireDragonRenderState out, float partialTick) {
        ClientState s = stateFor(entity);
        float age = entity.tickCount + partialTick;
        float dt = s.lastAge < 0.0F ? 0.0F : Mth.clamp(age - s.lastAge, 0.0F, 2.5F);
        s.lastAge = age;
        if (s.prevYoung != out.young) {
            s.hasPrevPose = false;
            s.prevYoung = out.young;
        }

        StageParams params = StageParams.of(out.young);
        var vel = entity.getDeltaMovement();
        float horizSpeed = (float) Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        float vertVel = (float) vel.y;
        float yawDelta = Mth.wrapDegrees(entity.yBodyRot - entity.yBodyRotO);

        float kinAlpha = 1.0F - (float) Math.exp(-0.28F * Math.max(dt, 0.05F));
        s.smoothHorizSpeed = Mth.lerp(kinAlpha, s.smoothHorizSpeed, horizSpeed);
        s.smoothVertVel = Mth.lerp(kinAlpha, s.smoothVertVel, vertVel);
        s.smoothTurnRate = Mth.lerp(kinAlpha, s.smoothTurnRate, Mth.clamp(yawDelta, -22.0F, 22.0F));

        boolean isAir = out.airborne && !out.perched && !out.sitting;
        boolean isSit = out.sitting && !out.perched;
        boolean isMoving = !isAir && !isSit && !out.perched && out.walkAnimationSpeed > 0.02F;
        float targetRun = isMoving ? Mth.clamp((out.walkAnimationSpeed - 0.42F) / 0.38F, 0.0F, 1.0F) : 0.0F;
        float targetWalk = isMoving ? Mth.clamp(out.walkAnimationSpeed * 1.4F, 0.0F, 1.0F) * (1.0F - targetRun) : 0.0F;
        // Glide smoothly when airborne, moving forward, and descending gently without climbing effort.
        boolean wantsGlide = isAir && s.smoothHorizSpeed > 0.14F && s.smoothVertVel <= -0.015F && s.smoothVertVel > -0.22F;

        float blendAlpha = 1.0F - (float) Math.exp(-0.24F * Math.max(dt, 0.05F));
        s.airWeight = Mth.lerp(blendAlpha, s.airWeight, isAir ? 1.0F : 0.0F);
        s.glideWeight = Mth.lerp(blendAlpha * 0.75F, s.glideWeight, wantsGlide ? 1.0F : 0.0F);
        s.sitWeight = Mth.lerp(blendAlpha, s.sitWeight, isSit ? 1.0F : 0.0F);
        s.walkWeight = Mth.lerp(blendAlpha, s.walkWeight, targetWalk);
        s.runWeight = Mth.lerp(blendAlpha, s.runWeight, targetRun);

        // Advance continuous wingbeat phase smoothly; slows down when gliding without ever resetting abruptly.
        float climbBoost = 1.0F + Mth.clamp(s.smoothVertVel * 1.8F, -0.15F, 0.35F);
        float flapActivity = Mth.lerp(s.glideWeight, 1.0F, 0.12F);
        s.wingPhase = (s.wingPhase + dt * params.wingRateRadPerTick() * climbBoost * flapActivity) % Mth.TWO_PI;

        // Progressive delayed secondary-motion buffers (neck follows head/turn, tail follows torso with cascade lag).
        float targetNeckYaw = Mth.clamp(out.yRot, -55.0F, 55.0F) * DEG;
        float turnRad = s.smoothTurnRate * DEG;
        float nRate = 1.0F - (float) Math.exp(-params.neckLagRate() * Math.max(dt, 0.05F));
        s.neckYawLag[0] = Mth.lerp(nRate, s.neckYawLag[0], targetNeckYaw * 0.35F + turnRad * 0.8F);
        s.neckYawLag[1] = Mth.lerp(nRate * 0.85F, s.neckYawLag[1], s.neckYawLag[0] * 0.9F + targetNeckYaw * 0.25F);
        s.neckYawLag[2] = Mth.lerp(nRate * 0.75F, s.neckYawLag[2], s.neckYawLag[1] * 0.85F + targetNeckYaw * 0.20F);

        float tRate = 1.0F - (float) Math.exp(-params.tailLagRate() * Math.max(dt, 0.05F));
        float baseTailYaw = -turnRad * 1.4F;
        float baseTailPitch = Mth.clamp(s.smoothVertVel * 0.65F, -0.25F, 0.25F);
        s.tailYawLag[0] = Mth.lerp(tRate, s.tailYawLag[0], baseTailYaw);
        s.tailPitchLag[0] = Mth.lerp(tRate, s.tailPitchLag[0], baseTailPitch);
        for (int i = 1; i < 4; i++) {
            float segRate = tRate * (0.82F - i * 0.08F);
            s.tailYawLag[i] = Mth.lerp(segRate, s.tailYawLag[i], s.tailYawLag[i - 1] * 1.08F);
            s.tailPitchLag[i] = Mth.lerp(segRate, s.tailPitchLag[i], s.tailPitchLag[i - 1] * 1.05F);
        }

        out.animState = s;
        out.dt = dt;
    }

    /**
     * Warps uniform phase [0..2pi] into asymmetric dragon wingbeat timing:
     * ~62% heavy powerful downstroke, ~38% fast folded recovery upstroke (matching breakdown sheet #2).
     */
    private static float asymmetricPhase(float phase) {
        float u = (phase % Mth.TWO_PI) / Mth.TWO_PI;
        if (u < 0.0F) u += 1.0F;
        // 0..0.62 -> downstroke (0..pi), 0.62..1.0 -> fast recovery upstroke (pi..2pi)
        return u < 0.62F
                ? (u / 0.62F) * Mth.PI
                : Mth.PI + ((u - 0.62F) / 0.38F) * Mth.PI;
    }

    /** Computes procedural blended base locomotion pose on the model's real 35-bone hierarchy. */
    static void applyLocomotion(FireDragonModel.Rig r, FireDragonRenderState state) {
        ClientState s = state.animState;
        if (s == null) return;
        StageParams p = StageParams.of(state.young);
        float t = state.ageInTicks;

        float idleW = Mth.clamp(1.0F - s.walkWeight - s.runWeight - s.airWeight - s.sitWeight, 0.0F, 1.0F);
        float groundMoveW = Mth.clamp(s.walkWeight + s.runWeight, 0.0F, 1.0F);

        // 1. Grounded Idle / Rest breathing + subtle non-repetitive curiosity
        if (idleW > 0.001F || s.sitWeight > 0.001F) {
            float breathFreq = s.sitWeight > 0.5F ? 0.095F : 0.145F;
            float breath = Mth.sin(t * breathFreq);
            float w = idleW + s.sitWeight * 0.75F;
            r.torso.xRot += breath * 1.8F * DEG * w;
            r.torso.y += -breath * 0.22F * w;
            r.belly.y += breath * 0.18F * w;

            // Asynchronous non-repetitive ambient head/neck glance when idle
            float glance = (Mth.sin(t * 0.031F * p.curiosityRate()) + 0.5F * Mth.sin(t * 0.073F * p.curiosityRate())) * idleW;
            float pitchGlance = Mth.sin(t * 0.047F * p.curiosityRate()) * idleW;
            r.neckBase.yRot += glance * 5.0F * DEG;
            r.neckMid.yRot += glance * 4.5F * DEG;
            r.neckTop.yRot += glance * 3.5F * DEG;
            r.head.yRot += glance * 6.0F * DEG;
            r.head.xRot += pitchGlance * 3.5F * DEG - breath * 1.5F * DEG * w;
            r.neckMid.xRot -= breath * 1.8F * DEG * w;

            // Gentle traveling tail wave on ground (slows calmly when seated)
            float tailSpeed = s.sitWeight > 0.5F ? 0.06F : 0.11F;
            float tailAmp = (s.sitWeight > 0.5F ? 3.0F : 7.5F) * p.tailWaveAmp() * (idleW + s.sitWeight * 0.5F);
            for (int i = 0; i < 4; i++) {
                r.tail[i].yRot += Mth.sin(t * tailSpeed - i * 0.75F) * tailAmp * (0.65F + i * 0.25F) * DEG;
            }
            // Subtle breathing settle on folded wings
            float wingSettle = breath * 1.6F * DEG * w;
            r.leftWingUpper.zRot += wingSettle;
            r.rightWingUpper.zRot -= wingSettle;
        }

        // 2. Seated / Resting posture (§11)
        if (s.sitWeight > 0.001F) {
            float sw = s.sitWeight;
            r.torso.xRot += -12.0F * DEG * sw;
            r.torso.y += 1.2F * sw;
            r.neckBase.xRot += 12.0F * DEG * sw;
            r.neckMid.xRot += -4.0F * DEG * sw;
            r.head.xRot += 4.0F * DEG * sw;
            r.leftHindUpper.xRot += -52.0F * DEG * sw;
            r.rightHindUpper.xRot += -52.0F * DEG * sw;
            r.leftHindLower.xRot += 74.0F * DEG * sw;
            r.rightHindLower.xRot += 74.0F * DEG * sw;
            r.leftHindFoot.xRot += -22.0F * DEG * sw;
            r.rightHindFoot.xRot += -22.0F * DEG * sw;
            r.leftFrontUpper.xRot += 10.0F * DEG * sw;
            r.rightFrontUpper.xRot += 10.0F * DEG * sw;
            r.tail[0].xRot += 10.0F * DEG * sw;
            r.tail[0].yRot += 20.0F * DEG * sw;
            r.tail[1].yRot += 24.0F * DEG * sw;
            r.tail[2].yRot += 22.0F * DEG * sw;
            r.tail[3].yRot += 16.0F * DEG * sw;
        }

        // 3. Quadruped Walk & Run with progressive blend (§3 & §4 — synced to real walkAnimationPos)
        if (groundMoveW > 0.001F) {
            float runFactor = groundMoveW > 0.001F ? s.runWeight / groundMoveW : 0.0F;
            float stridePos = state.walkAnimationPos * (1.25F + runFactor * 0.25F);
            float amp = groundMoveW * p.walkAmpScale() * (1.0F + runFactor * 0.55F);

            // Diagonal quadruped phases: FL (0), HR (0.4), FR (pi), HL (pi + 0.4)
            applyQuadrupedLeg(r.leftFrontUpper, r.leftFrontLower, r.leftFrontFoot, stridePos, amp, runFactor, false);
            applyQuadrupedLeg(r.rightHindUpper, r.rightHindLower, r.rightHindFoot, stridePos + 0.45F, amp, runFactor, true);
            applyQuadrupedLeg(r.rightFrontUpper, r.rightFrontLower, r.rightFrontFoot, stridePos + Mth.PI, amp, runFactor, false);
            applyQuadrupedLeg(r.leftHindUpper, r.leftHindLower, r.leftHindFoot, stridePos + Mth.PI + 0.45F, amp, runFactor, true);

            // Weight transfer, vertical bounce, and shoulder/hip roll
            float bob = Mth.abs(Mth.sin(stridePos));
            r.torso.y += (-bob * (0.45F + runFactor * 0.65F)) * groundMoveW;
            r.torso.xRot += (runFactor * 6.0F + Mth.cos(stridePos * 2.0F) * (2.0F + runFactor * 3.0F)) * DEG * groundMoveW;
            r.torso.zRot += Mth.sin(stridePos) * (2.8F + runFactor * 2.2F) * DEG * groundMoveW;

            // Neck & head compensate gait bounce smoothly
            r.neckBase.xRot += (runFactor * 8.0F - Mth.cos(stridePos * 2.0F - 0.5F) * (2.5F + runFactor * 3.0F)) * DEG * groundMoveW;
            r.head.xRot += (-runFactor * 6.0F + Mth.cos(stridePos * 2.0F - 1.0F) * 2.0F) * DEG * groundMoveW;

            // Balance tail sway with phase cascade
            float tailMoveAmp = (9.0F + runFactor * 8.0F) * p.tailWaveAmp() * groundMoveW;
            for (int i = 0; i < 4; i++) {
                r.tail[i].yRot += Mth.sin(stridePos - i * 0.65F) * tailMoveAmp * (0.75F + i * 0.20F) * DEG;
            }
            // During run, wings flare subtly outward for dynamic balance
            if (runFactor > 0.01F) {
                float flare = runFactor * groundMoveW;
                r.leftWingUpper.yRot += 14.0F * DEG * flare;
                r.rightWingUpper.yRot -= 14.0F * DEG * flare;
                r.leftWingUpper.zRot += (-8.0F + Mth.sin(stridePos * 2.0F) * 5.0F) * DEG * flare;
                r.rightWingUpper.zRot -= (-8.0F + Mth.sin(stridePos * 2.0F) * 5.0F) * DEG * flare;
            }
        }

        // 4. Flight (Articulated Asymmetric Wingbeat + Glide + Climb/Dive + Banking — §§6, 7, 8, 9)
        if (s.airWeight > 0.001F) {
            float aw = s.airWeight;
            float flapW = aw * (1.0F - s.glideWeight);
            float glideW = aw * s.glideWeight;
            float a = asymmetricPhase(s.wingPhase); // 0..pi downstroke, pi..2pi fast folded upstroke

            // Dive fold factor when plunging downward rapidly
            float diveFactor = Mth.clamp((-s.smoothVertVel - 0.08F) / 0.25F, 0.0F, 1.0F);
            float climbFactor = Mth.clamp((s.smoothVertVel - 0.03F) / 0.22F, 0.0F, 1.0F);
            float bankRad = Mth.clamp(s.smoothTurnRate * 0.032F, -0.48F, 0.48F) * aw;
            float pitchRad = Mth.clamp(-s.smoothVertVel * 1.15F, -0.40F, 0.42F) * aw;

            // Wing figure-8 / oval articulation (Images 1, 2 & 3)
            // Flap curve: cos(a) = +1 at apex (wings up), -1 at bottom of downstroke (wings swept down)
            float upDown = Mth.cos(a);             // +1 top -> -1 bottom
            float strokeVel = Mth.sin(a);          // +1 mid-downstroke, -1 mid-upstroke
            float isUpstroke = Mth.clamp(-strokeVel, 0.0F, 1.0F); // peaks at mid-recovery (breakdown 25 / frame 15!)

            float wingSpread = (66.0F - diveFactor * 30.0F) * DEG;
            float baseFlapZ = (-upDown * 50.0F * p.wingAmpScale() * (1.0F + climbFactor * 0.18F)) * DEG;
            float baseSweepY = (strokeVel * 12.0F) * DEG;   // pushes slightly forward on downstroke (lift!)
            float baseTwistX = (strokeVel * 12.0F - 4.0F) * DEG; // leading edge pitches down on downstroke

            // Forearm & wingtip lag behind base and FOLD sharply on upstroke to cut air resistance!
            float foreLagZ = (Mth.cos(a - 0.65F) * 20.0F - isUpstroke * 34.0F) * DEG;
            float foreFoldY = (-16.0F + isUpstroke * 42.0F + diveFactor * 28.0F) * DEG;
            float tipLagZ = (Mth.cos(a - 1.15F) * 24.0F - isUpstroke * 26.0F) * DEG;
            float tipFoldY = (-14.0F + isUpstroke * 28.0F + diveFactor * 18.0F) * DEG;

            // Glide pose: wide, gracefully arched wings with tiny stabilization micro-adjustments
            float glideMicro = Mth.sin(t * 0.16F) * 2.5F * DEG;
            float glideUpperZ = (-10.0F * DEG) + glideMicro;
            float glideForeZ = (6.0F * DEG) - glideMicro * 0.8F;
            float glideTipZ = (10.0F * DEG) + glideMicro * 1.2F;

            // Asymmetric banking adjustments (inner wing dips/tucks, outer wing lifts/spreads)
            float bankInnerL = bankRad * 0.45F;
            float bankInnerR = -bankRad * 0.45F;

            // Left wing (sign = +1)
            r.leftWingUpper.yRot += (wingSpread + (baseSweepY * flapW - bankInnerL * 0.35F)) * aw;
            r.leftWingUpper.zRot += (baseFlapZ * flapW + glideUpperZ * glideW + bankInnerL * 0.5F) * (1.0F - diveFactor * 0.55F);
            r.leftWingUpper.xRot += (baseTwistX * flapW) + diveFactor * 18.0F * DEG * aw;
            r.leftWingForearm.yRot += (foreFoldY * flapW + (-18.0F * DEG) * glideW);
            r.leftWingForearm.zRot += (foreLagZ * flapW + glideForeZ * glideW);
            r.leftWingTip.yRot += (tipFoldY * flapW + (-14.0F * DEG) * glideW);
            r.leftWingTip.zRot += (tipLagZ * flapW + glideTipZ * glideW);

            // Right wing (sign = -1, mirrored cleanly)
            r.rightWingUpper.yRot -= (wingSpread + (baseSweepY * flapW - bankInnerR * 0.35F)) * aw;
            r.rightWingUpper.zRot -= (baseFlapZ * flapW + glideUpperZ * glideW + bankInnerR * 0.5F) * (1.0F - diveFactor * 0.55F);
            r.rightWingUpper.xRot += (baseTwistX * flapW) + diveFactor * 18.0F * DEG * aw;
            r.rightWingForearm.yRot -= (foreFoldY * flapW + (-18.0F * DEG) * glideW);
            r.rightWingForearm.zRot -= (foreLagZ * flapW + glideForeZ * glideW);
            r.rightWingTip.yRot -= (tipFoldY * flapW + (-14.0F * DEG) * glideW);
            r.rightWingTip.zRot -= (tipLagZ * flapW + glideTipZ * glideW);

            // Delayed torso heave (Image 1: High Point lags ~0.65 rad AFTER deepest downstroke!)
            float heave = -Mth.cos(a - 0.65F); // +1 just after downstroke, -1 just after upstroke
            r.torso.y += (-heave * 1.45F * p.bodyHeaveScale()) * flapW;
            r.torso.xRot += (pitchRad + (-heave * 5.5F * DEG * flapW));
            r.torso.zRot += bankRad;

            // Predator head stabilization (Image 1: neck absorbs chest heave so head stays focused forward!)
            r.neckBase.xRot += (heave * 5.0F * DEG * flapW) - pitchRad * 0.25F;
            r.neckMid.xRot += (-heave * 2.5F * DEG * flapW);
            r.head.xRot += (-heave * 2.0F * DEG * flapW) - pitchRad * 0.35F;
            r.head.zRot -= bankRad * 0.45F;

            // Aerodynamic streamlined legs (front tucked cleanly under chest, hind swept backward with gentle inertia)
            float legInertia = heave * 5.0F * DEG * flapW;
            r.leftFrontUpper.xRot += (-50.0F * DEG - legInertia) * aw;
            r.rightFrontUpper.xRot += (-50.0F * DEG - legInertia) * aw;
            r.leftFrontLower.xRot += (76.0F * DEG + legInertia) * aw;
            r.rightFrontLower.xRot += (76.0F * DEG + legInertia) * aw;
            r.leftFrontFoot.xRot += (-24.0F * DEG) * aw;
            r.rightFrontFoot.xRot += (-24.0F * DEG) * aw;

            r.leftHindUpper.xRot += (44.0F * DEG + diveFactor * 14.0F * DEG + legInertia) * aw;
            r.rightHindUpper.xRot += (44.0F * DEG + diveFactor * 14.0F * DEG + legInertia) * aw;
            r.leftHindLower.xRot += (26.0F * DEG - diveFactor * 10.0F * DEG) * aw;
            r.rightHindLower.xRot += (26.0F * DEG - diveFactor * 10.0F * DEG) * aw;
            r.leftHindFoot.xRot += (26.0F * DEG) * aw;
            r.rightHindFoot.xRot += (26.0F * DEG) * aw;

            // Smooth S/C vertical traveling wave along all 4 tail segments (trailing the torso heave!)
            for (int i = 0; i < 4; i++) {
                float wave = Mth.cos(a - 1.15F - i * 0.65F);
                float segPitch = wave * (7.0F + i * 2.8F) * DEG * flapW * (1.0F - diveFactor * 0.5F);
                float segGlide = Mth.sin(t * 0.14F - i * 0.7F) * (2.5F + i * 0.8F) * DEG * glideW;
                r.tail[i].xRot += segPitch + segGlide - pitchRad * 0.18F;
            }
        }

        // 5. Universal delayed secondary motion (neck progression + gradual tail deceleration/curve lag)
        r.neckBase.yRot += s.neckYawLag[0] * 0.45F;
        r.neckMid.yRot += s.neckYawLag[1] * 0.35F;
        r.neckTop.yRot += s.neckYawLag[2] * 0.25F;
        r.head.yRot += Mth.clamp(state.yRot, -55.0F, 55.0F) * DEG * 0.40F;
        r.head.xRot += Mth.clamp(state.xRot, -25.0F, 30.0F) * DEG * 0.45F;

        for (int i = 0; i < 4; i++) {
            r.tail[i].yRot += s.tailYawLag[i] * (0.45F + i * 0.14F);
            r.tail[i].xRot += s.tailPitchLag[i] * (0.35F + i * 0.10F);
        }
    }

    private static void applyQuadrupedLeg(ModelPart upper, ModelPart lower, ModelPart foot,
                                          float phase, float amp, float runFactor, boolean hind) {
        float swing = Mth.sin(phase);
        float lift = Math.max(0.0F, Mth.cos(phase)); // >0 during swing-forward (foot lifted off ground)
        float upperSwing = swing * (24.0F + runFactor * 14.0F) * amp;
        float lowerBend = (lift * (26.0F + runFactor * 16.0F) - swing * (8.0F + runFactor * 4.0F)) * amp;
        float footComp = (-upperSwing * 0.35F - lowerBend * 0.45F);
        upper.xRot += (hind ? -upperSwing : upperSwing) * DEG;
        lower.xRot += (hind ? lowerBend * 0.9F : -lowerBend * 0.75F) * DEG;
        foot.xRot += footComp * DEG * 0.55F;
    }

    /** Smoothly cross-fades every bone from its previous rendered pose to eliminate hard snaps between states/clips. */
    static void smoothCrossFade(ModelPart[] joints, FireDragonRenderState state) {
        ClientState s = state.animState;
        if (s == null || state.preview || state.perched || state.deathTime > 0) {
            if (s != null) s.hasPrevPose = false;
            return;
        }
        int n = joints.length;
        if (s.prevXR == null || s.prevXR.length != n) {
            s.prevX = new float[n]; s.prevY = new float[n]; s.prevZ = new float[n];
            s.prevXR = new float[n]; s.prevYR = new float[n]; s.prevZR = new float[n];
            s.hasPrevPose = false;
        }
        // Fast enough to stay responsive for attacks/hurt, smooth enough to erase any 1-frame snap on clip switches.
        boolean urgent = state.hurtTicks > 0
                || state.clip == FireDragonAnimation.FIRE_RELEASE
                || state.clip == FireDragonAnimation.BITE_STRIKE;
        float alpha = !s.hasPrevPose || urgent
                ? 1.0F
                : 1.0F - (float) Math.exp(-0.55F * Math.max(state.dt, 0.12F));

        for (int i = 0; i < n; i++) {
            ModelPart j = joints[i];
            if (alpha < 0.999F) {
                j.x = Mth.lerp(alpha, s.prevX[i], j.x);
                j.y = Mth.lerp(alpha, s.prevY[i], j.y);
                j.z = Mth.lerp(alpha, s.prevZ[i], j.z);
                j.xRot = Mth.lerp(alpha, s.prevXR[i], j.xRot);
                j.yRot = Mth.lerp(alpha, s.prevYR[i], j.yRot);
                j.zRot = Mth.lerp(alpha, s.prevZR[i], j.zRot);
            }
            s.prevX[i] = j.x; s.prevY[i] = j.y; s.prevZ[i] = j.z;
            s.prevXR[i] = j.xRot; s.prevYR[i] = j.yRot; s.prevZR[i] = j.zRot;
        }
        s.hasPrevPose = true;
    }
}
