package com.hbm.blockentity.machine;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.machine.MachineRtgFurnaceBlock;
import com.hbm.inventory.menus.MachineRtgFurnaceMenu;
import com.hbm.items.machine.RTGPelletItem;
import com.hbm.util.RTGUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityRtgFurnace.
 *
 * Ein Ofen, der nicht mit Brennstoff und nicht mit Strom laeuft, sondern mit der Zerfallswaerme
 * von RTG-Pellets. Drei Pellets passen hinein; ihre Leistung addiert sich und treibt den
 * Fortschritt.
 *
 * Der Ofen braucht nichts weiter: keinen Anschluss, keine Wartung, keinen Knopf. Er laeuft, bis
 * die Pellets durch sind -- und sie altern auch dann, wenn nichts zu schmelzen da ist. Das ist
 * im Original so und hier uebernommen: Zerfall kennt keine Pause.
 *
 * Unterschied zum Original: dort gab es zwei getrennte Bloecke (an und aus). Auf 1.21 ist das
 * ein Block mit der Blockzustands-Eigenschaft LIT, so wie beim Elektroofen auch.
 */
public class MachineRtgFurnaceBlockEntity extends MachineBaseBlockEntity {

    /** Waermepunkte je Schmelzvorgang. */
    public static final int PROCESSING_SPEED = 1000;

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_RTG_FIRST = 1;
    public static final int SLOT_RTG_LAST = 3;
    public static final int SLOT_OUTPUT = 4;

    private static final int[] RTG_SLOTS = new int[] { SLOT_RTG_FIRST, 2, SLOT_RTG_LAST };

    private static final int[] SLOTS_TOP = new int[] { SLOT_INPUT };
    private static final int[] SLOTS_BOTTOM = new int[] { SLOT_OUTPUT };
    private static final int[] SLOTS_SIDE = RTG_SLOTS;

    public int progress;

    public MachineRtgFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_RTG_FURNACE.get(), pos, state, 5);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.rtgFurnace");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        boolean wasLit = this.progress > 0;

        ItemStack result = this.getSmeltingResult(this.slots.get(SLOT_INPUT));

        if(this.hasPower() && this.canProcess(result)) {

            this.progress += RTGUtil.updateRTGs(this.slots, RTG_SLOTS);

            if(this.progress >= PROCESSING_SPEED) {
                this.progress = 0;
                this.processItem(result);
                this.setChanged();
            }

        } else {
            this.progress = 0;
            /* Auch ohne Arbeit altern die Pellets weiter -- so steht es im Original. */
            RTGUtil.updateRTGs(this.slots, RTG_SLOTS);
        }

        if(wasLit != (this.progress > 0)) this.updateLitState(this.progress > 0);

        this.networkPackNT(50);
    }

    private void updateLitState(boolean lit) {

        BlockState state = this.getBlockState();
        if(!(state.getBlock() instanceof MachineRtgFurnaceBlock)) return;
        if(state.getValue(MachineRtgFurnaceBlock.LIT) == lit) return;

        this.level.setBlock(this.getBlockPos(), state.setValue(MachineRtgFurnaceBlock.LIT, lit), Block.UPDATE_ALL);
    }

    /* Zwischengespeicherte Rezeptsuche, wie beim Elektroofen. */
    private final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> quickCheck =
            RecipeManager.createCheck(RecipeType.SMELTING);

    private ItemStack getSmeltingResult(ItemStack input) {

        if(this.level == null || input.isEmpty()) return ItemStack.EMPTY;

        return this.quickCheck.getRecipeFor(new SingleRecipeInput(input.copy()), this.level)
                .map(holder -> holder.value().getResultItem(this.level.registryAccess()).copy())
                .orElse(ItemStack.EMPTY);
    }

    private boolean canProcess(ItemStack result) {

        if(this.slots.get(SLOT_INPUT).isEmpty()) return false;
        if(result.isEmpty()) return false;

        ItemStack output = this.slots.get(SLOT_OUTPUT);
        if(output.isEmpty()) return true;
        if(!ItemStack.isSameItemSameComponents(output, result)) return false;

        return output.getCount() + result.getCount() <= Math.min(this.getMaxStackSize(), output.getMaxStackSize());
    }

    private void processItem(ItemStack result) {

        if(result.isEmpty()) return;

        ItemStack input = this.slots.get(SLOT_INPUT);
        ItemStack output = this.slots.get(SLOT_OUTPUT);

        if(output.isEmpty()) {
            this.slots.set(SLOT_OUTPUT, result.copy());
        } else {
            output.grow(result.getCount());
        }

        input.shrink(1);
        if(input.isEmpty()) this.slots.set(SLOT_INPUT, ItemStack.EMPTY);
    }

    /** Liegt ueberhaupt ein Pellet ein? */
    public boolean hasPower() {
        return RTGUtil.hasHeat(this.slots, RTG_SLOTS);
    }

    public boolean isProcessing() {
        return this.progress > 0;
    }

    public int getProgressScaled(int pixels) {
        return this.progress * pixels / PROCESSING_SPEED;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_INPUT) return !this.getSmeltingResult(stack).isEmpty();
        if(slot >= SLOT_RTG_FIRST && slot <= SLOT_RTG_LAST) return stack.getItem() instanceof RTGPelletItem;
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return switch(direction) {
            case DOWN -> SLOTS_BOTTOM;
            case UP -> SLOTS_TOP;
            default -> SLOTS_SIDE;
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if(index == SLOT_OUTPUT) return true;
        /* Ein ausgebranntes Pellet darf wieder heraus. */
        return index >= SLOT_RTG_FIRST && index <= SLOT_RTG_LAST && !(stack.getItem() instanceof RTGPelletItem);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.progress);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.progress = buf.readInt();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.progress = tag.getInt("progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("progress", this.progress);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineRtgFurnaceMenu(id, inventory, this);
    }
}
