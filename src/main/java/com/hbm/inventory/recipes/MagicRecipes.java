package com.hbm.inventory.recipes;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.TagStack;
import com.hbm.items.NtmItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.MagicRecipes.
 *
 * Die Rezepte des Buchs. Es sind keine Werkbankrezepte: die vier Plaetze haben keine Form,
 * es zaehlt allein, welche vier Gegenstaende darin liegen -- und zwar in der Reihenfolge, in
 * der sie von links oben nach rechts unten gelesen werden. Genau so rechnet auch das Original.
 */
public class MagicRecipes {

    private static final List<MagicRecipe> recipes = new ArrayList<>();

    /** Liest die belegten Plaetze der Reihe nach und sucht das erste passende Rezept. */
    public static ItemStack getRecipe(Container matrix) {

        List<ItemStack> stacks = new ArrayList<>();

        for(int i = 0; i < 4; i++) {
            ItemStack stack = matrix.getItem(i);
            if(!stack.isEmpty()) stacks.add(stack.copyWithCount(1));
        }

        for(MagicRecipe recipe : recipes) {
            if(recipe.matches(stacks)) return recipe.getResult();
        }

        return ItemStack.EMPTY;
    }

    public static List<MagicRecipe> getRecipes() {
        return recipes;
    }

    public static void register() {
        recipes.clear();

        recipes.add(new MagicRecipe(new ItemStack(NtmItems.BALEFIRE_AND_STEEL.get()),
                new TagStack(tag("ingots/steel")),
                new ComparableStack(NtmItems.EGG_BALEFIRE_SHARD.get())));

        recipes.add(new MagicRecipe(new ItemStack(NtmItems.INGOT_ELECTRONIUM.get()),
                new ComparableStack(NtmItems.PELLET_CHARGED.get()),
                new ComparableStack(NtmItems.PELLET_CHARGED.get()),
                new ComparableStack(NtmItems.INGOT_DINEUTRONIUM.get()),
                new ComparableStack(NtmItems.INGOT_DINEUTRONIUM.get())));

        recipes.add(new MagicRecipe(new ItemStack(NtmItems.DIAMOND_GAVEL.get()),
                new ComparableStack(NtmBlocks.GRAVEL_DIAMOND.get()),
                new ComparableStack(NtmBlocks.GRAVEL_DIAMOND.get()),
                new ComparableStack(NtmBlocks.GRAVEL_DIAMOND.get()),
                new ComparableStack(NtmItems.LEAD_GAVEL.get())));
    }

    private static TagKey<Item> tag(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
    }

    public static class MagicRecipe {

        public final List<AStack> in;
        public final ItemStack out;

        public MagicRecipe(ItemStack out, AStack... in) {
            this.out = out;
            this.in = Arrays.asList(in);
        }

        public boolean matches(List<ItemStack> stacks) {

            if(stacks.size() != this.in.size()) return false;

            for(int i = 0; i < this.in.size(); i++) {
                if(!this.in.get(i).matchesRecipe(stacks.get(i), false)) return false;
            }

            return true;
        }

        public ItemStack getResult() {
            return this.out.copy();
        }
    }
}
