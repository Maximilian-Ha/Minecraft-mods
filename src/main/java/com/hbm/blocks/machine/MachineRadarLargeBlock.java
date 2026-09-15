package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.MachineRadarLargeBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineRadarLarge.
 *
 * Bis auf die Blockentitaet ist es das gewoehnliche Radar -- dieselbe Oberflaeche, dieselbe
 * Bedienung. Nur was dahintersteht, sieht dreimal so weit. Die Oberflaeche wird deshalb nicht
 * ueberschrieben: sie nimmt ein MachineRadarBlockEntity, und das grosse ist eines.
 */
public class MachineRadarLargeBlock extends MachineRadarBlock {

    public static final MapCodec<MachineRadarLargeBlock> CODEC = simpleCodec(MachineRadarLargeBlock::new);

    public MachineRadarLargeBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineRadarLargeBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachineRadarLargeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }
}
