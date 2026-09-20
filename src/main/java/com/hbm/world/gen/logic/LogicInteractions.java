package com.hbm.world.gen.logic;

import api.hbm.energymk2.IEnergyHandlerMK2;
import com.hbm.blockentity.machine.LockableBaseBlockEntity;
import com.hbm.registry.NtmSoundEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.world.gen.util.LogicBlockInteractions.
 *
 * Eine Wechselwirkung laeuft beim Rechtsklick auf den Logikstab. Die drei Bauwerke mit
 * Logikstaeben nennen genau EINE, und sie gehoert zum Stromschloss: der Tresor, den die
 * Aktion POWER_LOCK zugesperrt hat, geht wieder auf -- aber nur, wenn im Nachbarfeld ein
 * Energiespeicher mit mehr als 500 kHE steht.
 *
 * DAS ARGUMENT IST EIN OBJEKTFELD, wie im Original: Welt, Blockentitaet, Ort, Spieler. Das
 * ist unschoen und bleibt trotzdem so -- die Tabelle soll sich lesen wie ihre Vorlage, und
 * eine eigene Schnittstelle fuer einen einzigen Eintrag waere mehr Gerüst als Inhalt.
 */
public class LogicInteractions {

    private static final Map<String, Consumer<Object[]>> WECHSELWIRKUNGEN = Map.of(
            "POWER_LOCK", LogicInteractions::stromschloss);

    /** Null, wenn der Name unbekannt oder leer ist -- dann tut der Rechtsklick nichts. */
    public static Consumer<Object[]> finde(String name) {
        return name == null || name.isEmpty() ? null : WECHSELWIRKUNGEN.get(name);
    }

    private static void stromschloss(Object[] felder) {

        Level level = (Level) felder[0];
        BlockPos pos = (BlockPos) felder[2];
        Player spieler = (Player) felder[3];

        IEnergyHandlerMK2 speicher = null;
        for(Direction richtung : Direction.values()) {
            if(level.getBlockEntity(pos.relative(richtung)) instanceof IEnergyHandlerMK2 gefunden) {
                speicher = gefunden;
                break;
            }
        }

        if(speicher == null || speicher.getPower() <= 500_000L) {
            spieler.displayClientMessage(Component.literal(ChatFormatting.LIGHT_PURPLE + "[POWER LOCK]"
                    + ChatFormatting.RESET + " Charge adjacent energy storage to at least 500KHE to release emergency lock"), false);
            return;
        }

        spieler.displayClientMessage(Component.literal(ChatFormatting.LIGHT_PURPLE + "[POWER LOCK]"
                + ChatFormatting.RESET + " Power Restorted! Safe Unlocked!"), false);

        LockableBaseBlockEntity tresor = LogicActions.nachbarTresor(level, pos);
        if(tresor != null) {
            tresor.unlock();
            level.playSound(null, pos, NtmSoundEvents.LOCK_OPEN.get(), SoundSource.BLOCKS, 3.0F, 0.8F);
        }
    }
}
