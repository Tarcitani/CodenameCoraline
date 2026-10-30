package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.babydragon.BabyDragon.Pose;
import java.util.EnumMap;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Original four-legged hatchling: broad cheeks, cream belly, jointed bat wings and a three-joint tail. */
public final class BabyDragonModel extends EntityModel<BabyDragonRenderState> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID,"baby_dragon"),"main");
    private final EnumMap<Pose,KeyframeAnimation> baked=new EnumMap<>(Pose.class);
    public BabyDragonModel(ModelPart root) {
        super(root);
        BabyDragonAnimations.ALL.forEach((pose,definition)->this.baked.put(pose,definition.bake(root)));
    }
    private static CubeListBuilder cube(int slot,float x,float y,float z,float w,float h,float d) {
        return CubeListBuilder.create().texOffs((slot%8)*32,(slot/8)*16).addBox(x,y,z,w,h,d);
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh=new MeshDefinition(); PartDefinition root=mesh.getRoot();
        PartDefinition body=root.addOrReplaceChild("body",cube(0,-3,-3,-3.5F,6,6,7),PartPose.offset(0,19.5F,0));
        body.addOrReplaceChild("belly",cube(1,-2.4F,-1.8F,-.3F,4.8F,4.3F,.8F),PartPose.offsetAndRotation(0,.1F,-3.3F,-.08F,0,0));
        body.addOrReplaceChild("neck",cube(2,-2,-2,-2,4,4,3),PartPose.offset(0,-1.2F,-3));
        PartDefinition head=body.addOrReplaceChild("head",cube(3,-3.5F,-4,-4,7,6,6),PartPose.offset(0,-2,-3.4F));
        head.addOrReplaceChild("snout",cube(4,-2.5F,-1.5F,-2,5,2.8F,3),PartPose.offset(0,-.3F,-4));
        head.addOrReplaceChild("jaw",cube(5,-2.3F,0,-3.5F,4.6F,1,4),PartPose.offset(0,1,-2.5F));
        for(int side:new int[]{-1,1}) {
            String p=side==1?"left":"right";
            head.addOrReplaceChild(p+"_eye",cube(side==1?6:7,-.12F,-1.3F,-1.25F,.24F,2.6F,2.5F),PartPose.offset(side*3.51F,-1.4F,-2.1F));
            PartDefinition horn=head.addOrReplaceChild(p+"_horn",cube(8,-.65F,-2,0,1.3F,2.4F,1.4F),PartPose.offsetAndRotation(side*2.5F,-3.5F,.5F,-.35F,0,side*.22F));
            horn.addOrReplaceChild(p+"_horn_tip",cube(9,-.4F,-1.2F,0,.8F,1.4F,.9F),PartPose.offsetAndRotation(0,-1.7F,.5F,-.55F,0,side*.2F));
            head.addOrReplaceChild(p+"_ear",cube(10,-.3F,-1.5F,-.5F,.6F,3,2),PartPose.offsetAndRotation(side*3.5F,-.5F,1.2F,.3F,side*-.4F,side*-.65F));
            for(int front:new int[]{0,1}) {
                String leg=p+(front==1?"_front_leg":"_back_leg");
                PartDefinition limb=body.addOrReplaceChild(leg,cube(front==1?11:12,-1,-.5F,-1,2,3,2),PartPose.offset(side*2.6F,1.5F,front==1?-2.3F:2.5F));
                limb.addOrReplaceChild(leg+"_paw",cube(13,-1.1F,0,-1.5F,2.2F,1.1F,2.6F),PartPose.offset(0,2,0));
            }
            PartDefinition wing=body.addOrReplaceChild(p+"_wing",cube(14,side==1?0:-4,-.5F,-.6F,4,1,1.2F),PartPose.offsetAndRotation(side*2.5F,-2,0,0,side*-.9F,side*.55F));
            wing.addOrReplaceChild(p+"_inner_membrane",cube(15,side==1?0:-4,0,0,4,.12F,4),PartPose.offset(0,0,0));
            PartDefinition tip=wing.addOrReplaceChild(p+"_wing_tip",cube(16,side==1?0:-5,-.35F,-.3F,5,.7F,.8F),PartPose.offsetAndRotation(side*4,0,0,0,side*-.15F,0));
            tip.addOrReplaceChild(p+"_membrane_a",cube(17,side==1?0:-2,0,.2F,2,.12F,5.4F),PartPose.ZERO);
            tip.addOrReplaceChild(p+"_membrane_b",cube(18,side==1?2:-3.5F,0,.2F,1.5F,.12F,3.8F),PartPose.ZERO);
            tip.addOrReplaceChild(p+"_membrane_c",cube(19,side==1?3.5F:-5,0,.2F,1.5F,.12F,2),PartPose.ZERO);
            tip.addOrReplaceChild(p+"_finger",cube(20,-.23F,-.2F,0,.46F,.45F,5.4F),PartPose.offsetAndRotation(side*.8F,0,0,0,side*.3F,0));
        }
        PartDefinition tail=body.addOrReplaceChild("tail_base",cube(21,-1.25F,-1,0,2.5F,2,3.5F),PartPose.offsetAndRotation(0,.5F,3,.14F,0,0));
        PartDefinition middle=tail.addOrReplaceChild("tail_middle",cube(22,-.8F,-.7F,0,1.6F,1.4F,3),PartPose.offsetAndRotation(0,0,3,.12F,0,0));
        PartDefinition end=middle.addOrReplaceChild("tail_tip",cube(23,-.45F,-.4F,0,.9F,.8F,2.7F),PartPose.offsetAndRotation(0,0,2.6F,-.3F,0,0));
        end.addOrReplaceChild("tail_flame",cube(24,-1,-.2F,0,2,.4F,2),PartPose.offsetAndRotation(0,0,1.8F,0,.65F,0));
        for(int i=0;i<3;i++) body.addOrReplaceChild("dorsal_"+i,cube(25,-.4F,-1.1F,-.6F,.8F,1.3F,1.2F),PartPose.offset(0,-3,-.6F+i*1.6F));
        return LayerDefinition.create(mesh,256,128);
    }
    @Override public void setupAnim(BabyDragonRenderState state) {
        super.setupAnim(state);
        var parts=this.root().createPartLookup();
        for(var entry:this.baked.entrySet()) {
            var animation=state.animations.get(entry.getKey());
            if(animation.isStarted()) entry.getValue().apply(animation.getTimeInMillis(state.ageInTicks),1);
        }
        float air=state.flightBlend;
        for(int side:new int[]{-1,1}) {
            String p=side==1?"left":"right";
            parts.apply(p+"_wing").yRot+=side*.8F*air;
            parts.apply(p+"_wing").zRot-=side*.5F*air;
            parts.apply(p+"_front_leg").xRot-=.9F*air;
            parts.apply(p+"_back_leg").xRot-=.65F*air;
            boolean closed=state.sleeping || ((state.ageInTicks+state.blinkSeed)%137)<4;
            parts.apply(p+"_eye").yScale=closed?.12F:1;
        }
        ModelPart head=parts.apply("head");
        head.yRot+=Mth.clamp(state.yRot,-40,40)*Mth.DEG_TO_RAD*.65F;
        head.xRot+=Mth.clamp(state.xRot,-25,25)*Mth.DEG_TO_RAD*.5F;
        parts.apply("tail_tip").yRot+=Mth.sin(state.ageInTicks*.09F+1)*.09F;
        if(state.shoulder) { parts.apply("body").y+=.5F; head.zRot+=.06F; }
    }
}
