package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineDiFurnaceBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerDiFurnace.
 * Slot-Positionen unveraendert.
 */
public class MachineDiFurnaceMenu extends MenuBase<MachineDiFurnaceBlockEntity> {

    private static final int SLOT_COUNT = 4;

    public MachineDiFurnaceMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineDiFurnaceBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineDiFurnaceMenu(int id, Inventory inventory, MachineDiFurnaceBlockEntity be) {
        super(NtmMenuTypes.MACHINE_DIFURNACE.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineDiFurnaceBlockEntity.SLOT_INPUT_UPPER, 80, 18));
        this.addSlot(new SlotNonRetarded(be, MachineDiFurnaceBlockEntity.SLOT_INPUT_LOWER, 80, 54));
        this.addSlot(new SlotNonRetarded(be, MachineDiFurnaceBlockEntity.SLOT_FUEL, 8, 36));
        this.addSlot(new SlotCraftingOutput(inventory.player, be, MachineDiFurnaceBlockEntity.SLOT_OUTPUT, 134, 36));

        this.playerInv(inventory, 8, 84, 142);
    }

    /**
     * Rechtsklick auf einen leeren Eingabeslot mit leerem Mauszeiger schaltet die Seite weiter,
     * von der dieser Slot Gegenstaende annimmt -- wie im Original.
     */
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {

        if(slotId >= 0 && slotId < 3 && button == 1 && clickType == ClickType.PICKUP) {
            Slot slot = this.slots.get(slotId);

            if(!slot.hasItem() && this.getCarried().isEmpty()) {

                if(!player.level().isClientSide) {
                    if(slotId == MachineDiFurnaceBlockEntity.SLOT_INPUT_UPPER) this.be.sideUpper = (byte) ((this.be.sideUpper + 1) % 6);
                    if(slotId == MachineDiFurnaceBlockEntity.SLOT_INPUT_LOWER) this.be.sideLower = (byte) ((this.be.sideLower + 1) % 6);
                    if(slotId == MachineDiFurnaceBlockEntity.SLOT_FUEL) this.be.sideFuel = (byte) ((this.be.sideFuel + 1) % 6);

                    this.be.setChanged();
                }

                return;
            }
        }

        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem()) {
            ItemStack stack = slot.getItem();
            ret = stack.copy();

            if(index < SLOT_COUNT) {
                if(!this.moveItemStackTo(stack, SLOT_COUNT, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                // Der Ausgabeslot bleibt aussen vor, genau wie im Original.
                if(!this.moveItemStackTo(stack, MachineDiFurnaceBlockEntity.SLOT_INPUT_UPPER, MachineDiFurnaceBlockEntity.SLOT_OUTPUT, false)) return ItemStack.EMPTY;
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
