package com.hbm.items.tools;

import com.hbm.blockentity.machine.rbmk.CraneConsoleBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKConsoleBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKDisplayBlockEntity;
import com.hbm.blocks.machine.rbmk.RBMKBaseBlock;
import com.hbm.blocks.machine.rbmk.RBMKConsoleBlock;
import com.hbm.blocks.machine.rbmk.RBMKDisplayBlock;
import com.hbm.blocks.machine.rbmk.RBMKCraneConsoleBlock;
import com.hbm.items.component.NtmDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.tool.ItemRBMKTool.
 *
 * Der Verbindungsstab. Ein Rechtsklick auf eine beliebige RBMK-Saeule merkt sich deren Kern,
 * ein zweiter auf ein Reaktorpult setzt dessen Zielpunkt darauf.
 *
 * Abweichung vom Original: die gemerkte Position liegt in einer Datenkomponente statt in losem
 * NBT. Kranpult und Anzeigetafel sind seit Runde 47 mit dabei.
 */
public class RBMKLinkItem extends Item {

    public RBMKLinkItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if(player == null) return InteractionResult.PASS;

        if(level.getBlockState(pos).getBlock() instanceof RBMKBaseBlock column) {

            BlockPos corePos = column.findCore(level, pos);
            if(corePos == null) return InteractionResult.PASS;

            if(!level.isClientSide) {
                stack.set(NtmDataComponents.RBMK_LINK.get(), corePos);
                player.displayClientMessage(Component.translatable("item.hbmsntm.rbmk_link.linked",
                        corePos.getX(), corePos.getY(), corePos.getZ()).withStyle(ChatFormatting.YELLOW), true);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if(level.getBlockState(pos).getBlock() instanceof RBMKCraneConsoleBlock crane) {

            BlockPos target = stack.get(NtmDataComponents.RBMK_LINK.get());
            if(target == null) return InteractionResult.PASS;

            BlockPos corePos = crane.findCore(level, pos);
            if(corePos == null) return InteractionResult.PASS;

            if(!level.isClientSide && level.getBlockEntity(corePos) instanceof CraneConsoleBlockEntity be) {
                be.setTarget(target);
                player.displayClientMessage(Component.translatable("item.hbmsntm.rbmk_link.set").withStyle(ChatFormatting.YELLOW), true);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if(level.getBlockState(pos).getBlock() instanceof RBMKDisplayBlock) {

            BlockPos target = stack.get(NtmDataComponents.RBMK_LINK.get());
            if(target == null) return InteractionResult.PASS;

            /* Die Rasteranzeige ist ein einzelner Block, es gibt also keinen Kern zu suchen. */
            if(!level.isClientSide && level.getBlockEntity(pos) instanceof RBMKDisplayBlockEntity be) {
                be.setTarget(target);
                player.displayClientMessage(Component.translatable("item.hbmsntm.rbmk_link.set").withStyle(ChatFormatting.YELLOW), true);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if(level.getBlockState(pos).getBlock() instanceof RBMKConsoleBlock console) {

            BlockPos target = stack.get(NtmDataComponents.RBMK_LINK.get());
            if(target == null) return InteractionResult.PASS;

            BlockPos corePos = console.findCore(level, pos);
            if(corePos == null) return InteractionResult.PASS;

            if(!level.isClientSide && level.getBlockEntity(corePos) instanceof RBMKConsoleBlockEntity be) {
                be.setTarget(target);
                player.displayClientMessage(Component.translatable("item.hbmsntm.rbmk_link.set").withStyle(ChatFormatting.YELLOW), true);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {

        list.add(Component.translatable("item.hbmsntm.rbmk_link.desc").withStyle(ChatFormatting.YELLOW));

        BlockPos target = stack.get(NtmDataComponents.RBMK_LINK.get());
        if(target != null) {
            list.add(Component.translatable("item.hbmsntm.rbmk_link.target",
                    target.getX(), target.getY(), target.getZ()).withStyle(ChatFormatting.GRAY));
        }
    }
}
