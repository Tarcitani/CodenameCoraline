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
    private final ModelPart head;
    private final ModelPart neck;
    private final ModelPart[] joints;
    private final KeyframeAnimation[] clips;

    public FireDragonModel(ModelPart root) {
        super(root);
        var lookup = root.createPartLookup();
        head = lookup.apply("head");
        neck = lookup.apply("neck_base");
        joints = root.getAllParts().toArray(ModelPart[]::new);
        clips = new KeyframeAnimation[FireDragonAnimations.CLIPS.length];
        for (int i = 0; i < clips.length; i++) clips[i] = FireDragonAnimations.CLIPS[i].bake(root);
    }

    public static LayerDefinition createBodyLayer() { return FireDragonGeometry.createBodyLayer(); }

    /** Shared by the real renderer and the offline exporter; no alternate preview geometry. */
    public void applyPose(FireDragonAnimation clip, float ticks) {
        for (ModelPart joint : joints) joint.resetPose();
        clips[clip.ordinal()].apply((long) (Math.max(0, ticks) * 50.0F), 1.0F);
    }

    @Override
    public void setupAnim(FireDragonRenderState state) {
        super.setupAnim(state);
        if (state.preview) { applyPose(state.clip, state.clipTicks); return; }
        if (state.deathTime > 0) { applyPose(FireDragonAnimation.DEATH, state.deathTime); return; }
        if (state.hurtTicks > 0) { applyPose(FireDragonAnimation.HURT, 10 - state.hurtTicks); return; }
        if (state.sitting) applyPose(FireDragonAnimation.SIT, 12);
        else if (state.airborne) applyPose(FireDragonAnimation.FLY, state.ageInTicks);
        else if (state.walkAnimationSpeed > 0.01F) {
            for (ModelPart joint : joints) joint.resetPose();
            clips[FireDragonAnimation.WALK.ordinal()].applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 1.0F, 2.0F);
        } else if (state.clip != FireDragonAnimation.NONE) applyPose(state.clip, state.clipTicks);
        else applyPose(FireDragonAnimation.BREATHE, state.ageInTicks);
        neck.yRot += Mth.clamp(state.yRot, -55, 55) * Mth.DEG_TO_RAD * 0.35F;
        head.yRot += Mth.clamp(state.yRot, -55, 55) * Mth.DEG_TO_RAD * 0.65F;
        head.xRot += Mth.clamp(state.xRot, -25, 30) * Mth.DEG_TO_RAD * 0.5F;
    }
}
