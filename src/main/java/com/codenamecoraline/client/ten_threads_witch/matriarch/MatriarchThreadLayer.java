package com.codenamecoraline.client.ten_threads_witch.matriarch;

import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchMood;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/**
 * The Matriarch's own threads, drawn from the tips of her animated fingers (the puppets draw theirs to her staff wheel).
 * The pose's effect channels drive them: per hand, how far the threads have reached out, how taut they are and whether they
 * have just snapped; for attacks, how far the attack threads reach, how many there are (more as she unravels) and how much
 * they whip; and the crimson tint. Control threads of the left hand go to the active puppet, those of the right hand to the
 * puppets still hanging; attack threads fan out towards her focus. Lines only, no library.
 */
public final class MatriarchThreadLayer extends RenderLayer<MatriarchRenderState, MatriarchModel> {
    private static final int SEGMENTS = 10;
    private static final int SILVER = 0xD8E2E8;
    private static final int CRIMSON = 0xFF1E42;
    private static final float TIP = 1.4F / 16.0F;

    private final PoseStack chain = new PoseStack();
    private final Vector3f tmp = new Vector3f();

    public MatriarchThreadLayer(RenderLayerParent<MatriarchRenderState, MatriarchModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, MatriarchRenderState state, float yRot, float xRot) {
        if (state.isInvisible) {
            return;
        }
        float[] p = state.pose;
        int tl = (MatRig.THREAD_L * 3 + MatRig.POS) * 3;
        int tr = (MatRig.THREAD_R * 3 + MatRig.POS) * 3;
        int at = (MatRig.ATTACK * 3 + MatRig.POS) * 3;
        int gl = (MatRig.GLOW * 3 + MatRig.POS) * 3;
        float death = p[(MatRig.DISSOLVE * 3 + MatRig.POS) * 3];
        boolean left = p[tl] > 0.01F && death < MatRig.dissolveOrder("finger_l0");
        boolean right = p[tr] > 0.01F && death < MatRig.dissolveOrder("finger_r0");
        boolean attack = p[at] > 0.01F && state.hasFocus && death < MatRig.dissolveOrder("finger_l0");
        if (!left && !right && !attack) {
            return;
        }
        // fingertips in world axes around her feet, from the posed parts (the same transforms the renderer applied)
        float[] d = new float[72]; // copied per frame: the geometry is drawn later, after other entities' layers ran
        MatriarchModel model = this.getParentModel();
        for (int f = 0; f < 5; f++) {
            this.tip(model, state.bodyYawDegrees, MatRig.HAND_L, MatRig.ARM_L, MatRig.FOREARM_L, MatRig.FINGERS_L[f], d, f * 3);
            this.tip(model, state.bodyYawDegrees, MatRig.HAND_R, MatRig.ARM_R, MatRig.FOREARM_R, MatRig.FINGERS_R[f], d, 15 + f * 3);
        }
        d[30] = p[tl];
        d[31] = p[tl + 1];
        d[32] = p[tl + 2];
        d[33] = p[tr];
        d[34] = p[tr + 1];
        d[35] = p[tr + 2];
        d[36] = p[at];
        d[37] = p[at + 1];
        d[38] = p[at + 2];
        d[39] = Mth.clamp(p[gl + 1], 0.0F, 1.0F);
        d[40] = state.hasFocus ? 1.0F : 0.0F;
        d[41] = state.focusX;
        d[42] = state.focusY;
        d[43] = state.focusZ;
        d[44] = state.ageInTicks;
        d[45] = MatriarchMood.strangeness(state.unravel);
        int hanging = Math.min(state.hangingCount, 6);
        d[46] = hanging;
        System.arraycopy(state.hanging, 0, d, 47, hanging * 3);
        poseStack.pushPose();
        poseStack.translate(0.0F, 1.501F, 0.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-(180.0F - state.bodyYawDegrees)));
        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, consumer) -> draw(pose, consumer, d));
        poseStack.popPose();
    }

    private void tip(MatriarchModel model, float yaw, int hand, int arm, int forearm, int finger, float[] out, int at) {
        PoseStack s = this.chain;
        s.pushPose();
        s.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        s.scale(-1.0F, -1.0F, 1.0F);
        s.translate(0.0F, -1.501F, 0.0F);
        model.root().translateAndRotate(s);
        model.part(MatRig.BODY).translateAndRotate(s);
        model.part(MatRig.CHEST).translateAndRotate(s);
        model.part(arm).translateAndRotate(s);
        model.part(forearm).translateAndRotate(s);
        model.part(hand).translateAndRotate(s);
        model.part(finger).translateAndRotate(s);
        s.last().pose().transformPosition(0.0F, TIP, 0.0F, this.tmp);
        out[at] = this.tmp.x();
        out[at + 1] = this.tmp.y();
        out[at + 2] = this.tmp.z();
        s.popPose();
    }

    private static void draw(PoseStack.Pose pose, VertexConsumer c, float[] d) {
        int color = ARGB.opaque(blendRgb(SILVER, CRIMSON, d[39]));
        float time = d[44];
        boolean focus = d[40] > 0.5F;
        // the left hand: control threads to the active puppet
        if (d[30] > 0.01F) {
            for (int f = 0; f < 4; f++) {
                float tx = focus ? d[41] : d[f * 3];
                float ty = focus ? d[42] : d[f * 3 + 1] - 0.5F;
                float tz = focus ? d[43] : d[f * 3 + 2];
                thread(pose, c, color, d[f * 3], d[f * 3 + 1], d[f * 3 + 2], tx, ty, tz, d[30], d[31], d[32], 0.0F, time + f, 1.5F);
            }
        }
        // the right hand: threads to the puppets still hanging (or to the focus when none hangs)
        if (d[33] > 0.01F) {
            int n = (int) d[46];
            for (int f = 0; f < 4; f++) {
                float tx;
                float ty;
                float tz;
                if (n > 0) {
                    int k = f % n;
                    tx = d[47 + k * 3];
                    ty = d[48 + k * 3];
                    tz = d[49 + k * 3];
                } else if (focus) {
                    tx = d[41];
                    ty = d[42];
                    tz = d[43];
                } else {
                    tx = d[15 + f * 3];
                    ty = d[15 + f * 3 + 1] - 0.5F;
                    tz = d[15 + f * 3 + 2];
                }
                thread(pose, c, color, d[15 + f * 3], d[15 + f * 3 + 1], d[15 + f * 3 + 2], tx, ty, tz, d[33], d[34], d[35], 0.0F, time + 7 + f, 1.5F);
            }
        }
        // the attack threads: a fan towards the focus, more of them as she unravels, whipping
        if (d[36] > 0.01F && focus) {
            int count = Math.min(7, 1 + Math.round(d[37] * (1.0F + 4.0F * d[45])));
            float dx = d[41];
            float dz = d[43];
            float len = Mth.sqrt(dx * dx + dz * dz);
            float px = len > 1.0E-3F ? -dz / len : 1.0F;
            float pz = len > 1.0E-3F ? dx / len : 0.0F;
            int attackColor = ARGB.opaque(blendRgb(SILVER, CRIMSON, Math.max(d[39], 0.5F)));
            for (int k = 0; k < count; k++) {
                float off = (k - (count - 1) * 0.5F) * 0.55F;
                int finger = k % 4;
                thread(pose, c, attackColor, d[finger * 3], d[finger * 3 + 1], d[finger * 3 + 2], d[41] + px * off, d[42], d[43] + pz * off,
                        d[36], 1.0F, 0.0F, d[38], time * 1.7F + k * 2.1F, 2.5F);
            }
        }
    }

    /**
     * One thread from (ax, ay, az) towards (bx, by, bz): drawn up to {@code reach} of the way, sagging as it slackens
     * ({@code tension} 1 is straight), waving by {@code wave}, and when {@code snap} passes one half, broken: two loose ends
     * hanging from both sides.
     */
    private static void thread(PoseStack.Pose pose, VertexConsumer c, int color, float ax, float ay, float az, float bx, float by, float bz,
                               float reach, float tension, float snap, float wave, float phase, float width) {
        float dist = Mth.sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay) + (bz - az) * (bz - az));
        float sag = (1.0F - Mth.clamp(tension, 0.0F, 1.0F)) * (0.25F + dist * 0.08F);
        if (snap > 0.5F) {
            segment(pose, c, color, ax, ay, az, bx, by, bz, 0.0F, 0.3F, sag + 0.6F, 0.0F, phase, width);
            segment(pose, c, color, bx, by, bz, ax, ay, az, 0.0F, 0.3F, sag + 0.6F, 0.0F, phase, width);
            return;
        }
        segment(pose, c, color, ax, ay, az, bx, by, bz, 0.0F, Mth.clamp(reach, 0.0F, 1.0F), sag, wave, phase, width);
    }

    private static void segment(PoseStack.Pose pose, VertexConsumer c, int color, float ax, float ay, float az, float bx, float by, float bz,
                                float t0, float t1, float sag, float wave, float phase, float width) {
        float lx = 0.0F;
        float ly = 0.0F;
        float lz = 0.0F;
        for (int i = 0; i <= SEGMENTS; i++) {
            float t = t0 + (t1 - t0) * i / SEGMENTS;
            float x = ax + (bx - ax) * t;
            float y = ay + (by - ay) * t - sag * 4.0F * t * (1.0F - t);
            float z = az + (bz - az) * t;
            if (wave > 0.0F) {
                y += wave * 0.22F * Mth.sin(t * 9.0F - phase * 0.9F) * Mth.sin(t * Mth.PI);
            }
            if (i > 0) {
                float nx = x - lx;
                float ny = y - ly;
                float nz = z - lz;
                float n = Math.max(1.0E-4F, Mth.sqrt(nx * nx + ny * ny + nz * nz));
                c.addVertex(pose, lx, ly, lz).setColor(color).setNormal(pose, nx / n, ny / n, nz / n).setLineWidth(width);
                c.addVertex(pose, x, y, z).setColor(color).setNormal(pose, nx / n, ny / n, nz / n).setLineWidth(width);
            }
            lx = x;
            ly = y;
            lz = z;
        }
    }

    private static int blendRgb(int a, int b, float t) {
        int r = (int) Mth.lerp(t, (a >> 16) & 255, (b >> 16) & 255);
        int g = (int) Mth.lerp(t, (a >> 8) & 255, (b >> 8) & 255);
        int bl = (int) Mth.lerp(t, a & 255, b & 255);
        return (r << 16) | (g << 8) | bl;
    }
}
