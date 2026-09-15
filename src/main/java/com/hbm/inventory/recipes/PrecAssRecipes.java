package com.hbm.inventory.recipes;

import com.hbm.config.NtmConfig;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.NBTStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.BrokenItem;
import com.hbm.items.NtmItems;
import com.hbm.items.WireDenseItem;
import com.hbm.items.machine.OrbitalAssemblyItem.EnumOrbitalAssembly;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.PrecAssRecipes.
 *
 * Die Praezisionsmontage ist die einzige Maschine, die MISSLINGEN kann. Jedes ihrer Rezepte ist
 * ein Paar: mit einer bestimmten Wahrscheinlichkeit faellt das Werkstueck heraus, sonst ein
 * Ausschuss -- und den nimmt sie zurueck und gibt einen Teil der Zutaten wieder her.
 *
 * DAS IST DER SINN DER SACHE: die Schaltkreise der spaeten Runde entstehen hier, und sie
 * entstehen nicht zuverlaessig. Wer sie will, muss Ausschuss einkalkulieren.
 *
 * NICHT UEBERNOMMEN: die vier Rezepte der RPA-Ruestung. Der Port hat weder die Ruestung selbst
 * noch die Legendenteile (parts_legendary, EnumLegendaryType) noch den Desh-Motor. Die
 * Originalzahlen stehen unten als Kommentar, damit sie beim Nachliefern nicht neu erhoben werden
 * muessen.
 */
public class PrecAssRecipes extends GenericRecipes<GenericRecipe> {

    public static final PrecAssRecipes INSTANCE = new PrecAssRecipes();

    @Override public int inputItemLimit() { return 9; }
    @Override public int inputFluidLimit() { return 1; }
    @Override public int outputItemLimit() { return 9; }
    @Override public int outputFluidLimit() { return 1; }

    @Override public String getFileName() { return "hbmPrecisionAssembly.json"; }
    @Override public GenericRecipe instantiateRecipe(String name) { return new GenericRecipe(name); }

    @Override
    public void registerDefaults() {

        /*
         * Der Kristallschaltkreis -- das teuerste Erzeugnis der Maschine und das einzige, das
         * auch ohne den 528er-Schalter entsteht.
         *
         * ABWEICHUNGEN nach dem bekannten Muster: EnumCircuitType.CHIP_QUANTUM heisst im Port
         * CIRCUIT_SOLID_STATE_QUANTUM_PROCESSOR, PCB ist CIRCUIT_PRINTED_BOARD, und
         * GOLD.wireFine() ist WIRE_GOLD -- der Port kennt keine zweite Drahtguete.
         *
         * NICHT UEBERNOMMEN: die inputItemsEx-Fassung, das zweite, teurere Zutatenmuster. Der
         * Port hat die Alternativmuster in keiner Maschine, wie in allen Runden davor.
         */
        registerPair(new GenericRecipe("precass.crystalcircuit").setup(600, 20_000L)
                .inputItems(
                        new ComparableStack(NtmItems.CIRCUIT_SOLID_STATE_QUANTUM_PROCESSOR.get(), 4),
                        new ComparableStack(NtmItems.INGOT_CTF.get(), 4),
                        new ComparableStack(NtmItems.CIRCUIT_PRINTED_BOARD.get(), 16),
                        new ComparableStack(NtmItems.WIRE_GOLD.get(), 32)),
                MetaHelper.newStack(NtmItems.ORBITAL_ASSEMBLY.get(), 1, EnumOrbitalAssembly.CRYSTAL_CIRCUIT), 50, 100);

        /*
         * MIT DEM 528er-SCHALTER WANDERT DIE SCHALTKREISFERTIGUNG HIERHER. Ohne ihn entstehen
         * die Chips weiter an der Montagemaschine; mit ihm nur noch hier, und nur gegen
         * Ausschussrisiko. Das ist im Original genauso, und der ganze Block steht dort in
         * derselben Bedingung.
         */
        if(NtmConfig.enable528()) {

            registerPair(new GenericRecipe("precass.chip").setup(100, 200L)
                    .inputItems(
                            new ComparableStack(NtmItems.CIRCUIT_PRINTED_SILICON_WAFER.get(), 1),
                            new ComparableStack(NtmItems.PLATE_POLYMER.get(), 3),
                            new ComparableStack(NtmItems.WIRE_GOLD.get(), 4))
                    .setPools(POOL_PREFIX_528 + "chip"),
                    new ItemStack(NtmItems.CIRCUIT_MICROCHIP.get()), 90, 90);

            registerPair(new GenericRecipe("precass.chip_bismoid").setup(200, 1_000L)
                    .inputItems(
                            new ComparableStack(NtmItems.CIRCUIT_PRINTED_SILICON_WAFER.get(), 4),
                            new ComparableStack(NtmItems.PLATE_POLYMER.get(), 8),
                            new ComparableStack(NtmItems.NUGGET_BISMUTH.get(), 2),
                            new ComparableStack(NtmItems.WIRE_GOLD.get(), 4))
                    .inputFluids(new FluidStack(Fluids.PERFLUOROMETHYL, 500))
                    .setPools(POOL_PREFIX_528 + "chip_bismoid"),
                    new ItemStack(NtmItems.CIRCUIT_VERSATILE_INTEGRATED.get()), 75, 75);

            registerPair(new GenericRecipe("precass.chip_quantum").setup(300, 20_000L)
                    .inputItems(
                            new ComparableStack(NtmItems.CIRCUIT_PRINTED_SILICON_WAFER.get(), 8),
                            new ComparableStack(MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 2, WireDenseItem.Type.BSCCO.meta)),
                            new ComparableStack(NtmItems.INGOT_PC.get(), 4),
                            new ComparableStack(NtmItems.PELLET_CHARGED.get(), 4),
                            new ComparableStack(NtmItems.WIRE_GOLD.get(), 8))
                    .inputFluids(new FluidStack(Fluids.HELIUM4, 250))
                    .setPools(POOL_PREFIX_528 + "chip_quantum"),
                    new ItemStack(NtmItems.CIRCUIT_SOLID_STATE_QUANTUM_PROCESSOR.get()), 90, 75);

            registerPair(new GenericRecipe("precass.atomic_clock").setup(200, 2_000L)
                    .inputItems(
                            new ComparableStack(NtmItems.CIRCUIT_MICROCHIP.get(), 8),
                            new ComparableStack(NtmItems.INGOT_POLYMER.get(), 4),
                            new ComparableStack(NtmItems.WIRE_ZIRCONIUM.get(), 8),
                            new ComparableStack(NtmItems.POWDER_STRONTIUM.get(), 1))
                    .setPools(POOL_PREFIX_528 + "strontium"),
                    new ItemStack(NtmItems.CIRCUIT_ATOMIC_CLOCK.get()), 50, 75);

            registerPair(new GenericRecipe("precass.controller").setup(400, 15_000L)
                    .inputItems(
                            new ComparableStack(NtmItems.CIRCUIT_MICROCHIP.get(), 32),
                            new ComparableStack(NtmItems.CIRCUIT_CAPACITOR.get(), 32),
                            new ComparableStack(NtmItems.CIRCUIT_TANTALIUM_CAPACITOR.get(), 16),
                            new ComparableStack(NtmItems.CIRCUIT_CONTROL_UNIT_CASING.get(), 1),
                            new ComparableStack(NtmItems.UPGRADE_SPEED_1.get(), 1),
                            new ComparableStack(NtmItems.WIRE_LEAD.get(), 16))
                    .inputFluids(new FluidStack(Fluids.PERFLUOROMETHYL, 1_000))
                    .setPools(POOL_PREFIX_528 + "controller"),
                    new ItemStack(NtmItems.CIRCUIT_CONTROL_UNIT.get()), 75, 90);

            registerPair(new GenericRecipe("precass.controller_advanced").setup(600, 25_000L)
                    .inputItems(
                            new ComparableStack(NtmItems.CIRCUIT_VERSATILE_INTEGRATED.get(), 16),
                            new ComparableStack(NtmItems.CIRCUIT_TANTALIUM_CAPACITOR.get(), 48),
                            new ComparableStack(NtmItems.CIRCUIT_ATOMIC_CLOCK.get(), 1),
                            new ComparableStack(NtmItems.CIRCUIT_CONTROL_UNIT_CASING.get(), 1),
                            new ComparableStack(NtmItems.UPGRADE_SPEED_3.get(), 1),
                            new ComparableStack(NtmItems.WIRE_LEAD.get(), 24))
                    .inputFluids(new FluidStack(Fluids.PERFLUOROMETHYL, 4_000)),
                    new ItemStack(NtmItems.CIRCUIT_ADVANCED_CONTROL_UNIT.get()), 50, 75);

            registerPair(new GenericRecipe("precass.controller_quantum").setup(600, 250_000L)
                    .inputItems(
                            new ComparableStack(NtmItems.CIRCUIT_SOLID_STATE_QUANTUM_PROCESSOR.get(), 16),
                            new ComparableStack(NtmItems.CIRCUIT_VERSATILE_INTEGRATED.get(), 48),
                            new ComparableStack(NtmItems.CIRCUIT_ATOMIC_CLOCK.get(), 8),
                            new ComparableStack(NtmItems.CIRCUIT_ADVANCED_CONTROL_UNIT.get(), 2),
                            new ComparableStack(NtmItems.UPGRADE_OVERDRIVE_1.get(), 1),
                            new ComparableStack(NtmItems.WIRE_LEAD.get(), 32))
                    .inputFluids(new FluidStack(Fluids.PERFLUOROMETHYL_COLD, 6_000)),
                    new ItemStack(NtmItems.CIRCUIT_QUANTUM_COMPUTER.get()), 75, 75);

            /* Die Aufwertungen der zweiten und dritten Stufe. Im Original heisst der Kommentar
             * daneben "upgrades are now actually valuable" -- sie entstehen nur noch hier. */
            addFirstUpgrade(NtmItems.UPGRADE_SPEED_1.get(), NtmItems.UPGRADE_SPEED_2.get(), "precass.upgrade_speed_ii");
            addSecondUpgrade(NtmItems.UPGRADE_SPEED_2.get(), NtmItems.UPGRADE_SPEED_3.get(), "precass.upgrade_speed_iii");
            addFirstUpgrade(NtmItems.UPGRADE_EFFECT_1.get(), NtmItems.UPGRADE_EFFECT_2.get(), "precass.upgrade_effect_ii");
            addSecondUpgrade(NtmItems.UPGRADE_EFFECT_2.get(), NtmItems.UPGRADE_EFFECT_3.get(), "precass.upgrade_effect_iii");
            addFirstUpgrade(NtmItems.UPGRADE_POWER_1.get(), NtmItems.UPGRADE_POWER_2.get(), "precass.upgrade_power_ii");
            addSecondUpgrade(NtmItems.UPGRADE_POWER_2.get(), NtmItems.UPGRADE_POWER_3.get(), "precass.upgrade_power_iii");
            addFirstUpgrade(NtmItems.UPGRADE_FORTUNE_1.get(), NtmItems.UPGRADE_FORTUNE_2.get(), "precass.upgrade_fortune_ii");
            addSecondUpgrade(NtmItems.UPGRADE_FORTUNE_2.get(), NtmItems.UPGRADE_FORTUNE_3.get(), "precass.upgrade_fortune_iii");
            addFirstUpgrade(NtmItems.UPGRADE_AFTERBURN_1.get(), NtmItems.UPGRADE_AFTERBURN_2.get(), "precass.upgrade_ab_ii");
            addSecondUpgrade(NtmItems.UPGRADE_AFTERBURN_2.get(), NtmItems.UPGRADE_AFTERBURN_3.get(), "precass.upgrade_ab_iii");

            /* Die drei Uebertakter. Der Name "overdive" ist der Schreibfehler des Originals und
             * bleibt: er steht im Rezeptschluessel und damit in der JSON-Vorlage. */
            registerPair(new GenericRecipe("precass.upgrade_overdive_i").setup(200, 1_000L)
                    .inputItems(
                            new ComparableStack(NtmItems.UPGRADE_SPEED_3.get(), 1),
                            new ComparableStack(NtmItems.UPGRADE_EFFECT_3.get(), 1),
                            new ComparableStack(NtmItems.INGOT_SATURNITE.get(), 16),
                            new ComparableStack(NtmItems.INGOT_PC.get(), 16),
                            new ComparableStack(NtmItems.CIRCUIT_MILITARY_GRADE_BOARD.get(), 16)),
                    new ItemStack(NtmItems.UPGRADE_OVERDRIVE_1.get()), 50, 75);

            registerPair(new GenericRecipe("precass.upgrade_overdive_ii").setup(600, 5_000L)
                    .inputItems(
                            new ComparableStack(NtmItems.UPGRADE_OVERDRIVE_1.get(), 1),
                            new ComparableStack(NtmItems.UPGRADE_SPEED_3.get(), 1),
                            new ComparableStack(NtmItems.UPGRADE_EFFECT_3.get(), 1),
                            new ComparableStack(NtmItems.INGOT_SATURNITE.get(), 16),
                            new ComparableStack(NtmItems.INGOT_CTF.get(), 8),
                            new ComparableStack(NtmItems.CIRCUIT_CAPACITOR_BOARD.get(), 16)),
                    new ItemStack(NtmItems.UPGRADE_OVERDRIVE_2.get()), 50, 75);

            registerPair(new GenericRecipe("precass.upgrade_overdive_iii").setup(1_200, 100_000L)
                    .inputItems(
                            new ComparableStack(NtmItems.UPGRADE_OVERDRIVE_2.get(), 1),
                            new ComparableStack(NtmItems.UPGRADE_SPEED_3.get(), 1),
                            new ComparableStack(NtmItems.UPGRADE_EFFECT_3.get(), 1),
                            new ComparableStack(NtmItems.INGOT_BISMUTH_BRONZE.get(), 16),
                            new ComparableStack(NtmItems.INGOT_CTF.get(), 16),
                            new ComparableStack(NtmItems.CIRCUIT_VERSATILE_BOARD.get(), 16)),
                    new ItemStack(NtmItems.UPGRADE_OVERDRIVE_3.get()), 25, 75);
        }

        int min = 1_200;

        /*
         * Die beiden Blaupausenmappen. Sie sind KEIN Paar -- hier gibt es keinen Ausschuss,
         * sondern eine schlichte Aussicht: zehn beziehungsweise fuenf von hundert Laeufen
         * liefern die Mappe, der Rest gibt das Papier zurueck.
         *
         * ABWEICHUNG: KEY_BLUE des Originals ist der Erzwoerterbuch-Name "dyeBlue"; der Port hat
         * kein Erzwoerterbuch und nimmt den blauen Farbstoff. CINNABAR.gem() ist der Zinnober.
         * Und ItemFishFood.FishType.PUFFERFISH ist auf 1.21 ein eigener Gegenstand.
         */
        this.register(new GenericRecipe("precass.blueprints").setup(5 * min, 20_000L)
                .inputItems(
                        new ComparableStack(Items.PAPER, 16),
                        new ComparableStack(Items.BLUE_DYE, 16),
                        new ComparableStack(Items.PUFFERFISH, 4))
                .outputItems(new ChanceOutputMulti(
                        new ChanceOutput(MetaHelper.newStack(NtmItems.BLUEPRINTS.get(), 1, 0), 10),
                        new ChanceOutput(new ItemStack(Items.PAPER, 16), 90))));

        this.register(new GenericRecipe("precass.beigeprints").setup(5 * min, 50_000L)
                .inputItems(
                        new ComparableStack(Items.PAPER, 24),
                        new ComparableStack(NtmItems.CINNABAR.get(), 24),
                        new ComparableStack(Items.PUFFERFISH, 8))
                .outputItems(new ChanceOutputMulti(
                        new ChanceOutput(MetaHelper.newStack(NtmItems.BLUEPRINTS.get(), 1, 1), 5),
                        new ChanceOutput(new ItemStack(Items.PAPER, 24), 95))));

        /*
         * NICHT UEBERNOMMEN, Originalzahlen zum Nachliefern:
         *   precass.rpahelmet  3*min / 25_000  <- plate_armor_ajr 12, BIGMT.plateCast() 4,
         *                                         circuit ADVANCED 4, plate_kevlar 8
         *   precass.rpaplate   3*min / 25_000  <- plate_armor_ajr 24, BIGMT.plateCast() 8,
         *                                         BIGMT.mechanism() 8, motor_desh 8,
         *                                         plate_kevlar 16, parts_legendary TIER2 1
         *   precass.rpalegs    3*min / 25_000  <- plate_armor_ajr 24, BIGMT.plateCast() 8,
         *                                         motor_desh 8, plate_kevlar 16
         *   precass.rpaboots   3*min / 25_000  <- plate_armor_ajr 12, BIGMT.plateCast() 4,
         *                                         plate_kevlar 8
         *   alle vier im Vorrat POOL_PREFIX_DISCOVER + ".rpa"
         * Es fehlen: die RPA-Ruestung selbst, parts_legendary samt EnumLegendaryType und der
         * Desh-Motor.
         */
    }

    public void addFirstUpgrade(Item lower, Item higher, String name) {
        registerPair(new GenericRecipe(name).setup(300, 10_000L)
                .inputItems(
                        new ComparableStack(NtmItems.CIRCUIT_MICROCHIP.get(), 8),
                        new ComparableStack(NtmItems.CIRCUIT_TANTALIUM_CAPACITOR.get(), 4),
                        new ComparableStack(lower),
                        new ComparableStack(NtmItems.INGOT_POLYMER.get(), 4)),
                new ItemStack(higher), 50, 75);
    }

    public void addSecondUpgrade(Item lower, Item higher, String name) {
        registerPair(new GenericRecipe(name).setup(400, 25_000L)
                .inputItems(
                        new ComparableStack(NtmItems.CIRCUIT_MICROCHIP.get(), 16),
                        new ComparableStack(NtmItems.CIRCUIT_TANTALIUM_CAPACITOR.get(), 16),
                        new ComparableStack(lower),
                        new ComparableStack(NtmItems.INGOT_RUBBER.get(), 4))
                .inputFluids(new FluidStack(Fluids.SOLVENT, 500)),
                new ItemStack(higher), 25, 75);
    }

    /**
     * Registriert ein Paar: das Rezept mit seiner Ausschussaussicht und das Rueckgewinnungsrezept
     * dazu.
     *
     * chance ist die Aussicht in Prozent, dass das Stueck gelingt; reclaim, welcher Anteil der
     * Zutaten beim Einschmelzen des Ausschusses zurueckkommt.
     *
     * DAS RUECKGEWINNUNGSREZEPT ERKENNT DEN AUSSCHUSS AM DATENANHANG -- daher der NBTStack. Ohne
     * ihn passte jeder Ausschuss auf jedes Rueckgewinnungsrezept, und man koennte den billigsten
     * Fehlschlag gegen die teuersten Zutaten tauschen.
     */
    public void registerPair(GenericRecipe recipe, ItemStack output, int chance, int reclaim) {

        recipe.outputItems(new ChanceOutputMulti(
                new ChanceOutput(output, chance),
                new ChanceOutput(BrokenItem.make(output), 100 - chance)));

        this.register(recipe);

        float fReclaim = reclaim / 100F;

        IOutput[] recycle = new IOutput[recipe.inputItem.length];
        for(int i = 0; i < recycle.length; i++) {
            ItemStack stack = recipe.inputItem[i].extractForJEI().get(0).copy();
            recycle[i] = new ChanceOutput(stack, fReclaim);
        }

        FluidStack[] fluid = recipe.inputFluid != null ? new FluidStack[1] : null;
        if(fluid != null) {
            fluid[0] = new FluidStack(recipe.inputFluid[0].type, (int) Math.round(recipe.inputFluid[0].fill * fReclaim));
        }

        this.register(new GenericRecipe(recipe.getInternalName() + ".recycle").setup(recipe.duration, recipe.power).setNameWrapper("precass.recycle")
                .setIcon(BrokenItem.make(output))
                .inputItems(new AStack[] { new NBTStack(BrokenItem.make(output)) })
                .outputItems(recycle)
                .outputFluids(fluid));
    }
}
