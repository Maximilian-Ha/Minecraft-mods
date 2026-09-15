package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineCyclotronBlockEntity;
import com.hbm.inventory.menus.MachineCyclotronMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineCyclotron.
 * Blit- und Trefferkoordinaten unveraendert uebernommen.
 *
 * Die drei Tanks liegen waagerecht: Wasser und Abdampf schmal untereinander links, die
 * Antimaterie breiter rechts. Sie ist der Ertrag und soll man auch sehen.
 */
public class MachineCyclotronScreen extends InfoScreen<MachineCyclotronMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_cyclotron.png");

    private final MachineCyclotronBlockEntity be;

    public MachineCyclotronScreen(MachineCyclotronMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 190;
        this.imageHeight = 215;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 168, this.topPos + 18, 16, 63,
                this.be.power, MachineCyclotronBlockEntity.maxPower);

        this.be.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 11, this.topPos + 81, 34, 7);
        this.be.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 11, this.topPos + 90, 34, 7);
        this.be.tanks[2].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 107, this.topPos + 81, 34, 16);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 49, this.topPos + 85, 8, 8, mouseX, mouseY,
                Component.translatable("desc.gui.upgrade"),
                Component.translatable("desc.gui.upgrade.speed"),
                Component.translatable("desc.gui.upgrade.effectiveness"),
                Component.translatable("desc.gui.upgrade.power"));

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 79 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 15, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int power = (int) (this.be.power * 63 / MachineCyclotronBlockEntity.maxPower);
        if(power > 0) guiGraphics.blit(TEXTURE, this.leftPos + 168, this.topPos + 80 - power, 190, 62 - power, 16, power, 256, 256);

        int progress = this.be.progress * 34 / MachineCyclotronBlockEntity.duration;
        if(progress > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 48, this.topPos + 27, 206, 0, progress, 34, 256, 256);
            guiGraphics.blit(TEXTURE, this.leftPos + 172, this.topPos + 4, 190, 63, 9, 12, 256, 256);
        }

        this.drawInfoPanel(guiGraphics, this.leftPos + 49, this.topPos + 85, 8);

        this.be.tanks[0].renderTank(this.leftPos + 11, this.topPos + 88, 0, 34, 7, 1);
        this.be.tanks[1].renderTank(this.leftPos + 11, this.topPos + 97, 0, 34, 7, 1);
        this.be.tanks[2].renderTank(this.leftPos + 107, this.topPos + 97, 0, 34, 16, 1);
    }
}
