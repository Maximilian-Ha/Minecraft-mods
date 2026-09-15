package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.rbmk.RBMKRodBlockEntity;
import com.hbm.inventory.menus.RBMKRodMenu;
import com.hbm.items.machine.RBMKRodItem;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIRBMKRod.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 */
public class RBMKRodScreen extends InfoScreen<RBMKRodMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_rbmk_element.png");

    private final RBMKRodBlockEntity rod;

    public RBMKRodScreen(RBMKRodMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.rod = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 186;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        if(!this.rod.coldEnoughForAutoloader()) {
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos - 16, this.topPos + 20, 16, 16, this.leftPos - 8, this.topPos + 20 + 16,
                    Component.translatable("container.rbmkRod.tooHotAuto"));
        }

        if(!this.rod.coldEnoughForManual()) {
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos - 16, this.topPos + 36, 16, 16, this.leftPos - 8, this.topPos + 36 + 16,
                    Component.translatable("container.rbmkRod.tooHotManual"));
        }

        if(this.rod.fuelYield != null) {
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 34, this.topPos + 21, 18, 67, mouseX, mouseY, Component.literal(this.rod.fuelYield));
        }

        if(this.rod.fuelXenon != null) {
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 126, this.topPos + 24, 14, 58, mouseX, mouseY, Component.literal(this.rod.fuelXenon));
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

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        ItemStack stack = this.rod.getItem(0);

        if(stack.getItem() instanceof RBMKRodItem) {

            guiGraphics.blit(TEXTURE, this.leftPos + 34, this.topPos + 21, 176, 0, 18, 67, 256, 256);

            double depletion = 1D - RBMKRodItem.getEnrichment(stack);
            int d = (int) (depletion * 67);
            guiGraphics.blit(TEXTURE, this.leftPos + 34, this.topPos + 21, 194, 0, 18, d, 256, 256);

            double xenon = RBMKRodItem.getPoisonLevel(stack);
            int x = (int) (xenon * 58);
            guiGraphics.blit(TEXTURE, this.leftPos + 126, this.topPos + 82 - x, 212, 58 - x, 14, x, 256, 256);
        }

        if(!this.rod.coldEnoughForAutoloader()) this.drawInfoPanel(guiGraphics, this.leftPos - 16, this.topPos + 20, 6);
        if(!this.rod.coldEnoughForManual()) this.drawInfoPanel(guiGraphics, this.leftPos - 16, this.topPos + 36, 7);
    }
}
