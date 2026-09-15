package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineStrandCasterBlockEntity;
import com.hbm.inventory.menus.MachineStrandCasterMenu;
import com.hbm.items.machine.MoldItem.Mold;
import com.hbm.main.NuclearTechMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineStrandCaster.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 *
 * Der Metallstand wird in der Farbe der Schmelze gezeichnet -- dieselbe Faerbung, die auch die
 * Giessform benutzt.
 */
public class MachineStrandCasterScreen extends InfoScreen<MachineStrandCasterMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_strand_caster.png");

    private final MachineStrandCasterBlockEntity be;

    public MachineStrandCasterScreen(MachineStrandCasterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 214;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        List<Component> text = new ArrayList<>();
        Mold mold = this.be.getInstalledMold();

        if(this.be.type == null) {
            text.add(Component.translatable("foundry.empty").withStyle(ChatFormatting.RED));
        } else {
            text.add(this.be.type.getName().append(": " + this.be.amount + " / " + this.be.getCapacity()).withStyle(ChatFormatting.YELLOW));
        }

        if(mold != null) text.add(mold.getTitle().copy().withStyle(ChatFormatting.BLUE));

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 16, this.topPos + 17, 34, 76, mouseX, mouseY, text);

        this.be.water.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 82, this.topPos + 14, 16, 24);
        this.be.steam.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 82, this.topPos + 65, 16, 24);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 4, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        if(this.be.amount > 0 && this.be.type != null && this.be.getCapacity() > 0) {

            int height = Math.min(this.be.amount * 79 / this.be.getCapacity(), 92);

            if(height > 0) {
                guiGraphics.setColor(
                        (this.be.type.moltenColor >> 16 & 0xFF) / 255F,
                        (this.be.type.moltenColor >> 8 & 0xFF) / 255F,
                        (this.be.type.moltenColor & 0xFF) / 255F, 1F);
                guiGraphics.blit(TEXTURE, this.leftPos + 17, this.topPos + 93 - height, 176, 89 - height, 34, height, 256, 256);
                guiGraphics.setColor(1F, 1F, 1F, 1F);
            }
        }

        this.be.water.renderTank(this.leftPos + 82, this.topPos + 38, 0, 16, 24);
        this.be.steam.renderTank(this.leftPos + 82, this.topPos + 89, 0, 16, 24);
    }
}
