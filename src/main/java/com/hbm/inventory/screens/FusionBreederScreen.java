package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.fusion.FusionBreederBlockEntity;
import com.hbm.inventory.menus.FusionBreederMenu;
import com.hbm.inventory.screens.element.ScreenElements;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.BobMathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIFusionBreeder.
 *
 * Der Zeiger fuer den einlaufenden Fluss steigt nicht linear, sondern saettigt sich: er zeigt
 * eins minus e hoch minus Fluss -- so sieht man auch bei kleinen Werten noch einen Ausschlag.
 */
public class FusionBreederScreen extends InfoScreen<FusionBreederMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_fusion_breeder.png");

    private final FusionBreederBlockEntity breeder;

    public FusionBreederScreen(FusionBreederMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.breeder = menu.be;

        this.imageWidth = 176;
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 35;
        this.inventoryLabelY = this.imageHeight - 93;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 79, this.topPos + 23, 18, 18,
                Component.literal(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET
                        + (int) Math.ceil(this.breeder.neutronEnergy) + " flux/t"));

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 67, this.topPos + 46, 42, 14,
                Component.literal(BobMathUtil.format((int) Math.ceil(this.breeder.progress)) + " / "
                        + BobMathUtil.format((int) Math.ceil(FusionBreederBlockEntity.CAPACITY)) + " flux"));

        this.breeder.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 26, this.topPos + 18, 16, 52);
        this.breeder.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 134, this.topPos + 18, 16, 52);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int p = (int) Math.ceil(this.breeder.progress * 42 / FusionBreederBlockEntity.CAPACITY);
        if(p > 0) guiGraphics.blit(TEXTURE, this.leftPos + 67, this.topPos + 48, 176, 0, p, 10);

        double gauge = 1D - Math.pow(Math.E, -this.breeder.neutronEnergy * 10 / FusionBreederBlockEntity.CAPACITY);
        ScreenElements.drawSmoothGauge(this.leftPos + 88, this.topPos + 32, (float) gauge, 5, 2, 1, 0xA00000);

        this.breeder.tanks[0].renderTank(this.leftPos + 26, this.topPos + 70, 0F, 16, 52);
        this.breeder.tanks[1].renderTank(this.leftPos + 134, this.topPos + 70, 0F, 16, 52);
    }
}
