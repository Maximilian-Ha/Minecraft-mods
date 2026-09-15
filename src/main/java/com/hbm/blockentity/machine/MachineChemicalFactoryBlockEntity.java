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
import com.hbm.inventory.menus.MachineChemicalFactoryMenu;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.NuclearTechMod;
import com.hbm.module.machine.ModuleMachineChemicalPlant;
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
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineChemicalFactory.
 *
 * Die Chemiefabrik ist die Chemieanlage vier Mal in einem Bauwerk: vier Rezeptfelder, jedes mit
 * eigener Vorlage, eigenen drei Zutaten- und drei Ausgabefaechern und eigenen sechs Tanks. Sie
 * teilen sich Strom, Aufwertungen und die Kuehlung.
 *
 * SIE BRAUCHT KUEHLWASSER, und das ist ihr eigentlicher Unterschied zur einzelnen Anlage. Je
 * Arbeitsschritt gehen hundert Millibar Wasser weg und kommen als Abdampf wieder heraus; ohne
 * Wasser steht sie still. Die Anlage kuehlt sich selbst.
 *
 * DIE KUEHLUNG HAT EIGENE ANSCHLUESSE, und dahinter steckt der dynamische Stellvertreter: an den
 * vier Kuehlanschluessen sieht ein Rohr NUR den Wasser- und den Abdampftank, nicht die
 * Rezeptfluessigkeiten. Ohne diese Trennung liefe eine Wasserleitung Gefahr, in einem
 * Rezeptfeld zu landen, das gerade auch Wasser braucht.
 *
 * SIE TEILT FLUESSIGKEITEN INTERN. Was ein Feld ausgibt und ein anderes braucht, wandert bis zu
 * fuenfzig Millibar je Tick direkt hinueber, ohne den Umweg ueber ein Rohr. So laesst sich eine
 * Kette aus vier Schritten in EINER Maschine fahren.
 *
 * IHR STROMSPEICHER RICHTET SICH NACH DEN REZEPTEN: er fasst das Hundertfache dessen, was die
 * vier eingestellten Rezepte zusammen je Schritt brauchen, mindestens aber eine Million.
 *
 * NICHT UEBERNOMMEN: die Anbindung an Redstone-ueber-Funk. Sie gibt es im Port noch nicht.
 */
public class MachineChemicalFactoryBlockEntity extends MachineBaseBlockEntity
        implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IControlReceiver, IProxyDelegateProvider {

    public static final int FIELDS = 4;
    public static final int SLOTS = 32;

    public final FluidTank[] inputTanks = new FluidTank[12];
    public final FluidTank[] outputTanks = new FluidTank[12];
    public final FluidTank[] allTanks = new FluidTank[26];

    public final FluidTank water;
    public final FluidTank lps;

    public long power;
    public long maxPower = 1_000_000;

    public final boolean[] didProcess = new boolean[FIELDS];

    public boolean frame = false;
    public int anim;
    public int prevAnim;

    private AudioWrapper audio;

    public final ModuleMachineChemicalPlant[] modules = new ModuleMachineChemicalPlant[FIELDS];
    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    private final CoolantDelegate delegate = new CoolantDelegate();
    private AABB renderBox;

    public MachineChemicalFactoryBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_CHEMICAL_FACTORY.get(), pos, state, SLOTS);

        for(int i = 0; i < 12; i++) {
            this.inputTanks[i] = new FluidTank(Fluids.NONE, 24_000);
            this.outputTanks[i] = new FluidTank(Fluids.NONE, 24_000);
        }

        this.water = new FluidTank(Fluids.WATER, 4_000);
        this.lps = new FluidTank(Fluids.SPENTSTEAM, 4_000);

        System.arraycopy(this.inputTanks, 0, this.allTanks, 0, 12);
        System.arraycopy(this.outputTanks, 0, this.allTanks, 12, 12);
        this.allTanks[24] = this.water;
        this.allTanks[25] = this.lps;

        for(int i = 0; i < FIELDS; i++) {
            this.modules[i] = new ModuleMachineChemicalPlant(i, this, this.slots)
                    .itemInput(5 + i * 7, 6 + i * 7, 7 + i * 7)
                    .itemOutput(8 + i * 7, 9 + i * 7, 10 + i * 7)
                    .fluidInput(this.inputTanks[i * 3], this.inputTanks[1 + i * 3], this.inputTanks[2 + i * 3])
                    .fluidOutput(this.outputTanks[i * 3], this.outputTanks[1 + i * 3], this.outputTanks[2 + i * 3]);
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineChemicalFactory");
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(this.maxPower <= 0) this.maxPower = 10_000_000;

        if(!this.level.isClientSide) {

            long nextMaxPower = 0;

            for(int i = 0; i < FIELDS; i++) {
                GenericRecipe recipe = this.modules[i].getRecipe();
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

                this.modules[i].update(speed * 2D, pow * 2D, this.canCool(), this.slots.get(4 + i * 7));
                this.didProcess[i] = this.modules[i].didProcess;
                changed |= this.modules[i].markDirty;

                if(this.modules[i].didProcess) {
                    this.water.setFill(this.water.getFill() - 100);
                    this.lps.setFill(this.lps.getFill() + 100);
                }
            }

            this.shareFluids();

            if(changed) this.setChanged();

            this.networkPackNT(100);

        } else {

            this.prevAnim = this.anim;

            boolean didSomething = false;
            for(boolean b : this.didProcess) didSomething |= b;

            if(didSomething) this.anim++;

            if(this.level.getGameTime() % 20 == 0) {
                this.frame = !this.level.getBlockState(this.worldPosition.above(3)).isAir();
            }

            if(didSomething && NuclearTechMod.proxy.me() != null
                    && Math.sqrt(NuclearTechMod.proxy.me().distanceToSqr(this.getBlockPos().getBottomCenter())) < 50) {

                if(this.audio == null) {
                    this.audio = this.createAudioLoop();
                    this.audio.startSound();
                } else if(!this.audio.isPlaying()) {
                    this.audio = rebootAudio(this.audio);
                }

                this.audio.keepAlive();
                this.audio.updateVolume(this.getVolume(1F));

            } else if(this.audio != null) {
                this.audio.stopSound();
                this.audio = null;
            }
        }
    }

    /**
     * Was ein Feld ausgibt und ein anderes braucht, wandert direkt hinueber. Druck und Stoff
     * muessen passen; mehr als fuenfzig Millibar je Paar und Tick gehen nicht.
     */
    private void shareFluids() {

        for(FluidTank in : this.inputTanks) {

            if(in.getTankType() == Fluids.NONE) continue;

            for(FluidTank out : this.outputTanks) {

                if(out.getTankType() != in.getTankType()) continue;
                if(out.getPressure() != in.getPressure()) continue;

                int toMove = BobMathUtil.min(in.getMaxFill() - in.getFill(), out.getFill(), 50);

                if(toMove > 0) {
                    in.setFill(in.getFill() + toMove);
                    out.setFill(out.getFill() - toMove);
                }
            }
        }
    }

    public boolean canCool() {
        return this.water.getFill() >= 100 && this.lps.getFill() <= this.lps.getMaxFill() - 100;
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return AudioWrapper.getLoopedSound(NtmSoundEvents.CHEMICAL_PLANT_OPERATE.get(), SoundSource.BLOCKS, this, 1F, 15F, 1F, 20);
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
    // Anschluesse
    // ------------------------------------------------------------------------------------

    private Direction facing() {
        return this.getBlockState().getValue(DummyableBlock.FACING);
    }

    /** Zwoelf am Rand, zehn oben, vier an den Rezeptfeldern -- die Zahlen sind die des Originals. */
    public DirPos[] getConPos() {

        Direction dir = this.facing();
        Direction rot = dir.getClockWise(Axis.Y);
        BlockPos p = this.worldPosition;

        DirPos[] pos = new DirPos[26];
        int i = 0;

        for(int k = -2; k <= 2; k += 2) {
            pos[i++] = new DirPos(p.offset(3, 0, k), Direction.EAST);
            pos[i++] = new DirPos(p.offset(-3, 0, k), Direction.WEST);
            pos[i++] = new DirPos(p.offset(k, 0, 3), Direction.SOUTH);
            pos[i++] = new DirPos(p.offset(k, 0, -3), Direction.NORTH);
        }

        for(int k = -2; k <= 2; k++) {
            pos[i++] = new DirPos(p.offset(dir.getStepX() * k + rot.getStepX() * 2, 3, dir.getStepZ() * k + rot.getStepZ() * 2), Direction.UP);
            pos[i++] = new DirPos(p.offset(dir.getStepX() * k - rot.getStepX() * 2, 3, dir.getStepZ() * k - rot.getStepZ() * 2), Direction.UP);
        }

        for(DirPos io : this.getIOPos()) pos[i++] = io;

        return pos;
    }

    /** Die vier Kuehlanschluesse an den Stirnseiten. */
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

    /** Die vier Gueteranschluesse an den Laengsseiten, einer je Rezeptfeld. */
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

    /** An den vier Kuehlanschluessen sieht ein Rohr nur Wasser und Abdampf. */
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

        @Override public long getPower() { return MachineChemicalFactoryBlockEntity.this.getPower(); }
        @Override public void setPower(long power) { MachineChemicalFactoryBlockEntity.this.setPower(power); }
        @Override public long getMaxPower() { return MachineChemicalFactoryBlockEntity.this.getMaxPower(); }
        @Override public boolean isLoaded() { return MachineChemicalFactoryBlockEntity.this.isLoaded(); }

        @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { MachineChemicalFactoryBlockEntity.this.water }; }
        @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { MachineChemicalFactoryBlockEntity.this.lps }; }
        @Override public FluidTank[] getAllTanks() { return MachineChemicalFactoryBlockEntity.this.getAllTanks(); }
    }

    // ------------------------------------------------------------------------------------
    // Faecher
    // ------------------------------------------------------------------------------------

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {

        if(slot == 0) return true;
        if(slot >= 1 && slot <= 3) return stack.getItem() instanceof MachineUpgradeItem;

        for(int i = 0; i < FIELDS; i++) if(slot == 4 + i * 7) return stack.getItem() == NtmItems.BLUEPRINTS.get();
        for(int i = 0; i < FIELDS; i++) if(this.modules[i].isItemValid(slot, stack)) return true;

        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {

        for(int i = 0; i < FIELDS; i++) {
            if(index >= 8 + i * 7 && index <= 10 + i * 7) return true;
            if(this.modules[i].isSlotClogged(index)) return true;
        }

        return false;
    }

    @Override public int[] getSlotsForFace(Direction direction) { return ACCESS; }

    private static final int[] ACCESS = access();

    private static int[] access() {
        int[] slots = new int[FIELDS * 6];
        int i = 0;
        for(int f = 0; f < FIELDS; f++) for(int k = 0; k < 6; k++) slots[i++] = 5 + f * 7 + k;
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
        return new MachineChemicalFactoryMenu(id, inventory, this);
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

        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_CHEMICAL_FACTORY.get()));

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
        for(ModuleMachineChemicalPlant module : this.modules) module.serialize(buf);
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
        for(ModuleMachineChemicalPlant module : this.modules) module.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for(int i = 0; i < 12; i++) {
            this.inputTanks[i].readFromNBT(tag, "i" + i);
            this.outputTanks[i].readFromNBT(tag, "o" + i);
        }
        this.water.readFromNBT(tag, "w");
        this.lps.readFromNBT(tag, "s");
        this.power = tag.getLong("power");
        this.maxPower = tag.getLong("maxPower");
        for(ModuleMachineChemicalPlant module : this.modules) module.readFromNBT(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for(int i = 0; i < 12; i++) {
            this.inputTanks[i].writeToNBT(tag, "i" + i);
            this.outputTanks[i].writeToNBT(tag, "o" + i);
        }
        this.water.writeToNBT(tag, "w");
        this.lps.writeToNBT(tag, "s");
        tag.putLong("power", this.power);
        tag.putLong("maxPower", this.maxPower);
        for(ModuleMachineChemicalPlant module : this.modules) module.writeToNBT(tag);
    }

    public AABB getRenderBoundingBox() {

        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 2, p.getY(), p.getZ() - 2, p.getX() + 3, p.getY() + 3, p.getZ() + 3);
        }

        return this.renderBox;
    }
}
