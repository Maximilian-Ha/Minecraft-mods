package com.hbm.items.weapon.sedna.mags;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.machine.FluidIconItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.particle.SpentCasing;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mags.MagazineFluid.
 *
 * Ein Tank, der jedes Fluid aufnimmt. Anders als MagazineLiquidEngine merkt er sich, WAS
 * drin ist -- die Sorte steht neben der Menge in den Daten des Gegenstands.
 *
 * VON HAND NACHLADEN GEHT NICHT: canReload gibt immer falsch zurueck. Gefuellt wird von
 * aussen, an der Zapfsaeule oder am Tank.
 */
public class MagazineFluid implements IMagazine<FluidType> {

    public static final String KEY_MAG_COUNT = "magcount";
    public static final String KEY_MAG_TYPE = "magtype";
    public static final String KEY_MAG_PREV = "magprev";
    public static final String KEY_MAG_AFTER = "magafter";

    /** Eine Nummer, damit die Waffe mehrere Magazine auseinanderhalten kann. */
    public final int index;
    /** Wieviel hineingeht, in Millibar. */
    public final int capacity;

    public MagazineFluid(int index, int capacity) {
        this.index = index;
        this.capacity = capacity;
    }

    @Override public FluidType getType(ItemStack stack, Container container) { return Fluids.fromID(getMagType(stack, this.index)); }
    @Override public void setType(ItemStack stack, FluidType type) { setMagType(stack, this.index, type.getID()); }
    @Override public int getCapacity(ItemStack stack) { return this.capacity; }

    @Override
    public void useUpAmmo(ItemStack stack, Container container, int amount) {
        this.setAmount(stack, this.getAmount(stack, container) - amount);
    }

    @Override public int getAmount(ItemStack stack, Container container) { return getMagCount(stack, this.index); }
    @Override public void setAmount(ItemStack stack, int amount) { setMagCount(stack, this.index, amount); }

    @Override public boolean canReload(ItemStack stack, Container container) { return false; }
    @Override public void initNewType(ItemStack stack, Container container) { }
    @Override public void reloadAction(ItemStack stack, Container container) { }
    @Override public SpentCasing getCasing(ItemStack stack, Container container) { return null; }

    @Override
    public ItemStack getIconForHUD(ItemStack stack, Player player) {
        return FluidIconItem.make(this.getType(stack, player.getInventory()), 0);
    }

    @Override
    public String reportAmmoStateForHUD(ItemStack stack, Player player) {
        return this.getAmount(stack, player.getInventory()) + "mB";
    }

    @Override public void setAmountBeforeReload(ItemStack stack, int amount) { GunBaseNTItem.setValueInt(stack, KEY_MAG_PREV + this.index, amount); }
    @Override public int getAmountBeforeReload(ItemStack stack) { return GunBaseNTItem.getValueInt(stack, KEY_MAG_PREV + this.index); }
    @Override public void setAmountAfterReload(ItemStack stack, int amount) { GunBaseNTItem.setValueInt(stack, KEY_MAG_AFTER + this.index, amount); }
    @Override public int getAmountAfterReload(ItemStack stack) { return GunBaseNTItem.getValueInt(stack, KEY_MAG_AFTER + this.index); }

    public static int getMagType(ItemStack stack, int index) { return GunBaseNTItem.getValueInt(stack, KEY_MAG_TYPE + index); }
    public static void setMagType(ItemStack stack, int index, int value) { GunBaseNTItem.setValueInt(stack, KEY_MAG_TYPE + index, value); }
    public static int getMagCount(ItemStack stack, int index) { return GunBaseNTItem.getValueInt(stack, KEY_MAG_COUNT + index); }
    public static void setMagCount(ItemStack stack, int index, int value) { GunBaseNTItem.setValueInt(stack, KEY_MAG_COUNT + index, value); }
}
