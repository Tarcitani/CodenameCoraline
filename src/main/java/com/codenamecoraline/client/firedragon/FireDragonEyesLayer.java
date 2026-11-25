package com.codenamecoraline.client.firedragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.firedragon.FireDragon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Emissive eye highlights, sharing the exact base UV coordinates for baby, young and adult stages. */
public final class FireDragonEyesLayer extends EyesLayer<FireDragonRenderState, FireDragonModel> {
    private static final RenderType BABY_EYE_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/fire_dragon/eyes.png"));
    private static final RenderType YOUNG_EYE_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/fire_dragon/young/eyes.png"));
    private static final RenderType ADULT_EYE_GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/fire_dragon/adult/eyes.png"));

    private final FireDragon.Stage stage;

    public FireDragonEyesLayer(RenderLayerParent<FireDragonRenderState, FireDragonModel> parent, FireDragon.Stage stage) {
        super(parent);
        this.stage = stage;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       FireDragonRenderState state, float yRot, float xRot) {
        FireDragon.Stage active = state.adult ? FireDragon.Stage.ADULT : state.young ? FireDragon.Stage.YOUNG : FireDragon.Stage.BABY;
        if (state.glowing && active == stage) {
            super.submit(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot);
        }
    }

    @Override
    public RenderType renderType() {
        return switch (stage) {
            case BABY -> BABY_EYE_GLOW;
            case YOUNG -> YOUNG_EYE_GLOW;
            case ADULT -> ADULT_EYE_GLOW;
        };
    }
}
