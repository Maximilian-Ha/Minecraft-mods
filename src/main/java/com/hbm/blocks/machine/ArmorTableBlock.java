package com.hbm.blocks.machine;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.menus.ArmorTableMenu;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.BlockArmorTable.
 *
 * Der Ruestungstisch. Wie der Waffentisch hat er keine Blockentitaet -- was darauf liegt,
 * gehoert dem Menue und faellt beim Schliessen heraus.
 */
public class ArmorTableBlock extends Block implements ITooltipProvider {

    public static final MapCodec<ArmorTableBlock> CODEC = simpleCodec(ArmorTableBlock::new);

    public ArmorTableBlock(Properties properties) {
        super(properties);
    }

    @Override protected MapCodec<? extends Block> codec() { return CODEC; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {

        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.PASS;

        player.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> new ArmorTableMenu(id, inventory),
                Component.translatable("container.armorTable")), pos);

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
