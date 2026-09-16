package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.oil.MachinePyroOvenBlockEntity;
import com.hbm.inventory.menus.MachinePyroOvenMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIPyroOven.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 */
public class MachinePyroOvenScreen extends InfoScreen<MachinePyroOvenMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_pyrooven.png");

    private final MachinePyroOvenBlockEntity be;

    public MachinePyroOvenScreen(MachinePyroOvenMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 204;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.be.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 18, 16, 52);
        this.be.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 116, this.topPos + 18, 16, 52);
        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 152, this.topPos + 18, 16, 52,
                this.be.getPower(), this.be.getMaxPower());
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 108, this.topPos + 76, 8, 8, mouseX, mouseY, this.getUpgradeInfo());

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int titleX = this.imageWidth / 2 - this.font.width(this.title) / 2 - 18;
        guiGraphics.drawString(this.font, this.title, titleX, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int i = (int) (this.be.power * 52 / Math.max(MachinePyroOvenBlockEntity.MAX_POWER, 1L));
        guiGraphics.blit(TEXTURE, this.leftPos + 152, this.topPos + 70 - i, 176, 64 - i, 16, i, 256, 256);

        int p = (int) (this.be.progress * 27);
        guiGraphics.blit(TEXTURE, this.leftPos + 57, this.topPos + 47, 176, 0, p, 12, 256, 256);

        this.be.tanks[0].renderTank(this.leftPos + 8, this.topPos + 70, 0, 16, 52);
        this.be.tanks[1].renderTank(this.leftPos + 116, this.topPos + 70, 0, 16, 52);

        this.drawInfoPanel(guiGraphics, this.leftPos + 108, this.topPos + 76, 8);
    }

    private List<Component> getUpgradeInfo() {
        return upgradeInfo(this.be, this.be,
                MachinePyroOvenBlockEntity.SLOT_UPGRADE_START, MachinePyroOvenBlockEntity.SLOT_UPGRADE_END);
    }
}
