package com.codenamecoraline.client.canopy;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.canopy.CanopyAnim;
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
 * Articulated ape-like model of the Canopy Breaker: pelvis, chest (hunched), head with a hinged jaw, three-segment
 * arms with grasping hands, three-segment legs with long feet, the Deadroot Club in the right hand (visible when drawn)
 * and the same club stowed on the back (visible otherwise). The cubes come from the generated
 * {@link CanopyBreakerParts}; the resting stance is {@link CanopyBreakerAnimations#BASE} and every animation is a set of
 * deltas on top of it. The renderer scales the whole model by 1.2.
 */
public final class CanopyBreakerModel extends EntityModel<CanopyBreakerRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "canopy_breaker"), "main");
    public static final float SCALE = 1.2F;

    private final ModelPart head;
    private final ModelPart clubHand;
    private final ModelPart clubBack;
    private final Map<CanopyAnim, KeyframeAnimation> baked = new EnumMap<>(CanopyAnim.class);

    public CanopyBreakerModel(ModelPart root) {
        super(root);
        var lookup = root.createPartLookup();
        this.head = lookup.apply("head");
        this.clubHand = lookup.apply("club_hand");
        this.clubBack = lookup.apply("club_back");
        for (CanopyAnim anim : CanopyAnim.values()) {
            this.baked.put(anim, CanopyBreakerAnimations.get(anim).bake(root));
        }
    }

    private static PartPose pose(String part, float x, float y, float z) {
        float[] r = CanopyBreakerAnimations.BASE.getOrDefault(part, new float[] {0, 0, 0});
        return PartPose.offsetAndRotation(x, y, z, r[0] * Mth.DEG_TO_RAD, r[1] * Mth.DEG_TO_RAD, r[2] * Mth.DEG_TO_RAD);
    }

    private static PartDefinition add(PartDefinition parent, String name, float x, float y, float z) {
        return parent.addOrReplaceChild(name, CanopyBreakerParts.cubes(name), pose(name, x, y, z));
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition pelvis = add(root, "pelvis", 0, 6, 0);
        PartDefinition chest = add(pelvis, "chest", 0, -1, 0);
        PartDefinition head = add(chest, "head", 0, -14, -2);
        add(head, "jaw", 0, -2, -1);
        for (String side : new String[] {"left", "right"}) {
            float sx = side.equals("left") ? 14 : -14;
            PartDefinition upper = add(chest, side + "_upper_arm", sx, -11, 0);
            PartDefinition fore = add(upper, side + "_forearm", 0, 11, 0);
            PartDefinition hand = add(fore, side + "_hand", 0, 11, 0);
            if (side.equals("right")) {
                add(hand, "club_hand", 0, 4, 0);
            }
            float lx = side.equals("left") ? 4.5F : -4.5F;
            PartDefinition thigh = add(pelvis, side + "_thigh", lx, 2, 0);
            PartDefinition shin = add(thigh, side + "_shin", 0, 8, 0);
            add(shin, side + "_foot", 0, 6, 0);
        }
        add(chest, "club_back", 0, -6, 8);
        return LayerDefinition.create(mesh, CanopyBreakerParts.ATLAS, CanopyBreakerParts.ATLAS);
    }

    @Override
    public void setupAnim(CanopyBreakerRenderState state) {
        super.setupAnim(state);
        this.baked.get(state.anim).apply(state.animState, state.ageInTicks);
        if (state.overlay != null) {
            this.baked.get(state.overlay).apply(state.overlayState, state.ageInTicks);
        }
        this.clubHand.visible = state.clubDrawn;
        this.clubBack.visible = !state.clubDrawn;
        if (state.lookAtTarget) {
            this.head.yRot += Mth.clamp(state.yRot, -35.0F, 35.0F) * Mth.DEG_TO_RAD * 0.5F;
            this.head.xRot += Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD * 0.4F;
        }
    }
}
