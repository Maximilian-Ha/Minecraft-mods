package com.hbm.blocks.machine.fusion;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.fusion.FusionBreederBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/** Portiert aus 1.7.10: com.hbm.blocks.machine.fusion.MachineFusionBreeder. */
public class MachineFusionBreederBlock extends DummyableBlock implements ITooltipProvider {

    public MachineFusionBreederBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<MachineFusionBreederBlock> CODEC = simpleCodec(MachineFusionBreederBlock::new);
    @Override public MapCodec<MachineFusionBreederBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new FusionBreederBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override public int[] getDimensions() { return new int[] { 3, 0, 2, 2, 1, 1 }; }
    @Override public int getOffset() { return 2; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);
        Direction rot = dir.getClockWise();

        /* Vier Anschluesse an den Flanken, einer oben fuer den Plasmaarm. */
        this.makeExtra(level, core.offset(rot.getStepX(), 0, rot.getStepZ()));
        this.makeExtra(level, core.offset(-rot.getStepX(), 0, -rot.getStepZ()));
        this.makeExtra(level, core.offset(dir.getStepX() + rot.getStepX(), 0, dir.getStepZ() + rot.getStepZ()));
        this.makeExtra(level, core.offset(dir.getStepX() - rot.getStepX(), 0, dir.getStepZ() - rot.getStepZ()));
        this.makeExtra(level, core.offset(dir.getStepX() * 2, 2, dir.getStepZ() * 2));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
