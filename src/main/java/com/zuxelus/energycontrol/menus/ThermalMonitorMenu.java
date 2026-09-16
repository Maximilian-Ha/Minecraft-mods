package com.zuxelus.energycontrol.menus;

import com.zuxelus.energycontrol.blockentity.ThermalMonitorBlockEntity;
import com.zuxelus.energycontrol.init.ECMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.containers.ContainerThermalMonitor.
 *
 * Der Waermemelder hat keine Faecher; die Oberflaeche zeigt nur den Messwert und die
 * beiden Schalter.
 */
public class ThermalMonitorMenu extends ECMenuBase<ThermalMonitorBlockEntity> {

    public static final int BUTTON_HEAT_UP = 0;
    public static final int BUTTON_HEAT_DOWN = 1;
    public static final int BUTTON_INVERT = 2;

    public ThermalMonitorMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (ThermalMonitorBlockEntity) inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public ThermalMonitorMenu(int id, Inventory inventory, ThermalMonitorBlockEntity be) {
        super(ECMenuTypes.THERMAL_MONITOR.get(), id, be);
        addPlayerInventory(inventory, 8, 84);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        switch(id) {
            case BUTTON_HEAT_UP -> be.cycleHeatLevel(false);
            case BUTTON_HEAT_DOWN -> be.cycleHeatLevel(true);
            case BUTTON_INVERT -> be.toggleInverted();
            default -> {
                return false;
            }
        }
        return true;
    }
}
