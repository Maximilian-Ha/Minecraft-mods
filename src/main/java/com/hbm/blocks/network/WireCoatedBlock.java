package com.hbm.blocks.network;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.network.CableBaseBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.WireCoated.
 *
 * Schlichter Kabelblock in voller Wuerfelform, der wie das rote Kabel ans Stromnetz
 * angeschlossen ist. Weggelassen: die Connected-Textures des Originals (CT/IBlockCT),
 * dafuer gibt es im Port keine Entsprechung -- der Block nutzt schlicht red_wire_coated.
 */
public class WireCoatedBlock extends Block implements EntityBlock {

    public static final MapCodec<WireCoatedBlock> CODEC = simpleCodec(WireCoatedBlock::new);

    public WireCoatedBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<WireCoatedBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBaseBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }
}
