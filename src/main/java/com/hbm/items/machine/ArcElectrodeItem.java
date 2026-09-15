package com.hbm.items.machine;

import com.hbm.items.component.NtmDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemArcElectrode.
 *
 * Die Elektrode des grossen Lichtbogenofens. Sie zaehlt nicht Schaden, sondern Schmelzgaenge:
 * jeder Durchlauf erhoeht den Verbrauch um eins, und ist die Haltbarkeit erreicht, tauscht der
 * Ofen sie gegen die abgebrannte Fassung.
 *
 * ABWEICHUNG: das Original ist ein Gegenstand mit vier Untertypen, der Port hat vier eigene
 * Gegenstaende -- so, wie sie hier ohnehin schon registriert waren. Der Verbrauch steht in einer
 * Datenkomponente statt in einem NBT-Feld.
 */
public class ArcElectrodeItem extends Item {

    /** Wieviele Schmelzgaenge diese Elektrode aushaelt. */
    public final int maxDurability;

    public ArcElectrodeItem(int maxDurability, Properties properties) {
        super(properties.stacksTo(1));
        this.maxDurability = maxDurability;
    }

    public static int getDurability(ItemStack stack) {
        return stack.getOrDefault(NtmDataComponents.ELECTRODE_DURABILITY.get(), 0);
    }

    public static int getMaxDurability(ItemStack stack) {
        return stack.getItem() instanceof ArcElectrodeItem electrode ? electrode.maxDurability : 1;
    }

    /** Verbraucht einen Schmelzgang. Zurueck kommt, ob die Elektrode damit hinueber ist. */
    public static boolean damage(ItemStack stack) {
        if(!(stack.getItem() instanceof ArcElectrodeItem)) return false;

        int durability = getDurability(stack) + 1;
        stack.set(NtmDataComponents.ELECTRODE_DURABILITY.get(), durability);

        return durability >= getMaxDurability(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getDurability(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int max = getMaxDurability(stack);
        return Math.round(13F - (float) getDurability(stack) * 13F / max);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float left = Math.max(0F, 1F - (float) getDurability(stack) / getMaxDurability(stack));
        return net.minecraft.util.Mth.hsvToRgb(left / 3F, 1F, 1F);
    }
}
