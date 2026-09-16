package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.oil.MachinePyroOvenBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachinePyroOven.
 *
 * Ein langgestreckter Kasten: sieben Bloecke in Blickrichtung, fuenf quer, drei hoch. Die
 * Anschluesse liegen als Reihe an der einen Laengsseite, der Schornstein oben auf der anderen.
 */
public class MachinePyroOvenBlock extends DummyableBlock {

    public MachinePyroOvenBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachinePyroOvenBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    public static final MapCodec<MachinePyroOvenBlock> CODEC = simpleCodec(MachinePyroOvenBlock::new);
    @Override public MapCodec<MachinePyroOvenBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] {2, 0, 3, 3, 2, 2}; }
    @Override public int getOffset() { return 3; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);
        Direction rot = dir.getCounterClockWise();

        /* Die fuenf Anschlussbloecke nebeneinander an der Laengsseite. */
        for(int i = -2; i <= 2; i++) {
            this.makeExtra(level, core.relative(dir, i).relative(rot, 2));
        }

        /* Der Schornstein auf der anderen Seite, zwei Bloecke ueber dem Kern. */
        this.makeExtra(level, core.above(2).relative(rot.getOpposite()));
    }
}
