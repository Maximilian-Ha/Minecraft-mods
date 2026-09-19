package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.Receiver;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModChoke.
 *
 * Die Wuergebohrung. Sie verengt die Muendung einer Schrotflinte und halbiert damit die
 * Streuung der Ladung -- aus der Wand aus Blei wird ein Buendel.
 */
public class WeaponModChoke extends WeaponModBase {

    public WeaponModChoke(int id) {
        super(id, "BARREL");
    }

    @Override
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(Receiver.F_SPREADAMMO)) return cast((Float) base * 0.5F, base);

        return base;
    }
}
