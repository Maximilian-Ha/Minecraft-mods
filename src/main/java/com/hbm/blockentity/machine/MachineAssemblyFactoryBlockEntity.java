package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.ProxyDynBlockEntity.IProxyDelegateProvider;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineAssemblyFactoryMenu;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.NuclearTechMod;
import com.hbm.module.machine.ModuleMachineAssembler;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.BobMathUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineAssemblyFactory.
 *
 * Das Gegenstueck der Chemiefabrik fuer die Montagemaschine: vier Montagefelder in einem
 * Bauwerk, jedes mit eigener Vorlage, zwoelf Zutatenfaechern, einem Ausgabefach und je einem
 * Tank ein und aus. Sie teilen sich Strom, Aufwertungen und die Kuehlung.
 *
 * ZWOELF ZUTATENFAECHER JE FELD, und das ist der Grund fuer die Maschine: die einzelne
 * Montagemaschine hat ebenso viele, aber nur eines davon. Wer ein Rezept mit zehn Zutaten
 * viermal nebeneinander fahren will, baut sonst vier Maschinen und vier Zuleitungen.
 *
 * SIE BRAUCHT KUEHLWASSER, wie die Chemiefabrik: hundert Millibar je Arbeitsschritt, und der
 * Abdampf kommt hinten wieder heraus. Die Kuehlung hat eigene Anschluesse ueber den dynamischen
 * Stellvertreter aus Runde 113.
 *
 * SIE TEILT KEINE FLUESSIGKEITEN INTERN. Die Chemiefabrik tut das, weil ihre vier Felder eine
 * Kette bilden sollen; hier hat jedes Feld genau einen Tank ein und einen aus, und das Original
 * verzichtet darauf.
 *
 * NICHT UEBERNOMMEN: die Anbindung an Redstone-ueber-Funk und die beiden Greifarme, die das
 * Original ueber dem Bauwerk hin und her fahren laesst.
 */
public class MachineAssemblyFactoryBlockEntity extends MachineBaseBlockEntity
        implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IControlReceiver, IProxyDelegateProvider {

    public static final int FIELDS = 4;
    public static final int SLOTS = 60;

    /** Abstand zweier Felder in der Fachnummerierung. */
    private static final int STRIDE = 14;

    public final FluidTank[] inputTanks = new FluidTank[FIELDS];
    public final FluidTank[] outputTanks = new FluidTank[FIELDS];
    public final FluidTank[] allTanks = new FluidTank[FIELDS * 2 + 2];

    public final FluidTank water;
    public final FluidTank lps;

    public long power;
    public long maxPower = 1_000_000;

    public final boolean[] didProcess = new boolean[FIELDS];

    public boolean frame = false;

    private AudioWrapper audio;

    public final ModuleMachineAssembler[] modules = new ModuleMachineAssembler[FIELDS];
    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    private final CoolantDelegate delegate = new CoolantDelegate();
    private AABB renderBox;

    public MachineAssemblyFactoryBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_ASSEMBLY_FACTORY.get(), pos, state, SLOTS);

        for(int i = 0; i < FIELDS; i++) {
            this.inputTanks[i] = new FluidTank(Fluids.NONE, 4_000);
            this.outputTanks[i] = new FluidTank(Fluids.NONE, 4_000);
        }

        this.water = new FluidTank(Fluids.WATER, 4_000);
        this.lps = new FluidTank(Fluids.SPENTSTEAM, 4_000);

        System.arraycopy(this.inputTanks, 0, this.allTanks, 0, FIELDS);
        System.arraycopy(this.outputTanks, 0, this.allTanks, FIELDS, FIELDS);
        this.allTanks[FIELDS * 2] = this.water;
        this.allTanks[FIELDS * 2 + 1] = this.lps;

        for(int i = 0; i < FIELDS; i++) {
            this.modules[i] = new ModuleMachineAssembler(i, this, this.slots)
                    .itemInput(5 + i * STRIDE)
                    .itemOutput(17 + i * STRIDE)
                    .fluidInput(this.inputTanks[i])
                    .fluidOutput(this.outputTanks[i]);
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineAssemblyFactory");
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(this.maxPower <= 0) this.maxPower = 10_000_000;

        if(!this.level.isClientSide) {

            long nextMaxPower = 0;

            for(ModuleMachineAssembler module : this.modules) {
                GenericRecipe recipe = module.getRecipe();
                if(recipe != null) nextMaxPower += recipe.power * 100;
            }

            this.maxPower = BobMathUtil.max(this.power, nextMaxPower, 1_000_000);

            this.power = Library.chargeTEFromItems(this.slots, 0, this.power, this.maxPower);
            this.upgradeManager.checkSlots(this.slots, 1, 3);

            for(DirPos pos : this.getConPos()) {
                this.trySubscribe(this.level, pos);
                for(FluidTank tank : this.inputTanks) if(tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), this.level, pos);
                for(FluidTank tank : this.outputTanks) if(tank.getFill() > 0) this.tryProvide(tank, this.level, pos);
            }

            for(DirPos pos : this.getCoolPos()) {
                this.delegate.trySubscribe(this.level, pos);
                this.delegate.trySubscribe(this.water.getTankType(), this.level, pos);
                this.delegate.tryProvide(this.lps, this.level, pos);
            }

            double speed = 1D;
            double pow = 1D;

            speed += Math.min(this.upgradeManager.getLevel(UpgradeType.SPEED), 3) / 3D;
            speed += Math.min(this.upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3);

            pow -= Math.min(this.upgradeManager.getLevel(UpgradeType.POWER), 3) * 0.25D;
            pow += Math.min(this.upgradeManager.getLevel(UpgradeType.SPEED), 3);
            pow += Math.min(this.upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3) * 10D / 3D;

            boolean changed = false;

            for(int i = 0; i < FIELDS; i++) {

                this.modules[i].update(speed * 2D, pow * 2D, this.canCool(), this.slots.get(4 + i * STRIDE));
                this.didProcess[i] = this.modules[i].didProcess;
                changed |= this.modules[i].markDirty;

                if(this.modules[i].didProcess) {
                    this.water.setFill(this.water.getFill() - 100);
                    this.lps.setFill(this.lps.getFill() + 100);
                }
            }

            if(changed) this.setChanged();

            this.networkPackNT(100);

        } else {

            boolean didSomething = false;
            for(boolean b : this.didProcess) didSomething |= b;

            if(didSomething && NuclearTechMod.proxy.me() != null
                    && Math.sqrt(NuclearTechMod.proxy.me().distanceToSqr(this.getBlockPos().getBottomCenter())) < 50) {

                if(this.audio == null) {
                    this.audio = this.createAudioLoop();
                    this.audio.startSound();
                } else if(!this.audio.isPlaying()) {
                    this.audio = rebootAudio(this.audio);
                }

                this.audio.keepAlive();
                this.audio.updatePitch(0.75F);
                this.audio.updateVolume(this.getVolume(0.5F));

            } else if(this.audio != null) {
                this.audio.stopSound();
                this.audio = null;
            }

            if(this.level.getGameTime() % 20 == 0) {
                this.frame = !this.level.getBlockState(this.worldPosition.above(3)).isAir();
            }
        }
    }

    public boolean canCool() {
        return this.water.getFill() >= 100 && this.lps.getFill() <= this.lps.getMaxFill() - 100;
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return AudioWrapper.getLoopedSound(NtmSoundEvents.ELECTRIC_MOTOR_LOOP.get(), SoundSource.BLOCKS, this, 0.5F, 15F, 0.75F, 20);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.stopAudio();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.stopAudio();
    }

    private void stopAudio() {
        if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    // ------------------------------------------------------------------------------------
    // Anschluesse -- dieselbe Geometrie wie bei der Chemiefabrik
    // ------------------------------------------------------------------------------------

    private Direction facing() {
        return this.getBlockState().getValue(DummyableBlock.FACING);
    }

    public DirPos[] getConPos() {

        BlockPos p = this.worldPosition;

        DirPos[] pos = new DirPos[16];
        int i = 0;

        for(int k = -2; k <= 2; k += 2) {
            pos[i++] = new DirPos(p.offset(3, 0, k), Direction.EAST);
            pos[i++] = new DirPos(p.offset(-3, 0, k), Direction.WEST);
            pos[i++] = new DirPos(p.offset(k, 0, 3), Direction.SOUTH);
            pos[i++] = new DirPos(p.offset(k, 0, -3), Direction.NORTH);
        }

        for(DirPos io : this.getIOPos()) pos[i++] = io;

        return pos;
    }

    public DirPos[] getCoolPos() {

        Direction dir = this.facing();
        Direction rot = dir.getClockWise(Axis.Y);
        BlockPos p = this.worldPosition;

        return new DirPos[] {
                new DirPos(p.offset(rot.getStepX() + dir.getStepX() * 3, 0, rot.getStepZ() + dir.getStepZ() * 3), dir),
                new DirPos(p.offset(-rot.getStepX() + dir.getStepX() * 3, 0, -rot.getStepZ() + dir.getStepZ() * 3), dir),
                new DirPos(p.offset(rot.getStepX() - dir.getStepX() * 3, 0, rot.getStepZ() - dir.getStepZ() * 3), dir.getOpposite()),
                new DirPos(p.offset(-rot.getStepX() - dir.getStepX() * 3, 0, -rot.getStepZ() - dir.getStepZ() * 3), dir.getOpposite())
        };
    }

    public DirPos[] getIOPos() {

        Direction dir = this.facing();
        Direction rot = dir.getClockWise(Axis.Y);
        BlockPos p = this.worldPosition;

        return new DirPos[] {
                new DirPos(p.offset(dir.getStepX() + rot.getStepX() * 3, 0, dir.getStepZ() + rot.getStepZ() * 3), rot),
                new DirPos(p.offset(-dir.getStepX() + rot.getStepX() * 3, 0, -dir.getStepZ() + rot.getStepZ() * 3), rot),
                new DirPos(p.offset(dir.getStepX() - rot.getStepX() * 3, 0, dir.getStepZ() - rot.getStepZ() * 3), rot.getOpposite()),
                new DirPos(p.offset(-dir.getStepX() - rot.getStepX() * 3, 0, -dir.getStepZ() - rot.getStepZ() * 3), rot.getOpposite())
        };
    }

    @Override
    public Object getDelegateForPosition(BlockPos pos) {

        Direction dir = this.facing();
        Direction rot = dir.getClockWise(Axis.Y);
        BlockPos p = this.worldPosition;

        BlockPos[] line = {
                p.offset(rot.getStepX() + dir.getStepX() * 2, 0, rot.getStepZ() + dir.getStepZ() * 2),
                p.offset(-rot.getStepX() + dir.getStepX() * 2, 0, -rot.getStepZ() + dir.getStepZ() * 2),
                p.offset(rot.getStepX() - dir.getStepX() * 2, 0, rot.getStepZ() - dir.getStepZ() * 2),
                p.offset(-rot.getStepX() - dir.getStepX() * 2, 0, -rot.getStepZ() - dir.getStepZ() * 2)
        };

        for(BlockPos cool : line) if(cool.equals(pos)) return this.delegate;

        return null;
    }

    /** Der Stellvertreter der Kuehlanschluesse: gleicher Strom, aber nur die beiden Kuehltanks. */
    public class CoolantDelegate implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2 {

        @Override public long getPower() { return MachineAssemblyFactoryBlockEntity.this.getPower(); }
        @Override public void setPower(long power) { MachineAssemblyFactoryBlockEntity.this.setPower(power); }
        @Override public long getMaxPower() { return MachineAssemblyFactoryBlockEntity.this.getMaxPower(); }
        @Override public boolean isLoaded() { return MachineAssemblyFactoryBlockEntity.this.isLoaded(); }

        @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { MachineAssemblyFactoryBlockEntity.this.water }; }
        @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { MachineAssemblyFactoryBlockEntity.this.lps }; }
        @Override public FluidTank[] getAllTanks() { return MachineAssemblyFactoryBlockEntity.this.getAllTanks(); }
    }

    // ------------------------------------------------------------------------------------

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {

        if(slot == 0) return true;
        if(slot >= 1 && slot <= 3) return stack.getItem() instanceof MachineUpgradeItem;

        for(int i = 0; i < FIELDS; i++) if(slot == 4 + i * STRIDE) return stack.getItem() == NtmItems.BLUEPRINTS.get();
        for(int i = 0; i < FIELDS; i++) if(this.modules[i].isItemValid(slot, stack)) return true;

        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {

        for(int i = 0; i < FIELDS; i++) {
            if(index == 17 + i * STRIDE) return true;
            if(this.modules[i].isSlotClogged(index)) return true;
        }

        return false;
    }

    @Override public int[] getSlotsForFace(Direction direction) { return ACCESS; }

    private static final int[] ACCESS = access();

    private static int[] access() {
        int[] slots = new int[FIELDS * 13];
        int i = 0;
        for(int f = 0; f < FIELDS; f++) for(int k = 5; k <= 17; k++) slots[i++] = k + f * STRIDE;
        return slots;
    }

    // ------------------------------------------------------------------------------------

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return this.maxPower; }

    @Override public FluidTank[] getReceivingTanks() { return this.inputTanks; }
    @Override public FluidTank[] getSendingTanks() { return this.outputTanks; }
    @Override public FluidTank[] getAllTanks() { return this.allTanks; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineAssemblyFactoryMenu(id, inventory, this);
    }

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override
    public void receiveControl(CompoundTag tag) {

        if(!tag.contains("index") || !tag.contains("selection")) return;

        int index = tag.getInt("index");
        if(index < 0 || index >= FIELDS) return;

        this.modules[index].recipe = tag.getString("selection");
        this.setChanged();
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {

        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_ASSEMBLY_FACTORY.get()));

        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(KEY_SPEED, "+" + (lvl * 100 / 3) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_CONSUMPTION, "+" + (lvl * 100) + "%").withStyle(ChatFormatting.RED));
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
        upgrades.put(UpgradeType.OVERDRIVE, 3);
        return upgrades;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        for(FluidTank tank : this.inputTanks) tank.serialize(buf);
        for(FluidTank tank : this.outputTanks) tank.serialize(buf);
        this.water.serialize(buf);
        this.lps.serialize(buf);
        buf.writeLong(this.power);
        buf.writeLong(this.maxPower);
        for(boolean b : this.didProcess) buf.writeBoolean(b);
        for(ModuleMachineAssembler module : this.modules) module.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(FluidTank tank : this.inputTanks) tank.deserialize(buf);
        for(FluidTank tank : this.outputTanks) tank.deserialize(buf);
        this.water.deserialize(buf);
        this.lps.deserialize(buf);
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        for(int i = 0; i < FIELDS; i++) this.didProcess[i] = buf.readBoolean();
        for(ModuleMachineAssembler module : this.modules) module.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for(int i = 0; i < FIELDS; i++) {
            this.inputTanks[i].readFromNBT(tag, "i" + i);
            this.outputTanks[i].readFromNBT(tag, "o" + i);
        }
        this.water.readFromNBT(tag, "w");
        this.lps.readFromNBT(tag, "s");
        this.power = tag.getLong("power");
        this.maxPower = tag.getLong("maxPower");
        for(ModuleMachineAssembler module : this.modules) module.readFromNBT(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for(int i = 0; i < FIELDS; i++) {
            this.inputTanks[i].writeToNBT(tag, "i" + i);
            this.outputTanks[i].writeToNBT(tag, "o" + i);
        }
        this.water.writeToNBT(tag, "w");
        this.lps.writeToNBT(tag, "s");
        tag.putLong("power", this.power);
        tag.putLong("maxPower", this.maxPower);
        for(ModuleMachineAssembler module : this.modules) module.writeToNBT(tag);
    }

    public AABB getRenderBoundingBox() {

        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 2, p.getY(), p.getZ() - 2, p.getX() + 3, p.getY() + 3, p.getZ() + 3);
        }

        return this.renderBox;
    }
}
