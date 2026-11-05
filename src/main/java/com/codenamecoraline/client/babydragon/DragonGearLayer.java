package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.entity.babydragon.DragonRig;
import com.codenamecoraline.entity.babydragon.DragonStage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Renders equipped saddle/backpack, Totem of Undying, and hunted prey carried in the jaws. */
public final class DragonGearLayer extends RenderLayer<BabyDragonRenderState, DragonModel> {
    public DragonGearLayer(RenderLayerParent<BabyDragonRenderState, DragonModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack stack, SubmitNodeCollector collector, int light, BabyDragonRenderState state, float yaw, float pitch) {
        DragonModel model = this.getParentModel();
        DragonStage stage = DragonStage.byId(state.stage);
        DragonRig rig = DragonRig.of(stage);
        if (state.saddled && !state.saddleItem.isEmpty()) {
            stack.pushPose();
            model.part("body").translateAndRotate(stack);
            stack.translate(0.0F, -rig.saddleY() * 0.25F, rig.saddleZ());
            stack.mulPose(Axis.XP.rotationDegrees(90.0F));
            float saddleScale = switch (stage) {
                case BABY -> 0.42F;
                case YOUNG -> 0.65F;
                case ADULT -> 0.95F;
            };
            stack.scale(saddleScale, saddleScale, saddleScale);
            state.saddleItem.submit(stack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
            stack.popPose();
        }
        if (state.hasTotem && !state.totemItem.isEmpty()) {
            stack.pushPose();
            model.part("body").translateAndRotate(stack);
            float sideOffset = switch (stage) {
                case BABY -> 2.6F;
                case YOUNG -> 4.2F;
                case ADULT -> 6.4F;
            };
            stack.translate(sideOffset / 16.0F, -rig.saddleY() * 0.20F, rig.saddleZ() + 1.5F / 16.0F);
            stack.mulPose(Axis.YP.rotationDegrees(90.0F));
            stack.mulPose(Axis.ZP.rotationDegrees(-20.0F));
            float totemScale = switch (stage) {
                case BABY -> 0.28F;
                case YOUNG -> 0.40F;
                case ADULT -> 0.55F;
            };
            stack.scale(totemScale, totemScale, totemScale);
            state.totemItem.submit(stack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
            stack.popPose();
        }
        if (state.carryingItem && !state.mouthItem.isEmpty()) {
            stack.pushPose();
            model.part("body").translateAndRotate(stack);
            model.part("neck").translateAndRotate(stack);
            model.part("neck_middle").translateAndRotate(stack);
            model.part("head").translateAndRotate(stack);
            stack.translate(0.0F, (rig.mouthY() + 0.4F) / 16.0F, (rig.mouthZ() - rig.headZ()) / 16.0F);
            stack.mulPose(Axis.XP.rotationDegrees(90.0F));
            float itemScale = switch (stage) {
                case BABY -> 0.45F;
                case YOUNG -> 0.62F;
                case ADULT -> 0.80F;
            };
            stack.scale(itemScale, itemScale, itemScale);
            state.mouthItem.submit(stack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
            stack.popPose();
        }
    }
}
