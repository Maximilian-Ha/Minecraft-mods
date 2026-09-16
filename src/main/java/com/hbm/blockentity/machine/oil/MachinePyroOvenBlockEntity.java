package com.hbm.blockentity.machine.oil;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachinePollutingBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachinePyroOvenMenu;
import com.hbm.inventory.recipes.PyroOvenRecipes;
import com.hbm.inventory.recipes.PyroOvenRecipes.PyroOvenRecipe;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.NuclearTechMod;
import com.hbm.particle.NtmParticleTypes;
import com.hbm.particle.vanilla.NbtParticleOptions;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.BobMathUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.util.particle.ParticleUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
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
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.oil.TileEntityMachinePyroOven.
 *
 * Der Pyroofen zerlegt unter Hitze und Sauerstoffabschluss. Jedes Rezept nimmt wahlweise ein
 * Fluid, einen Gegenstand oder beides und gibt wahlweise beides zurueck; was woraus wird,
 * steht in PyroOvenRecipes.
 *
 * Laufen die Rauchtanks ueber, blaest der Ofen den Ueberschuss in die Luft ab und zeigt dabei
 * dieselbe Rauchsaeule wie der Drehrohrofen.
 */
public class MachinePyroOvenBlockEntity extends MachinePollutingBlockEntity implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider {

    public static final long MAX_POWER = 10_000_000L;
    public static final int CONSUMPTION_BASE = 10_000;

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_FLUID_ID = 3;
    public static final int SLOT_UPGRADE_START = 4;
    public static final int SLOT_UPGRADE_END = 5;

    public long power;
    public boolean isVenting;
    public boolean isProgressing;
    public float progress;

    public int prevAnim;
    public int anim;

    public final FluidTank[] tanks;

    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    private AudioWrapper audio;
    private PyroOvenRecipe lastValidRecipe;

    public MachinePyroOvenBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_PYRO_OVEN.get(), pos, state, 6, 50);
        this.tanks = new FluidTank[] {
                new FluidTank(Fluids.NONE, 24_000),
                new FluidTank(Fluids.NONE, 24_000)
        };
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machine_pyro_oven");
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(!this.level.isClientSide) {

            this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, MAX_POWER);
            this.tanks[0].setType(SLOT_FLUID_ID, this.slots);

            for(DirPos pos : this.getConPos()) {
                this.trySubscribe(this.level, pos);
                if(this.tanks[0].getTankType() != Fluids.NONE) this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
                if(this.tanks[1].getFill() > 0) this.tryProvide(this.tanks[1], this.level, pos);
            }

            /* Der Rauch geht allein durch den Schornstein, drei Bloecke ueber dem Kern. */
            this.sendSmoke(new DirPos[] { this.getChimneyPos() });

            this.upgradeManager.checkSlots(this.slots, SLOT_UPGRADE_START, SLOT_UPGRADE_END);
            int speed = this.upgradeManager.getLevel(UpgradeType.SPEED);
            int powerSaving = this.upgradeManager.getLevel(UpgradeType.POWER);
            int overdrive = this.upgradeManager.getLevel(UpgradeType.OVERDRIVE);

            this.isProgressing = false;
            this.isVenting = false;

            if(this.canProcess()) {

                PyroOvenRecipe recipe = this.getMatchingRecipe();
                this.progress += 1F / Math.max((recipe.duration - speed * (recipe.duration / 4)) / (overdrive * 2 + 1), 1);
                this.isProgressing = true;
                this.power -= getConsumption(speed + overdrive * 2, powerSaving);

                if(this.progress >= 1F) {
                    this.progress = 0F;
                    this.finishRecipe(recipe);
                    this.setChanged();
                }

                this.pollute(PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND);

            } else {
                this.progress = 0F;
            }

            this.networkPackNT(50);

        } else {

            this.prevAnim = this.anim;

            if(this.isProgressing) {

                this.anim++;

                if(this.audio == null) {
                    this.audio = this.createAudioLoop();
                    this.audio.startSound();
                } else if(!this.audio.isPlaying()) {
                    this.audio = rebootAudio(this.audio);
                }

                this.audio.keepAlive();
                this.audio.updateVolume(this.getVolume(1F));

                this.spawnExhaust();

            } else if(this.audio != null) {
                this.audio.stopSound();
                this.audio = null;
            }

            if(this.isVenting && this.level.getGameTime() % 2 == 0) this.spawnVentPlume();
        }
    }

    /** Die schwarze Fahne aus dem Schornstein, wenn die Rauchtanks ueberlaufen. */
    private void spawnVentPlume() {

        Direction rot = this.getBlockState().getValue(DummyableBlock.FACING).getCounterClockWise();

        CompoundTag fx = new CompoundTag();
        fx.putFloat("lift", 10F);
        fx.putFloat("base", 0.25F);
        fx.putFloat("max", 2.5F);
        fx.putInt("life", 100 + this.level.random.nextInt(20));
        fx.putInt("color", 0x202020);

        ParticleUtil.addParticle(this.level, new NbtParticleOptions(NtmParticleTypes.COOLING_TOWER.get(), fx),
                this.worldPosition.getX() + 0.5D - rot.getStepX(),
                this.worldPosition.getY() + 3D,
                this.worldPosition.getZ() + 0.5D - rot.getStepZ());
    }

    /**
     * Laeuft ein Rauchtank ueber, geht der Ueberschuss in die Umwelt -- das erledigt die
     * Oberklasse. Hier wird nur vorher vermerkt, dass gleich abgeblasen wird, damit der Schirm
     * die Fahne zeigen kann.
     */
    @Override
    public void pollute(PollutionType type, float amount) {

        FluidTank tank = switch(type) {
            case SOOT -> this.smoke;
            case HEAVYMETAL -> this.smokeLeaded;
            default -> this.smokePoison;
        };

        if(tank.getFill() + (int) Math.ceil(amount * 100F) > tank.getMaxFill()) this.isVenting = true;

        super.pollute(type, amount);
    }

    /** Die vier Auspuffstutzen auf der Haube, je zwei nach vorn und nach hinten versetzt. */
    private void spawnExhaust() {

        if(NuclearTechMod.proxy.me() == null) return;

        double x = this.worldPosition.getX() + 0.5D;
        double y = this.worldPosition.getY() + 3D;
        double z = this.worldPosition.getZ() + 0.5D;

        if(NuclearTechMod.proxy.me().distanceToSqr(x, y, z) > 2500D) return;

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getCounterClockWise();

        for(double offset : new double[] { -2.375D, -0.875D, 0.875D, 2.375D }) {
            if(this.level.random.nextInt(20) != 0) continue;
            this.level.addParticle(ParticleTypes.CLOUD,
                    x - rot.getStepX() + dir.getStepX() * offset, y,
                    z - rot.getStepZ() + dir.getStepZ() * offset,
                    0D, 0.05D, 0D);
        }
    }

    public static int getConsumption(int speed, int powerSaving) {
        return (int) (CONSUMPTION_BASE * Math.pow(speed + 1, 2)) / (powerSaving + 1);
    }

    public PyroOvenRecipe getMatchingRecipe() {

        if(this.lastValidRecipe != null && this.doesRecipeMatch(this.lastValidRecipe)) return this.lastValidRecipe;

        for(PyroOvenRecipe recipe : PyroOvenRecipes.recipes) {
            if(this.doesRecipeMatch(recipe)) {
                this.lastValidRecipe = recipe;
                return recipe;
            }
        }

        return null;
    }

    public boolean doesRecipeMatch(PyroOvenRecipe recipe) {

        if(recipe.inputFluid != null && this.tanks[0].getTankType() != recipe.inputFluid.type) return false;

        ItemStack input = this.getItem(SLOT_INPUT);

        if(recipe.inputItem != null) {
            if(input.isEmpty()) return false;
            return recipe.inputItem.matchesRecipe(input, true);
        }

        /* Ein Rezept ohne Gegenstand laeuft nur, wenn auch keiner im Fach liegt -- sonst
         * fraesse der Ofen jedes Rezept mit reiner Fluideingabe am Gegenstand vorbei. */
        return input.isEmpty();
    }

    public boolean canProcess() {

        int speed = this.upgradeManager.getLevel(UpgradeType.SPEED);
        int powerSaving = this.upgradeManager.getLevel(UpgradeType.POWER);
        if(this.power < getConsumption(speed, powerSaving)) return false;

        PyroOvenRecipe recipe = this.getMatchingRecipe();
        if(recipe == null) return false;

        if(recipe.inputFluid != null && this.tanks[0].getFill() < recipe.inputFluid.fill) return false;
        if(recipe.inputItem != null && this.getItem(SLOT_INPUT).getCount() < recipe.inputItem.stacksize) return false;

        if(recipe.outputFluid != null && recipe.outputFluid.type == this.tanks[1].getTankType()
                && recipe.outputFluid.fill + this.tanks[1].getFill() > this.tanks[1].getMaxFill()) return false;

        if(recipe.outputItem != null) {
            ItemStack out = this.getItem(SLOT_OUTPUT);
            if(!out.isEmpty()) {
                if(!ItemStack.isSameItemSameComponents(out, recipe.outputItem)) return false;
                if(out.getCount() + recipe.outputItem.getCount() > out.getMaxStackSize()) return false;
            }
        }

        return true;
    }

    public void finishRecipe(PyroOvenRecipe recipe) {

        if(recipe.outputItem != null) {
            ItemStack out = this.getItem(SLOT_OUTPUT);
            if(out.isEmpty()) {
                this.setItem(SLOT_OUTPUT, recipe.outputItem.copy());
            } else {
                ItemStack grown = out.copy();
                grown.grow(recipe.outputItem.getCount());
                this.setItem(SLOT_OUTPUT, grown);
            }
        }

        if(recipe.outputFluid != null) {
            this.tanks[1].setTankType(recipe.outputFluid.type);
            this.tanks[1].setFill(this.tanks[1].getFill() + recipe.outputFluid.fill);
        }

        if(recipe.inputItem != null) this.removeItem(SLOT_INPUT, recipe.inputItem.stacksize);
        if(recipe.inputFluid != null) this.tanks[0].setFill(this.tanks[0].getFill() - recipe.inputFluid.fill);
    }

    /** Der Schornstein sitzt drei Bloecke ueber dem Kern, um eins zur Seite versetzt. */
    private DirPos getChimneyPos() {
        Direction rot = this.getBlockState().getValue(DummyableBlock.FACING).getCounterClockWise();
        return new DirPos(this.worldPosition.getX() - rot.getStepX(), this.worldPosition.getY() + 3,
                this.worldPosition.getZ() - rot.getStepZ(), Direction.UP);
    }

    /** Fuenf Anschluesse nebeneinander an der langen Seite. */
    public DirPos[] getConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getCounterClockWise();

        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        DirPos[] positions = new DirPos[5];

        for(int i = 0; i < 5; i++) {
            int offset = 2 - i;
            positions[i] = new DirPos(
                    x + dir.getStepX() * offset + rot.getStepX() * 3, y,
                    z + dir.getStepZ() * offset + rot.getStepZ() * 3, rot);
        }

        return positions;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot == SLOT_INPUT) return true;
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
        return new int[] { SLOT_INPUT, SLOT_OUTPUT };
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {

        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_PYRO_OVEN.get()));

        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(IUpgradeInfoProvider.KEY_DELAY, "-" + (lvl * 25) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(IUpgradeInfoProvider.KEY_CONSUMPTION, "+" + (Math.pow(lvl + 1, 2) * 100 - 100) + "%").withStyle(ChatFormatting.RED));
        }

        if(type == UpgradeType.POWER) {
            components.add(Component.translatable(IUpgradeInfoProvider.KEY_CONSUMPTION, "-" + (100 - 100 / (lvl + 1)) + "%").withStyle(ChatFormatting.GREEN));
        }

        if(type == UpgradeType.OVERDRIVE) {
            components.add(Component.translatable(IUpgradeInfoProvider.KEY_YES).withStyle(BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        upgrades.put(UpgradeType.POWER, 3);
        upgrades.put(UpgradeType.OVERDRIVE, 3);
        return upgrades;
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return AudioWrapper.getLoopedSound(NtmSoundEvents.PYRO_OVEN_OPERATE.get(), SoundSource.BLOCKS, this, 1F, 15F, 1F, 20);
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

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.tanks[0].readFromNBT(tag, "t0");
        this.tanks[1].readFromNBT(tag, "t1");
        this.progress = tag.getFloat("prog");
        this.power = tag.getLong("power");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.tanks[0].writeToNBT(tag, "t0");
        this.tanks[1].writeToNBT(tag, "t1");
        tag.putFloat("prog", this.progress);
        tag.putLong("power", this.power);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
        buf.writeLong(this.power);
        buf.writeBoolean(this.isVenting);
        buf.writeBoolean(this.isProgressing);
        buf.writeFloat(this.progress);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
        this.power = buf.readLong();
        this.isVenting = buf.readBoolean();
        this.isProgressing = buf.readBoolean();
        this.progress = buf.readFloat();
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

    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.tanks[0], this.tanks[1], this.smoke }; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[1], this.smoke }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0] }; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachinePyroOvenMenu(id, inventory, this);
    }

    private AABB renderBox;

    /* Kein @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 3, y, z - 3, x + 4, y + 3, z + 4);
        }
        return this.renderBox;
    }
}
