package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.FluidIconItem;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.FusionRecipes.
 *
 * Elf Brennstoffe, aufsteigend nach Zuendschwelle. Das ist der ganze Fortschrittsbaum der Fusion:
 * mit Deuterium-Deuterium faengt man an, weil es mit einem Klystron zuendet; was dabei an Helium
 * und Tritium anfaellt, reicht fuer die naechste Stufe, und so weiter bis zum Sternenfluss, der
 * zwanzig Klystrons braucht und das Fuenfzigfache herausgibt.
 *
 * Alle Zahlen unveraendert aus dem Original. Der Neutronenfluss ist dort als Bruchteil der
 * Brutreaktor-Kapazitaet angegeben; die Zahl steht hier als Konstante, damit die Rezepte nicht am
 * Brutreaktor haengen, der erst spaeter nachkommt.
 *
 * ABWEICHUNG: das Original nimmt als Rezeptsymbol teils die gefuellte Gasflasche (gas_full), die
 * es im Port nicht gibt. An ihrer Stelle steht das Fluidsymbol, das der Port ohnehin ueberall
 * fuer Fluide benutzt.
 */
public class FusionRecipes extends GenericRecipes<FusionRecipe> {

    public static final FusionRecipes INSTANCE = new FusionRecipes();

    /** Die hoechste Zuendschwelle aller Rezepte -- danach richtet sich das Kreativ-Klystron. */
    public long maxInput;

    /** Fassungsvermoegen des Brutreaktors, aus TileEntityFusionBreeder.capacity. */
    public static final double BREEDER_CAPACITY = 10_000D;

    @Override public int inputItemLimit() { return 0; }
    @Override public int inputFluidLimit() { return 3; }
    @Override public int outputItemLimit() { return 1; }
    @Override public int outputFluidLimit() { return 11; }

    @Override public String getFileName() { return "hbmFusion.json"; }
    @Override public FusionRecipe instantiateRecipe(String name) { return new FusionRecipe(name); }

    @Override
    public void registerDefaults() {
        if(!this.recipeOrderedList.isEmpty()) return;

        long solenoid = 25_000;

        /* Vor allem zum Bruten von Helium und Tritium; der Ertrag reicht, um TH4 zu zuenden. */
        this.register((FusionRecipe) new FusionRecipe("fus.dd").setInputEnergy(750_000).setOutputEnergy(1_000_000).setOutputFlux(BREEDER_CAPACITY / 200)
                .setRGB(1F, 0.2F, 0.2F)
                .setNamed().setIcon(FluidIconItem.make(Fluids.DEUTERIUM, 1_000))
                .setPower(solenoid).setDuration(100)
                .inputFluids(new FluidStack(Fluids.DEUTERIUM, 20))
                .outputFluids(new FluidStack(Fluids.HELIUM4, 1_000)));

        /* Der Einstieg. */
        this.register((FusionRecipe) new FusionRecipe("fus.do").setInputEnergy(250_000).setOutputEnergy(1_250_000).setOutputFlux(BREEDER_CAPACITY / 200)
                .setNamed().setIcon(FluidIconItem.make(Fluids.OXYGEN, 1_000))
                .setPower(solenoid).setDuration(100)
                .inputFluids(new FluidStack(Fluids.DEUTERIUM, 10), new FluidStack(Fluids.OXYGEN, 10))
                .outputItems(new ItemStack(NtmItems.PELLET_CHARGED.get())));

        this.register((FusionRecipe) new FusionRecipe("fus.dt").setInputEnergy(750_000).setOutputEnergy(3_750_000).setOutputFlux(BREEDER_CAPACITY / 100)
                .setNamed().setIcon(FluidIconItem.make(Fluids.HELIUM4, 1_000))
                .setPower(solenoid).setDuration(100)
                .inputFluids(new FluidStack(Fluids.DEUTERIUM, 10), new FluidStack(Fluids.TRITIUM, 10))
                .outputFluids(new FluidStack(Fluids.HELIUM4, 1_000)));

        /* Drei Klystrons, oder zwei im Wechsel mit einem anderen Brennstoff. */
        this.register((FusionRecipe) new FusionRecipe("fus.tcl").setInputEnergy(2_500_000).setOutputEnergy(6_250_000).setOutputFlux(BREEDER_CAPACITY / 20)
                .setRGB(0.8F, 0.6F, 0.4F)
                .setNamed().setIcon(new ItemStack(NtmItems.POWDER_CHLOROPHYTE.get()))
                .setPower(solenoid).setDuration(100)
                .inputFluids(new FluidStack(Fluids.TRITIUM, 10), new FluidStack(Fluids.CHLORINE, 10))
                .outputItems(new ItemStack(NtmItems.POWDER_CHLOROPHYTE.get())));

        /* Aneutronisch -- kein Fluss, also nichts fuer den Brutreaktor. */
        this.register((FusionRecipe) new FusionRecipe("fus.h3").setInputEnergy(500_000).setOutputEnergy(3_750_000).setOutputFlux(0)
                .setRGB(0.2F, 0.2F, 1F)
                .setNamed().setIcon(FluidIconItem.make(Fluids.HELIUM3, 1_000))
                .setPower(solenoid).setDuration(100)
                .inputFluids(new FluidStack(Fluids.HELIUM3, 20))
                .outputFluids(new FluidStack(Fluids.HELIUM4, 1_000)));

        this.register((FusionRecipe) new FusionRecipe("fus.th4").setInputEnergy(875_000).setOutputEnergy(4_000_000).setOutputFlux(BREEDER_CAPACITY / 20)
                .setRGB(0.2F, 0.2F, 1F)
                .setNamed().setIcon(FluidIconItem.make(Fluids.TRITIUM, 1_000))
                .setPower(solenoid).setDuration(100)
                .inputFluids(new FluidStack(Fluids.TRITIUM, 10), new FluidStack(Fluids.HELIUM4, 10))
                .outputItems(new ItemStack(NtmItems.PELLET_CHARGED.get())));

        /* Ab hier reicht ein Klystron nicht mehr: erst TH4 oder H3 bringen die Anlage so weit. */
        this.register((FusionRecipe) new FusionRecipe("fus.cl").setInputEnergy(3_750_000).setOutputEnergy(10_000_000).setOutputFlux(BREEDER_CAPACITY / 10)
                .setRGB(1F, 0.6F, 0.2F)
                .setNamed().setIcon(new ItemStack(NtmItems.POWDER_CHLOROPHYTE.get()))
                .setPower(solenoid).setDuration(100)
                .inputFluids(new FluidStack(Fluids.CHLORINE, 20))
                .outputItems(new ItemStack(NtmItems.POWDER_CHLOROPHYTE.get())));

        this.register((FusionRecipe) new FusionRecipe("fus.dhc").setInputEnergy(10_000_000).setOutputEnergy(25_000_000).setOutputFlux(BREEDER_CAPACITY / 5)
                .setRGB(0.2F, 0.8F, 0.8F)
                .setNamed().setIcon(FluidIconItem.make(Fluids.DHC, 1_000))
                .setPower(solenoid).setDuration(100)
                .inputFluids(new FluidStack(Fluids.DHC, 20))
                .outputItems(new ItemStack(NtmItems.POWDER_CHLOROPHYTE.get())));

        /* Viel Ertrag bei niedriger Schwelle -- der Grund, Hoellenfeuer ueberhaupt herzustellen. */
        this.register((FusionRecipe) new FusionRecipe("fus.bf").setInputEnergy(1_000_000).setOutputEnergy(12_500_000).setOutputFlux(BREEDER_CAPACITY / 5)
                .setRGB(0.2F, 1F, 0.2F)
                .setNamed().setIcon(FluidIconItem.make(Fluids.BALEFIRE, 1_000))
                .setPower(solenoid).setDuration(100)
                .inputFluids(new FluidStack(Fluids.BALEFIRE, 15), new FluidStack(Fluids.AMAT, 5))
                .outputItems(new ItemStack(NtmItems.POWDER_BALEFIRE.get())));

        /* Das Ende der Fahnenstange: zwanzig Klystrons, eine Gigawattstunde je Sekunde. */
        this.register((FusionRecipe) new FusionRecipe("fus.stellar").setInputEnergy(10_000_000).setOutputEnergy(50_000_000).setOutputFlux(BREEDER_CAPACITY)
                .setRGB(1F, 0.4F, 0.1F)
                .setNamed().setIcon(FluidIconItem.make(Fluids.STELLAR_FLUX, 1_000))
                .setPower(solenoid).setDuration(100)
                .inputFluids(new FluidStack(Fluids.STELLAR_FLUX, 10))
                .outputItems(new ItemStack(NtmItems.POWDER_GOLD.get())));
    }

    @Override
    public void registerPost() {
        super.registerPost();

        this.maxInput = 0;
        for(FusionRecipe recipe : this.recipeOrderedList) {
            if(recipe.ignitionTemp > this.maxInput) this.maxInput = recipe.ignitionTemp;
        }
    }

    @Override
    public void readExtraData(JsonElement element, FusionRecipe recipe) {
        JsonObject obj = (JsonObject) element;

        recipe.ignitionTemp = obj.get("ignitionTemp").getAsLong();
        recipe.outputTemp = obj.get("outputTemp").getAsLong();
        recipe.neutronFlux = obj.get("outputFlux").getAsDouble();
        recipe.r = obj.get("r").getAsFloat();
        recipe.g = obj.get("g").getAsFloat();
        recipe.b = obj.get("b").getAsFloat();
    }

    @Override
    public void writeExtraData(FusionRecipe recipe, JsonWriter writer) throws IOException {
        writer.name("ignitionTemp").value(recipe.ignitionTemp);
        writer.name("outputTemp").value(recipe.outputTemp);
        writer.name("outputFlux").value(recipe.neutronFlux);
        writer.name("r").value(recipe.r);
        writer.name("g").value(recipe.g);
        writer.name("b").value(recipe.b);
    }
}
