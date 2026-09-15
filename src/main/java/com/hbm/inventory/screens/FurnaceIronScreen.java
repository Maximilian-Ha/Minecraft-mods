package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.FurnaceIronBlockEntity;
import com.hbm.inventory.menus.FurnaceIronMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIFurnaceIron.
 * Blit-Koordinaten und Texturaufteilung unveraendert.
 */
public class FurnaceIronScreen extends InfoScreen<FurnaceIronMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_furnace_iron.png");

    private final FurnaceIronBlockEntity be;

    public FurnaceIronScreen(FurnaceIronMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Brennwert-Boni ueber den leeren Brennstoffslots, wie im Original.
        Slot hoveredSlot = this.hoveredSlot;
        if(hoveredSlot != null
                && hoveredSlot.index >= FurnaceIronBlockEntity.SLOT_FUEL_1
                && hoveredSlot.index <= FurnaceIronBlockEntity.SLOT_FUEL_2
                && this.menu.getCarried().isEmpty()
                && !hoveredSlot.hasItem()) {
            List<Component> components = FurnaceIronBlockEntity.BURN_MODULE.getTimeDesc();
            if(!components.isEmpty()) guiGraphics.renderComponentTooltip(this.font, components, mouseX, mouseY);
        }

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 52, this.topPos + 35, 71, 7, mouseX, mouseY,
                Component.literal((this.be.progress * 100 / Math.max(this.be.processingTime, 1)) + "%"));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 52, this.topPos + 44, 71, 7, mouseX, mouseY,
                Component.literal((this.be.burnTime / 20) + "s"));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int progress = this.be.getProgressScaled(70);
        if(progress > 0) guiGraphics.blit(TEXTURE, this.leftPos + 53, this.topPos + 36, 176, 18, progress, 5);

        int burn = this.be.getBurnTimeScaled(70);
        if(burn > 0) guiGraphics.blit(TEXTURE, this.leftPos + 53, this.topPos + 45, 176, 23, burn, 5);

        // Das Original fragt hier canSmelt() ab; auf dem Client steht dafuer das synchronisierte
        // wasOn-Flag zur Verfuegung, eine Rezeptsuche im Renderpfad entfaellt damit.
        if(this.be.isSmelting()) guiGraphics.blit(TEXTURE, this.leftPos + 70, this.topPos + 16, 176, 0, 18, 18);
    }
}
