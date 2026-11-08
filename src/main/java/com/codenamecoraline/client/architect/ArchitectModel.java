package com.codenamecoraline.client.architect;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.architect.ArchitectAnim;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
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

/**
 * The Architect, drawn at real size (4.7 blocks since the reform): a hooded stone statue with a two-piece mask, a hinged
 * jaw with its own row of teeth, six arms in three tiers (shoulder, upper arm, elbow, forearm, wrist, hand and a
 * scimitar each), two gargoyle wings, a long stone coat in independent pieces and the Tablet of the Commandments. The rig and the cubes come from the generated
 * {@link ArchitectParts}; the battle idle {@link ArchitectAnimations#BASE} is baked into the parts and every clip is a
 * set of deltas on top of it (spec docs/design/architect.md §5, §15).
 */
public final class ArchitectModel extends EntityModel<ArchitectRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "architect"), "main");

    private final ModelPart head;
    private final ModelPart neck;
    private final ModelPart torso;
    private final ModelPart maskLeft;
    private final ModelPart maskRight;
    private final ModelPart tablet;
    private final Map<ArchitectAnim, KeyframeAnimation> baked = new EnumMap<>(ArchitectAnim.class);

    public ArchitectModel(ModelPart root) {
        super(root);
        var lookup = root.createPartLookup();
        this.head = lookup.apply("head");
        this.neck = lookup.apply("neck");
        this.torso = lookup.apply("torso");
        this.maskLeft = lookup.apply("face_mask_left");
        this.maskRight = lookup.apply("face_mask_right");
        this.tablet = lookup.apply("tablet_anchor");
        for (ArchitectAnim anim : ArchitectAnim.values()) {
            this.baked.put(anim, ArchitectAnimations.get(anim).bake(root));
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        Map<String, PartDefinition> defs = new HashMap<>();
        for (ArchitectParts.Part p : ArchitectParts.PARTS) {
            PartDefinition parent = p.parent() == null ? mesh.getRoot() : defs.get(p.parent());
            float[] r = ArchitectAnimations.BASE.getOrDefault(p.name(), new float[] {0, 0, 0});
            PartPose pose = PartPose.offsetAndRotation(p.x(), p.y(), p.z(),
                    r[0] * Mth.DEG_TO_RAD, r[1] * Mth.DEG_TO_RAD, r[2] * Mth.DEG_TO_RAD);
            defs.put(p.name(), parent.addOrReplaceChild(p.name(), ArchitectParts.cubes(p.name()), pose));
        }
        return LayerDefinition.create(mesh, ArchitectParts.ATLAS, ArchitectParts.ATLAS);
    }

    @Override
    public void setupAnim(ArchitectRenderState state) {
        super.setupAnim(state);
        this.baked.get(state.anim).apply(state.animState, state.ageInTicks);
        // the mask and the tablet belong to the disguise: gone once the reveal is over
        boolean disguise = state.anim == ArchitectAnim.DORMANT_STATUE || state.anim == ArchitectAnim.AWAKEN;
        this.maskLeft.visible = disguise;
        this.maskRight.visible = disguise;
        this.tablet.visible = disguise;
        if (state.lookAtTarget) {
            // The gaze is server state (the head leads the body by up to 80 degrees). The head turns first, the neck and
            // the shoulders follow, so the difference between head and hips is shared out instead of snapping (spec §10).
            // In a blow or a dodge the body is locked and the share of the shoulders is smaller.
            float share = state.lookShare;
            float yaw = Mth.clamp(state.yRot, -80.0F, 80.0F) * Mth.DEG_TO_RAD;
            float pitch = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;
            this.head.yRot += yaw * 0.55F;
            this.neck.yRot += yaw * 0.20F;
            this.torso.yRot += yaw * 0.22F * share;
            this.head.xRot += pitch * 0.6F;
        }
    }
}
