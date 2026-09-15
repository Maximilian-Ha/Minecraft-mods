package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineOreSlopperBlockEntity;
import com.hbm.inventory.menus.MachineOreSlopperMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Portiert aus 1.7.10: com.hbm.inventory.gui.GUIOreSlopper. */
public class MachineOreSlopperScreen extends InfoScreen<MachineOreSlopperMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/ore_slopper.png");

    private final MachineOreSlopperBlockEntity be;

    public MachineOreSlopperScreen(MachineOreSlopperMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);

        this.be = menu.be;

        this.imageWidth = 176;
        this.imageHeight = 204;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        be.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 26, this.topPos + 18, 34, 52);
        be.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 116, this.topPos + 18, 16, 52);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 18, 16, 52, be.power, MachineOreSlopperBlockEntity.MAX_POWER);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2 - 9, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        /* Der Fortschrittsbalken laeuft von unten nach oben. */
        int i = (int) (be.progress * 35);
        guiGraphics.blit(TEXTURE, this.leftPos + 62, this.topPos + 52 - i, 176, 34 - i, 34, i);

        int j = (int) (be.power * 52 / MachineOreSlopperBlockEntity.MAX_POWER);
        guiGraphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 70 - j, 176, 86 - j, 16, j);

        /* Die Leuchte oben: Strom reicht fuer einen Durchgang. */
        if(be.power >= be.consumption) guiGraphics.blit(TEXTURE, this.leftPos + 12, this.topPos + 4, 202, 34, 9, 12);

        be.tanks[0].renderTank(this.leftPos + 26, this.topPos + 70, 1F, 16, 52);
        be.tanks[1].renderTank(this.leftPos + 116, this.topPos + 70, 1F, 16, 52);
    }
}
