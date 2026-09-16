package com.zuxelus.energycontrol.items;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.blocks.PanelThickness;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.ItemPanelToolkit.
 *
 * Das Tafelwerkzeug richtet die Bloecke dieses Mods neu aus: ein Klick auf eine Seite
 * dreht die Schauseite dorthin. Zeigt sie schon dorthin, stellt der Klick bei der
 * fortgeschrittenen Tafel die naechste Dicke ein.
 *
 * Unterschied zum Original: dort zerlegte der Klick auf die Schauseite den Block. Das ist
 * ueberraschend und kostet im Zweifel eine eingemessene Karte -- das Werkzeug tut hier
 * nichts, was sich nicht mit einem zweiten Klick zuruecknehmen liesse.
 *
 * Fremde Bloecke laesst es in Ruhe: gedreht wird nur, was zu diesem Mod gehoert.
 */
public class ItemPanelToolkit extends Item {

    public ItemPanelToolkit(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ec.panel_toolkit.info").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if(!isOurs(state.getBlock())) return InteractionResult.PASS;

        DirectionProperty facing = facingOf(state);
        if(facing == null) return InteractionResult.PASS;

        Direction side = context.getClickedFace();
        if(level.isClientSide) return InteractionResult.SUCCESS;

        if(state.getValue(facing) == side) {
            if(!state.hasProperty(PanelThickness.THICKNESS)) return InteractionResult.PASS;
            int next = PanelThickness.next(state.getValue(PanelThickness.THICKNESS));
            level.setBlock(pos, state.setValue(PanelThickness.THICKNESS, next), Block.UPDATE_ALL);
            return InteractionResult.CONSUME;
        }

        // Ein waagerecht stehender Block laesst sich nicht nach oben drehen.
        if(!facing.getPossibleValues().contains(side)) return InteractionResult.PASS;

        level.setBlock(pos, state.setValue(facing, side), Block.UPDATE_ALL);
        return InteractionResult.CONSUME;
    }

    private static boolean isOurs(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        return id != null && EnergyControl.MODID.equals(id.getNamespace());
    }

    private static DirectionProperty facingOf(BlockState state) {
        if(state.hasProperty(BlockStateProperties.FACING)) return BlockStateProperties.FACING;
        if(state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return BlockStateProperties.HORIZONTAL_FACING;
        return null;
    }
}
