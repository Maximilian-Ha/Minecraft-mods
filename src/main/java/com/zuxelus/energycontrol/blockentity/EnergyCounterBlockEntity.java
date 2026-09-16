package com.zuxelus.energycontrol.blockentity;

import com.zuxelus.energycontrol.ECConfig;
import com.zuxelus.energycontrol.blocks.EnergyCounterBlock;
import com.zuxelus.energycontrol.energy.ECEnergyStorage;
import com.zuxelus.energycontrol.init.ECBlockEntityTypes;
import com.zuxelus.energycontrol.menus.EnergyCounterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Arrays;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.tileentities.TileEntityEnergyCounter.
 *
 * Ein Block, der in die Leitung gesetzt wird und zaehlt, was durch ihn hindurchgeht. Er
 * nimmt Strom auf allen Seiten ausser der Schauseite an und schiebt ihn auf der Schauseite
 * weiter -- die Texturen heissen im Original genau so, "input" und "output".
 *
 * Das Original war ein IC2-Netzteilnehmer und zaehlte EU. An die Stelle von IC2 tritt hier
 * die Energie-Schnittstelle von NeoForge, wie schon bei den Tafeln: der Zaehler sitzt damit
 * in jeder Leitung, die Forge-Energie fuehrt.
 *
 * Gezaehlt wird, was der Zaehler wirklich weitergegeben hat, nicht was er angenommen hat.
 * Steht die Leitung dahinter still, laeuft der Zaehler nicht weiter -- der Puffer fuellt
 * sich, und der Zufluss versiegt von selbst.
 */
public class EnergyCounterBlockEntity extends ECContainerBlockEntity {

    /** Wieviele Ticks in den Mittelwert eingehen -- eine Sekunde. */
    private static final int AVERAGE_TICKS = 20;

    private final int transfer = ECConfig.counterTransferRate();

    /**
     * Puffer fuer eine Sekunde bei voller Durchleitung; mehr muss der Zaehler nicht halten.
     * Bei sehr hoch eingestellter Durchleitung wuerde das Produkt ueberlaufen -- deshalb
     * wird in long gerechnet und bei Integer.MAX_VALUE abgeschnitten.
     */
    private final ECEnergyStorage energy = new ECEnergyStorage(
            (int) Math.min((long) AVERAGE_TICKS * transfer, Integer.MAX_VALUE), transfer);

    /** Alles, was seit dem letzten Zuruecksetzen durchgegangen ist. */
    private long counter;

    private final int[] recent = new int[AVERAGE_TICKS];
    private int recentIndex;
    private int lastTransfer;

    public EnergyCounterBlockEntity(BlockPos pos, BlockState state) {
        super(ECBlockEntityTypes.ENERGY_COUNTER.get(), pos, state, 0);
    }

    /**
     * Der Speicher, den die Nachbarn sehen -- aber nur von hinten. Auf der Schauseite gibt
     * der Zaehler ab, dort soll niemand einspeisen, und leersaugen laesst er sich nirgends
     * ({@code maxExtract} ist null).
     */
    public IEnergyStorage getEnergyStorage(Direction side) {
        return side == getOutputSide() ? null : energy;
    }

    public Direction getOutputSide() {
        BlockState state = getBlockState();
        return state.getBlock() instanceof EnergyCounterBlock ? state.getValue(EnergyCounterBlock.FACING) : Direction.UP;
    }

    public void tick() {
        if(level == null || level.isClientSide) return;

        int moved = push();
        counter += moved;

        recent[recentIndex] = moved;
        recentIndex = (recentIndex + 1) % AVERAGE_TICKS;

        // Einmal je Sekunde melden, und nur wenn sich etwas geaendert hat: der Durchsatz
        // schwankt sonst um einzelne FE, und jede Meldung ist ein Paket an jeden Zuschauer.
        if(level.getGameTime() % AVERAGE_TICKS != 0L) return;
        if(moved == lastTransfer) return;
        lastTransfer = moved;
        sync();
    }

    /** Schiebt weiter, was der Nachbar auf der Schauseite annimmt. */
    private int push() {
        Direction side = getOutputSide();
        IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                worldPosition.relative(side), side.getOpposite());
        if(target == null) return 0;

        int offered = energy.drain(transfer);
        if(offered <= 0) return 0;

        int accepted = target.receiveEnergy(offered, false);
        // Was der Nachbar nicht wollte, bleibt im Puffer.
        if(accepted < offered) energy.receiveEnergy(offered - accepted, false);
        return accepted;
    }

    public long getCounter() {
        return counter;
    }

    /** Durchsatz je Tick, gemittelt ueber die letzte Sekunde. */
    public double getAverage() {
        long sum = 0L;
        for(int value : recent) sum += value;
        return (double) sum / AVERAGE_TICKS;
    }

    public int getEnergyStored() {
        return energy.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energy.getMaxEnergyStored();
    }

    public void resetCounter() {
        counter = 0L;
        Arrays.fill(recent, 0);
        sync();
    }

    @Override
    public void receiveControl(Player player, CompoundTag tag) {
        if("reset".equals(tag.getString("action"))) resetCounter();
    }

    @Override
    protected void readProperties(CompoundTag tag, HolderLookup.Provider registries) {
        energy.setEnergyStored(tag.getInt("energy"));
        counter = tag.getLong("counter");
        lastTransfer = tag.getInt("lastTransfer");
        // Der Mittelwert wird nicht gespeichert: nach dem Laden ist er in einer Sekunde
        // wieder richtig, und ein gespeicherter Ringpuffer waere nur Ballast.
        Arrays.fill(recent, lastTransfer);
    }

    @Override
    protected void writeProperties(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("energy", energy.getEnergyStored());
        tag.putLong("counter", counter);
        tag.putInt("lastTransfer", lastTransfer);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.energycontrol.energy_counter");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new EnergyCounterMenu(id, inventory, this);
    }
}
