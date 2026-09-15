package com.hbm.blockentity.machine;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.interfaces.ICopiable;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityFoundryBase.
 *
 * Die Wurzel aller Giessereibloecke -- Rinnen, Formen, Becken, Behaelter. So ein Block haelt
 * immer nur ein Material auf einmal und tut damit entweder nichts (lagern) oder gibt es weiter.
 *
 * ABWEICHUNG: das Original stoesst bei jeder Aenderung ein markBlockForUpdate an, also ein
 * vollstaendiges Blockupdate. Der Port benutzt stattdessen networkPackNT, das ohnehin nur
 * sendet, wenn sich etwas geaendert hat -- dieselbe Sparsamkeit, nur ohne Blockupdate.
 */
public abstract class FoundryBaseBlockEntity extends LoadedBaseBlockEntity implements ICrucibleAcceptor, ICopiable, ITickable {

    public NTMMaterial type;
    public int amount;

    public FoundryBaseBlockEntity(BlockEntityType<? extends FoundryBaseBlockEntity> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        this.networkPackNT(50);
    }

    public abstract int getCapacity();

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.type == null ? -1 : this.type.id);
        buf.writeInt(this.amount);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.type = Mats.matById.get(buf.readInt());
        this.amount = buf.readInt();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.type = Mats.matById.get(tag.getInt("type"));
        this.amount = tag.getInt("amount");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("type", this.type == null ? -1 : this.type.id);
        tag.putInt("amount", this.amount);
    }

    /**
     * Die uebliche Pruefung, ob dieser Stapel hinein darf:
     * passt das Material zu dem, was schon drin ist, und ist noch Platz?
     */
    public boolean standardCheck(Level level, BlockPos pos, @Nullable Direction side, MaterialStack stack) {
        if(this.type != null && this.type != stack.material && this.amount > 0) return false;
        return this.amount < this.getCapacity();
    }

    /**
     * Das uebliche Hineingeben, ob gegossen oder geflossen: Material uebernehmen, Menge bis zum
     * Anschlag erhoehen und zurueckgeben, was nicht mehr hineinpasst.
     */
    public MaterialStack standardAdd(Level level, BlockPos pos, @Nullable Direction side, MaterialStack stack) {
        this.type = stack.material;

        if(stack.amount + this.amount <= this.getCapacity()) {
            this.amount += stack.amount;
            this.setChanged();
            return null;
        }

        int required = this.getCapacity() - this.amount;
        this.amount = this.getCapacity();
        stack.amount -= required;
        this.setChanged();

        return stack;
    }

    @Override
    public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        return this.standardCheck(level, pos, side, stack);
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        return this.standardAdd(level, pos, side, stack);
    }

    /** Wie oben, aber gegossen wird nur von oben. */
    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
        if(side != Direction.UP) return false;
        return this.standardCheck(level, pos, side, stack);
    }

    @Override
    public MaterialStack pour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
        return this.standardAdd(level, pos, side, stack);
    }

    @Override
    public CompoundTag getSettings(Level level, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        if(this.type != null) tag.putIntArray("matFilter", new int[] { this.type.id });
        return tag;
    }

    @Override
    public void pasteSettings(CompoundTag tag, int index, Level level, Player player, BlockPos pos) { }
}
