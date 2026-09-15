package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.inventory.menus.MachineTurbineGasMenu;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.BobMathUtil;
import com.hbm.util.SoundUtils;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineTurbineGas.
 *
 * Gasturbine mit kombiniertem Kreislauf: verbrennt Brenngas (FuelGrade.GAS) zu
 * Strom und nutzt die Abwaerme im Waermetauscher, um Wasser zu Heissdampf zu
 * verdampfen. Braucht zusaetzlich Schmiermittel. Die Maschine kennt drei
 * Zustaende -- aus (0), Anlauf (-1) und Betrieb (1) -- und kann per Regler
 * (0..60) oder per Automatik gefahren werden.
 *
 * Nicht uebernommen: OpenComputers (CompatHandler/SimpleComponent),
 * IInfoProviderEC (CompatEnergyControl) und RedstoneOverRadio
 * (IRORValueProvider/IRORInteractive) -- diese Schnittstellen gibt es im Port
 * nicht.
 */
public class MachineTurbineGasBlockEntity extends MachineBaseBlockEntity
        implements IEnergyProviderMK2, IFluidStandardTransceiverMK2, IControlReceiver, IFluidCopiable {

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_IDENTIFIER = 1;

    public long power;
    public static final long maxPower = 1_000_000L;

    /** 0-100, Sichelanzeige, bestimmt die Stromerzeugung, faengt oberhalb von 10% an */
    public int rpm;
    /** 0-800, bestimmt die verdampfte Wassermenge, ab 300 Grad C wird gekocht */
    public int temp;
    public int rpmIdle = 10;
    public int tempIdle = 300;

    /** Reglerstellung 0..60, 0 ist Leerlauf, 60 ist Volllast */
    public int powerSliderPos;
    /** Dasselbe, aber als Prozentwert 0..100 */
    public int throttle;

    public boolean autoMode;
    /** 0 ist aus, -1 ist Anlauf, 1 ist Betrieb */
    public int state = 0;

    /** Zaehler fuer Anlauf und Abschaltung */
    public int counter = 0;
    public int instantPowerOutput;
    public double waterToBoil;

    public FluidTank[] tanks;

    private AudioWrapper audio;

    private long powerBeforeNet;

    public MachineTurbineGasBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_TURBINEGAS.get(), pos, state, 2);

        this.tanks = new FluidTank[4];
        this.tanks[0] = new FluidTank(Fluids.GAS, 100_000);
        this.tanks[1] = new FluidTank(Fluids.LUBRICANT, 16_000);
        this.tanks[2] = new FluidTank(Fluids.WATER, 16_000);
        this.tanks[3] = new FluidTank(Fluids.HOTSTEAM, 160_000);
    }

    /**
     * Treibstoffverbrauch pro Tick bei Volllast. Im Original eine statische
     * HashMap; hier eine Methode, weil die Fluids erst zur Laufzeit angelegt
     * werden und ein statischer Block sonst auf noch leere Felder zugreifen
     * koennte. Die Werte sind unveraendert.
     */
    public static double getMaxConsumption(FluidType type) {
        if(type == Fluids.GAS) return 50D;          // Erdgas brennt schlecht, dafuer schneller
        if(type == Fluids.SYNGAS) return 10D;       // Syngas ist stark
        if(type == Fluids.OXYHYDROGEN) return 100D; // Knallgas ist miserabel, braucht Unmengen
        if(type == Fluids.REFORMGAS) return 5D;
        return 5D;                                  // Standardwert, wenn nicht gelistet
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.turbinegas");
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide) {

            this.waterToBoil = 0; // zuruecksetzen
            this.throttle = this.powerSliderPos * 100 / 60;

            ItemStack id = this.slots.get(SLOT_IDENTIFIER);
            if(!id.isEmpty() && id.getItem() instanceof IItemFluidIdentifier identifier) {
                FluidType fluid = identifier.getType(this.level, this.worldPosition, id);
                if(fluid != null && fluid.hasTrait(FT_Combustible.class) && fluid.getTrait(FT_Combustible.class).getGrade() == FuelGrade.GAS) {
                    this.tanks[0].setTankType(fluid);
                }
            }

            if(this.autoMode) { // Leistung nach Netzbedarf und Tankfuellstand

                int powerSliderTarget;

                // bei wenig Treibstoff wird der Verbrauch linear zurueckgefahren
                if(this.tanks[0].getFill() * 10 > this.tanks[0].getMaxFill()) {
                    powerSliderTarget = 60 - (int) (60 * this.power / maxPower); // Regler proportional zur Stromanzeige
                } else {
                    powerSliderTarget = (int) (this.tanks[0].getFill() * 0.0001 * (60 - (int) (60 * this.power / maxPower)));
                }

                if(powerSliderTarget > this.powerSliderPos) { // der Automatikregler gleitet, statt zu springen
                    this.powerSliderPos++;
                } else if(powerSliderTarget < this.powerSliderPos) {
                    this.powerSliderPos--;
                }
            }

            switch(this.state) { // was bei aus, Anlauf und Betrieb zu tun ist
                case 0 -> this.shutdown();
                case -1 -> { this.stopIfNotReady(); this.startup(); }
                case 1 -> { this.stopIfNotReady(); this.run(); }
                default -> { }
            }

            Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
            Direction rot = dir.getClockWise(Axis.Y);
            BlockPos pos = this.getBlockPos();

            this.powerBeforeNet = Math.min(this.power, maxPower);

            // erst Batterie und Netz bedienen...
            this.power = Library.chargeItemsFromTE(this.slots, SLOT_BATTERY, this.power, maxPower);
            this.tryProvide(this.level, pos.offset(rot.getStepX() * 5, 1, rot.getStepZ() * 5), rot); // Stromausgang

            // ...und erst danach begrenzen. Verhindert kuenftige Faelle, in denen die Leistung
            // beschnitten wuerde, weil der Treibstoff zu stark und der Puffer zu klein ist.
            if(this.power > maxPower) this.power = maxPower;

            for(int i = 0; i < 2; i++) { // Treibstoff und Schmiermittel
                this.trySubscribe(this.tanks[i].getTankType(), this.level,
                        pos.offset(-dir.getStepX() * 2 + rot.getStepX(), 0, -dir.getStepZ() * 2 + rot.getStepZ()), dir.getOpposite());
                this.trySubscribe(this.tanks[i].getTankType(), this.level,
                        pos.offset(dir.getStepX() * 2 + rot.getStepX(), 0, dir.getStepZ() * 2 + rot.getStepZ()), dir);
            }
            // Wasser
            this.trySubscribe(this.tanks[2].getTankType(), this.level,
                    pos.offset(-dir.getStepX() * 2 + rot.getStepX() * -4, 0, -dir.getStepZ() * 2 + rot.getStepZ() * -4), dir.getOpposite());
            this.trySubscribe(this.tanks[2].getTankType(), this.level,
                    pos.offset(dir.getStepX() * 2 + rot.getStepX() * -4, 0, dir.getStepZ() * 2 + rot.getStepZ() * -4), dir);
            // Dampf
            this.tryProvide(this.tanks[3], this.level,
                    new DirPos(pos.offset(-rot.getStepX() * 6, 1, -rot.getStepZ() * 6), rot.getOpposite()));

            this.networkPackNT(150);

        } else { // Clientseite, Sound und Gedoens

            if(this.rpm >= 10 && this.state != -1) { // wenn die Bedingungen stimmen, laeuft der Sound

                if(this.audio == null) { // laeuft noch keiner, starten

                    this.audio = AudioWrapper.getLoopedSound(NtmSoundEvents.GAS_TURBINE_RUNNING.get(), SoundSource.BLOCKS, this, this.getVolume(1.0F), 20F, 2.0F, 20);
                    this.audio.startSound();

                } else if(!this.audio.isPlaying()) {
                    this.audio.stopSound();
                    this.audio = AudioWrapper.getLoopedSound(NtmSoundEvents.GAS_TURBINE_RUNNING.get(), SoundSource.BLOCKS, this, this.getVolume(1.0F), 20F, 2.0F, 20);
                    this.audio.startSound();
                }

                this.audio.updatePitch((float) (0.55 + 0.1 * this.rpm / 10)); // Tonhoehe folgt der Drehzahl
                this.audio.updateVolume(this.getVolume(2F));
                this.audio.keepAlive();

            } else {

                if(this.audio != null) {
                    this.audio.stopSound();
                    this.audio = null;
                }
            }
        }
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.powerBeforeNet);
        buf.writeInt(this.rpm);
        buf.writeInt(this.temp);
        buf.writeInt(this.state);
        buf.writeBoolean(this.autoMode);
        buf.writeInt(this.throttle);
        buf.writeInt(this.powerSliderPos);

        if(this.state != 1) {
            buf.writeInt(this.counter); // waehrend Anlauf und Abschaltung
        } else {
            buf.writeInt(this.instantPowerOutput); // waehrend des Betriebs
        }

        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
        this.tanks[2].serialize(buf);
        this.tanks[3].serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.rpm = buf.readInt();
        this.temp = buf.readInt();
        this.state = buf.readInt();
        this.autoMode = buf.readBoolean();
        this.throttle = buf.readInt();
        this.powerSliderPos = buf.readInt();

        if(this.state != 1) {
            this.counter = buf.readInt();
        } else {
            this.instantPowerOutput = buf.readInt(); // Zustand 1
        }

        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
        this.tanks[2].deserialize(buf);
        this.tanks[3].deserialize(buf);
    }

    private void stopIfNotReady() {

        if(this.tanks[0].getFill() == 0 || this.tanks[1].getFill() == 0) {
            this.state = 0;
        }
        if(!this.hasAcceptableFuel()) {
            this.state = 0;
        }
    }

    public boolean hasAcceptableFuel() {

        if(this.tanks[0].getTankType().hasTrait(FT_Combustible.class)) {
            return this.tanks[0].getTankType().getTrait(FT_Combustible.class).getGrade() == FuelGrade.GAS;
        }

        return false;
    }

    private void startup() {
        if(this.level == null) return;

        this.counter++;

        if(this.counter <= 20) { // Drehzahlanzeige 0-100-0
            this.rpm = 5 * this.counter;
        } else if(this.counter > 20 && this.counter <= 40) {
            this.rpm = 100 - 5 * (this.counter - 20);
        } else if(this.counter > 50) {
            this.rpm = rpmIdle * (this.counter - 50) / 530; // Drehzahl und Temperatur steigen langsam an
            this.temp = tempIdle * (this.counter - 50) / 530;
        }

        if(this.counter == 50) {
            SoundUtils.playAtVec3(this.level, new Vec3(this.worldPosition.getX(), this.worldPosition.getY() + 2, this.worldPosition.getZ()),
                    NtmSoundEvents.GAS_TURBINE_STARTUP.get(), SoundSource.BLOCKS, this.getVolume(1.0F), 1.0F);
        }

        if(this.counter == 580) {
            this.counter = 225; // sorgt dafuer, dass direkt nach dem Anlauf sauber abgeschaltet werden kann
            this.state = 1;
        }
    }

    /** Damit Drehzahl und Temperatur beim Abschalten auslaufen, statt sofort auf 0 zu springen */
    int rpmLast;
    int tempLast;

    private void shutdown() {
        if(this.level == null) return;

        this.autoMode = false;
        this.instantPowerOutput = 0;

        if(this.powerSliderPos > 0) this.powerSliderPos--;

        if(this.rpm <= 10 && this.counter > 0) {

            if(this.counter == 225) {

                SoundUtils.playAtVec3(this.level, new Vec3(this.worldPosition.getX(), this.worldPosition.getY() + 2, this.worldPosition.getZ()),
                        NtmSoundEvents.GAS_TURBINE_SHUTDOWN.get(), SoundSource.BLOCKS, this.getVolume(1.0F), 1.0F);

                this.rpmLast = this.rpm;
                this.tempLast = this.temp;
            }

            this.counter--;

            this.rpm = this.rpmLast * this.counter / 225;
            this.temp = this.tempLast * this.counter / 225;

        } else if(this.rpm > 11) { // die Turbine faellt erst zuegig auf Leerlauf zurueck
            this.counter = 42069; // zwingend noetig, damit beim Abschalten nichts durcheinandergeraet
            this.rpm--;
        } else if(this.rpm == 11) {
            this.counter = 225;
            this.rpm--;
        }
    }

    /** Berechnet aus der Verbrennungsenergie eine (hoffentlich) sinnvolle Brenntemperatur, 300 - 800 Grad C */
    protected int getFluidBurnTemp(FluidType type) {
        double dFuel = type.hasTrait(FT_Combustible.class) ? type.getTrait(FT_Combustible.class).getCombustionEnergy() : 0;
        return (int) Math.floor(800D - (Math.pow(Math.E, -dFuel / 100_000D)) * 300D);
    }

    private void run() {
        if(this.level == null) return;

        if((int) (this.throttle * 0.9) > this.rpm - rpmIdle) { // bildet die Traegheit des Laeufers nach
            if(this.level.getGameTime() % 5 == 0) {
                this.rpm++;
            }
        } else if((int) (this.throttle * 0.9) < this.rpm - rpmIdle) {
            if(this.level.getGameTime() % 2 == 0) {
                this.rpm--;
            }
        }

        int maxTemp = this.getFluidBurnTemp(this.tanks[0].getTankType());

        if(this.throttle * 5 * (maxTemp - tempIdle) / 500 > this.temp - tempIdle) { // bildet die Traegheit des Waermetauschers nach
            if(this.level.getGameTime() % 2 == 0) {
                this.temp++;
            }
        } else if(this.throttle * 5 * (maxTemp - tempIdle) / 500 < this.temp - tempIdle) {
            if(this.level.getGameTime() % 2 == 0) {
                this.temp--;
            }
        }

        double consumption = getMaxConsumption(this.tanks[0].getTankType());
        if(this.level.getGameTime() % 20 == 0 && this.tanks[0].getTankType() != Fluids.OXYHYDROGEN) {
            PollutionHandler.incrementPollution(this.level, this.worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 3);
        }
        this.makePower(consumption, this.throttle);
    }

    /** Sammelt Bruchteile, damit auch bei weniger als 1 mb/Tick verbraucht wird */
    double fuelToConsume;

    private void makePower(double consMax, int throttle) {
        if(this.level == null) return;

        double idleConsumption = consMax * 0.05D;
        double consumption = idleConsumption + consMax * throttle / 100;

        this.fuelToConsume += consumption;

        this.tanks[0].setFill(this.tanks[0].getFill() - (int) Math.floor(this.fuelToConsume));
        this.fuelToConsume -= (int) Math.floor(this.fuelToConsume);

        if(this.level.getGameTime() % 10 == 0) { // Schmiermittelverbrauch
            this.tanks[1].setFill(this.tanks[1].getFill() - 1);
        }

        if(this.tanks[0].getFill() < 0) { // keine negativen Fuellstaende
            this.tanks[0].setFill(0);
            this.state = 0;
        }
        if(this.tanks[1].getFill() < 0) {
            this.tanks[1].setFill(0);
            this.state = 0;
        }

        long energy = 0; // Energie pro mb Treibstoff

        if(this.tanks[0].getTankType().hasTrait(FT_Combustible.class)) {
            energy = this.tanks[0].getTankType().getTrait(FT_Combustible.class).getCombustionEnergy() / 1000L;
        }

        int rpmEff = this.rpm - rpmIdle; // Drehzahl oberhalb des Leerlaufs, 0-90

        // consMax*energy entspricht der Leistung bei 100%
        if(this.instantPowerOutput < (consMax * energy * rpmEff / 90)) { // laesst die Leistung gleitend statt in 2000-HE-Spruengen steigen
            this.instantPowerOutput += Math.random() * 0.005 * consMax * energy;
            if(this.instantPowerOutput > (consMax * energy * rpmEff / 90)) {
                this.instantPowerOutput = (int) (consMax * energy * rpmEff / 90);
            }
        } else if(this.instantPowerOutput > (consMax * energy * rpmEff / 90)) {
            this.instantPowerOutput -= Math.random() * 0.011 * consMax * energy;
            if(this.instantPowerOutput < (consMax * energy * rpmEff / 90)) {
                this.instantPowerOutput = (int) (consMax * energy * rpmEff / 90);
            }
        }
        this.power += this.instantPowerOutput;

        double waterPerTick = (consMax * energy * (this.temp - tempIdle) / 220000);

        this.waterToBoil = waterPerTick; // im Feld gepuffert

        int heatCycles = (int) Math.floor(this.waterToBoil);
        int waterCycles = this.tanks[2].getFill();
        int steamCycles = (this.tanks[3].getMaxFill() - this.tanks[3].getFill()) / 10;
        int cycles = BobMathUtil.min(heatCycles, waterCycles, steamCycles);

        this.tanks[2].setFill(this.tanks[2].getFill() - cycles);
        this.tanks[3].setFill(this.tanks[3].getFill() + cycles * 10);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.tanks[0].readFromNBT(tag, "gas");
        this.tanks[1].readFromNBT(tag, "lube");
        this.tanks[2].readFromNBT(tag, "water");
        this.tanks[3].readFromNBT(tag, "densesteam");
        this.autoMode = tag.getBoolean("automode");
        this.power = tag.getLong("power");
        this.state = tag.getInt("state");
        this.rpm = tag.getInt("rpm");
        this.temp = tag.getInt("temperature");
        this.powerSliderPos = tag.getInt("slidPos");
        this.instantPowerOutput = tag.getInt("instPwr");
        this.counter = tag.getInt("counter");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        this.tanks[0].writeToNBT(tag, "gas");
        this.tanks[1].writeToNBT(tag, "lube");
        this.tanks[2].writeToNBT(tag, "water");
        this.tanks[3].writeToNBT(tag, "densesteam");
        tag.putBoolean("automode", this.autoMode);
        tag.putLong("power", this.power);
        if(this.state == 1) {
            tag.putInt("state", this.state);
            tag.putInt("rpm", this.rpm);
            tag.putInt("temperature", this.temp);
            tag.putInt("slidPos", this.powerSliderPos);
            tag.putInt("instPwr", this.instantPowerOutput);
            tag.putInt("counter", 225);
        } else {
            tag.putInt("state", 0);
            tag.putInt("rpm", 0);
            tag.putInt("temperature", 20);
            tag.putInt("slidPos", 0);
            tag.putInt("instpwr", 0); // Schreibfehler aus dem Original, bewusst uebernommen
            tag.putInt("counter", 0);
        }
    }

    @Override
    public void receiveControl(CompoundTag data) {

        if(data.contains("slidPos")) this.powerSliderPos = data.getInt("slidPos");

        if(data.contains("autoMode")) this.autoMode = data.getBoolean("autoMode");

        if(data.contains("state")) this.state = data.getInt("state");

        this.setChanged();
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()) < 625;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();

        if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    @Override public void setPower(long power) { this.power = power; }
    @Override public long getPower() { return this.power; }
    @Override public long getMaxPower() { return maxPower; }

    private AABB renderBox = null;

    public AABB getRenderBoundingBox() {

        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();

            this.renderBox = new AABB(x - 5, y, z - 5, x + 6, y + 3, z + 6);
        }

        return this.renderBox;
    }

    @Override public FluidTank[] getAllTanks() { return this.tanks; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0], this.tanks[1], this.tanks[2] }; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[3] }; }

    @Override
    public boolean canConnect(Direction dir) {
        return dir != null && dir != Direction.DOWN;
    }

    @Override
    public boolean canConnect(FluidType type, Direction dir) {
        return dir != null && dir != Direction.DOWN;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot == SLOT_IDENTIFIER) return stack.getItem() instanceof IItemFluidIdentifier;
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { SLOT_BATTERY };
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if(index == SLOT_BATTERY && stack.getItem() instanceof IBatteryItem battery) {
            return battery.getCharge(stack) == battery.getMaxCharge(stack);
        }
        return false;
    }

    @Override
    public FluidTank getTankToPaste() {
        return this.tanks[0];
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineTurbineGasMenu(id, inventory, this);
    }
}
