package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.FurnaceBrickBlockEntity;
import com.hbm.inventory.menus.FurnaceBrickMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIFurnaceBrick.
 * Die Blit-Koordinaten sind unveraendert uebernommen.
 */
public class FurnaceBrickScreen extends AbstractContainerScreen<FurnaceBrickMenu> {

    private static final ResourceLocation TEXTURE =
            NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_furnace_brick.png");

    private final FurnaceBrickBlockEntity be;

    public FurnaceBrickScreen(FurnaceBrickMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Kein eigener renderBackground-Aufruf: super.render zeichnet den Hintergrund schon.
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if(this.be.burnTime <= 0) return;

        // Die Glut brennt von oben herunter, darum der wandernde Anfang in der Textur.
        int glut = this.be.getBurnTimeScaled(13);
        guiGraphics.blit(TEXTURE, this.leftPos + 62, this.topPos + 54 + 12 - glut, 176, 12 - glut, 14, glut + 1);

        int fortschritt = this.be.getProgressScaled(24);
        guiGraphics.blit(TEXTURE, this.leftPos + 85, this.topPos + 34, 176, 14, fortschritt + 1, 16);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title,
                this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }
}
