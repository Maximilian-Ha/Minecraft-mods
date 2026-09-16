package com.zuxelus.energycontrol.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.ItemUpgrade.
 *
 * Aufwertungen fuer die Informationstafel. Im Original ein Gegenstand mit drei
 * Schadenswerten, hier drei Gegenstaende mit einer Art als Merkmal.
 */
public class ItemUpgrade extends Item {

    public enum UpgradeType {
        /** Vergroessert die Reichweite, in der Karten ihr Ziel noch erreichen. */
        RANGE,
        /** Erlaubt eigene Schrift- und Hintergrundfarben. */
        COLOR,
        /** Macht den Schirm beruehrungsempfindlich. */
        TOUCH
    }

    public final UpgradeType type;

    public ItemUpgrade(Properties properties, UpgradeType type) {
        super(properties);
        this.type = type;
    }

    public static boolean is(ItemStack stack, UpgradeType type) {
        return stack.getItem() instanceof ItemUpgrade upgrade && upgrade.type == type;
    }
}
