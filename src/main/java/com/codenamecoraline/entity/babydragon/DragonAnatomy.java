package com.codenamecoraline.entity.babydragon;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Shared, deterministic attack rig. Server fire origins and client bones use the same transforms. */
public final class DragonAnatomy {
    public record HeadPose(float bodyPitch, float neckPitch, float neckYaw, float middlePitch,
                           float headPitch, float headYaw, float jawPitch, float extension, float recoil,
                           Vec3 mouthModel, Vec3 directionModel) {}

    public static EntityDimensions dimensions(DragonStage stage, float growthScale) {
        DragonRig rig = DragonRig.of(stage);
        return EntityDimensions.scalable(rig.width(), rig.height()).withEyeHeight(rig.eye()).scale(growthScale / stage.scale);
    }
    public static HeadPose head(DragonStage stage, float relativeYaw, float lookPitch, int charge, int shot, int breath) {
        DragonRig rig = DragonRig.of(stage);
        float yaw = Mth.clamp(relativeYaw, -85, 85) * Mth.DEG_TO_RAD;
        float pitch = Mth.clamp(lookPitch, -65, 65) * Mth.DEG_TO_RAD;
        int duration = stage == DragonStage.ADULT ? 32 : stage == DragonStage.YOUNG ? 16 : 12;
        float preparing = charge > 0 ? 1-charge/(float)duration : 0;
        float breathing = breath > 0 ? Math.min(1, (100-breath)/20F) : breath < 0 ? Math.min(1, -breath/10F) : 0;
        float recoil = shot > 0 ? Mth.sin((1-shot/8F)*(float)Math.PI) : 0;
        float neck = rig.neckPitch() - preparing*.09F - breathing*.12F;
        float middle = rig.midPitch() + preparing*.04F + breathing*.03F;
        float neckYaw = yaw*.30F;
        float extension = -breathing*(stage.ordinal()+1)*.32F;
        float retract = preparing*.35F + recoil*(.3F+stage.ordinal()*.2F);
        float jaw = (charge > 0 ? preparing*.48F : 0) + (breath > 0 ? .15F+breathing*.55F : breath < 0 ? breathing*.70F : 0) + (shot > 0 ? shot/8F*.55F : 0);
        Matrix4f parent = new Matrix4f().translation(0, rig.bodyY(), 0)
                .translate(0, rig.neckY(), rig.neckZ()).rotateY(neckYaw).rotateX(neck)
                .translate(0, rig.midY(), rig.midZ()+extension).rotateX(middle)
                .translate(0, rig.headY(), rig.headZ()+retract);
        Vector3f desired = new Vector3f(-Mth.sin(yaw)*Mth.cos(pitch), Mth.sin(pitch), -Mth.cos(yaw)*Mth.cos(pitch));
        Vector3f local = new Matrix4f(parent).invert().transformDirection(desired, new Vector3f()).normalize();
        float headYaw = (float)Math.atan2(-local.x, -local.z);
        float headPitch = (float)Math.atan2(local.y, Math.sqrt(local.x*local.x+local.z*local.z));
        Matrix4f head = new Matrix4f(parent).rotateY(headYaw).rotateX(headPitch);
        Vector3f mouth = head.transformPosition(0, rig.mouthY() + Mth.sin(jaw)*rig.jawLever()*.45F, rig.mouthZ(), new Vector3f());
        Vector3f direction = head.transformDirection(0, 0, -1, new Vector3f()).normalize();
        return new HeadPose(0, neck, neckYaw, middle, headPitch, headYaw, jaw, extension, retract,
                new Vec3(mouth.x, mouth.y, mouth.z), new Vec3(direction.x, direction.y, direction.z));
    }
    public static Vec3 modelToWorld(Vec3 model, float growthScale, float bodyYaw) {
        return new Vec3(model.x, 24-model.y, -model.z).scale(.85*growthScale/16).yRot(-bodyYaw*Mth.DEG_TO_RAD);
    }
    public static Vec3 directionToWorld(Vec3 direction, float bodyYaw) {
        return new Vec3(direction.x, -direction.y, -direction.z).yRot(-bodyYaw*Mth.DEG_TO_RAD).normalize();
    }
    public static Vec3 saddle(DragonStage stage, float growthScale, float bodyYaw) {
        DragonRig rig = DragonRig.of(stage);
        return new Vec3(0, rig.saddleY(), rig.saddleZ()).scale(growthScale/stage.scale).yRot(-bodyYaw*Mth.DEG_TO_RAD);
    }
    public static Vec3 tailEmber(DragonStage stage, float ticks) {
        DragonRig rig=DragonRig.of(stage); int count=(int)rig.tailSegments();
        Matrix4f tail=new Matrix4f().translation(0,rig.bodyY()+.4F,rig.tailRootZ());
        float previous=0;
        for(int i=0;i<count;i++) {
            if(i>0) tail.translate(0,0,previous*.86F);
            float yaw=Mth.sin(ticks*(.09F-stage.ordinal()*.012F)-i*.65F)*(.07F+i*.012F);
            tail.rotateY(yaw).rotateX(i==0?.06F:-.025F);
            previous=rig.tailLength()*(1-i*.075F);
        }
        Vector3f p=tail.translate(0,0,previous*.8F).rotateY(.1F).transformPosition(0,0,2.5F,new Vector3f());
        return new Vec3(p.x,p.y,p.z);
    }
    private DragonAnatomy() {}
}
