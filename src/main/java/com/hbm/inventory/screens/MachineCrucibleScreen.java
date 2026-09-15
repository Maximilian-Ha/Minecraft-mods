package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineCrucibleBlockEntity;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.inventory.menus.MachineCrucibleMenu;
import com.hbm.inventory.recipes.CrucibleRecipe;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.items.NtmItems;
import com.hbm.main.NuclearTechMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUICrucible.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 *
 * ABWEICHUNG: wie beim Lichtbogenofen zeichnet der Port die Materialsaeulen ueber die regulaere
 * Mischung statt additiv -- GuiGraphics kennt keinen Wechsel der Mischfunktion.
 */
public class MachineCrucibleScreen extends InfoScreen<MachineCrucibleMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_crucible.png");

    private final MachineCrucibleBlockEntity crucible;

    public MachineCrucibleScreen(MachineCrucibleMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.crucible = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 214;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawStackInfo(guiGraphics, this.crucible.wasteStack, mouseX, mouseY, 16, 17);
        this.drawStackInfo(guiGraphics, this.crucible.recipeStack, mouseX, mouseY, 61, 17);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 125, this.topPos + 81, 34, 7, mouseX, mouseY,
                Component.literal(String.format(Locale.US, "%,d", this.crucible.progress) + " / " + String.format(Locale.US, "%,d", MachineCrucibleBlockEntity.processTime) + "TU"));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 125, this.topPos + 90, 34, 7, mouseX, mouseY,
                Component.literal(String.format(Locale.US, "%,d", this.crucible.heat) + " / " + String.format(Locale.US, "%,d", MachineCrucibleBlockEntity.maxHeat) + "TU"));

        if(this.isHovered(mouseX, mouseY, 106, 80, 18, 18)) {
            CrucibleRecipe recipe = CrucibleRecipes.INSTANCE.recipeNameMap.get(this.crucible.recipe);
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

        if(this.isHovered(mouseX, mouseY, 106, 80, 18, 18)) {
            RecipeSelectorScreen.openSelector(CrucibleRecipes.INSTANCE, this.crucible, this.crucible.recipe, 0, null, this);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int pGauge = this.crucible.progress * 33 / MachineCrucibleBlockEntity.processTime;
        if(pGauge > 0) guiGraphics.blit(TEXTURE, this.leftPos + 126, this.topPos + 82, 176, 0, pGauge, 5, 256, 256);

        int hGauge = this.crucible.heat * 33 / MachineCrucibleBlockEntity.maxHeat;
        if(hGauge > 0) guiGraphics.blit(TEXTURE, this.leftPos + 126, this.topPos + 91, 176, 5, hGauge, 5, 256, 256);

        CrucibleRecipe recipe = CrucibleRecipes.INSTANCE.recipeNameMap.get(this.crucible.recipe);
        guiGraphics.renderItem(recipe != null ? recipe.getIcon() : new ItemStack(NtmItems.TEMPLATE_FOLDER.get()), this.leftPos + 107, this.topPos + 81);

        this.drawStack(guiGraphics, this.crucible.recipeStack, MachineCrucibleBlockEntity.recipeCapacity, 62, 97);
        this.drawStack(guiGraphics, this.crucible.wasteStack, MachineCrucibleBlockEntity.wasteCapacity, 17, 97);
    }

    protected void drawStackInfo(GuiGraphics guiGraphics, List<MaterialStack> stacks, int mouseX, int mouseY, int x, int y) {

        List<Component> list = new ArrayList<>();

        if(stacks.isEmpty()) list.add(Component.translatable("desc.gui.arcfurnace.empty").withStyle(ChatFormatting.RED));

        for(MaterialStack stack : stacks) {
            list.add(stack.material.getName()
                    .append(": " + Mats.formatAmount(stack.amount, Screen.hasShiftDown()))
                    .withStyle(ChatFormatting.YELLOW));
        }

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + x, this.topPos + y, 36, 81, mouseX, mouseY, list);
    }

    protected void drawStack(GuiGraphics guiGraphics, List<MaterialStack> stacks, int capacity, int x, int y) {

        if(stacks.isEmpty()) return;

        int lastHeight = 0;
        int lastQuant = 0;

        for(MaterialStack stack : stacks) {

            int targetHeight = (lastQuant + stack.amount) * 79 / capacity;

            if(lastHeight == targetHeight) continue; // Schichten unter einem Pixel entfallen

            // Zuschlagstoffe bekommen einen anderen Ausschnitt der Textur
            int offset = stack.material.smeltable == SmeltingBehavior.ADDITIVE ? 34 : 0;

            Color color = new Color(stack.material.moltenColor);
            guiGraphics.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1F);
            guiGraphics.blit(TEXTURE, this.leftPos + x, this.topPos + y - targetHeight, 176 + offset, 89 - targetHeight, 34, targetHeight - lastHeight, 256, 256);
            guiGraphics.setColor(1F, 1F, 1F, 0.3F);
            guiGraphics.blit(TEXTURE, this.leftPos + x, this.topPos + y - targetHeight, 176 + offset, 89 - targetHeight, 34, targetHeight - lastHeight, 256, 256);
            guiGraphics.setColor(1F, 1F, 1F, 1F);

            lastQuant += stack.amount;
            lastHeight = targetHeight;
        }
    }
}
