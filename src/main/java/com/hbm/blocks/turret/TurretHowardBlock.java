package com.hbm.blocks.turret;

import com.hbm.blockentity.turret.TurretHowardBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Das Nahbereichsgeschuetz. */
public class TurretHowardBlock extends TurretBaseNTBlock {

    public TurretHowardBlock(Properties properties) { super(properties); }

    public static final MapCodec<TurretHowardBlock> CODEC = simpleCodec(TurretHowardBlock::new);
    @Override public MapCodec<? extends TurretBaseNTBlock> codec() { return CODEC; }

    @Override
    protected BlockEntity newTurret(BlockPos pos, BlockState state) {
        return new TurretHowardBlockEntity(pos, state);
    }
}
