package com.hbm.blockentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.config.IConfigurableMachine;
import com.hbm.blocks.DummyableBlock;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.inventory.fluid.trait.FT_Coolable.CoolingType;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineIndustrialTurbine.
 *
 * Die Industrieturbine ist die mittlere der drei Dampfturbinen: groesser als die Einzelblock-
 * Turbine, kleiner als der Koloss. Wie beide wandelt sie Dampf ueber die Kuehl-Eigenschaft in
 * Strom und gibt den abgekuehlten Dampf wieder aus.
 *
 * SIE HAT EIN SCHWUNGRAD, und das ist ihr ganzer Unterschied. Die anderen beiden geben in jedem
 * Tick genau das ab, was der Dampf gerade hergibt. Diese sammelt die Energie erst in einem
 * gedachten Rad und zieht daraus ab -- anfangs wenig, dann immer mehr, bis das Rad auf Drehzahl
 * ist. Faellt der Dampf weg, laeuft sie noch aus, statt sofort zu stehen.
 *
 * DAS RAD BRAUCHT EIN ACHTEL EINER MILLIARDE HE, um voll zu laufen. Das klingt viel und ist es
 * auch: dichter Dampf gibt je Millibar ungleich mehr her als gewoehnlicher, und eine Turbine,
 * die mit Heissdampf gefahren wird, ist deshalb schneller auf Drehzahl als eine mit kaltem.
 *
 * SIE ZIEHT JE TICK EIN FUENFTEL ihres Eingangstanks. Der Koloss zieht alles, die kleine
 * Turbine ebenfalls -- diese haelt bewusst einen Vorrat zurueck.
 *
 * DER HEBEL SITZT VORN RECHTS. Er schaltet den Verdichter eine Dampfstufe weiter und macht die
 * Tanks dabei zehnmal kleiner, denn dichter Dampf braucht weniger Platz fuer dieselbe Energie.
 * Waehrend die Turbine laeuft, laesst er sich nicht umlegen.
 *
 * NICHT UEBERNOMMEN: die OpenComputers-Anbindung und die JSON-Konfiguration; beides gibt es im
 * Port nicht. Das Modell des Originals fehlt ebenfalls -- hier stehen Stahlkaesten.
 */
public class MachineIndustrialTurbineBlockEntity extends TurbineBaseBlockEntity {

    /* Ueber MachineDynConfig einstellbar, deshalb nicht endgueltig. */
    public static int inputTankSize = 750_000;
    public static int outputTankSize = 3_000_000;
    public static double efficiency = 1D;

    /** Wie viel Energie das Schwungrad fasst -- seine gedachte Masse. */
    public static final double FLYWHEEL_MAX_ENERGY = 0.5e8;

    public float rotor;
    public float lastRotor;

    /** Drehzahl des Schwungrads, null bis eins. */
    public double spin = 0;

    private long maxPower = 0;
    private long lastPowerTarget = 0;
    private long flywheelEnergy = 0;

    private AudioWrapper audio;
    private final float audioDesync;

    public MachineIndustrialTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_INDUSTRIAL_TURBINE.get(), pos, state);

        this.tanks = new FluidTank[2];
        this.tanks[0] = new FluidTank(Fluids.STEAM, inputTankSize);
        this.tanks[1] = new FluidTank(Fluids.SPENTSTEAM, outputTankSize);

        this.audioDesync = RandomSource.create().nextFloat() * 0.05F;
    }

    @Override public double consumptionPercent() { return 0.2D; }
    @Override public double getEfficiency() { return efficiency; }
    @Override public boolean doesResizeCompressor() { return true; }

    /**
     * Statt die Energie sofort abzugeben, geht sie ins Schwungrad. Nebenbei wird gemerkt, was
     * dieser Dampf ueberhaupt hergeben koennte -- daran misst sich die Drehzahl.
     */
    @Override
    public void generatePower(long power, int steamConsumed) {

        FT_Coolable trait = this.tanks[0].getTankType().getTrait(FT_Coolable.class);
        if(trait == null) return;

        double eff = trait.getEfficiency(CoolingType.TURBINE) * this.getEfficiency();
        int maxOps = (int) Math.ceil((this.tanks[0].getMaxFill() * this.consumptionPercent()) / trait.amountReq);

        this.maxPower = (long) (maxOps * trait.heatEnergy * eff);
        this.flywheelEnergy += power;
    }

    @Override
    public void onServerTick() {

        this.spin = this.flywheelEnergy / FLYWHEEL_MAX_ENERGY;

        /* Auch ein fast stehendes Rad gibt fuenf Prozent ab, sonst kaeme es nie in Gang. */
        this.lastPowerTarget = Math.min((long) (Math.max(this.spin, 0.05) * this.maxPower), this.flywheelEnergy);
        this.flywheelEnergy -= this.lastPowerTarget;
        this.powerBuffer = this.lastPowerTarget;
    }

    @Override
    public void onClientTick() {

        if(this.level == null) return;

        this.lastRotor = this.rotor;

        /* Bis zur halben Drehzahl steigt die Bilddrehung mit der Wurzel -- so sieht man auch
         * kleine Aenderungen noch. */
        float speed = this.spin >= 0.5 ? 30F : (float) (Math.pow(this.spin * 2, 0.5) * 30);
        this.rotor += speed;

        if(this.rotor >= 360) {
            this.lastRotor -= 360;
            this.rotor -= 360;
        }

        if(this.spin > 0) {

            float spinNum = (float) Math.min(1F, this.spin * 2);
            float volume = this.getVolume(0.25F + spinNum * 0.75F);
            float pitch = 0.5F + spinNum * 0.5F + this.audioDesync;

            if(this.audio == null) {
                this.audio = AudioWrapper.getLoopedSound(NtmSoundEvents.TURBINE_LARGE_LOOP.get(), SoundSource.BLOCKS, this, volume, 20F, pitch, 20);
                this.audio.startSound();
            }

            this.audio.keepAlive();
            this.audio.updatePitch(pitch);
            this.audio.updateVolume(volume);

        } else {
            this.stopAudio();
        }
    }

    private void stopAudio() {
        if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.stopAudio();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.stopAudio();
    }

    /** Der Strom geht hinten heraus, vier Bloecke hinter dem Kern. */
    @Override
    public DirPos[] getPowerPos() {
        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        BlockPos pos = this.getBlockPos();
        return new DirPos[] { new DirPos(pos.offset(-dir.getStepX() * 4, 1, -dir.getStepZ() * 4), dir.getOpposite()) };
    }

    /** Vier Anschluesse an den Laengsseiten, zwei oben -- die Zahlen sind die des Originals. */
    @Override
    public DirPos[] getConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise(Axis.Y);
        BlockPos pos = this.getBlockPos();

        return new DirPos[] {
                new DirPos(pos.offset(dir.getStepX() * 3 + rot.getStepX() * 2, 0, dir.getStepZ() * 3 + rot.getStepZ() * 2), rot),
                new DirPos(pos.offset(dir.getStepX() * 3 - rot.getStepX() * 2, 0, dir.getStepZ() * 3 - rot.getStepZ() * 2), rot.getOpposite()),
                new DirPos(pos.offset(-dir.getStepX() + rot.getStepX() * 2, 0, -dir.getStepZ() + rot.getStepZ() * 2), rot),
                new DirPos(pos.offset(-dir.getStepX() - rot.getStepX() * 2, 0, -dir.getStepZ() - rot.getStepZ() * 2), rot.getOpposite()),
                new DirPos(pos.offset(dir.getStepX() * 3, 3, dir.getStepZ() * 3), Direction.UP),
                new DirPos(pos.offset(-dir.getStepX(), 3, -dir.getStepZ()), Direction.UP)
        };
    }

    /** Strom nimmt sie nur von hinten an; Dampf nur von den Laengsseiten. */
    @Override
    public boolean canConnect(Direction dir) {
        return dir == this.getBlockState().getValue(DummyableBlock.FACING).getOpposite();
    }

    @Override
    public boolean canConnect(FluidType type, Direction dir) {

        if(!type.hasTrait(FT_Coolable.class) && type != Fluids.SPENTSTEAM) return false;

        Direction facing = this.getBlockState().getValue(DummyableBlock.FACING);
        return dir != facing && dir != facing.getOpposite();
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeDouble(this.spin);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.spin = buf.readDouble();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.lastPowerTarget = tag.getLong("lastPowerTarget");
        this.flywheelEnergy = tag.getLong("flywheelEnergy");
        this.maxPower = tag.getLong("maxPower");
        this.spin = tag.getDouble("spin");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("lastPowerTarget", this.lastPowerTarget);
        tag.putLong("flywheelEnergy", this.flywheelEnergy);
        tag.putLong("maxPower", this.maxPower);
        tag.putDouble("spin", this.spin);
    }

    /** Konfiguration, siehe {@link com.hbm.config.MachineDynConfig}. */
    public static void readConfig(JsonObject obj) {
        inputTankSize = IConfigurableMachine.grab(obj, "I:inputTankSize", inputTankSize);
        outputTankSize = IConfigurableMachine.grab(obj, "I:outputTankSize", outputTankSize);
        efficiency = IConfigurableMachine.grab(obj, "D:efficiency", efficiency);
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("INFO").value("industrial steam turbine consumes 20% of available steam per tick");
        writer.name("I:inputTankSize").value(inputTankSize);
        writer.name("I:outputTankSize").value(outputTankSize);
        writer.name("D:efficiency").value(efficiency);
    }
}
