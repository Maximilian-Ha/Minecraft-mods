package com.hbm.blockentity.network;

import api.hbm.energymk2.Nodespace.PowerNode;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.network.ConnectorRedWireBlock;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.hbm.util.Vec3NT;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityConnector.
 *
 * Der Anschlusskasten haengt an einer Blockseite und verbindet das dort anliegende
 * Kabelnetz mit bis zu 10m langen Fernleitungen.
 */
public class ConnectorBlockEntity extends PylonBaseBlockEntity {

    public ConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.NETWORK_CONNECTOR.get(), pos, state);
    }

    public ConnectorBlockEntity(BlockEntityType<? extends ConnectorBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.SINGLE;
    }

    @Override
    public Vec3NT[] getMountPos() {
        return new Vec3NT[] {new Vec3NT(0.5, 0.5, 0.5)};
    }

    @Override
    public double getMaxWireLength() {
        return 10;
    }

    /** FACING ist die Seite, an der der Kasten klebt; die Gegenrichtung zeigt in den Traegerblock. */
    public Direction getMountDirection() {
        BlockState state = this.getBlockState();
        // Das Original faellt bei Metadaten 0 auf DOWN.getOpposite() == UP zurueck.
        if(!state.hasProperty(ConnectorRedWireBlock.FACING)) return Direction.UP;
        return state.getValue(ConnectorRedWireBlock.FACING).getOpposite();
    }

    @Override
    public PowerNode createNode() {
        BlockPos pos = this.getBlockPos();
        Direction dir = this.getMountDirection();
        // Erste Verbindung richtungslos (im Original ForgeDirection.UNKNOWN) fuer die Fernleitungen,
        // zweite in den Traegerblock hinein fuer das normale Kabelnetz.
        PowerNode node = new PowerNode(pos).setConnections(
                new DirPos(pos.getX(), pos.getY(), pos.getZ(), null),
                new DirPos(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ(), dir));
        for(BlockPos con : this.connected) node.addConnection(new DirPos(con, null));
        return node;
    }

    @Override
    public boolean canConnect(Direction dir) { //i've about had it with your fucking bullshit
        return this.getMountDirection() == dir;
    }
}
