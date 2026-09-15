package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineAmmoPressBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.SlotNonRetarded;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class MachineAmmoPressMenu extends MenuBase<MachineAmmoPressBlockEntity> {

    public MachineAmmoPressMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineAmmoPressBlockEntity) inventory.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    public MachineAmmoPressMenu(int id, Inventory inventory, MachineAmmoPressBlockEntity be) {
        super(NtmMenuTypes.AMMO_PRESS.get(), id, be);

        /* Das Gitter sitzt rechts, die Rezeptliste links. */
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                this.addSlot(new SlotNonRetarded(be, i * 3 + j, 116 + j * 18, 18 + i * 18));
            }
        }

        this.addSlot(new SlotCraftingOutput(inventory.player, be, 9, 134, 72));

        this.playerInv(inventory, 8, 118);
    }
}
