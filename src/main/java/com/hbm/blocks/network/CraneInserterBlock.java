package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.network.CraneInserterBlockEntity;
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
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.CraneInserter.
 *
 * Der Einleger. Er nimmt an, was von der Eingangsseite her in ihn hineinfaehrt, und gibt es an
 * die Maschine an seiner Ausgangsseite weiter. Er ist damit das Stueck, mit dem eine Bandstrecke
 * ueberhaupt erst irgendwo ankommt.
 *
 * Angenommen wird nur von der eingestellten Eingangsseite -- wer von woanders hereinfaehrt,
 * prallt ab und faellt vom Band.
 */
public class CraneInserterBlock extends CraneBaseBlock implements IEnterableBlock {

    public static final MapCodec<CraneInserterBlock> CODEC = simpleCodec(CraneInserterBlock::new);

    public CraneInserterBlock(Properties properties) {
        super(properties);
    }

    @Override protected MapCodec<? extends CraneBaseBlock> codec() { return CODEC; }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CraneInserterBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, NtmBlockEntityTypes.CRANE_INSERTER.get(), (l, p, s, be) -> be.updateEntity());
    }

    // ------------------------------------------------------------------------------------
    // Was vom Band kommt
    // ------------------------------------------------------------------------------------

    @Override
    public boolean canItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity) {
        return getInput(level.getBlockState(pos)) == dir;
    }

    @Override
    public void onItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity) {

        if(entity == null || entity.getItemStack().isEmpty()) return;

        this.accept(level, pos, entity.getItemStack().copy());
    }

    @Override
    public boolean canPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity) {
        return true;
    }

    @Override
    public void onPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity) {

        if(entity == null || entity.getItemStacks() == null) return;

        for(ItemStack stack : entity.getItemStacks()) {
            if(!stack.isEmpty()) this.accept(level, pos, stack.copy());
        }
    }

    /**
     * Drei Anlaeufe: erst in die Maschine, dann in die eigenen Faecher, und was dann noch uebrig
     * ist, faellt zu Boden -- oder verschwindet, wenn der Vernichter eingeschaltet ist.
     */
    private void accept(Level level, BlockPos pos, ItemStack stack) {

        if(!(level.getBlockEntity(pos) instanceof CraneInserterBlockEntity be)) return;

        if(!level.hasNeighborSignal(pos)) {
            IItemHandler target = be.getTarget();
            if(target != null) stack = ItemHandlerHelper.insertItemStacked(target, stack, false);
        }

        if(stack.isEmpty()) return;

        stack = be.storeOverflow(stack);

        if(stack.isEmpty() || be.destroyer) return;

        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {

        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CraneInserterBlockEntity be) {
            Containers.dropContents(level, pos, be);
        }

        super.onRemove(state, level, pos, newState, moved);
    }

    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CraneInserterBlockEntity be
                ? net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromContainer(be) : 0;
    }
}
