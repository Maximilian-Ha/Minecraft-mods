package com.hbm.blockentity.machine.fusion;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.config.IConfigurableMachine;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.networkproviders.PlasmaNetworkProvider;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.io.IOException;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.fusion.TileEntityFusionMHDT.
 *
 * Die magnetohydrodynamische Turbine. Sie macht aus Plasmawaerme unmittelbar Strom -- ohne Dampf,
 * ohne Kessel, mit 135 Prozent Ausbeute. Dafuer braucht sie durchgehend Kuehlmittel, und unter
 * fuenf Millionen Waerme je Tick arbeitet sie nur mit halber Ausbeute: die Anlage lohnt sich erst,
 * wenn der Reaktor wirklich laeuft.
 *
 * ABWEICHUNG: NICHT UEBERNOMMEN ist die OpenComputers-Anbindung (ENTSCHEIDUNGEN.md).
 */
public class FusionMHDTBlockEntity extends LoadedBaseBlockEntity implements ITickable, IEnergyProviderMK2, IFluidStandardTransceiverMK2, IFusionPowerReceiver {

    protected GenNode<?> plasmaNode;

    public long plasmaEnergy;
    public long plasmaEnergySync;
    public long power;

    /** Nur Client: der Winkel des Laeufers. */
    public float rotor;
    public float prevRotor;
    public float rotorSpeed;
    public static final float ROTOR_ACCELERATION = 0.125F;

    public static final double PLASMA_EFFICIENCY = 1.35D;
    public static final int COOLANT_USE = 50;
    /** Ab hier laeuft sie mit voller Ausbeute; darunter mit halber. */
    public static long MINIMUM_PLASMA = 5_000_000L;

    public final FluidTank[] tanks = new FluidTank[2];
    private AudioWrapper audio;
    private AABB renderBox;

    public static void readConfig(JsonObject obj) {
        MINIMUM_PLASMA = IConfigurableMachine.grab(obj, "L:minimumPlasma", MINIMUM_PLASMA);
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("L:minimumPlasma").value(MINIMUM_PLASMA);
    }

    public FusionMHDTBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FUSION_MHDT.get(), pos, state);

        this.tanks[0] = new FluidTank(Fluids.PERFLUOROMETHYL_COLD, 4_000);
        this.tanks[1] = new FluidTank(Fluids.PERFLUOROMETHYL, 4_000);
    }

    public boolean hasMinimumPlasma() {
        return this.plasmaEnergy >= MINIMUM_PLASMA;
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide) {
            this.serverUpdate();
        } else {
            this.clientUpdate();
        }
    }

    private void serverUpdate() {

        this.plasmaEnergySync = this.plasmaEnergy;

        if(this.isCool()) {
            this.power = (long) Math.floor(this.plasmaEnergy * PLASMA_EFFICIENCY);
            if(!this.hasMinimumPlasma()) this.power /= 2;
            this.tanks[0].setFill(this.tanks[0].getFill() - COOLANT_USE);
            this.tanks[1].setFill(this.tanks[1].getFill() + COOLANT_USE);
        }

        for(DirPos pos : this.getConPos()) {
            this.tryProvide(this.level, pos.makeCompat(), pos.getDir());
            if(this.tanks[0].getTankType() != Fluids.NONE) this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
            if(this.tanks[1].getFill() > 0) this.tryProvide(this.tanks[1], this.level, pos);
        }

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING).getOpposite();
        BlockPos nodePos = this.worldPosition.offset(dir.getStepX() * 6, 2, dir.getStepZ() * 6);

        this.plasmaNode = FusionNodes.ensure(this.plasmaNode, this.level, nodePos,
                new DirPos(this.worldPosition.offset(dir.getStepX() * 7, 2, dir.getStepZ() * 7), dir),
                PlasmaNetworkProvider.THE_PROVIDER);

        FusionNodes.subscribe(this.plasmaNode, this);

        this.networkPackNT(150);
        this.plasmaEnergy = 0;
    }

    private void clientUpdate() {

        if(this.plasmaEnergy > 0 && this.isCool()) this.rotorSpeed += ROTOR_ACCELERATION;
        else this.rotorSpeed -= ROTOR_ACCELERATION;

        this.rotorSpeed = Mth.clamp(this.rotorSpeed, 0F, this.hasMinimumPlasma() ? 15F : 10F);

        this.prevRotor = this.rotor;
        this.rotor += this.rotorSpeed;

        if(this.rotor >= 360F) {
            this.rotor -= 360F;
            this.prevRotor -= 360F;
        }

        Player me = Minecraft.getInstance().player;
        boolean near = me != null && me.distanceToSqr(
                this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 2.5, this.worldPosition.getZ() + 0.5) < 30 * 30;

        if(this.rotorSpeed > 0 && near) {

            float speed = this.rotorSpeed / 15F;

            if(this.audio == null) {
                this.audio = AudioWrapper.getLoopedSound(NtmSoundEvents.TURBINE_LARGE_LOOP.get(), SoundSource.BLOCKS,
                        this.worldPosition.getX() + 0.5F, this.worldPosition.getY() + 1.5F, this.worldPosition.getZ() + 0.5F,
                        this.getVolume(speed), 20F, speed, 20);
                this.audio.startSound();
            } else {
                this.audio.updateVolume(this.getVolume(speed));
                this.audio.updatePitch(speed);
                this.audio.keepAlive();
            }

        } else if(this.audio != null) {
            if(this.audio.isPlaying()) this.audio.stopSound();
            this.audio = null;
        }
    }

    public boolean isCool() {
        return this.tanks[0].getFill() >= COOLANT_USE
                && this.tanks[1].getFill() + COOLANT_USE <= this.tanks[1].getMaxFill();
    }

    public DirPos[] getConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise();

        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x + dir.getStepX() * 4 + rot.getStepX() * 4, y, z + dir.getStepZ() * 4 + rot.getStepZ() * 4, rot),
                new DirPos(x + dir.getStepX() * 4 - rot.getStepX() * 4, y, z + dir.getStepZ() * 4 - rot.getStepZ() * 4, rot.getOpposite()),
                new DirPos(x + dir.getStepX() * 8, y + 1, z + dir.getStepZ() * 8, dir)
        };
    }

    @Override public boolean receivesFusionPower() { return true; }
    @Override public void receiveFusionPower(long fusionPower, double neutronPower, float r, float g, float b) { this.plasmaEnergy = fusionPower; }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.plasmaEnergySync);
        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.plasmaEnergy = buf.readLong();
        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.tanks[0].readFromNBT(tag, "t0");
        this.tanks[1].readFromNBT(tag, "t1");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        this.tanks[0].writeToNBT(tag, "t0");
        this.tanks[1].writeToNBT(tag, "t1");
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        FusionNodes.destroy(this.level, this.plasmaNode);
        if(this.audio != null) { this.audio.stopSound(); this.audio = null; }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if(this.audio != null) { this.audio.stopSound(); this.audio = null; }
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    /* Wie im Original: der Generator hat keinen Puffer, was er erzeugt, geht sofort weiter. */
    @Override public long getMaxPower() { return this.power; }

    @Override public FluidTank[] getAllTanks() { return this.tanks; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[1] }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0] }; }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 9, y - 3, z - 9, x + 10, y + 6, z + 10);
        }
        return this.renderBox;
    }
}
