package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModSilencer.
 *
 * Der Schalldaempfer tauscht nur das Schussgeraeusch aus -- am Schaden aendert er nichts.
 *
 * ABWEICHUNG: das Original hat einen Sonderfall fuer das Antimateriegewehr, das einen eigenen
 * gedaempften Schuss hat. Die Waffe fehlt im Port; der Sonderfall kommt mit ihr.
 */
public class WeaponModSilencer extends WeaponModBase {

    public WeaponModSilencer(int id) {
        super(id, "SILENCER");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(Receiver.S_FIRESOUND)) return (T) NtmSoundEvents.GUN_SILENCED_FIRE;

        return base;
    }
}
