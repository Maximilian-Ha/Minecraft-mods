package com.hbm.blockentity.machine.rbmk;

import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.IRBMKLoadable.
 *
 * Eine Saeule, die der Ladekran be- und entladen kann.
 */
public interface IRBMKLoadable {

    /** Ob der gegebene Stapel in die Saeule passt. */
    boolean canLoad(ItemStack toLoad);

    /** Legt den Stapel ab. Vorher muss {@link #canLoad(ItemStack)} gefragt worden sein. */
    void load(ItemStack toLoad);

    /** Ob die Saeule etwas zum Entladen enthaelt. */
    boolean canUnload();

    /** Der Stapel, der als naechstes entladen wird. */
    ItemStack provideNext();

    /** Nimmt den naechsten Stapel heraus. */
    void unload();
}
