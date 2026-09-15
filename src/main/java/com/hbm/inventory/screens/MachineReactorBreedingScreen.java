package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineReactorBreedingBlockEntity;
import com.hbm.inventory.menus.MachineReactorBreedingMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineReactorBreeding.
 *
 * Eingang links, Ausgang rechts, dazwischen der Balken. Oben steht der Fluss, den die Anlage
 * gerade abbekommt.
 */
public class MachineReactorBreedingScreen extends InfoScreen<MachineReactorBreedingMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_breeder.png");

    private final MachineReactorBreedingBlockEntity breeder;

    public MachineReactorBreedingScreen(MachineReactorBreedingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.breeder = menu.be;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        String flux = String.valueOf(this.breeder.flux);
        guiGraphics.drawString(this.font, flux, 88 - this.font.width(flux) / 2, 21, 0x08FF00, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int i = this.breeder.getProgressScaled(70);
        guiGraphics.blit(TEXTURE, this.leftPos + 53, this.topPos + 32, 176, 0, i, 20);
    }
}
