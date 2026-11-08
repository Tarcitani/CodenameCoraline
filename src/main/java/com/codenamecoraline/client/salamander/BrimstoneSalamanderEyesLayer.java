package com.codenamecoraline.client.salamander;

import com.codenamecoraline.CodenameCoraline;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Emissive layer for the Brimstone Salamander's glowing throat bellows, eyes, forked tongue,
 * sulphur/mineral spots, and magma/soul/crimson fissures along the dorsal ridge and tail.
 */
public final class BrimstoneSalamanderEyesLayer
        extends EyesLayer<BrimstoneSalamanderRenderState, BrimstoneSalamanderModel> {
    private static final RenderType BRIMSTONE_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/brimstone_salamander_eyes.png"));
    private static final RenderType SOULFLAME_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/brimstone_salamander_soulflame_eyes.png"));
    private static final RenderType CRIMSON_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/brimstone_salamander_crimson_eyes.png"));

    private static final RenderType BRIMSTONE_BABY_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/brimstone_salamander_baby_eyes.png"));
    private static final RenderType SOULFLAME_BABY_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/brimstone_salamander_soulflame_baby_eyes.png"));
    private static final RenderType CRIMSON_BABY_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/brimstone_salamander_crimson_baby_eyes.png"));

    public BrimstoneSalamanderEyesLayer(
            RenderLayerParent<BrimstoneSalamanderRenderState, BrimstoneSalamanderModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       BrimstoneSalamanderRenderState state, float yRot, float xRot) {
        if (state.fissuresGlowing) {
            RenderType type = state.isBaby
                    ? switch (state.variant) {
                        case BRIMSTONE -> BRIMSTONE_BABY_GLOW;
                        case SOULFLAME -> SOULFLAME_BABY_GLOW;
                        case CRIMSON -> CRIMSON_BABY_GLOW;
                    }
                    : switch (state.variant) {
                        case BRIMSTONE -> BRIMSTONE_GLOW;
                        case SOULFLAME -> SOULFLAME_GLOW;
                        case CRIMSON -> CRIMSON_GLOW;
                    };
            submitNodeCollector.submitModel(
                    this.getParentModel(),
                    state,
                    poseStack,
                    type,
                    15728640,
                    OverlayTexture.NO_OVERLAY,
                    state.outlineColor,
                    null);
        }
    }

    @Override
    public RenderType renderType() {
        return BRIMSTONE_GLOW;
    }
}


