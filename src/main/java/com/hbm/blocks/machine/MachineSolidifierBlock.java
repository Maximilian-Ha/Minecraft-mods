package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.oil.MachineSolidifierBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineSolidifier.
 *
 * Vier Bloecke hoch auf einem Feld. Die Anschluesse liegen nicht am Kern, sondern an
 * der Haube oben und an den vier Auslegern auf mittlerer Hoehe -- deshalb werden
 * genau diese fuenf Stellen zu Anschlussbloecken aufgewertet.
 */
public class MachineSolidifierBlock extends DummyableBlock {

    public MachineSolidifierBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineSolidifierBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    public static final MapCodec<MachineSolidifierBlock> CODEC = simpleCodec(MachineSolidifierBlock::new);
    @Override public MapCodec<MachineSolidifierBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] {3, 0, 1, 1, 1, 1}; }
    @Override public int getOffset() { return 1; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);

        this.makeExtra(level, core.above(3));

        BlockPos mid = core.above();
        this.makeExtra(level, mid.east());
        this.makeExtra(level, mid.west());
        this.makeExtra(level, mid.south());
        this.makeExtra(level, mid.north());
    }
}
