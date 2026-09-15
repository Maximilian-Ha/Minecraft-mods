package com.hbm.blockentity.machine.rbmk;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.machine.rbmk.RBMKConsoleBlockEntity.RBMKColumn;
import com.hbm.util.Compat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKDisplay.
 *
 * Die Rasteranzeige: ein 7x7-Ausschnitt des Reaktors auf einer kleinen Tafel. Anders als das
 * Reaktorpult hat sie keine Oberflaeche und keine Bedienung -- sie zeigt nur an, wo welche Saeule
 * steht, wie heiss sie ist und, bei Regelstaeben, wie weit sie eingefahren ist.
 *
 * Der Zielpunkt wird mit dem Verbindungsstab gesetzt, die Ausrichtung des Rasters mit dem
 * Schraubenzieher gedreht -- genau wie beim Pult.
 */
public class RBMKDisplayBlockEntity extends LoadedBaseBlockEntity implements ITickable {

    public static final int GRID = 7;
    /** Nur jeden zehnten Tick wird abgetastet. */
    private static final int SCAN_INTERVAL = 10;

    private BlockPos target = BlockPos.ZERO;
    private byte rotation;

    /** Eindimensional, weil sich das viel einfacher verschicken laesst. */
    public RBMKColumn[] columns = new RBMKColumn[GRID * GRID];

    public RBMKDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_DISPLAY.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(this.level.getGameTime() % SCAN_INTERVAL == 0) {
            this.rescan();
            this.networkPackNT(50);
        }
    }

    /** Liest alle Saeulen im Raster neu ein. */
    private void rescan() {

        for(int index = 0; index < this.columns.length; index++) {

            BlockPos pos = this.target.offset(this.getXFromIndex(index), 0, this.getZFromIndex(index));
            BlockEntity te = Compat.getBlockEntityStandard(this.level, pos);

            if(te instanceof RBMKBaseBlockEntity rbmk) {

                RBMKColumn column = new RBMKColumn(rbmk.getConsoleType(), rbmk.getNBTForConsole());
                column.data.putDouble("heat", rbmk.heat);
                column.data.putDouble("maxHeat", rbmk.maxHeat());
                column.data.putByte("indicator", (byte) rbmk.craneIndicator);

                /*
                 * Die Farbe eines Regelstabs legt getNBTForConsole schon selbst bei, als Short mit
                 * -1 fuer "keine". Das Original traegt sie hier noch einmal nach; das waere hier
                 * doppelt gemoppelt und wuerde sich obendrein im Typ unterscheiden.
                 */

                this.columns[index] = column;

            } else {
                this.columns[index] = null;
            }
        }
    }

    public void setTarget(BlockPos pos) {
        this.target = pos.immutable();
        this.setChanged();
    }

    public void rotate() {
        this.rotation = (byte) ((this.rotation + 1) % 4);
        this.setChanged();
    }

    public int getXFromIndex(int col) {
        int i = col % GRID - 3;
        int j = col / GRID - 3;
        return switch(this.rotation) {
            case 1 -> -j;
            case 2 -> -i;
            case 3 -> j;
            default -> i;
        };
    }

    public int getZFromIndex(int col) {
        int i = col % GRID - 3;
        int j = col / GRID - 3;
        return switch(this.rotation) {
            case 1 -> i;
            case 2 -> -j;
            case 3 -> -i;
            default -> j;
        };
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);

        for(RBMKColumn column : this.columns) {
            if(column == null || column.type == null) {
                buf.writeByte(-1);
            } else {
                buf.writeByte((byte) column.type.ordinal());
                buf.writeNbt(column.data);
            }
        }
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);

        for(int i = 0; i < this.columns.length; i++) {
            byte ordinal = buf.readByte();
            if(ordinal < 0 || ordinal >= RBMKColumnType.values().length) {
                this.columns[i] = null;
            } else {
                this.columns[i] = new RBMKColumn(RBMKColumnType.values()[ordinal], buf.readNbt());
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.target = new BlockPos(tag.getInt("tX"), tag.getInt("tY"), tag.getInt("tZ"));
        this.rotation = tag.getByte("rotation");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("tX", this.target.getX());
        tag.putInt("tY", this.target.getY());
        tag.putInt("tZ", this.target.getZ());
        tag.putByte("rotation", this.rotation);
    }
}
