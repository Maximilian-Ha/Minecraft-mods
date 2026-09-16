package com.zuxelus.energycontrol.blockentity;

import com.zuxelus.energycontrol.blocks.ThermalMonitorBlock;
import com.zuxelus.energycontrol.crossmod.CrossModLoader;
import com.zuxelus.energycontrol.init.ECBlockEntityTypes;
import com.zuxelus.energycontrol.menus.ThermalMonitorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.tileentities.TileEntityThermalMonitor.
 *
 * Der Waermemelder sucht in seiner Nachbarschaft einen Reaktor und gibt ein
 * Redstone-Signal, sobald dessen Temperatur die eingestellte Schwelle erreicht. Welche
 * Bloecke als Reaktor zaehlen, weiss die jeweilige Anbindung -- fuer HBM sind das die
 * RBMK-Saeulen, der ZIRNOX, der Forschungsreaktor und der Watz.
 */
public class ThermalMonitorBlockEntity extends ECContainerBlockEntity {

    public static final int STATUS_NO_REACTOR = 0;
    public static final int STATUS_OK = 1;
    public static final int STATUS_ALARM = 2;

    /** Die Schwellen, die der Knopf in der Oberflaeche durchschaltet. */
    public static final int[] HEAT_STEPS = { 100, 250, 500, 1000, 2000, 5000, 10000, 25000, 50000 };

    private int heatLevel = 500;
    private boolean invertRedstone;
    private int status = STATUS_NO_REACTOR;
    private int heat = -1;

    public ThermalMonitorBlockEntity(BlockPos pos, BlockState state) {
        super(ECBlockEntityTypes.THERMAL_MONITOR.get(), pos, state, 0);
    }

    public void tick() {
        if(level == null || level.isClientSide) return;
        // Einmal je Sekunde genuegt; die Suche geht ueber gut 150 Bloecke.
        if(level.getGameTime() % 20L != 0L) return;

        int newHeat = CrossModLoader.getHeat(level, worldPosition);
        int newStatus = newHeat < 0 ? STATUS_NO_REACTOR : newHeat >= heatLevel ? STATUS_ALARM : STATUS_OK;
        // Der Zustand haengt am Blockzustand, die Temperatur nur am Abgleich -- die
        // Oberflaeche zeigt sie an und soll nicht auf den naechsten Zustandswechsel warten.
        if(newStatus == status && newHeat == heat) return;

        boolean statusChanged = newStatus != status;
        heat = newHeat;
        status = newStatus;
        if(statusChanged) {
            updateBlockState();
            notifyNeighbours();
        }
        sync();
    }

    private void updateBlockState() {
        if(level == null) return;
        BlockState state = getBlockState();
        if(!(state.getBlock() instanceof ThermalMonitorBlock)) return;
        if(state.getValue(ThermalMonitorBlock.STATUS) == status) return;
        level.setBlock(worldPosition, state.setValue(ThermalMonitorBlock.STATUS, status), Block.UPDATE_ALL);
    }

    /** Was der Block an Redstone abgibt: 15 bei Alarm, umgekehrt bei gesetzter Umkehr. */
    public int getSignal() {
        boolean active = status == STATUS_ALARM;
        return active != invertRedstone ? 15 : 0;
    }

    public int getHeat() {
        return heat;
    }

    public int getHeatLevel() {
        return heatLevel;
    }

    public int getStatus() {
        return status;
    }

    public boolean isInverted() {
        return invertRedstone;
    }

    public void toggleInverted() {
        invertRedstone = !invertRedstone;
        notifyNeighbours();
        sync();
    }

    /** Schaltet auf die naechste Schwelle weiter, mit Umlauf. */
    public void cycleHeatLevel(boolean backwards) {
        int index = 0;
        for(int i = 0; i < HEAT_STEPS.length; i++) {
            if(HEAT_STEPS[i] == heatLevel) {
                index = i;
                break;
            }
        }
        index = (index + (backwards ? HEAT_STEPS.length - 1 : 1)) % HEAT_STEPS.length;
        heatLevel = HEAT_STEPS[index];
        sync();
    }

    private void notifyNeighbours() {
        if(level != null && !level.isClientSide) level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }

    @Override
    protected void readProperties(CompoundTag tag, HolderLookup.Provider registries) {
        heatLevel = tag.contains("heatLevel") ? tag.getInt("heatLevel") : 500;
        invertRedstone = tag.getBoolean("invert");
        status = tag.getInt("status");
        heat = tag.contains("heat") ? tag.getInt("heat") : -1;
    }

    @Override
    protected void writeProperties(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("heatLevel", heatLevel);
        tag.putBoolean("invert", invertRedstone);
        tag.putInt("status", status);
        tag.putInt("heat", heat);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.energycontrol.thermal_monitor");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ThermalMonitorMenu(id, inventory, this);
    }
}
