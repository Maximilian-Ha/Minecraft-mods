package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineDiFurnaceBlockEntity;
import com.hbm.inventory.menus.MachineDiFurnaceMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIDiFurnace.
 * Blit-Koordinaten unveraendert uebernommen.
 */
public class MachineDiFurnaceScreen extends InfoScreen<MachineDiFurnaceMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_difurnace.png");

    private final MachineDiFurnaceBlockEntity be;

    public MachineDiFurnaceScreen(MachineDiFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Zeigt ueber den drei Eingabeslots an, von welcher Seite sie annehmen.
        Slot hovered = this.hoveredSlot;
        if(hovered != null && hovered.index < 3 && this.menu.getCarried().isEmpty()) {

            byte dir = switch(hovered.index) {
                case MachineDiFurnaceBlockEntity.SLOT_INPUT_UPPER -> this.be.sideUpper;
                case MachineDiFurnaceBlockEntity.SLOT_INPUT_LOWER -> this.be.sideLower;
                default -> this.be.sideFuel;
            };

            String name = Direction.from3DDataValue(dir).getName().toUpperCase(Locale.US);
            Component label = Component.literal("Accepts items from: " + name).withStyle(ChatFormatting.YELLOW);

            guiGraphics.renderComponentTooltip(this.font, List.of(label), mouseX, mouseY - (hovered.hasItem() ? 15 : 0));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if(this.be.hasPower()) {
            int fuel = this.be.getPowerRemainingScaled(52);
            guiGraphics.blit(TEXTURE, this.leftPos + 44, this.topPos + 70 - fuel, 201, 53 - fuel, 16, fuel);
        }

        int progress = this.be.getDiFurnaceProgressScaled(24);
        guiGraphics.blit(TEXTURE, this.leftPos + 101, this.topPos + 35, 176, 14, progress + 1, 17);

        if(this.be.hasPower() && (this.be.canProcess() || progress > 0)) {
            guiGraphics.blit(TEXTURE, this.leftPos + 63, this.topPos + 37, 176, 0, 14, 14);
        }
    }
}
