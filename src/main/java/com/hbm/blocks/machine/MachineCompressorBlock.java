package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineCompressorBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.handler.MultiblockHandlerXR;
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

public class MachineCompressorBlock extends DummyableBlock {

    /** Zusaetzlicher Turm und Ausleger, die das Original ueber getAllDimensions mitbelegt */
    private static final int[] DIM_TOWER = new int[] {3, -3, 1, 1, 1, 1};
    private static final int[] DIM_SHAFT = new int[] {8, -4, 0, 0, 1, 1};

    public MachineCompressorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new MachineCompressorBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).fluid().power();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    public static final MapCodec<MachineCompressorBlock> CODEC = simpleCodec(MachineCompressorBlock::new);
    @Override public MapCodec<MachineCompressorBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] {2, 0, 1, 2, 1, 1}; }
    @Override public int getOffset() { return 2; }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {
        return super.checkRequirement(level, pos, dir, offset)
                && MultiblockHandlerXR.checkSpace(level, pos.relative(dir, offset), DIM_TOWER, pos, dir)
                && MultiblockHandlerXR.checkSpace(level, pos.relative(dir, offset), DIM_SHAFT, pos, dir);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos corePos = pos.relative(dir, offset);

        MultiblockHandlerXR.fillSpace(level, corePos, DIM_TOWER, this, dir);
        MultiblockHandlerXR.fillSpace(level, corePos, DIM_SHAFT, this, dir);

        Direction rot = dir.getCounterClockWise();

        this.makeExtra(level, corePos.relative(dir.getOpposite()));
        this.makeExtra(level, corePos.relative(rot));
        this.makeExtra(level, corePos.relative(rot.getOpposite()));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }
}
