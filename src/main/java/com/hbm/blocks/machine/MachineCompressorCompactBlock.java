package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineCompressorCompactBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineCompressorCompact.
 * Abmessungen und Extra-Positionen unveraendert uebernommen.
 */
public class MachineCompressorCompactBlock extends DummyableBlock {

    public MachineCompressorCompactBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new MachineCompressorCompactBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    public static final MapCodec<MachineCompressorCompactBlock> CODEC = simpleCodec(MachineCompressorCompactBlock::new);
    @Override public MapCodec<MachineCompressorCompactBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] {2, 0, 1, 1, 3, 3}; }
    @Override public int getOffset() { return 1; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos corePos = pos.relative(dir, offset);
        Direction rot = dir.getCounterClockWise();

        this.makeExtra(level, corePos.relative(rot, 3).above());
        this.makeExtra(level, corePos.relative(rot.getOpposite(), 3).above());
        this.makeExtra(level, corePos.relative(dir).relative(rot).above());
        this.makeExtra(level, corePos.relative(dir).relative(rot.getOpposite()).above());
        this.makeExtra(level, corePos.relative(dir.getOpposite()).relative(rot).above());
        this.makeExtra(level, corePos.relative(dir.getOpposite()).relative(rot.getOpposite()).above());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }
}
