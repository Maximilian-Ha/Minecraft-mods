package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineAssemblyFactoryBlockEntity;
import com.hbm.inventory.menus.MachineAssemblyFactoryMenu;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
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
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineAssemblyFactory.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 *
 * Vier Montagefelder in zwei Reihen zu zwei, jedes mit zwoelf Zutatenfaechern, einer Vorlage,
 * einem Fortschrittsbalken und zwei Lampen.
 */
public class MachineAssemblyFactoryScreen extends InfoScreen<MachineAssemblyFactoryMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_assembly_factory.png");

    private static final int STRIDE = 14;

    private final MachineAssemblyFactoryBlockEntity be;

    public MachineAssemblyFactoryScreen(MachineAssemblyFactoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 256;
        this.imageHeight = 240;
    }

    private static int fieldX(int i) { return (i % 2) * 109; }
    private static int fieldY(int i) { return (i / 2) * 56; }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        for(int i = 0; i < MachineAssemblyFactoryBlockEntity.FIELDS; i++) {
            this.be.inputTanks[i].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 105 + fieldX(i), this.topPos + 20 + fieldY(i), 5, 32);
            this.be.outputTanks[i].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 105 + fieldX(i), this.topPos + 54 + fieldY(i), 5, 16);
        }

        this.be.water.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 232, this.topPos + 149, 7, 52);
        this.be.lps.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 241, this.topPos + 149, 7, 52);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 234, this.topPos + 18, 16, 92, this.be.power, this.be.maxPower);

        for(int i = 0; i < MachineAssemblyFactoryBlockEntity.FIELDS; i++) {

            if(!this.isHovered(mouseX, mouseY, 6 + fieldX(i), 53 + fieldY(i), 18, 18)) continue;

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

        for(int i = 0; i < MachineAssemblyFactoryBlockEntity.FIELDS; i++) {
            if(this.isHovered(mouseX, mouseY, 6 + fieldX(i), 53 + fieldY(i), 18, 18)) {
                RecipeSelectorScreen.openSelector(AssemblyMachineRecipes.INSTANCE, this.be, this.be.modules[i].recipe, i,
                        BlueprintsItem.grabPool(this.be.slots.get(4 + i * STRIDE)), this);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 113 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 33, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 256, 140, 256, 256);
        guiGraphics.blit(TEXTURE, this.leftPos + 25, this.topPos + 140, 25, 140, 231, 100, 256, 256);

        int p = (int) (this.be.power * 92 / Math.max(this.be.maxPower, 1L));
        if(p > 0) guiGraphics.blit(TEXTURE, this.leftPos + 234, this.topPos + 110 - p, 0, 232 - p, 16, p, 256, 256);

        for(int i = 0; i < MachineAssemblyFactoryBlockEntity.FIELDS; i++) {

            GenericRecipe recipe = this.be.modules[i].getRecipe();

            if(this.be.modules[i].progress > 0) {
                int j = (int) Math.ceil(37 * this.be.modules[i].progress);
                guiGraphics.blit(TEXTURE, this.leftPos + 45 + fieldX(i), this.topPos + 63 + fieldY(i), 0, 240, j, 6, 256, 256);
            }

            /* Linke Lampe: Rezept steht. */
            if(this.be.didProcess[i]) {
                guiGraphics.blit(TEXTURE, this.leftPos + 45 + fieldX(i), this.topPos + 55 + fieldY(i), 4, 236, 4, 4, 256, 256);
            } else if(recipe != null) {
                guiGraphics.blit(TEXTURE, this.leftPos + 45 + fieldX(i), this.topPos + 55 + fieldY(i), 0, 236, 4, 4, 256, 256);
            }

            /* Rechte Lampe: Strom und Kuehlung reichen. */
            if(this.be.didProcess[i]) {
                guiGraphics.blit(TEXTURE, this.leftPos + 53 + fieldX(i), this.topPos + 55 + fieldY(i), 4, 236, 4, 4, 256, 256);
            } else if(recipe != null && this.be.power >= recipe.power && this.be.canCool()) {
                guiGraphics.blit(TEXTURE, this.leftPos + 53 + fieldX(i), this.topPos + 55 + fieldY(i), 0, 236, 4, 4, 256, 256);
            }

            guiGraphics.renderItem(recipe != null ? recipe.getIcon() : new ItemStack(NtmItems.TEMPLATE_FOLDER.get()),
                    this.leftPos + 7 + fieldX(i), this.topPos + 54 + fieldY(i));

            if(recipe != null && recipe.inputItem != null) {

                RenderSystem.setShaderColor(1F, 1F, 1F, 0.5F);

                for(int k = 0; k < recipe.inputItem.length; k++) {
                    Slot slot = this.menu.slots.get(this.be.modules[i].inputSlots[k]);
                    if(!slot.hasItem()) guiGraphics.renderItem(recipe.inputItem[k].extractForCyclingDisplay(20), this.leftPos + slot.x, this.topPos + slot.y);
                }

                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            }
        }

        for(int i = 0; i < MachineAssemblyFactoryBlockEntity.FIELDS; i++) {
            this.be.inputTanks[i].renderTank(this.leftPos + 105 + fieldX(i), this.topPos + 52 + fieldY(i), 1F, 5, 32);
            this.be.outputTanks[i].renderTank(this.leftPos + 105 + fieldX(i), this.topPos + 70 + fieldY(i), 1F, 5, 16);
        }

        this.be.water.renderTank(this.leftPos + 232, this.topPos + 201, 1F, 7, 52);
        this.be.lps.renderTank(this.leftPos + 241, this.topPos + 201, 1F, 7, 52);
    }
}
