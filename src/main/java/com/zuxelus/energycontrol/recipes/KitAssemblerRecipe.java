package com.zuxelus.energycontrol.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zuxelus.energycontrol.init.ECRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.recipes.KitAssemblerRecipe.
 *
 * Ein Rezept der Bausatzmontage: bis zu drei Zutaten mit Anzahl, ein Ergebnis, eine Dauer.
 *
 * Unterschied zum Original: dort standen die drei Zutaten auf drei festen Faechern, und
 * wer sie vertauschte, bekam nichts. Hier zaehlt nur, dass sie in den sechs Eingabefaechern
 * liegen -- wie bei einem formlosen Werkbankrezept. Das ist dieselbe Freiheit, die der
 * Spieler an der Werkbank ohnehin gewohnt ist.
 *
 * Das Original schrieb sich sein Rezeptwesen selbst, samt eigener Fabrik und eigener
 * JSON-Form. Auf 1.21.1 gibt es dafuer die Rezepttypen von Minecraft -- damit liest der
 * Server die Rezepte aus dem Datenpaket, schickt sie an den Client, und Rezeptbrowser wie
 * JEI oder EMI finden sie ohne eigene Anbindung.
 */
public record KitAssemblerRecipe(List<SizedIngredient> inputs, ItemStack result, int time) implements Recipe<KitAssemblerInput> {

    /** Laenger als eine Minute soll kein Bausatz brauchen, kuerzer als ein Tick geht nicht. */
    public static final int DEFAULT_TIME = 200;

    public static final MapCodec<KitAssemblerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SizedIngredient.FLAT_CODEC.listOf().fieldOf("inputs").forGetter(KitAssemblerRecipe::inputs),
            ItemStack.CODEC.fieldOf("result").forGetter(KitAssemblerRecipe::result),
            Codec.INT.optionalFieldOf("time", DEFAULT_TIME).forGetter(KitAssemblerRecipe::time)
    ).apply(instance, KitAssemblerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, KitAssemblerRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), KitAssemblerRecipe::inputs,
            ItemStack.STREAM_CODEC, KitAssemblerRecipe::result,
            ByteBufCodecs.VAR_INT, KitAssemblerRecipe::time,
            KitAssemblerRecipe::new);

    @Override
    public boolean matches(KitAssemblerInput input, Level level) {
        return assign(input) != null;
    }

    /**
     * Ordnet jeder Zutat ein Fach zu, das sie erfuellt, und gibt die Fachnummern zurueck --
     * oder null, wenn das nicht aufgeht. Mit hoechstens drei Zutaten auf sechs Faechern ist
     * das Durchprobieren billig, und es kommt ohne die Annahme aus, dass die erste passende
     * Zuordnung auch die richtige ist.
     */
    public int[] assign(KitAssemblerInput input) {
        int[] slots = new int[inputs.size()];
        boolean[] used = new boolean[input.size()];
        return search(input, 0, slots, used) ? slots : null;
    }

    private boolean search(KitAssemblerInput input, int index, int[] slots, boolean[] used) {
        if(index >= inputs.size()) return true;

        SizedIngredient ingredient = inputs.get(index);
        for(int slot = 0; slot < input.size(); slot++) {
            if(used[slot]) continue;
            if(!ingredient.test(input.getItem(slot))) continue;

            used[slot] = true;
            slots[index] = slot;
            if(search(input, index + 1, slots, used)) return true;
            used[slot] = false;
        }

        return false;
    }

    @Override
    public ItemStack assemble(KitAssemblerInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    /** Fuer Rezeptbrowser: die Zutaten ohne ihre Anzahl. */
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for(SizedIngredient ingredient : inputs) list.add(ingredient.ingredient());
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ECRecipes.KIT_ASSEMBLER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ECRecipes.KIT_ASSEMBLER.get();
    }
}
