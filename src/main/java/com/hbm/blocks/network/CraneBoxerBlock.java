package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.network.CraneBoxerBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.blocks.network.CraneBoxer.
 *
 * Der Packer nimmt an, was von seiner Eingangsseite her hereinfaehrt, und schnuert daraus
 * Pakete. Pakete selbst nimmt er NICHT an -- ein Paket zu verpacken ergaebe keinen Sinn.
 */
public class CraneBoxerBlock extends CraneBaseBlock implements IEnterableBlock {

    public static final MapCodec<CraneBoxerBlock> CODEC = simpleCodec(CraneBoxerBlock::new);

    public CraneBoxerBlock(Properties properties) {
        super(properties);
    }

    @Override protected MapCodec<? extends CraneBaseBlock> codec() { return CODEC; }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CraneBoxerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, NtmBlockEntityTypes.CRANE_BOXER.get(), (l, p, s, be) -> be.updateEntity());
    }

    @Override
    public boolean canItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity) {
        return getInput(level.getBlockState(pos)) == dir;
    }

    @Override
    public void onItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity) {

        if(entity == null || entity.getItemStack().isEmpty()) return;
        if(!(level.getBlockEntity(pos) instanceof CraneBoxerBlockEntity be)) return;

        ItemStack rest = be.store(entity.getItemStack().copy());
        if(!rest.isEmpty()) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, rest);
    }

    @Override public boolean canPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity) { return false; }
    @Override public void onPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity) { }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {

        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CraneBoxerBlockEntity be) {
            Containers.dropContents(level, pos, be);
        }

        super.onRemove(state, level, pos, newState, moved);
    }

    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CraneBoxerBlockEntity be
                ? net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromContainer(be) : 0;
    }
}
