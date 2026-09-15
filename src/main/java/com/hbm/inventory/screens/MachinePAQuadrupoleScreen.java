package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.albion.MachinePAQuadrupoleBlockEntity;
import com.hbm.inventory.menus.MachinePAQuadrupoleMenu;
import com.hbm.items.machine.PACoilItem;
import com.hbm.items.machine.PACoilItem.EnumCoilType;
import com.hbm.main.NuclearTechMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIPAQuadrupole.
 * Blit- und Trefferkoordinaten unveraendert uebernommen.
 *
 * Drei Leuchten unten: kalt genug, Spule eingesetzt, Strom da. Stehen alle drei, laesst der
 * Quadrupol den Strahl durch -- und darueber zeigt ein Bild, welche Spule steckt.
 */
public class MachinePAQuadrupoleScreen extends InfoScreen<MachinePAQuadrupoleMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/particleaccelerator/gui_quadrupole.png");

    private final MachinePAQuadrupoleBlockEntity be;

    public MachinePAQuadrupoleScreen(MachinePAQuadrupoleMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 204;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.be.coolantTanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 116, this.topPos + 36, 16, 52);
        this.be.coolantTanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 134, this.topPos + 36, 16, 52);
        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 26, this.topPos + 18, 16, 52, this.be.power, this.be.getMaxPower());

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2 - 9, 6, 0xffffff, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);

        guiGraphics.drawString(this.font, Component.literal("/123K").withStyle(ChatFormatting.AQUA), 118, 22, 4210752, false);

        int heat = (int) Math.ceil(this.be.temperature);
        Component label = Component.literal(heat + "K").withStyle(heat > 123 ? ChatFormatting.RED : ChatFormatting.AQUA);
        guiGraphics.drawString(this.font, label, 148 - this.font.width(label), 12, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int power = (int) (this.be.power * 52 / this.be.getMaxPower());
        if(power > 0) guiGraphics.blit(TEXTURE, this.leftPos + 26, this.topPos + 70 - power, 184, 52 - power, 16, power);

        int heat = (int) Math.ceil(this.be.temperature);
        if(heat <= 123) guiGraphics.blit(TEXTURE, this.leftPos + 75, this.topPos + 64, 176, 8, 8, 8);

        EnumCoilType type = PACoilItem.getCoil(this.be.slots.get(MachinePAQuadrupoleBlockEntity.SLOT_COIL));
        if(type != null) {
            guiGraphics.blit(TEXTURE, this.leftPos + 85, this.topPos + 64, 176, 8, 8, 8);
            int u = type == EnumCoilType.NIOBIUM || type == EnumCoilType.CHLOROPHYTE ? 228 : 200;
            int v = type == EnumCoilType.BSCCO || type == EnumCoilType.CHLOROPHYTE ? 28 : 0;
            guiGraphics.blit(TEXTURE, this.leftPos + 65, this.topPos + 30, u, v, 28, 28);
        }

        if(this.be.power >= MachinePAQuadrupoleBlockEntity.usage) guiGraphics.blit(TEXTURE, this.leftPos + 65, this.topPos + 64, 176, 8, 8, 8);

        this.be.coolantTanks[0].renderTank(this.leftPos + 116, this.topPos + 88, 0, 16, 52);
        this.be.coolantTanks[1].renderTank(this.leftPos + 134, this.topPos + 88, 0, 16, 52);
    }
}
