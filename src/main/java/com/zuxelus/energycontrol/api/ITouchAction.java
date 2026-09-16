package com.zuxelus.energycontrol.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.api.ITouchAction.
 *
 * Laesst den Schirm einer Tafel als Schaltflaeche benutzen: ein Rechtsklick wirkt auf das
 * Ziel der Karte. Voraussetzung ist die Beruehrungsaufwertung im vierten Fach.
 *
 * NICHT UEBERNOMMEN: {@code renderImage} aus dem Original, mit dem eine Karte eigene Grafik
 * auf den Schirm zeichnen konnte. Keine Karte dieses Ports braucht das.
 */
public interface ITouchAction {

    boolean enableTouch(ItemStack stack);

    /**
     * Wird auf dem Server aufgerufen, wenn ein Spieler den Schirm anfasst.
     *
     * @return ob etwas geschehen ist
     */
    boolean runTouchAction(Level level, Player player, ICardReader reader, ItemStack stack);
}
