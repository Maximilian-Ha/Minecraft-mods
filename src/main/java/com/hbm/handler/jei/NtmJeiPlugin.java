package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.handler.jei.AssemblyMachineRecipeHandler;
import com.hbm.handler.jei.AssemblyMachineTransferInfo;
import com.hbm.handler.jei.BoilerRecipeHandler;
import com.hbm.handler.jei.ShredderRecipeHandler;
import com.hbm.handler.jei.subtypes.BatterySubtypeInterpreter;
import com.hbm.handler.jei.subtypes.MetaSubtypeInterpreter;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.ArcWelderRecipes;
import com.hbm.inventory.recipes.BlastFurnaceRecipes;
import com.hbm.inventory.recipes.ChemicalPlantRecipes;
import com.hbm.inventory.recipes.SuperComputerRecipes;
import com.hbm.inventory.recipes.PlasmaForgeRecipes;
import com.hbm.inventory.recipes.CentrifugeRecipes;
import com.hbm.inventory.recipes.CombinationRecipes;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.SolderingRecipes;
import com.hbm.inventory.recipes.PUREXRecipes;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.inventory.screens.AnvilMenuScreen;
import com.hbm.inventory.screens.MachineFurnaceCombinationScreen;
import com.hbm.inventory.screens.MachineAssemblyMachineScreen;
import com.hbm.inventory.screens.MachineArcWelderScreen;
import com.hbm.inventory.screens.MachineSolderingStationScreen;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.FluidIconItem;
import com.hbm.main.NuclearTechMod;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
@SuppressWarnings("unused")
public class NtmJeiPlugin implements IModPlugin {

    @Override public ResourceLocation getPluginUid() { return NuclearTechMod.withDefaultNamespace("jei_plugin"); }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new AnvilConstructionRecipeHandler(guiHelper),
                new AnvilRecipeHandler(guiHelper),
                new SolderingStationRecipeHandler(guiHelper),
                new ArcWelderRecipeHandler(guiHelper),
                new AssemblyMachineRecipeHandler(guiHelper),
                new PlasmaForgeRecipeHandler(guiHelper),
                new BlastFurnaceRecipeHandler(guiHelper),
                new CentrifugeRecipeHandler(guiHelper),
                new ChemicalPlantRecipeHandler(guiHelper),
                new SuperComputerRecipeHandler(guiHelper),
                new AmmoPressRecipeHandler(guiHelper),
                new FurnaceCombinationRecipeHandler(guiHelper),
                new PressRecipeHandler(guiHelper),
                new ShredderRecipeHandler(guiHelper),
                new RefineryRecipeHandler(guiHelper),
                new BoilerRecipeHandler(guiHelper),
                new CompressorRecipeHandler(guiHelper),
                new GasCentrifugeRecipeHandler(guiHelper),
                new CyclotronRecipeHandler(guiHelper),
                new SILEXRecipeHandler(guiHelper),
                new BookRecipeHandler(guiHelper),
                new ArcFurnaceRecipeHandler(guiHelper),
                new CrucibleAlloyingRecipeHandler(guiHelper),
                new CrucibleCastingRecipeHandler(guiHelper),
                new PUREXRecipeHandler(guiHelper),
                new RadiolysisRecipeHandler(guiHelper),
                new ZirnoxRecipeHandler(guiHelper),
                new WatzRecipeHandler(guiHelper),
                new PWRRecipeHandler(guiHelper),
                new FuelPoolRecipeHandler(guiHelper),
                new RTGRecipeHandler(guiHelper),
                new RBMKDisassemblyRecipeHandler(guiHelper),
                new FractionRecipeHandler(guiHelper),
                new ReformingRecipeHandler(guiHelper),
                new HydrotreatingRecipeHandler(guiHelper),
                new VacuumRefineryRecipeHandler(guiHelper),
                new SolidificationRecipeHandler(guiHelper),
                new LiquefactionRecipeHandler(guiHelper),
                new PyroOvenRecipeHandler(guiHelper),
                new CokerRecipeHandler(guiHelper),
                new CrackingRecipeHandler(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(AnvilConstructionRecipeHandler.RECIPE_TYPE, AnvilRecipes.getConstruction());
        registration.addRecipes(AnvilRecipeHandler.RECIPE_TYPE, AnvilRecipes.getSmithing());
        registration.addRecipes(SolderingStationRecipeHandler.RECIPE_TYPE, SolderingRecipes.recipes);
        registration.addRecipes(ArcWelderRecipeHandler.RECIPE_TYPE, ArcWelderRecipes.recipes);
        registration.addRecipes(AssemblyMachineRecipeHandler.RECIPE_TYPE, AssemblyMachineRecipes.INSTANCE.recipeOrderedList);
        registration.addRecipes(PlasmaForgeRecipeHandler.RECIPE_TYPE, PlasmaForgeRecipes.INSTANCE.recipeOrderedList);
        registration.addRecipes(BlastFurnaceRecipeHandler.RECIPE_TYPE, BlastFurnaceRecipes.INSTANCE.recipeOrderedList);
        registration.addRecipes(CentrifugeRecipeHandler.RECIPE_TYPE, CentrifugeRecipes.getJeiRecipes());
        registration.addRecipes(ChemicalPlantRecipeHandler.RECIPE_TYPE, ChemicalPlantRecipes.INSTANCE.recipeOrderedList);
        registration.addRecipes(SuperComputerRecipeHandler.RECIPE_TYPE, SuperComputerRecipes.INSTANCE.recipeOrderedList);
        registration.addRecipes(AmmoPressRecipeHandler.RECIPE_TYPE, AmmoPressRecipeHandler.getRecipes());
        registration.addRecipes(FurnaceCombinationRecipeHandler.RECIPE_TYPE, CombinationRecipes.getJeiRecipes());
        registration.addRecipes(PressRecipeHandler.RECIPE_TYPE, PressRecipeHandler.getRecipes());
        registration.addRecipes(ShredderRecipeHandler.RECIPE_TYPE, ShredderRecipes.getJeiRecipes());
        registration.addRecipes(RefineryRecipeHandler.RECIPE_TYPE, RefineryRecipeHandler.getRecipes());
        registration.addRecipes(BoilerRecipeHandler.RECIPE_TYPE, BoilerRecipeHandler.getRecipes());
        registration.addRecipes(CompressorRecipeHandler.RECIPE_TYPE, CompressorRecipeHandler.getRecipes());
        registration.addRecipes(GasCentrifugeRecipeHandler.RECIPE_TYPE, GasCentrifugeRecipeHandler.getRecipes());
        registration.addRecipes(CyclotronRecipeHandler.RECIPE_TYPE, CyclotronRecipeHandler.getRecipes());
        registration.addRecipes(SILEXRecipeHandler.RECIPE_TYPE, SILEXRecipeHandler.getRecipes());
        registration.addRecipes(BookRecipeHandler.RECIPE_TYPE, BookRecipeHandler.getRecipes());
        registration.addRecipes(ArcFurnaceRecipeHandler.RECIPE_TYPE, ArcFurnaceRecipeHandler.getRecipes());
        registration.addRecipes(CrucibleAlloyingRecipeHandler.RECIPE_TYPE, CrucibleRecipes.INSTANCE.recipeOrderedList);
        registration.addRecipes(CrucibleCastingRecipeHandler.RECIPE_TYPE, CrucibleCastingRecipeHandler.getRecipes());
        registration.addRecipes(PUREXRecipeHandler.RECIPE_TYPE, PUREXRecipes.INSTANCE.recipeOrderedList);
        registration.addRecipes(RadiolysisRecipeHandler.RECIPE_TYPE, RadiolysisRecipeHandler.getRecipes());
        registration.addRecipes(ZirnoxRecipeHandler.RECIPE_TYPE, ZirnoxRecipeHandler.getRecipes());
        registration.addRecipes(WatzRecipeHandler.RECIPE_TYPE, WatzRecipeHandler.getRecipes());
        registration.addRecipes(PWRRecipeHandler.RECIPE_TYPE, PWRRecipeHandler.getRecipes());
        registration.addRecipes(FuelPoolRecipeHandler.RECIPE_TYPE, FuelPoolRecipeHandler.getRecipes());
        registration.addRecipes(RTGRecipeHandler.RECIPE_TYPE, RTGRecipeHandler.getRecipes());
        registration.addRecipes(RBMKDisassemblyRecipeHandler.RECIPE_TYPE, RBMKDisassemblyRecipeHandler.getRecipes());

        /* ---- die Erdoelkette, Runde 148 ---- */
        registration.addRecipes(FractionRecipeHandler.RECIPE_TYPE, FractionRecipeHandler.getRecipes());
        registration.addRecipes(ReformingRecipeHandler.RECIPE_TYPE, ReformingRecipeHandler.getRecipes());
        registration.addRecipes(HydrotreatingRecipeHandler.RECIPE_TYPE, HydrotreatingRecipeHandler.getRecipes());
        registration.addRecipes(VacuumRefineryRecipeHandler.RECIPE_TYPE, VacuumRefineryRecipeHandler.getRecipes());
        registration.addRecipes(SolidificationRecipeHandler.RECIPE_TYPE, SolidificationRecipeHandler.getRecipes());
        registration.addRecipes(LiquefactionRecipeHandler.RECIPE_TYPE, LiquefactionRecipeHandler.getRecipes());
        registration.addRecipes(PyroOvenRecipeHandler.RECIPE_TYPE, PyroOvenRecipeHandler.getRecipes());
        registration.addRecipes(CokerRecipeHandler.RECIPE_TYPE, CokerRecipeHandler.getRecipes());
        registration.addRecipes(CrackingRecipeHandler.RECIPE_TYPE, CrackingRecipeHandler.getRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

        /* ---- die Erdoelkette, Runde 148 ---- */
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_FRACTION_TOWER.asItem(),
                FractionRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_CATALYTIC_REFORMER.asItem(),
                ReformingRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_HYDROTREATER.asItem(),
                HydrotreatingRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_VACUUM_DISTILL.asItem(),
                VacuumRefineryRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_SOLIDIFIER.asItem(),
                SolidificationRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_LIQUEFACTOR.asItem(),
                LiquefactionRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_PYRO_OVEN.asItem(),
                PyroOvenRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_COKER.asItem(),
                CokerRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_CATALYTIC_CRACKER.asItem(),
                CrackingRecipeHandler.RECIPE_TYPE
        );
        for(com.hbm.blocks.machine.NTMAnvilBlock.Variant variant : com.hbm.blocks.machine.NTMAnvilBlock.Variant.values()) {
            registration.addRecipeCatalyst(
                    MetaHelper.newStack(NtmBlocks.ANVIL.asItem(), variant.ordinal()),
                    AnvilConstructionRecipeHandler.RECIPE_TYPE
            );
            registration.addRecipeCatalyst(
                    MetaHelper.newStack(NtmBlocks.ANVIL.asItem(), variant.ordinal()),
                    AnvilRecipeHandler.RECIPE_TYPE
            );
        }

        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_SOLDERING_STATION.asItem(),
                SolderingStationRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_ARC_WELDER.asItem(),
                ArcWelderRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_ASSEMBLY_MACHINE.asItem(),
                AssemblyMachineRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.FUSION_PLASMA_FORGE.asItem(),
                PlasmaForgeRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_BLAST_FURNACE.asItem(),
                BlastFurnaceRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_SHREDDER.asItem(),
                ShredderRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_CENTRIFUGE.asItem(),
                CentrifugeRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_CHEMICAL_PLANT.asItem(),
                ChemicalPlantRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_SUPER_COMPUTER.asItem(),
                SuperComputerRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_AMMO_PRESS.asItem(),
                AmmoPressRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.FURNACE_COMBINATION.asItem(),
                FurnaceCombinationRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_PRESS.asItem(),
                PressRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_ARC_FURNACE.asItem(),
                ArcFurnaceRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_CRUCIBLE.asItem(),
                CrucibleAlloyingRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.FOUNDRY_MOLD.asItem(),
                CrucibleCastingRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.FOUNDRY_BASIN.asItem(),
                CrucibleCastingRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_REFINERY.asItem(),
                RefineryRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.HEAT_BOILER.asItem(),
                BoilerRecipeHandler.RECIPE_TYPE
        );
        // Beide Bauformen des Verdichters zeigen dieselben Rezepte.
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_COMPRESSOR.asItem(),
                CompressorRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_COMPRESSOR_COMPACT.asItem(),
                CompressorRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_GAS_CENT.asItem(),
                GasCentrifugeRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_CYCLOTRON.asItem(),
                CyclotronRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_SILEX.asItem(),
                SILEXRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_FEL.asItem(),
                SILEXRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmItems.BOOK_OF_.get(),
                BookRecipeHandler.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                NtmBlocks.MACHINE_INDUSTRIAL_BOILER.asItem(),
                BoilerRecipeHandler.RECIPE_TYPE
        );

        // Der Reaktorzweig
        registration.addRecipeCatalyst(NtmBlocks.MACHINE_PUREX.asItem(), PUREXRecipeHandler.RECIPE_TYPE);
        registration.addRecipeCatalyst(NtmBlocks.MACHINE_RADIOLYSIS.asItem(), RadiolysisRecipeHandler.RECIPE_TYPE);
        registration.addRecipeCatalyst(NtmBlocks.REACTOR_ZIRNOX.asItem(), ZirnoxRecipeHandler.RECIPE_TYPE);
        registration.addRecipeCatalyst(NtmBlocks.WATZ.asItem(), WatzRecipeHandler.RECIPE_TYPE);
        registration.addRecipeCatalyst(NtmBlocks.PWR_CONTROLLER.asItem(), PWRRecipeHandler.RECIPE_TYPE);
        registration.addRecipeCatalyst(NtmBlocks.MACHINE_WASTE_DRUM.asItem(), FuelPoolRecipeHandler.RECIPE_TYPE);
        // Das Original zeigt beim RTG-Zerfall sowohl den RTG als auch den Doppelofen.
        registration.addRecipeCatalyst(NtmBlocks.MACHINE_RTG.asItem(), RTGRecipeHandler.RECIPE_TYPE);
        registration.addRecipeCatalyst(NtmBlocks.MACHINE_DIFURNACE_RTG.asItem(), RTGRecipeHandler.RECIPE_TYPE);
        registration.addRecipeCatalyst(NtmBlocks.RBMK_ROD.asItem(), RBMKDisassemblyRecipeHandler.RECIPE_TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new SolderingStationTransferInfo());
        registration.addRecipeTransferHandler(new AssemblyMachineTransferInfo());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(AnvilMenuScreen.class, 12, 50, 36, 16, AnvilConstructionRecipeHandler.RECIPE_TYPE);
        registration.addRecipeClickArea(MachineSolderingStationScreen.class, 72, 29, 32, 13, SolderingStationRecipeHandler.RECIPE_TYPE);
        registration.addRecipeClickArea(MachineArcWelderScreen.class, 72, 36, 32, 13, ArcWelderRecipeHandler.RECIPE_TYPE);
        registration.addRecipeClickArea(MachineFurnaceCombinationScreen.class, 54, 61, 18, 18, FurnaceCombinationRecipeHandler.RECIPE_TYPE);
        registration.addRecipeClickArea(com.hbm.inventory.screens.MachinePressScreen.class, 79, 35, 18, 18, PressRecipeHandler.RECIPE_TYPE);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {

        List<Item> ignoreMeta = List.of(

                NtmItems.ROD.get(),
                NtmItems.ROD_DUAL.get(),
                NtmItems.ROD_QUAD.get(),

                NtmItems.STARTER_KIT.get(),

                NtmItems.MISSILE_SOYUZ.get(),

                NtmItems.BATTERY_SC.get(),
                NtmItems.WIRE_DENSE.get(),
                NtmItems.BOLT.get(),
                NtmItems.PART_GENERIC.get(),
                NtmItems.CAST_PLATE.get(),
                NtmItems.CAST_PLATE_WELDED.get(),

                NtmItems.FLUID_TANK_FULL.get(),
                NtmItems.FLUID_TANK_LEAD_FULL.get(),
                NtmItems.FLUID_BARREL_FULL.get(),
                NtmItems.FLUID_PACK_FULL.get(),

                NtmItems.SATELLITE.get(),

                NtmItems.FLUID_ICON.get(),
                NtmItems.FLUID_IDENTIFIER_MULTI.get(),
                NtmItems.INGOT_RAW.get(),

                NtmItems.DRINK.get(),
                NtmItems.CANNED_CONSERVE.get(),
                NtmItems.CAP.get(),

                NtmBlocks.BOBBLEHEAD.asItem(),
                NtmBlocks.PLUSHIE.asItem(),

                NtmBlocks.BARBED_WIRE.asItem(),

                NtmBlocks.FLUID_DUCT_NEO.asItem(),

                NtmBlocks.CRASHED_BOMB.asItem()
        );

        for(Item item : ignoreMeta) {
            registration.registerSubtypeInterpreter(item, MetaSubtypeInterpreter.INSTANCE);
        }

        registration.registerSubtypeInterpreter(NtmItems.BATTERY_PACK.get(), BatterySubtypeInterpreter.INSTANCE);
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        List<ItemStack> extra = new ArrayList<>();

        FluidType[] types = Fluids.getInNiceOrder();
        for(int i = 1; i < types.length; ++i) {
            FluidType type = types[i];

            extra.add(FluidIconItem.make(type, 1000));
        }

        registration.addExtraItemStacks(extra);
    }
}
