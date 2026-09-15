package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineOreSlopperBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineOreSlopper.
 *
 * Eine lange Anlage: sieben Bloecke in Blickrichtung, drei quer, vier hoch. Der Kern sitzt drei
 * Bloecke vor dem gesetzten Block; acht weitere Stellen sind Anschluesse, damit Wasser hinein-
 * und Schlamm herauskommt, ohne dass man an den Kern heranmuss.
 */
public class MachineOreSlopperBlock extends DummyableBlock implements ITooltipProvider {

    public static final MapCodec<MachineOreSlopperBlock> CODEC = simpleCodec(MachineOreSlopperBlock::new);

    public MachineOreSlopperBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineOreSlopperBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineOreSlopperBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 3, 0, 3, 3, 1, 1 }; }
    @Override public int getOffset() { return 3; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);
        Direction rot = dir.getClockWise();

        this.makeExtra(level, core.relative(dir, 3));
        this.makeExtra(level, core.relative(dir.getOpposite(), 3));
        this.makeExtra(level, core.relative(rot));
        this.makeExtra(level, core.relative(rot.getOpposite()));
        this.makeExtra(level, core.relative(dir, 2).relative(rot));
        this.makeExtra(level, core.relative(dir, 2).relative(rot.getOpposite()));
        this.makeExtra(level, core.relative(dir.getOpposite(), 2).relative(rot));
        this.makeExtra(level, core.relative(dir.getOpposite(), 2).relative(rot.getOpposite()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
