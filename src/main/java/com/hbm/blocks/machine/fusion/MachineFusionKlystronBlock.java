package com.hbm.blocks.machine.fusion;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.fusion.FusionKlystronBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.MultiblockHandlerXR;
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

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.fusion.MachineFusionKlystron.
 *
 * Der Klystronblock ist ein Quader mit einem Ruecksprung: der eigentliche Koerper steht auf dem
 * Boden, die Muendung ragt drei Bloecke weiter und liegt zwei Ebenen hoeher. Das Original loest
 * das ueber einen zweiten Bereich; hier ist es derselbe Griff wie bei der ICF-Kammer.
 */
public class MachineFusionKlystronBlock extends DummyableBlock implements ITooltipProvider {

    /** Der zweite Bereich: die hochgelegte Muendung. */
    private static final int[] DIM_MOUTH = new int[] {4, -3, 4, 3, 1, 1};

    public MachineFusionKlystronBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<MachineFusionKlystronBlock> CODEC = simpleCodec(MachineFusionKlystronBlock::new);
    @Override public MapCodec<MachineFusionKlystronBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new FusionKlystronBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).power().fluid();
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

    @Override public int[] getDimensions() { return new int[] { 3, 0, 4, 3, 2, 2 }; }
    @Override public int getOffset() { return 3; }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {
        return super.checkRequirement(level, pos, dir, offset)
                && MultiblockHandlerXR.checkSpace(level, pos.relative(dir, offset), DIM_MOUTH, pos, dir);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);
        MultiblockHandlerXR.fillSpace(level, core, DIM_MOUTH, this, dir);

        Direction rot = dir.getClockWise();

        /* Strom und Pressluft: einer an der Muendung, zwei an den Flanken. */
        this.makeExtra(level, core.offset(dir.getStepX() * 3, 2, dir.getStepZ() * 3));
        this.makeExtra(level, core.offset(rot.getStepX() * 2, 0, rot.getStepZ() * 2));
        this.makeExtra(level, core.offset(-rot.getStepX() * 2, 0, -rot.getStepZ() * 2));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
