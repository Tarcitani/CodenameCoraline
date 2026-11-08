package com.codenamecoraline.client.architect;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.architect.Architect;
import com.codenamecoraline.entity.architect.ArchitectAnim;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/** Client renderer of the Architect ({@code codenamecoraline:architect}), drawn at real size. */
public final class ArchitectRenderer extends MobRenderer<Architect, ArchitectRenderState, ArchitectModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/architect.png");

    public ArchitectRenderer(EntityRendererProvider.Context context) {
        super(context, new ArchitectModel(context.bakeLayer(ArchitectModel.LAYER)), 1.0F);
        this.addLayer(new ArchitectGlowLayer(this));
    }

    @Override
    public ArchitectRenderState createRenderState() {
        return new ArchitectRenderState();
    }

    @Override
    public Identifier getTextureLocation(ArchitectRenderState state) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F; // the death clip collapses him in stages instead of toppling over
    }

    @Override
    public void extractRenderState(Architect entity, ArchitectRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.anim = entity.getAnim();
        state.animState.copyFrom(entity.animState);
        state.state = entity.getState();
        state.lookAtTarget = state.anim == ArchitectAnim.BATTLE_IDLE || state.anim == ArchitectAnim.WALK
                || state.anim == ArchitectAnim.RUN || state.anim == ArchitectAnim.GUARD_FULL;
    }
}
