package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.factory.XFactory12ga;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.items.weapon.sedna.mags.MagazineSingleReload;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;

import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModLiberatorSpeedloader.
 *
 * Der Schnellader des Liberator. Er tauscht das einzelne Nachladen gegen einen Wechsel am
 * Stueck: ohne ihn schiebt man vier Patronen nacheinander ein, mit ihm fallen alle vier
 * zugleich heraus und vier neue hinein.
 *
 * ER TAUSCHT NICHT NUR DAS MAGAZIN, SONDERN AUCH DIE BEWEGUNG. Waere nur das Magazin
 * gewechselt, liefe weiter die Einzelnachlade-Bewegung -- die Waffe wuerde vier Patronen auf
 * einmal laden und dabei viermal dieselbe Patrone zeigen.
 */
public class WeaponModLiberatorSpeedloader extends WeaponModBase {

    /** Das Wechselmagazin. Seine Patronenliste wird beim ersten Gebrauch uebernommen. */
    public static final MagazineFullReload MAG = new MagazineFullReload(0, 4);

    public WeaponModLiberatorSpeedloader(int id) {
        super(id, "SPEEDLOADER");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(GunConfig.FUN_ANIMNATIONS)) return (T) LAMBDA_LIBERATOR_ANIMS;

        if(parent instanceof Receiver && base instanceof IMagazine && key.equals(Receiver.O_MAGAZINE)) {
            MagazineSingleReload original = (MagazineSingleReload) base;
            if(MAG.acceptedBullets.isEmpty()) MAG.acceptedBullets.addAll(original.acceptedBullets);
            return (T) MAG;
        }

        return base;
    }

    /**
     * Die Bewegung des Wechsels: Riegel auf, Lauf abknicken, alle vier Huelsen zugleich
     * heraus. Was hier nicht steht, faellt auf die gewoehnliche Liberator-Bewegung zurueck.
     */
    public static final BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_LIBERATOR_ANIMS = (stack, type) -> {
        switch(type) {
            case RELOAD: return new BusAnimation()
                    .addBus("LATCH", new BusAnimationSequence().addPos(15, 0, 0, 100))
                    .addBus("BREAK", new BusAnimationSequence().addPos(0, 0, 0, 100).addPos(60, 0, 0, 350, IType.SIN_DOWN))
                    .addBus("SHELL1", new BusAnimationSequence().addPos(2, -4, -2, 0).addPos(2, -4, -2, 400).addPos(0, 0, -2, 450, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP))
                    .addBus("SHELL2", new BusAnimationSequence().addPos(2, -4, -2, 0).addPos(2, -4, -2, 400).addPos(0, 0, -2, 450, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP))
                    .addBus("SHELL3", new BusAnimationSequence().addPos(2, -4, -2, 0).addPos(2, -4, -2, 400).addPos(0, 0, -2, 450, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP))
                    .addBus("SHELL4", new BusAnimationSequence().addPos(2, -4, -2, 0).addPos(2, -4, -2, 400).addPos(0, 0, -2, 450, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP));
            case RELOAD_END: return new BusAnimation()
                    .addBus("LATCH", new BusAnimationSequence().addPos(15, 0, 0, 0).addPos(15, 0, 0, 250).addPos(0, 0, 0, 50))
                    .addBus("BREAK", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 250, IType.SIN_UP));
            case JAMMED: return new BusAnimation()
                    .addBus("LATCH", new BusAnimationSequence().addPos(15, 0, 0, 0).addPos(15, 0, 0, 250).addPos(0, 0, 0, 50).addPos(0, 0, 0, 550).addPos(15, 0, 0, 100).addPos(15, 0, 0, 600).addPos(0, 0, 0, 50))
                    .addBus("BREAK", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 250, IType.SIN_UP).addPos(0, 0, 0, 600).addPos(45, 0, 0, 250, IType.SIN_DOWN).addPos(45, 0, 0, 300).addPos(0, 0, 0, 150, IType.SIN_UP));
            default: return XFactory12ga.LAMBDA_LIBERATOR_ANIMS.apply(stack, type);
        }
    };
}
