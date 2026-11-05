package com.codenamecoraline.client.ten_threads_witch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

/**
 * The thread that ties a puppet to the Matriarch's staff wheel: a quadratic Bezier curve of 12 segments. Silver and slightly
 * slack under gravity while the puppet is calm, a bright crimson line pulled straight and vibrating when a special attack is
 * winding up (the moment it can be cut), and fallen to the ground when the puppet is entangled. No library, just lines.
 */
public final class PuppetThreadRenderLayer extends RenderLayer<PuppetRenderState, PuppetModel> {
    public static final int SEGMENTS = 12;
    private static final int SILVER = 0xD8E2E8;
    private static final int CRIMSON = 0xFF1E42;

    public PuppetThreadRenderLayer(RenderLayerParent<PuppetRenderState, PuppetModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, PuppetRenderState state, float yRot, float xRot) {
        if (state.threadAnchor == null || state.isInvisible) {
            return;
        }
        Vec3 anchor = state.threadAnchor.subtract(state.x, state.y, state.z);
        Vec3 start = new Vec3(0.0D, state.boundingBoxHeight * 0.62D, 0.0D);
        double sag = state.crimson ? 0.0D : state.tangled ? Math.max(0.0D, anchor.y - 0.1D) * 0.9D : 0.7D + start.distanceTo(anchor) * 0.03D;
        Vec3 control = anchor.add(start).scale(0.5D).add(0.0D, -sag, 0.0D);
        if (state.tangled) {
            control = new Vec3(control.x, Math.min(control.y, 0.15D), control.z);
        }
        int rgb = state.crimson ? CRIMSON : SILVER;
        float width = state.crimson ? 4.0F : 2.0F;
        final Vec3 c = control;
        poseStack.pushPose();
        // undo the entity's own transform (flip, scale, body yaw) so the curve is drawn in world axes around the entity
        poseStack.translate(0.0F, 1.501F, 0.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-(180.0F - state.bodyYawDegrees)));
        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, consumer) -> {
            Vec3 prev = null;
            for (int i = 0; i <= SEGMENTS; i++) {
                double t = i / (double) SEGMENTS;
                double a = (1.0D - t) * (1.0D - t);
                double b = 2.0D * (1.0D - t) * t;
                double d = t * t;
                Vec3 p = new Vec3(a * anchor.x + b * c.x + d * start.x, a * anchor.y + b * c.y + d * start.y, a * anchor.z + b * c.z + d * start.z);
                if (state.crimson) {
                    p = p.add(0.0D, Math.sin(state.ageInTicks * 2.2D + i) * 0.045D * Math.sin(t * Math.PI), 0.0D); // vibrates, taut
                }
                if (prev != null) {
                    Vec3 n = p.subtract(prev);
                    float len = (float) Math.max(1.0E-4D, n.length());
                    float nx = (float) n.x / len;
                    float ny = (float) n.y / len;
                    float nz = (float) n.z / len;
                    int color = ARGB.opaque(rgb);
                    consumer.addVertex(pose, (float) prev.x, (float) prev.y, (float) prev.z).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(width);
                    consumer.addVertex(pose, (float) p.x, (float) p.y, (float) p.z).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(width);
                }
                prev = p;
            }
        });
        poseStack.popPose();
    }
}
