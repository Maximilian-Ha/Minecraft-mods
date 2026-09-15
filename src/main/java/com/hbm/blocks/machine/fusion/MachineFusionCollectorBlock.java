package com.hbm.blocks.machine.fusion;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.fusion.FusionCollectorBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Portiert aus 1.7.10: com.hbm.blocks.machine.fusion.MachineFusionCollector. */
public class MachineFusionCollectorBlock extends DummyableBlock implements ITooltipProvider {

    public MachineFusionCollectorBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<MachineFusionCollectorBlock> CODEC = simpleCodec(MachineFusionCollectorBlock::new);
    @Override public MapCodec<MachineFusionCollectorBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(TYPE) == DummyBlockType.CORE ? new FusionCollectorBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 3, 0, 2, 1, 2, 2 }; }
    @Override public int getOffset() { return 1; }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
