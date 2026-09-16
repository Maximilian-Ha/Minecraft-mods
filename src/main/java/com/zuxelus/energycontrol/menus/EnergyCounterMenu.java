package com.zuxelus.energycontrol.menus;

import com.zuxelus.energycontrol.blockentity.EnergyCounterBlockEntity;
import com.zuxelus.energycontrol.init.ECMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.containers.ContainerEnergyCounter.
 *
 * Der Zaehler hat keine Faecher. Zaehlerstand und Durchsatz stehen in der Block-Entitaet
 * und kommen ueber den Zustandsabgleich zum Client; das Zuruecksetzen geht ueber das
 * Steuerpaket, damit der Server die Reichweite prueft.
 */
public class EnergyCounterMenu extends ECMenuBase<EnergyCounterBlockEntity> {

    public EnergyCounterMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (EnergyCounterBlockEntity) inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public EnergyCounterMenu(int id, Inventory inventory, EnergyCounterBlockEntity be) {
        super(ECMenuTypes.ENERGY_COUNTER.get(), id, be);
        addPlayerInventory(inventory, 8, 84);
    }
}
