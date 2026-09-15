package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineReactorBreedingBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/** Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineReactorBreeding. */
public class MachineReactorBreedingMenu extends MenuBase<MachineReactorBreedingBlockEntity> {

    public MachineReactorBreedingMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineReactorBreedingBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineReactorBreedingMenu(int id, Inventory inventory, MachineReactorBreedingBlockEntity be) {
        super(NtmMenuTypes.MACHINE_REACTOR_BREEDING.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineReactorBreedingBlockEntity.SLOT_INPUT, 35, 35));
        this.addOutputSlots(inventory.player, be, MachineReactorBreedingBlockEntity.SLOT_OUTPUT, 125, 35, 1, 1);

        this.playerInv(inventory, 8, 84);
    }
}
