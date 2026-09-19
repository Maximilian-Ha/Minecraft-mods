package com.hbm.blockentity.machine;

import com.hbm.blockentity.NtmBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.deco.TileEntityDecoPoleSatelliteReceiver.
 *
 * Sie tut nichts ausser dazusein: der Darsteller braucht eine Blockentitaet, und die
 * Schuessel ragt ueber den Block hinaus, also muss der Sichtkasten groesser sein.
 */
public class DecoPoleSatelliteReceiverBlockEntity extends BlockEntity {

    private AABB sichtkasten;

    public DecoPoleSatelliteReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.POLE_SATELLITE_RECEIVER.get(), pos, state);
    }

    /* Ohne @Override: getRenderBoundingBox kommt aus der NeoForge-Erweiterung von
     * BlockEntity und gilt dem Uebersetzer nicht als ueberschrieben. */
    public AABB getRenderBoundingBox() {
        if(this.sichtkasten == null) {
            this.sichtkasten = new AABB(this.worldPosition).inflate(1);
        }
        return this.sichtkasten;
    }
}
