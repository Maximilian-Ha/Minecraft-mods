package com.hbm.blockentity.machine;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.menus.WasteDrumMenu;
import com.hbm.inventory.recipes.FuelPoolRecipes;
import com.hbm.items.machine.RBMKRodItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityWasteDrum.
 *
 * Das Brennstoffbecken. Zwoelf Faecher, je einen Gegenstand tief. Die Trommel zaehlt, an wie
 * vielen ihrer sechs Seiten Wasser steht -- ohne Wasser passiert gar nichts, mit sechs Seiten
 * geht es sechsmal so schnell.
 *
 * Zwei Arten von Inhalt werden unterschiedlich behandelt, genau wie im Original:
 * - Ein RBMK-Brennstab kuehlt ueber seine eigenen Waermewerte gegen zwanzig Grad ab, jeden Tick.
 * - Alles andere springt mit einer Wahrscheinlichkeit von einer Stunde geteilt durch die Zahl
 *   der Wasserseiten auf seinen abgekuehlten Zustand um.
 */
public class WasteDrumBlockEntity extends MachineBaseBlockEntity {

    /** Wie lange ein Stapel bei genau einer Wasserseite im Mittel braucht: eine Stunde. */
    private static final int BASE_TICKS = 60 * 60 * 20;

    public WasteDrumBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.WASTE_DRUM.get(), pos, state, 12);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.wasteDrum");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        int water = 0;
        for(Direction dir : Direction.values()) {
            if(this.level.getFluidState(this.worldPosition.relative(dir)).is(FluidTags.WATER)) water++;
        }

        if(water == 0) return;

        int chance = BASE_TICKS / water;

        for(int i = 0; i < this.slots.size(); i++) {

            ItemStack stack = this.slots.get(i);
            if(stack.isEmpty()) continue;

            if(stack.getItem() instanceof RBMKRodItem rod) {
                rod.updateHeat(this.level, stack, 0.025D);
                rod.provideHeat(this.level, stack, 20D, 0.025D);
                continue;
            }

            if(this.level.random.nextInt(chance) != 0) continue;

            ItemStack result = FuelPoolRecipes.getOutput(stack);
            if(result != null) {
                this.slots.set(i, result);
                this.setChanged();
            }
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return FuelPoolRecipes.isValidInput(stack) || stack.getItem() instanceof RBMKRodItem;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    /**
     * Herausnehmen darf man nur, was fertig ist: ein abgekuehlter Abfall oder ein Brennstab, der
     * unter fuenfzig Grad liegt. Das Original laesst alles heraus -- hier waere das eine Falle,
     * weil Rohrleitungen den Abfall sonst sofort wieder herausziehen wuerden, bevor er kuehlt.
     */
    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {

        if(stack.getItem() instanceof RBMKRodItem) {
            return RBMKRodItem.getHullHeat(stack) < 50 && RBMKRodItem.getCoreHeat(stack) < 50;
        }

        return !FuelPoolRecipes.isValidInput(stack);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new WasteDrumMenu(id, inventory, this);
    }
}
