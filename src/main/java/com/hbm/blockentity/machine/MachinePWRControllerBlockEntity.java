package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingType;
import com.hbm.inventory.fluid.trait.FT_PWRModerator;
import com.hbm.inventory.menus.MachinePWRMenu;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.PWRFuelItem.EnumPWRFuel;
import com.hbm.inventory.MetaHelper;
import com.hbm.util.EnumUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityPWRController.
 *
 * Der Druckwasserreaktor. Anders als der RBMK ist er kein Raster fester Saeulen, sondern ein
 * frei geformter Kern: man mauert ihn aus Brennstab-, Steuer-, Kanal-, Waermetauscher-,
 * Kuehlkoerper- und Quellbloecken zusammen, umhuellt ihn mit Gehaeuse und Reflektoren und laesst
 * die Steuerung ihn einlesen.
 *
 * WIE ER RECHNET
 *
 * Beim Einlesen zaehlt die Steuerung nicht die Bloecke, sondern die VERBINDUNGEN zwischen je
 * zwei Brennstaeben -- in allen sechs Richtungen, bis zu sechzehn Bloecke weit. Ein Reflektor am
 * Ende einer Sichtlinie zaehlt doppelt, weil er die Neutronen zurueckwirft. Steht ein Steuerstab
 * dazwischen, geht die Verbindung in den regelbaren Topf; ohne Steuerstab in den festen.
 *
 * Aus den Verbindungen wird ein Faktor, der mit der Zahl abflacht: die ersten hundert
 * Verbindungen bringen viel, die tausendste fast nichts. So laesst sich kein beliebig grosser
 * Reaktor bauen, der beliebig viel leistet.
 *
 * Das Kuehlmittel ist nicht nur Kuehlmittel: was den Moderator-Vermerk traegt, verstaerkt
 * zusaetzlich Fluss und Waerme. Schweres Wasser bringt ein Viertel mehr, Thoriumsalz das
 * Zweieinhalbfache -- und macht den Kern entsprechend schwerer zu halten.
 *
 * NICHT UEBERNOMMEN: die OpenComputers-Anbindung und der PWR-Drucker (ein Werkzeug, das den
 * Aufbau als Bild ausgibt; es reitet im Original auf dem Netzwerkpaket der Steuerung mit).
 */
public class MachinePWRControllerBlockEntity extends MachineBaseBlockEntity implements IFluidStandardTransceiverMK2, IControlReceiver {

    public static final long CORE_HEAT_CAPACITY_BASE = 10_000_000L;
    public static final long HULL_HEAT_CAPACITY_BASE = 10_000_000L;
    /** Mehr Kuehlkoerper als das bringen nichts mehr. */
    public static final int MAX_HEATSINKS = 80;

    public static final int SLOT_FUEL_IN = 0;
    public static final int SLOT_FUEL_OUT = 1;
    /** Hier legt man einen Fluidausweis hinein und sagt der Anlage damit, womit sie kuehlt. */
    public static final int SLOT_FLUID_ID = 2;

    public final FluidTank[] tanks = new FluidTank[2];

    public long coreHeat;
    public long coreHeatCapacity = CORE_HEAT_CAPACITY_BASE;
    public long hullHeat;
    public double flux;

    /** Wie weit die Steuerstaebe draussen sind, in Prozent. 100 heisst ganz draussen. */
    public double rodLevel = 100;
    public double rodTarget = 100;

    public int typeLoaded = -1;
    public int amountLoaded;
    public double progress;
    public double processTime;

    public int rodCount;
    public int connections;
    public int connectionsControlled;
    public int heatexCount;
    public int heatsinkCount;
    public int channelCount;
    public int sourceCount;

    public boolean assembled;

    /** Die Anschlussstellen nach draussen und die Brennstabpositionen, beide aus dem Einlesen. */
    protected final List<BlockPos> ports = new ArrayList<>();
    protected final List<BlockPos> rods = new ArrayList<>();

    public MachinePWRControllerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_PWR_CONTROLLER.get(), pos, state, 3);

        this.tanks[0] = new FluidTank(Fluids.COOLANT, 128_000);
        this.tanks[1] = new FluidTank(Fluids.COOLANT_HOT, 128_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.pwrController");
    }

    /** Das Einlesen: aus den gefundenen Bloecken werden die Kennzahlen des Reaktors. */
    public void setup(Map<BlockPos, Block> partMap, Map<BlockPos, Block> rodMap) {

        this.rodCount = 0;
        this.connections = 0;
        this.connectionsControlled = 0;
        this.heatexCount = 0;
        this.channelCount = 0;
        this.heatsinkCount = 0;
        this.sourceCount = 0;
        this.ports.clear();
        this.rods.clear();

        for(Map.Entry<BlockPos, Block> entry : partMap.entrySet()) {

            Block block = entry.getValue();

            if(block == NtmBlocks.PWR_FUEL.get()) this.rodCount++;
            if(block == NtmBlocks.PWR_HEATEX.get()) this.heatexCount++;
            if(block == NtmBlocks.PWR_CHANNEL.get()) this.channelCount++;
            if(block == NtmBlocks.PWR_HEATSINK.get()) this.heatsinkCount++;
            if(block == NtmBlocks.PWR_NEUTRON_SOURCE.get()) this.sourceCount++;
            if(block == NtmBlocks.PWR_PORT.get()) this.ports.add(entry.getKey());
        }

        /* Jede Verbindung wird von beiden Enden aus gefunden, darum am Schluss halbieren. */
        int connectionsDouble = 0;
        int connectionsControlledDouble = 0;

        for(BlockPos fuelPos : rodMap.keySet()) {

            this.rods.add(fuelPos);

            for(Direction dir : Direction.values()) {

                boolean controlled = false;

                for(int i = 1; i < 16; i++) {

                    BlockPos checkPos = fuelPos.relative(dir, i);
                    Block atPos = partMap.get(checkPos);

                    /* Gehaeuse und alles ausserhalb beenden die Sichtlinie. */
                    if(atPos == null || atPos == NtmBlocks.PWR_CASING.get()) break;

                    if(atPos == NtmBlocks.PWR_CONTROL.get()) controlled = true;

                    if(atPos == NtmBlocks.PWR_FUEL.get()) {
                        if(controlled) connectionsControlledDouble++;
                        else connectionsDouble++;
                        break;
                    }

                    /* Ein Reflektor wirft zurueck und zaehlt deshalb doppelt. */
                    if(atPos == NtmBlocks.PWR_REFLECTOR.get()) {
                        if(controlled) connectionsControlledDouble += 2;
                        else connectionsDouble += 2;
                        break;
                    }
                }
            }
        }

        this.connections = connectionsDouble / 2;
        this.connectionsControlled = connectionsControlledDouble / 2;
        this.heatsinkCount = Math.min(this.heatsinkCount, MAX_HEATSINKS);

        this.coreHeatCapacity = CORE_HEAT_CAPACITY_BASE + this.heatsinkCount * (CORE_HEAT_CAPACITY_BASE / 20);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        /* Solange kein Brennstoff eingefahren ist, darf das Kuehlmittel gewechselt werden. */
        if(this.amountLoaded <= 0) this.tanks[0].setType(SLOT_FLUID_ID, this.slots);
        this.setupTanks();

        if(this.assembled) {

            for(BlockPos port : this.ports) {
                for(Direction dir : Direction.values()) {
                    DirPos pos = new DirPos(port.relative(dir), dir);
                    if(this.tanks[1].getFill() > 0) this.tryProvide(this.tanks[1], this.level, pos);
                    this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
                }
            }

            this.loadFuel();
            this.moveRods();

            double multiplier = 1D;
            if(this.tanks[0].getTankType().hasTrait(FT_PWRModerator.class)) {
                multiplier = this.tanks[0].getTankType().getTrait(FT_PWRModerator.class).getMultiplier();
            }

            /* Die Quellen halten den Kern am Leben, auch wenn der Fluss sonst auf null faellt. */
            double newFlux = this.sourceCount * 20D;

            if(this.typeLoaded != -1 && this.amountLoaded > 0 && this.rodCount > 0) {

                EnumPWRFuel fuel = EnumUtil.grabEnumSafely(EnumPWRFuel.class, this.typeLoaded);

                double usedRods = this.getTotalProcessMultiplier();
                double fluxPerRod = this.flux / this.rodCount;
                double outputPerRod = fuel.function.effonix(fluxPerRod);
                double totalOutput = outputPerRod * this.amountLoaded * usedRods;
                double totalHeatOutput = totalOutput * fuel.heatEmission;

                if(this.tanks[0].getFill() > 0) totalHeatOutput *= multiplier;

                this.coreHeat += (long) totalHeatOutput;
                newFlux += totalOutput;

                this.processTime = fuel.yield;
                this.progress += totalOutput;

                if(this.progress >= this.processTime) {
                    this.progress -= this.processTime;
                    this.ejectSpentFuel();
                    this.amountLoaded--;
                    this.setChanged();
                }
            }

            if(this.amountLoaded <= 0) this.typeLoaded = -1;
            if(this.amountLoaded > this.rodCount) this.amountLoaded = this.rodCount;

            this.coolCore();
            this.updateCoolant();

            /* Ein Tausendstel verliert der Kern von allein an die Umgebung. */
            this.coreHeat *= 0.999D;
            this.hullHeat *= 0.999D;

            this.flux = newFlux;
            if(this.tanks[0].getFill() > 0) this.flux *= multiplier;

            if(this.coreHeat > this.coreHeatCapacity) this.meltDown();
        }

        this.networkPackNT(150);
    }

    /** Nimmt Brennstaebe aus dem Eingabefach, solange Sorte und Zahl passen. */
    private void loadFuel() {

        ItemStack in = this.slots.get(SLOT_FUEL_IN);
        if(in.isEmpty() || in.getItem() != NtmItems.PWR_FUEL.get()) return;

        int meta = MetaHelper.getMeta(in);

        if(this.typeLoaded == -1 || this.amountLoaded <= 0) {
            this.typeLoaded = meta;
            this.amountLoaded++;
            in.shrink(1);
            this.setChanged();

        } else if(meta == this.typeLoaded && this.amountLoaded < this.rodCount) {
            this.amountLoaded++;
            in.shrink(1);
            this.setChanged();
        }

        if(in.isEmpty()) this.slots.set(SLOT_FUEL_IN, ItemStack.EMPTY);
    }

    /** Die Steuerstaebe fahren um ein Prozent je Tick auf ihre Zielstellung zu. */
    private void moveRods() {

        double diff = this.rodLevel - this.rodTarget;
        if(diff < 1 && diff > -1) this.rodLevel = this.rodTarget;
        if(this.rodTarget > this.rodLevel) this.rodLevel++;
        if(this.rodTarget < this.rodLevel) this.rodLevel--;
    }

    private void ejectSpentFuel() {

        ItemStack out = this.slots.get(SLOT_FUEL_OUT);
        ItemStack hot = MetaHelper.newStack(NtmItems.PWR_FUEL_HOT.get(), 1, this.typeLoaded);

        if(out.isEmpty()) {
            this.slots.set(SLOT_FUEL_OUT, hot);
        } else if(ItemStack.isSameItemSameComponents(out, hot) && out.getCount() < out.getMaxStackSize()) {
            out.grow(1);
        }
    }

    /**
     * Der Waermeaustausch zwischen Kern und Huelle. Beide laufen aufeinander zu; wie schnell,
     * haengt an der Zahl der Waermetauscher im Verhaeltnis zur Zahl der Staebe.
     */
    private void coolCore() {

        double approach = getXOverE((double) this.heatexCount * 5 / (double) this.getRodCountForCoolant(), 2) / 2D;
        long average = (this.coreHeat + this.hullHeat) / 2;

        this.coreHeat -= (long) ((this.coreHeat - average) * approach);
        this.hullHeat -= (long) ((this.hullHeat - average) * approach);
    }

    /** Die Huellenwaerme geht ins Kuehlmittel und macht daraus heisses Kuehlmittel. */
    protected void updateCoolant() {

        FT_Heatable trait = this.tanks[0].getTankType().getTrait(FT_Heatable.class);
        if(trait == null || trait.getEfficiency(HeatingType.PWR) <= 0) return;

        /* Passen Kanalzahl und Stabzahl zusammen, sind es zehn Prozent je Tick. */
        double coolingEff = Math.min((double) this.channelCount / (double) this.getRodCountForCoolant() * 0.1D, 1D);

        HeatingStep step = trait.getFirstStep();
        if(step == null || step.heatReq <= 0 || step.amountReq <= 0 || step.amountProduced <= 0) return;

        /*
         * Die Tanks fassen ohnehin keine Mengen jenseits des int-Bereichs; die Deckelung haelt nur
         * die Zwischenrechnung heil.
         */
        int heatToUse = (int) Math.min(Math.min(this.hullHeat, (long) (this.hullHeat * coolingEff * trait.getEfficiency(HeatingType.PWR))), 2_000_000_000L);

        int coolCycles = this.tanks[0].getFill() / step.amountReq;
        int hotCycles = (this.tanks[1].getMaxFill() - this.tanks[1].getFill()) / step.amountProduced;
        int heatCycles = heatToUse / step.heatReq;
        int cycles = Math.min(coolCycles, Math.min(hotCycles, heatCycles));

        this.hullHeat -= (long) step.heatReq * cycles;
        this.tanks[0].setFill(this.tanks[0].getFill() - step.amountReq * cycles);
        this.tanks[1].setFill(this.tanks[1].getFill() + step.amountProduced * cycles);
    }

    /** Kuehlkoerper zaehlen zu einem Viertel wie Staebe -- sie wollen auch gekuehlt werden. */
    protected int getRodCountForCoolant() {
        return Math.max(this.rodCount + (int) Math.ceil(this.heatsinkCount / 4D), 1);
    }

    /** Der Kern ist durch: alle Brennstabbloecke werden zu Corium, dann fliegt alles auf. */
    protected void meltDown() {

        this.level.removeBlock(this.getBlockPos(), false);

        if(this.rods.isEmpty()) return;

        double x = 0;
        double y = 0;
        double z = 0;

        for(BlockPos pos : this.rods) {
            this.level.setBlock(pos, NtmBlocks.CORIUM.get().defaultBlockState(), 3);
            x += pos.getX() + 0.5;
            y += pos.getY() + 0.5;
            z += pos.getZ() + 0.5;
        }

        x /= this.rods.size();
        y /= this.rods.size();
        z /= this.rods.size();

        this.level.explode(null, x, y, z, 15F, Level.ExplosionInteraction.BLOCK);
    }

    protected void setupTanks() {

        FT_Heatable trait = this.tanks[0].getTankType().getTrait(FT_Heatable.class);

        if(trait == null || trait.getEfficiency(HeatingType.PWR) <= 0) {
            this.tanks[0].setTankType(Fluids.NONE);
            this.tanks[1].setTankType(Fluids.NONE);
            return;
        }

        this.tanks[1].setTankType(trait.getFirstStep().typeProduced);
    }

    /** Die eingefahrenen Steuerstaebe nehmen ihren Anteil der regelbaren Verbindungen heraus. */
    public double getTotalProcessMultiplier() {
        double totalConnections = this.connections + this.connectionsControlled * (1D - (this.rodLevel / 100D));
        return connectinFunc(totalConnections);
    }

    /**
     * Die Abflachung: bis etwa dreihundert Verbindungen zaehlt jede voll, darueber nur noch ein
     * Fuenfzehntel. Zahlen unveraendert aus dem Original.
     */
    public static double connectinFunc(double connections) {
        return connections / 10D * (1D - getXOverE(connections, 300D)) + connections / 150D * getXOverE(connections, 300D);
    }

    public static double getXOverE(double x, double d) {
        return 1 - Math.pow(Math.E, -x / d);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_FUEL_IN) return stack.getItem() == NtmItems.PWR_FUEL.get();
        return slot == SLOT_FLUID_ID;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { SLOT_FUEL_IN, SLOT_FUEL_OUT };
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == SLOT_FUEL_OUT;
    }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0] }; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[1] }; }
    @Override public FluidTank[] getAllTanks() { return this.tanks; }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getCenter()) < 20 * 20;
    }

    @Override
    public void receiveControl(CompoundTag data) {

        if(data.contains("rodTarget")) {
            this.rodTarget = Mth.clamp(data.getDouble("rodTarget"), 0D, 100D);
            this.setChanged();
        }
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.assembled);
        buf.writeInt(this.rodCount);
        buf.writeLong(this.coreHeat);
        buf.writeLong(this.hullHeat);
        buf.writeDouble(this.flux);
        buf.writeDouble(this.processTime);
        buf.writeDouble(this.progress);
        buf.writeInt(this.typeLoaded);
        buf.writeInt(this.amountLoaded);
        buf.writeDouble(this.rodLevel);
        buf.writeDouble(this.rodTarget);
        buf.writeLong(this.coreHeatCapacity);
        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.assembled = buf.readBoolean();
        this.rodCount = buf.readInt();
        this.coreHeat = buf.readLong();
        this.hullHeat = buf.readLong();
        this.flux = buf.readDouble();
        this.processTime = buf.readDouble();
        this.progress = buf.readDouble();
        this.typeLoaded = buf.readInt();
        this.amountLoaded = buf.readInt();
        this.rodLevel = buf.readDouble();
        this.rodTarget = buf.readDouble();
        this.coreHeatCapacity = buf.readLong();
        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.tanks[0].readFromNBT(tag, "t0");
        this.tanks[1].readFromNBT(tag, "t1");

        this.assembled = tag.getBoolean("assembled");
        this.coreHeat = tag.getLong("coreHeat");
        this.hullHeat = tag.getLong("hullHeat");
        this.flux = tag.getDouble("flux");
        this.rodLevel = tag.getDouble("rodLevel");
        this.rodTarget = tag.getDouble("rodTarget");
        this.typeLoaded = tag.getInt("typeLoaded");
        this.amountLoaded = tag.getInt("amountLoaded");
        this.progress = tag.getDouble("progress");
        this.processTime = tag.getDouble("processTime");

        this.coreHeatCapacity = Math.max(tag.getLong("coreHeatCapacity"), CORE_HEAT_CAPACITY_BASE);

        this.rodCount = tag.getInt("rodCount");
        this.connections = tag.getInt("connections");
        this.connectionsControlled = tag.getInt("connectionsControlled");
        this.heatexCount = tag.getInt("heatexCount");
        this.channelCount = tag.getInt("channelCount");
        this.sourceCount = tag.getInt("sourceCount");
        this.heatsinkCount = tag.getInt("heatsinkCount");

        this.ports.clear();
        for(long packed : tag.getLongArray("ports")) this.ports.add(BlockPos.of(packed));
        this.rods.clear();
        for(long packed : tag.getLongArray("rods")) this.rods.add(BlockPos.of(packed));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        this.tanks[0].writeToNBT(tag, "t0");
        this.tanks[1].writeToNBT(tag, "t1");

        tag.putBoolean("assembled", this.assembled);
        tag.putLong("coreHeat", this.coreHeat);
        tag.putLong("hullHeat", this.hullHeat);
        tag.putDouble("flux", this.flux);
        tag.putDouble("rodLevel", this.rodLevel);
        tag.putDouble("rodTarget", this.rodTarget);
        tag.putInt("typeLoaded", this.typeLoaded);
        tag.putInt("amountLoaded", this.amountLoaded);
        tag.putDouble("progress", this.progress);
        tag.putDouble("processTime", this.processTime);
        tag.putLong("coreHeatCapacity", this.coreHeatCapacity);

        tag.putInt("rodCount", this.rodCount);
        tag.putInt("connections", this.connections);
        tag.putInt("connectionsControlled", this.connectionsControlled);
        tag.putInt("heatexCount", this.heatexCount);
        tag.putInt("channelCount", this.channelCount);
        tag.putInt("sourceCount", this.sourceCount);
        tag.putInt("heatsinkCount", this.heatsinkCount);

        tag.putLongArray("ports", this.ports.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putLongArray("rods", this.rods.stream().mapToLong(BlockPos::asLong).toArray());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachinePWRMenu(id, inventory, this);
    }
}
