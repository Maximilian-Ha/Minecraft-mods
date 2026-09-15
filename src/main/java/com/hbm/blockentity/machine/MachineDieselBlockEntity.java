package com.hbm.blockentity.machine;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.config.IConfigurableMachine;
import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.MachinePollutingBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.inventory.menus.MachineDieselMenu;
import com.hbm.items.NtmItems;
import com.hbm.lib.Library;
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

import java.util.EnumMap;
import java.util.Map;
import java.io.IOException;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineDiesel.
 *
 * Verbrennt fluessigen Treibstoff zu Strom. Der Wirkungsgrad haengt von der
 * Treibstoffguete ab; niedrige Gueten (LOW, GAS) nimmt der Generator nicht an.
 * Laesst sich per Knopf in der Oberflaeche und zusaetzlich per Redstone
 * abschalten (Redstonesignal = aus, wie im Original).
 *
 * Nicht uebernommen: die JSON-Konfigurierbarkeit (IConfigurableMachine) fehlt im
 * Port bislang durchgaengig, daher stehen die Kennwerte hier fest.
 */
public class MachineDieselBlockEntity extends MachinePollutingBlockEntity
        implements IEnergyProviderMK2, IFluidStandardReceiverMK2, IControlReceiver, IFluidCopiable {

    public static long maxPower = 50_000L;
    public static int fuelCap = 16_000;
    private static final int SMOKE_BUFFER = 100;

    /** Wirkungsgrad je Treibstoffguete, unveraendert aus dem Original. */
    public static final Map<FuelGrade, Double> FUEL_EFFICIENCY = new EnumMap<>(FuelGrade.class);
    static {
        FUEL_EFFICIENCY.put(FuelGrade.MEDIUM, 0.5D);
        FUEL_EFFICIENCY.put(FuelGrade.HIGH, 0.75D);
        FUEL_EFFICIENCY.put(FuelGrade.AERO, 0.1D);
    }

    public static final int SLOT_FLUID_IN = 0;
    public static final int SLOT_FLUID_OUT = 1;
    public static final int SLOT_BATTERY = 2;
    public static final int SLOT_IDENTIFIER = 3;

    private static final int[] ACCESSIBLE_SLOTS = new int[] { SLOT_FLUID_IN, SLOT_FLUID_OUT, SLOT_BATTERY };

    public long power;
    public boolean isOn = false;
    /** true, solange tatsaechlich verbrannt wird -- steuert Sound und Rauchpartikel. */
    public boolean wasOn = false;
    public FluidTank tank;

    public MachineDieselBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_DIESEL.get(), pos, state, 4, SMOKE_BUFFER);
        this.tank = new FluidTank(Fluids.DIESEL, fuelCap);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machine_diesel");
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        this.wasOn = false;

        this.tank.setType(SLOT_IDENTIFIER, this.slots);
        this.tank.loadTank(this.level, SLOT_FLUID_IN, SLOT_FLUID_OUT, this.slots);

        this.power = Library.chargeItemsFromTE(this.slots, SLOT_BATTERY, this.power, maxPower);

        DirPos[] connections = this.getConPos();

        for(DirPos pos : connections) {
            if(this.power > 0) this.tryProvide(this.level, pos.makeCompat(), pos.getDir());
            this.trySubscribe(this.tank.getTankType(), this.level, pos);
        }
        this.sendSmoke(connections);

        if(this.isOn) this.generate();

        this.power = Math.max(0L, Math.min(this.power, maxPower));
        this.networkPackNT(50);
    }

    private void generate() {
        if(this.level == null) return;
        // Redstonesignal schaltet den Generator ab.
        if(this.level.hasNeighborSignal(this.getBlockPos())) return;
        if(!this.hasAcceptableFuel()) return;
        if(this.tank.getFill() <= 0) return;

        this.wasOn = true;
        this.tank.setFill(Math.max(0, this.tank.getFill() - 1));

        if(this.level.getGameTime() % 5 == 0) {
            this.pollute(this.tank.getTankType(), FluidReleaseType.BURN, 5F);
        }

        this.power = Math.min(maxPower, this.power + this.getHEFromFuel());
    }

    public boolean hasAcceptableFuel() {
        return this.getHEFromFuel() > 0;
    }

    public long getHEFromFuel() {
        return getHEFromFuel(this.tank.getTankType());
    }

    /** Stromausbeute pro Tick fuer den gegebenen Treibstoff, 0 wenn ungeeignet. */
    public static long getHEFromFuel(FluidType type) {
        if(type == null || !type.hasTrait(FT_Combustible.class)) return 0L;

        FT_Combustible fuel = type.getTrait(FT_Combustible.class);
        FuelGrade grade = fuel.getGrade();
        if(grade == FuelGrade.LOW) return 0L;

        double efficiency = FUEL_EFFICIENCY.getOrDefault(grade, 0D);
        return (long) (fuel.getCombustionEnergy() / 1000L * efficiency);
    }

    public DirPos[] getConPos() {
        BlockPos pos = this.getBlockPos();
        return new DirPos[] {
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Direction.EAST),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Direction.WEST),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Direction.SOUTH),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Direction.NORTH),
                new DirPos(pos.getX(), pos.getY() + 1, pos.getZ(), Direction.UP),
                new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Direction.DOWN)
        };
    }

    public int getPowerScaled(int pixels) {
        return (int) (this.getPower() * pixels / Math.max(maxPower, 1L));
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_FLUID_IN) return true;
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot == SLOT_IDENTIFIER) return true;
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return ACCESSIBLE_SLOTS;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if(index == SLOT_FLUID_OUT) {
            return stack.is(NtmItems.CANISTER_EMPTY.get()) || stack.is(NtmItems.TANK_STEEL.get());
        }
        if(index == SLOT_BATTERY && stack.getItem() instanceof IBatteryItem battery) {
            return battery.getCharge(stack) == battery.getMaxCharge(stack);
        }
        return false;
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5) < 625;
    }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("turnOn")) this.isOn = !this.isOn;
        this.setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.isOn = tag.getBoolean("isOn");
        this.power = tag.getLong("power");
        this.tank.readFromNBT(tag, "fuel");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("isOn", this.isOn);
        tag.putLong("power", this.power);
        this.tank.writeToNBT(tag, "fuel");
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeBoolean(this.isOn);
        buf.writeBoolean(this.wasOn);
        this.tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.isOn = buf.readBoolean();
        this.wasOn = buf.readBoolean();
        this.tank.deserialize(buf);
    }

    @Override public long getPower() { return Math.max(0L, Math.min(this.power, maxPower)); }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tank }; }

    @Override
    public FluidTank[] getAllTanks() {
        return new FluidTank[] { this.tank, this.smoke, this.smokeLeaded, this.smokePoison };
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineDieselMenu(id, inventory, this);
    }

    /** Konfiguration, siehe {@link com.hbm.config.MachineDynConfig}. */
    public static void readConfig(JsonObject obj) {
        maxPower = IConfigurableMachine.grab(obj, "L:powerCap", maxPower);
        fuelCap = IConfigurableMachine.grab(obj, "I:fuelCap", fuelCap);

        if(obj.has("D[:efficiency")) {
            JsonArray array = obj.get("D[:efficiency").getAsJsonArray();
            for(FuelGrade grade : FuelGrade.values()) {
                if(grade.ordinal() < array.size()) FUEL_EFFICIENCY.put(grade, array.get(grade.ordinal()).getAsDouble());
            }
        }
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("L:powerCap").value(maxPower);
        writer.name("I:fuelCap").value(fuelCap);

        StringBuilder info = new StringBuilder("Fuel grades in order:");
        for(FuelGrade grade : FuelGrade.values()) info.append(" ").append(grade.name());
        writer.name("INFO").value(info.toString());

        writer.name("D[:efficiency").beginArray().setIndent("");
        for(FuelGrade grade : FuelGrade.values()) writer.value(FUEL_EFFICIENCY.getOrDefault(grade, 0.0D));
        writer.endArray().setIndent("  ");
    }

}
