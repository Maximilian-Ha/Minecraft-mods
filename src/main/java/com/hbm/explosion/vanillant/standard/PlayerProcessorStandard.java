package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IPlayerProcessor;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.explosion.vanillant.standard.PlayerProcessorStandard.
 *
 * Der Rueckstoss auf Spieler. Er muss eigens verschickt werden, weil ein Spieler seine eigene
 * Bewegung rechnet: setzt der Server ihm nur die Geschwindigkeit, verwirft sein Rechner das
 * beim naechsten Zug wieder.
 *
 * ABWEICHUNG: das Original schickt dafuer ein eigenes Paket (ExplosionKnockbackPacket). In
 * 1.21 tut es das Bordmittel ClientboundSetEntityMotionPacket -- dasselbe, was auch die
 * Explosion der Grundausstattung verschickt.
 */
public class PlayerProcessorStandard implements IPlayerProcessor {

    @Override
    public void process(ExplosionVNT explosion, Level level, double x, double y, double z, HashMap<Player, Vec3> affectedPlayers) {

        for(Map.Entry<Player, Vec3> eintrag : affectedPlayers.entrySet()) {

            Player spieler = eintrag.getKey();
            if(!(spieler instanceof ServerPlayer serverSpieler)) continue;

            spieler.setDeltaMovement(spieler.getDeltaMovement().add(eintrag.getValue()));
            spieler.hurtMarked = true;
            serverSpieler.connection.send(new ClientboundSetEntityMotionPacket(spieler));
        }
    }
}
