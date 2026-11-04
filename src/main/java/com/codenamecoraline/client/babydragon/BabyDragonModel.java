package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.babydragon.DragonStage;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;

/** Refined hatchling: rounded cheeks, small horns, defined toes and delicate wing fingers. */
public final class BabyDragonModel extends DragonModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "baby_dragon"), "main");
    public BabyDragonModel(ModelPart root) { super(root, DragonStage.BABY); }
    public static LayerDefinition createBodyLayer() { return DragonGeometry.layer(DragonStage.BABY); }
}
