package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachinePUREXMenu;
import com.hbm.inventory.recipes.PUREXRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.NuclearTechMod;
import com.hbm.module.machine.ModuleMachinePUREX;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.BobMathUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachinePUREX.
 *
 * Die Wiederaufbereitung. Drei Eingabefaecher, sechs Ausgabefaecher, drei Eingabetanks und ein
 * Ausgabetank; welches Rezept laeuft, waehlt der Spieler ueber die Bauplanliste, und der
 * Umschaltverbund wechselt von selbst auf die Sorte, die gerade im Eingang liegt.
 *
 * NICHT UEBERNOMMEN: IRORValueProvider -- das Funkrelais (redstoneoverradio) gibt es im Port
 * noch nicht.
 */
public class MachinePUREXBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IControlReceiver {

    public final FluidTank[] inputTanks = new FluidTank[3];
    public final FluidTank[] outputTanks = new FluidTank[1];

    public long power;
    public long maxPower = 1_000_000;
    public boolean didProcess = false;

    public boolean frame = false;
    public int anim;
    public int prevAnim;
    private AudioWrapper audio;

    public final ModuleMachinePUREX purexModule;
    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    private AABB renderBox;

    public MachinePUREXBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_PUREX.get(), pos, state, 13);

        for(int i = 0; i < 3; i++) this.inputTanks[i] = new FluidTank(Fluids.NONE, 24_000);
        this.outputTanks[0] = new FluidTank(Fluids.NONE, 24_000);

        this.purexModule = new ModuleMachinePUREX(0, this, this.slots)
                .itemInput(4).itemOutput(7)
                .fluidInput(this.inputTanks[0], this.inputTanks[1], this.inputTanks[2])
                .fluidOutput(this.outputTanks[0]);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machinePUREX");
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(this.maxPower <= 0) this.maxPower = 1_000_000;

        if(!this.level.isClientSide) {

            GenericRecipe recipe = PUREXRecipes.INSTANCE.recipeNameMap.get(this.purexModule.recipe);
            if(recipe != null) this.maxPower = recipe.power * 100;

            this.maxPower = BobMathUtil.max(this.power, this.maxPower, 1_000_000);
            this.power = Library.chargeTEFromItems(this.slots, 0, this.power, this.maxPower);
            this.upgradeManager.checkSlots(this.slots, 2, 3);

            for(DirPos pos : this.getConPos()) {
                this.trySubscribe(this.level, pos);

                for(FluidTank tank : this.inputTanks) {
                    if(tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), this.level, pos);
                }

                for(FluidTank tank : this.outputTanks) {
                    if(tank.getFill() > 0) this.tryProvide(tank, this.level, pos);
                }
            }

            double speed = 1D;
            double pow = 1D;
            speed += Math.min(this.upgradeManager.getLevel(UpgradeType.SPEED), 3) / 3D;
            speed += Math.min(this.upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3);
            pow -= Math.min(this.upgradeManager.getLevel(UpgradeType.POWER), 3) * 0.25D;
            pow += Math.min(this.upgradeManager.getLevel(UpgradeType.SPEED), 3);
            pow += Math.min(this.upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3) * 10D / 3D;

            this.purexModule.update(speed, pow, true, this.slots.get(1));
            this.didProcess = this.purexModule.didProcess;
            if(this.purexModule.markDirty) this.setChanged();

            this.networkPackNT(100);

        } else {

            this.prevAnim = this.anim;

            if(this.level.getGameTime() % 20 == 0) {
                this.frame = !this.level.getBlockState(this.worldPosition.above(5)).isAir();
            }

            if(this.didProcess) {
                this.anim++;

                if(NuclearTechMod.proxy.me() != null && Math.sqrt(NuclearTechMod.proxy.me().distanceToSqr(this.getBlockPos().getBottomCenter())) < 25) {
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
            } else if(this.audio != null) {
                this.audio.stopSound();
                this.audio = null;
            }
        }
    }

    /** Dasselbe Geraeusch wie die Chemieanlage, nur tiefer -- so steht es im Original. */
    @Override
    public AudioWrapper createAudioLoop() {
        return AudioWrapper.getLoopedSound(NtmSoundEvents.CHEMICAL_PLANT_OPERATE.get(), SoundSource.BLOCKS, this, 1F, 15F, 0.75F, 15);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if(this.audio != null) { this.audio.stopSound(); this.audio = null; }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(this.audio != null) { this.audio.stopSound(); this.audio = null; }
    }

    /** Fuenf Anschlussstellen je Seite, drei Bloecke vom Kern entfernt. */
    public DirPos[] getConPos() {
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        DirPos[] positions = new DirPos[20];
        int i = 0;

        for(int o = -2; o <= 2; o++) positions[i++] = new DirPos(x + 3, y, z + o, Direction.EAST);
        for(int o = -2; o <= 2; o++) positions[i++] = new DirPos(x - 3, y, z + o, Direction.WEST);
        for(int o = -2; o <= 2; o++) positions[i++] = new DirPos(x + o, y, z + 3, Direction.SOUTH);
        for(int o = -2; o <= 2; o++) positions[i++] = new DirPos(x + o, y, z - 3, Direction.NORTH);

        return positions;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        for(FluidTank tank : this.inputTanks) tank.serialize(buf);
        for(FluidTank tank : this.outputTanks) tank.serialize(buf);
        buf.writeLong(this.power);
        buf.writeLong(this.maxPower);
        buf.writeBoolean(this.didProcess);
        this.purexModule.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(FluidTank tank : this.inputTanks) tank.deserialize(buf);
        for(FluidTank tank : this.outputTanks) tank.deserialize(buf);
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        this.didProcess = buf.readBoolean();
        this.purexModule.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for(int i = 0; i < 3; i++) this.inputTanks[i].readFromNBT(tag, "i" + i);
        this.outputTanks[0].readFromNBT(tag, "o0");
        this.power = tag.getLong("power");
        this.maxPower = tag.getLong("maxPower");
        this.purexModule.readFromNBT(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for(int i = 0; i < 3; i++) this.inputTanks[i].writeToNBT(tag, "i" + i);
        this.outputTanks[0].writeToNBT(tag, "o0");
        tag.putLong("power", this.power);
        tag.putLong("maxPower", this.maxPower);
        this.purexModule.writeToNBT(tag);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == 0) return true;
        if(slot == 1 && stack.getItem() == NtmItems.BLUEPRINTS.get()) return true;
        if(slot >= 2 && slot <= 3 && stack.getItem() instanceof MachineUpgradeItem) return true;
        return this.purexModule.isItemValid(slot, stack);
    }

    /** Heraus darf die Ausbeute -- und alles, was in einem Eingabefach feststeckt. */
    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return (index >= 7 && index <= 12) || this.purexModule.isSlotClogged(index);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] {4, 5, 6, 7, 8, 9, 10, 11, 12};
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return this.maxPower; }

    @Override public FluidTank[] getReceivingTanks() { return this.inputTanks; }
    @Override public FluidTank[] getSendingTanks() { return this.outputTanks; }
    @Override
    public FluidTank[] getAllTanks() {
        return new FluidTank[] { this.inputTanks[0], this.inputTanks[1], this.inputTanks[2], this.outputTanks[0] };
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachinePUREXMenu(id, inventory, this);
    }

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("index") && tag.contains("selection")) {
            int index = tag.getInt("index");
            if(index == 0) {
                this.purexModule.recipe = tag.getString("selection");
                this.setChanged();
            }
        }
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {
        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_PUREX.get()));
        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(KEY_SPEED, "+" + (lvl * 100 / 3) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_CONSUMPTION, "+" + (lvl * 50) + "%").withStyle(ChatFormatting.RED));
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

    /* Ohne @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 2, y, z - 2, x + 3, y + 5, z + 3);
        }
        return this.renderBox;
    }
}
