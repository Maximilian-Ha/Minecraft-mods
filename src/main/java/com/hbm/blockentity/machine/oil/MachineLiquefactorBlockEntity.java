package com.hbm.blockentity.machine.oil;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineLiquefactorMenu;
import com.hbm.inventory.recipes.LiquefactionRecipes;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.lib.Library;
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
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.oil.TileEntityMachineLiquefactor.
 *
 * Das Gegenstueck zum Verfestiger: er macht aus einem festen Gegenstand eine Fluessigkeit.
 * Was woraus wird, steht in LiquefactionRecipes; alles Essbare ohne eigenen Eintrag wird zu
 * Nahrfluessigkeit.
 *
 * Vier Plaetze, wie im Original: Eingabe, Batterie, zwei Aufwertungen.
 */
public class MachineLiquefactorBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardSenderMK2, IUpgradeInfoProvider {

    public static final long MAX_POWER = 100_000L;
    public static final int USAGE_BASE = 250;
    public static final int PROCESS_TIME_BASE = 60;

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int SLOT_UPGRADE_START = 2;
    public static final int SLOT_UPGRADE_END = 3;

    public long power;
    public int progress;
    public int usage;
    public int processTime;
    public final FluidTank tank;

    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    public MachineLiquefactorBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_LIQUEFACTOR.get(), pos, state, 4);
        this.tank = new FluidTank(Fluids.NONE, 24_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machine_liquefactor");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, MAX_POWER);

        for(DirPos pos : this.getConPos()) {
            this.trySubscribe(this.level, pos);
        }

        this.upgradeManager.checkSlots(this.slots, SLOT_UPGRADE_START, SLOT_UPGRADE_END);
        int speed = this.upgradeManager.getLevel(UpgradeType.SPEED);
        int powerLevel = this.upgradeManager.getLevel(UpgradeType.POWER);

        this.processTime = PROCESS_TIME_BASE - (PROCESS_TIME_BASE / 4) * speed;
        this.usage = (USAGE_BASE + (USAGE_BASE * speed)) / (powerLevel + 1);

        if(this.canProcess()) {
            this.process();
        } else {
            this.progress = 0;
        }

        for(DirPos pos : this.getConPos()) {
            this.tryProvide(this.tank, this.level, pos);
        }

        this.networkPackNT(50);
    }

    public boolean canProcess() {

        if(this.power < this.usage) return false;

        ItemStack input = this.getItem(SLOT_INPUT);
        if(input.isEmpty()) return false;

        FluidStack out = LiquefactionRecipes.getOutput(input);
        if(out == null) return false;

        if(out.type != this.tank.getTankType() && this.tank.getFill() > 0) return false;

        return out.fill + this.tank.getFill() <= this.tank.getMaxFill();
    }

    private void process() {

        this.power -= this.usage;
        this.progress++;

        if(this.progress < this.processTime) return;

        FluidStack out = LiquefactionRecipes.getOutput(this.getItem(SLOT_INPUT));
        if(out == null) return;

        this.tank.setTankType(out.type);
        this.tank.setFill(this.tank.getFill() + out.fill);
        this.removeItem(SLOT_INPUT, 1);

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
        if(slot == SLOT_INPUT) return LiquefactionRecipes.getOutput(stack) != null;
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) {
            return stack.getItem() instanceof MachineUpgradeItem item && this.getValidUpgrades().containsKey(item.type);
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { SLOT_INPUT };
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {

        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_LIQUEFACTOR.get()));

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

    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tank }; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.tank }; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineLiquefactorMenu(id, inventory, this);
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
