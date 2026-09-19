package com.hbm.inventory.menus;

import com.hbm.blockentity.network.RadioTorchCounterBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotPattern;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerCounterTorch.
 *
 * Drei Musterfaecher uebereinander, sonst nichts. Der abgefangene Klick auf ein Musterfach
 * steckt in FilterMenuBase -- er ist bei allen Musterfiltern des Ports derselbe.
 */
public class RadioTorchCounterMenu extends FilterMenuBase<RadioTorchCounterBlockEntity> {

    public RadioTorchCounterMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (RadioTorchCounterBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public RadioTorchCounterMenu(int id, Inventory inventory, RadioTorchCounterBlockEntity be) {
        super(NtmMenuTypes.RADIO_TORCH_COUNTER.get(), id, be);

        for(int i = 0; i < RadioTorchCounterBlockEntity.KANAELE; i++) {
            this.addSlot(new SlotPattern(be, i, 138, 18 + 44 * i));
        }

        this.playerInv(inventory, 12, 156, 214);
    }
}
