package com.codenamecoraline.client.firedragon;

import com.codenamecoraline.entity.firedragon.FireDragon;
import com.codenamecoraline.entity.firedragon.FireDragonAnimation;
import java.util.WeakHashMap;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Client-only procedural & blended animation controller for FireDragon (Baby, Young & Adult),
 * calibrated directly against the official animation reference sheet:
 * - PARADO (IDLE): tall proud S-neck, arched semi-open wings, low sweeping S-curve tail.
 * - DESCANSANDO: chest/belly lowered to ground, all 4 legs tucked neatly, wings draped, tail curled.
 * - ANDANDO / CAMINHAR & CORRER: neck stretched forward at shoulder height, wings folded sleekly flat,
 *   level constant-height body, 4-beat alternating quadruped steps, progressive run blend.
 * - VOANDO NORMAL & VOANDO RAPIDO: unfolded horizontal wing plane, shoulder -> forearm -> tip wave,
 *   swept-back delta high-speed blend at fast airspeed, flowing serpentine tail.
 * - PLANANDO: wide gull-wing arch with minimal air-current micro-stabilization and tucked legs.
 * - SUBINDO: steep upward pitch (-36 deg), +38% faster / +24% wider power strokes, trailing tail.
 * - DESCENDO: steep downward pitch (+34 deg), high V-dihedral braking wings, arched tail, landing legs extending.
 * - CURVAS & POUSO / DECOLAGEM: expressive banking roll and smooth transition envelopes.
 */
final class FireDragonAnimController {
    private static final float DEG = Mth.DEG_TO_RAD;
    private static final WeakHashMap<FireDragon, ClientState> STATES = new WeakHashMap<>();

    private FireDragonAnimController() {}

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
        static final StageParams BABY = new StageParams(0.36F, 0.90F, 0.70F, 1.00F, 1.05F, 0.24F, 0.28F, 1.15F);
        static final StageParams YOUNG = new StageParams(0.26F, 1.00F, 0.85F, 0.92F, 0.95F, 0.16F, 0.20F, 0.78F);
        static final StageParams ADULT = new StageParams(0.21F, 1.08F, 0.95F, 0.84F, 0.92F, 0.12F, 0.14F, 0.55F);

        static StageParams of(boolean young, boolean adult) {
            return adult ? ADULT : young ? YOUNG : BABY;
        }
    }

    static final class ClientState {
        float lastAge = -1.0F;
        float wingPhase;
        float walkWeight;
        float runWeight;
        float airWeight;
        float fastFlyWeight;
        float glideWeight;
        float climbWeight;
        float descentWeight;
        float takeoffTicks;
        float landTicks;
        float takeoffWeight;
        float landWeight;
        float sitWeight;
        boolean wasAirborne;
        float smoothHorizSpeed;
        float smoothVertVel;
        float smoothTurnRate;
        final float[] tailYawLag = new float[4];
        final float[] tailPitchLag = new float[4];
        final float[] neckYawLag = new float[3];
        float[] prevX, prevY, prevZ, prevXR, prevYR, prevZR;
        boolean hasPrevPose;
        int prevStageOrdinal = -1;
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
        int stageOrd = out.adult ? 2 : out.young ? 1 : 0;
        if (s.prevStageOrdinal != stageOrd) {
            s.hasPrevPose = false;
            s.prevStageOrdinal = stageOrd;
        }

        StageParams params = StageParams.of(out.young, out.adult);
        var vel = entity.getDeltaMovement();
        float dx = (float) (entity.getX() - entity.xo);
        float dy = (float) (entity.getY() - entity.yo);
        float dz = (float) (entity.getZ() - entity.zo);
        float posHoriz = Mth.sqrt(dx * dx + dz * dz);
        float velHoriz = (float) Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        float horizSpeed = Math.max(velHoriz, posHoriz);
        float vertVel = Math.abs(dy) > Math.abs((float) vel.y) ? dy : (float) vel.y;
        float yawDelta = Mth.wrapDegrees(entity.yBodyRot - entity.yBodyRotO);

        float kinAlpha = 1.0F - (float) Math.exp(-0.26F * Math.max(dt, 0.05F));
        s.smoothHorizSpeed = Mth.lerp(kinAlpha, s.smoothHorizSpeed, horizSpeed);
        s.smoothVertVel = Mth.lerp(kinAlpha, s.smoothVertVel, vertVel);
        s.smoothTurnRate = Mth.lerp(kinAlpha, s.smoothTurnRate, Mth.clamp(yawDelta, -22.0F, 22.0F));

        boolean isAir = out.airborne && !out.perched && !out.sitting;
        boolean isSit = out.sitting && !out.perched;
        boolean isMoving = !isAir && !isSit && !out.perched && (out.walkAnimationSpeed > 0.015F || s.smoothHorizSpeed > 0.015F);

        if (isAir && !s.wasAirborne) {
            s.takeoffTicks = 14.0F;
            s.landTicks = 0.0F;
        } else if (!isAir && s.wasAirborne) {
            s.landTicks = 14.0F;
            s.takeoffTicks = 0.0F;
        }
        s.wasAirborne = isAir;
        if (s.takeoffTicks > 0.0F) s.takeoffTicks = Math.max(0.0F, s.takeoffTicks - dt);
        if (s.landTicks > 0.0F) s.landTicks = Math.max(0.0F, s.landTicks - dt);
        s.takeoffWeight = s.takeoffTicks > 0.0F ? Mth.sin((1.0F - s.takeoffTicks / 14.0F) * Mth.PI) : 0.0F;
        s.landWeight = s.landTicks > 0.0F ? Mth.sin((1.0F - s.landTicks / 14.0F) * Mth.PI) : 0.0F;

        float effWalkSpeed = Math.max(out.walkAnimationSpeed, Mth.clamp(s.smoothHorizSpeed * 3.2F, 0.0F, 1.0F));
        float targetRun = isMoving ? Mth.clamp((effWalkSpeed - 0.48F) / 0.36F, 0.0F, 1.0F) : 0.0F;
        float targetWalk = isMoving ? Mth.clamp(effWalkSpeed * 1.35F, 0.0F, 1.0F) * (1.0F - targetRun) : 0.0F;

        // Dedicated Climb (SUBINDO), Descent (DESCENDO), Fast Flight (VOANDO RAPIDO) & Glide (PLANANDO) weights
        float targetClimb = isAir ? Mth.clamp((s.smoothVertVel - 0.025F) / 0.22F, 0.0F, 1.0F) : 0.0F;
        float targetDescent = isAir ? Mth.clamp((-s.smoothVertVel - 0.035F) / 0.24F, 0.0F, 1.0F) : 0.0F;
        float targetFastFly = isAir ? Mth.clamp((s.smoothHorizSpeed - 0.34F) / 0.22F, 0.0F, 1.0F) : 0.0F;
        // PLANANDO: triggers smoothly during steady shallow-gliding cruise without climbing or steep diving
        boolean wantsGlide = isAir && targetClimb < 0.06F && targetDescent < 0.25F
                && s.smoothHorizSpeed > 0.16F && s.smoothVertVel <= -0.008F && s.smoothVertVel >= -0.095F;

        float blendAlpha = 1.0F - (float) Math.exp(-0.20F * Math.max(dt, 0.05F));
        s.airWeight = Mth.lerp(blendAlpha, s.airWeight, isAir ? 1.0F : 0.0F);
        s.fastFlyWeight = Mth.lerp(blendAlpha * 0.85F, s.fastFlyWeight, targetFastFly);
        s.glideWeight = Mth.lerp(blendAlpha * 0.70F, s.glideWeight, wantsGlide ? 1.0F : 0.0F);
        s.climbWeight = Mth.lerp(blendAlpha * 0.90F, s.climbWeight, targetClimb);
        s.descentWeight = Mth.lerp(blendAlpha * 0.90F, s.descentWeight, targetDescent);
        s.sitWeight = Mth.lerp(blendAlpha, s.sitWeight, isSit ? 1.0F : 0.0F);
        s.walkWeight = Mth.lerp(blendAlpha, s.walkWeight, targetWalk);
        s.runWeight = Mth.lerp(blendAlpha, s.runWeight, targetRun);

        // Continuous wingbeat phase: faster in SUBINDO (+38%) and VOANDO RAPIDO (+20%), slower in DESCENDO/PLANANDO
        float freqMod = 1.0F + 0.38F * s.climbWeight + 0.20F * s.fastFlyWeight - 0.28F * s.descentWeight;
        float flapActivity = Mth.lerp(s.glideWeight, 1.0F, 0.04F);
        s.wingPhase = (s.wingPhase + dt * params.wingRateRadPerTick() * freqMod * flapActivity) % Mth.TWO_PI;

        // Progressive delayed secondary-motion buffers (neck follows head/turn, tail follows torso with cascade lag)
        float targetNeckYaw = Mth.clamp(out.yRot, -55.0F, 55.0F) * DEG;
        float turnRad = s.smoothTurnRate * DEG;
        float nRate = 1.0F - (float) Math.exp(-params.neckLagRate() * Math.max(dt, 0.05F));
        s.neckYawLag[0] = Mth.lerp(nRate, s.neckYawLag[0], targetNeckYaw * 0.35F + turnRad * 0.75F);
        s.neckYawLag[1] = Mth.lerp(nRate * 0.85F, s.neckYawLag[1], s.neckYawLag[0] * 0.9F + targetNeckYaw * 0.25F);
        s.neckYawLag[2] = Mth.lerp(nRate * 0.75F, s.neckYawLag[2], s.neckYawLag[1] * 0.85F + targetNeckYaw * 0.20F);

        float tRate = 1.0F - (float) Math.exp(-params.tailLagRate() * Math.max(dt, 0.05F));
        float baseTailYaw = -turnRad * 1.45F;
        float baseTailPitch = Mth.clamp(-s.smoothVertVel * 0.45F, -0.22F, 0.22F);
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

    private static float asymmetricPhase(float phase) {
        float u = (phase % Mth.TWO_PI) / Mth.TWO_PI;
        if (u < 0.0F) u += 1.0F;
        return u < 0.58F
                ? (u / 0.58F) * Mth.PI
                : Mth.PI + ((u - 0.58F) / 0.42F) * Mth.PI;
    }

    /** Computes procedural blended base locomotion pose on the model's real 35-bone hierarchy. */
    static void applyLocomotion(FireDragonModel.Rig r, FireDragonRenderState state) {
        ClientState s = state.animState;
        if (s == null) return;
        StageParams p = StageParams.of(state.young, state.adult);
        float t = state.ageInTicks;

        float groundMoveW = Mth.clamp(s.walkWeight + s.runWeight, 0.0F, 1.0F);
        float idleW = Mth.clamp(1.0F - groundMoveW - s.airWeight - s.sitWeight, 0.0F, 1.0F);

        // =========================================================================
        // 1. PARADO (IDLE) — Reference top-left: proud S-neck, semi-open arched wings, low sweeping S-tail
        // =========================================================================
        if (idleW > 0.001F || s.sitWeight > 0.001F) {
            float breathFreq = s.sitWeight > 0.5F ? 0.085F : 0.135F;
            float breath = Mth.sin(t * breathFreq);
            float w = idleW + s.sitWeight * 0.75F;
            r.torso.xRot += breath * 1.4F * DEG * w;
            r.torso.y += -breath * 0.14F * w;
            r.belly.y += breath * 0.14F * w;

            float glance = (Mth.sin(t * 0.031F * p.curiosityRate()) + 0.5F * Mth.sin(t * 0.073F * p.curiosityRate())) * idleW;
            float pitchGlance = Mth.sin(t * 0.047F * p.curiosityRate()) * idleW;
            r.neckBase.yRot += glance * 4.5F * DEG;
            r.neckMid.yRot += glance * 4.0F * DEG;
            r.neckTop.yRot += glance * 3.0F * DEG;
            r.head.yRot += glance * 5.0F * DEG;
            r.head.xRot += pitchGlance * 3.0F * DEG - breath * 1.2F * DEG * w;
            r.neckMid.xRot -= breath * 1.4F * DEG * w;

            // Proud semi-open arched wings in PARADO (IDLE) on Adult (matching reference top-left!)
            if (idleW > 0.001F) {
                float wingArch = state.adult ? 1.0F : 0.35F;
                float wingSettle = breath * 1.6F * DEG * idleW;
                r.leftWingUpper.yRot += (24.0F * wingArch) * DEG * idleW;
                r.rightWingUpper.yRot -= (24.0F * wingArch) * DEG * idleW;
                r.leftWingUpper.zRot += (-4.0F * wingArch) * DEG * idleW + wingSettle;
                r.rightWingUpper.zRot -= (-4.0F * wingArch) * DEG * idleW + wingSettle;
                r.leftWingForearm.yRot += (-12.0F * wingArch) * DEG * idleW;
                r.rightWingForearm.yRot -= (-12.0F * wingArch) * DEG * idleW;
                r.leftWingForearm.zRot += (14.0F * wingArch) * DEG * idleW;
                r.rightWingForearm.zRot -= (14.0F * wingArch) * DEG * idleW;
                r.leftWingTip.zRot += (12.0F * wingArch) * DEG * idleW;
                r.rightWingTip.zRot -= (12.0F * wingArch) * DEG * idleW;

                // Low sweeping S-curve tail resting near ground (tail.xRot < 0 lowers, > 0 curls tip up)
                r.tail[0].xRot += -11.0F * DEG * idleW;
                r.tail[1].xRot += -4.0F * DEG * idleW;
                r.tail[2].xRot += 7.0F * DEG * idleW;
                r.tail[3].xRot += 13.0F * DEG * idleW;
            }

            // Serpentine idle tail sway (MOVIMENTACAO DA CAUDA)
            float tailSpeed = s.sitWeight > 0.5F ? 0.055F : 0.095F;
            float tailAmp = (s.sitWeight > 0.5F ? 2.2F : 7.5F) * p.tailWaveAmp() * (idleW + s.sitWeight * 0.4F);
            for (int i = 0; i < 4; i++) {
                r.tail[i].yRot += Mth.sin(t * tailSpeed - i * 0.85F) * tailAmp * (0.60F + i * 0.28F) * DEG;
            }
        }

        // =========================================================================
        // 2. DESCANSANDO (Resting / Lying Down) — Belly lowered to ground, 4 legs tucked, wings draped
        // =========================================================================
        if (s.sitWeight > 0.001F) {
            float sw = s.sitWeight;
            float dropY = state.adult ? 4.4F : state.young ? 3.2F : 1.6F;
            r.torso.y += dropY * sw;
            r.torso.xRot += 3.5F * DEG * sw;

            // Calm relaxed neck & head posture
            if (state.adult) {
                r.neckBase.xRot += 18.0F * DEG * sw;
                r.neckMid.xRot += 6.0F * DEG * sw;
                r.neckTop.xRot -= 10.0F * DEG * sw;
                r.head.xRot -= 10.0F * DEG * sw;
            } else {
                r.neckBase.xRot += 10.0F * DEG * sw;
                r.head.xRot += 4.0F * DEG * sw;
            }

            // All 4 legs neatly folded beneath/beside the resting body
            r.leftFrontUpper.xRot += -56.0F * DEG * sw;
            r.rightFrontUpper.xRot += -56.0F * DEG * sw;
            r.leftFrontLower.xRot += 98.0F * DEG * sw;
            r.rightFrontLower.xRot += 98.0F * DEG * sw;
            r.leftFrontFoot.xRot += -42.0F * DEG * sw;
            r.rightFrontFoot.xRot += -42.0F * DEG * sw;

            r.leftHindUpper.xRot += -62.0F * DEG * sw;
            r.rightHindUpper.xRot += -62.0F * DEG * sw;
            r.leftHindLower.xRot += 108.0F * DEG * sw;
            r.rightHindLower.xRot += 108.0F * DEG * sw;
            r.leftHindFoot.xRot += -44.0F * DEG * sw;
            r.rightHindFoot.xRot += -44.0F * DEG * sw;

            // Wings folded softly down along the flanks
            r.leftWingUpper.yRot += -6.0F * DEG * sw;
            r.rightWingUpper.yRot -= -6.0F * DEG * sw;
            r.leftWingUpper.zRot += 20.0F * DEG * sw;
            r.rightWingUpper.zRot -= 20.0F * DEG * sw;

            // Tail curled gracefully around one side, resting flat on ground
            r.tail[0].xRot += -4.0F * DEG * sw;
            r.tail[0].yRot += 22.0F * DEG * sw;
            r.tail[1].yRot += 28.0F * DEG * sw;
            r.tail[2].yRot += 26.0F * DEG * sw;
            r.tail[3].yRot += 22.0F * DEG * sw;
        }

        // =========================================================================
        // 3. ANDANDO / CAMINHAR & CORRER — Reference top-right: forward low neck, sleek folded wings, 4-beat steps
        // =========================================================================
        if (groundMoveW > 0.001F) {
            float runFactor = s.runWeight / groundMoveW;
            float strideScale = state.adult ? 0.85F : state.young ? 1.05F : 1.20F;
            float stridePos = state.walkAnimationPos * strideScale * (1.0F + runFactor * 0.22F);
            float amp = groundMoveW * p.walkAmpScale() * (1.0F + runFactor * 0.42F);

            // True 4-beat alternating quadruped steps (90 deg apart: FL -> HR -> FR -> HL)
            applyQuadrupedLeg(r.leftFrontUpper, r.leftFrontLower, r.leftFrontFoot, stridePos, amp, runFactor);
            applyQuadrupedLeg(r.rightHindUpper, r.rightHindLower, r.rightHindFoot, stridePos + Mth.HALF_PI, amp, runFactor);
            applyQuadrupedLeg(r.rightFrontUpper, r.rightFrontLower, r.rightFrontFoot, stridePos + Mth.PI, amp, runFactor);
            applyQuadrupedLeg(r.leftHindUpper, r.leftHindLower, r.leftHindFoot, stridePos + Mth.PI + Mth.HALF_PI, amp, runFactor);

            // Constant-height heavy body with natural weight-transfer roll & yaw sway (ZERO vertical hop!)
            float weightShift = Mth.sin(stridePos);
            float doubleStep = Mth.cos(stridePos * 2.0F);
            r.torso.y += (doubleStep * (0.06F + runFactor * 0.14F)) * groundMoveW;
            r.torso.xRot += (runFactor * 5.0F + doubleStep * (0.8F + runFactor * 1.5F)) * DEG * groundMoveW;
            r.torso.zRot += weightShift * (2.2F + runFactor * 1.5F) * DEG * groundMoveW;
            r.torso.yRot += Mth.cos(stridePos) * (1.6F + runFactor * 1.0F) * DEG * groundMoveW;

            // Reference ANDANDO: neck lowers and stretches forward at shoulder height (replaces tall Idle S-curve!)
            if (state.adult) {
                r.neckBase.xRot += (20.0F + runFactor * 4.0F) * DEG * groundMoveW;
                r.neckMid.xRot += (8.0F + runFactor * 2.0F) * DEG * groundMoveW;
                r.neckTop.xRot -= (12.0F + runFactor * 2.0F) * DEG * groundMoveW;
                r.head.xRot -= (16.0F + runFactor * 4.0F) * DEG * groundMoveW;
            } else {
                r.neckBase.xRot += (runFactor * 5.0F) * DEG * groundMoveW;
                r.head.xRot -= (runFactor * 3.0F) * DEG * groundMoveW;
            }
            r.neckBase.xRot += -doubleStep * (0.8F + runFactor * 1.2F) * DEG * groundMoveW;
            r.neckBase.yRot += -Mth.cos(stridePos) * 1.1F * DEG * groundMoveW;
            r.head.zRot += -weightShift * 1.4F * DEG * groundMoveW;

            // Reference ANDANDO: wings folded sleekly flat and low along the back during walk; slight balance flare when running
            float walkFlatWingZ = (20.0F - runFactor * 16.0F) * DEG * groundMoveW;
            float runWingSpreadY = (runFactor * 14.0F) * DEG * groundMoveW;
            r.leftWingUpper.yRot += runWingSpreadY;
            r.rightWingUpper.yRot -= runWingSpreadY;
            r.leftWingUpper.zRot += walkFlatWingZ + doubleStep * 2.5F * DEG * runFactor * groundMoveW;
            r.rightWingUpper.zRot -= walkFlatWingZ + doubleStep * 2.5F * DEG * runFactor * groundMoveW;

            // Low flowing serpentine tail counterbalance (ANDANDO + MOVIMENTACAO DA CAUDA)
            float tailMoveAmp = (7.5F + runFactor * 6.0F) * p.tailWaveAmp() * groundMoveW;
            r.tail[0].xRot += -8.0F * DEG * groundMoveW;
            r.tail[1].xRot += -2.0F * DEG * groundMoveW;
            r.tail[2].xRot += 4.0F * DEG * groundMoveW;
            r.tail[3].xRot += 7.0F * DEG * groundMoveW;
            for (int i = 0; i < 4; i++) {
                r.tail[i].yRot += Mth.sin(stridePos - i * 0.72F) * tailMoveAmp * (0.68F + i * 0.24F) * DEG;
            }
        }

        // =========================================================================
        // 4. DECOLAGEM & POUSO (Smooth Transition Envelopes — Reference Row 3 Right)
        // =========================================================================
        if (s.takeoffWeight > 0.001F || s.landWeight > 0.001F) {
            float tkW = s.takeoffWeight * (1.0F - s.airWeight * 0.45F);
            float ldW = s.landWeight * (1.0F - s.airWeight * 0.45F);
            r.torso.y += (0.90F * ldW + 0.45F * tkW);
            r.torso.xRot += (-10.0F * tkW - 14.0F * ldW) * DEG; // POUSO flares chest slightly upright as wings brake!
            r.neckBase.xRot += (-6.0F * tkW + 10.0F * ldW) * DEG;
            r.head.xRot += (4.0F * tkW + 4.0F * ldW) * DEG;
            // Landing wide wing brake flare before folding
            r.leftWingUpper.yRot += (36.0F * ldW) * DEG;
            r.rightWingUpper.yRot -= (36.0F * ldW) * DEG;
            r.leftWingUpper.zRot += (12.0F * ldW) * DEG;
            r.rightWingUpper.zRot -= (12.0F * ldW) * DEG;
            // Legs flex to absorb heavy touchdown or launch into flight
            float legFlex = (tkW * 18.0F + ldW * 24.0F) * DEG;
            r.leftHindUpper.xRot -= legFlex;
            r.rightHindUpper.xRot -= legFlex;
            r.leftHindLower.xRot += legFlex * 1.35F;
            r.rightHindLower.xRot += legFlex * 1.35F;
            r.leftHindFoot.xRot -= legFlex * 0.45F;
            r.rightHindFoot.xRot -= legFlex * 0.45F;
        }

        // =========================================================================
        // 5. VOO NORMAL, VOO RAPIDO, PLANANDO, SUBINDO, DESCENDO & CURVAS (Reference Rows 2-3)
        // =========================================================================
        if (s.airWeight > 0.001F) {
            float aw = s.airWeight;
            float cw = s.climbWeight * aw;
            float dw = s.descentWeight * aw;
            float fw = s.fastFlyWeight * aw * (1.0F - cw * 0.5F) * (1.0F - dw * 0.5F);
            float glideW = aw * s.glideWeight * (1.0F - cw) * (1.0F - dw);
            float flapW = Math.max(0.0F, aw - glideW);
            float phi = asymmetricPhase(s.wingPhase);

            // CURVAS DURANTE O VOO: expressive banking roll up to ~32 deg
            float bankRad = Mth.clamp(s.smoothTurnRate * 0.036F, -0.56F, 0.56F) * aw;
            // SUBINDO (-36 deg steep upward pitch) vs DESCENDO / RAZANTE (+28 deg aerodynamic swoop dive)
            float climbDivePitch = (-36.0F * cw + 28.0F * dw + 3.0F * fw) * DEG;

            // Step A: Unfold wings cleanly from bind pose (-65 Y, -40 Z) into horizontal flight plane,
            // modulated by VOANDO RAPIDO (-22 Y), SUBINDO (+10 Y pull), DESCENDO / RAZANTE (sleek swept-back swoop -28 Y, -12 Z)
            float unfoldUpperY = (65.0F + 10.0F * cw - 28.0F * dw - 22.0F * fw) * DEG * aw;
            float unfoldUpperZ = (38.0F - 12.0F * dw + 4.0F * fw) * DEG * aw;
            float unfoldForeY = (-16.0F + 8.0F * fw + 14.0F * dw) * DEG * aw;
            float unfoldForeZ = (-8.0F + 4.0F * glideW) * DEG * aw;
            float unfoldTipY = (-14.0F + 8.0F * fw + 10.0F * dw) * DEG * aw;

            // Step B: Shoulder -> Forearm (-0.60 rad) -> Wingtip (-1.20 rad) continuous traveling wave
            float ampScale = p.wingAmpScale() * (1.0F + 0.24F * cw - 0.16F * fw - 0.55F * dw);
            float shoulderBeat = -Mth.sin(phi);
            float shoulderVel = Mth.cos(phi);
            float upstrokeFold = Mth.clamp(-shoulderVel, 0.0F, 1.0F);

            float upperFlapZ = (shoulderBeat * 38.0F * ampScale) * DEG * flapW;
            float upperSweepY = (shoulderVel * (9.0F + 6.0F * cw)) * DEG * flapW;
            float upperTwistX = (shoulderVel * 7.0F - 5.0F * cw + 10.0F * dw) * DEG * flapW;

            float foreWave = -Mth.sin(phi - 0.60F);
            float foreFlapZ = (foreWave * 24.0F * ampScale - upstrokeFold * 16.0F) * DEG * flapW;
            float foreFoldY = (-upstrokeFold * 20.0F) * DEG * flapW;

            float tipWave = -Mth.sin(phi - 1.20F);
            float tipFlapZ = (tipWave * 28.0F * ampScale - upstrokeFold * 12.0F) * DEG * flapW;
            float tipFoldY = (-upstrokeFold * 14.0F) * DEG * flapW;

            // PLANANDO (Reference Row 3 Left): wide, serene gull-wing arch with gentle air-current micro-corrections
            float glideMicro = Mth.sin(t * 0.14F) * 1.6F * DEG * glideW;
            float glideUpperZ = (-8.0F * DEG) * glideW + glideMicro;
            float glideForeZ = (5.0F * DEG) * glideW - glideMicro * 0.7F;
            float glideTipZ = (10.0F * DEG) * glideW + glideMicro * 1.1F;

            // Asymmetric banking wing tilt (CURVAS DURANTE O VOO)
            float bankL = bankRad * 0.45F;
            float bankR = -bankRad * 0.45F;

            r.leftWingUpper.yRot += unfoldUpperY + upperSweepY - bankL * 0.32F;
            r.leftWingUpper.zRot += unfoldUpperZ + upperFlapZ + glideUpperZ + bankL * 0.48F;
            r.leftWingUpper.xRot += upperTwistX;
            r.leftWingForearm.yRot += unfoldForeY + foreFoldY;
            r.leftWingForearm.zRot += unfoldForeZ + foreFlapZ + glideForeZ;
            r.leftWingTip.yRot += unfoldTipY + tipFoldY;
            r.leftWingTip.zRot += tipFlapZ + glideTipZ;

            r.rightWingUpper.yRot -= unfoldUpperY + upperSweepY - bankR * 0.32F;
            r.rightWingUpper.zRot -= unfoldUpperZ + upperFlapZ + glideUpperZ + bankR * 0.48F;
            r.rightWingUpper.xRot += upperTwistX;
            r.rightWingForearm.yRot -= unfoldForeY + foreFoldY;
            r.rightWingForearm.zRot -= unfoldForeZ + foreFlapZ + glideForeZ;
            r.rightWingTip.yRot -= unfoldTipY + tipFoldY;
            r.rightWingTip.zRot -= tipFlapZ + glideTipZ;

            // Step C: Smooth connected whole-body reaction to wing thrust (lags shoulder by 0.55 rad)
            float bodyWave = Mth.sin(phi - 0.55F);
            float bodyPitchWave = Mth.cos(phi - 0.55F);
            r.torso.y += (-bodyWave * 0.65F * p.bodyHeaveScale() * (1.0F + 0.22F * cw - 0.25F * fw)) * flapW;
            r.torso.xRot += climbDivePitch + (-bodyPitchWave * 3.0F * DEG * flapW);
            r.torso.zRot += bankRad;

            // Step D: Aerodynamic stretched neck + SUBINDO upward arch / DESCENDO (RAZANTE: stretches neck & head straight forward!)
            if (state.adult) {
                r.neckBase.xRot += (24.0F + 6.0F * dw) * DEG * aw;
                r.neckMid.xRot += (12.0F + 3.0F * dw) * DEG * aw;
                r.neckTop.xRot -= (16.0F + 4.0F * dw) * DEG * aw;
                r.head.xRot -= (20.0F + 5.0F * dw) * DEG * aw;
            }
            r.neckBase.xRot += (bodyPitchWave * 2.5F * DEG * flapW) + (-10.0F * cw) * DEG;
            r.neckMid.xRot += (-bodyPitchWave * 1.2F * DEG * flapW) + (-6.0F * cw) * DEG;
            r.neckTop.xRot += (-4.0F * cw) * DEG;
            r.head.xRot += (-bodyPitchWave * 1.0F * DEG * flapW) + (-4.0F * cw) * DEG;
            r.head.zRot -= bankRad * 0.45F;

            // Step E: Flight legs — tucked in VOANDO NORMAL/PLANANDO, tightly tucked in VOANDO RAPIDO, SUBINDO and RAZANTE (DESCENDO)!
            float legPulse = bodyPitchWave * 3.0F * DEG * flapW;
            float tight = Math.min(1.25F, fw + cw + dw * 1.2F);
            float frontUpperAir = (-46.0F - 14.0F * tight) * DEG * aw - legPulse;
            float frontLowerAir = (72.0F + 20.0F * tight) * DEG * aw + legPulse;
            float frontFootAir = (-22.0F - 10.0F * tight) * DEG * aw;

            float hindUpperAir = (44.0F + 18.0F * tight) * DEG * aw + legPulse;
            float hindLowerAir = (26.0F - 12.0F * tight) * DEG * aw;
            float hindFootAir = (24.0F + 10.0F * tight) * DEG * aw;

            r.leftFrontUpper.xRot += frontUpperAir;
            r.rightFrontUpper.xRot += frontUpperAir;
            r.leftFrontLower.xRot += frontLowerAir;
            r.rightFrontLower.xRot += frontLowerAir;
            r.leftFrontFoot.xRot += frontFootAir;
            r.rightFrontFoot.xRot += frontFootAir;

            r.leftHindUpper.xRot += hindUpperAir;
            r.rightHindUpper.xRot += hindUpperAir;
            r.leftHindLower.xRot += hindLowerAir;
            r.rightHindLower.xRot += hindLowerAir;
            r.leftHindFoot.xRot += hindFootAir;
            r.rightHindFoot.xRot += hindFootAir;

            // Step F: Tail aerodynamics (taut streamlined trail in RAZANTE / VOANDO RAPIDO, low trail in SUBINDO)
            float tailPitchShape = (-6.0F * cw + 4.0F * dw) * DEG;
            float tailWaveScale = Math.max(0.35F, 1.0F - 0.40F * fw - 0.35F * dw);
            for (int i = 0; i < 4; i++) {
                float waveV = Mth.sin(phi - 1.05F - i * 0.62F);
                float waveH = Mth.sin(t * 0.14F - i * 0.78F);
                float segPitch = waveV * (4.8F + i * 2.0F) * tailWaveScale * DEG * flapW;
                float segGlide = Mth.sin(t * 0.12F - i * 0.75F) * (2.2F + i * 0.8F) * DEG * glideW;
                r.tail[i].xRot += segPitch + segGlide + tailPitchShape;
                r.tail[i].yRot += waveH * (3.5F + i * 1.8F) * tailWaveScale * DEG * aw;
            }
        }

        // =========================================================================
        // 6. Universal delayed secondary motion (neck progression + serpentine tail rudder lag)
        // =========================================================================
        r.neckBase.yRot += s.neckYawLag[0] * 0.45F;
        r.neckMid.yRot += s.neckYawLag[1] * 0.35F;
        r.neckTop.yRot += s.neckYawLag[2] * 0.25F;
        r.head.yRot += Mth.clamp(state.yRot, -55.0F, 55.0F) * DEG * 0.40F;
        r.head.xRot += Mth.clamp(state.xRot, -25.0F, 30.0F) * DEG * 0.45F;

        for (int i = 0; i < 4; i++) {
            r.tail[i].yRot += s.tailYawLag[i] * (0.48F + i * 0.16F);
            r.tail[i].xRot += s.tailPitchLag[i] * (0.30F + i * 0.08F);
        }
    }

    /**
     * Natural 4-beat quadruped leg cycle:
     * - [0..pi]: forward swing (paw lifts just enough to clear ground and advances from rear to front).
     * - [pi..2pi]: grounded stance (paw stays planted pushing body forward at constant height).
     */
    private static void applyQuadrupedLeg(ModelPart upper, ModelPart lower, ModelPart foot,
                                          float phase, float amp, float runFactor) {
        float stride = Mth.cos(phase);
        float lift = Math.max(0.0F, Mth.sin(phase));
        float upperAngle = stride * (17.0F + runFactor * 11.0F) * amp;
        float kneeFlex = lift * (18.0F + runFactor * 13.0F) * amp;
        float footLevel = -upperAngle * 0.65F - kneeFlex * 0.55F;

        upper.xRot += upperAngle * DEG;
        lower.xRot += kneeFlex * DEG;
        foot.xRot += footLevel * DEG;
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
        boolean urgent = state.hurtTicks > 0
                || state.clip == FireDragonAnimation.FIRE_RELEASE
                || state.clip == FireDragonAnimation.BITE_STRIKE
                || state.clip == FireDragonAnimation.CLAW_STRIKE;
        float alpha = !s.hasPrevPose || urgent
                ? 1.0F
                : 1.0F - (float) Math.exp(-0.45F * Math.max(state.dt, 0.12F));

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
