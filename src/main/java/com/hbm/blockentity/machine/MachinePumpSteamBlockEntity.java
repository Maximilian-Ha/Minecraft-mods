package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachinePumpSteam.
 *
 * Die Dampfpumpe. Sie braucht keinen Strom, sondern Dampf -- hundert Millibar je Zug, ein
 * Millibar Restdampf kommt zurueck --, und foerdert dafuer tausend Millibar Wasser.
 *
 * SIE IST DER ANFANG DES KREISLAUFS. Ein Kessel braucht Wasser, um Dampf zu machen; diese Pumpe
 * braucht Dampf, um Wasser zu holen. Wer den ersten Eimer von Hand einfuellt, bekommt danach
 * beides von selbst.
 *
 * Sie ist zehnmal langsamer als die elektrische, und das ist Absicht: sie ist die Pumpe fuer den
 * Anfang, nicht fuer die Fabrik.
 */
public class MachinePumpSteamBlockEntity extends MachinePumpBaseBlockEntity implements IFluidStandardTransceiverMK2 {

    public final FluidTank steam;
    public final FluidTank lps;

    public MachinePumpSteamBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_PUMP_STEAM.get(), pos, state);
        this.water = new FluidTank(Fluids.WATER, STEAM_SPEED * 100);
        this.steam = new FluidTank(Fluids.STEAM, 1_000);
        this.lps = new FluidTank(Fluids.SPENTSTEAM, 10);
    }

    @Override
    public void updateEntity() {

        if(this.level != null && !this.level.isClientSide) {

            for(DirPos pos : this.getConPos()) {
                this.trySubscribe(this.steam.getTankType(), this.level, pos);
                if(this.lps.getFill() > 0) this.tryProvide(this.lps, this.level, pos);
            }
        }

        super.updateEntity();
    }

    @Override
    protected boolean canOperate() {
        return this.steam.getFill() >= 100
                && this.lps.getFill() < this.lps.getMaxFill()
                && this.water.getFill() < this.water.getMaxFill();
    }

    @Override
    protected void operate() {
        this.steam.setFill(this.steam.getFill() - 100);
        this.lps.setFill(this.lps.getFill() + 1);
        this.water.setFill(Math.min(this.water.getFill() + STEAM_SPEED, this.water.getMaxFill()));
    }

    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.water, this.steam, this.lps }; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.water, this.lps }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.steam }; }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        this.steam.serialize(buf);
        this.lps.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.steam.deserialize(buf);
        this.lps.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.steam.readFromNBT(tag, "steam");
        this.lps.readFromNBT(tag, "lps");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.steam.writeToNBT(tag, "steam");
        this.lps.writeToNBT(tag, "lps");
    }
}
