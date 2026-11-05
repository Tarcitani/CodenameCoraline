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
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

public final class BabyDragonRenderer extends MobRenderer<BabyDragon,BabyDragonRenderState,DragonModel> {
    public static final Identifier TEXTURE=Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID,"textures/entity/baby_dragon.png");
    private final DragonModel[] models;
    private final ItemModelResolver itemModelResolver;
    public BabyDragonRenderer(EntityRendererProvider.Context context) {
        super(context,new BabyDragonModel(context.bakeLayer(BabyDragonModel.LAYER)),.24F);
        this.models = new DragonModel[]{this.model, new YoungDragonModel(context.bakeLayer(YoungDragonModel.LAYER)), new AdultDragonModel(context.bakeLayer(AdultDragonModel.LAYER))};
        this.itemModelResolver = context.getItemModelResolver();
        this.addLayer(new DragonGlowLayer(this));
        this.addLayer(new DragonGearLayer(this));
    }
    @Override public BabyDragonRenderState createRenderState() { return new BabyDragonRenderState(); }
    public static Identifier textureFor(DragonStage stage, int variant) {
        int v = Math.clamp(variant, 0, BabyDragon.VARIANT_COUNT - 1);
        String prefix = stage.name().toLowerCase(java.util.Locale.ROOT) + "_dragon";
        String suffix = v == 0 ? ".png" : "_" + v + ".png";
        return Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/" + prefix + suffix);
    }
    @Override public Identifier getTextureLocation(BabyDragonRenderState state) {
        return textureFor(DragonStage.byId(state.stage), state.variant);
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
        state.flightBlend=dragon.flightBlend; state.flightRoll=dragon.flightRoll;
        state.flightPitch=dragon.flightPitch; state.glideFactor=dragon.glideFactor;
        state.blinkSeed=dragon.getId()*37;
        state.growthScale=dragon.getGrowthScale(); state.stage=dragon.getDragonStage().ordinal(); state.variant=dragon.getTextureVariant();
        state.chargeTicks=dragon.isAlive()?dragon.getChargeTicks():0; state.shotTicks=dragon.isAlive()?dragon.getShotTicks():0;
        state.breathTicks=dragon.isAlive()?dragon.getBreathTicks():0; state.breathClosingTicks=dragon.getBreathClosingTicks();
        state.saddled=dragon.isSaddled(); state.hasTotem=dragon.hasEquippedTotem();
        ItemStack carried=dragon.getCarriedPreyItem();
        state.carryingItem=!carried.isEmpty();
        if (state.saddled) this.itemModelResolver.updateForLiving(state.saddleItem, new ItemStack(Items.SADDLE), ItemDisplayContext.FIXED, dragon);
        else state.saddleItem.clear();
        if (state.hasTotem) this.itemModelResolver.updateForLiving(state.totemItem, new ItemStack(Items.TOTEM_OF_UNDYING), ItemDisplayContext.FIXED, dragon);
        else state.totemItem.clear();
        if (state.carryingItem) this.itemModelResolver.updateForLiving(state.mouthItem, carried, ItemDisplayContext.GROUND, dragon);
        else state.mouthItem.clear();
        dragon.animations.forEach((pose,animation)->state.animations.get(pose).copyFrom(animation));
    }
}
