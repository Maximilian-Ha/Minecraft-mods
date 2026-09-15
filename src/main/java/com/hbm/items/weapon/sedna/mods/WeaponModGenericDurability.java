package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.GunConfig;
import net.minecraft.world.item.ItemStack;

/** Portiert aus 1.7.10. Doppelte Haltbarkeit, multiplikativ. */
public class WeaponModGenericDurability extends WeaponModBase {

    public WeaponModGenericDurability(int id) {
        super(id, "GENERIC_DURABILITY");
        this.setPriority(PRIORITY_MULTIPLICATIVE);
    }

    @Override
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(parent instanceof GunConfig && key.equals(GunConfig.F_DURABILITY) && base instanceof Float value) {
            return this.cast(value * 2F, base);
        }

        return base;
    }
}
