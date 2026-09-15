package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.icf.ICFBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerICF.
 *
 * Oben die fuenf Faecher fuer frische Kuegelchen, darunter die Kammer, unten die fuenf fuer die
 * verbrauchten -- und links davon das Fach fuer den Kuehlmittelbehaelter.
 */
public class ICFMenu extends MenuBase<ICFBlockEntity> {

    public ICFMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (ICFBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level, extraData.readBlockPos()));
    }

    public ICFMenu(int id, Inventory inventory, ICFBlockEntity be) {
        super(NtmMenuTypes.ICF.get(), id, be);

        for(int i = 0; i < 5; i++) this.addSlot(new SlotNonRetarded(be, ICFBlockEntity.SLOT_IN_FIRST + i, 80 + i * 18, 18));
        this.addSlot(new SlotNonRetarded(be, ICFBlockEntity.SLOT_CHAMBER, 116, 54));
        for(int i = 0; i < 5; i++) this.addSlot(new SlotTakeOnly(be, ICFBlockEntity.SLOT_OUT_FIRST + i, 80 + i * 18, 90));
        this.addSlot(new SlotNonRetarded(be, ICFBlockEntity.SLOT_FLUID_ID, 44, 90));

        this.playerInv(inventory, 44, 140);
    }
}
