package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.entity.babydragon.BabyDragon.Pose;
import java.util.EnumMap;
import net.minecraft.client.animation.*;

/** Twelve coordinated native animations, with eased joints and staggered wing tips. */
public final class BabyDragonAnimations {
    public static final EnumMap<Pose, AnimationDefinition> ALL = new EnumMap<>(Pose.class);
    static {
        var idle = loop(3);
        rot(idle, "head", 3, new float[][]{{0,0,-2},{-3,7,3},{0,0,-2}});
        rot(idle, "tail_base", 3, new float[][]{{0,-7,0},{-2,9,0},{0,-7,0}});
        pos(idle, "body", 3, new float[][]{{0,0,0},{0,0.22F,0},{0,0,0}});
        ALL.put(Pose.IDLE, idle.build());
        var walk = loop(0.8F);
        for (String part : new String[]{"left_front_leg","right_back_leg"}) rot(walk, part, .8F, new float[][]{{22,0,0},{-22,0,0},{22,0,0}});
        for (String part : new String[]{"right_front_leg","left_back_leg"}) rot(walk, part, .8F, new float[][]{{-22,0,0},{22,0,0},{-22,0,0}});
        rot(walk,"body",.8F,new float[][]{{0,0,-2},{0,0,2},{0,0,-2}});
        rot(walk,"tail_middle",.8F,new float[][]{{0,9,0},{0,-9,0},{0,9,0}});
        ALL.put(Pose.WALK,walk.build());
        var takeoff = AnimationDefinition.Builder.withLength(.7F);
        pos(takeoff,"body",.7F,new float[][]{{0,0,0},{0,-1,0},{0,1,0},{0,0,0}});
        rot(takeoff,"head",.7F,new float[][]{{0,0,0},{-12,0,0},{4,0,0},{0,0,0}});
        wings(takeoff,.7F,0.8F); ALL.put(Pose.TAKEOFF,takeoff.build());
        var flying = loop(.65F); wings(flying,.65F,1);
        rot(flying,"body",.65F,new float[][]{{-3,0,0},{3,0,0},{-3,0,0}});
        rot(flying,"tail_middle",.65F,new float[][]{{-6,-5,0},{5,5,0},{-6,-5,0}});
        ALL.put(Pose.FLYING,flying.build());
        var hover = loop(.48F); wings(hover,.48F,.65F);
        pos(hover,"body",.48F,new float[][]{{0,-.35F,0},{0,.35F,0},{0,-.35F,0}});
        rot(hover,"head",.48F,new float[][]{{2,0,0},{-2,0,0},{2,0,0}});
        ALL.put(Pose.HOVER,hover.build());
        var landing = AnimationDefinition.Builder.withLength(.6F);
        pos(landing,"body",.6F,new float[][]{{0,0,0},{0,-.8F,0},{0,0,0}});
        rot(landing,"body",.6F,new float[][]{{-10,0,0},{4,0,0},{0,0,0}});
        rot(landing,"tail_base",.6F,new float[][]{{-15,0,0},{8,0,0},{0,0,0}});
        ALL.put(Pose.LANDING,landing.build());
        var shoulder = loop(4);
        rot(shoulder,"head",4,new float[][]{{4,-9,3},{-2,13,-4},{4,-9,3}});
        rot(shoulder,"tail_middle",4,new float[][]{{8,-9,0},{4,10,0},{8,-9,0}});
        pos(shoulder,"body",4,new float[][]{{0,0,0},{0,.15F,0},{0,0,0}});
        ALL.put(Pose.SHOULDER_IDLE,shoulder.build());
        var charge = AnimationDefinition.Builder.withLength(.6F);
        rot(charge,"head",.6F,new float[][]{{0,0,0},{-9,0,0},{-14,0,0}});
        rot(charge,"jaw",.6F,new float[][]{{0,0,0},{16,0,0},{27,0,0}});
        pos(charge,"head",.6F,new float[][]{{0,0,0},{0,0,.4F},{0,0,.7F}});
        scale(charge,"body",.6F,new float[][]{{1,1,1},{1.035F,1.02F,1.02F},{1.06F,1.045F,1.03F}});
        ALL.put(Pose.FIREBALL_CHARGE,charge.build());
        var shoot = AnimationDefinition.Builder.withLength(.4F);
        rot(shoot,"head",.4F,new float[][]{{-14,0,0},{9,0,0},{0,0,0}});
        rot(shoot,"jaw",.4F,new float[][]{{32,0,0},{23,0,0},{0,0,0}});
        pos(shoot,"body",.4F,new float[][]{{0,0,.5F},{0,0,-.3F},{0,0,0}});
        ALL.put(Pose.FIREBALL_SHOOT,shoot.build());
        var hurt = AnimationDefinition.Builder.withLength(.5F);
        rot(hurt,"body",.5F,new float[][]{{0,0,0},{-8,0,-10},{0,0,0}});
        rot(hurt,"left_wing",.5F,new float[][]{{0,0,0},{0,-12,20},{0,0,0}});
        rot(hurt,"right_wing",.5F,new float[][]{{0,0,0},{0,12,-20},{0,0,0}});
        ALL.put(Pose.HURT,hurt.build());
        var sleep = loop(5);
        rot(sleep,"head",5,new float[][]{{17,0,9},{19,0,10},{17,0,9}});
        pos(sleep,"body",5,new float[][]{{0,-1.2F,0},{0,-1.05F,0},{0,-1.2F,0}});
        rot(sleep,"tail_base",5,new float[][]{{0,30,0},{0,32,0},{0,30,0}});
        ALL.put(Pose.SLEEP,sleep.build());
        var happy = AnimationDefinition.Builder.withLength(1.5F);
        rot(happy,"head",1.5F,new float[][]{{0,0,0},{-4,0,12},{-2,0,-8},{0,0,0}});
        rot(happy,"tail_base",1.5F,new float[][]{{0,0,0},{0,24,0},{0,-20,0},{0,0,0}});
        wings(happy,1.5F,.3F); ALL.put(Pose.HAPPY,happy.build());
    }
    private static AnimationDefinition.Builder loop(float length) { return AnimationDefinition.Builder.withLength(length).looping(); }
    private static void rot(AnimationDefinition.Builder b,String part,float duration,float[][] values) { channel(b,part,duration,values,true); }
    private static void pos(AnimationDefinition.Builder b,String part,float duration,float[][] values) { channel(b,part,duration,values,false); }
    private static void scale(AnimationDefinition.Builder b,String part,float duration,float[][] values) {
        Keyframe[] frames=new Keyframe[values.length];
        for(int i=0;i<values.length;i++) frames[i]=new Keyframe(duration*i/(values.length-1),KeyframeAnimations.scaleVec(values[i][0],values[i][1],values[i][2]),AnimationChannel.Interpolations.CATMULLROM);
        b.addAnimation(part,new AnimationChannel(AnimationChannel.Targets.SCALE,frames));
    }
    private static void channel(AnimationDefinition.Builder b,String part,float duration,float[][] v,boolean rotation) {
        Keyframe[] frames=new Keyframe[v.length];
        for(int i=0;i<v.length;i++) frames[i]=new Keyframe(duration*i/(v.length-1), rotation ? KeyframeAnimations.degreeVec(v[i][0],v[i][1],v[i][2]) : KeyframeAnimations.posVec(v[i][0],v[i][1],v[i][2]), AnimationChannel.Interpolations.CATMULLROM);
        b.addAnimation(part,new AnimationChannel(rotation ? AnimationChannel.Targets.ROTATION : AnimationChannel.Targets.POSITION,frames));
    }
    private static void wings(AnimationDefinition.Builder b,float duration,float amount) {
        for(int side:new int[]{-1,1}) {
            String prefix=side==1?"left":"right";
            rot(b,prefix+"_wing",duration,new float[][]{{-4,0,side*-29*amount},{6,0,side*36*amount},{-4,0,side*-29*amount}});
            rot(b,prefix+"_wing_tip",duration,new float[][]{{0,side*12*amount,side*-12*amount},{0,side*-8*amount,side*22*amount},{0,side*12*amount,side*-12*amount}});
        }
    }
    private BabyDragonAnimations() {}
}
