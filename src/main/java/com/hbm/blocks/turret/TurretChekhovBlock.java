package com.hbm.blocks.turret;

import com.hbm.blockentity.turret.TurretChekhovBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Die .50-Gatling. */
public class TurretChekhovBlock extends TurretBaseNTBlock {

    public TurretChekhovBlock(Properties properties) { super(properties); }

    public static final MapCodec<TurretChekhovBlock> CODEC = simpleCodec(TurretChekhovBlock::new);
    @Override public MapCodec<? extends TurretBaseNTBlock> codec() { return CODEC; }

    @Override
    protected BlockEntity newTurret(BlockPos pos, BlockState state) {
        return new TurretChekhovBlockEntity(pos, state);
    }
}
