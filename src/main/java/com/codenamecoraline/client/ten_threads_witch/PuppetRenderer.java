package com.codenamecoraline.client.ten_threads_witch;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.ten_threads_witch.MatriarchWitchEntity;
import com.codenamecoraline.entity.ten_threads_witch.PuppetEntity;
import com.codenamecoraline.entity.ten_threads_witch.PuppetKind;
import com.codenamecoraline.entity.ten_threads_witch.PuppetState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/** Client renderer of one puppet: its own model and texture, the emissive layer and the thread to the Matriarch's staff. */
public final class PuppetRenderer extends MobRenderer<PuppetEntity, PuppetRenderState, PuppetModel> {
    private final Identifier texture;

    public PuppetRenderer(EntityRendererProvider.Context context, PuppetKind kind) {
        super(context, new PuppetModel(context.bakeLayer(PuppetModel.layer(kind.id)), kind.id), Math.max(0.35F, kind.width * 0.5F));
        this.texture = Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/" + kind.id + ".png");
        this.addLayer(new TenThreadsEyesLayer<>(this, kind.id));
        this.addLayer(new PuppetThreadRenderLayer(this));
    }

    @Override
    public PuppetRenderState createRenderState() {
        return new PuppetRenderState();
    }

    @Override
    public Identifier getTextureLocation(PuppetRenderState state) {
        return this.texture;
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F; // a puppet folds in place when its thread is cut
    }

    @Override
    public void extractRenderState(PuppetEntity entity, PuppetRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.anim = entity.getAnim();
        state.animState.copyFrom(entity.animState);
        state.crimson = entity.isCrimsonThreadActive();
        state.tangled = entity.isTangled();
        state.cracked = entity.isCracked();
        state.lookAtTarget = entity.getPuppetState() == PuppetState.IDLE_COMBAT;
        state.bodyYawDegrees = entity.yBodyRot;
        Entity owner = entity.level().getEntity(entity.getOwnerEntityId());
        state.threadAnchor = owner instanceof MatriarchWitchEntity witch && witch.isAlive() ? witch.getThreadAnchor() : null;
    }
}
