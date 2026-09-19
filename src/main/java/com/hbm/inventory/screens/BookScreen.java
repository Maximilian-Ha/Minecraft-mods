package com.hbm.inventory.screens;

import com.hbm.inventory.menus.BookMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIBook.
 * Alle Blit-Koordinaten sind unveraendert uebernommen.
 *
 * Die beiden Beschriftungen stehen im Original in der Schrift der Verzauberungstafel
 * (standardGalacticFontRenderer) -- lesen soll man sie nicht.
 */
public class BookScreen extends InfoScreen<BookMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_book.png");

    /** Die Schrift der Verzauberungstafel; im Original der standardGalacticFontRenderer. */
    private static final Style GALAKTISCH = Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt"));

    public BookScreen(BookMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, Component.literal("Extended 4-Slot Crafting").withStyle(GALAKTISCH), 28, 6, 4210752, false);
        guiGraphics.drawString(this.font, Component.literal("Standard Inventory").withStyle(GALAKTISCH), 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        /* Liegt ein Ergebnis bereit, legt das Original einen Rahmen um die vier Plaetze. */
        if(this.menu.slots.get(0).hasItem()) {
            guiGraphics.blit(TEXTURE, this.leftPos + 29, this.topPos + 16, 176, 0, 54, 54);
        }
    }
}
