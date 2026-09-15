package com.hbm.particle.helper;

import com.hbm.particle.FoundryParticle;
import com.hbm.particle.engine.ParticleEngineNT;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: der Zweig "foundry" in ClientProxy.effectNT.
 *
 * Schickt den Giessstrahl an alle Spieler in der Naehe. Farbe, Richtung und Laenge legt der
 * Giesser fest, denn nur er weiss, was er giesst und wie weit es faellt.
 */
public class FoundryCreator implements IParticleCreator {

    public static void composeEffect(Level level, double x, double y, double z, int color, Direction dir, float length, float base, float offset) {

        CompoundTag tag = new CompoundTag();
        tag.putString("type", "foundry");
        tag.putInt("color", color);
        tag.putByte("dir", (byte) dir.ordinal());
        tag.putFloat("len", length);
        tag.putFloat("base", base);
        tag.putFloat("off", offset);

        IParticleCreator.sendPacket(level, x, y, z, 50, tag);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void makeParticle(ClientLevel level, Player player, RandomSource rand, double x, double y, double z, CompoundTag tag) {

        Direction dir = Direction.values()[tag.getByte("dir") % Direction.values().length];

        ParticleEngineNT.INSTANCE.add(new FoundryParticle(level, x, y, z,
                tag.getInt("color"), dir, tag.getFloat("len"), tag.getFloat("base"), tag.getFloat("off")));
    }
}
