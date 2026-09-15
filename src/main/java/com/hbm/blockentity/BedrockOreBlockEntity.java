package com.hbm.blockentity;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
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
 * Portiert aus 1.7.10: die eingebettete Klasse BlockBedrockOreTE.TileEntityBedrockOre.
 *
 * Was in einem Grundgesteinserz steckt: der Gegenstand, den es hergibt, die Stufe des Bohrers,
 * die es dafuer braucht, und -- bei den ergiebigen -- die Saeure, die vorher hineinmuss.
 *
 * SIE TICKT NICHT. Sie liegt nur da und wartet auf den Bagger.
 *
 * Farbe und Form bestimmen bloss das Aussehen: das Original wuerfelt aus zehn Erzmustern eines
 * aus und faerbt es nach dem Inhalt ein.
 */
public class BedrockOreBlockEntity extends BlockEntity {

    public ItemStack resource = ItemStack.EMPTY;
    public FluidStack acidRequirement;
    public int tier;
    public int color;
    public int shape;

    public BedrockOreBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.BEDROCK_ORE.get(), pos, state);
    }

    public BedrockOreBlockEntity setStyle(int color, int shape) {
        this.color = color;
        this.shape = shape;
        return this;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.resource = tag.contains("resource")
                ? ItemStack.parse(registries, tag.getCompound("resource")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;

        FluidType type = Fluids.fromID(tag.getInt("fluid"));
        this.acidRequirement = type != Fluids.NONE ? new FluidStack(type, tag.getInt("amount")) : null;

        this.tier = tag.getInt("tier");
        this.color = tag.getInt("color");
        this.shape = tag.getInt("shape");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if(!this.resource.isEmpty()) tag.put("resource", this.resource.save(registries, new CompoundTag()));

        if(this.acidRequirement != null) {
            tag.putInt("fluid", this.acidRequirement.type.getID());
            tag.putInt("amount", this.acidRequirement.fill);
        }

        tag.putInt("tier", this.tier);
        tag.putInt("color", this.color);
        tag.putInt("shape", this.shape);
    }

    /* Der Client braucht Inhalt, Farbe und Form -- ohne sie saehe jedes Erz gleich aus und die
     * Anzeige beim Hinsehen bliebe leer. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
