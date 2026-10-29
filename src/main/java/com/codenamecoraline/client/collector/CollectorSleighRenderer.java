package com.codenamecoraline.client.collector;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.collector.CollectorSleigh;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Client renderer for the temporary sky sleigh (`codenamecoraline:collector_sleigh`). */
public final class CollectorSleighRenderer extends EntityRenderer<CollectorSleigh, CollectorSleighRenderState> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/collector_sleigh.png");

    private final CollectorSleighModel model;

    public CollectorSleighRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CollectorSleighModel(context.bakeLayer(CollectorSleighModel.LAYER));
        this.shadowRadius = 0.0F;
    }

    @Override
    protected boolean affectedByCulling(CollectorSleigh entity) {
        return false;
    }

    @Override
    public CollectorSleighRenderState createRenderState() {
        return new CollectorSleighRenderState();
    }

    @Override
    public void extractRenderState(CollectorSleigh entity, CollectorSleighRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.yRot = entity.getYRot(partialTick);
        state.xRot = entity.getXRot(partialTick);
        state.flightProgress = entity.getFlightProgress(partialTick);
        state.riderAboard = entity.isRiderAboard();
    }

    @Override
    public void submit(
            CollectorSleighRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 1.5F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        submitNodeCollector.submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityCutout(TEXTURE),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null
        );
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, cameraRenderState);
    }
}
