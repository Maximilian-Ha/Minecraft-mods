package com.hbm.handler.neutron;

import com.hbm.handler.neutron.NeutronNodeWorld.StreamWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;

/**
 * Ein Neutronenstrom. Lebt im Normalfall genau einen Tick: er wird erzeugt, einmal abgearbeitet
 * und danach verworfen.
 */
public abstract class NeutronStream {

    public enum NeutronType {
        /** Platzhalterstroeme, mit denen Knoten aus dem Cache geraeumt werden. */
        DUMMY,
        /** Stroeme des RBMK. */
        RBMK,
        /** Stroeme des Chicago Pile. */
        PILE
    }

    public NeutronNode origin;

    /** Gesamtmenge des Flusses. */
    public double fluxQuantity;
    /** Verhaeltnis von schnellem zu langsamem Fluss: 0 ist komplett langsam, 1 komplett schnell. */
    public double fluxRatio;

    public NeutronType type = NeutronType.DUMMY;

    /** Richtung, in die der Strom laeuft. */
    public Vec3 vector;

    /** Erzeugt einen Strom, der nicht in die Liste wandert -- zum Beispiel fuer Cache-Pruefungen. */
    public NeutronStream(NeutronNode origin, Vec3 vector) {
        this.origin = origin;
        this.vector = vector;
    }

    public NeutronStream(NeutronNode origin, Vec3 vector, double flux, double ratio, NeutronType type) {
        this.origin = origin;
        this.vector = vector;
        this.fluxQuantity = flux;
        this.fluxRatio = ratio;
        this.type = type;

        Level level = origin.tile.getLevel();
        if(level != null) NeutronNodeWorld.getOrAddWorld(level).addStream(this);
    }

    /** Laeuft die Bloecke entlang der Stromrichtung ab, den Ursprung ausgenommen. */
    public Iterator<BlockPos> getBlocks(int range) {

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos origin = this.origin.pos;

        return new Iterator<>() {

            private int i = 1;

            @Override
            public boolean hasNext() {
                return i <= range;
            }

            @Override
            public BlockPos next() {
                int x = (int) Math.floor(0.5 + vector.x * i);
                int z = (int) Math.floor(0.5 + vector.z * i);
                i++;
                return cursor.set(origin.getX() + x, origin.getY(), origin.getZ() + z);
            }
        };
    }

    public abstract void runStreamInteraction(Level level, StreamWorld streamWorld);
}
