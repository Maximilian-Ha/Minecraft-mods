package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.storage.MachineBigAssTankBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerBarrel -- der Big-Ass Tank
 * benutzt dort die Oberflaeche des Fasses mit.
 */
public class MachineBigAssTankMenu extends MenuBase<MachineBigAssTankBlockEntity> {

    public MachineBigAssTankMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineBigAssTankBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineBigAssTankMenu(int id, Inventory inventory, MachineBigAssTankBlockEntity be) {
        super(NtmMenuTypes.MACHINE_BIGASSTANK.get(), id, be);

        this.addSlot(new Slot(be, 0, 8, 17));
        this.addSlot(new Slot(be, 1, 8, 53));
        this.addSlot(new Slot(be, 2, 35, 17));
        this.addSlot(new Slot(be, 3, 35, 53));
        this.addSlot(new Slot(be, 4, 125, 17));
        this.addSlot(new Slot(be, 5, 125, 53));

        this.playerInv(inventory, 8, 84);
    }

    /*
     * Nicht die Vorgabe aus MenuBase: die pruefen acht Bloecke zum Kern, der Bau ist aber
     * dreizehn breit. Wer an seiner Aussenkante steht, waere schon zu weit weg, und die
     * Oberflaeche fiele beim Oeffnen sofort wieder zu. Derselbe Abstand wie in
     * MachineBigAssTankBlockEntity.hasPermission.
     */
    @Override
    public boolean stillValid(Player player) {
        return this.be.hasPermission(player);
    }
}
