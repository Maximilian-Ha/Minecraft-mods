package com.hbm.blocks.turret;

import com.hbm.blockentity.turret.TurretFriendlyBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Der 5,56-mm-Begleitturm. */
public class TurretFriendlyBlock extends TurretBaseNTBlock {

    public TurretFriendlyBlock(Properties properties) { super(properties); }

    public static final MapCodec<TurretFriendlyBlock> CODEC = simpleCodec(TurretFriendlyBlock::new);
    @Override public MapCodec<? extends TurretBaseNTBlock> codec() { return CODEC; }

    @Override
    protected BlockEntity newTurret(BlockPos pos, BlockState state) {
        return new TurretFriendlyBlockEntity(pos, state);
    }
}
