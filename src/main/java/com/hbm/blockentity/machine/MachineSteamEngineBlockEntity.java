package com.hbm.blockentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.config.IConfigurableMachine;
import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.inventory.fluid.trait.FT_Coolable.CoolingType;
import com.hbm.util.fauxpointtwelve.DirPos;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.sounds.SoundSource;
import java.io.IOException;

public class MachineSteamEngineBlockEntity extends LoadedBaseBlockEntity implements IEnergyProviderMK2, IFluidStandardTransceiverMK2, ITickable, IFluidCopiable {

    public long powerBuffer;

    public float rotor;
    public float lastRotor;
    private float syncRotor;
    public FluidTank[] tanks;

    private int turnProgress;
    private float acceleration = 0F;

    // todo config: im Original ueber IConfigurableMachine (steamengine.json) einstellbar
    public static int steamCap = 2_000;
    public static int ldsCap = 20;
    public static double efficiency = 0.85D;

    protected ByteBuf buf;
    private AABB renderBox;

    public MachineSteamEngineBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_STEAM_ENGINE.get(), pos, state);

        tanks = new FluidTank[2];
        tanks[0] = new FluidTank(Fluids.STEAM, steamCap);
        tanks[1] = new FluidTank(Fluids.SPENTSTEAM, ldsCap);
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide) {

            if(this.buf != null) this.buf.release();
            this.buf = Unpooled.buffer();

            this.powerBuffer = 0;

            tanks[0].setTankType(Fluids.STEAM);
            tanks[1].setTankType(Fluids.SPENTSTEAM);

            tanks[0].serialize(buf);

            FT_Coolable trait = tanks[0].getTankType().getTrait(FT_Coolable.class);

            if(trait != null) {
                double eff = trait.getEfficiency(CoolingType.TURBINE) * efficiency;

                int inputOps = tanks[0].getFill() / trait.amountReq;
                int outputOps = (tanks[1].getMaxFill() - tanks[1].getFill()) / trait.amountProduced;
                int ops = Math.min(inputOps, outputOps);
                tanks[0].setFill(tanks[0].getFill() - ops * trait.amountReq);
                tanks[1].setFill(tanks[1].getFill() + ops * trait.amountProduced);
                this.powerBuffer += (long) (ops * trait.heatEnergy * eff);

                if(ops > 0) {
                    this.acceleration += 0.1F;
                } else {
                    this.acceleration -= 0.1F;
                }
            }

            this.acceleration = Mth.clamp(this.acceleration, 0F, 40F);
            this.rotor += this.acceleration;

            if(this.rotor >= 360D) {
                this.rotor -= 360D;
                // Ein Schlag je Umdrehung; die Tonhoehe steigt mit der Drehzahl (Original Z. 114).
                this.level.playSound(null, this.getBlockPos(), NtmSoundEvents.STEAM_ENGINE_OPERATE.get(),
                        SoundSource.BLOCKS, this.getVolume(1.0F), 0.5F + (this.acceleration / 80F));
            }

            buf.writeLong(this.powerBuffer);
            buf.writeFloat(this.rotor);
            tanks[1].serialize(buf);

            for(DirPos pos : this.getConPos()) {
                if(this.powerBuffer > 0) this.tryProvide(this.level, pos.makeCompat(), pos.getDir());
                this.trySubscribe(tanks[0].getTankType(), this.level, pos);
                this.tryProvide(tanks[1], this.level, pos);
            }

            this.networkPackNT(150);
        } else {
            this.lastRotor = this.rotor;

            if(this.turnProgress > 0) {
                double d = Mth.wrapDegrees(this.syncRotor - (double) this.rotor);
                this.rotor = (float) ((double) this.rotor + d / (double) this.turnProgress);
                --this.turnProgress;
            } else {
                this.rotor = this.syncRotor;
            }
        }
    }

    protected DirPos[] getConPos() {
        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise(Axis.Y);
        BlockPos pos = this.getBlockPos();

        return new DirPos[] {
                new DirPos(pos.getX() + rot.getStepX() * 2, pos.getY() + 1, pos.getZ() + rot.getStepZ() * 2, rot),
                new DirPos(pos.getX() + rot.getStepX() * 2 + dir.getStepX(), pos.getY() + 1, pos.getZ() + rot.getStepZ() * 2 + dir.getStepZ(), rot),
                new DirPos(pos.getX() + rot.getStepX() * 2 - dir.getStepX(), pos.getY() + 1, pos.getZ() + rot.getStepZ() * 2 - dir.getStepZ(), rot)
        };
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.powerBuffer = tag.getLong("powerBuffer");
        this.acceleration = tag.getFloat("acceleration");
        this.tanks[0].readFromNBT(tag, "s");
        this.tanks[1].readFromNBT(tag, "w");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putLong("powerBuffer", this.powerBuffer);
        tag.putFloat("acceleration", this.acceleration);
        this.tanks[0].writeToNBT(tag, "s");
        this.tanks[1].writeToNBT(tag, "w");
    }

    /** Ersetzt INFINITE_EXTENT_AABB aus 1.7.10 -- deckt den gesamten Multiblock ab */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos pos = this.worldPosition;
            this.renderBox = new AABB(pos.getX() - 6, pos.getY(), pos.getZ() - 6, pos.getX() + 7, pos.getY() + 4, pos.getZ() + 7);
        }
        return this.renderBox;
    }

    @Override
    public boolean canConnect(Direction dir) {
        return dir != Direction.UP && dir != Direction.DOWN && dir != null;
    }

    @Override public long getPower() { return this.powerBuffer; }
    @Override public long getMaxPower() { return this.powerBuffer; }
    @Override public void setPower(long power) { this.powerBuffer = power; }

    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] {tanks[1]}; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tanks[0]}; }
    @Override public FluidTank[] getAllTanks() { return tanks; }

    @Override public FluidTank getTankToPaste() { return null; }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBytes(this.buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.tanks[0].deserialize(buf);
        this.powerBuffer = buf.readLong();
        this.syncRotor = buf.readFloat();
        this.tanks[1].deserialize(buf);
        this.turnProgress = 3; //use 3-ply for extra smoothness
    }

    /** Konfiguration, siehe {@link com.hbm.config.MachineDynConfig}. */
    public static void readConfig(JsonObject obj) {
        steamCap = IConfigurableMachine.grab(obj, "I:steamCap", steamCap);
        ldsCap = IConfigurableMachine.grab(obj, "I:ldsCap", ldsCap);
        efficiency = IConfigurableMachine.grab(obj, "D:efficiency", efficiency);
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("I:steamCap").value(steamCap);
        writer.name("I:ldsCap").value(ldsCap);
        writer.name("D:efficiency").value(efficiency);
    }

}
