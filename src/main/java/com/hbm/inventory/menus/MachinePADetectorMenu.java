package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.albion.MachinePADetectorBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerPADetector.
 *
 * Oben die beiden Huellenfaecher, darunter die beiden Ausgaben. Die Ausgabefaecher sind
 * Werkbankfaecher -- wer daraus nimmt, loest aus, was ein fertiges Erzeugnis ausloesen soll.
 */
public class MachinePADetectorMenu extends MenuBase<MachinePADetectorBlockEntity> {

    public static final int SLOTS = 5;

    public MachinePADetectorMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachinePADetectorBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachinePADetectorMenu(int id, Inventory inventory, MachinePADetectorBlockEntity be) {
        super(NtmMenuTypes.MACHINE_PA_DETECTOR.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachinePADetectorBlockEntity.SLOT_BATTERY, 8, 72));

        this.addSlot(new SlotNonRetarded(be, MachinePADetectorBlockEntity.SLOT_CONTAINER_1, 62, 18));
        this.addSlot(new SlotNonRetarded(be, MachinePADetectorBlockEntity.SLOT_CONTAINER_2, 80, 18));

        this.addSlot(new SlotCraftingOutput(inventory.player, be, MachinePADetectorBlockEntity.SLOT_OUTPUT_1, 62, 45));
        this.addSlot(new SlotCraftingOutput(inventory.player, be, MachinePADetectorBlockEntity.SLOT_OUTPUT_2, 80, 45));

        this.playerInv(inventory, 8, 122);
    }

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
            if(!this.moveItemStackTo(stack, MachinePADetectorBlockEntity.SLOT_BATTERY, MachinePADetectorBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;

        } else {
            if(!this.moveItemStackTo(stack, MachinePADetectorBlockEntity.SLOT_CONTAINER_1, MachinePADetectorBlockEntity.SLOT_CONTAINER_2 + 1, false)) return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return ret;
    }
}
