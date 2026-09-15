package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineSuperComputerBlockEntity;
import com.hbm.inventory.menus.MachineSuperComputerMenu;
import com.hbm.inventory.recipes.SuperComputerRecipes;
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
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineSuperComputer.
 *
 * Der Rezeptwaehler sitzt links unten neben dem Blaupausenfach; darunter laufen die beiden
 * Tanks nebeneinander, rechts steht der Stromspeicher.
 */
public class MachineSuperComputerScreen extends InfoScreen<MachineSuperComputerMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_supercomputer.png");

    private final MachineSuperComputerBlockEntity be;

    public MachineSuperComputerScreen(MachineSuperComputerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 211;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.be.inputTank.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 54, 52, 16);
        this.be.outputTank.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 80, this.topPos + 54, 52, 16);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 152, this.topPos + 18, 16, 61, this.be.power, this.be.maxPower);

        if(this.isHovered(mouseX, mouseY, 7, 80, 18, 18)) {
            if(this.be.computerModule.recipe != null && SuperComputerRecipes.INSTANCE.recipeNameMap.containsKey(this.be.computerModule.recipe)) {
                GenericRecipe recipe = SuperComputerRecipes.INSTANCE.recipeNameMap.get(this.be.computerModule.recipe);
                guiGraphics.renderComponentTooltip(this.font, recipe.print(), mouseX, mouseY);
            } else {
                guiGraphics.renderTooltip(this.font, Component.translatable("container.recipe.set_recipe").withStyle(ChatFormatting.YELLOW), mouseX, mouseY);
            }
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.isHovered(mouseX, mouseY, 7, 80, 18, 18)) {
            RecipeSelectorScreen.openSelector(SuperComputerRecipes.INSTANCE, this.be, this.be.computerModule.recipe, 0, BlueprintsItem.grabPool(this.be.slots.get(1)), this);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 70 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int p = (int) (this.be.power * 61 / Math.max(this.be.maxPower, 1L));
        guiGraphics.blit(TEXTURE, this.leftPos + 152, this.topPos + 79 - p, 176, 61 - p, 16, p);

        if(this.be.computerModule.progress > 0) {
            int j = (int) Math.ceil(70D * this.be.computerModule.progress);
            guiGraphics.blit(TEXTURE, this.leftPos + 62, this.topPos + 81, 176, 61, j, 16);
        }

        GenericRecipe recipe = SuperComputerRecipes.INSTANCE.recipeNameMap.get(this.be.computerModule.recipe);

        if(this.be.didProcess) {
            guiGraphics.blit(TEXTURE, this.leftPos + 51, this.topPos + 76, 195, 0, 3, 6);
        } else if(recipe != null) {
            guiGraphics.blit(TEXTURE, this.leftPos + 51, this.topPos + 76, 192, 0, 3, 6);
        }

        if(this.be.didProcess) {
            guiGraphics.blit(TEXTURE, this.leftPos + 56, this.topPos + 76, 195, 0, 3, 6);
        } else if(recipe != null && this.be.power >= recipe.power) {
            guiGraphics.blit(TEXTURE, this.leftPos + 56, this.topPos + 76, 192, 0, 3, 6);
        }

        guiGraphics.renderItem(recipe != null ? recipe.getIcon() : new ItemStack(NtmItems.TEMPLATE_FOLDER.get()), this.leftPos + 8, this.topPos + 81);

        if(recipe != null && recipe.inputItem != null) {
            RenderSystem.setShaderColor(1F, 1F, 1F, 0.5F);
            for(int i = 0; i < recipe.inputItem.length; i++) {
                Slot slot = this.menu.slots.get(this.be.computerModule.inputSlots[i]);
                if(!slot.hasItem()) {
                    guiGraphics.renderItem(recipe.inputItem[i].extractForCyclingDisplay(20), this.leftPos + slot.x, this.topPos + slot.y);
                }
            }
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        }

        this.be.inputTank.renderTank(this.leftPos + 8, this.topPos + 70, 1F, 52, 16, 1);
        this.be.outputTank.renderTank(this.leftPos + 80, this.topPos + 70, 1F, 52, 16, 1);
    }
}
