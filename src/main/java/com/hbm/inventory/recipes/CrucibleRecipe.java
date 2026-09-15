package com.hbm.inventory.recipes;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.CrucibleRecipe.
 *
 * Ein Tiegelrezept arbeitet nicht mit Gegenstaenden, sondern mit Material: soundsoviel Eisen und
 * Kohlenstoff werden zu Stahl. Die Haeufigkeit sagt, wieviele Ticks zwischen zwei Umsetzungen
 * liegen.
 */
public class CrucibleRecipe extends GenericRecipe {

    public MaterialStack[] input;
    public MaterialStack[] output;
    public int frequency = 1;

    public CrucibleRecipe(String name) {
        super(name);
    }

    public CrucibleRecipe setup(int frequency, ItemStack icon) {
        this.frequency = frequency;
        this.setIcon(icon);
        return this;
    }

    public CrucibleRecipe inputs(MaterialStack... input) { this.input = input; return this; }
    public CrucibleRecipe outputs(MaterialStack... output) { this.output = output; return this; }

    public int getInputAmount() {
        int content = 0;
        for(MaterialStack stack : this.input) content += stack.amount;
        return content;
    }

    @Override
    protected void input(List<Component> list) {
        list.add(Component.literal(I18nUtil.resolveKey("container.recipe.input") + ":").withStyle(ChatFormatting.BOLD));
        for(MaterialStack stack : this.input) this.line(list, stack);
    }

    @Override
    protected void output(List<Component> list) {
        list.add(Component.literal(I18nUtil.resolveKey("container.recipe.output") + ":").withStyle(ChatFormatting.BOLD));
        for(MaterialStack stack : this.output) this.line(list, stack);
    }

    @OnlyIn(Dist.CLIENT)
    private void line(List<Component> list, MaterialStack stack) {
        list.add(Component.literal("  ").append(stack.material.getName())
                .append(": " + Mats.formatAmount(stack.amount, Screen.hasShiftDown())).withStyle(ChatFormatting.GRAY));
    }
}
