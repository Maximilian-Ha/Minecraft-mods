package com.zuxelus.energycontrol.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Einheitlicher Blick in das Inventar eines Blocks -- egal, auf welchem Weg er es anbietet.
 *
 * Die Inventarkarte prueft im Original nur auf {@link Container}, also auf das alte Inventar
 * am Block selbst. Auf 1.21.1 bieten viele Mods ausschliesslich
 * {@code Capabilities.ItemHandler.BLOCK} an und wurden damit gar nicht erkannt. Beide Wege
 * fuehren hier zusammen; gefragt wird zuerst nach dem Inventar am Block, weil das ohne
 * Umweg ueber eine Capability auskommt.
 */
public interface BlockInventory {

    int size();

    ItemStack get(int slot);

    /** Null, wenn an dieser Stelle nichts steht, das Gegenstaende fuehrt. */
    static BlockInventory of(Level level, BlockPos pos) {
        if(level == null) return null;

        BlockEntity be = level.getBlockEntity(pos);
        if(be instanceof Container container) return of(container);

        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        return handler != null ? of(handler) : null;
    }

    static BlockInventory of(Container container) {
        return new BlockInventory() {
            @Override public int size() { return container.getContainerSize(); }
            @Override public ItemStack get(int slot) { return container.getItem(slot); }
        };
    }

    static BlockInventory of(IItemHandler handler) {
        return new BlockInventory() {
            @Override public int size() { return handler.getSlots(); }
            @Override public ItemStack get(int slot) { return handler.getStackInSlot(slot); }
        };
    }
}
