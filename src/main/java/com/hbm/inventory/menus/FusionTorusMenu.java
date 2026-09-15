package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.fusion.FusionTorusBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerFusionTorus.
 *
 * Drei Faecher: Batterie, Blaupause und das Ausgabefach. Alle Fluide gehen ueber die Anschluesse,
 * nicht ueber die Oberflaeche.
 */
public class FusionTorusMenu extends MenuBase<FusionTorusBlockEntity> {

    public FusionTorusMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (FusionTorusBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public FusionTorusMenu(int id, Inventory inventory, FusionTorusBlockEntity be) {
        super(NtmMenuTypes.FUSION_TORUS.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, FusionTorusBlockEntity.SLOT_BATTERY, 8, 82));
        this.addSlot(new SlotNonRetarded(be, FusionTorusBlockEntity.SLOT_BLUEPRINT, 71, 81));
        this.addSlot(new SlotNonRetarded(be, FusionTorusBlockEntity.SLOT_OUTPUT, 130, 36));

        this.playerInv(inventory, 35, 162);
    }
}
