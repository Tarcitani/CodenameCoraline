package com.codenamecoraline.client.crimson;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.crimson.CrimsonAnim;
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
 * Articulated armoured knight of the Crimson Commander, drawn at real size (3.5 blocks): pelvis with the heraldic
 * insignia, chest, closed helm with horns and a long plume, three-segment arms under spiked pauldrons, the Crimson Oath
 * Greatsword in the right fist (the blade points along -z from the grip), a tattered cape and armoured legs. The cubes
 * come from the generated {@link CrimsonCommanderParts}; the resting stance is {@link CrimsonCommanderAnimations#BASE} and
 * every animation is a set of deltas on top of it.
 *
 * <p>The Crimson Ash Vanguards wear the same model, scaled down by their renderer, without plume, cape or spikes.</p>
 */
public final class CrimsonCommanderModel extends EntityModel<CrimsonCommanderRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "crimson_commander"), "main");

    private final ModelPart head;
    private final ModelPart plume;
    private final ModelPart cape;
    private final ModelPart[] spikes;
    private final Map<CrimsonAnim, KeyframeAnimation> baked = new EnumMap<>(CrimsonAnim.class);

    public CrimsonCommanderModel(ModelPart root) {
        super(root);
        var lookup = root.createPartLookup();
        this.head = lookup.apply("head");
        this.plume = lookup.apply("plume");
        this.cape = lookup.apply("cape");
        this.spikes = new ModelPart[] {lookup.apply("r_spike_a"), lookup.apply("r_spike_b"), lookup.apply("r_spike_c"),
                lookup.apply("l_spike_a"), lookup.apply("l_spike_b"), lookup.apply("l_spike_c")};
        for (CrimsonAnim anim : CrimsonAnim.values()) {
            this.baked.put(anim, CrimsonCommanderAnimations.get(anim).bake(root));
        }
    }

    private static PartPose pose(String part, float x, float y, float z) {
        float[] r = CrimsonCommanderAnimations.BASE.getOrDefault(part, new float[] {0, 0, 0});
        return PartPose.offsetAndRotation(x, y, z, r[0] * Mth.DEG_TO_RAD, r[1] * Mth.DEG_TO_RAD, r[2] * Mth.DEG_TO_RAD);
    }

    private static PartDefinition add(PartDefinition parent, String name, float x, float y, float z) {
        return parent.addOrReplaceChild(name, CrimsonCommanderParts.cubes(name), pose(name, x, y, z));
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition pelvis = add(root, "pelvis", 0, 0, 0);
        PartDefinition chest = add(pelvis, "chest", 0, -3, 0);
        PartDefinition head = add(chest, "head", 0, -20, -0.5F);
        add(head, "horn_l", 4.4F, -4, -2.5F);
        add(head, "horn_r", -4.4F, -4, -2.5F);
        PartDefinition plume = add(head, "plume", 0, -9, 1.5F);
        add(plume, "plume_tail", 0, -12.5F, 0);
        add(chest, "cape", 0, -18.5F, 5.5F);
        for (String side : new String[] {"left", "right"}) {
            String s = side.substring(0, 1);
            float sx = side.equals("left") ? 1 : -1;
            PartDefinition arm = add(chest, side + "_arm", 10.5F * sx, -17, 0);
            add(arm, s + "_spike_a", 3.5F * sx, -3.5F, -3);
            add(arm, s + "_spike_b", 4.5F * sx, -3, 0.5F);
            add(arm, s + "_spike_c", 3.5F * sx, -3.5F, 3.5F);
            PartDefinition fore = add(arm, side + "_forearm", 0, 10, 0);
            PartDefinition hand = add(fore, side + "_hand", 0, 9, 0);
            if (side.equals("right")) {
                add(hand, "sword", 0, 2, 0);
            }
            PartDefinition thigh = add(pelvis, side + "_thigh", 3.6F * sx, 1, 0);
            PartDefinition shin = add(thigh, side + "_shin", 0, 12, 0);
            add(shin, side + "_foot", 0, 9, 0);
        }
        return LayerDefinition.create(mesh, CrimsonCommanderParts.ATLAS, CrimsonCommanderParts.ATLAS);
    }

    @Override
    public void setupAnim(CrimsonCommanderRenderState state) {
        super.setupAnim(state);
        this.baked.get(state.anim).apply(state.animState, state.ageInTicks);
        if (state.overlay != null) {
            this.baked.get(state.overlay).apply(state.overlayState, state.ageInTicks);
        }
        this.plume.visible = !state.vanguard;
        this.cape.visible = !state.vanguard;
        for (ModelPart spike : this.spikes) {
            spike.visible = !state.vanguard;
        }
        if (state.lookAtTarget) {
            this.head.yRot += Mth.clamp(state.yRot, -40.0F, 40.0F) * Mth.DEG_TO_RAD * 0.6F;
            this.head.xRot += Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD * 0.5F;
        }
        // the cape and the plume trail behind when he moves
        float sway = Math.min(1.0F, state.walkAnimationSpeed) * Mth.DEG_TO_RAD;
        this.cape.xRot += sway * (14.0F + 4.0F * Mth.sin(state.ageInTicks * 0.35F));
        this.plume.xRot -= sway * 6.0F;
    }
}
