package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.FurnaceBrickBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerFurnaceBrick.
 * Die Platzkoordinaten sind unveraendert uebernommen.
 */
public class FurnaceBrickMenu extends MenuBase<FurnaceBrickBlockEntity> {

    private static final int SLOT_COUNT = 4;

    public FurnaceBrickMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (FurnaceBrickBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public FurnaceBrickMenu(int id, Inventory inventory, FurnaceBrickBlockEntity be) {
        super(NtmMenuTypes.FURNACE_BRICK.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, FurnaceBrickBlockEntity.SLOT_INPUT, 62, 35));
        this.addSlot(new SlotNonRetarded(be, FurnaceBrickBlockEntity.SLOT_FUEL, 35, 17));
        this.addSlot(new SlotTakeOnly(be, FurnaceBrickBlockEntity.SLOT_OUTPUT, 116, 35));
        this.addSlot(new SlotTakeOnly(be, FurnaceBrickBlockEntity.SLOT_ASH, 35, 53));

        this.playerInv(inventory, 8, 84, 142);
    }

    /**
     * Wie im Original: aus dem Inventar wandert Brennbares in den Brennstoffplatz, alles
     * andere in den Eingang.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem()) {
            ItemStack stack = slot.getItem();
            ret = stack.copy();

            if(index < SLOT_COUNT) {
                if(!this.moveItemStackTo(stack, SLOT_COUNT, this.slots.size(), true)) return ItemStack.EMPTY;

            } else if(stack.getBurnTime(RecipeType.SMELTING) > 0) {
                if(!this.moveItemStackTo(stack, FurnaceBrickBlockEntity.SLOT_FUEL, FurnaceBrickBlockEntity.SLOT_FUEL + 1, false)) {
                    return ItemStack.EMPTY;
                }

            } else {
                if(!this.moveItemStackTo(stack, FurnaceBrickBlockEntity.SLOT_INPUT, FurnaceBrickBlockEntity.SLOT_INPUT + 1, false)) {
                    return ItemStack.EMPTY;
                }
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
