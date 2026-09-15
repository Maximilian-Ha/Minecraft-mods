package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.WatzStructBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.BlockWatzStruct.
 *
 * Der Klotz, um den herum ein Watz-Segment gemauert wird. Steht die Wand, verwandelt er sich
 * selbst in den Reaktorkern.
 */
public class WatzStructBlock extends BaseEntityBlock {

    public WatzStructBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WatzStructBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    /* Das Original ist kein voller Wuerfel; die Nachbarn zeichnen ihre Seiten also weiter. */
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    public static final MapCodec<WatzStructBlock> CODEC = simpleCodec(WatzStructBlock::new);
    @Override protected MapCodec<WatzStructBlock> codec() { return CODEC; }
}
