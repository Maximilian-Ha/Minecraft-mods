package com.hbm.handler.neutron;

import com.hbm.handler.neutron.NeutronStream.NeutronType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Ein Knoten im Neutronen-Nodespace, quasi die "Seele" einer Block-Entitaet fuer die
 * Neutronenrechnung. Siehe die Paketbeschreibung in {@code package-info.java}.
 */
public abstract class NeutronNode {

    protected NeutronType type;

    protected BlockPos pos;

    protected BlockEntity tile;

    /** Wie NBT, nur ohne den Aufwand. Haelt zum Beispiel den zwischengespeicherten Deckelzustand einer RBMK-Saeule. */
    protected Map<String, Object> data = new HashMap<>();

    public NeutronNode(BlockEntity tile, NeutronType type) {
        this.type = type;
        this.tile = tile;
        this.pos = tile.getBlockPos();
    }

    public NeutronType getType() {
        return this.type;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public BlockEntity getTile() {
        return this.tile;
    }
}
