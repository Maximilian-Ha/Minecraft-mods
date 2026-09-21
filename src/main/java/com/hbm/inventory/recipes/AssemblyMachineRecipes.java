package com.hbm.inventory.recipes;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.CastPlateItem;
import com.hbm.items.machine.DriveItem.DriveType;
import com.hbm.items.machine.PileRodItem.EnumPileRod;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.RecipesCommon.TagStack;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.DrillbitItem;
import com.hbm.items.machine.PistonsItem.PistonType;
import com.hbm.items.machine.PACoilItem.EnumCoilType;
import com.hbm.items.BoltItem;
import com.hbm.items.WireDenseItem;
import com.hbm.items.PartGenericItem;
import com.hbm.items.special.SatelliteItem.SatType;
import com.hbm.items.machine.BatteryPackItem;
import com.hbm.items.machine.BatteryPackItem.BatteryPackType;
import com.hbm.items.machine.FluidIconItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class AssemblyMachineRecipes extends GenericRecipes<GenericRecipe> {

    public static final AssemblyMachineRecipes INSTANCE = new AssemblyMachineRecipes();

    @Override public int inputItemLimit() { return 12; }
    @Override public int inputFluidLimit() { return 1; }
    @Override public int outputItemLimit() { return 1; }
    @Override public int outputFluidLimit() { return 1; }

    @Override public String getFileName() { return "hbmAssemblyMachine.json"; }
    @Override public GenericRecipe instantiateRecipe(String name) { return new GenericRecipe(name); }

    @Override
    public void registerDefaults() {
        // Originalrezept aus 1.7.10 ("ass.compressor"); am Werktisch gibt es keines.
        // Originalrezept aus 1.7.10 ("ass.compactcompressor")
        // ---- Runde 6 ----
        // ---- Runde 8 ----
        // Original "ass.acidizer". EnumCircuitType.BASIC ist "Integrated Circuit Board"
        // (en_US.lang:2432), nicht die Vakuumroehre -- der Port bildet BASIC ueberall auf
        // CIRCUIT_INTEGRATED_BOARD ab. Die inputItemsEx-Variante entfaellt wie ueberall.
        this.register(new GenericRecipe("ass.acidizer").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_CRYSTALLIZER, 1))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.STEEL, 2),
                        new ComparableStack(NtmItems.SHELL_TITANIUM.get(), 3),
                        new ComparableStack(NtmItems.INGOT_DESH.get(), 4),
                        new ComparableStack(NtmItems.MOTOR.get(), 1),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 2)
                ));

        /*
         * Original "ass.centrifugetower": vier Durastahlplatten, vier Titanplatten, ein Motor.
         * Das Element hat bis Runde 172 gefehlt, und mit ihm dieses Rezept.
         */
        this.register(new GenericRecipe("ass.centrifugetower").setup(100, 100).outputItems(new ItemStack(NtmItems.CENTRIFUGE_ELEMENT.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_DURA_STEEL.get(), 4),
                        new ComparableStack(NtmItems.PLATE_TITANIUM.get(), 4),
                        new ComparableStack(NtmItems.MOTOR.get(), 1)
                ));

        // ---- Runde 7 ----
        // Original "ass.thermoelement". Die zweite Variante (Golddraht + Siliziumbillet)
        // entfaellt, BILLET_SILICON gibt es im Port nicht.
        // MINGRADE.wireFine() ist WIRE_RED_COPPER -- keine Verengung: im Original ist der
        // Feindraht das Autogen-Item wire_fine, und dessen Mingrade-Variante traegt dort
        // genau den Namen wire_red_copper (ModItems.java:2767).
        this.register(new GenericRecipe("ass.thermoelement").setup(60, 100).outputItems(new ItemStack(NtmItems.THERMO_ELEMENT.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 1),
                        new ComparableStack(NtmItems.WIRE_RED_COPPER.get(), 2),
                        new ComparableStack(NtmItems.POWDER_QUARTZ.get(), 2)
                ));

        // Original "ass.rtgunit". PB.plateCast() -> Gussplatte Blei, CU.plate() -> PLATE_COPPER.
        this.register(new GenericRecipe("ass.rtgunit").setup(100, 100).outputItems(new ItemStack(NtmItems.RTG_UNIT.get(), 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.LEAD, 2),
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 4),
                        new ComparableStack(NtmItems.THERMO_ELEMENT.get(), 2)
                ));

        // Original "ass.rtg". MINGRADE.wireFine() ist WIRE_RED_COPPER (siehe oben),
        // ANY_PLASTIC.ingot() -> INGOT_POLYMER.
        this.register(new GenericRecipe("ass.rtg").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_RTG, 1))
                .inputItems(
                        new ComparableStack(NtmItems.RTG_UNIT.get(), 3),
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 4),
                        new ComparableStack(NtmItems.WIRE_RED_COPPER.get(), 16),
                        new ComparableStack(NtmItems.INGOT_POLYMER.get(), 4)
                ));

        // Original "ass.turbofan", ebenfalls ohne die Expensive-Variante.
        this.register(new GenericRecipe("ass.turbofan").setup(300, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_TURBOFAN, 1))
                .inputItems(
                        new ComparableStack(NtmItems.SHELL_TITANIUM.get(), 8),
                        new ComparableStack(NtmItems.PIPE_DURA_STEEL.get(), 4),
                        new ComparableStack(NtmItems.INGOT_POLYMER.get(), 12),
                        new ComparableStack(NtmItems.TURBINE_TUNGSTEN.get(), 1),
                        new ComparableStack(NtmItems.WIRE_DENSE.get(), 12, WireDenseItem.Type.GOLD.meta),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 3)
                ));

        // Original "ass.combustiongen". Die zweite Variante (inputItemsEx, Expensive Mode)
        // entfaellt, der Port kennt den Expensive Mode nicht.
        this.register(new GenericRecipe("ass.combustiongen").setup(300, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_COMBUSTION_ENGINE, 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 16),
                        new ComparableStack(NtmItems.INGOT_COPPER.get(), 12),
                        new ComparableStack(NtmItems.WIRE_DENSE.get(), 8, WireDenseItem.Type.GOLD.meta),
                        new ComparableStack(NtmItems.CANISTER_EMPTY.get(), 4),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 1)
                ));

        // Original "ass.gasturbine", ebenfalls ohne die Expensive-Variante.
        this.register(new GenericRecipe("ass.gasturbine").setup(400, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_TURBINEGAS, 1))
                .inputItems(
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 10),
                        new ComparableStack(NtmItems.WIRE_DENSE.get(), 12, WireDenseItem.Type.GOLD.meta),
                        new ComparableStack(NtmItems.PIPE_DURA_STEEL.get(), 4),
                        new ComparableStack(NtmItems.PIPE_STEEL.get(), 4),
                        new ComparableStack(NtmItems.TURBINE_TUNGSTEN.get(), 1),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 12),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 3)
                ));

        // Die vier Kolbensaetze, je 200 Ticks / 100 HE pro Tick wie im Original.
        // BIGMT des Originals ist Mats.MAT_SATURN alias Saturnite -> PLATE_SATURNITE;
        // ANY_PLASTIC.ingot() -> INGOT_POLYMER.
        this.register(new GenericRecipe("ass.pistonsetsteel").setup(200, 100).outputItems(MetaHelper.newStack(NtmItems.PISTON_SET.get(), 1, PistonType.STEEL.ordinal()))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 16),
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 4),
                        new ComparableStack(NtmItems.INGOT_TUNGSTEN.get(), 8),
                        new ComparableStack(NtmItems.BOLT.get(), 16, BoltItem.Type.TUNGSTEN.meta)
                ));
        this.register(new GenericRecipe("ass.pistonsetdura").setup(200, 100).outputItems(MetaHelper.newStack(NtmItems.PISTON_SET.get(), 1, PistonType.DURA.ordinal()))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_DURA_STEEL.get(), 24),
                        new ComparableStack(NtmItems.PLATE_TITANIUM.get(), 8),
                        new ComparableStack(NtmItems.INGOT_TUNGSTEN.get(), 8),
                        new ComparableStack(NtmItems.BOLT.get(), 16, BoltItem.Type.DURA_STEEL.meta)
                ));
        this.register(new GenericRecipe("ass.pistonsetdesh").setup(200, 100).outputItems(MetaHelper.newStack(NtmItems.PISTON_SET.get(), 1, PistonType.DESH.ordinal()))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_DESH.get(), 24),
                        new ComparableStack(NtmItems.INGOT_POLYMER.get(), 12),
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 24),
                        new ComparableStack(NtmItems.INGOT_TUNGSTEN.get(), 16),
                        new ComparableStack(NtmItems.PIPE_DURA_STEEL.get(), 4)
                ));
        this.register(new GenericRecipe("ass.pistonsetstar").setup(200, 100).outputItems(MetaHelper.newStack(NtmItems.PISTON_SET.get(), 1, PistonType.STARMETAL.ordinal()))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_STARMETAL.get(), 24),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 16),
                        new ComparableStack(NtmItems.PLATE_SATURNITE.get(), 24),
                        new ComparableStack(NtmItems.INGOT_NIOBIUM.get(), 16),
                        new ComparableStack(NtmItems.PIPE_DURA_STEEL.get(), 4)
                ));

        this.register(new GenericRecipe("ass.compactcompressor").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_COMPRESSOR_COMPACT, 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 8),
                        new ComparableStack(NtmItems.SHELL_TITANIUM.get(), 4),
                        new ComparableStack(NtmItems.PIPE_COPPER.get(), 4),
                        new ComparableStack(NtmItems.MOTOR.get(), 2),
                        new ComparableStack(NtmItems.CIRCUIT_PRINTED_BOARD.get(), 4)
                ));

        /*
         * Original "ass.teleporter", Runde 111. ABWEICHUNG: statt des Verschraenkungsbausatzes,
         * den der Port nicht hat, steht hier ein Quantenrechner -- das teuerste Bauteil
         * derselben Art, das es gibt. Der feine Golddraht des Originals fehlt ebenfalls; hier
         * steht gewoehnlicher.
         */
        this.register(new GenericRecipe("ass.teleporter").setup(100, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_TELEPORTER, 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_TITANIUM.get(), 12),
                        new ComparableStack(NtmItems.PLATE_DURA_STEEL.get(), 12),
                        new ComparableStack(NtmItems.WIRE_GOLD.get(), 32),
                        new ComparableStack(NtmItems.CIRCUIT_QUANTUM_COMPUTER.get(), 1),
                        new ComparableStack(NtmItems.BATTERY_PACK.get(), 1, BatteryPackItem.BatteryPackType.BATTERY_LITHIUM.ordinal())
                ));

        // Original "ass.hephaestus", Runde 110
        this.register(new GenericRecipe("ass.hephaestus").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_HEPHAESTUS, 1))
                .inputItems(
                        new ComparableStack(NtmItems.PIPE_STEEL.get(), 12),
                        new ComparableStack(NtmItems.INGOT_STEEL.get(), 24),
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 24),
                        new ComparableStack(NtmItems.INGOT_NIOBIUM.get(), 4),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 12),
                        new ComparableStack(NtmBlocks.GLASS_QUARTZ.get(), 16)
                ));

        /*
         * Original "ass.assemfac", Runde 114. Dieselben Abweichungen wie bei der Chemiefabrik:
         * ANY_RESISTANTALLOY ist im Port derselbe Gegenstand wie DURA, die sechzehn und die
         * acht stehen deshalb als vierundzwanzig zusammen.
         */
        this.register(new GenericRecipe("ass.assemfac").setup(400, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_ASSEMBLY_FACTORY, 1))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_DURA_STEEL.get(), 24),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 16),
                        new ComparableStack(NtmItems.INGOT_BORON.get(), 8),
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 4),
                        new ComparableStack(NtmItems.MOTOR.get(), 12),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 16)
                ));

        /*
         * Original "ass.chemfac", Runde 113. ABWEICHUNGEN nach dem bekannten Muster:
         * ANY_RESISTANTALLOY wird Schnellarbeitsstahl -- derselbe Gegenstand wie DURA, deshalb
         * stehen die sechzehn und die acht hier als vierundzwanzig zusammen. Den Desh-Motor gibt
         * es im Port nicht; an seiner Stelle steht der gewoehnliche.
         */
        this.register(new GenericRecipe("ass.chemfac").setup(400, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_CHEMICAL_FACTORY, 1))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_DURA_STEEL.get(), 24),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 16),
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 12),
                        new ComparableStack(NtmItems.PIPE_COPPER.get(), 8),
                        new ComparableStack(NtmItems.MOTOR.get(), 4),
                        new ComparableStack(NtmItems.COIL_TUNGSTEN.get(), 16),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 16)
                ));

        // Original "ass.strandcaster", Runde 109
        this.register(new GenericRecipe("ass.strandcaster").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_STRAND_CASTER, 1))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_FIREBRICK.get(), 16),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 6),
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.COPPER, 2),
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 2),
                        new ComparableStack(NtmBlocks.CONCRETE_SMOOTH.get(), 8)
                ));

        // Original "ass.iturbine", Runde 108
        this.register(new GenericRecipe("ass.iturbine").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_INDUSTRIAL_TURBINE, 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 16),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 4),
                        new ComparableStack(NtmItems.TURBINE_TITANIUM.get(), 2),
                        new ComparableStack(NtmItems.WIRE_DENSE.get(), 4, WireDenseItem.Type.GOLD.meta),
                        new ComparableStack(NtmItems.PIPE_DURA_STEEL.get(), 4),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 2)
                ));

        // Original "ass.epress", Runde 107
        this.register(new GenericRecipe("ass.epress").setup(100, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_EPRESS, 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 8),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 4),
                        new ComparableStack(NtmItems.PART_GENERIC.get(), 2, PartGenericItem.Type.PISTON_HYDRAULIC),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 1)
                ));

        /*
         * Die beiden Radare, Runde 120. Das kleine gab es im Port seit langem, aber ohne Rezept
         * -- es war nur mit Befehlen zu bekommen; jetzt stehen beide hier.
         *
         * ABWEICHUNGEN: ANY_RESISTANTALLOY ist im Port derselbe Gegenstand wie DURA. Den
         * Desh-Motor gibt es nicht; an seiner Stelle steht der gewoehnliche. Der fortgeschrittene
         * Schaltkreis wird der integrierte und der einfache die Leiterplatte, wie in allen Runden
         * davor. Geschweisstes Stahlblech hat der Port nicht; hier steht Stahlblech.
         */
        this.register(new GenericRecipe("ass.radar").setup(300, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_RADAR, 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 12),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 12),
                        new ComparableStack(NtmItems.MAGNETRON.get(), 5),
                        new ComparableStack(NtmItems.MOTOR.get(), 1),
                        new ComparableStack(NtmItems.CIRCUIT_PRINTED_BOARD.get(), 8),
                        new ComparableStack(NtmItems.CRT_DISPLAY.get(), 4)
                ));

        this.register(new GenericRecipe("ass.radarlarge").setup(400, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_RADAR_LARGE, 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 6),
                        new ComparableStack(NtmItems.INGOT_DURA_STEEL.get(), 4),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 24),
                        new ComparableStack(NtmItems.MAGNETRON.get(), 16),
                        new ComparableStack(NtmItems.MOTOR.get(), 1),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 4),
                        new ComparableStack(NtmItems.CRT_DISPLAY.get(), 4)
                ));

        /*
         * Original "ass.mininglaser", Runde 118. ABWEICHUNG nur in der Form: das Original nennt
         * Stahlblech, Titanhuelsen, Duraplatten und Kunststoff ueber das Erzwoerterbuch.
         */
        this.register(new GenericRecipe("ass.mininglaser").setup(400, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_MINING_LASER, 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 16),
                        new ComparableStack(NtmItems.SHELL_TITANIUM.get(), 4),
                        new ComparableStack(NtmItems.PLATE_DURA_STEEL.get(), 4),
                        new ComparableStack(NtmItems.CRYSTAL_REDSTONE.get(), 3),
                        new ComparableStack(Items.DIAMOND, 3),
                        new ComparableStack(NtmItems.INGOT_POLYMER.get(), 8),
                        new ComparableStack(NtmItems.MOTOR.get(), 3)
                ));

        /*
         * Original "ass.cyclotron", Runde 116. ABWEICHUNGEN nur in der Form: das Original nennt
         * Neodymdraht, Stahlgussplatten, Kunststoff und Kautschuk ueber das Erzwoerterbuch, der
         * Port ueber die Gegenstaende selbst. Der einfache Schaltkreis ist die Leiterplatte, wie
         * in allen Runden davor.
         */
        this.register(new GenericRecipe("ass.cyclotron").setup(600, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_CYCLOTRON, 1))
                .inputItems(
                        new ComparableStack(NtmItems.BATTERY_PACK.get(), 1, BatteryPackItem.BatteryPackType.BATTERY_LITHIUM.ordinal()),
                        new ComparableStack(NtmItems.WIRE_DENSE.get(), 32, WireDenseItem.Type.NEODYMIUM.meta),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 16),
                        new ComparableStack(NtmItems.INGOT_POLYMER.get(), 24),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 24),
                        new ComparableStack(NtmItems.CIRCUIT_PRINTED_BOARD.get(), 16)
                ));

        /*
         * Die fuenf Geschosse des Zyklotrons, Runde 116. Unveraendert aus dem Original: je acht
         * Stueck aus einem Pulver. ABWEICHUNG nur in der Form -- das Original nennt die Pulver
         * ueber das Erzwoerterbuch, den Port hat keines.
         */
        this.register(new GenericRecipe("ass.partlith").setup(40, 100).outputItems(new ItemStack(NtmItems.PART_LITHIUM.get(), 8))
                .inputItems(new ComparableStack(NtmItems.POWDER_LITHIUM.get(), 1)));
        this.register(new GenericRecipe("ass.partberyl").setup(40, 100).outputItems(new ItemStack(NtmItems.PART_BERYLLIUM.get(), 8))
                .inputItems(new ComparableStack(NtmItems.POWDER_BERYLLIUM.get(), 1)));
        this.register(new GenericRecipe("ass.partcoal").setup(40, 100).outputItems(new ItemStack(NtmItems.PART_CARBON.get(), 8))
                .inputItems(new ComparableStack(NtmItems.POWDER_COAL.get(), 1)));
        this.register(new GenericRecipe("ass.partcop").setup(40, 100).outputItems(new ItemStack(NtmItems.PART_COPPER.get(), 8))
                .inputItems(new ComparableStack(NtmItems.POWDER_COPPER.get(), 1)));
        this.register(new GenericRecipe("ass.partplut").setup(40, 100).outputItems(new ItemStack(NtmItems.PART_PLUTONIUM.get(), 8))
                .inputItems(new ComparableStack(NtmItems.POWDER_PLUTONIUM.get(), 1)));

        /*
         * Die SILEX, Runde 286. Original: ass.silex, 400 Ticks. Sechzehn Quarzglas, acht
         * Stahlgussplatten, vier Desh, acht Kautschuk, acht Stahlrohre.
         *
         * ABWEICHUNG: das Original nennt Platten, Barren und Rohre ueber das Erzwoerterbuch;
         * der Port nennt die Gegenstaende selbst. Der zweite Bauplan des Originals -- derselbe
         * Ausgang aus Ferroplatten statt Stahl -- faellt weg, wie bei allen Maschinen davor.
         */
        this.register(new GenericRecipe("ass.silex").setup(400, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_SILEX, 1))
                .inputItems(
                        new ComparableStack(NtmBlocks.GLASS_QUARTZ.get(), 16),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 8),
                        new ComparableStack(NtmItems.INGOT_DESH.get(), 4),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 8),
                        new ComparableStack(NtmItems.PIPE_STEEL.get(), 8)
                ));

        /*
         * Original "ass.gascent", Runde 115. ABWEICHUNGEN: das Zentrifugenelement gibt es im
         * Port nicht. Statt vier Stueck davon stehen hier seine Bestandteile ausgeschrieben --
         * das Original baut eines aus vier Duraplatten, vier Titanplatten und einem Motor, also
         * viermal das. Duraplatten hat der Port nicht; an ihrer Stelle steht Stahlblech, das
         * ohnehin schon im Rezept vorkommt, weshalb die acht des Originals hier als
         * vierundzwanzig zusammenstehen. Der fortgeschrittene Schaltkreis wird der integrierte,
         * wie in allen Runden davor.
         */
        this.register(new GenericRecipe("ass.gascent").setup(400, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_GAS_CENT, 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 24),
                        new ComparableStack(NtmItems.PLATE_TITANIUM.get(), 16),
                        new ComparableStack(NtmItems.MOTOR.get(), 4),
                        new ComparableStack(NtmItems.INGOT_POLYMER.get(), 8),
                        new ComparableStack(NtmItems.INGOT_DESH.get(), 2),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 1)
                ));

        this.register(new GenericRecipe("ass.compressor").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_COMPRESSOR, 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 8),
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 4),
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 2),
                        new ComparableStack(NtmItems.MOTOR.get(), 3),
                        new ComparableStack(NtmItems.CIRCUIT_ANALOG_BOARD.get(), 1)
                ));

        // Originalrezept aus 1.7.10 (AssemblyMachineRecipes, "ass.dieselgen")
        this.register(new GenericRecipe("ass.dieselgen").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_DIESEL, 1))
                .inputItems(
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 1),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.COPPER, 2),
                        new ComparableStack(NtmItems.COIL_COPPER.get(), 4)
                ));

        this.register(new GenericRecipe("ass.boytarget").setup(200, 100).outputItems(new ItemStack(NtmItems.LITTLE_BOY_TARGET.get(), 1))
                        .inputItems(new ComparableStack(NtmItems.INGOT_URANIUM.get(), 18)));

        this.register(new GenericRecipe("ass.machine_refinery").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_REFINERY, 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 8),
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 4),
                        new ComparableStack(NtmItems.PIPE_STEEL.get(), 12),
                        new ComparableStack(NtmItems.INSULATOR.get(), 8),
                        new ComparableStack(NtmItems.CIRCUIT_ANALOG_BOARD.get(), 3),
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.STEEL, 3)

                        ));

        this.register(new GenericRecipe("ass.drill_titanium").setup(200, 100).outputItems(new ItemStack(NtmItems.DRILL_TITANIUM.get(), 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.DURA_STEEL, 1),
                        new ComparableStack(NtmItems.PLATE_TITANIUM.get(), 8)
                )
        );

        this.register(new GenericRecipe("ass.machine_oil_derrick").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_WELL, 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.COPPER, 1),
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 8),
                        new ComparableStack(NtmItems.PIPE_STEEL.get(), 4),
                        new ComparableStack(NtmItems.DRILL_TITANIUM.get(), 1),
                        new ComparableStack(NtmItems.MOTOR.get(), 1)

                )
        );

        this.register(new GenericRecipe("ass.missileassembly").setup(200, 100).outputItems(new ItemStack(NtmItems.MISSILE_ASSEMBLY.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.SHELL_ALUMINIUM.get(), 2),
                        new ComparableStack(NtmItems.SHELL_TITANIUM.get(), 4),
                        new RecipesCommon.TagStack(ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "bars_hard_plastic")), 8),
                        new ComparableStack(NtmItems.ROCKET_FUEL.get(), 8),
                        new ComparableStack(NtmItems.CIRCUIT_PRINTED_BOARD.get(), 1)
                )
        );

        this.register(new GenericRecipe("ass.warheadhe1").setup(100, 100).outputItems(new ItemStack(NtmItems.WARHEAD_GENERIC_SMALL.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_TITANIUM.get(), 4),
                        new ComparableStack(NtmItems.BALL_DYNAMITE.get(), 2),
                        new ComparableStack(NtmItems.CIRCUIT_MICROCHIP.get(), 1)
                )
        );

        this.register(new GenericRecipe("ass.warheadhe2").setup(200, 100).outputItems(new ItemStack(NtmItems.WARHEAD_GENERIC_MEDIUM.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_TITANIUM.get(), 8),
                        new RecipesCommon.TagStack(ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "balls_he")), 4),
                        new ComparableStack(NtmItems.CIRCUIT_PRINTED_BOARD.get(), 1)
                )
        );

        this.register(new GenericRecipe("ass.warheadhe3").setup(400, 100).outputItems(new ItemStack(NtmItems.WARHEAD_GENERIC_LARGE.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_TITANIUM.get(), 16),
                        new RecipesCommon.TagStack(ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "balls_he")), 8),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 1)
                )
        );

        this.register(new GenericRecipe("ass.warheadinc1").setup(100, 100).outputItems(new ItemStack(NtmItems.WARHEAD_INCENDIARY_SMALL.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.WARHEAD_GENERIC_SMALL.get(), 1),
                        new ComparableStack(NtmItems.POWDER_FIRE.get(), 2)
                )
        );

        this.register(new GenericRecipe("ass.warheadinc2").setup(200, 100).outputItems(new ItemStack(NtmItems.WARHEAD_INCENDIARY_MEDIUM.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.WARHEAD_GENERIC_MEDIUM.get(), 1),
                        new ComparableStack(NtmItems.POWDER_FIRE.get(), 4)
                )
        );

        this.register(new GenericRecipe("ass.warheadinc3").setup(400, 100).outputItems(new ItemStack(NtmItems.WARHEAD_INCENDIARY_LARGE.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.WARHEAD_GENERIC_LARGE.get(), 1),
                        new ComparableStack(NtmItems.POWDER_FIRE.get(), 8)
                )
        );

        this.register(new GenericRecipe("ass.warheadcl1").setup(100, 100).outputItems(new ItemStack(NtmItems.WARHEAD_CLUSTER_SMALL.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.WARHEAD_GENERIC_SMALL.get(), 1),
                        new ComparableStack(NtmItems.PELLET_CLUSTER.get(), 2)
                )
        );

        this.register(new GenericRecipe("ass.warheadcl2").setup(200, 100).outputItems(new ItemStack(NtmItems.WARHEAD_CLUSTER_MEDIUM.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.WARHEAD_GENERIC_MEDIUM.get(), 1),
                        new ComparableStack(NtmItems.PELLET_CLUSTER.get(), 4)
                )
        );

        this.register(new GenericRecipe("ass.warheadcl3").setup(400, 100).outputItems(new ItemStack(NtmItems.WARHEAD_CLUSTER_LARGE.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.WARHEAD_GENERIC_LARGE.get(), 1),
                        new ComparableStack(NtmItems.PELLET_CLUSTER.get(), 8)
                )
        );

        this.register(new GenericRecipe("ass.warheadbb1").setup(100, 100).outputItems(new ItemStack(NtmItems.WARHEAD_BUSTER_SMALL.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.WARHEAD_GENERIC_SMALL.get(), 1),
                        new RecipesCommon.TagStack(ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "balls_he")), 2)
                )
        );

        this.register(new GenericRecipe("ass.warheadbb2").setup(200, 100).outputItems(new ItemStack(NtmItems.WARHEAD_BUSTER_MEDIUM.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.WARHEAD_GENERIC_MEDIUM.get(), 1),
                        new RecipesCommon.TagStack(ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "balls_he")), 4)
                )
        );

        this.register(new GenericRecipe("ass.warheadbb3").setup(400, 100).outputItems(new ItemStack(NtmItems.WARHEAD_BUSTER_LARGE.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.WARHEAD_GENERIC_LARGE.get(), 1),
                        new RecipesCommon.TagStack(ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "balls_he")), 8)
                )
        );

        this.register(new GenericRecipe("ass.warheadnuke").setup(400, 100).outputItems(new ItemStack(NtmItems.WARHEAD_NUCLEAR.get(), 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.TITANIUM, 12),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.LEAD, 6),
                        new ComparableStack(NtmItems.BILLET_U235.get(), 6),
                        new ComparableStack(NtmItems.CORDITE.get(), 12),
                        new ComparableStack(NtmItems.CIRCUIT_CONTROL_UNIT.get(), 1)
                )
        );

        this.register(new GenericRecipe("ass.warheadthermonuke").setup(600, 100).outputItems(new ItemStack(NtmItems.WARHEAD_MIRV.get(), 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.TITANIUM, 12),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.LEAD, 6),
                        new ComparableStack(NtmItems.BILLET_PU239.get(), 8),
                        new ComparableStack(NtmItems.BALL_TATB.get(), 12),
                        new ComparableStack(NtmItems.CIRCUIT_ADVANCED_CONTROL_UNIT.get(), 2)
                ).inputFluids(new FluidStack(Fluids.DEUTERIUM, 4_000))
        );

        this.register(new GenericRecipe("ass.warheadvolcano").setup(600, 100).outputItems(new ItemStack(NtmItems.WARHEAD_VOLCANO.get(), 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.TITANIUM, 12),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 6),
                        new ComparableStack(NtmBlocks.DET_NUKE.get(), 3),
                        new ComparableStack(NtmBlocks.BLOCK_U238.get(), 24),
                        new ComparableStack(NtmItems.CIRCUIT_CAPACITOR_BOARD.get(), 5)
                )
        );

        this.register(new GenericRecipe("ass.plate_desh").setup(200, 100).outputItems(new ItemStack(NtmItems.PLATE_DESH.get(), 4))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_DESH.get(), 4),
                        new ComparableStack(NtmItems.INGOT_DURA_STEEL.get(), 1),
                        new RecipesCommon.TagStack(ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "powders_plastic")), 2)

                )
        );

        this.register(new GenericRecipe("ass.plate_bismuth").setup(200, 100).outputItems(new ItemStack(NtmItems.PLATE_BISMUTH.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.NUGGET_BISMUTH.get(), 2),
                        new ComparableStack(NtmItems.BILLET_U238.get(), 2),
                        new ComparableStack(NtmItems.POWDER_NIOBIUM.get(), 1)

                )
        );

        /*
         * Runde 133: der Teilchenbeschleuniger. Alles ausser der Strahlfuehrung braucht sie
         * selbst als Baustein -- wer den Ring will, baut erst Rohre und dann alles andere
         * daraus. Das ist die Reihenfolge des Originals und der Grund, warum das Rohr mit
         * zweihundert Ticks das billigste Stueck ist.
         *
         * ABWEICHUNGEN nur in der Form: das Original nennt Stahlgussplatten, Kupferplatten,
         * Golddraht und Hartkunststoff ueber das Erzwoerterbuch, der Port ueber die Gegenstaende
         * selbst. Der "bismoide" Schaltkreis ist die Vielzweckplatine, der Quantenschaltkreis
         * die Quantenrecheneinheit -- wie in allen Runden davor.
         *
         * NICHT UEBERNOMMEN: die zweiten Zutatenlisten (inputItemsEx) des Originals. Sie nennen
         * durchweg die Gegenstandsfamilie "item_expensive" des Weltraumbaus, die es im Port
         * nicht gibt.
         */
        this.register(new GenericRecipe("ass.beamline").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_PA_BEAMLINE, 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 8),
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 16),
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 4, WireDenseItem.Type.GOLD.meta))
                )
                .setPools528(GenericRecipes.POOL_PREFIX_528 + "chip_quantum"));

        this.register(new GenericRecipe("ass.rfc").setup(400, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_PA_RFC, 1))
                .inputItems(
                        new ComparableStack(NtmBlocks.MACHINE_PA_BEAMLINE.get(), 3),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 16),
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 64),
                        new ComparableStack(NtmItems.INGOT_PC.get(), 16),
                        new ComparableStack(NtmItems.MAGNETRON.get(), 16)
                ));

        this.register(new GenericRecipe("ass.quadrupole").setup(400, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_PA_QUADRUPOLE, 1))
                .inputItems(
                        new ComparableStack(NtmBlocks.MACHINE_PA_BEAMLINE.get(), 1),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 16),
                        new ComparableStack(NtmItems.INGOT_PC.get(), 16),
                        new ComparableStack(NtmItems.CIRCUIT_VERSATILE_BOARD.get(), 1)
                ));

        this.register(new GenericRecipe("ass.dipole").setup(400, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_PA_DIPOLE, 1))
                .inputItems(
                        new ComparableStack(NtmBlocks.MACHINE_PA_BEAMLINE.get(), 2),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 16),
                        new ComparableStack(NtmItems.INGOT_PC.get(), 32),
                        new ComparableStack(NtmItems.CIRCUIT_VERSATILE_BOARD.get(), 4)
                ));

        this.register(new GenericRecipe("ass.source").setup(400, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_PA_SOURCE, 1))
                .inputItems(
                        new ComparableStack(NtmBlocks.MACHINE_PA_BEAMLINE.get(), 3),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 16),
                        new ComparableStack(NtmItems.INGOT_PC.get(), 16),
                        new ComparableStack(NtmItems.MAGNETRON.get(), 16),
                        new ComparableStack(NtmItems.CIRCUIT_QUANTUM_PROCESSING_UNIT.get(), 1)
                ));

        this.register(new GenericRecipe("ass.detector").setup(400, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_PA_DETECTOR, 1))
                .inputItems(
                        new ComparableStack(NtmBlocks.MACHINE_PA_BEAMLINE.get(), 3),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 24),
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 16, WireDenseItem.Type.GOLD.meta)),
                        new ComparableStack(NtmItems.INGOT_PC.get(), 16),
                        new ComparableStack(NtmItems.CIRCUIT_QUANTUM_PROCESSING_UNIT.get(), 4)
                ));

        /*
         * Die vier Spulen. Jede kostet zwei volle Stapel Draht -- und die beiden Stapel sind
         * der Grund, warum eine bessere Spule kein Nebenbei ist.
         */
        this.register(new GenericRecipe("ass.pagold").setup(400, 100)
                .outputItems(MetaHelper.newStack(NtmItems.PA_COIL.get(), 1, EnumCoilType.GOLD.ordinal()))
                .inputItems(
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 64, WireDenseItem.Type.GOLD.meta)),
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 64, WireDenseItem.Type.GOLD.meta))
                ));

        this.register(new GenericRecipe("ass.panbti").setup(400, 100)
                .outputItems(MetaHelper.newStack(NtmItems.PA_COIL.get(), 1, EnumCoilType.NIOBIUM.ordinal()))
                .inputItems(
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 64, WireDenseItem.Type.NIOBIUM.meta)),
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 64, WireDenseItem.Type.TITANIUM.meta))
                ));

        this.register(new GenericRecipe("ass.pabscco").setup(400, 100)
                .outputItems(MetaHelper.newStack(NtmItems.PA_COIL.get(), 1, EnumCoilType.BSCCO.ordinal()))
                .inputItems(
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 64, WireDenseItem.Type.BSCCO.meta)),
                        new ComparableStack(NtmItems.INGOT_POLYMER.get(), 64)
                ));

        /*
         * Original "ass.exposurechamber". ABWEICHUNGEN nur in der Form: Aluminiumgussplatten,
         * Hartkunststoff und Golddraht kommen aus dem Erzwoerterbuch, hier aus den Gegenstaenden
         * selbst; "widerstandsfaehige Legierung" ist im Port der Dura-Stahl, der Deshmotor der
         * Motor, der "bismoide" Schaltkreis die Vielzweckplatine.
         *
         * NICHT UEBERNOMMEN: die zweite Zutatenliste (inputItemsEx) -- sie nennt die
         * Gegenstandsfamilie item_expensive des Weltraumbaus.
         */
        this.register(new GenericRecipe("ass.exposurechamber").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_EXPOSURE_CHAMBER, 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.ALUMINIUM, 12),
                        new ComparableStack(NtmItems.INGOT_DURA_STEEL.get(), 4),
                        new ComparableStack(NtmItems.INGOT_PC.get(), 12),
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 32, WireDenseItem.Type.GOLD.meta)),
                        new ComparableStack(NtmItems.MOTOR.get(), 2),
                        new ComparableStack(NtmItems.CIRCUIT_VERSATILE_BOARD.get(), 4),
                        new ComparableStack(NtmItems.BATTERY_PACK.get(), 1, BatteryPackType.CAPACITOR_TANTALUM.ordinal()),
                        new ComparableStack(NtmBlocks.GLASS_QUARTZ.get().asItem(), 16)
                )
                .setPools528(GenericRecipes.POOL_PREFIX_528 + "chip_quantum"));

        this.register(new GenericRecipe("ass.pachlorophyte").setup(400, 100)
                .outputItems(MetaHelper.newStack(NtmItems.PA_COIL.get(), 1, EnumCoilType.CHLOROPHYTE.ordinal()))
                .inputItems(
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 64, WireDenseItem.Type.COPPER.meta)),
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 64, WireDenseItem.Type.COPPER.meta)),
                        new ComparableStack(NtmItems.POWDER_CHLOROPHYTE.get(), 16)
                ));

        /*
         * Runde 135: der Reaktorkern und der Radiothermalgenerator. Beide unveraendert aus dem
         * Original uebernommen; ABWEICHUNGEN nur in der Form, weil der Port kein Erzwoerterbuch
         * hat. Der "einfache" Schaltkreis ist die Integrierte Platine, wie in den Runden davor.
         *
         * ABWEICHUNG: das Original verlangt einen roten Farbstoff aus dem Erzwoerterbuch; hier
         * steht der rote Farbstoff von Minecraft selbst.
         *
         * NICHT UEBERNOMMEN: die zweite Zutatenliste (inputItemsEx) -- sie nennt die
         * Gegenstandsfamilie item_expensive des Weltraumbaus.
         */
        this.register(new GenericRecipe("ass.reactorcore").setup(100, 100).outputItems(new ItemStack(NtmItems.REACTOR_CORE.get(), 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.LEAD, 4),
                        new ComparableStack(NtmItems.INGOT_BERYLLIUM.get(), 8),
                        new ComparableStack(NtmItems.PLATE_DURA_STEEL.get(), 8),
                        new ComparableStack(NtmItems.INGOT_ASBESTOS.get(), 4)
                ));

        this.register(new GenericRecipe("ass.radgen").setup(400, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_RAD_GEN, 1))
                .setPools(GenericRecipes.POOL_PREFIX_DISCOVER + "radgen")
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_STEEL.get(), 8),
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 32),
                        new ComparableStack(NtmItems.COIL_MAGNETIZED_TUNGSTEN.get(), 6),
                        new ComparableStack(NtmItems.WIRE_MAGNETIZED_TUNGSTEN.get(), 24),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 16),
                        new ComparableStack(NtmItems.REACTOR_CORE.get(), 3),
                        new ComparableStack(NtmItems.INGOT_STARMETAL.get(), 1),
                        new ComparableStack(net.minecraft.world.item.Items.RED_DYE, 1)
                ));

        this.register(new GenericRecipe("ass.magnetron").setup(40, 100).outputItems(new ItemStack(NtmItems.MAGNETRON.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.WIRE_TUNGSTEN.get(), 4),
                        new ComparableStack(NtmItems.POWDER_NIOBIUM.get(), 3)

                )
        );

        this.register(new GenericRecipe("ass.buckshot").setup(50, 100).outputItems(new ItemStack(NtmItems.PELLET_BUCKSHOT.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.NUGGET_LEAD.get(), 6)
                )
        );

        this.register(new GenericRecipe("ass.pellet_cluster").setup(40, 100).outputItems(new ItemStack(NtmItems.PELLET_CLUSTER.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 4),
                        new RecipesCommon.TagStack(ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "balls_he")), 1)

                )
        );

        this.register(new GenericRecipe("ass.plate_dalek").setup(40, 100).outputItems(new ItemStack(NtmItems.PLATE_DALEKANIUM.get(), 1))
                .inputItems(
                        new ComparableStack(NtmBlocks.BLOCK_METEOR.get(), 1)

                )
        );

        this.register(new GenericRecipe("ass.man").setup(200, 100).outputItems(new ItemStack(NtmBlocks.NUKE_FAT_MAN.get(), 1))
                .inputItems(new ComparableStack(NtmItems.PELLET_ANTIMATTER.get(), 1)));

        // ---- Runde 10 ----
        // Original "ass.filtercoal". COAL.dust() -> POWDER_COAL.
        this.register(new GenericRecipe("ass.filtercoal").setup(50, 100).outputItems(new ItemStack(NtmItems.FILTER_COAL.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.POWDER_COAL.get(), 4),
                        new ComparableStack(Items.STRING, 2),
                        new ComparableStack(Items.PAPER, 1)
                ));

        // ---- Runde 36 ----
        /*
         * Original "ass.rbmk". Der eigentliche Herstellungsweg fuer die RBMK-Leersaeule; alles
         * andere im RBMK-Zweig baut darauf auf. STEEL.plateCast() ist die Stahlgussplatte,
         * CU.plate() die Kupferplatte, RUBBER.ingot() der Gummibarren.
         */
        this.register(new GenericRecipe("ass.rbmk").setup(100, 100).outputItems(new ItemStack(NtmBlocks.RBMK_BLANK.get(), 1))
                .inputItems(
                        new ComparableStack(NtmBlocks.CONCRETE_ASBESTOS.get(), 4),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 2),
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 4),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 2)
                ));

        /*
         * Runde 93. Original "ass.rbmkautoloader". Der selbsttaetige Lader war bis hierher der
         * einzige RBMK-Block ohne jeden Herstellungsweg -- und damit das Stueck, an dem ein
         * vollautomatischer Reaktor in der Ueberlebensrunde scheiterte.
         *
         * Drei Motoren fuer den Stempel, geschweisste Stahlplatten fuer das Gehaeuse, Blei
         * gegen die Strahlung und Bor fuer die Abschirmung der Stabfaecher.
         */
        this.register(new GenericRecipe("ass.rbmkautoloader").setup(100, 100).outputItems(new ItemStack(NtmBlocks.RBMK_AUTOLOADER.get(), 1))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.STEEL, 4),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.LEAD, 4),
                        new ComparableStack(NtmItems.INGOT_BORON.get(), 4),
                        new ComparableStack(NtmItems.MOTOR.get(), 3)
                ));

        // ---- Runde 60 ----
        /*
         * Der Chicago Pile. Der Graphitziegel ist ein Brett, vier Barren Graphit und zwei
         * Stahlbolzen -- billig, und das muss er auch sein: eine Anlage braucht mindestens
         * hundertfuenfundzwanzig davon.
         */
        this.register(new GenericRecipe("ass.pileblock").setup(20, 250).outputItems(new ItemStack(NtmBlocks.PILE_BRICK.get(), 1))
                .inputItems(
                        new TagStack(ItemTags.PLANKS, 1),
                        new ComparableStack(NtmItems.INGOT_GRAPHITE.get(), 4),
                        new ComparableStack(NtmItems.BOLT.get(), 2, BoltItem.Type.STEEL.meta)
                ));

        /*
         * Die fuenf Staebe, die sich herstellen lassen. Die uebrigen vier Sorten entstehen nur
         * im Reaktor selbst, wenn ein Stab abbrennt.
         */
        String autoPileRod = "autoswitch.pilerod";

        this.register(new GenericRecipe("ass.pilepabe").setup(40, 200).outputItems(MetaHelper.newStack(NtmItems.PILE_ROD, 1, EnumPileRod.RA226BE.ordinal()))
                .inputItems(new ComparableStack(NtmItems.BILLET_RA226BE.get(), 3)).setGroup(autoPileRod, INSTANCE));
        this.register(new GenericRecipe("ass.pilepobe").setup(40, 200).outputItems(MetaHelper.newStack(NtmItems.PILE_ROD, 1, EnumPileRod.PO210BE.ordinal()))
                .inputItems(new ComparableStack(NtmItems.BILLET_PO210BE.get(), 3)).setGroup(autoPileRod, INSTANCE));
        this.register(new GenericRecipe("ass.pilezr").setup(40, 200).outputItems(MetaHelper.newStack(NtmItems.PILE_ROD, 3, EnumPileRod.ZR.ordinal()))
                .inputItems(new TagStack(MaterialShapes.BILLET.getTag(Mats.MAT_ZIRCONIUM), 1)).setGroup(autoPileRod, INSTANCE));
        this.register(new GenericRecipe("ass.pilenu").setup(40, 200).outputItems(MetaHelper.newStack(NtmItems.PILE_ROD, 1, EnumPileRod.NU.ordinal()))
                .inputItems(new TagStack(MaterialShapes.BILLET.getTag(Mats.MAT_URANIUM), 3)).setGroup(autoPileRod, INSTANCE));
        this.register(new GenericRecipe("ass.pilethorium").setup(40, 200).outputItems(MetaHelper.newStack(NtmItems.PILE_ROD, 1, EnumPileRod.THORIUM.ordinal()))
                .inputItems(new TagStack(MaterialShapes.BILLET.getTag(Mats.MAT_THORIUM), 3)).setGroup(autoPileRod, INSTANCE));

        // ---- Runde 59 ----
        /*
         * Die drei Watz-Bauteile. ABWEICHUNGEN gegenueber dem Original, alle nach dem Muster,
         * das schon fuer die RBMK- und Radiolyse-Rezepte gilt:
         *   BIGMT              -> Saturnit (dasselbe Material, anderer Name)
         *   ANY_HARDPLASTIC    -> Polycarbonat, der erste der beiden Vertreter der Gruppe
         *   ANY_RESISTANTALLOY -> Schnellarbeitsstahl, der einzige Vertreter im Port
         * Die teure Variante (inputItemsEx) haengt an item_expensive und entfaellt wie ueberall.
         */
        this.register(new GenericRecipe("ass.watzrod").setup(200, 100).outputItems(new ItemStack(NtmBlocks.WATZ_ELEMENT.get(), 3))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 2),
                        new ComparableStack(NtmItems.INGOT_ZIRCONIUM.get(), 2),
                        new ComparableStack(NtmItems.INGOT_SATURNITE.get(), 2),
                        new ComparableStack(NtmItems.INGOT_PC.get(), 4)
                ));

        this.register(new GenericRecipe("ass.watzcooler").setup(200, 100).outputItems(new ItemStack(NtmBlocks.WATZ_COOLER.get(), 3))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 2),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.COPPER, 4),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 2)
                ));

        this.register(new GenericRecipe("ass.watzcasing").setup(100, 100).outputItems(new ItemStack(NtmBlocks.WATZ_END.get(), 3))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.DURA_STEEL, 1),
                        new ComparableStack(NtmItems.INGOT_BORON.get(), 3),
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.STEEL, 2)
                ));

        // ---- Runde 41 ----
        /*
         * Original "ass.purex". ABWEICHUNG: das Original verlangt einen Desh-Motor. Den gibt es
         * im Port nicht -- hier steht der gewoehnliche Motor, dafuer drei statt einem, damit die
         * Maschine nicht billiger wird als gedacht. Faellt der Desh-Motor nach, gehoert diese
         * Zeile zurueckgedreht.
         */
        this.register(new GenericRecipe("ass.purex").setup(300, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_PUREX.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 4),
                        new ComparableStack(NtmItems.PIPE_RUBBER.get(), 8),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.LEAD, 4),
                        new ComparableStack(NtmItems.MOTOR.get(), 3),
                        new ComparableStack(NtmItems.CIRCUIT_PRINTED_BOARD.get(), 4)
                ));

        // ---- Runde 66 ----
        /*
         * Original "ass.fusionklystron". Dieselben Ersetzungen wie bei den Watz-Bauteilen:
         * ANY_RESISTANTALLOY wird Schnellarbeitsstahl, ANY_HARDPLASTIC wird Polycarbonat, der
         * Bismoid-Schaltkreis wird die Versatile-Platine, und die teure Variante entfaellt.
         */
        this.register(new GenericRecipe("ass.fusioncollector").setup(300, 100).outputItems(new ItemStack(NtmBlocks.FUSION_COLLECTOR.get(), 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.DURA_STEEL, 4),
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 16),
                        new ComparableStack(NtmItems.INGOT_GRAPHITE.get(), 16),
                        new ComparableStack(NtmItems.INGOT_PC.get(), 4)
                ));

        this.register(new GenericRecipe("ass.fusionbreeder").setup(300, 100).outputItems(new ItemStack(NtmBlocks.FUSION_BREEDER.get(), 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.DURA_STEEL, 4),
                        new ComparableStack(NtmItems.PIPE_STEEL.get(), 4),
                        new ComparableStack(NtmItems.INGOT_BORON.get(), 16),
                        new ComparableStack(NtmItems.INGOT_PC.get(), 16)
                ));

        this.register(new GenericRecipe("ass.fusionboiler").setup(300, 100).outputItems(new ItemStack(NtmBlocks.FUSION_BOILER.get(), 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.DURA_STEEL, 16),
                        new ComparableStack(NtmItems.SHELL_COPPER.get(), 16),
                        new ComparableStack(NtmItems.PIPE_STEEL.get(), 8),
                        new ComparableStack(NtmItems.INGOT_PC.get(), 16)
                ));

        this.register(new GenericRecipe("ass.fusioncoupler").setup(300, 100).outputItems(new ItemStack(NtmBlocks.FUSION_COUPLER.get(), 1))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.DURA_STEEL, 4),
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 32),
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 16, WireDenseItem.Type.BSCCO.meta)),
                        new ComparableStack(NtmItems.CIRCUIT_VERSATILE_BOARD.get(), 4)
                ));

        /* Die grosse Turbine braucht eine Minute Bauzeit -- das Vierfache von allem anderen. */
        this.register(new GenericRecipe("ass.fusionmhdt").setup(1_200, 100).outputItems(new ItemStack(NtmBlocks.FUSION_MHDT.get(), 1))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.DURA_STEEL, 16),
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.COPPER, 64),
                        new ComparableStack(NtmItems.INGOT_BISMUTH_BRONZE.get(), 16),
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 64, WireDenseItem.Type.SCHRABIDATE.meta)),
                        new ComparableStack(NtmItems.CIRCUIT_QUANTUM_COMPUTER.get(), 4)
                ));

        this.register(new GenericRecipe("ass.fusionklystron").setup(300, 100).outputItems(new ItemStack(NtmBlocks.FUSION_KLYSTRON.get(), 1))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.TUNGSTEN, 4),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.DURA_STEEL, 16),
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 32),
                        new ComparableStack(NtmItems.INGOT_PC.get(), 16),
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 8, WireDenseItem.Type.BSCCO.meta)),
                        new ComparableStack(NtmItems.CIRCUIT_VERSATILE_BOARD.get(), 2)
                ));

        // ---- Runde 70 ----
        /*
         * Die Plasmaschmiede und die drei Bauteile, aus denen der Torus entsteht. Die
         * Schweissstufe des BSCCO-Bauteils (Stufe 1) kommt nicht aus der Maschine, sondern
         * entsteht mit dem Schweissbrenner am gesetzten Block -- siehe ToolConversionBlock.
         */
        this.register(new GenericRecipe("ass.turretchekhov").setup(200, 100).outputItems(new ItemStack(NtmBlocks.TURRET_CHEKHOV.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_STEEL.get(), 16),
                        new ComparableStack(NtmItems.MOTOR.get(), 3),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 1),
                        new ComparableStack(NtmItems.PIPES_STEEL.get(), 3),
                        new ComparableStack(NtmBlocks.CRATE_IRON.get(), 1),
                        new ComparableStack(NtmItems.CRT_DISPLAY.get(), 1)
                ));
        this.register(new GenericRecipe("ass.turretfriendly").setup(200, 100).outputItems(new ItemStack(NtmBlocks.TURRET_FRIENDLY.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_STEEL.get(), 16),
                        new ComparableStack(NtmItems.MOTOR.get(), 3),
                        new ComparableStack(NtmItems.CIRCUIT_PRINTED_BOARD.get(), 1),
                        new ComparableStack(NtmItems.PIPES_STEEL.get(), 3),
                        new ComparableStack(NtmBlocks.CRATE_IRON.get(), 1),
                        new ComparableStack(NtmItems.CRT_DISPLAY.get(), 1)
                ));

        /* Die beiden grossen Geschuetztuerme. */
        this.register(new GenericRecipe("ass.turretjeremy").setup(200, 100).outputItems(new ItemStack(NtmBlocks.TURRET_JEREMY.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_STEEL.get(), 16),
                        new ComparableStack(NtmItems.MOTOR.get(), 3),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 1),
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 3),
                        new ComparableStack(NtmBlocks.CRATE_STEEL.get(), 1),
                        new ComparableStack(NtmItems.CRT_DISPLAY.get(), 1)
                ));
        this.register(new GenericRecipe("ass.turrethoward").setup(200, 100).outputItems(new ItemStack(NtmBlocks.TURRET_HOWARD.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_STEEL.get(), 24),
                        new ComparableStack(NtmItems.MOTOR.get(), 4),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 3),
                        new ComparableStack(NtmItems.PIPES_STEEL.get(), 10),
                        new ComparableStack(NtmBlocks.CRATE_STEEL.get(), 1),
                        new ComparableStack(NtmItems.CRT_DISPLAY.get(), 1)
                ));

        this.register(new GenericRecipe("ass.fusionplasmaforge").setup(1_200, 100).outputItems(new ItemStack(NtmBlocks.FUSION_PLASMA_FORGE.get(), 1))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.DURA_STEEL, 8),
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 32, WireDenseItem.Type.BSCCO.meta)),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.BISMUTH_BRONZE, 16),
                        new ComparableStack(NtmItems.CIRCUIT_VERSATILE_BOARD.get(), 4)
                ));

        this.register(new GenericRecipe("ass.fusionbscco").setup(100, 100).outputItems(MetaHelper.newStack(NtmBlocks.FUSION_COMPONENT.get(), 2, 0))
                .inputItems(
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 1, WireDenseItem.Type.BSCCO.meta)),
                        new ComparableStack(NtmItems.PIPE_COPPER.get(), 1),
                        new ComparableStack(NtmItems.INGOT_DURA_STEEL.get(), 1),
                        new ComparableStack(NtmItems.INGOT_PC.get(), 4)
                ));

        this.register(new GenericRecipe("ass.fusionblanket").setup(100, 100).outputItems(MetaHelper.newStack(NtmBlocks.FUSION_COMPONENT.get(), 4, 2))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.TUNGSTEN, 1),
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.STEEL, 2),
                        new ComparableStack(NtmItems.INGOT_BERYLLIUM.get(), 4)
                ));

        this.register(new GenericRecipe("ass.fusionpipes").setup(100, 100).outputItems(MetaHelper.newStack(NtmBlocks.FUSION_COMPONENT.get(), 4, 3))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_PC.get(), 4),
                        new ComparableStack(NtmItems.PIPE_COPPER.get(), 2),
                        new ComparableStack(NtmItems.MOTOR.get(), 2),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 1)
                ));

        // ---- Runde 40 ----
        /*
         * Original "ass.radiolysis". ANY_RESISTANTALLOY im Original ist eine Sammelform; im Port
         * steht dafuer der Schnellarbeitsstahl, der einzige Vertreter dieser Gruppe. Die teure
         * Variante (inputItemsEx) haengt an item_expensive, das es hier nicht gibt -- dieselbe
         * Entscheidung wie bei den RBMK-Rezepten aus Runde 36.
         */
        this.register(new GenericRecipe("ass.radiolysis").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_RADIOLYSIS.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 4),
                        new ComparableStack(NtmItems.INGOT_DURA_STEEL.get(), 4),
                        new ComparableStack(NtmItems.PLATE_LEAD.get(), 12),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.COPPER, 4),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 8),
                        new ComparableStack(NtmItems.THERMO_ELEMENT.get(), 8)
                ));

        // ---- Runde 122 ----
        // Original "ass.satlink". EnumCircuitType.CONTROLLER heisst im Original "Control Unit"
        // (en_US.lang) -- im Port ist das CIRCUIT_CONTROL_UNIT. Das Alublech stand dort als
        // OreDictStack(AL.plate()); ein Erzwoerterbuch hat der Port nicht, also der Gegenstand
        // selbst.
        this.register(new GenericRecipe("ass.satlink").setup(100, 1_000).outputItems(new ItemStack(NtmBlocks.MACHINE_SAT_LINK, 1))
                .inputItems(
                        new ComparableStack(NtmBlocks.STEEL_SCAFFOLD.get(), 16),
                        new ComparableStack(NtmItems.PLATE_ALUMINIUM.get(), 16),
                        new ComparableStack(NtmItems.MAGNETRON.get(), 3),
                        new ComparableStack(NtmItems.CIRCUIT_CONTROL_UNIT.get(), 1)
                ));

        // ---- Runde 123 ----
        // Originale "ass.flashdrive" und "ass.diskdrive". ANY_PLASTIC.ingot() -> INGOT_POLYMER,
        // wie in allen Runden davor. EnumCircuitType.CHIP heisst im Original "Microchip", BASIC
        // ist die integrierte Leiterplatte. Der dichte Draht des Originals (ND.wireDense) ist
        // hier der Neodym-Dichtdraht.
        this.register(new GenericRecipe("ass.flashdrive").setup(100, 250).outputItems(MetaHelper.newStack(NtmItems.DRIVE.get(), 1, DriveType.FLASH_EMPTY))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_POLYMER.get(), 2),
                        new ComparableStack(NtmItems.CIRCUIT_MICROCHIP.get(), 1),
                        new ComparableStack(NtmItems.PLATE_GOLD.get(), 1)
                ));
        this.register(new GenericRecipe("ass.diskdrive").setup(200, 250).outputItems(MetaHelper.newStack(NtmItems.DRIVE.get(), 1, DriveType.DISK_EMPTY))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_POLYMER.get(), 8),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 1),
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 1, WireDenseItem.Type.NEODYMIUM.meta))
                ));

        // Original "ass.spacelab". Das Weltraumlabor ist der einzige Satellit, der Daten
        // erzeugt -- ohne ihn haette die Laufwerkskiste nichts zu schreiben.
        this.register(new GenericRecipe("ass.spacelab").setup(1_200, 25_000).outputItems(MetaHelper.newStack(NtmItems.SATELLITE.get(), 1, SatType.SCIENCE))
                .inputItems(
                        new ComparableStack(NtmItems.SHELL_ALUMINIUM.get(), 16),
                        new ComparableStack(NtmItems.PHOTO_PANEL.get(), 32),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 16),
                        new ComparableStack(NtmItems.CIRCUIT_ATOMIC_CLOCK.get(), 4),
                        new ComparableStack(MetaHelper.newStack(NtmItems.PART_GENERIC.get(), 16, PartGenericItem.Type.LDE)),
                        new ComparableStack(NtmItems.CIRCUIT_CONTROL_UNIT.get(), 1)
                ));

        // ---- Runde 129 ----
        // Original "ass.astrominer" und "ass.lunarminer". Ohne die beiden waere die
        // Satellitenstation nur im Schoepfmodus erreichbar -- sie holt ihre Ladung bei einem
        // Schuerfsatelliten ab und bei keinem anderen.
        // ABWEICHUNGEN nach dem bekannten Muster: BIGMT.plateCast() ist die Gussplatte aus
        // Saturnit; motor_bismuth gibt es im Port nicht, dort steht der gewoehnliche Motor;
        // EnumCircuitType.CONTROLLER_ADVANCED heisst hier CIRCUIT_ADVANCED_CONTROL_UNIT.
        // Beide Rezepte sind im Original Zeile fuer Zeile dasselbe, nur die Ausgabe wechselt.
        for(SatType type : new SatType[] { SatType.MINER_ASTRO, SatType.MINER_LUNAR }) {
            String name = type == SatType.MINER_ASTRO ? "ass.astrominer" : "ass.lunarminer";
            this.register(new GenericRecipe(name).setup(1_200, 25_000).outputItems(MetaHelper.newStack(NtmItems.SATELLITE.get(), 1, type))
                    .inputItems(
                            NtmItems.castPlateIngredient(CastPlateItem.Type.SATURNITE, 16),
                            new ComparableStack(NtmItems.PHOTO_PANEL.get(), 8),
                            new ComparableStack(NtmItems.THRUSTER_MEDIUM.get(), 1),
                            new ComparableStack(NtmItems.MOTOR.get(), 4),
                            new ComparableStack(MetaHelper.newStack(NtmItems.PART_GENERIC.get(), 16, PartGenericItem.Type.LDE)),
                            new ComparableStack(NtmItems.CIRCUIT_ADVANCED_CONTROL_UNIT.get(), 2)
                    ));
        }

        // ---- Runde 124 ----
        // Original "ass.sal9000". ABWEICHUNGEN nach dem bekannten Muster: ANY_RESISTANTALLOY ist
        // im Port der Schnellarbeitsstahl, ANY_PLASTIC das Polymer, BASIC die integrierte
        // Leiterplatte. EnumCircuitType.BISMOID heisst im Original "Versatile Circuit Board" --
        // im Port also CIRCUIT_VERSATILE_BOARD. Kupferrohre gibt es nicht, nur Stahlrohre; die
        // stehen an ihrer Stelle. Die inputItemsEx-Fassung entfaellt wie ueberall.
        this.register(new GenericRecipe("ass.sal9000").setup(400, 1_000).outputItems(new ItemStack(NtmBlocks.MACHINE_SUPER_COMPUTER, 1))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_DURA_STEEL.get(), 16),
                        new ComparableStack(NtmItems.INGOT_POLYMER.get(), 64),
                        new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 32, WireDenseItem.Type.GOLD.meta)),
                        new ComparableStack(NtmItems.PIPES_STEEL.get(), 8),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 32),
                        new ComparableStack(NtmItems.CIRCUIT_VERSATILE_BOARD.get(), 1)
                ));

        // ---- Runde 130 ----
        // Original "ass.precass". Die Praezisionsmontage selbst -- ohne sie gaebe es im
        // 528er-Modus keine Schaltkreise mehr. ABWEICHUNG: STEEL.plateCast() ist die Gussplatte
        // aus Stahl, ZR.ingot() der Zirkoniumbarren, CAPACITOR_BOARD die Kondensatorplatte.
        // Die inputItemsEx-Fassung entfaellt wie ueberall.
        this.register(new GenericRecipe("ass.precass").setup(1_200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_PRECASS, 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 8),
                        new ComparableStack(NtmItems.INGOT_ZIRCONIUM.get(), 8),
                        new ComparableStack(NtmItems.MOTOR.get(), 4),
                        new ComparableStack(NtmItems.CIRCUIT_CAPACITOR_BOARD.get(), 4)
                ));

        // ---- Runde 131 ----
        // Original "ass.slopper". Der Erzschlaemmer, die erste Haelfte der Grundgesteinskette.
        // ABWEICHUNG: STEEL.plateCast() ist die Gussplatte aus Stahl; CU.pipe() gibt es im Port
        // nicht, dort stehen Stahlrohre wie schon beim Grossrechner.
        this.register(new GenericRecipe("ass.slopper").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_ORE_SLOPPER, 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 6),
                        new ComparableStack(NtmItems.PLATE_TITANIUM.get(), 8),
                        new ComparableStack(NtmItems.PIPES_STEEL.get(), 3),
                        new ComparableStack(NtmItems.MOTOR.get(), 3),
                        new ComparableStack(NtmItems.CIRCUIT_ANALOG_BOARD.get(), 1)
                ));

        // Original "ass.excavator". Der Bagger, die zweite Haelfte der Grundgesteinskette.
        this.register(new GenericRecipe("ass.excavator").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_EXCAVATOR, 1))
                .inputItems(
                        new ComparableStack(Blocks.STONE_BRICKS.asItem(), 8),
                        new ComparableStack(NtmItems.INGOT_STEEL.get(), 8),
                        new ComparableStack(Items.IRON_INGOT, 8),
                        new ComparableStack(NtmItems.MOTOR.get(), 2),
                        new ComparableStack(NtmItems.CIRCUIT_ANALOG_BOARD.get(), 1)
                ));

        // Die zehn Bohrkoepfe: fuenf Grundkoepfe und fuenf Diamantfassungen, die jeweils den
        // Grundkopf verbrauchen. ANY_RESISTANTALLOY ist im Port der Schnellarbeitsstahl.
        drillbit("ass.drillsteel", DrillbitItem.EnumDrillType.STEEL,
                new ComparableStack(NtmItems.INGOT_STEEL.get(), 12),
                new ComparableStack(NtmItems.INGOT_TUNGSTEN.get(), 4));
        drillbitDiamond("ass.drillsteeldiamond", DrillbitItem.EnumDrillType.STEEL, DrillbitItem.EnumDrillType.STEEL_DIAMOND, 16);

        drillbit("ass.drilldura", DrillbitItem.EnumDrillType.HSS,
                new ComparableStack(NtmItems.INGOT_DURA_STEEL.get(), 12),
                new ComparableStack(NtmItems.INGOT_POLYMER.get(), 12),
                new ComparableStack(NtmItems.INGOT_TITANIUM.get(), 8));
        drillbitDiamond("ass.drillduradiamond", DrillbitItem.EnumDrillType.HSS, DrillbitItem.EnumDrillType.HSS_DIAMOND, 24);

        drillbit("ass.drilldesh", DrillbitItem.EnumDrillType.DESH,
                new ComparableStack(NtmItems.INGOT_DESH.get(), 16),
                new ComparableStack(NtmItems.INGOT_RUBBER.get(), 12),
                new ComparableStack(NtmItems.INGOT_NIOBIUM.get(), 4));
        drillbitDiamond("ass.drilldeshdiamond", DrillbitItem.EnumDrillType.DESH, DrillbitItem.EnumDrillType.DESH_DIAMOND, 32);

        drillbit("ass.drilltc", DrillbitItem.EnumDrillType.TCALLOY,
                new ComparableStack(NtmItems.INGOT_DURA_STEEL.get(), 20),
                new ComparableStack(NtmItems.INGOT_DESH.get(), 12),
                new ComparableStack(NtmItems.INGOT_RUBBER.get(), 8));
        drillbitDiamond("ass.drilltcdiamond", DrillbitItem.EnumDrillType.TCALLOY, DrillbitItem.EnumDrillType.TCALLOY_DIAMOND, 48);

        drillbit("ass.drillferro", DrillbitItem.EnumDrillType.FERRO,
                new ComparableStack(NtmItems.INGOT_FERROURANIUM.get(), 24),
                new ComparableStack(NtmItems.INGOT_DURA_STEEL.get(), 12),
                new ComparableStack(NtmItems.INGOT_BISMUTH.get(), 4));
        drillbitDiamond("ass.drillferrodiamond", DrillbitItem.EnumDrillType.FERRO, DrillbitItem.EnumDrillType.FERRO_DIAMOND, 56);

        // ---- Runde 136 ----
        // Original "ass.hazcloth": Bleistaub in Faden eingewebt. Das ist der einzige Weg
        // zum gelben Schutztuch; rot und grau werden daraus in der Werkbank weitergebaut.
        this.register(new GenericRecipe("ass.hazcloth").setup(50, 100).outputItems(new ItemStack(NtmItems.HAZMAT_CLOTH.get(), 4))
                .inputItems(
                        new ComparableStack(NtmItems.POWDER_LEAD.get(), 4),
                        new ComparableStack(Items.STRING, 8)
                ));

        // Original "ass.firecloth": ein Asbestbarren in Faden eingewebt. Das Tuch ist die
        // einzige Zutat der Asbestruestung und zugleich ihr Reparaturstueck.
        this.register(new GenericRecipe("ass.firecloth").setup(50, 100).outputItems(new ItemStack(NtmItems.ASBESTOS_CLOTH.get(), 4))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_ASBESTOS.get(), 1),
                        new ComparableStack(Items.STRING, 8)
                ));

        // ---- Runde 139 ----
        // Original "ass.reformer".
        // ABWEICHUNGEN, wie in allen Runden davor: ANY_RESISTANTALLOY ist im Port derselbe
        // Gegenstand wie DURA, und der Bismoid-Schaltkreis wird der integrierte.
        // Die inputItemsEx-Variante entfaellt wie ueberall.
        this.register(new GenericRecipe("ass.reformer").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_CATALYTIC_REFORMER, 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 12),
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 8),
                        new ComparableStack(NtmItems.INGOT_NIOBIUM.get(), 8),
                        new ComparableStack(NtmItems.INGOT_DURA_STEEL.get(), 4),
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 3),
                        new ComparableStack(NtmItems.PIPES_STEEL.get(), 8),
                        new ComparableStack(NtmItems.MOTOR.get(), 1),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 1)
                ));

        // ---- Runde 140 ----
        // Original "ass.hydrotreater". Denselben Abbildungen wie beim Reformer; zusaetzlich
        // steht der gewoehnliche Motor an der Stelle des Desh-Motors, den der Port nicht hat.
        this.register(new GenericRecipe("ass.hydrotreater").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_HYDROTREATER, 1))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.STEEL, 8),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.COPPER, 4),
                        new ComparableStack(NtmItems.INGOT_NIOBIUM.get(), 8),
                        new ComparableStack(NtmItems.INGOT_DURA_STEEL.get(), 4),
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 2),
                        new ComparableStack(NtmItems.PIPES_STEEL.get(), 8),
                        new ComparableStack(NtmItems.MOTOR.get(), 2),
                        new ComparableStack(NtmItems.CIRCUIT_INTEGRATED_BOARD.get(), 1)
                ));

        // ---- Runde 141 ----
        // Original "ass.vaccumrefinery" (der Schreibfehler steht so im Original, der Name
        // hier ist berichtigt). Dieselben Abbildungen wie beim Reformer; CHIP_BISMOID wird
        // der Mikrochip, wie in Runde 123.
        this.register(new GenericRecipe("ass.vacuumdistill").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_VACUUM_DISTILL, 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 16),
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 16),
                        new ComparableStack(NtmItems.INGOT_DURA_STEEL.get(), 4),
                        new ComparableStack(NtmItems.SPHERE_STEEL.get(), 1),
                        new ComparableStack(NtmItems.PIPES_STEEL.get(), 12),
                        new ComparableStack(NtmItems.MOTOR.get(), 3),
                        new ComparableStack(NtmItems.CIRCUIT_MICROCHIP.get(), 4)
                ));

        // ---- Runde 147 ----
        // Original "ass.crackingtower". Alle Zutaten stehen unveraendert.
        this.register(new GenericRecipe("ass.crackingtower").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_CATALYTIC_CRACKER, 1))
                .inputItems(
                        new ComparableStack(NtmBlocks.STEEL_SCAFFOLD.get(), 16),
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 6),
                        new ComparableStack(NtmItems.INGOT_DESH.get(), 12),
                        new ComparableStack(NtmItems.INGOT_NIOBIUM.get(), 4)
                ));

        // ---- Runde 146 ----
        // Original "ass.coker". Alle Zutaten stehen unveraendert.
        this.register(new GenericRecipe("ass.coker").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_COKER, 1))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.STEEL, 8),
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 4),
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 8),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 4),
                        new ComparableStack(NtmItems.INGOT_NIOBIUM.get(), 4)
                ));

        // ---- Runde 145 ----
        // Original "ass.flarestack". Alle Zutaten stehen unveraendert.
        this.register(new GenericRecipe("ass.flarestack").setup(100, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_GAS_FLARE, 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 12),
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 4),
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 4),
                        new ComparableStack(NtmItems.THERMO_ELEMENT.get(), 3)
                ));

        // ---- Runde 144 ----
        // Original "ass.liquefactor". ANY_TAR wird der Sammeltag der Teersorten, sonst steht
        // alles unveraendert.
        this.register(new GenericRecipe("ass.liquefactor").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_LIQUEFACTOR, 1))
                .inputItems(
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 4),
                        new ComparableStack(NtmItems.PLATE_COPPER.get(), 12),
                        new TagStack(ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "any_tars")), 4),
                        new ComparableStack(NtmItems.CIRCUIT_CAPACITOR.get(), 12),
                        new ComparableStack(NtmItems.COIL_TUNGSTEN.get(), 8)
                ));

        // ---- Runde 143 ----
        // Original "ass.pyrooven". Die ueblichen Abbildungen: ANY_HARDPLASTIC wird
        // Polycarbonat, der Desh-Motor wird der gewoehnliche Motor, CHIP_BISMOID wird der
        // Mikrochip. Statt Kupferrohren stehen Stahlrohre, die einzigen des Ports.
        this.register(new GenericRecipe("ass.pyrooven").setup(300, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_PYRO_OVEN, 1))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.STEEL, 16),
                        new ComparableStack(NtmItems.INGOT_PC.get(), 24),
                        new ComparableStack(NtmItems.PIPES_STEEL.get(), 12),
                        new ComparableStack(NtmItems.MOTOR.get(), 1),
                        new ComparableStack(NtmItems.CIRCUIT_MICROCHIP.get(), 1)
                ));

        // ---- Runde 142 ----
        // Original "ass.solidifier". ANY_PLASTIC.ingot() wird das Bakelit, das im Port der
        // einzige Kunststoffbarren ist; der Kondensatorschaltkreis und die Kupferspule
        // stehen unveraendert.
        this.register(new GenericRecipe("ass.solidifier").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_SOLIDIFIER, 1))
                .inputItems(
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 4),
                        new ComparableStack(NtmItems.PLATE_ALUMINIUM.get(), 12),
                        new ComparableStack(NtmItems.INGOT_BAKELITE.get(), 4),
                        new ComparableStack(NtmItems.CIRCUIT_CAPACITOR.get(), 12),
                        new ComparableStack(NtmItems.COIL_COPPER.get(), 4)
                ));

        // ---- Runde 154 ----
        /*
         * Original "ass.tank". Der gewoehnliche Tank ist seit Runde 1 im Port, hatte aber
         * kein Rezept -- in der Ueberlebensrunde war er damit unerreichbar. STEEL.plate()
         * wird die Stahlplatte, STEEL.shell() die Stahlhuelle. Die inputItemsEx-Fassung
         * entfaellt wie ueberall im Port.
         */
        this.register(new GenericRecipe("ass.tank").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_FLUID_TANK, 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 8),
                        new ComparableStack(NtmItems.SHELL_STEEL.get(), 4)
                ));

        /*
         * Original "ass.bigasstank". ANY_RESISTANTALLOY.plateWelded() wird die geschweisste
         * Schnellarbeitsstahlplatte -- im Port ist der Schnellarbeitsstahl der einzige
         * Vertreter dieser Gruppe, wie bei der Chemie- und der Montagefabrik.
         */
        this.register(new GenericRecipe("ass.bigasstank").setup(200, 100).outputItems(new ItemStack(NtmBlocks.MACHINE_BIGASSTANK, 1))
                .inputItems(
                        new ComparableStack(NtmItems.PLATE_STEEL.get(), 16),
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.DURA_STEEL, 4),
                        new ComparableStack(NtmBlocks.STEEL_SCAFFOLD.get(), 16)
                ));

        FluidType[] order = Fluids.getInNiceOrder();
        for(int i = 1; i < order.length; ++i) {
            FluidType type = order[i];
            if(type.hasNoContainer()) continue;
            this.register(new GenericRecipe("ass.package" + type.getUnlocalizedName()).setup(40, 100).outputItems(MetaHelper.newStack(NtmItems.FLUID_PACK_FULL, 1, type.getID()))
                    .inputItems(new ComparableStack(NtmItems.FLUID_PACK_EMPTY.get())).inputFluids(new FluidStack(type, 32_000)));
            this.register(new GenericRecipe("ass.unpackage" + type.getUnlocalizedName()).setup(40, 100).setIcon(FluidIconItem.make(type, 32_000)).outputItems(new ItemStack(NtmItems.FLUID_PACK_EMPTY.get()))
                    .inputItems(new ComparableStack(MetaHelper.newStack(NtmItems.FLUID_PACK_FULL, 1, type.getID()))).outputFluids(new FluidStack(type, 32_000)));
        }
    }

    /** Ein Grundbohrkopf: hundert Ticks, hundert HE je Tick, wie im Original. */
    private void drillbit(String name, DrillbitItem.EnumDrillType type, AStack... input) {
        this.register(new GenericRecipe(name).setup(100, 100)
                .outputItems(MetaHelper.newStack(NtmItems.DRILLBIT.get(), 1, type))
                .inputItems(input));
    }

    /** Die Diamantfassung: verbraucht den Grundkopf und eine Menge Diamantstaub. */
    private void drillbitDiamond(String name, DrillbitItem.EnumDrillType from, DrillbitItem.EnumDrillType to, int dust) {
        this.register(new GenericRecipe(name).setup(100, 100)
                .outputItems(MetaHelper.newStack(NtmItems.DRILLBIT.get(), 1, to))
                .inputItems(
                        new ComparableStack(MetaHelper.newStack(NtmItems.DRILLBIT.get(), 1, from)),
                        new ComparableStack(NtmItems.POWDER_DIAMOND.get(), dust)));
    }
}
