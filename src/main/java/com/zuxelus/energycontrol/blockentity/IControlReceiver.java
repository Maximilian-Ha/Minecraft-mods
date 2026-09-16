package com.zuxelus.energycontrol.blockentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Ein Block, der Eingaben aus einer Oberflaeche entgegennimmt.
 *
 * Fuer Schalter genuegt {@code clickMenuButton} von Minecraft; fuer alles, was einen Wert
 * mitbringt -- eingetippter Text, eine Farbe, eine Zahl --, braucht es ein eigenes Paket.
 * Dieselbe Schnittstelle fuehrt auch der HBM-Port (IControlReceiver), aus demselben Grund.
 *
 * Die Rechtepruefung liegt beim Empfaenger und nicht beim Paket: nur der Block weiss, wer ihn
 * bedienen darf.
 */
public interface IControlReceiver {

    /** Ob dieser Spieler den Block gerade bedienen darf -- in der Regel: steht er nah genug? */
    boolean hasPermission(Player player);

    void receiveControl(Player player, CompoundTag tag);
}
