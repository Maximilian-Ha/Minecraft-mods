package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineRotaryFurnaceBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineRotaryFurnace.
 * Alle Fachkoordinaten sind unveraendert uebernommen.
 */
public class MachineRotaryFurnaceMenu extends MenuBase<MachineRotaryFurnaceBlockEntity> {

    public MachineRotaryFurnaceMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineRotaryFurnaceBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineRotaryFurnaceMenu(int id, Inventory inventory, MachineRotaryFurnaceBlockEntity be) {
        super(NtmMenuTypes.MACHINE_ROTARY_FURNACE.get(), id, be);

        // Zutaten
        this.addSlot(new SlotNonRetarded(be, 0, 8, 18));
        this.addSlot(new SlotNonRetarded(be, 1, 26, 18));
        this.addSlot(new SlotNonRetarded(be, 2, 44, 18));
        // Fluidkennung
        this.addSlot(new SlotNonRetarded(be, 3, 8, 54));
        // fester Brennstoff
        this.addSlot(new SlotNonRetarded(be, 4, 44, 54));

        this.playerInv(inventory, 8, 104, 162);
    }
}
