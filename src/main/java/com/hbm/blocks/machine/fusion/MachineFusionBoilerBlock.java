package com.hbm.blocks.machine.fusion;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.fusion.FusionBoilerBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

/** Portiert aus 1.7.10: com.hbm.blocks.machine.fusion.MachineFusionBoiler. */
public class MachineFusionBoilerBlock extends DummyableBlock implements ITooltipProvider {

    public MachineFusionBoilerBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<MachineFusionBoilerBlock> CODEC = simpleCodec(MachineFusionBoilerBlock::new);
    @Override public MapCodec<MachineFusionBoilerBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new FusionBoilerBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 3, 0, 4, 4, 1, 1 }; }
    @Override public int getOffset() { return 4; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);
        Direction rot = dir.getClockWise();

        /* Vier Anschluesse fuer Wasser und Dampf, je zwei vorn und hinten. */
        this.makeExtra(level, core.offset(-dir.getStepX() + rot.getStepX(), 0, -dir.getStepZ() + rot.getStepZ()));
        this.makeExtra(level, core.offset(-dir.getStepX() - rot.getStepX(), 0, -dir.getStepZ() - rot.getStepZ()));
        this.makeExtra(level, core.offset(dir.getStepX() * 2 + rot.getStepX(), 0, dir.getStepZ() * 2 + rot.getStepZ()));
        this.makeExtra(level, core.offset(dir.getStepX() * 2 - rot.getStepX(), 0, dir.getStepZ() * 2 - rot.getStepZ()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
