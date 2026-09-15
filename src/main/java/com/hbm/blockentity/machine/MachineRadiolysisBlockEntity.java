package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineRadiolysisMenu;
import com.hbm.inventory.recipes.RadiolysisRecipes;
import com.hbm.items.machine.RTGPelletDepletedItem;
import com.hbm.items.machine.RTGPelletItem;
import com.hbm.lib.Library;
import com.hbm.util.RTGUtil;
import com.hbm.util.Tuple.Pair;
import com.hbm.util.fauxpointtwelve.DirPos;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineRadiolysis.
 *
 * Zehn RTG-Pellets bestrahlen eine Fluessigkeit und spalten sie in zwei Fraktionen. Die Waerme
 * der Pellets treibt beides an: sie wird zu Strom (zehn HE je Waermepunkt und Tick) und sie
 * bestimmt das Tempo. Ab hundert Waerme laeuft die Spaltung, und zwar umso schneller, je heisser
 * es wird -- von dreissig Ticks je Durchgang herunter auf fuenf.
 *
 * NICHT UEBERNOMMEN: die Entkeimung. Das Original hat zwei weitere Faecher, in denen es den
 * ntmContagion-Vermerk von verseuchten Gegenstaenden abstrahlt. Dieser Vermerk gehoert zum
 * MKU-Seuchenzweig -- Uebertragung ueber die Luft, Schutzanzugpruefung, verseuchte Beute --, und
 * der ist im Port nicht vorhanden. Zwei Faecher anzubieten, aus denen nie etwas herauskaeme,
 * waere eine Falle; sie kommen mit dem Seuchenzweig nach. Die Faecher danach ruecken deshalb
 * auf: 0 bis 9 Pellets, 10 und 11 der Behaelterwechsel, 12 die Batterie.
 */
public class MachineRadiolysisBlockEntity extends MachineBaseBlockEntity implements IEnergyProviderMK2, IFluidStandardTransceiverMK2 {

    public static final long MAX_POWER = 1_000_000;

    /** Alle Faecher ausser der Batterie sind von aussen erreichbar, wie im Original. */
    private static final int[] SLOT_IO = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    private static final int[] SLOT_RTG = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    public long power;
    public int heat;

    public final FluidTank[] tanks = new FluidTank[3];

    public MachineRadiolysisBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_RADIOLYSIS.get(), pos, state, 13);

        for(int i = 0; i < 3; i++) this.tanks[i] = new FluidTank(Fluids.NONE, 2_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.radiolysis");
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        this.power = Library.chargeItemsFromTE(this.slots, 12, this.power, MAX_POWER);

        this.heat = RTGUtil.updateRTGs(this.slots, SLOT_RTG);
        this.power += this.heat * 10L;

        if(this.power > MAX_POWER) this.power = MAX_POWER;

        this.tanks[0].setType(10, 11, this.slots);
        this.setupTanks();

        if(this.heat > 100) {

            /* Dreissig Ticks bei hundert Waerme, herunter auf die fuenf Ticks Untergrenze. */
            int crackTime = (int) Math.max(-0.1D * (this.heat - 100) + 30, 5);

            if(this.level.getGameTime() % crackTime == 0) this.crack();
        }

        for(DirPos pos : this.getConPos()) {
            this.tryProvide(this.level, pos.makeCompat(), pos.getDir());
            this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
            if(this.tanks[1].getFill() > 0) this.tryProvide(this.tanks[1], this.level, pos);
            if(this.tanks[2].getFill() > 0) this.tryProvide(this.tanks[2], this.level, pos);
        }

        this.networkPackNT(50);
    }

    public DirPos[] getConPos() {
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();
        return new DirPos[] {
                new DirPos(x + 2, y, z, Direction.EAST),
                new DirPos(x - 2, y, z, Direction.WEST),
                new DirPos(x, y, z + 2, Direction.SOUTH),
                new DirPos(x, y, z - 2, Direction.NORTH)
        };
    }

    /** Ein Durchgang: 100 mB Eingabe werden zu den beiden Ausgabemengen des Rezepts. */
    private void crack() {

        Pair<FluidStack, FluidStack> quart = RadiolysisRecipes.getRadiolysis(this.tanks[0].getTankType());
        if(quart == null) return;

        int left = quart.getKey().fill;
        int right = quart.getValue().fill;

        if(this.tanks[0].getFill() >= 100 && this.hasSpace(left, right)) {
            this.tanks[0].setFill(this.tanks[0].getFill() - 100);
            this.tanks[1].setFill(this.tanks[1].getFill() + left);
            this.tanks[2].setFill(this.tanks[2].getFill() + right);
        }
    }

    private boolean hasSpace(int left, int right) {
        return this.tanks[1].getFill() + left <= this.tanks[1].getMaxFill()
                && this.tanks[2].getFill() + right <= this.tanks[2].getMaxFill();
    }

    /** Die beiden Ausgabebehaelter richten sich nach dem, was eingefuellt ist. */
    private void setupTanks() {

        Pair<FluidStack, FluidStack> quart = RadiolysisRecipes.getRadiolysis(this.tanks[0].getTankType());

        if(quart != null) {
            this.tanks[1].setTankType(quart.getKey().type);
            this.tanks[2].setTankType(quart.getValue().type);
        } else {
            this.tanks[0].setTankType(Fluids.NONE);
            this.tanks[1].setTankType(Fluids.NONE);
            this.tanks[2].setTankType(Fluids.NONE);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 10 || (slot < 10 && stack.getItem() instanceof RTGPelletItem);
    }

    /** Heraus darf nur, was fertig ist: ein zerfallenes Pellet oder der geleerte Behaelter. */
    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return (index < 10 && stack.getItem() instanceof RTGPelletDepletedItem) || index == 11;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return SLOT_IO;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.heat = tag.getInt("heat");
        this.tanks[0].readFromNBT(tag, "input");
        this.tanks[1].readFromNBT(tag, "output1");
        this.tanks[2].readFromNBT(tag, "output2");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        tag.putInt("heat", this.heat);
        this.tanks[0].writeToNBT(tag, "input");
        this.tanks[1].writeToNBT(tag, "output1");
        this.tanks[2].writeToNBT(tag, "output2");
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.heat);
        for(FluidTank tank : this.tanks) tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.heat = buf.readInt();
        for(FluidTank tank : this.tanks) tank.deserialize(buf);
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return MAX_POWER; }

    @Override public FluidTank[] getAllTanks() { return this.tanks; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[1], this.tanks[2] }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0] }; }

    @Override
    public boolean canConnect(FluidType type, Direction dir) {
        return dir != null && dir != Direction.DOWN;
    }

    /* Ohne @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();
        return new AABB(x - 1, y, z - 1, x + 2, y + 3, z + 2);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineRadiolysisMenu(id, inventory, this);
    }
}
