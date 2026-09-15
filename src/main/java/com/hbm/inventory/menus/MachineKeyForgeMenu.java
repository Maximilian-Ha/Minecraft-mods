package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineKeyForgeBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineKeyForge.
 * Alle Fachkoordinaten sind die des Originals.
 */
public class MachineKeyForgeMenu extends MenuBase<MachineKeyForgeBlockEntity> {

    public MachineKeyForgeMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineKeyForgeBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineKeyForgeMenu(int id, Inventory inventory, MachineKeyForgeBlockEntity be) {
        super(NtmMenuTypes.MACHINE_KEY_FORGE.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineKeyForgeBlockEntity.SLOT_TEMPLATE, 44, 36));
        this.addSlot(new SlotNonRetarded(be, MachineKeyForgeBlockEntity.SLOT_COPY, 80, 36));
        this.addSlot(new SlotNonRetarded(be, MachineKeyForgeBlockEntity.SLOT_RANDOM, 116, 36));

        this.playerInv(inventory, 8, 104, 162);
    }
}
