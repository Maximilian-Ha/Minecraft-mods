package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.MoldItem;
import com.hbm.items.machine.MoldItem.Mold;
import com.hbm.items.machine.ScrapsItem;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.CrucibleRecipes.
 *
 * Die Legierungsrezepte des Tiegels. Eingang und Ausgang sind Material, keine Gegenstaende.
 *
 * ACHTUNG, wie im Original: der Tiegel prueft nicht, ob das Ergebnis mehr Platz braucht als der
 * Eingang. Ein Rezept, bei dem Material "waechst", laesst ihn ueberlaufen -- in der Wirklichkeit
 * tut das beim Legieren ohnehin kaum etwas.
 *
 * NICHT UEBERNOMMEN: die drei Stahlvarianten fuer GregTech 6 (Schmiedeeisen, Roheisen,
 * Meteoreisen). Sie haengen an einer Modabfrage, die der Port nicht hat.
 */
public class CrucibleRecipes extends GenericRecipes<CrucibleRecipe> {

    public static final CrucibleRecipes INSTANCE = new CrucibleRecipes();

    @Override public int inputItemLimit() { return 0; }
    @Override public int inputFluidLimit() { return 0; }
    @Override public int outputItemLimit() { return 0; }
    @Override public int outputFluidLimit() { return 0; }
    @Override public boolean hasDuration() { return false; }
    @Override public boolean hasPower() { return false; }

    @Override public CrucibleRecipe instantiateRecipe(String name) { return new CrucibleRecipe(name); }

    @Override
    public void deleteRecipes() {
        super.deleteRecipes();
        moldRecipes.clear();
    }

    @Override
    public void registerDefaults() {

        int n = MaterialShapes.NUGGET.q(1);
        int i = MaterialShapes.INGOT.q(1);

        this.register(new CrucibleRecipe("crucible.steel").setup(20, new ItemStack(NtmItems.INGOT_STEEL.get()))
                .inputs(new MaterialStack(Mats.MAT_IRON, n * 2), new MaterialStack(Mats.MAT_CARBON, n * 3), new MaterialStack(Mats.MAT_FLUX, n))
                .outputs(new MaterialStack(Mats.MAT_STEEL, n * 2)));

        this.register(new CrucibleRecipe("crucible.hematite").setup(6, new ItemStack(NtmBlocks.RESOURCE_HEMATITE.get()))
                .inputs(new MaterialStack(Mats.MAT_HEMATITE, i * 2), new MaterialStack(Mats.MAT_FLUX, n * 2))
                .outputs(new MaterialStack(Mats.MAT_IRON, i), new MaterialStack(Mats.MAT_SLAG, n * 3)));

        this.register(new CrucibleRecipe("crucible.malachite").setup(6, new ItemStack(NtmBlocks.RESOURCE_MALACHITE.get()))
                .inputs(new MaterialStack(Mats.MAT_MALACHITE, i * 2), new MaterialStack(Mats.MAT_FLUX, n * 2))
                .outputs(new MaterialStack(Mats.MAT_COPPER, i), new MaterialStack(Mats.MAT_SLAG, n * 3)));

        this.register(new CrucibleRecipe("crucible.redcopper").setup(2, new ItemStack(NtmItems.INGOT_RED_COPPER.get()))
                .inputs(new MaterialStack(Mats.MAT_COPPER, n), new MaterialStack(Mats.MAT_REDSTONE, n))
                .outputs(new MaterialStack(Mats.MAT_MINGRADE, n * 2)));

        this.register(new CrucibleRecipe("crucible.hss").setup(9, new ItemStack(NtmItems.INGOT_DURA_STEEL.get()))
                .inputs(new MaterialStack(Mats.MAT_STEEL, n * 5), new MaterialStack(Mats.MAT_TUNGSTEN, n * 3), new MaterialStack(Mats.MAT_COBALT, n))
                .outputs(new MaterialStack(Mats.MAT_DURA, n * 9)));

        this.register(new CrucibleRecipe("crucible.ferro").setup(3, new ItemStack(NtmItems.INGOT_FERROURANIUM.get()))
                .inputs(new MaterialStack(Mats.MAT_STEEL, n * 2), new MaterialStack(Mats.MAT_U238, n))
                .outputs(new MaterialStack(Mats.MAT_FERRO, n * 3)));

        this.register(new CrucibleRecipe("crucible.tcalloy").setup(9, new ItemStack(NtmItems.INGOT_TCALLOY.get()))
                .inputs(new MaterialStack(Mats.MAT_STEEL, n * 8), new MaterialStack(Mats.MAT_TECHNETIUM, n))
                .outputs(new MaterialStack(Mats.MAT_TCALLOY, i)));

        this.register(new CrucibleRecipe("crucible.cdalloy").setup(9, new ItemStack(NtmItems.INGOT_CDALLOY.get()))
                .inputs(new MaterialStack(Mats.MAT_STEEL, n * 8), new MaterialStack(Mats.MAT_CADMIUM, n))
                .outputs(new MaterialStack(Mats.MAT_CDALLOY, i)));

        this.register(new CrucibleRecipe("crucible.bbronze").setup(9, new ItemStack(NtmItems.INGOT_BISMUTH_BRONZE.get()))
                .inputs(new MaterialStack(Mats.MAT_COPPER, n * 8), new MaterialStack(Mats.MAT_BISMUTH, n), new MaterialStack(Mats.MAT_FLUX, n * 3))
                .outputs(new MaterialStack(Mats.MAT_BBRONZE, i), new MaterialStack(Mats.MAT_SLAG, n * 3)));

        this.register(new CrucibleRecipe("crucible.abronze").setup(9, new ItemStack(NtmItems.INGOT_ARSENIC_BRONZE.get()))
                .inputs(new MaterialStack(Mats.MAT_COPPER, n * 8), new MaterialStack(Mats.MAT_ARSENIC, n), new MaterialStack(Mats.MAT_FLUX, n * 3))
                .outputs(new MaterialStack(Mats.MAT_ABRONZE, i), new MaterialStack(Mats.MAT_SLAG, n * 3)));

        this.register(new CrucibleRecipe("crucible.cmb").setup(3, new ItemStack(NtmItems.INGOT_COMBINE_STEEL.get()))
                .inputs(new MaterialStack(Mats.MAT_MAGTUNG, n * 6), new MaterialStack(Mats.MAT_MUD, n * 3))
                .outputs(new MaterialStack(Mats.MAT_CMB, i)));

        this.register(new CrucibleRecipe("crucible.magtung").setup(3, new ItemStack(NtmItems.INGOT_MAGNETIZED_TUNGSTEN.get()))
                .inputs(new MaterialStack(Mats.MAT_TUNGSTEN, i), new MaterialStack(Mats.MAT_SCHRABIDIUM, n))
                .outputs(new MaterialStack(Mats.MAT_MAGTUNG, i)));

        this.register(new CrucibleRecipe("crucible.bscco").setup(3, new ItemStack(NtmItems.INGOT_BSCCO.get()))
                .inputs(new MaterialStack(Mats.MAT_BISMUTH, n * 2), new MaterialStack(Mats.MAT_STRONTIUM, n * 2), new MaterialStack(Mats.MAT_CALCIUM, n * 2), new MaterialStack(Mats.MAT_COPPER, n * 3))
                .outputs(new MaterialStack(Mats.MAT_BSCCO, i)));
    }

    @Override public String getFileName() { return "hbmCrucible.json"; }

    @Override
    public String getComment() {
        return "ID must be unique, but not sequential. Order in which the recipes are defined determines the order in which they are displayed in-game. "
                + "Frequency is the amount of ticks between operations, must be at least 1. The names are unlocalized by default, but if they can't be found in "
                + "the lang files the names will be displayed as-is. The icon is what's being displayed when holding shift on the template. "
                + "Amounts are in quanta (1 quantum is 1/72 of an ingot). Material names are the ones listed in Mats, case-sensitive.";
    }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = (JsonObject) recipe;
        String name = obj.get("name").getAsString();
        int freq = obj.get("frequency").getAsInt();
        ItemStack icon = readItemStack(obj.get("icon").getAsJsonArray());

        MaterialStack[] input = readMaterials(obj.get("input").getAsJsonArray());
        MaterialStack[] output = readMaterials(obj.get("output").getAsJsonArray());

        if(input.length == 0 || output.length == 0) return;

        this.register(new CrucibleRecipe(name).setup(freq, icon).inputs(input).outputs(output));
    }

    private static MaterialStack[] readMaterials(JsonArray array) {

        List<MaterialStack> mats = new ArrayList<>();

        for(int i = 0; i < array.size(); i++) {
            JsonArray entry = array.get(i).getAsJsonArray();
            NTMMaterial mat = Mats.matByName.get(entry.get(0).getAsString());
            if(mat == null) continue;
            mats.add(new MaterialStack(mat, entry.get(1).getAsInt()));
        }

        return mats.toArray(new MaterialStack[0]);
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        CrucibleRecipe rec = (CrucibleRecipe) recipe;

        writer.name("name").value(rec.getInternalName());
        writer.name("frequency").value(rec.frequency);
        writer.name("icon");
        writeItemStack(rec.getIcon(), writer);

        writeMaterials(writer, "input", rec.input);
        writeMaterials(writer, "output", rec.output);
    }

    private static void writeMaterials(JsonWriter writer, String name, MaterialStack[] mats) throws IOException {

        writer.name(name).beginArray();
        writer.setIndent("");

        for(MaterialStack mat : mats) {
            writer.beginArray();
            writer.value(mat.material.names[0]).value(mat.amount);
            writer.endArray();
        }

        writer.endArray();
        writer.setIndent("  ");
    }

    /**
     * Welche Form aus welchem Material was macht -- gebraucht wird das fuer die Rezeptansicht.
     * Jeder Eintrag: fluessiges Material, Form, der Block, in den sie gehoert, und das Ergebnis.
     */
    private static final List<ItemStack[]> moldRecipes = new ArrayList<>();

    /**
     * Die Liste entsteht beim ersten Abruf, nicht beim Registrieren der Rezepte: die Formen
     * finden ihr Ergebnis ueber Tags, und die sind zum Registrierzeitpunkt noch nicht gebunden.
     */
    public static List<ItemStack[]> getMoldRecipes() {
        if(moldRecipes.isEmpty()) registerMoldsForDisplay();
        return moldRecipes;
    }

    private static void registerMoldsForDisplay() {

        MoldItem.registerMolds();

        for(NTMMaterial material : Mats.orderedList) {

            if(material.smeltable != SmeltingBehavior.SMELTABLE) continue;

            for(Mold mold : MoldItem.molds) {

                ItemStack out = mold.getOutput(null, material);
                if(out.isEmpty()) continue;

                moldRecipes.add(new ItemStack[] {
                        ScrapsItem.create(new MaterialStack(material, mold.getCost()), true),
                        MoldItem.create(mold),
                        new ItemStack(mold.size == 0 ? NtmBlocks.FOUNDRY_MOLD.get() : NtmBlocks.FOUNDRY_BASIN.get()),
                        out });
            }
        }
    }
}
