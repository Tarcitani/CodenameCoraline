package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.entity.babydragon.BabyDragon.Pose;
import com.codenamecoraline.entity.babydragon.DragonStage;
import java.util.EnumMap;
import net.minecraft.client.animation.*;

/** Separate native animation catalogues for three anatomies; long neck attack aiming is shared with the server. */
public final class DragonAnimations {
    private static final EnumMap<DragonStage, EnumMap<Pose, AnimationDefinition>> STAGES = new EnumMap<>(DragonStage.class);
    static { for (DragonStage stage : DragonStage.values()) STAGES.put(stage, create(stage)); }
    public static EnumMap<Pose, AnimationDefinition> forStage(DragonStage stage) { return STAGES.get(stage); }
    private static EnumMap<Pose, AnimationDefinition> create(DragonStage stage) {
        var all = new EnumMap<Pose, AnimationDefinition>(Pose.class); float age = stage.ordinal();
        float idleTime = 3+age*.7F;
        var idle = loop(idleTime);
        rot(idle,"head",idleTime,new float[][]{{0,0,-2+age*.5F},{-2-age*.4F,5-age,2-age*.5F},{0,0,-2+age*.5F}});
        pos(idle,"body",idleTime,new float[][]{{0,0,0},{0,.20F+age*.08F,0},{0,0,0}});
        all.put(Pose.IDLE,idle.build());
        float gait = .7F+age*.22F, step = 27-age*4;
        var walk = loop(gait);
        for (String part : new String[]{"left_front_leg","right_back_leg"}) {
            rot(walk,part,gait,new float[][]{{step,0,0},{-step,0,0},{step,0,0}});
            rot(walk,part+"_shin",gait,new float[][]{{-12,0,0},{15,0,0},{-12,0,0}});
        }
        for (String part : new String[]{"right_front_leg","left_back_leg"}) {
            rot(walk,part,gait,new float[][]{{-step,0,0},{step,0,0},{-step,0,0}});
            rot(walk,part+"_shin",gait,new float[][]{{15,0,0},{-12,0,0},{15,0,0}});
        }
        rot(walk,"body",gait,new float[][]{{0,0,-2+age*.4F},{0,0,2-age*.4F},{0,0,-2+age*.4F}});
        pos(walk,"body",gait,new float[][]{{0,0,0},{0,.15F+age*.12F,0},{0,0,0}});
        rot(walk,"neck",gait,new float[][]{{-1,0,0},{2+age,0,0},{-1,0,0}});
        all.put(Pose.WALK,walk.build());
        float liftTime = .7F+age*.15F;
        var takeoff = once(liftTime);
        pos(takeoff,"body",liftTime,new float[][]{{0,0,0},{0,-.7F-age*.15F,0},{0,1+age*.2F,0},{0,0,0}});
        rot(takeoff,"neck",liftTime,new float[][]{{0,0,0},{-10-age*2,0,0},{-3,0,0},{0,0,0}});
        rot(takeoff,"jaw",liftTime,new float[][]{{0,0,0},{8,0,0},{0,0,0}});
        all.put(Pose.TAKEOFF,takeoff.build());
        float beat = .65F+age*.20F;
        var flying = loop(beat);
        rot(flying,"body",beat,new float[][]{{-3-age,0,0},{-1-age*2,0,0},{-3-age,0,0}});
        rot(flying,"neck_middle",beat,new float[][]{{-2,0,0},{1,0,0},{-2,0,0}});
        all.put(Pose.FLYING,flying.build());
        var hover = loop(beat*.8F);
        pos(hover,"body",beat*.8F,new float[][]{{0,-.15F,0},{0,.15F,0},{0,-.15F,0}});
        all.put(Pose.HOVER,hover.build());
        float landTime = .6F+age*.15F;
        var land = once(landTime);
        pos(land,"body",landTime,new float[][]{{0,.3F,0},{0,-.5F-age*.15F,0},{0,0,0}});
        rot(land,"neck",landTime,new float[][]{{-8,0,0},{4+age,0,0},{0,0,0}});
        all.put(Pose.LANDING,land.build());
        var shoulder = loop(4);
        rot(shoulder,"head",4,new float[][]{{3,-8,3},{-2,12,-3},{3,-8,3}});
        all.put(Pose.SHOULDER_IDLE,shoulder.build());
        float chargeTime = stage == DragonStage.ADULT ? 1.6F : stage == DragonStage.YOUNG ? .8F : .6F;
        var charge = once(chargeTime);
        for (int side : new int[]{-1,1}) rot(charge,side==1?"left_wing":"right_wing",chargeTime,new float[][]{{0,0,0},{0,side*5,side*-3},{0,side*8,side*-5}});
        all.put(Pose.FIREBALL_CHARGE,charge.build());
        var shot = once(.4F);
        for (int side : new int[]{-1,1}) rot(shot,side==1?"left_wing":"right_wing",.4F,new float[][]{{0,side*8,0},{0,side*-3,side*6},{0,0,0}});
        all.put(Pose.FIREBALL_SHOOT,shot.build());
        var hurt = once(.5F);
        rot(hurt,"body",.5F,new float[][]{{0,0,0},{-5,0,-9+age*2},{0,0,0}});
        all.put(Pose.HURT,hurt.build());
        var sleep = loop(5+age);
        rot(sleep,"neck",5+age,new float[][]{{12,0,0},{13,0,0},{12,0,0}});
        rot(sleep,"head",5+age,new float[][]{{12,0,7},{14,0,8},{12,0,7}});
        all.put(Pose.SLEEP,sleep.build());
        var happy = once(1.5F);
        rot(happy,"head",1.5F,new float[][]{{0,0,0},{-5,0,10-age*2},{-2,0,-8+age},{0,0,0}});
        all.put(Pose.HAPPY,happy.build());
        var prep = once(1);
        rot(prep,"left_wing",1,new float[][]{{0,0,0},{0,12,-5},{0,18,-8}});
        rot(prep,"right_wing",1,new float[][]{{0,0,0},{0,-12,5},{0,-18,8}});
        all.put(Pose.BREATH_PREPARE,prep.build());
        var breath = loop(.8F);
        rot(breath,"left_wing",.8F,new float[][]{{0,18,-8},{0,17,-7},{0,18,-8}});
        rot(breath,"right_wing",.8F,new float[][]{{0,-18,8},{0,-17,7},{0,-18,8}});
        all.put(Pose.BREATH_LOOP,breath.build());
        var evolve = once(3);
        rot(evolve,"neck",3,new float[][]{{0,0,0},{-18,0,0},{-10,0,0},{0,0,0}});
        rot(evolve,"head",3,new float[][]{{0,0,0},{-8,0,0},{-15,0,0},{0,0,0}});
        rot(evolve,"jaw",3,new float[][]{{0,0,0},{15,0,0},{27,0,0},{0,0,0}});
        all.put(Pose.EVOLVE,evolve.build());
        var death = once(1);
        rot(death,"body",1,new float[][]{{0,0,0},{6,0,35},{10,0,65}});
        rot(death,"head",1,new float[][]{{0,0,0},{8,0,0},{18,0,0}});
        all.put(Pose.DEATH,death.build());
        var melee = once(.7F);
        rot(melee,"neck",.7F,new float[][]{{-6,0,0},{12,0,0},{-3,0,0},{0,0,0}});
        rot(melee,"jaw",.7F,new float[][]{{22,0,0},{35,0,0},{0,0,0},{0,0,0}});
        all.put(Pose.MELEE,melee.build());
        return all;
    }
    private static AnimationDefinition.Builder loop(float time) { return once(time).looping(); }
    private static AnimationDefinition.Builder once(float time) { return AnimationDefinition.Builder.withLength(time); }
    private static void rot(AnimationDefinition.Builder b,String part,float time,float[][] values) { channel(b,part,time,values,true); }
    private static void pos(AnimationDefinition.Builder b,String part,float time,float[][] values) { channel(b,part,time,values,false); }
    private static void channel(AnimationDefinition.Builder b,String part,float time,float[][] values,boolean rotation) {
        Keyframe[] frames = new Keyframe[values.length];
        for(int i=0;i<values.length;i++) frames[i] = new Keyframe(time*i/(values.length-1), rotation ?
                KeyframeAnimations.degreeVec(values[i][0],values[i][1],values[i][2]) : KeyframeAnimations.posVec(values[i][0],values[i][1],values[i][2]),AnimationChannel.Interpolations.CATMULLROM);
        b.addAnimation(part,new AnimationChannel(rotation?AnimationChannel.Targets.ROTATION:AnimationChannel.Targets.POSITION,frames));
    }
    private DragonAnimations() {}
}
