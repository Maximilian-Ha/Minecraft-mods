package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineExposureChamberBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineExposureChamber.
 *
 * Links oben die Kapsel, darunter das Rueckgabefach fuer die leere Huelle, in der Mitte der
 * Barren, rechts das Ergebnis. Das versteckte Fach mit der eingezogenen Ladung hat absichtlich
 * keinen Platz in der Oberflaeche -- man soll nicht hineingreifen koennen.
 */
public class MachineExposureChamberMenu extends MenuBase<MachineExposureChamberBlockEntity> {

    public MachineExposureChamberMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineExposureChamberBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineExposureChamberMenu(int id, Inventory inventory, MachineExposureChamberBlockEntity be) {
        super(NtmMenuTypes.MACHINE_EXPOSURE_CHAMBER.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineExposureChamberBlockEntity.SLOT_PARTICLE, 8, 18));
        this.addSlot(new SlotTakeOnly(be, MachineExposureChamberBlockEntity.SLOT_PARTICLE_CONTAINER, 8, 54));
        this.addSlot(new SlotNonRetarded(be, MachineExposureChamberBlockEntity.SLOT_INGREDIENT, 80, 36));
        this.addSlot(new SlotCraftingOutput(inventory.player, be, MachineExposureChamberBlockEntity.SLOT_OUTPUT, 116, 36));
        this.addSlot(new SlotNonRetarded(be, MachineExposureChamberBlockEntity.SLOT_BATTERY, 152, 54));
        this.addSlot(new SlotNonRetarded(be, MachineExposureChamberBlockEntity.SLOT_UPGRADE_START, 44, 54));
        this.addSlot(new SlotNonRetarded(be, MachineExposureChamberBlockEntity.SLOT_UPGRADE_END, 62, 54));

        this.playerInv(inventory, 8, 104);
    }

    /** Sieben Faecher in der Oberflaeche -- das achte, die eingezogene Ladung, steht nicht darin. */
    public static final int SLOTS = 7;

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if(!slot.hasItem()) return ret;

        ItemStack stack = slot.getItem();
        ret = stack.copy();

        if(index < SLOTS) {
            if(!this.moveItemStackTo(stack, SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;

        } else if(stack.getItem() instanceof IBatteryItem) {
            if(!this.moveItemStackTo(stack, 4, 5, false)) return ItemStack.EMPTY;

        } else if(stack.getItem() instanceof MachineUpgradeItem) {
            if(!this.moveItemStackTo(stack, 5, 7, false)) return ItemStack.EMPTY;

        } else if(this.be.canPlaceItem(MachineExposureChamberBlockEntity.SLOT_PARTICLE, stack)) {
            if(!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;

        } else if(this.be.canPlaceItem(MachineExposureChamberBlockEntity.SLOT_INGREDIENT, stack)) {
            if(!this.moveItemStackTo(stack, 2, 3, false)) return ItemStack.EMPTY;

        } else {
            return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return ret;
    }
}
