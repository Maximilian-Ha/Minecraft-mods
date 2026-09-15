package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.Receiver;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModMinigunSpeedup.
 *
 * Der Schnellauf der Minigun. Er verdreifacht die Zahl der Geschosse je Schuss und nimmt dafuer
 * die halbe Treffgenauigkeit -- das Gegenstueck zur Drossel, und mit ihr unvertraeglich, weil
 * beide denselben Platz (SPEED) belegen.
 */
public class WeaponModMinigunSpeedup extends WeaponModBase {

    public WeaponModMinigunSpeedup(int id) {
        super(id, "SPEED");
    }

    @Override
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(Receiver.I_ROUNDSPERCYCLE)) return cast((Integer) base * 3, base);
        if(key.equals(Receiver.F_SPREADINNATE)) return cast((Float) base * 1.5F, base);

        return base;
    }
}
