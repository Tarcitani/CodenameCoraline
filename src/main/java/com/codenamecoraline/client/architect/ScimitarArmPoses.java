package com.codenamecoraline.client.architect;

import com.codenamecoraline.item.ArchitectScimitarItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

/**
 * Third-person arm poses of the Architect's Scimitar (spec §26.3), added to {@link HumanoidModel.ArmPose} through
 * META-INF/enumextensions.json. PARRY follows the use timeline (raise 0-2, hold with a tremor 2-10, lower 10-16);
 * COUNTER is the fixed outward sweep shown for a few ticks after a parry lands.
 */
public final class ScimitarArmPoses {
    public static final EnumProxy<HumanoidModel.ArmPose> PARRY = new EnumProxy<>(HumanoidModel.ArmPose.class,
            false, false, (IArmPoseTransformer) ScimitarArmPoses::parry);
    public static final EnumProxy<HumanoidModel.ArmPose> COUNTER = new EnumProxy<>(HumanoidModel.ArmPose.class,
            false, false, (IArmPoseTransformer) ScimitarArmPoses::counter);

    static final float RAISE_X = -95.0F;
    static final float RAISE_Y = -35.0F;
    static final float TREMOR = 1.0F;
    static final float COUNTER_X = -70.0F;
    static final float COUNTER_Y = 40.0F;

    private ScimitarArmPoses() {}

    /** 0..1: how far the guard is raised at this tick of the use (fractional). */
    static float raise(float t) {
        if (t < ArchitectScimitarItem.WINDOW_START) {
            float k = t / ArchitectScimitarItem.WINDOW_START;
            return 1.0F - (1.0F - k) * (1.0F - k); // ease-out
        }
        if (t < ArchitectScimitarItem.WINDOW_END) {
            return 1.0F;
        }
        float k = Mth.clamp((t - ArchitectScimitarItem.WINDOW_END) / (float) (ArchitectScimitarItem.USE_TICKS - ArchitectScimitarItem.WINDOW_END), 0.0F, 1.0F);
        return 1.0F - k * k; // ease-in
    }

    /** A ±1° shiver every 2 ticks while the window is open: the "I am ready" tell. */
    static float tremor(float t, float amount) {
        return t >= ArchitectScimitarItem.WINDOW_START && t < ArchitectScimitarItem.WINDOW_END
                ? Mth.sin(t * Mth.PI) * amount : 0.0F;
    }

    private static void parry(HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm arm) {
        ModelPart part = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float t = state.ticksUsingItem;
        float k = raise(t);
        part.xRot = Mth.lerp(k, part.xRot * 0.5F, (RAISE_X + tremor(t, TREMOR)) * Mth.DEG_TO_RAD);
        part.yRot = side * RAISE_Y * k * Mth.DEG_TO_RAD;
        part.zRot = 0.0F;
    }

    private static void counter(HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm arm) {
        ModelPart part = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        part.xRot = COUNTER_X * Mth.DEG_TO_RAD;
        part.yRot = side * COUNTER_Y * Mth.DEG_TO_RAD;
        part.zRot = 0.0F;
    }
}
