package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.MachineKeyForgeBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineKeyForge.
 * Ein Block ohne Ausrichtung, oben und unten mit eigenem Bild.
 */
public class MachineKeyForgeBlock extends BaseEntityBlock {

    public static final MapCodec<MachineKeyForgeBlock> CODEC = simpleCodec(MachineKeyForgeBlock::new);

    public MachineKeyForgeBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineKeyForgeBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachineKeyForgeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.SUCCESS;

        if(level.getBlockEntity(pos) instanceof MenuProvider provider) player.openMenu(provider, pos);

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {

        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MachineKeyForgeBlockEntity forge) {
            Containers.dropContents(level, pos, forge);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
}
