package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.rbmk.RBMKOutgasserBlockEntity;
import com.hbm.inventory.menus.RBMKOutgasserMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIRBMKOutgasser.
 * Alle Blit-Koordinaten sind unveraendert uebernommen.
 */
public class RBMKOutgasserScreen extends InfoScreen<RBMKOutgasserMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_rbmk_outgasser.png");

    private final RBMKOutgasserBlockEntity outgasser;

    public RBMKOutgasserScreen(RBMKOutgasserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.outgasser = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 186;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.outgasser.gas.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 112, this.topPos + 21, 16, 48);
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

        int progress = (int) (this.outgasser.progress * 13 / RBMKOutgasserBlockEntity.DURATION);
        if(progress > 0) guiGraphics.blit(TEXTURE, this.leftPos + 82, this.topPos + 50, 176, 0, progress, 6, 256, 256);

        int gas = (int) ((long) this.outgasser.gas.getFill() * 42 / this.outgasser.gas.getMaxFill());
        if(gas > 0) guiGraphics.blit(TEXTURE, this.leftPos + 115, this.topPos + 66 - gas, 188, 42 - gas, 10, gas, 256, 256);
    }
}
