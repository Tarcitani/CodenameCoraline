package com.codenamecoraline.client.macaque;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.macaque.MacaqueAnim;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.EnumMap;
import java.util.Map;

/**
 * Articulated model of the Bark Macaque: a crouched body with a bark plate and moss on its back, a head with a hinged jaw,
 * two-segment arms and legs with hands and feet, and a three-segment prehensile tail. The cubes come from the generated
 * {@link BarkMacaqueParts}; every part rests at zero and the animations are whole poses.
 */
public final class BarkMacaqueModel extends EntityModel<BarkMacaqueRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "bark_macaque"), "main");

    private final ModelPart head;
    private final Map<MacaqueAnim, KeyframeAnimation> baked = new EnumMap<>(MacaqueAnim.class);

    public BarkMacaqueModel(ModelPart root) {
        super(root);
        this.head = root.createPartLookup().apply("head");
        for (MacaqueAnim anim : MacaqueAnim.values()) {
            this.baked.put(anim, BarkMacaqueAnimations.get(anim).bake(root));
        }
    }

    private static PartDefinition add(PartDefinition parent, String name, float x, float y, float z) {
        return parent.addOrReplaceChild(name, BarkMacaqueParts.cubes(name), PartPose.offset(x, y, z));
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition body = add(mesh.getRoot(), "body", 0, 13, 0);
        PartDefinition head = add(body, "head", 0, -1, -5);
        add(head, "jaw", 0, 3, 0);
        for (String side : new String[] {"left", "right"}) {
            float sx = side.equals("left") ? 4 : -4;
            PartDefinition arm = add(body, side + "_arm", sx, 1, -4);
            PartDefinition fore = add(arm, side + "_forearm", 0, 4, 0);
            add(fore, side + "_hand", 0, 4, 0);
            PartDefinition thigh = add(body, side + "_thigh", sx, 2, 4);
            PartDefinition shin = add(thigh, side + "_shin", 0, 4, 0);
            add(shin, side + "_foot", 0, 4, 0);
        }
        PartDefinition tailA = add(body, "tail_a", 0, -1, 5);
        PartDefinition tailB = add(tailA, "tail_b", 0, 0, 5);
        add(tailB, "tail_c", 0, 0, 5);
        return LayerDefinition.create(mesh, BarkMacaqueParts.ATLAS, BarkMacaqueParts.ATLAS);
    }

    @Override
    public void setupAnim(BarkMacaqueRenderState state) {
        super.setupAnim(state);
        this.baked.get(state.anim).apply(state.animState, state.ageInTicks);
        if (state.lookAtTarget) {
            this.head.yRot += Mth.clamp(state.yRot, -30.0F, 30.0F) * Mth.DEG_TO_RAD * 0.5F;
        }
    }
}
