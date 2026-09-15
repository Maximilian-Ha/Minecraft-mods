package com.hbm.blockentity.network;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.network.CraneBaseBlock;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.item.MovingItem;
import com.hbm.inventory.menus.CraneUnboxerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityCraneUnboxer.
 *
 * Der Entpacker ist das Gegenstueck zum Packer: er nimmt ein ankommendes Paket auf, schuettet es
 * in seine einundzwanzig Faecher und setzt von dort einen Stapel nach dem anderen auf das
 * weiterfuehrende Band.
 *
 * Man beachte wieder die VERTAUSCHUNG, wie beim Auszieher: das Paket kommt an der AUSGANGSseite
 * herein, das Ausgepackte geht an der EINGANGSseite hinaus. Das steht so im Original -- die
 * Seiten heissen dort nach der Sicht des Bandes, nicht nach der der Maschine.
 *
 * NICHT UEBERNOMMEN: die beiden Aufwertungen upgrade_ejector und upgrade_stack, die Takt und
 * Menge erhoehen -- die Gegenstaende gibt es im Port nicht. Ohne sie setzt er einen Stapel alle
 * zwanzig Ticks heraus, genau wie der frisch gesetzte des Originals.
 */
public class CraneUnboxerBlockEntity extends MachineBaseBlockEntity {

    /** Drei Reihen zu sieben, wie im Original. */
    public static final int SLOTS = 7 * 3;

    /** Takt des Originals ohne Aufwertung. */
    private static final int DELAY = 20;

    public CraneUnboxerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.CRANE_UNBOXER.get(), pos, state, SLOTS);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.craneUnboxer");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(this.level.getGameTime() % DELAY == 0 && !this.level.hasNeighborSignal(this.worldPosition)) {
            this.sendOne();
        }

        this.networkPackNT(15);
    }

    private void sendOne() {

        Direction output = CraneBaseBlock.getInput(this.getBlockState()); // die Vertauschung
        BlockPos beltPos = this.worldPosition.relative(output);

        if(!(this.level.getBlockState(beltPos).getBlock() instanceof IConveyorBelt belt)) return;

        for(int i = 0; i < this.slots.size(); i++) {

            if(this.slots.get(i).isEmpty()) continue;

            /* Ein Stueck je Takt -- die Menge des Originals ohne Stapelaufwertung. Das ist
             * langsam, und es ist im Original genauso langsam; wer schneller entpacken will,
             * braucht dort die Aufwertungen. */
            ItemStack stack = this.removeItem(i, 1);

            Vec3 mouth = new Vec3(
                    this.worldPosition.getX() + 0.5 + output.getStepX() * 0.55,
                    this.worldPosition.getY() + 0.5 + output.getStepY() * 0.55,
                    this.worldPosition.getZ() + 0.5 + output.getStepZ() * 0.55);

            Vec3 snap = belt.getClosestSnappingPosition(this.level, beltPos, mouth);

            MovingItem moving = new MovingItem(NtmEntityTypes.MOVING_ITEM.get(), this.level);
            moving.setItemStack(stack);
            moving.moveTo(snap.x, snap.y, snap.z, 0F, 0F);

            if(belt instanceof IEnterableBlock enterable
                    && enterable.canItemEnter(this.level, beltPos, output.getOpposite(), moving)) {
                enterable.onItemEnter(this.level, beltPos, output.getOpposite(), moving);
                return;
            }

            this.level.addFreshEntity(moving);
            return;
        }
    }

    /**
     * Was aus einem ankommenden Paket kommt, legt der Block hier ab. Was nicht mehr hineinpasst,
     * bleibt uebrig und wird vom Aufrufer entsorgt.
     */
    public ItemStack store(ItemStack stack) {

        for(int pass = 0; pass < 2; pass++) {
            for(int i = 0; i < this.slots.size() && !stack.isEmpty(); i++) {

                ItemStack slot = this.slots.get(i);

                if(pass == 0 && !slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, stack)) {
                    int room = Math.min(slot.getMaxStackSize(), this.getMaxStackSize(slot)) - slot.getCount();
                    int moved = Math.min(room, stack.getCount());
                    if(moved <= 0) continue;
                    slot.grow(moved);
                    stack.shrink(moved);
                } else if(pass == 1 && slot.isEmpty()) {
                    int moved = Math.min(this.getMaxStackSize(stack), stack.getCount());
                    this.slots.set(i, stack.copyWithCount(moved));
                    stack.shrink(moved);
                }
            }
        }

        this.setChanged();
        return stack;
    }

    @Override public int[] getSlotsForFace(Direction direction) { return ALL_SLOTS; }
    @Override public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) { return true; }
    @Override public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) { return true; }

    private static final int[] ALL_SLOTS = allSlots();

    private static int[] allSlots() {
        int[] slots = new int[SLOTS];
        for(int i = 0; i < SLOTS; i++) slots[i] = i;
        return slots;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CraneUnboxerMenu(id, inventory, this);
    }
}
