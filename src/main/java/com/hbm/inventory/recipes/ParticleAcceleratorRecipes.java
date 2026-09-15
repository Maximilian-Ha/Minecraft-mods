package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.NtmItems;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.ParticleAcceleratorRecipes.
 *
 * Was der Detektor aus zwei Teilchen macht -- und ab welchem Impuls. Die Zahl ist die eigentliche
 * Ansage: 300 fuer Antimaterie, 70.000 fuer Digamma. Zwischen beidem liegt der ganze Ausbau des
 * Rings, von der Goldspule bis zum Chlorophyt.
 *
 * DIE REIHENFOLGE DER BEIDEN EINGAENGE IST EGAL. Ein Rezept passt auch verdreht -- der Strahl
 * weiss nicht, welches Teilchen er zuerst aufgenommen hat.
 *
 * ZWEI REZEPTE DES ORIGINALS FEHLEN, weil ihre Gegenstaende im Port noch nicht stehen:
 *
 *   Goldstaub + Schrabidatbarren bei 10.000 -> entartete Materie
 *   Haehnchen + Haehnchen bei 100 -> zwei Nuggets
 *
 * Das erste haengt an der Gegenstandsfamilie "item_expensive" des Weltraumbaus, das zweite an
 * einem Scherzgegenstand. Beides kommt mit seinem Teilsystem, nicht vorher.
 */
public class ParticleAcceleratorRecipes extends SerializableRecipe {

    public static final List<ParticleAcceleratorRecipe> recipes = new ArrayList<>();

    @Override
    public void registerDefaults() {

        /* Der billige Einstieg: Wasserstoff auf Kupfer. Dreihundert Impuls schafft ein Ring aus
         * drei Kavitaeten und Goldspulen. */
        make(NtmItems.PARTICLE_HYDROGEN.get(), NtmItems.PARTICLE_COPPER.get(), 300, new ItemStack(NtmItems.PARTICLE_AMAT.get()));
        make(NtmItems.PARTICLE_AMAT.get(), NtmItems.PARTICLE_AMAT.get(), 400, new ItemStack(NtmItems.PARTICLE_ASCHRAB.get()));
        make(NtmItems.PARTICLE_ASCHRAB.get(), NtmItems.PARTICLE_ASCHRAB.get(), 10_000, new ItemStack(NtmItems.PARTICLE_DARK.get()));
        make(NtmItems.PARTICLE_HYDROGEN.get(), NtmItems.PARTICLE_AMAT.get(), 2_500, new ItemStack(NtmItems.PARTICLE_MUON.get()));
        make(NtmItems.PARTICLE_HYDROGEN.get(), NtmItems.PARTICLE_LEAD.get(), 6_500, new ItemStack(NtmItems.PARTICLE_HIGGS.get()));
        make(NtmItems.PARTICLE_MUON.get(), NtmItems.PARTICLE_HIGGS.get(), 5_000, new ItemStack(NtmItems.PARTICLE_TACHYON.get()));
        make(NtmItems.PARTICLE_MUON.get(), NtmItems.PARTICLE_DARK.get(), 12_500, new ItemStack(NtmItems.PARTICLE_STRANGE.get()));

        /* Das einzige Rezept mit zwei Ausgaengen: das Magiepulver faellt als gewoehnlicher Staub
         * wieder heraus. */
        recipes.add(new ParticleAcceleratorRecipe(
                new ComparableStack(NtmItems.PARTICLE_STRANGE.get()),
                new ComparableStack(NtmItems.POWDER_MAGIC.get()),
                12_500,
                new ItemStack(NtmItems.PARTICLE_SPARKTICLE.get()),
                new ItemStack(NtmItems.DUST.get())));

        /* Siebzigtausend. Das geht nur mit Chlorophytspulen, und die verlangen einundfuenfzig
         * Bloecke Kantenlaenge -- der Ring dafuer ist ein Bauwerk. */
        make(NtmItems.PARTICLE_SPARKTICLE.get(), NtmItems.PARTICLE_HIGGS.get(), 70_000, new ItemStack(NtmItems.PARTICLE_DIGAMMA.get()));
    }

    private void make(net.minecraft.world.item.Item in1, net.minecraft.world.item.Item in2, int momentum, ItemStack out) {
        recipes.add(new ParticleAcceleratorRecipe(new ComparableStack(in1), new ComparableStack(in2), momentum, out, null));
    }

    public static ParticleAcceleratorRecipe getOutput(ItemStack input1, ItemStack input2) {

        for(ParticleAcceleratorRecipe recipe : recipes) {
            if(recipe.matchesRecipe(input1, input2)) return recipe;
        }

        return null;
    }

    public static class ParticleAcceleratorRecipe {

        public AStack input1;
        public AStack input2;
        public int momentum;
        public ItemStack output1;
        public ItemStack output2;

        public ParticleAcceleratorRecipe(AStack in1, AStack in2, int momentum, ItemStack out1, ItemStack out2) {
            this.input1 = in1;
            this.input2 = in2;
            this.momentum = momentum;
            this.output1 = out1;
            this.output2 = out2;
        }

        /** Passt in beiden Reihenfolgen -- der Strahl kennt keine erste und keine zweite Haelfte. */
        public boolean matchesRecipe(ItemStack in1, ItemStack in2) {
            return this.input1.matchesRecipe(in1, true) && this.input2.matchesRecipe(in2, true)
                || this.input1.matchesRecipe(in2, true) && this.input2.matchesRecipe(in1, true);
        }
    }

    @Override
    public String getFileName() {
        return "hbmParticleAccelerator.json";
    }

    @Override
    public Object getRecipeObject() {
        return recipes;
    }

    @Override
    public void deleteRecipes() {
        recipes.clear();
    }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = (JsonObject) recipe;
        int momentum = obj.get("momentum").getAsInt();
        AStack[] in = readAStackArray(obj.get("inputs").getAsJsonArray());
        ItemStack[] out = readItemStackArray(obj.get("outputs").getAsJsonArray());

        recipes.add(new ParticleAcceleratorRecipe(
                in[0],
                in.length > 1 ? in[1] : null,
                momentum,
                out[0],
                out.length > 1 ? out[1] : null));
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        ParticleAcceleratorRecipe rec = (ParticleAcceleratorRecipe) recipe;

        writer.name("momentum").value(rec.momentum);

        writer.name("inputs").beginArray();
        writeAStack(rec.input1, writer);
        writeAStack(rec.input2, writer);
        writer.endArray();

        writer.name("outputs").beginArray();
        writeItemStack(rec.output1, writer);
        if(rec.output2 != null) writeItemStack(rec.output2, writer);
        writer.endArray();
    }
}
