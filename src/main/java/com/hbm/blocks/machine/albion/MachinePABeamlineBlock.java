package com.hbm.blocks.machine.albion;

import api.hbm.block.IToolable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.albion.MachinePABeamlineBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.albion.BlockPABeamline.
 *
 * Drei Bloecke lang, ein Block breit -- das Stueck, aus dem der ganze Ring besteht. Der
 * Schraubenzieher oeffnet das Fenster, durch das man den Strahl sieht.
 */
public class MachinePABeamlineBlock extends DummyableBlock implements ITooltipProvider, IToolable {

    public static final MapCodec<MachinePABeamlineBlock> CODEC = simpleCodec(MachinePABeamlineBlock::new);

    public MachinePABeamlineBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachinePABeamlineBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(TYPE) == DummyBlockType.CORE ? new MachinePABeamlineBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 0, 0, 0, 0, 1, 1 }; }
    @Override public int getOffset() { return 0; }

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {

        if(tool != ToolType.SCREWDRIVER) return false;
        if(level.isClientSide) return true;

        BlockPos core = this.findCore(level, pos);
        if(core != null && level.getBlockEntity(core) instanceof MachinePABeamlineBlockEntity beamline) {
            beamline.window = !beamline.window;
            beamline.setChanged();
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
