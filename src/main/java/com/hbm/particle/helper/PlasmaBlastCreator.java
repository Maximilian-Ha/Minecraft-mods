package com.hbm.particle.helper;

import com.hbm.particle.PlasmaBlastParticle;
import com.hbm.particle.engine.ParticleEngineNT;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: der "plasmablast"-Zweig aus ClientProxy.
 *
 * Der Schockfaecher einer Energieexplosion: eine flache Scheibe, die aufgeht und verblasst.
 * Wer ihn ausloest, gibt Farbe, Neigung, Gier und Groesse mit -- gewoehnlich drei Stueck
 * uebereinander, um je 60 Grad gekippt, damit der Schlag aus jeder Richtung zu sehen ist.
 *
 * RUNDE 189: DIE PARTIKELKLASSE LAG SEIT JEHER IM BAUM UND NIEMAND HAT SIE JE ERZEUGT.
 * PlasmaBlastParticle war vollstaendig geschrieben, samt Textur, aber es gab keinen Weg, eine
 * zu bekommen: kein Verteilereintrag, kein Aufrufer. An drei Stellen steht deshalb bis heute
 * im Kommentar, diese Partikelart habe der Port nicht -- das stimmte nie. Mit diesem Erzeuger
 * stimmt es jetzt auch nicht mehr im Ergebnis.
 */
public class PlasmaBlastCreator implements IParticleCreator {

    /**
     * Der Faecher des Originals: drei Scheiben um je 60 Grad gekippt, mit einer zufaelligen
     * gemeinsamen Gier. Genau diese Schleife steht im Original an jeder der drei Fundstellen.
     */
    public static void composeEffectTriple(Level level, double x, double y, double z, float r, float g, float b, float scale) {

        if(!(level instanceof ServerLevel serverLevel)) return;

        float yaw = serverLevel.random.nextFloat() * 180F;

        for(int i = 0; i < 3; i++) {
            composeEffect(level, x, y, z, r, g, b, -60F + 60F * i, yaw, scale);
        }
    }

    public static void composeEffect(Level level, double x, double y, double z, float r, float g, float b, float pitch, float yaw, float scale) {

        CompoundTag tag = new CompoundTag();
        tag.putString("type", "plasmablast");
        tag.putFloat("r", r);
        tag.putFloat("g", g);
        tag.putFloat("b", b);
        tag.putFloat("pitch", pitch);
        tag.putFloat("yaw", yaw);
        tag.putFloat("scale", scale);
        IParticleCreator.sendPacket(level, x, y, z, 100, tag);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void makeParticle(ClientLevel level, Player player, RandomSource rand, double x, double y, double z, CompoundTag tag) {

        PlasmaBlastParticle partikel = new PlasmaBlastParticle(level, x, y, z,
                tag.getFloat("r"), tag.getFloat("g"), tag.getFloat("b"),
                tag.getFloat("pitch"), tag.getFloat("yaw"));
        partikel.setScale(tag.getFloat("scale"));
        ParticleEngineNT.INSTANCE.add(partikel);
    }
}
