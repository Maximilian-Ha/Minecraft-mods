package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.oil.MachineCokerBlockEntity;
import com.hbm.inventory.menus.MachineCokerMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineCoker.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 */
public class MachineCokerScreen extends InfoScreen<MachineCokerMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_coker.png");

    private final MachineCokerBlockEntity be;

    public MachineCokerScreen(MachineCokerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 204;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.be.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 35, this.topPos + 18, 16, 52);
        this.be.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 125, this.topPos + 18, 16, 52);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 60, this.topPos + 45, 54, 7, mouseX, mouseY,
                Component.literal(tu(this.be.progress, MachineCokerBlockEntity.PROCESS_TIME)));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 60, this.topPos + 54, 54, 7, mouseX, mouseY,
                Component.literal(tu(this.be.heat, MachineCokerBlockEntity.MAX_HEAT)));

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private static String tu(int value, int max) {
        return String.format(Locale.US, "%,d", value) + " / " + String.format(Locale.US, "%,d", max) + "TU";
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int p = this.be.progress * 53 / MachineCokerBlockEntity.PROCESS_TIME;
        guiGraphics.blit(TEXTURE, this.leftPos + 61, this.topPos + 46, 176, 0, p, 5, 256, 256);

        int h = this.be.heat * 52 / MachineCokerBlockEntity.MAX_HEAT;
        guiGraphics.blit(TEXTURE, this.leftPos + 61, this.topPos + 55, 176, 5, h, 5, 256, 256);

        this.be.tanks[0].renderTank(this.leftPos + 35, this.topPos + 70, 0, 16, 52);
        this.be.tanks[1].renderTank(this.leftPos + 125, this.topPos + 70, 0, 16, 52);
    }
}
