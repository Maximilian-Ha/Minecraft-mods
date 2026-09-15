package com.hbm.blocks.turret;

import com.hbm.blockentity.turret.TurretJeremyBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Der Kanonenturm. */
public class TurretJeremyBlock extends TurretBaseNTBlock {

    public TurretJeremyBlock(Properties properties) { super(properties); }

    public static final MapCodec<TurretJeremyBlock> CODEC = simpleCodec(TurretJeremyBlock::new);
    @Override public MapCodec<? extends TurretBaseNTBlock> codec() { return CODEC; }

    @Override
    protected BlockEntity newTurret(BlockPos pos, BlockState state) {
        return new TurretJeremyBlockEntity(pos, state);
    }
}
