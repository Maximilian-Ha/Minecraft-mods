package com.hbm.blockentity.machine.rbmk;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.menus.RBMKControlAutoMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKControlAuto.
 *
 * Der selbsttaetige Steuerstab. Er faehrt nicht auf Befehl, sondern nach der Temperatur seiner
 * eigenen Saeule: unterhalb der unteren Schwelle steht er auf dem einen Wert, oberhalb der
 * oberen auf dem anderen, dazwischen wird nach der gewaehlten Kurve ueberblendet.
 */
public class RBMKControlAutoBlockEntity extends RBMKControlBlockEntity {

    public double levelLower;
    public double levelUpper;
    public double heatLower;
    public double heatUpper;
    public RBMKFunction function = RBMKFunction.LINEAR;

    public RBMKControlAutoBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_CONTROL_AUTO.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.rbmkControlAuto");
    }

    @Override
    public void updateEntity() {

        if(this.level != null && !this.level.isClientSide) {

            double fauxLevel;

            double lowerBound = Math.min(this.heatLower, this.heatUpper);
            double upperBound = Math.max(this.heatLower, this.heatUpper);

            if(this.heat < lowerBound) {
                fauxLevel = this.levelLower;

            } else if(this.heat > upperBound) {
                fauxLevel = this.levelUpper;

            } else if(this.heatUpper == this.heatLower) {
                // Beide Schwellen gleich: es gibt keinen Bereich zum Ueberblenden, sonst waere
                // die Division unten durch null. Das Original faellt hier auf NaN herein.
                fauxLevel = this.levelUpper;

            } else {
                fauxLevel = switch(this.function) {
                    case LINEAR -> (this.heat - this.heatLower) * ((this.levelUpper - this.levelLower) / (this.heatUpper - this.heatLower)) + this.levelLower;
                    case QUAD_UP -> Math.pow((this.heat - this.heatLower) / (this.heatUpper - this.heatLower), 2) * (this.levelUpper - this.levelLower) + this.levelLower;
                    case QUAD_DOWN -> Math.pow((this.heat - this.heatUpper) / (this.heatLower - this.heatUpper), 2) * (this.levelLower - this.levelUpper) + this.levelUpper;
                };
            }

            this.targetLevel = Mth.clamp(fauxLevel * 0.01D, 0D, 1D);
        }

        super.updateEntity();
    }

    /**
     * Abweichung vom Original: dort klemmt receiveControl den Kurvenindex versehentlich gegen
     * die Laenge von RBMKColor (fuenf Werte) statt gegen die von RBMKFunction (drei) -- ein
     * Paket mit 3 oder 4 wirft dort eine ArrayIndexOutOfBoundsException. Hier wird gegen die
     * richtige Laenge geklemmt.
     */
    @Override
    public void receiveControl(CompoundTag tag) {

        if(tag.contains("function")) {
            int index = Math.abs(tag.getInt("function")) % RBMKFunction.values().length;
            this.function = RBMKFunction.values()[index];

        } else {
            this.levelLower = tag.getDouble("levelLower");
            this.levelUpper = tag.getDouble("levelUpper");
            this.heatLower = tag.getDouble("heatLower");
            this.heatUpper = tag.getDouble("heatUpper");
        }

        this.setChanged();
    }

    @Override
    public RBMKColumnType getConsoleType() {
        return RBMKColumnType.CONTROL_AUTO;
    }

    public enum RBMKFunction {
        LINEAR,
        QUAD_UP,
        QUAD_DOWN
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.levelLower = tag.getDouble("levelLower");
        this.levelUpper = tag.getDouble("levelUpper");
        this.heatLower = tag.getDouble("heatLower");
        this.heatUpper = tag.getDouble("heatUpper");
        this.function = RBMKFunction.values()[Math.abs(tag.getInt("function")) % RBMKFunction.values().length];
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("levelLower", this.levelLower);
        tag.putDouble("levelUpper", this.levelUpper);
        tag.putDouble("heatLower", this.heatLower);
        tag.putDouble("heatUpper", this.heatUpper);
        tag.putInt("function", this.function.ordinal());
    }

    /*
     * Abweichung vom Original: dort schreibt serialize die Kurve nur, wenn sie gesetzt ist,
     * deserialize liest sie aber immer -- der Puffer geriet damit aus dem Tritt. Hier wird sie
     * immer geschrieben, und die Kurve ist nie null.
     */
    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeDouble(this.levelLower);
        buf.writeDouble(this.levelUpper);
        buf.writeDouble(this.heatLower);
        buf.writeDouble(this.heatUpper);
        buf.writeInt(this.function.ordinal());
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.levelLower = buf.readDouble();
        this.levelUpper = buf.readDouble();
        this.heatLower = buf.readDouble();
        this.heatUpper = buf.readDouble();
        this.function = RBMKFunction.values()[Math.abs(buf.readInt()) % RBMKFunction.values().length];
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RBMKControlAutoMenu(id, inventory, this);
    }
}
