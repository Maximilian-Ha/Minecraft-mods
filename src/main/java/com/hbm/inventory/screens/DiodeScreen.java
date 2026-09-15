package com.hbm.inventory.screens;

import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import com.hbm.blockentity.network.DiodeBlockEntity;
import com.hbm.network.toserver.CompoundTagControl;
import com.hbm.util.EnumUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIDiode.
 *
 * Containerlose Oberflaeche ohne eigene Textur, genau wie im Original. Die Werte werden
 * erst beim Schliessen als ein CompoundTagControl-Paket an den Server geschickt.
 * Der GuiButton des Originals ist hier eine selbst gezeichnete Schaltflaeche: eine
 * Button-Klasse wird im Port sonst nirgends benutzt, und der Klickbereich liegt exakt
 * auf den Originalkoordinaten.
 */
public class DiodeScreen extends Screen {

    protected final DiodeBlockEntity diode;

    private EditBox textThroughput;
    private int priority;

    private int buttonX;
    private int buttonY;
    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_HEIGHT = 20;

    public DiodeScreen(DiodeBlockEntity diode) {
        super(Component.translatable("container.cable_diode"));
        this.diode = diode;
        this.priority = diode.priority.ordinal();
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {

        this.textThroughput = new EditBox(this.font, this.width / 2 - 150, 100, BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty());
        this.textThroughput.setMaxLength(11);
        this.textThroughput.setValue("" + this.diode.limit);
        this.addRenderableWidget(this.textThroughput);

        this.buttonX = this.width / 2 + 20;
        this.buttonY = 100;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(guiGraphics);

        guiGraphics.drawString(this.font, "Throughput:", this.width / 2 - 150, 80, 0xA0A0A0, false);
        guiGraphics.drawString(this.font, "(max. 10,000,000,000 HE)", this.width / 2 - 150, 90, 0xA0A0A0, false);
        guiGraphics.drawString(this.font, "Priority:", this.width / 2 + 20, 80, 0xA0A0A0, false);

        boolean hovered = ScreenUtils.isHovered(0, 0, mouseX, mouseY, this.buttonX, this.buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        guiGraphics.fill(this.buttonX, this.buttonY, this.buttonX + BUTTON_WIDTH, this.buttonY + BUTTON_HEIGHT, 0xFF000000);
        guiGraphics.fill(this.buttonX + 1, this.buttonY + 1, this.buttonX + BUTTON_WIDTH - 1, this.buttonY + BUTTON_HEIGHT - 1, hovered ? 0xFF8B8B8B : 0xFF6B6B6B);
        guiGraphics.drawCenteredString(this.font, Component.literal(this.getPriorityName()), this.buttonX + BUTTON_WIDTH / 2, this.buttonY + 6, 0xFFFFFF);

        for(Renderable renderable : this.renderables) renderable.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private String getPriorityName() {
        ConnectionPriority prio = EnumUtil.grabEnumSafely(ConnectionPriority.class, this.priority);
        return prio.name();
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        this.textThroughput.mouseClicked(mouseX, mouseY, button);

        if(ScreenUtils.isHovered(0, 0, mouseX, mouseY, this.buttonX, this.buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            this.priority++;
            if(this.priority >= ConnectionPriority.values().length) this.priority = 0;
            ScreenUtils.click(this.minecraft);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if(this.textThroughput.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if(this.textThroughput.keyPressed(keyCode, scanCode, modifiers)) return true;

        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        if(keyCode == GLFW.GLFW_KEY_ESCAPE || this.minecraft.options.keyInventory.isActiveAndMatches(key)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {

        CompoundTag tag = new CompoundTag();
        tag.putByte("priority", (byte) this.priority);

        try {
            tag.putLong("limit", Long.parseLong(this.textThroughput.getValue()));
        } catch(Exception ex) { }

        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.diode.getBlockPos()));

        super.onClose();
    }
}
