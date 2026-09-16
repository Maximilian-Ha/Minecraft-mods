package com.zuxelus.energycontrol.menus;

import com.zuxelus.energycontrol.blockentity.RangeTriggerBlockEntity;
import com.zuxelus.energycontrol.init.ECMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.containers.ContainerRangeTrigger.
 *
 * Die beiden Grenzen werden mit Knoepfen verschoben: je Schrittweite ein Paar plus und
 * minus. Im Original waren es zwei Textfelder; Knoepfe kommen ohne eigenes Netzwerkpaket
 * aus und sind mit dem Steuerpult daneben auch bequemer zu bedienen.
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

        addPlayerInventory(inventory, 8, 108);
    }

    /**
     * Die Kennung eines Verschiebeknopfes: Bit 0 sagt untere oder obere Grenze, Bit 1
     * plus oder minus, der Rest ist der Zeiger in die Schrittweiten.
     */
    public static int buttonId(boolean end, int stepIndex, boolean negative) {
        return (stepIndex << 2) | (negative ? 2 : 0) | (end ? 1 : 0);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if(id == BUTTON_INVERT) {
            be.toggleInverted();
            return true;
        }
        if(id < 0 || id >= (RangeTriggerBlockEntity.STEPS.length << 2)) return false;

        be.adjust((id & 1) != 0, id >> 2, (id & 2) != 0);
        return true;
    }
}
