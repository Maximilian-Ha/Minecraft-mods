package com.zuxelus.energycontrol.menus;

import com.zuxelus.energycontrol.blockentity.KitAssemblerBlockEntity;
import com.zuxelus.energycontrol.init.ECMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.containers.ContainerKitAssembler.
 *
 * Sechs Eingabefaecher in zwei Spalten, ein Ausgabefach rechts. Die Fachstellen sind die
 * des Originals -- sie stehen so im Hintergrundbild.
 */
public class KitAssemblerMenu extends ECMenuBase<KitAssemblerBlockEntity> {

    private int progress;
    private int maxProgress;

    public KitAssemblerMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (KitAssemblerBlockEntity) inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public KitAssemblerMenu(int id, Inventory inventory, KitAssemblerBlockEntity be) {
        super(ECMenuTypes.KIT_ASSEMBLER.get(), id, be);

        addSlot(new Slot(be, 0, 8, 17));
        addSlot(new Slot(be, 1, 8, 35));
        addSlot(new Slot(be, 2, 8, 53));
        addSlot(new Slot(be, 3, 62, 17));
        addSlot(new Slot(be, 4, 62, 35));
        addSlot(new Slot(be, 5, 62, 53));
        addSlot(new SlotResultOnly(be, KitAssemblerBlockEntity.SLOT_RESULT, 117, 31));

        addPlayerInventory(inventory, 8, 84);

        addDataSlot(new DataSlot() {
            @Override public int get() { return be.getProgress(); }
            @Override public void set(int value) { progress = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return be.getMaxProgress(); }
            @Override public void set(int value) { maxProgress = value; }
        });
        addEnergySync(be::getEnergyStored);
    }

    /** Der Fortschritt, so wie der Client ihn kennt. */
    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }
}
