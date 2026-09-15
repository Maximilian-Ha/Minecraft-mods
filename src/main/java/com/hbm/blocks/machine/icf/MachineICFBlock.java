package com.hbm.blocks.machine.icf;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.icf.ICFBlockEntity;
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

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineICF.
 *
 * Die zusammengebaute Brennkammer. Sie ist siebzehn Bloecke lang und sechs hoch; die beiden
 * seitlichen Ausbuchtungen auf halber Hoehe traegt das Original ueber zwei zusaetzliche Bereiche
 * nach.
 */
public class MachineICFBlock extends DummyableBlock {

    /* Die beiden seitlichen Ausbuchtungen, die das Original ueber getAllDimensions mitbelegt. */
    private static final int[] DIM_LEFT = new int[] {1, 1, -1, 2, 8, 8};
    private static final int[] DIM_RIGHT = new int[] {1, 1, 2, -1, 8, 8};

    public MachineICFBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new ICFBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] {5, 0, 1, 1, 8, 8}; }
    @Override public int getOffset() { return 1; }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {

        if(!super.checkRequirement(level, pos, dir, offset)) return false;

        BlockPos corePos = pos.relative(dir, offset).above(3);

        return MultiblockHandlerXR.checkSpace(level, corePos, DIM_LEFT, pos, dir)
                && MultiblockHandlerXR.checkSpace(level, corePos, DIM_RIGHT, pos, dir);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos corePos = pos.relative(dir, offset);
        BlockPos sidePos = corePos.above(3);

        MultiblockHandlerXR.fillSpace(level, sidePos, DIM_LEFT, this, dir);
        MultiblockHandlerXR.fillSpace(level, sidePos, DIM_RIGHT, this, dir);

        Direction rot = dir.getClockWise();

        /* Die sechs Anschlussstellen: eine oben, vier an den Ausbuchtungen. */
        this.makeExtra(level, corePos.above(5));
        this.makeExtra(level, corePos.offset(dir.getStepX() * 2 + rot.getStepX() * 6, 3, dir.getStepZ() * 2 + rot.getStepZ() * 6));
        this.makeExtra(level, corePos.offset(dir.getStepX() * 2 - rot.getStepX() * 6, 3, dir.getStepZ() * 2 - rot.getStepZ() * 6));
        this.makeExtra(level, corePos.offset(-dir.getStepX() * 2 + rot.getStepX() * 6, 3, -dir.getStepZ() * 2 + rot.getStepZ() * 6));
        this.makeExtra(level, corePos.offset(-dir.getStepX() * 2 - rot.getStepX() * 6, 3, -dir.getStepZ() * 2 - rot.getStepZ() * 6));
    }

    /**
     * Baut die Kammer an Ort und Stelle auf. Der Zusammenbauklotz benutzt das, nachdem er die
     * Wand geprueft hat -- er steht dabei einen Block vor dem Kern.
     */
    public void assemble(Level level, BlockPos pos, Direction dir) {
        this.fillSpace(level, pos, dir, -this.getOffset());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    public static final MapCodec<MachineICFBlock> CODEC = simpleCodec(MachineICFBlock::new);
    @Override public MapCodec<MachineICFBlock> codec() { return CODEC; }
}
