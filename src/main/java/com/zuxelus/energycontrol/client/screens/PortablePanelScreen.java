package com.zuxelus.energycontrol.client.screens;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.IItemCard;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.items.ItemPortablePanel;
import com.zuxelus.energycontrol.items.cards.ItemCardBase;
import com.zuxelus.energycontrol.items.cards.ItemCardReader;
import com.zuxelus.energycontrol.menus.PortablePanelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.gui.GuiPortablePanel.
 *
 * Zeigt dieselben Zeilen wie eine Tafel an der Wand, nur in der Hand. Die Farbe ist die
 * Grundfarbe der Tafel; eine Farbwahl gibt es hier nicht.
 */
public class PortablePanelScreen extends AbstractContainerScreen<PortablePanelMenu> {

    private static final ResourceLocation TEXTURE = EnergyControl.loc("textures/gui/gui_portable_panel.png");

    private static final int TEXT_LEFT = 12;
    private static final int TEXT_TOP = 12;
    private static final int TEXT_RIGHT = 160;
    private static final int LINE_HEIGHT = 10;
    private static final int TEXT_COLOR = 0x00CC00;

    public PortablePanelScreen(PortablePanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 197;
        this.imageHeight = 188;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<PanelString> lines = getLines();
        if(lines == null) return;

        int maxLines = (152 - TEXT_TOP) / LINE_HEIGHT;
        int count = Math.min(lines.size(), maxLines);

        for(int i = 0; i < count; i++) {
            PanelString line = lines.get(i);
            int y = TEXT_TOP + i * LINE_HEIGHT;
            draw(guiGraphics, line.textLeft, colorOr(line.colorLeft), TEXT_LEFT, y, false);
            draw(guiGraphics, line.textCenter, colorOr(line.colorCenter), (TEXT_LEFT + TEXT_RIGHT) / 2, y, true);
            drawRight(guiGraphics, line.textRight, colorOr(line.colorRight), TEXT_RIGHT, y);
        }
    }

    /** Die Zeilen der steckenden Karte -- oder null, wenn keine steckt. */
    private List<PanelString> getLines() {
        ItemStack stack = menu.be.getItem(ItemPortablePanel.SLOT_CARD);
        if(!ItemCardBase.isCard(stack)) return null;

        ItemCardReader reader = new ItemCardReader(stack);
        CardState state = reader.getState();
        if(state != CardState.OK && state != CardState.CUSTOM_ERROR) return ItemCardReader.getStateMessage(state);

        IItemCard card = (IItemCard) stack.getItem();
        return card.getStringData(card.getDefaultSettings(), reader, true);
    }

    private static int colorOr(int lineColor) {
        return lineColor != 0 ? lineColor : TEXT_COLOR;
    }

    private void draw(GuiGraphics guiGraphics, Component text, int color, int x, int y, boolean centered) {
        if(text == null) return;
        int drawX = centered ? x - font.width(text) / 2 : x;
        guiGraphics.drawString(font, text, drawX, y, color, false);
    }

    private void drawRight(GuiGraphics guiGraphics, Component text, int color, int x, int y) {
        if(text == null) return;
        guiGraphics.drawString(font, text, x - font.width(text), y, color, false);
    }
}
