package com.hbm.inventory.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.TagStack;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.CastPlateItem;
import com.hbm.items.NtmItems;
import com.hbm.items.WireDenseItem;
import com.hbm.main.NuclearTechMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static com.hbm.inventory.material.MaterialShapes.*;
import static com.hbm.inventory.material.Mats.*;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.material.MatDistribution.
 *
 * Sagt, woraus ein zusammengesetzter Gegenstand besteht -- was also beim Einschmelzen
 * herauskommt. Die Form-und-Material-Gegenstaende (Barren, Platte, Draht) braucht diese Liste
 * nicht, die findet Mats.getMaterialsFromItem ueber die Tags; hier stehen nur die Faelle, die
 * sich daraus nicht ergeben: Loren, Klingen, Stempel und die Erze samt ihrer Nebenprodukte.
 *
 * Erze sind im Original auf OreDictionary-Namen geschluesselt, hier auf Item-Tags.
 */
public class MatDistribution extends SerializableRecipe {

    private static TagKey<Item> ore(String material) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/" + material));
    }

    @Override
    public void registerDefaults() {
        //vanilla crap
        registerOre(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "stones")), MAT_STONE, BLOCK.q(1));
        registerEntry(Blocks.OBSIDIAN, MAT_OBSIDIAN, BLOCK.q(1));
        registerEntry(Blocks.RAIL, MAT_IRON, INGOT.q(6, 16));
        registerEntry(Blocks.POWERED_RAIL, MAT_GOLD, INGOT.q(6, 6), MAT_REDSTONE, DUST.q(1, 6));
        registerEntry(Blocks.DETECTOR_RAIL, MAT_IRON, INGOT.q(6, 6), MAT_REDSTONE, DUST.q(1, 6));
        registerEntry(Items.MINECART, MAT_IRON, INGOT.q(5));

        //castables
        registerEntry(NtmItems.BLADE_TITANIUM.get(), MAT_TITANIUM, INGOT.q(3));
        registerEntry(NtmItems.BLADE_TUNGSTEN.get(), MAT_TUNGSTEN, INGOT.q(3));
        registerEntry(NtmItems.BLADES_STEEL.get(), MAT_STEEL, INGOT.q(4));
        registerEntry(NtmItems.BLADES_TITANIUM.get(), MAT_TITANIUM, INGOT.q(4));
        registerEntry(NtmItems.PIPES_STEEL.get(), MAT_STEEL, BLOCK.q(3));
        registerEntry(NtmItems.POWDER_FLUX.get(), MAT_FLUX, DUST.q(1));

        //actual ores
        registerOre(ore("iron"), MAT_IRON, INGOT.q(2), MAT_TITANIUM, NUGGET.q(3), MAT_STONE, QUART.q(1));
        registerOre(ore("titanium"), MAT_TITANIUM, INGOT.q(2), MAT_IRON, NUGGET.q(3), MAT_STONE, QUART.q(1));
        registerOre(ore("tungsten"), MAT_TUNGSTEN, INGOT.q(2), MAT_STONE, QUART.q(1));
        registerOre(ore("aluminum"), MAT_ALUMINIUM, INGOT.q(2), MAT_SODIUM, NUGGET.q(3), MAT_STONE, QUART.q(1));
        registerOre(ore("coal"), MAT_CARBON, GEM.q(3), MAT_STONE, QUART.q(1));
        registerOre(ore("gold"), MAT_GOLD, INGOT.q(2), MAT_LEAD, NUGGET.q(3), MAT_STONE, QUART.q(1));
        registerOre(ore("uranium"), MAT_URANIUM, INGOT.q(2), MAT_LEAD, NUGGET.q(3), MAT_STONE, QUART.q(1));
        registerOre(ore("thorium"), MAT_THORIUM, INGOT.q(2), MAT_URANIUM, NUGGET.q(3), MAT_STONE, QUART.q(1));
        registerOre(ore("copper"), MAT_COPPER, INGOT.q(2), MAT_STONE, QUART.q(1));
        registerOre(ore("lead"), MAT_LEAD, INGOT.q(2), MAT_GOLD, NUGGET.q(1), MAT_STONE, QUART.q(1));
        registerOre(ore("beryllium"), MAT_BERYLLIUM, INGOT.q(2), MAT_STONE, QUART.q(1));
        registerOre(ore("cobalt"), MAT_COBALT, INGOT.q(1), MAT_STONE, QUART.q(1));
        registerOre(ore("redstone"), MAT_REDSTONE, INGOT.q(4), MAT_STONE, QUART.q(1));

        // Asche: im Original drei Untertypen eines Gegenstands, im Port drei eigene.
        registerEntry(NtmItems.POWDER_ASH_WOOD.get(), MAT_CARBON, NUGGET.q(1));
        registerEntry(NtmItems.POWDER_ASH_COAL.get(), MAT_CARBON, NUGGET.q(2));
        registerEntry(NtmItems.POWDER_ASH_MISC.get(), MAT_CARBON, NUGGET.q(1));

        // Gussplatte und dichter Draht: die beiden Formen, die im Port als Untertyp-Gegenstand
        // vorliegen und deshalb nicht ueber Tags gefunden werden koennen. Siehe MatShapeItems.
        registerShapeItems();

        // Nicht uebernommen, weil es die Gegenstaende im Port (noch) nicht gibt: die vier
        // Huelsentypen (casing), der Kryolith-Erzbrocken, Hematit und Malachit als Erz sowie
        // der Kalkstein als Flussmittelquelle.
    }

    /**
     * Traegt Gussplatten, geschweisste Platten und dichte Draehte ein. Jede dieser Formen ist
     * im Port EIN Gegenstand mit Metadaten; ein Tag koennte sie nicht auseinanderhalten, ein
     * ComparableStack schon.
     */
    private static void registerShapeItems() {

        int count = 0;

        for(Entry<CastPlateItem.Type, NTMMaterial> entry : MatShapeItems.getCastPlateMats().entrySet()) {

            CastPlateItem.Type type = entry.getKey();

            if(type.isAllowed(false)) {
                registerEntry(new ComparableStack(NtmItems.CAST_PLATE.get(), 1, type.ordinal()), entry.getValue(), CASTPLATE.q(1));
                count++;
            }

            if(type.isAllowed(true)) {
                registerEntry(new ComparableStack(NtmItems.CAST_PLATE_WELDED.get(), 1, type.ordinal()), entry.getValue(), WELDEDPLATE.q(1));
                count++;
            }
        }

        for(Entry<WireDenseItem.Type, NTMMaterial> entry : MatShapeItems.getWireDenseMats().entrySet()) {
            registerEntry(new ComparableStack(NtmItems.WIRE_DENSE.get(), 1, entry.getKey().meta), entry.getValue(), DENSEWIRE.q(1));
            count++;
        }

        NuclearTechMod.LOGGER.info("Materialsystem: {} Eintraege fuer Gussplatte und dichten Draht ({}).", count, MatShapeItems.describe());
    }

    public static void registerEntry(Object key, Object... matDef) {
        ComparableStack comp = null;

        if(key instanceof Item item) comp = new ComparableStack(item);
        if(key instanceof Block block) comp = new ComparableStack(block);
        if(key instanceof ItemStack stack) comp = new ComparableStack(stack);
        if(key instanceof ComparableStack stack) comp = stack;

        if(comp == null) return;

        List<MaterialStack> stacks = toStacks(matDef);
        if(stacks.isEmpty()) return;

        materialEntries.put(comp, stacks);
    }

    public static void registerOre(TagKey<Item> key, Object... matDef) {
        List<MaterialStack> stacks = toStacks(matDef);
        if(stacks.isEmpty()) return;

        materialOreEntries.put(key, stacks);
    }

    private static List<MaterialStack> toStacks(Object... matDef) {
        List<MaterialStack> stacks = new ArrayList<>();
        if(matDef.length % 2 == 1) return stacks;

        for(int i = 0; i < matDef.length; i += 2) {
            stacks.add(new MaterialStack((NTMMaterial) matDef[i], (int) matDef[i + 1]));
        }

        return stacks;
    }

    @Override public String getFileName() { return "hbmCrucibleSmelting.json"; }

    @Override
    public Object getRecipeObject() {
        List<Object> entries = new ArrayList<>();
        entries.addAll(Mats.materialEntries.entrySet());
        entries.addAll(Mats.materialOreEntries.entrySet());
        return entries;
    }

    @Override
    public void readRecipe(JsonElement recipe) {
        JsonObject obj = (JsonObject) recipe;
        AStack input = readAStack(obj.get("input").getAsJsonArray());

        List<MaterialStack> materials = new ArrayList<>();
        JsonArray output = obj.get("output").getAsJsonArray();

        for(int i = 0; i < output.size(); i++) {
            JsonArray entry = output.get(i).getAsJsonArray();
            NTMMaterial mat = Mats.matByName.get(entry.get(0).getAsString());
            if(mat == null) continue;
            materials.add(new MaterialStack(mat, entry.get(1).getAsInt()));
        }

        if(materials.isEmpty()) return;

        if(input instanceof ComparableStack comp) {
            Mats.materialEntries.put(comp, materials);
        } else if(input instanceof TagStack tag) {
            Mats.materialOreEntries.put(tag.tag, materials);
        }
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
        Entry<?, ?> entry = (Entry<?, ?>) recipe;
        AStack toSmelt = null;

        if(entry.getKey() instanceof TagKey<?> tag) {
            toSmelt = new TagStack(ItemTags.create(tag.location()));
        } else if(entry.getKey() instanceof ComparableStack comp) {
            toSmelt = comp;
        }

        if(toSmelt == null) return;

        writer.name("input");
        writeAStack(toSmelt, writer);
        writer.name("output").beginArray();
        writer.setIndent("");

        for(MaterialStack stack : (List<MaterialStack>) entry.getValue()) {
            writer.beginArray();
            writer.value(stack.material.names[0]).value(stack.amount);
            writer.endArray();
        }

        writer.endArray();
        writer.setIndent("  ");
    }

    @Override
    public void deleteRecipes() {
        Mats.materialEntries.clear();
        Mats.materialOreEntries.clear();
    }

    @Override
    public String getComment() {
        return "Defines a set of items that can be smelted. Smelting generated from shape-and-material tags (like c:ingots/steel) is auto-generated and cannot be "
                + "changed. This config only changes fixed items (like recycling for certain metallic objects) and ores (with variable byproducts). "
                + "Amounts used are in quanta (1 quantum is 1/72 of an ingot or 1/8 of a nugget). Material names are the ones listed in Mats, case-sensitive.";
    }
}
