package com.zuxelus.energycontrol.client.screens;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.blockentity.EnergyCounterBlockEntity;
import com.zuxelus.energycontrol.menus.EnergyCounterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.gui.GuiEnergyCounter.
 *
 * Zeigt Zaehlerstand und Durchsatz und laesst den Zaehler zuruecksetzen.
 */
public class EnergyCounterScreen extends AbstractContainerScreen<EnergyCounterMenu> {

    private static final ResourceLocation TEXTURE = EnergyControl.loc("textures/gui/gui_energy_counter.png");

    public EnergyCounterScreen(EnergyCounterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(Button.builder(Component.translatable("msg.ec.CounterReset"), button -> reset())
                .bounds(leftPos + 8, topPos + 54, 160, 16).build());
    }

    private void reset() {
        CompoundTag tag = new CompoundTag();
        tag.putString("action", "reset");
        ControlSender.send(menu.be.getBlockPos(), tag);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        EnergyCounterBlockEntity counter = menu.be;
        EnergyBar.tooltip(guiGraphics, font, mouseX, mouseY, leftPos + 8, topPos + 42, 160,
                counter.getEnergyStored(), counter.getMaxEnergyStored());

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        EnergyCounterBlockEntity counter = menu.be;
        EnergyBar.render(guiGraphics, leftPos + 8, topPos + 42, 160,
                counter.getEnergyStored(), counter.getMaxEnergyStored());
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        EnergyCounterBlockEntity counter = menu.be;

        guiGraphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);

        guiGraphics.drawString(font, Component.translatable("msg.ec.CounterTotal",
                PanelString.format(counter.getCounter())), 30, 20, 0x404040, false);
        guiGraphics.drawString(font, Component.translatable("msg.ec.CounterRate",
                PanelString.format(counter.getAverage())), 30, 32, 0x404040, false);

        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
