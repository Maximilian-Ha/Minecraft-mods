package com.hbm.blocks.generic;

import com.hbm.blocks.NtmBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockDeadPlant.
 *
 * Totes Gewaechs, wie es in der Oednis der Bauwerke steht. Das Original haelt fuenf Formen in
 * den Metadaten eines Blocks; im Port sind es fuenf Bloecke, wie bei allen Metadatenfamilien.
 *
 * Wo es stehen darf, steht so in canPlaceBlockOn des Originals: auf Gras, Erde, Oedland,
 * oeligem und totem Boden -- und nur dort; faellt der Halt weg, faellt die Pflanze ab.
 */
public class DeadPlantBlock extends BushBlock {

    public static final MapCodec<DeadPlantBlock> CODEC = simpleCodec(DeadPlantBlock::new);

    public DeadPlantBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<DeadPlantBlock> codec() { return CODEC; }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(NtmBlocks.NTM_DIRT.get())
                || state.is(NtmBlocks.WASTE_EARTH.get())
                || state.is(NtmBlocks.DIRT_OILY.get())
                || state.is(NtmBlocks.DIRT_DEAD.get());
    }
}
