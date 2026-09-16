package com.zuxelus.energycontrol.client.screens;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.menus.KitAssemblerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.gui.GuiKitAssembler.
 *
 * Der Fortschritt wird in den Pfeil des Hintergrundbildes gezeichnet. Das Original hatte
 * dafuer eine zweite, gefuellte Fassung des Pfeils auf demselben Blatt -- die gibt es dort
 * nicht mehr, also fuellt der Port den Schaft mit einer Flaeche. Dieselbe Loesung wie beim
 * Energiebalken, aus demselben Grund.
 */
public class KitAssemblerScreen extends AbstractContainerScreen<KitAssemblerMenu> {

    private static final ResourceLocation TEXTURE = EnergyControl.loc("textures/gui/gui_kit_assembler.png");

    /** Der Pfeil im Hintergrundbild: Schaft von x 87 bis 100, Spitze bis 108. */
    private static final int ARROW_X = 87;
    private static final int ARROW_Y = 41;
    private static final int ARROW_WIDTH = 22;
    private static final int ARROW_HEIGHT = 3;
    private static final int ARROW_COLOR = 0xFF4CA64C;

    private static final int BAR_X = 150;
    private static final int BAR_Y = 17;
    private static final int BAR_WIDTH = 12;
    private static final int BAR_HEIGHT = 52;

    public KitAssemblerScreen(KitAssemblerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        EnergyBar.tooltip(guiGraphics, font, mouseX, mouseY, leftPos + BAR_X, topPos + BAR_Y,
                BAR_WIDTH, BAR_HEIGHT, menu.getSyncedEnergy(), menu.be.getMaxEnergyStored());

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int max = menu.getMaxProgress();
        if(max > 0) {
            int filled = Math.min(ARROW_WIDTH, ARROW_WIDTH * menu.getProgress() / max);
            if(filled > 0) {
                guiGraphics.fill(leftPos + ARROW_X, topPos + ARROW_Y,
                        leftPos + ARROW_X + filled, topPos + ARROW_Y + ARROW_HEIGHT, ARROW_COLOR);
            }
        }

        EnergyBar.renderVertical(guiGraphics, leftPos + BAR_X, topPos + BAR_Y, BAR_WIDTH, BAR_HEIGHT,
                menu.getSyncedEnergy(), menu.be.getMaxEnergyStored());
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
