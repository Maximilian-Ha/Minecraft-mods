package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineTurbineBlockEntity;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.menus.MachineTurbineMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineTurbine.
 * Blit-Koordinaten unveraendert uebernommen.
 */
public class MachineTurbineScreen extends InfoScreen<MachineTurbineMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/gui_turbine.png");

    private final MachineTurbineBlockEntity be;

    public MachineTurbineScreen(MachineTurbineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 168;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.be.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 62, this.topPos + 17, 16, 52);
        this.be.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 134, this.topPos + 17, 16, 52);

        if(this.be.tanks[1].getTankType() == Fluids.NONE) {
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos - 16, this.topPos + 68, 16, 16,
                    this.leftPos - 8, this.topPos + 84, Component.literal("Error: Invalid fluid!"));
        }

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 123, this.topPos + 35, 7, 34, this.be.getPower(), this.be.getMaxPower());

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        if(this.be.tanks[0].getTankType() == Fluids.STEAM) guiGraphics.blit(TEXTURE, this.leftPos + 99, this.topPos + 18, 183, 0, 14, 14, 256, 256);
        if(this.be.tanks[0].getTankType() == Fluids.HOTSTEAM) guiGraphics.blit(TEXTURE, this.leftPos + 99, this.topPos + 18, 183, 14, 14, 14, 256, 256);
        if(this.be.tanks[0].getTankType() == Fluids.SUPERHOTSTEAM) guiGraphics.blit(TEXTURE, this.leftPos + 99, this.topPos + 18, 183, 28, 14, 14, 256, 256);
        if(this.be.tanks[0].getTankType() == Fluids.ULTRAHOTSTEAM) guiGraphics.blit(TEXTURE, this.leftPos + 99, this.topPos + 18, 183, 42, 14, 14, 256, 256);

        int power = (int) this.be.getPowerScaled(34);
        if(power > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 123, this.topPos + 69 - power, 176, 34 - power, 7, power, 256, 256);
        }

        if(this.be.tanks[1].getTankType() == Fluids.NONE) {
            this.drawInfoPanel(guiGraphics, this.leftPos - 16, this.topPos + 68, 6);
        }

        this.be.tanks[0].renderTank(this.leftPos + 62, this.topPos + 69, 0, 16, 52);
        this.be.tanks[1].renderTank(this.leftPos + 134, this.topPos + 69, 0, 16, 52);
    }
}
