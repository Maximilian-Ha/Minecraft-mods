package com.hbm.inventory.screens;

import com.hbm.inventory.menus.MachineKeyForgeMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineKeyForge.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 *
 * Zwei Hinweisfelder erklaeren die Faecher: das obere das Kopierwerk, das untere den Zufall.
 * Ohne sie waere an der Oberflaeche nicht zu sehen, was die drei Faecher unterscheidet.
 */
public class MachineKeyForgeScreen extends InfoScreen<MachineKeyForgeMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_keyforge.png");

    public MachineKeyForgeScreen(MachineKeyForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 186;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 12, this.topPos + 28, 16, 16,
                this.leftPos + 20, this.topPos + 44, Component.translatable("desc.gui.keyforge.key"));

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 12, this.topPos + 44, 16, 16,
                this.leftPos + 20, this.topPos + 60, Component.translatable("desc.gui.keyforge.random"));

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        this.drawInfoPanel(guiGraphics, this.leftPos + 12, this.topPos + 28, 2);
        this.drawInfoPanel(guiGraphics, this.leftPos + 12, this.topPos + 44, 3);
    }
}
