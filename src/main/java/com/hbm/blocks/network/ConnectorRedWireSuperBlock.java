package com.hbm.blocks.network;

import com.hbm.blockentity.network.ConnectorSuperBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.ConnectorRedWireSuper.
 * Wie der einfache Anschlusskasten, aber der Kasten geht auf der Montageachse
 * durch den ganzen Block hindurch.
 */
public class ConnectorRedWireSuperBlock extends ConnectorRedWireBlock {

    public static final MapCodec<ConnectorRedWireSuperBlock> CODEC = simpleCodec(ConnectorRedWireSuperBlock::new);

    public ConnectorRedWireSuperBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends ConnectorRedWireSuperBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConnectorSuperBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        double pixel = 0.0625D;
        double min = pixel * 5D;
        double max = pixel * 11D;

        Direction dir = state.getValue(FACING).getOpposite();

        double minX = (dir == Direction.WEST || dir == Direction.EAST) ? 0D : min;
        double maxX = (dir == Direction.EAST || dir == Direction.WEST) ? 1D : max;
        double minY = (dir == Direction.DOWN || dir == Direction.UP) ? 0D : min;
        double maxY = (dir == Direction.UP || dir == Direction.DOWN) ? 1D : max;
        double minZ = (dir == Direction.NORTH || dir == Direction.SOUTH) ? 0D : min;
        double maxZ = (dir == Direction.SOUTH || dir == Direction.NORTH) ? 1D : max;

        return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
