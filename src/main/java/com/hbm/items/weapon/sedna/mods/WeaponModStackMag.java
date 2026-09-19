package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.items.weapon.sedna.mags.MagazineSingleReload;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModStackMag.
 *
 * Das Doppelmagazin: anderthalbfache Kapazitaet.
 *
 * WARUM ZWEI FESTE ATTRAPPEN REICHEN -- und das ist die Anmerkung des Originals, nicht meine:
 * ein Magazin ist kein dauerhaftes Objekt. Es wird nirgends aufbewahrt, sondern nur benutzt,
 * um an die Munitionsdaten des Gegenstands heranzukommen und um das Nachladen abzuwickeln.
 * Deshalb darf man dieselben zwei Attrappen immer wieder umstellen, statt fuer jede Waffe
 * eine eigene anzulegen.
 */
public class WeaponModStackMag extends WeaponModBase {

    protected static final MagazineSingleReload DUMMY_SINGLE = new MagazineSingleReload(0, 0);
    protected static final MagazineFullReload DUMMY_FULL = new MagazineFullReload(0, 0);

    public WeaponModStackMag(int id) {
        super(id, "MAG");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(Receiver.O_MAGAZINE)) {

            if(base instanceof MagazineSingleReload original) {
                DUMMY_SINGLE.acceptedBullets = original.acceptedBullets;
                DUMMY_SINGLE.capacity = original.capacity * 3 / 2;
                DUMMY_SINGLE.index = original.index;
                return (T) DUMMY_SINGLE;
            }

            if(base instanceof MagazineFullReload original) {
                DUMMY_FULL.acceptedBullets = original.acceptedBullets;
                DUMMY_FULL.capacity = original.capacity * 3 / 2;
                DUMMY_FULL.index = original.index;
                return (T) DUMMY_FULL;
            }
        }

        return base;
    }

    @Override public void onInstall(ItemStack gun, ItemStack mod, int index) { XWeaponModManager.changedMagState(); }
    @Override public void onUninstall(ItemStack gun, ItemStack mod, int index) { XWeaponModManager.changedMagState(); }
}
