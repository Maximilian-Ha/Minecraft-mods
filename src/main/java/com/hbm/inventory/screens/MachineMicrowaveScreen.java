package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineMicrowaveBlockEntity;
import com.hbm.inventory.menus.MachineMicrowaveMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMicrowave.
 * Blit-Koordinaten und die beiden Knopffelder sind unveraendert uebernommen.
 */
public class MachineMicrowaveScreen extends InfoScreen<MachineMicrowaveMenu> {

    private static final ResourceLocation TEXTURE =
            NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_microwave.png");

    private final MachineMicrowaveBlockEntity be;

    public MachineMicrowaveScreen(MachineMicrowaveMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 168;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 17, 16, 34,
                this.be.getPower(), this.be.getMaxPower());

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        CompoundTag tag = new CompoundTag();

        if(this.isHovered(mouseX, mouseY, 43, 26, 18, 18)) tag.putBoolean("schneller", true);
        if(this.isHovered(mouseX, mouseY, 43, 44, 18, 18)) tag.putBoolean("langsamer", true);

        if(!tag.isEmpty()) PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));

        return super.mouseClicked(mouseX, mouseY, button);
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

        int strom = (int) this.be.getPowerScaled(34);
        guiGraphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 51 - strom, 176, 34 - strom, 16, strom);

        int fortschritt = Math.min(this.be.getProgressScaled(23), 22);
        if(fortschritt > 0) guiGraphics.blit(TEXTURE, this.leftPos + 104, this.topPos + 34, 192, 0, fortschritt, 16);

        int tempo = this.be.getSpeedScaled(34);
        guiGraphics.blit(TEXTURE, this.leftPos + 62, this.topPos + 60 - tempo, 214, 34 - tempo, 4, tempo);
    }
}
