package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.Receiver;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModSlowdown.
 *
 * Die Drossel der Minigun. Sie halbiert die Schussfolge und nimmt der Waffe dafuer die ganze
 * Streuung -- aus dem Rasenmaeher wird ein Praezisionsgeraet.
 */
public class WeaponModSlowdown extends WeaponModBase {

    public WeaponModSlowdown(int id) {
        super(id, "SPEED");
    }

    @Override
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(Receiver.I_DELAYAFTERFIRE)) return cast((Integer) base * 2, base);
        if(key.equals(Receiver.F_SPREADINNATE)) return cast(0F, base);

        return base;
    }
}
