package com.hbm.items;

import com.hbm.render.anim.BusAnimation;

import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.items.IAnimatedItem.
 *
 * EIN GEGENSTAND, DER SICH BEWEGT, OHNE EINE WAFFE ZU SEIN. Das Bewegungssystem des Ports
 * (HbmAnimations) schlaegt eine laufende Bewegung ueber den Uebersetzungsschluessel des
 * Gegenstands nach -- es fragt nie, ob der Gegenstand eine Waffe ist. Nur der Weg dorthin war
 * bisher einer: HbmAnimation, das Paket vom Server, sah ausschliesslich nach GunBaseNTItem.
 *
 * Diese Schnittstelle ist der zweite Weg. Wer sie umsetzt, bekommt seine Bewegung genauso in
 * den Regalplatz gelegt wie eine Waffe -- ohne Empfaenger, ohne Magazin, ohne GunConfig.
 *
 * DIE KENNUNG ist die Nummer aus dem Paket. Das Original benutzt dafuer einen NBT-Beutel und
 * liest daraus "mode"; im Port steht an dieser Stelle ohnehin schon eine Zahl, und eine Zahl
 * genuegt: die Bolzenpistole kennt genau eine Bewegung.
 */
public interface IAnimatedItem {

    /**
     * Die Bewegung zu einer Kennung, oder null. Wird NUR auf dem Client gerufen -- BusAnimation
     * ist reine Darstellung, auf dem Server gibt es sie nicht zu sehen.
     */
    @OnlyIn(Dist.CLIENT)
    BusAnimation getAnimation(ItemStack stack, short animType);
}
