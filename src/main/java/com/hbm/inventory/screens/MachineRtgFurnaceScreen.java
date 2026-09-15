package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineRtgFurnaceBlockEntity;
import com.hbm.inventory.menus.MachineRtgFurnaceMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIRtgFurnace.
 * Blit-Koordinaten unveraendert uebernommen.
 */
public class MachineRtgFurnaceScreen extends InfoScreen<MachineRtgFurnaceMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/gui_rtg_furnace.png");

    private final MachineRtgFurnaceBlockEntity be;

    public MachineRtgFurnaceScreen(MachineRtgFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
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
        Component name = this.title;
        guiGraphics.drawString(this.font, name, this.imageWidth / 2 - this.font.width(name) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        /* Die Glut brennt, sobald ueberhaupt ein Pellet einliegt -- unabhaengig von der Arbeit. */
        if(this.be.hasPower()) {
            guiGraphics.blit(TEXTURE, this.leftPos + 55, this.topPos + 35, 176, 0, 18, 16);
        }

        int progress = this.be.getProgressScaled(24);
        guiGraphics.blit(TEXTURE, this.leftPos + 79, this.topPos + 34, 176, 16, progress + 1, 17);
    }
}
