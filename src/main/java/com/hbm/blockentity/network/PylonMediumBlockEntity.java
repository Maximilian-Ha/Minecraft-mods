package com.hbm.blockentity.network;

import api.hbm.energymk2.Nodespace.PowerNode;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.util.Vec3NT;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mittlerer Mast, vier Bloecke teilen sich diese Klasse: Holz/Stahl, je mit und ohne Transformator.
 * Dreifachleitung, 45m Reichweite. Nur die Transformatorvarianten haengen an Kabeln.
 */
public class PylonMediumBlockEntity extends PylonBaseBlockEntity {

    public PylonMediumBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.NETWORK_PYLON_MEDIUM.get(), pos, state);
    }

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.TRIPLE;
    }

    @Override
    public Vec3NT[] getMountPos() {

        Direction dir = this.getFacing();
        double height = 7.5D;

        return new Vec3NT[] {
                new Vec3NT(0.5, height, 0.5),
                new Vec3NT(0.5 + dir.getStepX(), height, 0.5 + dir.getStepZ()),
                new Vec3NT(0.5 + dir.getStepX() * 2, height, 0.5 + dir.getStepZ() * 2),
        };
    }

    @Override
    public double getMaxWireLength() {
        return 45;
    }

    @Override
    public double getRenderHeight() {
        return 8D;
    }

    @Override
    public boolean canConnect(Direction dir) {
        return this.hasTransformer() && this.getFacing().getOpposite() == dir;
    }

    @Override
    public PowerNode createNode() {
        BlockPos pos = this.getBlockPos();

        PowerNode node = new PowerNode(pos).setConnections(new DirPos(pos, null));
        for(BlockPos con : this.connected) node.addConnection(new DirPos(con, null));

        if(this.hasTransformer()) {
            Direction dir = this.getFacing().getOpposite();
            node.addConnection(new DirPos(pos.getX() + dir.getStepX(), pos.getY(), pos.getZ() + dir.getStepZ(), dir));
        }

        return node;
    }

    /** Blickrichtung des Kerns; im Original die Metadaten minus BlockDummyable.offset */
    public Direction getFacing() {
        return this.getBlockState().getValue(DummyableBlock.FACING);
    }

    public boolean hasTransformer() {
        Block block = this.getBlockState().getBlock();
        return block == NtmBlocks.RED_PYLON_MEDIUM_WOOD_TRANSFORMER.get() || block == NtmBlocks.RED_PYLON_MEDIUM_STEEL_TRANSFORMER.get();
    }
}
