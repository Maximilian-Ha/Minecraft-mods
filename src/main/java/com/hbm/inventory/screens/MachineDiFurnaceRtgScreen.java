package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineDiFurnaceRtgBlockEntity;
import com.hbm.inventory.menus.MachineDiFurnaceRtgMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineDiFurnaceRTG.
 * Blit-Koordinaten unveraendert uebernommen.
 */
public class MachineDiFurnaceRtgScreen extends InfoScreen<MachineDiFurnaceRtgMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_rtg_difurnace.png");

    private final MachineDiFurnaceRtgBlockEntity be;

    public MachineDiFurnaceRtgScreen(MachineDiFurnaceRtgMenu menu, Inventory inventory, Component title) {
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

        /* Die Glut brennt erst ab fuenfzehn Waermepunkten -- darunter reicht es nicht zum Schmelzen. */
        if(this.be.hasPower()) {
            guiGraphics.blit(TEXTURE, this.leftPos + 58, this.topPos + 36, 176, 31, 18, 16);
        }

        int progress = this.be.getProgressScaled(24);
        guiGraphics.blit(TEXTURE, this.leftPos + 101, this.topPos + 35, 176, 14, progress + 1, 17);
    }
}
