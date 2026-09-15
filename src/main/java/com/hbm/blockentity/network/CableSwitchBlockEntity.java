package com.hbm.blockentity.network;

import api.hbm.energymk2.Nodespace;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.network.CableSwitchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityCableSwitch.
 * Der Schalter trennt ein Netz auf: ist er aus, entsteht gar kein Knoten.
 */
public class CableSwitchBlockEntity extends CableBaseBlockEntity {

    public CableSwitchBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.NETWORK_CABLE_SWITCH.get(), pos, state);
    }

    public void updateState() {

        //if the state is OFF and there is a net present, destroy and de-reference it.
        //that should be all, since the state being off also prevents the BE from updating and joining the new net.
        if(!this.isOn() && this.node != null) {
            Nodespace.destroyNode(level, this.getBlockPos());
            this.node = null;
        }
    }

    public boolean isOn() {
        BlockState state = this.getBlockState();
        return state.hasProperty(CableSwitchBlock.LIT) && state.getValue(CableSwitchBlock.LIT);
    }

    @Override
    public boolean shouldCreateNode() {
        return this.isOn();
    }
}
