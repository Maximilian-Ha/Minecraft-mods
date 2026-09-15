package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.AshpitBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerAshpit.
 *
 * Fuenf reine Entnahmefaecher -- hineinlegen laesst sich nichts, die Asche kommt aus den
 * angrenzenden Feuerstellen.
 */
public class AshpitMenu extends MenuBase<AshpitBlockEntity> {

    public AshpitMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (AshpitBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public AshpitMenu(int id, Inventory inventory, AshpitBlockEntity be) {
        super(NtmMenuTypes.MACHINE_ASHPIT.get(), id, be);

        for(int i = 0; i < 5; i++) this.addSlot(new SlotTakeOnly(be, i, 44 + i * 18, 27));

        this.playerInv(inventory, 8, 86);
    }

    /**
     * Original: transferStackInSlot schiebt nur aus der Grube heraus und gibt fuer jedes
     * Spielerfach null zurueck -- in die Grube laesst sich nichts umlagern.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if(!slot.hasItem()) return ItemStack.EMPTY;
        if(index > 4) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack ret = stack.copy();

        if(!this.moveItemStackTo(stack, 5, this.slots.size(), true)) return ItemStack.EMPTY;

        if(stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return ret;
    }
}
