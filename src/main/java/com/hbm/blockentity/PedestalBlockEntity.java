package com.hbm.blockentity;

import com.hbm.blocks.generic.PedestalBlock;
import com.hbm.items.NtmItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: die eingebettete Klasse BlockPedestal.TileEntityPedestal.
 *
 * Der Sockel haelt genau einen Stapel. Getickt wird nur des Talismans wegen: liegt ein
 * Schutz- oder Meteoritentalisman darauf, meldet er sich alle zwanzig Takte beim
 * Eintragsregister des Blocks, und das Meteoritensystem fragt dort nach.
 *
 * NICHT UEBERNOMMEN: der goldene Entschaerfer, der im Original alle sechzig Takte die
 * Creeper im Umkreis entschaerft. Der Port kennt defuser_gold nicht -- es gibt den
 * Entschaerfer nur als Werkzeug (ToolType.DEFUSER), nicht als Ruestungsaufsatz, und
 * ItemModDefuser.castrateCreeper hat kein Gegenstueck. Eine Abfrage auf einen Gegenstand,
 * den es nicht gibt, waere eine Zeile, die nie zutrifft.
 */
public class PedestalBlockEntity extends BlockEntity {

    public ItemStack item = ItemStack.EMPTY;

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.PEDESTAL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PedestalBlockEntity sockel) {

        if(level.getGameTime() % 20 != 0 || sockel.item.isEmpty()) return;

        if(sockel.item.is(NtmItems.PROTECTION_CHARM.get())) {
            PedestalBlock.pushEntry(level, PedestalBlock.EntryType.CHARM_OF_PROTECTION, pos);
        }
        if(sockel.item.is(NtmItems.METEOR_CHARM.get())) {
            PedestalBlock.pushEntry(level, PedestalBlock.EntryType.METEORITE_CHARM, pos);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.item = tag.contains("item")
                ? ItemStack.parseOptional(registries, tag.getCompound("item"))
                : ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if(!this.item.isEmpty()) tag.put("item", this.item.save(registries, new CompoundTag()));
    }

    /* Der Client muss wissen, was auf dem Sockel liegt -- sonst zeichnet der Darsteller nichts. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
