package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.rbmk.RBMKHeaterBlockEntity;
import com.hbm.inventory.menus.RBMKHeaterMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIRBMKHeater.
 * Alle Blit-Koordinaten sind unveraendert uebernommen.
 */
public class RBMKHeaterScreen extends InfoScreen<RBMKHeaterMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_rbmk_heater.png");

    private final RBMKHeaterBlockEntity heater;

    public RBMKHeaterScreen(RBMKHeaterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.heater = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 186;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.heater.feed.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 68, this.topPos + 24, 16, 58);
        this.heater.steam.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 126, this.topPos + 24, 16, 58);

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

        this.heater.feed.renderTank(this.leftPos + 68, this.topPos + 82, 0, 14, 58);
        this.heater.steam.renderTank(this.leftPos + 126, this.topPos + 82, 0, 14, 58);

        guiGraphics.blit(TEXTURE, this.leftPos + 72, this.topPos + 72, 176, 0, 10, 10, 256, 256);
        guiGraphics.blit(TEXTURE, this.leftPos + 130, this.topPos + 72, 186, 0, 10, 10, 256, 256);
    }
}
