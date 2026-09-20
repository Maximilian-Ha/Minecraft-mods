package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;

import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModLasAuto.
 *
 * Der Dauerfeuerverschluss: die Waffe feuert, solange die Taste gedrueckt bleibt, alle fuenf
 * Zuege -- und macht dafuer nur noch zwei Drittel des Schadens.
 *
 * ER NIMMT AUCH DAS ZIELFERNROHR WEG (O_SCOPETEXTURE auf null). Das ist keine Strafe, sondern
 * Folge: wer im Dauerfeuer durch ein Fernrohr sieht, sieht nichts mehr.
 */
public class WeaponModLasAuto extends WeaponModBase {

    public WeaponModLasAuto(int id) {
        super(id, "RECEIVER");
        this.setPriority(PRIORITY_SET);
    }

    @Override
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(Receiver.F_BASEDAMAGE)) return cast((Float) base * 0.66F, base);
        if(key.equals(Receiver.B_REFIREONHOLD)) return cast(true, base);
        if(key.equals(Receiver.I_DELAYAFTERFIRE)) return cast(5, base);
        if(key.equals(GunConfig.O_SCOPETEXTURE)) return cast(null, base);

        return base;
    }
}
