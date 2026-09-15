package com.hbm.blockentity.network;

import api.hbm.energymk2.Nodespace.PowerNode;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.lib.Library;
import com.hbm.util.Vec3NT;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Umspannwerk. Nimmt die Vierfachleitung des grossen Mastes auf und gibt den Strom
 * ueber die vier Eckbloecke an Kabel weiter.
 */
public class SubstationBlockEntity extends PylonBaseBlockEntity {

    public SubstationBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.NETWORK_SUBSTATION.get(), pos, state);
    }

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.QUAD;
    }

    @Override
    public Vec3NT[] getMountPos() {

        double topOff = 5.25;
        Vec3NT vec = new Vec3NT(1, 0, 0);

        // im Original die Metadaten minus BlockDummyable.offset
        switch(this.getBlockState().getValue(DummyableBlock.FACING)) {
        case NORTH: vec.rotateAroundYRad(Math.PI * 0.0D); break;
        case WEST: vec.rotateAroundYRad(Math.PI * 0.5D); break;
        case SOUTH: vec.rotateAroundYRad(Math.PI * 0.0D); break;
        case EAST: vec.rotateAroundYRad(Math.PI * 0.5D); break;
        default: break;
        }

        return new Vec3NT[] {
                new Vec3NT(0.5 + vec.xCoord * 0.5, topOff, 0.5 + vec.zCoord * 0.5),
                new Vec3NT(0.5 + vec.xCoord * 1.5, topOff, 0.5 + vec.zCoord * 1.5),
                new Vec3NT(0.5 - vec.xCoord * 0.5, topOff, 0.5 - vec.zCoord * 0.5),
                new Vec3NT(0.5 - vec.xCoord * 1.5, topOff, 0.5 - vec.zCoord * 1.5),
        };
    }

    @Override
    public Vec3NT getConnectionPoint() {
        BlockPos pos = this.getBlockPos();
        return new Vec3NT(pos.getX() + 0.5, pos.getY() + 5.25, pos.getZ() + 0.5);
    }

    @Override
    public double getMaxWireLength() {
        return 20;
    }

    @Override
    public double getRenderHeight() {
        return 6D;
    }

    @Override
    public PowerNode createNode() {
        BlockPos pos = this.getBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        PowerNode node = new PowerNode(
                pos,
                new BlockPos(x + 1, y, z + 1),
                new BlockPos(x + 1, y, z - 1),
                new BlockPos(x - 1, y, z + 1),
                new BlockPos(x - 1, y, z - 1)).setConnections(
                new DirPos(x, y, z, null),
                new DirPos(x + 2, y, z - 1, Library.POS_X),
                new DirPos(x + 2, y, z + 1, Library.POS_X),
                new DirPos(x - 2, y, z - 1, Library.NEG_X),
                new DirPos(x - 2, y, z + 1, Library.NEG_X),
                new DirPos(x - 1, y, z + 2, Library.POS_Z),
                new DirPos(x + 1, y, z + 2, Library.POS_Z),
                new DirPos(x - 1, y, z - 2, Library.NEG_Z),
                new DirPos(x + 1, y, z - 2, Library.NEG_Z)
                );
        for(BlockPos con : this.connected) node.addConnection(new DirPos(con, null));
        return node;
    }
}
