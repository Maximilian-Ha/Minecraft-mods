package com.hbm.blockentity.machine;

import com.hbm.items.machine.MoldItem;
import com.hbm.items.machine.MoldItem.Mold;
import com.hbm.inventory.material.Mats.MaterialStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityFoundryCastingBase.
 *
 * Ein Giessereiblock, in den eine Form eingelegt wird. Ist die Form voll, kuehlt der Guss ab und
 * wird zu einem Gegenstand, der im zweiten Fach liegen bleibt, bis ihn jemand herausnimmt.
 *
 * Fach 0 ist die Form, Fach 1 das fertige Gussstueck.
 */
public abstract class FoundryCastingBaseBlockEntity extends FoundryBaseBlockEntity implements WorldlyContainer {

    public final NonNullList<ItemStack> slots;
    public int cooloff = 100;

    public FoundryCastingBaseBlockEntity(BlockEntityType<? extends FoundryCastingBaseBlockEntity> blockEntityType, BlockPos pos, BlockState state) {
        this(blockEntityType, pos, state, 2);
    }

    /**
     * Mit mehr als zwei Faechern: Fach 0 bleibt die Form, alles danach ist Ausgabe. Der
     * Strangguss giesst in einem Zug bis zu neun Stueck und braucht deshalb Platz dafuer.
     */
    public FoundryCastingBaseBlockEntity(BlockEntityType<? extends FoundryCastingBaseBlockEntity> blockEntityType, BlockPos pos, BlockState state, int slotCount) {
        super(blockEntityType, pos, state);
        this.slots = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(this.amount > this.getCapacity()) this.amount = this.getCapacity();
        if(this.amount == 0) this.type = null;

        Mold mold = this.getInstalledMold();

        if(mold != null && this.amount == this.getCapacity() && this.getCapacity() > 0 && this.slots.get(1).isEmpty()) {

            this.cooloff--;

            if(this.cooloff <= 0) {
                this.amount = 0;

                ItemStack out = mold.getOutput(this.level, this.type);
                if(!out.isEmpty()) this.slots.set(1, out.copy());

                this.cooloff = 200;
                this.setChanged();
            }

        } else {
            this.cooloff = 200;
        }

        super.updateEntity();
    }

    /** Was in Fach 0 steckt, sofern es eine Form der passenden Groesse ist. */
    public @Nullable Mold getInstalledMold() {

        ItemStack stack = this.slots.get(0);
        if(!(stack.getItem() instanceof MoldItem)) return null;

        Mold mold = MoldItem.getMold(stack);

        return mold != null && mold.size == this.getMoldSize() ? mold : null;
    }

    /** Wieviel hineinpasst, haengt an der eingelegten Form; ohne Form passt nichts hinein. */
    @Override
    public int getCapacity() {
        Mold mold = this.getInstalledMold();
        return mold == null ? 0 : mold.getCost();
    }

    /**
     * Zusaetzlich zur Pruefung der Wurzelklasse: kein noch nicht entnommenes Gussstueck, eine
     * Form muss liegen, und die Form muss aus diesem Material ueberhaupt etwas machen koennen.
     */
    @Override
    public boolean standardCheck(Level level, BlockPos pos, @Nullable Direction side, MaterialStack stack) {

        if(!super.standardCheck(level, pos, side, stack)) return false;
        if(!this.slots.get(1).isEmpty()) return false;

        Mold mold = this.getInstalledMold();
        if(mold == null) return false;

        return !mold.getOutput(level, stack.material).isEmpty();
    }

    /** 0 fuer die kleine Form, 1 fuer das Becken. */
    public abstract int getMoldSize();

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        for(ItemStack stack : this.slots) {
            buf.writeNbt(stack.isEmpty() || this.level == null ? null : stack.save(this.level.registryAccess()));
        }
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(int i = 0; i < this.slots.size(); i++) {
            CompoundTag tag = buf.readNbt();
            this.slots.set(i, tag != null && this.level != null ? ItemStack.parseOptional(this.level.registryAccess(), tag) : ItemStack.EMPTY);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.slots.clear();
        ContainerHelper.loadAllItems(tag, this.slots, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.slots, registries);
    }

    /* --- Container: nur das Gussstueck darf heraus, hinein darf nichts --- */

    @Override public int getContainerSize() { return this.slots.size(); }
    @Override public boolean isEmpty() { return this.slots.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return this.slots.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.slots, slot, amount);
        if(!stack.isEmpty()) this.setChanged();
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.slots, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.slots.set(slot, stack);
        if(stack.getCount() > this.getMaxStackSize()) stack.setCount(this.getMaxStackSize());
        this.setChanged();
    }

    @Override public boolean stillValid(Player player) { return false; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return false; }
    @Override public void clearContent() { this.slots.clear(); }

    @Override public int[] getSlotsForFace(Direction direction) { return new int[] { 1 }; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) { return false; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return slot == 1; }
}
