package com.codenamecoraline.client.firedragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.firedragon.FireDragonAnimation;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Articulated baby: three neck joints, four tail joints, elbowed wings and four segmented legs.
 * Bind pose uses radians; rotation order is ZYX, as verified in ModelPart 26.1.2 sources.
 */
public final class FireDragonModel extends EntityModel<FireDragonRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "fire_dragon"), "main");
    public static final ModelLayerLocation YOUNG_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "fire_dragon"), "young");
    public static final ModelLayerLocation ADULT_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "fire_dragon"), "adult");
    static final class Rig {
        final ModelPart torso, belly;
        final @org.jspecify.annotations.Nullable ModelPart saddle;
        final ModelPart neckBase, neckMid, neckTop, head, jaw;
        final ModelPart leftWingUpper, leftWingForearm, leftWingTip;
        final ModelPart rightWingUpper, rightWingForearm, rightWingTip;
        final ModelPart leftFrontUpper, leftFrontLower, leftFrontFoot;
        final ModelPart rightFrontUpper, rightFrontLower, rightFrontFoot;
        final ModelPart leftHindUpper, leftHindLower, leftHindFoot;
        final ModelPart rightHindUpper, rightHindLower, rightHindFoot;
        final ModelPart[] tail;

        Rig(ModelPart root) {
            var l = root.createPartLookup();
            torso = l.apply("torso"); belly = l.apply("belly");
            saddle = torso.hasChild("saddle") ? torso.getChild("saddle") : null;
            neckBase = l.apply("neck_base"); neckMid = l.apply("neck_mid"); neckTop = l.apply("neck_top");
            head = l.apply("head"); jaw = l.apply("jaw");
            leftWingUpper = l.apply("left_wing_upper"); leftWingForearm = l.apply("left_wing_forearm"); leftWingTip = l.apply("left_wing_tip");
            rightWingUpper = l.apply("right_wing_upper"); rightWingForearm = l.apply("right_wing_forearm"); rightWingTip = l.apply("right_wing_tip");
            leftFrontUpper = l.apply("left_front_upper"); leftFrontLower = l.apply("left_front_lower"); leftFrontFoot = l.apply("left_front_foot");
            rightFrontUpper = l.apply("right_front_upper"); rightFrontLower = l.apply("right_front_lower"); rightFrontFoot = l.apply("right_front_foot");
            leftHindUpper = l.apply("left_hind_upper"); leftHindLower = l.apply("left_hind_lower"); leftHindFoot = l.apply("left_hind_foot");
            rightHindUpper = l.apply("right_hind_upper"); rightHindLower = l.apply("right_hind_lower"); rightHindFoot = l.apply("right_hind_foot");
            tail = new ModelPart[] { l.apply("tail_1"), l.apply("tail_2"), l.apply("tail_3"), l.apply("tail_4") };
        }
    }

    private final Rig rig;
    private final ModelPart[] joints;
    private final ModelPart[] shoulderCoil;
    private final KeyframeAnimation[] clips;

    public FireDragonModel(ModelPart root) {
        super(root);
        rig = new Rig(root);
        shoulderCoil = new ModelPart[] {rig.torso, rig.neckBase, rig.neckMid,
                rig.neckTop, rig.head, rig.tail[0], rig.tail[1], rig.tail[2], rig.tail[3]};
        joints = root.getAllParts().toArray(ModelPart[]::new);
        clips = new KeyframeAnimation[FireDragonAnimations.CLIPS.length];
        for (int i = 0; i < clips.length; i++) clips[i] = FireDragonAnimations.CLIPS[i].bake(root);
    }

    public static LayerDefinition createBodyLayer() { return FireDragonGeometry.createBodyLayer(); }
    public static LayerDefinition createYoungBodyLayer() { return FireDragonYoungGeometry.createBodyLayer(); }
    public static LayerDefinition createAdultBodyLayer() { return FireDragonAdultGeometry.createBodyLayer(); }

    /** Shared by the real renderer and the offline exporter; no alternate preview geometry. */
    public void applyPose(FireDragonAnimation clip, float ticks) {
        for (ModelPart joint : joints) joint.resetPose();
        clips[clip.ordinal()].apply((long) (Math.max(0, ticks) * 50.0F), 1.0F);
    }

    @Override
    public void setupAnim(FireDragonRenderState state) {
        super.setupAnim(state);
        if (rig.saddle != null) rig.saddle.visible = state.saddled;
        if (state.preview) { applyPose(state.clip, state.clipTicks); return; }
        if (state.deathTime > 0) { applyPose(FireDragonAnimation.DEATH, state.deathTime); return; }
        if (state.perched) {
            applyPose(FireDragonAnimation.SHOULDER, state.ageInTicks);
            if (state.shoulderSide > 0) for (ModelPart joint : shoulderCoil) {
                joint.yRot = -joint.yRot;
                joint.zRot = -joint.zRot;
            }
            return;
        }
        if (state.hurtTicks > 0) {
            applyPose(FireDragonAnimation.HURT, 10 - state.hurtTicks);
            FireDragonAnimController.smoothCrossFade(joints, state);
            return;
        }
        if (state.clip == FireDragonAnimation.PLAY_CHASE) {
            for (ModelPart joint : joints) joint.resetPose();
            FireDragonAnimController.applyLocomotion(rig, state);
            clips[FireDragonAnimation.PLAY_CHASE.ordinal()].apply((long) (Math.max(0, state.clipTicks) * 50.0F), 1.0F);
            FireDragonAnimController.smoothCrossFade(joints, state);
            return;
        }
        if (state.clip != FireDragonAnimation.NONE && state.clip != FireDragonAnimation.BREATHE) {
            applyPose(state.clip, state.clipTicks);
            // Keep gentle delayed tail motion alive during non-full-body action clips (idles, eat, growl, fire)
            if (state.animState != null && state.clip != FireDragonAnimation.REST_TOGETHER) {
                for (int i = 0; i < 4; i++) rig.tail[i].yRot += state.animState.tailYawLag[i] * (0.30F + i * 0.10F);
            }
            FireDragonAnimController.smoothCrossFade(joints, state);
            return;
        }
        for (ModelPart joint : joints) joint.resetPose();
        if (state.animState != null) {
            FireDragonAnimController.applyLocomotion(rig, state);
            FireDragonAnimController.smoothCrossFade(joints, state);
        } else {
            if (state.sitting) applyPose(FireDragonAnimation.SIT, 12);
            else if (state.airborne) applyPose(FireDragonAnimation.FLY, state.ageInTicks);
            else if (state.walkAnimationSpeed > 0.01F) {
                clips[FireDragonAnimation.WALK.ordinal()].applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 1.0F, 2.0F);
            } else applyPose(FireDragonAnimation.BREATHE, state.ageInTicks);
        }
    }
}
