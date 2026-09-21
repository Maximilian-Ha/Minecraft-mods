package com.hbm.blocks.bomb;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
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

/**
 * Portiert aus 1.7.10: com.hbm.blocks.bomb.BlockChargeSemtex. Bergbauladung mit Gluecksstufe 3.
 *
 * SIE SETZT KEINEN ENTITAETEN-VERARBEITER, und das ist kein Versehen des Ports: das Original
 * tut es auch nicht. Wer danebensteht, nimmt keinen Schaden -- genau das versprechen auch die
 * Hinweiszeilen des Gegenstands.
 */
public class ChargeSemtexBlock extends ChargeBaseBlock {

    public ChargeSemtexBlock(Properties properties) { super(properties); }

    public static final MapCodec<ChargeSemtexBlock> CODEC = simpleCodec(ChargeSemtexBlock::new);
    @Override public MapCodec<ChargeSemtexBlock> codec() { return CODEC; }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {

        if(level.isClientSide) return BombReturnCode.UNDEFINED;

        sicher = true;
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        sicher = false;

        ExplosionVNT vnt = new ExplosionVNT(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10F);
        vnt.setBlockAllocator(new BlockAllocatorStandard(32));
        vnt.setBlockProcessor(new BlockProcessorStandard().setAllDrop().setFortune(3));
        vnt.explode();
        ExplosionCreator.composeEffectSmall(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);

        return BombReturnCode.DETONATED;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, context, components, flag);
        components.add(Component.literal("Will drop all blocks.").withStyle(ChatFormatting.BLUE));
        components.add(Component.literal("Does not do damage.").withStyle(ChatFormatting.BLUE));
        components.add(Component.literal("").withStyle(ChatFormatting.BLUE));
        components.add(Component.literal("Fortune III").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
