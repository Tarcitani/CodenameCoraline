package com.codenamecoraline.client.architect;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.architect.Architect;
import com.codenamecoraline.entity.architect.ArchitectAnim;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Emissive layer of the Architect (spec §8.5), chosen by state: nothing while he is a statue, staggered or dying (the
 * eyes go dark), the waking texture during the reveal (seal, tablet glyphs, the light of the mask), the command texture
 * while he channels, and the combat texture otherwise (coral irises, lilac seal, cyan sockets and blade grooves).
 */
public final class ArchitectGlowLayer extends EyesLayer<ArchitectRenderState, ArchitectModel> {
    private static final RenderType COMBAT = type("architect_glow");
    private static final RenderType COMMAND = type("architect_glow_command");
    private static final RenderType WAKING = type("architect_glow_waking");

    public ArchitectGlowLayer(RenderLayerParent<ArchitectRenderState, ArchitectModel> parent) {
        super(parent);
    }

    private static RenderType type(String name) {
        return RenderTypes.eyes(Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/" + name + ".png"));
    }

    @Override
    public RenderType renderType() {
        return COMBAT;
    }

    @Override
    public void submit(PoseStack stack, SubmitNodeCollector collector, int light, ArchitectRenderState state, float yaw,
            float pitch) {
        RenderType type;
        if (state.anim == ArchitectAnim.DORMANT_STATUE || state.anim == ArchitectAnim.STAGGER
                || state.anim == ArchitectAnim.DEATH || state.state == Architect.State.STAGGERED) {
            return;
        } else if (state.anim == ArchitectAnim.AWAKEN) {
            type = WAKING;
        } else if (state.state == Architect.State.CHANNELING) {
            type = COMMAND;
        } else {
            type = COMBAT;
        }
        collector.order(1).submitModel(this.getParentModel(), state, stack, type, light, OverlayTexture.NO_OVERLAY,
                state.outlineColor, null);
    }
}
