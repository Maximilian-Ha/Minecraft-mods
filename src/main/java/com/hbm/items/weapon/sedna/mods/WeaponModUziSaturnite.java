package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModUziSaturnite.
 *
 * Das Saturnit-Gehaeuse der Uzi. Es sieht nicht nur anders aus: fuenffache Haltbarkeit und drei
 * Schaden mehr je Treffer. Es sitzt im Schaftfach, also nicht neben dem aufgearbeiteten Schaft
 * der M3 -- die beiden schliessen sich gegenseitig aus.
 */
public class WeaponModUziSaturnite extends WeaponModBase {

    public WeaponModUziSaturnite(int id) {
        super(id, "FURNITURE");
        this.setPriority(PRIORITY_ADDITIVE);
    }

    @Override
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(GunConfig.F_DURABILITY)) return cast((Float) base * 5F, base);
        if(key.equals(Receiver.F_BASEDAMAGE)) return cast((Float) base + 3F, base);

        return base;
    }
}
