package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.MachineElectricFurnaceBlock;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.menus.MachineElectricFurnaceMenu;
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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineElectricFurnace.
 *
 * Verbrennt Strom statt Brennstoff und nutzt die normalen Schmelzrezepte von Minecraft.
 * Die Kennzahlen (Stromspeicher, Verbrauch, Dauer, Upgrade-Effekte) sind unveraendert
 * aus dem Original uebernommen.
 *
 * Unterschied zum Original: dort gab es zwei getrennte Bloecke
 * (machine_electric_furnace_off / _on). Auf 1.21.1 ist das ein Block mit der
 * Blockstate-Eigenschaft LIT -- so macht es auch der Vanilla-Ofen.
 */
public class MachineElectricFurnaceBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IUpgradeInfoProvider {

    public static final long MAX_POWER = 100_000L;
    /** Ticks pro Schmelzvorgang ohne Upgrades. */
    public static final int BASE_PROCESSING_SPEED = 100;
    /** HE pro Tick ohne Upgrades. */
    public static final int BASE_CONSUMPTION = 50;
    /** Wartezeit in Ticks, nachdem der Strom ausgegangen ist. */
    private static final int COOLDOWN_TICKS = 20;

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_UPGRADE = 3;

    private static final int[] ACCESSIBLE_SLOTS = new int[] { SLOT_BATTERY, SLOT_INPUT, SLOT_OUTPUT };

    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    public long power;
    public int progress;
    public int maxProgress = BASE_PROCESSING_SPEED;
    public int consumption = BASE_CONSUMPTION;

    private int cooldown;

    public MachineElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_ELECTRIC_FURNACE.get(), pos, state, 4);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machine_electric_furnace");
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        if(this.cooldown > 0) this.cooldown--;

        this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.getPower(), this.getMaxPower());

        for(DirPos dirPos : this.getConPos()) {
            this.trySubscribe(this.level, dirPos);
        }

        this.applyUpgrades();

        if(!this.hasPower()) {
            this.cooldown = COOLDOWN_TICKS;
        }

        boolean wasLit = this.progress > 0;

        // Einmal pro Tick ermitteln und weiterreichen: die Rezeptsuche geht ueber alle
        // Schmelzrezepte, sie soll nicht mehrfach je Tick laufen.
        ItemStack result = this.getSmeltingResult(this.slots.get(SLOT_INPUT));

        if(this.hasPower() && this.canProcess(result)) {
            this.progress++;
            this.power -= this.consumption;

            if(this.progress >= this.maxProgress) {
                this.progress = 0;
                this.processItem(result);
                this.setChanged();
            }
        } else {
            this.progress = 0;
        }

        this.power = Math.max(0L, Math.min(this.power, this.getMaxPower()));

        if(wasLit != (this.progress > 0)) {
            this.updateLitState(this.progress > 0);
        }

        this.networkPackNT(50);
    }

    /**
     * Upgrade-Rechnung unveraendert aus dem Original:
     * Tempo-Upgrade verkuerzt die Dauer und erhoeht den Verbrauch, Strom-Upgrade
     * umgekehrt.
     */
    private void applyUpgrades() {
        this.maxProgress = BASE_PROCESSING_SPEED;
        this.consumption = BASE_CONSUMPTION;

        this.upgradeManager.checkSlots(this.slots, SLOT_UPGRADE, SLOT_UPGRADE);

        int speedLevel = this.upgradeManager.getLevel(UpgradeType.SPEED);
        int powerLevel = this.upgradeManager.getLevel(UpgradeType.POWER);

        this.maxProgress -= speedLevel * 25;
        this.consumption += speedLevel * 50;
        this.maxProgress += powerLevel * 10;
        this.consumption -= powerLevel * 15;

        this.maxProgress = Math.max(1, this.maxProgress);
        this.consumption = Math.max(1, this.consumption);
    }

    private void updateLitState(boolean lit) {
        if(this.level == null) return;

        BlockState state = this.getBlockState();
        if(!(state.getBlock() instanceof MachineElectricFurnaceBlock)) return;
        if(state.getValue(MachineElectricFurnaceBlock.LIT) == lit) return;

        this.level.setBlock(this.getBlockPos(), state.setValue(MachineElectricFurnaceBlock.LIT, lit), Block.UPDATE_ALL);
    }

    /**
     * Zwischengespeicherte Rezeptsuche, genau wie der Vanilla-Ofen sie benutzt. Vorher lief hier
     * eine lineare Schleife ueber saemtliche Schmelzrezepte -- einmal pro Tick und zusaetzlich
     * bei jedem Einlagerungsversuch durch einen Trichter.
     */
    private final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> quickCheck =
            RecipeManager.createCheck(RecipeType.SMELTING);

    /** Schmelzergebnis nach den normalen Ofenrezepten, oder ItemStack.EMPTY. */
    private ItemStack getSmeltingResult(ItemStack input) {
        if(this.level == null || input.isEmpty()) return ItemStack.EMPTY;

        SingleRecipeInput recipeInput = new SingleRecipeInput(input.copy());

        return this.quickCheck.getRecipeFor(recipeInput, this.level)
                .map(holder -> holder.value().getResultItem(this.level.registryAccess()).copy())
                .orElse(ItemStack.EMPTY);
    }

    private boolean canProcess(ItemStack result) {
        if(this.cooldown > 0) return false;
        if(this.slots.get(SLOT_INPUT).isEmpty()) return false;
        if(result.isEmpty()) return false;

        ItemStack output = this.slots.get(SLOT_OUTPUT);
        if(output.isEmpty()) return true;
        if(!ItemStack.isSameItemSameComponents(output, result)) return false;

        return output.getCount() + result.getCount() <= Math.min(this.getMaxStackSize(), output.getMaxStackSize());
    }

    private void processItem(ItemStack result) {
        if(result.isEmpty()) return;

        ItemStack input = this.slots.get(SLOT_INPUT);
        ItemStack output = this.slots.get(SLOT_OUTPUT);
        if(output.isEmpty()) {
            this.slots.set(SLOT_OUTPUT, result.copy());
        } else {
            output.grow(result.getCount());
        }

        input.shrink(1);
        if(input.isEmpty()) this.slots.set(SLOT_INPUT, ItemStack.EMPTY);
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

    public int getProgressScaled(int pixels) {
        return this.maxProgress <= 0 ? 0 : this.progress * pixels / this.maxProgress;
    }

    public int getPowerRemainingScaled(int pixels) {
        return (int) (this.getPower() * pixels / Math.max(this.getMaxPower(), 1L));
    }

    public boolean hasPower() {
        return this.power >= this.consumption;
    }

    public boolean isProcessing() {
        return this.progress > 0;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot == SLOT_INPUT) return !this.getSmeltingResult(stack).isEmpty();
        if(slot == SLOT_UPGRADE) {
            return stack.getItem() instanceof MachineUpgradeItem item && this.getValidUpgrades().containsKey(item.type);
        }
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return ACCESSIBLE_SLOTS;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if(index == SLOT_OUTPUT) return true;
        // Leere Batterien duerfen wieder herausgezogen werden.
        if(index == SLOT_BATTERY && stack.getItem() instanceof IBatteryItem battery) {
            return battery.getCharge(stack) == 0;
        }
        return false;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.progress = tag.getInt("progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        tag.putInt("progress", this.progress);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.maxProgress);
        buf.writeInt(this.progress);
        buf.writeInt(this.consumption);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.maxProgress = buf.readInt();
        this.progress = buf.readInt();
        this.consumption = buf.readInt();
    }

    @Override
    public long getPower() {
        return Math.max(0L, Math.min(this.power, this.getMaxPower()));
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineElectricFurnaceMenu(id, inventory, this);
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {
        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_ELECTRIC_FURNACE.get()));
        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(KEY_DELAY, "-" + (lvl * 25) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_CONSUMPTION, "+" + (lvl * 100) + "%").withStyle(ChatFormatting.RED));
        }
        if(type == UpgradeType.POWER) {
            components.add(Component.translatable(KEY_CONSUMPTION, "-" + (lvl * 30) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_DELAY, "+" + (lvl * 10) + "%").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        upgrades.put(UpgradeType.POWER, 3);
        return upgrades;
    }
}
