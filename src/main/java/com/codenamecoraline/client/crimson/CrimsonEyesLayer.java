package com.codenamecoraline.client.crimson;

import com.codenamecoraline.CodenameCoraline;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Emissive layer of the crimson armour: the ruby T of the visor and the runes of the blade (or a vanguard's seams). */
public final class CrimsonEyesLayer extends EyesLayer<CrimsonCommanderRenderState, CrimsonCommanderModel> {
    public static final RenderType COMMANDER = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/crimson_commander_eyes.png"));
    public static final RenderType VANGUARD = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/crimson_ash_vanguard_eyes.png"));

    private final RenderType type;

    public CrimsonEyesLayer(RenderLayerParent<CrimsonCommanderRenderState, CrimsonCommanderModel> parent, RenderType type) {
        super(parent);
        this.type = type;
    }

    @Override
    public RenderType renderType() {
        return this.type;
    }
}
