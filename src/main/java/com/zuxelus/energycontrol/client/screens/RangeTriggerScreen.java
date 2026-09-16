package com.zuxelus.energycontrol.client.screens;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.blockentity.RangeTriggerBlockEntity;
import com.zuxelus.energycontrol.menus.RangeTriggerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.gui.GuiRangeTrigger.
 *
 * Fuer jede Schrittweite ein Knopfpaar, je einmal fuer die untere und die obere Grenze.
 */
public class RangeTriggerScreen extends AbstractContainerScreen<RangeTriggerMenu> {

    private static final ResourceLocation TEXTURE = EnergyControl.loc("textures/gui/gui_range_trigger.png");

    public RangeTriggerScreen(RangeTriggerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 190;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        int steps = RangeTriggerBlockEntity.STEPS.length;
        int width = 138 / steps;

        for(int i = 0; i < steps; i++) {
            int index = i;
            int x = leftPos + 30 + i * width;
            addRenderableWidget(Button.builder(Component.literal("+" + shortStep(index)),
                            button -> send(RangeTriggerMenu.buttonId(false, index, false)))
                    .bounds(x, topPos + 24, width - 1, 13).build());
            addRenderableWidget(Button.builder(Component.literal("-" + shortStep(index)),
                            button -> send(RangeTriggerMenu.buttonId(false, index, true)))
                    .bounds(x, topPos + 38, width - 1, 13).build());
            addRenderableWidget(Button.builder(Component.literal("+" + shortStep(index)),
                            button -> send(RangeTriggerMenu.buttonId(true, index, false)))
                    .bounds(x, topPos + 62, width - 1, 13).build());
            addRenderableWidget(Button.builder(Component.literal("-" + shortStep(index)),
                            button -> send(RangeTriggerMenu.buttonId(true, index, true)))
                    .bounds(x, topPos + 76, width - 1, 13).build());
        }

        // Rechts unten, damit der Knopf die Beschriftung des Spielerinventars links
        // daneben nicht ueberdeckt.
        addRenderableWidget(Button.builder(Component.translatable("msg.ec.InvertRedstone"),
                        button -> send(RangeTriggerMenu.BUTTON_INVERT))
                .bounds(leftPos + 100, topPos + 90, 68, 14).build());
    }

    /** "1k" statt "1000", damit die Beschriftung in den Knopf passt. */
    private static String shortStep(int index) {
        long value = RangeTriggerBlockEntity.STEPS[index];
        if(value >= 1_000_000L) return (value / 1_000_000L) + "M";
        if(value >= 1_000L) return (value / 1_000L) + "k";
        return Long.toString(value);
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
        RangeTriggerBlockEntity trigger = menu.be;

        guiGraphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);

        guiGraphics.drawString(font, Component.translatable("msg.ec.RangeTriggerStart",
                PanelString.format(trigger.getLevelStart())), 30, 16, 0x404040, false);
        guiGraphics.drawString(font, Component.translatable("msg.ec.RangeTriggerEnd",
                PanelString.format(trigger.getLevelEnd())), 30, 54, 0x404040, false);

        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
