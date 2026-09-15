package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineTurbineGasBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineTurbineGas.
 * Slot-Positionen unveraendert uebernommen.
 */
public class MachineTurbineGasMenu extends MenuBase<MachineTurbineGasBlockEntity> {

    private static final int SLOT_COUNT = 2;

    public MachineTurbineGasMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineTurbineGasBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineTurbineGasMenu(int id, Inventory inventory, MachineTurbineGasBlockEntity be) {
        super(NtmMenuTypes.MACHINE_TURBINEGAS.get(), id, be);

        // Batterie
        this.addSlot(new SlotNonRetarded(be, MachineTurbineGasBlockEntity.SLOT_BATTERY, 8, 109));
        // Fluidkennung
        this.addSlot(new SlotNonRetarded(be, MachineTurbineGasBlockEntity.SLOT_IDENTIFIER, 36, 17));

        this.playerInv(inventory, 8, 141, 199);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem()) {
            ItemStack stack = slot.getItem();
            ret = stack.copy();

            if(index <= SLOT_COUNT - 1) { // aus Batterie- oder Kennungsslot ins Spielerinventar
                if(!this.moveItemStackTo(stack, SLOT_COUNT, this.slots.size(), true)) return ItemStack.EMPTY;

            } else if(stack.getItem() instanceof IBatteryItem) { // Batterien nur in den Batterieslot
                if(!this.moveItemStackTo(stack, MachineTurbineGasBlockEntity.SLOT_BATTERY, MachineTurbineGasBlockEntity.SLOT_BATTERY + 1, true)) return ItemStack.EMPTY;

            } else if(stack.getItem() instanceof IItemFluidIdentifier id) {

                FluidType type = id.getType(this.be.getLevel(), this.be.getBlockPos(), stack);
                // ueberfluessige Einschraenkung aus dem Original, aber 1:1 uebernommen
                if(type == null || !(type.hasTrait(FT_Combustible.class) && type.getTrait(FT_Combustible.class).getGrade() == FuelGrade.GAS)) return ItemStack.EMPTY;

                if(!this.moveItemStackTo(stack, MachineTurbineGasBlockEntity.SLOT_IDENTIFIER, MachineTurbineGasBlockEntity.SLOT_IDENTIFIER + 1, true)) return ItemStack.EMPTY;

            } else {
                return ItemStack.EMPTY;
            }

            if(stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return ret;
    }
}
