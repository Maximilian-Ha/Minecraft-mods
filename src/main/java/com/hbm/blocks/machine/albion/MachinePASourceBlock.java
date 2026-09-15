package com.hbm.blocks.machine.albion;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.albion.MachinePASourceBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.albion.BlockPASource.
 *
 * Die Quelle steht quer zum Strahl: drei mal neun Bloecke, und der Strahl tritt seitlich aus,
 * fuenf Bloecke von der Mitte. Die zehn Anschlusspunkte liegen in einer Reihe davor und dahinter
 * und unter der Anlage.
 */
public class MachinePASourceBlock extends DummyableBlock implements ITooltipProvider {

    public static final MapCodec<MachinePASourceBlock> CODEC = simpleCodec(MachinePASourceBlock::new);

    public MachinePASourceBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachinePASourceBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachinePASourceBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 1, 1, 1, 1, 4, 4 }; }
    @Override public int getOffset() { return 0; }
    @Override public int getHeightOffset() { return 1; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);
        Direction rot = dir.getClockWise(Axis.Y);

        /* Der Strahlaustritt, fuenf Bloecke seitlich. */
        this.makeExtra(level, core.relative(rot, 4));

        this.makeExtra(level, core.relative(dir));
        this.makeExtra(level, core.relative(dir).relative(rot, 2));
        this.makeExtra(level, core.relative(dir).relative(rot, -2));
        this.makeExtra(level, core.relative(dir, -1));
        this.makeExtra(level, core.relative(dir, -1).relative(rot, 2));
        this.makeExtra(level, core.relative(dir, -1).relative(rot, -2));

        this.makeExtra(level, core.below());
        this.makeExtra(level, core.below().relative(rot, 2));
        this.makeExtra(level, core.below().relative(rot, -2));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
