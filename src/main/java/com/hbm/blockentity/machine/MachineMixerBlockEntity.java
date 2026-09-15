package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineMixerMenu;
import com.hbm.inventory.recipes.MixerRecipes;
import com.hbm.inventory.recipes.MixerRecipes.MixerRecipe;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.util.BobMathUtil;
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
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineMixer.
 * Mischt zwei Fluide und einen Feststoff zu einem Ausgangsfluid; welches Rezept
 * gefahren wird, bestimmt die Fluidkennung im Ausgangstank plus der Rezeptindex,
 * den der Spieler ueber die Oberflaeche durchschaltet.
 */
public class MachineMixerBlockEntity extends MachineBaseBlockEntity implements IControlReceiver, IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IFluidCopiable {

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_IDENTIFIER = 2;
    public static final int SLOT_UPGRADE_START = 3;
    public static final int SLOT_UPGRADE_END = 4;

    public static final long maxPower = 10_000;

    public long power;
    public int progress;
    public int processTime;
    public int recipeIndex;

    /** Nur Client: Drehung des Ruehrwerks */
    public float rotation;
    public float prevRotation;
    public boolean wasOn = false;

    // todo config
    private int consumption = 50;

    public final FluidTank[] tanks = new FluidTank[3];

    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    private AABB renderBox;

    public MachineMixerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_MIXER.get(), pos, state, 5);

        this.tanks[0] = new FluidTank(Fluids.NONE, 16_000);
        this.tanks[1] = new FluidTank(Fluids.NONE, 16_000);
        this.tanks[2] = new FluidTank(Fluids.NONE, 24_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineMixer");
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide) {

            this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, maxPower);
            this.tanks[2].setType(SLOT_IDENTIFIER, this.slots);

            this.upgradeManager.checkSlots(this.slots, SLOT_UPGRADE_START, SLOT_UPGRADE_END);
            int speedLevel = this.upgradeManager.getLevel(UpgradeType.SPEED);
            int powerLevel = this.upgradeManager.getLevel(UpgradeType.POWER);
            int overLevel = this.upgradeManager.getLevel(UpgradeType.OVERDRIVE);

            this.consumption = 50;

            this.consumption += speedLevel * 150;
            this.consumption -= this.consumption * powerLevel * 0.25;
            this.consumption *= (overLevel * 3 + 1);

            this.wasOn = this.canProcess();

            if(this.wasOn) {
                this.progress++;
                this.power -= this.getConsumption();

                this.processTime -= this.processTime * speedLevel / 4;
                this.processTime /= (overLevel + 1);

                if(this.processTime <= 0) this.processTime = 1;

                if(this.progress >= this.processTime) {
                    this.process();
                    this.progress = 0;
                    this.setChanged();
                }

            } else {
                this.progress = 0;
            }

            // Ersatz fuer autoPort() aus 1.7.10: Strom abonnieren, Eingaenge abonnieren, Ausgang anbieten
            for(DirPos pos : this.getConPos()) {
                this.trySubscribe(this.level, pos);

                if(this.tanks[0].getTankType() != Fluids.NONE) this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
                if(this.tanks[1].getTankType() != Fluids.NONE) this.trySubscribe(this.tanks[1].getTankType(), this.level, pos);

                if(this.tanks[2].getFill() > 0) this.tryProvide(this.tanks[2], this.level, pos);
            }

            this.networkPackNT(50);

        } else {

            this.prevRotation = this.rotation;

            if(this.wasOn) {
                this.rotation += 20F;
            }

            if(this.rotation >= 360) {
                this.rotation -= 360;
                this.prevRotation -= 360;
            }
        }
    }

    public boolean canProcess() {

        MixerRecipe[] recipes = MixerRecipes.getOutput(this.tanks[2].getTankType());
        if(recipes == null || recipes.length <= 0) {
            this.recipeIndex = 0;
            return false;
        }

        this.recipeIndex = this.recipeIndex % recipes.length;
        MixerRecipe recipe = recipes[this.recipeIndex];
        if(recipe == null) {
            this.recipeIndex = 0;
            return false;
        }

        this.tanks[0].setTankType(recipe.input1 != null ? recipe.input1.type : Fluids.NONE);
        this.tanks[1].setTankType(recipe.input2 != null ? recipe.input2.type : Fluids.NONE);

        if(recipe.input1 != null && this.tanks[0].getFill() < recipe.input1.fill) return false;
        if(recipe.input2 != null && this.tanks[1].getFill() < recipe.input2.fill) return false;

        /* die einfachste Pruefung kaeme sonst zuerst, aber die Fluidpruefung richtet die Tanks ein -- das soll auch ohne Strom geschehen */
        if(this.power < this.getConsumption()) return false;

        if(recipe.output + this.tanks[2].getFill() > this.tanks[2].getMaxFill()) return false;

        if(recipe.solidInput != null) {

            ItemStack input = this.slots.get(SLOT_INPUT);
            if(input.isEmpty()) return false;

            if(!recipe.solidInput.matchesRecipe(input, true) || recipe.solidInput.stacksize > input.getCount()) return false;
        }

        this.processTime = recipe.processTime;
        return true;
    }

    protected void process() {

        MixerRecipe[] recipes = MixerRecipes.getOutput(this.tanks[2].getTankType());
        MixerRecipe recipe = recipes[this.recipeIndex % recipes.length];

        if(recipe.input1 != null) this.tanks[0].setFill(this.tanks[0].getFill() - recipe.input1.fill);
        if(recipe.input2 != null) this.tanks[1].setFill(this.tanks[1].getFill() - recipe.input2.fill);
        if(recipe.solidInput != null) this.removeItem(SLOT_INPUT, recipe.solidInput.stacksize);
        this.tanks[2].setFill(this.tanks[2].getFill() + recipe.output);
    }

    public int getConsumption() {
        return this.consumption;
    }

    protected DirPos[] getConPos() {
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x, y - 1, z, Direction.DOWN),
                new DirPos(x + 1, y, z, Direction.EAST),
                new DirPos(x - 1, y, z, Direction.WEST),
                new DirPos(x, y, z + 1, Direction.SOUTH),
                new DirPos(x, y, z - 1, Direction.NORTH),
        };
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { SLOT_INPUT };
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {

        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot == SLOT_IDENTIFIER) return stack.getItem() instanceof IItemFluidIdentifier;
        if(slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) {
            return stack.getItem() instanceof MachineUpgradeItem item && this.getValidUpgrades().containsKey(item.type);
        }

        if(slot != SLOT_INPUT) return false;

        MixerRecipe[] recipes = MixerRecipes.getOutput(this.tanks[2].getTankType());
        if(recipes == null || recipes.length <= 0) return false;

        MixerRecipe recipe = recipes[this.recipeIndex % recipes.length];
        if(recipe == null || recipe.solidInput == null) return false;

        return recipe.solidInput.matchesRecipe(stack, true);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.processTime);
        buf.writeInt(this.progress);
        buf.writeInt(this.recipeIndex);
        buf.writeBoolean(this.wasOn);

        for(FluidTank tank : this.tanks) tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.processTime = buf.readInt();
        this.progress = buf.readInt();
        this.recipeIndex = buf.readInt();
        this.wasOn = buf.readBoolean();

        for(FluidTank tank : this.tanks) tank.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.power = tag.getLong("power");
        this.progress = tag.getInt("progress");
        this.processTime = tag.getInt("processTime");
        this.recipeIndex = tag.getInt("recipe");
        for(int i = 0; i < 3; i++) this.tanks[i].readFromNBT(tag, i + "");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putLong("power", this.power);
        tag.putInt("progress", this.progress);
        tag.putInt("processTime", this.processTime);
        tag.putInt("recipe", this.recipeIndex);
        for(int i = 0; i < 3; i++) this.tanks[i].writeToNBT(tag, i + "");
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override public FluidTank[] getAllTanks() { return this.tanks; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[2] }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0], this.tanks[1] }; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineMixerMenu(id, inventory, this);
    }

    @Override
    public boolean hasPermission(Player player) {
        return this.stillValid(player);
    }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("toggle")) this.recipeIndex++;
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {
        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_MIXER.get()));
        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(KEY_DELAY, "-" + (lvl * 25) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_CONSUMPTION, "+" + (lvl * 300) + "%").withStyle(ChatFormatting.RED));
        }
        if(type == UpgradeType.POWER) {
            components.add(Component.translatable(KEY_CONSUMPTION, "-" + (lvl * 25) + "%").withStyle(ChatFormatting.GREEN));
        }
        if(type == UpgradeType.OVERDRIVE) {
            components.add(Component.translatable(KEY_YES).withStyle(BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        upgrades.put(UpgradeType.POWER, 3);
        upgrades.put(UpgradeType.OVERDRIVE, 6);
        return upgrades;
    }

    @Override
    public FluidTank getTankToPaste() {
        return this.tanks[2];
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x, y, z, x + 1, y + 3, z + 1);
        }
        return this.renderBox;
    }
}
