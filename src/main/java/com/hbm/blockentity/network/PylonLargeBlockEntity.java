package com.hbm.blockentity.network;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.util.Vec3NT;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Grosser Mast (red_pylon_large). Vierfachleitung, 100m Reichweite, braucht ein Umspannwerk,
 * um Strom abzugeben -- er selbst hat keine Kabelanschluesse.
 */
public class PylonLargeBlockEntity extends PylonBaseBlockEntity {

    public PylonLargeBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.NETWORK_PYLON_LARGE.get(), pos, state);
    }

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.QUAD;
    }

    @Override
    public Vec3NT[] getMountPos() {
        double topOff = 0.75 + 0.0625;
        double sideOff = 3.375;

        Vec3NT vec = new Vec3NT(sideOff, 0, 0);

        // im Original die Metadaten minus BlockDummyable.offset; der grosse Mast steht in 45-Grad-Schritten
        switch(this.getBlockState().getValue(DummyableBlock.FACING)) {
        case NORTH: vec.rotateAroundYRad(Math.PI * 0.0D); break;
        case WEST: vec.rotateAroundYRad(Math.PI * 0.25D); break;
        case SOUTH: vec.rotateAroundYRad(Math.PI * 0.5D); break;
        case EAST: vec.rotateAroundYRad(Math.PI * 0.75D); break;
        default: break;
        }

        return new Vec3NT[] {
                new Vec3NT(0.5 + vec.xCoord, 11.5 + topOff, 0.5 + vec.zCoord),
                new Vec3NT(0.5 + vec.xCoord, 11.5 - topOff, 0.5 + vec.zCoord),
                new Vec3NT(0.5 - vec.xCoord, 11.5 + topOff, 0.5 - vec.zCoord),
                new Vec3NT(0.5 - vec.xCoord, 11.5 - topOff, 0.5 - vec.zCoord),
        };
    }

    @Override
    public double getMaxWireLength() {
        return 100;
    }

    @Override
    public double getRenderHeight() {
        return 14D;
    }
}
