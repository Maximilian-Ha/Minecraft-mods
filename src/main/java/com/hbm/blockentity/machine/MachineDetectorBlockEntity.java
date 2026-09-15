package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.TickingBaseBlockEntity;
import com.hbm.blocks.machine.MachineDetectorBlock;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineDetector.
 *
 * Der Strommelder ist die einfachste Maschine des ganzen Mods: er nimmt Strom an und gibt ein
 * Rotsteinsignal ab, solange welcher kommt. Mehr tut er nicht.
 *
 * SEIN SPEICHER FASST FUENF HE, und das ist kein Schreibfehler. Er soll nichts speichern,
 * sondern melden: je Tick geht ein HE weg, und kommt keins nach, faellt das Signal binnen
 * fuenf Ticks. So zeigt er, ob ein Netz GERADE liefert -- nicht, ob es einmal geliefert hat.
 *
 * ER MELDET SICH MIT HOHEM VORRANG an. Ein Netz unter Last soll ihn trotzdem bedienen; ein
 * Melder, der bei Knappheit als Erster verhungert, meldete das Falsche.
 *
 * ABWEICHUNG: der Zustand steht in der Blockstate-Eigenschaft POWERED statt in den Metadaten.
 * Das ist auf 1.21 derselbe Gedanke mit anderem Namen.
 */
public class MachineDetectorBlockEntity extends TickingBaseBlockEntity implements IEnergyReceiverMK2 {

    public static final long MAX_POWER = 5;

    public long power;

    public MachineDetectorBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_DETECTOR.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        for(Direction dir : Direction.values()) {
            this.trySubscribe(this.level, new DirPos(this.worldPosition.relative(dir), dir));
        }

        boolean on = this.power > 0;
        if(on) this.power--;

        BlockState state = this.getBlockState();

        if(state.getValue(MachineDetectorBlock.POWERED) != on) {
            this.level.setBlock(this.worldPosition, state.setValue(MachineDetectorBlock.POWERED, on), 3);
            this.setChanged();
        }
    }

    @Override public boolean canConnect(Direction dir) { return true; }

    @Override public ConnectionPriority getPriority() { return ConnectionPriority.HIGH; }

    @Override public void setPower(long power) { this.power = power; }
    @Override public long getPower() { return this.power; }
    @Override public long getMaxPower() { return MAX_POWER; }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
    }
}
