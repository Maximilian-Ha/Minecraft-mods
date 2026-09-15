package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.TagStack;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.ScrapsItem;
import com.hbm.util.Tuple.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.ArcFurnaceRecipes.
 *
 * Der Lichtbogenofen kennt zwei Betriebsarten. Fest: ein Gegenstand wird zu einem anderen, wie
 * im Schmelzofen, nur schneller. Fluessig: der Gegenstand zerfaellt in seine Materialien, die
 * sich im Ofen sammeln und spaeter ausgegossen werden.
 *
 * ABWEICHUNGEN gegenueber dem Original, alle drei kommen daher, dass 1.21 zum Zeitpunkt der
 * Rezeptregistrierung (FMLCommonSetup) weder gebundene Tags noch geladene Rezepte kennt:
 *
 * 1. Die Autogenerierung laeuft nicht mehr beim Registrieren ueber alle Form-und-Material-
 *    Kombinationen, sondern beim Nachschlagen ueber Mats.getSmeltingMaterialsFromItem. Das
 *    Ergebnis ist dasselbe -- dieselbe Quelle, dieselben Umrechnungsfaktoren --, nur ohne
 *    zweitausend Leerrezepte in der Liste und in der Vorlagendatei.
 * 2. Dasselbe gilt fuer die Schmelzofenrezepte: statt sie einmal zu kopieren, fragt der Port
 *    beim Nachschlagen den Rezeptverwalter. Deshalb nimmt getOutput hier eine Welt entgegen.
 * 3. Die Kollisionspruefung des Originals (occupiedSolid/occupiedLiquid) greift nur bei
 *    Gegenstandsrezepten. Bei Tag-Rezepten ist zum Registrierzeitpunkt noch nicht bekannt, was
 *    im Tag steckt; dort entscheidet wie eh und je die Reihenfolge -- der erste Treffer gilt.
 */
public class ArcFurnaceRecipes extends SerializableRecipe {

    public static List<Pair<AStack, ArcFurnaceRecipe>> recipeList = new ArrayList<>();
    /* Schnellzugriff, wird beim ersten Nachschlagen eines Gegenstands angelegt */
    public static HashMap<ComparableStack, ArcFurnaceRecipe> fastCacheSolid = new HashMap<>();
    public static HashMap<ComparableStack, ArcFurnaceRecipe> fastCacheLiquid = new HashMap<>();
    /* merkt sich beim Registrieren, welche Eingaben schon vergeben sind */
    public static HashSet<ComparableStack> occupiedSolid = new HashSet<>();
    public static HashSet<ComparableStack> occupiedLiquid = new HashSet<>();

    private static TagKey<Item> tag(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
    }

    @Override
    public void registerDefaults() {

        register(new TagStack(tag("sands")),            new ArcFurnaceRecipe().solid(new ItemStack(NtmItems.NUGGET_SILICON.get()))      .fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(1))));
        register(new ComparableStack(Items.FLINT),          new ArcFurnaceRecipe().solid(new ItemStack(NtmItems.NUGGET_SILICON.get(), 4))   .fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
        register(new TagStack(tag("gems/quartz")),      new ArcFurnaceRecipe().solid(new ItemStack(NtmItems.NUGGET_SILICON.get(), 3))   .fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(3))));
        register(new TagStack(tag("dusts/quartz")),         new ArcFurnaceRecipe().solid(new ItemStack(NtmItems.NUGGET_SILICON.get(), 3))   .fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(3))));
        register(new TagStack(tag("storage_blocks/quartz")),new ArcFurnaceRecipe().solid(new ItemStack(NtmItems.NUGGET_SILICON.get(), 12))  .fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(12))));

        // Glasfaser und Asbest: im Original OreDictionary-Eintraege, im Port die Gegenstaende selbst
        register(new ComparableStack(NtmItems.INGOT_FIBERGLASS.get()),  new ArcFurnaceRecipe().solid(new ItemStack(NtmItems.NUGGET_SILICON.get(), 4))   .fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
        register(new ComparableStack(NtmBlocks.BLOCK_FIBERGLASS.get()), new ArcFurnaceRecipe().solid(new ItemStack(NtmItems.NUGGET_SILICON.get(), 40))  .fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(9, 2))));
        register(new ComparableStack(NtmItems.INGOT_ASBESTOS.get()),    new ArcFurnaceRecipe().solid(new ItemStack(NtmItems.NUGGET_SILICON.get(), 4))   .fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
        register(new ComparableStack(NtmItems.POWDER_ASBESTOS.get()),   new ArcFurnaceRecipe().solid(new ItemStack(NtmItems.NUGGET_SILICON.get(), 4))   .fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
        register(new ComparableStack(NtmBlocks.BLOCK_ASBESTOS.get()),   new ArcFurnaceRecipe().solid(new ItemStack(NtmItems.NUGGET_SILICON.get(), 40))  .fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(9, 2))));

        // Quarzsand: im Original der Untertyp QUARTZ des Mischsands, im Port ein eigener Block
        register(new ComparableStack(NtmBlocks.SAND_QUARTZ.get()),      new ArcFurnaceRecipe().solid(new ItemStack(NtmBlocks.GLASS_QUARTZ.get())));
        register(new ComparableStack(NtmItems.POWDER_BORAX.get()),      new ArcFurnaceRecipe().solid(new ItemStack(NtmItems.POWDER_BORON_TINY.get(), 3)).fluid(new MaterialStack(Mats.MAT_BORON, MaterialShapes.NUGGET.q(3))));

        // Nicht uebernommen, weil es die Gegenstaende im Port noch nicht gibt: die Grundgesteinserze
        // (ItemBedrockOreNew) mit ihren Roest-, Saeure- und Loesungsmittelstufen.
    }

    public static void register(AStack input, ArcFurnaceRecipe output) {

        List<ItemStack> inputs = input.extractForJEI();

        for(ItemStack stack : inputs) {
            ComparableStack compStack = new ComparableStack(stack).makeSingular();
            if(output.solidOutput != null && occupiedSolid.contains(compStack)) return;
            if(output.fluidOutput != null && occupiedLiquid.contains(compStack)) return;
        }

        recipeList.add(new Pair<>(input, output));

        for(ItemStack stack : inputs) {
            ComparableStack compStack = new ComparableStack(stack).makeSingular();
            if(output.solidOutput != null) occupiedSolid.add(compStack);
            if(output.fluidOutput != null) occupiedLiquid.add(compStack);
        }
    }

    /**
     * Was der Ofen aus diesem Gegenstand macht. Die Welt wird nur fuer den Rueckgriff auf die
     * Schmelzofenrezepte gebraucht und darf null sein.
     */
    public static @Nullable ArcFurnaceRecipe getOutput(@Nullable Level level, ItemStack stack, boolean liquid) {

        if(stack.isEmpty()) return null;

        // Schrott ist schon Material und geht ohne Umweg zurueck in die Schmelze
        if(stack.getItem() instanceof ScrapsItem && liquid) {
            MaterialStack mats = ScrapsItem.getMats(stack);
            if(mats != null && mats.material.smeltable == SmeltingBehavior.SMELTABLE) {
                return new ArcFurnaceRecipe().fluid(mats);
            }
            return null;
        }

        ComparableStack cacheKey = new ComparableStack(stack).makeSingular();
        HashMap<ComparableStack, ArcFurnaceRecipe> cache = liquid ? fastCacheLiquid : fastCacheSolid;
        if(cache.containsKey(cacheKey)) return cache.get(cacheKey);

        ArcFurnaceRecipe found = null;

        for(Pair<AStack, ArcFurnaceRecipe> entry : recipeList) {
            if(!entry.getKey().matchesRecipe(stack, true)) continue;

            ArcFurnaceRecipe rec = entry.getValue();
            if((liquid && rec.fluidOutput != null) || (!liquid && rec.solidOutput != null)) {
                found = rec;
                break;
            }
        }

        if(found == null) {
            if(liquid) {
                found = autogenLiquid(stack);
            } else if(level != null) {
                found = autogenSolid(level, stack);
            } else {
                // Ohne Welt laesst sich der Rueckgriff auf die Schmelzofenrezepte nicht
                // beantworten. Dann lieber gar nichts merken als ein falsches Nein.
                return null;
            }
        }

        cache.put(cacheKey, found);
        return found;
    }

    /** Ersetzt die Autogenerierung des Originals: was der Gegenstand hergibt, wird eingeschmolzen. */
    private static @Nullable ArcFurnaceRecipe autogenLiquid(ItemStack stack) {

        List<MaterialStack> smeltables = new ArrayList<>();

        for(MaterialStack mat : Mats.getSmeltingMaterialsFromItem(stack)) {
            if(mat.material.smeltable == SmeltingBehavior.SMELTABLE) smeltables.add(mat);
        }

        if(smeltables.isEmpty()) return null;

        return new ArcFurnaceRecipe().fluid(smeltables.toArray(new MaterialStack[0]));
    }

    /**
     * Ersetzt registerFurnaceSmeltables: der Ofen uebernimmt das Schmelzofenrezept, aber nur
     * fuer Metallisches -- genau wie im Original, wo die Menge arcSmeltable alles enthaelt, was
     * unter ingot, ore, plate oder block im OreDictionary steht.
     */
    private static @Nullable ArcFurnaceRecipe autogenSolid(@Nullable Level level, ItemStack stack) {

        if(level == null) return null;

        ItemStack result = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level)
                .map(RecipeHolder::value)
                .map(recipe -> recipe.getResultItem(level.registryAccess()))
                .orElse(ItemStack.EMPTY);

        if(result.isEmpty()) return null;
        if(!isArcSmeltable(stack) && !isArcSmeltable(result)) return null;

        return new ArcFurnaceRecipe().solid(result.copy());
    }

    private static boolean isArcSmeltable(ItemStack stack) {
        for(TagKey<Item> tag : stack.getTags().toList()) {
            if(!"c".equals(tag.location().getNamespace())) continue;

            String path = tag.location().getPath();
            if(path.startsWith("ingots") || path.startsWith("ores") || path.startsWith("plates")
                    || path.startsWith("storage_blocks") || path.startsWith("raw_materials")) return true;
        }
        return false;
    }

    @Override
    public String getFileName() {
        return "hbmArcFurnace.json";
    }

    @Override
    public Object getRecipeObject() {
        return recipeList;
    }

    @Override
    public void deleteRecipes() {
        occupiedSolid.clear();
        occupiedLiquid.clear();
        recipeList.clear();
        fastCacheSolid.clear();
        fastCacheLiquid.clear();
    }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject rec = (JsonObject) recipe;
        ArcFurnaceRecipe arc = new ArcFurnaceRecipe();

        AStack input = readAStack(rec.get("input").getAsJsonArray());

        if(rec.has("solid")) {
            arc.solid(readItemStack(rec.get("solid").getAsJsonArray()));
        }

        if(rec.has("fluid")) {
            JsonArray fluids = rec.get("fluid").getAsJsonArray();
            List<MaterialStack> mats = new ArrayList<>();

            for(JsonElement fluid : fluids) {
                JsonArray matStack = fluid.getAsJsonArray();
                var material = Mats.matByName.get(matStack.get(0).getAsString());
                if(material == null || material.smeltable != SmeltingBehavior.SMELTABLE) continue;
                mats.add(new MaterialStack(material, matStack.get(1).getAsInt()));
            }

            if(!mats.isEmpty()) arc.fluid(mats.toArray(new MaterialStack[0]));
        }

        register(input, arc);
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        Pair<AStack, ArcFurnaceRecipe> rec = (Pair<AStack, ArcFurnaceRecipe>) recipe;

        writer.name("input");
        writeAStack(rec.getKey(), writer);

        if(rec.getValue().solidOutput != null) {
            writer.name("solid");
            writeItemStack(rec.getValue().solidOutput, writer);
        }

        if(rec.getValue().fluidOutput != null) {
            writer.name("fluid").beginArray();
            writer.setIndent("");

            for(MaterialStack stack : rec.getValue().fluidOutput) {
                writer.beginArray();
                writer.value(stack.material.names[0]).value(stack.amount);
                writer.endArray();
            }

            writer.endArray();
            writer.setIndent("  ");
        }
    }

    @Override
    public String getComment() {
        return "Recipes for the large arc furnace. Solid mode turns one item into another, liquid mode melts the item down into its materials. "
                + "Smelting that follows from shape-and-material tags (like c:ingots/steel) and from regular furnace recipes is auto-generated and cannot be changed here. "
                + "Material amounts are in quanta (1 quantum is 1/72 of an ingot).";
    }

    public static class ArcFurnaceRecipe {

        public MaterialStack[] fluidOutput;
        public ItemStack solidOutput;

        public ArcFurnaceRecipe fluid(MaterialStack... outputs) {
            this.fluidOutput = outputs;
            return this;
        }

        public ArcFurnaceRecipe solid(ItemStack output) {
            this.solidOutput = output;
            return this;
        }
    }
}
