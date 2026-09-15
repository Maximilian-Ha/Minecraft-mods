package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.ReactorControlBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/** Portiert aus 1.7.10: com.hbm.inventory.container.ContainerReactorControl. Nur das Fuehlerfach. */
public class ReactorControlMenu extends MenuBase<ReactorControlBlockEntity> {

    public ReactorControlMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (ReactorControlBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public ReactorControlMenu(int id, Inventory inventory, ReactorControlBlockEntity be) {
        super(NtmMenuTypes.REACTOR_CONTROL.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, ReactorControlBlockEntity.SLOT_SENSOR, 92, 38));

        this.playerInv(inventory, 8, 84);
    }
}
