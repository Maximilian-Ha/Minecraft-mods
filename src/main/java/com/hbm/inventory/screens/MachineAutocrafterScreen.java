package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineAutocrafterBlockEntity;
import com.hbm.inventory.menus.MachineAutocrafterMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIAutocrafter.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 *
 * ZWEI HINWEISE MACHEN DIE OBERFLAECHE ERST BEDIENBAR. Am Musterfach steht, wie es vergleicht --
 * den zeichnet der gemeinsame Unterbau. Am Vorschaufach steht, das wievielte von wie vielen
 * passenden Rezepten gerade gebaut wird; ohne ihn saehe man nur ein Ergebnis und wuesste nicht,
 * dass es noch andere gibt.
 */
public class MachineAutocrafterScreen extends FilterScreen<MachineAutocrafterMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_autocrafter.png");

    private final MachineAutocrafterBlockEntity be;

    public MachineAutocrafterScreen(MachineAutocrafterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 240;
    }

    @Override
    protected String modeAt(int slot) {
        return this.be.matcher.modes[slot];
    }

    @Override
    protected void renderOwnTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 17, this.topPos + 45, 16, 52,
                this.be.getPower(), this.be.getMaxPower());

        if(!this.menu.getCarried().isEmpty()) return;
        if(this.be.getItem(MachineAutocrafterBlockEntity.SLOT_PREVIEW).isEmpty()) return;
        if(!this.isHovered(mouseX, mouseY, 116, 40, 16, 16)) return;

        /* Dreissig Pixel hoeher als der Zeiger, damit der Hinweis das Fach nicht verdeckt. */
        guiGraphics.renderComponentTooltip(this.font, List.of(
                Component.translatable("autocrafter.rightClick").withStyle(ChatFormatting.RED),
                Component.literal((this.be.recipeIndex + 1) + " / " + this.be.recipeCount).withStyle(ChatFormatting.YELLOW)),
                mouseX, mouseY - 30);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int power = (int) (this.be.getPower() * 52 / this.be.getMaxPower());
        if(power > 0) guiGraphics.blit(TEXTURE, this.leftPos + 17, this.topPos + 97 - power, 176, 52 - power, 16, power, 256, 256);
    }
}
