package com.hbm.blockentity.machine;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.machine.MachineDiFurnaceRtgBlock;
import com.hbm.inventory.menus.MachineDiFurnaceRtgMenu;
import com.hbm.inventory.recipes.BlastFurnaceRecipes;
import com.hbm.inventory.recipes.BlastFurnaceRecipe;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityDiFurnaceRTG.
 *
 * Der Hochofen ohne Feuer: sechs RTG-Pellets statt Kohle. Ihre Zerfallswaerme treibt den
 * Fortschritt; unter fuenfzehn Waermepunkten reicht es nicht zum Schmelzen.
 *
 * Rezepte, Zutatenabzug und Ausgabe sind dieselben wie beim gewoehnlichen Hochofen -- die
 * Rechnung steht seit dieser Runde einmal in MachineDiFurnaceBlockEntity statt zweimal.
 *
 * Wie dort lassen sich die beiden Eingabefaecher auf je eine Seite legen, damit Trichter von
 * oben und von der Seite verschiedene Zutaten einwerfen koennen.
 */
public class MachineDiFurnaceRtgBlockEntity extends MachineBaseBlockEntity {

    /** Waermepunkte je Schmelzvorgang. */
    public static final int TIME_REQUIRED = 1200;
    /** Darunter laeuft gar nichts. */
    public static final int MIN_HEAT = 15;

    public static final int SLOT_INPUT_UPPER = 0;
    public static final int SLOT_INPUT_LOWER = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_RTG_FIRST = 3;
    public static final int SLOT_RTG_LAST = 8;

    private static final int[] RTG_SLOTS = new int[] { 3, 4, 5, 6, 7, 8 };
    private static final int[] ACCESSIBLE_SLOTS = new int[] { SLOT_INPUT_UPPER, SLOT_INPUT_LOWER, SLOT_OUTPUT };

    public int progress;
    /** Die zuletzt gemessene Gesamtwaerme der Pellets. */
    public int heat;

    /** Auf welcher Seite das obere und das untere Eingabefach angenommen wird. */
    public byte sideUpper = 1;
    public byte sideLower = 1;

    public MachineDiFurnaceRtgBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_DIFURNACE_RTG.get(), pos, state, 9);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.diFurnaceRTG");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        boolean wasLit = this.progress > 0;

        /* Die Pellets altern in jedem Fall, auch wenn nichts zu schmelzen da ist. */
        this.heat = RTGUtil.updateRTGs(this.slots, RTG_SLOTS);

        if(this.hasPower() && this.canProcess()) {

            this.progress += this.heat;

            if(this.progress >= TIME_REQUIRED) {
                this.processItem();
                this.progress = 0;
                this.setChanged();
            }

        } else {
            this.progress = 0;
        }

        if(wasLit != (this.progress > 0)) this.updateLitState(this.progress > 0);

        this.networkPackNT(50);
    }

    private void updateLitState(boolean lit) {

        BlockState state = this.getBlockState();
        if(!(state.getBlock() instanceof MachineDiFurnaceRtgBlock)) return;
        if(state.getValue(MachineDiFurnaceRtgBlock.LIT) == lit) return;

        this.level.setBlock(this.getBlockPos(), state.setValue(MachineDiFurnaceRtgBlock.LIT, lit), Block.UPDATE_ALL);
    }

    @Nullable
    public BlastFurnaceRecipe getRecipe() {
        return BlastFurnaceRecipes.INSTANCE.getRecipe(this.slots.get(SLOT_INPUT_UPPER), this.slots.get(SLOT_INPUT_LOWER));
    }

    public boolean canProcess() {

        if(this.slots.get(SLOT_INPUT_UPPER).isEmpty() || this.slots.get(SLOT_INPUT_LOWER).isEmpty()) return false;

        BlastFurnaceRecipe recipe = this.getRecipe();
        if(recipe == null) return false;
        if(!BlastFurnaceRecipes.matchesInputs(recipe, this.slots.get(SLOT_INPUT_UPPER), this.slots.get(SLOT_INPUT_LOWER), false)) return false;

        ItemStack output = MachineDiFurnaceBlockEntity.getPrimaryOutput(recipe);
        if(output.isEmpty()) return false;

        ItemStack current = this.slots.get(SLOT_OUTPUT);
        if(current.isEmpty()) return true;
        if(!ItemStack.isSameItemSameComponents(current, output)) return false;

        return current.getCount() + output.getCount() <= current.getMaxStackSize();
    }

    private void processItem() {

        BlastFurnaceRecipe recipe = this.getRecipe();
        if(recipe == null) return;

        ItemStack output = MachineDiFurnaceBlockEntity.getPrimaryOutput(recipe);
        if(output.isEmpty()) return;

        MachineDiFurnaceBlockEntity.consumeInputs(recipe, this.slots, SLOT_INPUT_UPPER, SLOT_INPUT_LOWER);

        ItemStack current = this.slots.get(SLOT_OUTPUT);
        if(current.isEmpty()) {
            this.slots.set(SLOT_OUTPUT, output.copy());
        } else if(ItemStack.isSameItemSameComponents(current, output)) {
            current.grow(output.getCount());
        }
    }

    public boolean hasPower() {
        return this.heat >= MIN_HEAT;
    }

    public boolean isProcessing() {
        return this.progress > 0;
    }

    public int getProgressScaled(int pixels) {
        return this.progress * pixels / TIME_REQUIRED;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_INPUT_UPPER || slot == SLOT_INPUT_LOWER) return true;
        if(slot >= SLOT_RTG_FIRST && slot <= SLOT_RTG_LAST) return stack.getItem() instanceof RTGPelletItem;
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return ACCESSIBLE_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        /* Die beiden Eingaben hoeren jeweils nur auf ihre eingestellte Seite. */
        if(direction != null) {
            if(index == SLOT_INPUT_UPPER && this.sideUpper != direction.get3DDataValue()) return false;
            if(index == SLOT_INPUT_LOWER && this.sideLower != direction.get3DDataValue()) return false;
        }

        return this.canPlaceItem(index, stack);
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
        buf.writeInt(this.heat);
        buf.writeByte(this.sideUpper);
        buf.writeByte(this.sideLower);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.progress = buf.readInt();
        this.heat = buf.readInt();
        this.sideUpper = buf.readByte();
        this.sideLower = buf.readByte();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.progress = tag.getInt("progress");
        this.sideUpper = tag.getByte("sideUpper");
        this.sideLower = tag.getByte("sideLower");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("progress", this.progress);
        tag.putByte("sideUpper", this.sideUpper);
        tag.putByte("sideLower", this.sideLower);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineDiFurnaceRtgMenu(id, inventory, this);
    }
}
