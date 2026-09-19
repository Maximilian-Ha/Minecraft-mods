package com.hbm.datagen;

import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.generic.BarbedWireBlock;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.BoltItem;
import com.hbm.items.CastPlateItem;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import com.hbm.items.machine.GearItem;
import com.hbm.items.component.NtmDataComponents;
import com.hbm.inventory.recipes.crafting.RBMKFuelDisassemblyRecipe;
import com.hbm.main.NuclearTechMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.Locale;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import java.util.concurrent.CompletableFuture;

public class NtmRecipeProvider extends RecipeProvider {

    public NtmRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {

        this.watzParts(recipeOutput);
        this.pileDevices(recipeOutput);
        this.hazmatGear(recipeOutput);
        this.armorMods(recipeOutput);
        this.oilChain(recipeOutput);


        /*
         * RBMK-Brennstaebe. Das Original stellt sie in der Werkbank her: ein leerer Stab und
         * acht Billets desselben Stoffs. Der leere Stab selbst ist ein Zirkoniumrohr um eine
         * leere Vierfachstange.
         *
         * NICHT UEBERNOMMEN: rbmk_fuel_test, den es im Original nur zum Ausprobieren gibt und
         * der auch dort kein Rezept hat.
         */
        /* ---- Runde 74: der Waffentisch und die Aufsaetze, die an die portierten Waffen passen ---- */

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.WEAPON_TABLE.get(), 1)
                .pattern("PPP")
                .pattern("TCT")
                .pattern("TST")
                .define('P', NtmItems.PLATE_GUNMETAL.get())
                .define('T', NtmItems.INGOT_STEEL.get())
                .define('C', Blocks.CRAFTING_TABLE)
                .define('S', NtmBlocks.BLOCK_STEEL.get())
                .unlockedBy("has_gunmetal", has(NtmItems.PLATE_GUNMETAL.get()))
                .save(recipeOutput);

        /*
         * Die vier Allgemeinaufsaetze. Das Original macht sie formlos; hier auch. Die Namen der
         * Rezepte tragen die Baustufe, weil formlose Rezepte mit gleichem Erzeugnis-Item sonst
         * kollidieren -- die Aufsaetze sind alle derselbe Gegenstand mit anderem Metadatum.
         */
        weaponModShapeless(recipeOutput, GunFactory.ModGeneric.IRON_DAMAGE,
                NtmItems.INGOT_GUNMETAL.get(), Items.IRON_INGOT, Items.IRON_INGOT, Items.IRON_INGOT, NtmItems.DUCTTAPE.get());
        weaponModShapeless(recipeOutput, GunFactory.ModGeneric.IRON_DURA,
                NtmItems.INGOT_GUNMETAL.get(), Items.IRON_INGOT, NtmItems.DUCTTAPE.get());
        weaponModShapeless(recipeOutput, GunFactory.ModGeneric.STEEL_DAMAGE,
                NtmItems.INGOT_GUNMETAL.get(), NtmItems.INGOT_STEEL.get(), NtmItems.INGOT_STEEL.get(), NtmItems.INGOT_STEEL.get(), NtmItems.DUCTTAPE.get());
        weaponModShapeless(recipeOutput, GunFactory.ModGeneric.STEEL_DURA,
                NtmItems.PLATE_GUNMETAL.get(), NtmItems.INGOT_STEEL.get(), NtmItems.DUCTTAPE.get());
        weaponModShapeless(recipeOutput, GunFactory.ModGeneric.DURA_DAMAGE,
                NtmItems.INGOT_GUNMETAL.get(), NtmItems.INGOT_DURA_STEEL.get(), NtmItems.INGOT_DURA_STEEL.get(), NtmItems.INGOT_DURA_STEEL.get(), NtmItems.DUCTTAPE.get());
        weaponModShapeless(recipeOutput, GunFactory.ModGeneric.DURA_DURA,
                NtmItems.PLATE_GUNMETAL.get(), NtmItems.INGOT_DURA_STEEL.get(), NtmItems.DUCTTAPE.get());
        weaponModShapeless(recipeOutput, GunFactory.ModGeneric.DESH_DAMAGE,
                NtmItems.INGOT_GUNMETAL.get(), NtmItems.INGOT_DESH.get(), NtmItems.INGOT_DESH.get(), NtmItems.INGOT_DESH.get(), NtmItems.DUCTTAPE.get());
        weaponModShapeless(recipeOutput, GunFactory.ModGeneric.DESH_DURA,
                NtmItems.PLATE_GUNMETAL.get(), NtmItems.INGOT_DESH.get(), NtmItems.DUCTTAPE.get());
        weaponModShapeless(recipeOutput, GunFactory.ModGeneric.WSTEEL_DAMAGE,
                NtmItems.INGOT_GUNMETAL.get(), NtmItems.INGOT_WEAPON_STEEL.get(), NtmItems.INGOT_WEAPON_STEEL.get(), NtmItems.INGOT_WEAPON_STEEL.get(), NtmItems.DUCTTAPE.get());
        weaponModShapeless(recipeOutput, GunFactory.ModGeneric.WSTEEL_DURA,
                NtmItems.PLATE_GUNMETAL.get(), NtmItems.INGOT_WEAPON_STEEL.get(), NtmItems.DUCTTAPE.get());

        /* Der Schalldaempfer: ein Stahlrohr zwischen zwei Lagen Kunststoff. */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                        MetaHelper.newStack(NtmItems.WEAPON_MOD_SPECIAL.get(), 1, GunFactory.ModSpecial.SILENCER.ordinal()))
                .pattern("P")
                .pattern("B")
                .pattern("P")
                .define('P', NtmItems.INGOT_PC.get())
                .define('B', NtmItems.PIPES_STEEL.get())
                .unlockedBy("has_steel_pipes", has(NtmItems.PIPES_STEEL.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("weapon_mod_special_silencer"));

        /* Die Saege: ein Buegel aus Stahlplatten an zwei Griffen, mit einem Blatt aus HSS. */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                        MetaHelper.newStack(NtmItems.WEAPON_MOD_SPECIAL.get(), 1, GunFactory.ModSpecial.SAW.ordinal()))
                .pattern("BBS")
                .pattern("BHS")
                .define('B', NtmItems.PLATE_STEEL.get())
                .define('S', Items.STICK)
                .define('H', NtmItems.PLATE_DURA_STEEL.get())
                .unlockedBy("has_dura_plate", has(NtmItems.PLATE_DURA_STEEL.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("weapon_mod_special_saw"));

        /* Das Saturnit-Gehaeuse der Uzi: Lauf, Verschluss und Mechanik, alles aus Saturnit. */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                        MetaHelper.newStack(NtmItems.WEAPON_MOD_SPECIAL.get(), 1, GunFactory.ModSpecial.SKIN_SATURNITE.ordinal()))
                .pattern("IPI")
                .pattern(" P ")
                .define('I', NtmItems.INGOT_SATURNITE.get())
                .define('P', NtmItems.PLATE_SATURNITE.get())
                .unlockedBy("has_saturnite", has(NtmItems.INGOT_SATURNITE.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("weapon_mod_special_skin_saturnite"));

        /* Die 240-mm-Granaten. Vier Ausfuehrungen; die W9 wird nicht gebaut, sie ist Fundstueck. */
        shell(recipeOutput, GunFactory.Ammo240Shell.STOCK, Blocks.TNT, NtmItems.SHELL_STEEL.get());
        shell(recipeOutput, GunFactory.Ammo240Shell.EXPLOSIVE, NtmItems.INGOT_C4.get(), NtmItems.SHELL_STEEL.get());
        shell(recipeOutput, GunFactory.Ammo240Shell.APFSDS_T, NtmItems.INGOT_TUNGSTEN.get(), NtmItems.INGOT_TUNGSTEN.get());
        shell(recipeOutput, GunFactory.Ammo240Shell.APFSDS_DU, NtmItems.INGOT_U238.get(), NtmItems.INGOT_U238.get());

        /* Die 20-mm-Patrone des Nahbereichsgeschuetzes: Blei, Treibladung, Messing. */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.AMMO_DGK.get(), 1)
                .pattern("LLL")
                .pattern("GGG")
                .pattern("CCC")
                .define('L', NtmItems.PLATE_LEAD.get())
                .define('G', NtmItems.CORDITE.get())
                .define('C', NtmItems.INGOT_COPPER.get())
                .unlockedBy("has_cordite", has(NtmItems.CORDITE.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("ammo_dgk"));

        /* Der Zielchip: ein Rechenwerk zwischen zwei Lagen Golddraht. */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.TURRET_CHIP.get(), 1)
                .pattern("WWW")
                .pattern("CPC")
                .pattern("WWW")
                .define('W', NtmItems.WIRE_GOLD.get())
                .define('P', NtmItems.INGOT_PC.get())
                .define('C', NtmItems.CIRCUIT_INTEGRATED_BOARD.get())
                .unlockedBy("has_circuit", has(NtmItems.CIRCUIT_INTEGRATED_BOARD.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("turret_chip"));

        /* Der Wachturm: Laufblech, Motor, Bildschirm, Rechenwerk und ein Stueck Geruest. */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.TURRET_SENTRY.get(), 1)
                .pattern("PPL")
                .pattern(" MD")
                .pattern(" SC")
                .define('P', NtmItems.PLATE_STEEL.get())
                .define('L', NtmItems.INGOT_GUNMETAL.get())
                .define('M', NtmItems.MOTOR.get())
                .define('D', NtmItems.CRT_DISPLAY.get())
                .define('S', NtmBlocks.STEEL_SCAFFOLD.get())
                .define('C', NtmItems.CIRCUIT_PRINTED_BOARD.get())
                .unlockedBy("has_motor", has(NtmItems.MOTOR.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("turret_sentry"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.RBMK_FUEL_EMPTY.get(), 1)
                .pattern("ZRZ")
                .pattern("Z Z")
                .pattern("ZRZ")
                .define('Z', NtmItems.INGOT_ZIRCONIUM.get())
                .define('R', NtmItems.ROD_QUAD_EMPTY.get())
                .unlockedBy("has_zirconium", has(NtmItems.INGOT_ZIRCONIUM.get()))
                .save(recipeOutput);

        /*
         * DIE RBMK-BLOECKE.
         *
         * Bis hierher hatte kein einziger RBMK-Block ein Werkbankrezept -- der ganze Zweig war
         * nur ueber den Kreativreiter erreichbar. Die Muster stehen wortgetreu wie im Original
         * (CraftingManager.java Z. 751-789 und 987-993).
         *
         * NICHT UEBERNOMMEN:
         * - rbmk_control_reasim und rbmk_control_reasim_auto. Im Original stehen sie im
         *   else-Zweig von if(!GeneralConfig.enable528) und sind damit NIE gleichzeitig mit
         *   rbmk_control / rbmk_control_auto registriert -- ihre Muster sind Zeichen fuer
         *   Zeichen dieselben. Rezept-JSONs koennen zur Laufzeit nicht umschalten; da
         *   enable528 voreingestellt aus ist, gilt hier der Normalfall. Die beiden
         *   ReaSim-Bauformen bleiben vorerst nur ueber den Kreativreiter erreichbar.
         *
         * NACHGETRAGEN IN RUNDE 93: der Ladeblock, der Verbindungsstab (im Original rbmk_tool)
         * und die neun Anzeige- und Bedientafeln. Sie standen hier als "noch nicht portiert" --
         * das stimmte fuer die Bloecke schon seit den Runden 43 bis 47 nicht mehr, nur ihre
         * Muster fehlten noch.
         */

        // Deckel und Bleiglasdeckel
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.RBMK_LID.get(), 4)
                .pattern("PPP").pattern("CCC").pattern("PPP")
                .define('P', NtmItems.PLATE_STEEL.get())
                .define('C', NtmBlocks.CONCRETE_ASBESTOS.get())
                .unlockedBy("has_plate_steel", has(NtmItems.PLATE_STEEL.get()))
                .save(recipeOutput);

        /*
         * Das Original registriert den Glasdeckel zweimal mit vertauschten Glasreihen, damit die
         * Reihenfolge egal ist. In 1.21 brauchen die beiden eigene Rezept-Kennungen.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.RBMK_LID_GLASS.get(), 4)
                .pattern("LLL").pattern("BBB").pattern("P P")
                .define('L', NtmBlocks.GLASS_LEAD.get())
                .define('B', NtmBlocks.GLASS_BORON.get())
                .define('P', NtmItems.PLATE_STEEL.get())
                .unlockedBy("has_glass_lead", has(NtmBlocks.GLASS_LEAD.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("rbmk_lid_glass_lead_top").toString());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.RBMK_LID_GLASS.get(), 4)
                .pattern("BBB").pattern("LLL").pattern("P P")
                .define('L', NtmBlocks.GLASS_LEAD.get())
                .define('B', NtmBlocks.GLASS_BORON.get())
                .define('P', NtmItems.PLATE_STEEL.get())
                .unlockedBy("has_glass_boron", has(NtmBlocks.GLASS_BORON.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("rbmk_lid_glass_boron_top").toString());

        // Die Leersaeule und ihr Rueckweg aus der Deko
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.DECO_RBMK.get(), 8)
                .pattern("R")
                .define('R', NtmBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(NtmBlocks.RBMK_BLANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.DECO_RBMK_SMOOTH.get(), 1)
                .pattern("R")
                .define('R', NtmBlocks.DECO_RBMK.get())
                .unlockedBy("has_deco_rbmk", has(NtmBlocks.DECO_RBMK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_BLANK.get(), 1)
                .pattern("RRR").pattern("R R").pattern("RRR")
                .define('R', NtmBlocks.DECO_RBMK.get())
                .unlockedBy("has_deco_rbmk", has(NtmBlocks.DECO_RBMK.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("rbmk_blank_from_deco").toString());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_BLANK.get(), 1)
                .pattern("RRR").pattern("R R").pattern("RRR")
                .define('R', NtmBlocks.DECO_RBMK_SMOOTH.get())
                .unlockedBy("has_deco_rbmk_smooth", has(NtmBlocks.DECO_RBMK_SMOOTH.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("rbmk_blank_from_deco_smooth").toString());

        // Graphitblock -- das Gegenstueck zu den neun Barren, und Zutat der vier moderierten Saeulen
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.BLOCK_GRAPHITE.get(), 1)
                .pattern("GGG").pattern("GGG").pattern("GGG")
                .define('G', NtmItems.INGOT_GRAPHITE.get())
                .unlockedBy("has_ingot_graphite", has(NtmItems.INGOT_GRAPHITE.get()))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NtmItems.INGOT_GRAPHITE.get(), 9)
                .requires(NtmBlocks.BLOCK_GRAPHITE.get())
                .unlockedBy("has_block_graphite", has(NtmBlocks.BLOCK_GRAPHITE.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("ingot_graphite_from_block").toString());

        // Borblock -- der Chicago Pile braucht ihn als Absorber, hier steht vorerst nur das Paar.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.BLOCK_BORON.get(), 1)
                .pattern("GGG").pattern("GGG").pattern("GGG")
                .define('G', NtmItems.INGOT_BORON.get())
                .unlockedBy("has_ingot_boron", has(NtmItems.INGOT_BORON.get()))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NtmItems.INGOT_BORON.get(), 9)
                .requires(NtmBlocks.BLOCK_BORON.get())
                .unlockedBy("has_block_boron", has(NtmBlocks.BLOCK_BORON.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("ingot_boron_from_block").toString());

        /*
         * Die Trommel des Brennstoffbeckens. Hier kuehlt heisser Abfall ab -- die einzige
         * Stelle, an der das geht.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_WASTE_DRUM.get(), 1)
                .pattern("LRL").pattern("BRB").pattern("LRL")
                .define('L', NtmItems.INGOT_LEAD.get())
                .define('B', Blocks.IRON_BARS)
                .define('R', NtmItems.ROD_QUAD_EMPTY.get())
                .unlockedBy("has_rod_quad_empty", has(NtmItems.ROD_QUAD_EMPTY.get()))
                .save(recipeOutput);

        /*
         * Der Numitron-Schaltkreis. Im Original eine Metadaten-Variante von ModItems.circuit,
         * im Port ein eigenes Item -- so haelt es der Port mit allen Schaltkreisen.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CIRCUIT_NUMITRON.get(), 3)
                .pattern("G").pattern("W").pattern("I")
                .define('G', Tags.Items.GLASS_PANES)
                .define('W', NtmItems.COIL_TUNGSTEN.get())
                .define('I', NtmItems.PLATE_COPPER.get())
                .unlockedBy("has_coil_tungsten", has(NtmItems.COIL_TUNGSTEN.get()))
                .save(recipeOutput);

        // Die passiven Saeulen
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_MODERATOR.get(), 1)
                .pattern(" G ").pattern("GRG").pattern(" G ")
                .define('G', NtmBlocks.BLOCK_GRAPHITE.get())
                .define('R', NtmBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(NtmBlocks.RBMK_BLANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_ABSORBER.get(), 1)
                .pattern("GGG").pattern("GRG").pattern("GGG")
                .define('G', NtmItems.INGOT_BORON.get())
                .define('R', NtmBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(NtmBlocks.RBMK_BLANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_REFLECTOR.get(), 1)
                .pattern("GGG").pattern("GRG").pattern("GGG")
                .define('G', NtmItems.NEUTRON_REFLECTOR.get())
                .define('R', NtmBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(NtmBlocks.RBMK_BLANK.get()))
                .save(recipeOutput);

        // Die Brennkanaele
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_ROD.get(), 1)
                .pattern("C").pattern("R").pattern("C")
                .define('C', NtmItems.SHELL_STEEL.get())
                .define('R', NtmBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(NtmBlocks.RBMK_BLANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_ROD_MOD.get(), 1)
                .pattern("BGB").pattern("GRG").pattern("BGB")
                .define('G', NtmBlocks.BLOCK_GRAPHITE.get())
                .define('R', NtmBlocks.RBMK_ROD.get())
                .define('B', NtmItems.NUGGET_BISMUTH.get())
                .unlockedBy("has_rbmk_rod", has(NtmBlocks.RBMK_ROD.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_ROD_REASIM.get(), 1)
                .pattern("ZCZ").pattern("ZRZ").pattern("ZCZ")
                .define('Z', NtmItems.INGOT_ZIRCONIUM.get())
                .define('C', NtmItems.SHELL_STEEL.get())
                .define('R', NtmBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(NtmBlocks.RBMK_BLANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_ROD_REASIM_MOD.get(), 1)
                .pattern("BGB").pattern("GRG").pattern("BGB")
                .define('G', NtmBlocks.BLOCK_GRAPHITE.get())
                .define('R', NtmBlocks.RBMK_ROD_REASIM.get())
                .define('B', ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "any_resistant_alloy")))
                .unlockedBy("has_rbmk_rod_reasim", has(NtmBlocks.RBMK_ROD_REASIM.get()))
                .save(recipeOutput);

        // Die Steuerstaebe
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_CONTROL.get(), 1)
                .pattern(" B ").pattern("GRG").pattern(" B ")
                .define('G', NtmItems.INGOT_GRAPHITE.get())
                .define('B', NtmItems.MOTOR.get())
                .define('R', NtmBlocks.RBMK_ABSORBER.get())
                .unlockedBy("has_rbmk_absorber", has(NtmBlocks.RBMK_ABSORBER.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_CONTROL_MOD.get(), 1)
                .pattern("BGB").pattern("GRG").pattern("BGB")
                .define('G', NtmBlocks.BLOCK_GRAPHITE.get())
                .define('R', NtmBlocks.RBMK_CONTROL.get())
                .define('B', NtmItems.NUGGET_BISMUTH.get())
                .unlockedBy("has_rbmk_control", has(NtmBlocks.RBMK_CONTROL.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_CONTROL_AUTO.get(), 1)
                .pattern("C").pattern("R").pattern("D")
                .define('C', NtmItems.CIRCUIT_MILITARY_GRADE_BOARD.get())
                .define('R', NtmBlocks.RBMK_CONTROL.get())
                .define('D', NtmItems.CRT_DISPLAY.get())
                .unlockedBy("has_rbmk_control", has(NtmBlocks.RBMK_CONTROL.get()))
                .save(recipeOutput);

        // Dampf, Waerme und Kuehlung
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_BOILER.get(), 1)
                .pattern("CPC").pattern("CRC").pattern("CPC")
                .define('C', NtmItems.PIPE_COPPER.get())
                .define('P', NtmItems.SHELL_COPPER.get())
                .define('R', NtmBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(NtmBlocks.RBMK_BLANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_HEATER.get(), 1)
                .pattern("CIC").pattern("PRP").pattern("CIC")
                .define('C', NtmItems.PIPE_COPPER.get())
                .define('I', NtmItems.INGOT_POLYMER.get())
                .define('P', NtmItems.SHELL_STEEL.get())
                .define('R', NtmBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(NtmBlocks.RBMK_BLANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_COOLER.get(), 1)
                .pattern("IGI").pattern("GCG").pattern("IGI")
                .define('I', NtmItems.PLATE_POLYMER.get())
                .define('G', NtmBlocks.STEEL_GRATE.get())
                .define('C', NtmBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(NtmBlocks.RBMK_BLANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_STEAM_INLET.get(), 1)
                .pattern("SCS").pattern("CBC").pattern("SCS")
                .define('S', NtmItems.INGOT_STEEL.get())
                .define('C', NtmItems.PLATE_IRON.get())
                .define('B', NtmItems.TANK_STEEL.get())
                .unlockedBy("has_tank_steel", has(NtmItems.TANK_STEEL.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_STEAM_OUTLET.get(), 1)
                .pattern("SCS").pattern("CBC").pattern("SCS")
                .define('S', NtmItems.INGOT_STEEL.get())
                .define('C', NtmItems.PLATE_COPPER.get())
                .define('B', NtmItems.TANK_STEEL.get())
                .unlockedBy("has_tank_steel", has(NtmItems.TANK_STEEL.get()))
                .save(recipeOutput);

        // Bestrahlung und Lager
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_OUTGASSER.get(), 1)
                .pattern("GHG").pattern("GRG").pattern("GTG")
                .define('G', NtmBlocks.STEEL_GRATE.get())
                .define('H', Items.HOPPER)
                .define('T', NtmItems.TANK_STEEL.get())
                .define('R', NtmBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(NtmBlocks.RBMK_BLANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_STORAGE.get(), 1)
                .pattern("C").pattern("R").pattern("C")
                .define('C', NtmBlocks.CRATE_STEEL.get())
                .define('R', NtmBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(NtmBlocks.RBMK_BLANK.get()))
                .save(recipeOutput);

        // Die beiden Pulte
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_CONSOLE.get(), 1)
                .pattern("BBB").pattern("DGD").pattern("DCD")
                .define('B', NtmItems.INGOT_BORON.get())
                .define('D', NtmBlocks.DECO_RBMK.get())
                .define('G', Tags.Items.GLASS_PANES)
                .define('C', NtmItems.CIRCUIT_ANALOG_BOARD.get())
                .unlockedBy("has_deco_rbmk", has(NtmBlocks.DECO_RBMK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_CRANE_CONSOLE.get(), 1)
                .pattern("BCD").pattern("DDD")
                .define('B', NtmItems.INGOT_BORON.get())
                .define('C', NtmItems.CIRCUIT_ANALOG_BOARD.get())
                .define('D', NtmBlocks.DECO_RBMK.get())
                .unlockedBy("has_deco_rbmk", has(NtmBlocks.DECO_RBMK.get()))
                .save(recipeOutput);

        /*
         * Runde 94: die Foerderbaender.
         *
         * ABWEICHUNG: im Original gibt es fuer Baender keine Werkbankrezepte -- dort stellt man
         * den Bandstab her, und der setzt die Baender. Der Stab kommt in einer eigenen Runde;
         * bis dahin sind die Muster des Stabes hier unmittelbar auf die Baender gelegt, damit
         * sie in der Ueberlebensrunde ueberhaupt erreichbar sind. Mengen und Zutaten sind die
         * des Originals (CraftingManager.java Z. 223-228).
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CONVEYOR.get(), 16)
                .pattern("LLL").pattern("I I").pattern("LLL")
                .define('L', Items.LEATHER)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("conveyor_from_leather").toString());

        /* Mit Gummi statt Leder kommen vierundsechzig Stueck heraus statt sechzehn. */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CONVEYOR.get(), 64)
                .pattern("LLL").pattern("I I").pattern("LLL")
                .define('L', NtmItems.INGOT_RUBBER.get())
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_ingot_rubber", has(NtmItems.INGOT_RUBBER.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("conveyor_from_rubber").toString());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CONVEYOR_DOUBLE.get(), 1)
                .pattern("CPC")
                .define('C', NtmBlocks.CONVEYOR.get())
                .define('P', NtmItems.PLATE_IRON.get())
                .unlockedBy("has_conveyor", has(NtmBlocks.CONVEYOR.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CONVEYOR_TRIPLE.get(), 1)
                .pattern("DPC")
                .define('D', NtmBlocks.CONVEYOR_DOUBLE.get())
                .define('P', NtmItems.PLATE_STEEL.get())
                .define('C', NtmBlocks.CONVEYOR.get())
                .unlockedBy("has_conveyor_double", has(NtmBlocks.CONVEYOR_DOUBLE.get()))
                .save(recipeOutput);

        /*
         * Runde 104: die vier Bandstaebe. Das Muster des ersten ist das des Bandes selbst
         * (CraftingManager.java Z. 223) -- im Original ist der Stab ja der Bandvorrat, und
         * beides entsteht aus demselben Griff.
         *
         * ABWEICHUNG: dort kommen sechzehn Staebe heraus, hier einer. Im Port ist der Stab ein
         * WERKZEUG, das Baender aus dem Rucksack legt und sich dabei nicht verbraucht; ihn
         * stapelweise herzustellen ergaebe keinen Sinn.
         *
         * ABWEICHUNG: die drei uebrigen Staebe entstehen aus dem ersten und dem, was ihr Band
         * vom einfachen unterscheidet -- Schmierstoff, Eisenblech, Stahlblech. Im Original
         * stehen dort dieselben Zutaten, nur mit acht Staeben statt einem.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CONVEYOR_WAND.get(), 1)
                .pattern("LLL").pattern("I I").pattern("LLL")
                .define('L', Items.LEATHER)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CONVEYOR_WAND_EXPRESS.get(), 1)
                .pattern("CL")
                .define('C', NtmItems.CONVEYOR_WAND.get())
                .define('L', DataComponentIngredient.of(false, NtmDataComponents.META, Fluids.LUBRICANT.getID(), NtmItems.FLUID_TANK_FULL.get()))
                .unlockedBy("has_conveyor_wand", has(NtmItems.CONVEYOR_WAND.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CONVEYOR_WAND_DOUBLE.get(), 1)
                .pattern("CP")
                .define('C', NtmItems.CONVEYOR_WAND.get())
                .define('P', NtmItems.PLATE_IRON.get())
                .unlockedBy("has_conveyor_wand", has(NtmItems.CONVEYOR_WAND.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CONVEYOR_WAND_TRIPLE.get(), 1)
                .pattern("CP")
                .define('C', NtmItems.CONVEYOR_WAND_DOUBLE.get())
                .define('P', NtmItems.PLATE_STEEL.get())
                .unlockedBy("has_conveyor_wand", has(NtmItems.CONVEYOR_WAND.get()))
                .save(recipeOutput);

        /*
         * Der Einleger. Im Original gibt es ihn in drei Gueten aus drei Gehaeusematerialien
         * (CraftingManager.java Z. 873); uebernommen ist die erste, aus Stahlgehaeusen. Statt
         * des Bandstabs steht das Band selbst im Muster -- der Stab kommt spaeter.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CRANE_INSERTER.get(), 1)
                .pattern("CCC").pattern("C C").pattern("CBC")
                .define('C', NtmItems.PLATE_STEEL.get())
                .define('B', NtmBlocks.CONVEYOR.get())
                .unlockedBy("has_conveyor", has(NtmBlocks.CONVEYOR.get()))
                .save(recipeOutput);

        /*
         * Der Auszieher. Wie der Einleger, aber mit einem Antrieb in der Mitte -- er muss ja
         * aus der Maschine ziehen. Muster aus CraftingManager.java Z. 875.
         *
         * ABWEICHUNG: dort steht ein Druckluftkolben; den Gegenstand gibt es im Port noch
         * nicht. An seiner Stelle steht der Motor, das naechstliegende vorhandene Teil.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CRANE_EXTRACTOR.get(), 1)
                .pattern("CCC").pattern("CPC").pattern("CBC")
                .define('C', NtmItems.PLATE_STEEL.get())
                .define('P', NtmItems.MOTOR.get())
                .define('B', NtmBlocks.CONVEYOR.get())
                .unlockedBy("has_conveyor", has(NtmBlocks.CONVEYOR.get()))
                .save(recipeOutput);

        /*
         * Runde 99: der Greifer. Oben offen -- er greift ja durch --, zwei Antriebe an den
         * Seiten. Muster aus CraftingManager.java Z. 878.
         *
         * ABWEICHUNG: dieselbe wie beim Auszieher -- Motor statt Druckluftkolben, Band statt
         * Bandstab.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CRANE_GRABBER.get(), 1)
                .pattern("C C").pattern("P P").pattern("CBC")
                .define('C', NtmItems.PLATE_STEEL.get())
                .define('P', NtmItems.MOTOR.get())
                .define('B', NtmBlocks.CONVEYOR.get())
                .unlockedBy("has_conveyor", has(NtmBlocks.CONVEYOR.get()))
                .save(recipeOutput);

        /*
         * Runde 100: Packer und Entpacker. Muster aus CraftingManager.java Z. 881-882.
         *
         * Der Packer aus Brettern mit einem Antrieb -- eine Kiste zu schnueren braucht Holz.
         * Der Entpacker aus Stoecken mit einer Schere: er schneidet auf, was der Packer
         * zugebunden hat. Beides steht so im Original.
         *
         * ABWEICHUNG: Motor statt Druckluftkolben und Band statt Bandstab, wie bei den
         * uebrigen Kranmaschinen.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CRANE_BOXER.get(), 1)
                .pattern("WWW").pattern("WPW").pattern("CCC")
                .define('W', ItemTags.PLANKS)
                .define('P', NtmItems.MOTOR.get())
                .define('C', NtmBlocks.CONVEYOR.get())
                .unlockedBy("has_conveyor", has(NtmBlocks.CONVEYOR.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CRANE_UNBOXER.get(), 1)
                .pattern("WWW").pattern("WPW").pattern("CCC")
                .define('W', Items.STICK)
                .define('P', Items.SHEARS)
                .define('C', NtmBlocks.CONVEYOR.get())
                .unlockedBy("has_conveyor", has(NtmBlocks.CONVEYOR.get()))
                .save(recipeOutput);

        /*
         * Runde 101: der Verteiler. Muster aus CraftingManager.java Z. 883. Er braucht als
         * einzige Kranmaschine eine Schaltung -- er entscheidet, statt nur zu bewegen.
         *
         * ABWEICHUNG: Motor statt Druckluftkolben, wie bei den uebrigen.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CRANE_ROUTER.get(), 1)
                .pattern("PIP").pattern("ICI").pattern("PIP")
                .define('P', NtmItems.MOTOR.get())
                .define('I', NtmItems.PLATE_POLYMER.get())
                .define('C', NtmItems.CIRCUIT_INTEGRATED_BOARD.get())
                .unlockedBy("has_conveyor", has(NtmBlocks.CONVEYOR.get()))
                .save(recipeOutput);

        /*
         * Runde 102: der Portionierer. Muster aus CraftingManager.java Z. 887.
         *
         * ABWEICHUNG: EnumCircuitType.CHIP des Originals ist im Port der Mikrochip, und statt
         * des Bandstabs steht wie ueberall das Band.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CRANE_PARTITIONER.get(), 1)
                .pattern(" M ").pattern("BCB")
                .define('M', NtmItems.CIRCUIT_MICROCHIP.get())
                .define('B', NtmBlocks.CONVEYOR.get())
                .define('C', NtmBlocks.CRATE_STEEL.get())
                .unlockedBy("has_conveyor", has(NtmBlocks.CONVEYOR.get()))
                .save(recipeOutput);

        /*
         * Runde 103: die Weiche. Muster aus CraftingManager.java Z. 885.
         *
         * ABWEICHUNG: Motor statt Druckluftkolben und die Vakuumroehre fuer
         * EnumCircuitType.VACUUM_TUBE.
         */
        /*
         * Runde 105: Absauger und Geblaese. Muster aus CraftingManager.java Z. 930-931.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_DRAIN.get(), 1)
                .pattern("PPP").pattern("T  ").pattern("PPP")
                .define('P', NtmItems.PLATE_STEEL.get())
                .define('T', NtmItems.TANK_STEEL.get())
                .unlockedBy("has_tank_steel", has(NtmItems.TANK_STEEL.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_INTAKE.get(), 1)
                .pattern("GGG").pattern("PMP").pattern("PTP")
                .define('G', NtmBlocks.STEEL_GRATE.get())
                .define('P', NtmItems.PLATE_STEEL.get())
                .define('M', NtmItems.MOTOR.get())
                .define('T', NtmItems.TANK_STEEL.get())
                .unlockedBy("has_tank_steel", has(NtmItems.TANK_STEEL.get()))
                .save(recipeOutput);

        /*
         * Runde 112: der Strommelder. Muster aus CraftingManager.java Z. 238. ABWEICHUNG: der
         * feine Draht aus Fortschrittslegierung fehlt dem Port; hier steht Kupferdraht.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_DETECTOR.get(), 1)
                .pattern("IRI").pattern("CTC").pattern("IRI")
                .define('I', NtmItems.PLATE_POLYMER.get())
                .define('R', Items.REDSTONE)
                .define('C', NtmItems.WIRE_COPPER.get())
                .define('T', NtmItems.COIL_TUNGSTEN.get())
                .unlockedBy("has_coil_tungsten", has(NtmItems.COIL_TUNGSTEN.get()))
                .save(recipeOutput);

        /*
         * Runde 107: der Trichter. Muster aus CraftingManager.java Z. 284.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_FUNNEL.get(), 1)
                .pattern("S S").pattern("SRS").pattern(" S ")
                .define('S', NtmItems.INGOT_STEEL.get())
                .define('R', Items.REDSTONE)
                .unlockedBy("has_ingot_steel", has(NtmItems.INGOT_STEEL.get()))
                .save(recipeOutput);

        /*
         * Runde 106: der Selbstbauer. Muster aus CraftingManager.java Z. 283.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_AUTOCRAFTER.get(), 1)
                .pattern("SCS").pattern("MWM").pattern("SCS")
                .define('S', NtmItems.PLATE_STEEL.get())
                .define('C', NtmItems.CIRCUIT_VACUUM_TUBE.get())
                .define('M', NtmItems.MOTOR.get())
                .define('W', Blocks.CRAFTING_TABLE)
                .unlockedBy("has_motor", has(NtmItems.MOTOR.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CRANE_SPLITTER.get(), 1)
                .pattern("III").pattern("PCP").pattern("III")
                .define('P', NtmItems.MOTOR.get())
                .define('I', NtmItems.INGOT_STEEL.get())
                .define('C', NtmItems.CIRCUIT_VACUUM_TUBE.get())
                .unlockedBy("has_conveyor", has(NtmBlocks.CONVEYOR.get()))
                .save(recipeOutput);

        /*
         * Runde 98: Tueren, Leitern, Zaun, Falltuer und Schmalspurgleis. Alle Muster wortgetreu
         * aus CraftingManager.java (Z. 489, 637-639, 678-680, 795, 797).
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, NtmBlocks.DOOR_METAL.get(), 1)
                .pattern("II").pattern("SS").pattern("II")
                .define('I', NtmItems.PLATE_IRON.get())
                .define('S', NtmItems.PLATE_STEEL.get())
                .unlockedBy("has_plate_steel", has(NtmItems.PLATE_STEEL.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, NtmBlocks.DOOR_OFFICE.get(), 1)
                .pattern("II").pattern("SS").pattern("II")
                .define('I', ItemTags.PLANKS)
                .define('S', NtmItems.PLATE_IRON.get())
                .unlockedBy("has_plate_iron", has(NtmItems.PLATE_IRON.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, NtmBlocks.DOOR_BUNKER.get(), 1)
                .pattern("II").pattern("SS").pattern("II")
                .define('I', NtmItems.PLATE_STEEL.get())
                .define('S', NtmItems.PLATE_LEAD.get())
                .unlockedBy("has_plate_lead", has(NtmItems.PLATE_LEAD.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, NtmBlocks.FENCE_METAL.get(), 6)
                .pattern("BIB").pattern("BIB")
                .define('B', Items.IRON_BARS)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_iron_bars", has(Items.IRON_BARS))
                .save(recipeOutput);

        /* Feld und Pfosten lassen sich jederzeit ineinander umlegen. */
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, NtmBlocks.FENCE_METAL_POST.get(), 1)
                .requires(NtmBlocks.FENCE_METAL.get())
                .unlockedBy("has_fence_metal", has(NtmBlocks.FENCE_METAL.get()))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, NtmBlocks.FENCE_METAL.get(), 1)
                .requires(NtmBlocks.FENCE_METAL_POST.get())
                .unlockedBy("has_fence_metal_post", has(NtmBlocks.FENCE_METAL_POST.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("fence_metal_from_post").toString());

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, NtmBlocks.LADDER_STEEL.get(), 8)
                .pattern("LLL").pattern("LSL").pattern("LLL")
                .define('L', Items.LADDER)
                .define('S', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_ingot_steel", has(NtmItems.INGOT_STEEL.get()))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, NtmBlocks.TRAPDOOR_STEEL.get(), 1)
                .requires(ItemTags.WOODEN_TRAPDOORS)
                .requires(NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_ingot_steel", has(NtmItems.INGOT_STEEL.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, NtmBlocks.RAIL_NARROW.get(), 64)
                .pattern("S S").pattern("S S").pattern("S S")
                .define('S', NtmBlocks.STEEL_BEAM.get())
                .unlockedBy("has_steel_beam", has(NtmBlocks.STEEL_BEAM.get()))
                .save(recipeOutput);

        /*
         * ABWEICHUNG: die Kette hat im Original kein Rezept -- sie kommt nur in Verliesen vor.
         * Hier bekommt sie eines, weil der Port ihre Verliese noch nicht baut und sie sonst
         * gar nicht erreichbar waere. Muster nach der Kette von Minecraft, aber aus Stahl.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, NtmBlocks.DUNGEON_CHAIN.get(), 1)
                .pattern("N").pattern("S").pattern("N")
                .define('N', Items.IRON_NUGGET)
                .define('S', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_ingot_steel", has(NtmItems.INGOT_STEEL.get()))
                .save(recipeOutput);

        /* Acht Baender und ein Tank Schmierstoff ergeben acht Schnellbaender. */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CONVEYOR_EXPRESS.get(), 8)
                .pattern("CCC").pattern("CLC").pattern("CCC")
                .define('C', NtmBlocks.CONVEYOR.get())
                .define('L', DataComponentIngredient.of(false, NtmDataComponents.META, Fluids.LUBRICANT.getID(), NtmItems.FLUID_TANK_FULL.get()))
                .unlockedBy("has_conveyor", has(NtmBlocks.CONVEYOR.get()))
                .save(recipeOutput);

        /*
         * Runde 93: der Rohranschluss unter der Saeule und die neun Anzeige- und Bedientafeln.
         * Bis hierher waren sie nur ueber den Kreativreiter erreichbar -- die Bloecke standen
         * laengst, ihre Muster fehlten. Alle wortgetreu aus CraftingManager.java Z. 770-782.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_LOADER.get(), 1)
                .pattern("SCS").pattern("CBC").pattern("SCS")
                .define('S', NtmItems.PLATE_STEEL.get())
                .define('C', NtmItems.INGOT_COPPER.get())
                .define('B', NtmItems.TANK_STEEL.get())
                .unlockedBy("has_tank_steel", has(NtmItems.TANK_STEEL.get()))
                .save(recipeOutput);

        /* Die Blankotafel ist die Grundlage aller uebrigen acht. */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_DISPLAY_BLANK.get(), 8)
                .pattern("B").pattern("D")
                .define('B', NtmItems.INGOT_BORON.get())
                .define('D', NtmBlocks.CONCRETE_ASBESTOS.get())
                .unlockedBy("has_ingot_boron", has(NtmItems.INGOT_BORON.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_DISPLAY.get(), 1)
                .pattern("C").pattern("B")
                .define('C', NtmItems.CRT_DISPLAY.get())
                .define('B', NtmBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(NtmBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(recipeOutput);

        /*
         * Die Sender tragen eine Sendefackel, die Empfaenger eine Empfangsfackel -- daran
         * haengt, in welche Richtung die Tafel spricht. Tastenfeld, Hebel und Terminal senden,
         * Zeiger, Numitron, Graph und Leuchte empfangen.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_KEYPAD.get(), 1)
                .pattern("R").pattern("C").pattern("B")
                .define('R', NtmBlocks.RADIO_TORCH_SENDER.get())
                .define('C', NtmItems.CIRCUIT_VACUUM_TUBE.get())
                .define('B', NtmBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(NtmBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_GAUGE.get(), 1)
                .pattern("R").pattern("C").pattern("B")
                .define('R', NtmBlocks.RADIO_TORCH_RECEIVER.get())
                .define('C', NtmItems.CIRCUIT_VACUUM_TUBE.get())
                .define('B', NtmBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(NtmBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_NUMITRON.get(), 1)
                .pattern(" R ").pattern("CCC").pattern(" B ")
                .define('R', NtmBlocks.RADIO_TORCH_RECEIVER.get())
                .define('C', NtmItems.CIRCUIT_NUMITRON.get())
                .define('B', NtmBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(NtmBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_GRAPH.get(), 1)
                .pattern("R").pattern("C").pattern("B")
                .define('R', NtmBlocks.RADIO_TORCH_RECEIVER.get())
                .define('C', NtmItems.CRT_DISPLAY.get())
                .define('B', NtmBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(NtmBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_LEVER.get(), 1)
                .pattern("R").pattern("C").pattern("B")
                .define('R', NtmBlocks.RADIO_TORCH_SENDER.get())
                .define('C', NtmItems.INGOT_COPPER.get())
                .define('B', NtmBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(NtmBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_INDICATOR.get(), 1)
                .pattern("R").pattern("C").pattern("B")
                .define('R', NtmBlocks.RADIO_TORCH_RECEIVER.get())
                .define('C', NtmItems.COIL_TUNGSTEN.get())
                .define('B', NtmBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(NtmBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RBMK_TERMINAL.get(), 1)
                .pattern("R ").pattern("CD").pattern("B ")
                .define('R', NtmBlocks.RADIO_TORCH_SENDER.get())
                .define('C', NtmItems.CIRCUIT_ANALOG_BOARD.get())
                .define('D', NtmItems.CRT_DISPLAY.get())
                .define('B', NtmBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(NtmBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(recipeOutput);

        /*
         * Der Verbindungsstab. Ohne ihn liesse sich kein Reaktorpult auf den Reaktor
         * ausrichten -- er ist damit der Schluessel zum ganzen Zweig, und er hat bis jetzt
         * gefehlt. Muster aus ToolRecipes.java Z. 133; im Original heisst er rbmk_tool.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, NtmItems.RBMK_LINK.get(), 1)
                .pattern(" A ").pattern(" IA").pattern("I  ")
                .define('A', NtmItems.INGOT_LEAD.get())
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_ingot_lead", has(NtmItems.INGOT_LEAD.get()))
                .save(recipeOutput);

        // Zerlegen abgekuehlter Brennstaebe in acht Pellets -- die Bedingungen stecken im Rezept selbst.
        SpecialRecipeBuilder.special(RBMKFuelDisassemblyRecipe::new)
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("rbmk_fuel_disassembly").toString());

        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_UEU.get(), NtmItems.BILLET_URANIUM.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_MEU.get(), NtmItems.BILLET_URANIUM_FUEL.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_HEU233.get(), NtmItems.BILLET_U233.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_HEU235.get(), NtmItems.BILLET_U235.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_UZH.get(), NtmItems.BILLET_UZH.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_THMEU.get(), NtmItems.BILLET_THORIUM_FUEL.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_MOX.get(), NtmItems.BILLET_MOX_FUEL.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_LEP.get(), NtmItems.BILLET_PLUTONIUM_FUEL.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_MEP.get(), NtmItems.BILLET_PU_MIX.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_HEP.get(), NtmItems.BILLET_PU239.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_HEP241.get(), NtmItems.BILLET_PU241.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_LEA.get(), NtmItems.BILLET_AMERICIUM_FUEL.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_MEA.get(), NtmItems.BILLET_AM_MIX.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_HEA241.get(), NtmItems.BILLET_AM241.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_HEA242.get(), NtmItems.BILLET_AM242.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_MEN.get(), NtmItems.BILLET_NEPTUNIUM_FUEL.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_HEN.get(), NtmItems.BILLET_NEPTUNIUM.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_PO210BE.get(), NtmItems.BILLET_PO210BE.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_RA226BE.get(), NtmItems.BILLET_RA226BE.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_PU238BE.get(), NtmItems.BILLET_PU238BE.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_LEAUS.get(), NtmItems.BILLET_AUSTRALIUM_LESSER.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_HEAUS.get(), NtmItems.BILLET_AUSTRALIUM_GREATER.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_BALEFIRE.get(), NtmItems.EGG_BALEFIRE_SHARD.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_LES.get(), NtmItems.BILLET_LES.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_MES.get(), NtmItems.BILLET_SCHRABIDIUM_FUEL.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_HES.get(), NtmItems.BILLET_HES.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_BALEFIRE_GOLD.get(), NtmItems.BILLET_BALEFIRE_GOLD.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_FLASHLEAD.get(), NtmItems.BILLET_FLASHLEAD.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_ZFB_BISMUTH.get(), NtmItems.BILLET_ZFB_BISMUTH.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_ZFB_PU241.get(), NtmItems.BILLET_ZFB_PU241.get());
        rbmkRod(recipeOutput, NtmItems.RBMK_FUEL_ZFB_AM_MIX.get(), NtmItems.BILLET_ZFB_AM_MIX.get());

        /* Der Digamma-Stab entsteht nicht aus Billets, sondern aus einem Balefire-Stab und einem Digamma-Teilchen. */
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NtmItems.RBMK_FUEL_DRX.get(), 1)
                .requires(NtmItems.RBMK_FUEL_BALEFIRE.get())
                .requires(NtmItems.PARTICLE_DIGAMMA.get())
                .unlockedBy("has_digamma", has(NtmItems.PARTICLE_DIGAMMA.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.EGG_BALEFIRE.get(), 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', NtmItems.EGG_BALEFIRE_SHARD.get())
                .unlockedBy("has_balefire_shard", has(NtmItems.EGG_BALEFIRE_SHARD.get()))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NtmItems.EGG_BALEFIRE_SHARD.get(), 9)
                .requires(NtmItems.EGG_BALEFIRE.get())
                .unlockedBy("has_balefire_egg", has(NtmItems.EGG_BALEFIRE.get()))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.STRING, 3)
                .requires(NtmBlocks.PLANT_FLOWER.get())
                .unlockedBy("has_plant_flower", has(NtmBlocks.PLANT_FLOWER.get()))
                .save(recipeOutput);
        // this 2 below, to delete later
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.GOLD_BLOCK, 1)
                .pattern(" B ")
                .pattern("BGB")
                .pattern(" B ")
                .define('B', DataComponentIngredient.of(false, NtmDataComponents.META, BoltItem.Type.DURA_STEEL.meta, NtmItems.BOLT.get()))
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_bolt", has(NtmItems.BOLT.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "example_gold_block_from_dura_steel_bolts"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.GOLD_BLOCK, 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', DataComponentIngredient.of(false, NtmDataComponents.META, CastPlateItem.Type.IRON.ordinal(), NtmItems.CAST_PLATE.get()))
                .unlockedBy("has_iron_cast_plate", has(NtmItems.CAST_PLATE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "gold_block_from_iron_cast_plates"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CAST_PLATE.get(), 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "iron_cast_plate_from_iron_ingots"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetaHelper.newStack(NtmBlocks.BARBED_WIRE.asItem(), 1, BarbedWireBlock.BarbedWireType.FIRE.ordinal()))
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', DataComponentIngredient.of(false, NtmDataComponents.META, BoltItem.Type.TUNGSTEN.meta, NtmItems.BOLT.get()))
                .unlockedBy("has_tungsten_bolt", has(NtmItems.BOLT.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "flaming_barbed_wire_from_tungsten_bolts"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetaHelper.newStack(NtmItems.BOLT.get(), 1, BoltItem.Type.TUNGSTEN.meta))
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', DataComponentIngredient.of(false, NtmDataComponents.META, BarbedWireBlock.BarbedWireType.FIRE.ordinal(), NtmBlocks.BARBED_WIRE.asItem()))
                .unlockedBy("has_flaming_barbed_wire", has(NtmBlocks.BARBED_WIRE.asItem()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "tungsten_bolt_from_flaming_barbed_wire"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.UPGRADE_TEMPLATE.get(), 1)
                .pattern("ACA")
                .pattern("BDB")
                .pattern("ACA")
                .define('A', NtmItems.WIRE_COPPER.get())
                .define('B', NtmItems.PLATE_POLYMER.get())
                .define('C', NtmItems.PLATE_IRON.get())
                .define('D', NtmItems.CIRCUIT_ANALOG_BOARD.get())
                .unlockedBy("has_plate_iron", has(NtmItems.PLATE_IRON.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "upgrade_template_from_iron"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.UPGRADE_TEMPLATE.get(), 1)
                .pattern("ACA")
                .pattern("BDB")
                .pattern("ACA")
                .define('A', NtmItems.WIRE_COPPER.get())
                .define('B', NtmItems.PLATE_POLYMER.get())
                .define('C', NtmItems.INGOT_POLYMER.get())
                .define('D', NtmItems.CIRCUIT_INTEGRATED_BOARD.get())
                .unlockedBy("has_polymer", has(NtmItems.INGOT_POLYMER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "upgrade_template_from_polymer"));
        /*
         * Die sechs Sonderaufwertungen des Bergbaulasers, Runde 118. Vier davon bauen
         * AUFEINANDER AUF -- der Schredder braucht den Schmelzer, die Zentrifuge den Schredder,
         * der Kristallisator die Zentrifuge. So steht es im Original, und es ist der Grund, warum
         * man sie nicht alle nebeneinander hat: wer den Kristallisator will, hat die anderen drei
         * darin verbaut.
         *
         * ABWEICHUNGEN: der Transformator des Originals ist im Port der gewoehnliche
         * Transformatorblock. Das Zentrifugenelement gibt es nicht; an seiner Stelle steht die
         * Duraplatte, aus der das Original es zum groessten Teil baut. Der fortgeschrittene
         * Schaltkreis wird der integrierte, wie in allen Runden davor.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.UPGRADE_NULLIFIER.get(), 1)
                .pattern("SPS")
                .pattern("PUP")
                .pattern("SPS")
                .define('S', NtmItems.PLATE_STEEL.get())
                .define('P', NtmItems.POWDER_FIRE.get())
                .define('U', NtmItems.UPGRADE_TEMPLATE.get())
                .unlockedBy("has_upgrade_template", has(NtmItems.UPGRADE_TEMPLATE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "upgrade_nullifier"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.UPGRADE_SCREM.get(), 1)
                .pattern("SUS")
                .pattern("SCS")
                .pattern("SUS")
                .define('S', NtmItems.PLATE_STEEL.get())
                .define('U', NtmItems.UPGRADE_TEMPLATE.get())
                .define('C', NtmItems.CRYSTAL_XEN.get())
                .unlockedBy("has_crystal_xen", has(NtmItems.CRYSTAL_XEN.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "upgrade_screm"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.UPGRADE_SMELTER.get(), 1)
                .pattern("PHP")
                .pattern("CUC")
                .pattern("DTD")
                .define('P', NtmItems.PLATE_COPPER.get())
                .define('H', Items.HOPPER)
                .define('C', NtmItems.COIL_TUNGSTEN.get())
                .define('U', NtmItems.UPGRADE_TEMPLATE.get())
                .define('D', NtmItems.COIL_COPPER.get())
                .define('T', NtmBlocks.TRANSFORMER.get())
                .unlockedBy("has_upgrade_template", has(NtmItems.UPGRADE_TEMPLATE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "upgrade_smelter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.UPGRADE_SHREDDER.get(), 1)
                .pattern("PHP")
                .pattern("CUC")
                .pattern("DTD")
                .define('P', NtmItems.MOTOR.get())
                .define('H', Items.HOPPER)
                .define('C', NtmItems.BLADES_TITANIUM.get())
                .define('U', NtmItems.UPGRADE_SMELTER.get())
                .define('D', NtmItems.PLATE_TITANIUM.get())
                .define('T', NtmBlocks.TRANSFORMER.get())
                .unlockedBy("has_upgrade_smelter", has(NtmItems.UPGRADE_SMELTER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "upgrade_shredder"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.UPGRADE_CENTRIFUGE.get(), 1)
                .pattern("PHP")
                .pattern("PUP")
                .pattern("DTD")
                .define('P', NtmItems.PLATE_DURA_STEEL.get())
                .define('H', Items.HOPPER)
                .define('U', NtmItems.UPGRADE_SHREDDER.get())
                .define('D', NtmItems.INGOT_POLYMER.get())
                .define('T', NtmBlocks.TRANSFORMER.get())
                .unlockedBy("has_upgrade_shredder", has(NtmItems.UPGRADE_SHREDDER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "upgrade_centrifuge"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.UPGRADE_CRYSTALLIZER.get(), 1)
                .pattern("PHP")
                .pattern("CUC")
                .pattern("DTD")
                .define('P', DataComponentIngredient.of(false, NtmDataComponents.META, Fluids.PEROXIDE.getID(), NtmItems.FLUID_BARREL_FULL.get()))
                .define('H', NtmItems.CIRCUIT_INTEGRATED_BOARD.get())
                .define('C', NtmBlocks.BARREL_STEEL.get())
                .define('U', NtmItems.UPGRADE_CENTRIFUGE.get())
                .define('D', NtmItems.MOTOR.get())
                .define('T', NtmBlocks.TRANSFORMER.get())
                .unlockedBy("has_upgrade_centrifuge", has(NtmItems.UPGRADE_CENTRIFUGE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "upgrade_crystallizer"));

        /*
         * Die schnelle Gaszentrifuge, Runde 115. Unveraendert aus dem Original: Goldspulen an
         * den Ecken, Niob oben und unten in der Mitte, Kautschuk an den Seiten, Motor unten,
         * Vorlage in der Mitte.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.UPGRADE_GC_SPEED.get(), 1)
                .pattern("GNG")
                .pattern("RUR")
                .pattern("GMG")
                .define('G', NtmItems.COIL_GOLD.get())
                .define('N', NtmItems.INGOT_NIOBIUM.get())
                .define('R', NtmItems.INGOT_RUBBER.get())
                .define('M', NtmItems.MOTOR.get())
                .define('U', NtmItems.UPGRADE_TEMPLATE.get())
                .unlockedBy("has_upgrade_template", has(NtmItems.UPGRADE_TEMPLATE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "upgrade_gc_speed"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CIRCUIT_PRINTED_BOARD.get(), 1)
                .pattern(" A ")
                .pattern(" B ")
                .pattern("   ")
                .define('B', NtmItems.PLATE_COPPER.get())
                .define('A', NtmItems.PLATE_POLYMER.get())
                .unlockedBy("has_insulator", has(NtmItems.PLATE_POLYMER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "circuit_printed_board_from_copper_plate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CIRCUIT_PRINTED_BOARD.get(), 4)
                .pattern(" A ")
                .pattern(" B ")
                .pattern("   ")
                .define('B', NtmItems.PLATE_GOLD.get())
                .define('A', NtmItems.PLATE_POLYMER.get())
                .unlockedBy("has_insulator", has(NtmItems.PLATE_POLYMER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "circuit_printed_board_from_gold_plate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CIRCUIT_CAPACITOR.get(), 2)
                .pattern("   ")
                .pattern("ABA")
                .pattern("C C")
                .define('B', NtmItems.POWDER_ALUMINIUM.get())
                .define('A', NtmItems.PLATE_POLYMER.get())
                .define('C', NtmItems.WIRE_COPPER.get())
                .unlockedBy("has_insulator", has(NtmItems.PLATE_POLYMER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "circuit_capacitor_from_aluminium_powder_and_copper_wire"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CIRCUIT_CAPACITOR.get(), 2)
                .pattern("   ")
                .pattern("ABA")
                .pattern("C C")
                .define('B', NtmItems.POWDER_ALUMINIUM.get())
                .define('A', NtmItems.PLATE_POLYMER.get())
                .define('C', NtmItems.WIRE_ALUMINIUM.get())
                .unlockedBy("has_insulator", has(NtmItems.PLATE_POLYMER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "circuit_capacitor_from_aluminium_powder_and_aluminium_wire"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CIRCUIT_CAPACITOR.get(), 1)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('B', NtmItems.NUGGET_NIOBIUM.get())
                .define('A', NtmItems.PLATE_POLYMER.get())
                .define('C', NtmItems.WIRE_COPPER.get())
                .unlockedBy("has_insulator", has(NtmItems.PLATE_POLYMER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "circuit_capacitor_from_niobium_nugget_and_copper_wire"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CIRCUIT_CAPACITOR.get(), 1)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('B', NtmItems.FRAGMENT_NIOBIUM.get())
                .define('A', NtmItems.PLATE_POLYMER.get())
                .define('C', NtmItems.WIRE_COPPER.get())
                .unlockedBy("has_insulator", has(NtmItems.PLATE_POLYMER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "circuit_capacitor_from_niobium_fragment_and_copper_wire"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CIRCUIT_CAPACITOR.get(), 1)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('B', NtmItems.NUGGET_NIOBIUM.get())
                .define('A', NtmItems.PLATE_POLYMER.get())
                .define('C', NtmItems.WIRE_ALUMINIUM.get())
                .unlockedBy("has_insulator", has(NtmItems.PLATE_POLYMER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "circuit_capacitor_from_niobium_nugget_and_aluminium_wire"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CIRCUIT_CAPACITOR.get(), 1)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('B', NtmItems.FRAGMENT_NIOBIUM.get())
                .define('A', NtmItems.PLATE_POLYMER.get())
                .define('C', NtmItems.WIRE_ALUMINIUM.get())
                .unlockedBy("has_insulator", has(NtmItems.PLATE_POLYMER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "circuit_capacitor_from_niobium_fragment_and_aluminium_wire"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CIRCUIT_VACUUM_TUBE.get(), 1)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('B', NtmItems.WIRE_TUNGSTEN.get())
                .define('A', Items.GLASS_PANE)
                .define('C', NtmItems.PLATE_POLYMER.get())
                .unlockedBy("has_insulator", has(NtmItems.PLATE_POLYMER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "circuit_vacuum_tube_from_tungsten_wire"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CIRCUIT_VACUUM_TUBE.get(), 1)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('B', NtmItems.WIRE_CARBON.get())
                .define('A', Items.GLASS_PANE)
                .define('C', NtmItems.PLATE_POLYMER.get())
                .unlockedBy("has_insulator", has(NtmItems.PLATE_POLYMER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "circuit_vacuum_tube_from_carbon_wire"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.MOTOR.get(), 2)
                .pattern(" A ")
                .pattern("BCB")
                .pattern("BDB")
                .define('A', NtmItems.WIRE_RED_COPPER.get())
                .define('B', NtmItems.PLATE_IRON.get())
                .define('C', NtmItems.COIL_COPPER.get())
                .define('D', NtmItems.COIL_COPPER_RING.get())
                .unlockedBy("has_insulator", has(NtmItems.PLATE_POLYMER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "motor_from_iron_plates"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.MOTOR.get(), 2)
                .pattern(" A ")
                .pattern("BCB")
                .pattern(" D ")
                .define('A', NtmItems.WIRE_RED_COPPER.get())
                .define('B', NtmItems.PLATE_STEEL.get())
                .define('C', NtmItems.COIL_COPPER.get())
                .define('D', NtmItems.COIL_COPPER_RING.get())
                .unlockedBy("has_insulator", has(NtmItems.PLATE_POLYMER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "motor_from_steel_plates"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.PLATE_POLYMER.get(), 16)
                .pattern("   ")
                .pattern("AA ")
                .pattern("   ")
                .define('A', NtmItems.INGOT_FIBERGLASS.get())
                .unlockedBy("has_ingot_fiberglass", has(NtmItems.INGOT_FIBERGLASS.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "plate_polymer_from_fiberglass"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.PLATE_POLYMER.get(), 16)
                .pattern("   ")
                .pattern("AA ")
                .pattern("   ")
                .define('A', NtmItems.INGOT_ASBESTOS.get())
                .unlockedBy("has_ingot_asbestos", has(NtmItems.INGOT_ASBESTOS.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "plate_polymer_from_asbestos"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.PLATE_POLYMER.get(), 4)
                .pattern("   ")
                .pattern("AA ")
                .pattern("   ")
                .define('A', Items.BRICK)
                .unlockedBy("has_brick", has(Items.BRICK))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "plate_polymer_from_bricks"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.PLATE_POLYMER.get(), 4)
                .pattern("   ")
                .pattern("ABA")
                .pattern("   ")
                .define('A', Items.STRING)
                .define('B', Items.WHITE_WOOL)
                .unlockedBy("has_string", has(Items.STRING))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "plate_polymer_from_string_and_wool"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.PLATE_POLYMER.get(), 8)
                .pattern("   ")
                .pattern("AA ")
                .pattern("   ")
                .define('A', NtmItems.INGOT_POLYMER.get())
                .unlockedBy("has_ingot_polymer", has(NtmItems.INGOT_POLYMER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "plate_polymer_from_polymer"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.PLATE_POLYMER.get(), 8)
                .pattern("   ")
                .pattern("AA ")
                .pattern("   ")
                .define('A', NtmItems.INGOT_BAKELITE.get())
                .unlockedBy("has_ingot_bakelite", has(NtmItems.INGOT_BAKELITE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "plate_polymer_from_bakelite"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.PLATE_POLYMER.get(), 8)
                .pattern("   ")
                .pattern("AA ")
                .pattern("   ")
                .define('A', NtmItems.INGOT_RUBBER.get())
                .unlockedBy("has_ingot_rubber", has(NtmItems.INGOT_RUBBER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "plate_polymer_from_rubber"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.PLATE_POLYMER.get(), 8)
                .pattern("   ")
                .pattern("AA ")
                .pattern("   ")
                .define('A', NtmItems.INGOT_BIORUBBER.get())
                .unlockedBy("has_ingot_biorubber", has(NtmItems.INGOT_BIORUBBER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "plate_polymer_from_biorubber"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.PIPES_STEEL.get(), 1)
                .pattern(" A ")
                .pattern(" A ")
                .pattern(" A ")
                .define('A', NtmBlocks.BLOCK_STEEL.get())
                .unlockedBy("has_ingot_steel", has(NtmItems.INGOT_STEEL.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "1"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CIRCUIT_TANTALIUM_CAPACITOR.get(), 1)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', NtmItems.PLATE_POLYMER.get())
                .define('B', NtmItems.NUGGET_TANTALIUM.get())
                .define('C', NtmItems.WIRE_COPPER.get())
                .unlockedBy("has_nugget_tantalium", has(NtmItems.NUGGET_TANTALIUM.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "2"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CIRCUIT_TANTALIUM_CAPACITOR.get(), 1)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', NtmItems.PLATE_POLYMER.get())
                .define('B', NtmItems.NUGGET_TANTALIUM.get())
                .define('C', NtmItems.WIRE_ALUMINIUM.get())
                .unlockedBy("has_nugget_tantalium", has(NtmItems.NUGGET_TANTALIUM.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "3"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CIRCUIT_CONTROL_UNIT_CASING.get(), 1)
                .pattern("AAA")
                .pattern("BCC")
                .pattern("AAA")
                .define('A', NtmItems.INGOT_POLYMER.get())
                .define('B', NtmItems.CRT_DISPLAY.get())
                .define('C', NtmItems.CIRCUIT_PRINTED_BOARD.get())
                .unlockedBy("has_crt_display", has(NtmItems.CRT_DISPLAY.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "4"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CIRCUIT_CONTROL_UNIT_CASING.get(), 1)
                .pattern("AAA")
                .pattern("BCC")
                .pattern("AAA")
                .define('A', NtmItems.INGOT_BAKELITE.get())
                .define('B', NtmItems.CRT_DISPLAY.get())
                .define('C', NtmItems.CIRCUIT_PRINTED_BOARD.get())
                .unlockedBy("has_crt_display", has(NtmItems.CRT_DISPLAY.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "5"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CRT_DISPLAY.get(), 4)
                .pattern(" C ")
                .pattern("ADA")
                .pattern(" B ")
                .define('A', NtmItems.PLATE_STEEL.get())
                .define('B', NtmItems.CIRCUIT_VACUUM_TUBE.get())
                .define('C', NtmItems.POWDER_ALUMINIUM.get())
                .define('D', Items.GLASS_PANE)
                .unlockedBy("has_vacuum_tube", has(NtmItems.CIRCUIT_VACUUM_TUBE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "6"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CELL_EMPTY.get(), 6)
                .pattern(" A ")
                .pattern("D D")
                .pattern(" A ")
                .define('A', NtmItems.PLATE_STEEL.get())
                .define('D', Items.GLASS_PANE)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "7"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_WOOD_BURNER.get(), 1)
                .pattern("AAA")
                .pattern("BDB")
                .pattern("C C")
                .define('A', NtmItems.PLATE_STEEL.get())
                .define('B', NtmItems.COIL_COPPER.get())
                .define('C', Items.IRON_INGOT)
                .define('D', Items.FURNACE)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "8"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_BATTERY_SOCKET.get(), 1)
                .pattern("A A")
                .pattern("A A")
                .pattern("ABA")
                .define('A', NtmItems.PLATE_POLYMER.get())
                .define('B', NtmItems.COIL_COPPER.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "9"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_BATTERY_SOCKET.get(), 1)
                .pattern("   ")
                .pattern("ABA")
                .pattern("   ")
                .define('A', NtmItems.PLATE_STEEL.get())
                .define('B', NtmItems.INGOT_RED_COPPER.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "10"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.RING_STARMETAL.get(), 1)
                .pattern(" A ")
                .pattern("A A")
                .pattern(" A ")
                .define('A', NtmItems.INGOT_STARMETAL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "11"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.INGOT_COPPER.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_COPPER.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "12"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.INGOT_GRAPHITE.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_CARBON.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "13"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.GOLD_INGOT, 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_GOLD.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "14"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.INGOT_SCHRABIDIUM.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_SCHRABIDIUM.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "15"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.INGOT_TUNGSTEN.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_TUNGSTEN.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "16"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.INGOT_ALUMINIUM.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_ALUMINIUM.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "17"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.INGOT_RED_COPPER.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_RED_COPPER.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "18"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.INGOT_MAGNETIZED_TUNGSTEN.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_MAGNETIZED_TUNGSTEN.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "19"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.COIL_MAGNETIZED_TUNGSTEN.get(), 1)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_MAGNETIZED_TUNGSTEN.get())
                .define('B', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "20"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.COIL_MAGNETIZED_TUNGSTEN.get(), 1)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_MAGNETIZED_TUNGSTEN.get())
                .define('B', Items.IRON_INGOT)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "21"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.COIL_COPPER.get(), 1)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_RED_COPPER.get())
                .define('B', Items.IRON_INGOT)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "22"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.COIL_COPPER.get(), 1)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_RED_COPPER.get())
                .define('B', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "23"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.COIL_GOLD.get(), 1)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_GOLD.get())
                .define('B', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "24"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.COIL_GOLD.get(), 1)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_GOLD.get())
                .define('B', Items.IRON_INGOT)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "25"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.COIL_GOLD_RING.get(), 2)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', NtmItems.COIL_GOLD.get())
                .define('B', NtmItems.PLATE_IRON.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "26"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.COIL_GOLD_RING.get(), 2)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', NtmItems.COIL_GOLD.get())
                .define('B', NtmItems.PLATE_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "27"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.COIL_COPPER_RING.get(), 2)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', NtmItems.COIL_COPPER.get())
                .define('B', NtmItems.PLATE_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "28"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.COIL_COPPER_RING.get(), 2)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', NtmItems.COIL_COPPER.get())
                .define('B', NtmItems.PLATE_IRON.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "29"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.COIL_TUNGSTEN.get(), 1)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_TUNGSTEN.get())
                .define('B', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "30"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.COIL_TUNGSTEN.get(), 1)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', NtmItems.WIRE_TUNGSTEN.get())
                .define('B', Items.IRON_INGOT)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "31"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.TANK_STEEL.get(), 2)
                .pattern("ABA")
                .pattern("A A")
                .pattern("ABA")
                .define('A', NtmItems.PLATE_STEEL.get())
                .define('B', NtmItems.PLATE_TITANIUM.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "32"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CATALYST_CLAY.get(), 1)
                .pattern("   ")
                .pattern("AB ")
                .pattern("   ")
                .define('A', NtmItems.POWDER_IRON.get())
                .define('B', Items.CLAY)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "33"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.DEUTERIUM_FILTER.get(), 1)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', NtmItems.INGOT_TCALLOY.get())
                .define('B', NtmItems.SULFUR.get())
                .define('C', NtmItems.CATALYST_CLAY.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "34"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.DEUTERIUM_FILTER.get(), 1)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', NtmItems.INGOT_CDALLOY.get())
                .define('B', NtmItems.SULFUR.get())
                .define('C', NtmItems.CATALYST_CLAY.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "35"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.FINS_FLAT.get(), 1)
                .pattern("BA ")
                .pattern("AA ")
                .pattern("BA ")
                .define('A', NtmItems.PLATE_STEEL.get())
                .define('B', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "36"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.FINS_SMALL_STEEL.get(), 1)
                .pattern(" AA")
                .pattern("ABB")
                .pattern(" AA")
                .define('A', NtmItems.PLATE_STEEL.get())
                .define('B', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "37"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.FINS_BIG_STEEL.get(), 1)
                .pattern(" AB")
                .pattern("BBB")
                .pattern(" AB")
                .define('A', NtmItems.PLATE_STEEL.get())
                .define('B', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "38"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.FINS_TRI_STEEL.get(), 1)
                .pattern(" AB")
                .pattern("BBC")
                .pattern(" AB")
                .define('A', NtmItems.PLATE_STEEL.get())
                .define('B', NtmItems.INGOT_STEEL.get())
                .define('C', NtmBlocks.BLOCK_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "39"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.FINS_QUAD_TITANIUM.get(), 1)
                .pattern(" AA")
                .pattern("BBB")
                .pattern(" AA")
                .define('A', NtmItems.PLATE_TITANIUM.get())
                .define('B', NtmItems.INGOT_TITANIUM.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "40"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.SPHERE_STEEL.get(), 1)
                .pattern("ABA")
                .pattern("B B")
                .pattern("ABA")
                .define('A', NtmItems.PLATE_STEEL.get())
                .define('B', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "41"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.PEDESTAL_STEEL.get(), 1)
                .pattern("A A")
                .pattern("A A")
                .pattern("BBB")
                .define('A', NtmItems.PLATE_STEEL.get())
                .define('B', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "42"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.BLADE_TITANIUM.get(), 2)
                .pattern("BA ")
                .pattern("BA ")
                .pattern("BB ")
                .define('A', NtmItems.PLATE_TITANIUM.get())
                .define('B', NtmItems.INGOT_TITANIUM.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "43"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.TURBINE_TITANIUM.get(), 1)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', NtmItems.BLADE_TITANIUM.get())
                .define('B', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "44"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.TURBINE_TUNGSTEN.get(), 1)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', NtmItems.BLADE_TUNGSTEN.get())
                .define('B', NtmItems.INGOT_DURA_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "45"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.FLYWHEEL_BERYLLIUM.get(), 1)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', DataComponentIngredient.of(false, NtmDataComponents.META, CastPlateItem.Type.IRON.ordinal(), NtmItems.CAST_PLATE.get()))
                .define('B', NtmBlocks.BLOCK_BERYLLIUM.get())
                .define('C', NtmItems.PIPE_DURA_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "46"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.BLADES_STEEL.get(), 1)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', NtmItems.PLATE_STEEL.get())
                .define('B', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "47"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.BLADES_TITANIUM.get(), 1)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', NtmItems.PLATE_TITANIUM.get())
                .define('B', NtmItems.INGOT_TITANIUM.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "48"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.BLADES_DESH.get(), 1)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', NtmItems.PLATE_DESH.get())
                .define('B', NtmItems.BLADES_TITANIUM.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "49"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.DUCTTAPE.get(), 4)
                .pattern(" C ")
                .pattern(" B ")
                .pattern(" A ")
                .define('A', Items.SLIME_BALL)
                .define('B', Items.PAPER)
                .define('C', Items.STRING)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "50"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.TOOTHPICKS.get(), 3)
                .pattern("A  ")
                .pattern("AA ")
                .pattern("   ")
                .define('A', Items.STICK)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "51"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.PLANT_ITEM.get(), 1)
                .pattern("AA ")
                .pattern("A  ")
                .pattern("   ")
                .define('A', Items.STRING)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "52"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.PLANT_ITEM.get(), 4)
                .pattern(" A ")
                .pattern(" A ")
                .pattern(" A ")
                .define('A', NtmBlocks.PLANT_FLOWER.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "53"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetaHelper.newStack(NtmBlocks.BARBED_WIRE.asItem(), 16, BarbedWireBlock.BarbedWireType.STANDARD.ordinal()))
                .pattern("ABA")
                .pattern("B B")
                .pattern("ABA")
                .define('A', NtmItems.WIRE_STEEL.get())
                .define('B', Items.IRON_INGOT)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "54"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetaHelper.newStack(NtmBlocks.BARBED_WIRE.asItem(), 8, BarbedWireBlock.BarbedWireType.FIRE.ordinal()))
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', DataComponentIngredient.of(false, NtmDataComponents.META, BarbedWireBlock.BarbedWireType.STANDARD.ordinal(), NtmBlocks.BARBED_WIRE.asItem()))
                .define('B', NtmItems.POWDER_FIRE)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "55"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetaHelper.newStack(NtmBlocks.BARBED_WIRE.asItem(), 8, BarbedWireBlock.BarbedWireType.POISON.ordinal()))
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', DataComponentIngredient.of(false, NtmDataComponents.META, BarbedWireBlock.BarbedWireType.STANDARD.ordinal(), NtmBlocks.BARBED_WIRE.asItem()))
                .define('B', NtmItems.POWDER_POISON)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "56"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetaHelper.newStack(NtmBlocks.BARBED_WIRE.asItem(), 8, BarbedWireBlock.BarbedWireType.ACID.ordinal()))
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', DataComponentIngredient.of(false, NtmDataComponents.META, BarbedWireBlock.BarbedWireType.STANDARD.ordinal(), NtmBlocks.BARBED_WIRE.asItem()))
                .define('B', DataComponentIngredient.of(false, NtmDataComponents.META, Fluids.PEROXIDE.getID(), NtmItems.FLUID_TANK_FULL.get()))
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "57"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetaHelper.newStack(NtmBlocks.BARBED_WIRE.asItem(), 8, BarbedWireBlock.BarbedWireType.WITHER.ordinal()))
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', DataComponentIngredient.of(false, NtmDataComponents.META, BarbedWireBlock.BarbedWireType.STANDARD.ordinal(), NtmBlocks.BARBED_WIRE.asItem()))
                .define('B', Items.WITHER_SKELETON_SKULL)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "58"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetaHelper.newStack(NtmBlocks.BARBED_WIRE.asItem(), 4, BarbedWireBlock.BarbedWireType.ULTRADEATH.ordinal()))
                .pattern("ACA")
                .pattern("CBC")
                .pattern("ACA")
                .define('A', DataComponentIngredient.of(false, NtmDataComponents.META, BarbedWireBlock.BarbedWireType.STANDARD.ordinal(), NtmBlocks.BARBED_WIRE.asItem()))
                .define('B', NtmItems.NUCLEAR_WASTE)
                .define('C', NtmItems.POWDER_YELLOWCAKE.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "59"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetaHelper.newStack(NtmItems.BOLT.get(), 16, BoltItem.Type.TUNGSTEN.meta))
                .pattern(" A ")
                .pattern(" A ")
                .pattern("   ")
                .define('A', NtmItems.INGOT_TUNGSTEN.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "60"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetaHelper.newStack(NtmItems.BOLT.get(), 16, BoltItem.Type.LEAD.meta))
                .pattern(" A ")
                .pattern(" A ")
                .pattern("   ")
                .define('A', NtmItems.INGOT_LEAD.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "61"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetaHelper.newStack(NtmItems.BOLT.get(), 16, BoltItem.Type.STEEL.meta))
                .pattern(" A ")
                .pattern(" A ")
                .pattern("   ")
                .define('A', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "62"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetaHelper.newStack(NtmItems.BOLT.get(), 16, BoltItem.Type.DURA_STEEL.meta))
                .pattern(" A ")
                .pattern(" A ")
                .pattern("   ")
                .define('A', NtmItems.INGOT_DURA_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "63"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.REACHER.get(), 1)
                .pattern("CAC")
                .pattern("B B")
                .pattern("C C")
                .define('A', NtmItems.INGOT_TUNGSTEN.get())
                .define('B', NtmItems.INGOT_RUBBER.get())
                .define('C', DataComponentIngredient.of(false, NtmDataComponents.META, BoltItem.Type.TUNGSTEN.meta, NtmItems.BOLT.get()))
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "64"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.OIL_DETECTOR.get(), 1)
                .pattern("A B")
                .pattern("ADB")
                .pattern("CCC")
                .define('A', NtmItems.WIRE_GOLD.get())
                .define('B', NtmItems.INGOT_COPPER.get())
                .define('C', NtmItems.PLATE_STEEL.get())
                .define('D', NtmItems.CIRCUIT_ANALOG_BOARD.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "65"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_PRESS.get(), 1)
                .pattern("ADA")
                .pattern("ACA")
                .pattern("ABA")
                .define('A', Items.IRON_INGOT)
                .define('B', Items.IRON_BLOCK)
                .define('C', Items.PISTON)
                .define('D', Items.FURNACE)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "66"));

        // Originalrezept aus 1.7.10 (CraftingManager, machine_electric_furnace_off):
        // BBB / WFW / RRR  mit B=Beryllium-Barren, W=Kupferplatte, F=Ofen, R=Wolframspule
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_ELECTRIC_FURNACE.get(), 1)
                .pattern("BBB")
                .pattern("WFW")
                .pattern("RRR")
                .define('B', NtmItems.INGOT_BERYLLIUM.get())
                .define('W', NtmItems.PLATE_COPPER.get())
                .define('F', Items.FURNACE)
                .define('R', NtmItems.COIL_TUNGSTEN.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "machine_electric_furnace"));

        // Grosses Zahnrad, beide Varianten 1:1 aus CraftingManager.java Z. 916-917
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetaHelper.newStack(NtmItems.GEAR_LARGE.get(), 1, GearItem.GearType.IRON.ordinal()))
                .pattern("III").pattern("ICI").pattern("III")
                .define('I', NtmItems.PLATE_IRON.get())
                .define('C', NtmItems.INGOT_COPPER.get())
                .unlockedBy("has_plate_iron", has(NtmItems.PLATE_IRON.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "gear_large_iron"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetaHelper.newStack(NtmItems.GEAR_LARGE.get(), 1, GearItem.GearType.STEEL.ordinal()))
                .pattern("III").pattern("ICI").pattern("III")
                .define('I', NtmItems.PLATE_STEEL.get())
                .define('C', NtmItems.INGOT_TITANIUM.get())
                .unlockedBy("has_plate_steel", has(NtmItems.PLATE_STEEL.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "gear_large_steel"));

        // Saegeblatt (Original CraftingManager): III / ICI / III
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.SAWBLADE.get(), 1)
                .pattern("III")
                .pattern("ICI")
                .pattern("III")
                .define('I', NtmItems.PLATE_STEEL.get())
                .define('C', Items.IRON_INGOT)
                .unlockedBy("has_plate_steel", has(NtmItems.PLATE_STEEL.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "sawblade"));

        // ---- Nachgereichte Bloecke: Stahltraeger und Gneisstein ----

        // Original CraftingManager Z. 458 bzw. 463 -- zwei Wege zum selben Ergebnis.
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, NtmBlocks.STEEL_BEAM.get(), 8)
                .pattern("S").pattern("S").pattern("S")
                .define('S', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_ingot_steel", has(NtmItems.INGOT_STEEL.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "steel_beam_from_ingot"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, NtmBlocks.STEEL_BEAM.get(), 8)
                .pattern("S").pattern("S").pattern("S")
                .define('S', NtmBlocks.STEEL_SCAFFOLD.get())
                .unlockedBy("has_steel_scaffold", has(NtmBlocks.STEEL_SCAFFOLD.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "steel_beam_from_scaffold"));

        // Original CraftingManager Z. 293. Bisher fehlte dem Heliostatspiegel das Rezept,
        // weil steel_beam im Port nicht existierte.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.SOLAR_MIRROR.get(), 3)
                .pattern("AAA")
                .pattern(" B ")
                .pattern("SSS")
                .define('A', NtmItems.PLATE_ALUMINIUM.get())
                .define('B', NtmBlocks.STEEL_BEAM.get())
                .define('S', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_steel_beam", has(NtmBlocks.STEEL_BEAM.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "solar_mirror"));

        // ---- Runde 13: die beiden HE/RF-Wandler ----

        // Original CraftingManager Z. 252/253: "RRR" / "WWW" / "III".
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_CONVERTER_HE_RF.get(), 1)
                .pattern("RRR")
                .pattern("WWW")
                .pattern("III")
                .define('R', NtmItems.CIRCUIT_CAPACITOR.get())
                .define('W', Items.REDSTONE)
                .define('I', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_ingot_steel", has(NtmItems.INGOT_STEEL.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "machine_converter_he_rf"));

        // MINGRADE.wireFine() ist WIRE_RED_COPPER, siehe die Anmerkung beim ummantelten Draht.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_CONVERTER_RF_HE.get(), 1)
                .pattern("RRR")
                .pattern("WWW")
                .pattern("III")
                .define('R', Items.REDSTONE)
                .define('W', NtmItems.WIRE_RED_COPPER.get())
                .define('I', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_wire_red_copper", has(NtmItems.WIRE_RED_COPPER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "machine_converter_rf_he"));

        // ---- Runde 10: Stahlgitter ----

        // Original CraftingManager Z. 465: "SS" / "SS" aus Stahltraegern.
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, NtmBlocks.STEEL_GRATE.get(), 4)
                .pattern("SS")
                .pattern("SS")
                .define('S', NtmBlocks.STEEL_BEAM.get())
                .unlockedBy("has_steel_beam", has(NtmBlocks.STEEL_BEAM.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "steel_grate"));

        // ---- Runde 7: restliche Netzbauteile und Kondensatoren ----

        // Original CraftingManager Z. 237: " Q " / "CAC" / " Q "
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CABLE_DIODE.get(), 1)
                .pattern(" Q ")
                .pattern("CAC")
                .pattern(" Q ")
                .define('Q', NtmItems.NUGGET_SILICON.get())
                .define('C', NtmBlocks.RED_CABLE.get())
                .define('A', NtmItems.INGOT_ALUMINIUM.get())
                .unlockedBy("has_red_cable", has(NtmBlocks.RED_CABLE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "cable_diode"));

        // Original CraftingManager Z. 236
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CABLE_DETECTOR.get(), 1)
                .pattern("S")
                .pattern("W")
                .define('S', Items.REDSTONE)
                .define('W', NtmBlocks.RED_WIRE_COATED.get())
                .unlockedBy("has_red_wire_coated", has(NtmBlocks.RED_WIRE_COATED.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "cable_detector"));

        // Original CraftingManager Z. 242, formlos
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NtmBlocks.RED_CABLE_GAUGE.get(), 1)
                .requires(NtmBlocks.RED_WIRE_COATED.get())
                .requires(NtmItems.INGOT_STEEL.get())
                .requires(NtmItems.CIRCUIT_VACUUM_TUBE.get())
                .unlockedBy("has_red_wire_coated", has(NtmBlocks.RED_WIRE_COATED.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "red_cable_gauge"));

        // Original CraftingManager Z. 574, formlos. Das bemalbare Rohr des Originals
        // heisst im Port fluid_duct_neo.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NtmBlocks.FLUID_DUCT_GAUGE.get(), 1)
                .requires(NtmBlocks.FLUID_DUCT_NEO.get())
                .requires(NtmItems.INGOT_STEEL.get())
                .requires(NtmItems.CIRCUIT_VACUUM_TUBE.get())
                .unlockedBy("has_fluid_duct_neo", has(NtmBlocks.FLUID_DUCT_NEO.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "fluid_duct_gauge"));

        // Original CraftingManager Z. 842: "SIS" / "ICI" / "SIS".
        // CU.plateCast() gibt es im Port nicht, wie schon beim Elektroofen auf PLATE_COPPER verengt.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_CONDENSER.get(), 1)
                .pattern("SIS")
                .pattern("ICI")
                .pattern("SIS")
                .define('S', NtmItems.INGOT_STEEL.get())
                .define('I', NtmItems.PLATE_IRON.get())
                .define('C', NtmItems.PLATE_COPPER.get())
                .unlockedBy("has_ingot_steel", has(NtmItems.INGOT_STEEL.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "machine_condenser"));

        // ---- Runde 5: Stromnetz (Originalrezepte aus 1.7.10, CraftingManager Z. 229-249) ----

        // Original: R = MINGRADE.wireFine(). Das ist WIRE_RED_COPPER und keine Verengung --
        // wire_fine ist im Original ein Autogen-Item ueber Mats, und seine Mingrade-Variante
        // heisst dort wire_red_copper (ModItems.java:2767).
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RED_WIRE_COATED.get(), 16)
                .pattern("WRW")
                .pattern("RIR")
                .pattern("WRW")
                .define('W', NtmItems.PLATE_POLYMER.get())
                .define('I', NtmItems.INGOT_RED_COPPER.get())
                .define('R', NtmItems.WIRE_RED_COPPER.get())
                .unlockedBy("has_wire_red_copper", has(NtmItems.WIRE_RED_COPPER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "red_wire_coated"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CABLE_SWITCH.get(), 1)
                .pattern("S")
                .pattern("W")
                .define('S', Items.LEVER)
                .define('W', NtmBlocks.RED_WIRE_COATED.get())
                .unlockedBy("has_red_wire_coated", has(NtmBlocks.RED_WIRE_COATED.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "cable_switch"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RED_CONNECTOR.get(), 4)
                .pattern("C")
                .pattern("I")
                .pattern("S")
                .define('C', NtmItems.COIL_COPPER.get())
                .define('I', NtmItems.PLATE_POLYMER.get())
                .define('S', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_coil_copper", has(NtmItems.COIL_COPPER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "red_connector"));

        // Original: S = ANY_RESISTANTALLOY.ingot(), also Technetium- oder Cadmiumlegierung.
        // Beide liegen im Port, die Verengung auf INGOT_DURA_STEEL aus Runde 5 war unnoetig.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RED_CONNECTOR_SUPER.get(), 2)
                .pattern("CCC")
                .pattern("III")
                .pattern(" S ")
                .define('C', NtmItems.COIL_COPPER.get())
                .define('I', NtmItems.PLATE_POLYMER.get())
                .define('S', ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "any_resistant_alloy")))
                .unlockedBy("has_coil_copper", has(NtmItems.COIL_COPPER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "red_connector_super"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RED_PYLON.get(), 4)
                .pattern("CWC")
                .pattern("PWP")
                .pattern(" S ")
                .define('C', NtmItems.COIL_COPPER.get())
                .define('W', ItemTags.PLANKS)
                .define('P', NtmItems.PLATE_POLYMER.get())
                .define('S', Items.COBBLESTONE)
                .unlockedBy("has_coil_copper", has(NtmItems.COIL_COPPER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "red_pylon"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RED_PYLON_STEEL.get(), 4)
                .pattern("CWC")
                .pattern("PWP")
                .pattern(" S ")
                .define('C', NtmItems.COIL_COPPER.get())
                .define('W', NtmItems.PIPE_STEEL.get())
                .define('P', NtmItems.PLATE_POLYMER.get())
                .define('S', Items.COBBLESTONE)
                .unlockedBy("has_pipe_steel", has(NtmItems.PIPE_STEEL.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "red_pylon_steel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RED_PYLON_MEDIUM_WOOD.get(), 2)
                .pattern("CCW")
                .pattern("IIW")
                .pattern("  S")
                .define('C', NtmItems.COIL_COPPER.get())
                .define('W', ItemTags.PLANKS)
                .define('I', NtmItems.PLATE_POLYMER.get())
                .define('S', Items.COBBLESTONE)
                .unlockedBy("has_coil_copper", has(NtmItems.COIL_COPPER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "red_pylon_medium_wood"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NtmBlocks.RED_PYLON_MEDIUM_WOOD_TRANSFORMER.get(), 1)
                .requires(NtmBlocks.RED_PYLON_MEDIUM_WOOD.get())
                .requires(NtmItems.PLATE_POLYMER.get())
                .requires(NtmItems.COIL_COPPER.get())
                .unlockedBy("has_red_pylon_medium_wood", has(NtmBlocks.RED_PYLON_MEDIUM_WOOD.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "red_pylon_medium_wood_transformer"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.RED_PYLON_MEDIUM_STEEL.get(), 2)
                .pattern("CCW")
                .pattern("IIW")
                .pattern("  S")
                .define('C', NtmItems.COIL_COPPER.get())
                .define('W', NtmItems.PIPE_STEEL.get())
                .define('I', NtmItems.PLATE_POLYMER.get())
                .define('S', Items.COBBLESTONE)
                .unlockedBy("has_pipe_steel", has(NtmItems.PIPE_STEEL.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "red_pylon_medium_steel"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NtmBlocks.RED_PYLON_MEDIUM_STEEL_TRANSFORMER.get(), 1)
                .requires(NtmBlocks.RED_PYLON_MEDIUM_STEEL.get())
                .requires(NtmItems.PLATE_POLYMER.get())
                .requires(NtmItems.COIL_COPPER.get())
                .unlockedBy("has_red_pylon_medium_steel", has(NtmBlocks.RED_PYLON_MEDIUM_STEEL.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "red_pylon_medium_steel_transformer"));

        // Originalrezept aus 1.7.10 (CraftingManager Z. 540, wiring_red_copper)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.WIRING_TOOL.get(), 1)
                .pattern("PPP")
                .pattern("PIP")
                .pattern("PPP")
                .define('P', NtmItems.PLATE_STEEL.get())
                .define('I', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_plate_steel", has(NtmItems.PLATE_STEEL.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "wiring_tool"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_MIXER.get(), 1)
                .pattern("PIP")
                .pattern("GCG")
                .pattern("PMP")
                .define('P', NtmItems.PLATE_STEEL.get())
                .define('I', NtmItems.INGOT_DURA_STEEL.get())
                .define('G', Items.GLASS_PANE)
                .define('C', NtmItems.CIRCUIT_VACUUM_TUBE.get())
                .define('M', NtmItems.MOTOR.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "machine_mixer"));

        // Originalrezept aus 1.7.10 (CraftingManager, furnace_iron)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.FURNACE_IRON.get(), 1)
                .pattern("III")
                .pattern("IFI")
                .pattern("BBB")
                .define('I', Items.IRON_INGOT)
                .define('F', Items.FURNACE)
                .define('B', Blocks.STONE_BRICKS)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "furnace_iron"));

        // Originalrezept aus 1.7.10 (CraftingManager, machine_solar_boiler)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_SOLAR_BOILER.get(), 1)
                .pattern("SHS")
                .pattern("DHD")
                .pattern("SHS")
                .define('S', NtmItems.INGOT_STEEL.get())
                .define('H', NtmItems.SHELL_STEEL.get())
                // Original: KEY_BLACK, also der OreDict-Sammelbegriff "dyeBlack". In 1.21 ist
                // das Gegenstueck der Tag c:dyes/black, nicht nur der eine Gegenstand.
                .define('D', Tags.Items.DYES_BLACK)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "machine_solar_boiler"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.STAMP_STONE_FLAT.get(), 1)
                .pattern("   ")
                .pattern("AAA")
                .pattern("BBB")
                .define('A', Items.BRICK)
                .define('B', Items.STONE)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "67"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.STAMP_IRON_FLAT.get(), 1)
                .pattern("   ")
                .pattern("AAA")
                .pattern("BBB")
                .define('A', Items.BRICK)
                .define('B', Items.IRON_INGOT)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "68"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.STAMP_STEEL_FLAT.get(), 1)
                .pattern("   ")
                .pattern("AAA")
                .pattern("BBB")
                .define('A', Items.BRICK)
                .define('B', NtmItems.INGOT_STEEL.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "69"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.STAMP_TITANIUM_FLAT.get(), 1)
                .pattern("   ")
                .pattern("AAA")
                .pattern("BBB")
                .define('A', Items.BRICK)
                .define('B', NtmItems.INGOT_TITANIUM.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "70"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.STAMP_OBSIDIAN_FLAT.get(), 1)
                .pattern("   ")
                .pattern("AAA")
                .pattern("BBB")
                .define('A', Items.BRICK)
                .define('B', Items.OBSIDIAN)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "71"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.STAMP_DESH_FLAT.get(), 1)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Items.BRICK)
                .define('B', NtmItems.INGOT_DESH.get())
                .define('C', NtmItems.INGOT_FERROURANIUM.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "72"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.PHOTO_PANEL.get(), 1)
                .pattern(" B ")
                .pattern("ACA")
                .pattern(" D ")
                .define('A', NtmItems.PLATE_POLYMER.get())
                .define('B', Items.GLASS_PANE)
                .define('C', NtmItems.POWDER_QUARTZ.get())
                .define('D', NtmItems.CIRCUIT_PRINTED_BOARD.get())
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "73"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.ARC_ELECTRODE_GRAPHITE.get(), 1)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" A ")
                .define('A', NtmItems.INGOT_GRAPHITE.get())
                .define('B', DataComponentIngredient.of(false, NtmDataComponents.META, BoltItem.Type.STEEL.meta, NtmItems.BOLT.get()))
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "74"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.ARC_ELECTRODE_GRAPHITE.get(), 1)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" A ")
                .define('A', NtmItems.COKE_PETROLEUM.get())
                .define('B', ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "any_tars")))
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "75"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.ARC_ELECTRODE_LANTHANIUM.get(), 1)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" A ")
                .define('A', NtmItems.INGOT_LANTHANIUM.get())
                .define('B', Items.BRICK)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "76"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.ARC_ELECTRODE_DESH.get(), 1)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" A ")
                .define('A', NtmItems.INGOT_DESH.get())
                .define('B', NtmItems.INGOT_TITANIUM)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "77"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.ARC_ELECTRODE_DESH.get(), 1)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" A ")
                .define('A', NtmItems.INGOT_SATURNITE.get())
                .define('B', NtmItems.INGOT_NIOBIUM)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "78"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.ARC_ELECTRODE_SATURNITE.get(), 1)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" A ")
                .define('A', NtmItems.INGOT_DESH.get())
                .define('B', NtmItems.INGOT_TUNGSTEN)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "79"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.TRANSFORMER.get(), 1)
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ABA")
                .define('A', Items.IRON_INGOT)
                .define('B', NtmItems.CIRCUIT_CAPACITOR)
                .define('C', NtmItems.COIL_COPPER)
                .define('D', NtmItems.INGOT_RED_COPPER)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "80"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CRATE_IRON.get(), 1)
                .pattern("AAA")
                .pattern("B B")
                .pattern("BBB")
                .define('A', NtmItems.PLATE_IRON)
                .define('B', Items.IRON_INGOT)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "81"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CRATE_STEEL.get(), 1)
                .pattern("AAA")
                .pattern("B B")
                .pattern("BBB")
                .define('A', NtmItems.PLATE_STEEL)
                .define('B', NtmItems.INGOT_STEEL)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "82"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.CRATE_DESH.get(), 1)
                .pattern(" B ")
                .pattern("BAB")
                .pattern(" B ")
                .define('A', NtmBlocks.CRATE_STEEL)
                .define('B', NtmItems.PLATE_DESH)
                .unlockedBy("has_crafting_table", has(Items.CRAFTING_TABLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "83"));

        //
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, NtmBlocks.BRICK_CONCRETE_MOSSY.get(), 8)
                .pattern("BBB")
                .pattern("BVB")
                .pattern("BBB")
                .define('B', NtmBlocks.BRICK_CONCRETE.get())
                .define('V', Items.VINE)
                .unlockedBy("can_craft_bricks", has(NtmBlocks.BRICK_CONCRETE))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, NtmBlocks.BRICK_CONCRETE_CRACKED.get(), 6)
                .pattern(" B ")
                .pattern("B B")
                .pattern(" B ")
                .define('B', NtmBlocks.BRICK_CONCRETE.get())
                .unlockedBy("can_craft_bricks", has(NtmBlocks.BRICK_CONCRETE))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, NtmBlocks.BRICK_CONCRETE_BROKEN.get(), 6)
                .pattern(" B ")
                .pattern("B B")
                .pattern(" B ")
                .define('B', NtmBlocks.BRICK_CONCRETE_CRACKED.get())
                .unlockedBy("can_craft_bricks", has(NtmBlocks.BRICK_CONCRETE))
                .save(recipeOutput);
        stairBuilder(NtmBlocks.BRICK_CONCRETE_STAIRS.get(), Ingredient.of(NtmBlocks.BRICK_CONCRETE))
                .unlockedBy("can_craft_bricks", has(NtmBlocks.BRICK_CONCRETE))
                .save(recipeOutput);
        stairBuilder(NtmBlocks.BRICK_CONCRETE_MOSSY_STAIRS.get(), Ingredient.of(NtmBlocks.BRICK_CONCRETE_MOSSY))
                .unlockedBy("can_craft_bricks", has(NtmBlocks.BRICK_CONCRETE))
                .save(recipeOutput);
        stairBuilder(NtmBlocks.BRICK_CONCRETE_CRACKED_STAIRS.get(), Ingredient.of(NtmBlocks.BRICK_CONCRETE_CRACKED))
                .unlockedBy("can_craft_bricks", has(NtmBlocks.BRICK_CONCRETE))
                .save(recipeOutput);
        stairBuilder(NtmBlocks.BRICK_CONCRETE_BROKEN_STAIRS.get(), Ingredient.of(NtmBlocks.BRICK_CONCRETE_BROKEN))
                .unlockedBy("can_craft_bricks", has(NtmBlocks.BRICK_CONCRETE))
                .save(recipeOutput);

        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, NtmBlocks.BRICK_CONCRETE_SLAB.get(), NtmBlocks.BRICK_CONCRETE.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, NtmBlocks.BRICK_CONCRETE_MOSSY_SLAB.get(), NtmBlocks.BRICK_CONCRETE_MOSSY.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, NtmBlocks.BRICK_CONCRETE_CRACKED_SLAB.get(), NtmBlocks.BRICK_CONCRETE_CRACKED.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, NtmBlocks.BRICK_CONCRETE_BROKEN_SLAB.get(), NtmBlocks.BRICK_CONCRETE_BROKEN.get());

        addOreSmelting(recipeOutput, NtmBlocks.ORE_BERYLLIUM.get(), NtmBlocks.ORE_BERYLLIUM_DEEPSLATE.get(), NtmItems.INGOT_BERYLLIUM.get(), 0.7F);
        addOreSmelting(recipeOutput, NtmBlocks.ORE_URANIUM.get(), NtmBlocks.ORE_URANIUM_DEEPSLATE.get(), NtmItems.INGOT_URANIUM.get(), 0.7F);
        addOreSmelting(recipeOutput, NtmBlocks.ORE_TUNGSTEN.get(), NtmBlocks.ORE_TUNGSTEN_DEEPSLATE.get(), NtmItems.INGOT_TUNGSTEN.get(), 0.7F);
        addOreSmelting(recipeOutput, NtmBlocks.ORE_TITANIUM.get(), NtmBlocks.ORE_TITANIUM_DEEPSLATE.get(), NtmItems.INGOT_TITANIUM.get(), 0.7F);
        addOreSmelting(recipeOutput, NtmBlocks.ORE_LEAD.get(), NtmBlocks.ORE_LEAD_DEEPSLATE.get(), NtmItems.INGOT_LEAD.get(), 0.7F);
        addOreSmelting(recipeOutput, NtmBlocks.ORE_ALUMINIUM.get(), NtmBlocks.ORE_ALUMINIUM_DEEPSLATE.get(), NtmItems.INGOT_ALUMINIUM.get(), 0.7F);
        addOreSmelting(recipeOutput, NtmBlocks.ORE_ASBESTOS.get(), NtmBlocks.ORE_ASBESTOS_DEEPSLATE.get(), NtmItems.INGOT_ASBESTOS.get(), 0.7F);
        addOreSmelting(recipeOutput, NtmBlocks.ORE_THORIUM.get(), NtmBlocks.ORE_THORIUM_DEEPSLATE.get(), NtmItems.INGOT_THORIUM_FUEL.get(), 0.7F);
        addOreSmelting(recipeOutput, NtmBlocks.ORE_NITER.get(), NtmBlocks.ORE_NITER_DEEPSLATE.get(), NtmItems.NITER.get(), 0.7F);
        addOreSmelting(recipeOutput, NtmBlocks.ORE_COBALT.get(), NtmBlocks.ORE_COBALT_DEEPSLATE.get(), NtmItems.FRAGMENT_COBALT.get(), 0.7F);
        addOreSmelting(recipeOutput, NtmBlocks.ORE_CINNABAR.get(), NtmBlocks.ORE_CINNABAR_DEEPSLATE.get(), NtmItems.CINNABAR.get(), 0.7F);
        addOreSmelting(recipeOutput, NtmBlocks.ORE_FLUORITE.get(), NtmBlocks.ORE_FLUORITE_DEEPSLATE.get(), NtmItems.FLUORITE.get(), 0.7F);
        addOreSmelting(recipeOutput, NtmBlocks.ORE_RARE.get(), NtmBlocks.ORE_RARE_DEEPSLATE.get(), NtmItems.RARE_EARTH_ORE_CHUNK.get(), 0.7F);
        addOreSmelting(recipeOutput, NtmBlocks.ORE_SULFUR.get(), NtmBlocks.ORE_SULFUR_DEEPSLATE.get(), NtmItems.SULFUR.get(), 0.7F);
        addOreSmelting(recipeOutput, NtmBlocks.ORE_LIGNITE.get(), NtmItems.LIGNITE.get(), 0.1F);
        addOreSmelting(recipeOutput, NtmBlocks.GRAVEL_DIAMOND.get(), Items.DIAMOND, 0.1F);

        addNuggetCrafting(recipeOutput, NtmItems.INGOT_URANIUM.get(), NtmItems.NUGGET_URANIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_U233.get(), NtmItems.NUGGET_U233.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_U235.get(), NtmItems.NUGGET_U235.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_U238.get(), NtmItems.NUGGET_U238.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_U238M2.get(), NtmItems.NUGGET_U238M2.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_PLUTONIUM.get(), NtmItems.NUGGET_PLUTONIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_PU238.get(), NtmItems.NUGGET_PU238.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_PU239.get(), NtmItems.NUGGET_PU239.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_PU240.get(), NtmItems.NUGGET_PU240.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_PU241.get(), NtmItems.NUGGET_PU241.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_PU_MIX.get(), NtmItems.NUGGET_PU_MIX.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_AM241.get(), NtmItems.NUGGET_AM241.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_AM242.get(), NtmItems.NUGGET_AM242.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_AM_MIX.get(), NtmItems.NUGGET_AM_MIX.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_TECHNETIUM.get(), NtmItems.NUGGET_TECHNETIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_NEPTUNIUM.get(), NtmItems.NUGGET_NEPTUNIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_POLONIUM.get(), NtmItems.NUGGET_POLONIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_THORIUM_FUEL.get(), NtmItems.NUGGET_THORIUM_FUEL.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_URANIUM_FUEL.get(), NtmItems.NUGGET_URANIUM_FUEL.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_MOX_FUEL.get(), NtmItems.NUGGET_MOX_FUEL.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_PLUTONIUM_FUEL.get(), NtmItems.NUGGET_PLUTONIUM_FUEL.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_NEPTUNIUM_FUEL.get(), NtmItems.NUGGET_NEPTUNIUM_FUEL.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_AMERICIUM_FUEL.get(), NtmItems.NUGGET_AMERICIUM_FUEL.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_SCHRABIDIUM_FUEL.get(), NtmItems.NUGGET_SCHRABIDIUM_FUEL.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_HES.get(), NtmItems.NUGGET_HES.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_LES.get(), NtmItems.NUGGET_LES.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_LEAD.get(), NtmItems.NUGGET_LEAD.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_BERYLLIUM.get(), NtmItems.NUGGET_BERYLLIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_CADMIUM.get(), NtmItems.NUGGET_CADMIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_BISMUTH.get(), NtmItems.NUGGET_BISMUTH.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_ARSENIC.get(), NtmItems.NUGGET_ARSENIC.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_ZIRCONIUM.get(), NtmItems.NUGGET_ZIRCONIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_TANTALIUM.get(), NtmItems.NUGGET_TANTALIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_DESH.get(), NtmItems.NUGGET_DESH.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_OSMIRIDIUM.get(), NtmItems.NUGGET_OSMIRIDIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_SCHRABIDIUM.get(), NtmItems.NUGGET_SCHRABIDIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_SOLINIUM.get(), NtmItems.NUGGET_SOLINIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_EUPHEMIUM.get(), NtmItems.NUGGET_EUPHEMIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_DINEUTRONIUM.get(), NtmItems.NUGGET_DINEUTRONIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_NIOBIUM.get(), NtmItems.NUGGET_NIOBIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_SILICON.get(), NtmItems.NUGGET_SILICON.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_ACTINIUM.get(), NtmItems.NUGGET_ACTINIUM.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_COBALT.get(), NtmItems.NUGGET_COBALT.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_CO60.get(), NtmItems.NUGGET_CO60.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_SR90.get(), NtmItems.NUGGET_SR90.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_PB209.get(), NtmItems.NUGGET_PB209.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_GH336.get(), NtmItems.NUGGET_GH336.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_AU198.get(), NtmItems.NUGGET_AU198.get());
        addNuggetCrafting(recipeOutput, NtmItems.INGOT_RA226.get(), NtmItems.NUGGET_RA226.get());

        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_URANIUM.get(), NtmItems.INGOT_URANIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_U233.get(), NtmItems.INGOT_U233.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_U235.get(), NtmItems.INGOT_U235.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_U238.get(), NtmItems.INGOT_U238.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_U238M2.get(), NtmItems.INGOT_U238M2.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_PLUTONIUM.get(), NtmItems.INGOT_PLUTONIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_PU238.get(), NtmItems.INGOT_PU238.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_PU239.get(), NtmItems.INGOT_PU239.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_PU240.get(), NtmItems.INGOT_PU240.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_PU241.get(), NtmItems.INGOT_PU241.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_PU_MIX.get(), NtmItems.INGOT_PU_MIX.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_AM241.get(), NtmItems.INGOT_AM241.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_AM242.get(), NtmItems.INGOT_AM242.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_AM_MIX.get(), NtmItems.INGOT_AM_MIX.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_TECHNETIUM.get(), NtmItems.INGOT_TECHNETIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_NEPTUNIUM.get(), NtmItems.INGOT_NEPTUNIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_POLONIUM.get(), NtmItems.INGOT_POLONIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_THORIUM_FUEL.get(), NtmItems.INGOT_THORIUM_FUEL.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_URANIUM_FUEL.get(), NtmItems.INGOT_URANIUM_FUEL.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_MOX_FUEL.get(), NtmItems.INGOT_MOX_FUEL.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_PLUTONIUM_FUEL.get(), NtmItems.INGOT_PLUTONIUM_FUEL.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_NEPTUNIUM_FUEL.get(), NtmItems.INGOT_NEPTUNIUM_FUEL.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_AMERICIUM_FUEL.get(), NtmItems.INGOT_AMERICIUM_FUEL.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_SCHRABIDIUM_FUEL.get(), NtmItems.INGOT_SCHRABIDIUM_FUEL.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_HES.get(), NtmItems.INGOT_HES.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_LES.get(), NtmItems.INGOT_LES.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_LEAD.get(), NtmItems.INGOT_LEAD.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_BERYLLIUM.get(), NtmItems.INGOT_BERYLLIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_CADMIUM.get(), NtmItems.INGOT_CADMIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_BISMUTH.get(), NtmItems.INGOT_BISMUTH.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_ARSENIC.get(), NtmItems.INGOT_ARSENIC.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_ZIRCONIUM.get(), NtmItems.INGOT_ZIRCONIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_TANTALIUM.get(), NtmItems.INGOT_TANTALIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_DESH.get(), NtmItems.INGOT_DESH.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_OSMIRIDIUM.get(), NtmItems.INGOT_OSMIRIDIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_SCHRABIDIUM.get(), NtmItems.INGOT_SCHRABIDIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_SOLINIUM.get(), NtmItems.INGOT_SOLINIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_EUPHEMIUM.get(), NtmItems.INGOT_EUPHEMIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_DINEUTRONIUM.get(), NtmItems.INGOT_DINEUTRONIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_NIOBIUM.get(), NtmItems.INGOT_NIOBIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_SILICON.get(), NtmItems.INGOT_SILICON.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_ACTINIUM.get(), NtmItems.INGOT_ACTINIUM.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_COBALT.get(), NtmItems.INGOT_COBALT.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_CO60.get(), NtmItems.INGOT_CO60.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_SR90.get(), NtmItems.INGOT_SR90.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_PB209.get(), NtmItems.INGOT_PB209.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_GH336.get(), NtmItems.INGOT_GH336.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_AU198.get(), NtmItems.INGOT_AU198.get());
        addNuggetDeCrafting(recipeOutput, NtmItems.NUGGET_RA226.get(), NtmItems.INGOT_RA226.get());

        addPickaxeRecipe(recipeOutput, NtmItems.STEEL_PICKAXE.get(), NtmItems.INGOT_STEEL.get(), "steel_pickaxe");
        addAxeRecipe(recipeOutput, NtmItems.STEEL_AXE.get(), NtmItems.INGOT_STEEL.get(), "steel_axe");
        addShovelRecipe(recipeOutput, NtmItems.STEEL_SHOVEL.get(), NtmItems.INGOT_STEEL.get(), "steel_shovel");
        addHoeRecipe(recipeOutput, NtmItems.STEEL_HOE.get(), NtmItems.INGOT_STEEL.get(), "steel_hoe");

        addPickaxeRecipe(recipeOutput, NtmItems.TITANIUM_PICKAXE.get(), NtmItems.INGOT_TITANIUM.get(), "titanium_pickaxe");
        addAxeRecipe(recipeOutput, NtmItems.TITANIUM_AXE.get(), NtmItems.INGOT_TITANIUM.get(), "titanium_axe");
        addShovelRecipe(recipeOutput, NtmItems.TITANIUM_SHOVEL.get(), NtmItems.INGOT_TITANIUM.get(), "titanium_shovel");
        addHoeRecipe(recipeOutput, NtmItems.TITANIUM_HOE.get(), NtmItems.INGOT_TITANIUM.get(), "titanium_hoe");

        addPickaxeRecipe(recipeOutput, NtmItems.DESH_PICKAXE.get(), NtmItems.INGOT_DESH.get(), "desh_pickaxe");
        addAxeRecipe(recipeOutput, NtmItems.DESH_AXE.get(), NtmItems.INGOT_DESH.get(), "desh_axe");
        addShovelRecipe(recipeOutput, NtmItems.DESH_SHOVEL.get(), NtmItems.INGOT_DESH.get(), "desh_shovel");
        addHoeRecipe(recipeOutput, NtmItems.DESH_HOE.get(), NtmItems.INGOT_DESH.get(), "desh_hoe");

        addPickaxeRecipe(recipeOutput, NtmItems.COBALT_PICKAXE.get(), NtmItems.INGOT_COBALT.get(), "cobalt_pickaxe");
        addAxeRecipe(recipeOutput, NtmItems.COBALT_AXE.get(), NtmItems.INGOT_COBALT.get(), "cobalt_axe");
        addShovelRecipe(recipeOutput, NtmItems.COBALT_SHOVEL.get(), NtmItems.INGOT_COBALT.get(), "cobalt_shovel");
        addHoeRecipe(recipeOutput, NtmItems.COBALT_HOE.get(), NtmItems.INGOT_COBALT.get(), "cobalt_hoe");

        addPickaxeRecipe(recipeOutput, NtmItems.CMB_PICKAXE.get(), NtmItems.INGOT_COMBINE_STEEL.get(), "cmb_pickaxe");
        addAxeRecipe(recipeOutput, NtmItems.CMB_AXE.get(), NtmItems.INGOT_COMBINE_STEEL.get(), "cmb_axe");
        addShovelRecipe(recipeOutput, NtmItems.CMB_SHOVEL.get(), NtmItems.INGOT_COMBINE_STEEL.get(), "cmb_shovel");
        addHoeRecipe(recipeOutput, NtmItems.CMB_HOE.get(), NtmItems.INGOT_COMBINE_STEEL.get(), "cmb_hoe");

        addPickaxeRecipe(recipeOutput, NtmItems.BISMUTH_PICKAXE.get(), NtmItems.INGOT_BISMUTH.get(), "bismuth_pickaxe");
        addAxeRecipe(recipeOutput, NtmItems.BISMUTH_AXE.get(), NtmItems.INGOT_BISMUTH.get(), "bismuth_axe");

        addPickaxeRecipe(recipeOutput, NtmItems.STARMETAL_PICKAXE.get(), NtmItems.INGOT_STARMETAL.get(), "starmetal_pickaxe");
        addAxeRecipe(recipeOutput, NtmItems.STARMETAL_AXE.get(), NtmItems.INGOT_STARMETAL.get(), "starmetal_axe");
        addShovelRecipe(recipeOutput, NtmItems.STARMETAL_SHOVEL.get(), NtmItems.INGOT_STARMETAL.get(), "starmetal_shovel");
        addHoeRecipe(recipeOutput, NtmItems.STARMETAL_HOE.get(), NtmItems.INGOT_STARMETAL.get(), "starmetal_hoe");

        addPickaxeRecipe(recipeOutput, NtmItems.SCHRABIDIUM_PICKAXE.get(), NtmItems.INGOT_SCHRABIDIUM.get(), "schrabidium_pickaxe");
        addAxeRecipe(recipeOutput, NtmItems.SCHRABIDIUM_AXE.get(), NtmItems.INGOT_SCHRABIDIUM.get(), "schrabidium_axe");
        addShovelRecipe(recipeOutput, NtmItems.SCHRABIDIUM_SHOVEL.get(), NtmItems.INGOT_SCHRABIDIUM.get(), "schrabidium_shovel");
        addHoeRecipe(recipeOutput, NtmItems.SCHRABIDIUM_HOE.get(), NtmItems.INGOT_SCHRABIDIUM.get(), "schrabidium_hoe");

        /*
         * Die Laufwerkskiste, Runde 123. Das Original baut sie am Werktisch aus Kunststoff und
         * Leiterplatten -- ein Gehaeuse mit einer Reihe Elektronik in der Mitte.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_TAPE_DRIVE.get(), 1)
                .pattern("PPP")
                .pattern("CCC")
                .pattern("PPP")
                .define('P', NtmItems.INGOT_POLYMER.get())
                .define('C', NtmItems.CIRCUIT_PRINTED_BOARD.get())
                .unlockedBy("has_printed_board", has(NtmItems.CIRCUIT_PRINTED_BOARD.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "machine_tape_drive"));

        /*
         * Die Munitionspresse, Runde 125, wie im Original: Eisen und Kolben oben, Kupfer an den
         * Seiten, Stein als Sockel.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_AMMO_PRESS.get(), 1)
                .pattern("IPI")
                .pattern("C C")
                .pattern("SSS")
                .define('I', Items.IRON_INGOT)
                .define('P', Blocks.PISTON)
                .define('C', NtmItems.INGOT_COPPER.get())
                .define('S', Blocks.STONE)
                .unlockedBy("has_copper", has(NtmItems.INGOT_COPPER.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "machine_ammo_press"));

        /*
         * Der Radar-Verbinder, Runde 126, wie im Original: eine Bildroehre ueber einer
         * Leiterplatte ueber einer Stahlplatte.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.RADAR_LINKER.get(), 1)
                .pattern("S")
                .pattern("C")
                .pattern("P")
                .define('S', NtmItems.CRT_DISPLAY.get())
                .define('C', NtmItems.CIRCUIT_INTEGRATED_BOARD.get())
                .define('P', NtmItems.PLATE_STEEL.get())
                .unlockedBy("has_crt_display", has(NtmItems.CRT_DISPLAY.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "radar_linker"));

        /*
         * Die Sirene, Runde 127, wie im Original: Stahlplatten aussen, Gummi an den Seiten, eine
         * Vakuumroehre in der Mitte und Redstone unten. Die Kassetten selbst haben im Original
         * KEIN Rezept -- sie sind Fundstuecke; das bleibt hier so.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_SIREN.get(), 1)
                .pattern("SIS")
                .pattern("ICI")
                .pattern("SRS")
                .define('S', NtmItems.PLATE_STEEL.get())
                .define('I', NtmItems.INGOT_RUBBER.get())
                .define('C', NtmItems.CIRCUIT_VACUUM_TUBE.get())
                .define('R', Items.REDSTONE)
                .unlockedBy("has_vacuum_tube", has(NtmItems.CIRCUIT_VACUUM_TUBE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "machine_siren"));

        /*
         * Die Satellitenstation, Runde 129, wie im Original: Stahl oben, Kunststoff an den
         * Seiten, eine Eisenkiste in der Mitte. Das Original laesst als Kunststoff jede Sorte
         * aus dem Erzwoerterbuch zu; der Port hat kein Erzwoerterbuch und nimmt das Polymer.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_SAT_DOCK.get(), 1)
                .pattern("SSS")
                .pattern("PCP")
                .define('S', NtmItems.INGOT_STEEL.get())
                .define('P', NtmItems.INGOT_POLYMER.get())
                .define('C', NtmBlocks.CRATE_IRON.get())
                .unlockedBy("has_iron_crate", has(NtmBlocks.CRATE_IRON.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "sat_dock"));

        /*
         * Die Raketenmontage, Runde 132, wie im Original: zwei Sockel und ein Werkzeug oben,
         * Stahlplatten in der Mitte, Geruest unten.
         *
         * ABWEICHUNG: der Schraubenschluessel des Originals fehlt im Port; an seiner Stelle
         * steht der Schraubenzieher, das naechstliegende Werkzeug aus Runde 99.
         */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_MISSILE_ASSEMBLY.get(), 1)
                .pattern("PWP")
                .pattern("SSS")
                .pattern("CCC")
                .define('P', NtmItems.PEDESTAL_STEEL.get())
                .define('W', NtmItems.SCREWDRIVER.get())
                .define('S', NtmItems.PLATE_STEEL.get())
                .define('C', NtmBlocks.STEEL_SCAFFOLD.get())
                .unlockedBy("has_steel_plate", has(NtmItems.PLATE_STEEL.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", "machine_missile_assembly"));

        registerMaterialConversionRecipes(recipeOutput);
    }

    private void addNuggetCrafting(RecipeOutput recipeOutput, Item result, Item nugget) {
        String resultName = BuiltInRegistries.ITEM.getKey(result).getPath();
        String nuggetName = BuiltInRegistries.ITEM.getKey(nugget).getPath();
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath("hbmsntm", resultName + "_from_" + nuggetName);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result, 1)
                .pattern("nnn")
                .pattern("nnn")
                .pattern("nnn")
                .define('n', nugget)
                .unlockedBy("has_" + nuggetName, has(nugget))
                .save(recipeOutput, recipeId);
    }

    private void addNuggetDeCrafting(RecipeOutput recipeOutput, Item nugget, Item result) {
        String nuggetName = BuiltInRegistries.ITEM.getKey(nugget).getPath();
        String resultName = BuiltInRegistries.ITEM.getKey(result).getPath();
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath("hbmsntm", nuggetName + "_from_" + resultName);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nugget, 9)
                .requires(result)
                .unlockedBy("has_" + resultName, has(result))
                .save(recipeOutput, recipeId);
    }

    private void addOreSmelting(RecipeOutput recipeOutput, Block input, Block deepslateInput, Item result, float experience) {
        addOreSmelting(recipeOutput, input, result, experience);
        addOreSmelting(recipeOutput, deepslateInput, result, experience);
    }

    private void addOreSmelting(RecipeOutput recipeOutput, Block input, Item result, float experience) {
        String inputName = BuiltInRegistries.BLOCK.getKey(input).getPath();
        String resultName = BuiltInRegistries.ITEM.getKey(result).getPath();
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath("hbmsntm", resultName + "_from_smelting_" + inputName);

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, result, experience, 200)
                .unlockedBy("has_" + inputName, has(input))
                .save(recipeOutput, recipeId);
    }

    private void addPickaxeRecipe(RecipeOutput recipeOutput, Item result, Item ingredient, String baseName) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result, 1)
                .pattern("XXX")
                .pattern(" # ")
                .pattern(" # ")
                .define('X', ingredient)
                .define('#', Items.STICK)
                .unlockedBy("has_" + BuiltInRegistries.ITEM.getKey(ingredient).getPath(), has(ingredient))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", baseName));
    }

    private void addAxeRecipe(RecipeOutput recipeOutput, Item result, Item ingredient, String baseName) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result, 1)
                .pattern("XX ")
                .pattern("X# ")
                .pattern(" # ")
                .define('X', ingredient)
                .define('#', Items.STICK)
                .unlockedBy("has_" + BuiltInRegistries.ITEM.getKey(ingredient).getPath(), has(ingredient))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", baseName));
    }

    private void addShovelRecipe(RecipeOutput recipeOutput, Item result, Item ingredient, String baseName) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result, 1)
                .pattern(" X ")
                .pattern(" # ")
                .pattern(" # ")
                .define('X', ingredient)
                .define('#', Items.STICK)
                .unlockedBy("has_" + BuiltInRegistries.ITEM.getKey(ingredient).getPath(), has(ingredient))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", baseName));
    }

    private void addHoeRecipe(RecipeOutput recipeOutput, Item result, Item ingredient, String baseName) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result, 1)
                .pattern("XX ")
                .pattern(" # ")
                .pattern(" # ")
                .define('X', ingredient)
                .define('#', Items.STICK)
                .unlockedBy("has_" + BuiltInRegistries.ITEM.getKey(ingredient).getPath(), has(ingredient))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", baseName));
    }

    private void registerMaterialConversionRecipes(RecipeOutput recipeOutput) {
        registerMaterialBlockRecipes(recipeOutput);
        registerBilletRecipes(recipeOutput);
        registerPowderSmeltingRecipes(recipeOutput);
        registerTinyPowderRecipes(recipeOutput);
        registerCrystalSmeltingRecipes(recipeOutput);
    }

    private void registerMaterialBlockRecipes(RecipeOutput recipeOutput) {
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_ACTINIUM.get(), NtmItems.INGOT_ACTINIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_ALUMINIUM.get(), NtmItems.INGOT_ALUMINIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_ASBESTOS.get(), NtmItems.INGOT_ASBESTOS.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_AUSTRALIUM.get(), NtmItems.INGOT_AUSTRALIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_BERYLLIUM.get(), NtmItems.INGOT_BERYLLIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_BISMUTH.get(), NtmItems.INGOT_BISMUTH.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_CADMIUM.get(), NtmItems.INGOT_CADMIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_CDALLOY.get(), NtmItems.INGOT_CDALLOY.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_COLTAN.get(), NtmItems.POWDER_COLTAN.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_COMBINE_STEEL.get(), NtmItems.INGOT_COMBINE_STEEL.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_COPPER.get(), NtmItems.INGOT_COPPER.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_DESH.get(), NtmItems.INGOT_DESH.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_DINEUTRONIUM.get(), NtmItems.INGOT_DINEUTRONIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_DURA_STEEL.get(), NtmItems.INGOT_DURA_STEEL.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_EUPHEMIUM.get(), NtmItems.INGOT_EUPHEMIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_FIBERGLASS.get(), NtmItems.INGOT_FIBERGLASS.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_INSULATOR.get(), NtmItems.PLATE_POLYMER.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_LANTHANIUM.get(), NtmItems.INGOT_LANTHANIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_LEAD.get(), NtmItems.INGOT_LEAD.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_LITHIUM.get(), NtmItems.LITHIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_MAGNETIZED_TUNGSTEN.get(), NtmItems.INGOT_MAGNETIZED_TUNGSTEN.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_MOX_FUEL.get(), NtmItems.INGOT_MOX_FUEL.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_NEPTUNIUM.get(), NtmItems.INGOT_NEPTUNIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_NIOBIUM.get(), NtmItems.INGOT_NIOBIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_NITER.get(), NtmItems.NITER.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_PLUTONIUM.get(), NtmItems.INGOT_PLUTONIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_PLUTONIUM_FUEL.get(), NtmItems.INGOT_PLUTONIUM_FUEL.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_POLONIUM.get(), NtmItems.INGOT_POLONIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_PU238.get(), NtmItems.INGOT_PU238.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_PU239.get(), NtmItems.INGOT_PU239.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_PU240.get(), NtmItems.INGOT_PU240.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_PU_MIX.get(), NtmItems.INGOT_PU_MIX.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_RA226.get(), NtmItems.INGOT_RA226.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_RED_COPPER.get(), NtmItems.INGOT_RED_COPPER.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_SATURNITE.get(), NtmItems.INGOT_SATURNITE.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_SCHRABIDATE.get(), NtmItems.INGOT_SCHRABIDATE.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_SCHRABIDIUM.get(), NtmItems.INGOT_SCHRABIDIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_SCHRABIDIUM_FUEL.get(), NtmItems.INGOT_SCHRABIDIUM_FUEL.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_SCHRARANIUM.get(), NtmItems.INGOT_SCHRARANIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_SMORE.get(), NtmItems.INGOT_SMORE.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_SOLINIUM.get(), NtmItems.INGOT_SOLINIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_SULFUR.get(), NtmItems.SULFUR.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_TANTALIUM.get(), NtmItems.INGOT_TANTALIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_TCALLOY.get(), NtmItems.INGOT_TCALLOY.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_THORIUM.get(), NtmItems.INGOT_TH232.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_THORIUM_FUEL.get(), NtmItems.INGOT_THORIUM_FUEL.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_TITANIUM.get(), NtmItems.INGOT_TITANIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_TUNGSTEN.get(), NtmItems.INGOT_TUNGSTEN.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_U233.get(), NtmItems.INGOT_U233.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_U235.get(), NtmItems.INGOT_U235.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_U238.get(), NtmItems.INGOT_U238.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_URANIUM.get(), NtmItems.INGOT_URANIUM.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_URANIUM_FUEL.get(), NtmItems.INGOT_URANIUM_FUEL.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_WASTE.get(), NtmItems.NUCLEAR_WASTE.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_WASTE_VITRIFIED.get(), NtmItems.NUCLEAR_WASTE_VITRIFIED.get());
        addMaterialBlockRecipe(recipeOutput, NtmBlocks.BLOCK_YELLOWCAKE.get(), NtmItems.POWDER_YELLOWCAKE.get());
        /* Runde 41: der Abfallkrumen. Neun davon ergeben einen Abfall und umgekehrt, wie im Original. */
        addMaterialBlockRecipe(recipeOutput, NtmItems.NUCLEAR_WASTE.get(), NtmItems.NUCLEAR_WASTE_TINY.get());
    }

    private void registerBilletRecipes(RecipeOutput recipeOutput) {
        addBilletRecipe(recipeOutput, NtmItems.BILLET_NUCLEAR_WASTE.get(), NtmItems.NUCLEAR_WASTE_TINY.get(), NtmItems.NUCLEAR_WASTE.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_ACTINIUM.get(), NtmItems.NUGGET_ACTINIUM.get(), NtmItems.INGOT_ACTINIUM.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_AM241.get(), NtmItems.NUGGET_AM241.get(), NtmItems.INGOT_AM241.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_AM242.get(), NtmItems.NUGGET_AM242.get(), NtmItems.INGOT_AM242.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_AM_MIX.get(), NtmItems.NUGGET_AM_MIX.get(), NtmItems.INGOT_AM_MIX.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_AMERICIUM_FUEL.get(), NtmItems.NUGGET_AMERICIUM_FUEL.get(), NtmItems.INGOT_AMERICIUM_FUEL.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_AU198.get(), NtmItems.NUGGET_AU198.get(), NtmItems.INGOT_AU198.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_AUSTRALIUM.get(), NtmItems.NUGGET_AUSTRALIUM.get(), NtmItems.INGOT_AUSTRALIUM.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_BERYLLIUM.get(), NtmItems.NUGGET_BERYLLIUM.get(), NtmItems.INGOT_BERYLLIUM.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_BISMUTH.get(), NtmItems.NUGGET_BISMUTH.get(), NtmItems.INGOT_BISMUTH.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_CO60.get(), NtmItems.NUGGET_CO60.get(), NtmItems.INGOT_CO60.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_COBALT.get(), NtmItems.NUGGET_COBALT.get(), NtmItems.INGOT_COBALT.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_GH336.get(), NtmItems.NUGGET_GH336.get(), NtmItems.INGOT_GH336.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_HES.get(), NtmItems.NUGGET_HES.get(), NtmItems.INGOT_HES.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_LES.get(), NtmItems.NUGGET_LES.get(), NtmItems.INGOT_LES.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_MOX_FUEL.get(), NtmItems.NUGGET_MOX_FUEL.get(), NtmItems.INGOT_MOX_FUEL.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_NEPTUNIUM.get(), NtmItems.NUGGET_NEPTUNIUM.get(), NtmItems.INGOT_NEPTUNIUM.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_NEPTUNIUM_FUEL.get(), NtmItems.NUGGET_NEPTUNIUM_FUEL.get(), NtmItems.INGOT_NEPTUNIUM_FUEL.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_PB209.get(), NtmItems.NUGGET_PB209.get(), NtmItems.INGOT_PB209.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_PLUTONIUM.get(), NtmItems.NUGGET_PLUTONIUM.get(), NtmItems.INGOT_PLUTONIUM.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_PLUTONIUM_FUEL.get(), NtmItems.NUGGET_PLUTONIUM_FUEL.get(), NtmItems.INGOT_PLUTONIUM_FUEL.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_POLONIUM.get(), NtmItems.NUGGET_POLONIUM.get(), NtmItems.INGOT_POLONIUM.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_PU238.get(), NtmItems.NUGGET_PU238.get(), NtmItems.INGOT_PU238.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_PU239.get(), NtmItems.NUGGET_PU239.get(), NtmItems.INGOT_PU239.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_PU240.get(), NtmItems.NUGGET_PU240.get(), NtmItems.INGOT_PU240.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_PU241.get(), NtmItems.NUGGET_PU241.get(), NtmItems.INGOT_PU241.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_PU_MIX.get(), NtmItems.NUGGET_PU_MIX.get(), NtmItems.INGOT_PU_MIX.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_RA226.get(), NtmItems.NUGGET_RA226.get(), NtmItems.INGOT_RA226.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_SCHRABIDIUM.get(), NtmItems.NUGGET_SCHRABIDIUM.get(), NtmItems.INGOT_SCHRABIDIUM.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_SCHRABIDIUM_FUEL.get(), NtmItems.NUGGET_SCHRABIDIUM_FUEL.get(), NtmItems.INGOT_SCHRABIDIUM_FUEL.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_SOLINIUM.get(), NtmItems.NUGGET_SOLINIUM.get(), NtmItems.INGOT_SOLINIUM.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_SR90.get(), NtmItems.NUGGET_SR90.get(), NtmItems.INGOT_SR90.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_TECHNETIUM.get(), NtmItems.NUGGET_TECHNETIUM.get(), NtmItems.INGOT_TECHNETIUM.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_TH232.get(), NtmItems.NUGGET_TH232.get(), NtmItems.INGOT_TH232.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_THORIUM_FUEL.get(), NtmItems.NUGGET_THORIUM_FUEL.get(), NtmItems.INGOT_THORIUM_FUEL.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_U233.get(), NtmItems.NUGGET_U233.get(), NtmItems.INGOT_U233.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_U235.get(), NtmItems.NUGGET_U235.get(), NtmItems.INGOT_U235.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_U238.get(), NtmItems.NUGGET_U238.get(), NtmItems.INGOT_U238.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_UNOBTAINIUM.get(), NtmItems.NUGGET_UNOBTAINIUM.get(), NtmItems.INGOT_UNOBTAINIUM.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_URANIUM.get(), NtmItems.NUGGET_URANIUM.get(), NtmItems.INGOT_URANIUM.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_URANIUM_FUEL.get(), NtmItems.NUGGET_URANIUM_FUEL.get(), NtmItems.INGOT_URANIUM_FUEL.get());
        addBilletRecipe(recipeOutput, NtmItems.BILLET_ZIRCONIUM.get(), NtmItems.NUGGET_ZIRCONIUM.get(), NtmItems.INGOT_ZIRCONIUM.get());
    }

    private void registerPowderSmeltingRecipes(RecipeOutput recipeOutput) {
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_ACTINIUM.get(), NtmItems.INGOT_ACTINIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_ALUMINIUM.get(), NtmItems.INGOT_ALUMINIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_ASBESTOS.get(), NtmItems.INGOT_ASBESTOS.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_ASTATINE.get(), NtmItems.INGOT_ASTATINE.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_AU198.get(), NtmItems.INGOT_AU198.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_AUSTRALIUM.get(), NtmItems.INGOT_AUSTRALIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_BAKELITE.get(), NtmItems.INGOT_BAKELITE.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_BERYLLIUM.get(), NtmItems.INGOT_BERYLLIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_BISMUTH.get(), NtmItems.INGOT_BISMUTH.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_BORON.get(), NtmItems.INGOT_BORON.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_BROMINE.get(), NtmItems.INGOT_BROMINE.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_CADMIUM.get(), NtmItems.INGOT_CADMIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_CAESIUM.get(), NtmItems.INGOT_CAESIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_CALCIUM.get(), NtmItems.INGOT_CALCIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_CDALLOY.get(), NtmItems.INGOT_CDALLOY.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_CERIUM.get(), NtmItems.INGOT_CERIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_CO60.get(), NtmItems.INGOT_CO60.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_COBALT.get(), NtmItems.INGOT_COBALT.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_COMBINE_STEEL.get(), NtmItems.INGOT_COMBINE_STEEL.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_COPPER.get(), NtmItems.INGOT_COPPER.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_DAFFERGON.get(), NtmItems.INGOT_DAFFERGON.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_DESH.get(), NtmItems.INGOT_DESH.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_DINEUTRONIUM.get(), NtmItems.INGOT_DINEUTRONIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_DURA_STEEL.get(), NtmItems.INGOT_DURA_STEEL.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_EUPHEMIUM.get(), NtmItems.INGOT_EUPHEMIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_GOLD.get(), Items.GOLD_INGOT);
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_I131.get(), NtmItems.INGOT_I131.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_IODINE.get(), NtmItems.INGOT_IODINE.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_IRON.get(), Items.IRON_INGOT);
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_LANTHANIUM.get(), NtmItems.INGOT_LANTHANIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_LEAD.get(), NtmItems.INGOT_LEAD.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_MAGNETIZED_TUNGSTEN.get(), NtmItems.INGOT_MAGNETIZED_TUNGSTEN.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_METEORITE.get(), NtmItems.INGOT_METEORITE.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_NEPTUNIUM.get(), NtmItems.INGOT_NEPTUNIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_NIOBIUM.get(), NtmItems.INGOT_NIOBIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_OSMIRIDIUM.get(), NtmItems.INGOT_OSMIRIDIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_PB209.get(), NtmItems.INGOT_PB209.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_PLUTONIUM.get(), NtmItems.INGOT_PLUTONIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_POLONIUM.get(), NtmItems.INGOT_POLONIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_POLYMER.get(), NtmItems.INGOT_POLYMER.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_RA226.get(), NtmItems.INGOT_RA226.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_RED_COPPER.get(), NtmItems.INGOT_RED_COPPER.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_REIIUM.get(), NtmItems.INGOT_REIIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_SCHRABIDATE.get(), NtmItems.INGOT_SCHRABIDATE.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_SCHRABIDIUM.get(), NtmItems.INGOT_SCHRABIDIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_SR90.get(), NtmItems.INGOT_SR90.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_STEEL.get(), NtmItems.INGOT_STEEL.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_TANTALIUM.get(), NtmItems.INGOT_TANTALIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_TCALLOY.get(), NtmItems.INGOT_TCALLOY.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_TENNESSINE.get(), NtmItems.INGOT_TENNESSINE.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_TITANIUM.get(), NtmItems.INGOT_TITANIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_TUNGSTEN.get(), NtmItems.INGOT_TUNGSTEN.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_UNOBTAINIUM.get(), NtmItems.INGOT_UNOBTAINIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_URANIUM.get(), NtmItems.INGOT_URANIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_VERTICIUM.get(), NtmItems.INGOT_VERTICIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_WEIDANIUM.get(), NtmItems.INGOT_WEIDANIUM.get());
        addMaterialSmelting(recipeOutput, NtmItems.POWDER_ZIRCONIUM.get(), NtmItems.INGOT_ZIRCONIUM.get());
    }

    private void registerTinyPowderRecipes(RecipeOutput recipeOutput) {
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_METEORITE_TINY.get(), NtmItems.POWDER_METEORITE.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_ACTINIUM_TINY.get(), NtmItems.POWDER_ACTINIUM.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_AT209_TINY.get(), NtmItems.POWDER_AT209.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_AU198_TINY.get(), NtmItems.POWDER_AU198.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_BORON_TINY.get(), NtmItems.POWDER_BORON.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_CERIUM_TINY.get(), NtmItems.POWDER_CERIUM.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_CO60_TINY.get(), NtmItems.POWDER_CO60.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_COAL_TINY.get(), NtmItems.POWDER_COAL.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_COBALT_TINY.get(), NtmItems.POWDER_COBALT.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_CS137_TINY.get(), NtmItems.POWDER_CS137.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_I131_TINY.get(), NtmItems.POWDER_I131.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_IODINE_TINY.get(), NtmItems.POWDER_IODINE.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_LANTHANIUM_TINY.get(), NtmItems.POWDER_LANTHANIUM.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_LITHIUM_TINY.get(), NtmItems.POWDER_LITHIUM.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_NEODYMIUM_TINY.get(), NtmItems.POWDER_NEODYMIUM.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_NIOBIUM_TINY.get(), NtmItems.POWDER_NIOBIUM.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_PALEOGENITE_TINY.get(), NtmItems.POWDER_PALEOGENITE.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_PB209_TINY.get(), NtmItems.POWDER_PB209.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_SR90_TINY.get(), NtmItems.POWDER_SR90.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_STEEL_TINY.get(), NtmItems.POWDER_STEEL.get());
        addTinyPowderRecipe(recipeOutput, NtmItems.POWDER_XE135_TINY.get(), NtmItems.POWDER_XE135.get());
    }

    private void registerCrystalSmeltingRecipes(RecipeOutput recipeOutput) {
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_ALUMINIUM.get(), NtmItems.INGOT_ALUMINIUM.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_ASBESTOS.get(), NtmItems.INGOT_ASBESTOS.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_BERYLLIUM.get(), NtmItems.INGOT_BERYLLIUM.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_COBALT.get(), NtmItems.INGOT_COBALT.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_COPPER.get(), NtmItems.INGOT_COPPER.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_GOLD.get(), Items.GOLD_INGOT);
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_IRON.get(), Items.IRON_INGOT);
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_LEAD.get(), NtmItems.INGOT_LEAD.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_OSMIRIDIUM.get(), NtmItems.INGOT_OSMIRIDIUM.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_PHOSPHORUS.get(), NtmItems.POWDER_FIRE.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_PLUTONIUM.get(), NtmItems.INGOT_PLUTONIUM.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_SCHRABIDIUM.get(), NtmItems.INGOT_SCHRABIDIUM.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_SCHRARANIUM.get(), NtmItems.INGOT_SCHRARANIUM.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_STARMETAL.get(), NtmItems.INGOT_STARMETAL.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_TITANIUM.get(), NtmItems.INGOT_TITANIUM.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_TUNGSTEN.get(), NtmItems.INGOT_TUNGSTEN.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_URANIUM.get(), NtmItems.INGOT_URANIUM.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_SULFUR.get(), NtmItems.SULFUR.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_REDSTONE.get(), Items.REDSTONE);
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_LAPIS.get(), Items.LAPIS_LAZULI);
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_DIAMOND.get(), Items.DIAMOND);
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_THORIUM.get(), NtmItems.INGOT_TH232.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_NITER.get(), NtmItems.NITER.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_FLUORITE.get(), NtmItems.FLUORITE.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_CINNABAR.get(), NtmItems.CINNABAR.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_LITHIUM.get(), NtmItems.LITHIUM.get());
        addCrystalSmelting(recipeOutput, NtmItems.CRYSTAL_TRIXITE.get(), NtmItems.INGOT_PLUTONIUM.get());

    }

    private void addMaterialBlockRecipe(RecipeOutput recipeOutput, ItemLike blockItem, ItemLike counterpart) {
        Item result = blockItem.asItem();
        Item input = counterpart.asItem();
        String resultName = BuiltInRegistries.ITEM.getKey(result).getPath();
        String inputName = BuiltInRegistries.ITEM.getKey(input).getPath();

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, 1)
                .pattern("XXX")
                .pattern("XXX")
                .pattern("XXX")
                .define('X', input)
                .unlockedBy("has_" + inputName, has(input))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", resultName + "_from_9_" + inputName));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, input, 9)
                .requires(result)
                .unlockedBy("has_" + resultName, has(result))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", inputName + "_from_" + resultName));
    }

    private void addBilletRecipe(RecipeOutput recipeOutput, ItemLike billetItem, ItemLike nuggetItem, ItemLike ingotItem) {
        Item billet = billetItem.asItem();
        Item nugget = nuggetItem.asItem();
        Item ingot = ingotItem.asItem();
        String billetName = BuiltInRegistries.ITEM.getKey(billet).getPath();
        String nuggetName = BuiltInRegistries.ITEM.getKey(nugget).getPath();
        String ingotName = BuiltInRegistries.ITEM.getKey(ingot).getPath();

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, billet, 1)
                .pattern("nnn")
                .pattern("nnn")
                .define('n', nugget)
                .unlockedBy("has_" + nuggetName, has(nugget))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", billetName + "_from_6_" + nuggetName));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nugget, 6)
                .requires(billet)
                .unlockedBy("has_" + billetName, has(billet))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", nuggetName + "_from_" + billetName));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, billet, 3)
                .pattern("XX")
                .define('X', ingot)
                .unlockedBy("has_" + ingotName, has(ingot))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", billetName + "_from_2_" + ingotName));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ingot, 2)
                .requires(billet)
                .requires(billet)
                .requires(billet)
                .unlockedBy("has_" + billetName, has(billet))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", ingotName + "_from_3_" + billetName));
    }

    private void addMaterialSmelting(RecipeOutput recipeOutput, ItemLike inputItem, ItemLike outputItem) {
        Item input = inputItem.asItem();
        Item output = outputItem.asItem();
        String inputName = BuiltInRegistries.ITEM.getKey(input).getPath();
        String outputName = BuiltInRegistries.ITEM.getKey(output).getPath();

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, output, 0.7F, 200)
                .unlockedBy("has_" + inputName, has(input))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", inputName + "_to_" + outputName));
    }

    private void addTinyPowderRecipe(RecipeOutput recipeOutput, ItemLike tinyPowderItem, ItemLike powderItem) {
        Item tinyPowder = tinyPowderItem.asItem();
        Item powder = powderItem.asItem();
        String tinyName = BuiltInRegistries.ITEM.getKey(tinyPowder).getPath();
        String powderName = BuiltInRegistries.ITEM.getKey(powder).getPath();

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, powder, 1)
                .pattern("TTT")
                .pattern("TTT")
                .pattern("TTT")
                .define('T', tinyPowder)
                .unlockedBy("has_" + tinyName, has(tinyPowder))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", powderName + "_from_9_" + tinyName));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, tinyPowder, 9)
                .requires(powder)
                .unlockedBy("has_" + powderName, has(powder))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", tinyName + "_from_" + powderName));
    }

    /**
     * Die beiden Watz-Bauteile, die im Original auf der Werkbank entstehen.
     *
     * ABWEICHUNGEN:
     * - ANY_RESISTANTALLOY des Originals ist im Port der Schnellarbeitsstahl, wie schon beim
     *   Radiolyse-Rezept aus Runde 40.
     * - EnumCircuitType.ADVANCED heisst im Original "Military Grade Circuit Board", BISMOID
     *   "Versatile Circuit Board"; im Port sind das eigene Gegenstaende.
     * - Der Desh-Motor fehlt im Port. Die Pumpe nimmt stattdessen den gewoehnlichen Motor und
     *   wird dadurch guenstiger als gedacht. Beim PUREX-Rezept liess sich das ueber die
     *   Stueckzahl ausgleichen; in einem Werkbankmuster mit zwei festen Feldern geht das nicht.
     *   Faellt der Desh-Motor nach, gehoeren diese beiden Felder zurueckgedreht.
     */
    /** Die drei Geraete des Chicago Pile. Muster und Zutaten unveraendert aus dem Original. */
    private void pileDevices(RecipeOutput recipeOutput) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.PILE_LOADER.get(), 1)
                .pattern(" A ").pattern("CBS")
                .define('A', NtmItems.PLATE_ALUMINIUM.get())
                .define('C', DataComponentIngredient.of(false, NtmDataComponents.META, CastPlateItem.Type.STEEL.ordinal(), NtmItems.CAST_PLATE.get()))
                .define('B', NtmItems.INGOT_BORON.get())
                .define('S', NtmItems.SHELL_STEEL.get())
                .unlockedBy("has_ingot_boron", has(NtmItems.INGOT_BORON.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.PILE_VENT.get(), 1)
                .pattern(" M ").pattern("ACA").pattern(" S ")
                .define('M', NtmItems.MOTOR.get())
                .define('A', NtmItems.PLATE_ALUMINIUM.get())
                .define('C', NtmItems.SHELL_COPPER.get())
                .define('S', DataComponentIngredient.of(false, NtmDataComponents.META, CastPlateItem.Type.STEEL.ordinal(), NtmItems.CAST_PLATE.get()))
                .unlockedBy("has_motor", has(NtmItems.MOTOR.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.PILE_CONTROL.get(), 1)
                .pattern(" B ").pattern("SBS").pattern("SBS")
                .define('B', NtmItems.INGOT_BORON.get())
                .define('S', NtmItems.PLATE_STEEL.get())
                .unlockedBy("has_ingot_boron", has(NtmItems.INGOT_BORON.get()))
                .save(recipeOutput);
    }

    private void watzParts(RecipeOutput recipeOutput) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.WATZ_PUMP.get(), 1)
                .pattern("MPM").pattern("PCP").pattern("PSP")
                .define('M', NtmItems.MOTOR.get())
                .define('P', DataComponentIngredient.of(false, NtmDataComponents.META, CastPlateItem.Type.DURA_STEEL.ordinal(), NtmItems.CAST_PLATE.get()))
                .define('C', NtmItems.CIRCUIT_VERSATILE_BOARD.get())
                .define('S', NtmItems.PIPE_DURA_STEEL.get())
                .unlockedBy("has_circuit_versatile_board", has(NtmItems.CIRCUIT_VERSATILE_BOARD.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.STRUCT_WATZ_CORE.get(), 1)
                .pattern("CBC").pattern("BHB").pattern("CBC")
                .define('C', NtmItems.CIRCUIT_MILITARY_GRADE_BOARD.get())
                .define('B', DataComponentIngredient.of(false, NtmDataComponents.META, CastPlateItem.Type.DURA_STEEL.ordinal(), NtmItems.CAST_PLATE.get()))
                .define('H', NtmBlocks.WATZ_COOLER.get())
                .unlockedBy("has_watz_cooler", has(NtmBlocks.WATZ_COOLER.get()))
                .save(recipeOutput);
    }

    private void addCrystalSmelting(RecipeOutput recipeOutput, ItemLike inputItem, ItemLike outputItem) {
        Item input = inputItem.asItem();
        Item output = outputItem.asItem();
        String inputName = BuiltInRegistries.ITEM.getKey(input).getPath();
        String outputName = BuiltInRegistries.ITEM.getKey(output).getPath();

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, new ItemStack(output, 2), 0.7F, 200)
                .unlockedBy("has_" + inputName, has(input))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("hbmsntm", outputName + "_from_" + inputName));
    }

    /** Ein leerer Stab und acht Billets ergeben einen gefuellten Brennstab. */
    private void rbmkRod(RecipeOutput recipeOutput, Item rod, Item billet) {

        ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, rod, 1)
                .requires(NtmItems.RBMK_FUEL_EMPTY.get());

        for(int i = 0; i < 8; i++) builder.requires(billet);

        builder.unlockedBy("has_empty_rod", has(NtmItems.RBMK_FUEL_EMPTY.get())).save(recipeOutput);
    }


    /**
     * Ein formloses Rezept fuer einen Allgemeinaufsatz. Alle Aufsaetze sind derselbe Gegenstand mit
     * anderem Metadatum, deshalb braucht jedes Rezept einen eigenen Namen -- sonst ueberschreiben
     * sie einander. Das Metadatum des Erzeugnisses traegt eine Datenkomponente, genau wie beim
     * Stacheldraht weiter unten.
     */
    /**
     * Eine 240-mm-Granate. Alle vier bauen sich gleich: oben der Sprengsatz oder der Wuchtkoerper,
     * in der Mitte die Treibladung um die Huelse, unten der Messingboden.
     */
    private void shell(RecipeOutput recipeOutput, GunFactory.Ammo240Shell type, ItemLike head, ItemLike body) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetaHelper.newStack(NtmItems.AMMO_SHELL.get(), 4, type.ordinal()))
                .pattern(" T ")
                .pattern("GHG")
                .pattern("CCC")
                .define('T', head)
                .define('G', NtmItems.CORDITE.get())
                .define('H', body)
                .define('C', NtmItems.INGOT_COPPER.get())
                .unlockedBy("has_cordite", has(NtmItems.CORDITE.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("ammo_shell_" + type.name().toLowerCase(Locale.US)));
    }

    private void weaponModShapeless(RecipeOutput recipeOutput, GunFactory.ModGeneric mod, ItemLike... ingredients) {

        ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC,
                MetaHelper.newStack(NtmItems.WEAPON_MOD_GENERIC.get(), 1, mod.ordinal()));

        for(ItemLike ingredient : ingredients) builder.requires(ingredient);

        builder.unlockedBy("has_ducttape", has(NtmItems.DUCTTAPE.get()))
                .save(recipeOutput, NuclearTechMod.withDefaultNamespace("weapon_mod_generic_" + mod.name().toLowerCase(Locale.US)));
    }


    /**
     * Runde 136: Schutzkleidung, Gasmasken und Filter.
     *
     * Das gelbe Schutztuch kommt aus der Montagefabrik (ass.hazcloth), alles Weitere aus der
     * Werkbank -- die Formen sind unveraendert aus ArmorRecipes und ConsumableRecipes des
     * Originals. KEY_ANYPANE des Originals wird zur Marke c:glass_panes.
     */
    private void hazmatGear(RecipeOutput recipeOutput) {

        /* Die beiden besseren Tuecher bauen aufeinander auf. */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.HAZMAT_CLOTH_RED.get(), 1)
                .pattern("C")
                .pattern("R")
                .pattern("C")
                .define('C', NtmItems.HAZMAT_CLOTH.get())
                .define('R', Items.REDSTONE)
                .unlockedBy("has_hazmat_cloth", has(NtmItems.HAZMAT_CLOTH.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.HAZMAT_CLOTH_GREY.get(), 1)
                .pattern(" P ")
                .pattern("ICI")
                .pattern(" L ")
                .define('C', NtmItems.HAZMAT_CLOTH_RED.get())
                .define('P', NtmItems.PLATE_IRON.get())
                .define('L', NtmItems.PLATE_LEAD.get())
                .define('I', NtmItems.INGOT_RUBBER.get())
                .unlockedBy("has_hazmat_cloth_red", has(NtmItems.HAZMAT_CLOTH_RED.get()))
                .save(recipeOutput);

        /* Die drei Anzuege. Nur der Helm des gelben Anzugs hat eine andere Form als die
         * beiden anderen -- so ist es auch im Original. */
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, NtmItems.HAZMAT_HELMET.get(), 1)
                .pattern("EEE")
                .pattern("EIE")
                .pattern(" P ")
                .define('E', NtmItems.HAZMAT_CLOTH.get())
                .define('I', Tags.Items.GLASS_PANES)
                .define('P', NtmItems.PLATE_IRON.get())
                .unlockedBy("has_hazmat_cloth", has(NtmItems.HAZMAT_CLOTH.get()))
                .save(recipeOutput);
        hazmatChest(recipeOutput, NtmItems.HAZMAT_PLATE.get(), NtmItems.HAZMAT_CLOTH.get());
        hazmatLegs(recipeOutput, NtmItems.HAZMAT_LEGS.get(), NtmItems.HAZMAT_CLOTH.get());
        hazmatBoots(recipeOutput, NtmItems.HAZMAT_BOOTS.get(), NtmItems.HAZMAT_CLOTH.get());

        hazmatHelmet(recipeOutput, NtmItems.HAZMAT_HELMET_RED.get(), NtmItems.HAZMAT_CLOTH_RED.get());
        hazmatChest(recipeOutput, NtmItems.HAZMAT_PLATE_RED.get(), NtmItems.HAZMAT_CLOTH_RED.get());
        hazmatLegs(recipeOutput, NtmItems.HAZMAT_LEGS_RED.get(), NtmItems.HAZMAT_CLOTH_RED.get());
        hazmatBoots(recipeOutput, NtmItems.HAZMAT_BOOTS_RED.get(), NtmItems.HAZMAT_CLOTH_RED.get());

        hazmatHelmet(recipeOutput, NtmItems.HAZMAT_HELMET_GREY.get(), NtmItems.HAZMAT_CLOTH_GREY.get());
        hazmatChest(recipeOutput, NtmItems.HAZMAT_PLATE_GREY.get(), NtmItems.HAZMAT_CLOTH_GREY.get());
        hazmatLegs(recipeOutput, NtmItems.HAZMAT_LEGS_GREY.get(), NtmItems.HAZMAT_CLOTH_GREY.get());
        hazmatBoots(recipeOutput, NtmItems.HAZMAT_BOOTS_GREY.get(), NtmItems.HAZMAT_CLOTH_GREY.get());

        /* Der PAA-Anzug wird aus Platten statt aus Tuch gebaut; sein Helm hat wieder die
         * Form des gelben. */
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, NtmItems.HAZMAT_PAA_HELMET.get(), 1)
                .pattern("EEE")
                .pattern("IEI")
                .pattern(" P ")
                .define('E', NtmItems.PLATE_PAA.get())
                .define('I', Tags.Items.GLASS_PANES)
                .define('P', NtmItems.PLATE_IRON.get())
                .unlockedBy("has_plate_paa", has(NtmItems.PLATE_PAA.get()))
                .save(recipeOutput);
        hazmatChest(recipeOutput, NtmItems.HAZMAT_PAA_PLATE.get(), NtmItems.PLATE_PAA.get());
        hazmatLegs(recipeOutput, NtmItems.HAZMAT_PAA_LEGS.get(), NtmItems.PLATE_PAA.get());
        hazmatBoots(recipeOutput, NtmItems.HAZMAT_PAA_BOOTS.get(), NtmItems.PLATE_PAA.get());

        /* Die vier Masken: gleiche Form, anderes Material. Die Halbmaske hat eine eigene. */
        gasMask(recipeOutput, NtmItems.GAS_MASK.get(), NtmItems.PLATE_STEEL.get(), NtmItems.PLATE_IRON.get());
        gasMask(recipeOutput, NtmItems.GAS_MASK_M65.get(), NtmItems.INGOT_RUBBER.get(), NtmItems.PLATE_IRON.get());
        gasMask(recipeOutput, NtmItems.GAS_MASK_OLDE.get(), Items.LEATHER, Items.IRON_INGOT);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, NtmItems.GAS_MASK_MONO.get(), 1)
                .pattern(" P ")
                .pattern("PPP")
                .pattern(" F ")
                .define('P', NtmItems.INGOT_RUBBER.get())
                .define('F', NtmItems.PLATE_IRON.get())
                .unlockedBy("has_rubber", has(NtmItems.INGOT_RUBBER.get()))
                .save(recipeOutput);

        /* Der Lappen und was daraus wird. */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.RAG.get(), 4)
                .pattern("SW")
                .pattern("WS")
                .define('S', Items.STRING)
                .define('W', ItemTags.WOOL)
                .unlockedBy("has_string", has(Items.STRING))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, NtmItems.MASK_RAG.get(), 1)
                .pattern("RRR")
                .define('R', NtmItems.RAG_DAMP.get())
                .unlockedBy("has_rag_damp", has(NtmItems.RAG_DAMP.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, NtmItems.MASK_PISS.get(), 1)
                .pattern("RRR")
                .define('R', NtmItems.RAG_PISS.get())
                .unlockedBy("has_rag_piss", has(NtmItems.RAG_PISS.get()))
                .save(recipeOutput);

        /* Die fuenf Filtereinsaetze. */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.GAS_MASK_FILTER.get(), 1)
                .pattern("I")
                .pattern("F")
                .define('F', NtmItems.FILTER_COAL.get())
                .define('I', NtmItems.PLATE_IRON.get())
                .unlockedBy("has_filter_coal", has(NtmItems.FILTER_COAL.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.GAS_MASK_FILTER_MONO.get(), 1)
                .pattern("ZZZ")
                .pattern("ZCZ")
                .pattern("ZZZ")
                .define('Z', NtmItems.NUGGET_ZIRCONIUM.get())
                .define('C', NtmItems.CATALYST_CLAY.get())
                .unlockedBy("has_catalyst_clay", has(NtmItems.CATALYST_CLAY.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.GAS_MASK_FILTER_COMBO.get(), 1)
                .pattern("ZCZ")
                .pattern("CFC")
                .pattern("ZCZ")
                .define('Z', NtmItems.INGOT_ZIRCONIUM.get())
                .define('C', NtmItems.CATALYST_CLAY.get())
                .define('F', NtmItems.GAS_MASK_FILTER.get())
                .unlockedBy("has_gas_mask_filter", has(NtmItems.GAS_MASK_FILTER.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.GAS_MASK_FILTER_RAG.get(), 1)
                .pattern("I")
                .pattern("F")
                .define('F', NtmItems.RAG_DAMP.get())
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_rag_damp", has(NtmItems.RAG_DAMP.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.GAS_MASK_FILTER_PISS.get(), 1)
                .pattern("I")
                .pattern("F")
                .define('F', NtmItems.RAG_PISS.get())
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_rag_piss", has(NtmItems.RAG_PISS.get()))
                .save(recipeOutput);
    }

    /** Die Helmform der beiden besseren Anzuege: Sichtscheiben seitlich, Platte unten. */
    private void hazmatHelmet(RecipeOutput recipeOutput, ItemLike result, ItemLike cloth) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result, 1)
                .pattern("EEE")
                .pattern("IEI")
                .pattern("EFE")
                .define('E', cloth)
                .define('I', Tags.Items.GLASS_PANES)
                .define('F', NtmItems.PLATE_IRON.get())
                .unlockedBy("has_cloth", has(cloth))
                .save(recipeOutput);
    }

    private void hazmatChest(RecipeOutput recipeOutput, ItemLike result, ItemLike cloth) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result, 1)
                .pattern("E E")
                .pattern("EEE")
                .pattern("EEE")
                .define('E', cloth)
                .unlockedBy("has_cloth", has(cloth))
                .save(recipeOutput);
    }

    private void hazmatLegs(RecipeOutput recipeOutput, ItemLike result, ItemLike cloth) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result, 1)
                .pattern("EEE")
                .pattern("E E")
                .pattern("E E")
                .define('E', cloth)
                .unlockedBy("has_cloth", has(cloth))
                .save(recipeOutput);
    }

    private void hazmatBoots(RecipeOutput recipeOutput, ItemLike result, ItemLike cloth) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result, 1)
                .pattern("E E")
                .pattern("E E")
                .define('E', cloth)
                .unlockedBy("has_cloth", has(cloth))
                .save(recipeOutput);
    }

    /** Die gemeinsame Form der Vollmasken: Sichtscheiben seitlich, Filtergewinde unten. */
    private void gasMask(RecipeOutput recipeOutput, ItemLike result, ItemLike body, ItemLike mount) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result, 1)
                .pattern("PPP")
                .pattern("GPG")
                .pattern(" F ")
                .define('G', Tags.Items.GLASS_PANES)
                .define('P', body)
                .define('F', mount)
                .unlockedBy("has_body", has(body))
                .save(recipeOutput);
    }


    /**
     * Runde 137: der Ruestungstisch, die Auskleidungen und die Einlagen.
     *
     * Die Formen sind unveraendert aus ArmorRecipes und ConsumableRecipes des Originals.
     *
     * ABWEICHUNG: die XSAPI-Einlage braucht im Original eine Platte aus magnetisiertem
     * Wolfram. Die gibt es im Port nicht -- hier steht der Barren an ihrer Stelle.
     * NICHT UEBERNOMMEN: insert_doxium hat auch im Original kein Rezept.
     */
    private void armorMods(RecipeOutput recipeOutput) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.ARMOR_TABLE.get(), 1)
                .pattern("PPP")
                .pattern("TCT")
                .pattern("TST")
                .define('P', NtmItems.PLATE_STEEL.get())
                .define('T', NtmItems.INGOT_TUNGSTEN.get())
                .define('C', Blocks.CRAFTING_TABLE)
                .define('S', NtmBlocks.BLOCK_STEEL.get())
                .unlockedBy("has_steel_plate", has(NtmItems.PLATE_STEEL.get()))
                .save(recipeOutput);

        /* Die Bleifarbe ist die einzige formlose unter den Auskleidungen. */
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NtmItems.CLADDING_PAINT.get(), 1)
                .requires(NtmItems.NUGGET_LEAD.get(), 4)
                .requires(Items.CLAY_BALL)
                .requires(Items.GLASS_BOTTLE)
                .unlockedBy("has_lead_nugget", has(NtmItems.NUGGET_LEAD.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CLADDING_RUBBER.get(), 1)
                .pattern("RCR")
                .pattern("CDC")
                .pattern("RCR")
                .define('R', NtmItems.INGOT_RUBBER.get())
                .define('C', NtmItems.POWDER_COAL.get())
                .define('D', NtmItems.DUCTTAPE.get())
                .unlockedBy("has_rubber", has(NtmItems.INGOT_RUBBER.get()))
                .save(recipeOutput);

        /* Blei, Desh und Ghiorsium bauen der Reihe nach aufeinander auf. */
        claddingUpgrade(recipeOutput, NtmItems.CLADDING_LEAD.get(), NtmItems.CLADDING_RUBBER.get(), NtmItems.PLATE_LEAD.get());
        claddingUpgrade(recipeOutput, NtmItems.CLADDING_DESH.get(), NtmItems.CLADDING_LEAD.get(), NtmItems.PLATE_DESH.get());
        claddingUpgrade(recipeOutput, NtmItems.CLADDING_GHIORSIUM.get(), NtmItems.CLADDING_DESH.get(), NtmItems.INGOT_GH336.get());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CLADDING_OBSIDIAN.get(), 1)
                .pattern("OOO")
                .pattern("PDP")
                .pattern("OOO")
                .define('O', Blocks.OBSIDIAN)
                .define('P', NtmItems.PLATE_STEEL.get())
                .define('D', NtmItems.DUCTTAPE.get())
                .unlockedBy("has_obsidian", has(Blocks.OBSIDIAN))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CLADDING_IRON.get(), 1)
                .pattern("OOO")
                .pattern("PDP")
                .pattern("OOO")
                .define('O', NtmItems.PLATE_IRON.get())
                .define('P', NtmItems.PLATE_POLYMER.get())
                .define('D', NtmItems.DUCTTAPE.get())
                .unlockedBy("has_iron_plate", has(NtmItems.PLATE_IRON.get()))
                .save(recipeOutput);

        /* Die schweren Einlagen: ein Kern aus dem namengebenden Stoff, ringsherum Eisen. */
        heavyInsert(recipeOutput, NtmItems.INSERT_STEEL.get(), NtmItems.PLATE_IRON.get(), NtmBlocks.BLOCK_STEEL.get());
        heavyInsert(recipeOutput, NtmItems.INSERT_DU.get(), NtmItems.PLATE_IRON.get(), NtmBlocks.BLOCK_U238.get());
        heavyInsert(recipeOutput, NtmItems.INSERT_GHIORSIUM.get(), NtmItems.INGOT_GH336.get(), NtmItems.INGOT_U238.get());
        heavyInsert(recipeOutput, NtmItems.INSERT_POLONIUM.get(), NtmItems.PLATE_IRON.get(), NtmBlocks.BLOCK_POLONIUM.get());
        heavyInsert(recipeOutput, NtmItems.INSERT_ERA.get(), NtmItems.PLATE_IRON.get(), NtmItems.INGOT_SEMTEX.get());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.INSERT_KEVLAR.get(), 1)
                .pattern("KIK")
                .pattern("IDI")
                .pattern("KIK")
                .define('K', NtmItems.PLATE_KEVLAR.get())
                .define('I', NtmItems.INGOT_RUBBER.get())
                .define('D', NtmItems.DUCTTAPE.get())
                .unlockedBy("has_kevlar_plate", has(NtmItems.PLATE_KEVLAR.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.INSERT_SAPI.get(), 1)
                .pattern("PKP")
                .pattern("DPD")
                .pattern("PKP")
                .define('P', NtmItems.INGOT_PC.get())
                .define('K', NtmItems.INSERT_KEVLAR.get())
                .define('D', NtmItems.DUCTTAPE.get())
                .unlockedBy("has_insert_kevlar", has(NtmItems.INSERT_KEVLAR.get()))
                .save(recipeOutput);

        platedInsert(recipeOutput, NtmItems.INSERT_ESAPI.get(), NtmItems.INGOT_PC.get(), NtmItems.INSERT_SAPI.get(), NtmItems.PLATE_WEAPON_STEEL.get());
        platedInsert(recipeOutput, NtmItems.INSERT_XSAPI.get(), NtmItems.INGOT_ASBESTOS.get(), NtmItems.INSERT_ESAPI.get(), NtmItems.INGOT_MAGNETIZED_TUNGSTEN.get());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.INSERT_YHARONITE.get(), 1)
                .pattern("YIY")
                .pattern("IYI")
                .pattern("YIY")
                .define('Y', NtmItems.BILLET_YHARONITE.get())
                .define('I', NtmItems.INSERT_DU.get())
                .unlockedBy("has_yharonite_billet", has(NtmItems.BILLET_YHARONITE.get()))
                .save(recipeOutput);
    }

    /** Eine Auskleidung, die eine schwaechere umschliesst: aussen Klebeband, dazwischen Platten. */
    private void claddingUpgrade(RecipeOutput recipeOutput, ItemLike result, ItemLike previous, ItemLike plate) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result, 1)
                .pattern("DPD")
                .pattern("PRP")
                .pattern("DPD")
                .define('R', previous)
                .define('P', plate)
                .define('D', NtmItems.DUCTTAPE.get())
                .unlockedBy("has_previous", has(previous))
                .save(recipeOutput);
    }

    /** Eine schwere Einlage: ein Kern, ringsherum Platten und Klebeband. */
    private void heavyInsert(RecipeOutput recipeOutput, ItemLike result, ItemLike plate, ItemLike core) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result, 1)
                .pattern("DPD")
                .pattern("PSP")
                .pattern("DPD")
                .define('D', NtmItems.DUCTTAPE.get())
                .define('P', plate)
                .define('S', core)
                .unlockedBy("has_core", has(core))
                .save(recipeOutput);
    }

    /** Eine Einlage, die eine schwaechere um eine zusaetzliche Platte erweitert. */
    private void platedInsert(RecipeOutput recipeOutput, ItemLike result, ItemLike shell, ItemLike previous, ItemLike plate) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result, 1)
                .pattern("PKP")
                .pattern("DSD")
                .pattern("PKP")
                .define('P', shell)
                .define('K', previous)
                .define('D', NtmItems.DUCTTAPE.get())
                .define('S', plate)
                .unlockedBy("has_previous", has(previous))
                .save(recipeOutput);
    }


    /**
     * Runde 138: der Fraktionierturm und sein Zwischenstueck.
     *
     * Die Formen sind unveraendert aus CraftingManager des Originals: der Turm ist ein
     * Gitterrost zwischen zwei geschweissten Stahlplatten, das Zwischenstueck sind zwei
     * Eisengitter um eine Stahlhuelse.
     */
    private void oilChain(RecipeOutput recipeOutput) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.MACHINE_FRACTION_TOWER.get(), 1)
                .pattern("H")
                .pattern("G")
                .pattern("H")
                .define('H', DataComponentIngredient.of(false, NtmDataComponents.META, CastPlateItem.Type.STEEL.ordinal(), NtmItems.CAST_PLATE_WELDED.get()))
                .define('G', NtmBlocks.STEEL_GRATE.get())
                .unlockedBy("has_steel_grate", has(NtmBlocks.STEEL_GRATE.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmBlocks.FRACTION_SPACER.get(), 1)
                .pattern("BHB")
                .define('H', NtmItems.SHELL_STEEL.get())
                .define('B', Blocks.IRON_BARS)
                .unlockedBy("has_steel_shell", has(NtmItems.SHELL_STEEL.get()))
                .save(recipeOutput);

        /* Runde 139: der Katalysator des Reformers. Er wird nicht verbraucht.
         * ABWEICHUNG: ANY_HARDPLASTIC wird Polycarbonat und ANY_BISMOID Wismutbronze,
         * wie in allen Runden davor. */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.CATALYTIC_CONVERTER.get(), 1)
                .pattern("PCP")
                .pattern("PBP")
                .pattern("PCP")
                .define('P', NtmItems.INGOT_PC.get())
                .define('C', NtmItems.POWDER_COBALT.get())
                .define('B', NtmItems.INGOT_BISMUTH_BRONZE.get())
                .unlockedBy("has_cobalt_powder", has(NtmItems.POWDER_COBALT.get()))
                .save(recipeOutput);

        /* ---- Stufe 5: der Sternmetallblock ---- */

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, NtmBlocks.BLOCK_STARMETAL.get(), 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', NtmItems.INGOT_STARMETAL.get())
                .unlockedBy("has_starmetal", has(NtmItems.INGOT_STARMETAL.get()))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NtmItems.INGOT_STARMETAL.get(), 9)
                .requires(NtmBlocks.BLOCK_STARMETAL.get())
                .unlockedBy("has_starmetal_block", has(NtmBlocks.BLOCK_STARMETAL.get()))
                .save(recipeOutput, "ingot_starmetal_from_block");

        /* ---- Runde 170: die Hammerkette und das Buch ---- */

        /* Der Holzhammer. Das Original nimmt KEY_SLAB, KEY_LOG und KEY_STICK -- also jede
         * Holzart; im Port stehen dafuer die Sammelbegriffe. */
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, NtmItems.WOOD_GAVEL.get(), 1)
                .pattern("SWS")
                .pattern(" R ")
                .pattern(" R ")
                .define('S', ItemTags.WOODEN_SLABS)
                .define('W', ItemTags.LOGS)
                .define('R', Items.STICK)
                .unlockedBy("has_stick", has(Items.STICK))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, NtmItems.LEAD_GAVEL.get(), 1)
                .pattern("PIP")
                .pattern("IGI")
                .pattern("PIP")
                .define('P', NtmItems.PELLET_BUCKSHOT.get())
                .define('I', NtmItems.INGOT_LEAD.get())
                .define('G', NtmItems.WOOD_GAVEL.get())
                .unlockedBy("has_wood_gavel", has(NtmItems.WOOD_GAVEL.get()))
                .save(recipeOutput);

        /* Das Buch. Ohne dieses Rezept gaebe es das Buch nur aus Beute und vom Bobmazon --
         * und damit auch den Diamanthammer nicht, dessen Rezept im Buch steht. */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NtmItems.BOOK_OF_.get(), 1)
                .pattern("BGB")
                .pattern("GAG")
                .pattern("BGB")
                .define('B', NtmItems.EGG_BALEFIRE_SHARD.get())
                .define('G', Items.GOLD_INGOT)
                .define('A', Items.BOOK)
                .unlockedBy("has_balefire_shard", has(NtmItems.EGG_BALEFIRE_SHARD.get()))
                .save(recipeOutput);
    }

}
