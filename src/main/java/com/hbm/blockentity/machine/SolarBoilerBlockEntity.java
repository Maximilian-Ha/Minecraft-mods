package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;

/**
 * Solarkessel: nimmt die von den Heliostatspiegeln gebuendelte Waerme auf und
 * verdampft damit Wasser zu Dampf (1 mB Wasser -> 100 mB Dampf).
 */
public class SolarBoilerBlockEntity extends LoadedBaseBlockEntity implements ITickable, IFluidStandardTransceiverMK2, IFluidCopiable {

    // todo config: im Original feste Werte, keine JSON-Konfiguration
    private static final int WATER_CAP = 100;
    private static final int STEAM_CAP = 10_000;

    private final FluidTank water;
    private final FluidTank steam;
    /** Wieviel im letzten Tick verdampft wurde, nur zur Anzeige */
    public int display;
    /** Von den Spiegeln eingesammelte Waerme, wird jeden Servertick zurueckgesetzt */
    public int heat;

    /** Spiegelpositionen, die sich in diesem Clienttick gemeldet haben */
    public HashSet<BlockPos> primary = new HashSet<>();
    /** Verzoegerte Warteschlange, weil der Kessel nicht zwingend zuerst tickt */
    public HashSet<BlockPos> secondary = new HashSet<>();

    private AABB renderBox;

    public SolarBoilerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_SOLAR_BOILER.get(), pos, state);

        this.water = new FluidTank(Fluids.WATER, WATER_CAP);
        this.steam = new FluidTank(Fluids.STEAM, STEAM_CAP);
    }

    /** Anschluesse: oben ueber dem Extra-Block und unter dem Kern */
    public DirPos[] getConPos() {
        BlockPos pos = this.getBlockPos();
        return new DirPos[] {
                new DirPos(pos.above(3), Direction.UP),
                new DirPos(pos.below(), Direction.DOWN)
        };
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide) {

            for(DirPos pos : this.getConPos()) {
                this.trySubscribe(this.water.getTankType(), this.level, pos);
            }

            int process = this.heat / 50;
            this.display = process;
            process = Math.min(process, this.water.getFill());
            process = Math.min(process, (this.steam.getMaxFill() - this.steam.getFill()) / 100);

            if(process < 0) process = 0;

            this.water.setFill(this.water.getFill() - process);
            this.steam.setFill(this.steam.getFill() + process * 100);

            for(DirPos pos : this.getConPos()) {
                this.tryProvide(this.steam, this.level, pos);
            }

            this.heat = 0;

            this.networkPackNT(15);
        } else {

            this.secondary.clear();
            this.secondary.addAll(this.primary);
            this.primary.clear();
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.water.readFromNBT(tag, "water");
        this.steam.readFromNBT(tag, "steam");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.water.writeToNBT(tag, "water");
        this.steam.writeToNBT(tag, "steam");
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.display);
        this.water.serialize(buf);
        this.steam.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.display = buf.readInt();
        this.water.deserialize(buf);
        this.steam.deserialize(buf);
    }

    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.steam }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.water }; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.water, this.steam }; }

    @Override public FluidTank getTankToPaste() { return null; }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos pos = this.getBlockPos();
            this.renderBox = new AABB(
                    pos.getX() - 1,
                    pos.getY(),
                    pos.getZ() - 1,
                    pos.getX() + 2,
                    pos.getY() + 3,
                    pos.getZ() + 2
            );
        }
        return this.renderBox;
    }
}
