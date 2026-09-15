package com.hbm.blockentity.network;

import api.hbm.energymk2.Nodespace.PowerNode;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.lib.Library;
import com.hbm.util.Vec3NT;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kleiner Mast (red_pylon, red_pylon_steel). Einzelne Leitung, 25m Reichweite,
 * haengt an den Seiten direkt an Kabeln.
 *
 * Weggelassen: die Metadaten-Migration aus updateEntity (meta 0 -> 12). Im Port gibt es
 * keine Altstaende mit Metadaten, der Kern traegt seinen Zustand ueber DummyBlockType.CORE.
 */
public class PylonBlockEntity extends PylonBaseBlockEntity {

    public PylonBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.NETWORK_PYLON.get(), pos, state);
    }

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.SINGLE;
    }

    @Override
    public Vec3NT[] getMountPos() {
        return new Vec3NT[] {new Vec3NT(0.5, 5.5D, 0.5)};
    }

    @Override
    public double getMaxWireLength() {
        return 25;
    }

    @Override
    public double getRenderHeight() {
        return 6D;
    }

    @Override
    public boolean canConnect(Direction dir) {
        return dir != Direction.DOWN;
    }

    @Override
    public PowerNode createNode() {
        BlockPos pos = this.getBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        PowerNode node = new PowerNode(pos).setConnections(
                new DirPos(x, y, z, null),
                new DirPos(x + 1, y, z, Library.POS_X),
                new DirPos(x - 1, y, z, Library.NEG_X),
                new DirPos(x, y, z + 1, Library.POS_Z),
                new DirPos(x, y, z - 1, Library.NEG_Z)
                );
        for(BlockPos con : this.connected) node.addConnection(new DirPos(con, null));
        return node;
    }
}
