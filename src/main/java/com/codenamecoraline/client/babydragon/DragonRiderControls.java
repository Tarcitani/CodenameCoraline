package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.babydragon.BabyDragon;
import com.codenamecoraline.entity.babydragon.DragonRiderPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = CodenameCoraline.MOD_ID, value = Dist.CLIENT)
public final class DragonRiderControls {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "dragon"));
    public static final KeyMapping DESCEND = new KeyMapping("key.codenamecoraline.dragon.descend", InputConstants.KEY_X, CATEGORY);
    public static final KeyMapping DISMOUNT = new KeyMapping("key.codenamecoraline.dragon.dismount", InputConstants.KEY_G, CATEGORY);
    public static final KeyMapping FIREBALL = new KeyMapping("key.codenamecoraline.dragon.fireball", InputConstants.KEY_R, CATEGORY);
    // V already skips boss cinematics; B avoids that existing binding.
    public static final KeyMapping BREATH = new KeyMapping("key.codenamecoraline.dragon.breath", InputConstants.KEY_B, CATEGORY);
    @SubscribeEvent public static void register(RegisterKeyMappingsEvent event) {
        event.register(DESCEND); event.register(DISMOUNT); event.register(FIREBALL); event.register(BREATH);
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.getVehicle() instanceof BabyDragon dragon) || dragon.getRider() != mc.player) {
            while (FIREBALL.consumeClick()) {} while (BREATH.consumeClick()) {} while (DISMOUNT.consumeClick()) {}
            return;
        }
        int buttons = 0;
        if (mc.screen == null) {
            if (mc.options.keyUp.isDown()) buttons |= 1;
            if (mc.options.keyDown.isDown()) buttons |= 2;
            if (mc.options.keyJump.isDown()) buttons |= 4;
            if (DESCEND.isDown()) buttons |= 8;
            if (FIREBALL.consumeClick() || FIREBALL.isDown()) buttons |= 16;
            if (BREATH.consumeClick() || BREATH.isDown()) buttons |= 32;
            if (DISMOUNT.consumeClick()) buttons |= 64;
            if (mc.options.keyLeft.isDown()) mc.player.setYRot(mc.player.getYRot() - 4);
            if (mc.options.keyRight.isDown()) mc.player.setYRot(mc.player.getYRot() + 4);
        }
        ClientPacketDistributor.sendToServer(new DragonRiderPayload(buttons, mc.player.getYRot(), mc.player.getXRot()));
    }
    private DragonRiderControls() {}
}
