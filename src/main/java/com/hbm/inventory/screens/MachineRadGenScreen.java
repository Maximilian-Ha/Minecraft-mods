package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineRadGenBlockEntity;
import com.hbm.inventory.menus.MachineRadGenMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineRadGen.
 * Blit- und Trefferkoordinaten unveraendert uebernommen.
 *
 * Zwoelf schmale Balken untereinander, einer je Bahn. Wer daraufzeigt, liest Leistung und
 * Restlaufzeit ab -- anders waere bei zwoelf gleichzeitig laufenden Brennstoffen nicht zu
 * erkennen, welcher wann leer ist.
 */
public class MachineRadGenScreen extends InfoScreen<MachineRadGenMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_radgen.png");

    private final MachineRadGenBlockEntity be;

    public MachineRadGenScreen(MachineRadGenMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 184;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 64, this.topPos + 83, 48, 4,
                this.be.power, MachineRadGenBlockEntity.maxPower);

        for(int i = 0; i < MachineRadGenBlockEntity.LANES; i++) {

            if(this.be.maxProgress[i] <= 0) continue;

            int left = this.be.maxProgress[i] - this.be.progress[i];

            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 65, this.topPos + 18 + i * 5, 46, 5, mouseX, mouseY,
                    Component.literal("Slot " + (i + 1) + ":"),
                    Component.literal(this.be.production[i] + "HE/t for"),
                    Component.literal(left + " ticks (" + (left * 100 / this.be.maxProgress[i]) + "%)"));
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        for(int i = 0; i < MachineRadGenBlockEntity.LANES; i++) {
            if(this.be.maxProgress[i] <= 0) continue;
            int bar = this.be.progress[i] * 44 / this.be.maxProgress[i];
            if(bar > 0) guiGraphics.blit(TEXTURE, this.leftPos + 66, this.topPos + 19 + i * 5, 176, 0, bar, 3);
        }

        int power = (int) (this.be.power * 48 / MachineRadGenBlockEntity.maxPower);
        if(power > 0) guiGraphics.blit(TEXTURE, this.leftPos + 64, this.topPos + 83, 176, 3, power, 4);
    }
}
