package com.hbm.inventory.recipes.crafting;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.machine.RBMKRodItem;
import com.hbm.registry.NtmRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.crafting.handlers.RBMKFuelCraftingHandler.
 *
 * Ein abgekuehlter, angebrannter Brennstab zerfaellt im Werkbankraster in acht Pellets. Wie im
 * Original zaehlt nur, dass genau ein Gegenstand im Raster liegt -- die Anordnung ist egal.
 *
 * Die drei Bedingungen des Originals stehen unveraendert: der Stab muss ein Pellet kennen, er
 * muss unter 50 Grad abgekuehlt sein (Huelle wie Kern), und er darf nicht mehr fabrikneu sein.
 */
public class RBMKFuelDisassemblyRecipe extends CustomRecipe {

    public RBMKFuelDisassemblyRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {

        ItemStack stack = getSingleStack(input);
        if(stack == null) return false;
        if(!(stack.getItem() instanceof RBMKRodItem rod) || rod.pellet == null) return false;

        return RBMKRodItem.getHullHeat(stack) < 50 && RBMKRodItem.getCoreHeat(stack) < 50;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {

        ItemStack stack = getSingleStack(input);
        if(stack == null) return ItemStack.EMPTY;
        if(!(stack.getItem() instanceof RBMKRodItem rod) || rod.pellet == null) return ItemStack.EMPTY;

        double enrichment = RBMKRodItem.getEnrichment(stack);
        if(enrichment > 0.99D) return ItemStack.EMPTY;
        if(RBMKRodItem.getHullHeat(stack) >= 50 || RBMKRodItem.getCoreHeat(stack) >= 50) return ItemStack.EMPTY;

        int depletion = 4 - Mth.clamp((int) Math.ceil(enrichment * 5 - 1), 0, 4);
        int meta = depletion + (RBMKRodItem.getPoisonLevel(stack) >= 0.5D ? 5 : 0);

        return MetaHelper.newStack(rod.pellet.get(), 8, meta);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NtmRecipeSerializers.RBMK_FUEL_DISASSEMBLY.get();
    }

    /** Gibt den einzigen Gegenstand im Raster zurueck, oder null, wenn es nicht genau einer ist. */
    private static ItemStack getSingleStack(CraftingInput input) {

        ItemStack found = null;

        for(int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if(stack.isEmpty()) continue;
            if(found != null) return null;
            found = stack;
        }

        return found;
    }
}
