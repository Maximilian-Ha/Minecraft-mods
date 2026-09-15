package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineSatDockBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.ISatChip;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Portiert aus 1.7.10: com.hbm.inventory.container.ContainerSatDock. */
public class MachineSatDockMenu extends MenuBase<MachineSatDockBlockEntity> {

    public MachineSatDockMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineSatDockBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineSatDockMenu(int id, Inventory inventory, MachineSatDockBlockEntity be) {
        super(NtmMenuTypes.SAT_DOCK.get(), id, be);

        /* Die Ladung kommt von oben und geht nur heraus -- hineinlegen laesst sich nichts. */
        this.addTakeOnlySlots(be, 0, 71, 18, 3, 5);

        /* Der Chip bestimmt, bei welchem Satelliten die Station nachfragt. */
        this.addSlot(new SlotNonRetarded(be, MachineSatDockBlockEntity.SLOT_CHIP, 26, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ISatChip;
            }
        });

        this.playerInv(inventory, 8, 104);
    }
}
