package com.hbm.blocks.turret;

import com.hbm.blockentity.turret.TurretHowardDamagedBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Das verrostete Nahbereichsgeschuetz -- es laesst sich nicht oeffnen. */
public class TurretHowardDamagedBlock extends TurretBaseNTBlock {

    public TurretHowardDamagedBlock(Properties properties) { super(properties); }

    public static final MapCodec<TurretHowardDamagedBlock> CODEC = simpleCodec(TurretHowardDamagedBlock::new);
    @Override public MapCodec<? extends TurretBaseNTBlock> codec() { return CODEC; }

    @Override
    protected BlockEntity newTurret(BlockPos pos, BlockState state) {
        return new TurretHowardDamagedBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return InteractionResult.SUCCESS;
    }
}
