package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.Receiver;
import net.minecraft.world.item.ItemStack;

/** Portiert aus 1.7.10. Fuenfzehn Prozent mehr Schaden, multiplikativ. */
public class WeaponModGenericDamage extends WeaponModBase {

    public WeaponModGenericDamage(int id) {
        super(id, "GENERIC_DAMAGE");
        this.setPriority(PRIORITY_MULTIPLICATIVE);
    }

    @Override
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(parent instanceof Receiver && key.equals(Receiver.F_BASEDAMAGE) && base instanceof Float value) {
            return this.cast(value * 1.15F, base);
        }

        return base;
    }
}
