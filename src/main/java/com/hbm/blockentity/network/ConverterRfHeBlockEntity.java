package com.hbm.blockentity.network;

import api.hbm.energymk2.IEnergyProviderMK2;
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

import java.io.IOException;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityConverterRfHe.
 *
 * Die Gegenrichtung zum HE/RF-Wandler: nimmt Forge-Energie entgegen, die andere Mods
 * hineinschieben, und speist HE ins Stromnetz des Mods ein. Zwei FE ergeben fuenf HE.
 *
 * Der Wandler holt sich die Energie nicht selbst; er stellt nur einen Speicher bereit, in den
 * fremde Leitungen einzahlen. Genau so verhaelt sich auch das Original.
 */
public class ConverterRfHeBlockEntity extends LoadedBaseBlockEntity implements ITickable, IEnergyProviderMK2 {

    public static final long MAX_POWER = 5_000_000L;

    /** Konfigurierbar, siehe {@link com.hbm.config.MachineDynConfig}. */
    public static long rfInput = 2;
    public static long heOutput = 5;
    public static double inputDecay = 0.0D;

    public long power;
    public final NtmEnergyStorage storage = new NtmEnergyStorage(1_000_000, 1_000_000, 1_000_000);

    public ConverterRfHeBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.CONVERTER_RF_HE.get(), pos, state);
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        long rfUsed = Math.min(this.storage.getEnergyStored(), (MAX_POWER - this.power) * rfInput / heOutput);
        this.storage.setEnergyStored((int) (this.storage.getEnergyStored() - rfUsed));
        this.power += rfUsed * heOutput / rfInput;
        if(this.storage.getEnergyStored() > 0) this.storage.extractEnergy((int) Math.ceil(this.storage.getEnergyStored() * inputDecay), false);
        if(rfUsed > 0) this.setChanged();

        BlockPos pos = this.getBlockPos();
        for(Direction dir : Direction.values()) {
            this.tryProvide(this.level, pos.relative(dir), dir);
        }

        this.networkPackNT(15);
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return MAX_POWER; }

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
        rfInput = IConfigurableMachine.grab(obj, "L:RF_Used2", rfInput);
        heOutput = IConfigurableMachine.grab(obj, "L:HE_Created2", heOutput);
        inputDecay = IConfigurableMachine.grab(obj, "D:inputDecay2", inputDecay);
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("L:RF_Used2").value(rfInput);
        writer.name("L:HE_Created2").value(heOutput);
        writer.name("D:inputDecay2").value(inputDecay);
    }
}
