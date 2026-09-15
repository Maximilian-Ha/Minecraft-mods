package com.hbm.blockentity.machine.rbmk;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.entity.projectile.RBMKDebris.DebrisType;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingType;
import com.hbm.inventory.menus.RBMKHeaterMenu;
import com.hbm.util.fauxpointtwelve.DirPos;
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

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKHeater.
 *
 * Der Waermetauscher. Anders als der Dampferzeuger ist er nicht auf Wasser festgelegt: er nimmt
 * jede Fluessigkeit mit der Eigenschaft "erhitzbar" und arbeitet deren ersten Erhitzungsschritt
 * ab. Welche das sein soll, gibt eine Fluessigkeitskennung im Fach vor.
 */
public class RBMKHeaterBlockEntity extends RBMKSlottedBaseBlockEntity implements IFluidStandardTransceiverMK2 {

    public FluidTank feed;
    public FluidTank steam;

    public RBMKHeaterBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_HEATER.get(), pos, state, 1);

        this.feed = new FluidTank(Fluids.COOLANT, 16_000);
        this.steam = new FluidTank(Fluids.COOLANT_HOT, 16_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.rbmkHeater");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        // Die Kennung wird nur uebernommen, wenn der Tauscher kalt und leer ist.
        if(this.heat <= 50 || this.feed.getFill() <= 0) this.feed.setType(0, this.slots);

        if(this.feed.getTankType().hasTrait(FT_Heatable.class)) {

            FT_Heatable trait = this.feed.getTankType().getTrait(FT_Heatable.class);
            HeatingStep step = trait.getFirstStep();
            this.steam.setTankType(step.typeProduced);

            double tempRange = this.heat - this.steam.getTankType().temperature;
            double eff = trait.getEfficiency(HeatingType.HEATEXCHANGER);

            if(tempRange > 0 && eff > 0) {

                // Bezugsgroesse des Originals: 1 mB Wasser nimmt 200 TU auf und zugleich 0,1 Grad
                // aus einer RBMK-Saeule.
                double tuPerDegree = 2_000D * eff;

                int inputOps = this.feed.getFill() / step.amountReq;
                int outputOps = (this.steam.getMaxFill() - this.steam.getFill()) / step.amountProduced;
                int tempOps = (int) Math.floor((tempRange * tuPerDegree) / step.heatReq);
                int ops = Math.min(inputOps, Math.min(outputOps, tempOps));

                this.feed.setFill(this.feed.getFill() - step.amountReq * ops);
                this.steam.setFill(this.steam.getFill() + step.amountProduced * ops);
                this.heat -= (step.heatReq * ops / tuPerDegree) * eff;
            }

            if(eff <= 0) {
                this.feed.setTankType(Fluids.NONE);
                this.steam.setTankType(Fluids.NONE);
            }

        } else {
            this.feed.setTankType(Fluids.NONE);
            this.steam.setTankType(Fluids.NONE);
        }

        this.trySubscribe(this.feed.getTankType(), this.level, new DirPos(this.worldPosition.below(), Direction.DOWN));

        if(this.steam.getFill() > 0) {
            for(DirPos pos : this.getOutputPos()) this.tryProvide(this.steam, this.level, pos);
        }

        super.updateEntity();
    }

    @Override
    public FluidTank[] getSendingTanks() { return new FluidTank[] { this.steam }; }

    @Override
    public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.feed }; }

    @Override
    public FluidTank[] getAllTanks() { return new FluidTank[] { this.feed, this.steam }; }

    @Override
    public RBMKColumnType getConsoleType() {
        return RBMKColumnType.HEATEX;
    }

    @Override
    public CompoundTag getNBTForConsole() {
        CompoundTag data = super.getNBTForConsole();
        data.putInt("water", this.feed.getFill());
        data.putInt("maxWater", this.feed.getMaxFill());
        data.putInt("steam", this.steam.getFill());
        data.putInt("maxSteam", this.steam.getMaxFill());
        data.putShort("type", (short) this.feed.getTankType().getID());
        data.putShort("hottype", (short) this.steam.getTankType().getID());
        return data;
    }

    @Override
    public RBMKType getRBMKType() {
        return RBMKType.OTHER;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.feed.readFromNBT(tag, "feed");
        this.steam.readFromNBT(tag, "steam");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.feed.writeToNBT(tag, "feed");
        this.steam.writeToNBT(tag, "steam");
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        this.feed.serialize(buf);
        this.steam.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.feed.deserialize(buf);
        this.steam.deserialize(buf);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RBMKHeaterMenu(id, inventory, this);
    }

    /* Der Waermetauscher wirft Stahltraeger aus. */
    @Override
    public void onMelt(int reduce) {

        if(this.level != null && !this.level.isClientSide) {
            int count = 1 + this.level.random.nextInt(2);
            for(int i = 0; i < count; i++) this.spawnDebris(DebrisType.BLANK);
        }

        super.onMelt(reduce);
    }
}
