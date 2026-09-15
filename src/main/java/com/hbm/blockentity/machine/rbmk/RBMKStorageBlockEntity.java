package com.hbm.blockentity.machine.rbmk;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKType;
import com.hbm.inventory.menus.RBMKStorageMenu;
import com.hbm.items.machine.RBMKRodItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKStorage.
 *
 * Die Lagersaeule. Nimmt zwoelf Brennstaebe auf und rueckt sie regelmaessig nach vorne, damit
 * der Ladekran immer am selben Ende zugreifen kann.
 */
public class RBMKStorageBlockEntity extends RBMKSlottedBaseBlockEntity implements IRBMKLoadable {

    private static final int[] ALL_SLOTS = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

    public RBMKStorageBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_STORAGE.get(), pos, state, 12);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.rbmkStorage");
    }

    @Override
    public void updateEntity() {

        if(this.level != null && !this.level.isClientSide && this.level.getGameTime() % 10 == 0) {

            for(int i = 0; i < this.slots.size() - 1; i++) {
                if(this.slots.get(i).isEmpty() && !this.slots.get(i + 1).isEmpty()) {
                    this.slots.set(i, this.slots.get(i + 1));
                    this.slots.set(i + 1, ItemStack.EMPTY);
                }
            }
        }

        super.updateEntity();
    }

    @Override
    public RBMKColumnType getConsoleType() {
        return RBMKColumnType.STORAGE;
    }

    @Override
    public RBMKType getRBMKType() {
        return RBMKType.OTHER;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof RBMKRodItem;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return ALL_SLOTS;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RBMKStorageMenu(id, inventory, this);
    }

    /* Der Ladekran: er legt hinten ein, nimmt vorne heraus. Die Saeule schiebt selbst nach. */

    @Override
    public boolean canLoad(ItemStack toLoad) {
        return this.slots.get(11).isEmpty();
    }

    @Override
    public void load(ItemStack toLoad) {
        this.slots.set(11, toLoad.copy());
        this.setChanged();
    }

    @Override
    public boolean canUnload() {
        return !this.slots.get(0).isEmpty();
    }

    @Override
    public ItemStack provideNext() {
        return this.slots.get(0);
    }

    @Override
    public void unload() {
        this.slots.set(0, ItemStack.EMPTY);
        this.setChanged();
    }
}
