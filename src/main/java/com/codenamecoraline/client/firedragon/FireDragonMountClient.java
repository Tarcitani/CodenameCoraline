package com.codenamecoraline.client.firedragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.firedragon.FireDragon;
import com.codenamecoraline.entity.firedragon.FireDragonNetwork;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

/**
 * Client-side controls (Z/X/C/V conflict-scoped to mounted Adult Fire Dragon, plus Space/Ctrl vertical flight)
 * and compact mounted ability cooldown HUD.
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID, value = Dist.CLIENT)
public final class FireDragonMountClient {
    private FireDragonMountClient() {}

    /** Scopes Z/X/C/V exclusively to when riding an Adult Fire Dragon so they never conflict with vanilla or cinematics. */
    public static final IKeyConflictContext MOUNT_CONTEXT = new IKeyConflictContext() {
        @Override
        public boolean isActive() {
            Minecraft mc = Minecraft.getInstance();
            return mc.player != null && mc.screen == null
                    && mc.player.getVehicle() instanceof FireDragon d && d.isAdult();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return this == other;
        }
    };

    public static final KeyMapping KEY_METEOR = new KeyMapping(
            "key.codenamecoraline.dragon_meteor", MOUNT_CONTEXT, InputConstants.Type.KEYSYM,
            InputConstants.KEY_Z, KeyMapping.Category.GAMEPLAY);
    public static final KeyMapping KEY_BREATH = new KeyMapping(
            "key.codenamecoraline.dragon_breath", MOUNT_CONTEXT, InputConstants.Type.KEYSYM,
            InputConstants.KEY_X, KeyMapping.Category.GAMEPLAY);
    public static final KeyMapping KEY_REND = new KeyMapping(
            "key.codenamecoraline.dragon_rend", MOUNT_CONTEXT, InputConstants.Type.KEYSYM,
            InputConstants.KEY_C, KeyMapping.Category.GAMEPLAY);
    public static final KeyMapping KEY_ROAR = new KeyMapping(
            "key.codenamecoraline.dragon_roar", MOUNT_CONTEXT, InputConstants.Type.KEYSYM,
            InputConstants.KEY_V, KeyMapping.Category.GAMEPLAY);

    private static final KeyMapping[] ABILITY_KEYS = { KEY_METEOR, KEY_BREATH, KEY_REND, KEY_ROAR };
    private static final int[] ABILITY_MAX_CD = { 50, 120, 30, 240 };
    private static final String[] ABILITY_NAMES = {
            "hud.codenamecoraline.dragon.meteor",
            "hud.codenamecoraline.dragon.breath",
            "hud.codenamecoraline.dragon.rend",
            "hud.codenamecoraline.dragon.roar"
    };

    private static boolean lastAscend;
    private static boolean lastDescend;
    private static int heartbeatTicks;

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null
                || !(mc.player.getVehicle() instanceof FireDragon dragon) || !dragon.isAdult()) {
            lastAscend = false;
            lastDescend = false;
            return;
        }
        boolean ascend = mc.options.keyJump.isDown();
        boolean descend = mc.options.keySprint.isDown();
        if (ascend != lastAscend || descend != lastDescend || (++heartbeatTicks % 4 == 0 && (ascend || descend))) {
            lastAscend = ascend;
            lastDescend = descend;
            ClientPacketDistributor.sendToServer(new FireDragonNetwork.MountInputPayload(dragon.getId(), ascend, descend));
        }
        for (int i = 0; i < ABILITY_KEYS.length; i++) {
            while (ABILITY_KEYS[i].consumeClick()) {
                ClientPacketDistributor.sendToServer(new FireDragonNetwork.AbilityPayload(dragon.getId(), i));
            }
        }
    }

    @SubscribeEvent
    static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui
                || !(mc.player.getVehicle() instanceof FireDragon dragon) || !dragon.isAdult()) {
            return;
        }
        GuiGraphicsExtractor gui = event.getGuiGraphics();
        int width = gui.guiWidth();
        int height = gui.guiHeight();
        int slotW = 58;
        int slotH = 22;
        int gap = 4;
        int totalW = slotW * 4 + gap * 3;
        int startX = (width - totalW) / 2;
        int y = height - 68;

        for (int i = 0; i < 4; i++) {
            int x = startX + i * (slotW + gap);
            int cd = dragon.getAbilityCooldown(i);
            boolean ready = cd <= 0;
            gui.fill(x, y, x + slotW, y + slotH, ready ? 0xCC2C160E : 0xAA191311);
            int border = ready ? 0xFFE89B2D : 0xFF664438;
            gui.fill(x, y, x + slotW, y + 1, border);
            gui.fill(x, y + slotH - 1, x + slotW, y + slotH, border);
            gui.fill(x, y, x + 1, y + slotH, border);
            gui.fill(x + slotW - 1, y, x + slotW, y + slotH, border);
            if (!ready) {
                float frac = Math.clamp(cd / (float) ABILITY_MAX_CD[i], 0.0F, 1.0F);
                int fillW = Math.round((slotW - 2) * frac);
                gui.fill(x + 1, y + slotH - 4, x + 1 + fillW, y + slotH - 1, 0xFFD95B26);
            }
            String keyText = "[" + ABILITY_KEYS[i].getTranslatedKeyMessage().getString() + "]";
            Component label = Component.literal(keyText + " ").append(Component.translatable(ABILITY_NAMES[i]));
            gui.centeredText(mc.font, label, x + slotW / 2, y + 6, ready ? 0xFFFCE2A6 : 0xFF9C8478);
        }
    }
}
