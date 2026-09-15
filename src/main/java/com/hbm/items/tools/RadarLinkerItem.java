package com.hbm.items.tools;

import com.hbm.blockentity.machine.RadarScreenBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.util.CompatExternal;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.tool.ItemRadarLinker.
 *
 * Merkt sich einen Radarschirm. Liegt er danach im neunten Fach eines Radars, schickt dieses
 * seine Funde dorthin.
 *
 * ER MERKT SICH IMMER DEN KERN, nicht den angeklickten Block: der Schirm ist zwei mal zwei gross,
 * und ein gemerkter Randblock zeigte spaeter ins Leere.
 */
public class RadarLinkerItem extends CoordinateItem {

    public RadarLinkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canGrabCoordinateHere(Level level, BlockPos pos) {
        return CompatExternal.getCoreFromPos(level, pos) instanceof RadarScreenBlockEntity;
    }

    @Override
    public BlockPos getCoordinates(Level level, BlockPos pos) {

        if(level.getBlockState(pos).getBlock() instanceof DummyableBlock dummyable) {
            BlockPos core = dummyable.findCore(level, pos);
            if(core != null) return core;
        }

        return pos;
    }

    @Override
    public void onTargetSet(Level level, BlockPos pos, @Nullable Player player) {
        if(player != null) {
            player.displayClientMessage(Component.translatable("radar.linked", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.YELLOW), true);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        BlockPos pos = getPosition(stack);

        if(pos == null) {
            components.add(Component.translatable("radar.linker.empty").withStyle(ChatFormatting.GRAY));
        } else {
            components.add(Component.translatable("radar.linker.target", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GRAY));
        }
    }
}
