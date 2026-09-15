package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineTurbofanBlockEntity;
import com.hbm.inventory.menus.MachineTurbofanMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineTurbofan.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 *
 * Die Rundanzeige fuer den Bluttank kommt im Original aus
 * GUIElements.renderGauge(Gauge.ROUND_SMALL, ...). Diese (dort als veraltet
 * markierte) Klasse fehlt im Port; das Bild small_round.png ist mit 13 Stufen
 * zu je 18x18 Pixeln uebernommen und wird hier direkt geblittet.
 */
public class MachineTurbofanScreen extends InfoScreen<MachineTurbofanMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/generators/gui_turbofan.png");
    private static final ResourceLocation GAUGE_ROUND_SMALL = NuclearTechMod.withDefaultNamespace("textures/gui/gauges/small_round.png");

    /** small_round.png: 13 Einzelbilder zu je 18x18 Pixeln untereinander */
    private static final int GAUGE_SIZE = 18;
    private static final int GAUGE_FRAMES = 13;

    private final MachineTurbofanBlockEntity be;

    public MachineTurbofanScreen(MachineTurbofanMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 203;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.be.tank.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 35, this.topPos + 17, 34, 52);
        if(this.be.showBlood) this.be.blood.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 98, this.topPos + 17, 16, 16);
        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 143, this.topPos + 17, 16, 52, this.be.power, MachineTurbofanBlockEntity.maxPower);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 43 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int i = (int) this.be.getPowerScaled(52);
        if(i > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 152 - 9, this.topPos + 69 - i, 176 + 16, 52 - i, 16, i, 256, 256);
        }

        if(this.be.afterburner > 0) {
            int a = Math.min(this.be.afterburner, 6);
            guiGraphics.blit(TEXTURE, this.leftPos + 98, this.topPos + 44, 176, (a - 1) * 16, 16, 16, 256, 256);
        }

        if(this.be.showBlood) {
            double progress = (double) this.be.blood.getFill() / (double) this.be.blood.getMaxFill();
            int frame = (int) Math.round((GAUGE_FRAMES - 1) * progress);
            guiGraphics.blit(GAUGE_ROUND_SMALL, this.leftPos + 97, this.topPos + 16, 0, frame * GAUGE_SIZE, GAUGE_SIZE, GAUGE_SIZE, GAUGE_SIZE, GAUGE_SIZE * GAUGE_FRAMES);
        }

        this.be.tank.renderTank(this.leftPos + 35, this.topPos + 69, 0, 34, 52);
    }
}
