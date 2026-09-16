package com.zuxelus.energycontrol.client.screens;

import com.zuxelus.energycontrol.ECConfig;
import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.blockentity.RangeTriggerBlockEntity;
import com.zuxelus.energycontrol.menus.RangeTriggerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.gui.GuiRangeTrigger.
 *
 * Untere und obere Grenze werden eingetippt, wie im Original. Die erste Fassung dieses Ports
 * hatte dafuer ein Raster aus Schrittknoepfen, weil es noch kein Steuerpaket gab; seit Stufe 4
 * gibt es eines.
 */
public class RangeTriggerScreen extends AbstractContainerScreen<RangeTriggerMenu> {

    private static final ResourceLocation TEXTURE = EnergyControl.loc("textures/gui/gui_range_trigger.png");

    // Stehend unter den beiden Faechern: in der Breite brauchen die Eingabefelder den Platz.
    private static final int BAR_X = 8;
    private static final int BAR_Y = 58;
    private static final int BAR_WIDTH = 16;
    private static final int BAR_HEIGHT = 36;

    private EditBox lower;
    private EditBox upper;

    public RangeTriggerScreen(RangeTriggerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 190;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        lower = numberField(topPos + 34, menu.be.getLevelStart());
        upper = numberField(topPos + 66, menu.be.getLevelEnd());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> apply())
                .bounds(leftPos + 126, topPos + 34, 42, 16).build());

        addRenderableWidget(Button.builder(Component.translatable("msg.ec.InvertRedstoneShort"),
                        button -> send(RangeTriggerMenu.BUTTON_INVERT))
                .bounds(leftPos + 126, topPos + 66, 42, 16).build());
    }

    private EditBox numberField(int y, long value) {
        EditBox box = new EditBox(font, leftPos + 30, y, 90, 16, Component.empty());
        box.setMaxLength(18);
        box.setFilter(text -> text.matches("\\d*"));
        box.setValue(Long.toString(value));
        addRenderableWidget(box);
        return box;
    }

    private void apply() {
        long start = parse(lower, menu.be.getLevelStart());
        long end = parse(upper, menu.be.getLevelEnd());

        ControlSender.sendLong(menu.be.getBlockPos(), "levelStart", start);
        ControlSender.sendLong(menu.be.getBlockPos(), "levelEnd", end);
    }

    /** Ein leeres oder zu langes Feld laesst die Grenze, wie sie war. */
    private static long parse(EditBox box, long fallback) {
        String value = box.getValue();
        if(value.isEmpty()) return fallback;
        try {
            return Long.parseLong(value);
        } catch(NumberFormatException e) {
            return fallback;
        }
    }

    private void send(int id) {
        if(minecraft == null || minecraft.gameMode == null) return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if(ECConfig.requirePower()) {
            EnergyBar.tooltip(guiGraphics, font, mouseX, mouseY, leftPos + BAR_X, topPos + BAR_Y,
                    BAR_WIDTH, BAR_HEIGHT, menu.getSyncedEnergy(), menu.be.getMaxEnergyStored());
        }

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if(ECConfig.requirePower()) {
            EnergyBar.renderVertical(guiGraphics, leftPos + BAR_X, topPos + BAR_Y,
                    BAR_WIDTH, BAR_HEIGHT, menu.getSyncedEnergy(), menu.be.getMaxEnergyStored());
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        RangeTriggerBlockEntity trigger = menu.be;

        guiGraphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
        guiGraphics.drawString(font, Component.translatable("msg.ec.RangeTriggerStartShort"), 30, 24, 0x404040, false);
        guiGraphics.drawString(font, Component.translatable("msg.ec.RangeTriggerEndShort"), 30, 56, 0x404040, false);

        Component inverted = Component.translatable(trigger.isInverted() ? "options.on" : "options.off");
        guiGraphics.drawString(font, inverted, 126, 86, 0x404040, false);

        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
