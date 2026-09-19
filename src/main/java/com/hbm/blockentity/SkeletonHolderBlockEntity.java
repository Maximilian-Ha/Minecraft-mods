package com.hbm.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: die eingebettete Klasse BlockSkeletonHolder.TileEntitySkeletonHolder.
 *
 * Ein Sockel haelt genau einen Gegenstand, mehr nicht. Er tickt nicht -- er liegt da und
 * zeigt her, was man ihm gegeben hat.
 */
public class SkeletonHolderBlockEntity extends BlockEntity {

    public ItemStack item = ItemStack.EMPTY;

    public SkeletonHolderBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.SKELETON_HOLDER.get(), pos, state);
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
