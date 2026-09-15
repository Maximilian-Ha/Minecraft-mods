package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineIntakeBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineIntake.
 *
 * Das Geblaese ist zwei mal zwei Bloecke gross.
 */
public class MachineIntakeBlock extends DummyableBlock {

    public static final MapCodec<MachineIntakeBlock> CODEC = simpleCodec(MachineIntakeBlock::new);

    public MachineIntakeBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineIntakeBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineIntakeBlockEntity(pos, state);
            default -> new ProxyComboBlockEntity(pos, state).fluid().power();
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 0, 0, 1, 0, 1, 0 }; }
    @Override public int getOffset() { return 0; }
}
