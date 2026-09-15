package com.hbm.blockentity.machine;

import com.hbm.blockentity.NtmBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.WatzPump.TileEntityWatzPump.
 *
 * Traegt nichts bei ausser einem Platz im Speicher: sie haelt nur den Sichtbereich fuer das
 * Modell offen, damit der Deckel nicht verschwindet, sobald man an ihm vorbeischaut.
 */
public class WatzPumpBlockEntity extends BlockEntity {

    private AABB renderBox;

    public WatzPumpBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.WATZ_PUMP.get(), pos, state);
    }

    /* Ohne @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 1, y, z - 1, x + 2, y + 2, z + 2);
        }
        return this.renderBox;
    }
}
