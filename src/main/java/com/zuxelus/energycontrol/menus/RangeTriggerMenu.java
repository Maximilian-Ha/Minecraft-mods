package com.zuxelus.energycontrol.menus;

import com.zuxelus.energycontrol.blockentity.RangeTriggerBlockEntity;
import com.zuxelus.energycontrol.init.ECMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.containers.ContainerRangeTrigger.
 *
 * Die beiden Grenzen kommen als Zahl ueber das Steuerpaket; hier laeuft nur der Schalter
 * fuer die umgekehrte Redstone-Ausgabe.
 */
public class RangeTriggerMenu extends ECMenuBase<RangeTriggerBlockEntity> {

    public static final int BUTTON_INVERT = 100;

    public RangeTriggerMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (RangeTriggerBlockEntity) inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public RangeTriggerMenu(int id, Inventory inventory, RangeTriggerBlockEntity be) {
        super(ECMenuTypes.RANGE_TRIGGER.get(), id, be);

        addSlot(new SlotFiltered(be, RangeTriggerBlockEntity.SLOT_CARD, 8, 21));
        addSlot(new SlotFiltered(be, RangeTriggerBlockEntity.SLOT_UPGRADE_RANGE, 8, 39));

        addEnergySync(be::getEnergyStored);
        addPlayerInventory(inventory, 8, 108);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if(id != BUTTON_INVERT) return false;
        be.toggleInverted();
        return true;
    }
}
