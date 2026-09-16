package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.oil.MachineHydrotreaterBlockEntity;
import com.hbm.inventory.menus.MachineHydrotreaterMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineHydrotreater.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 */
public class MachineHydrotreaterScreen extends InfoScreen<MachineHydrotreaterMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_hydrotreater.png");

    private final MachineHydrotreaterBlockEntity be;

    public MachineHydrotreaterScreen(MachineHydrotreaterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 238;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.be.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 35, this.topPos + 18, 16, 52);
        this.be.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 53, this.topPos + 18, 16, 52);
        this.be.tanks[2].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 125, this.topPos + 18, 16, 52);
        this.be.tanks[3].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 143, this.topPos + 18, 16, 52);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 17, this.topPos + 18, 16, 52,
                this.be.power, MachineHydrotreaterBlockEntity.MAX_POWER);

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

        int p = (int) (this.be.power * 52 / Math.max(MachineHydrotreaterBlockEntity.MAX_POWER, 1L));
        guiGraphics.blit(TEXTURE, this.leftPos + 17, this.topPos + 70 - p, 176, 52 - p, 16, p, 256, 256);

        this.be.tanks[0].renderTank(this.leftPos + 35, this.topPos + 70, 0, 16, 52);
        this.be.tanks[1].renderTank(this.leftPos + 53, this.topPos + 70, 0, 16, 52);
        this.be.tanks[2].renderTank(this.leftPos + 125, this.topPos + 70, 0, 16, 52);
        this.be.tanks[3].renderTank(this.leftPos + 143, this.topPos + 70, 0, 16, 52);
    }
}
