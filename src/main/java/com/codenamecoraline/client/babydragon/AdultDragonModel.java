package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.babydragon.DragonStage;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;

/** Fire dragon anatomy: armoured ribcage, recurved horns, fan wings and a six-joint flame-tipped tail. */
public final class AdultDragonModel extends DragonModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "adult_dragon"), "main");
    public AdultDragonModel(ModelPart root) { super(root, DragonStage.ADULT); }
    public static LayerDefinition createBodyLayer() { return DragonGeometry.layer(DragonStage.ADULT); }
}
