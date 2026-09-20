package com.hbm.items.weapon.sedna.mags;

import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.particle.SpentCasing;
import com.hbm.util.BobMathUtil;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mags.MagazineElectricEngine.
 *
 * Der Akku eines Elektromotors. Dasselbe wie MagazineLiquidEngine, nur dass hier Hertz statt
 * Millibar stehen -- und dass es gar keinen Fluidtyp gibt: getType gibt null zurueck, weil es
 * nichts zu unterscheiden gibt.
 *
 * NACHLADEN VON HAND GEHT NICHT. Gefuellt wird von aussen, ueber IBatteryItem am Geraet.
 */
public class MagazineElectricEngine implements IMagazine<Object> {

    public static final String KEY_MAG_COUNT = "magcount";
    public static final String KEY_MAG_PREV = "magprev";
    public static final String KEY_MAG_AFTER = "magafter";

    /**
     * Eine Nummer, damit die Waffe mehrere Magazine auseinanderhalten kann, und die Menge, die
     * hineingeht.
     *
     * BEIDE SIND ABSICHTLICH NICHT final: WeaponModCanisters verdreifacht die Groesse, indem
     * es die Werte auf einer geteilten Vorlage umsetzt. Das ist im Original genauso geloest.
     */
    public int index;
    public int capacity;

    public MagazineElectricEngine(int index, int capacity) {
        this.index = index;
        this.capacity = capacity;
    }

    @Override public Object getType(ItemStack stack, Container container) { return null; }
    @Override public void setType(ItemStack stack, Object type) { }
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
        return new ItemStack(NtmItems.BATTERY_CREATIVE.get());
    }

    @Override
    public String reportAmmoStateForHUD(ItemStack stack, Player player) {
        return BobMathUtil.getShortNumber(this.getAmount(stack, player.getInventory())) + "/"
                + BobMathUtil.getShortNumber(this.capacity) + "HE";
    }

    @Override public void setAmountBeforeReload(ItemStack stack, int amount) { GunBaseNTItem.setValueInt(stack, KEY_MAG_PREV + this.index, amount); }
    @Override public int getAmountBeforeReload(ItemStack stack) { return GunBaseNTItem.getValueInt(stack, KEY_MAG_PREV + this.index); }
    @Override public void setAmountAfterReload(ItemStack stack, int amount) { GunBaseNTItem.setValueInt(stack, KEY_MAG_AFTER + this.index, amount); }
    @Override public int getAmountAfterReload(ItemStack stack) { return GunBaseNTItem.getValueInt(stack, KEY_MAG_AFTER + this.index); }

    public static int getMagCount(ItemStack stack, int index) { return GunBaseNTItem.getValueInt(stack, KEY_MAG_COUNT + index); }
    public static void setMagCount(ItemStack stack, int index, int value) { GunBaseNTItem.setValueInt(stack, KEY_MAG_COUNT + index, value); }
}
