package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.factory.XFactory556mm;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeapnModG3SawedOff (Schreibfehler im
 * Original; hier richtiggestellt).
 *
 * Der abgesaegte Schaft der G3. Er nimmt der Waffe die Schulterstuetze: sie zieht doppelt so
 * schnell, und das Anlegen geht entsprechend flotter. Sonst aendert er nichts -- alle uebrigen
 * Bewegungen kommen unveraendert aus der Fabrik.
 *
 * Dass die Waffe ohne Schaft sichtbar weiter zurueckspringt, steht nicht hier, sondern im
 * CYCLE-Satz der Fabrik: der fragt den Aufsatz selbst ab.
 */
public class WeaponModG3SawedOff extends WeaponModBase {

    public WeaponModG3SawedOff(int id) {
        super(id, "SHIELD");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(GunConfig.I_DRAWDURATION)) return cast(5, base);
        if(key.equals(GunConfig.FUN_ANIMNATIONS)) return (T) LAMBDA_G3_ANIMS;

        return base;
    }

    /** Nur das Ziehen ist eigen; alles andere reicht die Fabrik durch. */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_G3_ANIMS = (stack, type) -> {

        if(type == GunAnimation.EQUIP) return new BusAnimation()
                .addBus("EQUIP", new BusAnimationSequence().addPos(45, 0, 0, 0).addPos(0, 0, 0, 250, IType.SIN_FULL));

        return XFactory556mm.LAMBDA_G3_ANIMS.apply(stack, type);
    };
}
