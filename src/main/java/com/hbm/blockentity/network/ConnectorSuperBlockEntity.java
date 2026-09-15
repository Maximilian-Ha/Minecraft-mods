package com.hbm.blockentity.network;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.network.ConnectorRedWireBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import com.hbm.util.Vec3NT;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityConnectorSuper.
 * Wie der normale Anschlusskasten, nur mit 100m Reichweite und weiter aussen
 * liegendem Aufhaengepunkt.
 */
public class ConnectorSuperBlockEntity extends ConnectorBlockEntity {

    public ConnectorSuperBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.NETWORK_CONNECTOR_SUPER.get(), pos, state);
    }

    @Override
    public Vec3NT[] getMountPos() {
        BlockState state = this.getBlockState();
        Direction dir = state.hasProperty(ConnectorRedWireBlock.FACING) ? state.getValue(ConnectorRedWireBlock.FACING) : Direction.UP;
        return new Vec3NT[] {new Vec3NT(0.5 + dir.getStepX() * 0.375, 0.5 + dir.getStepY() * 0.375, 0.5 + dir.getStepZ() * 0.375)};
    }

    @Override
    public double getMaxWireLength() {
        return 100;
    }
}
