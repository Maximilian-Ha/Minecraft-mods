package com.hbm.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: die eingebettete Klasse BlockSupplyCrate.TileEntitySupplyCrate.
 *
 * Was in einer Nachschubkiste steckt -- nicht mehr als eine Liste von Gegenstaenden. Sie hat
 * keine Oberflaeche, keine Faecher und keine Schnittstelle nach aussen: was der Fallschirm
 * hineingelegt hat, faellt beim Aufbrechen wieder heraus, und sonst passiert hier nichts.
 *
 * SIE TICKT NICHT.
 *
 * Ueber IPersistentNBT wandert der Inhalt in den Gegenstand, wenn die Kiste abgebaut wird, und
 * aus ihm zurueck, wenn sie gesetzt wird -- derselbe Weg, den im Port schon die Lagerkisten
 * und die Faesser gehen. Das Original schreibt dafuer von Hand "slot0", "slot1" ... in die
 * Gegenstandsdaten; hier genuegt dieselbe Liste, die auch die Welt speichert.
 */
public class SupplyCrateBlockEntity extends BlockEntity implements IPersistentNBT {

    public final List<ItemStack> items = new ArrayList<>();

    public SupplyCrateBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.SUPPLY_CRATE.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.items.clear();
        ListTag liste = tag.getList("items", Tag.TAG_COMPOUND);
        for(int i = 0; i < liste.size(); i++) {
            ItemStack.parse(registries, liste.getCompound(i)).ifPresent(this.items::add);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        ListTag liste = new ListTag();
        for(ItemStack stueck : this.items) {
            if(!stueck.isEmpty()) liste.add(stueck.save(registries, new CompoundTag()));
        }
        tag.put("items", liste);
    }

    @Override
    public void writeNBT(CompoundTag savedTag) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, this.getLevel().registryAccess());
        savedTag.put(IPersistentNBT.NBT_PERSISTENT_KEY, tag);
    }

    @Override
    public void readNBT(CompoundTag savedTag) {
        if(savedTag.contains(IPersistentNBT.NBT_PERSISTENT_KEY)) {
            this.loadAdditional(savedTag.getCompound(IPersistentNBT.NBT_PERSISTENT_KEY), this.getLevel().registryAccess());
        }
    }
}
