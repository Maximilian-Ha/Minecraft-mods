package com.hbm.inventory.screens;

import com.hbm.blockentity.network.RadioRecBlockEntity;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIRadioRec.
 *
 * Eine Oberflaeche ohne Behaelter: ein Kanalfeld, ein Knopf zum Sichern und einer zum
 * Ein- und Ausschalten. Masse und Knopffelder unveraendert aus dem Original.
 */
public class RadioRecScreen extends Screen {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_radio.png");

    private static final int MAX_CHAN_LENGTH = 10;

    private final RadioRecBlockEntity radio;

    private final int imageWidth = 220;
    private final int imageHeight = 42;
    private int leftPos;
    private int topPos;

    private EditBox frequency;

    public RadioRecScreen(RadioRecBlockEntity radio) {
        super(Component.translatable("container.radiorec"));
        this.radio = radio;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        // Die Versaetze 4/4 des Originals sind hier schon eingerechnet.
        this.frequency = new EditBox(this.font, this.leftPos + 29, this.topPos + 21, 82, 14, Component.empty());
        this.frequency.setTextColor(0x00ff00);
        this.frequency.setTextColorUneditable(0x00ff00);
        this.frequency.setBordered(false);
        this.frequency.setMaxLength(MAX_CHAN_LENGTH);
        this.frequency.setValue(this.radio.channel == null ? "" : this.radio.channel);
        this.addRenderableWidget(this.frequency);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(guiGraphics);
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.renderLabels(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if(this.radio.isOn) {
            guiGraphics.blit(TEXTURE, this.leftPos + 173, this.topPos + 17, 0, 42, 18, 18);
        }

        this.frequency.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title,
                this.leftPos + this.imageWidth / 2 - this.font.width(this.title) / 2, this.topPos + 6, 4210752, false);

        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 137, 17, 18, 18)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("container.radiorec.save"), mouseX, mouseY);
        }
        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 173, 17, 18, 18)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("container.radiorec.toggle"), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        this.frequency.mouseClicked(mouseX, mouseY, button);

        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 137, 17, 18, 18)) {
            ScreenUtils.click(this.minecraft);
            CompoundTag tag = new CompoundTag();
            tag.putString("channel", this.frequency.getValue());
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.radio.getBlockPos()));
        }

        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 173, 17, 18, 18)) {
            ScreenUtils.click(this.minecraft);
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("isOn", !this.radio.isOn);
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.radio.getBlockPos()));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if(this.frequency.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if(this.frequency.keyPressed(keyCode, scanCode, modifiers)) return true;

        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        if(keyCode == GLFW.GLFW_KEY_ESCAPE || this.minecraft.options.keyInventory.isActiveAndMatches(key)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
