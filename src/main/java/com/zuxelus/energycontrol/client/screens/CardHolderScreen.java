package com.zuxelus.energycontrol.client.screens;

import com.zuxelus.energycontrol.menus.CardHolderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.gui.GuiCardHolder.
 *
 * Der Halter benutzt das Hintergrundbild der grossen Truhe von Minecraft. Das Original
 * hatte dafuer kein eigenes Bild, und sechs Reihen Faecher sehen dort genauso aus.
 */
public class CardHolderScreen extends AbstractContainerScreen<CardHolderMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    public CardHolderScreen(CardHolderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Genau wie ContainerScreen von Minecraft: erst die Faecher, dann das
        // Spielerinventar aus der zweiten Haelfte des Blattes.
        int rows = 6 * 18 + 17;
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, rows);
        guiGraphics.blit(TEXTURE, leftPos, topPos + rows, 0, 126, imageWidth, 96);
    }
}
