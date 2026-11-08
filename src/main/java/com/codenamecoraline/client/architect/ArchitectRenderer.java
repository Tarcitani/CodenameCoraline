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
        super(context, new ArchitectModel(context.bakeLayer(ArchitectModel.LAYER)), 1.0F * Architect.SCALE);
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
        state.lookAtTarget = switch (state.anim) {
            case BATTLE_IDLE, WALK, RUN, GUARD_FULL, STRAFE_LEFT, STRAFE_RIGHT, BACKPEDAL -> true;
            // the blows, dodges and parries lock the body: the gaze still follows the target, with the shoulders less
            case SIXFOLD_EDICT, CROSSED_MANDATE, SPLITTER_CRESCENT, PIERCING_SENTENCE, SHIELD_BREAKER, FALSE_VERDICT,
                 RUSHING_REBUKE, DODGE_LEFT, DODGE_RIGHT, DODGE_BACK, DODGE_DIAG_LEFT, DODGE_DIAG_RIGHT, LEAN_LEFT,
                 LEAN_RIGHT, PARRY_LEFT, PARRY_RIGHT, PROJECTILE_DEFLECT, BACKSTEP -> true;
            default -> false;
        };
        state.lookShare = state.state == Architect.State.COMBAT_IDLE || state.state == Architect.State.GUARDING ? 1.0F : 0.4F;
    }
}
