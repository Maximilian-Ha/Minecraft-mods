package com.hbm.blocks.machine;

import api.hbm.block.IToolable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineEPressBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineEPress.
 *
 * Eine Grundflaeche von einem Block, drei Bloecke hoch -- der Stempel braucht Platz nach oben.
 *
 * DER HANDBOHRER TRENNT DIE AUFSAETZE AB. Ein Bohrer auf einen der beiden oberen Bloecke loest
 * ihn heraus, ohne die Maschine abzureissen; das Original nennt es ausdruecklich einen Streich
 * fuer Bauleute. Auf den Kern wirkt er nicht -- sonst bliebe eine Presse ohne Presse stehen.
 */
public class MachineEPressBlock extends DummyableBlock implements IToolable {

    public static final MapCodec<MachineEPressBlock> CODEC = simpleCodec(MachineEPressBlock::new);

    public MachineEPressBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineEPressBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineEPressBlockEntity(pos, state);
            default -> new ProxyComboBlockEntity(pos, state).inventory().power();
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

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {

        if(tool != ToolType.HAND_DRILL) return false;
        if(level.getBlockState(pos).getValue(TYPE) == DummyBlockType.CORE) return false;

        safeRem = true;
        level.removeBlock(pos, false);
        safeRem = false;

        return true;
    }

    @Override public int[] getDimensions() { return new int[] { 2, 0, 0, 0, 0, 0 }; }
    @Override public int getOffset() { return 0; }
}
