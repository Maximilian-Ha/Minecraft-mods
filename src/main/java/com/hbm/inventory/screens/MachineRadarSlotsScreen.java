package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineRadarBlockEntity;
import com.hbm.inventory.menus.MachineRadarMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineRadarNTSlots.
 *
 * Die Faecherseite des Radars. Oben links sitzt die Umschaltflaeche zurueck zur Karte -- dieselbe
 * Stelle wie im Original.
 */
public class MachineRadarSlotsScreen extends InfoScreen<MachineRadarMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_radar_link.png");

    private final MachineRadarBlockEntity be;

    public MachineRadarSlotsScreen(MachineRadarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 184;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        if(this.isHovered(mouseX, mouseY, 5, 5, 8, 8)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("radar.toggleGui"), mouseX, mouseY);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered(mouseX, mouseY, 5, 5, 8, 8)) {
            this.click();
            this.onClose();
            NuclearTechMod.proxy.openScreen(this.minecraft.player, this.be.getBlockPos());
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
