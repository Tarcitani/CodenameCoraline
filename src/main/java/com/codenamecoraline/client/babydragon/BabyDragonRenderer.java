package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.babydragon.BabyDragon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import com.codenamecoraline.entity.babydragon.DragonStage;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;

public final class BabyDragonRenderer extends MobRenderer<BabyDragon,BabyDragonRenderState,DragonModel> {
    public static final Identifier TEXTURE=Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID,"textures/entity/baby_dragon.png");
    private final DragonModel[] models;
    public BabyDragonRenderer(EntityRendererProvider.Context context) {
        super(context,new BabyDragonModel(context.bakeLayer(BabyDragonModel.LAYER)),.24F);
        this.models = new DragonModel[]{this.model, new YoungDragonModel(context.bakeLayer(YoungDragonModel.LAYER)), new AdultDragonModel(context.bakeLayer(AdultDragonModel.LAYER))};
        this.addLayer(new DragonGlowLayer(this));
    }
    @Override public BabyDragonRenderState createRenderState() { return new BabyDragonRenderState(); }
    @Override public Identifier getTextureLocation(BabyDragonRenderState state) {
        return Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID,"textures/entity/"+DragonStage.byId(state.stage).name().toLowerCase(java.util.Locale.ROOT)+"_dragon.png");
    }
    @Override public void submit(BabyDragonRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        this.model = this.models[Math.clamp(state.stage,0,2)];
        super.submit(state,stack,collector,camera);
    }
    @Override protected AABB getBoundingBoxForCulling(BabyDragon dragon) {
        return super.getBoundingBoxForCulling(dragon).inflate(dragon.getDragonStage() == DragonStage.ADULT ? 7 : dragon.getDragonStage() == DragonStage.YOUNG ? 3 : .7);
    }
    @Override protected float getShadowRadius(BabyDragonRenderState state) { return .24F*state.growthScale*(1+state.stage*.35F); }
    @Override public boolean shouldRender(BabyDragon dragon, Frustum frustum, double x, double y, double z) {
        Minecraft minecraft = Minecraft.getInstance();
        // Like the vanilla shoulder layer, keep the owner's first-person view clear.
        if (dragon.isOnShoulder() && dragon.getOwner() == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) return false;
        return super.shouldRender(dragon, frustum, x, y, z);
    }
    @Override protected void scale(BabyDragonRenderState state,PoseStack stack) { float s=state.shoulder?.58F:.85F*state.growthScale; stack.scale(s,s,s); }
    @Override public void extractRenderState(BabyDragon dragon,BabyDragonRenderState state,float partialTick) {
        super.extractRenderState(dragon,state,partialTick);
        state.shoulder=dragon.isOnShoulder(); state.sleeping=dragon.isDragonSleeping();
        state.flightBlend=dragon.flightBlend;
        state.blinkSeed=dragon.getId()*37;
        state.growthScale=dragon.getGrowthScale(); state.stage=dragon.getDragonStage().ordinal();
        state.chargeTicks=dragon.isAlive()?dragon.getChargeTicks():0; state.shotTicks=dragon.isAlive()?dragon.getShotTicks():0;
        state.breathTicks=dragon.isAlive()?dragon.getBreathTicks():0; state.breathClosingTicks=dragon.getBreathClosingTicks();
        dragon.animations.forEach((pose,animation)->state.animations.get(pose).copyFrom(animation));
    }
}
