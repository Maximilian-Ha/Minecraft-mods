package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineChemicalFactoryBlockEntity;
import com.hbm.inventory.menus.MachineChemicalFactoryMenu;
import com.hbm.inventory.recipes.ChemicalPlantRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.BlueprintsItem;
import com.hbm.main.NuclearTechMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineChemicalFactory.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 *
 * Vier Rezeptfelder untereinander, jedes mit eigener Vorlage, eigenem Fortschrittsbalken und
 * zwei Lampen: die linke sagt "Rezept eingestellt", die rechte "Strom und Kuehlung reichen".
 */
public class MachineChemicalFactoryScreen extends InfoScreen<MachineChemicalFactoryMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_chemical_factory.png");

    private final MachineChemicalFactoryBlockEntity be;

    public MachineChemicalFactoryScreen(MachineChemicalFactoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 248;
        this.imageHeight = 216;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 4; j++) {
                this.be.inputTanks[i + j * 3].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 60 + i * 5, this.topPos + 20 + j * 22, 3, 16);
                this.be.outputTanks[i + j * 3].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 189 + i * 5, this.topPos + 20 + j * 22, 3, 16);
            }
        }

        this.be.water.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 224, this.topPos + 125, 7, 52);
        this.be.lps.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 233, this.topPos + 125, 7, 52);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 224, this.topPos + 18, 16, 68, this.be.power, this.be.maxPower);

        for(int i = 0; i < MachineChemicalFactoryBlockEntity.FIELDS; i++) {

            if(!this.isHovered(mouseX, mouseY, 74, 19 + i * 22, 18, 18)) continue;

            GenericRecipe recipe = this.be.modules[i].getRecipe();

            if(recipe != null) {
                guiGraphics.renderComponentTooltip(this.font, recipe.print(), mouseX, mouseY);
            } else {
                guiGraphics.renderTooltip(this.font, Component.translatable("container.recipe.set_recipe").withStyle(ChatFormatting.YELLOW), mouseX, mouseY);
            }
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        for(int i = 0; i < MachineChemicalFactoryBlockEntity.FIELDS; i++) {
            if(this.isHovered(mouseX, mouseY, 74, 19 + i * 22, 18, 18)) {
                RecipeSelectorScreen.openSelector(ChemicalPlantRecipes.INSTANCE, this.be, this.be.modules[i].recipe, i,
                        BlueprintsItem.grabPool(this.be.slots.get(4 + i * 7)), this);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 106 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 26, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        /* Zwei Teile: oben die vier Rezeptfelder, unten der schmalere Rucksackbereich. */
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 248, 116, 256, 256);
        guiGraphics.blit(TEXTURE, this.leftPos + 18, this.topPos + 116, 18, 116, 230, 100, 256, 256);

        int p = (int) (this.be.power * 68 / Math.max(this.be.maxPower, 1L));
        if(p > 0) guiGraphics.blit(TEXTURE, this.leftPos + 224, this.topPos + 86 - p, 0, 184 - p, 16, p, 256, 256);

        for(int i = 0; i < MachineChemicalFactoryBlockEntity.FIELDS; i++) {

            GenericRecipe recipe = this.be.modules[i].getRecipe();

            if(this.be.modules[i].progress > 0) {
                int j = (int) Math.ceil(22 * this.be.modules[i].progress);
                guiGraphics.blit(TEXTURE, this.leftPos + 113, this.topPos + 29 + i * 22, 0, 216, j, 6, 256, 256);
            }

            /* Linke Lampe: Rezept steht. */
            if(this.be.didProcess[i]) {
                guiGraphics.blit(TEXTURE, this.leftPos + 113, this.topPos + 21 + i * 22, 4, 222, 4, 4, 256, 256);
            } else if(recipe != null) {
                guiGraphics.blit(TEXTURE, this.leftPos + 113, this.topPos + 21 + i * 22, 0, 222, 4, 4, 256, 256);
            }

            /* Rechte Lampe: Strom und Kuehlung reichen. */
            if(this.be.didProcess[i]) {
                guiGraphics.blit(TEXTURE, this.leftPos + 121, this.topPos + 21 + i * 22, 4, 222, 4, 4, 256, 256);
            } else if(recipe != null && this.be.power >= recipe.power && this.be.canCool()) {
                guiGraphics.blit(TEXTURE, this.leftPos + 121, this.topPos + 21 + i * 22, 0, 222, 4, 4, 256, 256);
            }

            guiGraphics.renderItem(recipe != null ? recipe.getIcon() : new ItemStack(NtmItems.TEMPLATE_FOLDER.get()),
                    this.leftPos + 75, this.topPos + 20 + i * 22);

            if(recipe != null && recipe.inputItem != null) {

                RenderSystem.setShaderColor(1F, 1F, 1F, 0.5F);

                for(int k = 0; k < recipe.inputItem.length; k++) {
                    Slot slot = this.menu.slots.get(this.be.modules[i].inputSlots[k]);
                    if(!slot.hasItem()) guiGraphics.renderItem(recipe.inputItem[k].extractForCyclingDisplay(20), this.leftPos + slot.x, this.topPos + slot.y);
                }

                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            }
        }

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 4; j++) {
                this.be.inputTanks[i + j * 3].renderTank(this.leftPos + 60 + i * 5, this.topPos + 36 + j * 22, 1F, 3, 16);
                this.be.outputTanks[i + j * 3].renderTank(this.leftPos + 189 + i * 5, this.topPos + 36 + j * 22, 1F, 3, 16);
            }
        }

        this.be.water.renderTank(this.leftPos + 224, this.topPos + 177, 1F, 7, 52);
        this.be.lps.renderTank(this.leftPos + 233, this.topPos + 177, 1F, 7, 52);
    }
}
