package com.hbm.blocks.generic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.DecoBlock, soweit sie steel_beam betrifft.
 *
 * Der Stahltraeger ist eine duenne Saeule, kein Wuerfel: (7|0|7)-(9|16|9), genau die Masse
 * aus setBlockBoundsBasedOnState des Originals. Sein Aussehen kommt aus beam.obj -- derselbe
 * Quader, den RenderSteelBeam dort zeichnet.
 *
 * BERICHTIGUNG: der Port hatte ihn seit Runde 9 als vollen Wuerfel. Die Roadmap-Zeile, er sei
 * "im Original ein schlichter Block", stimmte nicht.
 */
public class SteelBeamBlock extends Block {

    public static final MapCodec<SteelBeamBlock> CODEC = simpleCodec(SteelBeamBlock::new);

    private static final VoxelShape SHAPE = Block.box(7, 0, 7, 9, 16, 9);

    public SteelBeamBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<SteelBeamBlock> codec() { return CODEC; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
