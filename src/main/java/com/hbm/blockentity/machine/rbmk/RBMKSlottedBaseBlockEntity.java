package com.hbm.blockentity.machine.rbmk;

import com.hbm.blockentity.LoadedBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKSlottedBase.
 *
 * Basis fuer RBMK-Saeulen mit Inventar und Oberflaeche. Wie im Original ist das eine Kopie der
 * gewoehnlichen Maschinenbasis -- Java kennt keine Mehrfachvererbung, und die RBMK-Basis ist
 * schon vergeben.
 */
public abstract class RBMKSlottedBaseBlockEntity extends RBMKBaseBlockEntity implements WorldlyContainer, Nameable, MenuProvider {

    public NonNullList<ItemStack> slots;

    @Nullable private Component customName;

    public RBMKSlottedBaseBlockEntity(BlockEntityType<? extends LoadedBaseBlockEntity> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state);
        this.slots = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    protected abstract Component getDefaultName();

    @Override public Component getName() { return this.customName != null ? this.customName : this.getDefaultName(); }
    @Override public Component getDisplayName() { return this.getName(); }
    @Override public @Nullable Component getCustomName() { return this.customName; }

    @Override public int getContainerSize() { return this.slots.size(); }
    @Override public ItemStack getItem(int slot) { return this.slots.get(slot); }

    @Override
    public void setItem(int index, ItemStack stack) {
        this.slots.set(index, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.setChanged();
    }

    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public void startOpen(Player player) { }
    @Override public void stopOpen(Player player) { }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return false; }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.slots, slot, amount);
        if(!stack.isEmpty()) this.setChanged();
        return stack;
    }

    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(this.slots, slot); }

    @Override
    public boolean isEmpty() {
        for(ItemStack stack : this.slots) if(!stack.isEmpty()) return false;
        return true;
    }

    @Override public void clearContent() { this.slots.clear(); }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) { return false; }
    @Override public int[] getSlotsForFace(Direction direction) { return new int[] {}; }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, this.slots, registries);
        if(tag.contains("CustomName", 8)) this.customName = parseCustomNameSafe(tag.getString("CustomName"), registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.slots, registries);
        if(this.customName != null) tag.putString("CustomName", Component.Serializer.toJson(this.customName, registries));
    }
}
