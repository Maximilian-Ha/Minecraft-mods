package com.hbm.blocks.generic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.DecoBlock, soweit sie steel_roof betrifft.
 *
 * Nur die Platte traegt: (0|0|0)-(16|1|16), genau die Masse aus setBlockBoundsBasedOnState
 * des Originals. Die beiden Streben darueber gehoeren zum Aussehen, nicht zur Kollision --
 * man laeuft durch sie hindurch, so wie im Original auch.
 */
public class SteelRoofBlock extends Block {

    public static final MapCodec<SteelRoofBlock> CODEC = simpleCodec(SteelRoofBlock::new);

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 1, 16);

    public SteelRoofBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<SteelRoofBlock> codec() { return CODEC; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
