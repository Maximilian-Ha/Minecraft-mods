package com.hbm.items.weapon.sedna.mags;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.FluidIconItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.particle.SpentCasing;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mags.MagazineLiquidEngine.
 *
 * Der Tank eines Verbrennungsmotors. Er traegt keine Patronen, sondern Millibar Kraftstoff,
 * und er laesst sich NICHT von Hand nachladen -- canReload gibt immer falsch zurueck. Gefuellt
 * wird er von aussen, so wie man einen Tank betankt.
 *
 * DIE LISTE DER ANGENOMMENEN FLUIDE steht im Konstruktor und wird hier nur verwahrt: welches
 * davon gerade drin ist, fragt dieses Magazin nie -- getType gibt stets das erste zurueck.
 * Das ist im Original genauso; die Waffe, die daran haengt, unterscheidet die Kraftstoffe
 * nicht.
 */
public class MagazineLiquidEngine implements IMagazine<FluidType> {

    public static final String KEY_MAG_COUNT = "magcount";
    public static final String KEY_MAG_PREV = "magprev";
    public static final String KEY_MAG_AFTER = "magafter";

    /** Eine Nummer, damit die Waffe mehrere Magazine auseinanderhalten kann. */
    public final int index;
    /** Wieviel Kraftstoff hineingeht, in Millibar. */
    public final int capacity;
    /** Was man hineinfuellen darf. */
    public final FluidType[] acceptedTypes;

    public MagazineLiquidEngine(int index, int capacity, FluidType... acceptedTypes) {
        this.index = index;
        this.capacity = capacity;
        this.acceptedTypes = acceptedTypes;
    }

    @Override public FluidType getType(ItemStack stack, Container container) { return this.acceptedTypes[0]; }
    @Override public void setType(ItemStack stack, FluidType type) { }
    @Override public int getCapacity(ItemStack stack) { return this.capacity; }

    @Override
    public void useUpAmmo(ItemStack stack, Container container, int amount) {
        this.setAmount(stack, Math.max(this.getAmount(stack, container) - amount, 0));
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
        return this.getAmount(stack, player.getInventory()) + "/" + this.capacity + "mB";
    }

    @Override public void setAmountBeforeReload(ItemStack stack, int amount) { GunBaseNTItem.setValueInt(stack, KEY_MAG_PREV + this.index, amount); }
    @Override public int getAmountBeforeReload(ItemStack stack) { return GunBaseNTItem.getValueInt(stack, KEY_MAG_PREV + this.index); }
    @Override public void setAmountAfterReload(ItemStack stack, int amount) { GunBaseNTItem.setValueInt(stack, KEY_MAG_AFTER + this.index, amount); }
    @Override public int getAmountAfterReload(ItemStack stack) { return GunBaseNTItem.getValueInt(stack, KEY_MAG_AFTER + this.index); }

    public static int getMagCount(ItemStack stack, int index) { return GunBaseNTItem.getValueInt(stack, KEY_MAG_COUNT + index); }
    public static void setMagCount(ItemStack stack, int index, int value) { GunBaseNTItem.setValueInt(stack, KEY_MAG_COUNT + index, value); }
}
