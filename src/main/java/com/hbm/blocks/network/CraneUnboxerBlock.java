package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.network.CraneUnboxerBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.CraneUnboxer.
 *
 * Der Entpacker nimmt NUR Pakete an, und nur an seiner Ausgangsseite -- die Vertauschung des
 * Originals, die sich durch alle Kranmaschinen zieht. Einzelne Gegenstaende laesst er
 * vorbeifahren; sie zu "entpacken" gaebe nichts her.
 */
public class CraneUnboxerBlock extends CraneBaseBlock implements IEnterableBlock {

    public static final MapCodec<CraneUnboxerBlock> CODEC = simpleCodec(CraneUnboxerBlock::new);

    public CraneUnboxerBlock(Properties properties) {
        super(properties);
    }

    @Override protected MapCodec<? extends CraneBaseBlock> codec() { return CODEC; }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CraneUnboxerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, NtmBlockEntityTypes.CRANE_UNBOXER.get(), (l, p, s, be) -> be.updateEntity());
    }

    @Override public boolean canItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity) { return false; }
    @Override public void onItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity) { }

    @Override
    public boolean canPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity) {
        return getOutput(level.getBlockState(pos)) == dir;
    }

    @Override
    public void onPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity) {

        if(entity == null || entity.getItemStacks() == null) return;
        if(!(level.getBlockEntity(pos) instanceof CraneUnboxerBlockEntity be)) return;

        for(ItemStack stack : entity.getItemStacks()) {

            if(stack.isEmpty()) continue;

            ItemStack rest = be.store(stack.copy());
            if(!rest.isEmpty()) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, rest);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {

        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CraneUnboxerBlockEntity be) {
            Containers.dropContents(level, pos, be);
        }

        super.onRemove(state, level, pos, newState, moved);
    }

    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CraneUnboxerBlockEntity be
                ? net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromContainer(be) : 0;
    }
}
