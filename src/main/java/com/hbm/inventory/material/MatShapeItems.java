package com.hbm.inventory.material;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.CastPlateItem;
import com.hbm.items.NtmItems;
import com.hbm.items.WireDenseItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Die Bruecke zwischen dem Materialsystem und den beiden Formen, die im Port als
 * Untertyp-Gegenstand vorliegen: Gussplatte und dichter Draht.
 *
 * WARUM ES DIESE KLASSE BRAUCHT
 *
 * Alle anderen Formen findet Mats ueber Tags -- ein Stahlbarren heisst ingot_steel und haengt
 * in c:ingots/steel. Gussplatte und dichter Draht sind im Port aber je EIN Gegenstand mit
 * Metadaten. Ein Tag kann nur ganze Gegenstaende aufnehmen, nicht einzelne Metadaten; c:
 * triple_plates/iron muesste also cast_plate insgesamt enthalten und wuerde damit auch die
 * Goldplatte treffen. Deshalb laufen diese beiden Formen ueber ausdrueckliche Eintraege
 * (ComparableStack kennt die Metadaten) statt ueber Tags.
 *
 * Beim dichten Draht steht die Material-ID ohnehin schon in der Aufzaehlung; bei der Gussplatte
 * ist der Schluessel der Tag-Name des Materials, bis auf fuenf Ausnahmen, die hier stehen.
 */
public class MatShapeItems {

    /** Wo der Schluessel der Gussplatte nicht dem Tag-Namen des Materials entspricht. */
    private static final Map<String, String> CAST_PLATE_OVERRIDES = Map.of(
            "aluminium", "aluminum",
            "desh", "workers_alloy",
            "tcalloy", "tc_alloy",
            "cdalloy", "cd_alloy",
            "combine_steel", "cmb_steel");

    private static Map<CastPlateItem.Type, NTMMaterial> castPlateMats;
    private static Map<WireDenseItem.Type, NTMMaterial> wireDenseMats;

    public static Map<CastPlateItem.Type, NTMMaterial> getCastPlateMats() {

        if(castPlateMats == null) {
            castPlateMats = new LinkedHashMap<>();

            for(CastPlateItem.Type type : CastPlateItem.Type.values()) {
                NTMMaterial mat = byTagName(CAST_PLATE_OVERRIDES.getOrDefault(type.key, type.key));
                if(mat != null) castPlateMats.put(type, mat);
            }
        }

        return castPlateMats;
    }

    public static Map<WireDenseItem.Type, NTMMaterial> getWireDenseMats() {

        if(wireDenseMats == null) {
            wireDenseMats = new LinkedHashMap<>();

            // Die Metadaten des dichten Drahts SIND die Material-IDs
            for(WireDenseItem.Type type : WireDenseItem.Type.values()) {
                NTMMaterial mat = Mats.matById.get(type.meta);
                if(mat != null) wireDenseMats.put(type, mat);
            }
        }

        return wireDenseMats;
    }

    private static @Nullable NTMMaterial byTagName(String tagName) {
        for(NTMMaterial mat : Mats.orderedList) {
            if(tagName.equals(mat.tagName)) return mat;
        }
        return null;
    }

    /** Die Gussplatte aus diesem Material, oder leer. */
    public static ItemStack castPlate(NTMMaterial mat, boolean welded) {
        for(Map.Entry<CastPlateItem.Type, NTMMaterial> entry : getCastPlateMats().entrySet()) {
            if(entry.getValue() == mat && entry.getKey().isAllowed(welded)) {
                return MetaHelper.newStack(welded ? NtmItems.CAST_PLATE_WELDED.get() : NtmItems.CAST_PLATE.get(), 1, entry.getKey().ordinal());
            }
        }
        return ItemStack.EMPTY;
    }

    /** Der dichte Draht aus diesem Material, oder leer. */
    public static ItemStack wireDense(NTMMaterial mat) {
        for(Map.Entry<WireDenseItem.Type, NTMMaterial> entry : getWireDenseMats().entrySet()) {
            if(entry.getValue() == mat) {
                return MetaHelper.newStack(NtmItems.WIRE_DENSE.get(), 1, entry.getKey().meta);
            }
        }
        return ItemStack.EMPTY;
    }

    /** Die gegossene Platte aus diesem Material -- als Funktion fuer die Formen. */
    public static ItemStack castPlateOf(NTMMaterial mat) { return castPlate(mat, false); }

    /** Der dichte Draht aus diesem Material -- als Funktion fuer die Formen. */
    public static ItemStack wireDenseOf(NTMMaterial mat) { return wireDense(mat); }

    /** Wieviele Zuordnungen zusammengekommen sind -- fuer die Meldung beim Start. */
    public static String describe() {
        return getCastPlateMats().size() + " Gussplatten, " + getWireDenseMats().size() + " dichte Draehte";
    }

    private MatShapeItems() { }
}
