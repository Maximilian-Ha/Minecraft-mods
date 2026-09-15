package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.fusion.FusionBreederBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/** Portiert aus 1.7.10: com.hbm.inventory.container.ContainerFusionBreeder. */
public class FusionBreederMenu extends MenuBase<FusionBreederBlockEntity> {

    public FusionBreederMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (FusionBreederBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public FusionBreederMenu(int id, Inventory inventory, FusionBreederBlockEntity be) {
        super(NtmMenuTypes.FUSION_BREEDER.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, FusionBreederBlockEntity.SLOT_FLUID_ID, 26, 72));
        this.addSlot(new SlotNonRetarded(be, FusionBreederBlockEntity.SLOT_INPUT, 48, 45));
        this.addSlot(new SlotCraftingOutput(inventory.player, be, FusionBreederBlockEntity.SLOT_OUTPUT, 112, 45));

        this.playerInv(inventory, 8, 118);
    }
}
