package com.hbm.blocks.turret;

import com.hbm.blockentity.turret.TurretSentryDamagedBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Der zerschossene Wachturm. Er unterscheidet sich vom heilen nur in der Blockentitaet -- und
 * damit in allem, was ihn ausmacht: kein Strom, kein Schalter, kein zweiter Lauf.
 */
public class TurretSentryDamagedBlock extends TurretSentryBlock {

    public TurretSentryDamagedBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<TurretSentryDamagedBlock> DAMAGED_CODEC = simpleCodec(TurretSentryDamagedBlock::new);
    @Override public MapCodec<? extends TurretSentryBlock> codec() { return DAMAGED_CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TurretSentryDamagedBlockEntity(pos, state);
    }
}
