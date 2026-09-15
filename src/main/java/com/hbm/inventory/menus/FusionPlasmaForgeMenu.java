package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.fusion.FusionPlasmaForgeBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachinePlasmaForge.
 *
 * Batterie, Bauplan, Booster, zwoelf Eingabefaecher in vier Spalten und ein Ausgabefach.
 */
public class FusionPlasmaForgeMenu extends MenuBase<FusionPlasmaForgeBlockEntity> {

    public FusionPlasmaForgeMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (FusionPlasmaForgeBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public FusionPlasmaForgeMenu(int id, Inventory inventory, FusionPlasmaForgeBlockEntity be) {
        super(NtmMenuTypes.FUSION_PLASMA_FORGE.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, FusionPlasmaForgeBlockEntity.SLOT_BATTERY, 152, 82));
        this.addSlot(new SlotNonRetarded(be, FusionPlasmaForgeBlockEntity.SLOT_BLUEPRINT, 35, 81));
        this.addSlot(new SlotNonRetarded(be, FusionPlasmaForgeBlockEntity.SLOT_BOOSTER, 98, 116));
        this.addSlots(be, FusionPlasmaForgeBlockEntity.SLOT_INPUT_FIRST, 8, 18, 3, 4);
        this.addOutputSlots(inventory.player, be, FusionPlasmaForgeBlockEntity.SLOT_OUTPUT, 116, 36, 1, 1);

        this.playerInv(inventory, 8, 162);
    }
}
