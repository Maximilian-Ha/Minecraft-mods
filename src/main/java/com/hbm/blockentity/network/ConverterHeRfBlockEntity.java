package com.hbm.blockentity.network;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.config.IConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.io.IOException;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityConverterHeRf.
 *
 * Nimmt HE aus dem Stromnetz des Mods entgegen und gibt Forge-Energie an die Nachbarn ab.
 * Die Umrechnung ist bewusst verlustbehaftet: fuenf HE ergeben eine Einheit FE.
 *
 * Wichtig zum Verstaendnis des Gesamtbildes: HBM hat KEINE durchgehende Bruecke zwischen
 * seinem Netz und RF/FE. Der einzige Uebergang sind diese beiden Wandlerbloecke -- wer HE und
 * FE verbinden will, muss sie aufstellen. Das ist im Original so und bleibt hier so.
 *
 * Auf 1.21 tritt NeoForges IEnergyStorage an die Stelle der COFH-Schnittstelle; angemeldet
 * wird es ueber Capabilities.EnergyStorage.BLOCK in com.hbm.lib.NtmCapabilities.
 */
public class ConverterHeRfBlockEntity extends LoadedBaseBlockEntity implements ITickable, IEnergyReceiverMK2 {

    public static final long MAX_POWER = 5_000_000L;

    /** Konfigurierbar, siehe {@link com.hbm.config.MachineDynConfig}. */
    public static long heInput = 5;
    public static long rfOutput = 1;
    public static double inputDecay = 0.0D;

    public long power;
    public final NtmEnergyStorage storage = new NtmEnergyStorage(1_000_000, 1_000_000, 1_000_000);

    public ConverterHeRfBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.CONVERTER_HE_RF.get(), pos, state);
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        long rfCreated = Math.min(this.storage.getMaxEnergyStored() - this.storage.getEnergyStored(), this.power / heInput * rfOutput);
        this.power -= rfCreated * heInput / rfOutput;
        this.storage.setEnergyStored((int) (this.storage.getEnergyStored() + rfCreated));
        if(this.power > 0) this.power *= (1D - inputDecay);
        if(rfCreated > 0) this.setChanged();

        BlockPos pos = this.getBlockPos();

        for(Direction dir : Direction.values()) {
            this.trySubscribe(this.level, pos.relative(dir), dir);

            IEnergyStorage neighbour = this.level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(dir), dir.getOpposite());
            if(neighbour == null || !neighbour.canReceive()) continue;

            int available = this.storage.extractEnergy(this.storage.getMaxExtract(), true);
            int transferred = neighbour.receiveEnergy(available, false);
            this.storage.extractEnergy(transferred, false);
        }

        this.networkPackNT(15);
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return MAX_POWER; }
    @Override public ConnectionPriority getPriority() { return ConnectionPriority.LOW; }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.storage.setEnergyStored(tag.getInt("energy"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        tag.putInt("energy", this.storage.getEnergyStored());
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.storage.getEnergyStored());
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.storage.setEnergyStored(buf.readInt());
    }

    public static void readConfig(JsonObject obj) {
        heInput = IConfigurableMachine.grab(obj, "L:HE_Used", heInput);
        rfOutput = IConfigurableMachine.grab(obj, "L:RF_Created", rfOutput);
        inputDecay = IConfigurableMachine.grab(obj, "D:inputDecay2", inputDecay);
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("L:HE_Used").value(heInput);
        writer.name("L:RF_Created").value(rfOutput);
        writer.name("D:inputDecay2").value(inputDecay);
    }
}
