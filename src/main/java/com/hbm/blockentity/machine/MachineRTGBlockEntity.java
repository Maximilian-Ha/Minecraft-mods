package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyProviderMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.config.VersatileConfig;
import com.hbm.inventory.menus.MachineRTGMenu;
import com.hbm.items.machine.RTGPelletItem;
import com.hbm.util.RTGUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineRTG.
 *
 * Der RTG erzeugt Strom aus dem Zerfall der eingelegten Pellets: RTGUtil zaehlt die
 * Waerme aller 15 Slots zusammen, daraus werden pro Tick heat * 5 HE.
 *
 * Nicht uebernommen: IInfoProviderEC (CompatEnergyControl gibt es im Port nicht).
 */
public class MachineRTGBlockEntity extends MachineBaseBlockEntity implements IEnergyProviderMK2 {

    /** Alle 15 Slots sind von jeder Seite erreichbar, wie im Original. */
    public static final int[] SLOT_IO = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };

    public static final long MAX_POWER = 100000;

    public int heat;
    /** 600 wenn Pellets zerfallen, sonst 200 -- unveraendert aus dem Original. */
    public final int heatMax;
    public long power;

    public MachineRTGBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_RTG.get(), pos, state, 15);
        this.heatMax = VersatileConfig.rtgDecay() ? 600 : 200;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.rtg");
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        for(Direction dir : Direction.values()) {
            this.tryProvide(this.level, this.worldPosition.relative(dir), dir);
        }

        this.heat = RTGUtil.updateRTGs(this.slots, SLOT_IO);

        if(this.heat > this.heatMax) this.heat = this.heatMax;

        this.power += this.heat * 5L;
        if(this.power > MAX_POWER) this.power = MAX_POWER;

        this.networkPackNT(50);
    }

    public long getPowerScaled(long i) {
        return (this.power * i) / MAX_POWER;
    }

    public int getHeatScaled(int i) {
        return (this.heat * i) / this.heatMax;
    }

    public boolean hasPower() {
        return this.power > 0;
    }

    public boolean hasHeat() {
        return RTGUtil.hasHeat(this.slots, SLOT_IO);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof RTGPelletItem;
    }

    /** Herausnehmen von aussen ist gesperrt -- im Original canExtractItem == false. */
    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return SLOT_IO;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.heat = tag.getInt("heat");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        tag.putInt("heat", this.heat);
    }

    /**
     * Das Original schickte die Energie ueber networkPackNT und die Waerme separat
     * ueber die Fortschrittsleiste des Containers. Im Port laeuft beides ueber das
     * BufPacket, damit der Bildschirm ohne eigenen Menue-Sync auskommt.
     */
    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.heat);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.heat = buf.readInt();
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return MAX_POWER; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineRTGMenu(id, inventory, this);
    }
}
