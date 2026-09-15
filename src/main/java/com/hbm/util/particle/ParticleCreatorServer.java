package com.hbm.util.particle;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Serverseitiges Gegenstueck zu ParticleCreatorClient.
 *
 * WICHTIG zur Stueckzahl 0: Vanilla wertet die drei Richtungswerte nur dann als
 * Geschwindigkeit aus, wenn count == 0 ist (dann gilt Geschwindigkeit = maxSpeed * Wert,
 * bei maxSpeed 1F also der Wert selbst). Bei count > 0 gelten sie stattdessen als
 * Gauss-POSITIONSVERSATZ, und die Geschwindigkeit kommt aus maxSpeed * Zufallswert.
 * Mit count == 1 wurde ein gerichteter Strahl daher zu einer Zufallswolke -- beim
 * Turbofan verstreute das die Nachbrennerflammen um mehrere Bloecke.
 * ParticleCreatorClient reicht die Werte direkt als Geschwindigkeit an createParticle
 * weiter; count == 0 ist also das, was beide Seiten gleich verhalten laesst.
 */
public class ParticleCreatorServer implements IParticleCreator {

    @Override
    public <T extends ParticleOptions> void addParticle(Level level, T options, double x, double y, double z, float xd, float yd, float zd, double radius) {
        if(level instanceof ServerLevel serverLevel) {
            serverLevel.getServer().getPlayerList().broadcast(
                    null,
                    x,
                    y,
                    z,
                    radius,
                    serverLevel.dimension(),
                    new ClientboundLevelParticlesPacket(options, true, x, y, z, xd, yd, zd, 1F, 0)
            );
        }
    }

    @Override
    public <T extends ParticleOptions> void addParticle(Level level, T options, double x, double y, double z, float xd, float yd, float zd) {
        if(level instanceof ServerLevel serverLevel) {
            serverLevel.getServer().getPlayerList().broadcastAll(
                    new ClientboundLevelParticlesPacket(options, true, x, y, z, xd, yd, zd, 1F, 0),
                    serverLevel.dimension()
            );
        }
    }
}
