package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;

import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModLasShotgun.
 *
 * Der Schrotlauf des Lasergewehrs: aus einem Strahl werden drei, jeder mit gut einem Drittel
 * des Schadens. Unterm Strich ein Zehntel mehr, dafuer breit gestreut -- und die Streuung aus
 * der Hueffe faellt dabei GANZ WEG, weil ohnehin nichts mehr zu treffen ist, was nicht im
 * Faecher liegt. Das Fadenkreuz wechselt zum grossen Kreis, damit man den Faecher sieht.
 */
public class WeaponModLasShotgun extends WeaponModBase {

    public WeaponModLasShotgun(int id) {
        super(id, "BARREL");
        this.setPriority(PRIORITY_MULTIPLICATIVE);
    }

    @Override
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(Receiver.F_BASEDAMAGE)) return cast((Float) base * 0.35F, base);
        if(key.equals(Receiver.F_SPLITPROJECTILES)) return cast((Float) base * 3F, base);
        if(key.equals(Receiver.F_SPREADINNATE)) return cast((Float) base + 3F, base);
        if(key.equals(Receiver.F_SPREADHIPFIRE)) return cast(0F, base);
        if(key.equals(GunConfig.O_CROSSHAIR)) return cast(Crosshair.L_CIRCLE, base);

        return base;
    }
}
