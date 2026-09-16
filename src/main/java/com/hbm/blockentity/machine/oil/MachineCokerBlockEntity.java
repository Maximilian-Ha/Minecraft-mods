package com.hbm.blockentity.machine.oil;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import api.hbm.tile.IHeatSource;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineCokerMenu;
import com.hbm.inventory.recipes.CokerRecipes;
import com.hbm.particle.NtmParticleTypes;
import com.hbm.particle.vanilla.NbtParticleOptions;
import com.hbm.util.Tuple.Triplet;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.util.particle.ParticleUtil;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.oil.TileEntityMachineCoker.
 *
 * Der Verkoker treibt aus einem Oel den Kohlenstoff aus. Er haengt nicht am Stromnetz, sondern
 * am Waermenetz: was unter ihm an Waerme anliegt, zieht er zu einem Viertel ab und heizt damit
 * den Vorgang. Ein Durchgang kostet 20.000 Waermeeinheiten.
 *
 * ABWEICHUNG: die Rauchfahne des Originals steigt aus der Partikelsorte "tower" auf; im Port
 * heisst sie COOLING_TOWER und wird genauso benutzt.
 */
public class MachineCokerBlockEntity extends MachineBaseBlockEntity implements IFluidStandardTransceiverMK2 {

    public static final int PROCESS_TIME = 20_000;
    public static final int MAX_HEAT = 100_000;
    /** Wie viel des Waermegefaelles je Tick uebernommen wird. */
    public static final double DIFFUSION = 0.25D;

    public static final int SLOT_FLUID_ID = 0;
    public static final int SLOT_OUTPUT = 1;

    public boolean wasOn;
    public int progress;
    public int heat;

    public final FluidTank[] tanks;

    public MachineCokerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_COKER.get(), pos, state, 2);
        this.tanks = new FluidTank[] {
                new FluidTank(Fluids.HEAVYOIL, 16_000),
                new FluidTank(Fluids.OIL_COKER, 8_000)
        };
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machine_coker");
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(!this.level.isClientSide) {

            this.tryPullHeat();
            this.tanks[0].setType(SLOT_FLUID_ID, this.slots);

            if(this.level.getGameTime() % 20 == 0) {
                for(DirPos pos : this.getConPos()) {
                    this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
                }
            }

            this.wasOn = false;

            if(this.canProcess()) {

                /* Ein Hundertstel der gespeicherten Waerme geht je Tick in den Vorgang. */
                int burn = this.heat / 100;

                if(burn > 0) {
                    this.wasOn = true;
                    this.progress += burn;
                    this.heat -= burn;

                    if(this.progress >= PROCESS_TIME) {
                        this.progress -= PROCESS_TIME;
                        this.finishRecipe();
                        this.setChanged();
                    }
                }

                if(this.wasOn && this.level.getGameTime() % 5 == 0) {
                    PollutionHandler.incrementPollution(this.level, this.worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 5);
                }
            }

            for(DirPos pos : this.getConPos()) {
                if(this.tanks[1].getFill() > 0) this.tryProvide(this.tanks[1], this.level, pos);
            }

            this.networkPackNT(25);

        } else if(this.wasOn && this.level.getGameTime() % 2 == 0) {
            this.spawnPlume();
        }
    }

    private void finishRecipe() {

        Triplet<Integer, ItemStack, FluidStack> recipe = CokerRecipes.getOutput(this.tanks[0].getTankType());
        if(recipe == null) return;

        ItemStack output = recipe.getY();

        if(output != null) {
            ItemStack slot = this.getItem(SLOT_OUTPUT);
            if(slot.isEmpty()) {
                this.setItem(SLOT_OUTPUT, output.copy());
            } else {
                ItemStack grown = slot.copy();
                grown.grow(output.getCount());
                this.setItem(SLOT_OUTPUT, grown);
            }
        }

        FluidStack byproduct = recipe.getZ();
        if(byproduct != null) this.tanks[1].setFill(this.tanks[1].getFill() + byproduct.fill);

        this.tanks[0].setFill(this.tanks[0].getFill() - recipe.getX());
    }

    public boolean canProcess() {

        Triplet<Integer, ItemStack, FluidStack> recipe = CokerRecipes.getOutput(this.tanks[0].getTankType());
        if(recipe == null) return false;

        FluidStack byproduct = recipe.getZ();

        /* Wie im Original: der Ausgabetank nimmt schon vor dem ersten Durchgang die Sorte an,
         * damit sich das Nebenprodukt gleich abpumpen laesst. */
        if(byproduct != null) this.tanks[1].setTankType(byproduct.type);

        if(this.tanks[0].getFill() < recipe.getX()) return false;
        if(byproduct != null && byproduct.fill + this.tanks[1].getFill() > this.tanks[1].getMaxFill()) return false;

        ItemStack output = recipe.getY();
        ItemStack slot = this.getItem(SLOT_OUTPUT);

        if(output != null && !slot.isEmpty()) {
            if(!ItemStack.isSameItemSameComponents(slot, output)) return false;
            if(output.getCount() + slot.getCount() > output.getMaxStackSize()) return false;
        }

        return true;
    }

    /** Zieht Waerme aus dem Block darunter; ohne Quelle kuehlt der Verkoker langsam aus. */
    private void tryPullHeat() {

        if(this.heat >= MAX_HEAT) return;

        BlockEntity below = this.level.getBlockEntity(this.worldPosition.below());

        if(below instanceof IHeatSource source) {

            int diff = source.getHeatStored() - this.heat;

            if(diff == 0) return;

            if(diff > 0) {
                diff = (int) Math.ceil(diff * DIFFUSION);
                source.useUpHeat(diff);
                this.heat = Math.min(this.heat + diff, MAX_HEAT);
                return;
            }
        }

        this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
    }

    /** Die Rauchfahne aus dem Schlot, zweiundzwanzig Bloecke ueber dem Kern. */
    private void spawnPlume() {

        CompoundTag fx = new CompoundTag();
        fx.putFloat("lift", 10F);
        fx.putFloat("base", 0.75F);
        fx.putFloat("max", 3F);
        fx.putInt("life", 200 + this.level.random.nextInt(50));
        fx.putInt("color", 0x404040);

        ParticleUtil.addParticle(this.level, new NbtParticleOptions(NtmParticleTypes.COOLING_TOWER.get(), fx),
                this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 22D, this.worldPosition.getZ() + 0.5D);
    }

    /** Acht Anschluesse, je zwei an jeder Seite des Sockels. */
    public DirPos[] getConPos() {
        BlockPos pos = this.getBlockPos();
        return new DirPos[] {
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() + 1, Direction.EAST),
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() - 1, Direction.EAST),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() + 1, Direction.WEST),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() - 1, Direction.WEST),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() + 2, Direction.SOUTH),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() + 2, Direction.SOUTH),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() - 2, Direction.NORTH),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() - 2, Direction.NORTH)
        };
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_FLUID_ID && stack.getItem() instanceof com.hbm.items.machine.IItemFluidIdentifier;
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
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.progress = tag.getInt("progress");
        this.heat = tag.getInt("heat");
        this.tanks[0].readFromNBT(tag, "t0");
        this.tanks[1].readFromNBT(tag, "t1");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("progress", this.progress);
        tag.putInt("heat", this.heat);
        this.tanks[0].writeToNBT(tag, "t0");
        this.tanks[1].writeToNBT(tag, "t1");
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.wasOn);
        buf.writeInt(this.heat);
        buf.writeInt(this.progress);
        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.wasOn = buf.readBoolean();
        this.heat = buf.readInt();
        this.progress = buf.readInt();
        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
    }

    @Override public boolean canConnect(FluidType type, Direction dir) { return dir != null; }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0] }; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[1] }; }
    @Override public FluidTank[] getAllTanks() { return this.tanks; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineCokerMenu(id, inventory, this);
    }

    private AABB renderBox;

    /* Kein @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 3, y, z - 3, x + 4, y + 23, z + 4);
        }
        return this.renderBox;
    }
}
