package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.babydragon.BabyDragon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public final class BabyDragonRenderer extends MobRenderer<BabyDragon,BabyDragonRenderState,BabyDragonModel> {
    public static final Identifier TEXTURE=Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID,"textures/entity/baby_dragon.png");
    public BabyDragonRenderer(EntityRendererProvider.Context context) { super(context,new BabyDragonModel(context.bakeLayer(BabyDragonModel.LAYER)),.24F); }
    @Override public BabyDragonRenderState createRenderState() { return new BabyDragonRenderState(); }
    @Override public Identifier getTextureLocation(BabyDragonRenderState state) { return TEXTURE; }
    @Override public boolean shouldRender(BabyDragon dragon, Frustum frustum, double x, double y, double z) {
        Minecraft minecraft = Minecraft.getInstance();
        // Like the vanilla shoulder layer, keep the owner's first-person view clear.
        if (dragon.isOnShoulder() && dragon.getOwner() == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) return false;
        return super.shouldRender(dragon, frustum, x, y, z);
    }
    @Override protected void scale(BabyDragonRenderState state,PoseStack stack) { float s=state.shoulder?.58F:.85F; stack.scale(s,s,s); }
    @Override public void extractRenderState(BabyDragon dragon,BabyDragonRenderState state,float partialTick) {
        super.extractRenderState(dragon,state,partialTick);
        state.shoulder=dragon.isOnShoulder(); state.sleeping=dragon.isDragonSleeping();
        state.flightBlend=dragon.flightBlend;
        state.blinkSeed=dragon.getId()*37;
        dragon.animations.forEach((pose,animation)->state.animations.get(pose).copyFrom(animation));
    }
}
