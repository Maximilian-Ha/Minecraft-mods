package com.hbm.inventory.recipes;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutput;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutputMulti;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.DriveItem.DriveType;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.SuperComputerRecipes.
 *
 * Der Grossrechner tut dreierlei mit Datentraegern: er RECHNET Vorausberechnungen aus, er
 * WERTET Messungen aus, und er KOPIERT beschriebene Traeger.
 *
 * JEDES DAVON GIBT ES DREIMAL, und darin liegt der eigentliche Reiz der Maschine: je kaelter
 * die Kuehlung, desto schneller -- und desto oefter geht der Traeger dabei kaputt. Mit Wasser
 * dauert die Flugbahnrechnung fuenfzehn Minuten und glueckt in fuenfundneunzig von hundert
 * Faellen; mit Helium dauert sie eine Minute und glueckt in fuenfundzwanzig.
 *
 * NICHT UEBERNOMMEN: "com.blueprints" und "com.beigeprints", die aus Papier und Farbstoff eine
 * Blaupausenmappe ziehen. Die Mappe ist im Original ein eigener Gegenstand mit zwei Metawerten,
 * aus dem man eine ZUFAELLIGE Blaupause zieht. Der Port kennt nur die Blaupause selbst, an einen
 * festen Rezeptvorrat gebunden -- eine Mappe gibt es nicht, und ChanceOutput braucht einen
 * festen Stapel. Wer die Mappe nachreicht, traegt die beiden Rezepte hier nach.
 */
public class SuperComputerRecipes extends GenericRecipes<GenericRecipe> {

    public static final SuperComputerRecipes INSTANCE = new SuperComputerRecipes();

    private static final int MIN = 60 * 20;

    @Override public int inputItemLimit() { return 3; }
    @Override public int inputFluidLimit() { return 1; }
    @Override public int outputItemLimit() { return 3; }
    @Override public int outputFluidLimit() { return 1; }

    @Override public String getFileName() { return "hbmSuperComputer.json"; }
    @Override public GenericRecipe instantiateRecipe(String name) { return new GenericRecipe(name); }

    @Override
    public void registerDefaults() {
        if(!this.recipeOrderedList.isEmpty()) return;

        // rechnen
        this.registerSimulation(DriveType.FLASH_FLIGHTSIM, "com.flightcalc");
        this.registerSimulation(DriveType.FLASH_PARTICLESIM, "com.particlecalc");

        // auswerten
        this.registerTriplet("com.processflight", 30 * MIN, 15 * MIN, 5 * MIN, DriveType.DISK_FLIGHTDATA, DriveType.DISK_FLIGHTDATA_PROCESSED, DriveType.DISK_BROKEN, 99, 95, 90);
        this.registerTriplet("com.processorbit", 60 * MIN, 30 * MIN, 15 * MIN, DriveType.DISK_ORBITDATA, DriveType.DISK_ORBITDATA_PROCESSED, DriveType.DISK_BROKEN, 75, 65, 50);

        // kopieren
        this.registerCopy("com.copyflightcalc", 15 * MIN, DriveType.FLASH_FLIGHTSIM, DriveType.FLASH_EMPTY, DriveType.FLASH_BROKEN, 95);
        this.registerCopy("com.copyparticlecalc", 15 * MIN, DriveType.FLASH_PARTICLESIM, DriveType.FLASH_EMPTY, DriveType.FLASH_BROKEN, 95);
        /* Der Schreibfehler im Namen ("fligth") steht so im Original. Er ist der Schluessel, unter
         * dem das Rezept in hbmSuperComputer.json landet -- berichtigen hiesse, gespeicherte
         * Einstellungen der Maschine ins Leere zeigen zu lassen. */
        this.registerCopy("com.copyfligthdata", 15 * MIN, DriveType.DISK_FLIGHTDATA_PROCESSED, DriveType.DISK_EMPTY, DriveType.DISK_BROKEN, 75);

        /* Eine Stunde, fuenf Millionen HE, hundertzweiundneunzig leere Platten und ein
         * Kubikmeter Wasser. Heraus kommt Klaus. */
        this.register(new GenericRecipe("com.klaus").setup(60 * MIN, 5_000_000L)
                .inputItems(
                        new ComparableStack(NtmItems.DRIVE.get(), 64, DriveType.DISK_EMPTY),
                        new ComparableStack(NtmItems.DRIVE.get(), 64, DriveType.DISK_EMPTY),
                        new ComparableStack(NtmItems.DRIVE.get(), 64, DriveType.DISK_EMPTY))
                .inputFluids(new FluidStack(Fluids.WATER, 1_000_000))
                .outputItems(MetaHelper.newStack(NtmItems.DRIVE.get(), 1, DriveType.KLAUS))
                .outputFluids(new FluidStack(Fluids.SLOP, 1_000)));
    }

    protected void registerSimulation(DriveType type, String name) {
        this.registerTriplet(name, 15 * MIN, 5 * MIN / 2, MIN, DriveType.FLASH_EMPTY, type, DriveType.FLASH_BROKEN, 95, 50, 25);
    }

    /**
     * Dasselbe Rezept dreimal, einmal je Kuehlmittel: Wasser ist langsam und sicher,
     * Perfluormethyl liegt dazwischen, Helium ist schnell und riskant. Das Helium wird dabei
     * verbraucht und kommt nicht wieder heraus -- so steht es im Original.
     */
    protected void registerTriplet(String name, int time0, int time1, int time2, DriveType input, DriveType output, DriveType broken, int chance0, int chance1, int chance2) {

        this.register(new GenericRecipe(name + "_water").setup(time0, 10_000)
                .inputItems(new ComparableStack(NtmItems.DRIVE.get(), 1, input))
                .outputItems(new ChanceOutputMulti(
                        new ChanceOutput(MetaHelper.newStack(NtmItems.DRIVE.get(), 1, output), chance0),
                        new ChanceOutput(MetaHelper.newStack(NtmItems.DRIVE.get(), 1, broken), 100 - chance0)))
                .inputFluids(new FluidStack(Fluids.WATER, 16_000)).outputFluids(new FluidStack(Fluids.SPENTSTEAM, 16_000)));

        this.register(new GenericRecipe(name + "_pfm").setup(time1, 10_000)
                .inputItems(new ComparableStack(NtmItems.DRIVE.get(), 1, input))
                .outputItems(new ChanceOutputMulti(
                        new ChanceOutput(MetaHelper.newStack(NtmItems.DRIVE.get(), 1, output), chance1),
                        new ChanceOutput(MetaHelper.newStack(NtmItems.DRIVE.get(), 1, broken), 100 - chance1)))
                .inputFluids(new FluidStack(Fluids.PERFLUOROMETHYL_COLD, 16_000)).outputFluids(new FluidStack(Fluids.PERFLUOROMETHYL, 16_000)));

        this.register(new GenericRecipe(name + "_helium").setup(time2, 10_000)
                .inputItems(new ComparableStack(NtmItems.DRIVE.get(), 1, input))
                .outputItems(new ChanceOutputMulti(
                        new ChanceOutput(MetaHelper.newStack(NtmItems.DRIVE.get(), 1, output), chance2),
                        new ChanceOutput(MetaHelper.newStack(NtmItems.DRIVE.get(), 1, broken), 100 - chance2)))
                .inputFluids(new FluidStack(Fluids.HELIUM4, 16_000)));
    }

    /** Ein beschriebener und ein leerer Traeger hinein, zwei beschriebene heraus -- wenn es glueckt. */
    protected void registerCopy(String name, int time, DriveType full, DriveType empty, DriveType broken, int chance) {

        this.register(new GenericRecipe(name).setup(time, 10_000).setNameWrapper("com.copy")
                .inputItems(
                        new ComparableStack(NtmItems.DRIVE.get(), 1, full),
                        new ComparableStack(NtmItems.DRIVE.get(), 1, empty))
                .outputItems(new ChanceOutputMulti(
                        new ChanceOutput(MetaHelper.newStack(NtmItems.DRIVE.get(), 2, full), chance),
                        new ChanceOutput(MetaHelper.newStack(NtmItems.DRIVE.get(), 2, broken), 100 - chance))));
    }
}
