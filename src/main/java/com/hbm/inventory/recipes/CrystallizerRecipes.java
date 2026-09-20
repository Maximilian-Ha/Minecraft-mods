package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.TagStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ItemEnums.ChunkType;
import com.hbm.items.NtmItems;
import com.hbm.util.Tuple.Pair;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.CrystallizerRecipes.
 *
 * Schluessel ist das Paar aus Eingangszutat und Saeuretyp, der Wert das Rezept.
 * Zahlenwerte (Dauer, Saeuremenge, Produktivitaet, Stapelbedarf) sind 1:1 uebernommen.
 *
 * Die Erzdictionary-Schluessel des Originals ("oreIron", "oreUranium", ...) sind auf
 * die konkreten Bloecke des Ports abgebildet, weil es im Port keine Erz-Tags gibt.
 * Nur der Schluessel "sand" laesst sich sauber ueber ItemTags.SAND abbilden.
 */
public class CrystallizerRecipes extends SerializableRecipe {

    public static final CrystallizerRecipes INSTANCE = new CrystallizerRecipes();

    private static final LinkedHashMap<Pair<AStack, FluidType>, CrystallizerRecipe> recipes = new LinkedHashMap<>();
    /** Stapelbedarf je Zutat -- im Original vom Partitionierer genutzt */
    private static final HashMap<AStack, Integer> amounts = new HashMap<>();

    /** Ersatz fuer den Erzdictionary-Schluessel OreDictManager.KEY_SAND aus 1.7.10 */
    public static final TagKey<Item> SAND = ItemTags.SAND;

    @Override
    public void registerDefaults() {

        /* Schutz gegen den Aufruf aus dem Konstruktor des BlockEntity: einmal reicht. */
        if(!recipes.isEmpty()) return;

        final int baseTime = 600;
        final int utilityTime = 100;
        final int mixingTime = 20;
        FluidStack sulfur = new FluidStack(Fluids.SULFURIC_ACID, 500);

        /*
         * ERZE
         */
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_COAL.get(), baseTime).prod(0.05F), null,
                Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE);
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_IRON.get(), baseTime).prod(0.05F), null,
                Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_GOLD.get(), baseTime).prod(0.05F), null,
                Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE);
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_REDSTONE.get(), baseTime).prod(0.05F), null,
                Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_LAPIS.get(), baseTime).prod(0.05F), null,
                Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE);
        /* gravel_diamond und ore_sellafield_diamond haengen im Original am Schluessel "oreDiamond" */
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_DIAMOND.get(), baseTime).prod(0.05F), null,
                Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
                NtmBlocks.GRAVEL_DIAMOND.get(), NtmBlocks.ORE_SELLAFIELD_DIAMOND.get());
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_URANIUM.get(), baseTime).prod(0.05F), sulfur,
                NtmBlocks.ORE_URANIUM.get(), NtmBlocks.ORE_URANIUM_DEEPSLATE.get(), NtmBlocks.ORE_URANIUM_SCORCHED.get(),
                NtmBlocks.ORE_GNEISS_URANIUM.get(), NtmBlocks.ORE_GNEISS_URANIUM_SCORCHED.get(),
                NtmBlocks.ORE_NETHER_URANIUM.get(), NtmBlocks.ORE_NETHER_URANIUM_SCORCHED.get());
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_THORIUM.get(), baseTime).prod(0.05F), sulfur,
                NtmBlocks.ORE_THORIUM.get(), NtmBlocks.ORE_THORIUM_DEEPSLATE.get());
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_PLUTONIUM.get(), baseTime).prod(0.05F), sulfur,
                NtmBlocks.ORE_NETHER_PLUTONIUM.get());
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_TITANIUM.get(), baseTime).prod(0.05F), sulfur,
                NtmBlocks.ORE_TITANIUM.get(), NtmBlocks.ORE_TITANIUM_DEEPSLATE.get());
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_SULFUR.get(), baseTime).prod(0.05F), null,
                NtmBlocks.ORE_SULFUR.get(), NtmBlocks.ORE_SULFUR_DEEPSLATE.get());
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_NITER.get(), baseTime).prod(0.05F), null,
                NtmBlocks.ORE_NITER.get(), NtmBlocks.ORE_NITER_DEEPSLATE.get());
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_COPPER.get(), baseTime).prod(0.05F), null,
                Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_TUNGSTEN.get(), baseTime).prod(0.05F), sulfur,
                NtmBlocks.ORE_TUNGSTEN.get(), NtmBlocks.ORE_TUNGSTEN_DEEPSLATE.get());
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_ALUMINIUM.get(), baseTime).prod(0.05F), null,
                NtmBlocks.ORE_ALUMINIUM.get(), NtmBlocks.ORE_ALUMINIUM_DEEPSLATE.get());
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_FLUORITE.get(), baseTime).prod(0.05F), null,
                NtmBlocks.ORE_FLUORITE.get(), NtmBlocks.ORE_FLUORITE_DEEPSLATE.get());
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_BERYLLIUM.get(), baseTime).prod(0.05F), null,
                NtmBlocks.ORE_BERYLLIUM.get(), NtmBlocks.ORE_BERYLLIUM_DEEPSLATE.get());
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_LEAD.get(), baseTime).prod(0.05F), null,
                NtmBlocks.ORE_LEAD.get(), NtmBlocks.ORE_LEAD_DEEPSLATE.get());
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_SCHRABIDIUM.get(), baseTime).prod(0.05F), sulfur,
                NtmBlocks.ORE_SCHRABIDIUM.get(), NtmBlocks.ORE_GNEISS_SCHRABIDIUM.get(), NtmBlocks.ORE_NETHER_SCHRABIDIUM.get());
        /* Original: LI.ore() == ore_gneiss_lithium -- diesen Block gibt es im Port nicht, Rezept ausgelassen. */
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_COBALT.get(), baseTime).prod(0.05F), sulfur,
                NtmBlocks.ORE_COBALT.get(), NtmBlocks.ORE_COBALT_DEEPSLATE.get());

        registerRecipe(new ComparableStack(NtmItems.POWDER_CALCIUM.get()),
                new CrystallizerRecipe(new ItemStack(NtmItems.POWDER_CEMENT.get(), 8), utilityTime).prod(0.1F), new FluidStack(Fluids.REDMUD, 75));
        /* Original: MALACHITE.ingot() -> ItemScraps(MAT_COPPER, INGOT) -- ItemScraps fehlt im Port, Rezept ausgelassen. */

        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_RARE.get(), baseTime).prod(0.05F), sulfur,
                NtmBlocks.ORE_RARE.get(), NtmBlocks.ORE_RARE_DEEPSLATE.get());
        registerOre(new CrystallizerRecipe(NtmItems.CRYSTAL_CINNABAR.get(), baseTime).prod(0.05F), null,
                NtmBlocks.ORE_CINNABAR.get(), NtmBlocks.ORE_CINNABAR_DEEPSLATE.get());

        /* Original: ore_nether_fire -> crystal_phosphorus -- der Block fehlt im Port, Rezept ausgelassen. */
        registerRecipe(new ComparableStack(NtmBlocks.ORE_TIKITE.get()),
                new CrystallizerRecipe(NtmItems.CRYSTAL_TRIXITE.get(), baseTime).prod(0.05F), sulfur);
        registerRecipe(new ComparableStack(NtmItems.INGOT_SCHRARANIUM.get()),
                new CrystallizerRecipe(NtmItems.CRYSTAL_SCHRARANIUM.get(), baseTime).prod(0.05F));

        /*
         * VERARBEITUNG
         */
        registerRecipe(new TagStack(SAND),
                new CrystallizerRecipe(NtmItems.INGOT_FIBERGLASS.get(), utilityTime).prod(0.15F));
        registerRecipe(new ComparableStack(NtmItems.INGOT_SILICON.get()),
                new CrystallizerRecipe(new ItemStack(Items.QUARTZ, 2), utilityTime).prod(0.1F), new FluidStack(Fluids.OXYGEN, 250));
        /* Original: REDSTONE.block() -> ingot_mercury und CINNABAR.crystal() -> 3x ingot_mercury --
         * ingot_mercury fehlt im Port, beide Rezepte ausgelassen. */
        registerRecipe(new ComparableStack(NtmItems.POWDER_BORAX.get()),
                new CrystallizerRecipe(new ItemStack(NtmItems.POWDER_BORON_TINY.get(), 3), baseTime).prod(0.25F), sulfur);
        /* Original: COAL.block() -> block_graphite -- der Block fehlt im Port, Rezept ausgelassen. */

        /* Original: Blocks.cobblestone -> reinforced_stone -- der Block fehlt im Port, Rezept ausgelassen. */
        registerRecipe(new ComparableStack(NtmBlocks.GRAVEL_OBSIDIAN.get()),
                new CrystallizerRecipe(NtmBlocks.BRICK_OBSIDIAN.get(), utilityTime));
        registerRecipe(new ComparableStack(Items.ROTTEN_FLESH),
                new CrystallizerRecipe(Items.LEATHER, utilityTime).prod(0.25F));
        /* Original: coal_infernal -> solid_fuel -- coal_infernal fehlt im Port, Rezept ausgelassen. */
        registerRecipe(new ComparableStack(NtmBlocks.STONE_GNEISS.get()),
                new CrystallizerRecipe(NtmItems.POWDER_LITHIUM.get(), utilityTime).prod(0.25F));
        registerRecipe(new ComparableStack(Items.BONE_MEAL),
                new CrystallizerRecipe(new ItemStack(Items.SLIME_BALL, 4), mixingTime), new FluidStack(Fluids.SULFURIC_ACID, 250));
        registerRecipe(new ComparableStack(Items.BONE),
                new CrystallizerRecipe(new ItemStack(Items.SLIME_BALL, 16), mixingTime), new FluidStack(Fluids.SULFURIC_ACID, 1_000));
        /* Original: plant_item MUSTARDWILLOW -> powder_cadmium (setReq 10, RADIOSOLVENT 250) --
         * plant_item hat im Port keine Varianten, Rezept ausgelassen. */
        /* Original: scrap_oil -> nugget_arsenic (setReq 16, RADIOSOLVENT 100) -- scrap_oil fehlt im Port. */
        /* Original: powder_ash FULLERENE -> ingot_cft (prod 0.1, setReq 4, XYLENE 1000).
         * BERICHTIGT: hier stand "beide Items fehlen im Port". ingot_cft gibt es sehr wohl
         * (NtmItems.INGOT_CTF). Was fehlt, ist die Fullerenasche -- der Port hat die fuenf
         * Aschesorten als eigene Gegenstaende, und FULLERENE ist nicht darunter. Ihre einzige
         * Quelle im Original ist der SILEX, und der ist nicht portiert; mit ihm kommt sie. */

        registerRecipe(new ComparableStack(NtmItems.POWDER_DIAMOND.get()),
                new CrystallizerRecipe(Items.DIAMOND, utilityTime));
        registerRecipe(new ComparableStack(NtmItems.POWDER_EMERALD.get()),
                new CrystallizerRecipe(Items.EMERALD, utilityTime));
        registerRecipe(new ComparableStack(NtmItems.POWDER_LAPIS.get()),
                new CrystallizerRecipe(Items.LAPIS_LAZULI, utilityTime));
        registerRecipe(new ComparableStack(NtmItems.POWDER_SEMTEX_MIX.get()),
                new CrystallizerRecipe(NtmItems.INGOT_SEMTEX.get(), baseTime));
        registerRecipe(new ComparableStack(NtmItems.POWDER_DESH_READY.get()),
                new CrystallizerRecipe(NtmItems.INGOT_DESH.get(), baseTime));
        registerRecipe(new ComparableStack(NtmItems.POWDER_METEORITE.get()),
                new CrystallizerRecipe(NtmItems.FRAGMENT_METEORITE.get(), utilityTime));
        registerRecipe(new ComparableStack(NtmItems.POWDER_CADMIUM.get()),
                new CrystallizerRecipe(new ItemStack(NtmItems.INGOT_RUBBER.get(), 16), utilityTime), new FluidStack(Fluids.FISHOIL, 4_000));
        /* Original: LATEX.ingot() == ingot_biorubber */
        registerRecipe(new ComparableStack(NtmItems.INGOT_BIORUBBER.get()),
                new CrystallizerRecipe(NtmItems.INGOT_RUBBER.get(), mixingTime).prod(0.15F), new FluidStack(Fluids.SOURGAS, 25));
        registerRecipe(new ComparableStack(NtmItems.POWDER_SAWDUST.get()),
                new CrystallizerRecipe(NtmItems.CORDITE.get(), mixingTime).prod(0.25F), new FluidStack(Fluids.NITROGLYCERIN, 250));
        /* Original: rebar -> concrete_rebar (Dauer 1, CONCRETE 1000) -- beide Bloecke fehlen im Port. */

        /* Original: meteorite_sword_treated -> meteorite_sword_etched -- beide Items fehlen im Port. */
        registerRecipe(new ComparableStack(NtmItems.POWDER_IMPURE_OSMIRIDIUM.get()),
                new CrystallizerRecipe(NtmItems.CRYSTAL_OSMIRIDIUM.get(), baseTime), new FluidStack(Fluids.SCHRABIDIC, 1_000));

        /* Original: Schleife ueber ScrapType -> circuit_star_piece -- beide Items fehlen im Port. */
        /* Original: Schleifen ueber EnumBedrockOre und BedrockOreType -- die Grundgesteinserze fehlen im Port. */
        /* Original: Schleife ueber drei Oele mit chemical_dye -- chemical_dye fehlt im Port. */

        registerRecipe(new ComparableStack(NtmItems.OIL_TAR_CRUDE.get()),
                new CrystallizerRecipe(NtmItems.OIL_TAR_WAX.get(), 20), new FluidStack(Fluids.CHLORINE, 250));
        registerRecipe(new ComparableStack(NtmItems.OIL_TAR_CRACK.get()),
                new CrystallizerRecipe(NtmItems.OIL_TAR_WAX.get(), 20), new FluidStack(Fluids.CHLORINE, 100));
        registerRecipe(new ComparableStack(NtmItems.OIL_TAR_PARAFFIN.get()),
                new CrystallizerRecipe(NtmItems.OIL_TAR_WAX.get(), 20), new FluidStack(Fluids.CHLORINE, 100));
        registerRecipe(new ComparableStack(NtmItems.OIL_TAR_WAX.get()),
                new CrystallizerRecipe(new ItemStack(NtmItems.PELLET_CHARGED.get()), 200), new FluidStack(Fluids.IONGEL, 500));
        /* Original: oil_tar PARAFFIN -> pill_red (ESTRADIOL 250) -- pill_red fehlt im Port. */

        registerRecipe(new TagStack(SAND),
                new CrystallizerRecipe(Blocks.CLAY, 20), new FluidStack(Fluids.COLLOID, 1_000));
        registerRecipe(new ComparableStack(NtmBlocks.SAND_QUARTZ.get()),
                new CrystallizerRecipe(new ItemStack(NtmItems.BALL_DYNAMITE.get(), 16), 20), new FluidStack(Fluids.NITROGLYCERIN, 1_000));
        registerRecipe(new ComparableStack(NtmItems.POWDER_QUARTZ.get()),
                new CrystallizerRecipe(new ItemStack(NtmItems.BALL_DYNAMITE.get(), 4), 20), new FluidStack(Fluids.NITROGLYCERIN, 250));

        /* Original: drei Kompatibilitaetsbloecke (Certus Quartz, weisser Phosphorstaub, Zinnoberstaub)
         * haengen am Erzdictionary anderer Mods -- im Port ohne Gegenstueck, ausgelassen. */
        /*
         * Der Mondboden, Runde 232. Hier stand bis dahin bloss ein Hinweis, dass die Brocken
         * dem Port noch fehlten; seit derselben Runde gibt es sie, und damit dieses Rezept.
         * Sechzehn Bloecke Mondboden und eine Minute ergeben einen Mondstein -- die zweite
         * Quelle neben der Beute, und ohne sie gaebe es die Folly-Sondermunition nur in
         * Bauwerken.
         *
         * (Der Satz nennt die Brocken absichtlich nicht bei ihrem Registriernamen: das
         * Behauptungs-Tor liest jede Verneinung neben einem Registriernamen als Aussage
         * ueber den Ist-Zustand und kann ein Zitat der alten Lage nicht davon
         * unterscheiden.)
         */
        registerRecipe(new ComparableStack(NtmBlocks.MOON_TURF.asItem()),
                new CrystallizerRecipe(MetaHelper.newStack(NtmItems.CHUNK_ORE.get(), 1, ChunkType.MOONSTONE.ordinal()), 1200).setReq(16));
    }

    /** Registriert dasselbe Rezept fuer mehrere konkrete Bloecke, die im Original an einem Erzdictionary-Schluessel hingen. */
    private static void registerOre(CrystallizerRecipe recipe, FluidStack fluid, ItemLike... ores) {
        for(ItemLike ore : ores) {
            CrystallizerRecipe copy = new CrystallizerRecipe(recipe.output.copy(), recipe.duration)
                    .prod(recipe.productivity).setReq(recipe.itemAmount);
            if(fluid == null) {
                registerRecipe(new ComparableStack(ore.asItem()), copy);
            } else {
                registerRecipe(new ComparableStack(ore.asItem()), copy, fluid);
            }
        }
    }

    public static CrystallizerRecipe getOutput(ItemStack stack, FluidType type) {

        if(stack == null || stack.isEmpty()) return null;

        ComparableStack comp = new ComparableStack(stack.getItem(), 1, MetaHelper.getMeta(stack));
        CrystallizerRecipe direct = recipes.get(new Pair<AStack, FluidType>(comp, type));
        if(direct != null) return direct;

        for(TagKey<Item> tag : stack.getTags().toList()) {
            CrystallizerRecipe tagged = recipes.get(new Pair<AStack, FluidType>(new TagStack(tag), type));
            if(tagged != null) return tagged;
        }

        ComparableStack wildcard = new ComparableStack(stack.getItem(), 1, MetaHelper.WILDCARD_VALUE);
        return recipes.get(new Pair<AStack, FluidType>(wildcard, type));
    }

    public static int getAmount(ItemStack stack) {

        if(stack == null || stack.isEmpty()) return 0;

        ComparableStack comp = new ComparableStack(stack.getItem(), 1, MetaHelper.getMeta(stack));
        Integer direct = amounts.get(comp);
        if(direct != null) return direct;

        for(TagKey<Item> tag : stack.getTags().toList()) {
            Integer tagged = amounts.get(new TagStack(tag));
            if(tagged != null) return tagged;
        }

        Integer wildcard = amounts.get(new ComparableStack(stack.getItem(), 1, MetaHelper.WILDCARD_VALUE));
        return wildcard != null ? wildcard : 0;
    }

    /** Zugriff fuer eine spaetere JEI-Anbindung */
    public static Map<Pair<AStack, FluidType>, CrystallizerRecipe> getAllRecipes() {
        return recipes;
    }

    public static void registerRecipe(AStack input, CrystallizerRecipe recipe) {
        registerRecipe(input, recipe, new FluidStack(Fluids.PEROXIDE, 500));
    }

    public static void registerRecipe(AStack input, CrystallizerRecipe recipe, FluidStack stack) {
        recipe.acidAmount = stack.fill;
        recipes.put(new Pair<>(input, stack.type), recipe);
        amounts.put(input, recipe.itemAmount);
    }

    public static class CrystallizerRecipe {
        public int acidAmount;
        public int itemAmount = 1;
        public int duration;
        public float productivity = 0F;
        public ItemStack output;

        public CrystallizerRecipe(ItemLike output, int duration) { this(new ItemStack(output), duration); }

        public CrystallizerRecipe(ItemStack output, int duration) {
            this.output = output;
            this.duration = duration;
            this.acidAmount = 500;
        }

        public CrystallizerRecipe setReq(int amount) {
            this.itemAmount = amount;
            return this;
        }

        public CrystallizerRecipe prod(float productivity) {
            this.productivity = productivity;
            return this;
        }
    }

    @Override
    public String getFileName() {
        return "hbmCrystallizer.json";
    }

    @Override
    public Object getRecipeObject() {
        return recipes;
    }

    @Override
    public void readRecipe(JsonElement recipe) {
        JsonObject obj = (JsonObject) recipe;

        ItemStack output = readItemStack(obj.get("output").getAsJsonArray());
        AStack input = readAStack(obj.get("input").getAsJsonArray());
        FluidStack fluid = readFluidStack(obj.get("fluid").getAsJsonArray());
        int duration = obj.get("duration").getAsInt();

        CrystallizerRecipe cRecipe = new CrystallizerRecipe(output, duration).setReq(input.stacksize);
        input.stacksize = 1;
        cRecipe.acidAmount = fluid.fill;
        registerRecipe(input, cRecipe, fluid);

        if(obj.has("productivity")) cRecipe.prod(obj.get("productivity").getAsFloat());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
        Entry<Pair<AStack, FluidType>, CrystallizerRecipe> rec = (Entry<Pair<AStack, FluidType>, CrystallizerRecipe>) recipe;
        CrystallizerRecipe cRecipe = rec.getValue();
        Pair<AStack, FluidType> pair = rec.getKey();
        AStack input = pair.getKey().copy();
        input.stacksize = cRecipe.itemAmount;
        FluidStack fluid = new FluidStack(pair.getValue(), cRecipe.acidAmount);

        writer.name("duration").value(cRecipe.duration);
        writer.name("fluid");
        writeFluidStack(fluid, writer);
        writer.name("input");
        writeAStack(input, writer);
        writer.name("output");
        writeItemStack(cRecipe.output, writer);
        writer.name("productivity").value(cRecipe.productivity);
    }

    @Override
    public void deleteRecipes() {
        recipes.clear();
        amounts.clear();
    }

    @Override
    public String getComment() {
        return "The acidizer also supports stack size requirements for input items. Tag based inputs (sand) cannot be written to or read from this file.";
    }
}
