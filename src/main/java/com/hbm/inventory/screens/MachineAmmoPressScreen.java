package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineAmmoPressBlockEntity;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.menus.MachineAmmoPressMenu;
import com.hbm.inventory.recipes.AmmoPressRecipes;
import com.hbm.inventory.recipes.AmmoPressRecipes.AmmoPressRecipe;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineAmmoPress.
 *
 * Links die Rezeptliste, rechts das Gitter. Zwoelf Rezepte stehen gleichzeitig da, vier Spalten
 * zu drei; mit dem Mausrad oder den beiden Balken am Rand rollt die Liste SPALTENWEISE weiter --
 * ein Schritt sind drei Rezepte, nicht zwoelf. Darunter ein Suchfeld, das auf den Anzeigenamen
 * des Ergebnisses sieht.
 *
 * DAS GEWAEHLTE REZEPT ZEIGT SICH ALS GEISTERBILD im Gitter: was noch fehlt, steht halb
 * durchsichtig an seinem Platz. So ist abzulesen, was hineingehoert, ohne die Liste zu verlassen.
 */
public class MachineAmmoPressScreen extends InfoScreen<MachineAmmoPressMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_ammo_press.png");

    private final MachineAmmoPressBlockEntity be;

    private final List<AmmoPressRecipe> recipes = new ArrayList<>();
    private EditBox search;
    private int index;
    private int size;
    private int selection;

    public MachineAmmoPressScreen(MachineAmmoPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 200;
        this.selection = this.be.selectedRecipe;

        this.regenerateRecipes();
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96 + 2;

        this.search = new EditBox(this.font, this.leftPos + 10, this.topPos + 75, 66, 12, Component.empty());
        this.search.setTextColor(-1);
        this.search.setTextColorUneditable(-1);
        this.search.setBordered(false);
        this.search.setMaxLength(25);
        this.addWidget(this.search);
    }

    private void regenerateRecipes() {
        this.recipes.clear();
        this.recipes.addAll(AmmoPressRecipes.recipes);
        this.resetPaging();
    }

    private void search(String term) {

        term = term.toLowerCase(Locale.US);
        this.recipes.clear();

        if(term.isEmpty()) {
            this.recipes.addAll(AmmoPressRecipes.recipes);
        } else {
            for(AmmoPressRecipe recipe : AmmoPressRecipes.recipes) {
                if(recipe.output.getHoverName().getString().toLowerCase(Locale.US).contains(term)) this.recipes.add(recipe);
            }
        }

        this.resetPaging();
    }

    private void resetPaging() {
        this.index = 0;
        this.size = Math.max(0, (int) Math.ceil((this.recipes.size() - 12) / 3D));
    }

    /** Die Bildschirmecke des i-ten sichtbaren Rezeptfeldes, spaltenweise von oben nach unten. */
    private int slotX(int visible) { return 16 + 18 * (visible / 3); }
    private int slotY(int visible) { return 17 + 18 * (visible % 3); }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.search.render(guiGraphics, mouseX, mouseY, partialTicks);

        /* Steht die Maus ueber einem belegten Fach, hat dessen Kurzhinweis Vorrang. */
        for(Slot slot : this.menu.slots) {
            if(slot.hasItem() && this.isHovered(mouseX, mouseY, slot.x, slot.y, 16, 16)) {
                this.renderTooltip(guiGraphics, mouseX, mouseY);
                return;
            }
        }

        for(int i = this.index * 3; i < this.index * 3 + 12; i++) {
            if(i >= this.recipes.size()) break;

            int visible = i - this.index * 3;
            if(this.isHovered(mouseX, mouseY, this.slotX(visible), this.slotY(visible), 18, 18)) {
                guiGraphics.renderTooltip(this.font, this.recipes.get(i).output, mouseX, mouseY);
                return;
            }
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {

        if(this.isHovered(mouseX, mouseY, 0, 0, this.imageWidth, this.imageHeight)) {
            if(scrollY > 0 && this.index > 0) { this.index--; return true; }
            if(scrollY < 0 && this.index < this.size) { this.index++; return true; }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        this.search.mouseClicked(mouseX, mouseY, button);

        if(this.isHovered(mouseX, mouseY, 7, 17, 9, 54)) {
            this.click();
            if(this.index > 0) this.index--;
            return true;
        }

        if(this.isHovered(mouseX, mouseY, 88, 17, 9, 54)) {
            this.click();
            if(this.index < this.size) this.index++;
            return true;
        }

        for(int i = this.index * 3; i < this.index * 3 + 12; i++) {
            if(i >= this.recipes.size()) break;

            int visible = i - this.index * 3;
            if(this.isHovered(mouseX, mouseY, this.slotX(visible), this.slotY(visible), 18, 18)) {

                int newSelection = AmmoPressRecipes.recipes.indexOf(this.recipes.get(i));
                this.selection = this.selection != newSelection ? newSelection : -1;

                CompoundTag tag = new CompoundTag();
                tag.putInt("selection", this.selection);
                PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));

                this.click();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if(this.search.charTyped(codePoint, modifiers)) {
            this.search(this.search.getValue());
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(this.search.isFocused() && this.search.keyPressed(keyCode, scanCode, modifiers)) {
            this.search(this.search.getValue());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if(this.isHovered(mouseX, mouseY, 7, 17, 9, 54)) guiGraphics.blit(TEXTURE, this.leftPos + 7, this.topPos + 17, 176, 0, 9, 54);
        if(this.isHovered(mouseX, mouseY, 88, 17, 9, 54)) guiGraphics.blit(TEXTURE, this.leftPos + 88, this.topPos + 17, 185, 0, 9, 54);

        if(this.search != null && this.search.isFocused()) guiGraphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 72, 176, 54, 70, 16);

        for(int i = this.index * 3; i < this.index * 3 + 12; i++) {
            if(i >= this.recipes.size()) break;

            int visible = i - this.index * 3;
            AmmoPressRecipe recipe = this.recipes.get(i);

            int x = this.leftPos + this.slotX(visible);
            int y = this.topPos + this.slotY(visible);

            guiGraphics.renderItem(recipe.output, x + 1, y + 1);

            /* Der Rahmen liegt UEBER dem Bild: gewaehlt hell, sonst dunkel. */
            boolean picked = this.selection == AmmoPressRecipes.recipes.indexOf(recipe);
            guiGraphics.blit(TEXTURE, x, y, picked ? 194 : 212, 0, 18, 18);

            guiGraphics.renderItemDecorations(this.font, recipe.output, x + 1, y + 1, String.valueOf(recipe.output.getCount()));
        }

        /* Geisterbilder dessen, was noch fehlt. */
        if(this.selection >= 0 && this.selection < AmmoPressRecipes.recipes.size()) {

            AmmoPressRecipe recipe = AmmoPressRecipes.recipes.get(this.selection);

            RenderSystem.setShaderColor(1F, 1F, 1F, 0.5F);

            for(int i = 0; i < 9; i++) {
                AStack stack = recipe.input[i];
                if(stack == null) continue;
                if(!this.be.slots.get(i).isEmpty()) continue;

                ItemStack ghost = stack.extractForCyclingDisplay(20);
                if(ghost.isEmpty()) continue;

                int x = this.leftPos + 116 + 18 * (i % 3);
                int y = this.topPos + 18 + 18 * (i / 3);

                guiGraphics.renderItem(ghost, x, y);
                guiGraphics.renderItemDecorations(this.font, ghost, x, y, ghost.getCount() > 1 ? String.valueOf(ghost.getCount()) : null);
            }

            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        }
    }
}
