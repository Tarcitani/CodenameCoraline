package com.codenamecoraline.client.ten_threads_witch.matriarch;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/**
 * The Matriarch's model: the generated part tree ({@code TenThreadsParts}), posed every frame from the composed pose of
 * {@link MatriarchAnimator} with the vanilla part offsets (rotation, position, scale). Besides the joints it shows what the
 * pose's effect channels say: the burst spools, the blindfold torn off in the last phase, the eyes swelling as they glow,
 * the entrance revealing her (silhouette first) and the death dissolving her (hands first, the smile last). Parts that
 * vanish stop drawing their own cubes but keep their children, so the order of vanishing is free of the part tree; only
 * leaf parts shrink as they go.
 */
public final class MatriarchModel extends EntityModel<MatriarchRenderState> {
    private final ModelPart[] parts = new ModelPart[MatRig.PARTS.size()];
    private final boolean[] leaf = new boolean[MatRig.PARTS.size()];
    private final float[] dissolveAt = new float[MatRig.PARTS.size()];
    private final float[] revealAt = new float[MatRig.PARTS.size()];
    private final ModelPart[] spools = new ModelPart[10];
    private final ModelPart blindfold;
    private final Vector3f scratch = new Vector3f();

    public MatriarchModel(ModelPart root) {
        super(root);
        var lookup = root.createPartLookup();
        for (int i = 0; i < this.parts.length; i++) {
            String name = MatRig.PARTS.get(i);
            this.parts[i] = lookup.apply(name);
            this.leaf[i] = this.parts[i].getAllParts().size() == 1;
            this.dissolveAt[i] = MatRig.dissolveOrder(name);
            this.revealAt[i] = MatRig.revealOrder(name);
        }
        for (int i = 0; i < 10; i++) {
            this.spools[i] = lookup.apply("spool_" + i);
        }
        this.blindfold = lookup.apply("blindfold");
    }

    public ModelPart part(int joint) {
        return this.parts[joint];
    }

    @Override
    public void setupAnim(MatriarchRenderState state) {
        super.setupAnim(state);
        float[] p = state.pose;
        float death = p[(MatRig.DISSOLVE * 3 + MatRig.POS) * 3];
        float hidden = p[(MatRig.DISSOLVE * 3 + MatRig.POS) * 3 + 1];
        float glow = p[(MatRig.GLOW * 3 + MatRig.POS) * 3];
        for (int j = 0; j < this.parts.length; j++) {
            ModelPart part = this.parts[j];
            int r = (j * 3 + MatRig.ROT) * 3;
            int t = (j * 3 + MatRig.POS) * 3;
            int s = (j * 3 + MatRig.SCALE) * 3;
            if (p[r] != 0.0F || p[r + 1] != 0.0F || p[r + 2] != 0.0F) {
                part.offsetRotation(this.scratch.set(p[r], p[r + 1], p[r + 2]));
            }
            if (p[t] != 0.0F || p[t + 1] != 0.0F || p[t + 2] != 0.0F) {
                part.offsetPos(this.scratch.set(p[t], p[t + 1], p[t + 2]));
            }
            if (p[s] != 0.0F || p[s + 1] != 0.0F || p[s + 2] != 0.0F) {
                part.offsetScale(this.scratch.set(p[s], p[s + 1], p[s + 2]));
            }
            // the death takes this part once the dissolve passes its turn; the entrance shows it once the reveal reaches it
            float reveal = 1.0F - hidden;
            boolean gone = death >= this.dissolveAt[j] || reveal < this.revealAt[j];
            part.skipDraw = gone;
            if (!gone && this.leaf[j]) {
                float shrink = Math.min(Mth.clamp((this.dissolveAt[j] - death) / 0.06F, 0.0F, 1.0F),
                        hidden > 0.0F ? Mth.clamp((reveal - this.revealAt[j]) / 0.06F, 0.0F, 1.0F) : 1.0F);
                if (shrink < 1.0F) {
                    part.xScale *= shrink;
                    part.yScale *= shrink;
                    part.zScale *= shrink;
                }
            }
        }
        // her wrist keeps the staff upright whatever the right arm does; a staff blow (the effect channel's z) or a body
        // thrown off its feet lets it swing with the arm
        ModelPart armR = this.parts[MatRig.ARM_R];
        ModelPart foreR = this.parts[MatRig.FOREARM_R];
        ModelPart chest = this.parts[MatRig.CHEST];
        float free = Mth.clamp(p[(MatRig.GLOW * 3 + MatRig.POS) * 3 + 2] + Math.abs(this.parts[MatRig.BODY].xRot) / (50.0F * Mth.DEG_TO_RAD) - 0.4F, 0.0F, 1.0F);
        float keep = 0.9F * (1.0F - free);
        ModelPart staff = this.parts[MatRig.STAFF];
        staff.xRot -= (armR.xRot + foreR.xRot + chest.xRot) * keep;
        staff.zRot -= (armR.zRot + foreR.zRot + chest.zRot) * keep;
        for (int i = 0; i < 10; i++) {
            this.spools[i].visible = ((state.brokenSpools >> i) & 1) == 0; // a burst spool is gone from the wheel
        }
        this.blindfold.visible = !state.blindfoldOff; // the last phase: the blindfold is torn off
        if (glow > 0.0F) {
            float k = 1.0F + 0.35F * Mth.clamp(glow, 0.0F, 1.0F);
            this.parts[MatRig.EYE_L].xScale *= k;
            this.parts[MatRig.EYE_L].yScale *= k;
            this.parts[MatRig.EYE_R].xScale *= k;
            this.parts[MatRig.EYE_R].yScale *= k;
        }
    }
}
