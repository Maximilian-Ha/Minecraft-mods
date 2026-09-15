package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineRockMillBlockEntity;
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

public class MachineRockMillBlock extends DummyableBlock {

    public MachineRockMillBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new MachineRockMillBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    public static final MapCodec<MachineRockMillBlock> CODEC = simpleCodec(MachineRockMillBlock::new);
    @Override public MapCodec<MachineRockMillBlock> codec() { return CODEC; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override public int[] getDimensions() { return new int[] {2, 0, 2, 2, 2, 2}; }
    @Override public int getOffset() { return 2; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos center = pos.relative(dir, offset);
        int x = center.getX();
        int y = center.getY();
        int z = center.getZ();

        // die acht Anschlussecken des 5x5-Grundrisses
        this.makeExtra(level, new BlockPos(x + 2, y, z + 1));
        this.makeExtra(level, new BlockPos(x - 2, y, z + 1));
        this.makeExtra(level, new BlockPos(x + 2, y, z - 1));
        this.makeExtra(level, new BlockPos(x - 2, y, z - 1));
        this.makeExtra(level, new BlockPos(x + 1, y, z + 2));
        this.makeExtra(level, new BlockPos(x + 1, y, z - 2));
        this.makeExtra(level, new BlockPos(x - 1, y, z + 2));
        this.makeExtra(level, new BlockPos(x - 1, y, z - 2));
    }
}
