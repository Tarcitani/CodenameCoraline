package com.codenamecoraline.client.firedragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.firedragon.FireDragon;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public final class FireDragonRenderer extends MobRenderer<FireDragon, FireDragonRenderState, FireDragonModel> {
    private static final float FIRST_PERSON_SHOULDER_SCALE = 0.60F;
    private static final double FIRST_PERSON_SHOULDER_ANCHOR_HEIGHT = 10.0 / 16.0;
    private static final Identifier[] SKINS = new Identifier[125];
    static {
        for (int p = 0; p < 5; p++) for (int s = 0; s < 5; s++) for (int a = 0; a < 5; a++)
            SKINS[p * 25 + s * 5 + a] = Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID,
                    "textures/entity/fire_dragon/" + p + "_" + s + "_" + a + ".png");
    }
    public FireDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new FireDragonModel(context.bakeLayer(FireDragonModel.LAYER)), 0.3F);
        addLayer(new FireDragonEyesLayer(this));
    }
    @Override public FireDragonRenderState createRenderState() { return new FireDragonRenderState(); }
    @Override public Identifier getTextureLocation(FireDragonRenderState state) { return SKINS[state.colorCode]; }
    @Override public net.minecraft.world.phys.Vec3 getRenderOffset(FireDragonRenderState state) {
        return super.getRenderOffset(state).add(state.shoulderOffset);
    }
    @Override protected float getFlipDegrees() { return 0.0F; }
    @Override public void extractRenderState(FireDragon entity, FireDragonRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.hurtTicks = Math.max(0, entity.hurtTime - partialTick);
        state.colorCode = entity.getColorCode();
        state.airborne = !entity.onGround() && !entity.isInWater();
        state.perched = entity.isOnShoulder();
        state.shoulderSide = entity.getShoulderSide();
        state.shoulderOffset = net.minecraft.world.phys.Vec3.ZERO;
        if (state.perched && entity.getOwner() instanceof net.minecraft.world.entity.player.Player owner) {
            // Render against the owner's current interpolated position, not delayed mob packets.
            float yaw = net.minecraft.util.Mth.rotLerp(partialTick, owner.yBodyRotO, owner.yBodyRot);
            double radians = Math.toRadians(yaw);
            var pos = owner.getPosition(partialTick);
            double side = state.shoulderSide * .32;
            state.shoulderOffset = new net.minecraft.world.phys.Vec3(
                    pos.x + Math.cos(radians) * side - state.x,
                    pos.y + owner.getBbHeight() - .75 - state.y,
                    pos.z + Math.sin(radians) * side - state.z);
            state.bodyRot = yaw;
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.getCameraEntity() == owner && minecraft.options.getCameraType().isFirstPerson()) {
                // First-person attachment must share the camera's frame, not the walking body's
                // yaw or the mob's separately interpolated movement packets.
                var camera = minecraft.gameRenderer.getMainCamera();
                double cameraYaw = Math.toRadians(camera.yRot());
                var cameraPos = camera.position();
                double baseScale = state.scale;
                state.scale *= FIRST_PERSON_SHOULDER_SCALE;
                double heightCompensation = FIRST_PERSON_SHOULDER_ANCHOR_HEIGHT
                        * baseScale * (1 - FIRST_PERSON_SHOULDER_SCALE);
                state.shoulderOffset = new net.minecraft.world.phys.Vec3(
                        cameraPos.x + Math.cos(cameraYaw) * side - state.x,
                        cameraPos.y - owner.getEyeHeight() + owner.getBbHeight() - .75
                                + heightCompensation - state.y,
                        cameraPos.z + Math.sin(cameraYaw) * side - state.z);
                state.bodyRot = camera.yRot();
                state.yRot = 0;
                state.xRot = 0;
            }
        }
        state.sitting = entity.isInSittingPose();
        state.glowing = !entity.isDeadOrDying();
        state.preview = entity.isVisualPreview();
        state.clip = entity.getVisualAnimation();
        state.clipTicks = entity.getVisualAnimationTicks(partialTick);
    }
}
