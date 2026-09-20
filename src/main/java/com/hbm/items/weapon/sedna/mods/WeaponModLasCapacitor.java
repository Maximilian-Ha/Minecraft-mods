package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;

import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModLasCapacitor.
 *
 * Der Unterlaufkondensator: anderthalbfaches Magazin und fuenf Prozent mehr Schaden.
 *
 * Er benutzt DIESELBE Attrappe wie das Doppelmagazin (WeaponModStackMag.DUMMY_FULL), und das
 * ist im Original so gewollt: ein Magazin wird nirgends aufbewahrt, sondern nur im Augenblick
 * benutzt. Beide Aufsaetze rechnen ohnehin dasselbe -- Kapazitaet mal drei durch zwei.
 */
public class WeaponModLasCapacitor extends WeaponModBase {

    public WeaponModLasCapacitor(int id) {
        super(id, "UNDERBARREL");
        this.setPriority(PRIORITY_MULTIPLICATIVE);
    }

    @Override
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(Receiver.F_BASEDAMAGE)) return cast((Float) base * 1.05F, base);

        if(key.equals(Receiver.O_MAGAZINE) && base instanceof MagazineFullReload original) {
            WeaponModStackMag.DUMMY_FULL.acceptedBullets = original.acceptedBullets;
            WeaponModStackMag.DUMMY_FULL.capacity = original.capacity * 3 / 2;
            WeaponModStackMag.DUMMY_FULL.index = original.index;
            return cast(WeaponModStackMag.DUMMY_FULL, base);
        }

        return base;
    }
}
