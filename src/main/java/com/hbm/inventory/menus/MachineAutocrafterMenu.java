package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineAutocrafterBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotPattern;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerAutocrafter.
 * Alle Fachkoordinaten sind die des Originals.
 *
 * EIN ZEHNTES FACH FAELLT AUS DER REIHE. Die Faecher null bis acht sind Muster, wie beim
 * Auszieher und beim Greifer; das Fach neun ist die Rezeptvorschau. Dort liegt kein Muster,
 * sondern das Ergebnis -- und ein Rechtsklick darauf blaettert zum naechsten Rezept, statt eine
 * Vergleichsart weiterzuschalten. Deshalb faengt dieses Menue den Klick auf Fach neun ab, bevor
 * ihn der gemeinsame Unterbau als Musterklick behandelt.
 *
 * ABWEICHUNG: das Batteriefach nimmt nur Batterien an. Im Original nimmt es alles, weil dort die
 * Pruefung der Maschine fuer die Oberflaeche gar nicht gilt -- ein Versehen der alten
 * Fachklasse, kein Entwurf.
 */
public class MachineAutocrafterMenu extends FilterMenuBase<MachineAutocrafterBlockEntity> {

    public MachineAutocrafterMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineAutocrafterBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineAutocrafterMenu(int id, Inventory inventory, MachineAutocrafterBlockEntity be) {
        super(NtmMenuTypes.MACHINE_AUTOCRAFTER.get(), id, be);

        /* Muster */
        for(int row = 0; row < 3; row++) {
            for(int col = 0; col < 3; col++) {
                this.addSlot(new SlotPattern(be, col + row * 3, 44 + col * 18, 22 + row * 18));
            }
        }

        /* Vorschau */
        this.addSlot(new SlotPattern(be, MachineAutocrafterBlockEntity.SLOT_PREVIEW, 116, 40).allowStackSize());

        /* Zutaten */
        this.addSlots(be, MachineAutocrafterBlockEntity.SLOT_INGREDIENTS, 44, 86, 3, 3);

        this.addSlot(new SlotTakeOnly(be, MachineAutocrafterBlockEntity.SLOT_OUTPUT, 116, 104));
        this.addSlot(new SlotNonRetarded(be, MachineAutocrafterBlockEntity.SLOT_BATTERY, 17, 99));

        this.playerInv(inventory, 8, 158, 216);
    }

    @Override
    public void clicked(int slotId, int button, ClickType type, Player player) {

        if(slotId != MachineAutocrafterBlockEntity.SLOT_PREVIEW) {
            super.clicked(slotId, button, type, player);
            return;
        }

        /* Die Vorschau bewegt nichts: ein Rechtsklick blaettert, alles andere tut gar nichts. */
        if(button == 1 && type == ClickType.PICKUP && this.getSlot(slotId).hasItem()) {
            this.be.nextTemplate();
        }
    }

    /**
     * Aus Mustern und Vorschau laesst sich nichts herausschieben, und aus dem Rucksack geht nur
     * die Batterie hinein -- die Zutaten legt man von Hand ein oder das Band schiebt sie herein.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        if(index <= MachineAutocrafterBlockEntity.SLOT_PREVIEW) return ItemStack.EMPTY;

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem()) {

            ItemStack stack = slot.getItem();
            ret = stack.copy();

            if(index <= MachineAutocrafterBlockEntity.SLOT_BATTERY) {

                if(!this.moveItemStackTo(stack, MachineAutocrafterBlockEntity.SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;

            } else if(stack.getItem() instanceof IBatteryItem) {

                if(!this.moveItemStackTo(stack, MachineAutocrafterBlockEntity.SLOT_BATTERY, MachineAutocrafterBlockEntity.SLOTS, false)) return ItemStack.EMPTY;

            } else {
                return ItemStack.EMPTY;
            }

            if(stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return ret;
    }
}
