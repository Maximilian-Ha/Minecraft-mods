package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineExposureChamberBlockEntity;
import com.hbm.inventory.menus.MachineExposureChamberMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineExposureChamber.
 * Blit- und Trefferkoordinaten unveraendert uebernommen.
 *
 * Der schmale Balken links zeigt, wieviel von der eingezogenen Kapsel noch uebrig ist -- acht
 * Striche, einer je Durchgang.
 */
public class MachineExposureChamberScreen extends InfoScreen<MachineExposureChamberMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_exposure_chamber.png");

    private final MachineExposureChamberBlockEntity be;

    public MachineExposureChamberScreen(MachineExposureChamberMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 186;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 152, this.topPos + 18, 16, 34,
                this.be.power, MachineExposureChamberBlockEntity.maxPower);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 26, this.topPos + 36, 9, 16, mouseX, mouseY,
                Component.literal(this.be.savedParticles + " / " + MachineExposureChamberBlockEntity.maxParticles));

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 70 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int progress = this.be.progress * 42 / (this.be.processTime + 1);
        if(progress > 0) guiGraphics.blit(TEXTURE, this.leftPos + 36, this.topPos + 39, 192, 0, progress, 10);

        int charge = this.be.savedParticles * 16 / MachineExposureChamberBlockEntity.maxParticles;
        if(charge > 0) guiGraphics.blit(TEXTURE, this.leftPos + 26, this.topPos + 52 - charge, 192, 26 - charge, 9, charge);

        int power = (int) (this.be.power * 34 / MachineExposureChamberBlockEntity.maxPower);
        if(power > 0) guiGraphics.blit(TEXTURE, this.leftPos + 152, this.topPos + 52 - power, 176, 34 - power, 16, power);

        if(this.be.consumption <= this.be.power) guiGraphics.blit(TEXTURE, this.leftPos + 156, this.topPos + 4, 176, 34, 9, 12);
    }
}
