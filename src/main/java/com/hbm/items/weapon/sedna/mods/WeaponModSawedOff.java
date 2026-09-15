package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.factory.XFactory12ga;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModSawedOff.
 *
 * Die Saege. Ein abgesaegter Lauf streut mehr und trifft dafuer haerter: mindestens 0,025 Streuung
 * aus der Waffe selbst, anderthalbfache Streuung aus der Munition, ein gutes Drittel mehr Schaden.
 *
 * An der Mare's Leg kommt zweierlei dazu: sie zieht schneller, weil weniger Waffe zu heben ist,
 * und sie bekommt den kurzen Animationssatz -- den mit dem Durchladen aus dem Handgelenk.
 *
 * ABWEICHUNG: das Original traegt die Saege auch an der Doppelflinte ein. Die fehlt im Port und
 * kommt mit ihr.
 */
public class WeaponModSawedOff extends WeaponModBase {

    public WeaponModSawedOff(int id) {
        super(id, "BARREL");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(Receiver.F_SPREADINNATE)) return cast(Math.max(0.025F, (Float) base), base);
        if(key.equals(Receiver.F_SPREADAMMO)) return cast((Float) base * 1.5F, base);
        if(key.equals(Receiver.F_BASEDAMAGE)) return cast((Float) base * 1.35F, base);

        if(gun.getItem() == NtmItems.GUN_MARESLEG.get()) {
            if(key.equals(GunConfig.FUN_ANIMNATIONS)) return (T) XFactory12ga.LAMBDA_MARESLEG_SHORT_ANIMS;
            if(key.equals(GunConfig.I_DRAWDURATION)) return cast(5, base);
        }

        return base;
    }
}
