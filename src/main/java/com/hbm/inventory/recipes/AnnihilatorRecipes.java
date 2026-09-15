package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.config.NtmConfig;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.BlueprintsItem;
import com.hbm.items.machine.DriveItem.DriveType;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.AnnihilatorRecipes.
 *
 * Die Schwellen des Annihilators: wer genug von einer Sorte vernichtet hat, bekommt eine
 * Blaupause. Das ist der EINZIGE Weg im Original, an die 528er-Blaupausen zu kommen -- die
 * Rezepte hinter ihnen stehen sonst hinter verschlossener Tuer.
 *
 * SECHS VON DREIZEHN POOLS SIND HIER, und das ist gemessen, nicht geschaetzt: das Original
 * verteilt sechzig Rezepte auf dreizehn 528er-Pools, und der Port hat davon siebzehn Rezepte in
 * sechs Pools. Die uebrigen sieben -- arty, chip_quantum, hardplastic, plastic, rubber, soyuz,
 * steel -- haetten im Port keinen einzigen Eintrag; ihre Blaupause waere ein Zettel ueber einen
 * leeren Vorrat. Wer die zugehoerigen Rezepte nachreicht, traegt die Schwelle hier nach; die
 * Zahlen des Originals stehen jeweils im Kommentar daneben.
 *
 * ALLES HAENGT AM 528er-SCHALTER, wie im Original: ist er aus, gibt es keine Schwellen und der
 * Annihilator vernichtet bloss.
 */
public class AnnihilatorRecipes extends SerializableRecipe {

    public static final Map<Object, AnnihilatorRecipe> recipes = new HashMap<>();

    @Override
    public void registerDefaults() {

        if(!recipes.isEmpty()) return;
        if(!NtmConfig.enable528()) return;

        /* gascent: die Gaszentrifuge, Runde 115. */
        milestone(NtmItems.BILLET_URANIUM.get(), 256, "gascent");
        /* ferrouranium: der RBMK. */
        milestone(NtmItems.INGOT_FERROURANIUM.get(), 1_024, "ferrouranium");
        /* chlorophyte: MHDT und ICF. */
        milestone(NtmItems.POWDER_CHLOROPHYTE.get(), 1_024, "chlorophyte");
        /* tcalloy: Fusion und Watz. Im Port ist ANY_RESISTANTALLOY der Schnellarbeitsstahl. */
        milestone(NtmItems.INGOT_DURA_STEEL.get(), 1_024, "tcalloy");
        /* bmg: die Geschuetztuerme. */
        milestone(new ComparableStack(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 1, Ammo.BMG50_FMJ)), 256, "bmg");
        /* controller: die Kernwaffen. */
        milestone(NtmItems.CIRCUIT_CONTROL_UNIT.get(), 128, "controller");

        /* Runde 130: die Praezisionsmontage fuellt drei der Vorraete, die hier bis dahin leer
         * geblieben waren -- ihre Rezepte liegen genau darin. */
        /* chip_bismoid: der vielseitige integrierte Schaltkreis. */
        milestone(NtmItems.NUGGET_BISMUTH.get(), 128, "chip_bismoid");
        /* chip_quantum: der Quantenprozessor. */
        milestone(NtmItems.PELLET_CHARGED.get(), 1_024, "chip_quantum");
        /* strontium: die Atomuhr. */
        milestone(NtmItems.POWDER_STRONTIUM.get(), 256, "strontium");

        /*
         * NICHT PORTIERT, weil der Port keine Rezepte in diesen Poolen hat:
         *   steel        256  STEEL.ingot()
         *   chip         256  SI.billet()   -- die Praezisionsmontage hat das Rezept dafuer,
         *                                      aber BILLET_SILICON gibt es im Port nicht
         *   plastic      512  ANY_PLASTIC.ingot()
         *   rubber       512  RUBBER.ingot()
         *   hardplastic 1024  ANY_HARDPLASTIC.ingot()
         *   soyuz         64  drive FLASH_FLIGHTSIM
         *   arty         128  ammo_arty
         */
    }

    /** Eine Schwelle: so viele Stueck vernichtet, dann diese Blaupause. */
    private static void milestone(Object key, long amount, String pool) {
        recipes.put(key, new AnnihilatorRecipe(new Milestone(BigInteger.valueOf(amount), BlueprintsItem.make(GenericRecipes.POOL_PREFIX_528 + pool))));
    }

    /**
     * Die hoechste Schwelle, die mit diesem Zuwachs neu erreicht wurde.
     *
     * Ist prevAmount null, zahlt jede erreichte Schwelle aus -- damit fordert das Anforderungsfach
     * eine Blaupause noch einmal an. Sonst zaehlt nur, was vorher NICHT erreicht war.
     */
    public static ItemStack getHighestPayoutFromKey(Object key, BigInteger prevAmount, BigInteger currentAmount) {

        AnnihilatorRecipe recipe = recipes.get(key);
        if(recipe == null) return ItemStack.EMPTY;

        BigInteger highestYet = BigInteger.ZERO;
        ItemStack highestPayout = ItemStack.EMPTY;

        for(Milestone milestone : recipe.milestones) {
            if(prevAmount != null && prevAmount.compareTo(milestone.amount) >= 0) continue;
            if(currentAmount.compareTo(highestYet) <= 0) continue;
            if(currentAmount.compareTo(milestone.amount) >= 0) {
                highestYet = milestone.amount;
                highestPayout = milestone.payout;
            }
        }

        return highestPayout.copy();
    }

    public static ItemStack getHighestPayoutFromFluid(FluidType fluid, BigInteger prevAmount, BigInteger currentAmount) {
        return getHighestPayoutFromKey(fluid, prevAmount, currentAmount);
    }

    @Override public String getFileName() { return "hbmAnnihilator.json"; }
    @Override public Object getRecipeObject() { return recipes; }
    @Override public void deleteRecipes() { recipes.clear(); }
    /* Ohne den 528er-Schalter gibt es keine Schwellen -- und das ist kein Fehler. */
    @Override public boolean allowEmptyRecipeList() { return true; }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = (JsonObject) recipe;
        JsonObject key = obj.get("key").getAsJsonObject();
        String keyType = key.get("type").getAsString();

        Object keyObject = null;
        if("item".equals(keyType)) keyObject = BuiltInRegistries.ITEM.get(ResourceLocation.parse(key.get("item").getAsString()));
        if("comp".equals(keyType)) keyObject = new ComparableStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(key.get("item").getAsString())), 1, key.get("meta").getAsInt());
        if("fluid".equals(keyType)) keyObject = Fluids.fromName(key.get("fluid").getAsString());
        /* "dict" faellt weg -- der Port hat kein Erzwoerterbuch. */

        if(keyObject == null) return;

        List<Milestone> milestones = new ArrayList<>();
        for(JsonElement e : obj.get("milestones").getAsJsonArray()) {
            JsonObject m = e.getAsJsonObject();
            milestones.add(new Milestone(m.get("amount").getAsBigInteger(), readItemStack(m.get("payout").getAsJsonArray())));
        }

        recipes.put(keyObject, new AnnihilatorRecipe(milestones));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        Entry<Object, AnnihilatorRecipe> entry = (Entry<Object, AnnihilatorRecipe>) recipe;
        Object key = entry.getKey();

        writer.name("key").beginObject();
        if(key instanceof Item item) {
            writer.name("type").value("item");
            writer.name("item").value(BuiltInRegistries.ITEM.getKey(item).toString());
        } else if(key instanceof ComparableStack comp) {
            writer.name("type").value("comp");
            writer.name("item").value(BuiltInRegistries.ITEM.getKey(comp.item).toString());
            writer.name("meta").value(comp.meta);
        } else if(key instanceof FluidType type) {
            writer.name("type").value("fluid");
            writer.name("fluid").value(type.getUnlocalizedName());
        }
        writer.endObject();

        writer.name("milestones").beginArray();
        for(Milestone milestone : entry.getValue().milestones) {
            writer.beginObject();
            writer.name("amount").value(milestone.amount);
            writer.name("payout");
            writeItemStack(milestone.payout, writer);
            writer.endObject();
        }
        writer.endArray();
    }

    /** Eine Schwelle samt Auszahlung. Im Original ein Pair. */
    public record Milestone(BigInteger amount, ItemStack payout) { }

    public static class AnnihilatorRecipe {

        public final List<Milestone> milestones;

        public AnnihilatorRecipe(Milestone... milestones) {
            this.milestones = List.of(milestones);
        }

        public AnnihilatorRecipe(List<Milestone> milestones) {
            this.milestones = milestones;
        }
    }
}
