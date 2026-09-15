package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineEPressBlockEntity;
import com.hbm.inventory.menus.MachineEPressMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineEPress.
 * Alle Blit-Koordinaten sind unveraendert uebernommen.
 */
public class MachineEPressScreen extends InfoScreen<MachineEPressMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_electric_press.png");

    private final MachineEPressBlockEntity be;

    public MachineEPressScreen(MachineEPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 186;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 152, this.topPos + 18, 16, 34,
                this.be.getPower(), this.be.getMaxPower());

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 111, this.topPos + 32, 16, 16, mouseX, mouseY,
                Component.translatable("desc.gui.upgrade"),
                Component.translatable("desc.gui.upgrade.speed"));

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 89 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int power = (int) (this.be.getPower() * 34 / this.be.getMaxPower());
        if(power > 0) guiGraphics.blit(TEXTURE, this.leftPos + 152, this.topPos + 52 - power, 176, 34 - power, 16, power, 256, 256);

        int stamp = (int) (this.be.renderPress * 16 / MachineEPressBlockEntity.maxPress);
        if(stamp > 0) guiGraphics.blit(TEXTURE, this.leftPos + 18, this.topPos + 33, 192, 0, 18, stamp, 256, 256);
    }
}
