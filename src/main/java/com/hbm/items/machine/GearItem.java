package com.hbm.items.machine;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.EnumMultiItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemGear (Registryname "gear_large").
 *
 * Das grosse Zahnrad des Stirlingmotors. Im Original ein Item mit Untertypen
 * ueber die Metadaten; getUnlocalizedName haengte bei Metadaten 1 ein "_steel" an,
 * alles andere blieb "gear_large".
 *
 * Im Port laufen Untertypen ueber NtmDataComponents.META bzw. MetaHelper --
 * das Gegenstueck zu ItemEnumMulti heisst hier EnumMultiItem, benutzt wie bei
 * PistonsItem und RTGPelletItem.
 *
 * multiTexture ist bewusst false: das Original hat nur eine einzige Textur
 * (textures/items/gear_large.png) fuer alle Untertypen, weil das Zahnrad im
 * Inventar ueber ItemRenderLibrary als 3D-Modell gezeichnet wurde. Eine
 * Textur pro Untertyp nachzumalen waere Erfindung.
 *
 * Welcher Untertyp zu welcher Maschine gehoert, sagt TileEntityStirling.getGeatMeta():
 * 0 fuer machine_stirling, 2 fuer machine_stirling_creative, sonst 1
 * (also machine_stirling_steel).
 */
public class GearItem extends EnumMultiItem {

    public GearItem(Properties properties) {
        super(properties, GearType.class, true, false);
    }

    /**
     * Das Original zeigt im Kreativreiter nur die Metadaten 0 und 1
     * (ItemGear.getSubItems laeuft ueber i &lt; 2). Das Zahnrad des
     * Kreativ-Stirlingmotors gab es dort nie zu holen, also bleibt es auch
     * hier aussen vor.
     */
    @Override
    public void getSubItems(Item item, List<ItemStack> stacks) {
        for(int i = 0; i < 2; i++) {
            stacks.add(MetaHelper.newStack(item, 1, i));
        }
    }

    /**
     * Reihenfolge = Metadaten des Originals.
     * IRON     (0): Zahnrad des einfachen Stirlingmotors, Eisenplatten + Kupferbarren
     * STEEL    (1): Zahnrad des Stahl-Stirlingmotors, Stahlplatten + Titanbarren
     * CREATIVE (2): Zahnrad des Kreativ-Stirlingmotors, im Original ohne Rezept
     */
    public enum GearType {
        IRON,
        STEEL,
        CREATIVE
    }
}
