package com.hbm.blocks.bomb;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.particle.helper.ExplosionSmallCreator;

import com.mojang.serialization.MapCodec;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.bomb.BlockChargeMiner. Die Ladung fuer den Stollen.
 *
 * DIE BEIDEN MERKMALE DES ORIGINALS sind zwei Fahnen an ExplosionNT: NOHURT und ALLDROP. Der
 * Port hat ExplosionNT nicht; NOHURT heisst hier "kein Entitaeten- und kein Spielerverarbeiter",
 * ALLDROP heisst setAllDrop(). Denselben Tausch nimmt der Port schon bei det_miner vor.
 */
public class ChargeMinerBlock extends ChargeBaseBlock {

    public ChargeMinerBlock(Properties properties) { super(properties); }

    public static final MapCodec<ChargeMinerBlock> CODEC = simpleCodec(ChargeMinerBlock::new);
    @Override public MapCodec<ChargeMinerBlock> codec() { return CODEC; }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {

        if(level.isClientSide) return BombReturnCode.UNDEFINED;

        sicher = true;
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        sicher = false;

        ExplosionVNT vnt = new ExplosionVNT(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4F);
        vnt.setBlockAllocator(new BlockAllocatorStandard());
        vnt.setBlockProcessor(new BlockProcessorStandard().setAllDrop());
        vnt.explode();
        ExplosionSmallCreator.composeEffect(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15, 3F, 1.25F);

        return BombReturnCode.DETONATED;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, context, components, flag);
        components.add(Component.literal("Will drop all blocks.").withStyle(ChatFormatting.BLUE));
        components.add(Component.literal("Does not do damage.").withStyle(ChatFormatting.BLUE));
    }
}
