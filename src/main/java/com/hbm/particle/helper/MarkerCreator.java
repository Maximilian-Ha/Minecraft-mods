package com.hbm.particle.helper;

import com.hbm.render.util.RenderMarkers;
import com.hbm.render.util.RenderMarkers.Marker;
import com.hbm.util.Clock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Portiert aus 1.7.10: der "marker"-Zweig von ClientProxy.effectNT.
 *
 * Keine Partikel, sondern ein Rahmen mit Beschriftung um einen einzelnen Block. Die grossen
 * Reaktoren melden damit, an welcher Stelle ihr Bau nicht aufgeht.
 */
public class MarkerCreator implements IParticleCreator {

    @Override
    public void makeParticle(ClientLevel level, Player player, RandomSource rand, double x, double y, double z, CompoundTag tag) {

        int color = tag.getInt("color");
        String label = tag.getString("label");
        int expires = tag.getInt("expires");
        double dist = tag.getDouble("dist");

        Marker marker = new Marker(color)
                .setDist(dist)
                .setExpire(expires > 0 ? Clock.get_ms() + expires : 0)
                .withLabel(label.isEmpty() ? null : label);

        RenderMarkers.queue(BlockPos.containing(x, y, z), marker);
    }

    /**
     * Portiert aus 1.7.10: MachinePWRController.sendError.
     *
     * Rahmt einen Block fuer fuenf Sekunden rot ein und schreibt den Grund daneben. Geht nur an
     * den einen Spieler, der den Bau ausgeloest hat.
     */
    public static void sendError(ServerPlayer player, BlockPos pos, Component message) {
        sendMarker(player, pos, 0xff0000, 5_000, 128D, message);
    }

    /** Der allgemeine Fall: eigene Farbe, eigene Frist, eigene Reichweite. */
    public static void sendMarker(ServerPlayer player, BlockPos pos, int color, int expires, double dist, Component message) {

        CompoundTag data = new CompoundTag();
        data.putString("type", "marker");
        data.putInt("color", color);
        data.putInt("expires", expires);
        data.putDouble("dist", dist);
        if(message != null) data.putString("label", message.getString());

        PacketDistributor.sendToPlayer(player, new com.hbm.network.toclient.AuxParticle(data,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
    }
}
