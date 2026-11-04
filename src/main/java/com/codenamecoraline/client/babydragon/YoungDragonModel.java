package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.babydragon.DragonStage;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;

/** Adolescent anatomy: long chest and neck, segmented wings, stronger legs and four tail joints. */
public final class YoungDragonModel extends DragonModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "young_dragon"), "main");
    public YoungDragonModel(ModelPart root) { super(root, DragonStage.YOUNG); }
    public static LayerDefinition createBodyLayer() { return DragonGeometry.layer(DragonStage.YOUNG); }
}
