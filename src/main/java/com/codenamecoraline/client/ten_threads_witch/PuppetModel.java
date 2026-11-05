package com.codenamecoraline.client.ten_threads_witch;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.ten_threads_witch.PuppetAnim;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * The articulated model of the Matriarch or of one puppet (the part tree comes from the generated
 * {@link TenThreadsParts}; each of the eleven has its own model layer, named after its entity). It plays the animation the
 * server selected, hides the Matriarch's burst spools and blindfold, and turns the head towards the target.
 */
public final class PuppetModel extends EntityModel<PuppetRenderState> {
    private final Map<PuppetAnim, KeyframeAnimation> baked = new EnumMap<>(PuppetAnim.class);
    private final ModelPart head;
    private final ModelPart[] spools;
    private final ModelPart blindfold;

    public PuppetModel(ModelPart root, String id) {
        super(root);
        var lookup = root.createPartLookup();
        this.head = TenThreadsParts.partNames(id).contains("head") ? lookup.apply("head") : null;
        boolean witch = id.equals("ten_threads_witch");
        this.blindfold = witch ? lookup.apply("blindfold") : null;
        this.spools = new ModelPart[witch ? 10 : 0];
        for (int i = 0; i < this.spools.length; i++) {
            this.spools[i] = lookup.apply("spool_" + i);
        }
        for (PuppetAnim anim : PuppetAnim.values()) {
            this.baked.put(anim, PuppetAnimations.get(id, anim).bake(root));
        }
    }

    public static ModelLayerLocation layer(String id) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, id), "main");
    }

    @Override
    public void setupAnim(PuppetRenderState state) {
        super.setupAnim(state);
        this.baked.get(state.anim).apply(state.animState, state.ageInTicks);
        for (int i = 0; i < this.spools.length; i++) {
            this.spools[i].visible = ((state.brokenSpools >> i) & 1) == 0; // a burst spool is gone from the wheel
        }
        if (this.blindfold != null) {
            this.blindfold.visible = !state.fury; // the last phase: the blindfold is torn off
        }
        if (state.lookAtTarget && this.head != null && state.anim != PuppetAnim.SLEEP && !state.tangled) {
            this.head.yRot += Mth.clamp(state.yRot, -35.0F, 35.0F) * Mth.DEG_TO_RAD * 0.5F;
            this.head.xRot += Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD * 0.4F;
        }
    }
}
