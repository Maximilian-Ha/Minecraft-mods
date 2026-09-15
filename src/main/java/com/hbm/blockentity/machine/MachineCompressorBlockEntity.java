package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineCompressorMenu;
import com.hbm.inventory.recipes.CompressorRecipes;
import com.hbm.inventory.recipes.CompressorRecipes.CompressorRecipe;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.sounds.SoundSource;
import com.hbm.main.NuclearTechMod;

/**
 * Portiert aus 1.7.10: TileEntityMachineCompressor + TileEntityMachineCompressorBase.
 * Beide Klassen sind hier zusammengefasst; die gemeinsamen Teile der Basisklasse sind
 * ueber die geschuetzten Haken (updateAnimation, getConPos, getInfoBlock,
 * getRenderBoundingBox) fuer Ableitungen wie MachineCompressorCompactBlockEntity offen.
 */
public class MachineCompressorBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IControlReceiver, IFluidCopiable {

    public static final int SLOT_IDENTIFIER = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int SLOT_UPGRADE_START = 2;
    public static final int SLOT_UPGRADE_END = 3;

    public static final long maxPower = 100_000;
    public static final int processTimeBase = 100;
    public static final int powerRequirementBase = 2_500;

    public final FluidTank[] tanks = new FluidTank[2];
    public long power;
    public boolean isOn;
    public int progress;
    public int processTime = processTimeBase;
    public int powerRequirement;

    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    /** Nur Client: Anzeige von Luefter und Pumpenhub */
    public float fanSpin;
    public float prevFanSpin;
    public float piston;
    public float prevPiston;
    public boolean pistonDir;
    private float randSpeed = 0.1F;

    protected AABB renderBox;

    public MachineCompressorBlockEntity(BlockPos pos, BlockState state) {
        this(NtmBlockEntityTypes.MACHINE_COMPRESSOR.get(), pos, state);
    }

    protected MachineCompressorBlockEntity(BlockEntityType<? extends MachineCompressorBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 4);

        this.tanks[0] = new FluidTank(Fluids.NONE, 16_000);
        this.tanks[1] = new FluidTank(Fluids.NONE, 16_000).withPressure(1);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineCompressor");
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide) {

            this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, maxPower);
            this.tanks[0].setType(SLOT_IDENTIFIER, this.slots);
            this.setupTanks();

            this.upgradeManager.checkSlots(this.slots, SLOT_BATTERY, SLOT_UPGRADE_END);

            int speedLevel = this.upgradeManager.getLevel(UpgradeType.SPEED);
            int powerLevel = this.upgradeManager.getLevel(UpgradeType.POWER);
            int overLevel = this.upgradeManager.getLevel(UpgradeType.OVERDRIVE);

            CompressorRecipe rec = CompressorRecipes.getRecipe(this.tanks[0].getTankType(), this.tanks[0].getPressure());
            int timeBase = processTimeBase;
            if(rec != null) timeBase = rec.duration;

            // Ohne Rezept gelten feste Stufen, mit Rezept wird die Rezeptdauer geteilt -- so im Original
            if(rec == null) this.processTime = speedLevel == 3 ? 10 : speedLevel == 2 ? 20 : speedLevel == 1 ? 60 : timeBase;
            else this.processTime = timeBase / (speedLevel + 1);
            this.powerRequirement = powerRequirementBase / (powerLevel + 1);
            this.processTime = this.processTime / (overLevel + 1);
            this.powerRequirement = this.powerRequirement * ((overLevel * 2) + 1);

            if(this.processTime <= 0) this.processTime = 1;

            if(this.canProcess()) {
                this.progress++;
                this.isOn = true;
                this.power -= this.powerRequirement;

                if(this.progress >= this.processTime) {
                    this.progress = 0;
                    this.process();
                    this.setChanged();
                }

            } else {
                this.progress = 0;
                this.isOn = false;
            }

            // Ersatz fuer autoPort() aus 1.7.10: Strom abonnieren, Eingang abonnieren, Ausgang anbieten
            for(DirPos pos : this.getConPos()) {
                this.trySubscribe(this.level, pos);

                if(this.tanks[0].getTankType() != Fluids.NONE) {
                    this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
                }

                if(this.tanks[1].getFill() > 0) {
                    this.tryProvide(this.tanks[1], this.level, pos);
                }
            }

            this.networkPackNT(100);

        } else {
            this.updateAnimation();
        }
    }

    /** Client-Animation; die kompakte Bauform hat nur Luefter und ueberschreibt das */
    protected void updateAnimation() {

        this.prevFanSpin = this.fanSpin;
        this.prevPiston = this.piston;

        if(this.isOn) {
            this.fanSpin += 15;

            if(this.fanSpin >= 360) {
                this.prevFanSpin -= 360;
                this.fanSpin -= 360;
            }

            if(this.pistonDir) {
                this.piston -= this.randSpeed;
                if(this.piston <= 0) {
                    // Kolbenschlag am unteren Umkehrpunkt (Original Z. 40). updateAnimation()
                    // laeuft im Client-Zweig, deshalb der Proxy wie beim Montagewerk.
                    NuclearTechMod.proxy.playLocalSound(this.getBlockPos().getCenter(), NtmSoundEvents.BOLTGUN.get(),
                            SoundSource.BLOCKS, this.getVolume(0.5F), 0.75F);
                    this.pistonDir = !this.pistonDir;
                }
            } else {
                this.piston += 0.05F;
                if(this.piston >= 1) {
                    this.randSpeed = 0.085F + this.level.random.nextFloat() * 0.03F;
                    this.pistonDir = !this.pistonDir;
                }
            }

            this.piston = Mth.clamp(this.piston, 0F, 1F);
        }
    }

    public boolean canProcess() {

        if(this.power <= this.powerRequirement) return false;

        CompressorRecipe recipe = CompressorRecipes.getRecipe(this.tanks[0].getTankType(), this.tanks[0].getPressure());

        if(recipe == null) {
            return this.tanks[0].getFill() >= 1_000 && this.tanks[1].getFill() + 1_000 <= this.tanks[1].getMaxFill();
        }

        return this.tanks[0].getFill() >= recipe.inputAmount && this.tanks[1].getFill() + recipe.output.fill <= this.tanks[1].getMaxFill();
    }

    public void process() {

        CompressorRecipe recipe = CompressorRecipes.getRecipe(this.tanks[0].getTankType(), this.tanks[0].getPressure());

        if(recipe == null) {
            this.tanks[0].setFill(this.tanks[0].getFill() - 1_000);
            this.tanks[1].setFill(this.tanks[1].getFill() + 1_000);
        } else {
            this.tanks[0].setFill(this.tanks[0].getFill() - recipe.inputAmount);
            this.tanks[1].setFill(this.tanks[1].getFill() + recipe.output.fill);
        }
    }

    protected void setupTanks() {

        CompressorRecipe recipe = CompressorRecipes.getRecipe(this.tanks[0].getTankType(), this.tanks[0].getPressure());

        if(recipe == null) {
            this.tanks[1].withPressure(this.tanks[0].getPressure() + 1).setTankType(this.tanks[0].getTankType());
        } else {
            this.tanks[1].withPressure(recipe.output.pressure).setTankType(recipe.output.type);
        }
    }

    /** Setzt die Eingangsdruckstufe und zieht den Ausgangstank nach */
    public void setCompression(int compression) {

        if(compression == this.tanks[0].getPressure()) return;

        this.tanks[0].withPressure(compression);

        CompressorRecipe recipe = CompressorRecipes.getRecipe(this.tanks[0].getTankType(), compression);

        if(recipe == null) {
            this.tanks[1].withPressure(compression + 1);
        } else {
            this.tanks[1].withPressure(recipe.output.pressure).setTankType(recipe.output.type);
        }

        this.setChanged();
    }

    /** Block, dessen Name im Upgrade-Tooltip steht */
    protected Block getInfoBlock() {
        return NtmBlocks.MACHINE_COMPRESSOR.get();
    }

    public DirPos[] getConPos() {
        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getCounterClockWise();

        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x + rot.getStepX() * 2, y, z + rot.getStepZ() * 2, rot),
                new DirPos(x - rot.getStepX() * 2, y, z - rot.getStepZ() * 2, rot.getOpposite()),
                new DirPos(x - dir.getStepX() * 2, y, z - dir.getStepZ() * 2, dir.getOpposite())
        };
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.progress);
        buf.writeInt(this.processTime);
        buf.writeInt(this.powerRequirement);
        buf.writeLong(this.power);
        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
        buf.writeBoolean(this.isOn);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.progress = buf.readInt();
        this.processTime = buf.readInt();
        this.powerRequirement = buf.readInt();
        this.power = buf.readLong();
        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
        this.isOn = buf.readBoolean();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.progress = tag.getInt("progress");
        this.tanks[0].readFromNBT(tag, "0");
        this.tanks[1].readFromNBT(tag, "1");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        tag.putInt("progress", this.progress);
        this.tanks[0].writeToNBT(tag, "0");
        this.tanks[1].writeToNBT(tag, "1");
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_IDENTIFIER) return stack.getItem() instanceof IItemFluidIdentifier;
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) {
            return stack.getItem() instanceof MachineUpgradeItem item && this.getValidUpgrades().containsKey(item.type);
        }
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        // Wie im Original: TileEntityMachineBase.getAccessibleSlotsFromSide gibt ein leeres Feld
        // zurueck und der Kompressor ueberschreibt das nicht. Trichter und Rohre kommen also
        // nicht an Fluid-Kennung, Batterie oder Upgrades heran.
        return new int[0];
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override public FluidTank[] getAllTanks() { return this.tanks; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[1] }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0] }; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineCompressorMenu(id, inventory, this);
    }

    @Override
    public boolean hasPermission(Player player) {
        return this.stillValid(player);
    }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("compression")) {
            this.setCompression(tag.getInt("compression"));
        }
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {
        components.add(IUpgradeInfoProvider.getStandardLabel(this.getInfoBlock()));
        if(type == UpgradeType.SPEED) {
            components.add(Component.literal("Generic compression: ").append(Component.translatable(KEY_DELAY, "-" + (lvl == 3 ? 90 : lvl == 2 ? 80 : lvl == 1 ? 40 : 0) + "%")).withStyle(ChatFormatting.GREEN));
            components.add(Component.literal("Recipe: ").append(Component.translatable(KEY_DELAY, "-" + (100 - 100 / (lvl + 1)) + "%")).withStyle(ChatFormatting.GREEN));
        }
        if(type == UpgradeType.POWER) {
            components.add(Component.translatable(KEY_CONSUMPTION, "-" + (100 - 100 / (lvl + 1)) + "%").withStyle(ChatFormatting.GREEN));
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
        upgrades.put(UpgradeType.OVERDRIVE, 9);
        return upgrades;
    }

    @Override
    public CompoundTag getSettings(Level level, BlockPos pos) {
        CompoundTag tag = IFluidCopiable.super.getSettings(level, pos);
        tag.putInt("compression", this.tanks[0].getPressure());
        return tag;
    }

    @Override
    public void pasteSettings(CompoundTag tag, int index, Level level, Player player, BlockPos pos) {
        if(tag.contains("compression")) {
            this.setCompression(tag.getInt("compression"));
        }
        IFluidCopiable.super.pasteSettings(tag, index, level, player, pos);
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 2, y, z - 2, x + 3, y + 9, z + 3);
        }
        return this.renderBox;
    }
}
