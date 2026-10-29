package com.codenamecoraline.client.collector;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.collector.CollectorGift;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Client renderer for the Collector's last gift (`codenamecoraline:collector_gift`). */
public final class CollectorGiftRenderer extends EntityRenderer<CollectorGift, CollectorGiftRenderState> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            CodenameCoraline.MOD_ID, "textures/entity/collector_gift.png");

    private final CollectorGiftModel model;

    public CollectorGiftRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CollectorGiftModel(context.bakeLayer(CollectorGiftModel.LAYER));
        this.shadowRadius = 0.9F;
    }

    @Override
    public CollectorGiftRenderState createRenderState() {
        return new CollectorGiftRenderState();
    }

    @Override
    public void submit(
            CollectorGiftRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 1.5F, 0.0F);
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
