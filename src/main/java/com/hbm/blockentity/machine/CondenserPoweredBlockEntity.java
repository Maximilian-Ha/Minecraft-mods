package com.hbm.blockentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.config.IConfigurableMachine;
import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import java.io.IOException;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityCondenserPowered.
 *
 * Der grosse Kondensator arbeitet wie der kleine, braucht dafuer aber Strom und hat
 * eigene Anschlusspunkte an den sechs Aussenbloecken. Alle Zahlenwerte 1:1 uebernommen.
 */
public class CondenserPoweredBlockEntity extends CondenserBaseBlockEntity implements IEnergyReceiverMK2 {

    public long power;
    public float spin;
    public float lastSpin;

    // todo config: im Original ueber IConfigurableMachine (condenserPowered.json) einstellbar
    public static long maxPower = 10_000_000;
    public static int inputTankSizeP = 1_000_000;
    public static int outputTankSizeP = 1_000_000;
    public static int powerConsumption = 10;

    private AABB renderBox;

    public CondenserPoweredBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.CONDENSER_POWERED.get(), pos, state);

        this.tanks = new FluidTank[2];
        this.tanks[0] = new FluidTank(Fluids.SPENTSTEAM, inputTankSizeP);
        this.tanks[1] = new FluidTank(Fluids.WATER, outputTankSizeP);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if(this.level == null) return;

        if(this.level.isClientSide) {

            this.lastSpin = this.spin;

            if(this.waterTimer > 0) {
                this.spin += 30F;

                if(this.spin >= 360F) {
                    this.spin -= 360F;
                    this.lastSpin -= 360F;
                }

                if(this.level.getGameTime() % 4 == 0) {
                    Direction dir = this.getFacing();
                    double x = this.getBlockPos().getX();
                    double y = this.getBlockPos().getY();
                    double z = this.getBlockPos().getZ();
                    this.level.addParticle(ParticleTypes.CLOUD, x + 0.5 + dir.getStepX() * 1.5, y + 1.5, z + 0.5 + dir.getStepZ() * 1.5, dir.getStepX() * 0.1, 0, dir.getStepZ() * 0.1);
                    this.level.addParticle(ParticleTypes.CLOUD, x + 0.5 - dir.getStepX() * 1.5, y + 1.5, z + 0.5 - dir.getStepZ() * 1.5, dir.getStepX() * -0.1, 0, dir.getStepZ() * -0.1);
                }
            }
        }
    }

    private Direction getFacing() {
        BlockState state = this.getBlockState();
        if(!state.hasProperty(DummyableBlock.FACING)) return Direction.NORTH;
        return state.getValue(DummyableBlock.FACING);
    }

    @Override
    public void packExtra(CompoundTag tag) {
        tag.putLong("power", this.power);
    }

    @Override
    public boolean extraCondition(int convert) {
        return this.power >= (convert * powerConsumption) * 0.95; // bit of tolerance
    }

    @Override
    public void postConvert(int convert) {
        this.power -= (long) convert * powerConsumption;
        if(this.power < 0) this.power = 0;
    }

    /**
     * Die doppelte Uebertragung von Tanks und Zeitgeber stammt 1:1 aus dem Original:
     * die Basis schreibt sie bereits, der angetriebene Kondensator haengt sie noch einmal
     * an. Serialisierung und Deserialisierung sind symmetrisch, also bleibt es dabei.
     */
    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
        buf.writeByte(this.waterTimer);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
        this.waterTimer = buf.readByte();
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

    @Override
    protected void subscribeToAllAround() {
        for(DirPos pos : this.getConPos()) {
            this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
            this.trySubscribe(this.level, pos);
        }
    }

    @Override
    protected void sendFluidToAll() {
        for(DirPos pos : this.getConPos()) {
            this.tryProvide(this.tanks[1].getTankType(), this.level, pos);
        }
    }

    public DirPos[] getConPos() {

        Direction dir = this.getFacing();
        Direction rot = dir.getCounterClockWise();

        int x = this.getBlockPos().getX();
        int y = this.getBlockPos().getY();
        int z = this.getBlockPos().getZ();

        return new DirPos[] {
                new DirPos(x + rot.getStepX() * 4, y + 1, z + rot.getStepZ() * 4, rot),
                new DirPos(x - rot.getStepX() * 4, y + 1, z - rot.getStepZ() * 4, rot.getOpposite()),
                new DirPos(x + dir.getStepX() * 2 - rot.getStepX(), y + 1, z + dir.getStepZ() * 2 - rot.getStepZ(), dir),
                new DirPos(x + dir.getStepX() * 2 + rot.getStepX(), y + 1, z + dir.getStepZ() * 2 + rot.getStepZ(), dir),
                new DirPos(x - dir.getStepX() * 2 - rot.getStepX(), y + 1, z - dir.getStepZ() * 2 - rot.getStepZ(), dir.getOpposite()),
                new DirPos(x - dir.getStepX() * 2 + rot.getStepX(), y + 1, z - dir.getStepZ() * 2 + rot.getStepZ(), dir.getOpposite())
        };
    }

    /** Ohne @Override, wie alle Renderkaesten im Port -- der Renderer holt die Box hierueber ab. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.getBlockPos().getX();
            int y = this.getBlockPos().getY();
            int z = this.getBlockPos().getZ();
            this.renderBox = new AABB(x - 3, y, z - 3, x + 4, y + 3, z + 4);
        }
        return this.renderBox;
    }

    @Override
    public long getPower() {
        return this.power;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    /** Konfiguration, siehe {@link com.hbm.config.MachineDynConfig}. */
    public static void readConfig(JsonObject obj) {
        maxPower = IConfigurableMachine.grab(obj, "L:maxPower", maxPower);
        inputTankSizeP = IConfigurableMachine.grab(obj, "I:inputTankSize", inputTankSizeP);
        outputTankSizeP = IConfigurableMachine.grab(obj, "I:outputTankSize", outputTankSizeP);
        powerConsumption = IConfigurableMachine.grab(obj, "I:powerConsumption", powerConsumption);
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("L:maxPower").value(maxPower);
        writer.name("I:inputTankSize").value(inputTankSizeP);
        writer.name("I:outputTankSize").value(outputTankSizeP);
        writer.name("I:powerConsumption").value(powerConsumption);
    }
}
