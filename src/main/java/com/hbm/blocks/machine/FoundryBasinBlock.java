package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.FoundryBasinBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.FoundryBasin.
 *
 * Das grosse Giessbecken: ein Trog ueber die volle Blockhoehe.
 *
 * ABWEICHUNG: dieselbe wie bei der Giessform -- aus fuenf Kollisionskaesten wird eine
 * VoxelShape. Der Umriss geht dabei auf die vollen sechzehn Sechzehntel; der Kniff des
 * Originals, die Oberkante bei 0.999 zu lassen, damit Gegenstaende nicht durchfallen, ist auf
 * 1.21 nicht mehr noetig.
 */
public class FoundryBasinBlock extends FoundryCastingBaseBlock {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 2, 16),
            Block.box(0, 0, 0, 16, 16, 2),
            Block.box(0, 0, 0, 2, 16, 16),
            Block.box(14, 0, 0, 16, 16, 16),
            Block.box(0, 0, 14, 16, 16, 16));

    public FoundryBasinBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<FoundryBasinBlock> CODEC = simpleCodec(FoundryBasinBlock::new);
    @Override public MapCodec<FoundryBasinBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundryBasinBlockEntity(pos, state);
    }

    @Override protected double getDropHeight() { return 1.0D; }

    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) { return SHAPE; }
}
