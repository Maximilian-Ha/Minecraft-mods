package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.fusion.FusionTorusBlockEntity;
import com.hbm.inventory.menus.FusionTorusMenu;
import com.hbm.inventory.recipes.FusionRecipe;
import com.hbm.inventory.recipes.FusionRecipes;
import com.hbm.items.machine.BlueprintsItem;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.BobMathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIFusionTorus.
 *
 * Links der Strom, daneben die drei Brennstofftanks, rechts das Erzeugnis und die beiden
 * Kuehlmitteltanks. Unten drei Lampen: Strom, Kuehlung, Plasma -- brennen alle drei, laeuft die
 * Anlage.
 */
public class FusionTorusScreen extends InfoScreen<FusionTorusMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_fusion_torus.png");

    private final FusionTorusBlockEntity torus;

    public FusionTorusScreen(FusionTorusMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.torus = menu.be;

        this.imageWidth = 230;
        this.imageHeight = 244;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = 106 - this.font.width(this.title) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 35;
        this.inventoryLabelY = this.imageHeight - 93;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 18, 16, 62, this.torus.power, this.torus.getMaxPower());

        this.torus.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 44, this.topPos + 18, 16, 52);
        this.torus.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 62, this.topPos + 18, 16, 52);
        this.torus.tanks[2].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 80, this.topPos + 18, 16, 52);
        this.torus.tanks[3].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 152, this.topPos + 18, 16, 52);
        this.torus.coolantTanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 188, this.topPos + 46, 16, 52);
        this.torus.coolantTanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 206, this.topPos + 46, 16, 52);

        FusionRecipe recipe = (FusionRecipe) this.torus.fusionModule.getRecipe();

        if(recipe != null) {

            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 43, this.topPos + 115, 18, 18,
                    Component.literal(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET
                            + BobMathUtil.getShortNumber(this.torus.klystronEnergy) + "KyU / "
                            + BobMathUtil.getShortNumber(recipe.ignitionTemp) + "KyU"));

            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 79, this.topPos + 115, 18, 18,
                    Component.literal(ChatFormatting.RED + "<- " + ChatFormatting.RESET
                            + BobMathUtil.getShortNumber(this.torus.plasmaEnergy) + "TU / "
                            + BobMathUtil.getShortNumber(recipe.outputTemp) + "TU"));

            List<Component> lines = new ArrayList<>();
            if(recipe.inputFluid != null) for(int i = 0; i < recipe.inputFluid.length; i++) {
                int consumption = (int) Math.ceil(recipe.inputFluid[i].fill * this.torus.fuelConsumption);
                lines.add(Component.literal(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + consumption + "mB/t ")
                        .append(recipe.inputFluid[i].type.getName()));
            }
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 115, this.topPos + 115, 18, 18, mouseX, mouseY, lines);

        } else {
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 43, this.topPos + 115, 18, 18, Component.literal("0KyU / 0KyU"));
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 79, this.topPos + 115, 18, 18, Component.literal("0TU / 0TU"));
        }

        if(this.isHovered(mouseX, mouseY, 43, 80, 18, 18)) {
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
        if(this.isHovered(mouseX, mouseY, 43, 80, 18, 18)) {
            RecipeSelectorScreen.openSelector(FusionRecipes.INSTANCE, this.torus, this.torus.fusionModule.recipe, 0,
                    BlueprintsItem.grabPool(this.torus.slots.get(FusionTorusBlockEntity.SLOT_BLUEPRINT)), this);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        guiGraphics.drawString(this.font, Component.literal(ChatFormatting.AQUA + "/123K"), 190, 32, 4210752, false);

        int heat = (int) Math.ceil(this.torus.temperature);
        Component label = Component.literal((heat > 123 ? ChatFormatting.RED : ChatFormatting.AQUA) + "" + heat + "K");
        guiGraphics.drawString(this.font, label, 220 - this.font.width(label), 22, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int p = (int) (this.torus.power * 62 / this.torus.getMaxPower());
        guiGraphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 80 - p, 230, 62 - p, 16, p);

        if(this.torus.fusionModule.progress > 0) {
            int j = (int) Math.ceil(70 * this.torus.fusionModule.progress);
            guiGraphics.blit(TEXTURE, this.leftPos + 98, this.topPos + 81, 0, 244, j, 6);
        }

        if(this.torus.fusionModule.bonus > 0) {
            int j = (int) Math.min(Math.ceil(70 * this.torus.fusionModule.bonus), 70);
            guiGraphics.blit(TEXTURE, this.leftPos + 98, this.topPos + 91, 0, 250, j, 6);
        }

        FusionRecipe recipe = (FusionRecipe) this.torus.fusionModule.getRecipe();

        if(recipe != null && this.torus.power >= recipe.power) guiGraphics.blit(TEXTURE, this.leftPos + 160, this.topPos + 115, 246, 14, 8, 8);
        if(Math.ceil(this.torus.temperature) <= 123) guiGraphics.blit(TEXTURE, this.leftPos + 170, this.topPos + 115, 246, 14, 8, 8);
        if(this.torus.didProcess) guiGraphics.blit(TEXTURE, this.leftPos + 180, this.topPos + 115, 246, 14, 8, 8);

        if(this.torus.didProcess) {
            guiGraphics.blit(TEXTURE, this.leftPos + 87, this.topPos + 76, 249, 0, 3, 6);
        } else if(recipe != null) {
            guiGraphics.blit(TEXTURE, this.leftPos + 87, this.topPos + 76, 246, 0, 3, 6);
        }

        guiGraphics.renderItem(recipe != null ? recipe.getIcon() : new net.minecraft.world.item.ItemStack(com.hbm.items.NtmItems.TEMPLATE_FOLDER.get()),
                this.leftPos + 43, this.topPos + 80);

        this.torus.tanks[0].renderTank(this.leftPos + 44, this.topPos + 70, 0F, 16, 52);
        this.torus.tanks[1].renderTank(this.leftPos + 62, this.topPos + 70, 0F, 16, 52);
        this.torus.tanks[2].renderTank(this.leftPos + 80, this.topPos + 70, 0F, 16, 52);
        this.torus.tanks[3].renderTank(this.leftPos + 152, this.topPos + 70, 0F, 16, 52);
        this.torus.coolantTanks[0].renderTank(this.leftPos + 188, this.topPos + 98, 0F, 16, 52);
        this.torus.coolantTanks[1].renderTank(this.leftPos + 206, this.topPos + 98, 0F, 16, 52);
    }
}
