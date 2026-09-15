package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineCombustionEngineBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineCombustionEngine.
 *
 * Multiblock (im Original BlockDummyable) mit vier Huellbloecken an den
 * Laengsseiten, ueber die Strom und Fluide angeschlossen werden.
 * Abmessungen und Versatz unveraendert aus dem Original.
 */
public class MachineCombustionEngineBlock extends DummyableBlock {

    public static final MapCodec<MachineCombustionEngineBlock> CODEC = simpleCodec(MachineCombustionEngineBlock::new);

    public MachineCombustionEngineBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineCombustionEngineBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new MachineCombustionEngineBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] {1, 0, 1, 0, 3, 2}; }
    @Override public int getOffset() { return 0; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);
        Direction rot = dir.getClockWise(Axis.Y);

        this.makeExtra(level, core.offset(rot.getStepX(), 0, rot.getStepZ()));
        this.makeExtra(level, core.offset(-rot.getStepX(), 0, -rot.getStepZ()));
        this.makeExtra(level, core.offset(-dir.getStepX() + rot.getStepX(), 0, -dir.getStepZ() + rot.getStepZ()));
        this.makeExtra(level, core.offset(-dir.getStepX() - rot.getStepX(), 0, -dir.getStepZ() - rot.getStepZ()));
    }
}
