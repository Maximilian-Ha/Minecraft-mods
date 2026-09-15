package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineRockMillMenu;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.NtmItems;
import com.hbm.lib.Library;
import com.hbm.module.machine.ModuleMachineRockMill;
import com.hbm.util.BobMathUtil;
import com.hbm.util.fauxpointtwelve.BlockPosNT;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: TileEntityMachineRockMill.
 */
public class MachineRockMillBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IControlReceiver {

    public final FluidTank[] inputTanks = new FluidTank[1];
    public final FluidTank[] outputTanks = new FluidTank[1];

    public long power;
    public long maxPower = 2_500;
    public boolean didProcess = false;

    /** Nur Client: Drehung des Mahlrads */
    public float rotation;
    public float prevRotation;
    public float rotationSpeed = 0F;
    public static final float ACCELERATION = 0.1F;
    public static final float MAX_SPEED = 15F;

    /** Nur Client: ob der Traeger ueber der Muehle steht */
    public boolean frame = false;

    public final ModuleMachineRockMill rockMillModule;

    private AABB renderBox;

    public MachineRockMillBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_ROCK_MILL.get(), pos, state, 8);

        this.inputTanks[0] = new FluidTank(Fluids.NONE, 4_000);
        this.outputTanks[0] = new FluidTank(Fluids.NONE, 4_000);

        this.rockMillModule = new ModuleMachineRockMill(0, this, this.slots)
                .itemInput(2).itemOutput(5)
                .fluidInput(this.inputTanks[0]).fluidOutput(this.outputTanks[0]);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineRockMill");
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(this.maxPower <= 0) this.maxPower = 2_500;

        if(!this.level.isClientSide) {

            GenericRecipe recipe = this.rockMillModule.getRecipe();
            if(recipe != null) {
                this.maxPower = recipe.power * 100;
            }

            this.maxPower = BobMathUtil.max(this.power, this.maxPower, 2_500);

            this.power = Library.chargeTEFromItems(this.slots, 0, this.power, this.maxPower);

            for(DirPos pos : this.getConPos()) {
                this.trySubscribe(this.level, pos);
                for(FluidTank tank : this.inputTanks) if(tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), this.level, pos);
                for(FluidTank tank : this.outputTanks) if(tank.getFill() > 0) this.tryProvide(tank, this.level, pos);
            }

            this.rockMillModule.update(1D, 1D, true, this.slots.get(1));
            this.didProcess = this.rockMillModule.didProcess;
            if(this.rockMillModule.markDirty) this.setChanged();

            if(this.didProcess && (this.level.getGameTime() + BlockPosNT.getIdentity(this.getBlockPos())) % 3 == 0) {
                // Schrittgeraeusch des gemahlenen Blocks, ersatzweise Stein
                SoundEvent sound = Blocks.STONE.getSoundType(Blocks.STONE.defaultBlockState(), this.level, this.getBlockPos(), null).getStepSound();

                if(recipe != null && recipe.getIcon().getItem() instanceof BlockItem blockItem) {
                    BlockState iconState = blockItem.getBlock().defaultBlockState();
                    sound = blockItem.getBlock().getSoundType(iconState, this.level, this.getBlockPos(), null).getStepSound();
                }

                this.level.playSound(null, this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 1.5D, this.worldPosition.getZ() + 0.5D,
                        sound, SoundSource.BLOCKS, this.getVolume(1.0F), 0.75F);
            }

            this.networkPackNT(100);

        } else {

            this.prevRotation = this.rotation;

            this.rotationSpeed += ACCELERATION * (this.didProcess ? 1 : -1);
            this.rotationSpeed = Mth.clamp(this.rotationSpeed, 0F, MAX_SPEED);

            this.rotation += this.rotationSpeed;

            if(this.rotation >= 360F) {
                this.prevRotation -= 360F;
                this.rotation -= 360F;
            }

            if(this.level.getGameTime() % 20 == 0) {
                this.frame = !this.level.getBlockState(this.worldPosition.above(3)).isAir();
            }

            // weggelassen: der "blockdust"-Partikelwirbel des Originals -- der Modus
            // "blockdust" fehlt in NuclearTechModClient.effectNT
        }
    }

    public DirPos[] getConPos() {
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x + 3, y, z + 1, Library.POS_X),
                new DirPos(x + 3, y, z - 1, Library.POS_X),
                new DirPos(x - 3, y, z + 1, Library.NEG_X),
                new DirPos(x - 3, y, z - 1, Library.NEG_X),
                new DirPos(x + 1, y, z + 3, Library.POS_Z),
                new DirPos(x - 1, y, z + 3, Library.POS_Z),
                new DirPos(x + 1, y, z - 3, Library.NEG_Z),
                new DirPos(x - 1, y, z - 3, Library.NEG_Z)
        };
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        for(FluidTank tank : this.inputTanks) tank.serialize(buf);
        for(FluidTank tank : this.outputTanks) tank.serialize(buf);
        buf.writeLong(this.power);
        buf.writeLong(this.maxPower);
        buf.writeBoolean(this.didProcess);
        this.rockMillModule.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(FluidTank tank : this.inputTanks) tank.deserialize(buf);
        for(FluidTank tank : this.outputTanks) tank.deserialize(buf);
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        this.didProcess = buf.readBoolean();
        this.rockMillModule.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.inputTanks[0].readFromNBT(tag, "i0");
        this.outputTanks[0].readFromNBT(tag, "o0");
        this.power = tag.getLong("power");
        this.maxPower = tag.getLong("maxPower");
        this.rockMillModule.readFromNBT(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.inputTanks[0].writeToNBT(tag, "i0");
        this.outputTanks[0].writeToNBT(tag, "o0");
        tag.putLong("power", this.power);
        tag.putLong("maxPower", this.maxPower);
        this.rockMillModule.writeToNBT(tag);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == 0) return stack.getItem() instanceof IBatteryItem;
        if(slot == 1 && stack.getItem() == NtmItems.BLUEPRINTS.get()) return true;
        return this.rockMillModule.isItemValid(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return (index >= 5 && index <= 7) || this.rockMillModule.isSlotClogged(index);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] {2, 3, 4, 5, 6, 7};
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return this.maxPower; }

    @Override public FluidTank[] getReceivingTanks() { return this.inputTanks; }
    @Override public FluidTank[] getSendingTanks() { return this.outputTanks; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] {this.inputTanks[0], this.outputTanks[0]}; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineRockMillMenu(id, inventory, this);
    }

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("index") && tag.contains("selection")) {
            int index = tag.getInt("index");
            if(index == 0) {
                this.rockMillModule.recipe = tag.getString("selection");
                this.setChanged();
            }
        }
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 2, y, z - 2, x + 3, y + 3, z + 3);
        }
        return this.renderBox;
    }
}
