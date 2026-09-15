package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.TagStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.CastPlateItem;
import com.hbm.items.NtmItems;
import com.hbm.items.PartGenericItem;
import com.hbm.items.WireDenseItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.PlasmaForgeRecipes.
 *
 * Die Rezepte der Plasmaschmiede. Jedes hat neben Dauer und Strom eine Zuendtemperatur: so viel
 * Plasmaleistung je Tick muss der Fusionsreaktor liefern, sonst laeuft der Ofen nicht an. Damit
 * haengt der ganze Zweig hinter der Fusion -- und die Fusion selbst haengt an einem Rezept dieses
 * Ofens, dem Torus. Das Original loest den Ring auf, indem der erste Torus aus dem ICF-Reaktor
 * kommt; der Ofen selbst wird an der Montagemaschine gebaut.
 *
 * Alle Zahlen sind unveraendert aus dem Original. Die Ersetzungen sind dieselben wie ueberall:
 *
 *   ANY_BISMOIDBRONZE  -> Bismutbronze
 *   ANY_RESISTANTALLOY -> Schnellarbeitsstahl
 *   ANY_HARDPLASTIC    -> der Tag ntm:bars_hard_plastic
 *   BIGMT              -> Saturnit
 *   CMB                -> Combine-Stahl
 *   Bismoid-Schaltkreis -> Versatile-Platine, Quanten-Schaltkreis -> Quantenprozessor
 *   die teure Variante (inputItemsEx) entfaellt, weil item_expensive fehlt
 *
 * NICHT UEBERNOMMEN, weil die Erzeugnisse im Port noch fehlen:
 *
 *   plsm.schrabhammer   der Schrabidiumhammer
 *   ass.fensusan        die Redd-Batterie braucht das Quanten-Batteriepaket
 *   plsm.gerald         der Gerald-Satellit braucht Datentraeger und UFO-Muenze
 *   plsm.dfc*           fuenf Rezepte fuer die Bauteile der Dark-Fusion-Chamber
 *
 * Damit stehen 24 der 36 Rezepte des Originals. Die zwoelf ICF-Rezepte und der Torus schliessen
 * zwei Luecken, die seit Runde 63 und 66 in der ROADMAP stehen: beide waren bisher nur im
 * Kreativreiter zu haben.
 */
public class PlasmaForgeRecipes extends GenericRecipes<PlasmaForgeRecipe> {

    public static final PlasmaForgeRecipes INSTANCE = new PlasmaForgeRecipes();

    @Override public int inputItemLimit() { return 12; }
    @Override public int inputFluidLimit() { return 1; }
    @Override public int outputItemLimit() { return 1; }
    @Override public int outputFluidLimit() { return 0; }

    @Override public String getFileName() { return "hbmPlasmaForge.json"; }
    @Override public PlasmaForgeRecipe instantiateRecipe(String name) { return new PlasmaForgeRecipe(name); }

    private static final TagStack HARD_PLASTIC_16 = hardPlastic(16);

    private static TagStack hardPlastic(int count) {
        return new TagStack(ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "bars_hard_plastic")), count);
    }

    private static ComparableStack wire(WireDenseItem.Type type, int count) {
        return new ComparableStack(NtmItems.WIRE_DENSE.get(), count, type.meta);
    }

    private static ComparableStack fusionPart(int stage, int count) {
        return new ComparableStack(NtmBlocks.FUSION_COMPONENT.get().asItem(), count, stage);
    }

    /** Ein Schweissrezept: zwei Gussplatten werden zu einer verschweissten Platte. */
    private void weld(String name, CastPlateItem.Type type, int duration, long power, FluidStack fluid, String group) {
        PlasmaForgeRecipe recipe = (PlasmaForgeRecipe) new PlasmaForgeRecipe(name)
                .setInputEnergy(500_000).setup(duration, power)
                .outputItems(NtmItems.castPlateWelded(type))
                .inputItems(NtmItems.castPlateIngredient(type, 2));
        if(fluid != null) recipe.inputFluids(fluid);
        this.register((PlasmaForgeRecipe) recipe.setGroup(group, this));
    }

    @Override
    public void registerDefaults() {

        String autoPlate = "autoswitch.weldPlates";

        /* Die beiden Platten, die es nur hier gibt. */
        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.plateeuphemium")
                .setInputEnergy(1_000_000).setup(600, 10_000_000)
                .outputItems(new ItemStack(NtmItems.PLATE_EUPHEMIUM.get(), 4))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_EUPHEMIUM.get(), 4),
                        new ComparableStack(NtmItems.POWDER_ASTATINE.get(), 3),
                        new ComparableStack(NtmItems.POWDER_BISMUTH.get(), 1),
                        new ComparableStack(NtmItems.GEM_VOLCANIC.get(), 1),
                        new ComparableStack(NtmItems.INGOT_OSMIRIDIUM.get(), 1)));

        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.platednt")
                .setInputEnergy(1_000_000).setup(600, 10_000_000)
                .outputItems(new ItemStack(NtmItems.PLATE_DINEUTRONIUM.get(), 4))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_DINEUTRONIUM.get(), 4),
                        new ComparableStack(NtmItems.POWDER_SPARK_MIX.get(), 2),
                        new ComparableStack(NtmItems.INGOT_DESH.get(), 1)));

        /* Der Hochdichte-Emitter, das teuerste Bauteil des Originals. */
        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.hde")
                .setInputEnergy(10_000_000).setup(600, 25_000_000L)
                .outputItems(MetaHelper.newStack(NtmItems.PART_GENERIC.get(), 1, PartGenericItem.Type.HDE))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.BISMUTH_BRONZE, 2),
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.COMBINE_STEEL, 1),
                        new ComparableStack(NtmItems.INGOT_CTF.get(), 1))
                .inputFluids(new FluidStack(Fluids.STELLAR_FLUX, 4_000)));

        /*
         * Die elf Schweissrezepte. Der Lichtbogenschweisser kann dasselbe -- die Schmiede ist die
         * schnellere Sorte und schaltet ueber die Autoswitch-Gruppe selbsttaetig auf das Material
         * um, das gerade im ersten Fach liegt.
         */
        this.weld("plsm.weldiron",       CastPlateItem.Type.IRON,          50, 100L,        null, autoPlate);
        this.weld("plsm.weldsteel",      CastPlateItem.Type.STEEL,         50, 500L,        null, autoPlate);
        this.weld("plsm.weldcopper",     CastPlateItem.Type.COPPER,        50, 1_000L,      null, autoPlate);
        this.weld("plsm.weldtitanium",   CastPlateItem.Type.TITANIUM,     300, 50_000L,     null, autoPlate);
        this.weld("plsm.weldzirconium",  CastPlateItem.Type.ZIRCONIUM,    300, 10_000L,     null, autoPlate);
        this.weld("plsm.weldaluminium",  CastPlateItem.Type.ALUMINIUM,    150, 10_000L,     null, autoPlate);
        this.weld("plsm.weldtcalloy",    CastPlateItem.Type.TCALLOY,      600, 1_000_000L,  new FluidStack(Fluids.OXYGEN, 1_000), autoPlate);
        this.weld("plsm.weldcdalloy",    CastPlateItem.Type.CDALLOY,      600, 1_000_000L,  new FluidStack(Fluids.OXYGEN, 1_000), autoPlate);
        this.weld("plsm.weldtungsten",   CastPlateItem.Type.TUNGSTEN,     600, 250_000L,    new FluidStack(Fluids.OXYGEN, 1_000), autoPlate);
        this.weld("plsm.weldcmb",        CastPlateItem.Type.COMBINE_STEEL, 600, 10_000_000L, new FluidStack(Fluids.REFORMGAS, 1_000), autoPlate);
        this.weld("plsm.weldosmiridium", CastPlateItem.Type.OSMIRIDIUM, 3_000, 50_000_000L, new FluidStack(Fluids.REFORMGAS, 16_000), autoPlate);

        /*
         * Der Torus. 320 Spulen, 192 Verrohrungen, 128 Reaktordecken und vier Quantenprozessoren
         * -- das groesste Rezept des ganzen Mods.
         */
        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.fusionvessel")
                .setInputEnergy(3_000_000).setup(1_200, 2_000_000)
                .outputItems(new ItemStack(NtmBlocks.FUSION_TORUS.get(), 1))
                .inputItems(
                        fusionPart(0, 64), fusionPart(0, 64), fusionPart(0, 64),
                        fusionPart(0, 64), fusionPart(0, 64),
                        fusionPart(3, 64), fusionPart(3, 64), fusionPart(3, 64),
                        fusionPart(2, 64), fusionPart(2, 64),
                        new ComparableStack(NtmItems.CIRCUIT_QUANTUM_PROCESSING_UNIT.get(), 4)));

        /*
         * Die zwoelf Bauteile des ICF. Sie schliessen die Luecke aus Runde 63: bisher gab es die
         * Traegheitsfusion nur im Kreativreiter.
         */
        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.icfcell")
                .setInputEnergy(1_000_000).setup(800, 10_000_000)
                .outputItems(MetaHelper.newStack(NtmBlocks.ICF_LASER_COMPONENT.get(), 1, ICF_CELL))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_CTF.get(), 2),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.BISMUTH_BRONZE, 4),
                        new ComparableStack(NtmBlocks.GLASS_QUARTZ.get().asItem(), 16)));

        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.icfemitter")
                .setInputEnergy(1_000_000).setup(800, 10_000_000)
                .outputItems(MetaHelper.newStack(NtmBlocks.ICF_LASER_COMPONENT.get(), 1, ICF_EMITTER))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.TUNGSTEN, 4),
                        wire(WireDenseItem.Type.MAGNETIZED_TUNGSTEN, 16))
                .inputFluids(new FluidStack(Fluids.XENON, 16_000)));

        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.icfcapacitor")
                .setInputEnergy(1_000_000).setup(800, 10_000_000)
                .outputItems(MetaHelper.newStack(NtmBlocks.ICF_LASER_COMPONENT.get(), 1, ICF_CAPACITOR))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.DURA_STEEL, 1),
                        wire(WireDenseItem.Type.NEODYMIUM, 16),
                        wire(WireDenseItem.Type.SCHRABIDATE, 2)));

        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.icfturbo")
                .setInputEnergy(1_000_000).setup(800, 10_000_000)
                .outputItems(MetaHelper.newStack(NtmBlocks.ICF_LASER_COMPONENT.get(), 1, ICF_TURBO))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.DURA_STEEL, 2),
                        wire(WireDenseItem.Type.DINEUTRONIUM, 4),
                        wire(WireDenseItem.Type.SCHRABIDATE, 4)));

        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.icfcasing")
                .setInputEnergy(1_000_000).setup(800, 10_000_000)
                .outputItems(MetaHelper.newStack(NtmBlocks.ICF_LASER_COMPONENT.get(), 1, ICF_CASING))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.BISMUTH_BRONZE, 4),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.SATURNITE, 4),
                        HARD_PLASTIC_16));

        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.icfport")
                .setInputEnergy(1_000_000).setup(800, 10_000_000)
                .outputItems(MetaHelper.newStack(NtmBlocks.ICF_LASER_COMPONENT.get(), 1, ICF_PORT))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.BISMUTH_BRONZE, 4),
                        hardPlastic(16),
                        wire(WireDenseItem.Type.NEODYMIUM, 16)));

        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.icfcontroller")
                .setInputEnergy(1_000_000).setup(800, 10_000_000)
                .outputItems(new ItemStack(NtmBlocks.ICF_CONTROLLER.get(), 1))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_CTF.get(), 16),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.BISMUTH_BRONZE, 4),
                        hardPlastic(16),
                        new ComparableStack(NtmItems.CIRCUIT_VERSATILE_BOARD.get(), 16)));

        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.icfscaffold")
                .setInputEnergy(1_000_000).setup(800, 10_000_000)
                .outputItems(MetaHelper.newStack(NtmBlocks.ICF_COMPONENT.get(), 1, 0))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.STEEL, 4),
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.TITANIUM, 2)));

        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.icfvessel")
                .setInputEnergy(1_000_000).setup(800, 10_000_000)
                .outputItems(MetaHelper.newStack(NtmBlocks.ICF_COMPONENT.get(), 1, 1))
                .inputItems(
                        new ComparableStack(NtmItems.INGOT_CTF.get(), 1),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.COMBINE_STEEL, 1),
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.TUNGSTEN, 2)));

        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.icfstructural")
                .setInputEnergy(1_000_000).setup(800, 10_000_000)
                .outputItems(MetaHelper.newStack(NtmBlocks.ICF_COMPONENT.get(), 1, 3))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.STEEL, 2),
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.COPPER, 2),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.BISMUTH_BRONZE, 1)));

        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.icfcore")
                .setInputEnergy(1_000_000).setup(3_000, 10_000_000)
                .outputItems(new ItemStack(NtmBlocks.STRUCT_ICF.get(), 1))
                .inputItems(
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.COMBINE_STEEL, 16),
                        NtmItems.castPlateWeldedIngredient(CastPlateItem.Type.DURA_STEEL, 16),
                        NtmItems.castPlateIngredient(CastPlateItem.Type.BISMUTH_BRONZE, 16),
                        wire(WireDenseItem.Type.SCHRABIDATE, 32),
                        new ComparableStack(NtmItems.CIRCUIT_VERSATILE_BOARD.get(), 32),
                        new ComparableStack(NtmItems.CIRCUIT_QUANTUM_PROCESSING_UNIT.get(), 16)));

        this.register((PlasmaForgeRecipe) new PlasmaForgeRecipe("plsm.icfpress")
                .setInputEnergy(1_000_000).setup(800, 10_000_000)
                .outputItems(new ItemStack(NtmBlocks.MACHINE_ICF_PRESS.get(), 1))
                .inputItems(
                        NtmItems.castPlateIngredient(CastPlateItem.Type.GOLD, 8),
                        new ComparableStack(NtmItems.MOTOR.get(), 4),
                        new ComparableStack(NtmItems.CIRCUIT_VERSATILE_BOARD.get(), 1)));
    }

    /* Die Metadaten der sechs Laserbauteile, in der Reihenfolge von ICFLaserComponentBlock. */
    private static final int ICF_CASING = 0;
    private static final int ICF_PORT = 1;
    private static final int ICF_CELL = 2;
    private static final int ICF_EMITTER = 3;
    private static final int ICF_CAPACITOR = 4;
    private static final int ICF_TURBO = 5;

    @Override
    public void readExtraData(JsonElement element, PlasmaForgeRecipe recipe) {
        JsonObject obj = (JsonObject) element;
        recipe.ignitionTemp = obj.get("ignitionTemp").getAsLong();
    }

    @Override
    public void writeExtraData(PlasmaForgeRecipe recipe, JsonWriter writer) throws IOException {
        writer.name("ignitionTemp").value(recipe.ignitionTemp);
    }
}
