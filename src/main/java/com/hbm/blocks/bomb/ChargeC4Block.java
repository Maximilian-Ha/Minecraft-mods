package com.hbm.blocks.bomb;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.particle.helper.ExplosionCreator;

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

/** Portiert aus 1.7.10: com.hbm.blocks.bomb.BlockChargeC4. Starke Ladung, laesst nichts fallen. */
public class ChargeC4Block extends ChargeBaseBlock {

    public ChargeC4Block(Properties properties) { super(properties); }

    public static final MapCodec<ChargeC4Block> CODEC = simpleCodec(ChargeC4Block::new);
    @Override public MapCodec<ChargeC4Block> codec() { return CODEC; }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {

        if(level.isClientSide) return BombReturnCode.UNDEFINED;

        sicher = true;
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        sicher = false;

        ExplosionVNT vnt = new ExplosionVNT(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15F);
        vnt.setBlockAllocator(new BlockAllocatorStandard(32));
        vnt.setBlockProcessor(new BlockProcessorStandard().setNoDrop());
        vnt.setEntityProcessor(new EntityProcessorStandard());
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();
        ExplosionCreator.composeEffectSmall(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);

        return BombReturnCode.DETONATED;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, context, components, flag);
        components.add(Component.literal("Does not drop blocks.").withStyle(ChatFormatting.BLUE));
    }
}
