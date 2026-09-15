package com.hbm.inventory.recipes;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.TagStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.DepletedFuelItem;
import com.hbm.items.machine.PileRodItem.EnumPileRod;
import com.hbm.items.machine.PWRFuelItem.EnumPWRFuel;
import com.hbm.items.machine.WatzPelletItem.EnumWatzType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.PUREXRecipes.
 *
 * Die Wiederaufbereitung. Abgebrannter Brennstoff wird in Salpetersaeure und Kerosin geloest und
 * in seine Bestandteile zerlegt -- was sich noch spalten laesst, kommt zurueck, der Rest faellt
 * als Abfallkrumen an.
 *
 * Alle Zahlen, Dauern und Stromwerte sind unveraendert aus dem Original.
 *
 * NICHT UEBERNOMMEN, weil die zugehoerigen Gegenstaende im Port noch fehlen:
 *
 *   purex.watznaqadah  braucht Naquadria-Klumpen aus einem fremden Mod
 *   purex.vit*         drei Rezepte -- sie brauchen sand_mix, den Bleisand, den es nicht gibt
 *   purex.thoriumsalt  braucht die Rezeptansicht des Fluid-Symbols und den Salzkreislauf des
 *                      Fluessigsalzreaktors, der ebenfalls fehlt
 *
 * Jede dieser Gruppen kommt mit ihrem Reaktor nach. Alles andere steht: die neun ZIRNOX-Sorten,
 * die sieben Brennstoffplatten, die vier Chicago-Pile-Staebe, die fuenfzehn PWR-Sorten, die zwoelf
 * Watz-Pellets, das ICF-Kuegelchen, Schraranium und die drei Sonderrezepte fuer UZH,
 * Balefire-Gold und Blitzblei.
 */
public class PUREXRecipes extends GenericRecipes<PUREXRecipe> {

    public static final PUREXRecipes INSTANCE = new PUREXRecipes();

    @Override public int inputItemLimit() { return 3; }
    @Override public int inputFluidLimit() { return 3; }
    @Override public int outputItemLimit() { return 6; }
    @Override public int outputFluidLimit() { return 1; }

    @Override public String getFileName() { return "hbmPUREX.json"; }
    @Override public PUREXRecipe instantiateRecipe(String name) { return new PUREXRecipe(name); }

    /** Abgekuehlter Abfall -- heisser Abfall gehoert erst ins Brennstoffbecken. */
    private static ComparableStack cooled(DeferredItem<Item> waste) {
        return new ComparableStack(waste.get(), 1, DepletedFuelItem.COOL);
    }

    @Override
    public void registerDefaults() {
        if(!this.recipeOrderedList.isEmpty()) return;

        long pilePower = 100;
        long pwrPower = 2_500;
        long zirnoxPower = 1_000;
        long platePower = 1_500;

        this.register((PUREXRecipe) new PUREXRecipe("purex.uzh").setup(600, 1_000)
                .inputItems(new ComparableStack(NtmItems.BILLET_URANIUM_FUEL.get()),
                        new TagStack(MaterialShapes.BILLET.getTag(Mats.MAT_ZIRCONIUM), 3))
                .inputFluids(new FluidStack(Fluids.NITRIC_ACID, 1_000), new FluidStack(Fluids.HYDROGEN, 4_000))
                .outputItems(new ItemStack(NtmItems.BILLET_UZH.get(), 4)));

        this.register((PUREXRecipe) new PUREXRecipe("purex.flashgold").setup(600, 1_000)
                .inputItems(new TagStack(MaterialShapes.BILLET.getTag(Mats.MAT_AU198)),
                        new ComparableStack(NtmItems.PELLET_CHARGED.get()))
                .inputFluids(new FluidStack(Fluids.AMAT, 1_000))
                .outputItems(new ItemStack(NtmItems.BILLET_BALEFIRE_GOLD.get(), 2)));

        this.register((PUREXRecipe) new PUREXRecipe("purex.flashlead").setup(600, 1_000)
                .inputItems(new TagStack(MaterialShapes.BILLET.getTag(Mats.MAT_PB209)),
                        new ComparableStack(NtmItems.BILLET_BALEFIRE_GOLD.get()))
                .inputFluids(new FluidStack(Fluids.AMAT, 1_000))
                .outputItems(new ItemStack(NtmItems.BILLET_FLASHLEAD.get(), 1)));

        /* Chicago Pile MK2: die vier Staebe, die sich lohnen. Schwefelsaeure statt Kerosin und
         * Salpetersaeure -- der Pile ist die frueheste Anlage, seine Wiederaufbereitung die
         * billigste. */
        String autoPile = "autoswitch.pile";

        this.register((PUREXRecipe) new PUREXRecipe("purex.pilepu239").setup(40, pilePower).setNameWrapper("purex.recycle").setGroup(autoPile, this)
                .inputItems(rod(EnumPileRod.PU239))
                .inputFluids(sulfuric())
                .outputItems(new ItemStack(NtmItems.BILLET_PU239.get(), 2),
                        new ItemStack(NtmItems.BILLET_URANIUM.get(), 1))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pilergp").setup(40, pilePower).setNameWrapper("purex.recycle").setGroup(autoPile, this)
                .inputItems(rod(EnumPileRod.RGP))
                .inputFluids(sulfuric())
                .outputItems(new ItemStack(NtmItems.BILLET_PU_MIX.get(), 2),
                        new ItemStack(NtmItems.BILLET_NUCLEAR_WASTE.get(), 1))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pilethorium").setup(40, pilePower).setNameWrapper("purex.recycle").setGroup(autoPile, this)
                .inputItems(rod(EnumPileRod.THORIUM_FUEL))
                .inputFluids(sulfuric())
                .outputItems(new ItemStack(NtmItems.BILLET_THORIUM_FUEL.get(), 2),
                        new ItemStack(NtmItems.BILLET_NUCLEAR_WASTE.get(), 1))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pilewaste").setup(40, pilePower).setNameWrapper("purex.recycle").setGroup(autoPile, this)
                .inputItems(rod(EnumPileRod.WASTE))
                .inputFluids(sulfuric())
                .outputItems(new ItemStack(NtmItems.BILLET_NUCLEAR_WASTE.get(), 2),
                        new ItemStack(NtmItems.BILLET_POLONIUM.get(), 1))
                .setIconToFirstIngredient());

        /*
         * Die ZIRNOX-Reste. Alle neun laufen mit demselben Loesungsmittel und derselben Dauer;
         * der Umschaltverbund sorgt dafuer, dass die Maschine von selbst auf die Sorte wechselt,
         * die gerade im Eingabefach liegt.
         */
        String autoZirnox = "autoswitch.zirnox";

        this.register((PUREXRecipe) new PUREXRecipe("purex.zirnoxnu").setup(100, zirnoxPower).setNameWrapper("purex.recycle").setGroup(autoZirnox, this)
                .inputItems(cooled(NtmItems.WASTE_NATURAL_URANIUM))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_U238.get(), 1),
                        new ItemStack(NtmItems.NUGGET_PU_MIX.get(), 2),
                        new ItemStack(NtmItems.NUGGET_PU239.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 2))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.zirnoxmeu").setup(100, zirnoxPower).setNameWrapper("purex.recycle").setGroup(autoZirnox, this)
                .inputItems(cooled(NtmItems.WASTE_URANIUM))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_PU_MIX.get(), 1),
                        new ItemStack(NtmItems.NUGGET_PLUTONIUM.get(), 2),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 2))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.zirnoxthmeu").setup(100, zirnoxPower).setNameWrapper("purex.recycle").setGroup(autoZirnox, this)
                .inputItems(cooled(NtmItems.WASTE_THORIUM))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_U238.get(), 1),
                        new ItemStack(NtmItems.NUGGET_TH232.get(), 1),
                        new ItemStack(NtmItems.NUGGET_U233.get(), 2),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 2))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.zirnoxmox").setup(100, zirnoxPower).setNameWrapper("purex.recycle").setGroup(autoZirnox, this)
                .inputItems(cooled(NtmItems.WASTE_MOX))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_PU_MIX.get(), 1),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 1),
                        new ItemStack(NtmItems.NUGGET_U238.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 3))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.zirnoxmep").setup(100, zirnoxPower).setNameWrapper("purex.recycle").setGroup(autoZirnox, this)
                .inputItems(cooled(NtmItems.WASTE_PLUTONIUM))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_PU_MIX.get(), 2),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 3))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.zirnoxheu233").setup(100, zirnoxPower).setNameWrapper("purex.recycle").setGroup(autoZirnox, this)
                .inputItems(cooled(NtmItems.WASTE_U233))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_U235.get(), 1),
                        new ItemStack(NtmItems.NUGGET_NEPTUNIUM.get(), 1),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 3))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.zirnoxheu235").setup(100, zirnoxPower).setNameWrapper("purex.recycle").setGroup(autoZirnox, this)
                .inputItems(cooled(NtmItems.WASTE_U235))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_PU238.get(), 1),
                        new ItemStack(NtmItems.NUGGET_NEPTUNIUM.get(), 1),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 3))
                .setIconToFirstIngredient());

        /*
         * Das Original fuehrt den Abfallkrumen hier zweimal auf, mit eins und mit zwei. Das ist
         * kein Schreibfehler, den man stillschweigend zusammenzieht: die Ausgabefaecher werden
         * einzeln bedient, und drei zusammengezogen wuerden ein Fach weniger belegen. Bleibt so.
         */
        this.register((PUREXRecipe) new PUREXRecipe("purex.zirnoxles").setup(100, zirnoxPower).setNameWrapper("purex.recycle").setGroup(autoZirnox, this)
                .inputItems(cooled(NtmItems.WASTE_SCHRABIDIUM))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_BERYLLIUM.get(), 2),
                        new ItemStack(NtmItems.NUGGET_PU239.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 2))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.zirnoxzfbmox").setup(100, zirnoxPower).setNameWrapper("purex.recycle").setGroup(autoZirnox, this)
                .inputItems(cooled(NtmItems.WASTE_ZFB_MOX))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_ZIRCONIUM.get(), 3),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 1),
                        new ItemStack(NtmItems.NUGGET_PU_MIX.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 1))
                .setIconToFirstIngredient());

        /* Die Plattenreste des Forschungsreaktors. */
        String autoPlate = "autoswitch.plate";

        this.register((PUREXRecipe) new PUREXRecipe("purex.platemox").setup(100, platePower).setNameWrapper("purex.recycle").setGroup(autoPlate, this)
                .inputItems(cooled(NtmItems.WASTE_PLATE_MOX))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.POWDER_SR90_TINY.get(), 1),
                        new ItemStack(NtmItems.NUGGET_PU_MIX.get(), 3),
                        new ItemStack(NtmItems.POWDER_CS137_TINY.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 4))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.platepu238be").setup(100, platePower).setNameWrapper("purex.recycle").setGroup(autoPlate, this)
                .inputItems(cooled(NtmItems.WASTE_PLATE_PU238BE))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_BERYLLIUM.get(), 1),
                        new ItemStack(NtmItems.NUGGET_PU238.get(), 1),
                        new ItemStack(NtmItems.POWDER_COAL_TINY.get(), 2),
                        new ItemStack(NtmItems.NUGGET_LEAD.get(), 2))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.platepu239").setup(100, platePower).setNameWrapper("purex.recycle").setGroup(autoPlate, this)
                .inputItems(cooled(NtmItems.WASTE_PLATE_PU239))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_PU240.get(), 2),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 1),
                        new ItemStack(NtmItems.POWDER_CS137_TINY.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 5))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.platera226be").setup(100, platePower).setNameWrapper("purex.recycle").setGroup(autoPlate, this)
                .inputItems(cooled(NtmItems.WASTE_PLATE_RA226BE))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_BERYLLIUM.get(), 2),
                        new ItemStack(NtmItems.NUGGET_POLONIUM.get(), 2),
                        new ItemStack(NtmItems.POWDER_COAL_TINY.get(), 1),
                        new ItemStack(NtmItems.NUGGET_LEAD.get(), 1))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.platesa326").setup(100, platePower).setNameWrapper("purex.recycle").setGroup(autoPlate, this)
                .inputItems(cooled(NtmItems.WASTE_PLATE_SA326))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_SOLINIUM.get(), 1),
                        new ItemStack(NtmItems.POWDER_NEODYMIUM_TINY.get(), 1),
                        new ItemStack(NtmItems.NUGGET_TANTALIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 6))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.plateu233").setup(100, platePower).setNameWrapper("purex.recycle").setGroup(autoPlate, this)
                .inputItems(cooled(NtmItems.WASTE_PLATE_U233))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_U235.get(), 1),
                        new ItemStack(NtmItems.POWDER_I131_TINY.get(), 1),
                        new ItemStack(NtmItems.POWDER_SR90_TINY.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 6))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.plateu235").setup(100, platePower).setNameWrapper("purex.recycle").setGroup(autoPlate, this)
                .inputItems(cooled(NtmItems.WASTE_PLATE_U235))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_NEPTUNIUM.get(), 1),
                        new ItemStack(NtmItems.NUGGET_PU238.get(), 1),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 6))
                .setIconToFirstIngredient());

        /*
         * Die PWR-Reste. Fuenfzehn Sorten, alle mit derselben Dauer und demselben Strombedarf.
         * Die drei Schrabidium-Sorten geben Solinium, Australium und Euphemium her -- der einzige
         * Weg zu Euphemium, der nicht ueber den Beschuss laeuft.
         */
        String autoPWR = "autoswitch.pwr";

        this.register((PUREXRecipe) new PUREXRecipe("purex.pwrmeu").setup(100, pwrPower).setNameWrapper("purex.recycle").setGroup(autoPWR, this)
                .inputItems(pwrFuel(EnumPWRFuel.MEU))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_U238.get(), 3),
                        new ItemStack(NtmItems.NUGGET_PLUTONIUM.get(), 4),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 2),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 3))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pwrheu233").setup(100, pwrPower).setNameWrapper("purex.recycle").setGroup(autoPWR, this)
                .inputItems(pwrFuel(EnumPWRFuel.HEU233))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_U235.get(), 3),
                        new ItemStack(NtmItems.NUGGET_PU238.get(), 3),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 5))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pwrheu235").setup(100, pwrPower).setNameWrapper("purex.recycle").setGroup(autoPWR, this)
                .inputItems(pwrFuel(EnumPWRFuel.HEU235))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_NEPTUNIUM.get(), 3),
                        new ItemStack(NtmItems.NUGGET_PU238.get(), 3),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 5))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pwrmen").setup(100, pwrPower).setNameWrapper("purex.recycle").setGroup(autoPWR, this)
                .inputItems(pwrFuel(EnumPWRFuel.MEN))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_U238.get(), 3),
                        new ItemStack(NtmItems.NUGGET_PU239.get(), 4),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 2),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 3))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pwrhen237").setup(100, pwrPower).setNameWrapper("purex.recycle").setGroup(autoPWR, this)
                .inputItems(pwrFuel(EnumPWRFuel.HEN237))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_PU238.get(), 2),
                        new ItemStack(NtmItems.NUGGET_PU239.get(), 4),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 5))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pwrmox").setup(100, pwrPower).setNameWrapper("purex.recycle").setGroup(autoPWR, this)
                .inputItems(pwrFuel(EnumPWRFuel.MOX))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_U238.get(), 3),
                        new ItemStack(NtmItems.NUGGET_PU240.get(), 4),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 2),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 3))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pwrmep").setup(100, pwrPower).setNameWrapper("purex.recycle").setGroup(autoPWR, this)
                .inputItems(pwrFuel(EnumPWRFuel.MEP))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_LEAD.get(), 2),
                        new ItemStack(NtmItems.NUGGET_PU_MIX.get(), 4),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 2),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 3))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pwrhep239").setup(100, pwrPower).setNameWrapper("purex.recycle").setGroup(autoPWR, this)
                .inputItems(pwrFuel(EnumPWRFuel.HEP239))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_PU_MIX.get(), 2),
                        new ItemStack(NtmItems.NUGGET_PU240.get(), 4),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 5))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pwrhep241").setup(100, pwrPower).setNameWrapper("purex.recycle").setGroup(autoPWR, this)
                .inputItems(pwrFuel(EnumPWRFuel.HEP241))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_LEAD.get(), 3),
                        new ItemStack(NtmItems.NUGGET_ZIRCONIUM.get(), 2),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 6))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pwrmea").setup(100, pwrPower).setNameWrapper("purex.recycle").setGroup(autoPWR, this)
                .inputItems(pwrFuel(EnumPWRFuel.MEA))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_LEAD.get(), 3),
                        new ItemStack(NtmItems.NUGGET_ZIRCONIUM.get(), 2),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 6))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pwrhea242").setup(100, pwrPower).setNameWrapper("purex.recycle").setGroup(autoPWR, this)
                .inputItems(pwrFuel(EnumPWRFuel.HEA242))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_LEAD.get(), 3),
                        new ItemStack(NtmItems.NUGGET_ZIRCONIUM.get(), 2),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 6))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pwrhes326").setup(100, pwrPower).setNameWrapper("purex.recycle").setGroup(autoPWR, this)
                .inputItems(pwrFuel(EnumPWRFuel.HES326))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_SOLINIUM.get(), 3),
                        new ItemStack(NtmItems.NUGGET_LEAD.get(), 2),
                        new ItemStack(NtmItems.NUGGET_EUPHEMIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 6))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pwrhes327").setup(100, pwrPower).setNameWrapper("purex.recycle").setGroup(autoPWR, this)
                .inputItems(pwrFuel(EnumPWRFuel.HES327))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_AUSTRALIUM.get(), 4),
                        new ItemStack(NtmItems.NUGGET_LEAD.get(), 1),
                        new ItemStack(NtmItems.NUGGET_EUPHEMIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 6))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pwrbfbam").setup(100, pwrPower).setNameWrapper("purex.recycle").setGroup(autoPWR, this)
                .inputItems(pwrFuel(EnumPWRFuel.BFB_AM_MIX))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_AM_MIX.get(), 9),
                        new ItemStack(NtmItems.NUGGET_PU_MIX.get(), 2),
                        new ItemStack(NtmItems.NUGGET_BISMUTH.get(), 6),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 1))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.pwrbfpu241").setup(100, pwrPower).setNameWrapper("purex.recycle").setGroup(autoPWR, this)
                .inputItems(pwrFuel(EnumPWRFuel.BFB_PU241))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_PU241.get(), 9),
                        new ItemStack(NtmItems.NUGGET_PU_MIX.get(), 2),
                        new ItemStack(NtmItems.NUGGET_BISMUTH.get(), 6),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 1))
                .setIconToFirstIngredient());

        /*
         * Die Watz-Reste. Elf Sorten, alle mit derselben Dauer und demselben Strombedarf --
         * zehntausend HE je Tick, das Zehnfache der ZIRNOX-Rezepte. Jedes gibt einen Kubikmeter
         * Rotschlamm ab; wer den Watz betreibt, muss den auch loswerden.
         *
         * NICHT UEBERNOMMEN: purex.watznaqadah, das zwoelf Naquadria-Klumpen ausgibt. Die kommen
         * im Original aus einem anderen Mod und stehen im Port nicht zur Verfuegung.
         *
         * BEFUND: im Original stehen BEIDE Naquadah-Rezepte in einem Block, der nur betreten
         * wird, wenn ein anderer Mod Naquadria-Klumpen im Erzwoerterbuch eingetragen hat.
         * purex.watznaqadria braucht aber gar kein Naquadria -- es gibt Kobalt-60, Euphemium und
         * Abfall aus. Ohne den fremden Mod ist es im Original also grundlos nicht zu bekommen.
         * Der Port traegt es unbedingt ein.
         */
        String autoWatz = "autoswitch.watz";
        long watzPower = 10_000;

        this.register((PUREXRecipe) new PUREXRecipe("purex.watzschrab").setup(60, watzPower).setNameWrapper("purex.recycle").setGroup(autoWatz, this)
                .inputItems(pellet(EnumWatzType.SCHRABIDIUM))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_SOLINIUM.get(), 15),
                        new ItemStack(NtmItems.NUGGET_EUPHEMIUM.get(), 3),
                        new ItemStack(NtmItems.NUCLEAR_WASTE.get(), 2))
                .outputFluids(mud())
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.watzhes").setup(60, watzPower).setNameWrapper("purex.recycle").setGroup(autoWatz, this)
                .inputItems(pellet(EnumWatzType.HES))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_SOLINIUM.get(), 17),
                        new ItemStack(NtmItems.NUGGET_EUPHEMIUM.get(), 1),
                        new ItemStack(NtmItems.NUCLEAR_WASTE.get(), 2))
                .outputFluids(mud())
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.watzmes").setup(60, watzPower).setNameWrapper("purex.recycle").setGroup(autoWatz, this)
                .inputItems(pellet(EnumWatzType.MES))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_SOLINIUM.get(), 12),
                        new ItemStack(NtmItems.NUGGET_TANTALIUM.get(), 6),
                        new ItemStack(NtmItems.NUCLEAR_WASTE.get(), 2))
                .outputFluids(mud())
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.watzles").setup(60, watzPower).setNameWrapper("purex.recycle").setGroup(autoWatz, this)
                .inputItems(pellet(EnumWatzType.LES))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_SOLINIUM.get(), 9),
                        new ItemStack(NtmItems.NUGGET_TANTALIUM.get(), 9),
                        new ItemStack(NtmItems.NUCLEAR_WASTE.get(), 2))
                .outputFluids(mud())
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.watzhen").setup(60, watzPower).setNameWrapper("purex.recycle").setGroup(autoWatz, this)
                .inputItems(pellet(EnumWatzType.HEN))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_PU239.get(), 12),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 6),
                        new ItemStack(NtmItems.NUCLEAR_WASTE.get(), 2))
                .outputFluids(mud())
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.watzmeu").setup(60, watzPower).setNameWrapper("purex.recycle").setGroup(autoWatz, this)
                .inputItems(pellet(EnumWatzType.MEU))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_PU239.get(), 12),
                        new ItemStack(NtmItems.NUGGET_BISMUTH.get(), 6),
                        new ItemStack(NtmItems.NUCLEAR_WASTE.get(), 2))
                .outputFluids(mud())
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.watzmep").setup(60, watzPower).setNameWrapper("purex.recycle").setGroup(autoWatz, this)
                .inputItems(pellet(EnumWatzType.MEP))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_PU241.get(), 12),
                        new ItemStack(NtmItems.NUGGET_BISMUTH.get(), 6),
                        new ItemStack(NtmItems.NUCLEAR_WASTE.get(), 2))
                .outputFluids(mud())
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.watzlead").setup(60, watzPower).setNameWrapper("purex.recycle").setGroup(autoWatz, this)
                .inputItems(pellet(EnumWatzType.LEAD))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_LEAD.get(), 6),
                        new ItemStack(NtmItems.NUGGET_BISMUTH.get(), 12),
                        new ItemStack(NtmItems.NUCLEAR_WASTE.get(), 2))
                .outputFluids(mud())
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.watzboron").setup(60, watzPower).setNameWrapper("purex.recycle").setGroup(autoWatz, this)
                .inputItems(pellet(EnumWatzType.BORON))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.POWDER_COAL_TINY.get(), 12),
                        new ItemStack(NtmItems.NUGGET_CO60.get(), 6),
                        new ItemStack(NtmItems.NUCLEAR_WASTE.get(), 2))
                .outputFluids(mud())
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.watzdu").setup(60, watzPower).setNameWrapper("purex.recycle").setGroup(autoWatz, this)
                .inputItems(pellet(EnumWatzType.DU))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_POLONIUM.get(), 12),
                        new ItemStack(NtmItems.NUGGET_PU238.get(), 6),
                        new ItemStack(NtmItems.NUCLEAR_WASTE.get(), 2))
                .outputFluids(mud())
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.watznaqadria").setup(60, watzPower).setNameWrapper("purex.recycle").setGroup(autoWatz, this)
                .inputItems(pellet(EnumWatzType.NQR))
                .inputFluids(kerosene(), acid())
                .outputItems(new ItemStack(NtmItems.NUGGET_CO60.get(), 12),
                        new ItemStack(NtmItems.NUGGET_EUPHEMIUM.get(), 6),
                        new ItemStack(NtmItems.NUCLEAR_WASTE.get(), 2))
                .outputFluids(mud())
                .setIconToFirstIngredient());

        /* Die Traegheitsfusion: das abgebrannte Kuegelchen gibt seine Huelle zurueck, dazu ein
         * geladenes Pellet, Eisenstaub -- und Helium-4, genug fuer ein neues Kuegelchen samt
         * einem Viertel Ueberschuss. */
        this.register((PUREXRecipe) new PUREXRecipe("purex.icf").setup(300, 10_000).setNameWrapper("purex.recycle")
                .inputItems(new ComparableStack(NtmItems.ICF_PELLET_DEPLETED.get()))
                .outputItems(new ItemStack(NtmItems.ICF_PELLET_EMPTY.get(), 1),
                        new ItemStack(NtmItems.PELLET_CHARGED.get(), 1),
                        new ItemStack(NtmItems.POWDER_IRON.get(), 1))
                .outputFluids(new FluidStack(Fluids.HELIUM4, 1_250))
                .setIconToFirstIngredient());

        /* Schraranium -- das einzige Schrabidium-Rezept, dessen Eingang schon im Port ist. */
        this.register((PUREXRecipe) new PUREXRecipe("purex.schraranium").setup(200, 1_000).setNameWrapper("purex.schrab")
                .inputItems(new ComparableStack(NtmItems.INGOT_SCHRARANIUM.get()))
                .inputFluids(new FluidStack(Fluids.KEROSENE, 2_000), new FluidStack(Fluids.NITRIC_ACID, 1_000))
                .outputItems(new ItemStack(NtmItems.NUGGET_SCHRABIDIUM.get(), 3),
                        new ItemStack(NtmItems.NUGGET_URANIUM.get(), 3),
                        new ItemStack(NtmItems.NUGGET_NEPTUNIUM.get(), 2))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.schrabzirnox").setup(200, 50_000).setNameWrapper("purex.schrab").setGroup("autoswitch.schrab", this)
                .inputItems(cooled(NtmItems.WASTE_PLUTONIUM))
                .inputFluids(new FluidStack(Fluids.SOLVENT, 4_000), new FluidStack(Fluids.SCHRABIDIC, 250))
                .outputItems(new ItemStack(NtmItems.POWDER_SCHRABIDIUM.get(), 1),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 3),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 4))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.schrabpwr").setup(200, 50_000).setNameWrapper("purex.schrab").setGroup("autoswitch.schrab", this)
                .inputItems(pwrFuel(EnumPWRFuel.MEP))
                .inputFluids(new FluidStack(Fluids.SOLVENT, 4_000), new FluidStack(Fluids.SCHRABIDIC, 250))
                .outputItems(new ItemStack(NtmItems.POWDER_SCHRABIDIUM.get(), 1),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 3),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 4))
                .setIconToFirstIngredient());

        this.register((PUREXRecipe) new PUREXRecipe("purex.schrabmen").setup(200, 50_000).setNameWrapper("purex.schrab").setGroup("autoswitch.schrab", this)
                .inputItems(pwrFuel(EnumPWRFuel.MEN))
                .inputFluids(new FluidStack(Fluids.SOLVENT, 4_000), new FluidStack(Fluids.SCHRABIDIC, 250))
                .outputItems(new ItemStack(NtmItems.POWDER_SCHRABIDIUM.get(), 1),
                        new ItemStack(NtmItems.NUGGET_TECHNETIUM.get(), 3),
                        new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 4))
                .setIconToFirstIngredient());
    }

    /** Ein abgebrannter Chicago-Pile-Stab der angegebenen Sorte. */
    private static ComparableStack rod(EnumPileRod type) {
        return new ComparableStack(NtmItems.PILE_ROD.get(), 1, type.ordinal());
    }

    /** Ein abgebrannter PWR-Brennstab der angegebenen Sorte. */
    private static ComparableStack pwrFuel(EnumPWRFuel type) {
        return new ComparableStack(NtmItems.PWR_FUEL_DEPLETED.get(), 1, type.ordinal());
    }

    private static FluidStack sulfuric() { return new FluidStack(Fluids.SULFURIC_ACID, 100); }

    /** Ein abgebranntes Watz-Pellet der angegebenen Sorte. */
    private static ComparableStack pellet(EnumWatzType type) {
        return new ComparableStack(NtmItems.WATZ_PELLET_DEPLETED.get(), 1, type.ordinal());
    }

    private static FluidStack mud() { return new FluidStack(Fluids.WATZ, 1_000); }

    private static FluidStack kerosene() { return new FluidStack(Fluids.KEROSENE, 500); }
    private static FluidStack acid() { return new FluidStack(Fluids.NITRIC_ACID, 250); }
}
