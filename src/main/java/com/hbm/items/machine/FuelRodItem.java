package com.hbm.items.machine;

import com.hbm.items.component.NtmDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemFuelRod.
 *
 * Ein Brennstoff, der sich verbraucht. Die aufgelaufenen Reaktionen stehen im Gegenstand;
 * erreichen sie die Lebensdauer, ist er abgebrannt.
 *
 * ABWEICHUNG: das Original legt den Zaehler im NBT unter "life" ab. In 1.21 gibt es dafuer
 * Datenkomponenten -- der Zaehler steht in FUEL_ROD_LIFE. Die Haltbarkeitsleiste zeichnet
 * Minecraft selbst, sobald getBarWidth/isBarVisible etwas liefern.
 */
public class FuelRodItem extends Item {

    /** Wie viele Reaktionen der Brennstoff aushaelt. */
    public final int lifeTime;

    public FuelRodItem(int lifeTime, Properties properties) {
        super(properties);
        this.lifeTime = lifeTime;
    }

    public static void setLifeTime(ItemStack stack, int time) {
        stack.set(NtmDataComponents.FUEL_ROD_LIFE.get(), Math.max(0, time));
    }

    public static int getLifeTime(ItemStack stack) {
        return stack.getOrDefault(NtmDataComponents.FUEL_ROD_LIFE.get(), 0);
    }

    /** Anteil des Verbrauchs, zwischen 0 und 1. */
    public static double getUsage(ItemStack stack) {
        if(!(stack.getItem() instanceof FuelRodItem rod) || rod.lifeTime <= 0) return 0D;
        return Math.min(1D, (double) getLifeTime(stack) / (double) rod.lifeTime);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getUsage(stack) > 0D;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(13D * (1D - getUsage(stack)));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        /* Von gruen nach rot, wie die Haltbarkeitsleiste eines Werkzeugs. */
        float left = (float) (1D - getUsage(stack));
        return net.minecraft.util.Mth.hsvToRgb(left / 3.0F, 1.0F, 1.0F);
    }
}
