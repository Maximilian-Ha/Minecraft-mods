package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineAnnihilatorBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.MultiblockHandlerXR;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineAnnihilator.
 *
 * Ein flacher Block von neun mal drei -- und ein Schlot, der acht Bloecke aufragt und zwei
 * hinabreicht. Der Schlot steht drei Bloecke HINTER dem Kern, deshalb der eigene zweite Koerper:
 * die Wurzelklasse kennt nur eine Abmessung.
 */
public class MachineAnnihilatorBlock extends DummyableBlock implements ITooltipProvider {

    public static final MapCodec<MachineAnnihilatorBlock> CODEC = simpleCodec(MachineAnnihilatorBlock::new);

    /** Der Hauptkoerper; der Schlot steht versetzt und wird eigens gesetzt. */
    private static final int[] BODY = { 2, 0, 4, 4, 1, 1 };
    private static final int[] STACK = { 8, -2, 1, 1, 1, 1 };
    /** Der Schlot sitzt drei Bloecke hinter dem Kern. */
    private static final int STACK_OFFSET = -3;

    public MachineAnnihilatorBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineAnnihilatorBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineAnnihilatorBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return BODY; }
    @Override public int getOffset() { return 4; }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {
        if(!super.checkRequirement(level, pos, dir, offset)) return false;
        return MultiblockHandlerXR.checkSpace(level, pos.relative(dir, offset + STACK_OFFSET), STACK, pos, dir);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);
        MultiblockHandlerXR.fillSpace(level, pos.relative(dir, offset + STACK_OFFSET), STACK, this, dir);

        BlockPos core = pos.relative(dir, offset);
        Direction rot = dir.getClockWise(Axis.Y);

        this.makeExtra(level, core.relative(dir, 3).relative(rot));
        this.makeExtra(level, core.relative(dir, 3).relative(rot.getOpposite()));
        this.makeExtra(level, core.relative(dir, 4));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
