package com.codenamecoraline.client.ten_threads_witch;

import com.codenamecoraline.CodenameCoraline;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** The emissive parts of a puppet or of the Matriarch: button eyes, burst-ready spools, the glowing seam of the neck. */
public final class TenThreadsEyesLayer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends EyesLayer<S, M> {
    private final RenderType type;

    public TenThreadsEyesLayer(RenderLayerParent<S, M> parent, String id) {
        super(parent);
        this.type = RenderTypes.eyes(Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/" + id + "_eyes.png"));
    }

    @Override
    public RenderType renderType() {
        return this.type;
    }
}
