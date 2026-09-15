package com.hbm.inventory.screens;

import com.hbm.inventory.menus.MachineSatDockMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/** Portiert aus 1.7.10: com.hbm.inventory.gui.GUISatDock. */
public class MachineSatDockScreen extends InfoScreen<MachineSatDockMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/gui_sat_dock.png");

    public MachineSatDockScreen(MachineSatDockMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = 176;
        this.imageHeight = 186;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = 115 - this.font.width(this.title) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        List<Component> text = new ArrayList<>();
        for(String s : I18nUtil.resolveKeyArray("desc.gui.satdock.desc")) text.add(Component.literal(s));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos - 7, this.topPos + 36, 16, 16, this.leftPos + 1, this.topPos + 36 + 16, text);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int partialTicks) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        this.drawInfoPanel(guiGraphics, this.leftPos - 7, this.topPos + 36, 2);
    }
}
