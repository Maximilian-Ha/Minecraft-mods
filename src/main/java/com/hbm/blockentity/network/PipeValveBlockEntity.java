package com.hbm.blockentity.network;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.network.FluidValveBlock;
import com.hbm.uninos.UniNodespace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityFluidValve.
 *
 * Ein Rohrstueck, das sich zumachen laesst. Geschlossen entsteht gar kein Netzknoten --
 * damit ist das Rohrnetz an dieser Stelle wirklich getrennt und nicht bloss gedrosselt.
 *
 * Im Original steht der Zustand in den Metadaten, hier in der Blockstate-Eigenschaft OPEN.
 */
public class PipeValveBlockEntity extends PipeBaseBlockEntity {

    public PipeValveBlockEntity(BlockPos pos, BlockState state) {
        this(NtmBlockEntityTypes.FLUID_VALVE.get(), pos, state);
    }

    protected PipeValveBlockEntity(BlockEntityType<? extends PipeValveBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean istOffen() {
        BlockState state = this.getBlockState();
        return state.hasProperty(FluidValveBlock.OPEN) && state.getValue(FluidValveBlock.OPEN);
    }

    @Override
    public boolean shouldCreateNode() {
        return this.istOffen();
    }

    /** Nach dem Umlegen: ist zugedreht, faellt der Knoten weg. */
    public void updateState() {
        if(!this.istOffen() && this.node != null) {
            UniNodespace.destroyNode(this.level, this.worldPosition, this.type.getNetworkProvider());
            this.node = null;
        }
    }
}
