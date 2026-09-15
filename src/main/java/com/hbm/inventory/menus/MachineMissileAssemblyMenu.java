package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineMissileAssemblyBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.weapon.CustomMissilePartItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineMissileAssembly.
 *
 * Fuenf Faecher nebeneinander fuer die Bauteile, eines rechts fuer die fertige Rakete.
 *
 * DIE BAUTEILFAECHER NEHMEN NUR BAUTEILE. Das Original prueft das nicht, weil die Ampel es
 * ohnehin anzeigt; der Port haelt Unsinniges gleich draussen.
 */
public class MachineMissileAssemblyMenu extends MenuBase<MachineMissileAssemblyBlockEntity> {

    public MachineMissileAssemblyMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineMissileAssemblyBlockEntity) inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public MachineMissileAssemblyMenu(int id, Inventory inventory, MachineMissileAssemblyBlockEntity be) {
        super(NtmMenuTypes.MISSILE_ASSEMBLY.get(), id, be);

        for(int i = 0; i < 5; i++) {
            this.addSlot(new SlotNonRetarded(be, i, 8 + i * 18, 36) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof CustomMissilePartItem;
                }
            });
        }

        this.addSlot(new SlotTakeOnly(be, MachineMissileAssemblyBlockEntity.SLOT_OUTPUT, 152, 36));

        this.playerInv(inventory, 8, 140);
    }
}
