package com.hbm.blocks.network;

import com.hbm.blockentity.network.PipeValveBlockEntity;
import com.hbm.registry.NtmSoundEvents;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.FluidSwitch.
 *
 * Dasselbe Ventil, aber vom Redstone geschaltet statt von Hand: liegt Strom an, ist es
 * offen. Die Blockentitaet ist dieselbe wie beim Handventil -- im Original wie hier.
 *
 * Von Hand laesst es sich nicht umlegen; das Signal hat das letzte Wort. Der
 * Fluidkennzeichner wirkt weiterhin, der kommt aus dem Rohr darunter.
 */
public class FluidSwitchBlock extends FluidValveBlock {

    public static final MapCodec<FluidSwitchBlock> CODEC = simpleCodec(FluidSwitchBlock::new);

    public FluidSwitchBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<FluidSwitchBlock> codec() { return CODEC; }

    @Override protected boolean vonHandZuDrehen() { return false; }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {

        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);

        if(level.isClientSide) return;

        boolean strom = level.hasNeighborSignal(pos);
        if(strom == state.getValue(OPEN)) return;

        level.setBlock(pos, state.setValue(OPEN, strom), Block.UPDATE_CLIENTS);
        level.playSound(null, pos, NtmSoundEvents.LEVER.get(), SoundSource.BLOCKS, 1.0F, strom ? 1.0F : 0.85F);

        if(level.getBlockEntity(pos) instanceof PipeValveBlockEntity ventil) ventil.updateState();
    }
}
