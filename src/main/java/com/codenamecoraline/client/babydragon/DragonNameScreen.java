package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.entity.babydragon.BabyDragon;
import com.codenamecoraline.entity.babydragon.DragonNamingPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Post-taming screen allowing the owner to name their newly tamed dragon or skip to keep the default name. */
public final class DragonNameScreen extends Screen {
    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_HEIGHT = 112;

    private final int entityId;
    private EditBox nameInput;
    private boolean submitted;

    public DragonNameScreen(int entityId) {
        super(Component.translatable("gui.codenamecoraline.dragon.name.title"));
        this.entityId = entityId;
    }

    public static void open(int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.getEntity(entityId) instanceof BabyDragon dragon && !dragon.hasCustomName()) {
            mc.setScreen(new DragonNameScreen(entityId));
        }
    }

    @Override
    protected void init() {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        this.nameInput = new EditBox(this.font, left + 16, top + 44, PANEL_WIDTH - 32, 20,
                Component.translatable("gui.codenamecoraline.dragon.name.hint"));
        this.nameInput.setMaxLength(BabyDragon.MAX_CUSTOM_NAME_LENGTH);
        this.nameInput.setHint(Component.translatable("gui.codenamecoraline.dragon.name.hint"));
        this.addRenderableWidget(this.nameInput);
        this.setInitialFocus(this.nameInput);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.codenamecoraline.dragon.name.confirm"), b -> this.confirm())
                .pos(left + 16, top + 76).size(88, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.codenamecoraline.dragon.name.skip"), b -> this.skip())
                .pos(left + PANEL_WIDTH - 104, top + 76).size(88, 20).build());
    }

    private void confirm() {
        if (this.submitted) return;
        this.submitted = true;
        String value = this.nameInput != null ? this.nameInput.getValue().trim() : "";
        ClientPacketDistributor.sendToServer(new DragonNamingPayloads.Submit(this.entityId, value));
        this.onClose();
    }

    private void skip() {
        if (this.submitted) return;
        this.submitted = true;
        ClientPacketDistributor.sendToServer(new DragonNamingPayloads.Submit(this.entityId, ""));
        this.onClose();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 257 || event.key() == 335) {
            this.confirm();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        graphics.fill(left - 2, top - 2, left + PANEL_WIDTH + 2, top + PANEL_HEIGHT + 2, 0xFF3A2A1D);
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xEE181412);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, top + 12, 0xFFFFD37F);
        graphics.centeredText(this.font, Component.translatable("gui.codenamecoraline.dragon.name.subtitle"), this.width / 2, top + 26, 0xFFD0C4B4);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

