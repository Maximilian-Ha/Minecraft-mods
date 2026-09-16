package com.hbm.blockentity.machine.oil;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineSolidifierMenu;
import com.hbm.inventory.recipes.SolidificationRecipes;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.util.Tuple.Pair;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.oil.TileEntityMachineSolidifier.
 *
 * Der Verfestiger macht aus einer Fluessigkeit einen festen Gegenstand -- Wasser zu Eis,
 * Lava zu Obsidian, Oel zu Teer, alles Brennbare zu Brennstoffwuerfeln. Was woraus wird,
 * steht in SolidificationRecipes.
 *
 * Fuenf Plaetze, wie im Original: Ausgabe, Batterie, zwei Aufwertungen, Fluidkennzeichner.
 */
public class MachineSolidifierBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardReceiverMK2, IUpgradeInfoProvider {

    public static final long MAX_POWER = 100_000L;
    public static final int USAGE_BASE = 250;
    public static final int PROCESS_TIME_BASE = 60;

    public static final int SLOT_OUTPUT = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int SLOT_UPGRADE_START = 2;
    public static final int SLOT_UPGRADE_END = 3;
    public static final int SLOT_FLUID_ID = 4;

    public long power;
    public int progress;
    public int usage;
    public int processTime;
    public final FluidTank tank;

    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    public MachineSolidifierBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_SOLIDIFIER.get(), pos, state, 5);
        this.tank = new FluidTank(Fluids.NONE, 24_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machine_solidifier");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, MAX_POWER);
        this.tank.setType(SLOT_FLUID_ID, this.slots);

        if(this.level.getGameTime() % 20 == 0) {
            for(DirPos pos : this.getConPos()) {
                this.trySubscribe(this.level, pos);
                this.trySubscribe(this.tank.getTankType(), this.level, pos);
            }
        }

        this.upgradeManager.checkSlots(this.slots, SLOT_UPGRADE_START, SLOT_UPGRADE_END);
        int speed = this.upgradeManager.getLevel(UpgradeType.SPEED);
        int powerLevel = this.upgradeManager.getLevel(UpgradeType.POWER);

        /* Tempo verkuerzt den Vorgang um je ein Viertel, Sparsamkeit teilt den Verbrauch. */
        this.processTime = PROCESS_TIME_BASE - (PROCESS_TIME_BASE / 4) * speed;
        this.usage = (USAGE_BASE + (USAGE_BASE * speed)) / (powerLevel + 1);

        if(this.canProcess()) {
            this.process();
        } else {
            this.progress = 0;
        }

        this.networkPackNT(50);
    }

    public boolean canProcess() {

        if(this.power < this.usage) return false;

        Pair<Integer, ItemStack> out = SolidificationRecipes.getOutput(this.tank.getTankType());
        if(out == null) return false;
        if(out.getKey() > this.tank.getFill()) return false;

        ItemStack result = out.getValue();
        ItemStack slot = this.getItem(SLOT_OUTPUT);

        if(slot.isEmpty()) return true;
        if(!ItemStack.isSameItemSameComponents(slot, result)) return false;

        return slot.getCount() + result.getCount() <= slot.getMaxStackSize();
    }

    private void process() {

        this.power -= this.usage;
        this.progress++;

        if(this.progress < this.processTime) return;

        Pair<Integer, ItemStack> out = SolidificationRecipes.getOutput(this.tank.getTankType());
        if(out == null) return;

        ItemStack result = out.getValue();
        this.tank.setFill(this.tank.getFill() - out.getKey());

        ItemStack slot = this.getItem(SLOT_OUTPUT);

        if(slot.isEmpty()) {
            this.setItem(SLOT_OUTPUT, result.copy());
        } else {
            ItemStack grown = slot.copy();
            grown.grow(result.getCount());
            this.setItem(SLOT_OUTPUT, grown);
        }

        this.progress = 0;
        this.setChanged();
    }

    public DirPos[] getConPos() {
        BlockPos pos = this.getBlockPos();
        return new DirPos[] {
                new DirPos(pos.getX(), pos.getY() + 4, pos.getZ(), Direction.UP),
                new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Direction.DOWN),
                new DirPos(pos.getX() + 2, pos.getY() + 1, pos.getZ(), Direction.EAST),
                new DirPos(pos.getX() - 2, pos.getY() + 1, pos.getZ(), Direction.WEST),
                new DirPos(pos.getX(), pos.getY() + 1, pos.getZ() + 2, Direction.SOUTH),
                new DirPos(pos.getX(), pos.getY() + 1, pos.getZ() - 2, Direction.NORTH)
        };
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot == SLOT_FLUID_ID) return stack.getItem() instanceof IItemFluidIdentifier;
        if(slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) {
            return stack.getItem() instanceof MachineUpgradeItem item && this.getValidUpgrades().containsKey(item.type);
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == SLOT_OUTPUT;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { SLOT_OUTPUT };
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, java.util.List<Component> components, TooltipFlag flag) {

        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_SOLIDIFIER.get()));

        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(IUpgradeInfoProvider.KEY_DELAY, "-" + (lvl * 25) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(IUpgradeInfoProvider.KEY_CONSUMPTION, "+" + (lvl * 100) + "%").withStyle(ChatFormatting.RED));
        }

        if(type == UpgradeType.POWER) {
            components.add(Component.translatable(IUpgradeInfoProvider.KEY_CONSUMPTION, "-" + (100 - 100 / (lvl + 1)) + "%").withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        upgrades.put(UpgradeType.POWER, 3);
        return upgrades;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.progress = tag.getInt("progress");
        this.tank.readFromNBT(tag, "tank");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        tag.putInt("progress", this.progress);
        this.tank.writeToNBT(tag, "tank");
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.progress);
        buf.writeInt(this.processTime);
        this.tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.progress = buf.readInt();
        this.processTime = buf.readInt();
        this.tank.deserialize(buf);
    }

    @Override public long getPower() { return Math.max(Math.min(this.power, MAX_POWER), 0); }
    @Override public void setPower(long i) { this.power = i; }
    @Override public long getMaxPower() { return MAX_POWER; }

    @Override
    public long transferPower(long power) {
        if(power + this.getPower() <= this.getMaxPower()) {
            this.setPower(power + this.getPower());
            return 0;
        }

        long overshoot = power - (this.getMaxPower() - this.getPower());
        this.setPower(this.getMaxPower());
        return overshoot;
    }

    @Override public boolean canConnect(Direction dir) { return dir != null; }
    @Override public boolean canConnect(FluidType type, Direction dir) { return dir != null; }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tank }; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.tank }; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineSolidifierMenu(id, inventory, this);
    }

    private AABB renderBox;

    /* Kein @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 1, y, z - 1, x + 2, y + 4, z + 2);
        }
        return this.renderBox;
    }
}
