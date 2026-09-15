package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.fusion.FusionKlystronBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/** Portiert aus 1.7.10: com.hbm.inventory.container.ContainerFusionKlystron. Nur das Batteriefach. */
public class FusionKlystronMenu extends MenuBase<FusionKlystronBlockEntity> {

    public FusionKlystronMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (FusionKlystronBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public FusionKlystronMenu(int id, Inventory inventory, FusionKlystronBlockEntity be) {
        super(NtmMenuTypes.FUSION_KLYSTRON.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, FusionKlystronBlockEntity.SLOT_BATTERY, 8, 72));

        this.playerInv(inventory, 17, 118);
    }
}
