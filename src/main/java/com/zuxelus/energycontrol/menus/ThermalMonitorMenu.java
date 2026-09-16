package com.zuxelus.energycontrol.menus;

import com.zuxelus.energycontrol.blockentity.RemoteThermalMonitorBlockEntity;
import com.zuxelus.energycontrol.blockentity.ThermalMonitorBlockEntity;
import com.zuxelus.energycontrol.init.ECMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.containers.ContainerThermalMonitor.
 *
 * Der Waermemelder hat keine Faecher; die Schwelle kommt als Zahl ueber das Steuerpaket,
 * hier laeuft nur der Schalter fuer die umgekehrte Redstone-Ausgabe.
 *
 * Die Fernwaermeanzeige benutzt dieselbe Oberflaeche -- sie ist ein Waermemelder mit zwei
 * Faechern, und die kommen dazu, wenn der Block welche hat. Ein zweiter Oberflaechentyp
 * fuer denselben Inhalt waere doppelte Arbeit an zwei Stellen.
 */
public class ThermalMonitorMenu extends ECMenuBase<ThermalMonitorBlockEntity> {

    public static final int BUTTON_INVERT = 0;

    public ThermalMonitorMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (ThermalMonitorBlockEntity) inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public ThermalMonitorMenu(int id, Inventory inventory, ThermalMonitorBlockEntity be) {
        super(ECMenuTypes.THERMAL_MONITOR.get(), id, be);

        if(be instanceof RemoteThermalMonitorBlockEntity) {
            addSlot(new SlotFiltered(be, RemoteThermalMonitorBlockEntity.SLOT_CARD, 8, 53));
            addSlot(new SlotFiltered(be, RemoteThermalMonitorBlockEntity.SLOT_UPGRADE_RANGE, 26, 53));
        }

        addPlayerInventory(inventory, 8, 84);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if(id != BUTTON_INVERT) return false;
        be.toggleInverted();
        return true;
    }
}
