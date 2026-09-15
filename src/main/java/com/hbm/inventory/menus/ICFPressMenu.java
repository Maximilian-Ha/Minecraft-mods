package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.icf.ICFPressBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerICFPress.
 *
 * Links das Myon und seine leere Huelle, in der Mitte die beiden Stoffe -- oben die Fluidkennung,
 * unten der Barren -- und rechts die leere Kuegelhuelle mit dem fertigen Kuegelchen darunter.
 */
public class ICFPressMenu extends MenuBase<ICFPressBlockEntity> {

    public ICFPressMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (ICFPressBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level, extraData.readBlockPos()));
    }

    public ICFPressMenu(int id, Inventory inventory, ICFPressBlockEntity be) {
        super(NtmMenuTypes.ICF_PRESS.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, ICFPressBlockEntity.SLOT_PELLET_IN, 98, 18));
        this.addSlot(new SlotTakeOnly(be, ICFPressBlockEntity.SLOT_PELLET_OUT, 98, 54));
        this.addSlot(new SlotNonRetarded(be, ICFPressBlockEntity.SLOT_MUON_IN, 8, 18));
        this.addSlot(new SlotTakeOnly(be, ICFPressBlockEntity.SLOT_MUON_OUT, 8, 54));
        this.addSlot(new SlotNonRetarded(be, ICFPressBlockEntity.SLOT_MATERIAL_FIRST, 62, 54));
        this.addSlot(new SlotNonRetarded(be, ICFPressBlockEntity.SLOT_MATERIAL_SECOND, 134, 54));
        this.addSlot(new SlotNonRetarded(be, ICFPressBlockEntity.SLOT_FLUID_ID_FIRST, 62, 18));
        this.addSlot(new SlotNonRetarded(be, ICFPressBlockEntity.SLOT_FLUID_ID_SECOND, 134, 18));

        this.playerInv(inventory, 8, 97);
    }
}
