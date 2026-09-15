package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachinePrecAssBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachinePrecAss.
 *
 * Das Original leitet den Block von MachineAssemblyMachine ab und tauscht nur die Blockentitaet
 * aus -- Bauflaeche, Anschluesse und Modell sind dieselben. Der Port schreibt die beiden
 * Methoden aus, statt zu erben: DummyableBlock im Port traegt ein MapCodec je Klasse, und ein
 * geerbtes simpleCodec zeigte auf die falsche.
 */
public class MachinePrecAssBlock extends DummyableBlock {

    public static final MapCodec<MachinePrecAssBlock> CODEC = simpleCodec(MachinePrecAssBlock::new);

    public MachinePrecAssBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachinePrecAssBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachinePrecAssBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override public int[] getDimensions() { return new int[] { 2, 0, 1, 1, 1, 1 }; }
    @Override public int getOffset() { return 1; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);

        int x = pos.getX() - dir.getStepX();
        int y = pos.getY();
        int z = pos.getZ() - dir.getStepZ();

        for(int i = -1; i <= 1; i++) for(int j = -1; j <= 1; j++) {
            if(i != 0 || j != 0) this.makeExtra(level, new BlockPos(x + i, y, z + j));
        }
    }
}
