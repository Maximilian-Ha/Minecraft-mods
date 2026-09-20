package com.hbm.handler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.items.NtmItems;
import com.hbm.items.armor.ItemModCladding;
import com.hbm.lib.ModEffect;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.ShadyUtil;
import com.hbm.util.TagsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class HazmatRegistry {
    public static void initDefault() {

        //assuming coefficient of 10
        //real coefficient turned out to be 5
        //oops

        double helmet = 0.2D;
        double chest = 0.4D;
        double legs = 0.3D;
        double boots = 0.1D;

        double iron = 0.0225D; // 5%
        double gold = 0.0225D; // 5%

        HazmatRegistry.registerHazmat(Items.GOLDEN_HELMET, gold * helmet);
        HazmatRegistry.registerHazmat(Items.GOLDEN_CHESTPLATE, gold * chest);
        HazmatRegistry.registerHazmat(Items.GOLDEN_LEGGINGS, gold * legs);
        HazmatRegistry.registerHazmat(Items.GOLDEN_BOOTS, gold * boots);

        HazmatRegistry.registerHazmat(Items.IRON_HELMET, iron * helmet);
        HazmatRegistry.registerHazmat(Items.IRON_CHESTPLATE, iron * chest);
        HazmatRegistry.registerHazmat(Items.IRON_LEGGINGS, iron * legs);
        HazmatRegistry.registerHazmat(Items.IRON_BOOTS, iron * boots);

        /* Die Schutzanzuege. Die Prozentangaben stammen aus dem Original und beziehen
         * sich auf den ganzen Satz; die Zahl davor ist der Wert, der mit dem Anteil des
         * jeweiligen Teils multipliziert wird. */
        double hazYellow = 0.6D;    // 50%
        double hazRed = 1.0D;       // 90%
        double hazGrey = 2.0D;      // 99%
        double paa = 1.7D;          // 97%

        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_HELMET.get(), hazYellow * helmet);
        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_PLATE.get(), hazYellow * chest);
        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_LEGS.get(), hazYellow * legs);
        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_BOOTS.get(), hazYellow * boots);

        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_HELMET_RED.get(), hazRed * helmet);
        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_PLATE_RED.get(), hazRed * chest);
        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_LEGS_RED.get(), hazRed * legs);
        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_BOOTS_RED.get(), hazRed * boots);

        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_HELMET_GREY.get(), hazGrey * helmet);
        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_PLATE_GREY.get(), hazGrey * chest);
        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_LEGS_GREY.get(), hazGrey * legs);
        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_BOOTS_GREY.get(), hazGrey * boots);

        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_PAA_HELMET.get(), paa * helmet);
        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_PAA_PLATE.get(), paa * chest);
        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_PAA_LEGS.get(), paa * legs);
        HazmatRegistry.registerHazmat(NtmItems.HAZMAT_PAA_BOOTS.get(), paa * boots);

        /* Der Bleianzug der Liquidatoren. 2,4 heisst im Original 99,6 Prozent -- der beste
         * Strahlenschutz ausserhalb des HEV-Anzugs. */
        /* Der Kombinationsstahl schirmt mit, ohne ein Schutzanzug zu sein: 95 Prozent.
         * Die PAA-Ruestung teilt sich ihren Wert mit dem PAA-Schutzanzug -- 97 Prozent --,
         * und zwar ohne Helm, weil sie keinen hat. */
        double cmb = 1.3D;          // 95%
        HazmatRegistry.registerHazmat(NtmItems.CMB_HELMET.get(), cmb * helmet);
        HazmatRegistry.registerHazmat(NtmItems.CMB_PLATE.get(), cmb * chest);
        HazmatRegistry.registerHazmat(NtmItems.CMB_LEGS.get(), cmb * legs);
        HazmatRegistry.registerHazmat(NtmItems.CMB_BOOTS.get(), cmb * boots);

        HazmatRegistry.registerHazmat(NtmItems.PAA_PLATE.get(), paa * chest);
        HazmatRegistry.registerHazmat(NtmItems.PAA_LEGS.get(), paa * legs);
        HazmatRegistry.registerHazmat(NtmItems.PAA_BOOTS.get(), paa * boots);

        /* Die T-51 nimmt dem Traeger neunzig Prozent der Strahlung ab -- setRadResist(1.0)
         * im Original. */
        double t51 = 1.0D;          // 90%
        HazmatRegistry.registerHazmat(NtmItems.T51_HELMET.get(), t51 * helmet);
        HazmatRegistry.registerHazmat(NtmItems.T51_PLATE.get(), t51 * chest);
        HazmatRegistry.registerHazmat(NtmItems.T51_LEGS.get(), t51 * legs);
        HazmatRegistry.registerHazmat(NtmItems.T51_BOOTS.get(), t51 * boots);

        /* Der AJR-Anzug und seine orangene Spielart: 1,3 auf den ganzen Satz, im Original
         * als Kommentar "95%". Beide Garnituren tragen denselben Wert. */
        double ajr = 1.3D;          // 95%
        HazmatRegistry.registerHazmat(NtmItems.AJR_HELMET.get(), ajr * helmet);
        HazmatRegistry.registerHazmat(NtmItems.AJR_PLATE.get(), ajr * chest);
        HazmatRegistry.registerHazmat(NtmItems.AJR_LEGS.get(), ajr * legs);
        HazmatRegistry.registerHazmat(NtmItems.AJR_BOOTS.get(), ajr * boots);
        HazmatRegistry.registerHazmat(NtmItems.AJRO_HELMET.get(), ajr * helmet);
        HazmatRegistry.registerHazmat(NtmItems.AJRO_PLATE.get(), ajr * chest);
        HazmatRegistry.registerHazmat(NtmItems.AJRO_LEGS.get(), ajr * legs);
        HazmatRegistry.registerHazmat(NtmItems.AJRO_BOOTS.get(), ajr * boots);

        /* Die Taurun-Ruestung haelt nur ein Viertel der Strahlung ab -- setRadResist(0.125)
         * im Original, der niedrigste Wert aller Wellenfront-Ruestungen. */
        double taurun = 0.125D;     // 25%
        HazmatRegistry.registerHazmat(NtmItems.TAURUN_HELMET.get(), taurun * helmet);
        HazmatRegistry.registerHazmat(NtmItems.TAURUN_PLATE.get(), taurun * chest);
        HazmatRegistry.registerHazmat(NtmItems.TAURUN_LEGS.get(), taurun * legs);
        HazmatRegistry.registerHazmat(NtmItems.TAURUN_BOOTS.get(), taurun * boots);

        /* Der Fau-Anzug: 4,0 auf den ganzen Satz, im Original als Kommentar "99.99%". */
        double fau = 4D;            // 99,99%
        HazmatRegistry.registerHazmat(NtmItems.FAU_HELMET.get(), fau * helmet);
        HazmatRegistry.registerHazmat(NtmItems.FAU_PLATE.get(), fau * chest);
        HazmatRegistry.registerHazmat(NtmItems.FAU_LEGS.get(), fau * legs);
        HazmatRegistry.registerHazmat(NtmItems.FAU_BOOTS.get(), fau * boots);

        /* Der Grabenmeister: 1,0 auf den ganzen Satz, im Original als Kommentar "90%". */
        double trench = 1.0D;       // 90%
        HazmatRegistry.registerHazmat(NtmItems.TRENCHMASTER_HELMET.get(), trench * helmet);
        HazmatRegistry.registerHazmat(NtmItems.TRENCHMASTER_PLATE.get(), trench * chest);
        HazmatRegistry.registerHazmat(NtmItems.TRENCHMASTER_LEGS.get(), trench * legs);
        HazmatRegistry.registerHazmat(NtmItems.TRENCHMASTER_BOOTS.get(), trench * boots);

        /* Der Umgebungsanzug: 1,0 auf den ganzen Satz, im Original als Kommentar "90%". */
        double env = 1.0D;          // 90%
        HazmatRegistry.registerHazmat(NtmItems.ENVSUIT_HELMET.get(), env * helmet);
        HazmatRegistry.registerHazmat(NtmItems.ENVSUIT_PLATE.get(), env * chest);
        HazmatRegistry.registerHazmat(NtmItems.ENVSUIT_LEGS.get(), env * legs);
        HazmatRegistry.registerHazmat(NtmItems.ENVSUIT_BOOTS.get(), env * boots);

        /* Der Blackjack-Anzug: 1,0 auf den ganzen Satz, im Original als Kommentar "90%".
         * Er traegt KEINE Gefahrenklasse -- setHazardClass fehlt dort, anders als bei allen
         * anderen Wellenfront-Ruestungen. Deshalb steht er nicht in ArmorUtil. */
        double blackjack = 1.0D;    // 90%
        HazmatRegistry.registerHazmat(NtmItems.BJ_HELMET.get(), blackjack * helmet);
        HazmatRegistry.registerHazmat(NtmItems.BJ_PLATE.get(), blackjack * chest);
        HazmatRegistry.registerHazmat(NtmItems.BJ_PLATE_JETPACK.get(), blackjack * chest);
        HazmatRegistry.registerHazmat(NtmItems.BJ_LEGS.get(), blackjack * legs);
        HazmatRegistry.registerHazmat(NtmItems.BJ_BOOTS.get(), blackjack * boots);

        /* Euphemium: Faktor 10, im Original mit dem Vermerk "<100%" -- der Satz haelt viel
         * ab, aber nicht alles. */
        double euph = 10D;
        HazmatRegistry.registerHazmat(NtmItems.EUPHEMIUM_HELMET.get(), euph * helmet);
        HazmatRegistry.registerHazmat(NtmItems.EUPHEMIUM_PLATE.get(), euph * chest);
        HazmatRegistry.registerHazmat(NtmItems.EUPHEMIUM_LEGS.get(), euph * legs);
        HazmatRegistry.registerHazmat(NtmItems.EUPHEMIUM_BOOTS.get(), euph * boots);

        double liquidator = 2.4D;   // 99,6%
        HazmatRegistry.registerHazmat(NtmItems.LIQUIDATOR_HELMET.get(), liquidator * helmet);
        HazmatRegistry.registerHazmat(NtmItems.LIQUIDATOR_PLATE.get(), liquidator * chest);
        HazmatRegistry.registerHazmat(NtmItems.LIQUIDATOR_LEGS.get(), liquidator * legs);
        HazmatRegistry.registerHazmat(NtmItems.LIQUIDATOR_BOOTS.get(), liquidator * boots);

        /* Der HEV-Anzug: 2,3 auf den ganzen Satz, im Original als Kommentar "99,5%".
         * Die Aufteilung auf die vier Teile ist dieselbe wie ueberall hier. */
        double hev = 2.3D;

        HazmatRegistry.registerHazmat(NtmItems.HEV_HELMET.get(), hev * helmet);
        HazmatRegistry.registerHazmat(NtmItems.HEV_PLATE.get(), hev * chest);
        HazmatRegistry.registerHazmat(NtmItems.HEV_LEGS.get(), hev * legs);
        HazmatRegistry.registerHazmat(NtmItems.HEV_BOOTS.get(), hev * boots);

        /* Die beiden Masken schirmen auch ein wenig ab, ganz ohne Anzug. */
        HazmatRegistry.registerHazmat(NtmItems.GAS_MASK.get(), 0.07D);
        HazmatRegistry.registerHazmat(NtmItems.GAS_MASK_M65.get(), 0.095D);
    }

    private static final HashMap<Item, Double> ENTRIES = new HashMap<>();

    private static void registerHazmat(Item item, double resistance) {
        ENTRIES.put(item, resistance);
    }

    public static void addInfo(List<Component> components, Level level, ItemStack stack) {
        double rad = ((int)(HazmatRegistry.getResistance(level, stack) * 1000)) / 1000D;
        if (rad > 0) components.add(Component.translatable("trait.radResistance", rad).withStyle(ChatFormatting.YELLOW));
    }

    public static double getResistance(Level level, ItemStack stack) {

        if (stack.isEmpty()) return 0;

        double cladding = getCladding(level, stack);

        Double f = ENTRIES.get(stack.getItem());

        if (f != null)
            return f + cladding;

        return cladding;
    }

    public static double getCladding(Level level, ItemStack stack) {

        float claddingRes = TagsUtil.getCustomData(stack).getFloat("hfr_cladding");

        if (claddingRes > 0)
            return claddingRes;

        if (ArmorModHandler.hasMods(stack)) {

            ItemStack[] mods = ArmorModHandler.pryMods(level, stack);
            ItemStack cladding = mods[ArmorModHandler.CLADDING];

            if (!cladding.isEmpty() && cladding.getItem() instanceof ItemModCladding modCladding) {
                return modCladding.rad;
            }
        }

        return 0;
    }

    public static float getResistance(Player player) {

        float res = 0.0F;

        if (player.getUUID().toString().equals(ShadyUtil.Pu_238)) res += 0.4F;

        for (int i = 0; i < 4; i++) {
            res += (float) getResistance(player.level(), player.getInventory().armor.get(i));
        }

        if (player.hasEffect(ModEffect.RADX))
            res += 0.2F;

        return res;
    }

    public static final Gson gson = new Gson();

    public static void registerHazmats() {
        File folder = NuclearTechMod.configHbmDir;

        File config = new File(folder.getAbsolutePath() + File.separatorChar + "hbmRadResist.json");
        File template = new File(folder.getAbsolutePath() + File.separatorChar + "_hbmRadResist.json");

        initDefault();

        if (!config.exists()) {
            writeDefault(template);
        } else {
            HashMap<Item, Double> conf = readConfig(config);

            if (conf != null) {
                ENTRIES.clear();
                ENTRIES.putAll(conf);
            }
        }
    }

    private static void writeDefault(File file) {

        try {
            JsonWriter writer = new JsonWriter(new FileWriter(file));
            writer.setIndent("  ");
            writer.beginObject();
            writer.name("comment").value("Template file, remove the underscore ('_') from the name to enable the config.");
            writer.name("entries").beginArray();

            for (Entry<Item, Double> entry : ENTRIES.entrySet()) {
                writer.beginObject();
                writer.name("item").value(BuiltInRegistries.ITEM.getKey(entry.getKey()).toString());
                writer.name("resistance").value(entry.getValue());
                writer.endObject();
            }

            writer.endArray();
            writer.endObject();
            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<Item, Double> readConfig(File config) {

        try {
            JsonObject json = gson.fromJson(new FileReader(config), JsonObject.class);
            JsonArray array = json.get("entries").getAsJsonArray();
            HashMap<Item, Double> conf = new HashMap<>();

            for (JsonElement element : array) {
                JsonObject object = (JsonObject) element;

                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(object.get("item").getAsString()));
                double resistance = object.get("resistance").getAsDouble();
                conf.put(item, resistance);
            }

            return conf;

        } catch(IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
