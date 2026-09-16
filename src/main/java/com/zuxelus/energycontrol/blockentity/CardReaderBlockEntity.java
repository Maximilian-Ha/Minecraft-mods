package com.zuxelus.energycontrol.blockentity;

import com.zuxelus.energycontrol.ECConfig;
import com.zuxelus.energycontrol.energy.ECEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Gemeinsames der Bloecke, die Karten auslesen: der Stromspeicher und der Zustand "laeuft".
 *
 * Im Original zogen diese Bloecke EU aus einem IC2-Netz. IC2 gibt es auf 1.21.1 nicht; an
 * seine Stelle tritt die Energie-Schnittstelle von NeoForge, die in NeoForge selbst steckt und
 * damit keine fremde Mod voraussetzt. Wer Strom hat, speist ein -- Mekanism, Thermal,
 * Immersive Engineering und jede andere Mod mit Forge-Energie, ohne dass hier eine Zeile je
 * Mod steht.
 *
 * HBMs eigenes Stromnetz (HE) ist davon getrennt; der Uebergang sind HBMs beide
 * Wandlerbloecke. Das ist die Bauweise des Originals, siehe docs/HBM-KOMPATIBILITAET.md.
 */
public abstract class CardReaderBlockEntity extends ECContainerBlockEntity {

    private final ECEnergyStorage energy = new ECEnergyStorage(ECConfig.energyCapacity(), ECConfig.energyCapacity());

    private boolean powered;

    protected CardReaderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state, size);
    }

    public ECEnergyStorage getEnergyStorage() {
        return energy;
    }

    public boolean isPowered() {
        return powered;
    }

    public int getEnergyStored() {
        return energy.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energy.getMaxEnergyStored();
    }

    /**
     * Zieht den Verbrauch dieser Runde ab. Laeuft auf dem Server, jeden Tick.
     *
     * Wechselt der Zustand, wird er den Clients gemeldet: der Renderer der Tafel zeichnet nur,
     * solange sie laeuft, und das muss er ohne Nachfrage wissen.
     */
    protected void tickPower() {
        boolean now = !ECConfig.requirePower() || energy.consume(ECConfig.energyConsumption());
        if(now == powered) return;

        powered = now;
        onPowerChanged();
        sync();
    }

    /** Was beim Wechsel zwischen "laeuft" und "steht" sonst noch zu tun ist. */
    protected void onPowerChanged() { }

    @Override
    protected void readProperties(CompoundTag tag, HolderLookup.Provider registries) {
        energy.setEnergyStored(tag.getInt("energy"));
        powered = tag.getBoolean("powered");
    }

    @Override
    protected void writeProperties(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("energy", energy.getEnergyStored());
        tag.putBoolean("powered", powered);
    }
}
