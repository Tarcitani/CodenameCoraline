package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.entity.babydragon.*;
import java.util.EnumMap;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Shared named rig and native animation playback; each subclass owns a different baked anatomy. */
public class DragonModel extends EntityModel<BabyDragonRenderState> {
    private final DragonStage stage;
    private final java.util.function.Function<String,ModelPart> partLookup;
    private final EnumMap<BabyDragon.Pose, KeyframeAnimation> baked = new EnumMap<>(BabyDragon.Pose.class);
    protected DragonModel(ModelPart root, DragonStage stage) {
        super(root); this.stage = stage; this.partLookup=root.createPartLookup();
        DragonAnimations.forStage(stage).forEach((pose, definition) -> this.baked.put(pose, definition.bake(root)));
    }
    public DragonStage stage() { return this.stage; }
    public ModelPart part(String name) { return this.partLookup.apply(name); }
    @Override public void setupAnim(BabyDragonRenderState state) {
        super.setupAnim(state);
        var parts = this.partLookup;
        boolean fire = state.chargeTicks > 0 || state.shotTicks > 0 || state.breathTicks > 0 || state.breathClosingTicks > 0;
        for (var entry : this.baked.entrySet()) {
            if (fire && switch(entry.getKey()) { case IDLE, WALK, FLYING, HOVER, HURT, HAPPY -> true; default -> false; }) continue;
            var clock = state.animations.get(entry.getKey());
            if (clock.isStarted()) entry.getValue().apply(clock.getTimeInMillis(state.ageInTicks), 1);
        }
        float air = state.flightBlend, age = this.stage.ordinal();
        float glide = Mth.clamp(state.glideFactor, 0.0F, 1.0F);
        float flapSuppression = 1.0F - 0.55F * glide;
        float phase = state.ageInTicks * (this.stage == DragonStage.BABY ? .47F : this.stage == DragonStage.YOUNG ? .35F : .24F);
        for (int side : new int[]{-1, 1}) {
            String p = side == 1 ? "left" : "right";
            ModelPart wing = parts.apply(p+"_wing"), elbow = parts.apply(p+"_wing_elbow"), tip = parts.apply(p+"_wing_tip");
            wing.yRot += side*(1.05F + 0.14F * glide)*air;
            wing.zRot += side*(.18F + Mth.sin(phase)*(.48F+age*.07F)*flapSuppression)*air;
            elbow.yRot += side*.18F*air; tip.yRot += side*.24F*air;
            tip.zRot += side*Mth.sin(phase-.65F)*.18F*flapSuppression*air;
            parts.apply(p+"_front_leg").xRot -= .85F*air;
            parts.apply(p+"_back_leg").xRot -= .65F*air;
            boolean closed = state.sleeping || (state.ageInTicks+state.blinkSeed)%137 < 4;
            parts.apply(p+"_eye").yScale = closed ? .12F : 1;
        }
        if (fire) {
            var rig = DragonRig.of(this.stage);
            var pose = DragonAnatomy.head(this.stage, state.yRot, state.xRot, state.chargeTicks, state.shotTicks, state.breathTicks > 0 ? state.breathTicks : -state.breathClosingTicks);
            parts.apply("body").xRot = pose.bodyPitch(); parts.apply("body").yRot = 0; parts.apply("body").zRot = 0;
            parts.apply("body").xScale = parts.apply("body").yScale = parts.apply("body").zScale = 1;
            parts.apply("body").y = rig.bodyY();
            parts.apply("body").x = parts.apply("body").z = 0;
            parts.apply("neck").xRot = pose.neckPitch(); parts.apply("neck").yRot = pose.neckYaw();
            parts.apply("neck_middle").xRot = pose.middlePitch(); parts.apply("neck_middle").z = rig.midZ()+pose.extension();
            parts.apply("head").xRot = pose.headPitch(); parts.apply("head").yRot = pose.headYaw(); parts.apply("head").z = rig.headZ()+pose.recoil();
            parts.apply("head").y = rig.headY(); parts.apply("head").zRot = 0;
            parts.apply("jaw").xRot = pose.jawPitch();
        } else {
            parts.apply("body").zRot += state.flightRoll * Mth.DEG_TO_RAD * air;
            parts.apply("body").xRot += state.flightPitch * Mth.DEG_TO_RAD * air;
            parts.apply("neck").yRot += Mth.clamp(state.yRot, -70, 70)*Mth.DEG_TO_RAD*.25F;
            parts.apply("head").yRot += Mth.clamp(state.yRot, -70, 70)*Mth.DEG_TO_RAD*.75F;
            parts.apply("neck").xRot += Mth.clamp(state.xRot, -45, 45)*Mth.DEG_TO_RAD*.25F;
            parts.apply("head").xRot += Mth.clamp(state.xRot, -45, 45)*Mth.DEG_TO_RAD*.75F;
            if (state.carryingItem) parts.apply("jaw").xRot = Math.max(parts.apply("jaw").xRot, 0.28F);
        }
        String[] tail = {"tail_base", "tail_middle", "tail_tip", "tail_3", "tail_4", "tail_5"};
        for (int i=0; i<tail.length; i++) {
            ModelPart part = parts.apply(tail[i]);
            if (part != null) part.yRot += Mth.sin(state.ageInTicks*(.09F-age*.012F)-i*.65F)*(.07F+air*.06F+i*.012F);
        }
        if(this.stage==DragonStage.ADULT) {
            ModelPart flame=parts.apply("tail_flame");
            flame.xScale=1+Mth.sin(state.ageInTicks*.34F)*.10F;
            flame.yScale=1+Mth.sin(state.ageInTicks*.29F)*.12F;
            flame.zScale=1+Mth.sin(state.ageInTicks*.23F)*.08F;
        }
    }
}
