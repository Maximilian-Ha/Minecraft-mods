package com.zuxelus.energycontrol.client.screens;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.blockentity.ThermalMonitorBlockEntity;
import com.zuxelus.energycontrol.menus.ThermalMonitorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.gui.GuiThermalMonitor.
 *
 * Zeigt die gemessene Temperatur und die Schwelle und laesst beide Schalter bedienen.
 */
public class ThermalMonitorScreen extends AbstractContainerScreen<ThermalMonitorMenu> {

    private static final ResourceLocation TEXTURE = EnergyControl.loc("textures/gui/gui_thermal_monitor.png");

    public ThermalMonitorScreen(ThermalMonitorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(Button.builder(Component.literal("-"),
                        button -> send(ThermalMonitorMenu.BUTTON_HEAT_DOWN))
                .bounds(leftPos + 8, topPos + 36, 20, 16).build());

        addRenderableWidget(Button.builder(Component.literal("+"),
                        button -> send(ThermalMonitorMenu.BUTTON_HEAT_UP))
                .bounds(leftPos + 148, topPos + 36, 20, 16).build());

        addRenderableWidget(Button.builder(Component.translatable("msg.ec.InvertRedstone"),
                        button -> send(ThermalMonitorMenu.BUTTON_INVERT))
                .bounds(leftPos + 8, topPos + 54, 160, 16).build());
    }

    private void send(int id) {
        if(minecraft == null || minecraft.gameMode == null) return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ThermalMonitorBlockEntity monitor = menu.be;

        guiGraphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);

        Component heat = monitor.getHeat() < 0
                ? Component.translatable("msg.ec.InfoPanelNoTarget")
                : Component.translatable("msg.ec.Thermo", monitor.getHeat());
        guiGraphics.drawString(font, heat, 8, 20, 0x404040, false);

        Component level = Component.translatable("msg.ec.ThermalMonitorSignalAt", monitor.getHeatLevel());
        guiGraphics.drawString(font, level, 32, 40, 0x404040, false);

        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
