package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.NtmItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.CyclotronRecipes.
 *
 * Das Zyklotron schiesst ein Geschoss auf ein Pulver und macht daraus ein anderes Element. Was
 * herauskommt, haengt an BEIDEM: Lithium auf Eisen ergibt Kobalt, Kupfer auf Eisen ergibt Niob.
 * Ein Rezept ist deshalb immer ein Paar.
 *
 * DIE FUENF GESCHOSSE SIND FUENF WEGE. Lithium ist der breite -- dreizehn Rezepte, billig, fuer
 * alles Leichte. Beryllium und Kohlenstoff fuellen die Luecken. Kupfer ist der Weg zu den
 * schweren Nichtmetallen. Plutonium ist der teure: er kostet das Zwanzigfache an Antimaterie und
 * fuehrt als einziger zu Tennessin, Australium und Schrabidium.
 *
 * JEDER SCHUSS ERZEUGT ANTIMATERIE, und das ist mehr als Beiwerk: die Menge steht im Rezept und
 * ist der eigentliche Grund, warum man das Zyklotron am Ende laufen laesst. Schrabidium aus
 * einer geladenen Kugel bringt tausend Millibar auf einen Schlag.
 *
 * ABWEICHUNG, UND ES IST DIE GROESSTE DIESER RUNDE: das Original nennt seine Eingaenge zum
 * grossen Teil ueber das Erzwoerterbuch -- "dustLithium", "dustIron". Ein Erzwoerterbuch gibt es
 * auf 1.21 nicht; an seine Stelle treten hier die Pulver des Mods selbst. Fuer den Spieler
 * aendert das nichts, solange kein anderer Mod Staeube beisteuert.
 *
 * ABWEICHUNG: "dustPhosphorus" traegt im Original KEIN Gegenstand des Mods -- es ist ein
 * Eintrag, den andere Mods fuellen sollen, und ohne sie laufen die beiden Rezepte ins Leere.
 * Hier steht dafuer der Phosphorbarren, den der Port aus der Zentrifuge kennt; damit arbeiten
 * die beiden Rezepte, statt tot dazustehen.
 *
 * ABWEICHUNG: das Original kennt einen Quecksilberbarren, der Port nur das Nugget. Beide
 * Stellen -- Ausgabe und spaeterer Eingang -- nennen deshalb das Nugget, und die Kette bleibt
 * geschlossen.
 *
 * ABWEICHUNG IN DER SUCHE: das Original geht bei jeder Anfrage die ganze Liste durch und
 * schreibt selbst dazu, dass ihm das bewusst ist. Hier sind die Rezepte nach dem Geschoss
 * vorsortiert -- die Suche sieht nur noch die Zeilen EINES Geschosses. Das aendert nichts am
 * Ergebnis; canProcess fragt aber drei Mal je Tick, und das Zyklotron ist eine Maschine, die man
 * stehen laesst.
 */
public class CyclotronRecipes extends SerializableRecipe {

    public static final Map<Pair, Result> recipes = new LinkedHashMap<>();

    /** Nach Geschoss vorsortiert; wird aus recipes abgeleitet und nie von Hand gefuellt. */
    private static final Map<ComparableStack, List<Entry<Pair, Result>>> byParticle = new HashMap<>();

    /** Geschoss und Ziel. Das Geschoss ist immer ein bestimmter Gegenstand, das Ziel darf ein Tag sein. */
    public record Pair(ComparableStack particle, AStack ingredient) { }

    /** Was herauskommt und wieviel Antimaterie dabei anfaellt. */
    public record Result(ItemStack output, int antimatter) { }

    @Override
    public void registerDefaults() {

        /* --- Lithium: der breite Weg --- */
        int liA = 50;

        make(NtmItems.PART_LITHIUM, NtmItems.POWDER_LITHIUM, NtmItems.POWDER_BERYLLIUM, liA);
        make(NtmItems.PART_LITHIUM, NtmItems.POWDER_BERYLLIUM, NtmItems.POWDER_BORON, liA);
        make(NtmItems.PART_LITHIUM, NtmItems.POWDER_BORON, NtmItems.POWDER_COAL, liA);
        make(NtmItems.PART_LITHIUM, NtmItems.POWDER_QUARTZ, NtmItems.POWDER_FIRE, liA);
        make(NtmItems.PART_LITHIUM, NtmItems.INGOT_PHOSPHORUS, NtmItems.SULFUR, liA);
        make(NtmItems.PART_LITHIUM, NtmItems.POWDER_IRON, NtmItems.POWDER_COBALT, liA);
        make(NtmItems.PART_LITHIUM, NtmItems.POWDER_STRONTIUM, NtmItems.POWDER_ZIRCONIUM, liA);
        make(NtmItems.PART_LITHIUM, NtmItems.POWDER_GOLD, NtmItems.NUGGET_MERCURY, liA);
        make(NtmItems.PART_LITHIUM, NtmItems.POWDER_POLONIUM, NtmItems.POWDER_ASTATINE, liA);
        make(NtmItems.PART_LITHIUM, NtmItems.POWDER_LANTHANIUM, NtmItems.POWDER_CERIUM, liA);
        make(NtmItems.PART_LITHIUM, NtmItems.POWDER_ACTINIUM, NtmItems.POWDER_THORIUM, liA);
        make(NtmItems.PART_LITHIUM, NtmItems.POWDER_URANIUM, NtmItems.POWDER_NEPTUNIUM, liA);
        make(NtmItems.PART_LITHIUM, NtmItems.POWDER_NEPTUNIUM, NtmItems.POWDER_PLUTONIUM, liA);

        /* --- Beryllium: die Luecken --- */
        int beA = 25;

        make(NtmItems.PART_BERYLLIUM, NtmItems.POWDER_LITHIUM, NtmItems.POWDER_BORON, beA);
        make(NtmItems.PART_BERYLLIUM, NtmItems.POWDER_QUARTZ, NtmItems.SULFUR, beA);
        make(NtmItems.PART_BERYLLIUM, NtmItems.POWDER_TITANIUM, NtmItems.POWDER_IRON, beA);
        make(NtmItems.PART_BERYLLIUM, NtmItems.POWDER_COBALT, NtmItems.POWDER_COPPER, beA);
        make(NtmItems.PART_BERYLLIUM, NtmItems.POWDER_STRONTIUM, NtmItems.POWDER_NIOBIUM, beA);
        make(NtmItems.PART_BERYLLIUM, NtmItems.POWDER_CERIUM, NtmItems.POWDER_NEODYMIUM, beA);
        make(NtmItems.PART_BERYLLIUM, NtmItems.POWDER_THORIUM, NtmItems.POWDER_URANIUM, beA);

        /* --- Kohlenstoff: der billige --- */
        int caA = 10;

        make(NtmItems.PART_CARBON, NtmItems.POWDER_BORON, NtmItems.POWDER_ALUMINIUM, caA);
        make(NtmItems.PART_CARBON, NtmItems.SULFUR, NtmItems.POWDER_TITANIUM, caA);
        make(NtmItems.PART_CARBON, NtmItems.POWDER_TITANIUM, NtmItems.POWDER_COBALT, caA);
        make(NtmItems.PART_CARBON, NtmItems.POWDER_CAESIUM, NtmItems.POWDER_LANTHANIUM, caA);
        make(NtmItems.PART_CARBON, NtmItems.POWDER_NEODYMIUM, NtmItems.POWDER_GOLD, caA);
        make(NtmItems.PART_CARBON, NtmItems.NUGGET_MERCURY, NtmItems.POWDER_POLONIUM, caA);
        make(NtmItems.PART_CARBON, NtmItems.POWDER_LEAD, NtmItems.POWDER_RA226, caA);
        make(NtmItems.PART_CARBON, NtmItems.POWDER_ASTATINE, NtmItems.POWDER_ACTINIUM, caA);

        /* --- Kupfer: zu den schweren Nichtmetallen --- */
        int coA = 15;

        make(NtmItems.PART_COPPER, NtmItems.POWDER_BERYLLIUM, NtmItems.POWDER_QUARTZ, coA);
        make(NtmItems.PART_COPPER, NtmItems.POWDER_COAL, NtmItems.POWDER_BROMINE, coA);
        make(NtmItems.PART_COPPER, NtmItems.POWDER_TITANIUM, NtmItems.POWDER_STRONTIUM, coA);
        make(NtmItems.PART_COPPER, NtmItems.POWDER_IRON, NtmItems.POWDER_NIOBIUM, coA);
        make(NtmItems.PART_COPPER, NtmItems.POWDER_BROMINE, NtmItems.POWDER_IODINE, coA);
        make(NtmItems.PART_COPPER, NtmItems.POWDER_STRONTIUM, NtmItems.POWDER_NEODYMIUM, coA);
        make(NtmItems.PART_COPPER, NtmItems.POWDER_NIOBIUM, NtmItems.POWDER_CAESIUM, coA);
        make(NtmItems.PART_COPPER, NtmItems.POWDER_IODINE, NtmItems.POWDER_POLONIUM, coA);
        make(NtmItems.PART_COPPER, NtmItems.POWDER_CAESIUM, NtmItems.POWDER_ACTINIUM, coA);
        make(NtmItems.PART_COPPER, NtmItems.POWDER_GOLD, NtmItems.POWDER_URANIUM, coA);

        /* --- Plutonium: der teure --- */
        int plA = 100;

        make(NtmItems.PART_PLUTONIUM, NtmItems.INGOT_PHOSPHORUS, NtmItems.POWDER_TENNESSINE, plA);
        make(NtmItems.PART_PLUTONIUM, NtmItems.POWDER_PLUTONIUM, NtmItems.POWDER_TENNESSINE, plA);
        make(NtmItems.PART_PLUTONIUM, NtmItems.POWDER_TENNESSINE, NtmItems.POWDER_AUSTRALIUM, plA);
        make(NtmItems.PART_PLUTONIUM, NtmItems.PELLET_CHARGED, NtmItems.NUGGET_SCHRABIDIUM, 1_000);
    }

    private static void make(ItemLike particle, ItemLike ingredient, ItemLike output, int antimatter) {
        register(new ComparableStack(new ItemStack(particle)), new ComparableStack(new ItemStack(ingredient)),
                new ItemStack(output), antimatter);
    }

    public static void register(ComparableStack particle, AStack ingredient, ItemStack output, int antimatter) {
        recipes.put(new Pair(particle, ingredient), new Result(output, antimatter));
        byParticle.clear();
    }

    private static List<Entry<Pair, Result>> forParticle(ComparableStack particle) {

        if(byParticle.isEmpty() && !recipes.isEmpty()) {
            for(Entry<Pair, Result> entry : recipes.entrySet()) {
                byParticle.computeIfAbsent(entry.getKey().particle(), key -> new ArrayList<>()).add(entry);
            }
        }

        return byParticle.getOrDefault(particle, List.of());
    }

    /**
     * Das Ergebnis fuer Geschoss und Ziel, oder null. Die Reihenfolge der Argumente folgt der
     * Maschine: erst was beschossen wird, dann womit.
     */
    public static @Nullable Result getOutput(ItemStack ingredient, ItemStack particle) {

        if(ingredient.isEmpty() || particle.isEmpty()) return null;

        for(Entry<Pair, Result> entry : forParticle(new ComparableStack(particle).makeSingular())) {
            if(entry.getKey().ingredient().matchesRecipe(ingredient, true)) {
                return new Result(entry.getValue().output().copy(), entry.getValue().antimatter());
            }
        }

        return null;
    }

    /** Ob dieser Gegenstand ueberhaupt als Geschoss taugt; die Oberflaeche fragt danach. */
    public static boolean isParticle(ItemStack stack) {
        if(stack.isEmpty()) return false;
        return !forParticle(new ComparableStack(stack).makeSingular()).isEmpty();
    }

    /** Ob dieser Gegenstand in irgendeinem Rezept beschossen wird; die Faecher fragen danach. */
    public static boolean isIngredient(ItemStack stack) {
        if(stack.isEmpty()) return false;
        for(Pair key : recipes.keySet()) if(key.ingredient().matchesRecipe(stack, true)) return true;
        return false;
    }

    /** Alle Geschosse, in der Reihenfolge ihrer ersten Nennung. */
    public static List<Item> getParticles() {
        List<Item> list = new ArrayList<>();
        for(Pair key : recipes.keySet()) {
            Item item = key.particle().toStack().getItem();
            if(!list.contains(item)) list.add(item);
        }
        return list;
    }

    @Override public String getFileName() { return "hbmCyclotron.json"; }
    @Override public Object getRecipeObject() { return recipes; }

    @Override
    public void deleteRecipes() {
        recipes.clear();
        byParticle.clear();
    }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = recipe.getAsJsonObject();

        ItemStack particle = readItemStack(obj.get("particle").getAsJsonArray());
        AStack ingredient = readAStack(obj.get("input").getAsJsonArray());
        ItemStack output = readItemStack(obj.get("output").getAsJsonArray());
        int antimatter = obj.get("antimatter").getAsInt();

        register(new ComparableStack(particle), ingredient, output, antimatter);
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        @SuppressWarnings("unchecked")
        Entry<Pair, Result> entry = (Entry<Pair, Result>) recipe;

        writer.name("particle");
        writeItemStack(entry.getKey().particle().toStack(), writer);
        writer.name("input");
        writeAStack(entry.getKey().ingredient(), writer);
        writer.name("output");
        writeItemStack(entry.getValue().output(), writer);
        writer.name("antimatter").value(entry.getValue().antimatter());
    }

    @Override
    public String getComment() {
        return "The particle item, while being an input, has to be defined as an item stack without tag support.";
    }
}
