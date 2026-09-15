package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.icf.ICFBlockEntity;
import com.hbm.inventory.menus.ICFMenu;
import com.hbm.inventory.screens.element.ScreenElements;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIICF.
 *
 * Links die Laseranzeige, rechts die drei Tanks -- kaltes Natrium, heisses, Sternenfluss -- und
 * dazwischen die Waerme.
 */
public class ICFScreen extends InfoScreen<ICFMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_icf.png");

    private final ICFBlockEntity be;

    public ICFScreen(ICFMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.be = menu.be;

        this.imageWidth = 248;
        this.imageHeight = 222;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 44;
        this.inventoryLabelY = this.imageHeight - 93;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.be.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 44, this.topPos + 18, 16, 70);
        this.be.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 188, this.topPos + 18, 16, 70);
        this.be.tanks[2].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 224, this.topPos + 18, 16, 70);

        Component laser = this.be.maxLaser <= 0
                ? Component.literal("OFFLINE")
                : Component.literal(BobMathUtil.getShortNumber(this.be.laser) + "TU/t - " + (this.be.laser * 1000 / this.be.maxLaser) / 10D + "%");

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 18, 16, 70, laser);
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 187, this.topPos + 89, 18, 18,
                Component.literal(BobMathUtil.getShortNumber(this.be.heat) + " / " + BobMathUtil.getShortNumber(ICFBlockEntity.MAX_HEAT) + "TU"));

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, 114);
        guiGraphics.blit(TEXTURE, this.leftPos + 36, this.topPos + 122, 36, 122, 176, 108);

        if(this.be.maxLaser > 0) {
            int p = (int) (this.be.laser * 70 / this.be.maxLaser);
            guiGraphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 88 - p, 212, 192 - p, 16, p);
        }

        ScreenElements.drawSmoothGauge(this.leftPos + 196, this.topPos + 98,
                (float) this.be.heat / (float) ICFBlockEntity.MAX_HEAT, 5, 2, 1, 0xFF00AF);

        this.be.tanks[0].renderTank(this.leftPos + 44, this.topPos + 88, 0F, 16, 70);
        this.be.tanks[1].renderTank(this.leftPos + 188, this.topPos + 88, 0F, 16, 70);
        this.be.tanks[2].renderTank(this.leftPos + 224, this.topPos + 88, 0F, 16, 70);
    }
}
