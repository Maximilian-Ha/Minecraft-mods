package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.FoundryMoldBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.FoundryMold.
 *
 * Die kleine Giessform: ein flacher Trog, einen halben Block hoch.
 *
 * ABWEICHUNG: das Original setzt fuenf einzelne Kollisionskaesten (Boden und vier Waende),
 * damit man in den Trog hineingreifen kann. Auf 1.21 leistet dasselbe eine VoxelShape aus
 * denselben fuenf Kaesten.
 */
public class FoundryMoldBlock extends FoundryCastingBaseBlock {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 2, 16),
            Block.box(0, 0, 0, 16, 8, 2),
            Block.box(0, 0, 0, 2, 8, 16),
            Block.box(14, 0, 0, 16, 8, 16),
            Block.box(0, 0, 14, 16, 8, 16));

    private static final VoxelShape OUTLINE = Block.box(0, 0, 0, 16, 8, 16);

    public FoundryMoldBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<FoundryMoldBlock> CODEC = simpleCodec(FoundryMoldBlock::new);
    @Override public MapCodec<FoundryMoldBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundryMoldBlockEntity(pos, state);
    }

    @Override protected double getDropHeight() { return 0.5D; }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) { return OUTLINE; }
    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) { return SHAPE; }
}
