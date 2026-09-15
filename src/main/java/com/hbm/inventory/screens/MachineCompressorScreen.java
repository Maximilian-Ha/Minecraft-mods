package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineCompressorBlockEntity;
import com.hbm.inventory.menus.MachineCompressorMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUICompressor.
 * Blit- und Trefferkoordinaten unveraendert uebernommen.
 */
public class MachineCompressorScreen extends InfoScreen<MachineCompressorMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_compressor.png");

    private final MachineCompressorBlockEntity be;

    public MachineCompressorScreen(MachineCompressorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 204;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.be.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 17, this.topPos + 18, 16, 52);
        this.be.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 107, this.topPos + 18, 16, 52);
        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 152, this.topPos + 18, 16, 52, this.be.power, MachineCompressorBlockEntity.maxPower);

        for(int j = 0; j < 5; j++) {
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 43 + j * 11, this.topPos + 46, 8, 14, mouseX, mouseY,
                    Component.literal(j + " PU -> " + (j + 1) + " PU"));
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 70 - this.font.width(this.title) / 2, 6, 0xC7C1A3, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        if(this.be.power >= this.be.powerRequirement) {
            guiGraphics.blit(TEXTURE, this.leftPos + 156, this.topPos + 4, 176, 52, 9, 12, 256, 256);
        }

        guiGraphics.blit(TEXTURE, this.leftPos + 43 + this.be.tanks[0].getPressure() * 11, this.topPos + 46, 193, 18, 8, 124, 256, 256);

        // Division abgesichert -- processTime kommt vom Server und ist dort immer >= 1
        int progress = this.be.progress * 55 / Math.max(1, this.be.processTime);
        if(progress > 0) guiGraphics.blit(TEXTURE, this.leftPos + 42, this.topPos + 26, 192, 0, progress, 17, 256, 256);

        int power = (int) (this.be.power * 52 / MachineCompressorBlockEntity.maxPower);
        if(power > 0) guiGraphics.blit(TEXTURE, this.leftPos + 152, this.topPos + 70 - power, 176, 52 - power, 16, power, 256, 256);

        this.be.tanks[0].renderTank(this.leftPos + 17, this.topPos + 70, 0, 16, 52);
        this.be.tanks[1].renderTank(this.leftPos + 107, this.topPos + 70, 0, 16, 52);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(button == 0) {
            for(int j = 0; j < 5; j++) {
                if(this.isHovered(mouseX, mouseY, 43 + j * 11, 46, 8, 14)) {
                    this.click();
                    CompoundTag tag = new CompoundTag();
                    tag.putInt("compression", j);
                    PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
