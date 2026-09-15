package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.fusion.FusionPlasmaForgeBlockEntity;
import com.hbm.inventory.menus.FusionPlasmaForgeMenu;
import com.hbm.inventory.recipes.PlasmaForgeRecipe;
import com.hbm.inventory.recipes.PlasmaForgeRecipes;
import com.hbm.inventory.screens.element.ScreenElements;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.BlueprintsItem;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.BobMathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachinePlasmaForge.
 *
 * Links der Rezeptwaehler, in der Mitte die zwoelf Eingabefaecher und der Tank, rechts der Strom.
 * Unten zwei Zeiger: der linke zeigt, wie weit die anliegende Plasmaleistung ueber der
 * Zuendtemperatur liegt (Vollausschlag bei anderthalbfacher), der rechte den Rest des Boosters.
 *
 * ABWEICHUNG: das Original blaettert beim Ueberfahren des Boosterfachs durch alle zwanzig
 * moeglichen Isotope. Diese Blaetteranzeige entfaellt -- der Port hat dafuer keine Entsprechung,
 * und der Rezeptwaehler fuehrt dieselbe Liste ohnehin nicht. Welche Isotope gehen, steht im
 * Handbuch.
 */
public class FusionPlasmaForgeScreen extends InfoScreen<FusionPlasmaForgeMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_fusion_plasmaforge.png");

    private final FusionPlasmaForgeBlockEntity forge;

    public FusionPlasmaForgeScreen(FusionPlasmaForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.forge = menu.be;

        this.imageWidth = 176;
        this.imageHeight = 244;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = 70 - this.font.width(this.title) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.forge.inputTank.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 80, this.topPos + 18, 16, 52);
        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 152, this.topPos + 18, 16, 62, this.forge.power, this.forge.maxPower);

        PlasmaForgeRecipe recipe = (PlasmaForgeRecipe) this.forge.plasmaModule.getRecipe();

        if(this.isHovered(mouseX, mouseY, 7, 80, 18, 18)) {
            if(recipe != null) {
                guiGraphics.renderComponentTooltip(this.font, recipe.print(), mouseX, mouseY);
            } else {
                guiGraphics.renderTooltip(this.font, Component.translatable("container.recipe.set_recipe").withStyle(ChatFormatting.YELLOW), mouseX, mouseY);
            }
        }

        Component plasma = recipe != null
                ? Component.literal(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET
                        + BobMathUtil.getShortNumber(this.forge.plasmaEnergySync) + "TU / "
                        + BobMathUtil.getShortNumber(recipe.ignitionTemp) + "TU")
                : Component.literal("0TU / 0TU");

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 25, this.topPos + 115, 18, 18, plasma);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.isHovered(mouseX, mouseY, 7, 80, 18, 18)) {
            RecipeSelectorScreen.openSelector(PlasmaForgeRecipes.INSTANCE, this.forge, this.forge.plasmaModule.recipe, 0,
                    BlueprintsItem.grabPool(this.forge.slots.get(FusionPlasmaForgeBlockEntity.SLOT_BLUEPRINT)), this);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int p = (int) (this.forge.power * 62 / Math.max(this.forge.maxPower, 1L));
        guiGraphics.blit(TEXTURE, this.leftPos + 152, this.topPos + 80 - p, 176, 62 - p, 16, p);

        if(this.forge.plasmaModule.progress > 0) {
            int j = (int) Math.ceil(70D * this.forge.plasmaModule.progress);
            guiGraphics.blit(TEXTURE, this.leftPos + 62, this.topPos + 81, 176, 62, j, 16);
        }

        PlasmaForgeRecipe recipe = (PlasmaForgeRecipe) this.forge.plasmaModule.getRecipe();

        /* Linke Lampe: ein Rezept liegt an. Rechte Lampe: der Strom reicht dafuer. */
        if(this.forge.didProcess) {
            guiGraphics.blit(TEXTURE, this.leftPos + 51, this.topPos + 76, 195, 0, 3, 6);
        } else if(recipe != null) {
            guiGraphics.blit(TEXTURE, this.leftPos + 51, this.topPos + 76, 192, 0, 3, 6);
        }

        if(this.forge.didProcess) {
            guiGraphics.blit(TEXTURE, this.leftPos + 56, this.topPos + 76, 195, 0, 3, 6);
        } else if(recipe != null && this.forge.power >= recipe.power) {
            guiGraphics.blit(TEXTURE, this.leftPos + 56, this.topPos + 76, 192, 0, 3, 6);
        }

        double inputGauge = recipe == null || recipe.ignitionTemp <= 0
                ? 0D
                : Math.min((double) this.forge.plasmaEnergySync / (double) recipe.ignitionTemp, 1.5D) / 1.5D;
        double boosterGauge = this.forge.maxBooster <= 0 ? 0D : (double) this.forge.booster / (double) this.forge.maxBooster;

        ScreenElements.drawSmoothGauge(this.leftPos + 34, this.topPos + 124, (float) inputGauge, 5, 2, 1, 0xFFA00000);
        ScreenElements.drawSmoothGauge(this.leftPos + 70, this.topPos + 124, (float) boosterGauge, 5, 2, 1, 0xFFA00000);

        guiGraphics.renderItem(recipe != null ? recipe.getIcon() : new ItemStack(NtmItems.TEMPLATE_FOLDER.get()), this.leftPos + 8, this.topPos + 81);

        if(recipe != null && recipe.inputItem != null) {
            RenderSystem.setShaderColor(1F, 1F, 1F, 0.5F);
            for(int i = 0; i < Math.min(recipe.inputItem.length, this.forge.plasmaModule.inputSlots.length); i++) {
                Slot slot = this.menu.slots.get(this.forge.plasmaModule.inputSlots[i]);
                if(!slot.hasItem()) {
                    guiGraphics.renderItem(recipe.inputItem[i].extractForCyclingDisplay(20), this.leftPos + slot.x, this.topPos + slot.y);
                }
            }
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        }

        this.forge.inputTank.renderTank(this.leftPos + 80, this.topPos + 70, 1F, 16, 52);
    }
}
