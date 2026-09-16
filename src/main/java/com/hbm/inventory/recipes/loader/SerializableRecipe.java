package com.hbm.inventory.recipes.loader;

import api.hbm.recipe.IRecipeRegisterListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.CompoundTag;
import com.hbm.inventory.RecipesCommon.NBTStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.SolderingRecipes;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.PrecAssRecipes;
import com.hbm.inventory.recipes.ParticleAcceleratorRecipes;
import com.hbm.inventory.recipes.ExposureChamberRecipes;
import com.hbm.inventory.recipes.ArcFurnaceRecipes;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.inventory.recipes.CrackingRecipes;
import com.hbm.inventory.recipes.FractionRecipes;
import com.hbm.inventory.recipes.ReformingRecipes;
import com.hbm.inventory.recipes.HydrotreatingRecipes;
import com.hbm.inventory.recipes.VacuumRefineryRecipes;
import com.hbm.inventory.recipes.SolidificationRecipes;
import com.hbm.inventory.recipes.PyroOvenRecipes;
import com.hbm.inventory.recipes.LiquefactionRecipes;
import com.hbm.inventory.recipes.BreederRecipes;
import com.hbm.inventory.recipes.FluidBreederRecipes;
import com.hbm.inventory.recipes.PlasmaForgeRecipes;
import com.hbm.inventory.recipes.FusionRecipes;
import com.hbm.inventory.recipes.PUREXRecipes;
import com.hbm.inventory.recipes.FuelPoolRecipes;
import com.hbm.inventory.recipes.OutgasserRecipes;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes;
import com.hbm.inventory.recipes.ArcWelderRecipes;
import com.hbm.inventory.recipes.BlastFurnaceRecipes;
import com.hbm.inventory.recipes.AmmoPressRecipes;
import com.hbm.inventory.recipes.AnnihilatorRecipes;
import com.hbm.inventory.recipes.ChemicalPlantRecipes;
import com.hbm.inventory.recipes.CentrifugeRecipes;
import com.hbm.inventory.material.MatDistribution;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.inventory.recipes.CyclotronRecipes;
import com.hbm.inventory.recipes.CompressorRecipes;
import com.hbm.inventory.recipes.MixerRecipes;
import com.hbm.inventory.recipes.RockMillRecipes;
import com.hbm.inventory.recipes.SuperComputerRecipes;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.ItemStackUtil;
import com.hbm.util.TagsUtil;
import com.hbm.util.Tuple.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.*;
import java.util.*;

//the anti-spaghetti. this class provides so much functionality and saves so much time, i just love you, SerializableRecipe <3
public abstract class SerializableRecipe {

    public static final Gson gson = new Gson();
    public static List<SerializableRecipe> recipeHandlers = new ArrayList<>();
    public static List<IRecipeRegisterListener> additionalListeners = new ArrayList<>();

    public static Map<String, InputStream> recipeSyncHandlers = new HashMap<>();

    public boolean modified = false;

    /*
     * INIT
     */

    public static void registerAllHandlers() {
        recipeHandlers.add(new SolderingRecipes());
        recipeHandlers.add(new ArcWelderRecipes());
        recipeHandlers.add(new PressRecipes());
        recipeHandlers.add(new ShredderRecipes());
        recipeHandlers.add(new CentrifugeRecipes());
        recipeHandlers.add(new CompressorRecipes());
        recipeHandlers.add(new MixerRecipes());
        recipeHandlers.add(new CrystallizerRecipes());
        recipeHandlers.add(new CyclotronRecipes());
        recipeHandlers.add(new AmmoPressRecipes());
        recipeHandlers.add(new AnnihilatorRecipes());
        recipeHandlers.add(new ParticleAcceleratorRecipes());
        recipeHandlers.add(new ExposureChamberRecipes());

        recipeHandlers.add(new AssemblyMachineRecipes());

        //GENERIC
        recipeHandlers.add(AssemblyMachineRecipes.INSTANCE);
        recipeHandlers.add(PrecAssRecipes.INSTANCE);
        recipeHandlers.add(BlastFurnaceRecipes.INSTANCE);
        recipeHandlers.add(ChemicalPlantRecipes.INSTANCE);
        recipeHandlers.add(PUREXRecipes.INSTANCE);
        recipeHandlers.add(FusionRecipes.INSTANCE);
        recipeHandlers.add(PlasmaForgeRecipes.INSTANCE);
        recipeHandlers.add(RockMillRecipes.INSTANCE);
        recipeHandlers.add(SuperComputerRecipes.INSTANCE);
        recipeHandlers.add(new MatDistribution());
        recipeHandlers.add(new ArcFurnaceRecipes());
        recipeHandlers.add(CrucibleRecipes.INSTANCE);
        recipeHandlers.add(new RotaryFurnaceRecipes());
        recipeHandlers.add(new OutgasserRecipes());
        recipeHandlers.add(new FuelPoolRecipes());
        recipeHandlers.add(new CrackingRecipes());
        recipeHandlers.add(new FractionRecipes());
        recipeHandlers.add(new ReformingRecipes());
        recipeHandlers.add(new HydrotreatingRecipes());
        recipeHandlers.add(new VacuumRefineryRecipes());
        recipeHandlers.add(new SolidificationRecipes());
        recipeHandlers.add(new PyroOvenRecipes());
        recipeHandlers.add(new LiquefactionRecipes());
        recipeHandlers.add(new FluidBreederRecipes());
        recipeHandlers.add(new BreederRecipes());
    }

    public static void initialize() {
        File recDir = new File(NuclearTechMod.configDir.getAbsolutePath() + File.separatorChar + "hbmRecipes");

        if(!recDir.exists()) {
            if(!recDir.mkdir()) {
                throw new IllegalStateException("Unable to make recipe directory " + recDir.getAbsolutePath());
            }
        }

        File info = new File(recDir.getAbsolutePath() + File.separatorChar + "REMOVE UNDERSCORE TO ENABLE RECIPE LOADING - RECIPES WILL RESET TO DEFAULT OTHERWISE");

        try { info.createNewFile(); } catch(IOException ignored) { }

        NuclearTechMod.LOGGER.info("Starting recipe init!");

        GenericRecipes.clearPools();

        for(SerializableRecipe recipe : recipeHandlers) {

            recipe.deleteRecipes();

            File recFile = new File(recDir.getAbsolutePath() + File.separatorChar + recipe.getFileName());

            if(recipeSyncHandlers.containsKey(recipe.getFileName())) {
                NuclearTechMod.LOGGER.info("Reading synced recipe file {}", recipe.getFileName());
                InputStream stream = recipeSyncHandlers.get(recipe.getFileName());

                try {
                    stream.reset();
                    Reader reader = new InputStreamReader(stream);
                    recipe.readRecipeStream(reader);
                    recipe.modified = true;
                } catch(Throwable ex) {
                    NuclearTechMod.LOGGER.error("Failed to reset synced recipe stream", ex);
                }
            } else if(recFile.exists() && recFile.isFile()) {
                NuclearTechMod.LOGGER.info("Reading recipe file {}", recFile.getName());
                recipe.readRecipeFile(recFile);
                recipe.modified = true;
            } else {
                NuclearTechMod.LOGGER.info("No recipe file found, registering defaults for {}", recipe.getFileName());
                recipe.registerDefaults();

                for(IRecipeRegisterListener listener : additionalListeners) {
                    listener.onRecipeLoad(recipe.getClass().getSimpleName());
                }

                File recTemplate = new File(recDir.getAbsolutePath() + File.separatorChar + "_" + recipe.getFileName());
                NuclearTechMod.LOGGER.info("Writing template file {}", recTemplate.getName());
                recipe.writeTemplateFile(recTemplate);
                recipe.modified = false;
            }

            recipe.registerPost();
        }

        NuclearTechMod.LOGGER.info("Finished recipe init!");
    }

    public static void receiveRecipes(String filename, byte[] data) {
        recipeSyncHandlers.put(filename, new ByteArrayInputStream(data));
    }

    public static void clearReceivedRecipes() {
        boolean hasCleared = !recipeSyncHandlers.isEmpty();
        recipeSyncHandlers.clear();

        if(hasCleared) initialize();
    }

    /*
     * ABSTRACT
     */

    /** The machine's (or process') name used for the recipe file */
    public abstract String getFileName();
    /** Return the list object holding all the recipes, usually an ArrayList or HashMap */
    public abstract Object getRecipeObject();
    /** Will use the supplied JsonElement (usually casts to JsonArray) from the over arching recipe array and adds the recipe to the recipe list object */
    public abstract void readRecipe(JsonElement recipe);
    /** Is given a single recipe from the recipe list object (a wrapper, Tuple, array, HashMap Entry, etc) and writes it to the current ongoing GSON stream */
    public abstract void writeRecipe(Object recipe, JsonWriter writer) throws IOException;
    /** Registers the default recipes */
    public abstract void registerDefaults();

    /** Ob dieser Rezeptsatz auch leer sein darf -- siehe writeTemplateFile. */
    public boolean allowEmptyRecipeList() { return false; }
    /** Deletes all existing recipes, currently unused */
    public abstract void deleteRecipes();
    /** A routine called after registering all recipes, whether it's a template or not. Good for IMC functionality. */
    public void registerPost() { }
    /** Returns a string to be printed as info at the top of the JSON file */
    public String getComment() {
        return null;
    }

    /*
     * JSON R/W WRAPPERS
     */

    public void writeTemplateFile(File template) {
        try {
            /* Get the recipe list object */
            Object recipeObject = this.getRecipeObject();
            List recipeList = new ArrayList();

            /* Try to pry all recipes from our list */
            if(recipeObject instanceof Collection) {
                recipeList.addAll((Collection) recipeObject);

            } else if(recipeObject instanceof HashMap) {
                recipeList.addAll(((HashMap) recipeObject).entrySet());
            }

            if(recipeList.isEmpty()) {
                /* Seit Runde 128: manche Rezeptsaetze DUERFEN leer sein. Der Annihilator etwa
                 * hat nur dann Schwellen, wenn der 528er-Schalter an ist; ohne diesen Haken
                 * wuerfe das Schreiben der Vorlage bei ausgeschaltetem Schalter. Im Original
                 * heisst der Haken genauso. */
                if(this.allowEmptyRecipeList()) return;
                throw new IllegalStateException("Error while writing recipes for " + this.getClass().getSimpleName() + ": Recipe list is either empty or in an unsupported format!");
            }

            JsonWriter writer = new JsonWriter(new FileWriter(template));
            writer.setIndent("  ");					//pretty formatting
            writer.beginObject();					//initial '{'

            if(this.getComment() != null) {
                writer.name("comment").value(this.getComment());
            }

            writer.name("recipes").beginArray();	//all recipes are stored in an array called "recipes"

            for(Object recipe : recipeList) {
                writer.beginObject();				//begin object for a single recipe
                this.writeRecipe(recipe, writer);	//serialize here
                writer.endObject();					//end recipe object
            }

            writer.endArray();						//end recipe array
            writer.endObject();						//final '}'
            writer.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void readRecipeFile(File file) {
        try {
            readRecipeStream(new FileReader(file));
        } catch(FileNotFoundException ex) { }
    }

    public void readRecipeStream(Reader reader) {
        JsonObject json = gson.fromJson(reader, JsonObject.class);
        JsonArray recipes = json.get("recipes").getAsJsonArray();
        for(JsonElement recipe : recipes) {
            if(recipe != null) this.readRecipe(recipe);
        }
    }

    /*
     * JSON IO UTIL
     */

    public static AStack readAStack(JsonArray array) {
        try {
            String type = array.get(0).getAsString();
            int stacksize = array.size() > 2 ? array.get(2).getAsInt() : 1;
            /* Runde 130: der NBT-Zweig war auskommentiert, seit der Port ihn ohne Nutzer
             * uebernommen hatte. Die Praezisionsmontage ist der erste Nutzer. */
            if("nbt".equals(type)) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(array.get(1).getAsString()));
                int meta = array.size() > 3 ? array.get(3).getAsInt() : 0;
                CompoundTag nbt = array.size() > 4 ? TagParser.parseTag(array.get(4).getAsString()) : null;
                return new NBTStack(item, stacksize, meta).withNBT(nbt);
            }
            if("item".equals(type)) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(array.get(1).getAsString())); // namespace:path
                int meta = array.size() > 3 ? array.get(3).getAsInt() : 0;
                return new ComparableStack(item, stacksize, meta);
            }
            //if("dict".equals(type)) {
            //    String dict = array.get(1).getAsString();
            //    return new OreDictStack(dict, stacksize);
            //}
        } catch(Exception ignored) { }
        NuclearTechMod.LOGGER.error("Error reading stack array {}", array.toString());
        return new ComparableStack(ItemStack.EMPTY);
    }

    public static AStack[] readAStackArray(JsonArray array) {
        try {
            AStack[] items = new AStack[array.size()];
            for(int i = 0; i < items.length; i++) { items[i] = readAStack((JsonArray) array.get(i)); }
            return items;
        } catch(Exception ex) { }
        NuclearTechMod.LOGGER.error("Error reading stack array {}", array.toString());
        return new AStack[0];
    }

    public static void writeAStack(AStack astack, JsonWriter writer) throws IOException {
        writer.beginArray();
        writer.setIndent("");
        /* Der NBT-Zweig MUSS vor dem ComparableStack stehen: NBTStack erbt von ComparableStack,
         * und ein instanceof faenge ihn sonst im falschen Zweig -- der Datenanhang ginge beim
         * Schreiben verloren, und das Rezept passte beim naechsten Lesen auf jeden Ausschuss. */
        if (astack instanceof NBTStack comp) {
            writer.value(comp.nbt != null ? "nbt" : "item");                                   //NBT   identifier
            writer.value(BuiltInRegistries.ITEM.getKey(comp.toStack().getItem()).toString());  //item name
            if (comp.stacksize != 1 || comp.meta > 0 || comp.nbt != null) writer.value(comp.stacksize);
            if (comp.meta > 0 || comp.nbt != null) writer.value(comp.meta);
            if (comp.nbt != null) writer.value(comp.nbt.toString());                           //NBT
        } else if (astack instanceof ComparableStack comp) {
            writer.value("item");                                                               //ITEM  identifier
            writer.value(BuiltInRegistries.ITEM.getKey(comp.toStack().getItem()).toString());   //item name
            if (comp.stacksize != 1 || comp.meta > 0) writer.value(comp.stacksize);             //stack size
            if (comp.meta > 0) writer.value(comp.meta);                                         //metadata
        }
        //} else if(astack instanceof OreDictStack) {
        //    OreDictStack ore = (OreDictStack) astack;
        //    writer.value("dict");														//DICT identifier
        //    writer.value(ore.name);														//dict name
        //    if(ore.stacksize != 1) writer.value(ore.stacksize);							//stacksize
        //}
        writer.endArray();
        writer.setIndent("  ");
    }

    public static ItemStack readItemStack(JsonArray array) {
        try {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(array.get(0).getAsString())); // namespace:path
            int stacksize = array.size() > 1 ? array.get(1).getAsInt() : 1;
            int meta = array.size() > 2 ? array.get(2).getAsInt() : 0;
            ItemStack stack = MetaHelper.newStack(item, stacksize, meta);
            if(array.size() > 3) ItemStackUtil.addNBTFromString(stack, array.get(3).getAsString());
            return stack;
        } catch(Exception ignored) { }
        NuclearTechMod.LOGGER.error("Error reading stack array {} - defaulting to BOTTLE_EMPTY item stack", array.toString());
        return ItemStack.EMPTY;
    }

    public static Pair<ItemStack, Float> readItemStackChance(JsonArray array) {
        try {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(array.get(0).getAsString())); // namespace:path
            int stacksize = array.size() > 2 ? array.get(1).getAsInt() : 1;
            int meta = array.size() > 3 ? array.get(2).getAsInt() : 0;

            ItemStack stack = MetaHelper.newStack(item, stacksize, meta);
            if(array.size() > 4) ItemStackUtil.addNBTFromString(stack, array.get(3).getAsString());
            float chance = array.get(array.size() - 1).getAsFloat();

            return new Pair<>(stack, chance);
        } catch(Exception ignored) { }
        NuclearTechMod.LOGGER.error("Error reading stack array {} - defaulting to NOTHING item!", array.toString());
        return new Pair<>(ItemStack.EMPTY, 1F);
    }

    public static ItemStack[] readItemStackArray(JsonArray array) {
        try {
            ItemStack[] items = new ItemStack[array.size()];
            for(int i = 0; i < items.length; i++) { items[i] = readItemStack((JsonArray) array.get(i)); }
            return items;
        } catch(Exception ignored) { }
        NuclearTechMod.LOGGER.error("Error reading stack array {}", array.toString());
        return new ItemStack[0];
    }

    public static Pair<ItemStack, Float>[] readItemStackArrayChance(JsonArray array) {
        try {
            Pair<ItemStack, Float>[] items = new Pair[array.size()];
            for(int i = 0; i < items.length; i++) { items[i] = readItemStackChance((JsonArray) array.get(i)); }
            return items;
        } catch(Exception ignored) { }
        NuclearTechMod.LOGGER.error("Error reading stack array " + array.toString());
        return new Pair[0];
    }

    public static void writeItemStack(ItemStack stack, JsonWriter writer) throws IOException {
        writer.beginArray();
        writer.setIndent("");
        writer.value(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());											                        //item name
        if(stack.getCount() != 1 || MetaHelper.getMeta(stack) != 0 || TagsUtil.hasCustomData(stack)) writer.value(stack.getCount());    //stack size
        if(MetaHelper.getMeta(stack) != 0 || TagsUtil.hasCustomData(stack)) writer.value(MetaHelper.getMeta(stack));				    //metadata
        if(TagsUtil.hasCustomData(stack)) writer.value(TagsUtil.getCustomData(stack).toString());								    //nbt
        writer.endArray();
        writer.setIndent("  ");
    }

    public static void writeItemStackChance(Pair<ItemStack, Float> stack, JsonWriter writer) throws IOException {
        writer.beginArray();
        writer.setIndent("");
        writer.value(BuiltInRegistries.ITEM.getKey(stack.getKey().getItem()).toString());											            //item name
        if(stack.getKey().getCount() != 1 || MetaHelper.getMeta(stack.getKey()) != 0 || TagsUtil.hasCustomData(stack.getKey()))
            writer.value(stack.getKey().getCount());                                                                                            //stack size
        if(MetaHelper.getMeta(stack.getKey()) != 0 || TagsUtil.hasCustomData(stack.getKey()))
            writer.value(MetaHelper.getMeta(stack.getKey()));				                                                                    //metadata
        if(TagsUtil.hasCustomData(stack.getKey()))
            writer.value(TagsUtil.getCustomData(stack.getKey()).toString());								                                //nbt
        writer.value(stack.value);																												//chance
        writer.endArray();
        writer.setIndent("  ");
    }

    public static FluidStack readFluidStack(JsonArray array) {
        try {
            FluidType type = Fluids.fromName(array.get(0).getAsString());
            int fill = array.get(1).getAsInt();
            int pressure = array.size() < 3 ? 0 : array.get(2).getAsInt();
            return new FluidStack(type, fill, pressure);
        } catch(Exception ex) { }
        NuclearTechMod.LOGGER.error("Error reading fluid array " + array.toString());
        return new FluidStack(Fluids.NONE, 0);
    }

    public static FluidStack[] readFluidArray(JsonArray array) {
        try {
            FluidStack[] fluids = new FluidStack[array.size()];
            for(int i = 0; i < fluids.length; i++) { fluids[i] = readFluidStack((JsonArray) array.get(i)); }
            return fluids;
        } catch(Exception ex) { }
        NuclearTechMod.LOGGER.error("Error reading fluid array " + array.toString());
        return new FluidStack[0];
    }

    public static void writeFluidStack(FluidStack stack, JsonWriter writer) throws IOException {
        writer.beginArray();
        writer.setIndent("");
        writer.value(stack.type.getUnlocalizedName());	//fluid type
        writer.value(stack.fill);			            //amount in mB
        if(stack.pressure != 0) writer.value(stack.pressure);
        writer.endArray();
        writer.setIndent("  ");
    }

}
