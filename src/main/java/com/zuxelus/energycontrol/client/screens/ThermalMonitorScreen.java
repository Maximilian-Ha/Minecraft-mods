package com.zuxelus.energycontrol.client.screens;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.blockentity.RemoteThermalMonitorBlockEntity;
import com.zuxelus.energycontrol.blockentity.ThermalMonitorBlockEntity;
import com.zuxelus.energycontrol.menus.ThermalMonitorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.gui.GuiThermalMonitor.
 *
 * Zeigt die gemessene Temperatur und laesst die Schwelle eintippen. Die erste Fassung dieses
 * Ports schaltete mit zwei Knoepfen durch feste Stufen, weil es noch kein Steuerpaket gab;
 * seit Stufe 4 gibt es eines, und damit ist die freie Eingabe des Originals wieder da.
 *
 * Dieselbe Oberflaeche dient der Fernwaermeanzeige: sie hat zwei Faecher mehr, ihr eigenes
 * Hintergrundbild und sonst genau denselben Inhalt.
 */
public class ThermalMonitorScreen extends AbstractContainerScreen<ThermalMonitorMenu> {

    private static final ResourceLocation TEXTURE = EnergyControl.loc("textures/gui/gui_thermal_monitor.png");
    private static final ResourceLocation TEXTURE_REMOTE = EnergyControl.loc("textures/gui/gui_remote_thermo.png");

    private EditBox heatLevel;

    public ThermalMonitorScreen(ThermalMonitorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        heatLevel = new EditBox(font, leftPos + 80, topPos + 34, 50, 16, Component.empty());
        heatLevel.setMaxLength(9);
        heatLevel.setFilter(value -> value.matches("\\d*"));
        heatLevel.setValue(Integer.toString(menu.be.getHeatLevel()));
        addRenderableWidget(heatLevel);

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> apply())
                .bounds(leftPos + 134, topPos + 34, 34, 16).build());

        // Bei der Fernwaermeanzeige liegen links zwei Faecher; der Schalter rueckt daneben.
        boolean remote = isRemote();
        addRenderableWidget(Button.builder(Component.translatable("msg.ec.InvertRedstone"),
                        button -> send(ThermalMonitorMenu.BUTTON_INVERT))
                .bounds(leftPos + (remote ? 52 : 8), topPos + (remote ? 53 : 54), remote ? 116 : 160, 16).build());
    }

    private void apply() {
        String value = heatLevel.getValue();
        if(value.isEmpty()) return;
        try {
            ControlSender.sendInt(menu.be.getBlockPos(), "heatLevel", Integer.parseInt(value));
        } catch(NumberFormatException ignored) {
            // Das Feld laesst nur Ziffern zu; laenger als neun Stellen kann es nicht werden.
        }
    }

    private boolean isRemote() {
        return menu.be instanceof RemoteThermalMonitorBlockEntity;
    }

    private void send(int id) {
        if(minecraft == null || minecraft.gameMode == null) return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Den Hintergrund zeichnet AbstractContainerScreen selbst -- ein zweiter Aufruf
        // legte die Abdunklung doppelt uebereinander.
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(isRemote() ? TEXTURE_REMOTE : TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ThermalMonitorBlockEntity monitor = menu.be;

        guiGraphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);

        Component heat = monitor.getHeat() < 0
                ? Component.translatable("msg.ec.InfoPanelNoTarget")
                : Component.translatable("msg.ec.Thermo", monitor.getHeat());
        guiGraphics.drawString(font, heat, 8, 20, 0x404040, false);

        guiGraphics.drawString(font, Component.translatable("msg.ec.ThermalMonitorSignalAtShort"), 8, 38, 0x404040, false);

        Component inverted = Component.translatable(monitor.isInverted() ? "options.on" : "options.off");
        guiGraphics.drawString(font, inverted, imageWidth - 8 - font.width(inverted), 20, 0x404040, false);

        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
