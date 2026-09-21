package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineSILEXBlockEntity;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineSILEX.
 *
 * Fuenf Bloecke lang, drei breit, zwei hoch -- und die beiden Anschluesse sitzen nicht in der
 * Wand, sondern einen Block ueber dem Kern, quer zur Blickrichtung. Dort stehen im Modell die
 * Rohre, durch die das Peroxid hereinkommt.
 */
public class MachineSILEXBlock extends DummyableBlock implements ITooltipProvider {

    public static final MapCodec<MachineSILEXBlock> CODEC = simpleCodec(MachineSILEXBlock::new);

    public MachineSILEXBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineSILEXBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineSILEXBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 2, 0, 1, 1, 1, 1 }; }
    @Override public int getOffset() { return 1; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    /**
     * Die beiden Anschlussbloecke liegen quer zur Blickrichtung, einen Block ueber dem Kern.
     * Das Original prueft dafuer die Himmelsrichtung und setzt einmal in X, einmal in Z; ueber
     * die Querrichtung ausgedrueckt ist es derselbe Griff fuer alle vier Richtungen.
     */
    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset).above();
        Direction rot = dir.getClockWise();

        this.makeExtra(level, core.relative(rot));
        this.makeExtra(level, core.relative(rot.getOpposite()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
