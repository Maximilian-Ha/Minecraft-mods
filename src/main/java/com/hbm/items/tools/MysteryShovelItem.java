package com.hbm.items.tools;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.ItemEnums.U238M2Type;
import com.hbm.items.NtmItems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.tool.ItemMS.
 *
 * Der bruechige Spaten. Er taugt zu genau einer Sache: auf die Erde der Bauwerke geschlagen
 * holt er drei Ostereier heraus und laesst den Block verschwinden. Auf jedem anderen Block tut
 * er nichts.
 *
 * WAS ER FINDET, sind die Spielarten eins bis drei von ingot_u238m2 -- im Original die
 * Metadaten desselben Gegenstands, im Port dieselben Ordnungszahlen. Ein Barren ist keines
 * davon; die drei tragen nur seine Kennung.
 */
public class MysteryShovelItem extends Item {

    public MysteryShovelItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if(level.getBlockState(pos).getBlock() != NtmBlocks.NTM_DIRT.get()) return InteractionResult.PASS;
        if(level.isClientSide) return InteractionResult.SUCCESS;

        level.destroyBlock(pos, false);

        for(U238M2Type fund : new U238M2Type[] { U238M2Type.ELEMENTS, U238M2Type.ARSENIC, U238M2Type.VAULT }) {
            Block.popResource(level, pos, MetaHelper.newStack(NtmItems.INGOT_U238M2.get(), fund));
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("desc.item.mysteryshovel").withStyle(ChatFormatting.GRAY));
    }
}
