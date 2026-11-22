package com.codenamecoraline.client.firedragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.firedragon.DragonFireball;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Custom 3D comet fireball renderer oriented along the projectile's flight vector with emissive glow. */
public final class DragonFireballRenderer extends EntityRenderer<DragonFireball, DragonFireballRenderState> {
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/fire_dragon/fireball.png");
    private static final Identifier GLOW_TEXTURE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/fire_dragon/fireball_glow.png");

    private final DragonFireballModel model;

    public DragonFireballRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new DragonFireballModel(context.bakeLayer(DragonFireballModel.LAYER));
        this.shadowRadius = 0.0F;
    }

    @Override
    public DragonFireballRenderState createRenderState() {
        return new DragonFireballRenderState();
    }

    @Override
    protected int getBlockLightLevel(DragonFireball entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void extractRenderState(DragonFireball entity, DragonFireballRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.visualScale = entity.getVisualScale();
        Vec3 vel = entity.getDeltaMovement();
        if (vel.lengthSqr() > 1.0E-6D) {
            double horiz = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
            state.yRot = (float) Math.toDegrees(Math.atan2(vel.x, vel.z));
            state.xRot = (float) Math.toDegrees(Math.atan2(-vel.y, horiz));
        } else {
            state.yRot = entity.getYRot();
            state.xRot = entity.getXRot();
        }
    }

    @Override
    public void submit(
            DragonFireballRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.14F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
        float pulse = (0.86F + 0.08F * Mth.sin(state.ageInTicks * 0.9F)) * state.visualScale;
        poseStack.scale(pulse, pulse, pulse);
        submitNodeCollector.submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityTranslucent(TEXTURE),
                FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null
        );
        submitNodeCollector.submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.eyes(GLOW_TEXTURE),
                FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null
        );
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, cameraRenderState);
    }
}
