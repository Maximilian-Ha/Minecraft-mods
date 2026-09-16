package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.oil.MachineSolidifierBlockEntity;
import com.hbm.inventory.menus.MachineSolidifierMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUISolidifier.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 */
public class MachineSolidifierScreen extends InfoScreen<MachineSolidifierMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_solidifier.png");

    private final MachineSolidifierBlockEntity be;

    public MachineSolidifierScreen(MachineSolidifierMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 204;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.be.tank.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 35, this.topPos + 36, 16, 52);
        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 134, this.topPos + 18, 16, 52,
                this.be.power, MachineSolidifierBlockEntity.MAX_POWER);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 5, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int p = (int) (this.be.power * 52 / Math.max(MachineSolidifierBlockEntity.MAX_POWER, 1L));
        guiGraphics.blit(TEXTURE, this.leftPos + 134, this.topPos + 70 - p, 176, 52 - p, 16, p, 256, 256);

        /* Die Lampe ueber der Batterieanzeige brennt, sobald ueberhaupt Strom da ist. */
        if(p > 0) guiGraphics.blit(TEXTURE, this.leftPos + 138, this.topPos + 4, 176, 52, 9, 12, 256, 256);

        /* Der Fortschrittsbalken laeuft von links nach rechts ueber den Trichter. */
        if(this.be.processTime > 0) {
            int j = this.be.progress * 42 / this.be.processTime;
            guiGraphics.blit(TEXTURE, this.leftPos + 42, this.topPos + 17, 192, 0, j, 35, 256, 256);
        }

        this.be.tank.renderTank(this.leftPos + 35, this.topPos + 88, 0, 16, 52);
    }
}
