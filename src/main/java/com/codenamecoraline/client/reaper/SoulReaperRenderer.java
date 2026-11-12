package com.codenamecoraline.client.reaper;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.reaper.SoulReaper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public final class SoulReaperRenderer extends MobRenderer<SoulReaper, SoulReaperRenderState, SoulReaperModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/soul_reaper.png");
    private static final float VISUAL_SCALE = 1.48F;

    public SoulReaperRenderer(EntityRendererProvider.Context context) {
        super(context, new SoulReaperModel(context.bakeLayer(SoulReaperModel.LAYER_LOCATION)), 1.15F);
        this.addLayer(new SoulReaperEyesLayer(this));
    }

    @Override
    public SoulReaperRenderState createRenderState() {
        return new SoulReaperRenderState();
    }

    @Override
    public void extractRenderState(SoulReaper entity, SoulReaperRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.state = entity.getState();
        state.stage = entity.getStage();
        state.locomotionMode = entity.getLocomotionMode();
        state.actionId = entity.getActionId();
        state.reactId = entity.getReactId();
        state.actionTick = entity.getActionTick();
        state.primaryAnimState.copyFrom(entity.primaryAnimState);
        state.reactAnimState.copyFrom(entity.reactAnimState);
    }

    @Override
    protected void scale(SoulReaperRenderState state, PoseStack poseStack) {
        poseStack.scale(VISUAL_SCALE, VISUAL_SCALE, VISUAL_SCALE);
    }

    @Override
    public Identifier getTextureLocation(SoulReaperRenderState state) {
        return TEXTURE;
    }
}

