package com.hbm.blocks.machine.fusion;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.fusion.FusionMHDTBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.MultiblockHandlerXR;
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

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.fusion.MachineFusionMHDT.
 *
 * Die groesste Anlage der Fusion: acht Bloecke lang, sieben breit, sechs hoch, und aus fuenf
 * Quadern zusammengesetzt -- der Rumpf, zwei Ausleger, das Dach und der Auslass.
 */
public class MachineFusionMHDTBlock extends DummyableBlock implements ITooltipProvider {

    private static final int[][] EXTRA_DIMS = new int[][] {
            {3, -2,  6, 2, 1, 1},
            {3, -2, -6, 7, 1, 1},
            {3, -2, -3, 5, 2, 2},
            {4, -3, -3, 5, 1, 1}
    };

    /** Der Auslass, drei Bloecke vor dem Kern. */
    private static final int[] DIM_OUTLET = new int[] {1, 0, 0, 1, 3, 3};

    public MachineFusionMHDTBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<MachineFusionMHDTBlock> CODEC = simpleCodec(MachineFusionMHDTBlock::new);
    @Override public MapCodec<MachineFusionMHDTBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new FusionMHDTBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 2, 0, 6, 7, 2, 2 }; }
    @Override public int getOffset() { return 7; }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {

        if(!super.checkRequirement(level, pos, dir, offset)) return false;

        BlockPos core = pos.relative(dir, offset);

        for(int[] dim : EXTRA_DIMS) {
            if(!MultiblockHandlerXR.checkSpace(level, core, dim, pos, dir)) return false;
        }

        return MultiblockHandlerXR.checkSpace(level, pos.relative(dir, offset + 3), DIM_OUTLET, pos, dir);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);

        for(int[] dim : EXTRA_DIMS) MultiblockHandlerXR.fillSpace(level, core, dim, this, dir);
        MultiblockHandlerXR.fillSpace(level, pos.relative(dir, offset + 3), DIM_OUTLET, this, dir);

        Direction rot = dir.getClockWise();

        /* Zwei Kuehlmittelanschluesse an den Auslegern, der Stromanschluss am Auslass. */
        this.makeExtra(level, core.offset(dir.getStepX() * 4 + rot.getStepX() * 3, 0, dir.getStepZ() * 4 + rot.getStepZ() * 3));
        this.makeExtra(level, core.offset(dir.getStepX() * 4 - rot.getStepX() * 3, 0, dir.getStepZ() * 4 - rot.getStepZ() * 3));
        this.makeExtra(level, core.offset(dir.getStepX() * 7, 1, dir.getStepZ() * 7));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
