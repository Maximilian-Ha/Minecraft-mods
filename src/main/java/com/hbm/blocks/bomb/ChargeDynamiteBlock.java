package com.hbm.blocks.bomb;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.particle.helper.ExplosionSmallCreator;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.bomb.BlockChargeDynamite. Die kleine, billige Ladung.
 *
 * DAS ORIGINAL NIMMT HIER ExplosionNT, die der Port nicht hat. An ihre Stelle tritt ExplosionVNT
 * mit derselben Staerke und den Standardverarbeitern -- also dasselbe Verhalten: sie tut weh und
 * laesst fallen, was ueblich ist. Denselben Tausch nimmt der Port schon bei det_miner vor
 * (ExplosiveChargeBlock), hier steht er nur ausdruecklich dabei.
 */
public class ChargeDynamiteBlock extends ChargeBaseBlock {

    public ChargeDynamiteBlock(Properties properties) { super(properties); }

    public static final MapCodec<ChargeDynamiteBlock> CODEC = simpleCodec(ChargeDynamiteBlock::new);
    @Override public MapCodec<ChargeDynamiteBlock> codec() { return CODEC; }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {

        if(level.isClientSide) return BombReturnCode.UNDEFINED;

        sicher = true;
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        sicher = false;

        ExplosionVNT vnt = new ExplosionVNT(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4F);
        vnt.setBlockAllocator(new BlockAllocatorStandard());
        vnt.setBlockProcessor(new BlockProcessorStandard());
        vnt.setEntityProcessor(new EntityProcessorStandard());
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();
        ExplosionSmallCreator.composeEffect(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15, 3F, 1.25F);

        return BombReturnCode.DETONATED;
    }
}
