package com.hbm.items.weapon.sedna.mods;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IWeaponMod {

    /** Lower numbers get installed and therefore evaluated first. Important when multiplicative and additive bonuses are supposed to stack */
    int getModPriority();
    String[] getSlots();
    /** The meat and bones of the upgrade eval. Requires the base value, the held gun, the value's
     * identifier and the yet unmodified parent (i.e. if the value is part of the receiver, that receiver) */
    <T> T eval(T base, ItemStack gun, String key, Object parent);

    /*
     * ABWEICHUNG VOM ORIGINAL: beide Haken bekommen die Welt mitgereicht. Das Original kommt
     * ohne aus, weil es Verzauberungen ueber feste Enchantment-Felder anspricht; in 1.21 sind
     * Verzauberungen datengetrieben und nur ueber die Registry der laufenden Welt zu
     * bekommen. Ohne diesen Parameter koennte WeaponModDrillFortune nichts tun.
     */
    default void onInstall(Level level, ItemStack gun, ItemStack mod, int index) { }
    default void onUninstall(Level level, ItemStack gun, ItemStack mod, int index) { }
}
